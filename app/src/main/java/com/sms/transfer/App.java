package com.sms.transfer;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class App extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
