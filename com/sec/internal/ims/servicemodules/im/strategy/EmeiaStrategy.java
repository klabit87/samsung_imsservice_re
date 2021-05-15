package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class EmeiaStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = EmeiaStrategy.class.getSimpleName();

    public EmeiaStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> participants, ChatData.ChatType chatType) {
        return imConfig.getFtDefaultMech() == ImConstants.FtMech.HTTP && isFtHttpRegistered() && (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) || checkFtHttpCapability(participants) || checkUserAvailableOffline(participants));
    }

    public long updateAvailableFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateAvailableFeatures:" + capex);
        if (capex == null) {
            return features;
        }
        if (result == CapabilityConstants.CapExResult.USER_UNAVAILABLE) {
            features = (long) (capex.isAvailable() ? Capabilities.FEATURE_OFFLINE_RCS_USER : Capabilities.FEATURE_NON_RCS_USER);
        }
        String str2 = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "updateAvailableFeatures: mAvailableFeatures " + features);
        return features;
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex == null || result == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        }
        if (result.isOneOf(CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR, CapabilityConstants.CapExResult.FORBIDDEN_403)) {
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
}
