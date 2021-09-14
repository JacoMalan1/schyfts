package com.codelog.schyfts.api.schedule;

import com.codelog.schyfts.api.Doctor;
import java.util.ArrayList;
import java.util.List;

public class Module {

    private List<Doctor> assignedDoctors;

    public Module(List<Doctor> assignedDoctors) {
        this.assignedDoctors = new ArrayList<>(assignedDoctors);
    }

    public Module(Doctor... doctors) {
        this(List.of(doctors));
    }

    public List<Doctor> getAssignedDoctors() {
        return assignedDoctors;
    }

}
