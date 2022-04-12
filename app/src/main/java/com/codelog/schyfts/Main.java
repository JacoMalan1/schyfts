package com.codelog.schyfts;

import com.codelog.clogg.LogEventBuffer;
import com.codelog.clogg.LogWriterFactory;
import com.codelog.clogg.Logger;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
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
        Logger.getInstance().info("System information: \n" + builder);
        Logger.getInstance().info("Schyfts version: " + Reference.VERSION_STRING);

        Logger.getInstance().info("Loading configuration...");
        if (ConfigContext.INSTANCE.getConfig().getBoolean("testing")) {
            Reference.API_URL = "https://testing-dot-schyfts.uc.r.appspot.com/";
        }
        Logger.getInstance().info("API URL: " + Reference.API_URL);

        try {
            Schyfts.main(args);
        } catch (Exception e) {
            Logger.getInstance().exception(e);
        }
    }
}
