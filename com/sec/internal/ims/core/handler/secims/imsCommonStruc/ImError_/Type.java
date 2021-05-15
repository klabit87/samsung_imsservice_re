package com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImError_;

import com.sec.internal.constants.ims.ImsConstants;

public final class Type {
    public static final int DEDICATED_BEARER_ERROR = 11;
    public static final int DEVICE_UNREGISTERED = 8;
    public static final int ENGINE_ERROR = 3;
    public static final int MSRP_ERROR = 2;
    public static final int NETWORK_ERROR = 5;
    public static final int REMOTE_PARTY_CANCELED = 7;
    public static final int SESSION_RELEASE = 4;
    public static final int SESSION_RSRC_UNAVAILABLE = 6;
    public static final int SIP_ERROR = 1;
    public static final int SIP_PROVISIONAL = 9;
    public static final int SUCCESS = 0;
    public static final int UNKNOWN_IM_ERROR = 10;
    public static final String[] names = {"SUCCESS", ImsConstants.Intents.EXTRA_SIP_ERROR_CODE, "MSRP_ERROR", "ENGINE_ERROR", "SESSION_RELEASE", "NETWORK_ERROR", "SESSION_RSRC_UNAVAILABLE", "REMOTE_PARTY_CANCELED", "DEVICE_UNREGISTERED", "SIP_PROVISIONAL", "UNKNOWN_IM_ERROR", "DEDICATED_BEARER_ERROR"};

    private Type() {
    }

    public static String name(int e) {
        return names[e];
    }
}
