package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class CallType {
    public static final int RTT_VIDEO_CALL = 15;
    public static final int RTT_VIDEO_CONF_CALL = 17;
    public static final int RTT_VIDEO_E911_CALL = 19;
    public static final int RTT_VOICE_CALL = 14;
    public static final int RTT_VOICE_CONF_CALL = 16;
    public static final int RTT_VOICE_E911_CALL = 18;
    public static final int TTY_FULL_CALL = 9;
    public static final int TTY_HCO_CALL = 10;
    public static final int TTY_VCO_CALL = 11;
    public static final int UNKNOWN_CALL = 0;
    public static final int USSD_CALL = 12;
    public static final int VIDEO_CALL = 2;
    public static final int VIDEO_CONF_CALL = 6;
    public static final int VIDEO_E911_CALL = 8;
    public static final int VOICE_CALL = 1;
    public static final int VOICE_CONF_CALL = 5;
    public static final int VOICE_E911_CALL = 7;
    public static final int VSH_RX_CALL = 4;
    public static final int VSH_TX_CALL = 3;
    public static final String[] names = {"UNKNOWN_CALL", "VOICE_CALL", "VIDEO_CALL", "VSH_TX_CALL", "VSH_RX_CALL", "VOICE_CONF_CALL", "VIDEO_CONF_CALL", "VOICE_E911_CALL", "VIDEO_E911_CALL", "TTY_FULL_CALL", "TTY_HCO_CALL", "TTY_VCO_CALL", "USSD_CALL", "", "RTT_VOICE_CALL", "RTT_VIDEO_CALL", "RTT_VOICE_CONF_CALL", "RTT_VIDEO_CONF_CALL", "RTT_VOICE_E911_CALL", "RTT_VIDEO_E911_CALL"};

    private CallType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
