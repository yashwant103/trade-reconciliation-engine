package com.github.yashwant3.reconengine.persistence;

import com.github.yashwant3.reconengine.checkpoint.CheckpointManager;
import com.github.yashwant3.reconengine.checkpoint.CheckpointState;
import com.github.yashwant3.reconengine.metrics.MetricsRegistry;
import com.github.yashwant3.reconengine.model.Break;
import com.github.yashwant3.reconengine.util.BackoffRetry;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class BreakRepository {

    private static final int BATCH_SIZE = 500;

    private static final String INSERT_SQL = """
        INSERT IGNORE INTO breaks
            (run_id, trade_id, break_type, bank_price, clearing_price, bank_quantity, clearing_quantity)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    public static void saveAll(String runId, List<Break> breaks, int resumeFromBatch) {
        int totalBatches = (int) Math.ceil(breaks.size() / (double) BATCH_SIZE);

        try (Connection conn = DbConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                for (int batchIndex = resumeFromBatch; batchIndex < totalBatches; batchIndex++) {
                    int start = batchIndex * BATCH_SIZE;
                    int end = Math.min(start + BATCH_SIZE, breaks.size());
                    int fStart = start, fEnd = end, fBatchIndex = batchIndex;

                    long batchStart = System.nanoTime();
                    BackoffRetry.withRetry(() -> {
                        ps.clearBatch();
                        for (int i = fStart; i < fEnd; i++) {
                            Break b = breaks.get(i);
                            ps.setString(1, runId);
                            ps.setString(2, b.tradeId());
                            ps.setString(3, b.type().name());
                            setNullableBigDecimal(ps, 4, b.bankPrice());
                            setNullableBigDecimal(ps, 5, b.clearingPrice());
                            setNullableInt(ps, 6, b.bankQuantity());
                            setNullableInt(ps, 7, b.clearingQuantity());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                        conn.commit();
                    }, "batch " + (fBatchIndex + 1) + "/" + totalBatches);
                    long batchEnd = System.nanoTime();
                    MetricsRegistry.recordBatchDuration((batchEnd - batchStart) / 1_000_000);;

                    CheckpointManager.save(new CheckpointState(runId, batchIndex, BATCH_SIZE, breaks.size()));

                    if (batchIndex % 50 == 0 || batchIndex == totalBatches - 1) {
                        System.out.printf("  Committed batch %d/%d%n", batchIndex + 1, totalBatches);
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Failed to save breaks for run " + runId, e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save breaks for run " + runId, e);
        }
    }

    private static void setNullableBigDecimal(PreparedStatement ps, int index, BigDecimal value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DECIMAL);
        else ps.setBigDecimal(index, value);
    }

    private static void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }
}