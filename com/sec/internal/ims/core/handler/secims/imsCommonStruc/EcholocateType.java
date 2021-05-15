package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class EcholocateType {
    public static final int RTPMSG = 1;
    public static final int SIGNALMSG = 0;
    public static final String[] names = {"SIGNALMSG", "RTPMSG"};

    private EcholocateType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
