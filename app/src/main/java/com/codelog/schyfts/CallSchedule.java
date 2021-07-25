package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.api.APIException;
import com.codelog.schyfts.api.APIRequest;
import com.codelog.schyfts.api.CallData;
import com.codelog.schyfts.api.Doctor;
import com.codelog.schyfts.util.AlertFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class CallSchedule implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private TableView<CallData> tblCalls;
    @FXML
    private MenuItem mnuAdd;
    @FXML
    private MenuItem mnuDelete;
    @FXML
    private ProgressBar prgStatus;
    @FXML
    private ChoiceBox<Doctor> cmbDoctor;
    @FXML
    private ChoiceBox<Boolean> cmbCall;
    @FXML
    private DatePicker dpDate;

    List<Doctor> doctors;
    List<CallData> callData;
    boolean submitAction = false;

    public void refresh() {
        btnSubmit.setDisable(true);
        doctors = Doctor.getAllDoctors();
        prgStatus.setProgress(0.33);

        callData = CallData.getAllCallData();
        prgStatus.setProgress(0.67);

        tblCalls.getItems().clear();
        tblCalls.getItems().addAll(callData);

        btnSubmit.setDisable(false);

        cmbDoctor.getItems().clear();
        cmbDoctor.getItems().addAll(doctors);

        prgStatus.setProgress(1);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tblCalls.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCalls.getColumns().clear();
        tblCalls.getItems().clear();

        TableColumn<CallData, Integer> clmDoctorId = new TableColumn<>("Doctor ID");
        clmDoctorId.setCellValueFactory(new PropertyValueFactory<>("doctorID"));

        TableColumn<CallData, String> clmName = new TableColumn<>("Name");
        clmName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<CallData, String> clmSurname = new TableColumn<>("Surname");
        clmSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));

        TableColumn<CallData, LocalDate> clmDate = new TableColumn<>("Date");
        clmDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<CallData, String> clmState = new TableColumn<>("Call Status");
        clmState.setCellValueFactory(new PropertyValueFactory<>("state"));

        tblCalls.getColumns().add(clmDoctorId);
        tblCalls.getColumns().add(clmName);
        tblCalls.getColumns().add(clmSurname);
        tblCalls.getColumns().add(clmDate);
        tblCalls.getColumns().add(clmState);

        mnuAdd.setOnAction(event -> {
            submitAction = true;
            btnSubmit.setText("Add");
        });

        AlertFactory.showAndWait("Refreshing call entries...");

        cmbDoctor.setConverter(new StringConverter<>() {
            @Override
            public String toString(Doctor object) {
                if (object == null)
                    return "";
                return "%s, %s".formatted(object.getSurname(), object.getName());
            }

            @Override
            public Doctor fromString(String string) {
                for (var d : doctors) {
                    var fullName = "%s, %s".formatted(d.getSurname(), d.getName());
                    if (fullName.equals(string))
                        return d;
                }
                return null;
            }
        });

        cmbCall.getItems().addAll(true, false);
        cmbCall.setConverter(new StringConverter<>() {
            @Override
            public String toString(Boolean object) {
                if (object == null)
                    return "CALL OFF";
                return object ? "CALL ON" : "CALL OFF";
            }

            @Override
            public Boolean fromString(String string) {
                return string.contains("ON");
            }
        });

        Thread refreshThread = new Thread(this::refresh);
        refreshThread.start();
    }

    public void btnSubmitClick(ActionEvent actionEvent) {

        if (cmbDoctor.getSelectionModel().isEmpty() || cmbCall.getSelectionModel().isEmpty()
                || dpDate.getValue() == null)
            AlertFactory.showAlert(Alert.AlertType.ERROR, "Please complete all fields!");

        if (submitAction) {
            APIRequest req = new APIRequest("addCall", true, "dID", "date", "value");
            var selectedDoctor = cmbDoctor.getSelectionModel().getSelectedItem();
            var dID = selectedDoctor.getId();
            var date = dpDate.getValue();
            var value = cmbCall.getValue();

            try {
                req.send(dID, date.toString().split("T")[0], value);
                AlertFactory.showAlert("Call added!");
            } catch (IOException | APIException e) {
                AlertFactory.showAlert(Alert.AlertType.ERROR, e.getMessage());
                Logger.getInstance().error("Error: Couldn't add call!");
                if (e instanceof APIException)
                    Logger.getInstance().error(((APIException)e).getApiResponse().toString(4));
                Logger.getInstance().exception(e);
            }

        } else {
            AlertFactory.showAndWait(Alert.AlertType.ERROR, "Function not implemented yet!");
        }

        cmbCall.getSelectionModel().clearSelection();
        cmbDoctor.getSelectionModel().clearSelection();
        dpDate.getEditor().clear();
        btnSubmit.setText("Submit");
        submitAction = false;
        Thread t = new Thread(this::refresh);
        t.start();
    }

    public void mnuDeleteClick(ActionEvent actionEvent) {
        if (tblCalls.getSelectionModel().isEmpty())
            return;

        try {
            APIRequest req = new APIRequest("deleteCall", true, "id");
            req.send(tblCalls.getSelectionModel().getSelectedItem().getId());
        } catch (IOException | APIException e) {
            AlertFactory.showAlert(Alert.AlertType.ERROR, "Couldn't delete call!");
            Logger.getInstance().exception(e);
            return;
        }

        AlertFactory.showAlert("Call deleted!");
        Thread t = new Thread(this::refresh);
        t.start();
    }
}
