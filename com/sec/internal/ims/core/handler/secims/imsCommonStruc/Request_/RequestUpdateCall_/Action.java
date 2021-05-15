package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCall_;

public final class Action {
    public static final int CODEC_CHANGE = 1;
    public static final int REINVITE_ONLY = 0;
    public static final String[] names = {"REINVITE_ONLY", "CODEC_CHANGE"};

    private Action() {
    }

    public static String name(int e) {
        return names[e];
    }
}
