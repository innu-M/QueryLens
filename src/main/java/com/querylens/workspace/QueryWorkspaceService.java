package com.querylens.workspace;

import com.querylens.persistence.ConnectionRepository;
import com.querylens.persistence.QueryHistoryRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class QueryWorkspaceService {
    private final ConnectionRepository connections;
    private final QueryHistoryRepository history;
    private final SqlValidationChain validation;
    private final SqlClassifier classifier;
    private final SimpleQueryAnalyzer analyzer;
    private final QueryExecutionTemplate executor;

    public QueryWorkspaceService(Path workspaceDatabase) {
        this(new ConnectionRepository(workspaceDatabase), new QueryHistoryRepository(workspaceDatabase),
                new SqlValidationChain(), new SqlClassifier(), new SimpleQueryAnalyzer(), new SQLiteQueryExecutor());
    }

    QueryWorkspaceService(ConnectionRepository connections, QueryHistoryRepository history,
                          SqlValidationChain validation, SqlClassifier classifier,
                          SimpleQueryAnalyzer analyzer, QueryExecutionTemplate executor) {
        this.connections = connections;
        this.history = history;
        this.validation = validation;
        this.classifier = classifier;
        this.analyzer = analyzer;
        this.executor = executor;
    }

    public SavedConnection saveConnection(String displayName, Path databasePath) {
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Enter a name for this connection.");
        if (databasePath == null || !Files.isRegularFile(databasePath)) throw new IllegalArgumentException("Choose an existing SQLite database file.");
        return connections.save(displayName, databasePath);
    }

    public List<SavedConnection> connections() { return connections.findAll(); }

    public List<QueryHistoryEntry> recentHistory() { return history.recent(20); }

    public boolean requiresMutationConfirmation(String sql) {
        validation.validate(sql);
        return classifier.classify(sql) != SqlQueryType.SELECT;
    }

    public QueryExecutionResult run(Path databasePath, String sql) {
        if (databasePath == null || !Files.isRegularFile(databasePath)) throw new IllegalArgumentException("Choose an existing SQLite database file.");
        validation.validate(sql);
        SqlQueryType type = classifier.classify(sql);
        QueryExecutionTemplate.RawQueryResult raw = executor.execute(databasePath, sql, type);
        QueryAnalysis analysis = analyzer.analyze(sql, type);
        history.save(sql, type, raw.durationMillis());
        return new QueryExecutionResult(raw.returnsRows(), raw.columns(), raw.rows(), raw.affectedRows(), raw.durationMillis(), analysis);
    }
}
