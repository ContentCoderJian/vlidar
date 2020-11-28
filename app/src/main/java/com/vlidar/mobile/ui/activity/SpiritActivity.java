package com.vlidar.mobile.ui.activity;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.vlidar.mobile.R;
import com.vlidar.mobile.logic.ProtocolController;
import com.vlidar.mobile.logic.ProtocolObserver;
import com.vlidar.mobile.logic.Status;
import com.vlidar.mobile.ui.view.SpiritLevelView;

import java.util.Locale;

import butterknife.OnClick;

public class SpiritActivity extends BaseActivity implements ProtocolObserver, SensorEventListener {
    //    @BindView(R.id.spiritLevelView)
    SpiritLevelView mSpiritLevelView;
    //    @BindView(R.id.tvv_vertical)
    TextView tvVertical;
    //    @BindView(R.id.tvv_horz)
    TextView tvHorizontal;

    private ProtocolController mProtocolController;

//    private SensorManager sensorManager;

    private float[] accValues = new float[3];
    private float[] magValues = new float[3];
    // 旋转矩阵，用来保存磁场和加速度的数据
    private float r[] = new float[9];
    // 模拟方向传感器的数据（原始数据为弧度）
    private float values[] = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_spirit);
        super.onCreate(savedInstanceState);
        mProtocolController = ProtocolController.getInstance();
        mProtocolController.addObservers(this);
        mSpiritLevelView = findViewById(R.id.spiritLevelView);
        tvVertical = findViewById(R.id.tvv_vertical);
        tvHorizontal = findViewById(R.id.tvv_horz);

//        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
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
        mLogTrace.d("updateIMU",
                "yaw: " + eularYaw + ", pitch: " + eularPitch + ", roll: " + eularRoll);
        runOnUiThread(() -> onAngleChanged(eularRoll, eularPitch, eularYaw));
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

    @Override
    public void onResume() {
        super.onResume();

        onAngleChanged(mProtocolController.getEularRoll(), mProtocolController.getEularPitch(),
                mProtocolController.getEularYaw());

        // 给传感器注册监听：
//        sensorManager.registerListener(this,
//                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_GAME);
//        sensorManager.registerListener(this,
//                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        // 取消方向传感器的监听
//        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        // 取消方向传感器的监听
//        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 获取手机触发event的传感器的类型
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                accValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magValues = event.values.clone();
                break;
        }
        SensorManager.getRotationMatrix(r, null, accValues, magValues);
        SensorManager.getOrientation(r, values);

        // 获取　沿着Z轴转过的角度
        float azimuth = values[0];

        // 获取　沿着X轴倾斜时　与Y轴的夹角
        float pitchAngle = values[1];

        // 获取　沿着Y轴的滚动时　与X轴的角度
        //此处与官方文档描述不一致，所在加了符号（https://developer.android.google
        // .cn/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])）
        float rollAngle = -values[2];

        onAngleChanged(rollAngle, pitchAngle, azimuth);
    }

    /**
     * 角度变更后显示到界面
     *
     * @param rollAngle
     * @param pitchAngle
     * @param azimuth
     */
    @SuppressLint("SetTextI18n")
    private void onAngleChanged(double rollAngle, double pitchAngle, double azimuth) {
        mLogTrace.d("onAngleChanged",
                "rollAngle: " + rollAngle + ", pitchAngle: " + pitchAngle + ", azimuth: " + azimuth);
        if (mSpiritLevelView != null && tvHorizontal != null || tvVertical != null) {
            // 传感器返回的是弧度，直接使用，显示需要转换成角度
            /*mSpiritLevelView.setAngle(rollAngle, pitchAngle, azimuth);
            tvHorizontal.setText((int) Math.toDegrees(rollAngle) + "°");
            tvVertical.setText((int) Math.toDegrees(pitchAngle) + "°");*/

            // 雷达返回的是角度，需要转成弧度绘制
            mSpiritLevelView.setAngle(Math.toRadians(rollAngle), Math.toRadians(pitchAngle),
                    azimuth);
            tvHorizontal.setText(String.format(Locale.CHINESE, "%.1f°", rollAngle));
            tvVertical.setText(String.format(Locale.CHINESE, "%.1f°", pitchAngle));
        }
    }
}
