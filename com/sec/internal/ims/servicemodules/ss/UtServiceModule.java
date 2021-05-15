package com.sec.internal.ims.servicemodules.ss;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CellLocation;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.ss.IImsUtEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.httpclient.GbaHttpController;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.log.IMSLog;
import com.sec.vsim.attsoftphone.ISoftphoneService;
import com.sec.vsim.attsoftphone.ISupplementaryServiceListener;
import com.sec.vsim.attsoftphone.data.CallForwardingInfo;
import com.sec.vsim.attsoftphone.data.CallWaitingInfo;
import com.sec.vsim.attsoftphone.data.SupplementaryServiceNotify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtServiceModule extends ServiceModuleBase implements IUtServiceModule {
    private static final int EVENT_SIM_READY = 4;
    private static final int EVENT_SOFTPHONE_RESPONSE = 5;
    private static final int EVENT_TRIGGER_CONFIG = 1;
    private static final String LOG_TAG = "UtServiceModule";
    private static final int MAX_RETRY_SIM_SERV_DOC = 3;
    public static final String NAME = UtServiceModule.class.getSimpleName();
    private static final int XCAP_ROOT_URI_PREF_IMSI = 2;
    private static final int XCAP_ROOT_URI_PREF_MSISDN = 1;
    private static final int XCAP_ROOT_URI_PREF_MSISDN_WITH_DOMAIN = 3;
    private static int mUtIdCounter = 0;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            int airPlaneModeOn = Settings.Global.getInt(UtServiceModule.this.mContext.getContentResolver(), "airplane_mode_on", 0);
            IMSLog.i(UtServiceModule.LOG_TAG, "AirplaneModeObserver onChange: " + airPlaneModeOn);
            if (airPlaneModeOn == 0) {
                GbaHttpController.getInstance().clearLastAuthInfo();
            }
            for (int i = 0; i < UtServiceModule.this.smUtMap.size(); i++) {
                ((UtStateMachine) UtServiceModule.this.smUtMap.get(Integer.valueOf(i))).onAirplaneModeChanged(airPlaneModeOn);
                if (UtServiceModule.this.mGetSrvDocAfterFlightModeOff[i]) {
                    UtServiceModule.this.mGetSrvDocAfterFlightModeOff[i] = false;
                    if (UtServiceModule.this.needToGetSimservDocOnBootup(i)) {
                        UtServiceModule.this.querySimServDoc(i);
                    }
                }
            }
        }
    };
    protected SparseArray<CWDBContentObserver> mCWDBChangeObserver;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMSLog.i(UtServiceModule.LOG_TAG, "connected");
            ISoftphoneService unused = UtServiceModule.this.mSoftphoneService = ISoftphoneService.Stub.asInterface(service);
            UtServiceModule.this.connected();
            boolean unused2 = UtServiceModule.this.mSoftphoneBound = true;
            IMSLog.i(UtServiceModule.LOG_TAG, "Softphone Service bind" + UtServiceModule.this.mSoftphoneBound);
        }

        public void onServiceDisconnected(ComponentName name) {
            IMSLog.i(UtServiceModule.LOG_TAG, "Disconnected");
            ISoftphoneService unused = UtServiceModule.this.mSoftphoneService = null;
        }
    };
    protected Context mContext = null;
    /* access modifiers changed from: private */
    public boolean[] mGetSrvDocAfterFlightModeOff = new boolean[SimUtil.getPhoneCount()];
    protected final IImsFramework mImsFramework;
    /* access modifiers changed from: private */
    public boolean[] mIsConfigured = {false, false};
    private boolean[] mIsEpdgAvailable = new boolean[SimUtil.getPhoneCount()];
    /* access modifiers changed from: private */
    public boolean mIsRunningRequest = false;
    private Map<String, ImsUri> mLastUri = new HashMap();
    private Map<Integer, IImsUtEventListener> mListeners = new ArrayMap();
    protected final Looper mLooper;
    ContentObserver mMnoUpdateObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange, Uri uri) {
            int phoneId = UriUtil.getSimSlotFromUri(uri);
            UtServiceModule.this.mIsConfigured[phoneId] = false;
            IMSLog.i(UtServiceModule.LOG_TAG, phoneId, "Loaded Config Data");
            UtServiceModule.this.removeMessages(1, Integer.valueOf(phoneId));
            UtServiceModule utServiceModule = UtServiceModule.this;
            utServiceModule.sendMessage(utServiceModule.obtainMessage(1, Integer.valueOf(phoneId)));
        }
    };
    private final NetworkStateListener mNetworkStateListener = new NetworkStateListener() {
        public void onDataConnectionStateChanged(int networkType, boolean isWifiConnected, int phoneId) {
            if (UtServiceModule.this.mPdnController.getMobileDataRegState(phoneId) == 0 && UtServiceModule.this.needToGetSimservDocOnBootup(phoneId)) {
                UtServiceModule.this.querySimServDoc(phoneId);
            }
        }

        public void onCellLocationChanged(CellLocation location, int phoneId) {
        }

        public void onEpdgConnected(int phoneId) {
            if (UtServiceModule.this.needToGetSimservDocOnBootup(phoneId)) {
                IMSLog.i(UtServiceModule.LOG_TAG, phoneId, "onEpdgConnected");
                UtServiceModule.this.querySimServDoc(phoneId);
            }
        }

        public void onEpdgDisconnected(int phoneId) {
        }

        public void onEpdgRegisterRequested(int phoneId, boolean cdmaAvailability) {
        }

        public void onEpdgDeregisterRequested(int phoneId) {
        }

        public void onIKEAuthFAilure(int phoneId) {
        }

        public void onEpdgIpsecDisconnected(int phoneId) {
        }

        public void onDefaultNetworkStateChanged(int phoneId) {
        }

        public void onPreciseDataConnectionStateChanged(int phoneId, PreciseDataConnectionState state) {
            if (state != null && state.getDataConnectionState() == -1) {
                ((UtStateMachine) UtServiceModule.this.smUtMap.get(Integer.valueOf(phoneId))).handlePdnFail(state);
            }
        }
    };
    /* access modifiers changed from: private */
    public IPdnController mPdnController = null;
    /* access modifiers changed from: private */
    public List<UtProfile> mPendingRequests;
    private final IImsRegistrationListener mRegistrationListener = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration reg) {
            int phoneId = reg.getPhoneId();
            Mno mno = SimUtil.getSimMno(phoneId);
            if (reg.hasVolteService() && UtServiceModule.this.getimpi(phoneId) != null) {
                if (mno == Mno.ATT) {
                    UtServiceModule utServiceModule = UtServiceModule.this;
                    utServiceModule.setLastUri(utServiceModule.getimpi(phoneId), UtServiceModule.this.getImpuOfType(reg));
                } else {
                    UtServiceModule utServiceModule2 = UtServiceModule.this;
                    utServiceModule2.setLastUri(utServiceModule2.getimpi(phoneId), reg.getPreferredImpu().getUri());
                }
            }
            if (reg.getImsProfile().isSoftphoneEnabled()) {
                UtServiceModule.this.bindSoftPhoneService();
                ((UtStateMachine) UtServiceModule.this.smUtMap.get(Integer.valueOf(phoneId))).updateConfig(UtServiceModule.this.makeConfig(phoneId), UtServiceModule.this.makeFeature(phoneId));
                UtServiceModule.this.updateCapabilities(phoneId);
            }
        }

        public void onDeregistered(ImsRegistration reg, ImsRegistrationError errorCode) {
            if (reg.getImsProfile().isSoftphoneEnabled()) {
                UtServiceModule.this.unbindSoftPhoneService();
            }
        }
    };
    private int[] mSentSimServDocCount = {0, 0};
    private String mSoftPhoneAccountId = null;
    private int mSoftPhoneClientId = -1;
    private UtProfile mSoftProfile = null;
    /* access modifiers changed from: private */
    public boolean mSoftphoneBound = false;
    /* access modifiers changed from: private */
    public ISoftphoneService mSoftphoneService = null;
    private final ISupplementaryServiceListener mSupplementaryServiceListener = new ISupplementaryServiceListener.Stub() {
        public void onNotify(SupplementaryServiceNotify noti) {
            IMSLog.i(UtServiceModule.LOG_TAG, "Receive notify for Request ID: " + noti.mRequestId);
            switch (noti.mRequestId) {
                case 8:
                case 9:
                case 10:
                case 11:
                    UtServiceModule utServiceModule = UtServiceModule.this;
                    utServiceModule.sendMessage(utServiceModule.obtainMessage(5, noti.mRequestId, 0, noti));
                    break;
                default:
                    Log.e(UtServiceModule.LOG_TAG, "Unknown request ID: " + noti.mRequestId);
                    break;
            }
            if (!UtServiceModule.this.mPendingRequests.isEmpty()) {
                IMSLog.i(UtServiceModule.LOG_TAG, "Process next request...");
                UtServiceModule utServiceModule2 = UtServiceModule.this;
                utServiceModule2.processSpUtRequest(utServiceModule2.dequeueRequest());
                return;
            }
            boolean unused = UtServiceModule.this.mIsRunningRequest = false;
        }
    };
    private ITelephonyManager mTelephonyManager;
    private SimpleEventLog mUtServiceHistory;
    private boolean[] mUtSwitch = {true, true};
    /* access modifiers changed from: private */
    public HashMap<Integer, UtStateMachine> smUtMap = new HashMap<>();

    public UtServiceModule(Looper looper, Context context, IImsFramework imsFramework) {
        super(looper);
        this.mLooper = looper;
        this.mContext = context;
        this.mUtServiceHistory = new SimpleEventLog(context, LOG_TAG, 300);
        this.mImsFramework = imsFramework;
        for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
            sm.registerForSimReady(this, 4, (Object) null);
        }
        this.mUtServiceHistory.add("Create UtServiceModule");
    }

    public void init() {
        super.init();
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mPdnController = this.mImsFramework.getPdnController();
        int phoneCnt = this.mTelephonyManager.getPhoneCount();
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        this.mIsRunningRequest = false;
        for (int i = 0; i < phoneCnt; i++) {
            UtStateMachine utSm = new UtStateMachine("UtMachine" + i, this.mLooper, this, this.mImsFramework, this.mContext);
            utSm.init(i);
            utSm.start();
            this.smUtMap.put(Integer.valueOf(i), utSm);
            this.mIsEpdgAvailable[i] = false;
            rm.registerListener(this.mRegistrationListener, i);
            sendMessage(obtainMessage(1, Integer.valueOf(i)));
        }
        this.mPendingRequests = new ArrayList();
        this.mCWDBChangeObserver = new SparseArray<>();
        registerObserver();
        registerAirplaneModeObserver();
        this.mPdnController.registerForNetworkState(this.mNetworkStateListener);
    }

    /* access modifiers changed from: protected */
    public UtConfigData makeConfig(int phoneId) {
        UtConfigData config = new UtConfigData();
        config.username = getSetting(phoneId, GlobalSettingsConstants.SS.HTTP_USERNAME, "");
        config.passwd = getSetting(phoneId, GlobalSettingsConstants.SS.HTTP_PASSWORD, "");
        int xcapUriPref = getSetting(phoneId, GlobalSettingsConstants.SS.XCAP_ROOT_URI_PREF, 2);
        if (xcapUriPref == 1 || xcapUriPref == 2 || xcapUriPref == 3) {
            config.nafServer = UtUtils.getNAFDomain(this.mContext, phoneId);
            config.bsfServer = UtUtils.getBSFDomain(this.mContext, phoneId);
        } else {
            config.nafServer = getSetting(phoneId, GlobalSettingsConstants.SS.AUTH_PROXY_IP, "");
            config.bsfServer = getSetting(phoneId, GlobalSettingsConstants.SS.BSF_IP, "");
        }
        config.nafPort = getSetting(phoneId, GlobalSettingsConstants.SS.AUTH_PROXY_PORT, 80);
        config.impu = getPublicId(phoneId);
        config.bsfPort = getSetting(phoneId, GlobalSettingsConstants.SS.BSF_PORT, 80);
        config.userAgent = getSetting(phoneId, GlobalSettingsConstants.Registration.USER_AGENT, "");
        config.xcapRootUri = getSetting(phoneId, GlobalSettingsConstants.SS.XCAP_ROOT_URI, "");
        config.xdmUserAgent = getSetting(phoneId, GlobalSettingsConstants.SS.XDM_USER_AGENT, "");
        config.apnSelection = getSetting(phoneId, GlobalSettingsConstants.SS.APN_SELECTION, "xcap");
        String pdaVer = SemSystemProperties.get("ro.build.PDA");
        if (pdaVer != null && pdaVer.length() > 8) {
            config.xdmUserAgent = config.xdmUserAgent.replace("[BUILD_VERSION_8_LETTER]", pdaVer.substring(pdaVer.length() - 8, pdaVer.length()));
        }
        if (NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(Build.MODEL)) {
            config.xdmUserAgent = config.xdmUserAgent.replace("[PRODUCT_MODEL]", SemSystemProperties.get("ro.product.base_model"));
        } else {
            config.xdmUserAgent = config.xdmUserAgent.replace("[PRODUCT_MODEL]", Build.MODEL);
        }
        String policy = getSetting(phoneId, GlobalSettingsConstants.SS.DOMAIN, "CS");
        if (policy.equalsIgnoreCase("CS") || policy.equalsIgnoreCase("CS_ALWAYS")) {
            registerCwdbObserver(phoneId);
        } else {
            unregisterCwdbObserver(phoneId);
        }
        IMSLog.s(LOG_TAG, phoneId, "makeConfig " + IMSLog.checker(getPublicId(phoneId)));
        return config;
    }

    /* access modifiers changed from: protected */
    public UtFeatureData makeFeature(int phoneId) {
        UtFeatureData feature = new UtFeatureData();
        feature.support_tls = getSetting(phoneId, GlobalSettingsConstants.SS.SUPPORT_TLS, false);
        feature.isCFSingleElement = getSetting(phoneId, GlobalSettingsConstants.SS.SELECT_MODE, true);
        feature.isCBSingleElement = getSetting(phoneId, GlobalSettingsConstants.SS.CB_SELECT_MODE, false);
        feature.support_media = getSetting(phoneId, GlobalSettingsConstants.SS.MEDIA_TYPE, 255) != 255;
        feature.support_ss = getSetting(phoneId, GlobalSettingsConstants.SS.SUPPORT_SS_ELEMENT, false);
        feature.supportSimservsRetry = getSetting(phoneId, GlobalSettingsConstants.SS.SUPPORT_SIMSERVS_RETRY, true);
        feature.cfb = getSetting(phoneId, GlobalSettingsConstants.SS.CF_BUSY_RULEID, "call-diversion-busy-audio");
        feature.cfu = getSetting(phoneId, GlobalSettingsConstants.SS.CF_UNCONDITIONAL_RULEID, "call-diversion-unconditional");
        feature.cfnr = getSetting(phoneId, GlobalSettingsConstants.SS.CF_NO_ANSWER_RULEID, "call-diversion-no-reply");
        feature.cfnrc = getSetting(phoneId, GlobalSettingsConstants.SS.CF_NOT_REACHABLE_RULEID, "call-diversion-not-reachable-audio");
        feature.cfni = getSetting(phoneId, GlobalSettingsConstants.SS.CF_NOT_LOGGED_IN_RULEID, "call-diversion-not-logged-in");
        feature.cbbaic = getSetting(phoneId, GlobalSettingsConstants.SS.ICB_UNCONDITIONAL_RULEID, "");
        feature.cbbicwr = getSetting(phoneId, GlobalSettingsConstants.SS.ICB_ROAMING_RULEID, "");
        feature.isBlockUntilReboot = getSetting(phoneId, GlobalSettingsConstants.SS.ERROR403_CSFB_UNTIL_REBOOT, true);
        feature.isNeedSeparateCFNL = getSetting(phoneId, GlobalSettingsConstants.SS.NEED_SEPERATE_CFNL, true);
        feature.isNeedSeparateCFNRY = getSetting(phoneId, GlobalSettingsConstants.SS.NEED_SEPERATE_CFNRY, true);
        feature.isNeedSeparateCFA = getSetting(phoneId, GlobalSettingsConstants.SS.NEED_SEPERATE_CFA, false);
        feature.isNeedFirstGet = getSetting(phoneId, GlobalSettingsConstants.SS.IS_NEED_GET_FIRST, true);
        feature.isErrorMsgDisplay = getSetting(phoneId, GlobalSettingsConstants.SS.ERROR_MSG_DISPLAY, false);
        feature.isDisconnectXcapPdn = getSetting(phoneId, GlobalSettingsConstants.SS.DISCONNECT_XCAP_PDN, true);
        feature.delay_disconnect_pdn = getSetting(phoneId, GlobalSettingsConstants.SS.DELAY_DISCONNECT_XCAP_PDN, 5) * 1000;
        feature.isCsfbWithImserror = getSetting(phoneId, GlobalSettingsConstants.SS.CSFB_WITH_IMSERROR, true);
        feature.ip_version = UtUtils.doConvertIpVersion(getSetting(phoneId, GlobalSettingsConstants.SS.SELECT_IP_VERSION, "default"));
        feature.insertNewRule = getSetting(phoneId, GlobalSettingsConstants.SS.INSERT_NEW_RULE, true);
        feature.noMediaForCB = getSetting(phoneId, GlobalSettingsConstants.SS.NO_MEDIA_FOR_CB, false);
        feature.setAllMediaCF = getSetting(phoneId, GlobalSettingsConstants.SS.CF_SET_ALL_MEDIA, false);
        feature.cfUriType = getSetting(phoneId, GlobalSettingsConstants.SS.CF_URI_TYPE, "SIP");
        feature.supportAlternativeMediaForCb = getSetting(phoneId, GlobalSettingsConstants.SS.SUPPORT_ALTERNATIVE_MEDIA_FOR_CB, false);
        Mno mno = SimUtil.getSimMno(phoneId);
        String salesCode = OmcCode.get();
        if (mno == Mno.GCF && "CHM".equalsIgnoreCase(salesCode)) {
            feature.isNeedSeparateCFNRY = false;
            feature.isNeedSeparateCFNL = false;
        }
        return feature;
    }

    public Context getContext() {
        return this.mContext;
    }

    public int querySimServDoc(int phoneId) {
        int rId = createRequestId();
        int[] iArr = this.mSentSimServDocCount;
        iArr[phoneId] = iArr[phoneId] + 1;
        UtProfile profile = new UtProfile(116, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int queryCallWaiting(int phoneId) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(114, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int queryCallBarring(int phoneId, int cbType, int ssClass) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(UtUtils.doconvertCBType(false, cbType), cbType, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int queryCallForward(int phoneId, int condition, String number) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(100, condition, number, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int queryCLIR(int phoneId) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(108, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int queryCLIP(int phoneId) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(106, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int queryCOLR(int phoneId) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(112, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int queryCOLP(int phoneId) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(110, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int updateCallBarring(int phoneId, int cbType, int action, int ssClass, String password, String[] barrList) {
        int cbType2;
        int i;
        int i2 = phoneId;
        int rId = createRequestId();
        if (SimUtil.getSimMno(phoneId) == Mno.CMCC) {
            i = cbType;
            if (i == 7) {
                cbType2 = 1;
                UtProfile profile = new UtProfile(UtUtils.doconvertCBType(true, cbType2), cbType2, action, ssClass, barrList, rId, password);
                printLog(phoneId, profile);
                startUtRequest(phoneId, profile);
                return rId;
            }
        } else {
            i = cbType;
        }
        cbType2 = i;
        UtProfile profile2 = new UtProfile(UtUtils.doconvertCBType(true, cbType2), cbType2, action, ssClass, barrList, rId, password);
        printLog(phoneId, profile2);
        startUtRequest(phoneId, profile2);
        return rId;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x006b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int updateCallForward(int r17, int r18, int r19, java.lang.String r20, int r21, int r22) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            r2 = r20
            int r10 = r16.createRequestId()
            com.sec.internal.constants.Mno r11 = com.sec.internal.helper.SimUtil.getSimMno(r17)
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TELSTRA
            if (r11 == r3) goto L_0x001a
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.SINGTEL
            if (r11 != r3) goto L_0x0017
            goto L_0x001a
        L_0x0017:
            r12 = r19
            goto L_0x0025
        L_0x001a:
            r3 = 2
            r12 = r19
            if (r12 != r3) goto L_0x0025
            if (r22 > 0) goto L_0x0025
            r3 = 20
            r13 = r3
            goto L_0x0027
        L_0x0025:
            r13 = r22
        L_0x0027:
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.VODAFONE_AUSTRALIA
            if (r11 != r3) goto L_0x006b
            boolean r3 = android.text.TextUtils.isEmpty(r20)
            if (r3 != 0) goto L_0x006b
            java.lang.String r3 = "+"
            boolean r3 = r2.startsWith(r3)
            if (r3 != 0) goto L_0x006b
            r3 = 0
            char r3 = r2.charAt(r3)
            r4 = 48
            java.lang.String r5 = "+61"
            if (r3 != r4) goto L_0x005a
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r5)
            r4 = 1
            java.lang.String r4 = r2.substring(r4)
            r3.append(r4)
            java.lang.String r2 = r3.toString()
            r14 = r2
            goto L_0x006c
        L_0x005a:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r5)
            r3.append(r2)
            java.lang.String r2 = r3.toString()
            r14 = r2
            goto L_0x006c
        L_0x006b:
            r14 = r2
        L_0x006c:
            com.sec.internal.ims.servicemodules.ss.UtProfile r15 = new com.sec.internal.ims.servicemodules.ss.UtProfile
            r3 = 101(0x65, float:1.42E-43)
            r2 = r15
            r4 = r18
            r5 = r19
            r6 = r14
            r7 = r21
            r8 = r13
            r9 = r10
            r2.<init>((int) r3, (int) r4, (int) r5, (java.lang.String) r6, (int) r7, (int) r8, (int) r9)
            r0.printLog(r1, r2)
            r0.startUtRequest(r1, r2)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtServiceModule.updateCallForward(int, int, int, java.lang.String, int, int):int");
    }

    public int updateCallWaiting(int phoneId, boolean enable, int serviceClass) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(115, enable, serviceClass, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int updateCLIR(int phoneId, int clirMode) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(109, clirMode, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int updateCLIP(int phoneId, boolean enable) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(107, enable, rId);
        printLog(phoneId, profile);
        startUtRequest(phoneId, profile);
        return rId;
    }

    public int updateCOLR(int phoneId, int presentation) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(113, presentation, rId);
        printLog(phoneId, profile);
        this.smUtMap.get(Integer.valueOf(phoneId)).query(profile);
        return rId;
    }

    public int updateCOLP(int phoneId, boolean enable) {
        int rId = createRequestId();
        UtProfile profile = new UtProfile(111, enable, rId);
        printLog(phoneId, profile);
        this.smUtMap.get(Integer.valueOf(phoneId)).query(profile);
        return rId;
    }

    /* access modifiers changed from: protected */
    public ImsUri getImpuOfType(ImsRegistration reg) {
        if (reg == null) {
            return null;
        }
        for (NameAddr addr : reg.getImpuList()) {
            if (addr.getUri().getUriType() == ImsUri.UriType.TEL_URI) {
                int phoneId = reg.getPhoneId();
                IMSLog.i(LOG_TAG, phoneId, "getPublicId for ATT : registered IMPU = " + IMSLog.checker(addr.getUri().toString()));
                return addr.getUri();
            }
        }
        return reg.getPreferredImpu().getUri();
    }

    private void startUtRequest(int phoneId, UtProfile profile) {
        UtStateMachine utsmc = this.smUtMap.get(Integer.valueOf(phoneId));
        int phoneCnt = this.mTelephonyManager.getPhoneCount();
        for (int i = 0; i < phoneCnt; i++) {
            if (i != phoneId && this.smUtMap.get(Integer.valueOf(i)).hasConnection()) {
                IMSLog.i(LOG_TAG, phoneId, "already connected on another slot");
                this.smUtMap.get(Integer.valueOf(i)).disconnectPdn();
            }
        }
        IUserAgent ua = getUa(phoneId);
        if (ua == null || !ua.getImsProfile().isSoftphoneEnabled() || isTerminalRequest(phoneId, profile)) {
            utsmc.query(profile);
            return;
        }
        enqueueRequest(profile);
        if (this.mIsRunningRequest) {
            IMSLog.i(LOG_TAG, phoneId, "Other request is processing now...");
        } else {
            processSpUtRequest(profile);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isTerminalRequest(int phoneId, UtProfile profile) {
        int clipClir = this.mImsFramework.getInt(phoneId, GlobalSettingsConstants.SS.CLIP_CLIR_BY_NETWORK, 0);
        int cb = this.mImsFramework.getInt(phoneId, GlobalSettingsConstants.SS.CALLBARRING_BY_NETWORK, 1);
        int cw = this.mImsFramework.getInt(phoneId, GlobalSettingsConstants.SS.CALLWAITING_BY_NETWORK, 0);
        if (UtUtils.isCallBarringType(profile.type)) {
            if (cb == 0) {
                return true;
            }
            return false;
        } else if (profile.type == 114 || profile.type == 115) {
            if (cw == 0) {
                return true;
            }
            return false;
        } else if (profile.type < 106 || profile.type > 109 || clipClir != 0) {
            return false;
        } else {
            return true;
        }
    }

    public int checkAvailabilityError(int phoneId) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null || !sm.isSimAvailable()) {
            IMSLog.i(LOG_TAG, phoneId, "isRequestAvailable(): Request not available because SIM is not ready");
            return 1004;
        } else if (SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) && phoneId != SimUtil.getDefaultPhoneId() && sm.getSimMno() == Mno.RJIL) {
            IMSLog.i(LOG_TAG, phoneId, "do not trigger XCAP for non dds sim");
            return 1005;
        } else if (ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, sm.getSimMnoName(), phoneId).isEmpty()) {
            if (sm.hasVsim() && this.mSoftphoneBound) {
                return 0;
            }
            IMSLog.i(LOG_TAG, phoneId, "isRequestAvailable(): no matched profile with SIM");
            return 1006;
        } else if (this.mIsConfigured[phoneId]) {
            return 0;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "isRequestAvailable(): UtStateMachine is not configured");
            return 1007;
        }
    }

    public boolean isInvalidUtRequest(int phoneId, UtProfile profile) {
        if (profile.type != 101 || profile.action != 3 || !TextUtils.isEmpty(profile.number)) {
            return false;
        }
        IMSLog.i(LOG_TAG, phoneId, "Invalid request - Registration should have number.");
        return true;
    }

    /* access modifiers changed from: protected */
    public IUserAgent getUa(int phoneId) {
        return getUa(phoneId, 0);
    }

    private IUserAgent getUa(int phoneId, int cmcType) {
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        Mno mno = SimUtil.getSimMno(phoneId);
        IUserAgent[] uaList = rm.getUserAgentByPhoneId(phoneId, "mmtel");
        if (uaList.length != 0 && uaList[0] == null) {
            uaList = (mno == Mno.TELUS || mno == Mno.KOODO) ? rm.getUserAgentByPhoneId(phoneId, "smsip") : rm.getUserAgentByPhoneId(phoneId, "ss");
        }
        if (uaList.length == 0) {
            return null;
        }
        for (IUserAgent ua : uaList) {
            if (ua != null && ua.getImsProfile().getCmcType() == cmcType) {
                return ua;
            }
        }
        return uaList[0];
    }

    private String getUtImpi(int phoneId) {
        ImsProfile profile;
        IUserAgent ua = getUa(phoneId);
        if (ua == null || (profile = ua.getImsProfile()) == null) {
            return "";
        }
        return this.mImsFramework.getRegistrationManager().getImpi(profile, phoneId);
    }

    /* access modifiers changed from: protected */
    public String getimpi(int phoneId) {
        String impi = getUtImpi(phoneId);
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm != null) {
            if (TextUtils.isEmpty(impi) && sm.hasIsim()) {
                impi = sm.getImpi();
            }
            if (TextUtils.isEmpty(impi)) {
                impi = sm.getDerivedImpi();
            }
        }
        if (!TextUtils.isEmpty(impi)) {
            return impi;
        }
        IMSLog.e(LOG_TAG, phoneId, "There is no impi");
        return "";
    }

    /* access modifiers changed from: protected */
    public String getPublicId(int phoneId) {
        ImsUri impuUri;
        ImsUri impuUri2;
        String impu = null;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        Mno mno = sm == null ? Mno.DEFAULT : sm.getSimMno();
        IUserAgent ua = getUa(phoneId);
        if (ua != null) {
            ImsRegistration reg = ua.getImsRegistration();
            if (mno == Mno.ATT) {
                if (!(reg == null || (impuUri2 = getImpuOfType(reg)) == null)) {
                    return impuUri2.toString();
                }
            } else if (!(mno == Mno.TELSTRA || reg == null || (impuUri = reg.getPreferredImpu().getUri()) == null)) {
                return impuUri.toString();
            }
        }
        ImsRegistration reg2 = getimpi(phoneId);
        if (this.mLastUri.size() > 0 && !TextUtils.isEmpty(reg2) && mno != Mno.TELSTRA && this.mLastUri.containsKey(reg2) && this.mLastUri.get(reg2) != null) {
            return this.mLastUri.get(reg2).toString();
        }
        if (sm != null) {
            impu = sm.getImpuFromIsim(0);
            if (TextUtils.isEmpty(impu)) {
                impu = sm.getDerivedImpu();
            }
            if (mno == Mno.ROGERS && !TextUtils.isEmpty(sm.getMsisdn())) {
                impu = sm.getDerivedImpuFromMsisdn();
            }
        }
        if (TextUtils.isEmpty(impu)) {
            return "";
        }
        return impu;
    }

    private int createRequestId() {
        if (mUtIdCounter >= 255) {
            mUtIdCounter = 0;
        }
        int i = mUtIdCounter + 1;
        mUtIdCounter = i;
        return i;
    }

    public void handleIntent(Intent intent) {
    }

    public String[] getServicesRequiring() {
        return new String[]{"ss"};
    }

    public void onNetworkChanged(NetworkEvent event, int phoneId) {
        super.onNetworkChanged(event, phoneId);
        IMSLog.i(LOG_TAG, phoneId, "onNetworkChanged to " + event);
        if (event.isEpdgAvailable != this.mIsEpdgAvailable[phoneId]) {
            onEpdgAvailabilityChanged(event.isEpdgAvailable, phoneId);
        }
    }

    private void onEpdgAvailabilityChanged(boolean isEpdgAvailable, int phoneId) {
        this.mIsEpdgAvailable[phoneId] = isEpdgAvailable;
        if (SimUtil.getSimMno(phoneId) == Mno.KDDI && !this.mIsEpdgAvailable[phoneId]) {
            this.smUtMap.get(Integer.valueOf(phoneId)).handleEpdgAvailabilityChanged(this.mIsEpdgAvailable[phoneId]);
        }
    }

    public void handleMessage(Message msg) {
        IMSLog.i(LOG_TAG, "handleMessage " + msg.what);
        super.handleMessage(msg);
        int i = msg.what;
        if (i == 1) {
            updateConfig(msg);
        } else if (i == 4) {
            for (ISimManager sm : SimManagerFactory.getAllSimManagers()) {
                if ((sm == null ? Mno.DEFAULT : sm.getSimMno()).isChn()) {
                    GbaHttpController.getInstance().clearLastAuthInfo();
                    return;
                }
            }
        } else if (i == 5) {
            handleSoftPhoneEvent(msg);
        }
    }

    private void handleSoftPhoneEvent(Message msg) {
        SupplementaryServiceNotify response = (SupplementaryServiceNotify) msg.obj;
        switch (msg.arg1) {
            case 8:
                if (!response.mSuccess || response.mCallWaitingInfo == null) {
                    IMSLog.e(LOG_TAG, 0, "Unable to get CallWaitingInfo. reason = " + response.mReason);
                    Bundle error = new Bundle();
                    error.putInt("errorCode", 408);
                    notifyFailResult(0, this.mSoftProfile.type, this.mSoftProfile.requestId, error);
                    return;
                }
                IMSLog.i(LOG_TAG, "CallWaitingInfo: " + response.mCallWaitingInfo.mActive);
                Bundle[] result = {new Bundle()};
                result[0].putBoolean("status", response.mSuccess);
                notifySuccessResult(0, this.mSoftProfile.type, this.mSoftProfile.requestId, result);
                return;
            case 9:
                if (response.mSuccess) {
                    IMSLog.i(LOG_TAG, 0, "Success to get CallForwardingInfo");
                    Bundle[] callForwardList = new Bundle[1];
                    Bundle bundle = new Bundle();
                    for (CallForwardingInfo info : response.mCallForwardingInfos) {
                        if (this.mSoftProfile.condition == info.mForwardCondition) {
                            if (info.mActive) {
                                bundle.putInt("status", 1);
                            } else {
                                bundle.putInt("status", 0);
                            }
                            bundle.putInt("condition", this.mSoftProfile.condition);
                            bundle.putString("number", info.mForwardNumber);
                            bundle.putInt("serviceClass", 17);
                        }
                    }
                    callForwardList[0] = bundle;
                    notifySuccessResult(0, this.mSoftProfile.type, this.mSoftProfile.requestId, callForwardList);
                    return;
                }
                IMSLog.e(LOG_TAG, "Unable to get CallForwardingInfo. reason = " + response.mReason);
                Bundle error2 = new Bundle();
                error2.putInt("errorCode", 408);
                notifyFailResult(0, this.mSoftProfile.type, this.mSoftProfile.requestId, error2);
                return;
            case 10:
            case 11:
                if (response.mSuccess) {
                    IMSLog.i(LOG_TAG, "Success to set " + msg.what);
                    notifySuccessResult(0, this.mSoftProfile.type, this.mSoftProfile.requestId, (Bundle[]) null);
                    return;
                }
                IMSLog.e(LOG_TAG, "Unable to " + msg.arg1 + ". reason = " + response.mReason);
                Bundle error3 = new Bundle();
                error3.putInt("errorCode", 408);
                notifyFailResult(0, this.mSoftProfile.type, this.mSoftProfile.requestId, error3);
                return;
            default:
                IMSLog.e(LOG_TAG, "Unknown message type: " + msg.what);
                return;
        }
    }

    private void updateConfig(Message msg) {
        int phoneId = ((Integer) msg.obj).intValue();
        this.mGetSrvDocAfterFlightModeOff[phoneId] = false;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm != null) {
            if (sm.isSimAvailable()) {
                this.smUtMap.get(Integer.valueOf(phoneId)).updateConfig(makeConfig(phoneId), makeFeature(phoneId));
                this.mIsConfigured[phoneId] = true;
                updateCapabilities(phoneId);
                this.smUtMap.get(Integer.valueOf(phoneId)).setSentSimServ(false);
                this.mSentSimServDocCount[phoneId] = 0;
                this.mUtSwitch[phoneId] = true;
                if (!needToGetSimservDocOnBootup(phoneId)) {
                    return;
                }
                if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                    this.mGetSrvDocAfterFlightModeOff[phoneId] = true;
                } else {
                    querySimServDoc(phoneId);
                }
            } else if (!sm.hasNoSim() && !hasMessages(msg.what, msg.obj)) {
                sendMessageDelayed(obtainMessage(msg.what, msg.obj), 10000);
            }
        }
    }

    public boolean isUtEnabled(int phoneId) {
        if (!(!this.smUtMap.isEmpty() && this.smUtMap.get(Integer.valueOf(phoneId)) != null)) {
            IMSLog.e(LOG_TAG, phoneId, "UtStateMachine is not initialized yet");
            return false;
        } else if (!this.mUtSwitch[phoneId]) {
            IMSLog.e(LOG_TAG, phoneId, "UtService is disabled");
            return false;
        } else {
            Mno mno = SimUtil.getSimMno(phoneId);
            if (mno != Mno.GLOBE_PH || checkXcapApn(phoneId)) {
                boolean enabled = false;
                String policy = getSetting(phoneId, GlobalSettingsConstants.SS.DOMAIN, "CS");
                if ("PS".equalsIgnoreCase(policy) || "PS_ALWAYS".equalsIgnoreCase(policy)) {
                    enabled = true;
                } else if (DiagnosisConstants.PSCI_KEY_CALL_BEARER.equalsIgnoreCase(policy) || "PS_ONLY_VOLTEREGIED".equalsIgnoreCase(policy)) {
                    enabled = isVolteServiceRegistered(phoneId);
                } else if ("PS_ONLY_PSREGIED".equalsIgnoreCase(policy)) {
                    enabled = isPsRegistered(phoneId);
                }
                if (this.smUtMap.get(Integer.valueOf(phoneId)).isForbidden()) {
                    IMSLog.e(LOG_TAG, phoneId, "Ut Request is blocked by previous 403 Error");
                    enabled = false;
                }
                if (mno == Mno.CTC || mno == Mno.CTCMO) {
                    String iccType = SemSystemProperties.get("ril.ICC_TYPE" + phoneId);
                    String isCsim = SemSystemProperties.get("ril.IsCSIM");
                    IMSLog.i(LOG_TAG, phoneId, "iccType : " + iccType + " isCsim = " + isCsim);
                    String[] splitIsCsim = isCsim.split(",");
                    if (DiagnosisConstants.RCSM_ORST_HTTP.equals(iccType) && splitIsCsim.length > phoneId && "0".equals(splitIsCsim[phoneId])) {
                        IMSLog.i(LOG_TAG, phoneId, "RUIM did not support UT interface");
                        this.smUtMap.get(Integer.valueOf(phoneId)).setForce403Error(true);
                        enabled = false;
                    }
                }
                IMSLog.i(LOG_TAG, phoneId, "isUtEnabled with policy : " + policy + " enabled = " + enabled);
                return enabled;
            }
            IMSLog.e(LOG_TAG, phoneId, "Doesn't have any XCAP apn");
            return false;
        }
    }

    public boolean isUssdEnabled(int phoneId) {
        boolean enabled = false;
        String policy = getSetting(phoneId, GlobalSettingsConstants.Call.USSD_DOMAIN, "CS");
        if ("PS".equalsIgnoreCase(policy) || DiagnosisConstants.PSCI_KEY_CALL_BEARER.equalsIgnoreCase(policy)) {
            enabled = checkSpecificPolicy(phoneId);
        } else if ("CS".equalsIgnoreCase(policy)) {
            enabled = false;
        }
        IMSLog.i(LOG_TAG, phoneId, "isUssdEnabled with policy : " + policy + " enabled = " + enabled);
        return enabled;
    }

    private String ctcOperator(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.CTC) {
            return "46003";
        }
        if (mno == Mno.CTCMO) {
            return "45502";
        }
        return null;
    }

    private boolean queryApnExist(int phoneId, String selection, Uri CONTENT_URI) {
        Context context = this.mContext;
        if (context == null) {
            IMSLog.e(LOG_TAG, phoneId, "queryApnExist(): There is no ImsContext");
            return false;
        }
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, (String[]) null, selection, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    IMSLog.i(LOG_TAG, phoneId, "queryApnExist " + selection);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
        throw th;
    }

    private boolean getApnExist(int phoneId, String apntype) {
        String selection;
        Uri CONTENT_URI;
        IMSLog.i(LOG_TAG, phoneId, "getApnExist(): " + apntype);
        String operator = ctcOperator(phoneId);
        if (operator == null) {
            CONTENT_URI = Uri.withAppendedPath(Telephony.Carriers.SIM_APN_URI, String.valueOf(SimUtil.getSubId(phoneId)));
            selection = "type like '%" + apntype + "%'";
            if (SimUtil.getPhoneCount() > 1) {
                if (phoneId == 1) {
                    selection = selection + " AND current1 = 1";
                } else {
                    selection = selection + " AND current = 1";
                }
            }
        } else {
            selection = "numeric = '" + operator + "'and (type LIKE '%" + apntype + "%')";
            CONTENT_URI = Uri.parse("content://telephony/carriers");
        }
        if (queryApnExist(phoneId, selection, CONTENT_URI)) {
            return true;
        }
        IMSLog.e(LOG_TAG, phoneId, "getApnExist(): There is no apntype=" + apntype);
        return false;
    }

    private boolean getEpdgApnExist(int phoneId, String apnType) {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        String mnoName = sm == null ? null : sm.getSimMnoName();
        IMSLog.i(LOG_TAG, phoneId, "getEpdgApnExist(): mnoName=" + mnoName);
        if (mnoName == null) {
            IMSLog.e(LOG_TAG, phoneId, "getEpdgApnExist(): There is no mnoName");
            return false;
        }
        if (queryApnExist(phoneId, "mnoname = '" + mnoName + "' AND apnname = '" + apnType + "'", Uri.parse("content://iwlansettings/todos"))) {
            return true;
        }
        IMSLog.e(LOG_TAG, phoneId, "getEpdgApnExist(): There is no apntype=" + apnType);
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkXcapApn(int phoneId) {
        IUserAgent ua = getUa(phoneId);
        String apnSelection = "xcap";
        if (this.mIsConfigured[phoneId]) {
            apnSelection = this.smUtMap.get(Integer.valueOf(phoneId)).getConfig().apnSelection;
        }
        if (ua != null && ua.getImsProfile().isSoftphoneEnabled()) {
            return true;
        }
        if (this.mPdnController.isEpdgConnected(phoneId)) {
            if (getEpdgApnExist(phoneId, apnSelection)) {
                return true;
            }
            if (this.mPdnController.getMobileDataRegState(phoneId) != 0) {
                IMSLog.i(LOG_TAG, phoneId, "checkXcapApn(): ePDG XCAP APN not exist. PS also not registered.");
                return false;
            }
        }
        return getApnExist(phoneId, apnSelection);
    }

    private boolean checkSpecificPolicy(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        ImsRegistration[] mRegiInfo = this.mImsFramework.getRegistrationManager().getRegistrationInfoByPhoneId(phoneId);
        if (mRegiInfo != null) {
            int length = mRegiInfo.length;
            int i = 0;
            while (i < length) {
                ImsRegistration regi = mRegiInfo[i];
                if (regi.getImsProfile().hasEmergencySupport() || !regi.hasService("mmtel") || regi.getImsProfile().getCmcType() != 0) {
                    i++;
                } else if (mno != Mno.ATT || regi.getCurrentRat() == 18) {
                    return true;
                } else {
                    if (regi.getCurrentRat() == 13 && this.mPdnController.getVopsIndication(phoneId) != VoPsIndication.NOT_SUPPORTED) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    private void registerAirplaneModeObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this.mAirplaneModeObserver);
    }

    /* access modifiers changed from: protected */
    public boolean needToGetSimservDocOnBootup(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        if (!mno.isTmobile() && mno != Mno.TELEKOM_ALBANIA) {
            return false;
        }
        IMSLog.i(LOG_TAG, phoneId, "needToGetSimservDocOnBootup : mPdnController.isVoiceRoaming(phoneId) = " + this.mPdnController.isVoiceRoaming(phoneId) + ", SimUtil.getDefaultPhoneId() = " + SimUtil.getDefaultPhoneId() + ", isUtEnabled(phoneId) = " + isUtEnabled(phoneId) + ", smUtMap.get(phoneId).isSentSimServ() = " + this.smUtMap.get(Integer.valueOf(phoneId)).isSentSimServ() + ", = mSentSimServDocCount[phoneId]) = " + this.mSentSimServDocCount[phoneId]);
        if (!this.mPdnController.isVoiceRoaming(phoneId) && phoneId == SimUtil.getDefaultPhoneId() && isUtEnabled(phoneId) && !this.smUtMap.get(Integer.valueOf(phoneId)).isSentSimServ() && this.mSentSimServDocCount[phoneId] <= 3) {
            return true;
        }
        return false;
    }

    public void enableUt(int phoneId, boolean enable) {
        IMSLog.i(LOG_TAG, phoneId, "UtSwitch: " + enable);
        SimpleEventLog simpleEventLog = this.mUtServiceHistory;
        simpleEventLog.add(phoneId, "UtSwitch: " + enable);
        this.mUtSwitch[phoneId] = enable;
        updateCapabilities(phoneId);
    }

    private class CWDBContentObserver extends ContentObserver {
        public CWDBContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            int phoneId = 0;
            if (uri.toString().contains("slot2")) {
                phoneId = 1;
            }
            IMSLog.i(UtServiceModule.LOG_TAG, phoneId, "CWDBContentObserver - onChange() with " + uri);
            UtServiceModule.this.setImsCallWaiting(phoneId);
        }
    }

    /* access modifiers changed from: protected */
    public boolean setImsCallWaiting(int phoneId) {
        boolean activate;
        boolean z = false;
        if (phoneId == 1) {
            if (Settings.System.getInt(this.mContext.getContentResolver(), "volte_call_waiting_slot2", 1) == 1) {
                z = true;
            }
            activate = z;
        } else {
            if (Settings.System.getInt(this.mContext.getContentResolver(), "volte_call_waiting", 1) == 1) {
                z = true;
            }
            activate = z;
        }
        IMSLog.i(LOG_TAG, phoneId, "setImsCallWaiting(): activate=" + activate);
        UserConfiguration.setUserConfig(this.mContext, phoneId, "enable_call_wait", activate);
        return activate;
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://com.sec.ims.settings/mno"), true, this.mMnoUpdateObserver);
    }

    public void registerCwdbObserver(int phoneId) {
        if (this.mCWDBChangeObserver.get(phoneId) == null) {
            this.mCWDBChangeObserver.put(phoneId, new CWDBContentObserver(this));
        }
        if (phoneId == 0) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("volte_call_waiting"), true, this.mCWDBChangeObserver.get(phoneId));
        } else {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("volte_call_waiting_slot2"), true, this.mCWDBChangeObserver.get(phoneId));
        }
    }

    public void unregisterCwdbObserver(int phoneId) {
        if (this.mCWDBChangeObserver.get(phoneId) != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mCWDBChangeObserver.get(phoneId));
            this.mCWDBChangeObserver.remove(phoneId);
        }
    }

    public void registerForUtEvent(int phoneId, IImsUtEventListener listener) {
        this.mListeners.put(Integer.valueOf(phoneId), listener);
    }

    public void deRegisterForUtEvent(int phoneId, IImsUtEventListener listener) {
        this.mListeners.remove(Integer.valueOf(phoneId));
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    /* access modifiers changed from: protected */
    public synchronized void notifySuccessResult(int phoneId, int requestType, int requestId, Bundle[] response) {
        IMSLog.i(LOG_TAG, phoneId, "notifySuccessResult : " + requestId);
        switch (requestType) {
            case 100:
                try {
                    this.mListeners.get(Integer.valueOf(phoneId)).onUtConfigurationCallForwardQueried(requestId, response);
                    break;
                } catch (RemoteException e) {
                    e.printStackTrace();
                    break;
                }
            case 101:
            case 103:
            case 105:
            case 107:
            case 109:
            case 111:
            case 113:
            case 115:
                try {
                    this.mListeners.get(Integer.valueOf(phoneId)).onUtConfigurationUpdated(requestId);
                    break;
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                    break;
                }
            case 102:
            case 104:
                try {
                    this.mListeners.get(Integer.valueOf(phoneId)).onUtConfigurationCallBarringQueried(requestId, response);
                    break;
                } catch (RemoteException e3) {
                    e3.printStackTrace();
                    break;
                }
            case 106:
            case 108:
            case 110:
            case 112:
                try {
                    this.mListeners.get(Integer.valueOf(phoneId)).onUtConfigurationQueried(requestId, response[0]);
                    break;
                } catch (RemoteException e4) {
                    e4.printStackTrace();
                    break;
                }
            case 114:
                try {
                    this.mListeners.get(Integer.valueOf(phoneId)).onUtConfigurationCallWaitingQueried(requestId, response[0].getBoolean("status", false));
                    break;
                } catch (RemoteException e5) {
                    e5.printStackTrace();
                    break;
                }
        }
        return;
    }

    /* access modifiers changed from: protected */
    public synchronized void notifyFailResult(int phoneId, int requestType, int requestId, Bundle response) {
        IMSLog.i(LOG_TAG, "notifyFailResult : " + requestId + " requestType : " + requestType);
        if (requestType % 2 != 0) {
            try {
                this.mListeners.get(Integer.valueOf(phoneId)).onUtConfigurationUpdateFailed(requestId, response);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.mListeners.get(Integer.valueOf(phoneId)).onUtConfigurationQueryFailed(requestId, response);
            } catch (RemoteException e2) {
                e2.printStackTrace();
            }
        }
        return;
    }

    public void updateCapabilities(int phoneId) {
        int[] capabilities = {4};
        boolean[] capables = new boolean[capabilities.length];
        Arrays.fill(capables, false);
        capables[0] = isUtEnabled(phoneId);
        this.mImsFramework.getGoogleImsAdaptor().updateCapabilities(phoneId, capabilities, capables);
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int phoneId) {
        ImsFeature.Capabilities capabilities = new ImsFeature.Capabilities();
        if (isUtEnabled(phoneId)) {
            capabilities.addCapabilities(4);
        }
        return capabilities;
    }

    /* access modifiers changed from: protected */
    public boolean isVolteServiceRegistered(int phoneId) {
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        if (rm == null) {
            return false;
        }
        for (IRegisterTask task : rm.getPendingRegistration(phoneId)) {
            if (task.getProfile().getPdnType() != 15 && task.getImsRegistration() != null && task.getProfile().getCmcType() == 0 && task.getImsRegistration().hasVolteService() && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                return true;
            }
        }
        return false;
    }

    private boolean isPsRegistered(int phoneId) {
        if (this.mPdnController.getMobileDataRegState(phoneId) != 0 && !this.mPdnController.isEpdgConnected(phoneId)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void processSpUtRequest(UtProfile mProfile) {
        if (this.mSoftphoneService != null) {
            this.mSoftProfile = mProfile;
            this.mIsRunningRequest = true;
            try {
                if (mProfile.type == 114) {
                    this.mSoftphoneService.getCallWaitingInfo(this.mSoftPhoneClientId);
                } else if (mProfile.type == 115) {
                    this.mSoftphoneService.setCallWaitingInfo(this.mSoftPhoneClientId, new CallWaitingInfo(mProfile.enable));
                } else if (mProfile.type == 100) {
                    this.mSoftphoneService.getCallForwardingInfo(this.mSoftPhoneClientId);
                } else if (mProfile.type == 101) {
                    boolean retained = false;
                    boolean enable = true;
                    if (mProfile.action == 3) {
                        enable = true;
                        if (TextUtils.isEmpty(mProfile.number)) {
                            retained = true;
                        }
                    } else if (mProfile.action == 4) {
                        enable = false;
                        retained = true;
                    } else if (mProfile.action == 1) {
                        enable = true;
                        if (TextUtils.isEmpty(mProfile.number)) {
                            retained = true;
                        }
                    } else if (mProfile.action == 0) {
                        enable = false;
                        retained = false;
                    }
                    this.mSoftphoneService.setCallForwardingInfo(this.mSoftPhoneClientId, new CallForwardingInfo(enable, retained, mProfile.timeSeconds, mProfile.condition, mProfile.number));
                }
            } catch (RemoteException e) {
                IMSLog.e(LOG_TAG, "RemoteException happen");
            }
        }
    }

    public void connected() {
        IMSLog.i(LOG_TAG, "connected is started");
        String activeAccount = getActiveAccount(this.mContext);
        this.mSoftPhoneAccountId = activeAccount;
        if (activeAccount == null) {
            IMSLog.e(LOG_TAG, "no active account, supplementary service is not available, grey out.");
            return;
        }
        IMSLog.i(LOG_TAG, "mAccountId = " + this.mSoftPhoneAccountId);
        registerSupplementaryServiceListener(this.mSoftPhoneAccountId);
    }

    public String getActiveAccount(Context mContext2) {
        IMSLog.i(LOG_TAG, "getActiveAccount is started");
        String mAccountId = null;
        Cursor cursor = mContext2.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildActiveAccountUri(), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                IMSLog.i(LOG_TAG, "found " + cursor.getCount() + " active users");
                if (cursor.getCount() == 0) {
                    mAccountId = null;
                } else if (cursor.moveToFirst()) {
                    mAccountId = cursor.getString(cursor.getColumnIndex("account_id"));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return mAccountId;
        throw th;
    }

    public void registerSupplementaryServiceListener(String accountId) {
        this.mSoftPhoneAccountId = accountId;
        try {
            int clientId = this.mSoftphoneService.getClientId(accountId);
            this.mSoftPhoneClientId = clientId;
            this.mSoftphoneService.registerSupplementaryServiceListener(clientId, this.mSupplementaryServiceListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void bindSoftPhoneService() {
        if (this.mSoftphoneService == null) {
            Intent serviceIntent = new Intent();
            serviceIntent.setClassName("com.sec.imsservice", SoftphoneContract.SERVICE_CLASS_NAME);
            this.mContext.bindService(serviceIntent, this.mConnection, 1);
            return;
        }
        IMSLog.i(LOG_TAG, "mSoftphoneService is not null");
        connected();
    }

    public void unbindSoftPhoneService() {
        if (this.mSoftphoneBound) {
            this.mContext.unbindService(this.mConnection);
            this.mSoftphoneBound = false;
            this.mConnection.onServiceDisconnected((ComponentName) null);
        }
        IMSLog.i(LOG_TAG, "is bind" + this.mSoftphoneBound);
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        this.mUtServiceHistory.dump();
        IMSLog.decreaseIndent(LOG_TAG);
    }

    /* access modifiers changed from: protected */
    public boolean getSetting(int phoneId, String name, boolean defaultVal) {
        return this.mImsFramework.getBoolean(phoneId, name, defaultVal);
    }

    /* access modifiers changed from: protected */
    public int getSetting(int phoneId, String name, int defaultVal) {
        return this.mImsFramework.getInt(phoneId, name, defaultVal);
    }

    /* access modifiers changed from: protected */
    public String getSetting(int phoneId, String name, String defaultVal) {
        return this.mImsFramework.getString(phoneId, name, defaultVal);
    }

    /* access modifiers changed from: protected */
    public void setLastUri(String impi, ImsUri uri) {
        this.mLastUri.put(impi, uri);
    }

    /* access modifiers changed from: protected */
    public void enqueueRequest(UtProfile profile) {
        this.mPendingRequests.add(profile);
    }

    /* access modifiers changed from: protected */
    public UtProfile dequeueRequest() {
        UtProfile retProfile = this.mPendingRequests.get(0);
        this.mPendingRequests.remove(0);
        return retProfile;
    }

    private void printLog(int phoneId, UtProfile profile) {
        if (profile == null) {
            IMSLog.d(LOG_TAG, phoneId, "UtProfile is null.");
            return;
        }
        String log = UtLog.extractLogFromUtProfile(profile);
        String crLog = UtLog.extractCrLogFromUtProfile(phoneId, profile);
        IMSLog.i(LOG_TAG, phoneId, log);
        this.mUtServiceHistory.add(phoneId, log);
        IMSLog.c(LogClass.UT_REQUEST, crLog);
    }

    /* access modifiers changed from: protected */
    public void writeDump(int phoneId, String val) {
        this.mUtServiceHistory.add(phoneId, val);
    }
}
