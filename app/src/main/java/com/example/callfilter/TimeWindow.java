package com.example.callfilter;

public class TimeWindow {
    private final int startMinutes;
    private final int endMinutes;

    public TimeWindow(int startMinutes, int endMinutes) {
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
    }

    public int getStartMinutes() {
        return startMinutes;
    }

    public int getEndMinutes() {
        return endMinutes;
    }
}
