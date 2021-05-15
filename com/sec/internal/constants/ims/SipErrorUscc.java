package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class SipErrorUscc extends SipErrorBase {
    public static final SipError BUSY_ESTABLISHING_ANOTHER_CALL = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Establishing Another Call");
    public static final SipError CALL_REJECTED_BY_USER = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Call Rejected by User");

    public SipError getFromRejectReason(int reason) {
        if (reason < 0) {
            reason = 3;
        }
        if (reason != 3) {
            return super.getFromRejectReason(reason);
        }
        return CALL_REJECTED_BY_USER;
    }
}
