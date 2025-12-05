package com.example.offdutycallfilter;

import java.util.List;

public class DaySchedule {
    private final int dayOfWeek;
    private final List<TimeWindow> windows;

    public DaySchedule(int dayOfWeek, List<TimeWindow> windows) {
        this.dayOfWeek = dayOfWeek;
        this.windows = windows;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public List<TimeWindow> getWindows() {
        return windows;
    }
}
