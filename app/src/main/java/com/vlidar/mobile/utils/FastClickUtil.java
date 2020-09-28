package com.vlidar.mobile.utils;

public class FastClickUtil {
    private static long lastClickTime = 0;
    private static final long SPACE_TIME = 600;

    /**
     * 判断用户是否快速点击多次
     */
    public static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        boolean isAllowClick;

        if (currentTime - lastClickTime > SPACE_TIME) {
            isAllowClick = false;
        } else {
            isAllowClick = true;
        }
        lastClickTime = currentTime;

        return isAllowClick;
    }
}
