package com.vlidar.mobile.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vlidar.jrecyclerview.JRecycleView;
import com.vlidar.jrecyclerview.adapter.JRefreshAndLoadMoreAdapter;
import com.vlidar.mobile.R;
import com.vlidar.mobile.ui.adapter.TimerAdapter;
import com.vlidar.mobile.ui.entity.ScanTimer;
import com.vlidar.mobile.ui.view.SimpleDividerDecoration;
import com.vlidar.mobile.ui.view.TimePopupWindow;
import com.vlidar.wheelview.adapter.ArrayWheelAdapter;
import com.vlidar.wheelview.widget.WheelView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.OnClick;

public class TimerActivity extends BaseActivity {
    @BindView(R.id.recycle_view)
    JRecycleView jRecycleView;

    @BindDimen(R.dimen.list_margin)
    int mItemHeight;
    @BindColor(android.R.color.transparent)
    int colorTransparent;

    private List<ScanTimer> mScanTimers = new ArrayList<>();
    private JRefreshAndLoadMoreAdapter mRefreshAndLoadMoreAdapter;
    private TimerAdapter mTimerAdapter;

    private TimePopupWindow mTimePopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_timer);
        super.onCreate(savedInstanceState);
        jRecycleView.addItemDecoration(new SimpleDividerDecoration(this,
                mItemHeight, colorTransparent, false));
        mTimePopupWindow = new TimePopupWindow(this);
        mTimePopupWindow.setOnAddTimeDoneListener(scanTimer -> {
            mLogTrace.d("onAddTimeDone", "scanTimer:" + scanTimer);
            mScanTimers.remove(scanTimer);
            mScanTimers.add(scanTimer);
            sortScanTimes();
            initRecyclerView(mScanTimers);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView(mScanTimers);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.ib_back, R.id.ib_add, R.id.ib_scan})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
                super.onBackPressed();
                break;

            case R.id.ib_add:
                mTimePopupWindow.darkenBackground(0.6F);
                mTimePopupWindow.resetData();
                mTimePopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
                break;

            case R.id.ib_scan:

                break;
            default:
                break;
        }
    }

    private void initRecyclerView(List<ScanTimer> scanTimers) {
        mTimerAdapter = new TimerAdapter(this, scanTimers);
        mTimerAdapter.setOnDeleteListener(position -> {
            mLogTrace.d("onDeleteClick, position = " + position);
            mScanTimers.remove(position);
            sortScanTimes();
            initRecyclerView(mScanTimers);
        });
        mRefreshAndLoadMoreAdapter = new JRefreshAndLoadMoreAdapter(this, mTimerAdapter);
        mRefreshAndLoadMoreAdapter.setIsOpenRefresh(false);
        mRefreshAndLoadMoreAdapter.setIsOpenLoadMore(false);
        jRecycleView.setLayoutManager(new LinearLayoutManager(this));
        jRecycleView.setAdapter(mRefreshAndLoadMoreAdapter);
    }

    private void sortScanTimes() {
        Collections.sort(mScanTimers, (o1, o2) -> {
            if (o1.getBeginMonth() != o2.getBeginMonth()) {
                return o1.getBeginMonth() - o2.getBeginMonth();
            } else if (o1.getBeginDay() != o2.getBeginDay()) {
                return o1.getBeginDay() - o2.getBeginDay();
            } else if (o1.getBeginHour() != o2.getBeginHour()) {
                return o1.getBeginHour() - o2.getBeginHour();
            } else if (o1.getBeginMinute() != o2.getBeginMinute()) {
                return o1.getBeginMinute() - o2.getBeginMinute();
            } else if (o1.getBeginSecond() != o2.getBeginSecond()) {
                return o1.getBeginSecond() - o2.getBeginSecond();
            } else if (o1.getTimes() != o2.getTimes()) {
                return o1.getTimes() - o2.getTimes();
            } else if (o1.getIntervalHour() != o2.getIntervalHour()) {
                return o1.getIntervalHour() - o2.getIntervalHour();
            } else if (o1.getIntervalMinute() != o2.getIntervalMinute()) {
                return o1.getIntervalMinute() - o2.getIntervalMinute();
            }
            return 0;
        });
    }

    @Override
    protected void handleMessages(Message msg) {
    }
}
