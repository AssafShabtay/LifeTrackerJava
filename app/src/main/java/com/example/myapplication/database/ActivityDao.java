package com.example.myapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.Date;
import java.util.List;

@Dao
public interface ActivityDao {

    @Insert
    long insertStillLocation(StillLocation stillLocation);

    @Query("DELETE FROM still_locations WHERE id = :id")
    void deleteStillLocation(long id);

    @Query("UPDATE still_locations SET endTimeDate = :endTimeDate WHERE id = :id")
    void endStillLocation(Long id, Date endTimeDate);

    @Query("UPDATE still_locations SET endTimeDate = :endTimeDate WHERE id = :id")
    void updateStillEndTime(long id, Date endTimeDate);

    @Query("SELECT * FROM still_locations WHERE id = :id LIMIT 1")
    StillLocation getStillLocationById(long id);

    @Insert
    long insertMovementActivity(MovementActivity movementActivity);

    @Query("DELETE FROM movement_activities WHERE id = :id")
    void deleteMovementActivity(long id);

    @Query("UPDATE movement_activities SET endLat = :endLatitude, endLng = :endLongitude, endTimeDate = :endTimeDate WHERE id = :id")
    void endMovementActivity(Long id, Double endLatitude, Double endLongitude, Date endTimeDate);

    @Query("UPDATE movement_activities SET endTimeDate = :endTimeDate WHERE id = :id")
    void updateMovementEndTime(long id, Date endTimeDate);

    @Query("SELECT * FROM still_locations WHERE (startTimeDate BETWEEN :start AND :end) OR (endTimeDate BETWEEN :start AND :end)")
    List<StillLocation> getStillForRange(Date start, Date end);

    @Query("SELECT * FROM movement_activities WHERE (startTimeDate BETWEEN :start AND :end) OR (endTimeDate BETWEEN :start AND :end)")
    List<MovementActivity> getMovementForRange(Date start, Date end);

    @Transaction
    default void replaceStillWithMovement(long id, MovementActivity movement) {
        deleteStillLocation(id);
        insertMovementActivity(movement);
    }
}