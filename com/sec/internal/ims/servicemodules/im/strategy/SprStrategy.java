package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SprStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = SprStrategy.class.getSimpleName();
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 4;
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();

    public SprStrategy(Context context, int phoneId) {
        super(context, phoneId);
        init();
    }

    private void init() {
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_REQUEST_UNINTELLIGIBLE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_CONTENT_TYPE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_PARAMETERS_OUT_OF_BOUND);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_METHOD);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.NETWORK_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        this.mRetryNeededIM_retryafter.add(ImError.DEDICATED_BEARER_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_REQUEST_UNINTELLIGIBLE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_CONTENT_TYPE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_PARAMETERS_OUT_OF_BOUND);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_METHOD);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_ERROR);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
        this.mRetryNeededFT_retryafter.add(ImError.NETWORK_ERROR);
        this.mRetryNeededFT_retryafter.add(ImError.DEDICATED_BEARER_ERROR);
        this.mRetryNeededFT_retryafter.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
    }

    public void forceRefreshCapability(Set<ImsUri> uris, boolean remoteOnline, ImError error) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        int phoneId = this.mPhoneId;
        if (capDiscModule == null) {
            IMSLog.i(TAG, this.mPhoneId, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceRefreshCapability: uris " + IMSLog.numberChecker((Collection<ImsUri>) uris));
        if (remoteOnline) {
            capDiscModule.getCapabilities(new ArrayList<>(uris), CapabilityRefreshType.ONLY_IF_NOT_FRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), phoneId);
        } else if (error != null && this.mForceRefreshRemoteCapa.contains(error)) {
            capDiscModule.getCapabilities(new ArrayList<>(uris), CapabilityRefreshType.ALWAYS_FORCE_REFRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), phoneId);
        }
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact, ChatData.ChatType chatType) {
        if (currentRetryCount < 1) {
            if (this.mRetryNeededErrorsForIm.contains(error)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mRetryNeededIM_retryafter.contains(error)) {
                if (retryAfter <= 0) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
                }
                if (retryAfter <= 4) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER;
                }
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
        return StrategyUtils.isCapabilityValidUriForUS(uri, this.mPhoneId);
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact) {
        if (currentRetryCount >= 1) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        if (this.mRetryNeededErrorsForFt.contains(error)) {
            return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        if (this.mRetryNeededFT_retryafter.contains(error)) {
            if (retryAfter <= 0) {
                return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
            }
            if (retryAfter <= 4) {
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean isFtHttp) {
        if (isSlmMessage) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode statusCode = getRetryStrategy(currentRetryCount, imError, retryAfter, newContact, chatType);
        if (statusCode != IMnoStrategy.StatusCode.NO_RETRY) {
            return new IMnoStrategy.StrategyResponse(statusCode);
        }
        if (ChatData.ChatType.isGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return handleImFailure(imError, chatType);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError ftError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage) {
        IMnoStrategy.StatusCode statusCode = getFtMsrpRetryStrategy(currentRetryCount, ftError, retryAfter, newContact);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return ChatData.ChatType.isGroupChat(chatType) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR) : handleFtFailure(ftError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public boolean isFtHttpOnlySupported(boolean isGroup) {
        return true;
    }

    public boolean isRevocationAvailableMessage(MessageBase message) {
        return message instanceof ImMessage;
    }

    public FtResumableOption getftResumableOption(CancelReason cancelreason, boolean isGroup, ImDirection direction, int transferMech) {
        if (cancelreason == CancelReason.CANCELED_BY_USER || cancelreason == CancelReason.DEVICE_UNREGISTERED || cancelreason == CancelReason.LOW_MEMORY) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        return FtResumableOption.NOTRESUMABLE;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError ftError, ChatData.ChatType chatType) {
        if (ftError.isOneOf(ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.INVALID_REQUEST, ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.UNSUPPORTED_MEDIA_TYPE, ImError.REMOTE_TEMPORARILY_UNAVAILABLE, ImError.INTERNAL_SERVER_ERROR, ImError.SERVICE_UNAVAILABLE, ImError.NO_DNS_RESULTS)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (imError.isOneOf(ImError.BUSY_HERE)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.SUCCESS);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }
}
