package com.sec.internal.ims.core.handler.secims;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.ims.core.handler.IshHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.IshIncomingSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.IshSessionEstablished;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.IshSessionTerminated;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.IshTransferProgress;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestIshAcceptSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestIshStartSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestIshStopSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CshGeneralResponse;
import com.sec.internal.ims.servicemodules.csh.event.CshCancelSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshErrorReason;
import com.sec.internal.ims.servicemodules.csh.event.CshRejectSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshSessionResult;
import com.sec.internal.ims.servicemodules.csh.event.ICshSuccessCallback;
import com.sec.internal.ims.servicemodules.csh.event.IshAcceptSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.IshFileTransfer;
import com.sec.internal.ims.servicemodules.csh.event.IshIncomingSessionEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshSessionEstablishedEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshStartSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferCompleteEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferFailedEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferProgressEvent;
import com.sec.internal.interfaces.ims.IImsFramework;
import java.util.Locale;

public class ResipIshHandler extends IshHandler {
    private static final int EVENT_ACCEPT_ISH_SESSION = 1;
    private static final int EVENT_ACCEPT_SESSION_DONE = 102;
    private static final int EVENT_CANCEL_ISH_SESSION = 3;
    private static final int EVENT_CANCEL_SESSION_DONE = 104;
    private static final int EVENT_REJECT_ISH_SESSION = 2;
    private static final int EVENT_REJECT_SESSION_DONE = 103;
    private static final int EVENT_STACK_NOTIFY = 1000;
    private static final int EVENT_START_ISH_SESSION = 0;
    private static final int EVENT_START_SESSION_DONE = 101;
    private static final int EVENT_STOP_ISH_SESSION = 4;
    private static final int EVENT_STOP_SESSION_DONE = 105;
    private static final String LOG_TAG = ResipIshHandler.class.getSimpleName();
    private final IImsFramework mImsFramework;
    private final RegistrantList mIncomingSessionRegistrants = new RegistrantList();
    private final RegistrantList mSessionEstablishedRegistrants = new RegistrantList();
    private final RegistrantList mTransferCompleteRegistrants = new RegistrantList();
    private final RegistrantList mTransferFailedRegistrants = new RegistrantList();
    private final RegistrantList mTransferProgressRegistrants = new RegistrantList();

    public ResipIshHandler(Looper looper, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
        StackIF.getInstance().registerIshEvent(this, 1000, (Object) null);
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            onStartIshSession((IshStartSessionParams) msg.obj);
        } else if (i == 1) {
            onAcceptIshSession((IshAcceptSessionParams) msg.obj);
        } else if (i == 2) {
            onRejectIshSession((CshRejectSessionParams) msg.obj);
        } else if (i == 3) {
            onCancelIshSession((CshCancelSessionParams) msg.obj);
        } else if (i == 4) {
            onStopIshSession((CshCancelSessionParams) msg.obj);
        } else if (i != 1000) {
            switch (i) {
                case 101:
                case 102:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    CshGeneralResponse response = (CshGeneralResponse) ar.result;
                    Message callback = (Message) ar.userObj;
                    if (callback != null) {
                        AsyncResult.forMessage(callback, new CshSessionResult((int) response.sessionId(), translateToCshResult(response.error())), (Throwable) null);
                        callback.sendToTarget();
                        return;
                    }
                    return;
                case 103:
                case 104:
                case 105:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    ICshSuccessCallback mCallback = (ICshSuccessCallback) ar2.userObj;
                    if (translateToCshResult(((CshGeneralResponse) ar2.result).error()) == CshErrorReason.SUCCESS) {
                        mCallback.onSuccess();
                        return;
                    } else {
                        mCallback.onFailure();
                        return;
                    }
                default:
                    Log.e(LOG_TAG, "handleMessage: Undefined message.");
                    return;
            }
        } else {
            handleNotify((Notify) ((AsyncResult) msg.obj).result);
        }
    }

    private CshErrorReason translateToCshResult(int error) {
        switch (error) {
            case 0:
                return CshErrorReason.SUCCESS;
            case 1:
                return CshErrorReason.USER_BUSY;
            case 2:
                return CshErrorReason.TEMPORAIRLY_NOT_AVAILABLE;
            case 3:
                return CshErrorReason.CANCELED;
            case 4:
                return CshErrorReason.REJECTED;
            case 5:
                return CshErrorReason.FORBIDDEN;
            case 6:
                return CshErrorReason.MSRP_TIMEOUT;
            default:
                return CshErrorReason.UNKNOWN;
        }
    }

    private void handleNotify(Notify notify) {
        switch (notify.notifyid()) {
            case Id.NOTIFY_ISH_INCOMING_SESSION /*16001*/:
                handleIncomingSessionNotify(notify);
                return;
            case Id.NOTIFY_ISH_SESSION_ESTABLISHED /*16002*/:
                handleSessionEstablishedNotify(notify);
                return;
            case Id.NOTIFY_ISH_SESSION_TERMINATED /*16003*/:
                handleSessionTerminatedNotify(notify);
                return;
            case Id.NOTIFY_ISH_TRANSFER_PROGRESS /*16004*/:
                handleTransferProgressNotify(notify);
                return;
            default:
                Log.w(LOG_TAG, "handleNotify(): unexpected id");
                return;
        }
    }

    private void onStartIshSession(IshStartSessionParams params) {
        int ReceiverOffset;
        int contentTypeOffset;
        IshStartSessionParams ishStartSessionParams = params;
        Log.i(LOG_TAG, "onStartIshSession: " + ishStartSessionParams);
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "onStartIshSession: ISH UA not registered");
            AsyncResult.forMessage(ishStartSessionParams.mCallback, new CshSessionResult(-1, CshErrorReason.ENGINE_ERROR), (Throwable) null);
            return;
        }
        String path = ishStartSessionParams.mfile.getPath();
        if (path == null) {
            Log.e(LOG_TAG, "onStartIshSession: path is null");
            return;
        }
        String contentType = ContentTypeTranslator.translate(path.substring(path.lastIndexOf(".") + 1).toUpperCase(Locale.ENGLISH));
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        if (ishStartSessionParams.mReceiver != null) {
            ReceiverOffset = builder.createString((CharSequence) ishStartSessionParams.mReceiver);
        } else {
            ReceiverOffset = -1;
        }
        if (contentType != null) {
            contentTypeOffset = builder.createString((CharSequence) contentType);
        } else {
            contentTypeOffset = -1;
        }
        int pathOffset = builder.createString((CharSequence) path);
        RequestIshStartSession.startRequestIshStartSession(builder);
        RequestIshStartSession.addRegistrationHandle(builder, (long) ua.getHandle());
        if (ReceiverOffset != -1) {
            RequestIshStartSession.addRemoteUri(builder, ReceiverOffset);
        }
        if (contentTypeOffset != -1) {
            RequestIshStartSession.addContentType(builder, contentTypeOffset);
        }
        if (pathOffset != -1) {
            RequestIshStartSession.addFilePath(builder, pathOffset);
        }
        RequestIshStartSession.addSize(builder, ishStartSessionParams.mfile.getSize());
        int requestIshStartSession = RequestIshStartSession.endRequestIshStartSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 1001);
        Request.addReqType(builder, ReqMsg.request_ish_start_session);
        Request.addReq(builder, requestIshStartSession);
        int i = pathOffset;
        sendRequestToStack(1001, builder, Request.endRequest(builder), obtainMessage(101, ishStartSessionParams.mCallback), ua);
    }

    private void onAcceptIshSession(IshAcceptSessionParams params) {
        int PathOffset;
        Log.i(LOG_TAG, "onAcceptIshSession(): " + params);
        int sessionHandle = params.mSessionId;
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "onStartIshSession: ISH UA not registered");
            AsyncResult.forMessage(params.mCallback, new CshSessionResult(-1, CshErrorReason.ENGINE_ERROR), (Throwable) null);
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        if (params.mPath != null) {
            PathOffset = builder.createString((CharSequence) params.mPath);
        } else {
            PathOffset = -1;
        }
        RequestIshAcceptSession.startRequestIshAcceptSession(builder);
        RequestIshAcceptSession.addSessionId(builder, (long) sessionHandle);
        if (PathOffset != -1) {
            RequestIshAcceptSession.addFilePath(builder, PathOffset);
        }
        int requestIshAcceptSession = RequestIshAcceptSession.endRequestIshAcceptSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 1002);
        Request.addReqType(builder, ReqMsg.request_ish_accept_session);
        Request.addReq(builder, requestIshAcceptSession);
        sendRequestToStack(1002, builder, Request.endRequest(builder), obtainMessage(101, params.mCallback), ua);
    }

    private void onRejectIshSession(CshRejectSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onRejectIshSession(): " + params);
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "onStartIshSession: ISH UA not registered");
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestIshStopSession.startRequestIshStopSession(builder);
        RequestIshStopSession.addSessionId(builder, (long) params.mSessionId);
        int requestIshStopSession = RequestIshStopSession.endRequestIshStopSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 1003);
        Request.addReqType(builder, ReqMsg.request_ish_stop_session);
        Request.addReq(builder, requestIshStopSession);
        sendRequestToStack(1003, builder, Request.endRequest(builder), obtainMessage(105, params.mCallback), ua);
    }

    private void onCancelIshSession(CshCancelSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onCancelIshSession(): " + params);
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "onStartIshSession: ISH UA not registered");
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestIshStopSession.startRequestIshStopSession(builder);
        RequestIshStopSession.addSessionId(builder, (long) params.mSessionId);
        int requestIshStopSession = RequestIshStopSession.endRequestIshStopSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 1003);
        Request.addReqType(builder, ReqMsg.request_ish_stop_session);
        Request.addReq(builder, requestIshStopSession);
        sendRequestToStack(1003, builder, Request.endRequest(builder), obtainMessage(105, params.mCallback), ua);
    }

    private void onStopIshSession(CshCancelSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onStopIshSession(): " + params);
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "onStartIshSession: ISH UA not registered");
            return;
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestIshStopSession.startRequestIshStopSession(builder);
        RequestIshStopSession.addSessionId(builder, (long) params.mSessionId);
        int requestIshStopSession = RequestIshStopSession.endRequestIshStopSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, 1003);
        Request.addReqType(builder, ReqMsg.request_ish_stop_session);
        Request.addReq(builder, requestIshStopSession);
        sendRequestToStack(1003, builder, Request.endRequest(builder), obtainMessage(105, params.mCallback), ua);
    }

    private void handleIncomingSessionNotify(Notify notify) {
        if (notify.notiType() != 62) {
            Log.e(LOG_TAG, "handleIncomingSessionNotify(): invalid notify");
            return;
        }
        IshIncomingSession noti = (IshIncomingSession) notify.noti(new IshIncomingSession());
        IshIncomingSessionEvent event = new IshIncomingSessionEvent((int) noti.sessionId(), ImsUri.parse(noti.remoteUri()), (String) null, new IshFileTransfer(noti.fileName(), (int) noti.size(), noti.contentType()));
        String str = LOG_TAG;
        Log.i(str, "handleIncomingSessionNotify: " + event);
        this.mIncomingSessionRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private void handleSessionEstablishedNotify(Notify notify) {
        if (notify.notiType() != 63) {
            Log.e(LOG_TAG, "handleSessionEstablishedNotify(): invalid notify");
            return;
        }
        IshSessionEstablished sessionEstablished = (IshSessionEstablished) notify.noti(new IshSessionEstablished());
        String str = LOG_TAG;
        Log.i(str, "handleSessionEstablishedNotify: " + sessionEstablished.error());
        if (translateToCshResult(sessionEstablished.error()) == CshErrorReason.SUCCESS) {
            this.mSessionEstablishedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new IshSessionEstablishedEvent((int) sessionEstablished.sessionId()), (Throwable) null));
        }
    }

    private void handleSessionTerminatedNotify(Notify notify) {
        if (notify.notiType() != 64) {
            Log.e(LOG_TAG, "handleSessionTerminatedNotify(): invalid notify");
            return;
        }
        IshSessionTerminated sessionTerminated = (IshSessionTerminated) notify.noti(new IshSessionTerminated());
        String str = LOG_TAG;
        Log.i(str, "handleSessionTerminatedNotify: " + sessionTerminated.reason());
        CshErrorReason reason = translateToCshResult(sessionTerminated.reason());
        if (reason == CshErrorReason.SUCCESS) {
            this.mTransferCompleteRegistrants.notifyRegistrants(new AsyncResult((Object) null, new IshTransferCompleteEvent((int) sessionTerminated.sessionId()), (Throwable) null));
        } else {
            this.mTransferFailedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new IshTransferFailedEvent((int) sessionTerminated.sessionId(), reason), (Throwable) null));
        }
    }

    private void handleTransferProgressNotify(Notify notify) {
        if (notify.notiType() != 65) {
            Log.e(LOG_TAG, "handleTransferProgressNotify(): invalid notify");
            return;
        }
        IshTransferProgress transferProgress = (IshTransferProgress) notify.noti(new IshTransferProgress());
        String str = LOG_TAG;
        Log.i(str, "handleTransferProgressNotify: id=" + transferProgress.sessionId() + "(" + transferProgress.transferred() + "/" + transferProgress.total() + ")");
        this.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, new IshTransferProgressEvent((int) transferProgress.sessionId(), transferProgress.transferred()), (Throwable) null));
    }

    public void startIshSession(IshStartSessionParams params) {
        sendMessage(obtainMessage(0, params));
    }

    public void acceptIshSession(IshAcceptSessionParams params) {
        sendMessage(obtainMessage(1, params));
    }

    public void rejectIshSession(CshRejectSessionParams params) {
        sendMessage(obtainMessage(2, params));
    }

    public void cancelIshSession(CshCancelSessionParams params) {
        sendMessage(obtainMessage(3, params));
    }

    public void stopIshSession(CshCancelSessionParams params) {
        sendMessage(obtainMessage(4, params));
    }

    public void registerForIshSessionEstablished(Handler h, int what, Object obj) {
        this.mSessionEstablishedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIshSessionEstablished(Handler h) {
        this.mSessionEstablishedRegistrants.remove(h);
    }

    public void registerForIshTransferFailed(Handler h, int what, Object obj) {
        this.mTransferFailedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIshTransferFailed(Handler h) {
        this.mTransferFailedRegistrants.remove(h);
    }

    public void registerForIshTransferComplete(Handler h, int what, Object obj) {
        this.mTransferCompleteRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIshTransferComplete(Handler h) {
        this.mTransferCompleteRegistrants.remove(h);
    }

    public void registerForIshTransferProgress(Handler h, int what, Object obj) {
        this.mTransferProgressRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIshTransferProgress(Handler h) {
        this.mTransferProgressRegistrants.remove(h);
    }

    public void registerForIshIncomingSession(Handler h, int what, Object obj) {
        this.mIncomingSessionRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForIshIncomingSession(Handler h) {
        this.mIncomingSessionRegistrants.remove(h);
    }

    private UserAgent getUserAgent() {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent("is");
    }

    private void sendRequestToStack(int id, FlatBufferBuilder request, int offset, Message callback, UserAgent ua) {
        if (ua == null) {
            Log.e(LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            ua.sendRequestToStack(new ResipStackRequest(id, request, offset, callback));
        }
    }
}
