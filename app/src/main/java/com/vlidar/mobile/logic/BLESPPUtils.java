package com.vlidar.mobile.logic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.provider.Settings;

import com.vlidar.mobile.utils.LogTrace;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.UUID;

/**
 * 蓝牙工具类
 * 功能：搜索蓝牙设备
 * 连接蓝牙串口
 * 发送串口数据
 * 接收串口数据
 */
public class BLESPPUtils {
    private static final LogTrace mLogTrace = new LogTrace("BleSpp", "BLESPPUtils");
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothManager mBluetoothManager;
    private OnBluetoothAction mOnBluetoothAction;
    private ConnectTask mConnectTask = new ConnectTask();
    private static BLESPPUtils mBLESPPUtils;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice device =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (mOnBluetoothAction != null) {
                            mOnBluetoothAction.onFoundDevice(device);
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        if (mOnBluetoothAction != null) {
                            mOnBluetoothAction.onFinishFoundDevice();
                        }
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                        switch (blueState) {
                            case BluetoothAdapter.STATE_ON:
                                mLogTrace.d("Bluetooth adapter is on.");
                                if (mOnBluetoothAction != null) {
                                    mOnBluetoothAction.onBluetoothStateChanged(true);
                                }
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                mLogTrace.d("Bluetooth adapter is off.");
                                if (mOnBluetoothAction != null) {
                                    mOnBluetoothAction.onBluetoothStateChanged(false);
                                }
                                break;
                            default:
                                mLogTrace.d("blueState = " + blueState);
                                break;
                        }
                        break;

                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        BluetoothDevice aclConnectDevice =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        mLogTrace.d("device connected. aclConnectDevice=" + aclConnectDevice);
                        connect(aclConnectDevice);
                        break;

                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        BluetoothDevice aclDisconnectDevice =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        mLogTrace.d("device disconnected. aclDisconnectDevice=" + aclDisconnectDevice);
                        mConnectTask.isRunning = false;
                        mConnectTask.mIsConnectSpp = false;
                        if (mOnBluetoothAction != null) {
                            mOnBluetoothAction.onDisconnected(aclDisconnectDevice);
                        }
                        break;

                    default:
                        break;
                }

            }
        }
    };

    /**
     * 连接任务
     */
    private static class ConnectTask extends AsyncTask<String, Byte[], Void> {
        private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        private BluetoothSocket bluetoothSocket;
        private BluetoothDevice remoteDevice;
        private OnBluetoothAction onBluetoothAction;
        private ProtocolController mProtocolController;
        private boolean isRunning = false;
        private boolean mIsConnectSpp = false;

        @Override
        protected Void doInBackground(String... bluetoothDevicesMac) {
            // 记录标志位，开始运行
            isRunning = true;
            mIsConnectSpp = false;

            // 尝试获取 bluetoothSocket
            try {
                UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                remoteDevice = bluetoothAdapter.getRemoteDevice(bluetoothDevicesMac[0]);
                bluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            } catch (Exception e) {
                mLogTrace.e("doInBackground", "获取Socket失败");
                isRunning = false;
                mIsConnectSpp = false;
                e.printStackTrace();
                return null;
            }

            // 检查有没有获取到
            if (bluetoothSocket == null) {
                if (onBluetoothAction != null) {
                    onBluetoothAction.onConnectFailed("连接失败:获取Socket失败");
                }
                isRunning = false;
                mIsConnectSpp = false;
                return null;
            }

            // 尝试连接
            try {
                // 等待连接，会阻塞线程
                bluetoothSocket.connect();
                mLogTrace.d("doInBackground", "连接成功");
                mIsConnectSpp = true;
                if (onBluetoothAction != null) {
                    onBluetoothAction.onConnectSuccess(remoteDevice);
                }
            } catch (Exception connectException) {
                connectException.printStackTrace();
                mIsConnectSpp = false;
                mLogTrace.e("doInBackground", "连接失败:" + connectException.getMessage());
                if (onBluetoothAction != null) {
                    onBluetoothAction.onConnectFailed("连接失败:" + connectException.getMessage());
                }
                return null;
            }

            // 开始监听数据接收
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                byte[] result = new byte[0];
                while (isRunning) {
                    mLogTrace.d("doInBackground", "looping");
                    byte[] buffer = new byte[256];
                    // 等待有数据
                    while (inputStream.available() == 0 && isRunning) {
                        if (System.currentTimeMillis() < 0) {
                            mLogTrace.d("current time < 0.");
                            break;
                        }
                    }
                    while (isRunning) {
                        try {
                            int num = inputStream.read(buffer);
                            byte[] temp = new byte[result.length + num];
                            System.arraycopy(result, 0, temp, 0, result.length);
                            System.arraycopy(buffer, 0, temp, result.length, num);
                            result = temp;
                            if (inputStream.available() == 0) break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (onBluetoothAction != null) {
                                onBluetoothAction.onConnectFailed("接收数据单次失败：" + e.getMessage());
                            }
                            break;
                        }
                    }
                    try {
                        // 返回数据
                        mLogTrace.d("doInBackground", "当前收到的数据=>" + byte2Hex(result));
                        if (isValidData(result)) {
                            if (onBluetoothAction != null) {
                                onBluetoothAction.onReceiveBytes(result);
                            }
                            mProtocolController.updateData(result);
                            // 清空
                            result = new byte[0];
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (onBluetoothAction != null) {
                            onBluetoothAction.onConnectFailed("验证收到数据结束标志出错：" + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (onBluetoothAction != null) {
                    onBluetoothAction.onConnectFailed("接收数据失败：" + e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            try {
                mLogTrace.d("onCancelled", "开始释放资源");
                isRunning = false;
                mIsConnectSpp = false;
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 发送
         *
         * @param msg 内容
         */
        void send(byte[] msg) {
            try {
                bluetoothSocket.getOutputStream().write(msg);
                if (onBluetoothAction != null) {
                    onBluetoothAction.onSendBytes(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * judge whether the data is valid
         */
        boolean isValidData(byte[] data) {
            if (data == null) {
                return false;
            }
            mLogTrace.d("isValidData, data:" + Arrays.toString(data));
            int length = data.length;
            // mLogTrace.d("isValidData, data.length:" + length);
            int length2 = length - 2;
            int length1 = length - 1;
            // mLogTrace.d("isValidData, data[0]:" + data[0] + ", data[1]:" + data[1] + ",
            // data["
            // + length2 + "]:" + data[length2] + ", data[" + length1 + "]:" + data[length1]);
            return data.length > 4
                    && data[0] == 0x0D
                    && data[1] == 0x0A
                    && data[data.length - 2] == 0x0D
                    && data[data.length - 1] == 0x0A;
        }

        private boolean isValidDataHead(byte[] data) {
            return data != null && data.length > 2
                    && data[0] == 0x0D
                    && data[1] == 0x0A;
        }
    }

    /**
     * 蓝牙活动回调
     */
    public interface OnBluetoothAction {
        /**
         * 蓝牙打开还是关闭状态
         *
         * @param enable
         */
        void onBluetoothStateChanged(boolean enable);

        /**
         * 当发现新设备
         *
         * @param device 设备
         */
        void onFoundDevice(BluetoothDevice device);

        /**
         * 当连接成功
         */
        void onConnectSuccess(BluetoothDevice device);

        /**
         * 当连接失败
         *
         * @param msg 失败信息
         */
        void onConnectFailed(String msg);

        /**
         * 当接收到 byte 数组
         *
         * @param bytes 内容
         */
        void onReceiveBytes(byte[] bytes);

        /**
         * 当调用接口发送了 byte 数组
         *
         * @param bytes 内容
         */
        void onSendBytes(byte[] bytes);

        /**
         * 当结束搜索设备
         */
        void onFinishFoundDevice();

        /**
         * 当断开蓝牙连接
         */
        void onDisconnected(BluetoothDevice bluetoothDevice);
    }

    /**
     * 构造蓝牙工具
     *
     * @param context 上下文
     */
    private BLESPPUtils(Context context) {
        mContext = context;
        mBluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter.getProfileProxy(context, proxyListener, BluetoothProfile.HEADSET);
        mConnectTask.mProtocolController = ProtocolController.getInstance();
    }

    public synchronized static BLESPPUtils getInstance(Context context) {
        if (mBLESPPUtils == null) {
            mBLESPPUtils = new BLESPPUtils(context);
        }
        return mBLESPPUtils;
    }

    public void setOnBluetoothAction(OnBluetoothAction onBluetoothAction) {
        this.mOnBluetoothAction = onBluetoothAction;
        mConnectTask.onBluetoothAction = mOnBluetoothAction;
    }

    /**
     * 初始化
     */
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(mReceiver, filter);
    }

    /**
     * 销毁，释放资源
     */
    public void onDestroy() {
        try {
            mLogTrace.d("onDestroy", "开始释放资源");
            mConnectTask.isRunning = false;
            mConnectTask.cancel(true);
            mContext.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始搜索
     */
    public void startDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * 使用搜索到的数据连接
     *
     * @param device 设备
     */
    public void connect(BluetoothDevice device) {
//        mBluetoothAdapter.cancelDiscovery();
        connect(device.getAddress());
    }

    /**
     * 使用Mac地址来连接
     *
     * @param deviceMac 要连接的设备的 MAC
     */
    public void connect(String deviceMac) {
        if (mConnectTask.getStatus() == AsyncTask.Status.RUNNING && mConnectTask.isRunning) {
            if (mOnBluetoothAction != null) {
                mOnBluetoothAction.onConnectFailed("有正在连接的任务");
            }
            return;
        }
        mConnectTask.onBluetoothAction = mOnBluetoothAction;
        try {
            mConnectTask.execute(deviceMac);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转系统蓝牙设置界面
     */
    public void startBtSettings() {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        mContext.startActivity(intent);
    }

    /**
     * 发送 byte 数组到串口
     *
     * @param bytes 要发送的数据
     */
    public void send(byte[] bytes) {
        if (mConnectTask != null) mConnectTask.send(bytes);
    }

    /**
     * 获取用户是否打开了蓝牙
     */
    public boolean isBluetoothEnable() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 获取当前spp是否已连接
     *
     * @return
     */
    public boolean isSppConnected() {
        return mConnectTask.mIsConnectSpp;
    }

    /**
     * 判断当前蓝牙是否已连接
     *
     * @return
     */
    public boolean isBTConnected() {
        int a2dp = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        return a2dp == BluetoothAdapter.STATE_CONNECTED || headset == BluetoothAdapter.STATE_CONNECTED
                || health == BluetoothAdapter.STATE_CONNECTED;
    }

    private List<BluetoothDevice> getConnectedDevice() {
        return mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
    }

    /**
     * 开启蓝牙
     */
    public void enableBluetooth() {
        mBluetoothAdapter.enable();
    }

    /**
     * 字节转换为 16 进制字符串
     *
     * @param b 字节
     * @return Hex 字符串
     */
    private static String byte2Hex(byte b) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(b));
        if (hex.length() > 2) {
            hex = new StringBuilder(hex.substring(hex.length() - 2));
        }
        while (hex.length() < 2) {
            hex.insert(0, "0");
        }
        return hex.toString();
    }

    /**
     * 字节数组转换为 16 进制字符串
     *
     * @param bytes 字节数组
     * @return Hex 字符串
     */
    private static String byte2Hex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02X", b);
        }
        String hash = formatter.toString();
        formatter.close();
        return hash;
    }

    private BluetoothProfile.ServiceListener proxyListener =
            new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    mLogTrace.d("onServiceConnected", "BluetoothProfile profile: " + profile);
                    if (proxy != null) {
                        List<BluetoothDevice> cd = proxy.getConnectedDevices();
                        if (cd != null) {
                            mLogTrace.d("onServiceConnected", "BluetoothDevice size: " + cd.size());
                            for (int i = 0; i < cd.size(); i++) {
                                BluetoothDevice t = cd.get(i);
                                mLogTrace.d("onServiceConnected",
                                        "BluetoothDevice : " + t.getName() + "|" + t.getAddress());
                            }
                            if (cd.size() > 0) {
                                connect(cd.get(0)); // TODO 目前只支持连一个蓝牙设备的spp，不支持一对多
                            }
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(int profile) {

                }

            };
}
