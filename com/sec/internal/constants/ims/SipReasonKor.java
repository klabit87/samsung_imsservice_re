package com.sec.internal.constants.ims;

import com.sec.internal.helper.header.WwwAuthenticateHeader;

public class SipReasonKor extends SipReason {
    public static final SipReason INTER_RAT = new SipReason("eHPRD", 105, "Inter-RAT", "fc=9558");
    public static final SipReason LOW_BATTERY = new SipReason("Power", 106, "Low Battery", "fc=9701");
    public static final SipReason OUT_OF_BATTERY = new SipReason("Power", 107, "Out of battery", "fc=9999");
    public static final SipReason SESSION_EXPIRED = new SipReason("SIP", 103, "Session-Expire", "fc=9602");
    public static final SipReason UNKNOWN = new SipReason("ETC", 104, WwwAuthenticateHeader.HEADER_PARAM_UNKNOWN_SCHEME, "fc=9999");
    public static final SipReason USER_TRIGGERED = new SipReason("USER", 101, "User triggered", "fc=9501");

    public SipReason getFromUserReason(int reason) {
        if (reason < 0) {
            return USER_TRIGGERED;
        }
        if (reason == 4) {
            return INTER_RAT;
        }
        if (reason == 5) {
            return USER_TRIGGERED;
        }
        if (reason == 6) {
            return LOW_BATTERY;
        }
        if (reason == 10) {
            return OUT_OF_BATTERY;
        }
        if (reason != 17) {
            return UNKNOWN;
        }
        return SESSION_EXPIRED;
    }
}
