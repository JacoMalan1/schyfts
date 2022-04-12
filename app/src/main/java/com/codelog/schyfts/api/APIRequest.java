package com.codelog.schyfts.api;

import com.codelog.schyfts.Reference;
import com.codelog.schyfts.util.Request;
import org.json.JSONObject;

import java.io.IOException;

public class APIRequest {

    private final String method;
    private JSONObject body;
    private final String[] params;
    private final boolean authenticate;

    /**
     * Constructs a new API Request.
     * @param method The name of the method to call. E.g. https://your-api.com/&lt;yourmethod&gt;
     * @param authenticate Whether or not to send the current user's authentication token.
     * @param params All of the parameters the API expects for the current method.
     */
    public APIRequest(String method, boolean authenticate, String... params) {

        this.method = method;
        this.params = params;
        this.authenticate = authenticate;

    }

    /**
     * Sends the constructed API Request.
     * @param values Values for the parameters specified in constructor
     * @return Returns a JSONObject representing the HTTP response from the API server.
     * @throws IllegalArgumentException Will throw IllegalArgumentException if the parameters don't match the values given.
     * @throws IOException Will throw an IOException if the HTTP Request throws.
     * @throws APIException Will throw an APIException if the API responds with an error code.
     */
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

        assert req.getResponseCode() == 200;

        return response;

    }

}
