package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;

public class UsccStrategy extends DefaultRCSMnoStrategy {
    public UsccStrategy(Context context, int phoneId) {
        super(context, phoneId);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason errorReason, boolean isPublish) {
        if (errorReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.TEMPORARILY_UNAVAILABLE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NOT_FOUND;
        }
        return PresenceResponse.PresenceStatusCode.NONE;
    }
}
