package com.sec.internal.ims.servicemodules.options;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.sec.ims.extensions.Extensions;

public class CapabilityIntentReceiver extends BroadcastReceiver {
    protected static final String INTENT_PERIODIC_POLL_TIMEOUT = "com.sec.internal.ims.servicemodules.options.poll_timeout";
    protected static final String INTENT_THROTTLED_RETRY_TIMEOUT = "com.sec.internal.ims.servicemodules.options.sub_throttled_timeout";
    private static final String LOG_TAG = "CapabilityIntentReceiver";
    private CapabilityDiscoveryModule mCapabilityDiscovery;

    public CapabilityIntentReceiver(CapabilityDiscoveryModule capabilityDiscoveryModule) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_PERIODIC_POLL_TIMEOUT);
        intentFilter.addAction(INTENT_THROTTLED_RETRY_TIMEOUT);
        intentFilter.addAction(Extensions.Intent.ACTION_USER_SWITCHED);
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        return intentFilter;
    }

    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "onReceive: " + intent.getAction());
        if (INTENT_PERIODIC_POLL_TIMEOUT.equals(intent.getAction())) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(17));
            CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule2.sendMessage(capabilityDiscoveryModule2.obtainMessage(1, true));
        } else if (INTENT_THROTTLED_RETRY_TIMEOUT.equals(intent.getAction())) {
            boolean isPeriodic = intent.getBooleanExtra("IS_PERIODIC", false);
            Log.i(LOG_TAG, "onReceive: subscription throttled timeout. isPeriodic = " + isPeriodic);
            CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule3.sendMessage(capabilityDiscoveryModule3.obtainMessage(1, Boolean.valueOf(isPeriodic)));
        } else if (Extensions.Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
            CapabilityDiscoveryModule capabilityDiscoveryModule4 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule4.sendMessage(capabilityDiscoveryModule4.obtainMessage(11));
        } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            CapabilityDiscoveryModule capabilityDiscoveryModule5 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule5.sendMessage(capabilityDiscoveryModule5.obtainMessage(12));
        }
    }
}
