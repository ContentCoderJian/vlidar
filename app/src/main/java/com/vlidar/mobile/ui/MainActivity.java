package com.vlidar.mobile.ui;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vlidar.mobile.R;
import com.vlidar.mobile.utils.LatLngUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends MapActivity {
    @BindView(R.id.ib_up_down)
    ImageButton ibUpDown;
    @BindView(R.id.tv_latitude)
    TextView tvLatitude;
    @BindView(R.id.tv_longitude)
    TextView tvLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO 测试，先用景枫中心经纬度
        setLatLng(31.929672, 118.820083);
        tvLatitude.setText(LatLngUtils.DDtoDMS(31.929672) + "N");
        tvLongitude.setText(LatLngUtils.DDtoDMS(118.820083) + "E");
        ibUpDown.setImageResource(textureMapView.getVisibility() == View.VISIBLE ?
                R.drawable.up : R.drawable.down);
    }

    @OnClick(R.id.ib_up_down)
    void onClick(View view) {
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
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void handleMessages(Message msg) {
    }
}
