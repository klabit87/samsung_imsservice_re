package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

public class DeviceDeactivation extends OperationUsingManageConnectivity {
    private static final String LOG_TAG = DeviceDeactivation.class.getSimpleName();

    public DeviceDeactivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version, String userAgent, String imei) {
        super(looper, context, baseFlowImpl, messenger, version, userAgent, imei);
        Log.i(LOG_TAG, "created.");
    }

    public void deactivateDevice() {
        this.mOperation = 2;
        executeOperationWithChallenge();
    }
}
