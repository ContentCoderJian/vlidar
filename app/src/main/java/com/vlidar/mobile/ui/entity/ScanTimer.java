package com.vlidar.mobile.ui.entity;

import org.litepal.crud.LitePalSupport;

import java.util.Objects;

public class ScanTimer extends LitePalSupport {
    private int beginMonth;
    private int beginDay;
    private int beginHour;
    private int beginMinute;
    private int beginSecond;
    private int intervalHour;
    private int intervalMinute;
    private int times;
    private boolean checked = false;

    public ScanTimer(int beginMonth, int beginDay, int beginHour, int beginMinute,
                     int beginSecond, int intervalHour, int intervalMinute, int times) {
        this.beginMonth = beginMonth;
        this.beginDay = beginDay;
        this.beginHour = beginHour;
        this.beginMinute = beginMinute;
        this.beginSecond = beginSecond;
        this.intervalHour = intervalHour;
        this.intervalMinute = intervalMinute;
        this.times = times;
    }

    public int getBeginMonth() {
        return beginMonth;
    }

    public void setBeginMonth(int beginMonth) {
        this.beginMonth = beginMonth;
    }

    public int getBeginDay() {
        return beginDay;
    }

    public void setBeginDay(int beginDay) {
        this.beginDay = beginDay;
    }

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
        this.beginHour = beginHour;
    }

    public int getBeginMinute() {
        return beginMinute;
    }

    public void setBeginMinute(int beginMinute) {
        this.beginMinute = beginMinute;
    }

    public int getBeginSecond() {
        return beginSecond;
    }

    public void setBeginSecond(int beginSecond) {
        this.beginSecond = beginSecond;
    }

    public int getIntervalHour() {
        return intervalHour;
    }

    public void setIntervalHour(int intervalHour) {
        this.intervalHour = intervalHour;
    }

    public int getIntervalMinute() {
        return intervalMinute;
    }

    public void setIntervalMinute(int intervalMinute) {
        this.intervalMinute = intervalMinute;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScanTimer scanTimer = (ScanTimer) o;
        return beginMonth == scanTimer.beginMonth &&
                beginDay == scanTimer.beginDay &&
                beginHour == scanTimer.beginHour &&
                beginMinute == scanTimer.beginMinute &&
                beginSecond == scanTimer.beginSecond;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginMonth, beginDay, beginHour, beginMinute, beginSecond);
    }

    @Override
    public String toString() {
        return "ScanTimer{" +
                "beginMonth=" + beginMonth +
                ", beginDay=" + beginDay +
                ", beginHour=" + beginHour +
                ", beginMinute=" + beginMinute +
                ", beginSecond=" + beginSecond +
                ", intervalHour=" + intervalHour +
                ", intervalMinute=" + intervalMinute +
                ", times=" + times +
                ", checked=" + checked +
                '}';
    }
}
