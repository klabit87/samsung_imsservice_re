package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class ConfType {
    public static final int ADHOC_LIST = 1;
    public static final int ADHOC_MERGE = 0;
    public static final int PREDEFINE = 2;
    public static final String[] names = {"ADHOC_MERGE", "ADHOC_LIST", "PREDEFINE"};

    private ConfType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
