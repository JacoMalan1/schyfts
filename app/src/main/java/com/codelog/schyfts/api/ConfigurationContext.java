package com.codelog.schyfts.api;

import com.codelog.clogg.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationContext {

    Map<String, String> configMap;
    private static ConfigurationContext instance;

    private ConfigurationContext() {
        configMap = new HashMap<>();
    }

    public static ConfigurationContext getInstance() {
        instance = (instance == null) ? new ConfigurationContext() : instance;
        return instance;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public String getValue(String key) {
        String result = "";
        if (configMap.containsKey(key)) {
            result = configMap.get(key);
        } else {
            try {
                result = ConfigurationProvider.getAPIVariable(key);
            } catch (APIException | IOException e) {
                Logger.getInstance().error("Couldn't get config variable (key: %s)".formatted(key));
                Logger.getInstance().exception(e);
            }
        }
        return result;
    }
}
