package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class RegiReason {
    public static final int ERROR_RESP = 3;
    public static final int INVALID_DATA = 1;
    public static final int NO_ERROR = 0;
    public static final int UNSUPPORTED_EVENT = 2;
    public static final String[] names = {"NO_ERROR", "INVALID_DATA", "UNSUPPORTED_EVENT", "ERROR_RESP"};

    private RegiReason() {
    }

    public static String name(int e) {
        return names[e];
    }
}
