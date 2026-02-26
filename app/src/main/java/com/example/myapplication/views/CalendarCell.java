package com.example.myapplication.views;

import java.util.List;

public class CalendarCell {
    private int day;
    private boolean isSelected;
    private boolean isFuture;

    // Abstracted data for the MiniPieChartView
    private List<Float> durations;
    private List<Integer> types; // 0 = Still, 1 = Movement, 2 = Remaining

    public CalendarCell(int day, boolean isSelected, boolean isFuture, List<Float> durations, List<Integer> types) {
        this.day = day;
        this.isSelected = isSelected;
        this.isFuture = isFuture;
        this.durations = durations;
        this.types = types;
    }

    public int getDay() { return day; }
    public boolean isSelected() { return isSelected; }
    public boolean isFuture() { return isFuture; }
    public List<Float> getDurations() { return durations; }
    public List<Integer> getTypes() { return types; }
}