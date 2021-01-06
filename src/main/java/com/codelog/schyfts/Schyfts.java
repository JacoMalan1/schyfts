package com.codelog.schyfts;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.net.URL;

public class Schyfts extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static Stage currentStage;
    private static JMetro jMetro;

    @Override
    public void start(Stage primaryStage) throws IOException {

        var file = getClass().getClassLoader().getResource("login.fxml");
        if (file == null)
            return;

        Parent root = FXMLLoader.load(file);

        Scene scene = new Scene(root);
        jMetro = new JMetro(Style.DARK);
        jMetro.setScene(scene);
        jMetro.setAutomaticallyColorPanes(true);

        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        currentStage = primaryStage;

    }

    public static void changeScene(String resource) {
        URL file = Schyfts.class.getClassLoader().getResource(resource);
        if (file == null)
            return;

        try {
            Parent root = FXMLLoader.load(file);
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            jMetro.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
