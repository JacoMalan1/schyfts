package com.codelog.schyfts;

import com.codelog.clogg.LogEvent;
import com.codelog.clogg.LogEventBuffer;
import com.codelog.clogg.LogEventSubscriber;
import com.codelog.clogg.Logger;
import com.codelog.schyfts.util.HashUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
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
        Random r = new Random();
        byte[] rBytes = new byte[256];
        r.nextBytes(rBytes);
        String message = txtLog.getText() + Arrays.toString(rBytes);
        String digest = "";
        try {
            digest = HashUtil.hashString("SHA-256", message);
        } catch (NoSuchAlgorithmException e) {
            Logger.getInstance().exception(e);
            return;
        }

        var timestamp = Date.from(Instant.now());
        digest += timestamp.toString();
        Logger.getInstance().debug("Uploading log file (filename: %s)".formatted(digest));

    }

    public void btnSaveClick(ActionEvent actionEvent) {
    }
}
