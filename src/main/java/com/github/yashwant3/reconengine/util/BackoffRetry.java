package com.github.yashwant3.reconengine.util;

import java.sql.SQLException;

public class BackoffRetry {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_DELAY_MS = 200;

    @FunctionalInterface
    public interface SqlAction {
        void run() throws SQLException;
    }

    public static void withRetry(SqlAction action, String description) throws SQLException {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                action.run();
                return;
            } catch (SQLException e) {
                if (attempt >= MAX_ATTEMPTS || !isTransient(e)) {
                    throw e;
                }
                long delay = BASE_DELAY_MS * (1L << (attempt - 1)); // 200ms, 400ms, 800ms
                System.out.printf("  Retry %d/%d for [%s] after transient error: %s (waiting %dms)%n",
                        attempt, MAX_ATTEMPTS, description, e.getMessage(), delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    private static boolean isTransient(SQLException e) {
        String sqlState = e.getSQLState();
        return sqlState != null && (sqlState.startsWith("08") || sqlState.equals("40001"));
    }
}