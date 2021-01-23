package com.codelog.schyfts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;

import java.net.URL;
import java.util.*;

@SuppressWarnings({"rawtypes"})
public class Roster implements Initializable {

    private static int MODULES = 18;
    private static int LISTS = 10;
    private static int CALLS = 3;
    private String[][] matrix;

    @FXML
    private TableView<Map> tblRoster;

    public void mnuClose(ActionEvent actionEvent) {
        Schyfts.changeScene("menu.fxml", "Menu");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        matrix = new String[MODULES][LISTS + CALLS];
        List<String> lists = new ArrayList<>();

        lists.add("Mon AM");
        lists.add("Mon PM");
        lists.add("Tue AM");
        lists.add("Tue PM");
        lists.add("Wed AM");
        lists.add("Wed PM");
        lists.add("Thu AM");
        lists.add("Thu PM");
        lists.add("Fri AM");
        lists.add("Fri PM");

        for (int i = 0; i < CALLS; i++) {
            lists.add(String.format("Call %d", i + 1));
        }

        for (int i = 0; i < MODULES; i++) {
            for (int j = 0; j < LISTS + CALLS; j++) {
                matrix[i][j] = String.valueOf(i * j);
            }
        }

        TableColumn<Map, String> clmLabel = new TableColumn<>("List");
        clmLabel.setCellValueFactory(new MapValueFactory<>("list"));
        tblRoster.getColumns().add(clmLabel);
        for (int i = 1; i < MODULES + 1; i++) {

            TableColumn<Map, String> clm = new TableColumn<>(String.valueOf(i));
            clm.setCellValueFactory(new MapValueFactory<>(String.valueOf(i)));
            tblRoster.getColumns().add(clm);

        }

        ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

        for (int j = 0; j < LISTS + CALLS; j++) {
            Map<String, Object> item = new HashMap<>();
            item.put("list", lists.get(j));
            for (int i = 0; i < MODULES; i++) {
                item.put(String.valueOf(i + 1), matrix[i][j]);
            }
            items.add(item);
        }

        tblRoster.getItems().addAll(items);

    }
}
