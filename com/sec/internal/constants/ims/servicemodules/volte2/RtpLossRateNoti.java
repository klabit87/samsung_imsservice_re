package com.sec.internal.constants.ims.servicemodules.volte2;

public class RtpLossRateNoti {
    int mInterval;
    float mJitter;
    float mLossRate;
    int mNotification;

    public RtpLossRateNoti(int interval, float lossrate, float jitter, int notification) {
        this.mInterval = interval;
        this.mLossRate = lossrate;
        this.mJitter = jitter;
        this.mNotification = notification;
    }

    public int getInterval() {
        return this.mInterval;
    }

    public float getLossRate() {
        return this.mLossRate;
    }

    public float getJitter() {
        return this.mJitter;
    }

    public int getNotification() {
        return this.mNotification;
    }
}
