package com.codelog.schyfts.api;

import java.time.LocalDate;

public class LeaveData {

    private int doctorId;
    private String doctorSurname;
    private String doctorName;
    private LocalDate startDate;
    private LocalDate endDate;

    public LeaveData(int doctorId, String doctorSurname, String doctorName, LocalDate startDate, LocalDate endDate) {
        this.doctorId = doctorId;
        this.doctorSurname = doctorSurname;
        this.doctorName = doctorName;
        this.startDate = startDate;
        this.endDate = endDate;
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
