package com.querylens.sandbox;

/** Measured result of comparing a real database with an indexed temporary copy. */
public record SandboxIndexResult(long originalNanos, long indexedCopyNanos, boolean resultsMatch) {
    public double improvementPercent() {
        if (originalNanos <= 0) return 0;
        return ((originalNanos - indexedCopyNanos) * 100.0) / originalNanos;
    }
}
