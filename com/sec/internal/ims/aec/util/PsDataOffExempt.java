package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.TelephonyNetworkSpecifier;
import android.os.Handler;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.AECLog;

public class PsDataOffExempt {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = PsDataOffExempt.class.getSimpleName();
    /* access modifiers changed from: private */
    public final ConnectivityManager mConnMgr;
    /* access modifiers changed from: private */
    public Network mNetwork;
    private NetworkCallback mNetworkCallback;
    /* access modifiers changed from: private */
    public final int mPhoneId;
    /* access modifiers changed from: private */
    public final Handler mWorkflowHandler;

    public PsDataOffExempt(Context context, int phoneId, Handler handler) {
        this.mPhoneId = phoneId;
        this.mWorkflowHandler = handler;
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public Network getNetwork() {
        return this.mNetwork;
    }

    public boolean isConnected() {
        NetworkInfo networkInfo;
        if (this.mNetworkCallback == null || (networkInfo = this.mConnMgr.getNetworkInfo(this.mNetwork)) == null) {
            return false;
        }
        boolean isConnected = networkInfo.getState() == NetworkInfo.State.CONNECTED || networkInfo.getState() == NetworkInfo.State.SUSPENDED;
        String str = LOG_TAG;
        AECLog.d(str, "isConnected: " + isConnected);
        return isConnected;
    }

    public void requestNetwork() {
        if (isConnected()) {
            this.mWorkflowHandler.sendEmptyMessage(1008);
            return;
        }
        int subId = SimUtil.getSubId(this.mPhoneId);
        String str = LOG_TAG;
        AECLog.i(str, "requestNetwork: transport " + 0 + " capability " + 9 + " subId " + subId, this.mPhoneId);
        NetworkRequest request = new NetworkRequest.Builder().addTransportType(0).addCapability(9).setNetworkSpecifier(new TelephonyNetworkSpecifier.Builder().setSubscriptionId(subId).build()).build();
        NetworkCallback networkCallback = new NetworkCallback();
        this.mNetworkCallback = networkCallback;
        this.mConnMgr.requestNetwork(request, networkCallback);
    }

    public void unregisterNetworkCallback() {
        NetworkCallback networkCallback = this.mNetworkCallback;
        if (networkCallback != null) {
            this.mConnMgr.unregisterNetworkCallback(networkCallback);
            this.mNetwork = null;
            this.mNetworkCallback = null;
            AECLog.i(LOG_TAG, "unregisterNetworkCallback", this.mPhoneId);
        }
    }

    private class NetworkCallback extends ConnectivityManager.NetworkCallback {
        private NetworkCallback() {
        }

        public void onAvailable(Network network) {
            String access$100 = PsDataOffExempt.LOG_TAG;
            AECLog.d(access$100, "onAvailable: " + network, PsDataOffExempt.this.mPhoneId);
            Network unused = PsDataOffExempt.this.mNetwork = network;
            LinkProperties lp = PsDataOffExempt.this.mConnMgr.getLinkProperties(network);
            if (lp == null || lp.getInterfaceName() == null) {
                AECLog.d(PsDataOffExempt.LOG_TAG, "onAvailable: no link properties", PsDataOffExempt.this.mPhoneId);
                PsDataOffExempt.this.unregisterNetworkCallback();
                return;
            }
            String access$1002 = PsDataOffExempt.LOG_TAG;
            AECLog.i(access$1002, "onAvailable link properties InterfaceName: " + lp.getInterfaceName() + ", LinkAddresses: " + lp.getLinkAddresses() + ", DnsAddresses: " + lp.getDnsServers(), PsDataOffExempt.this.mPhoneId);
            PsDataOffExempt.this.mWorkflowHandler.sendEmptyMessage(1008);
        }

        public void onLost(Network network) {
            String access$100 = PsDataOffExempt.LOG_TAG;
            AECLog.i(access$100, "onLost: " + network, PsDataOffExempt.this.mPhoneId);
        }
    }
}
