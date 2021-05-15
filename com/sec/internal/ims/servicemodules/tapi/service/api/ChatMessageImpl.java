package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.MessageBase;

public class ChatMessageImpl extends IChatMessage.Stub {
    private static final String LOG_TAG = ChatMessageImpl.class.getSimpleName();
    private String mMsgId = null;

    public ChatMessageImpl(String msgId) {
        this.mMsgId = msgId;
    }

    public long getTimestampDelivered() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getDeliveredTimestamp();
        }
        return 0;
    }

    public long getTimestampDisplayed() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getDisplayedTimestamp().longValue();
        }
        return 0;
    }

    public String getChatId() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getChatId();
        }
        return null;
    }

    public ContactId getContact() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg == null || msg.getRemoteUri() == null) {
            return null;
        }
        return new ContactId(msg.getRemoteUri().toString());
    }

    public String getContent() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getBody();
        }
        return null;
    }

    public int getDirection() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg == null || msg.getDirection() == null) {
            return 0;
        }
        return msg.getDirection().ordinal();
    }

    public String getId() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg == null) {
            return null;
        }
        return msg.getId() + "";
    }

    public String getMimeType() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getContentType();
        }
        return null;
    }

    public String getMaapTrafficType() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getMaapTrafficType();
        }
        return null;
    }

    public int getReasonCode() throws RemoteException {
        return ChatLog.Message.Content.ReasonCode.UNSPECIFIED.toInt();
    }

    public int getStatus() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg == null || msg.getStatus() == null) {
            return 0;
        }
        return convertMessageStatus(msg.getStatus().ordinal()).toInt();
    }

    private ChatLog.Message.Content.Status convertMessageStatus(int status) {
        ChatLog.Message.Content.Status result = ChatLog.Message.Content.Status.FAILED;
        switch (status) {
            case 0:
                return ChatLog.Message.Content.Status.DELIVERED;
            case 1:
                return ChatLog.Message.Content.Status.DISPLAYED;
            case 2:
                return ChatLog.Message.Content.Status.SENDING;
            case 3:
                return ChatLog.Message.Content.Status.SENT;
            case 4:
                return ChatLog.Message.Content.Status.FAILED;
            case 5:
                return ChatLog.Message.Content.Status.QUEUED;
            case 6:
                return ChatLog.Message.Content.Status.DISPLAY_REPORT_REQUESTED;
            case 7:
                return ChatLog.Message.Content.Status.QUEUED;
            case 8:
                return ChatLog.Message.Content.Status.REJECTED;
            default:
                return result;
        }
    }

    public long getTimestamp() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getInsertedTimestamp();
        }
        return 0;
    }

    public long getTimestampSent() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg != null) {
            return msg.getSentTimestamp();
        }
        return 0;
    }

    public boolean isRead() throws RemoteException {
        return true;
    }

    public boolean isExpiredDelivery() throws RemoteException {
        MessageBase msg = ImCache.getInstance().queryMessageForOpenApi(this.mMsgId);
        if (msg == null || msg.getNotificationStatus() != NotificationStatus.NONE) {
            return false;
        }
        return true;
    }
}
