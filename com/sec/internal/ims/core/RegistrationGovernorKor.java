package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Message;
import android.os.SemSystemProperties;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.SipError;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorKor;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.CscFeatureTagIMS;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImModule;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.DmProfileLoader;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class RegistrationGovernorKor extends RegistrationGovernorBase {
    private static final Long DEFAULT_RETRY_AFTER_BUFFER_MS = 500L;
    private static final int DEFAULT_TIMS_TIMER = 60;
    private static final int DNS_RETRY_TIME = 4000;
    private static final int IMS_NOT_AVAILABLE_REG_FAIL_RETRY = 2;
    protected static final String INTENT_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
    protected static final String INTENT_USIMDOWNLOAD_END = "com.sec.android.UsimRegistrationKOR.UsimDownload.end";
    protected static final String INTENT_WAP_PUSH_DM_NOTI_RECEIVED = "com.samsung.provider.Telephony.WAP_PUSH_DM_NOTI_RECEIVED";
    protected static final String LTE_DATA_NETWORK_MODE = "lte_mode_on";
    private static final int MAX_REQUESTPDN_COUNT = 5;
    private static final String OMADM_KT_DEFAULT_PCSCF = "volte.imskt.com";
    private static final int REG_RETRY_MAX_TIME_FOR_UNLIMITED_404 = 14400;
    private static final int REQUESTPDN_INTERVAL = 3;
    private static final int REQUEST_INTERNETPDN_TIMER = 30;
    /* access modifiers changed from: private */
    public String LOG_TAG = null;
    private int mConsecutiveForbiddenCounter = 0;
    protected String mCscConfigVerUiccMobilitySpec = "";
    protected boolean mCscSupportLTEPreferred = false;
    protected boolean mCscSupportOmdVolteRoaming = false;
    private Message mDmPollingTimer = null;
    private boolean mDmUpdatedFlag = false;
    private int mDnsQueryCount = 0;
    private boolean mHasNetworkFailure = false;
    protected boolean mHasPendingInitRegistrationByDMConfigChange = false;
    protected boolean mHasPendingNotifyImsNotAvailable = false;
    protected BroadcastReceiver mIntentReceiverKor;
    private boolean mIpsecEnabled = true;
    private boolean mIsAkaChallengeTimeout = false;
    protected boolean mIsShipBuild = false;
    private List<InetAddress> mLocalAddress = null;
    private boolean mNeedDelayedDeregister = false;
    protected Message mPDNdisconnectTimeout = null;
    private List<String> mPcscfList;
    private List<String> mRcsPcscfList;
    private int mRequestPdnTimeoutCount = 0;
    private boolean mSmsOverIp = false;
    private boolean mVolteServiceStatus = true;

    protected enum VoltePreferenceChangedReason {
        VOLTE_SETTING,
        LTE_MODE
    }

    public RegistrationGovernorKor(Context ctx) {
        this.mContext = ctx;
        setBotAgreementToFile(0);
    }

    public RegistrationGovernorKor(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        if (task.isRcsOnly()) {
            this.LOG_TAG = "RegiGvnKor-RCS";
        } else if (this.mTask.getProfile().hasEmergencySupport()) {
            this.LOG_TAG = "RegiGvnKor-EMC";
        } else {
            this.LOG_TAG = "RegiGvnKor";
        }
        this.mDmUpdatedFlag = false;
        this.mVolteServiceStatus = getVolteServiceStatus();
        this.mDnsQueryCount = 0;
        this.mPcscfList = new ArrayList();
        this.mRcsPcscfList = new ArrayList();
        this.mIpsecEnabled = true;
        this.mSmsOverIp = false;
        this.mIsShipBuild = CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));
        this.mCscSupportLTEPreferred = SemCscFeature.getInstance().getBoolean("CscFeature_COMMON_SupportLTEPreferred", false);
        this.mCscSupportOmdVolteRoaming = SemCscFeature.getInstance().getBoolean("CscFeature_Common_SupportOmdVolteRoaming", false);
        this.mCscConfigVerUiccMobilitySpec = SemCscFeature.getInstance().getString(this.mPhoneId, CscFeatureTagIMS.TAG_CSCFEATURE_IMS_CONFIGVERUICCMOBILITYSPEC, "2.0");
        String str = this.LOG_TAG;
        Log.i(str, "mCscSupportLTEPreferred(" + this.mCscSupportLTEPreferred + ")mCscSupportOmdVolteRoaming(" + this.mCscSupportOmdVolteRoaming + ")mCscConfigVerUiccMobilitySpec(" + this.mCscConfigVerUiccMobilitySpec + ")");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ImsConstants.Intents.ACTION_PERIODIC_POLLING_TIMEOUT);
        filter.addAction(ImsConstants.Intents.ACTION_FLIGHT_MODE);
        filter.addAction(ImsConstants.Intents.ACTION_AIRPLANE_MODE);
        filter.addAction(INTENT_USIMDOWNLOAD_END);
        filter.addAction("com.samsung.intent.action.SETTINGS_SOFT_RESET");
        filter.addAction(ImsConstants.Intents.INTENT_ACTION_REGIST_REJECT);
        filter.addAction(ImsConstants.Intents.INTENT_ACTION_LTE_REJECT);
        filter.addAction(INTENT_WAP_PUSH_DM_NOTI_RECEIVED);
        filter.addAction(INTENT_NEW_OUTGOING_CALL);
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        Log.i(this.LOG_TAG, "intent added");
        this.mIntentReceiverKor = new BroadcastReceiver() {
            /* JADX WARNING: Can't fix incorrect switch cases order */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r4, android.content.Intent r5) {
                /*
                    r3 = this;
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    java.lang.String r0 = r0.LOG_TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "onReceive:"
                    r1.append(r2)
                    java.lang.String r2 = r5.getAction()
                    r1.append(r2)
                    java.lang.String r2 = " mTask:"
                    r1.append(r2)
                    com.sec.internal.ims.core.RegistrationGovernorKor r2 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    com.sec.internal.ims.core.RegisterTask r2 = r2.mTask
                    com.sec.ims.settings.ImsProfile r2 = r2.getProfile()
                    java.lang.String r2 = r2.getName()
                    r1.append(r2)
                    java.lang.String r2 = "("
                    r1.append(r2)
                    com.sec.internal.ims.core.RegistrationGovernorKor r2 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    com.sec.internal.ims.core.RegisterTask r2 = r2.mTask
                    com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = r2.getState()
                    r1.append(r2)
                    java.lang.String r2 = ")"
                    r1.append(r2)
                    java.lang.String r1 = r1.toString()
                    android.util.Log.i(r0, r1)
                    java.lang.String r0 = r5.getAction()
                    int r1 = r0.hashCode()
                    switch(r1) {
                        case -1822432213: goto L_0x00b1;
                        case -1386427285: goto L_0x00a7;
                        case -1115331537: goto L_0x009d;
                        case -1076576821: goto L_0x0093;
                        case -1065317266: goto L_0x0089;
                        case -909058917: goto L_0x007f;
                        case -632711962: goto L_0x0075;
                        case 798292259: goto L_0x006a;
                        case 1402086673: goto L_0x0060;
                        case 1901012141: goto L_0x0055;
                        default: goto L_0x0053;
                    }
                L_0x0053:
                    goto L_0x00bb
                L_0x0055:
                    java.lang.String r1 = "android.intent.action.NEW_OUTGOING_CALL"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 8
                    goto L_0x00bc
                L_0x0060:
                    java.lang.String r1 = "com.sec.internal.ims.imsservice.dm_polling_timeout"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 0
                    goto L_0x00bc
                L_0x006a:
                    java.lang.String r1 = "android.intent.action.BOOT_COMPLETED"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 9
                    goto L_0x00bc
                L_0x0075:
                    java.lang.String r1 = "android.intent.action.LTE_REJECT"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 6
                    goto L_0x00bc
                L_0x007f:
                    java.lang.String r1 = "com.sec.android.UsimRegistrationKOR.UsimDownload.end"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 3
                    goto L_0x00bc
                L_0x0089:
                    java.lang.String r1 = "com.samsung.provider.Telephony.WAP_PUSH_DM_NOTI_RECEIVED"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 7
                    goto L_0x00bc
                L_0x0093:
                    java.lang.String r1 = "android.intent.action.AIRPLANE_MODE"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 2
                    goto L_0x00bc
                L_0x009d:
                    java.lang.String r1 = "com.samsung.intent.action.SETTINGS_SOFT_RESET"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 4
                    goto L_0x00bc
                L_0x00a7:
                    java.lang.String r1 = "com.android.server.status.regist_reject"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 5
                    goto L_0x00bc
                L_0x00b1:
                    java.lang.String r1 = "com.sec.android.internal.ims.FLIGHT_MODE"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0053
                    r0 = 1
                    goto L_0x00bc
                L_0x00bb:
                    r0 = -1
                L_0x00bc:
                    switch(r0) {
                        case 0: goto L_0x00f0;
                        case 1: goto L_0x00ea;
                        case 2: goto L_0x00e4;
                        case 3: goto L_0x00de;
                        case 4: goto L_0x00d8;
                        case 5: goto L_0x00d2;
                        case 6: goto L_0x00d2;
                        case 7: goto L_0x00cc;
                        case 8: goto L_0x00c6;
                        case 9: goto L_0x00c0;
                        default: goto L_0x00bf;
                    }
                L_0x00bf:
                    goto L_0x00f6
                L_0x00c0:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleBootCompletedIntent()
                    goto L_0x00f6
                L_0x00c6:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleNewOutgoingCallIntent()
                    goto L_0x00f6
                L_0x00cc:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleWapPushDmNotiReceivedIntent()
                    goto L_0x00f6
                L_0x00d2:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleNwRejectIntent(r5)
                    goto L_0x00f6
                L_0x00d8:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleSoftResetIntent()
                    goto L_0x00f6
                L_0x00de:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleUsimDownloadEndIntent()
                    goto L_0x00f6
                L_0x00e4:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleAirplaneModeIntent(r5)
                    goto L_0x00f6
                L_0x00ea:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handleFlightModeIntent(r5)
                    goto L_0x00f6
                L_0x00f0:
                    com.sec.internal.ims.core.RegistrationGovernorKor r0 = com.sec.internal.ims.core.RegistrationGovernorKor.this
                    r0.handlePeriodicPollingTimeoutIntent()
                L_0x00f6:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
            }
        };
        this.mContext.registerReceiver(this.mIntentReceiverKor, filter);
        updateVolteRoamingSetting();
        updateEutranValues();
    }

    private void checkUnprocessedOmadmConfig() {
        NetworkEvent event;
        if (this.mRegMan != null && this.mTask.isNeedOmadmConfig() && OmcCode.isKOROmcCode() && (event = this.mRegMan.getNetworkEvent(this.mPhoneId)) != null && !event.isDataRoaming) {
            Log.i(this.LOG_TAG, "checkUnprocessedOmadmConfig");
            this.mRegHandler.sendCheckUnprocessedOmadmConfig();
        }
    }

    /* access modifiers changed from: protected */
    public void updateVolteRoamingSetting() {
        int roamingHDVoiceOn = ImsConstants.SystemSettings.VOLTE_ROAMING.get(this.mContext, ImsConstants.SystemSettings.VOLTE_ROAMING_UNKNOWN);
        if ((this.mMno == Mno.SKT || this.mMno == Mno.KT) && roamingHDVoiceOn == ImsConstants.SystemSettings.VOLTE_ROAMING_UNKNOWN) {
            ImsConstants.SystemSettings.VOLTE_ROAMING.set(this.mContext, ImsConstants.SystemSettings.VOLTE_ROAMING_ENABLED);
            Log.i(this.LOG_TAG, "RegistrationManagerBase: roamingHDVoiceOn has no value. set default value as 1 in the first place");
            roamingHDVoiceOn = ImsConstants.SystemSettings.VOLTE_ROAMING.get(this.mContext, ImsConstants.SystemSettings.VOLTE_ROAMING_UNKNOWN);
        }
        String str = this.LOG_TAG;
        Log.i(str, "RegistrationManagerBase: roamingHDVoiceOn [" + roamingHDVoiceOn + "]");
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        this.mFailureCounter = 0;
        this.mConsecutiveForbiddenCounter = 0;
        this.mIsAkaChallengeTimeout = false;
        this.mDnsQueryCount = 0;
        stopPDNdisconnectTimer();
        stopRetryTimer();
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) 1) * 1000);
        startRetryTimer(((long) 1) * 1000);
        this.mDnsQueryCount = 0;
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(int retryAfter) {
        int i = this.mConsecutiveForbiddenCounter + 1;
        this.mConsecutiveForbiddenCounter = i;
        if (i >= 2) {
            this.mRegMan.getEventLog().logAndAdd("onRegistrationError: Two consecutive 403 errors");
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
            resetIPSecAllow();
            this.mIsPermanentStopped = true;
            makeRegistrationFailedToast();
            return;
        }
        Log.i(this.LOG_TAG, "onRegistrationError: 403 error. Need OmaDM trial only for KOR device in domestic");
        if (this.mTask.isNeedOmadmConfig()) {
            if (!OmcCode.isKOROmcCode() || this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming) {
                this.mRegHandler.sendTryRegister(this.mPhoneId);
            } else {
                this.mRegHandler.sendRequestDmConfig();
            }
        }
        if (this.mTask.getProfile().getNeedAutoconfig()) {
            this.mConfigModule.startAcs(this.mPhoneId);
        }
        if (!this.mTask.isNeedOmadmConfig() && !this.mTask.getProfile().getNeedAutoconfig()) {
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        }
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped + " mTask.mIsRefreshReg " + this.mTask.isRefreshReg());
        boolean mIsTimeout = false;
        boolean mIsRefreshReg = this.mTask.isRefreshReg();
        this.mTask.setDeregiReason(41);
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (!needImsNotAvailable() || (needImsNotAvailable() && retryAfter > 0)) {
            stopTimsTimer(RegistrationConstants.REASON_REGISTRATION_ERROR);
        }
        if (SipErrorBase.OK.equals(error)) {
            handleNormalResponse(error, retryAfter);
        } else if (SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error)) {
            Log.e(this.LOG_TAG, "onRegistrationError: Notify terminated expired.");
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        } else if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
            resetIPSecAllow();
            this.mRegMan.getEventLog().logAndAdd("onRegistrationError: Notify terminated rejected.");
            this.mIsPermanentStopped = true;
            this.mDnsQueryCount = 0;
        } else if (SipErrorKor.AKA_CHANLENGE_TIMEOUT.equals(error)) {
            Log.e(this.LOG_TAG, "onRegistrationError: Permanently prohibited.");
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = true;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
            resetIPSecAllow();
            this.mTask.setDeregiReason(71);
            this.mRegMan.getEventLog().logAndAdd("onRegistrationError: Aka challenge timeout");
            this.mIsPermanentStopped = true;
            resetPcscfList();
            this.mRegMan.deregister(this.mTask, true, true, "Aka challenge timeout");
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mDnsQueryCount = 0;
        } else if (needImsNotAvailable()) {
            onRegErrorforImsNotAvailable(error, retryAfter);
        } else if (SipErrorBase.USE_PROXY.equals(error) && this.mTask.isRefreshReg()) {
            if (this.mCurPcscfIpIdx + 1 == this.mNumOfPcscfIp) {
                retryAfter = 1;
                if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && !this.mTask.isRcsOnly()) {
                    mIsTimeout = true;
                    startPDNdisconnectTimer(((long) 1) * 1000);
                }
                this.mCurPcscfIpIdx = 0;
                this.mFailureCounter = 0;
                this.mConsecutiveForbiddenCounter = 0;
                this.mIsAkaChallengeTimeout = false;
                this.mDnsQueryCount = 0;
                resetIPSecAllow();
                Log.i(this.LOG_TAG, "onRegistrationError: 305 error. do initial regi. at the 1st P-CSCF after disconnecting/connecting IMS PDN");
            } else {
                moveToNextPcscfAndInitialRegister();
                Log.i(this.LOG_TAG, "onRegistrationError: 305 error. do initial regi. at the next P-CSCF");
            }
            if (!mIsTimeout) {
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                startRetryTimer(((long) retryAfter) * 1000);
            }
        } else if (SipErrorBase.isImsForbiddenError(error)) {
            handleForbiddenError(retryAfter);
        } else if (SipErrorBase.NOT_ACCEPTABLE.equals(error)) {
            Log.i(this.LOG_TAG, "onRegistrationError: 406 error. Ipsec not allow");
            this.mIPsecAllow = false;
            if (retryAfter > 0) {
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                startRetryTimer(((long) retryAfter) * 1000);
                return;
            }
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        } else if (!SipErrorBase.SERVICE_UNAVAILABLE.equals(error) || retryAfter > 0 || mIsRefreshReg) {
            Log.i(this.LOG_TAG, "onRegistrationError: etc mIsRefreshReg");
            if (mIsRefreshReg) {
                Log.i(this.LOG_TAG, "onRegistrationError: etc mIsRefreshReg");
                if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && !this.mTask.isRcsOnly()) {
                    mIsTimeout = true;
                    notifyReattachToRil();
                    this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                }
                this.mCurPcscfIpIdx = 0;
                this.mFailureCounter = 0;
                this.mConsecutiveForbiddenCounter = 0;
                this.mIsAkaChallengeTimeout = false;
                this.mDnsQueryCount = 0;
                resetIPSecAllow();
                if (retryAfter <= 0) {
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                } else if (!mIsTimeout) {
                    this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                    startRetryTimer(((long) retryAfter) * 1000);
                }
            } else {
                this.mCurPcscfIpIdx++;
                if (this.mNumOfPcscfIp >= 2) {
                    resetIPSecAllow();
                }
                if (this.mCurPcscfIpIdx == Math.max(2, this.mNumOfPcscfIp)) {
                    this.mCurPcscfIpIdx = 0;
                    if (SipErrorBase.NOT_FOUND.equals(error) && needToHandleUnlimited404()) {
                        retryAfter = getActualWaitTimeForUnlimited404();
                        Log.d(this.LOG_TAG, "it would be infinite 404 response. " + retryAfter);
                    }
                    this.mFailureCounter++;
                    if (retryAfter > 0) {
                        Log.i(this.LOG_TAG, "onRegistrationError: retryAfter from SIP header");
                    } else {
                        retryAfter = getActualWaitTime();
                    }
                    if (SipErrorBase.SIP_TIMEOUT.equals(error) && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                        if (!this.mTask.isRcsOnly()) {
                            mIsTimeout = true;
                            startPDNdisconnectTimer(((long) retryAfter) * 1000);
                        } else {
                            this.mConfigModule.getAcsConfig(this.mPhoneId).setForceAcs(true);
                        }
                    }
                    Log.i(this.LOG_TAG, "onRegistrationError: retry at the 1st P-CSCF in " + retryAfter + " seconds.");
                }
                if (retryAfter <= 0) {
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                } else if (!mIsTimeout) {
                    this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                    startRetryTimer(((long) retryAfter) * 1000);
                }
                this.mConsecutiveForbiddenCounter = 0;
                this.mIsAkaChallengeTimeout = false;
                this.mDnsQueryCount = 0;
            }
        } else {
            Log.i(this.LOG_TAG, "onRegistrationError: 503 error with no retry-after. do initial regi.");
            if (this.mCurPcscfIpIdx + 1 == this.mNumOfPcscfIp) {
                if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && !this.mTask.isRcsOnly()) {
                    startPDNdisconnectTimer(((long) 1) * 1000);
                    mIsTimeout = true;
                }
                resetIPSecAllow();
                this.mDnsQueryCount = 0;
                this.mCurPcscfIpIdx = 0;
                this.mConsecutiveForbiddenCounter = 0;
                this.mIsAkaChallengeTimeout = false;
                this.mFailureCounter = 0;
                Log.i(this.LOG_TAG, "onRegistrationError: 503 error with no retry-after. do initial regi. at the 1st P-CSCF after disconnecting/connecting IMS PDN");
            } else {
                moveToNextPcscfAndInitialRegister();
                Log.i(this.LOG_TAG, "onRegistrationError: 503 error with no retry-after. do initial regi. at the next P-CSCF");
            }
            if (!mIsTimeout) {
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) 1) * 1000);
                startRetryTimer(((long) 1) * 1000);
            }
        }
    }

    private void onRegErrorforImsNotAvailable(SipError error, int retryAfter) {
        Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable:");
        boolean mIsRefreshReg = this.mTask.isRefreshReg();
        boolean mNeedNotifyImsNotAvailable = false;
        this.mFailureCounter++;
        this.mTask.setDeregiReason(41);
        if (getCallStatus() != 0 && mIsRefreshReg) {
            this.mCurPcscfIpIdx = 0;
            this.mFailureCounter = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            resetIPSecAllow();
            this.mHasPendingNotifyImsNotAvailable = true;
            Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable(Postpone notifyImsNotAvailable during call)");
        } else if (SipErrorBase.INTERVAL_TOO_BRIEF.equals(error)) {
            this.mCurPcscfIpIdx = 0;
            this.mFailureCounter = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            resetIPSecAllow();
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
            this.mRegMan.getEventLog().logAndAdd("onRegErrorforImsNotAvailable(423)");
        } else if (SipErrorBase.NOT_ACCEPTABLE.equals(error)) {
            Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: 406 error. Ipsec not allow");
            this.mIPsecAllow = false;
            this.mFailureCounter = 0;
            if (retryAfter > 0) {
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                startRetryTimer(((long) retryAfter) * 1000);
                return;
            }
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        } else {
            Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: ETC error");
            if (!mIsRefreshReg) {
                this.mCurPcscfIpIdx++;
            }
            if (!mIsRefreshReg && this.mNumOfPcscfIp >= 2) {
                resetIPSecAllow();
            }
            if (this.mFailureCounter == 2) {
                mNeedNotifyImsNotAvailable = true;
            }
            if (mNeedNotifyImsNotAvailable) {
                this.mCurPcscfIpIdx = 0;
                this.mFailureCounter = 0;
                resetIPSecAllow();
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                this.mRegMan.notifyImsNotAvailable(this.mTask, true);
                this.mRegMan.getEventLog().logAndAdd("onRegErrorforImsNotAvailable(ETC)");
            } else if (!mIsRefreshReg) {
                if (retryAfter > 0) {
                    this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                    startRetryTimer(((long) retryAfter) * 1000);
                } else {
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                }
                Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: ETC error. Initial Reg retry");
            } else {
                if (retryAfter == 0) {
                    retryAfter = 1;
                }
                this.mTask.mKeepPdn = true;
                Log.i(this.LOG_TAG, "onRegErrorforImsNotAvailable: ETC error. Refresh Reg retry with same Call-ID");
                this.mRegHandler.sendUpdateRegistration(this.mTask.getProfile(), this.mPhoneId, (((long) retryAfter) * 1000) - DEFAULT_RETRY_AFTER_BUFFER_MS.longValue());
            }
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
        }
    }

    public void onRegistrationDone() {
        Log.i(this.LOG_TAG, "onRegistrationDone: clear mConsecutiveForbiddenCounter.");
        this.mFailureCounter = 0;
        this.mConsecutiveForbiddenCounter = 0;
        this.mIsAkaChallengeTimeout = false;
        this.mRegiAt = 0;
        this.mDnsQueryCount = 0;
        stopPDNdisconnectTimer();
        stopRetryTimer();
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    public void onSubscribeError(int event, SipError error) {
        String str = this.LOG_TAG;
        Log.i(str, "onSubscribeError: state " + this.mTask.getState() + " error " + error + ", event " + event);
        if (event == 0 && error.equals(new SipError(Id.REQUEST_IM_SENDMSG, "Subscribe 504 with init-regi"))) {
            Log.e(this.LOG_TAG, "onSubscribeError: SUBSCRIBE 504 with init regi.");
            this.mTask.setDeregiReason(44);
            this.mRegMan.deregister(this.mTask, true, true, "SUBSCRIBE 504 with init regi.");
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mConsecutiveForbiddenCounter = 0;
            this.mIsAkaChallengeTimeout = false;
            this.mDnsQueryCount = 0;
            stopPDNdisconnectTimer();
            stopRetryTimer();
        }
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent event, SipError error, int callType) {
        String str = this.LOG_TAG;
        Log.i(str, "onCallStatus: event=" + event + " error=" + error);
        if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END && this.mTask.getState() == RegistrationConstants.RegisterTaskState.IDLE) {
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        }
        super.onCallStatus(event, error, callType);
    }

    public SipError onSipError(String service, SipError error) {
        Log.i(this.LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error)) {
            removeCurrentPcscfAndInitialRegister(true);
            if (needImsNotAvailable()) {
                Log.i(this.LOG_TAG, "onSipError: 709 error. Initial Reg at the next P-CSCF");
                this.mFailureCounter++;
            }
        } else if (!SipErrorBase.FORBIDDEN.equals(error)) {
            ImsProfile profile = this.mTask.getProfile();
            if (SipErrorBase.NOT_ACCEPTABLE.equals(error) && (profile.hasService("mmtel") || profile.hasService("mmtel-video"))) {
                Log.e(this.LOG_TAG, "onSipError: 406 error. Ipsec not allow");
                this.mIPsecAllow = false;
                if (this.mTask.getUserAgent() != null) {
                    int timeout = profile.getDeregTimeout(13);
                    Log.i(this.LOG_TAG, "try regsiter after " + timeout);
                    this.mRegHandler.startRegistrationTimer(this.mTask, (long) timeout);
                }
                this.mTask.setDeregiReason(21);
                this.mRegMan.deregister(this.mTask, true, true, "user triggered");
            } else if (!"initial_registration".equals(error.getReason())) {
                return super.onSipError(service, error);
            } else {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, true, error.getCode() + " Initial Registration");
            }
        } else if ("smsip".equals(service) && this.mTask.getMno() == Mno.LGU) {
            return error;
        } else {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, true, "403 Forbidden");
        }
        return error;
    }

    private int getActualWaitTime() {
        return (int) (((double) getWaitTime()) * ((Math.random() * 0.5d) + 0.5d));
    }

    private int getActualWaitTimeForUnlimited404() {
        return Math.min(REG_RETRY_MAX_TIME_FOR_UNLIMITED_404, this.mRegBaseTime * ((int) Math.pow(2.0d, (double) this.mFailureCounter)));
    }

    /* access modifiers changed from: protected */
    public int getWaitTime() {
        int waitTime = this.mRegBaseTime * ((int) Math.pow(2.0d, (double) this.mFailureCounter));
        if (waitTime < 0) {
            return this.mRegMaxTime;
        }
        return Math.min(this.mRegMaxTime, waitTime);
    }

    /* access modifiers changed from: protected */
    public void removeCurrentPcscfAndInitialRegister(boolean forceInitialRegi) {
        Log.i(this.LOG_TAG, "removeCurrentPcscfAndInitialRegister()");
        this.mMoveToNextPcscfAfterTimerB = true;
        resetIPSecAllow();
        String nextPcscfIp = moveToNextPcscfIp();
        updatePcscfIpList(this.mPcscfIpList, forceInitialRegi);
        String str = this.LOG_TAG;
        Log.i(str, "removeCurrentPcscfAndInitialRegister(): nextPcscfIp " + nextPcscfIp + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mPcscfIpList " + this.mPcscfIpList);
    }

    private void moveToNextPcscfAndInitialRegister() {
        Log.i(this.LOG_TAG, "moveToNextPcscfAndInitialRegister()");
        resetIPSecAllow();
        String nextPcscfIp = moveToNextPcscfIp();
        if (this.mPcscfIpList == null) {
            Log.e(this.LOG_TAG, "moveToNextPcscfAndInitialRegister: null P-CSCF list!");
            return;
        }
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if (this.mCurPcscfIpIdx >= 0 && this.mIsValid) {
            Log.i(this.LOG_TAG, "moveToNextPcscfAndInitialRegister: forceInitialRegi");
            this.mFailureCounter = 0;
            this.mCurImpu = 0;
            this.mRegiAt = 0;
            if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                this.mTask.setDeregiReason(8);
                this.mRegMan.deregister(this.mTask, true, this.mIsValid, "pcscf updated");
            }
        }
        String str = this.LOG_TAG;
        Log.i(str, "moveToNextPcscfAndInitialRegister(): nextPcscfIp " + nextPcscfIp + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mPcscfIpList " + this.mPcscfIpList);
    }

    /* access modifiers changed from: protected */
    public String moveToNextPcscfIp() {
        String str = this.LOG_TAG;
        Log.i(str, "moveToNextPcscfIp: mCurPcscfIpIdx = " + this.mCurPcscfIpIdx + " mPcscfIpList = " + this.mPcscfIpList);
        if (this.mPcscfIpList == null || this.mPcscfIpList.isEmpty()) {
            Log.e(this.LOG_TAG, "moveToNextPcscfIp: empty P-CSCF list.");
            return "";
        }
        int offset = (this.mCurPcscfIpIdx + 1) % this.mPcscfIpList.size();
        this.mCurPcscfIpIdx = offset;
        return (String) this.mPcscfIpList.get(offset);
    }

    public boolean isThrottled() {
        return super.isThrottled();
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x016e  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01a5 A[EDGE_INSN: B:91:0x01a5->B:59:0x01a5 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkProfileUpdateFromDM(boolean r14) {
        /*
            r13 = this;
            com.sec.internal.ims.core.RegisterTask r0 = r13.mTask
            com.sec.ims.settings.ImsProfile r0 = r0.getProfile()
            java.lang.String r1 = "mmtel"
            boolean r0 = r0.hasService(r1)
            if (r0 != 0) goto L_0x001d
            com.sec.internal.ims.core.RegisterTask r0 = r13.mTask
            com.sec.ims.settings.ImsProfile r0 = r0.getProfile()
            java.lang.String r1 = "mmtel-video"
            boolean r0 = r0.hasService(r1)
            if (r0 != 0) goto L_0x001d
            return
        L_0x001d:
            java.lang.String r0 = r13.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "checkProfileUpdateFromDM: force="
            r1.append(r2)
            r1.append(r14)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r0 = 0
            com.sec.internal.constants.Mno r1 = r13.mMno
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.KT
            java.lang.String r3 = ""
            java.lang.String r4 = "ril.simtype"
            if (r1 != r2) goto L_0x0053
            java.lang.String r1 = android.os.SemSystemProperties.get(r4, r3)
            java.lang.String r2 = "20"
            boolean r1 = r2.equals(r1)
            if (r1 == 0) goto L_0x0053
            r0 = 1
            java.lang.String r1 = r13.LOG_TAG
            java.lang.String r2 = "checkProfileUpdateFromDM : KT_unreg SIM"
            android.util.Log.i(r1, r2)
        L_0x0053:
            r1 = 0
            com.sec.internal.constants.Mno r2 = r13.mMno
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.LGU
            if (r2 != r5) goto L_0x006e
            java.lang.String r2 = android.os.SemSystemProperties.get(r4, r3)
            java.lang.String r3 = "18"
            boolean r2 = r3.equals(r2)
            if (r2 == 0) goto L_0x006e
            r1 = 1
            java.lang.String r2 = r13.LOG_TAG
            java.lang.String r3 = "checkProfileUpdateFromDM : LGT_unreg SIM"
            android.util.Log.i(r2, r3)
        L_0x006e:
            android.content.Context r2 = r13.mContext
            com.sec.internal.ims.core.RegisterTask r3 = r13.mTask
            com.sec.ims.settings.ImsProfile r3 = r3.getProfile()
            int r4 = r13.mPhoneId
            com.sec.ims.settings.ImsProfile r2 = com.sec.internal.ims.settings.DmProfileLoader.getProfile(r2, r3, r4)
            int r3 = r2.getRegRetryBaseTime()
            r13.mRegBaseTime = r3
            int r3 = r2.getRegRetryMaxTime()
            r13.mRegMaxTime = r3
            boolean r3 = r13.isNeedForcibleSmsOverImsOn()
            r4 = 1
            if (r3 == 0) goto L_0x00b0
            android.content.Context r3 = r13.mContext
            int r5 = r13.mPhoneId
            com.sec.ims.settings.NvConfiguration.setSmsIpNetworkIndi(r3, r4, r5)
            r2.setSupportSmsOverIms(r4)
            com.sec.internal.ims.core.RegisterTask r3 = r13.mTask
            r3.setProfile(r2)
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r5 = "checkProfileUpdateFromDM: SmsOverIms is false. set it as true forcibly"
            android.util.Log.e(r3, r5)
            com.sec.internal.ims.core.RegistrationManagerInternal r3 = r13.mRegMan
            com.sec.internal.helper.SimpleEventLog r3 = r3.getEventLog()
            java.lang.String r5 = "checkProfileUpdateFromDM : SmsOverIms is false. set it as true forcibly"
            r3.logAndAdd(r5)
        L_0x00b0:
            com.sec.internal.ims.core.RegisterTask r3 = r13.mTask
            boolean r3 = r3.isNeedOmadmConfig()
            if (r3 != 0) goto L_0x00bc
            if (r0 != 0) goto L_0x00bc
            if (r1 == 0) goto L_0x027e
        L_0x00bc:
            com.sec.internal.ims.core.RegistrationManagerInternal r3 = r13.mRegMan
            boolean r3 = r3.hasOmaDmFinished()
            r5 = 0
            if (r3 != 0) goto L_0x0118
            if (r0 != 0) goto L_0x0118
            if (r1 == 0) goto L_0x00ca
            goto L_0x0118
        L_0x00ca:
            boolean r3 = com.sec.internal.helper.OmcCode.isKOROmcCode()
            if (r3 == 0) goto L_0x00fe
            com.sec.internal.ims.core.RegistrationManagerInternal r3 = r13.mRegMan
            int r4 = r13.mPhoneId
            com.sec.internal.constants.ims.os.NetworkEvent r3 = r3.getNetworkEvent(r4)
            boolean r3 = r3.isDataRoaming
            if (r3 == 0) goto L_0x00f5
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r4 = "Roaming, so use PCO"
            android.util.Log.i(r3, r4)
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r3)
            com.sec.internal.ims.core.RegisterTask r4 = r13.mTask
            r4.setProfile(r2)
            goto L_0x027e
        L_0x00f5:
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r4 = "not Roaming"
            android.util.Log.i(r3, r4)
            goto L_0x027e
        L_0x00fe:
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r4 = "oversea device and KOR SIM, so use PCO"
            android.util.Log.i(r3, r4)
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r3)
            com.sec.internal.ims.core.RegisterTask r4 = r13.mTask
            r4.setProfile(r2)
            goto L_0x027e
        L_0x0118:
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r6 = "checkProfileUpdateFromDM()"
            android.util.Log.i(r3, r6)
            boolean r3 = r13.mDmUpdatedFlag
            if (r3 == 0) goto L_0x012d
            if (r14 != 0) goto L_0x012d
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r4 = "mDmUpdatedFlag true"
            android.util.Log.i(r3, r4)
            return
        L_0x012d:
            java.lang.String r3 = r13.mCscConfigVerUiccMobilitySpec
            java.lang.String r6 = "3.0"
            boolean r3 = r6.equals(r3)
            if (r3 == 0) goto L_0x0157
            com.sec.internal.constants.Mno r3 = r13.mMno
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.KT
            if (r3 != r7) goto L_0x0157
            int r3 = r2.getPcscfPreference()
            if (r3 != 0) goto L_0x0157
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r6 = "[KT 5G] P-CSCF discovery PCO>DM>DEFAULT "
            android.util.Log.i(r3, r6)
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r3)
            goto L_0x01fe
        L_0x0157:
            java.util.List r3 = r2.getLboPcscfAddressList()
            r7 = 0
            if (r3 == 0) goto L_0x01a5
            int r8 = r3.size()
            if (r8 <= 0) goto L_0x01a5
            java.util.Iterator r8 = r3.iterator()
        L_0x0168:
            boolean r9 = r8.hasNext()
            if (r9 == 0) goto L_0x01a5
            java.lang.Object r9 = r8.next()
            java.lang.String r9 = (java.lang.String) r9
            java.util.regex.Pattern r10 = android.util.Patterns.DOMAIN_NAME
            java.util.regex.Matcher r10 = r10.matcher(r9)
            boolean r10 = r10.matches()
            if (r10 != 0) goto L_0x018e
            boolean r10 = com.sec.internal.helper.NetworkUtil.isIPv4Address(r9)
            if (r10 != 0) goto L_0x018e
            boolean r10 = com.sec.internal.helper.NetworkUtil.isIPv6Address(r9)
            if (r10 == 0) goto L_0x018d
            goto L_0x018e
        L_0x018d:
            goto L_0x0168
        L_0x018e:
            r7 = 1
            java.lang.String r8 = r13.LOG_TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "DM pcscf is valid : "
            r10.append(r11)
            r10.append(r9)
            java.lang.String r10 = r10.toString()
            android.util.Log.i(r8, r10)
        L_0x01a5:
            if (r3 == 0) goto L_0x01d6
            int r8 = r3.size()
            if (r8 <= 0) goto L_0x01d6
            if (r7 == 0) goto L_0x01d6
            int r5 = r2.getLboPcscfPort()
            r6 = 5
            r2.setPcscfPreference(r6)
            r2.setPcscfList(r3)
            if (r5 <= 0) goto L_0x01d5
            r2.setSipPort(r5)
            java.lang.String r6 = r13.LOG_TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "DM updated lbo pcscf port found : "
            r8.append(r9)
            r8.append(r5)
            java.lang.String r8 = r8.toString()
            android.util.Log.i(r6, r8)
        L_0x01d5:
            goto L_0x01fe
        L_0x01d6:
            java.lang.String r8 = r13.mCscConfigVerUiccMobilitySpec
            boolean r6 = r6.equals(r8)
            if (r6 == 0) goto L_0x01ec
            com.sec.internal.constants.Mno r6 = r13.mMno
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.KT
            if (r6 != r8) goto L_0x01ec
            java.lang.String r5 = r13.LOG_TAG
            java.lang.String r6 = "DM pcscf is empty. [KT 5G] P-CSCF discovery PCO>DM>DEFAULT"
            android.util.Log.i(r5, r6)
            goto L_0x01fe
        L_0x01ec:
            java.lang.String r6 = r13.LOG_TAG
            java.lang.String r8 = "DM pcscf is empty"
            android.util.Log.i(r6, r8)
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            r2.setPcscfPreference(r5)
            r2.setPcscfList(r6)
        L_0x01fe:
            boolean r3 = r2.isSupportSmsOverIms()
            if (r3 == 0) goto L_0x024d
            java.lang.String r3 = r13.LOG_TAG
            java.lang.String r5 = "SMS over IMS is enabled by OMADM"
            android.util.Log.i(r3, r5)
            java.util.Set r3 = r2.getNetworkSet()
            java.util.Iterator r5 = r3.iterator()
        L_0x0213:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x024d
            java.lang.Object r6 = r5.next()
            java.lang.Integer r6 = (java.lang.Integer) r6
            java.util.Set r7 = r2.getServiceSet(r6)
            r8 = 0
            java.util.Iterator r9 = r7.iterator()
        L_0x0228:
            boolean r10 = r9.hasNext()
            java.lang.String r11 = "smsip"
            if (r10 == 0) goto L_0x0240
            java.lang.Object r10 = r9.next()
            java.lang.String r10 = (java.lang.String) r10
            boolean r12 = r11.equals(r10)
            if (r12 == 0) goto L_0x023f
            r8 = 1
            goto L_0x0240
        L_0x023f:
            goto L_0x0228
        L_0x0240:
            if (r8 != 0) goto L_0x024c
            r7.add(r11)
            int r9 = r6.intValue()
            r2.setServiceSet(r9, r7)
        L_0x024c:
            goto L_0x0213
        L_0x024d:
            int r3 = r2.getDmPollingPeriod()
            if (r3 <= 0) goto L_0x0274
            java.lang.String r3 = r13.LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "DmPollingPeriod : "
            r5.append(r6)
            int r6 = r2.getDmPollingPeriod()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r3, r5)
            int r3 = r2.getDmPollingPeriod()
            r13.startDmPollingTimer(r3)
        L_0x0274:
            r13.checkDMConfigChange(r2)
            com.sec.internal.ims.core.RegisterTask r3 = r13.mTask
            r3.setProfile(r2)
            r13.mDmUpdatedFlag = r4
        L_0x027e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorKor.checkProfileUpdateFromDM(boolean):void");
    }

    public void updatePcscfIpList(List<String> pcscfIpList) {
        if (pcscfIpList == null) {
            Log.e(this.LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
            return;
        }
        this.mPcscfIpList = new ArrayList();
        LinkPropertiesWrapper linkProp = this.mPdnController.getLinkProperties(this.mTask);
        boolean z = false;
        if (linkProp == null) {
            Log.e(this.LOG_TAG, "updatePcscfIpList: null LinkProperties");
            this.mIsValid = false;
            return;
        }
        boolean hasipv4address = false;
        Iterator<String> it = pcscfIpList.iterator();
        while (true) {
            if (it.hasNext()) {
                if (NetworkUtil.isIPv4Address(it.next())) {
                    hasipv4address = true;
                    break;
                }
            } else {
                break;
            }
        }
        int localIpType = (linkProp.hasGlobalIPv6Address() || linkProp.hasIPv6DefaultRoute()) ? 2 : 1;
        if (this.mTask.isRcsOnly() && hasipv4address) {
            Log.i(this.LOG_TAG, "updatePcscfIpList: value ipv4 addr above ipv6 addr for RCS");
            localIpType = linkProp.hasIPv4Address() ? 1 : 2;
        }
        Log.i(this.LOG_TAG, "updatePcscfIpList: localIpType=" + localIpType);
        for (int i = 0; i < pcscfIpList.size(); i++) {
            if (this.mTask.getProfile().getIpVer() == 1) {
                if (NetworkUtil.isIPv4Address(pcscfIpList.get(i))) {
                    this.mPcscfIpList.add(pcscfIpList.get(i));
                }
            } else if (this.mTask.getProfile().getIpVer() == 2) {
                if (NetworkUtil.isIPv6Address(pcscfIpList.get(i))) {
                    this.mPcscfIpList.add(pcscfIpList.get(i));
                }
            } else if (this.mTask.getProfile().getIpVer() == 3) {
                if (localIpType == 1) {
                    if (NetworkUtil.isIPv4Address(pcscfIpList.get(i))) {
                        this.mPcscfIpList.add(pcscfIpList.get(i));
                    }
                } else if (NetworkUtil.isIPv6Address(pcscfIpList.get(i))) {
                    this.mPcscfIpList.add(pcscfIpList.get(i));
                }
            }
        }
        Log.i(this.LOG_TAG, "updatePcscfIpList mPcscfIpList = " + this.mPcscfIpList);
        this.mNumOfPcscfIp = this.mPcscfIpList.size();
        if (this.mNumOfPcscfIp > 0) {
            z = true;
        }
        this.mIsValid = z;
    }

    public void checkAcsPcscfListChange() {
        List<String> lboPcscfList = new ArrayList<>();
        String lboPcscfAddress = RcsConfigurationHelper.readStringParam(this.mContext, "address", (String) null);
        if (lboPcscfAddress == null) {
            Log.i(this.LOG_TAG, "checkAcsPcscfIpListChange : lboPcscfAddress is null");
            return;
        }
        lboPcscfList.add(lboPcscfAddress);
        String str = this.LOG_TAG;
        Log.i(str, "checkAcsPcscfIpListChange : previous pcscf = " + this.mRcsPcscfList + ", new pcscf = " + lboPcscfList);
        if (!lboPcscfList.equals(this.mRcsPcscfList)) {
            resetPcscfList();
            ArrayList arrayList = new ArrayList();
            this.mRcsPcscfList = arrayList;
            arrayList.add(lboPcscfAddress);
            Log.i(this.LOG_TAG, "checkAcsPcscfIpListChange : resetPcscfList");
        }
    }

    public void notifyReattachToRil() {
        Log.i(this.LOG_TAG, "notifyReattachToRil");
        sendRawRequestToTelephony(this.mContext, buildReattachNotiOemHookCmd());
    }

    private void startDmPollingTimer(int secs) {
        if (this.mDmPollingTimer != null) {
            stopPollingTimer();
        }
        String str = this.LOG_TAG;
        Log.i(str, "startDmPollingTimer: Timer " + secs + " sec");
        this.mDmPollingTimer = this.mRegHandler.startDmConfigTimer(this.mTask, ((long) secs) * 1000);
    }

    private void stopPollingTimer() {
        if (this.mDmPollingTimer != null) {
            Log.i(this.LOG_TAG, "stopPollingTimer");
            this.mRegHandler.stopTimer(this.mDmPollingTimer);
            this.mDmPollingTimer = null;
        }
    }

    private boolean checkValidRejectCode(int rejectCode) {
        if (rejectCode == 2 || rejectCode == 3 || rejectCode == 6 || rejectCode == 8) {
            return true;
        }
        return false;
    }

    public byte[] buildReattachNotiOemHookCmd() {
        return new byte[]{9, 11, 0, 4};
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices;
        Set<String> enabledServices = new HashSet<>();
        if (services == null) {
            filteredServices = new HashSet<>();
        }
        boolean isKTInRoaming = false;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        boolean isVoLteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1;
        if (!isImsEnabled) {
            Log.i(this.LOG_TAG, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (!this.mTask.isRcsOnly()) {
            NetworkEvent networkEvent = this.mRegMan.getNetworkEvent(this.mPhoneId);
            if (networkEvent.isDataRoaming) {
                if (this.mMno != Mno.SKT || !OmcCode.isSKTOmcCode()) {
                    if ((this.mMno != Mno.KT || !OmcCode.isKTTOmcCode()) && (this.mMno != Mno.LGU || !OmcCode.isLGTOmcCode())) {
                        if (!this.mCscSupportOmdVolteRoaming || !OmcCode.isKorOpenOmcCode()) {
                            Log.i(this.LOG_TAG, "Mno and OmcCode are different. Can't use volte roaming.");
                            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_ON_NET_CUSTOM.getCode());
                            return new HashSet();
                        }
                        Log.i(this.LOG_TAG, "filterService: mCscSupportOmdVolteRoaming = " + this.mCscSupportOmdVolteRoaming);
                        if (!((network == 13 || network == 20) && networkEvent.voiceOverPs == VoPsIndication.SUPPORTED)) {
                            Log.i(this.LOG_TAG, "NW is not LTE or IMSVoPS is off");
                            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                            return new HashSet();
                        }
                    } else if (!((network == 13 || network == 20) && networkEvent.voiceOverPs == VoPsIndication.SUPPORTED)) {
                        Log.i(this.LOG_TAG, "NW is not LTE or IMSVoPS is off");
                        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                        return new HashSet();
                    }
                } else if (!isVolteRoamingSettingEnabled()) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
                    return new HashSet();
                } else if (!((network == 13 || network == 20) && networkEvent.voiceOverPs == VoPsIndication.SUPPORTED)) {
                    Log.i(this.LOG_TAG, "NW is not LTE or IMSVoPS is off");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                    return new HashSet();
                }
            }
            if (isVoLteEnabled) {
                enabledServices.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
                if (!enabledServices.contains("mmtel")) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
                }
            }
            enabledServices = applyMmtelUserSettings(enabledServices, network);
            ImsProfile imsDmProfile = DmProfileLoader.getProfile(this.mContext, this.mTask.getProfile(), this.mPhoneId);
            if (networkEvent.isDataRoaming && this.mMno == Mno.KT && OmcCode.isKTTOmcCode()) {
                isKTInRoaming = true;
            }
            Log.i(this.LOG_TAG, "filterService: isKTInRoaming: " + isKTInRoaming);
            if (!imsDmProfile.isSupportSmsOverIms() && !isKTInRoaming) {
                removeService(enabledServices, "smsip", "isSupportSmsOverIms disabled");
            }
        }
        if (this.mTask.isRcsOnly()) {
            if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
                enabledServices.addAll(servicesByImsSwitch(ImsProfile.getRcsServiceList()));
            }
            Log.i(this.LOG_TAG, "filterService: skip checking Chatbot agreement. enable chatbot if RCS agreed by user.");
        }
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices);
        }
        Log.i(this.LOG_TAG, "filterService : filteredServices = " + filteredServices);
        return filteredServices;
    }

    private boolean checkOtaStatus() {
        if (this.mMno != Mno.SKT || !CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("ril.domesticOtaStart"))) {
            return true;
        }
        Log.i(this.LOG_TAG, "isReadyToRegister : OTA is working, don't try register");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_OTA.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRegiStatus() {
        if (this.mTask.mIsUpdateRegistering || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
            return false;
        }
        if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTED || this.mPDNdisconnectTimeout == null || this.mTask.isRcsOnly()) {
            return true;
        }
        Log.i(this.LOG_TAG, "isReadyToRegister: mPDNdisconnectTimeout is not null");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int rat) {
        if (this.mTask.isRcsOnly()) {
            boolean isRcsUserSettingEnabled = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1;
            if (RcsConfigurationHelper.readIntParam(this.mContext, "version", 0).intValue() <= 0 && !isRcsUserSettingEnabled) {
                Log.i(this.LOG_TAG, "isReadyToRegister: User don't try RCS service yet");
                return false;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                Log.i(this.LOG_TAG, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            } else {
                ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
                if (sm != null) {
                    String imsi = "IMSI_" + sm.getImsi();
                    if (TextUtils.isEmpty(sm.getMsisdn()) && TextUtils.isEmpty(ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, IConfigModule.MSISDN_FROM_PAU, imsi, ""))) {
                        Log.i(this.LOG_TAG, "isReadyToRegister: MSISDN is null, try to RCS ACS after registered VoLTE");
                        IMSLog.c(LogClass.KOR_PENDING_RCS, this.mPhoneId + "PENDING RCS");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        int callStatus = this.mRegMan.getTelephonyCallStatus(this.mTask.getPhoneId());
        Log.i(this.LOG_TAG, "isReadyToRegister : getTelephonyCallStatus is CALL_STATE_OFFHOOK");
        return callStatus == 0 || callStatus == 2;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkOtaStatus() && rat != 0 && checkRegiStatus() && checkRcsEvent(rat) && checkCallStatus());
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 4 || releaseCase == 10 || ((this.mIsAkaChallengeTimeout && (releaseCase == 1 || releaseCase == 5)) || (needImsNotAvailable() && (releaseCase == 9 || releaseCase == 1)))) {
            this.mIsPermanentStopped = false;
            resetIPSecAllow();
            this.mCurImpu = 0;
        } else if (releaseCase == 1) {
            resetRetry();
            resetAllRetryFlow();
        }
        if (!this.mIsPermanentStopped) {
            SimpleEventLog eventLog = this.mRegMan.getEventLog();
            eventLog.logAndAdd("releaseThrottle: case by " + releaseCase);
        }
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        if ("DETACH_WITH_REATTACH_LTE_NW_DETACH".equals(reason)) {
            this.mRegMan.getEventLog().logAndAdd("got DETACH_WITH_REATTACH_LTE_NW_DETACH, release throttle.");
            releaseThrottle(10);
            this.mIsReadyToGetReattach = false;
        }
    }

    public void startTimsTimer(String reason) {
        String str = this.LOG_TAG;
        Log.i(str, "startTimsTimer : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
        if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
            if (!this.mTask.isRcsOnly()) {
                if (!needImsNotAvailable()) {
                    int i = this.mRequestPdnTimeoutCount;
                    if (i < 5) {
                        this.mRequestPdnTimeoutCount = i + 1;
                        startTimsEstablishTimer(this.mTask, 180000, reason);
                    }
                } else if (!SlotBasedConfig.getInstance(this.mPhoneId).isNotifiedImsNotAvailable()) {
                    startTimsEstablishTimer(this.mTask, 60000, reason);
                }
            } else if (isMobilePreferredForRcs()) {
                int preferredPdnType = this.mPdnController.translateNetworkBearer(this.mPdnController.getDefaultNetworkBearer());
                if (this.mTask.getPdnType() != 0 || !NetworkUtil.isMobileDataOn(this.mContext) || !NetworkUtil.isMobileDataPressed(this.mContext) || ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON || this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).outOfService || preferredPdnType != 1 || this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
                    int i2 = this.mRequestPdnTimeoutCount;
                    if (i2 < 5) {
                        this.mRequestPdnTimeoutCount = i2 + 1;
                        startTimsEstablishTimer(this.mTask, (long) 180000, reason);
                        return;
                    }
                    return;
                }
                startTimsEstablishTimer(this.mTask, (long) 30000, reason);
            }
        }
    }

    public void stopTimsTimer(String reason) {
        String str = this.LOG_TAG;
        Log.i(str, "stopTimsTimer : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
        if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
            if (this.mTask.isRcsOnly()) {
                this.mHasNetworkFailure = false;
            }
            stopTimsEstablishTimer(this.mTask, reason);
        }
    }

    public void onTimsTimerExpired() {
        String str;
        Log.i(this.LOG_TAG, "onTimsTimerExpired : " + this.mTask.getProfile().getName() + "(" + this.mTask.getState() + ") Pdn(" + this.mTask.getPdnType() + "," + this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask) + ")");
        boolean imsNotAvailable = needImsNotAvailable();
        if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
            if (!this.mTask.isRcsOnly()) {
                SimpleEventLog eventLog = this.mRegMan.getEventLog();
                StringBuilder sb = new StringBuilder();
                sb.append("onTimsTimerExpired. ");
                sb.append(imsNotAvailable);
                if (imsNotAvailable) {
                    str = "";
                } else {
                    str = ",Count is " + this.mRequestPdnTimeoutCount;
                }
                sb.append(str);
                eventLog.logAndAdd(sb.toString());
                if (imsNotAvailable) {
                    super.onTimsTimerExpired();
                    return;
                }
                stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_TIMS_EXPIRED);
                deregisterIfConnecting(13);
            } else if (isMobilePreferredForRcs()) {
                this.mRegMan.getEventLog().logAndAdd("onTimsTimerExpired for RCS. " + "Count is " + this.mRequestPdnTimeoutCount);
                stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_TIMS_EXPIRED);
                this.mHasNetworkFailure = true;
                deregisterIfConnecting(13);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        if (this.mMno != Mno.LGU && (this.mMno != Mno.KT || !this.mIsShipBuild || !this.mCscSupportLTEPreferred)) {
            return super.getVoiceTechType();
        }
        Log.i(this.LOG_TAG, "getVoiceTechType : LGU device or KT LTE Preferred ship device have to enable VOLTE always, regardless of DB");
        if (ImsConstants.SystemSettings.VOLTE_SLOT1.get(this.mContext, 0) != 0) {
            Log.i(this.LOG_TAG, "getVoiceTechType : voicecall_type is not 0, correct it");
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, this.mPhoneId);
        }
        return 0;
    }

    public boolean isIPSecAllow() {
        if (!OmcCode.isKOROmcCode() && this.mMno == Mno.SKT) {
            Log.i(this.LOG_TAG, "isIPSecAllow : oversea device and SKT sim. do not use IPSec");
            return false;
        } else if (!this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming) {
            return this.mIPsecAllow;
        } else {
            if (this.mMno == Mno.SKT || this.mMno == Mno.LGU) {
                return false;
            }
            return this.mIPsecAllow;
        }
    }

    public String toString() {
        return "RegistrationGovernorKor [mRegBaseTime=" + this.mRegBaseTime + ", mDmUpdatedFlag=" + this.mDmUpdatedFlag + ", mConsecutiveForbiddenCounter=" + this.mConsecutiveForbiddenCounter + ", mHasPendingInitRegistrationByDMConfigChange=" + this.mHasPendingInitRegistrationByDMConfigChange + ", mIsAkaChallengeTimeout=" + this.mIsAkaChallengeTimeout + ", mHasPendingNotifyImsNotAvailable=" + this.mHasPendingNotifyImsNotAvailable + "] " + super.toString();
    }

    private boolean isVolteEnabled() {
        return isVolteSettingEnabled() && getVolteServiceStatus() && isLTEDataModeEnabled();
    }

    private void setOldVolteServiceStatus(boolean serviceStatus) {
        String str = this.LOG_TAG;
        Log.i(str, "setOldVolteServiceStatus : " + serviceStatus);
        this.mVolteServiceStatus = serviceStatus;
    }

    private boolean getVolteServiceStatus() {
        boolean isVolteServiceStatusOn = DmProfileLoader.getProfile(this.mContext, this.mTask.getProfile(), this.mPhoneId).isVolteServiceStatus();
        String str = this.LOG_TAG;
        Log.i(str, "getVolteServiceStatus : " + isVolteServiceStatusOn);
        return isVolteServiceStatusOn;
    }

    public boolean needImsNotAvailable() {
        boolean isLGUInVoLTERoaming = this.mMno == Mno.LGU && (OmcCode.isLGTOmcCode() || (this.mCscSupportOmdVolteRoaming && OmcCode.isKorOpenOmcCode())) && this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming;
        boolean isImsNotAvailableSupported = this.mTask.getProfile().getSupportImsNotAvailable();
        boolean isVoLTEOnly = !this.mTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(this.mTask.getProfile());
        String str = this.LOG_TAG;
        Log.i(str, "isLGUInVoLTERoaming : " + isLGUInVoLTERoaming + " isImsNotAvailableSupported : " + isImsNotAvailableSupported + " isVoLTEOnly : " + isVoLTEOnly);
        if (!isLGUInVoLTERoaming || !isImsNotAvailableSupported || !isVoLTEOnly) {
            return false;
        }
        return true;
    }

    public boolean hasNetworkFailure() {
        return this.mHasNetworkFailure;
    }

    public boolean isMobilePreferredForRcs() {
        if (!this.mTask.isRcsOnly() || this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).isDataRoaming) {
            return false;
        }
        return true;
    }

    public void onVolteSettingChanged() {
        Log.i(this.LOG_TAG, "onVolteSettingChanged ");
        if (!this.mTask.isRcsOnly()) {
            checkVoLTEStatusChanged(VoltePreferenceChangedReason.VOLTE_SETTING);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVolteSettingEnabled() {
        return getVoiceTechType() == 0;
    }

    public void onLteDataNetworkModeSettingChanged(boolean enabled) {
        String str = this.LOG_TAG;
        Log.i(str, "onLteDataNetworkModeSettingChanged : " + enabled);
        if (!this.mTask.isRcsOnly()) {
            checkVoLTEStatusChanged(VoltePreferenceChangedReason.LTE_MODE);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isLTEDataModeEnabled() {
        if (this.mMno == Mno.LGU) {
            Log.i(this.LOG_TAG, "LGU uses only LTE");
            return true;
        }
        int isLTEDataMode = Settings.Secure.getInt(this.mContext.getContentResolver(), "lte_mode_on", 1);
        String str = this.LOG_TAG;
        Log.i(str, "LTEDataMode : " + isLTEDataMode);
        if (isLTEDataMode == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void checkVoLTEStatusChanged(VoltePreferenceChangedReason changedReason) {
        boolean needReregi = false;
        boolean isVolteOn = true;
        boolean isServiceStatusOn = getVolteServiceStatus();
        boolean isVolteSettingOn = isVolteSettingEnabled();
        boolean isLTEDataModeOn = isLTEDataModeEnabled();
        if (changedReason == VoltePreferenceChangedReason.VOLTE_SETTING) {
            if (isServiceStatusOn && isLTEDataModeOn) {
                needReregi = true;
                isVolteOn = isVolteSettingOn;
            }
        } else if (changedReason == VoltePreferenceChangedReason.LTE_MODE && isServiceStatusOn && isVolteSettingOn) {
            needReregi = true;
            isVolteOn = isLTEDataModeOn;
        }
        String str = this.LOG_TAG;
        Log.i(str, "checkVoLTEStatusChanged : needReregi = " + needReregi + ", isVolteOn = " + isVolteOn);
        if (needReregi) {
            String str2 = this.LOG_TAG;
            Log.i(str2, "checkVoLTEStatusChanged: force update " + this.mTask);
            this.mRegHandler.requestForcedUpdateRegistration(this.mTask);
        }
    }

    public void onVolteRoamingSettingChanged(boolean enabled) {
        if (!this.mTask.isRcsOnly()) {
            String str = this.LOG_TAG;
            Log.i(str, "onVolteRoamingSettingChanged : " + enabled);
            if (enabled) {
                this.mRegHandler.sendTryRegister(this.mPhoneId);
                return;
            }
            if (this.mTask.getUserAgent() == null) {
                this.mRegHandler.sendDisconnectPdnByHdVoiceRoamingOff(this.mTask);
            } else {
                this.mTask.setDeregiReason(74);
                this.mRegMan.deregister(this.mTask, false, false, "volte roaming disabled");
            }
            resetRetry();
            resetAllRetryFlow();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isVolteRoamingSettingEnabled() {
        int isVolteRoaming = ImsConstants.SystemSettings.VOLTE_ROAMING.get(this.mContext, 0);
        String str = this.LOG_TAG;
        Log.i(str, "isVolteRoaming : " + isVolteRoaming);
        if (isVolteRoaming == 1) {
            return true;
        }
        return false;
    }

    public void onRoamingDataChanged(boolean enabled) {
        String str = this.LOG_TAG;
        Log.i(str, "onRoamingDataChanged : " + enabled);
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onRoamingDataChanged: remove dependency for dataRoaming");
        }
    }

    public void onRoamingLteChanged(boolean enabled) {
        String str = this.LOG_TAG;
        Log.i(str, "onRoamingLteChanged : " + enabled);
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onRoamingLteChanged: remove dependency for lteRoaming");
        }
    }

    public boolean isOmadmConfigAvailable() {
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming) {
            return false;
        }
        if (this.mMno == Mno.KT && "20".equals(SemSystemProperties.get("ril.simtype", ""))) {
            Log.i(this.LOG_TAG, "isOmadmConfigAvailable : KT_unreg SIM. do not trigger DM");
            return false;
        } else if (this.mMno == Mno.LGU && "18".equals(SemSystemProperties.get("ril.simtype", ""))) {
            Log.i(this.LOG_TAG, "isOmadmConfigAvailable : LGT_unreg SIM. do not trigger DM");
            return false;
        } else if (!OmcCode.isKOROmcCode()) {
            Log.i(this.LOG_TAG, "isOmadmConfigAvailable : oversea device and KOR sim. do not trigger DM");
            return false;
        } else {
            try {
                this.mContext.getPackageManager().getPackageInfo(ImsConstants.Packages.PACKAGE_DM_CLIENT, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                Log.i(this.LOG_TAG, "isOmadmConfigAvailable : DM Package not found");
                return false;
            }
        }
    }

    public void retryDNSQuery() {
        if (this.mMno != Mno.KT) {
            return;
        }
        if (NSDSNamespaces.NSDSApiVersion.MDSP30.equals(this.mCscConfigVerUiccMobilitySpec)) {
            Log.i(this.LOG_TAG, "retryDNSQuery : mDnsQueryCount = " + this.mDnsQueryCount);
            int i = this.mDnsQueryCount;
            if (i < 1) {
                Log.i(this.LOG_TAG, "retryDNSQuery : PCO => omadm ");
                if (this.mTask.getProfile().getPcscfPreference() == 0) {
                    this.mTask.getProfile().setPcscfPreference(5);
                    Log.i(this.LOG_TAG, "retryDNSQuery : getPcscfPreference = " + this.mTask.getProfile().getPcscfPreference());
                    if (this.mDmUpdatedFlag) {
                        Log.i(this.LOG_TAG, "retryDNSQuery : PCO => omadm mDmUpdatedFlag " + this.mDmUpdatedFlag);
                        checkProfileUpdateFromDM(true);
                    } else {
                        List<String> lboPcscfList = this.mTask.getProfile().getLboPcscfAddressList();
                        if (lboPcscfList.isEmpty()) {
                            Log.i(this.LOG_TAG, "retryDNSQuery : PCO => omadm no pcscf ");
                            List<String> pcscfList = new ArrayList<>();
                            pcscfList.add(OMADM_KT_DEFAULT_PCSCF);
                            this.mTask.getProfile().setPcscfList(pcscfList);
                        } else {
                            Log.i(this.LOG_TAG, "retryDNSQuery : PCO => omadm no pcscf ");
                            this.mTask.getProfile().setPcscfList(lboPcscfList);
                        }
                    }
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                    this.mDnsQueryCount = 0;
                    return;
                }
                this.mRegHandler.sendTryRegister(this.mPhoneId, 4000);
            } else if (i == 1) {
                List<String> pcscfList2 = new ArrayList<>();
                pcscfList2.add(OMADM_KT_DEFAULT_PCSCF);
                this.mTask.getProfile().setPcscfList(pcscfList2);
                this.mRegHandler.sendTryRegister(this.mPhoneId, 4000);
            } else {
                Log.i(this.LOG_TAG, "retryDNSQuery : final ");
                this.mTask.getProfile().setPcscfPreference(0);
                this.mDnsQueryCount = 0;
                return;
            }
            this.mDnsQueryCount++;
            return;
        }
        Log.i(this.LOG_TAG, "retryDNSQuery : mDnsQueryCount = " + this.mDnsQueryCount);
        int i2 = this.mDnsQueryCount;
        if (i2 < 1) {
            this.mRegHandler.sendTryRegister(this.mPhoneId, 4000);
        } else if (i2 == 1) {
            List<String> pcscfList3 = new ArrayList<>();
            pcscfList3.add(OMADM_KT_DEFAULT_PCSCF);
            this.mTask.getProfile().setPcscfList(pcscfList3);
            this.mRegHandler.sendTryRegister(this.mPhoneId, 4000);
        } else {
            this.mTask.getProfile().setPcscfPreference(0);
            this.mRegHandler.sendTryRegister(this.mPhoneId);
            this.mDnsQueryCount = 0;
            return;
        }
        this.mDnsQueryCount++;
    }

    public boolean isNeedDelayedDeregister() {
        String str = this.LOG_TAG;
        Log.i(str, "isNeedDelayedDeregister :  mNeedDelayedDeregister = " + this.mNeedDelayedDeregister);
        if (this.mNeedDelayedDeregister || ((Boolean) Optional.ofNullable((ImModule) ImsRegistry.getServiceModuleManager().getImModule()).map(new Function() {
            public final Object apply(Object obj) {
                return RegistrationGovernorKor.this.lambda$isNeedDelayedDeregister$0$RegistrationGovernorKor((ImModule) obj);
            }
        }).orElse(false)).booleanValue()) {
            return true;
        }
        return false;
    }

    public /* synthetic */ Boolean lambda$isNeedDelayedDeregister$0$RegistrationGovernorKor(ImModule im) {
        return Boolean.valueOf(im.hasIncomingSessionForA2P(this.mTask.getPhoneId()));
    }

    public void setNeedDelayedDeregister(boolean val) {
        String str = this.LOG_TAG;
        Log.i(str, "setNeedDelayedDeregister :  val = " + val);
        this.mNeedDelayedDeregister = val;
    }

    private void checkDMConfigChange(ImsProfile dmProfile) {
        boolean isNeedToInitRegi = false;
        if (dmProfile == null) {
            Log.i(this.LOG_TAG, "checkDMConfigChange : dmProfile in null");
            return;
        }
        List<String> lboPcscfList = dmProfile.getLboPcscfAddressList();
        if (!((lboPcscfList == null || lboPcscfList.equals(this.mPcscfList)) && !this.mPcscfList.isEmpty() && dmProfile.isIpSecEnabled() == this.mIpsecEnabled && dmProfile.isSupportSmsOverIms() == this.mSmsOverIp && dmProfile.isVolteServiceStatus() == this.mVolteServiceStatus)) {
            isNeedToInitRegi = true;
        }
        String str = this.LOG_TAG;
        Log.i(str, "checkDMConfigChange : previous pcscf = " + this.mPcscfList + ", new pcscf = " + lboPcscfList);
        String str2 = this.LOG_TAG;
        Log.i(str2, "checkDMConfigChange : previous IpSecEnabled = " + this.mIpsecEnabled + ", new IpSecEnabled = " + dmProfile.isIpSecEnabled());
        String str3 = this.LOG_TAG;
        Log.i(str3, "checkDMConfigChange : previous SmsOverIp = " + this.mSmsOverIp + ", new SmsOverIp = " + dmProfile.isSupportSmsOverIms());
        String str4 = this.LOG_TAG;
        Log.i(str4, "checkDMConfigChange : previous ServiceStatus = " + this.mVolteServiceStatus + ", new ServiceStatus = " + dmProfile.isVolteServiceStatus());
        if (lboPcscfList != null && !lboPcscfList.equals(this.mPcscfList)) {
            resetPcscfList();
            resetIPSecAllow();
            Log.i(this.LOG_TAG, "checkDMConfigChange : resetPcscfList");
        }
        this.mPcscfList = dmProfile.getLboPcscfAddressList();
        this.mIpsecEnabled = dmProfile.isIpSecEnabled();
        this.mSmsOverIp = dmProfile.isSupportSmsOverIms();
        setOldVolteServiceStatus(dmProfile.isVolteServiceStatus());
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING) && isNeedToInitRegi) {
            if (this.mTelephonyManager.getCallState() == 0) {
                Log.i(this.LOG_TAG, "checkDMConfigChange : need de-reg and init reg");
                this.mHasPendingInitRegistrationByDMConfigChange = false;
                this.mTask.setDeregiReason(29);
                this.mRegMan.deregister(this.mTask, true, true, "checkDMConfigChange : need de-reg and init reg");
                this.mRegHandler.sendTryRegister(this.mPhoneId);
                return;
            }
            Log.i(this.LOG_TAG, "checkDMConfigChange : de-reg and init reg after call end");
            this.mHasPendingInitRegistrationByDMConfigChange = true;
        }
    }

    public void onTelephonyCallStatusChanged(int callState) {
        setCallStatus(callState);
        String str = this.LOG_TAG;
        Log.i(str, "onTelephonyCallStatusChanged: " + callState);
        super.onTelephonyCallStatusChanged(callState);
        if ((this.mTask.getProfile().hasService("mmtel") || this.mTask.getProfile().hasService("mmtel-video")) && getCallStatus() == 0) {
            if (this.mHasPendingInitRegistrationByDMConfigChange) {
                Log.i(this.LOG_TAG, "onTelephonyCallStatusChanged : do pending de-reg and init reg");
                this.mHasPendingInitRegistrationByDMConfigChange = false;
                this.mTask.setDeregiReason(29);
                this.mRegMan.deregister(this.mTask, true, true, "onTelephonyCallStatusChanged : do pending de-reg and init reg");
                this.mRegHandler.sendTryRegister(this.mPhoneId);
            }
            if (this.mHasPendingNotifyImsNotAvailable) {
                this.mRegMan.getEventLog().logAndAdd("onTelephonyCallStatusChanged : send pending notifyImsNotAvailable");
                this.mHasPendingNotifyImsNotAvailable = false;
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                this.mRegMan.notifyImsNotAvailable(this.mTask, true);
            }
        }
    }

    public void resetIPSecAllow() {
        this.mIPsecAllow = true;
    }

    public void resetPcscfPreference() {
        if (this.mMno == Mno.KT && !this.mTask.isRcsOnly() && this.mTask.getProfile().getPcscfPreference() != 2) {
            if (NSDSNamespaces.NSDSApiVersion.MDSP30.equals(this.mCscConfigVerUiccMobilitySpec)) {
                this.mTask.getProfile().setPcscfPreference(0);
            } else {
                this.mTask.getProfile().setPcscfPreference(5);
            }
            String str = this.LOG_TAG;
            Log.i(str, "resetPcscfPreference : getPcscfPreference = " + this.mTask.getProfile().getPcscfPreference());
        }
    }

    /* access modifiers changed from: protected */
    public void startPDNdisconnectTimer(long millis) {
        stopPDNdisconnectTimer();
        String str = this.LOG_TAG;
        Log.i(str, "startPDNdisconnectTimer: millis " + millis);
        this.mPDNdisconnectTimeout = this.mRegHandler.startDisconnectPdnTimer(this.mTask, millis);
    }

    /* access modifiers changed from: protected */
    public void stopPDNdisconnectTimer() {
        if (this.mPDNdisconnectTimeout != null) {
            Log.i(this.LOG_TAG, "stopPDNdisconnectTimer");
            this.mRegHandler.stopTimer(this.mPDNdisconnectTimeout);
            this.mPDNdisconnectTimeout = null;
        }
    }

    public void resetAllRetryFlow() {
        this.mConsecutiveForbiddenCounter = 0;
        this.mIsAkaChallengeTimeout = false;
        this.mDnsQueryCount = 0;
        stopPDNdisconnectTimer();
        stopRetryTimer();
    }

    public void unRegisterIntentReceiver() {
        Log.i(this.LOG_TAG, "Un-register Intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mIntentReceiverKor);
        } catch (IllegalArgumentException e) {
            Log.e(this.LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    private void makeRegistrationFailedToast() {
        if (this.mMno == Mno.SKT) {
            Toast.makeText(this.mContext, this.mContext.getResources().getString(R.string.regi_failed_msg_skt), 1).show();
        } else if (this.mMno == Mno.KT) {
            Toast.makeText(this.mContext, this.mContext.getResources().getString(R.string.regi_failed_msg_kt), 1).show();
            this.mIsReadyToGetReattach = true;
        } else if (this.mMno == Mno.LGU) {
            Toast.makeText(this.mContext, this.mContext.getResources().getString(R.string.regi_failed_msg_lgu, new Object[]{"1544-0010"}), 1).show();
        }
    }

    private boolean isNeedForcibleSmsOverImsOn() {
        boolean isNeedSmsOverImsOn = false;
        if (this.mMno == Mno.KT || this.mMno == Mno.LGU) {
            boolean isVolteEnabled = isVolteEnabled();
            boolean isSMSIP = TextUtils.equals(NvConfiguration.get(this.mContext, "sms_over_ip_network_indication", "", this.mPhoneId), "1");
            String str = this.LOG_TAG;
            Log.i(str, "isNeedForcibleSmsOverImsOn: isVolteEnabled " + isVolteEnabled + " isSMSIP " + isSMSIP);
            if (this.mMno == Mno.KT) {
                boolean isDataRoaming = this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming;
                boolean isEpsOnlyReg = this.mPdnController.isEpsOnlyReg(this.mPhoneId);
                String str2 = this.LOG_TAG;
                Log.i(str2, "isNeedForcibleSmsOverImsOn: mCscSupportLTEPreferred " + this.mCscSupportLTEPreferred + " isDataRoaming " + isDataRoaming + " isEpsOnlyReg " + isEpsOnlyReg);
                if (this.mCscSupportLTEPreferred && !isDataRoaming && isEpsOnlyReg && isVolteEnabled && !isSMSIP) {
                    isNeedSmsOverImsOn = true;
                }
            } else if (this.mMno == Mno.LGU && isVolteEnabled && !isSMSIP) {
                isNeedSmsOverImsOn = true;
            }
        }
        String str3 = this.LOG_TAG;
        Log.i(str3, "isNeedForcibleSmsOverImsOn: isNeedSmsOverImsOn " + isNeedSmsOverImsOn);
        return isNeedSmsOverImsOn;
    }

    private boolean hasCall() {
        boolean ret = this.mVsm != null && this.mVsm.getSessionCount(this.mPhoneId) > 0 && this.mVsm.hasActiveCall(this.mPhoneId);
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "hasCall:" + ret);
        return ret;
    }

    /* access modifiers changed from: private */
    public void handlePeriodicPollingTimeoutIntent() {
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onReceive: dm polling timeout");
            this.mRegHandler.sendRequestDmConfig();
        }
    }

    /* access modifiers changed from: private */
    public void handleFlightModeIntent(Intent intent) {
        Intent batteryStatus;
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            this.mTask.setDeregiReason(23);
            int powerOff = intent.getIntExtra("powerofftriggered", -1);
            String str = this.LOG_TAG;
            Log.i(str, "powerOff :" + powerOff);
            if (!(powerOff == -1 || (batteryStatus = this.mContext.registerReceiver((BroadcastReceiver) null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) == null)) {
                int batteryLevel = (batteryStatus.getIntExtra("level", 0) * 100) / batteryStatus.getIntExtra("scale", 100);
                String str2 = this.LOG_TAG;
                Log.i(str2, "battery level: " + batteryLevel);
                if (batteryLevel <= 2) {
                    this.mTask.setDeregiReason(33);
                }
            }
            String str3 = this.LOG_TAG;
            Log.i(str3, "onReceive: FLIGHT_MODE is changed - reason : " + this.mTask.getDeregiReason());
            setNeedDelayedDeregister(true);
            Log.i(this.LOG_TAG, "deregister delay 300 ms for sending BYE");
            this.mRegMan.deregister(this.mTask, false, false, "flight mode enabled");
            resetRetry();
            resetAllRetryFlow();
        }
    }

    /* access modifiers changed from: private */
    public void handleAirplaneModeIntent(Intent intent) {
        if (!this.mTask.isRcsOnly()) {
            resetPcscfPreference();
        } else if (((Boolean) Optional.ofNullable(intent.getExtras()).map($$Lambda$RegistrationGovernorKor$Bf2KG11tN87jQfnNEkD6D13oLzY.INSTANCE).orElse(false)).booleanValue()) {
            this.mConfigModule.getAcsConfig(this.mPhoneId).setAcsCompleteStatus(false);
            this.mConfigModule.getAcsConfig(this.mPhoneId).setForceAcs(true);
            Log.i(this.LOG_TAG, "onReceive: AIRPLANE_MODE on. reset ACS Info");
        }
    }

    /* access modifiers changed from: private */
    public void handleUsimDownloadEndIntent() {
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            this.mRegHandler.sendTryRegister(this.mPhoneId);
        }
    }

    /* access modifiers changed from: private */
    public void handleSoftResetIntent() {
        if (!this.mTask.isRcsOnly()) {
            Context context = this.mContext;
            int i = 1;
            if (ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.VOLTE_DOMESTIC_DEFAULT_ENABLED, true)) {
                i = 0;
            }
            ImsConstants.SystemSettings.setVoiceCallType(context, i, this.mPhoneId);
        }
    }

    /* access modifiers changed from: private */
    public void handleNwRejectIntent(Intent intent) {
        if (this.mTask.isRcsOnly()) {
            int rejectCode = 0;
            String extraCause = intent.getStringExtra(ImsConstants.Intents.EXTRA_CAUSE_KEY);
            if (extraCause == null || extraCause.isEmpty()) {
                Log.e(this.LOG_TAG, "empty CAUSE");
                return;
            }
            try {
                rejectCode = Integer.parseInt(extraCause);
            } catch (NumberFormatException e) {
                Log.e(this.LOG_TAG, "invalid CAUSE");
            }
            String str = this.LOG_TAG;
            Log.i(str, "onReceive: " + intent.getAction() + ", CAUSE: " + rejectCode);
            if (checkValidRejectCode(rejectCode)) {
                this.mTask.setDeregiReason(10);
                String reason = null;
                if (ImsConstants.Intents.INTENT_ACTION_REGIST_REJECT.equals(intent.getAction())) {
                    reason = "nw_regist_reject";
                } else if (ImsConstants.Intents.INTENT_ACTION_LTE_REJECT.equals(intent.getAction())) {
                    reason = "nw_lte_reject";
                }
                this.mRegMan.deregister(this.mTask, false, true, reason);
                resetRetry();
                resetAllRetryFlow();
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleWapPushDmNotiReceivedIntent() {
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onReceive: INTENT_WAP_PUSH_DM_NOTI_RECEIVED is received");
            if (this.mIsPermanentStopped) {
                this.mIsPermanentStopped = false;
                resetIPSecAllow();
                this.mCurImpu = 0;
                this.mRegMan.getEventLog().logAndAdd("handleWapPushDmNotiReceivedIntent: reset mIsPermanentStopped");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleNewOutgoingCallIntent() {
        if ((this.mTask.getProfile().hasService("mmtel") || this.mTask.getProfile().hasService("mmtel-video")) && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 1 && !this.mTask.getProfile().hasEmergencySupport() && !hasCall()) {
            Log.i(this.LOG_TAG, "onReceive: INTENT_NEW_OUTGOING_CALL is received");
            resetRetry();
            resetAllRetryFlow();
            deregisterIfConnecting(37);
        }
    }

    /* access modifiers changed from: private */
    public void handleBootCompletedIntent() {
        if (!this.mTask.isRcsOnly()) {
            Log.i(this.LOG_TAG, "onReceive: ACTION_BOOT_COMPLETED is received");
            checkUnprocessedOmadmConfig();
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> services, int network) {
        if (services == null) {
            return new HashSet();
        }
        if (!isVolteEnabled()) {
            if (!isVolteSettingEnabled()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
            } else if (!getVolteServiceStatus()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_DM_OFF.getCode());
            } else if (!isLTEDataModeEnabled()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_3G_PREFERRED_MODE.getCode());
            }
            removeService(services, "mmtel", "isVolteEnabled disabled.");
        }
        return services;
    }

    public void onPdnConnected() {
        LinkPropertiesWrapper lp = this.mPdnController.getLinkProperties(this.mTask);
        if (lp == null) {
            Log.e(this.LOG_TAG, "onPdnConnected: LinkProperties are not exist! return..");
        } else if (this.mTask.getPdnType() == 11) {
            this.mTask.clearSuspended();
            this.mTask.clearSuspendedBySnapshot();
            if (this.mLocalAddress == null) {
                this.mLocalAddress = lp.getAddresses();
            }
            if (!this.mLocalAddress.equals(lp.getAddresses())) {
                Log.i(this.LOG_TAG, "onPdnConnected: local IP is changed. dm&initial regi. are needed.");
                resetRetry();
                this.mLocalAddress = lp.getAddresses();
                this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
                resetPcscfPreference();
                resetIPSecAllow();
                releaseThrottle(5);
            }
        }
    }

    private void deregisterIfConnecting(int reason) {
        this.mTask.setDeregiReason(reason);
        if (reason == 13 && this.mTask.getUserAgent() == null && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mRegHandler.sendTryRegister(this.mPhoneId, 1000);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            Log.i(this.LOG_TAG, "deregisterIfConnecting : stopPdnConnectivity");
            return;
        }
        boolean keepPdn = this.mTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTING;
        this.mTask.mKeepPdn = keepPdn;
        this.mRegMan.deregister(this.mTask, true, keepPdn, "user triggered");
        Log.i(this.LOG_TAG, "deregisterIfConnecting : deregister");
    }

    public void resetPdnFailureInfo() {
        super.resetPdnFailureInfo();
        if (this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
            this.mRequestPdnTimeoutCount = 0;
            if (isMobilePreferredForRcs() && this.mTask.getPdnType() == 0) {
                Log.i(this.LOG_TAG, "resetPdnFailureInfo: rcs");
                this.mHasNetworkFailure = false;
            }
        }
    }

    private boolean needToHandleUnlimited404() {
        return !OmcCode.isKOROmcCode() && this.mTask.getMno() == Mno.KT;
    }

    private void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            ContentValues eutranValue = new ContentValues();
            eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri.Builder buildUpon = Uri.parse("content://com.sec.ims.settings/global").buildUpon();
            contentResolver.update(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + this.mPhoneId).build(), eutranValue, (String) null, (String[]) null);
        }
    }
}
