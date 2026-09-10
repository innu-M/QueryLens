# Recommendations Engine

The recommendations engine gives practical next steps after a query runs. It keeps the query workspace flow simple: run a query, analyse it, save the analysis, then save any suggestions that apply.

## Patterns used

- **Strategy:** Each rule is a small strategy. The current rules flag `SELECT *`, filtered columns, sorting or grouping, and joins.
- **Factory:** `RecommendationStrategyFactory` chooses only the rules that match the query analysis.
- **State:** A recommendation starts as `PENDING` and can move once to `APPLIED` or `DISMISSED`. Completed recommendations cannot move backwards.
- **Repository:** `QueryAnalysisRepository` and `RecommendationRepository` keep analyses and suggestions in the workspace database.

## User flow

1. Choose a connection in **Query Workspace** and run a query.
2. Read the short suggestions below the result.
3. Open **Recommendations** to mark a useful suggestion as applied or dismiss one that does not fit the database.
