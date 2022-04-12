package com.codelog.schyfts.api;

import com.codelog.clogg.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveData {

    private final int doctorId;
    private final String doctorSurname;
    private final String doctorName;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public LeaveData(int doctorId, String doctorSurname, String doctorName, LocalDate startDate, LocalDate endDate) {
        this.doctorId = doctorId;
        this.doctorSurname = doctorSurname;
        this.doctorName = doctorName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static List<LeaveData> getSurgeonLeave() {
        var leave = new ArrayList<LeaveData>();

        try {
            APIRequest leaveReq = new APIRequest("getAllSurgeonLeave", true);
            var response = leaveReq.send();
            var results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++)
                leave.add(fromJSONObject(results.getJSONObject(i)));
            return leave;
        } catch (IOException | APIException e) {
            Logger.getInstance().exception(e);
            return null;
        }
    }

    public static List<LeaveData> getAllLeave() {
        var leave = new ArrayList<LeaveData>();

        try {
            APIRequest leaveReq = new APIRequest("getAllLeave", true);
            var response = leaveReq.send();
            var results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++)
                leave.add(fromJSONObject(results.getJSONObject(i)));
            return leave;
        } catch (IOException | APIException e) {
            Logger.getInstance().exception(e);
            return null;
        }
    }

    public static LeaveData fromJSONObject(JSONObject json) {
        var doctor = json.getJSONObject("doctor");
        var leaveData = json.getJSONObject("leaveData");
        var startStr = leaveData.getString("start");
        var endStr = leaveData.getString("end");

        return new LeaveData(
                doctor.getInt("id"),
                doctor.getString("surname"),
                doctor.getString("name"),
                LocalDate.parse(startStr.split("T")[0]),
                LocalDate.parse(endStr.split("T")[0])
        );
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getDoctorSurname() {
        return doctorSurname;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
