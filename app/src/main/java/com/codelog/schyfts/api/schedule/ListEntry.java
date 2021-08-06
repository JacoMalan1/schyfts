package com.codelog.schyfts.api.schedule;

public class ListEntry {

    private boolean leave;
    private boolean surgeonLeave;
    private String contents;

    public ListEntry(String contents, boolean leave, boolean surgeonLeave) {
        this.leave = leave;
        this.surgeonLeave = surgeonLeave;
        this.contents = contents;
    }

    public ListEntry(String contents) {
        this(contents, false, false);
    }
}
