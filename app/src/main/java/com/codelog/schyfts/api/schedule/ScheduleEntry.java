package com.codelog.schyfts.api.schedule;

import com.codelog.schyfts.api.matrix.SlotType;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScheduleEntry {

    private final Map<Module, DayList> mainSchedule;
    private final Map<Integer, String> callSchedule;
    private final Map<Integer, String> locumSchedule;
    private LocalDate date;
    private SlotType type;

    public ScheduleEntry(LocalDate date, SlotType type) {
        this.date = date;
        this.type = type;
        mainSchedule = new HashMap<>();
        callSchedule = new HashMap<>();
        locumSchedule = new HashMap<>();
    }

    public String getSlotName() {
        return "%d %s %s".formatted(
                date.getDayOfMonth(),
                date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                (type == SlotType.WEEKEND) ? "" : type.name()
        );
    }

    public Map<Module, DayList> getMainSchedule() {
        return mainSchedule;
    }

    public Map<Integer, String> getCallSchedule() {
        return callSchedule;
    }

    public Map<Integer, String> getLocumSchedule() {
        return locumSchedule;
    }

    public LocalDate getDate() {
        return date;
    }

    public SlotType getType() {
        return type;
    }
}
