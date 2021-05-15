package com.sec.internal.ims.cmstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private final String TAG = NetworkChangeReceiver.class.getSimpleName();
    private final IWorkingStatusProvisionListener mIWorkingStatusProvisionListener;

    public NetworkChangeReceiver(IWorkingStatusProvisionListener controller) {
        this.mIWorkingStatusProvisionListener = controller;
    }

    public void onReceive(Context context, Intent intent) {
        String str = this.TAG;
        Log.i(str, "receive intent==" + intent.getAction());
        this.mIWorkingStatusProvisionListener.onNetworkChangeDetected();
    }
}
