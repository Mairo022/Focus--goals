package com.app.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.app.util.Print.print;

public class TimerDialogueController extends Dialog {
    private final DialogPane dialogPane = this.getDialogPane();
    private HashMap<String, String> planData;

    @FXML
    public TextField inputTitle;
    @FXML
    public TextField inputShortBreak;
    @FXML
    public TextField inputLongBreak;
    @FXML
    public TextField inputFocusTime;
    @FXML
    public TextField inputCycles;

    @SafeVarargs
    public TimerDialogueController(HashMap<String, String>... planData) {
        if (planData.length != 0) this.planData = planData[0];

        loadUI();

        this.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                HashMap<String, String> data = new HashMap<>();
                data.put("title", inputTitle.getText());
                data.put("short_break", inputShortBreak.getText());
                data.put("long_break", inputLongBreak.getText());
                data.put("focus_time", inputFocusTime.getText());
                data.put("cycles", inputCycles.getText());

                return data;
            }
            return null;
        });
    }

    private void loadUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/app/timerDialogue.fxml"));
            fxmlLoader.setController(this);

            this.setTitle("Plan config");

            DialogPane dialogPaneFxml = fxmlLoader.load();
            dialogPane.setContent(dialogPaneFxml);
            dialogPane.getButtonTypes().add(ButtonType.CANCEL);
            dialogPane.getButtonTypes().add(ButtonType.OK);

            if (planData != null) {
                inputTitle.setText(planData.get("title"));
                inputFocusTime.setText(planData.get("focus_time"));
                inputShortBreak.setText(planData.get("short_break"));
                inputLongBreak.setText(planData.get("long_break"));
                inputCycles.setText(planData.get("cycles"));
            }

            Platform.runLater(() -> {
                Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
                Parent buttonsParent = okButton.getParent();

                buttonsParent.setStyle("-fx-background-color: #272e32");
                okButton.setStyle("-fx-cursor: hand");
                cancelButton.setStyle("-fx-cursor: hand");
            });

            handleOkButton();
        } catch (IOException e) {
            print(e);
        }
    }

    private boolean isInputValid(TextField textField) {
        if (!textField.getText().isBlank() || textField.equals(inputTitle))
            return true;

        textField.requestFocus();
        return false;
    }

    private void handleOkButton() {
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);

        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            List<TextField> textFieldList = Arrays.asList(inputFocusTime, inputTitle, inputLongBreak, inputShortBreak);
            boolean isInputDataValid = textFieldList.stream().allMatch(this::isInputValid);

            if (!isInputDataValid) event.consume();
        });
    }

}
