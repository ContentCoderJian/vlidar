package com.vlidar.mobile.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vlidar.mobile.R;
import com.vlidar.mobile.logic.ProtocolController;
import com.vlidar.mobile.logic.ProtocolObserver;
import com.vlidar.mobile.logic.Status;
import com.vlidar.mobile.ui.activity.amap.EnLargeMapActivity;
import com.vlidar.mobile.ui.entity.ScanTimer;
import com.vlidar.mobile.ui.view.CommonButton;
import com.vlidar.mobile.utils.AppTools;
import com.vlidar.mobile.utils.LatLngUtils;
import com.vlidar.mobile.utils.ScreenUtils;

import java.util.Arrays;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends AbstractBtActivity implements ProtocolObserver {
    private static final int DURATION_ANIM = 350;
    @BindView(R.id.ib_up_down)
    ImageButton ibUpDown;
    @BindView(R.id.tv_latitude)
    TextView tvLatitude;
    @BindView(R.id.tv_longitude)
    TextView tvLongitude;
    //    @BindView(R.id.tv_self_checking_status)
//    TextView tvSelfCheckingStatus;
    @BindView(R.id.cb_power)
    CommonButton cbPower;
    @BindView(R.id.cb_radar)
    CommonButton cbRadar;
    @BindView(R.id.cb_bt)
    CommonButton cbBt;
    @BindView(R.id.cb_imu)
    CommonButton cbImu;
    @BindView(R.id.cb_timer_scan)
    CommonButton cbTimerScan;
    @BindView(R.id.rl_scan_once)
    RelativeLayout rlScanOnce;
    @BindView(R.id.tv_scan_once)
    TextView tvScanOnce;
    @BindView(R.id.rl_map_with_button)
    RelativeLayout rlMapWithButton;
    @BindView(R.id.tv_no_gps)
    TextView tvNoGps;

    @BindDimen(R.dimen.main_margin_horizontal)
    int mainMarginHorizontal;
    @BindDimen(R.dimen.map_margin_top)
    int mapMarginTop;

    private int mTranslationY;
    private ObjectAnimator mShowMapObjectAnimator;
    private ObjectAnimator mHideMapObjectAnimator;

    private ProtocolController mProtocolController;

    private int mSendTimeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        initViews();
        mBleSppUtils.setOnBluetoothAction(this);
        mProtocolController = ProtocolController.getInstance();
        mProtocolController.addObservers(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setButtonMarginTop();
        mTranslationY = textureMapView.getMeasuredHeight() + mapMarginTop;
        mLogTrace.d("onWindowFocusChanged", "mTranslationY=" + mTranslationY);
        initAnimation();
    }

    /**
     * 计算出第一行button的列间距，作为该button的marginTop
     */
    private void setButtonMarginTop() {
        mLogTrace.d("setImuButtonMarginTop");
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) cbImu.getLayoutParams();
        layoutParams.setMargins(mainMarginHorizontal,
                ((ScreenUtils.getScreenWidth(this) - 3 * cbImu.getMeasuredWidth() - 2 * mainMarginHorizontal) / 2), 0, 0);
        cbImu.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams layoutParams1 =
                (RelativeLayout.LayoutParams) cbTimerScan.getLayoutParams();
        layoutParams1.setMargins(0,
                ((ScreenUtils.getScreenWidth(this) - 3 * cbTimerScan.getMeasuredWidth() - 2 * mainMarginHorizontal) / 2), 0, 0);
        cbTimerScan.setLayoutParams(layoutParams1);
    }

    private void initViews() {
        tvLatitude.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
        tvLongitude.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
                "-Regular" +
                ".ttf"));
//        tvSelfCheckingStatus.setTypeface(Typeface.createFromAsset(getAssets(),
//        "fonts/PingFangSC" +
//                "-Regular" +
//                ".ttf"));
        tvScanOnce.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/PingFangSC" +
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

        cbTimerScan.setTitleRes(R.string.time_scan);
        cbTimerScan.setEnabled(true);
        cbTimerScan.setPicSrcRes(R.drawable.timer_scan_selector);
        cbTimerScan.setValueText("关");
    }

    private void updateTimerScanStatus() {
        if (mProtocolController != null && mProtocolController.getScanTimes() != null) {
            for (ScanTimer scanTimer : mProtocolController.getScanTimes()) {
                if (scanTimer.isChecked()) {
                    cbTimerScan.setSelected(true);
                    cbTimerScan.setValueText("开");
                    return;
                }
            }
        }
        cbTimerScan.setSelected(false);
        cbTimerScan.setValueText("关");
    }

    private void updateBtStatus() {
        if (mBleSppUtils.isSppConnected()) {
            rlScanOnce.setEnabled(true);
            tvScanOnce.setEnabled(true);
            cbBt.setActivated(false);
            cbBt.setSelected(true);
            cbBt.setValueText("在线");
//            cbBt.startTwinkleAnim();
            cbRadar.setEnabled(true);
            cbRadar.setValueText("开");
            updateTimerScanStatus();
        } else if (mBleSppUtils.isBluetoothEnable()) {
//            cbBt.stopTwinkleAnim();
            cbBt.setActivated(false);
            cbBt.setSelected(false);
            cbBt.setValueText("离线");
            cbImu.setEnabled(false);
            cbImu.setValueText("关");
            cbImu.setActivated(false);
            cbImu.setValue1Text(null);
            rlScanOnce.setEnabled(false);
            tvScanOnce.setEnabled(false);
            cbRadar.setEnabled(false);
            cbRadar.setValueText("关");
            cbTimerScan.setSelected(false);
            cbTimerScan.setValueText("关");
            resetNoGps();
            updatePower(Status.POWER.NOT_WORKING);
            updateRadar(Status.RADAR.NO_SCAN);
        } else if (!mBleSppUtils.isBluetoothEnable()) {
//            cbBt.stopTwinkleAnim();
            cbBt.setActivated(false);
            cbBt.setSelected(false);
            cbBt.setValueText("关闭");
            cbImu.setEnabled(false);
            cbImu.setValueText("关");
            cbImu.setActivated(false);
            cbImu.setValue1Text(null);
            rlScanOnce.setEnabled(false);
            tvScanOnce.setEnabled(false);
            cbTimerScan.setSelected(false);
            cbTimerScan.setValueText("关");
            cbRadar.setEnabled(false);
            cbRadar.setValueText("关");
            resetNoGps();
            updatePower(Status.POWER.NOT_WORKING);
            updateRadar(Status.RADAR.NO_SCAN);
        } else {
//            cbBt.stopTwinkleAnim();
            cbBt.setActivated(false);
            cbBt.setSelected(true);
            cbBt.setValueText("故障");
            cbImu.setEnabled(false);
            cbImu.setValueText("关");
            cbImu.setActivated(false);
            cbImu.setValue1Text(null);
            rlScanOnce.setEnabled(false);
            tvScanOnce.setEnabled(false);
            cbTimerScan.setSelected(false);
            cbTimerScan.setValueText("关");
            cbRadar.setEnabled(false);
            cbRadar.setValueText("关");
            resetNoGps();
            updatePower(Status.POWER.NOT_WORKING);
            updateRadar(Status.RADAR.NO_SCAN);
        }
    }

    private void resetNoGps() {
        tvNoGps.setVisibility(View.VISIBLE);
        tvLongitude.setVisibility(View.INVISIBLE);
        tvLatitude.setVisibility(View.INVISIBLE);
        tvLatitude.setText("0°0'0\"N");
        tvLongitude.setText("0°0'0\"E");
    }

    private void updatePower(int status) {
        switch (status) {
            case Status.POWER.NOT_WORKING:
                cbPower.setActivated(false);
                cbPower.setSelected(false);
                cbPower.setValueText("关");
                cbPower.stopRotateAnim();
                break;
            case Status.POWER.WORKING:
                cbPower.setActivated(true);
                cbPower.setSelected(false);
                cbPower.setValueText("开");
                cbPower.startRotateAnim();
                break;
            case Status.POWER.EXCEPTION:
                cbPower.setSelected(true);
                cbPower.setActivated(false);
                cbPower.setValueText("异常");
                cbPower.stopRotateAnim();
                break;
            default:
                mLogTrace.d("updatePower", "invalid status, " + status);
                break;
        }
    }

    private void updateRadar(int status) {
        switch (status) {
            case Status.RADAR.NO_SCAN:
                rlScanOnce.setEnabled(mBleSppUtils.isSppConnected());
                tvScanOnce.setEnabled(mBleSppUtils.isSppConnected());
                cbRadar.setActivated(false);
                cbRadar.setEnabled(mBleSppUtils.isSppConnected());
                cbRadar.setSelected(false);
                cbRadar.setValueText("停止");
                cbRadar.stopRotateAnim();
                break;
            case Status.RADAR.SCANNING:
                cbRadar.setEnabled(true);
                rlScanOnce.setEnabled(false);
                tvScanOnce.setEnabled(false);
                cbRadar.setActivated(true);
                cbRadar.setSelected(false);
                cbRadar.setValueText("扫描中");
                cbRadar.startRotateAnim();
                break;

            case Status.RADAR.DATA_PROCESSING:
                cbRadar.setEnabled(true);
                rlScanOnce.setEnabled(false);
                tvScanOnce.setEnabled(false);
                cbRadar.setActivated(true);
                cbRadar.setSelected(false);
                cbRadar.setValueText("数据处理中");
                cbRadar.startRotateAnim();
                break;

            case Status.RADAR.INITTING:
                rlScanOnce.setEnabled(false);
                tvScanOnce.setEnabled(false);
                cbRadar.setEnabled(false);
                cbRadar.setActivated(true);
                cbRadar.setSelected(false);
                cbRadar.setValueText("初始化中");
                cbRadar.stopRotateAnim();
                break;

            case Status.RADAR.NO_TF:
                rlScanOnce.setEnabled(false);
                tvScanOnce.setEnabled(false);
                cbRadar.setEnabled(false);
                cbRadar.setActivated(false);
                cbRadar.setSelected(false);
                cbRadar.setValueText("未插卡");
                showToast("未插卡，请插入TF卡", DEFAULT_TOAST_DURATION);
                cbRadar.stopRotateAnim();
                break;
            default:
                mLogTrace.d("updateRadar", "invalid status, " + status);
                break;
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
            @SuppressLint("SetTextI18n")
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProtocolController.removeObservers(this);
    }

    @Override
    protected void zoomMap() {
        if (mBleSppUtils.isSppConnected() && mProtocolController.getLatitude() != 0 && mProtocolController.getLongitude() != 0) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EnLargeMapActivity.ACTION_LATLNG, mLatLng);
            AppTools.startForwardActivity(this, EnLargeMapActivity.class, bundle, false);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateViews() {
        // TODO 测试，先用景枫中心经纬度
//        setLatLng(31.929672, 118.820083);
//        tvLatitude.setText(LatLngUtils.DDtoDMS(31.929672) + "N");
//        tvLongitude.setText(LatLngUtils.DDtoDMS(118.820083) + "E");
//        setMarkerRotateAngle(-60F);
        if (mBleSppUtils.isSppConnected() && mProtocolController.getLatitude() != 0 && mProtocolController.getLongitude() != 0) {
            tvNoGps.setVisibility(View.INVISIBLE);
            setLatLng(mProtocolController.getLatitude(), mProtocolController.getLongitude());
            tvLatitude.setText(LatLngUtils.DDtoDMS(mProtocolController.getLatitude()) + "N");
            tvLongitude.setText(LatLngUtils.DDtoDMS(mProtocolController.getLongitude()) + "E");
            tvLatitude.setVisibility(View.VISIBLE);
            tvLongitude.setVisibility(View.VISIBLE);
        } else {
            resetNoGps();
        }
        ibUpDown.setImageResource(textureMapView.getVisibility() == View.VISIBLE ? R.drawable.up
                : R.drawable.down);

        updateBtStatus();
        if (mBleSppUtils.isSppConnected()) {
            updatePower(mProtocolController.getPowerState());
            updateRadar(mProtocolController.getRadarState());
            if (mProtocolController.getEularYaw() != 0 || mProtocolController.getEularPitch() != 0 || mProtocolController.getEularRoll() != 0) {
                cbImu.setValueText("开");
                cbImu.setEnabled(true);
                cbImu.setActivated(true);
                cbImu.setValue1Text("yaw: " + mProtocolController.getEularYaw() + "\npitch: " + mProtocolController.getEularPitch() + "\nroll: " + mProtocolController.getEularRoll());
            }
            updateTimerScanStatus();
        }
    }

    @OnClick({R.id.rl_show_hide_map, R.id.cb_power, R.id.cb_radar, R.id.cb_bt, R.id.cb_imu,
            R.id.cb_timer_scan, R.id.rl_scan_once})
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
                AppTools.startForwardActivity(this, RadarActivity.class);
                break;

            case R.id.cb_bt:
                cbBt.subViewCallOnClick();
//                mProtocolController.text();
                mBleSppUtils.startBtSettings();
                break;

            case R.id.cb_imu:
                cbImu.subViewCallOnClick();
                AppTools.startForwardActivity(this, SpiritActivity.class);
                break;

            case R.id.cb_timer_scan:
                cbTimerScan.subViewCallOnClick();
                AppTools.startForwardActivity(this, TimerActivity.class);
                break;

            case R.id.rl_scan_once:
//                tvTimeScan.callOnClick();
                mBleSppUtils.send(mProtocolController.sendOnceScan((byte) 0x03));
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
        runOnUiThread(this::updateBtStatus);
    }

    @Override
    public void onConnectSuccess(BluetoothDevice device) {
        mLogTrace.d("onConnectSuccess", "device=" + device);
        runOnUiThread(this::updateBtStatus);
        mSendTimeCount = 0;
        new Thread(() -> {
            while (mSendTimeCount <= 2) {
                mBleSppUtils.send(mProtocolController.sendCurrentTime());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mSendTimeCount++;
            }
            mProtocolController.getScanTimes();
            mBleSppUtils.send(mProtocolController.sendScanTimer());
        }).start();
    }

    @Override
    public void onConnectFailed(String msg) {
        mLogTrace.w("onConnectFailed", "msg=" + msg);
        runOnUiThread(() -> {
            updateBtStatus();
            showToast(msg, DEFAULT_TOAST_DURATION);
        });
    }

    @Override
    public void onReceiveBytes(final byte[] bytes) {
//        mLogTrace.i("onReceiveBytes", "bytes=" + Arrays.toString(bytes));
//        runOnUiThread(() -> Toast.makeText(MainActivity.this, "receive data : " + Arrays
//        .toString(bytes),
//                Toast.LENGTH_SHORT).show());
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

    @Override
    public void updatePowerStatus(int state) {
        runOnUiThread(() -> updatePower(state));
    }

    @Override
    public void updateRadarStatus(int state) {
        mLogTrace.d("updateRadarStatus, state: " + state);
        runOnUiThread(() -> {
            updateRadar(state);
            if (state != Status.RADAR.NO_TF && isShowNoTFToast()) {
                xToast.cancel();
            }
        });
    }

    @Override
    public void updateIMU(double eularYaw, double eularPitch, double eularRoll) {
//        mLogTrace.d("updateIMU", "eularYaw: " + eularYaw + ", eularPitch: " + eularPitch + ", " +
//                "eularRoll: " + eularRoll);
        runOnUiThread(() -> {
            cbImu.setValueText("开");
            cbImu.setEnabled(true);
            cbImu.setActivated(true);
            cbImu.setValue1Text("yaw: " + eularYaw + "\npitch: " + eularPitch + "\nroll: " + eularRoll);
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateGPSLocation(double latitude, double longitude) {
        mLogTrace.d("updateGPSLocation", "latitude:" + latitude + ", longitude: " + longitude);
        runOnUiThread(() -> {
            tvNoGps.setVisibility(View.INVISIBLE);
            setLatLng(latitude, longitude);
            tvLatitude.setText(LatLngUtils.DDtoDMS(latitude) + "N");
            tvLongitude.setText(LatLngUtils.DDtoDMS(longitude) + "E");
            tvLatitude.setVisibility(View.VISIBLE);
            tvLongitude.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void updateMCUUpdateStatus(int status, String info) {
        mLogTrace.d("updateMCUUpdateStatus, status: " + status + ", info:" + info);
        if (status == 0 || status == 1 || status == 2) {
            runOnUiThread(() -> showToast(info, DEFAULT_TOAST_DURATION));
        }
    }

    @Override
    public void updateAppUpdateStatus(int status, String info) {
        mLogTrace.d("updateAppUpdateStatus, status: " + status + ", info:" + info);
        if (status == 1 || status == 2 || status == 3) {
            runOnUiThread(() -> showToast(info, DEFAULT_TOAST_DURATION));
        }
    }

}
