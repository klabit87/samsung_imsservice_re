package com.sec.internal.ims.entitlement.nsds.strategy.operation;

import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class DefaultNsdsOperation {
    private static final String LOG_TAG = DefaultNsdsOperation.class.getSimpleName();

    public static int getOperation(int deviceEventType, int prevNsdsBaseOperation, int responseCode, Bundle dataMap) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType-" + deviceEventType + " prevOp-" + prevNsdsBaseOperation);
        boolean locAndTcStatus = false;
        if (dataMap != null) {
            locAndTcStatus = dataMap.getBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS);
        }
        if (prevNsdsBaseOperation == -1) {
            if (deviceEventType == 2) {
                return 2;
            }
            if (deviceEventType != 7) {
                if (deviceEventType == 11) {
                    return 1;
                }
                if (deviceEventType == 19) {
                    return 15;
                }
                switch (deviceEventType) {
                    case 13:
                        return 9;
                    case 14:
                        return 10;
                    case 15:
                        return 11;
                    default:
                        return -1;
                }
            } else if (responseCode == 1000) {
                return 2;
            } else {
                return -1;
            }
        } else if (prevNsdsBaseOperation != 2) {
            if (prevNsdsBaseOperation != 3) {
                return -1;
            }
            return getOperationAfterLocAndTcCheck(deviceEventType, responseCode, locAndTcStatus);
        } else if (responseCode != 1000) {
            return -1;
        } else {
            Log.i(LOG_TAG, "getOperation(): BULK_ENTITLEMENT_CHECK");
            return 3;
        }
    }

    protected static int getOperationAfterLocAndTcCheck(int deviceEventType, int responseCode, boolean locAndTcStatus) {
        if (deviceEventType == 2 && responseCode == 1000 && !locAndTcStatus) {
            return 8;
        }
        return -1;
    }
}
