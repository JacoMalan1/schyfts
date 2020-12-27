package com.codelog.schyfts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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

    public void btnLoginClick(ActionEvent actionEvent) {

        if (txtUname.getText().equals("jacom") && pwdPass.getText().equals("1234")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Logged in!");
            alert.setTitle("Logged in!");
            alert.show();
        }

    }
}
