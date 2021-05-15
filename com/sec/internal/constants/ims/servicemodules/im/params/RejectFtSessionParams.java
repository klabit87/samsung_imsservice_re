package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;

public class RejectFtSessionParams {
    public Message mCallback;
    public String mFileTransferId;
    public String mImdnMessageId;
    public boolean mIsSlmSvcMsg;
    public Object mRawHandle;
    public FtRejectReason mRejectReason;

    public RejectFtSessionParams(Object rawHandle, Message callback, FtRejectReason rejectReason, String fileTransferId) {
        this.mRawHandle = rawHandle;
        this.mCallback = callback;
        this.mRejectReason = rejectReason;
        this.mFileTransferId = fileTransferId;
    }

    public RejectFtSessionParams(Object rawHandle, Message callback, FtRejectReason rejectReason, String fileTransferId, String imdnId) {
        this(rawHandle, callback, rejectReason, fileTransferId);
        this.mImdnMessageId = imdnId;
    }

    public RejectFtSessionParams(Object rawHandle, Message callback, FtRejectReason rejectReason, String fileTransferId, boolean isSlmSvcMsg) {
        this(rawHandle, callback, rejectReason, fileTransferId);
        this.mIsSlmSvcMsg = isSlmSvcMsg;
    }

    public String toString() {
        return "RejectFtSessionParams [mRawHandle=" + this.mRawHandle + ", mCallback=" + this.mCallback + ", mRejectReason=" + this.mRejectReason + ", mFileTransferId=" + this.mFileTransferId + "]";
    }
}
