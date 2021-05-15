package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.SipResponse;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.constants.ims.servicemodules.im.result.SendSlmResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.strategy.DefaultRCSMnoStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImSessionDefaultState extends ImSessionStateBase {
    private static final String LOG_TAG = "DefaultState";
    private boolean mIsTriggeredCapex;

    ImSessionDefaultState(int phoneId, ImSession imSession) {
        super(phoneId, imSession);
    }

    public boolean processMessage(Message msg) {
        boolean retVal = super.processMessage(msg);
        if (!retVal) {
            ImSession imSession = this.mImSession;
            imSession.loge("Unexpected event " + msg.what + ". current state is " + this.mImSession.getCurrentState().getName());
        }
        return retVal;
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("DefaultState, processMessagingEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        switch (msg.what) {
            case ImSessionEvent.SEND_MESSAGE_DONE /*3002*/:
                onSendImMessageDone((AsyncResult) msg.obj);
                return true;
            case ImSessionEvent.RECEIVE_MESSAGE /*3003*/:
            case ImSessionEvent.RECEIVE_SLM_MESSAGE /*3009*/:
                onReceiveMessage((MessageBase) msg.obj);
                return true;
            case ImSessionEvent.ATTACH_FILE /*3004*/:
                onAttachFile(msg);
                return true;
            case ImSessionEvent.SEND_FILE /*3005*/:
                onSendFile(msg);
                return true;
            case ImSessionEvent.FILE_COMPLETE /*3006*/:
                onFileComplete((FtMessage) msg.obj);
                return true;
            case ImSessionEvent.SEND_SLM_MESSAGE /*3007*/:
                this.mImSession.onSendSlmMessage((MessageBase) msg.obj);
                return true;
            case ImSessionEvent.SEND_SLM_MESSAGE_DONE /*3008*/:
                onSendSlmMessageDone((AsyncResult) msg.obj);
                return true;
            case ImSessionEvent.SEND_DELIVERED_NOTIFICATION /*3010*/:
                onSendDeliveredNodification((MessageBase) msg.obj);
                return true;
            case ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE /*3011*/:
                onSendDeliveredNodificationDone(msg);
                return true;
            case ImSessionEvent.SEND_DISPLAYED_NOTIFICATION /*3012*/:
                onSendDisplayedNotification();
                return true;
            case ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE /*3013*/:
                onSendDisplayedNotificationDone(msg);
                return true;
            case ImSessionEvent.SEND_MESSAGE_RESPONSE_TIMEOUT /*3014*/:
                onSendMessageResponseTimeout((ImMessage) msg.obj);
                return true;
            case ImSessionEvent.DELIVERY_TIMEOUT /*3015*/:
                onExpireDeliveryTimeout();
                return true;
            case ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST /*3016*/:
                onSendMessageRevokeRequest((List) msg.obj);
                return true;
            case ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE /*3017*/:
                onSendMessageRevokeRequestInternalDone(msg);
                return true;
            case ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED /*3018*/:
                onMessageRevokeTimerExpired();
                return true;
            case ImSessionEvent.MESSAGE_REVOKE_OPERATION_TIMEOUT /*3019*/:
                onMessageRevokeOperationTimeout((String) msg.obj);
                return true;
            case ImSessionEvent.RESEND_MESSAGE_REVOKE_REQUEST /*3020*/:
                onResendMessageRevokeRequest();
                return true;
            case ImSessionEvent.SEND_ISCOMPOSING_NOTIFICATION /*3021*/:
                onSendIscomposingNotification();
                return true;
            case ImSessionEvent.SEND_ISCOMPOSING_TIMEOUT /*3023*/:
                this.mImSession.mIsComposing = false;
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("DefaultState, processGroupChatManagementEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 2016) {
            onDownloadGroupIconDone((ImIconData) msg.obj);
            return true;
        } else if (i != 2017) {
            switch (i) {
                case ImSessionEvent.EXTEND_TO_GROUP_CHAT /*2003*/:
                    this.mImSession.deferMessage(msg);
                    return true;
                case ImSessionEvent.EXTEND_TO_GROUP_CHAT_DONE /*2004*/:
                    onExtendToGroupChatDone(msg);
                    return true;
                case ImSessionEvent.CONFERENCE_INFO_UPDATED /*2005*/:
                    this.mImSession.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) msg.obj);
                    return true;
                default:
                    return false;
            }
        } else {
            this.mImSession.handleRequestTimeout();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("DefaultState, processSessionConnectionEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 1003) {
            onSessionEstablished((ImSessionEstablishedEvent) msg.obj);
            return true;
        } else if (i == 1019) {
            onSessionTimeoutThreshold((ImMessage) msg.obj);
            return true;
        } else if (i != 1022) {
            switch (i) {
                case 1010:
                    onProcessIncomingSnfSession((ImIncomingSessionEvent) msg.obj);
                    return true;
                case 1011:
                    onAcceptSnfSessionDone(msg);
                    return true;
                case 1012:
                    onCloseAllSession((ImSessionStopReason) msg.obj);
                    return true;
                default:
                    switch (i) {
                        case 1014:
                            this.mImSession.mClosedState.onSessionClosed((ImSessionClosedEvent) msg.obj);
                            return true;
                        case 1015:
                            onForceCloseSession();
                            return true;
                        case 1016:
                            onStartSessionProvisionalResponse(msg);
                            return true;
                        case 1017:
                            onStartSessionSynchronousDone(msg);
                            return true;
                        default:
                            return false;
                    }
            }
        } else {
            ImSession imSession2 = this.mImSession;
            imSession2.logi("REFRESH_CAPEX_UPDATE. current state is " + this.mImSession.getCurrentState().getName());
            this.mIsTriggeredCapex = false;
            return true;
        }
    }

    private void onExtendToGroupChatDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        List<ImsUri> participants = (List) ar.userObj;
        if (((StartImSessionResult) ar.result).mResult.getImError() == ImError.SUCCESS) {
            this.mImSession.onAddParticipantsSucceeded(participants);
        } else {
            this.mImSession.onAddParticipantsFailed(participants, ImErrorReason.ENGINE_ERROR);
        }
        this.mImSession.transitionToProperState();
    }

    private void onSendImMessageDone(AsyncResult ar) {
        AsyncResult asyncResult = ar;
        if (asyncResult.exception != null || asyncResult.result == null) {
            this.mImSession.loge("result is null");
            return;
        }
        MessageBase imMsg = (MessageBase) asyncResult.userObj;
        SendMessageResult result = (SendMessageResult) asyncResult.result;
        this.mImSession.removeMessages(1019, imMsg);
        if (!result.mIsProvisional || this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_PROVISIONAL_RESPONSE_ASSENT)) {
            ImError imError = result.mResult.getImError();
            boolean hasChatbotUri = !this.mImSession.isGroupChat() && ChatbotUriUtil.hasChatbotUri(this.mImSession.getParticipantsUri());
            ImSession imSession = this.mImSession;
            imSession.logi("onSendImMessageDone : " + imError + " retryTimer: " + this.mImSession.mRetryTimer + " newContactValue: " + this.mImSession.mNewContactValueUri + " hasChatbotUri: " + hasChatbotUri);
            setRevokeTimer(imMsg, hasChatbotUri, result.mResult);
            if (imError == ImError.SUCCESS) {
                imMsg.onSendMessageDone(result.mResult, new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                return;
            }
            MessageBase msg = this.mImSession.mGetter.getMessage(imMsg.getId());
            if (msg != null) {
                if (msg.getNotificationStatus() != NotificationStatus.NONE) {
                    this.mImSession.logi("onSendImMessageDone : msg has already been delivered successfully");
                    imMsg.onSendMessageDone(new Result(ImError.SUCCESS, result.mResult), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                    return;
                } else if (msg.getStatus() == ImConstants.Status.FAILED) {
                    ImSession imSession2 = this.mImSession;
                    imSession2.loge("onSendImMessageDone : Message Id " + imMsg.getId() + " had been failed");
                    return;
                }
            }
            if (this.mImSession.isGroupChat() || imError != ImError.FORBIDDEN_CHATBOT_CONVERSATION_NEEDED) {
                MessageBase messageBase = msg;
                IMnoStrategy.StrategyResponse strategyResponse = this.mImSession.getRcsStrategy(this.mPhoneId).handleSendingMessageFailure(imError, imMsg.getCurrentRetryCount(), this.mImSession.mRetryTimer, this.mImSession.mNewContactValueUri, this.mImSession.getChatType(), false, hasChatbotUri, imMsg instanceof FtHttpOutgoingMessage);
                IMnoStrategy.StatusCode statusCode = strategyResponse.getStatusCode();
                if (imMsg.getType() == ImConstants.Type.LOCATION && statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
                    this.mImSession.logi("onSendImMessageDone : GLS fallback to legacy");
                    statusCode = IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY;
                }
                this.mImSession.getRcsStrategy(this.mPhoneId).forceRefreshCapability(this.mImSession.getParticipantsUri(), false, imError);
                if (shouldCloseSession(imError) || this.mImSession.getRcsStrategy(this.mPhoneId).isCloseSessionNeeded(imError)) {
                    this.mImSession.mClosedState.handleCloseSession(result.mRawHandle, ImSessionStopReason.INVOLUNTARILY);
                    this.mImSession.transitionToProperState();
                }
                handleSendImResult(strategyResponse, imMsg, result);
                ImSession imSession3 = this.mImSession;
                imSession3.logi("onSendImMessageDone - msgId: " + imMsg.mId + " statusCode: " + statusCode);
                return;
            }
            this.mImSession.loge("onStartSessionDone : chatbot conversation needed");
            this.mImSession.updateIsChatbotRole(true);
            ChatbotUriUtil.updateChatbotCapability(this.mPhoneId, this.mImSession.getRemoteUri(), true);
            if (msg != null) {
                this.mImSession.sendImMessage(msg);
            }
        }
    }

    private void onReceiveMessage(MessageBase msg) {
        Preconditions.checkNotNull(msg, "msg cannot be null");
        IMSLog.s(LOG_TAG, "onReceiveImMessage: " + msg);
        msg.updateStatus(ImConstants.Status.UNREAD);
        msg.updateDeliveredTimestamp(System.currentTimeMillis());
        if (msg.isDeliveredNotificationRequired()) {
            msg.updateDesiredNotificationStatus(NotificationStatus.DELIVERED);
            this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION, (Object) msg));
        }
        if (msg instanceof ImMessage) {
            ((ImMessage) msg).onReceived();
        } else if (msg instanceof FtHttpIncomingMessage) {
            ((FtHttpIncomingMessage) msg).receiveTransfer();
        }
        if (this.mImSession.getComposingActiveUris().remove(msg.mRemoteUri)) {
            this.mImSession.mListener.onComposingReceived(this.mImSession, msg.mRemoteUri, (String) null, false, this.mImSession.mComposingNotificationInterval);
        }
    }

    private void onFileComplete(FtMessage msg) {
        Preconditions.checkNotNull(msg);
        ImSession imSession = this.mImSession;
        imSession.logi("onFileComplete: mProcessingFileTransfer size: " + this.mImSession.mProcessingFileTransfer.size() + ", mPendingFileTrasfer size: " + this.mImSession.mPendingFileTransfer.size());
        if (msg instanceof FtHttpOutgoingMessage) {
            msg.updateStatus(ImConstants.Status.TO_SEND);
            this.mImSession.mListener.onRequestSendMessage(this.mImSession, msg);
            return;
        }
        boolean isRemoved = this.mImSession.mProcessingFileTransfer.remove(msg);
        ImSession imSession2 = this.mImSession;
        imSession2.logi("onFileComplete isRemoved: " + isRemoved + ", mProcessingFileTransfer size: " + this.mImSession.mProcessingFileTransfer.size());
        if (!isRemoved) {
            boolean isRemoved2 = this.mImSession.mPendingFileTransfer.remove(msg);
            ImSession imSession3 = this.mImSession;
            imSession3.logi("onFileComplete isRemoved: " + isRemoved2 + ", mPendingFileTransfer size: " + this.mImSession.mPendingFileTransfer.size());
        }
        if (this.mImSession.mProcessingFileTransfer.isEmpty()) {
            this.mImSession.logi("onFileComplete next send file");
            FtMessage nextFtMsg = removeNextFtMessage();
            if (nextFtMsg != null) {
                if (nextFtMsg.getFtCallback() == null) {
                    nextFtMsg.setFtCompleteCallback(this.mImSession.obtainMessage(ImSessionEvent.FILE_COMPLETE));
                }
                if (this.mImSession.isGroupChat() && !this.mImSession.isBroadcastMsg(nextFtMsg)) {
                    this.mImSession.sendMessage(this.mImSession.obtainMessage(1001));
                }
                this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_FILE, (Object) nextFtMsg));
                this.mImSession.addToProcessingFileTransfer(nextFtMsg);
                return;
            }
            this.mImSession.mListener.onProcessingFileTransferChanged(this.mImSession);
        }
    }

    private void onForceCloseSession() {
        if (this.mImSession.isRejoinable() && !this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
            this.mImSession.mClosedReason = ImSessionClosedReason.CLOSED_INVOLUNTARILY;
        }
        IMnoStrategy strategy = this.mImSession.getRcsStrategy(this.mPhoneId);
        if (strategy == null) {
            strategy = new DefaultRCSMnoStrategy(this.mImSession.getContext(), SimManagerFactory.getPhoneId(this.mImSession.getOwnImsi()));
        }
        this.mImSession.handleCloseAllSession(strategy.getSessionStopReason(this.mImSession.isGroupChat()));
        this.mImSession.mImSessionInfoList.clear();
        this.mImSession.mEstablishedImSessionInfo.clear();
        this.mImSession.transitionToProperState();
    }

    private boolean updateParticipantWithPAI(MessageBase msg, String sipNumber) {
        if (sipNumber == null || sipNumber.isEmpty()) {
            return false;
        }
        Log.i(LOG_TAG, "updateParticipantWithPAI, sipNumber = " + IMSLog.checker(sipNumber));
        ImsUri sipUri = ImsUri.parse(sipNumber);
        if (sipUri == null || sipUri.equals(ImsUri.EMPTY)) {
            return false;
        }
        Collection<ImParticipant> insertedParticipants = new ArrayList<>();
        Collection<ImParticipant> deletedParticipants = new ArrayList<>();
        insertedParticipants.add(new ImParticipant(this.mImSession.getChatId(), ImParticipant.Status.INITIAL, sipUri));
        deletedParticipants.addAll(this.mImSession.getParticipants());
        this.mImSession.mListener.onParticipantsInserted(this.mImSession, insertedParticipants);
        this.mImSession.mListener.onParticipantsDeleted(this.mImSession, deletedParticipants);
        msg.updateRemoteUri(sipUri);
        return true;
    }

    private void onSendSlmMessageDone(AsyncResult ar) {
        SendSlmResult msgResult = (SendSlmResult) ar.result;
        Result r = msgResult.mResult;
        ImError result = r.getImError();
        MessageBase message = (MessageBase) ar.userObj;
        this.mImSession.removeMessages(1019, message);
        if (result == ImError.SUCCESS) {
            if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.CHECK_P_ASSERTED_IDENTITY)) {
                updateParticipantWithPAI(message, msgResult.mPAssertedIdentity);
            }
            message.onSendMessageDone(r, new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
            return;
        }
        MessageBase message2 = this.mImSession.mGetter.getPendingMessage(message.getId());
        if (message2 == null) {
            this.mImSession.logi("onSendSlmMessageDone: No message in pending message list. Ignore.");
        } else if (result == ImError.FORBIDDEN_CHATBOT_CONVERSATION_NEEDED) {
            this.mImSession.loge("onSendSlmMessageDone : chatbot conversation needed");
            if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.CHECK_P_ASSERTED_IDENTITY) && updateParticipantWithPAI(message2, msgResult.mPAssertedIdentity)) {
                message2.incrementRetryCount();
            }
            this.mImSession.updateIsChatbotRole(true);
            ChatbotUriUtil.updateChatbotCapability(this.mPhoneId, this.mImSession.getRemoteUri(), true);
            this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) message2));
        } else {
            IMnoStrategy.StrategyResponse strategyResponse = this.mImSession.getRcsStrategy(this.mPhoneId).handleSendingMessageFailure(result, message2.getCurrentRetryCount(), this.mImSession.mRetryTimer, this.mImSession.mNewContactValueUri, this.mImSession.getChatType(), true, message2 instanceof FtHttpOutgoingMessage);
            IMnoStrategy.StatusCode statusCode = strategyResponse.getStatusCode();
            this.mImSession.mNewContactValueUri = null;
            if (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[statusCode.ordinal()] != 1) {
                message2.onSendMessageDone(r, strategyResponse);
                return;
            }
            ImSession imSession = this.mImSession;
            imSession.logi("onSendSlmMessageDone retry msgId : " + message2.getId());
            message2.incrementRetryCount();
            this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) message2));
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionDefaultState$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        static {
            int[] iArr = new int[IMnoStrategy.StatusCode.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = iArr;
            try {
                iArr[IMnoStrategy.StatusCode.RETRY_IMMEDIATE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.SUCCESS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.RETRY_AFTER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.RETRY_WITH_NEW_CONTACT_HEADER.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.RETRY_AFTER_SESSION.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.FALLBACK_TO_SLM.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.DISPLAY_ERROR.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.RETRY_AFTER_REGI.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    private void triggerCapex() {
        Capabilities capx;
        ImSession imSession = this.mImSession;
        imSession.logi(getName() + "triggerCapex");
        if (!this.mIsTriggeredCapex && !this.mImSession.isGroupChat() && !this.mImSession.getParticipantsUri().isEmpty()) {
            this.mIsTriggeredCapex = true;
            ICapabilityDiscoveryModule discoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
            ImsUri uri = this.mImSession.getParticipantsUri().iterator().next();
            if (discoveryModule != null) {
                capx = discoveryModule.getCapabilities(uri, CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX, SimManagerFactory.getPhoneId(this.mImSession.getChatData().getOwnIMSI()));
            } else {
                capx = null;
            }
            long validityTime = ((long) this.mImSession.mConfig.getMsgCapValidityTime()) * 1000;
            long timeGap = validityTime;
            if (capx != null) {
                timeGap = new Date().getTime() - capx.getTimestamp().getTime();
                if (timeGap >= validityTime) {
                    timeGap = validityTime;
                }
            }
            ImSession imSession2 = this.mImSession;
            imSession2.logi("SEND_ISCOMPOSING_NOTIFICATION. TimeGap is " + timeGap);
            this.mImSession.removeMessages(1022);
            this.mImSession.sendMessageDelayed(this.mImSession.obtainMessage(1022), timeGap);
        }
    }

    private void onExpireDeliveryTimeout() {
        for (MessageBase m : this.mImSession.mGetter.getAllPendingMessages(this.mImSession.getChatId())) {
            if (m instanceof ImMessage) {
                ImMessage imMsg = (ImMessage) m;
                if (imMsg.getStatus() == ImConstants.Status.TO_SEND || imMsg.getStatus() == ImConstants.Status.SENDING) {
                    Log.i(LOG_TAG, "onExpireDeliveryTimeout : sending failed " + imMsg.getId());
                    imMsg.onSendMessageDone(new Result(ImError.SESSION_DELIVERY_TIMEOUT, Result.Type.ENGINE_ERROR), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                }
            }
        }
    }

    private void setRevokeTimer(MessageBase imMsg, boolean hasChatbotUri, Result result) {
        if (this.mImSession.getRcsStrategy(this.mPhoneId).isRevocationAvailableMessage(imMsg) && !hasChatbotUri) {
            if (this.mImSession.isMsgRevocationSupported() && result.getImError() == ImError.SUCCESS && (result.getSipResponse() != SipResponse.SIP_486_BUSY_HERE || this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_REVOKE_MSG_FOR_486_RESP))) {
                MessageBase msg = this.mImSession.mGetter.getMessage(imMsg.getId());
                if (msg != null && msg.getNotificationStatus() == NotificationStatus.NONE && !msg.isTemporary()) {
                    imMsg.updateRevocationStatus(ImConstants.RevocationStatus.AVAILABLE);
                    this.mImSession.getNeedToRevokeMessages().put(msg.getImdnId(), Integer.valueOf(msg.getId()));
                    if (!this.mImSession.mIsRevokeTimerRunning) {
                        ImSession imSession = this.mImSession;
                        imSession.logi("setRevokeTimer() : msg id : " + msg.getId() + " time : " + this.mImSession.mConfig.getChatRevokeTimer());
                        this.mImSession.mIsRevokeTimerRunning = true;
                        PreciseAlarmManager.getInstance(this.mImSession.getContext()).sendMessageDelayed(getClass().getSimpleName(), this.mImSession.obtainMessage(ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED), ((long) this.mImSession.mConfig.getChatRevokeTimer()) * 1000);
                        return;
                    }
                    ImSession imSession2 = this.mImSession;
                    imSession2.logi("setRevokeTimer() : msg id : " + msg.getId() + " aleady timer running");
                }
            } else if (this.mImSession.getNeedToRevokeMessages().containsKey(imMsg.getImdnId())) {
                imMsg.updateRevocationStatus(ImConstants.RevocationStatus.NONE);
                this.mImSession.removeMsgFromListForRevoke(imMsg.getImdnId());
            }
        }
    }

    private boolean shouldCloseSession(ImError mReason) {
        return mReason == ImError.MSRP_ACTION_NOT_ALLOWED || mReason == ImError.MSRP_SESSION_DOES_NOT_EXIST || mReason == ImError.MSRP_SESSION_ON_OTHER_CONNECTION || mReason == ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE;
    }

    private FtMessage removeNextFtMessage() {
        ImSession imSession = this.mImSession;
        imSession.logi("getNextFtMessage, current queue size: " + this.mImSession.mPendingFileTransfer.size());
        if (this.mImSession.mPendingFileTransfer.isEmpty()) {
            return null;
        }
        return this.mImSession.mPendingFileTransfer.remove(0);
    }

    public void onSessionEstablished(ImSessionEstablishedEvent event) {
        ImSession imSession = this.mImSession;
        imSession.logi("onSessionEstablished : " + event);
        ImSessionInfo info = this.mImSession.getImSessionInfo(event.mRawHandle);
        if (info != null) {
            info.mState = ImSessionInfo.ImSessionState.ESTABLISHED;
            if (!info.isSnFSession()) {
                this.mImSession.updateSessionInfo(info);
                this.mImSession.mEstablishedImSessionInfo.add(0, info);
                this.mImSession.mSupportedFeatures = EnumSet.copyOf(event.mFeatures);
                this.mImSession.mRemoteAcceptTypes = event.mAcceptTypes;
                this.mImSession.mRemoteAcceptWrappedTypes = event.mAcceptWrappedTypes;
                Iterator<FtMessage> it = this.mImSession.mPendingFileTransfer.iterator();
                while (it.hasNext()) {
                    it.next().conferenceUriChanged();
                }
            }
            this.mImSession.getHandler().removeMessages(1004, info.mRawHandle);
            this.mImSession.transitionToProperState();
            if (info.mStartingReason == ImSessionInfo.StartingReason.EXTENDING_1_1_TO_GROUP && info.mPrevExtendRawHandle != null) {
                this.mImSession.mImsService.stopImSession(new StopImSessionParams(info.mPrevExtendRawHandle, ImSessionStopReason.INVOLUNTARILY, (Message) null));
                info.mPrevExtendRawHandle = null;
                return;
            }
            return;
        }
        ImSession imSession2 = this.mImSession;
        imSession2.logi("SESSION_ESTABLISHED unknown rawHandle : " + event.mRawHandle);
    }

    public void sendAggregatedDisplayReport() {
        IMSLog.s(LOG_TAG, "sendAggregatedDisplayReport : messages = " + this.mImSession.mMessagesToSendDisplayNotification);
        List<SendImdnParams.ImdnData> imdnDataList = new ArrayList<>();
        List<MessageBase> imdnSendingMessageas = new ArrayList<>();
        while (!this.mImSession.mMessagesToSendDisplayNotification.isEmpty()) {
            MessageBase m = this.mImSession.mMessagesToSendDisplayNotification.pollFirst();
            imdnDataList.add(m.getNewImdnData(NotificationStatus.DISPLAYED));
            imdnSendingMessageas.add(m);
        }
        this.mImSession.mImsService.sendDisplayedNotification(new SendImdnParams((Object) null, this.mImSession.getRemoteUri(), this.mImSession.getChatId(), this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.getOwnImsi(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) imdnSendingMessageas), this.mImSession.getDeviceId(), imdnDataList, this.mImSession.isGroupChat(), new Date(), this.mImSession.isBotSessionAnonymized()));
    }

    private void onSendFile(Message msg) {
        FtMessage ftMsg = (FtMessage) msg.obj;
        if (!this.mImSession.isGroupChat() || !(ftMsg instanceof FtMsrpMessage) || this.mImSession.isBroadcastMsg(ftMsg) || ftMsg.getIsSlmSvcMsg()) {
            ftMsg.sendFile();
            return;
        }
        this.mImSession.logi("SEND_FILE in defaultState, conference uri will be changed");
        ftMsg.conferenceUriChanged();
        this.mImSession.deferMessage(msg);
    }

    private void onAttachFile(Message msg) {
        FtMessage ftMsg = (FtMessage) msg.obj;
        if (this.mImSession.getRcsStrategy(this.mPhoneId).checkCapability(this.mImSession.getParticipantsUri(), (long) (ftMsg instanceof FtMsrpMessage ? Capabilities.FEATURE_FT_SERVICE : Capabilities.FEATURE_FT_HTTP), this.mImSession.getChatType(), this.mImSession.isBroadcastMsg(ftMsg)).getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM && !ftMsg.isFtSms()) {
            ftMsg.attachSlmFile();
        } else if (!this.mImSession.isGroupChat() || !(ftMsg instanceof FtMsrpMessage) || this.mImSession.isBroadcastMsg(ftMsg)) {
            ftMsg.attachFile(true);
        } else {
            this.mImSession.deferMessage(msg);
            this.mImSession.transitionToProperState();
        }
    }

    private void onSendDeliveredNodification(MessageBase message) {
        ImSessionInfo info = this.mImSession.getImSessionInfoByMessageId(message.getId());
        Object rawHandle = this.mImSession.getRawHandle();
        if (info != null && info.isSnFSession() && info.mState == ImSessionInfo.ImSessionState.ESTABLISHED) {
            rawHandle = info.mRawHandle;
        }
        message.sendDeliveredNotification(rawHandle, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) message), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
    }

    private void onSendDisplayedNotification() {
        if (!this.mImSession.mConfig.isAggrImdnSupported() || !this.mImSession.isGroupChat() || this.mImSession.mMessagesToSendDisplayNotification.size() <= 1) {
            List<MessageBase> l = new ArrayList<>();
            while (!this.mImSession.mMessagesToSendDisplayNotification.isEmpty() && l.size() < this.mImSession.getRcsStrategy(this.mPhoneId).intSetting(RcsPolicySettings.RcsPolicy.NUM_OF_DISPLAY_NOTIFICATION_ATONCE)) {
                l.add(this.mImSession.mMessagesToSendDisplayNotification.pollFirst());
            }
            this.mImSession.onSendDisplayedNotification(l);
            return;
        }
        sendAggregatedDisplayReport();
    }

    private void onSendDeliveredNodificationDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        MessageBase message = (MessageBase) ar.userObj;
        if (((Result) ar.result).getImError() == ImError.ENGINE_ERROR) {
            this.mImSession.loge("There is ENGINE Error during sending DELIVERED");
        } else {
            message.onSendDeliveredNotificationDone();
        }
    }

    private void onSendDisplayedNotificationDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        if (((Result) ar.result).getImError() == ImError.ENGINE_ERROR) {
            this.mImSession.loge("There is ENGINE Error during sending DISPLAYED");
            return;
        }
        for (MessageBase message : (List) ar.userObj) {
            message.onSendDisplayedNotificationDone();
        }
    }

    private void onSendMessageResponseTimeout(ImMessage message) {
        ImConstants.Status status = message.getStatus();
        if (status == ImConstants.Status.TO_SEND || status == ImConstants.Status.SENDING) {
            message.onSendMessageResponseTimeout();
        }
    }

    private void onSessionTimeoutThreshold(ImMessage pendingMsg) {
        if (pendingMsg != null) {
            ImSession imSession = this.mImSession;
            imSession.loge("pendingMsg status : " + pendingMsg.getStatus());
            if (pendingMsg.getStatus() == ImConstants.Status.TO_SEND || pendingMsg.getStatus() == ImConstants.Status.SENDING) {
                pendingMsg.onSendMessageDone(new Result(ImError.SESSION_TIMED_OUT, Result.Type.ENGINE_ERROR), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            }
        }
    }

    private void onProcessIncomingSnfSession(ImIncomingSessionEvent event) {
        this.mImSession.handleAcceptSession(this.mImSession.addImSessionInfo(event, ImSessionInfo.ImSessionState.PENDING_INVITE));
        this.mImSession.onIncomingSessionProcessed(event.mReceivedMessage, false);
    }

    private void onAcceptSnfSessionDone(Message msg) {
        StartImSessionResult acceptResult = (StartImSessionResult) ((AsyncResult) msg.obj).result;
        ImSession imSession = this.mImSession;
        imSession.logi("ACCEPT_SNF_SESSION_DONE : " + acceptResult);
        if (acceptResult.mResult.getImError() != ImError.SUCCESS) {
            this.mImSession.removeImSessionInfo(acceptResult.mRawHandle);
        }
        this.mImSession.releaseWakeLock(acceptResult.mRawHandle);
    }

    private void onStartSessionSynchronousDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        Object rawHandle = ar.result;
        String sessionKey = (String) ar.userObj;
        ImSession imSession = this.mImSession;
        imSession.logi("START_SESSION_SYNCHRONOUS_DONE : sessionKey=" + sessionKey + ", rawHandle=" + rawHandle);
        ImSessionInfo info = this.mImSession.getImSessionInfo(sessionKey);
        if (info != null) {
            info.mState = ImSessionInfo.ImSessionState.STARTING;
            info.mRawHandle = rawHandle;
            if (sessionKey.equals(this.mImSession.getRawHandle())) {
                this.mImSession.setRawHandle(rawHandle);
            }
            this.mImSession.mStartingState.startSessionEstablishmentTimer(rawHandle);
            return;
        }
        ImSession imSession2 = this.mImSession;
        imSession2.loge("cannot find the imSessionInfo using sessionKey : " + sessionKey);
        this.mImSession.mImsService.stopImSession(new StopImSessionParams(rawHandle, ImSessionStopReason.INVOLUNTARILY, (Message) null));
    }

    private void onCloseAllSession(ImSessionStopReason stopReason) {
        if (!this.mImSession.mInProgressRequestCallbacks.isEmpty()) {
            this.mImSession.mPendingEvents.add(this.mImSession.obtainMessage(1012, (Object) stopReason));
            return;
        }
        this.mImSession.handleCloseAllSession(stopReason);
        this.mImSession.transitionToProperState();
    }

    private void onSendIscomposingNotification() {
        ImSession imSession = this.mImSession;
        imSession.logi("SEND_ISCOMPOSING_NOTIFICATION received in " + this.mImSession.getCurrentState().getName());
        IMnoStrategy mnoStrategy = this.mImSession.getRcsStrategy(this.mPhoneId);
        if (mnoStrategy == null) {
            this.mImSession.loge("SEND_ISCOMPOSING_NOTIFICATION : Failed to get strategy");
        } else if (mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.TRIGGER_CAPEX_WHEN_STARTTYPING) && !this.mImSession.mConfig.isImCapAlwaysOn()) {
            triggerCapex();
        }
    }

    private void onStartSessionProvisionalResponse(Message msg) {
        StartImSessionResult startResult = (StartImSessionResult) ((AsyncResult) msg.obj).result;
        ImSessionInfo info = this.mImSession.getImSessionInfo(startResult.mRawHandle);
        ImSession imSession = this.mImSession;
        imSession.logi("START_SESSION_PROVISIONAL_RESPONSE : response=" + startResult);
        if (info != null) {
            info.mLastProvisionalResponse = startResult.mResult.getImError();
        }
    }

    private void onMessageRevokeTimerExpired() {
        ImSession imSession = this.mImSession;
        imSession.logi("MESSAGE_REVOKE_TIMER_EXPIRED : " + this.mImSession.getNeedToRevokeMessages());
        this.mImSession.mIsRevokeTimerRunning = false;
        Map<String, Integer> needToRevokeMessages = this.mImSession.getNeedToRevokeMessages();
        List<String> imdnIds = new ArrayList<>(needToRevokeMessages.keySet());
        Collections.sort(imdnIds, new Comparator(needToRevokeMessages) {
            public final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            public final int compare(Object obj, Object obj2) {
                return ((Integer) this.f$0.get((String) obj)).compareTo((Integer) this.f$0.get((String) obj2));
            }
        });
        for (String imdnId : imdnIds) {
            MessageBase message = this.mImSession.mGetter.getMessage(imdnId, ImDirection.OUTGOING);
            if (message != null) {
                message.updateRevocationStatus(ImConstants.RevocationStatus.PENDING);
            }
        }
        this.mImSession.mListener.onMessageRevokeTimerExpired(this.mImSession.getChatId(), imdnIds, this.mImSession.getChatData().getOwnIMSI());
    }

    private void onSendMessageRevokeRequest(List<String> imdnIds) {
        ImSession imSession = this.mImSession;
        imSession.logi("SEND_MESSAGE_REVOKE_REQUEST : " + imdnIds);
        this.mImSession.mListener.setLegacyLatching(this.mImSession.getRemoteUri(), true, this.mImSession.getChatData().getOwnIMSI());
        Collection<MessageBase> messages = new ArrayList<>();
        Collections.sort(imdnIds, new Comparator(this.mImSession.getNeedToRevokeMessages()) {
            public final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            public final int compare(Object obj, Object obj2) {
                return ((Integer) this.f$0.get((String) obj)).compareTo((Integer) this.f$0.get((String) obj2));
            }
        });
        for (String imdnId : imdnIds) {
            MessageBase message = this.mImSession.mGetter.getMessage(imdnId, ImDirection.OUTGOING);
            if (message == null || message.getRevocationStatus() != ImConstants.RevocationStatus.PENDING) {
                ImSession imSession2 = this.mImSession;
                imSession2.loge("SEND_MESSAGE_REVOKE_REQUEST : message can't find - imdnId : " + imdnId);
            } else {
                message.sendMessageRevokeRequest(this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE, (Object) message), this.mImSession.getChatData().getOwnIMSI());
                if (this.mImSession.mConfig.isCfsTrigger()) {
                    message.updateRevocationStatus(ImConstants.RevocationStatus.SENDING);
                    this.mImSession.mListener.addToRevokingMessages(imdnId, this.mImSession.getChatId());
                } else {
                    messages.add(message);
                }
            }
        }
        if (!messages.isEmpty()) {
            this.mImSession.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.NONE, messages, this.mImSession);
        }
    }

    private void onResendMessageRevokeRequest() {
        Map<String, Integer> needToRevokeMessages = this.mImSession.getNeedToRevokeMessages();
        List<String> imdnIds = new ArrayList<>(needToRevokeMessages.keySet());
        Collections.sort(imdnIds, new Comparator(needToRevokeMessages) {
            public final /* synthetic */ Map f$0;

            {
                this.f$0 = r1;
            }

            public final int compare(Object obj, Object obj2) {
                return ((Integer) this.f$0.get((String) obj)).compareTo((Integer) this.f$0.get((String) obj2));
            }
        });
        for (String imdnId : imdnIds) {
            MessageBase message = this.mImSession.mGetter.getMessage(imdnId, ImDirection.OUTGOING);
            if (message != null && message.getRevocationStatus() == ImConstants.RevocationStatus.SENDING) {
                ImSession imSession = this.mImSession;
                imSession.logi("RESEND_MESSAGE_REVOKE_REQUEST : imdnId : " + imdnId);
                message.sendMessageRevokeRequest(this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE, (Object) message), this.mImSession.getChatData().getOwnIMSI());
            }
        }
    }

    private void onSendMessageRevokeRequestInternalDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        ImSession imSession = this.mImSession;
        imSession.logi("SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE : msgId = " + ((MessageBase) ar.userObj).getId() + ", result = " + ((ImError) ar.result));
    }

    private void onMessageRevokeOperationTimeout(String imdnId) {
        if (this.mImSession.getNeedToRevokeMessages().containsKey(imdnId)) {
            Collection<MessageBase> message = new ArrayList<>();
            message.add(this.mImSession.mGetter.getMessage(imdnId, ImDirection.OUTGOING));
            this.mImSession.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, message, this.mImSession);
            ImSession imSession = this.mImSession;
            imSession.logi("MESSAGE_REVOKE_OPERATION_TIMEOUT : imdnId = " + imdnId);
        }
    }

    private void onDownloadGroupIconDone(ImIconData iconData) {
        ImSession imSession = this.mImSession;
        imSession.logi("DOWNLOAD_GROUP_ICON_DONE : " + iconData);
        this.mImSession.updateIconData(iconData);
        this.mImSession.mListener.onGroupChatIconUpdated(this.mImSession.getChatId(), this.mImSession.getIconData());
    }

    private void handleSendImResult(IMnoStrategy.StrategyResponse strategyResponse, MessageBase imMsg, SendMessageResult result) {
        int retryTimer;
        ImError imError = result.mResult.getImError();
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[strategyResponse.getStatusCode().ordinal()]) {
            case 1:
                ImSession imSession = this.mImSession;
                imSession.logi("onSendImMessageDone retry msgId : " + imMsg.getId());
                this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) imMsg));
                imMsg.incrementRetryCount();
                this.mImSession.mNewContactValueUri = null;
                if (imError == ImError.UNSUPPORTED_URI_SCHEME) {
                    this.mImSession.logi("onSendImMessageDone retry with other URI format");
                    this.mImSession.mSwapUriType = true;
                    return;
                }
                return;
            case 2:
                imMsg.onSendMessageDone(new Result(ImError.SUCCESS, result.mResult), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                return;
            case 3:
                ImSession imSession2 = this.mImSession;
                imSession2.logi("onSendImMessageDone retry_after msgId: " + imMsg.getId());
                if ((imMsg instanceof FtHttpOutgoingMessage) && (retryTimer = this.mImSession.getRcsStrategy(this.mPhoneId).getFtHttpSessionRetryTimer(imMsg.getCurrentRetryCount(), imError)) != -1) {
                    this.mImSession.mRetryTimer = retryTimer;
                }
                this.mImSession.sendMessageDelayed(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) imMsg), ((long) this.mImSession.mRetryTimer) * 1000);
                imMsg.incrementRetryCount();
                this.mImSession.mNewContactValueUri = null;
                return;
            case 4:
                ImSession imSession3 = this.mImSession;
                imSession3.logi("onSendImMessageDone retry with new contact, msgId: " + imMsg.getId());
                imMsg.incrementRetryCount();
                this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) imMsg));
                return;
            case 5:
                ImSession imSession4 = this.mImSession;
                imSession4.logi("onSendImMessageDone retry_after_session msgId: " + imMsg.getId());
                this.mImSession.sendMessageDelayed(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) imMsg), 1000);
                imMsg.incrementRetryCount();
                return;
            case 6:
                this.mImSession.mNewContactValueUri = null;
                if (imMsg instanceof FtHttpOutgoingMessage) {
                    this.mImSession.handleUploadedFileFallback((FtHttpOutgoingMessage) imMsg);
                    return;
                } else {
                    this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) imMsg));
                    return;
                }
            case 7:
                this.mImSession.mClosedState.handleCloseSession(result.mRawHandle, ImSessionStopReason.INVOLUNTARILY);
                this.mImSession.transitionToProperState();
                this.mImSession.mNewContactValueUri = null;
                imMsg.onSendMessageDone(result.mResult, strategyResponse);
                return;
            case 8:
                ImSession imSession5 = this.mImSession;
                imSession5.logi("onSendImMessageDone retry_after_regi msgId: " + imMsg.getId());
                this.mImSession.sendMessageDelayed(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) imMsg), 1000);
                imMsg.incrementRetryCount();
                return;
            default:
                this.mImSession.mNewContactValueUri = null;
                imMsg.onSendMessageDone(result.mResult, strategyResponse);
                return;
        }
    }
}
