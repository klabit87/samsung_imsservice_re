package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class CapabilityRegistration {
    private static final long LAST_SEEN_UNKNOWN = -1;
    private static final String LOG_TAG = "CapabilityRegistration";
    private static final int SET_OWN_CAPABILITIES_DELAY = 500;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityUtil mCapabilityUtil;
    private IRegistrationManager mRegMan;

    CapabilityRegistration(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, IRegistrationManager rm) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mRegMan = rm;
    }

    /* access modifiers changed from: package-private */
    public void onRegistered(Context mContext, ImsRegistration regiInfo, Map<Integer, ImsRegistration> mImsRegInfoList, CapabilityConstants.CapExResult mLastCapExResult, long mOldFeature) {
        int phoneId = regiInfo.getPhoneId();
        setAvailablePhoneId(phoneId);
        IMSLog.i(LOG_TAG, phoneId, "onRegistered: RAT = " + regiInfo.getRegiRat() + ", Services = " + regiInfo.getServices());
        if (!this.mCapabilityUtil.isRegistrationSupported(regiInfo)) {
            Log.e(LOG_TAG, "onRegistered: registration is not supported, return");
            return;
        }
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(50, phoneId, 0, ConfigUtil.getRcsProfileWithFeature(mContext, phoneId, regiInfo.getImsProfile())));
        boolean needPublish = needPublish(regiInfo, phoneId, mImsRegInfoList, mLastCapExResult, mOldFeature);
        boolean needUnpublish = RcsPolicyManager.getRcsStrategy(phoneId).needUnpublish(mImsRegInfoList.get(Integer.valueOf(phoneId)), regiInfo);
        ImsRegistration imsRegInfo = regiInfo;
        this.mCapabilityDiscovery.setImsRegInfoList(phoneId, imsRegInfo);
        setUriGenerator(regiInfo, imsRegInfo, phoneId);
        fallbackToOptions(imsRegInfo, phoneId);
        updateOwnCapabilitiesOnRegi(mContext, imsRegInfo.getOwnNumber(), imsRegInfo.getImpi(), phoneId);
        RcsPolicyManager.getRcsStrategy(phoneId).startServiceBasedOnOmaDmNodes(phoneId);
        this.mCapabilityDiscovery.setNetworkType(this.mRegMan.getCurrentNetwork(imsRegInfo.getHandle()));
        publish(needUnpublish, needPublish, phoneId, imsRegInfo);
        startPoll(phoneId);
        triggerCapexForIncallRegiDeregi(phoneId, regiInfo, imsRegInfo);
        loadUserLastActiveTimeStamp(mContext, phoneId);
        callContactSync(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void onDeregistering(ImsRegistration reg, Map<Integer, ImsRegistration> mImsRegInfoList) {
        int phoneId = reg.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onDeregistering");
        if (reg.getImsProfile() != null && Mno.fromName(reg.getImsProfile().getMnoName()).isRjil()) {
            if (mImsRegInfoList.containsKey(Integer.valueOf(phoneId))) {
                this.mCapabilityDiscovery.triggerCapexForIncallRegiDeregi(phoneId, mImsRegInfoList.get(Integer.valueOf(phoneId)));
            }
            this.mCapabilityDiscovery.notifyOwnCapabilitiesChanged(phoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDeregistered(ImsRegistration regiInfo, Map<Integer, ImsRegistration> mImsRegInfoList) {
        Log.i(LOG_TAG, "onDeregistered");
        processDeregistered(regiInfo, mImsRegInfoList);
        if (this.mCapabilityUtil.isRegistrationSupported(regiInfo)) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(51, regiInfo.getPhoneId(), 0));
        }
    }

    private void processDeregistered(ImsRegistration regiInfo, Map<Integer, ImsRegistration> mImsRegInfoList) {
        this.mCapabilityDiscovery.post(new Runnable(regiInfo, mImsRegInfoList) {
            public final /* synthetic */ ImsRegistration f$1;
            public final /* synthetic */ Map f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                CapabilityRegistration.this.lambda$processDeregistered$0$CapabilityRegistration(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$processDeregistered$0$CapabilityRegistration(ImsRegistration regiInfo, Map mImsRegInfoList) {
        int phoneId = regiInfo.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "processDeregistered");
        if (!mImsRegInfoList.containsKey(Integer.valueOf(phoneId))) {
            Log.i(LOG_TAG, "processDeregistered: already deregistered");
            return;
        }
        Capabilities ownCap = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId));
        ownCap.setAvailiable(false);
        this.mCapabilityDiscovery.getOwnList().put(Integer.valueOf(phoneId), ownCap);
        IMSLog.i(LOG_TAG, phoneId, "processDeregistered: mIsConfiguredOnCapability sets as false.");
        this.mCapabilityDiscovery.setIsConfiguredOnCapability(false, phoneId);
        this.mCapabilityDiscovery.removeImsRegInfoList(phoneId);
        this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(phoneId));
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(5, 0, 0, Integer.valueOf(phoneId)), 500);
    }

    private boolean needPublish(ImsRegistration regiInfo, int phoneId, Map<Integer, ImsRegistration> mImsRegInfoList, CapabilityConstants.CapExResult mLastCapExResult, long mOldFeature) {
        if (!mImsRegInfoList.containsKey(Integer.valueOf(phoneId))) {
            return true;
        }
        if (!regiInfo.hasService("presence")) {
            Log.e(LOG_TAG, "needPublish: do not publish, Presence is not registered.");
            return false;
        } else if (!mImsRegInfoList.get(Integer.valueOf(phoneId)).getServices().equals(regiInfo.getServices()) || mLastCapExResult == CapabilityConstants.CapExResult.USER_NOT_REGISTERED) {
            return true;
        } else {
            if ((this.mCapabilityDiscovery.getCapabilityConfig(phoneId) == null || this.mCapabilityDiscovery.getCapabilityConfig(phoneId).usePresence()) && !this.mCapabilityDiscovery.getPresenceModule().isOwnCapPublished()) {
                return true;
            }
            if (!Mno.fromName(regiInfo.getImsProfile().getMnoName()).isKor() || !regiInfo.hasRcsService()) {
                IMSLog.e(LOG_TAG, phoneId, "needPublish: do not publish, service list is same.");
                return false;
            }
            long newFeature = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId)).getFeature();
            if (mOldFeature != newFeature) {
                this.mCapabilityDiscovery.setOldFeature(newFeature, phoneId);
                IMSLog.e(LOG_TAG, phoneId, "needPublish: do publish, service list is same, but different Features.(KOR RCS only)");
                return true;
            }
            IMSLog.e(LOG_TAG, phoneId, "needPublish: do not publish, service list & feature list are same.");
            return false;
        }
    }

    private void updateOwnCapabilitiesOnRegi(Context mContext, String msisdn, String privateUserId, int phoneId) {
        Capabilities ownCap = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId));
        if (ImsRegistry.getBoolean(phoneId, GlobalSettingsConstants.RCS.ENABLE_RCS_EXTENSIONS, false)) {
            for (Map.Entry<String, ?> entry : mContext.getSharedPreferences("iari_app_association", 0).getAll().entrySet()) {
                String extFeature = new String(Base64.decode(entry.getValue().toString(), 0)).replaceAll(":", "%3A");
                if (!CollectionUtils.isNullOrEmpty(extFeature) && !"default-tag".equals(extFeature)) {
                    ownCap.addExtFeature(extFeature);
                }
            }
            IMSLog.i(LOG_TAG, phoneId, "updateOwnCapabilitiesOnRegi: extFeature = " + ownCap.getExtFeature());
        }
        if (msisdn != null) {
            ownCap.setNumber(msisdn);
            ImsUri ownUri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(ownCap.getNumber(), true);
            if (ownUri != null) {
                ownCap.setUri(ownUri);
            }
        } else if (privateUserId != null && this.mCapabilityDiscovery.getCapabilityControl(phoneId) == this.mCapabilityDiscovery.getOptionsModule()) {
            ownCap.setNumber(this.mCapabilityUtil.extractMsisdnFromUri(privateUserId));
            ImsUri ownUri2 = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(ownCap.getNumber(), true);
            if (ownUri2 != null) {
                ownCap.setUri(ownUri2);
            }
        }
        IMSLog.s(LOG_TAG, phoneId, "updateOwnCapabilitiesOnRegi: own number: " + ownCap.getNumber());
        if (ownCap.getUri() != null) {
            IMSLog.i(LOG_TAG, phoneId, "updateOwnCapabilitiesOnRegi: own uri: " + ownCap.getUri().toStringLimit());
        }
        ownCap.setAvailiable(true);
        ownCap.setTimestamp(new Date());
        ownCap.setPhoneId(phoneId);
        this.mCapabilityDiscovery.putOwnList(phoneId, ownCap);
    }

    private void loadUserLastActiveTimeStamp(Context mContext, int phoneId) {
        SharedPreferences sp = mContext.getSharedPreferences("capdiscovery", 0);
        IMSLog.i(LOG_TAG, phoneId, "load last seen active");
        this.mCapabilityDiscovery.putUserLastActive(phoneId, sp.getLong("lastseenactive_" + SimManagerFactory.getImsiFromPhoneId(phoneId), -1));
    }

    private void publish(boolean needUnpublish, boolean needPublish, int phoneId, ImsRegistration imsRegInfo) {
        if (needUnpublish) {
            IMSLog.i(LOG_TAG, phoneId, "onRegistered : need unpublish, invoke presenceModule to trigger unpublish");
            if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).usePresence()) {
                this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(phoneId));
                this.mCapabilityDiscovery.getPresenceModule().unpublish(phoneId);
            }
        } else if (needPublish || (imsRegInfo.hasService("options") && SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS()) && Mno.fromName(imsRegInfo.getImsProfile().getMnoName()).isRjil())) {
            IMSLog.i(LOG_TAG, phoneId, "onRegistered : need PUBLISH, expecting EVT_SET_OWN_CAPABILITIES(5) after this");
            if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) == null || !this.mCapabilityDiscovery.getCapabilityConfig(phoneId).usePresence()) {
                this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(phoneId));
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(5, 0, 0, Integer.valueOf(phoneId)), 1000);
            } else if (this.mCapabilityDiscovery.getPresenceModule().getRegiInfoUpdater(phoneId)) {
                this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(phoneId));
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(5, 0, 0, Integer.valueOf(phoneId)), 500);
                this.mCapabilityDiscovery.getPresenceModule().setRegiInfoUpdater(phoneId, false);
            } else {
                CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule3.sendMessageDelayed(capabilityDiscoveryModule3.obtainMessage(53, 0, 0, Integer.valueOf(phoneId)), 500);
            }
        }
    }

    private void callContactSync(int phoneId) {
        if (this.mCapabilityDiscovery.getPhonebook().getBlockedInitialContactSyncBeforeRegi()) {
            IMSLog.i(LOG_TAG, phoneId, "callContactSync : set the current time to skip the contact scan.");
            if (this.mCapabilityDiscovery.getPhonebook().getLastRefreshTime() == 0) {
                if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isDisableInitialScan()) {
                    this.mCapabilityDiscovery.getPhonebook().setLastRefreshTime(new Date().getTime());
                } else {
                    this.mCapabilityDiscovery.getPhonebook().setLastRefreshTime(1);
                }
            }
        }
        if (this.mCapabilityDiscovery.getPhonebook().getIsBlockedContactChange() || this.mCapabilityDiscovery.getPhonebook().getBlockedInitialContactSyncBeforeRegi()) {
            IMSLog.i(LOG_TAG, phoneId, "callContactSync : call contact sync if the contact change is blocked.");
            this.mCapabilityDiscovery.getPhonebook().sendMessageContactSync();
        }
    }

    private void startPoll(int phoneId) {
        if (!this.mCapabilityDiscovery.hasMessages(1) && this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollingPeriod() != 0 && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollingRate() != 0) {
            this.mCapabilityDiscovery.startPoll(new Date(), phoneId);
        }
    }

    private void triggerCapexForIncallRegiDeregi(int phoneId, ImsRegistration regiInfo, ImsRegistration imsRegInfo) {
        if (imsRegInfo.getImsProfile() != null && Mno.fromName(imsRegInfo.getImsProfile().getMnoName()).isRjil()) {
            this.mCapabilityDiscovery.triggerCapexForIncallRegiDeregi(phoneId, regiInfo);
        }
    }

    private void setAvailablePhoneId(int phoneId) {
        int mAvailablePhoneId;
        if (RcsUtils.DualRcs.isDualRcsReg()) {
            mAvailablePhoneId = SimUtil.getDefaultPhoneId();
            if (mAvailablePhoneId == -1) {
                mAvailablePhoneId = 0;
            }
        } else {
            mAvailablePhoneId = phoneId;
        }
        this.mCapabilityDiscovery.setAvailablePhoneId(mAvailablePhoneId);
    }

    private void setUriGenerator(ImsRegistration regiInfo, ImsRegistration imsRegInfo, int phoneId) {
        UriGenerator mUriGenerator = UriGeneratorFactory.getInstance().get(imsRegInfo.getPreferredImpu().getUri());
        if (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_SIPURI_FOR_URIGENERATOR)) {
            Iterator it = regiInfo.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NameAddr addr = (NameAddr) it.next();
                if (addr.getUri().getUriType() == ImsUri.UriType.SIP_URI) {
                    mUriGenerator = UriGeneratorFactory.getInstance().get(addr.getUri());
                    break;
                }
            }
        }
        this.mCapabilityDiscovery.setUriGenerator(mUriGenerator);
        this.mCapabilityDiscovery.getPhonebook().setUriGenerator(mUriGenerator);
    }

    private void fallbackToOptions(ImsRegistration imsRegInfo, int phoneId) {
        if (!imsRegInfo.getImsProfile().getServiceSet(Integer.valueOf(imsRegInfo.getRegiRat())).contains("presence") && this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getDefaultDisc() != 2 && this.mCapabilityDiscovery.getOptionsModule().isRunning()) {
            Log.e(LOG_TAG, "fallbackToOptions: Presence is not enabled in ImsProfile.");
            if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null) {
                this.mCapabilityDiscovery.getCapabilityConfig(phoneId).setUsePresence(false);
            }
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.putCapabilityControlForOptionsModule(phoneId, capabilityDiscoveryModule.getOptionsModule());
        }
    }
}
