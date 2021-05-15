package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendEucResponseResponse_;

public final class Status {
    public static final int FAILURE_INTERNAL = 1;
    public static final int FAILURE_NETWORK = 2;
    public static final int SUCCESS = 0;
    public static final String[] names = {"SUCCESS", "FAILURE_INTERNAL", "FAILURE_NETWORK"};

    private Status() {
    }

    public static String name(int e) {
        return names[e];
    }
}
