package com.sec.internal.ims.core.handler.secims;

import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatbotXmlUtils;
import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.MessageRevokeResponse;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import com.sec.internal.constants.ims.servicemodules.im.event.ChatbotAnonymizeNotifyEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ChatbotAnonymizeRespEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingGroupChatListEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ReportChatbotAsSpamRespEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendMessageFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.RejectImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StopImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.ims.core.handler.BaseHandler;
import com.sec.internal.ims.core.handler.secims.ResipImHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AllowHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.GroupChatInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImComposingStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUser;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnRecRoute;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.FtIncomingSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.FtProgress;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.GroupChatInfoUpdated;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.GroupChatListUpdated;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImComposingStatusReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.Icon;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.SubjectExt;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImMessageReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImMessageReportReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImSessionInvited;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.MessageRevokeResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReportChatbotAsSpamResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RequestChatbotAnonymizeResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RequestChatbotAnonymizeResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SendMessageRevokeResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SessionClosed;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SessionEstablished;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SessionStarted;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CloseSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendImMessageResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendMessageRevokeInternalResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.StartSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.UpdateParticipantsResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.RetryHdr;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

public class ResipImResponseHandler extends BaseHandler {
    private static final String GROUPCHAT_ROLE_ADMIN = "Administrator";
    private static final String GROUPCHAT_ROLE_CHAIRMAN = "chairman";
    ResipImHandler mResipImHandler;

    ResipImResponseHandler(Looper looper, ResipImHandler resipImHandler) {
        super(looper);
        this.mResipImHandler = resipImHandler;
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 12) {
            handleAddParticipantsResponse((UpdateParticipantsResponse) ((AsyncResult) msg.obj).result);
        } else if (i == 17) {
            handleRejectImSessionResponse((CloseSessionResponse) ((AsyncResult) msg.obj).result);
        } else if (i == 19) {
            handleChangeGroupChatLeaderResponse((UpdateParticipantsResponse) ((AsyncResult) msg.obj).result);
        } else if (i != 100) {
            switch (i) {
                case 1:
                    handleStartImSessionResponse((StartSessionResponse) ((AsyncResult) msg.obj).result);
                    return;
                case 2:
                    handleAcceptImSessionResponse((StartSessionResponse) ((AsyncResult) msg.obj).result);
                    return;
                case 3:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    handleCloseImSessionResponse((CloseSessionResponse) ar.result, (Message) ar.userObj);
                    return;
                case 4:
                    handleSendMessageResponse((SendImMessageResponse) ((AsyncResult) msg.obj).result);
                    return;
                case 5:
                    handleAcceptFtSessionResponse((StartSessionResponse) ((AsyncResult) msg.obj).result);
                    return;
                case 6:
                    handleCancelFtSessionResponse((CloseSessionResponse) ((AsyncResult) msg.obj).result);
                    return;
                case 7:
                    handleRejectFtSessionResponse((CloseSessionResponse) ((AsyncResult) msg.obj).result);
                    return;
                case 8:
                    handleStartFtSessionResponse((StartSessionResponse) ((AsyncResult) msg.obj).result);
                    return;
                default:
                    switch (i) {
                        case 21:
                            handleRemoveParticipantsResponse((UpdateParticipantsResponse) ((AsyncResult) msg.obj).result);
                            return;
                        case 22:
                            handleChangeGroupChatSubjectResponse((UpdateParticipantsResponse) ((AsyncResult) msg.obj).result);
                            return;
                        case 23:
                            handleChangeGroupChatAliasResponse((UpdateParticipantsResponse) ((AsyncResult) msg.obj).result);
                            return;
                        case 24:
                            handleSubscribeGroupChatListResponse((GeneralResponse) ((AsyncResult) msg.obj).result);
                            return;
                        case 25:
                            handleSubscribeGroupChatInfoResponse((GeneralResponse) ((AsyncResult) msg.obj).result);
                            return;
                        default:
                            switch (i) {
                                case 28:
                                    AsyncResult ar2 = (AsyncResult) msg.obj;
                                    String str = this.LOG_TAG;
                                    Log.i(str, "EVENT_SEND_MESSAGE_REVOKE_REQUEST: " + msg);
                                    handleSendMessageRevokeInternalResponse((Message) ar2.userObj, (SendMessageRevokeInternalResponse) ar2.result);
                                    return;
                                case 29:
                                    handleSetMoreInfoToSipUAResponse((GeneralResponse) ((AsyncResult) msg.obj).result);
                                    return;
                                case 30:
                                    handleChangeGroupChatIconResponse((UpdateParticipantsResponse) ((AsyncResult) msg.obj).result);
                                    return;
                                default:
                                    String str2 = this.LOG_TAG;
                                    Log.i(str2, "mStackResponseHandler.handleMessage(): unhandled event - " + msg);
                                    return;
                            }
                    }
            }
        } else {
            handleNotify((Notify) ((AsyncResult) msg.obj).result);
        }
    }

    private void handleStartImSessionResponse(StartSessionResponse response) {
        if (response == null) {
            Log.e(this.LOG_TAG, "response object is null!!");
            return;
        }
        int sessionHandle = (int) response.sessionHandle();
        String fwSessionId = response.fwSessionId();
        Result result = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null);
        ImError imError = result.getImError();
        String str = this.LOG_TAG;
        Log.i(str, "handleStartImSessionResponse(): sessionHandle = " + sessionHandle + ", fwSessionId = " + fwSessionId + ", error = " + imError);
        ResipImHandler.ImSession session = this.mResipImHandler.mPendingSessions.remove(fwSessionId);
        if (session == null) {
            Log.e(this.LOG_TAG, "handleStartImSessionResponse(): cannot find session!");
        } else if (imError == ImError.SUCCESS) {
            session.mSessionHandle = Integer.valueOf(sessionHandle);
            this.mResipImHandler.mSessions.put(Integer.valueOf(sessionHandle), session);
            String str2 = this.LOG_TAG;
            Log.i(str2, "handleStartImSessionResponse(): sessionHandle = " + sessionHandle + ", fwSessionId = " + fwSessionId + ", error = " + imError);
            if (session.mStartSyncCallback != null) {
                this.mResipImHandler.sendCallback(session.mStartSyncCallback, Integer.valueOf(sessionHandle));
                session.mStartSyncCallback = null;
            }
        } else {
            if (session.mStartSyncCallback != null) {
                this.mResipImHandler.sendCallback(session.mStartSyncCallback, fwSessionId);
                session.mStartSyncCallback = null;
            }
            session.mStartProvisionalCallback = null;
            if (session.mStartCallback != null) {
                this.mResipImHandler.sendCallback(session.mStartCallback, new StartImSessionResult(result, (ImsUri) null, fwSessionId));
                session.mStartCallback = null;
            }
            if (session.mFirstMessageCallback != null) {
                if (imError == ImError.BUSY_HERE) {
                    Log.i(this.LOG_TAG, "handle 486 response as SUCCESS for the message in INVITE.");
                    this.mResipImHandler.sendCallback(session.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(sessionHandle), new Result(ImError.SUCCESS, result)));
                } else {
                    this.mResipImHandler.sendCallback(session.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(sessionHandle), result));
                }
                session.mFirstMessageCallback = null;
            }
        }
    }

    private void handleAcceptImSessionResponse(StartSessionResponse response) {
        Log.e(this.LOG_TAG, "handleAcceptImSessionResponse() called!");
        if (response == null) {
            Log.e(this.LOG_TAG, "handleAcceptImSessionResponse(): response is null!!");
            return;
        }
        int sessionHandle = (int) response.sessionHandle();
        Result result = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null);
        ImError imError = result.getImError();
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleAcceptImSessionResponse(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleAcceptImSessionResponse(): sessionHandle = " + sessionHandle + ", chat id = " + session.mChatId + ", error = " + imError);
        if (session.mAcceptCallback != null) {
            this.mResipImHandler.sendCallback(session.mAcceptCallback, new StartImSessionResult(result, (ImsUri) null, Integer.valueOf(sessionHandle)));
            session.mAcceptCallback = null;
        }
    }

    private void handleSendMessageResponse(SendImMessageResponse response) {
        Log.i(this.LOG_TAG, "handleSendMessageResponse()");
        Integer sessionHandle = Integer.valueOf((int) response.sessionId());
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(sessionHandle);
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleSendMessageResponse(): no session found sessionHandle=" + sessionHandle);
            return;
        }
        Message sendMessageCallback = session.findAndRemoveCallback(response.imdnMessageId());
        if (sendMessageCallback == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleSendMessageResponse(): no response callback set. sessionHandle = " + sessionHandle + " imdnid = " + response.imdnMessageId());
            return;
        }
        this.mResipImHandler.sendCallback(sendMessageCallback, new SendMessageResult(sessionHandle, ResipTranslatorCollection.translateImResult(response.imError(), (Object) null)));
    }

    private void handleChangeGroupChatLeaderResponse(UpdateParticipantsResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGroupChatLeaderResponse: " + response);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf((int) response.sessionHandle()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderResponse(): no session found");
        } else if (TextUtils.isEmpty(response.reqKey())) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
            Message callback = session.mChangeGCLeaderCallbacks.remove(response.reqKey());
            if (callback != null) {
                this.mResipImHandler.sendCallback(callback, imError);
            } else {
                Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderResponse(): no callback set");
            }
        }
    }

    private void handleAddParticipantsResponse(UpdateParticipantsResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleAddParticipantsResponse: " + response);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf((int) response.sessionHandle()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleAddParticipantsResponse(): no session found");
        } else if (TextUtils.isEmpty(response.reqKey())) {
            Log.e(this.LOG_TAG, "handleAddParticipantsResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), response.warningHdr()).getImError();
            Message callback = session.mAddParticipantsCallbacks.remove(response.reqKey());
            if (callback != null) {
                this.mResipImHandler.sendCallback(callback, imError);
            } else {
                Log.e(this.LOG_TAG, "handleAddParticipantsResponse(): no callback set");
            }
        }
    }

    private void handleRemoveParticipantsResponse(UpdateParticipantsResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleRemoveParticipantsResponse: " + response);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf((int) response.sessionHandle()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsResponse(): no session found");
        } else if (TextUtils.isEmpty(response.reqKey())) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), response.warningHdr()).getImError();
            Message callback = session.mRemoveParticipantsCallbacks.remove(response.reqKey());
            if (callback != null) {
                this.mResipImHandler.sendCallback(callback, imError);
            } else {
                Log.e(this.LOG_TAG, "handleRemoveParticipantsResponse(): no callback set");
            }
        }
    }

    private void handleStartFtSessionResponse(StartSessionResponse response) {
        int sessionHandle = (int) response.sessionHandle();
        String fileTransferId = response.fwSessionId();
        Result reason = ResipTranslatorCollection.translateFtResult(response.imError(), (Object) null);
        String str = this.LOG_TAG;
        Log.i(str, "handleStartFtSessionResponse(): sessionHandle = " + sessionHandle + ", FT id = " + fileTransferId + ", error = " + reason);
        ResipImHandler.FtSession ftSession = this.mResipImHandler.mPendingFtSessions.remove(fileTransferId);
        if (ftSession == null) {
            Log.e(this.LOG_TAG, "handleStartFtSessionResponse: cannot find session!");
        } else if (reason.getImError() == ImError.SUCCESS) {
            ftSession.mHandle = sessionHandle;
            if (ftSession.mStartSessionHandleCallback != null) {
                this.mResipImHandler.sendCallback(ftSession.mStartSessionHandleCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
                ftSession.mStartSessionHandleCallback = null;
            }
            this.mResipImHandler.mFtSessions.put(Integer.valueOf(sessionHandle), ftSession);
            if (ftSession.mCancelParams != null) {
                Log.i(this.LOG_TAG, "handleStartFtSessionResponse(): send postponed cancel request");
                this.mResipImHandler.sendFtCancelRequestToStack(ftSession);
            }
        } else if (ftSession.mStartCallback != null) {
            this.mResipImHandler.sendCallback(ftSession.mStartCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
            ftSession.mStartCallback = null;
        }
    }

    private void handleAcceptFtSessionResponse(StartSessionResponse response) {
        Log.e(this.LOG_TAG, "handleAcceptFtSessionResponse() called!");
        int sessionHandle = (int) response.sessionHandle();
        Result reason = ResipTranslatorCollection.translateFtResult(response.imError(), (Object) null);
        ResipImHandler.FtSession session = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleAcceptFtSessionResponse(): no session found sessionHandle = " + sessionHandle + ", result = " + reason);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleAcceptFtSessionResponse(): sessionHandle = " + sessionHandle + ", result = " + reason);
        if (reason.getImError() == ImError.SUCCESS) {
            Log.i(this.LOG_TAG, "handleAcceptFtSessionResponse INVITE response sent");
        } else if (session.mAcceptCallback != null) {
            this.mResipImHandler.sendCallback(session.mAcceptCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
            session.mAcceptCallback = null;
        }
    }

    private void handleCancelFtSessionResponse(CloseSessionResponse response) {
        int sessionHandle = (int) response.sessionHandle();
        Result reason = ResipTranslatorCollection.translateFtResult(response.imError(), (Object) null);
        ResipImHandler.FtSession session = this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleCancelFtSessionResponse(): cannot find ftsession sessionHandle = " + sessionHandle + ", result = " + reason);
            return;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "handleCancelFtSessionResponse(): sessionHandle = " + sessionHandle + ", result = " + reason);
        if (!(session.mCancelParams == null || session.mCancelParams.mCallback == null)) {
            this.mResipImHandler.sendCallback(session.mCancelParams.mCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
            session.mCancelParams.mCallback = null;
        }
        session.mCancelParams = null;
    }

    private void handleRejectFtSessionResponse(CloseSessionResponse response) {
        int sessionHandle = (int) response.sessionHandle();
        Result reason = ResipTranslatorCollection.translateFtResult(response.imError(), (Object) null);
        ResipImHandler.FtSession session = this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleRejectFtSessionResponse(): cannot find session sessionHandle = " + sessionHandle + ", result = " + reason);
            return;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "handleRejectFtSessionResponse(): sessionHandle = " + sessionHandle + ", result = " + reason);
        if (session.mCancelParams != null && session.mCancelParams.mCallback != null) {
            this.mResipImHandler.sendCallback(session.mCancelParams.mCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
            session.mCancelParams.mCallback = null;
        }
    }

    private void handleRejectImSessionResponse(CloseSessionResponse response) {
        int sessionHandle = (int) response.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.remove(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleRejectImSessionResponse(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "handleRejectImSessionResponse(): sessionHandle = " + sessionHandle + ", chat id = " + session.mChatId + ", error = " + imError);
        if (session.mRejectCallback != null) {
            this.mResipImHandler.sendCallback(session.mRejectCallback, new RejectImSessionResult(Integer.valueOf(sessionHandle), imError));
            session.mRejectCallback = null;
        }
    }

    private void handleCloseImSessionResponse(CloseSessionResponse response, Message callback) {
        Log.e(this.LOG_TAG, "handleCloseImSessionResponse() called!");
        int sessionHandle = (int) response.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleCloseImSessionResponse(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleCloseImSessionResponse(): sessionHandle = " + sessionHandle + ", chat id = " + session.mChatId + ", error = " + imError);
        if (callback != null) {
            this.mResipImHandler.sendCallback(callback, new StopImSessionResult(Integer.valueOf(sessionHandle), imError));
        }
    }

    private void handleChangeGroupChatSubjectResponse(UpdateParticipantsResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGcSubjectResponse: " + response);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf((int) response.sessionHandle()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleChangeGcSubjectResponse(): no session found");
        } else if (TextUtils.isEmpty(response.reqKey())) {
            Log.e(this.LOG_TAG, "handleChangeGcSubjectResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
            Message callback = session.mChangeGCSubjectCallbacks.remove(response.reqKey());
            if (callback != null) {
                this.mResipImHandler.sendCallback(callback, imError);
            } else {
                Log.e(this.LOG_TAG, "handleChangeGcSubjectResponse(): no callback set");
            }
        }
    }

    private void handleChangeGroupChatIconResponse(UpdateParticipantsResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGroupChatIconResponse: " + response);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf((int) response.sessionHandle()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatIconResponse(): no session found");
            return;
        }
        ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
        Message callback = session.mChangeGCIconCallbacks.remove(response.reqKey());
        if (callback != null) {
            this.mResipImHandler.sendCallback(callback, imError);
        } else {
            Log.e(this.LOG_TAG, "handleChangeGroupChatIconResponse(): no callback set");
        }
    }

    private void handleChangeGroupChatAliasResponse(UpdateParticipantsResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGcAliasResponse: " + response);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf((int) response.sessionHandle()));
        if (session == null) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasResponse(): no session found");
        } else if (TextUtils.isEmpty(response.reqKey())) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
            Message callback = session.mChangeGCAliasCallbacks.remove(response.reqKey());
            if (callback != null) {
                this.mResipImHandler.sendCallback(callback, imError);
            } else {
                Log.e(this.LOG_TAG, "handleChangeGcAliasResponse(): no callback set");
            }
        }
    }

    private void handleSubscribeGroupChatListResponse(GeneralResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleSubscribeGroupChatListResponse: " + response);
    }

    private void handleSubscribeGroupChatInfoResponse(GeneralResponse response) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleSubscribeGroupChatInfoResponse: " + response);
    }

    private void handleSendMessageRevokeInternalResponse(Message msg, SendMessageRevokeInternalResponse response) {
        String str = this.LOG_TAG;
        Log.i(str, "handleSendMessageRevokeInternalResponse() msg : " + msg + "response : " + response);
        if (response != null) {
            ImError imError = ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError();
            if (msg != null) {
                this.mResipImHandler.sendCallback(msg, imError);
            }
        }
    }

    private void handleSetMoreInfoToSipUAResponse(GeneralResponse response) {
        String str = this.LOG_TAG;
        Log.i(str, "handleSetMoreInfoToSipUAResponse: " + response);
    }

    private void handleNotify(Notify notify) {
        int notifyid = notify.notifyid();
        if (notifyid != 19000) {
            switch (notifyid) {
                case Id.NOTIFY_IM_SESSION_STARTED /*11001*/:
                    handleImSessionStartedNotify(notify);
                    return;
                case Id.NOTIFY_IM_SESSION_CLOSED /*11002*/:
                    handleImSessionClosedNotify(notify);
                    return;
                case Id.NOTIFY_IM_SESSION_ESTABLISHED /*11003*/:
                    handleImSessionEstablishedNotify(notify);
                    return;
                case Id.NOTIFY_IM_INCOMING_SESSION /*11004*/:
                    handleIncomingSessionNotify(notify);
                    return;
                case Id.NOTIFY_IM_MESSAGE_RECEIVED /*11005*/:
                    handleImMessageReceivedNotify(notify);
                    return;
                default:
                    switch (notifyid) {
                        case Id.NOTIFY_IM_COMPOSING_STATUS_RECEIVED /*11007*/:
                            handleImComposingStatusReceivedNotify(notify);
                            return;
                        case Id.NOTIFY_IM_MESSAGE_REPORT_RECEIVED /*11008*/:
                            handleImMessageReportReceivedNotify(notify);
                            return;
                        case Id.NOTIFY_GROUP_CHAT_SUBSCRIBE_STATUS /*11009*/:
                            handleGroupChatSubscribeStatusNotify();
                            return;
                        case Id.NOTIFY_GROUP_LIST_UPDATED /*11010*/:
                            handleGroupChatListNotify(notify);
                            return;
                        case Id.NOTIFY_GROUP_INFO_UPDATED /*11011*/:
                            handleGroupChatInfoNotify(notify);
                            return;
                        case Id.NOTIFY_IM_SESSION_PROVISIONAL_RESPONSE /*11012*/:
                            handleImSessionProvisionalResponseNotify(notify);
                            return;
                        case Id.NOTIFY_MESSAGE_REVOKE_RESPONSE_RECEIVED /*11013*/:
                            handleMessageRevokeResponseReceivedNotify(notify);
                            return;
                        case Id.NOTIFY_SEND_MESSAGE_REVOKE_RESPONSE /*11014*/:
                            handleSendMessageRevokeResponseNotify(notify);
                            return;
                        default:
                            switch (notifyid) {
                                case Id.NOTIFY_FT_SESSION_STARTED /*12001*/:
                                    handleFtSessionStartedNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_SESSION_CLOSED /*12002*/:
                                    handleFtSessionClosedNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_SESSION_ESTABLISHED /*12003*/:
                                    handleFtSessionEstablishedNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_PROGRESS /*12004*/:
                                    handleFtProgressNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_INCOMING_SESSION /*12005*/:
                                    handleFtIncomingSessionNotify(notify);
                                    return;
                                default:
                                    switch (notifyid) {
                                        case Id.NOTIFY_REPORT_CHATBOT_AS_SPAM_RESPONSE /*20011*/:
                                            handleReportChatbotAsSpamResponseNotify(notify);
                                            return;
                                        case Id.NOTIFY_REQUEST_CHATBOT_ANONYMIZE_RESPONSE /*20012*/:
                                            handleRequestChatbotAnonymizeResp(notify);
                                            return;
                                        case Id.NOTIFY_REQUEST_CHATBOT_ANONYMIZE_RESPONSE_RECEIVED /*20013*/:
                                            handleRequestChatbotAnonymizeNotify(notify);
                                            return;
                                        default:
                                            Log.w(this.LOG_TAG, "handleNotify(): unexpected id");
                                            return;
                                    }
                            }
                    }
            }
        } else {
            handleImConferenceInfoUpdateNotify(notify);
        }
    }

    private void handleIncomingSessionNotify(Notify notify) {
        Log.i(this.LOG_TAG, "handleIncomingSessionNotify()");
        boolean isTokenUsed = false;
        if (notify.notiType() != 30) {
            Log.e(this.LOG_TAG, "handleIncomingSessionNotify(): invalid notify");
            return;
        }
        ImSessionInvited invite = (ImSessionInvited) notify.noti(new ImSessionInvited());
        UserAgent ua = this.mResipImHandler.getUserAgent((int) invite.userHandle());
        if (ua == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleIncomingSessionNotify(): UserAgent not found. UserHandle = " + ((int) invite.userHandle()));
            return;
        }
        ImSessionParam imSessionParam = invite.session();
        if (imSessionParam == null) {
            Log.e(this.LOG_TAG, "handleIncomingSessionNotify(): invalid notify data");
            return;
        }
        BaseSessionData baseSessionData = imSessionParam.baseSessionData();
        ImMessageParam imMessageParam = invite.messageParam();
        if (baseSessionData == null) {
            Log.e(this.LOG_TAG, "handleIncomingSessionNotify(): invalid notify data");
            return;
        }
        ImIncomingSessionEvent event = new ImIncomingSessionEvent();
        Integer sessionHandle = Integer.valueOf((int) baseSessionData.sessionHandle());
        event.mRawHandle = sessionHandle;
        event.mOwnImsi = this.mResipImHandler.getImsiByUserAgent(ua);
        event.mIsDeferred = invite.isDeferred();
        event.mIsForStoredNoti = invite.isForStoredNoti();
        this.mResipImHandler.mSessions.put(sessionHandle, new ResipImHandler.ImSession(sessionHandle.intValue(), invite.isDeferred(), ua.getHandle()));
        event.mIsMsgRevokeSupported = imSessionParam.isMsgRevokeSupported();
        event.mIsMsgFallbackSupported = imSessionParam.isMsgFallbackSupported();
        event.mIsSendOnly = imSessionParam.isSendOnly();
        event.mIsChatbotRole = baseSessionData.isChatbotParticipant();
        event.mInitiator = ImsUri.parse(imSessionParam.sender());
        if (event.mIsChatbotRole && event.mInitiator != null && event.mInitiator.getParam("tk") != null && event.mInitiator.getParam("tk").equals("on")) {
            isTokenUsed = true;
        }
        event.mIsTokenUsed = isTokenUsed;
        if (!event.mIsDeferred || baseSessionData.isConference()) {
            event.mRecipients = new ArrayList();
            for (int i = 0; i < baseSessionData.receiversLength(); i++) {
                event.mRecipients.add(ImsUri.parse(baseSessionData.receivers(i)));
            }
        } else {
            event.mRecipients = new ArrayList();
            event.mRecipients.add(event.mInitiator);
        }
        if (baseSessionData.isConference() != 0) {
            event.mSessionType = ImIncomingSessionEvent.ImSessionType.CONFERENCE;
            event.mIsClosedGroupChat = imSessionParam.isClosedGroupchat();
            event.mSessionUri = ImsUri.parse(baseSessionData.sessionUri());
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "handleIncomingSessionNotify(): session uri = " + event.mSessionUri);
        } else {
            event.mSessionType = ImIncomingSessionEvent.ImSessionType.SINGLE;
            event.mInitiatorAlias = baseSessionData.userAlias();
            event.mSessionUri = null;
        }
        if (baseSessionData.sdpContentType() != null && !baseSessionData.sdpContentType().isEmpty()) {
            event.mSdpContentType = baseSessionData.sdpContentType();
        }
        if (baseSessionData.serviceId() != null && !baseSessionData.serviceId().isEmpty()) {
            event.mServiceId = baseSessionData.serviceId();
        }
        if (!(imMessageParam == null || imMessageParam.imdn() == null)) {
            event.mDeviceId = imMessageParam.imdn().deviceId();
        }
        event.mSubject = (imSessionParam.subject() == null || imSessionParam.subject().isEmpty()) ? null : imSessionParam.subject();
        event.mServiceType = ImIncomingSessionEvent.ImServiceType.NORMAL;
        event.mIsParticipantNtfy = false;
        event.mConversationId = (baseSessionData.conversationId() == null || baseSessionData.conversationId().isEmpty()) ? null : baseSessionData.conversationId();
        event.mContributionId = (baseSessionData.contributionId() == null || baseSessionData.contributionId().isEmpty()) ? null : baseSessionData.contributionId();
        if (baseSessionData.sessionReplaces() == null || baseSessionData.sessionReplaces().isEmpty()) {
            event.mPrevContributionId = null;
        } else {
            event.mPrevContributionId = baseSessionData.sessionReplaces();
        }
        event.mInReplyToContributionId = baseSessionData.inReplyToContributionId();
        event.mRemoteMsrpAddress = (invite.remoteMsrpAddr() == null || invite.remoteMsrpAddr().isEmpty()) ? null : invite.remoteMsrpAddr();
        List<String> acceptTypes = new ArrayList<>();
        for (int i2 = 0; i2 < imSessionParam.acceptTypesLength(); i2++) {
            String content = imSessionParam.acceptTypes(i2);
            if (content != null) {
                acceptTypes.addAll(Arrays.asList(content.split(" ")));
            }
        }
        List<String> acceptWrappedTypes = new ArrayList<>();
        for (int i3 = 0; i3 < imSessionParam.acceptWrappedTypesLength(); i3++) {
            String content2 = imSessionParam.acceptWrappedTypes(i3);
            if (content2 != null) {
                acceptWrappedTypes.addAll(Arrays.asList(content2.split(" ")));
            }
        }
        event.mAcceptTypes = acceptTypes;
        event.mAcceptWrappedTypes = acceptWrappedTypes;
        if (invite.messageParam() != null) {
            ImIncomingMessageEvent msgEvent = parseImMessageParam(invite.messageParam());
            if (msgEvent != null) {
                msgEvent.mRawHandle = sessionHandle;
                msgEvent.mUserAlias = baseSessionData.userAlias();
                String str3 = this.LOG_TAG;
                Log.i(str3, "handleIncomingSessionNotify(): " + msgEvent);
            }
            event.mReceivedMessage = msgEvent;
        }
        AsyncResult result = new AsyncResult((Object) null, event, (Throwable) null);
        if (this.mResipImHandler.mIncomingSessionRegistrants.size() != 0) {
            this.mResipImHandler.mIncomingSessionRegistrants.notifyRegistrants(result);
            boolean z = isTokenUsed;
            ImSessionInvited imSessionInvited = invite;
        } else {
            String str4 = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            boolean z2 = isTokenUsed;
            sb.append("handleIncomingSessionNotify(): Empty registrants, reject handle=");
            sb.append(sessionHandle);
            Log.i(str4, sb.toString());
            ImSessionInvited imSessionInvited2 = invite;
            this.mResipImHandler.handleRejectImSessionRequest(new RejectImSessionParams((String) null, sessionHandle, ImSessionRejectReason.FORBIDDEN, (Message) null));
        }
        String str5 = this.LOG_TAG;
        Log.i(str5, "handleIncomingSessionNotify(): " + event);
    }

    private void handleImSessionStartedNotify(Notify notify) {
        ResipImHandler.ImSession session;
        if (notify.notiType() != 27) {
            Log.e(this.LOG_TAG, "handleImSessionStartedNotify(): invalid notify");
            return;
        }
        SessionStarted noti = (SessionStarted) notify.noti(new SessionStarted());
        int sessionHandle = (int) noti.sessionHandle();
        String sessionUri = noti.sessionUri();
        String displayName = noti.displayName();
        Result result = ResipTranslatorCollection.translateImResult(noti.imError(), noti.warningHdr());
        ImError imError = result.getImError();
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleImSessionStartedNotify(): sessionHandle = " + sessionHandle + ", error = " + imError + ", sessionUri = " + sessionUri + ", displayName = " + displayName);
        ResipImHandler.ImSession session2 = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (session2 == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleImSessionStartedNotify(): Unknown session handle: " + sessionHandle);
            return;
        }
        if (session2.mStartSyncCallback != null) {
            this.mResipImHandler.sendCallback(session2.mStartSyncCallback, Integer.valueOf(sessionHandle));
            session2.mStartSyncCallback = null;
        }
        if (session2.mStartCallback != null) {
            RetryHdr retryHdr = noti.retryHdr();
            AllowHdr allowHdr = noti.allowHdr();
            ResipImHandler resipImHandler = this.mResipImHandler;
            Message message = session2.mStartCallback;
            ImsUri parse = TextUtils.isEmpty(sessionUri) ? null : ImsUri.parse(sessionUri);
            Integer valueOf = Integer.valueOf(sessionHandle);
            int retryTimer = retryHdr != null ? retryHdr.retryTimer() : 0;
            String text = allowHdr != null ? allowHdr.text() : null;
            boolean isMsgRevokeSupported = noti.isMsgRevokeSupported();
            boolean isMsgFallbackSupported = noti.isMsgFallbackSupported();
            boolean isChatbotRole = noti.isChatbotRole();
            String str3 = displayName == null ? "" : displayName;
            SessionStarted sessionStarted = noti;
            StartImSessionResult startImSessionResult = r8;
            String str4 = sessionUri;
            ResipImHandler resipImHandler2 = resipImHandler;
            String str5 = text;
            String str6 = displayName;
            session = session2;
            StartImSessionResult startImSessionResult2 = new StartImSessionResult(result, parse, valueOf, retryTimer, str5, isMsgRevokeSupported, isMsgFallbackSupported, isChatbotRole, str3);
            resipImHandler2.sendCallback(message, startImSessionResult);
            session.mStartCallback = null;
        } else {
            String str7 = sessionUri;
            String str8 = displayName;
            session = session2;
        }
        if (session.mFirstMessageCallback != null) {
            if (imError == ImError.BUSY_HERE) {
                Log.i(this.LOG_TAG, "handle 486 response as SUCCESS for the message in INVITE.");
                this.mResipImHandler.sendCallback(session.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(sessionHandle), new Result(ImError.SUCCESS, result)));
            } else {
                this.mResipImHandler.sendCallback(session.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(sessionHandle), result));
            }
            session.mFirstMessageCallback = null;
        }
        if (imError != ImError.SUCCESS) {
            this.mResipImHandler.mSessions.remove(Integer.valueOf(sessionHandle));
        }
    }

    private void handleImSessionClosedNotify(Notify notify) {
        int sessionHandle;
        SessionClosed noti;
        if (notify.notiType() != 28) {
            Log.e(this.LOG_TAG, "handleImSessionClosedNotify(): invalid notify");
            return;
        }
        Log.i(this.LOG_TAG, "handleImSessionClosedNotify");
        SessionClosed noti2 = (SessionClosed) notify.noti(new SessionClosed());
        int sessionHandle2 = (int) noti2.sessionHandle();
        Result result = ResipTranslatorCollection.translateImResult(noti2.imError(), noti2.reasonHdr());
        ImError imError = result.getImError();
        String referredBy = noti2.referredBy();
        ImsUri referredByUri = null;
        if (referredBy != null) {
            referredBy = referredBy.replaceAll("\\<|\\>", "");
            String str = this.LOG_TAG;
            IMSLog.s(str, "handleImSessionClosedNotify() Referred By =" + IMSLog.checker(referredBy));
            referredByUri = ImsUri.parse(referredBy);
        }
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle2));
        if (session == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleImSessionClosedNotify(): no session found sessionHandle = " + sessionHandle2 + ", error = " + imError);
            return;
        }
        String str3 = this.LOG_TAG;
        IMSLog.s(str3, "handleImSessionClosedNotify(): sessionHandle = " + sessionHandle2 + ", chat id = " + session.mChatId + ", error = " + imError + ", referredBy = " + referredBy);
        if (!(imError == ImError.NORMAL_RELEASE || imError == ImError.NORMAL_RELEASE_GONE || imError == ImError.CONFERENCE_PARTY_BOOTED || imError == ImError.CONFERENCE_CALL_COMPLETED)) {
            Log.e(this.LOG_TAG, "handleImSessionClosedNotify(): abnormal close");
            if (session.mStartSyncCallback != null) {
                this.mResipImHandler.sendCallback(session.mStartSyncCallback, Integer.valueOf(sessionHandle2));
                session.mStartSyncCallback = null;
            }
            if (session.mStartCallback != null) {
                this.mResipImHandler.sendCallback(session.mStartCallback, new StartImSessionResult(result, (ImsUri) null, Integer.valueOf(sessionHandle2)));
                session.mStartCallback = null;
            }
            if (session.mFirstMessageCallback != null && imError == ImError.DEVICE_UNREGISTERED) {
                this.mResipImHandler.sendCallback(session.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(sessionHandle2), result));
                session.mFirstMessageCallback = null;
            } else if (session.mFirstMessageCallback != null) {
                this.mResipImHandler.sendCallback(session.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(sessionHandle2), new Result(ImError.SUCCESS, result)));
                session.mFirstMessageCallback = null;
            }
            if (session.mAcceptCallback != null) {
                this.mResipImHandler.sendCallback(session.mAcceptCallback, new StartImSessionResult(result, (ImsUri) null, Integer.valueOf(sessionHandle2)));
                session.mAcceptCallback = null;
            }
        }
        this.mResipImHandler.mSessionClosedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new ImSessionClosedEvent(Integer.valueOf(sessionHandle2), session.mChatId, result, referredByUri), (Throwable) null));
        ResipImHandler.ImSession removedSession = this.mResipImHandler.mSessions.remove(Integer.valueOf(sessionHandle2));
        if (removedSession != null) {
            for (Message callback : removedSession.mSendMessageCallbacks.values()) {
                if (callback != null) {
                    noti = noti2;
                    sessionHandle = sessionHandle2;
                    this.mResipImHandler.sendCallback(callback, new SendMessageResult(Integer.valueOf(sessionHandle2), new Result(ImError.NETWORK_ERROR, Result.Type.NETWORK_ERROR)));
                } else {
                    noti = noti2;
                    sessionHandle = sessionHandle2;
                }
                Notify notify2 = notify;
                noti2 = noti;
                sessionHandle2 = sessionHandle;
            }
            int i = sessionHandle2;
            for (Message callback2 : removedSession.mAddParticipantsCallbacks.values()) {
                if (callback2 != null) {
                    this.mResipImHandler.sendCallback(callback2, ImError.NETWORK_ERROR);
                }
            }
            for (Message callback3 : removedSession.mRemoveParticipantsCallbacks.values()) {
                if (callback3 != null) {
                    this.mResipImHandler.sendCallback(callback3, ImError.NETWORK_ERROR);
                }
            }
            for (Message callback4 : removedSession.mChangeGCAliasCallbacks.values()) {
                if (callback4 != null) {
                    this.mResipImHandler.sendCallback(callback4, ImError.NETWORK_ERROR);
                }
            }
            for (Message callback5 : removedSession.mChangeGCLeaderCallbacks.values()) {
                if (callback5 != null) {
                    this.mResipImHandler.sendCallback(callback5, ImError.NETWORK_ERROR);
                }
            }
            for (Message callback6 : removedSession.mChangeGCSubjectCallbacks.values()) {
                if (callback6 != null) {
                    this.mResipImHandler.sendCallback(callback6, ImError.NETWORK_ERROR);
                }
            }
            for (Message callback7 : removedSession.mChangeGCIconCallbacks.values()) {
                if (callback7 != null) {
                    this.mResipImHandler.sendCallback(callback7, ImError.NETWORK_ERROR);
                }
            }
            return;
        }
        int i2 = sessionHandle2;
    }

    private void handleImMessageReceivedNotify(Notify notify) {
        if (notify.notiType() != 31) {
            Log.e(this.LOG_TAG, "handleImMessageReceivedNotify(): invalid notify");
            return;
        }
        ImMessageReceived proto = (ImMessageReceived) notify.noti(new ImMessageReceived());
        BaseSessionData baseSessionData = proto.sessionData();
        ImMessageParam imMessageParam = proto.messageParam();
        if (baseSessionData == null || imMessageParam == null) {
            Log.e(this.LOG_TAG, "handleImMessageReceivedNotify(): invalid message notify data");
            return;
        }
        int sessionHandle = (int) baseSessionData.sessionHandle();
        String str = this.LOG_TAG;
        Log.i(str, "handleImMessageReceivedNotify(): sessionId = " + sessionHandle);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleImMessageReceivedNotify(): Unknown session handle - " + sessionHandle);
            return;
        }
        ImIncomingMessageEvent event = parseImMessageParam(imMessageParam);
        if (event != null) {
            event.mRawHandle = Integer.valueOf(sessionHandle);
            event.mChatId = session.mChatId;
            String str3 = this.LOG_TAG;
            IMSLog.s(str3, "handleImMessageReceivedNotify(): " + event);
            this.mResipImHandler.mIncomingMessageRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
        }
    }

    private void handleImComposingStatusReceivedNotify(Notify notify) {
        Log.i(this.LOG_TAG, "handleImComposingStatusReceivedNotify");
        if (notify.notiType() != 32) {
            Log.e(this.LOG_TAG, "handleImComposingStatusReceivedNotify(): invalid notify");
            return;
        }
        ImComposingStatusReceived proto = (ImComposingStatusReceived) notify.noti(new ImComposingStatusReceived());
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf((int) proto.sessionId()));
        String userAlias = null;
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "Unkown session id " + proto.sessionId());
            return;
        }
        ImComposingStatus status = proto.status();
        if (status == null) {
            Log.e(this.LOG_TAG, "handleImComposingStatusReceivedNotify(): invalid notify data");
            return;
        }
        String remoteUri = proto.uri();
        if (proto.userAlias() != null && !proto.userAlias().isEmpty()) {
            Log.i(this.LOG_TAG, "handleImComposingStatusReceivedNotify: found userAlias");
            userAlias = proto.userAlias();
        }
        if (!TextUtils.isEmpty(remoteUri)) {
            remoteUri = remoteUri.replaceAll("\\<|\\>", "");
        }
        ImComposingEvent event = new ImComposingEvent(session.mChatId, remoteUri, userAlias, status.isActive(), (int) status.interval());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleImComposingStatusReceivedNotify: " + event);
        this.mResipImHandler.mComposingRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private void handleFtSessionStartedNotify(Notify notify) {
        if (notify.notiType() != 27) {
            Log.e(this.LOG_TAG, "handleFtSessionStartedNotify(): invalid notify");
            return;
        }
        SessionStarted noti = (SessionStarted) notify.noti(new SessionStarted());
        int sessionHandle = (int) noti.sessionHandle();
        Result reason = ResipTranslatorCollection.translateFtResult(noti.imError(), noti.warningHdr());
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleFtSessionStartedNotify(): sessionHandle = " + sessionHandle + ", error = " + reason);
        ResipImHandler.FtSession session = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleFtSessionStartedNotify(): Unknown session handle: " + noti.sessionHandle());
        } else if (reason.getImError() == ImError.SUCCESS) {
            Log.i(this.LOG_TAG, "handleFtSessionStartedNotify(): SUCCESS");
        } else {
            if (session.mStartCallback != null) {
                RetryHdr retryHdr = noti.retryHdr();
                this.mResipImHandler.sendCallback(session.mStartCallback, new FtResult(reason, (Object) Integer.valueOf(sessionHandle), retryHdr != null ? retryHdr.retryTimer() : 0));
                session.mStartCallback = null;
            }
            this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        }
    }

    private void handleFtProgressNotify(Notify notify) {
        if (notify.notiType() != 40) {
            Log.e(this.LOG_TAG, "handleFtProgressNotify(): invalid notify");
            return;
        }
        FtProgress proto = (FtProgress) notify.noti(new FtProgress());
        int sessionHandle = (int) proto.sessionHandle();
        ResipImHandler.FtSession session = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "Unkown session id " + proto.sessionHandle());
            return;
        }
        FtTransferProgressEvent.State state = ResipTranslatorCollection.translateFtProgressState((int) proto.state());
        if (state != FtTransferProgressEvent.State.TRANSFERRING) {
            this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        }
        Result reason = ResipTranslatorCollection.translateFtResult(proto.imError(), proto.reasonHdr());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleFtProgressNotify(): session handle = " + sessionHandle + ", state = " + proto.state() + ", fail reason = " + reason + ", total = " + proto.total() + ", transferred = " + proto.transferred());
        if (this.mResipImHandler.mTransferProgressRegistrants.size() != 0) {
            Integer valueOf = Integer.valueOf(sessionHandle);
            int i = session.mId;
            long j = proto.total();
            long transferred = proto.transferred();
            FtProgress ftProgress = proto;
            FtTransferProgressEvent ftTransferProgressEvent = r6;
            FtTransferProgressEvent ftTransferProgressEvent2 = new FtTransferProgressEvent(valueOf, i, j, transferred, state, reason);
            this.mResipImHandler.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, ftTransferProgressEvent, (Throwable) null));
            return;
        }
        String str3 = this.LOG_TAG;
        Log.e(str3, "No TransferProgressRegistrant for handle = " + session.mHandle);
    }

    private void handleFtIncomingSessionNotify(Notify notify) {
        FtIncomingSession proto;
        List<Integer> notiList;
        if (notify.notiType() != 41) {
            Log.e(this.LOG_TAG, "handleFtIncomingSessionNotify(): invalid notify");
            return;
        }
        FtIncomingSession proto2 = (FtIncomingSession) notify.noti(new FtIncomingSession());
        BaseSessionData baseSessionData = proto2.session();
        FtPayloadParam ftPayloadParam = proto2.payload();
        if (ftPayloadParam == null) {
        } else if (baseSessionData == null) {
            FtIncomingSession ftIncomingSession = proto2;
        } else {
            int sessionHandle = (int) baseSessionData.sessionHandle();
            String str = this.LOG_TAG;
            Log.i(str, "handleFtIncomingSessionNotify(): session handle = " + sessionHandle);
            UserAgent ua = this.mResipImHandler.getUserAgent((int) proto2.userHandle());
            if (ua == null) {
                String str2 = this.LOG_TAG;
                Log.e(str2, "handleFtIncomingSessionNotify(): UserAgent not found. UserHandle = " + ((int) proto2.userHandle()));
                return;
            }
            ResipImHandler.FtSession session = new ResipImHandler.FtSession();
            session.mHandle = sessionHandle;
            session.mUaHandle = (int) proto2.userHandle();
            this.mResipImHandler.mFtSessions.put(Integer.valueOf(sessionHandle), session);
            FtIncomingSessionEvent event = new FtIncomingSessionEvent();
            event.mRawHandle = Integer.valueOf(sessionHandle);
            event.mIsSlmSvcMsg = false;
            event.mOwnImsi = this.mResipImHandler.getImsiByUserAgent(ua);
            event.mSenderUri = ImsUri.parse(baseSessionData.sessionUri());
            event.mParticipants = new ArrayList();
            if (baseSessionData.isConference()) {
                event.mParticipants.add(event.mSenderUri);
            } else {
                for (int i = 0; i < baseSessionData.receiversLength(); i++) {
                    event.mParticipants.add(ImsUri.parse(baseSessionData.receivers(i)));
                }
            }
            event.mUserAlias = baseSessionData.userAlias();
            event.mSdpContentType = baseSessionData.sdpContentType();
            event.mIsConference = baseSessionData.isConference();
            event.mIsRoutingMsg = ftPayloadParam.silenceSupported();
            if (event.mIsRoutingMsg) {
                Log.i(this.LOG_TAG, "handleFtIncomingSessionNotify -> routing message");
                event.mRequestUri = ImsUri.parse(ftPayloadParam.requestUri());
                event.mPAssertedId = ImsUri.parse(ftPayloadParam.pAssertedId());
                event.mReceiver = ImsUri.parse(ftPayloadParam.receiver());
            }
            if (!TextUtils.isEmpty(ftPayloadParam.deviceName())) {
                event.mDeviceName = ftPayloadParam.deviceName();
            }
            if (!TextUtils.isEmpty(ftPayloadParam.reliableMessage())) {
                event.mReliableMessage = ftPayloadParam.reliableMessage();
            }
            event.mExtraFt = ftPayloadParam.extraFt();
            ImFileAttr fileAttr = ftPayloadParam.fileAttr();
            if (fileAttr != null) {
                event.mContentType = fileAttr.contentType();
                try {
                    event.mFileName = URLDecoder.decode(fileAttr.name(), "UTF-8");
                } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                if (event.mFileName == null) {
                    event.mFileName = fileAttr.name();
                }
                event.mFilePath = fileAttr.path();
                event.mFileSize = (long) ((int) fileAttr.size());
                event.mStart = (int) fileAttr.start();
                event.mEnd = (int) fileAttr.end();
                event.mTimeDuration = (int) fileAttr.timeDuration();
            }
            ImFileAttr iconAttr = ftPayloadParam.iconAttr();
            if (iconAttr == null || iconAttr.path() == null || iconAttr.path().isEmpty()) {
                event.mThumbPath = null;
            } else {
                event.mThumbPath = iconAttr.path();
            }
            event.mContributionId = baseSessionData.contributionId();
            if (baseSessionData.conversationId() != null) {
                event.mConversationId = baseSessionData.conversationId();
            }
            if (baseSessionData.inReplyToContributionId() != null) {
                event.mInReplyToConversationId = baseSessionData.inReplyToContributionId();
            }
            event.mFileTransferId = baseSessionData.id();
            event.mPush = ftPayloadParam.isPush();
            if (ftPayloadParam.imdn() != null) {
                event.mImdnId = ftPayloadParam.imdn().messageId();
                try {
                    String datetime = ftPayloadParam.imdn().datetime();
                    event.mImdnTime = !TextUtils.isEmpty(datetime) ? Iso8601.parse(datetime) : new Date();
                } catch (ParseException e2) {
                    e2.printStackTrace();
                    event.mImdnTime = new Date();
                }
                List<Integer> notiList2 = new ArrayList<>();
                for (int i2 = 0; i2 < ftPayloadParam.imdn().notiLength(); i2++) {
                    notiList2.add(Integer.valueOf(ftPayloadParam.imdn().noti(i2)));
                }
                event.mDisposition = ResipTranslatorCollection.translateStackImdnNoti(notiList2);
                event.mDeviceId = ftPayloadParam.imdn().deviceId();
                event.mOriginalToHdr = ftPayloadParam.imdn().originalToHdr();
                event.mRecRouteList = new ArrayList();
                int i3 = 0;
                while (i3 < ftPayloadParam.imdn().recRouteLength()) {
                    ImdnRecRoute route = ftPayloadParam.imdn().recRoute(i3);
                    if (route != null) {
                        notiList = notiList2;
                        proto = proto2;
                        event.mRecRouteList.add(new ImImdnRecRoute(event.mImdnId, route.uri(), route.name()));
                    } else {
                        notiList = notiList2;
                        proto = proto2;
                    }
                    i3++;
                    Notify notify2 = notify;
                    notiList2 = notiList;
                    proto2 = proto;
                }
                FtIncomingSession ftIncomingSession2 = proto2;
            }
            event.mCpimNamespaces = new ImCpimNamespaces();
            for (int i4 = 0; i4 < ftPayloadParam.cpimNamespacesLength(); i4++) {
                CpimNamespace protoNamespace = ftPayloadParam.cpimNamespaces(i4);
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
            String str3 = this.LOG_TAG;
            IMSLog.s(str3, "handleFtIncomingSessionNotify(): " + event);
            if (this.mResipImHandler.mIncomingFileTransferRegistrants.size() != 0) {
                this.mResipImHandler.mIncomingFileTransferRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
                return;
            }
            String str4 = this.LOG_TAG;
            Log.i(str4, "Empty registrants, reject handle=" + sessionHandle);
            this.mResipImHandler.handleRejectFtSessionRequest(new RejectFtSessionParams(Integer.valueOf(sessionHandle), (Message) null, FtRejectReason.FORBIDDEN_SERVICE_NOT_AUTHORIZED, event.mFileTransferId));
            return;
        }
        Log.e(this.LOG_TAG, "handleFtIncomingSessionNotify(): invalid notify data");
    }

    private void handleImSessionEstablishedNotify(Notify notify) {
        if (notify.notiType() != 29) {
            Log.e(this.LOG_TAG, "handleImSessionEstablishedNotify(): invalid notify");
            return;
        }
        SessionEstablished noti = (SessionEstablished) notify.noti(new SessionEstablished());
        int sessionHandle = (int) noti.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(noti.imError(), (Object) null).getImError();
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleImSessionEstablishedNotify(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleImSessionEstablishedNotify(): sessionHandle = " + sessionHandle + ", chat id = " + session.mChatId + ", error = " + imError);
        if (imError != ImError.SUCCESS) {
            Log.e(this.LOG_TAG, "handleImSessionEstablishedNotify(): failed");
            return;
        }
        List<String> acceptTypes = new ArrayList<>();
        for (int i = 0; i < noti.acceptContentLength(); i++) {
            String content = noti.acceptContent(i);
            if (content != null) {
                acceptTypes.addAll(Arrays.asList(content.split(" ")));
            }
        }
        List<String> acceptWrappedTypes = new ArrayList<>();
        for (int i2 = 0; i2 < noti.acceptWrappedContentLength(); i2++) {
            String content2 = noti.acceptWrappedContent(i2);
            if (content2 != null) {
                acceptWrappedTypes.addAll(Arrays.asList(content2.split(" ")));
            }
        }
        List<String> acceptContent = new ArrayList<>();
        acceptContent.addAll(acceptTypes);
        acceptContent.addAll(acceptWrappedTypes);
        String str3 = this.LOG_TAG;
        IMSLog.s(str3, "handleStartImMediaResponse(): acceptContent = " + acceptContent);
        EnumSet<SupportedFeature> supportedFeatures = EnumSet.noneOf(SupportedFeature.class);
        for (String property : acceptContent) {
            SupportedFeature feature = ResipTranslatorCollection.translateAcceptContent(property);
            if (feature != null) {
                supportedFeatures.add(feature);
            }
        }
        EnumSet<SupportedFeature> enumSet = supportedFeatures;
        imSessionEstablished(sessionHandle, (String) null, supportedFeatures, acceptTypes, acceptWrappedTypes);
    }

    private void imSessionEstablished(int sessionHandle, String sessionUri, EnumSet<SupportedFeature> supportedFeatures, List<String> acceptTypes, List<String> acceptWrappedTypes) {
        String str = sessionUri;
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "imSessionEstablished(): sessionHandle = " + sessionHandle + ", session uri = " + str + ", features = " + supportedFeatures);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (session != null) {
            String chatId = session.mChatId;
            String str3 = this.LOG_TAG;
            IMSLog.s(str3, "imSessionEstablished(): chatid = " + chatId);
            if (chatId == null) {
                Log.e(this.LOG_TAG, "imSessionEstablished(): Failed to find chat id.");
            } else {
                this.mResipImHandler.mSessionEstablishedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new ImSessionEstablishedEvent(Integer.valueOf(sessionHandle), chatId, str == null ? null : ImsUri.parse(sessionUri), supportedFeatures, acceptTypes, acceptWrappedTypes), (Throwable) null));
            }
        }
    }

    private void handleFtSessionEstablishedNotify(Notify notify) {
        if (notify.notiType() != 29) {
            Log.e(this.LOG_TAG, "handleFtSessionEstablishedNotify(): invalid notify");
            return;
        }
        SessionEstablished noti = (SessionEstablished) notify.noti(new SessionEstablished());
        int sessionHandle = (int) noti.sessionHandle();
        Result reason = ResipTranslatorCollection.translateFtResult(noti.imError(), (Object) null);
        ResipImHandler.FtSession session = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleFtSessionEstablishedNotify(): no session found sessionHandle = " + sessionHandle + ", result = " + reason);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleFtSessionEstablishedNotify(): sessionHandle = " + sessionHandle + ", result = " + reason);
        if (reason.getImError() != ImError.SUCCESS) {
            Log.e(this.LOG_TAG, "handleFtSessionEstablishedNotify(): failed");
        } else if (session.mStartCallback != null) {
            this.mResipImHandler.sendCallback(session.mStartCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
            session.mStartCallback = null;
        } else if (session.mAcceptCallback != null) {
            this.mResipImHandler.sendCallback(session.mAcceptCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
            session.mAcceptCallback = null;
        }
    }

    private void handleFtSessionClosedNotify(Notify notify) {
        if (notify.notiType() != 28) {
            Log.e(this.LOG_TAG, "handleFtSessionClosedNotify(): invalid notify");
            return;
        }
        SessionClosed proto = (SessionClosed) notify.noti(new SessionClosed());
        int sessionHandle = (int) proto.sessionHandle();
        Result reason = ResipTranslatorCollection.translateFtResult(proto.imError(), (Object) null);
        ResipImHandler.FtSession session = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleFtSessionClosedNotify(): no session found sessionHandle = " + sessionHandle + ", error = " + reason);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleFtSessionClosedNotify(): sessionHandle = " + sessionHandle + ", error = " + reason);
        if (reason.getImError() != ImError.SUCCESS) {
            Log.e(this.LOG_TAG, "handleFtSessionClosedNotify(): abnormal close");
            if (session.mStartCallback != null) {
                this.mResipImHandler.sendCallback(session.mStartCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
                session.mStartCallback = null;
            }
            if (session.mAcceptCallback != null) {
                this.mResipImHandler.sendCallback(session.mAcceptCallback, new FtResult(reason, Integer.valueOf(sessionHandle)));
                session.mAcceptCallback = null;
                SessionClosed sessionClosed = proto;
            } else if (this.mResipImHandler.mTransferProgressRegistrants.size() != 0) {
                IMSLog.s(this.LOG_TAG, "handleFtSessionClosedNotify(): post cancelled to progress registrants");
                FtTransferProgressEvent ftTransferProgressEvent = r6;
                SessionClosed sessionClosed2 = proto;
                FtTransferProgressEvent ftTransferProgressEvent2 = new FtTransferProgressEvent(Integer.valueOf(sessionHandle), session.mId, -1, -1, FtTransferProgressEvent.State.CANCELED, reason);
                this.mResipImHandler.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, ftTransferProgressEvent, (Throwable) null));
            } else {
                String str3 = this.LOG_TAG;
                Log.e(str3, "No TransferProgressRegistrant for handle = " + session.mHandle);
            }
        } else {
            IMSLog.s(this.LOG_TAG, "handleFtSessionClosedNotify(): get unexpected SessionClosed notify");
        }
        this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
    }

    /* JADX WARNING: Removed duplicated region for block: B:123:0x02ed  */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x02f6  */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x02fa  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0349  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x0355  */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x03a6  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x03ae  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x015f  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x021e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleImConferenceInfoUpdateNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify r31) {
        /*
            r30 = this;
            r1 = r30
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r2 = "handleImConferenceInfoUpdateNotify"
            android.util.Log.i(r0, r2)
            byte r0 = r31.notiType()
            r2 = 70
            if (r0 == r2) goto L_0x0019
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r2 = "handleImConferenceInfoUpdateNotify(): invalid notify"
            android.util.Log.e(r0, r2)
            return
        L_0x0019:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated r0 = new com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated
            r0.<init>()
            r2 = r31
            com.google.flatbuffers.Table r0 = r2.noti(r0)
            r3 = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated r3 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated) r3
            long r4 = r3.sessionId()
            int r4 = (int) r4
            com.sec.internal.ims.core.handler.secims.ResipImHandler r0 = r1.mResipImHandler
            java.util.Map<java.lang.Integer, com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession> r0 = r0.mSessions
            java.lang.Integer r5 = java.lang.Integer.valueOf(r4)
            java.lang.Object r0 = r0.get(r5)
            r5 = r0
            com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession r5 = (com.sec.internal.ims.core.handler.secims.ResipImHandler.ImSession) r5
            if (r5 == 0) goto L_0x03e0
            java.lang.String r0 = r5.mChatId
            if (r0 != 0) goto L_0x0045
            r26 = r4
            goto L_0x03e2
        L_0x0045:
            com.sec.internal.ims.core.handler.secims.ResipImHandler r0 = r1.mResipImHandler
            int r6 = r5.mUaHandle
            com.sec.internal.ims.core.handler.secims.UserAgent r6 = r0.getUserAgent((int) r6)
            if (r6 != 0) goto L_0x0057
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r7 = "handleImConferenceInfoUpdateNotify(): User Agent not found!"
            android.util.Log.e(r0, r7)
            return
        L_0x0057:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.FULL
            java.lang.String r7 = r3.state()
            boolean r8 = android.text.TextUtils.isEmpty(r7)
            r10 = 1
            if (r8 != 0) goto L_0x0098
            r8 = -1
            int r11 = r7.hashCode()
            r12 = -792934015(0xffffffffd0bcc981, float:-2.53385789E10)
            if (r11 == r12) goto L_0x007e
            r12 = 1550463001(0x5c6a3019, float:2.63672114E17)
            if (r11 == r12) goto L_0x0074
        L_0x0073:
            goto L_0x0088
        L_0x0074:
            java.lang.String r11 = "deleted"
            boolean r11 = r7.equals(r11)
            if (r11 == 0) goto L_0x0073
            r8 = r10
            goto L_0x0088
        L_0x007e:
            java.lang.String r11 = "partial"
            boolean r11 = r7.equals(r11)
            if (r11 == 0) goto L_0x0073
            r8 = 0
        L_0x0088:
            if (r8 == 0) goto L_0x0094
            if (r8 == r10) goto L_0x0090
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.FULL
            r8 = r0
            goto L_0x0099
        L_0x0090:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.DELETED
            r8 = r0
            goto L_0x0099
        L_0x0094:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.PARTIAL
            r8 = r0
            goto L_0x0099
        L_0x0098:
            r8 = r0
        L_0x0099:
            r11 = 0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.SubjectExt r20 = r3.subjectData()
            java.lang.String r12 = ""
            if (r20 == 0) goto L_0x0157
            java.lang.String r0 = r1.LOG_TAG
            java.lang.StringBuilder r13 = new java.lang.StringBuilder
            r13.<init>()
            java.lang.String r14 = "Received sub info: subject ="
            r13.append(r14)
            java.lang.String r14 = r20.subject()
            r13.append(r14)
            java.lang.String r14 = ", participant ="
            r13.append(r14)
            java.lang.String r14 = r20.participant()
            r13.append(r14)
            java.lang.String r14 = ", timestamp ="
            r13.append(r14)
            java.lang.String r14 = r20.timestamp()
            r13.append(r14)
            java.lang.String r13 = r13.toString()
            android.util.Log.i(r0, r13)
            java.lang.String r0 = r20.subject()
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 == 0) goto L_0x00f2
            java.lang.String r0 = r20.participant()
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 == 0) goto L_0x00f2
            java.lang.String r0 = r20.timestamp()
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0157
        L_0x00f2:
            java.lang.String r0 = r20.subject()
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 == 0) goto L_0x00fe
            r0 = r12
            goto L_0x0102
        L_0x00fe:
            java.lang.String r0 = r20.subject()
        L_0x0102:
            r13 = r0
            r0 = 0
            java.lang.String r14 = r20.participant()
            boolean r15 = android.text.TextUtils.isEmpty(r14)
            if (r15 != 0) goto L_0x0114
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r14)
            r15 = r0
            goto L_0x0115
        L_0x0114:
            r15 = r0
        L_0x0115:
            r16 = 0
            java.lang.String r9 = r20.timestamp()
            boolean r0 = android.text.TextUtils.isEmpty(r9)
            if (r0 != 0) goto L_0x014e
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r9)     // Catch:{ ParseException -> 0x0128 }
            r16 = r0
            goto L_0x0150
        L_0x0128:
            r0 = move-exception
            r18 = r0
            r0 = r18
            java.lang.String r10 = r1.LOG_TAG
            r19 = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "parsing subject timestamp failed : "
            r0.append(r2)
            r0.append(r9)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r10, r0)
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            r16 = r0
            goto L_0x0150
        L_0x014e:
            r0 = r16
        L_0x0150:
            com.sec.internal.constants.ims.servicemodules.im.ImSubjectData r2 = new com.sec.internal.constants.ims.servicemodules.im.ImSubjectData
            r2.<init>(r13, r15, r0)
            r11 = r2
            goto L_0x0158
        L_0x0157:
            r2 = r11
        L_0x0158:
            r9 = 0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.Icon r10 = r3.iconData()
            if (r10 == 0) goto L_0x01f6
            r0 = 0
            int r11 = r10.icontype()
            r13 = 0
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r14 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_FILE
            int r14 = r14.ordinal()
            if (r11 != r14) goto L_0x0172
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r0 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_FILE
            r14 = r13
            r13 = r0
            goto L_0x0187
        L_0x0172:
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r14 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_URI
            int r14 = r14.ordinal()
            if (r11 != r14) goto L_0x0183
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r0 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_URI
            java.lang.String r13 = r10.iconLocation()
            r14 = r13
            r13 = r0
            goto L_0x0187
        L_0x0183:
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r0 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_NONE
            r14 = r13
            r13 = r0
        L_0x0187:
            r0 = 0
            java.lang.String r15 = r10.participant()
            boolean r16 = android.text.TextUtils.isEmpty(r15)
            if (r16 != 0) goto L_0x0199
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r15)
            r16 = r0
            goto L_0x019b
        L_0x0199:
            r16 = r0
        L_0x019b:
            r19 = 0
            r27 = r7
            java.lang.String r7 = r10.timestamp()
            boolean r0 = android.text.TextUtils.isEmpty(r7)
            if (r0 != 0) goto L_0x01dd
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r7)     // Catch:{ ParseException -> 0x01b4 }
            r19 = r0
            r28 = r9
            r29 = r11
            goto L_0x01e1
        L_0x01b4:
            r0 = move-exception
            r21 = r0
            r0 = r21
            java.lang.String r0 = r1.LOG_TAG
            r28 = r9
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r29 = r11
            java.lang.String r11 = "parsing icon timestamp failed : "
            r9.append(r11)
            r9.append(r7)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r0, r9)
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            r19 = r0
            goto L_0x01e1
        L_0x01dd:
            r28 = r9
            r29 = r11
        L_0x01e1:
            com.sec.internal.constants.ims.servicemodules.im.ImIconData r0 = new com.sec.internal.constants.ims.servicemodules.im.ImIconData
            java.lang.String r25 = r10.iconLocation()
            r21 = r0
            r22 = r13
            r23 = r16
            r24 = r19
            r26 = r14
            r21.<init>(r22, r23, r24, r25, r26)
            r9 = r0
            goto L_0x01fa
        L_0x01f6:
            r27 = r7
            r28 = r9
        L_0x01fa:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r7 = r0
            int r15 = r3.usersLength()
            java.lang.String r0 = r1.LOG_TAG
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r13 = "handleOptionsReceived: tagLength "
            r11.append(r13)
            r11.append(r15)
            java.lang.String r11 = r11.toString()
            android.util.Log.i(r0, r11)
            r0 = 0
            r11 = r0
        L_0x021c:
            if (r11 >= r15) goto L_0x0389
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUser r13 = r3.users(r11)
            if (r13 == 0) goto L_0x0375
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo r0 = new com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo
            r0.<init>()
            r16 = r0
            java.lang.String r0 = r13.entity()
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r0)
            r14 = r16
            r14.mUri = r0
            java.lang.String r0 = r13.state()
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceUserElemState r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateImConferenceUserElemState(r0)
            r14.mUserElemState = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            if (r0 == 0) goto L_0x025e
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            java.lang.String r0 = r0.status()
            if (r0 == 0) goto L_0x025e
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            java.lang.String r0 = r0.status()
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceParticipantStatus(r0)
            goto L_0x0260
        L_0x025e:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.INVALID
        L_0x0260:
            r14.mParticipantStatus = r0
            boolean r0 = r13.yourOwn()
            r14.mIsOwn = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            if (r0 == 0) goto L_0x0277
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            java.lang.String r0 = r0.disconnectMethod()
            goto L_0x0278
        L_0x0277:
            r0 = r12
        L_0x0278:
            r16 = r0
            boolean r0 = android.text.TextUtils.isEmpty(r16)
            if (r0 != 0) goto L_0x0286
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceDisconnectionReason(r16)
            r14.mDisconnectionReason = r0
        L_0x0286:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            if (r0 == 0) goto L_0x0295
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserJoiningInfo r0 = r0.joininginfo()
            goto L_0x0296
        L_0x0295:
            r0 = 0
        L_0x0296:
            r21 = r0
            if (r21 == 0) goto L_0x02e1
            r22 = r10
            java.lang.String r10 = r21.when()
            boolean r0 = android.text.TextUtils.isEmpty(r10)
            if (r0 != 0) goto L_0x02dc
            java.lang.String r0 = r1.LOG_TAG     // Catch:{ ParseException -> 0x02cc }
            r23 = r12
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ ParseException -> 0x02ca }
            r12.<init>()     // Catch:{ ParseException -> 0x02ca }
            r24 = r15
            java.lang.String r15 = "parsing joiningInfo timestamp failed : "
            r12.append(r15)     // Catch:{ ParseException -> 0x02c8 }
            r12.append(r10)     // Catch:{ ParseException -> 0x02c8 }
            java.lang.String r12 = r12.toString()     // Catch:{ ParseException -> 0x02c8 }
            android.util.Log.i(r0, r12)     // Catch:{ ParseException -> 0x02c8 }
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r10)     // Catch:{ ParseException -> 0x02c8 }
            r14.mJoiningTime = r0     // Catch:{ ParseException -> 0x02c8 }
            goto L_0x02e7
        L_0x02c8:
            r0 = move-exception
            goto L_0x02d1
        L_0x02ca:
            r0 = move-exception
            goto L_0x02cf
        L_0x02cc:
            r0 = move-exception
            r23 = r12
        L_0x02cf:
            r24 = r15
        L_0x02d1:
            r0.printStackTrace()
            java.util.Date r12 = new java.util.Date
            r12.<init>()
            r14.mJoiningTime = r12
            goto L_0x02e7
        L_0x02dc:
            r23 = r12
            r24 = r15
            goto L_0x02e7
        L_0x02e1:
            r22 = r10
            r23 = r12
            r24 = r15
        L_0x02e7:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            if (r0 == 0) goto L_0x02f6
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r13.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserDisconnectionInfo r0 = r0.disconnectioninfo()
            goto L_0x02f7
        L_0x02f6:
            r0 = 0
        L_0x02f7:
            r10 = r0
            if (r10 == 0) goto L_0x0349
            java.lang.String r12 = r10.when()
            boolean r0 = android.text.TextUtils.isEmpty(r12)
            if (r0 != 0) goto L_0x0331
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r12)     // Catch:{ ParseException -> 0x030d }
            r14.mDisconnectionTime = r0     // Catch:{ ParseException -> 0x030d }
            r26 = r4
            goto L_0x0333
        L_0x030d:
            r0 = move-exception
            java.lang.String r15 = r1.LOG_TAG
            r25 = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r26 = r4
            java.lang.String r4 = "parsing disconnectionInfo timestamp failed : "
            r0.append(r4)
            r0.append(r12)
            java.lang.String r0 = r0.toString()
            android.util.Log.i(r15, r0)
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            r14.mDisconnectionTime = r0
            goto L_0x0333
        L_0x0331:
            r26 = r4
        L_0x0333:
            java.lang.String r0 = r10.reason()
            boolean r4 = android.text.TextUtils.isEmpty(r0)
            if (r4 != 0) goto L_0x034b
            int r4 = r1.parseReasonHeader(r0)
            r15 = 0
            com.sec.internal.constants.ims.servicemodules.im.ImError r15 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateSIPError(r4, r15)
            r14.mDisconnectionCause = r15
            goto L_0x034b
        L_0x0349:
            r26 = r4
        L_0x034b:
            java.lang.String r0 = r13.roles()
            boolean r4 = android.text.TextUtils.isEmpty(r0)
            if (r4 != 0) goto L_0x036b
            java.lang.String r4 = "chairman"
            boolean r4 = r4.equalsIgnoreCase(r0)
            if (r4 != 0) goto L_0x0368
            java.lang.String r4 = "Administrator"
            boolean r4 = r4.equalsIgnoreCase(r0)
            if (r4 == 0) goto L_0x0366
            goto L_0x0368
        L_0x0366:
            r4 = 0
            goto L_0x0369
        L_0x0368:
            r4 = 1
        L_0x0369:
            r14.mIsChairman = r4
        L_0x036b:
            java.lang.String r4 = r13.userAlias()
            r14.mDispName = r4
            r7.add(r14)
            goto L_0x037d
        L_0x0375:
            r26 = r4
            r22 = r10
            r23 = r12
            r24 = r15
        L_0x037d:
            int r11 = r11 + 1
            r10 = r22
            r12 = r23
            r15 = r24
            r4 = r26
            goto L_0x021c
        L_0x0389:
            r26 = r4
            r22 = r10
            r24 = r15
            r15 = 0
            boolean r0 = r7.isEmpty()
            if (r0 == 0) goto L_0x03ae
            int r0 = r6.getPhoneId()
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r0)
            java.lang.String r4 = "confinfo_update_not_allowed"
            boolean r0 = r0.boolSetting(r4)
            if (r0 == 0) goto L_0x03ae
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r4 = "imConferenceInfoUpdate: Drop the invalid info"
            com.sec.internal.log.IMSLog.s(r0, r4)
            return
        L_0x03ae:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent r0 = new com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent
            java.lang.String r12 = r5.mChatId
            long r10 = r3.maxUserCnt()
            int r4 = (int) r10
            long r10 = r3.sessionId()
            java.lang.Long r17 = java.lang.Long.valueOf(r10)
            com.sec.internal.ims.core.handler.secims.ResipImHandler r10 = r1.mResipImHandler
            java.lang.String r18 = r10.getImsiByUserAgent(r6)
            r11 = r0
            r13 = r8
            r10 = r15
            r14 = r7
            r21 = r24
            r15 = r4
            r16 = r2
            r19 = r9
            r11.<init>(r12, r13, r14, r15, r16, r17, r18, r19)
            com.sec.internal.ims.core.handler.secims.ResipImHandler r4 = r1.mResipImHandler
            com.sec.internal.helper.RegistrantList r4 = r4.mConferenceInfoUpdateRegistrants
            com.sec.internal.helper.AsyncResult r11 = new com.sec.internal.helper.AsyncResult
            r11.<init>(r10, r0, r10)
            r4.notifyRegistrants(r11)
            return
        L_0x03e0:
            r26 = r4
        L_0x03e2:
            java.lang.String r0 = r1.LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "Unknown sessionId - "
            r2.append(r4)
            r4 = r26
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r0, r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImResponseHandler.handleImConferenceInfoUpdateNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify):void");
    }

    private int parseReasonHeader(String header) {
        if (TextUtils.isEmpty(header)) {
            return 0;
        }
        try {
            int index = header.indexOf("cause=");
            if (index == -1 || index + 9 > header.length()) {
                return 0;
            }
            String code = header.substring(index + 6, index + 9);
            String str = this.LOG_TAG;
            IMSLog.s(str, "parseReasonHeader : " + code);
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void handleImMessageReportReceivedNotify(Notify notify) {
        if (notify.notiType() != 35) {
            Log.e(this.LOG_TAG, "handleImMessageReportReceivedNotify(): invalid notify");
            return;
        }
        ImMessageReportReceived proto = (ImMessageReportReceived) notify.noti(new ImMessageReportReceived());
        int sessionId = (int) proto.sessionId();
        String chatId = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionId)).mChatId;
        String imdnId = proto.imdnMessageId();
        Result result = ResipTranslatorCollection.translateImResult(proto.imError(), (Object) null);
        boolean isChat = proto.isChat();
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleImMessageReportReceivedNotify(): sessionId = " + sessionId + " chatId = " + chatId + " imdnId = " + imdnId + " result = " + result + " isChat = " + isChat);
        if (result.getImError() == ImError.SUCCESS) {
            return;
        }
        if (isChat) {
            this.mResipImHandler.mMessageFailedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new SendMessageFailedEvent(Integer.valueOf(sessionId), chatId, imdnId, result), (Throwable) null));
        } else {
            this.mResipImHandler.mImdnFailedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new SendImdnFailedEvent(Integer.valueOf(sessionId), chatId, imdnId, result), (Throwable) null));
        }
    }

    private void handleGroupChatSubscribeStatusNotify() {
        IMSLog.s(this.LOG_TAG, "handleGroupChatSubscribeStatusNotify()");
    }

    private void handleGroupChatListNotify(Notify notify) {
        IMSLog.s(this.LOG_TAG, "handleGroupChatListNotify()");
        GroupChatListUpdated groupChatListUpdated = (GroupChatListUpdated) notify.noti(new GroupChatListUpdated());
        int version = (int) groupChatListUpdated.version();
        UserAgent ua = this.mResipImHandler.getUserAgent((int) groupChatListUpdated.uaHandle());
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleGcListNotify(): User Agent not found!");
            return;
        }
        int groupChatListSize = groupChatListUpdated.groupChatsLength();
        ArrayList<ImIncomingGroupChatListEvent.Entry> list = new ArrayList<>();
        for (int index = 0; index < groupChatListSize; index++) {
            GroupChatInfo info = groupChatListUpdated.groupChats(index);
            if (info != null) {
                list.add(new ImIncomingGroupChatListEvent.Entry(Uri.parse(info.uri()), info.conversationId(), info.subject()));
            }
        }
        this.mResipImHandler.mGroupChatListRegistrants.notifyRegistrants(new AsyncResult((Object) null, new ImIncomingGroupChatListEvent(version, list, this.mResipImHandler.getImsiByUserAgent(ua)), (Throwable) null));
    }

    private void handleGroupChatInfoNotify(Notify notify) {
        ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType infoType;
        ImSubjectData subjectData;
        ImIconData iconData;
        ImIconData.IconType iconType;
        String iconUri;
        Date timestamp;
        ImsUri participant;
        IMSLog.s(this.LOG_TAG, "handleGroupChatInfoNotify()");
        GroupChatInfoUpdated groupChatInfoUpdated = (GroupChatInfoUpdated) notify.noti(new GroupChatInfoUpdated());
        String uri = groupChatInfoUpdated.uri();
        ImConfInfoUpdated imConferenceInfo = groupChatInfoUpdated.info();
        if (imConferenceInfo == null) {
            Log.e(this.LOG_TAG, "handleGroupChatInfoNotify(): info is null, return");
            return;
        }
        UserAgent ua = this.mResipImHandler.getUserAgent((int) groupChatInfoUpdated.uaHandle());
        if (ua == null) {
            Log.e(this.LOG_TAG, "handleGroupChatInfoNotify(): User Agent not found!");
            return;
        }
        ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType infoType2 = ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.FULL;
        if (imConferenceInfo.state() != null) {
            String state = imConferenceInfo.state();
            char c = 65535;
            int hashCode = state.hashCode();
            if (hashCode != -792934015) {
                if (hashCode == 1550463001 && state.equals("deleted")) {
                    c = 1;
                }
            } else if (state.equals("partial")) {
                c = 0;
            }
            if (c == 0) {
                infoType = ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.PARTIAL;
            } else if (c != 1) {
                infoType = ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.FULL;
            } else {
                infoType = ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.DELETED;
            }
        } else {
            infoType = infoType2;
        }
        SubjectExt subjectExt = imConferenceInfo.subjectData();
        if (subjectExt != null) {
            Date timestamp2 = null;
            if (subjectExt.participant() != null) {
                participant = ImsUri.parse(subjectExt.participant());
            } else {
                participant = null;
            }
            if (subjectExt.timestamp() != null) {
                try {
                    timestamp2 = Iso8601.parse(subjectExt.timestamp());
                } catch (ParseException e) {
                    e.printStackTrace();
                    timestamp2 = new Date();
                }
            }
            subjectData = new ImSubjectData(subjectExt.subject(), participant, timestamp2);
        } else {
            subjectData = null;
        }
        Icon icon = imConferenceInfo.iconData();
        if (icon != null) {
            ImsUri participant2 = ImsUri.parse(icon.participant());
            String iconlocation = icon.iconLocation();
            int icon_type = icon.icontype();
            if (icon_type == ImIconData.IconType.ICON_TYPE_FILE.ordinal()) {
                iconUri = null;
                iconType = ImIconData.IconType.ICON_TYPE_FILE;
            } else if (icon_type == ImIconData.IconType.ICON_TYPE_URI.ordinal()) {
                ImIconData.IconType iconType2 = ImIconData.IconType.ICON_TYPE_URI;
                iconUri = icon.iconLocation();
                iconType = iconType2;
            } else {
                iconUri = null;
                iconType = ImIconData.IconType.ICON_TYPE_NONE;
            }
            try {
                timestamp = icon.timestamp() != null ? Iso8601.parse(icon.timestamp()) : new Date();
            } catch (ParseException e2) {
                e2.printStackTrace();
                timestamp = new Date();
            }
            int i = icon_type;
            iconData = new ImIconData(iconType, participant2, timestamp, iconlocation, iconUri);
        } else {
            iconData = null;
        }
        ArrayList arrayList = new ArrayList();
        if (imConferenceInfo.usersLength() > 0) {
            for (int index = 0; index < imConferenceInfo.usersLength(); index++) {
                ImConfUser user = imConferenceInfo.users(index);
                if (user != null) {
                    ImConferenceParticipantInfo info = new ImConferenceParticipantInfo();
                    info.mUri = ImsUri.parse(user.entity());
                    info.mUserElemState = ResipTranslatorCollection.translateImConferenceUserElemState(user.state());
                    info.mIsOwn = user.yourOwn();
                    if (user.endpoint() != null) {
                        if (user.endpoint().status() != null) {
                            info.mParticipantStatus = ResipTranslatorCollection.translateToImConferenceParticipantStatus(user.endpoint().status());
                        }
                        if (user.endpoint().disconnectMethod() != null) {
                            info.mDisconnectionReason = ResipTranslatorCollection.translateToImConferenceDisconnectionReason(user.endpoint().disconnectMethod());
                        }
                        if (!(user.endpoint().joininginfo() == null || user.endpoint().joininginfo().when() == null)) {
                            try {
                                info.mJoiningTime = Iso8601.parse(user.endpoint().joininginfo().when());
                            } catch (ParseException e3) {
                                e3.printStackTrace();
                                info.mJoiningTime = new Date();
                            }
                        }
                        if (!(user.endpoint().disconnectioninfo() == null || user.endpoint().disconnectioninfo().when() == null)) {
                            try {
                                info.mDisconnectionTime = Iso8601.parse(user.endpoint().disconnectioninfo().when());
                            } catch (ParseException e4) {
                                e4.printStackTrace();
                                info.mDisconnectionTime = new Date();
                            }
                        }
                    }
                    if (user.roles() != null) {
                        info.mIsChairman = user.roles().equals(GROUPCHAT_ROLE_CHAIRMAN);
                    }
                    info.mDispName = user.userAlias();
                    arrayList.add(info);
                }
            }
        }
        ArrayList arrayList2 = arrayList;
        UserAgent userAgent = ua;
        this.mResipImHandler.mGroupChatInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, new ImSessionConferenceInfoUpdateEvent(uri, infoType, arrayList, (int) imConferenceInfo.maxUserCnt(), subjectData, (Object) null, this.mResipImHandler.getImsiByUserAgent(ua), iconData), (Throwable) null));
    }

    private void handleImSessionProvisionalResponseNotify(Notify notify) {
        if (notify.notiType() != 27) {
            Log.e(this.LOG_TAG, "handleImSessionProvisionalResponseNotify(): invalid notify");
            return;
        }
        SessionStarted noti = (SessionStarted) notify.noti(new SessionStarted());
        int sessionHandle = (int) noti.sessionHandle();
        Result result = ResipTranslatorCollection.translateImResult(noti.imError(), (Object) null);
        ImError imError = result.getImError();
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleImSessionProvisionalResponseNotify(): sessionHandle = " + sessionHandle + ", response = " + imError);
        ResipImHandler.ImSession session = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (session == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleImSessionProvisionalResponseNotify(): Unknown session handle: " + sessionHandle);
            return;
        }
        if (session.mStartSyncCallback != null) {
            this.mResipImHandler.sendCallback(session.mStartSyncCallback, Integer.valueOf(sessionHandle));
            session.mStartSyncCallback = null;
        }
        if (session.mStartProvisionalCallback != null) {
            this.mResipImHandler.sendCallback(Message.obtain(session.mStartProvisionalCallback), new StartImSessionResult(result, (ImsUri) null, Integer.valueOf(sessionHandle), true));
        }
        if (session.mFirstMessageCallback != null) {
            String str3 = this.LOG_TAG;
            Log.i(str3, "handleImSessionProvisionalResponseNotify(): handle provisional response as SUCCESS for the message in INVITE. sessionHandle = " + sessionHandle + ", response = " + imError);
            this.mResipImHandler.sendCallback(Message.obtain(session.mFirstMessageCallback), new SendMessageResult((Object) Integer.valueOf(sessionHandle), new Result(ImError.SUCCESS, result), true));
        }
    }

    private void handleMessageRevokeResponseReceivedNotify(Notify notify) {
        if (notify.notiType() != 45) {
            Log.e(this.LOG_TAG, "handleMessageRevokeResponseReceivedNotify(): invalid notify");
            return;
        }
        MessageRevokeResponseReceived response = (MessageRevokeResponseReceived) notify.noti(new MessageRevokeResponseReceived());
        ImsUri remoteUri = ImsUri.parse(response.uri());
        if (remoteUri == null) {
            String str = this.LOG_TAG;
            Log.i(str, "Invalid remote uri, return. uri=" + response.uri());
            return;
        }
        MessageRevokeResponse event = new MessageRevokeResponse(remoteUri, response.imdnMessageId(), response.result());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleMessageRevokeResponseReceivedNotify: " + event);
        this.mResipImHandler.mMessageRevokeResponseRegistransts.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private void handleSendMessageRevokeResponseNotify(Notify notify) {
        if (notify.notiType() != 46) {
            Log.e(this.LOG_TAG, "handleSendMessageRevokeResponseNotify(): invalid notify");
            return;
        }
        SendMessageRevokeResponse response = (SendMessageRevokeResponse) notify.noti(new SendMessageRevokeResponse());
        if (response != null) {
            MessageRevokeResponse event = new MessageRevokeResponse((ImsUri) null, response.imdnMessageId(), ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError() == ImError.SUCCESS);
            String str = this.LOG_TAG;
            IMSLog.s(str, "handleSendMessageRevokeResponseNotify: " + event);
            this.mResipImHandler.mSendMessageRevokeResponseRegistransts.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
        }
    }

    private void handleRequestChatbotAnonymizeResp(Notify notify) {
        if (notify.notiType() != 47) {
            Log.e(this.LOG_TAG, "handleRequestChatbotAnonymizeResp(): invalid notify");
            return;
        }
        RequestChatbotAnonymizeResponse response = (RequestChatbotAnonymizeResponse) notify.noti(new RequestChatbotAnonymizeResponse());
        if (response != null) {
            ChatbotAnonymizeRespEvent event = new ChatbotAnonymizeRespEvent(response.uri(), ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError(), response.commandId(), response.retryAfter());
            String str = this.LOG_TAG;
            IMSLog.s(str, "ChatbotAnonymizeRespEvent: " + event);
            this.mResipImHandler.mChatbotAnonymizeResponseRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
        }
    }

    private void handleRequestChatbotAnonymizeNotify(Notify notify) {
        String aliasResult = "";
        String aliasCommandId = "";
        if (notify.notiType() != 48) {
            Log.e(this.LOG_TAG, "handleSetChatbotAnonymizeResponseNotify(): invalid notify");
            return;
        }
        RequestChatbotAnonymizeResponseReceived response = (RequestChatbotAnonymizeResponseReceived) notify.noti(new RequestChatbotAnonymizeResponseReceived());
        String chatbotUri = response.uri();
        String resultXml = response.result();
        ChatbotXmlUtils mUtils = ChatbotXmlUtils.getInstance();
        if (resultXml != null) {
            try {
                aliasResult = mUtils.parseXml(resultXml, "AM/result");
                aliasCommandId = mUtils.parseXml(resultXml, "AM/Command-ID");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ChatbotAnonymizeNotifyEvent event = new ChatbotAnonymizeNotifyEvent(chatbotUri, aliasResult, aliasCommandId);
        String str = this.LOG_TAG;
        IMSLog.s(str, "ChatbotAnonymizeNotifyEvent: " + event);
        this.mResipImHandler.mChatbotAnonymizeNotifyRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private void handleReportChatbotAsSpamResponseNotify(Notify notify) {
        if (notify.notiType() != 49) {
            Log.e(this.LOG_TAG, "handleChatbotAsSpamResp(): invalid notify");
            return;
        }
        ReportChatbotAsSpamResponse response = (ReportChatbotAsSpamResponse) notify.noti(new ReportChatbotAsSpamResponse());
        if (response != null) {
            ReportChatbotAsSpamRespEvent event = new ReportChatbotAsSpamRespEvent(response.uri(), response.requestId(), ResipTranslatorCollection.translateImResult(response.imError(), (Object) null).getImError());
            String str = this.LOG_TAG;
            IMSLog.s(str, "handleReportChatbotAsSpamResponseNotify: " + event);
            this.mResipImHandler.mReportChatbotAsSpamRespRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
        }
    }

    private ImIncomingMessageEvent parseImMessageParam(ImMessageParam param) {
        ImIncomingMessageEvent event = new ImIncomingMessageEvent();
        if (param == null) {
            return event;
        }
        List<Integer> notiList = new ArrayList<>();
        if (param.imdn() != null) {
            event.mImdnMessageId = param.imdn().messageId();
            for (int i = 0; i < param.imdn().notiLength(); i++) {
                notiList.add(Integer.valueOf(param.imdn().noti(i)));
            }
            event.mDispositionNotification = ResipTranslatorCollection.translateStackImdnNoti(notiList);
            try {
                if (!TextUtils.isEmpty(event.mImdnMessageId)) {
                    String datetime = param.imdn().datetime();
                    event.mImdnTime = !TextUtils.isEmpty(datetime) ? Iso8601.parse(datetime) : new Date();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                event.mImdnTime = new Date();
            }
            if (!TextUtils.isEmpty(param.imdn().originalToHdr())) {
                event.mOriginalToHdr = param.imdn().originalToHdr();
            }
            if (param.imdn().recRouteLength() > 0) {
                event.mImdnRecRouteList = new ArrayList();
                for (int i2 = 0; i2 < param.imdn().recRouteLength(); i2++) {
                    ImdnRecRoute route = param.imdn().recRoute(i2);
                    if (route != null) {
                        String str = this.LOG_TAG;
                        IMSLog.s(str, "imdn route: " + route.uri());
                        String str2 = this.LOG_TAG;
                        IMSLog.s(str2, "mImdnMessageId: " + event.mImdnMessageId);
                        event.mImdnRecRouteList.add(new ImImdnRecRoute(event.mImdnMessageId, route.uri(), route.name()));
                    }
                }
            }
        }
        event.mContentType = param.contentType();
        event.mBody = ResipTranslatorCollection.adjustMessageBody(param.body(), event.mContentType);
        if (event.mBody == null) {
            return null;
        }
        String sender = param.sender();
        if (sender != null) {
            String sender2 = sender.replaceAll("\\<|\\>", "");
            String str3 = this.LOG_TAG;
            IMSLog.s(str3, "parseImMessageParam sender=" + sender2);
            event.mSender = ImsUri.parse(sender2);
        }
        event.mIsRoutingMsg = param.silenceSupported();
        if (event.mIsRoutingMsg) {
            Log.i(this.LOG_TAG, "parseImMessageParam -> routing message");
            event.mRequestUri = ImsUri.parse(param.requestUri());
            event.mPAssertedId = ImsUri.parse(param.pAssertedId());
            event.mReceiver = ImsUri.parse(param.receiver());
        }
        event.mUserAlias = param.userAlias();
        if (param.deviceName() != null && !param.deviceName().isEmpty()) {
            event.mDeviceName = param.deviceName();
        }
        if (param.reliableMessage() != null && !param.reliableMessage().isEmpty()) {
            event.mReliableMessage = param.reliableMessage();
        }
        event.mCpimNamespaces = new ImCpimNamespaces();
        for (int i3 = 0; i3 < param.cpimNamespacesLength(); i3++) {
            CpimNamespace protoNamespace = param.cpimNamespaces(i3);
            if (protoNamespace != null) {
                event.mCpimNamespaces.addNamespace(protoNamespace.name(), protoNamespace.uri());
                for (int j = 0; j < protoNamespace.headersLength(); j++) {
                    Pair header = protoNamespace.headers(j);
                    if (!(header == null || header.key() == null)) {
                        event.mCpimNamespaces.getNamespace(protoNamespace.name()).addHeader(header.key(), header.value());
                    }
                }
            }
        }
        event.mExtraFt = param.extraFt();
        if (param.ccParticipantsLength() > 0) {
            event.mCcParticipants = new ArrayList();
            for (int i4 = 0; i4 < param.ccParticipantsLength(); i4++) {
                event.mCcParticipants.add(ImsUri.parse(param.ccParticipants(i4)));
            }
            String str4 = this.LOG_TAG;
            IMSLog.s(str4, "parseImMessageParam event.mCcParticipants=" + event.mCcParticipants);
        }
        return event;
    }
}
