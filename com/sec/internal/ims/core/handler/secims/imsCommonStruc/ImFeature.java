package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class ImFeature {
    public static final int GEOLOCATION = 4;
    public static final int HTTP_FT = 3;
    public static final int IMDN = 2;
    public static final int ISCOMPOSING_TYPE = 1;
    public static final int MULTIMEDIA = 5;
    public static final int TEXT_PLAIN = 0;
    public static final String[] names = {"TEXT_PLAIN", "ISCOMPOSING_TYPE", "IMDN", "HTTP_FT", "GEOLOCATION", "MULTIMEDIA"};

    private ImFeature() {
    }

    public static String name(int e) {
        return names[e];
    }
}
