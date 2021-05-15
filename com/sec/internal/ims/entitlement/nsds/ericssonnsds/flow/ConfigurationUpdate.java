package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

public class ConfigurationUpdate extends ConfigurationRetrievalWithSIM {
    private static final String LOG_TAG = ConfigurationUpdate.class.getSimpleName();

    public ConfigurationUpdate(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version, String userAgent, String imei) {
        super(looper, context, baseFlowImpl, messenger, version, userAgent, imei);
        Log.d(LOG_TAG, "created.");
    }

    public void updateDeviceConfiguration(String deviceGroup, String vimsiEap) {
        this.mOperation = 1;
        this.mVIMSI = vimsiEap;
        this.mDeviceGroup = deviceGroup;
        executeOperationWithChallenge();
    }
}
