package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class Result {
    public static final int REQUEST_FAILURE = 1;
    public static final int REQUEST_SUCCESS = 0;
    public static final String[] names = {"REQUEST_SUCCESS", "REQUEST_FAILURE"};

    private Result() {
    }

    public static String name(int e) {
        return names[e];
    }
}
