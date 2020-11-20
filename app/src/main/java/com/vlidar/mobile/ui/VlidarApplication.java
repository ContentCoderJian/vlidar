package com.vlidar.mobile.ui;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import org.litepal.LitePal;

public class VlidarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
