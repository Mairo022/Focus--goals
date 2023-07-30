package com.app.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AppController {
    private double xOffset = 0;
    private double yOffset = 0;
    private VBox currentPage;

    @FXML
    private VBox pageRoutines;
    @FXML
    private VBox pageTasks;
    @FXML
    private VBox pageTimer;
    @FXML
    private VBox pageGoals;
    @FXML
    private GridPane navbar;

    @FXML
    public void initialize() {
        currentPage = pageRoutines;
        openPage(pageRoutines);
        keepPageButtonSelectedOnClick();
    }

    @FXML
    private void openPage(VBox page) {
        page.setVisible(true);
        page.setManaged(true);

        if (page != currentPage){
            currentPage.setVisible(false);
            currentPage.setManaged(false);

            currentPage = page;
        }
    }

    public void onRoutine() {
        openPage(pageRoutines);
    }

    public void onTasks() {
        openPage(pageTasks);
    }

    public void onTimer() {
        openPage(pageTimer);
    }

    public void onGoals() {
        openPage(pageGoals);
    }

    private void keepPageButtonSelectedOnClick() {
        navbar.getChildren().forEach(node -> {
            node.setOnMouseClicked(mouseEvent -> {
                if (node instanceof ToggleButton togglePage)
                    togglePage.setSelected(true);
            });
        });
    }
}