package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;

public class RejectImSessionParams {
    public Message mCallback;
    public String mChatId;
    public Object mRawHandle;
    public ImSessionRejectReason mSessionRejectReason;

    public RejectImSessionParams(String chatId, Object rawHandle) {
        this.mChatId = chatId;
        this.mRawHandle = rawHandle;
    }

    public RejectImSessionParams(String chatId, Object rawHandle, ImSessionRejectReason sessionStopReason, Message callback) {
        this.mChatId = chatId;
        this.mRawHandle = rawHandle;
        this.mSessionRejectReason = sessionStopReason;
        this.mCallback = callback;
    }

    public String toString() {
        return "RejectImSessionParams [mChatId=" + this.mChatId + ", mRawHandle=" + this.mRawHandle + ", mSessionStopReason= " + this.mSessionRejectReason + "]";
    }
}
