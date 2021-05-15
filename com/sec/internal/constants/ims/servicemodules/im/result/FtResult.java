package com.sec.internal.constants.ims.servicemodules.im.result;

import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class FtResult extends Result {
    public Object mRawHandle;
    public int mRetryTimer;

    public FtResult(ImError imError, Result.Type type, Object rawHandle) {
        super(imError, type);
        this.mRawHandle = rawHandle;
        this.mRetryTimer = 0;
    }

    public FtResult(Result result, Object rawHandle) {
        this(result, rawHandle, 0);
    }

    public FtResult(Result result, Object rawHandle, int retryTimer) {
        super(result.getImError(), result.getType(), result.getSipResponse(), result.getMsrpResponse(), result.getWarningHdr(), result.getReasonHdr());
        this.mRawHandle = rawHandle;
        this.mRetryTimer = retryTimer;
    }

    public String toString() {
        return "FtResult [" + super.toString() + ", mRawHandle=" + this.mRawHandle + ", mRetryTimer=" + this.mRetryTimer + "]";
    }
}
