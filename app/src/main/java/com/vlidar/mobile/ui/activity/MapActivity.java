package com.vlidar.mobile.ui.activity;

import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.vlidar.mobile.R;

import butterknife.BindView;

public abstract class MapActivity extends CheckPermissionsActivity {
    @BindView(R.id.map)
    TextureMapView textureMapView;

    private AMap aMap;
    private UiSettings mUiSettings;
    private Marker mPoiMarker;
    private int mZoomLevel = 16; //地图缩放级别，最大缩放级别为20

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textureMapView.onCreate(savedInstanceState);
        textureMapView.setOutlineProvider(new TextureVideoViewOutlineProvider(26));
        textureMapView.setClipToOutline(true);
        initAMap();
    }

    @Override
    protected void onPause() {
        textureMapView.onPause();
        super.onPause();
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mLogTrace.d("onSaveInstanceState");
        textureMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        textureMapView.onDestroy();
        if (mPoiMarker != null) {
            mPoiMarker.destroy();
        }
        super.onDestroy();
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
            mUiSettings = aMap.getUiSettings();
            mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
            mUiSettings.setZoomControlsEnabled(false);
            mUiSettings.setScaleControlsEnabled(false);
            mUiSettings.setAllGesturesEnabled(true);
            mUiSettings.setLogoBottomMargin(-70); //隐藏logo
//            // 默认显示zoom级别
            aMap.moveCamera(CameraUpdateFactory.zoomTo((float) mZoomLevel));

            // 定位
//            mUiSettings.setMyLocationButtonEnabled(true); // 是否显示默认的定位按钮
//            aMap.addOnMyLocationChangeListener(this);
//            aMap.setLocationSource(this);
//            aMap.setMyLocationEnabled(true); // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//            setLocationStyle();

            // poi 点击
//            aMap.setOnPOIClickListener(this);
//            aMap.setOnMarkerClickListener(this);

            //地图手势事件
//            aMap.setAMapGestureListener(this);
        }
    }

    protected void setLatLng(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        animMap(latLng);
        addPOIMarker(latLng);
    }

    /**
     * 传入经纬度绘制poi点
     *
     * @param latLng
     */
    private void addPOIMarker(LatLng latLng) {
        if (mPoiMarker != null) {
            mPoiMarker.destroy();
        }
        MarkerOptions markOptions = new MarkerOptions();
        markOptions.position(latLng);
        markOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked));
        mPoiMarker = aMap.addMarker(markOptions);
    }

    /**
     * 设置旋转角度是以正北为起点,逆时针旋转的.
     * 所以转化成顺时针效果要用360做差.
     *
     * @param angle
     */
    protected void setMarkerRotateAngle(float angle) {
        if (mPoiMarker != null) {
            mPoiMarker.setRotateAngle(angle);
        }
    }

    /**
     * 移动地图中心点到指定位置
     *
     * @param latLng
     */
    private void animMap(LatLng latLng) {
        if (latLng != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel));
        }
    }

    /**
     * 绘制TextureMapView圆角
     */
    protected static class TextureVideoViewOutlineProvider extends ViewOutlineProvider {
        private float mRadius;

        TextureVideoViewOutlineProvider(float radius) {
            this.mRadius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            int leftMargin = 0;
            int topMargin = 0;
            Rect selfRect = new Rect(leftMargin, topMargin,
                    rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
            outline.setRoundRect(selfRect, mRadius);
        }
    }
}
