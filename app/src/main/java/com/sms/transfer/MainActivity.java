package com.sms.transfer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.widget.CompoundButton;
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
            Manifest.permission.READ_PHONE_NUMBERS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
//        LogPrinter.initializeLogFile(this);
        requestIgnoreBatteryOptimization(this);

        // Retrieve the saved switch state from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("appPreferences", MODE_PRIVATE);
        boolean isServiceSwitchOn = sharedPreferences.getBoolean("serviceSwitchState", false); // Default is false
        binding.serviceSwitch.setChecked(isServiceSwitchOn);

        // Optionally start the service if the switch is on
        if (isServiceSwitchOn) {
            Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
            startForegroundService(serviceIntent);
        }

        binding.serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the switch state to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("appPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("serviceSwitchState", isChecked);
                editor.apply();

                if (isChecked) {
                    // Start the foreground service when the switch is turned on
                    Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
                    startForegroundService(serviceIntent);
                    Toast.makeText(MainActivity.this, "Service started", Toast.LENGTH_SHORT).show();
                } else {
                    // Stop the foreground service when the switch is turned off
                    Intent serviceIntent = new Intent(MainActivity.this, MyService.class);
                    stopService(serviceIntent);
                    Toast.makeText(MainActivity.this, "Service stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void requestIgnoreBatteryOptimization(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            if (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            }
        }
    }

    private void checkAndRequestPermissions() {
        boolean permissionsNeeded = false;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded = true;
                break;
            }
        }

        if (permissionsNeeded) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        } else {
            LogPrinter.print("All permissions are already granted.");
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
                    LogPrinter.print("Permission " + permissions[i] + " was not granted");
                    Toast.makeText(this, "Permission " + permissions[i] + " was not granted", Toast.LENGTH_SHORT).show();
                }
            }
            if (allPermissionsGranted) {
                LogPrinter.print("Permissions granted successfully!");
                Toast.makeText(this, "Permissions granted successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
