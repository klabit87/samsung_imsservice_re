package com.sec.internal.ims.servicemodules.csh.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;

public class VshIncomingSessionEvent {
    public String mContentType;
    public String mFilePath;
    public ImsUri mRemoteUri;
    public int mSessionId;
    public int mSource;

    public VshIncomingSessionEvent(int sessionId, ImsUri remoteUri, String contentType, int source, String filePath) {
        this.mSessionId = sessionId;
        this.mRemoteUri = remoteUri;
        this.mContentType = contentType;
        this.mSource = source;
        this.mFilePath = filePath;
    }

    public String toString() {
        return "VshIncomingSessionEvent [mSessionId=" + this.mSessionId + ", mRemoteUri=" + IMSLog.checker(this.mRemoteUri) + ", mContentType=" + this.mContentType + ", mSource=" + this.mSource + ", mFilePath=" + this.mFilePath + "]";
    }
}
