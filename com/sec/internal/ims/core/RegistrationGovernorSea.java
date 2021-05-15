package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorSea extends RegistrationGovernorBase {
    private static final int DELAYED_DEREGISTER_TIMER = 10;
    private static final String LOG_TAG = "RegiGvnSea";
    protected String mPdnFailedReason = "";

    public RegistrationGovernorSea(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        this.mNeedToCheckSrvcc = true;
        this.mHandlePcscfOnAlternativeCall = true;
        this.mNeedToCheckLocationSetting = false;
    }

    public boolean isThrottled() {
        if (this.mIsPermanentStopped || this.mRegiAt > SystemClock.elapsedRealtime()) {
            return true;
        }
        if (!this.mIsPermanentPdnFailed || this.mTask.getProfile().getPdnType() != 11) {
            return false;
        }
        if (this.mMno == Mno.SMART_PH && this.mPdnFailedReason.contains("INSUFFICIENT_RESOURCES") && this.mRegMan.getCurrentNetworkByPhoneId(this.mTask.getPhoneId()) == 18) {
            return false;
        }
        return true;
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1) {
            if (isDelayedDeregisterTimerRunning()) {
                Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "releaseThrottle: delete DelayedDeregisterTimer on fligt mode");
                setDelayedDeregisterTimerRunning(false);
            }
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mPdnFailedReason = "";
            this.mCurImpu = 0;
            this.mNonVoLTESimByPdnFail = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "releaseThrottle: case by " + releaseCase);
        }
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        this.mPdnFailedReason = reason;
        boolean isNeedToStop = false;
        String matchfrompdnfail = getMatchedPdnFailReason(getPdnFailureReasons(), reason);
        long retryTime = -1;
        if (!TextUtils.isEmpty(matchfrompdnfail)) {
            isNeedToStop = true;
            if (matchfrompdnfail.indexOf(":") != -1) {
                retryTime = Long.parseLong(matchfrompdnfail.substring(matchfrompdnfail.indexOf(":") + 1, matchfrompdnfail.length()));
            }
            setRetryTimeOnPdnFail(retryTime);
        }
        if (isNeedToStop) {
            this.mIsPermanentPdnFailed = true;
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            if (this.mMno != Mno.SMART_PH || reason == null || !reason.contains("INSUFFICIENT_RESOURCES")) {
                this.mNonVoLTESimByPdnFail = true;
                return;
            }
            Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "SMART_PH not enable Volte on all sites");
        }
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        int phoneId = this.mTask.getPhoneId();
        boolean isDataRoamingOn = false;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mTask.getPhoneId()) == 1;
        boolean isVolteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mTask.getPhoneId()) == 1;
        if (!isImsEnabled) {
            IMSLog.i(LOG_TAG, phoneId, "filterEnabledCoreService: IMS is disabled.");
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
        if (this.mConfigModule.isValidAcsVersion(this.mTask.getPhoneId())) {
            Set<String> serviceList = servicesByImsSwitch(ImsProfile.getRcsServiceList());
            enabledServices2.addAll(servicesByReadSwitch((String[]) serviceList.toArray(new String[serviceList.size()])));
        }
        if (this.mMno != Mno.TRUEMOVE && this.mMno != Mno.AIS && network == 13 && this.mTask.getProfile().getPdnType() == 11) {
            enabledServices2 = applyVoPsPolicy(enabledServices2);
            if (enabledServices2.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return enabledServices2;
            }
        }
        if (this.mMno == Mno.SINGTEL && network == 13 && this.mTask.getProfile().getPdnType() == 11) {
            if (ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) == ImsConstants.SystemSettings.ROAMING_DATA_ENABLED) {
                isDataRoamingOn = true;
            }
            if (!isDataRoamingOn && this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).isDataRoaming) {
                IMSLog.i(LOG_TAG, phoneId, "Data roaming OFF remove VoLTE service");
                String reason = "Data roaming : OFF," + this.mTask.getRegistrationRat();
                removeService(enabledServices2, "mmtel-video", reason);
                removeService(enabledServices2, "mmtel", reason);
                removeService(enabledServices2, "smsip", reason);
            }
        }
        if (this.mTask.getProfile().getPdnType() == 11) {
            enabledServices2 = applyMmtelUserSettings(enabledServices2, network);
        }
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices2);
        }
        return filteredServices;
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int rat) {
        int phoneId = this.mTask.getPhoneId();
        if ((!this.mRegMan.getNetworkEvent(phoneId).isDataRoaming && !this.mRegMan.getNetworkEvent(phoneId).isVoiceRoaming) || allowRoaming()) {
            return true;
        }
        if (rat == 18 && this.mTask.getProfile().getPdnType() == 11) {
            IMSLog.i(LOG_TAG, phoneId, "VoWIFI skip for roaming check");
            return true;
        }
        IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: roaming is not allowed.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        int phoneId = this.mTask.getPhoneId();
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(phoneId) == 0 || this.mTask.isEpdgHandoverInProgress()) {
            return true;
        }
        if (!isSrvccCase()) {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: call state is not idle");
            return false;
        } else if (this.mTask.getProfile().getBlockDeregiOnSrvcc()) {
            Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "isReadyToRegister: Skip deregister SRVCC");
            return false;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: SRVCC case");
            return true;
        }
    }

    private boolean checkDeregisterTimer() {
        if (!this.mTask.getProfile().getBlockDeregiOnSrvcc() || !isDelayedDeregisterTimerRunning()) {
            return true;
        }
        Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "isReadyToRegister: DelayedDeregisterTimer Running.");
        if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(this.mTask.getPhoneId()).outOfService) {
            return false;
        }
        Log.i("RegiGvnSea[" + this.mTask.getPhoneId() + "]", "isReadyToRegister: LTE attached. Delete DelayedDeregisterTimer.");
        onDelayedDeregister();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int rat) {
        int phoneId = this.mTask.getPhoneId();
        if (this.mMno == Mno.DIGI && rat != 13 && rat != 18 && this.mTask.getProfile().getPdnType() == 11) {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: Not LTE area");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DATA_RAT_IS_NOT_LTE.getCode());
            this.mTask.setRegistrationRat(rat);
            if (this.mTask.getImsRegistration() != null) {
                this.mTask.getImsRegistration().setCurrentRat(rat);
            }
            return false;
        } else if (!this.mRegHandler.hasNetworModeChangeEvent() || !this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "isReadyToRegister: networkModeChangeTimer Running.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_NW_MODE_CHANGE.getCode());
            return false;
        }
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkCallStatus() && checkRegiStatus() && checkRoamingStatus(rat) && checkNetworkEvent(rat) && checkWFCsettings(rat) && checkDeregisterTimer()) || checkMdmnProfile();
    }

    public boolean isLocationInfoLoaded(int rat) {
        if (this.mMno != Mno.GLOBE_PH) {
            return true;
        }
        return super.isLocationInfoLoaded(rat);
    }

    public void onTelephonyCallStatusChanged(int callState) {
        setCallStatus(callState);
        if (this.mTask.getProfile().getBlockDeregiOnSrvcc() && getCallStatus() == 0) {
            int phoneId = this.mTask.getPhoneId();
            if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                return;
            }
            if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(phoneId).outOfService) {
                IMSLog.i(LOG_TAG, phoneId, "onTelephonyCallStatusChanged: delayedDeregisterTimer 10 seconds start");
                setDelayedDeregisterTimerRunning(true);
                this.mRegMan.sendDeregister((IRegisterTask) this.mTask, 10000);
            }
        }
    }

    public boolean isDelayedDeregisterTimerRunning() {
        return isDelayedDeregisterTimerRunningWithCallStatus();
    }

    public void onDelayedDeregister() {
        super.runDelayedDeregister();
    }
}
