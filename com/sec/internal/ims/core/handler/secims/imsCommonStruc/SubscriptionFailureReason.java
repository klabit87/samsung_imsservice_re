package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class SubscriptionFailureReason {
    public static final int DEACTIVATED = 1;
    public static final int GIVEUP = 5;
    public static final int NORESOURCE = 6;
    public static final int PROBATION = 2;
    public static final int REJECTED = 3;
    public static final int TIMEOUT = 4;
    public static final int UNKNOWN = 0;
    public static final String[] names = {"UNKNOWN", "DEACTIVATED", "PROBATION", "REJECTED", "TIMEOUT", "GIVEUP", "NORESOURCE"};

    private SubscriptionFailureReason() {
    }

    public static String name(int e) {
        return names[e];
    }
}
