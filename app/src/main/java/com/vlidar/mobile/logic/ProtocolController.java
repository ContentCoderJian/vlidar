package com.vlidar.mobile.logic;

import android.os.Build;
import android.os.Handler;

import com.vlidar.mobile.ui.entity.ScanTimer;
import com.vlidar.mobile.utils.LogTrace;

import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
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
    private List<ScanTimer> scanTimers = new ArrayList<>();

    private BlockingQueue queue;
    private ExecutorService singleThreadPool;

    private int powerState;
    private int radarState;
    private int mcuState;
    private int appUpdateState;
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

        LitePal.getDatabase();
        getScanTimes();
        mLogTrace.d("init", "scanTimers: " + scanTimers);
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

    public void text() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateData(new byte[]{0x0D, 0x0A, 0x02, 0x02, 0x04, 0x0D, 0x0A});
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateData(new byte[]{0x0D, 0x0A, 0x02, 0x08, 0x02, 0x0D, 0x0A});
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateData(new byte[]{0x0D, 0x0A, 0x02, 0x08, 0x03, 0x0D, 0x0A});
                            }
                        }, 800);
                    }
                }, 800);
            }
        }, 1000);
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
//                if (data[4] != radarState) {
                radarState = data[4];
                for (ProtocolObserver protocolObserver : observers) {
                    protocolObserver.updateRadarStatus(radarState);
                }
//                }
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
                longitudeBuffer.append(0xff & data[12]);
                longitudeBuffer.append(0xff & data[13]);

                latitude = Double.parseDouble(latitudeBuffer.toString());
                longitude = Double.parseDouble(longitudeBuffer.toString());
                for (ProtocolObserver protocolObserver : observers) {
                    protocolObserver.updateGPSLocation(latitude, longitude);
                }

                break;
            case Cmd.MCU_UPDATE:
//                if (data[4] != mcuState) {
                mcuState = data[4];
                for (ProtocolObserver protocolObserver : observers) {
                    protocolObserver.updateMCUUpdateStatus(mcuState,
                            getMCUpdateStatus(mcuState));
                }
//                }
                break;
            case Cmd.APP_UPDATE:
//                if (data[4] != appUpdateState) {
                appUpdateState = data[4];
                for (ProtocolObserver protocolObserver : observers) {
                    protocolObserver.updateAppUpdateStatus(appUpdateState,
                            getAppUpdateStatus(appUpdateState));
                }
//                }
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

    public List<ScanTimer> getScanTimes() {
        scanTimers = LitePal.findAll(ScanTimer.class);
        mLogTrace.d("getScanTimes", "scanTimers: " + scanTimers);
        resetScanTimers();
        mLogTrace.d("getScanTimes", "reset end, scanTimers: " + scanTimers);
        sortScanTimes();
        return scanTimers;
    }

    private void resetScanTimers() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
//        int currentYear = calendar.get(Calendar.YEAR) - 2000;
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentSecond = calendar.get(Calendar.SECOND);
        Iterator<ScanTimer> iterator = scanTimers.iterator();
        while (iterator.hasNext()) {
            ScanTimer scanTimer = iterator.next();
            if (scanTimer.getBeginMonth() < currentMonth // 月小于
                    || scanTimer.getBeginMonth() == currentMonth && scanTimer.getBeginDay() < currentDay  // 日小于
                    || scanTimer.getBeginMonth() == currentMonth && scanTimer.getBeginDay() == currentDay
                    && (scanTimer.getBeginHour() < currentHour) // 同一天，小时小于
                    || (scanTimer.getBeginHour() == currentHour && scanTimer.getBeginMinute() < currentMinute)  // 同一天同小时，分钟小于
                    || ((scanTimer.getBeginHour() == currentHour && scanTimer.getBeginMinute() == currentMinute) && scanTimer.getBeginSecond() < currentSecond - 5)) // 同一天同小时，秒钟小于当前秒钟-5 {
                scanTimer.delete();
                iterator.remove();
        }
    }

    private void sortScanTimes() {
        Collections.sort(scanTimers, (o1, o2) -> {
            if (o1.getBeginMonth() != o2.getBeginMonth()) {
                return o1.getBeginMonth() - o2.getBeginMonth();
            } else if (o1.getBeginDay() != o2.getBeginDay()) {
                return o1.getBeginDay() - o2.getBeginDay();
            } else if (o1.getBeginHour() != o2.getBeginHour()) {
                return o1.getBeginHour() - o2.getBeginHour();
            } else if (o1.getBeginMinute() != o2.getBeginMinute()) {
                return o1.getBeginMinute() - o2.getBeginMinute();
            } else if (o1.getBeginSecond() != o2.getBeginSecond()) {
                return o1.getBeginSecond() - o2.getBeginSecond();
            } else if (o1.getTimes() != o2.getTimes()) {
                return o1.getTimes() - o2.getTimes();
            } else if (o1.getIntervalHour() != o2.getIntervalHour()) {
                return o1.getIntervalHour() - o2.getIntervalHour();
            } else if (o1.getIntervalMinute() != o2.getIntervalMinute()) {
                return o1.getIntervalMinute() - o2.getIntervalMinute();
            }
            return 0;
        });
    }

    public byte[] sendOnceScan(byte scanMode) {
        byte[] payload = new byte[]{0x0D, 0x0A, 0x02, 0x04, scanMode, 0x0D, 0x0A};
        mLogTrace.d("sendOnceScan", "payload: " + Arrays.toString(payload));
        return payload;
    }

    public byte[] sendScanTimer() {
        int realSize = 0;
        for (ScanTimer scanTimer : scanTimers) {
            if (scanTimer.isChecked()) {
                ++realSize;
            }
        }
        byte[] payload = new byte[realSize * 8 + 7];
        payload[0] = 0x0D;
        payload[1] = 0x0A;
        payload[2] = (byte) ((realSize * 8 + 2) & 0xFF);
        payload[3] = Cmd.TIME_SCAN;
        payload[4] = (byte) (realSize & 0xFF);
        payload[payload.length - 2] = 0x0D;
        payload[payload.length - 1] = 0x0A;
        int index = 5;
        for (ScanTimer scanTimer : scanTimers) {
            if (!scanTimer.isChecked()) {
                continue;
            }
            payload[index++] = (byte) scanTimer.getBeginMonth();
            payload[index++] = (byte) scanTimer.getBeginDay();
            payload[index++] = (byte) scanTimer.getBeginHour();
            payload[index++] = (byte) scanTimer.getBeginMinute();
            payload[index++] = (byte) scanTimer.getBeginSecond();
            payload[index++] = (byte) scanTimer.getIntervalHour();
            payload[index++] = (byte) scanTimer.getIntervalMinute();
            payload[index++] = (byte) scanTimer.getTimes();
        }
        mLogTrace.d("sendScanTimer", "payload: " + Arrays.toString(payload));
        return payload;
    }

    public byte[] sendCurrentTime() {
        byte[] payload = new byte[12];
        payload[0] = 0x0D;
        payload[1] = 0x0A;
        payload[2] = 7;
        payload[3] = Cmd.TIME_UPDATE;
        payload[payload.length - 2] = 0x0D;
        payload[payload.length - 1] = 0x0A;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        payload[4] = (byte) (calendar.get(Calendar.YEAR) - 2000);
        payload[5] = (byte) (calendar.get(Calendar.MONTH) + 1);
        payload[6] = (byte) (calendar.get(Calendar.DAY_OF_MONTH));
        payload[7] = (byte) (calendar.get(Calendar.HOUR_OF_DAY));
        payload[8] = (byte) (calendar.get(Calendar.MINUTE));
        payload[9] = (byte) (calendar.get(Calendar.SECOND));
        mLogTrace.d("sendCurrentTime", "payload: " + Arrays.toString(payload));
        return payload;
    }

    private String getMCUpdateStatus(int status) {
        switch (status) {
            case 0:
                return "MCU升级成功";
            case 1:
                return "MCU升级失败";
            case 2:
                return "MCU升级中，切勿断电";
            default:
                return "";
        }
    }

    private String getAppUpdateStatus(int status) {
        switch (status) {
            case 1:
                return "APP升级成功";
            case 2:
                return "APP升级失败";
            case 3:
                return "Updater升级成功";
            default:
                return "";
        }
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
