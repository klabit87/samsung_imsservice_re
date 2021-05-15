package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.SipReasonBmc;
import com.sec.internal.constants.ims.SipReasonKor;
import com.sec.internal.constants.ims.SipReasonOptus;
import com.sec.internal.constants.ims.SipReasonRjil;
import com.sec.internal.constants.ims.SipReasonTmoUs;
import com.sec.internal.constants.ims.SipReasonUscc;
import com.sec.internal.constants.ims.SipReasonVzw;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class CallStateMachine extends StateMachine {
    public static final int ACCEPT = 22;
    public static final int ADD_PARTICIPANT = 53;
    public static final int CANCEL_TRANSFER = 60;
    public static final int CHECK_VIDEO_DBR = 25;
    public static final int DELAYED_CAMSTART = 24;
    public static final int EMERGENCY_INVITE = 14;
    public static final int EXTENDS_CONFERENCE = 73;
    protected static final boolean FEATURE_FAST_ACCEPT = false;
    public static final int FORCE_NOTIFY_CURRENT_CODEC = 100;
    public static final int HANDLE_DTMF = 56;
    public static final int HOLD = 51;
    public static final int HOLD_VIDEO = 80;
    public static final int INCOMING = 21;
    public static final int INFO = 101;
    public static final int LOCATION_ACQUIRING = 13;
    private static final String LOG_TAG = "CallStateMachine";
    public static final int MERGE = 72;
    public static final int NOTIFY_ERROR = 26;
    public static final int ON_100_TRYING_TIMEOUT = 208;
    public static final int ON_BUSY = 42;
    public static final int ON_CALLING = 33;
    public static final int ON_CAMERA_START_FAILED = 207;
    public static final int ON_CMC_DTMF_EVENT = 86;
    public static final int ON_CMC_INFO_EVENT = 87;
    public static final int ON_DEDICATED_BEARER_LOST = 5000;
    public static final int ON_DUMMY_DNS_TIMER_EXPIRED = 305;
    public static final int ON_E911_INVITE_TILL_180_TIMER_FAIL = 307;
    public static final int ON_EARLYMEDIA = 32;
    public static final int ON_ENDED = 3;
    public static final int ON_EPDG_CONNECTION_CHANGED = 400;
    public static final int ON_EPDN_SETUP_FAIL = 306;
    public static final int ON_ERROR = 4;
    public static final int ON_ESTABLISHED = 41;
    public static final int ON_EXTEND_TO_CONFERENCE = 74;
    public static final int ON_FORCE_ESTABLISHED = 600;
    public static final int ON_FORWARDED = 36;
    public static final int ON_HELD_BOTH = 63;
    public static final int ON_HELD_LOCAL = 61;
    public static final int ON_HELD_REMOTE = 62;
    public static final int ON_LOCATION_ACQUIRING_SUCCESS = 501;
    public static final int ON_LOCATION_ACQUIRING_TIMEOUT = 500;
    public static final int ON_LTE_911_FAIL = 303;
    public static final int ON_LTE_911_FAIL_AFTER_DELAY = 304;
    public static final int ON_MODIFIED = 91;
    public static final int ON_NEXT_PCSCF_CHANGED = 402;
    public static final int ON_OUTGOING_CALL_REG_TIMEOUT = 211;
    public static final int ON_POOR_VIDEO_TIMER_EXPIRED = 205;
    public static final int ON_RECORD_EVENT = 700;
    public static final int ON_REFER_STATUS = 75;
    public static final int ON_REINVITE_TIMER_EXPIRED = 302;
    public static final int ON_RESUME_CALL_RETRY_TIMEOUT = 202;
    public static final int ON_RINGINGBACK = 34;
    public static final int ON_RING_TIMEOUT = 204;
    public static final int ON_RRC_RELEASED = 401;
    public static final int ON_RTT_DEDICATED_BEARER_LOST = 210;
    public static final int ON_RTT_DEDICATED_BEARER_TIMER_EXPIRED = 209;
    public static final int ON_SESSIONPROGRESS = 35;
    public static final int ON_SESSIONPROGRESS_TIMEOUT = 203;
    public static final int ON_SWITCH_REQUEST = 55;
    public static final int ON_TIMER_VZW_EXPIRED = 301;
    public static final int ON_TRYING = 31;
    public static final int ON_USSD_INDICATION = 94;
    public static final int ON_USSD_RESPONSE = 93;
    public static final int ON_VIDEO_HELD = 82;
    public static final int ON_VIDEO_HOLD_FAILED = 84;
    public static final int ON_VIDEO_RESUMED = 83;
    public static final int ON_VIDEO_RESUME_FAILED = 85;
    public static final int ON_VIDEO_RTP_RTCP_TIMEOUT = 206;
    public static final int PULLING = 12;
    public static final int REJECT = 23;
    public static final int REMOVE_PARTICIPANT = 54;
    public static final int RESUME = 71;
    public static final int RESUME_VIDEO = 81;
    public static final int RE_INVITE = 502;
    public static final int SEND_TEXT = 64;
    public static final int START = 11;
    public static final int TERMINATE = 1;
    public static final int TERMINATED = 2;
    public static final int TRANSFER_REQUEST = 59;
    public static final int UPDATE = 52;
    protected static final int VZW_TTY_REINVITE_TIMEOUT = 2000;
    int callType = 0;
    int errorCode = -1;
    String errorMessage = "";
    boolean isDeferedVideoResume = false;
    boolean isLocationAcquiringTriggered = false;
    boolean isRequestTtyFull = false;
    int lazerErrorCode = -1;
    String lazerErrorMessage = "";
    protected ImsAlertingCall mAlertingCall = null;
    protected boolean mCallInitEPDG = false;
    protected long mCallInitTime = 0;
    protected String mCallTypeHistory = "";
    protected boolean mCameraUsedAtOtherApp = false;
    protected long mCmcCallEstablishTime = 0;
    protected boolean mConfCallAdded = false;
    protected Context mContext = null;
    protected ImsDefaultCall mDefaultCall = null;
    protected ImsEndingCall mEndingCall = null;
    protected ImsHeldCall mHeldCall = null;
    protected CallProfile mHeldProfile = null;
    protected boolean mHoldBeforeTransfer = false;
    protected ImsHoldingCall mHoldingCall = null;
    protected CallProfile mHoldingProfile = null;
    protected ImsHoldingVideo mHoldingVideo = null;
    protected ImsInCall mInCall = null;
    protected ImsIncomingCall mIncomingCall = null;
    protected boolean mIsBigDataEndReason = false;
    protected boolean mIsCheckVideoDBR = false;
    protected boolean mIsCmcHandover = false;
    protected boolean mIsPendingCall = false;
    protected boolean mIsSentMobileCareEvt = false;
    protected boolean mIsStartCameraSuccess = true;
    protected boolean mIsWPSCall = false;
    protected RemoteCallbackList<IImsCallSessionEventListener> mListeners = null;
    private int mLocalVideoRtcpPort = 0;
    private int mLocalVideoRtpPort = 0;
    protected IImsMediaController mMediaController = null;
    protected Mno mMno = Mno.DEFAULT;
    protected ImsModifyRequested mModifyRequested = null;
    protected ImsModifyingCall mModifyingCall = null;
    protected CallProfile mModifyingProfile = null;
    protected IVolteServiceModuleInternal mModule = null;
    protected boolean mNeedToLateEndedNotify = false;
    protected boolean mNeedToWaitEndcall = false;
    protected NetworkStatsOnPortHandler mNetworkStatsOnPortHandler = null;
    protected HandlerThread mNetworkStatsOnPortThread = null;
    protected boolean mOnErrorDelayed = false;
    protected ImsOutgoingCall mOutgoingCall = null;
    protected boolean mPreAlerting = false;
    protected State mPrevState;
    protected ImsReadyToCall mReadyToCall = null;
    protected ImsRegistration mRegistration = null;
    protected IRegistrationManager mRegistrationManager = null;
    protected boolean mReinvite = false;
    protected boolean mRemoteHeld = false;
    private int mRemoteVideoRtcpPort = 0;
    private int mRemoteVideoRtpPort = 0;
    boolean mRequestLocation = false;
    protected ImsResumingCall mResumingCall = null;
    protected ImsResumingVideo mResumingVideo = null;
    protected Message mRetriggerTimeoutMessage = null;
    protected boolean mRetryInprogress = false;
    protected Message mRingTimeoutMessage = null;
    protected String mSIPFlowInfo = "";
    protected ImsCallSession mSession = null;
    protected ITelephonyManager mTelephonyManager;
    protected CallStateMachine mThisSm = this;
    protected boolean mTransferRequested = false;
    boolean mTryingReceived = false;
    protected boolean mUserAnswered = false;
    protected ImsVideoHeld mVideoHeld = null;
    protected boolean mVideoRTPtimeout = false;
    protected IVolteServiceInterface mVolteSvcIntf = null;
    boolean quit = false;
    SipError sipError = null;
    SipReason sipReason = null;
    boolean srvccStarted = false;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    protected CallStateMachine(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, String name, Looper looper) {
        super(name, looper);
        this.mSession = session;
        this.mRegistration = reg;
        this.mModule = volteModule;
        this.mContext = context;
        this.mVolteSvcIntf = stackIf;
        this.mRegistrationManager = rm;
        this.mMediaController = mediactnr;
        this.mListeners = listener;
        this.mMno = mno;
        ImsCallSession imsCallSession = session;
        ImsReadyToCall imsReadyToCall = r0;
        Looper looper2 = looper;
        ImsReadyToCall imsReadyToCall2 = new ImsReadyToCall(context, imsCallSession, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper2, this);
        this.mReadyToCall = imsReadyToCall;
        this.mIncomingCall = new ImsIncomingCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mOutgoingCall = new ImsOutgoingCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mAlertingCall = new ImsAlertingCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mInCall = new ImsInCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mHoldingCall = new ImsHoldingCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mHeldCall = new ImsHeldCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mResumingCall = new ImsResumingCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mModifyingCall = new ImsModifyingCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mModifyRequested = new ImsModifyRequested(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mHoldingVideo = new ImsHoldingVideo(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mVideoHeld = new ImsVideoHeld(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mResumingVideo = new ImsResumingVideo(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mEndingCall = new ImsEndingCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
        this.mDefaultCall = new ImsDefaultCall(this.mContext, imsCallSession, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, looper2, this);
    }

    /* access modifiers changed from: protected */
    public void init() {
        addState(this.mReadyToCall);
        addState(this.mIncomingCall);
        addState(this.mOutgoingCall);
        addState(this.mAlertingCall);
        addState(this.mInCall);
        addState(this.mHoldingCall);
        addState(this.mHeldCall);
        addState(this.mResumingCall);
        addState(this.mModifyingCall);
        addState(this.mModifyRequested);
        addState(this.mEndingCall);
        addState(this.mHoldingVideo, this.mInCall);
        addState(this.mVideoHeld, this.mInCall);
        addState(this.mResumingVideo, this.mInCall);
        setInitialState(this.mReadyToCall);
        HandlerThread handlerThread = new HandlerThread("NetworkStat");
        this.mNetworkStatsOnPortThread = handlerThread;
        handlerThread.start();
        this.mNetworkStatsOnPortHandler = new NetworkStatsOnPortHandler(this.mSession.getPhoneId(), this.mMno, this.mNetworkStatsOnPortThread.getLooper());
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mCallTypeHistory = "" + this.mSession.getCallProfile().getCallType();
        this.mCallInitEPDG = this.mSession.isEpdgCall();
    }

    /* access modifiers changed from: protected */
    public void onRegistrationDone(ImsRegistration regInfo) {
        this.mRegistration = regInfo;
        this.mReadyToCall.mRegistration = regInfo;
        this.mIncomingCall.mRegistration = regInfo;
        this.mOutgoingCall.mRegistration = regInfo;
        this.mAlertingCall.mRegistration = regInfo;
        this.mInCall.mRegistration = regInfo;
        this.mHoldingCall.mRegistration = regInfo;
        this.mHeldCall.mRegistration = regInfo;
        this.mResumingCall.mRegistration = regInfo;
        this.mModifyingCall.mRegistration = regInfo;
        this.mModifyRequested.mRegistration = regInfo;
        this.mHoldingVideo.mRegistration = regInfo;
        this.mVideoHeld.mRegistration = regInfo;
        this.mResumingVideo.mRegistration = regInfo;
        this.mEndingCall.mRegistration = regInfo;
    }

    /* access modifiers changed from: protected */
    public int determineCamera(int calltype, boolean isForSwitchRcved) {
        int camera = -1;
        if (calltype == 2 || calltype == 8 || calltype == 19) {
            camera = 1;
        } else if (calltype == 6) {
            camera = !this.mModule.getSessionByCallType(2).isEmpty() ? 2 : 1;
        } else if (calltype == 3) {
            camera = this.mMno == Mno.VZW ? 0 : 1;
        } else if (this.mMno != Mno.VZW && isForSwitchRcved && calltype == 4) {
            camera = 1;
        }
        if (camera >= 0 && this.mSession.mLastUsedCamera >= 0) {
            Log.i(LOG_TAG, "Using mSession.mLastUsedCamera: " + this.mSession.mLastUsedCamera);
            camera = this.mSession.mLastUsedCamera;
        }
        Log.i(LOG_TAG, "determineCamera calltype: " + calltype + ", isForSwitchRcved: " + isForSwitchRcved + ", camera: " + camera);
        return camera;
    }

    /* access modifiers changed from: protected */
    public void unhandledMessage(Message msg) {
        Log.i(LOG_TAG, "[ANY_STATE] unhandledMessage " + msg.what);
        int i = msg.what;
        if (i == 1) {
            this.mDefaultCall.terminate_ANYSTATE(msg);
            this.mDefaultCall.handleBigData_ANYSTATE(msg);
            transitionTo(this.mEndingCall);
        } else if (i == 52) {
            this.mDefaultCall.update_ANYSTATE(msg);
        } else if (i != 100) {
            if (i != 303) {
                if (i == 400) {
                    this.mDefaultCall.epdgConnChanged_ANYSTATE(msg);
                    return;
                } else if (i == 600) {
                    IState currentState = getCurrentState();
                    ImsInCall imsInCall = this.mInCall;
                    if (currentState == imsInCall) {
                        imsInCall.enter();
                        return;
                    } else {
                        transitionTo(imsInCall);
                        return;
                    }
                } else if (i == 5000) {
                    this.mDefaultCall.dbrLost_ANYSTATE(msg);
                    return;
                } else if (i == 3) {
                    this.mDefaultCall.ended_ANYSTATE(msg);
                    return;
                } else if (i == 4) {
                    this.mDefaultCall.error_ANYSTATE(msg);
                    return;
                } else if (i == 93) {
                    notifyOnUssdResponse(msg.arg1);
                    return;
                } else if (i == 94) {
                    this.mDefaultCall.ussdIndication_ANYSTATE(msg);
                    return;
                } else if (!(i == 306 || i == 307)) {
                    Log.e(LOG_TAG, "[ANY_STATE] msg:" + msg.what + " ignored !!!");
                    return;
                }
            }
            this.mThisSm.sendMessage(4, 0, -1, new SipError(Id.REQUEST_VSH_STOP_SESSION, "Tlte_911fail"));
        } else {
            forceNotifyCurrentCodec();
        }
    }

    /* access modifiers changed from: protected */
    public void onHalting() {
        StateMachine.LogRec lr;
        synchronized (this.mThisSm) {
            this.mThisSm.notifyAll();
        }
        Log.e(LOG_TAG, "Unexpected ACTION on STATE");
        int i = 0;
        while (i < this.mThisSm.getLogRecCount() && (lr = this.mThisSm.getLogRec(i)) != null) {
            Log.e(LOG_TAG, lr.toString());
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public void onQuitting() {
        StateMachine.LogRec lr;
        this.quit = true;
        synchronized (this.mThisSm) {
            this.mThisSm.notifyAll();
            this.mVolteSvcIntf.unregisterForCallStateEvent(this.mSession.mVolteStackEventHandler);
            this.mVolteSvcIntf.unregisterForUssdEvent(this.mSession.mUssdStackEventHandler);
            this.mVolteSvcIntf.unregisterForReferStatus(this.mSession.mVolteStackEventHandler);
            this.mVolteSvcIntf.unregisterForRrcConnectionEvent(this.mSession.mVolteStackEventHandler);
        }
        Log.e(LOG_TAG, "CallState Terminated");
        int i = 0;
        while (i < this.mThisSm.getLogRecCount() && (lr = this.mThisSm.getLogRec(i)) != null) {
            Log.e(LOG_TAG, lr.toString());
            i++;
        }
        try {
            if (this.mNetworkStatsOnPortThread != null) {
                if (!"robolectric".equals(Build.FINGERPRINT)) {
                    this.mNetworkStatsOnPortThread.quitSafely();
                    this.mNetworkStatsOnPortThread.join();
                }
                this.mNetworkStatsOnPortThread = null;
                this.mNetworkStatsOnPortHandler = null;
            }
        } catch (InterruptedException e) {
        }
    }

    public CallConstants.STATE getState() {
        if (this.quit) {
            return CallConstants.STATE.EndedCall;
        }
        IState state = getCurrentState();
        if (state == this.mReadyToCall) {
            return CallConstants.STATE.ReadyToCall;
        }
        if (state == this.mIncomingCall) {
            return CallConstants.STATE.IncomingCall;
        }
        if (state == this.mOutgoingCall) {
            return CallConstants.STATE.OutGoingCall;
        }
        if (state == this.mAlertingCall) {
            return CallConstants.STATE.AlertingCall;
        }
        if (state == this.mInCall) {
            return CallConstants.STATE.InCall;
        }
        if (state == this.mHoldingCall) {
            return CallConstants.STATE.HoldingCall;
        }
        if (state == this.mHeldCall) {
            return CallConstants.STATE.HeldCall;
        }
        if (state == this.mResumingCall) {
            return CallConstants.STATE.ResumingCall;
        }
        if (state == this.mModifyingCall) {
            return CallConstants.STATE.ModifyingCall;
        }
        if (state == this.mHoldingVideo) {
            return CallConstants.STATE.HoldingVideo;
        }
        if (state == this.mVideoHeld) {
            return CallConstants.STATE.VideoHeld;
        }
        if (state == this.mResumingVideo) {
            return CallConstants.STATE.ResumingVideo;
        }
        if (state == this.mEndingCall) {
            return CallConstants.STATE.EndingCall;
        }
        if (state == this.mModifyRequested) {
            return CallConstants.STATE.ModifyRequested;
        }
        return CallConstants.STATE.Idle;
    }

    public CallConstants.STATE getPreviousStateByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return CallConstants.STATE.ReadyToCall;
        }
        if (name.contains("ReadyToCall")) {
            return CallConstants.STATE.ReadyToCall;
        }
        if (name.contains("IncomingCall")) {
            return CallConstants.STATE.IncomingCall;
        }
        if (name.contains("OutGoingCall")) {
            return CallConstants.STATE.OutGoingCall;
        }
        if (name.contains("AlertingCall")) {
            return CallConstants.STATE.AlertingCall;
        }
        if (name.contains("InCall")) {
            return CallConstants.STATE.InCall;
        }
        if (name.contains("HoldingCall")) {
            return CallConstants.STATE.HoldingCall;
        }
        if (name.contains("HeldCall")) {
            return CallConstants.STATE.HeldCall;
        }
        if (name.contains("ResumingCall")) {
            return CallConstants.STATE.ResumingCall;
        }
        if (name.contains("ModifyingCall")) {
            return CallConstants.STATE.ModifyingCall;
        }
        if (name.contains("HoldingVideo")) {
            return CallConstants.STATE.HoldingVideo;
        }
        if (name.contains("VideoHeld")) {
            return CallConstants.STATE.VideoHeld;
        }
        if (name.contains("ResumingVideo")) {
            return CallConstants.STATE.ResumingVideo;
        }
        if (name.contains("EndingCall")) {
            return CallConstants.STATE.EndingCall;
        }
        if (name.contains("ModifyRequested")) {
            return CallConstants.STATE.ModifyRequested;
        }
        return CallConstants.STATE.Idle;
    }

    /* access modifiers changed from: protected */
    public boolean modifyCallType(CallProfile profile, boolean requested) {
        int ret;
        int currentCallType = this.mSession.getCallProfile().getCallType();
        int updateCallType = profile.getCallType();
        if (requested) {
            Log.i(LOG_TAG, "modifyCallType(" + requested + ") curCallType: " + currentCallType + ", updateCallType: " + updateCallType);
            if (!this.mModule.getSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.IncomingCall).isEmpty()) {
                notifyOnError(1109, "Call switch failed");
                return false;
            } else if (currentCallType == 9 && ImsCallUtil.isVideoCall(updateCallType)) {
                this.mMediaController.receiveSessionModifyResponse(this.mSession.getSessionId(), 1109, profile, this.mSession.getCallProfile());
                return false;
            } else if (currentCallType == 3 && updateCallType == 4) {
                notifyOnError(1109, "Call switch failed");
                return false;
            } else {
                if (updateCallType == 9 || ImsCallUtil.isRttCall(updateCallType)) {
                    this.isRequestTtyFull = true;
                }
                ret = this.mVolteSvcIntf.modifyCallType(this.mSession.getSessionId(), currentCallType, updateCallType);
                int cameraId = determineCamera(updateCallType, false);
                if (cameraId >= 0) {
                    this.mSession.startCamera(cameraId);
                } else {
                    ImsCallSession imsCallSession = this.mSession;
                    imsCallSession.mPrevUsedCamera = imsCallSession.mLastUsedCamera;
                }
            }
        } else {
            int requestCallType = this.mSession.mModifyRequestedProfile.getCallType();
            Log.i(LOG_TAG, "modifyCallType(" + requested + ") reqCallType: " + requestCallType + ", curCallType: " + currentCallType + ", updateCallType: " + updateCallType);
            ret = this.mVolteSvcIntf.replyModifyCallType(this.mSession.getSessionId(), currentCallType, updateCallType, requestCallType);
        }
        if (ret == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int rejectModifyCallType(int reason) {
        if (this.mMno == Mno.ATT || this.mMno == Mno.TMOUS || this.mMno.isChn() || this.mMno == Mno.CMHK || this.mMno == Mno.SAMSUNG || this.mMno == Mno.VODAFONE_CZ || this.mMno == Mno.TELSTRA || this.mMno == Mno.ETISALAT_EG) {
            int currentCallType = this.mSession.getCallProfile().getCallType();
            if (this.mSession.mModifyRequestedProfile == null) {
                Log.i(LOG_TAG, "ignoreModifyCallType(): mSession.mModifyRequestedProfile == null");
                return this.mVolteSvcIntf.rejectModifyCallType(this.mSession.getSessionId(), reason);
            }
            int requestCallType = this.mSession.mModifyRequestedProfile.getCallType();
            Log.i(LOG_TAG, "ignoreModifyCallType() reqCallType: " + requestCallType + ", curCallType: " + currentCallType);
            if (ImsCallUtil.isUpgradeCall(currentCallType, requestCallType)) {
                return this.mVolteSvcIntf.replyModifyCallType(this.mSession.getSessionId(), currentCallType, currentCallType, requestCallType);
            }
        }
        Log.i(LOG_TAG, "rejectModifyCallType() reason : " + reason);
        return this.mVolteSvcIntf.rejectModifyCallType(this.mSession.getSessionId(), reason);
    }

    /* access modifiers changed from: protected */
    public void transferCall(String msisdn) {
        ImsCallSession imsCallSession = this.mSession;
        ImsUri uri = imsCallSession.buildUri(msisdn, (String) null, imsCallSession.getCallProfile().getCallType());
        if (uri == null) {
            Log.e(LOG_TAG, "uri is null");
            notifyOnError(1119, "call transfer failed");
            if (this.mHoldBeforeTransfer) {
                this.mThisSm.sendMessage(71);
                return;
            }
            return;
        }
        this.mVolteSvcIntf.transferCall(this.mSession.getSessionId(), uri.toString());
        this.mTransferRequested = true;
        ContentValues tfItem = new ContentValues();
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration != null && imsRegistration.getImsProfile().isSoftphoneEnabled()) {
            Log.i(LOG_TAG, "transferCall for Softphone");
            tfItem.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_TOTAL_COUNT, 1);
            tfItem.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_SOFTPHONE_COUNT, 1);
        }
        tfItem.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(this.mSession.getPhoneId(), this.mContext, "DRPT", tfItem);
    }

    /* access modifiers changed from: protected */
    public boolean isChangedCallType(CallProfile profile) {
        if (this.mSession.getCallProfile().getCallType() == profile.getCallType()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void notifyOnEstablished() {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onEstablished(this.mSession.getCallProfile().getCallType());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnEarlyMediaStarted(int event) {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onEarlyMediaStarted(event);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnRingingBack() {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onRingingBack();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnCallForwarded() {
        if (this.mSession.getCallProfile().getDirection() == 1) {
            Log.i(LOG_TAG, "Do nothing");
        } else if (!(this.mMno == Mno.TMOUS || this.mMno == Mno.CU)) {
            this.mSession.getCallProfile().setHistoryInfo("");
        }
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onForwarded();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnEnded(int error) {
        int mPhoneId = this.mSession.getPhoneId();
        Log.i(LOG_TAG, "notifyOnEnded: " + error + ", errorCode:" + this.errorCode);
        CallProfile mCallProfile = this.mSession.getCallProfile();
        mCallProfile.setHasCSFBError(this.mModule.isCsfbErrorCode(mPhoneId, mCallProfile.getCallType(), new SipError(error)));
        if (this.mModule.getCmcServiceHelper().getSessionByCmcType(1) != null && (!(this.mModule.getCmcServiceHelper().getSessionByCmcType(3) == null && this.mModule.getCmcServiceHelper().getSessionByCmcType(7) == null && this.mModule.getCmcServiceHelper().getSessionByCmcType(5) == null) && this.mSession.getCmcType() == 0 && mCallProfile.hasCSFBError())) {
            int boundSessionId = mCallProfile.getCmcBoundSessionId();
            Log.i(LOG_TAG, "boundSessionId : " + boundSessionId);
            if (boundSessionId > 0) {
                this.mVolteSvcIntf.handleCmcCsfb(boundSessionId);
            }
        }
        if ((this.mMno == Mno.VZW || this.mMno == Mno.ATT || this.mMno == Mno.TMOUS) && ImsRegistry.getPdnController().isPendedEPDGWeakSignal(mPhoneId) && this.mSession.getCallProfile().getRadioTech() == 18) {
            error = 1703;
            ImsRegistry.getPdnController().setPendedEPDGWeakSignal(mPhoneId, false);
        } else if (this.mMno == Mno.TMOUS && ImsCallUtil.isRttEmergencyCall(this.mSession.getCallProfile().getCallType()) && this.errorCode == 2414) {
            error = 2414;
        }
        int length = this.mListeners.beginBroadcast();
        if (length == 0) {
            this.mNeedToLateEndedNotify = true;
        } else {
            this.mNeedToLateEndedNotify = false;
        }
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onEnded(error);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
        if (!this.mIsSentMobileCareEvt) {
            this.mModule.sendMobileCareEvent(mPhoneId, this.callType, error, (String) null);
            this.mIsSentMobileCareEvt = true;
        }
        this.lazerErrorCode = error;
    }

    /* access modifiers changed from: protected */
    public void notifyOnError(int error, String errorMsg) {
        notifyOnError(error, errorMsg, 10);
    }

    /* access modifiers changed from: protected */
    public void notifyOnError(int error, String errorMsg, int retryAfter) {
        int mPhoneId = this.mSession.getPhoneId();
        Log.i(LOG_TAG, "notifyOnError: " + error);
        handleSetCSFBError(mPhoneId, error, errorMsg, retryAfter);
        boolean isSwitchFail = false;
        if ((this.mMno == Mno.VZW || this.mMno == Mno.ATT || this.mMno == Mno.TMOUS) && ImsRegistry.getPdnController().isPendedEPDGWeakSignal(mPhoneId) && this.mSession.getCallProfile().getNetworkType() == 18) {
            error = 1703;
            ImsRegistry.getPdnController().setPendedEPDGWeakSignal(mPhoneId, false);
        } else if (this.mMno == Mno.TMOUS && ImsCallUtil.isRttEmergencyCall(this.mSession.getCallProfile().getCallType()) && (getState() == CallConstants.STATE.ReadyToCall || getState() == CallConstants.STATE.OutGoingCall)) {
            error = 2414;
            errorMsg = "RTT E911 Call Fail";
        }
        CallProfile profile = this.mSession.mModifyRequestedProfile == null ? this.mSession.getCallProfile() : this.mSession.mModifyRequestedProfile;
        if (getState() == CallConstants.STATE.HeldCall) {
            CallProfile callProfile = this.mHeldProfile;
            if (callProfile == null) {
                callProfile = this.mSession.getCallProfile();
            }
            profile = callProfile;
        }
        if (error == 1110 || error == 1109) {
            isSwitchFail = true;
        }
        if (isSwitchFail) {
            handleSwitchFail(profile, error);
        } else {
            int length = this.mListeners.beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mListeners.getBroadcastItem(i).onError(error, errorMsg, retryAfter);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }
        if (this.mIsSentMobileCareEvt == 0) {
            this.mModule.sendMobileCareEvent(this.mSession.getPhoneId(), this.callType, error, errorMsg);
            this.mIsSentMobileCareEvt = true;
        }
        this.lazerErrorCode = error;
        this.lazerErrorMessage = errorMsg;
    }

    private void handleSetCSFBError(int mPhoneId, int error, String errorMsg, int retryAfter) {
        if (!this.mIsWPSCall || (this.mMno == Mno.VZW && error == 403)) {
            this.mSession.getCallProfile().setHasCSFBError(this.mModule.isCsfbErrorCode(mPhoneId, this.mSession.getCallProfile().getCallType(), new SipError(error, errorMsg), retryAfter));
        } else {
            this.mSession.getCallProfile().setHasCSFBError(true);
        }
        if (this.mMno != Mno.USCC || error != 408) {
            return;
        }
        if (getState() == CallConstants.STATE.AlertingCall || getState() == CallConstants.STATE.EndingCall) {
            this.mSession.getCallProfile().setHasCSFBError(false);
            Log.i(LOG_TAG, "USCC - Do not perform CSFB when 408 is received after User is alerted");
        }
    }

    private void handleSwitchFail(CallProfile profile, int error) {
        CallProfile callProfile;
        if (ImsCallUtil.isVideoCall(profile.getCallType()) || ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) || ((callProfile = this.mModifyingProfile) != null && ImsCallUtil.isVideoCall(callProfile.getCallType()))) {
            this.mMediaController.receiveSessionModifyResponse(this.mSession.getSessionId(), error, profile, this.mSession.getCallProfile());
        } else if (ImsCallUtil.isRttCall(profile.getCallType()) && !ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType())) {
            this.mModule.onSendRttSessionModifyResponse(this.mSession.getCallId(), true, false);
        } else if (!ImsCallUtil.isRttCall(profile.getCallType()) && ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType())) {
            this.mModule.onSendRttSessionModifyResponse(this.mSession.getCallId(), false, false);
        } else if ((this.mMno.isEur() || this.mMno == Mno.SAMSUNG) && !(this.mHoldingProfile == null && this.mHeldProfile == null)) {
            Log.i(LOG_TAG, "Notify switch call fail during Holding call");
            this.mMediaController.receiveSessionModifyResponse(this.mSession.getSessionId(), error, profile, this.mSession.getCallProfile());
        } else if (this.mMno == Mno.RJIL && profile.getCallType() == this.mSession.getCallProfile().getCallType()) {
            Log.i(LOG_TAG, "Race condition - Call type is same as Requested call type");
            this.mMediaController.receiveSessionModifyResponse(this.mSession.getSessionId(), error, profile, this.mSession.getCallProfile());
        }
    }

    /* access modifiers changed from: protected */
    public void notifyOnHeld(boolean userAction) {
        int length = this.mListeners.beginBroadcast();
        Log.i(LOG_TAG, "notifyOnHeld local=" + userAction + "; localholdtone" + this.mSession.mLocalHoldTone);
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onHeld(userAction, this.mSession.mLocalHoldTone);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnResumed(boolean userAction) {
        int length = this.mListeners.beginBroadcast();
        Log.i(LOG_TAG, "notifyOnResumed: local=" + userAction);
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onResumed(userAction);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyConfParticipantOnHeld(int sessionId, boolean userAction) {
        int length = this.mListeners.beginBroadcast();
        Log.i(LOG_TAG, "notifyConfParticipantOnHeld: sessionId=" + sessionId);
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onConfParticipantHeld(sessionId, userAction);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyConfParticipanOnResumed(int sessionId, boolean userAction) {
        int length = this.mListeners.beginBroadcast();
        Log.i(LOG_TAG, "notifyConfParticipanOnResumed: sessionId=" + sessionId);
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onConfParticipantResumed(sessionId, userAction);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnModified(int type) {
        int length = this.mListeners.beginBroadcast();
        Log.i(LOG_TAG, "notifyOnModified callType=" + type);
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onSwitched(type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyOnUssdResponse(int statusCode) {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onUssdResponse(statusCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void forceNotifyCurrentCodec() {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            IImsCallSessionEventListener listener = this.mListeners.getBroadcastItem(i);
            NetworkEvent ne = this.mModule.getNetwork();
            if (this.mMno == Mno.TMOUS && !NetworkUtil.isMobileDataOn(this.mContext) && ne.network != 18) {
                this.mSession.getCallProfile().setRemoteVideoCapa(false);
            }
            try {
                listener.onProfileUpdated(this.mSession.getCallProfile().getMediaProfile(), this.mSession.mCallProfile.getMediaProfile());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void handleRemoteHeld(boolean isRemoteHeld) {
        boolean isLocalHoldToneChanged = this.mSession.mOldLocalHoldTone != this.mSession.mLocalHoldTone;
        if (this.mRemoteHeld != isRemoteHeld || (isRemoteHeld && isLocalHoldToneChanged)) {
            this.mRemoteHeld = isRemoteHeld;
            if (isRemoteHeld) {
                notifyOnHeld(false);
            } else {
                notifyOnResumed(false);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("handleRemoteHeld: remote already ");
            sb.append(isRemoteHeld ? "held" : "resumed");
            Log.i(LOG_TAG, sb.toString());
        }
    }

    public void setPreviousState(State state) {
        this.mPrevState = state;
    }

    /* access modifiers changed from: protected */
    public State getPreviousState() {
        return this.mPrevState;
    }

    /* access modifiers changed from: protected */
    public void setRetryInprogress(boolean flag) {
        this.mRetryInprogress = flag;
    }

    /* access modifiers changed from: protected */
    public void startRingTimer(long millis) {
        Log.i(LOG_TAG, "startRingTimer: millis " + millis);
        stopRingTimer();
        if (millis > 0) {
            this.mRingTimeoutMessage = this.mThisSm.obtainMessage(204);
            this.mSession.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mRingTimeoutMessage, millis);
        }
    }

    /* access modifiers changed from: protected */
    public void stopRingTimer() {
        if (this.mRingTimeoutMessage != null) {
            Log.i(LOG_TAG, "stopRingTimer");
            this.mSession.mAm.removeMessage(this.mRingTimeoutMessage);
            this.mRingTimeoutMessage = null;
        }
    }

    /* access modifiers changed from: protected */
    public String calculateCmcCallTime(ImsCallSession boundedSession, String CallId) {
        long callEstablishTime;
        long cmcCallTime = 0;
        if (boundedSession != null) {
            callEstablishTime = boundedSession.getCmcCallEstablishTime();
            Log.i(LOG_TAG, "PS callEstablishTime : " + callEstablishTime);
        } else {
            callEstablishTime = this.mModule.getCmcServiceHelper().getCmcCallEstablishTime(CallId);
            Log.i(LOG_TAG, "CS callEstablishTime : " + callEstablishTime);
        }
        if (callEstablishTime > 0) {
            long currentTime = System.currentTimeMillis();
            cmcCallTime = currentTime - callEstablishTime;
            Log.i(LOG_TAG, "callEstablishTime : " + callEstablishTime + ", currentTime : " + currentTime + ", cmcCallTime : " + cmcCallTime);
        }
        return Long.toString(cmcCallTime);
    }

    /* access modifiers changed from: protected */
    public SipReason getSipReasonFromUserReason(int reason) {
        int i = reason;
        Log.i(LOG_TAG, "getSipReasonFromUserReason: reason " + i);
        if (i == 8) {
            return new SipReason("SIP", 0, "SRVCC", new String[0]);
        }
        if (i == 25) {
            return new SipReason("SIP", 0, "INVITE FLUSH", new String[0]);
        }
        if (i == 13) {
            return new SipReason("", 0, "PS BARRING", true, new String[0]);
        }
        if (i == 20) {
            return new SipReason("", 6007, "MDMN_PULL_BY_PRIMARY", new String[0]);
        }
        if (i == 11) {
            if (this.mMno == Mno.TELSTRA) {
                return new SipReason("SIP", 0, "DEDICATED BEARER LOST", true, new String[0]);
            }
            if (this.mMno != Mno.TMOUS) {
                if (this.mMno == Mno.ATT) {
                    return new SipReason("SIP", 200, "DEDICATED BEARER LOST", new String[0]);
                }
                return new SipReason("SIP", 0, "DEDICATED BEARER LOST", new String[0]);
            }
        } else if (i == 14) {
            if (this.mMno == Mno.RJIL) {
                return new SipReason("SIP", 0, "DEREGISTERED", true, new String[0]);
            }
            return new SipReason("", 0, "", true, new String[0]);
        } else if (i == 23) {
            if (this.mMno == Mno.VZW || this.mMno == Mno.DOCOMO) {
                return new SipReason("", 0, "RRC CONNECTION REJECT", true, new String[0]);
            }
        } else if (i == 17) {
            if (this.mMno == Mno.ORANGE || this.mMno == Mno.FET) {
                return new SipReason("", 0, "SESSIONPROGRESS TIMEOUT", true, new String[0]);
            }
        } else if (this.mMno == Mno.GENERIC_IR92 && i == 5) {
            return new SipReason("SIP", 200, "User Triggered", false, new String[0]);
        }
        return getSipReasonMno().getFromUserReason(i);
    }

    /* access modifiers changed from: protected */
    public SipReason getSipReasonMno() {
        if (this.mMno.isKor()) {
            return new SipReasonKor();
        }
        if (this.mMno == Mno.VZW) {
            return new SipReasonVzw();
        }
        if (this.mMno == Mno.BELL) {
            return new SipReasonBmc();
        }
        if (this.mMno == Mno.USCC) {
            return new SipReasonUscc();
        }
        if (this.mMno == Mno.RJIL || this.mMno == Mno.TELEFONICA_UK || this.mMno == Mno.EE) {
            return new SipReasonRjil();
        }
        if (this.mMno == Mno.OPTUS) {
            return new SipReasonOptus();
        }
        if (this.mMno == Mno.TMOUS) {
            return new SipReasonTmoUs();
        }
        return new SipReason();
    }

    /* access modifiers changed from: protected */
    public void setVideoRtpPort(int localRtp, int localRtcp, int remoteRtp, int remoteRtcp) {
        this.mLocalVideoRtpPort = localRtp;
        this.mLocalVideoRtcpPort = localRtcp;
        this.mRemoteVideoRtpPort = remoteRtp;
        this.mRemoteVideoRtcpPort = remoteRtcp;
    }

    /* access modifiers changed from: protected */
    public void startNetworkStatsOnPorts() {
        Log.i(LOG_TAG, "startNetworkStatsOnPorts");
        NetworkStatsOnPortHandler networkStatsOnPortHandler = this.mNetworkStatsOnPortHandler;
        if (networkStatsOnPortHandler != null) {
            networkStatsOnPortHandler.setVideoPort(this.mLocalVideoRtpPort, this.mRemoteVideoRtpPort, this.mLocalVideoRtcpPort, this.mRemoteVideoRtcpPort);
            if (this.mRegistration != null) {
                this.mNetworkStatsOnPortHandler.setInterface(ImsRegistry.getPdnController().getIntfNameByNetType(this.mRegistration.getNetwork()));
            }
            NetworkStatsOnPortHandler networkStatsOnPortHandler2 = this.mNetworkStatsOnPortHandler;
            networkStatsOnPortHandler2.sendMessage(networkStatsOnPortHandler2.obtainMessage(1));
        }
    }

    /* access modifiers changed from: protected */
    public void stopNetworkStatsOnPorts() {
        Log.i(LOG_TAG, "stopNetworkStatsOnPorts");
        if (this.mNetworkStatsOnPortHandler != null) {
            requestCallDataUsage();
            NetworkStatsOnPortHandler networkStatsOnPortHandler = this.mNetworkStatsOnPortHandler;
            networkStatsOnPortHandler.sendMessage(networkStatsOnPortHandler.obtainMessage(2));
        }
    }

    /* access modifiers changed from: protected */
    public void requestCallDataUsage() {
        Log.i(LOG_TAG, "requestCallDataUsage");
        this.mMediaController.onChangeCallDataUsage(this.mSession.getSessionId(), getNetworkStatsVideoCall());
    }

    /* access modifiers changed from: protected */
    public long getNetworkStatsVideoCall() {
        Log.i(LOG_TAG, "getNetworkStatsVideoCall");
        NetworkStatsOnPortHandler networkStatsOnPortHandler = this.mNetworkStatsOnPortHandler;
        if (networkStatsOnPortHandler != null) {
            return networkStatsOnPortHandler.getNetworkStatsVideoCall();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void startRetriggerTimer(long millis) {
        Log.i(LOG_TAG, "startRetriggerTimer: millis " + millis);
        stopRetriggerTimer();
        this.mRetriggerTimeoutMessage = this.mThisSm.obtainMessage(202);
        this.mSession.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mRetriggerTimeoutMessage, millis);
    }

    /* access modifiers changed from: protected */
    public void stopRetriggerTimer() {
        if (this.mRetriggerTimeoutMessage != null) {
            Log.i(LOG_TAG, "stopRetriggerTimer");
            this.mSession.mAm.removeMessage(this.mRetriggerTimeoutMessage);
            this.mRetriggerTimeoutMessage = null;
        }
    }

    public void onReceiveSIPMSG(String sipMessage, boolean isRequest) {
        if (!TextUtils.isEmpty(sipMessage)) {
            String[] lines = sipMessage.split("\r\n");
            if (lines.length <= 1 || lines[0].length() <= 11) {
                Log.d(LOG_TAG, "onReceiveSIPMSG : No front Char");
                return;
            }
            String frontChar = lines[0].startsWith("SIP") ? lines[0].substring(8, 10) : lines[0].substring(0, 2);
            if (isRequest) {
                this.mSIPFlowInfo += "s" + frontChar;
                return;
            }
            this.mSIPFlowInfo += "r" + frontChar;
        }
    }

    public void setPendingCall(boolean isPending) {
        this.mIsPendingCall = isPending;
    }

    public void setStartCameraState(boolean isStartCameraSuccess) {
        this.mIsStartCameraSuccess = isStartCameraSuccess;
    }

    public boolean needToLogForATTGate(int callType2) {
        if (this.mMno != Mno.ATT || !ImsGateConfig.isGateEnabled() || !ImsCallUtil.isVideoCall(callType2)) {
            return false;
        }
        return true;
    }

    public void sendCmcPublishDialog() {
        if (this.mSession.getCmcType() == 0 && this.mModule.getCmcServiceHelper().isCmcRegExist(this.mSession.getPhoneId())) {
            int cmcType = 3;
            if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
                cmcType = 5;
            }
            for (int type = 1; type <= cmcType; type += 2) {
                if (this.mModule.getCmcServiceHelper().getSessionByCmcType(type) == null) {
                    this.mModule.getCmcServiceHelper().sendPublishDialogInternal(this.mSession.getPhoneId(), type);
                }
            }
        }
    }

    public boolean handleSPRoutgoingError(Message msg) {
        if (this.mMno != Mno.SPRINT || this.mRegistration == null) {
            return true;
        }
        SipError err = (SipError) msg.obj;
        int code = err.getCode();
        if (code == 486 || code == 487 || code == 408) {
            return false;
        }
        IRegistrationGovernor governor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle());
        if (governor == null) {
            return true;
        }
        String service = "mmtel";
        if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            service = "mmtel-video";
        }
        if (code >= 400 && code <= 699) {
            Log.i(LOG_TAG, "4xx,5xx,6xx error. trigger cs fallback");
            governor.onSipError(service, new SipError(code));
            err.setCode(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS);
        }
        if (code != 709) {
            return true;
        }
        Log.i(LOG_TAG, "709 error. trigger cs fallback");
        governor.onSipError(service, new SipError(code));
        err.setCode(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS);
        return true;
    }

    /* access modifiers changed from: protected */
    public void onCallModified(CallProfile modifiedProfile) {
        int videoQuality;
        CallProfile callProfile;
        int modifiedCallType = modifiedProfile.getCallType();
        if (this.mSession.mModifyRequestedProfile == null) {
            videoQuality = this.mSession.getCallProfile().getMediaProfile().getVideoQuality();
        } else {
            videoQuality = this.mSession.mModifyRequestedProfile.getMediaProfile().getVideoQuality();
        }
        modifiedProfile.getMediaProfile().setVideoQuality(videoQuality);
        notifyOnModified(modifiedCallType);
        if (ImsCallUtil.isVideoCall(modifiedCallType)) {
            startNetworkStatsOnPorts();
        } else {
            stopNetworkStatsOnPorts();
        }
        if (!ImsCallUtil.isTtyCall(modifiedCallType) && !ImsCallUtil.isRttCall(modifiedCallType)) {
            IImsMediaController iImsMediaController = this.mMediaController;
            int sessionId = this.mSession.getSessionId();
            if (this.mSession.mModifyRequestedProfile == null) {
                callProfile = this.mSession.getCallProfile();
            } else {
                callProfile = this.mSession.mModifyRequestedProfile;
            }
            iImsMediaController.receiveSessionModifyResponse(sessionId, 200, callProfile, modifiedProfile);
        }
    }

    public void setPreAlerting() {
        this.mPreAlerting = true;
    }

    public boolean getPreAlerting() {
        return this.mPreAlerting;
    }
}
