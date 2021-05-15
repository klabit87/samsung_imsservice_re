package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;

public class StopImSessionParams {
    public Message mCallback;
    public Object mRawHandle;
    public ImSessionStopReason mSessionStopReason;

    public StopImSessionParams(Object rawHandle, ImSessionStopReason stopReason, Message callback) {
        this.mRawHandle = rawHandle;
        this.mSessionStopReason = stopReason;
        this.mCallback = callback;
    }

    public String toString() {
        return "StopImSessionParams [mRawHandle=" + this.mRawHandle + ", mSessionStopReason=" + this.mSessionStopReason + ", mCallback=" + this.mCallback + " ]";
    }
}
