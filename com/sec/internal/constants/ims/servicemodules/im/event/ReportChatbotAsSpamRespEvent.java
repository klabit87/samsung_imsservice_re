package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class ReportChatbotAsSpamRespEvent {
    public ImError mError;
    public String mRequestId;
    public String mUri;

    public ReportChatbotAsSpamRespEvent(String uri, String requestId, ImError error) {
        this.mUri = uri;
        this.mError = error;
        this.mRequestId = requestId;
    }

    public String toString() {
        return "ReportChatbotAsSpamRespEvent, mError = " + this.mError + ", mRequestId = " + this.mRequestId + "]";
    }
}
