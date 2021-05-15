package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

public final class ReqCommonConfigMsg {
    public static final byte NONE = 0;
    public static final byte call_config = 3;
    public static final String[] names = {"NONE", "regi_config", "xdm_config", "call_config", "rcs_config", "serviceversion_config", "screen_config"};
    public static final byte rcs_config = 4;
    public static final byte regi_config = 1;
    public static final byte screen_config = 6;
    public static final byte serviceversion_config = 5;
    public static final byte xdm_config = 2;

    private ReqCommonConfigMsg() {
    }

    public static String name(int e) {
        return names[e];
    }
}
