package com.sec.internal.ims.servicemodules.im;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.ImExtensionMNOHeadersHelper;
import com.sec.internal.helper.MetaDataUtil;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage;
import com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage;
import com.sec.internal.ims.servicemodules.im.FtMsrpMessage;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.listener.IImCacheActionListener;
import com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class ImCache {
    private static final int DEFAULT_MAX_CONCURRENT_SESSION = 100;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImCache.class.getSimpleName();
    private static final int MAX_CACHED_MESSAGE = 30;
    private static final int MAX_CACHED_SESSION = 500;
    private static ImCache sInstance;
    /* access modifiers changed from: private */
    public final LruCache<Integer, ImSession> mActiveSessions = new LruCache<Integer, ImSession>(100) {
        /* access modifiers changed from: protected */
        public void entryRemoved(boolean evicted, Integer key, ImSession oldValue, ImSession newValue) {
            if (evicted) {
                String access$000 = ImCache.LOG_TAG;
                Log.i(access$000, "mActiveSessions#entryRemoved: " + oldValue.getChatId());
                oldValue.closeSession();
            }
        }
    };
    private LruCache<Pair<String, ImDirection>, MessageBase> mCachingMessages = new LruCache<Pair<String, ImDirection>, MessageBase>(30) {
        /* access modifiers changed from: protected */
        public MessageBase create(Pair<String, ImDirection> key) {
            String access$000 = ImCache.LOG_TAG;
            Log.i(access$000, "Cache miss. attempt to load from db: " + key);
            MessageBase msg = ImCache.this.mPersister.queryMessage((String) key.first, (ImDirection) key.second);
            if (msg != null) {
                return ImCache.this.loadExtras(msg);
            }
            Log.i(ImCache.LOG_TAG, "Couldn't load from db.");
            return null;
        }

        /* access modifiers changed from: protected */
        public void entryRemoved(boolean evicted, Pair<String, ImDirection> pair, MessageBase oldValue, MessageBase newValue) {
            if (evicted) {
                String access$000 = ImCache.LOG_TAG;
                Log.i(access$000, "CachingMessage#entryRemoved: id= " + oldValue.getId());
                ImCache.this.unregisterObserver(oldValue);
            }
        }
    };
    private final Set<ImsUri> mChatbotRoleUris = new HashSet();
    private CmStoreInvoker mCmStoreInvoker;
    private ImModule mImModule;
    private final LruCache<String, ImSession> mImSessions = new LruCache<String, ImSession>(500) {
        /* access modifiers changed from: protected */
        public ImSession create(String key) {
            String access$000 = ImCache.LOG_TAG;
            Log.i(access$000, "Cache miss. attempt to load from db: " + key);
            ChatData chatData = ImCache.this.mPersister.querySessionByChatId(key);
            if (chatData != null) {
                return ImCache.this.createSession(chatData);
            }
            Log.i(ImCache.LOG_TAG, "Couldn't load from db.");
            return null;
        }

        /* access modifiers changed from: protected */
        public void entryRemoved(boolean evicted, String key, ImSession oldValue, ImSession newValue) {
            if (evicted) {
                ImCache.this.mActiveSessions.remove(Integer.valueOf(oldValue.getId()));
                String access$000 = ImCache.LOG_TAG;
                Log.i(access$000, "ImSessions#entryRemoved: " + oldValue.getChatId());
                oldValue.closeSession();
            }
        }
    };
    private boolean mIsChatbotRoleUrisLoaded;
    private boolean mIsLoaded;
    private final List<IImCacheActionListener> mListener = new ArrayList();
    private final Observer mObserver = new Observer() {
        public final void update(Observable observable, Object obj) {
            ImCache.this.lambda$new$0$ImCache(observable, obj);
        }
    };
    private final MessageMap mPendingMessages = new MessageMap();
    /* access modifiers changed from: private */
    public ImPersister mPersister;

    public /* synthetic */ void lambda$new$0$ImCache(Observable observable, Object data) {
        if (observable instanceof ChatData) {
            updateChat((ChatData) observable, (ImCacheAction) data);
        } else if (observable instanceof MessageBase) {
            updateMessage((MessageBase) observable, (ImCacheAction) data);
        } else if (observable instanceof ImParticipant) {
            updateParticipant((ImParticipant) observable, (ImCacheAction) data);
        } else {
            String str = LOG_TAG;
            Log.e(str, "Unknown observable :" + observable + ", data : " + data);
        }
    }

    protected ImCache() {
    }

    public static synchronized ImCache getInstance() {
        ImCache imCache;
        synchronized (ImCache.class) {
            if (sInstance == null) {
                sInstance = new ImCache();
            }
            imCache = sInstance;
        }
        return imCache;
    }

    public static String getContentType(File file) {
        String fileName = file.getName();
        String contentType = getContentTypeFromFileName(fileName);
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (contentType == null && isMetaDataExtension(extension)) {
            contentType = MetaDataUtil.getContentType(file);
        }
        if (contentType != null) {
            return contentType;
        }
        Log.i(LOG_TAG, "ContentTypeTranslator error: UNKNOWN TYPE");
        return HttpPostBody.CONTENT_TYPE_DEFAULT;
    }

    private static String getContentTypeFromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (ContentTypeTranslator.isTranslationDefined(extension)) {
            return ContentTypeTranslator.translate(extension);
        }
        return null;
    }

    private static boolean isMetaDataExtension(String extension) {
        return "3gp".equalsIgnoreCase(extension) || "mp4".equalsIgnoreCase(extension) || "heic".equalsIgnoreCase(extension);
    }

    public void initializeLruCache(int maxConcurrentSession) {
        if (maxConcurrentSession <= 0) {
            maxConcurrentSession = 100;
        }
        this.mActiveSessions.resize(Math.min(maxConcurrentSession, 500));
    }

    public void addImCacheActionListener(IImCacheActionListener listener) {
        this.mListener.add(listener);
    }

    public void removeImCacheActionListener(IImCacheActionListener listener) {
        this.mListener.remove(listener);
    }

    private void registerObserver(Observable observable) {
        observable.addObserver(this.mObserver);
    }

    /* access modifiers changed from: private */
    public void unregisterObserver(Observable observable) {
        observable.deleteObserver(this.mObserver);
    }

    private void updateChat(ChatData chatData, ImCacheAction action) {
        this.mPersister.updateChat(chatData, action);
    }

    private void updateMessage(MessageBase message, ImCacheAction action) {
        this.mPersister.updateMessage(message, action);
        for (IImCacheActionListener listener : this.mListener) {
            listener.updateMessage(message, action);
        }
    }

    private void updateMessage(Collection<MessageBase> list, ImCacheAction action) {
        this.mPersister.updateMessage(list, action);
        for (IImCacheActionListener listener : this.mListener) {
            listener.updateMessage(list, action);
        }
    }

    private void updateParticipant(ImParticipant participant, ImCacheAction action) {
        this.mPersister.updateParticipant(participant, action);
        for (IImCacheActionListener listener : this.mListener) {
            listener.updateParticipant(participant, action);
        }
    }

    private void updateParticipant(Collection<ImParticipant> list, ImCacheAction action) {
        this.mPersister.updateParticipant(list, action);
        for (IImCacheActionListener listener : this.mListener) {
            listener.updateParticipant(list, action);
        }
    }

    public synchronized void load(ImModule imModule) {
        if (this.mIsLoaded) {
            Log.i(LOG_TAG, "Alraedy loaded");
            return;
        }
        this.mImModule = imModule;
        this.mPersister = new ImPersister(this.mImModule.getContext(), this.mImModule);
        this.mCmStoreInvoker = new CmStoreInvoker(imModule);
        this.mIsLoaded = true;
    }

    public synchronized void loadImSessionByChatType(boolean isGroupChat) {
        List<String> ret = this.mPersister.querySessionByChatType(isGroupChat);
        String str = LOG_TAG;
        Log.i(str, "loadImSessionByChatType loaded chat ids : " + ret);
        if (ret != null && !ret.isEmpty()) {
            for (String chatId : ret) {
                this.mImSessions.get(chatId);
            }
        }
    }

    public synchronized void loadImSessionForAutoRejoin() {
        List<String> ret = this.mPersister.querySessionForAutoRejoin();
        String str = LOG_TAG;
        Log.i(str, "loadImSessionForAutoRejoin Autorejoin chat ids : " + ret);
        if (!ret.isEmpty()) {
            for (String chatId : ret) {
                this.mImSessions.get(chatId);
            }
        }
    }

    public synchronized void loadImSessionWithPendingMessages() {
        List<String> ret = this.mPersister.queryAllChatIDwithPendingMessages();
        String str = LOG_TAG;
        Log.i(str, "loadImSessionWithPendingMessages " + ret.size() + " pending message(s)");
        if (!ret.isEmpty()) {
            for (String chatId : ret) {
                this.mImSessions.get(chatId);
            }
        }
    }

    public synchronized void loadImSessionWithFailedFTMessages() {
        List<String> ret = this.mPersister.queryAllChatIDwithFailedFTMessages();
        String str = LOG_TAG;
        Log.i(str, "loadImSessionWithFailedFTMessages " + ret.size() + " failed message(s)");
        if (!ret.isEmpty()) {
            for (String chatId : ret) {
                this.mImSessions.get(chatId);
            }
        }
    }

    public synchronized void updateUriGenerator(int phoneId) {
        Log.i(LOG_TAG, "updateUriGenerator");
        UriGenerator uriGenerator = this.mImModule.getUriGenerator(phoneId);
        for (ImSession session : this.mImSessions.snapshot().values()) {
            session.updateUriGenerator(uriGenerator);
        }
    }

    public synchronized List<Bundle> loadLastSentMessages(List<String> list) {
        return this.mPersister.queryLastSentMessages(list);
    }

    public synchronized boolean isLoaded() {
        return this.mIsLoaded;
    }

    public synchronized void clear() {
        this.mImSessions.evictAll();
    }

    /* access modifiers changed from: private */
    public synchronized ImSession createSession(ChatData chatData) {
        Map<ImsUri, ImParticipant> participants;
        int phoneId;
        Map<String, Integer> needToRevokeMessages;
        participants = new HashMap<>();
        for (ImParticipant p : this.mPersister.queryParticipantSet(chatData.getChatId())) {
            participants.put(p.getUri(), p);
            registerObserver(p);
        }
        phoneId = this.mImModule.getPhoneIdByIMSI(chatData.getOwnIMSI());
        String str = LOG_TAG;
        Log.i(str, "Load participants: size()=" + participants.size() + ", values()=" + IMSLog.checker(participants.values()));
        if (this.mImModule.getImConfig(phoneId).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
            if (chatData.getConversationId() == null) {
                chatData.setConversationId(StringIdGenerator.generateConversationId());
            }
            if (chatData.getContributionId() == null) {
                chatData.setContributionId(chatData.isGroupChat() ? chatData.getConversationId() : StringIdGenerator.generateContributionId());
            }
        } else if (chatData.getContributionId() == null) {
            chatData.setContributionId(StringIdGenerator.generateContributionId());
        }
        loadPendingMessages(chatData);
        needToRevokeMessages = new HashMap<>();
        if (this.mImModule.getImConfig().getChatRevokeTimer() > 0) {
            for (MessageBase m : loadMessageListForRevoke(chatData)) {
                needToRevokeMessages.put(m.getImdnId(), Integer.valueOf(m.getId()));
            }
        }
        registerObserver(chatData);
        return new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig(phoneId)).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator(phoneId)).chatData(chatData).participants(participants).needToRevokeMessages(needToRevokeMessages).getter(this.mImModule).build();
    }

    private void loadPendingMessages(ChatData chatData) {
        List<Integer> messageIds = this.mPersister.queryPendingMessageIds(chatData.getChatId());
        String str = LOG_TAG;
        Log.i(str, "pending messages count:" + messageIds.size());
        int phoneId = this.mImModule.getPhoneIdByIMSI(chatData.getOwnIMSI());
        List<String> messageIdsForQuerying = new ArrayList<>();
        for (Integer intValue : messageIds) {
            int id = intValue.intValue();
            MessageBase m = this.mPendingMessages.get(id);
            if (m == null) {
                messageIdsForQuerying.add(String.valueOf(id));
            } else if ((m instanceof FtHttpIncomingMessage) || (m instanceof FtHttpOutgoingMessage)) {
                ImModule imModule = this.mImModule;
                m.setNetwork(imModule.getNetwork(imModule.getImConfig(phoneId).isFtHttpOverDefaultPdn(), phoneId));
            } else {
                m.setNetwork(this.mImModule.getNetwork(false, phoneId));
            }
        }
        for (MessageBase m2 : this.mPersister.queryMessages((Collection<String>) messageIdsForQuerying)) {
            m2.setImdnRecRouteList(this.mPersister.queryImImdnRecRoute(m2));
            if ((m2 instanceof FtHttpIncomingMessage) || (m2 instanceof FtHttpOutgoingMessage)) {
                ImModule imModule2 = this.mImModule;
                m2.setNetwork(imModule2.getNetwork(imModule2.getImConfig(phoneId).isFtHttpOverDefaultPdn(), phoneId));
            } else {
                m2.setNetwork(this.mImModule.getNetwork(false, phoneId));
            }
            if (m2 instanceof FtMessage) {
                FtMessage message = (FtMessage) m2;
                message.setIsGroupChat(chatData.isGroupChat());
                message.setContributionId(chatData.getContributionId());
                message.setConversationId(chatData.getConversationId());
            }
            registerObserver(m2);
            this.mPendingMessages.put(m2);
        }
    }

    private List<MessageBase> loadMessageListForRevoke(ChatData chatData) {
        List<Integer> needToRevokeMessages = this.mPersister.queryMessagesIdsForRevoke(chatData.getChatId());
        String str = LOG_TAG;
        Log.i(str, "revoke messages count:" + needToRevokeMessages.size());
        List<MessageBase> messages = new ArrayList<>();
        List<String> messageIdsForQuerying = new ArrayList<>();
        for (Integer intValue : needToRevokeMessages) {
            int id = intValue.intValue();
            if (this.mPendingMessages.containsKey(id)) {
                messages.add(this.mPendingMessages.get(id));
            } else {
                messageIdsForQuerying.add(String.valueOf(id));
            }
        }
        for (MessageBase m : this.mPersister.queryMessages((Collection<String>) messageIdsForQuerying)) {
            this.mPendingMessages.put(m);
            m.setImdnRecRouteList(this.mPersister.queryImImdnRecRoute(m));
            registerObserver(m);
            messages.add(m);
        }
        return messages;
    }

    private IImServiceInterface getImHandler() {
        return this.mImModule.getImHandler();
    }

    public ImSession getImSession(String chatId) {
        ImSession imSession;
        if (chatId == null) {
            return null;
        }
        synchronized (this) {
            imSession = this.mImSessions.get(chatId);
        }
        return imSession;
    }

    public synchronized ImSession getImSessionByContributionId(String imsi, String cid, boolean isGroupChat) {
        if (cid == null) {
            return null;
        }
        for (ImSession session : this.mImSessions.snapshot().values()) {
            if (imsi.equals(session.getOwnImsi()) && cid.equals(session.getContributionId()) && session.isGroupChat() == isGroupChat) {
                return session;
            }
        }
        ChatData chatData = this.mPersister.querySessionByContributionId(imsi, cid, isGroupChat);
        if (chatData == null) {
            Log.i(LOG_TAG, "getImSessionByContributionId: Couldn't load from db.");
            return null;
        }
        return this.mImSessions.get(chatData.getChatId());
    }

    public synchronized ImSession getImSessionByConversationId(String cid, boolean isGroupChat) {
        String str = LOG_TAG;
        IMSLog.s(str, "getImSessionByConversationId cid=" + cid + " isGroupChat=" + isGroupChat);
        if (cid == null) {
            return null;
        }
        for (ImSession session : this.mImSessions.snapshot().values()) {
            if (session.isGroupChat() == isGroupChat && cid.equals(session.getConversationId())) {
                return session;
            }
        }
        ChatData chatData = this.mPersister.querySessionByConversationId(cid, isGroupChat);
        if (chatData == null) {
            Log.i(LOG_TAG, "getImSessionByConversationId: Couldn't load from db.");
            return null;
        }
        return this.mImSessions.get(chatData.getChatId());
    }

    public ImSession getImSessionByRawHandle(Object rawHandle) {
        for (ImSession session : this.mImSessions.snapshot().values()) {
            if (session.hasImSessionInfo(rawHandle)) {
                return session;
            }
        }
        return null;
    }

    public ImSession getImSessionByParticipants(Set<ImsUri> participants, ChatData.ChatType chatType, String imsi) {
        return getImSessionByParticipants(participants, chatType, imsi, ChatMode.OFF);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00c1, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.im.ImSession getImSessionByParticipants(java.util.Set<com.sec.ims.util.ImsUri> r7, com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType r8, java.lang.String r9, com.sec.internal.constants.ims.servicemodules.im.ChatMode r10) {
        /*
            r6 = this;
            monitor-enter(r6)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00c2 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c2 }
            r1.<init>()     // Catch:{ all -> 0x00c2 }
            java.lang.String r2 = "getImSessionByParticipants chatType= "
            r1.append(r2)     // Catch:{ all -> 0x00c2 }
            r1.append(r8)     // Catch:{ all -> 0x00c2 }
            java.lang.String r2 = " participants="
            r1.append(r2)     // Catch:{ all -> 0x00c2 }
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r7)     // Catch:{ all -> 0x00c2 }
            r1.append(r2)     // Catch:{ all -> 0x00c2 }
            java.lang.String r2 = " imsi="
            r1.append(r2)     // Catch:{ all -> 0x00c2 }
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r9)     // Catch:{ all -> 0x00c2 }
            r1.append(r2)     // Catch:{ all -> 0x00c2 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00c2 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00c2 }
            r0 = 0
            if (r7 == 0) goto L_0x00c0
            boolean r1 = r7.isEmpty()     // Catch:{ all -> 0x00c2 }
            if (r1 == 0) goto L_0x003a
            goto L_0x00c0
        L_0x003a:
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r1 = r6.mImSessions     // Catch:{ all -> 0x00c2 }
            java.util.Map r1 = r1.snapshot()     // Catch:{ all -> 0x00c2 }
            java.util.Collection r1 = r1.values()     // Catch:{ all -> 0x00c2 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x00c2 }
        L_0x0048:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x00c2 }
            if (r2 == 0) goto L_0x00a1
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x00c2 }
            com.sec.internal.ims.servicemodules.im.ImSession r2 = (com.sec.internal.ims.servicemodules.im.ImSession) r2     // Catch:{ all -> 0x00c2 }
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x00c2 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00c2 }
            r4.<init>()     // Catch:{ all -> 0x00c2 }
            java.lang.String r5 = "chat Type "
            r4.append(r5)     // Catch:{ all -> 0x00c2 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = r2.getChatType()     // Catch:{ all -> 0x00c2 }
            r4.append(r5)     // Catch:{ all -> 0x00c2 }
            java.lang.String r5 = " imsi="
            r4.append(r5)     // Catch:{ all -> 0x00c2 }
            java.lang.String r5 = r2.getOwnImsi()     // Catch:{ all -> 0x00c2 }
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r5)     // Catch:{ all -> 0x00c2 }
            r4.append(r5)     // Catch:{ all -> 0x00c2 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00c2 }
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x00c2 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = r2.getChatType()     // Catch:{ all -> 0x00c2 }
            if (r3 != r8) goto L_0x00a0
            java.lang.String r3 = r2.getOwnImsi()     // Catch:{ all -> 0x00c2 }
            if (r3 == 0) goto L_0x00a0
            java.lang.String r3 = r2.getOwnImsi()     // Catch:{ all -> 0x00c2 }
            boolean r3 = r3.equals(r9)     // Catch:{ all -> 0x00c2 }
            if (r3 == 0) goto L_0x00a0
            java.util.Set r3 = r2.getParticipantsUri()     // Catch:{ all -> 0x00c2 }
            boolean r3 = r7.equals(r3)     // Catch:{ all -> 0x00c2 }
            if (r3 == 0) goto L_0x00a0
            monitor-exit(r6)
            return r2
        L_0x00a0:
            goto L_0x0048
        L_0x00a1:
            com.sec.internal.ims.servicemodules.im.ImPersister r1 = r6.mPersister     // Catch:{ all -> 0x00c2 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r1 = r1.querySessionByParticipants(r7, r8, r9, r10)     // Catch:{ all -> 0x00c2 }
            if (r1 != 0) goto L_0x00b2
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x00c2 }
            java.lang.String r3 = "getImSessionByParticipants: Couldn't load from db."
            android.util.Log.i(r2, r3)     // Catch:{ all -> 0x00c2 }
            monitor-exit(r6)
            return r0
        L_0x00b2:
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r0 = r6.mImSessions     // Catch:{ all -> 0x00c2 }
            java.lang.String r2 = r1.getChatId()     // Catch:{ all -> 0x00c2 }
            java.lang.Object r0 = r0.get(r2)     // Catch:{ all -> 0x00c2 }
            com.sec.internal.ims.servicemodules.im.ImSession r0 = (com.sec.internal.ims.servicemodules.im.ImSession) r0     // Catch:{ all -> 0x00c2 }
            monitor-exit(r6)
            return r0
        L_0x00c0:
            monitor-exit(r6)
            return r0
        L_0x00c2:
            r7 = move-exception
            monitor-exit(r6)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.getImSessionByParticipants(java.util.Set, com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType, java.lang.String, com.sec.internal.constants.ims.servicemodules.im.ChatMode):com.sec.internal.ims.servicemodules.im.ImSession");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006a, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.util.Set<com.sec.internal.ims.servicemodules.im.ImSession> getAllImSessionByParticipants(java.util.Set<com.sec.ims.util.ImsUri> r6, com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType r7) {
        /*
            r5 = this;
            monitor-enter(r5)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x006b }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x006b }
            r1.<init>()     // Catch:{ all -> 0x006b }
            java.lang.String r2 = "getAllImSessionByParticipants chatType= "
            r1.append(r2)     // Catch:{ all -> 0x006b }
            r1.append(r7)     // Catch:{ all -> 0x006b }
            java.lang.String r2 = " participants="
            r1.append(r2)     // Catch:{ all -> 0x006b }
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r6)     // Catch:{ all -> 0x006b }
            r1.append(r2)     // Catch:{ all -> 0x006b }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x006b }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x006b }
            java.util.HashSet r0 = new java.util.HashSet     // Catch:{ all -> 0x006b }
            r0.<init>()     // Catch:{ all -> 0x006b }
            r1 = 0
            if (r6 == 0) goto L_0x0069
            boolean r2 = r6.isEmpty()     // Catch:{ all -> 0x006b }
            if (r2 == 0) goto L_0x0032
            goto L_0x0069
        L_0x0032:
            com.sec.internal.ims.servicemodules.im.ImPersister r2 = r5.mPersister     // Catch:{ all -> 0x006b }
            java.util.List r2 = r2.queryAllSessionByParticipant(r6, r7)     // Catch:{ all -> 0x006b }
            if (r2 == 0) goto L_0x0060
            boolean r3 = r2.isEmpty()     // Catch:{ all -> 0x006b }
            if (r3 == 0) goto L_0x0041
            goto L_0x0060
        L_0x0041:
            java.util.Iterator r1 = r2.iterator()     // Catch:{ all -> 0x006b }
        L_0x0045:
            boolean r3 = r1.hasNext()     // Catch:{ all -> 0x006b }
            if (r3 == 0) goto L_0x005e
            java.lang.Object r3 = r1.next()     // Catch:{ all -> 0x006b }
            java.lang.String r3 = (java.lang.String) r3     // Catch:{ all -> 0x006b }
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r4 = r5.mImSessions     // Catch:{ all -> 0x006b }
            java.lang.Object r4 = r4.get(r3)     // Catch:{ all -> 0x006b }
            com.sec.internal.ims.servicemodules.im.ImSession r4 = (com.sec.internal.ims.servicemodules.im.ImSession) r4     // Catch:{ all -> 0x006b }
            r0.add(r4)     // Catch:{ all -> 0x006b }
            goto L_0x0045
        L_0x005e:
            monitor-exit(r5)
            return r0
        L_0x0060:
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x006b }
            java.lang.String r4 = "getImSessionByParticipants: Couldn't load from db."
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x006b }
            monitor-exit(r5)
            return r1
        L_0x0069:
            monitor-exit(r5)
            return r1
        L_0x006b:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.getAllImSessionByParticipants(java.util.Set, com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType):java.util.Set");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0073, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.im.FtMessage getFtMessageforFtRequest(java.lang.String r6, java.lang.String r7, long r8, java.lang.String r10) {
        /*
            r5 = this;
            monitor-enter(r5)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0074 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0074 }
            r1.<init>()     // Catch:{ all -> 0x0074 }
            java.lang.String r2 = "getFtMessageforFtRequest chatid:"
            r1.append(r2)     // Catch:{ all -> 0x0074 }
            r1.append(r6)     // Catch:{ all -> 0x0074 }
            java.lang.String r2 = " fileName:"
            r1.append(r2)     // Catch:{ all -> 0x0074 }
            r1.append(r7)     // Catch:{ all -> 0x0074 }
            java.lang.String r2 = " fileSize:"
            r1.append(r2)     // Catch:{ all -> 0x0074 }
            r1.append(r8)     // Catch:{ all -> 0x0074 }
            java.lang.String r2 = " fileTransferId:"
            r1.append(r2)     // Catch:{ all -> 0x0074 }
            r1.append(r10)     // Catch:{ all -> 0x0074 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0074 }
            com.sec.internal.log.IMSLog.s(r0, r1)     // Catch:{ all -> 0x0074 }
            r0 = 0
            if (r6 == 0) goto L_0x0072
            if (r7 == 0) goto L_0x0072
            r1 = 0
            int r1 = (r8 > r1 ? 1 : (r8 == r1 ? 0 : -1))
            if (r1 <= 0) goto L_0x0072
            if (r10 != 0) goto L_0x003d
            goto L_0x0072
        L_0x003d:
            r1 = 0
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r2 = r5.mImSessions     // Catch:{ all -> 0x0074 }
            java.lang.Object r2 = r2.get(r6)     // Catch:{ all -> 0x0074 }
            com.sec.internal.ims.servicemodules.im.ImSession r2 = (com.sec.internal.ims.servicemodules.im.ImSession) r2     // Catch:{ all -> 0x0074 }
            if (r2 == 0) goto L_0x004d
            com.sec.internal.ims.servicemodules.im.FtMessage r3 = r2.findFtMessage(r7, r8, r10)     // Catch:{ all -> 0x0074 }
            r1 = r3
        L_0x004d:
            if (r1 != 0) goto L_0x0070
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x0074 }
            java.lang.String r4 = "getFtMessageforFtRequest Couldn't find a FtMessage in ImSession."
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x0074 }
            com.sec.internal.ims.servicemodules.im.ImPersister r3 = r5.mPersister     // Catch:{ all -> 0x0074 }
            com.sec.internal.ims.servicemodules.im.FtMessage r3 = r3.queryFtMessageByFileTransferId(r10, r6)     // Catch:{ all -> 0x0074 }
            r1 = r3
            if (r1 == 0) goto L_0x0067
            com.sec.internal.ims.servicemodules.im.MessageBase r0 = r5.loadExtras(r1)     // Catch:{ all -> 0x0074 }
            com.sec.internal.ims.servicemodules.im.FtMessage r0 = (com.sec.internal.ims.servicemodules.im.FtMessage) r0     // Catch:{ all -> 0x0074 }
            monitor-exit(r5)
            return r0
        L_0x0067:
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x0074 }
            java.lang.String r4 = "getFtMessageforFtRequest Couldn't find a FtMessage by fileTransferId in db."
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x0074 }
            monitor-exit(r5)
            return r0
        L_0x0070:
            monitor-exit(r5)
            return r1
        L_0x0072:
            monitor-exit(r5)
            return r0
        L_0x0074:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.getFtMessageforFtRequest(java.lang.String, java.lang.String, long, java.lang.String):com.sec.internal.ims.servicemodules.im.FtMessage");
    }

    public Collection<ImSession> getAllImSessions() {
        return this.mImSessions.snapshot().values();
    }

    public Cursor querySessions(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.mPersister.querySessions(projection, selection, selectionArgs, sortOrder);
    }

    public int cloudUpdateSession(String chatId, ContentValues values) {
        return this.mPersister.cloudUpdateSession(chatId, values);
    }

    public Cursor queryMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.mPersister.queryMessages(projection, selection, selectionArgs, sortOrder);
    }

    public Cursor queryChatMessagesForTapi(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.mPersister.queryChatMessagesForTapi(projection, selection, selectionArgs, sortOrder);
    }

    public Cursor queryFtMessagesForTapi(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.mPersister.queryFtMessagesForTapi(projection, selection, selectionArgs, sortOrder);
    }

    public Cursor queryParticipants(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.mPersister.queryParticipants(projection, selection, selectionArgs, sortOrder);
    }

    public Cursor queryMessageNotification(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return this.mPersister.queryMessageNotification(projection, selection, selectionArgs, sortOrder);
    }

    public Uri cloudInsertMessage(Uri uri, ContentValues values) {
        return this.mPersister.cloudInsertMessage(uri, values);
    }

    public synchronized int cloudDeleteMessage(String msgId) {
        String str = LOG_TAG;
        Log.i(str, "cloud delete message: " + msgId);
        int id = Integer.parseInt(msgId);
        MessageBase msg = getMessage(id);
        if (msg != null) {
            if (msg instanceof FtMessage) {
                handleDeleteFtMessage((FtMessage) msg);
            }
            unregisterMessage(msg);
        }
        this.mPersister.deleteMessage(id);
        return 1;
    }

    public int cloudUpdateMessage(String msgId, ContentValues values) {
        return this.mPersister.cloudUpdateMessage(msgId, values);
    }

    public Uri cloudInsertNotification(Uri uri, ContentValues values) {
        return this.mPersister.cloudInsertNotification(uri, values);
    }

    public int cloudupdateNotification(String imdnId, ContentValues values) {
        return this.mPersister.cloudUpdateNotification(imdnId, values);
    }

    public Uri cloudInsertParticipant(Uri uri, ContentValues values) {
        return this.mPersister.cloudInsertParticipant(uri, values);
    }

    public int cloudDeleteParticipant(String id) {
        return this.mPersister.cloudDeleteParticipant(id);
    }

    public int cloudUpdateParticipant(String rowId, ContentValues values) {
        return this.mPersister.cloudUpdateParticipant(rowId, values);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ea, code lost:
        return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int cloudsearchAndInsertSession(android.net.Uri r14, android.content.ContentValues[] r15) {
        /*
            r13 = this;
            monitor-enter(r13)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00fa }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00fa }
            r1.<init>()     // Catch:{ all -> 0x00fa }
            java.lang.String r2 = "cloudsearchAndInsertSession: "
            r1.append(r2)     // Catch:{ all -> 0x00fa }
            r1.append(r14)     // Catch:{ all -> 0x00fa }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00fa }
            com.sec.internal.log.IMSLog.s(r0, r1)     // Catch:{ all -> 0x00fa }
            r0 = 0
            if (r15 == 0) goto L_0x00eb
            int r1 = r15.length     // Catch:{ NullPointerException -> 0x00f4 }
            r2 = 1
            if (r1 >= r2) goto L_0x0020
            goto L_0x00eb
        L_0x0020:
            java.util.HashSet r1 = new java.util.HashSet     // Catch:{ NullPointerException -> 0x00f4 }
            r1.<init>()     // Catch:{ NullPointerException -> 0x00f4 }
            int r3 = r15.length     // Catch:{ NullPointerException -> 0x00f4 }
            r4 = r0
        L_0x0027:
            if (r4 >= r3) goto L_0x003d
            r5 = r15[r4]     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r6 = "uri"
            java.lang.String r6 = r5.getAsString(r6)     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.ims.util.ImsUri r6 = com.sec.ims.util.ImsUri.parse(r6)     // Catch:{ NullPointerException -> 0x00f4 }
            r1.add(r6)     // Catch:{ NullPointerException -> 0x00f4 }
            int r4 = r4 + 1
            goto L_0x0027
        L_0x003d:
            r3 = r15[r0]     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r3 = r13.cloudSessionTranslation(r3)     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r13.mImModule     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r5 = r3.getOwnIMSI()     // Catch:{ NullPointerException -> 0x00f4 }
            int r4 = r4.getPhoneIdByIMSI(r5)     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.ims.servicemodules.im.ImModule r5 = r13.mImModule     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r5 = r5.getRcsStrategy(r4)     // Catch:{ NullPointerException -> 0x00f4 }
            if (r5 == 0) goto L_0x00e9
            com.sec.internal.ims.servicemodules.im.ImModule r5 = r13.mImModule     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r5 = r5.getRcsStrategy(r4)     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r6 = "central_msg_store"
            boolean r5 = r5.boolSetting(r6)     // Catch:{ NullPointerException -> 0x00f4 }
            if (r5 == 0) goto L_0x00e9
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = r3.getChatType()     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r6 = LOG_TAG     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x00f4 }
            r7.<init>()     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r8 = "chatType = "
            r7.append(r8)     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r8 = r5.toString()     // Catch:{ NullPointerException -> 0x00f4 }
            r7.append(r8)     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r7 = r7.toString()     // Catch:{ NullPointerException -> 0x00f4 }
            android.util.Log.i(r6, r7)     // Catch:{ NullPointerException -> 0x00f4 }
            r6 = r15[r0]     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r7 = "conversation_id"
            java.lang.String r6 = r6.getAsString(r7)     // Catch:{ NullPointerException -> 0x00f4 }
            r7 = 0
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r8 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT     // Catch:{ NullPointerException -> 0x00f4 }
            if (r8 != r5) goto L_0x0095
            com.sec.internal.ims.servicemodules.im.ImPersister r8 = r13.mPersister     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r2 = r8.querySessionByConversationId(r6, r2)     // Catch:{ NullPointerException -> 0x00f4 }
            goto L_0x00a3
        L_0x0095:
            com.sec.internal.ims.servicemodules.im.ImPersister r2 = r13.mPersister     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r8 = r3.getOwnIMSI()     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r9 = r3.getChatMode()     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r2 = r2.querySessionByParticipants(r1, r5, r8, r9)     // Catch:{ NullPointerException -> 0x00f4 }
        L_0x00a3:
            if (r2 == 0) goto L_0x00bf
            if (r6 == 0) goto L_0x00b9
            java.lang.String r7 = r2.getConversationId()     // Catch:{ NullPointerException -> 0x00f4 }
            boolean r7 = r6.equals(r7)     // Catch:{ NullPointerException -> 0x00f4 }
            if (r7 == 0) goto L_0x00b9
            r2.setConversationId(r6)     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.ims.servicemodules.im.ImPersister r7 = r13.mPersister     // Catch:{ NullPointerException -> 0x00f4 }
            r7.onSessionUpdated(r2)     // Catch:{ NullPointerException -> 0x00f4 }
        L_0x00b9:
            int r0 = r2.getId()     // Catch:{ NullPointerException -> 0x00f4 }
            monitor-exit(r13)
            return r0
        L_0x00bf:
            r7 = 1
            int r8 = r15.length     // Catch:{ NullPointerException -> 0x00f4 }
            r9 = r0
        L_0x00c2:
            if (r9 >= r8) goto L_0x00da
            r10 = r15[r9]     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant r11 = r13.cloudParticipantTranslation(r10)     // Catch:{ NullPointerException -> 0x00f4 }
            com.sec.internal.ims.servicemodules.im.ImPersister r12 = r13.mPersister     // Catch:{ NullPointerException -> 0x00f4 }
            r12.insertParticipant((com.sec.internal.constants.ims.servicemodules.im.ImParticipant) r11)     // Catch:{ NullPointerException -> 0x00f4 }
            int r12 = r11.getId()     // Catch:{ NullPointerException -> 0x00f4 }
            if (r12 > 0) goto L_0x00d7
            r7 = 0
            goto L_0x00da
        L_0x00d7:
            int r9 = r9 + 1
            goto L_0x00c2
        L_0x00da:
            if (r7 == 0) goto L_0x00e7
            com.sec.internal.ims.servicemodules.im.ImPersister r8 = r13.mPersister     // Catch:{ NullPointerException -> 0x00f4 }
            r8.insertSession(r3)     // Catch:{ NullPointerException -> 0x00f4 }
            int r0 = r3.getId()     // Catch:{ NullPointerException -> 0x00f4 }
            monitor-exit(r13)
            return r0
        L_0x00e7:
            monitor-exit(r13)
            return r0
        L_0x00e9:
            monitor-exit(r13)
            return r0
        L_0x00eb:
            java.lang.String r1 = LOG_TAG     // Catch:{ NullPointerException -> 0x00f4 }
            java.lang.String r2 = "cloudsearchAndInsertSession: no values inserted"
            android.util.Log.i(r1, r2)     // Catch:{ NullPointerException -> 0x00f4 }
            monitor-exit(r13)
            return r0
        L_0x00f4:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x00fa }
            monitor-exit(r13)
            return r0
        L_0x00fa:
            r14 = move-exception
            monitor-exit(r13)
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.cloudsearchAndInsertSession(android.net.Uri, android.content.ContentValues[]):int");
    }

    private ChatData cloudSessionTranslation(ContentValues cv) {
        Integer direction;
        Integer chatType;
        Integer chatMode;
        ContentValues contentValues = cv;
        String chatId = contentValues.getAsString("chat_id");
        String ownNumber = contentValues.getAsString(ImContract.ImSession.OWN_PHONE_NUMBER);
        Integer direction2 = contentValues.getAsInteger("direction");
        if (direction2 == null) {
            direction = Integer.valueOf(ImDirection.INCOMING.getId());
        } else {
            direction = direction2;
        }
        String convId = contentValues.getAsString("conversation_id");
        String contrId = contentValues.getAsString("contribution_id");
        boolean is_group_chat = (contentValues.getAsInteger("is_group_chat") == null || contentValues.getAsInteger("is_group_chat").intValue() == 0) ? false : true;
        String subject = contentValues.getAsString("subject");
        Integer chatType2 = contentValues.getAsInteger(ImContract.ImSession.CHAT_TYPE);
        if (chatType2 == null) {
            chatType = Integer.valueOf((is_group_chat ? ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT : ChatData.ChatType.ONE_TO_ONE_CHAT).getId());
        } else {
            chatType = chatType2;
        }
        Integer chatMode2 = contentValues.getAsInteger(ImContract.ImSession.CHAT_MODE);
        String str = LOG_TAG;
        Log.i(str, "set own sim imsi: " + contentValues.getAsString("sim_imsi"));
        if (chatMode2 == null) {
            chatMode = Integer.valueOf(ChatMode.OFF.getId());
        } else {
            chatMode = chatMode2;
        }
        return new ChatData(chatId, ownNumber, "", subject, ChatData.ChatType.fromId(chatType.intValue()), ImDirection.fromId(direction.intValue()), convId, contrId, contentValues.getAsString("sim_imsi"), (String) null, ChatMode.fromId(chatMode.intValue()), (ImsUri) null);
    }

    private ImParticipant cloudParticipantTranslation(ContentValues cv) {
        return new ImParticipant(cv.getAsString("chat_id"), ImsUri.parse(cv.getAsString("uri")));
    }

    public synchronized MessageBase getMessage(int id) {
        MessageBase msg = this.mPendingMessages.get(id);
        if (msg != null) {
            return msg;
        }
        MessageBase msg2 = this.mPersister.queryMessage(String.valueOf(id));
        if (msg2 == null) {
            return null;
        }
        return loadExtras(msg2);
    }

    public synchronized List<MessageBase> getMessages(Collection<String> ids) {
        List<MessageBase> messages;
        messages = new ArrayList<>();
        List<String> messageIdsForQuerying = new ArrayList<>();
        for (String idstr : ids) {
            try {
                MessageBase msg = this.mPendingMessages.get(Integer.valueOf(idstr).intValue());
                if (msg != null) {
                    messages.add(msg);
                } else {
                    messageIdsForQuerying.add(idstr);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (!messageIdsForQuerying.isEmpty()) {
            for (MessageBase m : this.mPersister.queryMessages((Collection<String>) messageIdsForQuerying)) {
                loadExtras(m);
                messages.add(m);
            }
        }
        return messages;
    }

    public MessageBase getPendingMessage(int id) {
        return this.mPendingMessages.get(id);
    }

    public List<MessageBase> getAllPendingMessages(String chatId) {
        return this.mPendingMessages.getAll(chatId);
    }

    public ImMessage getImMessage(int id) {
        MessageBase msg = getMessage(id);
        if (msg instanceof ImMessage) {
            return (ImMessage) msg;
        }
        return null;
    }

    public ImMessage getImMessage(String imdnId, ImDirection direction, String cid, String ownImsi) {
        MessageBase msg = getMessage(imdnId, direction, cid, ownImsi);
        if (msg instanceof ImMessage) {
            return (ImMessage) msg;
        }
        return null;
    }

    public FtMessage getFtMessage(int id) {
        MessageBase msg = getMessage(id);
        if (msg instanceof FtMessage) {
            return (FtMessage) msg;
        }
        return null;
    }

    public FtMsrpMessage getFtMsrpMessage(Object rawHandle) {
        if (rawHandle == null) {
            return null;
        }
        for (MessageBase msg : this.mPendingMessages.getAll()) {
            if (msg instanceof FtMsrpMessage) {
                FtMsrpMessage ftMsrpMessage = (FtMsrpMessage) msg;
                if (rawHandle.equals(ftMsrpMessage.getRawHandle())) {
                    return ftMsrpMessage;
                }
            }
        }
        return null;
    }

    public synchronized List<MessageBase> getMessagesForPendingNotificationByChatId(String chatId) {
        List<MessageBase> messages;
        List<Integer> messageIds = this.mPersister.queryMessageIdsForPendingNotification(chatId);
        String str = LOG_TAG;
        Log.i(str, "pending notifications count:" + messageIds.size());
        messages = new ArrayList<>();
        List<String> messageIdsForQuerying = new ArrayList<>();
        for (Integer intValue : messageIds) {
            int id = intValue.intValue();
            if (this.mPendingMessages.containsKey(id)) {
                messages.add(this.mPendingMessages.get(id));
            } else {
                messageIdsForQuerying.add(String.valueOf(id));
            }
        }
        for (MessageBase m : this.mPersister.queryMessages((Collection<String>) messageIdsForQuerying)) {
            loadExtras(m);
            messages.add(m);
        }
        return messages;
    }

    public synchronized MessageBase getMessage(String imdnId, ImDirection direction, String cid, String ownImsi) {
        if (TextUtils.isEmpty(imdnId)) {
            return null;
        }
        MessageBase msg = this.mPendingMessages.get(imdnId, direction, cid);
        if (msg != null) {
            return msg;
        }
        MessageBase msg2 = this.mPersister.queryMessage(imdnId, direction, cid, ownImsi);
        if (msg2 == null) {
            return null;
        }
        return loadExtras(msg2);
    }

    public synchronized MessageBase getMessage(String imdnId, ImDirection direction) {
        if (TextUtils.isEmpty(imdnId)) {
            return null;
        }
        MessageBase msg = this.mPendingMessages.get(imdnId, direction);
        if (msg != null) {
            return msg;
        }
        return this.mCachingMessages.get(new Pair(imdnId, direction));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x003f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.im.MessageBase loadExtras(com.sec.internal.ims.servicemodules.im.MessageBase r6) {
        /*
            r5 = this;
            monitor-enter(r5)
            com.sec.internal.ims.servicemodules.im.ImPersister r0 = r5.mPersister     // Catch:{ all -> 0x0068 }
            java.util.List r0 = r0.queryImImdnRecRoute(r6)     // Catch:{ all -> 0x0068 }
            com.sec.internal.ims.servicemodules.im.ImModule r1 = r5.mImModule     // Catch:{ all -> 0x0068 }
            java.lang.String r2 = r6.getOwnIMSI()     // Catch:{ all -> 0x0068 }
            int r1 = r1.getPhoneIdByIMSI(r2)     // Catch:{ all -> 0x0068 }
            r6.setImdnRecRouteList(r0)     // Catch:{ all -> 0x0068 }
            boolean r2 = r6 instanceof com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage     // Catch:{ all -> 0x0068 }
            if (r2 != 0) goto L_0x0028
            boolean r2 = r6 instanceof com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage     // Catch:{ all -> 0x0068 }
            if (r2 == 0) goto L_0x001d
            goto L_0x0028
        L_0x001d:
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r5.mImModule     // Catch:{ all -> 0x0068 }
            r3 = 0
            android.net.Network r2 = r2.getNetwork(r3, r1)     // Catch:{ all -> 0x0068 }
            r6.setNetwork(r2)     // Catch:{ all -> 0x0068 }
            goto L_0x003b
        L_0x0028:
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r5.mImModule     // Catch:{ all -> 0x0068 }
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r5.mImModule     // Catch:{ all -> 0x0068 }
            com.sec.internal.ims.servicemodules.im.ImConfig r3 = r3.getImConfig(r1)     // Catch:{ all -> 0x0068 }
            boolean r3 = r3.isFtHttpOverDefaultPdn()     // Catch:{ all -> 0x0068 }
            android.net.Network r2 = r2.getNetwork(r3, r1)     // Catch:{ all -> 0x0068 }
            r6.setNetwork(r2)     // Catch:{ all -> 0x0068 }
        L_0x003b:
            boolean r2 = r6 instanceof com.sec.internal.ims.servicemodules.im.FtMessage     // Catch:{ all -> 0x0068 }
            if (r2 == 0) goto L_0x0063
            r2 = r6
            com.sec.internal.ims.servicemodules.im.FtMessage r2 = (com.sec.internal.ims.servicemodules.im.FtMessage) r2     // Catch:{ all -> 0x0068 }
            com.sec.internal.ims.servicemodules.im.ImPersister r3 = r5.mPersister     // Catch:{ all -> 0x0068 }
            java.lang.String r4 = r2.getChatId()     // Catch:{ all -> 0x0068 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r3 = r3.querySessionByChatId(r4)     // Catch:{ all -> 0x0068 }
            if (r3 == 0) goto L_0x0063
            boolean r4 = r3.isGroupChat()     // Catch:{ all -> 0x0068 }
            r2.setIsGroupChat(r4)     // Catch:{ all -> 0x0068 }
            java.lang.String r4 = r3.getContributionId()     // Catch:{ all -> 0x0068 }
            r2.setContributionId(r4)     // Catch:{ all -> 0x0068 }
            java.lang.String r4 = r3.getConversationId()     // Catch:{ all -> 0x0068 }
            r2.setConversationId(r4)     // Catch:{ all -> 0x0068 }
        L_0x0063:
            r5.registerObserver(r6)     // Catch:{ all -> 0x0068 }
            monitor-exit(r5)
            return r6
        L_0x0068:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.loadExtras(com.sec.internal.ims.servicemodules.im.MessageBase):com.sec.internal.ims.servicemodules.im.MessageBase");
    }

    public List<String> getMessageIdsForDisplayAggregation(String chatId, ImDirection direction, Long timestamp) {
        List<Integer> messageIds = this.mPersister.queryMessageIdsForDisplayAggregation(chatId, direction, timestamp);
        List<String> list = new ArrayList<>();
        for (Integer intValue : messageIds) {
            list.add(String.valueOf(intValue.intValue()));
        }
        String str = LOG_TAG;
        Log.i(str, "getMessageIdsForDisplayAggregation: list=" + list);
        return list;
    }

    public Set<ImParticipant> getParticipants(String chatId) {
        ImSession session = getImSession(chatId);
        if (session != null) {
            return session.getParticipants();
        }
        return null;
    }

    public NotificationStatus getNotificationStatus(String imdnId, ImsUri remoteUri) {
        if (imdnId == null || remoteUri == null) {
            return null;
        }
        return this.mPersister.queryNotificationStatus(imdnId, remoteUri);
    }

    public synchronized MessageBase queryMessageForOpenApi(String msgId) {
        return this.mPersister.queryMessage(msgId);
    }

    public synchronized ImSession makeNewOutgoingSession(String imsi, Set<ImsUri> participants, ChatData.ChatType chatType, String subject, String sdpContentType, int threadId, String requestMessageId, String iconPath, ChatMode chatMode) {
        try {
        } catch (Throwable th) {
            throw th;
        }
        return makeNewOutgoingSession(imsi, participants, chatType, subject, sdpContentType, threadId, requestMessageId, iconPath, chatMode, (String) null, (String) null, (ImsUri) null);
    }

    public synchronized ImSession makeNewOutgoingSession(String imsi, Set<ImsUri> participants, ChatData.ChatType chatType, String subject, String sdpContentType, int threadId, String requestMessageId, String iconPath, ChatMode chatMode, String conversationId, String contributionId, ImsUri sessionUri) {
        String contributionId2;
        String conversationId2;
        ImSession session;
        String str = imsi;
        Set<ImsUri> set = participants;
        ChatData.ChatType chatType2 = chatType;
        synchronized (this) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "makeNewOutgoingSession: chatType=" + chatType2 + " participants=" + IMSLog.checker(participants) + " imsi= " + IMSLog.checker(imsi));
            int phoneId = this.mImModule.getPhoneIdByIMSI(str);
            if (!TextUtils.isEmpty(conversationId) || !TextUtils.isEmpty(contributionId)) {
                conversationId2 = conversationId;
                contributionId2 = contributionId;
            } else if (this.mImModule.getImConfig(phoneId).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
                conversationId2 = StringIdGenerator.generateConversationId();
                contributionId2 = ChatData.ChatType.isGroupChat(chatType) ? conversationId2 : StringIdGenerator.generateContributionId();
            } else {
                contributionId2 = StringIdGenerator.generateContributionId();
                conversationId2 = conversationId;
            }
            session = new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig(phoneId)).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator(phoneId)).chatId(StringIdGenerator.generateChatId(set, str, ChatData.ChatType.isGroupChat(chatType), chatMode.getId())).participantsUri(set).chatType(chatType2).chatMode(chatMode).ownPhoneNum(this.mImModule.getOwnPhoneNum(phoneId)).ownSimIMSI(str).ownGroupAlias("").subject(subject).iconPath(iconPath).sdpContentType(sdpContentType).threadId(threadId).requestMessageId(requestMessageId).contributionId(contributionId2).conversationId(conversationId2).direction(ImDirection.OUTGOING).getter(this.mImModule).sessionUri(sessionUri).build();
            registerSession(session);
            registerParticipant(session.getParticipants());
            this.mCmStoreInvoker.onCreateSession(phoneId, session);
        }
        return session;
    }

    public synchronized ImSession makeNewIncomingSession(ImIncomingSessionEvent event, Set<ImsUri> participants, ChatData.ChatType chatType, ChatMode chatMode) {
        ImSession session;
        String str = LOG_TAG;
        Log.i(str, "makeNewIncomingSession: chatType=" + chatType + " participants=" + IMSLog.checker(participants));
        int phoneId = this.mImModule.getPhoneIdByIMSI(event.mOwnImsi);
        session = new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig()).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator()).chatId(StringIdGenerator.generateChatId(participants, event.mOwnImsi, ChatData.ChatType.isGroupChat(chatType), chatMode.getId())).participantsUri(participants).chatType(chatType).chatMode(chatMode).ownPhoneNum(this.mImModule.getOwnPhoneNum(phoneId)).ownSimIMSI(event.mOwnImsi).ownGroupAlias("").subject(event.mSubject).contributionId(event.mContributionId).conversationId(event.mConversationId).sdpContentType(event.mSdpContentType).direction(ImDirection.INCOMING).rawHandle(event.mIsDeferred ? null : event.mRawHandle).sessionType(event.mSessionType).getter(this.mImModule).build();
        registerSession(session);
        registerParticipant(session.getParticipants());
        this.mCmStoreInvoker.onCreateSession(phoneId, session);
        return session;
    }

    public ImSession makeNewEmptySession(String ownImsi, Set<ImsUri> participants, ChatData.ChatType chatType, ImDirection direction) {
        return makeNewEmptySession(ownImsi, participants, chatType, direction, ChatMode.OFF);
    }

    public synchronized ImSession makeNewEmptySession(String ownImsi, Set<ImsUri> participants, ChatData.ChatType chatType, ImDirection direction, ChatMode chatMode) {
        String contributionId;
        ImSession session;
        String str = LOG_TAG;
        Log.i(str, "makeNewEmptySession: chatType=" + chatType + " participants=" + IMSLog.checker(participants) + " ownImsi= " + IMSLog.checker(ownImsi));
        String conversationId = null;
        int phoneId = this.mImModule.getPhoneIdByIMSI(ownImsi);
        if (this.mImModule.getImConfig(phoneId).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
            conversationId = StringIdGenerator.generateConversationId();
            contributionId = ChatData.ChatType.isGroupChat(chatType) ? conversationId : StringIdGenerator.generateContributionId();
        } else {
            contributionId = StringIdGenerator.generateContributionId();
        }
        session = new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig(phoneId)).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator(phoneId)).chatId(StringIdGenerator.generateChatId(participants, ownImsi, ChatData.ChatType.isGroupChat(chatType), chatMode.getId())).participantsUri(participants).chatType(chatType).chatMode(chatMode).ownSimIMSI(ownImsi).ownPhoneNum(this.mImModule.getOwnPhoneNum(phoneId)).contributionId(contributionId).conversationId(conversationId).direction(direction).getter(this.mImModule).build();
        registerSession(session);
        registerParticipant(session.getParticipants());
        this.mCmStoreInvoker.onCreateSession(phoneId, session);
        return session;
    }

    public synchronized ImMessage makeNewOutgoingMessage(String imsi, ImSession session, String body, Set<NotificationStatus> disposition, String contentType, String requestMessageId, boolean isSlm, boolean isPublicAccountMsg, boolean isBroadcastMsg, boolean isGLSMsg, String deviceName, String reliableMessage, String xmsMessage, boolean isTemporary, String maapTrafficType) {
        try {
        } catch (Throwable th) {
            throw th;
        }
        return makeNewOutgoingMessage(imsi, session, body, disposition, contentType, requestMessageId, isSlm, isPublicAccountMsg, isBroadcastMsg, isGLSMsg, deviceName, reliableMessage, xmsMessage, isTemporary, maapTrafficType, (String) null, (String) null, (String) null);
    }

    public synchronized ImMessage makeNewOutgoingMessage(String imsi, ImSession session, String body, Set<NotificationStatus> disposition, String contentType, String requestMessageId, boolean isSlm, boolean isPublicAccountMsg, boolean isBroadcastMsg, boolean isGLSMsg, String deviceName, String reliableMessage, String xmsMessage, boolean isTemporary, String maapTrafficType, String referenceMessageId, String referenceMessageType, String referenceMessageValue) {
        ImConstants.Type msgType;
        ImMessage msg;
        String str = imsi;
        synchronized (this) {
            if (isPublicAccountMsg) {
                try {
                    msgType = ImConstants.Type.TEXT_PUBLICACCOUNT;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (isGLSMsg) {
                msgType = ImConstants.Type.LOCATION;
            } else {
                msgType = MessageBase.getType(contentType);
            }
            int flagMask = 0;
            if (isTemporary) {
                flagMask = 2;
            }
            int phoneId = this.mImModule.getPhoneIdByIMSI(str);
            ImConstants.Type type = msgType;
            msg = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).chatId(session.getChatId())).remoteUri(session.isGroupChat() ? null : ImsUri.parse(session.getParticipantsString().get(0)))).body(body)).userAlias(this.mImModule.getUserAlias(phoneId))).imdnId(StringIdGenerator.generateImdn())).dispNotification(disposition)).contentType(contentType)).direction(ImDirection.OUTGOING)).status(ImConstants.Status.TO_SEND)).type(msgType)).notDisplayedCounter(session.getParticipantsSize())).requestMessageId(requestMessageId)).insertedTimestamp(System.currentTimeMillis())).isSlmSvcMsg(isSlm)).isBroadcastMsg(isBroadcastMsg)).deviceName(deviceName)).reliableMessage(reliableMessage)).xmsMessage(xmsMessage)).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).simIMSI(str)).maapTrafficType(maapTrafficType)).messagingTech(ImConstants.MessagingTech.NORMAL)).flagMask(flagMask)).referenceId(referenceMessageId)).referenceType(referenceMessageType)).referenceValue(referenceMessageValue)).build();
            registerMessage(msg);
            addToPendingList(msg);
        }
        return msg;
    }

    public synchronized ImMessage makeNewIncomingMessage(String imsi, ImSession session, ImIncomingMessageEvent event, Network network, String suggestion) {
        ImMessage msg;
        int phoneId = this.mImModule.getPhoneIdByIMSI(imsi);
        ImDirection direction = ImCpimNamespacesHelper.extractImDirection(phoneId, event.mCpimNamespaces);
        String maapTrafficType = ImCpimNamespacesHelper.extractMaapTrafficType(event.mCpimNamespaces);
        String referenceId = ImCpimNamespacesHelper.extractRcsReferenceId(event.mCpimNamespaces);
        String referenceType = ImCpimNamespacesHelper.extractRcsReferenceType(event.mCpimNamespaces);
        msg = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).body(event.mBody)).suggestion(suggestion)).remoteUri(this.mImModule.normalizeUri(phoneId, event.mSender))).userAlias(event.mUserAlias)).imdnId(event.mImdnMessageId)).imdnIdOriginalTo(event.mOriginalToHdr)).direction(direction)).type(MessageBase.getType(event.mContentType))).contentType(event.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(event.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis())).sentTimestamp(event.mImdnTime != null ? event.mImdnTime.getTime() : System.currentTimeMillis())).imdnRecordRouteList(event.mImdnRecRouteList)).deviceName(event.mDeviceName)).reliableMessage(event.mReliableMessage)).extraFt(event.mExtraFt)).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).notDisplayedCounter(direction == ImDirection.OUTGOING ? session.getParticipantsSize() : 0)).isRoutingMsg(event.mIsRoutingMsg)).routingType(event.mRoutingType)).network(network)).conversationId(session.getConversationId())).contributionId(session.getContributionId())).deviceId(event.mDeviceId)).simIMSI(imsi)).maapTrafficType(maapTrafficType)).referenceId(referenceId)).referenceType(referenceType)).referenceValue(ImCpimNamespacesHelper.extractRcsReferenceValue(event.mCpimNamespaces))).rcsTrafficType(ImCpimNamespacesHelper.extractRcsTrafficType(event.mCpimNamespaces))).build();
        registerMessage(msg);
        this.mCmStoreInvoker.onReceiveRcsMessage(phoneId, msg);
        return msg;
    }

    public synchronized ImMessage makeNewIncomingMessage(String imsi, ImSession session, SlmIncomingMessageEvent event, Network network, String suggestion) {
        ImMessage msg;
        ImConstants.Type msgType = MessageBase.getType(event.mContentType);
        if (event.mIsPublicAccountMsg) {
            msgType = ImConstants.Type.TEXT_PUBLICACCOUNT;
        }
        if (event.mBody.toLowerCase().startsWith("geo")) {
            msgType = ImConstants.Type.LOCATION;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(imsi);
        ImConstants.MessagingTech messagingTech = event.mIsLMM ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE;
        ImDirection direction = ImCpimNamespacesHelper.extractImDirection(phoneId, event.mCpimNamespaces);
        msg = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).body(event.mBody)).suggestion(suggestion)).remoteUri(this.mImModule.normalizeUri(phoneId, event.mSender))).userAlias(event.mUserAlias)).imdnId(event.mImdnMessageId)).imdnIdOriginalTo(event.mOriginalToHdr)).direction(direction)).type(msgType)).isSlmSvcMsg(true)).contentType(event.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(event.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis())).sentTimestamp(event.mImdnTime != null ? event.mImdnTime.getTime() : System.currentTimeMillis())).imdnRecordRouteList(event.mImdnRecRouteList)).deviceName(event.mDeviceName)).reliableMessage(event.mReliableMessage)).extraFt(event.mExtraFt)).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).notDisplayedCounter(direction == ImDirection.OUTGOING ? session.getParticipantsSize() : 0)).isRoutingMsg(event.mIsRoutingMsg)).routingType(event.mRoutingType)).isVM2TextMsg(ImExtensionMNOHeadersHelper.isVM2TextMsg(event.mImExtensionMNOHeaders))).network(network)).conversationId(event.mConversationId)).contributionId(event.mContributionId)).simIMSI(imsi)).maapTrafficType(ImCpimNamespacesHelper.extractMaapTrafficType(event.mCpimNamespaces))).messagingTech(messagingTech)).rcsTrafficType(ImCpimNamespacesHelper.extractRcsTrafficType(event.mCpimNamespaces))).build();
        registerMessage(msg);
        this.mCmStoreInvoker.onReceiveRcsMessage(phoneId, msg);
        return msg;
    }

    public synchronized ImMessage makeNewSystemUserMessage(ImSession session, String participants, ImConstants.Type type, Date date) {
        ImMessage msg;
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getOwnImsi());
        msg = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).body(participants)).imdnId(StringIdGenerator.generateImdn())).direction(ImDirection.IRRELEVANT)).status(ImConstants.Status.UNREAD)).type(type)).insertedTimestamp(System.currentTimeMillis())).sentTimestamp(date == null ? System.currentTimeMillis() : date.getTime())).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).build();
        registerMessage(msg);
        return msg;
    }

    public synchronized ImMessage makeNewSystemUserMessage(ImSession session, String participants, ImConstants.Type type) {
        return makeNewSystemUserMessage(session, participants, type, (Date) null);
    }

    public synchronized FtMessage makeNewIncomingFtMessage(String imsi, ImSession session, FtIncomingSessionEvent event, boolean isSlmSvcMsg) {
        FtMessage msg;
        ImConstants.Type msgType = FtMessage.getType(event.mContentType);
        if (event.mIsPublicAccountMsg) {
            msgType = ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(imsi);
        ImDirection direction = ImCpimNamespacesHelper.extractImDirection(phoneId, event.mCpimNamespaces);
        String maapTrafficType = ImCpimNamespacesHelper.extractMaapTrafficType(event.mCpimNamespaces);
        String str = LOG_TAG;
        Log.i(str, "makeNewIncomingFtMessage msgType: " + msgType);
        ImConstants.MessagingTech messagingTech = ImConstants.MessagingTech.NORMAL;
        if (event.mIsSlmSvcMsg) {
            messagingTech = event.mIsLMM ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE;
        }
        int i = 0;
        FtMsrpMessage.Builder builder = (FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) FtMsrpMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).direction(direction)).filePath(event.mFilePath != null ? event.mFilePath : event.mFileName)).fileName(event.mFileName)).fileSize(event.mFileSize)).thumbnailPath(event.mThumbPath)).thumbnailTool(this.mImModule.getFtProcessor().getThumbnailTool())).timeDuration(event.mTimeDuration)).remoteUri(this.mImModule.normalizeUri(phoneId, event.mSenderUri))).userAlias(event.mUserAlias)).rawHandle(event.mRawHandle).isGroupChat(session.isGroupChat())).status(ImConstants.Status.UNREAD)).type(msgType)).isSlmSvcMsg(isSlmSvcMsg)).contentType(event.mContentType)).insertedTimestamp(System.currentTimeMillis())).conversationId(session.getConversationId())).contributionId(event.mContributionId)).inReplyToConversationId(event.mInReplyToConversationId)).imdnId(event.mImdnId)).imdnIdOriginalTo(event.mOriginalToHdr)).dispNotification(event.mDisposition)).fileTransferId(event.mFileTransferId)).setState(0)).sentTimestamp(event.mImdnTime != null ? event.mImdnTime.getTime() : System.currentTimeMillis())).imdnRecordRouteList(event.mRecRouteList)).deviceName(event.mDeviceName)).reliableMessage(event.mReliableMessage)).extraFt(event.mExtraFt)).mnoStrategy(this.mImModule.getRcsStrategy(phoneId));
        if (direction == ImDirection.OUTGOING) {
            i = session.getParticipantsSize();
        }
        msg = ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) builder.notDisplayedCounter(i)).isRoutingMsg(event.mIsRoutingMsg)).routingType(event.mRoutingType)).deviceId(session.getDeviceId())).simIMSI(imsi)).maapTrafficType(maapTrafficType)).messagingTech(messagingTech)).build();
        registerMessage(msg);
        addToPendingList(msg);
        return msg;
    }

    public synchronized FtMessage makeNewOutgoingFtMessage(String imsi, ImSession session, String filePath, ImsUri contactUri, Set<NotificationStatus> disposition, String requestMessageId, String contentType, boolean isPublicAccountMsg, boolean isResizable, boolean isBroadcastMsg, String extInfo) {
        String contentType2;
        FtMessage msg;
        String str = imsi;
        String str2 = filePath;
        synchronized (this) {
            String inReplyToContributionId = null;
            if (session.getDirection() == ImDirection.INCOMING) {
                inReplyToContributionId = session.getInReplyToContributionId();
                session.setDirection(ImDirection.OUTGOING);
            } else {
                ImSession imSession = session;
            }
            File file = new File(str2);
            long fileSize = file.length();
            String fileName = file.getName();
            int phoneId = this.mImModule.getPhoneIdByIMSI(str);
            if (contentType == null) {
                contentType2 = getContentType(file);
            } else {
                contentType2 = contentType;
            }
            ImConstants.Type msgType = FtMessage.getType(contentType2);
            if (isPublicAccountMsg) {
                msgType = ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT;
            }
            String str3 = LOG_TAG;
            Log.i(str3, "makeNewOutgoingFtMessage msgType: " + msgType);
            msg = ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) FtMsrpMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneId))).thumbnailTool(this.mImModule.getFtProcessor().getThumbnailTool())).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).direction(ImDirection.OUTGOING)).filePath(str2)).fileName(fileName)).fileSize(fileSize)).thumbnailPath((String) null)).timeDuration(0)).remoteUri(contactUri)).userAlias(this.mImModule.getUserAlias(phoneId))).contributionId(StringIdGenerator.generateContributionId())).isGroupChat(session.isGroupChat())).status(ImConstants.Status.TO_SEND)).type(msgType)).contentType(contentType2)).insertedTimestamp(System.currentTimeMillis())).conversationId(session.getConversationId())).inReplyToConversationId(inReplyToContributionId)).dispNotification(disposition)).imdnId(StringIdGenerator.generateImdn())).fileTransferId(StringIdGenerator.generateFileTransferId())).setState(0)).notDisplayedCounter(session.getParticipantsSize())).requestMessageId(requestMessageId)).isResizable(isResizable)).isBroadcastMsg(isBroadcastMsg)).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).extinfo(extInfo)).simIMSI(str)).messagingTech(ImConstants.MessagingTech.NORMAL)).build();
            registerMessage(msg);
            addToPendingList(msg);
        }
        return msg;
    }

    public synchronized FtHttpOutgoingMessage makeNewOutgoingFtHttpMessage(String imsi, ImSession session, String filePath, ImsUri contactUri, Set<NotificationStatus> disposition, String requestMessageId, String contentType, boolean extraFt, Network network, boolean Ftsms, boolean isBroadcastMsg, boolean isSlm, FileDisposition fileDisposition, boolean isResizable) {
        String contentType2;
        FtHttpOutgoingMessage msg;
        String str = imsi;
        String str2 = filePath;
        boolean z = extraFt;
        synchronized (this) {
            File file = new File(str2);
            long fileSize = file.length();
            String fileName = file.getName();
            int phoneId = this.mImModule.getPhoneIdByIMSI(str);
            if (contentType == null) {
                contentType2 = getContentType(file);
            } else {
                contentType2 = contentType;
            }
            msg = ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) FtHttpOutgoingMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).filePath(str2)).fileName(fileName)).fileSize(fileSize)).contentType(contentType2)).remoteUri(contactUri)).userAlias(this.mImModule.getUserAlias(phoneId))).imdnId(StringIdGenerator.generateImdn())).direction(ImDirection.OUTGOING)).type(FtMessage.getType(contentType2))).status(ImConstants.Status.TO_SEND)).dispNotification(disposition)).insertedTimestamp(System.currentTimeMillis())).setState(0)).notDisplayedCounter(session.getParticipantsSize())).requestMessageId(requestMessageId)).isGroupChat(session.isGroupChat())).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).setFileDisposition(fileDisposition)).network(network)).extraFt(z)).isBroadcastMsg(isBroadcastMsg)).isSlmSvcMsg(isSlm)).simIMSI(str)).isResizable(isResizable)).build();
            registerMessage(msg);
            msg.setExtraFt(z);
            msg.setFtSms(Ftsms);
            addToPendingList(msg);
        }
        return msg;
    }

    public synchronized FtHttpIncomingMessage makeNewIncomingFtHttpMessage(String imsi, ImSession session, ImIncomingMessageEvent event, Network network, String suggestion) {
        FtHttpIncomingMessage msg;
        int phoneId = this.mImModule.getPhoneIdByIMSI(imsi);
        ImDirection direction = ImCpimNamespacesHelper.extractImDirection(phoneId, event.mCpimNamespaces);
        msg = ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) FtHttpIncomingMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).body(event.mBody)).remoteUri(this.mImModule.normalizeUri(phoneId, event.mSender))).userAlias(event.mUserAlias)).imdnId(event.mImdnMessageId)).imdnIdOriginalTo(event.mOriginalToHdr)).direction(direction)).type(FtMessage.getType(event.mContentType))).contentType(event.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(event.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis())).sentTimestamp(event.mImdnTime != null ? event.mImdnTime.getTime() : System.currentTimeMillis())).setState(0)).imdnRecordRouteList(event.mImdnRecRouteList)).reliableMessage(event.mReliableMessage)).extraFt(event.mExtraFt)).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).network(network)).conversationId(session.getConversationId())).contributionId(session.getContributionId())).deviceId(session.getDeviceId())).simIMSI(imsi)).suggestion(suggestion)).maapTrafficType(ImCpimNamespacesHelper.extractMaapTrafficType(event.mCpimNamespaces))).isGroupChat(session.isGroupChat())).rcsTrafficType(ImCpimNamespacesHelper.extractRcsTrafficType(event.mCpimNamespaces))).build();
        registerMessage(msg);
        addToPendingList(msg);
        return msg;
    }

    public synchronized FtHttpIncomingMessage makeNewIncomingFtHttpMessage(String imsi, ImSession session, SlmIncomingMessageEvent event, Network network, String suggestion) {
        FtHttpIncomingMessage msg;
        int phoneId = this.mImModule.getPhoneIdByIMSI(imsi);
        ImDirection direction = ImCpimNamespacesHelper.extractImDirection(phoneId, event.mCpimNamespaces);
        msg = ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) FtHttpIncomingMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneId))).uriGenerator(this.mImModule.getUriGenerator(phoneId))).chatId(session.getChatId())).body(event.mBody)).remoteUri(this.mImModule.normalizeUri(phoneId, event.mSender))).userAlias(event.mUserAlias)).imdnId(event.mImdnMessageId)).imdnIdOriginalTo(event.mOriginalToHdr)).direction(direction)).type(FtMessage.getType(event.mContentType))).isSlmSvcMsg(true)).contentType(event.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(event.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis())).sentTimestamp(event.mImdnTime != null ? event.mImdnTime.getTime() : System.currentTimeMillis())).setState(0)).imdnRecordRouteList(event.mImdnRecRouteList)).reliableMessage(event.mReliableMessage)).extraFt(event.mExtraFt)).mnoStrategy(this.mImModule.getRcsStrategy(phoneId))).network(network)).conversationId(session.getConversationId())).contributionId(session.getContributionId())).deviceId(session.getDeviceId())).simIMSI(imsi)).suggestion(suggestion)).maapTrafficType(ImCpimNamespacesHelper.extractMaapTrafficType(event.mCpimNamespaces))).rcsTrafficType(ImCpimNamespacesHelper.extractRcsTrafficType(event.mCpimNamespaces))).build();
        registerMessage(msg);
        addToPendingList(msg);
        return msg;
    }

    public synchronized void addParticipant(Collection<ImParticipant> participants) {
        registerParticipant(participants);
    }

    public synchronized void deleteParticipant(Collection<ImParticipant> participants) {
        unregisterParticipant(participants);
    }

    public synchronized void updateParticipant(Collection<ImParticipant> participants) {
        updateParticipant(participants, ImCacheAction.UPDATED);
    }

    public synchronized void deleteSession(ImSession session) {
        unregisterMessage(this.mPendingMessages.getAll(session.getChatId()));
        this.mPersister.deleteParticipant((Collection<ImParticipant>) session.getParticipants());
        unregisterSession(session);
        removeActiveSession(session);
    }

    private void handleDeleteFtMessage(FtMessage ftMsg) {
        String str = LOG_TAG;
        Log.i(str, "handleDeleteFtMessage: msgId:" + ftMsg.getId() + " direction:" + ftMsg.getDirection() + " transferState:" + ftMsg.getStateId());
        ftMsg.removeAutoResumeFileTimer();
        if (ftMsg.getStateId() != 3) {
            ftMsg.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
        }
        if (ftMsg.getDirection() == ImDirection.INCOMING) {
            boolean isFileDeleted = ftMsg.deleteFile();
            boolean isThumbnailDeleted = ftMsg.deleteThumbnail();
            String str2 = LOG_TAG;
            Log.i(str2, "handleDeleteFtMessage: msgId:" + ftMsg.getId() + " isDeleted:" + isFileDeleted + " isThumbnailDeleted:" + isThumbnailDeleted);
        }
    }

    public void readMessagesforCloudSync(int phoneId, List<String> list) {
        this.mCmStoreInvoker.onReadRcsMessageList(phoneId, list);
    }

    public void deleteMessagesforCloudSyncUsingMsgId(List<String> list, boolean isLocalWipeout) {
        this.mCmStoreInvoker.onDeleteRcsMessagesUsingMsgId(list, isLocalWipeout);
    }

    public void deleteMessagesforCloudSyncUsingImdnId(Map<String, Integer> imdnIds, boolean isLocalWipeout) {
        List<String> list = new ArrayList<>();
        for (String add : imdnIds.keySet()) {
            list.add(add);
        }
        this.mCmStoreInvoker.onDeleteRcsMessagesUsingImdnId(list, isLocalWipeout);
    }

    public synchronized void deleteMessagesforCloudSyncUsingChatId(List<String> list, boolean isLocalWipeout) {
        this.mCmStoreInvoker.onDeleteRcsMessagesUsingChatId(list, isLocalWipeout);
    }

    public synchronized void deleteMessage(int id) {
        deleteMessage(getMessage(id));
    }

    public synchronized void deleteMessages(Map<String, Integer> imdnIds) {
        for (Map.Entry<String, Integer> imdnId : imdnIds.entrySet()) {
            deleteMessage(getMessage(imdnId.getKey(), ImDirection.fromId(imdnId.getValue().intValue())));
        }
    }

    private void deleteMessage(MessageBase msg) {
        if (msg != null) {
            if (msg instanceof FtMessage) {
                handleDeleteFtMessage((FtMessage) msg);
            }
            unregisterMessage(msg);
            this.mPersister.deleteMessage(msg.mId);
        }
    }

    public synchronized void deleteAllMessages(String chatId) {
        if (!TextUtils.isEmpty(chatId)) {
            List<FtMessage> ftmsgs = new ArrayList<>();
            List<Integer> ids = this.mPersister.queryAllMessageIdsByChatId(chatId, true);
            String str = LOG_TAG;
            Log.i(str, "deleteAllMessages ft message ids : " + ids);
            for (Integer intValue : ids) {
                FtMessage ftmsg = getFtMessage(intValue.intValue());
                if (ftmsg != null) {
                    ftmsgs.add(ftmsg);
                }
            }
            for (FtMessage msg : ftmsgs) {
                handleDeleteFtMessage(msg);
            }
            unregisterMessage(this.mPendingMessages.getAll(chatId));
        }
        this.mPersister.deleteMessage(chatId);
    }

    private void registerSession(ImSession session) {
        ChatData chatData = session.getChatData();
        registerObserver(chatData);
        chatData.triggerObservers(ImCacheAction.INSERTED);
        this.mImSessions.put(session.getChatId(), session);
    }

    private void unregisterSession(ImSession session) {
        ChatData chatData = session.getChatData();
        chatData.triggerObservers(ImCacheAction.DELETED);
        unregisterObserver(chatData);
        this.mImSessions.remove(session.getChatId());
    }

    private void registerMessage(MessageBase msg) {
        registerObserver(msg);
        msg.triggerObservers(ImCacheAction.INSERTED);
    }

    public void addToPendingList(MessageBase msg) {
        if (msg != null) {
            this.mPendingMessages.put(msg);
        } else {
            Log.w(LOG_TAG, "Message is null.");
        }
    }

    public void removeFromPendingList(int id) {
        MessageBase msg = this.mPendingMessages.get(id);
        if (msg != null) {
            unregisterObserver(msg);
            this.mPendingMessages.remove(id);
            String str = LOG_TAG;
            Log.i(str, "removed message from cache:" + id);
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Message is not in the cache:" + id);
    }

    private void unregisterMessage(MessageBase msg) {
        msg.triggerObservers(ImCacheAction.DELETED);
        unregisterObserver(msg);
        this.mPendingMessages.remove(msg.getId());
    }

    private void unregisterMessage(List<MessageBase> messages) {
        updateMessage((Collection<MessageBase>) messages, ImCacheAction.DELETED);
        for (MessageBase msg : messages) {
            unregisterObserver(msg);
            this.mPendingMessages.remove(msg.getId());
        }
    }

    private void registerParticipant(Collection<ImParticipant> participants) {
        for (ImParticipant p : participants) {
            registerObserver(p);
        }
        updateParticipant(participants, ImCacheAction.INSERTED);
    }

    private void unregisterParticipant(Collection<ImParticipant> participants) {
        updateParticipant(participants, ImCacheAction.DELETED);
        for (ImParticipant p : participants) {
            unregisterObserver(p);
        }
    }

    public void updateActiveSession(ImSession session) {
        this.mActiveSessions.put(Integer.valueOf(session.getId()), session);
    }

    public void removeActiveSession(ImSession session) {
        this.mActiveSessions.remove(Integer.valueOf(session.getId()));
    }

    public Collection<ImSession> getActiveSessions() {
        return this.mActiveSessions.snapshot().values();
    }

    public boolean isEstablishedSessionExist() {
        for (ImSession session : this.mActiveSessions.snapshot().values()) {
            if (session.isEstablishedState()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFileTransferInprogress() {
        for (MessageBase msg : new ArrayList<>(this.mPendingMessages.getAll())) {
            if ((msg instanceof FtMsrpMessage) && ((FtMessage) msg).getStateId() == 2) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:3:0x0014  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasProcessingFileTransfer() {
        /*
            r3 = this;
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r0 = r3.mImSessions
            java.util.Map r0 = r0.snapshot()
            java.util.Collection r0 = r0.values()
            java.util.Iterator r0 = r0.iterator()
        L_0x000e:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x002e
            java.lang.Object r1 = r0.next()
            com.sec.internal.ims.servicemodules.im.ImSession r1 = (com.sec.internal.ims.servicemodules.im.ImSession) r1
            java.util.ArrayList<com.sec.internal.ims.servicemodules.im.FtMessage> r2 = r1.mProcessingFileTransfer
            boolean r2 = r2.isEmpty()
            if (r2 == 0) goto L_0x002c
            java.util.ArrayList<com.sec.internal.ims.servicemodules.im.FtMessage> r2 = r1.mPendingFileTransfer
            boolean r2 = r2.isEmpty()
            if (r2 != 0) goto L_0x002b
            goto L_0x002c
        L_0x002b:
            goto L_0x000e
        L_0x002c:
            r0 = 1
            return r0
        L_0x002e:
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.hasProcessingFileTransfer():boolean");
    }

    public void updateDesiredNotificationStatusAsDisplay(Collection<String> messages) {
        Long displayedTimestamp = Long.valueOf(System.currentTimeMillis());
        for (String idStr : messages) {
            try {
                MessageBase msg = this.mPendingMessages.get(Integer.valueOf(idStr).intValue());
                if (msg != null) {
                    msg.setDesiredNotificationStatus(NotificationStatus.DISPLAYED);
                    msg.setDisplayedTimestamp(displayedTimestamp.longValue());
                    if (msg.getStatus() != ImConstants.Status.FAILED) {
                        msg.setStatus(ImConstants.Status.READ);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        this.mPersister.updateDesiredNotificationStatusAsDisplayed(messages, NotificationStatus.DISPLAYED.getId(), displayedTimestamp.longValue());
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public synchronized void addToChatbotRoleUris(ImsUri uri) {
        if (uri != null) {
            if (this.mIsLoaded) {
                String str = LOG_TAG;
                Log.i(str, "addToChatbotRoleUris: uri = " + IMSLog.checker(uri) + " " + IMSLog.checker(this.mChatbotRoleUris));
                if (!this.mIsChatbotRoleUrisLoaded) {
                    loadChatbotRoleUris();
                }
                this.mChatbotRoleUris.add(uri);
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public synchronized void removeFromChatbotRoleUris(ImsUri uri) {
        if (uri != null) {
            if (this.mIsLoaded) {
                String str = LOG_TAG;
                Log.i(str, "removeFromChatbotRoleUris: uri = " + IMSLog.checker(uri) + " " + IMSLog.checker(this.mChatbotRoleUris));
                if (!this.mIsChatbotRoleUrisLoaded) {
                    loadChatbotRoleUris();
                }
                this.mChatbotRoleUris.remove(uri);
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 1 */
    public synchronized boolean isChatbotRoleUri(ImsUri uri) {
        if (uri != null) {
            if (this.mIsLoaded) {
                if (!this.mIsChatbotRoleUrisLoaded) {
                    loadChatbotRoleUris();
                }
                return this.mChatbotRoleUris.contains(uri);
            }
        }
        return false;
    }

    private synchronized void loadChatbotRoleUris() {
        String str = LOG_TAG;
        Log.i(str, "loadChatbotRoleUris: mIsChatbotRoleUrisLoaded=" + this.mIsChatbotRoleUrisLoaded + "mIsLoaded=" + this.mIsLoaded);
        if (!this.mIsChatbotRoleUrisLoaded && this.mIsLoaded) {
            this.mChatbotRoleUris.clear();
            this.mChatbotRoleUris.addAll(this.mPersister.queryChatbotRoleUris());
            this.mIsChatbotRoleUrisLoaded = true;
        }
    }

    public void closeDB() {
        this.mPersister.closeDB();
    }

    public ImPersister getPersister() {
        return this.mPersister;
    }
}
