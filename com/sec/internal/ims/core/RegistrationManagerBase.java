package com.sec.internal.ims.core;

import android.os.Bundle;
import android.os.Message;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.entitlement.nsds.NSDSSimEventManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class RegistrationManagerBase extends RegistrationManagerInternal {
    public /* bridge */ /* synthetic */ void initSequentially() {
        super.initSequentially();
    }

    public /* bridge */ /* synthetic */ void sendReRegister(RegisterTask registerTask) {
        super.sendReRegister(registerTask);
    }

    public /* bridge */ /* synthetic */ void suspended(IRegisterTask iRegisterTask, boolean z) {
        super.suspended(iRegisterTask, z);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public RegistrationManagerBase(android.os.Looper r15, com.sec.internal.interfaces.ims.IImsFramework r16, android.content.Context r17, com.sec.internal.ims.core.PdnController r18, java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r19, com.sec.internal.helper.os.ITelephonyManager r20, com.sec.internal.interfaces.ims.core.ICmcAccountManager r21, com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager r22) {
        /*
            r14 = this;
            r12 = r14
            r0 = r14
            r1 = r16
            r2 = r17
            r3 = r18
            r4 = r19
            r5 = r20
            r6 = r21
            r7 = r22
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            com.sec.internal.ims.core.NetworkEventController r9 = new com.sec.internal.ims.core.NetworkEventController
            com.sec.internal.interfaces.ims.IImsFramework r8 = r12.mImsFramework
            r0 = r9
            r1 = r17
            r2 = r18
            r3 = r20
            r5 = r21
            r6 = r22
            r7 = r14
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8)
            r12.mNetEvtCtr = r9
            com.sec.internal.ims.core.UserEventController r6 = new com.sec.internal.ims.core.UserEventController
            com.sec.internal.helper.SimpleEventLog r5 = r12.mEventLog
            r0 = r6
            r2 = r14
            r3 = r19
            r4 = r20
            r0.<init>(r1, r2, r3, r4, r5)
            r12.mUserEvtCtr = r6
            com.sec.internal.ims.core.RegistrationManagerHandler r13 = new com.sec.internal.ims.core.RegistrationManagerHandler
            com.sec.internal.ims.core.NetworkEventController r9 = r12.mNetEvtCtr
            com.sec.internal.ims.core.UserEventController r10 = r12.mUserEvtCtr
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r11 = r12.mVsm
            r0 = r13
            r1 = r15
            r2 = r17
            r3 = r14
            r4 = r16
            r5 = r18
            r6 = r19
            r7 = r20
            r8 = r21
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
            r12.mHandler = r13
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerBase.<init>(android.os.Looper, com.sec.internal.interfaces.ims.IImsFramework, android.content.Context, com.sec.internal.ims.core.PdnController, java.util.List, com.sec.internal.helper.os.ITelephonyManager, com.sec.internal.interfaces.ims.core.ICmcAccountManager, com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager):void");
    }

    public void setThirdPartyFeatureTags(String[] featureTags) {
        this.mThirdPartyFeatureTags = Arrays.asList(featureTags);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(126, SimUtil.getDefaultPhoneId(), 0, (Object) null));
    }

    public void registerProfile(List<Integer> profileIds, int phoneId) {
        Log.i(IRegistrationManager.LOG_TAG, "registerProfile: " + profileIds);
        this.mHandler.notifyManualRegisterRequested(profileIds, phoneId);
    }

    public void deregisterProfile(List<Integer> profileIds, boolean disconnectPdn, int phoneId) {
        Log.i(IRegistrationManager.LOG_TAG, "deregisterProfile: " + profileIds + " disconnectPdn=" + disconnectPdn);
        this.mHandler.notifyManualDeRegisterRequested(profileIds, disconnectPdn, phoneId);
    }

    public int registerProfile(ImsProfile profile, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "registerProfile: profile=" + profile.toString());
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        if (sm == null) {
            return -1;
        }
        if (sm.hasVsim() && SlotBasedConfig.getInstance(phoneId).getIconManager() == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "registerProfile: added iconmanager.");
            SlotBasedConfig.getInstance(phoneId).createIconManager(this.mContext, this, this.mPdnController, sm.getSimMno(), phoneId);
        }
        return this.mHandler.notifyManualRegisterRequested(profile, sm.hasVsim(), phoneId);
    }

    public void deregisterProfile(int id, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "deregisterProfile: handle:" + id);
        this.mHandler.notifyManualDeRegisterRequested(id, phoneId);
    }

    public void deregisterProfile(int id, int phoneId, boolean disconnectPdn) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "deregisterProfile: handle:" + id + ", disconnectPdn: " + disconnectPdn);
        this.mHandler.notifyManualDeRegisterRequested(id, phoneId, disconnectPdn);
    }

    public int updateRegistration(ImsProfile profile, int phoneId) {
        Log.i(IRegistrationManager.LOG_TAG, "updateRegistration: profile=" + profile);
        return this.mHandler.notifyUpdateRegisterRequested(profile, phoneId);
    }

    public int forcedUpdateRegistration(ImsProfile profile, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "forcedUpdateRegistration: profile=" + profile);
        if (profile == null) {
            return -1;
        }
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getProfile().getId() == profile.getId()) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, task, "start updateRegistration");
                this.mHandler.requestForcedUpdateRegistration(task);
                return 0;
            }
        }
        return -1;
    }

    public void doPendingUpdateRegistration() {
        this.mHandler.removeMessages(32);
        this.mHandler.sendEmptyMessage(32);
    }

    public void bootCompleted() {
        this.mHandler.removeMessages(150);
        this.mHandler.sendEmptyMessage(150);
    }

    public void deregister(IRegisterTask task, boolean local, boolean keepPdnConnection, String reason) {
        Preconditions.checkNotNull(task);
        deregister(task, local, keepPdnConnection, 0, reason);
    }

    public void deregister(IRegisterTask task, boolean local, boolean keepPdnConnection, int delay, String reason) {
        Preconditions.checkNotNull(task);
        task.setReason(reason);
        Log.i(IRegistrationManager.LOG_TAG, "deregister: task=" + task + " local=" + local + " keepPdn=" + keepPdnConnection + " delay=" + delay + " reason=" + reason);
        this.mHandler.requestPendingDeregistration(task, local, keepPdnConnection, (long) delay);
    }

    public void sendDeregister(int cause) {
        for (ISimManager sm : this.mSimManagers) {
            sendDeregister(cause, sm.getSimSlotIndex());
        }
    }

    public void sendDeregister(int reason, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "sendDeregister: reason=" + reason);
        this.mHandler.notifySendDeRegisterRequested(SimUtil.getMno(), reason, phoneId);
    }

    public void sendDeregister(IRegisterTask task, long delay) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(128, task), delay);
    }

    public boolean isSuspended(int handle) {
        RegisterTask task = getRegisterTaskByRegHandle(handle);
        if (task != null) {
            return task.isSuspended();
        }
        Log.e(IRegistrationManager.LOG_TAG, "isSuspended: unknown handle " + handle);
        return false;
    }

    public int getCurrentNetworkByPhoneId(int phoneId) {
        if (getNetworkEvent(phoneId) == null) {
            return 0;
        }
        return getNetworkEvent(phoneId).network;
    }

    public int getCurrentNetwork(int handle) {
        RegisterTask task = getRegisterTaskByRegHandle(handle);
        if (task != null) {
            return task.getRegistrationRat();
        }
        Log.i(IRegistrationManager.LOG_TAG, "getCurrentNetwork: unknown handle " + handle);
        return 0;
    }

    public String[] getCurrentPcscf(int handle) {
        String[] pcscfInfo = new String[2];
        RegisterTask task = getRegisterTaskByRegHandle(handle);
        if (task == null) {
            Log.i(IRegistrationManager.LOG_TAG, "getCurrentPcscf: unknown handle " + handle);
            return null;
        }
        pcscfInfo[0] = task.getGovernor().getCurrentPcscfIp();
        pcscfInfo[1] = Integer.toString(task.getProfile().getSipPort());
        return pcscfInfo;
    }

    public void setTtyMode(int phoneId, int mode) {
        boolean bmode = (mode == Extensions.TelecomManager.TTY_MODE_OFF || mode == Extensions.TelecomManager.RTT_MODE) ? false : true;
        if (SlotBasedConfig.getInstance(phoneId).getTTYMode() != bmode) {
            Log.i(IRegistrationManager.LOG_TAG, "setTtyMode [" + bmode + "]");
            Bundle bundle = new Bundle();
            bundle.putInt("phoneId", phoneId);
            bundle.putBoolean("mode", bmode);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(37, bundle));
        }
    }

    public void setRttMode(int phoneId, boolean bmode) {
        if (SlotBasedConfig.getInstance(phoneId).getRTTMode() != bmode) {
            Log.i(IRegistrationManager.LOG_TAG, "setRttMode [" + bmode + "]");
            Bundle bundle = new Bundle();
            bundle.putInt("phoneId", phoneId);
            bundle.putBoolean("mode", bmode);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(RegistrationEvents.EVENT_RTTMODE_UPDATED, bundle));
        }
    }

    public void sendReRegister(int phoneId, int pdnType) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "sendReRegister : pdnType:" + pdnType);
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getPdnType() == pdnType) {
                sendReRegister(task);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendDnsQuery(int handle, String intf, String hostname, List<String> dnses, String type, String transport, String family, long netId) {
        int i = handle;
        IMSLog.i(IRegistrationManager.LOG_TAG, handle, "sendDnsQuery: hostname " + hostname + " dnses " + dnses);
        this.mRegStackIf.sendDnsQuery(handle, intf, hostname, dnses, type, transport, family, netId);
    }

    public void sendDummyDnsQuery() {
        String ipver;
        Log.i(IRegistrationManager.LOG_TAG, "sendDummyDnsQuery");
        List<String> dnses = this.mPdnController.getDnsServersByNetType();
        if (dnses != null) {
            Iterator<String> it = dnses.iterator();
            if (it.hasNext()) {
                String dns = it.next();
                Log.i(IRegistrationManager.LOG_TAG, "dns : " + dns);
                ipver = NetworkUtil.isIPv6Address(dns) ? "ipv6" : "ipv4";
                String iface = this.mPdnController.getIntfNameByNetType();
                Log.i(IRegistrationManager.LOG_TAG, "iface : " + iface + ",ipver:" + ipver);
                if (dnses != null && iface != null) {
                    this.mRegStackIf.sendDnsQuery(10, iface, "www.ims_rrc_refresh_dns.net", dnses, "HOST", "UDP", ipver, 0);
                    return;
                }
            }
        }
        ipver = "ipv4";
        String iface2 = this.mPdnController.getIntfNameByNetType();
        Log.i(IRegistrationManager.LOG_TAG, "iface : " + iface2 + ",ipver:" + ipver);
        if (dnses != null) {
        }
    }

    /* access modifiers changed from: package-private */
    public void tryRegister() {
        if (SimUtil.isDualIMS()) {
            for (ISimManager sm : this.mSimManagers) {
                tryRegister(sm.getSimSlotIndex());
            }
            return;
        }
        tryRegister(SimUtil.getDefaultPhoneId());
    }

    public int findBestNetwork(int phoneId, ImsProfile profile, IRegistrationGovernor governor) {
        return RegistrationUtils.findBestNetwork(phoneId, profile, governor, isPdnConnected(profile, phoneId), this.mPdnController, this.mVsm, this.mTelephonyManager.getVoiceNetworkType(SimUtil.getSubId(phoneId)), this.mContext);
    }

    /* access modifiers changed from: protected */
    public boolean onSimReady(boolean absent, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onSimReady: absent=" + absent);
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        NSDSSimEventManager.startIMSDeviceConfigService(this.mContext, sm);
        if (this.mImsFramework.getBoolean(phoneId, GlobalSettingsConstants.Registration.IMS_ENABLED, true)) {
            if (!absent) {
                IServiceModuleManager serviceModuleMgr = this.mImsFramework.getServiceModuleManager();
                if (!loadImsProfile(phoneId) || !serviceModuleMgr.isLooperExist()) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(20, new AsyncResult((Object) null, Integer.valueOf(phoneId), (Throwable) null)), 1000);
                } else {
                    serviceModuleMgr.serviceStartDeterminer(this, SlotBasedConfig.getInstance(phoneId).getProfiles(), phoneId);
                    serviceModuleMgr.notifyImsSwitchUpdateToApp();
                }
            } else {
                Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
                while (it.hasNext()) {
                    ((RegisterTask) it.next()).getGovernor().releaseThrottle(4);
                }
            }
            RegistrationUtils.setVoLTESupportProperty(absent, phoneId);
            ImsUtil.updateEmergencyCallDomain(this.mContext, phoneId, getEmergencyProfile(phoneId), sm, this.mImsFramework.getString(phoneId, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS"));
            if (!sm.hasVsim()) {
                clearTask(phoneId);
            }
            buildTask(phoneId);
            RegistrationUtils.initRttMode(this.mContext);
            if (!ConfigUtil.hasAcsProfile(this.mContext, phoneId, sm)) {
                this.mImsFramework.getServiceModuleManager().notifyConfigured(false, phoneId);
            }
            Mno mno = sm.getSimMno();
            if (RegistrationUtils.hasLoadedProfile(phoneId)) {
                if (SlotBasedConfig.getInstance(phoneId).getIconManager() == null) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onSimReady: added iconmanager.");
                    SlotBasedConfig.getInstance(phoneId).createIconManager(this.mContext, this, this.mPdnController, mno, phoneId);
                } else {
                    SlotBasedConfig.getInstance(phoneId).getIconManager().initConfiguration(mno, phoneId);
                }
            }
            if (this.mlegacyPhoneCount == 0 && SlotBasedConfig.getInstance(phoneId).getIconManager() == null && this.mCmcAccountManager.isSecondaryDevice()) {
                SlotBasedConfig.getInstance(phoneId).createIconManager(this.mContext, this, this.mPdnController, mno, phoneId);
            }
            if (mno == Mno.TMOUS && this.mVsm != null) {
                this.mVsm.setRttMode(Settings.Secure.getInt(this.mContext.getContentResolver(), "preferred_rtt_mode", 0));
            }
            if (mno == Mno.CMCC && sm.isLabSimCard() && !absent) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Change SS domain to PS_ONLY_VOLTEREGIED");
                ImsUtil.updateSsDomain(this.mContext, phoneId, "PS_ONLY_VOLTEREGIED");
            }
            if (!absent) {
                notifySimMobilityStatusChanged(phoneId, sm);
            }
            this.mRegStackIf.configure(phoneId);
            tryRegister(phoneId);
            return true;
        } else if (Mno.fromSalesCode(OmcCode.get()).isAus()) {
            this.mEventLog.logAndAdd(phoneId, "Aus device, keep IMS Service Up for Emergency Call.");
            ImsUtil.updateEmergencyCallDomain(this.mContext, phoneId, getEmergencyProfile(phoneId), sm, "PS");
            return true;
        } else {
            this.mEventLog.logAndAdd(phoneId, "IMS is disabled. Do not load profiles");
            IMSLog.c(LogClass.REGI_IMS_OFF, phoneId + ",IMS OFF");
            return false;
        }
    }

    public void stopPdnConnectivity(int network, IRegisterTask task) {
        int phoneId = task.getPhoneId();
        task.getGovernor().resetPcscfList();
        task.getGovernor().resetPcoType();
        task.getGovernor().resetPdnFailureInfo();
        task.clearSuspended();
        task.clearSuspendedBySnapshot();
        task.setKeepPdn(false);
        this.mPdnController.stopPdnConnectivity(network, phoneId, task);
    }

    public void moveNextPcscf(int phoneId, Message result) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "moveNextPcscf");
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask t = (RegisterTask) it.next();
            if (t.mProfile.hasEmergencySupport()) {
                IRegistrationGovernor gvnr = t.getGovernor();
                int phoneId2 = t.getPhoneId();
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "moveNextPcscf: current [" + gvnr.getPcscfOrdinal() + "]");
                t.setResultMessage(result);
                this.mMoveNextPcscf = true;
                t.setDeregiReason(11);
                if (t.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, t.getPhoneId(), "moveNextPcscf: EMERGENCY state, try UA delete");
                    onDeregistered(t, true, SipErrorBase.OK, 0);
                } else if (t.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, t.getPhoneId(), "moveNextPcscf: REGISTERED state, local deregister");
                    tryDeregisterInternal(t, true, true);
                } else {
                    IMSLog.i(IRegistrationManager.LOG_TAG, t.getPhoneId(), "It should not occur. ImsEmergencySession Issue!");
                    this.mMoveNextPcscf = false;
                    t.getProfile().setUicclessEmergency(true);
                    t.getGovernor().increasePcscfIdx();
                    this.mHandler.sendTryRegister(phoneId);
                }
            }
        }
    }

    public void suspendRegister(boolean suspend, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "suspendRegister:");
        if (suspend != SlotBasedConfig.getInstance(phoneId).isSuspendedWhileIrat()) {
            SlotBasedConfig.getInstance(phoneId).setSuspendWhileIrat(suspend);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(phoneId, "suspendedByIrat : " + suspend);
            if (!suspend) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Resume reRegister: mNetType = " + this.mNetEvtCtr.getNetType() + ", mWiFi = " + this.mNetEvtCtr.isWiFi());
                this.mHandler.removeMessages(136);
                if (this.mNetEvtCtr.isNwChanged()) {
                    this.mNetEvtCtr.setNwChanged(false);
                    Bundle bundle = new Bundle();
                    bundle.putInt("networkType", this.mNetEvtCtr.getNetType());
                    bundle.putInt("isWifiConnected", this.mNetEvtCtr.isWiFi() ? 1 : 0);
                    bundle.putInt("phoneId", phoneId);
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(3, bundle));
                }
            } else {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Suspend reRegister");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(136, phoneId, 0, (Object) null), 300000);
            }
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (suspend) {
                    task.suspendByIrat();
                } else {
                    task.resumeByIrat();
                }
            }
        }
    }

    public boolean getCsfbSupported(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getCsfbSupported:");
        NetworkEvent ne = getNetworkEvent(phoneId);
        if (ne == null) {
            return false;
        }
        if (ne.network == 13 || ne.network == 20) {
            boolean csOos = ne.csOutOfService;
            boolean isPsOnlyReg = this.mPdnController.isPsOnlyReg(phoneId);
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "isPsOnlyReg : " + isPsOnlyReg + " mEmmCause = " + getEmmCause());
            if (this.mEmmCause == 22) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "Support for EMM Cause 22");
                return true;
            } else if (csOos || isPsOnlyReg) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "CS OOS or CSFB not supported.");
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onEmergencyReady(int profileId) {
        Log.i(IRegistrationManager.LOG_TAG, "onEmergencyReady:");
        RegisterTask task = getRegisterTask(profileId);
        if (task != null) {
            task.setState(RegistrationConstants.RegisterTaskState.EMERGENCY);
            if (task.getResultMessage() != null) {
                task.getResultMessage().sendToTarget();
                task.setResultMessage((Message) null);
            }
        }
    }

    private boolean loadImsProfile(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "loadImsProfile:");
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        if (sm == null) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "loadImsProfile: no SIM loaded");
            return false;
        }
        String mnoName = sm.getSimMnoName();
        IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "loadImsProfile : " + mnoName);
        if (TextUtils.isEmpty(mnoName)) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "loadImsProfile: no SIM detected.");
            return false;
        }
        SlotBasedConfig.getInstance(phoneId).clearProfiles();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "loadImsProfile: mno: " + mnoName);
        for (ImsProfile p : ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, mnoName, phoneId)) {
            if (loademergencyprofileinvalidimpu(p, phoneId, sm.isISimDataValid())) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "loadImsProfile: Add profile: " + p.getName());
                SlotBasedConfig.getInstance(phoneId).addProfile(p);
            }
        }
        String rcsProfile = ConfigUtil.getRcsProfileLoaderInternalWithFeature(this.mContext, mnoName, phoneId);
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "loadImsProfile: mRcsProfile: " + rcsProfile);
        RcsUtils.DualRcs.refreshDualRcsReg(this.mContext);
        return true;
    }

    private boolean loademergencyprofileinvalidimpu(ImsProfile profile, int phoneId, boolean isISimDataVaild) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "loademergencyprofileinvalidimpu:");
        if (Mno.fromName(profile.getMnoName()) != Mno.BELL || isISimDataVaild || profile.hasEmergencySupport()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onImsProfileUpdated(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onImsProfileUpdated:");
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        if (sm == null) {
            this.mHandler.removeMessages(15);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(15, phoneId, 0, (Object) null), 100);
            return;
        }
        loadImsProfile(phoneId);
        RegistrationUtils.setVoLTESupportProperty(sm.hasNoSim(), phoneId);
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                task.setReason("profile updated");
                task.setDeregiReason(29);
                if (this.mTelephonyManager.getCallState(phoneId) != 0) {
                    task.setHasPendingDeregister(true);
                } else {
                    tryDeregisterInternal(task, false, false);
                }
            }
            RegistrationUtils.replaceProfilesOnTask(task);
        }
        buildTask(phoneId);
        this.mImsFramework.notifyImsReady(true, phoneId);
        notifySimMobilityStatusChanged(phoneId, sm);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, Integer.valueOf(phoneId)), 500);
    }

    /* access modifiers changed from: package-private */
    public void onImsSwitchUpdated(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onImsSwitchUpdated:");
        this.mImsFramework.getServiceModuleManager().onImsSwitchUpdated(phoneId);
        this.mRegStackIf.configure(phoneId);
        this.mHandler.onConfigUpdated((String) null, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void onOwnCapabilitiesChanged(int phoneId, Capabilities capa) {
        Log.i(IRegistrationManager.LOG_TAG, "onOwnCapabilitiesChanged: capabilities=" + capa);
        Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getPhoneId() == phoneId && (!task.getMno().isKor() || task.isRcsOnly())) {
                task.getGovernor().checkAcsPcscfListChange();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    task.setReason("own capability changed : " + capa);
                    updateRegistration(task, false);
                } else {
                    tryRegister(phoneId);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0086, code lost:
        r1 = r0.verify(r4, r8[0]);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean verifyCmcCertificate(java.security.cert.X509Certificate[] r8) {
        /*
            r7 = this;
            com.squareup.okhttp.internal.tls.OkHostnameVerifier r0 = com.squareup.okhttp.internal.tls.OkHostnameVerifier.INSTANCE
            r1 = 0
            int r2 = r7.getCmcLineSlotIndex()
            r3 = -1
            if (r2 == r3) goto L_0x0091
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r2 = r7.mCmcAccountManager
            boolean r2 = r2.isCmcActivated()
            if (r2 == 0) goto L_0x0091
            int r2 = r7.getCmcLineSlotIndex()
            int r3 = com.sec.internal.helper.SimUtil.getDefaultPhoneId()
            if (r2 == r3) goto L_0x0091
            int r2 = com.sec.internal.helper.SimUtil.getPhoneCount()
            r3 = 1
            if (r2 <= r3) goto L_0x0091
            int r2 = r7.getCmcLineSlotIndex()
            java.util.List r2 = r7.getPendingRegistration(r2)
            java.util.Iterator r2 = r2.iterator()
        L_0x002f:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0091
            java.lang.Object r3 = r2.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r3 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r3
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            boolean r4 = com.sec.internal.ims.core.RegistrationUtils.isCmcProfile(r4)
            if (r4 == 0) goto L_0x0090
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r3.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            if (r4 == r5) goto L_0x0055
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r3.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            if (r4 != r5) goto L_0x0090
        L_0x0055:
            java.lang.String r4 = r3.getPcscfHostname()
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Checking task: "
            r5.append(r6)
            com.sec.ims.settings.ImsProfile r6 = r3.getProfile()
            java.lang.String r6 = r6.getName()
            r5.append(r6)
            java.lang.String r6 = " / "
            r5.append(r6)
            r5.append(r4)
            java.lang.String r5 = r5.toString()
            java.lang.String r6 = "RegiMgr"
            android.util.Log.i(r6, r5)
            boolean r5 = android.text.TextUtils.isEmpty(r4)
            if (r5 == 0) goto L_0x0086
            goto L_0x002f
        L_0x0086:
            r5 = 0
            r5 = r8[r5]
            boolean r1 = r0.verify((java.lang.String) r4, (java.security.cert.X509Certificate) r5)
            if (r1 == 0) goto L_0x0090
            goto L_0x0091
        L_0x0090:
            goto L_0x002f
        L_0x0091:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerBase.verifyCmcCertificate(java.security.cert.X509Certificate[]):boolean");
    }

    /* access modifiers changed from: package-private */
    public void onSimRefresh(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onSimRefresh:");
        logTask();
        if (!this.mHandler.hasMessages(42)) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(42, phoneId, 0, (Object) null), 10000);
        }
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            task.getGovernor().releaseThrottle(0);
            if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "De-Register would be called by RIL(or timeout).");
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(36, new AsyncResult((Object) null, Integer.valueOf(task.getPhoneId()), (Throwable) null)), 600);
                if (RegistrationUtils.isCmcProfile(task.getProfile()) && task.getRegistrationRat() == 18) {
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        task.setDeregiReason(25);
                        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "CMC deregister explicitly on WiFi");
                        tryDeregisterInternal(task, false, false);
                    }
                }
                if (task.getMno().isKor() && !task.isRcsOnly() && !RegistrationUtils.isCmcProfile(task.getProfile()) && TelephonyManager.getDefault().getSimState() == 1 && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                    task.setDeregiReason(25);
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "De-Register is called right away to send SIP explicitly by sim absent event.");
                    tryDeregisterInternal(task, false, false);
                    return;
                }
                return;
            }
            if (!task.isOneOf(RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED)) {
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "connecting task Stop PDN by sim refresh event.");
                    stopPdnConnectivity(task.getPdnType(), task);
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
            } else if (task.getMno() == Mno.RJIL) {
                IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "stop auto configuration using config module");
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
        }
        this.mEventLog.logAndAdd(phoneId, "onSimRefresh: Reset SIM-related configuration.");
        clearTask(phoneId);
        SlotBasedConfig.getInstance(phoneId).clear();
        UriGeneratorFactory.getInstance().removeByPhoneId(phoneId);
        if (this.mHandler.hasMessages(42)) {
            this.mHandler.removeMessages(42);
        }
        ImsUtil.updateEmergencyCallDomain(this.mContext, phoneId, getEmergencyProfile(phoneId), (ISimManager) this.mSimManagers.get(phoneId), this.mImsFramework.getString(phoneId, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS"));
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "onSimRefresh: reset NetworkEvent");
        this.mPdnController.resetNetworkState(phoneId);
        this.mPdnController.unRegisterPhoneStateListener(phoneId);
        this.mPdnController.registerPhoneStateListener(phoneId);
        if (this.mCmcAccountManager != null) {
            this.mCmcAccountManager.onSimRefresh(phoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDefaultDataSubscriptionChanged() {
        RcsUtils.DualRcs.refreshDualRcsReg(this.mContext);
        if (SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS())) {
            handleDdsChangeOnSingleIms();
        } else if (SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS())) {
            handleDdsChangeOnDualIms();
        }
    }

    private void handleDdsChangeOnSingleIms() {
        int dds = SimUtil.getDefaultPhoneId();
        IMSLog.i(IRegistrationManager.LOG_TAG, dds, "onDefaultDataSubscriptionChanged");
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    task.setReason("DDS change");
                    task.setDeregiReason(35);
                    tryDeregisterInternal(task, true, false);
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
                if (!task.getMno().isTmobile() && !task.getMno().isChn() && !task.getMno().isHkMo() && !task.getMno().isTw()) {
                    if (!task.getMno().isOneOf(Mno.BOG, Mno.ORANGE, Mno.ORANGE_POLAND, Mno.DIGI, Mno.TELECOM_ITALY, Mno.VODAFONE, Mno.WINDTRE, Mno.TELEKOM_ALBANIA)) {
                    }
                }
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                    stopPdnConnectivity(task.getPdnType(), task);
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
            }
        }
        for (ISimManager sm : this.mSimManagers) {
            int phoneId2 = sm.getSimSlotIndex();
            this.mPdnController.unRegisterPhoneStateListener(phoneId2);
            if (phoneId2 == dds) {
                this.mPdnController.registerPhoneStateListener(phoneId2);
                if (sm.isSimAvailable()) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(20, new AsyncResult((Object) null, Integer.valueOf(dds), (Throwable) null)));
                }
            } else {
                IMSLog.i(IRegistrationManager.LOG_TAG, dds, "onDefaultDataSubscriptionChanged: reset NetworkEvent slot[" + phoneId2 + "]");
                SlotBasedConfig.getInstance(phoneId2).setNetworkEvent(new NetworkEvent());
                this.mPdnController.resetNetworkState(phoneId2);
            }
            ImsIconManager iconManager = getImsIconManager(phoneId2);
            if (iconManager != null) {
                iconManager.unRegisterPhoneStateListener();
                if (phoneId2 == dds) {
                    iconManager.registerPhoneStateListener();
                }
                iconManager.updateIconWithDDSChange();
            }
        }
        this.mHandler.removeMessages(20);
        this.mConfigModule.getAcsConfig(dds).setAcsCompleteStatus(false);
        this.mConfigModule.setDualSimRcsAutoConfig(true);
    }

    private void handleDdsChangeOnDualIms() {
        updateRegistration(SimUtil.getDefaultPhoneId());
        Iterator it = SlotBasedConfig.getInstance(SimUtil.getOppositeSimSlot(SimUtil.getDefaultPhoneId())).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (RegistrationUtils.isCmcProfile(task.getProfile())) {
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && task.getRegistrationRat() != 18) {
                    task.setReason("DDS change");
                    task.setDeregiReason(35);
                    tryDeregisterInternal(task, true, false);
                    task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    IMSLog.i(IRegistrationManager.LOG_TAG, SimUtil.getDefaultPhoneId(), "onDefaultDataSubscriptionChanged: Cmc deregister");
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDelayedDeregister(RegisterTask task) {
        this.mHandler.removeMessages(128);
        task.getGovernor().onDelayedDeregister();
    }

    /* access modifiers changed from: protected */
    public void notifyImsNotAvailable(RegisterTask task, boolean force) {
        if (this.mCallState != 0) {
            Log.i(IRegistrationManager.LOG_TAG, "ignore notifyImsNotAvailable in call");
        } else if ((!task.getMno().isKor() && task.getMno() != Mno.DOCOMO) || task.mGovernor.needImsNotAvailable()) {
            if (!SlotBasedConfig.getInstance(task.getPhoneId()).isNotifiedImsNotAvailable() || force) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                StringBuilder sb = new StringBuilder();
                sb.append("notifyImsNotAvailable: UserAgent: ");
                sb.append(task.mObject == null ? "null" : "exist");
                sb.append(", force: ");
                sb.append(force);
                sb.append(", task: ");
                sb.append(task.getState());
                sb.append(", reason: ");
                sb.append(task.getNotAvailableReason());
                simpleEventLog.logAndAdd(sb.toString());
                task.getGovernor().stopTimsTimer(RegistrationConstants.REASON_IMS_NOT_AVAILABLE);
                ImsRegistration reg = ImsRegistration.getBuilder().setHandle(-1).setImsProfile(new ImsProfile(task.getProfile())).setServices(task.getProfile().getAllServiceSetFromAllNetwork()).setEpdgStatus(this.mPdnController.isEpdgConnected(task.getPhoneId())).setPdnType(task.getPdnType()).setUuid(getUuid(task.getPhoneId(), task.getProfile())).setInstanceId(getInstanceId(task.getPhoneId(), task.getPdnType(), task.getProfile())).setNetwork(task.getNetworkConnected()).setRegiRat(SlotBasedConfig.getInstance(task.getPhoneId()).getNetworkEvent().network).setPhoneId(task.getPhoneId()).build();
                if (task.getUserAgent() == null || task.getNotAvailableReason() == 1) {
                    notifyImsRegistration(reg, false, task, new ImsRegistrationError(0, "", 72, 32));
                    makeThrottleforImsNotAvailable(task);
                }
                if (task.getUserAgent() != null) {
                    if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                        task.setDeregiReason(72);
                        makeThrottleforImsNotAvailable(task);
                        if (task.getNotAvailableReason() == 1) {
                            tryDeregisterInternal(task, true, true);
                        } else {
                            tryDeregisterInternal(task, true, false);
                        }
                    } else if (task.getNotAvailableReason() != 1) {
                        ImsRegistration taskReg = task.getImsRegistration();
                        notifyImsRegistration(taskReg == null ? reg : taskReg, false, task, new ImsRegistrationError(0, "", 72, 32));
                        makeThrottleforImsNotAvailable(task);
                    }
                }
                SlotBasedConfig.getInstance(task.getPhoneId()).setNotifiedImsNotAvailable(true);
                task.clearNotAvailableReason();
            }
        }
    }

    private void makeThrottleforImsNotAvailable(RegisterTask task) {
        if (task.getGovernor().needImsNotAvailable() && !this.mPdnController.isEpsOnlyReg(task.getPhoneId())) {
            int lteVoiceStatus = SemSystemProperties.getInt(ImsConstants.SystemProperties.LTE_VOICE_STATUS, -1);
            IMSLog.i(IRegistrationManager.LOG_TAG, "makeThrottleforImsNotAvailable: lteVoiceStatus = " + lteVoiceStatus);
            if (lteVoiceStatus == 1) {
                this.mEventLog.logAndAdd("makeThrottleforImsNotAvailable, combined with csfb supported");
                task.getGovernor().makeThrottle();
            }
        }
    }

    public void updateRegistrationBySSAC(int phoneId, boolean enabled) {
        Log.i(IRegistrationManager.LOG_TAG, "updateRegistrationBySSAC:[" + phoneId + "]");
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        if (sm != null && sm.getSimMno() == Mno.VZW && !Boolean.parseBoolean(SemSystemProperties.get("ro.ril.svlte1x"))) {
            boolean previousSsac = SlotBasedConfig.getInstance(phoneId).isSsacEnabled();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "updateRegistrationBySSAC : " + previousSsac + " -> " + enabled);
            if (!enabled) {
                this.mHandler.removeMessages(121, Integer.valueOf(phoneId));
            }
            if (previousSsac != enabled) {
                this.mHandler.removeMessages(121, Integer.valueOf(phoneId));
                if (enabled) {
                    int delay = 0;
                    Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        RegisterTask task = (RegisterTask) it.next();
                        if (ImsProfile.hasVolteService(task.getProfile()) && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && task.getRegistrationRat() == 13) {
                            delay = DmConfigHelper.readInt(this.mContext, "tvolte_hys_timer", 60, phoneId).intValue() * 1000;
                            break;
                        }
                    }
                    Log.i(IRegistrationManager.LOG_TAG, "updateRegistrationBySSAC : registration will be started after " + delay + "ms.");
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(121, 1, 0, Integer.valueOf(phoneId)), (long) delay);
                    return;
                }
                this.mHandler.sendMessage(this.mHandler.obtainMessage(121, 0, 0, Integer.valueOf(phoneId)));
            }
        }
    }

    public void updateTelephonyCallStatus(int phoneId, int callStatus) {
        Log.i(IRegistrationManager.LOG_TAG, "updateTelephonyCallStatus: " + callStatus);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(33, phoneId, callStatus, (Object) null));
    }

    public boolean isSelfActivationRequired(int phoneId) {
        boolean ret = false;
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (true) {
            if (it.hasNext()) {
                if (((RegisterTask) it.next()).getGovernor().getPcoType() == RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION) {
                    ret = true;
                    break;
                }
            } else {
                break;
            }
        }
        Log.d(IRegistrationManager.LOG_TAG, "isSelfActivationRequired = " + ret);
        return ret;
    }

    public void startEmergencyRegistration(int phoneId, Message result) {
        ImsProfile auProfile;
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "startEmergencyRegistration:");
        ISimManager sm = (ISimManager) this.mSimManagers.get(phoneId);
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (sm != null && rtl != null) {
            if (this.mHandler.hasMessages(10)) {
                this.mHasSilentE911 = result;
                this.mPhoneIdForSilentE911 = phoneId;
                Log.i(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: retry after previous stopEmergencyRegistration");
                return;
            }
            if (SlotBasedConfig.getInstance(phoneId).getIconManager() != null) {
                SlotBasedConfig.getInstance(phoneId).getIconManager().setDuringEmergencyCall(true);
            }
            Iterator it = rtl.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RegisterTask t = (RegisterTask) it.next();
                if (t.getProfile().hasEmergencySupport()) {
                    Log.i(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: EmergencyRegistration state=" + t.mState);
                    if (t.getMno() == Mno.KDDI || t.getMno().isAus()) {
                        if (t.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                            if (!t.getMno().isAus() || (auProfile = (ImsProfile) this.mAuEmergencyProfile.get(phoneId)) == null || auProfile.getId() == t.getProfile().getId()) {
                                Log.i(IRegistrationManager.LOG_TAG, "remove emergency pending RegiTask.");
                                rtl.remove(t);
                            } else {
                                if (t.getState() == RegistrationConstants.RegisterTaskState.CONNECTED) {
                                    stopPdnConnectivity(t.getPdnType(), t);
                                }
                                Log.i(IRegistrationManager.LOG_TAG, "Aus Emergency case, remove emergency task if old and new profile ID are different.");
                                rtl.remove(t);
                            }
                        }
                    }
                    if (t.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.EMERGENCY)) {
                        if (t.mState == RegistrationConstants.RegisterTaskState.EMERGENCY && t.mMno == Mno.GENERIC_IR92 && t.getUserAgent() == null) {
                            Log.d(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: Trigger New Register with same task");
                            t.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
                            t.setResultMessage(result);
                            rtl.remove(t);
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(118, t));
                            return;
                        }
                        Log.d(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: already registered.");
                        result.sendToTarget();
                        return;
                    } else if (t.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                        Log.i(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: DeRegistering Mode. Deregister current and start new registration.");
                        if (this.mHandler.hasMessages(107, t)) {
                            this.mHandler.removeMessages(107, t);
                            this.mHandler.sendMessage(this.mHandler.obtainMessage(107, t));
                        } else {
                            return;
                        }
                    } else {
                        if (t.getResultMessage() != null) {
                            t.getResultMessage().sendToTarget();
                        }
                        t.setResultMessage(result);
                        return;
                    }
                }
            }
            if (sm.getDevMno().isAus()) {
                Log.i(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: refresh Emergency profile...");
                refreshAuEmergencyProfile(phoneId);
            }
            ImsProfile profile = getEmergencyProfile(phoneId);
            if (profile != null) {
                tryEmergencyRegister(phoneId, profile, result, sm.hasNoSim());
            }
        }
    }

    public void refreshAuEmergencyProfile(int phoneId) {
        this.mAuEmergencyProfile.delete(phoneId);
    }

    public void stopEmergencyRegistration(int phoneId) {
        Log.i(IRegistrationManager.LOG_TAG, "stopEmergencyRegistration:");
        ImsProfile profile = getEmergencyProfile(phoneId);
        if (profile != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("id", profile.getId());
            bundle.putBoolean("explicitDeregi", true);
            bundle.putInt("phoneId", phoneId);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(10, bundle));
            if (!((ISimManager) this.mSimManagers.get(phoneId)).hasNoSim()) {
                profile.setUicclessEmergency(false);
            }
        }
        if (SlotBasedConfig.getInstance(phoneId).getIconManager() != null) {
            SlotBasedConfig.getInstance(phoneId).getIconManager().setDuringEmergencyCall(false);
        }
    }

    public void stopEmergencyPdnOnly(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "stopEmergencyPdnOnly:");
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getProfile().hasEmergencySupport()) {
                stopPdnConnectivity(task.getPdnType(), task);
                task.setState(RegistrationConstants.RegisterTaskState.IDLE);
            }
        }
    }

    public void setOwnCapabilities(int phoneId, Capabilities capa) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(31, phoneId, 0, capa));
    }

    /* access modifiers changed from: protected */
    public void updateGeolocation(LocationInfo geolocation, boolean silentUpdate) {
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                int geoLocPhase = task.getProfile().getSupportedGeolocationPhase();
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "updateGeolocation: " + task.getProfile().getName() + ", geoLocPhase: " + geoLocPhase);
                if (getNetworkEvent(phoneId) != null) {
                    if (geoLocPhase >= 2) {
                        this.mRegStackIf.updateGeolocation(task, geolocation);
                    }
                    if (task.getGovernor().onUpdateGeolocation(geolocation) && this.mGeolocationCon != null) {
                        this.mGeolocationCon.stopPeriodicLocationUpdate(phoneId);
                    }
                }
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "updateGeolocation: CountryCode : " + geolocation.mCountry + ", silentUpdate : " + silentUpdate);
        if (this.mVsm != null && !silentUpdate) {
            this.mVsm.onUpdateGeolocation();
        }
    }

    /* access modifiers changed from: protected */
    public void updateRat(RegisterTask task, int rat) {
        this.mRegStackIf.updateRat(task, rat);
    }

    /* access modifiers changed from: protected */
    public void updateTimeInPlani(int phoneId, boolean removePreviousLastPani) {
        if (removePreviousLastPani) {
            this.mRegStackIf.removePreviousLastPani(phoneId);
        }
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            this.mRegStackIf.updateTimeInPlani(phoneId, ((RegisterTask) it.next()).getProfile());
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGeolocationUpdate(LocationInfo geolocation, boolean silentUpdate) {
        IMSLog.i(IRegistrationManager.LOG_TAG, "notifyGeolocationUpdate, silentUpdate = " + silentUpdate);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(51, silentUpdate, 0, geolocation));
        if (!silentUpdate) {
            this.mHandler.sendEmptyMessage(40);
        }
    }

    /* access modifiers changed from: protected */
    public void onFlightModeChanged(boolean isOn) {
        if (isOn) {
            for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
                Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
                while (it.hasNext()) {
                    IRegisterTask task = (IRegisterTask) it.next();
                    if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
                        this.mRegStackIf.removeUserAgent(task);
                        task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    }
                    task.clearSuspended();
                }
                suspendRegister(false, phoneId);
            }
            return;
        }
        this.mSimManagers.forEach(new Consumer() {
            public final void accept(Object obj) {
                RegistrationManagerBase.this.lambda$onFlightModeChanged$0$RegistrationManagerBase((ISimManager) obj);
            }
        });
    }

    public /* synthetic */ void lambda$onFlightModeChanged$0$RegistrationManagerBase(ISimManager sm) {
        int phoneId = sm.getSimSlotIndex();
        SlotBasedConfig.getInstance(phoneId).setNotifiedImsNotAvailable(false);
        if (sm.getSimMno().isOneOf(Mno.VELCOM_BY, Mno.SBERBANK_RUSSIA, Mno.MTS_RUSSIA, Mno.MEGAFON_RUSSIA, Mno.BEELINE_RUSSIA, Mno.TMOBILE)) {
            updateTimeInPlani(phoneId, true);
        }
        if (sm.getSimMno().isKor()) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                IRegisterTask task = (IRegisterTask) it.next();
                if (task.getGovernor().isMobilePreferredForRcs() && NetworkUtil.isMobileDataOn(this.mContext) && NetworkUtil.isMobileDataPressed(this.mContext) && this.mPdnController.isWifiConnected()) {
                    this.mNetEvtCtr.isPreferredPdnForRCSRegister((RegisterTask) task, phoneId, true);
                }
            }
        }
        tryRegister(sm.getSimSlotIndex());
    }

    public void setSilentLogEnabled(boolean onOff) {
        this.mRegStackIf.setSilentLogEnabled(onOff);
    }

    public void onDnsResponse(List<String> ipAddr, int port, int phoneId) {
        Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            IRegisterTask task = (IRegisterTask) it.next();
            if (task.getState() == RegistrationConstants.RegisterTaskState.RESOLVING) {
                task.setState(RegistrationConstants.RegisterTaskState.RESOLVED);
                ipAddr = task.getGovernor().checkValidPcscfIp(ipAddr);
                if (ipAddr.isEmpty() || port > 65535) {
                    this.mAresLookupRequired = false;
                } else {
                    task.getGovernor().updatePcscfIpList(ipAddr);
                    task.getProfile().setSipPort(port);
                    task.setDnsQueryRetryCount(0);
                }
                if (task.isRcsOnly() && ipAddr.isEmpty()) {
                    String rcsAs = ConfigUtil.getAcsServerType(this.mContext, task.getPhoneId());
                    if (((ConfigUtil.isRcsEur(task.getMno()) || task.getMno().isKor() || task.getMno() == Mno.CMCC) && task.getRegistrationRat() == 18) || ImsConstants.RCS_AS.JIBE.equals(rcsAs)) {
                        int retrycount = task.getDnsQueryRetryCount();
                        IMSLog.s(IRegistrationManager.LOG_TAG, "onDnsResponse: retrycount=" + retrycount);
                        if (retrycount <= 5) {
                            task.setDnsQueryRetryCount(retrycount + 1);
                            this.mHandler.sendTryRegister(task.getPhoneId(), 10000);
                        }
                    }
                }
                if (!ipAddr.isEmpty()) {
                    this.mHandler.sendTryRegister(task.getPhoneId());
                }
            }
        }
    }

    public void registerDmListener(IImsDmConfigListener listener) {
        this.mHandler.registerDmListener(listener);
    }

    public void unregisterDmListener(IImsDmConfigListener listener) {
        this.mHandler.unregisterDmListener(listener);
    }
}
