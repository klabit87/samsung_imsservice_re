package com.samsung.android.cmcnsd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import com.samsung.android.cmcnsd.INsdNetworkCallback;
import com.samsung.android.cmcnsd.INsdService;
import com.samsung.android.cmcnsd.extension.ContextExt;
import com.samsung.android.cmcnsd.network.NsdNetwork;
import com.samsung.android.cmcnsd.network.NsdNetworkCallback;
import com.samsung.android.cmcnsd.network.NsdNetworkCapabilities;
import com.samsung.android.cmcnsd.network.NsdNetworkMessage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CmcNsdManager {
    public static final NsdNetwork DEFAULT_WIFI_AP_NETWORK = new NsdNetwork.Builder().setCapabilities(new NsdNetworkCapabilities.Builder().addTransport(0).setCapabilities(7).build()).build();
    public static final NsdNetwork DEFAULT_WIFI_DIRECT_NETWORK = new NsdNetwork.Builder().setCapabilities(new NsdNetworkCapabilities.Builder().addTransport(1).build()).build();
    public static final String SERVICE_PACKAGE = "com.samsung.android.mdecservice";
    public static final String VERSION = "0.0.2";
    public final String TAG = (CmcNsdManager.class.getSimpleName() + "[" + hashCode() + "]");
    public final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            String access$000 = CmcNsdManager.this.TAG;
            Log.w(access$000, "onServiceConnected() name=" + componentName);
            try {
                INsdService unused = CmcNsdManager.this.mService = INsdService.Stub.asInterface(iBinder);
                if (CmcNsdManager.this.mService == null) {
                    Log.e(CmcNsdManager.this.TAG, "onServiceConnected() failed to get proxy");
                    return;
                }
                CmcNsdManager.this.mService.registerNetworkCallback(CmcNsdManager.this.hashCode(), CmcNsdManager.this.mNsdNetworkCallback);
                CmcNsdManager.this.onBound();
            } catch (Exception e) {
                String access$0002 = CmcNsdManager.this.TAG;
                Log.e(access$0002, "onServiceConnected exception=" + e.getMessage());
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            String access$000 = CmcNsdManager.this.TAG;
            Log.w(access$000, "onServiceDisconnected() name=" + componentName);
            CmcNsdManager.this.onUnbound();
        }
    };
    public final Context mContext;
    public boolean mIsBound = false;
    public boolean mIsNetworkAcquired = false;
    public final SparseArray<NsdNetwork> mLastNetworks = new SparseArray<>();
    public BindStatusListener mListener = null;
    public final INsdNetworkCallback mNsdNetworkCallback = new INsdNetworkCallback.Stub() {
        public void onWifiApConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException {
            String access$000 = CmcNsdManager.this.TAG;
            Log.i(access$000, "onWifiApConnectionChanged() network=" + nsdNetwork);
            if (nsdNetwork != null) {
                CmcNsdManager.this.mLastNetworks.put(nsdNetwork.getTransport(), nsdNetwork);
                CmcNsdManager.this.notifyWifiApConnectionChanged(nsdNetwork);
            }
        }

        public void onWifiDirectConnectionChanged(NsdNetwork nsdNetwork) throws RemoteException {
            String access$000 = CmcNsdManager.this.TAG;
            Log.i(access$000, "onWifiDirectConnectionChanged() network=" + nsdNetwork);
            if (nsdNetwork != null) {
                CmcNsdManager.this.mLastNetworks.put(nsdNetwork.getTransport(), nsdNetwork);
                CmcNsdManager.this.notifyWifiDirectConnectionChanged(nsdNetwork);
            }
        }

        public void onWifiApNetworkMessageReceived(NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) throws RemoteException {
            String access$000 = CmcNsdManager.this.TAG;
            Log.i(access$000, "onWifiApNetworkMessageReceived() cap=" + nsdNetworkCapabilities + " message=" + nsdNetworkMessage);
            CmcNsdManager.this.notifyWifiApNetworkMessageReceived(nsdNetworkCapabilities, nsdNetworkMessage);
        }
    };
    public final List<NetworkCallbackWrapper> mNsdNetworkCallbackList = new ArrayList();
    public INsdService mService = null;

    public interface BindStatusListener {
        void onBound();

        void onUnbound();
    }

    public CmcNsdManager(Context context) {
        this.mContext = context;
        this.mLastNetworks.put(1, DEFAULT_WIFI_AP_NETWORK);
        this.mLastNetworks.put(2, DEFAULT_WIFI_DIRECT_NETWORK);
        String str = this.TAG;
        Log.i(str, "pkgName=" + context.getPackageName() + " version=" + VERSION);
    }

    public void registerServiceConnectionListener(BindStatusListener bindStatusListener) {
        this.mListener = bindStatusListener;
    }

    public void unregisterServiceConnectionListener() {
        this.mListener = null;
    }

    public void bind() {
        if (this.mContext == null) {
            Log.e(this.TAG, "bind() context null");
            return;
        }
        String str = this.TAG;
        Log.i(str, "bind() isBound=" + isBound());
        if (isBound()) {
            onBound();
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(SERVICE_PACKAGE, "com.samsung.android.mdecservice.nsd.NsdService");
        this.mIsBound = ContextExt.bindServiceAsUser(this.mContext, intent, this.mConnection, 1, ContextExt.CURRENT_OR_SELF);
    }

    public void unbind() {
        if (this.mContext == null) {
            Log.e(this.TAG, "unbind() context null");
            return;
        }
        String str = this.TAG;
        Log.i(str, "unbind() isBound=" + isBound());
        if (isBound()) {
            try {
                this.mService.unregisterNetworkCallback(this.mNsdNetworkCallback);
            } catch (RemoteException e) {
                String str2 = this.TAG;
                Log.e(str2, "unbind() exception=" + e.getMessage());
            }
            this.mContext.unbindService(this.mConnection);
        }
        onUnbound();
    }

    public boolean isBound() {
        return this.mService != null && this.mIsBound;
    }

    /* access modifiers changed from: private */
    public void onBound() {
        BindStatusListener bindStatusListener = this.mListener;
        if (bindStatusListener != null) {
            bindStatusListener.onBound();
        }
    }

    /* access modifiers changed from: private */
    public void onUnbound() {
        this.mService = null;
        this.mIsBound = false;
        synchronized (this.mNsdNetworkCallbackList) {
            this.mNsdNetworkCallbackList.clear();
        }
        this.mLastNetworks.put(1, DEFAULT_WIFI_AP_NETWORK);
        this.mLastNetworks.put(2, DEFAULT_WIFI_DIRECT_NETWORK);
        BindStatusListener bindStatusListener = this.mListener;
        if (bindStatusListener != null) {
            bindStatusListener.onUnbound();
        }
    }

    /* access modifiers changed from: private */
    public void notifyWifiApConnectionChanged(NsdNetwork nsdNetwork) {
        synchronized (this.mNsdNetworkCallbackList) {
            for (NetworkCallbackWrapper next : this.mNsdNetworkCallbackList) {
                if (next.getCapabilities().hasCapabilities(nsdNetwork.getCapabilities())) {
                    if (nsdNetwork.isConnected()) {
                        next.getCallback().onConnected(nsdNetwork);
                    } else {
                        next.getCallback().onDisconnected(nsdNetwork);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyWifiDirectConnectionChanged(NsdNetwork nsdNetwork) {
        synchronized (this.mNsdNetworkCallbackList) {
            for (NetworkCallbackWrapper next : this.mNsdNetworkCallbackList) {
                if (next.getCapabilities().hasCapabilities(nsdNetwork.getCapabilities())) {
                    if (!nsdNetwork.isConnected()) {
                        next.getCallback().onDisconnected(nsdNetwork);
                        this.mIsNetworkAcquired = false;
                    } else if (this.mIsNetworkAcquired) {
                        next.getCallback().onConnected(nsdNetwork);
                    } else {
                        next.getCallback().onAvailable(nsdNetwork.getCapabilities());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyWifiApNetworkMessageReceived(NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) {
        synchronized (this.mNsdNetworkCallbackList) {
            for (NetworkCallbackWrapper next : this.mNsdNetworkCallbackList) {
                if (next.getCapabilities().hasCapabilities(nsdNetworkCapabilities)) {
                    next.getCallback().onNetworkMessageReceived(nsdNetworkMessage);
                }
            }
        }
    }

    public static class NetworkCallbackWrapper {
        public final NsdNetworkCallback mCallback;
        public final NsdNetworkCapabilities mCapabilities;

        public NetworkCallbackWrapper(NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkCallback nsdNetworkCallback) {
            this.mCapabilities = nsdNetworkCapabilities;
            this.mCallback = nsdNetworkCallback;
        }

        public NsdNetworkCapabilities getCapabilities() {
            return this.mCapabilities;
        }

        public NsdNetworkCallback getCallback() {
            return this.mCallback;
        }

        public String toString() {
            return this.mCapabilities + "@" + this.mCallback;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0040, code lost:
        r4 = r3.TAG;
        android.util.Log.d(r4, "registerNetworkCallback() NsdNetworkCallbackList=" + r3.mNsdNetworkCallbackList);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005a, code lost:
        if (r0 > 1) goto L_0x0073;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005c, code lost:
        r4 = r3.mLastNetworks.get(1 << r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0065, code lost:
        if (r4 == null) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0067, code lost:
        if (r0 != 0) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0069, code lost:
        notifyWifiApConnectionChanged(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006d, code lost:
        notifyWifiDirectConnectionChanged(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0070, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0073, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean registerNetworkCallback(com.samsung.android.cmcnsd.network.NsdNetworkCapabilities r4, com.samsung.android.cmcnsd.network.NsdNetworkCallback r5) {
        /*
            r3 = this;
            java.lang.String r0 = r3.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "registerNetworkCallback() cap="
            r1.append(r2)
            r1.append(r4)
            java.lang.String r2 = " callback="
            r1.append(r2)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r0 = 0
            if (r4 == 0) goto L_0x0077
            if (r5 != 0) goto L_0x0025
            goto L_0x0077
        L_0x0025:
            java.util.List<com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper> r1 = r3.mNsdNetworkCallbackList
            monitor-enter(r1)
            com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper r2 = new com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper     // Catch:{ all -> 0x0074 }
            r2.<init>(r4, r5)     // Catch:{ all -> 0x0074 }
            java.util.List<com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper> r4 = r3.mNsdNetworkCallbackList     // Catch:{ all -> 0x0074 }
            boolean r4 = r4.add(r2)     // Catch:{ all -> 0x0074 }
            if (r4 != 0) goto L_0x003f
            java.lang.String r4 = r3.TAG     // Catch:{ all -> 0x0074 }
            java.lang.String r5 = "registerNetworkCallback() failed to add callback"
            android.util.Log.e(r4, r5)     // Catch:{ all -> 0x0074 }
            monitor-exit(r1)     // Catch:{ all -> 0x0074 }
            return r0
        L_0x003f:
            monitor-exit(r1)     // Catch:{ all -> 0x0074 }
            java.lang.String r4 = r3.TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r1 = "registerNetworkCallback() NsdNetworkCallbackList="
            r5.append(r1)
            java.util.List<com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper> r1 = r3.mNsdNetworkCallbackList
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r4, r5)
        L_0x0059:
            r4 = 1
            if (r0 > r4) goto L_0x0073
            android.util.SparseArray<com.samsung.android.cmcnsd.network.NsdNetwork> r5 = r3.mLastNetworks
            int r4 = r4 << r0
            java.lang.Object r4 = r5.get(r4)
            com.samsung.android.cmcnsd.network.NsdNetwork r4 = (com.samsung.android.cmcnsd.network.NsdNetwork) r4
            if (r4 == 0) goto L_0x0070
            if (r0 != 0) goto L_0x006d
            r3.notifyWifiApConnectionChanged(r4)
            goto L_0x0070
        L_0x006d:
            r3.notifyWifiDirectConnectionChanged(r4)
        L_0x0070:
            int r0 = r0 + 1
            goto L_0x0059
        L_0x0073:
            return r4
        L_0x0074:
            r4 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0074 }
            throw r4
        L_0x0077:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.android.cmcnsd.CmcNsdManager.registerNetworkCallback(com.samsung.android.cmcnsd.network.NsdNetworkCapabilities, com.samsung.android.cmcnsd.network.NsdNetworkCallback):boolean");
    }

    public void unregisterNetworkCallback(NsdNetworkCallback nsdNetworkCallback) {
        String str = this.TAG;
        Log.i(str, "unregisterNetworkCallback() callback=" + nsdNetworkCallback);
        synchronized (this.mNsdNetworkCallbackList) {
            Iterator<NetworkCallbackWrapper> it = this.mNsdNetworkCallbackList.iterator();
            while (it.hasNext()) {
                if (it.next().getCallback() == nsdNetworkCallback) {
                    it.remove();
                }
            }
        }
        String str2 = this.TAG;
        Log.d(str2, "unregisterNetworkCallback() NsdNetworkCallbackList=" + this.mNsdNetworkCallbackList);
    }

    public boolean acquireNetwork(NsdNetworkCapabilities nsdNetworkCapabilities) {
        Log.e(this.TAG, "acquireNetwork() NOT SUPPORTED");
        return false;
    }

    public void releaseNetwork() {
        Log.e(this.TAG, "releaseNetwork() NOT SUPPORTED");
    }

    public boolean sendNetworkMessage(String str, NsdNetworkCapabilities nsdNetworkCapabilities, NsdNetworkMessage nsdNetworkMessage) {
        String str2 = this.TAG;
        Log.i(str2, "sendNetworkMessage() cap=" + nsdNetworkCapabilities + " msg=" + nsdNetworkMessage);
        if (!isBound()) {
            return false;
        }
        try {
            return this.mService.sendNetworkMessage(hashCode(), str, nsdNetworkCapabilities, nsdNetworkMessage);
        } catch (RemoteException e) {
            String str3 = this.TAG;
            Log.e(str3, "failed to sendNetworkMessage()=" + e.getMessage());
            return false;
        }
    }
}
