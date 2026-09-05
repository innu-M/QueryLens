package com.querylens;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.querylens.model.QueryExecutionResult;
import com.querylens.service.QueryExecutionService;

import java.util.List;

public class App extends Application {

    private final QueryExecutionService queryService = new QueryExecutionService();

    @Override
    public void init() {
        queryService.initialize();
    }

    @Override
    public void start(Stage stage) {
        Label title = new Label("QueryLens");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField databasePath = new TextField("data/demo.db");
        databasePath.setPromptText("SQLite database path, for example: data/demo.db");
        HBox.setHgrow(databasePath, Priority.ALWAYS);

        TextArea sqlInput = new TextArea("SELECT 1 AS sample_result;");
        sqlInput.setPromptText("Write a SQLite query here...");
        sqlInput.setPrefRowCount(8);

        Label status = new Label("Ready. Results are saved in QueryLens history.");
        Button executeButton = new Button("Execute Query");

        TableView<List<String>> resultsTable = new TableView<>();
        resultsTable.setPlaceholder(new Label("Run a SELECT query to see its result."));

        executeButton.setOnAction(event -> {
            try {
                QueryExecutionResult result = queryService.execute(
                        databasePath.getText().trim(),
                        sqlInput.getText().trim()
                );

                showResults(resultsTable, result);
                status.setText(result.status() + " in " + result.executionTimeMs() + " ms"
                        + (result.slow() ? " — marked as slow" : ""));
            } catch (IllegalArgumentException exception) {
                status.setText(exception.getMessage());
                resultsTable.getItems().clear();
                resultsTable.getColumns().clear();
            } catch (Exception exception) {
                status.setText("Query failed: " + exception.getMessage());
                resultsTable.getItems().clear();
                resultsTable.getColumns().clear();
            }
        });

        HBox connectionBar = new HBox(10, new Label("SQLite database:"), databasePath);
        VBox top = new VBox(10, title, connectionBar, new Label("SQL query:"), sqlInput, executeButton, status);
        top.setStyle("-fx-padding: 18;");

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(resultsTable);
        BorderPane.setMargin(resultsTable, new javafx.geometry.Insets(0, 18, 18, 18));

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("QueryLens");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showResults(TableView<List<String>> table, QueryExecutionResult result) {
        table.getItems().clear();
        table.getColumns().clear();

        for (int index = 0; index < result.columnNames().size(); index++) {
            int columnIndex = index;
            TableColumn<List<String>, String> column = new TableColumn<>(result.columnNames().get(index));
            column.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get(columnIndex)));
            column.setPrefWidth(160);
            table.getColumns().add(column);
        }
        table.getItems().addAll(result.rows());
    }
}
