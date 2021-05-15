package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;

public class AcceptSlmLMMSessionParams {
    public Message mCallback;
    public String mChatId;
    public String mOwnImsi;
    public Object mRawHandle;
    public String mUserAlias;

    public AcceptSlmLMMSessionParams(String chatId, String userAlias, Object rawHandle, Message callback, String ownImsi) {
        this.mChatId = chatId;
        this.mUserAlias = userAlias;
        this.mRawHandle = rawHandle;
        this.mCallback = callback;
        this.mOwnImsi = ownImsi;
    }

    public String toString() {
        return "AcceptSlmLMMSessionParams [mChatId=" + this.mChatId + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mRawHandle=" + this.mRawHandle + ", mCallback=" + this.mCallback + "]";
    }
}
