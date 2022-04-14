package com.codelog.schyfts.api;

import com.codelog.clogg.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CallData {

    private final LocalDate date;
    private final int doctorID;
    private final String surname;
    private final String name;
    private final int state;
    private final int id;

    public CallData(int doctorID, int id, String surname, String name, LocalDate date, int state) {
        this.date = date;
        this.id = id;
        this.doctorID = doctorID;
        this.surname = surname;
        this.name = name;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public int getStateInt() { return state; }

    public String getState() {
        return (state != 0) ? "CALL ON" : "CALL OFF";
    }

    public static List<CallData> getAllCallData() {
        var doctors = Doctor.getAllDoctors();
        List<CallData> callData = new ArrayList<>();
        APIRequest req = new APIRequest("getAllCalls", true);

        try {
            var response = req.send();


            var results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                var result = results.getJSONObject(i);
                var dID = result.getInt("dID");
                var date = LocalDate.parse(result.getString("date").split("T")[0]);
                var state = result.getInt("value");
                var id = result.getInt("id");

                Doctor doctor = null;
                for (var d : doctors)
                    if (d.getId() == dID)
                        doctor = d;

                if (doctor != null) {
                    callData.add(new CallData(dID, id, doctor.getSurname(), doctor.getName(), date, state));
                }
            }
        } catch (APIException | IOException e) {
            Logger.getInstance().error("Couldn't refresh doctors!");
            Logger.getInstance().exception(e);
            return new ArrayList<>();
        }
        return callData;
    }
}
