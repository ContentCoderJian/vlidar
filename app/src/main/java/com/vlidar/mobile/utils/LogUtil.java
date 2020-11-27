package com.vlidar.mobile.utils;

import android.util.Log;

/**
 * Log日志工具类, 测试时才打印日志
 */
public class LogUtil {
    private static final String PLUS = "--";

    //打印i级别日志
    protected static void i(String module, String tag, String msg) {
        if (isLogOut())
            Log.i(module, tag + PLUS + msg);
    }

    //打印d级别日志
    protected static void d(String module, String tag, String msg) {
        if (isLogOut())
            Log.d(module, tag + PLUS + msg);
    }

    //打印w级别日志
    protected static void w(String module, String tag, String msg) {
        if (isLogOut())
            Log.w(module, tag + PLUS + msg);
    }

    //打印e级别日志
    protected static void e(String module, String tag, String msg) {
        if (isLogOut())
            Log.e(module, tag + PLUS + msg);
    }

    //打印v级别日志
    protected static void v(String module, String tag, String msg) {
        if (isLogOut())
            Log.v(module, tag + PLUS + msg);
    }

    //是否打印日志
    public static boolean isLogOut() {
        return false;
    }
}
