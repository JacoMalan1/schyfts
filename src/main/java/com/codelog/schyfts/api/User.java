package com.codelog.schyfts.api;

import com.codelog.schyfts.Reference;
import com.codelog.schyfts.logging.Logger;
import com.codelog.schyfts.util.Request;
import jdk.jfr.Unsigned;
import org.json.JSONObject;

import java.io.IOException;

public class User {
    private final String username;
    private String id;
    private String email;
    private String token;
    private @Unsigned int permissionLevel;

    private boolean loggedIn;

    public String getToken() { return token; }

    public User(String username) {
        this.username = username;
        this.loggedIn = false;
    }

    public boolean isLoggedIn() { return loggedIn; }

    public String getUsername() { return username; }

    public boolean login(String password) {
        try {
            Request req = new Request(Reference.API_URL + "login");

            var body = new JSONObject();
            body.put("uname", username);
            body.put("pword", password);

            req.setBody(body);
            req.sendRequest();

            if (req.getResponse().get("status").equals("ok")) {
                token = req.getResponse().getString("token");
                this.loggedIn = true;
                UserContext.getInstance().addUser(this);
                return true;
            }
        } catch (IOException e) {
            Logger.getInstance().exception(e);
            return false;
        }
        return false;
    }

    public void register(String password, String email) {

    }

    public void changePassword(String currentPass, String newPass) {

    }
}
