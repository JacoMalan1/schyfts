package com.codelog.schyfts.api.schedule;

public class DayList {

    private String contents;
    private boolean leave;
    private boolean surgeonLeave;

    public DayList(String contents, boolean leave, boolean surgeonLeave) {
        this.contents = contents;
        this.leave = leave;
        this.surgeonLeave = surgeonLeave;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isLeave() {
        return leave;
    }

    public void setLeave(boolean leave) {
        this.leave = leave;
    }

    public boolean isSurgeonLeave() {
        return surgeonLeave;
    }

    public void setSurgeonLeave(boolean surgeonLeave) {
        this.surgeonLeave = surgeonLeave;
    }
}
