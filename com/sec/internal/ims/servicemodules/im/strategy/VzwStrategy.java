package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public final class VzwStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = VzwStrategy.class.getSimpleName();
    private int lastNetworkType;
    private ICapabilityDiscoveryModule mDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
    private boolean mIsCapDiscoveryOption = true;
    private boolean mIsEABEnabled = true;
    private boolean mIsVLTEnabled = true;
    private final TelephonyManager mTelephonyManager = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY));

    public VzwStrategy(Context context, int phoneId) {
        super(context, phoneId);
        this.mIsVLTEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, false, phoneId).booleanValue();
        this.mIsEABEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, false, phoneId).booleanValue();
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, false, phoneId).booleanValue();
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean isFtHttp) {
        if (isSlmMessage) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode statusCode = getRetryStrategy(currentRetryCount, imError, retryAfter, newContact, chatType);
        return statusCode == IMnoStrategy.StatusCode.NO_RETRY ? handleImFailure(imError, chatType) : new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError ftError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage) {
        IMnoStrategy.StatusCode statusCode = getFtMsrpRetryStrategy(currentRetryCount, ftError, retryAfter, newContact);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleFtFailure(ftError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
        return StrategyUtils.isCapabilityValidUriForUS(uri, this.mPhoneId);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex != null) {
            capex.setFetchType(Capabilities.FetchType.FETCH_TYPE_OTHER);
        }
        if (result != CapabilityConstants.CapExResult.USER_NOT_FOUND && result == CapabilityConstants.CapExResult.FAILURE && capex != null && capex.isAvailable()) {
            return isCapCacheExpired(capex, cacheInfoExpiry);
        }
        return true;
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long capCacheExpiry) {
        ICapabilityDiscoveryModule iCapabilityDiscoveryModule = this.mDiscoveryModule;
        if (iCapabilityDiscoveryModule != null && !iCapabilityDiscoveryModule.hasVideoOwnCapability(this.mPhoneId)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: no video, return false");
            return false;
        } else if (refreshType == CapabilityRefreshType.DISABLED) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch disabled, no refresh");
            return false;
        } else if (refreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch forced, refresh");
            return true;
        } else if (capex == null) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "needRefresh: capability is null, type " + refreshType);
            if (refreshType == CapabilityRefreshType.FORCE_REFRESH_UCE) {
                return true;
            }
            return false;
        } else if (capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER) || isCapCacheExpired(capex, capCacheExpiry)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: fetch-blocked capabilities, no refresh");
            return false;
        } else if (capex.getFetchType() != Capabilities.FetchType.FETCH_TYPE_POLL && !capex.isExpired(capInfoExpiry)) {
            return false;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: cache expired or fetch after poll, refresh");
            capex.setFetchType(Capabilities.FetchType.FETCH_TYPE_OTHER);
            return true;
        }
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long serviceAvailabilityInfoExpiry, long capCacheExpiry, long msgCapvalidity) {
        return needRefresh(capex, refreshType, capInfoExpiry, capCacheExpiry);
    }

    private boolean isCapCacheExpired(Capabilities capex, long cacheInfoExpiry) {
        boolean isCapCacheExpired = false;
        if (capex != null) {
            Date current = new Date();
            if (current.getTime() - capex.getTimestamp().getTime() >= 1000 * cacheInfoExpiry && cacheInfoExpiry > 0) {
                isCapCacheExpired = true;
            }
            if (isCapCacheExpired) {
                capex.resetFeatures();
                String str = TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "isCapCacheExpired: " + cacheInfoExpiry + " current " + current.getTime() + " timestamp " + capex.getTimestamp().getTime() + " diff " + (current.getTime() - capex.getTimestamp().getTime()));
            }
            return isCapCacheExpired;
        }
        IMSLog.i(TAG, this.mPhoneId, "isCapCacheExpired: Capability is null");
        return false;
    }

    public boolean needPoll(Capabilities capex, long capInfoExpiry) {
        return true;
    }

    public long updateFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        if (capex == null || capex.hasFeature(Capabilities.FEATURE_NOT_UPDATED) || capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            return features;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateFeatures: updated features " + Capabilities.dumpFeature(capex.getFeature() | features));
        return capex.getFeature() | features;
    }

    public long isTdelay(long delay) {
        boolean isSVLTEDevice = SemSystemProperties.getBoolean("ro.ril.svlte1x", false);
        if (isSVLTEDevice || delay < 1) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "SVLTE: " + isSVLTEDevice + ", delay: " + delay);
            return 0;
        }
        int networkType = this.mTelephonyManager.getNetworkType();
        TelephonyManagerExt.NetworkTypeExt lastNetExtType = TelephonyManagerExt.getNetworkEnumType(this.lastNetworkType);
        TelephonyManagerExt.NetworkTypeExt netExtType = TelephonyManagerExt.getNetworkEnumType(networkType);
        String str2 = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "SRLTE, current network: " + netExtType + ", last network type : " + lastNetExtType);
        this.lastNetworkType = networkType;
        if (lastNetExtType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD && netExtType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return (delay - 1) * 1000;
        }
        return 0;
    }

    public boolean needUnpublish(int phoneId) {
        TelephonyManagerExt.NetworkTypeExt netExtType = TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType());
        if (netExtType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "needUnpublish: network type: " + netExtType);
            return false;
        }
        boolean isVoLteEnabled = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", phoneId) == 1;
        if (!isVoLteEnabled) {
            String str2 = TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "needUnpublish: isVoLteEnabled: " + isVoLteEnabled);
            return true;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", phoneId) == 1 || DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", phoneId) == 1) {
            return false;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: mmtel/mmtel-video: off");
            return true;
        }
    }

    public boolean needUnpublish(ImsRegistration oldInfo, ImsRegistration newInfo) {
        if (oldInfo == null) {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: oldInfo: empty");
            return false;
        }
        int voiceType = ImsConstants.SystemSettings.VOLTE_SLOT1.get(this.mContext, 0);
        String str = TAG;
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("needUnpublish: getVoiceTechType: ");
        sb.append(voiceType == 0 ? "VOLTE" : "CS");
        IMSLog.i(str, i, sb.toString());
        if ((oldInfo.hasService("mmtel") || oldInfo.hasService("mmtel-video")) && !newInfo.hasService("mmtel") && !newInfo.hasService("mmtel-video") && voiceType == 1) {
            return true;
        }
        return false;
    }

    public boolean isSubscribeThrottled(PresenceSubscription s, long millis, boolean isAvailFetch, boolean isAlwaysForce) {
        if (isAlwaysForce) {
            IMSLog.i(TAG, this.mPhoneId, "refresh type is always force.");
            return false;
        } else if (!isAvailFetch || !(s.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC || s.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE)) {
            Date current = new Date();
            long offset = current.getTime() - s.getTimestamp().getTime();
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "isSubscribeThrottled: interval from " + s.getTimestamp().getTime() + " to " + current.getTime() + ", offset " + offset + " sourceThrottlePublish " + millis);
            if (offset < millis) {
                return true;
            }
            return false;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: avail fetch after poll, not throttled");
            return false;
        }
    }

    public void startServiceBasedOnOmaDmNodes(int phoneId) {
        IMSLog.i(TAG, this.mPhoneId, "startServiceBasedOnOmaDmNodes");
        if (this.mDiscoveryModule != null) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "startServiceBasedOnOmaDmNodes: mIsVLTEnabled: " + this.mIsVLTEnabled + " mIsEABEnabled: " + this.mIsEABEnabled);
            if (!this.mIsVLTEnabled) {
                this.mDiscoveryModule.clearCapabilitiesCache(phoneId);
                this.mDiscoveryModule.changeParalysed(true, phoneId);
            }
        }
    }

    public void updateOmaDmNodes(int phoneId) {
        boolean modified = false;
        boolean newValue = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, false, phoneId).booleanValue();
        if (this.mIsVLTEnabled != newValue) {
            this.mIsVLTEnabled = newValue;
            modified = true;
        }
        boolean newValue2 = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, false, phoneId).booleanValue();
        if (this.mIsEABEnabled != newValue2) {
            this.mIsEABEnabled = newValue2;
            modified = true;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateOmaDmNodes: mIsVLTEnabled: " + this.mIsVLTEnabled + " mIsEABEnabled: " + this.mIsEABEnabled + " modified = " + modified);
        if (modified) {
            startServiceBasedOnOmaDmNodes(phoneId);
        }
    }

    public String checkNeedParsing(String number) {
        if (number == null) {
            return number;
        }
        if (!number.startsWith("*67") && !number.startsWith("*82")) {
            return number;
        }
        String number2 = number.substring(3);
        IMSLog.i(TAG, this.mPhoneId, "parsing for special character");
        return number2;
    }

    public void updateCapDiscoveryOption() {
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, false, this.mPhoneId).booleanValue();
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "update CapDiscoveryOption: " + this.mIsCapDiscoveryOption);
    }

    public boolean checkCapDiscoveryOption() {
        if (TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType()) != TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return true;
        }
        boolean result = this.mIsCapDiscoveryOption;
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "return CapDiscoveryOption: " + result);
        return result;
    }

    public boolean isPresenceReadyToRequest(boolean ownInfoPublished, boolean paralysed) {
        return ownInfoPublished && !paralysed;
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_FORBIDDEN;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_PROVISIONED;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NOT_FOUND;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_INTERVAL_TOO_SHORT;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.NO_RESPONSE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE, PresenceResponse.PresenceFailureReason.DECLINE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_DISABLE_MODE;
    }

    public void changeServiceDescription() {
        IMSLog.i(TAG, this.mPhoneId, "changeServiceDescription: VoLTE Capabilities Discovery");
        ServiceTuple.setServiceDescription((long) Capabilities.FEATURE_PRESENCE_DISCOVERY, "VoLTE Capabilities Discovery");
    }

    public long getThrottledDelay(long delay) {
        return 3 + delay;
    }

    public boolean isLocalConfigUsed() {
        return true;
    }
}
