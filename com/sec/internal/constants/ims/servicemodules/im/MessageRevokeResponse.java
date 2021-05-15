package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;

public class MessageRevokeResponse {
    public final String mImdnId;
    public final ImsUri mRemoteUri;
    public final boolean mResult;

    public MessageRevokeResponse(ImsUri uri, String imdnId, boolean result) {
        this.mRemoteUri = uri;
        this.mImdnId = imdnId;
        this.mResult = result;
    }

    public String toString() {
        return "MessageRevokeResponse [mRemoteUri=" + IMSLog.checker(this.mRemoteUri) + ", mImdnId=" + this.mImdnId + ", mResult=" + this.mResult + "]";
    }
}
