package com.codelog.schyfts.api;

import com.codelog.schyfts.Reference;
import com.codelog.schyfts.logging.Logger;
import com.codelog.schyfts.util.Request;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User {
    private final String username;
    private String id;
    private String email;
    private String token;
    private int permissionLevel;

    private boolean loggedIn;

    public String getToken() { return token; }

    public User(String username, String email, String id, int permissionLevel) {
        this.username = username;
        this.email = email;
        this.id = id;
        this.loggedIn = false;
    }

    public User(String username) {
        this(username, "", "", Integer.MAX_VALUE);
    }

    public boolean isLoggedIn() { return loggedIn; }

    public int getPermissionLevel() { return permissionLevel; }

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
                permissionLevel = req.getResponse().getInt("permissionLevel");
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static List<User> getAllUsers() {
        var results = new ArrayList<User>();

        try {
            Request req = new Request(Reference.API_URL + "getAllUsers");
            var body = new JSONObject();
            body.put("token", UserContext.getInstance().getCurrentUser().getToken());
            req.setBody(body);
            req.sendRequest();

            if (!req.getResponse().getString("status").equals("ok"))
                throw new IOException(req.getResponse().getString("message"));

            var jsonResults = req.getResponse().getJSONArray("results");
            for (int i = 0; i < jsonResults.length(); i++) {

                var user = jsonResults.getJSONObject(i);
                results.add(new User(
                        user.getString("uName"),
                        user.getString("uEmail"),
                        String.valueOf(user.getInt("uID")),
                        user.getInt("uPerm")
                ));

            }

        } catch (IOException e) {
            Logger.getInstance().exception(e);
        }

        return results;

    }

}
