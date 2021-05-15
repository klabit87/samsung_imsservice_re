package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class XCapDirection {
    public static final int XCAP_REQUEST = 0;
    public static final int XCAP_RESPONSE = 1;
    public static final String[] names = {"XCAP_REQUEST", "XCAP_RESPONSE"};

    private XCapDirection() {
    }

    public static String name(int e) {
        return names[e];
    }
}
