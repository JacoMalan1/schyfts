package com.codelog.schyfts.api;

import com.codelog.clogg.Logger;

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
            APIRequest req = new APIRequest("login", false, "uname", "pword");
            var response = req.send(username, password);

            if (response.get("status").equals("ok")) {
                token = response.getString("token");
                permissionLevel = response.getInt("permissionLevel");
                this.loggedIn = true;
                UserContext.getInstance().addUser(this);
                return true;
            }
        } catch (IOException | APIException e) {
            if (e instanceof APIException) {
                Logger.getInstance().debug("API Response: " +
                        ((APIException) e).getApiResponse().toString(4) + "\n");
            }
            Logger.getInstance().exception(e);
            return false;
        }
        return false;
    }

    public void register(String password, String email) {
        try {
            APIRequest req = new APIRequest("register", false, "uname", "pword", "email");
            req.send(username, password, email);
        } catch (IOException | APIException e) {
            Logger.getInstance().exception(e);
        }
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
            APIRequest req = new APIRequest("getAllUsers", true);
            var response = req.send();

            if (!response.getString("status").equals("ok"))
                throw new IOException(response.getString("message"));

            var jsonResults = response.getJSONArray("results");
            for (int i = 0; i < jsonResults.length(); i++) {

                var user = jsonResults.getJSONObject(i);
                results.add(new User(
                        user.getString("uName"),
                        user.getString("uEmail"),
                        String.valueOf(user.getInt("uID")),
                        user.getInt("uPerm")
                ));

            }

        } catch (IOException | APIException e) {
            Logger.getInstance().exception(e);
        }

        return results;

    }

}
