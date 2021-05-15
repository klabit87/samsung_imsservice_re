package com.sec.internal.constants.ims;

public class SipReasonOptus extends SipReason {
    public static final SipReason USER_TRIGGERED = new SipReason("SIP", 0, "User Triggered", new String[0]);

    public SipReason getFromUserReason(int reason) {
        if (reason < 0) {
            reason = 5;
        }
        if (reason != 5) {
            return super.getFromUserReason(reason);
        }
        return USER_TRIGGERED;
    }
}
