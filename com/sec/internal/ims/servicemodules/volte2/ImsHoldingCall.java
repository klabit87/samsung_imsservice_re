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
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsHoldingCall extends CallState {
    ImsHoldingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        Log.i("CallStateMachine", "Enter [HoldingCall]");
        if (this.mSession.getUsingCamera()) {
            this.mSession.stopCamera();
        }
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[HoldingCall] processMessage " + msg.what);
        int i = msg.what;
        if (i != 1) {
            if (i == 41) {
                this.mCsm.handleRemoteHeld(false);
            } else if (i == 55) {
                return switchRequest_HoldingCall(msg);
            } else {
                if (i == 91) {
                    modified_HoldingCall(msg);
                } else if (!(i == 100 || i == 400)) {
                    if (i == 502) {
                        this.mCsm.mReinvite = true;
                        Log.i("CallStateMachine", "[HoldingCall] Re-INVITE defered");
                        this.mCsm.deferMessage(msg);
                    } else if (i != 5000) {
                        if (i == 3) {
                            this.mCsm.errorCode = NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR;
                            this.mCsm.notifyOnError(this.mCsm.errorCode, "Call hold failed");
                            this.mCsm.transitionTo(this.mCsm.mEndingCall);
                            this.mCsm.sendMessage(3);
                        } else if (i == 4) {
                            error_HoldingCall(msg);
                        } else if (i == 51) {
                            Log.i("CallStateMachine", "ignore hold request while processing hold");
                            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
                        } else if (i != 52) {
                            switch (i) {
                                case 61:
                                    break;
                                case 62:
                                    this.mCsm.handleRemoteHeld(true);
                                    break;
                                case 63:
                                    if (this.mMno != Mno.TELSTRA) {
                                        this.mCsm.handleRemoteHeld(true);
                                        break;
                                    }
                                    break;
                                default:
                                    Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                                    break;
                            }
                            this.mCsm.transitionTo(this.mCsm.mHeldCall);
                        } else {
                            update_HoldingCall(msg);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        this.mCsm.mHoldingProfile = null;
    }

    private void error_HoldingCall(Message msg) {
        SipError err = (SipError) msg.obj;
        if (err.getCode() >= 5000) {
            Log.i("CallStateMachine", "[HoldingCall] big data code over 5000 means call ended");
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
            this.mCsm.transitionTo(this.mCsm.mEndingCall);
            this.mCsm.sendMessage(3);
        } else if (err.getCode() != 491 || this.mModule.getCallCount(this.mSession.getPhoneId())[0] <= 1) {
            if (this.mCsm.mHoldBeforeTransfer) {
                CallStateMachine callStateMachine = this.mCsm;
                callStateMachine.notifyOnError(1119, "call transfer failed (" + msg.arg1 + ")");
                this.mCsm.mHoldBeforeTransfer = false;
            } else {
                this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed");
            }
            this.mCsm.transitionTo(this.mCsm.mInCall);
        } else {
            this.mCsm.notifyOnError(NSDSNamespaces.NSDSResponseCode.ERROR_SERVER_ERROR, "Call hold failed", 0);
            this.mCsm.transitionTo(this.mCsm.mInCall);
        }
    }

    private void update_HoldingCall(Message msg) {
        CallProfile holdingProfile = ((Bundle) msg.obj).getParcelable("profile");
        if (holdingProfile != null && ImsCallUtil.isVideoCall(holdingProfile.getCallType())) {
            Log.i("CallStateMachine", "[HoldingCall] Holding request is ongoing return fail to UPDATE from APP");
            this.mCsm.mHoldingProfile = holdingProfile;
        }
        this.mCsm.notifyOnError(1109, "Call switch failed", 10);
    }

    private boolean switchRequest_HoldingCall(Message msg) {
        Log.i("CallStateMachine", "Rejecting switch request - send 603 to remote party");
        if (this.mCsm.rejectModifyCallType(Id.REQUEST_UPDATE_TIME_IN_PLANI) >= 0) {
            return true;
        }
        this.mCsm.sendMessage(4, 0, -1, new SipError(1001, ""));
        return false;
    }

    private void modified_HoldingCall(Message msg) {
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
}
