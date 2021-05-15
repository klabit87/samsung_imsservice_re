package com.sec.internal.constants.ims;

public class SipReasonTmoUs extends SipReason {
    public static final SipReason DEDICATED_BEARER_LOST = new SipReason("RELEASE_CAUSE", 3, "Media bearer loss", new String[0]);
    public static final SipReason DEDICATED_BEARER_NOT_ESTABLISHED = new SipReason("RELEASE_CAUSE", 3, "User ends call Media bearer loss", new String[0]);
    public static final SipReason INVITE_TIMEOUT = new SipReason("RELEASE_CAUSE", 5, "User ends call and SIP response time-out", new String[0]);
    public static final SipReason USER_TRIGGERED = new SipReason("RELEASE_CAUSE", 1, "User ends call", new String[0]);

    public SipReason getFromUserReason(int reason) {
        if (reason < 0) {
            reason = 5;
        }
        if (reason == 5) {
            return USER_TRIGGERED;
        }
        if (reason == 11) {
            return DEDICATED_BEARER_LOST;
        }
        if (reason == 28) {
            return INVITE_TIMEOUT;
        }
        if (reason != 29) {
            return super.getFromUserReason(reason);
        }
        return DEDICATED_BEARER_NOT_ESTABLISHED;
    }
}
