package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.contact.ContactId;
import java.util.Set;

public interface IOneToOneChatEventBroadcaster {
    void broadcastComposingEvent(ContactId contactId, boolean z);

    void broadcastMessageDeleted(String str, Set<String> set);

    void broadcastMessageReceived(String str, String str2, String str3);

    void broadcastMessageStatusChanged(ContactId contactId, String str, String str2, ChatLog.Message.Content.Status status, ChatLog.Message.Content.ReasonCode reasonCode);
}
