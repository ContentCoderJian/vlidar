package com.vlidar.mobile.ui.activity.amap;

import android.os.Bundle;
import android.os.Message;

import com.vlidar.mobile.R;
import com.vlidar.mobile.utils.StatusBarUtils;

public class EnLargeMapActivity extends AbstractMapActivity {
    public static String ACTION_LATLNG = "com.vlidar.mobile.action_latlng";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtils.transparencyBar(this);
        StatusBarUtils.changeStatusBar(this, true);
        setContentView(R.layout.activity_enlarge_map);
        super.onCreate(savedInstanceState);
//        mUiSettings.setZoomControlsEnabled(true);
        mLatLng = getIntent().getParcelableExtra(ACTION_LATLNG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLatLng(mLatLng.latitude, mLatLng.longitude);
    }

    @Override
    protected void zoomMap() {
        super.onBackPressed();
    }

    @Override
    protected void handleMessages(Message msg) {

    }
}
