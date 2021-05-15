package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.VshIncomingSession_;

public final class MediaType {
    public static final int EXTERNAL = 2;
    public static final int LIVE = 0;
    public static final int RECORDED = 1;
    public static final String[] names = {"LIVE", "RECORDED", "EXTERNAL"};

    private MediaType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
