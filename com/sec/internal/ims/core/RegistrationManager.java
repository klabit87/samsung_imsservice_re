package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.motricity.verizon.ssoengine.SSOContentProviderConstants;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.extensions.WiFiManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManager;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.TimeBasedUuidGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.interfaces.ims.core.handler.IRegistrationInterface;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public abstract class RegistrationManager implements IRegistrationManager {
    protected static final int ADHOC_ID_SIM2_OFFSET = 20000;
    protected static final int ADHOC_IMS_PROFILE_ID_BASE = 10000;
    protected static final int HANDOFF_EVENT_TIMER = 300;
    protected static final int ID_SIM2_OFFSET = 1000;
    protected static final int MAX_RECOVERY_ACTION_COUNT = 7;
    protected boolean mAresLookupRequired = true;
    protected SparseArray<ImsProfile> mAuEmergencyProfile;
    protected int mCallState = 0;
    protected ICmcAccountManager mCmcAccountManager;
    protected IConfigModule mConfigModule;
    protected Context mContext;
    protected int mEmmCause;
    protected SimpleEventLog mEventLog;
    protected IGeolocationController mGeolocationCon = null;
    protected RegistrationManagerHandler mHandler;
    protected Message mHasSilentE911 = null;
    protected IImsFramework mImsFramework;
    protected boolean mIsNonDDSDeRegRequired = false;
    protected boolean mIsVolteAllowedWithDsac = true;
    protected boolean mMoveNextPcscf = false;
    protected NetworkEventController mNetEvtCtr;
    protected OmadmConfigState mOmadmState = OmadmConfigState.IDLE;
    protected PdnController mPdnController;
    protected int mPhoneIdForSilentE911 = -1;
    protected IRcsPolicyManager mRcsPolicyManager;
    protected IRegistrationInterface mRegStackIf;
    private IImsRegistrationListener mRegisterP2pListener = null;
    protected List<ISimManager> mSimManagers;
    protected ITelephonyManager mTelephonyManager;
    protected List<String> mThirdPartyFeatureTags = null;
    protected UserEventController mUserEvtCtr;
    protected IVolteServiceModule mVsm;
    protected int mlegacyPhoneCount = 0;

    public enum OmadmConfigState {
        IDLE,
        TRIGGERED,
        FINISHED
    }

    public void setVolteServiceModule(IVolteServiceModule vsm) {
        this.mVsm = vsm;
        this.mNetEvtCtr.setVolteServiceModule(vsm);
        this.mUserEvtCtr.setVolteServiceModule(vsm);
    }

    public void setConfigModule(IConfigModule cm) {
        this.mConfigModule = cm;
        this.mHandler.setConfigModule(cm);
        this.mUserEvtCtr.setConfigModule(cm);
    }

    public void setStackInterface(IRegistrationInterface regStackIf) {
        this.mRegStackIf = regStackIf;
        regStackIf.setEventLog(this.mEventLog);
        this.mRegStackIf.setRegistrationHandler(this.mHandler);
        this.mRegStackIf.setSimManagers(this.mSimManagers);
        this.mRegStackIf.setPdnController(this.mPdnController);
    }

    public void setGeolocationController(GeolocationController glc) {
        this.mGeolocationCon = glc;
    }

    /* access modifiers changed from: package-private */
    public RegistrationManagerHandler getRegistrationManagerHandler() {
        return this.mHandler;
    }

    /* access modifiers changed from: protected */
    public ImsIconManager getImsIconManager(int phoneId) {
        ImsIconManager res = SlotBasedConfig.getInstance(phoneId).getIconManager();
        if (res == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getImsIconManager is not exist.");
        }
        return res;
    }

    public ISimManager getSimManager(int simSlot) {
        try {
            return this.mSimManagers.get(simSlot);
        } catch (IndexOutOfBoundsException e) {
            IMSLog.e(IRegistrationManager.LOG_TAG, simSlot, "getSimManager: " + e.toString());
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public SimpleEventLog getEventLog() {
        return this.mEventLog;
    }

    /* access modifiers changed from: package-private */
    public OmadmConfigState getOmadmState() {
        return this.mOmadmState;
    }

    public boolean hasOmaDmFinished() {
        return this.mOmadmState == OmadmConfigState.FINISHED;
    }

    /* access modifiers changed from: package-private */
    public boolean getAresLookupRequired() {
        return this.mAresLookupRequired;
    }

    /* access modifiers changed from: package-private */
    public int getEmmCause() {
        return this.mEmmCause;
    }

    public boolean isInvite403DisabledService(int phoneId) {
        return SlotBasedConfig.getInstance(phoneId).isInviteRejected();
    }

    /* access modifiers changed from: package-private */
    public boolean isAdhocProfile(ImsProfile profile) {
        return profile.getId() >= 10000;
    }

    /* access modifiers changed from: package-private */
    public boolean getVolteAllowedWithDsac() {
        return this.mIsVolteAllowedWithDsac;
    }

    /* access modifiers changed from: protected */
    public boolean isCdmaAvailableForVoice(int phoneId) {
        return SlotBasedConfig.getInstance(phoneId).isCdmaAvailableForVoice();
    }

    /* access modifiers changed from: package-private */
    public void resetNotifiedImsNotAvailable(int phoneId) {
        SlotBasedConfig.getInstance(phoneId).setNotifiedImsNotAvailable(false);
    }

    /* access modifiers changed from: package-private */
    public void setOmadmState(OmadmConfigState state) {
        this.mOmadmState = state;
    }

    /* access modifiers changed from: package-private */
    public void setAresLookupRequired(boolean required) {
        this.mAresLookupRequired = required;
    }

    /* access modifiers changed from: package-private */
    public void setEmmCause(int cause) {
        this.mEmmCause = cause;
    }

    public void setInvite403DisableService(boolean disableService, int phoneId) {
        SlotBasedConfig.getInstance(phoneId).setInviteReject(disableService);
    }

    /* access modifiers changed from: package-private */
    public void setVolteAllowedWithDsac(boolean IsVolteAllowedWithDac) {
        this.mIsVolteAllowedWithDsac = IsVolteAllowedWithDac;
    }

    /* access modifiers changed from: package-private */
    public void setCdmaAvailableForVoice(int phoneId, boolean value) {
        SlotBasedConfig.getInstance(phoneId).setCdmaAvailableForVoice(value);
    }

    /* access modifiers changed from: protected */
    public void setCallState(int callState) {
        this.mCallState = callState;
    }

    public void setNonDDSDeRegRequired(boolean nonDDSDeRegRequired) {
        this.mIsNonDDSDeRegRequired = nonDDSDeRegRequired;
    }

    public synchronized void registerListener(IImsRegistrationListener listener, int phoneId) {
        registerListener(listener, true, phoneId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0061, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void registerListener(com.sec.ims.IImsRegistrationListener r5, boolean r6, int r7) {
        /*
            r4 = this;
            monitor-enter(r4)
            if (r5 != 0) goto L_0x000c
            java.lang.String r0 = "RegiMgr"
            java.lang.String r1 = "listener is null.."
            com.sec.internal.log.IMSLog.i(r0, r7, r1)     // Catch:{ all -> 0x0062 }
            monitor-exit(r4)
            return
        L_0x000c:
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r7)     // Catch:{ all -> 0x0062 }
            android.os.RemoteCallbackList r0 = r0.getImsRegistrationListeners()     // Catch:{ all -> 0x0062 }
            if (r0 != 0) goto L_0x0026
            android.os.RemoteCallbackList r1 = new android.os.RemoteCallbackList     // Catch:{ all -> 0x0062 }
            r1.<init>()     // Catch:{ all -> 0x0062 }
            r1.register(r5)     // Catch:{ all -> 0x0062 }
            com.sec.internal.ims.core.SlotBasedConfig r2 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r7)     // Catch:{ all -> 0x0062 }
            r2.setImsRegistrationListeners(r1)     // Catch:{ all -> 0x0062 }
            goto L_0x0029
        L_0x0026:
            r0.register(r5)     // Catch:{ all -> 0x0062 }
        L_0x0029:
            if (r6 == 0) goto L_0x0060
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r7)     // Catch:{ all -> 0x0062 }
            java.util.Map r1 = r1.getImsRegistrations()     // Catch:{ all -> 0x0062 }
            java.util.Collection r1 = r1.values()     // Catch:{ all -> 0x0062 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x0062 }
        L_0x003b:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x0062 }
            if (r2 == 0) goto L_0x0060
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x0062 }
            com.sec.ims.ImsRegistration r2 = (com.sec.ims.ImsRegistration) r2     // Catch:{ all -> 0x0062 }
            int r3 = r2.getPhoneId()     // Catch:{ RemoteException -> 0x005b }
            if (r3 != r7) goto L_0x005a
            com.sec.ims.settings.ImsProfile r3 = r2.getImsProfile()     // Catch:{ RemoteException -> 0x005b }
            boolean r3 = com.sec.internal.ims.core.RegistrationUtils.isCmcProfile(r3)     // Catch:{ RemoteException -> 0x005b }
            if (r3 != 0) goto L_0x005a
            r5.onRegistered(r2)     // Catch:{ RemoteException -> 0x005b }
        L_0x005a:
            goto L_0x005f
        L_0x005b:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ all -> 0x0062 }
        L_0x005f:
            goto L_0x003b
        L_0x0060:
            monitor-exit(r4)
            return
        L_0x0062:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.registerListener(com.sec.ims.IImsRegistrationListener, boolean, int):void");
    }

    public synchronized void unregisterListener(IImsRegistrationListener listener, int phoneId) {
        RemoteCallbackList<IImsRegistrationListener> rcl = SlotBasedConfig.getInstance(phoneId).getImsRegistrationListeners();
        if (rcl != null) {
            rcl.unregister(listener);
        }
    }

    public synchronized void registerP2pListener(IImsRegistrationListener listener) {
        this.mRegisterP2pListener = listener;
        Log.d(IRegistrationManager.LOG_TAG, "registerP2pListener done");
    }

    private void notifyImsP2pRegistration(boolean registered, ImsRegistration registration, ImsRegistrationError errorCode) {
        int cmcType = registration.getImsProfile().getCmcType();
        Log.d(IRegistrationManager.LOG_TAG, "notifyImsP2pRegistration(): " + cmcType);
        IImsRegistrationListener iImsRegistrationListener = this.mRegisterP2pListener;
        if (iImsRegistrationListener != null && cmcType >= 2) {
            if (registered) {
                try {
                    iImsRegistrationListener.onRegistered(registration);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                iImsRegistrationListener.onDeregistered(registration, errorCode);
            }
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void notifyImsRegistration(ImsRegistration registration, boolean registered, IRegisterTask task, ImsRegistrationError error) {
        RemoteCallbackList<IImsRegistrationListener> rcl;
        IMSLog.i(IRegistrationManager.LOG_TAG, registration.getPhoneId(), "notifyImsRegistration(): " + registration.getImsProfile());
        notifyImsP2pRegistration(registered, registration, error);
        notifyCmcRegistration(registered, registration, error);
        this.mImsFramework.getServiceModuleManager().notifyImsRegistration(registration, registered, error.getSipErrorCode());
        this.mImsFramework.getIilManager(task.getPhoneId()).notifyImsRegistration(registration, registered, error);
        if (!RegistrationUtils.isCmcProfile(registration.getImsProfile()) && (rcl = SlotBasedConfig.getInstance(registration.getPhoneId()).getImsRegistrationListeners()) != null) {
            int i = rcl.beginBroadcast();
            while (i > 0) {
                i--;
                if (registered) {
                    try {
                        rcl.getBroadcastItem(i).onRegistered(registration);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    rcl.getBroadcastItem(i).onDeregistered(registration, error);
                }
            }
            Log.i(IRegistrationManager.LOG_TAG, "notify mRegistrationList, finish");
            rcl.finishBroadcast();
        }
        if (RegistrationUtils.needToNotifyImsPhoneRegistration(registration, registered, this.mCmcAccountManager.isSecondaryDevice())) {
            this.mImsFramework.getImsNotifier().notifyImsRegistration(registration, registered, error);
        }
        this.mImsFramework.getImsDiagMonitor().handleRegistrationEvent(registration, registered);
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager != null) {
            Message msg = Message.obtain((Handler) null, WiFiManagerExt.SEC_COMMAND_ID_SET_MAX_DTIM_IN_SUSPEND);
            Bundle args = new Bundle();
            if (!registered || !registration.getEpdgStatus() || (!registration.hasService("mmtel") && !registration.hasService("mmtel-video"))) {
                args.putInt("enable", 1);
            } else {
                args.putInt("enable", 0);
            }
            msg.obj = args;
            WiFiManagerExt.callSECApi(wifiManager, msg);
            IMSLog.i(IRegistrationManager.LOG_TAG, registration.getPhoneId(), "Notify to WiFiManager");
        }
        setImsRegistrationState(registration.getPhoneId());
        Intent intent = new Intent(ImsConstants.Intents.ACTION_IMS_STATE);
        intent.putExtra(ImsConstants.Intents.EXTRA_REGISTERED, registered).putExtra(ImsConstants.Intents.EXTRA_REGISTERED_SERVICES, registration.getServices().toString()).putExtra(ImsConstants.Intents.EXTRA_VOWIFI, registration.getEpdgStatus()).putExtra(ImsConstants.Intents.EXTRA_SIP_ERROR_CODE, error.getSipErrorCode()).putExtra(ImsConstants.Intents.EXTRA_REGI_PHONE_ID, registration.getPhoneId()).putExtra(ImsConstants.Intents.EXTRA_SIP_ERROR_REASON, error.getSipErrorReason());
        IntentUtil.sendBroadcast(this.mContext, intent);
    }

    /* access modifiers changed from: protected */
    public void setImsRegistrationState(int phoneId) {
        boolean hasRegisteredTask = false;
        Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RegisterTask task = (RegisterTask) it.next();
            if (!task.getProfile().hasEmergencySupport() && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                hasRegisteredTask = true;
                break;
            }
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "setImsRegistrationState : " + hasRegisteredTask);
        IMSLog.c(LogClass.REGI_PDN_DEACT_DELAY, phoneId + ",PDN_DEACT_DELAY: " + hasRegisteredTask);
        this.mTelephonyManager.setImsRegistrationState(phoneId, hasRegisteredTask);
    }

    public List<IRegisterTask> getPendingRegistration(int phoneId) {
        List<RegisterTask> taskList = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (taskList != null) {
            return new CopyOnWriteArrayList(taskList);
        }
        IMSLog.e(IRegistrationManager.LOG_TAG, "getPendingRegistration : no task return null");
        return null;
    }

    public ImsRegistration[] getRegistrationInfo() {
        IRegisterTask task;
        List<ImsRegistration> list = new ArrayList<>();
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            for (ImsRegistration reg : SlotBasedConfig.getInstance(phoneId).getImsRegistrations().values()) {
                if (!(reg == null || (task = getRegisterTaskByRegHandle(reg.getHandle())) == null || task.getState() != RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    list.add(reg);
                }
            }
        }
        return (ImsRegistration[]) list.toArray(new ImsRegistration[0]);
    }

    public ImsRegistration getRegistrationInfoByServiceType(String serviceType, int phoneId) {
        IRegisterTask task;
        for (ImsRegistration reg : SlotBasedConfig.getInstance(phoneId).getImsRegistrations().values()) {
            if (reg != null && reg.getPhoneId() == phoneId && reg.getImsProfile().getCmcType() == 0 && !reg.getImsProfile().hasEmergencySupport() && (task = getRegisterTaskByRegHandle(reg.getHandle())) != null && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && ImsUtil.isMatchedService(reg.getServices(), serviceType)) {
                if (!task.getMno().isKor()) {
                    return reg;
                }
                Set<String> updatedServiceList = reg.getServices();
                if (getNetworkEvent(task.getPhoneId()) == null) {
                    return reg;
                }
                for (String imsService : reg.getServices()) {
                    if ("mmtel".equals(imsService) && (!(getNetworkEvent(task.getPhoneId()).network == 13 || getNetworkEvent(task.getPhoneId()).network == 20) || getNetworkEvent(task.getPhoneId()).outOfService)) {
                        updatedServiceList.remove(imsService);
                    }
                }
                return new ImsRegistration(reg, updatedServiceList);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void notifySimMobilityStatusChanged(int phoneId, ISimManager simManager) {
        boolean newSimMobility = RegistrationUtils.hasSimMobilityProfile(phoneId);
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "notifySimMobilityStatusChanged: old[" + SlotBasedConfig.getInstance(phoneId).isSimMobilityActivated() + "], new [" + newSimMobility + "]");
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri(ImsConstants.Uris.SETTINGS_PROVIDER_SIMMOBILITY_URI.toString(), phoneId), (ContentObserver) null);
        int status = 0;
        if (SimUtil.isSimMobilityFeatureEnabled()) {
            status = 1;
        }
        Mno mno = simManager.getSimMno();
        if (!simManager.isLabSimCard() && mno != Mno.SAMSUNG && mno != Mno.GCF && CollectionUtils.isNullOrEmpty((Collection<?>) simManager.getNetworkNames())) {
            status = 2;
        }
        if (RegistrationUtils.hasSimMobilityProfile(phoneId)) {
            status = 4;
        }
        ContentValues cv = new ContentValues();
        cv.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 0);
        cv.put(DiagnosisConstants.DRPT_KEY_SIM_MOBILITY_ENABLED, Integer.valueOf(status));
        ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, "DRPT", cv);
        this.mEventLog.logAndAdd(phoneId, "notifySimMobilityStatusChanged: " + status);
        IMSLog.c(LogClass.REGI_SIMMO_STATE_CHANGED, phoneId + ",SIMMO:" + status);
        RemoteCallbackList<ISimMobilityStatusListener> rcl = SlotBasedConfig.getInstance(phoneId).getSimMobilityStatusListeners();
        if (rcl != null) {
            for (int i = rcl.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    rcl.getBroadcastItem(i).onSimMobilityStateChanged(newSimMobility);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            Log.i(IRegistrationManager.LOG_TAG, "notify SimMobilityStatusChanged, finish");
            rcl.finishBroadcast();
        }
    }

    public synchronized void registerSimMobilityStatusListener(ISimMobilityStatusListener listener, int phoneId) {
        registerSimMobilityStatusListener(listener, true, phoneId);
    }

    public synchronized void registerSimMobilityStatusListener(ISimMobilityStatusListener listener, boolean broadcast, int phoneId) {
        if (listener == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "listener is null..");
            return;
        }
        RemoteCallbackList<ISimMobilityStatusListener> rcl = SlotBasedConfig.getInstance(phoneId).getSimMobilityStatusListeners();
        if (rcl == null) {
            RemoteCallbackList newrcl = new RemoteCallbackList();
            newrcl.register(listener);
            SlotBasedConfig.getInstance(phoneId).setSimMobilityStatusListeners(newrcl);
        } else {
            rcl.register(listener);
        }
        if (broadcast) {
            try {
                listener.onSimMobilityStateChanged(RegistrationUtils.hasSimMobilityProfile(phoneId));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public synchronized void unregisterSimMobilityStatusListener(ISimMobilityStatusListener listener, int phoneId) {
        RemoteCallbackList<ISimMobilityStatusListener> rcl = SlotBasedConfig.getInstance(phoneId).getSimMobilityStatusListeners();
        if (rcl != null) {
            rcl.unregister(listener);
        }
    }

    public synchronized void registerCmcRegiListener(IImsRegistrationListener listener, int phoneId) {
        if (listener == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "registerCmcRegiListener: listener is null..");
            return;
        }
        RemoteCallbackList<IImsRegistrationListener> rcl = SlotBasedConfig.getInstance(phoneId).getCmcRegistrationListeners();
        if (rcl == null) {
            RemoteCallbackList newrcl = new RemoteCallbackList();
            newrcl.register(listener);
            SlotBasedConfig.getInstance(phoneId).setCmcRegistrationListeners(newrcl);
        } else {
            rcl.register(listener);
        }
        for (ImsRegistration registration : SlotBasedConfig.getInstance(phoneId).getImsRegistrations().values()) {
            try {
                if (registration.getPhoneId() == phoneId && RegistrationUtils.isCmcProfile(registration.getImsProfile())) {
                    listener.onRegistered(registration);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void unregisterCmcRegiListener(IImsRegistrationListener listener, int phoneId) {
        RemoteCallbackList<IImsRegistrationListener> rcl = SlotBasedConfig.getInstance(phoneId).getCmcRegistrationListeners();
        if (rcl != null) {
            rcl.unregister(listener);
        }
    }

    private void notifyCmcRegistration(boolean registered, ImsRegistration registration, ImsRegistrationError error) {
        if (registration.getImsProfile().getCmcType() != 0) {
            Log.d(IRegistrationManager.LOG_TAG, "notifyCmcRegistration(): CmcType: " + registration.getImsProfile().getCmcType());
            RemoteCallbackList<IImsRegistrationListener> rcl = SlotBasedConfig.getInstance(registration.getPhoneId()).getCmcRegistrationListeners();
            if (rcl != null) {
                int i = rcl.beginBroadcast();
                while (i > 0) {
                    i--;
                    if (registered) {
                        try {
                            rcl.getBroadcastItem(i).onRegistered(registration);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else {
                        rcl.getBroadcastItem(i).onDeregistered(registration, error);
                    }
                }
                Log.i(IRegistrationManager.LOG_TAG, "notifyCmcRegistration, finish");
                rcl.finishBroadcast();
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: com.sec.ims.settings.ImsProfile} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: com.sec.ims.settings.ImsProfile} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v20, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: com.sec.ims.settings.ImsProfile} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.ims.settings.ImsProfile getImsProfile(int r7, com.sec.ims.settings.ImsProfile.PROFILE_TYPE r8) {
        /*
            r6 = this;
            r0 = 0
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getImsProfile: profile ["
            r1.append(r2)
            r1.append(r8)
            java.lang.String r2 = "]"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r3 = "RegiMgr"
            com.sec.internal.log.IMSLog.i(r3, r7, r1)
            int[] r1 = com.sec.internal.ims.core.RegistrationManager.AnonymousClass1.$SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE
            int r4 = r8.ordinal()
            r1 = r1[r4]
            r4 = 1
            if (r1 == r4) goto L_0x0080
            r4 = 2
            r5 = 0
            if (r1 == r4) goto L_0x0066
            r4 = 3
            if (r1 == r4) goto L_0x004c
            r4 = 4
            if (r1 == r4) goto L_0x0032
            goto L_0x0085
        L_0x0032:
            com.sec.ims.settings.ImsProfile[] r1 = r6.getProfileList(r7)
            java.util.stream.Stream r1 = java.util.Arrays.stream(r1)
            com.sec.internal.ims.core.-$$Lambda$kmklNreFbxuYb5KlW6j7Qex0mVc r4 = com.sec.internal.ims.core.$$Lambda$kmklNreFbxuYb5KlW6j7Qex0mVc.INSTANCE
            java.util.stream.Stream r1 = r1.filter(r4)
            java.util.Optional r1 = r1.findFirst()
            java.lang.Object r1 = r1.orElse(r5)
            r0 = r1
            com.sec.ims.settings.ImsProfile r0 = (com.sec.ims.settings.ImsProfile) r0
            goto L_0x0085
        L_0x004c:
            com.sec.ims.settings.ImsProfile[] r1 = r6.getProfileList(r7)
            java.util.stream.Stream r1 = java.util.Arrays.stream(r1)
            com.sec.internal.ims.core.-$$Lambda$IrPIiJD6-gETE7opwaXZCeGzKrI r4 = com.sec.internal.ims.core.$$Lambda$IrPIiJD6gETE7opwaXZCeGzKrI.INSTANCE
            java.util.stream.Stream r1 = r1.filter(r4)
            java.util.Optional r1 = r1.findFirst()
            java.lang.Object r1 = r1.orElse(r5)
            r0 = r1
            com.sec.ims.settings.ImsProfile r0 = (com.sec.ims.settings.ImsProfile) r0
            goto L_0x0085
        L_0x0066:
            com.sec.ims.settings.ImsProfile[] r1 = r6.getProfileList(r7)
            java.util.stream.Stream r1 = java.util.Arrays.stream(r1)
            com.sec.internal.ims.core.-$$Lambda$RegistrationManager$Iy3NsSGi16qOIhLuN7pZCFDIt1I r4 = com.sec.internal.ims.core.$$Lambda$RegistrationManager$Iy3NsSGi16qOIhLuN7pZCFDIt1I.INSTANCE
            java.util.stream.Stream r1 = r1.filter(r4)
            java.util.Optional r1 = r1.findFirst()
            java.lang.Object r1 = r1.orElse(r5)
            r0 = r1
            com.sec.ims.settings.ImsProfile r0 = (com.sec.ims.settings.ImsProfile) r0
            goto L_0x0085
        L_0x0080:
            com.sec.ims.settings.ImsProfile r0 = r6.getEmergencyProfile(r7)
        L_0x0085:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "getImsProfile: found ["
            r1.append(r4)
            if (r0 == 0) goto L_0x0096
            java.lang.String r4 = r0.getName()
            goto L_0x0097
        L_0x0096:
            r4 = r0
        L_0x0097:
            r1.append(r4)
            java.lang.String r4 = "] for ["
            r1.append(r4)
            r1.append(r8)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.e(r3, r7, r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.getImsProfile(int, com.sec.ims.settings.ImsProfile$PROFILE_TYPE):com.sec.ims.settings.ImsProfile");
    }

    /* renamed from: com.sec.internal.ims.core.RegistrationManager$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE;

        static {
            int[] iArr = new int[ImsProfile.PROFILE_TYPE.values().length];
            $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE = iArr;
            try {
                iArr[ImsProfile.PROFILE_TYPE.EMERGENCY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE[ImsProfile.PROFILE_TYPE.VOLTE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE[ImsProfile.PROFILE_TYPE.RCS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$ims$settings$ImsProfile$PROFILE_TYPE[ImsProfile.PROFILE_TYPE.CHAT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    static /* synthetic */ boolean lambda$getImsProfile$0(ImsProfile p) {
        return !p.hasEmergencySupport() && DeviceConfigManager.IMS.equalsIgnoreCase(p.getPdn()) && ImsProfile.hasVolteService(p);
    }

    /* access modifiers changed from: protected */
    public ImsProfile getEmergencyProfile(int phoneId) {
        ImsProfile auProfile;
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile:");
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm == null) {
            return null;
        }
        Mno mno = sm.getDevMno();
        if (sm.hasNoSim() || RegistrationUtils.checkAusEmergencyCall(mno, phoneId, sm)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile(no SIM): profile in case of no SIM or AU sales code");
            if (sm.hasNoSim() && !mno.isAus()) {
                mno = sm.getNetMno();
            }
            String mnoName = RegistrationUtils.handleExceptionalMnoName(mno, phoneId, sm);
            if (mno.isAus() && !mnoName.equals(Mno.DEFAULT.getName()) && (auProfile = this.mAuEmergencyProfile.get(phoneId)) != null) {
                return auProfile;
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile(no SIM): mno: " + mnoName);
            for (ImsProfile profile : ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, mnoName, phoneId)) {
                if (profile.hasEmergencySupport()) {
                    if (mno.isAus()) {
                        this.mAuEmergencyProfile.put(phoneId, profile);
                    }
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile(no SIM): profile: " + profile.getName());
                    return profile;
                }
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile(no SIM): no profile found");
            return null;
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile: from SlotBasedConfig");
        List<ImsProfile> list = SlotBasedConfig.getInstance(phoneId).getProfiles();
        if (CollectionUtils.isNullOrEmpty((Collection<?>) list)) {
            IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile: ProfileList is Empty");
            return null;
        }
        synchronized (list) {
            for (ImsProfile profile2 : list) {
                if (profile2.hasEmergencySupport()) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile: profile: " + profile2.getName());
                    return profile2;
                }
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getEmergencyProfile: no profile found");
            return null;
        }
    }

    public void onDmConfigurationComplete() {
        this.mHandler.sendEmptyMessage(29);
    }

    public IRegistrationGovernor getEmergencyGovernor(int phoneId) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask t = (RegisterTask) it.next();
            if (t.mProfile.hasEmergencySupport()) {
                Log.e(IRegistrationManager.LOG_TAG, "getRegistrationGovernor: return Emergency Gvn");
                return t.getGovernor();
            }
        }
        Log.e(IRegistrationManager.LOG_TAG, "getRegistrationGovernor: not found Emergency task");
        return null;
    }

    public IRegistrationGovernor getRegistrationGovernor(int handle) {
        RegisterTask task = getRegisterTaskByRegHandle(handle);
        if (task != null) {
            return task.getGovernor();
        }
        Log.e(IRegistrationManager.LOG_TAG, "getRegistrationGovernor: unknown handle " + handle);
        return null;
    }

    public IRegistrationGovernor getRegistrationGovernorByProfileId(int profileId, int phoneId) {
        return getRegisterTaskByProfileId(profileId, phoneId).getGovernor();
    }

    /* access modifiers changed from: protected */
    public RegisterTask getRegisterTask(int profileId) {
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTask:");
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            RegisterTask task = getRegisterTaskByProfileId(profileId, phoneId);
            if (task != null) {
                return task;
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTask: Not exist matched RegisterTask. Return null..");
        return null;
    }

    /* access modifiers changed from: protected */
    public RegisterTask getRegisterTaskByRegHandle(int handle) {
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                if (task.mReg != null && task.mReg.getHandle() == handle) {
                    return task;
                }
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTaskByRegHandle: can not find handle : " + handle);
        return null;
    }

    /* access modifiers changed from: protected */
    public RegisterTask getRegisterTaskByProfileId(int profileId, int phoneId) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getProfile().getId() == profileId) {
                return task;
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "getRegisterTaskByProfileId: can not find profile id : " + profileId);
        return null;
    }

    public void requestTryRegister(int phoneId) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(phoneId)));
    }

    public void requestTryRegsiter(int phoneId, long delay) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(2, Integer.valueOf(phoneId)), delay);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a7, code lost:
        if (r11.getMno().isOneOf(com.sec.internal.constants.Mno.ATT, com.sec.internal.constants.Mno.KDDI, com.sec.internal.constants.Mno.H3G_AT) != false) goto L_0x00a9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getPublicUserIdentity(com.sec.internal.ims.core.RegisterTask r11, com.sec.internal.interfaces.ims.core.ISimManager r12) {
        /*
            r10 = this;
            int r0 = r11.getPhoneId()
            com.sec.ims.settings.ImsProfile r1 = r11.getProfile()
            java.lang.String r2 = ""
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r3 = r11.getGovernor()
            int r3 = r3.getNextImpuType()
            r4 = 1
            java.lang.String r5 = "RegiMgr"
            if (r3 != r4) goto L_0x001d
            java.lang.String r2 = r12.getDerivedImpu()
            goto L_0x00be
        L_0x001d:
            boolean r3 = r1.hasEmergencySupport()
            r6 = 11
            if (r3 == 0) goto L_0x0069
            boolean r3 = r1.isUicclessEmergency()
            if (r3 == 0) goto L_0x0069
            java.lang.String r3 = "profile.hasEmergencySupport() && profile.isUicclessEmergency()"
            com.sec.internal.log.IMSLog.i(r5, r3)
            java.lang.String r2 = r12.getEmergencyImpu()
            com.sec.internal.constants.Mno r3 = r11.getMno()
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.VZW
            if (r3 != r4) goto L_0x0051
            boolean r3 = r12.hasNoSim()
            if (r3 != 0) goto L_0x0051
            com.sec.internal.ims.core.PdnController r3 = r10.mPdnController
            boolean r3 = r3.hasEmergencyServiceOnly(r0)
            if (r3 == 0) goto L_0x0051
            java.lang.String r2 = r12.getDerivedImpu()
            goto L_0x00be
        L_0x0051:
            com.sec.internal.constants.Mno r3 = r11.getMno()
            boolean r3 = r3.isKor()
            if (r3 == 0) goto L_0x00be
            boolean r3 = r12.hasNoSim()
            if (r3 != 0) goto L_0x00be
            java.lang.String r3 = r10.getPreferredImpuOnPdn(r6, r0)
            if (r3 == 0) goto L_0x0068
            r2 = r3
        L_0x0068:
            goto L_0x00be
        L_0x0069:
            boolean r3 = r1.hasEmergencySupport()
            if (r3 == 0) goto L_0x00b4
            boolean r3 = r1.isUicclessEmergency()
            if (r3 != 0) goto L_0x00b4
            java.lang.String r3 = "profile.hasEmergencySupport() && !profile.isUicclessEmergency()"
            com.sec.internal.log.IMSLog.i(r5, r0, r3)
            java.lang.String r3 = r10.getPreferredImpuOnPdn(r6, r0)
            if (r3 == 0) goto L_0x0082
            r2 = r3
        L_0x0082:
            boolean r6 = android.text.TextUtils.isEmpty(r2)
            if (r6 != 0) goto L_0x00a9
            boolean r6 = com.sec.internal.ims.core.sim.SimManager.isValidImpu(r2)
            if (r6 == 0) goto L_0x00a9
            com.sec.internal.constants.Mno r6 = r11.getMno()
            r7 = 3
            com.sec.internal.constants.Mno[] r7 = new com.sec.internal.constants.Mno[r7]
            r8 = 0
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.ATT
            r7[r8] = r9
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.KDDI
            r7[r4] = r8
            r4 = 2
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.H3G_AT
            r7[r4] = r8
            boolean r4 = r6.isOneOf(r7)
            if (r4 == 0) goto L_0x00b3
        L_0x00a9:
            com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager r4 = r10.mRcsPolicyManager
            java.lang.String r4 = r4.getRcsPublicUserIdentity(r0)
            java.lang.String r2 = com.sec.internal.ims.core.RegistrationUtils.getPublicUserIdentity(r1, r0, r4, r12)
        L_0x00b3:
            goto L_0x00be
        L_0x00b4:
            com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager r3 = r10.mRcsPolicyManager
            java.lang.String r3 = r3.getRcsPublicUserIdentity(r0)
            java.lang.String r2 = com.sec.internal.ims.core.RegistrationUtils.getPublicUserIdentity(r1, r0, r3, r12)
        L_0x00be:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "impu : "
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.d(r5, r0, r3)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.getPublicUserIdentity(com.sec.internal.ims.core.RegisterTask, com.sec.internal.interfaces.ims.core.ISimManager):java.lang.String");
    }

    /* access modifiers changed from: protected */
    public boolean validateImpu(RegisterTask task, String impu) {
        int phoneId = task.getPhoneId();
        ImsProfile profile = task.getProfile();
        if (((task.getMno() == Mno.CMCC || task.getMno() == Mno.CU) && profile.hasEmergencySupport() && !profile.isUicclessEmergency()) || SimManager.isValidImpu(impu)) {
            return true;
        }
        IMSLog.e(IRegistrationManager.LOG_TAG, phoneId, "error : invalid IMPU");
        this.mEventLog.logAndAdd(phoneId, task, "registerInternal : error - invalid IMPU");
        IMSLog.c(LogClass.REGI_INVALID_IMPU, phoneId + ",REG FAIL:INVALD IMPU");
        task.setReason("");
        return false;
    }

    public String getHomeNetworkDomain(ImsProfile profile, int phoneId) {
        return RegistrationUtils.getHomeNetworkDomain(this.mContext, profile, phoneId, this.mTelephonyManager, this.mRcsPolicyManager, getSimManager(phoneId));
    }

    public String getPrivateUserIdentity(RegisterTask task) {
        int phoneId = task.getPhoneId();
        String impi = getImpi(task.getProfile(), phoneId);
        if (!task.isRcsOnly()) {
            return impi;
        }
        if (task.getMno() == Mno.SINGTEL || task.getMno() == Mno.STARHUB || task.getMno() == Mno.RJIL) {
            return RcsConfigurationHelper.getUserName(this.mContext, phoneId);
        }
        return impi;
    }

    public String getImpi(ImsProfile profile, int phoneId) {
        return RegistrationUtils.getPrivateUserIdentity(this.mContext, profile, phoneId, this.mTelephonyManager, this.mRcsPolicyManager, getSimManager(phoneId));
    }

    /* access modifiers changed from: protected */
    public String getInterfaceName(RegisterTask task, String currentPcscfIp, int phoneId) {
        String ifacename;
        if (!ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(ConfigUtil.getAcsServerType(this.mContext, phoneId)) || !task.isRcsOnly()) {
            ifacename = this.mPdnController.getInterfaceName(task);
        } else {
            ifacename = this.mRcsPolicyManager.changeRcsIfacename(task, this.mPdnController, currentPcscfIp);
        }
        if (task.getProfile() == null) {
            return ifacename;
        }
        int cmcType = task.getProfile().getCmcType();
        if (cmcType == 7 || cmcType == 8) {
            return "p2p-wlan0-0";
        }
        if (cmcType == 5) {
            return "swlan0";
        }
        return ifacename;
    }

    public ImsRegistration getRegistrationInfo(int profileId) {
        int phoneId = ImsConstants.Phone.SLOT_1;
        if (profileId >= 20000) {
            phoneId = ImsConstants.Phone.SLOT_2;
        } else if (profileId >= 10000) {
            phoneId = ImsConstants.Phone.SLOT_1;
        } else if (profileId >= 1000) {
            phoneId = ImsConstants.Phone.SLOT_2;
        }
        return RegistrationUtils.getRegistrationInfo(phoneId, profileId);
    }

    public NetworkEvent getNetworkEvent(int phoneId) {
        return RegistrationUtils.getNetworkEvent(phoneId);
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int phoneId) {
        return RegistrationUtils.getRegistrationInfoByPhoneId(phoneId, getRegistrationInfo());
    }

    public ImsProfile[] getProfileList(int phoneId) {
        return RegistrationUtils.getProfileList(phoneId);
    }

    public int getCmcLineSlotIndex() {
        return this.mCmcAccountManager.getCurrentLineSlotIndex();
    }

    public void releaseThrottleByAcs(int slot) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(slot).iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RegisterTask task = (RegisterTask) it.next();
            if (task.getPhoneId() == slot && ImsProfile.hasRcsService(task.getProfile())) {
                task.getGovernor().releaseThrottle(7);
                break;
            }
        }
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(slot)));
    }

    public void releaseThrottleByCmc(IRegisterTask task) {
        if (task.getGovernor().isThrottled()) {
            IMSLog.i(IRegistrationManager.LOG_TAG, task.getPhoneId(), "releaseThrottleByCmc: releaseThrottle");
            task.getGovernor().releaseThrottle(8);
        }
    }

    public void blockVoWifiRegisterOnRoaminByCsfbError(int regihandle, int duration) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(144, regihandle, duration, (Object) null));
    }

    public void updateChatService(int phoneId) {
        this.mHandler.removeMessages(137);
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(137, Integer.valueOf(phoneId)));
    }

    public void updatePcoInfo(int phoneId, String pdn, int pcoValue) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(703, pcoValue, phoneId, pdn));
    }

    public boolean isVoWiFiSupported(int phoneId) {
        boolean hasMmtelOnWiFi = false;
        ImsProfile[] profileList = getProfileList(phoneId);
        int length = profileList.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            ImsProfile profile = profileList[i];
            if (profile.hasService("mmtel", 18) || profile.hasService("mmtel-video", 18)) {
                hasMmtelOnWiFi = true;
            } else {
                i++;
            }
        }
        hasMmtelOnWiFi = true;
        if (!this.mImsFramework.getBoolean(phoneId, GlobalSettingsConstants.Registration.SUPPORT_VOWIFI, false) || !hasMmtelOnWiFi) {
            return false;
        }
        return true;
    }

    public boolean isPdnConnected(ImsProfile profile, int phoneId) {
        if (profile == null) {
            Log.e(IRegistrationManager.LOG_TAG, "isPdnConnected: profile not found.");
            return false;
        }
        RegisterTask task = getRegisterTaskByProfileId(profile.getId(), phoneId);
        if (task == null) {
            Log.e(IRegistrationManager.LOG_TAG, "isPdnConnected: task not found.");
            return false;
        }
        boolean isPdnConnected = this.mPdnController.isConnected(task.getPdnType(), task);
        Log.i(IRegistrationManager.LOG_TAG, "isPdnConnected: " + isPdnConnected + ", PdnType: " + task.getPdnType());
        return isPdnConnected;
    }

    public boolean hasVoLteSim(int phoneId) {
        return RegistrationUtils.hasVoLteSim(phoneId, getSimManager(phoneId).getSimMno(), RegistrationUtils.getPendingRegistrationInternal(phoneId));
    }

    public Map<Integer, ImsRegistration> getRegistrationList() {
        Map<Integer, ImsRegistration> registrationList = new HashMap<>();
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            registrationList.putAll(SlotBasedConfig.getInstance(phoneId).getImsRegistrations());
        }
        return registrationList;
    }

    public boolean isEmergencyCallProhibited(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "isEmergencyCallProhibited:");
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task != null && task.getProfile().getPdnType() == 11 && task.getGovernor().isPse911Prohibited()) {
                return true;
            }
        }
        return false;
    }

    public boolean isEpdnRequestPending(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "isEpdnRequestPending:");
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (task.getProfile().hasEmergencySupport() && task.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
                return true;
            }
        }
        return false;
    }

    public boolean isRcsRegistered(int phoneId) {
        return RegistrationUtils.isRcsRegistered(phoneId, getRegistrationInfo());
    }

    public int isCmcRegistered(int phoneId) {
        Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            int cmcType = task.getProfile().getCmcType();
            if ((cmcType == 1 || cmcType == 2) && task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                return task.getProfile().getId();
            }
        }
        return 0;
    }

    public int getTelephonyCallStatus(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getTelephonyCallStatus:");
        SlotBasedConfig.RegisterTaskList rtl = RegistrationUtils.getPendingRegistrationInternal(phoneId);
        if (rtl == null) {
            return -1;
        }
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        boolean hasImsCall = iVolteServiceModule != null && iVolteServiceModule.getSessionCount(phoneId) > 0;
        IMSLog.d(IRegistrationManager.LOG_TAG, phoneId, "getTelephonyCallStatus: hasImsCall = " + hasImsCall);
        Iterator it = rtl.iterator();
        while (it.hasNext()) {
            RegisterTask t = (RegisterTask) it.next();
            if (t.mProfile.hasEmergencySupport()) {
                return 0;
            }
            if (!hasImsCall && this.mTelephonyManager.getVoiceNetworkType(SimUtil.getSubId(phoneId)) == 0 && t.getRegistrationRat() == 18 && t.getProfile().getPdn().equals(DeviceConfigManager.IMS) && t.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
                IMSLog.d(IRegistrationManager.LOG_TAG, phoneId, "getTelephonyCallStatus: Have No normal IMS/CS call => allow VoWifi registration.");
                return 0;
            }
        }
        return this.mTelephonyManager.getCallState(phoneId);
    }

    public void setSSACPolicy(int phoneId, boolean enabled) {
        SlotBasedConfig.getInstance(phoneId).enableSsac(enabled);
        if (!enabled) {
            this.mHandler.removeMessages(121, Integer.valueOf(phoneId));
        }
    }

    public void notifyRomaingSettingsChanged(int roamingSettingsOn, int phoneId) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(46, roamingSettingsOn, phoneId, (Object) null));
    }

    public void notifyRCSAllowedChangedbyMDM() {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(53));
    }

    public Set<String> getServiceForNetwork(ImsProfile profile, int network, boolean isRcsForEur, int phoneId) {
        Set<String> services;
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getServiceForNetwork: network " + network);
        int network2 = NetworkEvent.blurNetworkType(network);
        Set<String> services2 = new HashSet<>();
        if (!profile.getNetworkSet().contains(Integer.valueOf(network2))) {
            return services2;
        }
        Set<String> services3 = profile.getServiceSet(Integer.valueOf(network2));
        if (isRcsForEur) {
            services3 = profile.getAllServiceSetFromAllNetwork();
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getServiceForNetwork: service " + services);
        if (profile.hasEmergencySupport()) {
            return services;
        }
        RegisterTask task = getRegisterTaskByProfileId(profile.getId(), phoneId);
        if (task != null) {
            task.clearFilteredReason();
            services = RegistrationUtils.filterserviceFbe(this.mContext, task.getGovernor().filterService(services, network2), task.getProfile());
            if (services == null) {
                services = new HashSet<>();
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getServiceForNetwork: filtered service " + services);
        }
        return services;
    }

    public void addPendingUpdateRegistration(IRegisterTask task, int delay) {
        task.setPendingUpdate(true);
        this.mHandler.removeMessages(32);
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(32, task), ((long) delay) * 1000);
    }

    /* access modifiers changed from: protected */
    public String getUuid(int phoneId, ImsProfile profile) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getUuid:");
        if (!profile.isEnableSessionId()) {
            return "";
        }
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Log.i(IRegistrationManager.LOG_TAG, "UUID=" + uuid);
        return uuid;
    }

    /* access modifiers changed from: protected */
    public String getInstanceId(int phoneId, int pdn, ImsProfile profile) {
        Mno mno = SimUtil.getMno();
        if (pdn == 11 || pdn == 15 || ((mno != Mno.CMCC || !ImsProfile.isRcsUp24Profile(profile.getRcsProfile())) && mno != Mno.MTS_RUSSIA)) {
            String instanceId = getInstanceId(phoneId);
            IMSLog.s(IRegistrationManager.LOG_TAG, "getInstanceId by phoneId: " + instanceId);
            return instanceId;
        }
        String instanceId2 = new TimeBasedUuidGenerator(phoneId, this.mContext).getUuidInstanceId();
        IMSLog.s(IRegistrationManager.LOG_TAG, "getInstanceId time based uuid: " + instanceId2);
        return instanceId2;
    }

    private String getInstanceId(int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getInstanceId:");
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm == null) {
            return "";
        }
        String instanceId = ImsSharedPrefHelper.getString(phoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, "instanceId", (String) null);
        String slotId = ImsSharedPrefHelper.getString(phoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, "slotId", (String) null);
        if (!TextUtils.isEmpty(instanceId) && instanceId.contains(SSOContentProviderConstants.ResultFields.IMEI) && !"0>".equals(instanceId.substring(instanceId.length() - 2))) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "remove invalid instance-ID=" + instanceId);
            instanceId = "";
        }
        if (TextUtils.isEmpty(instanceId) || TextUtils.isEmpty(slotId) || Integer.parseInt(slotId) != sm.getSimSlotIndex()) {
            String deviceId = this.mTelephonyManager.getDeviceId(sm.getSimSlotIndex());
            if (sm.hasNoSim()) {
                deviceId = this.mTelephonyManager.getDeviceId();
            }
            if (TextUtils.isEmpty(deviceId) || sm.hasVsim()) {
                instanceId = "<urn:uuid:" + UUID.randomUUID().toString() + ">";
            } else {
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "deviceId len: " + deviceId.length());
                String imei = deviceId;
                if (deviceId.length() < 14) {
                    Log.i(IRegistrationManager.LOG_TAG, "Invalid deviceId. Read imei again");
                    imei = this.mTelephonyManager.getImei();
                }
                String meid = this.mTelephonyManager.getMeid();
                if (!TextUtils.isEmpty(imei) && imei.length() >= 14) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getInstanceId: imei len=" + imei.length());
                    instanceId = "<urn:gsma:imei:" + IRegistrationManager.getFormattedDeviceId(imei) + ">";
                } else if (TextUtils.isEmpty(meid) || meid.length() < 14) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getInstanceId: imei/meid seems be wrong!");
                } else {
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "getInstanceId: meid len=" + meid.length());
                    instanceId = "<urn:device-id:meid:" + IRegistrationManager.getFormattedDeviceId(meid) + ">";
                }
            }
            ImsSharedPrefHelper.save(phoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, "instanceId", instanceId);
            ImsSharedPrefHelper.save(phoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, "slotId", Integer.toString(sm.getSimSlotIndex()));
        }
        return instanceId;
    }

    public String getAvailableNetworkType(String service) {
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            for (ImsRegistration registrationEntry : SlotBasedConfig.getInstance(phoneId).getImsRegistrations().values()) {
                if (registrationEntry.hasService(service)) {
                    return registrationEntry.getImsProfile().getPdn();
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getPreferredImpuOnPdn(int pdn, int phoneId) {
        IMSLog.i(IRegistrationManager.LOG_TAG, "getPreferredImpuOnPdn: phoneId=" + phoneId + " pdn=" + pdn);
        return (String) SlotBasedConfig.getInstance(phoneId).getImsRegistrations().values().stream().filter(new Predicate(pdn) {
            public final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return RegistrationManager.lambda$getPreferredImpuOnPdn$1(this.f$0, (ImsRegistration) obj);
            }
        }).findFirst().map($$Lambda$dTmsvAx6w6gLvr7pxl8ostahz84.INSTANCE).map($$Lambda$_9IspmiJbgZ5lo9huahE6Xzu2es.INSTANCE).orElse((Object) null);
    }

    static /* synthetic */ boolean lambda$getPreferredImpuOnPdn$1(int pdn, ImsRegistration reg) {
        return reg.getImsProfile().getPdnType() == pdn;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x021d  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0256  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0267  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0278  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0284  */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x02a8  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x02bd  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x02c4  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x02fc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String buildUserAgentString(com.sec.ims.settings.ImsProfile r19, java.lang.String r20, int r21) {
        /*
            r18 = this;
            r1 = r18
            r2 = r21
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r3 = r0
            android.content.Context r0 = r1.mContext
            java.lang.String r4 = "volte"
            int r0 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r0, (java.lang.String) r4, (int) r2)
            r5 = 1
            if (r0 != r5) goto L_0x0018
            r0 = r5
            goto L_0x0019
        L_0x0018:
            r0 = 0
        L_0x0019:
            r6 = r0
            java.lang.String r0 = r19.getMnoName()
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.fromName(r0)
            r8 = 3
            if (r6 == 0) goto L_0x00c4
            com.sec.internal.constants.ims.os.NetworkEvent r0 = r1.getNetworkEvent(r2)
            r9 = 20
            int r10 = r0.network
            if (r9 != r10) goto L_0x0035
            java.lang.String r9 = "EPSFB"
            r3.add(r9)
            goto L_0x003a
        L_0x0035:
            java.lang.String r9 = "VoLTE"
            r3.add(r9)
        L_0x003a:
            android.content.Context r9 = r1.mContext
            java.lang.String r10 = "rcs"
            int r9 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r9, (java.lang.String) r10, (int) r2)
            if (r9 != r5) goto L_0x0061
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.TMOUS
            if (r7 != r9) goto L_0x0059
            java.lang.String r9 = "ft_http"
            r10 = r19
            boolean r9 = r10.hasService(r9)
            if (r9 == 0) goto L_0x005b
            java.lang.String r9 = "RCSUP"
            r3.add(r9)
            goto L_0x0063
        L_0x0059:
            r10 = r19
        L_0x005b:
            java.lang.String r9 = "RCS"
            r3.add(r9)
            goto L_0x0063
        L_0x0061:
            r10 = r19
        L_0x0063:
            java.util.List r9 = r1.getPendingRegistration(r2)
            boolean r11 = r9.isEmpty()
            if (r11 != 0) goto L_0x007e
            boolean r11 = r19.isEpdgSupported()
            if (r11 == 0) goto L_0x007e
            java.lang.String r11 = "ePDG"
            boolean r12 = r3.contains(r11)
            if (r12 != 0) goto L_0x007e
            r3.add(r11)
        L_0x007e:
            android.content.Context r11 = r1.mContext
            java.lang.String r12 = "mmtel-video"
            int r11 = com.sec.internal.helper.DmConfigHelper.getImsSwitchValue((android.content.Context) r11, (java.lang.String) r12, (int) r2)
            if (r11 != r5) goto L_0x008d
            java.lang.String r5 = "IR94"
            r3.add(r5)
        L_0x008d:
            boolean r5 = r9.isEmpty()
            if (r5 != 0) goto L_0x00ab
            java.lang.String r5 = "RTT"
            boolean r11 = r3.contains(r5)
            if (r11 != 0) goto L_0x00ab
            int r11 = r19.getTtyType()
            r12 = 4
            if (r11 == r12) goto L_0x00a8
            int r11 = r19.getTtyType()
            if (r11 != r8) goto L_0x00ab
        L_0x00a8:
            r3.add(r5)
        L_0x00ab:
            boolean r5 = r9.isEmpty()
            if (r5 != 0) goto L_0x00c3
            boolean r5 = r19.getSupport3gppUssi()
            if (r5 == 0) goto L_0x00c3
            java.lang.String r5 = "ussd"
            boolean r11 = r3.contains(r5)
            if (r11 != 0) goto L_0x00c3
            r3.add(r5)
        L_0x00c3:
            goto L_0x00cb
        L_0x00c4:
            r10 = r19
            java.lang.String r0 = "TAS"
            r3.add(r0)
        L_0x00cb:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.TMOUS
            if (r7 != r0) goto L_0x00d4
            java.lang.String r0 = "VVM"
            r3.add(r0)
        L_0x00d4:
            java.lang.String r0 = ""
            java.lang.String r5 = "-"
            java.util.Iterator r9 = r3.iterator()
            r11 = r0
        L_0x00dd:
            boolean r0 = r9.hasNext()
            if (r0 == 0) goto L_0x00f8
            java.lang.Object r0 = r9.next()
            java.lang.String r0 = (java.lang.String) r0
            boolean r12 = r11.isEmpty()
            if (r12 != 0) goto L_0x00f3
            java.lang.String r11 = r11.concat(r5)
        L_0x00f3:
            java.lang.String r11 = r11.concat(r0)
            goto L_0x00dd
        L_0x00f8:
            r0 = r20
            java.lang.String r9 = "[SUPPORT]"
            java.lang.String r0 = r0.replace(r9, r11)
            java.lang.String r9 = android.os.Build.VERSION.RELEASE
            java.lang.String r12 = "[OS_VERSION]"
            java.lang.String r9 = r0.replace(r12, r9)
            java.lang.String r0 = "ro.build.PDA"
            java.lang.String r12 = android.os.SemSystemProperties.get(r0)
            com.samsung.android.feature.SemCscFeature r0 = com.samsung.android.feature.SemCscFeature.getInstance()
            java.lang.String r13 = "CscFeature_IMS_ConfigVerUICCMobilitySpec"
            java.lang.String r14 = "2.0"
            java.lang.String r13 = r0.getString(r2, r13, r14)
            java.lang.String r0 = "[IMEISV]"
            boolean r14 = r9.contains(r0)
            java.lang.String r15 = "RegiMgr"
            if (r14 == 0) goto L_0x01be
            java.lang.String r14 = "iphonesubinfo"
            android.os.IBinder r14 = android.os.ServiceManager.getService(r14)
            com.android.internal.telephony.IPhoneSubInfo r14 = com.android.internal.telephony.IPhoneSubInfo.Stub.asInterface(r14)
            if (r14 == 0) goto L_0x01b9
            com.sec.internal.helper.os.ITelephonyManager r8 = r1.mTelephonyManager     // Catch:{ RemoteException -> 0x01b0 }
            java.lang.String r8 = r8.getDeviceId(r2)     // Catch:{ RemoteException -> 0x01b0 }
            if (r8 == 0) goto L_0x01ab
            int r4 = r8.length()     // Catch:{ RemoteException -> 0x01b0 }
            r16 = r3
            r3 = 14
            if (r4 <= r3) goto L_0x01a8
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x01a4 }
            r4.<init>()     // Catch:{ RemoteException -> 0x01a4 }
            r17 = r5
            r5 = 0
            java.lang.String r3 = r8.substring(r5, r3)     // Catch:{ RemoteException -> 0x01a2 }
            r4.append(r3)     // Catch:{ RemoteException -> 0x01a2 }
            java.lang.String r3 = "imsservice"
            r5 = 0
            java.lang.String r3 = r14.getDeviceSvn(r3, r5)     // Catch:{ RemoteException -> 0x01a2 }
            r4.append(r3)     // Catch:{ RemoteException -> 0x01a2 }
            java.lang.String r3 = r4.toString()     // Catch:{ RemoteException -> 0x01a2 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x01a2 }
            r4.<init>()     // Catch:{ RemoteException -> 0x01a2 }
            java.lang.String r5 = "imeiSV = "
            r4.append(r5)     // Catch:{ RemoteException -> 0x01a2 }
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r3)     // Catch:{ RemoteException -> 0x01a2 }
            r4.append(r5)     // Catch:{ RemoteException -> 0x01a2 }
            java.lang.String r4 = r4.toString()     // Catch:{ RemoteException -> 0x01a2 }
            com.sec.internal.log.IMSLog.d(r15, r4)     // Catch:{ RemoteException -> 0x01a2 }
            if (r3 == 0) goto L_0x01bd
            boolean r4 = r3.isEmpty()     // Catch:{ RemoteException -> 0x01a2 }
            if (r4 != 0) goto L_0x01bd
            java.lang.String r0 = r9.replace(r0, r3)     // Catch:{ RemoteException -> 0x01a2 }
            r4 = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x019f }
            r0.<init>()     // Catch:{ RemoteException -> 0x019f }
            java.lang.String r5 = "inside sipUserAgent = "
            r0.append(r5)     // Catch:{ RemoteException -> 0x019f }
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r4)     // Catch:{ RemoteException -> 0x019f }
            r0.append(r5)     // Catch:{ RemoteException -> 0x019f }
            java.lang.String r0 = r0.toString()     // Catch:{ RemoteException -> 0x019f }
            com.sec.internal.log.IMSLog.d(r15, r0)     // Catch:{ RemoteException -> 0x019f }
            r9 = r4
            goto L_0x01bd
        L_0x019f:
            r0 = move-exception
            r9 = r4
            goto L_0x01b5
        L_0x01a2:
            r0 = move-exception
            goto L_0x01b5
        L_0x01a4:
            r0 = move-exception
            r17 = r5
            goto L_0x01b5
        L_0x01a8:
            r17 = r5
            goto L_0x01bd
        L_0x01ab:
            r16 = r3
            r17 = r5
            goto L_0x01bd
        L_0x01b0:
            r0 = move-exception
            r16 = r3
            r17 = r5
        L_0x01b5:
            r0.printStackTrace()
            goto L_0x01c2
        L_0x01b9:
            r16 = r3
            r17 = r5
        L_0x01bd:
            goto L_0x01c2
        L_0x01be:
            r16 = r3
            r17 = r5
        L_0x01c2:
            android.content.Context r0 = r1.mContext
            java.lang.String r3 = r19.getRcsProfile()
            java.lang.String r0 = com.sec.internal.ims.core.RegistrationUtils.replaceEnablerPlaceholderWithEnablerVersion(r0, r3, r9, r2)
            boolean r3 = r7.isTmobile()
            java.lang.String r4 = "[BUILD_VERSION]"
            java.lang.String r5 = "[OMCCODE]"
            if (r3 != 0) goto L_0x01da
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TELEKOM_ALBANIA
            if (r7 != r3) goto L_0x01fa
        L_0x01da:
            if (r12 == 0) goto L_0x01fa
            int r3 = r12.length()
            r8 = 8
            if (r3 <= r8) goto L_0x01fa
            int r3 = r12.length()
            int r3 = r3 - r8
            java.lang.String r3 = r12.substring(r3)
            java.lang.String r0 = r0.replace(r4, r3)
            java.lang.String r3 = com.sec.internal.helper.OmcCode.getNWCode(r21)
            java.lang.String r0 = r0.replace(r5, r3)
            goto L_0x026b
        L_0x01fa:
            boolean r3 = r7.isKor()
            if (r3 != 0) goto L_0x0206
            boolean r3 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r7)
            if (r3 == 0) goto L_0x0265
        L_0x0206:
            if (r12 == 0) goto L_0x0265
            int r3 = r12.length()
            r8 = 3
            if (r3 <= r8) goto L_0x0265
            boolean r3 = r7.isKor()
            if (r3 == 0) goto L_0x0256
            java.lang.String r3 = "3.0"
            boolean r3 = r13.equals(r3)
            if (r3 == 0) goto L_0x0256
            java.lang.String r3 = "ril.sw_ver"
            java.lang.String r3 = android.os.SemSystemProperties.get(r3)
            int r8 = r3.length()
            r9 = 3
            if (r8 <= r9) goto L_0x0234
            int r8 = r3.length()
            int r8 = r8 - r9
            java.lang.String r3 = r3.substring(r8)
        L_0x0234:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            int r14 = r12.length()
            int r14 = r14 - r9
            java.lang.String r9 = r12.substring(r14)
            r8.append(r9)
            java.lang.String r9 = "_"
            r8.append(r9)
            r8.append(r3)
            java.lang.String r8 = r8.toString()
            java.lang.String r0 = r0.replace(r4, r8)
            goto L_0x026b
        L_0x0256:
            int r3 = r12.length()
            r8 = 3
            int r3 = r3 - r8
            java.lang.String r3 = r12.substring(r3)
            java.lang.String r0 = r0.replace(r4, r3)
            goto L_0x026b
        L_0x0265:
            if (r12 == 0) goto L_0x026b
            java.lang.String r0 = r0.replace(r4, r12)
        L_0x026b:
            java.lang.String r3 = android.os.Build.MODEL
            java.lang.String r4 = "unknown"
            boolean r3 = r4.equals(r3)
            java.lang.String r4 = "[PRODUCT_MODEL]"
            if (r3 == 0) goto L_0x0284
            java.lang.String r3 = "ro.product.base_model"
            java.lang.String r3 = android.os.SemSystemProperties.get(r3)
            java.lang.String r0 = r0.replace(r4, r3)
            goto L_0x02a0
        L_0x0284:
            boolean r3 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((int) r21)
            if (r3 == 0) goto L_0x029a
            boolean r3 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r19)
            if (r3 == 0) goto L_0x029a
            java.lang.String r3 = com.sec.internal.ims.config.ConfigContract.BUILD.getTerminalModel()
            java.lang.String r0 = r0.replace(r4, r3)
            goto L_0x02a0
        L_0x029a:
            java.lang.String r3 = android.os.Build.MODEL
            java.lang.String r0 = r0.replace(r4, r3)
        L_0x02a0:
            java.lang.String r3 = "[CLIENT_VERSION]"
            boolean r4 = r0.contains(r3)
            if (r4 == 0) goto L_0x02b7
            com.sec.internal.interfaces.ims.IImsFramework r4 = r1.mImsFramework
            java.lang.String r8 = "rcs_client_version"
            java.lang.String r9 = "6.0"
            java.lang.String r4 = r4.getString(r2, r8, r9)
            java.lang.String r0 = r0.replace(r3, r4)
        L_0x02b7:
            boolean r3 = com.sec.internal.helper.OmcCode.isSKTOmcCode()
            if (r3 == 0) goto L_0x02c4
            java.lang.String r3 = "SKT"
            java.lang.String r0 = r0.replace(r5, r3)
            goto L_0x02f6
        L_0x02c4:
            boolean r3 = com.sec.internal.helper.OmcCode.isKTTOmcCode()
            if (r3 == 0) goto L_0x02d1
            java.lang.String r3 = "KT"
            java.lang.String r0 = r0.replace(r5, r3)
            goto L_0x02f6
        L_0x02d1:
            boolean r3 = com.sec.internal.helper.OmcCode.isLGTOmcCode()
            if (r3 == 0) goto L_0x02de
            java.lang.String r3 = "LGU"
            java.lang.String r0 = r0.replace(r5, r3)
            goto L_0x02f6
        L_0x02de:
            boolean r3 = com.sec.internal.helper.OmcCode.isKorOpenOmcCode()
            if (r3 != 0) goto L_0x02f0
            boolean r3 = r7.isKor()
            if (r3 == 0) goto L_0x02f6
            boolean r3 = r19.getSimMobility()
            if (r3 == 0) goto L_0x02f6
        L_0x02f0:
            java.lang.String r3 = "OMD"
            java.lang.String r0 = r0.replace(r5, r3)
        L_0x02f6:
            boolean r3 = r7.isKor()
            if (r3 == 0) goto L_0x0302
            java.lang.String r3 = "[UICC_VERSION]"
            java.lang.String r0 = r0.replace(r3, r13)
        L_0x0302:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "buildUserAgentString: isVoLteEnabled="
            r3.append(r4)
            r3.append(r6)
            java.lang.String r4 = ", sipUserAgent="
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r15, r3)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManager.buildUserAgentString(com.sec.ims.settings.ImsProfile, java.lang.String, int):java.lang.String");
    }

    /* access modifiers changed from: protected */
    public void updateVceConfig(IRegisterTask task, boolean config) {
        this.mRegStackIf.updateVceConfig(task, config);
    }

    /* access modifiers changed from: protected */
    public void logTask() {
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            StringBuilder taskInfo = new StringBuilder("RegisterTask(s): ");
            List<IRegisterTask> rtl = getPendingRegistration(phoneId);
            if (CollectionUtils.isNullOrEmpty((Collection<?>) rtl)) {
                taskInfo.append("Nothing!");
            } else {
                for (IRegisterTask task : rtl) {
                    taskInfo.append(task.getProfile().getName());
                    taskInfo.append(" (");
                    taskInfo.append(task.getState());
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                        taskInfo.append(", rat = ");
                        taskInfo.append(task.getRegistrationRat());
                        taskInfo.append(", service = ");
                        taskInfo.append((String) Optional.ofNullable(task.getImsRegistration()).map($$Lambda$vp9VQZqovRDuKML0z61DdV3B8.INSTANCE).map($$Lambda$CrPRuRICJfUpzGfdpT9eJ5q5dQ.INSTANCE).orElse(""));
                    }
                    taskInfo.append("), ");
                }
                IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, taskInfo.toString().replaceAll(", $", ""));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updatePani(int phoneId) {
        Iterator it = RegistrationUtils.getPendingRegistrationInternal(phoneId).iterator();
        while (it.hasNext()) {
            RegisterTask task = (RegisterTask) it.next();
            if (!task.getProfile().hasEmergencySupport()) {
                if (!task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING) && TextUtils.isEmpty(task.getProfile().getLastPaniHeader())) {
                }
            }
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "updatePani " + task);
            this.mRegStackIf.updatePani(task);
        }
    }

    public void dump() {
        IMSLog.dump(IRegistrationManager.LOG_TAG, "Dump of RegistrationManager:");
        IMSLog.increaseIndent(IRegistrationManager.LOG_TAG);
        IMSLog.dump(IRegistrationManager.LOG_TAG, "GCF mode: [" + DeviceUtil.getGcfMode() + "]");
        IMSLog.dump(IRegistrationManager.LOG_TAG, "RegisterTask(s) -");
        for (int phoneId = 0; phoneId < this.mSimManagers.size(); phoneId++) {
            Iterator it = SlotBasedConfig.getInstance(phoneId).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask task = (RegisterTask) it.next();
                IMSLog.dump(IRegistrationManager.LOG_TAG, "SIM slot: [" + task.getPhoneId() + "] state: [" + task.getState() + "] IMS Profile: [" + task.getProfile() + "]");
                StringBuilder sb = new StringBuilder();
                sb.append("Governor: ");
                sb.append(task.getGovernor());
                IMSLog.dump(IRegistrationManager.LOG_TAG, sb.toString());
            }
        }
        this.mEventLog.dump();
        IMSLog.decreaseIndent(IRegistrationManager.LOG_TAG);
        this.mRegStackIf.dump();
    }

    /* access modifiers changed from: protected */
    public void reportRegistrationStatus(IRegisterTask task) {
        int phoneId = task.getPhoneId();
        ContentValues cv = new ContentValues();
        int failReason = DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode();
        int lastFailReason = task.getLastRegiFailReason();
        if (task.getUserAgent() != null) {
            SipError error = task.getUserAgent().getErrorCode();
            if (error != null) {
                failReason = error.getCode();
            }
            int rat = task.getRegistrationRat();
            ImsProfile profile = task.getProfile();
            Set<String> registeredSvc = (Set) Optional.ofNullable(task.getImsRegistration()).map($$Lambda$vp9VQZqovRDuKML0z61DdV3B8.INSTANCE).orElse(new HashSet());
            if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && failReason == DiagnosisConstants.REGI_FRSN.OK.getCode()) {
                if (!(failReason == lastFailReason || lastFailReason == DiagnosisConstants.REGI_FRSN.OK_AFTER_FAIL.getCode())) {
                    failReason = DiagnosisConstants.REGI_FRSN.OK_AFTER_FAIL.getCode();
                }
                if (profile.hasService("mmtel", rat) && !registeredSvc.contains("mmtel")) {
                    failReason = task.getRegiFailReason();
                }
            } else if (task.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
                if (task.getRegiFailReason() < DiagnosisConstants.REGI_FRSN.OFFSET_DEREGI_REASON.getCode()) {
                    cv.put(DiagnosisConstants.REGI_KEY_FAIL_COUNT, Integer.valueOf(task.getGovernor().getFailureCount()));
                } else {
                    failReason = task.getRegiFailReason();
                }
                task.setRegiFailReason(DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode());
            }
            cv.put(DiagnosisConstants.REGI_KEY_DATA_RAT_TYPE, Integer.valueOf(rat));
            cv.put(DiagnosisConstants.REGI_KEY_SERVICE_SET_ALL, DiagnosisConstants.convertServiceSetToHex(profile.getServiceSet(Integer.valueOf(rat))));
            if (!registeredSvc.isEmpty()) {
                cv.put(DiagnosisConstants.REGI_KEY_SERVICE_SET_REGISTERED, DiagnosisConstants.convertServiceSetToHex(registeredSvc));
            }
            cv.put(DiagnosisConstants.REGI_KEY_PANI_PREFIX, Integer.valueOf(DiagnosisConstants.getPaniPrefix(task.getPani())));
            cv.put(DiagnosisConstants.REGI_KEY_PDN_TYPE, Integer.valueOf(DiagnosisConstants.getPdnType(profile.getPdn())));
            cv.put(DiagnosisConstants.REGI_KEY_PCSCF_ORDINAL, Integer.valueOf(task.getGovernor().getPcscfOrdinal()));
            cv.put("ROAM", Integer.valueOf(this.mPdnController.isDataRoaming(phoneId) ? 1 : 0));
            IVolteServiceModule iVolteServiceModule = this.mVsm;
            if (iVolteServiceModule != null) {
                cv.put(DiagnosisConstants.REGI_KEY_SIGNAL_STRENGTH, Integer.valueOf(Math.max(0, iVolteServiceModule.getSignalLevel())));
            }
        } else {
            failReason = task.getRegiFailReason();
        }
        cv.put(DiagnosisConstants.REGI_KEY_REQUEST_CODE, Integer.valueOf(task.getRegiRequestType().getCode()));
        cv.put(DiagnosisConstants.REGI_KEY_FAIL_REASON, Integer.valueOf(failReason));
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportRegiStatus: reason [" + failReason + "], prev [" + lastFailReason + "]");
        if (failReason > DiagnosisConstants.REGI_FRSN.UNKNOWN.getCode() && failReason != DiagnosisConstants.REGI_FRSN.OK.getCode()) {
            ImsLogAgentUtil.sendLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_REGI, cv);
        }
        reportRcsChatRegistrationStatus(task);
        task.setLastRegiFailReason(failReason);
    }

    /* access modifiers changed from: protected */
    public void reportRcsChatRegistrationStatus(IRegisterTask task) {
        if (ImsProfile.hasRcsService(task.getProfile())) {
            ContentValues drcs = new ContentValues();
            int phoneId = task.getPhoneId();
            int rcsRegState = 0;
            if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                Set<String> checkRcs = new HashSet<>();
                checkRcs.addAll((Collection) Optional.ofNullable(task.getImsRegistration()).map($$Lambda$vp9VQZqovRDuKML0z61DdV3B8.INSTANCE).orElse(new HashSet()));
                if (checkRcs.removeAll(Arrays.asList(ImsProfile.getChatServiceList()))) {
                    rcsRegState = 2;
                } else if (checkRcs.removeAll(Arrays.asList(ImsProfile.getRcsServiceList()))) {
                    rcsRegState = 1;
                }
            }
            drcs.put(DiagnosisConstants.KEY_SEND_MODE, 1);
            drcs.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 0);
            drcs.put(DiagnosisConstants.DRCS_KEY_RCS_REGI_STATUS, Integer.valueOf(rcsRegState));
            ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, drcs);
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportRcsRegiStatus: " + rcsRegState);
        }
    }

    /* access modifiers changed from: protected */
    public void reportRegistrationCount(IRegisterTask task) {
        StringBuilder key = new StringBuilder("R");
        int pdnType = task.getPdnType();
        if (pdnType == -1 || pdnType == 0 || pdnType == 1) {
            key.append("R");
        } else if (pdnType != 11) {
            int phoneId = task.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportRegistrationCount: PDN type [" + pdnType + "] - ignore!");
            return;
        } else {
            key.append("G");
        }
        if (task.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
            key.append("S");
        } else {
            key.append("F");
        }
        int rat = task.getRegistrationRat();
        int networkClass = TelephonyManagerExt.getNetworkClass(rat);
        if (networkClass == 3) {
            if (rat == 18) {
                key.append("W");
            } else {
                key.append(DiagnosisConstants.RCSM_ORST_HTTP);
            }
        } else if (networkClass == 2 || networkClass == 1) {
            key.append("L");
        } else {
            int phoneId2 = task.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId2, "reportRegistrationCount: rat [" + rat + "] - ignore!");
        }
        if (DiagnosisConstants.sRegiCountKey.contains(key.toString())) {
            ContentValues cv = new ContentValues();
            cv.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
            cv.put(key.toString(), 1);
            int phoneId3 = task.getPhoneId();
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId3, "reportRegistrationCount: key [" + key + "]");
            ImsLogAgentUtil.storeLogToAgent(task.getPhoneId(), this.mContext, "DRPT", cv);
        }
    }

    /* access modifiers changed from: protected */
    public void reportDualImsStatus(int phoneId) {
        int status = 0;
        if (SimUtil.isDualIMS()) {
            status = 1;
            if (getRegistrationInfoByPhoneId(1 - phoneId) != null) {
                status = 2;
            }
        }
        ContentValues cv = new ContentValues();
        cv.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 2);
        cv.put(DiagnosisConstants.DRPT_KEY_DUAL_IMS_ACTIVE, Integer.valueOf(status));
        ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, "DRPT", cv);
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "reportDualImsStatus: " + status);
    }

    public IUserAgent getUserAgent(int handle) {
        return this.mRegStackIf.getUserAgent(handle);
    }

    public IUserAgent getUserAgent(String service, int phoneId) {
        return this.mRegStackIf.getUserAgent(service, phoneId);
    }

    public IUserAgent getUserAgentByRegId(int regId) {
        return this.mRegStackIf.getUserAgentByRegId(regId);
    }

    public IUserAgent getUserAgentByImsi(String service, String imsi) {
        return this.mRegStackIf.getUserAgentByImsi(service, imsi);
    }

    public String getImsiByUserAgentHandle(int handle) {
        return this.mRegStackIf.getImsiByUserAgentHandle(handle);
    }

    public IUserAgent[] getUserAgentByPhoneId(int phoneId, String service) {
        return this.mRegStackIf.getUserAgentByPhoneId(phoneId, service);
    }

    public IUserAgent getUserAgentOnPdn(int pdn, int phoneId) {
        return this.mRegStackIf.getUserAgentOnPdn(pdn, phoneId);
    }

    public IUserAgent getUserAgent(String service) {
        return this.mRegStackIf.getUserAgent(service);
    }

    public String getImsiByUserAgent(IUserAgent ua) {
        return this.mRegStackIf.getImsiByUserAgent(ua);
    }

    public void forceNotifyToApp(int phoneId) {
        IConfigModule iConfigModule = this.mConfigModule;
        if (iConfigModule != null && iConfigModule.isRcsEnabled(phoneId)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "forceNotifyToApp");
            Intent intent = new Intent();
            intent.setAction(ImsConstants.Intents.ACTION_SERVICE_UP);
            intent.putExtra(ImsConstants.Intents.EXTRA_ANDORID_PHONE_ID, phoneId);
            intent.setPackage(ImsConstants.Packages.PACKAGE_SEC_MSG);
            intent.addFlags(LogClass.SIM_EVENT);
            this.mContext.sendBroadcast(intent);
        }
    }
}
