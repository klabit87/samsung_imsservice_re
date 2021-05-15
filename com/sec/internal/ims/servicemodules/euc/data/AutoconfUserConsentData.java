package com.sec.internal.ims.servicemodules.euc.data;

public class AutoconfUserConsentData {
    private final String mConsentMsgMessage;
    private final String mConsentMsgTitle;
    private final String mOwnIdentity;
    private final long mTimestamp;
    private final boolean mUserAccept;

    public AutoconfUserConsentData(long timestamp, boolean userAccept, String consentMsgTitle, String consentMsgMessage, String ownIdentity) {
        this.mTimestamp = timestamp;
        this.mUserAccept = userAccept;
        this.mConsentMsgTitle = consentMsgTitle;
        this.mConsentMsgMessage = consentMsgMessage;
        this.mOwnIdentity = ownIdentity;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    public boolean isUserAccept() {
        return this.mUserAccept;
    }

    public String getConsentMsgTitle() {
        return this.mConsentMsgTitle;
    }

    public String getConsentMsgMessage() {
        return this.mConsentMsgMessage;
    }

    public String getOwnIdentity() {
        return this.mOwnIdentity;
    }

    public String toString() {
        return getClass().getSimpleName() + ", mTimestamp=" + this.mTimestamp + ", mUserAccept=" + this.mUserAccept + ", mConsentMsgTitle=" + this.mConsentMsgTitle + ", mConsentMsgSubject=" + this.mConsentMsgMessage + ", mOwnIdentity=" + this.mOwnIdentity + "]";
    }
}
