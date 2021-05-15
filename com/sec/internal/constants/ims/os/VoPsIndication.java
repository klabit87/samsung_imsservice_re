package com.sec.internal.constants.ims.os;

public enum VoPsIndication {
    UNKNOWN,
    SUPPORTED,
    NOT_SUPPORTED;

    public static VoPsIndication translateVops(String strVops) {
        int vops = 1;
        try {
            vops = Integer.parseInt(strVops);
        } catch (NumberFormatException e) {
        }
        return translateVops(vops);
    }

    public static VoPsIndication translateVops(int vops) {
        if (vops == 2) {
            return SUPPORTED;
        }
        if (vops == 3) {
            return NOT_SUPPORTED;
        }
        return UNKNOWN;
    }
}
