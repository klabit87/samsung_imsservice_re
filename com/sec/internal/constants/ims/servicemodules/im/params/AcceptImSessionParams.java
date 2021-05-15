package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.internal.log.IMSLog;

public class AcceptImSessionParams {
    public Message mCallback;
    public String mChatId;
    public boolean mIsSnF;
    public Object mRawHandle;
    public String mUserAlias;

    public AcceptImSessionParams(String chatId, String userAlias, Object rawHandle, boolean isSnF, Message callback) {
        this.mChatId = chatId;
        this.mUserAlias = userAlias;
        this.mRawHandle = rawHandle;
        this.mIsSnF = isSnF;
        this.mCallback = callback;
    }

    public String toString() {
        return "AcceptImSessionParams [mChatId=" + this.mChatId + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mRawHandle=" + this.mRawHandle + ", mIsSnF=" + this.mIsSnF + ", mCallback=" + this.mCallback + "]";
    }
}
