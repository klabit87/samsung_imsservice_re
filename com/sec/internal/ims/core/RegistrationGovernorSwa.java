package com.sec.internal.ims.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorSwa;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorSwa extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnSwa";

    public RegistrationGovernorSwa(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        Log.i("RegiGvnSwa[" + this.mTask.getPhoneId() + "]", "Register : ShutdownEventReceiver");
        this.mHandlePcscfOnAlternativeCall = true;
        this.mNeedToCheckSrvcc = true;
        this.mNeedToCheckLocationSetting = false;
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int rat) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        int phoneId = this.mTask.getPhoneId();
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(phoneId) == 0 || this.mTask.isEpdgHandoverInProgress()) {
            return true;
        }
        if (this.mMno == Mno.AIRTEL || this.mMno == Mno.BSNL || !isSrvccCase()) {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: call state is not idle");
            return false;
        }
        IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: SRVCC case");
        return true;
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            Log.i("RegiGvnSwa[" + this.mTask.getPhoneId() + "]", "releaseThrottle: case by " + releaseCase);
        }
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        int phoneId = this.mTask.getPhoneId();
        boolean isVoLteEnabled = false;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, phoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", phoneId) == 1) {
            isVoLteEnabled = true;
        }
        if (!isImsEnabled) {
            IMSLog.i(LOG_TAG, phoneId, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (isVoLteEnabled) {
            Set<String> serviceList = servicesByImsSwitch(ImsProfile.getVoLteServiceList());
            if (!serviceList.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
            enabledServices.addAll(servicesByReadSwitch((String[]) serviceList.toArray(new String[serviceList.size()])));
            if (serviceList.contains("mmtel") && !enabledServices.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_DM_OFF.getCode());
            }
        }
        if (this.mConfigModule.isValidAcsVersion(phoneId)) {
            Set<String> serviceList2 = servicesByImsSwitch(ImsProfile.getRcsServiceList());
            enabledServices.addAll(servicesByReadSwitch((String[]) serviceList2.toArray(new String[serviceList2.size()])));
        }
        if (network == 13 && this.mTask.getProfile().getPdnType() == 11) {
            enabledServices = applyVoPsPolicy(enabledServices);
            if (enabledServices.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return enabledServices;
            }
        }
        if (this.mTask.getProfile().getPdnType() == 11) {
            enabledServices = applyMmtelUserSettings(enabledServices, network);
        }
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices);
        }
        return filteredServices;
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        String matchfrompdnfail = getMatchedPdnFailReason(getPdnFailureReasons(), reason);
        boolean isNeedToStop = false;
        long retryTime = -1;
        if (!TextUtils.isEmpty(matchfrompdnfail)) {
            isNeedToStop = true;
            if (matchfrompdnfail.indexOf(":") != -1) {
                retryTime = Long.parseLong(matchfrompdnfail.substring(matchfrompdnfail.indexOf(":") + 1, matchfrompdnfail.length()));
            }
            setRetryTimeOnPdnFail(retryTime);
        }
        if (isNeedToStop) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mIsPermanentStopped = true;
        }
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        Log.e(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorSwa.AKA_CHANLENGE_TIMEOUT.equals(error)) {
            Log.e(LOG_TAG, "onRegistrationError: Permanently prohibited.");
            this.mTask.setDeregiReason(71);
            this.mIsPermanentStopped = true;
            resetPcscfList();
            this.mRegMan.deregister(this.mTask, true, false, "Aka challenge timeout");
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            SimpleEventLog eventLog = this.mRegMan.getEventLog();
            eventLog.logAndAdd("onRegistrationError : " + error + ", fail count : " + this.mFailureCounter);
            return;
        }
        super.onRegistrationError(error, retryAfter, unsolicit);
    }

    public boolean isLocationInfoLoaded(int rat) {
        Log.d(LOG_TAG, "isLocationInfoLoaded  rat : " + rat);
        if (this.mMno == Mno.AIRTEL) {
            return true;
        }
        return super.isLocationInfoLoaded(rat);
    }
}
