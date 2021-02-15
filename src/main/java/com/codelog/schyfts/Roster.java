package com.codelog.schyfts;

import com.codelog.schyfts.api.Doctor;
import com.codelog.schyfts.api.UserContext;
import com.codelog.schyfts.logging.Logger;
import com.codelog.schyfts.util.Request;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageOptions;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;

@SuppressWarnings({"rawtypes"})
public class Roster implements Initializable {

    private static int MODULES = 16;
    private static int LISTS = 14;
    private static int CALLS = 3;
    private static int LOCI = 4;
    private static int STATIC_COLUMNS = 1;
    private String[][] matrix;
    private static List<String> lists;
    public static Stage primaryStage;
    private List<Doctor> doctors;
    private static int STATIC_MODULES = 3;
    private Map<Integer, List<Integer>> moduleMap;
    private Map<Integer, String> doctorNames;
    private List<String> keys;
    private Bucket storageBucket;
    private Map<Integer, Integer> sharedModules;
    private int scheduleOffset;

    @FXML
    private TableView<Map> tblRoster;
    @FXML
    private TableView<Map> tblSchedule;
    @FXML
    private Tab tabMatrix;
    @FXML
    private Tab tabSchedule;
    @FXML
    private Spinner<Integer> spnOffset;

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
                if (split[j].equals("\"\""))
                    matrix[j][i] = "";
                else
                    matrix[j][i] = split[j];
            }

        }
    }

    public void loadModules() {

    }

    public void getDoctorNames() {

        doctorNames = new HashMap<>();

        var doctors = Doctor.getAllDoctors();
        for (var d : doctors)
            doctorNames.put(d.getId(), String.format("%s %s", d.getSurname(), d.getName()));

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

        sharedModules = getSharedModules();
        doctors = Doctor.getAllDoctors();

        scheduleOffset = 0;
        spnOffset.setValueFactory(new IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        spnOffset.setPromptText("Offset for schedule (in modules)");
        spnOffset.valueProperty().addListener((observable, oldValue, newValue) -> {
            scheduleOffset = newValue;
            mnuGenerateSchedule(null);
        });

        var stream = getClass().getClassLoader().getResourceAsStream("google/service-account.json");
        if (stream != null) {

            try {
                var credentials = GoogleCredentials.fromStream(stream);
                var storage = StorageOptions.newBuilder().setCredentials(credentials)
                        .setProjectId(Reference.GOOGLE_CLOUD_PROJECT_ID).build().getService();

                storageBucket = storage.get("nelanest-roster");

            } catch (IOException e) {
                Logger.getInstance().exception(e);
            }

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

        Thread thread = new Thread(this::getDoctorNames);
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

            Request req = new Request(Reference.API_URL + "getSharedModules");
            var body = new JSONObject();
            body.put("token", UserContext.getInstance().getCurrentUser().getToken());
            req.setBody(body);
            req.sendRequest();

            if (!req.getResponse().getString("status").equals("ok"))
                throw new IOException(req.getResponse().getString("message"));

            var results = req.getResponse().getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                for (String s : results.getJSONObject(i).getString("doctors").split(","))
                    sharedModules.put(Integer.parseInt(s), results.getJSONObject(i).getInt("moduleNum"));
            }

        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }
        return sharedModules;
    }

    public void constructModuleMap() {
        moduleMap = new HashMap<>();
        int currentModule = spnOffset.getValue() % MODULES;
        doctors.sort(Comparator.comparing(Doctor::getSurname));
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
                clmDoctor.setCellFactory(TextFieldTableCell.forTableColumn());
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

        for (int i = 0; i < CALLS; i++) {
            TableColumn<Map, String> clmCall = new TableColumn<>(String.format("Call %d", i + 1));
            clmCall.setEditable(true);
            clmCall.setCellFactory(TextFieldTableCell.forTableColumn());
            clmCall.setCellValueFactory(new MapValueFactory<>(String.format("call%d", i + 1)));
            keys.add(String.format("call%d", i + 1));
            tblSchedule.getColumns().add(clmCall);
        }

        for (int i = 0; i < LOCI; i++) {
            TableColumn<Map, String> clm = new TableColumn<>(String.format("Locum %d", i + 1));
            clm.setEditable(true);
            clm.setCellValueFactory(new MapValueFactory<>(String.format("locum%d", i + 1)));
            keys.add(String.format("locum%d", i + 1));
            clm.setCellFactory(TextFieldTableCell.forTableColumn());
            tblSchedule.getColumns().add(clm);
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
                builder.append(item.get(key)).append(',');
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
}
