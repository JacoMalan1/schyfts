package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.util.FileUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.ResourceBundle;

@SuppressWarnings("rawtypes")
public class SurgeonLeave implements Initializable {
    @FXML
    TextField txtName;
    @FXML
    TextField txtSurname;
    @FXML
    DatePicker dpStartDate;
    @FXML
    DatePicker dpEndDate;
    @FXML
    TableView<Map> tblLeave;
    @FXML
    Button btnSubmit;

    private JSONObject leaveJson;

    private boolean action;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {

        TableColumn<Map, String> clmName = new TableColumn<>("Name");
        clmName.setCellValueFactory(new MapValueFactory<>("name"));

        TableColumn<Map, String> clmSurname = new TableColumn<>("Surname");
        clmSurname.setCellValueFactory(new MapValueFactory<>("surname"));

        TableColumn<Map, String> clmStartDate = new TableColumn<>("Start Date");
        clmStartDate.setCellValueFactory(new MapValueFactory<>("start"));
        TableColumn<Map, String> clmEndDate = new TableColumn<>("End Date");
        clmEndDate.setCellValueFactory(new MapValueFactory<>("end"));

        tblLeave.getColumns().addAll(clmName, clmSurname, clmStartDate, clmEndDate);

        action = false;
        leaveJson = new JSONObject();
        leaveJson.put("leave", new JSONArray());

    }

    public void mnuAddLeave(ActionEvent actionEvent) {
        btnSubmit.setText("Add");
        action = true;
    }

    public void mnuShowPastLeave(ActionEvent actionEvent) {
    }

    public void mnuDelete(ActionEvent actionEvent) {
    }

    public void btnSubmitClick(ActionEvent actionEvent) {

        if (!action) {

        } else {

            var leave = new JSONObject();
            leave.put("name", txtName.getText());
            leave.put("surname", txtName.getText());
            leave.put("startDate", dpStartDate.getValue().toString());
            leave.put("endDate", dpEndDate.getValue().toString());
            leaveJson.getJSONArray("leave").put(leave);



        }

        try {
            FileUtils.writeFile("leave.json", leaveJson.toString(4));
        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }

    }
}
