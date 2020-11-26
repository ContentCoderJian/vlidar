package com.vlidar.mobile.ui.activity.amap;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.vlidar.mobile.R;

import butterknife.BindView;

public abstract class AbstractMapActivity extends AbstractCheckPermissionsActivity
        implements LocationSource, AMapLocationListener, AMapGestureListener {
    @BindView(R.id.map)
    protected TextureMapView textureMapView;

    protected AMap aMap;
    protected UiSettings mUiSettings;
    private Marker mPoiMarker;
    private int mZoomLevel = 16; //地图缩放级别，最大缩放级别为20

    private MyLocationStyle myLocationStyle;
    private OnLocationChangedListener mLocationListener;
    protected AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;

    protected LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textureMapView.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textureMapView.setOutlineProvider(new TextureVideoViewOutlineProvider(26));
            textureMapView.setClipToOutline(true);
        }
        initAMap();
    }

    @Override
    protected void onPause() {
        textureMapView.onPause();
        deactivate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        textureMapView.onResume();
        if (mLatLng == null) {
            startLocation();
        }
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
        deactivate();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
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
            // 默认显示zoom级别
            aMap.moveCamera(CameraUpdateFactory.zoomTo((float) mZoomLevel));

            // 定位
//            aMap.setLocationSource(this);
//            aMap.setMyLocationEnabled(true); // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//            setLocationStyle();

            //地图手势事件
            aMap.setAMapGestureListener(this);
        }
    }

    protected void setLatLng(double latitude, double longitude) {
        deactivate();
        mLatLng = new LatLng(latitude, longitude);
        animMap(mLatLng);
        addPOIMarker(mLatLng);
    }

    protected void startLocation() {
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);
        setLocationStyle();
    }

    /**
     * 传入经纬度绘制poi点
     *
     * @param latLng
     */
    private void addPOIMarker(LatLng latLng) {
        if (mPoiMarker == null) {
            MarkerOptions markOptions = new MarkerOptions();
            markOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked));
            markOptions.anchor(0.5f, 0.5f);
            markOptions.position(latLng);
            mPoiMarker = aMap.addMarker(markOptions);
        } else {
            mPoiMarker.setPosition(latLng);
        }
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

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (null == mLocationListener || null == aMapLocation || aMapLocation.getErrorCode() != 0) {
            if (aMapLocation != null) {
                mLogTrace.e("onLocationChanged",
                        "定位失败：errorCode=" + aMapLocation.getErrorCode() + ",errorMsg=" + aMapLocation.getErrorInfo());
            }
            return;
        }
        double lng = aMapLocation.getLongitude();
        double lat = aMapLocation.getLatitude();
        LatLng latLng = new LatLng(lat, lng);
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel),
                new AMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
//                        mLogTrace.d("onLocationChanged",
//                                "longitude: " + aMapLocation.getLongitude() + ", " +
//                                        "latitude: " + aMapLocation.getLatitude() + ", bearing: " + aMapLocation.getBearing());
                        addPOIMarker(latLng);
                        setMarkerRotateAngle(aMapLocation.getBearing());
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationListener = onLocationChangedListener;
//        Log.d(TAG, "activate: mLocationListener = " + mLocationListener + "");
        //设置定位回调监听
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setSensorEnable(true);
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
//            mLocationOption.setInterval(2000);//定位时间间隔
            mLocationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
//            mLocationOption.setLocationCacheEnable(true);//开启定位缓存
            if (null != mLocationClient) {
                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.startLocation();
            }
        }
    }

    @Override
    public void deactivate() {
        mLocationListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
//        mPoiMarker = null;
        mLocationClient = null;
    }

    @Override
    public void onDoubleTap(float v, float v1) {
        mLogTrace.d("onDoubleTap", "v:" + v + ", v1:" + v1);
    }

    @Override
    public void onSingleTap(float v, float v1) {
        mLogTrace.d("onSingleTap", "v:" + v + ", v1:" + v1);
//        if (mLocationClient == null && mLatLng != null) {
        zoomMap();
//        }
    }

    @Override
    public void onFling(float v, float v1) {
        mLogTrace.d("onFling", "v:" + v + ", v1:" + v1);
    }

    @Override
    public void onScroll(float v, float v1) {
        mLogTrace.d("onScroll", "v:" + v + ", v1:" + v1);
    }

    @Override
    public void onLongPress(float v, float v1) {
        mLogTrace.d("onLongPress", "v:" + v + ", v1:" + v1);
    }

    @Override
    public void onDown(float v, float v1) {
    }

    @Override
    public void onUp(float v, float v1) {
    }

    @Override
    public void onMapStable() {
    }

    /**
     * 设置地图类型
     */
    private void setLocationStyle() {
        // 自定义系统定位蓝点
        if (null == myLocationStyle) {
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//圆圈的颜色,设为透明

//            myLocationStyle.strokeColor(STROKE_COLOR);
//            myLocationStyle.radiusFillColor(FILL_COLOR);//圆圈的颜色,设为透明
//            myLocationStyle.strokeWidth(5);
        }
        //定位、且将视角移动到地图中心点，定位点依照设备方向旋转， 并且会跟随设备移动。
        aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE));
    }

    /**
     * 绘制TextureMapView圆角
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

    protected abstract void zoomMap();
}
