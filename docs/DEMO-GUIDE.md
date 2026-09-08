# Final Demonstration Guide

## Workflow 1: Query execution and dynamic optimization

1. Open Query Analyzer.
2. Run `SELECT id, name FROM test WHERE cat = 7;`.
3. Show query results, the `SCAN test` plan, and the index recommendation.
4. Run `CREATE INDEX IF NOT EXISTS idx_cat ON test(cat);`.
5. Run the same select again.
6. Show `USING INDEX idx_cat` and explain why the index recommendation disappears.

## Workflow 2: Persistent history and recommendation lifecycle

1. Open History & Report and show saved execution records and timing.
2. Search a query and mark it reviewed.
3. Open Recommendations and mark one Pending recommendation as Applied or Dismissed.
4. Explain that State objects validate the lifecycle transition.

## Pattern Questions and Short Answers

### Why Strategy?

Optimization issues are independent. Adding a new optimization check requires one new strategy class rather than modifying a large conditional block.

### Why Adapter?

SQLite query plans and metadata use `EXPLAIN QUERY PLAN` and `PRAGMA`. The adapter keeps these details behind `QueryPlanProvider`, making future database support possible.

### Why Observer?

Execution should not know how data is stored. It publishes a result, while `PersistenceObserver` saves history, analysis, and recommendations.

### Why State?

A recommendation should move from Pending to Applied or Dismissed. State objects prevent invalid transitions.
