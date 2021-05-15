package com.sec.internal.constants.ims.servicemodules.volte2;

public class CmcInfoEvent {
    private String mExternalCallId;
    private int mRecordEvent;

    public CmcInfoEvent() {
        this.mRecordEvent = -1;
        this.mExternalCallId = "";
        this.mRecordEvent = -1;
        this.mExternalCallId = "";
    }

    public CmcInfoEvent(int recordEvent, String externalCallId) {
        this.mRecordEvent = -1;
        this.mExternalCallId = "";
        this.mRecordEvent = recordEvent;
        this.mExternalCallId = externalCallId;
    }

    public int getRecordEvent() {
        return this.mRecordEvent;
    }

    public void setRecordEvent(int recordEvent) {
        this.mRecordEvent = recordEvent;
    }

    public String getExternalCallId() {
        return this.mExternalCallId;
    }

    public void setExternalCallId(String externalCallId) {
        this.mExternalCallId = externalCallId;
    }
}
