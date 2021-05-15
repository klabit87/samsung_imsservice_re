package com.sec.internal.ims.servicemodules.volte2;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.epdg.EpdgManager;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorVzw;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.CscFeatureTagCommon;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RtpLossRateNoti;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.WfcEpdgManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.util.ImsPhoneStateManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.xq.att.ImsXqReporter;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.handler.IMediaServiceInterface;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VolteServiceModuleInternal extends ServiceModuleBase implements IVolteServiceModuleInternal {
    protected ImsUri[] mActiveImpu;
    protected boolean[] mAutomaticMode;
    protected boolean mCheckRunningState;
    protected ICmcMediaController mCmcMediaController;
    protected final Map<Integer, Message> mCmcPdCheckTimeOut;
    protected CmcServiceHelper mCmcServiceModule;
    protected final Context mContext;
    protected boolean[] mEcbmMode;
    protected TmoEcholocateIntentBroadcaster mEcholocateIntentBroadcaster;
    protected boolean mEnableCallWaitingRule;
    protected ImsManager.EpdgListener mEpdgListener;
    protected final Map<Integer, Message> mEpdnDisconnectTimeOut;
    protected SimpleEventLog mEventLog;
    protected ImsCallSessionManager mImsCallSessionManager;
    protected ImsExternalCallController mImsExternalCallController;
    protected ImsXqReporter mImsXqReporter;
    protected final Map<Integer, Boolean> mIsCmcPdCheckRespRecevied;
    private boolean[] mIsDeregisterTimerRunning;
    protected boolean[] mIsLteEpsOnlyAttached;
    protected DialogEvent[] mLastDialogEvent;
    protected int[] mLastRegiErrorCode;
    protected IImsMediaController mMediaController;
    protected IMediaServiceInterface mMediaSvcIntf;
    protected boolean mMmtelAcquiredEver;
    protected MobileCareController mMobileCareController;
    protected Map<Integer, NetworkEvent> mNetworks;
    protected IOptionsServiceInterface mOptionsSvcIntf;
    protected final IPdnController mPdnController;
    protected final List<PhoneStateListenerInternal> mPhoneStateListener;
    protected final ImsPhoneStateManager mPhoneStateManager;
    protected boolean[] mProhibited;
    protected QualityStatistics mQualityStatistics;
    protected boolean[] mRatChanged;
    protected final IRegistrationManager mRegMan;
    protected boolean[] mReleaseWfcBeforeHO;
    protected int[] mRttMode;
    private RttSettingObserver mRttSettingObserver;
    protected final List<? extends ISimManager> mSimManagers;
    protected SsacManager mSsacManager;
    protected final ITelephonyManager mTelephonyManager;
    protected int[] mTtyMode;
    protected VolteNotifier mVolteNotifier;
    protected IVolteServiceInterface mVolteSvcIntf;
    private WfcEpdgManager.WfcEpdgConnectionListener mWfcEpdgConnectionListener;
    protected WfcEpdgManager mWfcEpdgMgr;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public VolteServiceModuleInternal(Looper looper, Context context, IRegistrationManager rm, IPdnController pc, IVolteServiceInterface volteServiceInterface, IMediaServiceInterface mediaServiceInterface, IOptionsServiceInterface optionsServiceInterface) {
        super(looper);
        Context context2 = context;
        IRegistrationManager iRegistrationManager = rm;
        this.mEpdnDisconnectTimeOut = new ArrayMap();
        this.mCmcPdCheckTimeOut = new ArrayMap();
        this.mIsCmcPdCheckRespRecevied = new ArrayMap();
        this.mPhoneStateListener = new ArrayList();
        this.mEcholocateIntentBroadcaster = null;
        this.mImsXqReporter = null;
        this.mEnableCallWaitingRule = true;
        this.mNetworks = new ConcurrentHashMap();
        this.mMmtelAcquiredEver = false;
        this.mWfcEpdgMgr = null;
        this.mQualityStatistics = null;
        this.mWfcEpdgConnectionListener = null;
        this.mEpdgListener = null;
        this.mCheckRunningState = false;
        this.mRttSettingObserver = null;
        this.mCheckRunningState = false;
        this.mContext = context2;
        this.mEventLog = new SimpleEventLog(context2, NAME, 100);
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mVolteSvcIntf = volteServiceInterface;
        this.mMediaSvcIntf = mediaServiceInterface;
        this.mOptionsSvcIntf = optionsServiceInterface;
        List<? extends ISimManager> allSimManagers = SimManagerFactory.getAllSimManagers();
        this.mSimManagers = allSimManagers;
        int phoneCount = allSimManagers.size();
        boolean[] zArr = new boolean[phoneCount];
        this.mProhibited = zArr;
        this.mIsLteEpsOnlyAttached = new boolean[phoneCount];
        this.mRatChanged = new boolean[phoneCount];
        this.mEcbmMode = new boolean[phoneCount];
        this.mLastDialogEvent = new DialogEvent[phoneCount];
        this.mActiveImpu = new ImsUri[phoneCount];
        this.mTtyMode = new int[phoneCount];
        this.mRttMode = new int[phoneCount];
        this.mAutomaticMode = new boolean[phoneCount];
        this.mReleaseWfcBeforeHO = new boolean[phoneCount];
        this.mLastRegiErrorCode = new int[phoneCount];
        this.mIsDeregisterTimerRunning = new boolean[phoneCount];
        Arrays.fill(zArr, false);
        Arrays.fill(this.mIsLteEpsOnlyAttached, false);
        Arrays.fill(this.mRatChanged, false);
        Arrays.fill(this.mEcbmMode, false);
        Arrays.fill(this.mLastDialogEvent, (Object) null);
        Arrays.fill(this.mActiveImpu, (Object) null);
        Arrays.fill(this.mTtyMode, Extensions.TelecomManager.TTY_MODE_OFF);
        Arrays.fill(this.mRttMode, -1);
        Arrays.fill(this.mAutomaticMode, false);
        Arrays.fill(this.mReleaseWfcBeforeHO, false);
        Arrays.fill(this.mLastRegiErrorCode, 0);
        Arrays.fill(this.mIsDeregisterTimerRunning, false);
        this.mVolteSvcIntf.registerForIncomingCallEvent(this, 1, (Object) null);
        this.mVolteSvcIntf.registerForCallStateEvent(this, 2, (Object) null);
        this.mVolteSvcIntf.registerForDialogEvent(this, 3, (Object) null);
        this.mVolteSvcIntf.registerForDedicatedBearerNotifyEvent(this, 8, (Object) null);
        this.mVolteSvcIntf.registerForDtmfEvent(this, 17, (Object) null);
        this.mVolteSvcIntf.registerForTextEvent(this, 22, (Object) null);
        this.mVolteSvcIntf.registerForSIPMSGEvent(this, 25, (Object) null);
        this.mVolteSvcIntf.registerForRtpLossRateNoti(this, 18, (Object) null);
        SimManagerFactory.registerForSubIdChange(this, 24, (Object) null);
        if (this.mTelephonyManager.getPhoneCount() > 1 && SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS())) {
            SimManagerFactory.registerForDDSChange(this, 26, (Object) null);
        }
        this.mPhoneStateManager = new ImsPhoneStateManager(this.mContext, 20769);
        for (ISimManager sm : this.mSimManagers) {
            if (!SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || sm.getSimSlotIndex() == SimUtil.getDefaultPhoneId()) {
                PhoneStateListenerInternal psli = new PhoneStateListenerInternal(sm.getSimSlotIndex(), sm.getSubscriptionId());
                this.mPhoneStateListener.add(psli);
                this.mPhoneStateManager.registerListener(psli, sm.getSubscriptionId(), sm.getSimSlotIndex());
                sm.registerForSimReady(this, 30, (Object) null);
                sm.registerForSimRemoved(this, 31, (Object) null);
                this.mNetworks.put(Integer.valueOf(sm.getSimSlotIndex()), new NetworkEvent());
            } else {
                IMSLog.i(LOG_TAG, sm.getSimSlotIndex(), "do not make PhoneStateListenerInternal with non-DDS slot");
            }
        }
        this.mSsacManager = new SsacManager(this, iRegistrationManager, phoneCount);
        if (SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTECHOLOCATE)) {
            Log.d(LOG_TAG, "Echolocate enabled");
            this.mEcholocateIntentBroadcaster = new TmoEcholocateIntentBroadcaster(this.mContext, this);
        }
        if (SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTCIQ)) {
            this.mImsXqReporter = new ImsXqReporter(this.mContext);
        }
        this.mRegMan = iRegistrationManager;
        this.mPdnController = pc;
        this.mMediaController = new ImsMediaController(this, getLooper(), this.mEventLog);
        this.mMobileCareController = new MobileCareController(this.mContext);
        this.mImsCallSessionManager = new ImsCallSessionManager(this, this.mTelephonyManager, this.mPdnController, this.mRegMan, getLooper());
        this.mCmcServiceModule = new CmcServiceHelper(looper, this.mContext, this.mRegistrationList, this.mVolteSvcIntf, this.mMediaController, this.mImsCallSessionManager, this.mOptionsSvcIntf, phoneCount);
        this.mCmcMediaController = new CmcMediaController(this, getLooper(), this.mImsCallSessionManager, this.mEventLog);
        this.mImsExternalCallController = new ImsExternalCallController(this);
        this.mVolteNotifier = new VolteNotifier();
        setRttMode(ImsUtil.isRttModeOnFromCallSettings(this.mContext, 0) ? Extensions.TelecomManager.RTT_MODE : Extensions.TelecomManager.TTY_MODE_OFF);
        this.mRttSettingObserver = new RttSettingObserver(this.mContext, this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ImsConstants.Intents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        filter.addAction(IVolteServiceModuleInternal.INTENT_ACTION_LTE_BAND_CHANGED);
        filter.addAction(IVolteServiceModuleInternal.ACTION_EMERGENCY_CALLBACK_MODE_INTERNAL);
        filter.addAction(IVolteServiceModuleInternal.INTENT_ACTION_PS_BARRED);
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(IVolteServiceModuleInternal.INTENT_ACTION_IQISERVICE_STATE_CHNAGED);
        if (SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTHUXDEVICEQUALITYSTATISTICS)) {
            this.mQualityStatistics = new QualityStatistics(this.mContext);
        }
        this.mWfcEpdgMgr = ImsServiceStub.getInstance().getWfcEpdgManager();
        registerEpdgConnectionListener();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* JADX WARNING: Can't fix incorrect switch cases order */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r8, android.content.Intent r9) {
                /*
                    r7 = this;
                    java.lang.String r0 = r9.getAction()
                    int r1 = r0.hashCode()
                    r2 = 6
                    r3 = 1
                    r4 = 0
                    r5 = -1
                    switch(r1) {
                        case -2128145023: goto L_0x004c;
                        case -2065845397: goto L_0x0042;
                        case -1926447105: goto L_0x0038;
                        case -1664867553: goto L_0x002e;
                        case -1454123155: goto L_0x0024;
                        case -1264850110: goto L_0x001a;
                        case 414503228: goto L_0x0010;
                        default: goto L_0x000f;
                    }
                L_0x000f:
                    goto L_0x0056
                L_0x0010:
                    java.lang.String r1 = "android.intent.action.LTE_BAND"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x000f
                    r1 = 2
                    goto L_0x0057
                L_0x001a:
                    java.lang.String r1 = "com.android.intent.action.PSBARRED_FOR_VOLTE"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x000f
                    r1 = 3
                    goto L_0x0057
                L_0x0024:
                    java.lang.String r1 = "android.intent.action.SCREEN_ON"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x000f
                    r1 = 4
                    goto L_0x0057
                L_0x002e:
                    java.lang.String r1 = "com.samsung.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED_INTERNAL"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x000f
                    r1 = r3
                    goto L_0x0057
                L_0x0038:
                    java.lang.String r1 = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x000f
                    r1 = r4
                    goto L_0x0057
                L_0x0042:
                    java.lang.String r1 = "com.att.iqi.action.SERVICE_STATE_CHANGED"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x000f
                    r1 = r2
                    goto L_0x0057
                L_0x004c:
                    java.lang.String r1 = "android.intent.action.SCREEN_OFF"
                    boolean r1 = r0.equals(r1)
                    if (r1 == 0) goto L_0x000f
                    r1 = 5
                    goto L_0x0057
                L_0x0056:
                    r1 = r5
                L_0x0057:
                    r6 = 23
                    switch(r1) {
                        case 0: goto L_0x00aa;
                        case 1: goto L_0x00aa;
                        case 2: goto L_0x009c;
                        case 3: goto L_0x0084;
                        case 4: goto L_0x007a;
                        case 5: goto L_0x0070;
                        case 6: goto L_0x005e;
                        default: goto L_0x005c;
                    }
                L_0x005c:
                    goto L_0x00d3
                L_0x005e:
                    java.lang.String r1 = "com.att.iqi.extra.SERVICE_RUNNING"
                    boolean r1 = r9.getBooleanExtra(r1, r4)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r2 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    r3 = 28
                    android.os.Message r3 = r2.obtainMessage(r3, r1, r5)
                    r2.sendMessage(r3)
                    goto L_0x00d3
                L_0x0070:
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r1 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r2 = r1.obtainMessage(r6, r4, r5)
                    r1.sendMessage(r2)
                    goto L_0x00d3
                L_0x007a:
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r1 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r2 = r1.obtainMessage(r6, r3, r5)
                    r1.sendMessage(r2)
                    goto L_0x00d3
                L_0x0084:
                    java.lang.String r1 = "cmd"
                    java.lang.String r1 = r9.getStringExtra(r1)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r2 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    r3 = 14
                    java.lang.String r4 = "1"
                    boolean r4 = r4.equals(r1)
                    android.os.Message r3 = r2.obtainMessage(r3, r4, r5)
                    r2.sendMessage(r3)
                    goto L_0x00d3
                L_0x009c:
                    java.lang.String r1 = "BAND"
                    java.lang.String r1 = r9.getStringExtra(r1)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r2 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    com.sec.internal.ims.servicemodules.volte2.MobileCareController r2 = r2.mMobileCareController
                    r2.onLteBancChanged(r1)
                    goto L_0x00d3
                L_0x00aa:
                    java.lang.String r1 = "android.telephony.extra.PHONE_IN_ECM_STATE"
                    boolean r1 = r9.getBooleanExtra(r1, r4)
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r5 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    int r5 = r5.mDefaultPhoneId
                    java.lang.String r6 = "phone"
                    int r5 = r9.getIntExtra(r6, r5)
                    if (r1 == 0) goto L_0x00c9
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r4 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r2 = r4.obtainMessage(r2, r5, r3)
                    r4.sendMessage(r2)
                    goto L_0x00d3
                L_0x00c9:
                    com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal r3 = com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.this
                    android.os.Message r2 = r3.obtainMessage(r2, r5, r4)
                    r3.sendMessage(r2)
                L_0x00d3:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
            }
        }, filter);
        Log.i(LOG_TAG, "VolteServiceModule created");
    }

    public void init() {
        super.init();
        this.mRttSettingObserver.init();
        this.mCmcServiceModule.init();
    }

    public String[] getServicesRequiring() {
        return new String[0];
    }

    public ImsCallSession createSession(CallProfile profile) throws RemoteException {
        return this.mImsCallSessionManager.createSession(profile, profile == null ? null : getImsRegistration(profile.getPhoneId()));
    }

    public ImsCallSession createSession(CallProfile profile, int regId) throws RemoteException {
        return this.mImsCallSessionManager.createSession(profile, getRegInfo(regId));
    }

    public boolean isEmergencyRegistered(int phoneId) {
        return getImsRegistration(phoneId, true) != null;
    }

    public boolean isEcbmMode(int phoneId) {
        return this.mEcbmMode[phoneId];
    }

    private PhoneStateListenerInternal getPhoneStateListener(int phoneId) {
        for (PhoneStateListenerInternal psli : this.mPhoneStateListener) {
            if (psli.getInternalPhoneId() == phoneId) {
                return psli;
            }
        }
        IMSLog.i(LOG_TAG, phoneId, "getPhoneStateListener: psli is not exist.");
        return null;
    }

    /* access modifiers changed from: protected */
    public void registerPhoneStateListener(int phoneId) {
        if (!SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || phoneId == SimUtil.getDefaultPhoneId()) {
            IMSLog.i(LOG_TAG, phoneId, "registerPhoneStateListener:");
            int subId = SimUtil.getSubId(phoneId);
            if (subId >= 0) {
                PhoneStateListenerInternal psli = new PhoneStateListenerInternal(phoneId, subId);
                if (getPhoneStateListener(phoneId) == null) {
                    this.mPhoneStateListener.add(psli);
                }
                this.mPhoneStateManager.registerListener(psli, subId, phoneId);
                return;
            }
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "do not register to non-DDS PhoneStateListener");
    }

    /* access modifiers changed from: protected */
    public void onDefaultDataSubscriptionChanged() {
        Log.i(LOG_TAG, "onDefaultDataSubscriptionChanged");
        for (ISimManager sm : this.mSimManagers) {
            int phoneId = sm.getSimSlotIndex();
            unRegisterPhoneStateListener(phoneId);
            if (phoneId == SimUtil.getDefaultPhoneId()) {
                registerPhoneStateListener(phoneId);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void unRegisterPhoneStateListener(int simSlot) {
        IMSLog.i(LOG_TAG, simSlot, "unRegisterPhoneStateListener:");
        this.mPhoneStateManager.unRegisterListener(simSlot);
        PhoneStateListenerInternal removeObj = getPhoneStateListener(simSlot);
        if (removeObj != null) {
            this.mPhoneStateListener.remove(removeObj);
        }
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + LOG_TAG + ":");
        IMSLog.increaseIndent(LOG_TAG);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(LOG_TAG);
    }

    public void sendMobileCareEvent(int phoneId, int callType, int error, String msg) {
        if (this.mMobileCareController.isEnabled()) {
            this.mMobileCareController.sendMobileCareEvent(phoneId, callType, error, msg, this.mPdnController.isEpdgConnected(phoneId));
        }
    }

    public void onImsConifgChanged(int phoneId, String dmUri) {
        String str = LOG_TAG;
        Log.i(str, "onChange: config changed : " + dmUri);
        if (dmUri != null) {
            sendMessage(obtainMessage(21, phoneId, 0, dmUri));
        }
    }

    public boolean acceptCallWhileSmsipRegistered(ImsRegistration reg) {
        if (reg == null) {
            Log.e(LOG_TAG, "Not registered.");
            return false;
        }
        int phoneId = reg.getPhoneId();
        int subId = SimUtil.getSubId(phoneId);
        String str = LOG_TAG;
        Log.i(str, "isVowifiEnabled=" + isVowifiEnabled(phoneId) + ", isVideoSettingEnabled=" + isVideoSettingEnabled() + ", isEpdgConnected=" + this.mPdnController.isEpdgConnected(phoneId) + ", VoiceNetworkType=" + this.mTelephonyManager.getVoiceNetworkType(subId) + ", DataNetworkType=" + this.mTelephonyManager.getDataNetworkType(subId) + ", SMSIP=" + reg.hasService("smsip") + ", VOICE=" + reg.hasService("mmtel") + ", VIDEO=" + reg.hasService("mmtel-video"));
        if (!isVowifiEnabled(phoneId) || isVideoSettingEnabled() || !this.mPdnController.isEpdgConnected(phoneId) || this.mTelephonyManager.getVoiceNetworkType(subId) != 7) {
            return false;
        }
        if ((this.mTelephonyManager.getDataNetworkType(subId) == 14 || this.mTelephonyManager.getDataNetworkType(subId) == 18) && reg.hasService("smsip") && !reg.hasService("mmtel") && !reg.hasService("mmtel-video")) {
            return true;
        }
        return false;
    }

    public void setRttMode(int mode) {
        setRttMode(this.mDefaultPhoneId, mode);
    }

    public void setRttMode(int phoneId, int mode) {
        IRegistrationManager iRegistrationManager;
        int[] iArr = this.mRttMode;
        int prevMode = iArr[phoneId];
        iArr[phoneId] = mode;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("setRttMode: " + prevMode + " -> " + this.mRttMode[phoneId]);
        if (this.mImsCallSessionManager.getSessionCount() != 0 || (iRegistrationManager = this.mRegMan) == null) {
            Log.i(LOG_TAG, "setRttMode: RTT registration is skiped because the call session exist.");
        } else {
            iRegistrationManager.setRttMode(phoneId, this.mRttMode[phoneId] == Extensions.TelecomManager.RTT_MODE);
        }
        if (prevMode == this.mRttMode[phoneId]) {
            Log.e(LOG_TAG, "setRttMode: not updating sessions");
            return;
        }
        IMSLog.c(LogClass.VOLTE_CHANGE_RTTMODE, phoneId + "," + this.mRttMode[phoneId]);
        this.mVolteSvcIntf.setRttMode(phoneId, this.mRttMode[phoneId]);
    }

    /* Debug info: failed to restart local var, previous not found, register: 2 */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0025, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onTextReceived(com.sec.internal.ims.servicemodules.volte2.data.TextInfo r3) {
        /*
            r2 = this;
            monitor-enter(r2)
            if (r3 == 0) goto L_0x0024
            int r0 = r3.getSessionId()     // Catch:{ all -> 0x0021 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r2.getSession(r0)     // Catch:{ all -> 0x0021 }
            if (r0 != 0) goto L_0x000e
            goto L_0x0024
        L_0x000e:
            int r0 = r3.getSessionId()     // Catch:{ all -> 0x0021 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r2.getSession(r0)     // Catch:{ all -> 0x0021 }
            int r0 = r0.getPhoneId()     // Catch:{ all -> 0x0021 }
            com.sec.internal.ims.servicemodules.volte2.VolteNotifier r1 = r2.mVolteNotifier     // Catch:{ all -> 0x0021 }
            r1.notifyOnRttEventBySession(r0, r3)     // Catch:{ all -> 0x0021 }
            monitor-exit(r2)
            return
        L_0x0021:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        L_0x0024:
            monitor-exit(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.VolteServiceModuleInternal.onTextReceived(com.sec.internal.ims.servicemodules.volte2.data.TextInfo):void");
    }

    public boolean isCsfbErrorCode(int phoneId, int callType, SipError error) {
        return isCsfbErrorCode(phoneId, callType, error, 10);
    }

    public boolean isCsfbErrorCode(int phoneId, int callType, SipError error, int retryAfter) {
        return this.mImsCallSessionManager.isCsfbErrorCode(this.mContext, phoneId, callType, error, retryAfter);
    }

    public void onSendRttSessionModifyRequest(int callId, boolean mode) {
    }

    public void sendQualityStatisticsEvent() {
        QualityStatistics qualityStatistics = this.mQualityStatistics;
        if (qualityStatistics != null) {
            qualityStatistics.sendQualityStatisticsEvent();
        }
    }

    public void onSendRttSessionModifyResponse(int callId, boolean mode, boolean result) {
    }

    public void updateCapabilities(int phoneId) {
        int[] capabilities = {1, 2};
        boolean[] capables = new boolean[capabilities.length];
        Arrays.fill(capables, false);
        if (isCallServiceAvailable(phoneId, "mmtel")) {
            capables[0] = true;
        }
        if (isCallServiceAvailable(phoneId, "mmtel-video")) {
            capables[1] = true;
        }
        ImsRegistry.getGoogleImsAdaptor().updateCapabilities(phoneId, capabilities, capables);
    }

    public ImsFeature.Capabilities queryCapabilityStatus(int phoneId) {
        ImsFeature.Capabilities capabilities = new ImsFeature.Capabilities();
        if (isCallServiceAvailable(phoneId, "mmtel")) {
            capabilities.addCapabilities(1);
        }
        if (isCallServiceAvailable(phoneId, "mmtel-video")) {
            capabilities.addCapabilities(2);
        }
        return capabilities;
    }

    public boolean isCallServiceAvailable(int phoneId, String service) {
        Set<String> services;
        ImsRegistration regiInfo = getImsRegistration(phoneId);
        NetworkEvent ne = getNetwork(phoneId);
        boolean isRunning = isRunning();
        if (!isRunning || regiInfo == null) {
            return this.mCmcServiceModule.isCallServiceAvailableOnSecondary(phoneId, service, isRunning);
        }
        if (isRegistering(phoneId) && (services = this.mRegMan.getServiceForNetwork(regiInfo.getImsProfile(), regiInfo.getRegiRat(), false, phoneId)) != null && !services.contains(service)) {
            return false;
        }
        if (ne.outOfService) {
            String str = LOG_TAG;
            Log.e(str, service + " is not available due to outOfService");
            return false;
        }
        Mno mno = Mno.fromName(regiInfo.getImsProfile().getMnoName());
        if (mno != Mno.ATT) {
            if (mno != Mno.VZW) {
                if (mno.isOneOf(Mno.BOG, Mno.ORANGE, Mno.ORANGE_POLAND, Mno.DIGI, Mno.TELECOM_ITALY, Mno.VODAFONE, Mno.VODAFONE_NEWZEALAND, Mno.WINDTRE, Mno.TELEKOM_ALBANIA) || mno.isTmobile()) {
                    if (!(ne.network == 13 || ne.network == 18 || ne.network == 20)) {
                        String str2 = LOG_TAG;
                        Log.e(str2, service + " is not available due to unsupported N/W");
                        return false;
                    }
                } else if (mno == Mno.AIRTEL && this.mRegMan.isSuspended(regiInfo.getHandle())) {
                    String str3 = LOG_TAG;
                    Log.e(str3, service + " is not available due to N/W suspend");
                    return false;
                }
            } else if (this.mRegMan.isInvite403DisabledService(phoneId)) {
                String str4 = LOG_TAG;
                Log.e(str4, service + " is not available due to isInvite403DisabledService");
                return false;
            } else if (!(ne.network == 13 || ne.network == 18)) {
                String str5 = LOG_TAG;
                Log.e(str5, service + " is not available due to unsupported N/W");
                return false;
            }
            if (this.mIsDeregisterTimerRunning[phoneId]) {
                Log.e(LOG_TAG, "Call Service is not available for delayedDeregiTimer");
                return false;
            } else if (!OmcCode.isKOROmcCode() || ImsUtil.isSimMobilityActivated(phoneId) || !service.equals("mmtel") || ne.network == 13 || ne.network == 20) {
                return regiInfo.hasService(service);
            } else {
                String str6 = LOG_TAG;
                Log.e(str6, "Call Service is not available for " + service);
                return false;
            }
        } else if (SimUtil.isSoftphoneEnabled()) {
            return regiInfo.hasService(service);
        } else {
            if (ne.network == 18) {
                return regiInfo.hasService(service);
            }
            if (ne.network == 13 && (ne.voiceOverPs != VoPsIndication.NOT_SUPPORTED || hasActiveCall(phoneId))) {
                return regiInfo.hasService(service);
            }
            String str7 = LOG_TAG;
            Log.e(str7, service + " is not available due to unsupported N/W");
            return false;
        }
    }

    public long getRttDbrTimer(int phoneId) {
        ImsRegistration regInfo = getImsRegistration(phoneId);
        ImsProfile imsProfile = null;
        if (regInfo != null) {
            imsProfile = regInfo.getImsProfile();
        } else {
            IRegistrationManager iRegistrationManager = this.mRegMan;
            if (iRegistrationManager != null) {
                imsProfile = iRegistrationManager.getImsProfile(phoneId, ImsProfile.PROFILE_TYPE.EMERGENCY);
            }
        }
        if (imsProfile != null) {
            return (long) imsProfile.getDbrTimer();
        }
        return 20000;
    }

    public int startLocalRingBackTone(int streamType, int volume, int toneType) {
        int phoneId = -1;
        List<ImsCallSession> outgoingCalls = getSessionByState(CallConstants.STATE.OutGoingCall);
        List<ImsCallSession> alertingCalls = getSessionByState(CallConstants.STATE.AlertingCall);
        if (outgoingCalls.size() > 0) {
            Log.i(LOG_TAG, "has Outgoing call");
            phoneId = outgoingCalls.get(0).getPhoneId();
        } else if (alertingCalls.size() > 0) {
            phoneId = alertingCalls.get(0).getPhoneId();
            Log.i(LOG_TAG, "has Alerting call");
        }
        if (phoneId < 0 || phoneId > SimUtil.getPhoneCount() || !this.mPdnController.isEpdgConnected(phoneId)) {
            Log.i(LOG_TAG, "Do Not Use IMS RBT when non WiFi Calling");
            return -1;
        }
        Log.i(LOG_TAG, "Use IMS RBT when WiFi Calling");
        return this.mMediaSvcIntf.startLocalRingBackTone(streamType, volume, toneType);
    }

    public int stopLocalRingBackTone() {
        return this.mMediaSvcIntf.stopLocalRingBackTone();
    }

    public ICmcServiceHelperInternal getCmcServiceHelper() {
        return this.mCmcServiceModule;
    }

    public void transfer(int sessionId, String msisdn) {
        this.mImsExternalCallController.transfer(sessionId, msisdn);
    }

    public int[] getCallCount() {
        return this.mImsCallSessionManager.getCallCount(-1);
    }

    public int[] getCallCount(int phoneId) {
        return this.mImsCallSessionManager.getCallCount(phoneId);
    }

    public void releaseSessionByState(int phoneId, CallConstants.STATE state) {
        this.mImsCallSessionManager.releaseSessionByState(phoneId, state);
    }

    public void onConferenceParticipantAdded(int sessionId, String uri) {
    }

    public void onConferenceParticipantRemoved(int sessionId, String uri) {
    }

    public void sendRtpLossRate(int phoneId, RtpLossRateNoti noti) {
        this.mVolteNotifier.notifyOnRtpLossRate(phoneId, noti);
    }

    public String updateEccUrn(int phoneId, String dialingNumber) {
        String str;
        ImsRegistration regInfo = getImsRegistration(phoneId);
        Mno mno = SimUtil.getSimMno(phoneId);
        if (regInfo != null) {
            mno = Mno.fromName(regInfo.getImsProfile().getMnoName());
        }
        String eccCat = "";
        String eccCategoryList = updateCategoryList(phoneId);
        Log.i(LOG_TAG, "eccCategoryList : " + eccCategoryList);
        if (!TextUtils.isEmpty(eccCategoryList) && !TextUtils.isEmpty(dialingNumber)) {
            String[] split = eccCategoryList.split(",");
            int length = split.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    String str2 = dialingNumber;
                    break;
                }
                String[] splitStr = split[i].split("/");
                if (splitStr[0].equals(dialingNumber)) {
                    if (splitStr.length > 1) {
                        str = splitStr[1];
                    } else {
                        str = "";
                    }
                    eccCat = str;
                } else {
                    i++;
                }
            }
        } else {
            String str3 = dialingNumber;
        }
        if ("".equals(eccCat)) {
            return ImsCallUtil.ECC_SERVICE_URN_DEFAULT;
        }
        if (mno.isKor()) {
            return ImsCallUtil.convertEccCatToUrnSpecificKor(Integer.parseInt(eccCat));
        }
        return ImsCallUtil.convertEccCatToUrn(Integer.parseInt(eccCat));
    }

    public boolean isRegistering() {
        return isRegistering(this.mDefaultPhoneId);
    }

    public boolean isRegistering(int phoneId) {
        IRegistrationManager iRegistrationManager;
        IUserAgent ua;
        ImsRegistration regiInfo = getImsRegistration(phoneId);
        if (regiInfo != null && (iRegistrationManager = this.mRegMan) != null && (ua = iRegistrationManager.getUserAgentByRegId(regiInfo.getHandle())) != null) {
            return ua.isRegistering();
        }
        Log.i(LOG_TAG, "isRegistering: false");
        return false;
    }

    private String updateCategoryList(int phoneId) {
        String ecclistOnNet;
        String str;
        String str2;
        String str3;
        Mno mno = SimUtil.getSimMno(phoneId);
        String eccSimList = "";
        String ecclist_cdma = ImsRegistry.getString(phoneId, GlobalSettingsConstants.Call.ECC_CATEGORY_LIST_CDMA, "");
        if (phoneId <= 0) {
            ecclistOnNet = "ril.ecclist_net0";
        } else {
            ecclistOnNet = "ril.ecclist_net" + phoneId;
        }
        String eccNetList = SemSystemProperties.get(ecclistOnNet, "");
        String eccImsList = ImsRegistry.getString(phoneId, GlobalSettingsConstants.Call.ECC_CATEGORY_LIST, "");
        int i = 0;
        while (true) {
            String n = SemSystemProperties.get("ril.ecclist" + phoneId + Integer.toString(i));
            if (n.length() == 0) {
                break;
            }
            if (eccSimList.length() > 0) {
                eccSimList = eccSimList + "," + n;
            } else {
                eccSimList = eccSimList + n;
            }
            i++;
        }
        String eccCategoryList = eccImsList;
        if (eccNetList.length() > 0) {
            if ("".equals(eccCategoryList)) {
                str3 = eccNetList;
            } else {
                str3 = eccCategoryList + "," + eccNetList;
            }
            eccCategoryList = str3;
        }
        if (eccSimList.length() > 0) {
            if ("".equals(eccCategoryList)) {
                str2 = eccSimList;
            } else {
                str2 = eccCategoryList + "," + eccSimList;
            }
            eccCategoryList = str2;
        }
        if (ecclist_cdma.length() > 0) {
            if ("".equals(eccCategoryList)) {
                str = ecclist_cdma;
            } else {
                str = eccCategoryList + "," + ecclist_cdma;
            }
            eccCategoryList = str;
        }
        if (!this.mTelephonyManager.isNetworkRoaming()) {
            return eccCategoryList;
        }
        if (mno == Mno.SKT || mno == Mno.KT) {
            return "000/4,08/4,110/4,999/4,118/4," + eccCategoryList;
        } else if (mno != Mno.LGU) {
            return eccCategoryList;
        } else {
            return eccNetList + eccCategoryList;
        }
    }

    private void registerEpdgConnectionListener() {
        AnonymousClass2 r0 = new WfcEpdgManager.WfcEpdgConnectionListener() {
            public void onEpdgServiceConnected() {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "EPDG onEpdgServiceConnected");
                if (VolteServiceModuleInternal.this.mEpdgListener == null) {
                    VolteServiceModuleInternal.this.mEpdgListener = new ImsManager.EpdgListener() {
                        public void onEpdgReleaseCall(int phoneId) {
                            String str = IVolteServiceModuleInternal.LOG_TAG;
                            Log.i(str, "onEpdgReleaseCall, " + phoneId);
                            VolteServiceModuleInternal.this.sendMessage(VolteServiceModuleInternal.this.obtainMessage(20, phoneId, 0));
                        }
                    };
                }
                VolteServiceModuleInternal.this.mWfcEpdgMgr.registerEpdgHandoverListener(VolteServiceModuleInternal.this.mEpdgListener);
                for (int i = 0; i < VolteServiceModuleInternal.this.mTelephonyManager.getPhoneCount(); i++) {
                    boolean allowReleaseWfcBeforeHO = ImsRegistry.getBoolean(i, GlobalSettingsConstants.Call.ALLOW_RELEASE_WFC_BEFORE_HO, false);
                    String str = IVolteServiceModuleInternal.LOG_TAG;
                    Log.i(str, "Phone#" + i + " is allow release call " + allowReleaseWfcBeforeHO);
                    VolteServiceModuleInternal.this.mWfcEpdgMgr.getEpdgMgr().setReleaseCallBeforeHO(i, allowReleaseWfcBeforeHO);
                }
            }

            public void onEpdgServiceDisconnected() {
                Log.i(IVolteServiceModuleInternal.LOG_TAG, "WfcEpdgMgr : disconnected.");
                Arrays.fill(VolteServiceModuleInternal.this.mReleaseWfcBeforeHO, false);
                VolteServiceModuleInternal.this.mWfcEpdgMgr.unRegisterEpdgHandoverListener(VolteServiceModuleInternal.this.mEpdgListener);
                VolteServiceModuleInternal.this.mEpdgListener = null;
            }
        };
        this.mWfcEpdgConnectionListener = r0;
        this.mWfcEpdgMgr.registerWfcEpdgConnectionListener(r0);
    }

    public boolean isVowifiEnabled(int phoneId) {
        boolean isVowifiEnabled = VowifiConfig.isEnabled(this.mContext, phoneId);
        if (!this.mTelephonyManager.isNetworkRoaming() || !isVowifiEnabled) {
            return isVowifiEnabled;
        }
        boolean isVowifiEnabled2 = false;
        if (VowifiConfig.getRoamPrefMode(this.mContext, 0, phoneId) == 1) {
            isVowifiEnabled2 = true;
        }
        return isVowifiEnabled2;
    }

    public void checkCmcP2pList(ImsRegistration regInfo, CallProfile profile) {
    }

    /* access modifiers changed from: protected */
    public boolean isVolteSettingEnabled() {
        int voiceType = ImsConstants.SystemSettings.VOLTE_SLOT1.get(this.mContext, 0);
        String str = LOG_TAG;
        Log.i(str, "voiceType : " + voiceType);
        if (voiceType == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isLTEDataModeEnabled() {
        int isLTEDataMode = Settings.Secure.getInt(this.mContext.getContentResolver(), IVolteServiceModuleInternal.LTE_DATA_NETWORK_MODE, 1);
        String str = LOG_TAG;
        Log.i(str, "LTEDataMode : " + isLTEDataMode);
        if (isLTEDataMode == 1) {
            return true;
        }
        return false;
    }

    public boolean isRoaming(int phoneId) {
        if (getNetwork(phoneId) == null) {
            return false;
        }
        if (getNetwork(phoneId).isVoiceRoaming || getNetwork(phoneId).isDataRoaming) {
            return true;
        }
        return false;
    }

    public boolean isRegisteredOverLteOrNr(int phoneId) {
        ImsRegistration regiInfo = getImsRegistration(phoneId);
        if (regiInfo == null) {
            return false;
        }
        if (regiInfo.getCurrentRat() == 13 || regiInfo.getCurrentRat() == 20) {
            return true;
        }
        return false;
    }

    public boolean triggerPsRedial(int phoneId, int callId, int targetPdn) {
        return this.mImsCallSessionManager.triggerPsRedial(phoneId, callId, targetPdn, getImsRegistration(phoneId));
    }

    public void pushCallInternal() {
        this.mImsExternalCallController.pushCallInternal();
    }

    public int getLastRegiErrorCode(int phoneId) {
        return this.mLastRegiErrorCode[phoneId];
    }

    public boolean isSilentRedialEnabled(Context context, int phoneId) {
        return DmConfigHelper.readBool(context, "silent_redial", true, phoneId).booleanValue();
    }

    public IMediaServiceInterface getMediaSvcIntf() {
        return this.mMediaSvcIntf;
    }

    public CmcServiceHelper getCmcServiceModule() {
        return this.mCmcServiceModule;
    }

    public boolean isEnableCallWaitingRule() {
        return this.mEnableCallWaitingRule;
    }

    public boolean isMmtelAcquiredEver() {
        return this.mMmtelAcquiredEver;
    }

    private boolean isVideoSettingEnabled() {
        return ImsConstants.SystemSettings.VILTE_SLOT1.get(this.mContext, 0) == 0;
    }

    public boolean hasEmergencyCall(int phoneId) {
        return this.mImsCallSessionManager.hasEmergencyCall(phoneId);
    }

    public ImsCallSession getSessionByCallId(int callId) {
        return this.mImsCallSessionManager.getSessionByCallId(callId);
    }

    public ImsCallSession getSessionBySipCallId(String sipCallId) {
        return this.mImsCallSessionManager.getSessionBySipCallId(sipCallId);
    }

    public List<ImsCallSession> getSessionByState(CallConstants.STATE state) {
        return getSessionByState(-1, state);
    }

    public List<ImsCallSession> getSessionByState(int phoneId, CallConstants.STATE state) {
        return this.mImsCallSessionManager.getSessionByState(phoneId, state);
    }

    public boolean hasActiveCall(int phoneId) {
        return this.mImsCallSessionManager.hasActiveCall(phoneId);
    }

    public int getSessionCount() {
        return this.mImsCallSessionManager.getSessionCount();
    }

    public int getSessionCount(int phoneId) {
        return this.mImsCallSessionManager.getSessionCount(phoneId);
    }

    public ImsCallSession getSession(int sessionId) {
        return this.mImsCallSessionManager.getSession(sessionId);
    }

    public List<ImsCallSession> getSessionList() {
        return this.mImsCallSessionManager.getSessionList();
    }

    public List<ImsCallSession> getSessionList(int phoneId) {
        return this.mImsCallSessionManager.getSessionList(phoneId);
    }

    public ImsCallSession getForegroundSession() {
        return this.mImsCallSessionManager.getForegroundSession();
    }

    public ImsCallSession getForegroundSession(int phoneId) {
        return this.mImsCallSessionManager.getForegroundSession(phoneId);
    }

    public List<ImsCallSession> getSessionByCallType(int calltype) {
        return getSessionByCallType(-1, calltype);
    }

    public List<ImsCallSession> getSessionByCallType(int phoneId, int calltype) {
        return this.mImsCallSessionManager.getSessionByCallType(phoneId, calltype);
    }

    public boolean hasRingingCall() {
        return hasRingingCall(-1);
    }

    public boolean hasRingingCall(int phoneId) {
        return this.mImsCallSessionManager.hasRingingCall(phoneId);
    }

    public NetworkEvent getNetwork() {
        return getNetwork(this.mDefaultPhoneId);
    }

    public NetworkEvent getNetwork(int phoneId) {
        return this.mNetworks.get(Integer.valueOf(phoneId));
    }

    public IImsMediaController getImsMediaController() {
        return null;
    }

    public ICmcMediaController getCmcMediaController() {
        return null;
    }

    public void onCallModifyRequested(int sessionId) {
        String str = LOG_TAG;
        Log.i(str, "onCallModifyRequested: sessionId " + sessionId);
        ImsCallSession session = getSession(sessionId);
        if (session != null) {
            this.mVolteNotifier.notifyCallStateEvent(new CallStateEvent(CallStateEvent.CALL_STATE.MODIFY_REQUESTED), session);
        }
    }

    public void handleIntent(Intent intent) {
    }

    public boolean hasCsCall(int phoneId) {
        return hasCsCall(phoneId, false);
    }

    public boolean hasCsCall(int phoneId, boolean exceptIncomingCall) {
        boolean ret = false;
        int numPsCall = getSessionCount(phoneId);
        ImsCallSession incomingCallSession = this.mImsCallSessionManager.getIncomingCallSession();
        if (exceptIncomingCall && numPsCall == 1 && incomingCallSession != null && getSessionByCallId(incomingCallSession.getCallId()) != null) {
            Log.i(LOG_TAG, "only one PS incoming call exists");
            numPsCall = 0;
        }
        int callState = 0;
        ITelephonyManager tm = TelephonyManagerWrapper.getInstance(this.mContext);
        if (tm != null) {
            callState = tm.getCallState(phoneId);
            if (numPsCall == 0) {
                if (!SimUtil.isDualIMS()) {
                    int i = 0;
                    while (true) {
                        if (i >= SimUtil.getPhoneCount()) {
                            break;
                        } else if (tm.getCallState(i) != 0) {
                            ret = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                } else if (callState != 0) {
                    ret = true;
                }
            }
        }
        String str = LOG_TAG;
        Log.i(str, "hasCsCall: numPsCall=" + numPsCall + ", callState[" + phoneId + "]=" + callState);
        return ret;
    }

    public ContentValues getPSDataDetails(int phoneId) {
        this.mRatChanged[phoneId] = false;
        return this.mMobileCareController.getPSDataDetails(phoneId, this.mNetworks.get(Integer.valueOf(phoneId)), this.mRatChanged[phoneId]);
    }

    public void notifyProgressIncomingCall(int callId, HashMap<String, String> headers) {
        this.mVolteSvcIntf.proceedIncomingCall(this.mImsCallSessionManager.convertToSessionId(callId), headers);
    }

    public int publishDialog(int regId, String origUri, String dispName, String xmlBody, int expires) {
        Log.i(LOG_TAG, "publishDialog: ");
        return this.mVolteSvcIntf.publishDialog(regId, origUri, dispName, xmlBody, expires, false);
    }

    /* access modifiers changed from: protected */
    public void clearDialogList(int phoneId, int regId) {
        for (DialogEvent de : this.mLastDialogEvent) {
            if (de != null && de.getDialogList().size() > 0 && regId == de.getRegId()) {
                Log.i(LOG_TAG, "Match RegId clear Dialog List");
                DialogEvent emptyList = new DialogEvent(de.getMsisdn(), new ArrayList());
                emptyList.setRegId(de.getRegId());
                emptyList.setPhoneId(phoneId);
                removeMessages(15);
                sendMessage(obtainMessage(15, emptyList));
            }
        }
    }

    public String toString() {
        String ret;
        String ret2;
        String ret3 = "[";
        boolean z = false;
        if (SimUtil.isDualIMS()) {
            int i = 0;
            while (i < SimUtil.getPhoneCount()) {
                StringBuilder sb = new StringBuilder();
                sb.append(ret3);
                sb.append(i != 0 ? ", [" : "");
                String ret4 = sb.toString();
                if (isEmergencyRegistered(i)) {
                    ret2 = ret4 + "Emergency Registered - PhoneId <" + i + ">";
                } else {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(ret4);
                    sb2.append("phoneId <");
                    sb2.append(i);
                    sb2.append("> - Registered : ");
                    sb2.append(getImsRegistration(i) != null);
                    ret2 = sb2.toString();
                }
                ret3 = ret2 + " Feature: " + this.mEnabledFeatures[i] + "]";
                i++;
            }
            return ret3;
        }
        if (isEmergencyRegistered(this.mDefaultPhoneId)) {
            ret = ret3 + "Emergency Registered";
        } else {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(ret3);
            sb3.append("Registered: ");
            if (getImsRegistration() != null) {
                z = true;
            }
            sb3.append(z);
            ret = sb3.toString();
        }
        return ret + " Feature: " + this.mEnabledFeatures[this.mDefaultPhoneId] + "]";
    }

    /* access modifiers changed from: protected */
    public void terminateMoWfcWhenWfcSettingOff(int phoneId) {
        if (SimUtil.getSimMno(phoneId) == Mno.VZW && !isVowifiEnabled(phoneId) && this.mPdnController.isEpdgConnected(phoneId) && this.mTelephonyManager.getDataNetworkType(SimUtil.getSubId(phoneId)) == 13) {
            this.mImsCallSessionManager.terminateMoWfcWhenWfcSettingOff(phoneId);
        }
    }

    /* access modifiers changed from: protected */
    public void onImsCallEventForEstablish(ImsRegistration regiInfo, ImsCallSession session, CallStateEvent event) {
        ImsRegistration sdRegi;
        if (regiInfo != null) {
            int phoneId = regiInfo.getPhoneId();
            if (Mno.fromName(regiInfo.getImsProfile().getMnoName()) == Mno.VZW && !this.mRegMan.isVoWiFiSupported(phoneId) && regiInfo.getEpdgStatus() && event.getCallType() == 1) {
                String str = LOG_TAG;
                Log.i(str, "onImsCallEvent: session=" + event.getSessionID() + " releaseAllVideoCall due to the audio call");
                this.mImsCallSessionManager.releaseAllVideoCall();
            }
            if (this.mRegMan.isVoWiFiSupported(phoneId) && isVowifiEnabled(phoneId) && getCallCount(phoneId)[0] == 1) {
                WiFiManagerExt.setImsCallEstablished(this.mContext, true);
            }
            if (isCmcPrimaryType(regiInfo.getImsProfile().getCmcType())) {
                session.getCallProfile().setCmcDeviceId(event.getCmcDeviceId());
            }
            this.mCmcServiceModule.onImsCallEventWhenEstablished(phoneId, session, regiInfo);
        }
        if (session.getCmcType() == 1) {
            this.mCmcServiceModule.sendCmcCallStateForRcs(session.getPhoneId(), ImsConstants.CmcInfo.CMC_DUMMY_TEL_NUMBER, true);
        } else if (isCmcSecondaryType(session.getCmcType()) && (sdRegi = this.mCmcServiceModule.getCmcRegistration(session.getPhoneId(), false, session.getCmcType())) != null) {
            clearDialogList(session.getPhoneId(), sdRegi.getHandle());
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigUpdated(int phoneId, String item) {
        String str = LOG_TAG;
        Log.i(str, "onConfigUpdated[" + phoneId + "] : " + item);
        if ("VOLTE_ENABLED".equalsIgnoreCase(item) || "LVC_ENABLED".equalsIgnoreCase(item)) {
            onServiceSwitched(phoneId, (ContentValues) null);
        }
    }

    public EpdgManager getEpdgManager() {
        return this.mWfcEpdgMgr.getEpdgMgr();
    }

    public boolean getLteEpsOnlyAttached(int phoneId) {
        return this.mIsLteEpsOnlyAttached[phoneId];
    }

    public int getSrvccVersion(int phoneId) {
        return ImsRegistry.getInt(phoneId, GlobalSettingsConstants.Call.SRVCC_VERSION, 0);
    }

    public boolean isCallBarringByNetwork(int phoneId) {
        return ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.SS.CALLBARRING_BY_NETWORK, false);
    }

    public int getDefaultPhoneId() {
        return this.mDefaultPhoneId;
    }

    /* access modifiers changed from: protected */
    public void onSimSubscribeIdChanged(SubscriptionInfo subInfo) {
        String str = LOG_TAG;
        Log.i(str, "onSimSubscribeIdChanged, SimSlot: " + subInfo.getSimSlotIndex() + ", subId: " + subInfo.getSubscriptionId());
        int phoneId = subInfo.getSimSlotIndex();
        unRegisterPhoneStateListener(phoneId);
        registerPhoneStateListener(phoneId);
    }

    public int getMergeCallType(int phoneId, boolean isConfCallType) {
        return this.mImsCallSessionManager.getMergeCallType(phoneId, isConfCallType);
    }

    /* access modifiers changed from: protected */
    public void onSrvccStateChange(int phoneId, int srvccState) {
        Mno mno;
        String str = LOG_TAG;
        Log.i(str, "phoneId [" + phoneId + "] handleReinvite");
        ImsRegistration regInfo = getImsRegistration(phoneId);
        if (regInfo == null) {
            mno = SimUtil.getSimMno(phoneId);
        } else {
            mno = Mno.fromName(regInfo.getImsProfile().getMnoName());
        }
        if (isRunning()) {
            this.mImsCallSessionManager.handleSrvccStateChange(phoneId, srvccState, mno);
        }
    }

    private SipError getErrorCodeIncomingCallWithVolteOff(IncomingCallEvent event, Mno mno, ImsRegistration reg) {
        SipError sipError = SipErrorBase.OK;
        if (mno == Mno.VZW) {
            if (this.mMmtelAcquiredEver) {
                return this.mImsCallSessionManager.checkRejectIncomingCall(this.mContext, reg, event.getCallType());
            }
            if (this.mNetworks.get(Integer.valueOf(reg.getPhoneId())).network == 13 && this.mNetworks.get(Integer.valueOf(reg.getPhoneId())).voiceOverPs != VoPsIndication.SUPPORTED) {
                return SipErrorVzw.NOT_ACCEPTABLE_NO_VOPS;
            }
            if (this.mNetworks.get(Integer.valueOf(reg.getPhoneId())).network == 14) {
                return SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD;
            }
            if (isCallBarredBySSAC(reg.getPhoneId(), event.getCallType())) {
                return SipErrorVzw.NOT_ACCEPTABLE_SSAC_ON;
            }
            if (acceptCallWhileSmsipRegistered(reg)) {
                return SipErrorBase.OK;
            }
            if (!this.mRegMan.isVoWiFiSupported(reg.getPhoneId()) || !this.mPdnController.isEpdgConnected(reg.getPhoneId()) || isVowifiEnabled(reg.getPhoneId())) {
                return SipErrorVzw.NOT_ACCEPTABLE_VOLTE_OFF;
            }
            return SipErrorVzw.VOWIFI_OFF;
        } else if (mno == Mno.ATT) {
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        } else {
            return SipErrorBase.SERVICE_UNAVAILABLE;
        }
    }

    public boolean hasDialingOrIncomingCall() {
        if (this.mTelephonyManager.hasCall("csdialing") || this.mTelephonyManager.hasCall("csalerting") || this.mTelephonyManager.hasCall("csincoming")) {
            Log.i(LOG_TAG, "SD has already CS dialing or incoming call on SIM");
            return true;
        } else if (!this.mCmcServiceModule.hasDialingOrIncomingCall()) {
            return false;
        } else {
            Log.i(LOG_TAG, "SD has already PS dialing or incoming call on SIM");
            return true;
        }
    }

    private String getDialingNumber(IncomingCallEvent event, Mno mno) {
        String dialingNumber = ImsCallUtil.getRemoteCallerId(event.getPeerAddr(), mno, Debug.isProductShip());
        ITelephonyManager iTelephonyManager = this.mTelephonyManager;
        if (iTelephonyManager == null || iTelephonyManager.isNetworkRoaming()) {
            return dialingNumber;
        }
        if (mno == Mno.VZW || mno == Mno.USCC) {
            return ImsCallUtil.removeUriPlusPrefix(dialingNumber, Debug.isProductShip());
        }
        if (mno == Mno.KT) {
            return ImsCallUtil.removeUriPlusPrefix(dialingNumber, "+82", "0", Debug.isProductShip());
        }
        if (mno == Mno.TELENOR_MM) {
            return ImsCallUtil.removeUriPlusPrefix(dialingNumber, "+95", "0", Debug.isProductShip());
        }
        if (mno.isAus()) {
            return ImsCallUtil.removeUriPlusPrefix(dialingNumber, "+61", "0", Debug.isProductShip());
        }
        return dialingNumber;
    }

    private void setDisplayName(Mno mno, IncomingCallEvent event, CallProfile profile) {
        if (mno != Mno.AVEA_TURKEY && mno != Mno.KDDI && mno != Mno.OPTUS && mno != Mno.GLOBE_PH && !mno.isChn() && mno != Mno.TELENOR_MM) {
            String displayName = event.getPeerAddr().getDisplayName();
            if (mno.isKor() || !displayName.matches("\\+?[0-9\\-]+")) {
                if (mno == Mno.SKT || mno == Mno.ATT) {
                    displayName = displayName.replace("\\\\", "\\").replace("\\\"", "\"");
                }
                if (mno != Mno.GRAMEENPHONE) {
                    String str = LOG_TAG;
                    Log.d(str, "onImsIncomingCallEvent: displayName is different with phone number so setting extra mLetteringText" + displayName);
                    profile.setLetteringText(displayName);
                    return;
                }
                return;
            }
            Log.i(LOG_TAG, "onImsIncomingCallEvent: displayName match with phonenumber format, set as DialingNumber");
            if (!this.mTelephonyManager.isNetworkRoaming() && (mno == Mno.VZW || mno == Mno.USCC)) {
                displayName = ImsCallUtil.removeUriPlusPrefix(displayName, Debug.isProductShip());
            }
            profile.setDialingNumber(displayName);
        }
    }

    private void handlePreAlerting(ImsRegistration reg, IncomingCallEvent event, boolean isSamsungMdmnCall, boolean isDelayedIncoming, SipError error) {
        Mno mno;
        SipError error2;
        int callType;
        IncomingCallEvent incomingCallEvent = event;
        boolean z = isSamsungMdmnCall;
        Mno mno2 = Mno.fromName(reg.getImsProfile().getMnoName());
        if (z) {
            Log.i(LOG_TAG, "change mno to MDMN");
            mno = Mno.MDMN;
        } else {
            mno = mno2;
        }
        if (!hasCsCall(reg.getPhoneId()) || isDelayedIncoming) {
            CallProfile profile = new CallProfile();
            int callType2 = event.getCallType();
            SipError sipError = error;
            if (sipError == SipErrorBase.OK) {
                SipError error3 = this.mImsCallSessionManager.checkRejectIncomingCall(this.mContext, reg, callType2);
                if (error3 != SipErrorBase.OK) {
                    Log.i(LOG_TAG, "onImsIncomingCallEvent: reject call. error=" + error3);
                    this.mVolteSvcIntf.rejectCall(event.getSessionID(), event.getCallType(), error3);
                    return;
                }
                error2 = error3;
            } else {
                ImsRegistration imsRegistration = reg;
                error2 = sipError;
            }
            if (!isCmcSecondaryType(reg.getImsProfile().getCmcType()) || !hasDialingOrIncomingCall()) {
                if (mno != Mno.VZW || !ImsCallUtil.isVideoCall(callType2) || Settings.Global.getInt(this.mContext.getContentResolver(), Extensions.Settings.Global.MOBILE_DATA, 1) != 0 || reg.getEpdgStatus()) {
                    callType = callType2;
                } else {
                    Log.i(LOG_TAG, "onImsIncomingCallEvent: mobile data is off. Downgrade video call to voice call.");
                    callType = 1;
                }
                profile.setCallType(callType);
                profile.setMediaProfile(new MediaProfile());
                profile.setNetworkType(reg.getNetworkType());
                profile.setDirection(1);
                profile.setSamsungMdmnCall(z);
                profile.setDialingNumber(getDialingNumber(incomingCallEvent, mno));
                setDisplayName(mno, incomingCallEvent, profile);
                if (event.getParams().getComposerData() != null && !event.getParams().getComposerData().isEmpty()) {
                    Log.i(LOG_TAG, "onImsIncomingCallEvent: Setting composer data incoming flow");
                    profile.setComposerData(event.getParams().getComposerData());
                }
                if (isCmcPrimaryType(reg.getImsProfile().getCmcType())) {
                    profile.setCmcDeviceId(event.getParams().getCmcDeviceId());
                } else if (isCmcSecondaryType(reg.getImsProfile().getCmcType())) {
                    clearDialogList(reg.getPhoneId(), reg.getHandle());
                }
                ImsCallSession incomingCallSession = this.mImsCallSessionManager.onImsIncomingCallEvent(event, profile, reg, callType, this.mTtyMode[reg.getPhoneId()]);
                if (error2 != SipErrorBase.OK) {
                    ImsRegistry.getImsNotifier().onIncomingPreAlerting(new ImsCallInfo(incomingCallSession.getCallId(), callType, false, false, 0, 0, 0, 0, (String) null, (String) null, 0, false), event.getPeerAddr().getUri().toString());
                } else if (PackageUtils.isOneTalkFeatureEnabled(this.mContext)) {
                    int i = callType;
                } else if (this.mVolteSvcIntf.proceedIncomingCall(event.getSessionID(), (HashMap<String, String>) null) != 0) {
                    try {
                        incomingCallSession.terminate(5);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "session already removed: ", e);
                    }
                    this.mImsCallSessionManager.removeSession(event.getSessionID());
                    int i2 = callType;
                } else {
                    int i3 = callType;
                }
                this.mVolteNotifier.notifyIncomingPreAlerting(incomingCallSession);
                return;
            }
            Log.i(LOG_TAG, "onImsIncomingCallEvent: Ignore incoming CMC reley call");
            return;
        }
        Log.i(LOG_TAG, "Has Active CS Call, try after");
        sendMessageDelayed(obtainMessage(11, incomingCallEvent), 1000);
    }

    private int convertErrorToRejectReason(SipError error) {
        if (SipErrorBase.BUSY_HERE.equals(error)) {
            return 1607;
        }
        if (SipErrorBase.NOT_ACCEPTABLE_HERE.equals(error)) {
            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_RENEW_GEN_FAILURE;
        }
        if (SipErrorBase.SERVICE_UNAVAILABLE.equals(error)) {
            return 1612;
        }
        if (SipErrorVzw.BUSY_ALREADY_IN_TWO_CALLS.equals(error)) {
            return 1608;
        }
        if (SipErrorVzw.NOT_ACCEPTABLE_ACTIVE_1X_CALL.equals(error)) {
            return 1621;
        }
        if (SipErrorVzw.VOWIFI_OFF.equals(error) || SipErrorVzw.BUSY_ESTABLISHING_ANOTHER_CALL.equals(error)) {
            return 1604;
        }
        if (SipErrorVzw.TTY_ON.equals(error)) {
            return 1615;
        }
        if (SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD.equals(error) || SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD.equals(error) || SipErrorBase.USER_NOT_REGISTERED.equals(error) || SipErrorVzw.NOT_ACCEPTABLE_NO_VOPS.equals(error)) {
            return 1604;
        }
        if (SipErrorVzw.NOT_ACCEPTABLE_SSAC_ON.equals(error)) {
            return 1512;
        }
        if (SipErrorBase.OK.equals(error)) {
            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_RENEW_GEN_FAILURE;
        }
        return 0;
    }

    private int ignoreIncomingCallSession(ImsRegistration reg, ImsCallSession incomingCallSession, IncomingCallEvent event, Mno mno) {
        if (incomingCallSession == null) {
            Log.e(LOG_TAG, "onImsIncomingCallEvent: mIncomingCallSession is null");
            return 1612;
        } else if (!isCmcSecondaryType(reg.getImsProfile().getCmcType()) || !hasDialingOrIncomingCall()) {
            int phoneId = reg.getPhoneId();
            if (!hasCsCall(reg.getPhoneId(), true)) {
                return 0;
            }
            if ((!mno.isKor() || ImsCallUtil.isSamsungFmcConnected()) && mno != Mno.TMOUS && mno != Mno.SPRINT) {
                return 0;
            }
            Log.i(LOG_TAG, "need to reject incoming call.. due to CS Call");
            this.mVolteSvcIntf.rejectCall(event.getSessionID(), event.getCallType(), SipErrorBase.BUSY_HERE);
            return 1603;
        } else {
            Log.i(LOG_TAG, "onImsIncomingCallEvent: Ignore incoming CMC reley call");
            return 1602;
        }
    }

    private void setProfileForIncomingCallSession(ImsCallSession incomingCallSession, IncomingCallEvent event, Mno mno, Boolean isSamsungMdmnCall, int error) {
        if (mno != Mno.VZW && ImsCallUtil.isVideoCall(event.getCallType())) {
            this.mMediaController.stopActiveCamera();
        }
        CallProfile profile = incomingCallSession.getCallProfile();
        profile.setCallType(event.getCallType());
        profile.setRemoteVideoCapa(event.getRemoteVideoCapa());
        incomingCallSession.updateCallProfile(event.getParams());
        incomingCallSession.startIncoming();
        String replaces = event.getParams().getReplaces();
        if (isSamsungMdmnCall.booleanValue() && !TextUtils.isEmpty(replaces)) {
            Log.i(LOG_TAG, "Has replaces. Check Dialog Id");
            String str = LOG_TAG;
            Log.i(str, "replaceSipCallId: " + replaces);
            profile.setReplaceSipCallId(replaces);
        }
        String notifyHistoryInfo = event.getImsRegistration().getImsProfile().getNotifyHistoryInfo();
        if ((profile.getHistoryInfo() != null || profile.getHasDiversion()) && !"not_notify".equals(notifyHistoryInfo)) {
            if (profile.getHistoryInfo() == null && profile.getHasDiversion()) {
                profile.setHistoryInfo("");
            } else if ("change_number".equals(notifyHistoryInfo)) {
                profile.setDialingNumber(profile.getHistoryInfo());
                profile.setHistoryInfo("");
            } else if ("toast_only".equals(notifyHistoryInfo)) {
                profile.setHistoryInfo("");
            }
        } else if (mno != Mno.DOCOMO) {
            profile.setHistoryInfo((String) null);
        }
    }

    /* access modifiers changed from: protected */
    public void onImsIncomingCallEvent(IncomingCallEvent event, boolean isDelayedIncoming) {
        Mno mno;
        SipError error;
        int ignoredError;
        IncomingCallEvent incomingCallEvent = event;
        Log.i(LOG_TAG, "onImsIncomingCallEvent : sessionId=" + event.getSessionID() + " peerURI=" + IMSLog.checker(event.getPeerAddr() + "") + " param=" + event.getParams() + " type=" + event.getCallType() + "isDelayedIncoming=" + isDelayedIncoming);
        ImsRegistration reg = event.getImsRegistration();
        SipError error2 = SipErrorBase.OK;
        if (reg == null) {
            Log.e(LOG_TAG, "Not registered.");
            this.mVolteSvcIntf.rejectCall(event.getSessionID(), event.getCallType(), SipErrorBase.NOT_ACCEPTABLE_HERE);
            return;
        }
        if (getSession(event.getSessionID()) != null) {
            CallConstants.STATE sessionState = getSession(event.getSessionID()).getCallState();
            if (sessionState == CallConstants.STATE.IncomingCall) {
                Log.e(LOG_TAG, "same session exist.");
                return;
            } else if (sessionState == CallConstants.STATE.EndingCall || sessionState == CallConstants.STATE.EndedCall) {
                Log.e(LOG_TAG, "session is already Ending or Ended state");
                return;
            }
        }
        Mno mno2 = Mno.fromName(reg.getImsProfile().getMnoName());
        boolean isSamsungMdmnCall = reg.getImsProfile().isSamsungMdmnEnabled();
        if (isSamsungMdmnCall) {
            mno = Mno.MDMN;
        } else {
            mno = mno2;
        }
        if (!isRunning() || getRegInfo(reg.getHandle()) == null) {
            Log.e(LOG_TAG, "onImsNewIncomingCallEvent: Unexpected incoming call while volte is off.");
            SipError error3 = getErrorCodeIncomingCallWithVolteOff(incomingCallEvent, mno, reg);
            if (error3 != SipErrorBase.OK) {
                this.mVolteSvcIntf.rejectCall(event.getSessionID(), event.getCallType(), error3);
                return;
            }
            error = error3;
        } else {
            error = error2;
        }
        Log.i(LOG_TAG, "getPreAlerting is " + event.getPreAlerting());
        if (event.getPreAlerting()) {
            handlePreAlerting(reg, event, isSamsungMdmnCall, isDelayedIncoming, error);
        } else {
            ImsCallSession incomingCallSession = this.mImsCallSessionManager.getIncomingCallSession();
            if (error == SipErrorBase.OK) {
                ignoredError = ignoreIncomingCallSession(reg, incomingCallSession, incomingCallEvent, mno);
            } else {
                ignoredError = 0;
            }
            int phoneId = reg.getPhoneId();
            setProfileForIncomingCallSession(incomingCallSession, event, mno, Boolean.valueOf(isSamsungMdmnCall), ignoredError);
            ImsProfile imsProfile = event.getImsRegistration().getImsProfile();
            if (ignoredError == 0) {
                Log.i(LOG_TAG, "onImsIncomingCallEvent getCmcType : " + imsProfile.getCmcType());
                if (!isCmcPrimaryType(imsProfile.getCmcType())) {
                    this.mVolteNotifier.notifyOnIncomingCall(phoneId, incomingCallSession.getCallId());
                }
                post(new Runnable(incomingCallSession) {
                    public final /* synthetic */ ImsCallSession f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VolteServiceModuleInternal.this.lambda$onImsIncomingCallEvent$0$VolteServiceModuleInternal(this.f$1);
                    }
                });
                this.mCmcServiceModule.onImsIncomingCallEventWithSendPublish(phoneId, incomingCallSession.getCmcType());
                ImsRegistry.getImsNotifier().onIncomingCall(phoneId, incomingCallSession.getCallId());
            } else {
                return;
            }
        }
        if (reg.getImsProfile().getCmcType() > 0) {
            checkCmcP2pList(reg, (CallProfile) null);
        }
    }

    public /* synthetic */ void lambda$onImsIncomingCallEvent$0$VolteServiceModuleInternal(ImsCallSession incomingCallSession) {
        this.mVolteNotifier.notifyIncomingCallEvent(incomingCallSession);
    }

    public void setDelayedDeregisterTimerRunning(int phoneId, boolean deregiTimerRunning) {
        this.mIsDeregisterTimerRunning[phoneId] = deregiTimerRunning;
        updateCapabilities(phoneId);
    }

    private class PhoneStateListenerInternal extends PhoneStateListener {
        int mPhoneId;
        int mState = 0;
        int mSubId;

        public PhoneStateListenerInternal(int phoneId, int subId) {
            this.mSubId = subId;
            this.mPhoneId = phoneId;
        }

        public int getInternalPhoneId() {
            return this.mPhoneId;
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            VolteServiceModuleInternal.this.mMobileCareController.onSignalStrengthsChanged(this.mPhoneId, signalStrength);
        }

        public void onServiceStateChanged(ServiceState state) {
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "onServiceStateChanged(" + state + ")");
            ServiceStateWrapper serviceState = new ServiceStateWrapper(state);
            Mno mno = SimUtil.getSimMno(this.mPhoneId);
            boolean z = false;
            if (mno == Mno.TMOUS) {
                if (!VolteServiceModuleInternal.this.mPdnController.isEpdgConnected(this.mPhoneId) && serviceState.getVoiceRegState() == 0 && serviceState.getDataRegState() == 1 && serviceState.getVoiceNetworkType() == 16 && serviceState.getDataNetworkType() == 0) {
                    VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                    volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(10, this.mPhoneId, 0));
                }
            } else if ((mno == Mno.TELSTRA || mno == Mno.KDDI || mno.isKor() || mno.isTw()) && !VolteServiceModuleInternal.this.mPdnController.isEpdgConnected(this.mPhoneId) && serviceState.getVoiceRegState() == 1 && serviceState.getDataRegState() == 1 && serviceState.getDataNetworkType() == 0) {
                VolteServiceModuleInternal volteServiceModuleInternal2 = VolteServiceModuleInternal.this;
                volteServiceModuleInternal2.sendMessage(volteServiceModuleInternal2.obtainMessage(10, this.mPhoneId, 0));
            }
            boolean[] zArr = VolteServiceModuleInternal.this.mIsLteEpsOnlyAttached;
            int i = this.mPhoneId;
            if (serviceState.getDataRegState() == 0 && serviceState.getDataNetworkType() == 13 && serviceState.isPsOnlyReg()) {
                z = true;
            }
            zArr[i] = z;
            Log.i(IVolteServiceModuleInternal.LOG_TAG, "mIsLteEpsOnlyAttached(" + this.mPhoneId + "):" + VolteServiceModuleInternal.this.mIsLteEpsOnlyAttached[this.mPhoneId]);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            if (this.mState != state) {
                String str = IVolteServiceModuleInternal.LOG_TAG;
                Log.i(str, "onCallStateChanged: state " + state);
                this.mState = state;
                VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(5, this.mPhoneId, state));
            }
        }

        public void onSrvccStateChanged(int srvccState) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "onSrvccStateChanged: state " + srvccState);
            VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
            volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(27, this.mPhoneId, 0, Integer.valueOf(srvccState)));
        }

        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState state) {
            String str = IVolteServiceModuleInternal.LOG_TAG;
            Log.i(str, "onPreciseDataConnectionStateChanged: state=" + state);
            if (state != null && state.getDataConnectionState() == -1) {
                int failCause = state.getDataConnectionFailCause();
                if ((state.getDataConnectionApnTypeBitMask() & 512) == 512 && failCause != 0) {
                    Log.i(IVolteServiceModuleInternal.LOG_TAG, "ePDN setup failed");
                    VolteServiceModuleInternal volteServiceModuleInternal = VolteServiceModuleInternal.this;
                    volteServiceModuleInternal.sendMessage(volteServiceModuleInternal.obtainMessage(19, this.mPhoneId, 0));
                }
            }
        }
    }

    public boolean isCmcPrimaryType(int cmcType) {
        if (cmcType == 1 || cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    public boolean isCmcSecondaryType(int cmcType) {
        if (cmcType == 2 || cmcType == 4 || cmcType == 8) {
            return true;
        }
        return false;
    }
}
