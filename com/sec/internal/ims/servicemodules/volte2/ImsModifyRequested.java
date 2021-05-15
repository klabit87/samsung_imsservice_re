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
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsModifyRequested extends CallState {
    ImsModifyRequested(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        CallProfile requestedProfile = this.mSession.getModifyRequestedProfile();
        if (requestedProfile != null && ImsCallUtil.isUpgradeCall(this.mSession.getCallProfile().getCallType(), requestedProfile.getCallType())) {
            this.mCsm.sendCmcPublishDialog();
        }
        Log.i("CallStateMachine", "Enter [ModifyRequested]");
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[ModifyRequested] processMessage " + msg.what);
        int i = msg.what;
        if (i == 1) {
            return false;
        }
        if (i == 55) {
            switchRequest_ModifyRequested(msg);
        } else if (i == 62) {
            this.mCsm.handleRemoteHeld(true);
        } else if (i == 64) {
            Log.i("CallStateMachine", "[ModifyRequested] SEND_TEXT defered");
            this.mCsm.deferMessage(msg);
        } else if (i == 71) {
            Log.i("CallStateMachine", "[ModifyRequested] Rejecting resume request while processing modify");
            this.mCsm.notifyOnError(1112, "Call resume failed");
        } else if (i == 91) {
            moidfied_ModifyRequested(msg);
        } else if (i == 100 || i == 400) {
            return false;
        } else {
            if (i == 502) {
                Log.i("CallStateMachine", "[ModifyRequested] Re-INVITE defered");
                this.mCsm.deferMessage(msg);
            } else if (i == 5000 || i == 3 || i == 4) {
                return false;
            } else {
                if (i == 22) {
                    accept_ModifyRequested(msg);
                } else if (i == 23) {
                    reject_ModifyRequested(msg);
                } else if (i == 51) {
                    Log.i("CallStateMachine", "[ModifyRequested] Rejecting hold request while processing modify");
                    this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
                } else if (i == 52) {
                    update_ModifyRequested(msg);
                } else if (i == 80) {
                    Log.i("CallStateMachine", "[ModifyRequested] Hold video defered");
                    this.mCsm.deferMessage(msg);
                } else if (i != 81) {
                    Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                } else {
                    Log.i("CallStateMachine", "[ModifyRequested] Resume video defered");
                    this.mCsm.isDeferedVideoResume = true;
                    this.mCsm.deferMessage(msg);
                }
            }
        }
        return true;
    }

    private void accept_ModifyRequested(Message msg) {
        CallProfile profile = (CallProfile) msg.obj;
        if (this.mCsm.isChangedCallType(profile)) {
            this.mCsm.modifyCallType(profile, false);
        }
        this.mSession.mModifyRequestedProfile = profile;
    }

    private void reject_ModifyRequested(Message msg) {
        if (this.mSession.getUsingCamera()) {
            this.mSession.mLastUsedCamera = this.mSession.mPrevUsedCamera;
        }
        if (this.mCsm.rejectModifyCallType(msg.arg1) < 0) {
            this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
        } else {
            this.mCsm.transitionTo(this.mCsm.mInCall);
        }
    }

    private void moidfied_ModifyRequested(Message msg) {
        CallProfile callProfile;
        int modifiedCallType = msg.arg1;
        int orgCallType = msg.arg2;
        CallProfile modifiedProfile = new CallProfile();
        modifiedProfile.setCallType(modifiedCallType);
        if (this.mMno != Mno.CMCC || orgCallType != modifiedCallType) {
            this.mCsm.onCallModified(modifiedProfile);
        } else if (!ImsCallUtil.isTtyCall(modifiedCallType)) {
            IImsMediaController iImsMediaController = this.mMediaController;
            int sessionId = this.mSession.getSessionId();
            if (this.mSession.mModifyRequestedProfile == null) {
                callProfile = this.mSession.getCallProfile();
            } else {
                callProfile = this.mSession.mModifyRequestedProfile;
            }
            iImsMediaController.receiveSessionModifyResponse(sessionId, 487, callProfile, modifiedProfile);
        }
        this.mCsm.transitionTo(this.mCsm.mInCall);
    }

    private void update_ModifyRequested(Message msg) {
        CallProfile modifyingProfile = ((Bundle) msg.obj).getParcelable("profile");
        if (modifyingProfile == null || !ImsCallUtil.isTtyCall(modifyingProfile.getCallType())) {
            Log.i("CallStateMachine", "[ModifyRequested] Modify request from remote is ongoing return fail to UPDATE from APP");
            this.mCsm.mModifyingProfile = modifyingProfile;
            this.mCsm.notifyOnError(1109, "Call switch failed", 10);
            return;
        }
        Log.i("CallStateMachine", "[ModifyRequested] defer setTty request.");
        this.mCsm.deferMessage(msg);
    }

    private void switchRequest_ModifyRequested(Message msg) {
        int modifyCallType = msg.arg1;
        int curCallType = msg.arg2;
        if ((this.mMno == Mno.CTC || this.mMno == Mno.CTCMO) && curCallType == 2 && modifyCallType == 3) {
            Log.i("CallStateMachine", "[ModifyRequested] CTC Bidirectional call switch defered");
            this.mCsm.deferMessage(msg);
        }
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        this.mSession.mModifyRequestedProfile = null;
        this.mCsm.mModifyingProfile = null;
    }
}
