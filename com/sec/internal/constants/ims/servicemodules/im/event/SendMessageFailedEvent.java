package com.sec.internal.constants.ims.servicemodules.im.event;

import com.android.internal.util.Preconditions;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class SendMessageFailedEvent {
    public final String mChatId;
    public final String mImdnId;
    public final Object mRawHandle;
    public final Result mResult;

    public SendMessageFailedEvent(Object rawHandle, String chatId, String imdnId, Result result) {
        this.mRawHandle = rawHandle;
        this.mChatId = chatId;
        this.mImdnId = imdnId;
        this.mResult = (Result) Preconditions.checkNotNull(result);
    }

    public String toString() {
        return "SendMessageFailedEvent [mRawHandle=" + this.mRawHandle + ", mChatId=" + this.mChatId + ", mImdnId=" + this.mImdnId + ", mResult=" + this.mResult + "]";
    }
}
