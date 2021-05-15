package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.RejectImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import java.util.ArrayList;
import java.util.List;

public class ImSessionInitialState extends ImSessionStateBase {
    private static final String LOG_TAG = "InitialState";

    ImSessionInitialState(int phoneId, ImSession imSession) {
        super(phoneId, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState enter. " + this.mImSession.getChatId());
        this.mImSession.mListener.onChatStatusUpdate(this.mImSession, ImSession.SessionState.INITIAL);
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState, processMessagingEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        if (msg.what != 3001) {
            return false;
        }
        onSendMessage((MessageBase) msg.obj);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState, processGroupChatManagementEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 2001) {
            onAddParticipants(msg);
            return true;
        } else if (i == 2006) {
            onRemoveParticipants(msg);
            return true;
        } else if (i == 2008) {
            onChangeGCLeader(msg);
            return true;
        } else if (i == 2010) {
            onChangeGCSubject(msg);
            return true;
        } else if (i == 2012) {
            onChangeGroupAlias(msg);
            return true;
        } else if (i != 2014) {
            return false;
        } else {
            onChangeGCIcon(msg);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState, processSessionConnectionEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 1001) {
            onSendMessage((MessageBase) msg.obj);
            return true;
        } else if (i == 1012) {
            return onCloseAllSession();
        } else {
            if (i == 1005) {
                onProcessIncomingSession((ImIncomingSessionEvent) msg.obj);
                return true;
            } else if (i == 1006) {
                onAcceptSession(((Boolean) msg.obj).booleanValue());
                return true;
            } else if (i == 1020) {
                this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.AUTOMATIC_REJOINING, false);
                return true;
            } else if (i != 1021) {
                switch (i) {
                    case 1008:
                        onRejectSession((Integer) msg.obj);
                        return true;
                    case 1009:
                        onRejectSessionDone((RejectImSessionResult) ((AsyncResult) msg.obj).result);
                        return true;
                    case 1010:
                        onProcessIncomingSnfSession((ImIncomingSessionEvent) msg.obj);
                        return true;
                    default:
                        return false;
                }
            } else {
                this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.AUTOMATIC_REJOINING, true);
                return true;
            }
        }
    }

    public ImSessionInfo getPendingSessionInfoByType(ImSessionInfo.SessionType type) {
        for (ImSessionInfo info : this.mImSession.mImSessionInfoList) {
            if (info.mState == ImSessionInfo.ImSessionState.PENDING_INVITE && info.mSessionType == type) {
                return info;
            }
        }
        return null;
    }

    private void onRejectSession(Integer reason) {
        ImSessionRejectReason rejectReason;
        if (!this.mImSession.mIncomingMessageEvents.isEmpty()) {
            this.mImSession.logi("REJECT_SESSION: discard pended incoming message events");
            this.mImSession.mIncomingMessageEvents.clear();
        }
        if (this.mImSession.isChatbotManualAcceptUsed()) {
            Log.i(LOG_TAG, "chatbotRejectReason=" + reason);
            rejectReason = ImSessionRejectReason.CHATBOT_PROFILE_RETRIEVAL_FAIL;
        } else if (this.mImSession.isGroupChat()) {
            rejectReason = ImSessionRejectReason.VOLUNTARILY;
        } else {
            rejectReason = ImSessionRejectReason.BUSY_HERE;
        }
        if (!TextUtils.isEmpty(this.mImSession.getServiceId()) && (this.mImSession.getServiceId().equalsIgnoreCase("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\"") || this.mImSession.getServiceId().equalsIgnoreCase("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\""))) {
            rejectReason = ImSessionRejectReason.VOLUNTARILY;
        }
        for (ImSessionInfo info : new ArrayList<>(this.mImSession.mImSessionInfoList)) {
            if (info != null && info.mState == ImSessionInfo.ImSessionState.PENDING_INVITE) {
                this.mImSession.removeImSessionInfo(info);
                this.mImSession.mImsService.rejectImSession(new RejectImSessionParams(this.mImSession.getChatId(), info.mRawHandle, rejectReason, this.mImSession.obtainMessage(1009)));
            }
        }
    }

    private void onSendMessage(MessageBase imMsg) {
        for (ImSessionInfo info : this.mImSession.mImSessionInfoList) {
            if (info.mState == ImSessionInfo.ImSessionState.PENDING_INVITE) {
                this.mImSession.handleAcceptSession(info);
            }
        }
        if (this.mImSession.hasActiveImSessionInfo()) {
            if (imMsg != null) {
                this.mImSession.mCurrentMessages.add(imMsg);
            }
            this.mImSession.transitionToProperState();
            return;
        }
        if (imMsg != null && this.mImSession.mConfig.getChatRevokeTimer() > 0) {
            this.mImSession.setNetworkFallbackMech(ImSession.ChatFallbackMech.MESSAGE_REVOCATION);
        }
        this.mImSession.mStartingState.onStartSession(imMsg, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onProcessIncomingSession(ImIncomingSessionEvent event) {
        ImSessionInfo prev_info;
        this.mImSession.mIsOfflineGCInvitation = event.mIsSendOnly;
        ImIncomingMessageEvent imIncomingMessageEvent = null;
        this.mImSession.mClosedEvent = null;
        if (this.mImSession.isVoluntaryDeparture()) {
            this.mImSession.logi("User already left the chat voluntarily. Reject the invite");
            this.mImSession.leaveSessionWithReject(event.mRawHandle);
            return;
        }
        boolean hasPreviousPendingInvite = this.mImSession.hasImSessionInfo((Object) ImSessionInfo.ImSessionState.PENDING_INVITE);
        if (event.mFromBlocked) {
            ImIncomingMessageEvent msgEvent = event.mReceivedMessage;
            if (msgEvent != null && !TextUtils.isEmpty(msgEvent.mBody)) {
                msgEvent.mChatId = this.mImSession.getChatId();
                this.mImSession.mListener.onBlockedMessageReceived(msgEvent);
            }
            this.mImSession.mImsService.rejectImSession(new RejectImSessionParams(this.mImSession.getChatId(), event.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
        } else {
            if ((this.mImSession.isChatbotManualAcceptUsed() || (!this.mImSession.isAutoAccept() && !this.mImSession.isGroupChat())) && (prev_info = getPendingSessionInfoByType(ImSessionInfo.SessionType.NORMAL)) != null) {
                ImSession imSession = this.mImSession;
                imSession.logi("Reject previous invite rawHandle = " + prev_info.mRawHandle);
                this.mImSession.removeImSessionInfo(prev_info.mRawHandle);
                this.mImSession.mImsService.rejectImSession(new RejectImSessionParams(this.mImSession.getChatId(), prev_info.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
            }
            ImSessionInfo info = this.mImSession.addImSessionInfo(event, ImSessionInfo.ImSessionState.PENDING_INVITE);
            this.mImSession.updateSessionInfo(info);
            if (event.mReceivedMessage != null && this.mImSession.isChatbotManualAcceptUsed()) {
                this.mImSession.logi("Pending first message in INVITE from Chatbot");
                this.mImSession.mIncomingMessageEvents.add(event.mReceivedMessage);
            }
            if (!this.mImSession.isChatbotManualAcceptUsed() && this.mImSession.isAutoAccept() && !this.mImSession.needToUseGroupChatInvitationUI() && TextUtils.isEmpty(this.mImSession.getServiceId())) {
                this.mImSession.handleAcceptSession(info);
                this.mImSession.transitionToStartingState();
            }
            ImSession imSession2 = this.mImSession;
            if (!this.mImSession.isChatbotManualAcceptUsed()) {
                imIncomingMessageEvent = event.mReceivedMessage;
            }
            imSession2.onIncomingSessionProcessed(imIncomingMessageEvent, !hasPreviousPendingInvite);
        }
        this.mImSession.releaseWakeLock(event.mRawHandle);
    }

    private void onProcessIncomingSnfSession(ImIncomingSessionEvent event) {
        ImSessionInfo prev_info;
        boolean hasPreviousPendingInvite = this.mImSession.hasImSessionInfo((Object) ImSessionInfo.ImSessionState.PENDING_INVITE);
        ImIncomingMessageEvent imIncomingMessageEvent = null;
        if (event.mFromBlocked) {
            ImIncomingMessageEvent msgEvent = event.mReceivedMessage;
            if (msgEvent != null && !TextUtils.isEmpty(msgEvent.mBody)) {
                msgEvent.mChatId = this.mImSession.getChatId();
                this.mImSession.mListener.onBlockedMessageReceived(msgEvent);
            }
            this.mImSession.mImsService.rejectImSession(new RejectImSessionParams(this.mImSession.getChatId(), event.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
            return;
        }
        if ((this.mImSession.isChatbotManualAcceptUsed() || (!this.mImSession.isAutoAccept() && !this.mImSession.isGroupChat())) && (prev_info = getPendingSessionInfoByType(ImSessionInfo.SessionType.SNF_SESSION)) != null) {
            ImSession imSession = this.mImSession;
            imSession.logi("Reject previous invite rawHandle = " + prev_info.mRawHandle);
            this.mImSession.removeImSessionInfo(prev_info.mRawHandle);
            this.mImSession.mImsService.rejectImSession(new RejectImSessionParams(this.mImSession.getChatId(), prev_info.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
        }
        ImSessionInfo info = this.mImSession.addImSessionInfo(event, ImSessionInfo.ImSessionState.PENDING_INVITE);
        if (event.mReceivedMessage != null && this.mImSession.isChatbotManualAcceptUsed()) {
            this.mImSession.logi("Pending first message in INVITE from Chatbot");
            this.mImSession.mIncomingMessageEvents.add(event.mReceivedMessage);
        }
        if ((!this.mImSession.isChatbotManualAcceptUsed() && this.mImSession.isAutoAccept() && !this.mImSession.needToUseGroupChatInvitationUI()) || event.mIsForStoredNoti) {
            this.mImSession.handleAcceptSession(info);
        }
        ImSession imSession2 = this.mImSession;
        if (!this.mImSession.isChatbotManualAcceptUsed()) {
            imIncomingMessageEvent = event.mReceivedMessage;
        }
        imSession2.onIncomingSessionProcessed(imIncomingMessageEvent, !event.mIsForStoredNoti && !hasPreviousPendingInvite);
    }

    private void onAcceptSession(boolean explicit) {
        ImSession imSession = this.mImSession;
        imSession.logi("ACCEPT_SESSION, explicit=" + explicit + ", mImSessionInfoList.size()=" + this.mImSession.mImSessionInfoList.size() + ", isGroupChat()=" + this.mImSession.isGroupChat() + ", isRejoinable()=" + this.mImSession.isRejoinable() + ", isChatbotRole()=" + this.mImSession.isChatbotRole());
        if (!this.mImSession.mIncomingMessageEvents.isEmpty()) {
            this.mImSession.logi("ACCEPT_SESSION: process pended incoming message events");
            for (ImIncomingMessageEvent event : this.mImSession.mIncomingMessageEvents) {
                this.mImSession.mListener.onIncomingMessageProcessed(event, this.mImSession);
            }
            this.mImSession.mIncomingMessageEvents.clear();
        }
        if (!explicit || !this.mImSession.mImSessionInfoList.isEmpty() || !this.mImSession.isGroupChat()) {
            for (ImSessionInfo info : this.mImSession.mImSessionInfoList) {
                if (info.mState == ImSessionInfo.ImSessionState.PENDING_INVITE) {
                    this.mImSession.handleAcceptSession(info);
                }
            }
            this.mImSession.transitionToProperState();
            return;
        }
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onRejectSessionDone(RejectImSessionResult result) {
        if (result.mError != ImError.SUCCESS) {
            ImSession imSession = this.mImSession;
            imSession.loge("Failed to reject session:" + result.mError);
        }
    }

    private void onChangeGroupAlias(Message msg) {
        if (msg.arg1 == 1) {
            this.mImSession.onChangeGroupAliasFailed((String) msg.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        msg.arg1 = 1;
        this.mImSession.deferMessage(msg);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onChangeGCLeader(Message msg) {
        if (msg.arg1 == 1) {
            this.mImSession.onChangeGroupChatLeaderFailed((List) msg.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        msg.arg1 = 1;
        this.mImSession.deferMessage(msg);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onChangeGCSubject(Message msg) {
        if (msg.arg1 == 1) {
            this.mImSession.onChangeGroupChatSubjectFailed((String) msg.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        msg.arg1 = 1;
        this.mImSession.deferMessage(msg);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onChangeGCIcon(Message msg) {
        if (msg.arg1 == 1) {
            this.mImSession.onChangeGroupChatIconFailed((String) msg.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        msg.arg1 = 1;
        this.mImSession.deferMessage(msg);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onAddParticipants(Message msg) {
        if (msg.arg1 == 1) {
            this.mImSession.onAddParticipantsFailed((List) msg.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        msg.arg1 = 1;
        this.mImSession.deferMessage(msg);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onRemoveParticipants(Message msg) {
        if (msg.arg1 == 1) {
            this.mImSession.onRemoveParticipantsFailed((List) msg.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        msg.arg1 = 1;
        this.mImSession.deferMessage(msg);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private boolean onCloseAllSession() {
        if (!this.mImSession.isVoluntaryDeparture() || this.mImSession.getParticipantsSize() <= 0) {
            return false;
        }
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, true);
        return true;
    }
}
