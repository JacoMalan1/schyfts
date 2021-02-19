package com.codelog.schyfts.api;

import org.json.JSONObject;

public class APIException extends Exception {
    private JSONObject apiResponse;
    public APIException(String message, JSONObject apiResponse) {
        super(message);
        this.apiResponse = apiResponse;
    }

    @Override
    public String getMessage() {
        return String.format("%s\nAPI response:\n%s", super.getMessage(), apiResponse.toString(4));
    }
}
