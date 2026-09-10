package com.querylens.recommendation;

public final class RecommendationStateFactory {
    private RecommendationStateFactory() {
    }

    public static RecommendationState from(RecommendationStatus status) {
        return switch (status) {
            case PENDING -> new PendingRecommendationState();
            case APPLIED -> new AppliedRecommendationState();
            case DISMISSED -> new DismissedRecommendationState();
        };
    }
}
