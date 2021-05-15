package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorCmcc extends RegistrationGovernorBase {
    private static final int DELAYED_DEREGISTER_TIMER = 15;
    private static final String LOG_TAG = "RegiGvnCmcc";
    protected boolean mAllPcscfOver = false;

    public RegistrationGovernorCmcc(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        setUpsmEventReceiver();
        updateEutranValues();
        this.mHandlePcscfOnAlternativeCall = true;
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1) {
            if (isDelayedDeregisterTimerRunning()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "releaseThrottle: delete DelayedDeregisterTimer on fligt mode");
                setDelayedDeregisterTimerRunning(false);
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "releaseThrottle: RELEASE_AIRPLANEMODE_ON");
            this.mIsPermanentStopped = false;
            this.mAllPcscfOver = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mAllPcscfOver = false;
            this.mCurImpu = 0;
        } else if (releaseCase == 0) {
            this.mIsPermanentStopped = false;
        }
        if (!this.mIsPermanentStopped) {
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "releaseThrottle: case by " + releaseCase);
        }
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) 1) * 1000);
        startRetryTimer(((long) 1) * 1000);
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistrationError : ");
        sb.append(error);
        sb.append(", fail count : ");
        sb.append(this.mFailureCounter);
        eventLog.logAndAdd(sb.toString());
        if (this.mTask.isRcsOnly()) {
            super.onRegistrationError(error, retryAfter, unsolicit);
            return;
        }
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (SipErrorBase.isImsForbiddenError(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            handleForbiddenError(retryAfter);
            return;
        }
        if (SipErrorBase.MISSING_P_ASSOCIATED_URI.equals(error)) {
            this.mCurPcscfIpIdx--;
            this.mTask.mKeepPdn = true;
        } else if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
            if (this.mCurPcscfIpIdx != this.mNumOfPcscfIp) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "onRegistrationError: SIP_TIMEOUT error. Retry regi immediately");
                this.mRegiAt = 0;
                this.mRegHandler.sendTryRegister(this.mPhoneId);
                return;
            } else if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0 && (SimUtil.getPhoneCount() != 2 || this.mTask.getRegistrationRat() == 18 || this.mRegMan.getTelephonyCallStatus(SimUtil.getOppositeSimSlot(this.mPhoneId)) == 0)) {
                this.mAllPcscfOver = true;
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
                this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
        }
        handleRetryTimer(retryAfter);
    }

    public int getFailureType() {
        if (this.mDiscardCurrentNetwork) {
            return 32;
        }
        if (this.mIsPermanentStopped || this.mAllPcscfOver) {
            return 33;
        }
        return 16;
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
        } else if ((network == 13 || network == 20) && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "filterService: IMSVoPS is not supported");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
            return new HashSet();
        } else {
            if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
                filteredServices = applyMmtelUserSettings(filteredServices, network);
            }
            if (isVoLteEnabled) {
                enabledServices.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
                if (!enabledServices.contains("mmtel")) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
                }
            }
            if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
                enabledServices.addAll(servicesByImsSwitch(ImsProfile.getRcsServiceList()));
            }
            filteredServices.retainAll(enabledServices);
            return filteredServices;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS()) && this.mRegMan.getTelephonyCallStatus(SimUtil.getOppositeSimSlot(this.mPhoneId)) != 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: another slot's call state is not idle");
            return false;
        } else if (!isDelayedDeregisterTimerRunning()) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: DelayedDeregisterTimer Running.");
            if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(this.mPhoneId).outOfService) {
                return false;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: LTE attached. Delete DelayedDeregisterTimer.");
            onDelayedDeregister();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int rat) {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && this.mTask.isRcsOnly()) {
            if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            } else if (!(this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || !this.mPdnController.isWifiConnected() || rat == 18)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: The RCS rat is not wifi, when wifi is connected.");
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        if (getVoiceTechType() == 0 || !this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkVolteSetting(rat) && checkCallStatus() && checkRcsEvent(rat));
    }

    public void onDeregistrationDone(boolean requested) {
        if (requested && !this.mTask.mKeepPdn && getVoiceTechType() == 1 && this.mTask.getPdnType() == 11) {
            this.mRegHandler.notifyVolteSettingOff(this.mTask, 1000);
        }
    }

    public void notifyVoLteOnOffToRil(boolean enabled) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "notifyVoLteOnOffToRil: " + enabled);
        ContentValues eutranValue = new ContentValues();
        if (!enabled) {
            eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 1);
        } else {
            eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
        }
        Uri.Builder buildUpon = Uri.parse("content://com.sec.ims.settings/global").buildUpon();
        this.mContext.getContentResolver().update(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + this.mPhoneId).build(), eutranValue, (String) null, (String[]) null);
    }

    public void onVolteSettingChanged() {
        updateEutranValues();
    }

    private void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            int voiceTech = getVoiceTechType();
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

    public void onTelephonyCallStatusChanged(int callState) {
        setCallStatus(callState);
        if (getCallStatus() == 0) {
            boolean isProperState = this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED;
            if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && isProperState && (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(this.mPhoneId).outOfService)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "onTelephonyCallStatusChanged: delayedDeregisterTimer 15 seconds start");
                setDelayedDeregisterTimerRunning(true);
                this.mRegMan.sendDeregister((IRegisterTask) this.mTask, 15000);
            }
        }
        if (getCallStatus() == 2 && this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onTelephonyCallStatusChanged: deregister due to cs call");
            this.mTask.setDeregiReason(7);
            this.mRegMan.deregister(this.mTask, true, true, "call state changed");
        }
    }

    public boolean isDelayedDeregisterTimerRunning() {
        return isDelayedDeregisterTimerRunningWithCallStatus();
    }

    public void onDelayedDeregister() {
        super.runDelayedDeregister();
    }

    public boolean isThrottled() {
        return this.mIsPermanentStopped || this.mAllPcscfOver || this.mRegiAt > SystemClock.elapsedRealtime();
    }

    public void resetAllPcscfChecked() {
        this.mAllPcscfOver = false;
    }
}
