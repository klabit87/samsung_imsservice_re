package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorSbm extends SipErrorBase {
    public SipErrorSbm() {
        this.mDefaultRejectReason = DECLINE;
    }

    public SipError getFromRejectReason(int reason) {
        if (reason < 0) {
            return DECLINE;
        }
        if (reason == 3 || reason == 12 || reason == 14) {
            return FORBIDDEN;
        }
        return super.getFromRejectReason(reason);
    }
}
