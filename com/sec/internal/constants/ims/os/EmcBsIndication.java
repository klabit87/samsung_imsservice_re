package com.sec.internal.constants.ims.os;

public enum EmcBsIndication {
    UNKNOWN,
    SUPPORTED,
    NOT_SUPPORTED;

    public static EmcBsIndication translateEmcbs(int emcbs) {
        if (emcbs == 2) {
            return SUPPORTED;
        }
        if (emcbs == 3) {
            return NOT_SUPPORTED;
        }
        return UNKNOWN;
    }
}
