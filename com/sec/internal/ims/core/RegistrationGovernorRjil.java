package com.sec.internal.ims.core;

import android.content.Context;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorSwa;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RegistrationGovernorRjil extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnRjil";
    protected List<String> mLastPcscfList = null;

    public RegistrationGovernorRjil(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        this.mNeedToCheckSrvcc = true;
        this.mHandlePcscfOnAlternativeCall = true;
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
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
            if (!unsolicit) {
                if (checkEmergencyInProgress()) {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: No Need to permant fail in emergency registering");
                } else {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Permanently prohibited.");
                    this.mIsPermanentStopped = true;
                }
                if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                    this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                    resetPcscfList();
                    return;
                }
                return;
            }
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: 403 is received for Re-REG, retry according to RFC 5626.");
        } else if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
            handleTimeoutError(retryAfter);
        } else if (SipErrorSwa.AKA_CHANLENGE_TIMEOUT.equals(error)) {
            Log.e(LOG_TAG, "onRegistrationError: Permanently prohibited.");
            this.mTask.setDeregiReason(71);
            this.mIsPermanentStopped = true;
            resetPcscfList();
            this.mRegMan.deregister(this.mTask, true, false, "Aka challenge timeout");
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            return;
        }
        handleRetryTimer(retryAfter);
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mCurImpu = 0;
        } else if (releaseCase == 0) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 1) {
            this.mIsPermanentStopped = false;
        }
        if (!this.mIsPermanentStopped) {
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "releaseThrottle: case by " + releaseCase);
        }
    }

    public void updatePcscfIpList(List<String> pcscfIpList) {
        if (pcscfIpList == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "updatePcscfIpList: null P-CSCF list!");
        } else if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "RJIL Specific: Delaying P-CSCF change as call is in progress");
            this.mLastPcscfList = new ArrayList(pcscfIpList);
        } else {
            super.updatePcscfIpList(pcscfIpList);
        }
    }

    public void onTelephonyCallStatusChanged(int callState) {
        List<String> list;
        if (callState == 0 && (list = this.mLastPcscfList) != null && !list.isEmpty()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "RJIL Specific: Delayed P-CSCF change when call state changed to idle");
            updatePcscfIpList(this.mLastPcscfList);
            this.mLastPcscfList = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0 || this.mTask.isEpdgHandoverInProgress()) {
            return true;
        }
        if (isSrvccCase()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: SRVCC case");
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
        return false;
    }

    private boolean checkProperNetwork(int rat) {
        if (rat == 13 || rat == 18 || this.mTask.getProfile().getPdnType() != 11) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: No proper network");
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkProperNetwork(rat) && checkRegiStatus() && checkRoamingStatus(rat) && checkCallStatus() && checkNetworkEvent(rat)) || checkMdmnProfile();
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>();
        if (services != null) {
            filteredServices.addAll(services);
            boolean isDefaultMessageAppUsed = false;
            boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
            boolean isVolteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1;
            if (!isImsEnabled) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "filterEnabledCoreService: IMS is disabled.");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
                return new HashSet();
            }
            if (isVolteEnabled) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "Forcefully change voiceCall_type to PS(App checks value for making VOLTE Call)");
                ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, this.mPhoneId);
                enabledServices.addAll(servicesByImsSwitch(ImsProfile.getVoLteServiceList()));
                if (!enabledServices.contains("mmtel")) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
                }
            }
            Set<String> enabledServices2 = applyImsSwitch(enabledServices, network);
            if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                isDefaultMessageAppUsed = true;
            }
            if (isDefaultMessageAppUsed && this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
                enabledServices2.addAll(servicesByImsSwitch(ImsProfile.getRcsServiceList()));
                Arrays.stream(ImsProfile.getRcsServiceList()).filter(new Predicate(RcsConfigurationHelper.getRcsEnabledServiceList(this.mContext, this.mPhoneId, ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, this.mTask.getProfile()))) {
                    public final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final boolean test(Object obj) {
                        return RegistrationGovernorRjil.lambda$filterService$0(this.f$0, (String) obj);
                    }
                }).forEach(new Consumer(filteredServices) {
                    public final /* synthetic */ Set f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        RegistrationGovernorRjil.this.lambda$filterService$1$RegistrationGovernorRjil(this.f$1, (String) obj);
                    }
                });
            }
            if (network == 13 && this.mTask.getProfile().getPdnType() == 11) {
                enabledServices2 = applyVoPsPolicy(enabledServices2);
                if (enabledServices2.isEmpty()) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                    return enabledServices2;
                }
            }
            if (!filteredServices.isEmpty()) {
                filteredServices.retainAll(enabledServices2);
            }
            if (!filteredServices.contains("im") && !filteredServices.contains("ec")) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "No RCS services, Remove options");
                filteredServices.remove("options");
            }
            return filteredServices;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "filterServices: services null");
        return filteredServices;
    }

    static /* synthetic */ boolean lambda$filterService$0(List enabledRcsSvcsByAcs, String rcsSvc) {
        return !enabledRcsSvcsByAcs.contains(rcsSvc);
    }

    public /* synthetic */ void lambda$filterService$1$RegistrationGovernorRjil(Set filteredServices, String disabledSvc) {
        removeService(filteredServices, disabledSvc, "Disable from ACS.");
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> pcscfs, List<String> validPcscfIp, LinkPropertiesWrapper lp) {
        if (lp.hasIPv4Address()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "ipv4");
            for (String pcscf : pcscfs) {
                if (NetworkUtil.isIPv4Address(pcscf)) {
                    validPcscfIp.add(pcscf);
                }
            }
        }
        return validPcscfIp;
    }

    public void enableRcsOverIms(ImsProfile rcsProfile) {
        Set<String> services = this.mTask.getProfile().getServiceSet(13);
        services.addAll(rcsProfile.getServiceSet(13));
        this.mTask.getProfile().setServiceSet(13, services);
        this.mTask.getProfile().setNeedAutoconfig(true);
    }
}
