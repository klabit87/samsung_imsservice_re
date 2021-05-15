package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

public final class BearerState {
    public static final int CLOSED = 3;
    public static final int ESTABLISHED = 1;
    public static final int MODIFIED = 2;
    public static final int UN = 0;
    public static final String[] names = {"UN", "ESTABLISHED", "MODIFIED", "CLOSED"};

    private BearerState() {
    }

    public static String name(int e) {
        return names[e];
    }
}
