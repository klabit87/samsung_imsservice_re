package com.sec.internal.constants.ims.servicemodules.im.result;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class StopImSessionResult {
    public ImError mError;
    public Object mRawHandle;

    public StopImSessionResult(Object rawHandle, ImError error) {
        this.mRawHandle = rawHandle;
        this.mError = error;
    }

    public String toString() {
        return "StopImSessionResult [mRawHandle=" + this.mRawHandle + ", mError=" + this.mError + "]";
    }
}
