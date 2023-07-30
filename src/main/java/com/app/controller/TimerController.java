package com.app.controller;

import com.app.util.Action;
import com.app.util.ConfigManager;
import com.app.model.TimerModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import org.controlsfx.control.Notifications;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static com.app.util.Print.print;

public class TimerController {
    private final ConfigManager configManager = ConfigManager.getInstance();
    private final TimerModel configTimerModel = configManager.timerModel;

    private final ToggleGroup toggleGroupPlans = new ToggleGroup();
    private String activePlan = "";

    private final Timeline timer = countdownTimer();
    private boolean isRunning = false;
    private int timeRemaining = 0;
    private int stage = 0;
    private int cycle = 1;
    private int cycleLongBreak = 3;

    private int doubleClickDelay = 300;
    private Timer clickTimer;

    private String playSVG = "M12 24a12 12 0 1 1 12-12 12.013 12.013 0 0 1-12 12zm0-22a10 10 0 1 0 10 10A10.011 10.011 0 0 0 12 2z M9 16.766V7.234L16.944 12zm2-6v2.468L13.056 12z";
    private String pauseSVG = "M13 28H7a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v22a1 1 0 0 1-1 1zm-5-2h4V6H8v20zM25 28h-6a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v22a1 1 0 0 1-1 1zm-5-2h4V6h-4v20z";

    @FXML
    private Text timerTime;
    @FXML
    private ToggleButton stageShort;
    @FXML
    private ToggleButton stageLong;
    @FXML
    private ToggleButton stageFocus;
    @FXML
    private SVGPath playPauseSVG;
    @FXML
    private FlowPane plans;

    @FXML
    public void initialize() {
        toggleGroupPlans.setUserData("plans");
        handleTimerData();
    }

    private void handleTimerData() {
        String active = configTimerModel.getActive();
        HashMap<String, String[]> plans = configTimerModel.getPlans();

        activePlan = plans.containsKey(active) ? active : (plans.size() == 0 ? "" : plans.keySet().iterator().next());

        addEventsToStageButtons();
        loadPlanStage(activePlan, stage);
        displayPlans(plans);
    }

    private void displayPlans(HashMap<String, String[]> plans) {
        plans.keySet().forEach(key -> {
            createPlan(key, toggleGroupPlans);
        });
        addNewPlanButton();
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
                handlePlanSingleClick(togglePlan);

            if (MouseEvent.getClickCount() == 2) {
                handlePlanDoubleClick(togglePlan);
            }
        });
    }

    private void handlePlanSingleClick(ToggleButton togglePlan) {
        if (!togglePlan.getText().equals(activePlan)) {
            activePlan = togglePlan.getText();
            stage = 0;

            loadPlanStage(activePlan, stage);
            playPauseSVG.setContent(playSVG);
            timer.stop();

            configManager.timerModel.setActive(activePlan);
            configManager.saveConfig();
        };
        togglePlan.setSelected(true);
    }

    private void handlePlanDoubleClick(ToggleButton togglePlan) {
        TimerDialogueController dialogue = new TimerDialogueController(planToHashMap(togglePlan.getText()));

        Optional<HashMap<String, String>> result = dialogue.showAndWait();
        result.ifPresent(data -> {
            if (data.get("title").isBlank()) {
                removePlan(togglePlan);
                return;
            }
            activePlan = data.get("title");

            configTimerModel.editPlan(togglePlan.getText(), data);
            configTimerModel.setActive(activePlan);
            configManager.saveConfig();

            togglePlan.setText(activePlan);
            resetTimer();
            loadPlanStage(activePlan, stage);

            togglePlan.setSelected(true);
        });

        if (!result.isPresent()) togglePlan.setSelected(true);
    }

    private void removePlan(ToggleButton togglePlan) {
        HashMap<String, String[]> planData = configTimerModel.getPlans();

        configTimerModel.editPlans(Action.REMOVE, togglePlan.getText());

        plans.getChildren().remove(togglePlan);

        if (!planData.isEmpty()) {
            activePlan = planData.keySet().iterator().next();

            resetTimer();
            loadPlanStage(activePlan, stage);
            selectActivePlanButton(activePlan);

            configManager.routines.setActive(activePlan);
        }
        configManager.saveConfig();
    }

    private void createNewPlanFromDialogData(HashMap<String, String> data, Button newPlanButton) {
        activePlan = data.get("title");
        configTimerModel.setActive(activePlan);
        configTimerModel.editPlans(Action.ADD, activePlan, data);
        configManager.saveConfig();

        createPlan(activePlan, toggleGroupPlans);
        resetTimer();
        loadPlanStage(activePlan, stage);

        plans.getChildren().remove(newPlanButton);
        plans.getChildren().add(newPlanButton);
    }

    private void selectActivePlanButton(String activePlan) {
        plans.getChildren().forEach(node -> {
            if (node instanceof ToggleButton planButton)
                if (planButton.getText().equals(activePlan)) planButton.setSelected(true);
        });
    }

    private HashMap<String, String> planToHashMap(String plan) {
        String[] planDataArray = configTimerModel.getPlans().get(plan);

        HashMap<String, String> planData = new HashMap<>();
        planData.put("title", plan);
        planData.put("focus_time", planDataArray[0]);
        planData.put("short_break", planDataArray[1]);
        planData.put("long_break", planDataArray[2]);
        planData.put("cycles", planDataArray[3]);

        return planData;
    }

    private void addNewPlanButton() {
        Button newPlanButton = new Button("+");
        newPlanButton.getStyleClass().add("plans__add");

        plans.getChildren().add(newPlanButton);

        newPlanButton.setOnMouseClicked(MouseEvent -> {
            TimerDialogueController dialogue = new TimerDialogueController();

            Optional<HashMap<String, String>> result = dialogue.showAndWait();
            result.ifPresent(data -> {
                if (data.get("title").isBlank()) return;

                createNewPlanFromDialogData(data, newPlanButton);
            });
        });
    }

    private void resetTimer() {
        isRunning = false;
        stage = 0;
        cycle = 0;
        timer.stop();
        playPauseSVG.setContent(playSVG);
    }

    private Timeline countdownTimer() {
        Timeline timer = new Timeline(new KeyFrame(
                javafx.util.Duration.seconds(1),
                event -> {
                    timeRemaining -= 1;

                    setTimerTime(timeRemaining);

                    if (timeRemaining == 0) {
                        nextStage();
                    }
                }
        ));
        timer.setCycleCount(-1);

        return timer;
    }

    private void addEventsToStageButtons() {
        stageShort.setOnAction(event -> {
            stage = 1;
            loadPlanStage(activePlan, stage);
        });
        stageFocus.setOnAction(event -> {
            stage = 0;
            loadPlanStage(activePlan, stage);
        });
        stageLong.setOnAction(event -> {
            stage = 2;
            loadPlanStage(activePlan, stage);
        });
    }

    private void setStageButtonSelected(int stage) {
        stageShort.setSelected(false);
        stageFocus.setSelected(false);
        stageLong.setSelected(false);

        if (stage == 1) stageShort.setSelected(true);
        if (stage == 0) stageFocus.setSelected(true);
        if (stage == 2) stageLong.setSelected(true);
    }

    @FXML
    private void changeState() {
        isRunning = !isRunning;

        if (isRunning) {
            playPauseSVG.setContent(pauseSVG);
            timer.play();
        } else {
            playPauseSVG.setContent(playSVG);
            timer.stop();
        }
    }

    @FXML
    private void nextStage() {
        if (stage == 1 || stage == 2) cycle++;
        if (cycle < cycleLongBreak) stage = stage == 0 ? 1 : 0;
        if (cycle >= cycleLongBreak) stage = stage == 0 ? 2 : 0;

        handleNotifications(stage);
        loadPlanStage(activePlan, stage);
    }

    @FXML
    private void prevStage() {
        if (stage == 0 && cycle != 0) cycle--;
        if (cycle < cycleLongBreak) stage = stage == 0 ? 1 : 0;
        if (cycle >= cycleLongBreak) stage = stage == 0 ? 2 : 0;

        handleNotifications(stage);
        loadPlanStage(activePlan, stage);
    }

    private void loadPlanStage(String plan, int stage) {
        timeRemaining = getPlanTime(plan, stage);

        setTimerTime(timeRemaining);
        setStageButtonSelected(stage);
    }

    private void setTimerTime(int time) {
        timerTime.setText(timeToString(time));
    }

    private int getPlanTime(String active, int stage) {
        return Integer.parseInt(configTimerModel.getPlans().get(active)[stage]);
    }

    private String timeToString(int time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        Duration duration = Duration.ofSeconds(time);
        LocalTime localTime = LocalTime.ofSecondOfDay(duration.toSeconds());
        String formattedTime = localTime.format(formatter);

        return formattedTime;
    }

    private void handleNotifications(int stage) {
        if (stage == 0) createNotification("Focus");
        if (stage == 1) createNotification("Short Break");
        if (stage == 2) createNotification("Long Break");
    }

    private void createNotification(String text) {
        Notifications.create()
                .text(text)
                .show();
    }
}