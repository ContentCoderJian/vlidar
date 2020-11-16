package com.vlidar.mobile.ui.activity;


import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;

import com.vlidar.mobile.logic.BLESPPUtils;
import com.vlidar.mobile.ui.activity.amap.AbstractMapActivity;

public abstract class AbstractBtActivity extends AbstractMapActivity implements BLESPPUtils.OnBluetoothAction {
    protected BLESPPUtils mBleSppUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBleSppUtils = BLESPPUtils.getInstance(this);
        mBleSppUtils.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mBleSppUtils.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void handleMessages(Message msg) {

    }

    @Override
    public void onFoundDevice(BluetoothDevice device) {
        mLogTrace.d("onFinishFoundDevice", "device=" + device);
    }

//    @Override
//    public void onConnectSuccess(BluetoothDevice device) {
//
//    }

//    @Override
//    public void onConnectFailed(String msg) {
//
//    }

//    @Override
//    public void onReceiveBytes(byte[] bytes) {
//
//    }

//    @Override
//    public void onSendBytes(byte[] bytes) {
//
//    }

    @Override
    public void onFinishFoundDevice() {
        mLogTrace.d("onFinishFoundDevice");
    }
}
