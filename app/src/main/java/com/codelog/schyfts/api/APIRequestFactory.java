package com.codelog.schyfts.api;

public class APIRequestFactory {
    public static APIRequest createGetSetting() {
        return new APIRequest("getSetting", true, "key");
    }
}
