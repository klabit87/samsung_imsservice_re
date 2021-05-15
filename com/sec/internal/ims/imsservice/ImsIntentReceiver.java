package com.sec.internal.ims.imsservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import java.util.Arrays;
import java.util.Set;

public class ImsIntentReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ImsIntentReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(LOG_TAG, "ImsIntentReceiver: " + intent);
        if (ImsConstants.Intents.ACTION_PCO_INFO.equals(action)) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                Log.d(LOG_TAG, "PcoReceiver: Invalid PCO_INFO.(Not exist Extras)");
                return;
            }
            Set<String> keys = bundle.keySet();
            if (keys == null || keys.isEmpty()) {
                Log.d(LOG_TAG, "PcoReceiver: Invalid PCO_INFO.(Invalid keyset)");
                return;
            }
            String pdn = bundle.getString("apnType");
            byte[] pcokey = bundle.getByteArray(ImsConstants.Intents.EXTRA_PCO_VALUE_KEY);
            Log.d(LOG_TAG, "ACTION_PCO_INFO, pdn: " + pdn + ", pcokey: " + Arrays.asList(new byte[][]{pcokey}));
            int pcoValue = -1;
            if (pcokey != null) {
                pcoValue = pcokey[0] - 48;
            }
            if (pcoValue < 0) {
                Log.e(LOG_TAG, "Invalid pcoValue: " + pcoValue);
                return;
            }
            IRegistrationManager rm = ImsServiceStub.getInstance().getRegistrationManager();
            if (rm != null) {
                Log.d(LOG_TAG, "ACTION_PCO_INFO: PCO (" + pcoValue + "), PDN (" + pdn + ")");
                rm.updatePcoInfo(SimUtil.getDefaultPhoneId(), pdn, pcoValue);
                return;
            }
            Log.e(LOG_TAG, "ACTION_PCO_INFO: RegistrationManager is null..");
        }
    }
}
