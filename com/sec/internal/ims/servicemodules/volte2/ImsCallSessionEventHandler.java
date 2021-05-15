package com.sec.internal.ims.servicemodules.volte2;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.volte2.data.ReferStatus;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsCallSessionEventHandler {
    private static final int TMO_POOR_VIDEO_TIMEOUT = 20000;
    /* access modifiers changed from: private */
    public String LOG_TAG = "ImsCallSessionEventHandler";
    private PreciseAlarmManager mAm = null;
    /* access modifiers changed from: private */
    public CallProfile mCallProfile = null;
    private IImsMediaController mMediaController = null;
    /* access modifiers changed from: private */
    public Mno mMno = Mno.DEFAULT;
    /* access modifiers changed from: private */
    public IVolteServiceModuleInternal mModule = null;
    private Message mPoorVideoTimeoutMessage = null;
    /* access modifiers changed from: private */
    public ImsRegistration mRegistration = null;
    /* access modifiers changed from: private */
    public IRegistrationManager mRegistrationManager = null;
    /* access modifiers changed from: private */
    public ImsCallSession mSession = null;
    /* access modifiers changed from: private */
    public IVolteServiceInterface mVolteSvcIntf = null;
    /* access modifiers changed from: private */
    public CallStateMachine smCallStateMachine = null;

    public ImsCallSessionEventHandler(ImsCallSession session, IVolteServiceModuleInternal volteModule, ImsRegistration reg, IRegistrationManager rm, Mno mno, PreciseAlarmManager am, CallStateMachine csm, CallProfile cp, IVolteServiceInterface ivsif, IImsMediaController mcl) {
        this.mSession = session;
        this.mModule = volteModule;
        this.mRegistration = reg;
        this.mRegistrationManager = rm;
        this.mMno = mno;
        this.mAm = am;
        this.smCallStateMachine = csm;
        this.mCallProfile = cp;
        this.mVolteSvcIntf = ivsif;
        this.mMediaController = mcl;
    }

    /* access modifiers changed from: protected */
    public void onImsCallEventHandler(CallStateEvent event) {
        if (event.getSessionID() == this.mSession.getSessionId()) {
            String str = this.LOG_TAG;
            Log.i(str, "onImsCallEventHandler, " + event);
            ImsCallEventHandler imsCallEventHandler = new ImsCallEventHandler(this, event, (AnonymousClass1) null);
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[event.getState().ordinal()]) {
                case 1:
                    imsCallEventHandler.handleRingingBack();
                    return;
                case 2:
                    imsCallEventHandler.handleCalling();
                    return;
                case 3:
                    this.smCallStateMachine.sendMessage(31);
                    return;
                case 4:
                    imsCallEventHandler.handleEstablished();
                    return;
                case 5:
                    imsCallEventHandler.handleRefreshFail();
                    return;
                case 6:
                    if (!this.mMno.isChn() || this.mSession.getCallState() != CallConstants.STATE.IncomingCall) {
                        this.smCallStateMachine.mConfCallAdded = true;
                        return;
                    }
                    return;
                case 7:
                    imsCallEventHandler.handleModified();
                    return;
                case 8:
                    imsCallEventHandler.handleHeldLocal();
                    return;
                case 9:
                    imsCallEventHandler.handleHeldRemote();
                    return;
                case 10:
                    imsCallEventHandler.handleHeldBoth();
                    return;
                case 11:
                    imsCallEventHandler.handleEnded();
                    return;
                case 12:
                    imsCallEventHandler.handleModifyRequested();
                    return;
                case 13:
                    imsCallEventHandler.handleEarlyMediaStart();
                    return;
                case 14:
                    imsCallEventHandler.handleError();
                    return;
                case 15:
                    this.mSession.updateCallProfile(event.getParams());
                    this.smCallStateMachine.sendMessage(35);
                    return;
                case 16:
                    this.mSession.updateCallProfile(event.getParams());
                    this.smCallStateMachine.sendMessage(36);
                    return;
                case 17:
                    imsCallEventHandler.handleExtendToConference();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onReferStatus(AsyncResult result) {
        ReferStatus rs = (ReferStatus) result.result;
        if (((ImsCallSession) result.userObj).mSessionId == rs.mSessionId) {
            String str = this.LOG_TAG;
            Log.i(str, "onReferStatus: respCode=" + rs.mRespCode);
            if (rs.mRespCode >= 200) {
                this.smCallStateMachine.sendMessage(75, rs.mRespCode, -1);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onImsMediaEvent(IMSMediaEvent event) {
        int callType = this.mCallProfile.getCallType();
        if (event.getSessionID() == this.mSession.getSessionId() || (this.mMno == Mno.SKT && callType == 6)) {
            String str = this.LOG_TAG;
            Log.i(str, "onImsMediaEvent: " + event.getState() + " phoneId: " + event.getPhoneId());
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[event.getState().ordinal()];
            if (i == 1) {
                onVideoHeld();
            } else if (i != 2) {
                switch (i) {
                    case 5:
                    case 6:
                        onVideoRtpTimeout();
                        return;
                    case 7:
                        onVideoQuality(false);
                        return;
                    case 8:
                    case 9:
                    case 10:
                        onVideoQuality(true);
                        return;
                    case 11:
                        this.smCallStateMachine.sendMessage(84);
                        return;
                    case 12:
                        this.smCallStateMachine.sendMessage(85);
                        return;
                    case 13:
                        this.smCallStateMachine.sendMessage(207);
                        return;
                    case 14:
                        this.smCallStateMachine.sendMessage(700, 1);
                        return;
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                        this.smCallStateMachine.sendMessage(700, 0);
                        return;
                    case 19:
                        if (this.mCallProfile.getRecordState() == 1) {
                            this.smCallStateMachine.sendMessage(700, 0);
                            this.mMediaController.stopRecord();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            } else {
                onVideoResumed();
            }
        }
    }

    private void onVideoHeld() {
        this.mCallProfile.getMediaProfile().setVideoPause(true);
        this.smCallStateMachine.sendMessage(82);
        if (this.mMno == Mno.TMOUS) {
            stopPoorVideoTimer();
        }
    }

    private class ImsCallEventHandler {
        final CallStateEvent mEvent;

        /* synthetic */ ImsCallEventHandler(ImsCallSessionEventHandler x0, CallStateEvent x1, AnonymousClass1 x2) {
            this(x1);
        }

        private ImsCallEventHandler(CallStateEvent event) {
            this.mEvent = event;
        }

        /* access modifiers changed from: private */
        public void handleRingingBack() {
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            if ((ImsCallSessionEventHandler.this.mSession.getVideoCrbtSupportType() & 1) == 1) {
                if (this.mEvent.getParams().getVideoCrbtType() == 0) {
                    ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(false);
                    ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent("");
                }
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT());
            }
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(34);
        }

        /* access modifiers changed from: private */
        public void handleCalling() {
            ImsCallSession session;
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(33);
            if (ImsCallSessionEventHandler.this.mCallProfile.getReplaceSipCallId() != null && (session = ImsCallSessionEventHandler.this.mModule.getSessionBySipCallId(ImsCallSessionEventHandler.this.mCallProfile.getReplaceSipCallId())) != null) {
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "replace UserAgent. replaceSessionId " + session.getSessionId() + " newSessionId " + ImsCallSessionEventHandler.this.mSession.mSessionId);
                ImsCallSessionEventHandler.this.mVolteSvcIntf.replaceUserAgent(session.getSessionId(), ImsCallSessionEventHandler.this.mSession.mSessionId);
            }
        }

        /* access modifiers changed from: private */
        public void handleEstablished() {
            String prevCodec = ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString();
            boolean bNeedtoNotifyAudioRxId = this.mEvent.getParams().getAudioRxTrackId() != ImsCallSessionEventHandler.this.mCallProfile.getAudioRxTrackId();
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            if (ImsCallSessionEventHandler.this.mRegistration != null && ImsCallSessionEventHandler.this.mRegistration.getImsProfile() != null && ImsCallSessionEventHandler.this.mRegistration.getImsProfile().getNotifyCodecOnEstablished() && !prevCodec.equals(ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString()) && ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.InCall) {
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "forceNotifyCurrentCodec, Codec =" + this.mEvent.getParams().getAudioCodec() + ", HdIcon: " + ImsCallSessionEventHandler.this.mCallProfile.getHDIcon());
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
            }
            if (bNeedtoNotifyAudioRxId && ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.InCall) {
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
                Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "notified audiorxtrackid");
            }
            if (ImsCallUtil.isVideoCall(ImsCallSessionEventHandler.this.mCallProfile.getCallType()) && !ImsCallUtil.isVideoCall(this.mEvent.getCallType())) {
                ImsCallSessionEventHandler.this.mCallProfile.setDowngradedVideoCall(true);
                ImsCallSessionEventHandler.this.mCallProfile.setDowngradedAtEstablish(true);
                ImsCallSessionEventHandler.this.mSession.setUserCameraOff(false);
                if ((ImsCallSessionEventHandler.this.mMno.isChn() || (ImsCallSessionEventHandler.this.mMno == Mno.TMOUS && !this.mEvent.getRemoteVideoCapa())) && ImsCallSessionEventHandler.this.mCallProfile.isMOCall()) {
                    ImsCallSessionEventHandler.this.mSession.notifyCallDowngraded();
                }
            }
            if (ImsCallSessionEventHandler.this.mRegistration == null || !ImsCallSessionEventHandler.this.mRegistration.getImsProfile().isSoftphoneEnabled() || ImsCallSessionEventHandler.this.mCallProfile.getCallType() != 13) {
                ImsCallSessionEventHandler.this.mCallProfile.setCallType(this.mEvent.getCallType());
                if (ImsCallUtil.isRttCall(this.mEvent.getCallType())) {
                    ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().setRttMode(1);
                } else {
                    ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().setRttMode(0);
                }
            } else {
                String access$16002 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$16002, "ATT Softphone : not change FROM  callType = " + ImsCallSessionEventHandler.this.mCallProfile.getCallType() + "TO  calltype =" + this.mEvent.getCallType());
            }
            if (ImsCallSessionEventHandler.this.mSession.isCmcPrimaryType(ImsCallSessionEventHandler.this.mSession.mCmcType) && !TextUtils.isEmpty(this.mEvent.getCmcDeviceId())) {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(this.mEvent.getCmcDeviceId());
            }
            if (ImsCallSessionEventHandler.this.mSession.isCmcSecondaryType(ImsCallSessionEventHandler.this.mSession.mCmcType) && !TextUtils.isEmpty(this.mEvent.getCmcCallTime())) {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcCallTime(this.mEvent.getCmcCallTime());
            }
            ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            ImsCallSessionEventHandler.this.smCallStateMachine.setVideoRtpPort(this.mEvent.getParams().getLocalVideoRTPPort(), this.mEvent.getParams().getLocalVideoRTCPPort(), this.mEvent.getParams().getRemoteVideoRTPPort(), this.mEvent.getParams().getRemoteVideoRTCPPort());
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(41, this.mEvent.getParams().getIndicationFlag());
        }

        /* access modifiers changed from: private */
        public void handleRefreshFail() {
            IRegistrationGovernor governor;
            SipError error = this.mEvent.getErrorCode();
            String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
            Log.i(access$1600, "REFRESHFAIL " + error.toString());
            if (ImsCallSessionEventHandler.this.mRegistration != null && (governor = ImsCallSessionEventHandler.this.mRegistrationManager.getRegistrationGovernor(ImsCallSessionEventHandler.this.mRegistration.getHandle())) != null) {
                governor.onSipError("mmtel", error);
            }
        }

        /* access modifiers changed from: private */
        public void handleModified() {
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            if (ImsCallSessionEventHandler.this.mSession.mModifyRequestedProfile == null) {
                Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "unexpected ImsCallEvent");
            } else if (this.mEvent.getErrorCode() == null || this.mEvent.getErrorCode().equals(SipErrorBase.OK)) {
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "Change calltype from " + this.mEvent.getCallType() + " to " + ImsCallSessionEventHandler.this.mSession.mModifyRequestedProfile.getCallType());
                this.mEvent.setCallType(ImsCallSessionEventHandler.this.mSession.mModifyRequestedProfile.getCallType());
            }
            ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            if (ImsCallUtil.isVideoCall(ImsCallSessionEventHandler.this.mCallProfile.getCallType()) && !ImsCallUtil.isVideoCall(this.mEvent.getCallType())) {
                Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "Call is downgrade");
                ImsCallSessionEventHandler.this.mSession.stopCamera();
                ImsCallSessionEventHandler.this.mCallProfile.setDowngradedVideoCall(true);
                ImsCallSessionEventHandler.this.mSession.setUserCameraOff(false);
            } else if (!ImsCallUtil.isVideoCall(ImsCallSessionEventHandler.this.mCallProfile.getCallType()) && ImsCallUtil.isVideoCall(this.mEvent.getCallType())) {
                Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "Call is upgrade");
                ImsCallSessionEventHandler.this.mCallProfile.setDowngradedVideoCall(false);
            }
            ImsCallSessionEventHandler.this.mCallProfile.setDowngradedAtEstablish(false);
            int orgCallType = ImsCallSessionEventHandler.this.mCallProfile.getCallType();
            ImsCallSessionEventHandler.this.mCallProfile.setCallType(this.mEvent.getCallType());
            ImsCallSessionEventHandler.this.smCallStateMachine.setVideoRtpPort(this.mEvent.getParams().getLocalVideoRTPPort(), this.mEvent.getParams().getLocalVideoRTCPPort(), this.mEvent.getParams().getRemoteVideoRTPPort(), this.mEvent.getParams().getRemoteVideoRTCPPort());
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(91, this.mEvent.getCallType(), orgCallType);
        }

        /* access modifiers changed from: private */
        public void handleHeldLocal() {
            String prevCodec = ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString();
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            if (ImsCallSessionEventHandler.this.mMno.isChn() || ImsCallSessionEventHandler.this.mMno.isHkMo() || ImsCallSessionEventHandler.this.mMno.isKor() || ImsCallSessionEventHandler.this.mMno.isJpn() || ImsCallSessionEventHandler.this.mMno == Mno.RJIL || ImsCallSessionEventHandler.this.mMno == Mno.TELSTRA) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            if ((ImsCallSessionEventHandler.this.mMno == Mno.DOCOMO || ImsCallSessionEventHandler.this.mMno == Mno.TWM) && !prevCodec.equals(ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString())) {
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "forceNotifyCurrentCodec, Codec =" + this.mEvent.getParams().getAudioCodec() + ", HdIcon: " + ImsCallSessionEventHandler.this.mCallProfile.getHDIcon());
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
            }
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(61);
        }

        /* access modifiers changed from: private */
        public void handleHeldRemote() {
            if (ImsCallSessionEventHandler.this.mMno.isChn() || ImsCallSessionEventHandler.this.mMno.isHkMo() || ImsCallSessionEventHandler.this.mMno.isKor() || ImsCallSessionEventHandler.this.mMno.isJpn() || ImsCallSessionEventHandler.this.mMno == Mno.ATT || ImsCallSessionEventHandler.this.mMno == Mno.RJIL || ImsCallSessionEventHandler.this.mMno == Mno.TELSTRA) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            if (ImsCallSessionEventHandler.this.mMno == Mno.MOVISTAR_PERU || ImsCallSessionEventHandler.this.mMno == Mno.TWM) {
                String prevCodec = ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString();
                ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
                if (!prevCodec.equals(ImsCallSessionEventHandler.this.mCallProfile.getMediaProfile().getAudioCodec().toString())) {
                    String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.i(access$1600, "forceNotifyCurrentCodec, Codec =" + this.mEvent.getParams().getAudioCodec() + ", HdIcon: " + ImsCallSessionEventHandler.this.mCallProfile.getHDIcon());
                    ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
                }
            }
            ImsCallSessionEventHandler.this.mSession.mOldLocalHoldTone = ImsCallSessionEventHandler.this.mSession.mLocalHoldTone;
            ImsCallSessionEventHandler.this.mSession.mLocalHoldTone = this.mEvent.getParams().getLocalHoldTone();
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(62);
        }

        /* access modifiers changed from: private */
        public void handleHeldBoth() {
            if (ImsCallSessionEventHandler.this.mMno.isChn() || ImsCallSessionEventHandler.this.mMno.isHkMo() || ImsCallSessionEventHandler.this.mMno.isKor() || ImsCallSessionEventHandler.this.mMno.isJpn() || ImsCallSessionEventHandler.this.mMno == Mno.RJIL || ImsCallSessionEventHandler.this.mMno == Mno.TELSTRA) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            ImsCallSessionEventHandler.this.mSession.mOldLocalHoldTone = ImsCallSessionEventHandler.this.mSession.mLocalHoldTone;
            ImsCallSessionEventHandler.this.mSession.mLocalHoldTone = this.mEvent.getParams().getLocalHoldTone();
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(63);
        }

        /* access modifiers changed from: private */
        public void handleEnded() {
            if ((ImsCallSessionEventHandler.this.mSession.getVideoCrbtSupportType() & 1) == 1) {
                ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(false);
                ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent("");
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT());
            }
            SipError error = this.mEvent.getErrorCode();
            if (error == null) {
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(3);
                return;
            }
            if (!ImsCallSessionEventHandler.this.mSession.isCmcSecondaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) || !"MDMN_PULL_BY_PRIMARY".equals(error.getReason())) {
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(3, error.getCode(), -1, error.getReason());
            } else {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(ImsRegistry.getCmcAccountManager().getCurrentLineOwnerDeviceId());
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(3, error.getCode(), 6007, error.getReason());
            }
            if (ImsCallSessionEventHandler.this.mSession.isCmcSecondaryType(ImsCallSessionEventHandler.this.mSession.getCmcType()) && "MDMN_PULL_BY_SECONDARY".equals(error.getReason())) {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(this.mEvent.getCmcDeviceId());
            }
        }

        /* access modifiers changed from: private */
        public void handleModifyRequested() {
            int eventCallType = this.mEvent.getCallType();
            boolean isSdToSdPull = this.mEvent.getIsSdToSdPull();
            if (!ImsCallSessionEventHandler.this.mCallProfile.hasRemoteVideoCapa() && ImsCallSessionEventHandler.this.mModule.isCallServiceAvailable(ImsCallSessionEventHandler.this.mSession.mPhoneId, "mmtel-video") && ImsCallUtil.isVideoCall(eventCallType)) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(true);
                ImsCallSessionEventHandler.this.mSession.forceNotifyCurrentCodec();
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessageDelayed(55, eventCallType, 0, (Object) null, 100);
            } else if (!ImsCallSessionEventHandler.this.mSession.isCmcPrimaryType(ImsCallSessionEventHandler.this.mSession.mCmcType) || !isSdToSdPull) {
                ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(55, eventCallType, 0, (Object) null);
            } else {
                modifyCallTypeForPull();
            }
        }

        /* access modifiers changed from: private */
        public void handleEarlyMediaStart() {
            ImsCallSessionEventHandler.this.mSession.updateCallProfile(this.mEvent.getParams());
            boolean isVideoCrbt = false;
            String dtmfEvent = this.mEvent.getParams().getDtmfEvent();
            ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            if (this.mEvent.getParams().getVideoCrbtType() > 0) {
                isVideoCrbt = true;
                ImsCallSessionEventHandler.this.mCallProfile.setVideoCrbtValid(true);
            }
            ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(isVideoCrbt);
            ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent(dtmfEvent);
            String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
            Log.i(access$1600, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT() + ", dtmfEvent : " + dtmfEvent);
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(32, this.mEvent.getErrorCode().getCode());
        }

        /* access modifiers changed from: private */
        public void handleError() {
            IRegistrationGovernor governor;
            SipError error = this.mEvent.getErrorCode();
            int retryAfter = this.mEvent.getRetryAfter();
            IRegistrationGovernor.CallEvent callEvent = null;
            if (this.mEvent.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.INITIAL_REGISTRATION) {
                callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_INITIAL_REGI;
            } else if (this.mEvent.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY_REGISTRATION) {
                callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY_REGI;
            } else if (this.mEvent.getAlternativeService() == CallStateEvent.ALTERNATIVE_SERVICE.EMERGENCY) {
                callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY;
            }
            if ((ImsCallSessionEventHandler.this.mSession.getVideoCrbtSupportType() & 1) == 1) {
                ImsCallSessionEventHandler.this.mCallProfile.setVideoCRBT(false);
                ImsCallSessionEventHandler.this.mCallProfile.setDtmfEvent("");
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "isVideoCRBT : " + ImsCallSessionEventHandler.this.mCallProfile.isVideoCRBT());
            }
            if (!(ImsCallSessionEventHandler.this.mRegistration == null || (governor = ImsCallSessionEventHandler.this.mRegistrationManager.getRegistrationGovernor(ImsCallSessionEventHandler.this.mRegistration.getHandle())) == null)) {
                error = callEvent != null ? handleErrorOnCallEvent(error, callEvent, governor) : handleErrorOnNullEvent(error, governor);
            }
            handleErrorOnNullRegistration(error, callEvent);
            if (ImsCallSessionEventHandler.this.mSession.isCmcPrimaryType(ImsCallSessionEventHandler.this.mSession.mCmcType) && this.mEvent.getCmcDeviceId() != null && !this.mEvent.getCmcDeviceId().isEmpty()) {
                ImsCallSessionEventHandler.this.mCallProfile.setCmcDeviceId(this.mEvent.getCmcDeviceId());
            }
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(4, retryAfter, -1, error);
        }

        private SipError handleErrorOnCallEvent(SipError error, IRegistrationGovernor.CallEvent callEvent, IRegistrationGovernor governor) {
            if (ImsCallSessionEventHandler.this.mMno == Mno.CMCC) {
                governor.onCallStatus(callEvent, error, ImsCallSessionEventHandler.this.mCallProfile.getCallType());
                if (!SipErrorBase.ALTERNATIVE_SERVICE.equals(error)) {
                    return error;
                }
                String type = this.mEvent.getAlternativeServiceType();
                String reason = this.mEvent.getAlternativeServiceReason();
                String serviceUrn = this.mEvent.getAlternativeServiceUrn();
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "handleErrorOnCallEvent: type : " + type + ", reason : " + reason + ", serviceUrn : " + serviceUrn);
                String access$16002 = ImsCallSessionEventHandler.this.LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("handleErrorOnCallEvent: phoenId : ");
                sb.append(ImsCallSessionEventHandler.this.mSession.mPhoneId);
                sb.append(", callEvent : ");
                sb.append(callEvent);
                Log.i(access$16002, sb.toString());
                if (ImsRegistry.getPdnController().getEmcBsIndication(ImsCallSessionEventHandler.this.mSession.mPhoneId) == EmcBsIndication.SUPPORTED && callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY_REGI) {
                    SipError error2 = SipErrorBase.ALTERNATIVE_SERVICE_EMERGENCY;
                    if (TextUtils.isEmpty(serviceUrn)) {
                        error2.setReason(ImsCallUtil.ECC_SERVICE_URN_DEFAULT);
                        return error2;
                    }
                    error2.setReason(serviceUrn);
                    return error2;
                } else if (TextUtils.isEmpty(serviceUrn)) {
                    SipError error3 = SipErrorBase.ALTERNATIVE_SERVICE;
                    error3.setReason("");
                    return error3;
                } else if (ImsCallUtil.convertUrnToEccCat(serviceUrn) == 254) {
                    SipError error4 = SipErrorBase.ALTERNATIVE_SERVICE;
                    error4.setReason(serviceUrn);
                    return error4;
                } else {
                    SipError error5 = SipErrorBase.ALTERNATIVE_SERVICE_EMERGENCY_CSFB;
                    error5.setReason(serviceUrn);
                    return error5;
                }
            } else {
                governor.onCallStatus(callEvent, error, ImsCallSessionEventHandler.this.mCallProfile.getCallType());
                return ImsCallUtil.onConvertSipErrorReason(this.mEvent);
            }
        }

        private SipError handleErrorOnNullEvent(SipError error, IRegistrationGovernor governor) {
            if (ImsCallSessionEventHandler.this.mMno == Mno.CMCC) {
                return governor.onSipError("mmtel", error);
            }
            if ((ImsCallSessionEventHandler.this.smCallStateMachine.mReinvite || ImsCallSessionEventHandler.this.smCallStateMachine.mConfCallAdded) && ImsCallSessionEventHandler.this.mMno == Mno.KDDI) {
                Log.e(ImsCallSessionEventHandler.this.LOG_TAG, "Don't send Register for reINVITE's transaction timeout");
                return error;
            } else if (ImsCallSessionEventHandler.this.mMno == Mno.USCC && ((ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.AlertingCall || ImsCallSessionEventHandler.this.mSession.getCallState() == CallConstants.STATE.EndingCall) && error.getCode() == 408)) {
                Log.e(ImsCallSessionEventHandler.this.LOG_TAG, "USCC - Don't re-REGISTER for 408 if it is received after 180");
                return error;
            } else if (ImsCallSessionEventHandler.this.mMno != Mno.SPRINT || ImsCallSessionEventHandler.this.mSession.getCallState() != CallConstants.STATE.ModifyingCall) {
                return governor.onSipError("mmtel", error);
            } else {
                Log.e(ImsCallSessionEventHandler.this.LOG_TAG, "Don't deregister for Re-Invite failures");
                return error;
            }
        }

        private void handleErrorOnNullRegistration(SipError error, IRegistrationGovernor.CallEvent callEvent) {
            int retryAfter = this.mEvent.getRetryAfter();
            if (ImsCallSessionEventHandler.this.mMno != Mno.CMCC) {
                if (ImsCallSessionEventHandler.this.mMno == Mno.KDDI && retryAfter > 0) {
                    String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.e(access$1600, "KDDI : INVITE retry should happen after " + retryAfter + " seconds");
                    ImsCallSessionEventHandler.this.smCallStateMachine.setRetryInprogress(true);
                }
                if (callEvent != null && !DeviceUtil.getGcfMode()) {
                    handleErrorSetCodeReason(error, callEvent);
                }
            }
        }

        private void handleErrorSetCodeReason(SipError error, IRegistrationGovernor.CallEvent callEvent) {
            if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY_REGI) {
                if (ImsCallSessionEventHandler.this.mMno == Mno.STARHUB || ImsCallSessionEventHandler.this.mMno == Mno.CU) {
                    error.setCode(380);
                } else if (ImsCallSessionEventHandler.this.mMno != Mno.SPRINT || !this.mEvent.getAlternativeServiceReason().equals("VoIP emergency not available!")) {
                    error.setCode(381);
                } else {
                    error.setCode(382);
                }
                if (ImsCallSessionEventHandler.this.mMno == Mno.DOCOMO || ImsCallSessionEventHandler.this.mMno == Mno.KDDI || ImsCallSessionEventHandler.this.mMno.isEur() || ImsCallSessionEventHandler.this.mMno == Mno.MOBILEONE) {
                    Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "need to carry service urn info for e911");
                    error.setReason(this.mEvent.getAlternativeServiceUrn());
                } else {
                    error.setReason("");
                }
                String access$1600 = ImsCallSessionEventHandler.this.LOG_TAG;
                Log.i(access$1600, "convert error " + error.getCode() + " " + error.getReason());
            } else if (callEvent == IRegistrationGovernor.CallEvent.EVENT_CALL_ALT_SERVICE_EMERGENCY) {
                if (ImsCallSessionEventHandler.this.mRegistration != null && ImsCallSessionEventHandler.this.mRegistration.getImsProfile().getEcallCsfbWithoutActionTag() && !TextUtils.isEmpty(this.mEvent.getAlternativeServiceUrn())) {
                    error.setCode(381);
                    error.setReason(this.mEvent.getAlternativeServiceUrn());
                    String access$16002 = ImsCallSessionEventHandler.this.LOG_TAG;
                    Log.i(access$16002, "convert error " + error.getCode() + " " + error.getReason());
                }
            }
        }

        /* access modifiers changed from: private */
        public void handleExtendToConference() {
            SipError error = this.mEvent.getErrorCode();
            if (ImsCallSessionEventHandler.this.mMno.isKor()) {
                ImsCallSessionEventHandler.this.mCallProfile.setRemoteVideoCapa(this.mEvent.getRemoteVideoCapa());
            }
            ImsCallSessionEventHandler.this.smCallStateMachine.sendMessage(74, error.getCode(), this.mEvent.getCallType());
        }

        private void modifyCallTypeForPull() {
            ImsCallSession boundedSession;
            Log.i(ImsCallSessionEventHandler.this.LOG_TAG, "modifyCallType for SD to SD pull");
            int currentCallType = ImsCallSessionEventHandler.this.mCallProfile.getCallType();
            int boundSessionId = ImsCallSessionEventHandler.this.mCallProfile.getCmcBoundSessionId();
            if (boundSessionId > 0) {
                boundedSession = ImsCallSessionEventHandler.this.mModule.getSession(boundSessionId);
            } else {
                boundedSession = null;
            }
            ImsCallSessionEventHandler.this.mVolteSvcIntf.replyModifyCallType(ImsCallSessionEventHandler.this.mSession.getSessionId(), currentCallType, currentCallType, currentCallType, ImsCallSessionEventHandler.this.smCallStateMachine.calculateCmcCallTime(boundedSession, (String) null));
        }
    }

    private void onVideoResumed() {
        this.mCallProfile.getMediaProfile().setVideoPause(false);
        this.smCallStateMachine.sendMessage(83);
    }

    private void onVideoRtpTimeout() {
        if (this.mMno == Mno.ATT || this.mMno == Mno.TMOUS || this.mMno == Mno.CTC || this.mMno == Mno.CTCMO || this.mMno == Mno.DIGI || this.mMno == Mno.RJIL) {
            this.smCallStateMachine.sendMessage(206);
        }
    }

    private void onVideoQuality(boolean quality) {
        if (this.mMno != Mno.TMOUS) {
            return;
        }
        if (quality) {
            stopPoorVideoTimer();
        } else {
            startPoorVideoTimer(20000);
        }
    }

    private void startPoorVideoTimer(long millis) {
        String str = this.LOG_TAG;
        Log.i(str, "startPoorVideoTimer: " + millis);
        stopPoorVideoTimer();
        this.mPoorVideoTimeoutMessage = this.smCallStateMachine.obtainMessage(205);
        this.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mPoorVideoTimeoutMessage, millis);
    }

    private void stopPoorVideoTimer() {
        if (this.mPoorVideoTimeoutMessage != null) {
            Log.i(this.LOG_TAG, "stopPoorVidoeTimer");
            this.mAm.removeMessage(this.mPoorVideoTimeoutMessage);
            this.mPoorVideoTimeoutMessage = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onUssdEvent(UssdEvent event) {
        if (event.getSessionID() == this.mSession.getSessionId()) {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE[event.getState().ordinal()];
            if (i == 1) {
                this.smCallStateMachine.sendMessage(93, (Object) event.getErrorCode());
            } else if (i == 2) {
                Bundle bundle = new Bundle();
                bundle.putInt("status", event.getStatus());
                bundle.putInt("dcs", event.getDCS());
                bundle.putByteArray("data", event.getData());
                this.smCallStateMachine.sendMessage(94, (Object) bundle);
            } else if (i == 3) {
                this.smCallStateMachine.sendMessage(4, -1, -1, event.getErrorCode());
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.ImsCallSessionEventHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE;

        static {
            int[] iArr = new int[UssdEvent.USSD_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE = iArr;
            try {
                iArr[UssdEvent.USSD_STATE.USSD_RESPONSE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE[UssdEvent.USSD_STATE.USSD_INDICATION.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$UssdEvent$USSD_STATE[UssdEvent.USSD_STATE.USSD_ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            int[] iArr2 = new int[IMSMediaEvent.MEDIA_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE = iArr2;
            try {
                iArr2[IMSMediaEvent.MEDIA_STATE.VIDEO_HELD.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_RESUMED.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_FIRST_FRAME_READY.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_AVAILABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_RTP_TIMEOUT.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_RTCP_TIMEOUT.ordinal()] = 6;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_VERYPOOR_QUALITY.ordinal()] = 7;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_FAIR_QUALITY.ordinal()] = 8;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_GOOD_QUALITY.ordinal()] = 9;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_POOR_QUALITY.ordinal()] = 10;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_HOLD_FAILED.ordinal()] = 11;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.VIDEO_RESUME_FAILED.ordinal()] = 12;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.CAMERA_START_FAIL.ordinal()] = 13;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_START_SUCCESS.ordinal()] = 14;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE.ordinal()] = 15;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_START_FAILURE_NO_SPACE.ordinal()] = 16;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_STOP_SUCCESS.ordinal()] = 17;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_STOP_FAILURE.ordinal()] = 18;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$IMSMediaEvent$MEDIA_STATE[IMSMediaEvent.MEDIA_STATE.RECORD_STOP_NO_SPACE.ordinal()] = 19;
            } catch (NoSuchFieldError e22) {
            }
            int[] iArr3 = new int[CallStateEvent.CALL_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = iArr3;
            try {
                iArr3[CallStateEvent.CALL_STATE.RINGING_BACK.ordinal()] = 1;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CALLING.ordinal()] = 2;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.TRYING.ordinal()] = 3;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ESTABLISHED.ordinal()] = 4;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.REFRESHFAIL.ordinal()] = 5;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_ADDED.ordinal()] = 6;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.MODIFIED.ordinal()] = 7;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_LOCAL.ordinal()] = 8;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_REMOTE.ordinal()] = 9;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.HELD_BOTH.ordinal()] = 10;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ENDED.ordinal()] = 11;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.MODIFY_REQUESTED.ordinal()] = 12;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.EARLY_MEDIA_START.ordinal()] = 13;
            } catch (NoSuchFieldError e35) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ERROR.ordinal()] = 14;
            } catch (NoSuchFieldError e36) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.SESSIONPROGRESS.ordinal()] = 15;
            } catch (NoSuchFieldError e37) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.FORWARDED.ordinal()] = 16;
            } catch (NoSuchFieldError e38) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.EXTEND_TO_CONFERENCE.ordinal()] = 17;
            } catch (NoSuchFieldError e39) {
            }
        }
    }
}
