package com.sec.internal.ims.servicemodules.volte2.data;

public class DialogSubscribeStatus {
    private int mPhoneId;
    private String mReasonPhrase;
    private int mStatusCode;

    public DialogSubscribeStatus(int phoneId, int statusCode, String reasonPhrase) {
        this.mPhoneId = phoneId;
        this.mStatusCode = statusCode;
        this.mReasonPhrase = reasonPhrase;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public String toString() {
        return "Phone#" + this.mPhoneId + " statusCode : " + this.mStatusCode + " reasonPhrase : " + this.mReasonPhrase;
    }
}
