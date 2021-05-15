package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorNovaIs extends SipErrorBase {
    public SipError getFromRejectReason(int reason) {
        if (reason != 13) {
            return super.getFromRejectReason(reason);
        }
        return REQUEST_TIMEOUT;
    }
}
