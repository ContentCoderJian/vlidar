package com.vlidar.mobile.ui.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.vlidar.mobile.R;
import com.vlidar.mobile.utils.LogTrace;
import com.vlidar.wheelview.adapter.ArrayWheelAdapter;
import com.vlidar.wheelview.widget.WheelView;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class TimePopupWindow extends PopupWindow {
    private static final LogTrace mLogTrace = new LogTrace("UI", "TimePopupWindow");
    private View mView; // PopupWindow 菜单布局
    private Activity mContext; // 上下文参数

    private TabLayout mTabLayout;
    private WheelView<String> mHourWheelView;
    private WheelView<String> mMinuteWheelView;
    private WheelView<String> mSecondWheelView;
    private static final String[] tabs = new String[]{"开始", "间隔", "循环"};

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
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mHourWheelView = mView.findViewById(R.id.hour_wheel_view);
        mMinuteWheelView = mView.findViewById(R.id.minute_wheel_view);
        mSecondWheelView = mView.findViewById(R.id.second_wheel_view);

        initWheelView(mHourWheelView, createHours(), "时");
        initWheelView(mMinuteWheelView, createMinutesSeconds(), "分");
        initWheelView(mSecondWheelView, createMinutesSeconds(), "秒");

        setOnDismissListener(() -> darkenBackground(1F));
    }

    private void initWheelView(WheelView<String> wheelView, ArrayList<String> data, String extra) {
        wheelView.setWheelAdapter(new ArrayWheelAdapter<>(mContext));
        wheelView.setSkin(WheelView.Skin.None);
        wheelView.setLoop(true);
        //        mWheelView.setWheelClickable(true);

        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textSize = 16;
        style.selectedTextSize = 20;
        wheelView.setStyle(style);

        wheelView.setWheelData(data);
        wheelView.setExtraText(extra, Color.WHITE,
                (int) mContext.getResources().getDimension(R.dimen.time_extra_text_size), 76);
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

    private ArrayList<String> createMinutesSeconds() {
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
}
