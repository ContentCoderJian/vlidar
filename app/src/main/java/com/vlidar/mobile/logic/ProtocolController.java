package com.vlidar.mobile.logic;

import android.os.Build;

import com.vlidar.mobile.utils.LogTrace;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 协议解析类
 */
public class ProtocolController {
    private LogTrace mLogTrace = new LogTrace("Protocol", "ProtocolController");
    private static ProtocolController mProtocolController;
    private List<ProtocolObserver> observers = new CopyOnWriteArrayList<>();

    private BlockingQueue queue;
    private ExecutorService singleThreadPool;

    private int powerState;
    private int radarState;
    private int mcuState;
    private double eularYaw; // 偏航角
    private double eularPitch;  // 俯仰角
    private double eularRoll;   // 滚转角
    private double longitude; // 纬度
    private double latitude;   // 经度

    private Runnable handleDataRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    // get data from BlockQueue.
                    String queueString = String.valueOf(queue.take());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        byte[] dataFromBase64 = Base64.getDecoder().decode(queueString);
                        handleData(dataFromBase64);
                    } else {
                        byte[] dataFromHex = hexStringToBytes(queueString);
                        handleData(dataFromHex);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private ProtocolController() {
        singleThreadPool = Executors.newSingleThreadExecutor();
        queue = new ArrayBlockingQueue<Integer>(8);
        singleThreadPool.execute(handleDataRunnable);
    }

    public synchronized static ProtocolController getInstance() {
        if (mProtocolController == null) {
            mProtocolController = new ProtocolController();
        }
        return mProtocolController;
    }

    public void updateData(byte[] data) {
        // insert data string to BlockQueue.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                queue.put(Base64.getEncoder().encodeToString(data));
            } else {
                queue.put(bytesToHexString(data));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleData(byte[] data) {
        mLogTrace.d("handleData", "data: " + Arrays.toString(data));
        int length = data[2];
        int cmdId = data[3];
        switch (cmdId) {
            case Cmd.POWER:
                if (data[4] != powerState) {
                    powerState = data[4];
                    for (ProtocolObserver protocolObserver : observers) {
                        protocolObserver.updatePowerStatus(powerState);
                    }
                }
                break;
            case Cmd.RADAR:
                if (data[4] != radarState) {
                    radarState = data[4];
                    for (ProtocolObserver protocolObserver : observers) {
                        protocolObserver.updateRadarStatus(radarState);
                    }
                }
                break;
            case Cmd.IMU:
                byte[] eularYaws = new byte[8];
                byte[] eularPitchs = new byte[8];
                byte[] eularRolls = new byte[8];
                System.arraycopy(data, 4, eularYaws, 0, eularYaws.length);
                System.arraycopy(data, 12, eularPitchs, 0, eularYaws.length);
                System.arraycopy(data, 20, eularRolls, 0, eularYaws.length);
                eularYaw = bytes2Double(eularYaws);
                eularPitch = bytes2Double(eularPitchs);
                eularRoll = bytes2Double(eularRolls);
                for (ProtocolObserver protocolObserver : observers) {
                    protocolObserver.updateIMU(eularYaw, eularPitch, eularRoll);
                }
                break;
            case Cmd.GPS_LOCATION:
                StringBuffer latitudeBuffer = new StringBuffer();
                latitudeBuffer.append(0xff & data[4]);
                latitudeBuffer.append(".");
                latitudeBuffer.append(0xff & data[5]);
                latitudeBuffer.append(0xff & data[6]);
                latitudeBuffer.append(0xff & data[7]);
                latitudeBuffer.append(0xff & data[8]);

                StringBuffer longitudeBuffer = new StringBuffer();
                longitudeBuffer.append(0xff & data[9]);
                longitudeBuffer.append(".");
                longitudeBuffer.append(0xff & data[10]);
                longitudeBuffer.append(0xff & data[11]);
                latitudeBuffer.append(0xff & data[12]);
                longitudeBuffer.append(0xff & data[13]);

                latitude = Double.parseDouble(latitudeBuffer.toString());
                longitude = Double.parseDouble(longitudeBuffer.toString());
                for (ProtocolObserver protocolObserver : observers) {
                    protocolObserver.updateGPSLocation(latitude, longitude);
                }

                break;
            case Cmd.MCU_UPDATE:
                if (data[4] != mcuState) {
                    mcuState = data[4];
                    for (ProtocolObserver protocolObserver : observers) {
                        protocolObserver.updatePowerStatus(mcuState);
                    }
                }
                break;
            default:
                mLogTrace.w("handleData", "invalid cmdId.");
                break;
        }
    }

    public boolean addObservers(ProtocolObserver protocolObserver) {
        if (!observers.contains(protocolObserver)) {
            return observers.add(protocolObserver);
        }
        return false;
    }

    public boolean removeObservers(ProtocolObserver protocolObserver) {
        return observers.remove(protocolObserver);
    }

    public byte[] sendOnceScan() {
        return new byte[]{0x0D, 0x0A, 0x02, 0x04, 0x03, 0x0D, 0x0A};
    }

    public int getPowerState() {
        return powerState;
    }

    public int getRadarState() {
        return radarState;
    }

    public double getEularYaw() {
        return eularYaw;
    }

    public double getEularPitch() {
        return eularPitch;
    }

    public double getEularRoll() {
        return eularRoll;
    }

    public int getMcuState() {
        return mcuState;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    /**
     * byte[]转化为16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 16进制字符串转化为byte[]
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        double result = Double.longBitsToDouble(value);
        DecimalFormat df2 = new DecimalFormat("0.000000");
        return Double.parseDouble(df2.format(result));
    }
}
