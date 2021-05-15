package com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.operation;

import android.util.Log;

public class TmoNsdsOperation {
    private static final String LOG_TAG = TmoNsdsOperation.class.getSimpleName();

    public static int getOperation(int deviceEventType, int prevNsdsBaseOperation) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType-" + deviceEventType + " prevOp-" + prevNsdsBaseOperation);
        if (prevNsdsBaseOperation != -1) {
            return -1;
        }
        if (deviceEventType == 14) {
            return 10;
        }
        if (deviceEventType == 15) {
            return 11;
        }
        if (deviceEventType != 18) {
            return -1;
        }
        return 14;
    }
}
