package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class ImsEndingCall extends CallState {
    private long mCallDulation = 0;

    ImsEndingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        Log.i("CallStateMachine", "Enter [EndingCall], errorCode=" + this.mCsm.errorCode);
        if (this.mCsm.needToLogForATTGate(this.mSession.getCallProfile().getCallType())) {
            IMSLog.g("GATE", "<GATE-M>DISCONNECT_VIDEO_CALL</GATE-M>");
        }
        if (this.mCsm.errorCode == -1) {
            this.mCsm.errorCode = 200;
            this.mCsm.errorMessage = "";
        }
        this.mCsm.stopNetworkStatsOnPorts();
        this.mMediaController.stopEmoji(this.mSession.getSessionId());
        if (this.mSession.getUsingCamera() || (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType()) && this.mMno.isChn())) {
            if (!this.mMno.isKor() && this.mMno != Mno.DOCOMO) {
                Log.i("CallStateMachine", "stopCamera in EndingCall state");
                this.mSession.stopCamera();
            }
            this.mMediaController.resetCameraId();
        }
        this.mMediaController.unregisterForMediaEvent(this.mSession);
        if ((this.mRegistration != null && this.mRegistration.getImsProfile().isSoftphoneEnabled() && this.mSession.getCallProfile().getCallType() == 13) || this.mSession.getCallProfile().getCallType() == 7 || this.mSession.getCallProfile().getCallType() == 8) {
            Log.i("CallStateMachine", "[EndingCall] E911 Call end - restore User location settings");
            IGeolocationController geolocationCon = ImsRegistry.getGeolocationController();
            if (geolocationCon != null && this.mCsm.mRequestLocation) {
                geolocationCon.stopGeolocationUpdate();
                this.mCsm.mRequestLocation = false;
            }
            if (!this.mModule.getSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.HeldCall).isEmpty() && this.mRegistration != null && !this.mSession.isCmcPrimaryType(this.mSession.getCmcType())) {
                Log.i("CallStateMachine", "bindToNetwork for Normal call");
                this.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
            }
        }
        handleCallLoggingOnEndingCall();
        ImsRegistry.getImsDiagMonitor().notifyCallStatus(this.mSession.getSessionId(), "CALL_ENDED", this.mSession.getCallProfile().getCallType(), this.mSession.getCallProfile().getMediaProfile().getAudioCodec().toString());
        if (this.mSession.getCallProfile().getRejectCause() == 0) {
            Log.i("CallStateMachine", "[EndingCall] start EndCall timer (5 sec).");
            this.mCsm.sendMessageDelayed(2, 5000);
            return;
        }
        this.mCsm.sendMessage(2);
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[EndingCall] processMessage " + msg.what);
        int i = msg.what;
        if (i == 1) {
            terminate_EndingCall(msg);
        } else if (i == 2) {
            terminated_EndingCall(msg);
        } else if (i == 3) {
            ended_EndingCall(msg);
        } else if (i == 4) {
            error_EndingCall(msg);
        } else if (i != 55) {
            Log.e("CallStateMachine", getName() + " msg:" + msg.what + " ignored !!!");
        } else {
            switchRequest_EndingCall(msg);
        }
        return true;
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }

    private void terminate_EndingCall(Message msg) {
        if ((msg.arg1 < 0 ? 5 : msg.arg1) == 5) {
            this.mSession.setEndType(1);
            this.mCsm.errorCode = 200;
            this.mCsm.notifyOnEnded(this.mCsm.errorCode);
        }
    }

    private void ended_EndingCall(Message msg) {
        if (this.mCsm.mCameraUsedAtOtherApp) {
            this.mCsm.mCameraUsedAtOtherApp = false;
            this.mCsm.sendMessageDelayed(3, 500);
            return;
        }
        this.mCsm.srvccStarted = false;
        if (this.mCsm.mConfCallAdded) {
            Log.i("CallStateMachine", "Call end by Join to conference session");
            this.mCsm.mConfCallAdded = false;
            this.mSession.setEndType(1);
            this.mSession.setEndReason(7);
        } else if (this.mCsm.mRetryInprogress) {
            Log.i("CallStateMachine", "ImsTelePhonyService is handling retry!!");
            this.mCsm.mRetryInprogress = false;
        } else {
            if ((this.mMno.isChn() || this.mMno.isKor() || this.mMno.isJpn()) && this.mModule.getSessionCount(this.mSession.getPhoneId()) == 2) {
                ImsCallSession session = this.mModule.getForegroundSession(this.mSession.getPhoneId());
                if (session != null) {
                    CallProfile cp = session.getCallProfile();
                    Log.i("CallStateMachine", "setRemoteVideoCapa() : " + cp.getModifyHeader());
                    if (CloudMessageProviderContract.JsonData.TRUE.equals(cp.getModifyHeader())) {
                        session.getCallProfile().setRemoteVideoCapa(true);
                    } else {
                        session.getCallProfile().setRemoteVideoCapa(false);
                    }
                    session.forceNotifyCurrentCodec();
                } else {
                    Log.i("CallStateMachine", "getForegroundSessionn is NULL");
                }
            }
            onErrorCode(this.mCsm.errorCode);
            if (this.mCsm.mIsCmcHandover) {
                Log.i("CallStateMachine", "do not notifyOnEnded because it is created for cmc handover");
                ImsCallSession session2 = this.mModule.getSessionBySipCallId(this.mSession.getCallProfile().getReplaceSipCallId());
                if (session2 != null) {
                    session2.replaceSipCallId(this.mSession.getCallProfile().getSipCallId());
                } else {
                    Log.i("CallStateMachine", "replace session is null");
                }
            } else {
                this.mCsm.notifyOnEnded(this.mCsm.errorCode);
            }
        }
        this.mModule.onCallEnded(this.mSession.getPhoneId(), this.mSession.getSessionId(), msg.arg2);
        this.mCsm.removeMessages(2);
        if (this.mSession.mKaSender != null) {
            this.mSession.mKaSender.stop();
        }
        this.mCsm.quit();
    }

    private void switchRequest_EndingCall(Message msg) {
        Log.i("CallStateMachine", "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
        }
    }

    private void terminated_EndingCall(Message msg) {
        if (this.mCsm.mConfCallAdded) {
            Log.i("CallStateMachine", "Call end by Join to conference session");
            this.mCsm.mConfCallAdded = false;
            this.mSession.setEndType(1);
            this.mSession.setEndReason(7);
        } else if (this.mCsm.mRetryInprogress) {
            Log.i("CallStateMachine", "ImsTelePhonyService is handling retry!!");
            this.mCsm.mRetryInprogress = false;
        } else {
            onErrorCode(this.mCsm.errorCode);
            this.mCsm.notifyOnEnded(this.mCsm.errorCode);
        }
        this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(5);
        if ((this.mMno == Mno.DOCOMO || this.mMno == Mno.KDDI) && this.mCsm.errorCode == 709) {
            this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(25);
        }
        if (this.mVolteSvcIntf.endCall(this.mSession.getSessionId(), this.mCsm.callType, this.mCsm.sipReason) < 0) {
            Log.i("CallStateMachine", "[EndingCall] endCall failed but call terminated");
        }
        this.mModule.onCallEnded(this.mSession.getPhoneId(), this.mSession.getSessionId(), msg.arg2);
        Log.i("CallStateMachine", "[EndingCall] timeout. force to exit.");
        if (this.mSession.mKaSender != null) {
            this.mSession.mKaSender.stop();
        }
        this.mCsm.quit();
    }

    private void error_EndingCall(Message msg) {
        SipError err = (SipError) msg.obj;
        Log.i("CallStateMachine", "[EndingCall] err: " + err.getCode() + ", errorCode: " + this.mCsm.errorCode);
        if (this.mCsm.errorCode != 2414) {
            this.mCsm.errorCode = err.getCode();
        }
        if (this.mMno == Mno.KDDI && this.mModule.getSessionCount(this.mSession.getPhoneId()) > 1 && this.mCsm.errorCode == 709) {
            this.mCsm.notifyOnError(503, "Session Progress Timeout", 0);
        }
    }

    private void onErrorCode(int errorCode) {
        if (this.mCsm.mIsBigDataEndReason) {
            if (this.mSession.getDRBLost() && errorCode >= 5000) {
                if (errorCode >= 6000) {
                    errorCode += 200;
                } else {
                    errorCode += Id.REQUEST_SIP_DIALOG_SEND_SIP;
                }
            }
            this.mSession.setDRBLost(false);
            this.mCsm.notifyOnError(errorCode, this.mCsm.errorMessage);
        }
        if ((this.mCsm.mIsBigDataEndReason || errorCode >= 5000) && !this.mModule.isCsfbErrorCode(this.mSession.getPhoneId(), this.mSession.getCallProfile().getCallType(), new SipError(errorCode, this.mCsm.errorMessage))) {
            this.mModule.sendQualityStatisticsEvent();
        }
    }

    /* access modifiers changed from: protected */
    public void handleCallLoggingOnEndingCall() {
        if (isCallDrop(this.mCsm.errorCode)) {
            sendPSDropInfo(this.mModule.getPSDataDetails(this.mSession.getPhoneId()));
        }
        sendPSDailyInfo();
        if (this.mCsm.lazerErrorCode == -1) {
            this.mCsm.lazerErrorCode = this.mCsm.errorCode;
        }
        if (TextUtils.isEmpty(this.mCsm.lazerErrorMessage)) {
            this.mCsm.lazerErrorMessage = this.mCsm.errorMessage;
        }
        if (this.mSession.getCallProfile().hasCSFBError()) {
            IMSLog.LAZER_TYPE lazer_type = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type, this.mSession.getCallId() + " - RETRY OVER CS");
        } else if (!isCallDrop(this.mCsm.errorCode)) {
            IMSLog.LAZER_TYPE lazer_type2 = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type2, this.mSession.getCallId() + " - END");
        } else if (this.mCsm.getPreviousState() == this.mCsm.mOutgoingCall || this.mCsm.getPreviousState() == this.mCsm.mAlertingCall || (this.mCsm.getPreviousState() == this.mCsm.mReadyToCall && this.mSession.getCallProfile().isMOCall())) {
            IMSLog.LAZER_TYPE lazer_type3 = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type3, this.mSession.getCallId() + " - OUTGOING FAIL");
        } else if (this.mCsm.getPreviousState() == this.mCsm.mIncomingCall || (this.mCsm.getPreviousState() == this.mCsm.mReadyToCall && this.mSession.getCallProfile().isMTCall())) {
            IMSLog.LAZER_TYPE lazer_type4 = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type4, this.mSession.getCallId() + " - RECEIVE FAIL");
        } else {
            IMSLog.LAZER_TYPE lazer_type5 = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type5, this.mSession.getCallId() + " - DROP");
        }
        IMSLog.LAZER_TYPE lazer_type6 = IMSLog.LAZER_TYPE.CALL;
        IMSLog.lazer(lazer_type6, this.mSession.getCallId() + " - SIP REASON : " + this.mSession.getErrorMessage() + "(" + this.mSession.getErrorCode() + ")");
        IMSLog.LAZER_TYPE lazer_type7 = IMSLog.LAZER_TYPE.CALL;
        IMSLog.lazer(lazer_type7, this.mSession.getCallId() + " - INTERNAL REASON : " + this.mCsm.lazerErrorMessage + "(" + this.mCsm.lazerErrorCode + ")");
        if (this.mSession.getEndType() == 3) {
            IMSLog.LAZER_TYPE lazer_type8 = IMSLog.LAZER_TYPE.CALL;
            IMSLog.lazer(lazer_type8, this.mSession.getCallId() + " - LOCAL RELEASE");
        }
    }

    /* access modifiers changed from: protected */
    public void sendPSDropInfo(ContentValues psItem) {
        this.mCsm.mSIPFlowInfo = this.mCsm.mSIPFlowInfo.length() > 30 ? this.mCsm.mSIPFlowInfo.substring(this.mCsm.mSIPFlowInfo.length() - 30, this.mCsm.mSIPFlowInfo.length()) : this.mCsm.mSIPFlowInfo;
        this.mCallDulation = (SystemClock.elapsedRealtime() - this.mCsm.mCallInitTime) / 1000;
        psItem.put(DiagnosisConstants.PSCI_KEY_SIP_FLOW, this.mCsm.mSIPFlowInfo);
        psItem.put("MOMT", Integer.valueOf(this.mSession.getCallProfile().isMOCall() ? 1 : 0));
        psItem.put("TYPE", Integer.valueOf(this.mSession.getCallProfile().getCallType()));
        if (this.mSession.getCallProfile().isConferenceCall()) {
            if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                psItem.put("TYPE", 6);
            } else {
                psItem.put("TYPE", 5);
            }
        }
        psItem.put(DiagnosisConstants.PSCI_KEY_CALL_STATE, Integer.valueOf(this.mCsm.getPreviousStateByName(this.mCsm.getPreviousState().getName()).ordinal()));
        long j = this.mCallDulation;
        psItem.put(DiagnosisConstants.PSCI_KEY_CALL_TIME, Integer.valueOf(j > 999999 ? DiagnosisConstants.MAX_INT : (int) j));
        psItem.put(DiagnosisConstants.PSCI_KEY_FAIL_CODE, Integer.valueOf(this.mCsm.errorCode));
        if (this.mSession.getCallProfile().isDowngradedVideoCall()) {
            if (this.mCsm.mVideoRTPtimeout) {
                psItem.put(DiagnosisConstants.PSCI_KEY_CALL_DOWNGRADE, 2);
            } else if (!this.mCsm.mIsStartCameraSuccess) {
                psItem.put(DiagnosisConstants.PSCI_KEY_CALL_DOWNGRADE, 3);
            } else {
                psItem.put(DiagnosisConstants.PSCI_KEY_CALL_DOWNGRADE, 1);
            }
        }
        psItem.put("ROAM", Integer.valueOf(this.mTelephonyManager.isNetworkRoaming() ? 1 : 0));
        if (isEPDGwhenCallEnd(this.mCsm.errorCode)) {
            psItem.put(DiagnosisConstants.PSCI_KEY_EPDG_STATUS, 1);
            psItem.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, 2);
        } else {
            psItem.put(DiagnosisConstants.PSCI_KEY_EPDG_STATUS, 0);
            psItem.put(DiagnosisConstants.PSCI_KEY_CALL_BEARER, 1);
        }
        IMSLog.c(LogClass.VOLTE_END_CALL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId() + ":" + this.mCsm.mCallTypeHistory + ":" + this.mCallDulation + "," + (isCallDrop(this.mCsm.errorCode) ? 1 : 0) + "," + this.mSession.getEndReason() + "," + this.mCsm.errorCode);
        ImsLogAgentUtil.storeLogToAgent(this.mSession.getPhoneId(), this.mContext, DiagnosisConstants.FEATURE_PSCI, psItem);
        StringBuilder sb = new StringBuilder();
        sb.append("PSCI, storeLogToAgent[");
        sb.append(psItem.toString());
        sb.append("]");
        Log.i("CallStateMachine", sb.toString());
        ImsLogAgentUtil.requestToSendStoredLog(this.mSession.getPhoneId(), this.mContext, DiagnosisConstants.FEATURE_PSCI);
    }

    /* access modifiers changed from: protected */
    public boolean isCallDrop(int curErrorCode) {
        if (this.mSession.getCallProfile().hasCSFBError() || curErrorCode == 200 || curErrorCode == 210 || curErrorCode == 220 || curErrorCode == 1000 || curErrorCode == 486 || curErrorCode == 603 || curErrorCode == 1111 || curErrorCode == 3009 || curErrorCode == 3010) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isEPDGwhenCallEnd(int curErrorCode) {
        if (this.mSession.isEpdgCall() || curErrorCode == 2503 || this.mSession.getEndReason() == 21) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void sendPSDailyInfo() {
        ContentValues dayItem = new ContentValues();
        Log.i("CallStateMachine", "CallTypeHistory[" + this.mCsm.mCallTypeHistory + "]");
        if (!TextUtils.isEmpty(this.mCsm.mCallTypeHistory)) {
            String[] types = this.mCsm.mCallTypeHistory.split(",");
            int i = 0;
            int firstCallType = Integer.parseInt(types[0]);
            if (this.mCsm.mSession.getCmcType() > 0) {
                if (!this.mCsm.mSession.getCallProfile().isPullCall() && TextUtils.isEmpty(this.mCsm.mSession.getCallProfile().getReplaceSipCallId())) {
                    dayItem.put(DiagnosisConstants.DRPT_KEY_CMC_START_TOTAL_COUNT, 1);
                }
            } else if (!this.mCsm.mCallInitEPDG) {
                if (firstCallType == 1) {
                    dayItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_START_VOICE_COUNT, 1);
                } else if (ImsCallUtil.isVideoCall(firstCallType)) {
                    dayItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_START_VIDEO_COUNT, 1);
                } else if (ImsCallUtil.isE911Call(firstCallType)) {
                    dayItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_START_EMERGENCY_COUNT, 1);
                }
                dayItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_START_TOTAL_COUNT, 1);
            } else {
                if (firstCallType == 1) {
                    dayItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_START_VOICE_COUNT, 1);
                } else if (ImsCallUtil.isVideoCall(firstCallType)) {
                    dayItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_START_VIDEO_COUNT, 1);
                } else if (ImsCallUtil.isE911Call(firstCallType)) {
                    dayItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_START_EMERGENCY_COUNT, 1);
                }
                dayItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_START_TOTAL_COUNT, 1);
            }
            ContentValues dayItem2 = dailyInfoCallType(dailyInfoCallEnd(dayItem), types);
            if (this.mSession.getEndReason() == 8) {
                i = 1;
            }
            dayItem2.put(DiagnosisConstants.DRPT_KEY_SRVCC_COUNT, Integer.valueOf(i));
            dayItem2.put(DiagnosisConstants.DRPT_KEY_CSFB_COUNT, Integer.valueOf(this.mSession.getCallProfile().hasCSFBError() ? 1 : 0));
            dayItem2.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
            IMSLog.i("CallStateMachine", this.mSession.getPhoneId(), "DRPT, storeLogToAgent[" + dayItem2.toString() + "]");
            ImsLogAgentUtil.storeLogToAgent(this.mSession.getPhoneId(), this.mContext, "DRPT", dayItem2);
        }
    }

    /* access modifiers changed from: protected */
    public ContentValues dailyInfoCallEnd(ContentValues callEndItem) {
        if (this.mSession.getEndReason() != 8 && !this.mSession.getCallProfile().hasCSFBError()) {
            if (this.mCsm.mSession.getCmcType() > 0) {
                if (!this.mCsm.mSession.getCallProfile().isPullCall() && TextUtils.isEmpty(this.mCsm.mSession.getCallProfile().getReplaceSipCallId())) {
                    if (isCallDrop(this.mCsm.errorCode)) {
                        callEndItem.put(DiagnosisConstants.DRPT_KEY_CMC_END_FAIL_COUNT, 1);
                        if (isIncomingFail()) {
                            callEndItem.put(DiagnosisConstants.DRPT_KEY_CMC_INCOMING_FAIL, 1);
                        } else if (isOutgoingFail()) {
                            callEndItem.put(DiagnosisConstants.DRPT_KEY_CMC_OUTGOING_FAIL, 1);
                        }
                    }
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_CMC_END_TOTAL_COUNT, 1);
                }
            } else if (!isEPDGwhenCallEnd(this.mCsm.errorCode)) {
                if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_VIDEO_COUNT, 1);
                } else if (ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())) {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_EMERGENCY_COUNT, 1);
                } else {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_VOICE_COUNT, 1);
                }
                if (isCallDrop(this.mCsm.errorCode)) {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_FAIL_COUNT, 1);
                    if (isIncomingFail()) {
                        callEndItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_INCOMING_FAIL, 1);
                    } else if (isOutgoingFail()) {
                        callEndItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_OUTGOING_FAIL, 1);
                    }
                }
                callEndItem.put(DiagnosisConstants.DRPT_KEY_VOLTE_END_TOTAL_COUNT, 1);
            } else {
                if (ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_VIDEO_COUNT, 1);
                } else if (ImsCallUtil.isE911Call(this.mSession.getCallProfile().getCallType())) {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_EMERGENCY_COUNT, 1);
                } else {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_VOICE_COUNT, 1);
                }
                if (isCallDrop(this.mCsm.errorCode)) {
                    callEndItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_FAIL_COUNT, 1);
                    if (isIncomingFail()) {
                        callEndItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_INCOMING_FAIL, 1);
                    } else if (isOutgoingFail()) {
                        callEndItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_OUTGOING_FAIL, 1);
                    }
                }
                callEndItem.put(DiagnosisConstants.DRPT_KEY_VOWIFI_END_TOTAL_COUNT, 1);
            }
        }
        return callEndItem;
    }

    /* access modifiers changed from: protected */
    public boolean isIncomingFail() {
        if (this.mCsm.getPreviousStateByName(this.mCsm.getPreviousState().getName()) == CallConstants.STATE.IncomingCall) {
            return true;
        }
        if (this.mCsm.getPreviousStateByName(this.mCsm.getPreviousState().getName()) != CallConstants.STATE.ReadyToCall || !this.mSession.getCallProfile().isMTCall()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isOutgoingFail() {
        if (this.mCsm.getPreviousStateByName(this.mCsm.getPreviousState().getName()) == CallConstants.STATE.OutGoingCall || this.mCsm.getPreviousStateByName(this.mCsm.getPreviousState().getName()) == CallConstants.STATE.AlertingCall) {
            return true;
        }
        if (this.mCsm.getPreviousStateByName(this.mCsm.getPreviousState().getName()) != CallConstants.STATE.ReadyToCall || !this.mSession.getCallProfile().isMOCall()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public ContentValues dailyInfoCallType(ContentValues callTypeItem, String[] callTypes) {
        int[] usedType = new int[6];
        for (String type : callTypes) {
            int curType = Integer.parseInt(type);
            if (curType == 1) {
                callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_VOICE_COUNT, 1);
                callTypeItem.put(DiagnosisConstants.DRPT_KEY_DOWNGRADE_TO_VOICE_COUNT, Integer.valueOf(usedType[1] == 1 ? 1 : 0));
                usedType[0] = 1;
            } else if (ImsCallUtil.isVideoCall(curType)) {
                callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_VIDEO_COUNT, 1);
                callTypeItem.put(DiagnosisConstants.DRPT_KEY_UPGRADE_TO_VIDEO_COUNT, Integer.valueOf(usedType[0] == 1 ? 1 : 0));
                usedType[1] = 1;
            } else if (ImsCallUtil.isE911Call(curType)) {
                callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_EMERGENCY_COUNT, 1);
                usedType[2] = 1;
            } else if (ImsCallUtil.isTtyCall(curType)) {
                callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_TTY_COUNT, 1);
                usedType[3] = 1;
            } else if (ImsCallUtil.isRttCall(curType)) {
                callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_RTT_COUNT, 1);
                usedType[4] = 1;
            }
            if (this.mSession.getCallProfile().isConferenceCall()) {
                if (ImsCallUtil.isVideoCall(curType)) {
                    callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_VIDEO_CONFERENCE_COUNT, 1);
                } else {
                    callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_AUDIO_CONFERENCE_COUNT, 1);
                }
                usedType[5] = 1;
            }
        }
        int usedAll = 0;
        for (int i : usedType) {
            if (i == 1) {
                usedAll++;
            }
        }
        callTypeItem.put(DiagnosisConstants.DRPT_KEY_EXPERIENCE_TOTAL_COUNT, Integer.valueOf(usedAll));
        return callTypeItem;
    }
}
