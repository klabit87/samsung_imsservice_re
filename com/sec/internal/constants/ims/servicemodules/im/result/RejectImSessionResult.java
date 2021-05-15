package com.sec.internal.constants.ims.servicemodules.im.result;

import com.sec.internal.constants.ims.servicemodules.im.ImError;

public class RejectImSessionResult {
    public ImError mError;
    public Object mRawHandle;

    public RejectImSessionResult(Object rawHandle, ImError error) {
        this.mRawHandle = rawHandle;
        this.mError = error;
    }

    public String toString() {
        return "RejectImSessionResult [mRawHandle=" + this.mRawHandle + ", mError=" + this.mError + "]";
    }
}
