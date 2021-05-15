package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.MultimediaSession;

public interface IMultimediaMessagingSessionEventBroadcaster {
    void broadcastMessageReceived(ContactId contactId, String str, byte[] bArr);

    void broadcastMessageReceived(ContactId contactId, String str, byte[] bArr, String str2);

    void broadcastMessagesFlushed(ContactId contactId, String str);

    void broadcastStateChanged(ContactId contactId, String str, MultimediaSession.State state, MultimediaSession.ReasonCode reasonCode);
}
