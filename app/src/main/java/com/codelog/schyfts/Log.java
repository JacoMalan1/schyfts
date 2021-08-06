package com.codelog.schyfts;

import com.codelog.clogg.*;
import com.codelog.schyfts.google.StorageContext;
import com.codelog.schyfts.util.AlertFactory;
import com.codelog.schyfts.util.RandomUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.ResourceBundle;

public class Log implements LogEventSubscriber, Initializable {

    @FXML
    TextArea txtLog;

    private static boolean initialized = false;

    public static LogEventBuffer logBuffer;

    private void writeLogEvent(LogEvent logEvent) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = Date.from(logEvent.getTimeStamp());
        String output = String.format(
                "(%s)[%s]: %s\n",
                formatter.format(date),
                logEvent.getLogLevel().name(),
                logEvent.getMessage());
        txtLog.setText(txtLog.getText() + output);
    }

    @Override
    public void logEvent(LogEvent logEvent) {
        if (!initialized)
            return;

        for (int i = 0; i <= logBuffer.getUnhandledEvents().size(); i++) {
            var event = logBuffer.popEvent();
            writeLogEvent(event);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtLog.setEditable(false);
        initialized = true;
        Logger.getInstance().addLogEventSubscriber(this);
    }

    public void btnSendClick(ActionEvent actionEvent) {
        var timestamp = Date.from(Instant.now());
        var formatter = new SimpleDateFormat("yyyyMMdd");
        var fileName = formatter.format(timestamp);
        fileName += "_" + RandomUtil.getRandomString(3, "1234567890");
        fileName += ".log";

        var tmpDir = System.getProperty("java.io.tmpdir");
        var tmpFile = new File(tmpDir + FileSystems.getDefault().getSeparator() + fileName);
        try {
            var fileWriter = new FileWriter(tmpFile);
            LogWriter logWriter = new LogWriter(fileWriter);
            for (var event : logBuffer.getPastEvents())
                logWriter.logEvent(event);

            var bucket = StorageContext.getInstance().getStorage().get("nelanest-roster");
            bucket.create("logs/%s".formatted(fileName), new FileInputStream(tmpFile));
        } catch (IOException e) {
            Logger.getInstance().error(e.getMessage());
            Logger.getInstance().exception(e);
        }

        Logger.getInstance().debug("Uploaded log file (filename: %s)".formatted(fileName));
    }

    public void btnSaveClick(ActionEvent actionEvent) {
        AlertFactory.showAlert(Alert.AlertType.ERROR, "This feature hasn't been implemented yet!");
    }
}
