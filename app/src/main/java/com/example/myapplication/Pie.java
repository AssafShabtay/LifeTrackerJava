package com.example.myapplication;

import com.example.myapplication.ActivityData;
import com.example.myapplication.PieType;

public class Pie implements ActivityData {
    private String label;
    private int data;
    private int color;
    private Double lat;
    private Double lng;
    private Double endLat;
    private Double endLng;
    private String durationText;
    private Integer iconResId;
    private PieType type;
    private int selectedColor;
    private boolean clickable;

    public Pie(String label, int data, int color, Double lat, Double lng, Double endLat, Double endLng, String durationText, Integer iconResId, PieType type, int selectedColor, boolean clickable) {
        this.label = label;
        this.data = data;
        this.color = color;
        this.lat = lat;
        this.lng = lng;
        this.endLat = endLat;
        this.endLng = endLng;
        this.durationText = durationText;
        this.iconResId = iconResId;
        this.type = type;
        this.selectedColor = selectedColor;
        this.clickable = clickable;
    }

    // Standard getters mapping to the ActivityData interface
    @Override public String getLabel() { return label; }
    @Override public int getData() { return data; }
    @Override public int getColor() { return color; }
    @Override public Double getLat() { return lat; }
    @Override public Double getLng() { return lng; }
    @Override public Double getEndLat() { return endLat; }
    @Override public Double getEndLng() { return endLng; }
    @Override public String getDurationText() { return durationText; }
    @Override public Integer getIconResId() { return iconResId; }
    @Override public PieType getType() { return type; }
    @Override public int getSelectedColor() { return selectedColor; }
    public boolean isClickable() { return clickable; }

    // Setter for adjusting angles later
    public void setData(int data) { this.data = data; }
}