package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorSpr extends RegistrationGovernorBase {
    private static final String ACTION_LOCATION_TIMEOUT = "com.sec.sprint.wfc.LOCATION_TIMEOUT";
    private static final int DELAY_FOR_CDMA_HANDOVER = 3;
    private static final String INTENT_VOWIFI_HARDRESET = "com.sec.sprint.wfc.HRADRESET_SUCCESS";
    private static final String LOG_TAG = "RegiGvnSpr";
    private final int LOCATION_REQUEST_TIMEOUT = 45000;
    protected IGeolocationController mGeolocationCon;
    protected BroadcastReceiver mIntentReceiverSPR = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(RegistrationGovernorSpr.LOG_TAG, "intent = " + intent.getAction());
            if (RegistrationGovernorSpr.INTENT_VOWIFI_HARDRESET.equals(intent.getAction())) {
                RegistrationGovernorSpr.this.mIsPermanentStopped = false;
                RegistrationGovernorSpr.this.resetRetry();
                RegistrationGovernorSpr.this.stopRetryTimer();
                RegistrationGovernorSpr.this.mRegHandler.sendTryRegister(RegistrationGovernorSpr.this.mPhoneId);
            }
        }
    };
    protected Message mLocationTimeoutMessage = null;
    protected int mPrevVolteUIDefault = -1;

    public RegistrationGovernorSpr(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_VOWIFI_HARDRESET);
        this.mContext.registerReceiver(this.mIntentReceiverSPR, filter);
        this.mGeolocationCon = ImsRegistry.getGeolocationController();
        onConfigUpdated();
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        if (this.mTask.getRegistrationRat() == 13 && this.mTask.getPdnType() == 11) {
            Log.i(LOG_TAG, "send ImsNotAvailable");
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
        }
    }

    public void unRegisterIntentReceiver() {
        Log.i(LOG_TAG, "Un-register intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mIntentReceiverSPR);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices = super.filterService(services, network);
        if (!TextUtils.equals(NvConfiguration.get(this.mContext, "sms_over_ip_network_indication", ""), "1")) {
            removeService(filteredServices, "smsip", "DM off.");
        }
        if (network != 18 && !NetworkUtil.isMobileDataOn(this.mContext)) {
            Log.i(LOG_TAG, "filterService: Mobile data OFF!");
            if (this.mTask.getProfile().hasService("im") || this.mTask.getProfile().hasService("ft")) {
                return new HashSet();
            }
        }
        Set<String> filteredServices2 = applyMmtelUserSettings(filteredServices, network);
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, this.mPhoneId)) {
            for (String service : ImsProfile.getRcsServiceList()) {
                removeService(filteredServices2, service, "No DualRcs");
            }
        }
        return filteredServices2;
    }

    /* access modifiers changed from: protected */
    public boolean isVoWiFiPrefered(boolean roaming) {
        if (!roaming) {
            if (VowifiConfig.getPrefMode(this.mContext, 1, this.mPhoneId) == 1) {
                return true;
            }
            return false;
        } else if (VowifiConfig.getRoamPrefMode(this.mContext, 1, this.mPhoneId) == 1) {
            return true;
        } else {
            return false;
        }
    }

    public SipError onSipError(String service, SipError error) {
        Log.e(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if ("mmtel".equals(service)) {
            int code = error.getCode();
            if (code == 486 || code == 487 || code == 408) {
                return error;
            }
            if (code >= 400 && code <= 699) {
                this.mTask.setDeregiReason(43);
                RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
                RegisterTask registerTask = this.mTask;
                registrationManagerInternal.deregister(registerTask, false, true, ImSessionEvent.MESSAGING_EVENT, code + " error");
            } else if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error)) {
                this.mTask.setDeregiReason(43);
                this.mRegMan.deregister(this.mTask, false, true, ImSessionEvent.MESSAGING_EVENT, "invite timeout");
            }
        } else if (("im".equals(service) || "ft".equals(service)) && SipErrorBase.FORBIDDEN.equals(error)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, this.mIsValid, "SIP ERROR[IM] : FORBIDDEN. DeRegister..");
        }
        return error;
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int rat) {
        if (rat == 18 || !this.mRegMan.getNetworkEvent(this.mPhoneId).isDataRoaming || allowRoaming()) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: IMS roaming is not allowed.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mTask.getProfile().getPdnType() == 15 || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        if (!this.mTask.isRcsOnly()) {
            Log.i(LOG_TAG, "isReadyToRegister: TelephonyCallStatus is not idle");
            return false;
        } else if (this.mVsm == null || !this.mVsm.hasCsCall(this.mPhoneId)) {
            return true;
        } else {
            Log.i(LOG_TAG, "isReadyToRegister: TelephonyCallStatus is not idle (CS call)");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int rat) {
        if (this.mTask.isRcsOnly()) {
            if (RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("version", this.mPhoneId), -1).intValue() <= 0 && ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == -1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: User don't try RCS service yet");
                return false;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            }
        }
        return true;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkRoamingStatus(rat) && checkCallStatus() && checkRcsEvent(rat));
    }

    public void onTelephonyCallStatusChanged(int callState) {
        ImsRegistration reg = this.mTask.getImsRegistration();
        Log.i(LOG_TAG, "onTelephonyCallStatusChanged: callState = " + callState);
        if (callState == 0 && this.mTask.getRegistrationRat() != 18 && reg != null && reg.hasService("mmtel")) {
            this.mRegMan.addPendingUpdateRegistration(this.mTask, 0);
        }
        if (this.mVsm != null && this.mVsm.hasCsCall(this.mPhoneId) && reg != null && this.mTask.isRcsOnly() && this.mTask.getRegistrationRat() != 18) {
            if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                Log.i(LOG_TAG, "CS call. Trigger deregister for RCS");
                this.mTask.setDeregiReason(7);
                this.mRegMan.deregister(this.mTask, false, true, 0, "CS call. Trigger deregister for RCS");
            }
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        if (services == null) {
            return new HashSet();
        }
        int callState = this.mTelephonyManager.getCallState();
        Log.i(LOG_TAG, "applyVoPsPolicy: call state = " + callState);
        if (services.contains("mmtel") && callState == 0 && this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            removeService(services, "mmtel", "applyVoPsPolicy");
        }
        return services;
    }

    public boolean isLocationInfoLoaded(int rat) {
        Log.i(LOG_TAG, "isLocationInfoLoaded: rat = " + rat);
        if (this.mTask.getProfile().getSupportedGeolocationPhase() == 0 || rat != 18) {
            return true;
        }
        IGeolocationController iGeolocationController = this.mGeolocationCon;
        if (iGeolocationController != null) {
            if (iGeolocationController.isCountryCodeLoaded(this.mPhoneId)) {
                Log.i(LOG_TAG, "isLocationInfoLoaded: country code loaded");
                stopLocTimeoutTimer();
                return true;
            } else if (this.mGeolocationCon.isLocationServiceEnabled()) {
                Log.i(LOG_TAG, "isLocationInfoLoaded: request location info");
                this.mGeolocationCon.startGeolocationUpdate(this.mPhoneId, false);
                startLocTimeoutTimer();
            } else {
                notifyLocationTimeout();
            }
        }
        return false;
    }

    public void notifyLocationTimeout() {
        Log.i(LOG_TAG, "notifyLocationTimeout:");
        stopLocTimeoutTimer();
        Intent intent = new Intent();
        intent.setAction(ACTION_LOCATION_TIMEOUT);
        this.mContext.sendBroadcast(intent);
    }

    /* access modifiers changed from: protected */
    public void startLocTimeoutTimer() {
        if (this.mLocationTimeoutMessage != null) {
            Log.i(LOG_TAG, "startLocTimeoutTimer: timer already running");
            return;
        }
        Log.i(LOG_TAG, "startLocTimeoutTimer: timer 45000ms");
        this.mLocationTimeoutMessage = this.mRegHandler.startLocationRequestTimer(this.mTask, 45000);
    }

    /* access modifiers changed from: protected */
    public void stopLocTimeoutTimer() {
        Log.i(LOG_TAG, "stopLocTimeoutTimer:");
        if (this.mLocationTimeoutMessage != null) {
            this.mRegHandler.stopTimer(this.mLocationTimeoutMessage);
            this.mLocationTimeoutMessage = null;
        }
    }

    public void requestLocation(int phoneId) {
        Log.i(LOG_TAG, "requestLocation:");
        this.mGeolocationCon.startGeolocationUpdate(phoneId, false);
    }

    public void onConfigUpdated() {
        int phoneId = this.mTask != null ? this.mPhoneId : 0;
        int currVolteUIDefault = DmConfigHelper.readInt(ImsRegistry.getContext(), ConfigConstants.ConfigPath.OMADM_SPR_VOLTE_UI_DEFAULT, -1).intValue();
        Log.i(LOG_TAG, "onConfigUpdated: currentVolteUIDefault = " + currVolteUIDefault + ", prevVolteUIDefault = " + this.mPrevVolteUIDefault);
        if (currVolteUIDefault != -1 && currVolteUIDefault != this.mPrevVolteUIDefault) {
            this.mPrevVolteUIDefault = currVolteUIDefault;
            if (currVolteUIDefault == 2) {
                IMSLog.c(LogClass.SPR_DM_VOLTE_FORCED_ON, phoneId + ",DM UPD:VLT FORCED ON");
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, phoneId);
                ImsConstants.SystemSettings.setVoiceCallTypeUserAction(this.mContext, 2, phoneId);
                return;
            }
            boolean isChangedByUser = ImsConstants.SystemSettings.isUserToggledVoiceCallType(this.mContext, this.mPhoneId);
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "isVoltePrefChangedFromApp: Changed by user [" + isChangedByUser + "]");
            if (!isChangedByUser) {
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, currVolteUIDefault == 0 ? 1 : 0, phoneId);
                ImsConstants.SystemSettings.setVoiceCallTypeUserAction(this.mContext, 0, phoneId);
            } else {
                Log.i(LOG_TAG, "onConfigUpdated: user pref already changed from app");
            }
            StringBuilder sb = new StringBuilder();
            sb.append(phoneId);
            sb.append(",DM UPD:");
            sb.append(isChangedByUser ? "1" : "0");
            sb.append(",");
            sb.append(currVolteUIDefault);
            IMSLog.c(LogClass.SPR_DM_VOLTE_DEFAULT_UPDATE, sb.toString());
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> services, int network) {
        if (services == null) {
            return new HashSet();
        }
        if (network != 18) {
            if (getVoiceTechType() != 0) {
                removeService(services, "mmtel", "VoLTE OFF");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
            }
        } else if (this.mTask.getProfile().getPdnType() == 11) {
            if (!VowifiConfig.isEnabled(this.mContext, this.mPhoneId)) {
                Log.i(LOG_TAG, "filterService: remove [ALL] by Wi-Fi Calling OFF");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
                return new HashSet();
            }
            if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", this.mPhoneId) == 1 && !services.contains("mmtel")) {
                Log.i(LOG_TAG, "filterService: add [mmtel] by Wi-Fi Calling ON");
                services.add("mmtel");
            }
            if (this.mTelephonyManager.getCallState() == 0 && !isVoWiFiPrefered(this.mTelephonyManager.isNetworkRoaming())) {
                NetworkEvent ne = this.mRegMan.getNetworkEvent(this.mPhoneId);
                int voiceRat = ne.voiceNetwork;
                boolean voiceInSvc = true ^ ne.csOutOfService;
                boolean isPsOnlyReg = ne.isPsOnlyReg;
                int i = this.mPhoneId;
                IMSLog.i(LOG_TAG, i, "filterService: voiceRat [" + voiceRat + "] voiceInSvc [" + voiceInSvc + "] PsOnlyReg [" + isPsOnlyReg + "]");
                if (voiceInSvc && voiceRat != 0 && (voiceRat != 13 || !isPsOnlyReg)) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "filterService: remove [mmtel] due to cellular pref. mode");
                    removeService(services, "mmtel", "VoWiFi cellular pref. mode");
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
                }
            }
        }
        return services;
    }
}
