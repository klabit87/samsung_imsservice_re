package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImsInCall extends CallState {
    ImsInCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        enter_InCall();
        if (!ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType()) && this.mSession.getDedicatedBearerState(99) != 3) {
            Log.i("CallStateMachine", "[InCall] mRttBearerState initialzed to BEARER_STATE_CLOSED");
            this.mSession.setDedicatedBearerState(99, 3);
        }
        if (!checkVideo_InCall()) {
            this.mCsm.mPreAlerting = false;
            this.mCsm.mIsWPSCall = false;
            this.mCsm.mCameraUsedAtOtherApp = false;
            this.mSession.setIsEstablished(true);
            StringBuilder sb = new StringBuilder();
            CallStateMachine callStateMachine = this.mCsm;
            sb.append(callStateMachine.mCallTypeHistory);
            sb.append(",");
            sb.append(this.mSession.getCallProfile().getCallType());
            callStateMachine.mCallTypeHistory = sb.toString();
            Log.i("CallStateMachine", "Enter [InCall]");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:40:0x01ec, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processMessage(android.os.Message r11) {
        /*
            r10 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "[InCall] processMessage "
            r0.append(r1)
            int r1 = r11.what
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "CallStateMachine"
            android.util.Log.i(r1, r0)
            int r0 = r11.what
            r2 = 0
            r3 = 1
            switch(r0) {
                case 1: goto L_0x01eb;
                case 3: goto L_0x01eb;
                case 4: goto L_0x01eb;
                case 25: goto L_0x01e7;
                case 41: goto L_0x01e3;
                case 51: goto L_0x01df;
                case 52: goto L_0x01da;
                case 55: goto L_0x01d5;
                case 56: goto L_0x01a8;
                case 59: goto L_0x019e;
                case 60: goto L_0x019a;
                case 62: goto L_0x0194;
                case 64: goto L_0x015a;
                case 71: goto L_0x014e;
                case 73: goto L_0x0141;
                case 74: goto L_0x013c;
                case 75: goto L_0x0137;
                case 80: goto L_0x0132;
                case 81: goto L_0x012d;
                case 82: goto L_0x0117;
                case 83: goto L_0x00f8;
                case 86: goto L_0x00ec;
                case 87: goto L_0x00de;
                case 91: goto L_0x00d9;
                case 93: goto L_0x01eb;
                case 94: goto L_0x01eb;
                case 100: goto L_0x01eb;
                case 101: goto L_0x007c;
                case 205: goto L_0x0077;
                case 206: goto L_0x0077;
                case 207: goto L_0x0072;
                case 209: goto L_0x006d;
                case 210: goto L_0x006d;
                case 302: goto L_0x0064;
                case 400: goto L_0x01eb;
                case 502: goto L_0x005f;
                case 600: goto L_0x005a;
                case 700: goto L_0x0053;
                case 5000: goto L_0x0048;
                default: goto L_0x001f;
            }
        L_0x001f:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "["
            r0.append(r2)
            java.lang.String r2 = r10.getName()
            r0.append(r2)
            java.lang.String r2 = "] msg:"
            r0.append(r2)
            int r2 = r11.what
            r0.append(r2)
            java.lang.String r2 = " ignored !!!"
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r1, r0)
            goto L_0x01ec
        L_0x0048:
            int r0 = r10.dbrLost_InCall(r11)
            r1 = -1
            if (r0 == r1) goto L_0x01eb
            if (r0 != r3) goto L_0x0052
            r2 = r3
        L_0x0052:
            return r2
        L_0x0053:
            int r0 = r11.arg1
            r10.notifyRecordState(r0)
            goto L_0x01ec
        L_0x005a:
            r10.enter()
            goto L_0x01ec
        L_0x005f:
            r10.reInvite_InCall()
            goto L_0x01ec
        L_0x0064:
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r0 = r10.mSession
            com.sec.ims.volte2.data.CallProfile r0 = r0.mModifyRequestedProfile
            boolean r0 = r10.handleUpdate(r0)
            return r0
        L_0x006d:
            r10.rttDBRLost_InCall()
            goto L_0x01ec
        L_0x0072:
            r10.camStartFailed_InCall()
            goto L_0x01ec
        L_0x0077:
            r10.videoRTPTImer_InCall()
            goto L_0x01ec
        L_0x007c:
            java.lang.String r0 = "[InCall] sendInfo"
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r10.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r2 = r10.mSession
            com.sec.ims.volte2.data.CallProfile r2 = r2.getCallProfile()
            int r2 = r2.getCallType()
            r0.callType = r2
            java.lang.Object r0 = r11.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            java.lang.String r2 = "info"
            java.lang.String r2 = r0.getString(r2)
            java.lang.String r4 = "type"
            int r4 = r0.getInt(r4)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "info callType= %d"
            r5.append(r6)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r10.mCsm
            int r6 = r6.callType
            r5.append(r6)
            java.lang.String r6 = ", request=%s"
            r5.append(r6)
            r5.append(r2)
            java.lang.String r6 = ", ussdType=%d"
            r5.append(r6)
            r5.append(r4)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r1, r5)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r1 = r10.mVolteSvcIntf
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r10.mSession
            int r5 = r5.getSessionId()
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r6 = r10.mCsm
            int r6 = r6.callType
            r1.sendInfo(r5, r6, r2, r4)
            goto L_0x01ec
        L_0x00d9:
            r10.modified_InCall(r11)
            goto L_0x01ec
        L_0x00de:
            java.lang.String r0 = "[InCall] Receive CMC INFO EVENT."
            android.util.Log.i(r1, r0)
            java.lang.Object r0 = r11.obj
            com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent r0 = (com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent) r0
            r10.notifyCmcInfoEvent(r0)
            goto L_0x01ec
        L_0x00ec:
            java.lang.String r0 = "[InCall] Receive CMC DTMF EVENT."
            android.util.Log.i(r1, r0)
            int r0 = r11.arg1
            r10.notifyCmcDtmfEvent(r0)
            goto L_0x01ec
        L_0x00f8:
            java.lang.String r0 = "[InCall] Video resumed."
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r10.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r10.mSession
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()
            int r1 = r1.getCallType()
            r0.notifyOnModified(r1)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r10.mCsm
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r10.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsInCall r1 = r1.mInCall
            r0.transitionTo(r1)
            goto L_0x01ec
        L_0x0117:
            java.lang.String r0 = "[InCall] Video held."
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r10.mCsm
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r10.mSession
            com.sec.ims.volte2.data.CallProfile r1 = r1.getCallProfile()
            int r1 = r1.getCallType()
            r0.notifyOnModified(r1)
            goto L_0x01ec
        L_0x012d:
            r10.resumeVideo_InCall()
            goto L_0x01ec
        L_0x0132:
            r10.holdVideo_InCall()
            goto L_0x01ec
        L_0x0137:
            r10.referStatus_InCall(r11)
            goto L_0x01ec
        L_0x013c:
            r10.extendToConf_InCall(r11)
            goto L_0x01ec
        L_0x0141:
            java.lang.Object r0 = r11.obj
            java.lang.String[] r0 = (java.lang.String[]) r0
            java.util.List r1 = java.util.Arrays.asList(r0)
            r10.extendToConference(r1)
            goto L_0x01ec
        L_0x014e:
            java.lang.String r0 = "[InCall] already in InCall"
            android.util.Log.i(r1, r0)
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r10.mCsm
            r0.notifyOnResumed(r3)
            goto L_0x01ec
        L_0x015a:
            java.lang.Object r0 = r11.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            java.lang.String r2 = "text"
            java.lang.String r2 = r0.getString(r2)
            java.lang.String r4 = "len"
            int r4 = r0.getInt(r4)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "text="
            r5.append(r6)
            r5.append(r2)
            java.lang.String r6 = ", len="
            r5.append(r6)
            r5.append(r4)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r1, r5)
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r1 = r10.mVolteSvcIntf
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r5 = r10.mSession
            int r5 = r5.getSessionId()
            r1.sendText(r5, r2, r4)
            goto L_0x01ec
        L_0x0194:
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r0 = r10.mCsm
            r0.handleRemoteHeld(r3)
            goto L_0x01ec
        L_0x019a:
            r10.cancelTransfer_InCall()
            goto L_0x01ec
        L_0x019e:
            java.lang.Object r0 = r11.obj
            java.lang.String r0 = (java.lang.String) r0
            com.sec.internal.ims.servicemodules.volte2.CallStateMachine r1 = r10.mCsm
            r1.transferCall(r0)
            goto L_0x01ec
        L_0x01a8:
            java.lang.Object r0 = r11.obj
            android.os.Bundle r0 = (android.os.Bundle) r0
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r4 = r10.mVolteSvcIntf
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r1 = r10.mSession
            int r5 = r1.getSessionId()
            java.lang.String r1 = "code"
            int r6 = r0.getInt(r1)
            java.lang.String r1 = "mode"
            int r7 = r0.getInt(r1)
            java.lang.String r1 = "operation"
            int r8 = r0.getInt(r1)
            java.lang.String r1 = "result"
            android.os.Parcelable r1 = r0.getParcelable(r1)
            r9 = r1
            android.os.Message r9 = (android.os.Message) r9
            r4.handleDtmf(r5, r6, r7, r8, r9)
            goto L_0x01ec
        L_0x01d5:
            boolean r0 = r10.switchRequest_InCall(r11)
            return r0
        L_0x01da:
            boolean r0 = r10.update_InCall(r11)
            return r0
        L_0x01df:
            r10.hold_InCall()
            goto L_0x01ec
        L_0x01e3:
            r10.established_InCall()
            goto L_0x01ec
        L_0x01e7:
            r10.checkVideoDBR_InCall()
            goto L_0x01ec
        L_0x01eb:
            return r2
        L_0x01ec:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsInCall.processMessage(android.os.Message):boolean");
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }

    private void enter_InCall() {
        if (this.mCsm.hasMessages(CallStateMachine.ON_LTE_911_FAIL)) {
            this.mCsm.removeMessages(CallStateMachine.ON_LTE_911_FAIL);
        }
        if (this.mSession.mKaSender != null) {
            this.mSession.mKaSender.stop();
        }
        this.mSession.getCallProfile().setVideoCRBT(false);
        if (this.mCsm.getPreviousState() == this.mCsm.mOutgoingCall || this.mCsm.getPreviousState() == this.mCsm.mIncomingCall || this.mCsm.getPreviousState() == this.mCsm.mAlertingCall || (this.mCsm.getPreviousState() == this.mCsm.mReadyToCall && this.mSession.getCallProfile().isPullCall())) {
            Log.i("CallStateMachine", "[InCall] Notify on Established");
            IMSLog.LAZER_TYPE lazer_type = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type, this.mSession.getCallId() + " - CONNECTED");
            this.mCsm.notifyOnEstablished();
            if (this.mMno == Mno.TMOUS || this.mMno == Mno.VZW) {
                this.mCsm.forceNotifyCurrentCodec();
            }
            handleSetVideoQuality();
            if (this.mCsm.needToLogForATTGate(this.mSession.getCallProfile().getCallType())) {
                IMSLog.g("GATE", "<GATE-M>VIDEO_CALL_CONNECTED</GATE-M>");
            }
            ImsRegistry.getImsDiagMonitor().notifyCallStatus(this.mSession.getSessionId(), "CALL_ESTABLISHED", this.mSession.getCallProfile().getCallType(), this.mSession.getCallProfile().getMediaProfile().getAudioCodec().toString());
        }
        if ((this.mCsm.getPreviousState() == this.mCsm.mOutgoingCall || this.mCsm.getPreviousState() == this.mCsm.mIncomingCall || this.mCsm.getPreviousState() == this.mCsm.mAlertingCall) && this.mSession.getCmcType() == 0) {
            this.mCsm.mCmcCallEstablishTime = System.currentTimeMillis();
            Log.i("CallStateMachine", "[InCall] VoLTE callEstablishTime : " + this.mCsm.mCmcCallEstablishTime);
        }
        if ((this.mCsm.getPreviousState() == this.mCsm.mModifyRequested || this.mCsm.getPreviousState() == this.mCsm.mModifyingCall) && this.mSession.getCallProfile().getCallType() == 1) {
            this.mCsm.sendCmcPublishDialog();
        }
    }

    private boolean handleUpdate(CallProfile profile) {
        if (profile == null) {
            return false;
        }
        boolean needToTransition = false;
        if (this.mCsm.isChangedCallType(profile) && this.mCsm.modifyCallType(profile, true)) {
            needToTransition = true;
        }
        if (needToTransition) {
            this.mSession.mModifyRequestedProfile = profile;
            this.mCsm.transitionTo(this.mCsm.mModifyingCall);
        } else {
            this.mSession.mModifyRequestedProfile = null;
        }
        return true;
    }

    private void handleSetVideoQuality() {
        if (this.mMno != Mno.RJIL) {
            return;
        }
        if ("HD720" == this.mSession.getCallProfile().getMediaProfile().getVideoSize() || "HD720LAND" == this.mSession.getCallProfile().getMediaProfile().getVideoSize()) {
            this.mSession.getCallProfile().getMediaProfile().setVideoQuality(16);
        } else if ("VGA" == this.mSession.getCallProfile().getMediaProfile().getVideoSize() || "VGALAND" == this.mSession.getCallProfile().getMediaProfile().getVideoSize()) {
            this.mSession.getCallProfile().getMediaProfile().setVideoQuality(15);
        } else if ("QVGA" == this.mSession.getCallProfile().getMediaProfile().getVideoSize() || "QVGALAND" == this.mSession.getCallProfile().getMediaProfile().getVideoSize()) {
            this.mSession.getCallProfile().getMediaProfile().setVideoQuality(13);
        }
    }

    private boolean checkVideo_InCall() {
        if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            this.mCsm.startNetworkStatsOnPorts();
        }
        int cameraId = this.mCsm.determineCamera(this.mSession.getCallProfile().getCallType(), false);
        if (cameraId >= 0) {
            this.mSession.startCamera(cameraId);
            this.mCsm.mIsStartCameraSuccess = true;
        } else if (this.mSession.getUsingCamera()) {
            this.mCsm.mIsCheckVideoDBR = false;
            this.mSession.stopCamera();
            if (this.mCsm.getPreviousState() == this.mCsm.mOutgoingCall || this.mCsm.getPreviousState() == this.mCsm.mIncomingCall || this.mCsm.getPreviousState() == this.mCsm.mAlertingCall) {
                this.mSession.mLastUsedCamera = this.mSession.mPrevUsedCamera;
            }
        }
        if (!this.mCsm.mIsStartCameraSuccess && this.mMno != Mno.DOCOMO && !this.mMno.isKor()) {
            if (downgradeVideoToVoiceRequest()) {
                this.mSession.notifyCallDowngraded();
                this.mCsm.transitionTo(this.mCsm.mModifyingCall);
            }
            this.mCsm.mIsStartCameraSuccess = true;
        }
        if (this.mCsm.mIsCheckVideoDBR || this.mMno != Mno.TMOUS || !ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) || ImsRegistry.getPdnController().isEpdgConnected(this.mSession.getPhoneId()) || this.mSession.mIsNrSaMode) {
            if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) && !this.mModule.isCallServiceAvailable(this.mSession.getPhoneId(), "mmtel-video") && this.mSession.getCallProfile().getCallType() != 8) {
                Log.i("CallStateMachine", "[InCall] ForceDowngrade trigger due to MMTEL-VIDEO was not exist case");
                CallProfile profile = new CallProfile();
                profile.setCallType(1);
                if (this.mCsm.modifyCallType(profile, true)) {
                    this.mSession.notifyCallDowngraded();
                    this.mCsm.transitionTo(this.mCsm.mModifyingCall);
                    return true;
                }
            }
            return false;
        }
        int interval = (this.mSession.getCallProfile().isMOCall() ? 1500 : 500) + new Random().nextInt(1000);
        this.mCsm.mIsCheckVideoDBR = true;
        this.mCsm.sendMessageDelayed(25, 0, -1, (long) interval);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean downgradeVideoToVoiceRequest() {
        if (this.mSession.getCallProfile().getCallType() != 2 && this.mSession.getCallProfile().getCallType() != 3) {
            return false;
        }
        Log.i("CallStateMachine", "[InCall] downgradeVideoToVoiceRequest() trigger downgrade");
        CallProfile profile = new CallProfile();
        profile.setCallType(1);
        this.mSession.mModifyRequestedProfile = profile;
        return this.mCsm.modifyCallType(profile, true);
    }

    private void hold_InCall() {
        if (this.mRegistration == null || this.mModule.isProhibited(this.mSession.getPhoneId())) {
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
        } else if (this.mVolteSvcIntf.holdCall(this.mSession.getSessionId()) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "remote exception"));
        } else {
            this.mCsm.transitionTo(this.mCsm.mHoldingCall);
        }
    }

    private void established_InCall() {
        if (this.mMno != Mno.STARHUB) {
            this.mCsm.handleRemoteHeld(false);
        }
    }

    private boolean update_InCall(Message msg) {
        if (!this.mModule.isProhibited(this.mSession.getPhoneId())) {
            return handleUpdate(((Bundle) msg.obj).getParcelable("profile"));
        }
        this.mCsm.notifyOnError(1109, "Call switch failed");
        return true;
    }

    private void videoRTPTImer_InCall() {
        Log.i("CallStateMachine", "[InCall] Downgrade Video Quality due to Poor Video Quality/RTP Timeout");
        IMSLog.c(LogClass.VOLTE_VIDEO_RTP_TIMEOUT, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
        this.mCsm.mVideoRTPtimeout = true;
        if (handleVideoDowngradeRequest()) {
            this.mSession.notifyCallDowngraded();
            this.mCsm.transitionTo(this.mCsm.mModifyingCall);
        }
    }

    private void rttDBRLost_InCall() {
        Log.i("CallStateMachine", "[InCall] Downgrade voice call due to Rtt DBR Timeout/Lost");
        if (handleRttDowngradeRequest()) {
            this.mCsm.transitionTo(this.mCsm.mModifyingCall);
        }
    }

    /* access modifiers changed from: protected */
    public boolean handleRttDowngradeRequest() {
        EpdgManager epdgManager;
        Log.i("CallStateMachine", "[InCall] handleRttDowngradeRequest: " + this.mCsm.getCurrentState().getName());
        this.mSession.setRttDedicatedBearerTimeoutMessage((Message) null);
        if (this.mRegistrationManager != null && this.mRegistrationManager.isVoWiFiSupported(this.mSession.getPhoneId()) && (epdgManager = this.mModule.getEpdgManager()) != null && epdgManager.isDuringHandoverForIMS()) {
            Log.i("CallStateMachine", "handleRttDowngradeRequest: ignore RTT Dedicated Bearer Lost due to EPDG for mno:" + this.mMno);
            this.mSession.stopRttDedicatedBearerTimer();
            this.mSession.setDedicatedBearerState(99, 3);
            return false;
        } else if (!ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType()) || this.mCsm.mRemoteHeld) {
            return false;
        } else {
            Log.i("CallStateMachine", "handleRttDowngradeRequest: trigger downgrade");
            CallProfile profile = new CallProfile();
            profile.setCallType(1);
            this.mSession.mModifyRequestedProfile = profile;
            return this.mCsm.modifyCallType(profile, true);
        }
    }

    private boolean switchRequest_InCall(Message msg) {
        this.mSession.mModifyRequestedProfile = new CallProfile();
        this.mSession.mModifyRequestedProfile.setCallType(msg.arg1);
        this.mSession.mModifyRequestedProfile.getMediaProfile().setVideoQuality(this.mSession.getCallProfile().getMediaProfile().getVideoQuality());
        if (this.mModule.hasRingingCall()) {
            Log.i("CallStateMachine", "[InCall] Rejecting switch request - send 603 to remote party has Incoming call on other session");
            if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) >= 0) {
                return true;
            }
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
            return false;
        }
        int cameraId = this.mCsm.determineCamera(this.mSession.mModifyRequestedProfile.getCallType(), true);
        if (!this.mSession.getUsingCamera() && cameraId >= 0) {
            this.mSession.startCamera(cameraId);
        }
        if (!ImsCallUtil.isTtyCall(this.mSession.mModifyRequestedProfile.getCallType()) && !ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType()) && !ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType())) {
            this.mMediaController.receiveSessionModifyRequest(this.mSession.getSessionId(), this.mSession.mModifyRequestedProfile);
        }
        this.mCsm.transitionTo(this.mCsm.mModifyRequested);
        if (this.mSession.mModifyRequestedProfile.getCallType() == 9) {
            this.mCsm.sendMessage(22, (Object) this.mSession.mModifyRequestedProfile);
        } else if (ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType()) || ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType())) {
            this.mModule.onSendRttSessionModifyRequest(this.mSession.getCallId(), ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType()));
        } else {
            notifyOnSessionUpdateRequested(msg.arg1, (byte[]) msg.obj);
        }
        return true;
    }

    private void holdVideo_InCall() {
        if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) && this.mMno == Mno.VZW) {
            if (this.mCsm.isDeferedVideoResume) {
                Log.i("CallStateMachine", "[InCall] video resume defered. ignore video hold");
                this.mCsm.isDeferedVideoResume = false;
                return;
            }
            this.mMediaController.holdVideo(this.mSession.getSessionId());
            this.mCsm.transitionTo(this.mCsm.mHoldingVideo);
        }
    }

    private void resumeVideo_InCall() {
        if (this.mMno == Mno.VZW && ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            this.mCsm.isDeferedVideoResume = false;
            this.mMediaController.resumeVideo(this.mSession.getSessionId());
            this.mCsm.transitionTo(this.mCsm.mResumingVideo);
        }
    }

    private void extendToConference(List<String> participants) {
        ArrayList<String> mParticipantsUris = new ArrayList<>();
        int CallType = this.mSession.getCallProfile().getCallType();
        for (int i = 0; i < participants.size(); i++) {
            if (this.mMno != Mno.LGU || participants.get(i) == null || !participants.get(i).equals(this.mSession.getCallProfile().getDialingNumber())) {
                mParticipantsUris.add(this.mSession.buildUri(participants.get(i), (String) null, CallType).toString());
            }
        }
        if (this.mRegistration == null || mParticipantsUris.size() <= 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "Not enough participant."));
            return;
        }
        ImsProfile profile = this.mRegistration.getImsProfile();
        ConfCallSetupData data = new ConfCallSetupData(this.mSession.getConferenceUri(profile), mParticipantsUris, CallType);
        data.enableSubscription(this.mSession.getConfSubscribeEnabled(profile));
        data.setSubscribeDialogType(this.mSession.getConfSubscribeDialogType(profile));
        data.setReferUriType(this.mSession.getConfReferUriType(profile));
        data.setRemoveReferUriType(this.mSession.getConfRemoveReferUriType(profile));
        data.setReferUriAsserted(this.mSession.getConfReferUriAsserted(profile));
        data.setOriginatingUri(this.mSession.getOriginatingUri());
        data.setUseAnonymousUpdate(this.mSession.getConfUseAnonymousUpdate(profile));
        data.setSupportPrematureEnd(this.mSession.getConfSupportPrematureEnd(profile));
        int sessionId = this.mVolteSvcIntf.addUserForConferenceCall(this.mSession.getSessionId(), data, true);
        Log.i("CallStateMachine", "[InCall] extendToConference() returned session id " + sessionId);
        if (sessionId < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, "stack return -1"));
        }
    }

    private void extendToConf_InCall(Message msg) {
        int callType = this.mSession.getCallProfile().getCallType();
        if (callType != msg.arg2) {
            Log.i("CallStateMachine", "[InCall] callType " + callType + " to callType " + msg.arg2);
            this.mSession.getCallProfile().setCallType(msg.arg2);
            this.mSession.getCallProfile().setConferenceCall(2);
        }
        this.mCsm.notifyOnModified(msg.arg2);
    }

    private void cancelTransfer_InCall() {
        if (this.mCsm.mTransferRequested) {
            Log.i("CallStateMachine", "[InCall] cancel call transfer");
            this.mCsm.notifyOnError(1119, "cancel call transfer");
            if (this.mVolteSvcIntf.cancelTransferCall(this.mSession.getSessionId()) < 0) {
                this.mCsm.notifyOnError(1121, "cancel call transfer fail", 0);
            }
            this.mCsm.notifyOnError(1120, "cancel call transfer success", 0);
            this.mCsm.mTransferRequested = false;
            return;
        }
        Log.e("CallStateMachine", "[InCall] call transfer is not requested, so ignore cancel transfer");
        this.mCsm.notifyOnError(1121, "cancel call transfer fail", 0);
    }

    private void referStatus_InCall(Message msg) {
        if (this.mCsm.mTransferRequested) {
            if (msg.arg1 == 200) {
                CallStateMachine callStateMachine = this.mCsm;
                callStateMachine.notifyOnError(1118, "call transfer success (" + msg.arg1 + ")");
            } else {
                CallStateMachine callStateMachine2 = this.mCsm;
                callStateMachine2.notifyOnError(1119, "call transfer failed (" + msg.arg1 + ")");
            }
            this.mCsm.mHoldBeforeTransfer = false;
            this.mCsm.mTransferRequested = false;
        }
    }

    private void modified_InCall(Message msg) {
        CallProfile callProfile;
        int modifiedCallType = msg.arg1;
        int orgCallType = msg.arg2;
        Log.i("CallStateMachine", "[InCall] modifiedCallType " + modifiedCallType + ", orgCallType " + orgCallType);
        if (modifiedCallType != orgCallType && (ImsCallUtil.isRttCall(modifiedCallType) || ImsCallUtil.isRttCall(orgCallType))) {
            this.mModule.onSendRttSessionModifyResponse(this.mSession.getCallId(), !ImsCallUtil.isRttCall(orgCallType) && ImsCallUtil.isRttCall(modifiedCallType), true);
        }
        String isFocus = this.mSession.getCallProfile().getIsFocus();
        if ((Mno.RJIL == this.mMno || Mno.ZAIN_KSA == this.mMno || Mno.AIRTEL == this.mMno || Mno.MTN_SOUTHAFRICA == this.mMno) && "1".equals(isFocus)) {
            this.mCsm.notifyOnResumed(false);
        } else {
            this.mCsm.notifyOnModified(modifiedCallType);
            if (!ImsCallUtil.isTtyCall(modifiedCallType)) {
                CallProfile modifiedProfile = new CallProfile();
                modifiedProfile.setCallType(modifiedCallType);
                modifiedProfile.getMediaProfile().setVideoQuality(this.mSession.getCallProfile().getMediaProfile().getVideoQuality());
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
        int cameraId = this.mCsm.determineCamera(this.mSession.getCallProfile().getCallType(), false);
        if (cameraId >= 0) {
            this.mSession.startCamera(cameraId);
        }
    }

    private int dbrLost_InCall(Message msg) {
        if (msg.arg1 != 2) {
            return -1;
        }
        if (this.mMno == Mno.CTC || this.mMno == Mno.CU || this.mMno == Mno.CTCMO) {
            Log.i("CallStateMachine", "[InCall] Downgrade Call due to Video Dedicated Bearer lost");
            if (!handleVideoDowngradeRequest()) {
                return -1;
            }
            this.mSession.notifyCallDowngraded();
            this.mCsm.transitionTo(this.mCsm.mModifyingCall);
            return 1;
        }
        CallProfile profile = this.mSession.getCallProfile();
        profile.setCallType(1);
        return handleUpdate(profile) ? 1 : 0;
    }

    private void camStartFailed_InCall() {
        if (this.mMno != Mno.DOCOMO && !this.mMno.isKor()) {
            if (downgradeVideoToVoiceRequest()) {
                Log.i("CallStateMachine", "[InCall] Downgrade Call due to StartCamera failed");
                IMSLog.c(LogClass.VOLTE_START_CAMERA_FAIL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mSession.notifyCallDowngraded();
                this.mCsm.transitionTo(this.mCsm.mModifyingCall);
            }
            this.mCsm.mIsStartCameraSuccess = true;
        }
    }

    private void reInvite_InCall() {
        this.mCsm.callType = this.mSession.getCallProfile().getCallType();
        this.mCsm.mReinvite = true;
        if ((!ImsCallUtil.isVideoCall(this.mCsm.callType) || this.mMno != Mno.ATT) && !this.mCsm.mRemoteHeld) {
            Log.i("CallStateMachine", "[InCall] send H/O Re-INVITE");
            this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", 0, "", new String[0]));
            return;
        }
        Log.i("CallStateMachine", "[InCall] calltype=" + this.mCsm.callType + ", ignore re-INVITE");
    }

    private void checkVideoDBR_InCall() {
        if (this.mSession.getDedicatedBearerState(2) == 3 && this.mSession.getDedicatedBearerState(8) == 3) {
            Log.i("CallStateMachine", "[InCall] Downgrade Call due to Video DBR is not opened");
            if (handleVideoDowngradeRequest()) {
                this.mSession.notifyCallDowngraded();
                this.mCsm.transitionTo(this.mCsm.mModifyingCall);
            }
        }
    }

    private boolean handleVideoDowngradeRequest() {
        Log.i("CallStateMachine", "[InCall] handleVideoDowngradeRequest: " + this.mCsm.getCurrentState().getName());
        if ((this.mSession.getCallProfile().getCallType() != 2 && this.mSession.getCallProfile().getCallType() != 4) || this.mCsm.mRemoteHeld) {
            return false;
        }
        Log.i("CallStateMachine", "handleVideoDowngradeRequest: trigger downgrade");
        CallProfile profile = new CallProfile();
        profile.setCallType(1);
        return this.mCsm.modifyCallType(profile, true);
    }

    private void notifyOnSessionUpdateRequested(int type, byte[] data) {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onSessionUpdateRequested(type, data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyCmcDtmfEvent(int dtmfKey) {
        int length = this.mListeners.beginBroadcast();
        Log.i("CallStateMachine", "[InCall] notifyCmcDtmfEvent: " + dtmfKey);
        for (int i = 0; i < length; i++) {
            IImsCallSessionEventListener listener = this.mListeners.getBroadcastItem(i);
            try {
                this.mSession.mCallProfile.setCmcDtmfKey(dtmfKey);
                listener.onProfileUpdated(this.mSession.mCallProfile.getMediaProfile(), this.mSession.mCallProfile.getMediaProfile());
                this.mSession.mCallProfile.setCmcDtmfKey(-1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: protected */
    public void notifyCmcInfoEvent(CmcInfoEvent cmcInfoEvent) {
        int length = this.mListeners.beginBroadcast();
        Log.i("CallStateMachine", "notifyCmcDtmfEvent: " + cmcInfoEvent.getExternalCallId() + ", recordEvent : " + cmcInfoEvent.getRecordEvent());
        for (int i = 0; i < length; i++) {
            IImsCallSessionEventListener listener = this.mListeners.getBroadcastItem(i);
            try {
                this.mSession.mCallProfile.setCmcRecordEvent(cmcInfoEvent.getRecordEvent());
                listener.onProfileUpdated(this.mSession.mCallProfile.getMediaProfile(), this.mSession.mCallProfile.getMediaProfile());
                this.mSession.mCallProfile.setCmcRecordEvent(-1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyRecordState(int state) {
        Log.i("CallStateMachine", "[InCall] notifyRecordState: " + state);
        this.mSession.mCallProfile.setRecordState(state);
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onProfileUpdated(this.mSession.mCallProfile.getMediaProfile(), this.mSession.mCallProfile.getMediaProfile());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }
}
