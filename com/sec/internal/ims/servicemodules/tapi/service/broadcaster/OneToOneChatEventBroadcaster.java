package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.util.Log;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.IOneToOneChatListener;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OneToOneChatEventBroadcaster implements IOneToOneChatEventBroadcaster {
    private static final String LOG_TAG = OneToOneChatEventBroadcaster.class.getSimpleName();
    private Context mContext = null;
    private final RemoteCallbackList<IOneToOneChatListener> mOneToOneChatListeners = new RemoteCallbackList<>();

    public OneToOneChatEventBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addOneToOneChatEventListener(IOneToOneChatListener listener) {
        this.mOneToOneChatListeners.register(listener);
    }

    public void removeOneToOneChatEventListener(IOneToOneChatListener listener) {
        this.mOneToOneChatListeners.unregister(listener);
    }

    public void broadcastMessageStatusChanged(ContactId contact, String mimeType, String msgId, ChatLog.Message.Content.Status status, ChatLog.Message.Content.ReasonCode reasonCode) {
        Log.d(LOG_TAG, "start : broadcastMessageStatusChanged()");
        int N = this.mOneToOneChatListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mOneToOneChatListeners.getBroadcastItem(i).onMessageStatusChanged(contact, mimeType, msgId, status, reasonCode);
            } catch (Exception e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneChatListeners.finishBroadcast();
    }

    public void broadcastComposingEvent(ContactId contact, boolean status) {
        Log.d(LOG_TAG, "start : broadcastComposingEvent()");
        int N = this.mOneToOneChatListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mOneToOneChatListeners.getBroadcastItem(i).onComposingEvent(contact, status);
            } catch (Exception e) {
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneChatListeners.finishBroadcast();
    }

    public void broadcastMessageDeleted(String contact, Set<String> msgIds) {
        Log.d(LOG_TAG, "start : broadcastComposingEvent()");
        ContactId contactId = new ContactId(contact);
        List<String> listIds = new ArrayList<>(msgIds);
        int N = this.mOneToOneChatListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                this.mOneToOneChatListeners.getBroadcastItem(i).onMessagesDeleted(contactId, listIds);
            } catch (Exception e) {
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneChatListeners.finishBroadcast();
    }

    public void broadcastMessageReceived(String msgId, String mimeType, String contact) {
        String str = LOG_TAG;
        Log.d(str, "start : broadcastMessageReceived() msgId:" + msgId + ",mimeType:" + mimeType + ",contact:" + IMSLog.checker(contact));
        Intent newOneToOneMessage = new Intent("com.gsma.services.rcs.chat.action.NEW_ONE_TO_ONE_CHAT_MESSAGE");
        newOneToOneMessage.putExtra("messageId", msgId);
        newOneToOneMessage.putExtra("mimeType", mimeType);
        newOneToOneMessage.putExtra(ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, contact);
        this.mContext.sendBroadcast(newOneToOneMessage);
    }
}
