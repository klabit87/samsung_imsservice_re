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

public class ImsResumingVideo extends CallState {
    ImsResumingVideo(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        Log.i("CallStateMachine", "Enter [ResumingVideo]");
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[ResumingVideo] processMessage " + msg.what);
        int i = msg.what;
        if (i == 51) {
            Log.i("CallStateMachine", "[ResumingVideo] defer HOLD request.");
            this.mCsm.deferMessage(msg);
            return true;
        } else if (i == 71) {
            Log.i("CallStateMachine", "[ResumingVideo] defer RESUME request.");
            this.mCsm.deferMessage(msg);
            return true;
        } else if (i == 85) {
            Log.i("CallStateMachine", "[ResumingVideo] video resume failed, Try again");
            this.mCsm.transitionTo(this.mCsm.mInCall);
            this.mCsm.sendMessageDelayed(81, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            return true;
        } else if (i != 502) {
            switch (i) {
                case 80:
                    Log.i("CallStateMachine", "[ResumingVideo] defer HOLD_VIDEO request.");
                    this.mCsm.deferMessage(msg);
                    return true;
                case 81:
                    Log.i("CallStateMachine", "[ResumingVideo] defer RESUME_VIDEO request.");
                    this.mCsm.deferMessage(msg);
                    return true;
                case 82:
                    Log.i("CallStateMachine", "[ResumingVideo] Video held by remote.");
                    this.mCsm.notifyOnModified(this.mSession.getCallProfile().getCallType());
                    this.mCsm.transitionTo(this.mCsm.mInCall);
                    return true;
                case 83:
                    Log.i("CallStateMachine", "[ResumingVideo] Video resumed.");
                    this.mCsm.notifyOnModified(this.mSession.getCallProfile().getCallType());
                    this.mCsm.transitionTo(this.mCsm.mInCall);
                    return true;
                default:
                    return false;
            }
        } else if (this.mMno == Mno.ROGERS) {
            Log.i("CallStateMachine", "[ResumingVideo] defer RE_INVITE request.");
            this.mCsm.deferMessage(msg);
            return true;
        } else {
            Log.i("CallStateMachine", "[ResumingVideo] ignore re-INVITE");
            return true;
        }
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }
}
