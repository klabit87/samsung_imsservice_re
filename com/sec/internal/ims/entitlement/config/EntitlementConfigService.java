package com.sec.internal.ims.entitlement.config;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.NSDSConfigModule;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.nsds.NSDSSimEventManager;
import com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper;
import com.sec.internal.ims.entitlement.storagehelper.MigrationHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class EntitlementConfigService extends Service {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = EntitlementConfigService.class.getSimpleName();
    protected boolean mConRcvRegistered = false;
    protected boolean mConfigRcvRegistered = false;
    protected BroadcastReceiver mConfigReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String access$000 = EntitlementConfigService.LOG_TAG;
            IMSLog.i(access$000, "onReceive: " + intent.getAction());
            if (NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED.equals(intent.getAction())) {
                SimpleEventLog access$100 = EntitlementConfigService.this.mEventLog;
                access$100.logAndAdd(EntitlementConfigService.LOG_TAG + " DEVICE_CONFIG_UPDATED result [" + intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false) + "],  errorcode [" + intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, 0) + "]");
                StringBuilder sb = new StringBuilder();
                sb.append("RESULT:");
                sb.append(intent.getBooleanExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false));
                sb.append(",ERRC:");
                sb.append(intent.getIntExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, 0));
                IMSLog.c(LogClass.ES_DC_INTENT_RESULT, sb.toString());
            } else if (EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG.equals(intent.getAction()) || EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG.equals(intent.getAction())) {
                EntitlementConfigService.this.mServiceHandler.sendMessage(EntitlementConfigService.this.mServiceHandler.obtainMessage(107, Integer.valueOf(intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0))));
            }
        }
    };
    private Context mContext = null;
    private ConnectivityManager.NetworkCallback mDefaultNetworkCallback = null;
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog = null;
    private Messenger mMessenger;
    protected Map<Integer, EntitlementConfigModuleBase> mModuleMap = new HashMap();
    private NSDSDatabaseHelper mNSDSDatabaseHelper;
    /* access modifiers changed from: private */
    public ServiceHandler mServiceHandler;
    protected Looper mServiceLooper;
    protected boolean[] mSimEvtRegistered = new boolean[SimUtil.getPhoneCount()];
    private ISimManager mSimManager = SimManagerFactory.getSimManager();

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            String access$000 = EntitlementConfigService.LOG_TAG;
            IMSLog.i(access$000, "handleMessage:" + msg.what);
            int i = msg.what;
            if (i == 100) {
                EntitlementConfigService.this.onEventSimReady((Bundle) msg.obj);
            } else if (i == 200) {
                EntitlementConfigService.this.retriveAkaToken(((Integer) msg.obj).intValue());
            } else if (i != 201) {
                switch (i) {
                    case 106:
                        EntitlementConfigService.this.initEntitlementConfigService();
                        return;
                    case 107:
                        EntitlementConfigService.this.onDeviceReady(((Integer) msg.obj).intValue());
                        return;
                    case 108:
                        EntitlementConfigService.this.forceConfigUpdate(((Integer) msg.obj).intValue());
                        return;
                    default:
                        return;
                }
            } else {
                EntitlementConfigService.this.updateEntitlementUrl(msg.getData());
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        IMSLog.i(LOG_TAG, "onCreate");
        HandlerThread thread = new HandlerThread("EntitlementConfigService", 10);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mMessenger = new Messenger(this.mServiceHandler);
        this.mContext = getApplicationContext();
        this.mEventLog = new SimpleEventLog(this.mContext, LOG_TAG, 20);
        this.mNSDSDatabaseHelper = new NSDSDatabaseHelper(this.mContext);
        NSDSSimEventManager.createInstance(this.mServiceLooper, this);
        EntitlementConfigFactory.createInstance(this.mServiceLooper, this);
        try {
            Message msg = new Message();
            msg.what = 106;
            this.mMessenger.send(msg);
        } catch (RemoteException e) {
            IMSLog.i(LOG_TAG, "initialize failed");
        }
    }

    public IBinder onBind(Intent intent) {
        IMSLog.i(LOG_TAG, "onBind");
        return this.mMessenger.getBinder();
    }

    public void onDestroy() {
        IMSLog.i(LOG_TAG, "onDestroy");
        if (this.mMessenger != null) {
            NSDSSimEventManager.getInstance().unregisterSimEventMessenger(this.mMessenger);
        }
        unregisterConfigReceiver();
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        IMSLog.i(LOG_TAG, "onStartCommand");
        if (intent != null) {
            int slotIndex = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
            if (!this.mSimEvtRegistered[slotIndex]) {
                registerForSimEvents(slotIndex);
            }
            String str = LOG_TAG;
            IMSLog.i(str, "Received <" + slotIndex + "> startId:" + startId + " intent:" + intent);
            if (EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG.equals(intent.getAction()) || EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG.equals(intent.getAction())) {
                int eventType = intent.getIntExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, 0);
                int simSlotIdx = intent.getIntExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
                if (eventType == 18) {
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(108, Integer.valueOf(simSlotIdx)));
                } else if (eventType != 19) {
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(107, Integer.valueOf(simSlotIdx)));
                } else {
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(200, Integer.valueOf(simSlotIdx)));
                }
            }
            return 1;
        }
        IMSLog.i(LOG_TAG, "handleIntent() - Intent is null. return....");
        return 1;
    }

    /* access modifiers changed from: private */
    public void initEntitlementConfigService() {
        IMSLog.i(LOG_TAG, "initEntitlementConfigService");
        registerConfigReceiver();
        this.mContext.getContentResolver().update(Uri.withAppendedPath(EntitlementConfigContract.AUTHORITY_URI, "binding_service"), new ContentValues(), (String) null, (String[]) null);
    }

    private void registerForSimEvents(int simSlot) {
        NSDSSimEventManager.getInstance().registerSimEventMessenger(this.mMessenger, simSlot);
        this.mSimEvtRegistered[simSlot] = true;
    }

    /* access modifiers changed from: private */
    public void onEventSimReady(Bundle bundle) {
        int phoneId = bundle.getInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, 0);
        boolean absent = bundle.getBoolean(NSDSNamespaces.NSDSExtras.SIM_ABSENT, false);
        boolean isSwapped = bundle.getBoolean(NSDSNamespaces.NSDSExtras.SIM_SWAPPED, false);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "onEventSimReady: isSimAbsent " + absent + " isSimSwapped " + isSwapped);
        if (!absent) {
            EntitlementConfigModuleBase configModule = addAndGetConfigModule(phoneId);
            MnoNsdsConfigStrategyCreator.updateMnoStrategy(this.mContext, phoneId);
            resetDeviceConfigState();
            if (configModule != null) {
                configModule.onSimReady(isSwapped);
                if (isDeviceReady()) {
                    configModule.onDeviceReady();
                    return;
                }
                return;
            }
            IMSLog.e(LOG_TAG, "onEventSimReady: config module was not initiated");
        } else if (this.mModuleMap.get(Integer.valueOf(phoneId)) != null) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "remove nsdsconfigmodule for " + phoneId);
            this.mModuleMap.remove(Integer.valueOf(phoneId));
        }
    }

    /* access modifiers changed from: protected */
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

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0024, code lost:
        r3 = com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper.getDeviceId(r9.mContext, r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void resetDeviceConfigState() {
        /*
            r9 = this;
            java.util.List r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getAllSimManagers()
            java.util.Iterator r0 = r0.iterator()
        L_0x0008:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x005d
            java.lang.Object r1 = r0.next()
            com.sec.internal.interfaces.ims.core.ISimManager r1 = (com.sec.internal.interfaces.ims.core.ISimManager) r1
            int r2 = r1.getSimSlotIndex()
            java.util.Map<java.lang.Integer, com.sec.internal.ims.entitlement.config.EntitlementConfigModuleBase> r3 = r9.mModuleMap
            java.lang.Integer r4 = java.lang.Integer.valueOf(r2)
            java.lang.Object r3 = r3.get(r4)
            if (r3 != 0) goto L_0x005c
            android.content.Context r3 = r9.mContext
            java.lang.String r3 = com.sec.internal.ims.entitlement.storagehelper.DeviceIdHelper.getDeviceId(r3, r2)
            android.content.Context r4 = r9.mContext
            java.lang.String r5 = "device_config_state"
            java.lang.String r4 = com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.get(r4, r3, r5)
            if (r4 == 0) goto L_0x005c
            java.lang.String r6 = "deviceconfig_in_progress"
            boolean r6 = r6.equals(r4)
            if (r6 == 0) goto L_0x005c
            java.lang.String r6 = LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "["
            r7.append(r8)
            r7.append(r2)
            java.lang.String r8 = "] reset... device config state"
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.i(r6, r7)
            android.content.Context r6 = r9.mContext
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.remove(r6, r3, r5)
        L_0x005c:
            goto L_0x0008
        L_0x005d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.config.EntitlementConfigService.resetDeviceConfigState():void");
    }

    private ConnectivityManager.NetworkCallback getDefaultNetworkCallback() {
        return new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                IMSLog.i(EntitlementConfigService.LOG_TAG, "onAvailable");
                if (network != null && EntitlementConfigService.this.isDeviceReady()) {
                    EntitlementConfigService.this.onDeviceReady();
                }
            }

            public void onLost(Network network) {
                IMSLog.i(EntitlementConfigService.LOG_TAG, "onLost");
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

    private void registerConfigReceiver() {
        if (!this.mConfigRcvRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED);
            filter.addAction(EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG);
            filter.addAction(EntitlementNamespaces.EntitlementActions.ACTION_RETRY_DEVICE_CONFIG);
            registerReceiver(this.mConfigReceiver, filter);
            this.mConfigRcvRegistered = true;
        }
    }

    private void unregisterConfigReceiver() {
        if (this.mConfigRcvRegistered) {
            unregisterReceiver(this.mConfigReceiver);
            this.mConfigReceiver = null;
            this.mConfigRcvRegistered = false;
        }
    }

    private EntitlementConfigModuleBase addAndGetConfigModule(int phoneId) {
        EntitlementConfigModuleBase entitlementConfigModuleBase;
        EntitlementConfigModuleBase configModule = this.mModuleMap.get(Integer.valueOf(phoneId));
        if (configModule == null) {
            if (EntitlementConfigFactory.getInstance() == null) {
                entitlementConfigModuleBase = null;
            } else {
                entitlementConfigModuleBase = EntitlementConfigFactory.getInstance().getDeviceConfigModule(getSimManager(phoneId));
            }
            configModule = entitlementConfigModuleBase;
            if (configModule != null) {
                IMSLog.i(LOG_TAG, phoneId, "addAndGetConfigModule: added for phoneId ");
                this.mModuleMap.put(Integer.valueOf(phoneId), configModule);
            }
        }
        return configModule;
    }

    private ISimManager getSimManager(int simSlot) {
        return NSDSSimEventManager.getInstance().getSimManagerFromSimSlot(simSlot);
    }

    /* access modifiers changed from: private */
    public void onDeviceReady(int simslot) {
        IMSLog.i(LOG_TAG, simslot, "onDeviceReady");
        EntitlementConfigModuleBase configModule = this.mModuleMap.get(Integer.valueOf(simslot));
        if (configModule != null) {
            configModule.onDeviceReady();
        }
    }

    /* access modifiers changed from: private */
    public void onDeviceReady() {
        IMSLog.i(LOG_TAG, "onDeviceReady");
        for (EntitlementConfigModuleBase configModule : this.mModuleMap.values()) {
            configModule.onDeviceReady();
        }
    }

    /* access modifiers changed from: private */
    public void forceConfigUpdate(int phoneId) {
        EntitlementConfigModuleBase configModule = this.mModuleMap.get(Integer.valueOf(phoneId));
        if (configModule == null) {
            IMSLog.e(LOG_TAG, phoneId, "configModule is null");
        } else if (configModule instanceof NSDSConfigModule) {
            configModule.forceConfigUpdate();
        } else {
            IMSLog.i(LOG_TAG, "check why config module is not instance of NSDSConfigModule");
        }
    }

    /* access modifiers changed from: private */
    public void retriveAkaToken(int phoneId) {
        EntitlementConfigModuleBase configModule = this.mModuleMap.get(Integer.valueOf(phoneId));
        if (configModule == null) {
            IMSLog.e(LOG_TAG, phoneId, "configModule is null");
        } else if (configModule instanceof NSDSConfigModule) {
            configModule.retriveAkaToken();
        } else {
            IMSLog.i(LOG_TAG, "check why config module is not instance of NSDSConfigModule");
        }
    }

    /* access modifiers changed from: private */
    public void updateEntitlementUrl(Bundle data) {
        String url = data.getString("URL");
        String str = LOG_TAG;
        IMSLog.i(str, "updateEntitlementUrl: url " + url);
        if (!TextUtils.isEmpty(url)) {
            String deviceid = DeviceIdHelper.getDeviceId(this.mContext, this.mSimManager.getSimSlotIndex());
            this.mContext.getContentResolver().delete(EntitlementConfigContract.DeviceConfig.CONTENT_URI, (String) null, (String[]) null);
            this.mNSDSDatabaseHelper.deleteConfigAndResetDeviceAndAccountStatus(deviceid, this.mSimManager.getImsi(), this.mSimManager.getSimSlotIndex());
            NSDSSharedPrefHelper.setEntitlementServerUrl(this.mContext, deviceid, url);
        }
    }

    public static void startEntitlementConfigService(Context context, int phoneId) {
        IMSLog.i(LOG_TAG, "startEntitlementConfigService()");
        Intent intent = new Intent(context, EntitlementConfigService.class);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, phoneId);
        context.startService(intent);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        IMSLog.prepareDump(writer);
        this.mEventLog.dump();
        IMSLog.postDump(writer);
    }
}
