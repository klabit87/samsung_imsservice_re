package com.sec.internal.constants.ims;

public class SipReasonBmc extends SipReason {
    public static final SipReason NWAY_CONFERENCE = new SipReason("SIP", 0, "Conference Fail", new String[0]);
    public static final SipReason USER_TRIGGERED = new SipReason("SIP", 0, "User Triggered", new String[0]);

    public SipReason getFromUserReason(int reason) {
        if (reason < 0) {
            reason = 5;
        }
        if (reason == 5) {
            return USER_TRIGGERED;
        }
        if (reason != 7) {
            return super.getFromUserReason(reason);
        }
        return NWAY_CONFERENCE;
    }
}
