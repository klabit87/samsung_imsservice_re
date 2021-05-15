package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendMessageFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.listener.ImMessageListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ImProcessor extends Handler implements ImMessageListener {
    private static final String LOG_TAG = ImProcessor.class.getSimpleName();
    private ImCache mCache;
    private Context mContext;
    private ImModule mImModule;
    private ImSessionProcessor mImSessionProcessor;
    private ImTranslation mImTranslation;
    private final CollectionUtils.ArrayListMultimap<ImConstants.Type, IMessageEventListener> mMessageEventListeners = CollectionUtils.createArrayListMultimap();

    public ImProcessor(Context context, ImModule imModule, ImCache imCache) {
        this.mContext = context;
        this.mImModule = imModule;
        this.mCache = imCache;
    }

    /* access modifiers changed from: protected */
    public void init(ImSessionProcessor imSessionProcessor, ImTranslation imTranslation) {
        this.mImSessionProcessor = imSessionProcessor;
        this.mImTranslation = imTranslation;
    }

    /* access modifiers changed from: protected */
    public void registerMessageEventListener(ImConstants.Type type, IMessageEventListener listener) {
        this.mMessageEventListeners.put(type, listener);
    }

    public void onMessageSendResponse(ImMessage msg) {
        List<String> participants;
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session != null && ((participants = session.getParticipantsString()) == null || participants.isEmpty())) {
            Log.i(LOG_TAG, "onMessageSendResponse: no participants for this chat");
        }
        for (IMessageEventListener listener : this.mMessageEventListeners.get(msg.getType())) {
            listener.onMessageSendResponse(msg);
        }
    }

    public void onMessageReceived(ImMessage msg) {
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session != null) {
            for (IMessageEventListener listener : this.mMessageEventListeners.get(msg.getType())) {
                listener.onMessageReceived(msg, session);
            }
        }
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
        this.mImSessionProcessor.onMessageSendingSucceeded(msg);
    }

    public void onMessageSendResponseTimeout(ImMessage msg) {
        for (IMessageEventListener listener : this.mMessageEventListeners.get(msg.getType())) {
            listener.onMessageSendResponseTimeout(msg);
        }
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        this.mImSessionProcessor.onMessageSendingFailed(msg, strategyResponse, result);
    }

    /* access modifiers changed from: protected */
    public void sendMessage(ImSession session, MessageBase message) {
        String str = LOG_TAG;
        Log.i(str, "sendMessage: message id = " + message.getId());
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getOwnImsi());
        if (this.mImModule.isRegistered(phoneId)) {
            List<String> dumps = new ArrayList<>();
            dumps.add(ImsUtil.hideInfo(session.getConversationId(), 4));
            dumps.add(ImsUtil.hideInfo(message.getImdnId(), 4));
            dumps.add(ImsUtil.hideInfo(session.getRequestMessageId(), 4));
            dumps.add(" 0");
            ImsUtil.listToDumpFormat(LogClass.IM_SEND_IM, phoneId, message.getChatId(), dumps);
            session.setDirection(ImDirection.OUTGOING);
            session.sendImMessage(message);
        } else if (RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.PENDING_FOR_REGI)) {
            message.updateStatus(ImConstants.Status.TO_SEND);
        } else {
            message.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
        }
        this.mCache.updateActiveSession(session);
    }

    /* access modifiers changed from: protected */
    public Future<ImMessage> sendMessage(String cid, String body, Set<NotificationStatus> disposition, String contentType, String requestMessageId, int messageNumber, boolean isBroadcastMsg, boolean isprotectedAccountMsg, boolean isGLSMsg, String deviceName, String reliableMessage, String xmsMessage, List<ImsUri> ccList, boolean isTemporary, String maapTrafficType, String referenceMessageId, String referenceMessageType, String referenceMessageValue) {
        String str = maapTrafficType;
        String str2 = referenceMessageId;
        String str3 = referenceMessageType;
        $$Lambda$ImProcessor$p8dy2p3rNb6huOjjY4lVvVVrXcM r22 = r0;
        $$Lambda$ImProcessor$p8dy2p3rNb6huOjjY4lVvVVrXcM r0 = new Callable(this, cid, body, disposition, contentType, requestMessageId, isBroadcastMsg, isprotectedAccountMsg, isGLSMsg, deviceName, reliableMessage, xmsMessage, str, str2, str3, messageNumber, isTemporary, referenceMessageValue, ccList) {
            public final /* synthetic */ ImProcessor f$0;
            public final /* synthetic */ String f$1;
            public final /* synthetic */ String f$10;
            public final /* synthetic */ String f$11;
            public final /* synthetic */ String f$12;
            public final /* synthetic */ String f$13;
            public final /* synthetic */ String f$14;
            public final /* synthetic */ int f$15;
            public final /* synthetic */ boolean f$16;
            public final /* synthetic */ String f$17;
            public final /* synthetic */ List f$18;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ Set f$3;
            public final /* synthetic */ String f$4;
            public final /* synthetic */ String f$5;
            public final /* synthetic */ boolean f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ String f$9;

            {
                this.f$0 = r3;
                this.f$1 = r4;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
                this.f$5 = r8;
                this.f$6 = r9;
                this.f$7 = r10;
                this.f$8 = r11;
                this.f$9 = r12;
                this.f$10 = r13;
                this.f$11 = r14;
                this.f$12 = r15;
                this.f$13 = r16;
                this.f$14 = r17;
                this.f$15 = r18;
                this.f$16 = r19;
                this.f$17 = r20;
                this.f$18 = r21;
            }

            public final Object call() {
                return this.f$0.lambda$sendMessage$0$ImProcessor(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16, this.f$17, this.f$18);
            }
        };
        FutureTask<ImMessage> future = new FutureTask<>(r22);
        post(future);
        return future;
    }

    /* JADX WARNING: Removed duplicated region for block: B:119:0x02b6  */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x02e3  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x018c A[SYNTHETIC, Splitter:B:54:0x018c] */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x01d1  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01d6  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01ec  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x021a  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0238  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ com.sec.internal.ims.servicemodules.im.ImMessage lambda$sendMessage$0$ImProcessor(java.lang.String r28, java.lang.String r29, java.util.Set r30, java.lang.String r31, java.lang.String r32, boolean r33, boolean r34, boolean r35, java.lang.String r36, java.lang.String r37, java.lang.String r38, java.lang.String r39, java.lang.String r40, java.lang.String r41, int r42, boolean r43, java.lang.String r44, java.util.List r45) throws java.lang.Exception {
        /*
            r27 = this;
            r1 = r27
            r2 = r28
            r15 = r32
            r14 = r33
            r13 = r42
            r12 = r45
            r22 = 0
            r3 = 0
            r23 = 0
            java.lang.String r0 = LOG_TAG     // Catch:{ Exception -> 0x0294 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0294 }
            r4.<init>()     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = "sendMessage: chatId="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r4.append(r2)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", body="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r29)     // Catch:{ Exception -> 0x0294 }
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", disposition="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r9 = r30
            r4.append(r9)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", contentType="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r8 = r31
            r4.append(r8)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", requestMessageId="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r4.append(r15)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", isBroadcastMsg="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r4.append(r14)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", isprotectedAccountMsg="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r7 = r34
            r4.append(r7)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", isGLSMsg="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r6 = r35
            r4.append(r6)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r5 = ", deviceName="
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            r5 = r36
            r4.append(r5)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r11 = ", reliableMessage="
            r4.append(r11)     // Catch:{ Exception -> 0x0294 }
            r11 = r37
            r4.append(r11)     // Catch:{ Exception -> 0x0294 }
            java.lang.String r10 = ", xmsMessage="
            r4.append(r10)     // Catch:{ Exception -> 0x0294 }
            r10 = r38
            r4.append(r10)     // Catch:{ Exception -> 0x0294 }
            r18 = r3
            java.lang.String r3 = ", maapTrafficType="
            r4.append(r3)     // Catch:{ Exception -> 0x028a }
            r3 = r39
            r4.append(r3)     // Catch:{ Exception -> 0x028a }
            java.lang.String r3 = ", referenceMessageId="
            r4.append(r3)     // Catch:{ Exception -> 0x028a }
            r3 = r40
            r4.append(r3)     // Catch:{ Exception -> 0x028a }
            java.lang.String r3 = ", referenceMessageType="
            r4.append(r3)     // Catch:{ Exception -> 0x028a }
            r3 = r41
            r4.append(r3)     // Catch:{ Exception -> 0x028a }
            java.lang.String r4 = r4.toString()     // Catch:{ Exception -> 0x028a }
            android.util.Log.i(r0, r4)     // Catch:{ Exception -> 0x028a }
            com.sec.internal.ims.servicemodules.im.ImCache r0 = r1.mCache     // Catch:{ Exception -> 0x028a }
            com.sec.internal.ims.servicemodules.im.ImSession r0 = r0.getImSession(r2)     // Catch:{ Exception -> 0x028a }
            r4 = r0
            if (r4 != 0) goto L_0x00ef
            java.lang.String r0 = LOG_TAG     // Catch:{ Exception -> 0x00e5 }
            java.lang.String r3 = "sendMessage: Session not found in the cache."
            android.util.Log.e(r0, r3)     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.helper.CollectionUtils$ArrayListMultimap<com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type, com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener> r0 = r1.mMessageEventListeners     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r3 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.TEXT     // Catch:{ Exception -> 0x00e5 }
            java.util.Collection r0 = r0.get(r3)     // Catch:{ Exception -> 0x00e5 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ Exception -> 0x00e5 }
        L_0x00c7:
            boolean r3 = r0.hasNext()     // Catch:{ Exception -> 0x00e5 }
            if (r3 == 0) goto L_0x00e4
            java.lang.Object r3 = r0.next()     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener r3 = (com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener) r3     // Catch:{ Exception -> 0x00e5 }
            r10 = 4
            r3.onMessageSendResponseFailed(r2, r13, r10, r15)     // Catch:{ Exception -> 0x00da }
            r10 = r38
            goto L_0x00c7
        L_0x00da:
            r0 = move-exception
            r3 = r4
            r7 = r10
            r6 = r12
            r5 = r15
            r4 = r0
            r0 = r22
            goto L_0x029d
        L_0x00e4:
            return r23
        L_0x00e5:
            r0 = move-exception
            r3 = r4
            r6 = r12
            r5 = r15
            r7 = 4
            r4 = r0
            r0 = r22
            goto L_0x029d
        L_0x00ef:
            r10 = 4
            com.sec.internal.ims.servicemodules.im.ImModule r0 = r1.mImModule     // Catch:{ Exception -> 0x0280 }
            java.lang.String r3 = r4.getOwnImsi()     // Catch:{ Exception -> 0x0280 }
            int r0 = r0.getPhoneIdByIMSI(r3)     // Catch:{ Exception -> 0x0280 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r3 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r0)     // Catch:{ Exception -> 0x0280 }
            com.sec.internal.ims.servicemodules.im.ImModule r10 = r1.mImModule     // Catch:{ Exception -> 0x027a }
            boolean r10 = r10.isRegistered(r0)     // Catch:{ Exception -> 0x027a }
            if (r10 == 0) goto L_0x0141
            com.sec.internal.ims.servicemodules.im.ImModule r10 = r1.mImModule     // Catch:{ Exception -> 0x00e5 }
            r18 = r3
            java.lang.String r3 = "slm"
            boolean r3 = r10.isServiceRegistered(r0, r3)     // Catch:{ Exception -> 0x00e5 }
            if (r3 == 0) goto L_0x0143
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.ims.servicemodules.im.ImConfig r3 = r3.getImConfig(r0)     // Catch:{ Exception -> 0x00e5 }
            boolean r3 = r3.getChatEnabled()     // Catch:{ Exception -> 0x00e5 }
            if (r3 == 0) goto L_0x0121
            if (r14 == 0) goto L_0x0143
        L_0x0121:
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.ims.servicemodules.im.ImConfig r3 = r3.getImConfig(r0)     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$SlmAuth r3 = r3.getSlmAuth()     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$SlmAuth r10 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.SlmAuth.ENABLED     // Catch:{ Exception -> 0x00e5 }
            if (r3 != r10) goto L_0x0143
            boolean r3 = r4.isGroupChat()     // Catch:{ Exception -> 0x00e5 }
            if (r3 == 0) goto L_0x013f
            if (r14 != 0) goto L_0x013f
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = r4.getChatType()     // Catch:{ Exception -> 0x00e5 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r10 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_MANY_CHAT     // Catch:{ Exception -> 0x00e5 }
            if (r3 != r10) goto L_0x0143
        L_0x013f:
            r3 = 1
            goto L_0x0144
        L_0x0141:
            r18 = r3
        L_0x0143:
            r3 = 0
        L_0x0144:
            r24 = r3
            com.sec.internal.ims.servicemodules.im.ImCache r3 = r1.mCache     // Catch:{ Exception -> 0x027a }
            java.lang.String r10 = r4.getOwnImsi()     // Catch:{ Exception -> 0x027a }
            r25 = r18
            r26 = r4
            r4 = r10
            r5 = r26
            r6 = r29
            r7 = r30
            r8 = r31
            r9 = r32
            r2 = 4
            r10 = r24
            r11 = r34
            r12 = r33
            r13 = r35
            r14 = r36
            r15 = r37
            r16 = r38
            r17 = r43
            r18 = r39
            r19 = r40
            r20 = r41
            r21 = r44
            com.sec.internal.ims.servicemodules.im.ImMessage r3 = r3.makeNewOutgoingMessage(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21)     // Catch:{ Exception -> 0x026b }
            com.sec.internal.helper.CollectionUtils$ArrayListMultimap<com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type, com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener> r4 = r1.mMessageEventListeners     // Catch:{ Exception -> 0x025d }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r5 = r3.getType()     // Catch:{ Exception -> 0x025d }
            java.util.Collection r4 = r4.get(r5)     // Catch:{ Exception -> 0x025d }
            java.util.Iterator r4 = r4.iterator()     // Catch:{ Exception -> 0x025d }
        L_0x0186:
            boolean r5 = r4.hasNext()     // Catch:{ Exception -> 0x025d }
            if (r5 == 0) goto L_0x01a4
            java.lang.Object r5 = r4.next()     // Catch:{ Exception -> 0x0196 }
            com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener r5 = (com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener) r5     // Catch:{ Exception -> 0x0196 }
            r5.onMessageSendResponse(r3)     // Catch:{ Exception -> 0x0196 }
            goto L_0x0186
        L_0x0196:
            r0 = move-exception
            r5 = r32
        L_0x0199:
            r6 = r45
            r4 = r0
            r7 = r2
            r0 = r3
            r3 = r26
            r2 = r28
            goto L_0x029d
        L_0x01a4:
            java.util.ArrayList r4 = new java.util.ArrayList     // Catch:{ Exception -> 0x025d }
            r4.<init>()     // Catch:{ Exception -> 0x025d }
            java.lang.String r5 = r26.getConversationId()     // Catch:{ Exception -> 0x025d }
            java.lang.String r5 = com.sec.internal.ims.util.ImsUtil.hideInfo(r5, r2)     // Catch:{ Exception -> 0x025d }
            r4.add(r5)     // Catch:{ Exception -> 0x025d }
            java.lang.String r5 = r3.getImdnId()     // Catch:{ Exception -> 0x025d }
            java.lang.String r5 = com.sec.internal.ims.util.ImsUtil.hideInfo(r5, r2)     // Catch:{ Exception -> 0x025d }
            r4.add(r5)     // Catch:{ Exception -> 0x025d }
            r5 = r32
            r4.add(r5)     // Catch:{ Exception -> 0x0250 }
            java.lang.String r6 = com.sec.internal.log.IMSLog.checker(r29)     // Catch:{ Exception -> 0x0250 }
            java.lang.String r6 = com.sec.internal.ims.util.ImsUtil.hideInfo(r6, r2)     // Catch:{ Exception -> 0x0250 }
            r4.add(r6)     // Catch:{ Exception -> 0x0250 }
            if (r24 == 0) goto L_0x01d6
            java.lang.String r6 = "1"
            goto L_0x01d8
        L_0x01d4:
            r0 = move-exception
            goto L_0x0199
        L_0x01d6:
            java.lang.String r6 = " 0"
        L_0x01d8:
            r4.add(r6)     // Catch:{ Exception -> 0x0250 }
            r6 = 1073741825(0x40000001, float:2.0000002)
            r7 = r2
            r2 = r28
            com.sec.internal.ims.util.ImsUtil.listToDumpFormat(r6, r0, r2, r4)     // Catch:{ Exception -> 0x0246 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r6 = r26.getChatType()     // Catch:{ Exception -> 0x0246 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r8 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT     // Catch:{ Exception -> 0x0246 }
            if (r6 != r8) goto L_0x021a
            java.lang.String r6 = "allow_only_opengroupchat"
            r8 = r25
            boolean r6 = r8.boolSetting(r6)     // Catch:{ Exception -> 0x0211 }
            if (r6 == 0) goto L_0x021c
            java.lang.String r6 = LOG_TAG     // Catch:{ Exception -> 0x0211 }
            java.lang.String r9 = "Only OpenGroupChat is allowed, fallback to legacy(MMS)"
            android.util.Log.i(r6, r9)     // Catch:{ Exception -> 0x0211 }
            com.sec.internal.constants.ims.servicemodules.im.result.Result r6 = new com.sec.internal.constants.ims.servicemodules.im.result.Result     // Catch:{ Exception -> 0x0211 }
            com.sec.internal.constants.ims.servicemodules.im.ImError r9 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_TEMPORARILY_UNAVAILABLE     // Catch:{ Exception -> 0x0211 }
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r10 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.NONE     // Catch:{ Exception -> 0x0211 }
            r6.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r9, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r10)     // Catch:{ Exception -> 0x0211 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r9 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse     // Catch:{ Exception -> 0x0211 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r10 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY     // Catch:{ Exception -> 0x0211 }
            r9.<init>(r10)     // Catch:{ Exception -> 0x0211 }
            r3.onSendMessageDone(r6, r9)     // Catch:{ Exception -> 0x0211 }
            return r3
        L_0x0211:
            r0 = move-exception
            r6 = r45
        L_0x0214:
            r4 = r0
            r0 = r3
            r3 = r26
            goto L_0x029d
        L_0x021a:
            r8 = r25
        L_0x021c:
            boolean r6 = r26.isGroupChat()     // Catch:{ Exception -> 0x0246 }
            if (r6 == 0) goto L_0x0238
            r6 = r45
            if (r6 == 0) goto L_0x023a
            boolean r9 = r45.isEmpty()     // Catch:{ Exception -> 0x0236 }
            if (r9 != 0) goto L_0x023a
            com.sec.internal.ims.servicemodules.im.ImModule r9 = r1.mImModule     // Catch:{ Exception -> 0x0236 }
            java.util.Set r9 = r9.normalizeUri((int) r0, (java.util.Collection<com.sec.ims.util.ImsUri>) r6)     // Catch:{ Exception -> 0x0236 }
            r3.setGroupCcListUri(r9)     // Catch:{ Exception -> 0x0236 }
            goto L_0x023a
        L_0x0236:
            r0 = move-exception
            goto L_0x0214
        L_0x0238:
            r6 = r45
        L_0x023a:
            r9 = r26
            r1.sendMessage(r9, r3)     // Catch:{ Exception -> 0x0240 }
            return r3
        L_0x0240:
            r0 = move-exception
            r4 = r0
            r0 = r3
            r3 = r9
            goto L_0x029d
        L_0x0246:
            r0 = move-exception
            r6 = r45
            r9 = r26
            r4 = r0
            r0 = r3
            r3 = r9
            goto L_0x029d
        L_0x0250:
            r0 = move-exception
            r6 = r45
            r7 = r2
            r9 = r26
            r2 = r28
            r4 = r0
            r0 = r3
            r3 = r9
            goto L_0x029d
        L_0x025d:
            r0 = move-exception
            r5 = r32
            r6 = r45
            r7 = r2
            r9 = r26
            r2 = r28
            r4 = r0
            r0 = r3
            r3 = r9
            goto L_0x029d
        L_0x026b:
            r0 = move-exception
            r5 = r32
            r6 = r45
            r7 = r2
            r9 = r26
            r2 = r28
            r4 = r0
            r3 = r9
            r0 = r22
            goto L_0x029d
        L_0x027a:
            r0 = move-exception
            r9 = r4
            r6 = r12
            r5 = r15
            r7 = 4
            goto L_0x0285
        L_0x0280:
            r0 = move-exception
            r9 = r4
            r7 = r10
            r6 = r12
            r5 = r15
        L_0x0285:
            r4 = r0
            r3 = r9
            r0 = r22
            goto L_0x029d
        L_0x028a:
            r0 = move-exception
            r6 = r12
            r5 = r15
            r7 = 4
            r4 = r0
            r3 = r18
            r0 = r22
            goto L_0x029d
        L_0x0294:
            r0 = move-exception
            r18 = r3
            r6 = r12
            r5 = r15
            r7 = 4
            r4 = r0
            r0 = r22
        L_0x029d:
            java.lang.String r8 = LOG_TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "sendMessage Exception e = "
            r9.append(r10)
            r9.append(r4)
            java.lang.String r9 = r9.toString()
            android.util.Log.e(r8, r9)
            if (r0 != 0) goto L_0x02e3
            if (r3 == 0) goto L_0x02e0
            int r8 = r3.getParticipantsSize()
            r9 = 1
            if (r8 >= r9) goto L_0x02e0
            com.sec.internal.helper.CollectionUtils$ArrayListMultimap<com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type, com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener> r8 = r1.mMessageEventListeners
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r9 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.TEXT
            java.util.Collection r8 = r8.get(r9)
            java.util.Iterator r8 = r8.iterator()
        L_0x02cb:
            boolean r9 = r8.hasNext()
            if (r9 == 0) goto L_0x02dd
            java.lang.Object r9 = r8.next()
            com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener r9 = (com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener) r9
            r10 = r42
            r9.onMessageSendResponseFailed(r2, r10, r7, r5)
            goto L_0x02cb
        L_0x02dd:
            r10 = r42
            goto L_0x02e2
        L_0x02e0:
            r10 = r42
        L_0x02e2:
            return r23
        L_0x02e3:
            r10 = r42
            java.lang.String r7 = LOG_TAG
            java.lang.String r8 = "sendMessage Failed."
            android.util.Log.e(r7, r8)
            boolean r7 = r3.isGroupChat()
            if (r7 != 0) goto L_0x0306
            com.sec.internal.constants.ims.servicemodules.im.result.Result r7 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r8 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_TEMPORARILY_UNAVAILABLE
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r9 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.NONE
            r7.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r8, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r9)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r8 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r9 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r8.<init>(r9)
            r0.onSendMessageDone(r7, r8)
        L_0x0306:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImProcessor.lambda$sendMessage$0$ImProcessor(java.lang.String, java.lang.String, java.util.Set, java.lang.String, java.lang.String, boolean, boolean, boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, boolean, java.lang.String, java.util.List):com.sec.internal.ims.servicemodules.im.ImMessage");
    }

    /* access modifiers changed from: protected */
    public void resendMessage(int msgId) {
        post(new Runnable(msgId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImProcessor.this.lambda$resendMessage$1$ImProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$resendMessage$1$ImProcessor(int msgId) {
        ImMessage message = this.mCache.getImMessage(msgId);
        if (message == null) {
            Log.e(LOG_TAG, "resendMessage: message not found in the cache.");
            return;
        }
        ImSession session = this.mCache.getImSession(message.getChatId());
        if (session == null) {
            for (IMessageEventListener listener : this.mMessageEventListeners.get(message.getType())) {
                listener.onMessageSendResponse(message);
            }
        } else if (message.getStatus() == ImConstants.Status.FAILED) {
            sendMessage(session, message);
        }
    }

    /* access modifiers changed from: protected */
    public void reportMessages(ArrayList<String> list) {
        post(new Runnable(list) {
            public final /* synthetic */ ArrayList f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImProcessor.this.lambda$reportMessages$2$ImProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$reportMessages$2$ImProcessor(ArrayList list) {
        String imsi;
        ImsUri spamTo;
        ImsUri spamTo2;
        Set<ImsUri> normalizedReportPSI;
        ImSession session;
        ImsUri spamFrom;
        ImsUri reportPSI;
        ImSession session2;
        if (this.mImModule.isRegistered()) {
            String str = LOG_TAG;
            Log.i(str, "reportMessages: list=" + list);
            ImsUri reportPSI2 = ImsUri.parse(this.mImModule.getRcsStrategy().stringSetting(RcsPolicySettings.RcsPolicy.ONEKEY_REPORT_PSI));
            if (reportPSI2 == null) {
                Log.e(LOG_TAG, "reportMessages: reportPSI is null");
                return;
            }
            Set<ImsUri> hashSet = new HashSet<>();
            hashSet.add(this.mImModule.normalizeUri(reportPSI2));
            Iterator it = list.iterator();
            while (it.hasNext()) {
                int id = Integer.valueOf((String) it.next()).intValue();
                MessageBase msg = this.mCache.getMessage(id);
                if (msg != null) {
                    Date spamTime = new Date(msg.getSentTimestamp());
                    ImsUri spamFrom2 = msg.getRemoteUri();
                    ImsUri spamTo3 = ImsUri.parse("tel:+" + this.mImModule.getOwnPhoneNum());
                    ImSession chatSession = this.mCache.getImSession(msg.getChatId());
                    if (chatSession == null || chatSession.getOwnImsi() == null) {
                        imsi = "";
                        spamTo = spamTo3;
                    } else {
                        String imsi2 = chatSession.getOwnImsi();
                        StringBuilder sb = new StringBuilder();
                        sb.append("tel:+");
                        ImModule imModule = this.mImModule;
                        sb.append(imModule.getOwnPhoneNum(imModule.getPhoneIdByIMSI(imsi2)));
                        imsi = imsi2;
                        spamTo = ImsUri.parse(sb.toString());
                    }
                    if (spamFrom2 == null) {
                        Set<ImsUri> set = hashSet;
                        ImsUri imsUri = spamTo;
                        String str2 = imsi;
                        ImsUri imsUri2 = spamFrom2;
                        MessageBase messageBase = msg;
                        int i = id;
                        return;
                    } else if (spamTo != null) {
                        ImSession session3 = this.mCache.getImSessionByParticipants(hashSet, ChatData.ChatType.ONE_TO_ONE_CHAT, imsi);
                        if (session3 == null) {
                            Set<ImsUri> set2 = hashSet;
                            spamTo2 = spamTo;
                            String str3 = imsi;
                            normalizedReportPSI = hashSet;
                            spamFrom = spamFrom2;
                            session = this.mCache.makeNewOutgoingSession(imsi, set2, ChatData.ChatType.ONE_TO_ONE_CHAT, (String) null, (String) null, 0, "0", (String) null, ChatMode.OFF);
                        } else {
                            normalizedReportPSI = hashSet;
                            spamTo2 = spamTo;
                            String str4 = imsi;
                            spamFrom = spamFrom2;
                            session = session3;
                        }
                        if (msg instanceof ImMessage) {
                            ImMessage imMsg = (ImMessage) msg;
                            ImMessage message = this.mCache.makeNewOutgoingMessage(session.getOwnImsi(), session, imMsg.getBody(), NotificationStatus.toSet("display_delivery"), imMsg.getContentType(), "0", false, false, false, false, (String) null, (String) null, (String) null, false, imMsg.getMaapTrafficType());
                            ImsUri spamTo4 = spamTo2;
                            message.setSpamInfo(spamFrom, spamTo4, spamTime.toString(), id);
                            session.setDirection(ImDirection.OUTGOING);
                            session.sendImMessage(message);
                            reportPSI = reportPSI2;
                            ImsUri imsUri3 = spamTo4;
                            session2 = session;
                            MessageBase messageBase2 = msg;
                            int i2 = id;
                        } else {
                            MessageBase messageBase3 = msg;
                            reportPSI = reportPSI2;
                            FtMessage message2 = this.mCache.makeNewOutgoingFtMessage(session.getOwnImsi(), session, ((FtMessage) msg).getFilePath(), reportPSI2, NotificationStatus.toSet("display_delivery"), "1", (String) null, false, false, false, (String) null);
                            ImsUri spamTo5 = spamTo2;
                            message2.setSpamInfo(spamFrom, spamTo5, spamTime.toString(), id);
                            session2 = session;
                            session2.attachFile(message2);
                        }
                        this.mCache.updateActiveSession(session2);
                        hashSet = normalizedReportPSI;
                        reportPSI2 = reportPSI;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            Set<ImsUri> set3 = hashSet;
            return;
        }
        ArrayList arrayList = list;
        Log.e(LOG_TAG, "reportMessages: not registered");
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteMessages(List<String> list, boolean isLocalWipeout) {
        FutureTask<Boolean> future = new FutureTask<>(new Callable(list, isLocalWipeout) {
            public final /* synthetic */ List f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final Object call() {
                return ImProcessor.this.lambda$deleteMessages$3$ImProcessor(this.f$1, this.f$2);
            }
        });
        post(future);
        return future;
    }

    public /* synthetic */ Boolean lambda$deleteMessages$3$ImProcessor(List list, boolean isLocalWipeout) throws Exception {
        String str = LOG_TAG;
        Log.i(str, "deleteMessage: list=" + list + " localWipeout: " + isLocalWipeout);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String idStr = (String) it.next();
            if (idStr != null) {
                this.mCache.deleteMessage(Integer.valueOf(idStr).intValue());
            }
        }
        this.mCache.deleteMessagesforCloudSyncUsingMsgId(list, isLocalWipeout);
        return true;
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteMessagesByImdnId(Map<String, Integer> imdnIds, boolean isLocalWipeout) {
        FutureTask<Boolean> future = new FutureTask<>(new Callable(imdnIds, isLocalWipeout) {
            public final /* synthetic */ Map f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final Object call() {
                return ImProcessor.this.lambda$deleteMessagesByImdnId$4$ImProcessor(this.f$1, this.f$2);
            }
        });
        post(future);
        return future;
    }

    public /* synthetic */ Boolean lambda$deleteMessagesByImdnId$4$ImProcessor(Map imdnIds, boolean isLocalWipeout) throws Exception {
        String str = LOG_TAG;
        Log.i(str, "deleteMessage: imdnIds=" + imdnIds + " localWipeout: " + isLocalWipeout);
        this.mCache.deleteMessages(imdnIds);
        this.mCache.deleteMessagesforCloudSyncUsingImdnId(imdnIds, isLocalWipeout);
        return true;
    }

    /* access modifiers changed from: protected */
    public void deleteAllMessages(List<String> listChatId, boolean isLocalWipeout) {
        post(new Runnable(listChatId, isLocalWipeout) {
            public final /* synthetic */ List f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ImProcessor.this.lambda$deleteAllMessages$5$ImProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$deleteAllMessages$5$ImProcessor(List listChatId, boolean isLocalWipeout) {
        String str = LOG_TAG;
        Log.i(str, "deleteAllMessages: list=" + listChatId);
        Iterator it = listChatId.iterator();
        while (it.hasNext()) {
            this.mCache.deleteAllMessages((String) it.next());
        }
        this.mCache.deleteMessagesforCloudSyncUsingChatId(listChatId, isLocalWipeout);
    }

    /* access modifiers changed from: protected */
    public void onSendMessageHandleReportFailed(SendMessageFailedEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onSendMessageHandleReportFailed: " + event);
        ImSession session = this.mCache.getImSession(event.mChatId);
        if (session != null) {
            MessageBase msg = this.mCache.getMessage(event.mImdnId, ImDirection.OUTGOING, event.mChatId, session.getOwnImsi());
            if (msg != null) {
                this.mImModule.mNeedToRemoveFromPendingList.remove(Integer.valueOf(msg.getId()));
                session.onSendMessageHandleReportFailed(event, msg);
                return;
            }
            Log.e(LOG_TAG, "onSendMessageHandleReportFailed: Message not found.");
            return;
        }
        Log.e(LOG_TAG, "onSendMessageHandleReportFailed: Session not found.");
    }

    /* access modifiers changed from: protected */
    public void getLastSentMessagesStatus(List<String> list) {
        post(new Runnable(list) {
            public final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImProcessor.this.lambda$getLastSentMessagesStatus$6$ImProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$getLastSentMessagesStatus$6$ImProcessor(List list) {
        List<Bundle> ret = this.mCache.loadLastSentMessages(list);
        String str = LOG_TAG;
        Log.i(str, "getLastSentMessagesStatus " + ret.size() + " messages(s)");
        if (ret.isEmpty()) {
            this.mImTranslation.notifyLastSentMessagesStatus((List<Bundle>) null);
        } else {
            this.mImTranslation.notifyLastSentMessagesStatus(ret);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00f1  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01c6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onIncomingMessageReceived(com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent r14) {
        /*
            r13 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onIncomingMessageReceived: "
            r1.append(r2)
            r1.append(r14)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.servicemodules.im.ImCache r0 = r13.mCache
            java.lang.String r1 = r14.mChatId
            com.sec.internal.ims.servicemodules.im.ImSession r0 = r0.getImSession(r1)
            if (r0 != 0) goto L_0x0029
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "session not found"
            android.util.Log.e(r1, r2)
            return
        L_0x0029:
            com.sec.internal.ims.servicemodules.im.ImModule r1 = r13.mImModule
            com.sec.internal.constants.ims.servicemodules.im.ChatData r2 = r0.getChatData()
            java.lang.String r2 = r2.getOwnIMSI()
            int r1 = r1.getPhoneIdByIMSI(r2)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r13.mImModule
            com.sec.internal.ims.servicemodules.im.ImDump r2 = r2.getImDump()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "onIncomingMessageReceived: conversationId="
            r3.append(r4)
            java.lang.String r4 = r0.getConversationId()
            r3.append(r4)
            java.lang.String r4 = ", imdnId="
            r3.append(r4)
            java.lang.String r4 = r14.mImdnMessageId
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            r2.addEventLogs(r3)
            boolean r2 = r13.isDuplicateMessage(r1, r0, r14)
            if (r2 == 0) goto L_0x0066
            return
        L_0x0066:
            com.sec.internal.constants.ims.servicemodules.im.ChatData r2 = r0.getChatData()
            boolean r2 = r2.isMuted()
            if (r2 == 0) goto L_0x0078
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "onIncomingMessageReceived, user reject GC text."
            android.util.Log.i(r2, r3)
            return
        L_0x0078:
            java.lang.String r2 = r0.getDeviceId()
            r14.mDeviceId = r2
            r13.updateMessageSenderAlias(r1, r0, r14)
            boolean r2 = r0.isGroupChat()
            r10 = 0
            if (r2 != 0) goto L_0x00a5
            boolean r2 = r0.isChatbotRole()
            if (r2 == 0) goto L_0x0094
            com.sec.ims.util.ImsUri r2 = r14.mSender
            com.sec.internal.ims.util.ChatbotUriUtil.removeUriParameters(r2)
            goto L_0x00a5
        L_0x0094:
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r2 = r13.mImSessionProcessor
            com.sec.ims.util.ImsUri r3 = r0.getRemoteUri()
            com.sec.internal.constants.ims.servicemodules.im.ChatData r4 = r0.getChatData()
            java.lang.String r4 = r4.getOwnIMSI()
            r2.setLegacyLatching(r3, r10, r4)
        L_0x00a5:
            r2 = 0
            java.lang.String r3 = r14.mBody
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 != 0) goto L_0x00e2
            java.lang.String r3 = r14.mContentType
            boolean r3 = com.sec.internal.ims.servicemodules.im.ImMultipart.isMultipart(r3)
            if (r3 == 0) goto L_0x00e2
            com.sec.internal.ims.servicemodules.im.ImMultipart r3 = new com.sec.internal.ims.servicemodules.im.ImMultipart
            java.lang.String r4 = r14.mBody
            java.lang.String r5 = r14.mContentType
            r3.<init>(r4, r5)
            java.lang.String r4 = r3.getSuggestion()
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x00e2
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "onIncomingMessageReceived: message includes suggestion"
            android.util.Log.i(r4, r5)
            java.lang.String r4 = r3.getBody()
            r14.mBody = r4
            java.lang.String r4 = r3.getContentType()
            r14.mContentType = r4
            java.lang.String r2 = r3.getSuggestion()
            r12 = r2
            goto L_0x00e3
        L_0x00e2:
            r12 = r2
        L_0x00e3:
            java.lang.String r2 = r14.mContentType
            if (r2 == 0) goto L_0x010e
            java.lang.String r2 = r14.mContentType
            java.lang.String r3 = "application/vnd.gsma.rcs-ft-http+xml"
            boolean r2 = r2.startsWith(r3)
            if (r2 == 0) goto L_0x010e
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r13.mCache
            java.lang.String r3 = r0.getOwnImsi()
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r13.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r5 = r4.getImConfig(r1)
            boolean r5 = r5.isFtHttpOverDefaultPdn()
            android.net.Network r6 = r4.getNetwork(r5, r1)
            r4 = r0
            r5 = r14
            r7 = r12
            com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = r2.makeNewIncomingFtHttpMessage((java.lang.String) r3, (com.sec.internal.ims.servicemodules.im.ImSession) r4, (com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent) r5, (android.net.Network) r6, (java.lang.String) r7)
            goto L_0x0180
        L_0x010e:
            boolean r2 = r14.mIsRoutingMsg
            if (r2 == 0) goto L_0x0137
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            com.sec.ims.util.ImsUri r4 = r14.mRequestUri
            com.sec.ims.util.ImsUri r5 = r14.mPAssertedId
            com.sec.ims.util.ImsUri r6 = r14.mSender
            com.sec.ims.util.ImsUri r7 = r14.mReceiver
            boolean r8 = r0.isGroupChat()
            r9 = r1
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r2 = r3.getMsgRoutingType(r4, r5, r6, r7, r8, r9)
            r14.mRoutingType = r2
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r2 = r14.mRoutingType
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r3 = com.sec.internal.constants.ims.servicemodules.im.RoutingType.SENT
            if (r2 != r3) goto L_0x0137
            boolean r2 = r0.isGroupChat()
            if (r2 != 0) goto L_0x0137
            com.sec.ims.util.ImsUri r2 = r14.mReceiver
            r14.mSender = r2
        L_0x0137:
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r13.mCache
            java.lang.String r3 = r0.getOwnImsi()
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r13.mImModule
            android.net.Network r6 = r4.getNetwork(r10, r1)
            r4 = r0
            r5 = r14
            r7 = r12
            com.sec.internal.ims.servicemodules.im.ImMessage r2 = r2.makeNewIncomingMessage((java.lang.String) r3, (com.sec.internal.ims.servicemodules.im.ImSession) r4, (com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent) r5, (android.net.Network) r6, (java.lang.String) r7)
            boolean r3 = r0.isGroupChat()
            if (r3 == 0) goto L_0x0180
            java.util.List<com.sec.ims.util.ImsUri> r3 = r14.mCcParticipants
            if (r3 == 0) goto L_0x0180
            java.util.List<com.sec.ims.util.ImsUri> r3 = r14.mCcParticipants
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x0180
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            java.util.List<com.sec.ims.util.ImsUri> r4 = r14.mCcParticipants
            java.util.Set r3 = r3.normalizeUri((int) r1, (java.util.Collection<com.sec.ims.util.ImsUri>) r4)
            r4 = r2
            com.sec.internal.ims.servicemodules.im.ImMessage r4 = (com.sec.internal.ims.servicemodules.im.ImMessage) r4
            r4.setGroupCcListUri(r3)
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onIncomingMessageReceived, groupCcList="
            r5.append(r6)
            r5.append(r3)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
        L_0x0180:
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            com.sec.internal.ims.servicemodules.im.ImDump r3 = r3.getImDump()
            boolean r4 = r0.isGroupChat()
            java.lang.String r5 = r0.getChatId()
            java.lang.String r6 = r2.getImdnId()
            r3.dumpIncomingMessageReceived(r1, r4, r5, r6)
            java.lang.Object r3 = r14.mRawHandle
            r0.receiveMessage(r2, r3)
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            com.sec.internal.constants.ims.servicemodules.im.ChatData r4 = r0.getChatData()
            java.lang.String r4 = r4.getOwnIMSI()
            com.sec.ims.util.ImsUri r5 = r14.mSender
            java.util.Date r6 = r14.mImdnTime
            r3.updateServiceAvailability(r4, r5, r6)
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r3 = r13.mImSessionProcessor
            com.sec.internal.ims.servicemodules.im.ImBigDataProcessor r3 = r3.getBigDataProcessor()
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r5 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            boolean r4 = r0.isGroupChat()
            if (r4 != 0) goto L_0x01c6
            java.util.Set r4 = r0.getParticipantsUri()
            boolean r4 = com.sec.internal.ims.util.ChatbotUriUtil.hasChatbotUri(r4, r1)
            if (r4 == 0) goto L_0x01c6
            r4 = 1
            r6 = r4
            goto L_0x01c7
        L_0x01c6:
            r6 = r10
        L_0x01c7:
            r7 = 0
            r8 = 0
            r9 = 0
            r10 = 0
            java.lang.String r4 = r2.getMaapTrafficType()
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType r11 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.fromString(r4)
            r4 = r1
            r3.storeDRCSInfoToImsLogAgent(r4, r5, r6, r7, r8, r9, r10, r11)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImProcessor.onIncomingMessageReceived(com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent):void");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x013e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onIncomingSlmMessage(com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent r24) {
        /*
            r23 = this;
            r0 = r23
            r7 = r24
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onIncomingSlmMessageReceived: "
            r2.append(r3)
            r2.append(r7)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.servicemodules.im.ImModule r1 = r0.mImModule
            java.lang.String r2 = r7.mOwnImsi
            int r15 = r1.getPhoneIdByIMSI(r2)
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r1 = r0.mImSessionProcessor
            java.util.List<com.sec.ims.util.ImsUri> r2 = r7.mParticipants
            com.sec.ims.util.ImsUri r3 = r7.mSender
            java.util.Set r14 = r1.getNormalizedParticipants(r15, r2, r3)
            int r1 = r14.size()
            r13 = 0
            r12 = 1
            if (r1 <= r12) goto L_0x0036
            r1 = r12
            goto L_0x0037
        L_0x0036:
            r1 = r13
        L_0x0037:
            r17 = r1
            if (r17 == 0) goto L_0x003e
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r1 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT
            goto L_0x0040
        L_0x003e:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r1 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_ONE_CHAT
        L_0x0040:
            r11 = r1
            com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces r1 = r7.mCpimNamespaces
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r10 = com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper.extractImDirection(r15, r1)
            com.sec.internal.ims.servicemodules.im.ImCache r1 = r0.mCache
            java.lang.String r2 = r7.mOwnImsi
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r3 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.OFF
            com.sec.internal.ims.servicemodules.im.ImSession r1 = r1.getImSessionByParticipants(r14, r11, r2, r3)
            if (r1 != 0) goto L_0x005d
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r0.mCache
            java.lang.String r3 = r7.mOwnImsi
            com.sec.internal.ims.servicemodules.im.ImSession r1 = r2.makeNewEmptySession(r3, r14, r11, r10)
            r9 = r1
            goto L_0x005e
        L_0x005d:
            r9 = r1
        L_0x005e:
            com.sec.internal.ims.servicemodules.im.ImModule r1 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImDump r1 = r1.getImDump()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onIncomingSlmMessageReceived: conversationId="
            r2.append(r3)
            java.lang.String r3 = r9.getConversationId()
            r2.append(r3)
            java.lang.String r3 = ", imdnId="
            r2.append(r3)
            java.lang.String r3 = r7.mImdnMessageId
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.addEventLogs(r2)
            boolean r1 = r9.isGroupChat()
            if (r1 != 0) goto L_0x009d
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r1 = r0.mImSessionProcessor
            com.sec.ims.util.ImsUri r2 = r9.getRemoteUri()
            com.sec.internal.constants.ims.servicemodules.im.ChatData r3 = r9.getChatData()
            java.lang.String r3 = r3.getOwnIMSI()
            r1.setLegacyLatching(r2, r13, r3)
        L_0x009d:
            java.lang.String r1 = r7.mContributionId
            r9.setContributionId(r1)
            java.lang.String r1 = r7.mConversationId
            r9.setConversationId(r1)
            java.lang.String r1 = r7.mContributionId
            r9.setInReplyToContributionId(r1)
            com.sec.internal.ims.servicemodules.im.ImModule r1 = r0.mImModule
            java.lang.String r1 = r1.getOwnPhoneNum(r15)
            r9.setOwnPhoneNum(r1)
            com.sec.internal.ims.servicemodules.im.ImModule r1 = r0.mImModule
            java.lang.String r1 = r1.getImsiFromPhoneId(r15)
            r9.setOwnImsi(r1)
            boolean r1 = r7.mIsTokenUsed
            r9.setIsTokenUsed(r1)
            com.sec.internal.ims.servicemodules.im.ImCache r1 = r0.mCache
            java.lang.String r2 = r7.mImdnMessageId
            java.lang.String r3 = r9.getChatId()
            java.lang.String r4 = r9.getOwnImsi()
            com.sec.internal.ims.servicemodules.im.MessageBase r18 = r1.getMessage(r2, r10, r3, r4)
            r1 = 0
            java.lang.String r2 = r7.mBody
            if (r2 == 0) goto L_0x012e
            java.lang.String r2 = r7.mContentType
            if (r2 == 0) goto L_0x012e
            java.lang.String r2 = r7.mContentType
            boolean r2 = com.sec.internal.ims.servicemodules.im.ImMultipart.isMultipart(r2)
            if (r2 == 0) goto L_0x012e
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "onIncomingSlmMessage: isMultipart"
            android.util.Log.i(r2, r3)
            com.sec.internal.ims.servicemodules.im.ImMultipart r2 = new com.sec.internal.ims.servicemodules.im.ImMultipart
            java.lang.String r3 = r7.mBody
            java.lang.String r4 = r7.mContentType
            r2.<init>(r3, r4)
            java.lang.String r3 = r2.getSuggestion()
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 != 0) goto L_0x012e
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "onIncomingSlmMessage: message includes suggestion"
            android.util.Log.i(r3, r4)
            java.lang.String r3 = r2.getBody()
            r7.mBody = r3
            java.lang.String r3 = r2.getContentType()
            r7.mContentType = r3
            java.lang.String r1 = r2.getSuggestion()
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onIncomingSlmMessage: suggestion ="
            r4.append(r5)
            r4.append(r1)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            r19 = r1
            goto L_0x0130
        L_0x012e:
            r19 = r1
        L_0x0130:
            if (r18 == 0) goto L_0x013e
            boolean r1 = r7.mIsPublicAccountMsg
            if (r1 != 0) goto L_0x013e
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "duplicate message, ignore"
            android.util.Log.e(r1, r2)
            return
        L_0x013e:
            java.lang.String r1 = r7.mContentType
            if (r1 == 0) goto L_0x0175
            java.lang.String r1 = r7.mContentType
            java.lang.String r2 = "application/vnd.gsma.rcs-ft-http+xml"
            boolean r1 = r1.startsWith(r2)
            if (r1 == 0) goto L_0x0175
            com.sec.internal.ims.servicemodules.im.ImCache r1 = r0.mCache
            java.lang.String r2 = r9.getOwnImsi()
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r4 = r3.getImConfig(r15)
            boolean r4 = r4.isFtHttpOverDefaultPdn()
            android.net.Network r5 = r3.getNetwork(r4, r15)
            r3 = r9
            r4 = r24
            r6 = r19
            com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = r1.makeNewIncomingFtHttpMessage((java.lang.String) r2, (com.sec.internal.ims.servicemodules.im.ImSession) r3, (com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent) r4, (android.net.Network) r5, (java.lang.String) r6)
            r20 = r10
            r21 = r11
            r8 = r12
            r16 = r13
            r22 = r14
            r14 = r9
            goto L_0x01ca
        L_0x0175:
            boolean r1 = r7.mIsRoutingMsg
            if (r1 == 0) goto L_0x01ae
            com.sec.internal.ims.servicemodules.im.ImModule r8 = r0.mImModule
            com.sec.ims.util.ImsUri r1 = r7.mRequestUri
            com.sec.ims.util.ImsUri r2 = r7.mPAssertedId
            com.sec.ims.util.ImsUri r3 = r7.mSender
            com.sec.ims.util.ImsUri r4 = r7.mReceiver
            boolean r5 = r9.isGroupChat()
            r6 = r9
            r9 = r1
            r20 = r10
            r10 = r2
            r21 = r11
            r11 = r3
            r3 = r12
            r12 = r4
            r16 = r13
            r13 = r5
            r22 = r14
            r14 = r15
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r1 = r8.getMsgRoutingType(r9, r10, r11, r12, r13, r14)
            r7.mRoutingType = r1
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r1 = r7.mRoutingType
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r2 = com.sec.internal.constants.ims.servicemodules.im.RoutingType.SENT
            if (r1 != r2) goto L_0x01b8
            boolean r1 = r6.isGroupChat()
            if (r1 != 0) goto L_0x01b8
            com.sec.ims.util.ImsUri r1 = r7.mReceiver
            r7.mSender = r1
            goto L_0x01b8
        L_0x01ae:
            r6 = r9
            r20 = r10
            r21 = r11
            r3 = r12
            r16 = r13
            r22 = r14
        L_0x01b8:
            com.sec.internal.ims.servicemodules.im.ImCache r1 = r0.mCache
            java.lang.String r2 = r6.getOwnImsi()
            r5 = 0
            r8 = r3
            r3 = r6
            r4 = r24
            r14 = r6
            r6 = r19
            com.sec.internal.ims.servicemodules.im.ImMessage r1 = r1.makeNewIncomingMessage((java.lang.String) r2, (com.sec.internal.ims.servicemodules.im.ImSession) r3, (com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent) r4, (android.net.Network) r5, (java.lang.String) r6)
        L_0x01ca:
            boolean r2 = r14.isGroupChat()
            if (r2 != 0) goto L_0x01dc
            boolean r2 = r7.mIsChatbotRole
            if (r2 == 0) goto L_0x01dc
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotMessagingTech r2 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING
            r1.setChatbotMessagingTech(r2)
            r14.updateIsChatbotRole(r8)
        L_0x01dc:
            r14.receiveSlmMessage(r1)
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r2 = r0.mImSessionProcessor
            com.sec.internal.ims.servicemodules.im.ImBigDataProcessor r2 = r2.getBigDataProcessor()
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r10 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            boolean r3 = r14.isGroupChat()
            if (r3 != 0) goto L_0x01f9
            java.util.Set r3 = r14.getParticipantsUri()
            boolean r3 = com.sec.internal.ims.util.ChatbotUriUtil.hasChatbotUri(r3, r15)
            if (r3 == 0) goto L_0x01f9
            r11 = r8
            goto L_0x01fb
        L_0x01f9:
            r11 = r16
        L_0x01fb:
            r12 = 0
            r13 = 0
            r3 = 0
            r4 = 0
            java.lang.String r5 = r1.getMaapTrafficType()
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotTrafficType r16 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotTrafficType.fromString(r5)
            r8 = r2
            r9 = r15
            r2 = r14
            r14 = r3
            r3 = r15
            r15 = r4
            r8.storeDRCSInfoToImsLogAgent(r9, r10, r11, r12, r13, r14, r15, r16)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImProcessor.onIncomingSlmMessage(com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent):void");
    }

    /* access modifiers changed from: protected */
    public void onProcessPendingMessages(int phoneId) {
        Log.i(LOG_TAG, "EVENT_PROCESS_PENDING_MESSAGES");
        int limit = RcsPolicyManager.getRcsStrategy(phoneId).intSetting(RcsPolicySettings.RcsPolicy.NUM_OF_DISPLAY_NOTIFICATION_ATONCE);
        for (ImSession session : this.mCache.getAllImSessions()) {
            if (this.mImModule.isRegistered(phoneId)) {
                session.processPendingMessages(phoneId);
                int count = 0;
                List<MessageBase> notifications = this.mCache.getMessagesForPendingNotificationByChatId(session.getChatId());
                String str = LOG_TAG;
                Log.i(str, "pending notification list size : " + notifications.size() + " limit : " + limit);
                if (limit > 0) {
                    Iterator it = CollectionUtils.partition(notifications, limit).iterator();
                    while (it.hasNext()) {
                        postDelayed(new Runnable(phoneId, session, (List) it.next()) {
                            public final /* synthetic */ int f$1;
                            public final /* synthetic */ ImSession f$2;
                            public final /* synthetic */ List f$3;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                                this.f$3 = r4;
                            }

                            public final void run() {
                                ImProcessor.this.lambda$onProcessPendingMessages$7$ImProcessor(this.f$1, this.f$2, this.f$3);
                            }
                        }, ((long) count) * 1000);
                        count++;
                    }
                } else {
                    session.processPendingNotifications(notifications);
                }
            }
        }
    }

    public /* synthetic */ void lambda$onProcessPendingMessages$7$ImProcessor(int phoneId, ImSession session, List l) {
        if (this.mImModule.isRegistered(phoneId)) {
            session.processPendingNotifications(l);
        }
    }

    /* access modifiers changed from: protected */
    public Collection<IMessageEventListener> getMessageEventListener(ImConstants.Type type) {
        return this.mMessageEventListeners.get(type);
    }

    private boolean isDuplicateMessage(int phoneId, ImSession session, ImIncomingMessageEvent event) {
        MessageBase msg = this.mCache.getMessage(event.mImdnMessageId, ImCpimNamespacesHelper.extractImDirection(phoneId, event.mCpimNamespaces), session.getChatId(), session.getOwnImsi());
        if (msg == null) {
            return false;
        }
        String str = LOG_TAG;
        Log.e(str, "Duplicated message: " + msg);
        if (!msg.isDeliveredNotificationRequired()) {
            return true;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("sendDeliveredNotification: conversationId=" + session.getConversationId() + ", imdnId=" + event.mImdnMessageId);
        session.sendDeliveredNotification(msg);
        return true;
    }

    private void updateMessageSenderAlias(int phoneId, ImSession session, ImIncomingMessageEvent event) {
        ImsUri normalizedUri = this.mImModule.normalizeUri(phoneId, event.mSender);
        if (normalizedUri != null) {
            if (!this.mImModule.getImConfig(phoneId).getUserAliasEnabled()) {
                event.mUserAlias = "";
            } else if (!session.isGroupChat() && event.mUserAlias.isEmpty()) {
                ImParticipant participant = session.getParticipant(normalizedUri);
                if (participant == null) {
                    IMSLog.e(LOG_TAG, "Participant is null");
                } else {
                    event.mUserAlias = participant.getUserAlias();
                }
            }
            if (session.isGroupChat()) {
                session.updateParticipantAlias(event.mUserAlias, session.getParticipant(normalizedUri));
            }
        }
    }
}
