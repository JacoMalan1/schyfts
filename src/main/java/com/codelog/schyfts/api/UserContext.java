package com.codelog.schyfts.api;

import java.util.ArrayList;
import java.util.List;

public class UserContext {

    private static UserContext instance;

    private List<User> users;
    private User currentUser;

    private UserContext() {
        users = new ArrayList<>();
    }

    public static UserContext getInstance() {
        instance = (instance == null) ? new UserContext() : instance;
        return instance;
    }

    public List<User> getUsers() { return users; }
    public void addUser(User user) {
        users.add(user);
        if (user.isLoggedIn()) {
            currentUser = user;
        }
    }
    public void removeUser(User user) { users.remove(user); }

    public User getCurrentUser() { return currentUser; }
}
