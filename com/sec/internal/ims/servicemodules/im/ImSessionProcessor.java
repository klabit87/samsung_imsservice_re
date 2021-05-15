package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.ims.im.IImSessionListener;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmLMMIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmLMMSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.BlockedNumberUtil;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.servicemodules.im.strategy.CmccStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ImSessionProcessor extends Handler implements ImSessionListener {
    private static final int EVENT_RESET_INCOMING_SESSION_FOR_A2P = 2;
    private static final int EVENT_VOLUNTARY_DEPARTURE_GROUPCHAT = 1;
    private static final String LOG_TAG = ImSessionProcessor.class.getSimpleName();
    private ImCache mCache;
    protected final List<IChatEventListener> mChatEventListeners = new ArrayList();
    private Context mContext;
    private FtProcessor mFtProcessor;
    private GcmHandler mGcmHandler;
    private final List<ImSession> mGroupChatsForDeparture = new ArrayList();
    private final ImBigDataProcessor mImBigDataProcessor;
    private ImModule mImModule;
    private ImProcessor mImProcessor;
    private final ImRevocationHandler mImRevocationHandler;
    private final IImServiceInterface mImService;
    private final Map<Integer, ArrayList<IImSessionListener>> mImSessionListener;
    private ImTranslation mImTranslation;
    private ImdnHandler mImdnHandler;
    private final ISlmServiceInterface mSlmService;

    public ImSessionProcessor(Context context, IImServiceInterface imService, ImModule imModule, ImCache imCache) {
        this.mContext = context;
        this.mImService = imService;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mSlmService = ImsRegistry.getHandlerFactory().getSlmHandler();
        this.mImSessionListener = new HashMap();
        this.mImBigDataProcessor = new ImBigDataProcessor(context, imModule);
        this.mImRevocationHandler = new ImRevocationHandler(context, imModule, imCache, this);
    }

    /* access modifiers changed from: protected */
    public void init(ImProcessor imProcessor, FtProcessor ftProcessor, ImTranslation imTranslation) {
        this.mImProcessor = imProcessor;
        this.mFtProcessor = ftProcessor;
        this.mImTranslation = imTranslation;
        this.mImdnHandler = new ImdnHandler(this.mContext, this.mImModule, this.mCache, imProcessor, ftProcessor, this);
        this.mGcmHandler = new GcmHandler(this.mImModule, this.mCache, this, imTranslation);
    }

    /* access modifiers changed from: protected */
    public void registerChatEventListener(IChatEventListener listener) {
        this.mChatEventListeners.add(listener);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int i = msg.what;
        if (i == 1) {
            handleEventVoluntaryDeparture();
        } else if (i == 2) {
            int phoneId = ((Integer) msg.obj).intValue();
            String str = LOG_TAG;
            Log.i(str, "EVENT_RESET_INCOMING_SESSION_FOR_A2P: phoneId = " + phoneId);
            this.mImModule.mHasIncomingSessionForA2P.put(phoneId, false);
        }
    }

    public void onChatEstablished(ImSession chat) {
        for (IChatEventListener onChatEstablished : this.mChatEventListeners) {
            onChatEstablished.onChatEstablished(chat.getChatId(), chat.getDirection(), chat.getSessionUri(), chat.mRemoteAcceptTypes, chat.mRemoteAcceptWrappedTypes);
        }
        notifyImSessionEstablished(this.mImModule.getPhoneIdByIMSI(chat.getOwnImsi()));
    }

    public void onChatStatusUpdate(ImSession chat, ImSession.SessionState state) {
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onChatUpdateState(chat.getChatId(), chat.getDirection(), state);
        }
    }

    public void onChatClosed(ImSession chat, ImSessionClosedReason reason) {
        if (reason != ImSessionClosedReason.NONE) {
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onChatClosed(chat.getChatId(), chat.getDirection(), reason);
            }
        }
        this.mCache.removeActiveSession(chat);
        notifyImSessionClosed(this.mImModule.getPhoneIdByIMSI(chat.getOwnImsi()));
    }

    public void onChatDeparted(ImSession chat) {
        if (chat == null) {
            Log.e(LOG_TAG, "onChatDeparted : invalid ImSession");
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "onChatDeparted : " + chat.getChatId() + ", isReusable=" + chat.isReusable());
        if (chat.isReusable()) {
            chat.updateChatState(ChatData.State.NONE);
        } else {
            this.mCache.deleteSession(chat);
        }
        this.mGroupChatsForDeparture.remove(chat);
    }

    /* access modifiers changed from: protected */
    public void onSendImdnFailed(SendImdnFailedEvent event) {
        this.mImdnHandler.onSendImdnFailed(event);
    }

    public void onComposingReceived(ImSession chat, ImsUri eventUri, String userAlias, boolean isComposing, int interval) {
        String str = LOG_TAG;
        Log.i(str, "notifyComposingReceived: " + chat.getChatId() + " isComposing:" + isComposing);
        for (IChatEventListener onComposingNotificationReceived : this.mChatEventListeners) {
            onComposingNotificationReceived.onComposingNotificationReceived(chat.getChatId(), chat.isGroupChat(), eventUri, userAlias, isComposing, interval);
        }
    }

    /* access modifiers changed from: protected */
    public void onImdnNotificationReceived(ImdnNotificationEvent event) {
        this.mImdnHandler.onImdnNotificationReceived(event);
    }

    /* access modifiers changed from: protected */
    public void onComposingNotificationReceived(ImComposingEvent event) {
        this.mImdnHandler.onComposingNotificationReceived(event);
    }

    public void getComposingActiveUris(String chatId) {
        post(new Runnable(chatId) {
            public final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$getComposingActiveUris$0$ImSessionProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$getComposingActiveUris$0$ImSessionProcessor(String chatId) {
        String str = LOG_TAG;
        Log.i(str, "getComposingActiveUris: chatId=" + chatId);
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            Log.e(LOG_TAG, "Session not found in the cache.");
            this.mImTranslation.notifyComposingActiveUris(chatId, (Set<ImsUri>) null);
            return;
        }
        this.mImTranslation.notifyComposingActiveUris(chatId, session.getComposingActiveUris());
    }

    public void onAddParticipantsSucceeded(String chatId, List<ImsUri> participants) {
        String str = LOG_TAG;
        Log.i(str, "onAddParticipantsSucceeded: " + chatId);
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onAddParticipantsSucceeded(chatId, participants);
        }
    }

    public void onAddParticipantsFailed(String chatId, List<ImsUri> participants, ImErrorReason reason) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            int phoneId = session.getPhoneId();
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onAddParticipantsFailed: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", " + IMSLog.numberChecker((Collection<ImsUri>) participants) + ", error=" + reason);
            List<String> dumps = new ArrayList<>();
            dumps.add(reason.toString());
            ImsUtil.listToDumpFormat(LogClass.IM_ADD_PARTICIPANT_RES, phoneId, chatId, dumps);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onAddParticipantsFailed(chatId, participants, reason);
            }
        }
    }

    public void onRemoveParticipantsSucceeded(String chatId, List<ImsUri> participants) {
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onRemoveParticipantsSucceeded(chatId, participants);
        }
    }

    public void onRemoveParticipantsFailed(String chatId, List<ImsUri> participants, ImErrorReason reason) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            int phoneId = session.getPhoneId();
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onRemoveParticipantsFailed: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", " + IMSLog.numberChecker((Collection<ImsUri>) participants) + ", error=" + reason);
            List<String> dumps = new ArrayList<>();
            dumps.add(reason.toString());
            ImsUtil.listToDumpFormat(LogClass.IM_REMOVE_PARTICIPANT_RES, phoneId, chatId, dumps);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onRemoveParticipantsFailed(chatId, participants, reason);
            }
        }
    }

    public void onChangeGroupChatLeaderSucceeded(String chatId, List<ImsUri> participants) {
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onChangeGroupChatLeaderSucceeded(chatId, participants);
        }
    }

    public void onChangeGroupChatLeaderFailed(String chatId, List<ImsUri> participants, ImErrorReason reason) {
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onChangeGroupChatLeaderFailed(chatId, participants, reason);
        }
    }

    public void onChangeGroupChatSubjectSucceeded(String chatId, String subject) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatSubjectSucceeded: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", subject=" + IMSLog.checker(subject));
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onChangeGroupChatSubjectSucceeded(chatId, subject);
            }
        }
    }

    public void onChangeGroupChatSubjectFailed(String chatId, String subject, ImErrorReason reason) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatSubjectFailed: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", subject=" + IMSLog.checker(subject) + ", error=" + reason);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onChangeGroupChatSubjectFailed(chatId, subject, reason);
            }
        }
    }

    public void onChangeGroupChatIconSuccess(String chatId, String icon_path) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatIconSuccess: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", icon_path=" + icon_path);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onChangeGroupChatIconSuccess(chatId, icon_path);
            }
        }
    }

    public void onChangeGroupChatIconFailed(String chatId, String icon_path, ImErrorReason reason) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatIconFailed: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", icon_path=" + icon_path + ", error=" + reason);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onChangeGroupChatIconFailed(chatId, icon_path, reason);
            }
        }
    }

    public void onChangeGroupAliasSucceeded(String chatId, String alias) {
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onChangeGroupAliasSucceeded(chatId, alias);
        }
    }

    public void onChangeGroupAliasFailed(String chatId, String alias, ImErrorReason reason) {
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onChangeGroupAliasFailed(chatId, alias, reason);
        }
    }

    public void onParticipantsInserted(ImSession session, Collection<ImParticipant> participants) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "onParticipantsInserted: " + session.getChatId() + ", " + IMSLog.checker(participants));
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onParticipantsInserted: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", " + participants);
            this.mCache.addParticipant(participants);
            session.addParticipant(participants);
        }
    }

    public void onParticipantsUpdated(ImSession session, Collection<ImParticipant> participants) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "onParticipantsUpdated: " + session.getChatId() + ", " + participants);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onParticipantsUpdated: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", participants= " + participants);
            this.mCache.updateParticipant(participants);
        }
    }

    public void onParticipantsDeleted(ImSession session, Collection<ImParticipant> participants) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "onParticipantsDeleted: " + session.getChatId() + ", " + participants);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onParticipantsDeleted: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", participants= " + participants);
            this.mCache.deleteParticipant(participants);
            session.deleteParticipant(participants);
        }
    }

    public void onNotifyParticipantsAdded(ImSession session, Map<ImParticipant, Date> participants) {
        String str = LOG_TAG;
        Log.i(str, "onNotifyParticipantsAdded: " + session.getChatId() + ", " + participants);
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onNotifyParticipantsAdded: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", participants= " + participants);
        makeNewSystemUserMessage(session, participants, ImConstants.Type.SYSTEM_USER_JOINED);
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onParticipantsAdded(session, participants.keySet());
        }
    }

    public void onNotifyParticipantsJoined(ImSession session, Map<ImParticipant, Date> participants) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "onNotifyParticipantsJoined: " + session.getChatId() + ", " + participants);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onNotifyParticipantsJoined: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", participants= " + participants);
            makeNewSystemUserMessage(session, participants, ImConstants.Type.SYSTEM_USER_JOINED);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onParticipantsJoined(session, participants.keySet());
            }
        }
    }

    public void onNotifyParticipantsLeft(ImSession session, Map<ImParticipant, Date> participants) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "onNotifyParticipantsLeft: " + session.getChatId() + ", " + participants);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onNotifyParticipantsJoined: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", participants= " + participants);
            makeNewSystemUserMessage(session, participants, ImConstants.Type.SYSTEM_USER_LEFT);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onParticipantsLeft(session, participants.keySet());
            }
        }
    }

    public void onNotifyParticipantsKickedOut(ImSession session, Map<ImParticipant, Date> participants) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "onNotifyParticipantsKickedOut: " + session.getChatId() + ", " + participants);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onNotifyParticipantsKickedOut: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", participants= " + participants);
            makeNewSystemUserMessage(session, participants, ImConstants.Type.SYSTEM_USER_KICKOUT);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onParticipantsLeft(session, participants.keySet());
            }
        }
    }

    public void onGroupChatLeaderChanged(ImSession session, String leaderParticipants) {
        if (session != null) {
            String str = LOG_TAG;
            Log.i(str, "onGroupChatLeaderChanged: " + session.getChatId() + ", " + leaderParticipants);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onGroupChatLeaderChanged: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", leader= " + leaderParticipants);
            this.mCache.makeNewSystemUserMessage(session, leaderParticipants, ImConstants.Type.SYSTEM_LEADER_CHANGED);
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onGroupChatLeaderUpdated(session.getChatId(), leaderParticipants);
            }
        }
    }

    public void onGroupChatLeaderInformed(ImSession session, String leaderParticipants) {
        String str = LOG_TAG;
        Log.i(str, "onGroupChatLeaderInformed: " + IMSLog.numberChecker(leaderParticipants));
        this.mCache.makeNewSystemUserMessage(session, leaderParticipants, ImConstants.Type.SYSTEM_LEADER_INFORMED);
    }

    public void onIncomingSessionProcessed(ImIncomingMessageEvent msgEvent, ImSession session, boolean notify) {
        String str = LOG_TAG;
        Log.i(str, "onIncomingSessionProcessed, need to notify?: " + notify);
        this.mCache.updateActiveSession(session);
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getChatData().getOwnIMSI());
        if (this.mImModule.getImConfig(phoneId).getUserAliasEnabled() && !session.isGroupChat()) {
            if (msgEvent != null) {
                ImsUri normalizedUri = this.mImModule.normalizeUri(phoneId, msgEvent.mSender);
                if (normalizedUri != null) {
                    session.updateParticipantAlias(msgEvent.mUserAlias, session.getParticipant(normalizedUri));
                }
            } else if (!session.getParticipants().isEmpty()) {
                session.updateParticipantAlias(session.getInitiatorAlias(), session.getParticipants().iterator().next());
            }
        }
        if (notify) {
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onChatInvitationReceived(session);
            }
        }
        onIncomingMessageProcessed(msgEvent, session);
    }

    public void onIncomingMessageProcessed(ImIncomingMessageEvent msgEvent, ImSession session) {
        if (msgEvent != null && !TextUtils.isEmpty(msgEvent.mBody)) {
            String str = LOG_TAG;
            Log.i(str, "Received a message in INVITE : " + msgEvent.mImdnMessageId);
            msgEvent.mChatId = session.getChatId();
            this.mImProcessor.onIncomingMessageReceived(msgEvent);
        }
    }

    public void onImErrorReport(ImError imError, int phoneId) {
        Log.i(LOG_TAG, "onImErrorReport");
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration(phoneId);
        if (imsRegistration != null) {
            IRegistrationGovernor governor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(imsRegistration.getHandle());
            List<String> dumps = new ArrayList<>();
            dumps.add(String.valueOf(imError.ordinal()));
            ImsUtil.listToDumpFormat(LogClass.IM_IMERRORREPORT, phoneId, MessageContextValues.none, dumps);
            if (governor != null) {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()];
                if (i == 1) {
                    Log.i(LOG_TAG, "onImErrorReport : 403 forbidden no warning header, try re-regi");
                    governor.onSipError("im", SipErrorBase.FORBIDDEN);
                } else if (i == 2) {
                    Log.i(LOG_TAG, "onImErrorReport : 403 forbidden service not authorised");
                    governor.onSipError("im", SipErrorBase.FORBIDDEN_SERVICE_NOT_AUTHORISED);
                }
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionProcessor$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;

        static {
            int[] iArr = new int[ImError.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = iArr;
            try {
                iArr[ImError.FORBIDDEN_NO_WARNING_HEADER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public void onProcessingFileTransferChanged(ImSession session) {
        this.mFtProcessor.notifyOngoingFtEvent(session.mProcessingFileTransfer.isEmpty() && !this.mCache.hasProcessingFileTransfer(), this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()));
    }

    public void onChatSubjectUpdated(String chatId, ImSubjectData subjectData) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChatSubjectUpdated: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", subject=" + IMSLog.checker(subjectData.getSubject()));
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onChatSubjectUpdated(chatId, subjectData);
            }
        }
    }

    public void onGroupChatIconUpdated(String chatId, ImIconData iconData) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onGroupChatIconUpdated: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId());
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onGroupChatIconUpdated(chatId, iconData);
            }
        }
    }

    public void onGroupChatIconDeleted(String chatId) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onGroupChatIconDeleted: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId());
            for (IChatEventListener listener : this.mChatEventListeners) {
                listener.onGroupChatIconDeleted(chatId);
            }
        }
    }

    public void onParticipantAliasUpdated(String chatId, ImParticipant participant) {
        for (IChatEventListener listener : this.mChatEventListeners) {
            listener.onParticipantAliasUpdated(chatId, participant);
        }
    }

    public void onBlockedMessageReceived(ImIncomingMessageEvent event) {
        this.mImProcessor.onIncomingMessageReceived(event);
    }

    public void onRequestSendMessage(ImSession session, MessageBase message) {
        this.mImProcessor.sendMessage(session, message);
    }

    /* access modifiers changed from: protected */
    public Future<ImSession> createChat(List<ImsUri> participants, String subject, String sdpContentType, int threadId, String requestMessageId) {
        return createChat(0, participants, subject, sdpContentType, threadId, requestMessageId, false, false, (String) null, false, false, (String) null, (String) null, (ImsUri) null);
    }

    public Future<ImSession> createChat(int phoneId, List<ImsUri> participants, String subject, String sdpContentType, int threadId, String requestMessageId, boolean isBroadcastChat, boolean isClosedGC, String iconPath, boolean isTokenUsed, boolean isTokenLink) {
        return createChat(phoneId, participants, subject, sdpContentType, threadId, requestMessageId, isBroadcastChat, isClosedGC, iconPath, isTokenUsed, isTokenLink, (String) null, (String) null, (ImsUri) null);
    }

    /* access modifiers changed from: protected */
    public Future<ImSession> createChat(int phoneId, List<ImsUri> participants, String subject, String sdpContentType, int threadId, String requestMessageId, boolean isBroadcastChat, boolean isClosedGC, String iconPath, boolean isTokenUsed, boolean isTokenLink, String conversationId, String contributionId, ImsUri sessionUri) {
        $$Lambda$ImSessionProcessor$pXrj6MVuBsKuDh0sQntiJoydMrU r0 = r1;
        $$Lambda$ImSessionProcessor$pXrj6MVuBsKuDh0sQntiJoydMrU r1 = new Callable(phoneId, participants, subject, sdpContentType, threadId, requestMessageId, isBroadcastChat, isClosedGC, iconPath, isTokenUsed, isTokenLink, conversationId, contributionId, sessionUri) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ boolean f$10;
            public final /* synthetic */ boolean f$11;
            public final /* synthetic */ String f$12;
            public final /* synthetic */ String f$13;
            public final /* synthetic */ ImsUri f$14;
            public final /* synthetic */ List f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ String f$4;
            public final /* synthetic */ int f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ String f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
                this.f$9 = r10;
                this.f$10 = r11;
                this.f$11 = r12;
                this.f$12 = r13;
                this.f$13 = r14;
                this.f$14 = r15;
            }

            public final Object call() {
                return ImSessionProcessor.this.lambda$createChat$1$ImSessionProcessor(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14);
            }
        };
        FutureTask<ImSession> future = new FutureTask<>(r0);
        post(future);
        return future;
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01a3  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x01ad A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0201  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x022b  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x0258 A[LOOP:3: B:76:0x0252->B:78:0x0258, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0278  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x027a  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0281  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ com.sec.internal.ims.servicemodules.im.ImSession lambda$createChat$1$ImSessionProcessor(int r23, java.util.List r24, java.lang.String r25, java.lang.String r26, int r27, java.lang.String r28, boolean r29, boolean r30, java.lang.String r31, boolean r32, boolean r33, java.lang.String r34, java.lang.String r35, com.sec.ims.util.ImsUri r36) throws java.lang.Exception {
        /*
            r22 = this;
            r0 = r22
            r1 = r23
            r15 = r27
            r14 = r28
            r13 = r29
            r12 = r30
            r11 = r31
            r10 = r32
            r9 = r33
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "createChat: participants="
            r3.append(r4)
            java.lang.String r4 = com.sec.internal.log.IMSLog.numberChecker((java.util.Collection<com.sec.ims.util.ImsUri>) r24)
            r3.append(r4)
            java.lang.String r4 = " subject="
            r3.append(r4)
            java.lang.String r4 = com.sec.internal.log.IMSLog.checker(r25)
            r3.append(r4)
            java.lang.String r4 = " sdpContentType="
            r3.append(r4)
            r8 = r26
            r3.append(r8)
            java.lang.String r4 = " threadId="
            r3.append(r4)
            r3.append(r15)
            java.lang.String r4 = " requestMessageId="
            r3.append(r4)
            r3.append(r14)
            java.lang.String r4 = " isBroadcastChat="
            r3.append(r4)
            r3.append(r13)
            java.lang.String r4 = " isClosedGC="
            r3.append(r4)
            r3.append(r12)
            java.lang.String r4 = " iconPath="
            r3.append(r4)
            r3.append(r11)
            java.lang.String r4 = " isTokenUsed="
            r3.append(r4)
            r3.append(r10)
            java.lang.String r4 = " isTokenLink="
            r3.append(r4)
            r3.append(r9)
            java.lang.String r4 = " conversationId="
            r3.append(r4)
            r7 = r34
            r3.append(r7)
            java.lang.String r4 = " contributionId="
            r3.append(r4)
            r6 = r35
            r3.append(r6)
            java.lang.String r4 = " sessionUri="
            r3.append(r4)
            r5 = r36
            r3.append(r5)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r1, r3)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            java.lang.String r4 = r2.getImsiFromPhoneId(r1)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            boolean r2 = r2.isRegistered(r1)
            if (r2 != 0) goto L_0x00d1
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r2 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r23)
            java.lang.String r3 = "pending_for_regi"
            boolean r2 = r2.boolSetting(r3)
            if (r2 != 0) goto L_0x00d1
            java.util.List<com.sec.internal.ims.servicemodules.im.listener.IChatEventListener> r2 = r0.mChatEventListeners
            java.util.Iterator r2 = r2.iterator()
        L_0x00b9:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x00cf
            java.lang.Object r3 = r2.next()
            com.sec.internal.ims.servicemodules.im.listener.IChatEventListener r3 = (com.sec.internal.ims.servicemodules.im.listener.IChatEventListener) r3
            r17 = r2
            com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason.INVALID
            r3.onCreateChatFailed(r1, r15, r2, r14)
            r2 = r17
            goto L_0x00b9
        L_0x00cf:
            r2 = 0
            return r2
        L_0x00d1:
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            java.util.HashSet r3 = new java.util.HashSet
            r8 = r24
            r3.<init>(r8)
            java.util.Set r3 = r2.normalizeUri((int) r1, (java.util.Collection<com.sec.ims.util.ImsUri>) r3)
            if (r3 == 0) goto L_0x0294
            boolean r2 = r3.isEmpty()
            if (r2 == 0) goto L_0x00f0
            r2 = r25
            r7 = r1
            r17 = r3
            r18 = r4
            r1 = r14
            goto L_0x029c
        L_0x00f0:
            int r2 = r3.size()
            r5 = 1
            if (r2 > r5) goto L_0x0100
            boolean r2 = android.text.TextUtils.isEmpty(r35)
            if (r2 != 0) goto L_0x00fe
            goto L_0x0100
        L_0x00fe:
            r2 = 0
            goto L_0x0101
        L_0x0100:
            r2 = r5
        L_0x0101:
            if (r13 != 0) goto L_0x0136
            if (r2 == 0) goto L_0x0136
            com.sec.internal.ims.servicemodules.im.ImModule r5 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r5 = r5.getImConfig(r1)
            boolean r5 = r5.getGroupChatEnabled()
            if (r5 != 0) goto L_0x0136
            java.lang.String r5 = LOG_TAG
            java.lang.String r6 = "GroupChat is disabled. getGroupChatEnabled=false"
            android.util.Log.i(r5, r6)
            java.util.List<com.sec.internal.ims.servicemodules.im.listener.IChatEventListener> r5 = r0.mChatEventListeners
            java.util.Iterator r5 = r5.iterator()
        L_0x011e:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x0134
            java.lang.Object r6 = r5.next()
            com.sec.internal.ims.servicemodules.im.listener.IChatEventListener r6 = (com.sec.internal.ims.servicemodules.im.listener.IChatEventListener) r6
            r17 = r5
            com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason r5 = com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED
            r6.onCreateChatFailed(r1, r15, r5, r14)
            r5 = r17
            goto L_0x011e
        L_0x0134:
            r5 = 0
            return r5
        L_0x0136:
            if (r2 == 0) goto L_0x016e
            if (r11 == 0) goto L_0x016e
            java.io.File r5 = new java.io.File
            r5.<init>(r11)
            boolean r6 = r5.exists()
            if (r6 != 0) goto L_0x016c
            java.lang.String r6 = LOG_TAG
            r19 = r5
            java.lang.String r5 = "icon file doesn't exist"
            android.util.Log.e(r6, r5)
            java.util.List<com.sec.internal.ims.servicemodules.im.listener.IChatEventListener> r5 = r0.mChatEventListeners
            java.util.Iterator r5 = r5.iterator()
        L_0x0154:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x016a
            java.lang.Object r6 = r5.next()
            com.sec.internal.ims.servicemodules.im.listener.IChatEventListener r6 = (com.sec.internal.ims.servicemodules.im.listener.IChatEventListener) r6
            r17 = r5
            com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason r5 = com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason.INVALID_ICON_PATH
            r6.onCreateChatFailed(r1, r15, r5, r14)
            r5 = r17
            goto L_0x0154
        L_0x016a:
            r5 = 0
            return r5
        L_0x016c:
            r19 = r5
        L_0x016e:
            r5 = 0
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r6 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r23)
            if (r12 == 0) goto L_0x0182
            r16 = r5
            java.lang.String r5 = "participantbased_closed_groupchat"
            boolean r5 = r6.boolSetting(r5)
            if (r5 == 0) goto L_0x0184
            r5 = 1
            goto L_0x0185
        L_0x0182:
            r16 = r5
        L_0x0184:
            r5 = 0
        L_0x0185:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = r0.generateChatType(r2, r5, r13)
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r19 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.OFF
            if (r9 == 0) goto L_0x018e
            goto L_0x0195
        L_0x018e:
            if (r10 == 0) goto L_0x0195
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r19 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.ON
            r1 = r19
            goto L_0x0197
        L_0x0195:
            r1 = r19
        L_0x0197:
            if (r2 == 0) goto L_0x01a3
            r19 = r6
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r6 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT
            if (r5 != r6) goto L_0x01a0
            goto L_0x01a5
        L_0x01a0:
            r6 = r16
            goto L_0x01ab
        L_0x01a3:
            r19 = r6
        L_0x01a5:
            com.sec.internal.ims.servicemodules.im.ImCache r6 = r0.mCache
            com.sec.internal.ims.servicemodules.im.ImSession r6 = r6.getImSessionByParticipants(r3, r5, r4, r1)
        L_0x01ab:
            if (r2 != 0) goto L_0x01f8
            if (r6 != 0) goto L_0x01f8
            r16 = r2
            int r2 = r1.getId()
            r20 = r5
            r5 = 0
            java.lang.String r2 = com.sec.internal.ims.util.StringIdGenerator.generateChatId(r3, r4, r5, r2)
            com.sec.internal.ims.servicemodules.im.ImCache r5 = r0.mCache
            com.sec.internal.ims.servicemodules.im.ImSession r6 = r5.getImSession(r2)
            if (r6 == 0) goto L_0x01f3
            int r5 = r6.getParticipantsSize()
            r7 = 1
            if (r5 >= r7) goto L_0x01f3
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            java.util.Iterator r7 = r3.iterator()
            java.lang.Object r7 = r7.next()
            com.sec.ims.util.ImsUri r7 = (com.sec.ims.util.ImsUri) r7
            r17 = r3
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant r3 = new com.sec.internal.constants.ims.servicemodules.im.ImParticipant
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r8 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INVITED
            r3.<init>(r2, r8, r7)
            r5.add(r3)
            java.lang.String r8 = LOG_TAG
            r18 = r2
            java.lang.String r2 = "createChat() : error, participant table is empty"
            android.util.Log.e(r8, r2)
            r0.onParticipantsInserted(r6, r5)
            goto L_0x01fe
        L_0x01f3:
            r18 = r2
            r17 = r3
            goto L_0x01fe
        L_0x01f8:
            r16 = r2
            r17 = r3
            r20 = r5
        L_0x01fe:
            r8 = r6
            if (r8 != 0) goto L_0x022b
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r0.mCache
            r3 = r4
            r18 = r4
            r4 = r17
            r5 = r20
            r7 = r19
            r6 = r25
            r0 = r7
            r7 = r26
            r19 = r0
            r0 = r8
            r8 = r27
            r9 = r28
            r10 = r31
            r11 = r1
            r12 = r34
            r13 = r35
            r21 = r1
            r1 = r14
            r14 = r36
            com.sec.internal.ims.servicemodules.im.ImSession r8 = r2.makeNewOutgoingSession(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14)
            r2 = r25
            goto L_0x0236
        L_0x022b:
            r21 = r1
            r18 = r4
            r0 = r8
            r1 = r14
            r2 = r25
            r0.restartSession(r15, r1, r2)
        L_0x0236:
            java.lang.String r0 = "start_session_when_create_groupchat"
            r3 = r19
            boolean r0 = r3.boolSetting(r0)
            if (r0 == 0) goto L_0x024a
            boolean r0 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.isGroupChatIdBasedGroupChat(r20)
            if (r0 == 0) goto L_0x024a
            r8.startSession()
        L_0x024a:
            r0 = r22
            java.util.List<com.sec.internal.ims.servicemodules.im.listener.IChatEventListener> r4 = r0.mChatEventListeners
            java.util.Iterator r4 = r4.iterator()
        L_0x0252:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L_0x0262
            java.lang.Object r5 = r4.next()
            com.sec.internal.ims.servicemodules.im.listener.IChatEventListener r5 = (com.sec.internal.ims.servicemodules.im.listener.IChatEventListener) r5
            r5.onCreateChatSucceeded(r8)
            goto L_0x0252
        L_0x0262:
            java.util.ArrayList r4 = new java.util.ArrayList
            r4.<init>()
            int r5 = r20.getId()
            java.lang.String r5 = java.lang.String.valueOf(r5)
            r4.add(r5)
            java.lang.String r5 = "1"
            java.lang.String r6 = "0"
            if (r16 == 0) goto L_0x027a
            r7 = r5
            goto L_0x027b
        L_0x027a:
            r7 = r6
        L_0x027b:
            r4.add(r7)
            if (r30 == 0) goto L_0x0281
            goto L_0x0282
        L_0x0281:
            r5 = r6
        L_0x0282:
            r4.add(r5)
            r5 = 1073741833(0x40000009, float:2.0000021)
            java.lang.String r6 = r8.getChatId()
            r7 = r23
            r19 = r21
            com.sec.internal.ims.util.ImsUtil.listToDumpFormat(r5, r7, r6, r4)
            return r8
        L_0x0294:
            r2 = r25
            r7 = r1
            r17 = r3
            r18 = r4
            r1 = r14
        L_0x029c:
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "createChat: normalizedParticipants is null or empty"
            android.util.Log.i(r3, r4)
            java.util.List<com.sec.internal.ims.servicemodules.im.listener.IChatEventListener> r3 = r0.mChatEventListeners
            java.util.Iterator r3 = r3.iterator()
        L_0x02a9:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x02bb
            java.lang.Object r4 = r3.next()
            com.sec.internal.ims.servicemodules.im.listener.IChatEventListener r4 = (com.sec.internal.ims.servicemodules.im.listener.IChatEventListener) r4
            com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason r5 = com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason.INVALID
            r4.onCreateChatFailed(r7, r15, r5, r1)
            goto L_0x02a9
        L_0x02bb:
            r3 = 0
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSessionProcessor.lambda$createChat$1$ImSessionProcessor(int, java.util.List, java.lang.String, java.lang.String, int, java.lang.String, boolean, boolean, java.lang.String, boolean, boolean, java.lang.String, java.lang.String, com.sec.ims.util.ImsUri):com.sec.internal.ims.servicemodules.im.ImSession");
    }

    /* access modifiers changed from: protected */
    public void addParticipants(String chatId, List<ImsUri> participants) {
        post(new Runnable(chatId, participants) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ List f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$addParticipants$2$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$addParticipants$2$ImSessionProcessor(String chatId, List participants) {
        this.mGcmHandler.addParticipants(chatId, participants);
    }

    /* access modifiers changed from: protected */
    public void removeParticipants(String chatId, List<ImsUri> participants) {
        post(new Runnable(chatId, participants) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ List f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$removeParticipants$3$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$removeParticipants$3$ImSessionProcessor(String chatId, List participants) {
        this.mGcmHandler.removeParticipants(chatId, participants);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatLeader(String chatId, List<ImsUri> participants) {
        post(new Runnable(chatId, participants) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ List f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$changeGroupChatLeader$4$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$changeGroupChatLeader$4$ImSessionProcessor(String chatId, List participants) {
        this.mGcmHandler.changeGroupChatLeader(chatId, participants);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatSubject(String chatId, String subject) {
        post(new Runnable(chatId, subject) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$changeGroupChatSubject$5$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$changeGroupChatSubject$5$ImSessionProcessor(String chatId, String subject) {
        this.mGcmHandler.changeGroupChatSubject(chatId, subject);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatIcon(String chatId, String icon_path) {
        post(new Runnable(chatId, icon_path) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$changeGroupChatIcon$6$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$changeGroupChatIcon$6$ImSessionProcessor(String chatId, String icon_path) {
        this.mGcmHandler.changeGroupChatIcon(chatId, icon_path);
    }

    /* access modifiers changed from: protected */
    public void changeGroupAlias(String chatId, String alias) {
        post(new Runnable(chatId, alias) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$changeGroupAlias$7$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$changeGroupAlias$7$ImSessionProcessor(String chatId, String alias) {
        this.mGcmHandler.changeGroupAlias(chatId, alias);
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteChats(List<String> list, boolean isLocalWipeout) {
        FutureTask<Boolean> future = new FutureTask<>(new Callable(list, isLocalWipeout) {
            public final /* synthetic */ List f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final Object call() {
                return ImSessionProcessor.this.lambda$deleteChats$8$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
        post(future);
        return future;
    }

    public /* synthetic */ Boolean lambda$deleteChats$8$ImSessionProcessor(List list, boolean isLocalWipeout) throws Exception {
        String str = LOG_TAG;
        Log.i(str, "deleteChats: " + list);
        List<ImSession> deletingGroupChats = new ArrayList<>();
        List<String> deleteAllCid = new ArrayList<>();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String cid = (String) it.next();
            ImSession session = this.mCache.getImSession(cid);
            if (session != null && !session.isGroupChat()) {
                Set<ImSession> allSession = this.mCache.getAllImSessionByParticipants(session.getParticipantsUri(), ChatData.ChatType.ONE_TO_ONE_CHAT);
                if (!allSession.isEmpty()) {
                    for (ImSession tempSession : allSession) {
                        deleteAllCid.add(tempSession.getChatId());
                    }
                }
            }
            deleteAllCid.add(cid);
        }
        for (String cid2 : deleteAllCid) {
            this.mCache.deleteAllMessages(cid2);
            ImSession session2 = this.mCache.getImSession(cid2);
            if (session2 != null) {
                int stateId = session2.getChatStateId();
                int phoneId = this.mImModule.getPhoneIdByIMSI(session2.getOwnImsi());
                boolean waitDeactivating = RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.WAIT_DEACTVAING_DELETE_CHAT);
                String str2 = LOG_TAG;
                Log.i(str2, "deleteChats, stateId=" + stateId);
                ImsUtil.listToDumpFormat(LogClass.IM_DELETE_CHAT, phoneId, cid2);
                if (RcsPolicyManager.getRcsStrategy(phoneId).isDeleteSessionSupported(session2.getChatType(), stateId)) {
                    if (session2.isGroupChat()) {
                        deletingGroupChats.add(session2);
                    } else if (!waitDeactivating) {
                        session2.closeSession();
                        this.mCache.deleteSession(session2);
                    }
                }
            }
        }
        if (!deletingGroupChats.isEmpty()) {
            handleVoluntaryDeparture(deletingGroupChats, false);
        }
        this.mCache.deleteMessagesforCloudSyncUsingChatId(list, isLocalWipeout);
        return true;
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteChatsForUnsubscribe() {
        FutureTask<Boolean> future = new FutureTask<>(new Callable() {
            public final Object call() {
                return ImSessionProcessor.this.lambda$deleteChatsForUnsubscribe$9$ImSessionProcessor();
            }
        });
        post(future);
        return future;
    }

    public /* synthetic */ Boolean lambda$deleteChatsForUnsubscribe$9$ImSessionProcessor() throws Exception {
        Log.i(LOG_TAG, "deleteChatsForUnsubscribe");
        this.mCache.loadImSessionByChatType(true);
        for (ImSession session : this.mCache.getAllImSessions()) {
            if (session != null && session.getChatType() == ChatData.ChatType.REGULAR_GROUP_CHAT) {
                this.mCache.deleteAllMessages(session.getChatId());
                this.mCache.deleteSession(session);
                for (IChatEventListener listener : this.mChatEventListeners) {
                    listener.onChatClosed(session.getChatId(), session.getDirection(), ImSessionClosedReason.LEFT_BY_SERVER);
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteAllChats() {
        FutureTask<Boolean> future = new FutureTask<>(new Callable() {
            public final Object call() {
                return ImSessionProcessor.this.lambda$deleteAllChats$10$ImSessionProcessor();
            }
        });
        post(future);
        return future;
    }

    public /* synthetic */ Boolean lambda$deleteAllChats$10$ImSessionProcessor() throws Exception {
        Log.i(LOG_TAG, "deleteAllChats");
        for (ImSession session : this.mCache.getAllImSessions()) {
            IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()));
            this.mCache.deleteAllMessages(session.getChatId());
            int stateId = session.getChatStateId();
            String str = LOG_TAG;
            Log.i(str, "deleteChats, stateId=" + stateId);
            if (mnoStrategy.isDeleteSessionSupported(session.getChatType(), stateId)) {
                this.mCache.deleteSession(session);
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void answerGcSession(String cid, boolean answer) {
        post(new Runnable(cid, answer) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$answerGcSession$11$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$answerGcSession$11$ImSessionProcessor(String cid, boolean answer) {
        Log.i(LOG_TAG, String.format("answerSession: %s %b", new Object[]{cid, Boolean.valueOf(answer)}));
        ImSession session = this.mCache.getImSession(cid);
        if (session != null) {
            if (answer) {
                session.acceptSession(true);
            } else {
                session.rejectSession();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readMessages(String cid, List<String> list) {
        readMessages(cid, list, false);
    }

    /* access modifiers changed from: protected */
    public void readMessages(String cid, List<String> list, boolean updateOnlyMStore) {
        this.mImdnHandler.readMessages(cid, list, updateOnlyMStore);
    }

    /* access modifiers changed from: protected */
    public void ignoreIncomingMsgSet(String chatId, boolean isIgnore) {
        post(new Runnable(chatId, isIgnore) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$ignoreIncomingMsgSet$12$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$ignoreIncomingMsgSet$12$ImSessionProcessor(String chatId, boolean isIgnore) {
        String str = LOG_TAG;
        Log.i(str, "ignoreIncomingMsgSet: chatId=" + chatId + " isIgnore=" + isIgnore);
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()))) {
                session.getChatData().updateIsMuted(isIgnore);
                this.mImTranslation.onIgnoreIncomingMsgSetResponse(chatId, true);
                return;
            }
        }
        this.mImTranslation.onIgnoreIncomingMsgSetResponse(chatId, false);
    }

    public void sendComposingNotification(String cid, int interval, boolean isTyping) {
        post(new Runnable(cid, interval, isTyping) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$sendComposingNotification$13$ImSessionProcessor(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$sendComposingNotification$13$ImSessionProcessor(String cid, int interval, boolean isTyping) {
        this.mImdnHandler.sendComposingNotification(cid, interval, isTyping);
    }

    /* access modifiers changed from: protected */
    public void acceptChat(String cid, boolean isAccept, int reason) {
        post(new Runnable(cid, isAccept, reason) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$acceptChat$14$ImSessionProcessor(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$acceptChat$14$ImSessionProcessor(String cid, boolean isAccept, int reason) {
        String str = LOG_TAG;
        Log.i(str, "acceptChat: chatId=" + cid + "isAccept=" + isAccept + ", reason=" + reason);
        ImSession session = this.mCache.getImSession(cid);
        if (session == null) {
            Log.e(LOG_TAG, "acceptChat: Session not found in the cache");
            return;
        }
        if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()))) {
            if (isAccept) {
                session.acceptSession(false);
            } else {
                session.rejectSession(reason);
            }
        }
    }

    public void openChat(String cid, boolean hasInvitationUI) {
        post(new Runnable(cid, hasInvitationUI) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$openChat$15$ImSessionProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$openChat$15$ImSessionProcessor(String cid, boolean hasInvitationUI) {
        String str = LOG_TAG;
        Log.i(str, "openChat: chatId=" + cid + ", has Invitation UI=" + hasInvitationUI);
        ImSession session = this.mCache.getImSession(cid);
        if (session == null) {
            Log.e(LOG_TAG, "openChat: Session not found in the cache");
            return;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getChatData().getOwnIMSI());
        if (this.mImModule.isRegistered(phoneId) && !session.isAutoAccept() && this.mImModule.getImConfig(phoneId).getImSessionStart() == ImConstants.ImSessionStart.WHEN_OPENS_CHAT_WINDOW && !hasInvitationUI) {
            session.acceptSession(false);
        }
    }

    /* access modifiers changed from: protected */
    public void closeChat(String cid) {
        List<String> chatList = new ArrayList<>();
        chatList.add(cid);
        post(new Runnable(chatList) {
            public final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$closeChat$16$ImSessionProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$closeChat$16$ImSessionProcessor(List chatList) {
        lambda$closeChat$17$ImSessionProcessor(chatList, true, false);
    }

    /* access modifiers changed from: protected */
    public void closeChat(List<String> chatList, boolean isVoluntary, boolean isDismissGroupChat) {
        post(new Runnable(chatList, isVoluntary, isDismissGroupChat) {
            public final /* synthetic */ List f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$closeChat$17$ImSessionProcessor(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: closeChatInternal */
    public void lambda$closeChat$17$ImSessionProcessor(List<String> chatList, boolean isVoluntary, boolean isDismissGroupChat) {
        String str = LOG_TAG;
        Log.i(str, "closeChatInternal: chatId=" + chatList);
        List<ImSession> leaveSessionList = new ArrayList<>();
        List<ImSession> sessionList = new ArrayList<>();
        for (String cid : chatList) {
            ImSession session = this.mCache.getImSession(cid);
            if (session == null) {
                Log.e(LOG_TAG, "Session not found in the cache.");
            } else {
                sessionList.add(session);
            }
        }
        for (ImSession session2 : sessionList) {
            int phoneId = this.mImModule.getPhoneIdByIMSI(session2.getOwnImsi());
            ImsUtil.listToDumpFormat(LogClass.IM_CLOSE_CHAT, phoneId, session2.getChatId());
            if (isDismissGroupChat) {
                if (isVoluntary && session2.isGroupChat() && RcsPolicyManager.getRcsStrategy(phoneId) != null && (RcsPolicyManager.getRcsStrategy(phoneId) instanceof CmccStrategy)) {
                    session2.updateChatState(ChatData.State.CLOSED_VOLUNTARILY);
                }
                session2.closeSession(true, ImSessionStopReason.GC_FORCE_CLOSE);
            } else if (!isVoluntary) {
                session2.closeSession();
            } else if (session2.isGroupChat()) {
                leaveSessionList.add(session2);
            } else {
                session2.closeSession(true, ImSessionStopReason.VOLUNTARILY);
            }
            this.mCache.removeActiveSession(session2);
        }
        if (!leaveSessionList.isEmpty()) {
            handleVoluntaryDeparture(leaveSessionList, true);
        }
    }

    /* access modifiers changed from: protected */
    public void processRejoinGCSession(int phoneId) {
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        int count = 0;
        String imsi = this.mImModule.getImsiFromPhoneId(phoneId);
        List<ImSession> rejoinSession = new ArrayList<>();
        for (ImSession session : this.mCache.getAllImSessions()) {
            if (session.isAutoRejoinSession()) {
                rejoinSession.add(session);
            }
        }
        int limit = mnoStrategy.intSetting(RcsPolicySettings.RcsPolicy.MAX_SIPINVITE_ATONCE);
        String str = LOG_TAG;
        Log.i(str, "rejoinSession: list size : " + rejoinSession.size() + " limit : " + limit);
        if (limit > 0) {
            Iterator it = CollectionUtils.partition(rejoinSession, limit).iterator();
            while (it.hasNext()) {
                postDelayed(new Runnable((List) it.next(), phoneId, imsi) {
                    public final /* synthetic */ List f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ String f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        ImSessionProcessor.this.lambda$processRejoinGCSession$18$ImSessionProcessor(this.f$1, this.f$2, this.f$3);
                    }
                }, ((long) count) * 1000);
                count++;
            }
            return;
        }
        for (ImSession session2 : rejoinSession) {
            if (this.mImModule.isRegistered(phoneId) && session2.isGroupChat() && TextUtils.equals(session2.getChatData().getOwnIMSI(), imsi)) {
                session2.processRejoinGCSession();
            }
        }
    }

    public /* synthetic */ void lambda$processRejoinGCSession$18$ImSessionProcessor(List l, int phoneId, String imsi) {
        Iterator it = l.iterator();
        while (it.hasNext()) {
            ImSession session = (ImSession) it.next();
            if (this.mImModule.isRegistered(phoneId) && session.isGroupChat() && TextUtils.equals(session.getChatData().getOwnIMSI(), imsi)) {
                session.processRejoinGCSession();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onConferenceInfoUpdated(ImSessionConferenceInfoUpdateEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onConferenceInfoUpdated: " + event);
        ImSession session = this.mCache.getImSession(event.mChatId);
        if (session == null) {
            Log.e(LOG_TAG, "onConferenceInfoUpdated: Session not found.");
        } else if (session.getChatType() == ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT) {
            Log.i(LOG_TAG, "onConferenceInfoUpdated: ignore the event.");
        } else {
            session.receiveConferenceInfo(event);
        }
    }

    /* access modifiers changed from: protected */
    public void onSessionEstablished(ImSessionEstablishedEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onSessionEstablished: " + event);
        ImSession session = this.mCache.getImSession(event.mChatId);
        List<String> dumps = new ArrayList<>();
        if (session == null) {
            Log.e(LOG_TAG, "onSessionEstablished: Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onSessionEstablished: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId());
        dumps.add(session.getConversationId() != null ? session.getConversationId() : MessageContextValues.none);
        session.receiveSessionEstablished(event);
        ImsUtil.listToDumpFormat(LogClass.IM_SESSION_ESTABLISHED, session.getPhoneId(), event.mChatId, dumps);
    }

    /* access modifiers changed from: protected */
    public void onSessionClosed(ImSessionClosedEvent event) {
        ImSession session;
        String str = LOG_TAG;
        Log.i(str, "onSessionClosed: " + event);
        if (event.mChatId == null) {
            session = this.mCache.getImSessionByRawHandle(event.mRawHandle);
        } else {
            session = this.mCache.getImSession(event.mChatId);
        }
        if (session == null) {
            Log.e(LOG_TAG, "onSessionClosed: Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onSessionClosed: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + event.mResult.toString());
        List<String> dumps = new ArrayList<>();
        dumps.add(String.valueOf(event.mResult.getType().ordinal()));
        ImsUtil.listToDumpFormat(LogClass.IM_SESSION_CLOSED, session.getPhoneId(), event.mChatId, dumps);
        session.receiveSessionClosed(event);
    }

    /* access modifiers changed from: protected */
    public void onIncomingSessionReceived(ImIncomingSessionEvent event) {
        new Thread(new Runnable(event) {
            public final /* synthetic */ ImIncomingSessionEvent f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$onIncomingSessionReceived$19$ImSessionProcessor(this.f$1);
            }
        }).start();
    }

    public /* synthetic */ void lambda$onIncomingSessionReceived$19$ImSessionProcessor(ImIncomingSessionEvent event) {
        ChatMode chatMode;
        ImIncomingSessionEvent imIncomingSessionEvent = event;
        this.mImModule.acquireWakeLock(imIncomingSessionEvent.mRawHandle);
        if (!TextUtils.isEmpty(imIncomingSessionEvent.mServiceId)) {
            this.mImModule.releaseWakeLock(imIncomingSessionEvent.mRawHandle);
            return;
        }
        Log.i(LOG_TAG, "onIncomingSessionReceived: " + imIncomingSessionEvent);
        this.mImModule.getImDump().addEventLogs("onIncomingSessionReceived: convId=" + imIncomingSessionEvent.mConversationId + ", contId=" + imIncomingSessionEvent.mContributionId);
        int phoneId = this.mImModule.getPhoneIdByIMSI(imIncomingSessionEvent.mOwnImsi);
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        Set<ImsUri> normalizedParticipants = getNormalizedParticipants(phoneId, imIncomingSessionEvent.mRecipients, imIncomingSessionEvent.mInitiator);
        boolean z = true;
        boolean isGroupChat = normalizedParticipants.size() > 1 || imIncomingSessionEvent.mSessionType == ImIncomingSessionEvent.ImSessionType.CONFERENCE;
        ImSessionRejectReason rejectReason = checkForRejectIncomingSession(phoneId, isGroupChat, imIncomingSessionEvent.mIsClosedGroupChat);
        if (rejectReason != null) {
            Log.i(LOG_TAG, "onIncomingSessionReceived: reject");
            this.mImService.rejectImSession(new RejectImSessionParams((String) null, imIncomingSessionEvent.mRawHandle, rejectReason, (Message) null));
            this.mImModule.releaseWakeLock(imIncomingSessionEvent.mRawHandle);
            return;
        }
        if (!imIncomingSessionEvent.mIsClosedGroupChat || !mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.PARTICIPANTBASED_CLOSED_GROUPCHAT)) {
            z = false;
        }
        ChatData.ChatType chatType = generateChatType(isGroupChat, z, false);
        String senderUri = imIncomingSessionEvent.mInitiator.getUriType() == ImsUri.UriType.SIP_URI ? imIncomingSessionEvent.mInitiator.toString() : imIncomingSessionEvent.mInitiator.getMsisdn();
        imIncomingSessionEvent.mFromBlocked = checkFromBlocked(phoneId, isGroupChat, imIncomingSessionEvent.mInitiator, senderUri);
        synchronized (this) {
            try {
                String str = imIncomingSessionEvent.mOwnImsi;
                String str2 = imIncomingSessionEvent.mPrevContributionId;
                String senderUri2 = imIncomingSessionEvent.mContributionId;
                String str3 = imIncomingSessionEvent.mConversationId;
                if (imIncomingSessionEvent.mIsTokenUsed) {
                    try {
                        chatMode = ChatMode.ON;
                    } catch (Throwable th) {
                        th = th;
                        String str4 = senderUri;
                        ImSessionRejectReason imSessionRejectReason = rejectReason;
                        boolean z2 = isGroupChat;
                        IMnoStrategy iMnoStrategy = mnoStrategy;
                        ChatData.ChatType chatType2 = chatType;
                    }
                } else {
                    chatMode = ChatMode.OFF;
                }
                String str5 = str3;
                ChatData.ChatType chatType3 = chatType;
                String str6 = senderUri;
                IMnoStrategy iMnoStrategy2 = mnoStrategy;
                ChatData.ChatType chatType4 = chatType;
                String str7 = str5;
                ImSessionRejectReason imSessionRejectReason2 = rejectReason;
                boolean isGroupChat2 = isGroupChat;
                ImSession session = findSession(phoneId, str, isGroupChat, chatType3, str2, senderUri2, str7, normalizedParticipants, chatMode);
                this.mGcmHandler.updateParticipants(session, normalizedParticipants);
                if (session == null) {
                    if (imIncomingSessionEvent.mIsForStoredNoti) {
                        Log.i(LOG_TAG, "onIncomingSessionReceived: no session. accept rcse-standfw invite");
                        this.mImService.acceptImSession(new AcceptImSessionParams((String) null, this.mImModule.getUserAlias(phoneId), imIncomingSessionEvent.mRawHandle, true, (Message) null));
                        this.mImModule.releaseWakeLock(imIncomingSessionEvent.mRawHandle);
                        return;
                    }
                    Log.i(LOG_TAG, "onIncomingSessionReceived: Make new incoming session.");
                    session = this.mCache.makeNewIncomingSession(imIncomingSessionEvent, normalizedParticipants, chatType4, imIncomingSessionEvent.mIsTokenUsed ? ChatMode.ON : ChatMode.OFF);
                    if (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.CHECK_INITIATOR_SESSIONURI) && isGroupChat2 && this.mImModule.normalizeUri(phoneId, imIncomingSessionEvent.mInitiator).equals(this.mImModule.normalizeUri(phoneId, imIncomingSessionEvent.mSessionUri))) {
                        session.updateChatState(ChatData.State.CLOSED_BY_USER);
                    }
                }
                this.mImModule.getImDump().dumpIncomingSession(phoneId, session, imIncomingSessionEvent.mIsDeferred, imIncomingSessionEvent.mIsForStoredNoti);
                if (this.mImModule.getImConfig(phoneId).getUserAliasEnabled() && !isGroupChat2) {
                    session.setInitiatorAlias(imIncomingSessionEvent.mInitiatorAlias);
                }
                if (isGroupChat2) {
                    session.setInitiator(this.mImModule.normalizeUri(phoneId, imIncomingSessionEvent.mInitiator));
                } else if (imIncomingSessionEvent.mIsChatbotRole) {
                    Log.i(LOG_TAG, "onIncomingSessionReceived: event.mIsChatbotRole=true, event.mInitiator=" + IMSLog.numberChecker(imIncomingSessionEvent.mInitiator));
                    ChatbotUriUtil.removeUriParameters(imIncomingSessionEvent.mInitiator);
                    session.setInitiator(imIncomingSessionEvent.mInitiator);
                    int delayForA2P = RcsPolicyManager.getRcsStrategy(phoneId).intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_DEREGI_FOR_A2P_SESSION);
                    if (delayForA2P > 0) {
                        processIncomingSessionForA2P(phoneId, delayForA2P);
                    }
                }
                session.setIsTokenUsed(imIncomingSessionEvent.mIsTokenUsed);
                session.setDeviceId(imIncomingSessionEvent.mDeviceId);
                if (!imIncomingSessionEvent.mIsDeferred) {
                    session.setNetworkFallbackMech(imIncomingSessionEvent.mIsMsgFallbackSupported, imIncomingSessionEvent.mIsMsgRevokeSupported);
                    session.mRemoteAcceptTypes = imIncomingSessionEvent.mAcceptTypes;
                    session.mRemoteAcceptWrappedTypes = imIncomingSessionEvent.mAcceptWrappedTypes;
                }
                session.updateIsChatbotRole(imIncomingSessionEvent.mIsChatbotRole);
                session.processIncomingSession(imIncomingSessionEvent);
                if (imIncomingSessionEvent.mReceivedMessage != null) {
                    this.mImModule.updateServiceAvailability(imIncomingSessionEvent.mOwnImsi, imIncomingSessionEvent.mReceivedMessage.mSender, imIncomingSessionEvent.mReceivedMessage.mImdnTime);
                }
                this.mImModule.releaseWakeLock(imIncomingSessionEvent.mRawHandle);
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onIncomingSlmLMMSessionReceived(SlmLMMIncomingSessionEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onIncomingSlmLMMSessionReceived: " + event);
        this.mSlmService.acceptSlmLMMSession(new AcceptSlmLMMSessionParams((String) null, this.mImModule.getUserAlias(this.mImModule.getPhoneIdByIMSI(event.mOwnImsi)), event.mRawHandle, (Message) null, event.mOwnImsi));
    }

    /* access modifiers changed from: protected */
    public Set<ImsUri> getNormalizedParticipants(int phoneId, List<ImsUri> recipients, ImsUri initiator) {
        Set<ImsUri> normalizedParticipants;
        if (recipients != null) {
            normalizedParticipants = this.mImModule.normalizeUri(phoneId, (Collection<ImsUri>) recipients);
        } else {
            normalizedParticipants = new HashSet<>();
        }
        removeOwnNumberFromParticipants(normalizedParticipants, this.mImModule.normalizeUri(phoneId, initiator), phoneId);
        return normalizedParticipants;
    }

    /* access modifiers changed from: protected */
    public void removeOwnNumberFromParticipants(Set<ImsUri> participants, ImsUri sender, int phoneId) {
        String str = LOG_TAG;
        IMSLog.s(str, "removeOwnNumberFromParticipants participants=" + participants + " ,sender=" + sender);
        if (sender != null) {
            participants.add(sender);
        }
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration(phoneId);
        if (imsRegistration != null) {
            List<ImsUri> ownUris = new ArrayList<>();
            for (NameAddr addr : imsRegistration.getImpuList()) {
                ownUris.add(this.mImModule.normalizeUri(phoneId, addr.getUri()));
            }
            if (participants.size() > 1) {
                participants.removeAll(ownUris);
            }
        }
    }

    /* access modifiers changed from: protected */
    public ImSession getImSession(String chatId) {
        return this.mCache.getImSession(chatId);
    }

    /* access modifiers changed from: protected */
    public void onMessageSendingSucceeded(MessageBase msg) {
        Object cldMsgServiceObj;
        this.mImBigDataProcessor.sendRCSMInfoToHQM(msg, "0", (String) null, (Result.Type) null, (IMnoStrategy.StatusCode) null);
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session == null) {
            Log.e(LOG_TAG, "onMessageSendingSucceeded: session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onMessageSendingSucceeded: type= " + msg.getType() + ", chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", imdnId=" + msg.getImdnId());
        if (!isReportMsg(msg)) {
            for (IMessageEventListener listener : this.mImProcessor.getMessageEventListener(msg.getType())) {
                listener.onMessageSendingSucceeded(msg);
            }
            if (msg.isTemporary()) {
                this.mCache.deleteMessage(msg.getId());
                return;
            }
            if (msg.getRevocationStatus() != ImConstants.RevocationStatus.AVAILABLE) {
                this.mImModule.removeFromPendingListWithDelay(msg.getId());
            }
            IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()));
            if (mnoStrategy != null && mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.CENTRAL_MSG_STORE)) {
                String str = LOG_TAG;
                Log.i(str, "onMessageSendingSucceeded for cloud sync: " + msg.getId());
                try {
                    Class<?> cldMsgServiceClass = Class.forName("com.sec.internal.ims.cmstore.CloudMessageServiceWrapper");
                    if (cldMsgServiceClass != null && (cldMsgServiceObj = cldMsgServiceClass.getMethod("getInstance", new Class[]{Context.class}).invoke((Object) null, new Object[]{this.mContext})) != null) {
                        ReflectionUtils.invoke(cldMsgServiceClass.getMethod("sentRCSMessage", new Class[]{Integer.TYPE, String.class, String.class}), cldMsgServiceObj, new Object[]{Integer.valueOf(msg.getId()), msg.getImdnId(), null});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (msg.getReportMsgParams() != null) {
            this.mCache.deleteMessage(msg.getId());
            this.mImTranslation.onMessageReportResponse(Long.valueOf((long) msg.getReportMsgParams().getSpamMsgId()), true);
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        if (msg == null) {
            Log.e(LOG_TAG, "onMessageSendingFailed: msg is null.");
        } else if (msg.isTemporary()) {
            Log.i(LOG_TAG, "onMessageSendingFailed: temporary message.");
            for (IMessageEventListener listener : this.mImProcessor.getMessageEventListener(msg.getType())) {
                listener.onMessageSendingFailed(msg, new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE), (Result) null);
            }
            this.mCache.deleteMessage(msg.getId());
        } else {
            ImSession session = this.mCache.getImSession(msg.getChatId());
            if (result != null && result.getImError() == ImError.ENGINE_ERROR) {
                ImModule imModule = this.mImModule;
                if (!imModule.isRegistered(imModule.getPhoneIdByIMSI(msg.getOwnIMSI())) && session != null && !session.isGroupChat()) {
                    Log.e(LOG_TAG, "onMessageSendingFailed: engine error and deregistered. fallback to legacy.");
                    strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
                }
            }
            if (!(result == null || result.getType() == Result.Type.NONE)) {
                this.mImBigDataProcessor.onMessageSendingFailed(msg, strategyResponse, result);
            }
            if (session == null) {
                Log.e(LOG_TAG, "onMessageSendingFailed: session not found.");
                return;
            }
            this.mImModule.getImDump().dumpMessageSendingFailed(this.mImModule.getPhoneIdByChatId(msg.getChatId()), session, result, msg.mImdnId, strategyResponse.getStatusCode().toString());
            if (!isReportMsg(msg) || msg.getReportMsgParams() == null) {
                if (ImsGateConfig.isGateEnabled()) {
                    IMSLog.g("GATE", "<GATE-M>MMS_ERROR</GATE-M>");
                }
                ImDump imDump = this.mImModule.getImDump();
                StringBuilder sb = new StringBuilder();
                sb.append("onMessageSendingFailed: type=");
                sb.append(msg.getType());
                sb.append("chatId=");
                sb.append(session.getChatId());
                sb.append(", convId=");
                sb.append(session.getConversationId());
                sb.append(", contId=");
                sb.append(session.getContributionId());
                sb.append(", imdnId=");
                sb.append(msg.getImdnId());
                sb.append("result=");
                sb.append(result != null ? result.toString() : "");
                sb.append(", required_action=");
                sb.append(strategyResponse.getStatusCode().toString());
                imDump.addEventLogs(sb.toString());
                if (msg instanceof ImMessage) {
                    String str = LOG_TAG;
                    Log.e(str, "onMessageSendingFailed ImMessage: id=" + msg.getId() + ", strategy=" + strategyResponse + ", result=" + result);
                    for (IMessageEventListener listener2 : this.mImProcessor.getMessageEventListener(msg.getType())) {
                        listener2.onMessageSendingFailed(msg, strategyResponse, result);
                    }
                } else if (msg instanceof FtMessage) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "onMessageSendingFailed FtMessage: id=" + msg.getId() + ", strategy=" + strategyResponse + ", result=" + result);
                    for (IFtEventListener listener3 : this.mFtProcessor.getFtEventListener(msg.getType())) {
                        listener3.onMessageSendingFailed(msg, strategyResponse, result);
                    }
                }
                this.mCache.removeFromPendingList(msg.getId());
                return;
            }
            this.mCache.deleteMessage(msg.getId());
            this.mImTranslation.onMessageReportResponse(Long.valueOf((long) msg.getReportMsgParams().getSpamMsgId()), false);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyImSessionEstablished(int phoneId) {
        if (this.mCache.isEstablishedSessionExist() || this.mCache.hasFileTransferInprogress()) {
            post(new Runnable(phoneId) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ImSessionProcessor.this.lambda$notifyImSessionEstablished$20$ImSessionProcessor(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$notifyImSessionEstablished$20$ImSessionProcessor(int phoneId) {
        Log.i(LOG_TAG, "notifyImSessionEstablished");
        if (this.mImSessionListener.containsKey(Integer.valueOf(phoneId))) {
            Iterator<IImSessionListener> it = this.mImSessionListener.get(Integer.valueOf(phoneId)).iterator();
            while (it.hasNext()) {
                try {
                    it.next().onImSessionEstablished(true);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "notifyImSessionEstablished failed to send IImSessionListener.onImSessionEstablished");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyImSessionClosed(int phoneId) {
        if (!this.mCache.isEstablishedSessionExist() && !this.mCache.hasFileTransferInprogress()) {
            post(new Runnable(phoneId) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ImSessionProcessor.this.lambda$notifyImSessionClosed$21$ImSessionProcessor(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$notifyImSessionClosed$21$ImSessionProcessor(int phoneId) {
        Log.i(LOG_TAG, "notifyImSessionClosed");
        if (this.mImModule.getImsRegistration() != null) {
            ImsRegistry.getRegistrationManager().doPendingUpdateRegistration();
        }
        if (this.mImSessionListener.containsKey(Integer.valueOf(phoneId))) {
            Iterator<IImSessionListener> it = this.mImSessionListener.get(Integer.valueOf(phoneId)).iterator();
            while (it.hasNext()) {
                try {
                    it.next().onImSessionEstablished(false);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "notifyImSessionClosed failed to send IImSessionListener.onImSessionEstablished");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasEstablishedSession() {
        boolean hasSession = false;
        if (this.mImModule.getImsRegistration() != null && (this.mCache.isEstablishedSessionExist() || this.mCache.hasFileTransferInprogress())) {
            hasSession = true;
        }
        String str = LOG_TAG;
        Log.i(str, "hasEstablishedSession : " + hasSession);
        return hasSession;
    }

    /* access modifiers changed from: protected */
    public void receiveDeliveryTimeout(String chatId) {
        post(new Runnable(chatId) {
            public final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImSessionProcessor.this.lambda$receiveDeliveryTimeout$22$ImSessionProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$receiveDeliveryTimeout$22$ImSessionProcessor(String chatId) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            Log.i(LOG_TAG, "receiveDeliveryTimeout session not found");
        } else {
            session.receiveDeliveryTimeout();
        }
    }

    private void makeNewSystemUserMessage(ImSession session, Map<ImParticipant, Date> participants, ImConstants.Type type) {
        Map<Date, StringBuilder> dateList = new TreeMap<>();
        Set<String> participantSet = new HashSet<>();
        for (Map.Entry<ImParticipant, Date> entry : participants.entrySet()) {
            Date date = entry.getValue();
            String uri = entry.getKey().getUri().toString();
            if (date != null) {
                StringBuilder builder = dateList.get(date);
                if (builder == null) {
                    dateList.put(date, new StringBuilder().append(uri));
                } else {
                    builder.append(";");
                    builder.append(uri);
                }
            } else {
                participantSet.add(uri);
            }
        }
        for (Map.Entry<Date, StringBuilder> entry2 : dateList.entrySet()) {
            this.mCache.makeNewSystemUserMessage(session, entry2.getValue().toString(), type, entry2.getKey());
        }
        if (!participantSet.isEmpty()) {
            this.mCache.makeNewSystemUserMessage(session, TextUtils.join(";", participantSet), type);
        }
    }

    private void handleVoluntaryDeparture(List<ImSession> chats, boolean isReusable) {
        if (chats != null && !chats.isEmpty()) {
            for (ImSession chat : chats) {
                int phoneId = this.mImModule.getPhoneIdByIMSI(chat.getOwnImsi());
                if (!chat.isEmptySession()) {
                    chat.getChatData().updateIsReusable(isReusable);
                    chat.updateChatState(ChatData.State.CLOSED_VOLUNTARILY);
                    if (this.mImModule.isOwnNumberChanged(chat)) {
                        if (!isReusable) {
                            this.mCache.deleteSession(chat);
                        }
                    } else if (!this.mImModule.isRegistered(phoneId)) {
                        for (MessageBase noti : this.mCache.getMessagesForPendingNotificationByChatId(chat.getChatId())) {
                            noti.updateDesiredNotificationStatus(NotificationStatus.NONE);
                        }
                        chat.processCancelMessages(false, (ImError) null);
                    } else if (chat.isEstablishedState()) {
                        chat.closeSession(isReusable, ImSessionStopReason.VOLUNTARILY);
                    } else {
                        this.mGroupChatsForDeparture.add(chat);
                    }
                } else if (!isReusable) {
                    this.mCache.deleteSession(chat);
                }
            }
        }
        if (!this.mGroupChatsForDeparture.isEmpty() && !hasMessages(1)) {
            handleEventVoluntaryDeparture();
        }
    }

    /* access modifiers changed from: protected */
    public void handleEventVoluntaryDeparture() {
        String str = LOG_TAG;
        Log.i(str, "handleEventVoluntaryDeparture: mGroupChatsForDeparture size=" + this.mGroupChatsForDeparture.size());
        if (!this.mGroupChatsForDeparture.isEmpty()) {
            int limit = this.mImModule.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.MAX_SIPINVITE_ATONCE);
            if (limit <= 0 || !this.mImModule.isRegistered()) {
                for (ImSession chat : this.mGroupChatsForDeparture) {
                    if (this.mImModule.isRegistered()) {
                        chat.closeSession(chat.isReusable(), ImSessionStopReason.VOLUNTARILY);
                    } else {
                        for (IChatEventListener listener : this.mChatEventListeners) {
                            listener.onChatClosed(chat.getChatId(), chat.getDirection(), ImSessionClosedReason.LEAVE_SESSION_PENDING);
                        }
                    }
                }
                this.mGroupChatsForDeparture.clear();
                return;
            }
            List<ImSession> list = CollectionUtils.partition(this.mGroupChatsForDeparture, limit).get(0);
            for (ImSession chat2 : list) {
                chat2.closeSession(chat2.isReusable(), ImSessionStopReason.VOLUNTARILY);
            }
            this.mGroupChatsForDeparture.removeAll(list);
            if (!this.mGroupChatsForDeparture.isEmpty()) {
                removeMessages(1);
                sendEmptyMessageDelayed(1, 1000);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleEventBlocklistChanged() {
        Log.i(LOG_TAG, "handleEventBlocklistChanged()");
        for (String number : BlockedNumberUtil.getBlockedNumbersList(this.mContext)) {
            Set<ImsUri> participants = new HashSet<>();
            ImsUri normalizedUri = this.mImModule.getUriGenerator(SimUtil.getSimSlotPriority()).getNormalizedUri(number, true);
            if (normalizedUri != null) {
                participants.add(normalizedUri);
                ImSession session = this.mCache.getImSessionByParticipants(participants, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
                if (session != null && session.getDetailedState() == ImSession.SessionState.ESTABLISHED) {
                    session.closeSession();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public List<String> getMessageIdsForDisplayAggregation(String chatId, ImDirection direction, Long timestamp) {
        return this.mCache.getMessageIdsForDisplayAggregation(chatId, direction, timestamp);
    }

    /* access modifiers changed from: protected */
    public void registerImSessionListenerByPhoneId(IImSessionListener listener, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "registerImSessionListener phoneId = " + phoneId);
        if (listener != null) {
            if (!this.mImSessionListener.containsKey(Integer.valueOf(phoneId))) {
                this.mImSessionListener.put(Integer.valueOf(phoneId), new ArrayList());
            }
            ArrayList<IImSessionListener> list = this.mImSessionListener.get(Integer.valueOf(phoneId));
            if (list != null) {
                list.add(listener);
            }
            notifyImSessionEstablished(phoneId);
            return;
        }
        Log.e(LOG_TAG, "no registerImSessionListener and not work");
    }

    /* access modifiers changed from: protected */
    public void unregisterImSessionListenerByPhoneId(IImSessionListener listener, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "unregisterImSessionListener phoneId = " + phoneId);
        if (this.mImSessionListener.containsKey(Integer.valueOf(phoneId))) {
            this.mImSessionListener.get(Integer.valueOf(phoneId)).remove(listener);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReportMsg(MessageBase msg) {
        ImsUri psi = ImsUri.parse(RcsPolicyManager.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(msg.getOwnIMSI())).stringSetting(RcsPolicySettings.RcsPolicy.ONEKEY_REPORT_PSI));
        return (psi == null || msg.getRemoteUri() == null || !psi.equals(msg.getRemoteUri())) ? false : true;
    }

    /* access modifiers changed from: protected */
    public ImBigDataProcessor getBigDataProcessor() {
        return this.mImBigDataProcessor;
    }

    public void setLegacyLatching(ImsUri uri, boolean b, String imsi) {
        this.mImRevocationHandler.setLegacyLatching(uri, b, imsi);
    }

    public void onMessageRevokeTimerExpired(String chatId, Collection<String> imdnIds, String imsi) {
        this.mImRevocationHandler.onMessageRevokeTimerExpired(chatId, imdnIds, imsi);
    }

    public void onMessageRevocationDone(ImConstants.RevocationStatus status, Collection<MessageBase> messages, ImSession session) {
        this.mImRevocationHandler.onMessageRevocationDone(status, messages, session);
    }

    public void addToRevokingMessages(String imdnId, String chatId) {
        this.mImRevocationHandler.addToRevokingMessages(imdnId, chatId);
    }

    public void removeFromRevokingMessages(Collection<String> imdnIds) {
        this.mImRevocationHandler.removeFromRevokingMessages(imdnIds);
    }

    /* access modifiers changed from: protected */
    public List<IChatEventListener> getChatEventListeners() {
        return this.mChatEventListeners;
    }

    /* access modifiers changed from: protected */
    public ImRevocationHandler getImRevocationHandler() {
        return this.mImRevocationHandler;
    }

    /* access modifiers changed from: protected */
    public Collection<IMessageEventListener> getMessageEventListener(ImConstants.Type type) {
        return this.mImProcessor.getMessageEventListener(type);
    }

    /* access modifiers changed from: protected */
    public Collection<IFtEventListener> getFtEventListener(ImConstants.Type type) {
        return this.mFtProcessor.getFtEventListener(type);
    }

    /* access modifiers changed from: protected */
    public ChatData.ChatType generateChatType(boolean isGroupChat, boolean isParticipantsBasedGC, boolean isBroadcastChat) {
        if (isBroadcastChat) {
            return ChatData.ChatType.ONE_TO_MANY_CHAT;
        }
        if (!isGroupChat) {
            return ChatData.ChatType.ONE_TO_ONE_CHAT;
        }
        if (isParticipantsBasedGC) {
            return ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT;
        }
        return ChatData.ChatType.REGULAR_GROUP_CHAT;
    }

    private boolean checkFromBlocked(int phoneId, boolean isGroupChat, ImsUri initiator, String senderUri) {
        if (((isGroupChat || !RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.BLOCK_MSG)) && (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.SKIP_BLOCK_CHATBOT_MSG) || !ChatbotUriUtil.isChatbotUri(initiator, phoneId))) || !BlockedNumberUtil.isBlockedNumber(this.mContext, senderUri)) {
            return false;
        }
        String str = LOG_TAG;
        IMSLog.s(str, "Incoming session from blocked number (" + senderUri + ") - reject");
        return true;
    }

    private ImSessionRejectReason checkForRejectIncomingSession(int phoneId, boolean isGroupChat, boolean isCGC) {
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.CHECK_MSGAPP_IMSESSION_REJECT) && !this.mImModule.isDefaultMessageAppInUse()) {
            Log.e(LOG_TAG, "checkForRejectIncomingSession: default message app is not samsung");
            return ImSessionRejectReason.INVOLUNTARILY;
        } else if (mnoStrategy.checkMainSwitchOff(this.mContext, phoneId)) {
            Log.e(LOG_TAG, "checkForRejectIncomingSession: main Switch Off");
            return isGroupChat ? ImSessionRejectReason.INVOLUNTARILY : ImSessionRejectReason.TEMPORARILY_UNAVAILABLE;
        } else if (isCGC && !RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.PARTICIPANTBASED_CLOSED_GROUPCHAT)) {
            Log.e(LOG_TAG, "checkForRejectIncomingSession: group chat type mismatched");
            return ImSessionRejectReason.VOLUNTARILY;
        } else if (!DeviceUtil.getGcfMode() || !mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.AUTH_BASED_SESSION_CONTROL) || this.mImModule.getImConfig(phoneId).getGroupChatEnabled()) {
            return null;
        } else {
            Log.e(LOG_TAG, "GroupChatAuth is disabled");
            return ImSessionRejectReason.NOT_ACCEPTABLE_HERE;
        }
    }

    /* access modifiers changed from: protected */
    public ImSession findSession(int phoneId, String ownImsi, boolean isGroupChat, ChatData.ChatType chatType, String prevContributionId, String contributionId, String conversationId, Set<ImsUri> normalizedParticipants, ChatMode chatMode) {
        ImSession session = null;
        if (!isGroupChat) {
            return this.mCache.getImSessionByParticipants(normalizedParticipants, ChatData.ChatType.ONE_TO_ONE_CHAT, ownImsi, chatMode);
        }
        if (!TextUtils.isEmpty(prevContributionId)) {
            session = this.mCache.getImSessionByContributionId(ownImsi, prevContributionId, false);
        }
        if (session == null) {
            if (this.mImModule.getImConfig(phoneId).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
                session = this.mCache.getImSessionByConversationId(ownImsi, conversationId, isGroupChat);
            } else {
                session = this.mCache.getImSessionByContributionId(ownImsi, contributionId, isGroupChat);
            }
        }
        if (session == null && chatType == ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT) {
            return this.mCache.getImSessionByParticipants(normalizedParticipants, ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT, ownImsi, ChatMode.OFF);
        }
        return session;
    }

    private void processIncomingSessionForA2P(int phoneId, int delay) {
        String str = LOG_TAG;
        Log.i(str, "processIncomingSessionForA2P: phoneId = " + phoneId + ", delay = " + delay);
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration(phoneId);
        if (imsRegistration != null && imsRegistration.getRegiRat() != 18) {
            removeMessages(2);
            this.mImModule.mHasIncomingSessionForA2P.put(phoneId, true);
            sendMessageDelayed(obtainMessage(2, Integer.valueOf(phoneId)), ((long) delay) * 1000);
        }
    }
}
