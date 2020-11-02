package com.vlidar.mobile.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vlidar.mobile.R;
import com.vlidar.mobile.ui.view.CommonButton;
import com.vlidar.mobile.utils.LatLngUtils;
import com.vlidar.mobile.utils.LogTrace;
import com.vlidar.mobile.utils.ScreenUtils;

import java.util.Arrays;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends AbstractBtActivity {
    private static final int DURATION_ANIM = 500;
    @BindView(R.id.ib_up_down)
    ImageButton ibUpDown;
    @BindView(R.id.tv_latitude)
    TextView tvLatitude;
    @BindView(R.id.tv_longitude)
    TextView tvLongitude;
    @BindView(R.id.tv_self_checking_status)
    TextView tvSelfCheckingStatus;
    @BindView(R.id.cb_power)
    CommonButton cbPower;
    @BindView(R.id.cb_radar)
    CommonButton cbRadar;
    @BindView(R.id.cb_bt)
    CommonButton cbBt;
    @BindView(R.id.cb_imu)
    CommonButton cbImu;
    @BindView(R.id.rl_time_scan)
    RelativeLayout rlTimeScan;
    @BindView(R.id.tv_time_scan)
    TextView tvTimeScan;
    @BindView(R.id.rl_map_with_button)
    RelativeLayout rlMapWithButton;

    @BindDimen(R.dimen.main_margin_horizontal)
    int mainMarginHorizontal;
    @BindDimen(R.dimen.map_margin_top)
    int mapMarginTop;

    private int mTranslationY;
    private ObjectAnimator mShowMapObjectAnimator;
    private ObjectAnimator mHideMapObjectAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        initViews();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setImuButtonMarginTop();
        mTranslationY = textureMapView.getMeasuredHeight() + mapMarginTop;
        mLogTrace.d("onWindowFocusChanged", "mTranslationY=" + mTranslationY);
        initAnimation();
    }

    /**
     * 计算出第一行button的列间距，作为该button的marginTop
     */
    private void setImuButtonMarginTop() {
        mLogTrace.d("setImuButtonMarginTop");
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) cbImu.getLayoutParams();
        layoutParams.setMargins(mainMarginHorizontal,
                ((ScreenUtils.getScreenWidth(this) - 3 * cbImu.getMeasuredWidth() - 2 * mainMarginHorizontal) / 2), 0, 0);
        cbImu.setLayoutParams(layoutParams);
    }

    private void initViews() {
        tvLatitude.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        tvLongitude.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        tvSelfCheckingStatus.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        tvTimeScan.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        cbPower.setTitleRes(R.string.power);
        cbPower.setEnabled(false);
        cbPower.setPicBgRes(R.drawable.power_out_selector);
        cbPower.setPicSrcRes(R.drawable.power_in_selector);
        cbPower.setValueText("关");

        cbRadar.setTitleRes(R.string.radar);
        cbRadar.setEnabled(false);
        cbRadar.setPicSrcRes(R.drawable.radar_selector);
        cbRadar.setValueText("关");

        // 蓝牙按钮一直可以点击
        cbBt.setTitleRes(R.string.bt);
        cbBt.setPicSrcRes(R.drawable.bt_selector);
        updateBtStatus();

        cbImu.setTitleRes(R.string.imu);
        cbImu.setEnabled(false);
        cbImu.setPicBgRes(R.drawable.imu_out_selector);
        cbImu.setPicSrcRes(R.drawable.imu_in_selector);
        cbImu.setValueText("关");
    }

    private void updateBtStatus() {
        if (mBleSppUtils.isSppConnected()) {
            cbBt.setActivated(false);
            cbBt.setSelected(true);
            cbBt.setValueText("在线");
        } else if (mBleSppUtils.isBluetoothEnable()) {
            cbBt.setActivated(false);
            cbBt.setSelected(false);
            cbBt.setValueText("离线");
        } else if (!mBleSppUtils.isBluetoothEnable()) {
            cbBt.setActivated(false);
            cbBt.setSelected(false);
            cbBt.setValueText("关闭");
        } else {
            cbBt.setActivated(false);
            cbBt.setSelected(true);
            cbBt.setValueText("故障");
        }
    }

    private void initAnimation() {
        mHideMapObjectAnimator = ObjectAnimator.ofFloat(rlMapWithButton, "translationY",
                -mTranslationY);
        mHideMapObjectAnimator.setDuration(DURATION_ANIM);
        mHideMapObjectAnimator.setInterpolator(new LinearInterpolator());
        mHideMapObjectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLogTrace.d("mHideMapObjectAnimator, onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLogTrace.d("mHideMapObjectAnimator, onAnimationEnd");
                textureMapView.setVisibility(View.INVISIBLE);
                ibUpDown.setImageResource(R.drawable.down);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mLogTrace.d("mHideMapObjectAnimator, onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mLogTrace.d("mHideMapObjectAnimator, onAnimationRepeat");
            }
        });

        mShowMapObjectAnimator = ObjectAnimator.ofFloat(rlMapWithButton, "translationY",
                0);
        mShowMapObjectAnimator.setDuration(DURATION_ANIM);
        mShowMapObjectAnimator.setInterpolator(new LinearInterpolator());
        mShowMapObjectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLogTrace.d("mShowMapObjectAnimator, onAnimationStart");
                textureMapView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLogTrace.d("mShowMapObjectAnimator, onAnimationEnd");
                ibUpDown.setImageResource(R.drawable.up);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mLogTrace.d("mShowMapObjectAnimator, onAnimationCancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mLogTrace.d("mShowMapObjectAnimator, onAnimationRepeat");
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    private void updateViews() {
        // TODO 测试，先用景枫中心经纬度
        setLatLng(31.929672, 118.820083);
        tvLatitude.setText(LatLngUtils.DDtoDMS(31.929672) + "N");
        tvLongitude.setText(LatLngUtils.DDtoDMS(118.820083) + "E");
        setMarkerRotateAngle(-60F);
        ibUpDown.setImageResource(textureMapView.getVisibility() == View.VISIBLE ?
                R.drawable.up : R.drawable.down);
    }

    @OnClick({R.id.rl_show_hide_map, R.id.cb_power, R.id.cb_radar, R.id.cb_bt, R.id.cb_imu,
            R.id.rl_time_scan})
    void onClick(View view) {
        mLogTrace.d("onClick", "view: " + view);
        switch (view.getId()) {
            case R.id.rl_show_hide_map:
                if (textureMapView.getVisibility() == View.VISIBLE) {
                    mLogTrace.d("onClick, hide map.");
                    mHideMapObjectAnimator.start();
                } else {
                    mLogTrace.d("onClick, show map.");
                    mShowMapObjectAnimator.start();
                }
                break;
            case R.id.cb_power:
                cbPower.subViewCallOnClick();
                break;

            case R.id.cb_radar:
                cbRadar.subViewCallOnClick();
                break;

            case R.id.cb_bt:
                cbBt.subViewCallOnClick();
                if (!mBleSppUtils.isSppConnected()) {
                    mBleSppUtils.startBtSettings();
                }
                break;

            case R.id.cb_imu:
                cbImu.subViewCallOnClick();
                break;

            case R.id.rl_time_scan:
//                tvTimeScan.callOnClick();
                break;
            default:
                break;
        }
    }

    @Override
    protected void handleMessages(Message msg) {
    }

    @Override
    public void onBluetoothStateChanged(boolean enable) {
        mLogTrace.d("onBluetoothStateChanged", "enable=" + enable);
        runOnUiThread(() -> {
            updateBtStatus();
        });
    }

    @Override
    public void onConnectSuccess(BluetoothDevice device) {
        mLogTrace.d("onConnectSuccess", "device=" + device);
        runOnUiThread(() -> {
            updateBtStatus();
        });
    }

    @Override
    public void onConnectFailed(String msg) {
        mLogTrace.w("onConnectFailed", "msg=" + msg);
        runOnUiThread(() -> {
            updateBtStatus();
        });
    }

    @Override
    public void onReceiveBytes(byte[] bytes) {
        mLogTrace.i("onReceiveBytes", "bytes=" + Arrays.toString(bytes));
        Toast.makeText(this, "receive data : " + Arrays.toString(bytes), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSendBytes(byte[] bytes) {
        mLogTrace.i("onSendBytes", "bytes=" + Arrays.toString(bytes));
    }

    @Override
    public void onDisconnected(BluetoothDevice bluetoothDevice) {
        runOnUiThread(() -> {
            updateBtStatus();
        });
    }

}
