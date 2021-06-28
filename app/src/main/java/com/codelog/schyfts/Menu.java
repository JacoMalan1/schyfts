package com.codelog.schyfts;

import com.codelog.schyfts.api.UserContext;
import com.codelog.clogg.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class Menu implements Initializable {

    public static Stage stage;

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
    @FXML
    private ImageView imgLogo;

    public void btnRosterClick(ActionEvent actionEvent) {
        Roster.primaryStage = Schyfts.createStage("roster.fxml", "Roster");
    }

    public void btnLeaveCalendarClick(ActionEvent actionEvent) {
        if (UserContext.getInstance().getCurrentUser() != null) {
            Schyfts.createStage("leave.fxml", "Leave");
        }
    }

    public void btnPatientReportsClick(ActionEvent actionEvent) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Image img = new Image(Objects.requireNonNull(
                    getClass().getClassLoader().getResourceAsStream("nelanest.png")
            ));
            imgLogo.setImage(img);
        } catch (NullPointerException e) {
            Logger.getInstance().exception(e);
            Logger.getInstance().error("Couldn't load logo");
        }

        btnPatientReports.setDisable(true);
        if (UserContext.getInstance().getCurrentUser() != null) {
            indLoggedIn.fillProperty().set(Paint.valueOf("LIME"));
            var currentUser = UserContext.getInstance().getCurrentUser();
            lblUsername.setText(String.format("%s (%d)", currentUser.getUsername(), currentUser.getPermissionLevel()));
        }
    }

    public void btnDoctorInformationClick(ActionEvent actionEvent) {

        if (UserContext.getInstance().getCurrentUser() != null) {
            Schyfts.createStage("doctors.fxml", "Doctors");
        }

    }

    public void btnUserManagementClick(ActionEvent actionEvent) {

        if (UserContext.getInstance().getCurrentUser() != null)
            Schyfts.createStage("users.fxml", "User Management");

    }

    public void btnSurgeonLeaveClick(ActionEvent actionEvent) {
        if (UserContext.getInstance().getCurrentUser() != null)
            Schyfts.createStage("surgeons_leave.fxml", "Surgeon Leave");
    }
}
