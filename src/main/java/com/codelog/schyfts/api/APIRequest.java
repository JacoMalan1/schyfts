package com.codelog.schyfts.api;

import com.codelog.schyfts.Reference;
import com.codelog.schyfts.util.Request;
import org.json.JSONObject;

import java.io.IOException;

public class APIRequest {

    private String method;
    private JSONObject body;
    private String[] params;
    private boolean authenticate;

    public APIRequest(String method, boolean authenticate, String... params) throws IOException {

        this.method = method;
        this.params = params;
        this.authenticate = authenticate;

    }

    public JSONObject send(Object... values) throws IllegalArgumentException, IOException, APIException {

        if (params.length != values.length)
            throw new IllegalArgumentException("Values don't match parameters");

        body = new JSONObject();
        if (authenticate)
            body.put("token", UserContext.getInstance().getCurrentUser().getToken());
        for (int i = 0; i < values.length; i++)
            body.put(params[i], values[i]);

        Request req = new Request(Reference.API_URL + method);
        req.setBody(body);
        req.sendRequest();
        var response = req.getResponse();

        if (!response.getString("status").equals("ok"))
            throw new APIException(response.getString("message"), response);

        return response;

    }

}
