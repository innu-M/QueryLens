package com.querylens.alternative;

import com.querylens.benchmark.BenchmarkSettings;
import com.querylens.benchmark.RankingStrategy;
import com.querylens.persistence.ComparisonHistoryRepository;
import com.querylens.persistence.DatabaseInitializer;
import com.querylens.plan.SQLiteQueryPlanInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlternativeQueryCompetitionServiceTest {
    @TempDir
    Path temporaryDirectory;

    private Path targetDatabase;
    private ComparisonHistoryRepository history;

    @BeforeEach
    void setUp() throws Exception {
        targetDatabase = temporaryDirectory.resolve("target.db");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + targetDatabase);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE sailors(sid INTEGER PRIMARY KEY, name TEXT, rating INTEGER)");
            statement.execute("CREATE INDEX idx_rating ON sailors(rating)");
            statement.execute("INSERT INTO sailors VALUES (1, 'Irin', 10), (2, 'Munni', 8), (3, 'Ada', 10)");
        }
        Path historyDatabase = temporaryDirectory.resolve("history.db");
        new DatabaseInitializer().initialize(historyDatabase);
        history = new ComparisonHistoryRepository(historyDatabase);
    }

    @Test
    void generatesBenchmarksRanksAndPersistsCandidates() {
        AlternativeQueryCompetitionService service = new AlternativeQueryCompetitionService(
                new AlternativeQueryGenerator(new SQLiteIndexCatalogProvider()),
                new SQLiteReadOnlyQueryExecutor(), new SQLiteQueryPlanInspector(), history);
        BenchmarkSettings settings = new BenchmarkSettings(0, 2, Duration.ofSeconds(2), 5, RankingStrategy.MEDIAN);

        AlternativeCompetitionResult result = service.compete(
                targetDatabase, "SELECT name FROM sailors WHERE rating = 10", settings, progress -> { });

        assertTrue(result.comparisonId() > 0);
        assertTrue(result.candidates().size() >= 3);
        assertTrue(result.candidates().stream().allMatch(CompetitionCandidate::equivalent));
        assertTrue(result.candidates().stream().allMatch(candidate -> candidate.samplesNs().size() == 2));
        assertNotNull(result.winner());
        assertEquals(1, result.winner().rank());
        assertFalse(result.winner().planText().isBlank());
        assertEquals(1, history.findSessions("").size());
        assertEquals(result.candidates().size(), history.findCandidates(result.comparisonId()).size());
    }
}
