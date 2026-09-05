package com.querylens.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QueryHistoryRepository {

    public void save(String databasePath, String sql, long executionTimeMs, String status) {
        String statement = """
                INSERT INTO query_history (database_path, sql_query, execution_time_ms, status)
                VALUES (?, ?, ?, ?)
                """;

        try (var connection = DatabaseManager.openHistoryConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.setString(1, databasePath);
            preparedStatement.setString(2, sql);
            preparedStatement.setLong(3, executionTimeMs);
            preparedStatement.setString(4, status);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save query history.", exception);
        }
    }
}
