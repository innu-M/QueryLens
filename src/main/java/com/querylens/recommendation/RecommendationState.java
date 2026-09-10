package com.querylens.recommendation;

public interface RecommendationState {
    RecommendationStatus status();

    RecommendationState apply();

    RecommendationState dismiss();
}
