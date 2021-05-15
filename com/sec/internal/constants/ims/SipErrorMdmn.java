package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;

public class SipErrorMdmn extends SipErrorBase {
    public SipErrorMdmn() {
        this.mDefaultRejectReason = DECLINE;
    }

    public SipError getFromRejectReason(int reason) {
        if (reason == 3) {
            return DECLINE;
        }
        if (reason == 13) {
            return NOT_ACCEPTABLE_GLOBALLY;
        }
        if (reason != 15) {
            return super.getFromRejectReason(reason);
        }
        return E911_NOT_ALLOWED_ON_SD;
    }
}
