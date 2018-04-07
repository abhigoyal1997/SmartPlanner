package com.example.abhinav.smartplanner;

import android.app.Application;

/**
 * Created by abhi on 4/3/18.
 */

public class App extends Application {
    private static App mInstance;

    public static App get() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
    }
}
