# Plan-Tree Visualization

The Plan Trees tab turns SQLite's flat `EXPLAIN QUERY PLAN` rows into expandable parent-child trees and compares an original `SELECT` with an alternative.

## Workflow

1. Choose an existing SQLite database file.
2. Enter the original and alternative `SELECT` queries.
3. Select **Compare plan trees**.
4. Compare the color-coded operator trees and structural verdict.
5. Select a node to read its specific explanation.

Orange scan nodes warn about operations that may inspect every row. Green search nodes show index-assisted access. Red temporary B-tree nodes identify extra sorting, grouping, or duplicate-removal work. Purple nodes represent compound operations with multiple branches.

The structural verdict is guidance, not a timing claim. QueryLens explicitly advises benchmarking both candidates because row counts, cached pages, data distribution, and result sizes can affect real performance.

## Safety

`SQLiteQueryPlanInspector` accepts one `SELECT` or common-table-expression statement at a time. It rejects modifying SQL and multiple statements, enables SQLite `query_only` mode, and executes only `EXPLAIN QUERY PLAN`.

## Design patterns

- **Adapter:** `SQLiteQueryPlanInspector` converts SQLite-specific result columns into stable `QueryPlanRow` values behind `QueryPlanProvider`.
- **Composite:** `QueryPlanNode` represents both operators and nested operator groups through the same `QueryPlanComponent` interface.
- **Builder:** `QueryPlanTreeBuilder` assembles flat `id` and `parent` rows into one or more tree roots.
- **Visitor:** `PlanExplanationVisitor` traverses the tree and produces an explanation for every node without adding recommendation rules to the node model.
- **Facade/Service:** `PlanComparisonService` coordinates inspection, building, explanation, structural comparison, and the final verdict for the UI.

## Verification

Run:

```shell
mvn test
```

The tests cover hierarchy construction, operator classification, orphan and duplicate IDs, visitor traversal, write and multi-statement rejection, real SQLite plan inspection, and original-versus-alternative comparison.
