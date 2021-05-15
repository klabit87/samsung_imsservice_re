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

public class EmeiaUPStrategy extends DefaultUPMnoStrategy {
    private static final String TAG = EmeiaUPStrategy.class.getSimpleName();

    public EmeiaUPStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> participants, ChatData.ChatType chatType) {
        return imConfig.getFtDefaultMech() == ImConstants.FtMech.HTTP && isFtHttpRegistered() && (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) || checkFtHttpCapability(participants) || checkUserAvailableOffline(participants));
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult result, Capabilities capex, long features, long cacheInfoExpiry) {
        if (capex == null || result == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (result == CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR || result == CapabilityConstants.CapExResult.FORBIDDEN_403) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: do not change anything");
            return false;
        } else if (result != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public long updateAvailableFeatures(Capabilities capex, long features, CapabilityConstants.CapExResult result) {
        if (capex == null) {
            IMSLog.i(TAG, this.mPhoneId, "updateAvailableFeatures: capex is null.");
            return features;
        }
        if (result == CapabilityConstants.CapExResult.USER_UNAVAILABLE) {
            if (capex.isAvailable()) {
                features = (long) Capabilities.FEATURE_OFFLINE_RCS_USER;
            } else {
                features = (long) Capabilities.FEATURE_NON_RCS_USER;
            }
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateAvailableFeatures:" + capex + ", mAvailableFeatures " + features);
        return features;
    }
}
