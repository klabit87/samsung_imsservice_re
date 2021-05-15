package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;
import java.util.UUID;

public class ChangeGroupChatSubjectParams {
    public final Message mCallback;
    public final Object mRawHandle;
    public final String mReqKey = UUID.randomUUID().toString();
    public final String mSubject;

    public ChangeGroupChatSubjectParams(Object rawHandle, String subject, Message mCallback2) {
        this.mRawHandle = rawHandle;
        this.mSubject = subject;
        this.mCallback = mCallback2;
    }

    public String toString() {
        return "ChangeGroupChatLeaderParams [mRawHandle=" + this.mRawHandle + ", mSubject=" + IMSLog.checker(this.mSubject) + ", mCallback=" + this.mCallback + ", mReqKey=" + this.mReqKey + "]";
    }
}
