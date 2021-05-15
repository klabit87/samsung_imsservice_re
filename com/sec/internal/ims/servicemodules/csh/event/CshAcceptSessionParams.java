package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Message;

public class CshAcceptSessionParams {
    public Message mCallback;
    public int mSessionId;

    public CshAcceptSessionParams(int sessionId, Message callback) {
        this.mSessionId = sessionId;
        this.mCallback = callback;
    }
}
