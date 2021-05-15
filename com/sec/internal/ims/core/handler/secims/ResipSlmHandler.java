package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.MaapNamespace;
import com.sec.internal.constants.ims.servicemodules.im.SlmMode;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmLMMIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmLMMSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmLMMSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmFileTransferParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendSlmResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.core.handler.SlmHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnRecRoute;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.FtIncomingSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmLMMInvited;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmMessageIncoming;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmProgress;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmSipResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Participant;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReportMessageHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptSlmLMMSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestCancelFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRejectSlmLMMSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImSlmMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendSlmFile;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CloseSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendSlmResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.StartSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResipSlmHandler extends SlmHandler {
    private static final int EVENT_ACCEPT_FT_SLM_MESSAGE = 2;
    private static final int EVENT_ACCEPT_SLM_LMM_SESSION = 7;
    private static final int EVENT_CANCEL_FT_SLM_MESSAGE = 3;
    private static final int EVENT_REJECT_FT_SLM_MESSAGE = 4;
    private static final int EVENT_REJECT_SLM_LMM_SESSION = 8;
    private static final int EVENT_SEND_DISPOSITION_NOTIFICATION = 6;
    private static final int EVENT_SEND_FT_SLM_MESSAGE = 5;
    private static final int EVENT_SEND_SLM_MESSAGE = 1;
    private static final int EVENT_STACK_NOTIFY = 100;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ResipSlmHandler.class.getSimpleName();
    private final ResipImdnHandler mImdnHandler;
    private final IImsFramework mImsFramework;
    private final RegistrantList mIncomingFileTransferRegistrants;
    private final RegistrantList mIncomingMessageRegistrants;
    private final RegistrantList mIncomingSlmLMMSessionRegistrants;
    private final Map<String, StandaloneMessage> mMessageSendRequests;
    private final Map<Integer, StandaloneMessage> mMessages;
    private final StackIFHandler mStackResponseHandler;
    private final RegistrantList mTransferProgressRegistrants;

    public static final class StandaloneMessage {
        public boolean isFile;
        public RejectFtSessionParams mCancelParams = null;
        public long mFileSize;
        public int mId = -1;
        public FtIncomingSession mIncomingFtSession;
        public boolean mIsChatbotMessage;
        public SlmMode mMode;
        public Integer mSessionHandle = -1;
        public Message mStatusCallback;
        public int mUaHandle;
    }

    private final class StackIFHandler extends Handler {
        StackIFHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                ResipSlmHandler.this.handleSendMessageResponse((SendSlmResponse) ((AsyncResult) msg.obj).result);
            } else if (i == 3) {
                ResipSlmHandler.this.handleCancelResponse((CloseSessionResponse) ((AsyncResult) msg.obj).result);
            } else if (i == 5) {
                ResipSlmHandler.this.handleSendFileResponse((SendSlmResponse) ((AsyncResult) msg.obj).result);
            } else if (i == 100) {
                ResipSlmHandler.this.handleNotify((Notify) ((AsyncResult) msg.obj).result);
            } else if (i == 7) {
                ResipSlmHandler.this.handleAcceptSlmLMMSessionResponse((StartSessionResponse) ((AsyncResult) msg.obj).result);
            } else if (i != 8) {
                String access$600 = ResipSlmHandler.LOG_TAG;
                Log.e(access$600, "mStackResponseHandler.handleMessage(): unhandled event - " + msg);
            } else {
                ResipSlmHandler.this.handleRejectSlmLMMSessionResponse((CloseSessionResponse) ((AsyncResult) msg.obj).result);
            }
        }
    }

    public ResipSlmHandler(Looper looper, IImsFramework imsFramework) {
        this(looper, imsFramework, new ResipImdnHandler(looper, imsFramework));
    }

    public ResipSlmHandler(Looper looper, IImsFramework imsFramework, ResipImdnHandler imdnHandler) {
        super(looper);
        this.mIncomingMessageRegistrants = new RegistrantList();
        this.mIncomingFileTransferRegistrants = new RegistrantList();
        this.mTransferProgressRegistrants = new RegistrantList();
        this.mIncomingSlmLMMSessionRegistrants = new RegistrantList();
        this.mMessages = new HashMap();
        this.mMessageSendRequests = new HashMap();
        this.mImsFramework = imsFramework;
        this.mStackResponseHandler = new StackIFHandler(looper);
        this.mImdnHandler = imdnHandler;
        StackIF.getInstance().registerSlmHandler(this.mStackResponseHandler, 100, (Object) null);
    }

    public void sendSlmDeliveredNotification(SendImdnParams params) {
        sendMessage(obtainMessage(6, params));
    }

    public void sendSlmDisplayedNotification(SendImdnParams params) {
        sendMessage(obtainMessage(6, params));
    }

    public void sendSlmMessage(SendSlmMessageParams params) {
        sendMessage(obtainMessage(1, params));
    }

    public void sendFtSlmMessage(SendSlmFileTransferParams params) {
        sendMessage(obtainMessage(5, params));
    }

    public void acceptFtSlmMessage(AcceptFtSessionParams params) {
        sendMessage(obtainMessage(2, params));
    }

    public void rejectFtSlmMessage(RejectFtSessionParams params) {
        sendMessage(obtainMessage(4, params));
    }

    public void cancelFtSlmMessage(RejectFtSessionParams params) {
        sendMessage(obtainMessage(3, params));
    }

    public void registerForSlmIncomingMessage(Handler h, int what, Object obj) {
        this.mIncomingMessageRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSlmIncomingMessage(Handler h) {
        this.mIncomingMessageRegistrants.remove(h);
    }

    public void registerForSlmIncomingFileTransfer(Handler h, int what, Object obj) {
        this.mIncomingFileTransferRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSlmIncomingFileTransfer(Handler h) {
        this.mIncomingFileTransferRegistrants.remove(h);
    }

    public void registerForSlmTransferProgress(Handler h, int what, Object obj) {
        this.mTransferProgressRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSlmTransferProgress(Handler h) {
        this.mTransferProgressRegistrants.remove(h);
    }

    public void acceptSlmLMMSession(AcceptSlmLMMSessionParams params) {
        sendMessage(obtainMessage(7, params));
    }

    public void rejectSlmLMMSession(RejectSlmLMMSessionParams rejectParams) {
        sendMessage(obtainMessage(8, rejectParams));
    }

    public void registerForSlmLMMIncomingSession(Handler h, int what, Object obj) {
        this.mIncomingSlmLMMSessionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSlmLMMIncomingSession(Handler h) {
        this.mIncomingSlmLMMSessionRegistrants.remove(h);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                handleSendMessageRequest((SendSlmMessageParams) msg.obj);
                return;
            case 2:
                handleAcceptFileRequest((AcceptFtSessionParams) msg.obj);
                return;
            case 3:
            case 4:
                handleCancelFileTransfer((RejectFtSessionParams) msg.obj);
                return;
            case 5:
                handleSendFileRequest((SendSlmFileTransferParams) msg.obj);
                return;
            case 6:
                this.mImdnHandler.sendDispositionNotification((SendImdnParams) msg.obj, 0, -1);
                return;
            case 7:
                handleAcceptSlmLMMSessionRequest((AcceptSlmLMMSessionParams) msg.obj);
                return;
            case 8:
                handleRejectSlmLMMSessionRequest((RejectSlmLMMSessionParams) msg.obj);
                return;
            default:
                Log.e(LOG_TAG, "handleMessage: Undefined message.");
                return;
        }
    }

    private void handleSendMessageRequest(SendSlmMessageParams params) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        int convIdOffset;
        int spamFromOffset;
        int headersVectorOffset;
        int devNameOffset;
        Iterator<ImsUri> it;
        String str6;
        SendSlmMessageParams sendSlmMessageParams = params;
        Log.i(LOG_TAG, "handleSendMessageRequest(): " + sendSlmMessageParams);
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        UserAgent ua = (UserAgent) rm.getUserAgentByImsi("slm", sendSlmMessageParams.mOwnImsi);
        if (ua == null) {
            Log.e(LOG_TAG, "handleSendMessageRequest(): UserAgent not found!");
            if (sendSlmMessageParams.mCallback != null) {
                sendCallback(sendSlmMessageParams.mCallback, new SendSlmResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (String) null));
                return;
            }
            return;
        }
        StandaloneMessage message = new StandaloneMessage();
        message.mId = sendSlmMessageParams.mMessageId;
        message.mStatusCallback = sendSlmMessageParams.mCallback;
        this.mMessageSendRequests.put(sendSlmMessageParams.mImdnMessageId, message);
        message.mUaHandle = ua.getHandle();
        message.mIsChatbotMessage = sendSlmMessageParams.mIsChatbotParticipant;
        if (!sendSlmMessageParams.mContentType.toLowerCase(Locale.US).contains("charset=")) {
            Log.e(LOG_TAG, "handleSendMessageRequest(): missed charset, use utf8!");
            sendSlmMessageParams.mContentType += ";charset=UTF-8";
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int[] pOffsetArr = new int[sendSlmMessageParams.mReceivers.size()];
        int uaOffset = builder.createString((CharSequence) sendSlmMessageParams.mUserAlias != null ? sendSlmMessageParams.mUserAlias : "");
        if (sendSlmMessageParams.mInReplyToContributionId != null) {
            str = sendSlmMessageParams.mInReplyToContributionId;
        } else {
            str = "";
        }
        int irtcOffset = builder.createString((CharSequence) str);
        if (sendSlmMessageParams.mContributionId != null) {
            str2 = sendSlmMessageParams.mContributionId;
        } else {
            str2 = "";
        }
        int contIdOffset = builder.createString((CharSequence) str2);
        int convIdOffset2 = builder.createString((CharSequence) parseStr(sendSlmMessageParams.mConversationId));
        if (sendSlmMessageParams.mReportMsgParams != null) {
            str3 = sendSlmMessageParams.mReportMsgParams.getSpamFrom().toString();
        } else {
            str3 = "";
        }
        int spamFromOffset2 = builder.createString((CharSequence) str3);
        if (sendSlmMessageParams.mReportMsgParams != null) {
            str4 = sendSlmMessageParams.mReportMsgParams.getSpamTo().toString();
        } else {
            str4 = "";
        }
        int spamToOffset = builder.createString((CharSequence) str4);
        if (sendSlmMessageParams.mReportMsgParams != null) {
            str5 = sendSlmMessageParams.mReportMsgParams.getSpamDate();
        } else {
            str5 = "";
        }
        int spamDOffset = builder.createString((CharSequence) str5);
        int i = 0;
        int imdnMsgIdOffset = builder.createString((CharSequence) parseStr(sendSlmMessageParams.mImdnMessageId));
        String str7 = "";
        int notiOffset = ImdnParams.createNotiVector(builder, ResipTranslatorCollection.translateFwImdnNoti(sendSlmMessageParams.mDispositionNotification));
        IRegistrationManager iRegistrationManager = rm;
        int dtOffset = builder.createString((CharSequence) Iso8601.formatMillis(sendSlmMessageParams.mImdnTime));
        StandaloneMessage standaloneMessage = message;
        int bodyOffset = builder.createString((CharSequence) parseStr(sendSlmMessageParams.mBody));
        UserAgent ua2 = ua;
        int ctOffset = builder.createString((CharSequence) parseStr(sendSlmMessageParams.mContentType));
        int devNameOffset2 = builder.createString((CharSequence) parseStr(sendSlmMessageParams.mDeviceName));
        int relMsgOffset = builder.createString((CharSequence) parseStr(sendSlmMessageParams.mReliableMessage));
        Iterator<ImsUri> it2 = sendSlmMessageParams.mReceivers.iterator();
        while (it2.hasNext()) {
            ImsUri uri = it2.next();
            if (uri != null) {
                it = it2;
                str6 = uri.toString();
            } else {
                it = it2;
                str6 = str7;
            }
            int uriOffset = builder.createString((CharSequence) str6);
            Participant.startParticipant(builder);
            Participant.addUri(builder, uriOffset);
            int i2 = uriOffset;
            if (sendSlmMessageParams.mIsBroadcastMsg != 0) {
                Participant.addCopyControl(builder, 2);
            }
            pOffsetArr[i] = Participant.endParticipant(builder);
            i++;
            it2 = it;
        }
        int pOffset = RequestSendImSlmMessage.createParticipantVector(builder, pOffsetArr);
        BaseSessionData.startBaseSessionData(builder);
        BaseSessionData.addUserAlias(builder, uaOffset);
        BaseSessionData.addInReplyToContributionId(builder, irtcOffset);
        int[] iArr = pOffsetArr;
        if (sendSlmMessageParams.mConversationId != null) {
            BaseSessionData.addConversationId(builder, convIdOffset2);
        }
        if (sendSlmMessageParams.mContributionId != null) {
            BaseSessionData.addContributionId(builder, contIdOffset);
        }
        BaseSessionData.addIsChatbotParticipant(builder, sendSlmMessageParams.mIsChatbotParticipant);
        int bsdOffset = BaseSessionData.endBaseSessionData(builder);
        ReportMessageHdr.startReportMessageHdr(builder);
        ReportMessageHdr.addSpamFrom(builder, spamFromOffset2);
        ReportMessageHdr.addSpamTo(builder, spamToOffset);
        ReportMessageHdr.addSpamDate(builder, spamDOffset);
        int i3 = spamDOffset;
        int rmhOffset = ReportMessageHdr.endReportMessageHdr(builder);
        ImdnParams.startImdnParams(builder);
        ImdnParams.addMessageId(builder, imdnMsgIdOffset);
        int imdnMsgIdOffset2 = imdnMsgIdOffset;
        if (sendSlmMessageParams.mImdnTime != null) {
            ImdnParams.addDatetime(builder, dtOffset);
        }
        ImdnParams.addNoti(builder, notiOffset);
        int imdnpOffset = ImdnParams.endImdnParams(builder);
        ArrayList<Integer> cpimNamespacesOffsetIntegers = new ArrayList<>();
        int i4 = spamToOffset;
        int notiOffset2 = notiOffset;
        if (sendSlmMessageParams.mMaapTrafficType != null) {
            int cpimNamespaceNameOffset = builder.createString((CharSequence) MaapNamespace.NAME);
            int cpimNamespaceUriOffset = builder.createString((CharSequence) MaapNamespace.URI);
            spamFromOffset = spamFromOffset2;
            int cpimNamespaceKeyOffset = builder.createString((CharSequence) "Traffic-Type");
            convIdOffset = convIdOffset2;
            int cpimNamespaceValueOffset = builder.createString((CharSequence) sendSlmMessageParams.mMaapTrafficType);
            Pair.startPair(builder);
            Pair.addKey(builder, cpimNamespaceKeyOffset);
            Pair.addValue(builder, cpimNamespaceValueOffset);
            int i5 = cpimNamespaceKeyOffset;
            int i6 = cpimNamespaceValueOffset;
            int[] headersOffset = {Pair.endPair(builder)};
            int headersVectorOffset2 = CpimNamespace.createHeadersVector(builder, headersOffset);
            CpimNamespace.startCpimNamespace(builder);
            CpimNamespace.addName(builder, cpimNamespaceNameOffset);
            CpimNamespace.addUri(builder, cpimNamespaceUriOffset);
            CpimNamespace.addHeaders(builder, headersVectorOffset2);
            int i7 = cpimNamespaceNameOffset;
            int i8 = dtOffset;
            ArrayList<Integer> cpimNamespacesOffsetIntegers2 = cpimNamespacesOffsetIntegers;
            cpimNamespacesOffsetIntegers2.add(Integer.valueOf(CpimNamespace.endCpimNamespace(builder)));
            int[] cpimNamespacesOffset = new int[cpimNamespacesOffsetIntegers2.size()];
            int i9 = cpimNamespaceUriOffset;
            int j = 0;
            while (true) {
                int[] headersOffset2 = headersOffset;
                if (j >= cpimNamespacesOffset.length) {
                    break;
                }
                cpimNamespacesOffset[j] = cpimNamespacesOffsetIntegers2.get(j).intValue();
                j++;
                headersOffset = headersOffset2;
            }
            headersVectorOffset = ImMessageParam.createCpimNamespacesVector(builder, cpimNamespacesOffset);
        } else {
            spamFromOffset = spamFromOffset2;
            convIdOffset = convIdOffset2;
            int i10 = dtOffset;
            ArrayList<Integer> arrayList = cpimNamespacesOffsetIntegers;
            headersVectorOffset = -1;
        }
        ImMessageParam.startImMessageParam(builder);
        ImMessageParam.addUserAlias(builder, uaOffset);
        ImMessageParam.addBody(builder, bodyOffset);
        int ctOffset2 = ctOffset;
        ImMessageParam.addContentType(builder, ctOffset2);
        if (sendSlmMessageParams.mDeviceName != null) {
            devNameOffset = devNameOffset2;
            ImMessageParam.addDeviceName(builder, devNameOffset);
        } else {
            devNameOffset = devNameOffset2;
        }
        if (sendSlmMessageParams.mReliableMessage != null) {
            ImMessageParam.addReliableMessage(builder, relMsgOffset);
        }
        ImMessageParam.addExtraFt(builder, sendSlmMessageParams.mExtraFt);
        ImMessageParam.addIsPublicAccountMsg(builder, sendSlmMessageParams.mIsPublicAccountMsg);
        ImMessageParam.addImdn(builder, imdnpOffset);
        if (sendSlmMessageParams.mMaapTrafficType != null) {
            ImMessageParam.addCpimNamespaces(builder, headersVectorOffset);
        }
        int immpOffset = ImMessageParam.endImMessageParam(builder);
        RequestSendImSlmMessage.startRequestSendImSlmMessage(builder);
        int imdnpOffset2 = imdnpOffset;
        RequestSendImSlmMessage.addRegistrationHandle(builder, (long) ua2.getHandle());
        RequestSendImSlmMessage.addMessageParam(builder, immpOffset);
        RequestSendImSlmMessage.addReportData(builder, rmhOffset);
        RequestSendImSlmMessage.addSessionData(builder, bsdOffset);
        RequestSendImSlmMessage.addParticipant(builder, pOffset);
        int offset = RequestSendImSlmMessage.endRequestSendImSlmMessage(builder);
        Request.startRequest(builder);
        Request.addReq(builder, offset);
        Request.addReqid(builder, Id.REQUEST_SLM_SEND_MSG);
        Request.addReqType(builder, (byte) 49);
        int i11 = imdnMsgIdOffset2;
        int imdnMsgIdOffset3 = imdnpOffset2;
        int imdnpOffset3 = i11;
        int i12 = rmhOffset;
        int i13 = immpOffset;
        int i14 = notiOffset2;
        int notiOffset3 = devNameOffset;
        int devNameOffset3 = i14;
        int i15 = spamFromOffset;
        int spamFromOffset3 = ctOffset2;
        int ctOffset3 = i15;
        int i16 = convIdOffset;
        int convIdOffset3 = headersVectorOffset;
        sendRequestToStack(Id.REQUEST_SLM_SEND_MSG, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(1), ua2);
    }

    /* access modifiers changed from: private */
    public void handleSendMessageResponse(SendSlmResponse response) {
        Log.i(LOG_TAG, "handleSendMessageResponse()");
        StandaloneMessage message = this.mMessageSendRequests.remove(response.imdnMessageId());
        if (message == null) {
            Log.e(LOG_TAG, "no message found!");
            return;
        }
        message.mMode = SlmMode.fromId((int) response.slmMode());
        message.mSessionHandle = Integer.valueOf((int) response.sessionHandle());
        Result reason = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null);
        String str = LOG_TAG;
        Log.i(str, "handleSendMessageResponse(): sessionHandle = " + message.mSessionHandle + ", result = " + reason);
        if (message.mStatusCallback == null || reason.getImError() == ImError.SUCCESS) {
            this.mMessages.put(message.mSessionHandle, message);
            return;
        }
        Log.e(LOG_TAG, "request sendMessage is failed!");
        sendCallback(message.mStatusCallback, new SendSlmResult(reason, (String) null));
        message.mStatusCallback = null;
    }

    private void handleSendFileRequest(SendSlmFileTransferParams params) {
        String str;
        String str2;
        String str3;
        int relMsgOffset;
        Iterator<ImsUri> it;
        String str4;
        SendSlmFileTransferParams sendSlmFileTransferParams = params;
        String str5 = LOG_TAG;
        Log.i(str5, "handleSendFileRequest(): " + sendSlmFileTransferParams);
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        UserAgent ua = (UserAgent) rm.getUserAgentByImsi("slm", sendSlmFileTransferParams.mOwnImsi);
        if (ua == null) {
            Log.e(LOG_TAG, "handleSendMessageRequest(): UserAgent not found!");
            if (sendSlmFileTransferParams.mCallback != null) {
                sendCallback(sendSlmFileTransferParams.mCallback, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                sendSlmFileTransferParams.mCallback = null;
                return;
            }
            return;
        }
        StandaloneMessage message = new StandaloneMessage();
        message.mId = sendSlmFileTransferParams.mMessageId;
        message.mStatusCallback = sendSlmFileTransferParams.mCallback;
        message.isFile = true;
        message.mFileSize = sendSlmFileTransferParams.mFileSize;
        this.mMessageSendRequests.put(sendSlmFileTransferParams.mImdnMsgId, message);
        message.mUaHandle = ua.getHandle();
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uaOffset = builder.createString((CharSequence) sendSlmFileTransferParams.mUserAlias != null ? sendSlmFileTransferParams.mUserAlias : "");
        if (sendSlmFileTransferParams.mConfUri != null) {
            str = sendSlmFileTransferParams.mConfUri;
        } else {
            str = "";
        }
        int sessionUriOffset = builder.createString((CharSequence) str);
        if (sendSlmFileTransferParams.mInReplyToContributionId != null) {
            str2 = sendSlmFileTransferParams.mInReplyToContributionId;
        } else {
            str2 = "";
        }
        int irtcOffset = builder.createString((CharSequence) str2);
        int sdpctOffset = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mSdpContentType));
        if (sendSlmFileTransferParams.mContributionId != null) {
            str3 = sendSlmFileTransferParams.mContributionId;
        } else {
            str3 = "";
        }
        int contIdOffset = builder.createString((CharSequence) str3);
        int convIdOffset = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mConversationId));
        int fnameOffset = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mFileName));
        int pathOffset = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mFilePath));
        int fctOffset = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mContentType));
        String str6 = "";
        int imdnMsgIdOffset = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mImdnMsgId));
        IRegistrationManager iRegistrationManager = rm;
        int notiOffset = ImdnParams.createNotiVector(builder, ResipTranslatorCollection.translateFwImdnNoti(sendSlmFileTransferParams.mDispositionNotification));
        StandaloneMessage standaloneMessage = message;
        int devNameOffset = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mDeviceName));
        UserAgent ua2 = ua;
        int relMsgOffset2 = builder.createString((CharSequence) parseStr(sendSlmFileTransferParams.mReliableMessage));
        int[] pOffsetArr = new int[sendSlmFileTransferParams.mRecipients.size()];
        int i = 0;
        int relMsgOffset3 = relMsgOffset2;
        Iterator<ImsUri> it2 = sendSlmFileTransferParams.mRecipients.iterator();
        while (it2.hasNext()) {
            ImsUri uri = it2.next();
            if (uri != null) {
                it = it2;
                str4 = uri.toString();
            } else {
                it = it2;
                str4 = str6;
            }
            int uriOffset = builder.createString((CharSequence) str4);
            Participant.startParticipant(builder);
            Participant.addUri(builder, uriOffset);
            int i2 = uriOffset;
            if (sendSlmFileTransferParams.mIsBroadcastMsg != 0) {
                Participant.addCopyControl(builder, 2);
            }
            pOffsetArr[i] = Participant.endParticipant(builder);
            i++;
            it2 = it;
        }
        int pOffset = RequestSendImSlmMessage.createParticipantVector(builder, pOffsetArr);
        BaseSessionData.startBaseSessionData(builder);
        BaseSessionData.addUserAlias(builder, uaOffset);
        BaseSessionData.addSessionUri(builder, sessionUriOffset);
        BaseSessionData.addInReplyToContributionId(builder, irtcOffset);
        BaseSessionData.addSdpContentType(builder, sdpctOffset);
        int[] iArr = pOffsetArr;
        if (sendSlmFileTransferParams.mContributionId != null) {
            BaseSessionData.addContributionId(builder, contIdOffset);
        }
        if (sendSlmFileTransferParams.mConversationId != null) {
            BaseSessionData.addConversationId(builder, convIdOffset);
        }
        int bsdOffset = BaseSessionData.endBaseSessionData(builder);
        ImFileAttr.startImFileAttr(builder);
        ImFileAttr.addName(builder, fnameOffset);
        ImFileAttr.addPath(builder, pathOffset);
        ImFileAttr.addContentType(builder, fctOffset);
        int i3 = pathOffset;
        int i4 = fnameOffset;
        ImFileAttr.addSize(builder, (long) ((int) sendSlmFileTransferParams.mFileSize));
        int fileOffset = ImFileAttr.endImFileAttr(builder);
        ImdnParams.startImdnParams(builder);
        ImdnParams.addMessageId(builder, imdnMsgIdOffset);
        ImdnParams.addNoti(builder, notiOffset);
        int imdnpOffset = ImdnParams.endImdnParams(builder);
        FtPayloadParam.startFtPayloadParam(builder);
        FtPayloadParam.addImdn(builder, imdnpOffset);
        FtPayloadParam.addFileAttr(builder, fileOffset);
        int fctOffset2 = fctOffset;
        if (sendSlmFileTransferParams.mDeviceName != null) {
            FtPayloadParam.addDeviceName(builder, devNameOffset);
        }
        if (sendSlmFileTransferParams.mReliableMessage != null) {
            relMsgOffset = relMsgOffset3;
            FtPayloadParam.addReliableMessage(builder, relMsgOffset);
        } else {
            relMsgOffset = relMsgOffset3;
        }
        int relMsgOffset4 = relMsgOffset;
        FtPayloadParam.addExtraFt(builder, sendSlmFileTransferParams.mExtraFt);
        int ftpOffset = FtPayloadParam.endFtPayloadParam(builder);
        RequestSendSlmFile.startRequestSendSlmFile(builder);
        int i5 = imdnMsgIdOffset;
        int i6 = imdnpOffset;
        RequestSendSlmFile.addRegistrationHandle(builder, (long) ua2.getHandle());
        RequestSendSlmFile.addParticipant(builder, pOffset);
        RequestSendSlmFile.addPayloadParam(builder, ftpOffset);
        RequestSendSlmFile.addSessionData(builder, bsdOffset);
        int offset = RequestSendSlmFile.endRequestSendSlmFile(builder);
        Request.startRequest(builder);
        Request.addReq(builder, offset);
        Request.addReqid(builder, Id.REQUEST_SLM_SEND_FILE);
        Request.addReqType(builder, (byte) 50);
        int i7 = ftpOffset;
        int i8 = fctOffset2;
        int fctOffset3 = relMsgOffset4;
        int relMsgOffset5 = i8;
        int i9 = fileOffset;
        int i10 = convIdOffset;
        int i11 = contIdOffset;
        sendRequestToStack(Id.REQUEST_SLM_SEND_FILE, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(5), ua2);
    }

    /* access modifiers changed from: private */
    public void handleSendFileResponse(SendSlmResponse response) {
        Log.i(LOG_TAG, "handleSendFileResponse()");
        StandaloneMessage message = this.mMessageSendRequests.remove(response.imdnMessageId());
        if (message == null) {
            Log.e(LOG_TAG, "handleSendFileResponse(): no StandaloneMessage found!");
            return;
        }
        message.mMode = SlmMode.fromId((int) response.slmMode());
        if (message.mCancelParams != null) {
            Log.i(LOG_TAG, "handleSendFileResponse(): send pending cancel request");
            sendCancelRequestToStack(message);
            return;
        }
        message.mSessionHandle = Integer.valueOf((int) response.sessionHandle());
        Result reason = ResipTranslatorCollection.translateFtResult(response.imError(), (Object) null);
        String str = LOG_TAG;
        Log.i(str, "handleSendFileResponse(): sessionHandle = " + message.mSessionHandle + ", result = " + reason);
        if (message.mStatusCallback != null) {
            sendCallback(message.mStatusCallback, new FtResult(reason, message.mSessionHandle));
            message.mStatusCallback = null;
        }
        if (reason.getImError() != ImError.SUCCESS) {
            Log.e(LOG_TAG, "request sendFile is failed!");
            return;
        }
        this.mMessages.put(message.mSessionHandle, message);
        if (message.mMode == SlmMode.PAGER) {
            AsyncResult result = new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf((int) response.sessionHandle()), message.mId, message.mFileSize, 0, FtTransferProgressEvent.State.TRANSFERRING, reason), (Throwable) null);
            if (this.mTransferProgressRegistrants.size() != 0) {
                this.mTransferProgressRegistrants.notifyRegistrants(result);
            } else {
                Log.e(LOG_TAG, "handleSendFileResponse(): no listener!");
            }
        }
    }

    private void handleAcceptFileRequest(AcceptFtSessionParams params) {
        AcceptFtSessionParams acceptFtSessionParams = params;
        Log.i(LOG_TAG, "handleAcceptFileRequest(): " + acceptFtSessionParams);
        int sessionHandle = ((Integer) acceptFtSessionParams.mRawHandle).intValue();
        if (acceptFtSessionParams.mCallback != null) {
            sendCallback(acceptFtSessionParams.mCallback, new FtResult(ImError.SUCCESS, Result.Type.SUCCESS, (Object) Integer.valueOf(sessionHandle)));
            acceptFtSessionParams.mCallback = null;
        }
        StandaloneMessage session = this.mMessages.remove(Integer.valueOf(sessionHandle));
        if (session == null) {
            Log.e(LOG_TAG, "handleAcceptFileRequest(): session not found!");
            if (this.mTransferProgressRegistrants.size() != 0) {
                RegistrantList registrantList = this.mTransferProgressRegistrants;
                FtTransferProgressEvent ftTransferProgressEvent = r7;
                FtTransferProgressEvent ftTransferProgressEvent2 = new FtTransferProgressEvent(Integer.valueOf(sessionHandle), acceptFtSessionParams.mMessageId, 0, 0, FtTransferProgressEvent.State.CANCELED, (Result) null);
                registrantList.notifyRegistrants(new AsyncResult((Object) null, ftTransferProgressEvent, (Throwable) null));
                return;
            }
            Log.e(LOG_TAG, "handleAcceptFileRequest(): no listener!");
            return;
        }
        FtPayloadParam ftPayload = session.mIncomingFtSession.payload();
        if (ftPayload == null) {
            Log.e(LOG_TAG, "handleAcceptFileRequest(): ftpayload is null");
            return;
        }
        ImFileAttr fileAttr = ftPayload.fileAttr();
        if (fileAttr == null) {
            Log.e(LOG_TAG, "handleAcceptFileRequest(): fileAttr is null");
            return;
        }
        String filePath = fileAttr.path();
        String contentType = fileAttr.contentType();
        if (filePath == null) {
            Log.e(LOG_TAG, "handleAcceptFileRequest(): file info is null");
            return;
        }
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (contentType != null && mimeTypeMap.hasMimeType(contentType)) {
            filePath = filePath + "." + mimeTypeMap.getExtensionFromMimeType(contentType);
        }
        File in = new File(filePath);
        boolean isCopied = FileUtils.copyFile(in, new File(acceptFtSessionParams.mFilePath));
        in.delete();
        if (this.mTransferProgressRegistrants.size() != 0) {
            this.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf(sessionHandle), acceptFtSessionParams.mMessageId, fileAttr.size(), fileAttr.size(), isCopied ? FtTransferProgressEvent.State.COMPLETED : FtTransferProgressEvent.State.CANCELED, (Result) null), (Throwable) null));
            return;
        }
        Log.e(LOG_TAG, "handleAcceptFileRequest(): no listener!");
    }

    private void handleAcceptSlmLMMSessionRequest(AcceptSlmLMMSessionParams params) {
        String str = LOG_TAG;
        IMSLog.s(str, "handleAcceptSlmLMMSessionRequest(): params " + params);
        int sessionHandle = ((Integer) params.mRawHandle).intValue();
        UserAgent ua = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("slm", params.mOwnImsi);
        if (ua == null) {
            Log.e(LOG_TAG, "handleAcceptSlmLMMSessionRequest(): User agent not found!");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, ImError.ENGINE_ERROR);
                params.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uaOffset = builder.createString((CharSequence) params.mUserAlias == null ? "" : params.mUserAlias);
        RequestAcceptSlmLMMSession.startRequestAcceptSlmLMMSession(builder);
        RequestAcceptSlmLMMSession.addSessionId(builder, (long) sessionHandle);
        RequestAcceptSlmLMMSession.addUserAlias(builder, uaOffset);
        int offset = RequestAcceptSlmLMMSession.endRequestAcceptSlmLMMSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_ACCEPT_SLM_LMM_SESSION);
        Request.addReqType(builder, (byte) 57);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_ACCEPT_SLM_LMM_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(7), ua);
    }

    private void handleRejectSlmLMMSessionRequest(RejectSlmLMMSessionParams rejectParams) {
        String str = LOG_TAG;
        IMSLog.s(str, "handleRejectSlmLMMSessionRequest: " + rejectParams);
        int sessionHandle = ((Integer) rejectParams.mRawHandle).intValue();
        UserAgent ua = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("slm", rejectParams.mOwnImsi);
        if (ua == null) {
            Log.e(LOG_TAG, "handleRejectSlmLMMSessionRequest(): User agent not found!");
            if (rejectParams.mCallback != null) {
                sendCallback(rejectParams.mCallback, ImError.ENGINE_ERROR);
                rejectParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int wTxtOffset = builder.createString((CharSequence) rejectParams.mSessionRejectReason != null ? rejectParams.mSessionRejectReason.getWarningText() : "");
        WarningHdr.startWarningHdr(builder);
        if (rejectParams.mSessionRejectReason != null) {
            WarningHdr.addCode(builder, rejectParams.mSessionRejectReason.getWarningCode());
            WarningHdr.addText(builder, wTxtOffset);
        }
        int whOffset = WarningHdr.endWarningHdr(builder);
        RequestRejectSlmLMMSession.startRequestRejectSlmLMMSession(builder);
        RequestRejectSlmLMMSession.addSessionHandle(builder, (long) sessionHandle);
        if (rejectParams.mSessionRejectReason != null) {
            RequestRejectSlmLMMSession.addSipCode(builder, (long) rejectParams.mSessionRejectReason.getSipCode());
        }
        RequestRejectSlmLMMSession.addWarningHdr(builder, whOffset);
        int offset = RequestRejectSlmLMMSession.endRequestRejectSlmLMMSession(builder);
        Request.startRequest(builder);
        Request.addReq(builder, offset);
        Request.addReqid(builder, Id.REQUEST_REJECT_SLM_LMM_SESSION);
        Request.addReqType(builder, (byte) 58);
        sendRequestToStack(Id.REQUEST_REJECT_SLM_LMM_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(8), ua);
    }

    /* access modifiers changed from: private */
    public void handleAcceptSlmLMMSessionResponse(StartSessionResponse response) {
        ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
        String str = LOG_TAG;
        Log.e(str, "handleAcceptSlmLMMSessionResponse() sessionHandle = " + ((int) response.sessionHandle()) + ", error = " + imError);
    }

    /* access modifiers changed from: private */
    public void handleRejectSlmLMMSessionResponse(CloseSessionResponse response) {
        ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
        String str = LOG_TAG;
        Log.e(str, "handleRejectSlmLMMSessionResponse() sessionHandle = " + ((int) response.sessionHandle()) + ", error = " + imError);
    }

    private void handleIncomingSlmMessageNotify(Notify notify) {
        List<Integer> notiList;
        Log.i(LOG_TAG, "handleIncomingSlmMessageNotify()");
        boolean isTokenUsed = false;
        if (notify.notiType() != 69) {
            Log.e(LOG_TAG, "handleIncomingSlmMessageNotify(): invalid notify!");
            return;
        }
        SlmMessageIncoming noti = (SlmMessageIncoming) notify.noti(new SlmMessageIncoming());
        ImMessageParam message = noti.msg();
        BaseSessionData baseSessionData = noti.sessionData();
        if (message == null || baseSessionData == null) {
            Log.e(LOG_TAG, "handleIncomingSlmMessageNotify(): invalid data.");
            return;
        }
        SlmIncomingMessageEvent event = new SlmIncomingMessageEvent();
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        UserAgent ua = (UserAgent) rm.getUserAgent((int) noti.userHandle());
        if (ua == null) {
            Log.e(LOG_TAG, "handleIncomingSlmMessageNotify(): User agent not found!");
            return;
        }
        event.mOwnImsi = rm.getImsiByUserAgent(ua);
        if (message.sender() == null) {
            String str = LOG_TAG;
            Log.i(str, "Invalid sender uri, return. uri=" + message.sender());
            return;
        }
        event.mSender = ImsUri.parse(message.sender());
        if (event.mSender == null) {
            String str2 = LOG_TAG;
            Log.i(str2, "Invalid sender uri, return. uri=" + message.sender());
            return;
        }
        event.mUserAlias = message.userAlias();
        event.mIsPublicAccountMsg = message.isPublicAccountMsg();
        event.mParticipants = new ArrayList();
        for (int i = 0; i < baseSessionData.receiversLength(); i++) {
            event.mParticipants.add(ImsUri.parse(baseSessionData.receivers(i)));
        }
        event.mContentType = message.contentType();
        event.mBody = ResipTranslatorCollection.adjustMessageBody(message.body(), event.mContentType);
        if (event.mBody != null) {
            event.mIsRoutingMsg = message.silenceSupported();
            if (event.mIsRoutingMsg) {
                Log.i(LOG_TAG, "handleIncomingSlmMessageNotify -> routing message");
                event.mRequestUri = ImsUri.parse(message.requestUri());
                event.mPAssertedId = ImsUri.parse(message.pAssertedId());
                event.mReceiver = ImsUri.parse(message.receiver());
            }
            if (!(message.imdn() == null || message.imdn().messageId() == null)) {
                event.mImdnMessageId = message.imdn().messageId();
            }
            if (!(message.imdn() == null || message.imdn().originalToHdr() == null)) {
                event.mOriginalToHdr = message.imdn().originalToHdr();
            }
            try {
                event.mImdnTime = (message.imdn() == null || message.imdn().datetime() == null) ? null : Iso8601.parse(message.imdn().datetime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<Integer> notiList2 = new ArrayList<>();
            if (message.imdn() != null) {
                for (int i2 = 0; i2 < message.imdn().notiLength(); i2++) {
                    notiList2.add(Integer.valueOf(message.imdn().noti(i2)));
                }
            }
            event.mDispositionNotification = ResipTranslatorCollection.translateStackImdnNoti(notiList2);
            event.mContributionId = baseSessionData.contributionId();
            event.mConversationId = baseSessionData.conversationId();
            if (!TextUtils.isEmpty(message.deviceName())) {
                event.mDeviceName = message.deviceName();
            }
            if (!TextUtils.isEmpty(message.reliableMessage())) {
                event.mReliableMessage = message.reliableMessage();
            }
            event.mExtraFt = message.extraFt();
            if (message.imdn() == null || message.imdn().recRouteLength() <= 0) {
            } else {
                event.mImdnRecRouteList = new ArrayList();
                int i3 = 0;
                while (i3 < message.imdn().recRouteLength()) {
                    ImdnRecRoute route = message.imdn().recRoute(i3);
                    if (route != null) {
                        String str3 = LOG_TAG;
                        Log.i(str3, "imdn route: " + route.uri());
                        notiList = notiList2;
                        event.mImdnRecRouteList.add(new ImImdnRecRoute(event.mImdnMessageId, route.uri(), route.name()));
                    } else {
                        notiList = notiList2;
                    }
                    i3++;
                    notiList2 = notiList;
                }
            }
            event.mCpimNamespaces = new ImCpimNamespaces();
            for (int i4 = 0; i4 < message.cpimNamespacesLength(); i4++) {
                CpimNamespace nameSpace = message.cpimNamespaces(i4);
                if (nameSpace != null) {
                    event.mCpimNamespaces.addNamespace(nameSpace.name(), nameSpace.uri());
                    for (int j = 0; j < nameSpace.headersLength(); j++) {
                        Pair header = nameSpace.headers(j);
                        if (header != null) {
                            event.mCpimNamespaces.getNamespace(nameSpace.name()).addHeader(header.key(), header.value());
                        }
                    }
                }
            }
            String[] email = parseEmailOverSlm(event.mSender, event.mBody);
            if (email != null) {
                event.mSender = ImsUri.parse("sip:" + email[0]);
                event.mUserAlias = "";
                event.mBody = email[1];
            }
            if (noti.extension() != null) {
                event.mImExtensionMNOHeaders = ResipTranslatorCollection.translateFwImExtensionHeaders(noti.extension());
            }
            event.mIsLMM = noti.isLmm();
            event.mIsChatbotRole = baseSessionData.isChatbotParticipant();
            if (!(!event.mIsChatbotRole || event.mSender == null || event.mSender.getParam("tk") == null)) {
                if (event.mSender.getParam("tk").equals("on")) {
                    isTokenUsed = true;
                }
                event.mSender.removeParam("tk");
            }
            event.mIsTokenUsed = isTokenUsed;
            String str4 = LOG_TAG;
            Log.i(str4, "handleIncomingSlmMessageNotify(): " + event);
            RegistrantList registrantList = this.mIncomingMessageRegistrants;
            if (registrantList != null) {
                registrantList.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
            } else {
                Log.e(LOG_TAG, "handleIncomingSlmMessageNotify(): no listener!");
            }
        }
    }

    private void handleIncomingSlmFileNotify(Notify notify) {
        BaseSessionData baseSessionData;
        FtIncomingSession noti;
        List<Integer> notiList;
        Log.i(LOG_TAG, "handleIncomingSlmFileNotify()");
        if (notify.notiType() != 41) {
            Log.e(LOG_TAG, "handleIncomingSlmFileNotify(): invalid notify");
            return;
        }
        FtIncomingSession noti2 = (FtIncomingSession) notify.noti(new FtIncomingSession());
        BaseSessionData baseSessionData2 = noti2.session();
        FtPayloadParam ftPayload = noti2.payload();
        if (baseSessionData2 == null) {
            BaseSessionData baseSessionData3 = baseSessionData2;
        } else if (ftPayload == null) {
            FtIncomingSession ftIncomingSession = noti2;
            BaseSessionData baseSessionData4 = baseSessionData2;
        } else {
            ImFileAttr fileAttr = ftPayload.fileAttr();
            if (fileAttr == null) {
                Log.i(LOG_TAG, "handleIncomingSlmFileNotify(): fileAttr is null");
                return;
            }
            IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
            UserAgent ua = (UserAgent) rm.getUserAgent((int) noti2.userHandle());
            if (ua == null) {
                Log.e(LOG_TAG, "handleIncomingSlmFileNotify(): User agent not found!");
                return;
            }
            int sessionHandle = (int) baseSessionData2.sessionHandle();
            StandaloneMessage session = new StandaloneMessage();
            session.mSessionHandle = Integer.valueOf(sessionHandle);
            session.mIncomingFtSession = noti2;
            this.mMessages.put(Integer.valueOf(sessionHandle), session);
            session.mUaHandle = (int) noti2.userHandle();
            FtIncomingSessionEvent event = new FtIncomingSessionEvent();
            event.mRawHandle = Integer.valueOf(sessionHandle);
            event.mIsSlmSvcMsg = true;
            event.mIsLMM = noti2.isLmm();
            event.mOwnImsi = rm.getImsiByUserAgent(ua);
            event.mSenderUri = ImsUri.parse(baseSessionData2.sessionUri());
            event.mUserAlias = baseSessionData2.userAlias();
            event.mParticipants = new ArrayList();
            for (int i = 0; i < baseSessionData2.receiversLength(); i++) {
                ImsUri parti = ImsUri.parse(baseSessionData2.receivers(i));
                if (parti == null) {
                    Log.e(LOG_TAG, "participant has Wrong Uri.");
                } else {
                    event.mParticipants.add(parti);
                }
            }
            event.mContentType = fileAttr.contentType();
            event.mSdpContentType = baseSessionData2.sdpContentType();
            if (!TextUtils.isEmpty(ftPayload.deviceName())) {
                event.mDeviceName = ftPayload.deviceName();
            }
            if (!TextUtils.isEmpty(ftPayload.reliableMessage())) {
                event.mReliableMessage = ftPayload.reliableMessage();
            }
            event.mExtraFt = ftPayload.extraFt();
            event.mFileName = fileAttr.name();
            event.mFilePath = fileAttr.path();
            event.mFileSize = fileAttr.size();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            if (mimeTypeMap.hasMimeType(event.mContentType)) {
                File from = new File(event.mFilePath);
                event.mFilePath += "." + mimeTypeMap.getExtensionFromMimeType(event.mContentType);
                File to = new File(event.mFilePath);
                if (!from.exists()) {
                    Log.e(LOG_TAG, "handleIncomingSlmFileNotify(): file doesn't exist! " + from.getPath());
                } else if (!from.renameTo(to)) {
                    Log.e(LOG_TAG, "handleIncomingSlmFileNotify(): failed to rename! " + to.getPath());
                }
            }
            event.mContributionId = baseSessionData2.contributionId();
            if (baseSessionData2.conversationId() != null) {
                event.mConversationId = baseSessionData2.conversationId();
            }
            if (baseSessionData2.inReplyToContributionId() != null) {
                event.mInReplyToConversationId = baseSessionData2.inReplyToContributionId();
            }
            event.mStart = (int) fileAttr.start();
            event.mEnd = (int) fileAttr.end();
            event.mPush = ftPayload.isPush();
            if (ftPayload.imdn() != null) {
                event.mImdnId = ftPayload.imdn().messageId();
                List<Integer> notiList2 = new ArrayList<>();
                for (int i2 = 0; i2 < ftPayload.imdn().notiLength(); i2++) {
                    notiList2.add(Integer.valueOf(ftPayload.imdn().noti(i2)));
                }
                event.mDisposition = ResipTranslatorCollection.translateStackImdnNoti(notiList2);
                event.mDeviceId = ftPayload.imdn().deviceId();
                event.mOriginalToHdr = ftPayload.imdn().originalToHdr();
                event.mRecRouteList = new ArrayList();
                int i3 = 0;
                while (i3 < ftPayload.imdn().recRouteLength()) {
                    ImdnRecRoute route = ftPayload.imdn().recRoute(i3);
                    if (route != null) {
                        notiList = notiList2;
                        noti = noti2;
                        baseSessionData = baseSessionData2;
                        event.mRecRouteList.add(new ImImdnRecRoute(event.mImdnId, route.uri(), route.name()));
                    } else {
                        notiList = notiList2;
                        noti = noti2;
                        baseSessionData = baseSessionData2;
                    }
                    i3++;
                    notiList2 = notiList;
                    noti2 = noti;
                    baseSessionData2 = baseSessionData;
                }
                FtIncomingSession ftIncomingSession2 = noti2;
                BaseSessionData baseSessionData5 = baseSessionData2;
                try {
                    event.mImdnTime = ftPayload.imdn().datetime() != null ? Iso8601.parse(ftPayload.imdn().datetime()) : new Date();
                } catch (ParseException e) {
                    e.printStackTrace();
                    event.mImdnTime = new Date();
                }
            } else {
                BaseSessionData baseSessionData6 = baseSessionData2;
            }
            event.mCpimNamespaces = new ImCpimNamespaces();
            for (int i4 = 0; i4 < ftPayload.cpimNamespacesLength(); i4++) {
                CpimNamespace protoNamespace = ftPayload.cpimNamespaces(i4);
                if (protoNamespace != null) {
                    event.mCpimNamespaces.addNamespace(protoNamespace.name(), protoNamespace.uri());
                    for (int j = 0; j < protoNamespace.headersLength(); j++) {
                        Pair header = protoNamespace.headers(j);
                        if (header != null) {
                            event.mCpimNamespaces.getNamespace(protoNamespace.name()).addHeader(header.key(), header.value());
                        }
                    }
                }
            }
            Log.i(LOG_TAG, "handleIncomingSlmFileNotify(): " + event);
            RegistrantList registrantList = this.mIncomingFileTransferRegistrants;
            if (registrantList != null) {
                registrantList.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
                return;
            } else {
                Log.e(LOG_TAG, "handleIncomingSlmFileNotify(): no listener!");
                return;
            }
        }
        Log.i(LOG_TAG, "handleIncomingSlmFileNotify(): invalid data");
    }

    private void handleSlmProgress(Notify notify) {
        if (notify.notiType() != 38) {
            Log.e(LOG_TAG, "handleSlmProgress(): invalid notify");
            return;
        }
        SlmProgress noti = (SlmProgress) notify.noti(new SlmProgress());
        String str = LOG_TAG;
        Log.i(str, "handleSlmProgress(): total = " + noti.total() + ", transferred = " + noti.transferred() + ", imdnMessageId = " + noti.imdnMessageId() + ", sessionHandle = " + noti.sessionHandle());
        StandaloneMessage message = this.mMessages.get(Integer.valueOf((int) noti.sessionHandle()));
        FtTransferProgressEvent.State state = ResipTranslatorCollection.translateFtProgressState((int) noti.state());
        if (message == null) {
            Log.e(LOG_TAG, "handleSlmProgress(): no StandaloneMessage found!");
            return;
        }
        if (state != FtTransferProgressEvent.State.TRANSFERRING) {
            this.mMessages.remove(message.mSessionHandle);
        }
        if (message.isFile) {
            AsyncResult result = new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf((int) noti.sessionHandle()), message.mId, noti.total(), noti.transferred(), state, ResipTranslatorCollection.translateFtResult(noti.imError(), (Object) null)), (Throwable) null);
            if (this.mTransferProgressRegistrants.size() != 0) {
                this.mTransferProgressRegistrants.notifyRegistrants(result);
            } else {
                Log.e(LOG_TAG, "handleSlmProgress(): no listener!");
            }
        } else {
            Result result2 = ResipTranslatorCollection.translateImResult(noti.imError(), (Object) null);
            if (state == FtTransferProgressEvent.State.COMPLETED || result2.getImError() != ImError.SUCCESS) {
                sendCallback(message.mStatusCallback, new SendSlmResult(result2, (String) null));
                message.mStatusCallback = null;
            }
        }
    }

    private void handleSlmSipResponseReceived(Notify notify) {
        Log.i(LOG_TAG, "HandleSlmSipResponseReceived() Enter");
        if (notify.notiType() != 37) {
            Log.e(LOG_TAG, "handlSlmSipResponseReceived(): invalid notify");
            return;
        }
        SlmSipResponseReceived noti = (SlmSipResponseReceived) notify.noti(new SlmSipResponseReceived());
        StandaloneMessage message = this.mMessages.get(Integer.valueOf((int) noti.sessionHandle()));
        if (message == null) {
            String str = LOG_TAG;
            Log.e(str, "handleSlmSipResponseReceived(): no StandaloneMessage found!, ImdnMessageId : " + noti.imdnMessageId() + ", SessionHandle : " + noti.sessionHandle());
        } else if (message.isFile && message.mMode == SlmMode.PAGER) {
            Result reason = ResipTranslatorCollection.translateFtResult(noti.imError(), noti.warningHdr());
            AsyncResult result = new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf((int) noti.sessionHandle()), message.mId, message.mFileSize, reason.getImError() == ImError.SUCCESS ? message.mFileSize : 0, reason.getImError() == ImError.SUCCESS ? FtTransferProgressEvent.State.COMPLETED : FtTransferProgressEvent.State.CANCELED, reason), (Throwable) null);
            if (this.mTransferProgressRegistrants.size() != 0) {
                this.mTransferProgressRegistrants.notifyRegistrants(result);
            } else {
                Log.e(LOG_TAG, "handleSlmSipResponseReceived(): no listener!");
            }
            this.mMessages.remove(message.mSessionHandle);
        } else if (message.mMode == SlmMode.PAGER && !message.isFile) {
            Result result2 = ResipTranslatorCollection.translateImResult(noti.imError(), noti.warningHdr());
            String str2 = LOG_TAG;
            Log.i(str2, "handleSlmSipResponseReceived(), result= " + result2);
            sendCallback(message.mStatusCallback, new SendSlmResult(result2, noti.passertedId()));
            message.mStatusCallback = null;
            this.mMessages.remove(message.mSessionHandle);
        } else if (message.mMode == SlmMode.LARGE_MESSAGE && message.isFile) {
            Result reason2 = ResipTranslatorCollection.translateFtResult(noti.imError(), noti.warningHdr());
            if (reason2.getImError() != ImError.SUCCESS) {
                AsyncResult result3 = new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf((int) noti.sessionHandle()), message.mId, 0, 0, FtTransferProgressEvent.State.CANCELED, reason2), (Throwable) null);
                this.mMessages.remove(message.mSessionHandle);
                if (this.mTransferProgressRegistrants.size() != 0) {
                    this.mTransferProgressRegistrants.notifyRegistrants(result3);
                } else {
                    Log.e(LOG_TAG, "handlSlmSipResponseReceived(): no listener!");
                }
            }
        } else if (message.mMode == SlmMode.LARGE_MESSAGE && !message.isFile) {
            Result result4 = ResipTranslatorCollection.translateImResult(noti.imError(), noti.warningHdr());
            if (result4.getImError() != ImError.SUCCESS) {
                String str3 = LOG_TAG;
                Log.e(str3, "handleSlmSipResponseReceived(): SipResponse is not 200 OK, result= " + result4);
                sendCallback(message.mStatusCallback, new SendSlmResult(result4, (String) null));
                message.mStatusCallback = null;
                this.mMessages.remove(message.mSessionHandle);
            }
        }
    }

    private void handleSlmLMMIncomingSession(Notify notify) {
        Log.i(LOG_TAG, "handleSlmLMMIncomingSession()");
        if (notify.notiType() != 39) {
            Log.e(LOG_TAG, "handleSlmLMMIncomingSession(): invalid notify");
            return;
        }
        SlmLMMInvited invite = (SlmLMMInvited) notify.noti(new SlmLMMInvited());
        IRegistrationManager rm = this.mImsFramework.getRegistrationManager();
        UserAgent ua = (UserAgent) rm.getUserAgent((int) invite.userHandle());
        if (ua == null) {
            Log.e(LOG_TAG, "handleSlmLMMIncomingSession(): UserAgent not found.");
            return;
        }
        SlmLMMIncomingSessionEvent event = new SlmLMMIncomingSessionEvent();
        Integer sessionHandle = Integer.valueOf((int) invite.sessionHandle());
        event.mRawHandle = sessionHandle;
        event.mInitiator = ImsUri.parse(invite.sender());
        event.mInitiatorAlias = invite.userAlias();
        event.mOwnImsi = rm.getImsiByUserAgent(ua);
        AsyncResult result = new AsyncResult((Object) null, event, (Throwable) null);
        if (this.mIncomingSlmLMMSessionRegistrants.size() != 0) {
            this.mIncomingSlmLMMSessionRegistrants.notifyRegistrants(result);
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "handleSlmLMMIncomingSession(): Empty registrants, reject handle=" + sessionHandle);
        handleRejectSlmLMMSessionRequest(new RejectSlmLMMSessionParams((String) null, sessionHandle, ImSessionRejectReason.BUSY_HERE, (Message) null, rm.getImsiByUserAgent(ua)));
    }

    private void handleCancelFileTransfer(RejectFtSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "handleCancelFileTransfer(): " + params);
        if (params.mRawHandle == null) {
            Log.i(LOG_TAG, "handleCancelFileTransfer: params.mRawHandle is null!");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, new FtResult(ImError.UNKNOWN_ERROR, Result.Type.UNKNOWN_ERROR, (Object) null));
                params.mCallback = null;
                return;
            }
            return;
        }
        int sessionHandle = ((Integer) params.mRawHandle).intValue();
        StandaloneMessage session = null;
        if (params.mImdnMessageId != null) {
            session = this.mMessageSendRequests.get(params.mImdnMessageId);
        }
        if (session != null) {
            Log.i(LOG_TAG, "handleCancelFileTransfer(): pending message - postpone");
            session.mCancelParams = params;
            return;
        }
        StandaloneMessage session2 = this.mMessages.get(Integer.valueOf(sessionHandle));
        if (session2 == null) {
            Log.e(LOG_TAG, "handleCancelFileTransfer(): unknown session!");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, new FtResult(ImError.ENGINE_ERROR, Result.Type.UNKNOWN_ERROR, (Object) Integer.valueOf(sessionHandle)));
                params.mCallback = null;
            }
        } else if (session2.mCancelParams != null) {
            Log.e(LOG_TAG, "handleCancelFileTransfer(): cancel already in progress!");
        } else {
            session2.mCancelParams = params;
            sendCancelRequestToStack(session2);
        }
    }

    private void sendCancelRequestToStack(StandaloneMessage session) {
        String str = LOG_TAG;
        Log.i(str, "sendCancelRequestToStack(): session handle = " + session.mSessionHandle);
        UserAgent ua = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(LOG_TAG, "sendCancelRequestToStack(): UserAgent not found.");
            if (session.mStatusCallback != null) {
                sendCallback(session.mStatusCallback, new FtResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (Object) null));
                session.mStatusCallback = null;
                return;
            }
            return;
        }
        RejectFtSessionParams params = session.mCancelParams;
        if (params == null) {
            Log.e(LOG_TAG, "sendCancelRequestToStack(): null reject params!");
            return;
        }
        FtRejectReason reason = params.mRejectReason != null ? params.mRejectReason : FtRejectReason.DECLINE;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int wTxtOffset = builder.createString((CharSequence) reason.getWarningText());
        WarningHdr.startWarningHdr(builder);
        WarningHdr.addCode(builder, reason.getWarningCode());
        WarningHdr.addText(builder, wTxtOffset);
        int whOffset = WarningHdr.endWarningHdr(builder);
        RequestCancelFtSession.startRequestCancelFtSession(builder);
        RequestCancelFtSession.addSipCode(builder, reason.getSipCode());
        RequestCancelFtSession.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
        RequestCancelFtSession.addWarningHdr(builder, whOffset);
        int offset = RequestCancelFtSession.endRequestCancelFtSession(builder);
        Request.startRequest(builder);
        Request.addReq(builder, offset);
        Request.addReqid(builder, Id.REQUEST_SLM_CANCEL_SESSION);
        Request.addReqType(builder, (byte) 47);
        sendRequestToStack(Id.REQUEST_SLM_CANCEL_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(3), ua);
    }

    /* access modifiers changed from: private */
    public void handleCancelResponse(CloseSessionResponse response) {
        int sessionHandle = (int) response.sessionHandle();
        Result reason = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null);
        String str = LOG_TAG;
        Log.i(str, "handleCancelResponse(): sessionHandle = " + sessionHandle + ", error = " + reason);
        StandaloneMessage session = this.mMessages.remove(Integer.valueOf(sessionHandle));
        if (session == null) {
            Log.e(LOG_TAG, "handleCancelResponse(): cannot find ftsession");
        } else if (session.mCancelParams == null || session.mCancelParams.mCallback == null) {
            Log.e(LOG_TAG, "handleCancelResponse(): no callback set");
        } else {
            sendCallback(session.mCancelParams.mCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
            session.mCancelParams.mCallback = null;
        }
    }

    /* access modifiers changed from: private */
    public void handleNotify(Notify notify) {
        switch (notify.notifyid()) {
            case Id.NOTIFY_SLM_MSG_INCOMING /*18000*/:
                handleIncomingSlmMessageNotify(notify);
                return;
            case Id.NOTIFY_SLM_FILE_INCOMING /*18001*/:
                handleIncomingSlmFileNotify(notify);
                return;
            case Id.NOTIFY_SLM_PROGRESS /*18003*/:
                handleSlmProgress(notify);
                return;
            case Id.NOTIFY_SLM_LMM_INCOMING_SESSION /*18004*/:
                handleSlmLMMIncomingSession(notify);
                return;
            case Id.NOTIFY_SLM_SIP_RESPONSE_RECEIVED /*18005*/:
                handleSlmSipResponseReceived(notify);
                return;
            default:
                Log.w(LOG_TAG, "handleNotify(): unexpected id");
                return;
        }
    }

    private void sendRequestToStack(int id, FlatBufferBuilder request, int offset, Message callback, UserAgent ua) {
        if (ua == null) {
            Log.e(LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            ua.sendRequestToStack(new ResipStackRequest(id, request, offset, callback));
        }
    }

    private String[] parseEmailOverSlm(ImsUri originalSender, String message) {
        Log.i(LOG_TAG, "parseEmailOverSlm");
        if (originalSender == null || originalSender.getUser() == null || couldBeEmailGateway(originalSender.getUser())) {
            String[] parts = message.split("( /)|( )", 2);
            for (String a : parts) {
                Log.i(LOG_TAG, "parseEmailOverSlm: part: " + a);
            }
            if (parts.length < 2) {
                Log.i(LOG_TAG, "parseEmailOverSlm: message type is not email");
                return null;
            } else if (!ResipUtils.validateEmailAddressFormat(parts[0])) {
                return null;
            } else {
                Log.i(LOG_TAG, "parseEmailOverSlm: email type message");
                return parts;
            }
        } else {
            Log.i(LOG_TAG, "parseEmailOverSlm: No email gateway");
            return null;
        }
    }

    private boolean couldBeEmailGateway(String address) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("couldBeEmailGateway(");
        sb.append(address);
        sb.append(") = ");
        sb.append(address.length() <= 4);
        Log.i(str, sb.toString());
        if (address.length() <= 4) {
            return true;
        }
        return false;
    }

    private void sendCallback(Message callback, Object object) {
        AsyncResult.forMessage(callback, object, (Throwable) null);
        callback.sendToTarget();
    }

    private String parseStr(String str) {
        return str != null ? str : "";
    }
}
