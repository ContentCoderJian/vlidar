package com.vlidar.mobile.utils;

public class LatLngUtils {
    public static String DDtoDMS(Double d) {
        String[] array = d.toString().split("[.]");
        String degrees = array[0]; // 度

        Double m = Double.parseDouble("0." + array[1]) * 60;
        String[] array1 = m.toString().split("[.]");
        String minutes = array1[0]; // 分

        Double s = Double.parseDouble("0." + array1[1]) * 60;
        String seconds = s.toString(); // 秒
        return degrees + "°" + minutes + "'" + seconds + "\"";
    }
}