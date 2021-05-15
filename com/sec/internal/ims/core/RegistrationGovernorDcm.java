package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorDcm extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnDcm";

    public RegistrationGovernorDcm(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(int retryAfter) {
        if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            resetPcscfList();
            stopTimsTimer(RegistrationConstants.REASON_TIMS_REFRESHING);
        }
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) 1) * 1000);
        startRetryTimer(((long) 1) * 1000);
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (SipErrorBase.isImsForbiddenError(error)) {
            retryAfter = Id.REQUEST_STOP_RECORD;
            Log.e(LOG_TAG, "onRegistrationError: SIP_403 error triggers DCM timer " + Id.REQUEST_STOP_RECORD);
        } else if (SipErrorBase.SIP_TIMEOUT.equals(error) || SipErrorBase.USE_PROXY.equals(error) || SipErrorBase.SERVER_TIMEOUT.equals(error)) {
            handleTimeoutError(retryAfter);
        } else if (SipErrorBase.SERVICE_UNAVAILABLE.equals(error)) {
            if (retryAfter > 0) {
                Log.i(LOG_TAG, "onRegistrationError: block Ps e911 for " + retryAfter + "s");
                this.mPse911Prohibited = true;
            }
            removeCurrentPcscfAndInitialRegister(true);
        }
        if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
            this.mCurPcscfIpIdx = 0;
        }
        if (retryAfter == 0) {
            retryAfter = getWaitTime();
        }
        if (retryAfter > 0) {
            this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
            startRetryTimer(((long) retryAfter) * 1000);
            return;
        }
        this.mRegHandler.sendTryRegister(this.mPhoneId, 1000);
    }

    public void onDeregistrationDone(boolean requested) {
        if (requested && !this.mTask.mKeepPdn && getVoiceTechType(this.mTask.getPhoneId()) == 1 && this.mTask.getPdnType() == 11) {
            this.mRegHandler.notifyVolteSettingOff(this.mTask, 1000);
        }
    }

    public SipError onSipError(String service, SipError error) {
        Log.i(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if (SipErrorBase.SERVER_TIMEOUT.equals(error)) {
            removeCurrentPcscfAndInitialRegister(true);
        } else if (SipErrorBase.REQUEST_TIMEOUT.equals(error)) {
            if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
            }
        } else if (!"smsip".equals(service)) {
            return super.onSipError(service, error);
        } else {
            Log.i(LOG_TAG, "onSipError SMS caught is : service=" + service + " error=" + error);
            if (error.getCode() == 408 || error.getCode() == 708 || error.getCode() == 504) {
                Log.i(LOG_TAG, "SMS error : mCurPcscfIpIdx=" + this.mCurPcscfIpIdx + " mNumOfPcscfIp=" + this.mNumOfPcscfIp);
                if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp) {
                    Log.i(LOG_TAG, "SMS Error caught state = " + this.mTask.getState());
                    if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                        Log.i(LOG_TAG, "SMSError stop pdn called : service=" + service);
                        this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                        resetPcscfList();
                    }
                } else {
                    Log.i(LOG_TAG, "SMS Error trying on next pcscf is");
                    removeCurrentPcscfAndInitialRegister(true);
                }
            }
        }
        return error;
    }

    public boolean isThrottled() {
        if (this.mIsPermanentStopped || this.mRegiAt > SystemClock.elapsedRealtime()) {
            return true;
        }
        if (isPse911Prohibited()) {
            Log.i(LOG_TAG, "release blocking Ps e911 as throttling is expired for 503 error");
            this.mPse911Prohibited = false;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        if (services == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs != VoPsIndication.NOT_SUPPORTED) {
            return services;
        }
        Log.i(LOG_TAG, "applyVoPsPolicy: not support VoPS, filtering all services.");
        return new HashSet();
    }

    public void onVolteSettingChanged() {
        boolean isVolteOn = getVoiceTechType() == 0;
        Log.i(LOG_TAG, "onVolteSettingChanged: " + isVolteOn);
        if (isVolteOn) {
            if (this.mRegHandler.hasVolteSettingOffEvent()) {
                this.mRegHandler.removeVolteSettingOffEvent();
            }
            notifyVoLteOnOffToRil(isVolteOn);
            return;
        }
        if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
            notifyVoLteOnOffToRil(isVolteOn);
        }
    }

    public void notifyVoLteOnOffToRil(boolean enabled) {
        Log.i(LOG_TAG, "notifyVoLteOnOffToRil: " + enabled);
        sendRawRequestToTelephony(this.mContext, buildVolteOnOffOemHookCmd(enabled));
    }

    private byte[] buildVolteOnOffOemHookCmd(boolean isVolteOn) {
        return new byte[]{9, 5, 0, 6, 7, isVolteOn ? (byte) 1 : 0};
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
            Log.i(LOG_TAG, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (isVoLteEnabled) {
            enabledServices.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!enabledServices.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (network == 13 && this.mTask.getProfile().getPdnType() == 11) {
            enabledServices = applyVoPsPolicy(enabledServices);
            if (enabledServices.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return enabledServices;
            }
        }
        if (!isVideoCallEnabled()) {
            removeService(filteredServices, "mmtel-video", "VideoCall disable.");
        }
        if (filteredServices.size() > 0) {
            filteredServices.retainAll(enabledServices);
        }
        return filteredServices;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        if (rat == 18 || getVoiceTechType(this.mPhoneId) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        if (foundBestRat != 0 || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToDeRegistration: Block deregister for VoLte task during emergency call.");
        return true;
    }
}
