package com.enss.sha256tool;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Sha256ToolApp extends Application {

    @Override
    public void start(Stage stage) {
        // ── Row 1: file path field + choose file button ──────────────────────
        TextField pathField = new TextField();
        pathField.setPromptText("请选择文件...");
        pathField.setEditable(false);
        HBox.setHgrow(pathField, Priority.ALWAYS);

        Button chooseBtn = new Button("选择文件");

        HBox fileRow = new HBox(8, pathField, chooseBtn);
        fileRow.setAlignment(Pos.CENTER_LEFT);

        // ── Row 2: result label (left) + calculate button (right) ────────────
        Label resultLabel = new Label("计算结果：");
        resultLabel.setVisible(false);
        resultLabel.setManaged(false);

        Button calcBtn = new Button("计算 SHA256");
        calcBtn.setDisable(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox calcRow = new HBox(8, resultLabel, spacer, calcBtn);
        calcRow.setAlignment(Pos.CENTER_LEFT);

        // ── Row 3: result text area (initially hidden) ────────────────────────
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefRowCount(3);
        resultArea.setWrapText(true);
        resultArea.setVisible(false);
        resultArea.setManaged(false);

        // ── Row 4: copy button (right-aligned, initially hidden) ──────────────
        Button copyBtn = new Button("复制结果");

        HBox copyRow = new HBox(copyBtn);
        copyRow.setAlignment(Pos.CENTER_RIGHT);
        copyRow.setVisible(false);
        copyRow.setManaged(false);

        // ── Loading overlay ───────────────────────────────────────────────────
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(60, 60);

        StackPane loadingOverlay = new StackPane(spinner);
        loadingOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        loadingOverlay.setVisible(false);

        final File[] selectedFile = {null};

        // Choose file button action
        chooseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("选择文件");
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                selectedFile[0] = file;
                pathField.setText(file.getAbsolutePath());
                calcBtn.setDisable(false);
                // Hide previous result when a new file is selected
                setResultVisible(resultLabel, resultArea, copyRow, false);
                stage.sizeToScene();
            }
        });

        // Calculate SHA256 button action
        calcBtn.setOnAction(e -> {
            loadingOverlay.setVisible(true);
            calcBtn.setDisable(true);
            chooseBtn.setDisable(true);

            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return Sha256Util.sha256Hex(selectedFile[0]);
                }
            };

            task.setOnSucceeded(ev -> {
                resultArea.setText(task.getValue());
                setResultVisible(resultLabel, resultArea, copyRow, true);
                loadingOverlay.setVisible(false);
                calcBtn.setDisable(false);
                chooseBtn.setDisable(false);
                stage.sizeToScene();
            });

            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                resultArea.setText("计算失败：" + (ex != null ? ex.getMessage() : "未知错误"));
                setResultVisible(resultLabel, resultArea, copyRow, true);
                loadingOverlay.setVisible(false);
                calcBtn.setDisable(false);
                chooseBtn.setDisable(false);
                stage.sizeToScene();
            });

            new Thread(task).start();
        });

        // Copy result button action
        copyBtn.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(resultArea.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });

        // Root layout
        VBox content = new VBox(12, fileRow, calcRow, resultArea, copyRow);
        content.setPadding(new Insets(20));

        StackPane root = new StackPane(content, loadingOverlay);
        stage.setTitle("SHA256Tool v1.0.0");
        // Width fixed at 480; height = Region.USE_COMPUTED_SIZE (-1) → computed from content
        stage.setScene(new Scene(root, 480, Region.USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.show();
    }

    /** Toggles visibility of the result section (label, text area, copy button row). */
    private void setResultVisible(Label label, TextArea area, HBox copyRow, boolean visible) {
        label.setVisible(visible);
        label.setManaged(visible);
        area.setVisible(visible);
        area.setManaged(visible);
        copyRow.setVisible(visible);
        copyRow.setManaged(visible);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
