package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class CallReason {
    public static final int CALL_FAILURE = 1;
    public static final int CALL_SUCCESS = 0;
    public static final String[] names = {"CALL_SUCCESS", "CALL_FAILURE"};

    private CallReason() {
    }

    public static String name(int e) {
        return names[e];
    }
}
