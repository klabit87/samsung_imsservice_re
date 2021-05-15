package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.IMultimediaStreamingSessionListener;
import com.gsma.services.rcs.extension.MultimediaSession;

public class MultimediaStreamingSessionEventBroadcaster implements IMultimediaStreamingSessionEventBroadcaster {
    private final RemoteCallbackList<IMultimediaStreamingSessionListener> mMultimediaStreamingListeners = new RemoteCallbackList<>();

    public void addMultimediaStreamingEventListener(IMultimediaStreamingSessionListener listener) {
        this.mMultimediaStreamingListeners.register(listener);
    }

    public void removeMultimediaStreamingEventListener(IMultimediaStreamingSessionListener listener) {
        this.mMultimediaStreamingListeners.unregister(listener);
    }

    public void broadcastPayloadReceived(ContactId contact, String sessionId, byte[] content) {
        int N = this.mMultimediaStreamingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mMultimediaStreamingListeners.getBroadcastItem(i).onPayloadReceived(contact, sessionId, content);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaStreamingListeners.finishBroadcast();
    }

    public void broadcastStateChanged(ContactId contact, String sessionId, MultimediaSession.State state, MultimediaSession.ReasonCode reasonCode) {
        int N = this.mMultimediaStreamingListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mMultimediaStreamingListeners.getBroadcastItem(i).onStateChanged(contact, sessionId, state, reasonCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaStreamingListeners.finishBroadcast();
    }

    public void broadcastInvitation(String sessionId, Intent rtpSessionInvite) {
    }
}
