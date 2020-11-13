package com.vlidar.mobile.ui.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;

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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private List<ScanTimer> mScanTimers = new CopyOnWriteArrayList<>();
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
            mScanTimers.add(scanTimer);
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
            initRecyclerView(mScanTimers);
        });
        mRefreshAndLoadMoreAdapter = new JRefreshAndLoadMoreAdapter(this, mTimerAdapter);
        mRefreshAndLoadMoreAdapter.setIsOpenRefresh(false);
        mRefreshAndLoadMoreAdapter.setIsOpenLoadMore(false);
        jRecycleView.setLayoutManager(new LinearLayoutManager(this));
        jRecycleView.setAdapter(mRefreshAndLoadMoreAdapter);
    }

    @Override
    protected void handleMessages(Message msg) {
    }
}
