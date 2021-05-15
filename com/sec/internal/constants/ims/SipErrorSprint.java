package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorSprint extends SipErrorBase {
    public SipError getFromRejectReason(int reason) {
        if (reason != 7) {
            return super.getFromRejectReason(reason);
        }
        return NOT_ACCEPTABLE_HERE;
    }
}
