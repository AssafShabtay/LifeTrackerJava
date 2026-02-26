package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ServiceCompat;

import com.example.myapplication.database.ActivityDao;
import com.example.myapplication.database.ActivityDatabase;
import com.example.myapplication.database.MovementActivity;
import com.example.myapplication.database.StillLocation;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationService extends Service {

    public static final int NOTIFICATION_ID = 101;
    public static final String CHANNEL_ID = "LocationServiceChannel";
    public static final String TAG = "LocationService";

    public static final Set<Integer> MOVEMENT_ACTIVITIES = new HashSet<>(Arrays.asList(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.RUNNING,
            DetectedActivity.WALKING,
            DetectedActivity.ON_FOOT,
            DetectedActivity.ON_BICYCLE
    ));

    private int currentActivity = DetectedActivity.UNKNOWN;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation = null;
    private ActivityDao dao;
    private Long currentTrackingId = null;

    // Replaces CoroutineScope + Dispatchers.IO
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dao = ActivityDatabase.getDatabase(getApplicationContext()).activityDao();
        Log.d(TAG, "Service created");
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Shut down the executor to prevent memory leaks, similar to serviceScope.cancel()
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundService();

        if (intent != null && ActivityTransitionReceiver.ACTION_ACTIVITY_UPDATE.equals(intent.getAction())) {
            int activityType = intent.getIntExtra(ActivityTransitionReceiver.EXTRA_ACTIVITY_TYPE, DetectedActivity.UNKNOWN);
            int transitionType = intent.getIntExtra(ActivityTransitionReceiver.EXTRA_TRANSITION_TYPE, -1);

            // Launch on background thread
            executorService.execute(() -> handleActivityUpdate(activityType, transitionType));
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleActivityUpdate(int activityType, int transitionType) {
        boolean enteringActivity = (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER);

        if (activityType == DetectedActivity.UNKNOWN) {
            Log.d(TAG, "Ignoring unknown activity update (raw=" + activityType + ")");
            //TODO update that the app isn't recording when its unknown
            return;
        }

        if (enteringActivity) {
            currentActivity = activityType;

            if (activityType == DetectedActivity.STILL) {
                startStillTracking();
            } else if (MOVEMENT_ACTIVITIES.contains(activityType)) {
                startMovementTracking(activityType);
            }
        } else {
            if (activityType == DetectedActivity.STILL) {
                endStillTracking();
            } else if (MOVEMENT_ACTIVITIES.contains(activityType)) {
                endMovementTracking();
            }
            currentActivity = DetectedActivity.UNKNOWN;
        }
        updateNotification();
    }

    /**
     * One-shot location fetch.
     * Synchronous equivalent of your suspend function. Must be called from a background thread.
     */
    @SuppressLint("MissingPermission") // Ensure permissions are checked before starting service
    private Location getLocationOnce() {
        try {
            Task<Location> locationTask = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null);
            return Tasks.await(locationTask); // Blocks background thread until location is found
        } catch (Exception e) {
            Log.e(TAG, "getCurrentLocation failed", e);
            return null;
        }
    }

    //----------------------------------- Notifications ------------------------------------------

    private void startForegroundService() {
        Notification notification = buildNotification();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                );
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start foreground service (notification permission?)", t);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Location Tracking");
        channel.setShowBadge(false);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        String activityLabel = (currentActivity == DetectedActivity.UNKNOWN) ? "Unknown" : getActivityName(currentActivity);

        String contentText;
        if (currentTrackingId != null && currentActivity != DetectedActivity.UNKNOWN) {
            contentText = "Recording: " + activityLabel;
        } else {
            contentText = "Idle • Waiting for activity updates…";
        }

        Intent openAppIntent = new Intent(this, MainActivity.class);
        int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, flags);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @SuppressLint("MissingPermission") // Handle permission checks before calling
    private void updateNotification() {
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification());
        } catch (Throwable t) {
            Log.w(TAG, "Failed to update notification", t);
        }
    }

    // ----------------------------------- End and start activities -----------------------------------

    private void startStillTracking() {
        currentLocation = getLocationOnce();

        // Note: You may need to adjust the constructor depending on how you translate your Data Classes to Java
        Double lat = (currentLocation != null) ? currentLocation.getLatitude() : null;
        Double lng = (currentLocation != null) ? currentLocation.getLongitude() : null;
        StillLocation stillLocation = new StillLocation(lat, lng, new Date());

        try {
            currentTrackingId = dao.insertStillLocation(stillLocation);
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert start still location into database", e);
            currentTrackingId = null;
        }
    }

    private void endStillTracking() {
        currentLocation = getLocationOnce(); // Ending location
        if (currentTrackingId == null) return;

        long id = currentTrackingId;
        StillLocation still = dao.getStillLocationById(id);

        if (still == null) {
            Log.e(TAG, "StillLocation missing for id=" + id);
            currentTrackingId = null;
            return;
        }

        Double startLat = still.getLat();
        Double startLng = still.getLng();
        Date startTime = still.getStartTimeDate();
        Date endTime = new Date();

        if (startLat != null && startLng != null && currentLocation != null) {
            String resolvedActivityType = checkIfStillIsMovement(
                    startLat,
                    startLng,
                    startTime,
                    endTime,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );

            if ("Still".equalsIgnoreCase(resolvedActivityType)) {
                try {
                    dao.endStillLocation(id, endTime);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to end still location into database", e);
                    currentTrackingId = null;
                }
            } else {
                MovementActivity movement = new MovementActivity(
                        resolvedActivityType,
                        startLat,
                        startLng,
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        startTime,
                        endTime
                );
                try {
                    dao.replaceStillWithMovement(id, movement);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to replace still with movement location", e);
                    currentTrackingId = null;
                }
            }
        } else {
            try {
                dao.endStillLocation(id, endTime);
            } catch (Exception e) {
                Log.e(TAG, "Failed to end still location into database", e);
                currentTrackingId = null;
            }
        }
        currentTrackingId = null;
    }

    private String checkIfStillIsMovement(double startLatitude, double startLongitude, Date startTime, Date endTime, double endLatitude, double endLongitude) {
        float distanceMeters = distanceInMeters(startLatitude, startLongitude, endLatitude, endLongitude);

        long durationMillis = endTime.getTime() - startTime.getTime();
        float durationSeconds = (durationMillis > 0) ? durationMillis / 1000f : 0f;

        float speedMps = (durationSeconds > 0) ? (distanceMeters / durationSeconds) : 0f;

        if (distanceMeters < 100f || speedMps < 0.3f) {
            return "Still";
        } else if (speedMps < 2f) {
            return "Walking";
        } else if (speedMps < 15f) {
            return "Running";
        } else {
            return "Driving";
        }
    }

    private void startMovementTracking(int activityType) {
        currentLocation = getLocationOnce();

        Double lat = (currentLocation != null) ? currentLocation.getLatitude() : null;
        Double lng = (currentLocation != null) ? currentLocation.getLongitude() : null;

        MovementActivity movementActivity = new MovementActivity(
                getActivityName(activityType),
                lat,
                lng,
                new Date()
        );

        try {
            currentTrackingId = dao.insertMovementActivity(movementActivity);
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert movement location into database", e);
            currentTrackingId = null;
        }
    }

    private void endMovementTracking() {
        try {
            Double lat = (currentLocation != null) ? currentLocation.getLatitude() : null;
            Double lng = (currentLocation != null) ? currentLocation.getLongitude() : null;
            dao.endMovementActivity(currentTrackingId, lat, lng, new Date());
        } catch (Exception e) {
            Log.e(TAG, "Failed to end movement location into database", e);
            currentTrackingId = null;
        }
        currentTrackingId = null;
    }

    //----------------------------------- Tracking & Helpers -----------------------------------

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult result) {
            Location loc = result.getLastLocation();
            if (loc != null) {
                currentLocation = loc;
            }
        }
    };

    public float distanceInMeters(double startLat, double startLon, double endLat, double endLon) {
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLon, endLat, endLon, results);
        return results[0];
    }

    private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE: return "Driving";
            case DetectedActivity.ON_BICYCLE: return "Cycling";
            case DetectedActivity.ON_FOOT: return "On Foot";
            case DetectedActivity.RUNNING: return "Running";
            case DetectedActivity.WALKING: return "Walking";
            case DetectedActivity.STILL:
            case DetectedActivity.UNKNOWN: return "Still";
            default: return "Unknown";
        }
    }
}