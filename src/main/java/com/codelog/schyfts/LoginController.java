package com.codelog.schyfts;

import com.codelog.schyfts.api.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button btnLogin;
    @FXML
    private TextField txtUname;
    @FXML
    private PasswordField pwdPass;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
    }

    public void btnLoginClick(ActionEvent actionEvent) throws IOException {

        String uname = txtUname.getText();
        String pword = pwdPass.getText();

        User user = new User(uname);
        var loggedIn = user.login(pword);

        Alert alert = new Alert(
                loggedIn ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                loggedIn ? "Logged in!" : "Error!"
        );

        alert.show();

        if (loggedIn) {
            Schyfts.changeScene("menu.fxml");
        }

    }
}
