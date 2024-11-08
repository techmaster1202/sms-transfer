package com.sms.transfer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    private static final String TAG = MyService.class.getSimpleName();
    private static final String CHANNEL_ID = "SMSServiceChannel";
    private MySmsReceiver smsReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null; // We are not binding this service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create notification channel for Android O and above
        createNotificationChannel();

        // Create a notification to be shown while the service is running
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SMS Listener Service")
                .setContentText("Running in background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        // Start the service as a foreground service
        startForeground(1, notification);

        String sender = intent.getStringExtra("sender");
        String message = intent.getStringExtra("message");
        String receiver = intent.getStringExtra("receiver");

        if (sender != null && message != null) {
            Log.d(TAG, "Handling SMS from: " + sender);
//            new SmsForwarder().sendToMail(sender, message);
            new SmsForwarder().sendToAPI(sender, receiver, message);
        }

        return START_STICKY; // Ensures the service runs continuously
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    private void createNotificationChannel() {
        // Create a notification channel (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SMS Service Channel";
            String description = "Channel for SMS listener service notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
