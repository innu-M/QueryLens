package com.querylens.model;

import java.util.List;

public record AnalysisResult(
        String queryType,
        List<String> tables,
        int joinCount,
        boolean hasWhereClause,
        List<String> whereColumns,
        int complexityScore,
        String riskLevel,
        boolean usesSelectStar,
        List<String> planSteps
) {
    public String displayText() {
        return "Query type: " + queryType + "\n"
                + "Tables: " + (tables.isEmpty() ? "Not detected" : String.join(", ", tables)) + "\n"
                + "Joins: " + joinCount + "\n"
                + "WHERE clause: " + (hasWhereClause ? "Yes" : "No") + "\n"
                + "WHERE columns: " + (whereColumns.isEmpty() ? "Not detected" : String.join(", ", whereColumns)) + "\n"
                + "Complexity: " + riskLevel + " (score " + complexityScore + ")\n"
                + "Plan: " + String.join(" → ", planSteps);
    }
}
