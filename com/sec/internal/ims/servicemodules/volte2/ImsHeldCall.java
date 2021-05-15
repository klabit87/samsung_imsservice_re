package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;

public class ImsHeldCall extends CallState {
    ImsHeldCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        if (this.mCsm.mHoldBeforeTransfer) {
            this.mModule.pushCallInternal();
        } else {
            this.mCsm.notifyOnHeld(true);
        }
        Log.i("CallStateMachine", "Enter [HeldCall]");
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[HeldCall] processMessage " + msg.what);
        int i = msg.what;
        if (!(i == 3 || i == 4)) {
            if (i == 51) {
                Log.i("CallStateMachine", "[HeldCall] already in HOLD");
                this.mCsm.notifyOnHeld(true);
            } else if (i != 52) {
                switch (i) {
                    case 1:
                    case 100:
                    case 400:
                    case 5000:
                        break;
                    case 41:
                    case 61:
                        this.mCsm.handleRemoteHeld(false);
                        break;
                    case 55:
                        return switchRequest_HeldCall(msg);
                    case 59:
                        this.mCsm.transferCall((String) msg.obj);
                        break;
                    case 63:
                        this.mCsm.handleRemoteHeld(true);
                        break;
                    case 71:
                        resume_HeldCall();
                        break;
                    case 75:
                        refuerStatus_HeldCall(msg);
                        break;
                    case 91:
                        modified_HeldCall(msg);
                        break;
                    case 202:
                        this.mCsm.sendMessage(71);
                        break;
                    case 502:
                        Log.i("CallStateMachine", "[HeldCall] ignore re-INVITE request");
                        break;
                    default:
                        Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                        break;
                }
            } else {
                update_HeldCall(msg);
            }
            return true;
        }
        return false;
    }

    public void exit() {
        this.mCsm.stopRetriggerTimer();
        this.mCsm.setPreviousState(this);
        this.mCsm.mHeldProfile = null;
    }

    private void resume_HeldCall() {
        if (this.mVolteSvcIntf.resumeCall(this.mSession.getSessionId()) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
        } else {
            this.mCsm.transitionTo(this.mCsm.mResumingCall);
        }
    }

    private void refuerStatus_HeldCall(Message msg) {
        if (this.mCsm.mTransferRequested) {
            if (msg.arg1 != 200) {
                CallStateMachine callStateMachine = this.mCsm;
                callStateMachine.notifyOnError(1119, "call transfer failed (" + msg.arg1 + ")");
                Iterator<ImsCallSession> it = this.mModule.getSessionList(this.mSession.mPhoneId).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ImsCallSession session = it.next();
                    if (session.smCallStateMachine.mHoldBeforeTransfer) {
                        session.smCallStateMachine.sendMessage(71);
                        session.smCallStateMachine.mHoldBeforeTransfer = false;
                        session.smCallStateMachine.mTransferRequested = false;
                        break;
                    }
                }
            } else {
                CallStateMachine callStateMachine2 = this.mCsm;
                callStateMachine2.sendMessage(4, 0, -1, new SipError(1118, "call transfer success (" + msg.arg1 + ")"));
            }
            this.mCsm.mHoldBeforeTransfer = false;
            this.mCsm.mTransferRequested = false;
        }
    }

    private void update_HeldCall(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        CallProfile profile = bundle.getParcelable("profile");
        int srvccVersion = this.mModule.getSrvccVersion(this.mSession.getPhoneId());
        if (profile != null || srvccVersion == 0) {
            if (profile != null) {
                if (ImsCallUtil.isVideoCall(profile.getCallType())) {
                    Log.i("CallStateMachine", "[HeldCall] Held request is ongoing return fail to UPDATE from APP");
                    this.mCsm.mHeldProfile = profile;
                } else if (ImsCallUtil.isRttCall(profile.getCallType()) || ImsCallUtil.isRttCall(this.mSession.getCallProfile().getCallType())) {
                    this.mCsm.mHeldProfile = profile;
                }
            }
            this.mCsm.notifyOnError(1109, "Call switch failed", 10);
        } else if (srvccVersion >= 9 || DeviceUtil.getGcfMode()) {
            Log.i("CallStateMachine", "mid-call sRVCC supported [during held state]");
            int cause = bundle.getInt("cause");
            if (cause == 100) {
                Log.i("CallStateMachine", "SRVCC HO STARTED");
                IMSLog.c(LogClass.VOLTE_SRVCC_START, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = true;
            } else if (cause == 200) {
                Log.i("CallStateMachine", "SRVCC HO SUCCESS");
                IMSLog.c(LogClass.VOLTE_SRVCC_SUCCESS, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = false;
            } else if (cause == 487) {
                Log.i("CallStateMachine", "SRVCC HO FAILURE OR CANCELED");
                IMSLog.c(LogClass.VOLTE_SRVCC_FAIL, this.mSession.getPhoneId() + "," + this.mSession.getSessionId());
                this.mCsm.srvccStarted = false;
                this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", cause, bundle.getString("reasonText"), new String[0]));
            }
        }
    }

    private boolean switchRequest_HeldCall(Message msg) {
        Log.i("CallStateMachine", "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) >= 0) {
            return true;
        }
        this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
        return false;
    }

    private void modified_HeldCall(Message msg) {
        CallProfile callProfile;
        int modifiedCallType = msg.arg1;
        int orgCallType = msg.arg2;
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
            if (ImsCallUtil.isRttCall(modifiedCallType) || ImsCallUtil.isRttCall(orgCallType)) {
                this.mModule.onSendRttSessionModifyRequest(this.mSession.getCallId(), ImsCallUtil.isRttCall(modifiedCallType));
            }
        }
    }
}
