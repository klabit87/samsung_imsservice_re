package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.SemSystemProperties;
import android.telecom.TelecomManager;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorCTC;
import com.sec.internal.constants.ims.SipErrorCmcc;
import com.sec.internal.constants.ims.SipErrorDcm;
import com.sec.internal.constants.ims.SipErrorGlobe;
import com.sec.internal.constants.ims.SipErrorKdi;
import com.sec.internal.constants.ims.SipErrorKor;
import com.sec.internal.constants.ims.SipErrorMdmn;
import com.sec.internal.constants.ims.SipErrorNovaIs;
import com.sec.internal.constants.ims.SipErrorSbm;
import com.sec.internal.constants.ims.SipErrorSprint;
import com.sec.internal.constants.ims.SipErrorUscc;
import com.sec.internal.constants.ims.SipErrorVzw;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.helper.BlockedNumberUtil;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class ImsIncomingCall extends CallState {
    private Message mDummyDnsTimeoutMessage = null;
    private Message mReinviteTimeoutMessage = null;

    ImsIncomingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        Log.i("CallStateMachine", "Enter [IncomingCall]");
        if (this.mCsm.needToLogForATTGate(this.mSession.getCallProfile().getCallType())) {
            IMSLog.g("GATE", "<GATE-M>INCOMING_VIDEO_CALL</GATE-M>");
        }
        IMSLog.LAZER_TYPE lazer_type = IMSLog.LAZER_TYPE.CALL;
        IMSLog.lazer(lazer_type, this.mSession.getCallId() + " - START INCOMING");
        if (this.mRegistration != null) {
            this.mCsm.startRingTimer(((long) this.mRegistration.getImsProfile().getRingingTimer()) * 1000);
        }
        if (this.mSession.mKaSender != null) {
            this.mSession.mKaSender.start();
        }
        int cameraId = this.mCsm.determineCamera(this.mSession.getCallProfile().getCallType(), false);
        if (cameraId >= 0 && !this.mSession.getCameraStartByApp()) {
            if (this.mModule.getCallCount()[1] > 1) {
                this.mSession.startCamera(cameraId);
            } else if (this.mMno != Mno.SKT || !isTPhoneMode()) {
                Log.e("CallStateMachine", "camera in use by other app");
                this.mCsm.sendMessageDelayed(24, 0, -1, 100);
            } else {
                Log.e("CallStateMachine", "delay camera start due to check isTPhoneRelaxMode");
                this.mCsm.sendMessageDelayed(24, 0, -1, 1500);
            }
        }
        if (this.mSession.getCallProfile().getHistoryInfo() != null) {
            this.mCsm.notifyOnCallForwarded();
        }
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[IncomingCall] processMessage " + msg.what);
        switch (msg.what) {
            case 1:
                int isTerminated = terminate_IncomingCall(msg);
                if (isTerminated != -1) {
                    if (isTerminated == 1) {
                        return true;
                    }
                    return false;
                }
                break;
            case 3:
            case 4:
            case 94:
            case 100:
            case 400:
                return false;
            case 22:
                accept_IncomingCall(msg);
                break;
            case 23:
                break;
            case 24:
                delayedCamStart_IncomingCall(msg);
                break;
            case 32:
                earlymedia_IncomingCall(msg);
                break;
            case 41:
                established_IncomingCall(msg);
                break;
            case 51:
                hold_IncomingCall(msg);
                break;
            case 52:
                update_IncomingCall(msg);
                break;
            case 64:
                sendText_IncomingCall(msg);
                break;
            case 80:
                Log.i("CallStateMachine", "[IncomingCall] Hold video defered");
                this.mCsm.deferMessage(msg);
                break;
            case 81:
                Log.i("CallStateMachine", "[IncomingCall] Resume video defered");
                this.mCsm.isDeferedVideoResume = true;
                this.mCsm.deferMessage(msg);
                break;
            case 204:
                ringTimeout_IncomingCall(msg);
                break;
            case CallStateMachine.ON_REINVITE_TIMER_EXPIRED:
                Log.i("CallStateMachine", "[IncomingCall] Re-INVITE Timer expired defered");
                this.mCsm.deferMessage(msg);
                break;
            case CallStateMachine.ON_DUMMY_DNS_TIMER_EXPIRED:
                Log.i("CallStateMachine", "[IncomingCall] Sending Dummy Dns");
                this.mRegistrationManager.sendDummyDnsQuery();
                startDummyDnsTimer();
                break;
            case 502:
                this.mCsm.mReinvite = true;
                Log.i("CallStateMachine", "[IncomingCall] Re-INVITE defered");
                this.mCsm.deferMessage(msg);
                break;
            case 5000:
                dbrLost_IncomingCall(msg);
                break;
            default:
                Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                break;
        }
        reject_IncomingCall(msg);
        return true;
    }

    /* access modifiers changed from: protected */
    public void startDummyDnsTimer() {
        stopDummyDnsTimer();
        Log.i("CallStateMachine", "startDummyDnsTimer");
        this.mDummyDnsTimeoutMessage = this.mCsm.obtainMessage(CallStateMachine.ON_DUMMY_DNS_TIMER_EXPIRED);
        this.mSession.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mDummyDnsTimeoutMessage, 8000);
    }

    /* access modifiers changed from: protected */
    public void stopDummyDnsTimer() {
        if (this.mDummyDnsTimeoutMessage != null) {
            Log.i("CallStateMachine", "stopDummyDnsTimer");
            this.mSession.mAm.removeMessage(this.mDummyDnsTimeoutMessage);
            this.mDummyDnsTimeoutMessage = null;
        }
    }

    public void exit() {
        this.mCsm.removeMessages(24);
        this.mCsm.setPreviousState(this);
        this.mCsm.stopRingTimer();
    }

    private boolean isTPhoneMode() {
        if ("com.skt.prod.dialer".equals(((TelecomManager) this.mContext.getSystemService("telecom")).getDefaultDialerPackage())) {
            return true;
        }
        return false;
    }

    private void delayedCamStart_IncomingCall(Message msg) {
        Message message = msg;
        int spCameraRun = SemSystemProperties.getInt("service.camera.running", 0);
        int spCameraRecRun = SemSystemProperties.getInt("service.camera.rec.running", 0);
        if (message.arg1 < 50 && (spCameraRun == 1 || spCameraRecRun == 1)) {
            Log.e("CallStateMachine", "trying " + message.arg1 + " delayType = " + message.arg2);
            this.mCsm.mCameraUsedAtOtherApp = true;
            if (spCameraRun == 0 && spCameraRecRun == 1) {
                this.mCsm.sendMessageDelayed(24, message.arg1 + 1, 2, 100);
            } else {
                this.mCsm.sendMessageDelayed(24, message.arg1 + 1, -1, 100);
            }
        } else if (message.arg2 == 2) {
            this.mCsm.sendMessageDelayed(24, message.arg1 + 12, -1, 1200);
        } else if ((this.mMno != Mno.SKT || !this.mSession.isTPhoneRelaxMode()) && !BlockedNumberUtil.isBlockedNumber(this.mContext, this.mSession.getCallProfile().getDialingNumber())) {
            this.mSession.startCamera(this.mCsm.determineCamera(this.mSession.getCallProfile().getCallType(), false));
        }
    }

    private void accept_IncomingCall(Message msg) {
        if (this.mRegistration != null && !this.mSession.isCmcPrimaryType(this.mSession.getCmcType())) {
            Log.i("CallStateMachine", "bindToNetwork for MT");
            this.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
        }
        if (this.mSession.mModifyRequestedProfile != null) {
            Log.i("CallStateMachine", "[IncomingCall] start reinvite timer");
            startReinviteTimer(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        }
        if (this.mSession.mKaSender != null) {
            this.mSession.mKaSender.stop();
        }
        this.mCsm.callType = ((CallProfile) msg.obj).getCallType();
        handleCallTypeAtIncomingCall();
        Log.i("CallStateMachine", "answerCall with callType: " + this.mCsm.callType);
        this.mCsm.removeMessages(24);
        int cameraId = this.mCsm.determineCamera(this.mCsm.callType, false);
        if (cameraId >= 0) {
            this.mSession.startCamera(cameraId);
        } else {
            this.mSession.stopCamera();
            this.mSession.mLastUsedCamera = -1;
        }
        this.mSession.setIsEstablished(true);
        if (this.mSession.isCmcPrimaryType(this.mSession.getCmcType())) {
            Log.i("CallStateMachine", "mSession.getCallProfile().getReplaceSipCallId(): " + this.mSession.getCallProfile().getReplaceSipCallId());
            ImsCallSession boundSession = this.mModule.getSessionBySipCallId(this.mSession.getCallProfile().getReplaceSipCallId());
            String cmcCallTime = "";
            ImsCallSession boundSessionForCurrentSession = null;
            int boundSessionIdForCurrentSession = this.mSession.getCallProfile().getCmcBoundSessionId();
            if (boundSessionIdForCurrentSession > 0) {
                boundSessionForCurrentSession = this.mModule.getSession(boundSessionIdForCurrentSession);
            }
            if (boundSession != null) {
                this.mSession.getCallProfile().setCmcBoundSessionId(boundSession.getSessionId());
                boundSession.getCallProfile().setCmcBoundSessionId(this.mSession.mSessionId);
                Log.i("CallStateMachine", "PS PD to SD pull");
                cmcCallTime = this.mCsm.calculateCmcCallTime(boundSession, this.mSession.getCallProfile().getReplaceSipCallId());
            } else if (boundSessionForCurrentSession != null) {
                Log.i("CallStateMachine", "do nothing when SD call answer for PS");
            } else {
                Log.i("CallStateMachine", "bounded session is not found");
                if (this.mSession.getCallProfile().getReplaceSipCallId() == null) {
                    long CallEstablishTimeExtra = System.currentTimeMillis();
                    Log.i("CallStateMachine", "save SD call answer time for CS : " + CallEstablishTimeExtra);
                    this.mModule.getCmcServiceHelper().setCallEstablishTimeExtra(CallEstablishTimeExtra);
                } else {
                    Log.i("CallStateMachine", "CS PD to SD pull");
                    cmcCallTime = this.mCsm.calculateCmcCallTime((ImsCallSession) null, this.mSession.getCallProfile().getReplaceSipCallId());
                }
            }
            if (this.mVolteSvcIntf.answerCallWithCallType(this.mSession.getSessionId(), this.mCsm.callType, cmcCallTime) < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "call session already released"));
                return;
            }
        } else if (this.mVolteSvcIntf.answerCallWithCallType(this.mSession.getSessionId(), this.mCsm.callType) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "call session already released"));
            return;
        }
        handleFastAccept();
        this.mCsm.mUserAnswered = true;
    }

    private void startReinviteTimer(long millis) {
        Log.i("CallStateMachine", "startReinviteTimer: " + millis);
        stopReinviteTimer();
        this.mReinviteTimeoutMessage = this.mCsm.obtainMessage(CallStateMachine.ON_REINVITE_TIMER_EXPIRED);
        this.mSession.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mReinviteTimeoutMessage, millis);
    }

    private void stopReinviteTimer() {
        if (this.mReinviteTimeoutMessage != null) {
            Log.i("CallStateMachine", "stopReinviteTimer");
            this.mSession.mAm.removeMessage(this.mReinviteTimeoutMessage);
            this.mReinviteTimeoutMessage = null;
        }
    }

    private void handleCallTypeAtIncomingCall() {
        if (ImsCallUtil.isVideoCall(this.mCsm.callType) && !this.mModule.isCallServiceAvailable(this.mSession.getPhoneId(), "mmtel-video") && this.mCsm.callType != 8) {
            Log.i("CallStateMachine", "Call Type change Video to Voice for no video feature tag");
            this.mCsm.callType = 1;
        }
        if (ImsCallUtil.isTtyCall(this.mCsm.callType) && this.mRegistration != null && (this.mRegistration.getImsProfile().getTtyType() == 1 || this.mRegistration.getImsProfile().getTtyType() == 3)) {
            Log.i("CallStateMachine", "CS TTY Enable so do not answer IMS TTY call");
            this.mCsm.callType = 1;
        }
        if (ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType())) {
            if (this.mCsm.callType == 1) {
                this.mCsm.callType = 14;
            } else if (this.mCsm.callType == 2 && this.mMno != Mno.TMOUS) {
                this.mCsm.callType = 15;
            }
            this.mSession.getCallProfile().getMediaProfile().setRttMode(1);
            return;
        }
        this.mSession.getCallProfile().getMediaProfile().setRttMode(0);
    }

    private void handleFastAccept() {
        if (this.mMno.isKor() || this.mMno == Mno.RJIL) {
            if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) && !ImsCallUtil.isVideoCall(this.mCsm.callType)) {
                this.mSession.getCallProfile().setDowngradedVideoCall(true);
                this.mSession.getCallProfile().setDowngradedAtEstablish(true);
                this.mSession.setUserCameraOff(false);
            }
            if (this.mSession.getCallProfile().getCallType() == 9) {
                this.mCsm.callType = 9;
            }
            if (this.mMno.isChn() && !ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                this.mCsm.callType = 1;
            }
            this.mSession.getCallProfile().setCallType(this.mCsm.callType);
            int[] callsCount = this.mModule.getCallCount(this.mSession.getPhoneId());
            int pdCallCount = 0;
            int cmcType = 5;
            if (ImsRegistry.getP2pCC().isEnabledWifiDirectFeature()) {
                cmcType = 7;
            }
            for (int type = 1; type <= cmcType; type += 2) {
                pdCallCount += this.mModule.getCmcServiceHelper().getSessionCountByCmcType(this.mSession.getPhoneId(), type);
            }
            Log.i("CallStateMachine", "Notify fake ESTABLISH event. callsCount: " + callsCount[0] + " pdCallCount: " + pdCallCount);
            if ((this.mMno.isKor() || this.mMno.isChn() || this.mMno.isJpn()) && callsCount[0] - pdCallCount > 1) {
                Log.i("CallStateMachine", "force to set modifiable to false for fake ESTABLISH");
                this.mSession.getCallProfile().setRemoteVideoCapa(false);
            }
            this.mCsm.notifyOnEstablished();
        }
    }

    private int terminate_IncomingCall(Message msg) {
        if (!this.mCsm.mUserAnswered || msg.arg1 == 8) {
            return 0;
        }
        if (!this.mCsm.mUserAnswered || this.mMno == Mno.CHT || this.mMno == Mno.RJIL) {
            return -1;
        }
        this.mCsm.deferMessage(msg);
        return 1;
    }

    private void reject_IncomingCall(Message msg) {
        if (this.mSession.getIsEstablished()) {
            Log.i("CallStateMachine", "ignore reject msg after call accepted");
            return;
        }
        this.mCsm.callType = this.mSession.getCallProfile().getCallType();
        this.mCsm.sipError = getSipErrorFromUserReason(msg.arg1);
        if (ImsCallUtil.isCameraUsingCall(this.mCsm.callType)) {
            this.mCsm.removeMessages(24);
            this.mSession.stopCamera();
        }
        if (this.mVolteSvcIntf.rejectCall(this.mSession.getSessionId(), this.mCsm.callType, this.mCsm.sipError) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
            return;
        }
        if (this.mSession.isCmcPrimaryType(this.mSession.getCmcType()) && this.mSession.getCallProfile().getReplaceSipCallId() != null && msg.arg1 == 3) {
            this.mModule.getCmcServiceHelper().sendDummyPublishDialog(this.mSession.getPhoneId(), this.mSession.getCmcType());
        }
        if (!this.mCsm.mCameraUsedAtOtherApp) {
            this.mCsm.notifyOnEnded(ImsCallUtil.convertCallEndReasonToFramework(2, msg.arg1));
        }
        this.mCsm.transitionTo(this.mCsm.mEndingCall);
    }

    private void dbrLost_IncomingCall(Message msg) {
        this.mCsm.callType = this.mSession.getCallProfile().getCallType();
        if (msg.arg1 == 1) {
            this.mCsm.sipError = SipErrorBase.PRECONDITION_FAILURE;
            if (this.mVolteSvcIntf.rejectCall(this.mSession.getSessionId(), this.mCsm.callType, this.mCsm.sipError) < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(Id.REQUEST_SIP_DIALOG_OPEN, ""));
                return;
            }
            this.mCsm.notifyOnEnded(Id.REQUEST_SIP_DIALOG_OPEN);
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
        }
    }

    private void ringTimeout_IncomingCall(Message msg) {
        if (this.mMno == Mno.ATT) {
            this.mCsm.sipError = getSipErrorFromUserReason(9);
        } else {
            this.mCsm.sipError = getSipErrorFromUserReason(13);
        }
        if (this.mVolteSvcIntf.rejectCall(this.mSession.getSessionId(), this.mCsm.callType, this.mCsm.sipError) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
            return;
        }
        this.mSession.setEndType(2);
        this.mSession.setEndReason(13);
        this.mCsm.notifyOnEnded(ImsCallUtil.convertCallEndReasonToFramework(2, 13));
        this.mCsm.transitionTo(this.mCsm.mEndingCall);
    }

    private void established_IncomingCall(Message msg) {
        IRegistrationGovernor regGov;
        int i;
        this.mCsm.transitionTo(this.mCsm.mInCall);
        if (this.mRegistration != null && (regGov = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle())) != null) {
            IRegistrationGovernor.CallEvent callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED;
            if (this.mSession.getCallProfile().isDowngradedVideoCall()) {
                i = 2;
            } else {
                i = this.mSession.getCallProfile().getCallType();
            }
            regGov.onCallStatus(callEvent, (SipError) null, i);
        }
    }

    private void update_IncomingCall(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        CallProfile profile = bundle.getParcelable("profile");
        int srvccVersion = this.mModule.getSrvccVersion(this.mSession.getPhoneId());
        if (profile != null || srvccVersion == 0 || (srvccVersion < 10 && !DeviceUtil.getGcfMode().booleanValue())) {
            Log.i("CallStateMachine", "Postpone update request till established state");
            this.mSession.mModifyRequestedProfile = profile;
            if (this.mSession.mModifyRequestedProfile == null || !ImsCallUtil.isTtyCall(this.mSession.mModifyRequestedProfile.getCallType())) {
                Log.i("CallStateMachine", "deferMessage only for non TTY UPDATE");
                this.mCsm.deferMessage(msg);
                return;
            }
            return;
        }
        Log.i("CallStateMachine", "MT aSRVCC supported");
        this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", bundle.getInt("cause"), bundle.getString("reasonText"), new String[0]));
    }

    private void hold_IncomingCall(Message msg) {
        Log.i("CallStateMachine", "received hold request maybe because of FAST_ACCEPT");
        if (this.mMno == Mno.RJIL || this.mMno == Mno.SAMSUNG) {
            this.mCsm.deferMessage(msg);
        }
    }

    private void sendText_IncomingCall(Message msg) {
        if (this.mCsm.mUserAnswered) {
            Bundle bundle = (Bundle) msg.obj;
            String text = bundle.getString("text");
            int len = bundle.getInt("len");
            Log.i("CallStateMachine", "text=" + text + ", len=" + len);
            this.mVolteSvcIntf.sendText(this.mSession.getSessionId(), text, len);
        }
    }

    private void earlymedia_IncomingCall(Message msg) {
        Log.i("CallStateMachine", "mSession.getCallProfile().isVideoCRBT: " + this.mSession.getCallProfile().isVideoCRBT());
        if (this.mRegistration != null && this.mSession.getCallProfile().isVideoCRBT()) {
            if (!this.mSession.isCmcPrimaryType(this.mSession.getCmcType())) {
                Log.i("CallStateMachine", "bindToNetwork for MT");
                this.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
            }
            this.mVolteSvcIntf.startVideoEarlyMedia(this.mSession.getSessionId());
        }
        this.mCsm.notifyOnEarlyMediaStarted(msg.arg1);
    }

    /* access modifiers changed from: protected */
    public SipError getSipErrorFromUserReason(int reason) {
        SipError response;
        Log.i("CallStateMachine", "getSipErrorFromUserReason: reason " + reason);
        if (this.mMno == Mno.VZW) {
            response = new SipErrorVzw();
        } else if (this.mMno.isKor()) {
            response = new SipErrorKor();
        } else if (this.mMno == Mno.CMCC) {
            response = new SipErrorCmcc();
        } else if (this.mMno == Mno.CTC || this.mMno == Mno.CTCMO) {
            response = new SipErrorCTC();
        } else if (this.mMno == Mno.KDDI) {
            response = new SipErrorKdi();
        } else if (this.mMno == Mno.DOCOMO) {
            response = new SipErrorDcm();
        } else if (this.mMno == Mno.SOFTBANK) {
            response = new SipErrorSbm();
        } else if (this.mMno == Mno.USCC) {
            response = new SipErrorUscc();
        } else if (this.mMno == Mno.GLOBE_PH) {
            response = new SipErrorGlobe();
        } else if (this.mMno == Mno.MDMN) {
            response = new SipErrorMdmn();
        } else if (this.mMno == Mno.NOVA_IS) {
            response = new SipErrorNovaIs();
        } else if (this.mMno == Mno.SPRINT) {
            response = new SipErrorSprint();
        } else {
            response = new SipErrorBase();
        }
        return response.getFromRejectReason(reason);
    }
}
