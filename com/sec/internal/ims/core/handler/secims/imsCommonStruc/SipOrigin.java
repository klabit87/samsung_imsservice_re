package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class SipOrigin {
    public static final int EXTERNAL = 1;
    public static final int INTERNAL = 0;
    public static final String[] names = {"INTERNAL", "EXTERNAL"};

    private SipOrigin() {
    }

    public static String name(int e) {
        return names[e];
    }
}
