package com.codelog.schyfts;

import com.codelog.schyfts.api.User;
import com.codelog.clogg.Logger;
import com.codelog.schyfts.concurrency.Callback;
import com.codelog.schyfts.concurrency.CallbackWorker;
import com.codelog.schyfts.util.AlertFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

public class Login implements Initializable, Callback {

    @FXML
    private TextField txtUname;
    @FXML
    private PasswordField pwdPass;

    private boolean result;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var stage = Schyfts.createStage("logs.fxml", "Log", false, false);
        assert stage != null;

        var x = Screen.getPrimary().getBounds().getMinX();
        var y = Screen.getPrimary().getBounds().getMinY();

        stage.setX(x);
        stage.setY(y);

        CallbackWorker worker = new CallbackWorker(this) {
            @Override
            public void run() {
                result = false;
                try {
                    URL url = new URL(Reference.API_URL);
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    result = true;
                } catch (IOException ignored) {
                }
                callback();
            }
        };
        Thread t = new Thread(worker);
        t.start();
    }

    public void btnLoginClick(ActionEvent actionEvent) {
        String uname = txtUname.getText();
        String pword = pwdPass.getText();

        Logger.getInstance().debug(String.format("Attemping login (user: %s)", uname));

        User user = new User(uname);
        var loggedIn = user.login(pword);

        if (!loggedIn) {
            Logger.getInstance().debug("Login unsuccessful");
            Alert alert = new Alert(
                    Alert.AlertType.ERROR,
                    "Login error"
            );
            alert.setTitle("Error");
            alert.show();
        } else {
            Logger.getInstance().debug(String.format("Login successfull (token: %s)", user.getToken().substring(0, 4)));
            var newStage = Schyfts.createStage("menu.fxml", "Menu", false, false);
            var oldStage = Schyfts.currentStage;
            Schyfts.currentStage = newStage;
            oldStage.close();
        }
    }

    @Override
    public void callback() {
        Platform.runLater(() -> {
            if (!result) {
                AlertFactory.showAlert(Alert.AlertType.ERROR, "No internet connetion!");
            }
        });
    }
}
