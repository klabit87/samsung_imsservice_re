package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.ims.ImsRegistration;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;

public class IncomingCallEvent {
    private final int mCallType;
    private final CallParams mParams;
    private final NameAddr mPeerAddr;
    private final boolean mPreAlerting;
    private final ImsRegistration mRegistration;
    private boolean mRemoteVideoCapa = false;
    private final int mSessionID;

    public IncomingCallEvent(ImsRegistration reg, int sessionId, int callType, NameAddr peerAddr, boolean preAlerting, boolean remoteHasVideoCapa, CallParams params) {
        this.mRegistration = reg;
        this.mSessionID = sessionId;
        this.mCallType = callType;
        this.mPeerAddr = peerAddr;
        this.mPreAlerting = preAlerting;
        this.mRemoteVideoCapa = remoteHasVideoCapa;
        this.mParams = params;
    }

    public ImsRegistration getImsRegistration() {
        return this.mRegistration;
    }

    public boolean getPreAlerting() {
        return this.mPreAlerting;
    }

    public int getSessionID() {
        return this.mSessionID;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public NameAddr getPeerAddr() {
        return this.mPeerAddr;
    }

    public boolean getRemoteVideoCapa() {
        return this.mRemoteVideoCapa;
    }

    public CallParams getParams() {
        return this.mParams;
    }
}
