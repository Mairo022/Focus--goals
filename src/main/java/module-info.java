module com.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens com.app to javafx.fxml;
    exports com.app;
    exports com.app.controller;
    opens com.app.controller to javafx.fxml;
}