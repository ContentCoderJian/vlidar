package com.vlidar.wheelview.common;

/**
 * 滚轮异常类
 */
public class WheelViewException extends RuntimeException {

    public WheelViewException() {
        super();
    }

    public WheelViewException(String detailMessage) {
        super(detailMessage);
    }

    public WheelViewException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public WheelViewException(Throwable throwable) {
        super(throwable);
    }
}
