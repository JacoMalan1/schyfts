package com.codelog.schyfts.logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Logger {
    private static Logger instance;
    private List<String> messages;
    private int logLevel;

    private Logger() {
        logLevel = LogLevel.Debug.ordinal();
        messages = new ArrayList<>();
    }

    public static Logger getInstance() {
        if (instance == null)
            instance = new Logger();
        return instance;
    }

    public void setLogLevel(int logLevel) { this.logLevel = logLevel; }
    public int getLogLevel() { return logLevel; }

    public void writeToFile(String logName) throws IOException {

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        Date date = Date.from(Instant.now());

        int num = 0;
        boolean _result = new File("logs").mkdir();
        String originalName = logName;
        logName = String.format("logs/%s_%s_%s.log", originalName, formatter.format(date), num);
        while (Files.exists(Path.of(logName))) {
            num++;
            logName = String.format("logs/%s_%s_%s.log", originalName, formatter.format(date), num);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(logName));
        for (String m : messages)
            writer.write(m);
        writer.close();

    }

    private void log(String message, LogLevel logLevel, PrintStream stream) {

        if (logLevel.ordinal() < this.logLevel)
            return;

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = Date.from(Instant.now());
        String msgToSave = String.format("(%s)[%s]: %s\n", formatter.format(date), logLevel.name(), message);

        messages.add(msgToSave);
        stream.print(msgToSave);

    }

    private void log(String message, LogLevel logLevel) { log(message, logLevel, System.out); }

    public void info(String message) {
        log(message, LogLevel.Info);
    }

    public void debug(String message) {
        log(message, LogLevel.Debug);
    }

    public void error(String message) {
        log(message, LogLevel.Error, System.err);
    }

    public void warn(String message) {
        log(message, LogLevel.Warn);
    }
}
