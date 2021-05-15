package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

public class ConfigurationRetrievalWithSIM extends OperationUsingManageConnectivity {
    private static final String LOG_TAG = ConfigurationRetrievalWithSIM.class.getSimpleName();

    public ConfigurationRetrievalWithSIM(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version, String userAgent, String imei) {
        super(looper, context, baseFlowImpl, messenger, version, userAgent, imei);
        Log.i(LOG_TAG, "created.");
    }

    public void retriveDeviceConfiguration(String url, String deviceGroup, String vimsiEap) {
        this.mOperation = 3;
        this.mVIMSI = vimsiEap;
        this.mDeviceGroup = deviceGroup;
        this.mBaseFlowImpl.getNSDSClient().setRequestUrl(url);
        executeOperationWithChallenge();
    }
}
