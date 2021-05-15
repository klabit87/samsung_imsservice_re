package com.sec.internal.ims.config;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.diagnosis.RcsHqmAgent;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.log.IMSLog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigComplete {
    private static final String LOG_TAG = ConfigComplete.class.getSimpleName();
    private IConfigModule mCm;
    private final Context mContext;
    private final SimpleEventLog mEventLog;
    private IRegistrationManager mRm;

    public ConfigComplete(Context context, IRegistrationManager rm, IConfigModule cm, SimpleEventLog eventLog) {
        this.mContext = context;
        this.mRm = rm;
        this.mCm = cm;
        this.mEventLog = eventLog;
    }

    /* access modifiers changed from: protected */
    public void setStateforACSComplete(int errorCode, int phoneId, List<IRegisterTask> rtl, int forbiddenCnt) {
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "setStateforACSComplete: " + errorCode);
        if (rtl != null) {
            Integer version = this.mCm.getRcsConfVersion(phoneId);
            ContentValues cv = new ContentValues();
            cv.put(DiagnosisConstants.DRCS_KEY_RACV, version);
            ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, cv);
            int i = phoneId;
            List<IRegisterTask> list = rtl;
            Integer num = version;
            String acsServerType = ConfigUtil.getAcsServerType(this.mContext, phoneId);
            releasePermanentBlockforJibe(errorCode, i, list, num, acsServerType, forbiddenCnt);
            ImsRegistry.getServiceModuleManager().notifyAutoConfigDone(phoneId);
            ImsRegistry.getServiceModuleManager().notifyConfigured(true, phoneId);
            setStateforDualRegistration(errorCode, i, list, num, acsServerType);
        }
    }

    private void releasePermanentBlockforJibe(int errorCode, int phoneId, List<IRegisterTask> rtl, Integer version, String rcsAs, int forbiddenCnt) {
        for (IRegisterTask task : rtl) {
            if (ImsConstants.RCS_AS.JIBE.equals(rcsAs) && ConfigUtil.hasChatbotService(phoneId, this.mRm) && task.getGovernor().isThrottled() && errorCode == 200 && version != null && version.intValue() > 0 && forbiddenCnt < 2) {
                IMSLog.i(LOG_TAG, phoneId, "releasePermanentBlock: register is blocked, release");
                this.mRm.releaseThrottleByAcs(phoneId);
            }
        }
    }

    private void setStateforDualRegistration(int errorCode, int phoneId, List<IRegisterTask> rtl, Integer version, String rcsAs) {
        for (IRegisterTask task : rtl) {
            if ((!task.getMno().isKor() && !ImsConstants.RCS_AS.JIBE.equals(rcsAs)) || (!task.getProfile().hasService("mmtel") && !task.getProfile().hasService("mmtel-video"))) {
                if (task.getState() == RegistrationConstants.RegisterTaskState.CONFIGURING) {
                    this.mEventLog.logAndAdd(phoneId, "RegisterTask setState: CONFIGURED");
                    task.setState(RegistrationConstants.RegisterTaskState.CONFIGURED);
                } else {
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && ((this.mCm.getAcsConfig(phoneId).getAcsVersion() == 0 || (task.getMno().isKor() && errorCode == -1)) && task.getMno() != Mno.RJIL && version != null && version.intValue() > 0 && task.getProfile().getNeedAutoconfig())) {
                        task.setReason("autocofig is changed");
                        task.setDeregiReason(32);
                        this.mRm.deregister(task, false, true, "AUTOCONFIG_CHANGED");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean sendRCSAInfoToHQM(int acsVersion, int errorCode, int phoneId) {
        if (phoneId < 0) {
            Log.i(LOG_TAG, "sendRCSAInfoToHQM : phoneId is invaild " + phoneId);
            phoneId = 0;
        }
        int i = 1;
        boolean isPass = errorCode == 200;
        Map<String, String> rcsmKeys = new LinkedHashMap<>();
        if (isPass) {
            i = 0;
        }
        rcsmKeys.put(DiagnosisConstants.RCSA_KEY_ARST, String.valueOf(i));
        rcsmKeys.put(DiagnosisConstants.RCSA_KEY_AVER, String.valueOf(acsVersion));
        rcsmKeys.put("ERRC", String.valueOf(errorCode));
        rcsmKeys.put(DiagnosisConstants.RCSA_KEY_PROF, this.mCm.getRcsProfile(phoneId));
        rcsmKeys.put(DiagnosisConstants.RCSA_KEY_ATRE, String.valueOf(this.mCm.getAcsTryReason(phoneId).ordinal()));
        this.mCm.resetAcsTryReason(phoneId);
        return RcsHqmAgent.sendRCSInfoToHQM(this.mContext, DiagnosisConstants.FEATURE_RCSA, phoneId, rcsmKeys);
    }

    /* access modifiers changed from: protected */
    public void handleAutoconfigurationComplete(int phoneId, List<IRegisterTask> rtl, int errorCode, IWorkflow workflow) {
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "handleAutoconfigurationComplete: " + errorCode);
        Mno mno = SimUtil.getSimMno(phoneId);
        ACSConfig config = this.mCm.getAcsConfig(phoneId);
        if (rtl != null && mno != Mno.DEFAULT && workflow != null && config != null) {
            handleAutoconfigurationVersion(workflow, config, mno, errorCode, phoneId);
            config.setAcsCompleteStatus(true);
            config.setForceAcs(false);
            config.setIsTriggeredByNrcr(false);
        }
    }

    private void handleAutoconfigurationVersion(IWorkflow workflow, ACSConfig config, Mno mno, int errorCode, int phoneId) {
        Integer version = this.mCm.getRcsConfVersion(phoneId);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "Autoconfiguration Version: " + version);
        if (version != null) {
            if (version.intValue() == 0) {
                this.mEventLog.logAndAdd(phoneId, "Since the version is 0, RCS services are filtered");
                IMSLog.c(LogClass.CM_ACS_COMPLETE, phoneId + ",VER:" + version + ",EC:" + errorCode);
                if (errorCode != 987) {
                    config.disableRcsByAcs(true);
                }
            } else if (version.intValue() == -1 || version.intValue() == -2) {
                IMSLog.i(LOG_TAG, phoneId, "RCS services are disabled");
                if (mno == Mno.SKT) {
                    workflow.clearToken();
                    workflow.removeValidToken();
                    IImModule imModule = ImsRegistry.getServiceModuleManager().getImModule();
                    if (imModule != null) {
                        IMSLog.i(LOG_TAG, phoneId, "Try deleteChatsForUnsubscribe for SKT");
                        imModule.deleteChatsForUnsubscribe();
                    }
                }
                if (config.isTriggeredByNrcr() || mno == Mno.SWISSCOM || mno.isKor()) {
                    DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), -2, phoneId);
                }
                ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 0, phoneId);
            } else if (version.intValue() == -3) {
                config.setRcsDormantMode(true);
            } else if (version.intValue() >= 1) {
                handleAutoconfigurationActiveVersion(config, mno, phoneId);
            }
            config.setAcsVersion(version.intValue());
        }
    }

    private void handleAutoconfigurationActiveVersion(ACSConfig config, Mno mno, int phoneId) {
        int oldVersion = config.getAcsVersion();
        if (oldVersion == -2 || (mno.isKor() && oldVersion == -1)) {
            if (config.isTriggeredByNrcr() != 0) {
                DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), 3, phoneId);
            }
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 1, phoneId);
        } else if (mno == Mno.SKT && DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), phoneId) == 2) {
            IMSLog.i(LOG_TAG, phoneId, "disable RCS failed modify rcs_user_setting to RCS_ENABLED for SKT");
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 1, phoneId);
        }
    }
}
