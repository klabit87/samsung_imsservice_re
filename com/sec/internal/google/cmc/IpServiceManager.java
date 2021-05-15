package com.sec.internal.google.cmc;

import android.content.Context;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import android.util.Log;

public class IpServiceManager {
    private static final String LOG_TAG = IpServiceManager.class.getSimpleName();
    private Context mContext;
    private INetworkManagementService mNetworkService = null;

    public IpServiceManager(Context context) {
        this.mContext = context;
        IBinder binder = ServiceManager.getService("network_management");
        if (binder != null) {
            this.mNetworkService = INetworkManagementService.Stub.asInterface(binder);
        } else {
            Log.e(LOG_TAG, "bind failed");
        }
    }

    public void ipRuleAdd(String nodeName, String ipAddr) {
        try {
            if (this.mNetworkService != null) {
                String str = LOG_TAG;
                Log.d(str, "try to [add] iprule: " + ipAddr + ", in " + nodeName);
                this.mNetworkService.updateSourceRule(true, ipAddr, nodeName);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "add iprule error");
        }
    }

    public void ipRuleRemove(String nodeName, String ipAddr) {
        try {
            if (this.mNetworkService != null) {
                String str = LOG_TAG;
                Log.d(str, "try to [delete] prve iprule in " + nodeName);
                this.mNetworkService.updateSourceRule(false, ipAddr, nodeName);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "remove iprule error");
        }
    }
}
