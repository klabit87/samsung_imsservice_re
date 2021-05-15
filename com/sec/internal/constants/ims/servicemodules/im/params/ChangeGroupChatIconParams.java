package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import java.util.UUID;

public class ChangeGroupChatIconParams {
    public final Message mCallback;
    public final String mIconPath;
    public final Object mRawHandle;
    public final String mReqKey = UUID.randomUUID().toString();

    public ChangeGroupChatIconParams(Object rawHandle, String iconPath, Message mCallback2) {
        this.mRawHandle = rawHandle;
        this.mIconPath = iconPath;
        this.mCallback = mCallback2;
    }

    public String toString() {
        return "ChangeGroupChatIconParams [mRawHandle=" + this.mRawHandle + ", mIconPath=" + this.mIconPath + ", mCallback=" + this.mCallback + ", mReqKey=" + this.mReqKey + "]";
    }
}
