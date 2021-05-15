package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class CshError {
    public static final int CSH_CAM_ERROR = 7;
    public static final int CSH_CANCELLED = 3;
    public static final int CSH_FORBIDDEN = 5;
    public static final int CSH_REJECTED = 4;
    public static final int CSH_SUCCESS = 0;
    public static final int CSH_TEMPORAIRLY_NOT_AVAILABLE = 2;
    public static final int CSH_TIMEOUT = 6;
    public static final int CSH_USER_BUSY = 1;
    public static final String[] names = {"CSH_SUCCESS", "CSH_USER_BUSY", "CSH_TEMPORAIRLY_NOT_AVAILABLE", "CSH_CANCELLED", "CSH_REJECTED", "CSH_FORBIDDEN", "CSH_TIMEOUT", "CSH_CAM_ERROR"};

    private CshError() {
    }

    public static String name(int e) {
        return names[e];
    }
}
