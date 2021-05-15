package com.sec.internal.ims.core;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorCan extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnCan";

    public RegistrationGovernorCan(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
    }

    protected RegistrationGovernorCan(Context ctx) {
        this.mContext = ctx;
    }

    public boolean isReadyToRegister(int rat) {
        ImsRegistration reg = this.mRegMan.getRegistrationList().get(Integer.valueOf(this.mTask.getProfile().getId()));
        if (reg != null) {
            Set<String> oldSvc = reg.getServices();
            Set<String> newSvc = this.mRegMan.getServiceForNetwork(this.mTask.getProfile(), rat, false, this.mPhoneId);
            IMSLog.s(LOG_TAG, "getServiceForNetwork: services registered=" + oldSvc + " new=" + newSvc);
            if ((this.mMno == Mno.BELL || this.mMno == Mno.VTR) && newSvc != null && !newSvc.isEmpty() && !newSvc.equals(oldSvc) && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
                Log.i(LOG_TAG, "Call going on so registration blocked as per requirement for Bell");
                return false;
            }
        }
        if (this.mMno == Mno.SASKTEL && rat == 13 && this.mPdnController.getVopsIndication(this.mPhoneId) != VoPsIndication.SUPPORTED && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
            Log.i(LOG_TAG, "isReadyToRegister: Sasktel if VOPS is not supported in LTE rat when call is ongoing");
            return false;
        } else if (this.mVsm != null && ((this.mMno == Mno.TELUS || this.mMno == Mno.KOODO) && this.mTask.getProfile().hasEmergencySupport() && this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && this.mVsm.hasEmergencyCall(this.mPhoneId))) {
            return false;
        } else {
            if (this.mTask.getProfile().hasEmergencySupport() && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING) {
                return true;
            }
            if (this.mMno != Mno.BELL && this.mMno != Mno.VTR && this.mMno != Mno.SASKTEL && this.mMno != Mno.WIND && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
                return false;
            }
            if (rat != 18 || (isWiFiSettingOn() && VowifiConfig.isEnabled(this.mContext, this.mPhoneId))) {
                if (this.mMno == Mno.ROGERS && this.mTelephonyManager.isNetworkRoaming()) {
                    String gid = this.mTelephonyManager.getGroupIdLevel1();
                    if (rat != 18 && "BA".equalsIgnoreCase(gid)) {
                        Log.i(LOG_TAG, "isReadyToRegister: No Volte roaming support for TBT");
                        return false;
                    }
                }
                if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
                    return false;
                }
                if (!this.mTask.mIsUpdateRegistering) {
                    return true;
                }
                Log.i(LOG_TAG, "isReadyToRegister: Task State is UpdateRegistering");
                return false;
            }
            Log.i(LOG_TAG, "isReadyToRegister: Wifi Calling or Wifi turned off, RAT is not updated at framework side");
            return false;
        }
    }

    public SipError onSipError(String service, SipError error) {
        Log.e(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if ("mmtel".equals(service)) {
            if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) || SipErrorBase.SIP_TIMEOUT.equals(error) || ((this.mMno == Mno.TELUS || this.mMno == Mno.KOODO) && SipErrorBase.SERVER_TIMEOUT.equals(error))) {
                removeCurrentPcscfAndInitialRegister(true);
            }
        } else if (("im".equals(service) || "ft".equals(service)) && SipErrorBase.FORBIDDEN.equals(error)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, true, this.mIsValid, "SIP ERROR[IM] : FORBIDDEN. DeRegister..");
        }
        return error;
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices = super.filterService(services, network);
        boolean isDataRoamingOn = true;
        boolean isRcsEnabledByUser = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1;
        if (this.mMno == Mno.BELL) {
            if (network == 18 || (NetworkUtil.isMobileDataOn(this.mContext) && !SlotBasedConfig.getInstance(this.mPhoneId).isDataUsageExceeded())) {
                boolean isNetworkInRoaming = this.mTelephonyManager.isNetworkRoaming();
                if (ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.DATA_ROAMING_UNKNOWN) != ImsConstants.SystemSettings.ROAMING_DATA_ENABLED) {
                    isDataRoamingOn = false;
                }
                Log.i(LOG_TAG, "isNetworkInRoaming " + isNetworkInRoaming + " isDataRoamingOn " + isDataRoamingOn);
                if (isNetworkInRoaming && !isDataRoamingOn) {
                    removeService(filteredServices, "ft_http", "DataRoaming Disabled");
                    removeService(filteredServices, "im", "DataRoaming Disabled");
                    removeService(filteredServices, "mmtel-video", "DataRoaming Disabled");
                }
            } else {
                Log.i(LOG_TAG, "Remove IM, FT, mmtel-video when Mobile Data off or limited");
                removeService(filteredServices, "ft_http", "MobileData unavailable");
                removeService(filteredServices, "im", "MobileData unavailable");
                removeService(filteredServices, "mmtel-video", "MobileData unavailable");
            }
        } else if (network != 18 && ((!NetworkUtil.isMobileDataOn(this.mContext) || SlotBasedConfig.getInstance(this.mPhoneId).isDataUsageExceeded()) && this.mMno != Mno.ROGERS)) {
            removeService(filteredServices, "mmtel-video", "MobileData unavailable");
        }
        if (this.mMno == Mno.ROGERS && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && this.mTelephonyManager.isNetworkRoaming() && NetworkUtil.isLegacy3gppNetwork(network)) {
            return new HashSet();
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            List<String> enabledRcsServices = RcsConfigurationHelper.getRcsEnabledServiceList(this.mContext, this.mPhoneId, ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, this.mTask.getProfile()));
            for (String service : ImsProfile.getRcsServiceList()) {
                if (!enabledRcsServices.contains(service)) {
                    removeService(filteredServices, service, "Disable from ACS");
                }
            }
            if (!isRcsEnabledByUser) {
                for (String service2 : ImsProfile.getChatServiceList()) {
                    removeService(filteredServices, service2, "chatservice off");
                }
            }
        }
        return applyMmtelUserSettings(filteredServices, network);
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        if (services == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            removeService(services, "mmtel-video", "VoPS Off");
            removeService(services, "mmtel", "VoPS Off");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
            if (this.mMno == Mno.VTR) {
                removeService(services, "smsip", "VoPS Off");
            }
        }
        return services;
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mCurImpu = 0;
        } else if (releaseCase == 6) {
            this.mRegiAt = 0;
        } else if (releaseCase == 9) {
            this.mIsPermanentPdnFailed = false;
        }
        if (!this.mIsPermanentStopped || !this.mIsPermanentPdnFailed) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + releaseCase);
        }
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        if (isMatchedPdnFailReason(getPdnFailureReasons(), reason) && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
            Log.i(LOG_TAG, "call pdn disconnect to clear off state.. : ");
            if (this.mMno.isCanada()) {
                this.mIsPermanentPdnFailed = true;
            }
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mRegHandler.notifyPdnDisconnected(this.mTask);
        }
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        Log.e(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
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
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        if (SipErrorBase.SipErrorType.ERROR_4XX.equals(error) || SipErrorBase.SipErrorType.ERROR_5XX.equals(error) || SipErrorBase.SipErrorType.ERROR_6XX.equals(error)) {
            if (SipErrorBase.isImsForbiddenError(error)) {
                handleForbiddenError(retryAfter);
                return;
            } else {
                this.mFailureCounter++;
                this.mCurPcscfIpIdx++;
            }
        } else if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
            this.mFailureCounter++;
            this.mCurPcscfIpIdx++;
            handleTimeoutError(retryAfter);
        }
        handleRetryTimer(retryAfter);
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> pcscfs, List<String> validPcscfIp, LinkPropertiesWrapper lp) {
        if (lp.hasIPv4Address() && (this.mMno == Mno.BELL || validPcscfIp.isEmpty())) {
            Log.i(LOG_TAG, "ipv4");
            for (String pcscf : pcscfs) {
                if (NetworkUtil.isIPv4Address(pcscf)) {
                    validPcscfIp.add(pcscf);
                }
            }
        }
        return validPcscfIp;
    }

    public boolean isLocationInfoLoaded(int rat) {
        if (this.mTask.getProfile().getSupportedGeolocationPhase() == 0 || rat != 18) {
            return true;
        }
        IGeolocationController geolocationCon = ImsRegistry.getGeolocationController();
        if (geolocationCon != null) {
            if (geolocationCon.isCountryCodeLoaded(this.mPhoneId)) {
                return true;
            }
            if (!geolocationCon.isLocationServiceEnabled()) {
                Log.i(LOG_TAG, "locationService is disabled");
                return false;
            }
            geolocationCon.startGeolocationUpdate(this.mPhoneId, false);
        }
        return false;
    }

    public boolean isThrottled() {
        if (this.mIsPermanentStopped) {
            return true;
        }
        if ((!this.mIsPermanentPdnFailed || this.mTask.getProfile().getPdnType() != 11) && this.mRegiAt <= SystemClock.elapsedRealtime()) {
            return false;
        }
        return true;
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        int regiRat = this.mTask.getRegistrationRat();
        if (!this.mMno.isOneOf(Mno.ROGERS, Mno.TELUS, Mno.KOODO) || foundBestRat == 0 || foundBestRat == regiRat || ((foundBestRat != 18 || regiRat == 13) && (foundBestRat == 13 || regiRat != 18))) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "ROGERS, TELUS and KOODO do de-register between 2/3G and IWLAN.");
        this.mTask.setReason("network changed between 2G/3G and IWLAN.");
        this.mRegMan.tryDeregisterInternal(this.mTask, true, false);
        return true;
    }
}
