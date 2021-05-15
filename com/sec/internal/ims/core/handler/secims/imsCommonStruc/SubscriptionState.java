package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class SubscriptionState {
    public static final int ACTIVE = 2;
    public static final int NEUTRAL = 1;
    public static final int PENDING = 3;
    public static final int TERMINATED = 4;
    public static final int UNKNOWN = 0;
    public static final String[] names = {"UNKNOWN", "NEUTRAL", "ACTIVE", "PENDING", "TERMINATED"};

    private SubscriptionState() {
    }

    public static String name(int e) {
        return names[e];
    }
}
