package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class ImsOutgoingCall extends CallState {
    ImsOutgoingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        int cameraId;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        this.mCsm.mTryingReceived = false;
        this.mCsm.callType = this.mSession.getCallProfile().getCallType();
        startTimer_OutgoingCall();
        start100Timer_OutgoingCall();
        Log.i("CallStateMachine", "Enter [OutgoingCall]");
        if (this.mRegistration != null && this.mRegistration.getImsProfile() != null) {
            if ((!(this.mRegistration.getImsProfile().getUsePrecondition() != 0) || !(this.mMno == Mno.ATT || this.mMno == Mno.TMOUS)) && (cameraId = this.mCsm.determineCamera(this.mCsm.callType, false)) >= 0 && !this.mSession.getCameraStartByApp()) {
                this.mSession.startCamera(cameraId);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:61:0x016c, code lost:
        if (terminate_OutgoingCall(r8) != false) goto L_0x0176;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0173, code lost:
        if (dbrLost_OutgoingCall(r8) != false) goto L_0x0176;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processMessage(android.os.Message r8) {
        /*
            r7 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "[OutgoingCall] processMessage "
            r0.append(r1)
            int r1 = r8.what
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "CallStateMachine"
            android.util.Log.i(r1, r0)
            int r0 = r8.what
            r2 = 1
            if (r0 == r2) goto L_0x0168
            r3 = 26
            r4 = 0
            if (r0 == r3) goto L_0x0155
            r3 = 41
            if (r0 == r3) goto L_0x014b
            r3 = 52
            if (r0 == r3) goto L_0x0147
            r3 = 203(0xcb, float:2.84E-43)
            if (r0 == r3) goto L_0x0143
            r3 = 301(0x12d, float:4.22E-43)
            if (r0 == r3) goto L_0x013f
            r3 = 303(0x12f, float:4.25E-43)
            if (r0 == r3) goto L_0x0177
            r3 = 502(0x1f6, float:7.03E-43)
            if (r0 == r3) goto L_0x0130
            r3 = 5000(0x1388, float:7.006E-42)
            if (r0 == r3) goto L_0x016f
            r3 = 3
            if (r0 == r3) goto L_0x0177
            r3 = 4
            if (r0 == r3) goto L_0x012b
            r3 = 93
            if (r0 == r3) goto L_0x0177
            r3 = 94
            if (r0 == r3) goto L_0x0177
            r3 = 100
            if (r0 == r3) goto L_0x0177
            r3 = 101(0x65, float:1.42E-43)
            if (r0 == r3) goto L_0x00cf
            r3 = 306(0x132, float:4.29E-43)
            if (r0 == r3) goto L_0x0177
            r3 = 307(0x133, float:4.3E-43)
            if (r0 == r3) goto L_0x0177
            r3 = 400(0x190, float:5.6E-43)
            if (r0 == r3) goto L_0x00ce
            r3 = 401(0x191, float:5.62E-43)
            if (r0 == r3) goto L_0x00c9
            switch(r0) {
                case 31: goto L_0x00c4;
                case 32: goto L_0x00bf;
                case 33: goto L_0x00ba;
                case 34: goto L_0x00b5;
                case 35: goto L_0x00b0;
                case 36: goto L_0x00a4;
                default: goto L_0x0067;
            }
        L_0x0067:
            switch(r0) {
                case 208: goto L_0x009f;
                case 209: goto L_0x0093;
                case 210: goto L_0x0093;
                default: goto L_0x006a;
            }
        L_0x006a:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = "["
            r0.append(r3)
            java.lang.String r3 = r7.getName()
            r0.append(r3)
            java.lang.String r3 = "] msg:"
            r0.append(r3)
            int r3 = r8.what
            r0.append(r3)
            java.lang.String r3 = " ignored !!!"
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r1, r0)
            goto L_0x0176
        L_0x0093:
            java.lang.String r0 = "[OutgoingCall] deferMessage Downgrade Rtt to voice call"
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            r0.deferMessage(r8)
            goto L_0x0176
        L_0x009f:
            r7.tryingTimeout_OutgoingCall()
            goto L_0x0176
        L_0x00a4:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            r0.stopRingTimer()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            r0.notifyOnCallForwarded()
            goto L_0x0176
        L_0x00b0:
            r7.sessionProgress_OutgoingCall()
            goto L_0x0176
        L_0x00b5:
            r7.ringingBack_OutgoingCall()
            goto L_0x0176
        L_0x00ba:
            r7.notifyOnCalling()
            goto L_0x0176
        L_0x00bf:
            r7.earlymedia_OutgoingCall(r8)
            goto L_0x0176
        L_0x00c4:
            r7.tyring_OutgoingCall()
            goto L_0x0176
        L_0x00c9:
            r7.rrcReleased_OutgoingCall()
            goto L_0x0176
        L_0x00ce:
            return r4
        L_0x00cf:
            java.lang.String r0 = "[OutgoingCall] sendInfo"
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r3 = r7.mSession
            com.sec.ims.volte2.data.CallProfile r3 = r3.getCallProfile()
            int r3 = r3.getCallType()
            r0.callType = r3
            java.lang.Object r0 = r8.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            java.lang.String r3 = "info"
            java.lang.String r3 = r0.getString(r3)
            java.lang.String r4 = "type"
            int r4 = r0.getInt(r4)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "info callType= %d"
            r5.append(r6)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r7.mCsm
            int r6 = r6.callType
            r5.append(r6)
            java.lang.String r6 = ", request=%s"
            r5.append(r6)
            r5.append(r3)
            java.lang.String r6 = ", ussdType=%d"
            r5.append(r6)
            r5.append(r4)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r1, r5)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r1 = r7.mVolteSvcIntf
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r7.mSession
            int r5 = r5.getSessionId()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r7.mCsm
            int r6 = r6.callType
            r1.sendInfo(r5, r6, r3, r4)
            goto L_0x0176
        L_0x012b:
            boolean r0 = r7.error_OutgoingCall(r8)
            return r0
        L_0x0130:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            r0.mReinvite = r2
            java.lang.String r0 = "[OutgoingCall] Re-INVITE defered"
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            r0.deferMessage(r8)
            goto L_0x0176
        L_0x013f:
            r7.timerVZWExpired_OutgoingCall()
            goto L_0x0176
        L_0x0143:
            r7.sessionProgressTimeout_OutgoingCall()
            goto L_0x0176
        L_0x0147:
            r7.update_OutgoingCall(r8)
            goto L_0x0176
        L_0x014b:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r7.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsInCall r1 = r1.mInCall
            r0.transitionTo(r1)
            goto L_0x0176
        L_0x0155:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            r1 = 503(0x1f7, float:7.05E-43)
            java.lang.String r3 = "Session Progress Timeout"
            r0.notifyOnError(r1, r3, r4)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r7.mCsm
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r7.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsEndingCall r1 = r1.mEndingCall
            r0.transitionTo(r1)
            goto L_0x0176
        L_0x0168:
            boolean r0 = r7.terminate_OutgoingCall(r8)
            if (r0 == 0) goto L_0x016f
            goto L_0x0176
        L_0x016f:
            boolean r0 = r7.dbrLost_OutgoingCall(r8)
            if (r0 == 0) goto L_0x0177
        L_0x0176:
            return r2
        L_0x0177:
            boolean r0 = r7.endOrFail_OutgoingCall(r8)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsOutgoingCall.processMessage(android.os.Message):boolean");
    }

    public void exit() {
        this.mCsm.removeMessages(203);
        this.mCsm.removeMessages(208);
        this.mCsm.removeMessages(CallStateMachine.ON_TIMER_VZW_EXPIRED);
        this.mCsm.setPreviousState(this);
    }

    private void startTimer_OutgoingCall() {
        String dialingNumber = this.mSession.getCallProfile().getDialingNumber();
        if (isNeedToStartVZWTimer()) {
            Log.i("CallStateMachine", "[OutgoingCall] start Timer_VZW " + getTimerVzw() + " msec.");
            this.mCsm.sendMessageDelayed((int) CallStateMachine.ON_TIMER_VZW_EXPIRED, (long) getTimerVzw());
        } else if (this.mMno == Mno.KDDI && this.mCsm.callType != 12) {
            Log.i("CallStateMachine", "[OutgoingCall] Start Session Progress Timer (10 sec).");
            this.mCsm.sendMessageDelayed(203, 10000);
        } else if (this.mMno == Mno.ELISA_EE && this.mCsm.callType != 12) {
            Log.i("CallStateMachine", "[OutgoingCall] Start Session Progress Timer (15 sec).");
            this.mCsm.sendMessageDelayed(203, 15000);
        } else if (this.mMno == Mno.ATT) {
            handleStartATTTimer(dialingNumber);
        } else if ((this.mMno == Mno.EE || this.mMno == Mno.EE_ESN) && this.mCsm.callType != 12) {
            Log.i("CallStateMachine", "[OutgoingCall] Start EE-UK Session Progress Timer (20 sec).");
            this.mCsm.sendMessageDelayed(203, 19500);
        } else if (this.mSession.isCmcSecondaryType(this.mSession.getCmcType())) {
            Log.i("CallStateMachine", "[OutgoingCall] Start Session Progress Timer for SD (12 sec).");
            this.mCsm.sendMessageDelayed(203, 12000);
        }
    }

    private int getTimerVzw() {
        return DmConfigHelper.readInt(this.mContext, "timer_vzw", 6).intValue() * 1000;
    }

    private void start100Timer_OutgoingCall() {
        if (this.mRegistration != null && this.mRegistration.getImsProfile() != null && this.mRegistration.getImsProfile().get100tryingTimer() > 0 && this.mCsm.callType != 12 && !ImsCallUtil.isE911Call(this.mCsm.callType)) {
            int timer_100trying = this.mRegistration.getImsProfile().get100tryingTimer();
            if (this.mMno == Mno.USCC && this.mModule.getSessionCount(this.mSession.getPhoneId()) == 1) {
                Log.i("CallStateMachine", "[OutgoingCall] USCC G30 Timer (12 sec)");
                this.mCsm.sendMessageDelayed(208, 12000);
            } else if (this.mMno != Mno.SFR || this.mSession.isEpdgCall()) {
                Log.i("CallStateMachine", "[OutgoingCall] Start 100 Trying Timer (" + timer_100trying + " msec).");
                this.mCsm.sendMessageDelayed(208, (long) timer_100trying);
            } else {
                Log.i("CallStateMachine", "[OutgoingCall] Skip 100 Trying Timer ()");
            }
        }
    }

    private void tyring_OutgoingCall() {
        this.mCsm.mTryingReceived = true;
        notifyOnTrying();
        if (!(this.mMno == Mno.CMCC || this.mMno == Mno.VIVA_BAHRAIN || this.mMno == Mno.ETISALAT_UAE || this.mSession.mKaSender == null)) {
            this.mSession.mKaSender.start();
        }
        this.mCsm.removeMessages(208);
        this.mCsm.removeMessages(CallStateMachine.ON_TIMER_VZW_EXPIRED);
        if (this.mMno == Mno.VZW || this.mMno == Mno.RJIL) {
            this.mCsm.sendMessageDelayed(203, 180000);
        }
        if ((this.mMno == Mno.CTC || this.mMno == Mno.CTCMO) && this.mRegistration != null && this.mRegistration.getImsProfile() != null) {
            this.mCsm.sendMessageDelayed(203, (long) this.mRegistration.getImsProfile().getTimerB());
        }
    }

    private void earlymedia_OutgoingCall(Message msg) {
        Log.i("CallStateMachine", "mSession.getCallProfile().isVideoCRBT: " + this.mSession.getCallProfile().isVideoCRBT());
        if (this.mRegistration != null && this.mSession.getCallProfile().isVideoCRBT()) {
            this.mVolteSvcIntf.startVideoEarlyMedia(this.mSession.getSessionId());
        }
        this.mCsm.notifyOnEarlyMediaStarted(msg.arg1);
        this.mCsm.transitionTo(this.mCsm.mAlertingCall);
    }

    private void ringingBack_OutgoingCall() {
        int ringbacktimer;
        this.mCsm.notifyOnRingingBack();
        this.mCsm.transitionTo(this.mCsm.mAlertingCall);
        if (this.mRegistration != null && (ringbacktimer = this.mRegistration.getImsProfile().getRingbackTimer()) > 0) {
            this.mCsm.startRingTimer(((long) ringbacktimer) * 1000);
        }
    }

    private void sessionProgress_OutgoingCall() {
        this.mCsm.removeMessages(203);
        this.mCsm.removeMessages(208);
        this.mCsm.removeMessages(CallStateMachine.ON_TIMER_VZW_EXPIRED);
        if (this.mSession.mKaSender != null) {
            this.mSession.mKaSender.stop();
        }
        int cameraId = this.mCsm.determineCamera(this.mCsm.callType, false);
        if (cameraId >= 0) {
            this.mSession.startCamera(cameraId);
        }
    }

    private boolean terminate_OutgoingCall(Message msg) {
        if (!this.mCsm.mTryingReceived && msg.arg1 == 4 && !this.mCsm.srvccStarted) {
            Log.i("CallStateMachine", "Network Handover on dialing (before get 100 TRYING)");
            if (this.mMno != Mno.VZW || !ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                this.mCsm.notifyOnEnded(1117);
            } else {
                this.mCsm.notifyOnEnded(1107);
            }
        }
        if (this.mCsm.mTryingReceived || !(msg.arg1 == 14 || msg.arg2 == 3)) {
            if (!this.mCsm.mTryingReceived && msg.arg1 == 13) {
                Log.i("CallStateMachine", "PS Barred. notify error call barred by network!");
                this.mCsm.notifyOnError(2801, "ps Barred");
                this.mCsm.transitionTo(this.mCsm.mEndingCall);
            }
            if ((this.mMno != Mno.KDDI && this.mMno != Mno.DOCOMO) || msg.arg1 != 25) {
                return false;
            }
            Log.i("CallStateMachine", "on terminate out of service.");
            this.mCsm.notifyOnError(1114, "CALL_INVITE_TIMEOUT");
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
            return true;
        }
        Log.i("CallStateMachine", "Deregistered. notify error 1701 for CSFB");
        this.mCsm.notifyOnError(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS, "deregistered");
        this.mCsm.transitionTo(this.mCsm.mEndingCall);
        return true;
    }

    private boolean dbrLost_OutgoingCall(Message msg) {
        if (this.mMno != Mno.PLAY || msg.what != 5000 || this.mSession.getDedicatedBearerState(msg.arg1) == 3) {
            return false;
        }
        Log.i("CallStateMachine", "dedicated bearer was re-established, the call is not terminated");
        return true;
    }

    private boolean endOrFail_OutgoingCall(Message msg) {
        if ((this.mMno == Mno.TMOUS || this.mMno == Mno.SPRINT) && msg.what == 100) {
            Log.i("CallStateMachine", "[OutgoingCall] Skip FORCE_NOTIFY_CURRENT_CODEC");
            return true;
        } else if (!this.mCsm.mNeedToWaitEndcall) {
            return false;
        } else {
            Log.i("CallStateMachine", "[OutgoingCall] need to Wait Endcall");
            this.mCsm.mNeedToWaitEndcall = false;
            return true;
        }
    }

    private boolean error_OutgoingCall(Message msg) {
        Message message = msg;
        Log.e("CallStateMachine", "[OutgoingCall] on error.");
        int i = 0;
        if (!this.mCsm.handleSPRoutgoingError(message)) {
            return false;
        }
        if ((this.mMno == Mno.KDDI || this.mMno == Mno.DOCOMO) && ((SipError) message.obj).getCode() == 709) {
            Log.i("CallStateMachine", "on error 709.");
            this.mCsm.sendMessage(1, 25);
            return true;
        }
        String errorMessage = "";
        if (this.mMno == Mno.BELL) {
            SipError err = (SipError) message.obj;
            boolean needDelayed = this.mModule.isCsfbErrorCode(this.mSession.getPhoneId(), this.mSession.getCallProfile().getCallType(), new SipError(err.getCode(), err.getReason() == null ? errorMessage : err.getReason()));
            Log.e("CallStateMachine", "On error delayed for 300ms, needDelayed : " + needDelayed + " ,mOnErrorDelayed : " + this.mCsm.mOnErrorDelayed);
            if (this.mCsm.mOnErrorDelayed || !needDelayed) {
                this.mCsm.mOnErrorDelayed = false;
            } else {
                this.mCsm.mOnErrorDelayed = true;
                this.mCsm.sendMessageDelayed(Message.obtain(msg), 300);
                return true;
            }
        }
        if (this.mCsm.mIsWPSCall) {
            this.mCsm.sendMessageDelayed(26, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            this.mModule.releaseSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.HeldCall);
            return true;
        } else if (this.mMno != Mno.CMCC) {
            return false;
        } else {
            Log.i("CallStateMachine", "[OutgoingCall] check delay!");
            SipError err2 = (SipError) message.obj;
            int code = err2.getCode();
            if (err2.getReason() != null) {
                errorMessage = err2.getReason();
            }
            boolean needEndHeldCall = (code == 380 || code == 382) && this.mModule.isCsfbErrorCode(this.mSession.getPhoneId(), this.mSession.getCallProfile().getCallType(), new SipError(code, errorMessage));
            Log.i("CallStateMachine", "needEndHeldCall : " + needEndHeldCall + ", mOnErrorDelayed : " + this.mCsm.mOnErrorDelayed);
            if (this.mSession.getCallProfile().isConferenceCall() || !needEndHeldCall || this.mCsm.mOnErrorDelayed) {
                return false;
            }
            for (ImsCallSession s : this.mModule.getSessionList(this.mSession.getPhoneId())) {
                Log.i("CallStateMachine", "phoneId[" + this.mSession.getPhoneId() + "] session Id : " + s.getSessionId() + ", state : " + s.getCallState());
                if (s.getSessionId() != this.mSession.getSessionId() && s.getCallState() == CallConstants.STATE.HeldCall) {
                    CallStateMachine callStateMachine = this.mCsm;
                    String[] strArr = new String[i];
                    SipReason sipReason = r12;
                    SipReason sipReason2 = new SipReason("SIP", 0, "User triggered", true, strArr);
                    callStateMachine.sipReason = sipReason;
                    this.mVolteSvcIntf.endCall(s.getSessionId(), s.getCallProfile().getCallType(), this.mCsm.sipReason);
                    this.mCsm.mOnErrorDelayed = true;
                }
                i = 0;
            }
            if (!this.mCsm.mOnErrorDelayed) {
                return false;
            }
            Log.i("CallStateMachine", "error notify delayed!");
            this.mCsm.sendMessageDelayed(Message.obtain(msg), 200);
            return true;
        }
    }

    private void sessionProgressTimeout_OutgoingCall() {
        IRegistrationGovernor governor;
        Log.i("CallStateMachine", "[OutgoingCall] SessionProgress Timeout - Call Terminate/CSFB");
        if (this.mMno == Mno.VZW || this.mMno == Mno.CTC || this.mMno == Mno.CTCMO) {
            this.mCsm.notifyOnError(SipErrorBase.REQUEST_TIMEOUT.getCode(), SipErrorBase.REQUEST_TIMEOUT.getReason(), 0);
        } else if (this.mMno == Mno.KDDI && this.mModule.getSessionCount(this.mSession.getPhoneId()) > 1) {
            this.mCsm.errorCode = 503;
        } else if (this.mSession.isCmcSecondaryType(this.mSession.getCmcType())) {
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE, "REJECT_REASON_PD_UNREACHABLE", 0);
        } else if (this.mCsm.mIsWPSCall) {
            this.mCsm.mNeedToWaitEndcall = true;
            Log.i("CallStateMachine", "[OutgoingCall] CANCEL now CSFB after 2s");
            this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(17);
            this.mVolteSvcIntf.endCall(this.mSession.getSessionId(), this.mCsm.callType, this.mCsm.sipReason);
            this.mCsm.sendMessageDelayed(26, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            this.mModule.releaseSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.HeldCall);
            return;
        } else {
            this.mCsm.notifyOnError(503, "Session Progress Timeout", 0);
            if (!(this.mMno != Mno.EE || this.mRegistration == null || (governor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle())) == null)) {
                governor.onSipError("mmtel", SipErrorBase.SIP_INVITE_TIMEOUT);
            }
        }
        this.mCsm.sendMessage(1, 17);
    }

    private void tryingTimeout_OutgoingCall() {
        if (this.mMno != Mno.TMOUS || ImsCallUtil.isE911Call(this.mSession.mCallProfile.getCallType())) {
            Log.i("CallStateMachine", "[OutgoingCall] 100 Trying Timeout - Call Terminate/CSFB");
            this.mCsm.notifyOnError(503, "100 Trying Timeout", 0);
            if (this.mMno.isChn()) {
                Log.i("CallStateMachine", "Force to change END_REASON to terminate client socket with RST");
                this.mCsm.sendMessage(1, 8);
            } else {
                this.mCsm.sendMessage(1, 17);
            }
            if (this.mMno == Mno.USCC && this.mRegistration != null) {
                Log.i("CallStateMachine", "[OutgoingCall] USCC 12 sec 100 Trying Timer expired.");
                IRegistrationGovernor governor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle());
                if (governor != null) {
                    String service = "mmtel";
                    if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                        service = "mmtel-video";
                    }
                    governor.onSipError(service, new SipError(503));
                    return;
                }
                return;
            }
            return;
        }
        Log.i("CallStateMachine", "[OutgoingCall] TMOUS, 100 Trying Timeout");
        this.mCsm.errorCode = 28;
    }

    private void timerVZWExpired_OutgoingCall() {
        IRegistrationGovernor governor;
        Log.i("CallStateMachine", "[OutgoingCall] TimerVzw expired.");
        if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
            Log.i("CallStateMachine", "[OutgoingCall] But AirplainModeOn, cannot fallback to 1x");
            this.mCsm.sendMessage(1, NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE, 0, "Timer_VZW expired");
            return;
        }
        this.mCsm.sendMessage(4, 0, -1, new SipError(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE, "Timer_VZW expired"));
        if (!(this.mRegistration == null || (governor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle())) == null)) {
            String service = "mmtel";
            if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                service = "mmtel-video";
            }
            governor.onSipError(service, new SipError(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE));
        }
        this.mCsm.sipReason = new SipReason("", 0, "TIMER VZW EXPIRED", true, new String[0]);
        this.mVolteSvcIntf.endCall(this.mSession.getSessionId(), this.mSession.getCallProfile().getCallType(), this.mCsm.sipReason);
    }

    private void rrcReleased_OutgoingCall() {
        IRegistrationGovernor governor;
        Log.i("CallStateMachine", "[OutgoingCall] RRC connection released.");
        if (this.mMno != Mno.VZW || (!this.mTelephonyManager.isNetworkRoaming() && !ImsUtil.isCdmalessEnabled(this.mSession.getPhoneId()))) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID, "RRC connection released"));
        } else {
            Log.i("CallStateMachine", "Socket close with NO_LINGER in case RRC Non-Depriorization Reject in MO case");
            this.mCsm.sendMessage(1, 23);
        }
        if (this.mMno == Mno.DOCOMO) {
            this.mVolteSvcIntf.DeleteTcpSocket(this.mSession.getSessionId(), this.mSession.getCallProfile().getCallType());
        }
        if (this.mRegistration != null && (governor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle())) != null) {
            String service = "mmtel";
            if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                service = "mmtel-video";
            }
            governor.onSipError(service, new SipError(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID));
        }
    }

    private void update_OutgoingCall(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        CallProfile profile = bundle.getParcelable("profile");
        Log.i("CallStateMachine", "Received srvcc H/O event");
        int srvccVersion = this.mModule.getSrvccVersion(this.mSession.getPhoneId());
        if (profile == null && srvccVersion != 0) {
            if (srvccVersion >= 10 || DeviceUtil.getGcfMode().booleanValue()) {
                Log.i("CallStateMachine", "MO bsrvcc support");
                this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", bundle.getInt("cause"), bundle.getString("reasonText"), new String[0]));
            }
        }
    }

    private boolean isNeedToStartVZWTimer() {
        if (this.mMno != Mno.VZW || ImsCallUtil.isVideoCall(this.mCsm.callType) || ImsRegistry.getPdnController().isEpdgConnected(this.mSession.getPhoneId()) || this.mModule.getSessionCount(this.mSession.getPhoneId()) != 1) {
            return false;
        }
        return true;
    }

    private void handleStartATTTimer(String dialingNumber) {
        if (this.mCsm.needToLogForATTGate(this.mCsm.callType)) {
            IMSLog.g("GATE", "<GATE-M>MO_VIDEO_CALL</GATE-M>");
        } else if (ImsCallUtil.isE911Call(this.mCsm.callType) && this.mRegistration != null && this.mRegistration.getImsProfile() != null && this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
            this.mCsm.sendMessageDelayed(203, 12000);
        } else if (dialingNumber == null) {
        } else {
            if (dialingNumber.startsWith("*272") || dialingNumber.startsWith("#31#*272") || dialingNumber.startsWith("*31#*272")) {
                this.mCsm.sendMessageDelayed(203, 8000);
                this.mCsm.mIsWPSCall = true;
            }
        }
    }

    private void notifyOnCalling() {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onCalling();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyOnTrying() {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onTrying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }
}
