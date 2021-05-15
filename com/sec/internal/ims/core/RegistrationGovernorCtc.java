package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.android.internal.telephony.TelephonyFeatures;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RegistrationGovernorCtc extends RegistrationGovernorBase {
    private static final int DEFAULT_TIMS_TIMER = 730;
    private static final String LOG_TAG = "RegiGvnCtc";
    private boolean mPendingCtcVolteOff = false;
    private boolean mPendingCtcVolteOn = false;
    protected final int[] mRegRetryTime = {0, 30, 60, 120, Id.REQUEST_STOP_RECORD, NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE};

    public RegistrationGovernorCtc(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        updateEutranValues();
        updateCTCVolteState();
        this.mHandlePcscfOnAlternativeCall = true;
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent event, SipError error, int callType) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "onCallStatus: event=" + event + " error=" + error);
        if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            this.mHasVoLteCall = false;
            if (isDeregisterWithVoPSNeeded()) {
                this.mTask.setDeregiReason(72);
                this.mRegMan.deregister(this.mTask, false, false, "SERVICE NOT AVAILABLE");
                return;
            }
            return;
        }
        super.onCallStatus(event, error, callType);
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        boolean isVoLteEnabled = false;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1) {
            isVoLteEnabled = true;
        }
        if (!isImsEnabled) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        Set<String> filteredServices2 = applyMmtelUserSettings(filteredServices, network);
        if (isVoLteEnabled) {
            Set<String> serviceList = servicesByImsSwitch(ImsProfile.getVoLteServiceList());
            enabledServices.addAll(servicesByReadSwitch((String[]) serviceList.toArray(new String[serviceList.size()])));
            if (!enabledServices.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            Set<String> serviceList2 = servicesByImsSwitch(ImsProfile.getRcsServiceList());
            enabledServices.addAll(servicesByReadSwitch((String[]) serviceList2.toArray(new String[serviceList2.size()])));
        }
        if ((network == 13 || network == 20) && this.mTask.getProfile().getPdnType() == 11) {
            enabledServices = applyVoPsPolicy(enabledServices);
            if (enabledServices.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return enabledServices;
            }
        }
        if (!filteredServices2.isEmpty()) {
            filteredServices2.retainAll(enabledServices);
        }
        return filteredServices2;
    }

    public SipError onSipError(String service, SipError error) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "onSipError: service=" + service + " error=" + error);
        if (!"smsip".equals(service)) {
            return super.onSipError(service, error);
        }
        if (error.getCode() == 408 || error.getCode() == 708) {
            int i2 = this.mPhoneId;
            IMSLog.i(LOG_TAG, i2, "SMS error : mCurPcscfIpIdx=" + this.mCurPcscfIpIdx + " mNumOfPcscfIp=" + this.mNumOfPcscfIp);
            removeCurrentPcscfAndInitialRegister(true);
        }
        return error;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        int phoneId = this.mPhoneId;
        if (this.mRegMan.getTelephonyCallStatus(phoneId) != 0 || (this.mVsm != null && this.mVsm.getSessionCount(phoneId) > 0 && !this.mVsm.hasEmergencyCall(phoneId) && this.mVsm.hasActiveCall(phoneId))) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (!SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS()) || this.mRegMan.getTelephonyCallStatus(SimUtil.getOppositeSimSlot(phoneId)) == 0) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: another slot's call state is not idle");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkDelayedStopPdnEvent() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        if (rat == 18 || getVoiceTechType(this.mPhoneId) == 0) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public int getWaitTime() {
        int currentRetryTimer = Math.min(this.mFailureCounter, this.mRegRetryTime.length - 1);
        int random = 0;
        if (currentRetryTimer == 3) {
            random = new Random().nextInt(15);
        }
        return this.mRegRetryTime[currentRetryTimer] + random;
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    public void onDeregistrationDone(boolean requested) {
        if (requested && !this.mTask.mKeepPdn && getVoiceTechType(this.mPhoneId) == 1 && this.mTask.getPdnType() == 11) {
            this.mRegHandler.notifyVolteSettingOff(this.mTask, 1000);
        }
        if (TelephonyFeatures.isChnGlobalModel(this.mPhoneId) && this.mTask.getPdnType() == 11 && this.mPendingCtcVolteOff) {
            IMSLog.d(LOG_TAG, this.mPhoneId, "update volte off state to CP after IMS deregistered.");
            this.mPendingCtcVolteOff = false;
            sendRawRequestToTelephony2(buildVolteStateOemHookCmd(false), this.mPhoneId);
            if (this.mPendingCtcVolteOn) {
                this.mPendingCtcVolteOn = false;
                sendRawRequestToTelephony2(buildVolteStateOemHookCmd(true), this.mPhoneId);
            }
        }
    }

    public void notifyVoLteOnOffToRil(boolean enabled) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "notifyVoLteOnOffToRil: " + enabled);
        ContentValues eutranValue = new ContentValues();
        if (enabled) {
            eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
        } else {
            eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 1);
        }
        Uri.Builder buildUpon = Uri.parse("content://com.sec.ims.settings/global").buildUpon();
        this.mContext.getContentResolver().update(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + this.mPhoneId).build(), eutranValue, (String) null, (String[]) null);
    }

    public void onVolteSettingChanged() {
        updateEutranValues();
        updateCTCVolteState();
    }

    public void startTimsTimer(String reason) {
        startTimsEstablishTimer(this.mTask, (long) 730000, reason);
    }

    public void stopTimsTimer(String reason) {
        stopTimsEstablishTimer(this.mTask, reason);
    }

    private void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            int voiceTech = getVoiceTechType(this.mPhoneId);
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "updateEutranValues : voiceTech : " + voiceTech);
            if (voiceTech == 0) {
                this.mRegHandler.removeVolteSettingOffEvent();
                notifyVoLteOnOffToRil(true);
            } else if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING) {
                notifyVoLteOnOffToRil(false);
            }
        }
    }

    private byte[] buildVolteStateOemHookCmd(boolean isVolteOn) {
        return new byte[]{2, -127, 0, 5, isVolteOn ? (byte) 1 : 0};
    }

    private void updateCTCVolteState() {
        if (TelephonyFeatures.isChnGlobalModel(this.mPhoneId) && this.mTask.getProfile().hasService("mmtel")) {
            int voiceTech = getVoiceTechType(this.mPhoneId);
            int i = this.mPhoneId;
            IMSLog.d(LOG_TAG, i, "updateCTCVolteState : voiceTech : " + voiceTech);
            if (voiceTech == 0) {
                if (this.mPendingCtcVolteOff) {
                    this.mPendingCtcVolteOn = true;
                } else {
                    sendRawRequestToTelephony2(buildVolteStateOemHookCmd(true), this.mPhoneId);
                }
            } else if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                this.mPendingCtcVolteOn = false;
                this.mPendingCtcVolteOff = true;
            } else {
                sendRawRequestToTelephony2(buildVolteStateOemHookCmd(false), this.mPhoneId);
            }
        }
    }
}
