package com.codelog.schyfts.util;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Request {

    private URL url;
    private JSONObject body;
    private boolean sent;
    private int responseCode;
    private JSONObject response;

    private HttpURLConnection connection;

    public Request(String url, JSONObject body) throws MalformedURLException {
        this.url = new URL(url);
        sent = false;
        this.body = body;
    }

    public Request(String url) throws MalformedURLException {
        this(url, new JSONObject());
    }

    public void setBody(JSONObject body) {
        this.body = body;
    }

    public void sendRequest() throws IOException {
        if (sent)
            return;

        connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");

        String strBody = body.toString();
        connection.setDoOutput(true);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        writer.write(strBody);
        writer.flush();
        writer.close();

        responseCode = connection.getResponseCode();
        sent = true;

        StringBuilder builder = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while ((line = reader.readLine()) != null)
            builder.append(line);

        response = new JSONObject(builder.toString());

        connection.disconnect();
    }

    public JSONObject getResponse() { return response; }
    public int getResponseCode() { return responseCode; }

}
