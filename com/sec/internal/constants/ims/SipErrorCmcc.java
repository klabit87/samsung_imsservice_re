package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorCmcc extends SipErrorBase {
    public SipErrorCmcc() {
        this.mDefaultRejectReason = DECLINE;
    }

    public SipError getFromRejectReason(int reason) {
        if (reason == 3) {
            return DECLINE;
        }
        if (reason != 13) {
            return super.getFromRejectReason(reason);
        }
        return BUSY_HERE;
    }
}
