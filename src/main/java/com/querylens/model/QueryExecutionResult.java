package com.querylens.model;

import java.util.List;

public record QueryExecutionResult(
        List<String> columnNames,
        List<List<String>> rows,
        long executionTimeMs,
        String status,
        boolean slow
) {
}
