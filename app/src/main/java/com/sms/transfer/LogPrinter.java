package com.sms.transfer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class LogPrinter {

    private static final String TAG = LogPrinter.class.getSimpleName();
    private static Uri logFileUri;

    public static void saveLogToFile(String logMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10 and above
            saveLogToScopedStorage(logMessage);
        } else {
            // Use legacy file access for Android versions below 10
            saveLogToLegacyStorage(logMessage);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static void saveLogToScopedStorage(String logMessage) {
        logMessage = logMessage + "\n";
        ContentResolver resolver = App.getContext().getContentResolver();

        // If the log file URI is not initialized, create it
        if (logFileUri == null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "sms_transfer_logs.txt");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS); // Save to Downloads

            logFileUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
        }

        if (logFileUri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(logFileUri, "wa")) {  // "wa" mode to append to the file
                if (outputStream != null) {
                    outputStream.write(logMessage.getBytes());
                    outputStream.flush();
                    Log.d(TAG, "Log saved successfully to Downloads");
                }
            } catch (IOException e) {
                Log.e(TAG, "Error writing log to file: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Failed to create log file URI");
        }
    }

    private static void saveLogToLegacyStorage(String logMessage) {
        logMessage = logMessage + "\n";
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File logFile = new File(downloadsDir, "sms_transfer_logs.txt");

        try (FileOutputStream fos = new FileOutputStream(logFile, true);  // 'true' to append
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(logMessage + "\n");
        } catch (IOException e) {
            Log.e(TAG, "Error writing log to file: " + e.getMessage());
        }
    }

    // Initialize the log file once with the application context
    public static void initializeLogFile() {
        if (logFileUri == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Scoped Storage (Android 10 and above)
                ContentResolver resolver = App.getContext().getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "sms_transfer_logs.txt");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS); // Save to Downloads

                logFileUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
            } else {
                // For legacy storage, handle initialization
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File logFile = new File(downloadsDir, "sms_transfer_logs.txt");
                try {
                    if (!logFile.exists()) {
                        boolean created = logFile.createNewFile();
                        if (created) {
                            Log.d(TAG, "Log file created successfully in Downloads: " + logFile.getAbsolutePath());
                        } else {
                            Log.e(TAG, "Failed to create log file in Downloads.");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error initializing log file in Downloads: " + e.getMessage());
                }
            }
        }
    }

}
