package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class ImSessionType {
    public static final int SESSION_TYPE_EXTEND_TO_GROUP = 1;
    public static final int SESSION_TYPE_NORMAL = 0;
    public static final int SESSION_TYPE_REJOIN = 2;
    public static final int SESSION_TYPE_RESTART = 3;
    public static final String[] names = {"SESSION_TYPE_NORMAL", "SESSION_TYPE_EXTEND_TO_GROUP", "SESSION_TYPE_REJOIN", "SESSION_TYPE_RESTART"};

    private ImSessionType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
