package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ChangeGroupChatLeaderParams {
    public final Message mCallback;
    public final List<ImsUri> mLeader;
    public final Object mRawHandle;
    public final String mReqKey = UUID.randomUUID().toString();

    public ChangeGroupChatLeaderParams(Object mRawHandle2, List<ImsUri> leader, Message mCallback2) {
        this.mRawHandle = mRawHandle2;
        this.mLeader = leader;
        this.mCallback = mCallback2;
    }

    public String toString() {
        return "ChangeGroupChatLeaderParams [mRawHandle=" + this.mRawHandle + ", mLeader=" + IMSLog.numberChecker((Collection<ImsUri>) this.mLeader) + ", mCallback=" + this.mCallback + ", mReqKey=" + this.mReqKey + "]";
    }
}
