package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class DefaultRCSMnoStrategy extends DefaultMnoStrategy {
    private static final String TAG = DefaultRCSMnoStrategy.class.getSimpleName();

    public DefaultRCSMnoStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean isFtHttp) {
        if (isSlmMessage && !ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode statusCode = getRetryStrategy(currentRetryCount, imError, retryAfter, newContact, chatType);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleImFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> uris, long capability, ChatData.ChatType chatType, boolean isBroadcastMsg) {
        return checkCapability(uris, capability);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long capability) {
        IMSLog.i(TAG, this.mPhoneId, "checkCapability");
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long serviceAvailabilityInfoExpiry, long capCacheExpiry, long msgCapvalidity) {
        if (refreshType == CapabilityRefreshType.DISABLED) {
            return false;
        }
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability is null");
            return true;
        } else if (capex.hasFeature(Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: fetch failed capabilities");
            return true;
        } else if (refreshType == CapabilityRefreshType.FORCE_REFRESH_UCE || refreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            return true;
        } else {
            if (refreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capex.isExpired(capInfoExpiry)) {
                return true;
            }
            if (refreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX || !capex.isExpired(msgCapvalidity)) {
                return false;
            }
            return true;
        }
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (result == CapabilityConstants.CapExResult.USER_UNAVAILABLE) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: User is offline");
            return false;
        } else if (capex == null || result == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (result == CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR || result == CapabilityConstants.CapExResult.FORBIDDEN_403) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: do not change anything");
            return false;
        } else if (result != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public long updateFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        if (result != CapabilityConstants.CapExResult.USER_UNAVAILABLE || capex == null) {
            return features;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateFeatures: keep old caps " + capex);
        return capex.getFeature();
    }

    public long updateAvailableFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        return features;
    }

    public boolean isCustomizedFeature(long featureCapability) {
        return false;
    }

    public boolean isRevocationAvailableMessage(MessageBase message) {
        return false;
    }

    public final boolean dropUnsupportedCharacter(String alias) {
        return false;
    }

    public final IMnoStrategy.StrategyResponse handleSlmFailure(ImError error) {
        if (error == ImError.REMOTE_PARTY_DECLINED) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public boolean isResendFTResume(boolean isGroupChat) {
        return false;
    }

    public boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return true;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return false;
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return false;
    }

    public boolean isNeedToReportToRegiGvn(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError ftError, ChatData.ChatType chatType) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        return PresenceResponse.PresenceStatusCode.NONE;
    }

    public IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError errorReason) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }

    public IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason close) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int stateId) {
        return true;
    }

    public boolean isFirstMsgInvite(boolean isFirstMsgInvite) {
        return isFirstMsgInvite;
    }

    public long getThrottledDelay(long delay) {
        return delay;
    }
}
