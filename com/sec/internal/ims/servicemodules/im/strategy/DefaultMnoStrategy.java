package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
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
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.RetryTimerUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.List;
import java.util.Set;

public abstract class DefaultMnoStrategy implements IMnoStrategy {
    protected static final int MAX_RETRY_COUNT_AFTER_REGI = 4;
    private static final String TAG = DefaultMnoStrategy.class.getSimpleName();
    protected Context mContext;
    protected final int mPhoneId;
    protected RcsPolicySettings mPolicySettings;
    protected RcsPolicySettings.RcsPolicyType mRcsPolicyType = RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;

    public abstract IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j);

    public abstract IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z);

    public abstract boolean dropUnsupportedCharacter(String str);

    public abstract long getThrottledDelay(long j);

    public abstract IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason imSessionClosedReason);

    public abstract IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType);

    public abstract IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError imError);

    public abstract IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType);

    public abstract PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z);

    public abstract IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ImsUri imsUri, ChatData.ChatType chatType, boolean z, boolean z2);

    public abstract IMnoStrategy.StrategyResponse handleSlmFailure(ImError imError);

    public abstract boolean isCloseSessionNeeded(ImError imError);

    public abstract boolean isCustomizedFeature(long j);

    public abstract boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i);

    public abstract boolean isFTHTTPAutoResumeAndCancelPerConnectionChange();

    public abstract boolean isFirstMsgInvite(boolean z);

    public abstract boolean isNeedToReportToRegiGvn(ImError imError);

    public abstract boolean isResendFTResume(boolean z);

    public abstract boolean isRevocationAvailableMessage(MessageBase messageBase);

    public abstract boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2);

    public abstract boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4);

    public abstract boolean needStopAutoRejoin(ImError imError);

    public abstract long updateAvailableFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult);

    public abstract long updateFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult);

    public DefaultMnoStrategy(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mPolicySettings = new RcsPolicySettings(context, phoneId);
    }

    public RcsPolicySettings.RcsPolicyType getPolicyType() {
        return this.mRcsPolicyType;
    }

    public void setPolicyType(RcsPolicySettings.RcsPolicyType policyType) {
        this.mRcsPolicyType = policyType;
    }

    public boolean boolSetting(String name) {
        return this.mPolicySettings.readBool(name);
    }

    public int intSetting(String name) {
        return this.mPolicySettings.readInt(name);
    }

    public String stringSetting(String name) {
        return this.mPolicySettings.readString(name);
    }

    public List<String> stringArraySetting(String name) {
        return this.mPolicySettings.readStringArray(name);
    }

    /* access modifiers changed from: protected */
    public ICapabilityDiscoveryModule getCapDiscModule() {
        if (ImsRegistry.isReady()) {
            return ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        }
        IMSLog.i(TAG, this.mPhoneId, "getCapDiscModule: getInstance is null");
        return null;
    }

    /* access modifiers changed from: protected */
    public IImModule getImModule() {
        if (ImsRegistry.isReady()) {
            return ImsRegistry.getServiceModuleManager().getImModule();
        }
        IMSLog.i(TAG, this.mPhoneId, "getImModule: getInstance is null");
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOneOfFeatures(Capabilities cap, long features) {
        boolean ret = false;
        if (cap != null) {
            ret = (cap.getFeature() & features) > 0;
            if (!ret) {
                String str = TAG;
                int i = this.mPhoneId;
                StringBuilder sb = new StringBuilder();
                sb.append("hasOneOfFeatures:");
                sb.append(cap.getUri() == null ? "" : cap.getUri().toStringLimit());
                sb.append(" getFeature()=");
                sb.append(cap.getFeature());
                sb.append(", features=");
                sb.append(features);
                sb.append(", ret=false");
                IMSLog.i(str, i, sb.toString());
            }
        }
        return ret;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOneOfFeaturesAvailable(Capabilities cap, long features) {
        boolean ret = false;
        if (cap != null) {
            ret = (cap.getAvailableFeatures() & features) > 0 || features == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER);
            if (!ret) {
                String str = TAG;
                int i = this.mPhoneId;
                StringBuilder sb = new StringBuilder();
                sb.append("hasOneOfFeaturesAvailable:");
                sb.append(cap.getUri() == null ? "" : cap.getUri().toStringLimit());
                sb.append(" getFeature()=");
                sb.append(cap.getFeature());
                sb.append(", features=");
                sb.append(features);
                sb.append(", ret=false");
                IMSLog.i(str, i, sb.toString());
            }
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public boolean isSlmEnabled() {
        IImModule module = getImModule();
        return module != null && module.isServiceRegistered(this.mPhoneId, "slm");
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse getStrategyResponse() {
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean hasChatbotUri, boolean isFtHttp) {
        ImError imError2 = imError;
        IMnoStrategy.StrategyResponse strategyResponse = handleSendingMessageFailure(imError, currentRetryCount, retryAfter, newContact, chatType, isSlmMessage, isFtHttp);
        if (!hasChatbotUri || (strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM && strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY && imError2 != ImError.GONE && imError2 != ImError.REQUEST_PENDING)) {
            return strategyResponse;
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError ftError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact, ChatData.ChatType chatType) {
        if (!ImError.FORBIDDEN_NO_WARNING_HEADER.equals(error) || currentRetryCount >= 4) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        String str = TAG;
        IMSLog.i(str, "getRetryStrategy FORBIDDEN_NO_WARNING_HEADER; currentRetryCount= " + currentRetryCount);
        return IMnoStrategy.StatusCode.RETRY_AFTER_REGI;
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact) {
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
        return true;
    }

    public FtResumableOption getftResumableOption(CancelReason cancelreason, boolean isGroup, ImDirection direction, int transferMech) {
        if (cancelreason == CancelReason.CANCELED_BY_USER || cancelreason == CancelReason.LOW_MEMORY) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        return FtResumableOption.NOTRESUMABLE;
    }

    public void forceRefreshCapability(Set<ImsUri> set, boolean remoteOnline, ImError error) {
    }

    public long calSubscribeDelayTime(PresenceSubscription s) {
        return 0;
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
        IMSLog.i(str, i, "isSubscribeThrottled: interval from " + s.getTimestamp().getTime() + " to " + current.getTime() + ", offset " + offset + " sourceThrottlePublish " + millis);
        if (offset < millis) {
            return true;
        }
        return false;
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
        } else if (refreshType == CapabilityRefreshType.FORCE_REFRESH_UCE || refreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH || refreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC) {
            return true;
        } else {
            if (refreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capex.isExpired(capInfoExpiry)) {
                return true;
            }
            if (refreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX || !capex.isExpired(capInfoExpiry)) {
                return false;
            }
            return true;
        }
    }

    public boolean needPoll(Capabilities capex, long capInfoExpiry) {
        return needRefresh(capex, CapabilityRefreshType.ONLY_IF_NOT_FRESH, capInfoExpiry, 0);
    }

    public long isTdelay(long delay) {
        return 0;
    }

    public boolean needUnpublish(ImsRegistration oldInfo, ImsRegistration newInfo) {
        return false;
    }

    public boolean needUnpublish(int phoneId) {
        return false;
    }

    public void updateOmaDmNodes(int phoneId) {
    }

    public void startServiceBasedOnOmaDmNodes(int phoneId) {
    }

    public String checkNeedParsing(String number) {
        return number;
    }

    public int getNextFileTransferAutoResumeTimer(ImDirection direction, int retryCount) {
        return -1;
    }

    public boolean checkCapDiscoveryOption() {
        return true;
    }

    public void updateCapDiscoveryOption() {
    }

    public boolean isLocalConfigUsed() {
        return false;
    }

    public void updateLocalConfigUsedState(boolean useLocalConfig) {
    }

    public boolean isRemoteConfigNeeded(int phoneId) {
        return false;
    }

    public boolean isPresenceReadyToRequest(boolean ownInfoPublished, boolean paralysed) {
        return true;
    }

    public void changeServiceDescription() {
    }

    public long calThrottledPublishRetryDelayTime(long lastPublishTimestamp, long sourceThrottlePublish) {
        return 0;
    }

    public boolean isFtHttpOnlySupported(boolean isGroup) {
        return false;
    }

    public ImDirection convertToImDirection(String strDirection) {
        return ImDirection.INCOMING;
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> participants, ChatData.ChatType chatType) {
        return imConfig.getFtDefaultMech() == ImConstants.FtMech.HTTP && isFtHttpRegistered() && (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) || checkFtHttpCapability(participants));
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x001d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkFtHttpCapability(java.util.Set<com.sec.ims.util.ImsUri> r11) {
        /*
            r10 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r10.getCapDiscModule()
            int r1 = r10.mPhoneId
            r2 = 0
            if (r0 != 0) goto L_0x0013
            java.lang.String r3 = TAG
            int r4 = r10.mPhoneId
            java.lang.String r5 = "checkFtHttpCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            return r2
        L_0x0013:
            java.util.Iterator r3 = r11.iterator()
        L_0x0017:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x0065
            java.lang.Object r4 = r3.next()
            com.sec.ims.util.ImsUri r4 = (com.sec.ims.util.ImsUri) r4
            com.sec.ims.options.CapabilityRefreshType r5 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r5 = r0.getCapabilities((com.sec.ims.util.ImsUri) r4, (com.sec.ims.options.CapabilityRefreshType) r5, (int) r1)
            java.lang.String r6 = TAG
            int r7 = r10.mPhoneId
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "checkFtHttpCapability, capx: = "
            r8.append(r9)
            if (r5 == 0) goto L_0x003e
            java.lang.String r9 = r5.toString()
            goto L_0x003f
        L_0x003e:
            r9 = 0
        L_0x003f:
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.i(r6, r7, r8)
            if (r5 == 0) goto L_0x005b
            int r6 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP
            boolean r6 = r5.hasFeature(r6)
            if (r6 == 0) goto L_0x005b
            boolean r6 = r5.isAvailable()
            if (r6 != 0) goto L_0x005a
            goto L_0x005b
        L_0x005a:
            goto L_0x0017
        L_0x005b:
            java.lang.String r3 = TAG
            int r6 = r10.mPhoneId
            java.lang.String r7 = "No FT HTTP capability"
            com.sec.internal.log.IMSLog.i(r3, r6, r7)
            return r2
        L_0x0065:
            r2 = 1
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.DefaultMnoStrategy.checkFtHttpCapability(java.util.Set):boolean");
    }

    /* access modifiers changed from: protected */
    public boolean isFtHttpRegistered() {
        IImModule imModule = getImModule();
        return imModule != null && imModule.isServiceRegistered(this.mPhoneId, "ft_http");
    }

    public int getFtHttpSessionRetryTimer(int retryCount, ImError result) {
        return -1;
    }

    public RoutingType getMsgRoutingType(ImsUri requestUri, ImsUri pAssertedId, ImsUri sender, ImsUri receiver, boolean isGroupchat) {
        return RoutingType.NONE;
    }

    public IMnoStrategy.StrategyResponse handleFtHttpRequestFailure(CancelReason cancelReason, ImDirection direction, boolean isGroup) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.HttpStrategyResponse handleFtHttpDownloadError(HttpRequest httpReq) {
        int delay;
        CancelReason reason = CancelReason.ERROR;
        int code = httpReq.code();
        if (code == 403) {
            delay = 3;
            handleFtHttpRequestFailure(CancelReason.FORBIDDEN_FT_HTTP, ImDirection.INCOMING, false);
        } else if (code != 503) {
            delay = 3;
        } else {
            delay = RetryTimerUtil.getRetryAfter(httpReq.header(HttpRequest.HEADER_RETRY_AFTER));
        }
        return new IMnoStrategy.HttpStrategyResponse(reason, delay);
    }

    public boolean isHTTPUsedForEmptyFtHttpPOST() {
        return false;
    }

    public String getFtHttpUserAgent(ImConfig imConfig) {
        return imConfig.getUserAgent();
    }

    public Uri getFtHttpCsUri(ImConfig imConfig, Set<ImsUri> set, boolean isExtraFt, boolean isGroupChat) {
        return imConfig.getFtHttpCsUri();
    }

    /* access modifiers changed from: protected */
    public boolean checkUserAvailableOffline(Set<ImsUri> participants) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        int phoneId = this.mPhoneId;
        if (capDiscModule == null) {
            IMSLog.i(TAG, this.mPhoneId, "checkUserAvailableOffline: capDiscModule is null");
            return false;
        }
        for (ImsUri uri : participants) {
            Capabilities capx = capDiscModule.getCapabilities(uri, CapabilityRefreshType.ONLY_IF_NOT_FRESH, phoneId);
            String str = TAG;
            int i = this.mPhoneId;
            StringBuilder sb = new StringBuilder();
            sb.append("checkUserAvailableOffline, capx: = ");
            sb.append(capx != null ? capx.toString() : null);
            IMSLog.i(str, i, sb.toString());
            boolean capIsNotNull = capx != null;
            boolean featureNonRcs = capIsNotNull && capx.hasFeature(Capabilities.FEATURE_NON_RCS_USER);
            boolean featureIsNotUpdated = capIsNotNull && capx.hasFeature(Capabilities.FEATURE_NOT_UPDATED);
            if (capIsNotNull && !capx.isAvailable() && !featureNonRcs && !featureIsNotUpdated) {
                IMSLog.i(TAG, this.mPhoneId, "USER_AVAILABLE_OFFLINE..!!");
                return true;
            }
        }
        return false;
    }

    public Set<ImsUri> getNetworkPreferredUri(UriGenerator uriGenerator, Set<ImsUri> uris) {
        return uriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, uris);
    }

    public String getErrorReasonForStrategyResponse(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse) {
        if (strategyResponse == null) {
            return null;
        }
        if (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS) {
            return ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString();
        }
        return null;
    }

    public boolean checkMainSwitchOff(Context context, int phoneId) {
        return false;
    }

    public int getFtHttpRetryInterval(int interval, int retryCount) {
        return interval;
    }

    public ImSessionStopReason getSessionStopReason(boolean isGroupChat) {
        return isGroupChat ? ImSessionStopReason.INVOLUNTARILY : ImSessionStopReason.CLOSE_1_TO_1_SESSION;
    }

    public final boolean checkImsiBasedRegi(ImsRegistration regiInfo) {
        ISimManager sm;
        if (!boolSetting(RcsPolicySettings.RcsPolicy.CHECK_IMSIBASED_REGI) || (sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId)) == null) {
            return false;
        }
        String imsi = sm.getImsi();
        ImsUri impu = regiInfo.getRegisteredImpu();
        String str = TAG;
        IMSLog.s(str, "checkImsiBasedRegi: impu " + impu);
        if (impu == null || imsi == null || impu.getUser() == null) {
            return false;
        }
        return impu.getUser().contains(imsi);
    }

    public final boolean isWarnSizeFile(Network network, long fileSize, long warnSizeFileTr, boolean isWifiConnected) {
        if (warnSizeFileTr == 0 || fileSize <= warnSizeFileTr) {
            return false;
        }
        if (boolSetting(RcsPolicySettings.RcsPolicy.IGNORE_WIFI_WARNSIZE)) {
            if (boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN)) {
                String str = TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "isWarnSizeFile: isWifiConnected = " + isWifiConnected);
                return !isWifiConnected;
            }
            NetworkInfo ni = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(network);
            if (ni != null && ni.getType() == 1) {
                IMSLog.i(TAG, this.mPhoneId, "isWarnSizeFile: Wifi connected");
                return false;
            }
        }
        return true;
    }

    public boolean isBMode(boolean checkSettingOnly) {
        return false;
    }

    public ImConstants.ChatbotMessagingTech checkChatbotMessagingTech(ImConfig config, boolean isGroupChat, Set<ImsUri> set) {
        return ImConstants.ChatbotMessagingTech.NONE;
    }

    public boolean checkSlmFileType(String contentType) {
        return true;
    }

    public boolean isDisplayBotError() {
        return false;
    }

    public boolean isDisplayWarnText() {
        return false;
    }

    public IMnoStrategy.StrategyResponse getUploadedFileFallbackSLMTech() {
        return getStrategyResponse();
    }

    public boolean needToCapabilityCheckForImdn(boolean isGroupChat) {
        return false;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.DefaultMnoStrategy$1  reason: invalid class name */
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
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.SESSION_DOESNT_EXIST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.TRANSACTION_DOESNT_EXIST.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.GONE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public boolean shouldRestartSession(ImError error) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[error.ordinal()];
        if (i == 1 || i == 2 || i == 3 || i == 4) {
            return true;
        }
        if (i == 5 && !boolSetting(RcsPolicySettings.RcsPolicy.GONE_SHOULD_ENDSESSION)) {
            return true;
        }
        return false;
    }

    public boolean loadRcsSettings(boolean forceReload) {
        return this.mPolicySettings.load(forceReload);
    }

    public ImSessionClosedReason handleSessionFailure(ImError error) {
        return ImSessionClosedReason.NONE;
    }
}
