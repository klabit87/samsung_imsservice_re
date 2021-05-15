package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Participant_;

public final class CopyControl {
    public static final int BCC = 2;
    public static final int CC = 1;
    public static final int TO = 0;
    public static final String[] names = {"TO", "CC", "BCC"};

    private CopyControl() {
    }

    public static String name(int e) {
        return names[e];
    }
}
