package com.querylens.recommendation;

public record Recommendation(long id, long analysisId, String message, RecommendationStatus status) {
}
