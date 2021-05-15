package com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement;

import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.util.HashMap;
import java.util.Map;

public class XaaWfcErrorCodeTranslator {
    private static final String LOG_TAG = XaaWfcErrorCodeTranslator.class.getSimpleName();
    private static final Map<Integer, Integer> sMapE911FilteredFailureCodes;
    private static final Map<Integer, Integer> sMapE911FilteredSuccessCodes;

    static {
        HashMap hashMap = new HashMap();
        sMapE911FilteredSuccessCodes = hashMap;
        hashMap.put(7, Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_COMPLETED_SUCCESS_CODE));
        sMapE911FilteredSuccessCodes.put(12, Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE));
        sMapE911FilteredSuccessCodes.put(2, Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_NOT_REQUIRED));
        Map<Integer, Integer> map = sMapE911FilteredSuccessCodes;
        Integer valueOf = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE);
        map.put(3, valueOf);
        HashMap hashMap2 = new HashMap();
        sMapE911FilteredFailureCodes = hashMap2;
        hashMap2.put(7, Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE));
        sMapE911FilteredFailureCodes.put(12, valueOf);
        sMapE911FilteredFailureCodes.put(2, valueOf);
    }

    public static int translateErrorCode(int deviceEventType, boolean success, int nsdsErrorCode) {
        String str = LOG_TAG;
        Log.i(str, "translateErrorCode: deviceEventType " + deviceEventType + " success " + success + "nsdsErrorCode " + nsdsErrorCode);
        if (nsdsErrorCode == 1000) {
            return translateSuccessCode(deviceEventType);
        }
        if (nsdsErrorCode != 1046) {
            if (nsdsErrorCode == 2500 || nsdsErrorCode == 2300 || nsdsErrorCode == 2301) {
                return nsdsErrorCode;
            }
            return translateErrorCodeByEventType(deviceEventType, success);
        } else if (deviceEventType == 2) {
            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
        } else {
            return -1;
        }
    }

    private static int translateErrorCodeByEventType(int deviceEventType, boolean success) {
        if (success) {
            Log.e(LOG_TAG, "translateErrorCodeByEventType: result cannot be success");
            return -1;
        }
        if (deviceEventType != 1) {
            if (deviceEventType == 2) {
                return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
            }
            if (deviceEventType != 4) {
                return -1;
            }
        }
        return NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE;
    }

    private static int translateSuccessCode(int deviceEventType) {
        Integer filteredCode = sMapE911FilteredSuccessCodes.get(Integer.valueOf(deviceEventType));
        if (filteredCode == null) {
            filteredCode = 1000;
        }
        return filteredCode.intValue();
    }
}
