package com.codelog.schyfts;

import com.codelog.clogg.LogEventBuffer;
import com.codelog.clogg.Logger;
import com.codelog.clogg.LogWriterFactory;
import com.codelog.schyfts.util.FileUtils;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static JSONObject config;
    public static String logFile;

    public static void main(String[] args) {
        config = null;
        try {
            if (Files.exists(Path.of("config.json")))
                config = FileUtils.readJSONFile("config.json");
            else
                config = FileUtils.readJSONResource("defaults.json");
        } catch (IOException e) {
            Logger.getInstance().error("Couldn't load config!");
            Logger.getInstance().exception(e);
            System.exit(-1);
        }

        String[] props = {
                "os.name",
                "os.arch",
                "os.version",
                "java.vendor",
                "java.vm.name",
                "java.version"
        };

        StringBuilder builder = new StringBuilder();
        for (String prop : props) {
            builder.append('\t').append("\"").append(prop).append("\" = ");
            builder.append(System.getProperty(prop)).append('\n');
        }

        Logger.getInstance().addLogEventSubscriber(LogWriterFactory.createPrintStreamLogWriter());
        Log.logBuffer = new LogEventBuffer();
        Logger.getInstance().addLogEventSubscriber(Log.logBuffer);
        try {
            Logger.getInstance().addLogEventSubscriber(LogWriterFactory.createFileLogWriter("schyfts"));
        } catch (IOException e) {
            Logger.getInstance().error("Couldn't open file stream writer");
            Logger.getInstance().exception(e);
        }
        Logger.getInstance().info("System information: \n" + builder.toString());

        try {
            Schyfts.main(args);
        } catch (Exception e) {
            Logger.getInstance().exception(e);
        }
    }
}
