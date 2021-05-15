package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorOce extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnOce";
    private IGeolocationController mGeolocationCon = ImsRegistry.getGeolocationController();

    public RegistrationGovernorOce(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        this.mNeedToCheckSrvcc = true;
        this.mHandlePcscfOnAlternativeCall = true;
        this.mNeedToCheckLocationSetting = false;
    }

    public boolean isThrottled() {
        return this.mIsPermanentStopped || (this.mIsPermanentPdnFailed && this.mTask.getProfile().getPdnType() == 11) || this.mRegiAt > SystemClock.elapsedRealtime();
    }

    public void releaseThrottle(int releaseCase) {
        IUtServiceModule usm;
        if (releaseCase == 1) {
            if (this.mTask.isRcsOnly()) {
                this.mTask.setDeregiReason(23);
                this.mRegMan.deregister(this.mTask, false, false, "flight mode enabled");
            }
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        } else if (releaseCase == 6) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            Log.i("RegiGvnOce[" + this.mPhoneId + "]", "releaseThrottle: case by " + releaseCase);
            if (this.mMno == Mno.TELSTRA && (usm = ImsRegistry.getServiceModuleManager().getUtServiceModule()) != null) {
                usm.enableUt(this.mPhoneId, true);
            }
        }
    }

    public void onPdnRequestFailed(String reason) {
        IUtServiceModule usm;
        super.onPdnRequestFailed(reason);
        if (isMatchedPdnFailReason(getPdnFailureReasons(), reason)) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mIsPermanentPdnFailed = true;
            this.mNonVoLTESimByPdnFail = true;
            if (this.mMno == Mno.TELSTRA && (usm = ImsRegistry.getServiceModuleManager().getUtServiceModule()) != null) {
                usm.enableUt(this.mPhoneId, false);
            }
        }
    }

    private boolean checkSimState() {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (sm == null || sm.isSimLoaded()) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Sim hasn't loaded yet");
        return false;
    }

    private boolean checkAvailableRat(int rat) {
        if (rat == 13 || rat == 18 || rat == 20 || this.mTask.getProfile().getPdnType() != 11 || this.mMno != Mno.VODAFONE_NEWZEALAND) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Not LTE area");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DATA_RAT_IS_NOT_LTE.getCode());
        this.mTask.setRegistrationRat(rat);
        if (this.mTask.getImsRegistration() == null) {
            return false;
        }
        this.mTask.getImsRegistration().setCurrentRat(rat);
        return false;
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
        return checkEmergencyStatus() || (checkRegiStatus() && checkSimState() && checkRoamingStatus(rat) && checkAvailableRat(rat) && checkCallStatus() && checkWFCsettings(rat) && checkNetworkEvent(rat));
    }

    public boolean isLocationInfoLoaded(int rat) {
        return this.mMno != Mno.VODAFONE_AUSTRALIA || super.isLocationInfoLoaded(rat);
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        boolean isVoLteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1;
        if (!isImsEnabled) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (isVoLteEnabled) {
            enabledServices.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
            if (!enabledServices.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        Set<String> enabledServices2 = applyImsSwitch(enabledServices, network);
        if (!NetworkUtil.isMobileDataOn(this.mContext) && network != 18) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Mobile off!");
            for (String service : ImsProfile.getRcsServiceList()) {
                if (DmConfigHelper.getImsSwitchValue(this.mContext, service, this.mPhoneId) == 1 && DmConfigHelper.readBool(this.mContext, service, true, this.mPhoneId).booleanValue()) {
                    removeService(filteredServices, service, "MobileOff");
                }
            }
        }
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
        if ((this.mMno == Mno.TELSTRA || this.mMno == Mno.VODAFONE_AUSTRALIA) && !isVideoCallEnabled()) {
            removeService(filteredServices, "mmtel-video", "TELSTRA/VODAFONE VideoCall disabled");
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
    public void handleForbiddenError(int retryAfter) {
        IUtServiceModule usm;
        IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Permanently prohibited.");
        this.mIsPermanentStopped = true;
        if (this.mMno == Mno.TELSTRA && this.mTask.getProfile().getPdnType() == 11 && (usm = ImsRegistry.getServiceModuleManager().getUtServiceModule()) != null) {
            usm.enableUt(this.mPhoneId, false);
        }
        if (this.mMno == Mno.VODAFONE_AUSTRALIA && this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId) == 18 && !"AU".equalsIgnoreCase(countryInfoLoaded())) {
            this.mGeolocationCon.startGeolocationUpdate(this.mPhoneId, false, ATTGlobalVariables.INTERVAL_ZCODE_API);
        }
        if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            resetPcscfList();
        }
    }

    private String countryInfoLoaded() {
        IGeolocationController iGeolocationController = this.mGeolocationCon;
        if (iGeolocationController == null || !iGeolocationController.isCountryCodeLoaded(this.mPhoneId)) {
            return "";
        }
        return this.mGeolocationCon.getGeolocation().mCountry;
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        if (foundBestRat != 0 || this.mMno != Mno.VODAFONE_AUSTRALIA || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        this.mTask.setDeregiReason(4);
        this.mRegMan.deregister(this.mTask, false, false, 6000, "Vodafone AU: delay 6s to deregister");
        return true;
    }

    public boolean onUpdateGeolocation(LocationInfo geolocation) {
        return this.mMno == Mno.VODAFONE_AUSTRALIA && updateGeolocation(geolocation.mCountry);
    }
}
