package com.querylens.recommendation;

import com.querylens.workspace.QueryAnalysis;

import java.util.List;

public final class JoinIndexRecommendationStrategy implements RecommendationStrategy {
    @Override
    public List<String> recommend(QueryAnalysis analysis) {
        return List.of("Check that both sides of the join use indexed columns.");
    }
}
