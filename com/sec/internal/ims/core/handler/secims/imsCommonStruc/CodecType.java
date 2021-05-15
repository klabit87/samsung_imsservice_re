package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class CodecType {
    public static final int AMR = 0;
    public static final int AMR_WB = 1;
    public static final int H_264 = 2;
    public static final String[] names = {"AMR", "AMR_WB", "H_264"};

    private CodecType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
