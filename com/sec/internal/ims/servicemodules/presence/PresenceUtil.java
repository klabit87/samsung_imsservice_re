package com.sec.internal.ims.servicemodules.presence;

import android.content.Context;
import android.content.Intent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.ims.diagnosis.RcsHqmAgent;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PresenceUtil {
    private static final String LOG_TAG = "PresenceUtil";

    PresenceUtil() {
    }

    static CapabilityConstants.CapExResult translateToCapExResult(PresenceInfo info, ImsUri uri, long features, PresenceResponse.PresenceStatusCode statusCode) {
        PresenceSubscription s = PresenceSubscriptionController.getSubscription(uri, true, info.getPhoneId());
        if (info.isFetchSuccess()) {
            if (s == null) {
                return CapabilityConstants.CapExResult.POLLING_SUCCESS;
            }
            s.updateState(4);
            return CapabilityConstants.CapExResult.SUCCESS;
        } else if (statusCode == PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED) {
            return CapabilityConstants.CapExResult.USER_NOT_REGISTERED;
        } else {
            if (s != null && CapabilityUtil.hasFeature(features, (long) Capabilities.FEATURE_NON_RCS_USER)) {
                return CapabilityConstants.CapExResult.USER_NOT_FOUND;
            }
            if (s == null || !CapabilityUtil.hasFeature(features, Capabilities.FEATURE_CHATBOT_ROLE)) {
                return CapabilityConstants.CapExResult.FAILURE;
            }
            return CapabilityConstants.CapExResult.USER_NOT_FOUND;
        }
    }

    static ImsUri convertUriType(ImsUri uri, boolean useSipOnly, PresenceInfo info, Mno mno, UriGenerator uriGenerator, int phoneId) {
        ImsUri uriToRequest;
        if (info == null) {
            uriToRequest = uri;
        } else if (mno.isKor()) {
            uriToRequest = info.getTelUri() != null ? ImsUri.parse(info.getTelUri()) : uri;
        } else {
            uriToRequest = info.getUri() != null ? ImsUri.parse(info.getUri()) : uri;
        }
        if (useSipOnly) {
            uriToRequest = uriGenerator.getNetworkPreferredUri(uri);
        }
        IMSLog.s(LOG_TAG, phoneId, "convertUriType: requested uri = " + uriToRequest);
        return uriToRequest;
    }

    static int getPollListSubExp(Context context, int phoneId) {
        return DmConfigHelper.readInt(context, ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP, 30, phoneId).intValue();
    }

    static void triggerOmadmTreeSync(Context context, int phoneId) {
        IMSLog.s(LOG_TAG, phoneId, "triggerOmadmTreeSync:");
        Intent intent = new Intent("com.samsung.sdm.START_DM_SYNC_SESSION");
        intent.setPackage(ImsConstants.Packages.PACKAGE_SDM);
        context.sendBroadcast(intent);
    }

    static boolean isRegProhibited(ImsRegistration regInfo, int phoneId) {
        ImsRegistration reg;
        if (regInfo == null || (reg = ImsRegistry.getRegistrationManager().getRegistrationList().get(Integer.valueOf(regInfo.getImsProfile().getId()))) == null) {
            return false;
        }
        regInfo.setProhibited(reg.isProhibited());
        IMSLog.s(LOG_TAG, phoneId, "isRegProhibited: " + reg.isProhibited());
        return reg.isProhibited();
    }

    static long getPublishExpBackOffRetryTime(int phoneId, int retryCount) {
        if (RcsPolicyManager.getRcsStrategy(phoneId).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP)) {
            return calPublishExponentialBackOffRetryTime(phoneId, retryCount);
        }
        if (RcsPolicyManager.getRcsStrategy(phoneId).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.SEC_UP, RcsPolicySettings.RcsPolicyType.KT_UP)) {
            return calPublishExpBackOffRetryTimeUnlimit(phoneId, retryCount);
        }
        return 0;
    }

    static long getSubscribeExpBackOffRetryTime(int phoneId, int retryCount) {
        if (RcsPolicyManager.getRcsStrategy(phoneId).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP)) {
            return calSubscribeExponentialBackOffRetryTime(phoneId, retryCount);
        }
        return 0;
    }

    static long getListSubscribeExpBackOffRetryTime(int phoneId, int retryCount) {
        if (RcsPolicyManager.getRcsStrategy(phoneId).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP)) {
            return calListSubscribeExponentialBackOffRetryTime(phoneId, retryCount);
        }
        return 0;
    }

    static boolean getExtendedPublishTimerCond(int phoneId, List<ServiceTuple> serviceTuples) {
        if (!RcsPolicyManager.getRcsStrategy(phoneId).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP) || !isExtendedPublishTimerCond(phoneId, serviceTuples)) {
            return false;
        }
        return true;
    }

    private static long calPublishExponentialBackOffRetryTime(int phoneId, int retryCount) {
        long[] ExpBackOffRetrySlots_1Min = {60, 120, 240, 480};
        if (retryCount <= 4 && retryCount > 0) {
            return ExpBackOffRetrySlots_1Min[retryCount - 1];
        }
        IMSLog.s(LOG_TAG, phoneId, "calPublishExponentialBackOffRetryTime: invaild retryCount: " + retryCount);
        return 0;
    }

    public static long calPublishExpBackOffRetryTimeUnlimit(int phoneId, int retryCount) {
        long[] ExpBackOffRetrySlots_2Min = {120, 240, 480, 960, 3600};
        if (retryCount <= 0) {
            IMSLog.s(LOG_TAG, phoneId, "calPublishExponentialBackOffRetryTime: invaild retryCount: " + retryCount);
            return 0;
        } else if (retryCount > ExpBackOffRetrySlots_2Min.length) {
            return ExpBackOffRetrySlots_2Min[ExpBackOffRetrySlots_2Min.length - 1];
        } else {
            return ExpBackOffRetrySlots_2Min[retryCount - 1];
        }
    }

    private static long calSubscribeExponentialBackOffRetryTime(int phoneId, int retryCount) {
        long[] ExpBackOffRetrySlots_1Min = {60, 120, 240, 480};
        if (retryCount <= 4 && retryCount > 0) {
            return ExpBackOffRetrySlots_1Min[retryCount - 1];
        }
        IMSLog.s(LOG_TAG, phoneId, "calSubscribeExponentialBackOffRetryTime: invaild retryCount: " + retryCount);
        return 0;
    }

    private static long calListSubscribeExponentialBackOffRetryTime(int phoneId, int retryCount) {
        long[] ExpBackOffRetrySlots_30Mins = {1800, 3600, 7200, 14400, 28800};
        if (retryCount <= 5 && retryCount > 0) {
            return ExpBackOffRetrySlots_30Mins[retryCount - 1];
        }
        IMSLog.s(LOG_TAG, phoneId, "calListSubscribeExponentialBackOffRetryTime: invaild retryCount: " + retryCount);
        return 0;
    }

    private static boolean isExtendedPublishTimerCond(int phoneId, List<ServiceTuple> serviceTuples) {
        long featureSet = ServiceTuple.getFeatures(serviceTuples);
        IMSLog.i(LOG_TAG, phoneId, "isExtendedPublishTimerCond: services: " + serviceTuples);
        return (((long) Capabilities.FEATURE_MMTEL_VIDEO) & featureSet) == 0 && (((long) Capabilities.FEATURE_CHAT_CPM) & featureSet) == 0 && (((long) Capabilities.FEATURE_FT) & featureSet) == 0;
    }

    static boolean sendRCSPPubInfoToHQM(Context context, int publishErrorCode, String publishErrorReason, int phoneId) {
        if (phoneId < 0) {
            phoneId = 0;
        }
        Map<String, String> rcsmKeys = new LinkedHashMap<>();
        rcsmKeys.put("ERRC", String.valueOf(publishErrorCode));
        rcsmKeys.put(DiagnosisConstants.RCSP_KEY_ERES, publishErrorReason);
        return RcsHqmAgent.sendRCSInfoToHQM(context, DiagnosisConstants.FEATURE_RCSP, phoneId, rcsmKeys);
    }

    static boolean sendRCSPSubInfoToHQM(Context context, int subscribeErrorCode, int phoneId) {
        if (subscribeErrorCode == 403 || subscribeErrorCode == 404) {
            return false;
        }
        if (phoneId < 0) {
            phoneId = 0;
        }
        Map<String, String> rcsmKeys = new LinkedHashMap<>();
        rcsmKeys.put(DiagnosisConstants.RCSP_KEY_SERR, String.valueOf(subscribeErrorCode));
        return RcsHqmAgent.sendRCSInfoToHQM(context, DiagnosisConstants.FEATURE_RCSP, phoneId, rcsmKeys);
    }
}
