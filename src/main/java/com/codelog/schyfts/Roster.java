package com.codelog.schyfts;

import com.codelog.schyfts.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings({"rawtypes"})
public class Roster implements Initializable {

    private static int MODULES = 16;
    private static int LISTS = 10;
    private static int CALLS = 3;
    private String[][] matrix;
    private static List<String> lists;
    public static Stage primaryStage;

    @FXML
    private TableView<Map> tblRoster;

    public void loadRoster() {
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("roster.csv"));
            String line;
            while ((line = reader.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }

        for (int i = 0; i < lines.size(); i++) {

            String[] split = lines.get(i).split(",");
            for (int j = 0; j < split.length; j++)
                matrix[j][i] = split[j];

        }
    }

    public void loadModules() {

    }

    public void updateItems() {
        tblRoster.getItems().clear();
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tblRoster.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tblRoster.setEditable(true);
        matrix = new String[MODULES][LISTS + CALLS];
        loadRoster();

        lists = new ArrayList<>();

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

        TableColumn<Map, String> clmLabel = new TableColumn<>("List");
        clmLabel.setCellValueFactory(new MapValueFactory<>("list"));
        clmLabel.setSortable(false);
        tblRoster.getColumns().add(clmLabel);
        for (int i = 1; i < MODULES + 1; i++) {

            TableColumn<Map, String> clm = new TableColumn<>(String.valueOf(i));
            clm.setEditable(true);
            clm.setCellValueFactory(new MapValueFactory<>(String.valueOf(i)));
            clm.setCellFactory(TextFieldTableCell.forTableColumn());

            clm.setSortable(false);
            clm.setOnEditCommit(event -> {
                    matrix[tblRoster.getColumns().indexOf(clm) - 1][tblRoster.getSelectionModel().getSelectedIndex()] = event.getNewValue();
                    updateItems();
            });

            tblRoster.getColumns().add(clm);
        }
        updateItems();

        Thread thread = new Thread(this::loadModules);
        thread.start();

    }

    public void mnuSave(ActionEvent actionEvent) {
        List<String> lines = new ArrayList<>();
        var flipMat = new String[LISTS + CALLS][MODULES];

        for (int i = 0; i < MODULES; i++) {
            for (int j = 0; j < LISTS + CALLS; j++) {
                flipMat[j][i] = matrix[i][j];
            }
        }

        for (int i = 0; i < LISTS + CALLS; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < MODULES - 1; j++) {
                builder.append(flipMat[i][j]).append(',');
            }
            builder.append(flipMat[i][LISTS + CALLS - 1]);
            lines.add(builder.toString());
        }

        try {
            if (Files.exists(Path.of("roster.csv").toAbsolutePath()))
                Files.delete(Path.of("roster.csv").toAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter("roster.csv"));
            for (String line : lines) {
                writer.write(line);
                writer.flush();
                writer.newLine();
            }
            writer.close();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Roster saved!");
            alert.setTitle("Saved");
            alert.show();

        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }

        Logger.getInstance().info("Saved roster to 'roster.csv'");

    }

}
