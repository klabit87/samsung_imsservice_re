package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.im.interfaces.FtIntent;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FtTranslation extends TranslationBase implements IFtEventListener {
    private static final String INNER_RELIABLE_URI = "content://com.samsung.rcs.im/getreliableimage/";
    private static final String LOG_TAG = FtTranslation.class.getSimpleName();
    private final Context mContext;
    private final ExecutorService mFileExecutor = Executors.newSingleThreadExecutor();
    private final FtProcessor mFtProcessor;
    private final ImModule mImModule;

    public FtTranslation(Context context, ImModule imModule, FtProcessor ftProcessor) {
        this.mContext = context;
        this.mImModule = imModule;
        imModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA, this);
        this.mImModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT, this);
        this.mFtProcessor = ftProcessor;
    }

    private void broadcastIntent(Intent intent) {
        String str = LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent + intent.getExtras());
        intent.addFlags(LogClass.SIM_EVENT);
        if (this.mImModule.getRcsStrategy() == null || !this.mImModule.getRcsStrategy().isBMode(true)) {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.OWNER);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleIntent(android.content.Intent r5) {
        /*
            r4 = this;
            java.lang.String r0 = r5.getAction()
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Received intent: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r1, r2)
            int r1 = r0.hashCode()
            switch(r1) {
                case -1898279646: goto L_0x0096;
                case -1873675579: goto L_0x008c;
                case -1840010002: goto L_0x0082;
                case -1784273312: goto L_0x0078;
                case -1143332975: goto L_0x006e;
                case -1130273179: goto L_0x0064;
                case -596796384: goto L_0x0059;
                case 1052208476: goto L_0x004e;
                case 1399551557: goto L_0x0044;
                case 1514235824: goto L_0x003a;
                case 1870667080: goto L_0x002f;
                case 2024535319: goto L_0x0023;
                default: goto L_0x0021;
            }
        L_0x0021:
            goto L_0x00a1
        L_0x0023:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.READ_FILE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 9
            goto L_0x00a2
        L_0x002f:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.ATTACH_FILE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 0
            goto L_0x00a2
        L_0x003a:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.TRANSFER_DECLINE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 4
            goto L_0x00a2
        L_0x0044:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.RESUME_SENDING_FILE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 5
            goto L_0x00a2
        L_0x004e:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.response.RESPONSE_FILE_RESIZE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 11
            goto L_0x00a2
        L_0x0059:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.SET_AUTO_ACCEPT_FT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 10
            goto L_0x00a2
        L_0x0064:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.SEND_FILE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 7
            goto L_0x00a2
        L_0x006e:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.RESUME_INCOMING_FILE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 6
            goto L_0x00a2
        L_0x0078:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.TRANSFER_CANCEL"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 3
            goto L_0x00a2
        L_0x0082:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.TRANSFER_ACCEPT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 2
            goto L_0x00a2
        L_0x008c:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.ATTACH_FILE_TO_GROUP_CHAT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 1
            goto L_0x00a2
        L_0x0096:
            java.lang.String r1 = "com.samsung.rcs.framework.filetransfer.action.SEND_FILE_TO_GROUP_CHAT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 8
            goto L_0x00a2
        L_0x00a1:
            r1 = -1
        L_0x00a2:
            switch(r1) {
                case 0: goto L_0x00e4;
                case 1: goto L_0x00e0;
                case 2: goto L_0x00dc;
                case 3: goto L_0x00d8;
                case 4: goto L_0x00d4;
                case 5: goto L_0x00d0;
                case 6: goto L_0x00cc;
                case 7: goto L_0x00c8;
                case 8: goto L_0x00c8;
                case 9: goto L_0x00c4;
                case 10: goto L_0x00c0;
                case 11: goto L_0x00bc;
                default: goto L_0x00a5;
            }
        L_0x00a5:
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Unexpected intent received. acition="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            goto L_0x00e8
        L_0x00bc:
            r4.handleFileResizeResponse(r5)
            goto L_0x00e8
        L_0x00c0:
            r4.requestSetAutoAcceptFt(r5)
            goto L_0x00e8
        L_0x00c4:
            r4.requestReadFile(r5)
            goto L_0x00e8
        L_0x00c8:
            r4.requestSendFile(r5)
            goto L_0x00e8
        L_0x00cc:
            r4.requestResumeReceivingFileTransfer(r5)
            goto L_0x00e8
        L_0x00d0:
            r4.requestResumeSendingFileTransfer(r5)
            goto L_0x00e8
        L_0x00d4:
            r4.requestDeclineFileTransfer(r5)
            goto L_0x00e8
        L_0x00d8:
            r4.requestCancelFileTransfer(r5)
            goto L_0x00e8
        L_0x00dc:
            r4.requestAcceptFileTransfer(r5)
            goto L_0x00e8
        L_0x00e0:
            r4.requestAttachFileToGroupChat(r5)
            goto L_0x00e8
        L_0x00e4:
            r4.requestAttachFileToSingleChat(r5)
        L_0x00e8:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtTranslation.handleIntent(android.content.Intent):void");
    }

    private void requestAttachFileToSingleChat(Intent intent) {
        Bundle extras = intent.getExtras();
        String filePath = extras.getString(FtIntent.Extras.EXTRA_FILE_PATH);
        Uri uri = (Uri) extras.getParcelable("contactUri");
        boolean isResizable = extras.getBoolean("is_resizable", false);
        String disposition = extras.getString("disposition_notification");
        FileDisposition fileDisposition = FileDisposition.valueOf(intent.getIntExtra("file_disposition", FileDisposition.ATTACH.toInt()));
        String requestMessageId = String.valueOf(extras.getLong(FtIntent.Extras.EXTRA_REQUEST_SESSION_ID));
        boolean isPublicAccountMsg = extras.getBoolean("is_publicAccountMsg", false);
        boolean isExtraFt = extras.getBoolean(FtIntent.Extras.EXTRA_EXTRA_FT, false);
        boolean isFtSms = extras.getBoolean(FtIntent.Extras.EXTRA_IS_FTSMS, false);
        String slotId = extras.getString("sim_slot_id");
        String contentType = extras.getString(FtIntent.Extras.EXTRA_FT_CONTENTTYPE);
        boolean isTokenUsed = extras.getBoolean(ImIntent.Extras.IS_TOKEN_USED, false);
        boolean isTokenLink = extras.getBoolean(ImIntent.Extras.IS_TOKEN_LINK, false);
        if (isPublicAccountMsg) {
            PublicAccountUri.setPublicAccountDomain(extras.getString("publicAccount_Send_Domain"));
        }
        if (filePath == null || uri == null) {
            Bundle bundle = extras;
            String str = LOG_TAG;
            Log.e(str, "illegal arguments from message app: filePath: " + filePath + "uri: " + IMSLog.checker(uri) + "disposition: " + disposition + "requestMessageId: " + requestMessageId);
            return;
        }
        $$Lambda$FtTranslation$4M5q0SmnRLd7y6LhM2sHuHcL4 r26 = r0;
        ExecutorService executorService = this.mFileExecutor;
        Bundle bundle2 = extras;
        $$Lambda$FtTranslation$4M5q0SmnRLd7y6LhM2sHuHcL4 r0 = new Runnable(slotId, filePath, extras, uri, disposition, requestMessageId, contentType, isPublicAccountMsg, isResizable, isExtraFt, isFtSms, fileDisposition, isTokenUsed, isTokenLink) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$10;
            public final /* synthetic */ boolean f$11;
            public final /* synthetic */ FileDisposition f$12;
            public final /* synthetic */ boolean f$13;
            public final /* synthetic */ boolean f$14;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ Bundle f$3;
            public final /* synthetic */ Uri f$4;
            public final /* synthetic */ String f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ String f$7;
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

            public final void run() {
                FtTranslation.this.lambda$requestAttachFileToSingleChat$0$FtTranslation(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14);
            }
        };
        executorService.execute(r26);
        String str2 = requestMessageId;
        String str3 = disposition;
        String str4 = filePath;
    }

    public /* synthetic */ void lambda$requestAttachFileToSingleChat$0$FtTranslation(String slotId, String filePath, Bundle extras, Uri uri, String disposition, String requestMessageId, String contentType, boolean isPublicAccountMsg, boolean isResizable, boolean isExtraFt, boolean isFtSms, FileDisposition fileDisposition, boolean isTokenUsed, boolean isTokenLink) {
        String str = slotId;
        int phoneId = 0;
        if (str != null) {
            try {
                phoneId = Integer.parseInt(slotId);
            } catch (NumberFormatException e) {
                NumberFormatException numberFormatException = e;
                String str2 = LOG_TAG;
                Log.e(str2, "Invalid slot id : " + str);
            }
        }
        String str3 = LOG_TAG;
        Log.i(str3, "requestAttachFileToSingleChat() phoneId= " + phoneId);
        String filePathFromUri = FileUtils.copyFileFromUri(this.mContext, filePath, extras.getString("fileName"));
        if (!TextUtils.isEmpty(filePathFromUri)) {
            this.mFtProcessor.attachFileToSingleChat(phoneId, filePathFromUri, ImsUri.parse(uri.toString()), NotificationStatus.toSet(disposition), requestMessageId, contentType, isPublicAccountMsg, isResizable, isExtraFt, isFtSms, (String) null, fileDisposition, isTokenUsed, isTokenLink);
        }
    }

    private void requestAttachFileToGroupChat(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.i(LOG_TAG, "requestAttachFileToGroupChat()");
        String chatId = extras.getString(FtIntent.Extras.EXTRA_CHAT_ID);
        String filePath = extras.getString(FtIntent.Extras.EXTRA_FILE_PATH);
        boolean isResizable = extras.getBoolean("is_resizable", false);
        String disposition = extras.getString("disposition_notification");
        String requestMessageId = String.valueOf(extras.getLong(FtIntent.Extras.EXTRA_REQUEST_SESSION_ID));
        boolean isBroadcastMsg = extras.getBoolean("is_broadcast_msg", false);
        boolean isExtraFt = extras.getBoolean(FtIntent.Extras.EXTRA_EXTRA_FT, false);
        boolean isFtSms = extras.getBoolean(FtIntent.Extras.EXTRA_IS_FTSMS, false);
        FileDisposition fileDisposition = FileDisposition.valueOf(intent.getIntExtra("file_disposition", FileDisposition.ATTACH.toInt()));
        String contentType = extras.getString(FtIntent.Extras.EXTRA_FT_CONTENTTYPE);
        if (filePath != null) {
            Bundle bundle = extras;
            $$Lambda$FtTranslation$3XQRDuteLFsZNcSLYDWOsSrkxA r13 = r0;
            String str = chatId;
            ExecutorService executorService = this.mFileExecutor;
            $$Lambda$FtTranslation$3XQRDuteLFsZNcSLYDWOsSrkxA r0 = new Runnable(filePath, extras, chatId, disposition, requestMessageId, contentType, isResizable, isBroadcastMsg, isExtraFt, isFtSms, fileDisposition) {
                public final /* synthetic */ String f$1;
                public final /* synthetic */ boolean f$10;
                public final /* synthetic */ FileDisposition f$11;
                public final /* synthetic */ Bundle f$2;
                public final /* synthetic */ String f$3;
                public final /* synthetic */ String f$4;
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
                }

                public final void run() {
                    FtTranslation.this.lambda$requestAttachFileToGroupChat$1$FtTranslation(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11);
                }
            };
            executorService.execute(r13);
            return;
        }
        String str2 = chatId;
    }

    public /* synthetic */ void lambda$requestAttachFileToGroupChat$1$FtTranslation(String filePath, Bundle extras, String chatId, String disposition, String requestMessageId, String contentType, boolean isResizable, boolean isBroadcastMsg, boolean isExtraFt, boolean isFtSms, FileDisposition fileDisposition) {
        String filePathFromUri = FileUtils.copyFileFromUri(this.mContext, filePath, extras.getString("fileName"));
        if (!TextUtils.isEmpty(filePathFromUri)) {
            this.mImModule.attachFileToGroupChat(chatId, filePathFromUri, NotificationStatus.toSet(disposition), requestMessageId, contentType, isResizable, isBroadcastMsg, isExtraFt, isFtSms, (String) null, fileDisposition);
        }
    }

    private void requestSendFile(Intent intent) {
        Bundle extras = intent.getExtras();
        Long messageId = Long.valueOf(extras.getLong("sessionId"));
        this.mFtProcessor.sendFile(messageId.longValue(), extras.getString("device_name"));
    }

    private void requestReadFile(Intent intent) {
        this.mFtProcessor.readFile(Long.valueOf(intent.getExtras().getLong("sessionId")).longValue());
    }

    private void requestAcceptFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras.containsKey("sessionId")) {
            this.mImModule.acceptFileTransfer(Long.valueOf(extras.getLong("sessionId")).intValue());
        }
    }

    private void requestDeclineFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras.containsKey("sessionId")) {
            this.mImModule.rejectFileTransfer(Long.valueOf(extras.getLong("sessionId")).intValue());
        }
    }

    private void requestResumeSendingFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras.containsKey("sessionId")) {
            Long messageId = Long.valueOf(extras.getLong("sessionId"));
            boolean isResizable = extras.getBoolean("is_resizable", false);
            String str = LOG_TAG;
            Log.i(str, "requestResumeSendingFileTransfer isResizable=" + isResizable);
            this.mImModule.resumeSendingTransfer(messageId.intValue(), isResizable);
        }
    }

    private void requestResumeReceivingFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras.containsKey("sessionId")) {
            this.mImModule.resumeReceivingTransfer(Long.valueOf(extras.getLong("sessionId")).intValue());
        }
    }

    private void requestCancelFileTransfer(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras.containsKey("sessionId")) {
            this.mImModule.cancelFileTransfer(Long.valueOf(extras.getLong("sessionId")).intValue());
        }
    }

    private void requestSetAutoAcceptFt(Intent intent) {
        Bundle extras = intent.getExtras();
        String slotId = extras.getString("sim_slot_id");
        int accept = extras.getInt(FtIntent.Extras.EXTRA_AUTO_ACCEPT_STATE);
        int phoneId = 0;
        if (!TextUtils.isEmpty(slotId)) {
            try {
                phoneId = Integer.valueOf(slotId).intValue();
            } catch (NumberFormatException e) {
                String str = LOG_TAG;
                Log.e(str, "Invalid slot id : " + slotId);
            }
        }
        this.mImModule.setAutoAcceptFt(phoneId, accept);
    }

    private void handleFileResizeResponse(Intent intent) {
        Bundle extras = intent.getExtras();
        long messageId = extras.getLong("sessionId");
        boolean isSuccessful = extras.getBoolean(FtIntent.Extras.EXTRA_REQUEST_RESULT);
        String filePath = extras.getString(FtIntent.Extras.EXTRA_FILE_PATH);
        if (filePath != null) {
            this.mFileExecutor.execute(new Runnable(messageId, filePath, isSuccessful) {
                public final /* synthetic */ long f$1;
                public final /* synthetic */ String f$2;
                public final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                public final void run() {
                    FtTranslation.this.lambda$handleFileResizeResponse$2$FtTranslation(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    public /* synthetic */ void lambda$handleFileResizeResponse$2$FtTranslation(long messageId, String filePath, boolean isSuccessful) {
        FtMessage msg = this.mImModule.getFtMessage((int) messageId);
        if (msg != null) {
            String filePathFromUri = FileUtils.copyFileFromUri(this.mContext, filePath, msg.getFileName());
            if (!TextUtils.isEmpty(filePathFromUri)) {
                this.mFtProcessor.handleFileResizeResponse((int) messageId, isSuccessful, filePathFromUri);
                return;
            }
            return;
        }
        Log.e(LOG_TAG, "Message not found");
    }

    public void onFileTransferCreated(FtMessage msg) {
        Log.i(LOG_TAG, "onFileTransferCreated()");
        Preconditions.checkNotNull(msg, "msg is null");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_CREATED);
        intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, msg.getChatId());
        intent.putExtra("contactUri", msg.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_FILE_PATH, FileUtils.getUriForFileAsString(this.mContext, msg.getFilePath()));
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(msg.getFileSize()).intValue());
        broadcastIntent(intent);
    }

    public void onFileTransferAttached(FtMessage msg) {
        Log.i(LOG_TAG, "onFileTransferAttached()");
        Preconditions.checkNotNull(msg, "msg is null");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_ATTACHED);
        intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_REQUEST_SESSION_ID, msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
        intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, msg.getChatId());
        intent.putExtra("contactUri", msg.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(msg.getFileSize()).intValue());
        intent.putExtra(FtIntent.Extras.EXTRA_FT_MECH, msg.getTransferMech());
        broadcastIntent(intent);
    }

    public void onTransferProgressReceived(FtMessage msg) {
        Log.i(LOG_TAG, "onTransferProgressReceived()");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_PROGRESS);
        intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, msg.getChatId());
        intent.putExtra("sessionDirection", msg.getDirection().getId());
        intent.putExtra("contactUri", msg.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_MESSAGE_IMDN, msg.getImdnId());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_DONE, Long.valueOf(msg.getTransferredBytes()).intValue());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(msg.getFileSize()).intValue());
        broadcastIntent(intent);
    }

    public void onTransferStarted(FtMessage msg) {
    }

    public void onTransferCompleted(FtMessage msg) {
        Log.i(LOG_TAG, "onTransferCompleted()");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_COMPLETED);
        intent.putExtra(FtIntent.Extras.EXTRA_REQUEST_SESSION_ID, msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
        intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, msg.getChatId());
        intent.putExtra("sessionDirection", msg.getDirection().getId());
        intent.putExtra("contactUri", msg.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_MESSAGE_IMDN, msg.getImdnId());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, Long.valueOf(msg.getFileSize()).intValue());
        intent.putExtra("notification_status", msg.getDesiredNotificationStatus().getId());
        intent.putExtra(FtIntent.Extras.EXTRA_FILE_EXPIRE, msg.getDirection() == ImDirection.OUTGOING ? msg.getFileExpire() : "");
        String filePath = msg.getFilePath();
        String str = LOG_TAG;
        Log.i(str, "File Path : " + filePath);
        File cacheDir = this.mContext.getCacheDir();
        if (msg.getDirection() != ImDirection.OUTGOING || filePath == null || cacheDir == null || !filePath.startsWith(cacheDir.getAbsolutePath())) {
            intent.putExtra(FtIntent.Extras.EXTRA_FILE_PATH, FileUtils.getUriForFileAsString(this.mContext, filePath));
        } else {
            Log.i(LOG_TAG, "Remove completed outgoing file");
            FileUtils.removeFile(filePath);
        }
        FileDisposition fileDisposition = msg.getFileDisposition();
        if (fileDisposition == null) {
            fileDisposition = FileDisposition.ATTACH;
        }
        intent.putExtra("file_disposition", fileDisposition.toInt());
        if (fileDisposition == FileDisposition.RENDER) {
            intent.putExtra("playing_length", msg.getPlayingLength());
        }
        intent.putExtra(FtIntent.Extras.FT_SMS_DATAURL, msg.getFileDataUrl());
        intent.putExtra(FtIntent.Extras.EXTRA_IS_FTSMS, msg.isFtSms());
        if (msg.isFtSms()) {
            intent.putExtra(FtIntent.Extras.FT_SMS_BRANDEDURL, msg.getFileBrandedUrl());
        }
        broadcastIntent(intent);
    }

    public void onFileTransferReceived(FtMessage msg) {
        Log.i(LOG_TAG, "onFileTransferReceived()");
        String reliableMessage = msg.getReliableMessage();
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_INCOMING);
        intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, msg.getChatId());
        intent.putExtra("fileName", msg.getFileName());
        intent.putExtra("contactUri", msg.getRemoteUri());
        intent.putExtra(FtIntent.Extras.EXTRA_BYTES_TOTAL, msg.getFileSize());
        intent.putExtra(FtIntent.Extras.EXTRA_THUMBNAIL_PATH, FileUtils.getUriForFileAsString(this.mContext, msg.getThumbnailPath()));
        intent.putExtra(FtIntent.Extras.EXTRA_TIME_DURATION, msg.getTimeDuration());
        intent.putExtra(FtIntent.Extras.EXTRA_OUTGOING_REQUEST, msg.getDirection() == ImDirection.OUTGOING);
        intent.putExtra(FtIntent.Extras.EXTRA_FILE_EXPIRE, msg.getFileExpire());
        intent.putExtra(FtIntent.Extras.EXTRA_IS_STANDALONE, msg.getIsSlmSvcMsg());
        intent.putExtra("message_type", msg.getType().getId());
        FileDisposition fileDisposition = msg.getFileDisposition();
        if (fileDisposition == null) {
            fileDisposition = FileDisposition.ATTACH;
        }
        intent.putExtra("file_disposition", fileDisposition.toInt());
        if (fileDisposition == FileDisposition.RENDER) {
            intent.putExtra("playing_length", msg.getPlayingLength());
        }
        if (msg.mDeviceName != null) {
            intent.putExtra("device_name", msg.mDeviceName);
        }
        intent.putExtra("sessionDirection", msg.mDirection.getId());
        if (msg.isRoutingMsg()) {
            intent.putExtra(ImIntent.Extras.IS_ROUTING_MSG, msg.isRoutingMsg());
            if (!(msg.getRoutingType() == null || msg.getRoutingType() == RoutingType.NONE)) {
                intent.putExtra(ImIntent.Extras.ROUTING_MSG_TYPE, msg.getRoutingType().getId());
            }
        }
        if (reliableMessage != null) {
            intent.putExtra("reliable_message", INNER_RELIABLE_URI + reliableMessage.substring(reliableMessage.lastIndexOf(47) + 1));
        }
        intent.putExtra(FtIntent.Extras.EXTRA_EXTRA_FT, msg.mExtraFt);
        if (msg instanceof FtMsrpMessage) {
            intent.addFlags(LogClass.SIM_EVENT);
        } else {
            intent.putExtra(FtIntent.Extras.EXTRA_FT_AUTODOWNLOAD, msg.mIsAutoDownload ? 1 : 0);
        }
        if (msg.getDirection() == ImDirection.INCOMING) {
            intent.putExtra("from", msg.getRemoteUri() == null ? "" : msg.getRemoteUri().toString());
        }
        intent.putExtra(FtIntent.Extras.EXTRA_FT_MECH, msg.getTransferMech());
        if (msg.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        putMaapExtras(msg, intent);
        String rcsTrafficType = msg.getRcsTrafficType();
        if (rcsTrafficType != null) {
            String str = LOG_TAG;
            Log.i(str, "rcsTrafficType = [" + rcsTrafficType + "]");
            intent.putExtra(ImIntent.Extras.RCS_TRAFFIC_TYPE, rcsTrafficType);
        }
        broadcastIntent(intent);
    }

    public void onTransferCanceled(FtMessage msg) {
        Log.i(LOG_TAG, "onTransferCanceled()");
        if (msg.getCancelReason() != CancelReason.INVALID_URL_TEMPLATE) {
            Intent intent = new Intent();
            intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
            intent.setAction(FtIntent.Actions.ResponseIntents.TRANSFER_CANCELED);
            intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
            intent.putExtra(FtIntent.Extras.EXTRA_REQUEST_SESSION_ID, msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
            intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, msg.getChatId());
            intent.putExtra("sessionDirection", msg.getDirection().getId());
            intent.putExtra("contactUri", msg.getRemoteUri());
            intent.putExtra(FtIntent.Extras.EXTRA_MESSAGE_IMDN, msg.getImdnId());
            intent.putExtra("reason", ((msg.getCancelReason() == null || this.mImModule.hasChatbotParticipant(msg.getChatId())) ? CancelReason.UNKNOWN : msg.getCancelReason()).getId());
            intent.putExtra(FtIntent.Extras.EXTRA_RESUMABLE_OPTION_CODE, msg.getResumableOptionCode());
            intent.putExtra(ImIntent.Extras.ERROR_NOTIFICATION_ID, msg.getErrorNotificationId().ordinal());
            broadcastIntent(intent);
        }
    }

    public void onImdnNotificationReceived(FtMessage msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
        Log.i(LOG_TAG, "onImdnNotificationReceived()");
        broadcastIntent(createImdnNotificationReceivedIntent(msg, remoteUri, status, isGroupChat));
    }

    public void onFileResizingNeeded(FtMessage msg, long resizeLimit) {
        Log.i(LOG_TAG, "requestLargeMessageModeFileResize()");
        Preconditions.checkNotNull(msg, "msg is null");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.RequestIntentToApp.REQUEST_FILE_RESIZE);
        intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_REQUEST_SESSION_ID, msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
        intent.putExtra(FtIntent.Extras.EXTRA_CHAT_ID, msg.getChatId());
        intent.putExtra(FtIntent.Extras.EXTRA_FILE_PATH, FileUtils.getUriForFileAsString(this.mContext, msg.getFilePath()));
        intent.putExtra(FtIntent.Extras.EXTRA_RESIZE_LIMIT, resizeLimit);
        broadcastIntent(intent);
    }

    public void onCancelRequestFailed(FtMessage msg) {
        Log.i(LOG_TAG, "onCancelRequestFailed()");
        Intent intent = new Intent();
        intent.addCategory(FtIntent.CATEGORY_NOTIFICATION);
        intent.setAction(FtIntent.Actions.ResponseIntents.REQUEST_FAILED);
        intent.putExtra("sessionId", Long.valueOf((long) msg.getId()));
        intent.putExtra(FtIntent.Extras.EXTRA_INVOKING_ACTION, FtIntent.Actions.RequestIntents.TRANSFER_CANCEL);
        broadcastIntent(intent);
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        Log.i(LOG_TAG, "onMessageSendingFailed()");
        Preconditions.checkNotNull(msg, "message is null");
        Intent intent = createMessageSendingFailedIntent(msg, strategyResponse, result);
        if (msg instanceof FtMessage) {
            intent.putExtra(ImIntent.Extras.IS_FT, true);
        }
        if (msg instanceof FtHttpOutgoingMessage) {
            intent.putExtra(FtIntent.Extras.FT_SMS_DATAURL, ((FtMessage) msg).getFileDataUrl());
            intent.putExtra(FtIntent.Extras.FT_SMS_BRANDEDURL, ((FtMessage) msg).getFileBrandedUrl());
        }
        broadcastIntent(intent);
    }
}
