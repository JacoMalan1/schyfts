package com.codelog.schyfts;

import com.codelog.schyfts.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Schyfts extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static Stage currentStage;

    @Override
    public void start(Stage primaryStage) throws IOException {

        Logger.getInstance().info("Building GUI...");
        var file = getClass().getClassLoader().getResource("login.fxml");
        if (file == null)
            return;

        Parent root = FXMLLoader.load(file);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        currentStage = primaryStage;
        Logger.getInstance().info("Done");

    }

    public static void changeScene(String resource) {
        URL file = Schyfts.class.getClassLoader().getResource(resource);
        if (file == null)
            return;

        try {
            Parent root = FXMLLoader.load(file);
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void changeScene(String resource, String title) {
        currentStage.setTitle(title);
        changeScene(resource);
    }

}
