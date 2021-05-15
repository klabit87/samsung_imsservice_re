package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.OptionsReceivedInfo_;

public final class ErrorReason {
    public static final int AUTOMATA_PRESENT = 6;
    public static final int DOES_NOT_EXIST_ANYWHERE = 2;
    public static final int FAILURE_NONE = 0;
    public static final int FAILURE_OTHERS = 9;
    public static final int FORBIDDEN = 8;
    public static final int INVALID_DATA = 7;
    public static final int REQUEST_TIMEOUT = 5;
    public static final int USER_NOT_AVAIABLE = 1;
    public static final int USER_NOT_REACHABLE = 4;
    public static final int USER_NOT_REGISTERED = 3;
    public static final String[] names = {"FAILURE_NONE", "USER_NOT_AVAIABLE", "DOES_NOT_EXIST_ANYWHERE", "USER_NOT_REGISTERED", "USER_NOT_REACHABLE", "REQUEST_TIMEOUT", "AUTOMATA_PRESENT", "INVALID_DATA", "FORBIDDEN", "FAILURE_OTHERS"};

    private ErrorReason() {
    }

    public static String name(int e) {
        return names[e];
    }
}
