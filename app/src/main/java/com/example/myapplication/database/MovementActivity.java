package com.example.myapplication.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "movement_activities")
public class MovementActivity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String activityType;
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
    private Date startTimeDate;
    private Date endTimeDate;

    // Default constructor for Room
    public MovementActivity() {}

    // Convenience constructor for starting an activity
    @Ignore
    public MovementActivity(String activityType, Double startLat, Double startLng, Date startTimeDate) {
        this.activityType = activityType;
        this.startLat = startLat;
        this.startLng = startLng;
        this.startTimeDate = startTimeDate;
    }

    // Convenience constructor for replacing still with movement
    @Ignore
    public MovementActivity(String activityType, Double startLat, Double startLng, Double endLat, Double endLng, Date startTimeDate, Date endTimeDate) {
        this.activityType = activityType;
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
        this.startTimeDate = startTimeDate;
        this.endTimeDate = endTimeDate;
    }

    // Getters
    public long getId() { return id; }
    public String getActivityType() { return activityType; }
    public Double getStartLat() { return startLat; }
    public Double getStartLng() { return startLng; }
    public Double getEndLat() { return endLat; }
    public Double getEndLng() { return endLng; }
    public Date getStartTimeDate() { return startTimeDate; }
    public Date getEndTimeDate() { return endTimeDate; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public void setStartLat(Double startLat) { this.startLat = startLat; }
    public void setStartLng(Double startLng) { this.startLng = startLng; }
    public void setEndLat(Double endLat) { this.endLat = endLat; }
    public void setEndLng(Double endLng) { this.endLng = endLng; }
    public void setStartTimeDate(Date startTimeDate) { this.startTimeDate = startTimeDate; }
    public void setEndTimeDate(Date endTimeDate) { this.endTimeDate = endTimeDate; }
}