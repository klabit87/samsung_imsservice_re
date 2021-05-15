package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Uri;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SecUPStrategy extends DefaultUPMnoStrategy {
    private static final String TAG = SecUPStrategy.class.getSimpleName();
    protected final int MAX_RETRY_COUNT = 1;
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();

    public SecUPStrategy(Context context, int phoneId) {
        super(context, phoneId);
        init();
    }

    private void init() {
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse getStrategyResponse() {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean hasChatbotUri, boolean isFtHttp) {
        ImError imError2 = imError;
        IMnoStrategy.StrategyResponse strategyResponse = handleSendingMessageFailure(imError, currentRetryCount, retryAfter, newContact, chatType, isSlmMessage, isFtHttp);
        if (!hasChatbotUri) {
            int i = currentRetryCount;
        } else if (strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM && strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY && imError2 != ImError.GONE && imError2 != ImError.REQUEST_PENDING) {
            int i2 = currentRetryCount;
        } else if (currentRetryCount < 1) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.RETRY_AFTER);
        } else {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY);
        }
        return strategyResponse;
    }

    public void forceRefreshCapability(Set<ImsUri> uris, boolean remoteOnline, ImError error) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        if (capDiscModule == null) {
            IMSLog.i(TAG, this.mPhoneId, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceRefreshCapability: uris " + IMSLog.numberChecker((Collection<ImsUri>) uris));
        ArrayList arrayList = new ArrayList(uris);
        if (remoteOnline) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ONLY_IF_NOT_FRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), this.mPhoneId);
        } else if (error != null && this.mForceRefreshRemoteCapa.contains(error)) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ALWAYS_FORCE_REFRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), this.mPhoneId);
        }
    }

    public Uri getFtHttpCsUri(ImConfig imConfig, Set<ImsUri> participants, boolean isExtraFt, boolean isGroupChat) {
        if (imConfig.getCbftHTTPCSURI() != null && !isGroupChat && ChatbotUriUtil.hasChatbotUri(participants)) {
            return imConfig.getCbftHTTPCSURI();
        }
        if (imConfig.getFtHTTPExtraCSURI() == null || !isExtraFt) {
            return imConfig.getFtHttpCsUri();
        }
        return imConfig.getFtHTTPExtraCSURI();
    }

    public ImConstants.ChatbotMessagingTech checkChatbotMessagingTech(ImConfig config, boolean isGroupChat, Set<ImsUri> participants) {
        Capabilities cap;
        if (!isGroupChat && participants.size() == 1 && ChatbotUriUtil.hasChatbotUri(participants)) {
            ImConstants.ChatbotMsgTechConfig configTech = config.getChatbotMsgTech();
            if (configTech == ImConstants.ChatbotMsgTechConfig.DISABLED) {
                return ImConstants.ChatbotMessagingTech.NOT_AVAILABLE;
            }
            ICapabilityDiscoveryModule capModule = getCapDiscModule();
            if (!(capModule == null || (cap = capModule.getCapabilities(participants.iterator().next(), CapabilityRefreshType.ONLY_IF_NOT_FRESH, config.getPhoneId())) == null)) {
                if (cap.hasFeature(Capabilities.FEATURE_CHATBOT_CHAT_SESSION) && (configTech == ImConstants.ChatbotMsgTechConfig.SESSION_ONLY || configTech == ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM)) {
                    return ImConstants.ChatbotMessagingTech.SESSION;
                }
                if (!cap.hasFeature(Capabilities.FEATURE_CHATBOT_STANDALONE_MSG) || (configTech != ImConstants.ChatbotMsgTechConfig.SLM_ONLY && configTech != ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM)) {
                    return ImConstants.ChatbotMessagingTech.NOT_AVAILABLE;
                }
                return ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING;
            }
        }
        return ImConstants.ChatbotMessagingTech.NONE;
    }

    public boolean dropUnsupportedCharacter(String alias) {
        if (alias == null || !alias.contains("?")) {
            return false;
        }
        return true;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.ADDRESS_INCOMPLETE);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT, ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (isPublish) {
            if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED, PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.BAD_EVENT)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
            }
            if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT, PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF;
            }
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.DOES_NOT_EXIST_ANYWHERE, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        }
        return PresenceResponse.PresenceStatusCode.NONE;
    }
}
