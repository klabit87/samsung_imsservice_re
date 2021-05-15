package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.internal.log.IMSLog;

public class ImComposingEvent {
    public final String mChatId;
    public final int mInterval;
    public final boolean mIsComposing;
    public final String mUri;
    public final String mUserAlias;

    public ImComposingEvent(String chatId, String uri, String userAlias, boolean isComposing, int interval) {
        this.mChatId = chatId;
        this.mUri = uri;
        this.mUserAlias = userAlias;
        this.mIsComposing = isComposing;
        this.mInterval = interval;
    }

    public String toString() {
        return "ImComposingEvent [mChatId=" + this.mChatId + ", mUri=" + IMSLog.checker(this.mUri) + ", mIsComposing=" + this.mIsComposing + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mInterval=" + this.mInterval + "]";
    }
}
