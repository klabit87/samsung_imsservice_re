package com.sec.internal.ims.entitlement.nsds.strategy.operation;

import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class XAAEntitlementOperation {
    private static final String LOG_TAG = XAAEntitlementOperation.class.getSimpleName();

    public static int getOperation(int deviceEventType, int prevNsdsBaseOperation, int responseCode, Bundle dataMap) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType " + deviceEventType + " prevOp " + prevNsdsBaseOperation);
        boolean locAndTcStatus = false;
        if (dataMap != null) {
            locAndTcStatus = dataMap.getBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS);
        }
        if (prevNsdsBaseOperation == -1) {
            return getInitialOperation(deviceEventType, responseCode);
        }
        return getNextOperation(prevNsdsBaseOperation, deviceEventType, responseCode, locAndTcStatus);
    }

    protected static int getInitialOperation(int deviceEventType, int responseCode) {
        if (deviceEventType == 5) {
            return 3;
        }
        if (deviceEventType != 7) {
            if (deviceEventType == 19) {
                return 15;
            }
            if (deviceEventType == 11) {
                return 1;
            }
            if (deviceEventType != 12) {
                return 2;
            }
        }
        if (responseCode == 1000) {
            return 2;
        }
        return -1;
    }

    protected static int getNextOperation(int prevNsdsBaseOperation, int deviceEventType, int responseCode, boolean locAndTcStatus) {
        if (prevNsdsBaseOperation != 2) {
            if (prevNsdsBaseOperation != 3) {
                return -1;
            }
            return getOperationAfterLocAndTcCheck(deviceEventType, responseCode, locAndTcStatus);
        } else if (responseCode == 1000) {
            return 3;
        } else {
            return -1;
        }
    }

    protected static int getOperationAfterLocAndTcCheck(int deviceEventType, int responseCode, boolean locAndTcStatus) {
        if (deviceEventType != 2) {
            if (deviceEventType == 5 && responseCode == 1000) {
                return 13;
            }
            return -1;
        } else if (responseCode != 1000 || locAndTcStatus) {
            return -1;
        } else {
            return 8;
        }
    }
}
