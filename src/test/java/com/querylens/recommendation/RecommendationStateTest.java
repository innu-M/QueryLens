package com.querylens.recommendation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecommendationStateTest {
    @Test
    void pendingRecommendationCanBeAppliedOrDismissed() {
        RecommendationState pending = new PendingRecommendationState();

        assertEquals(RecommendationStatus.APPLIED, pending.apply().status());
        assertEquals(RecommendationStatus.DISMISSED, pending.dismiss().status());
    }

    @Test
    void completedStatesCannotMoveBackwards() {
        assertThrows(IllegalStateException.class, () -> new AppliedRecommendationState().dismiss());
        assertThrows(IllegalStateException.class, () -> new DismissedRecommendationState().apply());
    }
}
