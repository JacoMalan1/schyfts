package com.codelog.schyfts.api;

public class RosterEntry {

    private String firstName;
    private String lastName;

    public RosterEntry(String p1, String p2) {
        firstName = p1;
        lastName = p2;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
