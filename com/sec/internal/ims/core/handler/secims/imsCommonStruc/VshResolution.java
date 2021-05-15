package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class VshResolution {
    public static final int CIF = 4;
    public static final int CIF_PORTRAIT = 8;
    public static final int NONE = 0;
    public static final int QCIF = 1;
    public static final int QCIF_PORTRAIT = 5;
    public static final int QVGA = 2;
    public static final int QVGA_PORTRAIT = 6;
    public static final int VGA = 3;
    public static final int VGA_PORTRAIT = 7;
    public static final String[] names = {"NONE", "QCIF", "QVGA", "VGA", "CIF", "QCIF_PORTRAIT", "QVGA_PORTRAIT", "VGA_PORTRAIT", "CIF_PORTRAIT"};

    private VshResolution() {
    }

    public static String name(int e) {
        return names[e];
    }
}
