package com.sec.internal.constants.ims.servicemodules.im.params;

import com.sec.ims.util.ImsUri;

public class SendReportMsgParams {
    private String mSpamDate;
    private ImsUri mSpamFrom;
    private int mSpamMsgId;
    private ImsUri mSpamTo;

    public SendReportMsgParams(ImsUri spamFrom, ImsUri spamTo, String spamDate, int spamMsgId) {
        this.mSpamFrom = spamFrom;
        this.mSpamTo = spamTo;
        this.mSpamDate = spamDate;
        this.mSpamMsgId = spamMsgId;
    }

    public ImsUri getSpamFrom() {
        return this.mSpamFrom;
    }

    public ImsUri getSpamTo() {
        return this.mSpamTo;
    }

    public String getSpamDate() {
        return this.mSpamDate;
    }

    public int getSpamMsgId() {
        return this.mSpamMsgId;
    }

    public String toString() {
        return "SendReportMsgParams [mSpamFrom=" + this.mSpamFrom + ", mSpamTo=" + this.mSpamTo + ", mSpamDate=" + this.mSpamDate + ", mSpamMsgId=" + this.mSpamMsgId + "]";
    }
}
