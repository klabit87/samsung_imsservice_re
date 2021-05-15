package com.sec.internal.ims.entitlement.nsds.strategy.operation;

import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.util.E911AidValidator;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;

public class ATTWfcEntitlementOperation {
    private static final String LOG_TAG = ATTWfcEntitlementOperation.class.getSimpleName();

    public static int getOperation(int deviceEventType, int prevNsdsBaseOperation, int responseCode, Bundle dataMap) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType " + deviceEventType + " prevOp " + prevNsdsBaseOperation);
        boolean locAndTcStatus = false;
        boolean onSvcProv = false;
        String e911AidExp = null;
        if (dataMap != null) {
            locAndTcStatus = dataMap.getBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS);
            onSvcProv = dataMap.getBoolean(NSDSNamespaces.NSDSDataMapKey.SVC_PROV_STATUS);
            e911AidExp = dataMap.getString(NSDSNamespaces.NSDSDataMapKey.E911_AID_EXP);
        }
        if (prevNsdsBaseOperation == -1) {
            return getInitialOperation(deviceEventType, responseCode);
        }
        return getNextOperation(prevNsdsBaseOperation, deviceEventType, responseCode, locAndTcStatus, onSvcProv, e911AidExp);
    }

    protected static int getInitialOperation(int deviceEventType, int responseCode) {
        if (deviceEventType == 3) {
            return 5;
        }
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
                if (deviceEventType == 14) {
                    return 10;
                }
                if (deviceEventType != 15) {
                    return 2;
                }
                return 11;
            }
        }
        if (responseCode == 1000) {
            return 2;
        }
        return -1;
    }

    protected static int getNextOperation(int prevNsdsBaseOperation, int deviceEventType, int responseCode, boolean locAndTcStatus, boolean onSvcProv, String e911AidExp) {
        if (prevNsdsBaseOperation == 2) {
            return getOperationAfterEntitlementCheck(deviceEventType, responseCode, e911AidExp, onSvcProv);
        }
        if (prevNsdsBaseOperation == 3) {
            return getOperationAfterLocAndTcCheck(deviceEventType, responseCode, locAndTcStatus, onSvcProv);
        }
        if (prevNsdsBaseOperation == 4) {
            return getOperationAfterPushTokenRegistration(deviceEventType, responseCode);
        }
        if (prevNsdsBaseOperation != 16) {
            return -1;
        }
        return getOperationAfterLocAndTcCheckforAutoOn(deviceEventType, responseCode, locAndTcStatus, onSvcProv);
    }

    protected static int getOperationAfterEntitlementCheck(int deviceEventType, int responseCode, String e911AidExp, boolean onSvcProv) {
        switch (deviceEventType) {
            case 1:
                String str = LOG_TAG;
                Log.i(str, "[ATT_AutoOn] getOperationAfterEntitlementCheck responseCode: " + responseCode + ",onSvcProv:" + onSvcProv);
                if (responseCode == 1000 && !E911AidValidator.validate(e911AidExp)) {
                    return 3;
                }
                if (!EntFeatureDetector.checkWFCAutoOnEnabled(0)) {
                    return -1;
                }
                if (responseCode == 1048) {
                    if (!onSvcProv) {
                        return 19;
                    }
                    return -1;
                } else if (responseCode == 1063) {
                    return 19;
                } else {
                    return -1;
                }
            case 2:
                if (responseCode == 1000 || responseCode == 1048) {
                    return 4;
                }
                return -1;
            case 4:
                if (responseCode == 1000) {
                    return 4;
                }
                return 5;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                if (responseCode == 1000) {
                    return 3;
                }
                return -1;
            default:
                return -1;
        }
    }

    protected static int getOperationAfterPushTokenRegistration(int deviceEventType, int responseCode) {
        if (responseCode == 1000 && deviceEventType == 2) {
            return 3;
        }
        return -1;
    }

    protected static int getOperationAfterLocAndTcCheck(int deviceEventType, int responseCode, boolean locAndTcStatus, boolean onSvcProv) {
        if (deviceEventType != 2) {
            if (deviceEventType == 5 && responseCode == 1000) {
                return 13;
            }
            return -1;
        } else if (responseCode != 1000) {
            return -1;
        } else {
            if (!locAndTcStatus || onSvcProv) {
                return 8;
            }
            return -1;
        }
    }

    private static int getOperationAfterLocAndTcCheckforAutoOn(int deviceEventType, int responseCode, boolean locAndTcStatus, boolean onSvcProv) {
        if (deviceEventType != 2) {
            return -1;
        }
        String str = LOG_TAG;
        Log.i(str, "[ATT_AutoOn] getOperationAfterLocAndTcCheckforAutoOn responseCode: " + responseCode + ",onSvcProv:" + onSvcProv);
        if (responseCode != 1000) {
            if ((responseCode == 1048 && !onSvcProv) || responseCode == 1063) {
            }
            return 17;
        } else if (locAndTcStatus) {
            return -1;
        } else {
            String str2 = LOG_TAG;
            Log.i(str2, "[ATT_AutoOn] getOperationAfterLocAndTcCheckforAutoOn responseCode: " + responseCode + ",locAndTcStatus:" + locAndTcStatus);
            return -1;
        }
    }
}
