package com.vlidar.mobile.ui;

import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.TextureMapView;
import com.vlidar.mobile.R;

import butterknife.BindView;

public abstract class MapActivity extends CheckPermissionsActivity {
    @BindView(R.id.map)
    TextureMapView textureMapView;

    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textureMapView.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        textureMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        textureMapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLogTrace.d("onSaveInstanceState");
        textureMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textureMapView.onDestroy();
    }

    /**
     * 初始化AMap对象及监听
     */
    private void initAMap() {
        if (aMap == null) {
            // 初始化显示
            aMap = textureMapView.getMap();
            //显示实时交通
            aMap.setTrafficEnabled(true);
//            mUiSettings = aMap.getUiSettings();
//            mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
//            mUiSettings.setZoomControlsEnabled(false);
//            mUiSettings.setScaleControlsEnabled(true);
////            mUiSettings.setAllGesturesEnabled(true);
//            // 默认显示zoom级别
//            aMap.moveCamera(CameraUpdateFactory.zoomTo(Float.valueOf(mZoomLevel)));

            // 定位
//            mUiSettings.setMyLocationButtonEnabled(true); // 是否显示默认的定位按钮
//            aMap.addOnMyLocationChangeListener(this);
//            aMap.setLocationSource(this);
            aMap.setMyLocationEnabled(true); // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//            setLocationStyle();

            // poi 点击
//            aMap.setOnPOIClickListener(this);
//            aMap.setOnMarkerClickListener(this);

            //地图手势事件
//            aMap.setAMapGestureListener(this);
        }
    }
}
