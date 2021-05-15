package com.sec.internal.ims.entitlement.util;

import android.util.Log;
import java.util.Date;

public class E911AidValidator {
    private static final long E911_AID_CHECK_EXPIRATION_TIME = 172800000;
    private static final String LOG_TAG = DateUtil.class.getSimpleName();

    public static boolean validate(String e911AidExp) {
        Date e911AidExpDate = DateUtil.parseIso8601Date(e911AidExp);
        if (e911AidExpDate != null && e911AidExpDate.getTime() - new Date().getTime() >= E911_AID_CHECK_EXPIRATION_TIME) {
            return true;
        }
        Log.i(LOG_TAG, "validate: e911 AID is expired");
        return false;
    }
}
