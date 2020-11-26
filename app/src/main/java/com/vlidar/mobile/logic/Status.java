package com.vlidar.mobile.logic;

public final class Status {

    public final static class POWER {
        public static final int NOT_WORKING = 0;
        public static final int WORKING = 1;
        public static final int EXCEPTION = 2;
    }

    public final static class RADAR {
        public static final int NO_SCAN = 0;
        public static final int SCANNING = 1;
        public static final int DATA_PROCESSING = 2;
        public static final int INITTING = 3;
        public static final int NO_TF = 4;
    }

    public final static class MCU {
        public static final int UPDATE_SUCCESS = 0;
        public static final int UPDATE_FAILED = 1;
        public static final int UPDATING = 2;
    }

}
