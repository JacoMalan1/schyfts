package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.util.StageCreateCallback;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class Schyfts extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage currentStage;

    @Override
    public void start(Stage primaryStage) throws IOException {

        Logger.getInstance().info("Building GUI...");
        var file = getClass().getClassLoader().getResource("FXML/login.fxml");
        if (file == null)
            return;

        Parent root = FXMLLoader.load(file);
        root.getStylesheets().add("styles.css");

        Scene scene = new Scene(root);

        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(Reference.ICON);
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
            var x = Screen.getPrimary().getBounds().getMaxX() - Screen.getPrimary().getBounds().getMinX();
            x /= 2;
            x += Screen.getPrimary().getBounds().getMinX();
            var y = Screen.getPrimary().getBounds().getMaxY() - Screen.getPrimary().getBounds().getMinY();
            y /= 2;
            y += Screen.getPrimary().getBounds().getMinY();
            currentStage.setX(x - scene.getWidth() / 2);
            currentStage.setY(y - scene.getHeight() / 2);
        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }
    }

    public static Stage createStage(String resource, String title, boolean setModal, boolean setOwner, StageCreateCallback callback) {
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
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle("%s - Schyfts %s".formatted(title, Reference.VERSION_STRING));
        stage.getIcons().add(Reference.ICON);
        stage.setScene(scene);

        if (setOwner) {
            stage.initOwner(Schyfts.currentStage);
        }

        if (setModal)
            stage.initModality(Modality.WINDOW_MODAL);
        else
            stage.initModality(Modality.NONE);

        if (callback != null) {
            callback.callback(stage);
        }

        stage.show();

        return stage;
    }

    public static Stage createStage(String resource, String title, boolean setModal, boolean setOwner) {
        return createStage(resource, title, setModal, setOwner, null);
    }

    public static Stage createStage(String resource, String title) {
        return createStage(resource, title, true, true);
    }

    public static void changeScene(String resource, String title) {
        currentStage.setTitle(title);
        changeScene(resource);
    }

}
