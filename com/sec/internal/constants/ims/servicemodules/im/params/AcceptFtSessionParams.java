package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;

public class AcceptFtSessionParams {
    public Message mCallback;
    public long mEnd;
    public String mFilePath;
    public int mMessageId;
    public Object mRawHandle;
    public long mStart;
    public String mUserAlias;

    public AcceptFtSessionParams(int messageId, Object handle, String filePath, String userAlias, Message callback, long start, long end) {
        this.mMessageId = messageId;
        this.mRawHandle = handle;
        this.mFilePath = filePath;
        this.mUserAlias = userAlias;
        this.mCallback = callback;
        this.mStart = start;
        this.mEnd = end;
    }

    public String toString() {
        return "AcceptFtSessionParams [mMessageId=" + this.mMessageId + ", mRawHandle=" + this.mRawHandle + ", mStart=" + this.mStart + ", mEnd=" + this.mEnd + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mCallback=" + this.mCallback + "]";
    }
}
