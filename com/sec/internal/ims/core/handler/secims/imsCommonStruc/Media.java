package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class Media {
    public static final int MEDIA_ALL = 0;
    public static final int MEDIA_APPLICATION = 4;
    public static final int MEDIA_AUDIO = 1;
    public static final int MEDIA_MESSAGE = 5;
    public static final int MEDIA_TEXT = 3;
    public static final int MEDIA_VIDEO = 2;
    public static final String[] names = {"MEDIA_ALL", "MEDIA_AUDIO", "MEDIA_VIDEO", "MEDIA_TEXT", "MEDIA_APPLICATION", "MEDIA_MESSAGE"};

    private Media() {
    }

    public static String name(int e) {
        return names[e];
    }
}
