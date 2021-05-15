package com.sec.internal.ims.entitlement.util;

import android.util.Log;

public class EntFeatureDetector {
    private static final String LOG_TAG = EntFeatureDetector.class.getSimpleName();

    public static boolean checkVSimFeatureEnabled(String vsimType, int simSlot) {
        String configserver = NSDSConfigHelper.getConfigServer(simSlot);
        String str = LOG_TAG;
        Log.i(str, "checkVSimFeatureEnabled: " + vsimType + " configserver:" + configserver);
        if (vsimType == null || !vsimType.equalsIgnoreCase(configserver)) {
            return false;
        }
        return true;
    }

    public static boolean checkWFCAutoOnEnabled(int simSlot) {
        boolean wfcAutoOnEnabled = NSDSConfigHelper.isWFCAutoOnEnabled(simSlot);
        String str = LOG_TAG;
        Log.i(str, "checkWFCAutoOnEnabled: " + wfcAutoOnEnabled);
        if (wfcAutoOnEnabled) {
            return true;
        }
        return false;
    }
}
