package com.sec.internal.constants.ims;

import com.sec.ims.util.SipError;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;

public class SipErrorTmoUs extends SipErrorBase {
    public static final SipError VERSION_NOT_SUPPORTED = new SipError(Id.REQUEST_IM_SEND_COMPOSING_STATUS, "SIP Version Not Supported");

    public boolean requireVoLteCsfb() {
        if (equals(ALTERNATIVE_SERVICE) || equals(BAD_REQUEST) || equals(UNAUTHORIZED) || equals(FORBIDDEN) || equals(METHOD_NOT_ALLOWED) || equals(NOT_ACCEPTABLE)) {
            return true;
        }
        return false;
    }

    public boolean requireSmsCsfb() {
        return false;
    }
}
