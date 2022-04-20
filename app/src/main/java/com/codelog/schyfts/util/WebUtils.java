package com.codelog.schyfts.util;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.ConfigContext;
import com.codelog.schyfts.Reference;
import javafx.scene.control.Alert;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebUtils {
    public static void browseURI(URI uri) {
        String browserPathKey = Reference.BROWSER_PATH_CONFIG_KEY;
        try {
            if (Desktop.isDesktopSupported() && !System.getProperty("os.name").equals("Linux")) {
                Desktop.getDesktop().browse(uri);
            } else {
                var config = ConfigContext.INSTANCE.getConfig();
                String browserExecutable = Reference.DEFAULT_BROWSER_PATH;
                if (config.has(browserPathKey))
                    browserExecutable = config.getString(browserPathKey);
                else {
                    config.put(browserPathKey, browserExecutable);
                    ConfigContext.INSTANCE.writeConfig();
                }
                if (!Files.exists(Path.of(browserExecutable))) {
                    Logger.getInstance().error("Couldn't find chromium executable!");
                    AlertFactory.showAlert(Alert.AlertType.ERROR,
                            String.format(
                                    "Executable \"%s\" not found!, " +
                                    "please set the correct path to your browser in config.json",
                                    browserExecutable
                            )
                    );
                } else {
                    Runtime rt = Runtime.getRuntime();
                    rt.exec(new String[] { browserExecutable, uri.toString() });
                }
            }
        } catch (IOException e) {
            Logger.getInstance().error(String.format("Couldn't open url: %s", uri.toASCIIString()));
            Logger.getInstance().exception(e);
        }
    }
}
