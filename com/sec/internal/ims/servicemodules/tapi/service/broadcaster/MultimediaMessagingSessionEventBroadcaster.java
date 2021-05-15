package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.os.RemoteCallbackList;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.IMultimediaMessagingSessionListener;
import com.gsma.services.rcs.extension.MultimediaSession;

public class MultimediaMessagingSessionEventBroadcaster implements IMultimediaMessagingSessionEventBroadcaster {
    private final RemoteCallbackList<IMultimediaMessagingSessionListener> mMultimediaMessagingListeners = new RemoteCallbackList<>();

    public void addMultimediaMessagingEventListener(IMultimediaMessagingSessionListener listener) {
        this.mMultimediaMessagingListeners.register(listener);
    }

    public void removeMultimediaMessagingEventListener(IMultimediaMessagingSessionListener listener) {
        this.mMultimediaMessagingListeners.unregister(listener);
    }

    public void broadcastMessageReceived(ContactId contact, String sessionId, byte[] message) {
        int N = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onMessageReceived(contact, sessionId, message, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }

    public void broadcastMessageReceived(ContactId contact, String sessionId, byte[] message, String contentType) {
        int N = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onMessageReceived(contact, sessionId, message, contentType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }

    public void broadcastStateChanged(ContactId contact, String sessionId, MultimediaSession.State state, MultimediaSession.ReasonCode reasonCode) {
        int N = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onStateChanged(contact, sessionId, state, reasonCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }

    public void broadcastMessagesFlushed(ContactId contact, String sessionId) {
        int N = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onMessagesFlushed(contact, sessionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }
}
