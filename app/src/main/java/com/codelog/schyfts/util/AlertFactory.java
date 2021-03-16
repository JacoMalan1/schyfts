package com.codelog.schyfts.util;

import javafx.scene.control.Alert;

public class AlertFactory {

    public static Alert createAlert(Alert.AlertType type, String message) {
        return new Alert(type, message);
    }

    public static void showAlert(Alert.AlertType type, String message) {
        Alert alert = createAlert(type, message);
        alert.show();
    }

    public static void showAndWait(Alert.AlertType type, String message) {
        Alert alert = createAlert(type, message);
        alert.showAndWait();
    }

    public static void showAndWait(String message) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    public static void showAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, message);
    }

}
