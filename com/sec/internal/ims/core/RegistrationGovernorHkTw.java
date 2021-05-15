package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorHkTw extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnHkTw";

    public RegistrationGovernorHkTw(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        this.mNeedToCheckLocationSetting = false;
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices = new HashSet<>(services);
        Set<String> enabledServices = new HashSet<>();
        boolean isVolteEnabled = false;
        if (!(DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1) {
            isVolteEnabled = true;
        }
        if (isVolteEnabled) {
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
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices);
        }
        return applyMmtelUserSettings(filteredServices, network);
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int rat) {
        int networkClass = TelephonyManagerExt.getNetworkClass(this.mRegMan.getNetworkEvent(this.mPhoneId).network);
        if (rat != 0 || this.mTask.getProfile().getPdnType() != 11 || networkClass == 1 || networkClass == 2) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: W2L NW unknown moment");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkVolteSetting(int rat) {
        if (rat == 18 || getVoiceTechType() == 0) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: volte disabled");
        this.mRegMan.resetNotifiedImsNotAvailable(this.mPhoneId);
        return true;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkNetworkEvent(rat) && checkRoamingStatus(rat) && checkVolteSetting(rat) && checkWFCsettings(rat));
    }

    public boolean isLocationInfoLoaded(int rat) {
        return !this.mRegMan.getNetworkEvent(this.mPhoneId).isEpdgConnected || super.isLocationInfoLoaded(rat);
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 6) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mNonVoLTESimByPdnFail = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "releaseThrottle: case by " + releaseCase);
        }
    }

    public boolean isThrottled() {
        return this.mIsPermanentStopped || (this.mIsPermanentPdnFailed && this.mTask.getProfile().getPdnType() == 11) || this.mRegiAt > SystemClock.elapsedRealtime();
    }

    public void onPdnRequestFailed(String reason) {
        if (SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) && this.mPhoneId != SimUtil.getDefaultPhoneId()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnRequestFailed, SIM slot is not DDS slot");
        } else if (this.mTask.getProfile().getPdnType() != 11) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnRequestFailed, not IMS PDN");
        } else if (this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId) == 13 || this.mTask.getProfile().getPdnType() != 11) {
            super.onPdnRequestFailed(reason);
            if (isMatchedPdnFailReason(getPdnFailureReasons(), reason)) {
                this.mIsPermanentPdnFailed = true;
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                this.mNonVoLTESimByPdnFail = true;
            }
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onPdnRequestFailed ignore in non LTE");
        }
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        if (foundBestRat != 0 || this.mMno != Mno.TWM || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        this.mTask.setDeregiReason(4);
        this.mRegMan.deregister(this.mTask, false, false, 6000, "TWM: delay 6s to deregister");
        return true;
    }

    public boolean onUpdateGeolocation(LocationInfo geolocation) {
        if (this.mMno.isOneOf(Mno.HK3, Mno.SMARTONE, Mno.CMHK, Mno.CSL, Mno.PCCW)) {
            updateGeolocation(geolocation.mCountry);
        }
        return false;
    }
}
