package com.app.controller;

import com.app.util.Action;
import com.app.util.ConfigManager;
import com.app.model.TasksModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class TasksController {
    private ConfigManager configManager = ConfigManager.getInstance();
    private TasksModel tasksModel = configManager.tasksModel;

    @FXML
    private VBox tasksList;

    @FXML
    public void initialize() {
        handleTimerData();
    }

    private void handleTimerData() {
        String[] tasksList = tasksModel.getTasks();

        displayTasks(tasksList);
    }

    private void displayTasks(String[] tasksList) {
        for (String task: tasksList) {
            createTask(task);
        }
    }

    @FXML
    private void addNewTask() {
        HBox lineBox = new HBox();
        TextField inputField = new TextField();

        lineBox.getChildren().add(inputField);
        tasksList.getChildren().add(lineBox);

        HBox.setHgrow(inputField, Priority.ALWAYS);
        lineBox.getStyleClass().add("tasks__list__task");
        inputField.getStyleClass().add("tasks__list__task__input");
        inputField.requestFocus();

        inputField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                tasksList.getChildren().remove(lineBox);
            }

            if (keyEvent.getCode() == KeyCode.ENTER) {
                String task = inputField.getText();

                tasksList.getChildren().remove(lineBox);
                createTask(task);

                configManager.tasksModel.editTasks(Action.ADD, task);
                configManager.saveConfig();
            }
        });
    }

    private void createTask(String task) {
        String classname = "tasks__list__task";

        HBox lineBox = new HBox();
        Text taskText = new Text(task);
        Button deleteButton = new Button();
        Text deleteText = new Text("X");
        Region spacing = new Region();

        lineBox.getStyleClass().add(classname);
        taskText.getStyleClass().add(classname + "__name");
        deleteButton.getStyleClass().add(classname + "__delete");
        deleteText.getStyleClass().add(classname + "__delete__text");

        HBox.setHgrow(spacing, Priority.ALWAYS);
        taskText.setWrappingWidth(400);
        deleteButton.setGraphic(deleteText);

        deleteButton.setVisible(false);

        lineBox.getChildren().addAll(taskText, spacing, deleteButton);
        tasksList.getChildren().add(lineBox);

        lineBox.setOnMouseEntered(event -> {
            deleteButton.setVisible(true);
        });
        lineBox.setOnMouseExited(event -> {
            deleteButton.setVisible(false);
        });
        deleteButton.setOnMouseClicked(event -> {
            tasksList.getChildren().remove(lineBox);

            configManager.tasksModel.editTasks(Action.REMOVE, task);
            configManager.saveConfig();
        });
    }
}
