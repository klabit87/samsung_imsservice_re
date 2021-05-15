package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Handler;
import com.sec.internal.log.AECLog;

public class DefaultNetwork {
    /* access modifiers changed from: private */
    public final String LOG_TAG = DefaultNetwork.class.getSimpleName();
    /* access modifiers changed from: private */
    public final ConnectivityManager mConnMgr;
    ConnectivityManager.NetworkCallback mDefaultNetworkCallback = null;
    /* access modifiers changed from: private */
    public final Handler mModuleHandler;

    public DefaultNetwork(Context context, Handler handler) {
        this.mModuleHandler = handler;
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public void registerDefaultNetworkCallback() {
        if (this.mDefaultNetworkCallback == null) {
            ConnectivityManager.NetworkCallback defaultNetworkCallback = getDefaultNetworkCallback();
            this.mDefaultNetworkCallback = defaultNetworkCallback;
            this.mConnMgr.registerDefaultNetworkCallback(defaultNetworkCallback);
        }
    }

    public void unregisterNetworkCallback() {
        ConnectivityManager.NetworkCallback networkCallback = this.mDefaultNetworkCallback;
        if (networkCallback != null) {
            this.mConnMgr.unregisterNetworkCallback(networkCallback);
            this.mDefaultNetworkCallback = null;
        }
    }

    private ConnectivityManager.NetworkCallback getDefaultNetworkCallback() {
        return new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                if (network != null) {
                    NetworkInfo activeNetworkInfo = DefaultNetwork.this.mConnMgr.getActiveNetworkInfo();
                    boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
                    String access$100 = DefaultNetwork.this.LOG_TAG;
                    AECLog.i(access$100, "onAvailable: connected [" + isConnected + "]");
                    DefaultNetwork.this.mModuleHandler.sendEmptyMessage(3);
                }
            }

            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                if (network != null) {
                    boolean isOnline = networkCapabilities != null && networkCapabilities.hasCapability(12) && networkCapabilities.hasCapability(16);
                    String access$100 = DefaultNetwork.this.LOG_TAG;
                    AECLog.i(access$100, "onCapabilitiesChanged: online [" + isOnline + "]");
                    DefaultNetwork.this.mModuleHandler.sendEmptyMessage(3);
                }
            }

            public void onLost(Network network) {
                AECLog.i(DefaultNetwork.this.LOG_TAG, "onLost");
            }
        };
    }
}
