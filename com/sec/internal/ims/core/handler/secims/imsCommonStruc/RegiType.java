package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.sec.internal.constants.ims.DiagnosisConstants;

public final class RegiType {
    public static final int DE_REGI = 1;
    public static final int REGI = 0;
    public static final String[] names = {DiagnosisConstants.FEATURE_REGI, "DE_REGI"};

    private RegiType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
