package com.converter;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class ConverterApp extends Application {
    
    private TextArea inputArea;
    private TextArea outputArea;
    private Label statusLabel;
    private FileConverter localConverter;
    private ApiConverter apiConverter;
    private ToggleGroup modeGroup;
    private RadioButton localModeRadio;
    private RadioButton apiModeRadio;
    
    @Override
    public void start(Stage primaryStage) {
        localConverter = new FileConverter();
        apiConverter = new ApiConverter();
        
        primaryStage.setTitle("JSON ⇄ XML Converter");
        
        // Create UI components
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        
        // Top section - Title and buttons
        VBox topSection = createTopSection(primaryStage);
        root.setTop(topSection);
        
        // Center section - Text areas
        HBox centerSection = createCenterSection();
        root.setCenter(centerSection);
        
        // Bottom section - Status
        HBox bottomSection = createBottomSection();
        root.setBottom(bottomSection);
        
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createTopSection(Stage stage) {
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(0, 0, 15, 0));
        
        Label titleLabel = new Label("JSON ⇄ XML Converter");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Mode Selection
        HBox modeBox = new HBox(15);
        modeBox.setAlignment(Pos.CENTER);
        modeBox.setPadding(new Insets(10, 0, 10, 0));
        modeBox.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
        
        Label modeLabel = new Label("Conversion Mode:");
        modeLabel.setStyle("-fx-font-weight: bold;");
        
        modeGroup = new ToggleGroup();
        
        localModeRadio = new RadioButton("Local (Offline)");
        localModeRadio.setToggleGroup(modeGroup);
        localModeRadio.setSelected(true);
        localModeRadio.setStyle("-fx-font-size: 12px;");
        
        apiModeRadio = new RadioButton("API (Online)");
        apiModeRadio.setToggleGroup(modeGroup);
        apiModeRadio.setStyle("-fx-font-size: 12px;");
        
        // Add tooltip for clarification
        localModeRadio.setTooltip(new Tooltip("Uses local Java libraries (no internet required)"));
        apiModeRadio.setTooltip(new Tooltip("Uses external REST API (requires internet connection)"));
        
        modeBox.getChildren().addAll(modeLabel, localModeRadio, apiModeRadio);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loadFileBtn = new Button("Load File");
        loadFileBtn.setPrefWidth(120);
        loadFileBtn.setOnAction(e -> loadFile(stage));
        
        Button jsonToXmlBtn = new Button("JSON → XML");
        jsonToXmlBtn.setPrefWidth(120);
        jsonToXmlBtn.setOnAction(e -> convertJsonToXml());
        
        Button xmlToJsonBtn = new Button("XML → JSON");
        xmlToJsonBtn.setPrefWidth(120);
        xmlToJsonBtn.setOnAction(e -> convertXmlToJson());
        
        Button saveBtn = new Button("Save Output");
        saveBtn.setPrefWidth(120);
        saveBtn.setOnAction(e -> saveFile(stage));
        
        Button clearBtn = new Button("Clear All");
        clearBtn.setPrefWidth(120);
        clearBtn.setOnAction(e -> clearAll());
        
        buttonBox.getChildren().addAll(loadFileBtn, jsonToXmlBtn, xmlToJsonBtn, saveBtn, clearBtn);
        
        topBox.getChildren().addAll(titleLabel, modeBox, buttonBox);
        return topBox;
    }
    
    private HBox createCenterSection() {
        HBox centerBox = new HBox(15);
        centerBox.setPrefHeight(400);
        
        VBox inputBox = new VBox(5);
        Label inputLabel = new Label("Input:");
        inputLabel.setStyle("-fx-font-weight: bold;");
        inputArea = new TextArea();
        inputArea.setPromptText("Paste your JSON or XML here, or load from file...");
        inputArea.setWrapText(true);
        VBox.setVgrow(inputArea, Priority.ALWAYS);
        inputBox.getChildren().addAll(inputLabel, inputArea);
        
        VBox outputBox = new VBox(5);
        Label outputLabel = new Label("Output:");
        outputLabel.setStyle("-fx-font-weight: bold;");
        outputArea = new TextArea();
        outputArea.setPromptText("Converted output will appear here...");
        outputArea.setWrapText(true);
        outputArea.setEditable(false);
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        outputBox.getChildren().addAll(outputLabel, outputArea);
        
        HBox.setHgrow(inputBox, Priority.ALWAYS);
        HBox.setHgrow(outputBox, Priority.ALWAYS);
        
        centerBox.getChildren().addAll(inputBox, outputBox);
        return centerBox;
    }
    
    private HBox createBottomSection() {
        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #666666;");
        
        bottomBox.getChildren().add(statusLabel);
        return bottomBox;
    }
    
    private void loadFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                String content = localConverter.readFile(file.getAbsolutePath());
                inputArea.setText(content);
                statusLabel.setText("Loaded: " + file.getName());
                statusLabel.setStyle("-fx-text-fill: green;");
            } catch (Exception e) {
                showError("Failed to load file: " + e.getMessage());
            }
        }
    }
    
    private void convertJsonToXml() {
        String input = inputArea.getText().trim();
        if (input.isEmpty()) {
            showError("Please provide JSON input");
            return;
        }
        
        try {
            String xml;
            boolean useApi = apiModeRadio.isSelected();
            
            if (useApi) {
                statusLabel.setText("Converting using API... (requires internet)");
                statusLabel.setStyle("-fx-text-fill: blue;");
                xml = apiConverter.jsonToXml(input);
                statusLabel.setText("Successfully converted JSON to XML using API");
            } else {
                statusLabel.setText("Converting locally...");
                statusLabel.setStyle("-fx-text-fill: blue;");
                xml = localConverter.jsonToXml(input);
                statusLabel.setText("Successfully converted JSON to XML (Local)");
            }
            
            outputArea.setText(xml);
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            showError("Conversion failed: " + e.getMessage());
        }
    }
    
    private void convertXmlToJson() {
        String input = inputArea.getText().trim();
        if (input.isEmpty()) {
            showError("Please provide XML input");
            return;
        }
        
        try {
            String json;
            boolean useApi = apiModeRadio.isSelected();
            
            if (useApi) {
                statusLabel.setText("Converting using API... (requires internet)");
                statusLabel.setStyle("-fx-text-fill: blue;");
                json = apiConverter.xmlToJson(input);
                statusLabel.setText("Successfully converted XML to JSON using API");
            } else {
                statusLabel.setText("Converting locally...");
                statusLabel.setStyle("-fx-text-fill: blue;");
                json = localConverter.xmlToJson(input);
                statusLabel.setText("Successfully converted XML to JSON (Local)");
            }
            
            outputArea.setText(json);
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            showError("Conversion failed: " + e.getMessage());
        }
    }
    
    private void saveFile(Stage stage) {
        String content = outputArea.getText().trim();
        if (content.isEmpty()) {
            showError("No output to save");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                localConverter.writeFile(file.getAbsolutePath(), content);
                statusLabel.setText("Saved: " + file.getName());
                statusLabel.setStyle("-fx-text-fill: green;");
            } catch (Exception e) {
                showError("Failed to save file: " + e.getMessage());
            }
        }
    }
    
    private void clearAll() {
        inputArea.clear();
        outputArea.clear();
        statusLabel.setText("Ready");
        statusLabel.setStyle("-fx-text-fill: #666666;");
    }
    
    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        statusLabel.setStyle("-fx-text-fill: red;");
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
