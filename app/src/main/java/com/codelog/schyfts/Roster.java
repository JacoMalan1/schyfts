package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.api.APIException;
import com.codelog.schyfts.api.ConfigurationProvider;
import com.codelog.schyfts.api.matrix.MatrixEntry;
import com.codelog.schyfts.api.matrix.MatrixValueFactory;
import com.codelog.schyfts.google.StorageContext;
import com.codelog.schyfts.util.AlertFactory;
import com.codelog.schyfts.util.CSVUtil;
import com.codelog.schyfts.util.FileUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class Roster implements Initializable {

    @FXML
    private TableView<MatrixEntry> tblMatrix;
    private static int MODULES;

    private static final String[] DAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            MODULES = Integer.parseInt(ConfigurationProvider.getAPIVariable("modules"));
        } catch (APIException | IOException e) {
            Logger.getInstance().error("Couldn't fetch module number configuration!");
            Logger.getInstance().exception(e);
            if (e instanceof APIException)
                Logger.getInstance().error(((APIException) e).getApiResponse().toString());
            MODULES = 0;
        }
        setupTblMatrix();
        loadMatrix();
    }

    private void loadMatrix() {
        var storage = StorageContext.getInstance().getStorage();
        var bucket = storage.get(Reference.MAIN_STORAGE_BUCKET);
        var matrixBlob = bucket.get("matrix.csv");

        var matrixFilePath = Path.of("matrix.csv");
        try {
            if (Files.exists(matrixFilePath))
                Files.delete(matrixFilePath);
            if (matrixBlob == null) {
                Logger.getInstance().error("Matrix blob is null!");
                return;
            }

            matrixBlob.downloadTo(matrixFilePath);
            var contents = FileUtils.readFileToString("matrix.csv");
            var parsedContents = CSVUtil.parseCSV(contents.split("\n"));
            var entries = MatrixEntry.fromCSVData(parsedContents, DAYS);

            tblMatrix.setItems(entries);
            tblMatrix.refresh();
        } catch (IOException e) {
            Logger.getInstance().error("Couldn't load matrix!");
            Logger.getInstance().exception(e);

            AlertFactory.showAlert(Alert.AlertType.ERROR,
                    "Couldn't download matrix file from server!");
        }
    }

    private void setupTblMatrix() {
        tblMatrix.getColumns().clear();

        TableColumn<MatrixEntry, String> clmList = new TableColumn<>("List");
        clmList.setCellValueFactory(new PropertyValueFactory<>("list"));
        tblMatrix.getColumns().add(clmList);

        for (short i = 0; i < MODULES; i++) {
            short moduleNum = (short)(i + 1);
            TableColumn<MatrixEntry, String> clm = new TableColumn<>(String.valueOf(moduleNum));
            clm.setCellValueFactory(new MatrixValueFactory(moduleNum));
            clm.setCellFactory(TextFieldTableCell.forTableColumn());
            clm.setOnEditCommit(event -> {
                event.getRowValue().getModuleMap().replace(moduleNum, event.getNewValue());
                tblMatrix.refresh();
            });
            tblMatrix.getColumns().add(clm);
        }
    }

    public void mnuGenerateScheduleClick(ActionEvent actionEvent) {
    }

    public void mnuSaveScheduleClick(ActionEvent actionEvent) {
    }

    public void mnuSaveMatrixClick(ActionEvent actionEvent) {
    }

    public void mnuPrintClick(ActionEvent actionEvent) {
    }

    public void mnuLoadScheduleClick(ActionEvent actionEvent) {
    }

    public void btnPrevClick(ActionEvent actionEvent) {
    }

    public void btnNextClick(ActionEvent actionEvent) {
    }
}