package com.codelog.schyfts.api;

import java.io.IOException;

public class ConfigurationProvider {
    public static String getAPIVariable(String key) throws APIException, IOException {
        var req = APIRequestFactory.createGetSetting();
        var res = req.send(key);
        return res.getJSONObject("result").getString("value");
    }
}
