package com.vlidar.mobile.ui.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vlidar.mobile.R;
import com.vlidar.mobile.ui.view.CommonButton;
import com.vlidar.mobile.utils.LatLngUtils;
import com.vlidar.mobile.utils.ScreenUtils;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends MapActivity {
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

    @BindDimen(R.dimen.main_margin_horizontal)
    int mainMarginHorizontal;

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
//        cbBt.setActivated(true); // 故障
//        cbBt.setSelected(true);  // 在线
        cbBt.setValueText("离线");

        cbImu.setTitleRes(R.string.imu);
        cbImu.setEnabled(false);
        cbImu.setPicBgRes(R.drawable.imu_out_selector);
        cbImu.setPicSrcRes(R.drawable.imu_in_selector);
        cbImu.setValueText("关");
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

    @OnClick({R.id.ib_up_down, R.id.cb_power, R.id.cb_radar, R.id.cb_bt, R.id.cb_imu,
            R.id.rl_time_scan})
    void onClick(View view) {
        mLogTrace.d("onClick", "view: " + view);
        switch (view.getId()) {
            case R.id.ib_up_down:
                if (textureMapView.getVisibility() == View.VISIBLE) {
                    textureMapView.setVisibility(View.GONE);
                    ibUpDown.setImageResource(R.drawable.down);
                } else {
                    textureMapView.setVisibility(View.VISIBLE);
                    ibUpDown.setImageResource(R.drawable.up);
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

}
