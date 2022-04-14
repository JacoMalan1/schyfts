package com.codelog.schyfts;

import com.codelog.schyfts.api.Doctor;
import com.codelog.schyfts.api.LeaveData;
import com.codelog.schyfts.api.UserContext;
import com.codelog.clogg.Logger;
import com.codelog.schyfts.util.Request;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Leave implements Initializable {

    @FXML
    TableView<LeaveData> tblLeave;
    @FXML
    RadioMenuItem mnuShowPastLeave;
    @FXML
    ProgressBar prgStatus;
    @FXML
    Button btnSubmit;
    @FXML
    ChoiceBox<String> cmbDoctor;
    @FXML
    DatePicker dpStartDate;
    @FXML
    DatePicker dpEndDate;

    List<LeaveData> leaveList;
    FilteredList<LeaveData> filteredLeave;
    List<Doctor> doctors;
    private boolean submitAction;

    public void refresh() {

        btnSubmit.setDisable(true);
        doctors = new ArrayList<>();

        prgStatus.setProgress(0);

        tblLeave.getItems().clear();
        leaveList.clear();

        doctors = Doctor.getAllDoctors();
        prgStatus.setProgress(0.33);

        try {
            Request req = new Request(Reference.API_URL + "getAllLeave");
            var body = new JSONObject();
            body.put("token", UserContext.getInstance().getCurrentUser().getToken());
            req.setBody(body);
            req.sendRequest();

            if (!req.getResponse().getString("status").equals("ok"))
                throw new IOException(req.getResponse().getString("message"));

            var results = req.getResponse().getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {

                var doctor = results.getJSONObject(i).getJSONObject("doctor");
                var leaveData = results.getJSONObject(i).getJSONObject("leaveData");

                LeaveData entry = new LeaveData(
                        leaveData.getInt("dID"),
                        doctor.getString("surname"),
                        doctor.getString("name"),
                        LocalDate.parse(leaveData.getString("start").split("T")[0]),
                        LocalDate.parse(leaveData.getString("end").split("T")[0])
                );

                leaveList.add(entry);

            }

        } catch (IOException e) {
            Logger.getInstance().exception(e);
            prgStatus.setProgress(1);
            return;
        }
        prgStatus.setProgress(0.66);

        doctors = Doctor.getAllDoctors();
        prgStatus.setProgress(1);
        cmbDoctor.getItems().clear();
        for (var d : doctors) {
            cmbDoctor.getItems().add(String.format("%s, %s", d.getSurname(), d.getName()));
        }

        Platform.runLater(() -> {
           cmbDoctor.getSelectionModel().select(0);
        });

    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        submitAction = false;

        leaveList = new ArrayList<>();
        tblLeave.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LeaveData, Integer> clmDoctorId = new TableColumn<>("Doctor ID");
        clmDoctorId.setCellValueFactory(new PropertyValueFactory<>("doctorId"));

        TableColumn<LeaveData, String> clmDoctorSurname = new TableColumn<>("Surname");
        clmDoctorSurname.setCellValueFactory(new PropertyValueFactory<>("doctorSurname"));

        TableColumn<LeaveData, String> clmDoctorName = new TableColumn<>("Initials");
        clmDoctorName.setCellValueFactory(new PropertyValueFactory<>("doctorName"));

        TableColumn<LeaveData, LocalDate> clmStartDate = new TableColumn<>("Start Date");
        clmStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<LeaveData, LocalDate> clmEndDate = new TableColumn<>("End Date");
        clmEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        tblLeave.getColumns().add(clmDoctorId);
        tblLeave.getColumns().add(clmDoctorSurname);
        tblLeave.getColumns().add(clmDoctorName);
        tblLeave.getColumns().add(clmStartDate);
        tblLeave.getColumns().add(clmEndDate);

        Thread t = new Thread(this::refresh);
        t.start();

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Refreshing leave...");
        alert.setTitle("Info");
        alert.showAndWait();

        tblLeave.getSortOrder().addAll(clmDoctorSurname, clmDoctorName);

        tblLeave.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cmbDoctor.getSelectionModel().select(
                        String.format("%s, %s", newSelection.getDoctorSurname(), newSelection.getDoctorName())
                );
                dpStartDate.setValue(newSelection.getStartDate());
                dpEndDate.setValue(newSelection.getEndDate());
            }
        });

        cmbDoctor.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            filteredLeave = new FilteredList<>(FXCollections.observableArrayList(leaveList), p -> true);
            tblLeave.setItems(filteredLeave);
            btnSubmit.setDisable(false);
            if (newSelection != null && submitAction) {
                filteredLeave.setPredicate(ld -> newSelection.equals("%s, %s".formatted(
                        ld.getDoctorSurname(), ld.getDoctorName()
                )));
            } else {
                filteredLeave.setPredicate(ld -> true);
            }
        });
    }

    public void mnuAddLeave(ActionEvent actionEvent) {
        submitAction = true;
        btnSubmit.setText("Add");
        cmbDoctor.setValue("");
        dpStartDate.getEditor().setText("");
        dpEndDate.getEditor().setText("");
        tblLeave.getSelectionModel().clearSelection();
    }

    public void mnuShowPastLeave(ActionEvent actionEvent) {
    }

    public void mnuDelete(ActionEvent actionEvent) {

        if (tblLeave.getSelectionModel().getSelectedItem() == null)
            return;

        LeaveData leaveData = tblLeave.getSelectionModel().getSelectedItem();
        boolean success = false;
        Exception exception = new Exception();

        try {
            Request req = new Request(Reference.API_URL + "deleteLeave");
            var startDate = leaveData.getStartDate();
            String startDateStr = String.format("%04d-%02d-%02d",
                    startDate.getYear(),
                    startDate.getMonthValue(),
                    startDate.getDayOfMonth()
            );
            var body = new JSONObject();
            body.put("token", UserContext.getInstance().getCurrentUser().getToken());
            body.put("dID", leaveData.getDoctorId());
            body.put("startDate", startDateStr);
            req.setBody(body);
            req.sendRequest();
            if (!req.getResponse().getString("status").equals("ok"))
                throw new IOException(req.getResponse().getString("message"));
            success = true;
        } catch (IOException e) {
            Logger.getInstance().exception(e);
            exception = e;
        }

        Alert alert;
        if (success)
            alert = new Alert(Alert.AlertType.INFORMATION, "Leave deleted");
        else
            alert = new Alert(Alert.AlertType.ERROR, "Error: " + exception.getLocalizedMessage());

        alert.setTitle(success ? "Done" : "Error");
        alert.show();
        refresh();

    }

    public void btnSubmitClick(ActionEvent actionEvent) {

        if (cmbDoctor.getValue().equals("") ||
                dpStartDate.getEditor().getText().equals("") || dpEndDate.getEditor().getText().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all fields");
            alert.setTitle("Incomplete fields");
            alert.show();
            return;
        }

        if (!submitAction) {
            // TODO: Add leave edit code.
        } else {

            try {

                Request req = new Request(Reference.API_URL + "addLeave");
                var body = new JSONObject();
                body.put("token", UserContext.getInstance().getCurrentUser().getToken());

                var startDate = dpStartDate.getValue();
                String startDateStr = String.format("%04d-%02d-%02d",
                        startDate.getYear(),
                        startDate.getMonthValue(),
                        startDate.getDayOfMonth()
                );

                var endDate = dpEndDate.getValue();
                String endDateStr = String.format("%04d-%02d-%02d",
                        endDate.getYear(),
                        endDate.getMonthValue(),
                        endDate.getDayOfMonth()
                );

                body.put("startDate", startDateStr);
                body.put("endDate", endDateStr);
                Doctor doctor = null;
                for (var d : doctors) {
                    if (String.format("%s, %s", d.getSurname(), d.getName()).equals(cmbDoctor.getValue()))
                        doctor = d;
                }

                if (doctor == null) {
                    Logger.getInstance().error("Couldn't find doctor!");
                    return;
                }

                body.put("id", doctor.getId());
                req.setBody(body);
                req.sendRequest();

                if (!req.getResponse().getString("status").equals("ok")) {
                    Logger.getInstance().error(String.format("Error: %s", req.getResponse().getString("message")));
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Leave added successfully");
                alert.setTitle("Success");
                alert.show();

                LeaveData data = new LeaveData(doctor.getId(), doctor.getSurname(), doctor.getName(), startDate, endDate);
                tblLeave.getItems().add(data);

            } catch (IOException e) {
                Logger.getInstance().exception(e);
            }

            btnSubmit.setText("Submit");
            submitAction = false;

        }

    }
}
