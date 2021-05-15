package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import com.sec.internal.helper.State;

public abstract class ImSessionStateBase extends State {
    ImSession mImSession;
    int mPhoneId;

    ImSessionStateBase(int phoneId, ImSession imSession) {
        this.mPhoneId = phoneId;
        this.mImSession = imSession;
    }

    public boolean processMessage(Message msg) {
        if (msg.what > 3000) {
            return processMessagingEvent(msg);
        }
        if (msg.what > 2000) {
            return processGroupChatManagementEvent(msg);
        }
        return processSessionConnectionEvent(msg);
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message msg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message msg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message msg) {
        return false;
    }
}
