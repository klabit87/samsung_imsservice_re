package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Network;
import android.net.Uri;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.UriGenerator;
import java.util.List;
import java.util.Set;

public interface IMnoStrategy {

    public enum ErrorNotificationId {
        NONE,
        EXCEED_MAXIMUM_RECIPIENTS
    }

    public enum StatusCode {
        DISPLAY_ERROR,
        DISPLAY_ERROR_CFS,
        FALLBACK_TO_LEGACY,
        FALLBACK_TO_LEGACY_CFS,
        FALLBACK_TO_SLM,
        FALLBACK_TO_SLM_FILE,
        NONE,
        NO_RETRY,
        RETRY_IMMEDIATE,
        RETRY_WITH_NEW_CONTACT_HEADER,
        RETRY_AFTER,
        RETRY_AFTER_SESSION,
        RETRY_AFTER_REGI,
        SUCCESS
    }

    boolean boolSetting(String str);

    long calSubscribeDelayTime(PresenceSubscription presenceSubscription);

    long calThrottledPublishRetryDelayTime(long j, long j2);

    void changeServiceDescription();

    boolean checkCapDiscoveryOption();

    StrategyResponse checkCapability(Set<ImsUri> set, long j);

    StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z);

    ImConstants.ChatbotMessagingTech checkChatbotMessagingTech(ImConfig imConfig, boolean z, Set<ImsUri> set);

    boolean checkImsiBasedRegi(ImsRegistration imsRegistration);

    boolean checkMainSwitchOff(Context context, int i);

    String checkNeedParsing(String str);

    boolean checkSlmFileType(String str);

    ImDirection convertToImDirection(String str);

    boolean dropUnsupportedCharacter(String str);

    void forceRefreshCapability(Set<ImsUri> set, boolean z, ImError imError);

    String getErrorReasonForStrategyResponse(MessageBase messageBase, StrategyResponse strategyResponse);

    Uri getFtHttpCsUri(ImConfig imConfig, Set<ImsUri> set, boolean z, boolean z2);

    int getFtHttpRetryInterval(int i, int i2);

    int getFtHttpSessionRetryTimer(int i, ImError imError);

    String getFtHttpUserAgent(ImConfig imConfig);

    StatusCode getFtMsrpRetryStrategy(int i, ImError imError, int i2, ImsUri imsUri);

    RoutingType getMsgRoutingType(ImsUri imsUri, ImsUri imsUri2, ImsUri imsUri3, ImsUri imsUri4, boolean z);

    Set<ImsUri> getNetworkPreferredUri(UriGenerator uriGenerator, Set<ImsUri> set);

    int getNextFileTransferAutoResumeTimer(ImDirection imDirection, int i);

    RcsPolicySettings.RcsPolicyType getPolicyType();

    StatusCode getRetryStrategy(int i, ImError imError, int i2, ImsUri imsUri, ChatData.ChatType chatType);

    ImSessionStopReason getSessionStopReason(boolean z);

    long getThrottledDelay(long j);

    StrategyResponse getUploadedFileFallbackSLMTech();

    FtResumableOption getftResumableOption(CancelReason cancelReason, boolean z, ImDirection imDirection, int i);

    StrategyResponse handleAttachFileFailure(ImSessionClosedReason imSessionClosedReason);

    StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType);

    HttpStrategyResponse handleFtHttpDownloadError(HttpRequest httpRequest);

    StrategyResponse handleFtHttpRequestFailure(CancelReason cancelReason, ImDirection imDirection, boolean z);

    StrategyResponse handleFtMsrpInterruption(ImError imError);

    StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType);

    PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z);

    StrategyResponse handleSendingFtMsrpMessageFailure(ImError imError, int i, int i2, ImsUri imsUri, ChatData.ChatType chatType, boolean z);

    StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ImsUri imsUri, ChatData.ChatType chatType, boolean z, boolean z2);

    StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ImsUri imsUri, ChatData.ChatType chatType, boolean z, boolean z2, boolean z3);

    ImSessionClosedReason handleSessionFailure(ImError imError);

    StrategyResponse handleSlmFailure(ImError imError);

    int intSetting(String str);

    boolean isBMode(boolean z);

    boolean isCapabilityValidUri(ImsUri imsUri);

    boolean isCloseSessionNeeded(ImError imError);

    boolean isCustomizedFeature(long j);

    boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i);

    boolean isDisplayBotError();

    boolean isDisplayWarnText();

    boolean isFTHTTPAutoResumeAndCancelPerConnectionChange();

    boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType);

    boolean isFirstMsgInvite(boolean z);

    boolean isFtHttpOnlySupported(boolean z);

    boolean isHTTPUsedForEmptyFtHttpPOST();

    boolean isLocalConfigUsed();

    boolean isNeedToReportToRegiGvn(ImError imError);

    boolean isPresenceReadyToRequest(boolean z, boolean z2);

    boolean isRemoteConfigNeeded(int i);

    boolean isResendFTResume(boolean z);

    boolean isRevocationAvailableMessage(MessageBase messageBase);

    boolean isSubscribeThrottled(PresenceSubscription presenceSubscription, long j, boolean z, boolean z2);

    long isTdelay(long j);

    boolean isWarnSizeFile(Network network, long j, long j2, boolean z);

    boolean loadRcsSettings(boolean z);

    boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2);

    boolean needPoll(Capabilities capabilities, long j);

    boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2);

    boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4);

    boolean needStopAutoRejoin(ImError imError);

    boolean needToCapabilityCheckForImdn(boolean z);

    boolean needUnpublish(int i);

    boolean needUnpublish(ImsRegistration imsRegistration, ImsRegistration imsRegistration2);

    void setPolicyType(RcsPolicySettings.RcsPolicyType rcsPolicyType);

    boolean shouldRestartSession(ImError imError);

    void startServiceBasedOnOmaDmNodes(int i);

    List<String> stringArraySetting(String str);

    String stringSetting(String str);

    long updateAvailableFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult);

    void updateCapDiscoveryOption();

    long updateFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult);

    void updateLocalConfigUsedState(boolean z);

    void updateOmaDmNodes(int i);

    public static class StrategyResponse {
        private final ErrorNotificationId mErrorNotificationId;
        private final StatusCode mStatusCode;

        public StrategyResponse(StatusCode statusCode, ErrorNotificationId errorNotificationId) {
            this.mStatusCode = statusCode;
            this.mErrorNotificationId = errorNotificationId;
        }

        public StrategyResponse(StatusCode statusCode) {
            this.mStatusCode = statusCode;
            this.mErrorNotificationId = ErrorNotificationId.NONE;
        }

        public StatusCode getStatusCode() {
            return this.mStatusCode;
        }

        public ErrorNotificationId getErrorNotificationId() {
            return this.mErrorNotificationId;
        }
    }

    public static class HttpStrategyResponse {
        private final CancelReason mCancelReason;
        private final int mDelay;

        public HttpStrategyResponse(CancelReason cancelReason, int delay) {
            this.mCancelReason = cancelReason;
            this.mDelay = delay;
        }

        public CancelReason getCancelReason() {
            return this.mCancelReason;
        }

        public int getDelay() {
            return this.mDelay;
        }
    }
}
