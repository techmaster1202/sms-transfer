package com.sms.transfer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogPrinter {

    private static final String TAG = LogPrinter.class.getSimpleName();
    private static Uri logFileUri;

    private static final String LOG_FILE_URI_PREF_KEY = "log_file_uri";
    private static final String PREF_NAME = "LogFilePrefs";

    public static void print(String logMessage) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // Use MediaStore for Android 10 and above
//            saveLogToScopedStorage(logMessage);
//        } else {
//            // Use legacy file access for Android versions below 10
//            saveLogToLegacyStorage(logMessage);
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static void saveLogToScopedStorage(String logMessage) {
        logMessage = logMessage + "\n";
        ContentResolver resolver = App.getContext().getContentResolver();

        // If the log file URI is not initialized, create it
        if (logFileUri == null) {
            logFileUri = getLogFileUriFromPreferences(App.getContext());
            if (logFileUri == null) {
                // Create a new URI if it doesn't exist
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, getLogFileName());
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/sms-transfer/");

                logFileUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

                // Save the URI in SharedPreferences
                saveLogFileUriToPreferences(App.getContext(), logFileUri);
            }
        }

        // Write log to the file
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
        File smsTransferDir = new File(downloadsDir, "sms-transfer");

        // Create sms-transfer directory if it doesn't exist
        if (!smsTransferDir.exists() && !smsTransferDir.mkdirs()) {
            Log.e(TAG, "Failed to create sms-transfer directory.");
            return;
        }

        File logFile = new File(smsTransferDir, "sms_transfer_logs.txt");

        // Write log to the file in legacy storage
        try (FileOutputStream fos = new FileOutputStream(logFile, true);  // 'true' to append
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(logMessage + "\n");
        } catch (IOException e) {
            Log.e(TAG, "Error writing log to file: " + e.getMessage());
        }
    }

    // Initialize the log file URI once with the application context
    public static void initializeLogFile(Context context) {
        if (logFileUri == null) {
            logFileUri = getLogFileUriFromPreferences(context);
            if (logFileUri == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // For Scoped Storage (Android 10 and above)
                    ContentResolver resolver = context.getContentResolver();
                    ContentValues contentValues = new ContentValues();

                    // Create the sms-transfer directory in Downloads folder
                    String directoryPath = Environment.DIRECTORY_DOWNLOADS + "/sms-transfer/";
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, getLogFileName());
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, directoryPath); // Save to sms-transfer folder under Downloads

                    // Insert into MediaStore
                    logFileUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

                    // Save the URI in SharedPreferences
                    saveLogFileUriToPreferences(context, logFileUri);
                } else {
                    // For legacy storage, handle initialization
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File smsTransferDir = new File(downloadsDir, "sms-transfer");

                    // Create sms-transfer directory if it doesn't exist
                    if (!smsTransferDir.exists() && !smsTransferDir.mkdirs()) {
                        Log.e(TAG, "Failed to create sms-transfer directory.");
                        return;
                    }

                    // Create log file with date suffix
                    String logFileName = getLogFileName();
                    File logFile = new File(smsTransferDir, logFileName);
                    try {
                        if (!logFile.exists() && logFile.createNewFile()) {
                            Log.d(TAG, "Log file created successfully: " + logFile.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error initializing log file: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Helper method to generate log file name with date suffix
    private static String getLogFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        return "sms_transfer_logs_" + currentDate + ".txt";
    }

    // Save the log file URI to SharedPreferences
    private static void saveLogFileUriToPreferences(Context context, Uri logFileUri) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOG_FILE_URI_PREF_KEY, logFileUri.toString());
        editor.apply();
    }

    // Retrieve the log file URI from SharedPreferences
    private static Uri getLogFileUriFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String uriString = sharedPreferences.getString(LOG_FILE_URI_PREF_KEY, null);
        if (uriString != null) {
            return Uri.parse(uriString);
        }
        return null;
    }
}
