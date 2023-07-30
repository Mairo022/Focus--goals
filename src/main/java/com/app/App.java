package com.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
            Scene scene = new Scene(root, 510, 650);

            stage.setTitle("Focus");
            stage.setScene(scene);
            stage.setMinWidth(510);
            stage.setMinHeight(650);
            stage.setMaxWidth(510);
            stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
