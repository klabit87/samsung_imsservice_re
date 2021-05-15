package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Intent;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.MultimediaSession;

public interface IMultimediaStreamingSessionEventBroadcaster {
    void broadcastInvitation(String str, Intent intent);

    void broadcastPayloadReceived(ContactId contactId, String str, byte[] bArr);

    void broadcastStateChanged(ContactId contactId, String str, MultimediaSession.State state, MultimediaSession.ReasonCode reasonCode);
}
