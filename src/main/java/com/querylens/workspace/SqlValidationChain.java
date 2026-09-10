package com.querylens.workspace;

import java.util.List;
import java.util.Locale;

public final class SqlValidationChain {
    private final List<SqlValidationRule> rules = List.of(
            sql -> {
                if (sql == null || sql.isBlank()) throw new IllegalArgumentException("Enter a SQL statement.");
            },
            sql -> {
                String trimmed = sql.strip();
                String withoutOneTrailingSemicolon = trimmed.endsWith(";")
                        ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
                if (withoutOneTrailingSemicolon.contains(";")) {
                    throw new IllegalArgumentException("Run one SQL statement at a time.");
                }
            },
            sql -> {
                String keyword = sql.stripLeading().split("\\s+", 2)[0].toUpperCase(Locale.ROOT);
                if (keyword.equals("DROP") || keyword.equals("ALTER") || keyword.equals("ATTACH") || keyword.equals("VACUUM")) {
                    throw new IllegalArgumentException("This workspace blocks schema-changing statements. Use SELECT, INSERT, UPDATE, or DELETE.");
                }
            });

    public void validate(String sql) {
        rules.forEach(rule -> rule.validate(sql));
    }
}
