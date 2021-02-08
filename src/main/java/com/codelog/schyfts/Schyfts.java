package com.codelog.schyfts;

import com.codelog.schyfts.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
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
            Logger.getInstance().exception(e);
        }
    }

    public static Stage createStage(String resource, String title) {

        var file = Schyfts.class.getClassLoader().getResource(resource);
        if (file == null) {
            Logger.getInstance().error(String.format("Couldn't load resource (%s)", resource));
            return null;
        }

        Parent root = null;
        try {
            root = FXMLLoader.load(file);
        } catch(IOException e) {
            Logger.getInstance().exception(e);
        }

        if (root == null) {
            Logger.getInstance().error(String.format("Couldn't load stage (%s)", resource));
            return null;
        }

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initOwner(currentStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();

        return stage;

    }

    public static void changeScene(String resource, String title) {
        currentStage.setTitle(title);
        changeScene(resource);
    }

}
