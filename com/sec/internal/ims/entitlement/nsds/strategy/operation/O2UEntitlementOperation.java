package com.sec.internal.ims.entitlement.nsds.strategy.operation;

import android.util.Log;

public class O2UEntitlementOperation {
    private static final String LOG_TAG = O2UEntitlementOperation.class.getSimpleName();

    public static int getOperation(int deviceEventType, int prevNsdsBaseOperation) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType-" + deviceEventType + " prevOp-" + prevNsdsBaseOperation);
        if (prevNsdsBaseOperation == -1) {
            return getInitialOperation();
        }
        return getNextOperation();
    }

    private static int getInitialOperation() {
        return 2;
    }

    private static int getNextOperation() {
        return -1;
    }
}
