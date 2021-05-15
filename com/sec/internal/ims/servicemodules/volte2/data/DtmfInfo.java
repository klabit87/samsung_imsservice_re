package com.sec.internal.ims.servicemodules.volte2.data;

public final class DtmfInfo {
    public static final int DTMF_MODE_BRUST = 0;
    public static final int DTMF_MODE_CONTINOUS = 1;
    public static final int START_CONTINOUS_DTMF = 1;
    public static final int STOP_CONTINOUS_DTMF = 2;
    private final int mDuration;
    private final int mEndbit;
    private final int mEvent;
    private final int mVolume;

    public DtmfInfo(int event, int volume, int duration, int endbit) {
        this.mEvent = event;
        this.mVolume = volume;
        this.mDuration = duration;
        this.mEndbit = endbit;
    }

    public int getEvent() {
        return this.mEvent;
    }
}
