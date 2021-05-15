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
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class ImsDefaultCall extends CallState {
    ImsDefaultCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    /* access modifiers changed from: protected */
    public void dbrLost_ANYSTATE(Message msg) {
        if (msg.arg1 != 1) {
            return;
        }
        if (this.mMno.isChn() && this.mCsm.mOnErrorDelayed) {
            Log.i("CallStateMachine", "[ANYSTATE] Delaying CSFB is InProgress. Ignore DBR lost");
            this.mCsm.mOnErrorDelayed = false;
        } else if (this.mSession.getDedicatedBearerState(1) == 3 || this.mMno != Mno.SWISSCOM) {
            this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(11);
            if (this.mVolteSvcIntf.endCall(this.mSession.getSessionId(), this.mCsm.callType, this.mCsm.sipReason) < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(Id.REQUEST_SIP_DIALOG_OPEN, ""));
                return;
            }
            this.mSession.setEndType(1);
            this.mSession.setEndReason(11);
            if (this.mMno == Mno.KDDI) {
                this.mCsm.errorCode = 2699;
            } else {
                this.mCsm.errorCode = Id.REQUEST_SIP_DIALOG_OPEN;
            }
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
        } else {
            Log.i("CallStateMachine", "[ANYSTATE] Audio dedicated bearer is re-established. Ignore DBR lost");
        }
    }

    /* access modifiers changed from: protected */
    public void terminate_ANYSTATE(Message msg) {
        this.mSession.setEndType(1);
        this.mCsm.callType = this.mSession.getCallProfile().getCallType();
        if (msg.arg1 == 19) {
            this.mCsm.errorCode = 4001;
            this.mCsm.notifyOnEnded(this.mCsm.errorCode);
            return;
        }
        if (msg.arg1 == 8) {
            this.mCsm.srvccStarted = false;
            Log.i("CallStateMachine", "[ANYSTATE] SRVCC HO Success");
            IMSLog.c(LogClass.VOLTE_SRVCC_SUCCESS_UNHA_TERM, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
        }
        if (this.mCsm.srvccStarted) {
            Log.i("CallStateMachine", "[ANYSTATE] SRVCC HO ongoing, do not terminate call");
            return;
        }
        this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(msg.arg1);
        if (this.mMno == Mno.TMOUS) {
            if (this.mCsm.errorCode == 28) {
                this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(28);
                IRegistrationGovernor governor = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle());
                if (governor != null) {
                    governor.onSipError("mmtel", SipErrorBase.SIP_INVITE_TIMEOUT);
                }
            } else if (this.mSession.getDRBLost() || this.mCsm.errorCode == 11) {
                Log.i("CallStateMachine", "TMOUS, DBR Lost");
                this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(11);
            } else if (this.mSession.getCallProfile().getRadioTech() != 18 && this.mSession.getDedicatedBearerState(1) == 3 && this.mSession.getCallState() == CallConstants.STATE.InCall) {
                Log.i("CallStateMachine", "TMOUS, DBR was not established");
                this.mCsm.sipReason = this.mCsm.getSipReasonFromUserReason(29);
            }
        }
        if (msg.arg2 == 3) {
            Log.i("CallStateMachine", "[ANYSTATE] Local Release");
            this.mSession.setEndType(3);
            if (this.mCsm.sipReason == null) {
                this.mCsm.sipReason = new SipReason("", 0, "", true, new String[0]);
            } else {
                this.mCsm.sipReason.setLocalRelease(true);
            }
        }
        if (this.mVolteSvcIntf.endCall(this.mSession.getSessionId(), this.mCsm.callType, this.mCsm.sipReason) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
        }
    }

    /* access modifiers changed from: protected */
    public void handleBigData_ANYSTATE(Message msg) {
        if (this.mCsm.errorCode != 2414) {
            this.mCsm.errorCode = ImsCallUtil.convertCallEndReasonToFramework(1, msg.arg1);
        }
        Log.i("CallStateMachine", "[handleBigData_ANYSTATE] setEndReason: " + msg.arg1);
        this.mSession.setEndReason(msg.arg1);
        if (msg.arg1 < 0) {
            this.mCsm.errorCode = 220;
        }
        if (this.mCsm.errorCode == 1701) {
            this.mCsm.mIsBigDataEndReason = true;
            this.mCsm.errorMessage = "Network disconnected";
        } else if (this.mCsm.errorCode == 1107) {
            this.mCsm.mIsBigDataEndReason = true;
            this.mCsm.errorMessage = "Network handover";
        } else if (this.mCsm.errorCode == 2503) {
            this.mCsm.mIsBigDataEndReason = true;
            this.mCsm.errorMessage = "Network disconnected";
        } else if (this.mCsm.errorCode == 1201) {
            this.mCsm.mIsBigDataEndReason = true;
            this.mCsm.errorMessage = "Qos failure";
        } else if (this.mCsm.errorCode == 6007) {
            this.mCsm.mIsBigDataEndReason = true;
            this.mCsm.errorMessage = "pull call by primary";
        } else if (this.mCsm.errorCode == 1703) {
            this.mCsm.mIsBigDataEndReason = true;
            this.mCsm.errorMessage = "Network disconnected";
        }
    }

    /* access modifiers changed from: protected */
    public void ended_ANYSTATE(Message msg) {
        if (msg.obj != null && (msg.obj instanceof String)) {
            this.mCsm.errorMessage = (String) msg.obj;
            boolean hasCallEndReason = false;
            Log.i("CallStateMachine", "[ANYSTATE] ENDED Reason " + this.mCsm.errorMessage);
            if (this.mMno == Mno.VZW) {
                hasCallEndReason = checkVZWHasEndReason();
            } else if (this.mMno == Mno.ATT) {
                hasCallEndReason = checkATTHasEndReason();
            } else if (this.mMno == Mno.MDMN) {
                hasCallEndReason = checkMDMNHasEndReason();
            } else if (this.mCsm.getState() != CallConstants.STATE.IncomingCall || !isCallForkingReason(this.mCsm.errorMessage)) {
                if (this.mMno == Mno.TELSTRA && "RTP Timeout".equalsIgnoreCase(this.mCsm.errorMessage)) {
                    hasCallEndReason = true;
                    this.mCsm.errorCode = Id.REQUEST_CHATBOT_ANONYMIZE;
                }
            } else if ("call completed elsewhere".equalsIgnoreCase(this.mCsm.errorMessage) || this.mCsm.errorMessage.toLowerCase().contains("call completed elsewhere")) {
                hasCallEndReason = true;
                this.mCsm.errorCode = ImSessionEvent.SEND_DELIVERED_NOTIFICATION;
            } else if ("busy everywhere".equalsIgnoreCase(this.mCsm.errorMessage) || "declined".equalsIgnoreCase(this.mCsm.errorMessage)) {
                hasCallEndReason = true;
                this.mCsm.errorCode = ImSessionEvent.RECEIVE_SLM_MESSAGE;
            }
            if ("RTP Timeout".equalsIgnoreCase(this.mCsm.errorMessage) || "RTCP timeout".equalsIgnoreCase(this.mCsm.errorMessage) || "RTP-RTCP Timeout".equalsIgnoreCase(this.mCsm.errorMessage)) {
                this.mCsm.mIsBigDataEndReason = true;
                this.mCsm.errorCode = Id.REQUEST_CHATBOT_ANONYMIZE;
            }
            if (hasCallEndReason) {
                this.mCsm.notifyOnError(this.mCsm.errorCode, this.mCsm.errorMessage);
            } else if (!this.mCsm.mIsBigDataEndReason) {
                this.mCsm.sipReason = new SipReason("SIP", 210, this.mCsm.errorMessage, new String[0]);
                this.mCsm.errorCode = 210;
                this.mCsm.errorMessage = "";
            }
        }
        this.mCsm.deferMessage(msg);
        this.mCsm.transitionTo(this.mCsm.mEndingCall);
    }

    private boolean isCallForkingReason(String errorMessage) {
        return "call completed elsewhere".equalsIgnoreCase(errorMessage) || "busy everywhere".equalsIgnoreCase(errorMessage) || "declined".equalsIgnoreCase(errorMessage) || errorMessage.toLowerCase().contains("call completed elsewhere");
    }

    private boolean checkVZWHasEndReason() {
        if (this.mCsm.errorMessage.toLowerCase().contains("call completion elsewhere")) {
            this.mCsm.errorCode = 2504;
            return true;
        } else if (this.mCsm.errorMessage.toLowerCase().contains("another device sent all devices busy response")) {
            this.mCsm.errorCode = 2505;
            return true;
        } else if (this.mCsm.errorMessage.toLowerCase().contains("call has been pulled by another device")) {
            this.mCsm.errorCode = 2506;
            return true;
        } else if (!this.mCsm.errorMessage.toLowerCase().contains("deregistered") || this.mCsm.mTryingReceived) {
            return false;
        } else {
            this.mCsm.errorCode = NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS;
            return true;
        }
    }

    private boolean checkATTHasEndReason() {
        boolean hasEndReason = false;
        if ("call completed elsewhere".equalsIgnoreCase(this.mCsm.errorMessage)) {
            hasEndReason = true;
            this.mCsm.errorCode = ImSessionEvent.SEND_DELIVERED_NOTIFICATION;
        }
        if (this.mRegistration == null || !this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
            return hasEndReason;
        }
        if ("call has been transferred to another device".equalsIgnoreCase(this.mCsm.errorMessage)) {
            this.mCsm.errorCode = ImSessionEvent.SEND_MESSAGE_DONE;
            return true;
        } else if ("service not allowed in this location".equalsIgnoreCase(this.mCsm.errorMessage)) {
            this.mCsm.errorCode = ImSessionEvent.ATTACH_FILE;
            return true;
        } else if (!"call completed elsewhere".equalsIgnoreCase(this.mCsm.errorMessage)) {
            return hasEndReason;
        } else {
            this.mCsm.errorCode = ImSessionEvent.SEND_MESSAGE;
            return true;
        }
    }

    private boolean checkMDMNHasEndReason() {
        if ("push_to_master".equalsIgnoreCase(this.mCsm.errorMessage)) {
            this.mCsm.errorCode = 4002;
            return true;
        } else if ("MDMN_PULL_BY_PRIMARY".equalsIgnoreCase(this.mCsm.errorMessage)) {
            this.mCsm.errorCode = 6007;
            return true;
        } else if (!"MDMN_PULL_BY_SECONDARY".equalsIgnoreCase(this.mCsm.errorMessage)) {
            return false;
        } else {
            this.mCsm.errorCode = 6008;
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void error_ANYSTATE(Message msg) {
        SipError err = (SipError) msg.obj;
        int retryAfter = msg.arg1;
        this.mCsm.errorCode = err.getCode();
        this.mCsm.errorMessage = err.getReason() == null ? "" : err.getReason();
        int i = 0;
        if (this.mCsm.errorCode == 1001) {
            Log.e("CallStateMachine", "[ANY_STATE] Client error: " + this.mCsm.errorMessage);
            if (this.mMno.isKor()) {
                Log.i("CallStateMachine", "[ANY_STATE] IMSService Restart!!!.");
                System.exit(0);
            }
        } else {
            Log.e("CallStateMachine", "[ANY_STATE] Unexpected ErrorCode: " + this.mCsm.errorCode + ": errorMessage " + this.mCsm.errorMessage);
        }
        if (this.mCsm.errorCode != 403 || !"Call switch failed".equalsIgnoreCase(this.mCsm.errorMessage)) {
            handleUssdError();
            handleGcfModeError();
            if ((this.mMno == Mno.STARHUB || this.mMno == Mno.SMARTFREN || this.mMno == Mno.AIS) && this.mCsm.errorCode == 480) {
                Log.i("CallStateMachine", "[ANY_STATE] TEMPORARILY_UNAVAILABLE -> REQUEST_TIMEOUT");
                this.mCsm.errorCode = 408;
            }
            handleUSACarrierError();
            if (this.mMno == Mno.KDDI && retryAfter > 0) {
                this.mCsm.mRetryInprogress = true;
            }
            if (this.mMno.isTmobile() && this.mCsm.errorCode == 603) {
                this.mCsm.errorCode = NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE;
            }
            if (this.mMno.isKor()) {
                if (this.mCsm.errorCode == 499) {
                    this.mCsm.errorCode = 2102;
                } else if (this.mCsm.errorCode == 709) {
                    this.mCsm.errorCode = 1114;
                }
            }
            if (this.mSession.isCmcSecondaryType(this.mSession.getCmcType()) && this.mCsm.errorCode == 403 && "SD_NOT_REGISTERED".equals(this.mCsm.errorMessage)) {
                this.mCsm.errorCode = 404;
            }
            CallStateMachine callStateMachine = this.mCsm;
            int i2 = this.mCsm.errorCode;
            String str = this.mCsm.errorMessage;
            if (retryAfter > 0) {
                i = retryAfter;
            }
            callStateMachine.notifyOnError(i2, str, i);
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
            this.mCsm.sendMessage(3, msg.arg1, msg.arg2);
            return;
        }
        this.mCsm.notifyOnError(1109, "Call switch failed");
    }

    private void handleUssdError() {
        if (this.mCsm.callType != 12 || !ImsCallUtil.isCSFBbySIPErrorCode(this.mCsm.errorCode) || this.mMno != Mno.TMOUS) {
            return;
        }
        if (this.mSession.getCallProfile().getOriginatingUri() == null || this.mRegistration == null || this.mRegistration.getPreferredImpu().getUri().equals(this.mSession.getCallProfile().getOriginatingUri())) {
            this.mCsm.errorCode = 403;
            return;
        }
        Log.i("CallStateMachine", "[ANY_STATE] no CSFB USSD for virtual line.");
        this.mCsm.errorCode = Id.REQUEST_UPDATE_TIME_IN_PLANI;
    }

    private void handleGcfModeError() {
        if (!DeviceUtil.getGcfMode().booleanValue()) {
            return;
        }
        if (this.mCsm.errorCode == 503 || this.mCsm.errorCode == 504) {
            this.mCsm.errorCode = 1000;
        }
    }

    private void setErrorCodeForAtt() {
        if (this.mCsm.errorCode == 403 && "Service not allowed in this location".equalsIgnoreCase(this.mCsm.errorMessage)) {
            this.mCsm.errorCode = ImSessionEvent.RECEIVE_MESSAGE;
        } else if (this.mCsm.errorCode == 503 && "Emergency calls over WiFi not allowed in this location".equalsIgnoreCase(this.mCsm.errorMessage)) {
            this.mCsm.errorCode = ImSessionEvent.SEND_SLM_MESSAGE_DONE;
        }
        if (this.mRegistration != null && this.mRegistration.getImsProfile().isSoftphoneEnabled()) {
            if (this.mCsm.errorCode == 603 && "Secondary device already in use".equalsIgnoreCase(this.mCsm.errorMessage)) {
                this.mCsm.errorCode = ImSessionEvent.FILE_COMPLETE;
            } else if (this.mCsm.errorCode == 480 && this.mCsm.errorMessage.equalsIgnoreCase("You have an active call on another soft phone that must complete before you can use this soft phone")) {
                this.mCsm.errorCode = ImSessionEvent.SEND_SLM_MESSAGE;
            } else if (this.mCsm.errorCode != 403) {
            } else {
                if ("Service not allowed in this location".equalsIgnoreCase(this.mCsm.errorMessage)) {
                    this.mCsm.errorCode = ImSessionEvent.ATTACH_FILE;
                } else if ("Simultaneous call limit has already been reached".equalsIgnoreCase(this.mCsm.errorMessage)) {
                    this.mCsm.errorCode = ImSessionEvent.SEND_FILE;
                }
            }
        }
    }

    public void handleUSACarrierError() {
        if (this.mMno == Mno.TMOUS && this.mCsm.errorCode == SipErrorBase.DECLINE.getCode() && this.mSession.getCallProfile().getOriginatingUri() != null && this.mRegistration != null && !this.mRegistration.getPreferredImpu().getUri().equals(this.mSession.getCallProfile().getOriginatingUri())) {
            Log.i("CallStateMachine", "[ANY_STATE] no CSFB for virtual line.");
            this.mCsm.errorCode = 2413;
        }
        if (this.mMno == Mno.VZW && this.mCsm.errorCode == 403 && this.mCsm.errorMessage.toLowerCase().contains("simultaneous call limit has already been reached")) {
            this.mCsm.errorCode = 2510;
        }
        if (this.mMno == Mno.ATT) {
            setErrorCodeForAtt();
        }
    }

    /* access modifiers changed from: protected */
    public void ussdIndication_ANYSTATE(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        int status = bundle.getInt("status");
        notifyOnUssdIndication(status, bundle.getInt("dcs"), bundle.getByteArray("data"));
        Log.i("CallStateMachine", "[ANYSTATE] USSD indi, change status=" + status);
        if (status == 2) {
            this.mCsm.transitionTo(this.mCsm.mInCall);
        } else if (status == 1 && this.mSession.getCallProfile().getDirection() == 1) {
            this.mCsm.transitionTo(this.mCsm.mInCall);
            Bundle infoBundle = new Bundle();
            infoBundle.putInt("type", 4);
            infoBundle.putString("info", "");
            this.mCsm.sendMessage(101, (Object) infoBundle);
        } else {
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
            this.mCsm.sendMessage(3);
        }
    }

    /* access modifiers changed from: protected */
    public void update_ANYSTATE(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        if (bundle.getParcelable("profile") == null) {
            int cause = bundle.getInt("cause");
            if (cause == 100) {
                Log.i("CallStateMachine", "[ANYSTATE] SRVCC HO STARTED");
                IMSLog.c(LogClass.VOLTE_SRVCC_START_UNHA_UPDA, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = true;
            } else if (cause == 200) {
                Log.i("CallStateMachine", "[ANYSTATE] SRVCC HO SUCCESS");
                IMSLog.c(LogClass.VOLTE_SRVCC_SUCCESS_UNHA_UPDA, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = false;
            } else if (cause == 487) {
                Log.i("CallStateMachine", "[ANYSTATE] SRVCC HO FAILURE OR CANCELED");
                IMSLog.c(LogClass.VOLTE_SRVCC_FAIL_UNHA_UPDA, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = false;
                this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", cause, bundle.getString("reasonText"), new String[0]));
            }
        } else {
            Log.e("CallStateMachine", "[ANYSTATE] Profile-related update is possible in InCall state only");
        }
    }

    /* access modifiers changed from: protected */
    public void epdgConnChanged_ANYSTATE(Message msg) {
        if (this.mSession.getCallProfile() != null && ImsCallUtil.isVideoCall(this.mSession.getCallProfile().getCallType())) {
            Log.i("CallStateMachine", "[ANY_STATE] msg: ON_EPDG_CONNECTION_CHANGED " + msg.arg1);
            this.mCsm.stopNetworkStatsOnPorts();
            this.mCsm.startNetworkStatsOnPorts();
        }
        notifyOnEpdgStateChanged();
    }

    private void notifyOnEpdgStateChanged() {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onEpdgStateChanged();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void notifyOnUssdIndication(int status, int dcs, byte[] data) {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onUssdReceived(status, dcs, data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }
}
