package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsSettings;
import com.sec.ims.settings.NvConfiguration;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RegistrationGovernorUsc extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnUsc";

    public RegistrationGovernorUsc(Context ctx) {
        this.mContext = ctx;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RegistrationGovernorUsc(RegistrationManagerInternal regMan, ITelephonyManager telephonyManager, RegisterTask task, PdnController pdnController, IVolteServiceModule vsm, IConfigModule cm, Context context) {
        super(regMan, telephonyManager, task, pdnController, vsm, cm, context);
        int phoneId = this.mTask.getPhoneId();
        int eutranPrefFromDm = getVoiceDomainPrefEutran();
        boolean isSwitchEnabled = false;
        if (eutranPrefFromDm >= 1 && eutranPrefFromDm <= 4) {
            String nvVersion = NvConfiguration.get(this.mContext, DeviceConfigManager.NV_INIT_DONE, "1");
            ContentValues mnoInfo = this.mRegMan.getSimManager(phoneId).getMnoInfo();
            IMSLog.i(LOG_TAG, phoneId, "NV version [" + nvVersion + "], DM [" + eutranPrefFromDm + "]");
            String eutranFromImsupdate = CollectionUtils.getIntValue(mnoInfo, ISimManager.KEY_IMSSWITCH_TYPE, 0) == 4 ? getEutranPrefFromImsUpdate() : "";
            if (!"1".equals(nvVersion) || TextUtils.isEmpty(eutranFromImsupdate) || Integer.parseInt(eutranFromImsupdate) != 3) {
                ContentValues value = new ContentValues();
                value.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, String.valueOf(eutranPrefFromDm));
                this.mContext.getContentResolver().update(ImsSettings.GlobalTable.CONTENT_URI, value, (String) null, (String[]) null);
                this.mRegMan.getEventLog().logAndAdd("GvnUsc: Restoring VOICE_DOMAIN_PREF_EUTRAN from DM.");
                IMSLog.c(LogClass.USC_LOAD_EUTRAN, this.mTask.getPhoneId() + ",EUTRAN:" + eutranPrefFromDm);
            } else {
                this.mRegMan.getEventLog().logAndAdd(phoneId, "RegiGvnUsc: SET EUTRAN 3 BY FORCE!");
                NvConfiguration.set(this.mContext, DeviceConfigManager.NV_INIT_DONE, "2");
                NvConfiguration.set(this.mContext, "VOICE_DOMAIN_PREF_EUTRAN", String.valueOf(3));
            }
        }
        isSwitchEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", this.mTask.getPhoneId()) == 1 ? true : isSwitchEnabled;
        String nvLvc = NvConfiguration.get(this.mContext, "LVC_ENABLED", "0");
        if (isSwitchEnabled && "0".equals(nvLvc)) {
            NvConfiguration.set(this.mContext, "LVC_ENABLED", "1");
        }
    }

    private String getEutranPrefFromImsUpdate() {
        int phoneId = this.mTask.getPhoneId();
        String eutranFromImsUpdate = ImsAutoUpdate.getInstance(this.mContext, phoneId).getGlobalSettingsSpecificParam(1, Mno.USCC.getName(), GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN);
        IMSLog.i(LOG_TAG, phoneId, "getEutranPrefFromImsUpdate: " + eutranFromImsUpdate);
        return (String) Optional.ofNullable(eutranFromImsUpdate).orElse("");
    }

    public void onPdnRequestFailed(String reason) {
        super.onPdnRequestFailed(reason);
        if (("SERVICE_OPTION_NOT_SUBSCRIBED".equalsIgnoreCase(reason) || "MULTI_CONN_TO_SAME_PDN_NOT_ALLOWED".equalsIgnoreCase(reason)) && this.mTask.getPdnType() == 11 && this.mTask.getRegistrationRat() == 13) {
            Log.i(LOG_TAG, "send ImsNotAvailable");
            this.mRegMan.notifyImsNotAvailable(this.mTask, true);
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
        if (!unsolicit) {
            this.mCurPcscfIpIdx++;
        }
        this.mFailureCounter++;
        if (SipErrorBase.isImsForbiddenError(error)) {
            handleForbiddenError(retryAfter);
            return;
        }
        if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
            handleTimeoutError(retryAfter);
        }
        handleRetryTimer(retryAfter);
    }

    public SipError onSipError(String service, SipError error) {
        Log.e(LOG_TAG, "onSipError: service=" + service + " error=" + error);
        this.mIsValid = this.mNumOfPcscfIp > 0;
        if (!"mmtel".equals(service)) {
            return super.onSipError(service, error);
        }
        this.mTask.setReason("SIP ERROR[MMTEL] : Deregister..");
        if (SipErrorBase.ALTERNATIVE_SERVICE.equals(error) || ((!SipErrorBase.SESSION_INTERVAL_TOO_SMALL.equals(error) && !SipErrorBase.INTERVAL_TOO_BRIEF.equals(error) && !SipErrorBase.ANONYMITY_DISALLOWED.equals(error) && !SipErrorBase.BUSY_HERE.equals(error) && !SipErrorBase.REQUEST_TERMINATED.equals(error) && SipErrorBase.SipErrorType.ERROR_4XX.equals(error)) || SipErrorBase.SipErrorType.ERROR_5XX.equals(error) || SipErrorBase.SipErrorType.ERROR_6XX.equals(error))) {
            this.mTask.setDeregiReason(43);
            RegistrationManagerInternal registrationManagerInternal = this.mRegMan;
            RegisterTask registerTask = this.mTask;
            registrationManagerInternal.deregister(registerTask, true, true, "Deregister due to " + error);
        }
        return error;
    }

    public Set<String> filterService(Set<String> services, int network) {
        Set<String> filteredServices = super.filterService(services, network);
        int phoneId = this.mTask.getPhoneId();
        if (getVoiceTechType() == 0 || network == 18) {
            if (RcsConfigurationHelper.getConfigData(this.mContext, "root/application/*", phoneId).readInt(ConfigConstants.ConfigTable.SERVICES_IR94_VIDEO_AUTH, -1).intValue() != 1) {
                removeService(filteredServices, "mmtel-video", "ir94VideoAuth off");
            }
            return filteredServices;
        }
        Log.i(LOG_TAG, "Volte : OFF, RAT : " + this.mTask.getRegistrationRat());
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF.getCode());
        return new HashSet();
    }

    /* access modifiers changed from: protected */
    public boolean checkEutranSetting(int rat) {
        if (rat == 18) {
            return true;
        }
        int voiceDomainPrefEutran = getVoiceDomainPrefEutran();
        Log.i(LOG_TAG, "voiceDomainPrefEutran : " + voiceDomainPrefEutran);
        if (voiceDomainPrefEutran == 3) {
            return true;
        }
        this.mTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DM_EUTRAN_OFF.getCode());
        return false;
    }

    public boolean isReadyToRegister(int rat) {
        return checkEmergencyStatus() || (checkRegiStatus() && checkEpdgEvent(rat) && checkCallStatus() && checkEutranSetting(rat) && checkRcsEvent(rat)) || checkMdmnProfile();
    }

    public void onConfigUpdated() {
        int voiceDomainPrefEutran = getVoiceDomainPrefEutran();
        Log.i(LOG_TAG, "onConfigUpdated : voiceDomainPrefEutran : " + voiceDomainPrefEutran);
        IMSLog.c(LogClass.USC_UPDATE_EUTRAN, this.mTask.getPhoneId() + ",UPD EUTRAN:" + voiceDomainPrefEutran);
        ContentValues value = new ContentValues();
        value.put(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, String.valueOf(voiceDomainPrefEutran));
        this.mContext.getContentResolver().update(ImsSettings.GlobalTable.CONTENT_URI, value, (String) null, (String[]) null);
        SimpleEventLog eventLog = this.mRegMan.getEventLog();
        eventLog.logAndAdd("GvnUsc: onConfigUpdated(): Update to GlobalSettings voice_domain_pref_eutran [" + voiceDomainPrefEutran + "]");
        if (voiceDomainPrefEutran != 3) {
            Log.i(LOG_TAG, "volte had disabled by DM");
            this.mTask.setDeregiReason(73);
            this.mRegMan.deregister(this.mTask, false, false, "volte had disabled by DM");
        }
    }

    /* access modifiers changed from: protected */
    public int getVoiceDomainPrefEutran() {
        return DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_VOICE_DOMAIN_PREF_EUTRAN, 0).intValue();
    }
}
