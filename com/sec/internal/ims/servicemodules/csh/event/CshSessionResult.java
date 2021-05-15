package com.sec.internal.ims.servicemodules.csh.event;

public class CshSessionResult {
    public CshErrorReason mReason;
    public int mSessionNumber;

    public CshSessionResult(int number, CshErrorReason reason) {
        this.mSessionNumber = number;
        this.mReason = reason;
    }
}
