package com.sec.internal.constants.ims;

public class SipReason {
    private int mCause;
    private String[] mExtension;
    private boolean mIsLocalRelease;
    private String mProtocol;
    private String mText;

    public SipReason() {
        this.mIsLocalRelease = false;
    }

    public SipReason(String protocol, int cause, String text, String... extensions) {
        this.mProtocol = protocol;
        this.mCause = cause;
        this.mText = text;
        this.mExtension = extensions;
        this.mIsLocalRelease = false;
    }

    public SipReason(String protocol, int cause, String text, boolean isLocalRelease, String... extensions) {
        this.mProtocol = protocol;
        this.mCause = cause;
        this.mText = text;
        this.mExtension = extensions;
        this.mIsLocalRelease = isLocalRelease;
    }

    public String getProtocol() {
        return this.mProtocol;
    }

    public int getCause() {
        return this.mCause;
    }

    public String getText() {
        return this.mText;
    }

    public String[] getExtensions() {
        return this.mExtension;
    }

    public boolean isLocalRelease() {
        return this.mIsLocalRelease;
    }

    public void setLocalRelease(boolean isLocalRelease) {
        this.mIsLocalRelease = isLocalRelease;
    }

    public SipReason getFromUserReason(int reason) {
        return null;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Reason: ");
        buffer.append(this.mProtocol);
        buffer.append(";");
        buffer.append("cause=");
        buffer.append(this.mCause);
        buffer.append(";");
        buffer.append("text=");
        buffer.append(this.mText);
        buffer.append(";");
        String[] strArr = this.mExtension;
        if (strArr != null) {
            for (String ext : strArr) {
                buffer.append(ext);
            }
        }
        return buffer.toString();
    }
}
