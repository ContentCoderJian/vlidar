package com.vlidar.mobile.logic;

public interface Cmd {
    int POWER = 0x01;         // soc
    int RADAR = 0x02;         // soc
    int IMU = 0x03;           // soc

    int SCAN = 0x04;          // app
    int TIME_SCAN = 0x05;     // app
    int GPS_LOCATION = 0x06;  // soc
    int MCU_UPDATE = 0x07;           // soc
}
