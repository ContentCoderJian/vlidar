package com.vlidar.mobile.ui.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.vlidar.jrecyclerview.JRecycleView;
import com.vlidar.jrecyclerview.adapter.JRefreshAndLoadMoreAdapter;
import com.vlidar.mobile.R;
import com.vlidar.mobile.logic.BLESPPUtils;
import com.vlidar.mobile.logic.ProtocolController;
import com.vlidar.mobile.logic.ProtocolObserver;
import com.vlidar.mobile.logic.Status;
import com.vlidar.mobile.ui.adapter.TimerAdapter;
import com.vlidar.mobile.ui.entity.ScanTimer;
import com.vlidar.mobile.ui.view.SimpleDividerDecoration;
import com.vlidar.mobile.ui.view.TimePopupWindow;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.OnClick;

public class TimerActivity extends BaseActivity implements ProtocolObserver {
    @BindView(R.id.recycle_view)
    JRecycleView jRecycleView;

    @BindView(R.id.ib_scan)
    ImageView ibScan;

    @BindDimen(R.dimen.list_margin)
    int mItemHeight;
    @BindColor(android.R.color.transparent)
    int colorTransparent;

    private ProtocolController mProtocolController;
    private BLESPPUtils mBleSppUtils;

    private List<ScanTimer> mScanTimers = new ArrayList<>();
    private JRefreshAndLoadMoreAdapter mRefreshAndLoadMoreAdapter;
    private TimerAdapter mTimerAdapter;

    private TimePopupWindow mTimePopupWindow;
    private RotateAnimation mRotateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_timer);
        super.onCreate(savedInstanceState);
        mProtocolController = ProtocolController.getInstance();
        mProtocolController.addObservers(this);
        mBleSppUtils = BLESPPUtils.getInstance(this);
        jRecycleView.addItemDecoration(new SimpleDividerDecoration(this,
                mItemHeight, colorTransparent, false));
        mTimePopupWindow = new TimePopupWindow(this);
        mTimePopupWindow.setOnAddTimeDoneListener(new TimePopupWindow.OnAddTimeDoneListener() {
            @Override
            public void onAddTimeError(String error) {
                mLogTrace.w("onAddTimeError", "error:" + error);
                showToast(error, DEFAULT_TOAST_DURATION);
            }

            @Override
            public void onAddTimeDone(ScanTimer scanTimer) {
                mLogTrace.d("onAddTimeDone", "scanTimer:" + scanTimer);
                if (mScanTimers.contains(scanTimer)) {
                    mScanTimers.get(mScanTimers.indexOf(scanTimer)).delete();
                }
                scanTimer.save();
                mScanTimers = mProtocolController.getScanTimes();
                mBleSppUtils.send(mProtocolController.sendScanTimer());
                initRecyclerView(mScanTimers);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScanTimers = mProtocolController.getScanTimes();
        startOrStopRotate();
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
                for (ScanTimer scanTimer : mScanTimers) {
                    scanTimer.setChecked(true);
                    scanTimer.save();
                }
                mScanTimers = mProtocolController.getScanTimes();
                initRecyclerView(mScanTimers);
                mBleSppUtils.send(mProtocolController.sendScanTimer());
                break;
            default:
                break;
        }
    }

    private void initRecyclerView(List<ScanTimer> scanTimers) {
        mTimerAdapter = new TimerAdapter(this, scanTimers);
        startOrStopRotate();
        mTimerAdapter.setOnDeleteListener(position -> {
            mLogTrace.d("onDeleteClick, position = " + position);
            mScanTimers.get(position).delete();
            mScanTimers.remove(position);
            mScanTimers = mProtocolController.getScanTimes();
            mBleSppUtils.send(mProtocolController.sendScanTimer());
            initRecyclerView(mScanTimers);
        });
        mTimerAdapter.setOnCheckedListener(checked -> {
            mScanTimers = mProtocolController.getScanTimes();
            startOrStopRotate();
            mBleSppUtils.send(ProtocolController.getInstance().sendScanTimer());
        });
        mRefreshAndLoadMoreAdapter = new JRefreshAndLoadMoreAdapter(this, mTimerAdapter);
        mRefreshAndLoadMoreAdapter.setIsOpenRefresh(false);
        mRefreshAndLoadMoreAdapter.setIsOpenLoadMore(false);
        jRecycleView.setLayoutManager(new LinearLayoutManager(this));
        jRecycleView.setAdapter(mRefreshAndLoadMoreAdapter);
    }

    private void startOrStopRotate() {
        for (ScanTimer scanTimer : mScanTimers) {
            if (scanTimer.isChecked()) {
                startRotateAnimation(ibScan);
                return;
            }
        }
        stopRotateAnimation(ibScan);
    }

    private void startRotateAnimation(ImageView imageView) {
        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateAnimation.setDuration(1000);
            mRotateAnimation.setInterpolator(new LinearInterpolator());
//            mRotateAnimation.setFillAfter(true);
//            mRotateAnimation.setFillBefore(true);
            mRotateAnimation.setRepeatCount(AlphaAnimation.INFINITE);
        }
        imageView.startAnimation(mRotateAnimation);
    }

    private void stopRotateAnimation(ImageView imageView) {
        //清除动画
        imageView.clearAnimation();
        if (mRotateAnimation != null) {
            mRotateAnimation.cancel();
            mRotateAnimation = null;
        }
    }

    @Override
    protected void handleMessages(Message msg) {
    }

    @Override
    public void updatePowerStatus(int state) {

    }

    @Override
    public void updateRadarStatus(int state) {
        runOnUiThread(() -> {
            if (state == Status.RADAR.NO_TF) {
                showToast("未插卡，请插入TF卡", DEFAULT_TOAST_DURATION);
            }
        });
    }

    @Override
    public void updateIMU(double eularYaw, double eularPitch, double eularRoll) {

    }

    @Override
    public void updateGPSLocation(double latitude, double longitude) {

    }

    @Override
    public void updateMCUUpdateStatus(int status, String info) {
        if (status == 0 || status == 1 || status == 2) {
            runOnUiThread(() -> showToast(info, DEFAULT_TOAST_DURATION));
        }
    }

    @Override
    public void updateAppUpdateStatus(int status, String info) {
        if (status == 1 || status == 2 || status == 3) {
            runOnUiThread(() -> showToast(info, DEFAULT_TOAST_DURATION));
        }
    }
}
