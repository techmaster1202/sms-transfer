package com.sms.transfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Objects;

public class MySmsReceiver extends BroadcastReceiver {

    private static final String TAG = MySmsReceiver.class.getSimpleName();
    private static final String PDU_TYPE = "pdus";

    private static IMessageReceive mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String format = bundle.getString("format");
            Object[] pdus = (Object[]) bundle.get(PDU_TYPE);
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = smsMessage.getMessageBody();
                    String sender = Objects.requireNonNull(smsMessage.getOriginatingAddress()).replaceAll("\\D", "");
                    String receiver = getDevicePhoneNumber(context).replaceAll("\\D", "");
                    Log.d(TAG, "SMS from: " + sender);
                    Log.d(TAG, "Message: " + messageBody);
                    Log.d(TAG, "Device Phone Number: " + receiver);

                    boolean isMatching = isMessageMatchingPattern(messageBody);
                    if (isMatching) {
                        // Start the background service to handle the SMS
                        Intent serviceIntent = new Intent(context, MyService.class);
                        serviceIntent.putExtra("sender", sender);
                        serviceIntent.putExtra("message", messageBody);
                        serviceIntent.putExtra("receiver", receiver);
                        context.startService(serviceIntent);
                        mListener.onSMSReceive(sender, receiver, messageBody);
                    } else {
                        mListener.onSMSReceive("", "", "");
                    }
                }
            }
        }
    }

    private String getDevicePhoneNumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "Permission not granted";
        }
        return telephonyManager.getLine1Number();
    }

    public boolean isMessageMatchingPattern(String messageBody) {
        if (messageBody.contains("Inicis")) {
            String regex = "\\d{6}";
            if (messageBody.matches(".*" + regex + ".*")) {
                return true;
            }
        }
        return false;
    }

    public static void subscribe(IMessageReceive listener) {
        mListener = listener;
    }
}
