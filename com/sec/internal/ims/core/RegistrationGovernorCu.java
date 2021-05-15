package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;

public class RegistrationGovernorCu extends RegistrationGovernorBase {
    private static final int DELAYED_DEREGISTER_TIMER = 15000;
    private static final String LOG_TAG = "RegiGvnCu";
    protected boolean mAllPcscfOver = false;

    public RegistrationGovernorCu(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
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
        } else if (releaseCase == 0) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mAllPcscfOver = false;
            this.mCurImpu = 0;
        }
        if (!this.mIsPermanentStopped) {
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "releaseThrottle: case by " + releaseCase);
        }
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

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0 && !isSrvccCase()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS()) && this.mRegMan.getTelephonyCallStatus(SimUtil.getOppositeSimSlot(this.mPhoneId)) != 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: another slot's call state is not idle");
            return false;
        } else if (!isDelayedDeregisterTimerRunning()) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: DelayedDeregisterTimer Running.");
            if (isDeregisterWithRATNeeded() || isDeregisterWithVoPSNeeded() || this.mRegMan.getNetworkEvent(this.mPhoneId).outOfService) {
                return false;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: LTE attached. Delete DelayedDeregisterTimer.");
            onDelayedDeregister();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || getVoiceTechType() == 0) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: volte disabled");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkVolteSetting(rat) && checkCallStatus());
    }

    public boolean isSrvccCase() {
        return this.mTask.getRegistrationRat() == 13 && TelephonyManagerExt.getNetworkClass(this.mRegMan.getNetworkEvent(this.mPhoneId).network) == 2;
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
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (ImsCallUtil.isImsForbiddenError(error)) {
            handleForbiddenError(retryAfter);
            return;
        }
        if (SipErrorBase.SIP_TIMEOUT.equals(error) || SipErrorBase.NOT_FOUND.equals(error) || SipErrorBase.BAD_REQUEST.equals(error)) {
            if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0 && (SimUtil.getPhoneCount() != 2 || this.mTask.getRegistrationRat() == 18 || this.mRegMan.getTelephonyCallStatus(SimUtil.getOppositeSimSlot(this.mPhoneId)) == 0)) {
                this.mAllPcscfOver = true;
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
                this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
        } else if (SipErrorBase.EMPTY_PCSCF.equals(error)) {
            handlePcscfError();
            return;
        }
        handleRetryTimer(retryAfter);
    }

    public void onTelephonyCallStatusChanged(int callState) {
        setCallStatus(callState);
        if (getCallStatus() == 0) {
            boolean isProperState = this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED;
            if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && isProperState && (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(this.mPhoneId).outOfService)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "onTelephonyCallStatusChanged: delayedDeregisterTimer 15000 milliseconds start");
                setDelayedDeregisterTimerRunning(true);
                this.mRegMan.sendDeregister((IRegisterTask) this.mTask, 15000);
            }
        }
        if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && getCallStatus() == 2) {
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
