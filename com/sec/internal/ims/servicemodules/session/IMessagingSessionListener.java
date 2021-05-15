package com.sec.internal.ims.servicemodules.session;

import com.sec.internal.ims.servicemodules.im.ImSession;

public interface IMessagingSessionListener {
    void onIncomingSessionInvited(ImSession imSession, String str);

    void onMessageReceived(ImSession imSession, byte[] bArr, String str);

    void onMessagesFlushed(ImSession imSession);

    void onStateChanged(ImSession imSession, ImSession.SessionState sessionState);
}
