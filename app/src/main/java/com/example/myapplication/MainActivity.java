package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.database.ActivityDao;
import com.example.myapplication.database.ActivityDatabase;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private View mainContentLayout;
    private View permissionLayout;
    private PieChartViewModel viewModel;

    // --- Permissions List ---
    private String[] getRequiredPermissions() {
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            perms.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        return perms.toArray(new String[0]);
    }

    // --- Permission Launcher ---
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    if (!entry.getValue()) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    onPermissionsGranted();
                } else {
                    handleDeniedPermissions(result);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind Views
        mainContentLayout = findViewById(R.id.mainContentLayout);
        permissionLayout = findViewById(R.id.permissionLayout);
        Button btnGrant = findViewById(R.id.btnGrantPermissions);
        Button btnSettings = findViewById(R.id.btnOpenSettings);
        Button btnInsertData = findViewById(R.id.btnInsertData);

        // Setup UI Listeners
        btnGrant.setOnClickListener(v -> requestPermissions());
        btnSettings.setOnClickListener(v -> openAppSettings());

        // Database & ViewModel Setup
        // Note: You need to implement the Database getter in Java or call the Kotlin object instance
        ActivityDao dao = ActivityDatabase.getDatabase(getApplicationContext()).activityDao();

        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                //noinspection unchecked
                return (T) new PieChartViewModel(dao);
            }
        }).get(PieChartViewModel.class);

        // Setup Main Screen Actions (Example: Insert Data)
        btnInsertData.setOnClickListener(v -> {
            // Calling the helper method (assuming it's converted to static Java or accessible Kotlin file)
            // com.example.myapplication.helpers.Helpers.insertExampleData(dao);
            Toast.makeText(this, "Inserting Data...", Toast.LENGTH_SHORT).show();
        });

        // Initial Check
        checkPermissionsAndInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check permissions when returning from Settings or background
        checkPermissionsAndInit();
    }

    private void checkPermissionsAndInit() {
        if (hasAllPermissions()) {
            onPermissionsGranted();
        } else {
            showPermissionScreen();
            // Optional: Request immediately on first run if desired
            // requestPermissions();
        }
    }

    private void onPermissionsGranted() {
        showMainContent();
        requestTransitions();
        startLocationService();
    }

    // --- UI Toggling ---
    private void showMainContent() {
        mainContentLayout.setVisibility(View.VISIBLE);
        permissionLayout.setVisibility(View.GONE);
    }

    private void showPermissionScreen() {
        mainContentLayout.setVisibility(View.GONE);
        permissionLayout.setVisibility(View.VISIBLE);
    }

    // --- Permission Logic ---
    private void requestPermissions() {
        permissionLauncher.launch(getRequiredPermissions());
    }

    private void handleDeniedPermissions(Map<String, Boolean> result) {
        List<String> deniedPermissions = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            if (!entry.getValue()) {
                deniedPermissions.add(entry.getKey());
            }
        }

        boolean shouldShowRationale = false;
        for (String perm : deniedPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                shouldShowRationale = true;
                break;
            }
        }

        if (shouldShowRationale) {
            Log.d("Permissions", "Should show rationale");
            showPermissionRationaleDialog();
        } else {
            Log.d("Permissions", "NOT Should show rationale (Permanently denied?)");
            showGoToSettingsDialog();
        }
    }

    private boolean hasAllPermissions() {
        for (String perm : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // --- Dialogs ---
    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission required")
                .setMessage("We need these permissions to use the app features. Please allow them.")
                .setPositiveButton("Allow", (dialog, which) -> requestPermissions())
                .setNegativeButton("Not now", (dialog, which) -> Log.d("Permissions", "Permissions denied"))
                .show();
    }

    private void showGoToSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Enable permissions in Settings")
                .setMessage("Permissions are denied permanently. Please enable them in Settings to continue.")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Cancel", (dialog, which) -> Log.d("Permissions", "Permissions denied"))
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    // --- Services & Transitions ---
    private void startLocationService() {
        Intent intent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void requestTransitions() {
        if (!hasAllPermissions()) {
            Log.w("ActivityRecognition", "Aborting requestTransitions: Permissions not fully granted.");
            return;
        }

        List<ActivityTransition> transitions = new ArrayList<>();
        int[] types = {
                DetectedActivity.STILL, DetectedActivity.WALKING, DetectedActivity.RUNNING,
                DetectedActivity.IN_VEHICLE, DetectedActivity.ON_BICYCLE, DetectedActivity.ON_FOOT
        };

        for (int type : types) {
            transitions.add(new ActivityTransition.Builder()
                    .setActivityType(type)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build());
            transitions.add(new ActivityTransition.Builder()
                    .setActivityType(type)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build());
        }

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);
        Intent intent = new Intent(this, ActivityTransitionReceiver.class);

        // Flags handling
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags);

        try {
            ActivityRecognition.getClient(this)
                    .requestActivityTransitionUpdates(request, pendingIntent)
                    .addOnFailureListener(e -> Log.e("ActivityRecognition", "Registration failed.", e));
        } catch (SecurityException e) {
            Log.e("ActivityRecognition", "SecurityException: Missing permission", e);
        }
    }
}