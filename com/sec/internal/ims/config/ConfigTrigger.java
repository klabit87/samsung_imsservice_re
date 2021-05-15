package com.sec.internal.ims.config;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.im.strategy.CmccStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigTrigger {
    private static final String INTENT_ACTION_RCS_AUTOCONFIG_START = "com.android.ims.RCS_AUTOCONFIG_START";
    private static final String LOG_TAG = ConfigTrigger.class.getSimpleName();
    private static final String MESSAGE_PACKAGE_NAME = "com.samsung.android.messaging";
    private Map<Integer, DiagnosisConstants.RCSA_ATRE> mAcsTryReason = new ConcurrentHashMap();
    private IConfigModule mCm;
    private final Context mContext;
    private boolean mDualSimRcsAutoConfig = false;
    private final SimpleEventLog mEventLog;
    private boolean mNeedResetConfig = false;
    private SparseBooleanArray mReadyStartCmdList = new SparseBooleanArray();
    private boolean mReadyStartForceCmd = false;
    private IRegistrationManager mRm;

    public ConfigTrigger(Context context, IRegistrationManager rm, IConfigModule cm, SimpleEventLog eventLog) {
        this.mContext = context;
        this.mRm = rm;
        this.mCm = cm;
        this.mEventLog = eventLog;
    }

    /* access modifiers changed from: protected */
    public void setStateforTriggeringACS(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "setStateforTriggeringACS:");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        List<IRegisterTask> rtl = this.mRm.getPendingRegistration(phoneId);
        if (sm != null && rtl != null) {
            Mno mno = sm.getSimMno();
            if (!sm.hasNoSim() && ConfigUtil.isRcsAvailable(this.mContext, phoneId, sm) && !this.mCm.getAcsConfig(phoneId).isAcsCompleted() && !mno.isKor()) {
                for (IRegisterTask task : rtl) {
                    if (isWaitAutoconfig(task) && (task.getState() == RegistrationConstants.RegisterTaskState.IDLE || (task.getState() == RegistrationConstants.RegisterTaskState.CONFIGURED && (mno == Mno.SPRINT || mno == Mno.TCE || mno == Mno.CLARO_ARGENTINA || mno == Mno.CLARO_COLOMBIA || mno == Mno.CLARO_BRAZIL || mno == Mno.TIM_BRAZIL)))) {
                        this.mEventLog.logAndAdd(phoneId, "RegisterTask setState: CONFIGURING");
                        task.setState(RegistrationConstants.RegisterTaskState.CONFIGURING);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setRegisterFromApp(boolean tryregi, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "setRegisterFromApp:");
        List<IRegisterTask> rtl = this.mRm.getPendingRegistration(phoneId);
        if (rtl != null) {
            if (tryregi) {
                for (IRegisterTask task : rtl) {
                    if (task.isRcsOnly()) {
                        if (task.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONFIGURED)) {
                            IMSLog.i(LOG_TAG, phoneId, "setRegisterFromApp: set AcsCompleteStatus as false");
                            this.mCm.getAcsConfig(phoneId).setAcsCompleteStatus(false);
                        }
                    }
                }
                IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
                if (mnoStrategy != null && mnoStrategy.isRemoteConfigNeeded(phoneId)) {
                    IMSLog.i(LOG_TAG, phoneId, "setRegisterFromApp: reset acsSettings");
                    this.mCm.getAcsConfig(phoneId).resetAcsSettings();
                    setReadyStartForceCmd(true);
                }
                setAcsTryReason(phoneId, DiagnosisConstants.RCSA_ATRE.FROM_APP);
            }
            this.mRm.requestTryRegister(phoneId);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isWaitAutoconfig(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "isWaitAutoConfig:");
        Mno mno = task.getMno();
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (this.mCm.isSimMoActivatedAndRcsEurSupported(phoneId, sm, this.mRm)) {
            boolean mobilityRCS = false;
            if (task.getProfile().getEnableRcs()) {
                IMSLog.i(LOG_TAG, phoneId, "isWaitAutoConfig: RCS is enabled in SIM mobility");
                mobilityRCS = true;
            } else if (OmcCode.isKorOpenOmcCode() && mno.isKor()) {
                mobilityRCS = true;
            }
            if (!mobilityRCS) {
                IMSLog.i(LOG_TAG, phoneId, "isWaitAutoConfig: This is a other country SIM, RCS disabled in SIM mobility");
                return false;
            }
        }
        if (!ConfigUtil.isRcsAvailable(this.mContext, phoneId, sm) || (this.mCm.getAcsConfig(phoneId).isAcsCompleted() && (!mno.isKor() || !this.mCm.getAcsConfig(phoneId).needForceAcs()))) {
            return false;
        }
        return task.getProfile().getNeedAutoconfig();
    }

    /* access modifiers changed from: protected */
    public boolean triggerAutoConfig(boolean forceAutoconfig, int phoneId, List<IRegisterTask> regiTaskList) {
        IMSLog.i(LOG_TAG, phoneId, "triggerAutoConfig:");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null || regiTaskList == null) {
            return false;
        }
        Mno mno = sm.getSimMno();
        if (sm.hasNoSim() || !ConfigUtil.isRcsAvailable(this.mContext, phoneId, sm)) {
            return true;
        }
        if (this.mCm.getAcsConfig(phoneId).isAcsCompleted() && (!mno.isKor() || !this.mCm.getAcsConfig(phoneId).needForceAcs())) {
            return true;
        }
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "RCS is enabled, triggering autoconfiguration... forceAutoconfig:" + forceAutoconfig);
        if (mno.isKor()) {
            return triggerAutoConfigForKor(forceAutoconfig, phoneId, regiTaskList);
        }
        Bundle bundle = new Bundle();
        bundle.putInt("phoneId", phoneId);
        if (getDualSimRcsAutoConfig()) {
            if (ConfigUtil.isRcsEur(mno)) {
                IConfigModule iConfigModule = this.mCm;
                iConfigModule.startAutoConfigDualsim(phoneId, iConfigModule.obtainConfigMessage(13, bundle));
            } else {
                IConfigModule iConfigModule2 = this.mCm;
                iConfigModule2.startAutoConfig(forceAutoconfig, iConfigModule2.obtainConfigMessage(13, bundle), phoneId);
            }
            setDualSimRcsAutoConfig(false);
            return true;
        }
        IConfigModule iConfigModule3 = this.mCm;
        iConfigModule3.startAutoConfig(forceAutoconfig, iConfigModule3.obtainConfigMessage(13, bundle), phoneId);
        return true;
    }

    private boolean triggerAutoConfigForKor(boolean forceAutoconfig, int phoneId, List<IRegisterTask> regiTaskList) {
        Iterator<IRegisterTask> it = regiTaskList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            IRegisterTask rTask = it.next();
            if (!rTask.isRcsOnly()) {
                ISimManager simManager = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
                if (simManager != null && TextUtils.isEmpty(simManager.getMsisdn()) && rTask.getPdnType() == 11 && rTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
                    IMSLog.i(LOG_TAG, phoneId, "MSISDN is null, try to RCS ACS after registered VoLTE");
                    return false;
                }
            } else if (rTask.getState() != RegistrationConstants.RegisterTaskState.CONFIGURING || !isWaitAutoconfig(rTask)) {
                if (rTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED && isWaitAutoconfig(rTask)) {
                    this.mEventLog.logAndAdd(phoneId, "RegisterTask setState: CONFIGURING");
                    rTask.setState(RegistrationConstants.RegisterTaskState.CONFIGURING);
                }
                Bundle bundle = new Bundle();
                bundle.putInt("phoneId", rTask.getPhoneId());
                if (this.mCm.getAcsConfig(phoneId).needForceAcs()) {
                    IConfigModule iConfigModule = this.mCm;
                    iConfigModule.startAutoConfig(true, iConfigModule.obtainConfigMessage(13, bundle), phoneId);
                } else {
                    IConfigModule iConfigModule2 = this.mCm;
                    iConfigModule2.startAutoConfig(forceAutoconfig, iConfigModule2.obtainConfigMessage(13, bundle), phoneId);
                }
            } else {
                IMSLog.i(LOG_TAG, phoneId, "triggerAutoConfig : already autoconfiguration is processing and not get complete notify yet");
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isValidAcsVersion(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "isValidAcsVersion:");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null || sm.hasNoSim()) {
            return false;
        }
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, phoneId)) {
            IMSLog.i(LOG_TAG, phoneId, "DDS set to other SIM");
            return false;
        } else if (!ConfigUtil.isSimMobilityRCS(phoneId, sm, this.mRm)) {
            IMSLog.i(LOG_TAG, phoneId, "isValidAcsVersion: This is a other country SIM, RCS disabled in SIM mobility");
            return false;
        } else {
            boolean userSetting = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 0, phoneId) == 1;
            Integer version = this.mCm.getRcsConfVersion(phoneId);
            boolean isRcsAcsCompleted = this.mCm.getAcsConfig(phoneId).isAcsCompleted();
            boolean mdmrcsstatus = ConfigUtil.checkMdmRcsStatus(this.mContext, phoneId);
            String str = LOG_TAG;
            IMSLog.i(str, phoneId, "RCS switch: " + userSetting + ", version: " + version + ", isRcsAcsCompleted: " + isRcsAcsCompleted);
            if (!mdmrcsstatus) {
                IMSLog.i(LOG_TAG, phoneId, "RCS service isn't allowed by MDM");
                return false;
            } else if (!isRcsAcsCompleted) {
                String str2 = LOG_TAG;
                IMSLog.i(str2, phoneId, "RCS switch is on & config version: " + version + ". This shouldn't happen!");
                return userSetting;
            } else {
                if (!userSetting) {
                    Mno mno = sm.getSimMno();
                    String rcsAs = ConfigUtil.getAcsServerType(this.mContext, phoneId);
                    if (!(mno == Mno.ATT || mno == Mno.VZW) || ImsConstants.RCS_AS.JIBE.equals(rcsAs)) {
                        IMSLog.i(LOG_TAG, phoneId, "userSetting is disabled");
                        return false;
                    }
                }
                if (version == null || version.intValue() == 0 || version.intValue() == -3) {
                    String str3 = LOG_TAG;
                    IMSLog.i(str3, phoneId, "version is improper : " + version);
                    return false;
                } else if (version.intValue() >= 0) {
                    return true;
                } else {
                    String str4 = LOG_TAG;
                    IMSLog.i(str4, phoneId, "RCS switch is on & config version: " + version + ". This shouldn't happen!");
                    return false;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void tryAutoConfig(IWorkflow workflow, int phoneId, boolean isSimInfochanged, boolean mobileNetwork) {
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "tryAutoConfig: mobileNetwork: " + mobileNetwork);
        if (workflow == null) {
            IMSLog.i(LOG_TAG, phoneId, "tryAutoConfig: workflow is null");
            return;
        }
        IMSLog.c(LogClass.CM_TRY_ACS, phoneId + ",FORCE:" + getReadyStartForceCmd() + ",RST:" + getNeedResetConfig());
        this.mCm.getAvailableNetwork(phoneId);
        boolean mobileNetwork2 = this.mCm.updateMobileNetworkforDualRcs(phoneId);
        String str2 = LOG_TAG;
        IMSLog.i(str2, phoneId, "tryAutoConfig: updateMobileNetworkforDualRcs: " + mobileNetwork2);
        if (getDualSimRcsAutoConfig()) {
            workflow.startAutoConfigDualsim(mobileNetwork2);
            setDualSimRcsAutoConfig(false);
        } else if (getReadyStartForceCmd()) {
            if (getNeedResetConfig()) {
                workflow.forceAutoConfigNeedResetConfig(mobileNetwork2);
                setNeedResetConfig(false);
            } else {
                workflow.forceAutoConfig(mobileNetwork2);
            }
            setReadyStartForceCmd(false);
        } else {
            IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
            if (!isSimInfochanged || !(mnoStrategy instanceof CmccStrategy)) {
                workflow.startAutoConfig(mobileNetwork2);
            } else {
                workflow.forceAutoConfig(mobileNetwork2);
            }
        }
        if (SimUtil.getSimMno(phoneId) != Mno.CMCC) {
            setReadyStartCmdList(phoneId, false);
        }
    }

    /* access modifiers changed from: protected */
    public void startAutoConfig(boolean forced, Message onComplete, int phoneId) {
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "startAutoConfig: forced: " + forced);
        IMSLog.c(LogClass.CM_START_ACS, phoneId + ",FORCE:" + forced);
        if (!forced) {
            startConfig(2, onComplete, phoneId);
        } else {
            startConfig(1, onComplete, phoneId);
        }
    }

    /* access modifiers changed from: protected */
    public void startAutoConfigDualsim(int phoneId, Message onComplete) {
        startConfig(9, onComplete, phoneId);
    }

    /* access modifiers changed from: protected */
    public void startConfig(int cmd, Message onComplete, int phoneId) {
        this.mCm.sendConfigMessage(0, phoneId);
        String str = LOG_TAG;
        IMSLog.i(str, phoneId, "startConfig: cmd: " + cmd);
        if (cmd == 1 || cmd == 2) {
            String rcsAs = ConfigUtil.getAcsServerType(this.mContext, phoneId);
            Mno simMno = SimUtil.getSimMno(phoneId);
            if (ImsConstants.RCS_AS.JIBE.equals(rcsAs) || ImsConstants.RCS_AS.SEC.equals(rcsAs)) {
                Log.i(LOG_TAG, "sendBroadcast com.android.ims.RCS_AUTOCONFIG_START");
                Intent intent = new Intent();
                intent.setAction(INTENT_ACTION_RCS_AUTOCONFIG_START);
                intent.setPackage("com.samsung.android.messaging");
                intent.addFlags(LogClass.SIM_EVENT);
                this.mContext.sendBroadcast(intent);
            }
        } else {
            if (cmd != 20) {
                switch (cmd) {
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        break;
                    case 9:
                        break;
                    default:
                        Log.i(LOG_TAG, "unknown cmd");
                        return;
                }
            }
            this.mCm.sendConfigMessage(cmd, phoneId);
        }
        String rcsAs2 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onComplete: ");
        sb.append(onComplete != null ? onComplete.toString() : "null");
        Log.i(rcsAs2, sb.toString());
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, "Autoconfig start: cmd: " + cmd);
        this.mCm.sendConfigMessage(cmd, phoneId);
    }

    /* access modifiers changed from: protected */
    public void setAcsTryReason(int phoneId, DiagnosisConstants.RCSA_ATRE autoconfigTryReason) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, "setAutoconfigTryReason: " + autoconfigTryReason.toString());
        IMSLog.c(LogClass.CM_ACS_TRY_REASON, phoneId + ",TR:" + autoconfigTryReason.toString());
        this.mAcsTryReason.put(Integer.valueOf(phoneId), autoconfigTryReason);
    }

    /* access modifiers changed from: protected */
    public DiagnosisConstants.RCSA_ATRE getAcsTryReason(int phoneId) {
        DiagnosisConstants.RCSA_ATRE rsn = this.mAcsTryReason.get(Integer.valueOf(phoneId));
        if (rsn != null) {
            return rsn;
        }
        DiagnosisConstants.RCSA_ATRE rsn2 = DiagnosisConstants.RCSA_ATRE.INIT;
        this.mAcsTryReason.put(Integer.valueOf(phoneId), rsn2);
        return rsn2;
    }

    /* access modifiers changed from: protected */
    public void resetAcsTryReason(int phoneId) {
        DiagnosisConstants.RCSA_ATRE rsn = this.mAcsTryReason.get(Integer.valueOf(phoneId));
        if (rsn == null || rsn != DiagnosisConstants.RCSA_ATRE.INIT) {
            this.mAcsTryReason.put(Integer.valueOf(phoneId), DiagnosisConstants.RCSA_ATRE.INIT);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getDualSimRcsAutoConfig() {
        return this.mDualSimRcsAutoConfig;
    }

    /* access modifiers changed from: protected */
    public void setDualSimRcsAutoConfig(boolean isDualSimAcs) {
        String str = LOG_TAG;
        Log.i(str, "setDualSimRcsAutoConfig: isDualSimAcs: " + isDualSimAcs);
        this.mDualSimRcsAutoConfig = isDualSimAcs;
    }

    /* access modifiers changed from: protected */
    public boolean getReadyStartCmdList(int phoneId) {
        return this.mReadyStartCmdList.get(phoneId);
    }

    /* access modifiers changed from: protected */
    public void setReadyStartCmdList(int phoneId, boolean readyStartCmd) {
        this.mReadyStartCmdList.put(phoneId, readyStartCmd);
    }

    /* access modifiers changed from: protected */
    public int getReadyStartCmdListIndexOfKey(int phoneId) {
        return this.mReadyStartCmdList.indexOfKey(phoneId);
    }

    /* access modifiers changed from: protected */
    public boolean getReadyStartForceCmd() {
        return this.mReadyStartForceCmd;
    }

    /* access modifiers changed from: protected */
    public void setReadyStartForceCmd(boolean readyStartForceCmd) {
        String str = LOG_TAG;
        Log.i(str, "setReadyStartForceCmd: readyStartForceCmd: " + readyStartForceCmd);
        this.mReadyStartForceCmd = readyStartForceCmd;
    }

    /* access modifiers changed from: protected */
    public boolean getNeedResetConfig() {
        return this.mNeedResetConfig;
    }

    /* access modifiers changed from: protected */
    public void setNeedResetConfig(boolean needResetConfig) {
        String str = LOG_TAG;
        Log.i(str, "setNeedResetConfig: needResetConfig: " + needResetConfig);
        this.mNeedResetConfig = needResetConfig;
    }

    /* access modifiers changed from: protected */
    public boolean tryAutoconfiguration(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        List<IRegisterTask> rtl = this.mRm.getPendingRegistration(phoneId);
        ACSConfig config = this.mCm.getAcsConfig(phoneId);
        boolean forceAcs = false;
        boolean isRcsEnabled = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 1, phoneId) == 1;
        if (config != null && config.getAcsVersion() == -2 && isRcsEnabled) {
            Log.i(LOG_TAG, "ACS version: -2, IMS RCS switch enabled - set force autoconfig NOW.");
            config.clear();
            forceAcs = true;
        }
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) rtl) && isWaitAutoconfig(task)) {
            IMSLog.i(LOG_TAG, phoneId, "autoconfig is not ready");
            if (task.getMno().isKor()) {
                if (task.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                    triggerAutoConfig(false, task.getPhoneId(), rtl);
                    return true;
                }
            } else if (config == null || !config.isRcsDisabled() || !ConfigUtil.isRcsEurNonRjil(task.getMno())) {
                setStateforTriggeringACS(task.getPhoneId());
                triggerAutoConfig(forceAcs, task.getPhoneId(), rtl);
                return true;
            } else {
                Log.i(LOG_TAG, "Version & validity == 0. Autoconfiguration will be performed after next reboot");
                return true;
            }
        }
        return false;
    }
}
