package com.example.myapplication;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {

    public static void saveLog(Context context, String message) {
        // Format the time
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logMessage = time + " - " + message + "\n";

        // Get reference to the file
        File file = new File(context.getFilesDir(), "activity_log.txt");

        // Append to file using try-with-resources (automatically closes the writer)
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}