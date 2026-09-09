package com.querylens;

import com.querylens.persistence.DatabaseInitializer;
import com.querylens.ui.BenchmarkControlsView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Path databasePath = Path.of("data", "querylens.db");
        new DatabaseInitializer().initialize(databasePath);

        Label title = new Label("QueryLens");
        title.getStyleClass().add("title");
        Label message = new Label("Your local query analysis workspace is ready.");
        Label database = new Label("Workspace database: " + databasePath.toAbsolutePath());

        Label benchmarkHeading = new Label("Benchmark controls");
        benchmarkHeading.getStyleClass().add("title");
        VBox root = new VBox(12, title, message, database, benchmarkHeading, new BenchmarkControlsView());
        root.setPadding(new Insets(28));
        Scene scene = new Scene(root, 620, 500);
        stage.setTitle("QueryLens");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
