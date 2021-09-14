package com.codelog.schyfts.api;

public class APIRequestFactory {
    public static APIRequest createGetSetting() {
        return new APIRequest("getSetting", true, "key");
    }
    public static APIRequest createGetSharedModules() {
        return new APIRequest("getSharedModules", true);
    }
}
