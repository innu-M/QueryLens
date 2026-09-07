package com.querylens.repository;

import com.querylens.model.RecommendationEntry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationRepository {

    public List<RecommendationEntry> findAll() {
        List<RecommendationEntry> entries = new ArrayList<>();
        try (var connection = DatabaseManager.openHistoryConnection();
             var statement = connection.prepareStatement("""
                     SELECT recommendation_id, query_id, recommendation_type, description, priority, status
                     FROM recommendations ORDER BY recommendation_id DESC
                     """);
             var results = statement.executeQuery()) {
            while (results.next()) {
                entries.add(new RecommendationEntry(results.getLong("recommendation_id"), results.getLong("query_id"),
                        results.getString("recommendation_type"), results.getString("description"),
                        results.getString("priority"), results.getString("status")));
            }
            return entries;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load recommendations.", exception);
        }
    }

    public void updateStatus(long id, String status) {
        try (var connection = DatabaseManager.openHistoryConnection();
             var statement = connection.prepareStatement("UPDATE recommendations SET status = ? WHERE recommendation_id = ?")) {
            statement.setString(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not update recommendation.", exception);
        }
    }

    public void delete(long id) {
        try (var connection = DatabaseManager.openHistoryConnection();
             var statement = connection.prepareStatement("DELETE FROM recommendations WHERE recommendation_id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not delete recommendation.", exception);
        }
    }
}
