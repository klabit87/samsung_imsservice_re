package com.sec.internal.ims.core.handler.secims;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.MaapNamespace;
import com.sec.internal.constants.ims.servicemodules.im.RcsNamespace;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AddParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupAliasParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatIconParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatLeaderParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatSubjectParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChatbotAnonymizeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.GroupChatInfoParams;
import com.sec.internal.constants.ims.servicemodules.im.params.GroupChatListParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ImSendComposingParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RemoveParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ReportChatbotAsSpamParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageRevokeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.RejectImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StopImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.ims.core.handler.ImHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImComposingStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReportMessageHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptImSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestCancelFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestChatbotAnonymize;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestCloseImSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestGroupInfoSubscribe;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestGroupListSubscribe;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestImSetMoreInfoToSipUA;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRejectImSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestReportChatbotAsSpam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImComposingStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendMessageRevokeRequest;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartImSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartMedia;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class ResipImHandler extends ImHandler {
    protected final RegistrantList mChatbotAnonymizeNotifyRegistrants;
    protected final RegistrantList mChatbotAnonymizeResponseRegistrants;
    protected final RegistrantList mComposingRegistrants;
    protected final RegistrantList mConferenceInfoUpdateRegistrants;
    protected final Map<Integer, FtSession> mFtSessions;
    protected final RegistrantList mGroupChatInfoRegistrants;
    protected final RegistrantList mGroupChatListRegistrants;
    protected final RegistrantList mImdnFailedRegistrants;
    private ResipImdnHandler mImdnHandler;
    private IImsFramework mImsFramework;
    protected final RegistrantList mIncomingFileTransferRegistrants;
    protected final RegistrantList mIncomingMessageRegistrants;
    protected final RegistrantList mIncomingSessionRegistrants;
    protected final RegistrantList mMessageFailedRegistrants;
    protected final RegistrantList mMessageRevokeResponseRegistransts;
    protected final Map<String, FtSession> mPendingFtSessions;
    protected final Map<String, ImSession> mPendingSessions;
    protected final RegistrantList mReportChatbotAsSpamRespRegistrants;
    protected final RegistrantList mSendMessageRevokeResponseRegistransts;
    protected final RegistrantList mSessionClosedRegistrants;
    protected final RegistrantList mSessionEstablishedRegistrants;
    protected final Map<Integer, ImSession> mSessions;
    private ResipImResponseHandler mStackResponseHandler;
    protected final RegistrantList mTransferProgressRegistrants;

    protected static final class ImSession {
        protected Message mAcceptCallback;
        protected Map<String, Message> mAddParticipantsCallbacks;
        protected Map<String, Message> mChangeGCAliasCallbacks;
        protected Map<String, Message> mChangeGCIconCallbacks;
        protected Map<String, Message> mChangeGCLeaderCallbacks;
        protected Map<String, Message> mChangeGCSubjectCallbacks;
        protected String mChatId;
        protected Message mFirstMessageCallback;
        protected boolean mIsSnF;
        protected Message mRejectCallback;
        protected Map<String, Message> mRemoveParticipantsCallbacks;
        protected Map<String, Message> mSendMessageCallbacks;
        protected Integer mSessionHandle;
        protected Message mStartCallback;
        protected Message mStartProvisionalCallback;
        protected Message mStartSyncCallback;
        protected StopImSessionParams mStopParams;
        protected final int mUaHandle;

        protected ImSession(String chatId, Message startCallback, Message startSyncCallback, Message startProvisionalCallback, boolean isSnF, int uaHandle) {
            this.mChangeGCLeaderCallbacks = new HashMap();
            this.mChangeGCSubjectCallbacks = new HashMap();
            this.mChangeGCAliasCallbacks = new HashMap();
            this.mChangeGCIconCallbacks = new HashMap();
            this.mAddParticipantsCallbacks = new HashMap();
            this.mRemoveParticipantsCallbacks = new HashMap();
            this.mSendMessageCallbacks = new HashMap();
            this.mChatId = chatId;
            this.mStartCallback = startCallback;
            this.mStartSyncCallback = startSyncCallback;
            this.mStartProvisionalCallback = startProvisionalCallback;
            this.mIsSnF = isSnF;
            this.mUaHandle = uaHandle;
        }

        protected ImSession(int handle, boolean isSnf, int uaHandle) {
            this((String) null, (Message) null, (Message) null, (Message) null, isSnf, uaHandle);
            this.mSessionHandle = Integer.valueOf(handle);
        }

        /* access modifiers changed from: protected */
        public Message findAndRemoveCallback(String id) {
            Message callBack = this.mSendMessageCallbacks.get(id);
            this.mSendMessageCallbacks.remove(id);
            return callBack;
        }
    }

    protected static final class FtSession {
        protected Message mAcceptCallback;
        protected RejectFtSessionParams mCancelParams;
        protected int mHandle;
        protected int mId = -1;
        protected Message mStartCallback;
        protected Message mStartSessionHandleCallback;
        protected int mUaHandle;

        protected FtSession() {
        }
    }

    public ResipImHandler(Looper looper, IImsFramework imsFramework) {
        this(looper, imsFramework, new ResipImdnHandler(looper, imsFramework));
    }

    public ResipImHandler(Looper looper, IImsFramework imsFramework, ResipImdnHandler imdnHandler) {
        super(looper);
        this.mFtSessions = new HashMap();
        this.mPendingFtSessions = new HashMap();
        this.mSessions = new HashMap();
        this.mPendingSessions = new HashMap();
        this.mSessionEstablishedRegistrants = new RegistrantList();
        this.mSessionClosedRegistrants = new RegistrantList();
        this.mIncomingSessionRegistrants = new RegistrantList();
        this.mIncomingFileTransferRegistrants = new RegistrantList();
        this.mIncomingMessageRegistrants = new RegistrantList();
        this.mComposingRegistrants = new RegistrantList();
        this.mConferenceInfoUpdateRegistrants = new RegistrantList();
        this.mMessageFailedRegistrants = new RegistrantList();
        this.mImdnFailedRegistrants = new RegistrantList();
        this.mTransferProgressRegistrants = new RegistrantList();
        this.mGroupChatListRegistrants = new RegistrantList();
        this.mGroupChatInfoRegistrants = new RegistrantList();
        this.mMessageRevokeResponseRegistransts = new RegistrantList();
        this.mSendMessageRevokeResponseRegistransts = new RegistrantList();
        this.mChatbotAnonymizeResponseRegistrants = new RegistrantList();
        this.mChatbotAnonymizeNotifyRegistrants = new RegistrantList();
        this.mReportChatbotAsSpamRespRegistrants = new RegistrantList();
        this.mImsFramework = imsFramework;
        this.mStackResponseHandler = new ResipImResponseHandler(looper, this);
        this.mImdnHandler = imdnHandler;
        StackIF.getInstance().registerImHandler(this.mStackResponseHandler, 100, (Object) null);
    }

    public void unregisterAllFileTransferProgress() {
    }

    public void startImSession(StartImSessionParams params) {
        sendMessage(obtainMessage(1, params));
    }

    public void acceptImSession(AcceptImSessionParams params) {
        sendMessage(obtainMessage(2, params));
    }

    public void stopImSession(StopImSessionParams params) {
        sendMessage(obtainMessage(3, params));
    }

    public void rejectImSession(RejectImSessionParams rejectParams) {
        sendMessage(obtainMessage(17, rejectParams));
    }

    public void sendImMessage(SendMessageParams params) {
        sendMessage(obtainMessage(4, params));
    }

    public void acceptFtSession(AcceptFtSessionParams params) {
        sendMessage(obtainMessage(5, params));
    }

    public void rejectFtSession(RejectFtSessionParams params) {
        sendMessage(obtainMessage(7, params));
    }

    public void cancelFtSession(RejectFtSessionParams params) {
        sendMessage(obtainMessage(6, params));
    }

    public void sendFtSession(SendFtSessionParams params) {
        sendMessage(obtainMessage(8, params));
    }

    public void sendFtDeliveredNotification(SendImdnParams params) {
        sendMessage(obtainMessage(14, params));
    }

    public void sendFtDisplayedNotification(SendImdnParams params) {
        sendMessage(obtainMessage(14, params));
    }

    public void removeImParticipants(RemoveParticipantsParams params) {
        sendMessage(obtainMessage(21, params));
    }

    public void changeGroupChatLeader(ChangeGroupChatLeaderParams params) {
        sendMessage(obtainMessage(19, params));
    }

    public void changeGroupChatSubject(ChangeGroupChatSubjectParams params) {
        sendMessage(obtainMessage(22, params));
    }

    public void changeGroupChatIcon(ChangeGroupChatIconParams params) {
        sendMessage(obtainMessage(30, params));
    }

    public void changeGroupAlias(ChangeGroupAliasParams params) {
        sendMessage(obtainMessage(23, params));
    }

    public void sendComposingNotification(ImSendComposingParams params) {
        sendMessage(obtainMessage(9, params));
    }

    public void sendDeliveredNotification(SendImdnParams params) {
        sendMessage(obtainMessage(10, params));
    }

    public void sendDisplayedNotification(SendImdnParams params) {
        sendMessage(obtainMessage(10, params));
    }

    public void sendMessageRevokeRequest(SendMessageRevokeParams params) {
        sendMessage(obtainMessage(28, params));
    }

    public void addImParticipants(AddParticipantsParams params) {
        sendMessage(obtainMessage(12, params));
    }

    public void extendToGroupChat(StartImSessionParams params) {
        sendMessage(obtainMessage(13, params));
    }

    public void registerForComposingNotification(Handler h, int what, Object obj) {
        this.mComposingRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForComposingNotification(Handler h) {
        this.mComposingRegistrants.remove(h);
    }

    public void registerForImdnNotification(Handler h, int what, Object obj) {
        this.mImdnHandler.registerForImdnNotification(h, what, obj);
    }

    public void unregisterForImdnNotification(Handler h) {
        this.mImdnHandler.unregisterForImdnNotification(h);
    }

    public void registerForMessageFailed(Handler h, int what, Object obj) {
        this.mMessageFailedRegistrants.add(new Registrant(h, what, obj));
    }

    public void registerForImdnResponse(Handler h, int what, Object obj) {
        this.mImdnHandler.registerForImdnResponse(h, what, obj);
    }

    public void unregisterForImdnResponse(Handler h) {
        this.mImdnHandler.unregisterForImdnResponse(h);
    }

    public void unregisterForMessageFailed(Handler h) {
        this.mMessageFailedRegistrants.remove(h);
    }

    public void registerForImdnFailed(Handler h, int what, Object obj) {
        this.mImdnFailedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImdnFailed(Handler h) {
        this.mImdnFailedRegistrants.remove(h);
    }

    public void registerForConferenceInfoUpdate(Handler h, int what, Object obj) {
        this.mConferenceInfoUpdateRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForConferenceInfoUpdate(Handler h) {
        this.mConferenceInfoUpdateRegistrants.remove(h);
    }

    public void registerForImSessionEstablished(Handler h, int what, Object obj) {
        this.mSessionEstablishedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImSessionEstablished(Handler h) {
        this.mSessionEstablishedRegistrants.remove(h);
    }

    public void registerForImSessionClosed(Handler h, int what, Object obj) {
        this.mSessionClosedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImSessionClosed(Handler h) {
        this.mSessionClosedRegistrants.remove(h);
    }

    public void registerForImIncomingSession(Handler h, int what, Object obj) {
        String str = this.LOG_TAG;
        Log.i(str, "registerForImIncomingSession(): " + h);
        this.mIncomingSessionRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImIncomingSession(Handler h) {
        this.mIncomingSessionRegistrants.remove(h);
    }

    public void registerForImIncomingFileTransfer(Handler h, int what, Object obj) {
        this.mIncomingFileTransferRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImIncomingFileTransfer(Handler h) {
        this.mIncomingFileTransferRegistrants.remove(h);
    }

    public void registerForImIncomingMessage(Handler h, int what, Object obj) {
        this.mIncomingMessageRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImIncomingMessage(Handler h) {
        this.mIncomingMessageRegistrants.remove(h);
    }

    public void registerForTransferProgress(Handler h, int what, Object obj) {
        this.mTransferProgressRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForTransferProgress(Handler h) {
        this.mTransferProgressRegistrants.remove(h);
    }

    public void setFtMessageId(Object rawHandle, int msgId) {
        Integer sessionHandle = (Integer) rawHandle;
        String str = this.LOG_TAG;
        Log.i(str, "setFtMessageId():  sessionHandle = " + sessionHandle + ", msgId:" + msgId);
        FtSession session = this.mFtSessions.get(sessionHandle);
        if (session == null) {
            String str2 = this.LOG_TAG;
            Log.i(str2, "setFtMessageId(): no session in map, id = " + sessionHandle);
            return;
        }
        session.mId = msgId;
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                handleStartImSessionRequest((StartImSessionParams) msg.obj);
                return;
            case 2:
                handleAcceptImSessionRequest((AcceptImSessionParams) msg.obj);
                return;
            case 3:
                handleCloseImSessionRequest((StopImSessionParams) msg.obj);
                return;
            case 4:
                handleSendMessageRequest((SendMessageParams) msg.obj);
                return;
            case 5:
                handleAcceptFtSessionRequest((AcceptFtSessionParams) msg.obj);
                return;
            case 6:
                handleCancelFtSessionRequest((RejectFtSessionParams) msg.obj);
                return;
            case 7:
                handleRejectFtSessionRequest((RejectFtSessionParams) msg.obj);
                return;
            case 8:
                handleStartFtSessionRequest((SendFtSessionParams) msg.obj);
                return;
            case 9:
                handleSendComposingNotification((ImSendComposingParams) msg.obj);
                return;
            case 10:
                handleSendDispositionNotification((SendImdnParams) msg.obj);
                return;
            case 12:
                handleAddParticipantsRequest((AddParticipantsParams) msg.obj);
                return;
            case 13:
                handleStartImSessionRequest((StartImSessionParams) msg.obj);
                return;
            case 14:
                handleSendFtDispositionNotification((SendImdnParams) msg.obj);
                return;
            case 16:
                return;
            case 17:
                handleRejectImSessionRequest((RejectImSessionParams) msg.obj);
                return;
            case 18:
                handleStartFtMediaRequest(((Integer) msg.obj).intValue());
                return;
            case 19:
                handleChangeGroupChatLeaderRequest((ChangeGroupChatLeaderParams) msg.obj);
                return;
            case 20:
                handleRejectImSessionRequest((RejectImSessionParams) msg.obj);
                return;
            case 21:
                handleRemoveParticipantsRequest((RemoveParticipantsParams) msg.obj);
                return;
            case 22:
                handleChangeGroupChatSubjectRequest((ChangeGroupChatSubjectParams) msg.obj);
                return;
            case 23:
                handleChangeGroupChatAliasRequest((ChangeGroupAliasParams) msg.obj);
                return;
            case 24:
                onSubscribeGroupChatList((GroupChatListParams) msg.obj);
                return;
            case 25:
                onSubscribeGroupChatInfo((GroupChatInfoParams) msg.obj);
                return;
            case 28:
                handleSendMessageRevokeRequest((SendMessageRevokeParams) msg.obj);
                return;
            case 29:
                handleSetMoreInfoToSipUARequest((String) msg.obj, msg.arg1);
                return;
            case 30:
                handleChangeGroupChatIconRequest((ChangeGroupChatIconParams) msg.obj);
                return;
            case 31:
                handleReportChatbotAsSpam((ReportChatbotAsSpamParams) msg.obj);
                return;
            case 32:
                handleRequestChatbotAnonymize((ChatbotAnonymizeParams) msg.obj);
                return;
            default:
                Log.e(this.LOG_TAG, "handleMessage: Undefined message.");
                return;
        }
    }

    private void handleStartImSessionRequest(StartImSessionParams params) {
        String fwSessionId;
        boolean isError;
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        int inReplyToContributionIdOffset;
        int acceptTypesOffset;
        int acceptWrappedTypesOffset;
        int userAliasOffset;
        int contributionIdOffset;
        int acceptWrappedTypesOffset2;
        int dateTimeOffset;
        int cpimNamespacesVectorOffset;
        ArrayList<Integer> cpimNamespacesOffsetIntegers;
        int i;
        StartImSessionParams startImSessionParams = params;
        IMSLog.s(this.LOG_TAG, "handleStartImSessionRequest: params = " + startImSessionParams);
        boolean isError2 = false;
        if (startImSessionParams.mSynchronousCallback != null) {
            fwSessionId = (String) startImSessionParams.mSynchronousCallback.obj;
        } else {
            isError2 = true;
            fwSessionId = "";
        }
        UserAgent ua = getUserAgent(startImSessionParams.mOwnImsi);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleStartImSessionRequest(): UserAgent not found.");
            isError2 = true;
        }
        if (startImSessionParams.mReceivers.size() == 0) {
            Log.e(this.LOG_TAG, "handleStartImSessionRequest(): receiver.size() = 0 !");
            isError = true;
        } else if (startImSessionParams.mReceivers.get(0) == null) {
            Log.e(this.LOG_TAG, "handleStartImSessionRequest(): null receiver!");
            isError = true;
        } else {
            isError = isError2;
        }
        if (isError) {
            if (startImSessionParams.mSynchronousCallback != null) {
                sendCallback(startImSessionParams.mSynchronousCallback, fwSessionId);
                startImSessionParams.mSynchronousCallback = null;
            }
            if (startImSessionParams.mCallback != null) {
                sendCallback(startImSessionParams.mCallback, new StartImSessionResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (ImsUri) null, fwSessionId));
                startImSessionParams.mCallback = null;
            }
            if (startImSessionParams.mSendMessageParams != null && startImSessionParams.mSendMessageParams.mCallback != null) {
                sendCallback(startImSessionParams.mSendMessageParams.mCallback, new SendMessageResult(fwSessionId, new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR)));
                startImSessionParams.mSendMessageParams.mCallback = null;
                return;
            }
            return;
        }
        ImSession session = new ImSession(startImSessionParams.mChatId, startImSessionParams.mCallback, startImSessionParams.mSynchronousCallback, startImSessionParams.mDedicatedBearerCallback, false, ua.getHandle());
        this.mPendingSessions.put(fwSessionId, session);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int receiversVectorOffset = BaseSessionData.createReceiversVector(builder, getImsUriOffsetArray(builder, startImSessionParams.mReceivers, startImSessionParams.mReceivers.size()));
        int fwSessionIdOffset = builder.createString((CharSequence) fwSessionId != null ? fwSessionId : "");
        if (startImSessionParams.mSdpContentType != null) {
            str = startImSessionParams.mSdpContentType;
        } else {
            str = "";
        }
        int contentTypeOffset = builder.createString((CharSequence) str);
        if (startImSessionParams.mUserAlias != null) {
            str2 = startImSessionParams.mUserAlias;
        } else {
            str2 = "";
        }
        int userAliasOffset2 = builder.createString((CharSequence) str2);
        if (startImSessionParams.mContributionId != null) {
            str3 = startImSessionParams.mContributionId;
        } else {
            str3 = "";
        }
        int contributionIdOffset2 = builder.createString((CharSequence) str3);
        if (startImSessionParams.mConversationId != null) {
            str4 = startImSessionParams.mConversationId;
        } else {
            str4 = "";
        }
        int conversationIdOffset = builder.createString((CharSequence) str4);
        if (startImSessionParams.mInReplyToContributionId != null) {
            str5 = startImSessionParams.mInReplyToContributionId;
        } else {
            str5 = "";
        }
        int inReplyToContributionIdOffset2 = builder.createString((CharSequence) str5);
        if (startImSessionParams.mPrevContributionId != null) {
            str6 = startImSessionParams.mPrevContributionId;
        } else {
            str6 = "";
        }
        int sessionReplacesOffset = builder.createString((CharSequence) str6);
        String str7 = "";
        int serviceIdOffset = builder.createString((CharSequence) startImSessionParams.mServiceId != null ? startImSessionParams.mServiceId : str7);
        String str8 = fwSessionId;
        int chatModeOffset = builder.createString((CharSequence) startImSessionParams.mChatMode != null ? startImSessionParams.mChatMode.toString() : str7);
        BaseSessionData.startBaseSessionData(builder);
        BaseSessionData.addId(builder, fwSessionIdOffset);
        boolean z = isError;
        BaseSessionData.addIsConference(builder, startImSessionParams.mIsConf);
        BaseSessionData.addIsChatbotParticipant(builder, startImSessionParams.mIsChatbotParticipant);
        BaseSessionData.addSdpContentType(builder, contentTypeOffset);
        BaseSessionData.addReceivers(builder, receiversVectorOffset);
        if (startImSessionParams.mUserAlias != null) {
            BaseSessionData.addUserAlias(builder, userAliasOffset2);
        }
        if (startImSessionParams.mContributionId != null) {
            BaseSessionData.addContributionId(builder, contributionIdOffset2);
        }
        if (startImSessionParams.mConversationId != null) {
            BaseSessionData.addConversationId(builder, conversationIdOffset);
        }
        if (startImSessionParams.mInReplyToContributionId != null) {
            BaseSessionData.addInReplyToContributionId(builder, inReplyToContributionIdOffset2);
        }
        if (startImSessionParams.mPrevContributionId != null) {
            BaseSessionData.addSessionReplaces(builder, sessionReplacesOffset);
        }
        if (!TextUtils.isEmpty(startImSessionParams.mServiceId)) {
            BaseSessionData.addServiceId(builder, serviceIdOffset);
        }
        if (startImSessionParams.mChatMode != null) {
            BaseSessionData.addChatMode(builder, chatModeOffset);
        }
        int baseSessionDataOffset = BaseSessionData.endBaseSessionData(builder);
        int serviceIdOffset2 = serviceIdOffset;
        if (startImSessionParams.mAcceptTypes == null || startImSessionParams.mAcceptTypes.isEmpty()) {
            inReplyToContributionIdOffset = inReplyToContributionIdOffset2;
            acceptTypesOffset = -1;
        } else {
            inReplyToContributionIdOffset = inReplyToContributionIdOffset2;
            acceptTypesOffset = ImSessionParam.createAcceptTypesVector(builder, getStringOffsetArray(builder, startImSessionParams.mAcceptTypes, startImSessionParams.mAcceptTypes.size()));
        }
        if (startImSessionParams.mAcceptWrappedTypes == null || startImSessionParams.mAcceptWrappedTypes.isEmpty()) {
            acceptWrappedTypesOffset = -1;
        } else {
            int i2 = sessionReplacesOffset;
            acceptWrappedTypesOffset = ImSessionParam.createAcceptWrappedTypesVector(builder, getStringOffsetArray(builder, startImSessionParams.mAcceptWrappedTypes, startImSessionParams.mAcceptWrappedTypes.size()));
        }
        int subjectOffset = builder.createString((CharSequence) startImSessionParams.mSubject != null ? startImSessionParams.mSubject : str7);
        ImSessionParam.startImSessionParam(builder);
        int i3 = conversationIdOffset;
        ImSessionParam.addIsRejoin(builder, startImSessionParams.mIsRejoin);
        ImSessionParam.addIsClosedGroupchat(builder, startImSessionParams.mIsClosedGroupChat);
        ImSessionParam.addIsInviteforbye(builder, startImSessionParams.mIsInviteForBye);
        ImSessionParam.addIsGeolocationPush(builder, startImSessionParams.mIsGeolocationPush);
        if (startImSessionParams.mSubject != null) {
            ImSessionParam.addSubject(builder, subjectOffset);
        }
        if (startImSessionParams.mAcceptTypes != null && !startImSessionParams.mAcceptTypes.isEmpty()) {
            ImSessionParam.addAcceptTypes(builder, acceptTypesOffset);
        }
        int subjectOffset2 = subjectOffset;
        if (!TextUtils.isEmpty(startImSessionParams.mServiceId)) {
            ImSessionParam.addIsExtension(builder, true);
        }
        if (startImSessionParams.mAcceptWrappedTypes != null && !startImSessionParams.mAcceptWrappedTypes.isEmpty()) {
            ImSessionParam.addAcceptWrappedTypes(builder, acceptWrappedTypesOffset);
        }
        ImSessionParam.addBaseSessionData(builder, baseSessionDataOffset);
        int imSessionParamOffset = ImSessionParam.endImSessionParam(builder);
        int acceptTypesOffset2 = acceptTypesOffset;
        SendMessageParams msgParams = startImSessionParams.mSendMessageParams;
        if (msgParams != null) {
            ArrayList<Integer> cpimNamespacesOffsetIntegers2 = new ArrayList<>();
            acceptWrappedTypesOffset2 = acceptWrappedTypesOffset;
            if (msgParams.mContentType == null || msgParams.mContentType.isEmpty()) {
                contributionIdOffset = contributionIdOffset2;
            } else {
                contributionIdOffset = contributionIdOffset2;
                if (!msgParams.mContentType.toLowerCase(Locale.US).contains("charset=")) {
                    msgParams.mContentType += ";charset=UTF-8";
                }
            }
            if (msgParams.mMaapTrafficType != null) {
                int cpimNamespaceNameOffset = builder.createString((CharSequence) MaapNamespace.NAME);
                Object obj = ";charset=UTF-8";
                int cpimNamespaceUriOffset = builder.createString((CharSequence) MaapNamespace.URI);
                cpimNamespacesVectorOffset = -1;
                int cpimNamespaceKeyOffset = builder.createString((CharSequence) "Traffic-Type");
                userAliasOffset = userAliasOffset2;
                int cpimNamespaceValueOffset = builder.createString((CharSequence) msgParams.mMaapTrafficType);
                Pair.startPair(builder);
                Pair.addKey(builder, cpimNamespaceKeyOffset);
                Pair.addValue(builder, cpimNamespaceValueOffset);
                int i4 = cpimNamespaceKeyOffset;
                int i5 = cpimNamespaceValueOffset;
                int[] headersOffset = {Pair.endPair(builder)};
                int headersVectorOffset = CpimNamespace.createHeadersVector(builder, headersOffset);
                CpimNamespace.startCpimNamespace(builder);
                CpimNamespace.addName(builder, cpimNamespaceNameOffset);
                CpimNamespace.addUri(builder, cpimNamespaceUriOffset);
                CpimNamespace.addHeaders(builder, headersVectorOffset);
                int i6 = cpimNamespaceUriOffset;
                int[] iArr = headersOffset;
                cpimNamespacesOffsetIntegers = cpimNamespacesOffsetIntegers2;
                cpimNamespacesOffsetIntegers.add(Integer.valueOf(CpimNamespace.endCpimNamespace(builder)));
            } else {
                String charSet = ";charset=UTF-8";
                cpimNamespacesVectorOffset = -1;
                userAliasOffset = userAliasOffset2;
                cpimNamespacesOffsetIntegers = cpimNamespacesOffsetIntegers2;
            }
            if (msgParams.mReferenceId == null && msgParams.mReferenceType == null && msgParams.mReferenceValue == null) {
                int i7 = chatModeOffset;
            } else {
                int cpimNamespaceNameOffset2 = builder.createString((CharSequence) RcsNamespace.KOR.NAME);
                int cpimNamespaceUriOffset2 = builder.createString((CharSequence) RcsNamespace.KOR.URI);
                int[] headersOffset2 = new int[0];
                if (msgParams.mReferenceId != null) {
                    int cpimNamespaceKeyOffset2 = builder.createString((CharSequence) RcsNamespace.REFERENCE_ID_KEY);
                    int i8 = chatModeOffset;
                    int cpimNamespaceValueOffset2 = builder.createString((CharSequence) msgParams.mReferenceId);
                    Pair.startPair(builder);
                    Pair.addKey(builder, cpimNamespaceKeyOffset2);
                    Pair.addValue(builder, cpimNamespaceValueOffset2);
                    int headersPairOffset = Pair.endPair(builder);
                    int i9 = cpimNamespaceKeyOffset2;
                    headersOffset2 = Arrays.copyOf(headersOffset2, headersOffset2.length + 1);
                    headersOffset2[headersOffset2.length - 1] = headersPairOffset;
                    int i10 = cpimNamespaceValueOffset2;
                }
                if (msgParams.mReferenceType != null) {
                    int cpimNamespaceKeyOffset22 = builder.createString((CharSequence) RcsNamespace.REFERENCE_TYPE_KEY);
                    int cpimNamespaceValueOffset22 = builder.createString((CharSequence) msgParams.mReferenceType);
                    Pair.startPair(builder);
                    Pair.addKey(builder, cpimNamespaceKeyOffset22);
                    Pair.addValue(builder, cpimNamespaceValueOffset22);
                    int headersPairOffset2 = Pair.endPair(builder);
                    int i11 = cpimNamespaceKeyOffset22;
                    headersOffset2 = Arrays.copyOf(headersOffset2, headersOffset2.length + 1);
                    headersOffset2[headersOffset2.length - 1] = headersPairOffset2;
                    int i12 = cpimNamespaceValueOffset22;
                }
                if (msgParams.mReferenceValue != null) {
                    int cpimNamespaceKeyOffset3 = builder.createString((CharSequence) RcsNamespace.REFERENCE_VALUE_KEY);
                    int cpimNamespaceValueOffset3 = builder.createString((CharSequence) msgParams.mReferenceValue);
                    Pair.startPair(builder);
                    Pair.addKey(builder, cpimNamespaceKeyOffset3);
                    Pair.addValue(builder, cpimNamespaceValueOffset3);
                    int headersPairOffset3 = Pair.endPair(builder);
                    int i13 = cpimNamespaceKeyOffset3;
                    headersOffset2 = Arrays.copyOf(headersOffset2, headersOffset2.length + 1);
                    headersOffset2[headersOffset2.length - 1] = headersPairOffset3;
                    int i14 = cpimNamespaceValueOffset3;
                }
                int headersVectorOffset2 = CpimNamespace.createHeadersVector(builder, headersOffset2);
                CpimNamespace.startCpimNamespace(builder);
                CpimNamespace.addName(builder, cpimNamespaceNameOffset2);
                CpimNamespace.addUri(builder, cpimNamespaceUriOffset2);
                CpimNamespace.addHeaders(builder, headersVectorOffset2);
                cpimNamespacesOffsetIntegers.add(Integer.valueOf(CpimNamespace.endCpimNamespace(builder)));
            }
            if (cpimNamespacesOffsetIntegers.size() > 0) {
                int[] cpimNamespacesOffset = new int[cpimNamespacesOffsetIntegers.size()];
                for (int i15 = 0; i15 < cpimNamespacesOffset.length; i15++) {
                    cpimNamespacesOffset[i15] = cpimNamespacesOffsetIntegers.get(i15).intValue();
                }
                i = ImMessageParam.createCpimNamespacesVector(builder, cpimNamespacesOffset);
            } else {
                i = cpimNamespacesVectorOffset;
            }
            session.mFirstMessageCallback = msgParams.mCallback;
            int notiVectorOffset = ImdnParams.createNotiVector(builder, ResipTranslatorCollection.translateFwImdnNoti(msgParams.mDispositionNotification));
            int msgIdOffset = builder.createString((CharSequence) msgParams.mImdnMessageId != null ? msgParams.mImdnMessageId : str7);
            int dateTimeOffset2 = builder.createString((CharSequence) msgParams.mImdnTime != null ? Iso8601.formatMillis(msgParams.mImdnTime) : str7);
            int bodyOffset = builder.createString((CharSequence) msgParams.mBody != null ? msgParams.mBody : str7);
            int i16 = baseSessionDataOffset;
            int contentTypeStrOffset = builder.createString((CharSequence) msgParams.mContentType != null ? msgParams.mContentType : str7);
            ImSession imSession = session;
            int xmsMsgOffset = builder.createString((CharSequence) msgParams.mXmsMessage == null ? str7 : msgParams.mXmsMessage);
            ImdnParams.startImdnParams(builder);
            ImdnParams.addMessageId(builder, msgIdOffset);
            ImdnParams.addDatetime(builder, dateTimeOffset2);
            ImdnParams.addNoti(builder, notiVectorOffset);
            int i17 = notiVectorOffset;
            int notiVectorOffset2 = ImdnParams.endImdnParams(builder);
            ImMessageParam.startImMessageParam(builder);
            ImMessageParam.addBody(builder, bodyOffset);
            ImMessageParam.addContentType(builder, contentTypeStrOffset);
            ImMessageParam.addImdn(builder, notiVectorOffset2);
            ImMessageParam.addXmsMessage(builder, xmsMsgOffset);
            int i18 = notiVectorOffset2;
            ImMessageParam.addExtraFt(builder, msgParams.mExtraFt);
            if (cpimNamespacesOffsetIntegers.size() > 0) {
                ImMessageParam.addCpimNamespaces(builder, i);
            }
            dateTimeOffset = ImMessageParam.endImMessageParam(builder);
        } else {
            acceptWrappedTypesOffset2 = acceptWrappedTypesOffset;
            contributionIdOffset = contributionIdOffset2;
            userAliasOffset = userAliasOffset2;
            int i19 = chatModeOffset;
            int i20 = baseSessionDataOffset;
            ImSession imSession2 = session;
            dateTimeOffset = -1;
        }
        RequestStartImSession.startRequestStartImSession(builder);
        RequestStartImSession.addRegistrationHandle(builder, (long) ua.getHandle());
        RequestStartImSession.addSession(builder, imSessionParamOffset);
        if (msgParams != null) {
            RequestStartImSession.addMessageParam(builder, dateTimeOffset);
        }
        int offset = RequestStartImSession.endRequestStartImSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 501);
        Request.addReqType(builder, (byte) 39);
        Request.addReq(builder, offset);
        int offset2 = Request.endRequest(builder);
        Log.e(this.LOG_TAG, "handleStartImSessionRequest(): Armaan: sending to stack!");
        int i21 = serviceIdOffset2;
        int i22 = subjectOffset2;
        int i23 = inReplyToContributionIdOffset;
        int i24 = acceptTypesOffset2;
        SendMessageParams sendMessageParams = msgParams;
        int i25 = acceptWrappedTypesOffset2;
        int i26 = imSessionParamOffset;
        int i27 = contributionIdOffset;
        int i28 = userAliasOffset;
        sendRequestToStack(501, builder, offset2, this.mStackResponseHandler.obtainMessage(1), ua);
    }

    private void handleAcceptImSessionRequest(AcceptImSessionParams params) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleAcceptImSessionRequest(): params " + params);
        int sessionHandle = ((Integer) params.mRawHandle).intValue();
        ImSession session = this.mSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleAcceptImSessionRequest: no session in map, return accept failure");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, new StartImSessionResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (ImsUri) null, Integer.valueOf(sessionHandle)));
                params.mCallback = null;
                return;
            }
            return;
        }
        session.mChatId = params.mChatId;
        session.mAcceptCallback = params.mCallback;
        session.mIsSnF = params.mIsSnF;
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleAcceptImSessionRequest(): UserAgent not found. UaHandle = " + session.mUaHandle);
            if (session.mAcceptCallback != null) {
                sendCallback(session.mAcceptCallback, new StartImSessionResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (ImsUri) null, Integer.valueOf(sessionHandle)));
                session.mAcceptCallback = null;
                return;
            }
            return;
        }
        String userAlias = params.mUserAlias == null ? "" : params.mUserAlias;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int userAliasOffset = builder.createString((CharSequence) parseStr(userAlias));
        RequestAcceptImSession.startRequestAcceptImSession(builder);
        RequestAcceptImSession.addSessionId(builder, (long) sessionHandle);
        RequestAcceptImSession.addUserAlias(builder, userAliasOffset);
        int offset = RequestAcceptImSession.endRequestAcceptImSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 502);
        Request.addReqType(builder, (byte) 40);
        Request.addReq(builder, offset);
        sendRequestToStack(502, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(2), ua);
    }

    /* access modifiers changed from: protected */
    public void handleRejectImSessionRequest(RejectImSessionParams rejectParams) {
        String str = this.LOG_TAG;
        Log.i(str, "handleRejectImSessionRequest: " + rejectParams);
        int sessionHandle = ((Integer) rejectParams.mRawHandle).intValue();
        ImSession session = this.mSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleRejectImSessionRequest: no session in map, return reject failure");
            if (rejectParams.mCallback != null) {
                sendCallback(rejectParams.mCallback, new RejectImSessionResult(Integer.valueOf(sessionHandle), ImError.ENGINE_ERROR));
                rejectParams.mCallback = null;
                return;
            }
            return;
        }
        session.mRejectCallback = rejectParams.mCallback;
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleRejectImSessionRequest: User Agent not found");
            if (rejectParams.mCallback != null) {
                sendCallback(rejectParams.mCallback, new RejectImSessionResult(Integer.valueOf(sessionHandle), ImError.ENGINE_ERROR));
                rejectParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int warningTextOffset = builder.createString((CharSequence) parseStr(rejectParams.mSessionRejectReason.getWarningText()));
        WarningHdr.startWarningHdr(builder);
        WarningHdr.addCode(builder, rejectParams.mSessionRejectReason.getWarningCode());
        WarningHdr.addText(builder, warningTextOffset);
        int offset = WarningHdr.endWarningHdr(builder);
        RequestRejectImSession.startRequestRejectImSession(builder);
        RequestRejectImSession.addSessionHandle(builder, (long) sessionHandle);
        RequestRejectImSession.addSipCode(builder, (long) rejectParams.mSessionRejectReason.getSipCode());
        RequestRejectImSession.addWarningHdr(builder, offset);
        int offset2 = RequestRejectImSession.endRequestRejectImSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_REJECT_IM_SESSION);
        Request.addReqType(builder, (byte) 51);
        Request.addReq(builder, offset2);
        sendRequestToStack(Id.REQUEST_REJECT_IM_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(17), ua);
    }

    private void handleCloseImSessionRequest(StopImSessionParams params) {
        String str = this.LOG_TAG;
        Log.i(str, "handleCloseImSessionRequest(): " + params);
        ImSession session = this.mSessions.get(params.mRawHandle);
        if (session == null) {
            Log.e(this.LOG_TAG, "handleCloseImSessionRequest(): unknown session!");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, new StopImSessionResult(params.mRawHandle, ImError.ENGINE_ERROR));
                params.mCallback = null;
                return;
            }
            return;
        }
        session.mStopParams = params;
        sendImCancelRequestToStack(session);
    }

    private void onSubscribeGroupChatList(GroupChatListParams settings) {
        Log.i(this.LOG_TAG, "onSubscribeGroupChatList()");
        UserAgent ua = getUserAgent(settings.getOwnImsi());
        if (ua == null) {
            Log.e(this.LOG_TAG, "onSubscribeGroupChatList(): UserAgent not found.");
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int subscriptionIdOffset = builder.createString((CharSequence) "SubscribeGroupChatList");
        RequestGroupListSubscribe.startRequestGroupListSubscribe(builder);
        RequestGroupListSubscribe.addHandle(builder, (long) ua.getHandle());
        RequestGroupListSubscribe.addSubscriptionId(builder, subscriptionIdOffset);
        RequestGroupListSubscribe.addVersion(builder, (long) settings.getVersion());
        RequestGroupListSubscribe.addIsIncrease(builder, settings.getIncreaseMode());
        int offset = RequestGroupListSubscribe.endRequestGroupListSubscribe(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_GROUP_LIST_SUBSCRIBE);
        Request.addReqType(builder, (byte) 55);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_GROUP_LIST_SUBSCRIBE, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(24), ua);
    }

    private void onSubscribeGroupChatInfo(GroupChatInfoParams params) {
        String str = this.LOG_TAG;
        Log.i(str, "onSubscribeGroupChatInfo() uri:" + params.getUri());
        UserAgent ua = getUserAgent(params.getOwnImsi());
        if (ua == null) {
            Log.e(this.LOG_TAG, "onSubscribeGroupChatList(): UserAgent not found.");
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int subscriptionIdOffset = builder.createString((CharSequence) "SubscribeGroupChatInfo" + params.getUri().toString());
        int uriOffset = builder.createString((CharSequence) parseStr(params.getUri().toString()));
        RequestGroupInfoSubscribe.startRequestGroupInfoSubscribe(builder);
        RequestGroupInfoSubscribe.addHandle(builder, (long) ua.getHandle());
        RequestGroupInfoSubscribe.addSubscriptionId(builder, subscriptionIdOffset);
        RequestGroupInfoSubscribe.addUri(builder, uriOffset);
        int offset = RequestGroupInfoSubscribe.endRequestGroupInfoSubscribe(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_GROUP_INFO_SUBSCRIBE);
        Request.addReqType(builder, (byte) 56);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_GROUP_INFO_SUBSCRIBE, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(25, (Object) null), ua);
    }

    private void sendImCancelRequestToStack(ImSession session) {
        int textOffset;
        StopImSessionParams params = session.mStopParams;
        if (params == null) {
            Log.e(this.LOG_TAG, "sendImCancelRequestToStack(): null stop params!");
            return;
        }
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "sendImCancelRequestToStack: UserAgent not found. UaHandle = " + session.mUaHandle);
            if (params.mCallback != null) {
                sendCallback(params.mCallback, new StopImSessionResult(params.mRawHandle, ImError.ENGINE_ERROR));
                params.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        if (params.mSessionStopReason != null) {
            textOffset = builder.createString((CharSequence) parseStr(params.mSessionStopReason.getReasonText()));
        } else {
            textOffset = -1;
        }
        ReasonHdr.startReasonHdr(builder);
        if (params.mSessionStopReason != null) {
            ReasonHdr.addCode(builder, (long) params.mSessionStopReason.getCauseCode());
            ReasonHdr.addText(builder, textOffset);
        }
        int offset = ReasonHdr.endReasonHdr(builder);
        RequestCloseImSession.startRequestCloseImSession(builder);
        RequestCloseImSession.addSessionId(builder, (long) session.mSessionHandle.intValue());
        RequestCloseImSession.addReasonHdr(builder, offset);
        int offset2 = RequestCloseImSession.endRequestCloseImSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 503);
        Request.addReqType(builder, (byte) 41);
        Request.addReq(builder, offset2);
        sendRequestToStack(503, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(3, params.mCallback), ua);
    }

    private void handleSendComposingNotification(ImSendComposingParams params) {
        String str = this.LOG_TAG;
        Log.i(str, "handleSendComposingNotification(): " + params);
        ImSession session = this.mSessions.get(params.mRawHandle);
        if (session == null) {
            Log.e(this.LOG_TAG, "handleSendComposingNotification(): invalid session handle!");
            return;
        }
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleSendComposingNotification(): user agent not found");
            return;
        }
        String userAlias = params.mUserAlias == null ? "" : params.mUserAlias;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int contentTypeOffset = builder.createString((CharSequence) "text/plain");
        int aliasOffset = builder.createString((CharSequence) userAlias);
        ImComposingStatus.startImComposingStatus(builder);
        ImComposingStatus.addContentType(builder, contentTypeOffset);
        ImComposingStatus.addInterval(builder, (long) params.mInterval);
        ImComposingStatus.addIsActive(builder, params.mIsComposing);
        int offset = ImComposingStatus.endImComposingStatus(builder);
        RequestSendImComposingStatus.startRequestSendImComposingStatus(builder);
        RequestSendImComposingStatus.addSessionId(builder, (long) session.mSessionHandle.intValue());
        RequestSendImComposingStatus.addStatus(builder, offset);
        RequestSendImComposingStatus.addUserAlias(builder, aliasOffset);
        int offset2 = RequestSendImComposingStatus.endRequestSendImComposingStatus(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_IM_SEND_COMPOSING_STATUS);
        Request.addReqType(builder, (byte) 44);
        Request.addReq(builder, offset2);
        sendRequestToStack(Id.REQUEST_IM_SEND_COMPOSING_STATUS, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(9), ua);
    }

    private void handleSendMessageRequest(SendMessageParams params) {
        String userAlias;
        int cpimNamespacesVectorOffset;
        int cpimNamespacesVectorOffset2;
        int ccParticipantsOffset;
        int deviceNameOffset;
        int reliableMsgOffset;
        int xmsMsgOffset;
        int cpimNamespaceKeyOffset2;
        int cpimNamespaceValueOffset;
        int cpimNamespaceKeyOffset22;
        SendMessageParams sendMessageParams = params;
        IMSLog.s(this.LOG_TAG, "handleSendMessageRequest(): " + sendMessageParams);
        ImSession session = this.mSessions.get(sendMessageParams.mRawHandle);
        if (session == null) {
            Log.e(this.LOG_TAG, "handleSendMessageRequest(): invalid session handle!");
            if (sendMessageParams.mCallback != null) {
                sendCallback(sendMessageParams.mCallback, new SendMessageResult(sendMessageParams.mRawHandle, new Result(ImError.TRANSACTION_DOESNT_EXIST, Result.Type.ENGINE_ERROR)));
                return;
            }
            return;
        }
        session.mSendMessageCallbacks.put(sendMessageParams.mImdnMessageId, sendMessageParams.mCallback);
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleSendMessageRequest(): user agent not found");
            if (sendMessageParams.mCallback != null) {
                sendCallback(sendMessageParams.mCallback, new SendMessageResult(sendMessageParams.mRawHandle, new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR)));
                sendMessageParams.mCallback = null;
                return;
            }
            return;
        }
        if (!sendMessageParams.mContentType.toLowerCase(Locale.US).contains("charset=")) {
            Log.e(this.LOG_TAG, "handleSendMessageRequest(): missed charset, use utf8!");
            sendMessageParams.mContentType += ";charset=UTF-8";
        }
        int[] imdnNotification = ResipTranslatorCollection.translateFwImdnNoti(sendMessageParams.mDispositionNotification);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int imdnNotiVectorOffset = ImdnParams.createNotiVector(builder, imdnNotification);
        int imdnMsgIdOffset = builder.createString((CharSequence) parseStr(sendMessageParams.mImdnMessageId));
        int dateTimeOffset = builder.createString((CharSequence) sendMessageParams.mImdnTime != null ? Iso8601.formatMillis(sendMessageParams.mImdnTime) : "");
        ImdnParams.startImdnParams(builder);
        ImdnParams.addMessageId(builder, imdnMsgIdOffset);
        ImdnParams.addDatetime(builder, dateTimeOffset);
        ImdnParams.addNoti(builder, imdnNotiVectorOffset);
        int imdnParamsOffset = ImdnParams.endImdnParams(builder);
        ArrayList<Integer> cpimNamespacesOffsetIntegers = new ArrayList<>();
        if (sendMessageParams.mMaapTrafficType != null) {
            int cpimNamespaceNameOffset = builder.createString((CharSequence) MaapNamespace.NAME);
            int cpimNamespaceUriOffset = builder.createString((CharSequence) MaapNamespace.URI);
            int cpimNamespaceKeyOffset = builder.createString((CharSequence) "Traffic-Type");
            cpimNamespacesVectorOffset = -1;
            int cpimNamespaceValueOffset2 = builder.createString((CharSequence) sendMessageParams.mMaapTrafficType);
            Pair.startPair(builder);
            Pair.addKey(builder, cpimNamespaceKeyOffset);
            Pair.addValue(builder, cpimNamespaceValueOffset2);
            int i = cpimNamespaceValueOffset2;
            userAlias = "";
            int[] headersOffset = {Pair.endPair(builder)};
            int headersVectorOffset = CpimNamespace.createHeadersVector(builder, headersOffset);
            CpimNamespace.startCpimNamespace(builder);
            CpimNamespace.addName(builder, cpimNamespaceNameOffset);
            CpimNamespace.addUri(builder, cpimNamespaceUriOffset);
            CpimNamespace.addHeaders(builder, headersVectorOffset);
            int[] iArr = headersOffset;
            cpimNamespacesOffsetIntegers.add(Integer.valueOf(CpimNamespace.endCpimNamespace(builder)));
        } else {
            cpimNamespacesVectorOffset = -1;
            userAlias = "";
        }
        if (!(sendMessageParams.mReferenceId == null && sendMessageParams.mReferenceType == null && sendMessageParams.mReferenceValue == null)) {
            int cpimNamespaceNameOffset2 = builder.createString((CharSequence) RcsNamespace.KOR.NAME);
            int cpimNamespaceUriOffset2 = builder.createString((CharSequence) RcsNamespace.KOR.URI);
            int[] headersOffset2 = new int[0];
            if (sendMessageParams.mReferenceId != null) {
                int cpimNamespaceKeyOffset3 = builder.createString((CharSequence) RcsNamespace.REFERENCE_ID_KEY);
                cpimNamespaceKeyOffset2 = -1;
                int cpimNamespaceValueOffset3 = builder.createString((CharSequence) sendMessageParams.mReferenceId);
                Pair.startPair(builder);
                Pair.addKey(builder, cpimNamespaceKeyOffset3);
                Pair.addValue(builder, cpimNamespaceValueOffset3);
                int headersPairOffset = Pair.endPair(builder);
                int i2 = cpimNamespaceKeyOffset3;
                headersOffset2 = Arrays.copyOf(headersOffset2, headersOffset2.length + 1);
                headersOffset2[headersOffset2.length - 1] = headersPairOffset;
                cpimNamespaceValueOffset = cpimNamespaceValueOffset3;
            } else {
                cpimNamespaceKeyOffset2 = -1;
                cpimNamespaceValueOffset = -1;
            }
            if (sendMessageParams.mReferenceType != null) {
                cpimNamespaceKeyOffset22 = builder.createString((CharSequence) RcsNamespace.REFERENCE_TYPE_KEY);
                int i3 = cpimNamespaceValueOffset;
                int cpimNamespaceValueOffset22 = builder.createString((CharSequence) sendMessageParams.mReferenceType);
                Pair.startPair(builder);
                Pair.addKey(builder, cpimNamespaceKeyOffset22);
                Pair.addValue(builder, cpimNamespaceValueOffset22);
                int headersPairOffset2 = Pair.endPair(builder);
                int i4 = cpimNamespaceValueOffset22;
                headersOffset2 = Arrays.copyOf(headersOffset2, headersOffset2.length + 1);
                headersOffset2[headersOffset2.length - 1] = headersPairOffset2;
            } else {
                cpimNamespaceKeyOffset22 = cpimNamespaceKeyOffset2;
            }
            if (sendMessageParams.mReferenceValue != null) {
                int cpimNamespaceKeyOffset32 = builder.createString((CharSequence) RcsNamespace.REFERENCE_VALUE_KEY);
                int i5 = cpimNamespaceKeyOffset22;
                int cpimNamespaceValueOffset32 = builder.createString((CharSequence) sendMessageParams.mReferenceValue);
                Pair.startPair(builder);
                Pair.addKey(builder, cpimNamespaceKeyOffset32);
                Pair.addValue(builder, cpimNamespaceValueOffset32);
                int headersPairOffset3 = Pair.endPair(builder);
                int i6 = cpimNamespaceKeyOffset32;
                headersOffset2 = Arrays.copyOf(headersOffset2, headersOffset2.length + 1);
                headersOffset2[headersOffset2.length - 1] = headersPairOffset3;
                int i7 = cpimNamespaceValueOffset32;
            }
            int headersVectorOffset2 = CpimNamespace.createHeadersVector(builder, headersOffset2);
            CpimNamespace.startCpimNamespace(builder);
            CpimNamespace.addName(builder, cpimNamespaceNameOffset2);
            CpimNamespace.addUri(builder, cpimNamespaceUriOffset2);
            CpimNamespace.addHeaders(builder, headersVectorOffset2);
            cpimNamespacesOffsetIntegers.add(Integer.valueOf(CpimNamespace.endCpimNamespace(builder)));
        }
        if (cpimNamespacesOffsetIntegers.size() > 0) {
            int[] cpimNamespacesOffset = new int[cpimNamespacesOffsetIntegers.size()];
            for (int i8 = 0; i8 < cpimNamespacesOffset.length; i8++) {
                cpimNamespacesOffset[i8] = cpimNamespacesOffsetIntegers.get(i8).intValue();
            }
            cpimNamespacesVectorOffset2 = ImMessageParam.createCpimNamespacesVector(builder, cpimNamespacesOffset);
        } else {
            cpimNamespacesVectorOffset2 = cpimNamespacesVectorOffset;
        }
        if (sendMessageParams.mGroupCcList != null) {
            Log.i(this.LOG_TAG, "handleSendMessageRequest, params.mGroupCcList=" + sendMessageParams.mGroupCcList);
            ccParticipantsOffset = ImMessageParam.createCcParticipantsVector(builder, getImsUriOffsetArray(builder, sendMessageParams.mGroupCcList, sendMessageParams.mGroupCcList.size()));
        } else {
            ccParticipantsOffset = -1;
        }
        int bodyOffset = builder.createString((CharSequence) parseStr(sendMessageParams.mBody));
        int contentTypeOffset = builder.createString((CharSequence) parseStr(sendMessageParams.mContentType));
        if (sendMessageParams.mDeviceName != null) {
            deviceNameOffset = builder.createString((CharSequence) sendMessageParams.mDeviceName);
        } else {
            deviceNameOffset = -1;
        }
        int[] iArr2 = imdnNotification;
        if (sendMessageParams.mReliableMessage != null) {
            reliableMsgOffset = builder.createString((CharSequence) sendMessageParams.mReliableMessage);
        } else {
            reliableMsgOffset = -1;
        }
        int i9 = imdnNotiVectorOffset;
        if (sendMessageParams.mXmsMessage != null) {
            xmsMsgOffset = builder.createString((CharSequence) sendMessageParams.mXmsMessage);
        } else {
            xmsMsgOffset = -1;
        }
        int xmsMsgOffset2 = imdnMsgIdOffset;
        if (sendMessageParams.mUserAlias != null) {
            userAlias = sendMessageParams.mUserAlias;
        }
        int i10 = dateTimeOffset;
        int userAliasOffset = builder.createString((CharSequence) parseStr(userAlias));
        ImMessageParam.startImMessageParam(builder);
        ImMessageParam.addBody(builder, bodyOffset);
        ImMessageParam.addUserAlias(builder, userAliasOffset);
        ImMessageParam.addContentType(builder, contentTypeOffset);
        ImMessageParam.addImdn(builder, imdnParamsOffset);
        if (cpimNamespacesOffsetIntegers.size() > 0) {
            ImMessageParam.addCpimNamespaces(builder, cpimNamespacesVectorOffset2);
        }
        int i11 = contentTypeOffset;
        if (sendMessageParams.mDeviceName != null) {
            ImMessageParam.addDeviceName(builder, deviceNameOffset);
        }
        if (sendMessageParams.mReliableMessage != null) {
            ImMessageParam.addReliableMessage(builder, reliableMsgOffset);
        }
        ImMessageParam.addExtraFt(builder, sendMessageParams.mExtraFt);
        if (sendMessageParams.mXmsMessage != null) {
            ImMessageParam.addXmsMessage(builder, xmsMsgOffset);
        }
        if (sendMessageParams.mGroupCcList != null) {
            ImMessageParam.addCcParticipants(builder, ccParticipantsOffset);
        }
        int imMessageParamOffset = ImMessageParam.endImMessageParam(builder);
        BaseSessionData.startBaseSessionData(builder);
        int i12 = deviceNameOffset;
        int i13 = bodyOffset;
        int ccParticipantsOffset2 = ccParticipantsOffset;
        BaseSessionData.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
        int baseSessionDataOffset = BaseSessionData.endBaseSessionData(builder);
        RequestSendImMessage.startRequestSendImMessage(builder);
        RequestSendImMessage.addSessionData(builder, baseSessionDataOffset);
        RequestSendImMessage.addMessageParam(builder, imMessageParamOffset);
        int requestSendImMessageOffset = RequestSendImMessage.endRequestSendImMessage(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_IM_SENDMSG);
        Request.addReqType(builder, (byte) 43);
        Request.addReq(builder, requestSendImMessageOffset);
        int i14 = imMessageParamOffset;
        int i15 = requestSendImMessageOffset;
        int i16 = ccParticipantsOffset2;
        int ccParticipantsOffset3 = baseSessionDataOffset;
        int i17 = cpimNamespacesVectorOffset2;
        ArrayList<Integer> arrayList = cpimNamespacesOffsetIntegers;
        sendRequestToStack(Id.REQUEST_IM_SENDMSG, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(4), ua);
    }

    private void handleSendDispositionNotification(SendImdnParams params) {
        ImSession session = null;
        if (params.mRawHandle != null) {
            session = this.mSessions.get(params.mRawHandle);
        }
        this.mImdnHandler.sendDispositionNotification(params, 1, session != null ? session.mSessionHandle.intValue() : -1);
    }

    private void handleSendFtDispositionNotification(SendImdnParams params) {
        this.mImdnHandler.sendDispositionNotification(params, 2, -1);
    }

    private void handleSendMessageRevokeRequest(SendMessageRevokeParams params) {
        SendMessageRevokeParams sendMessageRevokeParams = params;
        String str = this.LOG_TAG;
        IMSLog.s(str, "SendMessageRevokeRequest - " + sendMessageRevokeParams);
        UserAgent ua = getUserAgent(sendMessageRevokeParams.mOwnImsi);
        String mUri = null;
        if (ua == null) {
            Log.e(this.LOG_TAG, "sendDispositionNotification(): UserAgent not found.");
            AsyncResult.forMessage(sendMessageRevokeParams.mCallback, ImError.ENGINE_ERROR, (Throwable) null);
            sendMessageRevokeParams.mCallback.sendToTarget();
            return;
        }
        int registrationHandle = ua.getHandle();
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        if (sendMessageRevokeParams.mUri != null) {
            mUri = sendMessageRevokeParams.mUri.toString();
        }
        int imdnIdOffset = builder.createString((CharSequence) parseStr(sendMessageRevokeParams.mImdnId));
        int uriOffset = builder.createString((CharSequence) mUri);
        int convIdOffset = builder.createString((CharSequence) parseStr(sendMessageRevokeParams.mConversationId));
        int contIdOffset = builder.createString((CharSequence) parseStr(sendMessageRevokeParams.mContributionId));
        try {
            RequestSendMessageRevokeRequest.startRequestSendMessageRevokeRequest(builder);
            RequestSendMessageRevokeRequest.addImdnMessageId(builder, imdnIdOffset);
            RequestSendMessageRevokeRequest.addRegistrationHandle(builder, (long) registrationHandle);
            RequestSendMessageRevokeRequest.addService(builder, 1);
            RequestSendMessageRevokeRequest.addUri(builder, uriOffset);
            if (sendMessageRevokeParams.mConversationId != null) {
                RequestSendMessageRevokeRequest.addConversationId(builder, convIdOffset);
            }
            if (sendMessageRevokeParams.mContributionId != null) {
                RequestSendMessageRevokeRequest.addContributionId(builder, contIdOffset);
            }
            int offset = RequestSendMessageRevokeRequest.endRequestSendMessageRevokeRequest(builder);
            Request.startRequest(builder);
            Request.addReqid(builder, Id.REQUEST_SEND_MSG_REVOKE_REQUEST);
            Request.addReqType(builder, (byte) 100);
            Request.addReq(builder, offset);
            int i = contIdOffset;
            sendRequestToStack(Id.REQUEST_SEND_MSG_REVOKE_REQUEST, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(28, sendMessageRevokeParams.mCallback), ua);
        } catch (NullPointerException e) {
            int i2 = contIdOffset;
            Log.i(this.LOG_TAG, "Discard handleSendMessageRevokeRequest() :");
        }
    }

    private void handleAddParticipantsRequest(AddParticipantsParams params) {
        String str;
        AddParticipantsParams addParticipantsParams = params;
        IMSLog.s(this.LOG_TAG, "handleAddParticipantsRequest: " + addParticipantsParams);
        ImSession session = this.mSessions.get(addParticipantsParams.mRawHandle);
        if (session == null) {
            Log.e(this.LOG_TAG, "handleAddParticipantsRequest: Session not exist.");
            if (addParticipantsParams.mCallback != null) {
                sendCallback(addParticipantsParams.mCallback, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        if (addParticipantsParams.mCallback != null) {
            addParticipantsParams.mCallback.obj = addParticipantsParams.mReceivers;
            session.mAddParticipantsCallbacks.put(addParticipantsParams.mReqKey, addParticipantsParams.mCallback);
        }
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleAddParticipantsRequest: User agent not found.");
            if (addParticipantsParams.mCallback != null) {
                sendCallback(addParticipantsParams.mCallback, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int[] receiversOffsets = new int[addParticipantsParams.mReceivers.size()];
        Iterator<ImsUri> it = addParticipantsParams.mReceivers.iterator();
        int index = 0;
        while (true) {
            str = "";
            if (it.hasNext() == 0) {
                break;
            }
            ImsUri recipient = it.next();
            int index2 = index + 1;
            if (recipient != null) {
                str = recipient.toString();
            }
            receiversOffsets[index] = builder.createString((CharSequence) str);
            index = index2;
        }
        int reqKeyOffset = builder.createString((CharSequence) addParticipantsParams.mReqKey);
        if (addParticipantsParams.mSubject != null) {
            str = addParticipantsParams.mSubject;
        }
        int subjectOffset = builder.createString((CharSequence) str);
        int receiverOffset = RequestUpdateParticipants.createReceiverVector(builder, receiversOffsets);
        RequestUpdateParticipants.startRequestUpdateParticipants(builder);
        RequestUpdateParticipants.addReceiver(builder, receiverOffset);
        RequestUpdateParticipants.addReqKey(builder, reqKeyOffset);
        RequestUpdateParticipants.addSubject(builder, subjectOffset);
        RequestUpdateParticipants.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(builder, 0);
        int requestUpdateParticipantOffset = RequestUpdateParticipants.endRequestUpdateParticipants(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(builder, (byte) 54);
        Request.addReq(builder, requestUpdateParticipantOffset);
        int i = requestUpdateParticipantOffset;
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(12), ua);
    }

    private void handleRemoveParticipantsRequest(RemoveParticipantsParams params) {
        int ReqKeyOffset;
        RemoveParticipantsParams removeParticipantsParams = params;
        IMSLog.s(this.LOG_TAG, "handleRemoveParticipantsRequest: " + removeParticipantsParams);
        ImSession session = this.mSessions.get(removeParticipantsParams.mRawHandle);
        if (session == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsRequest: Session not exist.");
            if (removeParticipantsParams.mCallback != null) {
                sendCallback(removeParticipantsParams.mCallback, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        if (removeParticipantsParams.mCallback != null) {
            removeParticipantsParams.mCallback.obj = removeParticipantsParams.mRemovedParticipants;
            session.mRemoveParticipantsCallbacks.put(removeParticipantsParams.mReqKey, removeParticipantsParams.mCallback);
        }
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsRequest: User agent not found.");
            if (removeParticipantsParams.mCallback != null) {
                sendCallback(removeParticipantsParams.mCallback, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int[] receiversOffsets = new int[removeParticipantsParams.mRemovedParticipants.size()];
        Iterator<ImsUri> it = removeParticipantsParams.mRemovedParticipants.iterator();
        int index = 0;
        while (it.hasNext() != 0) {
            ImsUri recipient = it.next();
            int index2 = index + 1;
            receiversOffsets[index] = builder.createString((CharSequence) recipient != null ? recipient.toString() : "");
            index = index2;
        }
        if (removeParticipantsParams.mReqKey != null) {
            ReqKeyOffset = builder.createString((CharSequence) removeParticipantsParams.mReqKey);
        } else {
            ReqKeyOffset = -1;
        }
        int receiverOffset = RequestUpdateParticipants.createReceiverVector(builder, receiversOffsets);
        RequestUpdateParticipants.startRequestUpdateParticipants(builder);
        RequestUpdateParticipants.addReceiver(builder, receiverOffset);
        if (ReqKeyOffset != -1) {
            RequestUpdateParticipants.addReqKey(builder, ReqKeyOffset);
        }
        RequestUpdateParticipants.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(builder, 1);
        int requestUpdateParticipantOffset = RequestUpdateParticipants.endRequestUpdateParticipants(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(builder, (byte) 54);
        Request.addReq(builder, requestUpdateParticipantOffset);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(21), ua);
    }

    private void handleChangeGroupChatLeaderRequest(ChangeGroupChatLeaderParams params) {
        int ReqKeyOffset;
        ChangeGroupChatLeaderParams changeGroupChatLeaderParams = params;
        IMSLog.s(this.LOG_TAG, "handleChangeGroupChatLeaderRequest: " + changeGroupChatLeaderParams);
        ImSession session = this.mSessions.get(changeGroupChatLeaderParams.mRawHandle);
        if (session == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderRequest: Session not exist.");
            if (changeGroupChatLeaderParams.mCallback != null) {
                sendCallback(changeGroupChatLeaderParams.mCallback, ImError.TRANSACTION_DOESNT_EXIST);
            }
        } else if (changeGroupChatLeaderParams.mLeader == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderRequest: Leader info not exist.");
            if (changeGroupChatLeaderParams.mCallback != null) {
                sendCallback(changeGroupChatLeaderParams.mCallback, ImError.ENGINE_ERROR);
            }
        } else {
            if (changeGroupChatLeaderParams.mCallback != null) {
                changeGroupChatLeaderParams.mCallback.obj = changeGroupChatLeaderParams.mLeader;
                session.mChangeGCLeaderCallbacks.put(changeGroupChatLeaderParams.mReqKey, changeGroupChatLeaderParams.mCallback);
            }
            UserAgent ua = getUserAgent(session.mUaHandle);
            if (ua == null) {
                Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderRequest: User agent not found.");
                if (changeGroupChatLeaderParams.mCallback != null) {
                    sendCallback(changeGroupChatLeaderParams.mCallback, ImError.ENGINE_ERROR);
                    return;
                }
                return;
            }
            FlatBufferBuilder builder = new FlatBufferBuilder(0);
            int[] receiversOffsets = new int[changeGroupChatLeaderParams.mLeader.size()];
            Iterator<ImsUri> it = changeGroupChatLeaderParams.mLeader.iterator();
            while (it.hasNext()) {
                ImsUri recipient = it.next();
                receiversOffsets[0] = builder.createString((CharSequence) recipient != null ? recipient.toString() : "");
            }
            if (changeGroupChatLeaderParams.mReqKey != null) {
                ReqKeyOffset = builder.createString((CharSequence) changeGroupChatLeaderParams.mReqKey);
            } else {
                ReqKeyOffset = -1;
            }
            int receiverOffset = RequestUpdateParticipants.createReceiverVector(builder, receiversOffsets);
            RequestUpdateParticipants.startRequestUpdateParticipants(builder);
            RequestUpdateParticipants.addReceiver(builder, receiverOffset);
            if (ReqKeyOffset != -1) {
                RequestUpdateParticipants.addReqKey(builder, ReqKeyOffset);
            }
            RequestUpdateParticipants.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
            RequestUpdateParticipants.addReqType(builder, 2);
            int requestUpdateParticipantOffset = RequestUpdateParticipants.endRequestUpdateParticipants(builder);
            Request.startRequest(builder);
            Request.addReqid(builder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
            Request.addReqType(builder, (byte) 54);
            Request.addReq(builder, requestUpdateParticipantOffset);
            sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(19), ua);
        }
    }

    private void handleChangeGroupChatSubjectRequest(ChangeGroupChatSubjectParams params) {
        int ReqKeyOffset;
        IMSLog.s(this.LOG_TAG, "handleChangeGcSubjectRequest: " + params);
        ImSession session = this.mSessions.get(Integer.valueOf(((Integer) params.mRawHandle).intValue()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleChangeGcSubjectRequest: Session not exist.");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        if (params.mCallback != null) {
            params.mCallback.obj = params.mSubject;
            session.mChangeGCSubjectCallbacks.put(params.mReqKey, params.mCallback);
        }
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsRequest: User agent not found.");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int subjectOffset = builder.createString((CharSequence) params.mSubject);
        if (params.mReqKey != null) {
            ReqKeyOffset = builder.createString((CharSequence) params.mReqKey);
        } else {
            ReqKeyOffset = -1;
        }
        RequestUpdateParticipants.startRequestUpdateParticipants(builder);
        if (ReqKeyOffset != -1) {
            RequestUpdateParticipants.addReqKey(builder, ReqKeyOffset);
        }
        RequestUpdateParticipants.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(builder, 4);
        RequestUpdateParticipants.addSubject(builder, subjectOffset);
        int requestUpdateParticipantOffset = RequestUpdateParticipants.endRequestUpdateParticipants(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(builder, (byte) 54);
        Request.addReq(builder, requestUpdateParticipantOffset);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(22), ua);
    }

    private void handleChangeGroupChatIconRequest(ChangeGroupChatIconParams params) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "onChangeGroupChatIcon: " + params);
        ImSession session = this.mSessions.get(Integer.valueOf(((Integer) params.mRawHandle).intValue()));
        if (session == null) {
            Log.e(this.LOG_TAG, "onChangeGroupChatIcon: Session does not exist.");
            return;
        }
        if (params.mCallback != null) {
            params.mCallback.obj = params.mIconPath;
            session.mChangeGCIconCallbacks.put(params.mReqKey, params.mCallback);
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int fileOffset = -1;
        if (params.mIconPath != null) {
            String mimetype = "";
            int extPos = params.mIconPath.lastIndexOf(".");
            if (extPos >= 0) {
                String extension = params.mIconPath.substring(extPos + 1, params.mIconPath.length());
                if (ContentTypeTranslator.isTranslationDefined(extension)) {
                    mimetype = ContentTypeTranslator.translate(extension);
                }
            }
            int IconPathOffset = -1;
            if (params.mIconPath != null) {
                IconPathOffset = builder.createString((CharSequence) params.mIconPath);
            }
            int mimetypeOffset = -1;
            if (mimetype != null) {
                mimetypeOffset = builder.createString((CharSequence) mimetype);
            }
            ImFileAttr.startImFileAttr(builder);
            if (IconPathOffset != -1) {
                ImFileAttr.addPath(builder, IconPathOffset);
            }
            if (mimetypeOffset != -1) {
                ImFileAttr.addContentType(builder, mimetypeOffset);
            }
            ImFileAttr.addSize(builder, new File(params.mIconPath).length());
            fileOffset = ImFileAttr.endImFileAttr(builder);
        }
        int ReqKeyOffset = -1;
        if (params.mReqKey != null) {
            ReqKeyOffset = builder.createString((CharSequence) params.mReqKey);
        }
        RequestUpdateParticipants.startRequestUpdateParticipants(builder);
        if (params.mIconPath != null) {
            RequestUpdateParticipants.addIconAttr(builder, fileOffset);
        }
        RequestUpdateParticipants.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(builder, 5);
        if (ReqKeyOffset != -1) {
            RequestUpdateParticipants.addReqKey(builder, ReqKeyOffset);
        }
        int requestUpdateParticipantOffset = RequestUpdateParticipants.endRequestUpdateParticipants(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(builder, (byte) 54);
        Request.addReq(builder, requestUpdateParticipantOffset);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(30));
    }

    private void handleChangeGroupChatAliasRequest(ChangeGroupAliasParams params) {
        int ReqKeyOffset;
        IMSLog.s(this.LOG_TAG, "handleChangeGcAliasRequest: " + params);
        ImSession session = this.mSessions.get(Integer.valueOf(((Integer) params.mRawHandle).intValue()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasRequest: Session not exist.");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        if (params.mCallback != null) {
            params.mCallback.obj = params.mAlias;
            session.mChangeGCAliasCallbacks.put(params.mReqKey, params.mCallback);
        }
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasRequest: User agent not found.");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        if (params.mReqKey != null) {
            ReqKeyOffset = builder.createString((CharSequence) params.mReqKey);
        } else {
            ReqKeyOffset = -1;
        }
        int aliasOffset = builder.createString((CharSequence) parseStr(params.mAlias));
        RequestUpdateParticipants.startRequestUpdateParticipants(builder);
        if (ReqKeyOffset != -1) {
            RequestUpdateParticipants.addReqKey(builder, ReqKeyOffset);
        }
        RequestUpdateParticipants.addSessionHandle(builder, (long) session.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(builder, 3);
        RequestUpdateParticipants.addUserAlias(builder, aliasOffset);
        int requestUpdateParticipantOffset = RequestUpdateParticipants.endRequestUpdateParticipants(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(builder, (byte) 54);
        Request.addReq(builder, requestUpdateParticipantOffset);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(23), ua);
    }

    private void handleStartFtSessionRequest(SendFtSessionParams sendParams) {
        String str;
        String str2;
        int imdnIdOffset;
        int confUriOffset;
        int imdnIdOffset2;
        int confUriOffset2;
        int fileNameOffset;
        int contentTypeOffset;
        int spamDateOffset;
        int imdnIdOffset3;
        int deviceNameOffset;
        int reliableMessageOffset;
        int fileFingerprintOffset;
        String mimetype;
        String mimetype2;
        Iterator<ImsUri> it;
        String str3;
        SendFtSessionParams sendFtSessionParams = sendParams;
        IMSLog.s(this.LOG_TAG, "handleStartFtSessionRequest: " + sendFtSessionParams);
        UserAgent ua = getUserAgent("ft", sendFtSessionParams.mOwnImsi);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleStartFtSessionRequest(): UserAgent not found.");
            FtResult result = new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null);
            if (sendFtSessionParams.mCallback != null) {
                sendCallback(sendFtSessionParams.mCallback, result);
                return;
            }
            return;
        }
        FtSession ftSession = new FtSession();
        ftSession.mId = sendFtSessionParams.mMessageId;
        ftSession.mStartCallback = sendFtSessionParams.mCallback;
        ftSession.mStartSessionHandleCallback = sendFtSessionParams.mSessionHandleCallback;
        ftSession.mUaHandle = ua.getHandle();
        this.mPendingFtSessions.put(sendFtSessionParams.mFileTransferID, ftSession);
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int fileTransferIdOffset = builder.createString((CharSequence) parseStr(sendFtSessionParams.mFileTransferID));
        int userAliasOffset = builder.createString((CharSequence) parseStr(sendFtSessionParams.mUserAlias));
        int contributionIdOffset = builder.createString((CharSequence) parseStr(sendFtSessionParams.mContributionId));
        int conversationIdOffset = builder.createString((CharSequence) parseStr(sendFtSessionParams.mConversationId));
        int inReplyToContributionIdOffset = builder.createString((CharSequence) parseStr(sendFtSessionParams.mInReplyToContributionId));
        int fileNameOffset2 = builder.createString((CharSequence) parseStr(sendFtSessionParams.mFileName));
        int filePathOffset = builder.createString((CharSequence) parseStr(adjustFilePath(sendFtSessionParams.mFilePath)));
        int contentTypeOffset2 = builder.createString((CharSequence) parseStr(sendFtSessionParams.mContentType));
        int fileFingerprintOffset2 = builder.createString((CharSequence) parseStr(sendFtSessionParams.mFileFingerPrint));
        int deviceNameOffset2 = builder.createString((CharSequence) parseStr(sendFtSessionParams.mDeviceName));
        int reliableMessageOffset2 = builder.createString((CharSequence) parseStr(sendFtSessionParams.mReliableMessage));
        FtSession ftSession2 = ftSession;
        int spamFromOffset = builder.createString((CharSequence) sendFtSessionParams.mReportMsgParams != null ? sendFtSessionParams.mReportMsgParams.getSpamFrom().toString() : "");
        UserAgent ua2 = ua;
        if (sendFtSessionParams.mReportMsgParams != null) {
            str = sendFtSessionParams.mReportMsgParams.getSpamTo().toString();
        } else {
            str = "";
        }
        int spamToOffset = builder.createString((CharSequence) str);
        int reliableMessageOffset3 = reliableMessageOffset2;
        if (sendFtSessionParams.mReportMsgParams != null) {
            str2 = sendFtSessionParams.mReportMsgParams.getSpamDate();
        } else {
            str2 = "";
        }
        int spamDateOffset2 = builder.createString((CharSequence) str2);
        int deviceNameOffset3 = deviceNameOffset2;
        int imdnIdOffset4 = builder.createString((CharSequence) sendFtSessionParams.mImdnId);
        int fileFingerprintOffset3 = fileFingerprintOffset2;
        int imdnNotiVectorOffset = ImdnParams.createNotiVector(builder, ResipTranslatorCollection.translateFwImdnNoti(sendFtSessionParams.mDispositionNotification));
        int dateTimeOffset = builder.createString((CharSequence) Iso8601.formatMillis(sendFtSessionParams.mImdnTime));
        if (sendFtSessionParams.mConfUri != null) {
            imdnIdOffset = imdnIdOffset4;
            confUriOffset = builder.createString((CharSequence) sendFtSessionParams.mConfUri.toString());
            imdnIdOffset2 = -1;
        } else {
            int[] receiversVector = new int[sendFtSessionParams.mRecipients.size()];
            int i = 0;
            imdnIdOffset = imdnIdOffset4;
            Iterator<ImsUri> it2 = sendFtSessionParams.mRecipients.iterator();
            while (it2.hasNext()) {
                ImsUri recipient = it2.next();
                int i2 = i + 1;
                if (recipient != null) {
                    it = it2;
                    str3 = recipient.toString();
                } else {
                    it = it2;
                    str3 = "";
                }
                receiversVector[i] = builder.createString((CharSequence) str3);
                i = i2;
                it2 = it;
            }
            int receiversOffset = BaseSessionData.createReceiversVector(builder, receiversVector);
            confUriOffset = -1;
            imdnIdOffset2 = receiversOffset;
        }
        BaseSessionData.startBaseSessionData(builder);
        BaseSessionData.addId(builder, fileTransferIdOffset);
        int i3 = fileTransferIdOffset;
        BaseSessionData.addIsConference(builder, sendFtSessionParams.mConfUri != null);
        if (sendFtSessionParams.mConfUri != null) {
            BaseSessionData.addSessionUri(builder, confUriOffset);
        } else {
            BaseSessionData.addReceivers(builder, imdnIdOffset2);
        }
        if (sendFtSessionParams.mUserAlias != null) {
            BaseSessionData.addUserAlias(builder, userAliasOffset);
        }
        if (sendFtSessionParams.mContributionId != null) {
            BaseSessionData.addContributionId(builder, contributionIdOffset);
        }
        if (sendFtSessionParams.mConversationId != null) {
            BaseSessionData.addConversationId(builder, conversationIdOffset);
        }
        if (sendFtSessionParams.mInReplyToContributionId != null) {
            BaseSessionData.addInReplyToContributionId(builder, inReplyToContributionIdOffset);
        }
        int baseSessionDataOffset = BaseSessionData.endBaseSessionData(builder);
        ImFileAttr.startImFileAttr(builder);
        ImFileAttr.addName(builder, fileNameOffset2);
        ImFileAttr.addPath(builder, filePathOffset);
        ImFileAttr.addContentType(builder, contentTypeOffset2);
        int i4 = contentTypeOffset2;
        int receiversOffset2 = imdnIdOffset2;
        ImFileAttr.addSize(builder, (long) ((int) sendFtSessionParams.mFileSize));
        if (sendFtSessionParams.mIsResuming) {
            confUriOffset2 = confUriOffset;
            fileNameOffset = fileNameOffset2;
            ImFileAttr.addStart(builder, sendFtSessionParams.mTransferredBytes + 1 > sendFtSessionParams.mFileSize ? sendFtSessionParams.mFileSize : sendFtSessionParams.mTransferredBytes + 1);
            ImFileAttr.addEnd(builder, sendFtSessionParams.mFileSize);
        } else {
            confUriOffset2 = confUriOffset;
            fileNameOffset = fileNameOffset2;
            ImFileAttr.addStart(builder, 0);
            ImFileAttr.addEnd(builder, 0);
        }
        ImFileAttr.addTimeDuration(builder, (long) sendFtSessionParams.mTimeDuration);
        int imFileAttrOffset = ImFileAttr.endImFileAttr(builder);
        if (sendFtSessionParams.mThumbPath == null || sendFtSessionParams.mDirection != ImDirection.OUTGOING) {
            contentTypeOffset = -1;
        } else {
            int extPos = sendFtSessionParams.mThumbPath.lastIndexOf(".");
            if (extPos >= 0) {
                int i5 = filePathOffset;
                mimetype2 = "";
                String extension = sendFtSessionParams.mThumbPath.substring(extPos + 1, sendFtSessionParams.mThumbPath.length());
                if (ContentTypeTranslator.isTranslationDefined(extension)) {
                    mimetype = ContentTypeTranslator.translate(extension);
                    int filePathOffset2 = builder.createString((CharSequence) parseStr(adjustFilePath(sendFtSessionParams.mThumbPath)));
                    int contentTypeOffset3 = builder.createString((CharSequence) parseStr(mimetype));
                    ImFileAttr.startImFileAttr(builder);
                    ImFileAttr.addPath(builder, filePathOffset2);
                    ImFileAttr.addContentType(builder, contentTypeOffset3);
                    int i6 = filePathOffset2;
                    String str4 = mimetype;
                    int i7 = extPos;
                    ImFileAttr.addSize(builder, new File(sendFtSessionParams.mThumbPath).length());
                    int i8 = contentTypeOffset3;
                    contentTypeOffset = ImFileAttr.endImFileAttr(builder);
                }
            } else {
                mimetype2 = "";
            }
            mimetype = mimetype2;
            int filePathOffset22 = builder.createString((CharSequence) parseStr(adjustFilePath(sendFtSessionParams.mThumbPath)));
            int contentTypeOffset32 = builder.createString((CharSequence) parseStr(mimetype));
            ImFileAttr.startImFileAttr(builder);
            ImFileAttr.addPath(builder, filePathOffset22);
            ImFileAttr.addContentType(builder, contentTypeOffset32);
            int i62 = filePathOffset22;
            String str42 = mimetype;
            int i72 = extPos;
            ImFileAttr.addSize(builder, new File(sendFtSessionParams.mThumbPath).length());
            int i82 = contentTypeOffset32;
            contentTypeOffset = ImFileAttr.endImFileAttr(builder);
        }
        ReportMessageHdr.startReportMessageHdr(builder);
        ReportMessageHdr.addSpamFrom(builder, spamFromOffset);
        ReportMessageHdr.addSpamTo(builder, spamToOffset);
        ReportMessageHdr.addSpamDate(builder, spamDateOffset2);
        int reportMsgHdrOffset = ReportMessageHdr.endReportMessageHdr(builder);
        if (sendFtSessionParams.mReportMsgParams != null) {
            String str5 = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            spamDateOffset = spamDateOffset2;
            sb.append("andleStartFtSessionRequest, mReportMsgParams=");
            sb.append(sendFtSessionParams.mReportMsgParams);
            Log.i(str5, sb.toString());
        } else {
            spamDateOffset = spamDateOffset2;
        }
        ImdnParams.startImdnParams(builder);
        if (sendFtSessionParams.mImdnId != null) {
            imdnIdOffset3 = imdnIdOffset;
            ImdnParams.addMessageId(builder, imdnIdOffset3);
        } else {
            imdnIdOffset3 = imdnIdOffset;
        }
        ImdnParams.addNoti(builder, imdnNotiVectorOffset);
        ImdnParams.addDatetime(builder, dateTimeOffset);
        int imdnNotiVectorOffset2 = ImdnParams.endImdnParams(builder);
        FtPayloadParam.startFtPayloadParam(builder);
        int imdnIdOffset5 = imdnIdOffset3;
        FtPayloadParam.addIsPush(builder, sendFtSessionParams.mDirection == ImDirection.OUTGOING);
        FtPayloadParam.addExtraFt(builder, sendFtSessionParams.mExtraFt);
        FtPayloadParam.addIsPublicAccountMsg(builder, sendFtSessionParams.mIsPublicAccountMsg);
        int fileFingerprintOffset4 = fileFingerprintOffset3;
        FtPayloadParam.addFileFingerPrint(builder, fileFingerprintOffset4);
        if (sendFtSessionParams.mDeviceName != null) {
            deviceNameOffset = deviceNameOffset3;
            FtPayloadParam.addDeviceName(builder, deviceNameOffset);
        } else {
            deviceNameOffset = deviceNameOffset3;
        }
        int deviceNameOffset4 = deviceNameOffset;
        if (sendFtSessionParams.mReliableMessage != null) {
            reliableMessageOffset = reliableMessageOffset3;
            FtPayloadParam.addReliableMessage(builder, reliableMessageOffset);
        } else {
            reliableMessageOffset = reliableMessageOffset3;
        }
        FtPayloadParam.addFileAttr(builder, imFileAttrOffset);
        int reliableMessageOffset4 = reliableMessageOffset;
        if (sendFtSessionParams.mThumbPath != null) {
            fileFingerprintOffset = fileFingerprintOffset4;
            if (sendFtSessionParams.mDirection == ImDirection.OUTGOING) {
                FtPayloadParam.addIconAttr(builder, contentTypeOffset);
            }
        } else {
            fileFingerprintOffset = fileFingerprintOffset4;
        }
        FtPayloadParam.addImdn(builder, imdnNotiVectorOffset2);
        int ftPayloadOffset = FtPayloadParam.endFtPayloadParam(builder);
        RequestStartFtSession.startRequestStartFtSession(builder);
        int imdnParamsOffset = imdnNotiVectorOffset2;
        RequestStartFtSession.addRegistrationHandle(builder, (long) ua2.getHandle());
        RequestStartFtSession.addSessionData(builder, baseSessionDataOffset);
        RequestStartFtSession.addReportData(builder, reportMsgHdrOffset);
        RequestStartFtSession.addPayload(builder, ftPayloadOffset);
        int rsfsOffset = RequestStartFtSession.endRequestStartFtSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_FT_START_SESSION);
        Request.addReqType(builder, (byte) 46);
        Request.addReq(builder, rsfsOffset);
        int rsfsOffset2 = rsfsOffset;
        int reliableMessageOffset5 = reliableMessageOffset4;
        int reliableMessageOffset6 = deviceNameOffset4;
        int dateTimeOffset2 = dateTimeOffset;
        int i9 = rsfsOffset2;
        int rsfsOffset3 = imdnParamsOffset;
        int imdnParamsOffset2 = reliableMessageOffset5;
        int i10 = spamDateOffset;
        int imdnNotiVectorOffset3 = imdnNotiVectorOffset;
        int imdnNotiVectorOffset4 = i10;
        int i11 = ftPayloadOffset;
        int i12 = reportMsgHdrOffset;
        int imdnIdOffset6 = receiversOffset2;
        int receiversOffset3 = imdnIdOffset5;
        int i13 = contentTypeOffset;
        int i14 = confUriOffset2;
        int i15 = imdnNotiVectorOffset3;
        int imdnNotiVectorOffset5 = dateTimeOffset2;
        int dateTimeOffset3 = fileFingerprintOffset;
        int fileFingerprintOffset5 = i15;
        int i16 = fileNameOffset;
        int fileNameOffset3 = imFileAttrOffset;
        sendRequestToStack(Id.REQUEST_FT_START_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(8), ua2);
    }

    private void handleStartFtMediaRequest(int sessionHandle) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleStartFtMediaRequest(): file transdfer session handle = " + sessionHandle);
        FtSession session = this.mFtSessions.get(Integer.valueOf(sessionHandle));
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleStartFtMediaRequest(): UserAgent not found. UaHandle = " + session.mUaHandle);
            FtResult result = new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null);
            if (session.mStartCallback != null) {
                sendCallback(session.mStartCallback, result);
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestStartMedia.startRequestStartMedia(builder);
        RequestStartMedia.addSessionId(builder, (long) sessionHandle);
        int offset = RequestStartMedia.endRequestStartMedia(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_FT_START_MEDIA);
        Request.addReqType(builder, (byte) 42);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_FT_START_MEDIA, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(18), ua);
    }

    private void handleAcceptFtSessionRequest(AcceptFtSessionParams params) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleAcceptFtSessionRequest(): " + params);
        Integer sessionHandle = (Integer) params.mRawHandle;
        FtSession session = this.mFtSessions.get(sessionHandle);
        if (session == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleAcceptFtSessionRequest(): no session in map, return accept failure, id = " + sessionHandle);
            if (params.mCallback != null) {
                sendCallback(params.mCallback, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                params.mCallback = null;
                return;
            }
            return;
        }
        session.mAcceptCallback = params.mCallback;
        session.mId = params.mMessageId;
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleAcceptFtSessionRequest(): User agent not found!");
            if (params.mCallback != null) {
                sendCallback(params.mCallback, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                params.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int filePathOffset = builder.createString((CharSequence) parseStr(adjustFilePath(params.mFilePath)));
        int userAliasOffset = builder.createString((CharSequence) params.mUserAlias != null ? params.mUserAlias : "");
        RequestAcceptFtSession.startRequestAcceptFtSession(builder);
        RequestAcceptFtSession.addSessionHandle(builder, (long) session.mHandle);
        RequestAcceptFtSession.addStart(builder, params.mStart);
        RequestAcceptFtSession.addEnd(builder, params.mEnd);
        RequestAcceptFtSession.addFilePath(builder, filePathOffset);
        RequestAcceptFtSession.addUserAlias(builder, userAliasOffset);
        int offset = RequestAcceptFtSession.endRequestAcceptFtSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_FT_ACCEPT_SESSION);
        Request.addReqType(builder, (byte) 48);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_FT_ACCEPT_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(5), ua);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0030, code lost:
        r1 = (java.lang.Integer) r7.mRawHandle;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCancelFtSessionRequest(com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams r7) {
        /*
            r6 = this;
            java.lang.String r0 = r6.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleCancelFtSessionRequest: "
            r1.append(r2)
            r1.append(r7)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r0 = 0
            java.util.Map<java.lang.String, com.sec.internal.ims.core.handler.secims.ResipImHandler$FtSession> r1 = r6.mPendingFtSessions
            java.lang.String r2 = r7.mFileTransferId
            java.lang.Object r1 = r1.get(r2)
            r0 = r1
            com.sec.internal.ims.core.handler.secims.ResipImHandler$FtSession r0 = (com.sec.internal.ims.core.handler.secims.ResipImHandler.FtSession) r0
            if (r0 == 0) goto L_0x002e
            java.lang.String r1 = r6.LOG_TAG
            java.lang.String r2 = "handleCancelFtSessionRequest(): pending session - postpone"
            android.util.Log.i(r1, r2)
            r0.mCancelParams = r7
            return
        L_0x002e:
            if (r0 != 0) goto L_0x005c
            java.lang.Object r1 = r7.mRawHandle
            java.lang.Integer r1 = (java.lang.Integer) r1
            java.util.Map<java.lang.Integer, com.sec.internal.ims.core.handler.secims.ResipImHandler$FtSession> r2 = r6.mFtSessions
            java.lang.Object r2 = r2.get(r1)
            r0 = r2
            com.sec.internal.ims.core.handler.secims.ResipImHandler$FtSession r0 = (com.sec.internal.ims.core.handler.secims.ResipImHandler.FtSession) r0
            if (r0 != 0) goto L_0x005c
            java.lang.String r2 = r6.LOG_TAG
            java.lang.String r3 = "handleCancelFtSessionRequest(): unknown session!"
            android.util.Log.i(r2, r3)
            android.os.Message r2 = r7.mCallback
            if (r2 == 0) goto L_0x005b
            android.os.Message r2 = r7.mCallback
            com.sec.internal.constants.ims.servicemodules.im.result.FtResult r3 = new com.sec.internal.constants.ims.servicemodules.im.result.FtResult
            com.sec.internal.constants.ims.servicemodules.im.ImError r4 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r5 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.ENGINE_ERROR
            r3.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r4, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r5, (java.lang.Object) r1)
            r6.sendCallback(r2, r3)
            r2 = 0
            r7.mCallback = r2
        L_0x005b:
            return
        L_0x005c:
            com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams r1 = r0.mCancelParams
            if (r1 == 0) goto L_0x0068
            java.lang.String r1 = r6.LOG_TAG
            java.lang.String r2 = "handleCancelFtSessionRequest(): there is a ongoing cancel request, ignore further cancel request"
            android.util.Log.i(r1, r2)
            return
        L_0x0068:
            r0.mCancelParams = r7
            r6.sendFtCancelRequestToStack(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImHandler.handleCancelFtSessionRequest(com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams):void");
    }

    /* access modifiers changed from: protected */
    public void sendFtCancelRequestToStack(FtSession session) {
        RejectFtSessionParams params = session.mCancelParams;
        if (params == null) {
            Log.e(this.LOG_TAG, "sendFtCancelRequestToStack(): null reject params!");
            return;
        }
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "sendFtCancelRequestToStack(): User agent not found!");
            if (session.mCancelParams.mCallback != null) {
                sendCallback(session.mCancelParams.mCallback, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                session.mCancelParams.mCallback = null;
                return;
            }
            return;
        }
        FtRejectReason reason = params.mRejectReason != null ? params.mRejectReason : FtRejectReason.DECLINE;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int warningTxtOffset = builder.createString((CharSequence) parseStr(reason.getWarningText()));
        WarningHdr.startWarningHdr(builder);
        WarningHdr.addCode(builder, reason.getWarningCode());
        WarningHdr.addText(builder, warningTxtOffset);
        int warningHdrOffset = WarningHdr.endWarningHdr(builder);
        RequestCancelFtSession.startRequestCancelFtSession(builder);
        RequestCancelFtSession.addSessionHandle(builder, (long) session.mHandle);
        RequestCancelFtSession.addSipCode(builder, reason.getSipCode());
        RequestCancelFtSession.addWarningHdr(builder, warningHdrOffset);
        int offset = RequestCancelFtSession.endRequestCancelFtSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_FT_CANCEL_SESSION);
        Request.addReqType(builder, (byte) 47);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_FT_CANCEL_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(6), ua);
    }

    /* access modifiers changed from: protected */
    public void handleRejectFtSessionRequest(RejectFtSessionParams rejectParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleRejectFtSessionRequest: " + rejectParams);
        Integer sessionHandle = (Integer) rejectParams.mRawHandle;
        FtSession session = this.mFtSessions.get(sessionHandle);
        if (session == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleRejectFtSessionRequest: no session in map, return reject failure id=" + sessionHandle);
            if (rejectParams.mCallback != null) {
                sendCallback(rejectParams.mCallback, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                return;
            }
            return;
        }
        session.mCancelParams = rejectParams;
        FtRejectReason reason = rejectParams.mRejectReason != null ? rejectParams.mRejectReason : FtRejectReason.DECLINE;
        UserAgent ua = getUserAgent(session.mUaHandle);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleRejectFtSessionRequest(): User Agent not found!");
            if (rejectParams.mCallback != null) {
                sendCallback(rejectParams.mCallback, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                rejectParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int warningTxtOffset = builder.createString((CharSequence) parseStr(reason.getWarningText()));
        WarningHdr.startWarningHdr(builder);
        WarningHdr.addCode(builder, reason.getWarningCode());
        WarningHdr.addText(builder, warningTxtOffset);
        int warningHdrOffset = WarningHdr.endWarningHdr(builder);
        RequestCancelFtSession.startRequestCancelFtSession(builder);
        RequestCancelFtSession.addSessionHandle(builder, (long) session.mHandle);
        RequestCancelFtSession.addSipCode(builder, reason.getSipCode());
        RequestCancelFtSession.addWarningHdr(builder, warningHdrOffset);
        int offset = RequestCancelFtSession.endRequestCancelFtSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_FT_CANCEL_SESSION);
        Request.addReqType(builder, (byte) 47);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_FT_CANCEL_SESSION, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(7), ua);
    }

    private void handleSetMoreInfoToSipUARequest(String info, int uaHandle) {
        String str = this.LOG_TAG;
        Log.i(str, "handleSetMoreInfoToSipUARequest: " + info);
        if (!TextUtils.isEmpty(info)) {
            UserAgent ua = getUserAgent(uaHandle);
            if (ua == null) {
                Log.e(this.LOG_TAG, "handleSetMoreInfoToSipUARequest(): User Agent not found!");
                return;
            }
            FlatBufferBuilder builder = new FlatBufferBuilder(0);
            int valueOffset = builder.createString((CharSequence) info != null ? info : "");
            RequestImSetMoreInfoToSipUA.startRequestImSetMoreInfoToSipUA(builder);
            RequestImSetMoreInfoToSipUA.addValue(builder, valueOffset);
            int offset = RequestImSetMoreInfoToSipUA.endRequestImSetMoreInfoToSipUA(builder);
            Request.startRequest(builder);
            Request.addReqid(builder, Id.REQUEST_IM_SET_MORE_INFO_TO_SIP_UA);
            Request.addReqType(builder, (byte) 59);
            Request.addReq(builder, offset);
            sendRequestToStack(Id.REQUEST_IM_SET_MORE_INFO_TO_SIP_UA, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(29), ua);
        }
    }

    private void handleReportChatbotAsSpam(ReportChatbotAsSpamParams param) {
        ReportChatbotAsSpamParams reportChatbotAsSpamParams = param;
        Log.i(this.LOG_TAG, "handleReportChatbotAsSpam");
        ImsUri chatbotUrl = reportChatbotAsSpamParams.mChatbotUri;
        String xmlInfo = reportChatbotAsSpamParams.mSpamInfo;
        String request_id = reportChatbotAsSpamParams.mRequestId;
        if (TextUtils.isEmpty(chatbotUrl.toString())) {
            Log.e(this.LOG_TAG, "handleReportChatbotAsSpam - Invalid ChatBotUrl");
            return;
        }
        UserAgent ua = getUserAgent(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, reportChatbotAsSpamParams.mPhoneId);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleReportChatbotAsSpam(): User Agent not found!");
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int urlOffset = builder.createString((CharSequence) chatbotUrl.toString());
        int xmlOffset = builder.createString((CharSequence) xmlInfo);
        int reuestOffset = builder.createString((CharSequence) request_id);
        RequestReportChatbotAsSpam.startRequestReportChatbotAsSpam(builder);
        RequestReportChatbotAsSpam.addRegistrationHandle(builder, (long) ua.getHandle());
        RequestReportChatbotAsSpam.addChatbotUri(builder, urlOffset);
        RequestReportChatbotAsSpam.addSpamInfo(builder, xmlOffset);
        RequestReportChatbotAsSpam.addRequestId(builder, reuestOffset);
        int offset = RequestReportChatbotAsSpam.endRequestReportChatbotAsSpam(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 1400);
        Request.addReqType(builder, (byte) 53);
        Request.addReq(builder, offset);
        sendRequestToStack(1400, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(31), ua);
    }

    private void handleRequestChatbotAnonymize(ChatbotAnonymizeParams param) {
        ChatbotAnonymizeParams chatbotAnonymizeParams = param;
        Log.i(this.LOG_TAG, "handleRequestChatbotAnonymize");
        ImsUri chatbotUri = chatbotAnonymizeParams.mChatbotUri;
        String aliasXml = chatbotAnonymizeParams.mAliasXml;
        String commandId = chatbotAnonymizeParams.mCommandId;
        if (TextUtils.isEmpty(chatbotUri.toString())) {
            Log.e(this.LOG_TAG, "handleRequestChatbotAnonymize - Invalid ChatBotUrl");
            return;
        }
        UserAgent ua = getUserAgent(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, chatbotAnonymizeParams.mPhoneId);
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleRequestChatbotAnonymize(): User Agent not found!");
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int urlOffset = builder.createString((CharSequence) chatbotUri.toString());
        int xmlOffset = builder.createString((CharSequence) aliasXml);
        int commandIdOffset = builder.createString((CharSequence) commandId);
        RequestChatbotAnonymize.startRequestChatbotAnonymize(builder);
        RequestChatbotAnonymize.addRegistrationHandle(builder, (long) ua.getHandle());
        RequestChatbotAnonymize.addChatbotUri(builder, urlOffset);
        RequestChatbotAnonymize.addAnonymizeInfo(builder, xmlOffset);
        RequestChatbotAnonymize.addCommandId(builder, commandIdOffset);
        int offset = RequestChatbotAnonymize.endRequestChatbotAnonymize(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_CHATBOT_ANONYMIZE);
        Request.addReqType(builder, (byte) 52);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_CHATBOT_ANONYMIZE, builder, Request.endRequest(builder), this.mStackResponseHandler.obtainMessage(32), ua);
    }

    private void sendRequestToStack(int id, FlatBufferBuilder request, int offset, Message callback) {
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(this.LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            sendRequestToStack(id, request, offset, callback, ua);
        }
    }

    private void sendRequestToStack(int id, FlatBufferBuilder request, int offset, Message callback, UserAgent ua) {
        if (ua == null) {
            Log.e(this.LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            ua.sendRequestToStack(new ResipStackRequest(id, request, offset, callback));
        }
    }

    private static String adjustFilePath(String path) {
        return path;
    }

    /* access modifiers changed from: protected */
    public void sendCallback(Message callback, Object object) {
        AsyncResult.forMessage(callback, object, (Throwable) null);
        callback.sendToTarget();
    }

    private int[] getStringOffsetArray(FlatBufferBuilder builder, Iterable<String> stringList, int size) {
        int[] stringArray = new int[size];
        int i = 0;
        for (String str : stringList) {
            if (str != null && !str.isEmpty()) {
                stringArray[i] = builder.createString((CharSequence) str);
                i++;
            }
        }
        return stringArray;
    }

    private int[] getImsUriOffsetArray(FlatBufferBuilder builder, Iterable<ImsUri> stringList, int size) {
        int[] stringArray = new int[size];
        int i = 0;
        for (ImsUri uri : stringList) {
            if (uri != null && !uri.toString().isEmpty()) {
                stringArray[i] = builder.createString((CharSequence) uri.toString());
                i++;
            }
        }
        return stringArray;
    }

    private String parseStr(String str) {
        return str != null ? str : "";
    }

    public void subscribeGroupChatList(int version, boolean increaseMode, String ownImsi) {
        Log.i(this.LOG_TAG, "subscribeGroupChatList()");
        sendMessage(obtainMessage(24, new GroupChatListParams(version, increaseMode, ownImsi)));
    }

    public void subscribeGroupChatInfo(Uri uri, String ownImsi) {
        String str = this.LOG_TAG;
        Log.i(str, "subscribeGroupChatInfo() uri:" + uri.toString());
        sendMessage(obtainMessage(25, new GroupChatInfoParams(uri, ownImsi)));
    }

    public void registerForGroupChatListUpdate(Handler h, int what, Object obj) {
        this.mGroupChatListRegistrants.add(new Registrant(h, what, obj));
    }

    public void unRegisterForGroupChatListUpdate(Handler h) {
        this.mGroupChatListRegistrants.remove(h);
    }

    public void registerForGroupChatInfoUpdate(Handler h, int what, Object obj) {
        this.mGroupChatInfoRegistrants.add(new Registrant(h, what, obj));
    }

    public void unRegisterForGroupChatInfoUpdate(Handler h) {
        this.mGroupChatInfoRegistrants.remove(h);
    }

    public void registerForMessageRevokeResponse(Handler h, int what, Object obj) {
        this.mMessageRevokeResponseRegistransts.add(new Registrant(h, what, obj));
    }

    public void unregisterForMessageRevokeResponse(Handler h) {
        this.mMessageRevokeResponseRegistransts.remove(h);
    }

    public void registerForSendMessageRevokeDone(Handler h, int what, Object obj) {
        this.mSendMessageRevokeResponseRegistransts.add(new Registrant(h, what, obj));
    }

    public void unregisterForSendMessageRevokeDone(Handler h) {
        this.mSendMessageRevokeResponseRegistransts.remove(h);
    }

    public void setMoreInfoToSipUserAgent(String info, int uaHandle) {
        sendMessage(obtainMessage(29, uaHandle, 0, info));
    }

    public void requestChatbotAnonymize(ChatbotAnonymizeParams param) {
        sendMessage(obtainMessage(32, param));
    }

    public void registerForChatbotAnonymizeResp(Handler h, int what, Object obj) {
        this.mChatbotAnonymizeResponseRegistrants.add(h, what, obj);
    }

    public void unregisterForChatbotAnonymizeResp(Handler h) {
        this.mChatbotAnonymizeResponseRegistrants.remove(h);
    }

    public void registerForChatbotAnonymizeNotify(Handler h, int what, Object obj) {
        this.mChatbotAnonymizeNotifyRegistrants.add(h, what, obj);
    }

    public void unregisterForChatbotAnonymizeNotify(Handler h) {
        this.mChatbotAnonymizeNotifyRegistrants.remove(h);
    }

    public void reportChatbotAsSpam(ReportChatbotAsSpamParams param) {
        sendMessage(obtainMessage(31, param));
    }

    public void registerForChatbotAsSpamNotify(Handler h, int what, Object obj) {
        this.mReportChatbotAsSpamRespRegistrants.add(h, what, obj);
    }

    public void unregisterForChatbotAsSpamNotify(Handler h) {
        this.mReportChatbotAsSpamRespRegistrants.remove(h);
    }

    public UserAgent getUserAgent(String service, String ownImsi) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi(service, ownImsi);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent(String ownImsi) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("im", ownImsi);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent(String service, int phoneId) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(service, phoneId);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent(int uaHandle) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(uaHandle);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent() {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent("im");
    }

    /* access modifiers changed from: protected */
    public String getImsiByUserAgent(UserAgent ua) {
        return this.mImsFramework.getRegistrationManager().getImsiByUserAgent(ua);
    }
}
