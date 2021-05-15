package com.sec.internal.constants.ims.servicemodules.im.result;

import com.android.internal.util.Preconditions;

public class SendMessageResult {
    public String mAllowedMethods;
    public boolean mIsProvisional;
    public Object mRawHandle;
    public final Result mResult;

    public SendMessageResult(Object rawHandle, Result result) {
        this(rawHandle, result, false);
    }

    public SendMessageResult(Object rawHandle, Result result, boolean isProvisional) {
        this.mRawHandle = rawHandle;
        this.mResult = (Result) Preconditions.checkNotNull(result);
        this.mIsProvisional = isProvisional;
        this.mAllowedMethods = null;
    }

    public SendMessageResult(Object rawHandle, Result result, String allowedMethods) {
        this.mRawHandle = rawHandle;
        this.mResult = (Result) Preconditions.checkNotNull(result);
        this.mIsProvisional = false;
        this.mAllowedMethods = allowedMethods;
    }

    public String toString() {
        return "SendMessageParams [mRawHandle=" + this.mRawHandle + ", mResult=" + this.mResult + ", mIsProvisional=" + this.mIsProvisional + ", mAllowedMethods=" + this.mAllowedMethods + "]";
    }
}
