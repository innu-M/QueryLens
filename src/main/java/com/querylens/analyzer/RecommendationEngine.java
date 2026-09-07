package com.querylens.analyzer;

import com.querylens.model.AnalysisResult;
import com.querylens.model.Recommendation;

import java.util.List;

public class RecommendationEngine {
    private final List<RecommendationStrategy> strategies = List.of(
            new SelectStarStrategy(), new JoinStrategy(), new WhereColumnStrategy()
    );

    public List<Recommendation> recommend(AnalysisResult analysis) {
        return strategies.stream()
                .map(strategy -> strategy.recommend(analysis))
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
