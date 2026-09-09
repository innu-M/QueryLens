# Comparison History & Reports

This feature stores completed SQL candidate comparisons in QueryLens's private workspace database and presents them in a searchable JavaFX report.

## Responsibilities

- Store one comparison session with its database path, original SQL, and ranking strategy.
- Store every candidate's SQL, rank, median, P95, equivalence result, explanation, and query plan.
- Retain every measured duration so results remain auditable instead of keeping only aggregates.
- Search comparison sessions by SQL or database path.
- Display overall session count, verified candidate count, average improvement, and best improvement.
- Delete a session and its candidates and runs through database foreign-key cascades.

## Integration point

The alternative-query benchmarking workflow creates a `ComparisonDraft` after it has generated candidates, verified equivalent results, benchmarked them, and assigned ranks. It then persists the completed comparison with:

```java
long comparisonId = comparisonHistoryRepository.save(comparisonDraft);
```

The repository owns SQLite statements and transactions. The benchmark service therefore does not need to know the workspace schema, while the JavaFX view only asks the repository for report data.

## Design patterns

- **Repository:** `ComparisonHistoryRepository` separates persistence and SQL from the UI and benchmark workflow.
- **Facade:** The repository's `save` operation provides one transaction for a session, its candidates, and all samples.
- **Immutable data transfer objects:** Records in `com.querylens.history` move complete comparison data between services, persistence, and the view without shared mutable state.

## Verification

Run the complete test suite:

```shell
mvn test
```

`ComparisonHistoryRepositoryTest` covers transactional persistence, ordered retrieval, case-insensitive search, summary calculation, and cascading deletion.
