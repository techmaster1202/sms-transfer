package com.sms.transfer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sms.transfer.databinding.ActivityMainBinding;


@RequiresApi(api = Build.VERSION_CODES.P)
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Request permissions only if they are not already granted
        checkAndRequestPermissions();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MySmsReceiver.subscribe((from, to, content) -> {
            binding.from.setText("SMS From: " + from);
            binding.to.setText("SMS To: " + to);
            binding.content.setText("Content: " + content);
        });
        LogPrinter.initializeLogFile();

    }

    private void checkAndRequestPermissions() {
        // List to keep track of permissions that need to be requested
        boolean permissionsNeeded = false;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded = true;
                break; // Exit loop if we found any permission that needs to be requested
            }
        }

        if (permissionsNeeded) {
            // Request permissions that are not yet granted
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        } else {
            // All permissions are already granted, proceed with the app functionality
            Toast.makeText(this, "All permissions are already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    Toast.makeText(this, "Permission " + permissions[i] + " was not granted", Toast.LENGTH_SHORT).show();
                }
            }
            if (allPermissionsGranted) {
                // Handle the case when all permissions are granted
                Toast.makeText(this, "Permissions granted successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
