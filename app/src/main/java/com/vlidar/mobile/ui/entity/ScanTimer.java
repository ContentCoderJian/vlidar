package com.vlidar.mobile.ui.entity;

import java.util.Objects;

public class ScanTimer {
    private String BeginTime;
    private String interval;
    private String times;

    public ScanTimer(String beginTime, String interval, String times) {
        BeginTime = beginTime;
        this.interval = interval;
        this.times = times;
    }

    public String getBeginTime() {
        return BeginTime;
    }

    public void setBeginTime(String beginTime) {
        BeginTime = beginTime;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScanTimer scanTimer = (ScanTimer) o;
        return BeginTime.equals(scanTimer.BeginTime) &&
                interval.equals(scanTimer.interval) &&
                times.equals(scanTimer.times);
    }

    @Override
    public int hashCode() {
        return Objects.hash(BeginTime, interval, times);
    }

    @Override
    public String toString() {
        return "ScanTimer{" +
                "BeginTime='" + BeginTime + '\'' +
                ", interval='" + interval + '\'' +
                ", times='" + times + '\'' +
                '}';
    }
}
