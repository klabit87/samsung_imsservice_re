package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class StrengthTag {
    public static final int MANDATORY = 0;
    public static final int OPTIONAL = 1;
    public static final String[] names = {"MANDATORY", "OPTIONAL"};

    private StrengthTag() {
    }

    public static String name(int e) {
        return names[e];
    }
}
