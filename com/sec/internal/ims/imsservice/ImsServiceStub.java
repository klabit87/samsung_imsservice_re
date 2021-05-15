package com.sec.internal.ims.imsservice;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.provider.Telephony;
import android.telephony.TelephonyFrameworkInitializer;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.samsung.android.ims.cmc.SemCmcRecordingInfo;
import com.sec.ims.DialogEvent;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IEpdgListener;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IImsService;
import com.sec.ims.IRttEventListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsEventListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.cmc.CmcCallInfo;
import com.sec.ims.configuration.DATA;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.gba.IGbaEventListener;
import com.sec.ims.im.IImSessionListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.GoogleImsService;
import com.sec.internal.google.cmc.CmcConnectivityController;
import com.sec.internal.google.cmc.ICmcConnectivityController;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.ImsFramework;
import com.sec.internal.ims.aec.AECModule;
import com.sec.internal.ims.config.ConfigModule;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.GeolocationController;
import com.sec.internal.ims.core.NtpTimeController;
import com.sec.internal.ims.core.PdnController;
import com.sec.internal.ims.core.RegistrationManagerBase;
import com.sec.internal.ims.core.WfcEpdgManager;
import com.sec.internal.ims.core.cmc.CmcAccountManager;
import com.sec.internal.ims.core.handler.HandlerFactory;
import com.sec.internal.ims.core.iil.IilManager;
import com.sec.internal.ims.core.imslogger.ImsDiagnosticMonitorNotificationManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.entitlement.fcm.FcmHandler;
import com.sec.internal.ims.entitlement.fcm.interfaces.IFcmHandler;
import com.sec.internal.ims.gba.GbaServiceModule;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.servicemodules.ServiceModuleManager;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.ValidationHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.DmConfigModule;
import com.sec.internal.ims.settings.GlobalSettingsManager;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.google.IGoogleImsService;
import com.sec.internal.interfaces.google.IImsNotifier;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.INtpTimeController;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.core.iil.IIilManager;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.CriticalLogger;
import com.sec.internal.log.IMSLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ImsServiceStub extends IImsService.Stub implements IImsFramework {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsServiceStub.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    private static final String TC_POPUP_USER_ACCEPT = "info/tc_popup_user_accept";
    private static boolean mIsExplicitGcCalled = false;
    /* access modifiers changed from: private */
    public static boolean mIsImsAvailable = false;
    /* access modifiers changed from: private */
    public static boolean mUserUnlocked = false;
    private static ImsServiceStub sInstance = null;
    private IAECModule mAECModule = null;
    private CallStateTracker mCallStateTracker = null;
    private CmcAccountManager mCmcAccountManager = null;
    /* access modifiers changed from: private */
    public ConfigModule mConfigModule = null;
    private CmcConnectivityController mConnectivityController = null;
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mCoreHandler;
    private final HandlerThread mCoreThread;
    private BroadcastReceiver mDefaultSmsPackageChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL".equals(intent.getAction())) {
                String currentPackage = null;
                try {
                    currentPackage = Telephony.Sms.getDefaultSmsPackage(ImsServiceStub.this.mContext);
                } catch (Exception e) {
                    String access$100 = ImsServiceStub.LOG_TAG;
                    Log.e(access$100, "Failed to get currentPackage: " + e);
                }
                String access$1002 = ImsServiceStub.LOG_TAG;
                Log.d(access$1002, "onChange: MessageApplication is changed : " + currentPackage);
                if (currentPackage != null) {
                    IImModule imModule = ImsServiceStub.this.mServiceModuleManager.getImModule();
                    if (imModule != null) {
                        imModule.handleEventDefaultAppChanged();
                    }
                    if (ImsServiceStub.this.mConfigModule != null) {
                        ImsServiceStub.this.mConfigModule.onDefaultSmsPackageChanged();
                    }
                    ISmsServiceModule sms = ImsServiceStub.this.mServiceModuleManager.getSmsServiceModule();
                    if (sms != null) {
                        sms.handleEventDefaultAppChanged();
                    }
                }
            }
        }
    };
    private DmConfigModule mDmConfigModule = null;
    private SimpleEventLog mEventLog;
    private FcmHandler mFcmHandler = null;
    private GbaServiceModule mGbaServiceModule = null;
    private GeolocationController mGeolocationController = null;
    private GoogleImsService mGoogleImsAdaptor = null;
    private HandlerFactory mHandlerFactory = null;
    private List<IIilManager> mIilManagers = new ArrayList();
    private ImsDiagnosticMonitorNotificationManager mImsDiagMonitor = null;
    private IImsFramework mImsFramework;
    private NtpTimeController mNtpTimeController = null;
    private PdnController mPdnController = null;
    private RcsPolicyManager mRcsPolicyManager = null;
    /* access modifiers changed from: private */
    public RegistrationManagerBase mRegistrationManager = null;
    private List<ISequentialInitializable> mSequentialInitializer = new ArrayList();
    private ServiceExtensionManager mServiceExtensionManager = null;
    /* access modifiers changed from: private */
    public ServiceModuleManager mServiceModuleManager = null;
    private ISimManager mSimManager = null;
    private List<ISimManager> mSimManagers = new ArrayList();
    private BroadcastReceiver mUserUnlockReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction())) {
                Log.i(ImsServiceStub.LOG_TAG, "ACTION_USER_UNLOCKED received");
                boolean unused = ImsServiceStub.mUserUnlocked = true;
                if (ImsServiceStub.this.mRegistrationManager != null && ImsServiceStub.mIsImsAvailable) {
                    ImsServiceStub.explicitGC();
                    ImsServiceStub.this.mRegistrationManager.bootCompleted();
                }
                if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 28) {
                    ImsSharedPrefHelper.migrateToCeStorage(context);
                }
                IntentUtil.sendBroadcast(context, new Intent(NSDSNamespaces.NSDSActions.DEVICE_READY_AFTER_BOOTUP));
            }
        }
    };
    /* access modifiers changed from: private */
    public WfcEpdgManager mWfcEpdgManager = null;

    protected ImsServiceStub(Context context) {
        this.mContext = context;
        this.mCoreThread = new HandlerThread(getClass().getSimpleName());
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 300);
        this.mImsFramework = new ImsFramework(this);
        checkUt(context);
    }

    public static ImsServiceStub getInstance() {
        while (getInstanceInternal() == null) {
            Log.e(LOG_TAG, "instance is null...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return getInstanceInternal();
    }

    private static synchronized ImsServiceStub getInstanceInternal() {
        ImsServiceStub imsServiceStub;
        synchronized (ImsServiceStub.class) {
            imsServiceStub = sInstance;
        }
        return imsServiceStub;
    }

    public static synchronized ImsServiceStub makeImsService(Context context) {
        synchronized (ImsServiceStub.class) {
            if (sInstance != null) {
                Log.d(LOG_TAG, "Already created.");
                ImsServiceStub imsServiceStub = sInstance;
                return imsServiceStub;
            }
            Log.i(LOG_TAG, "Creating IMSService");
            ImsServiceStub imsServiceStub2 = new ImsServiceStub(context);
            sInstance = imsServiceStub2;
            imsServiceStub2.createModules();
            sInstance.init();
            Log.i(LOG_TAG, "Done.");
            IMSLog.c(LogClass.GEN_IMS_SERVICE_CREATED, "PID:" + Process.myPid());
            ImsServiceStub imsServiceStub3 = sInstance;
            return imsServiceStub3;
        }
    }

    /* access modifiers changed from: private */
    public static void explicitGC() {
        if (mIsExplicitGcCalled) {
            return;
        }
        if (!Debug.isProductShip() || mUserUnlocked) {
            new Thread($$Lambda$ImsServiceStub$f6l1v8cA9Lv44kEdHydsjHjRfug.INSTANCE).start();
            mIsExplicitGcCalled = true;
        }
    }

    static /* synthetic */ void lambda$explicitGC$0() {
        Log.i(LOG_TAG, "Call explicit GC");
        System.gc();
        System.runFinalization();
    }

    public static boolean isImsAvailable() {
        return mIsImsAvailable;
    }

    public void registerDefaultSmsPackageChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL");
        this.mContext.registerReceiver(this.mDefaultSmsPackageChangeReceiver, filter);
    }

    public void registerUserUnlockReceiver() {
        this.mContext.registerReceiver(this.mUserUnlockReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
    }

    private void registerFactoryResetReceiver() {
        IntentFilter intent = new IntentFilter();
        intent.addAction("com.samsung.intent.action.SETTINGS_SOFT_RESET");
        intent.addAction(ImsConstants.Intents.ACTION_RESET_NETWORK_SETTINGS);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* JADX WARNING: Removed duplicated region for block: B:12:0x0049 A[ADDED_TO_REGION] */
            /* JADX WARNING: Removed duplicated region for block: B:16:0x0063  */
            /* JADX WARNING: Removed duplicated region for block: B:22:0x007d  */
            /* JADX WARNING: Removed duplicated region for block: B:25:? A[RETURN, SYNTHETIC] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r6, android.content.Intent r7) {
                /*
                    r5 = this;
                    java.lang.String r0 = com.sec.internal.ims.imsservice.ImsServiceStub.LOG_TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "received intent : "
                    r1.append(r2)
                    java.lang.String r2 = r7.getAction()
                    r1.append(r2)
                    java.lang.String r1 = r1.toString()
                    android.util.Log.d(r0, r1)
                    java.lang.String r0 = r7.getAction()
                    int r1 = r0.hashCode()
                    r2 = -1115331537(0xffffffffbd85642f, float:-0.06513249)
                    r3 = -1
                    r4 = 1
                    if (r1 == r2) goto L_0x003c
                    r2 = -535938999(0xffffffffe00e3849, float:-4.0992085E19)
                    if (r1 == r2) goto L_0x0032
                L_0x0031:
                    goto L_0x0046
                L_0x0032:
                    java.lang.String r1 = "com.samsung.intent.action.SETTINGS_NETWORK_RESET"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0031
                    r0 = r4
                    goto L_0x0047
                L_0x003c:
                    java.lang.String r1 = "com.samsung.intent.action.SETTINGS_SOFT_RESET"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0031
                    r0 = 0
                    goto L_0x0047
                L_0x0046:
                    r0 = r3
                L_0x0047:
                    if (r0 == 0) goto L_0x0063
                    if (r0 == r4) goto L_0x004c
                    goto L_0x0075
                L_0x004c:
                    java.lang.String r0 = "subId"
                    boolean r1 = r7.hasExtra(r0)
                    if (r1 == 0) goto L_0x0075
                    int r0 = r7.getIntExtra(r0, r3)
                    int r0 = com.sec.internal.ims.core.sim.SimManagerFactory.getSlotId(r0)
                    com.sec.internal.ims.imsservice.ImsServiceStub r1 = com.sec.internal.ims.imsservice.ImsServiceStub.this
                    r1.factoryReset(r0)
                    goto L_0x0075
                L_0x0063:
                    r0 = 0
                L_0x0064:
                    com.sec.internal.ims.imsservice.ImsServiceStub r1 = com.sec.internal.ims.imsservice.ImsServiceStub.this
                    int r1 = r1.getPhoneCount()
                    if (r0 >= r1) goto L_0x0074
                    com.sec.internal.ims.imsservice.ImsServiceStub r1 = com.sec.internal.ims.imsservice.ImsServiceStub.this
                    r1.factoryReset(r0)
                    int r0 = r0 + 1
                    goto L_0x0064
                L_0x0074:
                L_0x0075:
                    com.sec.internal.ims.imsservice.ImsServiceStub r0 = com.sec.internal.ims.imsservice.ImsServiceStub.this
                    com.sec.internal.ims.core.WfcEpdgManager r0 = r0.mWfcEpdgManager
                    if (r0 == 0) goto L_0x0086
                    com.sec.internal.ims.imsservice.ImsServiceStub r0 = com.sec.internal.ims.imsservice.ImsServiceStub.this
                    com.sec.internal.ims.core.WfcEpdgManager r0 = r0.mWfcEpdgManager
                    r0.onResetSetting(r7)
                L_0x0086:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.imsservice.ImsServiceStub.AnonymousClass2.onReceive(android.content.Context, android.content.Intent):void");
            }
        }, intent);
    }

    private void registerPackageManagerReceiver() {
        Log.d(LOG_TAG, "registerPackageMgrListener");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addDataScheme("package");
        String smkVersion = getSmkVersion();
        if (smkVersion != null) {
            writeSmkVerData(smkVersion);
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String packageName = "";
                if (intent.getData() != null) {
                    packageName = intent.getData().toString().replace("package:", "");
                }
                String packageStatus = intent.getAction();
                Log.d(ImsServiceStub.LOG_TAG, "packageStatus : " + packageStatus + ", packageName : " + packageName);
                boolean z = false;
                if (TextUtils.equals(ImsConstants.Packages.PACKAGE_SIMMOBILITY_KIT, packageName)) {
                    if (packageStatus.hashCode() != -810471698 || !packageStatus.equals("android.intent.action.PACKAGE_REPLACED")) {
                        z = true;
                    }
                    if (!z) {
                        String ver = ImsServiceStub.this.getSmkVersion();
                        if (!ImsServiceStub.this.isPreloadedSmk(ver)) {
                            ImsServiceStub.this.startDeviceConfigService();
                        }
                        ImsServiceStub.this.writeSmkVerData(ver);
                    }
                } else if (TextUtils.equals(ImsConstants.Packages.PACKAGE_SEC_MSG, packageName)) {
                    if (packageStatus.hashCode() != -810471698 || !packageStatus.equals("android.intent.action.PACKAGE_REPLACED")) {
                        z = true;
                    }
                    if (!z) {
                        ICapabilityDiscoveryModule cdm = ImsServiceStub.this.mServiceModuleManager.getCapabilityDiscoveryModule();
                        if (cdm == null || !cdm.isRunning()) {
                            Log.d(ImsServiceStub.LOG_TAG, "registerPackageManagerReceiver:CapaModule not available");
                            return;
                        }
                        Log.d(ImsServiceStub.LOG_TAG, "registerPackageManagerReceiver: notify to CapaModule");
                        cdm.onPackageUpdated(packageName);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    public void writeSmkVerData(String version) {
        ContentValues dailyReport = new ContentValues();
        dailyReport.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        dailyReport.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 0);
        dailyReport.put(DiagnosisConstants.DRPT_KEY_SMK_VERSION, version);
        ImsLogAgentUtil.storeLogToAgent(SimUtil.getDefaultPhoneId(), this.mContext, "DRPT", dailyReport);
    }

    /* access modifiers changed from: private */
    public String getSmkVersion() {
        String version = null;
        try {
            version = this.mContext.getPackageManager().getPackageInfo(ImsConstants.Packages.PACKAGE_SIMMOBILITY_KIT, 0).versionName;
            String str = LOG_TAG;
            Log.d(str, "Get SMK version Success : " + version);
            return version;
        } catch (Exception e) {
            Log.e(LOG_TAG, "fail to get versionName");
            return version;
        }
    }

    /* access modifiers changed from: private */
    public boolean isPreloadedSmk(String version) {
        return version == null || version.equals(ImsConstants.Packages.SMK_PRELOADED_VERSION);
    }

    /* access modifiers changed from: private */
    public void startDeviceConfigService() {
        this.mEventLog.logAndAdd("call SMK start");
        Intent intent = new Intent();
        intent.setClassName(ImsConstants.Packages.PACKAGE_SIMMOBILITY_KIT, ImsConstants.Packages.CLASS_SIMMOBILITY_KIT_UPDATE);
        this.mContext.startForegroundService(intent);
    }

    public void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (action == FtIntent.Actions.RequestIntents.GRANT_FILE_PERMISSION || action == FtIntent.Actions.RequestIntents.MOVE_FILE_COMPLETE || action == FtIntent.Actions.RequestIntents.MOVE_FILE_FINAL_COMPLETE) {
                RcsFileProviderManager.handleIntent(this.mContext, intent);
            } else {
                this.mServiceModuleManager.handleIntent(intent);
            }
        }
    }

    private void createModules() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.i(LOG_TAG, "createModules started");
        this.mCoreThread.start();
        Looper serviceLooper = this.mCoreThread.getLooper();
        this.mCoreHandler = new Handler(serviceLooper);
        Log.i(LOG_TAG, "Creating SimManagers.");
        SimManagerFactory.createInstance(serviceLooper, this.mContext);
        this.mSimManager = SimManagerFactory.getSimManager();
        this.mSimManagers.clear();
        this.mSimManagers.addAll(SimManagerFactory.getAllSimManagers());
        this.mSequentialInitializer.addAll(this.mSimManagers);
        int phoneCount = TelephonyManagerWrapper.getInstance(this.mContext).getPhoneCount();
        for (int phoneId = 0; phoneId < phoneCount; phoneId++) {
            this.mIilManagers.add(phoneId, new IilManager(this.mContext, phoneId, this));
        }
        Log.i(LOG_TAG, "Creating WfcEpdgManager.");
        WfcEpdgManager wfcEpdgManager = new WfcEpdgManager(serviceLooper);
        this.mWfcEpdgManager = wfcEpdgManager;
        this.mSequentialInitializer.add(wfcEpdgManager);
        Log.i(LOG_TAG, "Creating PdnController.");
        PdnController pdnController = new PdnController(this.mContext, serviceLooper, this);
        this.mPdnController = pdnController;
        this.mSequentialInitializer.add(pdnController);
        Log.i(LOG_TAG, "Creating DmConfigModule.");
        DmConfigModule dmConfigModule = new DmConfigModule(this.mContext, serviceLooper, this);
        this.mDmConfigModule = dmConfigModule;
        this.mSequentialInitializer.add(dmConfigModule);
        this.mCmcAccountManager = new CmcAccountManager(this.mContext, serviceLooper);
        Log.i(LOG_TAG, "Creating RcsPolicyManager.");
        RcsPolicyManager rcsPolicyManager = new RcsPolicyManager(serviceLooper, this.mContext, this.mSimManagers);
        this.mRcsPolicyManager = rcsPolicyManager;
        this.mSequentialInitializer.add(rcsPolicyManager);
        Log.i(LOG_TAG, "Creating RegistrationManager.");
        Context context = this.mContext;
        RegistrationManagerBase registrationManagerBase = new RegistrationManagerBase(serviceLooper, this, context, this.mPdnController, this.mSimManagers, TelephonyManagerWrapper.getInstance(context), this.mCmcAccountManager, this.mRcsPolicyManager);
        this.mRegistrationManager = registrationManagerBase;
        this.mSequentialInitializer.add(registrationManagerBase);
        Log.i(LOG_TAG, "Creating ConfigModule.");
        ConfigModule configModule = new ConfigModule(serviceLooper, this.mContext, this.mRegistrationManager);
        this.mConfigModule = configModule;
        this.mSequentialInitializer.add(configModule);
        Log.i(LOG_TAG, "Creating GbaServiceModule.");
        this.mGbaServiceModule = new GbaServiceModule(serviceLooper, this.mContext, this);
        Log.i(LOG_TAG, "Creating HandlerFactory.");
        HandlerFactory createStackHandler = HandlerFactory.createStackHandler(serviceLooper, this.mContext, this);
        this.mHandlerFactory = createStackHandler;
        this.mSequentialInitializer.add(createStackHandler);
        Log.i(LOG_TAG, "Creating ServiceModuleManager.");
        ServiceModuleManager serviceModuleManager = new ServiceModuleManager(serviceLooper, this.mContext, this, this.mSimManagers, this.mRegistrationManager, this.mHandlerFactory);
        this.mServiceModuleManager = serviceModuleManager;
        this.mSequentialInitializer.add(serviceModuleManager);
        Log.i(LOG_TAG, "Creating AECModule.");
        AECModule aECModule = new AECModule(serviceLooper, this.mContext);
        this.mAECModule = aECModule;
        this.mSequentialInitializer.add(aECModule);
        Log.i(LOG_TAG, "Creating GeolocationController.");
        GeolocationController geolocationController = new GeolocationController(this.mContext, serviceLooper, this.mRegistrationManager);
        this.mGeolocationController = geolocationController;
        this.mSequentialInitializer.add(geolocationController);
        CallStateTracker callStateTracker = new CallStateTracker(this.mContext, this.mCoreHandler, this.mServiceModuleManager);
        this.mCallStateTracker = callStateTracker;
        this.mSequentialInitializer.add(callStateTracker);
        Log.i(LOG_TAG, "Creating ImsDiagnosticMonitorNotificationManager.");
        ImsDiagnosticMonitorNotificationManager imsDiagnosticMonitorNotificationManager = new ImsDiagnosticMonitorNotificationManager(this.mContext, serviceLooper);
        this.mImsDiagMonitor = imsDiagnosticMonitorNotificationManager;
        this.mSequentialInitializer.add(imsDiagnosticMonitorNotificationManager);
        Log.i(LOG_TAG, "Creating NtpTimeController.");
        NtpTimeController ntpTimeController = new NtpTimeController(this.mContext, serviceLooper);
        this.mNtpTimeController = ntpTimeController;
        this.mSequentialInitializer.add(ntpTimeController);
        this.mRegistrationManager.setConfigModule(this.mConfigModule);
        this.mRegistrationManager.setGeolocationController(this.mGeolocationController);
        this.mRegistrationManager.setStackInterface(this.mHandlerFactory.getRegistrationStackAdaptor());
        this.mCmcAccountManager.setRegistrationManager(this.mRegistrationManager);
        this.mRcsPolicyManager.setRegistrationManager(this.mRegistrationManager);
        this.mDmConfigModule.setRegistrationManager(this.mRegistrationManager);
        this.mConnectivityController = new CmcConnectivityController(serviceLooper, getRegistrationManager());
    }

    private void init() {
        Log.i(LOG_TAG, "init started");
        this.mSequentialInitializer.forEach($$Lambda$Coz7SzymPQHD4TahSkxD2V2ic9w.INSTANCE);
        this.mSequentialInitializer.clear();
        this.mRegistrationManager.setVolteServiceModule(this.mServiceModuleManager.getVolteServiceModule());
        SimManagerFactory.initInstances();
        GoogleImsService instance = GoogleImsService.getInstance(this.mContext, this.mServiceModuleManager);
        this.mGoogleImsAdaptor = instance;
        instance.setConnectivityController(this.mConnectivityController);
        registerFactoryResetReceiver();
        if (ValidationHelper.isTapiAuthorisationSupports()) {
            ServiceExtensionManager instance2 = ServiceExtensionManager.getInstance(this.mContext);
            this.mServiceExtensionManager = instance2;
            instance2.start();
        }
        try {
            if (Build.VERSION.SEM_INT >= 2716) {
                SemImsServiceStub.makeSemImsService(this.mContext);
            }
        } catch (NoSuchFieldError e) {
            Log.e(LOG_TAG, e.toString());
        }
        registerDefaultSmsPackageChangeReceiver();
        registerPackageManagerReceiver();
        registerUserUnlockReceiver();
        linkToPhoneDeath();
        this.mConnectivityController.tryNsdBind();
    }

    public void registerSimMobilityStatusListener(ISimMobilityStatusListener listener, boolean broadcast, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            IMSLog.d(str, phoneId, "registerSimMobilityStatusListener: broadcast = " + broadcast);
            if (phoneId == -1) {
                Log.d(LOG_TAG, "Requested registerSimMobilityStatusListener without phoneId. register it by all phoneId.");
                this.mSimManagers.forEach(new Consumer(listener) {
                    public final /* synthetic */ ISimMobilityStatusListener f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        ImsServiceStub.this.lambda$registerSimMobilityStatusListener$1$ImsServiceStub(this.f$1, (ISimManager) obj);
                    }
                });
                return;
            }
            this.mRegistrationManager.registerSimMobilityStatusListener(listener, broadcast, phoneId);
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public /* synthetic */ void lambda$registerSimMobilityStatusListener$1$ImsServiceStub(ISimMobilityStatusListener listener, ISimManager sm) {
        this.mRegistrationManager.registerSimMobilityStatusListener(listener, sm.getSimSlotIndex());
    }

    public boolean isSimMobilityActivated(int phoneId) {
        return ImsUtil.isSimMobilityActivated(phoneId);
    }

    private boolean hasVoImsFeature(String service, int rat, int phoneId) {
        String str = service;
        int i = phoneId;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null) {
            IMSLog.d(LOG_TAG, i, "hasVolteFeature - no simMgr");
            return true;
        }
        boolean needToCheckVoLTE = false;
        boolean needToCheckVoWIFI = false;
        boolean needToCheckSmsIP = false;
        boolean needToCheckViLTE = false;
        if ("mmtel".equalsIgnoreCase(str)) {
            needToCheckVoLTE = true;
        } else if ("smsip".equalsIgnoreCase(str)) {
            needToCheckSmsIP = true;
        } else if ("mmtel-video".equalsIgnoreCase(str)) {
            needToCheckViLTE = true;
        } else {
            IMSLog.d(LOG_TAG, i, "no VoLTE feature, no need to check mnoInfo");
            return true;
        }
        if (rat == 18) {
            needToCheckVoWIFI = true;
            needToCheckVoLTE = false;
        }
        ContentValues mnoInfo = sm.getMnoInfo();
        Integer imsSwitchType = mnoInfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
        if (imsSwitchType == null) {
            IMSLog.d(LOG_TAG, i, "hasVoImsFeature - mnoInfo not available");
            return false;
        } else if (imsSwitchType.intValue() == 4 || imsSwitchType.intValue() == 5) {
            ISimManager iSimManager = sm;
            boolean isEnableIms = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false);
            if (!isEnableIms) {
                boolean z = isEnableIms;
                String str2 = LOG_TAG;
                IMSLog.d(str2, i, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS + " false");
                return false;
            }
            boolean isEnableIms2 = false;
            if (needToCheckVoWIFI) {
                if (!CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false)) {
                    String str3 = LOG_TAG;
                    IMSLog.d(str3, i, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI + " false");
                    return false;
                }
                isEnableIms2 = false;
            }
            if (needToCheckVoLTE && !CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, isEnableIms2)) {
                String str4 = LOG_TAG;
                IMSLog.d(str4, i, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE + " false");
                return false;
            } else if (needToCheckSmsIP && !CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, false)) {
                String str5 = LOG_TAG;
                IMSLog.d(str5, i, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP + " false");
                return false;
            } else if (!needToCheckViLTE || CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, false)) {
                return true;
            } else {
                String str6 = LOG_TAG;
                IMSLog.d(str6, i, "hasVoImsFeature - " + ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL + " false");
                return false;
            }
        } else {
            IMSLog.d(LOG_TAG, i, "hasVoImsFeature - No SIM or GCF or LABSIM or TestBed SIM or Softphone or Simmobility or Default ImsSwitch");
            return true;
        }
    }

    private void changeOpModeByRcsSwtich(boolean isRcsEnabled, boolean enable, int phoneId) {
        if (isRcsEnabled != enable) {
            String val = RcsConfigurationHelper.readStringParamWithPath(this.mContext, ImsUtil.getPathWithPhoneId("info/tc_popup_user_accept", phoneId));
            int tcPopupUserAccept = -1;
            if (val != null) {
                try {
                    tcPopupUserAccept = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    IMSLog.e(LOG_TAG, phoneId, "Error while parsing integer in getIntValue() - NumberFormatException");
                }
            }
            this.mConfigModule.changeOpMode(enable, phoneId, tcPopupUserAccept);
        }
    }

    private void enableRcsMainSwitchByPhoneId(boolean enable, int phoneId) {
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, phoneId) != 1) {
            z = false;
        }
        boolean isRcsEnabled = z;
        String str = LOG_TAG;
        Log.d(str, "enableRcsMainSwitchByPhoneId: oldValue: " + isRcsEnabled + ", newValue: " + enable);
        changeOpModeByRcsSwtich(isRcsEnabled, enable, phoneId);
        if (SimUtil.getSimMno(phoneId) != Mno.SKT || enable) {
            DmConfigHelper.setImsSwitch(this.mContext, DeviceConfigManager.RCS, enable, phoneId);
        } else {
            Log.d(LOG_TAG, "enableRcs: Ignore RCS disable for SKT until server responds");
        }
    }

    private void dump(PrintWriter pw) {
        CriticalLogger.getInstance().flush();
        Context context = this.mContext;
        context.enforceCallingOrSelfPermission("android.permission.DUMP", "Permission Denial: can't dump ims from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        IMSLog.prepareDump(pw);
        SimpleEventLog simpleEventLog = this.mEventLog;
        StringBuilder sb = new StringBuilder();
        sb.append("SimMobility Feature ");
        sb.append(SimUtil.isSimMobilityFeatureEnabled() ? "Enabled" : "Disabled");
        simpleEventLog.add(sb.toString());
        this.mEventLog.dump();
        this.mServiceModuleManager.dump();
        SimManagerFactory.dump();
        RegistrationManagerBase registrationManagerBase = this.mRegistrationManager;
        if (registrationManagerBase != null) {
            registrationManagerBase.dump();
        }
        PdnController pdnController = this.mPdnController;
        if (pdnController != null) {
            pdnController.dump();
        }
        ConfigModule configModule = this.mConfigModule;
        if (configModule != null) {
            configModule.dump();
        }
        DmConfigModule dmConfigModule = this.mDmConfigModule;
        if (dmConfigModule != null) {
            dmConfigModule.dump();
        }
        IAECModule iAECModule = this.mAECModule;
        if (iAECModule != null) {
            iAECModule.dump();
        }
        CmcAccountManager cmcAccountManager = this.mCmcAccountManager;
        if (cmcAccountManager != null) {
            cmcAccountManager.dump();
        }
        PreciseAlarmManager.getInstance(this.mContext).dump();
        this.mContext.getContentResolver().call(Uri.parse(ImsConstants.Uris.CONFIG_URI), "dump", (String) null, (Bundle) null);
        IMSLog.postDump(pw);
    }

    private boolean isPermissionGranted() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == 0 || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0;
    }

    /* access modifiers changed from: private */
    public void factoryReset(int phoneId) {
        if (phoneId < 0 || phoneId >= getPhoneCount()) {
            IMSLog.e(LOG_TAG, phoneId, "factoryReset : invalid phoneId");
            return;
        }
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null || sm.hasNoSim()) {
            IMSLog.e(LOG_TAG, phoneId, "factoryReset : skip reset due to no SIM");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "factoryReset");
        boolean volteEnabledDoemstic = getBoolean(phoneId, GlobalSettingsConstants.Registration.VOLTE_DOMESTIC_DEFAULT_ENABLED, true);
        boolean vilteEnabled = getBoolean(phoneId, GlobalSettingsConstants.Registration.VIDEO_DEFAULT_ENABLED, true);
        String str = LOG_TAG;
        Log.d(str, "reset to default] Volte : " + volteEnabledDoemstic + ", Video : " + vilteEnabled);
        ImsConstants.SystemSettings.setVoiceCallType(this.mContext, volteEnabledDoemstic ^ true ? 1 : 0, phoneId);
        ImsConstants.SystemSettings.setVideoCallType(this.mContext, vilteEnabled ^ true ? 1 : 0, phoneId);
    }

    public WfcEpdgManager getWfcEpdgManager() {
        return this.mWfcEpdgManager;
    }

    public void registerCallback(ImsEventListener callback, String caller) {
    }

    public void unregisterCallback(ImsEventListener callback) {
    }

    public int getPhoneCount() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return SimUtil.getPhoneCount();
    }

    public void setIsimLoaded() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mSimManager.setIsimLoaded();
    }

    public void setSimRefreshed() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mSimManager.setSimRefreshed();
    }

    public int setActiveImpu(int phoneId, String impu, String service) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "setActiveImpu: impu " + impu + " service " + service + " to phoneId" + phoneId);
        this.mServiceModuleManager.getVolteServiceModule().setActiveImpu(phoneId, impu);
        return 0;
    }

    public int setActiveMsisdn(int phoneId, String msisdn, String service) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        IMSLog.d(str, "setActiveMsisdn: msisdn " + IMSLog.checker(msisdn) + " service " + service);
        if (TextUtils.isEmpty(service)) {
            return -1;
        }
        if (TextUtils.isEmpty(msisdn)) {
            Log.d(LOG_TAG, "setActiveMsisdn: unset activeMsisdn.");
            return setActiveImpu(phoneId, (String) null, service);
        }
        ImsUri normalizedUri = this.mServiceModuleManager.getVolteServiceModule().getNormalizedUri(phoneId, msisdn);
        if (normalizedUri != null) {
            return setActiveImpu(phoneId, normalizedUri.toString(), service);
        }
        Log.e(LOG_TAG, "setActiveMsisdn: not found!");
        return -2;
    }

    public void sendVerificationCode(String value, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.sendVerificationCode(value, phoneId);
    }

    public void sendMsisdnNumber(String value, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.sendMsisdnNumber(value, phoneId);
    }

    public int getNetworkType(int handle) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        int network = this.mRegistrationManager.getCurrentNetwork(handle);
        if (network >= 1 && network <= 17) {
            return 1;
        }
        if (network == 18) {
            return 2;
        }
        return 0;
    }

    public String getAvailableNetworkType(String service) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.getAvailableNetworkType(service);
    }

    public void registerImSessionListener(IImSessionListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "registerImSessionListener:");
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.registerImSessionListener(listener);
        }
    }

    public void registerImSessionListenerByPhoneId(IImSessionListener listener, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "registerImSessionListenerByPhoneId: PhoneId " + phoneId);
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.registerImSessionListenerByPhoneId(listener, phoneId);
        }
    }

    public void unregisterImSessionListener(IImSessionListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "unregisterImSessionListener:");
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.unregisterImSessionListener(listener);
        }
    }

    public void unregisterImSessionListenerByPhoneId(IImSessionListener listener, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "unregisterImSessionListenerByPhoneId: PhoneId " + phoneId);
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.unregisterImSessionListenerByPhoneId(listener, phoneId);
        }
    }

    public void registerImsOngoingFtListener(IImsOngoingFtEventListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "registerImsOngoingFtListener");
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.registerImsOngoingFtListener(listener);
        }
    }

    public void registerImsOngoingFtListenerByPhoneId(IImsOngoingFtEventListener listener, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "registerImsOngoingFtListenerByPhoneId: PhoneId " + phoneId);
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.registerImsOngoingFtListenerByPhoneId(listener, phoneId);
        }
    }

    public void unregisterImsOngoingFtListener(IImsOngoingFtEventListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "unregisterImsOngoingFtListener");
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.unregisterImsOngoingListener(listener);
        }
    }

    public void unregisterImsOngoingFtListenerByPhoneId(IImsOngoingFtEventListener listener, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "unregisterImsOngoingFtListenerByPhoneId: PhoneId " + phoneId);
        IImModule module = this.mServiceModuleManager.getImModule();
        if (module != null) {
            module.unregisterImsOngoingListenerByPhoneId(listener, phoneId);
        }
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener listener, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "registerAutoConfigurationListener: PhoneId " + phoneId);
        this.mConfigModule.registerAutoConfigurationListener(listener, phoneId);
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "unregisterAutoConfigurationListener: PhoneId " + phoneId);
        this.mConfigModule.unregisterAutoConfigurationListener(listener, phoneId);
    }

    public void registerSimMobilityStatusListenerByPhoneId(ISimMobilityStatusListener listener, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            Log.d(str, "registerSimMobilityStatusListenerByPhoneId: phoneId " + phoneId);
            registerSimMobilityStatusListener(listener, true, phoneId);
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public void unregisterSimMobilityStatusListenerByPhoneId(ISimMobilityStatusListener listener, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            Log.d(str, "unregisterSimMobilityStatusListenerByPhoneId: phoneId " + phoneId);
            if (phoneId == -1) {
                this.mSimManagers.forEach(new Consumer(listener) {
                    public final /* synthetic */ ISimMobilityStatusListener f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        ImsServiceStub.this.lambda$unregisterSimMobilityStatusListenerByPhoneId$2$ImsServiceStub(this.f$1, (ISimManager) obj);
                    }
                });
            } else {
                this.mRegistrationManager.unregisterSimMobilityStatusListener(listener, phoneId);
            }
        } else {
            throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
        }
    }

    public /* synthetic */ void lambda$unregisterSimMobilityStatusListenerByPhoneId$2$ImsServiceStub(ISimMobilityStatusListener listener, ISimManager sm) {
        this.mRegistrationManager.unregisterSimMobilityStatusListener(listener, sm.getSimSlotIndex());
    }

    public boolean isRegistered() throws RemoteException {
        RegistrationManagerBase registrationManagerBase = this.mRegistrationManager;
        return (registrationManagerBase == null || registrationManagerBase.getRegistrationInfo() == null || this.mRegistrationManager.getRegistrationInfo().length <= 0) ? false : true;
    }

    public ImsRegistration[] getRegistrationInfo() throws RemoteException {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.getRegistrationInfo();
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int phoneId) {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.getRegistrationInfoByPhoneId(phoneId);
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public ImsRegistration getRegistrationInfoByServiceType(String serviceType, int phoneId) {
        if (isPermissionGranted()) {
            return this.mRegistrationManager.getRegistrationInfoByServiceType(serviceType, phoneId);
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public ImsProfile[] getCurrentProfile() {
        return getCurrentProfileForSlot(SimUtil.getDefaultPhoneId());
    }

    public ImsProfile[] getCurrentProfileForSlot(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.getProfileList(phoneId);
    }

    public String getRcsProfileType(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String rcsProfile = (String) Arrays.stream(this.mRegistrationManager.getProfileList(phoneId)).filter($$Lambda$2re5zpqdOiYPaeoxPsP2TiymwJw.INSTANCE).map(new Function(phoneId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final Object apply(Object obj) {
                return ImsServiceStub.this.lambda$getRcsProfileType$3$ImsServiceStub(this.f$1, (ImsProfile) obj);
            }
        }).filter($$Lambda$ImsServiceStub$a7J71XDGMUKionXrQfHVjWH4Yug.INSTANCE).findFirst().orElse("");
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "getRcsProfileType: rcsProfile = " + rcsProfile);
        return rcsProfile;
    }

    static /* synthetic */ boolean lambda$getRcsProfileType$4(String s) {
        return !TextUtils.isEmpty(s);
    }

    public /* synthetic */ String lambda$getRcsProfileType$3$ImsServiceStub(int phoneId, ImsProfile p) {
        return ConfigUtil.getRcsProfileWithFeature(this.mContext, phoneId, p);
    }

    public int registerAdhocProfile(ImsProfile profile) {
        return registerAdhocProfileByPhoneId(profile, SimUtil.getDefaultPhoneId());
    }

    public int registerAdhocProfileByPhoneId(ImsProfile profile, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.registerProfile(profile, phoneId);
    }

    public void deregisterAdhocProfile(int id) throws RemoteException {
        deregisterAdhocProfileByPhoneId(id, SimUtil.getDefaultPhoneId());
    }

    public void deregisterAdhocProfileByPhoneId(int id, int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.deregisterProfile(id, phoneId);
    }

    public void registerProfile(List profileIds) {
        registerProfileByPhoneId(profileIds, SimUtil.getDefaultPhoneId());
    }

    public void registerProfileByPhoneId(List profileIds, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.registerProfile((List<Integer>) profileIds, phoneId);
    }

    public void deregisterProfile(List profileIds, boolean disconnectPdn) {
        deregisterProfileByPhoneId(profileIds, disconnectPdn, SimUtil.getDefaultPhoneId());
    }

    public void deregisterProfileByPhoneId(List profileIds, boolean disconnectPdn, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.deregisterProfile((List<Integer>) profileIds, disconnectPdn, phoneId);
    }

    public void sendTryRegister() {
        sendTryRegisterByPhoneId(SimUtil.getDefaultPhoneId());
    }

    public void sendTryRegisterByPhoneId(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mConfigModule.setRegisterFromApp(true, phoneId);
    }

    public void forcedUpdateRegistration(ImsProfile imsProfile) {
        forcedUpdateRegistrationByPhoneId(imsProfile, SimUtil.getDefaultPhoneId());
    }

    public void forcedUpdateRegistrationByPhoneId(ImsProfile imsProfile, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.forcedUpdateRegistration(imsProfile, phoneId);
    }

    public void sendDeregister(int cause, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.sendDeregister(cause, phoneId);
    }

    public void suspendRegister(boolean suspend, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mRegistrationManager.suspendRegister(suspend, phoneId);
    }

    public int updateRegistration(ImsProfile profile, int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mRegistrationManager.updateRegistration(profile, phoneId);
    }

    public void setEmergencyPdnInfo(String intfName, String[] pcscfAddressArray, String gwAddress, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "ePDN setup failure was changed to onPreciseDataConnectionStateChanged");
    }

    public void registerEpdgListener(IEpdgListener listener) {
        Log.d(LOG_TAG, "registerEpdgListener");
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        WfcEpdgManager wfcEpdgManager = this.mWfcEpdgManager;
        if (wfcEpdgManager != null) {
            wfcEpdgManager.registerEpdgHandoverListener(listener);
        }
    }

    public void unRegisterEpdgListener(IEpdgListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        WfcEpdgManager wfcEpdgManager = this.mWfcEpdgManager;
        if (wfcEpdgManager != null) {
            wfcEpdgManager.unRegisterEpdgHandoverListener(listener);
        }
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener) {
        if (isPermissionGranted()) {
            Log.d(LOG_TAG, "registerImsRegistrationListener");
            registerImsRegistrationListener(listener, true, -1);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        if (isPermissionGranted()) {
            Log.d(LOG_TAG, "Requested unregisterListener without phoneId. unregister it by all phoneId.");
            this.mSimManagers.forEach(new Consumer(listener) {
                public final /* synthetic */ IImsRegistrationListener f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    ImsServiceStub.this.lambda$unregisterImsRegistrationListener$5$ImsServiceStub(this.f$1, (ISimManager) obj);
                }
            });
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public /* synthetic */ void lambda$unregisterImsRegistrationListener$5$ImsServiceStub(IImsRegistrationListener listener, ISimManager sm) {
        this.mRegistrationManager.unregisterListener(listener, sm.getSimSlotIndex());
    }

    public void registerImsRegistrationListenerForSlot(IImsRegistrationListener listener, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            Log.d(str, "registerImsRegistrationListenerForSlot: phoneId " + phoneId);
            registerImsRegistrationListener(listener, true, phoneId);
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public void unregisterImsRegistrationListenerForSlot(IImsRegistrationListener listener, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            Log.d(str, "unregisterImsRegistrationListenerForSlot: phoneId " + phoneId);
            if (phoneId == -1) {
                Log.d(LOG_TAG, "Requested unRegisterListener without phoneId. register it by all phoneId.");
                this.mSimManagers.forEach(new Consumer(listener) {
                    public final /* synthetic */ IImsRegistrationListener f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        ImsServiceStub.this.lambda$unregisterImsRegistrationListenerForSlot$6$ImsServiceStub(this.f$1, (ISimManager) obj);
                    }
                });
                return;
            }
            this.mRegistrationManager.unregisterListener(listener, phoneId);
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public /* synthetic */ void lambda$unregisterImsRegistrationListenerForSlot$6$ImsServiceStub(IImsRegistrationListener listener, ISimManager sm) {
        this.mRegistrationManager.unregisterListener(listener, sm.getSimSlotIndex());
    }

    public void registerCmcRegistrationListenerForSlot(IImsRegistrationListener listener, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            Log.d(str, "registerCmcRegistrationListenerForSlot: phoneId " + phoneId);
            this.mRegistrationManager.registerCmcRegiListener(listener, phoneId);
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public void unregisterCmcRegistrationListenerForSlot(IImsRegistrationListener listener, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            Log.d(str, "unregisterCmcRegistrationListenerForSlot: phoneId " + phoneId);
            this.mRegistrationManager.unregisterCmcRegiListener(listener, phoneId);
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public void registerDialogEventListener(int phoneId, IDialogEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "registerDialogEventListener");
        this.mServiceModuleManager.getVolteServiceModule().registerDialogEventListener(phoneId, listener);
    }

    public void unregisterDialogEventListener(int phoneId, IDialogEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "unregisterDialogEventListener");
        this.mServiceModuleManager.getVolteServiceModule().unregisterDialogEventListener(phoneId, listener);
    }

    public DialogEvent getLastDialogEvent(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getVolteServiceModule().getLastDialogEvent(phoneId);
    }

    public int getMasterValue(int item) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return 0;
    }

    public String getMasterStringValue(int item) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return "";
    }

    public void setProvisionedValue(int item, int value) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
    }

    public void setProvisionedStringValue(int item, String value) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
    }

    public boolean isImsEnabled() {
        return isImsEnabledByPhoneId(0);
    }

    public boolean isImsEnabledByPhoneId(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, phoneId) == 1;
    }

    public boolean isVoLteEnabled() {
        return isVoLteEnabledByPhoneId(0);
    }

    public boolean isVoLteEnabledByPhoneId(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return DmConfigHelper.getImsSwitchValue(this.mContext, "volte", phoneId) == 1;
    }

    public boolean isVolteEnabledFromNetwork(int phoneId) {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().isVolteServiceStatus(phoneId);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isVolteSupportECT() {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().isVolteSupportECT();
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isVolteSupportEctByPhoneId(int phoneId) {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().isVolteSupportECT(phoneId);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isRcsEnabled() {
        return isRcsEnabledByPhoneId(SimUtil.getDefaultPhoneId());
    }

    public boolean isServiceEnabled(String service) {
        return isServiceEnabledByPhoneId(service, 0);
    }

    public boolean isServiceAvailable(String service, int rat, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        if (!hasVoImsFeature(service, rat, phoneId)) {
            Log.i(LOG_TAG, "isServiceAvailable: VoImsFeature is not supported");
            return false;
        }
        boolean isFind = Arrays.stream(getCurrentProfileForSlot(phoneId)).anyMatch(new Predicate(service, rat) {
            public final /* synthetic */ String f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return ImsServiceStub.lambda$isServiceAvailable$7(this.f$0, this.f$1, (ImsProfile) obj);
            }
        });
        boolean isEnabled = isServiceEnabledByPhoneId(service, phoneId);
        String str = LOG_TAG;
        Log.i(str, "isServiceAvailable: " + service + ", rat: " + rat + ", profileFind:" + isFind + ", Enabled:" + isEnabled);
        if (!isFind || !isEnabled) {
            return false;
        }
        return true;
    }

    static /* synthetic */ boolean lambda$isServiceAvailable$7(String service, int rat, ImsProfile p) {
        return p != null && !p.hasEmergencySupport() && p.hasService(service, rat);
    }

    public boolean isServiceEnabledByPhoneId(String service, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return DmConfigHelper.getImsSwitchValue(this.mContext, service, phoneId) == 1;
    }

    public boolean hasVoLteSim() {
        return hasVoLteSimByPhoneId(SimUtil.getDefaultPhoneId());
    }

    public boolean hasVoLteSimByPhoneId(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        RegistrationManagerBase registrationManagerBase = this.mRegistrationManager;
        if (registrationManagerBase != null) {
            return registrationManagerBase.hasVoLteSim(phoneId);
        }
        IMSLog.d(LOG_TAG, phoneId, "hasVoLteSimByPhoneId - no mRegistrationManager");
        return true;
    }

    public void enableService(String service, boolean enable) {
        enableServiceByPhoneId(service, enable, 0);
    }

    public void enableServiceByPhoneId(String service, boolean enable, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String processName = PackageUtils.getProcessNameById(getContext(), Binder.getCallingPid());
        if ("com.samsung.advp.imssettings".equalsIgnoreCase(processName) || "com.android.phone".equals(processName)) {
            int i = 0;
            if (TextUtils.equals(service, ImsConstants.SystemSettings.VOLTE_SLOT1.getName()) || TextUtils.equals(service, ImsConstants.SystemSettings.VILTE_SLOT1.getName())) {
                Context context = this.mContext;
                if (!enable) {
                    i = 1;
                }
                DmConfigHelper.setImsUserSetting(context, service, i, phoneId);
            } else if (TextUtils.equals(service, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName())) {
                Context context2 = this.mContext;
                if (enable) {
                    i = 1;
                }
                DmConfigHelper.setImsUserSetting(context2, service, i, phoneId);
            } else {
                DmConfigHelper.setImsSwitch(this.mContext, service, enable, phoneId);
            }
        } else {
            Log.d(LOG_TAG, "deprecated] enableService is called by " + processName);
        }
    }

    public void enableVoLte(boolean enable) {
        enableVoLteByPhoneId(enable, 0);
    }

    public void enableVoLteByPhoneId(boolean enable, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        DmConfigHelper.setImsSwitch(this.mContext, "volte", enable, phoneId);
    }

    public void enableRcs(boolean enable) {
        enableRcsByPhoneId(enable, 0);
    }

    public void enableRcsByPhoneId(boolean enable, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        if (!ConfigUtil.checkMdmRcsStatus(this.mContext, phoneId) && enable) {
            IMSLog.e(LOG_TAG, phoneId, "RCS isn't allowed by MDM. Don't enable RCS");
        } else if ("com.samsung.advp.imssettings".equalsIgnoreCase(PackageUtils.getProcessNameById(getContext(), Binder.getCallingPid()))) {
            Log.d(LOG_TAG, "Called by ImsSettings app. Change main switch value.");
            enableRcsMainSwitchByPhoneId(enable, phoneId);
        } else {
            boolean z = true;
            if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 1, phoneId) != 1) {
                z = false;
            }
            boolean isRcsEnabled = z;
            String str = LOG_TAG;
            Log.i(str, "enableRcs: oldValue: " + isRcsEnabled + ", newValue: " + enable);
            changeOpModeByRcsSwtich(isRcsEnabled, enable, phoneId);
            if (SimUtil.getSimMno(phoneId) != Mno.SKT || enable) {
                ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, enable, phoneId);
            } else {
                Log.d(LOG_TAG, "enableRcs: Ignore RCS disable for SKT until server responds");
            }
        }
    }

    public int[] getCallCount(int phoneId) {
        if (isPermissionGranted()) {
            return this.mServiceModuleManager.getVolteServiceModule().getCallCount(phoneId);
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public boolean isForbidden() {
        return this.mRegistrationManager.isInvite403DisabledService(SimUtil.getDefaultPhoneId());
    }

    public boolean isForbiddenByPhoneId(int phoneId) {
        return this.mRegistrationManager.isInvite403DisabledService(phoneId);
    }

    public void transferCall(String msisdn, String dialogId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModuleManager.getVolteServiceModule().transferCall(msisdn, dialogId);
    }

    public int startLocalRingBackTone(int streamType, int volume, int toneType) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        IVolteServiceModule vm = this.mServiceModuleManager.getVolteServiceModule();
        if (vm != null) {
            return vm.startLocalRingBackTone(streamType, volume, toneType);
        }
        Log.e(LOG_TAG, "VolteServiceModule is not ready");
        return -1;
    }

    public int stopLocalRingBackTone() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        IVolteServiceModule vm = this.mServiceModuleManager.getVolteServiceModule();
        if (vm != null) {
            return vm.stopLocalRingBackTone();
        }
        Log.e(LOG_TAG, "VolteServiceModule is not ready");
        return -1;
    }

    public void changeAudioPath(int direction) {
        changeAudioPathForSlot(0, direction);
    }

    public void changeAudioPathForSlot(int phoneId, int direction) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        IVolteServiceModule vm = this.mServiceModuleManager.getVolteServiceModule();
        if (vm == null) {
            Log.e(LOG_TAG, "VolteServiceModule is not ready");
        } else {
            vm.updateAudioInterface(phoneId, direction);
        }
    }

    public boolean setVideocallType(int videocallType) {
        ImsConstants.SystemSettings.VILTE_SLOT1.set(this.mContext, videocallType);
        return true;
    }

    public int getVideocallType() {
        return ImsConstants.SystemSettings.VILTE_SLOT1.get(this.mContext, -1);
    }

    public void registerDmValueListener(IImsDmConfigListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "registerDmValueListener:");
        this.mRegistrationManager.registerDmListener(listener);
    }

    public void unregisterDmValueListener(IImsDmConfigListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "unregisterDmValueListener:");
        this.mRegistrationManager.unregisterDmListener(listener);
    }

    public ContentValues getConfigValues(String[] fields, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mDmConfigModule.getConfigValues(fields, phoneId);
    }

    public boolean updateConfigValues(ContentValues updateValue, int transactionId, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mDmConfigModule.updateConfigValues(updateValue, transactionId, phoneId);
    }

    public int startDmConfig() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mDmConfigModule.startDmConfig();
    }

    public void finishDmConfig(int transactionId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mDmConfigModule.finishDmConfig(transactionId);
    }

    public boolean isRttCall(int sessionId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "isRttCall");
        return this.mServiceModuleManager.getVolteServiceModule().isRttCall(sessionId);
    }

    public void setAutomaticMode(int phoneId, boolean mode) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "setAutomaticMode, mode=" + mode);
        this.mServiceModuleManager.getVolteServiceModule().setAutomaticMode(phoneId, mode);
    }

    public void setRttMode(int phoneId, int mode) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "setRttMode, mode=" + mode);
        this.mServiceModuleManager.getVolteServiceModule().setRttMode(phoneId, mode);
    }

    public int getRttMode(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "getRttMode");
        return this.mServiceModuleManager.getVolteServiceModule().getRttMode();
    }

    public void sendRttMessage(String msg) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "sendRttMessage, mode=" + msg);
        this.mServiceModuleManager.getVolteServiceModule().sendRttMessage(msg);
    }

    public void sendRttSessionModifyResponse(int callId, boolean accept) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        String str = LOG_TAG;
        Log.d(str, "sendRttSessionModifyResponse, accept=" + accept);
        this.mServiceModuleManager.getVolteServiceModule().sendRttSessionModifyResponse(callId, accept);
    }

    public void sendRttSessionModifyRequest(int callId, boolean mode) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "sendRttSessionModifyRequest");
        this.mServiceModuleManager.getVolteServiceModule().sendRttSessionModifyRequest(callId, mode);
    }

    public void registerRttEventListener(int phoneId, IRttEventListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "registerRttEventListener");
        this.mServiceModuleManager.getVolteServiceModule().registerRttEventListener(phoneId, listener);
    }

    public void unregisterRttEventListener(int phoneId, IRttEventListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "unregisterRttEventListener");
        this.mServiceModuleManager.getVolteServiceModule().unregisterRttEventListener(phoneId, listener);
    }

    public void triggerAutoConfigurationForApp(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        IAECModule iAECModule = this.mAECModule;
        if (iAECModule != null) {
            iAECModule.triggerAutoConfigForApp(phoneId);
        }
    }

    public void getAuthorizationHeader(int phoneId, String wwwAuthenticateHeader, String requestUri, String cipherSuite) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        GbaServiceModule gbaServiceModule = this.mGbaServiceModule;
        if (gbaServiceModule != null) {
            gbaServiceModule.getAuthorizationHeader(phoneId, requestUri, wwwAuthenticateHeader, cipherSuite);
        }
    }

    public boolean isValidGbaKey(int phoneId, String nafUri) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        GbaServiceModule gbaServiceModule = this.mGbaServiceModule;
        if (gbaServiceModule != null) {
            return gbaServiceModule.isValidGbaKey(phoneId, nafUri);
        }
        return false;
    }

    public void registerGbaEventListener(int phoneId, IGbaEventListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "registerGbaEventListener");
        this.mGbaServiceModule.registerGbaEventListener(phoneId, listener);
    }

    public void unregisterGbaEventListener(int phoneId, IGbaEventListener listener) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "unregisterGbaEventListener");
        this.mGbaServiceModule.unregisterGbaEventListener(phoneId, listener);
    }

    public String getGlobalSettingsValueToString(String projection, int phoneId, String defVal) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return getString(phoneId, projection, defVal);
    }

    public int getGlobalSettingsValueToInteger(String projection, int phoneId, int defVal) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return getInt(phoneId, projection, defVal);
    }

    public boolean getGlobalSettingsValueToBoolean(String projection, int phoneId, boolean defVal) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return getBoolean(phoneId, projection, defVal);
    }

    public int getInt(int phoneId, String projection, int defVal) {
        return GlobalSettingsManager.getInstance(this.mContext, phoneId).getInt(projection, defVal);
    }

    public boolean getBoolean(int phoneId, String projection, boolean defVal) {
        return GlobalSettingsManager.getInstance(this.mContext, phoneId).getBoolean(projection, defVal);
    }

    public String getString(int phoneId, String projection, String defVal) {
        return GlobalSettingsManager.getInstance(this.mContext, phoneId).getString(projection, defVal);
    }

    public String[] getStringArray(int phoneId, String key, String[] defVal) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return GlobalSettingsManager.getInstance(this.mContext, phoneId).getStringArray(key, defVal);
    }

    public void dump() {
        dump((PrintWriter) null);
    }

    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        dump(fout);
    }

    public IPdnController getPdnController() {
        return this.mPdnController;
    }

    public ICmcAccountManager getCmcAccountManager() {
        return this.mCmcAccountManager;
    }

    public IRcsPolicyManager getRcsPolicyManager() {
        return this.mRcsPolicyManager;
    }

    public IServiceModuleManager getServiceModuleManager() {
        return this.mServiceModuleManager;
    }

    public IRegistrationManager getRegistrationManager() {
        return this.mRegistrationManager;
    }

    public IConfigModule getConfigModule() {
        return this.mConfigModule;
    }

    public IGbaServiceModule getGbaService() {
        return this.mGbaServiceModule;
    }

    public IHandlerFactory getHandlerFactory() {
        return this.mHandlerFactory;
    }

    public IGoogleImsService getGoogleImsAdaptor() {
        return GoogleImsService.getInstance(this.mContext, this.mServiceModuleManager);
    }

    public IImsNotifier getImsNotifier() {
        return this.mGoogleImsAdaptor.getImsNotifier();
    }

    public IAECModule getAECModule() {
        return this.mAECModule;
    }

    public ICmcConnectivityController getP2pCC() {
        return this.mConnectivityController;
    }

    public IGeolocationController getGeolocationController() {
        return this.mGeolocationController;
    }

    public INtpTimeController getNtpTimeController() {
        return this.mNtpTimeController;
    }

    public IImsDiagMonitor getImsDiagMonitor() {
        return this.mImsDiagMonitor;
    }

    public IFcmHandler getFcmHandler() {
        if (this.mFcmHandler == null) {
            this.mFcmHandler = new FcmHandler(this.mContext);
        }
        return this.mFcmHandler;
    }

    public IIilManager getIilManager(int phoneId) {
        return this.mIilManagers.get(phoneId);
    }

    public List<ServiceModuleBase> getAllServiceModules() {
        return this.mServiceModuleManager.getAllServiceModules();
    }

    public Context getContext() {
        return this.mContext;
    }

    public void startAutoConfig(boolean force, Message onComplete) {
        this.mConfigModule.startAutoConfig(force, onComplete, SimUtil.getDefaultPhoneId());
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener, boolean broadcast, int phoneId) {
        if (isPermissionGranted()) {
            String str = LOG_TAG;
            IMSLog.d(str, phoneId, "registerImsRegistrationListener: broadcast = " + broadcast);
            if (phoneId == -1) {
                Log.d(LOG_TAG, "Requested registerListener without phoneId. register it by all phoneId.");
                this.mSimManagers.forEach(new Consumer(listener) {
                    public final /* synthetic */ IImsRegistrationListener f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        ImsServiceStub.this.lambda$registerImsRegistrationListener$8$ImsServiceStub(this.f$1, (ISimManager) obj);
                    }
                });
                return;
            }
            this.mRegistrationManager.registerListener(listener, broadcast, phoneId);
            return;
        }
        throw new SecurityException(LOG_TAG + "[" + phoneId + "] Permission denied");
    }

    public /* synthetic */ void lambda$registerImsRegistrationListener$8$ImsServiceStub(IImsRegistrationListener listener, ISimManager sm) {
        this.mRegistrationManager.registerListener(listener, sm.getSimSlotIndex());
    }

    public boolean isRcsEnabledByPhoneId(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        Log.d(LOG_TAG, "isRcsEnabled:");
        return this.mConfigModule.isValidAcsVersion(phoneId);
    }

    public Binder getBinder(String service) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getBinder(service);
    }

    public Binder getBinder(String service, String aux) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getBinder(service, aux);
    }

    public Binder getSemBinder() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModuleManager.getSemBinder();
    }

    public boolean isDefaultDmValue(String dm, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        if (ConfigConstants.ATCMD.OMADM_VALUE.equalsIgnoreCase(dm)) {
            ContentValues valueSp = this.mDmConfigModule.getConfigValues(new String[]{"93", "94", "31"}, phoneId);
            boolean isDmVolteDefault = "1".equalsIgnoreCase(valueSp.getAsString("93"));
            boolean isDmLvcDefault = "1".equalsIgnoreCase(valueSp.getAsString("94"));
            boolean isDmEabDefault = "1".equalsIgnoreCase(valueSp.getAsString("31"));
            String str = LOG_TAG;
            Log.d(str, "OMADM Default Value [VoLTE : " + isDmVolteDefault + ", LVC : " + isDmLvcDefault + ", EAB : " + isDmEabDefault + "]");
            if (!isDmVolteDefault || !isDmLvcDefault || !isDmEabDefault) {
                return false;
            }
            return true;
        } else if (ConfigConstants.ATCMD.SMS_SETTING.equalsIgnoreCase(dm)) {
            String smsFormat = this.mDmConfigModule.getConfigValues(new String[]{"9"}, phoneId).getAsString("9");
            String str2 = LOG_TAG;
            Log.d(str2, "SMS Setting Default Value : " + smsFormat);
            return "3GPP2".equalsIgnoreCase(smsFormat);
        } else {
            String smsFormat2 = LOG_TAG;
            Log.e(smsFormat2, dm + " is wrong value on isDefaultDmValue");
            return false;
        }
    }

    public boolean setDefaultDmValue(String dm, int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        if (ConfigConstants.ATCMD.OMADM_VALUE.equalsIgnoreCase(dm)) {
            ContentValues omadmUpdateValue = new ContentValues();
            omadmUpdateValue.put(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt("93"))).getName(), "1");
            omadmUpdateValue.put(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt("94"))).getName(), "1");
            omadmUpdateValue.put(((DATA.DM_FIELD_INFO) DATA.DM_FIELD_LIST.get(Integer.parseInt("31"))).getName(), "1");
            this.mContext.getContentResolver().insert(NvConfiguration.URI, omadmUpdateValue);
            return isDefaultDmValue(dm, phoneId);
        }
        String str = LOG_TAG;
        Log.e(str, dm + " is wrong value on setDefaultDmValue");
        return false;
    }

    public void notifyImsReady(boolean readiness, int phoneId) {
        Intent intent = new Intent();
        intent.setAction(readiness ? ImsConstants.Intents.ACTION_SERVICE_UP : ImsConstants.Intents.ACTION_SERVICE_DOWN);
        intent.putExtra(ImsConstants.Intents.EXTRA_ANDORID_PHONE_ID, phoneId);
        intent.putExtra(ImsConstants.Intents.EXTRA_SIMMOBILITY, ImsUtil.isSimMobilityActivated(phoneId));
        intent.addFlags(LogClass.SIM_EVENT);
        IntentUtil.sendBroadcast(this.mContext, intent);
        mIsImsAvailable = true;
        this.mIilManagers.get(phoneId).notifyImsReady(readiness);
        explicitGC();
    }

    private void linkToPhoneDeath() {
        IBinder phoneBinder = TelephonyFrameworkInitializer.getTelephonyServiceManager().getPhoneSubServiceRegisterer().tryGet();
        if (phoneBinder != null) {
            try {
                this.mEventLog.logAndAdd("Link to Phone Binder Death");
                phoneBinder.linkToDeath(new IBinder.DeathRecipient() {
                    public final void binderDied() {
                        ImsServiceStub.this.lambda$linkToPhoneDeath$9$ImsServiceStub();
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public /* synthetic */ void lambda$linkToPhoneDeath$9$ImsServiceStub() {
        this.mEventLog.logAndAdd("Phone Crashed. Cleanup IMS");
        this.mRegistrationManager.sendDeregister(6);
        getServiceModuleManager().cleanUpModules();
        this.mEventLog.logAndAdd("Restart service");
        IMSLog.c(LogClass.GEN_PHONE_BINDER_DIED, (String) null, true);
        System.exit(0);
    }

    public void sendCmcRecordingEvent(int phoneId, int event, SemCmcRecordingInfo info) {
        IVolteServiceModule vm = this.mServiceModuleManager.getVolteServiceModule();
        if (vm != null) {
            vm.sendCmcRecordingEvent(phoneId, event, info);
        }
    }

    public CmcCallInfo getCmcCallInfo() {
        IVolteServiceModule vm = this.mServiceModuleManager.getVolteServiceModule();
        if (vm != null) {
            return vm.getCmcCallInfo();
        }
        return null;
    }

    public void registerCmcRecordingListener(int phoneId, ISemCmcRecordingListener listener) {
        String str = LOG_TAG;
        Log.d(str, "registerCmcRecordingListener : " + phoneId);
        IVolteServiceModule vm = this.mServiceModuleManager.getVolteServiceModule();
        if (vm != null) {
            vm.registerCmcRecordingListener(phoneId, listener);
        }
    }

    public static class BootCompleteReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Log.i(ImsServiceStub.LOG_TAG, "ACTION_BOOT_COMPLETED received");
                int phoneCount = SimUtil.getPhoneCount();
                for (int i = 0; i < phoneCount; i++) {
                    DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.RCS, i);
                }
            }
        }
    }

    private static void checkUt(Context context) {
        try {
            if (context.getPackageManager().getPackageUid("com.salab.issuetracker", 0) == 1000) {
                Log.i(LOG_TAG, "issueTracker found should be UT device");
                IMSLog.setIsUt(true);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(LOG_TAG, "issueTracker not found");
        }
    }
}
