package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import java.util.HashSet;

public final class TceStrategy extends DefaultRCSMnoStrategy {
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 4;
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();

    public TceStrategy(Context context, int phoneId) {
        super(context, phoneId);
        init();
    }

    private void init() {
        this.mRetryNeededFT_retryafter.add(ImError.NETWORK_ERROR);
        this.mRetryNeededFT_retryafter.add(ImError.DEDICATED_BEARER_ERROR);
        this.mRetryNeededFT_retryafter.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_REQUEST_UNINTELLIGIBLE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_CONTENT_TYPE);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_METHOD);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_PARAMETERS_OUT_OF_BOUND);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
        this.mRetryNeededErrorsForFt.add(ImError.MSRP_UNKNOWN_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        this.mRetryNeededIM_retryafter.add(ImError.NETWORK_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.DEDICATED_BEARER_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_REQUEST_UNINTELLIGIBLE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_PARAMETERS_OUT_OF_BOUND);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_CONTENT_TYPE);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_METHOD);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_UNKNOWN_ERROR);
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
        if (currentRetryCount < 1) {
            if (this.mRetryNeededErrorsForFt.contains(error)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mRetryNeededFT_retryafter.contains(error)) {
                if (retryAfter <= 0) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
                }
                if (retryAfter > 0 && retryAfter <= 4) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER;
                }
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean isFtHttp) {
        if (isSlmMessage) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode statusCode = getRetryStrategy(currentRetryCount, imError, retryAfter, newContact, chatType);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return ChatData.ChatType.isGroupChat(chatType) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR) : handleImFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError ftError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage) {
        IMnoStrategy.StatusCode statusCode = getFtMsrpRetryStrategy(currentRetryCount, ftError, retryAfter, newContact);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return ChatData.ChatType.isGroupChat(chatType) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR) : handleFtFailure(ftError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
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

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError ftError, ChatData.ChatType chatType) {
        if (ftError.isOneOf(ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }
}
