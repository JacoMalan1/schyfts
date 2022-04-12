package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.api.APIException;
import com.codelog.schyfts.api.APIRequest;
import com.codelog.schyfts.util.AlertFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

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
    ProgressBar prgStatus;
    @FXML
    Button btnSubmit;

    private JSONObject leaveJson;
    private ObservableList<Map<String, String>> leaveList;
    private FilteredList<Map<String, String>> filteredLeave;

    private boolean action;
    public static SurgeonLeave instance;


    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {

        leaveList = FXCollections.observableList(new ArrayList<>());

        if (prgStatus != null && tblLeave != null) {

            TableColumn<Map, String> clmId = new TableColumn<>("ID");
            clmId.setCellValueFactory(new MapValueFactory<>("id"));

            TableColumn<Map, String> clmName = new TableColumn<>("Name");
            clmName.setCellValueFactory(new MapValueFactory<>("name"));

            TableColumn<Map, String> clmSurname = new TableColumn<>("Surname");
            clmSurname.setCellValueFactory(new MapValueFactory<>("surname"));

            TableColumn<Map, String> clmStartDate = new TableColumn<>("Start Date");
            clmStartDate.setCellValueFactory(new MapValueFactory<>("start"));
            TableColumn<Map, String> clmEndDate = new TableColumn<>("End Date");
            clmEndDate.setCellValueFactory(new MapValueFactory<>("end"));

            tblLeave.getColumns().addAll(clmId, clmName, clmSurname, clmStartDate, clmEndDate);

            action = false;
            leaveJson = new JSONObject();
            leaveJson.put("leave", new JSONArray());

            instance = this;

            AlertFactory.showAndWait("Refreshing Leave");

            txtSurname.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!oldValue.equals(newValue) && !newValue.equals("")) {
                    filteredLeave = new FilteredList<>(leaveList);
                    filteredLeave.setPredicate(p -> p.get("surname").contains(newValue));
                } else {
                    filteredLeave = new FilteredList<>(leaveList);
                    filteredLeave.setPredicate(p -> true);
                }
                tblLeave.getItems().clear();
                tblLeave.getItems().addAll(filteredLeave);
            });
        }

        startRefresh();

    }

    public void startRefresh() {
        try {
            Method refresh = getClass().getMethod("refresh");
            Runnable r = () -> {
                try {
                    refresh.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
            Thread t = new Thread(r);
            t.start();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static List<JSONObject> refresh() {
        if (instance == null) {
            instance = new SurgeonLeave();
            instance.initialize(null, null);
        }

        List<JSONObject> leaveList = new ArrayList<>();

        try {
            APIRequest req = new APIRequest("getAllSurgeonLeave", true);
            var res = req.send();

            if (instance.prgStatus != null)
                instance.prgStatus.setProgress(0.33);

            var results = res.getJSONArray("results");

            var items = FXCollections.<Map<String, String>>observableArrayList();
            for (int i = 0; i < results.length(); i++) {
                var leave = results.getJSONObject(i);
                leaveList.add(leave);

                Map<String, String> item = new HashMap<>();
                item.put("name", leave.getString("name"));
                item.put("surname", leave.getString("surname"));
                item.put("start", leave.getString("start").split("T")[0]);
                item.put("end", leave.getString("end").split("T")[0]);
                item.put("id", String.valueOf(leave.getInt("id")));
                items.add(item);
            }
            if (instance.prgStatus != null)
                instance.prgStatus.setProgress(0.67);

            if (instance.tblLeave != null) {
                instance.tblLeave.getItems().clear();
                instance.tblLeave.getItems().addAll(items);
                instance.leaveList.clear();
                instance.leaveList.addAll(items);
            }
        } catch (IOException | APIException e) {
            Logger.getInstance().error("Couldn't refresh leave");
            Logger.getInstance().exception(e);
        }

        if (instance.prgStatus != null)
            instance.prgStatus.setProgress(1);

        return leaveList;
    }

    public void mnuDelete(ActionEvent actionEvent) {

        if (tblLeave.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        APIRequest req = new APIRequest("deleteSurgeonLeave", true, "surname", "startDate");

        var debugSurname = "";
        var debugStart = "";
        try {
            Map item = tblLeave.getSelectionModel().getSelectedItem();
            debugSurname = (String)item.get("surname");
            debugStart = (String)item.get("start");
            req.send(item.get("surname"), item.get("start"));
            AlertFactory.showAlert("Leave removed");
        } catch (IOException | APIException e) {
            Logger.getInstance().debug("surname: " + debugSurname);
            Logger.getInstance().debug("start: " + debugStart);

            Logger.getInstance().error("Couldn't delete leave");
            Logger.getInstance().exception(e);
        }

        startRefresh();

    }

    public void btnSubmitClick(ActionEvent actionEvent) {

            APIRequest req = new APIRequest("addSurgeonLeave", true,
                    "name", "surname", "startDate", "endDate");
            try {
                var res = req.send(
                        txtName.getText(),
                        txtSurname.getText(),
                        dpStartDate.getValue().toString(),
                        dpEndDate.getValue().toString()
                );

                AlertFactory.showAlert("Leave added");
            } catch (APIException | IOException e) {
                Logger.getInstance().error("Couldn't upload leave");
                Logger.getInstance().exception(e);
            }

            startRefresh();

    }
}
