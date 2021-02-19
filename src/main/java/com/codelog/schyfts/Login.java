package com.codelog.schyfts;

import com.codelog.schyfts.api.User;
import com.codelog.schyfts.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Login implements Initializable {

    @FXML
    private Button btnLogin;
    @FXML
    private TextField txtUname;
    @FXML
    private PasswordField pwdPass;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
            Logger.getInstance().debug(String.format("Login successfull (token: %s)", user.getToken()));
            Schyfts.changeScene("menu.fxml", "Menu");
        }

    }
}
