package com.example.myapplication.database;

import android.content.Context;

import com.example.myapplication.Converters;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;



@Database(
        entities = {
                StillLocation.class,
                MovementActivity.class
        },
        version = 3,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class ActivityDatabase extends RoomDatabase {

    public abstract ActivityDao activityDao();

    private static volatile ActivityDatabase INSTANCE;

    public static ActivityDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ActivityDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ActivityDatabase.class,
                                    "activity_database"
                            )
                            .fallbackToDestructiveMigration(false)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}