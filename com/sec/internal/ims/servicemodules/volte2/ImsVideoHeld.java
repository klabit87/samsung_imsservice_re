package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsVideoHeld extends CallState {
    ImsVideoHeld(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        if (!this.mModule.getSessionByState(this.mSession.getPhoneId(), CallConstants.STATE.IncomingCall).isEmpty() && this.mSession.getCallProfile().getCallType() != 4) {
            this.mSession.stopCamera();
        }
        this.mMediaController.setVideoPause(this.mSession.getSessionId(), true);
        Log.i("CallStateMachine", "Enter [VideoHeld]");
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", "[VideoHeld] processMessage " + msg.what);
        int i = msg.what;
        if (i == 80) {
            Log.i("CallStateMachine", "[VideoHeld] already held. ignore.");
            return true;
        } else if (i != 81) {
            return false;
        } else {
            if (this.mMno != Mno.VZW) {
                return true;
            }
            int cameraId = this.mCsm.determineCamera(this.mSession.getCallProfile().getCallType(), false);
            if (cameraId >= 0) {
                this.mSession.startCamera(cameraId);
            }
            this.mCsm.isDeferedVideoResume = false;
            this.mMediaController.resumeVideo(this.mSession.getSessionId());
            this.mCsm.transitionTo(this.mCsm.mResumingVideo);
            return true;
        }
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
    }
}
