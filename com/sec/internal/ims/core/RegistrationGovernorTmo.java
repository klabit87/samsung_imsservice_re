package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RegistrationGovernorTmo extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnTmo";
    static final int WFC_STATUS_OFF = 2;
    static final int WFC_STATUS_ON = 1;
    protected boolean mAllPcscfFailed = false;
    protected boolean mHasPendingDeregistration = false;
    protected byte mWfcPrefMode = 0;
    protected byte mWfcStatus = 0;

    RegistrationGovernorTmo(Context ctx) {
        this.mContext = ctx;
        this.mNeedToCheckSrvcc = true;
    }

    public RegistrationGovernorTmo(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        this.mNeedToCheckSrvcc = true;
        updateEutranValues();
    }

    public void onWfcProfileChanged(byte[] data) {
        this.mWfcPrefMode = data[4];
        this.mWfcStatus = data[5];
        Log.i(LOG_TAG, "[WFC] PrefMode = " + this.mWfcPrefMode + ", Status = " + this.mWfcStatus);
        if (this.mWfcStatus == 2) {
            Log.i(LOG_TAG, "WFC switch has turned off. Release throttle.");
            releaseThrottle(3);
        }
    }

    public void onVolteSettingChanged() {
        updateEutranValues();
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        if (retryAfter > 0) {
            this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
            startRetryTimer(((long) retryAfter) * 1000);
        } else {
            this.mRegiAt = 0;
        }
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegHandler.sendTryRegister(this.mPhoneId);
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        if (this.mTask.getPdnType() == 11) {
            boolean notify = false;
            if (ImsUtil.isPermanentPdnFailureReason(reason)) {
                Log.d(LOG_TAG, "Permanent Failure");
                notify = true;
            } else if (this.mTask.getRegistrationRat() == 13 && !this.mRegMan.getCsfbSupported(this.mTask.getPhoneId())) {
                this.mPdnRejectCounter++;
                if (this.mPdnRejectCounter >= 2) {
                    notify = true;
                }
            } else if (this.mTask.getRegistrationRat() == 20 && "PDN_MAX_TIMEOUT".equalsIgnoreCase(reason)) {
                notify = true;
            }
            if (notify) {
                Log.d(LOG_TAG, "notifyImsNotAvailable");
                this.mRegMan.notifyImsNotAvailable(this.mTask, true);
                this.mPdnRejectCounter = 0;
            }
        }
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter);
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        if (SipErrorBase.MISSING_P_ASSOCIATED_URI.equals(error)) {
            this.mTask.mKeepPdn = true;
        } else if (SipErrorBase.EMPTY_PCSCF.equals(error)) {
            this.mFailureCounter++;
            handlePcscfError();
            return;
        } else {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx++;
            if (retryAfter == 0) {
                retryAfter = getWaitTime();
            }
        }
        if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
            this.mCurPcscfIpIdx = 0;
            this.mAllPcscfFailed = true;
        }
        if (retryAfter > 0) {
            this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
            startRetryTimer(((long) retryAfter) * 1000);
            return;
        }
        this.mRegHandler.sendTryRegister(this.mPhoneId);
    }

    public void onPublishError(SipError error) {
        Log.e(LOG_TAG, "onPublishError: state " + this.mTask.getState() + " error " + error);
        if (SipErrorBase.FORBIDDEN.equals(error)) {
            this.mTask.setReason("Publish Error. ReRegister..");
            this.mRegMan.sendReRegister(this.mTask);
        }
    }

    /* access modifiers changed from: protected */
    public int getWaitTime(int failCount) {
        int waitTime = this.mRegBaseTime * ((int) Math.pow(2.0d, (double) (failCount - 1)));
        if (waitTime < 0) {
            return this.mRegMaxTime;
        }
        return Math.min(this.mRegMaxTime, waitTime);
    }

    public void onSubscribeError(int event, SipError error) {
        Log.e(LOG_TAG, "onSubscribeError: state " + this.mTask.getState() + ", error " + error + ", event " + event);
        if (event != 0) {
            return;
        }
        if (SipErrorBase.OK.equals(error)) {
            this.mWFCSubscribeForbiddenCounter = 0;
            this.mSubscribeForbiddenCounter = 0;
            return;
        }
        boolean keeppdn = true;
        if (!SipErrorBase.FORBIDDEN.equals(error) || this.mTask.getRegistrationRat() != 18) {
            this.mWFCSubscribeForbiddenCounter = 0;
            this.mSubscribeForbiddenCounter++;
        } else {
            int i = this.mWFCSubscribeForbiddenCounter + 1;
            this.mWFCSubscribeForbiddenCounter = i;
            if (i > 2) {
                keeppdn = false;
            }
        }
        int retryAfter = getWaitTime(this.mSubscribeForbiddenCounter + this.mWFCSubscribeForbiddenCounter);
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
        startRetryTimer(((long) retryAfter) * 1000);
        this.mTask.setDeregiReason(44);
        this.mRegMan.deregister(this.mTask, true, keeppdn, "Subscribe Error. Deregister..");
    }

    public void onTelephonyCallStatusChanged(int callState) {
        if (callState == 0 && this.mTask.getProfile().hasEmergencySupport()) {
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, true, false, "Call status changed. Deregister..");
        }
    }

    private boolean isDataAllowed() {
        boolean isDataRoamingOn = Settings.Global.getInt(this.mContext.getContentResolver(), "data_roaming", 0) == 1;
        boolean isRoaming = this.mTelephonyManager.isNetworkRoaming();
        if ((isRoaming || !NetworkUtil.isMobileDataOn(this.mContext)) && (!isRoaming || !isDataRoamingOn)) {
            return false;
        }
        return true;
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices;
        Set<String> set = services;
        int i = network;
        Set<String> enabledServices = new HashSet<>();
        if (set == null) {
            filteredServices = new HashSet<>();
        }
        boolean isVolteEnabled = DmConfigHelper.isImsSwitchEnabled(this.mContext, "volte", this.mPhoneId);
        boolean isRcsEnabled = DmConfigHelper.isImsSwitchEnabled(this.mContext, DeviceConfigManager.RCS, this.mPhoneId);
        boolean z = true;
        if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) != 1) {
            z = false;
        }
        boolean isRcsEnabledByUser = z;
        boolean dataAllowed = isDataAllowed();
        boolean isDefaultMsgAppInUsed = DmConfigHelper.isImsSwitchEnabled(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId);
        if (!DmConfigHelper.isImsSwitchEnabled(this.mContext, DeviceConfigManager.IMS, this.mPhoneId)) {
            Log.i(LOG_TAG, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "VOLTE: " + isVolteEnabled + ", RCS: " + isRcsEnabled + ", rcs_user_setting: " + isRcsEnabledByUser + ", Data allowed: " + dataAllowed + ", Default MSG app: " + isDefaultMsgAppInUsed);
        if (i == 13 || i == 20) {
            if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.SUPPORTED) {
                if (this.mTask.getProfile().getPdn().equals("internet")) {
                    Log.i(LOG_TAG, "VoPS Supported. Registration over IMS pdn.");
                    return new HashSet();
                }
            } else if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
                if (!hasRcsSession()) {
                    Log.i(LOG_TAG, "VoPS NOT Supported. Registration over Internet PDN.");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                    return new HashSet();
                }
                Log.i(LOG_TAG, "VoPS NOT Supported. But, there are rcs sessions");
            }
        }
        if (isVolteEnabled) {
            enabledServices.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!enabledServices.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (!dataAllowed && i != 18) {
            removeService(filteredServices, "mmtel-video", "MobileData OFF");
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId) && isRcsEnabled) {
            enabledServices.addAll(servicesByImsSwitch(ImsProfile.getRcsServiceList()));
            Arrays.stream(ImsProfile.getRcsServiceList()).filter(new Predicate(RcsConfigurationHelper.getRcsEnabledServiceList(this.mContext, this.mPhoneId, ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, this.mTask.getProfile()))) {
                public final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return RegistrationGovernorTmo.lambda$filterService$0(this.f$0, (String) obj);
                }
            }).forEach(new Consumer(filteredServices) {
                public final /* synthetic */ Set f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    RegistrationGovernorTmo.this.lambda$filterService$1$RegistrationGovernorTmo(this.f$1, (String) obj);
                }
            });
            if (RcsConfigurationHelper.getConfigData(this.mContext, "root/application/1/services/IR94VideoAuth", this.mPhoneId).readInt(ConfigConstants.ConfigTable.SERVICES_IR94_VIDEO_AUTH, -1).intValue() == 0) {
                removeService(filteredServices, "mmtel-video", "ir94videoauth disabled");
            }
            if (!isRcsEnabledByUser || !isDefaultMsgAppInUsed) {
                if (this.mTask.isRcsOnly()) {
                    String[] rcsServiceList = ImsProfile.getRcsServiceList();
                    int length = rcsServiceList.length;
                    int i2 = 0;
                    while (i2 < length) {
                        removeService(filteredServices, rcsServiceList[i2], "RCS service off");
                        i2++;
                        Set<String> set2 = services;
                    }
                } else {
                    for (String service : ImsProfile.getChatServiceList()) {
                        removeService(filteredServices, service, "chatservice off");
                    }
                }
            }
        }
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
            for (String service2 : ImsProfile.getRcsServiceList()) {
                removeService(filteredServices, service2, "No DualRcs");
            }
        }
        if (!isVolteEnabled || !isRcsEnabled) {
            enabledServices.remove("mmtel-call-composer");
        } else {
            int val = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, this.mPhoneId), DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, -1, this.mPhoneId)).intValue();
            if (!(val == 2 || val == 3)) {
                removeService(filteredServices, "mmtel-call-composer", "MMTEL Composer off from ACS");
            }
        }
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices);
        }
        return filteredServices;
    }

    static /* synthetic */ boolean lambda$filterService$0(List enabledRcsSvcsByAcs, String rcsSvc) {
        return !enabledRcsSvcsByAcs.contains(rcsSvc);
    }

    public /* synthetic */ void lambda$filterService$1$RegistrationGovernorTmo(Set filteredServices, String disabledSvc) {
        removeService(filteredServices, disabledSvc, "Disable from ACS.");
    }

    public SipError onSipError(String service, SipError error) {
        Log.e(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if ("mmtel".equals(service)) {
            if (SipErrorBase.SIP_TIMEOUT.equals(error) || SipErrorBase.PROXY_AUTHENTICATION_REQUIRED.equals(error)) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, true, this.mIsValid, "SIP ERROR[MMTEL] : INVITE_TIMEOUT, Deregister..");
            } else if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) || TextUtils.equals(error.getReason(), "TCP Connection Error")) {
                if (this.mHasVoLteCall) {
                    IMSLog.i(LOG_TAG, this.mTask.getPhoneId(), "onSipError: postpone deregi till call end");
                    this.mHasPendingDeregistration = true;
                } else {
                    removeCurrentPcscfAndInitialRegister(true);
                }
            }
        } else if (("im".equals(service) || "ft".equals(service)) && SipErrorBase.FORBIDDEN.equals(error)) {
            this.mTask.setReason("SIP ERROR[IM] : FORBIDDEN, Reregister..");
            this.mRegMan.sendReRegister(this.mTask);
        }
        return error;
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent event, SipError error, int callType) {
        Log.i(LOG_TAG, "onCallStatus: event=" + event + " error=" + error);
        if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            if (this.mHasPendingDeregistration) {
                removeCurrentPcscfAndInitialRegister(true);
                this.mHasPendingDeregistration = false;
            }
        } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_INITIAL_REGI && SipErrorBase.SERVER_TIMEOUT.equals(error)) {
            removeCurrentPcscfAndInitialRegister(true);
            return;
        }
        super.onCallStatus(event, error, callType);
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return isSrvccCase();
        }
        if (this.mTask.getProfile().getPdn().equals("internet")) {
            return false;
        }
        return true;
    }

    private boolean checkVowifiSetting(int rat) {
        if (rat != 18 || this.mWfcStatus != 2) {
            return true;
        }
        Log.i(LOG_TAG, "Rat is IWLAN but WFC switch is OFF.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int rat) {
        if (!hasRcsSession()) {
            return true;
        }
        boolean isVopsUpdated = false;
        NetworkEvent event = this.mRegMan.getNetworkEvent(this.mPhoneId);
        if (event != null) {
            isVopsUpdated = event.isVopsUpdated;
        }
        if ((rat != 13 || isVopsUpdated) && rat != 18) {
            return true;
        }
        Log.i(LOG_TAG, "RCS session is active");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_RCS_SESSION.getCode());
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkCallStatus() && checkVowifiSetting(rat) && checkRcsEvent(rat));
    }

    private boolean hasRcsSession() {
        IImModule imModule = ImsRegistry.getServiceModuleManager().getImModule();
        return imModule != null && imModule.hasEstablishedSession();
    }

    public boolean isThrottled() {
        return this.mRegiAt > SystemClock.elapsedRealtime() || (this.mWFCSubscribeForbiddenCounter > 2 && this.mTask.getRegistrationRat() == 18);
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1) {
            this.mAllPcscfFailed = false;
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            stopRetryTimer();
            this.mSubscribeForbiddenCounter = 0;
        } else if (releaseCase == 3 || releaseCase == 2) {
            this.mWFCSubscribeForbiddenCounter = 0;
        }
    }

    public void onContactActivated() {
        Log.i(LOG_TAG, "ContactActivated. Reset SRMR2 failure counter");
        this.mSubscribeForbiddenCounter = 0;
        this.mWFCSubscribeForbiddenCounter = 0;
    }

    public int getFailureType() {
        if (!this.mAllPcscfFailed) {
            return 16;
        }
        if (this.mTask.getRegistrationRat() != 20 && this.mRegMan.getCsfbSupported(this.mTask.getPhoneId())) {
            return 16;
        }
        this.mAllPcscfFailed = false;
        return 32;
    }

    public void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            int voiceTech = getVoiceTechType();
            Log.i(LOG_TAG, "updateEutranValues : voiceTech : " + voiceTech);
            ContentValues eutranValue = new ContentValues();
            if (voiceTech == 0) {
                eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
            } else {
                eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 2);
            }
            this.mContext.getContentResolver().update(GlobalSettingsConstants.CONTENT_URI, eutranValue, (String) null, (String[]) null);
        }
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToDeRegistration:");
        if (foundBestRat != 0) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "isNeedToDeRegistration: no IMS service for network " + currentRat + ". Deregister.");
        RegisterTask registerTask = this.mTask;
        registerTask.setReason("no IMS service for network : " + currentRat);
        this.mTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(this.mTask, this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && isSrvccCase(), false);
        return true;
    }
}
