package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorGlobe extends SipErrorBase {
    public SipErrorGlobe() {
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
