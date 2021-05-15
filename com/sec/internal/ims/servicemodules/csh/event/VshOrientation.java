package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;

public enum VshOrientation {
    LANDSCAPE(0),
    PORTRAIT(90),
    FLIPPED_LANDSCAPE(MNO.EVR_ESN),
    REVERSE_PORTRAIT(270);
    
    private final int mAngle;

    private VshOrientation(int i) {
        this.mAngle = i;
    }
}
