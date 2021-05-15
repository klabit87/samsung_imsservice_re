package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorKor extends SipErrorBase {
    public static final SipError AKA_CHANLENGE_TIMEOUT = new SipError(1003, "Aka challenge timeout");

    public SipErrorKor() {
        this.mDefaultRejectReason = DECLINE;
    }

    public SipError getFromRejectReason(int reason) {
        if (reason == 3) {
            return DECLINE;
        }
        if (reason != 13) {
            return super.getFromRejectReason(reason);
        }
        return NOT_ACCEPTABLE_GLOBALLY;
    }
}
