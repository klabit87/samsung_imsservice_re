package com.sec.internal.constants.ims.servicemodules.volte2;

import com.sec.ims.util.SipError;

public class UssdEvent {
    private int mDCS = -1;
    private byte[] mData = null;
    private int mSessionID = -1;
    private SipError mSipErrorCode = null;
    private USSD_STATE mState = USSD_STATE.NOT_INITIALIZED;
    private int mStatus = -1;

    public enum USSD_STATE {
        NOT_INITIALIZED,
        USSD_INDICATION,
        USSD_RESPONSE,
        USSD_ERROR
    }

    public void setSessionID(int sessionID) {
        this.mSessionID = sessionID;
    }

    public int getSessionID() {
        return this.mSessionID;
    }

    public void setState(USSD_STATE state) {
        this.mState = state;
    }

    public USSD_STATE getState() {
        return this.mState;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public void setDCS(int dcs) {
        this.mDCS = dcs;
    }

    public int getDCS() {
        return this.mDCS;
    }

    private void setData(byte[] data) {
        this.mData = data;
    }

    public void setData(Object obj) {
        if (obj instanceof String) {
            setData(((String) obj).getBytes());
        } else if (obj instanceof byte[]) {
            setData((byte[]) obj);
        } else if (obj instanceof Integer) {
            setData(((Integer) obj).toString().getBytes());
        }
    }

    public byte[] getData() {
        return this.mData;
    }

    public void setErrorCode(SipError errorCode) {
        this.mSipErrorCode = errorCode;
    }

    public SipError getErrorCode() {
        return this.mSipErrorCode;
    }
}
