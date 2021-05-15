package com.sec.internal.constants.ims;

public class SipReasonVzw extends SipReason {
    public static final SipReason INTER_RAT = new SipReason("SIP", 0, "Moved to eHRPD", new String[0]);
    public static final SipReason NEW_DIALOG_ESTABLISHED = new SipReason("SIP", 0, "New Dialog Established", new String[0]);
    public static final SipReason SESSION_EXPIRED = new SipReason("SIP", 0, "Session Expired", new String[0]);
    public static final SipReason USER_TRIGGERED = new SipReason("SIP", 0, "User Triggered", new String[0]);

    public SipReason getFromUserReason(int reason) {
        if (reason < 0) {
            reason = 5;
        }
        if (reason == 4) {
            return INTER_RAT;
        }
        if (reason != 5) {
            return super.getFromUserReason(reason);
        }
        return USER_TRIGGERED;
    }
}
