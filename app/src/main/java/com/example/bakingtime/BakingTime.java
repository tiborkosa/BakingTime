package com.example.bakingtime;

import android.app.Application;
import android.content.Context;

public class BakingTime extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        BakingTime.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return BakingTime.context;
    }


}
