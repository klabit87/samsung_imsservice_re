package com.sec.internal.constants.ims.servicemodules.im.event;

import com.android.internal.util.Preconditions;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;

public class ImSessionClosedEvent {
    public final String mChatId;
    public final Object mRawHandle;
    public final ImsUri mReferredBy;
    public final Result mResult;

    public ImSessionClosedEvent(Object rawHandle, String chatId, Result result) {
        this.mRawHandle = rawHandle;
        this.mChatId = chatId;
        this.mResult = (Result) Preconditions.checkNotNull(result);
        this.mReferredBy = null;
    }

    public ImSessionClosedEvent(Object rawHandle, String chatId, Result result, ImsUri referredBy) {
        this.mRawHandle = rawHandle;
        this.mChatId = chatId;
        this.mResult = (Result) Preconditions.checkNotNull(result);
        this.mReferredBy = referredBy;
    }

    public String toString() {
        return "ImSessionClosedEvent [mRawHandle= " + this.mRawHandle + ", mChatId=" + this.mChatId + ", mResult=" + this.mResult + "]";
    }
}
