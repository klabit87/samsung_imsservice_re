package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.GroupChat;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.chat.IChatService;
import com.gsma.services.rcs.chat.IChatServiceConfiguration;
import com.gsma.services.rcs.chat.IGroupChat;
import com.gsma.services.rcs.chat.IGroupChatListener;
import com.gsma.services.rcs.chat.IOneToOneChat;
import com.gsma.services.rcs.chat.IOneToOneChatListener;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneChatEventBroadcaster;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class ChatServiceImpl extends IChatService.Stub implements IChatEventListener, IMessageEventListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = ChatServiceImpl.class.getSimpleName();
    public static final String SUBJECT = "chat";
    private static Hashtable<String, IOneToOneChat> mChatSessions = new Hashtable<>();
    private static Hashtable<String, IGroupChat> mGroupChatSessions = new Hashtable<>();
    private Context mContext = null;
    private RemoteCallbackList<IGroupChatListener> mGroupChatListeners = new RemoteCallbackList<>();
    private IImModule mImModule = null;
    private Object mLock = new Object();
    private OneToOneChatEventBroadcaster mOneToOneChatEventBroadcaster = null;
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public ChatServiceImpl(Context context, IImModule service) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(service);
        this.mOneToOneChatEventBroadcaster = new OneToOneChatEventBroadcaster(context);
        this.mImModule = service;
        this.mContext = context;
        service.registerChatEventListener(this);
        this.mImModule.registerMessageEventListener(ImConstants.Type.TEXT, this);
        this.mImModule.registerMessageEventListener(ImConstants.Type.LOCATION, this);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager manager = ImsRegistry.getRegistrationManager();
        if (manager == null) {
            return false;
        }
        for (ImsRegistration reg : manager.getRegistrationInfo()) {
            if (reg.hasService("im")) {
                return true;
            }
        }
        return false;
    }

    public void addEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.register(listener);
        }
    }

    public void removeEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.unregister(listener);
        }
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        synchronized (this.mLock) {
            int N = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public void notifyGroupChatStateChanged(String chatId, GroupChat.State state, GroupChat.ReasonCode reasonCode) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("notifyGroupChateStateChanged  chatId = ");
        sb.append(chatId);
        sb.append(", state = ");
        sb.append(state.name());
        sb.append(",  reasonCode = ");
        sb.append(reasonCode == null ? "" : reasonCode.name());
        Log.i(str, sb.toString());
        synchronized (this.mLock) {
            if (mGroupChatSessions.get(chatId) == null) {
                Log.i(LOG_TAG, "notifyMessageGroupDeliveryInfoChanged: Not group chat, drop out");
                return;
            }
            int N = this.mGroupChatListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (reasonCode != null) {
                    try {
                        this.mGroupChatListeners.getBroadcastItem(i).onStateChanged(chatId, state.ordinal(), reasonCode.ordinal());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.mGroupChatListeners.finishBroadcast();
        }
    }

    public void notifyGroupMessageStateChanged(MessageBase msg, ChatLog.Message.Content.Status status, ChatLog.Message.Content.ReasonCode reasonCode) {
        Log.i(LOG_TAG, "notifyGroupMessageStateChanged");
        synchronized (this.mLock) {
            String msgId = String.valueOf(msg.getId());
            String chatId = msg.getChatId();
            String contentType = msg.getContentType();
            ImSession session = this.mImModule.getImSession(chatId);
            if (session == null || session.isGroupChat()) {
                int N = this.mGroupChatListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        this.mGroupChatListeners.getBroadcastItem(i).onMessageStatusChanged(chatId, contentType, msgId, status.ordinal(), reasonCode.ordinal());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.mGroupChatListeners.finishBroadcast();
                return;
            }
            Log.i(LOG_TAG, "notifyMessageGroupDeliveryInfoChanged: Not group chat, drop out");
        }
    }

    public void notifyMessageGroupDeliveryInfoChanged(ImMessage msg, ImsUri remoteUri, GroupDeliveryInfo.Status status, GroupDeliveryInfo.ReasonCode reasonCode) {
        Log.i(LOG_TAG, "notifyGroupDeliveryInfoChanged");
        synchronized (this.mLock) {
            String chatId = msg.getChatId();
            if (this.mImModule.getImSession(chatId) == null) {
                Log.i(LOG_TAG, "notifyMessageGroupDeliveryInfoChanged: Session is null, drop out");
                return;
            }
            String msgId = String.valueOf(msg.getId());
            String contentType = msg.getContentType();
            if (remoteUri != null) {
                ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(remoteUri.toString()));
                int N = this.mGroupChatListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        this.mGroupChatListeners.getBroadcastItem(i).onMessageGroupDeliveryInfoChanged(chatId, contactId, contentType, msgId, status.ordinal(), reasonCode.ordinal());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.mGroupChatListeners.finishBroadcast();
            }
        }
    }

    public void notifyGroupParticipantInfoChanged(ImParticipant participant) {
        Log.i(LOG_TAG, "notifyGroupParticipantInfoChanged");
        synchronized (this.mLock) {
            ImParticipant.Status status = participant.getStatus();
            ContactId contact = new ContactId(PhoneUtils.extractNumberFromUri(participant.getUri().toString()));
            GroupChat.ParticipantStatus participantStatus = convertParticipantStatus(status);
            int N = this.mGroupChatListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    this.mGroupChatListeners.getBroadcastItem(i).onParticipantStatusChanged(participant.getChatId(), contact, participantStatus);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mGroupChatListeners.finishBroadcast();
        }
    }

    public void notifyGroupChatDeleted(List<String> chatIds) {
        Log.i(LOG_TAG, "notifyGroupChatDeleted");
        synchronized (this.mLock) {
            int N = this.mGroupChatListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    this.mGroupChatListeners.getBroadcastItem(i).onDeleted(chatIds);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mGroupChatListeners.finishBroadcast();
        }
    }

    private void notifyMessageStateChanged(ContactId contactId, MessageBase msg, ChatLog.Message.Content.Status status, ChatLog.Message.Content.ReasonCode reasonCode) {
        ImSession session = this.mImModule.getImSession(msg.getChatId());
        if (!(session != null && session.isGroupChat())) {
            this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(contactId, msg.getContentType(), String.valueOf(msg.getId()), status, reasonCode);
        } else {
            notifyGroupMessageStateChanged(msg, status, reasonCode);
        }
    }

    private GroupChat.ParticipantStatus convertParticipantStatus(ImParticipant.Status status) {
        GroupChat.ParticipantStatus participantStatus = GroupChat.ParticipantStatus.DISCONNECTED;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[status.ordinal()]) {
            case 1:
                return GroupChat.ParticipantStatus.CONNECTED;
            case 2:
                return GroupChat.ParticipantStatus.INVITED;
            case 3:
            case 4:
                return GroupChat.ParticipantStatus.CONNECTED;
            case 5:
                return GroupChat.ParticipantStatus.DECLINED;
            case 6:
                return GroupChat.ParticipantStatus.DEPARTED;
            case 7:
                return GroupChat.ParticipantStatus.TIMEOUT;
            case 8:
                return GroupChat.ParticipantStatus.INVITING;
            default:
                return GroupChat.ParticipantStatus.DISCONNECTED;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    public IOneToOneChat getOneToOneChat(ContactId contact) throws ServerApiException {
        try {
            Log.d(LOG_TAG, "start : openSingleChat()");
            String number = PhoneUtils.extractNumberFromUri(contact.toString());
            ImSession session = null;
            ImCache cache = ImCache.getInstance();
            ChatImpl sessionApi = getChatSession(number);
            if (sessionApi == null) {
                Iterator<ImSession> it = cache.getAllImSessions().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ImSession se = it.next();
                    if (!se.isGroupChat()) {
                        List<String> strings = se.getParticipantsString();
                        if (strings != null) {
                            if (strings.size() != 0) {
                                if (PhoneUtils.extractNumberFromUri(strings.get(0)).equals(number)) {
                                    session = se;
                                    break;
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } else {
                ImSession coreSession = sessionApi.getCoreSession();
                if (coreSession != null) {
                    String str = LOG_TAG;
                    Log.d(str, "Core chat session already exist: " + coreSession.getChatId());
                    if (cache.getImSession(coreSession.getChatId()) != null) {
                        return sessionApi;
                    }
                    removeChatSession(number);
                } else {
                    removeChatSession(number);
                }
            }
            if (session == null) {
                String str2 = LOG_TAG;
                Log.d(str2, "Create a new chat session with " + IMSLog.checker(number));
                ArrayList<ImsUri> uriList = new ArrayList<>();
                uriList.add(ImsUri.parse("tel:" + contact.toString()));
                try {
                    session = this.mImModule.createChat(uriList, SUBJECT, "text/plain", -1, (String) null).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e2) {
                    e2.printStackTrace();
                }
            }
            if (session != null) {
                ChatImpl sessionApi2 = new ChatImpl(number, session, this.mImModule);
                addChatSession(number, sessionApi2);
                return sessionApi2;
            }
            Log.e(LOG_TAG, "getOneToOneChat: session is error...");
            throw new ServerApiException("session is error...");
        } catch (RemoteException e3) {
            throw new ServerApiException(e3.getMessage());
        }
    }

    public static void addChatSession(String contact, ChatImpl session) {
        mChatSessions.put(PhoneUtils.extractNumberFromUri(contact), session);
    }

    protected static IOneToOneChat getChatSession(String contact) {
        return mChatSessions.get(PhoneUtils.extractNumberFromUri(contact));
    }

    protected static void removeChatSession(String contact) {
        String number = PhoneUtils.extractNumberFromUri(contact);
        Hashtable<String, IOneToOneChat> hashtable = mChatSessions;
        if (hashtable != null && contact != null) {
            hashtable.remove(number);
        }
    }

    public void onCreateChatSucceeded(ImSession session) {
    }

    public void onCreateChatFailed(int phoneId, int threadId, ImErrorReason reason, String requestMessageId) {
    }

    public void onChatUpdateState(String chatId, ImDirection direction, ImSession.SessionState state) {
        GroupChat.State groupChatState = translateState(state, direction, ImSessionClosedReason.NONE);
        if (groupChatState != null) {
            notifyGroupChatStateChanged(chatId, groupChatState, translateReasonCode(ImSessionClosedReason.NONE));
        }
    }

    public void onChatEstablished(String chatId, ImDirection direction, ImsUri sessionUri, List<String> list, List<String> list2) {
    }

    public void onChatClosed(String chatId, ImDirection direction, ImSessionClosedReason reason) {
        GroupChat.State groupChatState = translateState(ImSession.SessionState.CLOSED, direction, reason);
        if (groupChatState != null) {
            notifyGroupChatStateChanged(chatId, groupChatState, translateReasonCode(reason));
        }
    }

    public void onChatSubjectUpdated(String chatId, ImSubjectData subjectData) {
    }

    public void onGroupChatIconUpdated(String chatId, ImIconData iconData) {
    }

    public void onGroupChatIconDeleted(String chatId) {
    }

    public void onParticipantAliasUpdated(String chatId, ImParticipant participant) {
    }

    public void onMessageSendResponseTimeout(MessageBase msg) {
    }

    public void onChatInvitationReceived(ImSession session) {
        Log.d(LOG_TAG, "start : onChatInvitationReceived()");
        if (session.isGroupChat()) {
            addGroupChatSession(new GroupChatImpl(session));
            Intent intent = new Intent("com.gsma.services.rcs.chat.action.NEW_GROUP_CHAT");
            intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, session.getChatId());
            this.mContext.sendBroadcast(intent);
        }
        if (session.getParticipantsString().size() != 1) {
            Log.d(LOG_TAG, "session.getParticipantsString().size() != 1");
            return;
        }
        String number = PhoneUtils.extractNumberFromUri(session.getParticipantsString().get(0));
        if (!mChatSessions.containsKey(number)) {
            addChatSession(number, new ChatImpl(number, session, this.mImModule));
        }
    }

    public void receiveGroupChatMessage(MessageBase msg) {
        Log.d(LOG_TAG, "start : receiveGroupChatMessage()");
        Intent intent = new Intent("com.gsma.services.rcs.chat.action.NEW_GROUP_CHAT_MESSAGE");
        intent.putExtra("messageId", Integer.toString(msg.getId()));
        intent.putExtra("mimeType", msg.getContentType());
        this.mContext.sendBroadcast(intent);
    }

    protected static void addGroupChatSession(GroupChatImpl session) {
        mGroupChatSessions.put(session.getChatId(), session);
    }

    protected static void removeGroupChatSession(String chatId) {
        mGroupChatSessions.remove(chatId);
    }

    public boolean isAllowedToInitiateGroupChat() throws RemoteException {
        Capabilities capx = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
        if (capx == null || !capx.hasFeature(Capabilities.FEATURE_SF_GROUP_CHAT)) {
            return false;
        }
        return true;
    }

    public boolean canInitiateGroupChat(ContactId contact) throws RemoteException {
        Capabilities capx;
        if (contact == null || (capx = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getCapabilities(contact.toString(), (long) Capabilities.FEATURE_SF_GROUP_CHAT, 0)) == null || !capx.hasFeature(Capabilities.FEATURE_SF_GROUP_CHAT)) {
            return false;
        }
        return true;
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public IGroupChat initiateGroupChat(List<ContactId> contacts, String subject) throws ServerApiException {
        Log.d(LOG_TAG, "start : initiateGroupChat()");
        ArrayList arrayList = new ArrayList();
        for (ContactId id : contacts) {
            arrayList.add(ImsUri.parse("tel:" + id.toString()));
        }
        try {
            ImSession session = this.mImModule.createChat(arrayList, subject, "text/plain", -1, (String) null).get();
            if (session != null) {
                GroupChatImpl groupChat = new GroupChatImpl(session.getChatId());
                addGroupChatSession(groupChat);
                return groupChat;
            }
            Log.e(LOG_TAG, "initiateGroupChat: session is error...");
            throw new ServerApiException("session is error...");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public IGroupChat getGroupChat(String chatId) throws ServerApiException {
        return mGroupChatSessions.get(chatId);
    }

    public void addOneToOneChatEventListener(IOneToOneChatListener listener) throws RemoteException {
        synchronized (this.mLock) {
            this.mOneToOneChatEventBroadcaster.addOneToOneChatEventListener(listener);
        }
    }

    public void removeOneToOneChatEventListener(IOneToOneChatListener listener) throws RemoteException {
        synchronized (this.mLock) {
            this.mOneToOneChatEventBroadcaster.removeOneToOneChatEventListener(listener);
        }
    }

    public void addGroupChatEventListener(IGroupChatListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupChatListeners.register(listener);
        }
    }

    public void removeGroupChatEventListener(IGroupChatListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupChatListeners.unregister(listener);
        }
    }

    public IChatServiceConfiguration getConfiguration() throws ServerApiException {
        return new ChatServiceConfigurationImpl(this.mImModule.getImConfig());
    }

    public void markMessageAsRead(String msgId) throws ServerApiException {
        Log.d(LOG_TAG, "start : markMessageAsRead()");
        ImMessage message = ImCache.getInstance().getImMessage(Integer.valueOf(msgId).intValue());
        if (message != null) {
            List<String> msgIds = new ArrayList<>();
            msgIds.add(msgId);
            this.mImModule.readMessages(message.getChatId(), msgIds);
            message.updateNotificationStatus(NotificationStatus.DELIVERED);
            ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(message.getChatId())));
            synchronized (this.mLock) {
                notifyMessageStateChanged(contactId, message, ChatLog.Message.Content.Status.DISPLAYED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
            }
        }
    }

    public void notifyChangeForDelete() {
        this.mContext.getContentResolver().notifyChange(ChatLog.Message.CONTENT_URI, (ContentObserver) null);
    }

    public void deleteOneToOneChats() throws RemoteException {
        Log.d(LOG_TAG, "start : deleteOneToOneChats()");
        Map<String, Set<String>> msgs = getMessages(false, "is_filetransfer != 1");
        if (msgs != null) {
            List<String> listChats = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
                listChats.add(entry.getKey());
                String uriString = getRemoteUserByChatId(entry.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneChatEventBroadcaster.broadcastMessageDeleted(PhoneUtils.extractNumberFromUri(uriString), entry.getValue());
                }
            }
            this.mImModule.deleteChats(listChats, false);
            mChatSessions.clear();
            notifyChangeForDelete();
        }
    }

    public void deleteGroupChats() throws RemoteException {
        Log.d(LOG_TAG, "start : delete All GroupChats()");
        mGroupChatSessions.clear();
        List<String> chatIds = new ArrayList<>();
        for (ImSession session : ImCache.getInstance().getAllImSessions()) {
            if (session.isGroupChat()) {
                chatIds.add(session.getChatId());
            }
        }
        this.mImModule.deleteChats(chatIds, false);
        notifyGroupChatDeleted(chatIds);
        notifyChangeForDelete();
    }

    public void deleteOneToOneChat(ContactId contact) throws RemoteException {
        Log.d(LOG_TAG, "start : deleteOneToOneChat()");
        Set<ImsUri> uris = new HashSet<>();
        uris.add(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contact.toString())));
        ImSession session = ImCache.getInstance().getImSessionByParticipants(uris, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        if (session == null) {
            Log.e(LOG_TAG, "there is no session for ft");
            return;
        }
        Map<String, Set<String>> msgs = getMessages(false, "is_filetransfer != 1 and chat_id = '" + session.getChatId() + "'");
        if (msgs != null) {
            List<String> listChats = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
                listChats.add(entry.getKey());
                String uriString = getRemoteUserByChatId(entry.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneChatEventBroadcaster.broadcastMessageDeleted(PhoneUtils.extractNumberFromUri(uriString), entry.getValue());
                }
            }
            this.mImModule.deleteChats(listChats, false);
            removeChatSession(PhoneUtils.extractNumberFromUri(contact.toString()));
            notifyChangeForDelete();
        }
    }

    public void deleteGroupChat(String chatId) throws RemoteException {
        Log.d(LOG_TAG, "start : deleteGroupChat()");
        mGroupChatSessions.remove(chatId);
        List<String> chatIds = new ArrayList<>();
        chatIds.add(chatId);
        this.mImModule.deleteChats(chatIds, false);
        notifyGroupChatDeleted(chatIds);
        notifyChangeForDelete();
    }

    public void deleteMessage(String msgId) throws RemoteException {
        boolean isGroup;
        IMSLog.s(LOG_TAG, "start : deleteMessage() msgId:" + msgId);
        Cursor cursorDb = this.mContext.getContentResolver().query(Uri.withAppendedPath(ChatLog.Message.CONTENT_URI, msgId), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursorDb != null) {
            try {
                if (cursorDb.getCount() != 0) {
                    cursorDb.moveToFirst();
                    String chatId = cursorDb.getString(cursorDb.getColumnIndex("chat_id"));
                    String remoteUser = cursorDb.getString(cursorDb.getColumnIndex(ICshConstants.ShareDatabase.KEY_TARGET_CONTACT));
                    if (cursorDb != null) {
                        cursorDb.close();
                    }
                    ImSession session = this.mImModule.getImSession(chatId);
                    if (session == null || !session.isGroupChat()) {
                        isGroup = false;
                    } else {
                        isGroup = true;
                    }
                    Set<String> msgs = new HashSet<>();
                    msgs.add(msgId);
                    List<String> list = new ArrayList<>();
                    list.add(msgId);
                    this.mImModule.deleteMessages(list, false);
                    if (!isGroup) {
                        synchronized (this.mLock) {
                            this.mOneToOneChatEventBroadcaster.broadcastMessageDeleted(new ContactId(PhoneUtils.extractNumberFromUri(remoteUser)).toString(), msgs);
                        }
                    } else {
                        List<String> msgList = new ArrayList<>(msgs);
                        synchronized (this.mLock) {
                            int N = this.mGroupChatListeners.beginBroadcast();
                            for (int i = 0; i < N; i++) {
                                try {
                                    this.mGroupChatListeners.getBroadcastItem(i).onMessagesDeleted(chatId, msgList);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            this.mGroupChatListeners.finishBroadcast();
                        }
                    }
                    notifyChangeForDelete();
                    return;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursorDb != null) {
            cursorDb.close();
            return;
        }
        return;
        throw th;
    }

    public IChatMessage getChatMessage(String msgId) throws RemoteException {
        return new ChatMessageImpl(msgId);
    }

    public void setRespondToDisplayReports(boolean enable) throws RemoteException {
        String str = LOG_TAG;
        IMSLog.s(str, "start : setRespondToDisplayReports() enable:" + enable);
        RcsSettingsUtils rcsSetting = RcsSettingsUtils.getInstance();
        if (rcsSetting != null) {
            rcsSetting.writeBoolean(ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS, enable);
        }
    }

    public List<String> getUndeliveredMessages(ContactId contact) throws RemoteException {
        Log.d(LOG_TAG, "start : getUndeliveredMessages()");
        ImsUri uri = ImsUri.parse("tel:" + contact.toString());
        Set<ImsUri> uris = new HashSet<>();
        uris.add(uri);
        ImSession session = ImCache.getInstance().getImSessionByParticipants(uris, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        List<String> msgIds = new ArrayList<>();
        if (session == null) {
            return msgIds;
        }
        Cursor cursor = ImCache.getInstance().queryMessages(new String[]{"_id"}, "chat_id = '" + session.getChatId() + "' and " + "notification_status" + " = " + NotificationStatus.NONE.getId() + " and " + "direction" + " = " + ImDirection.OUTGOING.getId() + " and " + ImContract.ChatItem.IS_FILE_TRANSFER + " = 0", (String[]) null, (String) null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    msgIds.add(String.valueOf(cursor.getInt(0)));
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return msgIds;
        throw th;
    }

    public void markUndeliveredMessagesAsProcessed(List<String> msgIds) throws RemoteException {
        Log.d(LOG_TAG, "start : markUndeliveredMessagesAsProcessed()");
        ImCache cache = ImCache.getInstance();
        for (String msgId : msgIds) {
            ImMessage message = cache.getImMessage(Integer.valueOf(msgId).intValue());
            if (message != null) {
                message.updateStatus(ImConstants.Status.SENT);
                cache.removeFromPendingList(Integer.valueOf(msgId).intValue());
            }
        }
    }

    public Map<String, Set<String>> getMessages(boolean isGroup, String selection) {
        Log.d(LOG_TAG, "start : deleteFileTransfers()");
        ImCache cache = ImCache.getInstance();
        Map<String, Set<String>> msgs = new TreeMap<>();
        Cursor cursorDb = cache.queryMessages(new String[]{"_id", "chat_id"}, selection, (String[]) null, (String) null);
        if (cursorDb != null) {
            try {
                if (cursorDb.getCount() != 0) {
                    while (cursorDb.moveToNext()) {
                        String chatIdString = cursorDb.getString(cursorDb.getColumnIndexOrThrow("chat_id"));
                        ImSession session = cache.getImSession(chatIdString);
                        if (session != null) {
                            if (session.isGroupChat() == isGroup) {
                                addRecord(chatIdString, String.valueOf(cursorDb.getInt(cursorDb.getColumnIndexOrThrow("_id"))), msgs);
                            }
                        }
                    }
                    if (cursorDb != null) {
                        cursorDb.close();
                    }
                    return msgs;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        Log.e(LOG_TAG, "deleteOneToOneFileTransfers: Message not found.");
        if (cursorDb != null) {
            cursorDb.close();
        }
        return null;
        throw th;
    }

    private void addRecord(String chatIdString, String idString, Map<String, Set<String>> msgs) {
        Set<String> setMsgs = msgs.get(chatIdString);
        if (setMsgs == null) {
            Set<String> setMsgs2 = new HashSet<>();
            setMsgs2.add(idString);
            msgs.put(chatIdString, setMsgs2);
            return;
        }
        setMsgs.add(idString);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleReceiveMessage(com.sec.internal.ims.servicemodules.im.MessageBase r11, boolean r12) {
        /*
            r10 = this;
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r0 = r10.mImModule
            java.lang.String r1 = r11.getChatId()
            com.sec.internal.ims.servicemodules.im.ImSession r0 = r0.getImSession(r1)
            if (r0 == 0) goto L_0x005a
            boolean r1 = r0.isGroupChat()
            java.lang.Object r2 = r10.mLock
            monitor-enter(r2)
            if (r1 == 0) goto L_0x001b
            r10.receiveGroupChatMessage(r11)     // Catch:{ all -> 0x0019 }
            goto L_0x0056
        L_0x0019:
            r3 = move-exception
            goto L_0x0058
        L_0x001b:
            if (r12 == 0) goto L_0x001f
            monitor-exit(r2)     // Catch:{ all -> 0x0019 }
            return
        L_0x001f:
            com.sec.ims.util.ImsUri r3 = r11.getRemoteUri()     // Catch:{ all -> 0x0019 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0019 }
            com.gsma.services.rcs.contact.ContactId r5 = new com.gsma.services.rcs.contact.ContactId     // Catch:{ all -> 0x0019 }
            java.lang.String r4 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r3)     // Catch:{ all -> 0x0019 }
            r5.<init>(r4)     // Catch:{ all -> 0x0019 }
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneChatEventBroadcaster r4 = r10.mOneToOneChatEventBroadcaster     // Catch:{ all -> 0x0019 }
            java.lang.String r6 = r11.getContentType()     // Catch:{ all -> 0x0019 }
            int r7 = r11.getId()     // Catch:{ all -> 0x0019 }
            java.lang.String r7 = java.lang.String.valueOf(r7)     // Catch:{ all -> 0x0019 }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r8 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.RECEIVED     // Catch:{ all -> 0x0019 }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$ReasonCode r9 = com.gsma.services.rcs.chat.ChatLog.Message.Content.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x0019 }
            r4.broadcastMessageStatusChanged(r5, r6, r7, r8, r9)     // Catch:{ all -> 0x0019 }
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneChatEventBroadcaster r4 = r10.mOneToOneChatEventBroadcaster     // Catch:{ all -> 0x0019 }
            int r6 = r11.getId()     // Catch:{ all -> 0x0019 }
            java.lang.String r6 = java.lang.String.valueOf(r6)     // Catch:{ all -> 0x0019 }
            java.lang.String r7 = r11.getContentType()     // Catch:{ all -> 0x0019 }
            r4.broadcastMessageReceived(r6, r7, r3)     // Catch:{ all -> 0x0019 }
        L_0x0056:
            monitor-exit(r2)     // Catch:{ all -> 0x0019 }
            goto L_0x005a
        L_0x0058:
            monitor-exit(r2)     // Catch:{ all -> 0x0019 }
            throw r3
        L_0x005a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl.handleReceiveMessage(com.sec.internal.ims.servicemodules.im.MessageBase, boolean):void");
    }

    public GroupChat.State translateState(ImSession.SessionState state, ImDirection direction, ImSessionClosedReason closedReason) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[state.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    return GroupChat.State.STARTED;
                }
                if ((i == 4 || i == 5) && closedReason != ImSessionClosedReason.NONE) {
                    return GroupChat.State.ABORTED;
                }
                return null;
            } else if (direction == ImDirection.INCOMING) {
                return GroupChat.State.ACCEPTING;
            } else {
                return null;
            }
        } else if (direction == ImDirection.INCOMING) {
            return GroupChat.State.INVITED;
        } else {
            if (direction == ImDirection.OUTGOING) {
                return GroupChat.State.INITIATING;
            }
            return null;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState;

        static {
            int[] iArr = new int[ImSessionClosedReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason = iArr;
            try {
                iArr[ImSessionClosedReason.CLOSED_BY_REMOTE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason[ImSessionClosedReason.CLOSED_BY_LOCAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            int[] iArr2 = new int[ImSession.SessionState.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState = iArr2;
            try {
                iArr2[ImSession.SessionState.INITIAL.ordinal()] = 1;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.STARTING.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.ESTABLISHED.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.CLOSING.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.CLOSED.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            int[] iArr3 = new int[ImParticipant.Status.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status = iArr3;
            try {
                iArr3[ImParticipant.Status.INITIAL.ordinal()] = 1;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.INVITED.ordinal()] = 2;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.ACCEPTED.ordinal()] = 3;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.PENDING.ordinal()] = 4;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.DECLINED.ordinal()] = 5;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.GONE.ordinal()] = 6;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.TIMEOUT.ordinal()] = 7;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.TO_INVITE.ordinal()] = 8;
            } catch (NoSuchFieldError e15) {
            }
        }
    }

    public GroupChat.ReasonCode translateReasonCode(ImSessionClosedReason closedReason) {
        GroupChat.ReasonCode reasonCode = GroupChat.ReasonCode.UNSPECIFIED;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason[closedReason.ordinal()];
        if (i == 1) {
            return GroupChat.ReasonCode.ABORTED_BY_REMOTE;
        }
        if (i != 2) {
            return GroupChat.ReasonCode.UNSPECIFIED;
        }
        return GroupChat.ReasonCode.ABORTED_BY_USER;
    }

    public static ChatLog.Message.Content.Status translateStatus(ImConstants.Status status) {
        ChatLog.Message.Content.Status retStatus = ChatLog.Message.Content.Status.DISPLAY_REPORT_REQUESTED;
        if (ImConstants.Status.SENDING == status) {
            return ChatLog.Message.Content.Status.SENDING;
        }
        if (ImConstants.Status.SENT == status) {
            return ChatLog.Message.Content.Status.SENT;
        }
        if (ImConstants.Status.FAILED == status) {
            return ChatLog.Message.Content.Status.FAILED;
        }
        if (ImConstants.Status.TO_SEND == status) {
            return ChatLog.Message.Content.Status.QUEUED;
        }
        if (ImConstants.Status.READ == status) {
            return ChatLog.Message.Content.Status.DISPLAYED;
        }
        return retStatus;
    }

    public String getRemoteUserByChatId(String chatId) {
        List<String> participantsString;
        ImSession imSession = ImCache.getInstance().getImSession(chatId);
        if (imSession == null || (participantsString = imSession.getParticipantsString()) == null || participantsString.size() <= 0) {
            return "";
        }
        return participantsString.get(0);
    }

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public void onComposingNotificationReceived(String chatId, boolean isGroupChat, ImsUri uri, String userAlias, boolean isTyping, int interval) {
        Log.i(LOG_TAG, "onComposingNotificationReceived");
        synchronized (this.mLock) {
            ContactId contactId = null;
            if (uri != null) {
                try {
                    contactId = new ContactId(PhoneUtils.extractNumberFromUri(uri.toString()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Throwable contactId2) {
                    throw contactId2;
                }
            }
            if (isGroupChat) {
                int N = this.mGroupChatListeners.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    this.mGroupChatListeners.getBroadcastItem(i).onComposingEvent(chatId, contactId, isTyping);
                }
                this.mGroupChatListeners.finishBroadcast();
            } else {
                this.mOneToOneChatEventBroadcaster.broadcastComposingEvent(contactId, isTyping);
            }
        }
    }

    public void onAddParticipantsSucceeded(String chatId, Collection<ImsUri> collection) {
    }

    public void onAddParticipantsFailed(String chatId, Collection<ImsUri> collection, ImErrorReason reason) {
    }

    public void onRemoveParticipantsSucceeded(String chatId, Collection<ImsUri> collection) {
    }

    public void onRemoveParticipantsFailed(String chatId, Collection<ImsUri> collection, ImErrorReason reason) {
    }

    public void onParticipantsAdded(ImSession session, Collection<ImParticipant> participants) {
        for (ImParticipant p : participants) {
            notifyGroupParticipantInfoChanged(p);
        }
    }

    public void onParticipantsJoined(ImSession session, Collection<ImParticipant> participants) {
        for (ImParticipant p : participants) {
            notifyGroupParticipantInfoChanged(p);
        }
    }

    public void onParticipantsLeft(ImSession session, Collection<ImParticipant> participants) {
        for (ImParticipant p : participants) {
            notifyGroupParticipantInfoChanged(p);
        }
    }

    public void onChangeGroupChatLeaderSucceeded(String chatId, List<ImsUri> list) {
    }

    public void onChangeGroupChatLeaderFailed(String chatId, List<ImsUri> list, ImErrorReason reason) {
    }

    public void onChangeGroupAliasSucceeded(String chatId, String alias) {
    }

    public void onChangeGroupAliasFailed(String chatId, String alias, ImErrorReason reason) {
    }

    public void onChangeGroupChatSubjectSucceeded(String chatId, String subject) {
    }

    public void onChangeGroupChatSubjectFailed(String chatId, String subject, ImErrorReason reason) {
    }

    public void onMessageSendResponse(MessageBase msg) {
    }

    public void onChangeGroupChatIconSuccess(String chatId, String icon_path) {
    }

    public void onChangeGroupChatIconFailed(String chatId, String icon_path, ImErrorReason reason) {
    }

    public void onMessageReceived(MessageBase msg, ImSession session) {
        if (session.isGroupChat()) {
            receiveGroupChatMessage(msg);
            return;
        }
        String uriString = msg.getRemoteUri().toString();
        this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(new ContactId(PhoneUtils.extractNumberFromUri(uriString)), msg.getContentType(), String.valueOf(msg.getId()), ChatLog.Message.Content.Status.RECEIVED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
        this.mOneToOneChatEventBroadcaster.broadcastMessageReceived(String.valueOf(msg.getId()), msg.getContentType(), uriString);
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
        Log.d(LOG_TAG, "onMessageSendingSucceeded():");
        notifyMessageStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(msg.getChatId()))), msg, ChatLog.Message.Content.Status.SENT, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse reason, Result result) {
        Log.d(LOG_TAG, "onMessageSendingFailed():");
        notifyMessageStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(msg.getChatId()))), msg, ChatLog.Message.Content.Status.FAILED, ChatLog.Message.Content.ReasonCode.FAILED_SEND);
    }

    public void onMessageSendResponseFailed(String chatId, int messageNumber, int reasoncode, String requestMessageId) {
    }

    public void onImdnNotificationReceived(MessageBase msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
        ContactId contact = new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(msg.getChatId())));
        Log.d(LOG_TAG, "onImdnNotificationReceived()");
        if (NotificationStatus.DELIVERED == status) {
            if (!isGroupChat) {
                this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(contact, msg.getContentType(), String.valueOf(msg.getId()), ChatLog.Message.Content.Status.DELIVERED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
            } else {
                notifyMessageGroupDeliveryInfoChanged((ImMessage) msg, remoteUri, GroupDeliveryInfo.Status.DELIVERED, GroupDeliveryInfo.ReasonCode.UNSPECIFIED);
            }
        } else if (NotificationStatus.DISPLAYED != status) {
        } else {
            if (!isGroupChat) {
                this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(contact, msg.getContentType(), String.valueOf(msg.getId()), ChatLog.Message.Content.Status.DISPLAYED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
                return;
            }
            notifyMessageGroupDeliveryInfoChanged((ImMessage) msg, remoteUri, GroupDeliveryInfo.Status.DISPLAYED, GroupDeliveryInfo.ReasonCode.UNSPECIFIED);
        }
    }

    public void clearMessageDeliveryExpiration(List<String> list) throws RemoteException {
    }

    public void onMessageRevokeTimerExpired(String chatId, Collection<String> collection) {
    }

    public void onGroupChatLeaderUpdated(String chatId, String leaderParticipant) {
    }
}
