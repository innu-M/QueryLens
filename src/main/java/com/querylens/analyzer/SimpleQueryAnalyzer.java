package com.querylens.analyzer;

import com.querylens.model.AnalysisResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleQueryAnalyzer {

    private static final Pattern QUERY_TYPE = Pattern.compile("^\\s*(SELECT|INSERT|UPDATE|DELETE)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_NAME = Pattern.compile("\\b(?:FROM|JOIN|INTO|UPDATE)\\s+([a-zA-Z_][a-zA-Z0-9_]*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern JOIN = Pattern.compile("\\bJOIN\\b", Pattern.CASE_INSENSITIVE);

    public AnalysisResult analyze(String sql) {
        if (sql == null || sql.isBlank()) {
            return new AnalysisResult("Unknown", List.of(), 0, false, List.of(), 0, "Low", false,
                    List.of("Input"), List.of());
        }

        String queryType = findQueryType(sql);
        List<String> tables = findTables(sql);
        int joinCount = countMatches(JOIN, sql);
        boolean hasWhereClause = sql.toUpperCase(Locale.ROOT).contains("WHERE");
        boolean usesSelectStar = sql.toUpperCase(Locale.ROOT).matches("(?s).*SELECT\\s+\\*.*");

        int score = joinCount * 2;
        if (hasWhereClause) score++;
        if (usesSelectStar) score++;
        String risk = score >= 4 ? "High" : score >= 2 ? "Medium" : "Low";

        List<String> whereColumns = findWhereColumns(sql);
        return new AnalysisResult(queryType, tables, joinCount, hasWhereClause, whereColumns,
                score, risk, usesSelectStar, List.of("Plan will be captured during execution."), List.of());
    }

    private String findQueryType(String sql) {
        Matcher matcher = QUERY_TYPE.matcher(sql);
        return matcher.find() ? matcher.group(1).toUpperCase(Locale.ROOT) : "Unknown";
    }

    private List<String> findTables(String sql) {
        List<String> tables = new ArrayList<>();
        Matcher matcher = TABLE_NAME.matcher(sql);
        while (matcher.find() && !tables.contains(matcher.group(1))) {
            tables.add(matcher.group(1));
        }
        return tables;
    }

    private int countMatches(Pattern pattern, String text) {
        int count = 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private List<String> findWhereColumns(String sql) {
        Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:=|<|>|<=|>=|LIKE)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        List<String> columns = new ArrayList<>();
        while (matcher.find() && !columns.contains(matcher.group(1))) {
            columns.add(matcher.group(1));
        }
        return columns;
    }
}
