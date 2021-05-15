package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public final class BmcStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = BmcStrategy.class.getSimpleName();

    public BmcStrategy(Context context, int phoneId) {
        super(context, phoneId);
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.BmcStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean isCapabilityValidUri(ImsUri uri) {
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
        IMSLog.s(str, "forceRefreshCapability: uris " + uris);
        if (remoteOnline) {
            for (ImsUri uri : uris) {
                capDiscModule.getCapabilities(uri.getMsisdn(), (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_CPM), phoneId);
            }
        }
    }

    public boolean needRefresh(Capabilities capex, CapabilityRefreshType refreshType, long capInfoExpiry, long serviceAvailabilityInfoExpiry, long capCacheExpiry, long msgCapvalidity) {
        return needRefresh(capex, refreshType, capInfoExpiry, capCacheExpiry);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex == null || result == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
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

    public boolean isPresenceReadyToRequest(boolean ownInfoPublished, boolean paralysed) {
        return ownInfoPublished && !paralysed;
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.MSRP_ACTION_NOT_ALLOWED, ImError.REMOTE_PARTY_DECLINED);
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

    public long getThrottledDelay(long delay) {
        return 3;
    }
}
