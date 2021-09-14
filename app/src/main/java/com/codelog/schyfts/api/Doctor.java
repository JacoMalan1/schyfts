package com.codelog.schyfts.api;

import com.codelog.schyfts.Reference;
import com.codelog.schyfts.util.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Doctor {

    private int id;
    private String shortcode;
    private String cellphone;
    private String name;
    private String surname;

    private static List<Doctor> doctors;

    public static List<Doctor> getAllDoctors() {
        return getAllDoctors(false);
    }

    protected static List<Doctor> getAllDoctors(boolean forceSync) {
        List<Doctor> results = new ArrayList<>();
        if (doctors != null && !forceSync) {
            return doctors;
        }

        try {
            APIRequest req = new APIRequest("getAllDoctors", true);
            var res = req.send();

            parseResults(results, res);
        } catch (IOException | APIException e) {
            e.printStackTrace(System.err);
            return new ArrayList<>();
        }

        doctors = new ArrayList<>(results);
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
                parseResults(results, res);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }

        return results;

    }

    private static void parseResults(List<Doctor> results, JSONObject res) {
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

    public static Doctor fromId(int id) {
        for (var d : doctors) {
            if (d.getId() == id)
                return d;
        }
        return null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id == doctor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
