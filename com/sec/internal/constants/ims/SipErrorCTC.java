package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class SipErrorCTC extends SipErrorBase {
    public static final SipError CALL_REJECTED_BY_USER = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Call Rejected by User");

    public SipError getFromRejectReason(int reason) {
        if (reason == 3) {
            return CALL_REJECTED_BY_USER;
        }
        if (reason != 13) {
            return super.getFromRejectReason(reason);
        }
        return REQUEST_TIMEOUT;
    }
}
