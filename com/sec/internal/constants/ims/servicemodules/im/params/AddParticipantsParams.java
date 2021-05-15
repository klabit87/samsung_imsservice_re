package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.UUID;

public class AddParticipantsParams {
    public final Message mCallback;
    public final Object mRawHandle;
    public final List<ImsUri> mReceivers;
    public final String mReqKey = UUID.randomUUID().toString();
    public final String mSubject;

    public AddParticipantsParams(Object rawHandle, List<ImsUri> receivers, Message callback, String subject) {
        this.mRawHandle = rawHandle;
        this.mReceivers = receivers;
        this.mCallback = callback;
        this.mSubject = subject;
    }

    public String toString() {
        return "AddParticipantsParams [mRawHandle=" + this.mRawHandle + ", mReceivers=" + IMSLog.checker(this.mReceivers) + ", mCallback=" + this.mCallback + ", mSubject=" + IMSLog.checker(this.mSubject) + ", mReqKey=" + this.mReqKey + "]";
    }
}
