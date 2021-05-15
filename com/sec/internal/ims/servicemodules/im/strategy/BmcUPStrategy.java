package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Set;

public final class BmcUPStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = BmcUPStrategy.class.getSimpleName();
    protected final int MAX_RETRY_COUNT = 1;

    public BmcUPStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public boolean isFtHttpOnlySupported(boolean isGroup) {
        return true;
    }

    public void forceRefreshCapability(Set<ImsUri> uris, boolean remoteOnline, ImError error) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        if (capDiscModule == null) {
            IMSLog.i(TAG, this.mPhoneId, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceRefreshCapability: uris " + IMSLog.checker(uris));
        if (remoteOnline) {
            for (ImsUri uri : uris) {
                capDiscModule.getCapabilities(uri.getMsisdn(), (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_CPM), this.mPhoneId);
            }
        }
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
        if (ChatbotUriUtil.hasUriBotPlatform(uri)) {
            return true;
        }
        return StrategyUtils.isCapabilityValidUriForUS(uri, this.mPhoneId);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (result == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability USER_NOT_FOUND");
            return true;
        } else if (result == CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: UNCLASSIFIED_ERROR. do not change anything");
            return false;
        } else if (capex.getFeature() == ((long) Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is not_updated");
            return true;
        } else if (result != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long serviceAvailabilityInfoExpiry, long capCacheExpiry, long msgCapvalidity) {
        return needRefresh(capex, refreshType, capInfoExpiry, capCacheExpiry);
    }

    public boolean isPresenceReadyToRequest(boolean ownInfoPublished, boolean paralysed) {
        return ownInfoPublished && !paralysed;
    }

    public boolean isSubscribeThrottled(PresenceSubscription s, long millis, boolean isAvailFetch, boolean isAlwaysForce) {
        if (s.getState() == 5) {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: retried subscription");
            return false;
        } else if (isAlwaysForce) {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: isAlwaysForce true");
            return false;
        } else {
            Date current = new Date();
            long offset = current.getTime() - s.getTimestamp().getTime();
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "isSubscribeThrottled: interval from " + s.getTimestamp().getTime() + " to " + current.getTime() + ", offset " + offset + " sourceThrottlePublish " + millis);
            if (offset < millis) {
                return true;
            }
            return false;
        }
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

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.BmcUPStrategy$1  reason: invalid class name */
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

    private IMnoStrategy.StrategyResponse handleSlmFailure(ImError error, int currentRetryCount) {
        IMnoStrategy.StatusCode statusCode = IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[error.ordinal()];
        if (!(i == 1 || i == 2 || i == 3 || i == 4 || currentRetryCount >= 1)) {
            statusCode = IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> uris, long capability, ChatData.ChatType chatType, boolean isBroadcastMsg) {
        if (chatType == ChatData.ChatType.REGULAR_GROUP_CHAT) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return checkCapability(uris, capability);
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x001e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r9, long r10) {
        /*
            r8 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r8.getCapDiscModule()
            if (r0 != 0) goto L_0x0014
            java.lang.String r1 = TAG
            int r2 = r8.mPhoneId
            java.lang.String r3 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r1, r2, r3)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r1 = r8.getStrategyResponse()
            return r1
        L_0x0014:
            java.util.Iterator r1 = r9.iterator()
        L_0x0018:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x00b1
            java.lang.Object r2 = r1.next()
            com.sec.ims.util.ImsUri r2 = (com.sec.ims.util.ImsUri) r2
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            int r4 = r8.mPhoneId
            com.sec.ims.options.Capabilities r3 = r0.getCapabilities((com.sec.ims.util.ImsUri) r2, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r4)
            if (r3 == 0) goto L_0x003c
            boolean r4 = r3.isAvailable()
            if (r4 == 0) goto L_0x003c
            boolean r4 = r8.hasOneOfFeaturesAvailable(r3, r10)
            if (r4 != 0) goto L_0x003b
            goto L_0x003c
        L_0x003b:
            goto L_0x0018
        L_0x003c:
            java.lang.String r1 = TAG
            int r4 = r8.mPhoneId
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "checkCapability: No capabilities for "
            r5.append(r6)
            boolean r6 = com.sec.internal.log.IMSLog.isShipBuild()
            if (r6 == 0) goto L_0x0057
            if (r2 == 0) goto L_0x0057
            java.lang.String r6 = r2.toStringLimit()
            goto L_0x0058
        L_0x0057:
            r6 = r2
        L_0x0058:
            r5.append(r6)
            if (r3 != 0) goto L_0x0060
            java.lang.String r6 = ""
            goto L_0x0075
        L_0x0060:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = ": isAvailable="
            r6.append(r7)
            boolean r7 = r3.isAvailable()
            r6.append(r7)
            java.lang.String r6 = r6.toString()
        L_0x0075:
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r1, r4, r5)
            r1 = 302710784(0x120b0000, float:4.3860666E-28)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            int r5 = r8.mPhoneId
            r4.append(r5)
            java.lang.String r5 = ","
            r4.append(r5)
            r4.append(r10)
            java.lang.String r5 = ",NOCAP,"
            r4.append(r5)
            if (r2 == 0) goto L_0x009f
            java.lang.String r5 = r2.toStringLimit()
            goto L_0x00a2
        L_0x009f:
            java.lang.String r5 = "xx"
        L_0x00a2:
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.c(r1, r4)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r1 = r8.getStrategyResponse()
            return r1
        L_0x00b1:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r1 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r2 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r1.<init>(r2)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.BmcUPStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean isRevocationAvailableMessage(MessageBase message) {
        return true;
    }

    public boolean isCustomizedFeature(long featureCapability) {
        return false;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (imError.isOneOf(ImError.MSRP_ACTION_NOT_ALLOWED, ImError.REMOTE_PARTY_DECLINED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
    }

    public boolean isFirstMsgInvite(boolean isFirstMsgInvite) {
        return false;
    }

    public long getThrottledDelay(long delay) {
        return 3;
    }
}
