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
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsModifyingCall extends CallState {
    ImsModifyingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        CallProfile requestedProfile = this.mSession.getModifyRequestedProfile();
        if (requestedProfile != null && ImsCallUtil.isUpgradeCall(this.mSession.getCallProfile().getCallType(), requestedProfile.getCallType())) {
            this.mCsm.sendCmcPublishDialog();
        }
        Log.i("CallStateMachine", "Enter [ModifyingCall]");
        this.mModule.onCallModifyRequested(this.mSession.getSessionId());
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[ModifyingCall] processMessage " + msg.what);
        int i = msg.what;
        if (i != 3) {
            if (i == 4) {
                error_ModifyingCall(msg);
            } else if (i == 51) {
                Log.i("CallStateMachine", "[ModifyingCall] Rejecting hold request while processing modify");
                this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
            } else if (i == 52) {
                this.mCsm.notifyOnError(1109, "Call switch failed", 10);
            } else if (i == 80) {
                Log.i("CallStateMachine", "[ModifyingCall] Hold video defered");
                this.mCsm.deferMessage(msg);
            } else if (i != 81) {
                if (i != 84 && i != 85) {
                    switch (i) {
                        case 1:
                        case 100:
                        case 400:
                        case 5000:
                            break;
                        case 41:
                            this.mCsm.handleRemoteHeld(false);
                            break;
                        case 55:
                            switchRequest_ModifyingCall(msg);
                            break;
                        case 62:
                            this.mCsm.handleRemoteHeld(true);
                            break;
                        case 64:
                            Log.i("CallStateMachine", "[ModifyingCall] SEND_TEXT defered");
                            this.mCsm.deferMessage(msg);
                            break;
                        case 71:
                            Log.i("CallStateMachine", "[ModifyingCall] Rejecting resume request while processing modify");
                            this.mCsm.notifyOnError(1112, "Call resume failed");
                            break;
                        case 91:
                            modified_ModifyingCall(msg);
                            break;
                        case 502:
                            Log.i("CallStateMachine", "[ModifyingCall] Re-INVITE defered");
                            this.mCsm.deferMessage(msg);
                            break;
                        default:
                            Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                            break;
                    }
                } else {
                    this.mCsm.transitionTo(this.mCsm.mInCall);
                }
            } else {
                Log.i("CallStateMachine", "[ModifyingCall] Resume video defered");
                this.mCsm.isDeferedVideoResume = true;
                this.mCsm.deferMessage(msg);
            }
            return true;
        }
        return false;
    }

    private void modified_ModifyingCall(Message msg) {
        int modifiedCallType = msg.arg1;
        int orgCallType = msg.arg2;
        Log.i("CallStateMachine", "modifiedCallType " + modifiedCallType + ", orgCallType " + orgCallType);
        boolean z = false;
        if (modifiedCallType == orgCallType) {
            Log.e("CallStateMachine", "Modify requested but callType hasn't changed");
            if (!ImsCallUtil.isRttCall(this.mSession.mModifyRequestedProfile.getCallType())) {
                this.mCsm.notifyOnError(1110, "Call switch rejected");
            } else {
                this.mModule.onSendRttSessionModifyResponse(this.mSession.getCallId(), true ^ ImsCallUtil.isRttCall(orgCallType), false);
            }
            if (this.mSession.mModifyRequestedProfile.getCallType() == 3 && this.mSession.mPrevUsedCamera == -1 && this.mSession.mLastUsedCamera == 0) {
                this.mSession.mLastUsedCamera = -1;
            }
            this.mCsm.transitionTo(this.mCsm.mInCall);
            return;
        }
        if (modifiedCallType == 9 || ImsCallUtil.isRttCall(modifiedCallType)) {
            if (!this.mCsm.isRequestTtyFull) {
                Log.i("CallStateMachine", "TTY/RTT FULL defered");
                this.mCsm.deferMessage(msg);
                return;
            }
            this.mCsm.isRequestTtyFull = false;
        }
        if (modifiedCallType != orgCallType && (ImsCallUtil.isRttCall(modifiedCallType) || ImsCallUtil.isRttCall(orgCallType))) {
            IVolteServiceModuleInternal iVolteServiceModuleInternal = this.mModule;
            int callId = this.mSession.getCallId();
            if (!ImsCallUtil.isRttCall(orgCallType) && ImsCallUtil.isRttCall(modifiedCallType)) {
                z = true;
            }
            iVolteServiceModuleInternal.onSendRttSessionModifyResponse(callId, z, true);
        }
        CallProfile modifiedProfile = new CallProfile();
        modifiedProfile.setCallType(modifiedCallType);
        this.mCsm.onCallModified(modifiedProfile);
        this.mCsm.transitionTo(this.mCsm.mInCall);
    }

    private void switchRequest_ModifyingCall(Message msg) {
        if (this.mSession.mModifyRequestedProfile == null || this.mSession.mModifyRequestedProfile.getCallType() != msg.arg1) {
            Log.i("CallStateMachine", "[ModifyingCall] Rejecting switch request while processing modify");
            if (this.mCsm.rejectModifyCallType(491) < 0) {
                this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
                return;
            }
            return;
        }
        this.mCsm.modifyCallType(this.mSession.mModifyRequestedProfile, false);
        Log.i("CallStateMachine", "[ModifyingCall] accept a call modification in progress of resolving race condition");
    }

    private void error_ModifyingCall(Message msg) {
        SipError err = (SipError) msg.obj;
        if (err.getCode() >= 5000) {
            Log.i("CallStateMachine", "[ModifyingCall] big data code over 5000 means call ended");
            this.mCsm.notifyOnError(1109, "Call switch failed");
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
            this.mCsm.sendMessage(3);
            return;
        }
        if (err.getCode() == 603) {
            this.mCsm.notifyOnError(1110, "Call switch rejected");
        } else {
            this.mCsm.notifyOnError(1109, "Call switch failed");
        }
        this.mCsm.transitionTo(this.mCsm.mInCall);
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        this.mSession.mModifyRequestedProfile = null;
    }
}
