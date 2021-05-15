package com.sec.internal.constants.ims.servicemodules.im.result;

import com.android.internal.util.Preconditions;

public class SendSlmResult {
    public final String mPAssertedIdentity;
    public final Result mResult;

    public SendSlmResult(Result result, String pAssertedIdentity) {
        this.mResult = (Result) Preconditions.checkNotNull(result);
        this.mPAssertedIdentity = pAssertedIdentity;
    }

    public String toString() {
        return "SendSlmResult [, mResult=" + this.mResult + ", mPAssertedIdentity=" + this.mPAssertedIdentity + "]";
    }
}
