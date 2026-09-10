package com.querylens.workspace;

import com.querylens.persistence.ConnectionRepository;
import com.querylens.persistence.QueryHistoryRepository;
import com.querylens.persistence.QueryAnalysisRepository;
import com.querylens.persistence.RecommendationRepository;
import com.querylens.recommendation.Recommendation;
import com.querylens.recommendation.RecommendationEngine;
import com.querylens.recommendation.RecommendationState;
import com.querylens.recommendation.RecommendationStateFactory;
import com.querylens.recommendation.RecommendationStrategyFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class QueryWorkspaceService {
    private final ConnectionRepository connections;
    private final QueryHistoryRepository history;
    private final QueryAnalysisRepository analyses;
    private final RecommendationRepository recommendations;
    private final RecommendationEngine recommendationEngine;
    private final SqlValidationChain validation;
    private final SqlClassifier classifier;
    private final SimpleQueryAnalyzer analyzer;
    private final QueryExecutionTemplate executor;

    public QueryWorkspaceService(Path workspaceDatabase) {
        this(new ConnectionRepository(workspaceDatabase), new QueryHistoryRepository(workspaceDatabase),
                new QueryAnalysisRepository(workspaceDatabase), new RecommendationRepository(workspaceDatabase),
                new RecommendationEngine(new RecommendationStrategyFactory()), new SqlValidationChain(),
                new SqlClassifier(), new SimpleQueryAnalyzer(), new SQLiteQueryExecutor());
    }

    QueryWorkspaceService(ConnectionRepository connections, QueryHistoryRepository history,
                          QueryAnalysisRepository analyses, RecommendationRepository recommendations,
                          RecommendationEngine recommendationEngine,
                          SqlValidationChain validation, SqlClassifier classifier,
                          SimpleQueryAnalyzer analyzer, QueryExecutionTemplate executor) {
        this.connections = connections;
        this.history = history;
        this.analyses = analyses;
        this.recommendations = recommendations;
        this.recommendationEngine = recommendationEngine;
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

    public List<Recommendation> recommendations() { return recommendations.findAll(); }

    public void applyRecommendation(long id) { updateRecommendation(id, true); }

    public void dismissRecommendation(long id) { updateRecommendation(id, false); }

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
        long historyId = history.save(sql, type, raw.durationMillis());
        long analysisId = analyses.save(historyId, analysis);
        List<Recommendation> generated = recommendations.saveAll(analysisId, recommendationEngine.generate(analysis));
        return new QueryExecutionResult(raw.returnsRows(), raw.columns(), raw.rows(), raw.affectedRows(), raw.durationMillis(), analysis, generated);
    }

    private void updateRecommendation(long id, boolean apply) {
        Recommendation recommendation = recommendations.findById(id);
        RecommendationState current = RecommendationStateFactory.from(recommendation.status());
        RecommendationState next = apply ? current.apply() : current.dismiss();
        recommendations.updateStatus(id, next.status());
    }
}
