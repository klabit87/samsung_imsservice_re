package com.sec.internal.ims.core;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RegistrationGovernorEur extends RegistrationGovernorBase {
    private static final int DELAYED_DEREGISTER_TIMER = 10;
    private static final String LOG_TAG = "RegiGvnEur";
    private boolean checkEndPcscfList = false;
    protected IAECModule mAECModule;
    protected List<String> mLastPcscfList = null;
    private boolean mNeedDirectRetry = false;
    private Map<String, Long> mPcscfRetryTimeMap = new HashMap();
    protected int mRegiRetryLimit = 0;

    public RegistrationGovernorEur(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        this.mNeedToCheckSrvcc = true;
        this.mNeedToCheckLocationSetting = false;
        IMSLog.i(LOG_TAG, this.mPhoneId, "Register : ShutdownEventReceiver");
        if (this.mMno.isOneOf(Mno.ORANGE_POLAND, Mno.TELIA_NORWAY, Mno.TELIA_SWE, Mno.ORANGE)) {
            updateEutranValues();
        }
        this.mAECModule = ImsRegistry.getAECModule();
        this.mHandlePcscfOnAlternativeCall = true;
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd(this.mPhoneId, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (!SipErrorBase.isImsForbiddenError(error) || !this.mTask.isRcsOnly() || !this.mTask.getProfile().getNeedAutoconfig()) {
            if (retryAfter < 0) {
                retryAfter = 0;
            }
            this.mNeedDirectRetry = false;
            if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error) || (SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error) && !this.mMno.isTmobile() && this.mMno != Mno.TELEKOM_ALBANIA && this.mMno != Mno.BEELINE_RUSSIA)) {
                handleNormalResponse(error, retryAfter);
                return;
            }
            this.mFailureCounter++;
            this.mCurPcscfIpIdx++;
            if (SipErrorBase.isImsForbiddenError(error)) {
                handleForbiddenError(retryAfter);
                if (this.mIsPermanentStopped) {
                    return;
                }
            } else if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
                handleTimeoutError(retryAfter);
                return;
            } else if (SipErrorBase.SERVICE_UNAVAILABLE.equals(error)) {
                handleServiceUnavailable(retryAfter);
            }
            if ((this.mMno.isTmobile() || this.mMno == Mno.TELEKOM_ALBANIA) && this.mTask.getProfile().getPdnType() == 11) {
                handleTmobileVolteError(error, retryAfter);
            }
            handleRetryTimer(retryAfter);
            return;
        }
        int i = this.mRegiRetryLimit;
        if (i > 3) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onRegistrationError: REGI_RETRY_LIMIT is " + this.mRegiRetryLimit + " so ship re-config.");
            return;
        }
        this.mRegiRetryLimit = i + 1;
        this.mConfigModule.startAcs(this.mPhoneId);
    }

    public String getLastPcscfIp() {
        if (CollectionUtils.isNullOrEmpty((Collection<?>) this.mPcscfIpList)) {
            Log.e(LOG_TAG, "getPcscf: empty P-CSCF list.");
            return "";
        } else if (this.mCurPcscfIpIdx < 0) {
            return "";
        } else {
            int lastPcscfIpIdx = this.mCurPcscfIpIdx - 1;
            if (this.mCurPcscfIpIdx == 0) {
                lastPcscfIpIdx = this.mNumOfPcscfIp;
            }
            String lastPcscf = (String) this.mPcscfIpList.get(lastPcscfIpIdx % this.mPcscfIpList.size());
            if (lastPcscf == null) {
                return "";
            }
            return lastPcscf;
        }
    }

    public void resetPcscfList() {
        this.mIsValid = false;
        this.checkEndPcscfList = false;
        this.mPcscfRetryTimeMap.clear();
    }

    public void onSubscribeError(int event, SipError error) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onSubscribeError: state " + this.mTask.getState() + ", error " + error + ", event " + event);
        if ((this.mMno != Mno.TELENOR_DK && this.mMno != Mno.BEELINE_RUSSIA) || event != 0) {
            return;
        }
        if (error.getCode() == 403) {
            this.mSubscribeForbiddenCounter++;
            this.mTask.setDeregiReason(44);
            this.mRegMan.deregister(this.mTask, true, true, "Subscribe Error. Deregister..");
            this.mFailureCounter = this.mSubscribeForbiddenCounter;
            IMSLog.i(LOG_TAG, this.mPhoneId, " onSubscribeError: state " + this.mTask.getState() + " error " + error + " mFailureCounter: " + this.mFailureCounter);
            if (isLastPcscfAddr()) {
                this.mCurPcscfIpIdx = 0;
            }
            int retryAfter = getWaitTime();
            this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
            startRetryTimer(((long) retryAfter) * 1000);
            return;
        }
        this.mSubscribeForbiddenCounter = 0;
    }

    public boolean isThrottled() {
        if (this.mIsPermanentStopped || this.mRegiAt > SystemClock.elapsedRealtime()) {
            return true;
        }
        if (!this.mIsPermanentPdnFailed || this.mTask.getProfile().getPdnType() != 11) {
            return false;
        }
        if (!this.mMno.isOneOf(Mno.SWISSCOM, Mno.SFR, Mno.VODAFONE) || this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId) != 18) {
            return true;
        }
        return false;
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 6) {
            this.mIsPermanentStopped = false;
            this.mIsPermanentPdnFailed = false;
            this.mNonVoLTESimByPdnFail = false;
        } else if (releaseCase == 1) {
            if (isDelayedDeregisterTimerRunning()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "releaseThrottle: delete DelayedDeregisterTimer on fligt mode");
                setDelayedDeregisterTimerRunning(false);
            } else if (this.mTask.isRcsOnly()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "onReceive: FLIGHT_MODE is changed");
                this.mTask.setDeregiReason(23);
                this.mRegMan.deregister(this.mTask, false, false, "flight mode enabled");
            }
            this.mIsPermanentStopped = false;
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
        if (this.mMno == Mno.TELEFONICA_UK) {
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            stopRetryTimer();
        }
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        boolean isNeedToStop = false;
        String matchfrompdnfail = getMatchedPdnFailReason(getPdnFailureReasons(), reason);
        long retryTime = -1;
        if (!TextUtils.isEmpty(matchfrompdnfail)) {
            isNeedToStop = true;
            if (matchfrompdnfail.contains(":")) {
                retryTime = Long.parseLong(matchfrompdnfail.substring(matchfrompdnfail.indexOf(":") + 1));
            }
            setRetryTimeOnPdnFail(retryTime);
        }
        if (isNeedToStop) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            this.mIsPermanentPdnFailed = true;
            this.mNonVoLTESimByPdnFail = true;
            this.mRegHandler.notifyPdnDisconnected(this.mTask);
            if (this.mMno == Mno.TELIA_NORWAY || this.mMno == Mno.TELIA_SWE) {
                updateEutranValues();
            }
        }
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

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x004b, code lost:
        if (r4.mMno.isOneOf(com.sec.internal.constants.Mno.TELE2NL, com.sec.internal.constants.Mno.SWISSCOM, com.sec.internal.constants.Mno.H3G, com.sec.internal.constants.Mno.MEGAFON_RUSSIA, com.sec.internal.constants.Mno.TELEKOM_ALBANIA) != false) goto L_0x004d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updatePcscfIpList(java.util.List<java.lang.String> r5) {
        /*
            r4 = this;
            if (r5 != 0) goto L_0x0021
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "RegiGvnEur["
            r0.append(r1)
            int r1 = r4.mPhoneId
            r0.append(r1)
            java.lang.String r1 = "]"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "updatePcscfIpList: null P-CSCF list!"
            android.util.Log.e(r0, r1)
            return
        L_0x0021:
            com.sec.internal.constants.Mno r0 = r4.mMno
            boolean r0 = r0.isTmobile()
            if (r0 != 0) goto L_0x004d
            com.sec.internal.constants.Mno r0 = r4.mMno
            r1 = 5
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r1]
            r2 = 0
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TELE2NL
            r1[r2] = r3
            r2 = 1
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.SWISSCOM
            r1[r2] = r3
            r2 = 2
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.H3G
            r1[r2] = r3
            r2 = 3
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.MEGAFON_RUSSIA
            r1[r2] = r3
            r2 = 4
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TELEKOM_ALBANIA
            r1[r2] = r3
            boolean r0 = r0.isOneOf(r1)
            if (r0 == 0) goto L_0x0067
        L_0x004d:
            int r0 = r4.mCallStatus
            if (r0 == 0) goto L_0x0067
            com.sec.internal.ims.core.RegisterTask r0 = r4.mTask
            com.sec.ims.settings.ImsProfile r0 = r0.getProfile()
            int r0 = r0.getPdnType()
            r1 = 11
            if (r0 != r1) goto L_0x0067
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>(r5)
            r4.mLastPcscfList = r0
            goto L_0x006a
        L_0x0067:
            super.updatePcscfIpList(r5)
        L_0x006a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorEur.updatePcscfIpList(java.util.List):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x002c, code lost:
        if (r7.mMno.isOneOf(com.sec.internal.constants.Mno.TELE2NL, com.sec.internal.constants.Mno.SWISSCOM, com.sec.internal.constants.Mno.H3G, com.sec.internal.constants.Mno.MEGAFON_RUSSIA, com.sec.internal.constants.Mno.TELEKOM_ALBANIA) != false) goto L_0x002e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTelephonyCallStatusChanged(int r8) {
        /*
            r7 = this;
            r7.mCallStatus = r8
            com.sec.internal.constants.Mno r0 = r7.mMno
            boolean r0 = r0.isTmobile()
            r1 = 2
            r2 = 0
            r3 = 1
            if (r0 != 0) goto L_0x002e
            com.sec.internal.constants.Mno r0 = r7.mMno
            r4 = 5
            com.sec.internal.constants.Mno[] r4 = new com.sec.internal.constants.Mno[r4]
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TELE2NL
            r4[r2] = r5
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.SWISSCOM
            r4[r3] = r5
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.H3G
            r4[r1] = r5
            r5 = 3
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.MEGAFON_RUSSIA
            r4[r5] = r6
            r5 = 4
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.TELEKOM_ALBANIA
            r4[r5] = r6
            boolean r0 = r0.isOneOf(r4)
            if (r0 == 0) goto L_0x0044
        L_0x002e:
            int r0 = r7.mCallStatus
            if (r0 != 0) goto L_0x0044
            java.util.List<java.lang.String> r0 = r7.mLastPcscfList
            if (r0 == 0) goto L_0x0044
            boolean r0 = r0.isEmpty()
            if (r0 != 0) goto L_0x0044
            java.util.List<java.lang.String> r0 = r7.mLastPcscfList
            r7.updatePcscfIpList(r0)
            r0 = 0
            r7.mLastPcscfList = r0
        L_0x0044:
            com.sec.internal.ims.core.RegisterTask r0 = r7.mTask
            com.sec.ims.settings.ImsProfile r0 = r0.getProfile()
            boolean r0 = r0.getBlockDeregiOnSrvcc()
            if (r0 == 0) goto L_0x0094
            int r0 = r7.mCallStatus
            if (r0 != 0) goto L_0x0094
            com.sec.internal.ims.core.RegisterTask r0 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r1 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[r1]
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            r1[r2] = r4
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            r1[r3] = r2
            boolean r0 = r0.isOneOf(r1)
            if (r0 == 0) goto L_0x0094
            boolean r0 = r7.isDeregisterWithVoPSNeeded()
            if (r0 != 0) goto L_0x007e
            boolean r0 = r7.isDeregisterWithRATNeeded()
            if (r0 != 0) goto L_0x007e
            com.sec.internal.ims.core.RegistrationManagerInternal r0 = r7.mRegMan
            int r1 = r7.mPhoneId
            com.sec.internal.constants.ims.os.NetworkEvent r0 = r0.getNetworkEvent(r1)
            boolean r0 = r0.outOfService
            if (r0 == 0) goto L_0x0094
        L_0x007e:
            int r0 = r7.mPhoneId
            java.lang.String r1 = "RegiGvnEur"
            java.lang.String r2 = "onTelephonyCallStatusChanged: delayedDeregisterTimer 10 seconds start"
            com.sec.internal.log.IMSLog.i(r1, r0, r2)
            r7.setDelayedDeregisterTimerRunning(r3)
            com.sec.internal.ims.core.RegistrationManagerInternal r0 = r7.mRegMan
            com.sec.internal.ims.core.RegisterTask r1 = r7.mTask
            r2 = 10000(0x2710, double:4.9407E-320)
            r0.sendDeregister((com.sec.internal.interfaces.ims.core.IRegisterTask) r1, (long) r2)
        L_0x0094:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorEur.onTelephonyCallStatusChanged(int):void");
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        boolean isDefaultAppInUsed = true;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        boolean isVoLteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
            isDefaultAppInUsed = false;
        }
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
        applyRcsSwitch(services, enabledServices2, filteredServices, network);
        if (network == 13 && this.mTask.getProfile().getPdnType() == 11) {
            enabledServices2 = applyVoPsPolicy(enabledServices2);
            if (enabledServices2.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return enabledServices2;
            }
        }
        if (this.mTask.getProfile().getPdnType() == 11) {
            enabledServices2 = applyMmtelUserSettings(enabledServices2, network);
        }
        IAECModule iAECModule = this.mAECModule;
        if (iAECModule != null && iAECModule.isEntitlementRequired(this.mPhoneId)) {
            enabledServices2 = applyEntitlementStatus(enabledServices2, network);
        }
        if (!filteredServices.isEmpty()) {
            filteredServices.retainAll(enabledServices2);
        }
        if (!isDefaultAppInUsed && this.mTask.isRcsOnly()) {
            for (String service : ImsProfile.getRcsServiceList()) {
                removeService(filteredServices, service, "RCS service off");
            }
        }
        return filteredServices;
    }

    private void applyRcsSwitch(Set<String> services, Set<String> enabledServices, Set<String> filteredServices, int network) {
        int i = 0;
        ContentValues switchValue = DmConfigHelper.getImsSwitchValue(this.mContext, (String[]) services.toArray(new String[0]), this.mPhoneId);
        int i2 = 1;
        if (NetworkUtil.isMobileDataOn(this.mContext)) {
            Set<String> set = filteredServices;
            int i3 = network;
        } else if (network != 18) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Mobile off!");
            if (switchValue == null || switchValue.size() <= 0) {
                Set<String> set2 = filteredServices;
            } else {
                for (String service : ImsProfile.getRcsServiceList()) {
                    Integer serviceSwitch = switchValue.getAsInteger(service);
                    if (serviceSwitch == null || serviceSwitch.intValue() != 1) {
                        Set<String> set3 = filteredServices;
                    } else {
                        removeService(filteredServices, service, "MobileOff");
                    }
                }
                Set<String> set4 = filteredServices;
            }
        } else {
            Set<String> set5 = filteredServices;
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            boolean isEnableGls = isGlsEnabled(this.mPhoneId);
            boolean isEnableEc = isEcEnabled(this.mPhoneId);
            if (switchValue == null || switchValue.size() <= 0) {
                Set<String> set6 = enabledServices;
                return;
            }
            String[] rcsServiceList = ImsProfile.getRcsServiceList();
            int length = rcsServiceList.length;
            while (i < length) {
                String service2 = rcsServiceList[i];
                Integer serviceSwitch2 = switchValue.getAsInteger(service2);
                if (serviceSwitch2 == null || serviceSwitch2.intValue() != i2) {
                    Set<String> set7 = enabledServices;
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "by switch and DM + service " + service2);
                    if (service2.equals("gls") && !isEnableGls) {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "skip service " + service2 + " , isEnableGls is " + isEnableGls);
                        Set<String> set8 = enabledServices;
                    } else if (!service2.equals("ec") || isEnableEc) {
                        enabledServices.add(service2);
                    } else {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "skip service " + service2 + " , isEnableEc is " + isEnableEc);
                        Set<String> set9 = enabledServices;
                    }
                }
                i++;
                i2 = 1;
            }
            Set<String> set10 = enabledServices;
            return;
        }
        Set<String> set11 = enabledServices;
    }

    private Set<String> applyEntitlementStatus(Set<String> services, int network) {
        if (services == null) {
            return new HashSet();
        }
        if (network != 18) {
            if (!this.mAECModule.getVoLteEntitlementStatus(this.mPhoneId)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "VoLTE ES not ready");
                removeService(services, "mmtel-video", "VoLTE ES not ready");
                removeService(services, "mmtel", "VoLTE ES not ready");
            }
            if (!this.mAECModule.getSMSoIpEntitlementStatus(this.mPhoneId)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "SMS ES not ready");
                removeService(services, "smsip", "SMS ES not ready");
            }
        }
        if (network == 18) {
            if (!this.mAECModule.getVoWiFiEntitlementStatus(this.mPhoneId)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "VoWiFi ES not ready");
                removeService(services, "mmtel-video", "VoWiFi ES not ready");
                removeService(services, "mmtel", "VoWiFi ES not ready");
            }
            if (!this.mAECModule.getSMSoIpEntitlementStatus(this.mPhoneId)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "SMS ES not ready at IWLAN");
                removeService(services, "smsip", "SMS ES not ready at IWLAN");
            }
        }
        return services;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        if (services == null) {
            return new HashSet();
        }
        if (this.mMno == Mno.H3G && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0) {
            return services;
        }
        if (this.mMno == Mno.ORANGE && this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "not support VoPS, filtering mmtel, mmtel-video.");
            removeService(services, "mmtel-video", "VoPS Off");
            removeService(services, "mmtel", "VoPS Off");
            return services;
        } else if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs != VoPsIndication.NOT_SUPPORTED) {
            return services;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "by VoPS policy: remove all service");
            return new HashSet();
        }
    }

    public SipError onSipError(String service, SipError error) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "onSipError: service=" + service + " error=" + error);
        if (ImsProfile.isRcsService(service) && SipErrorBase.FORBIDDEN.equals(error)) {
            this.mRegMan.deregister(this.mTask, true, true, "403 Forbidden for RCS service");
            Log.i(LOG_TAG, "onSipError() deregister RCS by 403 Forbidden");
        }
        if ("smsip".equals(service) && SipErrorBase.SIP_TIMEOUT.equals(error)) {
            removeCurrentPcscfAndInitialRegister(true);
        }
        if ("mmtel".equals(service)) {
            if (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) || SipErrorBase.SIP_TIMEOUT.equals(error)) {
                removeCurrentPcscfAndInitialRegister(true);
            }
            if (this.mMno == Mno.VODAFONE && SipErrorBase.SERVER_TIMEOUT.equals(error)) {
                removeCurrentPcscfAndInitialRegister(true);
            }
        }
        return error;
    }

    /* access modifiers changed from: protected */
    public int getWaitTime() {
        int waitTime = this.mRegBaseTime << this.mFailureCounter;
        if (waitTime <= 0 || waitTime > this.mRegMaxTime) {
            return this.mRegMaxTime;
        }
        return waitTime;
    }

    private int getActualWaitTime() {
        int wait = getWaitTime();
        return ThreadLocalRandom.current().nextInt(wait / 2, wait + 1);
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0 && !this.mTask.isEpdgHandoverInProgress()) {
            if (isSrvccCase()) {
                if (this.mMno.isOneOf(Mno.ORANGE, Mno.ORANGE_SWITZERLAND, Mno.TELEKOM_ALBANIA) || this.mMno.isTmobile() || this.mTask.getProfile().getBlockDeregiOnSrvcc()) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Skip deregister SRVCC");
                    return false;
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: SRVCC case");
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: call state is not idle");
                return false;
            }
        }
        return true;
    }

    private boolean checkSetupWizard() {
        boolean isSetupWizardCompleted = Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1;
        if (!this.mTask.isRcsOnly() || isSetupWizardCompleted) {
            return true;
        }
        Log.i(LOG_TAG, "SetupWizard is not completed");
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0044, code lost:
        if (r6.mMno.isOneOf(com.sec.internal.constants.Mno.ORANGE, com.sec.internal.constants.Mno.ORANGE_POLAND, com.sec.internal.constants.Mno.WINDTRE, com.sec.internal.constants.Mno.VODAFONE, com.sec.internal.constants.Mno.TELEKOM_ALBANIA) != false) goto L_0x0046;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkAvailableRat(int r7) {
        /*
            r6 = this;
            r0 = 1
            r1 = 13
            if (r7 == r1) goto L_0x0071
            r1 = 18
            if (r7 == r1) goto L_0x0071
            r1 = 20
            if (r7 == r1) goto L_0x0071
            com.sec.internal.ims.core.RegisterTask r1 = r6.mTask
            com.sec.ims.settings.ImsProfile r1 = r1.getProfile()
            int r1 = r1.getPdnType()
            r2 = 11
            if (r1 != r2) goto L_0x0071
            com.sec.internal.constants.Mno r1 = r6.mMno
            boolean r1 = r1.isTmobile()
            r2 = 0
            if (r1 != 0) goto L_0x0046
            com.sec.internal.constants.Mno r1 = r6.mMno
            r3 = 5
            com.sec.internal.constants.Mno[] r3 = new com.sec.internal.constants.Mno[r3]
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ORANGE
            r3[r2] = r4
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ORANGE_POLAND
            r3[r0] = r4
            r4 = 2
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.WINDTRE
            r3[r4] = r5
            r4 = 3
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.VODAFONE
            r3[r4] = r5
            r4 = 4
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TELEKOM_ALBANIA
            r3[r4] = r5
            boolean r1 = r1.isOneOf(r3)
            if (r1 == 0) goto L_0x0071
        L_0x0046:
            int r0 = r6.mPhoneId
            java.lang.String r1 = "RegiGvnEur"
            java.lang.String r3 = "isReadyToRegister: Not LTE area"
            com.sec.internal.log.IMSLog.i(r1, r0, r3)
            com.sec.internal.ims.core.RegisterTask r0 = r6.mTask
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r1 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.DATA_RAT_IS_NOT_LTE
            int r1 = r1.getCode()
            r0.setRegiFailReason(r1)
            com.sec.internal.ims.core.RegisterTask r0 = r6.mTask
            r0.setRegistrationRat(r7)
            com.sec.internal.ims.core.RegisterTask r0 = r6.mTask
            com.sec.ims.ImsRegistration r0 = r0.getImsRegistration()
            if (r0 == 0) goto L_0x0070
            com.sec.internal.ims.core.RegisterTask r0 = r6.mTask
            com.sec.ims.ImsRegistration r0 = r0.getImsRegistration()
            r0.setCurrentRat(r7)
        L_0x0070:
            return r2
        L_0x0071:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorEur.checkAvailableRat(int):boolean");
    }

    private boolean checkDeregisterTimer() {
        if (!this.mTask.getProfile().getBlockDeregiOnSrvcc() || !isDelayedDeregisterTimerRunning()) {
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: DelayedDeregisterTimer Running.");
        if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded() || this.mRegMan.getNetworkEvent(this.mPhoneId).outOfService) {
            return false;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: LTE attached. Delete DelayedDeregisterTimer.");
        onDelayedDeregister();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsEvent(int rat) {
        if (this.mTask.isRcsOnly() && ImsRegistry.getInt(this.mPhoneId, GlobalSettingsConstants.RCS.PRE_CONSENT, 0) == 1) {
            boolean isRcsUserSettingEnabled = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == 1;
            if (RcsConfigurationHelper.readIntParam(this.mContext, "version", 0).intValue() <= 0 && !isRcsUserSettingEnabled) {
                Log.i(LOG_TAG, "isReadyToRegister: User don't try RCS service yet");
                return false;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                Log.i(LOG_TAG, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            }
        }
        return true;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkSetupWizard() && checkRegiStatus() && checkRoamingStatus(rat) && checkAvailableRat(rat) && checkCallStatus() && checkWFCsettings(rat) && checkDeregisterTimer() && checkNetworkEvent(rat) && checkDelayedStopPdnEvent() && checkRcsEvent(rat));
    }

    public void onVolteSettingChanged() {
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("onVolteSettingChanged: isVolteOn=");
        sb.append(getVoiceTechType() == 0);
        IMSLog.i(LOG_TAG, i, sb.toString());
        if (this.mMno.isOneOf(Mno.ORANGE_POLAND, Mno.TELIA_NORWAY, Mno.TELIA_SWE, Mno.ORANGE)) {
            updateEutranValues();
        }
    }

    public boolean isDelayedDeregisterTimerRunning() {
        return isDelayedDeregisterTimerRunningWithCallStatus();
    }

    public void onDelayedDeregister() {
        super.runDelayedDeregister();
    }

    public boolean isLocationInfoLoaded(int rat) {
        if (!this.mMno.isTeliaCo()) {
            return true;
        }
        return super.isLocationInfoLoaded(rat);
    }

    public void onRegistrationDone() {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "onRegistrationDone: state " + this.mTask.getState());
        this.mRegiRetryLimit = 0;
        this.mFailureCounter = 0;
        this.mRegiAt = 0;
        stopRetryTimer();
    }

    private boolean isGlsEnabled(int phoneId) {
        return RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, phoneId)).booleanValue();
    }

    public void updateEutranValues() {
        if (this.mTask.getProfile().hasService("mmtel")) {
            int voiceTech = getVoiceTechType();
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "updateEutranValues : voiceTech : " + voiceTech);
            ContentValues eutranValue = new ContentValues();
            if (voiceTech == 0) {
                eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 3);
            } else {
                eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 1);
            }
            if (this.mMno.isOneOf(Mno.TELIA_NORWAY, Mno.TELIA_SWE) && this.mNonVoLTESimByPdnFail) {
                eutranValue.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, 1);
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri.Builder buildUpon = Uri.parse("content://com.sec.ims.settings/global").buildUpon();
            contentResolver.update(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + this.mPhoneId).build(), eutranValue, (String) null, (String[]) null);
        }
    }

    public boolean allowRoaming() {
        if (this.mTask.getProfile().hasEmergencySupport()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "allowRoaming: Emergency profile. Return true.");
            return true;
        } else if (this.mMno != Mno.BTOP || this.mTask.getProfile().isAllowedOnRoaming()) {
            return this.mTask.getProfile().isAllowedOnRoaming();
        } else {
            if (this.mPdnController.isInternationalRoaming(this.mPhoneId)) {
                return false;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "allowRoaming: Domestic roaming. Return true.");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(int retryAfter) {
        if (this.mMno.isOneOf(Mno.ORANGE_MOLDOVA, Mno.BOG, Mno.UPC_CH, Mno.TELEFONICA_SPAIN) || (this.mMno == Mno.ORANGE && retryAfter > 0)) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Retry to same PCSCF address in case 403 Forbidden");
            this.mCurPcscfIpIdx--;
            return;
        }
        if (this.mMno.isOneOf(Mno.ORANGE_SPAIN, Mno.SFR, Mno.TELEKOM_ALBANIA, Mno.TELENOR_DK) || this.mMno.isTmobile()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Retry to next PCSCF address in case 403 Forbidden");
            return;
        }
        if (!this.mMno.isOneOf(Mno.TELIA_NORWAY, Mno.EE, Mno.EE_ESN) || !checkEmergencyInProgress() || this.mTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTED) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Permanently prohibited.");
            this.mIsPermanentStopped = true;
            if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
                return;
            }
            return;
        }
        IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: No need permant fail in emergency registering");
        this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
        resetPcscfList();
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(int retryAfter) {
        if (isLastPcscfAddr() && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            if (!this.mTask.isRcsOnly()) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
            } else {
                IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: skip resetPcscfList.");
            }
        }
        if (this.mMno == Mno.ORANGE && retryAfter == 0) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "onRegistrationError: Orange requirment,send Try register after timer F next PCSF address");
            this.mNeedDirectRetry = true;
        }
    }

    /* access modifiers changed from: protected */
    public void handleRetryTimer(int retryAfter) {
        if (isLastPcscfAddr()) {
            this.mCurPcscfIpIdx = 0;
            if (this.mTask.isRcsOnly()) {
                this.checkEndPcscfList = true;
            }
        }
        if (this.mNeedDirectRetry) {
            retryAfter = 1;
        }
        if (retryAfter == 0) {
            retryAfter = getActualWaitTime();
            Log.i(LOG_TAG, "retryAfter set to ActualWaitTime = " + retryAfter + "; mFailureCounter = " + this.mFailureCounter);
        }
        if (this.mTask.isRcsOnly() && this.checkEndPcscfList) {
            if (this.mPcscfRetryTimeMap.containsKey(getCurrentPcscfIp())) {
                long getRetryTime = this.mPcscfRetryTimeMap.get(getCurrentPcscfIp()).longValue();
                if (getRetryTime - SystemClock.elapsedRealtime() <= 0) {
                    retryAfter = 1;
                } else {
                    retryAfter = (int) ((getRetryTime - SystemClock.elapsedRealtime()) / 1000);
                }
            } else {
                retryAfter = getWaitTime();
            }
        }
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
        startRetryTimer(((long) retryAfter) * 1000);
    }

    private boolean isLastPcscfAddr() {
        return this.mCurPcscfIpIdx >= this.mNumOfPcscfIp;
    }

    private int handleServiceUnavailable(int retryAfter) {
        if (this.mMno.isOneOf(Mno.TELIA_SWE, Mno.MEGAFON_RUSSIA)) {
            if (isLastPcscfAddr() && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
            }
            this.mNeedDirectRetry = true;
        } else if (this.mMno == Mno.TELEFONICA_SPAIN) {
            if (retryAfter != 0) {
                this.mCurPcscfIpIdx--;
            }
        } else if (this.mTask.isRcsOnly()) {
            retryAfter = retryAfter == 0 ? getWaitTime() : retryAfter;
            long waitTime = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
            String lastPcscf = getLastPcscfIp();
            if (!lastPcscf.isEmpty()) {
                this.mPcscfRetryTimeMap.put(lastPcscf, Long.valueOf(waitTime));
            }
            this.mNeedDirectRetry = true ^ this.checkEndPcscfList;
        }
        return retryAfter;
    }

    private void handleTmobileVolteError(SipError error, int retryAfter) {
        if (SipErrorBase.SipErrorType.ERROR_4XX.equals(error) || SipErrorBase.SipErrorType.ERROR_5XX.equals(error) || SipErrorBase.SipErrorType.ERROR_6XX.equals(error)) {
            if (retryAfter != 0) {
                this.mCurPcscfIpIdx--;
            }
            if (isLastPcscfAddr() && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
                resetPcscfList();
                this.mNeedDirectRetry = true;
            }
        }
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        if (foundBestRat != 0) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        if (this.mMno == Mno.TELENOR_DK) {
            this.mTask.setDeregiReason(4);
            this.mRegMan.deregister(this.mTask, false, false, 5000, "Telenor DK delay 5s to deregister");
            return true;
        }
        boolean localDeRegi = false;
        if (this.mMno == Mno.TELEKOM_ALBANIA) {
            localDeRegi = this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) && isSrvccCase();
        }
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "isNeedToDeRegistration: no IMS service for network " + currentRat + ". Deregister.");
        RegisterTask registerTask = this.mTask;
        registerTask.setReason("no IMS service for network : " + currentRat);
        this.mTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(this.mTask, localDeRegi, false);
        return true;
    }

    public boolean onUpdateGeolocation(LocationInfo geolocation) {
        if (this.mMno == Mno.TELENOR_DK && !TextUtils.isEmpty(geolocation.mCountry) && isThrottled()) {
            releaseThrottle(6);
        }
        if (this.mMno.isOneOf(Mno.TELEFONICA_UK)) {
            updateGeolocation(geolocation.mCountry);
        }
        return false;
    }
}
