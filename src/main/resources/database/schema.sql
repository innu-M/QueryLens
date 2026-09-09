CREATE TABLE IF NOT EXISTS database_connections (
    id INTEGER PRIMARY KEY,
    display_name TEXT NOT NULL,
    database_path TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS query_history (
    id INTEGER PRIMARY KEY,
    connection_id INTEGER,
    sql_text TEXT NOT NULL,
    query_type TEXT NOT NULL,
    duration_ms REAL,
    executed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (connection_id) REFERENCES database_connections(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS query_analyses (
    id INTEGER PRIMARY KEY,
    history_id INTEGER NOT NULL UNIQUE,
    complexity_score INTEGER NOT NULL DEFAULT 0,
    risk_level TEXT NOT NULL,
    plan_text TEXT,
    FOREIGN KEY (history_id) REFERENCES query_history(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recommendations (
    id INTEGER PRIMARY KEY,
    analysis_id INTEGER NOT NULL,
    message TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (analysis_id) REFERENCES query_analyses(id) ON DELETE CASCADE
);
