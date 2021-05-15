package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorKddi extends RegistrationGovernorBase {
    private static final int CP_T3402_EVENT_TIMER = 720;
    private static final int DEFAULT_RETRY_AFTER = 10;
    private static final Long DEFAULT_RETRY_AFTER_BUFFER_MS = 500L;
    private static final int DEFAULT_TIMS_TIMER = 600;
    private static final int KDDI_REG_FAIL_RETRY = 5;
    private static final String LOG_TAG = "RegiGvnKddi";
    protected int mSubscribeFailureCounter = 0;

    public RegistrationGovernorKddi(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    private void handleEmergencyRegistrationError(SipError error) {
        if (this.mTask.getProfile().hasEmergencySupport()) {
            this.mRegiAt = 0;
            Log.i(LOG_TAG, "onRegistrationError: Emergency Registration failed by " + error + ", tried on all PCSCF so trying again on First PCSCF");
            this.mRegHandler.sendTryRegister(this.mPhoneId);
            return;
        }
        Log.e(LOG_TAG, "onRegistrationError: Registration Retries failed so start the T3402 Timer");
        if (this.mPdnController.isEpdgConnected(this.mPhoneId)) {
            Log.e(LOG_TAG, "onRegistrationError: block Registration Retries for the T3402 Timer on Epdg");
            this.mRegiAt = SystemClock.elapsedRealtime() + 720000;
            startRetryTimer(720000);
        }
        this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
        resetPcscfList();
        this.mRegMan.notifyImsNotAvailable(this.mTask, true);
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        this.mFailureCounter = 0;
        this.mSubscribeFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mRegiAt = SystemClock.elapsedRealtime() + ((((long) 10) * 1000) - DEFAULT_RETRY_AFTER_BUFFER_MS.longValue());
        startRetryTimer((((long) 10) * 1000) - DEFAULT_RETRY_AFTER_BUFFER_MS.longValue());
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        Log.i(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        StringBuilder sb = new StringBuilder();
        sb.append("onRegistrationError : ");
        sb.append(error);
        sb.append(", fail count : ");
        sb.append(this.mFailureCounter);
        eventLog.logAndAdd(sb.toString());
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            Log.e(LOG_TAG, "onRegistrationError: Silently Purge the IMS Registration and dont send REGISTER");
            this.mFailureCounter = 0;
            this.mIsPermanentStopped = true;
        } else if (SipErrorBase.NOTIFY_TERMINATED_UNREGISTERED.equals(error)) {
            this.mTask.mKeepPdn = true;
            if (this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
                this.mTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            } else {
                this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            this.mRegMan.deregister(this.mTask, true, true, "Notify terminated unregistered");
        } else if (this.mTask.isRefreshReg()) {
            onRefreshRegError(error, retryAfter);
        } else if (!SipErrorBase.USE_PROXY.equals(error) && this.mFailureCounter < 5) {
            Log.i(LOG_TAG, "onRegistrationError: Registration failed error " + error + " Incremented mFailureCounter " + this.mFailureCounter);
            this.mCurPcscfIpIdx = this.mCurPcscfIpIdx - 1;
            if (retryAfter == 0 && !SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error)) {
                retryAfter = 10;
            }
            this.mTask.mKeepPdn = true;
            this.mRegiAt = SystemClock.elapsedRealtime() + ((((long) retryAfter) * 1000) - DEFAULT_RETRY_AFTER_BUFFER_MS.longValue());
            startRetryTimer((((long) retryAfter) * 1000) - DEFAULT_RETRY_AFTER_BUFFER_MS.longValue());
        } else if (this.mCurPcscfIpIdx < this.mNumOfPcscfIp) {
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mTask.mKeepPdn = true;
            if (this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
                this.mTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            } else {
                this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            Log.i(LOG_TAG, "onRegistrationError: Registration failed error " + error + " mFailureCounter " + this.mFailureCounter + " Incremented mCurPcscfIpIdx " + this.mCurPcscfIpIdx + "mTask.getState()" + this.mTask.getState());
            this.mRegMan.deregister(this.mTask, true, true, error.getReason());
        } else {
            this.mCurPcscfIpIdx = 0;
            this.mFailureCounter = 0;
            this.mTask.mKeepPdn = true;
            handleEmergencyRegistrationError(error);
        }
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        stopTimsTimer(RegistrationConstants.REASON_REGISTERED);
    }

    private void onRefreshRegError(SipError error, int retryAfter) {
        boolean bErrorRespReceived = SipErrorBase.SIP_TIMEOUT.equals(error) || SipErrorBase.REQUEST_TIMEOUT.equals(error) || SipErrorBase.SERVER_INTERNAL_ERROR.equals(error) || SipErrorBase.SERVER_TIMEOUT.equals(error) || SipErrorBase.SERVICE_UNAVAILABLE.equals(error) || SipErrorBase.FORBIDDEN.equals(error) || SipErrorBase.NOT_FOUND.equals(error) || SipErrorBase.USE_PROXY.equals(error);
        if (bErrorRespReceived) {
            this.mFailureCounter--;
        }
        if (!bErrorRespReceived && this.mFailureCounter < 5) {
            this.mCurPcscfIpIdx--;
            if (retryAfter == 0 && !SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error)) {
                retryAfter = 10;
            }
            this.mTask.mKeepPdn = true;
            Log.i(LOG_TAG, "onRegistrationError: Refresh Reg Retry same Refresh ");
            this.mRegHandler.sendUpdateRegistration(this.mTask.getProfile(), this.mPhoneId, (((long) retryAfter) * 1000) - DEFAULT_RETRY_AFTER_BUFFER_MS.longValue());
        } else if (this.mCurPcscfIpIdx < this.mNumOfPcscfIp) {
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mTask.mKeepPdn = true;
            this.mTask.setDeregiReason(41);
            if (this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
                this.mTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            } else {
                this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            Log.i(LOG_TAG, "onRegistrationError: Send Initial REGISTER on NextPCSCF for error of Refresh REG");
            this.mRegMan.deregister(this.mTask, true, true, error.getReason());
        } else {
            this.mCurPcscfIpIdx = 0;
            this.mFailureCounter = 0;
            this.mTask.mKeepPdn = true;
            handleEmergencyRegistrationError(error);
        }
    }

    public void onSubscribeError(int event, SipError error) {
        Log.i(LOG_TAG, "onSubscribeError: state " + this.mTask.getState() + " error " + error + "mSubscribeFailureCounter=" + this.mSubscribeFailureCounter + ", event " + event);
        if (event == 0) {
            int i = this.mSubscribeFailureCounter + 1;
            this.mSubscribeFailureCounter = i;
            if (i >= 5) {
                Log.i(LOG_TAG, "onSubscribeError: Complain to Governor");
                this.mFailureCounter = this.mSubscribeFailureCounter;
                this.mSubscribeFailureCounter = 0;
                this.mTask.getGovernor().onRegistrationError(error, 0, false);
            } else if (error.getCode() == 403 || error.getCode() == 404 || error.getCode() == 408 || error.getCode() == 500 || error.getCode() == 503 || error.getCode() == 504 || error.getCode() == 708) {
                this.mTask.setDeregiReason(44);
                this.mRegMan.deregister(this.mTask, true, true, "Subscribe Error. Initial Register.");
            }
        }
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent event, SipError error, int callType) {
        Log.i(LOG_TAG, "onCallStatus: event=" + event + " error=" + error);
        if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED) {
            this.mHasVoLteCall = true;
        } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            this.mHasVoLteCall = false;
        } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_INITIAL_REGI) {
            removeCurrentPcscfAndInitialRegister(true);
        }
    }

    public SipError onSipError(String service, SipError error) {
        Log.i(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if ("mmtel".equals(service)) {
            if ((SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) && this.mVsm != null && this.mVsm.getSessionCount(this.mPhoneId) < 2) || SipErrorBase.SIP_TIMEOUT.equals(error)) {
                removeCurrentPcscfAndInitialRegister(true);
            }
        } else if ("smsip".equals(service)) {
            if (error.getCode() != 403 && error.getCode() != 404 && error.getCode() != 423 && error.getCode() != 408 && error.getCode() != 500 && error.getCode() != 503 && error.getCode() != 504 && error.getCode() != 708) {
                return super.onSipError(service, error);
            }
            this.mFailureCounter++;
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, true, "SMS Error. Inital Register.");
        }
        return error;
    }

    public void resetRetry() {
        Log.i(LOG_TAG, "resetRetry()");
        this.mFailureCounter = 0;
        this.mSubscribeFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mCurImpu = 0;
        this.mRegiAt = 0;
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        int voiceType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, this.mPhoneId);
        if (voiceType != 0) {
            Log.i(LOG_TAG, "getVoiceTechType : voicecall_type is not 0, correct it");
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, this.mPhoneId);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getVoiceTechType: ");
        sb.append(voiceType == 0 ? "VOLTE" : "CS");
        Log.i(LOG_TAG, sb.toString());
        return voiceType;
    }

    public void startTimsTimer(String reason) {
        if (!SlotBasedConfig.getInstance(this.mPhoneId).isNotifiedImsNotAvailable()) {
            startTimsEstablishTimer(this.mTask, (long) 600000, reason);
        }
    }

    public void stopTimsTimer(String reason) {
        stopTimsEstablishTimer(this.mTask, reason);
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        boolean isVolteEnabled = false;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1) {
            isVolteEnabled = true;
        }
        if (!isImsEnabled) {
            Log.i(LOG_TAG, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        } else if (getVoiceTechType() != 0) {
            Log.i(LOG_TAG, "filterService: volte disabled");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
            return new HashSet();
        } else {
            if (isVolteEnabled) {
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
                removeService(filteredServices, "mmtel-video", "VideoCall disable");
            }
            if (!filteredServices.isEmpty()) {
                filteredServices.retainAll(enabledServices);
            }
            return filteredServices;
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        boolean isRoaming = this.mTelephonyManager.isNetworkRoaming();
        if (services == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            if (!isRoaming) {
                return new HashSet();
            }
            removeService(services, "mmtel-video", "VoPS Off");
            removeService(services, "mmtel", "VoPS Off");
        }
        return services;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        if (rat == 18 || getVoiceTechType() == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: volte disabled");
        this.mRegMan.resetNotifiedImsNotAvailable(this.mPhoneId);
        return true;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkCallStatus() && checkRoamingStatus(rat) && checkVolteSetting(rat));
    }

    public RegisterTask onManualDeregister(boolean isExplicit) {
        if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED) || !this.mTask.getProfile().hasEmergencySupport()) {
            if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.EMERGENCY) && (this.mTask.getState() != RegistrationConstants.RegisterTaskState.DEREGISTERING || this.mTask.getUserAgent() != null)) {
                return super.onManualDeregister(isExplicit);
            }
            this.mRegMan.tryDeregisterInternal(this.mTask, true, true);
            return null;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "onManualDeregister: for Emergency DeRegistration");
        this.mRegMan.tryDeregisterInternal(this.mTask, true, false);
        return null;
    }
}
