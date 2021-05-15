package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class GeneralReason {
    public static final int REASON_INVALID_VALUE = 1;
    public static final int REASON_NOT_AVAILABLE = 6;
    public static final int REASON_NO_ERROR = 0;
    public static final int REASON_NO_HANDLE = 4;
    public static final int REASON_NULL_EXCEPTION = 3;
    public static final int REASON_UNSUPPORTED_EVENT = 2;
    public static final int REASON_WRONG_OPERATION = 5;
    public static final String[] names = {"REASON_NO_ERROR", "REASON_INVALID_VALUE", "REASON_UNSUPPORTED_EVENT", "REASON_NULL_EXCEPTION", "REASON_NO_HANDLE", "REASON_WRONG_OPERATION", "REASON_NOT_AVAILABLE"};

    private GeneralReason() {
    }

    public static String name(int e) {
        return names[e];
    }
}
