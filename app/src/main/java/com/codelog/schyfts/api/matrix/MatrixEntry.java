package com.codelog.schyfts.api.matrix;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatrixEntry {

    private Map<Short, String> moduleMap;
    private String slot;
    private SlotType type;

    public MatrixEntry(String slot, SlotType type) {
        this.slot = slot;
        this.type = type;
        moduleMap = new HashMap<>();
    }

    public Map<Short, String> getModuleMap() {
        return moduleMap;
    }

    public String getSlot() { return slot; }

    public SlotType getSlotType() { return this.type; }

    public static ObservableList<MatrixEntry> fromCSVData(List<List<String>> csvData, String[] DAYS) {
        ObservableList<MatrixEntry> entries = FXCollections.observableArrayList();

        // Weekdays
        for (var i = 0; i < DAYS.length - 2; i++) {
            for (var j = 0; j < 2; j++) {
                SlotType type = SlotType.values()[j];
                var list = DAYS[i] + " " + type;
                entries.add(new MatrixEntry(list, type));
            }
        }

        // Weekends
        for (var i = DAYS.length - 2; i < DAYS.length; i++)
            entries.add(new MatrixEntry(DAYS[i], SlotType.WEEKEND));

        for (var i = 0; i < csvData.size(); i++) {
            var row = csvData.get(i);
            for (var j = 0; j < row.size(); j++) {
                entries.get(i).getModuleMap().put((short)(j + 1), row.get(j));
            }
        }
        return entries;
    }

}
