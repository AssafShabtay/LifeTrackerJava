package com.example.myapplication.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "still_locations")
public class StillLocation {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private Double lat;
    private Double lng;
    private Date startTimeDate;
    private Date endTimeDate;
    private String wasSupposedToBeActivity;
    private String placeId;
    private String placeName;
    private String placeCategory;
    private String placeAddress;

    // Room requires a default constructor or an exact match
    public StillLocation() {}

    // Convenience constructor matching your typical usage in LocationService
    @Ignore
    public StillLocation(Double lat, Double lng, Date startTimeDate) {
        this.lat = lat;
        this.lng = lng;
        this.startTimeDate = startTimeDate;
    }

    // Getters
    public long getId() { return id; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public Date getStartTimeDate() { return startTimeDate; }
    public Date getEndTimeDate() { return endTimeDate; }
    public String getWasSupposedToBeActivity() { return wasSupposedToBeActivity; }
    public String getPlaceId() { return placeId; }
    public String getPlaceName() { return placeName; }
    public String getPlaceCategory() { return placeCategory; }
    public String getPlaceAddress() { return placeAddress; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setLat(Double lat) { this.lat = lat; }
    public void setLng(Double lng) { this.lng = lng; }
    public void setStartTimeDate(Date startTimeDate) { this.startTimeDate = startTimeDate; }
    public void setEndTimeDate(Date endTimeDate) { this.endTimeDate = endTimeDate; }
    public void setWasSupposedToBeActivity(String wasSupposedToBeActivity) { this.wasSupposedToBeActivity = wasSupposedToBeActivity; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public void setPlaceCategory(String placeCategory) { this.placeCategory = placeCategory; }
    public void setPlaceAddress(String placeAddress) { this.placeAddress = placeAddress; }
}