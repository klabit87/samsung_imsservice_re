package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class FtTransferProgressEvent {
    public int mId = -1;
    public Object mRawHandle;
    public Result mReason;
    public State mState;
    public long mTotal;
    public long mTransferred;

    public enum State {
        TRANSFERRING,
        INTERRUPTED,
        CANCELED,
        COMPLETED
    }

    public FtTransferProgressEvent(Object rawHandle, int id, long total, long transferred, State state, Result reason) {
        this.mRawHandle = rawHandle;
        this.mId = id;
        this.mTotal = total;
        this.mTransferred = transferred;
        this.mState = state;
        this.mReason = reason;
    }

    public String toString() {
        return "FtTransferProgressEvent [mRawHandle=" + this.mRawHandle + ", mId=" + this.mId + ", mTotal=" + this.mTotal + ", mTransferred=" + this.mTransferred + ", mState=" + this.mState + ", mReason=" + this.mReason + "]";
    }
}
