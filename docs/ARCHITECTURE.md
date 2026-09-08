# QueryLens Architecture and Design Decisions

## Purpose

QueryLens is a JavaFX developer tool that executes SQLite queries, measures their execution time, reads SQLite's actual `EXPLAIN QUERY PLAN` output, stores query history, and creates optimization recommendations.

## Architecture

```text
ui → command → service facade → analyzer / plan provider → repositories → SQLite
```

The JavaFX package contains controls and event handlers only. Query analysis, recommendations, state transitions, execution, and persistence are implemented outside the UI.

## Applied Patterns

| Pattern | Problem | Implementation | Future benefit |
| --- | --- | --- | --- |
| Strategy | Each optimization issue has different detection logic. | `RecommendationStrategy` with select-star, join, full-scan, automatic-index, and temporary-B-tree strategies. | New optimization rules can be added without changing the engine. |
| Repository | SQL in UI or services would mix responsibilities. | Connection, history, analysis, recommendation, and index-catalog repositories. | Persistence can be changed or tested independently. |
| Facade / Service Layer | Query execution requires many coordinated steps. | `QueryExecutionService`. | UI needs one execution method instead of knowing every subsystem. |
| Factory | Creating all strategies in the engine causes tight coupling. | `RecommendationStrategyFactory`. | A different strategy set can be supplied later. |
| Builder | `AnalysisResult` has many optional fields. | `AnalysisResultBuilder`. | New analysis fields can be added without long constructors. |
| Adapter | SQLite uses database-specific `EXPLAIN` and `PRAGMA` APIs. | `QueryPlanProvider` and `SQLiteQueryPlanInspector`. | MySQL or PostgreSQL providers can be introduced later. |
| Template Method | Query analyzers share a fixed parse-score-build process. | `QueryAnalysisTemplate`. | Specialized analyzers can customize complexity without copying the workflow. |
| Command | A user query is an executable request containing data and behavior. | `ExecuteQueryCommand`. | Command history, retry, and undoable safe actions can be added later. |
| Observer | Saving results should not make execution depend on persistence details. | `QueryExecutionPublisher` and `PersistenceObserver`. | More observers, such as notifications or dashboards, can subscribe without changing execution. |
| State | Recommendations have valid lifecycle transitions. | Pending, Applied, and Dismissed state classes. | Future states such as `REJECTED` can be added with transition rules. |

## Real SQLite Optimization Logic

QueryLens does not invent numeric database costs. SQLite does not expose a PostgreSQL-style cost value. Instead, QueryLens uses SQLite's actual optimizer decisions:

- `SCAN table` without `USING ...` → possible full table scan.
- `USING INDEX ...` → the plan is using an existing index.
- `AUTOMATIC INDEX` → SQLite built a temporary automatic index.
- `USE TEMP B-TREE` → SQLite created a temporary sorting or grouping structure.

The application also checks primary keys and indexes with `PRAGMA table_info`, `PRAGMA index_list`, and `PRAGMA index_info` before suggesting a new index.

## Error Handling

- Empty database paths and SQL are rejected before execution.
- SQL/JDBC failures are shown in the UI instead of crashing the application.
- Result display is limited to 100 rows.
- Recommendation status changes are validated by State objects.
- History deletion uses a transaction to remove related analysis and recommendation records safely.
