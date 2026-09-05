package com.querylens.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    private static final String HISTORY_DATABASE_URL = "jdbc:sqlite:data/querylens-history.db";

    private DatabaseManager() {
    }

    public static void initializeHistoryDatabase() {
        try {
            Files.createDirectories(Path.of("data"));
            try (Connection connection = DriverManager.getConnection(HISTORY_DATABASE_URL);
                 var statement = connection.createStatement()) {
                statement.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS query_history (
                            query_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            database_path TEXT NOT NULL,
                            sql_query TEXT NOT NULL,
                            execution_time_ms INTEGER NOT NULL,
                            status TEXT NOT NULL,
                            executed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                        )
                        """);
            }
        } catch (IOException | SQLException exception) {
            throw new IllegalStateException("Could not initialize QueryLens history database.", exception);
        }
    }

    public static Connection openHistoryConnection() throws SQLException {
        return DriverManager.getConnection(HISTORY_DATABASE_URL);
    }

    public static Connection openTargetConnection(String databasePath) throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
    }
}
