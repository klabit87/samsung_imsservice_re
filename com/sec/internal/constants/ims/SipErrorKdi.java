package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class SipErrorKdi extends SipErrorBase {
    public static final SipError MULTIPARTY_CALL_IS_ESTABLISHED = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "Already On Two Calls");

    public SipErrorKdi() {
        this.mDefaultRejectReason = DECLINE;
    }

    public SipError getFromRejectReason(int reason) {
        if (reason == 3) {
            return DECLINE;
        }
        if (reason != 13) {
            return super.getFromRejectReason(reason);
        }
        return SERVER_INTERNAL_ERROR;
    }
}
