package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsHoldingVideo extends CallState {
    ImsHoldingVideo(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        Log.i("CallStateMachine", "Enter [HoldingVideo]");
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[HoldingVideo] processMessage " + msg.what);
        int i = msg.what;
        if (i == 51) {
            Log.i("CallStateMachine", "[HoldingVideo] defer hold request.");
            this.mCsm.deferMessage(msg);
        } else if (i != 71) {
            switch (i) {
                case 80:
                    Log.i("CallStateMachine", "[HoldingVideo] defer hold video request.");
                    this.mCsm.deferMessage(msg);
                    break;
                case 81:
                    Log.i("CallStateMachine", "[HoldingVideo] defer resume video request.");
                    this.mCsm.isDeferedVideoResume = true;
                    this.mCsm.deferMessage(msg);
                    break;
                case 82:
                    Log.i("CallStateMachine", "[HoldingVideo] Video held.");
                    this.mCsm.notifyOnModified(this.mSession.getCallProfile().getCallType());
                    this.mCsm.transitionTo(this.mCsm.mVideoHeld);
                    break;
                case 83:
                    Log.i("CallStateMachine", "[HoldingVideo] do not handle video resumed");
                    break;
                case 84:
                    Log.i("CallStateMachine", "[HoldingVideo] video hold failed, Try again");
                    this.mCsm.transitionTo(this.mCsm.mInCall);
                    this.mCsm.sendMessageDelayed(80, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    break;
                default:
                    return false;
            }
        } else {
            Log.i("CallStateMachine", "[HoldingVideo] defer resume request.");
            this.mCsm.deferMessage(msg);
        }
        return true;
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }
}
