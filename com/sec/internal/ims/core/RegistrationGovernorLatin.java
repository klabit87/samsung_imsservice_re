package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorLatin extends RegistrationGovernorBase {
    private static final String INTENT_ACTION_IMS_DEREGISTERED = "com.sec.imsservice.action.IMS_DEREGISTERED";
    private static final String INTENT_ACTION_IMS_REGISTERED = "com.sec.imsservice.action.IMS_REGISTERED";
    protected static final String INTENT_ACTION_SETUPWIZARD_COMPLETE = "com.sec.android.app.secsetupwizard.SETUPWIZARD_COMPLETE";
    protected static final String INTENT_RCS_REGISTRATION = "com.samsung.android.messaging.action.REQUEST_RCS_REGISTRATION";
    private static final String LOG_TAG = "RegiGvnLatin";
    protected BroadcastReceiver mIntentReceiverLatin = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(RegistrationGovernorLatin.LOG_TAG, "intent = " + intent.getAction());
            if (RegistrationGovernorLatin.INTENT_ACTION_SETUPWIZARD_COMPLETE.equals(intent.getAction())) {
                Log.i(RegistrationGovernorLatin.LOG_TAG, "Try register after setupwizard is completed");
                RegistrationGovernorLatin.this.mRegHandler.sendTryRegister(RegistrationGovernorLatin.this.mPhoneId);
            } else if (RegistrationGovernorLatin.INTENT_RCS_REGISTRATION.equals(intent.getAction())) {
                Log.i(RegistrationGovernorLatin.LOG_TAG, "Try register when user trigger rcs registration on MSG app");
                RegistrationGovernorLatin.this.mConfigModule.getAcsConfig(RegistrationGovernorLatin.this.mPhoneId).setAcsCompleteStatus(false);
                RegistrationGovernorLatin.this.mRegHandler.sendTryRegister(RegistrationGovernorLatin.this.mPhoneId);
            }
        }
    };
    protected String mRegisteredNetworkType = null;

    public RegistrationGovernorLatin(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_SETUPWIZARD_COMPLETE);
        filter.addAction(INTENT_RCS_REGISTRATION);
        this.mContext.registerReceiver(this.mIntentReceiverLatin, filter);
        this.mHandlePcscfOnAlternativeCall = true;
    }

    RegistrationGovernorLatin(Context context) {
        this.mContext = context;
    }

    public void unRegisterIntentReceiver() {
        Log.i(LOG_TAG, "Un-register Intent receiver(s)");
        try {
            this.mContext.unregisterReceiver(this.mIntentReceiverLatin);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "unRegisterIntentReceiver: Receiver not registered!");
        }
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

    public boolean isReadyToRegister(int rat) {
        if (this.mTask.isRcsOnly()) {
            if (RcsConfigurationHelper.getConfigData(this.mContext, "root/vers/*", this.mPhoneId).readInt("version", -1).intValue() <= 0 && ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == -1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: User don't try RCS service yet");
                return false;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) != 1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "isReadyToRegister: Default MSG app isn't used for RCS");
                return false;
            }
        }
        return super.isReadyToRegister(rat);
    }

    public void onDeregistrationDone(boolean requested) {
        super.onDeregistrationDone(requested);
        if (this.mMno == Mno.VIVO_BRAZIL && this.mTask.getProfile().getPdnType() == 11) {
            Intent intent = new Intent(INTENT_ACTION_IMS_DEREGISTERED);
            if (!TextUtils.isEmpty(this.mRegisteredNetworkType)) {
                intent.putExtra("rat", this.mRegisteredNetworkType);
                this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
                Log.i(LOG_TAG, "Broadcast intent: " + intent.getAction() + ", rat [" + intent.getStringExtra("rat") + "]");
                this.mRegisteredNetworkType = null;
            }
        }
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> enabledServices = new HashSet<>();
        Set<String> filteredServices = new HashSet<>(services);
        boolean isVoLteEnabled = true;
        boolean isImsEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.IMS, this.mPhoneId) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", this.mPhoneId) != 1) {
            isVoLteEnabled = false;
        }
        if (!isImsEnabled) {
            Log.i(LOG_TAG, "filterEnabledCoreService: IMS is disabled.");
            this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF.getCode());
            return new HashSet();
        }
        if (isVoLteEnabled) {
            enabledServices.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch(ImsProfile.getVoLteServiceList()).toArray(new String[0])));
            if (!enabledServices.contains("mmtel")) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NO_MMTEL_IMS_SWITCH_OFF.getCode());
            }
        }
        if (this.mConfigModule.isValidAcsVersion(this.mPhoneId)) {
            enabledServices.addAll(servicesByReadSwitch((String[]) servicesByImsSwitch(ImsProfile.getRcsServiceList()).toArray(new String[0])));
            List<String> enabledRcsServices = RcsConfigurationHelper.getRcsEnabledServiceList(this.mContext, this.mPhoneId, ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, this.mTask.getProfile()));
            for (String service : ImsProfile.getRcsServiceList()) {
                if (!enabledRcsServices.contains(service)) {
                    removeService(filteredServices, service, "Disabled from ACS");
                }
            }
        }
        if (network == 13 && this.mTask.getProfile().getPdnType() == 11) {
            enabledServices = applyVoPsPolicy(enabledServices);
            if (enabledServices.isEmpty()) {
                this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.VOPS_OFF.getCode());
                return enabledServices;
            }
        } else if (!NetworkUtil.isMobileDataOn(this.mContext) && network != 18) {
            Log.i(LOG_TAG, "Mobile off!");
            if (this.mTask.getProfile().hasService("im") || this.mTask.getProfile().hasService("ft")) {
                return new HashSet();
            }
        }
        Set<String> filteredServices2 = applyMmtelUserSettings(filteredServices, network);
        if (!filteredServices2.isEmpty()) {
            filteredServices2.retainAll(enabledServices);
        }
        return filteredServices2;
    }

    public boolean isLocationInfoLoaded(int rat) {
        Log.i(LOG_TAG, "Latin operator allow registration even without geo-location");
        return true;
    }

    public void onRegistrationDone() {
        super.onRegistrationDone();
        if (this.mMno == Mno.VIVO_BRAZIL && this.mTask.getProfile().getPdnType() == 11) {
            Intent intent = new Intent(INTENT_ACTION_IMS_REGISTERED);
            if (this.mRegMan.getCurrentNetworkByPhoneId(this.mPhoneId) == 18) {
                this.mRegisteredNetworkType = "wifi";
            } else {
                this.mRegisteredNetworkType = "mobile";
            }
            intent.putExtra("rat", this.mRegisteredNetworkType);
            this.mContext.sendBroadcast(intent, "android.permission.READ_PHONE_STATE");
            Log.i(LOG_TAG, "Broadcast intent: " + intent.getAction() + ", rat [" + intent.getStringExtra("rat") + "]");
        }
    }

    public SipError onSipError(String service, SipError error) {
        Log.e(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        if ("mmtel".equals(service) && this.mMno == Mno.TIGO_GUATEMALA && SipErrorBase.SERVER_TIMEOUT.equals(error)) {
            removeCurrentPcscfAndInitialRegister(true);
        }
        return error;
    }

    /* access modifiers changed from: protected */
    public void handleAlternativeCallState() {
        if (this.mMno == Mno.TIGO_GUATEMALA) {
            super.handleAlternativeCallState();
        }
    }

    /* access modifiers changed from: protected */
    public Set<String> applyMmtelUserSettings(Set<String> services, int network) {
        if (services == null) {
            return new HashSet();
        }
        if (!isVideoCallEnabled()) {
            removeService(services, "mmtel-video", "VideoCall disable.");
        }
        if (getVoiceTechType() == 0 || this.mTask.getRegistrationRat() == 18) {
            return services;
        }
        Log.i(LOG_TAG, "by VoLTE OFF, remove all service, RAT :" + this.mTask.getRegistrationRat());
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return new HashSet();
    }
}
