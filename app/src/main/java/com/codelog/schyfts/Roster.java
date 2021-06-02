package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.api.APIException;
import com.codelog.schyfts.api.APIRequest;
import com.codelog.schyfts.api.Doctor;
import com.codelog.schyfts.api.LeaveData;
import com.codelog.schyfts.google.StorageContext;
import com.codelog.schyfts.util.RosterUtils;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

@SuppressWarnings({"rawtypes"})
public class Roster implements Initializable {

    private static final int MODULES = 16;
    private static final int LISTS = 14;
    private static final int CALLS = 3;
    private static final int LOCI = 4;
    private static final int STATIC_COLUMNS = 1;
    private String[][] matrix;
    private static List<String> lists;
    public static Stage primaryStage;
    private List<Doctor> doctors;
    private static final int STATIC_MODULES = 3;
    private Map<Integer, List<Integer>> moduleMap;
    private Map<Integer, String> doctorNames;
    private List<String> keys;
    private List<LeaveData> doctorLeave;
    private Bucket storageBucket;
    private Map<Integer, Integer> sharedModules;
    private int scheduleOffset;
    private static Optional<Pair<LocalDate, LocalDate>> dateRange;

    @FXML
    private TableView<Map> tblRoster;
    @FXML
    private TableView<Map> tblSchedule;
    @FXML
    private Tab tabMatrix;
    @FXML
    private Tab tabSchedule;
    @FXML
    private BorderPane bdpRoot;

    public void loadRoster() {
        var matrixBlob = storageBucket.get("matrix.csv");
        if (matrixBlob != null) {
            try {
                if (Files.exists(Path.of("matrix.csv")))
                    Files.delete(Path.of("matrix.csv"));
                matrixBlob.downloadTo(Path.of("matrix.csv"));
            } catch (IOException e) {
                Logger.getInstance().exception(e);
            }
        }

        if (!Files.exists(Path.of("matrix.csv"))) {
            return;
        }

        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("matrix.csv"));
            String line;
            while ((line = reader.readLine()) != null)
                lines.add(line);
        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }

        for (int i = 0; i < lines.size(); i++) {

            String[] split = lines.get(i).split(",");
            for (int j = 0; j < split.length; j++) {
                if (split[j].equals("\"\"") || split[j].equals("null"))
                    matrix[j][i] = "";
                else
                    matrix[j][i] = split[j];
            }

        }
    }

    public void refresh() {

        doctorNames = new HashMap<>();

        var doctors = Doctor.getAllDoctors();
        if (doctors == null) {
            Logger.getInstance().error("Couldn't retrieve doctors");
            return;
        }

        for (var d : doctors)
            doctorNames.put(d.getId(), String.format("%s %s", d.getSurname(), d.getName()));

        doctorLeave = LeaveData.getAllLeave();

        try {
            APIRequest getSettingRequest = new APIRequest("getSetting", true, "key");
            var response = getSettingRequest.send("scheduleOffset");
            Logger.getInstance().debug(String.format(
                    "Got setting scheduleOffset: %s",
                    response.getJSONObject("result").getString("value"))
            );
            Logger.getInstance().debug(response.toString(4));
            scheduleOffset = Integer.parseInt(response.getJSONObject("result").getString("value"));
        } catch (IOException | IllegalArgumentException | APIException e) {
            Logger.getInstance().exception(e);
        }

    }

    public void updateItems() {
        tblRoster.getItems().clear();
        ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();
        for (int j = 0; j < LISTS; j++) {
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

        tblSchedule.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        sharedModules = getSharedModules();
        var temp = Doctor.getAllDoctors();
        doctors = new ArrayList<>();

        APIRequest req = new APIRequest("getSetting", true, "key");
        assert temp != null;
        List<Integer> order = new ArrayList<>();
        try {
            var res = req.send("doctorOrder");
            var stringResult = res.getJSONObject("result").getString("value").split(",");
            for (var s : stringResult)
                order.add(Integer.parseInt(s));
        } catch (IOException | APIException e) {
            Logger.getInstance().exception(e);
        }

        for (var item : order) {
            for (Doctor d : temp) {
                if (d.getId() == item) {
                    doctors.add(d);
                }
            }
        }

        scheduleOffset = 0;

        var stream = getClass().getClassLoader().getResourceAsStream("google/service-account.json");
        if (stream != null) {

            var storage = StorageContext.getInstance().getStorage();
            storageBucket = storage.get("nelanest-roster");

        } else {
            Logger.getInstance().error("Couldn't load storage credentials");
        }

        tabSchedule.setDisable(true);

        tblRoster.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tblRoster.setEditable(true);
        matrix = new String[MODULES][LISTS];
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
        lists.add("Sat AM");
        lists.add("Sat PM");
        lists.add("Sun AM");
        lists.add("Sun PM");

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

        Thread thread = new Thread(this::refresh);
        thread.start();

    }

    public void mnuSave(ActionEvent actionEvent) {
        List<String> lines = new ArrayList<>();
        var flipMat = new String[LISTS][MODULES];

        for (int i = 0; i < MODULES; i++) {
            for (int j = 0; j < LISTS; j++) {
                flipMat[j][i] = matrix[i][j];
            }
        }

        for (int i = 0; i < LISTS; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < MODULES - 1; j++) {
                builder.append(flipMat[i][j]).append(',');
            }
            builder.append(flipMat[i][MODULES - 1]);
            lines.add(builder.toString());
        }

        try {
            if (Files.exists(Path.of("matrix.csv").toAbsolutePath()))
                Files.delete(Path.of("matrix.csv").toAbsolutePath());

            StringBuilder builder = new StringBuilder();
            BufferedWriter writer = new BufferedWriter(new FileWriter("matrix.csv"));
            for (String line : lines) {
                writer.write(line);
                builder.append(line).append('\n');
                writer.flush();
                writer.newLine();
            }
            writer.close();

            storageBucket.create("matrix.csv", getClass().getResourceAsStream("matrix.csv"));
            Blob matrixBlob = storageBucket.get("matrix.csv");
            var writeChannel = matrixBlob.writer();

            String contents = builder.toString();
            byte[] arr = contents.getBytes();
            ByteBuffer buff = ByteBuffer.allocate(arr.length);
            buff.put(arr);
            buff.rewind();
            writeChannel.write(buff);
            writeChannel.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Roster saved!");
            alert.setTitle("Saved");
            alert.show();

        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }

        Logger.getInstance().info("Saved roster to 'matrix.csv'");

    }

    public static Map<Integer, Integer> getSharedModules() {
        Map<Integer, Integer> sharedModules = new HashMap<>();

        try {
            APIRequest req = new APIRequest("getSharedModules", true);
            var response = req.send();

            if (!response.getString("status").equals("ok"))
                throw new IOException(response.getString("message"));

            var results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                for (String s : results.getJSONObject(i).getString("doctors").split(","))
                    sharedModules.put(Integer.parseInt(s), results.getJSONObject(i).getInt("moduleNum"));
            }
        } catch (IOException | APIException e) {
            Logger.getInstance().exception(e);
        }
        return sharedModules;
    }

    public void constructModuleMap() {
        moduleMap = new HashMap<>();
        assert dateRange.isPresent();
        var period = dateRange.get().getKey().toEpochDay() - Reference.GENESIS_TIME.toEpochDay();

        int currentModule = (scheduleOffset + (int)Math.floor((float)period / 7.0f) + 2) % MODULES;
        for (Doctor d : doctors) {
            if (!sharedModules.containsKey(d.getId())) {
                moduleMap.put(currentModule + 1, List.of(d.getId()));
                currentModule++;
                currentModule %= MODULES;
            }
        }

        var copy = new HashMap<>(sharedModules);
        for (var d : sharedModules.keySet()) {
            if (!moduleMap.containsKey(currentModule + 1)) {
                moduleMap.put(currentModule + 1, new ArrayList<>());
            }
            moduleMap.get(currentModule + 1).add(d);
            copy.remove(d);
            if (!copy.containsValue(sharedModules.get(d))) {
                currentModule++;
                currentModule %= MODULES;
            }
        }
    }

    public void mnuGenerateSchedule(ActionEvent actionEvent) {
        tblSchedule.getStylesheets().add("styles.css");
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

        dateRange = dialog.showAndWait();
        generateSchedule();
    }

    private int maxWeeks;
    private JSONObject surgeonLeaveJson;

    public void generateSchedule() {
        maxWeeks = 0;

        tblSchedule.getColumns().clear();
        tblSchedule.getItems().clear();
        assert !dateRange.isPresent();

        keys = new ArrayList<>();
        tabSchedule.setDisable(false);
        tabSchedule.getTabPane().getSelectionModel().select(tabSchedule);
        tblSchedule.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tblSchedule.getItems().clear();
        tblSchedule.getColumns().clear();
        tblSchedule.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tblSchedule.setEditable(true);

        constructModuleMap();

        TableColumn<Map, String> clmList = new TableColumn<>("List");
        clmList.setCellValueFactory(new MapValueFactory<>("List"));
        tblSchedule.getColumns().add(clmList);
        keys.add("List");

        for (var module : moduleMap.keySet()) {

            TableColumn<Map, String> clm = new TableColumn<>(String.valueOf(module));

            for (var doctor : moduleMap.get(module)) {
                TableColumn<Map, String> clmDoctor = new TableColumn<>(doctorNames.get(doctor));
                clmDoctor.setCellValueFactory(new MapValueFactory<>(doctorNames.get(doctor)));
                keys.add(doctorNames.get(doctor));
                clmDoctor.setEditable(true);
                clmDoctor.setCellFactory(param -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText("");
                            setStyle("");
                        } else {
                            Text text = new Text(item);
                            text.setStyle("-fx-text-alignment:left;");
                            text.wrappingWidthProperty().bind(getTableColumn().widthProperty());
                            setGraphic(text);
                        }
                    }
                });
                clm.getColumns().add(clmDoctor);
            }

            tblSchedule.getColumns().add(clm);

        }

        TableColumn<Map, String> clmStatic = new TableColumn<>("Static");
        TableColumn<Map, String> joubert = new TableColumn<>("Joubert L");
        keys.add("Joubert L");
        joubert.setCellFactory(TextFieldTableCell.forTableColumn());
        joubert.setCellValueFactory(new MapValueFactory<>("static"));
        joubert.setEditable(true);
        clmStatic.getColumns().add(joubert);
        tblSchedule.getColumns().add(clmStatic);

        for (int i = 0; i < 3; i++) {
            TableColumn<Map, String> clmCall = new TableColumn<>("Call " + (i + 1));
            clmCall.setCellValueFactory(new MapValueFactory<>("call" + (i + 1)));
            tblSchedule.getColumns().add(clmCall);
        }

        for (int i = 0; i < 5; i++) {
            TableColumn<Map, String> clmLoc = new TableColumn<>("Loc " + (i + 1));
            clmLoc.setCellValueFactory(new MapValueFactory<>("loc" + (i + 1)));
            tblSchedule.getColumns().add(clmLoc);
        }

        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        for (int i = 0; i < LISTS; i++) {
            var item = new HashMap<String, String>();
            item.put("List", lists.get(i));
            for (int j = 0; j < MODULES; j++) {
                for (var d : moduleMap.get(j + 1))
                    item.put(doctorNames.get(d), matrix[j][i]);
            }
            items.add(item);
        }

        tblSchedule.getItems().addAll(items);

        var surgeonLeaveJson = SurgeonLeave.refresh();

        LocalDate currentStart = dateRange.get().getKey().plusWeeks(scheduleOffset);
        LocalDate currentEnd = currentStart.plusDays(5);
        var day = currentStart.getDayOfMonth();

        int idx = 0;
        for (var j = 0; j < lists.size(); j++) {
            if (lists.get(j).contains("\n"))
                lists.set(j, lists.get(j).substring(lists.get(j).indexOf('\n') + 1));
            lists.set(j, currentStart.plusDays(idx).getDayOfMonth() + "\n" + lists.get(j));
            idx += j % 2;
        }

        for (var i = 0; i < tblSchedule.getItems().size(); i++) {
            tblSchedule.getItems().get(i).put("List", lists.get(i));
        }

        for (var item : tblSchedule.getItems()) {
            for (int i = 0; i < 3; i++) {
                item.put("call" + (i + 1), "");
                keys.add("call" + (i + 1));
            }
            for (int i = 0; i < 5; i++) {
                item.put("loc" + (i + 1), "");
                keys.add("loc" + (i + 1));
            }
        }

        for (var leave : surgeonLeaveJson) {
            // name, surname, start, end
            String name = leave.getString("name");
            String surname = leave.getString("surname");
            LocalDate start = LocalDate.parse(leave.getString("start").split("T")[0]);
            LocalDate end = LocalDate.parse(leave.getString("end").split("T")[0]);

            List<Pair<Map, String>> itemsToRemove = new ArrayList<>();
            for (var item : tblSchedule.getItems()) {
                for (var key : item.keySet()) {
                    var value = (String)item.get(key);
                    if (value != null) {

                        var fullName = (name.equals(" ")) ? surname : "%s %s".formatted(name, surname);
                        var valueFullName = (
                                value.startsWith("LH") || value.startsWith("DH")) ? value.substring(3) :
                                value.startsWith("DCL") ? value.substring(4) : value;
                        if (value.contains(surname)) {
                            var itemDate = currentStart.plusDays((tblSchedule.getItems().indexOf(item) / 2));
                            if (!itemDate.isAfter(end) && !itemDate.isBefore(start)) {
                                Logger.getInstance().debug("To remove: %s(%s)".formatted(fullName, key));
                                itemsToRemove.add(new Pair<>(item, (String) key));
                            }
                        }

                    }
                }
            }

            for (var item : itemsToRemove) {
                item.getKey().put(item.getValue(), "OFF");
            }

            tblSchedule.refresh();
            var rosterPeriod = dateRange.get().getKey().until(dateRange.get().getValue());
            maxWeeks = rosterPeriod.getMonths() * 4 + Math.round(rosterPeriod.getDays() / 7.0f);

        }

        var i = currentStart;

        while (i.isBefore(currentEnd) || i.isEqual(currentEnd)) {
            var intervalLength = currentStart.until(i).getDays();

            Map<String, String>[] dayitems = new Map[] { tblSchedule.getItems().get(intervalLength * 2),
                                                        tblSchedule.getItems().get(intervalLength * 2 + 1) };
            var freeSlots = RosterUtils.getFreeSlots(dayitems);
            for (var leave : doctorLeave) {

                if ((i.isAfter(leave.getStartDate()) || i.isEqual(leave.getStartDate())
                    ) && (i.isBefore(leave.getEndDate()) || i.isEqual(leave.getEndDate()))) {

                    // OVERLAP!!!
                    // If this code executes, it means that we have a doctor with leave on the day of month 'i'.

                    var keys = dayitems[0].keySet();
                    for (var key : keys) {
                        var value = dayitems[0].get(key);
                        var nextDayValue = dayitems[1].get(key);

                        if ("%s %s".formatted(leave.getDoctorSurname(), leave.getDoctorName()).equals(key)) {

                            if (!dayitems[0].get(key).contains("#"))
                                dayitems[0].put(key, "#" + dayitems[0].get(key));

                            if (!dayitems[1].get(key).contains("#"))
                                dayitems[1].put(key, "#" + dayitems[1].get(key));

//                            for (int k = 0; k < freeSlots.size(); k++) {
//                                var slot = freeSlots.get(k);
//                                var item = slot.getKey();
//                                var kv = slot.getValue();
//
//                                if (item.equals(dayitems[0])) {
//
//                                    if (!(value.contains("OFF") && nextDayValue.contains("OFF")) && !value.contains("#")
//                                            && !nextDayValue.contains("#") && !value.contains("$")
//                                            && !nextDayValue.contains("$")) {
//                                        dayitems[0].put(kv.getKey(), "$" + value.replace("#", ""));
//                                        dayitems[0].put(key, "#");
//
//                                        dayitems[1].put(kv.getKey(), "$" + nextDayValue);
//                                        dayitems[1].put(key, "#");
//
//                                        freeSlots.remove(slot);
//                                        freeSlots = RosterUtils.getFreeSlots(dayitems);
//                                        break;
//                                    }
//
//                                }
//
//                            }

                        }

                    }
                }

            }

            i = i.plusDays(1);
        }

        tblSchedule.refresh();

    }



    @SuppressWarnings("unchecked")
    public void mnuSaveSchedule(ActionEvent actionEvent) {

        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        chooser.getExtensionFilters().add(filter);

        VBox vbox = new VBox(new Text("Save"));
        vbox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vbox);
        Stage stage = new Stage();
        stage.initOwner(Schyfts.currentStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Save as");
        stage.setScene(scene);

        File file = chooser.showSaveDialog(stage);
        List<String> lines = new ArrayList<>();

        for (Map<String, String> item : tblSchedule.getItems()) {

            StringBuilder builder = new StringBuilder();
            for (var key : keys) {
                if (key.equals("List")) {
                    builder.append(item.get(key).replace('\n', ' ')).append(',');
                } else {
                    builder.append(item.get(key)).append(',');
                }
            }
            builder.deleteCharAt(builder.length() - 1);
            lines.add(builder.toString());

        }

        StringBuilder builder = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        for (var clm : tblSchedule.getColumns()) {
            builder.append(clm.getText()).append(',');
            builder.append(",".repeat(Math.max(0, clm.getColumns().size() - 1)));
            for (int i = 0; i < clm.getColumns().size(); i++) {
                builder2.append(clm.getColumns().get(i).getText()).append(',');
            }
            if (clm.getColumns().size() == 0)
                builder2.append(',');
        }

        builder.deleteCharAt(builder.length() - 1);
        builder2.deleteCharAt(builder.length() - 1);
        lines.add(0, builder.toString());
        lines.add(1, builder2.toString());

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (var line : lines) {
                line = line.replace("null", "");
                writer.write(line);
                writer.flush();
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }
    }

    public void btnPrevClick(ActionEvent actionEvent) {
        if (tabSchedule.isDisabled())
            return;
        if (scheduleOffset <= 0)
            return;
        scheduleOffset--;
        generateSchedule();
    }

    public void btnNextClick(ActionEvent actionEvent) {
        if (tabSchedule.isDisabled())
            return;
        if (scheduleOffset >= maxWeeks)
            return;
        scheduleOffset++;
        generateSchedule();
    }

    public void mnuPrintClick(ActionEvent actionEvent) {
        tblSchedule.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        PrinterJob job = PrinterJob.createPrinterJob(Printer.getDefaultPrinter());
        if (job.showPrintDialog(primaryStage.getOwner())) {
            job.printPage(Printer.getDefaultPrinter()
                    .createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT),
                    tblSchedule);
            job.endJob();
        }
        primaryStage.setResizable(true);
        primaryStage.setHeight(primaryStage.getMaxHeight());
        primaryStage.setWidth(primaryStage.getMaxWidth());
        tblSchedule.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

}
