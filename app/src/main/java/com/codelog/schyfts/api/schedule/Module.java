package com.codelog.schyfts.api.schedule;

import com.codelog.schyfts.api.Doctor;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public enum ModuleType {
        DYNAMIC,
        STATIC
    }

    private ModuleType type;
    private short moduleNum;
    private List<Doctor> assignedDoctors;

    public Module(short moduleNum, List<Doctor> doctors) {
        this.moduleNum = moduleNum;
        this.assignedDoctors = new ArrayList<>(doctors);
    }

    public Module(short moduleNum, Doctor... doctors) {
        this(moduleNum, List.of(doctors));
    }

}
