package com.querylens.persistence;

import com.querylens.workspace.QueryAnalysis;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class QueryAnalysisRepository {
    private final Path workspaceDatabase;

    public QueryAnalysisRepository(Path workspaceDatabase) {
        this.workspaceDatabase = workspaceDatabase;
    }

    public long save(long historyId, QueryAnalysis analysis) {
        String insert = "INSERT INTO query_analyses(history_id, complexity_score, risk_level, plan_text) VALUES (?, ?, ?, ?)";
        try (var connection = DriverManager.getConnection(url());
             var statement = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, historyId);
            statement.setInt(2, analysis.complexityScore());
            statement.setString(3, analysis.riskLevel());
            statement.setString(4, "Tables: " + analysis.tables());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new IllegalStateException("Could not retrieve query analysis ID.");
        } catch (Exception exception) {
            throw new IllegalStateException("Could not save query analysis.", exception);
        }
    }

    private String url() {
        return "jdbc:sqlite:" + workspaceDatabase.toAbsolutePath();
    }
}
