package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;

public class RejectSlmLMMSessionParams {
    public Message mCallback;
    public String mChatId;
    public String mOwnImsi;
    public Object mRawHandle;
    public ImSessionRejectReason mSessionRejectReason;

    public RejectSlmLMMSessionParams(String chatId, Object rawHandle, ImSessionRejectReason sessionStopReason, Message callback, String ownImsi) {
        this.mChatId = chatId;
        this.mRawHandle = rawHandle;
        this.mSessionRejectReason = sessionStopReason;
        this.mCallback = callback;
        this.mOwnImsi = ownImsi;
    }

    public String toString() {
        return "RejectSlmLMMSessionParams [mChatId=" + this.mChatId + ", mRawHandle=" + this.mRawHandle + ", mSessionStopReason= " + this.mSessionRejectReason + "]";
    }
}
