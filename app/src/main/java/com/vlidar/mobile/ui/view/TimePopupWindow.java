package com.vlidar.mobile.ui.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.hjq.xtoast.OnToastListener;
import com.hjq.xtoast.XToast;
import com.vlidar.mobile.R;
import com.vlidar.mobile.ui.entity.ScanTimer;
import com.vlidar.mobile.utils.DateTimeUtils;
import com.vlidar.mobile.utils.LogTrace;
import com.vlidar.wheelview.adapter.ArrayWheelAdapter;
import com.vlidar.wheelview.widget.WheelView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

public class TimePopupWindow extends PopupWindow implements WheelView.OnWheelItemSelectedListener<String> {
    private static final LogTrace mLogTrace = new LogTrace("UI", "TimePopupWindow");
    private static final String[] tabs = new String[]{"开始日期", "开始时间", "循环次数", "间隔时间"};

    private View mView; // PopupWindow 菜单布局
    private Activity mContext; // 上下文参数

    private TabLayout mTabLayout;

    private LinearLayout llDate;
    private WheelView<String> mMonthWheelView;
    private WheelView<String> mDayWheelView;

    private LinearLayout llTime;
    private WheelView<String> mHourWheelView;
    private WheelView<String> mMinuteWheelView;
    private WheelView<String> mSecondWheelView;

    private LinearLayout llTimes;
    private WheelView<String> mTimesWheelView;

    private LinearLayout llInterval;
    private WheelView<String> mIntervalHourWheelView;
    private WheelView<String> mIntervalMinuteWheelView;

    private int mCurrentTab = 0;

    private int mBeginYear;
    private int mBeginMonth;
    private int mBeginDay;
    private int mBeginHour;
    private int mBeginMinute;
    private int mBeginSecond;

    private int mIntervalHour;
    private int mIntervalMinute = 20;

    private int mLoopTimes = 1;
    private OnAddTimeDoneListener mOnAddTimeDoneListener;

    private int mCurrentMonth;
    private int mCurrentDay;
    private int mCurrentHour;
    private int mCurrentMinute;
    private int mCurrentSecond;
    private int mBeginMonthAfter5Days;
    private int mBeginDayAfter5Days;
    private int mCrossMonthDays;

    public TimePopupWindow(Activity context) {
        super(context);
        this.mContext = context;
        Init();
        fitPopupWindowOverStatusBar();
    }

    /**
     * 设置布局以及点击事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void Init() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.popup_add_time, null);

        // 导入布局
        this.setContentView(mView);
        // 设置动画效果
        this.setAnimationStyle(R.style.popwindow_anim_style);

        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);

        Button btnCancel = mView.findViewById(R.id.btn_cancel);
        btnCancel.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        btnCancel.setOnClickListener(v -> dismiss());

        Button btnDone = mView.findViewById(R.id.btn_done);
        btnDone.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        btnDone.setOnClickListener(v -> {
            mLogTrace.d("onDoneClick", "mBeginMonth: " + mBeginMonth + ", mBeginDay: " + mBeginDay +
                    ", mBeginHour: " + mBeginHour + ", mBeginMinute: " + mBeginMinute + ", " +
                    "mBeginSecond: " + mBeginSecond + ", mIntervalHour: " + mIntervalHour + ", " +
                    "mIntervalMinute: " + mIntervalMinute + ", mLoopTimes: " + mLoopTimes);
            if (judgeDoneValue()) {
                if (mOnAddTimeDoneListener != null) {
                    mOnAddTimeDoneListener.onAddTimeDone(new ScanTimer(mBeginMonth, mBeginDay,
                            mBeginHour, mBeginMinute, mBeginSecond, mIntervalHour, mIntervalMinute,
                            mLoopTimes));
                }
                dismiss();
            } else {
                resetData();
            }
        });

        // 单击弹出窗以外处 关闭弹出窗
        mView.findViewById(R.id.rl_timer_background).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                dismiss();
            }
            return true;
        });
        mView.findViewById(R.id.rl_duck_bar).setOnTouchListener((v, event) -> true);

        mTabLayout = mView.findViewById(R.id.tab_layout);
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.tab_item), 0, true);
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.tab_item), 1, false);
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.tab_item), 2, false);
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.tab_item), 3, false);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            ((TextView) mTabLayout.getTabAt(i).getCustomView().findViewById(R.id.tv_tab)).setText(tabs[i]);
            ((TextView) mTabLayout.getTabAt(i).getCustomView().findViewById(R.id.tv_tab)).setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/PingFangSC" +
                    "-Regular" +
                    ".ttf"));
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mLogTrace.d("onTabSelected", "tab=" + tab.getPosition());
                if (mCurrentTab == tab.getPosition()) {
                    mLogTrace.d("onTabSelected", "The same tab, do nothing.");
                    return;
                }
                updateWheelState(tab.getPosition());
                mCurrentTab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        llDate = mView.findViewById(R.id.ll_date);
        llTime = mView.findViewById(R.id.ll_time);
        llTimes = mView.findViewById(R.id.ll_times);
        llInterval = mView.findViewById(R.id.ll_interval);

        mMonthWheelView = mView.findViewById(R.id.month_wheel_view);
        mDayWheelView = mView.findViewById(R.id.day_wheel_view);
        mHourWheelView = mView.findViewById(R.id.hour_wheel_view);
        mMinuteWheelView = mView.findViewById(R.id.minute_wheel_view);
        mSecondWheelView = mView.findViewById(R.id.second_wheel_view);
        mTimesWheelView = mView.findViewById(R.id.times_wheel_view);
        mIntervalHourWheelView = mView.findViewById(R.id.interval_hour_wheel_view);
        mIntervalMinuteWheelView = mView.findViewById(R.id.interval_minute_wheel_view);

        setOnDismissListener(() -> darkenBackground(1F));
    }

    private void updateWheelState(int tabPosition) {
        switch (tabPosition) {
            case 0:
                llTime.setVisibility(View.INVISIBLE);
                llTimes.setVisibility(View.INVISIBLE);
                llInterval.setVisibility(View.INVISIBLE);
                llDate.setVisibility(View.VISIBLE);
                updateWheel(mMonthWheelView, createMonths(), 0, "月", false);
                updateWheel(mDayWheelView, createDays(), 0, "日", false);
                break;
            case 1:
                llDate.setVisibility(View.INVISIBLE);
                llTimes.setVisibility(View.INVISIBLE);
                llInterval.setVisibility(View.INVISIBLE);
                llTime.setVisibility(View.VISIBLE);
                setLimitBeginHour();
                setLimitBeginMinute();
//                updateWheel(mHourWheelView, createValidHours(), 0, "时", false);
//                updateWheel(mMinuteWheelView, createMinutes(), 0, "分", false);
                updateWheel(mSecondWheelView, createSeconds(), 0, "秒", false);
                break;
            case 2:
                llTime.setVisibility(View.INVISIBLE);
                llDate.setVisibility(View.INVISIBLE);
                llInterval.setVisibility(View.INVISIBLE);
                llTimes.setVisibility(View.VISIBLE);
                break;
            case 3:
                llTime.setVisibility(View.INVISIBLE);
                llDate.setVisibility(View.INVISIBLE);
                llTimes.setVisibility(View.INVISIBLE);
                llInterval.setVisibility(View.VISIBLE);
//                setLimitIntervalHour();
//                setLimitIntervalMinute();
                break;
            default:
                break;
        }
    }

    private void updateWheel(WheelView<String> wheelView, ArrayList<String> data, int selectedIndex,
                             String sign, boolean isLoop) {
        wheelView.setLoop(isLoop);
        wheelView.setWheelData(data);
//        wheelView.setSelection(selectedIndex);
        wheelView.setExtraText(sign, Color.WHITE,
                (int) mContext.getResources().getDimension(R.dimen.time_extra_text_size), 0);
    }

    public void resetData() {
        mLogTrace.d("resetData");
        mCurrentTab = 0;
        setTabSelected(0);

        mIntervalHour = 0;
        mIntervalMinute = 20;
        mLoopTimes = 1;
        mTimesWheelView.setSelection(0);
        mIntervalHourWheelView.setSelection(0);
        mIntervalMinuteWheelView.setSelection(0);

        setTime();

        initWheelView(mMonthWheelView, createMonths(), "月", false);
        initWheelView(mDayWheelView, createDays(), "日", false);
        initWheelView(mHourWheelView, createValidHours(), "时", false);
        initWheelView(mMinuteWheelView, createMinutes(), "分", false);
        initWheelView(mSecondWheelView, createSeconds(), "秒", false);
        initWheelView(mTimesWheelView, createLoopTimes(), "次", true);
        initWheelView(mIntervalHourWheelView, createInvalidTime(), "小时", true);
        initWheelView(mIntervalMinuteWheelView, createInvalidTime(), "分钟", true);
    }

    private void initWheelView(WheelView<String> wheelView, ArrayList<String> data, String extra,
                               boolean isLoop) {
        wheelView.setWheelAdapter(new ArrayWheelAdapter<>(mContext));
        wheelView.setSkin(WheelView.Skin.None);

        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textSize = 16;
        style.selectedTextSize = 20;
        wheelView.setStyle(style);

        updateWheel(wheelView, data, 0, extra, isLoop);
        wheelView.setOnWheelItemSelectedListener(this);
    }

    private void fitPopupWindowOverStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Field mLayoutInScreen = PopupWindow.class.getDeclaredField("mLayoutInScreen");
                mLayoutInScreen.setAccessible(true);
                mLayoutInScreen.set(this, true);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTabSelected(int index) {
        mTabLayout.selectTab(mTabLayout.getTabAt(index));
    }

    public void darkenBackground(Float alpha) {
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        lp.alpha = alpha;
        mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mContext.getWindow().setAttributes(lp);
    }

    private ArrayList<String> createMonths() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = mCurrentMonth; i <= mBeginMonthAfter5Days; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createDays() {
        ArrayList<String> list = new ArrayList<>();
        if (mBeginDayAfter5Days < mCurrentDay) { // 跨月
            mCrossMonthDays = mBeginDayAfter5Days;
            mLogTrace.d("createDays",
                    "mCurrentDay:" + mCurrentDay + "mCrossMonthDays: " + mCrossMonthDays);
            for (int i = mCurrentDay; i < mCurrentDay + 5 - mCrossMonthDays; i++) {
                list.add("" + i);
            }
            for (int i = 1; i <= mCrossMonthDays; i++) {
                list.add("0" + i);
            }
        } else {
            for (int i = mCurrentDay; i <= mBeginDayAfter5Days; i++) {
                if (i < 10) {
                    list.add("0" + i);
                } else {
                    list.add("" + i);
                }
            }
        }
        return list;
    }

    private ArrayList<String> createValidHours() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = mCurrentHour; i < 24; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createHours() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createMinutes() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = mCurrentMinute; i < 60; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createSeconds() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createMinutesSeconds(int limit) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = limit; i < 60; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createLoopTimes() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createInvalidTime() {
        ArrayList<String> list = new ArrayList<>();
        list.add("-");
        return list;
    }

    private void setTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // 从10分钟以后开始算
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 10);

        mCurrentMonth = calendar.get(Calendar.MONTH) + 1;
        mCurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
        mCurrentHour = calendar.get(Calendar.HOUR_OF_DAY);
        mCurrentMinute = calendar.get(Calendar.MINUTE);
        mCurrentSecond = calendar.get(Calendar.SECOND);
        mLogTrace.d("setTime", "mCurrentMonth: " + mCurrentMonth + ", mCurrentDay: " + mCurrentDay +
                ", mCurrentHour:" + mCurrentHour + ", mCurrentMinute:" + mCurrentMinute + ", " +
                "mCurrentSecond:" + mCurrentSecond);

        // 设置日期不能超过5天
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 5);
        mBeginMonthAfter5Days = calendar.get(Calendar.MONTH) + 1;
        mBeginDayAfter5Days = calendar.get(Calendar.DAY_OF_MONTH);
        mLogTrace.d("setTime", "mBeginMonthAfter5Days:" + mBeginMonthAfter5Days + ", " +
                "mBeginDayAfter5Days:" + mBeginDayAfter5Days);

        updateWheelState(mCurrentTab);
    }

    private boolean judgeDoneValue() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = calendar.get(Calendar.YEAR);
        if (mCurrentMonth == 12 && mBeginMonth == 1) { // 跨年
            currentYear += 1;
        }
        Calendar itemCalendar = Calendar.getInstance();
        itemCalendar.setTime(DateTimeUtils.parse(currentYear + "-" + mBeginMonth + "-" + mBeginDay + " " + mBeginHour + ":" + mBeginMinute + ":" + mBeginSecond));
        itemCalendar.set(Calendar.MINUTE,
                itemCalendar.get(Calendar.MINUTE) + (mLoopTimes - 1) * (mIntervalHour * 60 + mIntervalMinute));

        if (itemCalendar.getTimeInMillis() + 5 <= System.currentTimeMillis()) {
            if (mOnAddTimeDoneListener != null) {
                mOnAddTimeDoneListener.onAddTimeError("定时器设置过期，请重新设置");
            }
            return false;
        }

        /*if (mBeginMonth < currentMonth) {
            if (mOnAddTimeDoneListener != null) {
                mOnAddTimeDoneListener.onAddTimeError("开始日期(" + mBeginMonth + "月)无效");
            }
            return false;
        }
        if (mBeginMonth == currentMonth && mBeginDay < currentDay) {
            if (mOnAddTimeDoneListener != null) {
                mOnAddTimeDoneListener.onAddTimeError("开始日期(" + mBeginDay + "日)无效");
            }
            return false;
        }
        if (mBeginMonth == currentMonth && mBeginDay == currentDay
                && (mBeginHour < currentHour
                || (mBeginHour == currentHour && mBeginMinute < currentMinute)
                || ((mBeginHour == currentHour && mBeginMinute == currentMinute) &&
                mBeginSecond < mCurrentSecond - 5))) {
            if (mOnAddTimeDoneListener != null) {
                mOnAddTimeDoneListener.onAddTimeError("开始时间(" + mBeginHour + ":" + mBeginMinute
                + ":" + mBeginSecond + ")无效");
            }
            return false;
        }*/
        return true;
    }

    @Override
    public void onItemSelected(View view, int position, String str) {
        switch (view.getId()) {
            case R.id.month_wheel_view:
                mLogTrace.d("onItemSelected, begin month-->",
                        "position=" + position + ", str=" + str);
                mBeginMonth = Integer.parseInt(str);
                setLimitBeginDay();
                break;

            case R.id.day_wheel_view:
                mLogTrace.d("onItemSelected, begin day-->",
                        "position=" + position + ", str=" + str);
                mBeginDay = Integer.parseInt(str);
                setLimitBeginMonth();
                setLimitBeginHour();
                setLimitBeginMinute();
                break;

            case R.id.hour_wheel_view:
                mLogTrace.d("onItemSelected, begin hour-->",
                        "position=" + position + ", str=" + str);
                mBeginHour = Integer.parseInt(str);
                setLimitBeginMinute();
                break;

            case R.id.minute_wheel_view:
                mLogTrace.d("onItemSelected, begin minute-->",
                        "position=" + position + ", str=" + str);
                mBeginMinute = Integer.parseInt(str);
                break;

            case R.id.second_wheel_view:
                mLogTrace.d("onItemSelected, begin second-->",
                        "position=" + position + ", str=" + str);
                mBeginSecond = Integer.parseInt(str);
                break;

            case R.id.times_wheel_view:
                mLogTrace.d("onItemSelected, loop times-->",
                        "position=" + position + ", str=" + str);
                mLoopTimes = Integer.parseInt(str);
                setLimitIntervalHour();
                setLimitIntervalMinute();
                break;
            case R.id.interval_hour_wheel_view:
                mLogTrace.d("onItemSelected, interval hour-->",
                        "position=" + position + ", str=" + str);
                if (TextUtils.equals("-", str)) {
                    mIntervalHour = 0;
                } else {
                    mIntervalHour = Integer.parseInt(str);
                }
                setLimitIntervalMinute();
                break;

            case R.id.interval_minute_wheel_view:
                mLogTrace.d("onItemSelected, interval minute-->",
                        "position=" + position + ", str=" + str);
                if (TextUtils.equals("-", str)) {
                    mIntervalMinute = 20;
                } else {
                    mIntervalMinute = Integer.parseInt(str);
                }
                break;
            default:
                break;
        }
    }

    private void setLimitIntervalHour() {
        if (mLoopTimes > 1) {
            mIntervalHour = 0;
            updateWheel(mIntervalHourWheelView, createHours(), 0, "小时", true);
        } else {
            mIntervalHour = 0;
            updateWheel(mIntervalHourWheelView, createInvalidTime(), 0, "小时", true);
        }
        mIntervalHourWheelView.setSelection(0);
    }

    /**
     * 如果设了循环次数，间隔不能小于20分钟
     */
    private void setLimitIntervalMinute() {
        if (mLoopTimes > 1) {
            if (mIntervalHour <= 0) {
                mIntervalMinute = 20;
                updateWheel(mIntervalMinuteWheelView, createMinutesSeconds(20), 0, "分钟", true);
            } else {
                mIntervalMinute = 0;
                updateWheel(mIntervalMinuteWheelView, createMinutesSeconds(0), 0, "分钟", true);
            }
        } else {
            mIntervalMinute = 20;
            updateWheel(mIntervalMinuteWheelView, createInvalidTime(), 0, "分钟", true);
        }
        mIntervalMinuteWheelView.setSelection(0);
    }

    /**
     * 选择当日时，选择的小时不能小于当前时间，选择后续时间，可以选择24小时
     */
    private void setLimitBeginHour() {
        if (mBeginMonth == mCurrentMonth && mBeginDay <= mCurrentDay) {
            mBeginHour = mCurrentHour;
            updateWheel(mHourWheelView, createValidHours(), 0, "时", false);
        } else {
            mBeginHour = 0;
            updateWheel(mHourWheelView, createHours(), 0, "时", false);
        }
        mHourWheelView.setSelection(0);
    }

    /**
     * 选择当日时，选择的分钟不能小于当前时间，选择后续时间，可以选择任意时间
     */
    private void setLimitBeginMinute() {
        if (mBeginMonth == mCurrentMonth && mBeginDay <= mCurrentDay && mBeginHour <= mCurrentHour) {
            mBeginMinute = mCurrentMinute;
            updateWheel(mMinuteWheelView, createMinutes(), 0, "分", false);
        } else {
            mBeginMinute = 0;
            updateWheel(mMinuteWheelView, createMinutesSeconds(0), 0, "分", false);
        }
        mMinuteWheelView.setSelection(0);
    }

    private void setLimitBeginMonth() {
        updateWheel(mMonthWheelView, createMonths(), 0, "月", false);
        if (mCrossMonthDays > 0) { // 存在跨月
            if (mBeginDay > 0 && mBeginDay < 5) {
                mBeginMonth = mBeginMonthAfter5Days;
                mMonthWheelView.setSelection(1);
            } else {
                mBeginMonth = mCurrentMonth;
                mMonthWheelView.setSelection(0);
            }
        }
    }

    private void setLimitBeginDay() {
        updateWheel(mDayWheelView, createDays(), 0, "日", false);
        if (mCrossMonthDays > 0) { // 存在跨月
            if (mBeginMonth > mCurrentMonth) {
                mBeginDay = 1;
                mDayWheelView.setSelection(5 - mCrossMonthDays);
            } else {
                mBeginDay = mCurrentDay;
                mDayWheelView.setSelection(0);
            }
        }
    }


    public void setOnAddTimeDoneListener(OnAddTimeDoneListener onAddTimeDoneListener) {
        this.mOnAddTimeDoneListener = onAddTimeDoneListener;
    }

    public interface OnAddTimeDoneListener {
        void onAddTimeError(String error);

        void onAddTimeDone(ScanTimer scanTimer);
    }
}
