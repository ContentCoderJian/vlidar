package com.vlidar.jrecyclerview.utils;

import android.util.Log;

import com.vlidar.jrecyclerview.config.JRecycleViewManager;

/**
 * 日志打印
 */
public class LogUtils {

    private static final String TAG = "JRecycleView";

    public static void i(String tag, String msg) {
        if (JRecycleViewManager.getInstance().isDebug()) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (JRecycleViewManager.getInstance().isDebug()) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (JRecycleViewManager.getInstance().isDebug()) {
            Log.w(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (JRecycleViewManager.getInstance().isDebug()) {
            Log.v(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (JRecycleViewManager.getInstance().isDebug()) {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void e(String msg) {
        Log.i(TAG, msg);
    }
}
