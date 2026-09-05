# QueryLens — Database Query Performance Analyzer

QueryLens is a desktop application for analyzing SQL queries and helping users identify common database-performance problems. Built with JavaFX, Maven, and SQLite, it is designed as a practical developer tool rather than a full database engine.

The application will let a user connect to a supported database, submit a SQL query, measure its execution time, inspect its structure, and receive clear optimization recommendations. Analysis results and performance history are stored in SQLite so that performance changes can be reviewed over time.

## Main Goals

- Identify the query type, referenced tables, joins, conditions, and a simple complexity score.
- Measure query execution time and flag slow queries.
- Detect common issues such as `SELECT *`, frequently filtered non-indexed columns, inefficient joins, and repeated expensive queries.
- Recommend improvements, including possible `CREATE INDEX` statements.
- Maintain query history and provide dashboard/report views for performance comparison.
- Display a simplified query-plan flow, such as `Scan → Filter → Join → Output`.

## Typical Workflow

```text
Connect Database
      ↓
Enter and Execute SQL Query
      ↓
Measure Performance
      ↓
Analyze Query Structure
      ↓
Generate Recommendations
      ↓
Save History and Refresh Dashboard/Reports
```

## Planned Technology Stack

- Java 21
- JavaFX desktop user interface
- Maven dependency and build management
- SQLite persistent storage
- JDBC database access

## Design Patterns

The project aims to apply patterns only where they solve a real design or maintainability problem. The exact set may evolve as features are implemented.

| Pattern | Intended responsibility in QueryLens |
| --- | --- |
| **Strategy** | Encapsulate independent optimization rules, such as detecting `SELECT *`, missing indexes, inefficient joins, and repeated expensive queries. New rules can be added without changing the main analysis service. |
| **Factory Method** | Create the appropriate query analyzer for a query type, such as `SELECT`, `INSERT`, or `UPDATE`. |
| **Facade** | Provide one simple service entry point that coordinates execution, timing, analysis, recommendation generation, persistence, and UI updates. |
| **Observer** | Notify independent components—dashboard, history logger, and report generator—when a query analysis is completed. |
| **Chain of Responsibility** | Run the analysis as an ordered series of focused checks: validation, complexity, joins, index usage, and recommendations. |
| **Builder** | Assemble a complete `QueryAnalysisResult`, whose optional data may include tables, joins, timing metrics, query-plan steps, risk level, and recommendations. |
| **Command** | Represent user actions such as executing a query, applying a safe test index, or exporting a report. This also provides a foundation for action history. |
| **Adapter** | Isolate database-specific query-plan behavior. The initial version targets SQLite, while future adapters could support MySQL or PostgreSQL. |
| **Repository** | Separate SQLite persistence code from business logic through repositories for query history, recommendations, database connections, and index information. |

## Core Data

QueryLens will persist information about database connections, query history, query analyses, optimization recommendations, index information, and performance metrics. This allows users to compare executions, identify slow-query trends, and evaluate whether an optimization improved performance.

## Project Status

This repository is in the initial setup and design phase. The current focus is establishing a clean JavaFX/Maven/SQLite foundation and implementing the core query-analysis workflow before adding advanced reports or additional database support.
