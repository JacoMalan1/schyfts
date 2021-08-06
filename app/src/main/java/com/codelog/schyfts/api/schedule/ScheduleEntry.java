package com.codelog.schyfts.api.schedule;

import com.codelog.schyfts.api.matrix.ListType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleEntry {
    private String list;
    private ListType listType;
    private List<String> calls;
    private List<String> locums;
    private List<Module> modules;
    private Map<Module, ListEntry> moduleListMap;
    private LocalDate date;

    public ScheduleEntry(LocalDate date, String list, ListType listType, List<Module> modules) {
        this.list = date.getDayOfMonth() + " " + list;
        this.date = date;
        this.listType = listType;
        this.modules = modules;
        this.locums = new ArrayList<>(5);
        this.calls = new ArrayList<>(3);
        this.moduleListMap = new HashMap<>(17);
    }

    public String getList() {
        return list;
    }

    public ListType getListType() {
        return listType;
    }

    public List<String> getCalls() {
        return calls;
    }

    public List<String> getLocums() {
        return locums;
    }

    public List<Module> getModules() {
        return modules;
    }

    public Map<Module, ListEntry> getModuleListMap() {
        return moduleListMap;
    }
}
