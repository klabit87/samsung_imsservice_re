package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class CallState {
    public static final int CALL_BUSY = 7;
    public static final int CALL_CALLING = 2;
    public static final int CALL_EARLY_MEDIA_START = 12;
    public static final int CALL_ENDED = 11;
    public static final int CALL_ESTABLISHED = 8;
    public static final int CALL_EXTEND_TO_CONFERENCE = 18;
    public static final int CALL_FORWARDED = 5;
    public static final int CALL_HELD_BOTH = 13;
    public static final int CALL_HELD_LOCAL = 9;
    public static final int CALL_HELD_REMOTE = 10;
    public static final int CALL_IDLE = 0;
    public static final int CALL_INFO_RESPONSE = 19;
    public static final int CALL_MODIFIED = 14;
    public static final int CALL_REFRESHFAIL = 16;
    public static final int CALL_RINGING = 3;
    public static final int CALL_RINGING_BACK = 4;
    public static final int CALL_SESSIONPROGRESS = 15;
    public static final int CALL_TRYING = 1;
    public static final int CALL_USSD_INDI_BY_MESSAGE = 17;
    public static final int CAll_ACK_RECEIVED = 6;
    public static final String[] names = {"CALL_IDLE", "CALL_TRYING", "CALL_CALLING", "CALL_RINGING", "CALL_RINGING_BACK", "CALL_FORWARDED", "CAll_ACK_RECEIVED", "CALL_BUSY", "CALL_ESTABLISHED", "CALL_HELD_LOCAL", "CALL_HELD_REMOTE", "CALL_ENDED", "CALL_EARLY_MEDIA_START", "CALL_HELD_BOTH", "CALL_MODIFIED", "CALL_SESSIONPROGRESS", "CALL_REFRESHFAIL", "CALL_USSD_INDI_BY_MESSAGE", "CALL_EXTEND_TO_CONFERENCE", "CALL_INFO_RESPONSE"};

    private CallState() {
    }

    public static String name(int e) {
        return names[e];
    }
}
