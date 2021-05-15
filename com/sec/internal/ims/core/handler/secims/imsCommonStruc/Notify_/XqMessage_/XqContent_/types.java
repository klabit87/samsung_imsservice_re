package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.XqMessage_.XqContent_;

public final class types {
    public static final int STRING = 4;
    public static final int UCHAR = 1;
    public static final int UINT32 = 3;
    public static final int UN = 0;
    public static final int USHORT = 2;
    public static final String[] names = {"UN", "UCHAR", "USHORT", "UINT32", "STRING"};

    private types() {
    }

    public static String name(int e) {
        return names[e];
    }
}
