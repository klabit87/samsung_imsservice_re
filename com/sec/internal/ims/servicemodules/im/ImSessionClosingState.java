package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import java.util.ArrayList;

public class ImSessionClosingState extends ImSessionStateBase {
    private static final String LOG_TAG = "ClosingState";

    ImSessionClosingState(int phoneId, ImSession imSession) {
        super(phoneId, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState enter. " + this.mImSession.getChatId());
        this.mImSession.mListener.onChatStatusUpdate(this.mImSession, ImSession.SessionState.CLOSING);
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState, processMessagingEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 3001) {
            this.mImSession.deferMessage(msg);
            return true;
        } else if (i == 3010) {
            onSendDeliveredNotification(msg);
            return true;
        } else if (i != 3012) {
            return false;
        } else {
            onSendDisplayedNotification(msg);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState, processGroupChatManagementEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (!(i == 2001 || i == 2008 || i == 2010 || i == 2012 || i == 2014)) {
            if (i == 2005) {
                this.mImSession.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) msg.obj);
                return true;
            } else if (i != 2006) {
                return false;
            }
        }
        this.mImSession.deferMessage(msg);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState, processSessionConnectionEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 1001) {
            this.mImSession.deferMessage(msg);
            return true;
        } else if (i != 1005) {
            switch (i) {
                case 1012:
                    return onCloseAllSession(msg);
                case 1013:
                    this.mImSession.mClosedState.onCloseSessionDone(msg);
                    return true;
                case 1014:
                    this.mImSession.mClosedState.onSessionClosed((ImSessionClosedEvent) msg.obj);
                    return true;
                default:
                    return false;
            }
        } else {
            onProcessIncomingSession(msg);
            return true;
        }
    }

    private void onProcessIncomingSession(Message msg) {
        if (this.mImSession.isVoluntaryDeparture()) {
            this.mImSession.logi("Explicit departure is in progress. Reject the incoming invite");
            this.mImSession.leaveSessionWithReject(((ImIncomingSessionEvent) msg.obj).mRawHandle);
            return;
        }
        this.mImSession.deferMessage(msg);
    }

    private void onSendDeliveredNotification(Message msg) {
        MessageBase message = (MessageBase) msg.obj;
        ImSessionInfo info = this.mImSession.getImSessionInfoByMessageId(message.getId());
        if (info == null || !info.isSnFSession()) {
            this.mImSession.deferMessage(msg);
        } else {
            message.sendDeliveredNotification(info.mState == ImSessionInfo.ImSessionState.ESTABLISHED ? info.mRawHandle : null, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) message), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
        }
    }

    private void onSendDisplayedNotification(Message msg) {
        synchronized (this.mImSession.mMessagesToSendDisplayNotification) {
            for (MessageBase message : new ArrayList<>(this.mImSession.mMessagesToSendDisplayNotification)) {
                ImSessionInfo info = this.mImSession.getImSessionInfoByMessageId(message.getId());
                if (info != null && info.isSnFSession()) {
                    message.sendDisplayedNotification(info.mState == ImSessionInfo.ImSessionState.ESTABLISHED ? info.mRawHandle : null, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) message.toList()), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
                    this.mImSession.mMessagesToSendDisplayNotification.remove(message);
                }
            }
            if (!this.mImSession.mMessagesToSendDisplayNotification.isEmpty()) {
                this.mImSession.deferMessage(msg);
            }
        }
    }

    private boolean onCloseAllSession(Message msg) {
        if (!this.mImSession.isVoluntaryDeparture()) {
            return false;
        }
        this.mImSession.logi("Voluntary departure in ClosingState. DeferMessage");
        this.mImSession.deferMessage(msg);
        return true;
    }
}
