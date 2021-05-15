package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.log.IMSLog;

public class RelayStreams {
    private static final String LOG_TAG = RelayStreams.class.getSimpleName();
    private int mBoundStreamId = -1;
    private int mRelayChannelId = -1;
    private int mSessionId = -1;
    private int mStreamId = -1;

    public RelayStreams() {
        IMSLog.i(LOG_TAG, "RelayStreams");
    }

    public RelayStreams(int streamId, int sessionId) {
        String str = LOG_TAG;
        IMSLog.i(str, "streamId: " + streamId + " sessionId: " + sessionId);
        this.mStreamId = streamId;
        this.mSessionId = sessionId;
    }

    public RelayStreams(IMSMediaEvent relayStreamEvent) {
        String str = LOG_TAG;
        IMSLog.i(str, "streamId: " + relayStreamEvent.getStreamId() + " sessionId: " + relayStreamEvent.getSessionID() + " state: " + relayStreamEvent.getRelayStreamEvent());
        this.mStreamId = relayStreamEvent.getStreamId();
        this.mSessionId = relayStreamEvent.getSessionID();
    }

    public int getStreamId() {
        return this.mStreamId;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public void setBoundStreamId(int boundStreamId) {
        this.mBoundStreamId = boundStreamId;
    }

    public int getBoundStreamId() {
        return this.mBoundStreamId;
    }

    public void setRelayChannelId(int relayChannelId) {
        this.mRelayChannelId = relayChannelId;
    }

    public int getRelayChannelId() {
        return this.mRelayChannelId;
    }

    public String toString() {
        return "RelayStreams [mStreamId=" + this.mStreamId + ", mSessionId=" + this.mSessionId + ", mBoundStreamId=" + this.mBoundStreamId + ", mRelayChannelId=" + this.mRelayChannelId + "]";
    }
}
