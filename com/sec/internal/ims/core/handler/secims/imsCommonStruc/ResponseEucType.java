package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.sec.internal.constants.tapi.UserConsentProviderContract;

public final class ResponseEucType {
    public static final int PERSISTENT = 0;
    public static final int VOLATILE = 1;
    public static final String[] names = {UserConsentProviderContract.EUCR_PERSISTENT_LABEL, UserConsentProviderContract.EUCR_VOLATILE_LABEL};

    private ResponseEucType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
