package com.codelog.schyfts;

import com.codelog.schyfts.api.UserContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class Menu implements Initializable {

    @FXML
    public Circle indLoggedIn;
    @FXML
    private Button btnRoster;
    @FXML
    private Button btnLeaveCalendar;
    @FXML
    private Button btnPatientReports;
    @FXML
    private Label lblUsername;

    public void btnRosterClick(ActionEvent actionEvent) {

    }

    public void btnLeaveCalendarClick(ActionEvent actionEvent) {

    }

    public void btnPatientReportsClick(ActionEvent actionEvent) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (UserContext.getInstance().getCurrentUser() != null) {
            indLoggedIn.fillProperty().set(Paint.valueOf("LIME"));
            lblUsername.setText(UserContext.getInstance().getCurrentUser().getUsername());
        }
    }
}
