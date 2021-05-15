package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class TmoStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = TmoStrategy.class.getSimpleName();
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 5;
    private final ArrayList<Integer> mFtResumeRetryMOTimerList = new ArrayList<>();
    private final ArrayList<Integer> mFtResumeRetryMTTimerList = new ArrayList<>();
    private final HashSet<ImError> mTmoForceRefreshRemoteCapa = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrorsForGroupIm = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrors_AfterRegi = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededFT_changecontact = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededIM_changecontact = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededIM_retryafter = new HashSet<>();

    public TmoStrategy(Context context, int phoneId) {
        super(context, phoneId);
        initTmoMaps();
    }

    private void initTmoMaps() {
        this.mTmoRetryNeededErrorsForIm.add(ImError.UNSUPPORTED_URI_SCHEME);
        this.mTmoRetryNeededErrorsForIm.add(ImError.NETWORK_ERROR);
        this.mTmoRetryNeededErrorsForIm.add(ImError.BAD_GATEWAY);
        this.mTmoRetryNeededErrorsForIm.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        this.mTmoRetryNeededErrorsForIm.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mTmoRetryNeededErrorsForIm.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mTmoRetryNeededErrorsForIm.add(ImError.REQUEST_PENDING);
        this.mTmoRetryNeededErrorsForIm.add(ImError.FORBIDDEN_RETRY_FALLBACK);
        this.mTmoRetryNeededErrorsForIm.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mTmoRetryNeededErrorsForIm.add(ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK);
        this.mTmoRetryNeededErrorsForIm.add(ImError.LOOP_DETECTED);
        this.mTmoRetryNeededErrorsForIm.add(ImError.TOO_MANY_HOPS);
        this.mTmoRetryNeededErrorsForIm.add(ImError.REMOTE_TEMPORARILY_UNAVAILABLE);
        this.mTmoRetryNeededErrorsForGroupIm.add(ImError.MSRP_SESSION_DOES_NOT_EXIST);
        this.mTmoRetryNeededErrorsForGroupIm.add(ImError.MSRP_ACTION_NOT_ALLOWED);
        this.mTmoRetryNeededErrorsForFt.add(ImError.FORBIDDEN_RETRY_FALLBACK);
        this.mTmoRetryNeededErrorsForFt.add(ImError.UNSUPPORTED_URI_SCHEME);
        this.mTmoRetryNeededErrorsForFt.add(ImError.BAD_GATEWAY);
        this.mTmoRetryNeededErrorsForFt.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        this.mTmoRetryNeededErrorsForFt.add(ImError.REQUEST_PENDING);
        this.mTmoRetryNeededErrorsForFt.add(ImError.TRANSACTION_DOESNT_EXIST);
        this.mTmoRetryNeededErrorsForFt.add(ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK);
        this.mTmoRetryNeededErrorsForFt.add(ImError.GONE);
        this.mTmoRetryNeededErrorsForFt.add(ImError.LOOP_DETECTED);
        this.mTmoRetryNeededErrorsForFt.add(ImError.TOO_MANY_HOPS);
        this.mTmoRetryNeededErrorsForFt.add(ImError.REMOTE_TEMPORARILY_UNAVAILABLE);
        this.mTmoRetryNeededFT_retryafter.add(ImError.REMOTE_TEMPORARILY_UNAVAILABLE);
        this.mTmoRetryNeededFT_retryafter.add(ImError.INTERNAL_SERVER_ERROR);
        this.mTmoRetryNeededFT_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        this.mTmoRetryNeededFT_retryafter.add(ImError.BUSY_EVERYWHERE);
        this.mTmoRetryNeededIM_retryafter.add(ImError.REMOTE_TEMPORARILY_UNAVAILABLE);
        this.mTmoRetryNeededIM_retryafter.add(ImError.INTERNAL_SERVER_ERROR);
        this.mTmoRetryNeededIM_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        this.mTmoRetryNeededIM_retryafter.add(ImError.BUSY_EVERYWHERE);
        this.mTmoRetryNeededIM_retryafter.add(ImError.REMOTE_USER_INVALID);
        this.mTmoRetryNeededFT_changecontact.add(ImError.MULTIPLE_CHOICES);
        this.mTmoRetryNeededFT_changecontact.add(ImError.MOVED_PERMANENTLY);
        this.mTmoRetryNeededFT_changecontact.add(ImError.MOVED_TEMPORARILY);
        this.mTmoRetryNeededFT_changecontact.add(ImError.USE_PROXY);
        this.mTmoRetryNeededIM_changecontact.add(ImError.MULTIPLE_CHOICES);
        this.mTmoRetryNeededIM_changecontact.add(ImError.MOVED_PERMANENTLY);
        this.mTmoRetryNeededIM_changecontact.add(ImError.MOVED_TEMPORARILY);
        this.mTmoRetryNeededIM_changecontact.add(ImError.USE_PROXY);
        this.mTmoForceRefreshRemoteCapa.add(ImError.SESSION_TIMED_OUT);
        this.mTmoForceRefreshRemoteCapa.add(ImError.REMOTE_TEMPORARILY_UNAVAILABLE);
        this.mTmoForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
        this.mTmoRetryNeededErrors_AfterRegi.add(ImError.FORBIDDEN_NO_WARNING_HEADER);
        this.mFtResumeRetryMTTimerList.add(1);
        this.mFtResumeRetryMOTimerList.add(1);
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse getStrategyResponse() {
        return getStrategyResponse(ChatData.ChatType.ONE_TO_ONE_CHAT);
    }

    private IMnoStrategy.StrategyResponse getStrategyResponse(ChatData.ChatType chatType) {
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return super.getStrategyResponse();
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

    public IMnoStrategy.StrategyResponse handleFtHttpRequestFailure(CancelReason cancelReason, ImDirection direction, boolean isGroup) {
        if (direction != ImDirection.OUTGOING || !cancelReason.equals(CancelReason.ERROR) || isGroup) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return getStrategyResponse();
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> uris, long features, ChatData.ChatType chatType, boolean isBroadcastMsg) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "checkCapability->features:" + features + ", isBroadcastMsg:" + isBroadcastMsg);
        if (ChatData.ChatType.isGroupChat(chatType) && isBroadcastMsg) {
            return getStrategyResponse();
        }
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return checkCapability(uris, features);
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x0020  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r10, long r11) {
        /*
            r9 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r9.getCapDiscModule()
            int r1 = r9.mPhoneId
            if (r0 != 0) goto L_0x0016
            java.lang.String r2 = TAG
            int r3 = r9.mPhoneId
            java.lang.String r4 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r2, r3, r4)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r2 = r9.getStrategyResponse()
            return r2
        L_0x0016:
            java.util.Iterator r2 = r10.iterator()
        L_0x001a:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x00b1
            java.lang.Object r3 = r2.next()
            com.sec.ims.util.ImsUri r3 = (com.sec.ims.util.ImsUri) r3
            com.sec.ims.options.CapabilityRefreshType r4 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r4 = r0.getCapabilities((com.sec.ims.util.ImsUri) r3, (com.sec.ims.options.CapabilityRefreshType) r4, (int) r1)
            if (r4 == 0) goto L_0x003c
            boolean r5 = r4.isAvailable()
            if (r5 == 0) goto L_0x003c
            boolean r5 = r9.hasOneOfFeaturesAvailable(r4, r11)
            if (r5 != 0) goto L_0x003b
            goto L_0x003c
        L_0x003b:
            goto L_0x001a
        L_0x003c:
            java.lang.String r2 = TAG
            int r5 = r9.mPhoneId
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "checkCapability: No capabilities for "
            r6.append(r7)
            boolean r7 = com.sec.internal.log.IMSLog.isShipBuild()
            if (r7 == 0) goto L_0x0057
            if (r3 == 0) goto L_0x0057
            java.lang.String r7 = r3.toStringLimit()
            goto L_0x0058
        L_0x0057:
            r7 = r3
        L_0x0058:
            r6.append(r7)
            if (r4 != 0) goto L_0x0060
            java.lang.String r7 = ""
            goto L_0x0075
        L_0x0060:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = ": isAvailable="
            r7.append(r8)
            boolean r8 = r4.isAvailable()
            r7.append(r8)
            java.lang.String r7 = r7.toString()
        L_0x0075:
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r2, r5, r6)
            r2 = 302710784(0x120b0000, float:4.3860666E-28)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            int r6 = r9.mPhoneId
            r5.append(r6)
            java.lang.String r6 = ","
            r5.append(r6)
            r5.append(r11)
            java.lang.String r6 = ",NOCAP,"
            r5.append(r6)
            if (r3 == 0) goto L_0x009f
            java.lang.String r6 = r3.toStringLimit()
            goto L_0x00a2
        L_0x009f:
            java.lang.String r6 = "xx"
        L_0x00a2:
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.c(r2, r5)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r2 = r9.getStrategyResponse()
            return r2
        L_0x00b1:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r2 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r3 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r2.<init>(r3)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.TmoStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact, ChatData.ChatType chatType) {
        if (this.mTmoRetryNeededErrors_AfterRegi.contains(error) && currentRetryCount < 4) {
            return IMnoStrategy.StatusCode.RETRY_AFTER_REGI;
        }
        if (currentRetryCount >= 1) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        if (this.mTmoRetryNeededIM_retryafter.contains(error)) {
            if (retryAfter <= 0 || retryAfter > 5) {
                return IMnoStrategy.StatusCode.NO_RETRY;
            }
            return IMnoStrategy.StatusCode.RETRY_AFTER;
        } else if (ChatData.ChatType.isGroupChat(chatType) && this.mTmoRetryNeededErrorsForGroupIm.contains(error)) {
            return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        } else {
            if (this.mTmoRetryNeededErrorsForIm.contains(error)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (!this.mTmoRetryNeededIM_changecontact.contains(error)) {
                return IMnoStrategy.StatusCode.NO_RETRY;
            }
            if (newContact != null) {
                return IMnoStrategy.StatusCode.RETRY_WITH_NEW_CONTACT_HEADER;
            }
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int currentRetryCount, ImError error, int retryAfter, ImsUri newContact) {
        if (!this.mTmoRetryNeededFT_retryafter.contains(error)) {
            if (currentRetryCount < 1) {
                if (this.mTmoRetryNeededErrorsForFt.contains(error)) {
                    return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
                }
                if (this.mTmoRetryNeededFT_changecontact.contains(error)) {
                    if (newContact != null) {
                        return IMnoStrategy.StatusCode.RETRY_WITH_NEW_CONTACT_HEADER;
                    }
                    return IMnoStrategy.StatusCode.NO_RETRY;
                }
            }
            return IMnoStrategy.StatusCode.NO_RETRY;
        } else if (retryAfter <= 0 || retryAfter > 5) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        } else {
            return IMnoStrategy.StatusCode.RETRY_AFTER;
        }
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
        if (!ChatbotUriUtil.hasUriBotPlatform(uri)) {
            return StrategyUtils.isCapabilityValidUriForUS(uri, this.mPhoneId);
        }
        return true;
    }

    public void forceRefreshCapability(Set<ImsUri> uris, boolean remoteOnline, ImError error) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        int phoneId = this.mPhoneId;
        if (capDiscModule == null) {
            IMSLog.i(TAG, phoneId, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        IMSLog.i(str, phoneId, "forceRefreshCapability: uris " + IMSLog.numberChecker((Collection<ImsUri>) uris));
        if (remoteOnline) {
            for (ImsUri uri : uris) {
                capDiscModule.getCapabilities(uri, (long) (Capabilities.FEATURE_FT_SERVICE | Capabilities.FEATURE_CHAT_CPM), phoneId);
            }
        } else if (error != null && this.mTmoForceRefreshRemoteCapa.contains(error)) {
            for (ImsUri uri2 : uris) {
                capDiscModule.getCapabilities(uri2, CapabilityRefreshType.ALWAYS_FORCE_REFRESH, phoneId);
            }
        }
    }

    public FtResumableOption getftResumableOption(CancelReason cancelreason, boolean isGroup, ImDirection direction, int transferMech) {
        if (cancelreason == null) {
            return FtResumableOption.NOTRESUMABLE;
        }
        return isGroup ? getResumableOptionGroupFt(cancelreason, direction, transferMech) : getResumableOptionSingleFt(cancelreason, direction);
    }

    private FtResumableOption getResumableOptionSingleFt(CancelReason cancelreason, ImDirection direction) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionSingleFt, cancelreason: " + cancelreason.getId() + " direction:" + direction.getId());
        FtResumableOption resumeOption = TMOFtCancelReasonResumableOptionCodeMap.translateCancelReason(cancelreason);
        if (resumeOption == null) {
            return FtResumableOption.NOTRESUMABLE;
        }
        return resumeOption;
    }

    private FtResumableOption getResumableOptionGroupFt(CancelReason cancelreason, ImDirection direction, int transferMech) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionGroupFt, cancelreason: " + cancelreason.getId() + " direction:" + direction.getId() + " transferMech:" + transferMech);
        if ((direction == ImDirection.INCOMING && transferMech == 0) || cancelreason == CancelReason.CANCELED_BY_REMOTE) {
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

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long capCacheExpiry) {
        if (refreshType == CapabilityRefreshType.DISABLED) {
            return false;
        }
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability is null");
            return true;
        } else if (isCapCacheExpired(capex, capCacheExpiry)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability cache is expired");
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
            } else if (refreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC) {
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
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex == null || result == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (result == CapabilityConstants.CapExResult.FAILURE) {
            return isCapCacheExpired(capex, cacheInfoExpiry);
        } else {
            return true;
        }
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long serviceAvailabilityInfoExpiry, long capCacheExpiry, long msgCapvalidity) {
        return needRefresh(capex, refreshType, capInfoExpiry, capCacheExpiry);
    }

    private boolean isCapCacheExpired(Capabilities capex, long cacheInfoExpiry) {
        boolean isCapCacheExpired = true;
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "isCapCacheExpired: Capability is null");
            return true;
        }
        Date current = new Date();
        if (current.getTime() - capex.getTimestamp().getTime() < 1000 * cacheInfoExpiry || cacheInfoExpiry <= 0) {
            isCapCacheExpired = false;
        }
        if (isCapCacheExpired) {
            capex.resetFeatures();
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "isCapCacheExpired: " + cacheInfoExpiry + " current " + current.getTime() + " timestamp " + capex.getTimestamp().getTime() + " diff " + (current.getTime() - capex.getTimestamp().getTime()));
        }
        return isCapCacheExpired;
    }

    public int getNextFileTransferAutoResumeTimer(ImDirection direction, int retryCount) {
        if (direction != ImDirection.INCOMING || retryCount >= this.mFtResumeRetryMTTimerList.size()) {
            return -1;
        }
        return this.mFtResumeRetryMTTimerList.get(retryCount).intValue();
    }

    public ImDirection convertToImDirection(String strDirection) {
        ImDirection direction = ImDirection.INCOMING;
        if ("Out".equals(strDirection)) {
            return ImDirection.OUTGOING;
        }
        return direction;
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
            java.lang.String r5 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            return r2
        L_0x0013:
            java.util.Iterator r3 = r11.iterator()
        L_0x0017:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x005a
            java.lang.Object r4 = r3.next()
            com.sec.ims.util.ImsUri r4 = (com.sec.ims.util.ImsUri) r4
            com.sec.ims.options.CapabilityRefreshType r5 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r5 = r0.getCapabilities((com.sec.ims.util.ImsUri) r4, (com.sec.ims.options.CapabilityRefreshType) r5, (int) r1)
            java.lang.String r6 = TAG
            int r7 = r10.mPhoneId
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "isFTViaHttp, uri = "
            r8.append(r9)
            java.lang.String r9 = com.sec.internal.log.IMSLog.numberChecker((com.sec.ims.util.ImsUri) r4)
            r8.append(r9)
            java.lang.String r9 = ", capx = "
            r8.append(r9)
            r8.append(r5)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.i(r6, r7, r8)
            if (r5 == 0) goto L_0x0059
            int r6 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP
            boolean r6 = r5.hasFeature(r6)
            if (r6 != 0) goto L_0x0058
            goto L_0x0059
        L_0x0058:
            goto L_0x0017
        L_0x0059:
            return r2
        L_0x005a:
            r2 = 1
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.TmoStrategy.checkFtHttpCapability(java.util.Set):boolean");
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> participants, ChatData.ChatType chatType) {
        Uri ftHttpCsUri = imConfig.getFtHttpCsUri();
        if (ftHttpCsUri == null || TextUtils.isEmpty(ftHttpCsUri.toString()) || !isFtHttpRegistered()) {
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

    public String getFtHttpUserAgent(ImConfig imConfig) {
        return buildFTHTTPUserAgentForTMOUS() + " 3gpp-gba";
    }

    private String buildFTHTTPUserAgentForTMOUS() {
        return "UP1" + ' ' + Build.VERSION.INCREMENTAL + ' ' + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ' ' + NSDSNamespaces.NSDSSettings.OS + ' ' + Build.VERSION.RELEASE + ' ' + Build.MODEL;
    }

    public boolean isCustomizedFeature(long featureCapability) {
        return ((long) Capabilities.FEATURE_GEO_VIA_SMS) == featureCapability;
    }

    public boolean isResendFTResume(boolean isGroupChat) {
        return isGroupChat;
    }

    public boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return !"RCS_TMB_PHASE2".equals(ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, ""));
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.MSRP_ACTION_NOT_ALLOWED, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.MSRP_ACTION_NOT_ALLOWED, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.OUTOFSERVICE)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (imError.isOneOf(ImError.GROUPCHAT_DISABLED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        } else if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        } else {
            return getStrategyResponse();
        }
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError ftError, ChatData.ChatType chatType) {
        if (ftError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.BUSY_HERE, ImError.REMOTE_PARTY_CANCELED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (ftError.isOneOf(ImError.DEVICE_UNREGISTERED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.NONE);
        } else if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        } else {
            return getStrategyResponse();
        }
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (isPublish) {
            if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_RE_REGISTRATION;
            }
            if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED, PresenceResponse.PresenceFailureReason.UNSUPPORTED_MEDIA_TYPE, PresenceResponse.PresenceFailureReason.BAD_EVENT)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
            }
            if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.ENTITY_TOO_LARGE, PresenceResponse.PresenceFailureReason.TEMPORARILY_UNAVAILABLE, PresenceResponse.PresenceFailureReason.BUSY_HERE, PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE, PresenceResponse.PresenceFailureReason.BUSY_EVERYWHERE, PresenceResponse.PresenceFailureReason.DECLINE)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH_AFTER;
            }
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
        }
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        }
        return PresenceResponse.PresenceStatusCode.NONE;
    }

    public IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError errorReason) {
        if (errorReason.isOneOf(ImError.MSRP_ACTION_NOT_ALLOWED, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.NETWORK_ERROR, ImError.DEVICE_UNREGISTERED, ImError.REMOTE_PARTY_CANCELED, ImError.SESSION_TIMED_OUT, ImError.SERVICE_UNAVAILABLE, ImError.NORMAL_RELEASE)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return getStrategyResponse();
    }

    public IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason close) {
        if (isSlmEnabled()) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int stateId) {
        return chatType == ChatData.ChatType.REGULAR_GROUP_CHAT;
    }

    public boolean checkSlmFileType(String contentType) {
        return !TextUtils.isEmpty(contentType) && (contentType.contains(CallConstants.ComposerData.IMAGE) || contentType.contains(TMOConstants.CallLogTypes.VIDEO) || contentType.contains(TMOConstants.CallLogTypes.AUDIO) || "text/x-vCard".equalsIgnoreCase(contentType) || "text/vcard".equalsIgnoreCase(contentType) || "text/x-vCalendar".equalsIgnoreCase(contentType) || "text/x-vNote".equalsIgnoreCase(contentType) || "text/x-vtodo".equalsIgnoreCase(contentType) || "application/ogg".equalsIgnoreCase(contentType));
    }

    public long getThrottledDelay(long delay) {
        return 3 + delay;
    }

    public IMnoStrategy.StrategyResponse getUploadedFileFallbackSLMTech() {
        return isSlmEnabled() ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_SLM_FILE) : new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public static class TMOFtCancelReasonResumableOptionCodeMap {
        private static final MappingTranslator<CancelReason, FtResumableOption> mTMOFtResumableOptionTranslator = new MappingTranslator.Builder().map(CancelReason.UNKNOWN, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CANCELED_BY_USER, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CANCELED_BY_REMOTE, FtResumableOption.NOTRESUMABLE).map(CancelReason.CANCELED_BY_SYSTEM, FtResumableOption.NOTRESUMABLE).map(CancelReason.REJECTED_BY_REMOTE, FtResumableOption.NOTRESUMABLE).map(CancelReason.TIME_OUT, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.ERROR, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.CONNECTION_RELEASED, FtResumableOption.MANUALLY_RESUMABLE_ONLY).map(CancelReason.DEVICE_UNREGISTERED, FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE).map(CancelReason.NOT_AUTHORIZED, FtResumableOption.NOTRESUMABLE).map(CancelReason.FORBIDDEN_NO_RETRY_FALLBACK, FtResumableOption.NOTRESUMABLE).map(CancelReason.MSRP_SESSION_ERROR_NO_RESUME, FtResumableOption.NOTRESUMABLE).buildTranslator();

        public static FtResumableOption translateCancelReason(CancelReason value) {
            if (mTMOFtResumableOptionTranslator.isTranslationDefined(value)) {
                return mTMOFtResumableOptionTranslator.translate(value);
            }
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
    }

    public ImSessionClosedReason handleSessionFailure(ImError error) {
        if (error == ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED) {
            return ImSessionClosedReason.KICKED_OUT_BY_LEADER;
        }
        return ImSessionClosedReason.NONE;
    }

    public boolean isDisplayBotError() {
        return true;
    }

    public boolean isDisplayWarnText() {
        return true;
    }
}
