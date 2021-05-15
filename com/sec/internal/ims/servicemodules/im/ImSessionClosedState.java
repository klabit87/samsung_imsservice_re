package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.StopImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.strategy.CmccStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImSessionClosedState extends ImSessionStateBase {
    private static final String LOG_TAG = "ClosedState";
    protected ImSessionStopReason mStopReason;

    ImSessionClosedState(int phoneId, ImSession imSession) {
        super(phoneId, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState enter. " + this.mImSession.getChatId() + ", mClosedReason=" + this.mImSession.mClosedReason + ", ChatState=" + this.mImSession.getChatData().getState());
        this.mImSession.mIsComposing = false;
        for (ImsUri uri : this.mImSession.getComposingActiveUris()) {
            this.mImSession.mListener.onComposingReceived(this.mImSession, uri, (String) null, false, this.mImSession.mComposingNotificationInterval);
        }
        this.mImSession.getComposingActiveUris().clear();
        if (this.mImSession.mClosedReason == ImSessionClosedReason.CLOSED_INVOLUNTARILY) {
            this.mImSession.getChatData().updateState(ChatData.State.CLOSED_INVOLUNTARILY);
        } else if (this.mImSession.mClosedReason == ImSessionClosedReason.KICKED_OUT_BY_LEADER || this.mImSession.mClosedReason == ImSessionClosedReason.GROUP_CHAT_DISMISSED || this.mImSession.mClosedReason == ImSessionClosedReason.LEFT_BY_SERVER) {
            this.mImSession.getChatData().updateState(ChatData.State.NONE);
        } else if (!this.mImSession.isChatState(ChatData.State.CLOSED_VOLUNTARILY) && !this.mImSession.isChatState(ChatData.State.CLOSED_INVOLUNTARILY) && !this.mImSession.isChatState(ChatData.State.NONE)) {
            this.mImSession.getChatData().updateState(ChatData.State.CLOSED_BY_USER);
        }
        if (this.mImSession.mClosedReason == ImSessionClosedReason.ALL_PARTICIPANTS_LEFT) {
            this.mImSession.setSessionUri((ImsUri) null);
            Collection<ImParticipant> deletedParticipants = new ArrayList<>();
            deletedParticipants.addAll(this.mImSession.mParticipants.values());
            this.mImSession.mListener.onParticipantsDeleted(this.mImSession, deletedParticipants);
        }
        this.mImSession.mListener.onChatStatusUpdate(this.mImSession, ImSession.SessionState.CLOSED);
        this.mImSession.mListener.onChatClosed(this.mImSession, this.mImSession.mClosedReason);
        if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_OFFLINE_GC_INVITATION) && this.mImSession.mIsOfflineGCInvitation && this.mImSession.mClosedReason == ImSessionClosedReason.CLOSED_BY_REMOTE && this.mImSession.getChatData().getState() == ChatData.State.CLOSED_BY_USER && this.mImSession.isRejoinable()) {
            this.mImSession.mIsOfflineGCInvitation = false;
            this.mImSession.sendMessage(this.mImSession.obtainMessage(1020));
        }
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState, processMessagingEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 3004) {
            onAttachFile((FtMessage) msg.obj);
            return true;
        } else if (i != 3005) {
            return false;
        } else {
            onSendFile((FtMessage) msg.obj);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState, processGroupChatManagementEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        if (msg.what != 2003) {
            return false;
        }
        this.mImSession.onAddParticipantsFailed((List) msg.obj, ImErrorReason.ENGINE_ERROR);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState, processSessionConnectionEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        return false;
    }

    public void handleCloseSession(Object rawHandle, ImSessionStopReason reason) {
        ImSessionInfo info = this.mImSession.getImSessionInfo(rawHandle);
        this.mStopReason = reason;
        if (info != null) {
            ImSession imSession = this.mImSession;
            imSession.logi("handleCloseSession, info.mState=" + info.mState);
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[info.mState.ordinal()]) {
                case 1:
                    this.mImSession.removeImSessionInfo(rawHandle);
                    return;
                case 2:
                    RejectImSessionParams params = new RejectImSessionParams(this.mImSession.getChatId(), info.mRawHandle);
                    if (!this.mImSession.isGroupChat()) {
                        params.mSessionRejectReason = ImSessionRejectReason.BUSY_HERE;
                    } else if (reason == ImSessionStopReason.INVOLUNTARILY) {
                        params.mSessionRejectReason = ImSessionRejectReason.INVOLUNTARILY;
                    } else if (reason == ImSessionStopReason.VOLUNTARILY) {
                        params.mSessionRejectReason = ImSessionRejectReason.VOLUNTARILY;
                    }
                    this.mImSession.mImsService.rejectImSession(params);
                    this.mImSession.removeImSessionInfo(rawHandle);
                    return;
                case 3:
                    if (!info.isSnFSession()) {
                        this.mImSession.mEstablishedImSessionInfo.remove(info);
                        if (!this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
                            this.mImSession.updateSessionInfo(this.mImSession.mEstablishedImSessionInfo.get(0));
                        }
                    }
                    info.mState = ImSessionInfo.ImSessionState.CLOSING;
                    if (reason == ImSessionStopReason.VOLUNTARILY) {
                        info.mIsTryToLeave = true;
                    }
                    this.mImSession.mImsService.stopImSession(new StopImSessionParams(info.mRawHandle, reason, this.mImSession.obtainMessage(1013)));
                    return;
                case 4:
                case 5:
                case 6:
                    this.mImSession.getHandler().removeMessages(1004, info.mRawHandle);
                    info.mState = ImSessionInfo.ImSessionState.CLOSING;
                    if (reason == ImSessionStopReason.VOLUNTARILY) {
                        info.mIsTryToLeave = true;
                    }
                    this.mImSession.mImsService.stopImSession(new StopImSessionParams(info.mRawHandle, reason, this.mImSession.obtainMessage(1013)));
                    return;
                default:
                    return;
            }
        } else {
            ImSession imSession2 = this.mImSession;
            imSession2.logi("handleCloseSession cannot find ImSessionInfo with rawHandle : " + rawHandle);
        }
    }

    public void onSessionClosed(ImSessionClosedEvent event) {
        this.mImSession.mClosedEvent = event;
        ImSession imSession = this.mImSession;
        imSession.logi("onSessionClosed : " + event);
        if (event.mRawHandle != null) {
            ImSessionInfo info = this.mImSession.removeImSessionInfo(event.mRawHandle);
            if (info == null || info.isSnFSession()) {
                ImSession imSession2 = this.mImSession;
                imSession2.logi("onSessionClosed : unknown rawHandle = " + event.mRawHandle);
                return;
            }
            if (event.mRawHandle.equals(this.mImSession.getRawHandle())) {
                this.mImSession.mClosedReason = getClosedReasonByImError(event.mResult.getImError(), event.mReferredBy, info.mIsTryToLeave);
                if (this.mImSession.getParticipantsSize() < 1 && event.mResult.getImError() == ImError.NORMAL_RELEASE_GONE) {
                    forceCancelFt(true, CancelReason.CANCELED_BY_USER, true);
                }
                if (info.mIsTryToLeave && this.mImSession.isVoluntaryDeparture()) {
                    if (this.mImSession.mClosedReason == ImSessionClosedReason.CLOSED_BY_LOCAL) {
                        this.mImSession.getChatData().updateState(ChatData.State.NONE);
                        this.mImSession.mListener.onChatDeparted(this.mImSession);
                    } else if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.HANDLE_LEAVE_OGC_FAILURE)) {
                        this.mImSession.getChatData().updateState(ChatData.State.NONE);
                    }
                }
            } else {
                ImSession imSession3 = this.mImSession;
                imSession3.logi("session closed event for invalid handle current : " + this.mImSession.getRawHandle() + " event.mRawHandle : " + event.mRawHandle);
            }
            if (info.mState == ImSessionInfo.ImSessionState.ESTABLISHED) {
                this.mImSession.mEstablishedImSessionInfo.remove(info);
                if (!this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
                    this.mImSession.updateSessionInfo(this.mImSession.mEstablishedImSessionInfo.get(0));
                }
            } else {
                this.mImSession.getHandler().removeMessages(1004, info.mRawHandle);
            }
            if (!this.mImSession.hasActiveImSessionInfo()) {
                this.mImSession.failCurrentMessages(event.mRawHandle, event.mResult);
            }
            this.mImSession.transitionToProperState();
        }
    }

    public void onCloseSessionDone(Message msg) {
        Result r;
        StopImSessionResult result = (StopImSessionResult) ((AsyncResult) msg.obj).result;
        ImSession imSession = this.mImSession;
        imSession.logi("onCloseSessionDone : " + result);
        ImError imError = result.mError;
        ImSessionInfo info = this.mImSession.getImSessionInfo(result.mRawHandle);
        if (info == null) {
            ImSession imSession2 = this.mImSession;
            imSession2.logi("onCloseSessionDone : unknown rawHandle=" + result.mRawHandle);
        } else if (!info.isSnFSession() && !this.mImSession.hasActiveImSessionInfo()) {
            if (this.mStopReason != ImSessionStopReason.NO_RESPONSE || this.mImSession.mCurrentMessages.isEmpty() || !this.mImSession.isFirstMessageInStart(this.mImSession.mCurrentMessages.get(0).getBody())) {
                if (this.mImSession.mClosedReason == ImSessionClosedReason.CLOSED_INVOLUNTARILY) {
                    r = new Result(imError, Result.Type.DEVICE_UNREGISTERED);
                } else if (this.mStopReason == ImSessionStopReason.NO_RESPONSE) {
                    r = new Result(imError, Result.Type.NETWORK_ERROR);
                } else {
                    r = new Result(imError, Result.Type.ENGINE_ERROR);
                }
                this.mImSession.failCurrentMessages(result.mRawHandle, r);
                return;
            }
            this.mImSession.logi("Retry when MSRP is not respond");
            retryCurrentMessages();
        }
    }

    private void retryCurrentMessages() {
        this.mImSession.logi("send pending messages");
        for (MessageBase message : this.mImSession.mCurrentMessages) {
            this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) message));
        }
        this.mImSession.mCurrentMessages.clear();
    }

    private void onSendFile(FtMessage ftMsg) {
        if (!this.mImSession.isGroupChat() || !(ftMsg instanceof FtMsrpMessage) || this.mImSession.isBroadcastMsg(ftMsg)) {
            ftMsg.sendFile();
            return;
        }
        IMnoStrategy.StatusCode statusCode = this.mImSession.getRcsStrategy(this.mPhoneId).handleAttachFileFailure(this.mImSession.mClosedReason).getStatusCode();
        if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY) {
            ftMsg.cancelTransfer(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE);
        } else if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
            ftMsg.sendFile();
        }
    }

    private void onAttachFile(FtMessage ftMsg) {
        IMnoStrategy mnoStrategy = this.mImSession.getRcsStrategy(this.mPhoneId);
        if (this.mImSession.isBroadcastMsg(ftMsg) && !(mnoStrategy instanceof CmccStrategy)) {
            ftMsg.attachSlmFile();
        } else if (!this.mImSession.isGroupChat() || !(ftMsg instanceof FtMsrpMessage) || mnoStrategy == null || (mnoStrategy instanceof CmccStrategy)) {
            ftMsg.attachFile(true);
        } else {
            IMnoStrategy.StatusCode statusCode = mnoStrategy.handleAttachFileFailure(this.mImSession.mClosedReason).getStatusCode();
            if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY) {
                ftMsg.cancelTransfer(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE);
            } else if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
                ftMsg.attachSlmFile();
                if (!ftMsg.isResuming()) {
                    ftMsg.sendFile();
                }
            }
        }
    }

    private ImSessionClosedReason getClosedReasonByImError(ImError error, ImsUri referredBy, boolean isTryToLeave) {
        ImSessionClosedReason reason = ImSessionClosedReason.CLOSED_BY_REMOTE;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[error.ordinal()]) {
            case 1:
                if (this.mImSession.isGroupChat()) {
                    return ImSessionClosedReason.KICKED_OUT_BY_LEADER;
                }
                return reason;
            case 2:
                if (this.mImSession.isGroupChat()) {
                    return ImSessionClosedReason.GROUP_CHAT_DISMISSED;
                }
                return reason;
            case 3:
            case 4:
                return ImSessionClosedReason.CLOSED_WITH_480_REASON_CODE;
            case 5:
            case 6:
                if (this.mImSession.isRejoinable()) {
                    return ImSessionClosedReason.CLOSED_INVOLUNTARILY;
                }
                return reason;
            case 7:
                if (isTryToLeave) {
                    return ImSessionClosedReason.LEAVE_SESSION_FAILED;
                }
                if (this.mImSession.isChatState(ChatData.State.ACTIVE) || this.mImSession.isChatState(ChatData.State.CLOSED_INVOLUNTARILY)) {
                    return ImSessionClosedReason.CLOSED_INVOLUNTARILY;
                }
                return reason;
            case 8:
                if (referredBy != null) {
                    ImSession imSession = this.mImSession;
                    imSession.logi("receive BYE with 410 reason. referred by = " + IMSLog.numberChecker(referredBy.toString()));
                    this.mImSession.setSessionUri((ImsUri) null);
                    return ImSessionClosedReason.KICKED_OUT_BY_LEADER;
                } else if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_CHAT_CLOSE_BY_SERVER)) {
                    return ImSessionClosedReason.LEFT_BY_SERVER;
                } else {
                    return reason;
                }
            case 9:
                if (!isTryToLeave) {
                    return reason;
                }
                if (this.mStopReason == ImSessionStopReason.VOLUNTARILY) {
                    return ImSessionClosedReason.CLOSED_BY_LOCAL;
                }
                return ImSessionClosedReason.LEAVE_SESSION_FAILED;
            default:
                return ImSessionClosedReason.CLOSED_BY_REMOTE;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionClosedState$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState;

        static {
            int[] iArr = new int[ImError.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = iArr;
            try {
                iArr[ImError.CONFERENCE_PARTY_BOOTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.CONFERENCE_CALL_COMPLETED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.NORMAL_RELEASE_BEARER_UNAVAILABLE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.SESSION_TIMED_OUT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.DEVICE_UNREGISTERED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.DEDICATED_BEARER_ERROR.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.NETWORK_ERROR.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.NORMAL_RELEASE_GONE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.NORMAL_RELEASE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            int[] iArr2 = new int[ImSessionInfo.ImSessionState.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState = iArr2;
            try {
                iArr2[ImSessionInfo.ImSessionState.INITIAL.ordinal()] = 1;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[ImSessionInfo.ImSessionState.PENDING_INVITE.ordinal()] = 2;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[ImSessionInfo.ImSessionState.ESTABLISHED.ordinal()] = 3;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[ImSessionInfo.ImSessionState.STARTING.ordinal()] = 4;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[ImSessionInfo.ImSessionState.STARTED.ordinal()] = 5;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[ImSessionInfo.ImSessionState.ACCEPTING.ordinal()] = 6;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[ImSessionInfo.ImSessionState.CLOSING.ordinal()] = 7;
            } catch (NoSuchFieldError e16) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void forceCancelFt(boolean cancelPending, CancelReason cancelReason, boolean exceptCancel) {
        ImSession imSession = this.mImSession;
        imSession.logi("forceCancelFt :" + this.mImSession.getChatId());
        for (MessageBase m : this.mImSession.mGetter.getAllPendingMessages(this.mImSession.getChatId())) {
            if ((m instanceof FtMessage) && ((FtMessage) m).getStateId() == 2) {
                ImSession imSession2 = this.mImSession;
                imSession2.logi("forceCancelFt : mPendingMessages FtMessage.getStateId() = " + ((FtMessage) m).getStateId());
                if (!(m instanceof FtHttpIncomingMessage)) {
                    ((FtMessage) m).setFtCompleteCallback((Message) null);
                    ((FtMessage) m).cancelTransfer(cancelReason);
                } else if (!exceptCancel) {
                    ((FtMessage) m).cancelTransfer(cancelReason);
                }
            }
        }
        if (cancelPending) {
            this.mImSession.cancelPendingFilesInQueue();
        }
    }
}
