package com.example.myapplication;

import androidx.room.TypeConverter;
import java.util.Date;

public class Converters {

    // Converts the Long from the database back into a Date object for your Java code
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    // Converts the Date object from your Java code into a Long for the database
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}