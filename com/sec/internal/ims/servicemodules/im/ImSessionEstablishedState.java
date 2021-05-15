package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AddParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupAliasParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatIconParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatLeaderParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatSubjectParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ImSendComposingParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RemoveParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImSessionEstablishedState extends ImSessionStateBase {
    private static final String LOG_TAG = "EstablishedState";

    ImSessionEstablishedState(int phoneId, ImSession imSession) {
        super(phoneId, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("EstablishedState enter. " + this.mImSession.getChatId());
        this.mImSession.mNewContactValueUri = null;
        this.mImSession.mSwapUriType = false;
        this.mImSession.getChatData().updateState(ChatData.State.ACTIVE);
        if (!this.mImSession.isGroupChat()) {
            this.mImSession.updateParticipantsStatus(ImParticipant.Status.ACCEPTED);
        }
        if (this.mImSession.getChatData().getDirection() == ImDirection.INCOMING) {
            this.mImSession.getRcsStrategy(this.mPhoneId).forceRefreshCapability(this.mImSession.getParticipantsUri(), true, (ImError) null);
        }
        if (this.mImSession.getChatData().isIconUpdatedRequiredOnSessionEstablished() && this.mImSession.mSupportedFeatures.contains(SupportedFeature.GROUP_SESSION_MANAGEMENT) && this.mImSession.getChatData().getIconPath() != null) {
            onChangeGCIcon(this.mImSession.getChatData().getIconPath());
        }
        for (MessageBase m : this.mImSession.mCurrentMessages) {
            onSendImMessage(m);
        }
        this.mImSession.mCurrentMessages.clear();
        this.mImSession.mIsComposing = false;
        this.mImSession.checkAndUpdateSessionTimeout();
        this.mImSession.mListener.onChatStatusUpdate(this.mImSession, ImSession.SessionState.ESTABLISHED);
        this.mImSession.mListener.onChatEstablished(this.mImSession);
    }

    public boolean processMessage(Message msg) {
        if (!(msg.what == 3023 && msg.what == 1019)) {
            this.mImSession.checkAndUpdateSessionTimeout();
        }
        return super.processMessage(msg);
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("EstablishedState, processMessagingEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 3001) {
            onSendMessage((MessageBase) msg.obj);
            return true;
        } else if (i == 3012) {
            onSendDisplayedNotification();
            return true;
        } else if (i == 3004) {
            onAttachFile((FtMessage) msg.obj);
            return true;
        } else if (i != 3005) {
            switch (i) {
                case ImSessionEvent.SEND_ISCOMPOSING_NOTIFICATION /*3021*/:
                    onSendIscomposingNotification(((Boolean) msg.obj).booleanValue());
                    return true;
                case ImSessionEvent.SEND_ISCOMPOSING_REFRESH /*3022*/:
                    onSendIscomposingRefresh();
                    return true;
                case ImSessionEvent.SEND_ISCOMPOSING_TIMEOUT /*3023*/:
                    onSendIscomposingTimeout();
                    return true;
                case ImSessionEvent.RECEIVE_ISCOMPOSING_TIMEOUT /*3024*/:
                    onReceiveIscomposingTimeout();
                    return true;
                default:
                    return false;
            }
        } else {
            onSendFile((FtMessage) msg.obj);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("EstablishedState, processGroupChatManagementEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        switch (msg.what) {
            case ImSessionEvent.ADD_PARTICIPANTS /*2001*/:
                onAddParticipants(msg);
                return true;
            case ImSessionEvent.ADD_PARTICIPANTS_DONE /*2002*/:
                onAddParticipantsDone(msg);
                return true;
            case ImSessionEvent.EXTEND_TO_GROUP_CHAT /*2003*/:
                onExtendToGroupChat(msg);
                return true;
            case ImSessionEvent.CONFERENCE_INFO_UPDATED /*2005*/:
                this.mImSession.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) msg.obj);
                return true;
            case ImSessionEvent.REMOVE_PARTICIPANTS /*2006*/:
                onRemoveParticipants(msg);
                return true;
            case ImSessionEvent.REMOVE_PARTICIPANTS_DONE /*2007*/:
                onRemoveParticipantsDone(msg);
                return true;
            case ImSessionEvent.CHANGE_GC_LEADER /*2008*/:
                onChangeGCLeader(msg);
                return true;
            case ImSessionEvent.CHANGE_GC_LEADER_DONE /*2009*/:
                onChangeGCLeaderDone(msg);
                return true;
            case ImSessionEvent.CHANGE_GC_SUBJECT /*2010*/:
                onChangeGCSubject(msg);
                return true;
            case ImSessionEvent.CHANGE_GC_SUBJECT_DONE /*2011*/:
                onChangeGCSubjectDone(msg);
                return true;
            case ImSessionEvent.CHANGE_GROUP_ALIAS /*2012*/:
                onChangeGCAlias(msg);
                return true;
            case ImSessionEvent.CHANGE_GROUP_ALIAS_DONE /*2013*/:
                onChangeGCAliasDone(msg);
                return true;
            case ImSessionEvent.CHANGE_GC_ICON /*2014*/:
                onChangeGCIcon((String) msg.obj);
                return true;
            case ImSessionEvent.CHANGE_GC_ICON_DONE /*2015*/:
                onChangeGCIconDone(msg);
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("EstablishedState, processSessionConnectionEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 1002) {
            this.mImSession.mStartingState.onStartSessionDone(msg);
            return true;
        } else if (i == 1007) {
            this.mImSession.mStartingState.onAcceptSessionDone(msg);
            return true;
        } else if (i == 1013) {
            this.mImSession.mClosedState.onCloseSessionDone(msg);
            return true;
        } else if (i == 1018) {
            onSessionTimeoutWithoutActivity();
            return true;
        } else if (i == 1004) {
            this.mImSession.onEstablishmentTimeOut(msg.obj);
            return true;
        } else if (i != 1005) {
            return false;
        } else {
            onProcessIncomingSession((ImIncomingSessionEvent) msg.obj);
            return true;
        }
    }

    private void onExtendToGroupChat(Message msg) {
        Message message = msg;
        String oldContributionId = this.mImSession.getContributionId();
        this.mImSession.setContributionId(StringIdGenerator.generateContributionId());
        String sessionKey = StringIdGenerator.generateUuid();
        Message cb = this.mImSession.obtainMessage((int) ImSessionEvent.EXTEND_TO_GROUP_CHAT_DONE, message.obj);
        Message synchronousCb = this.mImSession.obtainMessage(1017, (Object) sessionKey);
        Message provisionalCb = this.mImSession.obtainMessage(1016);
        HashSet hashSet = new HashSet();
        hashSet.addAll(this.mImSession.getParticipantsUri());
        hashSet.addAll((List) message.obj);
        Set<ImsUri> networkPrefRecipients = this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, (Set<ImsUri>) hashSet);
        List<ImsUri> participantsList = new ArrayList<>(networkPrefRecipients);
        String chatId = this.mImSession.getChatId();
        String subject = this.mImSession.getChatData().getSubject();
        String contributionId = this.mImSession.getContributionId();
        String userAlias = this.mImSession.getUserAlias();
        StartImSessionParams.ServiceType serviceType = StartImSessionParams.ServiceType.NORMAL;
        String sdpContentType = this.mImSession.getSdpContentType();
        String conversationId = this.mImSession.getChatData().getConversationId();
        String inReplyToContributionId = this.mImSession.getInReplyToContributionId();
        String serviceId = this.mImSession.getServiceId();
        List<String> list = this.mImSession.mAcceptTypes;
        List<String> list2 = this.mImSession.mAcceptWrappedTypes;
        String ownIMSI = this.mImSession.getChatData().getOwnIMSI();
        boolean z = !this.mImSession.isGroupChat() && ChatbotUriUtil.hasChatbotUri(hashSet);
        List<String> list3 = list;
        Set<ImsUri> set = networkPrefRecipients;
        HashSet hashSet2 = hashSet;
        String str = sessionKey;
        StartImSessionParams params = new StartImSessionParams(chatId, subject, participantsList, contributionId, oldContributionId, userAlias, serviceType, true, sdpContentType, cb, provisionalCb, synchronousCb, (SendMessageParams) null, conversationId, inReplyToContributionId, false, false, false, serviceId, list3, list2, ownIMSI, z, this.mImSession.getChatMode());
        ImSessionInfo imSessionInfo = new ImSessionInfo(ImSessionInfo.ImSessionState.INITIAL, ImDirection.OUTGOING, (ImsUri) null, this.mImSession.getContributionId(), this.mImSession.getConversationId(), this.mImSession.getInReplyToContributionId(), this.mImSession.getSdpContentType());
        imSessionInfo.mStartingReason = ImSessionInfo.StartingReason.EXTENDING_1_1_TO_GROUP;
        imSessionInfo.mPrevExtendRawHandle = this.mImSession.getRawHandle();
        this.mImSession.addImSessionInfo(imSessionInfo);
        this.mImSession.mImsService.extendToGroupChat(params);
    }

    private void onSendImMessage(MessageBase msg) {
        MessageBase messageBase = msg;
        Preconditions.checkNotNull(messageBase, "msg cannot be null");
        this.mImSession.logi("onSendImMessage");
        Set<ImsUri> groupCcList = new HashSet<>();
        if (messageBase instanceof ImMessage) {
            groupCcList = this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, ((ImMessage) messageBase).getGroupCcListUri());
        }
        Set<NotificationStatus> updatedNotification = msg.getDispositionNotification();
        if (this.mImSession.isMsgFallbackSupported()) {
            updatedNotification.add(NotificationStatus.INTERWORKING_SMS);
        }
        Object rawHandle = this.mImSession.getRawHandle();
        String body = msg.getBody();
        String userAlias = msg.getUserAlias();
        String contentType = msg.getContentType();
        String imdnId = msg.getImdnId();
        Date date = r10;
        Date date2 = new Date();
        Set<NotificationStatus> set = updatedNotification;
        SendMessageParams msgParams = new SendMessageParams(rawHandle, body, userAlias, contentType, imdnId, date, updatedNotification, msg.getDeviceName(), msg.getReliableMessage(), this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.EXTRA_FT_FOR_NS), msg.getXmsMessage(), groupCcList, this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE_DONE, (Object) messageBase), msg.getMaapTrafficType(), msg.getReferenceId(), msg.getReferenceType(), msg.getReferenceValue());
        this.mImSession.mImsService.sendImMessage(msgParams);
        this.mImSession.onMessageSending(messageBase);
        this.mImSession.setSessionTimeoutThreshold(messageBase);
    }

    public void onAddParticipants(Message msg) {
        this.mImSession.logi("onAddParticipants");
        List<ImsUri> normalizedUris = new ArrayList<>(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, (Set<ImsUri>) new HashSet((List) msg.obj)));
        IMSLog.s(LOG_TAG, "normalizedUris=" + normalizedUris);
        if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_INDIVIDUAL_REFER)) {
            for (ImsUri uri : normalizedUris) {
                List<ImsUri> list = new ArrayList<>();
                list.add(uri);
                this.mImSession.mImsService.addImParticipants(new AddParticipantsParams(this.mImSession.getRawHandle(), list, this.mImSession.obtainMessage((int) ImSessionEvent.ADD_PARTICIPANTS_DONE, (Object) list), this.mImSession.getSubject()));
            }
            return;
        }
        this.mImSession.mImsService.addImParticipants(new AddParticipantsParams(this.mImSession.getRawHandle(), normalizedUris, this.mImSession.obtainMessage((int) ImSessionEvent.ADD_PARTICIPANTS_DONE, msg.obj), this.mImSession.getSubject()));
    }

    public void onRemoveParticipants(Message msg) {
        this.mImSession.logi("onRemoveParticipants");
        List<ImsUri> normalizedUris = new ArrayList<>(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, (Set<ImsUri>) new HashSet((List) msg.obj)));
        IMSLog.s(LOG_TAG, "normalizedUris=" + normalizedUris);
        this.mImSession.mImsService.removeImParticipants(new RemoveParticipantsParams(this.mImSession.getRawHandle(), normalizedUris, this.mImSession.obtainMessage((int) ImSessionEvent.REMOVE_PARTICIPANTS_DONE, msg.obj)));
    }

    public void onRemoveParticipantsSucceeded(List<ImsUri> participants) {
        ImParticipant p;
        List<ImParticipant> deletedParticipants = new ArrayList<>();
        for (ImsUri uri : participants) {
            ImsUri normalizedUri = this.mImSession.mGetter.normalizeUri(uri);
            if (!(normalizedUri == null || (p = this.mImSession.getParticipant(normalizedUri)) == null)) {
                p.setStatus(ImParticipant.Status.DECLINED);
                deletedParticipants.add(p);
            }
        }
        if (!deletedParticipants.isEmpty()) {
            this.mImSession.mListener.onParticipantsDeleted(this.mImSession, deletedParticipants);
        }
        this.mImSession.mListener.onRemoveParticipantsSucceeded(this.mImSession.getChatId(), participants);
    }

    public void onChangeGroupChatLeaderSucceeded(List<ImsUri> participants) {
        this.mImSession.mListener.onChangeGroupChatLeaderSucceeded(this.mImSession.getChatId(), participants);
    }

    public void onChangeGroupChatSubjectSucceeded(String subject) {
        this.mImSession.mListener.onChangeGroupChatSubjectSucceeded(this.mImSession.getChatId(), subject);
    }

    public void onChangeGroupChatIconSuccess(String icon_path) {
        this.mImSession.mListener.onChangeGroupChatIconSuccess(this.mImSession.getChatId(), icon_path);
    }

    public void onChangeGroupAliasSucceeded(String alias) {
        this.mImSession.mListener.onChangeGroupAliasSucceeded(this.mImSession.getChatId(), alias);
    }

    public void onChangeGCIcon(String icon_path) {
        this.mImSession.mImsService.changeGroupChatIcon(new ChangeGroupChatIconParams(this.mImSession.getRawHandle(), icon_path, this.mImSession.obtainMessage((int) ImSessionEvent.CHANGE_GC_ICON_DONE, (Object) icon_path)));
    }

    private void onSendMessage(MessageBase message) {
        if (!this.mImSession.isGroupChat() || !this.mImSession.isBroadcastMsg(message)) {
            onSendImMessage(message);
        } else {
            this.mImSession.onSendSlmMessage(message);
        }
    }

    private void onSendDisplayedNotification() {
        List<MessageBase> l;
        synchronized (this.mImSession.mMessagesToSendDisplayNotification) {
            l = new ArrayList<>(this.mImSession.mMessagesToSendDisplayNotification);
            this.mImSession.mMessagesToSendDisplayNotification.clear();
        }
        this.mImSession.onSendDisplayedNotification(l);
    }

    private void onSendIscomposingNotification(boolean isComposing) {
        ImSendComposingParams composingParams = new ImSendComposingParams(this.mImSession.getRawHandle(), isComposing, this.mImSession.mComposingNotificationInterval, this.mImSession.getUserAlias());
        if (isComposing) {
            if (!this.mImSession.mIsComposing) {
                this.mImSession.logi("SEND_ISCOMPOSING_NOTIFICATION, sending isComposing=true");
                this.mImSession.mImsService.sendComposingNotification(composingParams);
                this.mImSession.mIsComposing = true;
                this.mImSession.sendMessageDelayed(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_ISCOMPOSING_REFRESH, (Object) true), ((long) this.mImSession.mComposingNotificationInterval) * 1000);
            }
            this.mImSession.removeMessages(ImSessionEvent.SEND_ISCOMPOSING_TIMEOUT);
            this.mImSession.sendMessageDelayed((int) ImSessionEvent.SEND_ISCOMPOSING_TIMEOUT, ((long) this.mImSession.getRcsStrategy(this.mPhoneId).intSetting(RcsPolicySettings.RcsPolicy.COMPOSING_NOTIFICATION_IDLE_INTERVAL)) * 1000);
            return;
        }
        if (this.mImSession.mIsComposing) {
            this.mImSession.logi("SEND_ISCOMPOSING_NOTIFICATION, sending isComposing=false");
            this.mImSession.mImsService.sendComposingNotification(composingParams);
            this.mImSession.mIsComposing = false;
        }
        this.mImSession.removeMessages(ImSessionEvent.SEND_ISCOMPOSING_REFRESH);
        this.mImSession.removeMessages(ImSessionEvent.SEND_ISCOMPOSING_TIMEOUT);
    }

    private void onSendIscomposingTimeout() {
        if (this.mImSession.mIsComposing) {
            this.mImSession.logi("SEND_ISCOMPOSING_TIMEOUT, sending mIsComposing=false");
            this.mImSession.mImsService.sendComposingNotification(new ImSendComposingParams(this.mImSession.getRawHandle(), false, this.mImSession.mComposingNotificationInterval, this.mImSession.getUserAlias()));
            this.mImSession.mIsComposing = false;
        }
        this.mImSession.removeMessages(ImSessionEvent.SEND_ISCOMPOSING_REFRESH);
    }

    private void onSendIscomposingRefresh() {
        if (this.mImSession.mIsComposing) {
            this.mImSession.logi("SEND_ISCOMPOSING_REFRESH, sending mIsComposing=true");
            this.mImSession.mImsService.sendComposingNotification(new ImSendComposingParams(this.mImSession.getRawHandle(), true, this.mImSession.mComposingNotificationInterval, this.mImSession.getUserAlias()));
            this.mImSession.sendMessageDelayed(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_ISCOMPOSING_REFRESH, (Object) true), ((long) this.mImSession.mComposingNotificationInterval) * 1000);
        }
    }

    private void onReceiveIscomposingTimeout() {
        for (ImsUri uri : this.mImSession.getComposingActiveUris()) {
            this.mImSession.mListener.onComposingReceived(this.mImSession, uri, (String) null, false, this.mImSession.mComposingNotificationInterval);
        }
        this.mImSession.getComposingActiveUris().clear();
    }

    private void onAddParticipantsDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        ImError result = (ImError) ar.result;
        List<ImsUri> participants = (List) ar.userObj;
        if (result == ImError.SUCCESS) {
            this.mImSession.onAddParticipantsSucceeded(participants);
        } else if (result == ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED) {
            this.mImSession.onAddParticipantsFailed(participants, ImErrorReason.FORBIDDEN_SERVICE_NOT_AUTHORIZED);
        } else {
            this.mImSession.onAddParticipantsFailed(participants, ImErrorReason.ENGINE_ERROR);
        }
    }

    private void onRemoveParticipantsDone(Message msg) {
        AsyncResult arRemove = (AsyncResult) msg.obj;
        ImError resultRemove = (ImError) arRemove.result;
        List<ImsUri> participantRemove = (List) arRemove.userObj;
        if (resultRemove == ImError.SUCCESS) {
            onRemoveParticipantsSucceeded(participantRemove);
        } else if (resultRemove == ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED) {
            this.mImSession.onRemoveParticipantsFailed(participantRemove, ImErrorReason.FORBIDDEN_SERVICE_NOT_AUTHORIZED);
        } else {
            this.mImSession.onRemoveParticipantsFailed(participantRemove, ImErrorReason.ENGINE_ERROR);
        }
    }

    private void onChangeGCLeader(Message msg) {
        Message cb = this.mImSession.obtainMessage((int) ImSessionEvent.CHANGE_GC_LEADER_DONE, msg.obj);
        ChangeGroupChatLeaderParams params = new ChangeGroupChatLeaderParams(this.mImSession.getRawHandle(), (List) msg.obj, cb);
        this.mImSession.addInProgressRequestCallback(cb);
        this.mImSession.mImsService.changeGroupChatLeader(params);
    }

    private void onChangeGCLeaderDone(Message msg) {
        AsyncResult arChangeLeader = (AsyncResult) msg.obj;
        ImError resultChangeLeader = (ImError) arChangeLeader.result;
        List<ImsUri> leader = (List) arChangeLeader.userObj;
        this.mImSession.removeInProgressRequestCallback(msg);
        if (resultChangeLeader == ImError.SUCCESS) {
            onChangeGroupChatLeaderSucceeded(leader);
        } else {
            this.mImSession.onChangeGroupChatLeaderFailed(leader, ImErrorReason.ENGINE_ERROR);
        }
    }

    private void onChangeGCSubject(Message msg) {
        this.mImSession.mImsService.changeGroupChatSubject(new ChangeGroupChatSubjectParams(this.mImSession.getRawHandle(), (String) msg.obj, this.mImSession.obtainMessage((int) ImSessionEvent.CHANGE_GC_SUBJECT_DONE, msg.obj)));
    }

    private void onChangeGCSubjectDone(Message msg) {
        AsyncResult arResult = (AsyncResult) msg.obj;
        ImError ImResult = (ImError) arResult.result;
        String subject = (String) arResult.userObj;
        if (ImResult == ImError.SUCCESS) {
            onChangeGroupChatSubjectSucceeded(subject);
        } else {
            this.mImSession.onChangeGroupChatSubjectFailed(subject, ImErrorReason.ENGINE_ERROR);
        }
    }

    private void onChangeGCIconDone(Message msg) {
        AsyncResult arResult = (AsyncResult) msg.obj;
        ImError ImResult = (ImError) arResult.result;
        String icon_path = (String) arResult.userObj;
        if (ImResult == ImError.SUCCESS) {
            onChangeGroupChatIconSuccess(icon_path);
        } else {
            this.mImSession.onChangeGroupChatIconFailed(icon_path, ImErrorReason.ENGINE_ERROR);
        }
    }

    private void onChangeGCAlias(Message msg) {
        this.mImSession.mImsService.changeGroupAlias(new ChangeGroupAliasParams(this.mImSession.getRawHandle(), (String) msg.obj, this.mImSession.obtainMessage((int) ImSessionEvent.CHANGE_GROUP_ALIAS_DONE, msg.obj)));
    }

    private void onChangeGCAliasDone(Message msg) {
        AsyncResult arResult = (AsyncResult) msg.obj;
        ImError ImResult = (ImError) arResult.result;
        String alias = (String) arResult.userObj;
        if (ImResult == ImError.SUCCESS) {
            onChangeGroupAliasSucceeded(alias);
        } else {
            this.mImSession.onChangeGroupAliasFailed(alias, ImErrorReason.ENGINE_ERROR);
        }
    }

    private void onAttachFile(FtMessage ftMsg) {
        if (this.mImSession.isGroupChat() && (ftMsg instanceof FtMsrpMessage)) {
            if (this.mImSession.isBroadcastMsg(ftMsg)) {
                ((FtMsrpMessage) ftMsg).setConferenceUri((ImsUri) null);
                this.mImSession.logi("setConferenceUri null");
            } else {
                ((FtMsrpMessage) ftMsg).setConferenceUri(this.mImSession.getSessionUri());
                this.mImSession.logi("setConferenceUri");
            }
        }
        if (ftMsg.isResuming()) {
            ftMsg.sendFile();
        } else {
            ftMsg.attachFile(true);
        }
    }

    private void onSendFile(FtMessage ftMsg) {
        if (this.mImSession.isGroupChat() && (ftMsg instanceof FtMsrpMessage) && ftMsg.isConferenceUriChanged()) {
            ((FtMsrpMessage) ftMsg).setConferenceUri(this.mImSession.getSessionUri());
        }
        ftMsg.sendFile();
    }

    private void onSessionTimeoutWithoutActivity() {
        this.mImSession.logi("in SESSION_TIMEOUT_WITHOUT_ACTIVITY event. closed session");
        this.mImSession.setIsTimerExpired(true);
        this.mImSession.closeSession();
    }

    private void onProcessIncomingSession(ImIncomingSessionEvent event) {
        ImSession imSession = this.mImSession;
        imSession.logi("race condition : mRawHandle=" + event.mRawHandle);
        IMSLog.c(LogClass.IM_INCOMING_SESSION_ERR, " race : " + event.mRawHandle);
        if (this.mImSession.isVoluntaryDeparture()) {
            this.mImSession.leaveSessionWithReject(event.mRawHandle);
            return;
        }
        if (!this.mImSession.isGroupChat()) {
            this.mImSession.mClosedState.handleCloseSession(this.mImSession.getRawHandle(), ImSessionStopReason.INVOLUNTARILY);
        }
        ImSessionInfo info = this.mImSession.addImSessionInfo(event, ImSessionInfo.ImSessionState.ACCEPTING);
        if (this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
            this.mImSession.updateSessionInfo(info);
        }
        this.mImSession.handleAcceptSession(info);
        this.mImSession.onIncomingSessionProcessed(event.mReceivedMessage, false);
        this.mImSession.transitionToProperState();
        this.mImSession.releaseWakeLock(event.mRawHandle);
    }
}
