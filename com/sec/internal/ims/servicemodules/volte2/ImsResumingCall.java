package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsResumingCall extends CallState {
    ImsResumingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        Log.i("CallStateMachine", "Enter [ResumingCall]");
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[ResumingCall] processMessage " + msg.what);
        int i = msg.what;
        if (i != 1) {
            if (i == 41) {
                established_ResumingCall();
            } else if (i == 55) {
                switchRequest_ResumingCall();
            } else if (i == 71) {
                Log.i("CallStateMachine", "[ResumingCall] ignore resume request while processing resume");
                this.mCsm.notifyOnError(1112, "Call resume failed");
            } else if (i == 91) {
                modified_ResumingCall(msg);
            } else if (!(i == 100 || i == 400)) {
                if (i == 502) {
                    Log.i("CallStateMachine", "[ResumingCall] Re-INVITE defered");
                    this.mCsm.deferMessage(msg);
                } else if (i != 5000) {
                    if (i == 3) {
                        this.mCsm.errorCode = 1112;
                        this.mCsm.notifyOnError(this.mCsm.errorCode, "Call resume failed");
                        this.mCsm.transitionTo(this.mCsm.mEndingCall);
                        this.mCsm.sendMessage(3);
                    } else if (i == 4) {
                        error_ResumingCall(msg);
                    } else if (i == 51) {
                        Log.i("CallStateMachine", "[ResumingCall] Rejecting hold request while processing modify");
                        this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
                    } else if (i != 52) {
                        switch (i) {
                            case 61:
                                this.mCsm.handleRemoteHeld(false);
                                break;
                            case 62:
                                heldRemote_ResumingCall();
                                break;
                            case 63:
                                this.mCsm.handleRemoteHeld(true);
                                break;
                            default:
                                Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                                break;
                        }
                    } else {
                        this.mCsm.notifyOnError(1109, "Call switch failed", 10);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void modified_ResumingCall(Message msg) {
        CallProfile callProfile;
        int modifiedCallType = msg.arg1;
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

    private void heldRemote_ResumingCall() {
        if (this.mMno == Mno.TELSTRA) {
            this.mCsm.mRemoteHeld = false;
        }
        this.mCsm.notifyOnResumed(true);
        this.mCsm.handleRemoteHeld(true);
        this.mCsm.transitionTo(this.mCsm.mInCall);
    }

    private void established_ResumingCall() {
        this.mCsm.sendCmcPublishDialog();
        this.mCsm.notifyOnResumed(true);
        this.mCsm.transitionTo(this.mCsm.mInCall);
    }

    private void switchRequest_ResumingCall() {
        Log.i("CallStateMachine", "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
        }
    }

    private void error_ResumingCall(Message msg) {
        if (((SipError) msg.obj).getCode() >= 5000) {
            Log.i("CallStateMachine", "[ResumingCall] big data code over 5000 means call ended");
            this.mCsm.notifyOnError(1112, "Call resume failed");
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
            this.mCsm.sendMessage(3);
            return;
        }
        this.mCsm.notifyOnError(1112, "Call resume failed");
        int[] callsCount = this.mModule.getCallCount(this.mSession.getPhoneId());
        if (this.mSession.mResumeCallRetriggerTimer != 0 && callsCount[0] == 1) {
            this.mCsm.startRetriggerTimer((long) this.mSession.mResumeCallRetriggerTimer);
        }
        this.mCsm.transitionTo(this.mCsm.mHeldCall);
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }
}
