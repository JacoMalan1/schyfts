package com.codelog.schyfts;

import com.codelog.schyfts.api.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

public class Users implements Initializable {

    @FXML
    Spinner<Integer> spnPerms;
    @FXML
    ChoiceBox<String> cmbUser;
    @FXML
    TextField txtUsername;
    @FXML
    TextField txtEmail;

    private List<User> users;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        users = User.getAllUsers();
        spnPerms.setValueFactory(new IntegerSpinnerValueFactory(0, 100, 0));

        for (var user : users) {
            cmbUser.getItems().add(user.getUsername());
        }

        cmbUser.getSelectionModel().selectedItemProperty().addListener((event, oldValue, newValue) -> {

            txtUsername.clear();
            txtEmail.clear();
            spnPerms.getEditor().setText("0");

            for (var user : users) {
                if (user.getUsername().equals(newValue)) {
                    txtUsername.setText(user.getUsername());
                    txtEmail.setText(user.getEmail());
                    spnPerms.getEditor().setText(String.valueOf(user.getPermissionLevel()));
                }
            }

        });

    }

    public void btnSubmitClick(ActionEvent actionEvent) {
    }

    public void mnuChangePasswordClick(ActionEvent actionEvent) {
    }

    public void mnuAddUserClick(ActionEvent actionEvent) {
    }
}
