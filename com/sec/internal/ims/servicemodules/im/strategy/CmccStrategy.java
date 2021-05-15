package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.text.TextUtils;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public final class CmccStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = CmccStrategy.class.getSimpleName();
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 4;
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();

    public CmccStrategy(Context context, int phoneId) {
        super(context, phoneId);
        init();
    }

    private void init() {
        this.mRetryNeededIM_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        this.mRetryNeededIM_retryafter.add(ImError.DEDICATED_BEARER_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mRetryNeededIM_retryafter.add(ImError.NETWORK_ERROR);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_REQUEST_UNINTELLIGIBLE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_CONTENT_TYPE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_PARAMETERS_OUT_OF_BOUND);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_ERROR);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_METHOD);
        this.mRetryNeededFT_retryafter.add(ImError.NETWORK_ERROR);
        this.mRetryNeededFT_retryafter.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mRetryNeededFT_retryafter.add(ImError.DEDICATED_BEARER_ERROR);
        this.mRetryNeededFT_retryafter.add(ImError.NORMAL_RELEASE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_REQUEST_UNINTELLIGIBLE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_PARAMETERS_OUT_OF_BOUND);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_CONTENT_TYPE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_METHOD);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_ERROR);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean isFtHttp) {
        if (isSlmMessage) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode statusCode = getRetryStrategy(currentRetryCount, imError, retryAfter, newContact, chatType);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleImFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError ftError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage) {
        IMnoStrategy.StatusCode statusCode = getFtMsrpRetryStrategy(currentRetryCount, ftError, retryAfter, newContact);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleFtFailure(ftError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long capability, ChatData.ChatType chatType, boolean isBroadcastMsg) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "checkCapability->capability:" + capability + ",isBroadcastMsg:" + isBroadcastMsg);
        if (capability == ((long) Capabilities.FEATURE_FT_SERVICE) || capability == ((long) Capabilities.FEATURE_FT_HTTP)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (isBroadcastMsg || !ChatData.ChatType.isGroupChat(chatType)) {
            return getStrategyResponse();
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact, ChatData.ChatType chatType) {
        if (currentRetryCount < 1) {
            if (this.mRetryNeededErrorsForIm.contains(error)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mRetryNeededIM_retryafter.contains(error)) {
                if (retryAfter > 0 && retryAfter <= 4) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER;
                }
                if (retryAfter <= 0) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
                }
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact) {
        if (currentRetryCount >= 1) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        if (this.mRetryNeededErrorsForFt.contains(error)) {
            return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        if (this.mRetryNeededFT_retryafter.contains(error)) {
            if (retryAfter > 0 && retryAfter <= 4) {
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            }
            if (retryAfter <= 0) {
                return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public FtResumableOption getftResumableOption(CancelReason cancelreason, boolean isGroup, ImDirection direction, int transferMech) {
        if (cancelreason == null) {
            return FtResumableOption.NOTRESUMABLE;
        }
        if (isGroup) {
            return getResumableOptionGroupFt(cancelreason, direction);
        }
        return getResumableOptionSingleFt(cancelreason, direction);
    }

    private FtResumableOption getResumableOptionSingleFt(CancelReason cancelreason, ImDirection direction) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionSingleFt, cancelreason: " + cancelreason.getId() + " direction:" + direction.getId());
        FtResumableOption resumeOption = FtCancelReasonResumableOptionCodeMap.translateCancelReason(cancelreason);
        if (resumeOption == null) {
            return FtResumableOption.NOTRESUMABLE;
        }
        return resumeOption;
    }

    private FtResumableOption getResumableOptionGroupFt(CancelReason cancelreason, ImDirection direction) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionGroupFt, cancelreason: " + cancelreason.getId() + " direction:" + direction.getId());
        if (direction == ImDirection.INCOMING && (cancelreason == CancelReason.CANCELED_BY_REMOTE || cancelreason == CancelReason.ERROR)) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        if (cancelreason == CancelReason.CANCELED_BY_REMOTE) {
            return FtResumableOption.NOTRESUMABLE;
        }
        if (cancelreason == CancelReason.CANCELED_BY_USER) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        if (cancelreason == CancelReason.ERROR || cancelreason == CancelReason.DEVICE_UNREGISTERED) {
            return FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE;
        }
        return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
    }

    public boolean isSubscribeThrottled(PresenceSubscription s, long millis, boolean isAvailFetch, boolean isAlwaysForce) {
        IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled");
        return false;
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long serviceAvailabilityInfoExpiry, long capCacheExpiry, long msgCapvalidity) {
        return needRefresh(capex, refreshType, capInfoExpiry, capCacheExpiry);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (result != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public long updateFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        return features;
    }

    public boolean isCustomizedFeature(long featureCapability) {
        return ((long) Capabilities.FEATURE_GEO_VIA_SMS) == featureCapability;
    }

    public ImConstants.ChatbotMessagingTech checkChatbotMessagingTech(ImConfig config, boolean isGroupChat, Set<ImsUri> participants) {
        if (isGroupChat || participants.size() != 1 || !ChatbotUriUtil.hasChatbotUri(participants)) {
            return ImConstants.ChatbotMessagingTech.NONE;
        }
        if (config.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.DISABLED) {
            return ImConstants.ChatbotMessagingTech.NOT_AVAILABLE;
        }
        IMSLog.i(TAG, this.mPhoneId, "checkChatbotMessagingTech: force to STANDALONE_MESSAGING");
        return ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING;
    }

    public RoutingType getMsgRoutingType(ImsUri requestUri, ImsUri pAssertedId, ImsUri sender, ImsUri receiver, boolean isGroupChat) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "getMsgRoutingType->requestUri:" + requestUri + ", pAssertedId:" + pAssertedId + ", sender:" + sender + ", receiver:" + receiver + ", isGroupChat:" + isGroupChat);
        RoutingType routingType = RoutingType.NONE;
        String requestNumber = null;
        if (requestUri != null) {
            requestNumber = requestUri.getMsisdn();
        }
        if (isGroupChat) {
            String senderNumber = sender.getMsisdn();
            if (!TextUtils.isEmpty(senderNumber) && !TextUtils.isEmpty(requestNumber)) {
                routingType = senderNumber.contains(requestNumber) ? RoutingType.SENT : RoutingType.RECEIVED;
            }
        } else if (!TextUtils.isEmpty(requestNumber) && pAssertedId != null && !TextUtils.isEmpty(pAssertedId.toString())) {
            routingType = pAssertedId.toString().contains(requestNumber) ? RoutingType.SENT : RoutingType.RECEIVED;
        }
        String str2 = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "getMsgRoutingType routingType:" + routingType);
        return routingType;
    }

    public String getErrorReasonForStrategyResponse(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse) {
        if (strategyResponse == null) {
            return null;
        }
        if (strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY && strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS) {
            return null;
        }
        if (msg == null || ((msg.getType() != ImConstants.Type.TEXT && msg.getType() != ImConstants.Type.TEXT_PUBLICACCOUNT) || msg.getBody() == null)) {
            return ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString();
        }
        if (msg.getBody().getBytes(StandardCharsets.UTF_8).length > 900) {
            IMSLog.i(TAG, this.mPhoneId, "getErrorReasonForStrategyResponse(), > 900");
            return "";
        }
        IMSLog.i(TAG, this.mPhoneId, "getErrorReasonForStrategyResponse(), <= 900");
        return ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString();
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int stateId) {
        return chatType != ChatData.ChatType.REGULAR_GROUP_CHAT || stateId == ChatData.State.NONE.getId() || stateId == ChatData.State.CLOSED_BY_USER.getId();
    }

    public static class FtCancelReasonResumableOptionCodeMap {
        private static final MappingTranslator<CancelReason, FtResumableOption> mCMCCFtResumableOptionTranslator = new MappingTranslator.Builder().map(CancelReason.UNKNOWN, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CANCELED_BY_USER, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CANCELED_BY_REMOTE, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CANCELED_BY_SYSTEM, FtResumableOption.NOTRESUMABLE).map(CancelReason.REJECTED_BY_REMOTE, FtResumableOption.NOTRESUMABLE).map(CancelReason.TIME_OUT, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.ERROR, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CONNECTION_RELEASED, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.DEVICE_UNREGISTERED, FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE).map(CancelReason.NOT_AUTHORIZED, FtResumableOption.NOTRESUMABLE).map(CancelReason.FORBIDDEN_NO_RETRY_FALLBACK, FtResumableOption.NOTRESUMABLE).map(CancelReason.MSRP_SESSION_ERROR_NO_RESUME, FtResumableOption.NOTRESUMABLE).buildTranslator();

        public static FtResumableOption translateCancelReason(CancelReason value) {
            if (mCMCCFtResumableOptionTranslator.isTranslationDefined(value)) {
                return mCMCCFtResumableOptionTranslator.translate(value);
            }
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType) {
        return imConfig.getFtHttpEnabled();
    }

    public String getFtHttpUserAgent(ImConfig imConfig) {
        String userAgent = imConfig.getUserAgent();
        if (!boolSetting(RcsPolicySettings.RcsPolicy.FT_WITH_GBA)) {
            return userAgent;
        }
        return userAgent + " 3gpp-gba";
    }
}
