package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Network;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.listener.FtMessageListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.ims.util.ThumbnailTool;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class FtProcessor extends Handler implements FtMessageListener {
    private static final int EVENT_REJECT_FT_RESUME_INVITE = 1;
    private static final String LOG_TAG = FtProcessor.class.getSimpleName();
    private ImCache mCache;
    private Context mContext;
    private final CollectionUtils.ArrayListMultimap<ImConstants.Type, IFtEventListener> mFtEventListeners = CollectionUtils.createArrayListMultimap();
    private ImModule mImModule;
    private final IImServiceInterface mImService;
    private ImSessionProcessor mImSessionProcessor;
    private ImTranslation mImTranslation;
    private final Map<Integer, RemoteCallbackList<IImsOngoingFtEventListener>> mImsFtListenerList = new HashMap();
    private RcsSettingsUtils mRcsSettingsUtils;
    private final ISlmServiceInterface mSlmService;
    private final ThumbnailTool mThumbnailTool;

    public FtProcessor(Context context, IImServiceInterface imService, ImModule imModule, ImCache imCache) {
        this.mContext = context;
        this.mImService = imService;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mSlmService = ImsRegistry.getHandlerFactory().getSlmHandler();
        this.mThumbnailTool = new ThumbnailTool(context);
        this.mRcsSettingsUtils = RcsSettingsUtils.getInstance(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void init(ImSessionProcessor imSessionProcessor, ImTranslation imTranslation) {
        this.mImSessionProcessor = imSessionProcessor;
        this.mImTranslation = imTranslation;
    }

    /* access modifiers changed from: protected */
    public void registerFtEventListener(ImConstants.Type type, IFtEventListener listener) {
        this.mFtEventListeners.put(type, listener);
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        super.handleMessage(msg);
        if (msg.what == 1 && (ar = (AsyncResult) msg.obj) != null && ((FtResult) ar.result).getImError() != ImError.SUCCESS) {
            Log.e(LOG_TAG, "CancelingState: Failed to reject transfer.");
        }
    }

    /* access modifiers changed from: protected */
    public void registerImsOngoingFtListenerByPhoneId(IImsOngoingFtEventListener listener, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "registerImsOngoingFtListener phoneId= " + phoneId);
        if (!this.mImsFtListenerList.containsKey(Integer.valueOf(phoneId))) {
            this.mImsFtListenerList.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        synchronized (this.mImsFtListenerList) {
            RemoteCallbackList<IImsOngoingFtEventListener> listenerList = this.mImsFtListenerList.get(Integer.valueOf(phoneId));
            if (listener != null) {
                listenerList.register(listener);
                notifyOngoingFtEvent(this.mCache.hasProcessingFileTransfer(), phoneId);
                return;
            }
            Log.e(LOG_TAG, "no registerImsOngoingFtListener and not work");
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterImsOngoingListenerByPhoneId(IImsOngoingFtEventListener listener, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "unregisterImsOngoingListener phoneId= " + phoneId);
        if (this.mImsFtListenerList.containsKey(Integer.valueOf(phoneId))) {
            synchronized (this.mImsFtListenerList) {
                RemoteCallbackList<IImsOngoingFtEventListener> listenerlist = this.mImsFtListenerList.get(Integer.valueOf(phoneId));
                if (listener != null) {
                    listenerlist.unregister(listener);
                }
            }
        }
    }

    public void onTransferCreated(FtMessage msg) {
        String str = LOG_TAG;
        Log.i(str, "onTransferCreated: " + msg);
        onNotifyCloudMsgFtEvent(msg);
        if (this.mImSessionProcessor.isReportMsg(msg)) {
            ImSession session = this.mCache.getImSession(msg.getChatId());
            if (session != null) {
                session.sendFile(msg);
                return;
            }
            return;
        }
        for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
            listener.onFileTransferCreated(msg);
            listener.onFileTransferAttached(msg);
        }
    }

    public void onTransferReceived(FtMessage msg) {
        String str = LOG_TAG;
        Log.i(str, "onFileTransferReceived: " + msg);
        onNotifyCloudMsgFtEvent(msg);
        for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
            listener.onFileTransferReceived(msg);
        }
    }

    public void onTransferProgressReceived(FtMessage msg) {
        String str = LOG_TAG;
        Log.i(str, "onTransferProgressReceived: " + msg.getId() + " " + msg.getTransferredBytes() + "/" + msg.getFileSize());
        if (!this.mImSessionProcessor.isReportMsg(msg)) {
            for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
                listener.onTransferProgressReceived(msg);
            }
        }
    }

    public void onTransferCompleted(FtMessage msg) {
        String contentType;
        if (!this.mImSessionProcessor.isReportMsg(msg)) {
            IMnoStrategy mnoStrategy = this.mImModule.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(msg.getOwnIMSI()));
            if (mnoStrategy != null && mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.DISPLAY_FT_IN_GALLERY) && msg.isIncoming() && (contentType = msg.getContentType()) != null && (contentType.contains(TMOConstants.CallLogTypes.VIDEO) || contentType.contains(CallConstants.ComposerData.IMAGE))) {
                String str = LOG_TAG;
                Log.i(str, "update gallery app: " + contentType);
                MediaScannerConnection.scanFile(this.mContext, new String[]{msg.getFilePath()}, (String[]) null, (MediaScannerConnection.OnScanCompletedListener) null);
            }
            this.mImModule.setCountReconfiguration(0);
            this.mImModule.removeReconfigurationEvent();
            for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
                listener.onTransferCompleted(msg);
            }
            if (!(msg instanceof FtHttpOutgoingMessage)) {
                ImSession session = this.mCache.getImSession(msg.getChatId());
                this.mCache.removeFromPendingList(msg.getId());
                if (session != null && !this.mCache.hasFileTransferInprogress()) {
                    this.mImSessionProcessor.notifyImSessionClosed(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()));
                }
            }
        } else if (msg.getReportMsgParams() != null) {
            this.mCache.deleteMessage(msg.getId());
            this.mImTranslation.onMessageReportResponse(Long.valueOf((long) msg.getReportMsgParams().getSpamMsgId()), true);
        }
    }

    public void onTransferCanceled(FtMessage msg) {
        if (!this.mImSessionProcessor.isReportMsg(msg)) {
            if (ImsGateConfig.isGateEnabled()) {
                IMSLog.g("GATE", "<GATE-M>MMS_ERROR</GATE-M>");
            }
            for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
                listener.onTransferCanceled(msg);
            }
            ImSession session = this.mCache.getImSession(msg.getChatId());
            if (session == null) {
                Log.e(LOG_TAG, "onTransferCanceled: session not found in the cache.");
            } else if (!this.mCache.hasFileTransferInprogress()) {
                this.mImSessionProcessor.notifyImSessionClosed(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()));
            }
        } else if (msg.getReportMsgParams() != null) {
            this.mCache.deleteMessage(msg.getId());
            this.mImTranslation.onMessageReportResponse(Long.valueOf((long) msg.getReportMsgParams().getSpamMsgId()), false);
        }
    }

    public void onTransferInProgress(FtMessage msg) {
        for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
            listener.onTransferStarted(msg);
        }
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session != null) {
            this.mImSessionProcessor.notifyImSessionEstablished(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()));
        }
    }

    public void onAutoResumeTransfer(FtMessage msg) {
        post(new Runnable(msg) {
            public final /* synthetic */ FtMessage f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FtProcessor.this.lambda$onAutoResumeTransfer$0$FtProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onAutoResumeTransfer$0$FtProcessor(FtMessage msg) {
        String str = LOG_TAG;
        Log.i(str, "onAutoResumeTransfer: messageId =" + msg.getId());
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session == null) {
            Log.e(LOG_TAG, "onAutoResumeTransfer: session not found in the cache.");
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()))) {
            Log.e(LOG_TAG, "onAutoResumeTransfer: not registered");
            return;
        }
        if (session.isGroupChat() && !session.isBroadcastMsg(msg)) {
            session.startSession();
        }
        this.mCache.addToPendingList(msg);
        session.resumeTransferFile(msg);
    }

    public Integer onRequestRegistrationType() {
        return this.mImModule.onRequestRegistrationType();
    }

    public String onRequestIncomingFtTransferPath() {
        return FilePathGenerator.getFileDownloadPath(this.mContext, false);
    }

    public void onFileResizingNeeded(FtMessage msg, long resizeLimit) {
        for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
            listener.onFileResizingNeeded(msg, resizeLimit);
        }
    }

    public void onCancelRequestFailed(FtMessage msg) {
        for (IFtEventListener listener : this.mFtEventListeners.get(msg.getType())) {
            listener.onCancelRequestFailed(msg);
        }
    }

    public void onSendDeliveredNotification(FtMessage msg) {
        post(new Runnable(msg) {
            public final /* synthetic */ FtMessage f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FtProcessor.this.lambda$onSendDeliveredNotification$1$FtProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onSendDeliveredNotification$1$FtProcessor(FtMessage msg) {
        String str = LOG_TAG;
        Log.i(str, "onSendDeliveredNotification: msgId=" + msg.getId());
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("sendDeliveredNotification: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", imdnId" + msg.getImdnId());
            session.sendDeliveredNotification(msg);
            return;
        }
        Log.e(LOG_TAG, "session not found in the cache.");
    }

    public void onFtErrorReport(ImError ftError) {
        IRegistrationGovernor governor;
        Log.i(LOG_TAG, "onFtErrorReport");
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration();
        if (imsRegistration != null && (governor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(imsRegistration.getHandle())) != null && ftError == ImError.FORBIDDEN_NO_WARNING_HEADER) {
            Log.i(LOG_TAG, "onFtErrorReport : 403 forbidden w/o warning header");
            governor.onSipError("ft", new SipError(403, "Forbidden"));
        }
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
        this.mImSessionProcessor.onMessageSendingSucceeded(msg);
    }

    public void onMessageSendingFailed(MessageBase ftMessage, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        this.mImSessionProcessor.onMessageSendingFailed(ftMessage, strategyResponse, result);
    }

    public ChatData.ChatType onRequestChatType(String chatId) {
        ImSession session = this.mImSessionProcessor.getImSession(chatId);
        if (session != null) {
            return session.getChatType();
        }
        return null;
    }

    public Message onRequestCompleteCallback(String chatId) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            return session.getFtCompleteCallback();
        }
        return null;
    }

    public Set<ImsUri> onRequestParticipantUris(String chatId) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session != null) {
            return session.getParticipantsUri();
        }
        return new HashSet();
    }

    /* access modifiers changed from: protected */
    public ThumbnailTool getThumbnailTool() {
        return this.mThumbnailTool;
    }

    /* access modifiers changed from: protected */
    public void acceptFileTransfer(int messageId) {
        post(new Runnable(messageId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FtProcessor.this.lambda$acceptFileTransfer$2$FtProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$acceptFileTransfer$2$FtProcessor(int messageId) {
        String str = LOG_TAG;
        Log.i(str, "acceptFileTransfer: messageId=" + messageId);
        FtMessage message = this.mCache.getFtMessage(messageId);
        if (message == null) {
            Log.e(LOG_TAG, "FT not found in the cache.");
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(message.getOwnIMSI()))) {
            Log.i(LOG_TAG, "acceptFileTransfer: not registered");
            if (this.mCache.getImSession(message.getChatId()) == null) {
                Log.e(LOG_TAG, "acceptFileTransfer: No session");
            } else {
                notifyRegistrationError(message);
            }
        } else {
            message.acceptTransfer();
        }
    }

    /* access modifiers changed from: protected */
    public Future<FtMessage> attachFileToSingleChat(int phoneId, String filePath, ImsUri contactUri, Set<NotificationStatus> dispositionNotification, String requestMessageId, String contentType, boolean isprotectedAccountMsg, boolean isResizable, boolean isExtraft, boolean isFtSms, String extInfo, FileDisposition fileDisposition) {
        return attachFileToSingleChat(phoneId, filePath, contactUri, dispositionNotification, requestMessageId, contentType, isprotectedAccountMsg, isResizable, isExtraft, isFtSms, extInfo, fileDisposition, false, false);
    }

    /* access modifiers changed from: protected */
    public Future<FtMessage> attachFileToSingleChat(int phoneId, String filePath, ImsUri contactUri, Set<NotificationStatus> dispositionNotification, String requestMessageId, String contentType, boolean isprotectedAccountMsg, boolean isResizable, boolean isExtraft, boolean isFtSms, String extInfo, FileDisposition fileDisposition, boolean isTokenUsed, boolean isTokenLink) {
        $$Lambda$FtProcessor$1W7ZfSfa0h8vJhXN7jlwDfaA0U r0 = r1;
        $$Lambda$FtProcessor$1W7ZfSfa0h8vJhXN7jlwDfaA0U r1 = new Callable(phoneId, filePath, contactUri, dispositionNotification, requestMessageId, contentType, isprotectedAccountMsg, isResizable, isExtraft, isFtSms, fileDisposition, isTokenUsed, isTokenLink, extInfo) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ boolean f$10;
            public final /* synthetic */ FileDisposition f$11;
            public final /* synthetic */ boolean f$12;
            public final /* synthetic */ boolean f$13;
            public final /* synthetic */ String f$14;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ ImsUri f$3;
            public final /* synthetic */ Set f$4;
            public final /* synthetic */ String f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ boolean f$9;

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
                return FtProcessor.this.lambda$attachFileToSingleChat$3$FtProcessor(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14);
            }
        };
        FutureTask<FtMessage> future = new FutureTask<>(r0);
        post(future);
        return future;
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x0260  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x026b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ com.sec.internal.ims.servicemodules.im.FtMessage lambda$attachFileToSingleChat$3$FtProcessor(int r27, java.lang.String r28, com.sec.ims.util.ImsUri r29, java.util.Set r30, java.lang.String r31, java.lang.String r32, boolean r33, boolean r34, boolean r35, boolean r36, com.sec.internal.constants.ims.servicemodules.im.FileDisposition r37, boolean r38, boolean r39, java.lang.String r40) throws java.lang.Exception {
        /*
            r26 = this;
            r0 = r26
            r1 = r27
            r15 = r28
            r14 = r29
            r13 = r30
            r12 = r32
            r11 = r36
            r10 = r38
            r9 = r39
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "attachFileToSingleChat: filePath="
            r3.append(r4)
            r3.append(r15)
            java.lang.String r4 = " contactUri="
            r3.append(r4)
            java.lang.String r4 = com.sec.internal.log.IMSLog.numberChecker((com.sec.ims.util.ImsUri) r29)
            r3.append(r4)
            java.lang.String r4 = " disp="
            r3.append(r4)
            r3.append(r13)
            java.lang.String r4 = " requestMessageId="
            r3.append(r4)
            r8 = r31
            r3.append(r8)
            java.lang.String r4 = " contentType="
            r3.append(r4)
            r3.append(r12)
            java.lang.String r4 = " isprotectedAccountMsg="
            r3.append(r4)
            r7 = r33
            r3.append(r7)
            java.lang.String r4 = " isResizable="
            r3.append(r4)
            r6 = r34
            r3.append(r6)
            java.lang.String r4 = " isExtraft="
            r3.append(r4)
            r5 = r35
            r3.append(r5)
            java.lang.String r4 = " isFtSms="
            r3.append(r4)
            r3.append(r11)
            java.lang.String r4 = " fileDisposition="
            r3.append(r4)
            r4 = r37
            r3.append(r4)
            java.lang.String r4 = " isTokenUsed="
            r3.append(r4)
            r3.append(r10)
            java.lang.String r4 = " isTokenLink="
            r3.append(r4)
            r3.append(r9)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r1, r3)
            java.util.HashSet r2 = new java.util.HashSet
            r2.<init>()
            r4 = r2
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            com.sec.ims.util.ImsUri r2 = r2.normalizeUri((int) r1, (com.sec.ims.util.ImsUri) r14)
            r4.add(r2)
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r2 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.OFF
            if (r9 == 0) goto L_0x00a2
            goto L_0x00a8
        L_0x00a2:
            if (r10 == 0) goto L_0x00a8
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r2 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.ON
            r3 = r2
            goto L_0x00a9
        L_0x00a8:
            r3 = r2
        L_0x00a9:
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r0.mCache
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_ONE_CHAT
            com.sec.internal.ims.servicemodules.im.ImModule r6 = r0.mImModule
            java.lang.String r6 = r6.getImsiFromPhoneId(r1)
            com.sec.internal.ims.servicemodules.im.ImSession r2 = r2.getImSessionByParticipants(r4, r5, r6, r3)
            if (r2 != 0) goto L_0x00fa
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>()
            r5.add(r14)
            com.sec.internal.ims.servicemodules.im.ImCache r6 = r0.mCache
            r22 = r2
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            java.lang.String r17 = r2.getImsiFromPhoneId(r1)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            java.util.Set r18 = r2.normalizeUri((int) r1, (java.util.Collection<com.sec.ims.util.ImsUri>) r5)
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r19 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_ONE_CHAT
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r20 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            r16 = r6
            r21 = r3
            com.sec.internal.ims.servicemodules.im.ImSession r2 = r16.makeNewEmptySession(r17, r18, r19, r20, r21)
            java.lang.String r6 = LOG_TAG
            r16 = r3
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r17 = r4
            java.lang.String r4 = "session not found, new session created: "
            r3.append(r4)
            r3.append(r2)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r6, r3)
            r6 = r2
            goto L_0x0102
        L_0x00fa:
            r22 = r2
            r16 = r3
            r17 = r4
            r6 = r22
        L_0x0102:
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r5 = r2.getRcsStrategy(r1)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r2 = r2.getImConfig(r1)
            java.util.Set r3 = r6.getParticipantsUri()
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r4 = r6.getChatType()
            boolean r2 = r5.isFTViaHttp(r2, r3, r4)
            r3 = 1
            r4 = 0
            if (r2 != 0) goto L_0x0123
            if (r11 == 0) goto L_0x0121
            goto L_0x0123
        L_0x0121:
            r2 = r4
            goto L_0x0124
        L_0x0123:
            r2 = r3
        L_0x0124:
            r18 = r2
            java.lang.String r2 = "application/vnd.gsma.rcspushlocation+xml"
            r19 = 0
            if (r18 != 0) goto L_0x013a
            boolean r20 = r5.isFtHttpOnlySupported(r4)
            if (r20 == 0) goto L_0x0133
            goto L_0x013a
        L_0x0133:
            r21 = r5
            r25 = r6
            r24 = r16
            goto L_0x0192
        L_0x013a:
            boolean r20 = r2.equals(r12)
            if (r20 != 0) goto L_0x018c
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r0.mCache
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r0.mImModule
            java.lang.String r3 = r3.getImsiFromPhoneId(r1)
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r20 = r4.getImConfig(r1)
            r21 = r5
            boolean r5 = r20.isFtHttpOverDefaultPdn()
            android.net.Network r20 = r4.getNetwork(r5, r1)
            r22 = 0
            r23 = 0
            r24 = r16
            r4 = r6
            r5 = r28
            r25 = r6
            r6 = r29
            r7 = r30
            r8 = r31
            r9 = r32
            r10 = r35
            r11 = r20
            r12 = r36
            r13 = r22
            r14 = r23
            r15 = r37
            r16 = r34
            com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r2 = r2.makeNewOutgoingFtHttpMessage(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16)
            if (r18 != 0) goto L_0x0242
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "attachFileToSingleChat: isFTViaHttp is false"
            android.util.Log.e(r3, r4)
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r3 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE
            r2.cancelTransfer(r3)
            return r19
        L_0x018c:
            r21 = r5
            r25 = r6
            r24 = r16
        L_0x0192:
            java.util.HashSet r5 = new java.util.HashSet
            r5.<init>()
            r14 = r5
            com.sec.internal.ims.servicemodules.im.ImModule r5 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r5 = r5.getImConfig(r1)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r5 = r5.getImMsgTech()
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r6 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ImMsgTech.CPM
            if (r5 != r6) goto L_0x01a7
            goto L_0x01a8
        L_0x01a7:
            r3 = r4
        L_0x01a8:
            r15 = r3
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r3 = r3.getImConfig(r1)
            boolean r16 = r3.isFtStAndFwEnabled()
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r3 = com.sec.internal.ims.registry.ImsRegistry.getServiceModuleManager()
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r13 = r3.getCapabilityDiscoveryModule()
            if (r13 == 0) goto L_0x01c6
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.DISABLED
            r12 = r29
            com.sec.ims.options.Capabilities r3 = r13.getCapabilities((com.sec.ims.util.ImsUri) r12, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r1)
            goto L_0x01ca
        L_0x01c6:
            r12 = r29
            r3 = r19
        L_0x01ca:
            r11 = r3
            if (r15 == 0) goto L_0x01d5
            r10 = r30
            r14.addAll(r10)
            r9 = r32
            goto L_0x021f
        L_0x01d5:
            r10 = r30
            if (r11 == 0) goto L_0x01eb
            if (r16 == 0) goto L_0x01eb
            int r3 = com.sec.ims.options.Capabilities.FEATURE_FT_STORE
            boolean r3 = r11.hasFeature(r3)
            if (r3 == 0) goto L_0x01eb
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r2 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED
            r14.add(r2)
            r9 = r32
            goto L_0x01fc
        L_0x01eb:
            r9 = r32
            boolean r2 = r2.equals(r9)
            if (r2 == 0) goto L_0x01f7
            r14.addAll(r10)
            goto L_0x01fc
        L_0x01f7:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r2 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.NONE
            r14.add(r2)
        L_0x01fc:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "IMDN modified: ["
            r3.append(r4)
            r3.append(r10)
            java.lang.String r4 = "] to ["
            r3.append(r4)
            r3.append(r14)
            java.lang.String r4 = "]"
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
        L_0x021f:
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r0.mCache
            java.lang.String r3 = r25.getOwnImsi()
            r20 = 0
            r4 = r25
            r5 = r28
            r6 = r29
            r7 = r14
            r8 = r31
            r9 = r32
            r10 = r33
            r22 = r11
            r11 = r34
            r12 = r20
            r20 = r13
            r13 = r40
            com.sec.internal.ims.servicemodules.im.FtMessage r2 = r2.makeNewOutgoingFtMessage(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13)
        L_0x0242:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "attachFileToSingleChat: Make new outgoing ft "
            r4.append(r5)
            r4.append(r2)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.s(r3, r4)
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r0.mImModule
            boolean r3 = r3.isRegistered(r1)
            if (r3 != 0) goto L_0x026b
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "attachFileToSingleChat: not registered"
            android.util.Log.e(r3, r4)
            r0.notifyRegistrationError(r2)
            return r19
        L_0x026b:
            java.lang.String r3 = r2.getContentType()
            java.lang.String r4 = "UNSUPPORTED TYPE"
            boolean r3 = r4.equalsIgnoreCase(r3)
            if (r3 == 0) goto L_0x027d
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r3 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r2.cancelTransfer(r3)
            return r19
        L_0x027d:
            java.io.File r3 = new java.io.File
            r4 = r28
            r3.<init>(r4)
            boolean r3 = r3.exists()
            if (r3 != 0) goto L_0x0297
            java.lang.String r3 = LOG_TAG
            java.lang.String r5 = "attachFileToSingleChat: No files found"
            android.util.Log.e(r3, r5)
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r3 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR
            r2.cancelTransfer(r3)
            return r19
        L_0x0297:
            com.sec.internal.ims.servicemodules.im.ImCache r3 = r0.mCache
            r5 = r25
            r3.updateActiveSession(r5)
            r5.attachFile(r2)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtProcessor.lambda$attachFileToSingleChat$3$FtProcessor(int, java.lang.String, com.sec.ims.util.ImsUri, java.util.Set, java.lang.String, java.lang.String, boolean, boolean, boolean, boolean, com.sec.internal.constants.ims.servicemodules.im.FileDisposition, boolean, boolean, java.lang.String):com.sec.internal.ims.servicemodules.im.FtMessage");
    }

    /* access modifiers changed from: protected */
    public Future<FtMessage> attachFileToGroupChat(String chatId, String filePath, Set<NotificationStatus> dispositionNotification, String requestMessageId, String contentType, boolean isResizable, boolean isBroadcast, boolean isExtraFt, boolean isFtSms, String extInfo, FileDisposition fileDisposition) {
        FutureTask<FtMessage> future = new FutureTask<>(new Callable(chatId, filePath, dispositionNotification, requestMessageId, isFtSms, contentType, isResizable, isBroadcast, fileDisposition, isExtraFt, extInfo) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$10;
            public final /* synthetic */ String f$11;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ Set f$3;
            public final /* synthetic */ String f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ FileDisposition f$9;

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
            }

            public final Object call() {
                return FtProcessor.this.lambda$attachFileToGroupChat$4$FtProcessor(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11);
            }
        });
        post(future);
        return future;
    }

    public /* synthetic */ FtMessage lambda$attachFileToGroupChat$4$FtProcessor(String chatId, String filePath, Set dispositionNotification, String requestMessageId, boolean isFtSms, String contentType, boolean isResizable, boolean isBroadcast, FileDisposition fileDisposition, boolean isExtraFt, String extInfo) throws Exception {
        ImSession session;
        FtMessage message;
        FtProcessor ftProcessor;
        String str = chatId;
        String str2 = filePath;
        boolean z = isBroadcast;
        String str3 = LOG_TAG;
        Log.i(str3, "attachFileToGroupChat: chatId=" + str + ", filePath=" + IMSLog.checker(filePath) + ", disp=" + dispositionNotification + ", requestMessageId=" + requestMessageId + "isFtSms=" + isFtSms + ", contentType=" + contentType + ", isResizable=" + isResizable + ", isBroadcast=" + z + ", fileDisposition=" + fileDisposition);
        ImSession session2 = this.mCache.getImSession(str);
        if (session2 == null) {
            String str4 = LOG_TAG;
            Log.e(str4, "attachFileToGroupChat: chat not exist - " + str);
            ImSession imSession = session2;
            String str5 = str2;
            return null;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(session2.getOwnImsi());
        IMnoStrategy mnoStrategy = this.mImModule.getRcsStrategy(phoneId);
        if (mnoStrategy.isFTViaHttp(this.mImModule.getImConfig(phoneId), session2.getParticipantsUri(), session2.getChatType())) {
            boolean isSlm = this.mImModule.isRegistered(phoneId) && this.mImModule.isServiceRegistered(phoneId, "slm") && (!this.mImModule.getImConfig(phoneId).getChatEnabled() || z) && this.mImModule.getImConfig(phoneId).getSlmAuth() == ImConstants.SlmAuth.ENABLED && (!session2.isGroupChat() || z || session2.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT);
            ImCache imCache = this.mCache;
            String imsiFromPhoneId = this.mImModule.getImsiFromPhoneId(phoneId);
            ImsUri sessionUri = session2.getSessionUri();
            ImModule imModule = this.mImModule;
            IMnoStrategy mnoStrategy2 = mnoStrategy;
            Network network = imModule.getNetwork(imModule.getImConfig(phoneId).isFtHttpOverDefaultPdn(), phoneId);
            String str6 = imsiFromPhoneId;
            String str7 = "attachFileToGroupChat: No files found";
            int phoneId2 = phoneId;
            ImSession session3 = session2;
            String str8 = str2;
            message = imCache.makeNewOutgoingFtHttpMessage(str6, session2, filePath, sessionUri, dispositionNotification, requestMessageId, contentType, isExtraFt, network, isFtSms, isBroadcast, isSlm, fileDisposition, isResizable);
            if (!new File(str8).exists()) {
                Log.e(LOG_TAG, str7);
                message.cancelTransfer(CancelReason.ERROR);
                return null;
            }
            String str9 = str8;
            session = session3;
            IMnoStrategy iMnoStrategy = mnoStrategy2;
            int i = phoneId2;
            ftProcessor = this;
        } else {
            String str10 = "attachFileToGroupChat: No files found";
            IMnoStrategy mnoStrategy3 = mnoStrategy;
            ImSession session4 = session2;
            String str11 = str2;
            ftProcessor = this;
            int phoneId3 = phoneId;
            message = ftProcessor.mCache.makeNewOutgoingFtMessage(ftProcessor.mImModule.getImsiFromPhoneId(phoneId3), session4, filePath, session4.getSessionUri(), dispositionNotification, requestMessageId, contentType, false, isResizable, isBroadcast, extInfo);
            String str12 = LOG_TAG;
            IMSLog.s(str12, "attachFileToGroupChat: Make new outgoing ft " + message);
            if (!ftProcessor.mImModule.isRegistered(phoneId3)) {
                IMSLog.i(LOG_TAG, "attachFileToGroupChat: not registered");
                ftProcessor.notifyRegistrationError(message);
                return null;
            } else if (!new File(str11).exists()) {
                Log.e(LOG_TAG, str10);
                message.cancelTransfer(CancelReason.ERROR);
                return null;
            } else if (mnoStrategy3.isFtHttpOnlySupported(true)) {
                Log.e(LOG_TAG, "attachFileToGroupChat: FT MSRP is not supported");
                message.cancelTransfer(session4.getChatType() == ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT ? CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE : CancelReason.ERROR);
                return null;
            } else {
                session = session4;
                IMnoStrategy.StrategyResponse strategyResponse = ftProcessor.mImModule.getRcsStrategy(phoneId3).checkCapability(session4.getParticipantsUri(), (long) Capabilities.FEATURE_FT_SERVICE, session4.getChatType(), session.isBroadcastMsg(message));
                if (!session.isBroadcastMsg(message) && strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.NONE) {
                    session.startSession();
                }
            }
        }
        ftProcessor.mCache.updateActiveSession(session);
        session.attachFile(message);
        return message;
    }

    /* access modifiers changed from: protected */
    public void sendFile(long messageId) {
        sendFile(messageId, (String) null);
    }

    /* access modifiers changed from: protected */
    public void sendFile(long messageId, String deviceName) {
        post(new Runnable(messageId, deviceName) {
            public final /* synthetic */ long f$1;
            public final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r4;
            }

            public final void run() {
                FtProcessor.this.lambda$sendFile$5$FtProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$sendFile$5$FtProcessor(long messageId, String deviceName) {
        String str = LOG_TAG;
        Log.i(str, "sendFile: messageId=" + messageId + ", deviceName=" + deviceName);
        FtMessage msg = this.mCache.getFtMessage((int) messageId);
        if (msg == null) {
            Log.e(LOG_TAG, "sendFile: Message not found in cache");
            return;
        }
        msg.setDeviceName(deviceName);
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session == null) {
            Log.e(LOG_TAG, "sendFile: Session not found in the cache");
            return;
        }
        if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi())) || !(msg instanceof FtMsrpMessage)) {
            if (session.isGroupChat() && !session.isBroadcastMsg(msg) && !msg.mIsSlmSvcMsg) {
                session.startSession();
            }
            session.sendFile(msg);
            return;
        }
        IMSLog.i(LOG_TAG, "sendFile: not registered");
        notifyRegistrationError(msg);
    }

    /* access modifiers changed from: protected */
    public void readFile(long messageId) {
        post(new Runnable(messageId) {
            public final /* synthetic */ long f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FtProcessor.this.lambda$readFile$6$FtProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$readFile$6$FtProcessor(long messageId) {
        String str = LOG_TAG;
        Log.i(str, "readFile: messageId=" + messageId);
        FtMessage msg = this.mCache.getFtMessage((int) messageId);
        if (msg == null) {
            String str2 = LOG_TAG;
            Log.e(str2, "Message not found " + messageId);
            return;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(msg.getOwnIMSI());
        List<String> messageList = new ArrayList<>();
        messageList.add(String.valueOf(msg.getId()));
        if (this.mImModule.isRegistered(phoneId) || !(msg instanceof FtMsrpMessage)) {
            ImSession session = this.mCache.getImSession(msg.getChatId());
            if (session == null) {
                Log.e(LOG_TAG, "readFile: session not found.");
                return;
            }
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("sendDisplayedNotification: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", imdnId=" + msg.getImdnId());
            session.readMessages(messageList);
            return;
        }
        Log.e(LOG_TAG, "readFile: not registered");
        this.mCache.updateDesiredNotificationStatusAsDisplay(messageList);
    }

    /* access modifiers changed from: protected */
    public void rejectFileTransfer(int messageId) {
        post(new Runnable(messageId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FtProcessor.this.lambda$rejectFileTransfer$7$FtProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$rejectFileTransfer$7$FtProcessor(int messageId) {
        String str = LOG_TAG;
        Log.i(str, "rejectFileTransfer: messageId=" + messageId);
        FtMessage message = this.mCache.getFtMessage(messageId);
        if (message == null) {
            Log.e(LOG_TAG, "FT not found in the cache.");
        } else {
            message.rejectTransfer();
        }
    }

    /* access modifiers changed from: protected */
    public void resumeSendingTransfer(int messageId, boolean isResizable) {
        post(new Runnable(messageId, isResizable) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                FtProcessor.this.lambda$resumeSendingTransfer$8$FtProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$resumeSendingTransfer$8$FtProcessor(int messageId, boolean isResizable) {
        String str = LOG_TAG;
        Log.i(str, "resumeSendingTransfer: messageId=" + messageId);
        FtMessage message = this.mCache.getFtMessage(messageId);
        if (message == null) {
            Log.e(LOG_TAG, "resumeSendingTransfer: FT not found in the cache.");
            return;
        }
        message.setIsResizable(isResizable);
        ImSession session = this.mCache.getImSession(message.getChatId());
        if (session == null) {
            Log.e(LOG_TAG, "resumeSendingTransfer: FT not found in the cache.");
            return;
        }
        if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi())) || !(message instanceof FtMsrpMessage)) {
            if (session.isGroupChat() && !session.isBroadcastMsg(message)) {
                session.startSession();
            }
            this.mCache.addToPendingList(message);
            message.removeAutoResumeFileTimer();
            session.resumeTransferFile(message);
            return;
        }
        IMSLog.i(LOG_TAG, "resumeSendingTransfer: not registered");
        notifyRegistrationError(message);
    }

    /* access modifiers changed from: protected */
    public void resumeReceivingTransfer(int messageId) {
        post(new Runnable(messageId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FtProcessor.this.lambda$resumeReceivingTransfer$9$FtProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$resumeReceivingTransfer$9$FtProcessor(int messageId) {
        String str = LOG_TAG;
        Log.i(str, "resumeReceivingTransfer: messageId=" + messageId);
        FtMessage message = this.mCache.getFtMessage(messageId);
        if (message == null) {
            Log.e(LOG_TAG, "resumeReceivingTransfer: FT not found in the cache.");
        } else if (message.isOutgoing()) {
            Log.e(LOG_TAG, "resumeReceivingTransfer: Ignore resume forking FT.");
        } else {
            ImSession session = this.mCache.getImSession(message.getChatId());
            if (session == null) {
                Log.e(LOG_TAG, "FT not found in the cache.");
                return;
            }
            if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi())) || !(message instanceof FtMsrpMessage)) {
                this.mCache.addToPendingList(message);
                if (message instanceof FtMsrpMessage) {
                    Log.i(LOG_TAG, "request resuming FT to sender using INVITE");
                    message.removeAutoResumeFileTimer();
                    session.resumeTransferFile(message);
                    return;
                }
                session.receiveTransfer(message, (FtIncomingSessionEvent) null, true);
                return;
            }
            Log.e(LOG_TAG, "resumeReceivingTransfer: not registered");
            notifyRegistrationError(message);
        }
    }

    /* access modifiers changed from: protected */
    public void cancelFileTransfer(int messageId) {
        post(new Runnable(messageId) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                FtProcessor.this.lambda$cancelFileTransfer$10$FtProcessor(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$cancelFileTransfer$10$FtProcessor(int messageId) {
        String str = LOG_TAG;
        Log.i(str, "cancelFileTransfer: messageId=" + messageId);
        FtMessage message = this.mCache.getFtMessage(messageId);
        if (message == null) {
            Log.e(LOG_TAG, "FT not found in the cache.");
        } else {
            message.cancelTransfer(CancelReason.CANCELED_BY_USER);
        }
    }

    /* access modifiers changed from: protected */
    public void setAutoAcceptFt(int phoneId, int accept) {
        post(new Runnable(phoneId, accept) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                FtProcessor.this.lambda$setAutoAcceptFt$11$FtProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setAutoAcceptFt$11$FtProcessor(int phoneId, int accept) {
        int tmpPhoneId;
        if (RcsUtils.DualRcs.isDualRcsSettings()) {
            tmpPhoneId = phoneId;
        } else {
            tmpPhoneId = SimUtil.getSimSlotPriority();
        }
        Log.i(LOG_TAG, "setAutoAcceptFt: accept=" + accept + " isRoaming=" + this.mImModule.isDataRoaming(tmpPhoneId));
        this.mImModule.getImConfig(tmpPhoneId).setFtAutAccept(this.mContext, accept, this.mImModule.isDataRoaming(tmpPhoneId));
        boolean roaming = false;
        boolean home = accept == 1 || accept == 2;
        if (accept == 2) {
            roaming = true;
        }
        RcsSettingsUtils rcsSettingsUtils = this.mRcsSettingsUtils;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FILE_TRANSFER, home);
            this.mRcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FT_IN_ROAMING, roaming);
        }
    }

    /* access modifiers changed from: protected */
    public void onIncomingFileTransferReceived(FtIncomingSessionEvent event) {
        ImDirection direction;
        FtIncomingSessionEvent ftIncomingSessionEvent = event;
        String str = LOG_TAG;
        Log.i(str, "onIncomingFileTransferReceived: " + ftIncomingSessionEvent);
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onIncomingFileTransferReceived: conversationId=" + ftIncomingSessionEvent.mConversationId + ", imdnId=" + ftIncomingSessionEvent.mImdnId + ", isSLM=" + ftIncomingSessionEvent.mIsSlmSvcMsg);
        int phoneId = this.mImModule.getPhoneIdByIMSI(ftIncomingSessionEvent.mOwnImsi);
        IMnoStrategy mnoStrategy = this.mImModule.getRcsStrategy(phoneId);
        Set<ImsUri> normalizedParticipants = this.mImSessionProcessor.getNormalizedParticipants(phoneId, ftIncomingSessionEvent.mParticipants, ftIncomingSessionEvent.mSenderUri);
        String str2 = LOG_TAG;
        Log.i(str2, "onIncomingFileTransferReceived normalizedParticipants : " + IMSLog.numberChecker((Collection<ImsUri>) normalizedParticipants));
        boolean isGroupChat = normalizedParticipants.size() > 1 || ftIncomingSessionEvent.mIsConference;
        boolean isResumeRequest = ftIncomingSessionEvent.mStart != 0;
        ChatData.ChatType chatType = this.mImSessionProcessor.generateChatType(isGroupChat, ftIncomingSessionEvent.mIsSlmSvcMsg || this.mImModule.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.PARTICIPANTBASED_CLOSED_GROUPCHAT), false);
        ImSession session = this.mImSessionProcessor.findSession(phoneId, ftIncomingSessionEvent.mOwnImsi, isGroupChat, chatType, (String) null, ftIncomingSessionEvent.mConversationId, ftIncomingSessionEvent.mConversationId, normalizedParticipants, ChatMode.OFF);
        ImDirection direction2 = ImCpimNamespacesHelper.extractImDirection(phoneId, ftIncomingSessionEvent.mCpimNamespaces);
        FtMessage message = findFileTransfer(session, ftIncomingSessionEvent, direction2);
        boolean z = session != null;
        boolean isResumeRequest2 = isResumeRequest;
        boolean z2 = isGroupChat;
        ImDirection direction3 = direction2;
        ChatData.ChatType chatType2 = chatType;
        Set<ImsUri> normalizedParticipants2 = normalizedParticipants;
        IMnoStrategy mnoStrategy2 = mnoStrategy;
        int phoneId2 = phoneId;
        RejectFtSessionParams rejectParams = checkForRejectIncomingFileTransfer(phoneId, event, isGroupChat, z, session != null && session.getChatData().isMuted(), message != null, isResumeRequest2, message != null && message.getStatus() == ImConstants.Status.SENT);
        if (rejectParams != null) {
            rejectFtSession(rejectParams);
            return;
        }
        boolean isResumeRequest3 = isResumeRequest2;
        if (isResumeRequest3 && ftIncomingSessionEvent.mPush && message != null) {
            Log.i(LOG_TAG, "onIncomingFileTransferReceived, resume invite");
            message.setTransferredBytes(ftIncomingSessionEvent.mStart > 0 ? ftIncomingSessionEvent.mStart - 1 : 0);
        }
        if (session == null) {
            Log.e(LOG_TAG, "onIncomingFileTransferReceived: Session not found by participants.");
            direction = direction3;
            session = this.mCache.makeNewEmptySession(ftIncomingSessionEvent.mOwnImsi, normalizedParticipants2, chatType2, direction);
        } else {
            direction = direction3;
        }
        session.setConversationId(ftIncomingSessionEvent.mConversationId);
        session.setContributionId(ftIncomingSessionEvent.mContributionId);
        session.setDirection(direction);
        if (message != null) {
            if (ftIncomingSessionEvent.mDeviceName != null) {
                message.setDeviceName(ftIncomingSessionEvent.mDeviceName);
            }
            this.mCache.addToPendingList(message);
            message.setConversationId(ftIncomingSessionEvent.mConversationId);
            message.setContributionId(ftIncomingSessionEvent.mContributionId);
        } else {
            if (ftIncomingSessionEvent.mIsRoutingMsg) {
                ftIncomingSessionEvent.mRoutingType = this.mImModule.getMsgRoutingType(ftIncomingSessionEvent.mRequestUri, ftIncomingSessionEvent.mPAssertedId, ftIncomingSessionEvent.mSenderUri, ftIncomingSessionEvent.mReceiver, session.isGroupChat(), phoneId2);
                if (ftIncomingSessionEvent.mRoutingType == RoutingType.SENT && !session.isGroupChat()) {
                    ftIncomingSessionEvent.mSenderUri = ftIncomingSessionEvent.mReceiver;
                }
            }
            message = this.mCache.makeNewIncomingFtMessage(session.getOwnImsi(), session, ftIncomingSessionEvent, ftIncomingSessionEvent.mIsSlmSvcMsg);
        }
        if (!session.isGroupChat()) {
            this.mImSessionProcessor.setLegacyLatching(session.getRemoteUri(), false, session.getChatData().getOwnIMSI());
        }
        session.receiveTransfer(message, ftIncomingSessionEvent, isResumeRequest3);
        if (!isResumeRequest3) {
        } else if (mnoStrategy2.boolSetting(RcsPolicySettings.RcsPolicy.AUTO_ACCEPT_FT_RESUME)) {
            Log.i(LOG_TAG, "Resume FT auto accept");
            message.acceptTransfer();
        }
        this.mImModule.updateServiceAvailability(ftIncomingSessionEvent.mOwnImsi, ftIncomingSessionEvent.mSenderUri, ftIncomingSessionEvent.mImdnTime);
    }

    /* access modifiers changed from: protected */
    public void handleFileResizeResponse(int messageId, boolean isSuccess, String filePath) {
        post(new Runnable(messageId, isSuccess, filePath) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ String f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                FtProcessor.this.lambda$handleFileResizeResponse$12$FtProcessor(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$handleFileResizeResponse$12$FtProcessor(int messageId, boolean isSuccess, String filePath) {
        FtMessage msg = this.mCache.getFtMessage(messageId);
        if (msg != null) {
            ImSession session = this.mCache.getImSession(msg.getChatId());
            if (session == null) {
                Log.e(LOG_TAG, "handleFileResizeResponse: FT not found in the cache.");
                return;
            }
            if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(session.getOwnImsi()))) {
                IMSLog.i(LOG_TAG, "handleFileResizeResponse: not registered");
                notifyRegistrationError(msg);
            } else if (msg.getCancelReason() == CancelReason.CANCELED_BY_USER) {
                Log.e(LOG_TAG, "handleFileResizeResponse: FT is cancelled already!");
            } else {
                msg.handleFileResizeResponse(isSuccess, filePath);
            }
        } else {
            Log.e(LOG_TAG, "Message not found");
        }
    }

    /* access modifiers changed from: protected */
    public void notifyOngoingFtEvent(boolean state, int phoneId) {
        post(new Runnable(state, phoneId) {
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                FtProcessor.this.lambda$notifyOngoingFtEvent$13$FtProcessor(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$notifyOngoingFtEvent$13$FtProcessor(boolean state, int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "notifyOngoingFtEvent [" + state + "] phoneId = " + phoneId);
        try {
            if (this.mImsFtListenerList.containsKey(Integer.valueOf(phoneId))) {
                RemoteCallbackList<IImsOngoingFtEventListener> listenerList = this.mImsFtListenerList.get(Integer.valueOf(phoneId));
                int length = listenerList.beginBroadcast();
                for (int index = 0; index < length; index++) {
                    listenerList.getBroadcastItem(index).onFtStateChanged(state);
                }
                listenerList.finishBroadcast();
            }
        } catch (RemoteException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void notifyRegistrationError(FtMessage message) {
        IMnoStrategy mnoStrategy = this.mImModule.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(message.getOwnIMSI()));
        if (mnoStrategy == null || !mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FT_FALLBACK_DIRECTLY_OFFLINE)) {
            message.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
        } else {
            message.cancelTransfer(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE);
        }
    }

    /* access modifiers changed from: protected */
    public void handleFileTransferProgress(FtTransferProgressEvent event) {
        String str = LOG_TAG;
        Log.i(str, "handleFileTransferProgress: " + event);
        if (event != null) {
            FtMessage ftMsg = null;
            if (event.mId != -1) {
                ftMsg = this.mCache.getFtMessage(event.mId);
            } else if (event.mRawHandle != null) {
                ftMsg = this.mCache.getFtMsrpMessage(event.mRawHandle);
            }
            if (ftMsg != null) {
                ftMsg.handleTransferProgress(event);
            } else {
                Log.i(LOG_TAG, "handleFileTransferProgress: cannot get FtMessage.");
            }
        }
    }

    private void onNotifyCloudMsgFtEvent(FtMessage msg) {
        Object cldMsgServiceObj;
        IMnoStrategy mnoStrategy = this.mImModule.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(msg.getOwnIMSI()));
        if (mnoStrategy != null && mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.CENTRAL_MSG_STORE)) {
            String str = LOG_TAG;
            Log.i(str, "onNotifyCloudMsgFtEvent: " + msg.getId());
            try {
                Class<?> cldMsgServiceClass = Class.forName("com.sec.internal.ims.cmstore.CloudMessageServiceWrapper");
                if (cldMsgServiceClass != null && (cldMsgServiceObj = cldMsgServiceClass.getMethod("getInstance", new Class[]{Context.class}).invoke((Object) null, new Object[]{this.mContext})) != null) {
                    if (ImDirection.INCOMING == msg.getDirection()) {
                        ReflectionUtils.invoke(cldMsgServiceClass.getMethod("receiveRCSMessage", new Class[]{Integer.TYPE, String.class, String.class}), cldMsgServiceObj, new Object[]{Integer.valueOf(msg.getId()), msg.getImdnId(), null});
                        return;
                    }
                    ReflectionUtils.invoke(cldMsgServiceClass.getMethod("sentRCSMessage", new Class[]{Integer.TYPE, String.class, String.class}), cldMsgServiceObj, new Object[]{Integer.valueOf(msg.getId()), msg.getImdnId(), null});
                }
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void rejectFtSession(RejectFtSessionParams rejectParams) {
        if (rejectParams.mIsSlmSvcMsg) {
            this.mSlmService.rejectFtSlmMessage(rejectParams);
        } else {
            this.mImService.rejectFtSession(rejectParams);
        }
    }

    /* access modifiers changed from: protected */
    public Collection<IFtEventListener> getFtEventListener(ImConstants.Type type) {
        return this.mFtEventListeners.get(type);
    }

    private RejectFtSessionParams checkForRejectIncomingFileTransfer(int phoneId, FtIncomingSessionEvent event, boolean isGroupChat, boolean hasImSession, boolean isMuted, boolean hasDuplicateMessage, boolean isResumeRequest, boolean isSent) {
        int i = phoneId;
        FtIncomingSessionEvent ftIncomingSessionEvent = event;
        if (ftIncomingSessionEvent.mContentType != null && ftIncomingSessionEvent.mContentType.contains(MIMEContentType.LOCATION_PUSH)) {
            ImModule imModule = this.mImModule;
            if (!imModule.getActiveCall(imModule.normalizeUri(phoneId, ftIncomingSessionEvent.mSenderUri)) && this.mImModule.getImConfig(phoneId).getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
                Log.i(LOG_TAG, "Receive geolocation Push via MSRP FT during inactive call!!.");
                return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, (Message) null, FtRejectReason.DECLINE, ftIncomingSessionEvent.mFileTransferId);
            }
        }
        if (isGroupChat) {
            if (!hasImSession && !ftIncomingSessionEvent.mIsSlmSvcMsg) {
                Log.i(LOG_TAG, "onIncomingFileTransferReceived, no GC session for GC FT. auto reject");
                return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, (Message) null, FtRejectReason.NOT_ACCEPTABLE_HERE, ftIncomingSessionEvent.mFileTransferId);
            } else if (hasImSession && isMuted) {
                Log.i(LOG_TAG, "onIncomingFileTransferReceived, user reject GC FT.");
                return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, (Message) null, FtRejectReason.DECLINE, ftIncomingSessionEvent.mFileTransferId);
            }
        }
        if (hasDuplicateMessage && !isResumeRequest && isSent) {
            String str = LOG_TAG;
            Log.i(str, "onIncomingFileTransferReceived, duplicate message with imdnid: " + ftIncomingSessionEvent.mImdnId);
            return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, Message.obtain(this, 1), FtRejectReason.NOT_ACCEPTABLE_HERE, (String) null, ftIncomingSessionEvent.mIsSlmSvcMsg);
        } else if (hasDuplicateMessage || !isResumeRequest || !ftIncomingSessionEvent.mPush) {
            return null;
        } else {
            Log.i(LOG_TAG, "onIncomingFileTransferReceived, resume invite from MT cannot find history, auto reject");
            return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, Message.obtain(this, 1), FtRejectReason.NOT_ACCEPTABLE_HERE, (String) null, ftIncomingSessionEvent.mIsSlmSvcMsg);
        }
    }

    private FtMessage findFileTransfer(ImSession session, FtIncomingSessionEvent event, ImDirection direction) {
        if (session == null) {
            return null;
        }
        FtMessage message = this.mCache.getFtMessageforFtRequest(session.getChatId(), event.mFileName, event.mFileSize, event.mFileTransferId);
        if (message != null || TextUtils.isEmpty(event.mImdnId)) {
            return message;
        }
        MessageBase messageByImdn = this.mCache.getMessage(event.mImdnId, direction, session.getChatId(), session.getOwnImsi());
        if (!(messageByImdn instanceof FtMessage)) {
            return message;
        }
        String str = LOG_TAG;
        Log.i(str, "onIncomingFileTransferReceived, found messageByImdn: " + event.mImdnId);
        return (FtMessage) messageByImdn;
    }
}
