package com.sec.internal.constants.ims.servicemodules.im.params;

import com.sec.ims.util.ImsUri;

public class ReportChatbotAsSpamParams {
    public ImsUri mChatbotUri;
    public int mPhoneId;
    public String mRequestId;
    public String mSpamInfo;

    public ReportChatbotAsSpamParams(int phoneId, String requestId, ImsUri uri, String spamInfo) {
        this.mChatbotUri = uri;
        this.mSpamInfo = spamInfo;
        this.mPhoneId = phoneId;
        this.mRequestId = requestId;
    }

    public String toString() {
        return "ReportChatbotAsSpamParams [ spamInfo = " + this.mSpamInfo + ", PhoneId = " + this.mPhoneId + ", RequestId = " + this.mRequestId + "]";
    }
}
