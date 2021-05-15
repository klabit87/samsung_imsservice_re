package com.sec.internal.ims.core;

import android.content.Context;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

abstract class RegistrationManagerInternal extends RegistrationManager {
    /* access modifiers changed from: protected */
    public abstract void notifyImsNotAvailable(RegisterTask registerTask, boolean z);

    RegistrationManagerInternal(IImsFramework imsFramework, Context ctx, PdnController pc, List<ISimManager> smList, ITelephonyManager tm, ICmcAccountManager cmcAm, IRcsPolicyManager rcsPm) {
        this.mContext = ctx;
        this.mEmmCause = -1;
        this.mEventLog = new SimpleEventLog(ctx, IRegistrationManager.LOG_TAG, ImSessionEvent.MESSAGING_EVENT);
        this.mImsFramework = imsFramework;
        this.mTelephonyManager = tm;
        this.mPdnController = pc;
        this.mSimManagers = smList;
        this.mCmcAccountManager = cmcAm;
        this.mRcsPolicyManager = rcsPm;
        this.mAuEmergencyProfile = new SparseArray();
    }

    public void initSequentially() {
        this.mNetEvtCtr.setRegistrationHandler(this.mHandler);
        this.mHandler.init();
        this.mlegacyPhoneCount = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getPhoneCount();
        if (this.mlegacyPhoneCount == 0 && this.mCmcAccountManager.isSecondaryDevice()) {
            Log.i(IRegistrationManager.LOG_TAG, "CMC phone count 0 : need direct onSimReady");
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(20, new AsyncResult((Object) null, Integer.valueOf(ImsConstants.Phone.SLOT_1), (Throwable) null)), 1000);
        }
    }

    public void suspended(IRegisterTask task, boolean suspended) {
        if (this.mRegStackIf.suspended(task, suspended)) {
            if (getImsIconManager(task.getPhoneId()) != null) {
                getImsIconManager(task.getPhoneId()).updateRegistrationIcon(suspended);
            }
            if (!suspended) {
                if (!this.mHandler.hasMessages(32)) {
                    this.mHandler.sendEmptyMessage(32);
                }
                this.mHandler.sendTryRegister(task.getPhoneId());
            }
        }
        if (!suspended) {
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                this.mHandler.sendTryRegister(task.getPhoneId());
            } else if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                this.mImsFramework.getServiceModuleManager().updateCapabilities(task.getPhoneId());
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: com.sec.ims.settings.ImsProfile} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void buildTask(int r20) {
        /*
            r19 = this;
            r9 = r19
            r10 = r20
            java.lang.String r0 = "RegiMgr"
            java.lang.String r1 = "buildTask:"
            com.sec.internal.log.IMSLog.i(r0, r10, r1)
            boolean r1 = com.sec.internal.ims.core.RegistrationUtils.hasLoadedProfile(r20)
            if (r1 != 0) goto L_0x0017
            java.lang.String r1 = "buildTask: no profile found."
            com.sec.internal.log.IMSLog.i(r0, r10, r1)
            return
        L_0x0017:
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r11 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r20)
            if (r11 != 0) goto L_0x001e
            return
        L_0x001e:
            java.util.List r0 = r9.mSimManagers
            java.lang.Object r0 = r0.get(r10)
            r12 = r0
            com.sec.internal.interfaces.ims.core.ISimManager r12 = (com.sec.internal.interfaces.ims.core.ISimManager) r12
            if (r12 != 0) goto L_0x002a
            return
        L_0x002a:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r13 = r0
            r0 = 0
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r20)
            java.util.List r1 = r1.getProfiles()
            java.util.Iterator r1 = r1.iterator()
        L_0x003d:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x006c
            java.lang.Object r2 = r1.next()
            com.sec.ims.settings.ImsProfile r2 = (com.sec.ims.settings.ImsProfile) r2
            com.sec.internal.ims.core.SlotBasedConfig r3 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r20)
            java.util.Map r3 = r3.getExtendedProfiles()
            int r4 = r2.getId()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.Object r3 = r3.get(r4)
            com.sec.ims.settings.ImsProfile r3 = (com.sec.ims.settings.ImsProfile) r3
            if (r3 == 0) goto L_0x0068
            java.util.List r4 = r3.getExtImpuList()
            r2.setExtImpuList(r4)
        L_0x0068:
            r13.add(r2)
            goto L_0x003d
        L_0x006c:
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r20)
            java.util.Map r1 = r1.getExtendedProfiles()
            java.util.Set r1 = r1.entrySet()
            java.util.Iterator r1 = r1.iterator()
        L_0x007c:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x009e
            java.lang.Object r2 = r1.next()
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2
            java.lang.Object r3 = r2.getValue()
            com.sec.ims.settings.ImsProfile r3 = (com.sec.ims.settings.ImsProfile) r3
            boolean r3 = r9.isAdhocProfile(r3)
            if (r3 == 0) goto L_0x009d
            java.lang.Object r3 = r2.getValue()
            com.sec.ims.settings.ImsProfile r3 = (com.sec.ims.settings.ImsProfile) r3
            r13.add(r3)
        L_0x009d:
            goto L_0x007c
        L_0x009e:
            java.util.Iterator r1 = r11.iterator()
        L_0x00a2:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x00b6
            java.lang.Object r2 = r1.next()
            com.sec.internal.ims.core.RegisterTask r2 = (com.sec.internal.ims.core.RegisterTask) r2
            com.sec.ims.settings.ImsProfile r3 = r2.getProfile()
            r13.remove(r3)
            goto L_0x00a2
        L_0x00b6:
            com.sec.internal.interfaces.ims.IImsFramework r1 = r9.mImsFramework
            r2 = -1
            java.lang.String r3 = "default_rcs_volte_registration"
            int r1 = r1.getInt(r10, r3, r2)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.valueOf((int) r1)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG
            if (r1 == r2) goto L_0x00e6
            android.content.Context r2 = r9.mContext
            java.lang.String r3 = "rcsVolteSingleRegistration"
            java.lang.String r3 = com.sec.internal.ims.util.ImsUtil.getPathWithPhoneId(r3, r10)
            int r4 = r1.getValue()
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            java.lang.Integer r2 = com.sec.internal.ims.config.RcsConfigurationHelper.readIntParam(r2, r3, r4)
            int r2 = r2.intValue()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.valueOf((int) r2)
            r14 = r1
            goto L_0x00e7
        L_0x00e6:
            r14 = r1
        L_0x00e7:
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r20)
            r1.setRcsVolteSingleRegistration(r14)
            java.util.Iterator r15 = r13.iterator()
            r8 = r0
        L_0x00f3:
            boolean r0 = r15.hasNext()
            r1 = 0
            if (r0 == 0) goto L_0x01c9
            java.lang.Object r0 = r15.next()
            r7 = r0
            com.sec.ims.settings.ImsProfile r7 = (com.sec.ims.settings.ImsProfile) r7
            boolean r0 = r7.hasEmergencySupport()
            if (r0 == 0) goto L_0x010a
            r9 = r8
            goto L_0x01c4
        L_0x010a:
            int r0 = r7.getEnableStatus()
            r2 = 2
            java.lang.String r3 = "buildTask: ["
            if (r0 == r2) goto L_0x0133
            com.sec.internal.helper.SimpleEventLog r0 = r9.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r3)
            java.lang.String r2 = r7.getName()
            r1.append(r2)
            java.lang.String r2 = "] - Disabled profile"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r10, r1)
            r9 = r8
            goto L_0x01c4
        L_0x0133:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegistrationType r0 = com.sec.internal.constants.ims.core.RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG
            if (r14 == r0) goto L_0x016a
            boolean r0 = r9.isSingleReg(r14, r10)
            if (r0 == 0) goto L_0x016a
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r7)
            if (r0 == 0) goto L_0x016a
            com.sec.internal.interfaces.ims.config.IConfigModule r0 = r9.mConfigModule
            boolean r0 = r0.isValidConfigDb(r10)
            if (r0 == 0) goto L_0x016a
            r8 = r7
            com.sec.internal.helper.SimpleEventLog r0 = r9.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r3)
            java.lang.String r2 = r7.getName()
            r1.append(r2)
            java.lang.String r2 = "] - RcsVolteSingleRegistration"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r10, r1)
            goto L_0x00f3
        L_0x016a:
            java.lang.String r0 = r7.getMnoName()
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.fromName(r0)
            com.sec.internal.interfaces.ims.IImsFramework r2 = r9.mImsFramework
            java.lang.String r4 = "enable_gba"
            int r1 = r2.getInt(r10, r4, r1)
            boolean r2 = r12.isGBASupported()
            boolean r0 = com.sec.internal.ims.core.RegistrationUtils.isSatisfiedCarrierRequirement(r10, r7, r0, r1, r2)
            if (r0 != 0) goto L_0x01a3
            com.sec.internal.helper.SimpleEventLog r0 = r9.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r3)
            java.lang.String r2 = r7.getName()
            r1.append(r2)
            java.lang.String r2 = "] - Unsatisfying carrier requirement"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r10, r1)
            r9 = r8
            goto L_0x01c4
        L_0x01a3:
            com.sec.internal.ims.core.RegisterTask r16 = new com.sec.internal.ims.core.RegisterTask
            com.sec.internal.helper.os.ITelephonyManager r3 = r9.mTelephonyManager
            com.sec.internal.ims.core.PdnController r4 = r9.mPdnController
            android.content.Context r5 = r9.mContext
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r6 = r9.mVsm
            com.sec.internal.interfaces.ims.config.IConfigModule r2 = r9.mConfigModule
            r0 = r16
            r1 = r7
            r17 = r2
            r2 = r19
            r18 = r7
            r7 = r17
            r9 = r8
            r8 = r20
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8)
            r11.add(r0)
        L_0x01c4:
            r8 = r9
            r9 = r19
            goto L_0x00f3
        L_0x01c9:
            r9 = r8
            if (r9 == 0) goto L_0x01d9
            java.lang.Object r0 = r11.get(r1)
            com.sec.internal.ims.core.RegisterTask r0 = (com.sec.internal.ims.core.RegisterTask) r0
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r0.getGovernor()
            r0.enableRcsOverIms(r9)
        L_0x01d9:
            com.sec.internal.ims.core.-$$Lambda$RegistrationManagerInternal$vtXnljxuJWjLtvywNEHMlWrOaYU r0 = com.sec.internal.ims.core.$$Lambda$RegistrationManagerInternal$vtXnljxuJWjLtvywNEHMlWrOaYU.INSTANCE
            java.util.Collections.sort(r11, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerInternal.buildTask(int):void");
    }

    static /* synthetic */ int lambda$buildTask$0(RegisterTask task1, RegisterTask task2) {
        return task2.mProfile.getPriority() - task1.mProfile.getPriority();
    }

    private boolean isSingleReg(RegistrationConstants.RegistrationType rcsVolteSingleRegistration, int phoneId) {
        return rcsVolteSingleRegistration == RegistrationConstants.RegistrationType.SINGLE_REG || (rcsVolteSingleRegistration == RegistrationConstants.RegistrationType.DUAL_WHEN_ROAMING_REG && !this.mTelephonyManager.isNetworkRoaming(SimUtil.getSubId(phoneId)));
    }

    /* access modifiers changed from: protected */
    public void clearTask(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "clearTask:");
        if (SimUtil.isSoftphoneEnabled()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "skip clearTask for softphone");
            return;
        }
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            List<RegisterTask> removeTask = new ArrayList<>();
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (!RegistrationUtils.isCmcProfile(task.getProfile()) || this.mHandler.hasMessages(42)) {
                    this.mHandler.removeMessages(22, task);
                    task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_SIM_REFRESH);
                    stopPdnConnectivity(task.getPdnType(), task);
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Remove task: " + task);
                    removeTask.add(task);
                    this.mRegStackIf.removeUserAgent(task);
                }
            }
            rtl.removeAll(removeTask);
        }
    }

    /* access modifiers changed from: protected */
    public void tryRegister(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "tryRegister:");
        boolean hasEmergencyTask = RegistrationUtils.pendingHasEmergencyTask(phoneId, getSimManager(phoneId).getSimMno());
        this.mHandler.removeMessages(2, Integer.valueOf(phoneId));
        if (this.mUserEvtCtr.isShuttingDown()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Device is getting shutdown");
        } else if (this.mHandler.hasMessages(36)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Sim refresh is ongoing. retry after 2s");
            this.mHandler.sendTryRegister(phoneId, UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        } else {
            logTask();
            ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
            SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
            Iterator it = rtl.iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (!RegistrationUtils.needToSkipTryRegister(task, this.mRcsPolicyManager.pendingRcsRegister(task, getPendingRegistration(phoneId), phoneId), sm != null && sm.hasNoSim(), this.mHandler.hasMessages(107), this.mTelephonyManager, this.mPdnController)) {
                    RegistrationConstants.RegistrationType rcsVolteSingleRegistration = SlotBasedConfig.getInstance(phoneId).getRcsVolteSingleRegistration();
                    if (rcsVolteSingleRegistration != RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG && task.isRcsOnly() && task.getState() == RegistrationConstants.RegisterTaskState.CONFIGURED) {
                        RegistrationConstants.RegistrationType newRcsVolteSingleRegistration = RegistrationConstants.RegistrationType.valueOf(RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_RCS_VOLTE_SINGLE_REGISTRATION, phoneId), Integer.valueOf(rcsVolteSingleRegistration.getValue())).intValue());
                        SlotBasedConfig.getInstance(phoneId).setRcsVolteSingleRegistration(rcsVolteSingleRegistration);
                        if (isSingleReg(newRcsVolteSingleRegistration, phoneId)) {
                            ImsProfile rcsProfile = task.getProfile();
                            rtl.remove(task);
                            ((RegisterTask) rtl.get(0)).getGovernor().enableRcsOverIms(rcsProfile);
                        }
                    }
                    if (!task.getProfile().hasEmergencySupport() && task.getGovernor().hasEmergencyTaskInPriority(rtl)) {
                        this.mHandler.sendTryRegister(phoneId, 500);
                    } else if (tryRegister(task) && hasEmergencyTask && !getNetworkEvent(task.getPhoneId()).outOfService) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "tryRegister: pending EM regi for the sequential regi of Lab TC.");
                        return;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean tryRegister(RegisterTask task) {
        if (checkForTryRegister(task)) {
            return true;
        }
        int regiFailReason = task.getRegiFailReason();
        if (regiFailReason > DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode() && regiFailReason != task.getLastRegiFailReason()) {
            reportRegistrationStatus(task);
            IMSLog.c(LogClass.REGI_TRY_REGISTER, task.getPhoneId() + "," + task.getMno().getName() + "," + task.getProfile().getPdn() + ",REG FAIL:" + DiagnosisConstants.REGI_FRSN.valueOf(regiFailReason));
        }
        DiagnosisConstants.REGI_FRSN reason = DiagnosisConstants.REGI_FRSN.valueOf(regiFailReason);
        if (reason.isOneOf(DiagnosisConstants.REGI_FRSN.VOPS_OFF, DiagnosisConstants.REGI_FRSN.USER_SETTINGS_OFF, DiagnosisConstants.REGI_FRSN.MAIN_SWITCHES_OFF, DiagnosisConstants.REGI_FRSN.ROAMING_NOT_SUPPORTED, DiagnosisConstants.REGI_FRSN.DATA_RAT_IS_NOT_LTE, DiagnosisConstants.REGI_FRSN.ONGOING_OTA)) {
            IMSLog.lazer((IRegisterTask) task, "NOT_TRIGGERED : " + reason);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkForTryRegister(RegisterTask task) {
        RegisterTask registerTask = task;
        ImsProfile profile = task.getProfile();
        int phoneId = task.getPhoneId();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "checkForTryRegister id: " + profile.getId());
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl == null || !rtl.contains(registerTask)) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, registerTask, "checkForTryRegister UNKNOWN task. (it should be removed task)");
            return false;
        }
        int rat = RegistrationUtils.findBestNetwork(phoneId, profile, task.getGovernor(), isPdnConnected(profile, phoneId), this.mPdnController, this.mVsm, this.mTelephonyManager.getVoiceNetworkType(SimUtil.getSubId(phoneId)), this.mContext);
        registerTask.setRegistrationRat(rat);
        if (!task.getGovernor().isReadyToDualRegister(this.mCmcAccountManager.getCmcRegisterTask(SimUtil.getOppositeSimSlot(task.getPhoneId())) != null && RegistrationUtils.isCmcProfile(task.getProfile()))) {
            this.mHandler.sendTryRegister(phoneId, 2500);
            return false;
        }
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        if (sm == null) {
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.SIMMANAGER_NULL.getCode());
            return false;
        }
        boolean isAirplaneModeOn = ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON;
        boolean isRoaming = this.mTelephonyManager.isNetworkRoaming();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "checkInitialRegistrationIsReady: APM ON [" + isAirplaneModeOn + "], Roamimg [" + isRoaming + "]");
        boolean z = isRoaming;
        boolean z2 = isAirplaneModeOn;
        if (!RegistrationUtils.checkInitialRegistrationIsReady(task, getPendingRegistration(phoneId), isAirplaneModeOn, isRoaming, sm.hasNoSim(), this.mRcsPolicyManager, this.mHandler)) {
            return false;
        }
        if (!task.getGovernor().isReadyToRegister(rat)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "checkForTryRegister: isReadyToRegister = false");
            if (!task.isKeepPdn()) {
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED) && task.getPdnType() == 11) {
                    Log.i(IRegistrationManager.LOG_TAG, "stopPdnConnectivity. IMS PDN should not be established in this case.");
                    stopPdnConnectivity(task.getPdnType(), registerTask);
                    registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
            }
            if (task.getRegiFailReason() == DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode()) {
                registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.GVN_NOT_READY.getCode());
            }
            return false;
        }
        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.RESOLVED, RegistrationConstants.RegisterTaskState.CONFIGURED, RegistrationConstants.RegisterTaskState.CONNECTED)) {
            ISimManager iSimManager = sm;
            if (!RegistrationUtils.checkConfigForInitialRegistration(task, ConfigUtil.isRcsAvailable(this.mContext, phoneId, sm), RegistrationUtils.isCdmConfigured(this.mImsFramework, phoneId), getOmadmState() != RegistrationManager.OmadmConfigState.FINISHED, this.mVsm != null && !this.mVsm.hasEmergencyCall(phoneId), this.mRcsPolicyManager, this.mHandler, this.mNetEvtCtr)) {
                return false;
            }
            int pdn = RegistrationUtils.selectPdnType(profile, rat);
            registerTask.setPdnType(pdn);
            Set<String> services = getServiceForNetwork(profile, rat, ConfigUtil.isRcsEur(phoneId) && task.isRcsOnly(), phoneId);
            if (!checkServicesForInitialRegistration(registerTask, services)) {
                return false;
            }
            return tryInitialRegistration(registerTask, rat, pdn, services);
        }
        if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERING) {
            return true;
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "tryRegister: already registering.");
        registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.ALREADY_REGISTERING.getCode());
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean tryInitialRegistration(RegisterTask task, int rat, int pdn, Set<String> services) {
        RegisterTask registerTask = task;
        int i = rat;
        int i2 = pdn;
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        if (this.mImsFramework.getP2pCC().isEnabledWifiDirectFeature() && tryInitialP2pRegistration(task, rat, pdn, services)) {
            return true;
        }
        if (!this.mPdnController.isConnected(i2, registerTask)) {
            Set<String> set = services;
        } else if (task.getNetworkConnected() == null && !profile.hasEmergencySupport()) {
            Set<String> set2 = services;
        } else if (task.isSuspended()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "tryRegister: network is suspended " + i2 + ". try Register once network is resumed.");
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NETWORK_SUSPENDED.getCode());
            return false;
        } else {
            registerTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            registerTask.setKeepPdn(true);
            if (task.getGovernor().isMobilePreferredForRcs() && i2 == 0) {
                int phoneId2 = task.getPhoneId();
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "tryRegister: startTimsTimer connected pdn = " + i2);
                if (this.mPdnController.translateNetworkBearer(this.mPdnController.getDefaultNetworkBearer()) == 1) {
                    task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
                }
                task.getGovernor().startTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
            }
            this.mPdnController.startPdnConnectivity(i2, registerTask, RegistrationUtils.getPhoneIdForStartConnectivity(task));
            if (task.getGovernor().isReadyToGetReattach()) {
                Log.i(IRegistrationManager.LOG_TAG, "keep pdn and block trying registration. return");
                return false;
            }
            String pcscf = this.mNetEvtCtr.getPcscfIpAddress(registerTask, this.mPdnController.getInterfaceName(registerTask));
            if (TextUtils.isEmpty(pcscf)) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, registerTask, "tryRegister: pcscf is null. return..");
                registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.EMPTY_PCSCF.getCode());
                if (task.getMno() != Mno.KT || profile.getPcscfPreference() == 0) {
                    if (profile.hasEmergencySupport()) {
                        if (task.getMno() == Mno.KDDI) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, Integer.valueOf(phoneId)), 1000);
                        } else {
                            RegistrationUtils.sendEmergencyRegistrationFailed(task);
                        }
                    }
                    this.mEventLog.logAndAdd(phoneId, registerTask, "regi failed due to empty p-cscf");
                    if (task.getPdnType() == 11) {
                        if (task.getMno() == Mno.TMOUS) {
                            stopPdnConnectivity(task.getPdnType(), registerTask);
                            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                            registerTask.setDeregiReason(42);
                            onRegisterError(registerTask, -1, SipErrorBase.EMPTY_PCSCF, 0);
                        }
                        if (task.getMno().isOneOf(Mno.CTC, Mno.CTCMO)) {
                            Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. Notify registration state to CP.");
                            notifyImsNotAvailable(registerTask, false);
                            if (getImsIconManager(phoneId) != null) {
                                Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. fresh icon once.");
                                getImsIconManager(phoneId).updateRegistrationIcon(task.isSuspended());
                            }
                        }
                        if (task.getMno().isOneOf(Mno.CMCC, Mno.CU) && i == 20) {
                            Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. Notify registration state to CP in NR rat.");
                            notifyImsNotAvailable(registerTask, true);
                        }
                    }
                    return false;
                }
                Log.i(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is null. return here for dns query retry");
                return false;
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, registerTask, "tryInitialRegistration on pdn: " + i2 + ". Register now.");
            StringBuilder sb = new StringBuilder();
            sb.append("InitialRegi : rat = ");
            sb.append(i);
            registerTask.setReason(sb.toString());
            registerInternal(registerTask, pcscf, services);
            return true;
        }
        return tryStartPdnConnectivity(registerTask, profile, i, i2);
    }

    private boolean tryInitialP2pRegistration(RegisterTask task, int rat, int pdn, Set<String> services) {
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        int cmcType = profile.getCmcType();
        if (cmcType == 5 || cmcType == 7 || cmcType == 8) {
            IMSLog.d(IRegistrationManager.LOG_TAG, phoneId, task, "tryInitialRegistration, skip pdn connect");
            task.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            task.setKeepPdn(true);
            new ArrayList();
            List<String> pcscfList = profile.getPcscfList();
            if (pcscfList == null || pcscfList.isEmpty()) {
                Log.d(IRegistrationManager.LOG_TAG, "tryRegister: pcscf is invalid");
                return false;
            }
            task.mGovernor.updatePcscfIpList(pcscfList);
            Iterator<String> it = pcscfList.iterator();
            if (it.hasNext()) {
                String pcscf = it.next();
                Log.d(IRegistrationManager.LOG_TAG, "tryRegister: wifi-direct or mobile-hotspot registration: " + pcscf);
                task.setReason("InitialRegi : rat = " + rat);
                registerInternal(task, pcscf, services);
                return true;
            }
        }
        return false;
    }

    private boolean tryStartPdnConnectivity(RegisterTask task, ImsProfile profile, int rat, int pdn) {
        int phoneId = task.getPhoneId();
        if (!RegistrationUtils.hasRcsService(phoneId, profile) || rat == 18 || RegistrationUtils.hasVolteService(phoneId, profile) || RcsUtils.UiUtils.getRcsUserConsent(this.mContext, this.mTelephonyManager, phoneId)) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, task, "tryRegister: connecting to network " + pdn);
            task.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
            if (task.getGovernor().isMobilePreferredForRcs() && pdn == 0) {
                int phoneId2 = task.getPhoneId();
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "tryRegister: startTimsTimer rcs pdn = " + pdn);
                if (this.mPdnController.translateNetworkBearer(this.mPdnController.getDefaultNetworkBearer()) == 1) {
                    task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
                    stopPdnConnectivity(task.getPdnType(), task);
                }
                task.getGovernor().startTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
            }
            this.mPdnController.startPdnConnectivity(pdn, task, RegistrationUtils.getPhoneIdForStartConnectivity(task));
            if (task.getMno().isOneOf(Mno.VZW, Mno.KDDI, Mno.CTCMO, Mno.CTC) || (task.mMno.isKor() && !task.isRcsOnly() && !RegistrationUtils.isCmcProfile(profile))) {
                task.getGovernor().startTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
            }
            IMSLog.lazer((IRegisterTask) task, "PDN REQUEST : " + pdn);
            return true;
        }
        task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.RCS_ONLY_NEEDED.getCode());
        return false;
    }

    private boolean checkServicesForInitialRegistration(RegisterTask task, Set<String> services) {
        int phoneId = task.getPhoneId();
        if (CollectionUtils.isNullOrEmpty((Collection<?>) services)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, task.getProfile().getName() + ": no ims service for current rat" + task.getRegistrationRat());
            NetworkEvent ne = getNetworkEvent(phoneId);
            if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                if (ne != null && !ne.outOfService) {
                    notifyImsNotAvailable(task, false);
                }
                if (task.getMno().isOneOf(Mno.CTC, Mno.CTCMO) && getImsIconManager(phoneId) != null && task.getPdnType() == 11) {
                    Log.i(IRegistrationManager.LOG_TAG, "no ims service. fresh icon once.");
                    getImsIconManager(phoneId).updateRegistrationIcon(task.isSuspended());
                }
            }
            if (!ne.outOfService) {
                stopPdnConnectivity(task.getPdnType(), task);
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            IMSLog.c(LogClass.REGI_FILTERED_ALL_SERVICES, phoneId + ",FILTERED ALL:" + task.getPdnType());
            return false;
        }
        int rat = task.getRegistrationRat();
        if (rat == 0) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "tryRegister: crap. No service?");
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.NETWORK_UNKNOWN.getCode());
            return false;
        } else if (!task.getGovernor().isLocationInfoLoaded(rat)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "location is not loaded");
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.LOCATION_NOT_LOADED.getCode());
            return false;
        } else {
            List<RegisterTask> lowerTasks = RegistrationUtils.getPriorityRegiedTask(false, task);
            if (lowerTasks.isEmpty()) {
                return true;
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "deregi found lowerPriority task " + lowerTasks);
            for (RegisterTask t : lowerTasks) {
                t.setDeregiReason(46);
                deregister(t, false, false, "deregi found lowerPriority task");
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onManualRegister(ImsProfile profile, int phoneId) {
        IGeolocationController geolocationCon;
        ImsProfile imsProfile = profile;
        int i = phoneId;
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onManualRegister: profile " + profile.getName());
        ISimManager sm = (ISimManager) this.mSimManagers.get(i);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (sm != null && rtl != null) {
            this.mImsFramework.getServiceModuleManager().serviceStartDeterminer(this, Collections.singletonList(profile), i);
            Iterator it = rtl.iterator();
            boolean taskExists = false;
            while (it.hasNext()) {
                RegisterTask rt = (RegisterTask) it.next();
                ImsProfile curProfile = rt.getProfile();
                if (curProfile.getCmcType() > 2 && curProfile.getName().equals(profile.getName())) {
                    IMSLog.d(IRegistrationManager.LOG_TAG, "Task with profile name already exists, update imsprofile");
                    rt.setProfile(imsProfile);
                    if (curProfile.getCmcType() == 4 || curProfile.getCmcType() == 8) {
                        IMSLog.d(IRegistrationManager.LOG_TAG, "onManualRegister: releaseThrottle, resetRetry");
                        rt.mGovernor.releaseThrottle(8);
                        rt.mGovernor.resetRetry();
                        rt.mGovernor.updatePcscfIpList(profile.getPcscfList());
                    }
                    taskExists = true;
                }
            }
            SlotBasedConfig.getInstance(phoneId).addExtendedProfile(profile.getId(), imsProfile);
            RegisterTask task = new RegisterTask(profile, this, this.mTelephonyManager, this.mPdnController, this.mContext, this.mVsm, this.mConfigModule, phoneId);
            if (sm.isSimLoaded() || profile.isSoftphoneEnabled() || profile.isSamsungMdmnEnabled()) {
                this.mImsFramework.notifyImsReady(true, i);
                if (!taskExists) {
                    rtl.add(task);
                }
                tryRegister(i);
            }
            if (SimUtil.isSoftphoneEnabled() && (geolocationCon = this.mImsFramework.getGeolocationController()) != null) {
                geolocationCon.startGeolocationUpdate(i, false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void tryEmergencyRegister(RegisterTask task) {
        IMSLog.i(IRegistrationManager.LOG_TAG, task.getPhoneId(), "tryEmergencyRegister:");
        this.mHandler.removeMessages(118);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(task.getPhoneId());
        if (rtl != null) {
            rtl.add(task);
            tryRegister(task.getPhoneId());
        }
    }

    /* access modifiers changed from: protected */
    public void onManualDeregister(int id, boolean explicitDeregi, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onManualDeregister: profile id:" + id + ", explicitDeregi:" + explicitDeregi);
        RegisterTask task = getRegisterTaskByProfileId(id, phoneId);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (task == null || rtl == null) {
            Log.i(IRegistrationManager.LOG_TAG, "onManualDeregister: profile not found.");
            startSilentEmergency();
            return;
        }
        ImsProfile profile = task.getProfile();
        if (RegistrationUtils.needToNotifyImsReady(profile, phoneId)) {
            this.mEventLog.logAndAdd(phoneId, "onManualDeregister: notify IMS ready [false]");
            this.mImsFramework.notifyImsReady(false, phoneId);
        }
        Log.i(IRegistrationManager.LOG_TAG, "onManualDeregister: deregistering profile " + profile.getName());
        task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_MANUAL_DEREGI);
        Optional ofNullable = Optional.ofNullable(task.getGovernor().onManualDeregister(explicitDeregi));
        Objects.requireNonNull(rtl);
        ofNullable.ifPresent(new Consumer() {
            public final void accept(Object obj) {
                SlotBasedConfig.RegisterTaskList.this.remove((IRegisterTask) obj);
            }
        });
        SlotBasedConfig.getInstance(task.getPhoneId()).removeExtendedProfile(profile.getId());
        startSilentEmergency();
    }

    /* access modifiers changed from: protected */
    public void startSilentEmergency() {
        if (this.mHasSilentE911 != null) {
            startEmergencyRegistration(this.mPhoneIdForSilentE911, this.mHasSilentE911);
            this.mHasSilentE911 = null;
            this.mPhoneIdForSilentE911 = -1;
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateRegistration(int phoneId) {
        boolean result = false;
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getPhoneId() == phoneId) {
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    result |= updateRegistration(task, false);
                } else {
                    tryRegister(task);
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean updateRegistration(RegisterTask task, boolean isForceReRegi) {
        boolean triggered = updateRegistration(task, isForceReRegi, false);
        this.mRegStackIf.updatePani(task);
        return triggered;
    }

    /* access modifiers changed from: protected */
    public boolean updateRegistration(RegisterTask task, boolean isForceReRegi, boolean immediately) {
        int phoneId;
        RegisterTask registerTask = task;
        boolean z = isForceReRegi;
        boolean z2 = immediately;
        int phoneId2 = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        int rat = RegistrationUtils.findBestNetwork(phoneId2, profile, task.getGovernor(), isPdnConnected(profile, phoneId2), this.mPdnController, this.mVsm, this.mTelephonyManager.getVoiceNetworkType(SimUtil.getSubId(phoneId2)), this.mContext);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId2, registerTask, "updateRegistration: reason= " + task.getReason() + ", rat=" + rat + ", isForceReRegi=" + z + ", immediately=" + z2);
        NetworkEvent ne = getNetworkEvent(phoneId2);
        if (ne == null) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId2, "updateRegistration: profile=" + profile.getName() + ", NetworkEvent is null");
            return false;
        } else if (task.getGovernor().isNeedToPendingUpdateRegistration(rat, ne.outOfService, z, z2)) {
            this.mEventLog.logAndAdd(phoneId2, registerTask, "updateRegistration: pending");
            return false;
        } else {
            if (task.getGovernor().determineDeRegistration(rat, ne.network)) {
                phoneId = phoneId2;
            } else if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
                ImsProfile imsProfile = profile;
                phoneId = phoneId2;
            } else if (!task.getGovernor().isLocationInfoLoaded(rat)) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "location info is not loaded");
                return false;
            } else {
                if (task.getMno() == Mno.RJIL) {
                    if (this.mRcsPolicyManager.doRcsConfig(registerTask, getPendingRegistration(phoneId2))) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, registerTask, "RCS auto-configuration triggered..");
                        return false;
                    }
                }
                ImsProfile imsProfile2 = profile;
                int i = phoneId2;
                return compareSvcAndDoUpdateRegistration(task, isForceReRegi, immediately, rat, ne);
            }
            this.mEventLog.logAndAdd(phoneId, registerTask, "Stop updateRegistration");
            return false;
        }
    }

    private boolean compareSvcAndDoUpdateRegistration(RegisterTask task, boolean isForceReRegi, boolean immediately, int rat, NetworkEvent ne) {
        RegisterTask registerTask = task;
        int i = rat;
        NetworkEvent networkEvent = ne;
        int previousRat = task.getRegistrationRat();
        registerTask.setRegistrationRat(i);
        int phoneId = task.getPhoneId();
        ImsRegistration reg = SlotBasedConfig.getInstance(phoneId).getImsRegistrations().get(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(task.getProfile().getId(), phoneId)));
        if (reg == null) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, " reg is null for " + task.getProfile().getName());
            return false;
        }
        Set<String> oldSvc = reg.getServices();
        Set<String> newSvc = getServiceForNetwork(task.getProfile(), i, ConfigUtil.isRcsEur(phoneId) && task.isRcsOnly(), phoneId);
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getServiceForNetwork: registered service " + oldSvc);
        if (CollectionUtils.isNullOrEmpty((Collection<?>) newSvc)) {
            registerTask.setReason("empty service list : " + networkEvent.network);
            registerTask.setDeregiReason(72);
            if (!task.isRcsOnly() || phoneId == SimUtil.getDefaultPhoneId()) {
                tryDeregisterInternal(registerTask, false, false);
            } else {
                tryDeregisterInternal(registerTask, true, false);
            }
            return false;
        }
        Set<String> newSvc2 = newSvc;
        if (RegistrationUtils.determineUpdateRegistration(task, previousRat, rat, oldSvc, newSvc, isForceReRegi)) {
            Set<String> deregSvc = new HashSet<>();
            for (String svc : oldSvc) {
                Set<String> newSvc3 = newSvc2;
                if (!newSvc3.contains(svc)) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "updateRegistration: Add to delete service" + svc);
                    deregSvc.add(svc);
                }
                newSvc2 = newSvc3;
            }
            Set<String> newSvc4 = newSvc2;
            if (!deregSvc.isEmpty()) {
                this.mImsFramework.getServiceModuleManager().notifyRcsDeregistering(deregSvc, reg);
            }
            if (task.getImsRegistration() != null) {
                task.getImsRegistration().setCurrentRat(i);
            }
            if (!task.getGovernor().isReadyToDualRegister(this.mCmcAccountManager.getCmcRegisterTask(SimUtil.getOppositeSimSlot(phoneId)) != null && RegistrationUtils.isCmcProfile(task.getProfile()))) {
                if (isForceReRegi) {
                    registerTask.mHasForcedPendingUpdate = true;
                } else {
                    registerTask.mHasPendingUpdate = true;
                }
                if (task.isEpdgHandoverInProgress()) {
                    registerTask.setHasPendingEpdgHandover(true);
                }
                registerTask.setImmediatePendingUpdate(immediately);
                this.mHandler.sendEmptyMessageDelayed(32, 1500);
                return false;
            }
            boolean z = immediately;
            registerTask.setReason("rat = " + task.getRegistrationRat() + "(" + networkEvent.network + "), " + task.getReason());
            registerInternal(registerTask, (String) null, newSvc4);
            return true;
        }
        boolean z2 = immediately;
        Set<String> set = newSvc2;
        if ((task.getProfile().getReregiOnRatChange() == 0 || (task.getProfile().getReregiOnRatChange() == 1 && i != previousRat)) && getImsIconManager(phoneId) != null) {
            boolean removeIconNoSvc = this.mImsFramework.getBoolean(phoneId, GlobalSettingsConstants.Registration.REMOVE_ICON_NOSVC, false);
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "updateRegistration: updateRegistrationIcon: remove_icon_nosvc: " + removeIconNoSvc);
            getImsIconManager(phoneId).updateRegistrationIcon(!removeIconNoSvc && task.isSuspended());
        }
        registerTask.setReason("");
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0090 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0091  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void registerInternal(com.sec.internal.ims.core.RegisterTask r33, java.lang.String r34, java.util.Set<java.lang.String> r35) {
        /*
            r32 = this;
            r0 = r32
            r15 = r33
            int r14 = r33.getPhoneId()
            com.sec.internal.interfaces.ims.core.IUserAgent r1 = r33.getUserAgent()
            java.lang.String r13 = "RegiMgr"
            if (r1 == 0) goto L_0x0030
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = r33.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            if (r1 == r2) goto L_0x0030
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = r33.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.DEREGISTERING
            if (r1 != r2) goto L_0x0027
            java.lang.String r1 = "registerInternal: skip re-register during deregistration"
            com.sec.internal.log.IMSLog.e(r13, r14, r1)
            return
        L_0x0027:
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            java.lang.String r2 = "registerInternal: re-register is not allowed if not registered. Delete UA first."
            r1.logAndAdd(r14, r15, r2)
            return
        L_0x0030:
            java.util.List r12 = r0.getPendingRegistration(r14)
            com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager r1 = r0.mRcsPolicyManager
            boolean r1 = r1.doRcsConfig(r15, r12)
            if (r1 == 0) goto L_0x0042
            java.lang.String r1 = "RCS auto-configuration triggered. Stop."
            com.sec.internal.log.IMSLog.i(r13, r14, r15, r1)
            return
        L_0x0042:
            java.lang.String r17 = r32.getPrivateUserIdentity(r33)
            com.sec.ims.settings.ImsProfile r11 = r33.getProfile()
            int r1 = r33.getPdnType()
            java.lang.String r1 = r0.getInstanceId(r14, r1, r11)
            boolean r2 = r11.isSamsungMdmnEnabled()
            if (r2 == 0) goto L_0x0065
            java.lang.String r2 = r11.getDuid()
            boolean r3 = android.text.TextUtils.isEmpty(r2)
            if (r3 != 0) goto L_0x0065
            r1 = r2
            r10 = r1
            goto L_0x0066
        L_0x0065:
            r10 = r1
        L_0x0066:
            java.lang.String r1 = ""
            java.lang.String r2 = ""
            int r3 = r11.getCmcType()
            if (r3 == 0) goto L_0x0081
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r3 = r0.mCmcAccountManager
            java.lang.String r1 = r3.getCmcSaServerUrl()
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r3 = r0.mCmcAccountManager
            java.lang.String r2 = r3.getCmcRelayType()
            r18 = r1
            r19 = r2
            goto L_0x0085
        L_0x0081:
            r18 = r1
            r19 = r2
        L_0x0085:
            java.util.List r1 = r0.mSimManagers
            java.lang.Object r1 = r1.get(r14)
            r9 = r1
            com.sec.internal.interfaces.ims.core.ISimManager r9 = (com.sec.internal.interfaces.ims.core.ISimManager) r9
            if (r9 != 0) goto L_0x0091
            return
        L_0x0091:
            java.lang.String r8 = r0.getPublicUserIdentity(r15, r9)
            boolean r1 = r0.validateImpu(r15, r8)
            if (r1 != 0) goto L_0x009c
            return
        L_0x009c:
            r7 = r34
            java.lang.String r20 = r0.getInterfaceName(r15, r7, r14)
            java.lang.String r1 = r11.getSipUserAgent()
            java.lang.String r1 = r0.buildUserAgentString(r11, r1, r14)
            r11.setSipUserAgent(r1)
            int r6 = r33.getPdnType()
            java.lang.String r5 = r33.getReason()
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r1 = r33.getGovernor()
            r1.startTimsTimer(r5)
            java.lang.String r1 = ""
            r15.setReason(r1)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = r33.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            if (r1 != r2) goto L_0x00cd
            r1 = 1
            r15.setUpdateRegistering(r1)
        L_0x00cd:
            r4 = 0
            r15.setPendingUpdate(r4)
            r15.setHasForcedPendingUpdate(r4)
            r15.setHasPendingEpdgHandover(r4)
            r15.setImmediatePendingUpdate(r4)
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "registerInternal : "
            r2.append(r3)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r14, r15, r2)
            java.util.List r1 = r33.getFilteredReason()
            java.util.Iterator r1 = r1.iterator()
        L_0x00f9:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x0120
            java.lang.Object r2 = r1.next()
            java.lang.String r2 = (java.lang.String) r2
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r14)
            java.lang.String r3 = ",RMSVC,"
            r4.append(r3)
            r4.append(r2)
            java.lang.String r3 = r4.toString()
            r4 = 285212672(0x11000000, float:1.00974196E-28)
            com.sec.internal.log.IMSLog.c(r4, r3)
            r4 = 0
            goto L_0x00f9
        L_0x0120:
            r1 = 285343745(0x11020001, float:1.0255193E-28)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r14)
            java.lang.String r3 = ",REGI:"
            r2.append(r3)
            r2.append(r6)
            java.lang.String r3 = ":"
            r2.append(r3)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.c(r1, r2)
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            java.util.Set r2 = r11.getAllServiceSetFromAllNetwork()
            boolean r2 = com.sec.internal.ims.rcs.util.RcsUtils.isAutoConfigNeeded(r2)
            if (r2 == 0) goto L_0x015e
            com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager r2 = r0.mRcsPolicyManager
            com.sec.internal.constants.Mno r3 = r9.getSimMno()
            android.os.Bundle r1 = r2.getRcsConfigForUserAgent(r11, r3, r6, r14)
            r21 = r1
            goto L_0x0160
        L_0x015e:
            r21 = r1
        L_0x0160:
            com.sec.internal.interfaces.ims.core.handler.IRegistrationInterface r1 = r0.mRegStackIf
            com.sec.ims.options.Capabilities r22 = r0.getOwnCapabilities(r11, r14)
            java.lang.String r23 = r0.getHomeNetworkDomain(r11, r14)
            java.lang.String r24 = r0.getUuid(r14, r11)
            java.util.List r4 = r0.mThirdPartyFeatureTags
            boolean r25 = r0.isVoWiFiSupported(r14)
            r2 = r33
            r3 = r20
            r16 = r4
            r4 = r34
            r26 = r5
            r5 = r35
            r27 = r6
            r6 = r22
            r7 = r23
            r22 = r8
            r23 = r9
            r9 = r17
            r28 = r10
            r29 = r11
            r11 = r24
            r24 = r12
            r12 = r18
            r30 = r13
            r13 = r19
            r31 = r14
            r14 = r16
            r15 = r21
            r16 = r25
            boolean r1 = r1.registerInternal(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16)
            if (r1 != 0) goto L_0x01cd
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            java.lang.String r2 = "registerInternal: failed to create UserAgent."
            r3 = r33
            r4 = r31
            r1.logAndAdd(r4, r3, r2)
            r33.clearUserAgent()
            com.sec.internal.ims.core.PdnController r1 = r0.mPdnController
            r2 = r27
            boolean r1 = r1.isConnected(r2, r3)
            if (r1 == 0) goto L_0x01c7
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            r3.setState(r1)
            goto L_0x01d3
        L_0x01c7:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            r3.setState(r1)
            goto L_0x01d3
        L_0x01cd:
            r3 = r33
            r2 = r27
            r4 = r31
        L_0x01d3:
            boolean r1 = com.sec.internal.ims.core.RegistrationUtils.isCmcProfile(r29)
            if (r1 == 0) goto L_0x0235
            java.lang.String r1 = "start p2p in registerInternal"
            r5 = r30
            android.util.Log.i(r5, r1)
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r1 = r0.mVsm
            if (r1 == 0) goto L_0x0232
            com.sec.internal.interfaces.ims.core.IUserAgent r1 = r33.getUserAgent()
            if (r1 == 0) goto L_0x0232
            r1 = r17
            java.lang.String r6 = "@"
            boolean r7 = r1.contains(r6)
            if (r7 == 0) goto L_0x01fe
            int r6 = r1.indexOf(r6)
            r7 = 0
            java.lang.String r1 = r1.substring(r7, r6)
        L_0x01fe:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "lineId : "
            r6.append(r7)
            r6.append(r1)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r5, r6)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "deviceId : "
            r6.append(r7)
            r7 = r28
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r5, r6)
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r5 = r0.mVsm
            com.sec.internal.interfaces.ims.servicemodules.volte2.ICmcServiceHelper r5 = r5.getCmcServiceHelper()
            r5.startP2p(r7, r1)
            goto L_0x0237
        L_0x0232:
            r7 = r28
            goto L_0x0237
        L_0x0235:
            r7 = r28
        L_0x0237:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerInternal.registerInternal(com.sec.internal.ims.core.RegisterTask, java.lang.String, java.util.Set):void");
    }

    private Capabilities getOwnCapabilities(ImsProfile profile, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getOwnCapabilities:");
        ICapabilityDiscoveryModule cdm = this.mImsFramework.getServiceModuleManager().getCapabilityDiscoveryModule();
        Integer version = this.mConfigModule.getRcsConfVersion(phoneId);
        if (!profile.getNeedAutoconfig() || cdm == null || !cdm.isRunning() || version == null || version.intValue() <= 0 || (!RcsUtils.DualRcs.isDualRcsReg() && phoneId != SimUtil.getDefaultPhoneId())) {
            Capabilities ownCap = new Capabilities();
            if (this.mVsm != null) {
                ownCap.setFeatures(this.mVsm.getSupportFeature(phoneId));
            }
            if (!RegistrationUtils.isCmcProfile(profile)) {
                return ownCap;
            }
            ownCap.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
            Log.i(IRegistrationManager.LOG_TAG, "getOwnCapabilities : add mmtel to Capabilities for CMC-REGI");
            return ownCap;
        }
        Capabilities ownCap2 = cdm.getOwnCapabilitiesBase(phoneId);
        if (ownCap2 != null) {
            return ownCap2;
        }
        Log.i(IRegistrationManager.LOG_TAG, "getOwnCapabilities: ownCap is null, create empty Capabilities");
        return new Capabilities();
    }

    /* access modifiers changed from: protected */
    public void tryEmergencyRegister(int phoneId, ImsProfile profile, Message result, boolean isNoSim) {
        if (profile.getPdnType() == 11) {
            for (ImsRegistration r : getRegistrationInfo()) {
                if (r.getImsProfile().getPdnType() == 11) {
                    Log.i(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: Emergency is supported via IMS PDN");
                    result.sendToTarget();
                    return;
                }
            }
        }
        RegisterTask task = new RegisterTask(profile, this, this.mTelephonyManager, this.mPdnController, this.mContext, this.mVsm, this.mConfigModule, phoneId);
        task.setResultMessage(result);
        task.setProfile(profile);
        if (task.getMno() == Mno.ATT) {
            task.mKeepPdn = true;
        }
        if (isNoSim && (task.getMno() == Mno.TELSTRA || task.getMno().isCanada())) {
            this.mRegStackIf.configure(phoneId);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(118, task));
    }

    /* access modifiers changed from: protected */
    public void tryDeregisterInternal(IRegisterTask task, boolean local, boolean keepPdn) {
        task.setKeepPdn(keepPdn);
        IUserAgent ua = task.getUserAgent();
        ImsRegistration reg = SlotBasedConfig.getInstance(task.getPhoneId()).getImsRegistrations().get(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(task.getProfile().getId(), task.getPhoneId())));
        if (reg != null) {
            reg.setDeregiReason(task.getDeregiReason());
            this.mImsFramework.getServiceModuleManager().notifyDeregistering(reg);
        }
        if (task.getGovernor().isNeedDelayedDeregister() || ((task.getProfile().getCmcType() == 1 || (ua != null && ua.getSuspendState())) && !local)) {
            task.getGovernor().setNeedDelayedDeregister(false);
            this.mHandler.requestDelayedDeRegister(task, local, 300);
            return;
        }
        deregisterInternal(task, local);
    }

    /* access modifiers changed from: protected */
    public void deregisterInternal(IRegisterTask task, boolean local) {
        int phoneId = task.getPhoneId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, task, "deregisterInternal: local=" + local + " reason=" + task.getReason());
        if (this.mHandler.hasMessages(145)) {
            this.mHandler.removeMessages(145);
        }
        if (task.getUserAgent() == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, task, "deregister: ua is null");
            if (task.getMno() == Mno.KDDI || !task.getProfile().hasEmergencySupport()) {
                if (this.mPdnController.isConnected(task.getPdnType(), task)) {
                    task.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
                } else {
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
                this.mHandler.sendTryRegister(phoneId, 500);
                return;
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, task, "deregister: this task will be deleted. do nothing");
            return;
        }
        this.mRegStackIf.deregisterInternal(task, local);
        task.setReason("");
        this.mHandler.setDeregisterTimeout(task);
        if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING || (task.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY && task.needKeepEmergencyTask())) {
            task.setState(RegistrationConstants.RegisterTaskState.DEREGISTERING);
        }
        this.mHandler.removeMessages(100, task);
    }

    /* access modifiers changed from: protected */
    public void onRegistered(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        if (task.getUserAgent() == null) {
            this.mEventLog.logAndAdd(phoneId, task, "onRegistered: Failed to process. UA has already removed");
        } else if (!this.mRegStackIf.isUserAgentInRegistered(task)) {
            this.mEventLog.logAndAdd(phoneId, task, "onRegistered: Failed to process. UA is not registered!");
        } else {
            ImsRegistration reg = task.getImsRegistration();
            reg.setRegiRat(task.getRegistrationRat());
            reg.setCurrentRat(task.getRegistrationRat());
            ImsProfile profile = task.getProfile();
            SlotBasedConfig.getInstance(phoneId).addImsRegistration(IRegistrationManager.getRegistrationInfoId(profile.getId(), phoneId), reg);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, task, "onRegistered: RAT = " + task.getRegistrationRat() + ", profile=" + profile.getName() + ", service=" + Arrays.toString(reg.getServices().toArray()));
            IMSLog.c(LogClass.REGI_REGISTERED, phoneId + ",REG OK:" + task.getRegistrationRat() + ":" + task.getMno().getName() + ":" + profile.getPdn() + ":" + DiagnosisConstants.convertServiceSetToHex(reg.getServices()));
            if (ImsGateConfig.isGateEnabled()) {
                IMSLog.g("GATE", "<GATE-M>IMS_ENABLED_PS_IND_" + SemSystemProperties.get(ImsConstants.SystemProperties.PS_INDICATOR) + "</GATE-M>");
            }
            task.setState(RegistrationConstants.RegisterTaskState.REGISTERED);
            task.clearUpdateRegisteringFlag();
            task.setIsRefreshReg(false);
            UriGeneratorFactory.getInstance().updateUriGenerator(reg, this.mRcsPolicyManager.getRcsNetworkUriType(phoneId, profile.getRemoteUriType(), profile.getNeedAutoconfig()));
            RegistrationUtils.updateImsIcon(task);
            notifyImsRegistration(reg, true, task, new ImsRegistrationError());
            if (task.getProfile().hasEmergencySupport() && task.getResultMessage() != null) {
                task.getResultMessage().sendToTarget();
                task.setResultMessage((Message) null);
            }
            if (!SimUtil.isSoftphoneEnabled()) {
                RegistrationUtils.saveRegisteredImpu(this.mContext, reg, getSimManager(phoneId));
            } else {
                this.mAresLookupRequired = true;
            }
            task.getGovernor().onRegistrationDone();
            if (SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS())) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Integer.valueOf(ImsConstants.Phone.SLOT_1)));
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Integer.valueOf(ImsConstants.Phone.SLOT_2)));
            } else {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Integer.valueOf(phoneId)));
            }
            this.mHandler.sendEmptyMessage(32);
            reportRegistrationStatus(task);
            if (task.getRegiRequestType() != DiagnosisConstants.REGI_REQC.REFRESH) {
                reportRegistrationCount(task);
            }
            reportDualImsStatus(phoneId);
            IMSLog.lazer(task, ImsConstants.Intents.EXTRA_REGISTERED);
            task.setReason("");
            task.setEpdgHandoverInProgress(false);
            task.setRegiRequestType(DiagnosisConstants.REGI_REQC.REFRESH);
            task.setDeregiReason(41);
        }
    }

    /* access modifiers changed from: protected */
    public void onRegisterError(IRegisterTask task, int handle, SipError error, int retryAfter) {
        int phoneId = task.getPhoneId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, task, "onRegisterError: error " + error + " retryAfter " + retryAfter);
        IMSLog.c(LogClass.REGI_REGISTER_ERROR, task.getPhoneId() + ",REG ERR:" + task.getMno().getName() + ":" + task.getProfile().getPdn() + ":" + error + ":" + retryAfter);
        task.setEpdgHandoverInProgress(false);
        if (!SipErrorBase.UNAUTHORIZED.equals(error) || task.isRcsOnly()) {
            if ((task.getMno() == Mno.KDDI || task.getMno().isKor()) && (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || task.isRefreshReg())) {
                task.setIsRefreshReg(true);
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: mIsRefreshReg " + task.isRefreshReg());
            } else if (this.mPdnController.isConnected(task.getPdnType(), task)) {
                task.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            } else {
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            removeAdhocProfile(phoneId, task);
            makeThrottle(phoneId, task);
            try {
                if (task.getProfile().hasEmergencySupport()) {
                    if (task.getMno() == Mno.VZW) {
                        if (SipErrorBase.SIP_TIMEOUT.equals(error)) {
                            task.getGovernor().onRegistrationError(error, retryAfter, false);
                            if (task.getGovernor().getFailureCount() < 2) {
                                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: Emergency Registration timed out. Retry.");
                                return;
                            }
                        }
                    } else if (task.getMno() == Mno.KDDI) {
                        task.getGovernor().onRegistrationError(error, retryAfter, false);
                        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: Emergency Registration Error Retry Infinitely.");
                        this.mRegStackIf.onRegisterError(task, handle, error, retryAfter);
                        return;
                    }
                    RegistrationUtils.sendEmergencyRegistrationFailed(task);
                    this.mRegStackIf.onRegisterError(task, handle, error, retryAfter);
                    return;
                }
                task.getGovernor().onRegistrationError(error, retryAfter, false);
                int deregiReason = task.getGovernor().getFailureType();
                int detailedDeregiReason = RegistrationUtils.getDetailedDeregiReason(deregiReason);
                if (task.getDeregiCause(error) == 32) {
                    deregiReason = 32;
                }
                if (deregiReason != 16) {
                    task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_IMS_NOT_AVAILABLE);
                }
                boolean epdgState = this.mPdnController.isEpdgConnected(phoneId);
                if (task.getImsRegistration() != null) {
                    epdgState = task.getImsRegistration().getEpdgStatus();
                }
                notifyImsRegistration(ImsRegistration.getBuilder().setHandle(handle).setImsProfile(new ImsProfile(task.getProfile())).setServices(task.getProfile().getServiceSet(Integer.valueOf(task.getRegistrationRat()))).setEpdgStatus(epdgState).setPdnType(task.getPdnType()).setUuid(getUuid(phoneId, task.getProfile())).setInstanceId(getInstanceId(phoneId, task.getPdnType(), task.getProfile())).setNetwork(task.getNetworkConnected()).setRegiRat(task.getRegistrationRat()).setPhoneId(phoneId).build(), false, task, new ImsRegistrationError(error.getCode(), error.getReason(), detailedDeregiReason, deregiReason));
                reportRegistrationStatus(task);
                reportRegistrationCount(task);
                IMSLog.lazer(task, "REGISTRATION FAILED : " + error);
                this.mRegStackIf.onRegisterError(task, handle, error, retryAfter);
            } finally {
                this.mRegStackIf.onRegisterError(task, handle, error, retryAfter);
            }
        }
    }

    private void removeAdhocProfile(int phoneId, IRegisterTask task) {
        boolean found = false;
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (sm != null && rtl != null) {
            if (!task.getMno().isAus() || !task.getProfile().hasEmergencySupport()) {
                for (ImsProfile profile : SlotBasedConfig.getInstance(phoneId).getProfiles()) {
                    if (profile.getId() == task.getProfile().getId()) {
                        found = true;
                    }
                }
            } else if (((Integer) Optional.ofNullable((ImsProfile) this.mAuEmergencyProfile.get(phoneId)).map($$Lambda$XX8uNPsmy4cJ_ATQztOoFCwoEvU.INSTANCE).orElse(-1)).intValue() == task.getProfile().getId()) {
                found = true;
            }
            if (!found && !SlotBasedConfig.getInstance(phoneId).getExtendedProfiles().containsKey(Integer.valueOf(task.getProfile().getId()))) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onDeregisterd: remove RegisterTask: " + task.getProfile().getName());
                rtl.remove(task);
            }
        }
    }

    private void makeThrottle(int phoneId, IRegisterTask task) {
        if (task.getProfile().getCmcType() == 8) {
            IMSLog.d(IRegistrationManager.LOG_TAG, phoneId, "onRegisterError: don't retry register");
            task.getGovernor().makeThrottle();
        }
    }

    /* access modifiers changed from: protected */
    public void onDeregistered(IRegisterTask task, boolean requested, SipError error, int retryAfter) {
        int phoneId = task.getPhoneId();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, task, "onDeregistered: rat=" + task.getRegistrationRat() + ", requested=" + requested + ", reason=" + task.getDeregiReason() + ", error=" + error + ", retryAfter=" + retryAfter + ", keepPdn=" + task.isKeepPdn());
        if (ImsGateConfig.isGateEnabled()) {
            IMSLog.g("GATE", "<GATE-M>IMS_DISABLED</GATE-M>");
        }
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl != null) {
            if (task.getMno().isKor() && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                task.setIsRefreshReg(true);
            }
            task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.OFFSET_DEREGI_REASON.getCode() + task.getDeregiReason());
            reportRegistrationStatus(task);
            IMSLog.lazer(task, "DE-REGISTERED : " + task.getDeregiReason());
            ImsRegistration reg = SlotBasedConfig.getInstance(phoneId).getImsRegistrations().remove(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(task.getProfile().getId(), phoneId)));
            task.clearUpdateRegisteringFlag();
            if (!task.getMno().isKor() || !task.isRefreshReg() || requested) {
                task.setIsRefreshReg(false);
            }
            RegistrationUtils.updateImsIcon(task);
            if (reg != null) {
                int deregiReason = task.getDeregiCause(error);
                notifyImsRegistration(reg, false, task, new ImsRegistrationError(error.getCode(), error.getReason(), task.getDeregiReason(), deregiReason));
                if (deregiReason == 32) {
                    task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_IMS_NOT_AVAILABLE);
                    Log.i(IRegistrationManager.LOG_TAG, "ImsNotAvailable has sent by onDeregistered.");
                    SlotBasedConfig.getInstance(phoneId).setNotifiedImsNotAvailable(true);
                }
            }
            if (task.getMno().isChn() && this.mIsNonDDSDeRegRequired) {
                sendDeregister(12, SimUtil.getOppositeSimSlot(phoneId));
                this.mIsNonDDSDeRegRequired = false;
            }
            if (task.getProfile().hasEmergencySupport()) {
                if (this.mMoveNextPcscf) {
                    task.getProfile().setUicclessEmergency(true);
                    task.getGovernor().increasePcscfIdx();
                    this.mMoveNextPcscf = false;
                } else {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onDeregistered: leave it to EMERGENCY state.");
                    task.setState(RegistrationConstants.RegisterTaskState.EMERGENCY);
                    task.setIsRefreshReg(false);
                    if (task.getMno() == Mno.KDDI) {
                        this.mRegStackIf.onDeregistered(task, requested, error, retryAfter);
                        return;
                    }
                    return;
                }
            }
            if (this.mPdnController.isConnected(task.getPdnType(), task)) {
                task.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
            } else if (task.isKeepPdn()) {
                task.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
            } else {
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
            task.setImsRegistration((ImsRegistration) null);
            removeAdhocProfile(rtl, task);
            if (requested) {
                handleSolicitedDeregistration(task, error);
            } else {
                handleUnSolicitedDeregistration(task, error, retryAfter);
            }
            IMSLog.c(LogClass.REGI_DEREGISTERED, phoneId + ",DEREG:" + task.getMno().getName() + ":" + task.getProfile().getPdn() + ":" + task.getState());
            task.setReason("");
            task.getGovernor().onDeregistrationDone(requested);
            task.setDeregiReason(41);
            task.setIsRefreshReg(false);
            this.mRegStackIf.onDeregistered(task, requested, error, retryAfter);
        }
    }

    private void handleSolicitedDeregistration(IRegisterTask task, SipError error) {
        if (!task.isKeepPdn()) {
            if (task.getMno() == Mno.GCF && task.getProfile().getPdn().equals(DeviceConfigManager.IMS) && (task.getDeregiCause(error) == 2 || task.getDeregiReason() == 73)) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(133, task), 500);
            } else {
                stopPdnConnectivity(task.getPdnType(), task);
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                if (!task.getMno().isKor() || !task.isRcsOnly()) {
                    setOmadmState(RegistrationManager.OmadmConfigState.IDLE);
                }
            }
        }
        tryNextRegistration(task, task.getDeregiReason());
    }

    private void handleUnSolicitedDeregistration(IRegisterTask task, SipError error, int retryAfter) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int phoneId = task.getPhoneId();
        simpleEventLog.logAndAdd(phoneId, task, "onDeregistered: registration error = " + error);
        task.getGovernor().onRegistrationError(error, retryAfter, true);
        if (!this.mPdnController.isConnected(task.getPdnType(), task)) {
            task.getGovernor().resetPcscfList();
        } else {
            task.setKeepPdn(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPendingUpdateRegistration() {
        this.mHandler.removeMessages(32);
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && task.mHasForcedPendingUpdate) {
                    Log.i(IRegistrationManager.LOG_TAG, "onPendingUpdateRegistration: forced " + task.getProfile().getName());
                    task.mHasForcedPendingUpdate = false;
                    if (task.hasPendingEpdgHandover()) {
                        task.setHasPendingEpdgHandover(false);
                        task.setEpdgHandoverInProgress(true);
                    }
                    if (task.isImmediatePendingUpdate()) {
                        updateRegistration(task, true, true);
                    } else {
                        updateRegistration(task, true);
                    }
                } else if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && task.mHasPendingUpdate) {
                    Log.i(IRegistrationManager.LOG_TAG, "onPendingUpdateRegistration: " + task.getProfile().getName());
                    task.mHasPendingUpdate = false;
                    if (task.hasPendingEpdgHandover()) {
                        task.setHasPendingEpdgHandover(false);
                        task.setEpdgHandoverInProgress(true);
                    }
                    if (task.isImmediatePendingUpdate()) {
                        updateRegistration(task, false, true);
                    } else {
                        updateRegistration(task, false);
                    }
                } else if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && task.hasPendingDeregister()) {
                    Log.i(IRegistrationManager.LOG_TAG, "onPendingDeRegistration: " + task.getProfile().getName());
                    task.setHasPendingDeregister(false);
                    tryDeregisterInternal(task, false, true);
                }
            }
        }
    }

    private void removeAdhocProfile(SlotBasedConfig.RegisterTaskList rtl, IRegisterTask task) {
        boolean isInternalProfile = false;
        Iterator<ImsProfile> it = SlotBasedConfig.getInstance(task.getPhoneId()).getProfiles().iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().getId() == task.getProfile().getId()) {
                    isInternalProfile = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (!isInternalProfile && !task.getProfile().hasEmergencySupport() && !SlotBasedConfig.getInstance(task.getPhoneId()).getExtendedProfiles().containsKey(Integer.valueOf(task.getProfile().getId()))) {
            int phoneId = task.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onDeregisterd: Remove RegiTask for [" + task.getProfile().getName() + "]");
            rtl.remove(task);
        }
    }

    private void tryNextRegistration(IRegisterTask task, int reason) {
        if (task.getPdnType() == 11) {
            int delay = 0;
            if (reason == 2 || reason == 24) {
                delay = ImSessionEvent.MESSAGING_EVENT;
                task.getGovernor().addDelay(ImSessionEvent.MESSAGING_EVENT);
            } else if (reason == 27) {
                delay = 1000;
                task.getGovernor().addDelay(1000);
            } else if (reason == 21 && task.isKeepPdn()) {
                delay = ImSessionEvent.MESSAGING_EVENT;
                task.getGovernor().addDelay(ImSessionEvent.MESSAGING_EVENT);
            }
            if (task.getMno() == Mno.KDDI) {
                this.mHandler.sendEmptyMessageDelayed(32, (long) (delay + 100));
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, Integer.valueOf(task.getPhoneId())), (long) (delay + 100));
                return;
            }
            this.mHandler.sendEmptyMessageDelayed(32, (long) (delay + 300));
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, Integer.valueOf(task.getPhoneId())), (long) (delay + 300));
            return;
        }
        this.mHandler.sendEmptyMessage(32);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Integer.valueOf(task.getPhoneId())));
    }

    /* access modifiers changed from: protected */
    public void onSubscribeError(IRegisterTask task, SipError error) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int phoneId = task.getPhoneId();
        simpleEventLog.logAndAdd(phoneId, task, "onSubscribeError: error " + error);
        task.getGovernor().onSubscribeError(0, error);
    }

    /* access modifiers changed from: protected */
    public void onForcedUpdateRegistrationRequested(RegisterTask task) {
        task.setReason("forced update registration");
        updateRegistration(task, true);
    }

    /* access modifiers changed from: protected */
    public void onRefreshRegistration(IRegisterTask task, int handle) {
        int phoneId = task.getPhoneId();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onRefreshRegistration: profile " + task.getProfile().getName() + " handle : " + handle);
        if (!SimUtil.isMultiSimSupported()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, task.getPhoneId(), "This model is not for Dual IMS.");
            return;
        }
        if (this.mVsm != null) {
            for (int phoneId2 = 0; phoneId2 < this.mSimManagers.size(); phoneId2++) {
                Iterator it = SlotBasedConfig.getInstance(phoneId2).getRegistrationTasks().iterator();
                while (it.hasNext()) {
                    RegisterTask t = (RegisterTask) it.next();
                    if (t.mReg != null && t.mReg.getHandle() != handle && this.mVsm.getSessionCount(phoneId2) > 0 && !this.mVsm.hasEmergencyCall(phoneId2) && this.mVsm.hasActiveCall(phoneId2)) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "Active VoLTE call exists on this slot. Try to de-regi.");
                        tryDeregisterInternal(task, true, true);
                        return;
                    }
                }
            }
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, task.getPhoneId(), "onRefreshRegistration: No de-registration has triggered");
    }

    public void sendReRegister(RegisterTask task) {
        this.mHandler.notifySendReRegisterRequested(task);
    }

    /* access modifiers changed from: protected */
    public void setDelayedDeregisterTimerRunning(IRegisterTask task, boolean delayedDeregisterTimerRunning) {
        if (task.getProfile().hasService("mmtel") || task.getProfile().hasService("mmtel-video")) {
            this.mVsm.setDelayedDeregisterTimerRunning(task.getPhoneId(), delayedDeregisterTimerRunning);
        }
        if (task.getProfile().hasService("smsip")) {
            this.mImsFramework.getServiceModuleManager().getSmsServiceModule().setDelayedDeregisterTimerRunning(task.getPhoneId(), delayedDeregisterTimerRunning);
        }
    }
}
