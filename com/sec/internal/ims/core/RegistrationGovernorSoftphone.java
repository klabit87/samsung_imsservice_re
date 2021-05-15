package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.HashSet;
import java.util.Set;

public class RegistrationGovernorSoftphone extends RegistrationGovernorAtt {
    protected static final String INTENT_ACTION_WIFI_MODE_CHANGED = "com.samsung.android.net.wifi.SEC_NETWORK_STATE_CHANGED";
    private static final String LOG_TAG = "RegiGvnSoftp";
    protected int mForbiddenCount = 0;
    protected final int[] mForbiddenRetryTime = {17, 34, 68};
    protected final ShutdownEventReceiver mShutdownEventReceiver = new ShutdownEventReceiver();
    protected final WifiEventReceiver mWifiEventReceiver = new WifiEventReceiver();

    public RegistrationGovernorSoftphone(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        Log.i(LOG_TAG, "Register : ShutdownEventReceiver");
        IntentFilter shutdownIntentFilter = new IntentFilter();
        shutdownIntentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        context.registerReceiver(this.mShutdownEventReceiver, shutdownIntentFilter);
        IntentFilter wifiEventIntentFilter = new IntentFilter();
        wifiEventIntentFilter.addAction(INTENT_ACTION_WIFI_MODE_CHANGED);
        context.registerReceiver(this.mWifiEventReceiver, wifiEventIntentFilter);
    }

    public void unRegisterIntentReceiver() {
        Log.i(LOG_TAG, "Un-register intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mShutdownEventReceiver);
            this.mContext.unregisterReceiver(this.mWifiEventReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        this.mForbiddenCount = 0;
        if (this.mPdnController.isWifiConnected()) {
            sendDelayMsgToWifi(true);
        } else {
            sendDelayMsgToWifi(false);
        }
    }

    public void onDeregistrationDone(boolean requested) {
        sendDelayMsgToWifi(false);
    }

    protected class ShutdownEventReceiver extends BroadcastReceiver {
        protected ShutdownEventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            RegistrationGovernorSoftphone.this.mIsPermanentStopped = true;
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction()) && RegistrationGovernorSoftphone.this.mPdnController.isWifiConnected()) {
                RegistrationGovernorSoftphone.this.mRegMan.sendDeregister(13);
                while (true) {
                    if (RegistrationGovernorSoftphone.this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || RegistrationGovernorSoftphone.this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || RegistrationGovernorSoftphone.this.mTask.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Log.e(RegistrationGovernorSoftphone.LOG_TAG, "Sleep exception : " + e);
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }

    protected class WifiEventReceiver extends BroadcastReceiver {
        protected WifiEventReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (RegistrationGovernorSoftphone.INTENT_ACTION_WIFI_MODE_CHANGED.equals(intent.getAction())) {
                Log.i(RegistrationGovernorSoftphone.LOG_TAG, "WifiEventReceiver: INTENT_ACTION_WIFI_MODE_CHANGED Received.");
                if (intent.getExtras().getBoolean("delayState", false)) {
                    RegistrationGovernorSoftphone.this.mRegMan.sendDeregister(13);
                    Log.i(RegistrationGovernorSoftphone.LOG_TAG, "WifiEventReceiver: send Deregister message.");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkCallStatus() {
        if (this.mRegMan.getTelephonyCallStatus(this.mTask.getPhoneId()) != 0 || this.mTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
            return false;
        }
        if (!this.mTask.mIsUpdateRegistering) {
            return true;
        }
        Log.i(LOG_TAG, "isReadyToRegister: Task State is UpdateRegistering");
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || checkCallStatus();
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices = new HashSet<>();
        boolean isVideoSettingsOn = true;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mTask.getPhoneId()) == 1;
        boolean isVoLteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mTask.getPhoneId()) == 1;
        if (ImsConstants.SystemSettings.VILTE_SLOT1.get(this.mContext, -1) != 0) {
            isVideoSettingsOn = false;
        }
        if (services != null) {
            filteredServices.addAll(services);
        }
        if (!isImsEnabled) {
            Log.i(LOG_TAG, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (!isVoLteEnabled) {
            for (String service : ImsProfile.getVoLteServiceList()) {
                removeService(filteredServices, service, "VoLTE disabled");
            }
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
        }
        if (!isVideoSettingsOn || (network != 18 && !NetworkUtil.isMobileDataOn(this.mContext))) {
            removeService(filteredServices, "mmtel-video", "MobileData or Setting off");
        }
        if (!this.mConfigModule.isValidAcsVersion(this.mTask.getPhoneId())) {
            for (String service2 : ImsProfile.getRcsServiceList()) {
                removeService(filteredServices, service2, "RCS disabled.");
            }
        }
        return filteredServices;
    }

    public void onRegistrationError(SipError error, int retryAfter, boolean unsolicit) {
        Log.i(LOG_TAG, "onRegistrationError: state " + this.mTask.getState() + " error " + error + " retryAfter " + retryAfter + " mCurPcscfIpIdx " + this.mCurPcscfIpIdx + " mNumOfPcscfIp " + this.mNumOfPcscfIp + " mFailureCounter " + this.mFailureCounter + " mIsPermanentStopped " + this.mIsPermanentStopped);
        if (retryAfter < 0) {
            retryAfter = 0;
        }
        if (SipErrorBase.isImsForbiddenError(error)) {
            Log.i(LOG_TAG, "403 response : " + this.mForbiddenCount);
            int i = this.mForbiddenCount;
            if (i >= 3) {
                Log.i(LOG_TAG, "got 403 response over 3 times...");
                this.mIsPermanentStopped = true;
                return;
            }
            int[] iArr = this.mForbiddenRetryTime;
            this.mForbiddenCount = i + 1;
            int retryAfter2 = iArr[i];
            if (retryAfter2 > 0) {
                this.mRegiAt = SystemClock.elapsedRealtime() + (((long) retryAfter2) * 1000);
                startRetryTimer(((long) retryAfter2) * 1000);
                return;
            }
            return;
        }
        super.onRegistrationError(error, retryAfter, unsolicit);
    }

    public SipError onSipError(String service, SipError error) {
        Log.i(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if ("mmtel".equals(service) && (SipErrorBase.SIP_INVITE_TIMEOUT.equals(error) || SipErrorBase.SIP_TIMEOUT.equals(error) || SipErrorBase.FORBIDDEN.equals(error) || SipErrorBase.SERVER_TIMEOUT.equals(error))) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, false, this.mIsValid, "Sip Error[MMTEL]. DeRegister..");
        }
        if (SipErrorBase.PROXY_AUTHENTICATION_REQUIRED.equals(error) || SipErrorBase.SERVICE_UNAVAILABLE.equals(error)) {
            this.mTask.setDeregiReason(43);
            this.mRegMan.deregister(this.mTask, false, this.mIsValid, "Sip Error 407 or 503. DeRegister..");
        }
        return error;
    }

    public void onPublishError(SipError error) {
        if (SipErrorBase.FORBIDDEN.equals(error)) {
            this.mTask.setDeregiReason(45);
            this.mRegMan.deregister(this.mTask, false, true, "publish error");
        }
    }

    public void releaseThrottle(int releaseCase) {
        if (releaseCase == 0) {
            this.mIsPermanentStopped = false;
        } else if (releaseCase == 1) {
            Log.i(LOG_TAG, "releaseThrottle: sendDeregister");
            this.mRegMan.sendDeregister(12);
            this.mIsPermanentStopped = false;
            this.mFailureCounter = 0;
            this.mRegiAt = 0;
            this.mForbiddenCount = 0;
            stopRetryTimer();
        }
        if (!this.mIsPermanentStopped) {
            Log.i(LOG_TAG, "releaseThrottle: case by " + releaseCase);
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceTechType() {
        return 0;
    }

    private void sendDelayMsgToWifi(boolean enable) {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            Message msg = Message.obtain((Handler) null, WiFiManagerExt.SEC_COMMAND_ID_DELAY_DISCONNECT_TRANSITION);
            msg.arg1 = enable;
            msg.arg2 = enable ? 10000 : 0;
            WiFiManagerExt.callSECApi(wifiManager, msg);
            Log.i(LOG_TAG, "Notify to WiFiManager");
        }
    }

    public boolean determineDeRegistration(int foundBestRat, int currentRat) {
        int regiRat = this.mTask.getRegistrationRat();
        if ((regiRat == 18 || foundBestRat != 18) && (regiRat != 18 || foundBestRat == 18)) {
            return super.determineDeRegistration(foundBestRat, currentRat);
        }
        this.mTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(this.mTask, false, false);
        return true;
    }
}
