package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public final class AttStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = AttStrategy.class.getSimpleName();
    private final int CONSECUTIVE_SUBSCRIBE_THRESHOLD = 10;
    private final int LIMITED_SUBSCRIBE_INTERVAL = 1000;
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 5;
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();
    private final int[] mFtHttpMOSessionRetryTimerList = {0, 10, 20};
    private final int[] mFtResumeRetryMOTimerList = {1};
    private final int[] mFtResumeRetryMTTimerList = {1, 600, 3600, 86400, 172800, 259200};
    private final long[] mReconfigurationTimerList = {0, 14400000, 28800000, 57600000, 115200000};
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_changecontact = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_changecontact = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();
    private final ArrayBlockingQueue<PresenceSubscription> mSubscriptionQueue = new ArrayBlockingQueue<>(10, true);

    public AttStrategy(Context context, int phoneId) {
        super(context, phoneId);
        init();
    }

    private void init() {
        String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "");
        this.mRetryNeededErrorsForIm.add(ImError.GONE);
        this.mRetryNeededErrorsForIm.add(ImError.UNSUPPORTED_URI_SCHEME);
        this.mRetryNeededErrorsForIm.add(ImError.NETWORK_ERROR);
        this.mRetryNeededErrorsForIm.add(ImError.BAD_GATEWAY);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mRetryNeededErrorsForIm.add(ImError.REQUEST_PENDING);
        this.mRetryNeededErrorsForIm.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mRetryNeededErrorsForIm.add(ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK);
        this.mRetryNeededErrorsForIm.add(ImError.LOOP_DETECTED);
        this.mRetryNeededErrorsForIm.add(ImError.TOO_MANY_HOPS);
        this.mRetryNeededErrorsForFt.add(ImError.FORBIDDEN_RETRY_FALLBACK);
        this.mRetryNeededErrorsForFt.add(ImError.UNSUPPORTED_URI_SCHEME);
        this.mRetryNeededErrorsForFt.add(ImError.BAD_GATEWAY);
        this.mRetryNeededErrorsForFt.add(ImError.REQUEST_PENDING);
        this.mRetryNeededErrorsForFt.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mRetryNeededErrorsForFt.add(ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK);
        this.mRetryNeededErrorsForFt.add(ImError.GONE);
        this.mRetryNeededErrorsForFt.add(ImError.LOOP_DETECTED);
        this.mRetryNeededErrorsForFt.add(ImError.TOO_MANY_HOPS);
        this.mRetryNeededFT_retryafter.add(ImError.INTERNAL_SERVER_ERROR);
        this.mRetryNeededFT_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        this.mRetryNeededFT_retryafter.add(ImError.BUSY_EVERYWHERE);
        this.mRetryNeededIM_retryafter.add(ImError.INTERNAL_SERVER_ERROR);
        this.mRetryNeededIM_retryafter.add(ImError.BUSY_EVERYWHERE);
        this.mRetryNeededFT_changecontact.add(ImError.MULTIPLE_CHOICES);
        this.mRetryNeededFT_changecontact.add(ImError.MOVED_PERMANENTLY);
        this.mRetryNeededFT_changecontact.add(ImError.MOVED_TEMPORARILY);
        this.mRetryNeededFT_changecontact.add(ImError.USE_PROXY);
        this.mRetryNeededIM_changecontact.add(ImError.MULTIPLE_CHOICES);
        this.mRetryNeededIM_changecontact.add(ImError.MOVED_PERMANENTLY);
        this.mRetryNeededIM_changecontact.add(ImError.MOVED_TEMPORARILY);
        this.mRetryNeededIM_changecontact.add(ImError.USE_PROXY);
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
        this.mForceRefreshRemoteCapa.add(ImError.GONE);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean hasChatbotUri, boolean isFtHttp) {
        IMnoStrategy.StrategyResponse strategyResponse;
        if (isSlmMessage) {
            strategyResponse = handleSlmFailure(imError);
        } else if (!isFtHttp || hasChatbotUri) {
            IMnoStrategy.StatusCode statusCode = getRetryStrategy(currentRetryCount, imError, retryAfter, newContact, chatType);
            if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
                strategyResponse = handleImFailure(imError, chatType);
            } else {
                strategyResponse = new IMnoStrategy.StrategyResponse(statusCode);
            }
        } else {
            strategyResponse = handleSendingFtHttpMessageFailure(imError, currentRetryCount);
        }
        if (!hasChatbotUri || (strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM && strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY && imError != ImError.GONE && imError != ImError.REQUEST_PENDING)) {
            return strategyResponse;
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY);
    }

    private IMnoStrategy.StrategyResponse handleSendingFtHttpMessageFailure(ImError imError, int currentRetryCount) {
        int ftSessionRetryTimer = getFtHttpSessionRetryTimer(currentRetryCount, imError);
        if (ftSessionRetryTimer == -1) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (ftSessionRetryTimer == 0) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.RETRY_IMMEDIATE);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.RETRY_AFTER);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError ftError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage) {
        IMnoStrategy.StatusCode statusCode = getFtMsrpRetryStrategy(currentRetryCount, ftError, retryAfter, newContact);
        if (statusCode == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleFtFailure(ftError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> uris, long capability, ChatData.ChatType chatType, boolean isBroadcastMsg) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "checkCapability->capability:" + capability + ", isBroadcastMsg:" + isBroadcastMsg);
        if (ChatData.ChatType.isGroupChat(chatType) && isBroadcastMsg) {
            return getStrategyResponse();
        }
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return checkCapability(uris, capability);
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0030  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r12, long r13) {
        /*
            r11 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r6 = r11.getCapDiscModule()
            int r7 = r11.mPhoneId
            if (r6 != 0) goto L_0x0016
            java.lang.String r0 = TAG
            int r1 = r11.mPhoneId
            java.lang.String r2 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r0 = r11.getStrategyResponse()
            return r0
        L_0x0016:
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>(r12)
            com.sec.ims.options.CapabilityRefreshType r2 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            int r0 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r3 = (long) r0
            r0 = r6
            r5 = r7
            com.sec.ims.options.Capabilities[] r0 = r0.getCapabilities(r1, r2, r3, r5)
            java.util.Iterator r2 = r12.iterator()
        L_0x002a:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x00bf
            java.lang.Object r3 = r2.next()
            com.sec.ims.util.ImsUri r3 = (com.sec.ims.util.ImsUri) r3
            com.sec.ims.options.Capabilities r4 = r11.findMatchingCapabilities(r3, r0)
            if (r4 == 0) goto L_0x004a
            boolean r5 = r4.isAvailable()
            if (r5 == 0) goto L_0x004a
            boolean r5 = r11.hasOneOfFeaturesAvailable(r4, r13)
            if (r5 != 0) goto L_0x0049
            goto L_0x004a
        L_0x0049:
            goto L_0x002a
        L_0x004a:
            java.lang.String r2 = TAG
            int r5 = r11.mPhoneId
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "checkCapability: No capabilities for "
            r8.append(r9)
            boolean r9 = com.sec.internal.log.IMSLog.isShipBuild()
            if (r9 == 0) goto L_0x0065
            if (r3 == 0) goto L_0x0065
            java.lang.String r9 = r3.toStringLimit()
            goto L_0x0066
        L_0x0065:
            r9 = r3
        L_0x0066:
            r8.append(r9)
            if (r4 != 0) goto L_0x006e
            java.lang.String r9 = ""
            goto L_0x0083
        L_0x006e:
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = ": isAvailable="
            r9.append(r10)
            boolean r10 = r4.isAvailable()
            r9.append(r10)
            java.lang.String r9 = r9.toString()
        L_0x0083:
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.i(r2, r5, r8)
            r2 = 302710784(0x120b0000, float:4.3860666E-28)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            int r8 = r11.mPhoneId
            r5.append(r8)
            java.lang.String r8 = ","
            r5.append(r8)
            r5.append(r13)
            java.lang.String r8 = ",NOCAP,"
            r5.append(r8)
            if (r3 == 0) goto L_0x00ad
            java.lang.String r8 = r3.toStringLimit()
            goto L_0x00b0
        L_0x00ad:
            java.lang.String r8 = "xx"
        L_0x00b0:
            r5.append(r8)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.c(r2, r5)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r2 = r11.getStrategyResponse()
            return r2
        L_0x00bf:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r2 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r3 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r2.<init>(r3)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.AttStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    private Capabilities findMatchingCapabilities(ImsUri uri, Capabilities[] capexList) {
        if (capexList == null) {
            IMSLog.e(TAG, this.mPhoneId, "findMatchingCapabilities: capexList is null");
            return null;
        }
        for (Capabilities capex : capexList) {
            if (capex.getUri().equals(uri)) {
                return capex;
            }
        }
        return null;
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact, ChatData.ChatType chatType) {
        if (currentRetryCount < 1) {
            if (this.mRetryNeededErrorsForIm.contains(error)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mRetryNeededIM_retryafter.contains(error)) {
                if (retryAfter <= 0 || retryAfter > 5) {
                    return IMnoStrategy.StatusCode.NO_RETRY;
                }
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            } else if (this.mRetryNeededIM_changecontact.contains(error)) {
                if (newContact != null) {
                    return IMnoStrategy.StatusCode.RETRY_WITH_NEW_CONTACT_HEADER;
                }
                return IMnoStrategy.StatusCode.NO_RETRY;
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
                if (retryAfter <= 0 || retryAfter > 5) {
                    return IMnoStrategy.StatusCode.NO_RETRY;
                }
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            } else if (this.mRetryNeededFT_changecontact.contains(error)) {
                if (newContact != null) {
                    return IMnoStrategy.StatusCode.RETRY_WITH_NEW_CONTACT_HEADER;
                }
                return IMnoStrategy.StatusCode.NO_RETRY;
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
        if (ChatbotUriUtil.hasUriBotPlatform(uri)) {
            return true;
        }
        return StrategyUtils.isCapabilityValidUriForUS(uri, this.mPhoneId);
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
        IMSLog.i(str, i, "forceRefreshCapability: uris " + IMSLog.checker(uris));
        if (remoteOnline) {
            capDiscModule.getCapabilities(new ArrayList<>(uris), CapabilityRefreshType.ONLY_IF_NOT_FRESH, (long) (Capabilities.FEATURE_FT_SERVICE | Capabilities.FEATURE_CHAT_CPM), phoneId);
        }
        if (error != null && this.mForceRefreshRemoteCapa.contains(error)) {
            capDiscModule.getCapabilities(new ArrayList<>(uris), CapabilityRefreshType.ALWAYS_FORCE_REFRESH, Capabilities.FEATURE_CHATBOT_CHAT_SESSION | Capabilities.FEATURE_CHATBOT_STANDALONE_MSG, phoneId);
        }
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
        FtResumableOption resumeOption = AttFtCancelReasonResumableOptionCodeMap.translateCancelReason(cancelreason);
        if (resumeOption == null) {
            return FtResumableOption.NOTRESUMABLE;
        }
        return resumeOption;
    }

    private FtResumableOption getResumableOptionGroupFt(CancelReason cancelreason, ImDirection direction) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionGroupFt, cancelreason: " + cancelreason.getId() + " direction:" + direction.getId());
        if (direction == ImDirection.INCOMING && cancelreason == CancelReason.CANCELED_BY_REMOTE) {
            return FtResumableOption.NOTRESUMABLE;
        }
        if (cancelreason == CancelReason.ERROR || cancelreason == CancelReason.DEVICE_UNREGISTERED) {
            return FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE;
        }
        if (cancelreason == CancelReason.CANCELED_BY_USER) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
    }

    public long calSubscribeDelayTime(PresenceSubscription subscription) {
        IMSLog.i(TAG, this.mPhoneId, "calSubscribeDelayTime");
        try {
            PresenceSubscription s = subscription.clone();
            if (this.mSubscriptionQueue.remainingCapacity() == 0) {
                IMSLog.i(TAG, this.mPhoneId, "calSubscribeDelayTime: threshold is maxed");
                Date timestamp = null;
                if (this.mSubscriptionQueue.peek() != null) {
                    timestamp = this.mSubscriptionQueue.peek().getTimestamp();
                }
                long offset = 0;
                if (timestamp != null) {
                    offset = s.getTimestamp().getTime() - timestamp.getTime();
                    String str = TAG;
                    int i = this.mPhoneId;
                    IMSLog.i(str, i, "calSubscribeDelayTime: interval from " + timestamp.getTime() + " to " + s.getTimestamp().getTime() + ", offset " + offset);
                }
                if (offset >= 0 && offset < 1000) {
                    return 1000 - offset;
                }
                try {
                    this.mSubscriptionQueue.take();
                } catch (InterruptedException e) {
                    IMSLog.e(TAG, this.mPhoneId, "calSubscribeDelayTime: current queue is empty");
                }
            }
            this.mSubscriptionQueue.add(s);
            return 0;
        } catch (CloneNotSupportedException e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    public boolean isSubscribeThrottled(PresenceSubscription s, long millis, boolean isAvailFetch, boolean isAlwaysForce) {
        if (s.getState() == 5) {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: retried subscription");
            return false;
        }
        Date current = new Date();
        long offset = current.getTime() - s.getTimestamp().getTime();
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "isSubscribeThrottled: state " + s.getState() + " interval from " + s.getTimestamp().getTime() + " to " + current.getTime() + ", offset " + offset + " sourceThrottlePublish " + millis + " isAlwaysForce " + isAlwaysForce);
        if (isAlwaysForce) {
            if (s.getState() != 0 || offset >= millis) {
                return false;
            }
            return true;
        } else if (offset < millis) {
            return true;
        } else {
            return false;
        }
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long capCacheExpiry) {
        if (refreshType == CapabilityRefreshType.DISABLED) {
            return false;
        }
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability is null");
            return true;
        } else if (capex.hasFeature(Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: fetch failed capabilities");
            return true;
        } else if (refreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            return true;
        } else {
            if (refreshType == CapabilityRefreshType.FORCE_REFRESH_UCE) {
                if (!capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
                    return true;
                }
                IMSLog.i(TAG, this.mPhoneId, "needRefresh: non capabilitydisovery capable user");
                return capex.isExpired(capInfoExpiry);
            } else if ((refreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capex.isExpired(capInfoExpiry)) || refreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC) {
                return true;
            } else {
                if (refreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX || !capex.isExpired(capInfoExpiry)) {
                    return false;
                }
                return true;
            }
        }
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long serviceAvailabilityInfoExpiry, long capCacheExpiry, long msgCapvalidity) {
        return needRefresh(capex, refreshType, capInfoExpiry, capCacheExpiry);
    }

    public int getNextFileTransferAutoResumeTimer(ImDirection direction, int retryCount) {
        if (direction == ImDirection.INCOMING) {
            int[] iArr = this.mFtResumeRetryMTTimerList;
            if (retryCount < iArr.length) {
                return iArr[retryCount];
            }
        }
        if (direction != ImDirection.OUTGOING) {
            return -1;
        }
        int[] iArr2 = this.mFtResumeRetryMOTimerList;
        if (retryCount < iArr2.length) {
            return iArr2[retryCount];
        }
        return -1;
    }

    public long calThrottledPublishRetryDelayTime(long lastPublishTimestamp, long sourceThrottlePublish) {
        Date current = new Date();
        if (sourceThrottlePublish <= 0 || lastPublishTimestamp <= 0 || current.getTime() - lastPublishTimestamp >= sourceThrottlePublish * 1000) {
            return 0;
        }
        long retry = ((1000 * sourceThrottlePublish) + lastPublishTimestamp) - current.getTime();
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "calThrottledPublishRetryDelayTime: throttled. retry in " + retry + "ms");
        return retry;
    }

    public boolean isPresenceReadyToRequest(boolean ownInfoPublished, boolean paralysed) {
        return ownInfoPublished && !paralysed;
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> participants, ChatData.ChatType chatType) {
        String rcsPhaseVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "");
        Uri ftHttpCsUri = imConfig.getFtHttpCsUri();
        if (!"RCS_ATT_PHASE2".equals(rcsPhaseVersion) || ftHttpCsUri == null || TextUtils.isEmpty(ftHttpCsUri.toString()) || !isFtHttpRegistered()) {
            return false;
        }
        if (chatType == ChatData.ChatType.ONE_TO_ONE_CHAT) {
            return checkFtHttpCapability(participants);
        }
        if (chatType == ChatData.ChatType.REGULAR_GROUP_CHAT) {
            return true;
        }
        return false;
    }

    public boolean isFtHttpOnlySupported(boolean isGroup) {
        return "RCS_ATT_PHASE2".equals(ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "")) && isGroup;
    }

    public int getFtHttpSessionRetryTimer(int retryCount, ImError result) {
        if (ImError.MSRP_UNKNOWN_CONTENT_TYPE == result || ImError.MSRP_TRANSACTION_TIMED_OUT == result) {
            return -1;
        }
        int[] iArr = this.mFtHttpMOSessionRetryTimerList;
        if (retryCount < iArr.length) {
            return iArr[retryCount];
        }
        return -1;
    }

    public IMnoStrategy.StrategyResponse handleFtHttpRequestFailure(CancelReason cancelReason, ImDirection direction, boolean isGroup) {
        if (cancelReason.equals(CancelReason.FORBIDDEN_FT_HTTP)) {
            IImModule imModule = getImModule();
            if (imModule != null) {
                imModule.reconfiguration(this.mReconfigurationTimerList);
            }
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        } else if (direction != ImDirection.OUTGOING || !cancelReason.equals(CancelReason.ERROR) || isGroup) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        } else {
            return getStrategyResponse();
        }
    }

    public int getFtHttpRetryInterval(int interval, int retryCount) {
        return retryCount == 0 ? 5 : 3;
    }

    public boolean checkMainSwitchOff(Context context, int phoneId) {
        return "RCS_ATT_PHASE2".equals(ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "")) && DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.RCS, phoneId) != 1;
    }

    public boolean isHTTPUsedForEmptyFtHttpPOST() {
        return "RCS_ATT_PHASE2".equals(ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, ""));
    }

    public boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return !"RCS_ATT_PHASE2".equals(ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, ""));
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.EXCEED_MAXIMUM_RECIPIENTS);
    }

    public boolean isNeedToReportToRegiGvn(ImError imError) {
        if ("RCS_ATT_PHASE2".equals(ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, ""))) {
            return false;
        }
        return imError.isOneOf(ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.SESSION_DOESNT_EXIST)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (imError.isOneOf(ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.NORMAL_RELEASE_BEARER_UNAVAILABLE, ImError.GROUPCHAT_DISABLED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (imError.isOneOf(ImError.EXCEED_MAXIMUM_RECIPIENTS)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR, IMnoStrategy.ErrorNotificationId.EXCEED_MAXIMUM_RECIPIENTS);
        }
        return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError ftError, ChatData.ChatType chatType) {
        if (ftError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.REMOTE_PARTY_CANCELED, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.SESSION_DOESNT_EXIST)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (ftError.isOneOf(ImError.FORBIDDEN_VERSION_NOT_SUPPORTED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (ftError.isOneOf(ImError.DEVICE_UNREGISTERED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (ftError.isOneOf(ImError.EXCEED_MAXIMUM_RECIPIENTS)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR, IMnoStrategy.ErrorNotificationId.EXCEED_MAXIMUM_RECIPIENTS);
        }
        return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.BAD_EVENT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_BAD_EVENT;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
    }

    public IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason close) {
        if (close == ImSessionClosedReason.CLOSED_WITH_480_REASON_CODE) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        return getStrategyResponse();
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int stateId) {
        return chatType == ChatData.ChatType.REGULAR_GROUP_CHAT;
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

    public boolean isDisplayBotError() {
        return true;
    }

    public IMnoStrategy.StrategyResponse getUploadedFileFallbackSLMTech() {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public boolean needToCapabilityCheckForImdn(boolean isGroupChat) {
        if (!isGroupChat) {
            return true;
        }
        IMSLog.i(TAG, this.mPhoneId, "needToCapabilityCheckForImdn: failed");
        return false;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.AttStrategy$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;

        static {
            int[] iArr = new int[ImError.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = iArr;
            try {
                iArr[ImError.REMOTE_USER_INVALID.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.TRANSACTION_DOESNT_EXIST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.GONE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public boolean shouldRestartSession(ImError error) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[error.ordinal()];
        if (i == 1 || i == 2 || i == 3) {
            return true;
        }
        if (i == 4 && !boolSetting(RcsPolicySettings.RcsPolicy.GONE_SHOULD_ENDSESSION)) {
            return true;
        }
        return false;
    }

    public static class AttFtCancelReasonResumableOptionCodeMap {
        private static final MappingTranslator<CancelReason, FtResumableOption> mAttFtResumableOptionTranslator = new MappingTranslator.Builder().map(CancelReason.UNKNOWN, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CANCELED_BY_USER, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CANCELED_BY_REMOTE, FtResumableOption.NOTRESUMABLE).map(CancelReason.CANCELED_BY_SYSTEM, FtResumableOption.NOTRESUMABLE).map(CancelReason.REJECTED_BY_REMOTE, FtResumableOption.NOTRESUMABLE).map(CancelReason.TIME_OUT, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.ERROR, FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE).map(CancelReason.CONNECTION_RELEASED, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.DEVICE_UNREGISTERED, FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE).map(CancelReason.NOT_AUTHORIZED, FtResumableOption.NOTRESUMABLE).map(CancelReason.FORBIDDEN_NO_RETRY_FALLBACK, FtResumableOption.NOTRESUMABLE).map(CancelReason.MSRP_SESSION_ERROR_NO_RESUME, FtResumableOption.NOTRESUMABLE).buildTranslator();

        public static FtResumableOption translateCancelReason(CancelReason value) {
            if (mAttFtResumableOptionTranslator.isTranslationDefined(value)) {
                return mAttFtResumableOptionTranslator.translate(value);
            }
            return FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE;
        }
    }

    public ImSessionClosedReason handleSessionFailure(ImError error) {
        if (error == ImError.SESSION_DOESNT_EXIST) {
            return ImSessionClosedReason.ALL_PARTICIPANTS_LEFT;
        }
        return ImSessionClosedReason.NONE;
    }
}
