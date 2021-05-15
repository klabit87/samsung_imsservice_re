package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;

public class IshIncomingSessionEvent {
    public IshFileTransfer mFt;
    public ImsUri mRemoteUri;
    public int mSessionId;
    public String mUserAlias;

    public IshIncomingSessionEvent(int sessionId, ImsUri remoteUri, String userAlias, IshFileTransfer ft) {
        this.mSessionId = sessionId;
        this.mRemoteUri = remoteUri;
        this.mUserAlias = userAlias;
        this.mFt = ft;
    }

    public String toString() {
        return "IshIncomingSessionEvent [mSessionId=" + this.mSessionId + ", mRemoteUri=" + IMSLog.checker(this.mRemoteUri) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFt=" + this.mFt + "]";
    }
}
