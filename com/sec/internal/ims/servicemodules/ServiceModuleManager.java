package com.sec.internal.ims.servicemodules;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.cmstore.CmsModule;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.ImageShareModule;
import com.sec.internal.ims.servicemodules.csh.VideoShareModule;
import com.sec.internal.ims.servicemodules.euc.EucModule;
import com.sec.internal.ims.servicemodules.gls.GlsModule;
import com.sec.internal.ims.servicemodules.im.ImModule;
import com.sec.internal.ims.servicemodules.openapi.ImsStatusService;
import com.sec.internal.ims.servicemodules.openapi.ImsStatusServiceModule;
import com.sec.internal.ims.servicemodules.openapi.OpenApiService;
import com.sec.internal.ims.servicemodules.openapi.OpenApiServiceModule;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService;
import com.sec.internal.ims.servicemodules.options.OptionsModule;
import com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService;
import com.sec.internal.ims.servicemodules.presence.PresenceModule;
import com.sec.internal.ims.servicemodules.session.SessionModule;
import com.sec.internal.ims.servicemodules.sms.SmsService;
import com.sec.internal.ims.servicemodules.sms.SmsServiceModule;
import com.sec.internal.ims.servicemodules.ss.UtService;
import com.sec.internal.ims.servicemodules.ss.UtServiceModule;
import com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager;
import com.sec.internal.ims.servicemodules.tapi.service.api.interfaces.ITapiServiceManager;
import com.sec.internal.ims.servicemodules.volte2.VolteService;
import com.sec.internal.ims.servicemodules.volte2.VolteServiceModule;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.cmstore.ICmsModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.handler.IHandlerFactory;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucModule;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IImsStatusServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IOpenApiServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.IOptionsModule;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ServiceModuleManager extends Handler implements IServiceModuleManager {
    private static final int EVT_CONFIG_CHANGED = 2;
    private static final int EVT_IMS_SWITCH_UPDATED = 1;
    private static final int EVT_SIM_READY = 3;
    private static final String IMS_SETTINGS_UPDATED = "android.intent.action.IMS_SETTINGS_UPDATED";
    private static final String LOG_TAG = "ServiceModuleManager";
    private static Set<String> OBSERVE_DM_SET = new HashSet();
    private static Set<String> OBSERVE_PREFIX_DM_SET = new HashSet();
    private Map<Integer, Boolean> mAutoConfigCompletedList = new ConcurrentHashMap();
    private Map<String, Binder> mBinders = new HashMap();
    CapabilityDiscoveryModule mCapabilityDiscoveryModule;
    private final ReentrantLock mChangingServiceModulesStateLock = new ReentrantLock();
    CmsModule mCmsModule;
    private ContentObserver mConfigObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange, Uri uri) {
            int phoneId = UriUtil.getSimSlotFromUri(uri);
            Log.d(ServiceModuleManager.LOG_TAG, "onChange[" + phoneId + "]: config changed : " + uri.getLastPathSegment());
            if (!TextUtils.isEmpty(uri.getLastPathSegment())) {
                ServiceModuleManager.this.notifyConfigChanged(uri.getLastPathSegment(), phoneId);
            }
            ServiceModuleManager serviceModuleManager = ServiceModuleManager.this;
            serviceModuleManager.sendMessage(serviceModuleManager.obtainMessage(1, uri));
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    EucModule mEucModule;
    GlsModule mGlsModule;
    private final IHandlerFactory mHandlerFactory;
    ImModule mImModule;
    ImageShareModule mImageShareModule;
    private IImsFramework mImsFramework;
    ImsStatusServiceModule mImsStatusServiceModule;
    private Map<Integer, ContentValues> mLastImsServiceSwitches = new ConcurrentHashMap();
    private Looper mLooper;
    OpenApiServiceModule mOpenApiServiceModule;
    OptionsModule mOptionsModule;
    private boolean[] mPendingCapabilitiesUpdate;
    PresenceModule mPresenceModule;
    private final IRegistrationManager mRegMan;
    private Binder mSemBinder = null;
    private List<ServiceModuleBase> mServiceModules = new CopyOnWriteArrayList();
    SessionModule mSessionModule;
    private SimEventListener mSimEventListener = new SimEventListener();
    private List<ISimManager> mSimManagers;
    SmsServiceModule mSmsServiceModule;
    private boolean mStarted = false;
    TapiServiceManager mTapiServiceManager;
    UtServiceModule mUtServiceModule;
    VideoShareModule mVideoShareModule;
    VolteServiceModule mVolteServiceModule;

    static {
        OBSERVE_DM_SET.add("EAB_SETTING");
        OBSERVE_DM_SET.add("LVC_ENABLED");
        OBSERVE_DM_SET.add("VOLTE_ENABLED");
        OBSERVE_DM_SET.add("CAP_CACHE_EXP");
        OBSERVE_DM_SET.add("CAP_POLL_INTERVAL");
        OBSERVE_DM_SET.add("SRC_THROTTLE_PUBLISH");
        OBSERVE_DM_SET.add("SUBSCRIBE_MAX_ENTRY");
        OBSERVE_DM_SET.add("AVAIL_CACHE_EXP");
        OBSERVE_DM_SET.add("POLL_LIST_SUB_EXP");
        OBSERVE_DM_SET.add("PUBLISH_TIMER");
        OBSERVE_DM_SET.add("PUBLISH_TIMER_EXTEND");
        OBSERVE_DM_SET.add("PUBLISH_ERR_RETRY_TIMER");
        OBSERVE_DM_SET.add("CAP_DISCOVERY");
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_EAB_SETTING);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_LVC_ENABLED);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_CAP_CACHE_EXP);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_CAP_POLL_INTERVAL);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_SRC_THROTTLE_PUBLISH);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_SUBSCRIBE_MAX_ENTRY);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_AVAIL_CACHE_EXP);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER_EXTEND);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_PUBLISH_ERR_RETRY_TIMER);
        OBSERVE_PREFIX_DM_SET.add(ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY);
    }

    public ServiceModuleManager(Looper looper, Context context, IImsFramework imsFramework, List<ISimManager> simManagers, IRegistrationManager regMan, IHandlerFactory handlerFactory) {
        super(looper);
        Log.d(LOG_TAG, "created");
        this.mContext = context;
        this.mSimManagers = simManagers;
        this.mImsFramework = imsFramework;
        this.mRegMan = regMan;
        this.mHandlerFactory = handlerFactory;
    }

    public void initSequentially() {
        for (ISimManager sm : this.mSimManagers) {
            if (sm.isSimLoaded()) {
                int phoneId = sm.getSimSlotIndex();
                sendMessage(obtainMessage(3, phoneId, 0, (Object) null));
                IMSLog.d(LOG_TAG, phoneId, "SIM is ready subId:");
            }
            sm.registerSimCardEventListener(this.mSimEventListener);
            this.mLastImsServiceSwitches.put(Integer.valueOf(sm.getSimSlotIndex()), new ContentValues());
        }
        ContentResolver cr = this.mContext.getContentResolver();
        for (String dm : OBSERVE_DM_SET) {
            cr.registerContentObserver(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/" + dm), false, this.mConfigObserver);
        }
        for (String prefix_dm : OBSERVE_PREFIX_DM_SET) {
            cr.registerContentObserver(Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/" + prefix_dm), false, this.mConfigObserver);
        }
        if (SimUtil.getPhoneCount() > 0) {
            Log.d(LOG_TAG, "Initializting ServiceModules.");
            createIMSServiceModules();
            startIMSServiceModules();
            return;
        }
        Log.d(LOG_TAG, "no phone skip Initializting ServiceModules.");
    }

    /* JADX WARNING: type inference failed for: r3v13, types: [com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService, android.os.Binder] */
    public void createIMSServiceModules() {
        this.mChangingServiceModulesStateLock.lock();
        try {
            Log.d(LOG_TAG, "createIMSServiceModules");
            HandlerThread thread = new HandlerThread("ServiceModule");
            thread.start();
            this.mLooper = thread.getLooper();
            SmsServiceModule smsServiceModule = new SmsServiceModule(this.mLooper, this.mContext, this.mHandlerFactory.getSmsHandler());
            this.mSmsServiceModule = smsServiceModule;
            this.mServiceModules.add(smsServiceModule);
            this.mBinders.put("smsip", new SmsService(this.mSmsServiceModule));
            VolteServiceModule volteServiceModule = new VolteServiceModule(this.mLooper, this.mContext, this.mRegMan, this.mImsFramework.getPdnController(), this.mHandlerFactory.getVolteStackAdaptor(), this.mHandlerFactory.getMediaHandler(), this.mHandlerFactory.getOptionsHandler());
            this.mVolteServiceModule = volteServiceModule;
            this.mServiceModules.add(volteServiceModule);
            VolteService volteService = new VolteService(this.mVolteServiceModule);
            this.mBinders.put("mmtel", volteService);
            ImsStatusServiceModule imsStatusServiceModule = new ImsStatusServiceModule(this.mLooper, volteService);
            this.mImsStatusServiceModule = imsStatusServiceModule;
            this.mServiceModules.add(imsStatusServiceModule);
            this.mBinders.put("ImsStatus", new ImsStatusService(this.mImsStatusServiceModule));
            OpenApiServiceModule openApiServiceModule = new OpenApiServiceModule(this.mLooper, this.mContext, this.mHandlerFactory.getRawSipHandler());
            this.mOpenApiServiceModule = openApiServiceModule;
            this.mServiceModules.add(openApiServiceModule);
            this.mBinders.put("OpenApi", new OpenApiService(this.mOpenApiServiceModule));
            UtServiceModule utServiceModule = new UtServiceModule(this.mLooper, this.mContext, this.mImsFramework);
            this.mUtServiceModule = utServiceModule;
            this.mServiceModules.add(utServiceModule);
            this.mBinders.put("ss", new UtService(this.mUtServiceModule));
            this.mBinders.put("options", new CapabilityDiscoveryService());
            this.mSemBinder = new SemCapabilityDiscoveryService();
            for (ServiceModuleBase m : this.mServiceModules) {
                m.init();
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
        if (r2.hasService("presence") != false) goto L_0x003a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void createRcsServiceModulesAndStart(com.sec.internal.interfaces.ims.core.IRegistrationManager r18, com.sec.ims.settings.ImsProfile r19, int r20) {
        /*
            r17 = this;
            r1 = r17
            r2 = r19
            r3 = r20
            java.lang.String r0 = "vs"
            java.lang.String r4 = "options"
            java.util.concurrent.CopyOnWriteArrayList r5 = new java.util.concurrent.CopyOnWriteArrayList
            r5.<init>()
            java.util.concurrent.locks.ReentrantLock r6 = r1.mChangingServiceModulesStateLock
            r6.lock()
            android.content.Context r6 = r1.mContext     // Catch:{ all -> 0x01ed }
            java.lang.String r7 = "rcsswitch"
            int r6 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r6, (java.lang.String) r7, (int) r3)     // Catch:{ all -> 0x01ed }
            r7 = 1
            if (r6 != r7) goto L_0x0023
            goto L_0x0024
        L_0x0023:
            r7 = 0
        L_0x0024:
            r6 = r7
            com.sec.internal.constants.Mno r7 = com.sec.internal.helper.SimUtil.getSimMno(r20)     // Catch:{ all -> 0x01ed }
            if (r6 == 0) goto L_0x01e3
            boolean r8 = r2.hasService(r4)     // Catch:{ all -> 0x01ed }
            java.lang.String r9 = "presence"
            if (r8 != 0) goto L_0x003a
            boolean r8 = r2.hasService(r9)     // Catch:{ all -> 0x01ed }
            if (r8 == 0) goto L_0x01e3
        L_0x003a:
            com.sec.internal.ims.servicemodules.im.ImModule r8 = r1.mImModule     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x0059
            com.sec.internal.ims.servicemodules.im.ImModule r8 = new com.sec.internal.ims.servicemodules.im.ImModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r10 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r11 = r1.mContext     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r12 = r1.mHandlerFactory     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r12 = r12.getImHandler()     // Catch:{ all -> 0x01ed }
            r8.<init>(r10, r11, r12)     // Catch:{ all -> 0x01ed }
            r1.mImModule = r8     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r10 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r10.add(r8)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.im.ImModule r8 = r1.mImModule     // Catch:{ all -> 0x01ed }
            r5.add(r8)     // Catch:{ all -> 0x01ed }
        L_0x0059:
            com.sec.internal.ims.servicemodules.session.SessionModule r8 = r1.mSessionModule     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x0078
            com.sec.internal.ims.servicemodules.session.SessionModule r8 = new com.sec.internal.ims.servicemodules.session.SessionModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r10 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r11 = r1.mContext     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r12 = r1.mHandlerFactory     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r12 = r12.getImHandler()     // Catch:{ all -> 0x01ed }
            r8.<init>(r10, r11, r12)     // Catch:{ all -> 0x01ed }
            r1.mSessionModule = r8     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r10 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r10.add(r8)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.session.SessionModule r8 = r1.mSessionModule     // Catch:{ all -> 0x01ed }
            r5.add(r8)     // Catch:{ all -> 0x01ed }
        L_0x0078:
            java.lang.String r8 = "gls"
            boolean r8 = r2.hasService(r8)     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x0084
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.ATT     // Catch:{ all -> 0x01ed }
            if (r7 != r8) goto L_0x009d
        L_0x0084:
            com.sec.internal.ims.servicemodules.gls.GlsModule r8 = r1.mGlsModule     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x009d
            com.sec.internal.ims.servicemodules.gls.GlsModule r8 = new com.sec.internal.ims.servicemodules.gls.GlsModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r10 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r11 = r1.mContext     // Catch:{ all -> 0x01ed }
            r8.<init>(r10, r11)     // Catch:{ all -> 0x01ed }
            r1.mGlsModule = r8     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r10 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r10.add(r8)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.gls.GlsModule r8 = r1.mGlsModule     // Catch:{ all -> 0x01ed }
            r5.add(r8)     // Catch:{ all -> 0x01ed }
        L_0x009d:
            java.lang.String r8 = "euc"
            boolean r8 = r2.hasService(r8)     // Catch:{ all -> 0x01ed }
            if (r8 == 0) goto L_0x00c4
            com.sec.internal.ims.servicemodules.euc.EucModule r8 = r1.mEucModule     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x00c4
            com.sec.internal.ims.servicemodules.euc.EucModule r8 = new com.sec.internal.ims.servicemodules.euc.EucModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r10 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r11 = r1.mContext     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r12 = r1.mHandlerFactory     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.servicemodules.euc.IEucServiceInterface r12 = r12.getEucHandler()     // Catch:{ all -> 0x01ed }
            r8.<init>(r10, r11, r12)     // Catch:{ all -> 0x01ed }
            r1.mEucModule = r8     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r10 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r10.add(r8)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.euc.EucModule r8 = r1.mEucModule     // Catch:{ all -> 0x01ed }
            r5.add(r8)     // Catch:{ all -> 0x01ed }
        L_0x00c4:
            java.lang.String r8 = "is"
            boolean r8 = r2.hasService(r8)     // Catch:{ all -> 0x01ed }
            if (r8 == 0) goto L_0x00eb
            com.sec.internal.ims.servicemodules.csh.ImageShareModule r8 = r1.mImageShareModule     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x00eb
            com.sec.internal.ims.servicemodules.csh.ImageShareModule r8 = new com.sec.internal.ims.servicemodules.csh.ImageShareModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r10 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r11 = r1.mContext     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r12 = r1.mHandlerFactory     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface r12 = r12.getIshHandler()     // Catch:{ all -> 0x01ed }
            r8.<init>(r10, r11, r12)     // Catch:{ all -> 0x01ed }
            r1.mImageShareModule = r8     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r10 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r10.add(r8)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.csh.ImageShareModule r8 = r1.mImageShareModule     // Catch:{ all -> 0x01ed }
            r5.add(r8)     // Catch:{ all -> 0x01ed }
        L_0x00eb:
            boolean r8 = r2.hasService(r0)     // Catch:{ all -> 0x01ed }
            if (r8 == 0) goto L_0x011c
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r8 = r1.mVideoShareModule     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x011c
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r8 = new com.sec.internal.ims.servicemodules.csh.VideoShareModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r10 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r11 = r1.mContext     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r12 = r1.mHandlerFactory     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface r12 = r12.getVshHandler()     // Catch:{ all -> 0x01ed }
            r8.<init>(r10, r11, r12)     // Catch:{ all -> 0x01ed }
            r1.mVideoShareModule = r8     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r10 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r10.add(r8)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r8 = r1.mVideoShareModule     // Catch:{ all -> 0x01ed }
            r5.add(r8)     // Catch:{ all -> 0x01ed }
            java.util.Map<java.lang.String, android.os.Binder> r8 = r1.mBinders     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.csh.VshBinderFuntions r10 = new com.sec.internal.ims.servicemodules.csh.VshBinderFuntions     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.csh.VideoShareModule r11 = r1.mVideoShareModule     // Catch:{ all -> 0x01ed }
            r10.<init>(r11)     // Catch:{ all -> 0x01ed }
            r8.put(r0, r10)     // Catch:{ all -> 0x01ed }
        L_0x011c:
            com.sec.internal.ims.servicemodules.options.OptionsModule r0 = r1.mOptionsModule     // Catch:{ all -> 0x01ed }
            if (r0 != 0) goto L_0x0133
            com.sec.internal.ims.servicemodules.options.OptionsModule r0 = new com.sec.internal.ims.servicemodules.options.OptionsModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r8 = r1.mLooper     // Catch:{ all -> 0x01ed }
            r0.<init>(r8)     // Catch:{ all -> 0x01ed }
            r1.mOptionsModule = r0     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r8 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r8.add(r0)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.OptionsModule r0 = r1.mOptionsModule     // Catch:{ all -> 0x01ed }
            r5.add(r0)     // Catch:{ all -> 0x01ed }
        L_0x0133:
            com.sec.internal.ims.servicemodules.presence.PresenceModule r0 = r1.mPresenceModule     // Catch:{ all -> 0x01ed }
            if (r0 != 0) goto L_0x014c
            com.sec.internal.ims.servicemodules.presence.PresenceModule r0 = new com.sec.internal.ims.servicemodules.presence.PresenceModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r8 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r10 = r1.mContext     // Catch:{ all -> 0x01ed }
            r0.<init>(r8, r10)     // Catch:{ all -> 0x01ed }
            r1.mPresenceModule = r0     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r8 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r8.add(r0)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.presence.PresenceModule r0 = r1.mPresenceModule     // Catch:{ all -> 0x01ed }
            r5.add(r0)     // Catch:{ all -> 0x01ed }
        L_0x014c:
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r0 = r1.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01ed }
            if (r0 != 0) goto L_0x0194
            com.sec.internal.ims.servicemodules.options.OptionsModule r0 = r1.mOptionsModule     // Catch:{ all -> 0x01ed }
            if (r0 == 0) goto L_0x0194
            com.sec.internal.ims.servicemodules.presence.PresenceModule r0 = r1.mPresenceModule     // Catch:{ all -> 0x01ed }
            if (r0 == 0) goto L_0x0194
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r0 = new com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule     // Catch:{ all -> 0x01ed }
            android.os.Looper r11 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r12 = r1.mContext     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.OptionsModule r13 = r1.mOptionsModule     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.presence.PresenceModule r14 = r1.mPresenceModule     // Catch:{ all -> 0x01ed }
            com.sec.internal.interfaces.ims.core.IRegistrationManager r15 = r1.mRegMan     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.im.ImModule r8 = r1.mImModule     // Catch:{ all -> 0x01ed }
            r10 = r0
            r16 = r8
            r10.<init>(r11, r12, r13, r14, r15, r16)     // Catch:{ all -> 0x01ed }
            r1.mCapabilityDiscoveryModule = r0     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r8 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r8.add(r0)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r0 = r1.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01ed }
            r5.add(r0)     // Catch:{ all -> 0x01ed }
            java.util.Map<java.lang.String, android.os.Binder> r0 = r1.mBinders     // Catch:{ all -> 0x01ed }
            java.lang.Object r0 = r0.get(r4)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService r0 = (com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService) r0     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r8 = r1.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01ed }
            r0.setServiceModule(r8)     // Catch:{ all -> 0x01ed }
            android.os.Binder r0 = r1.mSemBinder     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService r0 = (com.sec.internal.ims.servicemodules.options.SemCapabilityDiscoveryService) r0     // Catch:{ all -> 0x01ed }
            java.util.Map<java.lang.String, android.os.Binder> r8 = r1.mBinders     // Catch:{ all -> 0x01ed }
            java.lang.Object r4 = r8.get(r4)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService r4 = (com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService) r4     // Catch:{ all -> 0x01ed }
            r0.setServiceModule(r4)     // Catch:{ all -> 0x01ed }
        L_0x0194:
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r0 = r1.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01ed }
            if (r0 == 0) goto L_0x01a4
            java.util.Map<java.lang.String, android.os.Binder> r0 = r1.mBinders     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.presence.PresenceService r4 = new com.sec.internal.ims.servicemodules.presence.PresenceService     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r8 = r1.mCapabilityDiscoveryModule     // Catch:{ all -> 0x01ed }
            r4.<init>(r8)     // Catch:{ all -> 0x01ed }
            r0.put(r9, r4)     // Catch:{ all -> 0x01ed }
        L_0x01a4:
            boolean r0 = com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager.isSupportTapi()     // Catch:{ all -> 0x01ed }
            if (r0 == 0) goto L_0x01c3
            com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager r0 = r1.mTapiServiceManager     // Catch:{ all -> 0x01ed }
            if (r0 != 0) goto L_0x01c3
            com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager r0 = new com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager     // Catch:{ all -> 0x01ed }
            android.os.Looper r4 = r1.mLooper     // Catch:{ all -> 0x01ed }
            android.content.Context r8 = r1.mContext     // Catch:{ all -> 0x01ed }
            r0.<init>(r4, r8)     // Catch:{ all -> 0x01ed }
            r1.mTapiServiceManager = r0     // Catch:{ all -> 0x01ed }
            java.util.List<com.sec.internal.ims.servicemodules.base.ServiceModuleBase> r4 = r1.mServiceModules     // Catch:{ all -> 0x01ed }
            r4.add(r0)     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager r0 = r1.mTapiServiceManager     // Catch:{ all -> 0x01ed }
            r5.add(r0)     // Catch:{ all -> 0x01ed }
        L_0x01c3:
            java.util.Iterator r0 = r5.iterator()     // Catch:{ all -> 0x01ed }
        L_0x01c7:
            boolean r4 = r0.hasNext()     // Catch:{ all -> 0x01ed }
            if (r4 == 0) goto L_0x01e3
            java.lang.Object r4 = r0.next()     // Catch:{ all -> 0x01ed }
            com.sec.internal.ims.servicemodules.base.ServiceModuleBase r4 = (com.sec.internal.ims.servicemodules.base.ServiceModuleBase) r4     // Catch:{ all -> 0x01ed }
            boolean r8 = r4.isReady()     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x01e2
            boolean r8 = r4.isRunning()     // Catch:{ all -> 0x01ed }
            if (r8 != 0) goto L_0x01e2
            r4.init()     // Catch:{ all -> 0x01ed }
        L_0x01e2:
            goto L_0x01c7
        L_0x01e3:
            java.util.concurrent.locks.ReentrantLock r0 = r1.mChangingServiceModulesStateLock
            r0.unlock()
            r1.startRcsServiceModules(r5, r3)
            return
        L_0x01ed:
            r0 = move-exception
            java.util.concurrent.locks.ReentrantLock r4 = r1.mChangingServiceModulesStateLock
            r4.unlock()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ServiceModuleManager.createRcsServiceModulesAndStart(com.sec.internal.interfaces.ims.core.IRegistrationManager, com.sec.ims.settings.ImsProfile, int):void");
    }

    public void serviceStartDeterminer(IRegistrationManager regMan, List<ImsProfile> profiles, int phoneId) {
        for (ImsProfile profile : profiles) {
            if (ImsProfile.hasRcsService(profile)) {
                createRcsServiceModulesAndStart(regMan, profile, phoneId);
            }
        }
        List<String> services = getExtendedServices(phoneId);
        if (!services.isEmpty() && services.contains("cms") && !"AIO".equals(OmcCode.getNWCode(phoneId)) && this.mCmsModule == null) {
            CmsModule cmsModule = new CmsModule(this.mLooper, this.mContext);
            this.mCmsModule = cmsModule;
            this.mServiceModules.add(cmsModule);
            if (isStartRequired(this.mCmsModule, phoneId, (ISimManager) null)) {
                this.mCmsModule.init();
                this.mCmsModule.start();
            }
        }
    }

    public boolean isLooperExist() {
        return this.mLooper != null;
    }

    private synchronized void startRcsServiceModules(List<ServiceModuleBase> rcsServiceModules, int phoneId) {
        for (ServiceModuleBase module : rcsServiceModules) {
            if (isStartRequired(module, phoneId, (ISimManager) null)) {
                module.start();
            }
        }
        this.mLastImsServiceSwitches.put(Integer.valueOf(phoneId), DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) null, phoneId));
    }

    public synchronized void startIMSServiceModules() {
        this.mChangingServiceModulesStateLock.lock();
        try {
            Log.d(LOG_TAG, "startServiceModules");
            if (this.mStarted) {
                Log.d(LOG_TAG, "startServiceModules() - already started");
                return;
            }
            for (ISimManager sm : this.mSimManagers) {
                int phoneId = sm.getSimSlotIndex();
                for (ServiceModuleBase module : this.mServiceModules) {
                    if (isStartRequired(module, phoneId, sm)) {
                        module.start();
                    }
                }
                this.mLastImsServiceSwitches.put(Integer.valueOf(phoneId), DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) null, phoneId));
            }
            this.mStarted = true;
            if (this.mPendingCapabilitiesUpdate != null) {
                for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
                    if (this.mPendingCapabilitiesUpdate[i]) {
                        updateCapabilities(i);
                        this.mPendingCapabilitiesUpdate[i] = false;
                    }
                }
            }
            this.mChangingServiceModulesStateLock.unlock();
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public Binder getBinder(String service) {
        return getBinder(service, (String) null);
    }

    public synchronized Binder getBinder(String service, String aux) {
        String serviceName;
        serviceName = service;
        if (aux != null) {
            serviceName = serviceName + "-" + aux;
        }
        Log.d(LOG_TAG, "getBinder for " + serviceName);
        return this.mBinders.get(serviceName);
    }

    public Binder getSemBinder() {
        return this.mSemBinder;
    }

    public void notifyReRegistering(int phoneId, Set<String> newServices) {
        this.mChangingServiceModulesStateLock.lock();
        try {
            IMSLog.d(LOG_TAG, phoneId, "notify Ims Re-registration : " + newServices);
            for (ServiceModuleBase module : this.mServiceModules) {
                if (module.isRunning()) {
                    module.onReRegistering(phoneId, newServices);
                }
            }
            updateCapabilities(phoneId);
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    private boolean needRegistrationNotification(ServiceModuleBase module, Set<String> services) {
        Set<String> dst = new HashSet<>(Arrays.asList(module.getServicesRequiring()));
        Log.d(LOG_TAG, "Service not matched. Not notified to " + module.getName() + " " + dst);
        return !Collections.disjoint(services, dst);
    }

    public void notifyImsRegistration(ImsRegistration regiInfo, boolean registered, int errorCode) {
        int phoneId = regiInfo.getPhoneId();
        ImsProfile profile = regiInfo.getImsProfile();
        IMSLog.d(LOG_TAG, phoneId, "notifyImsRegistration: [" + profile.getName() + "] registered: " + registered + ", errorCode: " + errorCode);
        ImsRegistration reg = new ImsRegistration(regiInfo);
        IConfigModule cm = this.mImsFramework.getConfigModule();
        if (cm != null) {
            cm.onRegistrationStatusChanged(registered, errorCode, regiInfo);
        }
        if (!registered) {
            this.mChangingServiceModulesStateLock.lock();
            try {
                for (ServiceModuleBase module : this.mServiceModules) {
                    if (module.isRunning()) {
                        if (needRegistrationNotification(module, reg.getServices())) {
                            module.onDeregistered(reg, errorCode);
                        }
                    }
                }
            } finally {
                this.mChangingServiceModulesStateLock.unlock();
            }
        } else {
            Set<String> deregisteredServices = reg.getImsProfile().getAllServiceSetFromAllNetwork();
            for (String service : reg.getServices()) {
                deregisteredServices.remove(service);
            }
            if (!cm.isValidAcsVersion(phoneId)) {
                Log.d(LOG_TAG, "RCS disabled : remove rcs services from deregi list");
                for (String service2 : ImsProfile.getRcsServiceList()) {
                    deregisteredServices.remove(service2);
                }
            }
            this.mChangingServiceModulesStateLock.lock();
            try {
                for (ServiceModuleBase module2 : this.mServiceModules) {
                    if (module2.isRunning()) {
                        if (needRegistrationNotification(module2, regiInfo.getServices())) {
                            module2.onRegistered(reg);
                        } else if (needRegistrationNotification(module2, deregisteredServices)) {
                            module2.onDeregistered(reg, errorCode);
                        }
                    }
                }
            } finally {
                this.mChangingServiceModulesStateLock.unlock();
            }
        }
        if (((Boolean) Optional.ofNullable(this.mSimManagers.get(phoneId)).map($$Lambda$ByLF0vigF4DGnwqQtI1aS3uyLeQ.INSTANCE).map($$Lambda$C16e9kxCNBgNe18grmjdCfeP1H8.INSTANCE).orElse(false)).booleanValue() && profile.hasEmergencySupport()) {
            IVolteServiceModule vsm = this.mImsFramework.getServiceModuleManager().getVolteServiceModule();
            if (!vsm.isRunning()) {
                if (registered) {
                    vsm.onRegistered(reg);
                } else {
                    vsm.onDeregistered(reg, errorCode);
                }
            }
        }
        updateCapabilities(phoneId);
    }

    public void notifyDeregistering(ImsRegistration reg) {
        boolean isNotified = false;
        this.mChangingServiceModulesStateLock.lock();
        try {
            for (ServiceModuleBase module : this.mServiceModules) {
                if (module.isRunning() && needRegistrationNotification(module, reg.getServices())) {
                    module.onDeregistering(reg);
                    isNotified = true;
                }
            }
            if (isNotified) {
                updateCapabilities(reg.getPhoneId());
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void notifyRcsDeregistering(Set<String> deregServices, ImsRegistration reg) {
        boolean isNotified = false;
        Set<String> deregRcsService = new HashSet<>();
        for (String service : ImsProfile.getRcsServiceList()) {
            if (deregServices.contains(service)) {
                deregRcsService.add(service);
            }
        }
        this.mChangingServiceModulesStateLock.lock();
        try {
            for (ServiceModuleBase module : this.mServiceModules) {
                if (module.isRunning() && needRegistrationNotification(module, deregRcsService)) {
                    module.onDeregistering(reg);
                    isNotified = true;
                }
            }
            if (isNotified) {
                updateCapabilities(reg.getPhoneId());
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void notifyConfigChanged(String dmUri, int phoneId) {
        Log.d(LOG_TAG, "notifyConfigChanged: dmUri " + dmUri);
        this.mChangingServiceModulesStateLock.lock();
        try {
            for (ServiceModuleBase module : this.mServiceModules) {
                if (module.isRunning()) {
                    module.onImsConifgChanged(phoneId, dmUri);
                }
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    /* JADX INFO: finally extract failed */
    public void notifyConfigured(boolean checkAutoConfig, int phoneId) {
        Log.d(LOG_TAG, "notifyConfigured: phoneId " + phoneId);
        if (!checkAutoConfig || (this.mAutoConfigCompletedList.containsKey(Integer.valueOf(phoneId)) && this.mAutoConfigCompletedList.get(Integer.valueOf(phoneId)).booleanValue())) {
            this.mChangingServiceModulesStateLock.lock();
            try {
                for (ServiceModuleBase module : this.mServiceModules) {
                    if (module.isRunning()) {
                        if (module != this.mCapabilityDiscoveryModule) {
                            module.onConfigured(phoneId);
                        }
                    }
                }
                this.mChangingServiceModulesStateLock.unlock();
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscoveryModule;
                if (capabilityDiscoveryModule == null) {
                    return;
                }
                if (capabilityDiscoveryModule.isRunning()) {
                    Log.d(LOG_TAG, "notifyConfigured: CDM is running");
                    this.mCapabilityDiscoveryModule.onConfigured(phoneId);
                    return;
                }
                Log.d(LOG_TAG, "notifyConfigured: CDM is not running, trigger tryRegister");
                this.mImsFramework.getRegistrationManager().setOwnCapabilities(phoneId, new Capabilities());
            } catch (Throwable th) {
                this.mChangingServiceModulesStateLock.unlock();
                throw th;
            }
        }
    }

    public void notifySimChange(int phoneId) {
        CapabilityDiscoveryModule capabilityDiscoveryModule;
        Log.d(LOG_TAG, "notifySimChange");
        Mno mno = SimUtil.getSimMno(phoneId);
        for (ServiceModuleBase module : this.mServiceModules) {
            if (module.isRunning() || ((capabilityDiscoveryModule = this.mCapabilityDiscoveryModule) != null && module == capabilityDiscoveryModule && ConfigUtil.isRcsEur(mno))) {
                module.onSimChanged(phoneId);
            }
        }
    }

    public void notifyNetworkChanged(NetworkEvent event, int phoneId) {
        for (ServiceModuleBase module : this.mServiceModules) {
            if (module.isRunning()) {
                module.onNetworkChanged(new NetworkEvent(event), phoneId);
            }
        }
        updateCapabilities(phoneId);
    }

    public void handleIntent(Intent intent) {
        Log.d(LOG_TAG, "handleIntent:");
        for (ServiceModuleBase module : this.mServiceModules) {
            if (module.isRunning()) {
                module.handleIntent(intent);
            }
        }
    }

    public List<ServiceModuleBase> getAllServiceModules() {
        return Collections.unmodifiableList(this.mServiceModules);
    }

    public Handler getServiceModuleHandler(String service) {
        for (ServiceModuleBase module : this.mServiceModules) {
            if (module.getClass().getSimpleName().equals(service)) {
                return module;
            }
        }
        return null;
    }

    public IImModule getImModule() {
        return this.mImModule;
    }

    public IGlsModule getGlsModule() {
        return this.mGlsModule;
    }

    public IOptionsModule getOptionsModule() {
        return this.mOptionsModule;
    }

    public IPresenceModule getPresenceModule() {
        return this.mPresenceModule;
    }

    public ICapabilityDiscoveryModule getCapabilityDiscoveryModule() {
        return this.mCapabilityDiscoveryModule;
    }

    public IEucModule getEucModule() {
        return this.mEucModule;
    }

    public ISmsServiceModule getSmsServiceModule() {
        return this.mSmsServiceModule;
    }

    public ISessionModule getSessionModule() {
        return this.mSessionModule;
    }

    public ICmsModule getCmsModule() {
        return this.mCmsModule;
    }

    public IVolteServiceModule getVolteServiceModule() {
        return this.mVolteServiceModule;
    }

    public IImsStatusServiceModule getImsStatusServiceModule() {
        return this.mImsStatusServiceModule;
    }

    public IImageShareModule getImageShareModule() {
        return this.mImageShareModule;
    }

    public IVideoShareModule getVideoShareModule() {
        return this.mVideoShareModule;
    }

    public ITapiServiceManager getTapiServiceManager() {
        return this.mTapiServiceManager;
    }

    public IOpenApiServiceModule getOpenApiServiceModule() {
        return this.mOpenApiServiceModule;
    }

    public IUtServiceModule getUtServiceModule() {
        return this.mUtServiceModule;
    }

    public void dump() {
        for (ServiceModuleBase module : this.mServiceModules) {
            if (module.isRunning()) {
                module.dump();
            }
        }
    }

    public void notifyCallStateChanged(List<ICall> calls, int phoneId) {
        for (ServiceModuleBase module : this.mServiceModules) {
            module.onCallStateChanged(phoneId, calls);
        }
    }

    public void notifyAutoConfigDone(int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "notifyAutoConfigDone");
        this.mAutoConfigCompletedList.put(Integer.valueOf(phoneId), true);
    }

    public void notifyOmadmVolteConfigDone(int phoneId) {
        Log.d(LOG_TAG, "notifyOmadmVolteConfigDone()");
        Uri.Builder buildUpon = Uri.parse("content://com.samsung.rcs.dmconfigurationprovider/").buildUpon();
        sendMessage(obtainMessage(1, buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + phoneId).build()));
    }

    public void notifyImsSwitchUpdateToApp() {
        IntentUtil.sendBroadcast(this.mContext, new Intent(IMS_SETTINGS_UPDATED));
    }

    public void onImsSwitchUpdated(int phoneId) {
        Iterator<ServiceModuleBase> it;
        ContentValues lastSwitch;
        Integer rcs;
        Iterator<ServiceModuleBase> it2;
        int i = phoneId;
        ContentValues snapShot = DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) null, i);
        this.mChangingServiceModulesStateLock.lock();
        try {
            IMSLog.d(LOG_TAG, i, "onImsSwitchUpdated " + snapShot + ", old " + this.mLastImsServiceSwitches.get(Integer.valueOf(phoneId)));
            boolean isCmcActivated = this.mImsFramework.getCmcAccountManager().isCmcDeviceActivated();
            Iterator<ServiceModuleBase> it3 = this.mServiceModules.iterator();
            while (it3.hasNext()) {
                ServiceModuleBase module = it3.next();
                if (module.isRunning()) {
                    boolean needStop = true;
                    boolean needRemoveFeature = true;
                    String[] servicesRequiring = module.getServicesRequiring();
                    int length = servicesRequiring.length;
                    int i2 = 0;
                    while (i2 < length) {
                        String service = servicesRequiring[i2];
                        Integer snapService = snapShot.getAsInteger(service);
                        if (snapService != null) {
                            it2 = it3;
                            if (snapService.intValue() == 1 && DmConfigHelper.readSwitch(this.mContext, service, true, i)) {
                                needStop = false;
                                needRemoveFeature = false;
                            }
                        } else {
                            it2 = it3;
                        }
                        if (module.getName().equals(VolteServiceModule.NAME) && isCmcActivated) {
                            Log.d(LOG_TAG, "onImsSwitchUpdated: CMC device: " + module.getName() + " module.");
                            needStop = false;
                            needRemoveFeature = false;
                        }
                        Iterator<ISimManager> it4 = this.mSimManagers.iterator();
                        while (it4.hasNext()) {
                            ISimManager sm = it4.next();
                            boolean isCmcActivated2 = isCmcActivated;
                            if (sm.getSimSlotIndex() == i) {
                                isCmcActivated = isCmcActivated2;
                            } else {
                                Iterator<ISimManager> it5 = it4;
                                ContentValues lastSwitch2 = this.mLastImsServiceSwitches.get(Integer.valueOf(sm.getSimSlotIndex()));
                                if (lastSwitch2 == null || lastSwitch2.size() <= 0) {
                                } else {
                                    Integer lastService = lastSwitch2.getAsInteger(service);
                                    if (lastService != null) {
                                        ContentValues contentValues = lastSwitch2;
                                        Integer num = lastService;
                                        if (lastService.intValue() == 1) {
                                            Log.d(LOG_TAG, "onImsSwitchUpdated: opposite sim slot enabled " + module.getName() + " module.");
                                            needStop = false;
                                        }
                                    } else {
                                        Integer num2 = lastService;
                                    }
                                }
                                isCmcActivated = isCmcActivated2;
                                it4 = it5;
                            }
                        }
                        i2++;
                        it3 = it2;
                    }
                    boolean isCmcActivated3 = isCmcActivated;
                    Iterator<ServiceModuleBase> it6 = it3;
                    if (needRemoveFeature) {
                        Log.d(LOG_TAG, "onImsSwitchUpdated: Configuring " + module.getName() + " module.");
                        module.onConfigured(i);
                    }
                    if (needStop) {
                        Log.d(LOG_TAG, "onImsSwitchUpdated: Stopping " + module.getName() + " module.");
                        module.stop();
                    }
                    it3 = it6;
                    isCmcActivated = isCmcActivated3;
                }
            }
            IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
            List<ServiceModuleBase> modulesToStart = new ArrayList<>();
            for (ServiceModuleBase module2 : this.mServiceModules) {
                for (String service2 : module2.getServicesRequiring()) {
                    Integer snapService2 = snapShot.getAsInteger(service2);
                    if ((module2.isStopped() || module2.isReady()) && snapService2 != null && snapService2.intValue() == 1 && DmConfigHelper.readBool(this.mContext, service2, true, i).booleanValue()) {
                        Log.d(LOG_TAG, "Starting " + module2.getName() + " module");
                        module2.start();
                        modulesToStart.add(module2);
                    }
                }
            }
            Integer rcs2 = snapShot.getAsInteger(DeviceConfigManager.RCS);
            if ((this.mAutoConfigCompletedList.containsKey(Integer.valueOf(phoneId)) && this.mAutoConfigCompletedList.get(Integer.valueOf(phoneId)).booleanValue()) || !((rcs2 == null || rcs2.intValue() == 1) && i == SimUtil.getSimSlotPriority())) {
                for (ServiceModuleBase module3 : modulesToStart) {
                    module3.onConfigured(i);
                }
            }
            ContentValues lastSwitch3 = this.mLastImsServiceSwitches.get(Integer.valueOf(phoneId));
            if (lastSwitch3 != null) {
                Iterator<ServiceModuleBase> it7 = this.mServiceModules.iterator();
                while (it7.hasNext()) {
                    ServiceModuleBase module4 = it7.next();
                    Set<String> switchedServices = new ArraySet<>();
                    String[] servicesRequiring2 = module4.getServicesRequiring();
                    int length2 = servicesRequiring2.length;
                    int i3 = 0;
                    while (i3 < length2) {
                        String service3 = servicesRequiring2[i3];
                        Integer snapService3 = snapShot.getAsInteger(service3);
                        Integer lastService2 = lastSwitch3.getAsInteger(service3);
                        if (snapService3 == null) {
                            rcs = rcs2;
                            lastSwitch = lastSwitch3;
                            it = it7;
                        } else if (lastService2 == null) {
                            rcs = rcs2;
                            lastSwitch = lastSwitch3;
                            it = it7;
                        } else {
                            rcs = rcs2;
                            lastSwitch = lastSwitch3;
                            it = it7;
                            if ((snapService3.intValue() == 1) != (lastService2.intValue() == 1)) {
                                switchedServices.add(service3);
                            }
                            i3++;
                            rcs2 = rcs;
                            lastSwitch3 = lastSwitch;
                            it7 = it;
                        }
                        Log.d(LOG_TAG, "Unknown switch value : " + service3);
                        i3++;
                        rcs2 = rcs;
                        lastSwitch3 = lastSwitch;
                        it7 = it;
                    }
                    Integer rcs3 = rcs2;
                    ContentValues lastSwitch4 = lastSwitch3;
                    Iterator<ServiceModuleBase> it8 = it7;
                    if (!switchedServices.isEmpty()) {
                        Log.d(LOG_TAG, "onImsSwitchUpdated: switchedServices " + switchedServices);
                        module4.onServiceSwitched(i, snapShot);
                    }
                    rcs2 = rcs3;
                    lastSwitch3 = lastSwitch4;
                    it7 = it8;
                }
                ContentValues contentValues2 = lastSwitch3;
            } else {
                ContentValues contentValues3 = lastSwitch3;
            }
            this.mLastImsServiceSwitches.put(Integer.valueOf(phoneId), snapShot);
            for (ServiceModuleBase module5 : modulesToStart) {
                module5.onNetworkChanged(registrationManager.getNetworkEvent(i), i);
                for (ImsRegistration reg : registrationManager.getRegistrationInfo()) {
                    if (needRegistrationNotification(module5, reg.getServices())) {
                        module5.onRegistered(reg);
                    }
                }
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    private void onSimReady(int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "ServiceModuleManager : onSimReady");
        for (ServiceModuleBase module : this.mServiceModules) {
            if (module.isRunning()) {
                module.onSimReady(phoneId);
            }
        }
    }

    public void handleMessage(Message msg) {
        Log.d(LOG_TAG, "handleMessage: evt=" + msg.what);
        int i = msg.what;
        if (i == 1) {
            onImsSwitchUpdated(UriUtil.getSimSlotFromUri((Uri) msg.obj));
        } else if (i == 2) {
            notifyConfigured(true, msg.arg1);
        } else if (i == 3) {
            Log.d(LOG_TAG, "ON SIM READY");
            onSimReady(msg.arg1);
        }
    }

    public void cleanUpModules() {
        this.mChangingServiceModulesStateLock.lock();
        try {
            for (ServiceModuleBase module : this.mServiceModules) {
                if (module.isRunning()) {
                    module.cleanUp();
                }
            }
        } finally {
            this.mChangingServiceModulesStateLock.unlock();
        }
    }

    public void updateCapabilities(int phoneId) {
        if (!this.mStarted) {
            Log.e(LOG_TAG, "Can't update capabilities before Service module started!");
            if (this.mPendingCapabilitiesUpdate == null) {
                this.mPendingCapabilitiesUpdate = new boolean[SimUtil.getPhoneCount()];
            }
            this.mPendingCapabilitiesUpdate[phoneId] = true;
            return;
        }
        ImsFeature.Capabilities capabilities = new ImsFeature.Capabilities();
        for (ServiceModuleBase module : this.mServiceModules) {
            capabilities.addCapabilities(module.queryCapabilityStatus(phoneId).getMask());
        }
        Log.d(LOG_TAG, "updateCapabilities to " + capabilities);
        this.mImsFramework.getGoogleImsAdaptor().updateCapabilities(phoneId, capabilities);
    }

    private class SimEventListener implements ISimEventListener {
        private SimEventListener() {
        }

        public void onReady(int phoneId, boolean absent) {
            int simState = TelephonyManagerWrapper.getInstance(ServiceModuleManager.this.mContext).getSimState(phoneId);
            Log.d(ServiceModuleManager.LOG_TAG, "onReady: phoneId=" + phoneId + " absent=" + absent + "SIM state=" + simState);
            if (simState == 5) {
                ServiceModuleManager serviceModuleManager = ServiceModuleManager.this;
                serviceModuleManager.sendMessage(serviceModuleManager.obtainMessage(3, phoneId, 0, (Object) null));
            }
        }
    }

    private List<String> getExtendedServices(int phoneId) {
        String servicesFromGs = ImsRegistry.getString(phoneId, GlobalSettingsConstants.Registration.EXTENDED_SERVICES, "");
        List<String> services = new ArrayList<>();
        if (servicesFromGs != null) {
            for (String service : servicesFromGs.split(",")) {
                services.add(service);
            }
        }
        return services;
    }

    private boolean isStartRequired(ServiceModuleBase module, int phoneId, ISimManager sm) {
        String[] servicesRequiring = module.getServicesRequiring();
        int length = servicesRequiring.length;
        int i = 0;
        while (i < length) {
            String service = servicesRequiring[i];
            boolean isDmOn = DmConfigHelper.readSwitch(this.mContext, service, true, phoneId);
            if (service.equalsIgnoreCase("mmtel") && sm != null && sm.getSimMno() == Mno.SPRINT) {
                isDmOn |= DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_VWF_ENABLED, false, phoneId).booleanValue();
            }
            if (DmConfigHelper.getImsSwitchValue(this.mContext, Arrays.asList(ImsProfile.getRcsServiceList()).contains(service) ? DeviceConfigManager.RCS_SWITCH : service, phoneId) != 1 || !isDmOn || module.isRunning()) {
                i++;
            } else {
                Log.d(LOG_TAG, "isStartRequired: start " + module.getName() + " module");
                return true;
            }
        }
        return false;
    }
}
