# QueryLens — Database Query Performance Analyzer

QueryLens is a desktop application for analyzing SQL queries and helping users identify common database-performance problems. Built with JavaFX, Maven, and SQLite, it is designed as a practical developer tool rather than a full database engine.

The application will let a user connect to a supported database, submit a SQL query, measure its execution time, inspect its structure, and receive clear optimization recommendations. Analysis results and performance history are stored in SQLite so that performance changes can be reviewed over time.

## Current Features

- Execute SQLite queries and display up to 100 returned rows.
- Identify query type, referenced tables, joins, `WHERE` clauses, filtered columns, and a simple complexity score.
- Measure execution time and flag queries at or above 500 ms as slow.
- Create recommendations for `SELECT *`, joins, and frequently filtered columns.
- Persist database paths, query history, analysis results, recommendations, and index information in a separate SQLite history database.
- Show recent execution history and a small performance report with total, average, and slow-query counts.
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
| **Strategy** | Implemented through independent optimization rules: `SelectStarStrategy`, `JoinStrategy`, and `WhereColumnStrategy`. A new rule can be added without changing the execution service. |
| **Repository** | Implemented through repositories for connections, query history, and analysis/recommendations. SQLite code is kept out of the user interface. |
| **Service / Facade** | `QueryExecutionService` coordinates execution, timing, analysis, recommendations, and persistent storage through one simple method. |
| **Factory Method** | Planned only if distinct analyzers for `SELECT`, `INSERT`, and `UPDATE` become necessary. |

## Core Data

QueryLens will persist information about database connections, query history, query analyses, optimization recommendations, index information, and performance metrics. This allows users to compare executions, identify slow-query trends, and evaluate whether an optimization improved performance.

## Project Status

The core SQLite query-analysis workflow is implemented. Next improvements can include a dedicated connection-management screen, real SQLite `EXPLAIN QUERY PLAN` output, index inspection, and report export.

## Submission Documentation

- [Architecture and patterns](docs/ARCHITECTURE.md)
- [UML class diagram](docs/UML-CLASS-DIAGRAM.md)
- [Database ER diagram](docs/ER-DIAGRAM.md)
- [Final demonstration guide](docs/DEMO-GUIDE.md)
- [SQLite schema](src/main/resources/database/schema.sql)
- [SQLite sample data](src/main/resources/database/sample-data.sql)
