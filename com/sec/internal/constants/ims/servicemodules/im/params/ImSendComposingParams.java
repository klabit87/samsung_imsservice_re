package com.sec.internal.constants.ims.servicemodules.im.params;

public class ImSendComposingParams {
    public final int mInterval;
    public final boolean mIsComposing;
    public final Object mRawHandle;
    public final String mUserAlias;

    public ImSendComposingParams(Object rawHandle, boolean isComposing, int interval, String userAlias) {
        this.mRawHandle = rawHandle;
        this.mIsComposing = isComposing;
        this.mInterval = interval;
        this.mUserAlias = userAlias;
    }

    public String toString() {
        return "ImSendComposingParams [mRawHandle=" + this.mRawHandle + ", mIsComposing=" + this.mIsComposing + ", mInterval=" + this.mInterval + "]";
    }
}
