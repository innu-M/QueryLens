package com.querylens.ui;

import com.querylens.model.HistoryEntry;
import com.querylens.command.ExecuteQueryCommand;
import com.querylens.model.QueryExecutionResult;
import com.querylens.model.DatabaseConnectionEntry;
import com.querylens.model.RecommendationEntry;
import com.querylens.model.IndexEntry;
import com.querylens.repository.DatabaseConnectionRepository;
import com.querylens.repository.IndexCatalogRepository;
import com.querylens.repository.QueryHistoryRepository;
import com.querylens.repository.RecommendationRepository;
import com.querylens.service.QueryExecutionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;


public class MainView {

    private final QueryExecutionService queryService;
    private final QueryHistoryRepository historyRepository = new QueryHistoryRepository();
    private final DatabaseConnectionRepository connectionRepository = new DatabaseConnectionRepository();
    private final RecommendationRepository recommendationRepository = new RecommendationRepository();
    private final IndexCatalogRepository indexCatalogRepository = new IndexCatalogRepository();

    public MainView(QueryExecutionService queryService) {
        this.queryService = queryService;
    }

    public TabPane create() {
        Tab analyzerTab = new Tab("Query Analyzer", createAnalyzerView());
        analyzerTab.setClosable(false);
        Tab connectionsTab = new Tab("Connections", createConnectionsView());
        connectionsTab.setClosable(false);
        Tab historyTab = new Tab("History & Report", createHistoryView());
        historyTab.setClosable(false);
        Tab recommendationsTab = new Tab("Recommendations", createRecommendationsView());
        recommendationsTab.setClosable(false);
        Tab indexesTab = new Tab("Index Catalog", createIndexCatalogView());
        indexesTab.setClosable(false);
        return new TabPane(analyzerTab, connectionsTab, historyTab, recommendationsTab, indexesTab);
    }

    private BorderPane createAnalyzerView() {
        Label title = new Label("QueryLens");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        TextField databasePath = new TextField("data/demo.db");
        databasePath.setPromptText("SQLite database path, for example: data/demo.db");
        HBox.setHgrow(databasePath, Priority.ALWAYS);
        TextArea sqlInput = new TextArea("SELECT 1 AS sample_result;");
        sqlInput.setPromptText("Write one SQLite query here...");
        sqlInput.setPrefRowCount(8);
        TextArea analysisOutput = new TextArea("Run a query to view its analysis.");
        analysisOutput.setEditable(false);
        analysisOutput.setPrefRowCount(7);
        ListView<String> recommendations = new ListView<>();
        recommendations.setPlaceholder(new Label("No recommendation for this query yet."));
        recommendations.setPrefHeight(100);
        Label status = new Label("Ready. Results are saved in QueryLens history.");
        Button executeButton = new Button("Execute Query");
        TableView<List<String>> resultsTable = new TableView<>();
        resultsTable.setPlaceholder(new Label("Run a SELECT query to see its result."));
        Tab resultsTab = new Tab("Query Results", resultsTable);
        resultsTab.setClosable(false);
        Tab analysisTab = new Tab("Analysis", analysisOutput);
        analysisTab.setClosable(false);
        Tab recommendationsTab = new Tab("Recommendations", recommendations);
        recommendationsTab.setClosable(false);
        TabPane details = new TabPane(resultsTab, analysisTab, recommendationsTab);
        executeButton.setOnAction(event -> executeQuery(databasePath, sqlInput, resultsTable,
                analysisOutput, recommendations, status, details));

        HBox connectionBar = new HBox(10, new Label("SQLite database:"), databasePath);
        VBox top = new VBox(10, title, connectionBar, new Label("SQL query:"), sqlInput, executeButton, status);
        top.setPadding(new Insets(18));
        SplitPane content = new SplitPane(top, details);
        content.setOrientation(javafx.geometry.Orientation.VERTICAL);
        content.setDividerPositions(0.43);
        BorderPane root = new BorderPane();
        root.setCenter(content);
        BorderPane.setMargin(content, new Insets(0, 18, 18, 18));
        return root;
    }

    private void executeQuery(TextField databasePath, TextArea sqlInput, TableView<List<String>> resultsTable,
                              TextArea analysisOutput, ListView<String> recommendations, Label status, TabPane details) {
        try {
            QueryExecutionResult result = new ExecuteQueryCommand(queryService,
                    databasePath.getText().trim(), sqlInput.getText().trim()).execute();
            showResults(resultsTable, result);
            analysisOutput.setText(result.analysis().displayText());
            recommendations.getItems().setAll(result.recommendations().stream()
                    .map(item -> "[" + item.priority() + "] " + item.description()).toList());
            details.getSelectionModel().select(0);
            status.setText(result.status() + " in " + result.executionTimeMs() + " ms"
                    + (result.slow() ? " — marked as slow" : ""));
        } catch (IllegalArgumentException exception) {
            clearResults(resultsTable);
            status.setText(exception.getMessage());
        } catch (Exception exception) {
            clearResults(resultsTable);
            status.setText("Query failed: " + exception.getMessage());
        }
    }

    private void showResults(TableView<List<String>> table, QueryExecutionResult result) {
        clearResults(table);
        for (int index = 0; index < result.columnNames().size(); index++) {
            int columnIndex = index;
            TableColumn<List<String>, String> column = new TableColumn<>(result.columnNames().get(index));
            column.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(columnIndex)));
            column.setPrefWidth(160);
            table.getColumns().add(column);
        }
        table.getItems().addAll(result.rows());
    }

    private void clearResults(TableView<List<String>> table) {
        table.getItems().clear();
        table.getColumns().clear();
    }

    private VBox createHistoryView() {
        Label report = new Label();
        report.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        TableView<HistoryEntry> historyTable = new TableView<>();
        historyTable.getColumns().addAll(List.of(
                column("ID", entry -> String.valueOf(entry.id()), 70),
                column("SQL", HistoryEntry::sql, 360),
                column("Time (ms)", entry -> String.valueOf(entry.executionTimeMs()), 100),
                column("Status", HistoryEntry::status, 100),
                column("Review", HistoryEntry::reviewStatus, 100),
                column("Executed", HistoryEntry::executedAt, 170)
        ));
        TextField search = new TextField();
        search.setPromptText("Search SQL, status, or database path");
        Button searchButton = new Button("Search");
        searchButton.setOnAction(event -> historyTable.getItems().setAll(historyRepository.find(search.getText())));
        Button clearSearch = new Button("Clear");
        clearSearch.setOnAction(event -> {
            search.clear();
            refreshHistory(historyTable, report);
        });
        Button delete = new Button("Delete Selected");
        delete.setOnAction(event -> {
            HistoryEntry selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                historyRepository.delete(selected.id());
                refreshHistory(historyTable, report);
            }
        });
        Button reviewed = new Button("Mark Reviewed");
        reviewed.setOnAction(event -> {
            HistoryEntry selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                historyRepository.markReviewed(selected.id());
                refreshHistory(historyTable, report);
            }
        });
        Button refresh = new Button("Refresh History");
        refresh.setOnAction(event -> refreshHistory(historyTable, report));
        refreshHistory(historyTable, report);
        HBox actions = new HBox(10, search, searchButton, clearSearch, refresh, reviewed, delete);
        HBox.setHgrow(search, Priority.ALWAYS);
        VBox box = new VBox(12, new Label("Performance report"), report, actions, historyTable);
        box.setPadding(new Insets(18));
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        return box;
    }

    private VBox createConnectionsView() {
        Label title = new Label("Database Connections");
        TextField path = new TextField();
        path.setPromptText("For example: data/demo.db");
        Label message = new Label("Select a row to edit it, or enter a new path.");
        TableView<DatabaseConnectionEntry> table = new TableView<>();
        table.getColumns().addAll(List.of(
                column("ID", entry -> String.valueOf(entry.id()), 70),
                column("Database path", DatabaseConnectionEntry::databasePath, 500),
                column("Added", DatabaseConnectionEntry::createdAt, 180)
        ));
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) path.setText(selected.databasePath());
        });
        Button save = new Button("Save Connection");
        save.setOnAction(event -> {
            DatabaseConnectionEntry selected = table.getSelectionModel().getSelectedItem();
            try {
                connectionRepository.save(selected == null ? 0 : selected.id(), path.getText().trim());
                refreshConnections(table);
                table.getSelectionModel().clearSelection();
                path.clear();
                message.setText("Connection saved.");
            } catch (Exception exception) {
                message.setText(exception.getMessage());
            }
        });
        Button delete = new Button("Delete Selected");
        delete.setOnAction(event -> {
            DatabaseConnectionEntry selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                connectionRepository.delete(selected.id());
                refreshConnections(table);
                path.clear();
                message.setText("Connection deleted.");
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> refreshConnections(table));
        refreshConnections(table);
        HBox controls = new HBox(10, path, save, delete, refresh);
        HBox.setHgrow(path, Priority.ALWAYS);
        VBox box = new VBox(12, title, controls, message, table);
        box.setPadding(new Insets(18));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox createRecommendationsView() {
        Label title = new Label("Saved Recommendations");
        Label message = new Label("Recommendations are created automatically after query analysis.");
        TableView<RecommendationEntry> table = new TableView<>();
        table.getColumns().addAll(List.of(
                column("ID", entry -> String.valueOf(entry.id()), 65),
                column("Type", RecommendationEntry::type, 110),
                column("Recommendation", RecommendationEntry::description, 470),
                column("Priority", RecommendationEntry::priority, 95),
                column("Status", RecommendationEntry::status, 100)
        ));
        Button applied = new Button("Mark Applied");
        applied.setOnAction(event -> updateRecommendationStatus(table, "APPLIED", message));
        Button dismissed = new Button("Dismiss");
        dismissed.setOnAction(event -> updateRecommendationStatus(table, "DISMISSED", message));
        Button delete = new Button("Delete Selected");
        delete.setOnAction(event -> {
            RecommendationEntry selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                recommendationRepository.delete(selected.id());
                refreshRecommendations(table);
                message.setText("Recommendation deleted.");
            }
        });
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> refreshRecommendations(table));
        refreshRecommendations(table);
        HBox controls = new HBox(10, applied, dismissed, delete, refresh);
        VBox box = new VBox(12, title, message, controls, table);
        box.setPadding(new Insets(18));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox createIndexCatalogView() {
        Label title = new Label("Actual SQLite Index Catalog");
        TextField path = new TextField("data/demo.db");
        path.setPromptText("SQLite database path");
        Label message = new Label("Refresh to inspect indexes in the selected database.");
        TableView<IndexEntry> table = new TableView<>();
        table.getColumns().addAll(List.of(
                column("Table", IndexEntry::tableName, 230),
                column("Index", IndexEntry::indexName, 300),
                column("Column", IndexEntry::columnName, 220),
                column("Unique", entry -> entry.unique() ? "Yes" : "No", 100)
        ));
        Button refresh = new Button("Inspect Indexes");
        refresh.setOnAction(event -> {
            try {
                table.getItems().setAll(indexCatalogRepository.findAll(path.getText().trim()));
                message.setText(table.getItems().size() + " index column(s) found.");
            } catch (Exception exception) {
                table.getItems().clear();
                message.setText(exception.getMessage());
            }
        });
        HBox controls = new HBox(10, new Label("SQLite database:"), path, refresh);
        HBox.setHgrow(path, Priority.ALWAYS);
        VBox box = new VBox(12, title, controls, message, table);
        box.setPadding(new Insets(18));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private <T> TableColumn<T, String> column(String title, Function<T, String> value, int width) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        return column;
    }

    private void refreshHistory(TableView<HistoryEntry> historyTable, Label report) {
        historyTable.getItems().setAll(historyRepository.findRecent());
        report.setText(historyRepository.performanceSummary());
    }

    private void refreshConnections(TableView<DatabaseConnectionEntry> table) {
        table.getItems().setAll(connectionRepository.findAll());
    }

    private void refreshRecommendations(TableView<RecommendationEntry> table) {
        table.getItems().setAll(recommendationRepository.findAll());
    }

    private void updateRecommendationStatus(TableView<RecommendationEntry> table, String status, Label message) {
        RecommendationEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            recommendationRepository.updateStatus(selected.id(), status);
            refreshRecommendations(table);
            message.setText("Recommendation marked " + status.toLowerCase() + ".");
        }
    }
}
