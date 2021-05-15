package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class SSForwardTo {
    public static final int FWD_TO_NOTIFY_CALLER = 1;
    public static final int FWD_TO_NOTIFY_SERVED_USER = 4;
    public static final int FWD_TO_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL = 5;
    public static final int FWD_TO_REVEAL_IDENTITY_TO_CALLER = 3;
    public static final int FWD_TO_REVEAL_IDENTITY_TO_TARGET = 6;
    public static final int FWD_TO_REVEAL_SERVED_USER_IDENTITY_TO_CALLER = 2;
    public static final int FWD_TO_TARGET = 0;
    public static final String[] names = {"FWD_TO_TARGET", "FWD_TO_NOTIFY_CALLER", "FWD_TO_REVEAL_SERVED_USER_IDENTITY_TO_CALLER", "FWD_TO_REVEAL_IDENTITY_TO_CALLER", "FWD_TO_NOTIFY_SERVED_USER", "FWD_TO_NOTIFY_SERVED_USER_ON_OUTBOUND_CALL", "FWD_TO_REVEAL_IDENTITY_TO_TARGET"};

    private SSForwardTo() {
    }

    public static String name(int e) {
        return names[e];
    }
}
