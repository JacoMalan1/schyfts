package com.codelog.schyfts.api;

import com.codelog.schyfts.Reference;
import com.codelog.schyfts.util.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Doctor {

    private int id;
    private String shortcode;
    private String cellphone;
    private String name;
    private String surname;

    public static List<Doctor> getAllDoctors() {
        List<Doctor> results = new ArrayList<>();

        try {
            Request req = new Request(Reference.API_URL + "getAllDoctors");
            JSONObject body = new JSONObject();
            body.put("token", UserContext.getInstance().getCurrentUser().getToken());
            req.setBody(body);
            req.sendRequest();
            JSONObject res = req.getResponse();

            if (res.get("status").equals("ok")) {
                JSONArray jsonResults = res.getJSONArray("results");
                for (int i = 0; i < jsonResults.length(); i++) {

                    JSONObject result = jsonResults.getJSONObject(i);
                    results.add(new Doctor(
                            result.getInt("id"),
                            result.getString("shortcode"),
                            result.getString("cellphone"),
                            result.getString("name"),
                            result.getString("surname")
                    ));

                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }

        return results;
    }

    public static List<Doctor> searchDoctors(String surname) {

        List<Doctor> results = new ArrayList<>();

        try {
            Request req = new Request(Reference.API_URL + "getDoctor");
            JSONObject body = new JSONObject();
            body.put("surname", surname);
            body.put("token", UserContext.getInstance().getCurrentUser().getToken());
            req.setBody(body);
            req.sendRequest();
            JSONObject res = req.getResponse();

            if (res.get("status").equals("ok")) {
                JSONArray jsonResults = res.getJSONArray("results");
                for (int i = 0; i < jsonResults.length(); i++) {

                    JSONObject result = jsonResults.getJSONObject(i);
                    results.add(new Doctor(
                            result.getInt("id"),
                            result.getString("shortcode"),
                            result.getString("cellphone"),
                            result.getString("name"),
                            result.getString("surname")
                    ));

                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }

        return results;

    }

    public Doctor(int id, String shortcode, String cellphone, String name, String surname) {

        this.id = id;
        this.shortcode = shortcode;
        this.cellphone = cellphone;
        this.name = name;
        this.surname = surname;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
