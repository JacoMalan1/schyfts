package com.codelog.schyfts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;

import java.net.URL;
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

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {

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

    }

    public void mnuAddLeave(ActionEvent actionEvent) {
    }

    public void mnuShowPastLeave(ActionEvent actionEvent) {
    }

    public void mnuDelete(ActionEvent actionEvent) {
    }

    public void btnSubmitClick(ActionEvent actionEvent) {
    }
}
