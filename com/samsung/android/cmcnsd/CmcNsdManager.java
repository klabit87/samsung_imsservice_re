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
    private static final NsdNetwork DEFAULT_WIFI_AP_NETWORK = new NsdNetwork.Builder().setCapabilities(new NsdNetworkCapabilities.Builder().addTransport(0).setCapabilities(7).build()).build();
    private static final NsdNetwork DEFAULT_WIFI_DIRECT_NETWORK = new NsdNetwork.Builder().setCapabilities(new NsdNetworkCapabilities.Builder().addTransport(1).build()).build();
    private static final String SERVICE_PACKAGE = "com.samsung.android.mdecservice";
    private static final String VERSION = "0.0.1";
    /* access modifiers changed from: private */
    public final String TAG = (CmcNsdManager.class.getSimpleName() + "[" + hashCode() + "]");
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            String access$000 = CmcNsdManager.this.TAG;
            Log.w(access$000, "onServiceConnected() name=" + name);
            try {
                INsdService unused = CmcNsdManager.this.mService = INsdService.Stub.asInterface(service);
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

        public void onServiceDisconnected(ComponentName name) {
            String access$000 = CmcNsdManager.this.TAG;
            Log.w(access$000, "onServiceDisconnected() name=" + name);
            CmcNsdManager.this.onUnbound();
        }
    };
    private final Context mContext;
    private boolean mIsBound = false;
    private boolean mIsNetworkAcquired = false;
    /* access modifiers changed from: private */
    public final SparseArray<NsdNetwork> mLastNetworks = new SparseArray<>();
    private BindStatusListener mListener = null;
    /* access modifiers changed from: private */
    public final INsdNetworkCallback mNsdNetworkCallback = new INsdNetworkCallback.Stub() {
        public void onWifiApConnectionChanged(NsdNetwork network) throws RemoteException {
            String access$000 = CmcNsdManager.this.TAG;
            Log.i(access$000, "onWifiApConnectionChanged() network=" + network);
            if (network != null) {
                CmcNsdManager.this.mLastNetworks.put(network.getTransport(), network);
                CmcNsdManager.this.notifyWifiApConnectionChanged(network);
            }
        }

        public void onWifiDirectConnectionChanged(NsdNetwork network) throws RemoteException {
            String access$000 = CmcNsdManager.this.TAG;
            Log.i(access$000, "onWifiDirectConnectionChanged() network=" + network);
            if (network != null) {
                CmcNsdManager.this.mLastNetworks.put(network.getTransport(), network);
                CmcNsdManager.this.notifyWifiDirectConnectionChanged(network);
            }
        }

        public void onWifiApNetworkMessageReceived(NsdNetworkCapabilities capabilities, NsdNetworkMessage message) throws RemoteException {
            String access$000 = CmcNsdManager.this.TAG;
            Log.i(access$000, "onWifiApNetworkMessageReceived() cap=" + capabilities + " message=" + message);
            CmcNsdManager.this.notifyWifiApNetworkMessageReceived(capabilities, message);
        }
    };
    private final List<NetworkCallbackWrapper> mNsdNetworkCallbackList = new ArrayList();
    /* access modifiers changed from: private */
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

    public void registerServiceConnectionListener(BindStatusListener listener) {
        this.mListener = listener;
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
            this.mListener.onBound();
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
    public void notifyWifiApConnectionChanged(NsdNetwork network) {
        synchronized (this.mNsdNetworkCallbackList) {
            for (NetworkCallbackWrapper w : this.mNsdNetworkCallbackList) {
                if (w.getCapabilities().hasCapabilities(network.getCapabilities())) {
                    if (network.isConnected()) {
                        w.getCallback().onConnected(network);
                    } else {
                        w.getCallback().onDisconnected(network);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyWifiDirectConnectionChanged(NsdNetwork network) {
        synchronized (this.mNsdNetworkCallbackList) {
            for (NetworkCallbackWrapper w : this.mNsdNetworkCallbackList) {
                if (w.getCapabilities().hasCapabilities(network.getCapabilities())) {
                    if (!network.isConnected()) {
                        w.getCallback().onDisconnected(network);
                        this.mIsNetworkAcquired = false;
                    } else if (this.mIsNetworkAcquired) {
                        w.getCallback().onConnected(network);
                    } else {
                        w.getCallback().onAvailable(network.getCapabilities());
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyWifiApNetworkMessageReceived(NsdNetworkCapabilities capabilities, NsdNetworkMessage message) {
        synchronized (this.mNsdNetworkCallbackList) {
            for (NetworkCallbackWrapper w : this.mNsdNetworkCallbackList) {
                if (w.getCapabilities().hasCapabilities(capabilities)) {
                    w.getCallback().onNetworkMessageReceived(message);
                }
            }
        }
    }

    private static class NetworkCallbackWrapper {
        private final NsdNetworkCallback mCallback;
        private final NsdNetworkCapabilities mCapabilities;

        public NetworkCallbackWrapper(NsdNetworkCapabilities capabilities, NsdNetworkCallback callback) {
            this.mCapabilities = capabilities;
            this.mCallback = callback;
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
        r0 = r5.TAG;
        android.util.Log.d(r0, "registerNetworkCallback() NsdNetworkCallbackList=" + r5.mNsdNetworkCallbackList);
        r0 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005b, code lost:
        if (r0 > 1) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005d, code lost:
        r1 = r5.mLastNetworks.get(1 << r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0066, code lost:
        if (r1 == null) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0068, code lost:
        if (r0 != 0) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x006a, code lost:
        notifyWifiApConnectionChanged(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006e, code lost:
        notifyWifiDirectConnectionChanged(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0071, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0074, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean registerNetworkCallback(com.samsung.android.cmcnsd.network.NsdNetworkCapabilities r6, com.samsung.android.cmcnsd.network.NsdNetworkCallback r7) {
        /*
            r5 = this;
            java.lang.String r0 = r5.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "registerNetworkCallback() cap="
            r1.append(r2)
            r1.append(r6)
            java.lang.String r2 = " callback="
            r1.append(r2)
            r1.append(r7)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r0 = 0
            if (r6 == 0) goto L_0x0078
            if (r7 != 0) goto L_0x0025
            goto L_0x0078
        L_0x0025:
            java.util.List<com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper> r1 = r5.mNsdNetworkCallbackList
            monitor-enter(r1)
            com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper r2 = new com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper     // Catch:{ all -> 0x0075 }
            r2.<init>(r6, r7)     // Catch:{ all -> 0x0075 }
            java.util.List<com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper> r3 = r5.mNsdNetworkCallbackList     // Catch:{ all -> 0x0075 }
            boolean r3 = r3.add(r2)     // Catch:{ all -> 0x0075 }
            if (r3 != 0) goto L_0x003f
            java.lang.String r3 = r5.TAG     // Catch:{ all -> 0x0075 }
            java.lang.String r4 = "registerNetworkCallback() failed to add callback"
            android.util.Log.e(r3, r4)     // Catch:{ all -> 0x0075 }
            monitor-exit(r1)     // Catch:{ all -> 0x0075 }
            return r0
        L_0x003f:
            monitor-exit(r1)     // Catch:{ all -> 0x0075 }
            java.lang.String r0 = r5.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "registerNetworkCallback() NsdNetworkCallbackList="
            r1.append(r2)
            java.util.List<com.samsung.android.cmcnsd.CmcNsdManager$NetworkCallbackWrapper> r2 = r5.mNsdNetworkCallbackList
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            r0 = 0
        L_0x005a:
            r1 = 1
            if (r0 > r1) goto L_0x0074
            android.util.SparseArray<com.samsung.android.cmcnsd.network.NsdNetwork> r2 = r5.mLastNetworks
            int r1 = r1 << r0
            java.lang.Object r1 = r2.get(r1)
            com.samsung.android.cmcnsd.network.NsdNetwork r1 = (com.samsung.android.cmcnsd.network.NsdNetwork) r1
            if (r1 == 0) goto L_0x0071
            if (r0 != 0) goto L_0x006e
            r5.notifyWifiApConnectionChanged(r1)
            goto L_0x0071
        L_0x006e:
            r5.notifyWifiDirectConnectionChanged(r1)
        L_0x0071:
            int r0 = r0 + 1
            goto L_0x005a
        L_0x0074:
            return r1
        L_0x0075:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0075 }
            throw r0
        L_0x0078:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.samsung.android.cmcnsd.CmcNsdManager.registerNetworkCallback(com.samsung.android.cmcnsd.network.NsdNetworkCapabilities, com.samsung.android.cmcnsd.network.NsdNetworkCallback):boolean");
    }

    public void unregisterNetworkCallback(NsdNetworkCallback callback) {
        String str = this.TAG;
        Log.i(str, "unregisterNetworkCallback() callback=" + callback);
        synchronized (this.mNsdNetworkCallbackList) {
            Iterator<NetworkCallbackWrapper> it = this.mNsdNetworkCallbackList.iterator();
            while (it.hasNext()) {
                if (it.next().getCallback() == callback) {
                    it.remove();
                }
            }
        }
        String str2 = this.TAG;
        Log.d(str2, "unregisterNetworkCallback() NsdNetworkCallbackList=" + this.mNsdNetworkCallbackList);
    }

    public boolean acquireNetwork(NsdNetworkCapabilities capabilities) {
        Log.e(this.TAG, "acquireNetwork() NOT SUPPORTED");
        return false;
    }

    public void releaseNetwork() {
        Log.e(this.TAG, "releaseNetwork() NOT SUPPORTED");
    }

    public boolean sendNetworkMessage(String deviceId, NsdNetworkCapabilities capabilities, NsdNetworkMessage message) {
        String str = this.TAG;
        Log.i(str, "sendNetworkMessage() cap=" + capabilities + " msg=" + message);
        if (!isBound()) {
            return false;
        }
        try {
            return this.mService.sendNetworkMessage(hashCode(), deviceId, capabilities, message);
        } catch (RemoteException e) {
            String str2 = this.TAG;
            Log.e(str2, "failed to sendNetworkMessage()=" + e.getMessage());
            return false;
        }
    }
}
