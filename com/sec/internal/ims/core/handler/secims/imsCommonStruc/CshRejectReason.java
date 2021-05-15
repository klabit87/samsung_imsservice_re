package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class CshRejectReason {
    public static final int BUSY = 2;
    public static final int DEVICE_FAILED = 3;
    public static final int NO_ANSWER = 1;
    public static final int USER = 0;
    public static final String[] names = {"USER", "NO_ANSWER", "BUSY", "DEVICE_FAILED"};

    private CshRejectReason() {
    }

    public static String name(int e) {
        return names[e];
    }
}
