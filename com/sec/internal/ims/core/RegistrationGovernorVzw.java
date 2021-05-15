package com.sec.internal.ims.core;

import android.content.Context;
import android.content.Intent;
import android.os.SemSystemProperties;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorVzw;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.CscFeatureTagIMS;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ImsCscFeature;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.DmProfileLoader;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorVzw extends RegistrationGovernorBase {
    protected static final int DEFAULT_RETRY_AFTER = 30;
    protected static final int DEFAULT_TIMS_TIMER = 120;
    protected static final int DELAY_FOR_CDMALESS_MODEL = 6;
    private static final int DELAY_FOR_CDMA_HANDOVER = 3;
    protected static final String INTENT_ACTION_TRIGGER_OMADM_TREE_SYNC = "com.samsung.sdm.START_DM_SYNC_SESSION";
    private static final String LOG_TAG = "RegiGvnVzw";
    protected CallSnapshot mCallSnapshot = new CallSnapshot();
    protected boolean mDmLastEabEnabled = false;
    protected boolean mDmLastLvcEnabled = false;
    protected boolean mDmLastVceEnabled = false;
    protected boolean mDmLastVolteEnabled = false;
    protected int mDmTimsTimer = 120;
    protected boolean mDmVolteNodeUpdated = false;
    protected boolean mHasPendingDeregistration = false;
    protected boolean mHasPendingOmadmUpdate = false;
    protected boolean mHasPendingReregistration = false;
    protected boolean mIsInviteForbidden = false;
    protected boolean mOverrideEpdgCellularPref = false;
    protected final int[] mRegRetryTime = {0, 30, 30, 60, 120, NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE, 900};

    private static class CallSnapshot {
        /* access modifiers changed from: private */
        public int mCallType;
        /* access modifiers changed from: private */
        public SipError mError;
        /* access modifiers changed from: private */
        public IRegistrationGovernor.CallEvent mEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_UNKNOWN;

        public void setCallSnapshot(IRegistrationGovernor.CallEvent event, SipError error, int callType) {
            this.mEvent = event;
            this.mError = error;
            this.mCallType = callType;
        }

        public void clear() {
            this.mEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_UNKNOWN;
        }

        public boolean isEmpty() {
            return this.mEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_UNKNOWN;
        }
    }

    RegistrationGovernorVzw(Context ctx) {
        this.mContext = ctx;
    }

    public RegistrationGovernorVzw(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        String str;
        boolean readSwitch = DmConfigHelper.readSwitch(this.mContext, "mmtel", true, this.mPhoneId);
        this.mDmLastVolteEnabled = readSwitch;
        if (!readSwitch && "LRA".equals(this.mMno.getMatchedSalesCode(OmcCode.get()))) {
            IMSLog.i(LOG_TAG, task.getPhoneId(), "Turn off SMS_OVER_IP based on VOLTE_ENABLED");
            IMSLog.c(LogClass.LRA_OOB_SMSIP_OFF, this.mPhoneId + ",OFF:SMS_OVER_IP");
            NvConfiguration.setSmsIpNetworkIndi(this.mContext, false, this.mPhoneId);
        }
        this.mDmLastLvcEnabled = DmConfigHelper.readSwitch(this.mContext, "mmtel-video", true, this.mPhoneId);
        this.mDmLastEabEnabled = DmConfigHelper.readSwitch(this.mContext, "presence", true, this.mPhoneId);
        this.mDmLastVceEnabled = readVCEConfigValue(context);
        this.mDmTimsTimer = readDmTimsTimer(context);
        Log.i(LOG_TAG, "RegistrationGovernorVzw: mDmLastVceEnabled[" + this.mDmLastVceEnabled + "]");
        task.getProfile().setVceConfigEnabled(this.mDmLastVceEnabled);
        try {
            if (this.mDmLastVolteEnabled) {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            } else {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 0);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if ("BAE1000000000000".equalsIgnoreCase(telephonyManager.getGroupIdLevel1())) {
            try {
                ImsConstants.SystemSettings.setVoiceCallType(context, 0, this.mPhoneId);
                IMSLog.c(LogClass.FKR_VOLTE_FORCED_ON, this.mPhoneId + ",FKR VLT ON");
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
        }
        this.mPcoType = RegistrationGovernor.PcoType.PCO_DEFAULT;
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append(",");
        String str2 = "1";
        sb.append(this.mDmLastVolteEnabled ? str2 : "0");
        sb.append(",");
        sb.append(this.mDmLastLvcEnabled ? str2 : "0");
        sb.append(",");
        if (this.mDmLastEabEnabled) {
            str = str2;
        } else {
            str = "0";
        }
        sb.append(str);
        sb.append(",");
        sb.append(!this.mDmLastVceEnabled ? "0" : str2);
        IMSLog.c(LogClass.VZW_OMADM_VALUES, sb.toString());
    }

    private boolean readVCEConfigValue(Context context) {
        String mdmnType = ImsCscFeature.getInstance().getString(this.mPhoneId, CscFeatureTagIMS.TAG_CSCFEATURE_IMS_CONFIGMDMNTYPE);
        Log.i(LOG_TAG, "CSC MDMN type: " + mdmnType);
        return mdmnType.toUpperCase().contains("MEP".toUpperCase()) && NvConfiguration.get(context, "VCE_CONFIG", "0").equals("1");
    }

    private int readDmTimsTimer(Context context) {
        return DmConfigHelper.readInt(context, ConfigConstants.ConfigPath.OMADM_VZW_TIMS_TIMER, 120).intValue();
    }

    /* access modifiers changed from: protected */
    public boolean checkEpdgEvent(int rat) {
        if (rat != 18 || this.mPdnController.isEpdgConnected(this.mPhoneId) || !this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
            return true;
        }
        Log.i(LOG_TAG, "EPDG is not actually connected");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!isSVLTEDevice()) {
            if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
                if (this.mTask.getRegistrationRat() == 13 && TelephonyManagerExt.getNetworkClass(this.mRegMan.getNetworkEvent(this.mPhoneId).network) == 2 && this.mTelephonyManager.isNetworkRoaming()) {
                    Log.i(LOG_TAG, "Keep going IMS deregistration");
                } else {
                    if (ImsUtil.isCdmalessEnabled(this.mPhoneId)) {
                        if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                            Log.i(LOG_TAG, "Call status is not idle but CDMA-less should allow this.");
                        }
                    }
                    Log.i(LOG_TAG, "TelephonyCallStatus is not idle");
                    return false;
                }
            }
            return true;
        } else if (this.mVsm.getSessionCount(this.mPhoneId) <= 0 || this.mVsm.hasEmergencyCall(this.mPhoneId)) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int rat) {
        if (!this.mHasPendingOmadmUpdate) {
            return true;
        }
        Log.i(LOG_TAG, "mHasPendingOmadmUpdate is enabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_OTA.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkVowifiSetting(int rat) {
        if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURED) || rat != 18 || this.mRegMan.isVoWiFiSupported(this.mPhoneId)) {
            return true;
        }
        Log.i(LOG_TAG, "VoWiFi feature is not enabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.CSC_DISABLED.getCode());
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkNetworkEvent(rat) && checkCallStatus() && checkRegiStatus() && checkVowifiSetting(rat) && checkEpdgEvent(rat));
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        this.mTask.mKeepPdn = false;
        stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_REGISTERED);
        if (!this.mCallSnapshot.isEmpty()) {
            onCallStatus(this.mCallSnapshot.mEvent, this.mCallSnapshot.mError, this.mCallSnapshot.mCallType);
            this.mCallSnapshot.clear();
        }
    }

    public boolean isThrottled() {
        if (!this.mDiscardCurrentNetwork) {
            return super.isThrottled();
        }
        Log.i(LOG_TAG, "Under discard current network. Do not try IMS registration.");
        return true;
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        int retryAfter2;
        Log.e(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistrationError :");
        sb.append(error);
        sb.append(", fail count : ");
        sb.append(this.mFailureCounter);
        eventLog.logAndAdd(sb.toString());
        if (!this.mCallSnapshot.isEmpty()) {
            Log.i(LOG_TAG, "handle call snapshot");
            onCallStatus(this.mCallSnapshot.mEvent, this.mCallSnapshot.mError, this.mCallSnapshot.mCallType);
            this.mCallSnapshot.clear();
        }
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            this.mFailureCounter = 0;
            this.mCurPcscfIpIdx = 0;
            if (unsolicit) {
                retryAfter2 = 60;
            } else {
                retryAfter2 = 1;
            }
            this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter2) * 1000);
            startRetryTimer(((long) retryAfter2) * 1000);
        } else if (!ImsUtil.isCdmalessEnabled(this.mPhoneId) || getPcoType() != RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION) {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx = (this.mCurPcscfIpIdx + 1) % this.mNumOfPcscfIp;
            if (this.mTask.getProfile().hasEmergencySupport()) {
                if (SipErrorBase.SIP_TIMEOUT.equals(error) && this.mFailureCounter < 2) {
                    this.mRegHandler.sendTryRegister(this.mPhoneId);
                }
            } else if (SipErrorBase.USE_PROXY.equals(error)) {
                Log.e(LOG_TAG, "onRegistrationError: start from 1st P-CSCF.");
                this.mCurPcscfIpIdx = 0;
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                startRetryTimer(((long) retryAfter) * 1000);
            } else if (SipErrorBase.BAD_REQUEST.equals(error) || SipErrorBase.PAYMENT_REQUIRED.equals(error)) {
                if (unsolicit) {
                    this.mIsPermanentStopped = true;
                } else if (this.mFailureCounter > 1) {
                    this.mIsPermanentStopped = true;
                } else {
                    if (retryAfter == 0) {
                        retryAfter = 30;
                    }
                    this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                    startRetryTimer(((long) retryAfter) * 1000);
                }
            } else if (!SipErrorBase.isImsForbiddenError(error) && SipErrorBase.NOT_FOUND.getCode() != error.getCode()) {
                if (this.mFailureCounter > 2 && this.mCurPcscfIpIdx == 0) {
                    Log.e(LOG_TAG, "onRegistrationError: all PCSCF failed to Regi");
                    if (!this.mRegMan.getCsfbSupported(this.mPhoneId) && (ImsUtil.isCdmalessEnabled(this.mPhoneId) || this.mTelephonyManager.isNetworkRoaming())) {
                        Log.e(LOG_TAG, "onRegistrationError: Discard current N/W. CSFB is unavailable");
                        this.mDiscardCurrentNetwork = true;
                    }
                }
                if (unsolicit) {
                    this.mFailureCounter++;
                }
                if (retryAfter == 0) {
                    retryAfter = getWaitTime();
                }
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                startRetryTimer(((long) retryAfter) * 1000);
            } else if (this.mCurPcscfIpIdx == 0 && this.mCurImpu == 1) {
                this.mIsPermanentStopped = true;
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                Log.i(LOG_TAG, "onRegistrationError: Failed for all PCSCFs with IMSI_BASED");
            } else {
                if (this.mCurPcscfIpIdx == 0) {
                    this.mRegMan.getEventLog().logAndAdd("try regi with IMSI for next Registration");
                    this.mCurImpu = 1;
                }
                if (retryAfter == 0) {
                    retryAfter = 30;
                }
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
                startRetryTimer(((long) retryAfter) * 1000);
            }
        } else {
            this.mRegMan.getEventLog().logAndAdd("RegiGvnVzw: Discard current network immediately when PCO=5");
            this.mDiscardCurrentNetwork = true;
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        if (services == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            if (!ImsUtil.isCdmalessEnabled(this.mPhoneId) || this.mTelephonyManager.isNetworkRoaming()) {
                removeService(services, "mmtel-video", "VoPS Off");
                removeService(services, "mmtel", "VoPS Off");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_VOPS_OFF.getCode());
            } else {
                Log.i(LOG_TAG, "CDMALess and VoPS not Supported. Disable VZW LTE PLMN.");
                return new HashSet();
            }
        }
        return services;
    }

    public void onPublishError(SipError error) {
        if (error.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED) || error.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED2)) {
            this.mTask.setReason("Publish Error. ReRegister..");
            this.mRegMan.sendReRegister(this.mTask);
        } else if (ImsCallUtil.isImsOutageError(error)) {
            this.mTask.setDeregiReason(45);
            this.mRegMan.deregister(this.mTask, true, false, "Publish Error. DeRegister..");
        }
    }

    public void onSubscribeError(int event, SipError error) {
        Log.e(LOG_TAG, "onSubscribeError: state " + this.mTask.getState() + ", error " + error + ", event " + event);
        if (error.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED) || error.equalsWithStrict(SipErrorBase.USER_NOT_REGISTERED2)) {
            this.mTask.setReason("Subscribe Error. ReRegister..");
            this.mRegMan.sendReRegister(this.mTask);
        } else if (!SipErrorBase.isImsOutageError(error)) {
        } else {
            if (!this.mHasVoLteCall) {
                this.mTask.setDeregiReason(44);
                this.mRegMan.deregister(this.mTask, true, false, "Subscribe Error. DeRegister..");
                return;
            }
            this.mHasPendingDeregistration = true;
            if (this.mTask.getImsRegistration() != null) {
                this.mTask.getImsRegistration().setProhibited(true);
            }
        }
    }

    public void onTimsTimerExpired() {
        super.onTimsTimerExpired();
        if (!this.mTelephonyManager.isNetworkRoaming() || !this.mRegMan.getCsfbSupported(this.mPhoneId)) {
            resetRetry();
            stopRetryTimer();
            this.mDiscardCurrentNetwork = true;
            return;
        }
        Log.i(LOG_TAG, "Continue IMS regi retry...");
    }

    private boolean isCallTypeVideo(int callType) {
        return callType == 2 || callType == 4 || callType == 3;
    }

    private boolean needReRegiOnCallStatusChanged(int phoneId, IRegistrationGovernor.CallEvent event, int callType) {
        if (this.mTask.getRegistrationRat() == 18) {
            if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED) {
                if (this.mTask.getImsRegistration() != null && !this.mTask.getImsRegistration().hasService("mmtel")) {
                    if (isCallTypeVideo(callType) || getVoiceTechType(phoneId) == 0) {
                        this.mOverrideEpdgCellularPref = true;
                        Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, re-regi case");
                        return true;
                    }
                    Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, VOWIFI or VoLTE disabled case, no re-regi");
                    return false;
                }
            } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END && this.mOverrideEpdgCellularPref) {
                this.mOverrideEpdgCellularPref = false;
                if (this.mVsm != null) {
                    EpdgManager epdgMgr = this.mVsm.getEpdgManager();
                    if (epdgMgr == null) {
                        Log.i(LOG_TAG, "Can not find epdgManager");
                    } else if (epdgMgr.isPossibleW2LHOAfterCallEnd()) {
                        Log.i(LOG_TAG, "W2L indication from EpdgManager will be coming.");
                        return false;
                    } else if (this.mTask.getRegistrationRat() == 18) {
                        if (!isVoiceOverWifiPreferred()) {
                            Log.i(LOG_TAG, "re-regi case");
                            return true;
                        }
                        Log.i(LOG_TAG, "VoWiFi pref. re-regi not required");
                        return false;
                    }
                } else {
                    Log.e(LOG_TAG, "VolteServiceModule is null");
                }
                if (this.mTelephonyManager.getNetworkType() != 13 || this.mPdnController.getVopsIndication(phoneId) == VoPsIndication.NOT_SUPPORTED) {
                    Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, re-regi case");
                    return true;
                }
                Log.i(LOG_TAG, "needReRegiOnCallStatusChanged, LTE HO case no need for re-regi");
                return false;
            }
        } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED) {
            this.mOverrideEpdgCellularPref = true;
        } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            this.mOverrideEpdgCellularPref = false;
        }
        return false;
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent event, SipError error, int callType) {
        Log.i(LOG_TAG, "onCallStatus: event=" + event + " error=" + error);
        IUserAgent ua = this.mTask.getUserAgent();
        if (ua == null || !ua.isRegistering()) {
            if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
                if (this.mHasPendingDeregistration) {
                    this.mTask.setDeregiReason(47);
                    this.mRegMan.deregister(this.mTask, true, this.mTask.mKeepPdn, "onCallStatus: process pending deregistration.");
                    this.mHasPendingDeregistration = false;
                }
                if (this.mHasPendingReregistration) {
                    Log.i(LOG_TAG, "onCallStatus: process pending updateRegistration.");
                    this.mRegMan.addPendingUpdateRegistration(this.mTask, 0);
                    this.mHasPendingReregistration = false;
                }
                this.mHasVoLteCall = false;
            } else {
                super.onCallStatus(event, error, callType);
            }
            if (this.mRegMan.isVoWiFiSupported(this.mPhoneId) && needReRegiOnCallStatusChanged(this.mPhoneId, event, callType)) {
                this.mRegMan.sendReRegister(this.mTask);
                return;
            }
            return;
        }
        Log.i(LOG_TAG, "onCallStatus: defer call status event to registration done");
        this.mCallSnapshot.setCallSnapshot(event, error, callType);
    }

    private SipError onSipError_MmtelVoice(String service, SipError error) {
        boolean isCDMALessEnabled = ImsUtil.isCdmalessEnabled(this.mPhoneId);
        if (SipErrorVzw.FORBIDDEN_ORIG_USER_NOT_REGISTERED.equals(error) || SipErrorVzw.FORBIDDEN_ORIG_USER_NOT_REGISTERED2.equals(error)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, true, 0, "403 Forbidden");
            return error;
        } else if (SipErrorVzw.FORBIDDEN_USER_NOT_AUTHORIZED_FOR_SERVICE.equals(error) && !isCDMALessEnabled) {
            this.mRegMan.getEventLog().logAndAdd("Volte service will be disable for 403 Forbidden");
            this.mRegMan.setInvite403DisableService(true, this.mPhoneId);
            Intent intent = new Intent(INTENT_ACTION_TRIGGER_OMADM_TREE_SYNC);
            intent.setPackage(ImsConstants.Packages.PACKAGE_SDM);
            this.mContext.sendBroadcast(intent);
            this.mRegMan.sendReRegister(this.mTask);
            return error;
        } else if (SipErrorBase.isImsOutageError(error)) {
            if (!this.mHasVoLteCall) {
                int delay = 3;
                if (isCDMALessEnabled) {
                    delay = 0;
                }
                this.mTask.mKeepPdn = true;
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, this.mTask.mKeepPdn, delay * 1000, "503 Service Unavailable: IMS Outage for voice service");
                return new SipError(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_NOT_REQUIRED, error.getReason());
            }
            this.mTask.mKeepPdn = true;
            this.mHasPendingDeregistration = true;
            if (this.mTask.getImsRegistration() != null) {
                this.mTask.getImsRegistration().setProhibited(true);
            }
            return SipErrorBase.FORBIDDEN;
        } else if (ImsCallUtil.isTimerVzwExpiredError(error) || 1702 == error.getCode() || 2507 == error.getCode()) {
            this.mTask.setDeregiReason(49);
            this.mRegMan.deregister(this.mTask, true, true, "vzw timer expired");
            return error;
        } else if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) || SipErrorBase.SIP_TIMEOUT.equals(error)) {
            if (!this.mHasVoLteCall || (!isCDMALessEnabled && (!this.mTelephonyManager.isNetworkRoaming() || !SipErrorBase.SIP_INVITE_TIMEOUT.equals(error)))) {
                removeCurrentPcscfAndInitialRegister(false);
                return error;
            }
            this.mTask.mKeepPdn = true;
            this.mHasPendingDeregistration = true;
            return error;
        } else if (!isCDMALessEnabled || !SipErrorBase.PRECONDITION_FAILURE.equals(error)) {
            return super.onSipError(service, error);
        } else {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, this.mTask.mKeepPdn, 6000, "VoLTE call setup failure");
            return error;
        }
    }

    public SipError onSipError(String service, SipError error) {
        Log.e(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if ("mmtel".equals(service)) {
            return onSipError_MmtelVoice(service, error);
        }
        if ("smsip".equals(service)) {
            if (!SipErrorBase.isImsOutageError(error)) {
                return super.onSipError(service, error);
            }
            if (this.mHasVoLteCall) {
                return error;
            }
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, true, "503 Service Unavailable: IMS Outage for SMS service request");
            return error;
        } else if (!SipErrorBase.isImsOutageError(error)) {
            return super.onSipError(service, error);
        } else {
            if (!this.mHasVoLteCall) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, false, "503 Service Unavailable: IMS Outage for Non-voice request");
                return error;
            }
            this.mHasPendingDeregistration = true;
            if (this.mTask.getImsRegistration() == null) {
                return error;
            }
            this.mTask.getImsRegistration().setProhibited(true);
            return error;
        }
    }

    /* access modifiers changed from: protected */
    public int getWaitTime() {
        int currentRetryTimer = Math.min(this.mFailureCounter, this.mRegRetryTime.length - 1);
        int random = 0;
        if (currentRetryTimer == 3) {
            random = (int) (Math.random() * 15.0d);
        }
        return this.mRegRetryTime[currentRetryTimer] + random;
    }

    public void checkProfileUpdateFromDM(boolean force) {
        Log.i(LOG_TAG, "checkProfileUpdateFromDM()");
        this.mTask.setProfile(DmProfileLoader.getProfile(this.mContext, this.mTask.getProfile(), this.mPhoneId));
    }

    private boolean isVoiceOverWifiEnabled() {
        boolean isVowifiEnabled = VowifiConfig.isEnabled(this.mContext, this.mPhoneId);
        boolean isRoaming = this.mTelephonyManager.isNetworkRoaming();
        if (isRoaming && isVowifiEnabled) {
            boolean z = false;
            if (VowifiConfig.getRoamPrefMode(this.mContext, 0, this.mPhoneId) == 1) {
                z = true;
            }
            isVowifiEnabled = z;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "VoWiFi pref: " + isVowifiEnabled + ", isRoaming: " + isRoaming);
        return isVowifiEnabled;
    }

    private boolean isVoiceOverWifiPreferred() {
        boolean isVowifiPreferred = isVoiceOverWifiEnabled();
        boolean isCdmaAvailable = this.mRegMan.isCdmaAvailableForVoice(this.mPhoneId);
        Log.i(LOG_TAG, "isVoiceOverWifiPreferred: isVowifiPreferred [" + isVowifiPreferred + "] isCdmaAvailableForVoice : [" + isCdmaAvailable + "]");
        if (this.mTelephonyManager.isNetworkRoaming()) {
            return isVowifiPreferred;
        }
        return isVowifiPreferred && !isCdmaAvailable;
    }

    public Set<String> filterService(Set<String> services, int network) {
        boolean dataAllowed;
        boolean isVTcallExist;
        String log;
        int i = network;
        Set<String> filteredServices = super.filterService(services, network);
        if (filteredServices.isEmpty()) {
            return new HashSet();
        }
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.IMS_TEST_MODE_PROP, 0) == 1) {
            Log.i(LOG_TAG, "by VZW IMS_TEST_MODE_PROP - remove all service");
            return new HashSet();
        }
        if (this.mTask.getProfile().hasEmergencySupport() && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 29) {
            removeService(filteredServices, "smsip", "by unsupported E911 over SMS");
        }
        Set<String> filteredServices2 = applyMmtelUserSettings(filteredServices, i);
        if (i == 13 && !this.mRegMan.getVolteAllowedWithDsac()) {
            removeService(filteredServices2, "mmtel", "by DSAC feature");
            removeService(filteredServices2, "mmtel-video", "by DSAC feature");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_DSAC.getCode());
        }
        boolean keepMsisdnValidation = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.KEEP_MSISDN_VALIDATION, true);
        if (this.mCurImpu == 1 || (!this.mTelephonyManager.validateMsisdn(SimUtil.getSubId(this.mPhoneId)) && keepMsisdnValidation)) {
            if (ImsUtil.isCdmalessEnabled(this.mPhoneId)) {
                removeService(filteredServices2, "mmtel-video", "CDMALess IMSI based");
            } else {
                removeService(filteredServices2, "mmtel-video", "by limited regi");
                removeService(filteredServices2, "mmtel", "by limited regi");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_LIMITED_MODE.getCode());
            }
        }
        if (this.mRegMan.isInvite403DisabledService(this.mPhoneId)) {
            removeService(filteredServices2, "mmtel-video", "Invite 403");
            removeService(filteredServices2, "mmtel", "Invite 403");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_INVITE_403.getCode());
        }
        if (i == 13) {
            filteredServices2 = applySSACPolicy(filteredServices2);
        }
        boolean isVTcallExist2 = this.mVsm != null && this.mVsm.getCallCount(this.mPhoneId)[1] > 0;
        boolean isRoaming = this.mTelephonyManager.isNetworkRoaming();
        if (isRoaming) {
            dataAllowed = Settings.Global.getInt(this.mContext.getContentResolver(), "data_roaming", 0) == 1;
        } else {
            dataAllowed = NetworkUtil.isMobileDataOn(this.mContext);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "DataAllowed: romaing [" + isRoaming + "]: " + dataAllowed);
        if ((i != 18 && !dataAllowed) || !this.mDmLastVolteEnabled || !this.mDmLastEabEnabled) {
            if (isVTcallExist2) {
                if (this.mTask.getImsRegistration() == null || this.mTask.getImsRegistration().hasService("mmtel-video")) {
                    Log.i(LOG_TAG, "by EAB false but activated VT call is exist.");
                    this.mHasPendingReregistration = true;
                }
            } else if (!filteredServices2.isEmpty()) {
                if (!this.mDmLastVolteEnabled || !this.mDmLastEabEnabled) {
                    log = "by DM : volte(" + this.mDmLastVolteEnabled + "), eab(" + this.mDmLastEabEnabled + ")";
                } else if (!dataAllowed) {
                    log = "by mobile data off";
                } else {
                    log = "remove mmtel-video";
                }
                removeService(filteredServices2, "mmtel-video", log);
            }
        }
        if (!this.mDmLastVolteEnabled) {
            Log.i(LOG_TAG, "by volteEnabled false - presence");
            removeService(filteredServices2, "presence", "by volteEnabled false");
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            List<String> enabledRcsServices = RcsConfigurationHelper.getRcsEnabledServiceList(this.mContext, this.mPhoneId, ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, this.mTask.getProfile()));
            for (String service : ImsProfile.getRcsServiceList()) {
                if (!enabledRcsServices.contains(service)) {
                    removeService(filteredServices2, service, "Disable from ACS");
                }
            }
            boolean isRcsEnabledByUser = true;
            if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) != 1) {
                isRcsEnabledByUser = false;
            }
            if (!isRcsEnabledByUser || (isRoaming && i != 18)) {
                String[] rcsServiceList = ImsProfile.getRcsServiceList();
                int length = rcsServiceList.length;
                int i2 = 0;
                while (i2 < length) {
                    String service2 = rcsServiceList[i2];
                    if (!"presence".equals(service2)) {
                        StringBuilder sb = new StringBuilder();
                        isVTcallExist = isVTcallExist2;
                        sb.append("Roaming:");
                        sb.append(isRoaming);
                        removeService(filteredServices2, service2, sb.toString());
                    } else {
                        isVTcallExist = isVTcallExist2;
                    }
                    i2++;
                    int i3 = network;
                    isVTcallExist2 = isVTcallExist;
                }
            } else {
                boolean z = isVTcallExist2;
            }
            if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
                for (String service3 : ImsProfile.getRcsServiceList()) {
                    if (!"presence".equals(service3)) {
                        removeService(filteredServices2, service3, "No DualRcs");
                    }
                }
            }
        }
        if (isRoaming) {
            filteredServices2 = applyCsfbSupported(filteredServices2);
        }
        SlotBasedConfig slotInfo = SlotBasedConfig.getInstance(this.mPhoneId);
        if (!(slotInfo == null || this.mTask.getProfile().getTtyType() == 2 || this.mTask.getProfile().getTtyType() == 4 || !slotInfo.getTTYMode())) {
            removeService(filteredServices2, "mmtel-video", "TTY ON");
            removeService(filteredServices2, "mmtel", "TTY ON");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_CS_TTY.getCode());
        }
        return filteredServices2;
    }

    public void startOmadmProvisioningUpdate() {
        this.mHasPendingOmadmUpdate = true;
        setRadioPower(false);
        this.mRegHandler.sendFinishOmadmProvisioningUpdate(this.mTask, 10000);
    }

    public void finishOmadmProvisioningUpdate() {
        this.mHasPendingOmadmUpdate = false;
        setRadioPower(true);
    }

    public void resetRetry() {
        Log.i(LOG_TAG, "resetRetry()");
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegiAt = 0;
        if (this.mPcoType != RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION) {
            this.mCurImpu = 0;
        }
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 4 || releaseCase == 1) {
            this.mIsPermanentStopped = false;
            this.mCurImpu = 0;
            this.mPcoType = RegistrationGovernor.PcoType.PCO_DEFAULT;
        } else if (releaseCase == 5) {
            resetRetry();
            stopRetryTimer();
        } else if (releaseCase == 9 || releaseCase == 6) {
            this.mDiscardCurrentNetwork = false;
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + releaseCase);
        }
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        Log.i(LOG_TAG, "onPdnRequestFailed: " + reason + ", counter: " + this.mPdnRejectCounter);
        if (this.mTask.getPdnType() != 11 || this.mTask.getRegistrationRat() != 13 || ImsUtil.isCdmalessEnabled(this.mPhoneId) || this.mTelephonyManager.isNetworkRoaming() || DeviceUtil.isTablet()) {
            Log.i(LOG_TAG, "Do not notify");
            return;
        }
        boolean notify = false;
        if ("PDN_THROTTLED".equalsIgnoreCase(reason)) {
            notify = true;
        } else {
            this.mPdnRejectCounter++;
            if (this.mPdnRejectCounter >= 2) {
                notify = true;
            }
        }
        if (notify) {
            Log.i(LOG_TAG, "notifyImsNotAvailable");
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
            this.mPdnRejectCounter = 0;
        }
    }

    public void onConfigUpdated() {
        boolean dmVolteEnabled = DmConfigHelper.readSwitch(this.mContext, "mmtel", true, this.mPhoneId);
        boolean dmLvcEnabled = DmConfigHelper.readSwitch(this.mContext, "mmtel-video", true, this.mPhoneId);
        boolean dmEabEnabled = DmConfigHelper.readSwitch(this.mContext, "presence", true, this.mPhoneId);
        boolean dmVceEnabled = readVCEConfigValue(this.mContext);
        int dmTimsTimer = readDmTimsTimer(this.mContext);
        Log.i(LOG_TAG, "onConfigUpdated: VOLTE_ENABLED [" + this.mDmLastVolteEnabled + "] -> [" + dmVolteEnabled + "]");
        if (dmVolteEnabled != this.mDmLastVolteEnabled) {
            this.mDmLastVolteEnabled = dmVolteEnabled;
            if (dmVolteEnabled) {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 1);
            } else {
                ImsConstants.SystemSettings.VOLTE_PROVISIONING.set(this.mContext, 0);
                if ("LRA".equals(this.mMno.getMatchedSalesCode(OmcCode.get()))) {
                    NvConfiguration.setSmsIpNetworkIndi(this.mContext, false, this.mPhoneId);
                    this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "Turn off SMS_OVER_IP based on VOLTE_ENABLED");
                    IMSLog.c(LogClass.LRA_SMSIP_OFF_OMADM, this.mPhoneId + ",OFF:SMS_OVER_IP");
                }
            }
            this.mRegMan.setInvite403DisableService(false, this.mPhoneId);
            if (this.mTelephonyManager.getCallState() != 0) {
                this.mDmVolteNodeUpdated = true;
            } else if (this.mTask.getRegistrationRat() == 13 || this.mTask.getRegistrationRat() == 14) {
                Log.i(LOG_TAG, "onConfigUpdated, need network detach/reattach");
                this.mHasPendingOmadmUpdate = true;
                this.mRegHandler.sendOmadmProvisioningUpdateStarted(this.mTask);
            } else {
                this.mTask.setDeregiReason(29);
                this.mRegMan.deregister(this.mTask, false, false, "profile updated");
            }
        }
        Log.i(LOG_TAG, "onConfigUpdated: VCE_CONFIG [" + this.mDmLastVceEnabled + "] -> [" + dmVceEnabled + "]");
        if (dmVceEnabled != this.mDmLastVceEnabled) {
            this.mDmLastVceEnabled = dmVceEnabled;
            this.mTask.getProfile().setVceConfigEnabled(this.mDmLastVceEnabled);
            this.mRegMan.updateVceConfig(this.mTask, this.mDmLastVceEnabled);
        }
        Log.i(LOG_TAG, "onConfigUpdated: VZW_TIMS_TIMER [" + this.mDmTimsTimer + "] -> [" + dmTimsTimer + "]");
        if (this.mDmTimsTimer != dmTimsTimer) {
            this.mDmTimsTimer = dmTimsTimer;
        }
        if (this.mDmVolteNodeUpdated || this.mHasPendingOmadmUpdate || dmLvcEnabled != this.mDmLastLvcEnabled || dmEabEnabled != this.mDmLastEabEnabled) {
            this.mDmLastLvcEnabled = dmLvcEnabled;
            this.mDmLastEabEnabled = dmEabEnabled;
            ImsRegistry.getServiceModuleManager().notifyOmadmVolteConfigDone(this.mPhoneId);
        }
    }

    public void onTelephonyCallStatusChanged(int callState) {
        super.onTelephonyCallStatusChanged(callState);
        if (this.mDmVolteNodeUpdated && callState == 0) {
            this.mDmVolteNodeUpdated = false;
            if (this.mTask.getRegistrationRat() == 13 || this.mTask.getRegistrationRat() == 14) {
                this.mRegHandler.sendOmadmProvisioningUpdateStarted(this.mTask);
                return;
            }
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, false, false, "call state changed");
        }
    }

    public void onDeregistrationDone(boolean requested) {
        super.onDeregistrationDone(requested);
        if (this.mTask.getPdnType() != 11) {
            return;
        }
        if (this.mTask.getDeregiReason() == 2) {
            startTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
        } else if (this.mTask.getDeregiReason() == 76) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Local deregi done by SSAC. Discard current network!");
            this.mDiscardCurrentNetwork = true;
        }
    }

    public void startTimsTimer(String reason) {
        if (this.mTelephonyManager.isNetworkRoaming() || ImsUtil.isCdmalessEnabled(this.mPhoneId)) {
            int i = this.mDmTimsTimer;
            if (i == 9999) {
                Log.i(LOG_TAG, "ignore Tims timer for labtest");
                return;
            }
            if (i < 0 || i > 255) {
                Log.i(LOG_TAG, "startTimsTimer; Invalid DM value [" + this.mDmTimsTimer + "] for Tims Timer. Use default value[120].");
                this.mDmTimsTimer = 120;
            }
            startTimsEstablishTimer(this.mTask, ((long) this.mDmTimsTimer) * 1000, reason);
            return;
        }
        Log.i(LOG_TAG, "ignore Tims timer for hVoLTE device in VZW NW");
    }

    public void stopTimsTimer(String reason) {
        stopTimsEstablishTimer(this.mTask, reason);
    }

    private Set<String> applySSACPolicy(Set<String> services) {
        if (services == null) {
            return new HashSet();
        }
        if (!SlotBasedConfig.getInstance(this.mPhoneId).isSsacEnabled()) {
            if (ImsUtil.isCdmalessEnabled(this.mPhoneId) && !this.mRegMan.getCsfbSupported(this.mPhoneId)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "applySSACPolicy: CSFB is not available. Discard PLMN.");
                return new HashSet();
            } else if (!isSVLTEDevice()) {
                removeService(services, "mmtel-video", "by SSAC");
                removeService(services, "mmtel", "by SSAC");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_SSAC_BARRING.getCode());
            }
        }
        return services;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyCsfbSupported(Set<String> services) {
        if (services.contains("mmtel") || this.mRegMan.getCsfbSupported(this.mPhoneId)) {
            return services;
        }
        return new HashSet();
    }

    private void setRadioPower(boolean turnOn) {
        Log.i(LOG_TAG, "setRadioPower [" + turnOn + "]");
        this.mTelephonyManager.setRadioPower(turnOn);
    }

    private boolean isSVLTEDevice() {
        String property = SemSystemProperties.get("ro.ril.svlte1x");
        if (property == null || property.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(property);
    }

    public boolean onUpdatedPcoInfo(String pdn, int pco) {
        if (!DeviceConfigManager.IMS.equalsIgnoreCase(pdn)) {
            return false;
        }
        RegistrationGovernor.PcoType pcoType = RegistrationGovernor.PcoType.fromType(pco);
        Log.i(LOG_TAG, "onUpdatedPcoInfo: PCO Type: " + pcoType);
        setPcoType(pcoType);
        if (pcoType == RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION && ImsUtil.isCdmalessEnabled(this.mPhoneId)) {
            this.mCurImpu = 1;
            Log.i(LOG_TAG, "set PREFERED_IMPU as IMSI_BASED");
        }
        return true;
    }

    public void resetPcoType() {
        this.mPcoType = RegistrationGovernor.PcoType.PCO_DEFAULT;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> services, int network) {
        if (services == null) {
            return new HashSet();
        }
        if (!(ImsUtil.isCdmalessEnabled(this.mPhoneId) || network == 18 || getVoiceTechType(this.mPhoneId) == 0)) {
            removeService(services, "mmtel", "by voice type cs");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
        }
        if (!services.contains("mmtel")) {
            removeService(services, "mmtel-video", "by no mmtel");
        }
        if (this.mRegMan.isVoWiFiSupported(this.mPhoneId) && network == 18) {
            boolean isVowifiPreferred = isVoiceOverWifiPreferred();
            if (!this.mOverrideEpdgCellularPref) {
                if (!VowifiConfig.isEnabled(this.mContext, this.mPhoneId)) {
                    removeService(services, "mmtel", "by VoWiFi settings");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
                }
                if (!(this.mTelephonyManager.getVoiceNetworkType() == 13 && this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.SUPPORTED) && !isVowifiPreferred) {
                    removeService(services, "mmtel", "by VowifiPreferred");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
                }
            }
        }
        return services;
    }

    public boolean isLocationInfoLoaded(int rat) {
        return true;
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        if (foundBestRat != 0) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "isNeedToDeRegistration: no IMS service for network " + currentRat + ". Deregister.");
        RegisterTask registerTask = this.mTask;
        registerTask.setReason("no IMS service for network : " + currentRat);
        this.mTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(this.mTask, NetworkUtil.isLegacy3gppNetwork(currentRat), false);
        return true;
    }

    public boolean needPendingPdnConnected() {
        if (!ImsUtil.isCdmalessEnabled(this.mPhoneId) || this.mTask.getProfile().hasEmergencySupport()) {
            return false;
        }
        if (this.mPcoType == RegistrationGovernor.PcoType.PCO_DEFAULT) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnConnected: Pending 1 sec! PCO not yet received.");
            this.mRegHandler.sendMessageDelayed(this.mRegHandler.obtainMessage(22, this.mTask), 1000);
            setPcoType(RegistrationGovernor.PcoType.PCO_AWAITING);
            return true;
        } else if (this.mPcoType != RegistrationGovernor.PcoType.PCO_AWAITING) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnConnected: 1 sec delay has expired!");
            setPcoType(RegistrationGovernor.PcoType.PCO_POSTPAY);
            return false;
        }
    }

    public String toString() {
        return "RegistrationGovernorVzw [mHasPendingDeregistration=" + this.mHasPendingDeregistration + ", mDmLastVolteEnabled=" + this.mDmLastVolteEnabled + ", mDmLastLvcEnabled=" + this.mDmLastLvcEnabled + ", mDmLastEabEnabled=" + this.mDmLastEabEnabled + ", mDmLastVceEnabled=" + this.mDmLastVceEnabled + ", mIsInviteForbidden=" + this.mIsInviteForbidden + ", mDmVolteNodeUpdated=" + this.mDmVolteNodeUpdated + ", mHasPendingOmadmUpdate=" + this.mHasPendingOmadmUpdate + ", mOverrideEpdgCellularPref=" + this.mOverrideEpdgCellularPref + ", mIsVolteAllowedWithDsac=" + this.mRegMan.getVolteAllowedWithDsac() + "]" + super.toString();
    }
}
