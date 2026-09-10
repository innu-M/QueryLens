package com.querylens;

import com.querylens.alternative.AlternativeQueryCompetitionService;
import com.querylens.alternative.AlternativeQueryGenerator;
import com.querylens.alternative.SQLiteIndexCatalogProvider;
import com.querylens.alternative.SQLiteReadOnlyQueryExecutor;
import com.querylens.persistence.DatabaseInitializer;
import com.querylens.persistence.ComparisonHistoryRepository;
import com.querylens.plan.PlanComparisonService;
import com.querylens.plan.SQLiteQueryPlanInspector;
import com.querylens.ui.BenchmarkControlsView;
import com.querylens.ui.AlternativeCompetitionView;
import com.querylens.ui.ComparisonHistoryView;
import com.querylens.ui.PlanTreeComparisonView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Path databasePath = Path.of("data", "querylens.db");
        new DatabaseInitializer().initialize(databasePath);
        ComparisonHistoryRepository historyRepository = new ComparisonHistoryRepository(databasePath);

        TabPane navigation = new TabPane();
        navigation.getTabs().add(new Tab("Benchmark", createBenchmarkWorkspace(databasePath)));
        AlternativeQueryCompetitionService competitionService = new AlternativeQueryCompetitionService(
                new AlternativeQueryGenerator(new SQLiteIndexCatalogProvider()),
                new SQLiteReadOnlyQueryExecutor(),
                new SQLiteQueryPlanInspector(),
                historyRepository);
        navigation.getTabs().add(new Tab("Alternative Competition",
                new AlternativeCompetitionView(competitionService)));
        navigation.getTabs().add(new Tab("Comparison History",
                new ComparisonHistoryView(historyRepository)));
        navigation.getTabs().add(new Tab("Plan Trees",
                new PlanTreeComparisonView(new PlanComparisonService(new SQLiteQueryPlanInspector()))));
        navigation.getTabs().forEach(tab -> tab.setClosable(false));

        Scene scene = new Scene(navigation, 1180, 760);
        stage.setTitle("QueryLens");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createBenchmarkWorkspace(Path databasePath) {
        Label title = new Label("QueryLens");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label message = new Label("Your local query analysis workspace is ready.");
        Label database = new Label("Workspace database: " + databasePath.toAbsolutePath());
        Label benchmarkHeading = new Label("Benchmark controls");
        benchmarkHeading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        VBox root = new VBox(12, title, message, database, benchmarkHeading, new BenchmarkControlsView());
        root.setPadding(new Insets(28));
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
