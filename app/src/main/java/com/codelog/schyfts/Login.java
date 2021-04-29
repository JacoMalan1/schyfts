package com.codelog.schyfts;

import com.codelog.schyfts.api.User;
import com.codelog.clogg.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;

import java.net.URL;
import java.util.ResourceBundle;

public class Login implements Initializable {

    @FXML
    private TextField txtUname;
    @FXML
    private PasswordField pwdPass;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var stage = Schyfts.createStage("logs.fxml", "Log", false);
        assert stage != null;

        var x = Screen.getPrimary().getBounds().getMinX();
        var y = Screen.getPrimary().getBounds().getMinY();

        stage.setX(x);
        stage.setY(y);
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
            Schyfts.createStage("menu.fxml", "Menu", false);
        }

    }
}
