package com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.nsds.NSDSMultiSimService;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.log.IMSLog;
import java.security.SecureRandom;

public class PushTokenHelper {
    private static final String LOG_TAG = PushTokenHelper.class.getSimpleName();

    public static String getPushToken(Context context, String deviceUid) {
        String pushToken = null;
        SharedPreferences sp = NSDSSharedPrefHelper.getSharedPref(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sp != null) {
            pushToken = sp.getString(deviceUid + ":" + NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN, (String) null);
        }
        if (!TextUtils.isEmpty(pushToken)) {
            return pushToken;
        }
        requestGcmRegistrationToken(context);
        return generatePushToken();
    }

    private static void requestGcmRegistrationToken(Context context) {
        IMSLog.i(LOG_TAG, "push token was dummy.txt. Requesting one from GCM now");
        Intent intent = new Intent(context, NSDSMultiSimService.class);
        intent.setAction(NSDSNamespaces.NSDSActions.ACTION_REFRESH_GCM_TOKEN);
        context.startService(intent);
    }

    private static String generatePushToken() {
        String encodedDeviceId = Base64.encodeToString(Long.toHexString(new SecureRandom().nextLong()).getBytes(), 2);
        String str = LOG_TAG;
        IMSLog.s(str, "generatePushToken: " + encodedDeviceId);
        return encodedDeviceId;
    }
}
