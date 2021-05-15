package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class SipDirection {
    public static final int SIP_REQUEST = 0;
    public static final int SIP_RESPONSE = 1;
    public static final String[] names = {"SIP_REQUEST", "SIP_RESPONSE"};

    private SipDirection() {
    }

    public static String name(int e) {
        return names[e];
    }
}
