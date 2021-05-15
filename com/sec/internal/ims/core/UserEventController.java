package com.sec.internal.ims.core;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.List;

public class UserEventController {
    private static final String LOG_TAG = "RegiMgr-UsrEvtCtr";
    IConfigModule mConfigModule;
    Context mContext;
    protected int mCurrentUserId;
    protected boolean mIsDeviceShutdown = false;
    RegistrationManagerBase mRegMan;
    List<ISimManager> mSimManagers;
    protected SimpleEventLog mSimpleEventLog;
    ITelephonyManager mTelephonyManager;
    IVolteServiceModule mVolteServiceModule;

    public UserEventController(Context context, RegistrationManagerBase regMan, List<ISimManager> simManagers, ITelephonyManager telephonyManager, SimpleEventLog eventLog) {
        this.mContext = context;
        this.mRegMan = regMan;
        this.mSimManagers = simManagers;
        this.mTelephonyManager = telephonyManager;
        this.mSimpleEventLog = eventLog;
        this.mCurrentUserId = Extensions.ActivityManager.getCurrentUser();
        Log.i(LOG_TAG, "Start with User " + this.mCurrentUserId);
    }

    public void setCurrentUserId(int currentUserId) {
        this.mCurrentUserId = currentUserId;
    }

    public void setVolteServiceModule(IVolteServiceModule vsm) {
        this.mVolteServiceModule = vsm;
    }

    public void setConfigModule(IConfigModule cm) {
        this.mConfigModule = cm;
    }

    public boolean isShuttingDown() {
        return this.mIsDeviceShutdown;
    }

    public int getCurrentUserId() {
        return this.mCurrentUserId;
    }

    /* access modifiers changed from: protected */
    public void onDataUsageLimitReached(boolean isDataLimited, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onDataUsageLimitReached: " + isDataLimited);
        List<RegisterTask> rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            SlotBasedConfig.getInstance(phoneId).setDataUsageExceed(isDataLimited);
            for (RegisterTask task : rtl) {
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    task.setReason("data limited exceed");
                    if (task.getMno() == Mno.BELL) {
                        IMSLog.i(LOG_TAG, phoneId, "onDataUsageLimitReached: force update " + task);
                        this.mRegMan.updateRegistration(task, true);
                    } else {
                        this.mRegMan.updateRegistration(task, false);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onChatbotAgreementChanged(int phoneId) {
        Log.i(LOG_TAG, "onChatbotAgreementChanged");
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.mProfile.hasService(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                    task.setReason("chatbot agreement changed");
                    this.mRegMan.updateRegistration(task, true);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMobileDataChanged(int mobileDataOn, int phoneId, NetworkEventController netEvtController) {
        IMSLog.i(LOG_TAG, phoneId, "onMobileDataChanged: " + mobileDataOn);
        for (int slot = 0; slot < this.mSimManagers.size(); slot++) {
            Iterator it = SlotBasedConfig.getInstance(slot).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    task.setReason("mobile data changed : " + mobileDataOn);
                    if (task.getMno().isOneOf(Mno.ATT, Mno.BELL, Mno.VTR)) {
                        this.mRegMan.updateRegistration(task, true);
                    } else if (task.getMno() != Mno.TMOUS || !task.getProfile().getPdn().equals(DeviceConfigManager.IMS)) {
                        if (!task.getGovernor().isMobilePreferredForRcs() || !this.mRegMan.mPdnController.isWifiConnected() || mobileDataOn != 1) {
                            this.mRegMan.updateRegistration(task, false);
                        } else {
                            netEvtController.isPreferredPdnForRCSRegister(task, phoneId, true);
                        }
                    } else if (Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_on", 0) != 1) {
                        task.setDeregiReason(34);
                        if (this.mTelephonyManager.getCallState(task.getPhoneId()) != 0) {
                            IMSLog.i(LOG_TAG, task.getPhoneId(), "Call State is not IDLE. Postpone deregister..");
                            task.setHasPendingDeregister(true);
                        } else {
                            this.mRegMan.deregister(task, false, true, "wifi off with mobileDataSettingChanged");
                        }
                    } else {
                        this.mRegMan.updateRegistration(task, false);
                    }
                }
            }
        }
        this.mRegMan.tryRegister(phoneId);
        if (mobileDataOn == 1) {
            for (int slot2 = 0; slot2 < this.mSimManagers.size(); slot2++) {
                if (slot2 != phoneId && RcsUtils.DualRcs.isRegAllowed(this.mContext, slot2)) {
                    IMSLog.i(LOG_TAG, phoneId, "onMobileDataChanged: tryRegister RCS on other slot");
                    this.mRegMan.tryRegister(slot2);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMobileDataPressedChanged(int mobileDataPressed, int phoneId, NetworkEventController netEvtController) {
        IMSLog.i(LOG_TAG, phoneId, "onMobileDataPressedChanged: " + mobileDataPressed);
        for (int slot = 0; slot < this.mSimManagers.size(); slot++) {
            Iterator it = SlotBasedConfig.getInstance(slot).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && task.getGovernor().isMobilePreferredForRcs() && this.mRegMan.mPdnController.isWifiConnected() && mobileDataPressed == 1) {
                    netEvtController.isPreferredPdnForRCSRegister(task, phoneId, true);
                    this.mRegMan.mHandler.sendTryRegister(task.getPhoneId(), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRoamingDataChanged(boolean RoamingDataOn, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onRoamingDataChanged: " + RoamingDataOn);
        ISimManager sm = this.mSimManagers.get(phoneId);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (sm != null && rtl != null) {
            Mno mno = sm.getSimMno();
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().onRoamingDataChanged(RoamingDataOn);
            }
            if (!mno.isKor()) {
                Iterator it2 = rtl.iterator();
                while (it2.hasNext()) {
                    RegisterTask task = (RegisterTask) it2.next();
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        task.setReason("roaming data changed : " + RoamingDataOn);
                        this.mRegMan.updateRegistration(task, false);
                    }
                }
                this.mRegMan.tryRegister(phoneId);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRoamingSettingsChanged(int RoamingSettingsPref, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onRoamingSettingsChanged: " + RoamingSettingsPref);
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (!RegistrationUtils.hasVolteService(task.getPhoneId(), task.getProfile()) && ConfigUtil.isRcsEur(task.getPhoneId()) && task.isRcsOnly()) {
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    if (RoamingSettingsPref == 0) {
                        task.setReason("Roaming Setting turned off");
                        this.mRegMan.tryDeregisterInternal(task, false, true);
                    }
                } else if (RoamingSettingsPref == 1 || RoamingSettingsPref == 2) {
                    this.mRegMan.tryRegister(task.getPhoneId());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRoamingLteChanged(boolean RoamingLteOn) {
        Log.i(LOG_TAG, "onRoamingLteChanged: " + RoamingLteOn);
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().onRoamingLteChanged(RoamingLteOn);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onVideoCallServiceSettingChanged(boolean isVideoCallEnabled, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onVideoCallServiceSettingChanged:" + isVideoCallEnabled);
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm != null) {
            Mno mno = sm.getSimMno();
            DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.VILTE_SLOT1.getName(), isVideoCallEnabled ^ true ? 1 : 0, phoneId);
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    if (mno == Mno.VZW) {
                        task.setReason("Video Call state changed : " + isVideoCallEnabled);
                        this.mRegMan.updateRegistration(task, false);
                    } else if (mno != Mno.TMOUS) {
                        task.setReason("Video Call state changed : " + isVideoCallEnabled);
                        this.mRegMan.updateRegistration(task, true);
                    } else if (task.getRegistrationRat() != 18 || !SemEmergencyManager.isEmergencyMode(this.mContext)) {
                        task.setReason("Video Call state changed : " + isVideoCallEnabled);
                        this.mRegMan.updateRegistration(task, true);
                    } else {
                        Log.i(LOG_TAG, "skip update registration");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onRcsUserSettingChanged(int newRcsUserSetting, int phoneId) {
        Log.i(LOG_TAG, "onRcsUserSettingChanged: switch: " + newRcsUserSetting);
        int prevRcsUserSetting = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), phoneId);
        Mno mno = SimUtil.getSimMno(phoneId);
        int acsVersion = this.mConfigModule.getAcsConfig(phoneId).getAcsVersion();
        if (newRcsUserSetting == 0) {
            if (mno == Mno.SKT && (prevRcsUserSetting == 1 || prevRcsUserSetting == 3)) {
                if (acsVersion == -1 || acsVersion == -2) {
                    IMSLog.e(LOG_TAG, phoneId, mno.getName() + ": already turnned off - acs version=" + acsVersion);
                } else {
                    IMSLog.e(LOG_TAG, phoneId, mno.getName() + ": treat RCS_DISABLED(0) as RCS_TURNING_OFF(2)");
                    newRcsUserSetting = 2;
                }
            }
        } else if (newRcsUserSetting == 2 && mno != Mno.SKT) {
            IMSLog.e(LOG_TAG, phoneId, mno.getName() + ": RCS_TURNING_OFF(2) is not allowed set rcs_user_setting to 0");
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, 0, phoneId);
            return;
        }
        if (prevRcsUserSetting == newRcsUserSetting) {
            Log.i(LOG_TAG, "same rcs_user_setting not changed : " + newRcsUserSetting);
            return;
        }
        if (prevRcsUserSetting == -1 && newRcsUserSetting == 1) {
            IMSLog.i(LOG_TAG, phoneId, "Reset ACS settings : RCS user switch turned on first time.");
            this.mConfigModule.getAcsConfig(phoneId).resetAcsSettings();
        }
        updateOpMode(phoneId, prevRcsUserSetting, newRcsUserSetting, mno);
        Log.i(LOG_TAG, "modify internal ImsUserSetting(shared pref) from " + prevRcsUserSetting + " to " + newRcsUserSetting);
        DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), newRcsUserSetting, phoneId);
        updateRegistrationByRcsUserSettings(phoneId, newRcsUserSetting, mno);
    }

    /* access modifiers changed from: package-private */
    public void onTTYmodeUpdated(int phoneId, boolean newTtyMode) {
        boolean oldTtyMode = SlotBasedConfig.getInstance(phoneId).getTTYMode();
        IMSLog.i(LOG_TAG, phoneId, "onTTYmodeUpdated: current=" + oldTtyMode + " new=" + newTtyMode);
        List<RegisterTask> rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            boolean isSupportCsTTY = false;
            if (oldTtyMode != newTtyMode) {
                SlotBasedConfig.getInstance(phoneId).setTTYMode(Boolean.valueOf(newTtyMode));
                RegisterTask selectedTask = null;
                Iterator<RegisterTask> it = rtl.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    RegisterTask task = it.next();
                    if (RegistrationUtils.supportCsTty(task)) {
                        selectedTask = task;
                        isSupportCsTTY = true;
                        break;
                    }
                }
                if (isSupportCsTTY) {
                    Log.i(LOG_TAG, "onTTYmodeUpdated: isSupportCsTTY=" + isSupportCsTTY + " new=" + newTtyMode);
                    if (!newTtyMode) {
                        if (!selectedTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                            this.mRegMan.tryRegister(phoneId);
                            return;
                        }
                    }
                    this.mRegMan.updateRegistration(phoneId);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0069 A[EDGE_INSN: B:23:0x0069->B:14:0x0069 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x004c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onRTTmodeUpdated(int r12, boolean r13) {
        /*
            r11 = this;
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r12)
            boolean r1 = r0.getRTTMode()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onRTTmodeUpdated: current="
            r2.append(r3)
            r2.append(r1)
            java.lang.String r3 = " new="
            r2.append(r3)
            r2.append(r13)
            java.lang.String r2 = r2.toString()
            java.lang.String r4 = "RegiMgr-UsrEvtCtr"
            android.util.Log.i(r4, r2)
            r2 = 0
            if (r1 == r13) goto L_0x00c7
            java.lang.Boolean r5 = java.lang.Boolean.valueOf(r13)
            r0.setRTTMode(r5)
            r5 = 0
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r6 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r12)
            boolean r7 = r6.isEmpty()
            if (r7 == 0) goto L_0x0042
            java.lang.String r3 = "RegiterTaskList is empty."
            com.sec.internal.log.IMSLog.i(r4, r12, r3)
            return
        L_0x0042:
            java.util.Iterator r7 = r6.iterator()
        L_0x0046:
            boolean r8 = r7.hasNext()
            if (r8 == 0) goto L_0x0069
            java.lang.Object r8 = r7.next()
            com.sec.internal.ims.core.RegisterTask r8 = (com.sec.internal.ims.core.RegisterTask) r8
            com.sec.ims.settings.ImsProfile r9 = r8.mProfile
            int r9 = r9.getTtyType()
            r10 = 4
            if (r9 == r10) goto L_0x0066
            com.sec.ims.settings.ImsProfile r9 = r8.mProfile
            int r9 = r9.getTtyType()
            r10 = 3
            if (r9 != r10) goto L_0x0065
            goto L_0x0066
        L_0x0065:
            goto L_0x0046
        L_0x0066:
            r2 = 1
            r5 = r8
        L_0x0069:
            if (r2 == 0) goto L_0x00c7
            if (r5 == 0) goto L_0x00c7
            com.sec.internal.constants.Mno r7 = r5.getMno()
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.VZW
            if (r7 == r8) goto L_0x00c7
            com.sec.internal.constants.Mno r7 = r5.getMno()
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.USCC
            if (r7 == r8) goto L_0x00c7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "onRTTmodeUpdated: isSupportRTT="
            r7.append(r8)
            r7.append(r2)
            r7.append(r3)
            r7.append(r13)
            java.lang.String r3 = r7.toString()
            android.util.Log.i(r4, r3)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r7 = "onRTTmodeUpdated: force update "
            r3.append(r7)
            r3.append(r5)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r4, r12, r3)
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "RTT changed : "
            r3.append(r4)
            r3.append(r13)
            java.lang.String r3 = r3.toString()
            r5.setReason(r3)
            com.sec.internal.ims.core.RegistrationManagerBase r3 = r11.mRegMan
            r4 = 1
            r3.updateRegistration(r5, r4)
        L_0x00c7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.UserEventController.onRTTmodeUpdated(int, boolean):void");
    }

    /* access modifiers changed from: package-private */
    public void onVowifiServiceSettingChanged(int phoneId, IRegistrationHandlerNotifiable handler) {
        IMSLog.i(LOG_TAG, phoneId, "onVowifiServiceSettingChanged:");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule == null) {
            Log.e(LOG_TAG, "VolteServiceModule is not create yet so retry after 3 seconds");
            handler.notifyVowifiSettingChanged(phoneId, 3000);
            return;
        }
        iVolteServiceModule.onVoWiFiSwitched(phoneId);
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                if (this.mTelephonyManager.getVoiceNetworkType(SimUtil.getSubId(phoneId)) != 13) {
                    task.setReason("VoWiFi settings changed");
                    this.mRegMan.updateRegistration(task, false);
                } else if (!this.mTelephonyManager.isNetworkRoaming() || this.mRegMan.getNetworkEvent(phoneId).voiceOverPs != VoPsIndication.SUPPORTED) {
                    task.setReason("VoWiFi settings changed");
                    this.mRegMan.updateRegistration(task, false);
                } else {
                    IMSLog.i(LOG_TAG, phoneId, "Skip updateRegistration under LTE roaming NW");
                }
            }
        }
        this.mRegMan.tryRegister(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void onVolteServiceSettingChanged(boolean isVolteEnabled, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onVolteServiceSettingChanged:" + isVolteEnabled);
        ISimManager sm = this.mSimManagers.get(phoneId);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (sm != null && rtl != null) {
            if (!sm.isSimLoaded()) {
                IMSLog.i(LOG_TAG, phoneId, "onVolteServiceSettingChanged: SIM is not available don't save setting");
                return;
            }
            Mno mno = sm.getSimMno();
            DmConfigHelper.setImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), isVolteEnabled ^ true ? 1 : 0, phoneId);
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getRegistrationRat() == 18) {
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && this.mRegMan.getNetworkEvent(phoneId).isEpdgConnected) {
                        if (mno.isOneOf(Mno.ORANGE_POLAND, Mno.TELIA_NORWAY, Mno.TELIA_SWE, Mno.ORANGE)) {
                            Log.i(LOG_TAG, "update eutrn param");
                            task.getGovernor().onVolteSettingChanged();
                        }
                        Log.i(LOG_TAG, "WFC is enabled. Do not modify regi status");
                        return;
                    }
                }
                task.getGovernor().onVolteSettingChanged();
            }
            updateRegistrationByVolteServiceSettings(phoneId, isVolteEnabled, mno);
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserSwitched() {
        this.mSimpleEventLog.logAndAdd("onUserSwitched by MUM");
        IMSLog.c(LogClass.REGI_USER_SWITCHED, ",USER SWITCHED");
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    this.mRegMan.sendDeregister(1000, task.getPhoneId());
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onShuttingDown(int powerOff) {
        Log.i(LOG_TAG, "powerOff :" + powerOff);
        if (powerOff != -1) {
            this.mIsDeviceShutdown = true;
        }
        for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
            boolean deregisterRequires = false;
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    task.setDeregiReason(23);
                    deregisterRequires = true;
                }
                if (task.getMno().isKor()) {
                    deregisterRequires = false;
                }
            }
            if (deregisterRequires) {
                this.mRegMan.sendDeregister(12, phoneId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onVolteRoamingServiceSettingChanged(boolean enabled, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onVolteRoamingServiceSettingChanged:");
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().onVolteRoamingSettingChanged(enabled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onLteDataNetworkModeSettingChanged(boolean enabled, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onLteDataNetworkModeSettingChanged:");
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                ((RegisterTask) it.next()).getGovernor().onLteDataNetworkModeSettingChanged(enabled);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onFlightModeChanged(boolean isOn) {
        if (isOn) {
            this.mRegMan.setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
            for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
                boolean deregisterRequires = false;
                Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
                while (it.hasNext()) {
                    RegisterTask task = (RegisterTask) it.next();
                    this.mConfigModule.getAcsConfig(phoneId).setForceAcs(true);
                    task.setReason("FlightMode On");
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                        this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                        task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    } else {
                        if (!task.isOneOf(RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED)) {
                            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                                deregisterRequires = true;
                                if (task.getMno().isChn() && RegistrationUtils.isDelayDeRegForNonDDSOnFlightModeChanged(task)) {
                                    Log.i(LOG_TAG, "QCT , non-dds send de-reg later");
                                    this.mRegMan.setNonDDSDeRegRequired(true);
                                    deregisterRequires = false;
                                }
                            }
                        } else if (task.getMno() == Mno.RJIL) {
                            task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                        }
                    }
                    task.mIsUpdateRegistering = false;
                    task.getGovernor().resetPcscfList();
                    task.getGovernor().releaseThrottle(1);
                    task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_AIRPLANE_MODE_ON);
                }
                if (deregisterRequires) {
                    this.mRegMan.sendDeregister(12, phoneId);
                }
            }
        }
        this.mRegMan.onFlightModeChanged(isOn);
    }

    private void updateOpMode(int phoneId, int prevRcsUserSetting, int newRcsUserSetting, Mno mno) {
        boolean needChangeOpMode = false;
        boolean prevRcsEnabled = prevRcsUserSetting == 1 || prevRcsUserSetting == 3;
        boolean newRcsEnabled = newRcsUserSetting == 1;
        if (newRcsEnabled != prevRcsEnabled) {
            needChangeOpMode = true;
        }
        if (mno.isKor() && (prevRcsUserSetting == -2 || prevRcsUserSetting == 3)) {
            IMSLog.i(LOG_TAG, phoneId, "Changed rcs_user_setting by network. Skip change op mode.");
            needChangeOpMode = false;
        }
        if (needChangeOpMode) {
            String val = RcsConfigurationHelper.readStringParamWithPath(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.PATH.TC_POPUP_USER_ACCEPT, phoneId));
            int tcPopupUserAccept = -1;
            if (val != null) {
                try {
                    tcPopupUserAccept = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    IMSLog.e(LOG_TAG, phoneId, "Error while parsing integer in getIntValue() - NumberFormatException");
                }
            }
            this.mConfigModule.changeOpMode(newRcsEnabled, phoneId, tcPopupUserAccept);
        }
    }

    private void updateRegistrationByVolteServiceSettings(int phoneId, boolean isVolteEnabled, Mno mno) {
        if (mno != Mno.TMOUS && !mno.isKor()) {
            if (!isVolteEnabled) {
                if (!mno.isOneOf(Mno.VZW, Mno.SPRINT, Mno.ATT)) {
                    Log.i(LOG_TAG, "VoLTE turned off, DeRegister");
                    Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
                    while (it.hasNext()) {
                        RegisterTask task = (RegisterTask) it.next();
                        if (!task.isRcsOnly() && !RegistrationUtils.isCmcProfile(task.getProfile())) {
                            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                                task.setReason("volte setting turned off");
                                task.setDeregiReason(73);
                                this.mRegMan.tryDeregisterInternal(task, false, false);
                            } else {
                                if (mno.isOneOf(Mno.CTC, Mno.CTCMO) || ConfigUtil.isRcsEur(mno) || mno.isOce()) {
                                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                                        this.mRegMan.getImsIconManager(phoneId).updateRegistrationIcon(task.isSuspended());
                                        if (task.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                                            Log.i(LOG_TAG, "VoLTE turned off, no need to keep pdn.");
                                            this.mRegMan.stopPdnConnectivity(task.getPdnType(), task);
                                            task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return;
                }
            }
            Log.i(LOG_TAG, "VoLTE switch changed, updateRegistration");
            this.mRegMan.updateRegistration(phoneId);
        }
    }

    private void updateRegistrationByRcsUserSettings(int phoneId, int newRcsUserSetting, Mno mno) {
        if (mno == Mno.SKT && newRcsUserSetting == 2) {
            Log.i(LOG_TAG, "RCS_TURNING_OFF: Ignore RCS disable for SKT until server responds");
            return;
        }
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (RegistrationUtils.hasRcsService(phoneId, task.getProfile(), this.mRegMan.mPdnController.isWifiConnected())) {
                if (!task.isRcsOnly()) {
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        this.mRegMan.updateRegistration(task, false);
                    } else if (newRcsUserSetting == 1) {
                        this.mRegMan.mHandler.sendTryRegister(task.getPhoneId());
                    } else {
                        this.mRegMan.deregister(task, false, true, "RCS USER SWITCH OFF");
                    }
                } else if (newRcsUserSetting == 1) {
                    this.mRegMan.mHandler.sendTryRegister(task.getPhoneId());
                } else {
                    this.mRegMan.deregister(task, false, true, "RCS USER SWITCH OFF");
                }
            }
        }
    }
}
