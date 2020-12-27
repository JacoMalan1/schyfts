package com.codelog.schyfts;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Schyfts extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        var file = getClass().getClassLoader().getResource("login.fxml");
        if (file == null)
            return;

        Parent root = FXMLLoader.load(file);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("styles.css").toExternalForm());

        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

    }
}
