package com.sec.internal.ims.servicemodules.volte2.data;

public class DedicatedBearerEvent {
    private final int mBearerSessionId;
    private final int mBearerState;
    private final int mQci;

    public DedicatedBearerEvent(int bearState, int qci, int bearerSessionId) {
        this.mBearerState = bearState;
        this.mQci = qci;
        this.mBearerSessionId = bearerSessionId;
    }

    public int getBearerState() {
        return this.mBearerState;
    }

    public int getQci() {
        return this.mQci;
    }

    public int getBearerSessionId() {
        return this.mBearerSessionId;
    }
}
