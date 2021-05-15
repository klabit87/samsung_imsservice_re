package com.sec.internal.ims.servicemodules.csh.event;

public class VshVideoDisplayParams {
    public ICshSuccessCallback mCallback;
    public int mSessionId;
    public VideoDisplay mVideoDisplay;
    public VshViewType mViewType;

    public VshVideoDisplayParams(int sessionId, VshViewType viewType, VideoDisplay videoDisplay, ICshSuccessCallback callback) {
        this.mSessionId = sessionId;
        this.mViewType = viewType;
        this.mVideoDisplay = videoDisplay;
        this.mCallback = callback;
    }

    public String toString() {
        return "VshVideoDisplayParams #" + this.mSessionId;
    }
}
