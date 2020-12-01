package com.vlidar.mobile.ui;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import androidx.multidex.MultiDex;

import org.litepal.LitePal;

public class VlidarApplication extends Application {
    public static Typeface PingFangSC_Regular;
    public static Typeface PingFangSC_Medium;
    public static Typeface PingFangSC_Thin;

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        PingFangSC_Regular = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts" +
                "/PingFangSC" +
                "-Regular" +
                ".ttf");
        PingFangSC_Medium = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts" +
                "/PingFangSC" +
                "-Medium.ttf");
        PingFangSC_Thin = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts" +
                "/PingFangSC" +
                "-Thin" +
                ".ttf");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
