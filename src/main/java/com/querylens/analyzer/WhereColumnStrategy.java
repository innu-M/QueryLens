package com.querylens.analyzer;

import com.querylens.model.AnalysisResult;
import com.querylens.model.Recommendation;

import java.util.Optional;

public class WhereColumnStrategy implements RecommendationStrategy {
    @Override
    public Optional<Recommendation> recommend(AnalysisResult analysis) {
        if (analysis.whereColumns().isEmpty() || analysis.tables().isEmpty()) return Optional.empty();
        String column = analysis.whereColumns().getFirst();
        String table = analysis.tables().getFirst();
        return Optional.of(new Recommendation("Index",
                "If this query is used often, consider: CREATE INDEX idx_" + table + "_" + column
                        + " ON " + table + "(" + column + ");", "HIGH"));
    }
}
