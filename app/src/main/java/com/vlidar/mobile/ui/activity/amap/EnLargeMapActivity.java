package com.vlidar.mobile.ui.activity.amap;

import android.os.Bundle;
import android.os.Message;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.vlidar.mobile.R;
import com.vlidar.mobile.logic.ProtocolController;
import com.vlidar.mobile.logic.ProtocolObserver;
import com.vlidar.mobile.logic.Status;
import com.vlidar.mobile.utils.StatusBarUtils;

public class EnLargeMapActivity extends AbstractMapActivity implements ProtocolObserver {
    public static String ACTION_LATLNG = "com.vlidar.mobile.action_latlng";

    private ProtocolController mProtocolController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.transparencyBar(this);
        StatusBarUtils.changeStatusBar(this, true);
        setContentView(R.layout.activity_enlarge_map);
        super.onCreate(savedInstanceState);
//        mUiSettings.setZoomControlsEnabled(true);
        mLatLng = getIntent().getParcelableExtra(ACTION_LATLNG);
        mProtocolController = ProtocolController.getInstance();
        mProtocolController.addObservers(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLatLng != null) {
            setLatLng(mLatLng.latitude, mLatLng.longitude);
            setMarkerRotateAngle((float) mProtocolController.getEularYaw());
        }
    }

    @Override
    protected void zoomMap() {
        super.onBackPressed();
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
        runOnUiThread(() -> {
            if (mProtocolController.getLatitude() != 0 && mProtocolController.getLongitude() != 0) {
                setMarkerRotateAngle((float) eularYaw);
            }
        });
    }

    @Override
    public void updateGPSLocation(double latitude, double longitude) {
        runOnUiThread(() -> {
            setLatLng(latitude, longitude);
        });
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
