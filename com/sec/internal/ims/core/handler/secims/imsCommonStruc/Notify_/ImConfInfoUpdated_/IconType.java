package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_;

public final class IconType {
    public static final int ICON_TYPE_FILE = 1;
    public static final int ICON_TYPE_NONE = 0;
    public static final int ICON_TYPE_URI = 2;
    public static final String[] names = {"ICON_TYPE_NONE", "ICON_TYPE_FILE", "ICON_TYPE_URI"};

    private IconType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
