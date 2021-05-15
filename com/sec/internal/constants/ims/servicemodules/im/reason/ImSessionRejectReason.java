package com.sec.internal.constants.ims.servicemodules.im.reason;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public enum ImSessionRejectReason {
    VOLUNTARILY,
    INVOLUNTARILY(503, -1, "Service Unavailable, User Dreged"),
    DEDICATED_BEARER_UNAVAILABLE_TIMEOUT,
    GC_FORCE_CLOSE,
    TEMPORARILY_UNAVAILABLE(NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE, -1, "Temporarily Unavailable"),
    BUSY_HERE(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, -1, "Busy Here"),
    FORBIDDEN(403, 127, "Service not authorised"),
    CHATBOT_PROFILE_RETRIEVAL_FAIL(400, -1, "Bad request"),
    NOT_ACCEPTABLE_HERE(488, -1, "Not Acceptable Here"),
    TOO_MANY_PARTICIPANTS,
    ISFOCUS_ALREADY_ASSIGNED,
    ANONYMITY_NOT_ALLOWED,
    FUNCTION_NOT_ALLOWED,
    SESSION_DOES_NOT_EXIST(481, -1, ""),
    NO_MESSAGES,
    SERVICE_NOT_AUTHORISED,
    NO_DESTINATIONS,
    VERSION_NOT_SUPPORTED,
    SIZE_EXCEEDED;
    
    private final int mSipCode;
    private final int mWarningCode;
    private final String mWarningText;

    private ImSessionRejectReason(int sipCode, int warningCode, String warningText) {
        this.mSipCode = sipCode;
        this.mWarningCode = warningCode;
        this.mWarningText = warningText;
    }

    public int getSipCode() {
        return this.mSipCode;
    }

    public int getWarningCode() {
        return this.mWarningCode;
    }

    public String getWarningText() {
        return this.mWarningText;
    }
}
