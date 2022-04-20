package com.codelog.schyfts

import com.codelog.schyfts.util.FileUtils
import org.json.JSONObject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import com.codelog.schyfts.util.KLoggerContext as Logger

object ConfigContext {
    private val config: JSONObject

    init {
        if (Files.exists(Path.of("config.json"))) {
            config = FileUtils.readJSONFile("config.json");
        } else {
            Logger.warn("No config file exists, creating...")
            config = FileUtils.readJSONResource("defaults.json");
            FileUtils.writeFile("config.json", config.toString(4))
        }
    }

    fun getConfig(): JSONObject = config
    fun writeConfig() {
        try {
            FileUtils.writeFile("config.json", config.toString(4));
        } catch (e: IOException) {
            Logger.error("Couldn't write config file!");
            Logger.exception(e);
        }
    }
}