package com.github.yashwant3.reconengine.persistence;

import com.github.yashwant3.reconengine.model.RunMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Optional;

public class RunRepository {
    public static void startRun(String runId){
        String sql= "INSERT INTO runs (run_id, status) VALUES (?, 'RUNNING')";
        try(Connection conn= DbConnectionPool.getConnection()){
            PreparedStatement ps= conn.prepareStatement(sql);
            ps.setString(1, runId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to start run "+runId,e);
        }
    }

    public static void completeRun(RunMetadata metadata){
        String sql= """
            UPDATE runs
            SET status= 'COMPLETE',
                total_bank_trades=?,
                total_clearing_trades=?,
                matched_count=?,
                total_breaks=?,
                completed_at = CURRENT_TIMESTAMP
            WHERE run_id=?
            """;
        try(Connection conn= DbConnectionPool.getConnection()){
            PreparedStatement ps= conn.prepareStatement(sql);
            ps.setInt(1,metadata.totalBankTrades());
            ps.setInt(2,metadata.totalClearingTrades());
            ps.setInt(3,metadata.matchCount());
            ps.setInt(4,metadata.totalBreaks());
            ps.setString(5,metadata.runId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to comlpete run "+metadata.runId(),e);
        }
    }

    public static Optional<String> findRunningRunId() {
        String sql = "SELECT run_id FROM runs WHERE status = 'RUNNING' ORDER BY started_at ASC LIMIT 1";
        try (Connection conn = DbConnectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(rs.getString("run_id"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check for incomplete runs", e);
        }
    }
}
