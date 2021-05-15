package com.sec.internal.ims.core;

import android.content.Context;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorMeAfrica extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnMeAfr";

    public RegistrationGovernorMeAfrica(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        this.mHandlePcscfOnAlternativeCall = true;
        this.mNeedToCheckLocationSetting = false;
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (!SipErrorBase.FORBIDDEN.equals(error) || !this.mTask.isRcsOnly() || !this.mTask.getProfile().getNeedAutoconfig()) {
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
                if (this.mMno == Mno.ETISALAT_UAE) {
                    this.mCurPcscfIpIdx--;
                    IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Ignore 403 error for Etisalat, retry on same PCSCF.");
                } else {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Permanently prohibited.");
                    this.mIsPermanentStopped = true;
                    if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                        this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                        return;
                    }
                    return;
                }
            } else if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
                handleTimeoutError(retryAfter);
            }
            handleRetryTimer(retryAfter);
            return;
        }
        this.mConfigModule.startAcs(this.mPhoneId);
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        boolean isVolteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1;
        if (!isImsEnabled) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (isVolteEnabled) {
            enabledServices.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!enabledServices.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        Set<String> enabledServices2 = applyImsSwitch(enabledServices, network);
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            enabledServices2.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch(ImsProfile.getRcsServiceList()).toArray(new String[0])));
        }
        if (network == 13 && this.mTask.getProfile().getPdnType() == 11) {
            enabledServices2 = applyVoPsPolicy(enabledServices2);
            if (enabledServices2.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return enabledServices2;
            }
        }
        if (1 == this.mTask.getProfile().getPdnType() && !VowifiConfig.isEnabled(this.mContext, this.mPhoneId) && network == 18) {
            removeService(filteredServices, "mmtel", "VoWiFi diabled");
            removeService(filteredServices, "smsip", "VoWiFi diabled");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_USER_SETTINGS_OFF.getCode());
        }
        if (filteredServices.contains("gls") && !isGlsEnabled()) {
            removeService(filteredServices, "gls", "GLS disabled");
        }
        if (filteredServices.contains("ec") && !isEcEnabled(this.mPhoneId)) {
            removeService(filteredServices, "ec", "EC disabled");
        }
        if (this.mTask.getProfile().getPdnType() == 11) {
            filteredServices = applyMmtelUserSettings(filteredServices, network);
        }
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices2);
        }
        return filteredServices;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0 || this.mTask.isEpdgHandoverInProgress()) {
            return true;
        }
        if (!isSrvccCase()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (this.mTask.getProfile().getBlockDeregiOnSrvcc()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Skip deregister SRVCC");
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: SRVCC case");
            return true;
        }
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkCallStatus() && checkRegiStatus() && checkRoamingStatus(rat) && checkNetworkEvent(rat));
    }

    private boolean isGlsEnabled() {
        return RcsConfigurationHelper.readBoolParam(this.mContext, ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH).booleanValue();
    }

    public boolean isLocationInfoLoaded(int rat) {
        return this.mMno != Mno.CELLC_SOUTHAFRICA || super.isLocationInfoLoaded(rat);
    }
}
