package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class ImServiceType {
    public static final int IM_SERVICE_CHAT = 1;
    public static final int IM_SERVICE_FT = 2;
    public static final int IM_SERVICE_SLM = 0;
    public static final String[] names = {"IM_SERVICE_SLM", "IM_SERVICE_CHAT", "IM_SERVICE_FT"};

    private ImServiceType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
