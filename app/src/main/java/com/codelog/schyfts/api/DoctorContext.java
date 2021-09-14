package com.codelog.schyfts.api;

import java.util.List;

public class DoctorContext {

    private static DoctorContext instance;
    private List<Doctor> doctors;

    private DoctorContext() {
        doctors = Doctor.getAllDoctors(true);
    }

    public static DoctorContext getInstance() {
        instance = (instance == null) ? new DoctorContext() : instance;
        return instance;
    }

    public void refresh() {
        doctors = Doctor.getAllDoctors(true);
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }
}
