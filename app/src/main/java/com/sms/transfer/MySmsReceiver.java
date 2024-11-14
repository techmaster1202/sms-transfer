package com.sms.transfer;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                    int simSlotIndex = bundle.getInt("simSlotIndex", 0);
                    String receiver = getAllDevicePhoneNumbers(context);

                    boolean isMatching = isMessageMatchingPattern(messageBody);
                    if (isMatching) {
                        // Retrieve the saved switch state from SharedPreferences
                        SharedPreferences sharedPreferences = context.getSharedPreferences("appPreferences", MODE_PRIVATE);
                        boolean isServiceSwitchOn = sharedPreferences.getBoolean("serviceSwitchState", false);
                        if (isServiceSwitchOn) {
                            // Start the background service to handle the SMS
                            Intent serviceIntent = new Intent(context, MyService.class);
                            serviceIntent.putExtra("sender", sender);
                            serviceIntent.putExtra("receiver", receiver);
                            serviceIntent.putExtra("message", messageBody);
                            Log.d(TAG, "SMS from: " + sender);
                            Log.d(TAG, "SMS to: " + receiver);
                            Log.d(TAG, "message: " + messageBody);

                            LogPrinter.print("SMS from: " + sender);
                            LogPrinter.print("SMS to: " + receiver);
                            LogPrinter.print("message: " + messageBody);

                            // Check Android version for service type
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent); // Use this for API 26+
                            } else {
                                context.startService(serviceIntent); // Use this for lower versions
                            }

                            if (mListener != null) {
                                mListener.onSMSReceive(sender, receiver, messageBody);
                            } else {
                                Log.w(TAG, "No listener registered to handle the SMS");
                            }
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onSMSReceive("", "", "");
                        } else {
                            Log.w(TAG, "No listener registered to handle the SMS");
                        }

                    }
                }
            }
        }
    }

    private String getAllDevicePhoneNumbers(Context context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            return "Permission not granted";
        }

        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (subscriptionManager != null) {
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            StringBuilder phoneNumbers = new StringBuilder();

            for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                String phoneNumber = subscriptionInfo.getNumber();

                if (phoneNumber != null) {
                    phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
                } else {
                    phoneNumber = "Unknown";
                }

                if (phoneNumbers.length() > 0) {
                    if (phoneNumber.length() > 0) {
                        phoneNumbers.append(" / ");
                    }
                }
                phoneNumbers.append(phoneNumber);
            }

            return phoneNumbers.toString();
        }

        return "Unknown";
    }


    public boolean isMessageMatchingPattern(String messageBody) {
        if (messageBody.contains("Inicis")) {
            String regex = "\\d{6}"; // Match exactly 6 digits
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(messageBody);
            return matcher.find(); // Check if the pattern exists anywhere in the string
        }
        return false;
    }

    public static void subscribe(IMessageReceive listener) {
        mListener = listener;
    }
}
