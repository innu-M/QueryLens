package com.querylens.recommendation;

public final class AppliedRecommendationState implements RecommendationState {
    @Override
    public RecommendationStatus status() {
        return RecommendationStatus.APPLIED;
    }

    @Override
    public RecommendationState apply() {
        throw new IllegalStateException("This recommendation is already applied.");
    }

    @Override
    public RecommendationState dismiss() {
        throw new IllegalStateException("Applied recommendations cannot be dismissed.");
    }
}
