package com.querylens.recommendation;

import com.querylens.workspace.QueryAnalysis;

import java.util.List;

public interface RecommendationStrategy {
    List<String> recommend(QueryAnalysis analysis);
}
