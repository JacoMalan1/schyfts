package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.api.*;
import com.codelog.schyfts.api.matrix.MatrixEntry;
import com.codelog.schyfts.api.matrix.MatrixValueFactory;
import com.codelog.schyfts.api.matrix.SlotType;
import com.codelog.schyfts.api.schedule.DayList;
import com.codelog.schyfts.api.schedule.Module;
import com.codelog.schyfts.api.schedule.ScheduleEntry;
import com.codelog.schyfts.google.StorageContext;
import com.codelog.schyfts.util.AlertFactory;
import com.codelog.schyfts.util.CSVUtil;
import com.codelog.schyfts.util.FileUtils;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Roster implements Initializable {

    @FXML
    protected TableView<MatrixEntry> tblMatrix;
    @FXML
    private TableView<ScheduleEntry> tblSchedule;
    @FXML
    private Tab tabSchedule;

    private static int MODULES;
    private List<List<Doctor>> sharedModules;
    private int scheduleOffset;

    private static final String[] DAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    private Pair<LocalDate, LocalDate> dateRange;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refresh();

        setupTblMatrix();
        loadMatrix();
        scheduleOffset = 0;
        tabSchedule.setDisable(true);
        tblSchedule.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refresh() {
        try {
            MODULES = Integer.parseInt(ConfigurationProvider.getAPIVariable("modules"));
        } catch (APIException | IOException e) {
            Logger.getInstance().error("Couldn't fetch module number configuration!");
            Logger.getInstance().exception(e);
            if (e instanceof APIException)
                Logger.getInstance().error(((APIException) e).getApiResponse().toString());
            MODULES = 0;
        }

        if (sharedModules == null)
            sharedModules = new ArrayList<>(2);
        else
            sharedModules.clear();

        try {
            var res = APIRequestFactory.createGetSharedModules().send();
            var results = res.getJSONArray("results");
            for (var i = 0; i < results.length(); i++) {
                var sharedModule = results.getJSONObject(i);
                var stringIDs = sharedModule.getString("doctors").split(",");
                var ids = new ArrayList<Integer>(stringIDs.length);
                List.of(stringIDs).forEach(item -> ids.add(Integer.parseInt(item)));
                var doctors = new ArrayList<Doctor>();
                for (var id : ids)
                    doctors.add(Doctor.fromId(id));

                sharedModules.add(doctors);
            }
        } catch (IOException | APIException e) {
            Logger.getInstance().error("Couldn't fetch shared modules!");
            Logger.getInstance().exception(e);
            if (e instanceof APIException)
                Logger.getInstance().error(((APIException) e).getApiResponse().toString());
        }
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
        tblMatrix.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MatrixEntry, String> clmSlot = new TableColumn<>("List");
        clmSlot.setCellValueFactory(param -> new StringBinding() {
            @Override
            protected String computeValue() {
                return param.getValue().getSlot();
            }
        });
        tblMatrix.getColumns().add(clmSlot);

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
        Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Schedule options");
        dialog.setHeaderText("Please select a start and end date");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(20, 150, 10, 10));

        DatePicker dpStartDate = new DatePicker();
        DatePicker dpEndDate = new DatePicker();

        pane.add(new Label("Start Date:"), 0, 0);
        pane.add(dpStartDate, 1, 0);
        pane.add(new Label("End Date:"), 0, 1);
        pane.add(dpEndDate, 1, 1);

        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(dialogButton -> {
            if (!dialogButton.getButtonData().isCancelButton()) {
                return new Pair<>(dpStartDate.getValue(), dpEndDate.getValue());
            }
            return null;
        });

        Optional<Pair<LocalDate, LocalDate>> dateRange = dialog.showAndWait();
        if (dateRange.isEmpty())
            return;

        tabSchedule.setDisable(false);
        tabSchedule.getTabPane().getSelectionModel().select(tabSchedule);

        this.dateRange = dateRange.get();
        var data = generateSchedule();
        if (data != null) {
            tblSchedule.getColumns().clear();
            tblSchedule.getColumns().addAll(data.getValue());
            tblSchedule.getItems().clear();
            tblSchedule.getItems().addAll(data.getKey());
        } else {
            Logger.getInstance().error("Couldn't generate Roster");
            AlertFactory.showAlert(Alert.AlertType.ERROR, "Couldn't generate roster!");
        }
    }

    private Pair<List<ScheduleEntry>, List<TableColumn<ScheduleEntry, ?>>> generateSchedule() {
        List<TableColumn<ScheduleEntry, ?>> columnsToAdd = new ArrayList<>(23);
        List<ScheduleEntry> entriesToAdd = new ArrayList<>(12);
        var daysSinceGenesis = dateRange.getKey().toEpochDay() - Reference.GENESIS_TIME.toEpochDay();
        var initialOffset = (scheduleOffset + (int)Math.floor((float)daysSinceGenesis / 7.0f) + 2) % MODULES;

        TableColumn<ScheduleEntry, String> clmList = new TableColumn<>("List");
        clmList.setCellValueFactory(param -> new StringBinding() {
            @Override
            protected String computeValue() {
                return param.getValue().getSlotName();
            }
        });
        columnsToAdd.add(clmList);

        List<ScheduleEntry> scheduleEntries = new ArrayList<>();
        List<Module> moduleList = assignDoctorsToModules(initialOffset);
        for (var i = 0; i < moduleList.size(); i++) {
            final Module module = moduleList.get(i);
            TableColumn<ScheduleEntry, String> clmModule = new TableColumn<>(String.valueOf(i + 1));

            for (var assignedDoctor : moduleList.get(i).getAssignedDoctors()) {
                var dName = "%s, %s".formatted(assignedDoctor.getSurname(), assignedDoctor.getName());
                TableColumn<ScheduleEntry, String> clmDoctor = new TableColumn<>(dName);

                clmDoctor.setCellValueFactory(param -> new StringBinding() {
                    @Override
                    protected String computeValue() {
                        return param.getValue().getMainSchedule().get(module).getContents();
                    }
                });
                clmModule.getColumns().add(clmDoctor);
            }

            columnsToAdd.add(clmModule);
            return new Pair<>(entriesToAdd, columnsToAdd);
        }

        var date = dateRange.getKey().plusWeeks(scheduleOffset);
        for (var matrixEntry : tblMatrix.getItems()) {
            var scheduleEntry = new ScheduleEntry(date, matrixEntry.getSlotType());
            for (var key : matrixEntry.getModuleMap().keySet()) {
                var correspondingModule = moduleList.get(key - 1);
                var dayList = new DayList(matrixEntry.getModuleMap().get(key), false, false);
                scheduleEntry.getMainSchedule().put(correspondingModule, dayList);
            }
            scheduleEntries.add(scheduleEntry);

            if (matrixEntry.getSlotType() == SlotType.PM || matrixEntry.getSlotType() == SlotType.WEEKEND)
                date = date.plusDays(1L);
        }

        entriesToAdd.addAll(scheduleEntries);
        return null;
    }

    private List<Module> assignDoctorsToModules(int offset) {
        var doctorOrderStr = ConfigurationContext.getInstance().getValue("doctorOrder").split(",");
        List<Integer> doctorOrder = new ArrayList<>(doctorOrderStr.length);
        for (var str : doctorOrderStr)
            doctorOrder.add(Integer.parseInt(str));

        List<Module> result = new ArrayList<>(MODULES);

        // Loop backwards, since we'll be removing items from the
        // list we're working with.
        List<Integer> skipList = new ArrayList<>(2);
        for (Integer id : doctorOrder) {
            var doctor = Doctor.fromId(id);

            if (skipList.contains(id))
                continue;

            boolean shared = false;
            for (var sm : sharedModules) {
                if (sm.contains(doctor)) {
                    result.add(new Module(sm));

                    for (var d : sm) {
                        skipList.add(d.getId());
                    }
                    shared = true;
                }
            }

            if (shared)
                continue;

            result.add(new Module(Doctor.fromId(id)));
        }

        return result;
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