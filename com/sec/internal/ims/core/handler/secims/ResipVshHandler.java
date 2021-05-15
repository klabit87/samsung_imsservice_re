package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.VshHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.VshIncomingSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.VshSessionEstablished;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.VshSessionTerminated;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestVshAcceptSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestVshStartSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestVshStopSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CshGeneralResponse;
import com.sec.internal.ims.servicemodules.csh.event.CshAcceptSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshCancelSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshErrorReason;
import com.sec.internal.ims.servicemodules.csh.event.CshRejectSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.CshSessionResult;
import com.sec.internal.ims.servicemodules.csh.event.ICshSuccessCallback;
import com.sec.internal.ims.servicemodules.csh.event.VshIncomingSessionEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshOrientation;
import com.sec.internal.ims.servicemodules.csh.event.VshResolution;
import com.sec.internal.ims.servicemodules.csh.event.VshSessionEstablishedEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshSessionTerminatedEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshStartSessionParams;
import com.sec.internal.ims.servicemodules.csh.event.VshSwitchCameraParams;
import com.sec.internal.ims.servicemodules.csh.event.VshVideoDisplayParams;
import com.sec.internal.ims.servicemodules.csh.event.VshViewType;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.sve.SecVideoEngineManager;

public class ResipVshHandler extends VshHandler {
    private static final int EVENT_ACCEPT_SESSION_DONE = 101;
    private static final int EVENT_ACCEPT_VSH_SESSION = 1;
    private static final int EVENT_CANCEL_SESSION_DONE = 104;
    private static final int EVENT_CANCEL_VSH_SESSION = 3;
    private static final int EVENT_REJECT_SESSION_DONE = 102;
    private static final int EVENT_REJECT_VSH_SESSION = 2;
    private static final int EVENT_RESET_VIDEO_DISPLAY = 7;
    private static final int EVENT_SET_VIDEO_DISPLAY = 6;
    private static final int EVENT_SET_VSH_PHONE_ORIENTATION = 5;
    private static final int EVENT_STACK_NOTIFY = 1000;
    private static final int EVENT_START_SESSION_DONE = 100;
    private static final int EVENT_START_VSH_SESSION = 0;
    private static final int EVENT_STOP_SESSION_DONE = 103;
    private static final int EVENT_STOP_VSH_SESSION = 4;
    private static final int EVENT_SWITCH_CAMERA = 8;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ResipVshHandler.class.getSimpleName();
    private final IImsFramework mImsFramework;
    private final RegistrantList mIncomingSessionRegistrants = new RegistrantList();
    private final RegistrantList mSessionEstablishedRegistrants = new RegistrantList();
    private final RegistrantList mSessionTerminatedRegistrants = new RegistrantList();
    private final StackIF mStackIf;
    private SecVideoEngineManager mSveManager = null;

    public ResipVshHandler(Looper looper, Context context, IImsFramework imsFramework) {
        super(looper);
        this.mImsFramework = imsFramework;
        StackIF instance = StackIF.getInstance();
        this.mStackIf = instance;
        instance.registerVshEvent(this, 1000, (Object) null);
        SecVideoEngineManager secVideoEngineManager = new SecVideoEngineManager(context, new SecVideoEngineManager.ConnectionListener() {
            public void onDisconnected() {
            }

            public void onConnected() {
                Log.i(ResipVshHandler.LOG_TAG, "sve connected.");
            }
        });
        this.mSveManager = secVideoEngineManager;
        secVideoEngineManager.connectService();
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != 1000) {
            switch (i) {
                case 0:
                    onStartVshSession((VshStartSessionParams) msg.obj);
                    return;
                case 1:
                    onAcceptVshSession((CshAcceptSessionParams) msg.obj);
                    return;
                case 2:
                    onRejectVshSession((CshRejectSessionParams) msg.obj);
                    return;
                case 3:
                    onCancelVshSession((CshCancelSessionParams) msg.obj);
                    return;
                case 4:
                    onStopVshSession((CshCancelSessionParams) msg.obj);
                    return;
                case 5:
                    onSetOrientation((VshOrientation) msg.obj);
                    return;
                case 6:
                    onSetVshVideoDisplay((VshVideoDisplayParams) msg.obj);
                    return;
                case 7:
                    onResetVshVideoDisplay();
                    return;
                case 8:
                    onSwitchCamera((VshSwitchCameraParams) msg.obj);
                    return;
                default:
                    switch (i) {
                        case 100:
                        case 101:
                            AsyncResult ar = (AsyncResult) msg.obj;
                            CshGeneralResponse response = (CshGeneralResponse) ar.result;
                            Message callback = (Message) ar.userObj;
                            if (callback != null) {
                                AsyncResult.forMessage(callback, new CshSessionResult((int) response.sessionId(), translateToCshResult(response.error())), (Throwable) null);
                                callback.sendToTarget();
                                return;
                            }
                            return;
                        case 102:
                        case 103:
                        case 104:
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
            }
        } else {
            handleNotify((Notify) ((AsyncResult) msg.obj).result);
        }
    }

    private void handleNotify(Notify notify) {
        switch (notify.notifyid()) {
            case Id.NOTIFY_VSH_INCOMING_SESSION /*17001*/:
                handleIncomingSessionNotify(notify);
                return;
            case Id.NOTIFY_VSH_SESSION_ESTABLISHED /*17002*/:
                handleSessionEstablishedNotify(notify);
                return;
            case Id.NOTIFY_VSH_SESSION_TERMINATED /*17003*/:
                handleSessionTerminatedNotify(notify);
                return;
            default:
                Log.w(LOG_TAG, "handleNotify(): unexpected id");
                return;
        }
    }

    private void handleIncomingSessionNotify(Notify notify) {
        if (notify.notiType() != 66) {
            Log.e(LOG_TAG, "Invalid notify");
            return;
        }
        VshIncomingSession proto = (VshIncomingSession) notify.noti(new VshIncomingSession());
        VshIncomingSessionEvent event = new VshIncomingSessionEvent((int) proto.sessionId(), ImsUri.parse(proto.remoteUri()), (String) null, 1, (String) null);
        String str = LOG_TAG;
        Log.i(str, "handleIncomingSessionNotify: " + event);
        this.mIncomingSessionRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private void handleSessionEstablishedNotify(Notify notify) {
        if (notify.notiType() != 67) {
            Log.e(LOG_TAG, "Invalid notify");
            return;
        }
        VshSessionEstablished proto = (VshSessionEstablished) notify.noti(new VshSessionEstablished());
        VshSessionEstablishedEvent event = new VshSessionEstablishedEvent((int) proto.sessionId(), translateToVshResolution(proto.resolution()));
        String str = LOG_TAG;
        Log.i(str, "handleIncomingSessionNotify: " + event);
        this.mSessionEstablishedRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private void handleSessionTerminatedNotify(Notify notify) {
        if (notify.notiType() != 68) {
            Log.e(LOG_TAG, "Invalid notify");
            return;
        }
        VshSessionTerminated proto = (VshSessionTerminated) notify.noti(new VshSessionTerminated());
        VshSessionTerminatedEvent event = new VshSessionTerminatedEvent((int) proto.sessionId(), translateToCshResult(proto.reason()));
        String str = LOG_TAG;
        Log.i(str, "handleSessionTerminatedNotify: " + event);
        this.mSessionTerminatedRegistrants.notifyRegistrants(new AsyncResult((Object) null, event, (Throwable) null));
    }

    private CshErrorReason translateToCshResult(int error) {
        if (error == 0) {
            return CshErrorReason.SUCCESS;
        }
        if (error == 3) {
            return CshErrorReason.CANCELED;
        }
        if (error == 6) {
            return CshErrorReason.RTP_RTCP_TIMEOUT;
        }
        if (error != 7) {
            return CshErrorReason.UNKNOWN;
        }
        return CshErrorReason.CSH_CAM_ERROR;
    }

    private VshResolution translateToVshResolution(int resolution) {
        switch (resolution) {
            case 0:
                return VshResolution.NONE;
            case 1:
                return VshResolution.QCIF;
            case 2:
                return VshResolution.QVGA;
            case 3:
                return VshResolution.VGA;
            case 4:
                return VshResolution.CIF;
            case 5:
                return VshResolution.QCIF_PORTRAIT;
            case 6:
                return VshResolution.QVGA_PORTRAIT;
            case 7:
                return VshResolution.VGA_PORTRAIT;
            case 8:
                return VshResolution.CIF_PORTRAIT;
            default:
                return VshResolution.QCIF;
        }
    }

    private void onStartVshSession(VshStartSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onStartVshSession(): " + params);
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "UA not found.");
            if (params.mCallback != null) {
                AsyncResult.forMessage(params.mCallback, new CshSessionResult(-1, CshErrorReason.ENGINE_ERROR), (Throwable) null);
                params.mCallback.sendToTarget();
                return;
            }
            return;
        }
        if (ua.getImsRegistration() != null) {
            String str2 = LOG_TAG;
            Log.i(str2, "bind network for VSH " + ua.getImsRegistration().getNetwork());
            this.mSveManager.bindToNetwork(ua.getImsRegistration().getNetwork());
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int remoteUriOffset = builder.createString((CharSequence) params.mReceiver);
        RequestVshStartSession.startRequestVshStartSession(builder);
        RequestVshStartSession.addRegistrationHandle(builder, (long) ua.getHandle());
        RequestVshStartSession.addRemoteUri(builder, remoteUriOffset);
        int offset = RequestVshStartSession.endRequestVshStartSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_VSH_START_SESSION);
        Request.addReqType(builder, (byte) 96);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_VSH_START_SESSION, builder, Request.endRequest(builder), obtainMessage(100, params.mCallback));
    }

    private void onAcceptVshSession(CshAcceptSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onAcceptVshSession(): " + params);
        int sessionHandle = params.mSessionId;
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "UA not found.");
            if (params.mCallback != null) {
                AsyncResult.forMessage(params.mCallback, new CshSessionResult(sessionHandle, CshErrorReason.ENGINE_ERROR), (Throwable) null);
                params.mCallback.sendToTarget();
                return;
            }
            return;
        }
        if (ua.getImsRegistration() != null) {
            String str2 = LOG_TAG;
            Log.i(str2, "bind network for VSH " + ua.getImsRegistration().getNetwork());
            this.mSveManager.bindToNetwork(ua.getImsRegistration().getNetwork());
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestVshAcceptSession.startRequestVshAcceptSession(builder);
        RequestVshAcceptSession.addSessionId(builder, (long) sessionHandle);
        int offset = RequestVshAcceptSession.endRequestVshAcceptSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_VSH_ACCEPT_SESSION);
        Request.addReqType(builder, (byte) 97);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_VSH_ACCEPT_SESSION, builder, Request.endRequest(builder), obtainMessage(101, params.mCallback));
    }

    private void onRejectVshSession(CshRejectSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onRejectVshSession(): " + params);
        int sessionHandle = params.mSessionId;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestVshStopSession.startRequestVshStopSession(builder);
        RequestVshStopSession.addSessionId(builder, (long) sessionHandle);
        int offset = RequestVshStopSession.endRequestVshStopSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_VSH_STOP_SESSION);
        Request.addReq(builder, offset);
        Request.addReqType(builder, (byte) 98);
        sendRequestToStack(Id.REQUEST_VSH_STOP_SESSION, builder, Request.endRequest(builder), obtainMessage(102, params.mCallback));
    }

    private void onCancelVshSession(CshCancelSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onCancelVshSession(): " + params);
        int sessionHandle = params.mSessionId;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestVshStopSession.startRequestVshStopSession(builder);
        RequestVshStopSession.addSessionId(builder, (long) sessionHandle);
        int offset = RequestVshStopSession.endRequestVshStopSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_VSH_STOP_SESSION);
        Request.addReqType(builder, (byte) 98);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_VSH_STOP_SESSION, builder, Request.endRequest(builder), obtainMessage(104, params.mCallback));
    }

    private void onStopVshSession(CshCancelSessionParams params) {
        String str = LOG_TAG;
        Log.i(str, "onStopVshSession(): " + params);
        int sessionHandle = params.mSessionId;
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        RequestVshStopSession.startRequestVshStopSession(builder);
        RequestVshStopSession.addSessionId(builder, (long) sessionHandle);
        int offset = RequestVshStopSession.endRequestVshStopSession(builder);
        Request.startRequest(builder);
        Request.addReqid(builder, Id.REQUEST_VSH_STOP_SESSION);
        Request.addReqType(builder, (byte) 98);
        Request.addReq(builder, offset);
        sendRequestToStack(Id.REQUEST_VSH_STOP_SESSION, builder, Request.endRequest(builder), obtainMessage(103, params.mCallback));
    }

    /* renamed from: com.sec.internal.ims.core.handler.secims.ResipVshHandler$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$VshOrientation;

        static {
            int[] iArr = new int[VshOrientation.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$VshOrientation = iArr;
            try {
                iArr[VshOrientation.LANDSCAPE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$VshOrientation[VshOrientation.PORTRAIT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$VshOrientation[VshOrientation.FLIPPED_LANDSCAPE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$VshOrientation[VshOrientation.REVERSE_PORTRAIT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private void onSetOrientation(VshOrientation orientation) {
        int value = 0;
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$ims$servicemodules$csh$event$VshOrientation[orientation.ordinal()];
        if (i == 1) {
            value = 1;
        } else if (i == 2) {
            value = 0;
        } else if (i == 3) {
            value = 3;
        } else if (i == 4) {
            value = 2;
        }
        this.mSveManager.setOrientation(value);
    }

    private void onSetVshVideoDisplay(VshVideoDisplayParams params) {
        if (params.mViewType == VshViewType.LOCAL) {
            this.mSveManager.setPreviewSurface(params.mVideoDisplay.getWindowHandle(), params.mVideoDisplay.getColor());
        } else {
            this.mSveManager.setDisplaySurface(params.mVideoDisplay.getWindowHandle(), params.mVideoDisplay.getColor());
        }
        params.mCallback.onSuccess();
    }

    private void onResetVshVideoDisplay() {
    }

    private void onSwitchCamera(VshSwitchCameraParams params) {
        this.mSveManager.switchCamera();
        params.mCallback.onSuccess();
    }

    public void setVshPhoneOrientation(VshOrientation orientation) {
        sendMessage(obtainMessage(5, orientation));
    }

    public void startVshSession(VshStartSessionParams params) {
        sendMessage(obtainMessage(0, params));
    }

    public void acceptVshSession(CshAcceptSessionParams params) {
        sendMessage(obtainMessage(1, params));
    }

    public void rejectVshSession(CshRejectSessionParams params) {
        sendMessage(obtainMessage(2, params));
    }

    public void cancelVshSession(CshCancelSessionParams params) {
        sendMessage(obtainMessage(3, params));
    }

    public void stopVshSession(CshCancelSessionParams params) {
        sendMessage(obtainMessage(4, params));
    }

    public void switchCamera(VshSwitchCameraParams params) {
        sendMessage(obtainMessage(8, params));
    }

    public void setVshVideoDisplay(VshVideoDisplayParams params) {
        sendMessage(obtainMessage(6, params));
    }

    public void resetVshVideoDisplay(VshVideoDisplayParams params) {
        sendMessage(obtainMessage(7, params));
    }

    public void registerForVshIncomingSession(Handler h, int what, Object obj) {
        this.mIncomingSessionRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVshIncomingSession(Handler h) {
        this.mIncomingSessionRegistrants.remove(h);
    }

    public void registerForVshSessionEstablished(Handler h, int what, Object obj) {
        this.mSessionEstablishedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVshSessionEstablished(Handler h) {
        this.mSessionEstablishedRegistrants.remove(h);
    }

    public void registerForVshSessionTerminated(Handler h, int what, Object obj) {
        this.mSessionTerminatedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForVshSessionTerminated(Handler h) {
        this.mSessionTerminatedRegistrants.remove(h);
    }

    private UserAgent getUserAgent() {
        int dds = SimUtil.getDefaultPhoneId();
        String str = LOG_TAG;
        Log.i(str, "getUserAgent() of SIM slot: " + dds);
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent("vs", dds);
    }

    private void sendRequestToStack(int id, FlatBufferBuilder request, int offset, Message callback) {
        UserAgent ua = getUserAgent();
        if (ua == null) {
            Log.e(LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            ua.sendRequestToStack(new ResipStackRequest(id, request, offset, callback));
        }
    }
}
