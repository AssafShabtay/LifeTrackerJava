package com.example.myapplication;

import com.example.myapplication.PieType;

// Note: Replaced Compose Color and ImageVector with Android ints
public interface ActivityData {
    String getLabel();
    int getData();
    int getColor();
    Double getLat();
    Double getLng();
    Double getEndLat();
    Double getEndLng();
    String getDurationText();
    Integer getIconResId(); // Drawable Resource ID
    PieType getType();
    int getSelectedColor();
}