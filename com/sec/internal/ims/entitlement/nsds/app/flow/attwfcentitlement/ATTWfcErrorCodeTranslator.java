package com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement;

import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import java.util.HashMap;
import java.util.Map;

public class ATTWfcErrorCodeTranslator {
    private static final int ENTITLEMENT_CHECK_MAX_RETRY = 2;
    private static final String LOG_TAG = ATTWfcErrorCodeTranslator.class.getSimpleName();
    private static final Map<Integer, Integer> sMapE911FilteredFailureCodes;
    private static final Map<Integer, Integer> sMapE911FilteredSuccessCodes;

    static {
        HashMap hashMap = new HashMap();
        sMapE911FilteredSuccessCodes = hashMap;
        Integer valueOf = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_COMPLETED_SUCCESS_CODE);
        hashMap.put(7, valueOf);
        sMapE911FilteredSuccessCodes.put(10, valueOf);
        sMapE911FilteredSuccessCodes.put(9, valueOf);
        Map<Integer, Integer> map = sMapE911FilteredSuccessCodes;
        Integer valueOf2 = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE);
        map.put(8, valueOf2);
        sMapE911FilteredSuccessCodes.put(12, valueOf2);
        sMapE911FilteredSuccessCodes.put(2, Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_NOT_REQUIRED));
        Map<Integer, Integer> map2 = sMapE911FilteredSuccessCodes;
        Integer valueOf3 = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE);
        map2.put(3, valueOf3);
        HashMap hashMap2 = new HashMap();
        sMapE911FilteredFailureCodes = hashMap2;
        Integer valueOf4 = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE);
        hashMap2.put(7, valueOf4);
        sMapE911FilteredFailureCodes.put(10, valueOf4);
        sMapE911FilteredFailureCodes.put(9, valueOf4);
        sMapE911FilteredFailureCodes.put(8, valueOf3);
        sMapE911FilteredFailureCodes.put(12, valueOf3);
        sMapE911FilteredFailureCodes.put(2, valueOf3);
    }

    public static int translateErrorCode(NSDSDatabaseHelper dbHelper, int deviceEventType, boolean success, int nsdsErrorCode, int retryCount, String deviceUid) {
        String str = LOG_TAG;
        Log.i(str, "translateErrorCode: deviceEventType " + deviceEventType + "success " + success + "nsdsErrorCode " + nsdsErrorCode + "retryCount " + retryCount);
        if (nsdsErrorCode == 1000) {
            return filterSuccessCodeWithE911Validity(dbHelper, deviceEventType, deviceUid);
        }
        if (nsdsErrorCode != 1046) {
            if (nsdsErrorCode == 2500 || nsdsErrorCode == 2300 || nsdsErrorCode == 2301) {
                return nsdsErrorCode;
            }
            return translateErrorCodeByEventType(deviceEventType, success, retryCount);
        } else if (deviceEventType == 2) {
            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
        } else {
            return -1;
        }
    }

    private static int filterSuccessCodeWithE911Validity(NSDSDatabaseHelper dbHelper, int deviceEventType, String deviceUid) {
        Integer filteredCode;
        if (dbHelper == null || !dbHelper.isE911InfoAvailForNativeLine(deviceUid)) {
            filteredCode = sMapE911FilteredFailureCodes.get(Integer.valueOf(deviceEventType));
            if (filteredCode == null) {
                filteredCode = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE);
            }
        } else {
            filteredCode = sMapE911FilteredSuccessCodes.get(Integer.valueOf(deviceEventType));
            if (filteredCode == null) {
                filteredCode = 1000;
            }
        }
        return filteredCode.intValue();
    }

    private static int translateErrorCodeByEventType(int deviceEventType, boolean success, int retryCount) {
        if (success) {
            Log.e(LOG_TAG, "translateErrorCodeByEventType: result cannot be success");
            return -1;
        }
        if (deviceEventType != 1) {
            if (deviceEventType == 2) {
                return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
            }
            if (deviceEventType != 4) {
                if (deviceEventType == 9) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
                }
                if (deviceEventType != 10) {
                    return -1;
                }
                if (retryCount == 2) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
                }
                return NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_PENDING_ERROR_CODE;
            }
        }
        return NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE;
    }
}
