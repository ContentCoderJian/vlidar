package com.vlidar.mobile.logic;

/**
 * 监视协议状态改变，通知更新UI
 */
public interface ProtocolObserver {
    void updatePowerStatus(int state);

    void updateRadarStatus(int state);

    void updateIMU(double eularYaw, double eularPitch, double eularRoll);

    void updateGPSLocation(double latitude, double longitude);

    void updateMCUUpdateStatus(int status);
}
