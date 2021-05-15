package com.sec.internal.ims.core;

import android.content.Context;
import android.os.RemoteCallbackList;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SlotBasedConfig {
    private static final HashMap<Integer, SlotBasedConfig> sInstances = new HashMap<>();
    private boolean mCdmaAvailableForVoice;
    private RemoteCallbackList<IImsRegistrationListener> mCmcRegistrationListeners;
    private boolean mDataUsageExceeded;
    private boolean mEntitlementNsds;
    private ImsIconManager mIconManager;
    private boolean mInviteRejected;
    private NetworkEvent mNetworkEvent;
    private boolean mNotifiedImsNotAvailable;
    private List<ImsProfile> mProfileList = new CopyOnWriteArrayList();
    private Map<Integer, ImsProfile> mProfileListExt = new ConcurrentHashMap();
    private boolean mRTTMode;
    private RegistrationConstants.RegistrationType mRcsVolteSingleRegistration = RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG;
    private Map<Integer, ImsRegistration> mRegistrationList = new ConcurrentHashMap();
    private RemoteCallbackList<IImsRegistrationListener> mRegistrationListeners;
    private RegisterTaskList mRegistrationTasks = new RegisterTaskList();
    private boolean mSSACPolicy;
    private boolean mSimMobilityActivation;
    private RemoteCallbackList<ISimMobilityStatusListener> mSimMobilityStatusListeners;
    private boolean mSuspendRegiWhileIrat;
    private boolean mTTYMode;

    private SlotBasedConfig() {
        clear();
    }

    public static SlotBasedConfig getInstance(int phoneId) {
        synchronized (sInstances) {
            if (sInstances.containsKey(Integer.valueOf(phoneId))) {
                SlotBasedConfig slotBasedConfig = sInstances.get(Integer.valueOf(phoneId));
                return slotBasedConfig;
            }
            SlotBasedConfig config = new SlotBasedConfig();
            sInstances.put(Integer.valueOf(phoneId), config);
            return config;
        }
    }

    public void clear() {
        ImsIconManager imsIconManager = this.mIconManager;
        if (imsIconManager != null) {
            imsIconManager.updateRegistrationIcon(false);
        }
        this.mProfileList.clear();
        this.mProfileListExt.clear();
        this.mRegistrationList.clear();
        this.mRegistrationTasks.clear();
        this.mRcsVolteSingleRegistration = RegistrationConstants.RegistrationType.IMS_PROFILE_BASED_REG;
        this.mTTYMode = false;
        this.mRTTMode = false;
        this.mInviteRejected = false;
        this.mCdmaAvailableForVoice = false;
        this.mEntitlementNsds = false;
        this.mSimMobilityActivation = false;
        this.mSSACPolicy = true;
        this.mSuspendRegiWhileIrat = false;
        this.mDataUsageExceeded = false;
        this.mNotifiedImsNotAvailable = false;
        this.mNetworkEvent = new NetworkEvent();
    }

    /* access modifiers changed from: package-private */
    public ImsIconManager getIconManager() {
        return this.mIconManager;
    }

    /* access modifiers changed from: package-private */
    public void createIconManager(Context context, IRegistrationManager regMgr, PdnController pdnController, Mno mno, int phoneId) {
        this.mIconManager = new ImsIconManager(context, regMgr, pdnController, mno, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void clearProfiles() {
        this.mProfileList.clear();
    }

    public List<ImsProfile> getProfiles() {
        return this.mProfileList;
    }

    /* access modifiers changed from: package-private */
    public void addProfile(ImsProfile profile) {
        this.mProfileList.add(profile);
    }

    /* access modifiers changed from: package-private */
    public Map<Integer, ImsProfile> getExtendedProfiles() {
        return this.mProfileListExt;
    }

    /* access modifiers changed from: package-private */
    public void addExtendedProfile(int profileId, ImsProfile profile) {
        this.mProfileListExt.put(Integer.valueOf(profileId), profile);
    }

    /* access modifiers changed from: package-private */
    public void removeExtendedProfile(int profileId) {
        this.mProfileListExt.remove(Integer.valueOf(profileId));
    }

    public Map<Integer, ImsRegistration> getImsRegistrations() {
        return this.mRegistrationList;
    }

    public void addImsRegistration(int profileId, ImsRegistration registration) {
        this.mRegistrationList.put(Integer.valueOf(profileId), registration);
    }

    /* access modifiers changed from: package-private */
    public RemoteCallbackList<IImsRegistrationListener> getImsRegistrationListeners() {
        return this.mRegistrationListeners;
    }

    /* access modifiers changed from: package-private */
    public void setImsRegistrationListeners(RemoteCallbackList<IImsRegistrationListener> registrationListeners) {
        this.mRegistrationListeners = registrationListeners;
    }

    /* access modifiers changed from: package-private */
    public RemoteCallbackList<IImsRegistrationListener> getCmcRegistrationListeners() {
        return this.mCmcRegistrationListeners;
    }

    /* access modifiers changed from: package-private */
    public void setCmcRegistrationListeners(RemoteCallbackList<IImsRegistrationListener> registrationListeners) {
        this.mCmcRegistrationListeners = registrationListeners;
    }

    /* access modifiers changed from: package-private */
    public RemoteCallbackList<ISimMobilityStatusListener> getSimMobilityStatusListeners() {
        return this.mSimMobilityStatusListeners;
    }

    /* access modifiers changed from: package-private */
    public void setSimMobilityStatusListeners(RemoteCallbackList<ISimMobilityStatusListener> SimMobilityStatusListeners) {
        this.mSimMobilityStatusListeners = SimMobilityStatusListeners;
    }

    public NetworkEvent getNetworkEvent() {
        return this.mNetworkEvent;
    }

    /* access modifiers changed from: package-private */
    public void setNetworkEvent(NetworkEvent networkEvent) {
        this.mNetworkEvent = networkEvent;
    }

    /* access modifiers changed from: package-private */
    public boolean getTTYMode() {
        return this.mTTYMode;
    }

    /* access modifiers changed from: package-private */
    public void setTTYMode(Boolean ttyMode) {
        this.mTTYMode = ttyMode.booleanValue();
    }

    public boolean getRTTMode() {
        return this.mRTTMode;
    }

    public void setRTTMode(Boolean rttMode) {
        this.mRTTMode = rttMode.booleanValue();
    }

    /* access modifiers changed from: package-private */
    public boolean isInviteRejected() {
        return this.mInviteRejected;
    }

    /* access modifiers changed from: package-private */
    public void setInviteReject(boolean rejected) {
        this.mInviteRejected = rejected;
    }

    /* access modifiers changed from: package-private */
    public boolean isCdmaAvailableForVoice() {
        return this.mCdmaAvailableForVoice;
    }

    /* access modifiers changed from: package-private */
    public void setCdmaAvailableForVoice(boolean available) {
        this.mCdmaAvailableForVoice = available;
    }

    public RegisterTaskList getRegistrationTasks() {
        return this.mRegistrationTasks;
    }

    /* access modifiers changed from: package-private */
    public boolean getEntitlementNsds() {
        return this.mEntitlementNsds;
    }

    public void setEntitlementNsds(Boolean isEntitled) {
        this.mEntitlementNsds = isEntitled.booleanValue();
    }

    public void activeSimMobility(boolean active) {
        this.mSimMobilityActivation = active;
    }

    public boolean isSimMobilityActivated() {
        return this.mSimMobilityActivation;
    }

    /* access modifiers changed from: package-private */
    public boolean isSsacEnabled() {
        return this.mSSACPolicy;
    }

    /* access modifiers changed from: package-private */
    public void enableSsac(boolean enabled) {
        this.mSSACPolicy = enabled;
    }

    /* access modifiers changed from: package-private */
    public boolean isSuspendedWhileIrat() {
        return this.mSuspendRegiWhileIrat;
    }

    /* access modifiers changed from: package-private */
    public void setSuspendWhileIrat(boolean suspend) {
        this.mSuspendRegiWhileIrat = suspend;
    }

    /* access modifiers changed from: package-private */
    public boolean isDataUsageExceeded() {
        return this.mDataUsageExceeded;
    }

    /* access modifiers changed from: package-private */
    public void setDataUsageExceed(boolean limited) {
        this.mDataUsageExceeded = limited;
    }

    public boolean isNotifiedImsNotAvailable() {
        return this.mNotifiedImsNotAvailable;
    }

    public void setNotifiedImsNotAvailable(boolean notifiedImsNotAvailable) {
        this.mNotifiedImsNotAvailable = notifiedImsNotAvailable;
    }

    /* access modifiers changed from: package-private */
    public RegistrationConstants.RegistrationType getRcsVolteSingleRegistration() {
        return this.mRcsVolteSingleRegistration;
    }

    /* access modifiers changed from: package-private */
    public void setRcsVolteSingleRegistration(RegistrationConstants.RegistrationType registrationType) {
        this.mRcsVolteSingleRegistration = registrationType;
    }

    public static class RegisterTaskList extends CopyOnWriteArrayList<RegisterTask> {
        public boolean remove(Object o) {
            ((RegisterTask) o).getGovernor().unRegisterIntentReceiver();
            return super.remove(o);
        }

        public void clear() {
            try {
                Iterator it = iterator();
                while (it.hasNext()) {
                    ((RegisterTask) it.next()).getGovernor().unRegisterIntentReceiver();
                }
            } catch (NullPointerException e) {
            }
            super.clear();
        }

        public boolean removeAll(Collection<?> c) {
            for (RegisterTask task : (List) c) {
                task.getGovernor().unRegisterIntentReceiver();
            }
            return super.removeAll(c);
        }
    }
}
