package com.sap.jam.mobile_sdk;

import android.app.Application;
import android.content.Context;

// Provides singleton-style access to app context so we don't have to pass it around everywhere
public class JamSDKDemoApp extends Application {
    private static JamSDKDemoApp context;

    public static Context getAppContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        JamSDKDemoApp.context = this;
    }
}
