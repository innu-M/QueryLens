package com.querylens.workspace;

@FunctionalInterface
public interface SqlValidationRule {
    void validate(String sql);
}
