package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class VzwUPStrategy extends DefaultUPMnoStrategy {
    private static final String TAG = VzwUPStrategy.class.getSimpleName();
    private int lastNetworkType;
    private ICapabilityDiscoveryModule mDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();
    private boolean mIsCapDiscoveryOption = true;
    private boolean mIsEABEnabled = true;
    private boolean mIsLocalConfigUsed;
    private boolean mIsVLTEnabled = true;
    private final TelephonyManager mTelephonyManager = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY));

    public VzwUPStrategy(Context context, int phoneId) {
        super(context, phoneId);
        this.mIsVLTEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, false, phoneId).booleanValue();
        this.mIsEABEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, false, phoneId).booleanValue();
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, false, phoneId).booleanValue();
        init();
    }

    private void init() {
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
    }

    public boolean isCustomizedFeature(long featureCapability) {
        IImModule module;
        if (featureCapability != ((long) Capabilities.FEATURE_FT_VIA_SMS) || (module = getImModule()) == null || !module.getImConfig(this.mPhoneId).getFtHttpEnabled()) {
            return false;
        }
        return true;
    }

    public boolean isRevocationAvailableMessage(MessageBase message) {
        return false;
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int currentRetryCount, int retryAfter, ImsUri newContact, ChatData.ChatType chatType, boolean isSlmMessage, boolean isFtHttp) {
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (isSlmMessage) {
            return handleSlmFailure(imError, currentRetryCount);
        }
        IMnoStrategy.StatusCode statusCode = getRetryStrategy(currentRetryCount, imError, retryAfter, newContact, chatType);
        if (statusCode != IMnoStrategy.StatusCode.NO_RETRY) {
            return new IMnoStrategy.StrategyResponse(statusCode);
        }
        IMnoStrategy.StrategyResponse response = handleImFailure(imError, chatType);
        if (!isFtHttp || response.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
            return response;
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact, ChatData.ChatType chatType) {
        if (ImError.MSRP_SESSION_DOES_NOT_EXIST.equals(error) && currentRetryCount < 1) {
            String str = TAG;
            IMSLog.i(str, "getRetryStrategy MSRP_SESSION_DOES_NOT_EXIST; currentRetryCount= " + currentRetryCount);
            return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
        } else if (!ImError.FORBIDDEN_NO_WARNING_HEADER.equals(error) || currentRetryCount >= 4) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        } else {
            String str2 = TAG;
            IMSLog.i(str2, "getRetryStrategy FORBIDDEN_NO_WARNING_HEADER; currentRetryCount= " + currentRetryCount);
            return IMnoStrategy.StatusCode.RETRY_AFTER_REGI;
        }
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
        StringBuilder sb = new StringBuilder();
        sb.append("forceRefreshCapability");
        sb.append(IMSLog.isShipBuild() ? "" : uris);
        IMSLog.i(str, i, sb.toString());
        ArrayList arrayList = new ArrayList(uris);
        if (remoteOnline) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), phoneId);
        } else if (error != null && this.mForceRefreshRemoteCapa.contains(error)) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ALWAYS_FORCE_REFRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), phoneId);
        }
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
            if (r4 == 0) goto L_0x0054
            java.lang.Object r4 = r3.next()
            com.sec.ims.util.ImsUri r4 = (com.sec.ims.util.ImsUri) r4
            com.sec.ims.options.CapabilityRefreshType r5 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            com.sec.ims.options.Capabilities r5 = r0.getCapabilities((com.sec.ims.util.ImsUri) r4, (com.sec.ims.options.CapabilityRefreshType) r5, (int) r1)
            java.lang.String r6 = TAG
            int r7 = r10.mPhoneId
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "checkFtHttpCapability, capx: = "
            r8.append(r9)
            r8.append(r5)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.i(r6, r7, r8)
            if (r5 == 0) goto L_0x0053
            int r6 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP
            boolean r6 = r5.hasFeature(r6)
            if (r6 == 0) goto L_0x0053
            boolean r6 = r5.isAvailable()
            if (r6 != 0) goto L_0x0052
            goto L_0x0053
        L_0x0052:
            goto L_0x0017
        L_0x0053:
            return r2
        L_0x0054:
            r2 = 1
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.VzwUPStrategy.checkFtHttpCapability(java.util.Set):boolean");
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
        if (ChatbotUriUtil.hasUriBotPlatform(uri)) {
            return true;
        }
        return StrategyUtils.isCapabilityValidUriForUS(uri, this.mPhoneId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00b3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r12, long r13) {
        /*
            r11 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r11.getCapDiscModule()
            int r1 = r11.mPhoneId
            if (r0 != 0) goto L_0x0019
            java.lang.String r2 = TAG
            int r3 = r11.mPhoneId
            java.lang.String r4 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r2, r3, r4)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r2 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r3 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r2.<init>(r3)
            return r2
        L_0x0019:
            r2 = 0
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r3 = r11.getImModule()
            if (r3 == 0) goto L_0x0028
            com.sec.internal.ims.servicemodules.im.ImConfig r4 = r3.getImConfig(r1)
            boolean r2 = r4.isImCapAlwaysOn()
        L_0x0028:
            java.lang.String r4 = TAG
            int r5 = r11.mPhoneId
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "checkCapability: isCapAlwaysOn = "
            r6.append(r7)
            r6.append(r2)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r4, r5, r6)
            if (r2 == 0) goto L_0x00a9
            java.util.Iterator r4 = r12.iterator()
        L_0x0046:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x00a8
            java.lang.Object r5 = r4.next()
            com.sec.ims.util.ImsUri r5 = (com.sec.ims.util.ImsUri) r5
            com.sec.ims.options.CapabilityRefreshType r6 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            com.sec.ims.options.Capabilities r6 = r0.getCapabilities((com.sec.ims.util.ImsUri) r5, (com.sec.ims.options.CapabilityRefreshType) r6, (int) r1)
            if (r6 == 0) goto L_0x0079
            long r7 = r6.getFeature()
            int r9 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r9 = (long) r9
            int r7 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
            if (r7 != 0) goto L_0x0079
            com.sec.internal.ims.servicemodules.im.ImConfig r7 = r3.getImConfig(r1)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r7 = r7.getImMsgTech()
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r8 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ImMsgTech.SIMPLE_IM
            if (r7 != r8) goto L_0x0079
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r7 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r4.<init>(r7)
            return r4
        L_0x0079:
            if (r6 == 0) goto L_0x0089
            boolean r7 = r6.isAvailable()
            if (r7 == 0) goto L_0x0089
            boolean r7 = r11.hasOneOfFeatures(r6, r13)
            if (r7 != 0) goto L_0x0088
            goto L_0x0089
        L_0x0088:
            goto L_0x0046
        L_0x0089:
            r11.logNoCapability(r5, r6, r13)
            boolean r4 = r11.isNonRcs(r6)
            if (r4 == 0) goto L_0x00a3
            java.lang.String r4 = TAG
            int r7 = r11.mPhoneId
            java.lang.String r8 = "checkCapability: Non-RCS user"
            com.sec.internal.log.IMSLog.i(r4, r7, r8)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r7 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r4.<init>(r7)
            return r4
        L_0x00a3:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = r11.getStrategyResponse()
            return r4
        L_0x00a8:
            goto L_0x00e3
        L_0x00a9:
            java.util.Iterator r4 = r12.iterator()
        L_0x00ad:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x00e3
            java.lang.Object r5 = r4.next()
            com.sec.ims.util.ImsUri r5 = (com.sec.ims.util.ImsUri) r5
            com.sec.ims.options.CapabilityRefreshType r6 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            com.sec.ims.options.Capabilities r6 = r0.getCapabilities((com.sec.ims.util.ImsUri) r5, (com.sec.ims.options.CapabilityRefreshType) r6, (int) r1)
            if (r6 == 0) goto L_0x00cf
            boolean r7 = r6.isAvailable()
            if (r7 == 0) goto L_0x00cf
            boolean r7 = r11.hasOneOfFeatures(r6, r13)
            if (r7 != 0) goto L_0x00ce
            goto L_0x00cf
        L_0x00ce:
            goto L_0x00ad
        L_0x00cf:
            java.lang.String r4 = TAG
            int r7 = r11.mPhoneId
            java.lang.String r8 = "isCapAlwaysOn is off"
            com.sec.internal.log.IMSLog.i(r4, r7, r8)
            r11.logNoCapability(r5, r6, r13)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r7 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r4.<init>(r7)
            return r4
        L_0x00e3:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r5 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r4.<init>(r5)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.VzwUPStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean isNonRcs(Capabilities caps) {
        if (caps == null || caps.getFeature() == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) || caps.getFeature() == ((long) Capabilities.FEATURE_NON_RCS_USER)) {
            return true;
        }
        return false;
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex != null) {
            capex.setFetchType(Capabilities.FetchType.FETCH_TYPE_OTHER);
        }
        if (result != CapabilityConstants.CapExResult.USER_NOT_FOUND && result == CapabilityConstants.CapExResult.FAILURE && capex != null && capex.isAvailable()) {
            return isCapCacheExpired(capex, cacheInfoExpiry);
        }
        return true;
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long capCacheExpiry) {
        ICapabilityDiscoveryModule iCapabilityDiscoveryModule = this.mDiscoveryModule;
        if (iCapabilityDiscoveryModule != null && !iCapabilityDiscoveryModule.hasVideoOwnCapability(this.mPhoneId)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: no video, return false");
            return false;
        } else if (refreshType == CapabilityRefreshType.DISABLED) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch disabled, no refresh");
            return false;
        } else if (refreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch forced, refresh");
            return true;
        } else if (capex == null) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "needRefresh: capability is null, type " + refreshType);
            if (refreshType == CapabilityRefreshType.FORCE_REFRESH_UCE) {
                return true;
            }
            return false;
        } else if (isCapCacheExpired(capex, capCacheExpiry) || capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: fetch-blocked capabilities, no refresh");
            return false;
        } else if (!capex.isExpired(capInfoExpiry) && capex.getFetchType() != Capabilities.FetchType.FETCH_TYPE_POLL) {
            return false;
        } else {
            String str2 = TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "needRefresh: cache expired or fetch after poll(" + capex.getFetchType() + "), refresh");
            capex.setFetchType(Capabilities.FetchType.FETCH_TYPE_OTHER);
            return true;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00aa, code lost:
        if (r15.isExpired(r19) == false) goto L_0x00af;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean needRefresh(com.sec.ims.options.Capabilities r15, com.sec.ims.options.CapabilityRefreshType r16, long r17, long r19, long r21, long r23) {
        /*
            r14 = this;
            r7 = r14
            r8 = r15
            r9 = r16
            java.lang.String r0 = TAG
            int r1 = r7.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "needRefresh: mIsLocalConfigUsed: "
            r2.append(r3)
            boolean r3 = r7.mIsLocalConfigUsed
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            boolean r0 = r7.mIsLocalConfigUsed
            if (r0 == 0) goto L_0x002f
            r0 = r14
            r1 = r15
            r2 = r16
            r3 = r17
            r5 = r21
            boolean r0 = r0.needRefresh(r1, r2, r3, r5)
            return r0
        L_0x002f:
            com.sec.ims.options.CapabilityRefreshType r0 = com.sec.ims.options.CapabilityRefreshType.DISABLED
            r1 = 0
            if (r9 != r0) goto L_0x003e
            java.lang.String r0 = TAG
            int r2 = r7.mPhoneId
            java.lang.String r3 = "needRefresh: availability fetch disabled, no refresh"
            com.sec.internal.log.IMSLog.i(r0, r2, r3)
            return r1
        L_0x003e:
            com.sec.ims.options.CapabilityRefreshType r0 = com.sec.ims.options.CapabilityRefreshType.ALWAYS_FORCE_REFRESH
            r2 = 1
            if (r9 != r0) goto L_0x004d
            java.lang.String r0 = TAG
            int r1 = r7.mPhoneId
            java.lang.String r3 = "needRefresh: availability fetch forced, refresh"
            com.sec.internal.log.IMSLog.i(r0, r1, r3)
            return r2
        L_0x004d:
            if (r8 != 0) goto L_0x0061
            com.sec.ims.options.CapabilityRefreshType r0 = com.sec.ims.options.CapabilityRefreshType.FORCE_REFRESH_UCE
            if (r9 == r0) goto L_0x0057
            com.sec.ims.options.CapabilityRefreshType r0 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            if (r9 != r0) goto L_0x0061
        L_0x0057:
            java.lang.String r0 = TAG
            int r1 = r7.mPhoneId
            java.lang.String r3 = "needRefresh: capability is null, refresh only for the refreshType"
            com.sec.internal.log.IMSLog.i(r0, r1, r3)
            return r2
        L_0x0061:
            if (r8 != 0) goto L_0x006d
            java.lang.String r0 = TAG
            int r2 = r7.mPhoneId
            java.lang.String r3 = "needRefresh: capability is null, no refresh"
            com.sec.internal.log.IMSLog.i(r0, r2, r3)
            return r1
        L_0x006d:
            com.sec.ims.options.CapabilityRefreshType r0 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            if (r9 != r0) goto L_0x008b
            r3 = r17
            boolean r0 = r15.isExpired(r3)
            if (r0 != 0) goto L_0x0081
            int r0 = com.sec.ims.options.Capabilities.FEATURE_NOT_UPDATED
            boolean r0 = r15.hasFeature(r0)
            if (r0 == 0) goto L_0x008d
        L_0x0081:
            java.lang.String r0 = TAG
            int r1 = r7.mPhoneId
            java.lang.String r5 = "needRefresh: capInfo is expired or feature isn't updated, refresh"
            com.sec.internal.log.IMSLog.i(r0, r1, r5)
            return r2
        L_0x008b:
            r3 = r17
        L_0x008d:
            r5 = r21
            boolean r0 = r14.isCapCacheExpired(r15, r5)
            if (r0 != 0) goto L_0x00df
            int r0 = com.sec.ims.options.Capabilities.FEATURE_NON_RCS_USER
            boolean r0 = r15.hasFeature(r0)
            if (r0 == 0) goto L_0x00a0
            r10 = r19
            goto L_0x00e1
        L_0x00a0:
            com.sec.ims.options.CapabilityRefreshType r0 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            if (r9 == r0) goto L_0x00ad
            r10 = r19
            boolean r0 = r15.isExpired(r10)
            if (r0 != 0) goto L_0x00b7
            goto L_0x00af
        L_0x00ad:
            r10 = r19
        L_0x00af:
            com.sec.ims.options.Capabilities$FetchType r0 = r15.getFetchType()
            com.sec.ims.options.Capabilities$FetchType r12 = com.sec.ims.options.Capabilities.FetchType.FETCH_TYPE_POLL
            if (r0 != r12) goto L_0x00de
        L_0x00b7:
            java.lang.String r0 = TAG
            int r1 = r7.mPhoneId
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "needRefresh: cache expired or fetch after poll("
            r12.append(r13)
            com.sec.ims.options.Capabilities$FetchType r13 = r15.getFetchType()
            r12.append(r13)
            java.lang.String r13 = "), refresh"
            r12.append(r13)
            java.lang.String r12 = r12.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r12)
            com.sec.ims.options.Capabilities$FetchType r0 = com.sec.ims.options.Capabilities.FetchType.FETCH_TYPE_OTHER
            r15.setFetchType(r0)
            return r2
        L_0x00de:
            return r1
        L_0x00df:
            r10 = r19
        L_0x00e1:
            java.lang.String r0 = TAG
            int r2 = r7.mPhoneId
            java.lang.String r12 = "needRefresh: fetch-blocked capabilities, no refresh"
            com.sec.internal.log.IMSLog.i(r0, r2, r12)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.VzwUPStrategy.needRefresh(com.sec.ims.options.Capabilities, com.sec.ims.options.CapabilityRefreshType, long, long, long, long):boolean");
    }

    private boolean isCapCacheExpired(Capabilities capex, long cacheInfoExpiry) {
        boolean isCapCacheExpired = false;
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "isCapCacheExpired: Capability is null");
            return false;
        }
        Date current = new Date();
        if (current.getTime() - capex.getTimestamp().getTime() >= 1000 * cacheInfoExpiry && cacheInfoExpiry > 0) {
            isCapCacheExpired = true;
        }
        if (isCapCacheExpired) {
            capex.resetFeatures();
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "isCapCacheExpired: " + cacheInfoExpiry + " current " + current.getTime() + " timestamp " + capex.getTimestamp().getTime() + " diff " + (current.getTime() - capex.getTimestamp().getTime()));
        }
        return isCapCacheExpired;
    }

    public boolean needPoll(Capabilities capex, long capInfoExpiry) {
        return true;
    }

    public long isTdelay(long delay) {
        boolean isSVLTEDevice = SemSystemProperties.getBoolean("ro.ril.svlte1x", false);
        if (isSVLTEDevice || delay < 1) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "SVLTE: " + isSVLTEDevice + ", delay: " + delay);
            return 0;
        }
        int networkType = this.mTelephonyManager.getNetworkType();
        TelephonyManagerExt.NetworkTypeExt netExtType = TelephonyManagerExt.getNetworkEnumType(networkType);
        TelephonyManagerExt.NetworkTypeExt lastNetExtType = TelephonyManagerExt.getNetworkEnumType(this.lastNetworkType);
        String str2 = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "SRLTE, current network: " + netExtType + ", last network type : " + lastNetExtType);
        this.lastNetworkType = networkType;
        if (lastNetExtType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD && netExtType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return (delay - 1) * 1000;
        }
        return 0;
    }

    public long updateFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        if (capex == null || capex.hasFeature(Capabilities.FEATURE_NOT_UPDATED) || capex.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            return features;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateFeatures: updated features " + Capabilities.dumpFeature(capex.getFeature() | features));
        return capex.getFeature() | features;
    }

    public boolean needUnpublish(ImsRegistration oldInfo, ImsRegistration newInfo) {
        if (oldInfo == null) {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: oldInfo: empty");
            return false;
        }
        int voiceType = ImsConstants.SystemSettings.VOLTE_SLOT1.get(this.mContext, 0);
        String str = TAG;
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("needUnpublish: getVoiceTechType: ");
        sb.append(voiceType == 0 ? "VOLTE" : "CS");
        IMSLog.i(str, i, sb.toString());
        if ((oldInfo.hasService("mmtel") || oldInfo.hasService("mmtel-video")) && !newInfo.hasService("mmtel") && !newInfo.hasService("mmtel-video") && voiceType == 1) {
            return true;
        }
        return false;
    }

    public boolean needUnpublish(int phoneId) {
        TelephonyManagerExt.NetworkTypeExt netExtType = TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType());
        if (netExtType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "needUnpublish: network type: " + netExtType);
            return false;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", phoneId) != 1) {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: isVoLteEnabled: off");
            return true;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", phoneId) == 1 || DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", phoneId) == 1) {
            return false;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: mmtel/mmtel-video: off");
            return true;
        }
    }

    public boolean isSubscribeThrottled(PresenceSubscription s, long millis, boolean isAvailFetch, boolean isAlwaysForce) {
        if (isAlwaysForce) {
            IMSLog.i(TAG, this.mPhoneId, "refresh type is always force.");
            return false;
        }
        CapabilityConstants.RequestType requestType = s.getRequestType();
        if (!isAvailFetch || !(requestType == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC || requestType == CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE)) {
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
        IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: avail fetch after poll, not throttled");
        return false;
    }

    public void updateOmaDmNodes(int phoneId) {
        boolean modified = false;
        boolean newValue = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, false, phoneId).booleanValue();
        if (this.mIsEABEnabled != newValue) {
            this.mIsEABEnabled = newValue;
            modified = true;
        }
        boolean newValue2 = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, false, phoneId).booleanValue();
        if (this.mIsVLTEnabled != newValue2) {
            this.mIsVLTEnabled = newValue2;
            modified = true;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateOmaDmNodes: mIsVLTEnabled: " + this.mIsVLTEnabled + " mIsEABEnabled: " + this.mIsEABEnabled + " modified = " + modified);
        if (modified) {
            startServiceBasedOnOmaDmNodes(phoneId);
        }
    }

    public void startServiceBasedOnOmaDmNodes(int phoneId) {
        IMSLog.i(TAG, this.mPhoneId, "startServiceBasedOnOmaDmNodes");
        if (this.mDiscoveryModule != null) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "startServiceBasedOnOmaDmNodes: mIsVLTEnabled: " + this.mIsVLTEnabled + " mIsEABEnabled: " + this.mIsEABEnabled);
            if (!this.mIsVLTEnabled) {
                this.mDiscoveryModule.clearCapabilitiesCache(phoneId);
                this.mDiscoveryModule.changeParalysed(true, phoneId);
            }
        }
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

    public boolean checkCapDiscoveryOption() {
        if (TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType()) != TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return true;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "return CapDiscoveryOption: " + this.mIsCapDiscoveryOption);
        return this.mIsCapDiscoveryOption;
    }

    public void updateCapDiscoveryOption() {
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, false, this.mPhoneId).booleanValue();
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "update CapDiscoveryOption: " + this.mIsCapDiscoveryOption);
    }

    public boolean isLocalConfigUsed() {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "isLocalConfigUsed: " + this.mIsLocalConfigUsed);
        return this.mIsLocalConfigUsed;
    }

    public void updateLocalConfigUsedState(boolean useLocalConfig) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateLocalConfigUsedState: change mIsLocalConfigUsed(" + this.mIsLocalConfigUsed + ") to useLocalConfig(" + useLocalConfig + ")");
        this.mIsLocalConfigUsed = useLocalConfig;
    }

    public boolean isRemoteConfigNeeded(int phoneId) {
        if (ConfigUtil.getAutoconfigSourceWithFeature(this.mContext, phoneId, 0) == 0) {
            return true;
        }
        return false;
    }

    public boolean isPresenceReadyToRequest(boolean ownInfoPublished, boolean paralysed) {
        return ownInfoPublished && !paralysed;
    }

    public void changeServiceDescription() {
        IMSLog.i(TAG, this.mPhoneId, "changeServiceDescription: VoLTE Capabilities Discovery");
        ServiceTuple.setServiceDescription((long) Capabilities.FEATURE_PRESENCE_DISCOVERY, "VoLTE Capabilities Discovery");
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT, ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_FORBIDDEN;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_PROVISIONED;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NOT_FOUND;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_INTERVAL_TOO_SHORT;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE, PresenceResponse.PresenceFailureReason.DECLINE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.NO_RESPONSE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_DISABLE_MODE;
    }

    public long getThrottledDelay(long delay) {
        return 3 + delay;
    }
}
