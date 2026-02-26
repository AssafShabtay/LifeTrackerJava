package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityTransitionReceiver extends BroadcastReceiver {

    public static final String ACTION_ACTIVITY_UPDATE = "com.example.myapplication.ACTIVITY_UPDATE";
    public static final String EXTRA_ACTIVITY_TYPE = "activity_type";
    public static final String EXTRA_TRANSITION_TYPE = "transition_type";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ActivityTransitionResult.hasResult(intent)) {
            return;
        }

        ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
        if (result == null) {
            return;
        }

        handleActivityTransitions(context, result);

        // Assuming Logger is another class in your project that you'll also convert or access
        Logger.saveLog(context, "OnReceive");
    }

    private void handleActivityTransitions(Context context, ActivityTransitionResult result) {
        for (ActivityTransitionEvent event : result.getTransitionEvents()) {
            int activityType = handleUnknownActivity(event.getActivityType());
            boolean isEnter = (event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER);

            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    if (isEnter) handleDrivingStarted(context); else handleDrivingStopped(context);
                    break;
                case DetectedActivity.RUNNING:
                    if (isEnter) handleRunningStarted(context); else handleRunningStopped(context);
                    break;
                case DetectedActivity.WALKING:
                    if (isEnter) handleWalkingStarted(context); else handleWalkingStopped(context);
                    break;
                case DetectedActivity.ON_BICYCLE:
                    if (isEnter) handleCyclingStarted(context); else handleCyclingStopped(context);
                    break;
                case DetectedActivity.STILL:
                    if (isEnter) handleStillStarted(context); else handleStillStopped(context);
                    break;
                case DetectedActivity.ON_FOOT:
                    if (isEnter) handleOnFootStarted(context); else handleOnFootStopped(context);
                    break;
            }

            notifyLocationService(context, event, activityType);
        }
    }

    private void notifyLocationService(Context context, ActivityTransitionEvent event, int activityType) {
        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.setAction(ACTION_ACTIVITY_UPDATE);
        serviceIntent.putExtra(EXTRA_ACTIVITY_TYPE, activityType);
        serviceIntent.putExtra(EXTRA_TRANSITION_TYPE, event.getTransitionType());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void handleDrivingStarted(Context context) {}
    private void handleDrivingStopped(Context context) {}
    private void handleRunningStarted(Context context) {}
    private void handleRunningStopped(Context context) {}
    private void handleWalkingStarted(Context context) {}
    private void handleWalkingStopped(Context context) {}
    private void handleCyclingStarted(Context context) {}
    private void handleCyclingStopped(Context context) {}
    private void handleStillStarted(Context context) {}
    private void handleStillStopped(Context context) {}
    private void handleOnFootStarted(Context context) {}
    private void handleOnFootStopped(Context context) {}

    private int handleUnknownActivity(int activityType) {
        return (activityType == DetectedActivity.UNKNOWN) ? DetectedActivity.STILL : activityType;
    }
}