package com.sms.transfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

                    Log.d(TAG, "SMS from: " + sender);
                    Log.d(TAG, "Message: " + messageBody);
                    Log.d(TAG, "Device Phone Number: " + receiver);

                    boolean isMatching = isMessageMatchingPattern(messageBody);
                    if (isMatching) {
                        // Start the background service to handle the SMS
                        Intent serviceIntent = new Intent(context, MyService.class);
                        serviceIntent.putExtra("sender", sender);
                        serviceIntent.putExtra("receiver", receiver);
                        serviceIntent.putExtra("message", messageBody);
                        Log.d(TAG, "SMS slot index: " + bundle.getInt("simSlotIndex"));
                        LogPrinter.print("SMS slot index: " + bundle.getInt("simSlotIndex"));
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
        // Check if permission is granted for reading phone state
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "Permission not granted"; // Permission is not granted, return message
        }

        // Get the SubscriptionManager system service
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        if (subscriptionManager != null) {
            // Get the list of active subscriptions
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

            // Check if the subscription list is not null and not empty
            if (subscriptionInfoList != null && !subscriptionInfoList.isEmpty()) {
                StringBuilder phoneNumbers = new StringBuilder();

                // Iterate over each subscription info and collect phone numbers
                for (int i = 0; i < subscriptionInfoList.size(); i++) {
                    SubscriptionInfo subscriptionInfo = subscriptionInfoList.get(i);
                    String phoneNumber = subscriptionInfo.getNumber();

                    // Clean the phone number by removing non-numeric characters
                    if (phoneNumber != null) {
                        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
                    } else {
                        phoneNumber = "Unknown";
                    }

                    // Append the phone number to the StringBuilder
                    if (phoneNumber.length() > 0) {
                        if (phoneNumbers.length() > 0) {
                            phoneNumbers.append("/"); // Append the delimiter
                        }
                        phoneNumbers.append(phoneNumber);
                    }

                    // Log the phone number for debugging purposes
                    Log.d("SIM_INFO", "SIM Slot Index: " + i + ", Clean Phone Number: " + phoneNumber);
                }

                // Return the collected phone numbers
                return phoneNumbers.toString();
            } else {
                Log.d("SIM_INFO", "No active SIM subscriptions found.");
            }
        }

        // If something goes wrong, return "Unknown" as fallback
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
