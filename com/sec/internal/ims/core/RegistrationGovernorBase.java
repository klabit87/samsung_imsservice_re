package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.ISemTelephony;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.extensions.SemEmergencyConstantsExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorBase extends RegistrationGovernor {
    protected static final int DELAY_RESTORE_SETTING_TIMER = 1000;
    private static final String LOG_TAG = "RegiGvnBase";
    protected IConfigModule mConfigModule;
    protected Context mContext;
    protected Mno mMno;
    private BroadcastReceiver mPackageDataClearedIntentReceiver;
    protected PdnController mPdnController;
    protected RegistrationManagerHandler mRegHandler;
    protected RegistrationManagerInternal mRegMan;
    protected String mSamsungMsgPackage = "";
    protected RegisterTask mTask;
    protected ITelephonyManager mTelephonyManager = null;
    protected final BroadcastReceiver mUpsmEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(RegistrationGovernorBase.LOG_TAG, "Received UpsmEvent: " + intent.getAction() + " extra: " + intent.getExtras());
            RegistrationGovernorBase.this.onUltraPowerSavingModeChanged();
        }
    };
    protected IVolteServiceModule mVsm = null;

    protected RegistrationGovernorBase() {
    }

    public RegistrationGovernorBase(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        this.mContext = context;
        this.mRegMan = regMan;
        this.mPdnController = pdnController;
        this.mTask = task;
        this.mPhoneId = task.getPhoneId();
        this.mMno = Mno.fromName(task.getProfile().getMnoName());
        this.mRegBaseTime = task.getProfile().getRegRetryBaseTime();
        this.mRegMaxTime = task.getProfile().getRegRetryMaxTime();
        this.mTelephonyManager = telephonyManager;
        this.mVsm = vsm;
        this.mConfigModule = cm;
        if (this.mRegMan != null) {
            this.mRegHandler = regMan.getRegistrationManagerHandler();
        }
        this.mSamsungMsgPackage = PackageUtils.getMsgAppPkgName(context);
        IntentFilter packageDataClearedIntentfilter = new IntentFilter();
        packageDataClearedIntentfilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        packageDataClearedIntentfilter.addDataScheme("package");
        AnonymousClass1 r1 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Uri data = intent.getData();
                int i = RegistrationGovernorBase.this.mPhoneId;
                IMSLog.s(RegistrationGovernorBase.LOG_TAG, i, "onReceive:" + intent.getAction() + " mTask:" + RegistrationGovernorBase.this.mTask.getProfile().getName() + "(" + RegistrationGovernorBase.this.mTask.getState() + ")");
                if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action) && data != null) {
                    RegistrationGovernorBase.this.onPackageDataCleared(data);
                }
            }
        };
        this.mPackageDataClearedIntentReceiver = r1;
        this.mContext.registerReceiver(r1, packageDataClearedIntentfilter);
    }

    public void onPackageDataCleared(Uri data) {
        this.mRegMan.getEventLog().logAndAdd("onReceive: ACTION_PACKAGE_DATA_CLEARED is received");
        String dataClearedPackageName = data.getSchemeSpecificPart();
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "Intent received is packageName: " + dataClearedPackageName + ", mSamsungMsgPackage: " + this.mSamsungMsgPackage);
        if ((this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) && !TextUtils.isEmpty(dataClearedPackageName) && !TextUtils.isEmpty(this.mSamsungMsgPackage) && TextUtils.equals(dataClearedPackageName, this.mSamsungMsgPackage)) {
            String rcsAs = ConfigUtil.getAcsServerType(this.mContext, this.mTask.getPhoneId());
            if (this.mMno == Mno.ATT || this.mMno == Mno.VZW || (this.mTask.isRcsOnly() && (ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(rcsAs) || this.mMno == Mno.CMCC))) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        ImsConstants.SystemSettings.setRcsUserSetting(RegistrationGovernorBase.this.mContext, DmConfigHelper.getImsUserSetting(RegistrationGovernorBase.this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), RegistrationGovernorBase.this.mTask.getPhoneId()), RegistrationGovernorBase.this.mTask.getPhoneId());
                    }
                }, 1000);
            } else if (this.mMno.isKor() && this.mTask.isRcsOnly()) {
                setBotAgreementToFile(0);
                this.mRegHandler.notifyChatbotAgreementChanged(this.mTask.getPhoneId());
                DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mTask.getPhoneId()), this.mTask.getPhoneId());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setBotAgreementToFile(int result) {
        IMSLog.s(LOG_TAG, this.mPhoneId, "setBotAgreementToFile : " + result);
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        String imsi = "IMSI_";
        if (sm != null) {
            imsi = "IMSI_" + sm.getImsi();
        }
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "bot_agreement_from_app", imsi, result == 1 ? "1" : "0");
    }

    /* access modifiers changed from: protected */
    public void handleNormalResponse(SipError error, int retryAfter) {
        int retryAfter2;
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        if (SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            retryAfter2 = 128;
        } else {
            retryAfter2 = 1;
        }
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter2) * 1000);
        startRetryTimer(((long) retryAfter2) * 1000);
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(int retryAfter) {
        Log.e(LOG_TAG, "onRegistrationError: Permanently prohibited.");
        this.mIsPermanentStopped = true;
        if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            resetPcscfList();
        }
    }

    /* access modifiers changed from: protected */
    public void handleTimeoutError(int retryAfter) {
        if (this.mCurPcscfIpIdx == this.mNumOfPcscfIp && this.mTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            resetPcscfList();
        }
    }

    /* access modifiers changed from: protected */
    public void handlePcscfError() {
        this.mTask.mKeepPdn = false;
        int retryAfter = getWaitTime();
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
        startRetryTimer(((long) retryAfter) * 1000);
    }

    /* access modifiers changed from: protected */
    public void handleRetryTimer(int retryAfter) {
        if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
            this.mCurPcscfIpIdx = 0;
        }
        if (retryAfter == 0) {
            retryAfter = getWaitTime();
        }
        this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter) * 1000);
        startRetryTimer(((long) retryAfter) * 1000);
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        this.mRegMan.getEventLog().logAndAdd("onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.OK.equals(error) || SipErrorBase.NOTIFY_TERMINATED_DEACTIVATED.equals(error) || SipErrorBase.NOTIFY_TERMINATED_REJECTED.equals(error)) {
            handleNormalResponse(error, retryAfter);
            return;
        }
        this.mFailureCounter++;
        this.mCurPcscfIpIdx++;
        if (SipErrorBase.isImsForbiddenError(error)) {
            handleForbiddenError(retryAfter);
            return;
        }
        if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
            handleTimeoutError(retryAfter);
        } else if (SipErrorBase.EMPTY_PCSCF.equals(error)) {
            handlePcscfError();
            return;
        }
        handleRetryTimer(retryAfter);
    }

    public void onRegistrationDone() {
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "onRegistrationDone: state " + this.mTask.getState());
        this.mFailureCounter = 0;
        this.mRegiAt = 0;
        stopRetryTimer();
    }

    public void onTimsTimerExpired() {
        stopTimsEstablishTimer(this.mTask, RegistrationConstants.REASON_TIMS_EXPIRED);
        this.mTask.setNotAvailableReason(1);
        this.mRegMan.notifyImsNotAvailable(this.mTask, true);
    }

    /* access modifiers changed from: protected */
    public void removeCurrentPcscfAndInitialRegister(boolean forceInitialRegi) {
        String curPcscfIp = getCurrentPcscfIp();
        this.mPcscfIpList.remove(curPcscfIp);
        this.mNumOfPcscfIp--;
        updatePcscfIpList(this.mPcscfIpList, forceInitialRegi);
        IMSLog.s(LOG_TAG, this.mPhoneId, "removeCurrentPcscfAndInitialRegister(): curPcscfIp " + curPcscfIp + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mCurPcscfIpIndex " + this.mCurPcscfIpIdx + " mPcscfIpList " + this.mPcscfIpList);
    }

    /* access modifiers changed from: protected */
    public void handleAlternativeCallState() {
        if (this.mHandlePcscfOnAlternativeCall) {
            this.mCurPcscfIpIdx++;
            if (this.mCurPcscfIpIdx >= this.mNumOfPcscfIp) {
                this.mCurPcscfIpIdx = 0;
            }
        }
        this.mTask.setDeregiReason(7);
        this.mRegMan.deregister(this.mTask, true, true, "call state changed");
    }

    public void onCallStatus(IRegistrationGovernor.CallEvent event, SipError error, int callType) {
        Log.i(LOG_TAG, "onCallStatus: event=" + event + " error=" + error);
        if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED) {
            this.mHasVoLteCall = true;
        } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_LAST_CALL_END) {
            this.mHasVoLteCall = false;
        } else if (event == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_INITIAL_REGI) {
            handleAlternativeCallState();
        }
    }

    public SipError onSipError(String service, SipError error) {
        Log.i(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if ("mmtel".equals(service) && (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) || SipErrorBase.SIP_TIMEOUT.equals(error))) {
            removeCurrentPcscfAndInitialRegister(true);
        }
        return error;
    }

    public int getFailureType() {
        if (this.mDiscardCurrentNetwork) {
            return 32;
        }
        if (this.mIsPermanentStopped) {
            return 33;
        }
        return 16;
    }

    /* access modifiers changed from: protected */
    public void removeService(Set<String> services, String rmSvc, String reason) {
        if (services.remove(rmSvc)) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "remove service: " + rmSvc + "(" + reason + ")");
            this.mTask.addFilteredReason(rmSvc, reason);
        }
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        int i = 0;
        boolean isDefaultMsgAppInUsed = true;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        boolean isVoLteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
            isDefaultMsgAppInUsed = false;
        }
        if (!isImsEnabled) {
            Log.i(LOG_TAG, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        } else if ((this.mTask.getProfile().getPdnType() == -1 || this.mTask.getProfile().getPdnType() == 0) && !NetworkUtil.isMobileDataOn(this.mContext) && network != 18) {
            Log.i(LOG_TAG, "filterService: Mobile data off");
            return new HashSet();
        } else {
            if (isVoLteEnabled) {
                Set<String> serviceList = servicesByImsSwitch(ImsProfile.getVoLteServiceList());
                if (!serviceList.contains("mmtel")) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
                }
                enabledServices.addAll(servicesByReadSwitch((String[]) serviceList.toArray(new String[0])));
                if (serviceList.contains("mmtel") && !enabledServices.contains("mmtel")) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_DM_OFF.getCode());
                }
            }
            if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
                enabledServices.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch(ImsProfile.getRcsServiceList()).toArray(new String[0])));
            }
            if ((network == 13 || network == 20) && this.mTask.getProfile().getPdnType() == 11) {
                enabledServices = applyVoPsPolicy(enabledServices);
                if (enabledServices.isEmpty()) {
                    this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                    return enabledServices;
                }
            }
            if (!isVideoCallEnabled()) {
                removeService(filteredServices, "mmtel-video", "Videocall disabled.");
            }
            if (!isDefaultMsgAppInUsed) {
                if (this.mTask.isRcsOnly()) {
                    String[] rcsServiceList = ImsProfile.getRcsServiceList();
                    int length = rcsServiceList.length;
                    while (i < length) {
                        removeService(filteredServices, rcsServiceList[i], "DefaultAppInUse is false");
                        i++;
                    }
                } else {
                    String[] chatServiceList = ImsProfile.getChatServiceList();
                    int length2 = chatServiceList.length;
                    while (i < length2) {
                        removeService(filteredServices, chatServiceList[i], "DefaultAppInUse is false");
                        i++;
                    }
                }
            }
            if (!filteredServices.isEmpty()) {
                filteredServices.retainAll(enabledServices);
            }
            return filteredServices;
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> servicesByImsSwitch(String[] serviceList) {
        Set<String> services = new HashSet<>();
        ContentValues switchValue = DmConfigHelper.getImsSwitchValue(this.mContext, serviceList, this.mPhoneId);
        for (String service : serviceList) {
            if (switchValue.getAsInteger(service) != null && switchValue.getAsInteger(service).intValue() == 1) {
                services.add(service);
            }
        }
        return services;
    }

    /* access modifiers changed from: protected */
    public Set<String> servicesByReadSwitch(String[] serviceList) {
        Set<String> services = new HashSet<>();
        for (String service : serviceList) {
            if (DmConfigHelper.readSwitch(this.mContext, service, true, this.mPhoneId)) {
                services.add(service);
            }
        }
        return services;
    }

    /* access modifiers changed from: protected */
    public Set<String> applyVoPsPolicy(Set<String> services) {
        if (services == null) {
            return new HashSet();
        }
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs != VoPsIndication.NOT_SUPPORTED) {
            return services;
        }
        Log.i(LOG_TAG, "by VoPS policy: remove all service");
        return new HashSet();
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv6Addr(List<String> pcscfs, List<String> validPcscfIp, LinkPropertiesWrapper lp) {
        if (lp.hasGlobalIPv6Address() || lp.hasIPv6DefaultRoute()) {
            for (String pcscf : pcscfs) {
                if (NetworkUtil.isIPv6Address(pcscf)) {
                    validPcscfIp.add(pcscf);
                }
            }
        }
        return validPcscfIp;
    }

    /* access modifiers changed from: protected */
    public List<String> addIpv4Addr(List<String> pcscfs, List<String> validPcscfIp, LinkPropertiesWrapper lp) {
        if (validPcscfIp.isEmpty() && lp.hasIPv4Address()) {
            Log.i(LOG_TAG, "ipv4");
            for (String pcscf : pcscfs) {
                if (NetworkUtil.isIPv4Address(pcscf)) {
                    validPcscfIp.add(pcscf);
                }
            }
        }
        return validPcscfIp;
    }

    public List<String> checkValidPcscfIp(List<String> pcscfs) {
        List<String> validPcscfIp = new ArrayList<>();
        LinkPropertiesWrapper lp = this.mPdnController.getLinkProperties(this.mTask);
        if (pcscfs == null || pcscfs.isEmpty() || lp == null) {
            return validPcscfIp;
        }
        List<String> validPcscfIp2 = addIpv4Addr(pcscfs, addIpv6Addr(pcscfs, validPcscfIp, lp), lp);
        Log.i(LOG_TAG, "ValidPcscfIp: " + validPcscfIp2);
        return validPcscfIp2;
    }

    public void updatePcscfIpList(List<String> pcscfIpList) {
        updatePcscfIpList(pcscfIpList, false);
    }

    /* access modifiers changed from: protected */
    public void updatePcscfIpList(List<String> pcscfIpList, boolean forceInitialRegi) {
        if (pcscfIpList == null) {
            Log.e(LOG_TAG, "updatePcscfIpList: null P-CSCF list!");
            return;
        }
        String curPcscfIp = getCurrentPcscfIp();
        this.mNumOfPcscfIp = pcscfIpList.size();
        this.mPcscfIpList = pcscfIpList;
        this.mIsValid = this.mNumOfPcscfIp > 0;
        int curPcscfIndex = this.mPcscfIpList.indexOf(curPcscfIp);
        if (curPcscfIndex >= 0) {
            if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                Log.i(LOG_TAG, "updatePcscfIpList: keeping " + curPcscfIp + " as current forceInitialRegi=" + forceInitialRegi + " mMoveToNextPcscfAfterTimerB=" + this.mMoveToNextPcscfAfterTimerB);
                this.mCurPcscfIpIdx = curPcscfIndex;
                if (forceInitialRegi) {
                    if (this.mMno.isKor()) {
                        if (this.mMoveToNextPcscfAfterTimerB) {
                            this.mFailureCounter = 0;
                            this.mCurImpu = 0;
                            this.mRegiAt = 0;
                        } else {
                            resetRetry();
                        }
                        this.mMoveToNextPcscfAfterTimerB = false;
                    }
                    if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        this.mTask.setDeregiReason(8);
                        this.mRegMan.deregister(this.mTask, true, this.mIsValid, "pcscf updated");
                        return;
                    }
                    return;
                } else if (this.mMno == Mno.VZW) {
                    this.mRegMan.sendReRegister(this.mTask);
                    return;
                } else {
                    return;
                }
            }
        }
        Log.i(LOG_TAG, "updatePcscfIpList: whole new set of PCSCFs (" + this.mTask.getState() + ")");
        resetRetry();
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            this.mTask.setDeregiReason(8);
            this.mRegMan.deregister(this.mTask, true, this.mIsValid, "pcscf updated");
        }
    }

    public String getCurrentPcscfIp() {
        if (CollectionUtils.isNullOrEmpty((Collection<?>) this.mPcscfIpList)) {
            Log.e(LOG_TAG, "getNextPcscf: empty P-CSCF list.");
            return "";
        }
        String curPcscf = (String) this.mPcscfIpList.get(this.mCurPcscfIpIdx % this.mPcscfIpList.size());
        Log.i(LOG_TAG, "getCurrentPcscfIp: " + curPcscf);
        return curPcscf;
    }

    public void resetRetry() {
        Log.i(LOG_TAG, "resetRetry()");
        this.mFailureCounter = 0;
        this.mCurPcscfIpIdx = 0;
        this.mCurImpu = 0;
        this.mRegiAt = 0;
        resetIPSecAllow();
    }

    /* access modifiers changed from: protected */
    public int getWaitTime() {
        int waitTime = this.mRegBaseTime * ((int) Math.pow(2.0d, (double) (this.mFailureCounter - 1)));
        if (waitTime < 0) {
            return this.mRegMaxTime;
        }
        return Math.min(this.mRegMaxTime, waitTime);
    }

    public long getNextRetryMillis() {
        if (this.mIsPermanentStopped || this.mIsPermanentPdnFailed) {
            return -1;
        }
        return Math.max(0, this.mRegiAt - SystemClock.elapsedRealtime());
    }

    /* access modifiers changed from: protected */
    public void startTimsEstablishTimer(RegisterTask task, long millis, String reason) {
        if (task.getProfile().hasEmergencySupport()) {
            Log.i(LOG_TAG, "Emergecy Task doens't required Tims timer.");
        } else if (!task.isRcsOnly() || !task.getMno().isKor()) {
            if (this.mTimEshtablishTimeout != null) {
                Log.i(LOG_TAG, "Tims is running. don't need to start new timer.");
                return;
            }
            Log.i(LOG_TAG, "startTimsEstablishTimer: millis = " + millis + ", reason = [" + reason + "]");
            this.mTimEshtablishTimeout = this.mRegHandler.startTimsEshtablishTimer(task, millis);
        } else if (this.mTimEshtablishTimeoutRCS != null) {
            Log.i(LOG_TAG, "TimsRCS is running. don't need to start new timer.");
        } else {
            Log.i(LOG_TAG, "start TimsRCS timer; millis = " + millis + ", reason = [" + reason + "]");
            this.mTimEshtablishTimeoutRCS = this.mRegHandler.startTimsEshtablishTimer(task, millis);
        }
    }

    /* access modifiers changed from: protected */
    public void stopTimsEstablishTimer(RegisterTask task, String reason) {
        if (!task.isRcsOnly() || !task.getMno().isKor()) {
            Log.i(LOG_TAG, "stop Tims timer by " + reason);
            if (this.mTimEshtablishTimeout != null) {
                this.mRegHandler.stopTimer(this.mTimEshtablishTimeout);
                this.mTimEshtablishTimeout = null;
                return;
            }
            return;
        }
        Log.i(LOG_TAG, "stop TimsRCS timer by " + reason);
        if (this.mTimEshtablishTimeoutRCS != null) {
            this.mRegHandler.stopTimer(this.mTimEshtablishTimeoutRCS);
            this.mTimEshtablishTimeoutRCS = null;
        }
    }

    /* access modifiers changed from: protected */
    public void startRetryTimer(long millis) {
        stopRetryTimer();
        Log.i(LOG_TAG, "startRetryTimer: millis " + millis);
        this.mRetryTimeout = this.mRegHandler.startRegistrationTimer(this.mTask, millis);
    }

    /* access modifiers changed from: protected */
    public void stopRetryTimer() {
        if (this.mRetryTimeout != null) {
            Log.i(LOG_TAG, "stopRetryTimer; what = " + this.mRetryTimeout.what);
            this.mRegHandler.stopTimer(this.mRetryTimeout);
            this.mRetryTimeout = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkRoamingStatus(int rat) {
        if (rat == 18 || !this.mTelephonyManager.isNetworkRoaming(SimUtil.getSubId(this.mPhoneId)) || allowRoaming()) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: roaming is not allowed.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (!this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mRegMan.getTelephonyCallStatus(this.mPhoneId) == 0) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: call state is not idle");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkEmergencyStatus() {
        return this.mTask.getProfile().hasEmergencySupport() && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING;
    }

    /* access modifiers changed from: protected */
    public boolean checkRegiStatus() {
        Log.i(LOG_TAG, "checkRegiStatus: getState()=" + this.mTask.getState() + " mIsUpdateRegistering=" + this.mTask.mIsUpdateRegistering);
        if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING && !this.mTask.mIsUpdateRegistering) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkNetworkEvent(int rat) {
        if (!this.mRegHandler.hasNetworModeChangeEvent() || !this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS) || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: networkModeChangeTimer Running.");
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ONGOING_NW_MODE_CHANGE.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkWFCsettings(int rat) {
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURED) && rat == 18 && this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            int wifion = DeviceUtil.getWifiStatus(this.mContext, 0);
            boolean vowifi_enable = VowifiConfig.isEnabled(this.mContext, this.mPhoneId);
            if (wifion == 0 || !vowifi_enable) {
                Log.i("RegiGvnBase[" + this.mPhoneId + "]", "VoWiFi menu is not enabled or WIFI is not enabled");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkDelayedStopPdnEvent() {
        if (!this.mRegHandler.hasDelayedStopPdnEvent() || !this.mTask.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
            return true;
        }
        Log.i(LOG_TAG, "stopPdn would be called soon. Skip IMS registration");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkMdmnProfile() {
        return this.mTask.getProfile().isSamsungMdmnEnabled();
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkEpdgEvent(rat) && checkCallStatus() && checkRoamingStatus(rat) && checkVolteSetting(rat) && checkNetworkEvent(rat) && checkDelayedStopPdnEvent() && checkRcsEvent(rat)) || checkMdmnProfile();
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

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 1 || releaseCase == 7) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 4) {
            this.mIsPermanentStopped = false;
            this.mCurImpu = 0;
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + releaseCase);
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        int voiceType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, this.mPhoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("getVoiceTechType: ");
        sb.append(voiceType == 0 ? "VOLTE" : "CS");
        Log.i(LOG_TAG, sb.toString());
        return voiceType;
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType(int phoneId) {
        int voiceType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, phoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("getVoiceTechType: ");
        sb.append(voiceType == 0 ? "VOLTE" : "CS");
        Log.i(LOG_TAG, sb.toString());
        return voiceType;
    }

    /* access modifiers changed from: protected */
    public boolean isVideoCallEnabled() {
        int videoCallType = ImsConstants.SystemSettings.getVideoCallType(this.mContext, -1, this.mPhoneId);
        StringBuilder sb = new StringBuilder();
        sb.append("isVideoCallEnabled: ");
        sb.append(videoCallType == 0 ? "Enable" : "Disable");
        Log.i(LOG_TAG, sb.toString());
        return videoCallType == 0;
    }

    public boolean allowRoaming() {
        if (!this.mTask.getProfile().hasEmergencySupport()) {
            return this.mTask.getProfile().isAllowedOnRoaming();
        }
        Log.i(LOG_TAG, "allowRoaming: Emergency profile. Return true.");
        return true;
    }

    public boolean isLocationInfoLoaded(int rat) {
        if (this.mTask.getProfile().getSupportedGeolocationPhase() == 0 || rat != 18) {
            return true;
        }
        IGeolocationController geolocationCon = ImsRegistry.getGeolocationController();
        if (geolocationCon != null) {
            if (this.mNeedToCheckLocationSetting && !geolocationCon.isLocationServiceEnabled()) {
                Log.i(LOG_TAG, "locationService is disabled");
                return false;
            } else if (geolocationCon.isCountryCodeLoaded(this.mPhoneId)) {
                return true;
            } else {
                geolocationCon.startGeolocationUpdate(this.mPhoneId, false);
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isDeregisterWithRATNeeded() {
        boolean result = false;
        int rat = this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId);
        if (!(rat == 13 || rat == 20 || rat == 18)) {
            result = true;
        }
        Log.i(LOG_TAG, "isDeregisterWithRATNeeded [" + result + "]");
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean isDeregisterWithVoPSNeeded() {
        int rat;
        boolean result = false;
        if (this.mRegMan.getNetworkEvent(this.mPhoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED && ((rat = this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId)) == 13 || rat == 20)) {
            result = true;
        }
        Log.i(LOG_TAG, "isDeregisterWithVoPSNeeded [" + result + "]");
        return result;
    }

    /* access modifiers changed from: protected */
    public void setDelayedDeregisterTimerRunning(boolean value) {
        Log.i(LOG_TAG, "setDelayedDeregisterTimerRunning [" + value + "]");
        this.mDelayedDeregisterTimerRunning = value;
        this.mRegMan.setDelayedDeregisterTimerRunning(this.mTask, this.mDelayedDeregisterTimerRunning);
    }

    /* access modifiers changed from: protected */
    public void runDelayedDeregister() {
        if (isDelayedDeregisterTimerRunning()) {
            Log.i(LOG_TAG, "runDelayedDeregister : delete DelayedDeregisterTimer. mState [" + this.mTask.getState() + "]");
            setDelayedDeregisterTimerRunning(false);
            if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                this.mRegMan.addPendingUpdateRegistration(this.mTask, 0);
            } else if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTED) {
                this.mRegHandler.sendTryRegister(this.mTask.getPhoneId());
            } else if (isDeregisterWithVoPSNeeded() || isDeregisterWithRATNeeded()) {
                this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            } else {
                this.mRegHandler.sendTryRegister(this.mTask.getPhoneId());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDelayedDeregisterTimerRunningWithCallStatus() {
        if (this.mRegMan.getTelephonyCallStatus(this.mPhoneId) != 0 || getCallStatus() == 0 || (!isDeregisterWithVoPSNeeded() && !isDeregisterWithRATNeeded() && !this.mRegMan.getNetworkEvent(this.mPhoneId).outOfService)) {
            Log.i("RegiGvnBase[" + this.mPhoneId + "]", "isDelayedDeregisterTimerRunning [" + this.mDelayedDeregisterTimerRunning + "]");
            return this.mDelayedDeregisterTimerRunning;
        }
        Log.i("RegiGvnBase[" + this.mPhoneId + "]", "isDelayedDeregisterTimerRunning: Timer will start soon. return true.");
        return true;
    }

    public void onTelephonyCallStatusChanged(int callState) {
        setCallStatus(callState);
    }

    public void onPdnRequestFailed(String reason) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "onPdnRequestFailed: " + reason);
        this.mHasPdnFailure = true;
    }

    public String toString() {
        return "RegiGvnBase [mMno=" + this.mMno + ", mFailureCounter=" + this.mFailureCounter + ", mIsPermanentStopped=" + this.mIsPermanentStopped + ", mCurPcscfIpIdx=" + this.mCurPcscfIpIdx + ", mNumOfPcscfIp=" + this.mNumOfPcscfIp + ", mCurImpu=" + this.mCurImpu + ", mPcscfIpList=" + this.mPcscfIpList + ", mIsValid=" + this.mIsValid + ", mIPsecAllow=" + this.mIPsecAllow + ", mMoveToNextPcscfAfterTimerB=" + this.mMoveToNextPcscfAfterTimerB + ", mRegiAt=" + this.mRegiAt + ", mHasVoLteCall=" + this.mHasVoLteCall + ",  mRegBaseTime=" + this.mRegBaseTime + ", mRegMaxTime=" + this.mRegMaxTime + "]";
    }

    public boolean isWiFiSettingOn() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            return false;
        }
        boolean isWifiOn = wifiManager.isWifiEnabled();
        Log.i(LOG_TAG, "WifiManager.isWifiEnabled() : " + isWifiOn);
        return isWifiOn;
    }

    public boolean isSrvccCase() {
        int network = this.mRegMan.getNetworkEvent(this.mPhoneId).network;
        if (this.mNeedToCheckSrvcc && this.mTask.getRegistrationRat() == 13 && (TelephonyManagerExt.getNetworkClass(network) == 1 || TelephonyManagerExt.getNetworkClass(network) == 2)) {
            return true;
        }
        return false;
    }

    public boolean isEcEnabled(int phoneId) {
        return RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, phoneId)).booleanValue() || RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, phoneId)).booleanValue() || RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, phoneId)).booleanValue();
    }

    /* access modifiers changed from: protected */
    public Set<String> applyImsSwitch(Set<String> enabledServices, int network) {
        Set<String> set = enabledServices;
        int i = network;
        if (set == null) {
            return new HashSet();
        }
        ISimManager sm = this.mRegMan.getSimManager(this.mPhoneId);
        if (sm == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "applyImsSwitch: sm is null!! retrun empty set");
            return new HashSet();
        }
        boolean isLabSimCard = sm.isLabSimCard();
        boolean isSimMoblity = this.mTask.getProfile().getSimMobility();
        if (!"GCF".equalsIgnoreCase(OmcCode.get()) && !isLabSimCard && !isSimMoblity && this.mTask.getProfile().getPdnType() == 11) {
            ContentValues mnoInfo = sm.getMnoInfo();
            boolean isEnableVoLTE = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false);
            boolean isEnableVoWIFI = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false);
            boolean isEnableSMSIp = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, false);
            boolean isEnableViLTE = CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, false);
            if (!isEnableVoLTE && i != 18) {
                removeService(set, "mmtel", "VoLTE MPS false");
                removeService(set, "mmtel-video", "VoLTE MPS false");
                removeService(set, "smsip", "VoLTE MPS false");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_MPS_DISABLED.getCode());
            }
            if (!isEnableVoWIFI && i == 18) {
                removeService(set, "mmtel", "Vowifi MPS false");
                removeService(set, "mmtel-video", "Vowifi MPS false");
                removeService(set, "smsip", "Vowifi MPS false");
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_MPS_DISABLED.getCode());
            }
            if (!isEnableSMSIp) {
                removeService(set, "smsip", "SMSIP MPS false");
            }
            if (!isEnableViLTE) {
                removeService(set, "mmtel-video", "Enable ViLTE MPS false");
            }
        }
        return set;
    }

    public boolean isNoNextPcscf() {
        boolean z = true;
        if (this.mCurPcscfIpIdx + 1 < this.mNumOfPcscfIp) {
            z = false;
        }
        boolean ret = z;
        int phoneId = this.mTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "isNoNextPcscf = " + ret);
        return ret;
    }

    public void increasePcscfIdx() {
        if (this.mNumOfPcscfIp > 0) {
            this.mCurPcscfIpIdx = (this.mCurPcscfIpIdx + 1) % this.mNumOfPcscfIp;
        }
        int phoneId = this.mTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "increasePcscfIdx: now [" + this.mCurPcscfIpIdx + "]");
    }

    public String[] getPdnFailureReasons() {
        String[] reasons = ImsRegistry.getStringArray(this.mTask.getPhoneId(), GlobalSettingsConstants.Registration.PDN_FAIL_REASON_LIST, new String[0]);
        IMSLog.i(LOG_TAG, "getPdnFailureReasons : " + Arrays.asList(reasons));
        return reasons;
    }

    public boolean isMatchedPdnFailReason(String[] pdnFailureReasons, String failReasonByNetwork) {
        int length = pdnFailureReasons.length;
        int i = 0;
        while (i < length) {
            String failReason = pdnFailureReasons[i];
            if (!TextUtils.isEmpty(failReason)) {
                failReason = failReason.replace("\"", "");
            }
            if (TextUtils.isEmpty(failReason) || !failReason.equals(failReasonByNetwork)) {
                i++;
            } else {
                Log.i(LOG_TAG, "match with " + failReasonByNetwork);
                return true;
            }
        }
        return false;
    }

    public String getMatchedPdnFailReason(String[] pdnFailReasonList, String failReasonByNetwork) {
        Log.i(LOG_TAG, "getMatchedPdnFailReason :" + failReasonByNetwork);
        int length = pdnFailReasonList.length;
        for (int i = 0; i < length; i++) {
            String failReason = pdnFailReasonList[i];
            if (!TextUtils.isEmpty(failReason)) {
                failReason = failReason.replace("\"", "");
            }
            if (failReason.contains(failReasonByNetwork)) {
                return failReason;
            }
        }
        return "";
    }

    private static Object getITelephonyExt(Context context) {
        try {
            Method method = Class.forName(TelephonyManager.class.getName()).getDeclaredMethod("getITelephony", new Class[0]);
            method.setAccessible(true);
            return method.invoke(context.getSystemService(PhoneConstants.PHONE_KEY), new Object[0]);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    /* access modifiers changed from: protected */
    public boolean sendRawRequestToTelephony(Context context, byte[] cmd) {
        Class cls = byte[].class;
        Object ITelephonyExtInstance = getITelephonyExt(context);
        byte[] resp = new byte[4];
        try {
            ITelephonyExtInstance.getClass().getMethod("invokeOemRilRequestRaw", new Class[]{cls, cls}).invoke(ITelephonyExtInstance, new Object[]{cmd, resp});
            return true;
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | NullPointerException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean sendRawRequestToTelephony2(byte[] cmd, int phoneId) {
        byte[] resp = new byte[4];
        int subId = SimUtil.getSubId(phoneId);
        try {
            ISemTelephony semTelephony = ISemTelephony.Stub.asInterface(ServiceManager.getService("isemtelephony"));
            if (semTelephony == null) {
                Log.e(LOG_TAG, "Unable to find ISemTelephony interface.");
                return false;
            }
            semTelephony.invokeOemRilRequestRawForSubscriber(subId, cmd, resp);
            return true;
        } catch (RemoteException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public RegistrationConstants.RegisterTaskState getState() {
        return this.mTask.getState();
    }

    /* access modifiers changed from: protected */
    public void setUpsmEventReceiver() {
        Log.i(LOG_TAG, "setUpsmEventReceiver.");
        IntentFilter upsmIntentFilter = new IntentFilter();
        upsmIntentFilter.addAction("com.samsung.intent.action.EMERGENCY_STATE_CHANGED");
        upsmIntentFilter.addAction(SemEmergencyConstantsExt.EMERGENCY_CHECK_ABNORMAL_STATE);
        upsmIntentFilter.addAction("com.samsung.intent.action.EMERGENCY_START_SERVICE_BY_ORDER");
        this.mContext.registerReceiver(this.mUpsmEventReceiver, upsmIntentFilter);
    }

    public void unRegisterIntentReceiver() {
        Log.i(LOG_TAG, "Un-register Intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mUpsmEventReceiver);
            this.mContext.unregisterReceiver(this.mPackageDataClearedIntentReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    /* access modifiers changed from: protected */
    public int onUltraPowerSavingModeChanged() {
        Log.i(LOG_TAG, "onUltraPowerSavingModeChanged.");
        SemEmergencyManager emergencyManager = SemEmergencyManager.getInstance(this.mContext);
        Log.i(LOG_TAG, "Emergency is " + SemEmergencyManager.isEmergencyMode(this.mContext));
        Log.i(LOG_TAG, "UPSM is " + SystemUtil.checkUltraPowerSavingMode(emergencyManager));
        if (!SemEmergencyManager.isEmergencyMode(this.mContext) || !SystemUtil.checkUltraPowerSavingMode(emergencyManager)) {
            if (!this.mUpsmEnabled) {
                return -1;
            }
            Log.i(LOG_TAG, "EM is disabled");
            this.mUpsmEnabled = false;
            if (isThrottled()) {
                releaseThrottle(0);
            }
            return 1;
        } else if (this.mUpsmEnabled) {
            Log.i(LOG_TAG, "EM is already enabled, so skip.");
            return -1;
        } else {
            Log.i(LOG_TAG, "EM is enabled");
            this.mUpsmEnabled = true;
            if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                return 0;
            }
            return -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0036, code lost:
        if (r0.isOneOf(com.sec.internal.constants.Mno.TELSTRA, com.sec.internal.constants.Mno.TELENOR_NORWAY, com.sec.internal.constants.Mno.TELIA_NORWAY, com.sec.internal.constants.Mno.RJIL, com.sec.internal.constants.Mno.EE, com.sec.internal.constants.Mno.EE_ESN, com.sec.internal.constants.Mno.KDDI) != false) goto L_0x0038;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkEmergencyInProgress() {
        /*
            r7 = this;
            com.sec.internal.ims.core.RegisterTask r0 = r7.mTask
            com.sec.internal.constants.Mno r0 = r0.getMno()
            boolean r1 = r0.isCanada()
            r2 = 2
            r3 = 1
            r4 = 0
            if (r1 != 0) goto L_0x0038
            r1 = 7
            com.sec.internal.constants.Mno[] r1 = new com.sec.internal.constants.Mno[r1]
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TELSTRA
            r1[r4] = r5
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TELENOR_NORWAY
            r1[r3] = r5
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TELIA_NORWAY
            r1[r2] = r5
            r5 = 3
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.RJIL
            r1[r5] = r6
            r5 = 4
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.EE
            r1[r5] = r6
            r5 = 5
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.EE_ESN
            r1[r5] = r6
            r5 = 6
            com.sec.internal.constants.Mno r6 = com.sec.internal.constants.Mno.KDDI
            r1[r5] = r6
            boolean r1 = r0.isOneOf(r1)
            if (r1 == 0) goto L_0x0057
        L_0x0038:
            com.sec.internal.ims.core.RegisterTask r1 = r7.mTask
            com.sec.ims.settings.ImsProfile r1 = r1.getProfile()
            boolean r1 = r1.hasEmergencySupport()
            if (r1 == 0) goto L_0x0057
            com.sec.internal.ims.core.RegisterTask r1 = r7.mTask
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r2 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[r2]
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            r2[r4] = r5
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.DEREGISTERING
            r2[r3] = r5
            boolean r1 = r1.isOneOf(r2)
            if (r1 == 0) goto L_0x0057
            return r3
        L_0x0057:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorBase.checkEmergencyInProgress():boolean");
    }

    public boolean isReadyToDualRegister(boolean isCmcDualRegi) {
        if (!SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS())) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - Non DSDS_DI");
            return true;
        }
        int otherSlotIndex = SimUtil.getOppositeSimSlot(this.mPhoneId);
        boolean otherSlotRegistering = false;
        boolean otherSlotConnecting = false;
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(otherSlotIndex).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RegisterTask taskOnOpposite = (RegisterTask) it.next();
            if (!taskOnOpposite.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                if (!taskOnOpposite.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    if (taskOnOpposite.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING) && taskOnOpposite.getPdnType() == 11 && !this.mRegMan.getNetworkEvent(otherSlotIndex).outOfService && this.mRegMan.getNetworkEvent(otherSlotIndex).network != 18 && this.mTask.getPdnType() == 11 && !taskOnOpposite.getGovernor().hasPdnFailure()) {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister, other slot is connecting IMS PDN");
                        otherSlotConnecting = true;
                        break;
                    }
                } else if (taskOnOpposite.isEpdgHandoverInProgress() && !this.mRegMan.getNetworkEvent(otherSlotIndex).isEpdgConnected && taskOnOpposite.mIsUpdateRegistering) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : false, other slot is re-registering for W2L handover");
                    return false;
                }
            } else {
                int i = this.mPhoneId;
                IMSLog.i(LOG_TAG, i, "isReadyToDualRegister : other slot " + taskOnOpposite.getState());
                otherSlotRegistering = true;
                break;
            }
        }
        boolean otherSlotDuringCall = false;
        if (this.mTelephonyManager.getCallState(otherSlotIndex) != 0) {
            otherSlotDuringCall = true;
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : other slot is on Calling");
        }
        if (otherSlotRegistering || otherSlotDuringCall || otherSlotConnecting) {
            return isReadyToDualRegisterOnOtherSlotBusy(otherSlotIndex, isCmcDualRegi);
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true");
        return true;
    }

    private boolean isReadyToDualRegisterOnOtherSlotBusy(int otherSlotIndex, boolean isCmcDualRegi) {
        if (this.mTelephonyManager.getDataNetworkType(SimUtil.getSubId(otherSlotIndex)) == 18 && this.mPdnController.isEpdgConnected(otherSlotIndex)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - other slot is EPDG Call or Registering");
            return true;
        } else if (this.mTask.getProfile().hasEmergencySupport()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - The slot will make E911 call");
            return true;
        } else if (this.mRegMan.findBestNetwork(this.mPhoneId, this.mTask.getProfile(), this) == 18 && !isCmcDualRegi) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - This slot is going to register VoWifi");
            return true;
        } else if (this.mTask.isRcsOnly()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : true - This slot is going to register RCS only profile");
            return true;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToDualRegister : false");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> services, int network) {
        if (services == null) {
            return new HashSet();
        }
        if (getVoiceTechType() == 0 || this.mTask.getRegistrationRat() == 18) {
            return services;
        }
        Log.i(LOG_TAG, "by VoLTE OFF, remove all service, RAT :" + this.mTask.getRegistrationRat());
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return new HashSet();
    }

    public boolean isNeedToPendingUpdateRegistration(int rat, boolean oos, boolean isForceReRegi, boolean immediately) {
        boolean needPending = false;
        int pdnType = this.mTask.getProfile().getPdnType();
        if (!immediately && !isReadyToRegister(rat)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToPendingUpdateRegistration: Not ready to register");
            needPending = true;
        } else if (!isSrvccCase() && this.mTask.isSuspended()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToPendingUpdateRegistration: suspended and not SRVCC");
            needPending = true;
        } else if (oos) {
            if (pdnType == 11 || pdnType == 15) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToPendingUpdateRegistration: Out Of Service");
                needPending = true;
            } else if (!this.mPdnController.isWifiConnected()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToPendingUpdateRegistration: WiFi is not connected");
                needPending = true;
            }
        }
        if (needPending) {
            if (isForceReRegi) {
                this.mTask.mHasForcedPendingUpdate = true;
            } else {
                this.mTask.mHasPendingUpdate = true;
            }
            this.mTask.setImmediatePendingUpdate(immediately);
            if (this.mTask.isEpdgHandoverInProgress()) {
                this.mTask.setHasPendingEpdgHandover(true);
            }
        }
        return needPending;
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "isNeedToDeRegistration:");
        boolean isInCall = this.mTelephonyManager.getCallState() != 0;
        if (foundBestRat == 0) {
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "isNeedToDeRegistration: no IMS service for network " + currentRat + ". Deregister.");
            RegisterTask registerTask = this.mTask;
            registerTask.setReason("no IMS service for network : " + currentRat);
            this.mTask.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
            return true;
        } else if (RegistrationUtils.supportCsTty(this.mTask) && SlotBasedConfig.getInstance(this.mPhoneId).getTTYMode() && !isInCall) {
            this.mTask.setReason("TTY enabled");
            this.mTask.setDeregiReason(75);
            this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
            return true;
        } else if (!ConfigUtil.isRcsEur(this.mMno) || !this.mTask.isRcsOnly() || this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED || foundBestRat != 18 || this.mTask.getRegistrationRat() == 18) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "determineDeRegistration:  WiFi is connected.");
            this.mTask.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(this.mTask, true, false);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateGeolocation(String countryCode) {
        boolean needNotify = false;
        if (!TextUtils.isEmpty(countryCode) && !countryCode.equalsIgnoreCase(this.mCountry) && this.mTask.getProfile().getSupportedGeolocationPhase() >= 1) {
            if (isThrottled()) {
                releaseThrottle(6);
            }
            needNotify = true;
        }
        int phoneId = this.mTask.getPhoneId();
        NetworkEvent ne = this.mRegMan.getNetworkEvent(phoneId);
        if ((!TextUtils.isEmpty(countryCode) && !countryCode.equalsIgnoreCase(this.mCountry)) || (TextUtils.isEmpty(countryCode) && !TextUtils.isEmpty(this.mCountry))) {
            if (this.mTask.getProfile().getPdnType() == 11 && this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && !this.mTask.mIsUpdateRegistering && ne.isEpdgConnected) {
                IMSLog.i(LOG_TAG, phoneId, "updateRegistration as Country Code change");
                this.mRegMan.updatePani(phoneId);
                this.mTask.setReason("update location");
                this.mRegMan.updateRegistration(this.mTask, true, false);
            }
            this.mCountry = countryCode;
        }
        return needNotify;
    }

    public RegisterTask onManualDeregister(boolean isExplicit) {
        int phoneId = this.mTask.getPhoneId();
        ImsProfile profile = this.mTask.getProfile();
        boolean z = true;
        if (this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            boolean isEmergency = profile.hasEmergencySupport();
            IMSLog.i(LOG_TAG, phoneId, "onManualDeregister: emergency: " + isEmergency + ", explicit dereg: " + isExplicit);
            this.mTask.setReason("manual deregi");
            this.mTask.setDeregiReason(22);
            RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
            RegisterTask registerTask = this.mTask;
            if (isExplicit && !isEmergency) {
                z = false;
            }
            registrationManagerInternal.tryDeregisterInternal(registerTask, z, false);
            return null;
        }
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
            IMSLog.i(LOG_TAG, phoneId, "onManualDeregister: disconnecting PDN network " + this.mTask.getPdnType());
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            this.mTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            if (profile.hasEmergencySupport()) {
                return this.mTask;
            }
            return null;
        }
        if (this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.EMERGENCY) || (this.mTask.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING && this.mTask.getUserAgent() == null)) {
            IMSLog.i(LOG_TAG, phoneId, "onManualDeregister: disconnect Emergency PDN.");
            this.mTask.setReason("manual deregi(EPDN)");
            this.mTask.setDeregiReason(30);
            if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                this.mRegMan.tryDeregisterInternal(this.mTask, true, false);
            }
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            if (this.mTask.getState() != RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                return this.mTask;
            }
            if (this.mTask.needKeepEmergencyTask()) {
                this.mTask.keepEmergencyTask(false);
                return null;
            } else if (!RegistrationUtils.isCmcProfile(this.mTask.getProfile())) {
                return this.mTask;
            } else {
                return null;
            }
        } else {
            if (!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED, RegistrationConstants.RegisterTaskState.CONNECTED) || !SlotBasedConfig.getInstance(phoneId).getExtendedProfiles().containsKey(Integer.valueOf(profile.getId()))) {
                return null;
            }
            this.mRegMan.stopPdnConnectivity(this.mTask.getPdnType(), this.mTask);
            return this.mTask;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x003a, code lost:
        if (r6.mMno.isOneOf(com.sec.internal.constants.Mno.OPTUS, com.sec.internal.constants.Mno.TELSTRA, com.sec.internal.constants.Mno.TELIA_NORWAY, com.sec.internal.constants.Mno.EE, com.sec.internal.constants.Mno.EE_ESN, com.sec.internal.constants.Mno.CTC, com.sec.internal.constants.Mno.CTCMO, com.sec.internal.constants.Mno.CHT) != false) goto L_0x003c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasEmergencyTaskInPriority(java.util.List<? extends com.sec.internal.interfaces.ims.core.IRegisterTask> r7) {
        /*
            r6 = this;
            com.sec.internal.constants.Mno r0 = r6.mMno
            boolean r0 = r0.isCanada()
            r1 = 1
            r2 = 0
            if (r0 != 0) goto L_0x003c
            com.sec.internal.constants.Mno r0 = r6.mMno
            r3 = 8
            com.sec.internal.constants.Mno[] r3 = new com.sec.internal.constants.Mno[r3]
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.OPTUS
            r3[r2] = r4
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TELSTRA
            r3[r1] = r4
            r4 = 2
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TELIA_NORWAY
            r3[r4] = r5
            r4 = 3
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.EE
            r3[r4] = r5
            r4 = 4
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.EE_ESN
            r3[r4] = r5
            r4 = 5
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CTC
            r3[r4] = r5
            r4 = 6
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CTCMO
            r3[r4] = r5
            r4 = 7
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.CHT
            r3[r4] = r5
            boolean r0 = r0.isOneOf(r3)
            if (r0 == 0) goto L_0x0055
        L_0x003c:
            java.util.stream.Stream r0 = r7.stream()
            com.sec.internal.ims.core.-$$Lambda$RegistrationGovernorBase$a5gv-AEjM-1bVKmnv-fLX-dmbdQ r3 = com.sec.internal.ims.core.$$Lambda$RegistrationGovernorBase$a5gvAEjM1bVKmnvfLXdmbdQ.INSTANCE
            java.util.stream.Stream r0 = r0.filter(r3)
            com.sec.internal.ims.core.-$$Lambda$-M3aFGwHLqIk3rVMHxC5mm0IEws r3 = com.sec.internal.ims.core.$$Lambda$M3aFGwHLqIk3rVMHxC5mm0IEws.INSTANCE
            java.util.stream.Stream r0 = r0.map(r3)
            com.sec.internal.ims.core.-$$Lambda$s8c69XYNgDxHzmtOH8_cwiDgYsI r3 = com.sec.internal.ims.core.$$Lambda$s8c69XYNgDxHzmtOH8_cwiDgYsI.INSTANCE
            boolean r0 = r0.anyMatch(r3)
            if (r0 == 0) goto L_0x0055
            goto L_0x0056
        L_0x0055:
            r1 = r2
        L_0x0056:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationGovernorBase.hasEmergencyTaskInPriority(java.util.List):boolean");
    }

    public boolean needPendingPdnConnected() {
        ImsProfile profile = this.mTask.getProfile();
        if (profile.hasEmergencySupport() || !hasEmergencyTaskInPriority(RegistrationUtils.getPendingRegistrationInternal(this.mPhoneId))) {
            return false;
        }
        RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(22, this.mTask), 500);
        Log.i(LOG_TAG, "onPdnConnected: delay " + profile.getName() + " due to priority of Emergency.");
        return true;
    }
}
