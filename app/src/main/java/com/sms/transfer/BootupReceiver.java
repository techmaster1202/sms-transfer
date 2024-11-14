package com.sms.transfer;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            SharedPreferences sharedPreferences = context.getSharedPreferences("appPreferences", MODE_PRIVATE);
            boolean isServiceSwitchOn = sharedPreferences.getBoolean("serviceSwitchState", false);
            if (isServiceSwitchOn) {
                Intent serviceIntent = new Intent(context, MyService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }

        }
    }
}
