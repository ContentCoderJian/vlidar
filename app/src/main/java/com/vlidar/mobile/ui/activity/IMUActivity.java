package com.vlidar.mobile.ui.activity;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.vlidar.mobile.R;
import com.vlidar.mobile.logic.BLESPPUtils;
import com.vlidar.mobile.logic.ProtocolController;
import com.vlidar.mobile.logic.ProtocolObserver;
import com.vlidar.mobile.logic.Status;

import butterknife.BindView;
import butterknife.OnClick;

public class IMUActivity extends BaseActivity implements ProtocolObserver {
    @BindView(R.id.gridlayout)
    GridLayout gridLayout;

    private ProtocolController mProtocolController;
    private BLESPPUtils mBleSppUtils;

    private RotateAnimation mRotateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_imu);
        super.onCreate(savedInstanceState);
        mProtocolController = ProtocolController.getInstance();
        mProtocolController.addObservers(this);
        mBleSppUtils = BLESPPUtils.getInstance(this);

        initViews();
    }

    private void initViews() {
        setImuBackground(gridLayout.getChildAt(0), R.drawable.imu30);
        setImuBackground(gridLayout.getChildAt(1), R.drawable.imu60);
        setImuBackground(gridLayout.getChildAt(2), R.drawable.imu90);
        setImuBackground(gridLayout.getChildAt(3), R.drawable.imu120);
        setImuBackground(gridLayout.getChildAt(4), R.drawable.imu180);

        resetScanStatus();

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            gridLayout.getChildAt(i).setOnClickListener(v -> {
                mLogTrace.d("onClick", "v : " + v);
                resetScanStatus();
                v.findViewById(R.id.ib_scan).callOnClick();
                setImuClicked(v);
                switch (v.getId()) {
                    case R.id.imu_30:
                        mBleSppUtils.send(mProtocolController.sendOnceScan((byte) 0x04));
                        break;
                    case R.id.imu_60:
                        mBleSppUtils.send(mProtocolController.sendOnceScan((byte) 0x05));
                        break;
                    case R.id.imu_90:
                        mBleSppUtils.send(mProtocolController.sendOnceScan((byte) 0x06));
                        break;
                    case R.id.imu_120:
                        mBleSppUtils.send(mProtocolController.sendOnceScan((byte) 0x07));
                        break;
                    case R.id.imu_180:
                        mBleSppUtils.send(mProtocolController.sendOnceScan((byte) 0x08));
                        break;
                    default:
                        break;
                }
            });
        }
    }

    private void resetScanStatus() {
        setImuText(gridLayout.getChildAt(0), "30°");
        setImuText(gridLayout.getChildAt(1), "60°");
        setImuText(gridLayout.getChildAt(2), "90°");
        setImuText(gridLayout.getChildAt(3), "120°");
        setImuText(gridLayout.getChildAt(4), "180°");
    }

    private void setImuBackground(View view, int drawableResId) {
        ((ImageView) view.findViewById(R.id.iv_imu)).setImageResource(drawableResId);
    }

    private void setImuText(View view, String text) {
        view.findViewById(R.id.iv_scan).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.tv_imu).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.tv_imu)).setText(text);
        stopRotateAnimation(view);
    }

    private void setImuClicked(View view) {
        view.findViewById(R.id.tv_imu).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.iv_scan).setVisibility(View.VISIBLE);
        startRotateAnimation(view);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.ib_back})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
                super.onBackPressed();
                break;

            default:
                break;
        }
    }

    private void startRotateAnimation(View imageView) {
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

    private void stopRotateAnimation(View imageView) {
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
        if (!XXPermissions.hasPermission(this, Permission.SYSTEM_ALERT_WINDOW)) {
            runOnUiThread(() -> {
                if (state == Status.RADAR.NO_TF) {
                    showToast("未插卡，请插入TF卡", DEFAULT_TOAST_DURATION);
                } else if (!isShowNoTFToast()) {
                    xToast.cancel();
                }
            });
        }
    }

    @Override
    public void updateIMU(double eularYaw, double eularPitch, double eularRoll) {

    }

    @Override
    public void updateGPSLocation(double latitude, double longitude) {

    }

    @Override
    public void updateMCUUpdateStatus(int status, String info) {
        if (!XXPermissions.hasPermission(this, Permission.SYSTEM_ALERT_WINDOW)) {
            if (status == 0 || status == 1 || status == 2) {
                runOnUiThread(() -> showToast(info, DEFAULT_TOAST_DURATION));
            }
        }
    }

    @Override
    public void updateAppUpdateStatus(int status, String info) {
        if (!XXPermissions.hasPermission(this, Permission.SYSTEM_ALERT_WINDOW)) {
            if (status == 1 || status == 2 || status == 3) {
                runOnUiThread(() -> showToast(info, DEFAULT_TOAST_DURATION));
            }
        }
    }
}
