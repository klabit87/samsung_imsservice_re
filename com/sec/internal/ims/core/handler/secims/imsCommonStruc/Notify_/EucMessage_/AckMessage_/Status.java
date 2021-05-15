package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.EucMessage_.AckMessage_;

public final class Status {
    public static final int ERROR = 1;
    public static final int OK = 0;
    public static final String[] names = {"OK", "ERROR"};

    private Status() {
    }

    public static String name(int e) {
        return names[e];
    }
}
