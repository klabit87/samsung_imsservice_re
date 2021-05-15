package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.RetryTimerUtil;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class DefaultUPMnoStrategy extends DefaultMnoStrategy {
    private static final String TAG = DefaultUPMnoStrategy.class.getSimpleName();
    protected final int MAX_RETRY_COUNT = 1;

    public DefaultUPMnoStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public boolean isFtHttpOnlySupported(boolean isGroup) {
        return true;
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean isFtHttp) {
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (isSlmMessage) {
            return handleSlmFailure(imError, currentRetryCount);
        }
        return handleImFailure(imError, chatType);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.DefaultUPMnoStrategy$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;

        static {
            int[] iArr = new int[ImError.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = iArr;
            try {
                iArr[ImError.ALTERNATE_SERVICE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.SESSION_TIMED_OUT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.BUSY_HERE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.CONNECTION_RELEASED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse handleSlmFailure(ImError error, int currentRetryCount) {
        IMnoStrategy.StatusCode statusCode = IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[error.ordinal()];
        if (!(i == 1 || i == 2 || i == 3 || i == 4 || currentRetryCount >= 1)) {
            statusCode = IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public FtResumableOption getftResumableOption(CancelReason cancelreason, boolean isGroup, ImDirection direction, int transferMech) {
        if (cancelreason == CancelReason.CANCELED_BY_USER || cancelreason == CancelReason.DEVICE_UNREGISTERED || cancelreason == CancelReason.LOW_MEMORY || cancelreason == CancelReason.ERROR || cancelreason == CancelReason.WIFI_DISCONNECTED) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        return FtResumableOption.NOTRESUMABLE;
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> uris, long capability, ChatData.ChatType chatType, boolean isBroadcastMsg) {
        if (chatType == ChatData.ChatType.REGULAR_GROUP_CHAT) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return checkCapability(uris, capability);
    }

    /* access modifiers changed from: protected */
    public void logNoCapability(ImsUri uri, Capabilities caps, long features) {
        String str;
        String str2 = TAG;
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("checkCapability: No capabilities for ");
        sb.append((!IMSLog.isShipBuild() || uri == null) ? uri : uri.toStringLimit());
        if (caps == null) {
            str = "";
        } else {
            str = ": isAvailable=" + caps.isAvailable();
        }
        sb.append(str);
        IMSLog.i(str2, i, sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.mPhoneId);
        sb2.append(",");
        sb2.append(features);
        sb2.append(",NOCAP,");
        sb2.append(uri != null ? uri.toStringLimit() : "xx");
        IMSLog.c(LogClass.STRATEGY_CHECKCAPA, sb2.toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x008d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r12, long r13) {
        /*
            r11 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r11.getCapDiscModule()
            int r1 = r11.mPhoneId
            if (r0 != 0) goto L_0x0016
            java.lang.String r2 = TAG
            int r3 = r11.mPhoneId
            java.lang.String r4 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r2, r3, r4)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r2 = r11.getStrategyResponse()
            return r2
        L_0x0016:
            r2 = 0
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r3 = r11.getImModule()
            if (r3 == 0) goto L_0x0025
            com.sec.internal.ims.servicemodules.im.ImConfig r4 = r3.getImConfig(r1)
            boolean r2 = r4.isImCapAlwaysOn()
        L_0x0025:
            java.lang.String r4 = TAG
            int r5 = r11.mPhoneId
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "checkCapability: isCapAlwaysOn = "
            r6.append(r7)
            r6.append(r2)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r4, r5, r6)
            if (r2 == 0) goto L_0x0083
            java.util.Iterator r4 = r12.iterator()
        L_0x0043:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x0082
            java.lang.Object r5 = r4.next()
            com.sec.ims.util.ImsUri r5 = (com.sec.ims.util.ImsUri) r5
            com.sec.ims.options.CapabilityRefreshType r6 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r6 = r0.getCapabilities((com.sec.ims.util.ImsUri) r5, (com.sec.ims.options.CapabilityRefreshType) r6, (int) r1)
            if (r6 == 0) goto L_0x006a
            long r7 = r6.getFeature()
            int r9 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r9 = (long) r9
            int r7 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r7 != 0) goto L_0x006a
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r7 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r4.<init>(r7)
            return r4
        L_0x006a:
            if (r6 == 0) goto L_0x007a
            boolean r7 = r6.isAvailable()
            if (r7 == 0) goto L_0x007a
            boolean r7 = r11.hasOneOfFeatures(r6, r13)
            if (r7 != 0) goto L_0x0079
            goto L_0x007a
        L_0x0079:
            goto L_0x0043
        L_0x007a:
            r11.logNoCapability(r5, r6, r13)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = r11.getStrategyResponse()
            return r4
        L_0x0082:
            goto L_0x00b1
        L_0x0083:
            java.util.Iterator r4 = r12.iterator()
        L_0x0087:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x00b1
            java.lang.Object r5 = r4.next()
            com.sec.ims.util.ImsUri r5 = (com.sec.ims.util.ImsUri) r5
            com.sec.ims.options.CapabilityRefreshType r6 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r6 = r0.getCapabilities((com.sec.ims.util.ImsUri) r5, (com.sec.ims.options.CapabilityRefreshType) r6, (int) r1)
            if (r6 == 0) goto L_0x00a9
            boolean r7 = r6.isAvailable()
            if (r7 == 0) goto L_0x00a9
            boolean r7 = r11.hasOneOfFeaturesAvailable(r6, r13)
            if (r7 != 0) goto L_0x00a8
            goto L_0x00a9
        L_0x00a8:
            goto L_0x0087
        L_0x00a9:
            r11.logNoCapability(r5, r6, r13)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = r11.getStrategyResponse()
            return r4
        L_0x00b1:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r5 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r4.<init>(r5)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.DefaultUPMnoStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex == null || result == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (result == CapabilityConstants.CapExResult.USER_UNAVAILABLE && !capex.hasFeature(Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: User is offline");
            return false;
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
        if (capex == null || capex.hasFeature(Capabilities.FEATURE_NOT_UPDATED) || capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER) || features == ((long) Capabilities.FEATURE_NON_RCS_USER)) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "updateFeatures: set features " + Capabilities.dumpFeature(features));
            return features;
        } else if (features == ((long) Capabilities.FEATURE_NOT_UPDATED)) {
            String str2 = TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "updateFeatures: feature is NOT_UPDATED, remains previous features " + Capabilities.dumpFeature(capex.getFeature()));
            return capex.getFeature();
        } else {
            String str3 = TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str3, i3, "updateFeatures: updated features " + Capabilities.dumpFeature(capex.getFeature() | features));
            return capex.getFeature() | features;
        }
    }

    public long updateAvailableFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        return features;
    }

    public boolean isRevocationAvailableMessage(MessageBase message) {
        return true;
    }

    public boolean isCustomizedFeature(long featureCapability) {
        return ((long) Capabilities.FEATURE_GEO_VIA_SMS) == featureCapability || ((long) Capabilities.FEATURE_FT_VIA_SMS) == featureCapability;
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
        } else if ((refreshType == CapabilityRefreshType.FORCE_REFRESH_UCE && capex.isExpired(serviceAvailabilityInfoExpiry)) || refreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            return true;
        } else {
            if (refreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capex.isExpired(capInfoExpiry)) {
                return true;
            }
            if (refreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX) {
                return false;
            }
            if (capex.isExpired(capex.getLegacyLatching() ? serviceAvailabilityInfoExpiry : capInfoExpiry)) {
                return true;
            }
            return false;
        }
    }

    public final boolean isResendFTResume(boolean isGroupChat) {
        return false;
    }

    public final boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return true;
    }

    public boolean isDisplayWarnText() {
        return true;
    }

    public final boolean isNeedToReportToRegiGvn(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public final IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError errorReason) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }

    public final IMnoStrategy.StrategyResponse handleSlmFailure(ImError error) {
        if (error == ImError.REMOTE_PARTY_DECLINED) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public final IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason close) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public final boolean isDeleteSessionSupported(ChatData.ChatType chatType, int stateId) {
        return true;
    }

    public final boolean isFirstMsgInvite(boolean isFirstMsgInvite) {
        return isFirstMsgInvite;
    }

    public boolean dropUnsupportedCharacter(String alias) {
        return false;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return false;
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return false;
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT, ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError ftError, ChatData.ChatType chatType) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.HttpStrategyResponse handleFtHttpDownloadError(HttpRequest httpReq) {
        int delay;
        CancelReason reason = CancelReason.ERROR;
        int code = httpReq.code();
        if (code == 403) {
            delay = 3;
            handleFtHttpRequestFailure(CancelReason.FORBIDDEN_FT_HTTP, ImDirection.INCOMING, false);
        } else if (code == 404 || code == 410) {
            reason = CancelReason.VALIDITY_EXPIRED;
            delay = -1;
        } else if (code != 503) {
            delay = 3;
        } else {
            delay = RetryTimerUtil.getRetryAfter(httpReq.header(HttpRequest.HEADER_RETRY_AFTER));
        }
        return new IMnoStrategy.HttpStrategyResponse(reason, delay);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.DOES_NOT_EXIST_ANYWHERE, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
    }

    public long getThrottledDelay(long delay) {
        return delay;
    }
}
