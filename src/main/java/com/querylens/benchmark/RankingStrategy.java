package com.querylens.benchmark;

/** Defines how verified benchmark candidates are ordered. */
public enum RankingStrategy {
    MEDIAN,
    AVERAGE,
    P95,
    STABILITY
}
