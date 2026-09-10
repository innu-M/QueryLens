package com.querylens.workspace;

import java.util.List;

public record QueryExecutionResult(
        boolean returnsRows,
        List<String> columns,
        List<List<String>> rows,
        int affectedRows,
        long durationMillis,
        QueryAnalysis analysis) {

    public QueryExecutionResult {
        columns = List.copyOf(columns);
        rows = rows.stream().map(List::copyOf).toList();
    }
}
