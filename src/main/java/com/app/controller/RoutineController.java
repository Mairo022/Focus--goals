package com.app.controller;

import com.app.util.Action;
import com.app.util.ConfigManager;
import com.app.model.RoutineModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.*;

public class RoutineController {
    private ConfigManager configManager = ConfigManager.getInstance();
    private RoutineModel routineModel = configManager.routines;

    private String activePlan = "";

    ToggleGroup toggleGroupPlans = new ToggleGroup();
    @FXML
    private FlowPane plans;
    @FXML
    private VBox routinesList;
    @FXML
    private Button routinesAdd;

    @FXML
    public void initialize() {
        toggleGroupPlans.setUserData("plans");
        addEventToAddRoutine();
        handleRoutinesData();
    }

    private void handleRoutinesData() {
        String active = routineModel.getActive();
        HashMap<String, String[]> plans = routineModel.getPlans();

        activePlan = plans.containsKey(active) ? active : (plans.size() == 0 ? "" : plans.keySet().iterator().next());

        displayPlans(plans);
        displayRoutines(plans);
    }


    private void displayPlans(HashMap<String, String[]> plans) {
        plans.keySet().forEach(key -> {
            createPlan(key, toggleGroupPlans);
        });
        addNewPlanButton();
    }

    private void displayRoutines(HashMap<String, String[]> plans) {
        if (!plans.containsKey(activePlan)) return;
        if (plans.get(activePlan).length == 1) return;

        for (String activity: plans.get(activePlan)) {
            createRoutine(activity);
        }
    }

    private void createPlan(String plan, ToggleGroup toggleGroup) {
        ToggleButton togglePlan = new ToggleButton(plan);
        togglePlan.setToggleGroup(toggleGroup);

        togglePlan.getStyleClass().add("plans__plan");
        plans.getChildren().add(togglePlan);

        if (plan.equals(activePlan)) {
            togglePlan.setSelected(true);
        }

        togglePlan.setOnMouseClicked(MouseEvent -> {
            if (MouseEvent.getButton() != MouseButton.PRIMARY) return;

            if (MouseEvent.getClickCount() == 1)
                handlePlanSingleClick(togglePlan, plan);

            if (MouseEvent.getClickCount() == 2)
                handlePlanDoubleClick(togglePlan);
        });
    }

    private void handlePlanSingleClick(ToggleButton togglePlan, String plan) {
        if (!plan.equals(activePlan)) loadPlan(togglePlan.getText());

        togglePlan.setSelected(true);
    }

    private void handlePlanDoubleClick(ToggleButton togglePlan) {
        int planIndex = plans.getChildren().indexOf(togglePlan);
        double planWidth = togglePlan.getWidth();
        double inputWidth = planWidth >= 80 ? planWidth : 80;
        TextField inputField = new TextField();

        plans.getChildren().remove(planIndex);
        plans.getChildren().add(planIndex, inputField);

        inputField.getStyleClass().add("plans__input");
        inputField.setPrefWidth(inputWidth);
        inputField.requestFocus();

        inputField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                plans.getChildren().remove(planIndex);
                plans.getChildren().add(planIndex, togglePlan);
                selectActivePlanButton();
                return;
            }

            if (keyEvent.getCode() == KeyCode.ENTER) {
                String planCurrent = togglePlan.getText();
                String planNew = inputField.getText();

                if (planNew.isBlank()) {
                    plans.getChildren().remove(planIndex);
                    removePlan(planCurrent);
                    return;
                }

                plans.getChildren().remove(planIndex);
                plans.getChildren().add(planIndex, togglePlan);

                editPlanName(togglePlan, planNew, planCurrent);
                selectActivePlanButton();
            }
        });
    }

    private void editPlanName(ToggleButton togglePlan, String planNew, String planCurrent) {
        configManager.routines.setActive(planNew);
        configManager.routines.editPlans(Action.RENAME, planCurrent, planNew);
        configManager.saveConfig();

        togglePlan.setText(planNew);
        activePlan = planNew;
    }

    private void removePlan(String planCurrent) {
        routinesList.getChildren().clear();

        configManager.routines.editPlans(Action.REMOVE, planCurrent);

        HashMap<String, String[]> planData = configManager.routines.getPlans();

        if (!planData.isEmpty()) {
            activePlan = planData.keySet().iterator().next();

            loadPlan(activePlan);
            selectActivePlanButton();
            configManager.routines.setActive(activePlan);
        }
        configManager.saveConfig();
    }

    private void createRoutine(String activity) {
        if (activity.isEmpty()) return;

        String classname = "routines__list__routine";

        HBox lineBox = new HBox();
        HBox activityBox = new HBox();
        HBox bulletPointBox = new HBox();
        Text bulletPointText = new Text("•");
        Text activityText = new Text(activity);
        Button deleteButton = new Button();
        Text deleteText = new Text("X");

        lineBox.getStyleClass().add(classname);

        bulletPointBox.getStyleClass().add(classname + "__bulletPoint");
        bulletPointText.getStyleClass().add(classname + "__bulletPoint__text");

        activityBox.getStyleClass().add(classname + "__activity");
        activityText.getStyleClass().add(classname + "__activity__text");

        deleteButton.getStyleClass().add(classname + "__delete");
        deleteText.getStyleClass().add(classname + "__delete__text");

        bulletPointBox.getChildren().add(bulletPointText);
        activityBox.getChildren().add(activityText);
        deleteButton.setGraphic(deleteText);
        lineBox.getChildren().addAll(bulletPointBox, activityBox, deleteButton);

        deleteButton.setVisible(false);

        routinesList.getChildren().add(lineBox);

        activityText.setOnMouseClicked(event -> {
            activityText.setStrikethrough(!activityText.isStrikethrough());
        });

        lineBox.setOnMouseEntered(event -> {
            deleteButton.setVisible(true);
        });
        lineBox.setOnMouseExited(event -> {
             deleteButton.setVisible(false);
        });

        deleteButton.setOnMouseClicked(event -> {
            routinesList.getChildren().remove(lineBox);

            configManager.routines.editPlan(Action.REMOVE, activePlan, activity);
            configManager.saveConfig();
        });
    }

    private void addNewPlanButton() {
        Button newPlanButton = new Button("+");

        newPlanButton.getStyleClass().add("plans__add");

        plans.getChildren().add(newPlanButton);

        newPlanButton.setOnMouseClicked(MouseEvent -> {
            createNewPlanInput(newPlanButton);
        });
    }

    private void createNewPlanInput(Button newPlanButton) {
        TextField inputField = new TextField();

        inputField.getStyleClass().add("plans__input");

        plans.getChildren().remove(newPlanButton);
        plans.getChildren().add(inputField);

        inputField.requestFocus();

        inputField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                plans.getChildren().remove(inputField);
                plans.getChildren().add(newPlanButton);
            }

            if (keyEvent.getCode() == KeyCode.ENTER) {
                String plan = inputField.getText();

                activePlan = plan;
                createPlan(plan, toggleGroupPlans);

                plans.getChildren().remove(inputField);
                plans.getChildren().add(newPlanButton);

                configManager.routines.editPlans(Action.ADD, plan);

                loadPlan(plan);
            }
        });
    }

    private void loadPlan(String plan) {
        activePlan = plan;

        configManager.routines.setActive(plan);
        configManager.saveConfig();

        routinesList.getChildren().clear();
        displayRoutines(routineModel.getPlans());
    }

    private void selectActivePlanButton() {
        plans.getChildren().forEach(node -> {
            if (node instanceof ToggleButton planButton)
                if (planButton.getText().equals(activePlan)) planButton.setSelected(true);
        });
    }

    private void addEventToAddRoutine() {
        routinesAdd.setOnMouseClicked(event -> {
            HBox lineBox = new HBox();
            HBox bulletPointBox = new HBox();
            Text bulletPointText = new Text("•");
            HBox inputBox = new HBox();
            TextField inputField = new TextField();

            lineBox.getStyleClass().add("routines__list__routine");
            bulletPointBox.getStyleClass().add("routines__list__routine__bulletPoint");
            bulletPointText.getStyleClass().add("routines__list__routine__bulletPoint__text");
            inputBox.getStyleClass().add("routines__list__routine__input");
            inputField.getStyleClass().add("routines__list__routine__input__field");

            HBox.setHgrow(inputField, Priority.ALWAYS);
            HBox.setHgrow(inputBox, Priority.ALWAYS);
            bulletPointBox.getChildren().add(bulletPointText);
            lineBox.getChildren().addAll(bulletPointBox, inputBox);
            inputBox.getChildren().add(inputField);

            routinesList.getChildren().add(lineBox);

            inputField.setPromptText("New Activity");
            inputField.requestFocus();

            inputField.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    routinesList.getChildren().remove(lineBox);
                }

                if (keyEvent.getCode() == KeyCode.ENTER) {
                    String activity = inputField.getText();

                    routinesList.getChildren().remove(lineBox);
                    createRoutine(activity);

                    configManager.routines.editPlan(Action.ADD, activePlan, activity);
                    configManager.saveConfig();
                }
            });
        });
    }
}
