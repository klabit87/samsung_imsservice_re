package com.sec.internal.ims.entitlement.nsds;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NSDSMultiSimService extends Service {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = NSDSMultiSimService.class.getSimpleName();
    public static boolean[] mSimEvtRegistered = new boolean[SimUtil.getPhoneCount()];
    private static AtomicBoolean mVsimServiceIsRunning = new AtomicBoolean(false);
    private Context mContext = null;
    private ConnectivityManager.NetworkCallback mDefaultNetworkCallback = null;
    private Messenger mMessenger;
    protected Map<Integer, NSDSModuleBase> mModuleMap = new ConcurrentHashMap();
    protected ServiceHandler mServiceHandler;
    private Looper mServiceLooper;

    protected final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            String access$000 = NSDSMultiSimService.LOG_TAG;
            IMSLog.i(access$000, "handleMessage:" + msg.what);
            int i = msg.what;
            if (i == 3) {
                NSDSMultiSimService.this.activateSimDevice(msg.getData());
            } else if (i == 4) {
                NSDSMultiSimService.this.deactivateSimDevice(msg.getData());
            } else if (i == 5) {
                NSDSMultiSimService.this.bindNSDSMultiSimService();
            } else if (i == 19) {
                NSDSMultiSimService.this.updateE911Address(msg.getData());
            } else if (i == 49) {
                NSDSMultiSimService.this.retrieveAkaToken(msg.getData());
            } else if (i == 100) {
                NSDSMultiSimService.this.onEventSimReady((Bundle) msg.obj);
            } else if (i == 212) {
                NSDSMultiSimService.this.updateEntitlementUrl(msg.getData());
            } else if (i == 220) {
                NSDSMultiSimService.this.handleVoWifToggleOnEvent(msg.getData());
            } else if (i == 221) {
                NSDSMultiSimService.this.handleVoWifToggleOffEvent(msg.getData());
            } else if (i == 223) {
                NSDSMultiSimService.this.registerNsdsEventMessenger(msg.replyTo, msg.arg1);
            } else if (i == 224) {
                NSDSMultiSimService.this.unregisterNsdsEventMessenger(msg.replyTo, msg.arg1);
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        try {
            if (Extensions.UserHandle.myUserId() != 0) {
                IMSLog.i(LOG_TAG, "Do not initialize on non-system user");
                return;
            }
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "IllegalStateException occurred" + e.getMessage());
        }
        IMSLog.i(LOG_TAG, "onCreate");
        this.mContext = getApplicationContext();
        HandlerThread thread = new HandlerThread("NSDSMultiSimService", 10);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mMessenger = new Messenger(this.mServiceHandler);
        NSDSSimEventManager.createInstance(this.mServiceLooper, this);
        NSDSModuleFactory.createInstance(this.mServiceLooper, this);
        try {
            Message msg = new Message();
            msg.what = 5;
            this.mMessenger.send(msg);
        } catch (RemoteException e2) {
            IMSLog.i(LOG_TAG, "initialize failed");
        }
    }

    public IBinder onBind(Intent intent) {
        if (Extensions.UserHandle.myUserId() != 0) {
            IMSLog.i(LOG_TAG, "Do not allow bind on non-system user");
            return null;
        }
        IMSLog.i(LOG_TAG, "onBind");
        return this.mMessenger.getBinder();
    }

    public void onDestroy() {
        IMSLog.i(LOG_TAG, "onDestroy");
        if (this.mMessenger != null) {
            NSDSSimEventManager.getInstance().unregisterSimEventMessenger(this.mMessenger);
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        IMSLog.i(LOG_TAG, "onStartCommand");
        if (intent != null) {
            int slotIndex = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
            if (!mSimEvtRegistered[slotIndex]) {
                registerForSimEvents(slotIndex);
            }
            String str = LOG_TAG;
            IMSLog.i(str, "Received <" + slotIndex + "> startId:" + startId + " intent:" + intent);
            handleIntent(intent, flags, startId);
            return 1;
        }
        IMSLog.i(LOG_TAG, "handleIntent() - Intent is null. return....");
        return 1;
    }

    /* access modifiers changed from: private */
    public void bindNSDSMultiSimService() {
        IMSLog.i(LOG_TAG, "bindNSDSMultiSimService");
        this.mContext.getContentResolver().update(Uri.withAppendedPath(NSDSContractExt.AUTHORITY_URI, "binding_service"), new ContentValues(), (String) null, (String[]) null);
    }

    private void registerForSimEvents(int simSlot) {
        NSDSSimEventManager.getInstance().registerSimEventMessenger(this.mMessenger, simSlot);
        mSimEvtRegistered[simSlot] = true;
    }

    public void handleIntent(Intent intent, int flags, int startId) {
        String str = LOG_TAG;
        IMSLog.i(str, "Received startId:" + startId + " flags:" + flags + " intent:" + intent);
        String action = intent.getAction();
        String str2 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onStartCommand: ");
        sb.append(action);
        IMSLog.i(str2, sb.toString());
        String imsi = intent.getStringExtra("imsi");
        if (NSDSNamespaces.NSDSActions.ACTION_REFRESH_DEVICE_CONFIG.equals(action)) {
            refreshDeviceConfig(imsi);
        } else if (NSDSNamespaces.NSDSActions.ACTION_SIM_DEVICE_ACTIVATION.equals(action)) {
            activateSimDevice(11, 0);
        } else if (NSDSNamespaces.NSDSActions.ACTION_REFRESH_GCM_TOKEN.equals(action)) {
            getGcmRegistrationToken(imsi);
        }
    }

    /* access modifiers changed from: private */
    public void updateEntitlementUrl(Bundle data) {
        String imsi = data.getString("IMSI");
        String url = data.getString("URL");
        NSDSModuleBase nsdsModule = getNsdsModuleForImsi(imsi);
        if (nsdsModule != null) {
            nsdsModule.updateEntitlementUrl(url);
        }
    }

    public void refreshDeviceConfig(String imsi) {
        if (!TextUtils.isEmpty(imsi)) {
            NSDSModuleBase nsdsModule = getNsdsModuleForImsi(imsi);
            if (nsdsModule != null) {
                nsdsModule.queueRefreshDeviceConfig(0);
                return;
            }
            return;
        }
        IMSLog.s(LOG_TAG, "Refresh device config for all modules");
        for (NSDSModuleBase nsdsModule2 : this.mModuleMap.values()) {
            nsdsModule2.queueRefreshDeviceConfig(0);
        }
    }

    private void getGcmRegistrationToken(String imsi) {
        ISimManager sm;
        if (TextUtils.isEmpty(imsi) && (sm = getSimManager(0)) != null) {
            imsi = sm.getImsi();
        }
        NSDSModuleBase nsdsModule = getNsdsModuleForImsi(imsi);
        if (nsdsModule != null) {
            nsdsModule.queueGcmTokenRetrieval();
        }
    }

    private NSDSModuleBase getNsdsModuleForImsi(String imsi) {
        if (TextUtils.isEmpty(imsi)) {
            return getNsdsModuleForSimSlot(0);
        }
        ISimManager sm = getSimManager(imsi);
        if (sm != null && sm.getSimSlotIndex() != -1) {
            return getNsdsModuleForSimSlot(sm.getSimSlotIndex());
        }
        String str = LOG_TAG;
        IMSLog.s(str, "Could not find any NSDSModule for imsi:" + imsi + ", returning for sim slot 0");
        return getNsdsModuleForSimSlot(0);
    }

    private NSDSModuleBase getNsdsModuleForSimSlot(int simSlot) {
        NSDSModuleBase nsdsModule = this.mModuleMap.get(Integer.valueOf(simSlot));
        if (nsdsModule == null) {
            String str = LOG_TAG;
            IMSLog.s(str, "creating NSDSModule for simSlot:" + simSlot);
            nsdsModule = NSDSModuleFactory.getInstance().getNsdsModule(getSimManager(simSlot));
            if (nsdsModule != null) {
                this.mModuleMap.put(Integer.valueOf(simSlot), nsdsModule);
            }
        }
        return nsdsModule;
    }

    private ISimManager getSimManager(int simSlot) {
        return NSDSSimEventManager.getInstance().getSimManagerFromSimSlot(simSlot);
    }

    private ISimManager getSimManager(String imsi) {
        return NSDSSimEventManager.getInstance().getSimManager(imsi);
    }

    /* access modifiers changed from: private */
    public void registerNsdsEventMessenger(Messenger messenger, int slotid) {
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(slotid);
        if (nsdsModule != null) {
            nsdsModule.registerEventMessenger(messenger);
        }
    }

    /* access modifiers changed from: private */
    public void unregisterNsdsEventMessenger(Messenger messenger, int slotid) {
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(slotid);
        if (nsdsModule != null) {
            nsdsModule.unregisterEventMessenger(messenger);
        }
    }

    /* access modifiers changed from: private */
    public void retrieveAkaToken(Bundle data) {
        String imsi = data.getString("IMSI");
        int deviceEventType = data.getInt("EVENT_TYPE", 19);
        int retryCount = data.getInt("RETRYCOUNT", 0);
        NSDSModuleBase nsdsModule = getNsdsModuleForImsi(imsi);
        if (nsdsModule != null) {
            nsdsModule.retrieveAkaToken(deviceEventType, retryCount);
        }
    }

    private NSDSModuleBase getVSimModuleForSimSlot2(int simSlot) {
        return this.mModuleMap.get(Integer.valueOf(simSlot));
    }

    private void activateSimDevice(int deviceEventType, int retryCount) {
        String str = LOG_TAG;
        IMSLog.i(str, "activateSimDevice: deviceEventType " + deviceEventType);
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(0);
        if (nsdsModule != null) {
            nsdsModule.activateSimDevice(deviceEventType, retryCount);
        }
    }

    /* access modifiers changed from: private */
    public void activateSimDevice(Bundle data) {
        int slotid = data.getInt("SLOT_ID", 0);
        int deviceEventType = data.getInt("EVENT_TYPE", 11);
        int retryCount = data.getInt("RETRYCOUNT", 0);
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(slotid);
        if (nsdsModule != null) {
            nsdsModule.activateSimDevice(deviceEventType, retryCount);
        }
    }

    /* access modifiers changed from: private */
    public void updateE911Address(Bundle data) {
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(data.getInt("SLOT_ID", 0));
        if (nsdsModule != null) {
            nsdsModule.updateE911Address();
        }
    }

    /* access modifiers changed from: private */
    public void handleVoWifToggleOnEvent(Bundle data) {
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(data.getInt("SLOT_ID", 0));
        if (nsdsModule != null) {
            nsdsModule.handleVoWifToggleOnEvent();
        }
    }

    /* access modifiers changed from: private */
    public void handleVoWifToggleOffEvent(Bundle data) {
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(data.getInt("SLOT_ID", 0));
        if (nsdsModule != null) {
            nsdsModule.handleVoWifToggleOffEvent();
        }
    }

    /* access modifiers changed from: private */
    public void deactivateSimDevice(Bundle data) {
        NSDSModuleBase nsdsModule = getNsdsModuleForImsi(data.getString("IMSI"));
        if (nsdsModule != null) {
            nsdsModule.deactivateSimDevice(0);
        }
    }

    /* access modifiers changed from: private */
    public void onEventSimReady(Bundle bundle) {
        int phoneId = bundle.getInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
        boolean absent = bundle.getBoolean(NSDSNamespaces.NSDSExtras.SIM_ABSENT, false);
        boolean isSwapped = bundle.getBoolean(NSDSNamespaces.NSDSExtras.SIM_SWAPPED, false);
        String str = LOG_TAG;
        IMSLog.i(str, "onEventSimReady: isSimAbsent " + absent);
        if (absent) {
            onSimStateNotAvailable(phoneId);
            return;
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, phoneId, " isSimSwapped:" + isSwapped);
        NSDSModuleBase nsdsModule = getNsdsModuleForSimSlot(phoneId);
        if (nsdsModule != null) {
            nsdsModule.onSimReady(isSwapped);
            if (isDeviceReady()) {
                nsdsModule.onDeviceReady();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceReady() {
        if (!NSDSConfigHelper.isUserUnlocked(this.mContext)) {
            IMSLog.i(LOG_TAG, "isDeviceReady() User lock ");
            return false;
        }
        if (!MigrationHelper.checkMigrateDB(this.mContext)) {
            MigrationHelper.migrateDBToCe(this.mContext);
        }
        if (!NetworkUtil.isConnected(this.mContext)) {
            registerDefaultNetworkCallback();
            return false;
        }
        unregisterNetworkCallback();
        return true;
    }

    private ConnectivityManager.NetworkCallback getDefaultNetworkCallback() {
        return new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                IMSLog.i(NSDSMultiSimService.LOG_TAG, "onAvailable");
                if (network != null && NSDSMultiSimService.this.isDeviceReady()) {
                    NSDSMultiSimService.this.onDeviceReady();
                }
            }

            public void onLost(Network network) {
                IMSLog.i(NSDSMultiSimService.LOG_TAG, "onLost");
            }
        };
    }

    private void registerDefaultNetworkCallback() {
        ConnectivityManager cm;
        if (this.mDefaultNetworkCallback == null && (cm = (ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            IMSLog.i(LOG_TAG, "registerDefaultNetworkCallback");
            ConnectivityManager.NetworkCallback defaultNetworkCallback = getDefaultNetworkCallback();
            this.mDefaultNetworkCallback = defaultNetworkCallback;
            cm.registerDefaultNetworkCallback(defaultNetworkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        ConnectivityManager cm;
        if (this.mDefaultNetworkCallback != null && (cm = (ConnectivityManager) this.mContext.getSystemService("connectivity")) != null) {
            IMSLog.i(LOG_TAG, "unregisterNetworkCallback");
            cm.unregisterNetworkCallback(this.mDefaultNetworkCallback);
            this.mDefaultNetworkCallback = null;
        }
    }

    /* access modifiers changed from: private */
    public void onDeviceReady() {
        IMSLog.i(LOG_TAG, "onDeviceReady()");
        for (NSDSModuleBase nsdsModule : this.mModuleMap.values()) {
            nsdsModule.initForDeviceReady();
            nsdsModule.onDeviceReady();
        }
    }

    private void onSimStateNotAvailable(int phoneId) {
        IMSLog.i(LOG_TAG, "onSimStateNotAvailable()");
        NSDSModuleBase nsdsModule = getVSimModuleForSimSlot2(phoneId);
        if (nsdsModule != null) {
            nsdsModule.onSimNotAvailable();
        } else {
            IMSLog.i(LOG_TAG, "onSimStateNotAvailable() - nsdsModule is null");
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IMSLog.prepareDump(writer);
        for (NSDSModuleBase nsdsModule : this.mModuleMap.values()) {
            nsdsModule.dump();
        }
        IMSLog.postDump(writer);
    }

    public static void startNsdsMultiSimService(Context context, int phoneId) {
        IMSLog.i(LOG_TAG, "startNsdsMultiSimService()");
        Intent intent = new Intent(context, NSDSMultiSimService.class);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, phoneId);
        context.startService(intent);
    }
}
