package com.sec.internal.ims.entitlement.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sec.internal.ims.registry.ImsRegistry;

public class FcmListenerService extends FirebaseMessagingService {
    public void onMessageReceived(RemoteMessage message) {
        ImsRegistry.getFcmHandler().onMessageReceived(this, message.getFrom(), message.getData());
    }
}
