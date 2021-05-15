package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;

public class RegistrationGovernorSoftBank extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGovernorSoftBank";

    public RegistrationGovernorSoftBank(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) 1) * 1000);
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        startRetryTimer(((long) 1) * 1000);
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(int retryAfter) {
        Log.e(LOG_TAG, "onRegistrationError: Timer F fired.");
        this.mTask.mKeepPdn = true;
        this.mRegHandler.sendTryRegister(this.mPhoneId);
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            Log.e(LOG_TAG, "onRegistrationError: Silently Purge the IMS Registration and dont send REGISTER");
            this.mFailureCounter = 0;
            this.mIsPermanentStopped = true;
        } else if (SipErrorBase.FORBIDDEN.getCode() == error.getCode()) {
            handleForbiddenError(retryAfter);
        } else if (SipErrorBase.REQUEST_TIMEOUT.equals(error) || SipErrorBase.NOT_FOUND.getCode() == error.getCode() || SipErrorBase.SERVER_INTERNAL_ERROR.equals(error) || SipErrorBase.SERVICE_UNAVAILABLE.equals(error) || SipErrorBase.BUSY_EVERYWHERE.equals(error)) {
            if (retryAfter == 0) {
                this.mCurPcscfIpIdx--;
                retryAfter = getWaitTime();
            } else if (unsolicit) {
                this.mCurPcscfIpIdx--;
            }
            this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
            startRetryTimer(((long) retryAfter) * 1000);
        } else if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
            handleTimeoutError(retryAfter);
        }
    }

    public void onSubscribeError(int event, SipError error) {
        Log.i(LOG_TAG, "onSubscribeError: state " + this.mTask.getState() + " error " + error + ", event = " + event);
        if (event == 0) {
            if (SipErrorBase.SIP_TIMEOUT.equals(error) || SipErrorBase.REQUEST_TIMEOUT.equals(error)) {
                Log.i(LOG_TAG, " complain to governor");
                this.mTask.getGovernor().onRegistrationError(error, 0, false);
            }
            if (!SipErrorBase.REQUEST_TIMEOUT.equals(error) && SipErrorBase.NOT_FOUND.getCode() != error.getCode() && !SipErrorBase.SERVER_INTERNAL_ERROR.equals(error) && !SipErrorBase.SERVICE_UNAVAILABLE.equals(error)) {
                SipErrorBase.BUSY_EVERYWHERE.equals(error);
            }
        }
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
}
