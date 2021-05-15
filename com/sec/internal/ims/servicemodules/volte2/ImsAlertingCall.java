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
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipReason;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class ImsAlertingCall extends CallState {
    ImsAlertingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
    }

    public void enter() {
        this.mCsm.callType = 0;
        this.mCsm.errorCode = -1;
        this.mCsm.errorMessage = "";
        Log.i("CallStateMachine", "Enter [AlertingCall]");
        if ((this.mMno == Mno.CMCC || this.mMno == Mno.VIVA_BAHRAIN || this.mMno == Mno.ETISALAT_UAE) && this.mSession.mKaSender != null) {
            this.mSession.mKaSender.start();
        }
        int cameraId = this.mCsm.determineCamera(this.mSession.getCallProfile().getCallType(), false);
        if (cameraId >= 0) {
            this.mSession.startCamera(cameraId);
        }
    }

    public boolean processMessage(Message msg) {
        Log.i("CallStateMachine", " [AlertingCall] processMessage " + msg.what);
        int i = msg.what;
        if (i != 1) {
            if (i == 26) {
                this.mCsm.notifyOnError(503, "Session Progress Timeout", 0);
                this.mCsm.transitionTo(this.mCsm.mEndingCall);
            } else if (i == 52) {
                update_AlertingCall(msg);
            } else if (i == 56) {
                Bundle dtmfData = (Bundle) msg.obj;
                this.mVolteSvcIntf.handleDtmf(this.mSession.getSessionId(), dtmfData.getInt(AuthenticationHeaders.HEADER_PARAM_CODE), dtmfData.getInt("mode"), dtmfData.getInt("operation"), (Message) dtmfData.getParcelable("result"));
            } else if (i == 64) {
                Bundle bundle1 = (Bundle) msg.obj;
                String text = bundle1.getString("text");
                int len = bundle1.getInt("len");
                Log.i("CallStateMachine", "text=" + text + ", len=" + len);
                this.mVolteSvcIntf.sendText(this.mSession.getSessionId(), text, len);
            } else if (i != 100) {
                if (i == 204) {
                    Log.i("CallStateMachine", "ringback timer expired.");
                    this.mCsm.sendMessage(1, 1802, 0, "Ringback timer expired");
                } else if (i != 400) {
                    if (i == 502) {
                        Log.i("CallStateMachine", "[AlertingCall] Re-INVITE defered");
                        this.mCsm.deferMessage(msg);
                    } else if (i != 5000) {
                        if (i != 3) {
                            if (i == 4) {
                                return error_AlertingCall(msg);
                            }
                            if (i == 41) {
                                established_AlertingCall();
                            } else if (i != 42) {
                                if (!(i == 93 || i == 94)) {
                                    if (i != 209 && i != 210) {
                                        switch (i) {
                                            case 31:
                                                Log.i("CallStateMachine", "response from network by re-invite. do nothing.");
                                                break;
                                            case 32:
                                                earlymedia_AlertingCall(msg);
                                                break;
                                            case 33:
                                                Log.i("CallStateMachine", "Ignore.");
                                                break;
                                            case 34:
                                                ringingBack_AlertingCall();
                                                break;
                                            case 35:
                                                if (this.mMno == Mno.TMOUS) {
                                                    sessionProgress_AlertingCall(this.mSession.getCallProfile().getAudioEarlyMediaDir());
                                                    break;
                                                }
                                                break;
                                            case 36:
                                                forwarded_AlertingCall();
                                                break;
                                            default:
                                                Log.e("CallStateMachine", "[" + getName() + "] msg:" + msg.what + " ignored !!!");
                                                break;
                                        }
                                    } else {
                                        Log.i("CallStateMachine", "[AlertingCall] deferMessage Downgrade Rtt to voice call");
                                        this.mCsm.deferMessage(msg);
                                    }
                                }
                            } else {
                                this.mCsm.transitionTo(this.mCsm.mEndingCall);
                                this.mCsm.sendMessage(3);
                            }
                        }
                    } else if (dbrLost_AlertingCall()) {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void exit() {
        this.mCsm.setPreviousState(this);
        if (this.mSession.mKaSender != null) {
            this.mSession.mKaSender.stop();
        }
        this.mCsm.stopRingTimer();
    }

    private void ringingBack_AlertingCall() {
        if (this.mRegistration != null) {
            this.mCsm.startRingTimer(((long) this.mRegistration.getImsProfile().getRingbackTimer()) * 1000);
        }
        this.mCsm.notifyOnRingingBack();
    }

    /* access modifiers changed from: protected */
    public void sessionProgress_AlertingCall(int audioEarlyMediaDir) {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onSessionProgress(audioEarlyMediaDir);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    private void established_AlertingCall() {
        IRegistrationGovernor regGov;
        int i;
        this.mCsm.transitionTo(this.mCsm.mInCall);
        if (this.mRegistration != null && (regGov = this.mRegistrationManager.getRegistrationGovernor(this.mRegistration.getHandle())) != null) {
            IRegistrationGovernor.CallEvent callEvent = IRegistrationGovernor.CallEvent.EVENT_CALL_ESTABLISHED;
            if (this.mSession.getCallProfile().isDowngradedVideoCall()) {
                i = 2;
            } else {
                i = this.mSession.getCallProfile().getCallType();
            }
            regGov.onCallStatus(callEvent, (SipError) null, i);
        }
    }

    private boolean dbrLost_AlertingCall() {
        if (this.mMno != Mno.VIVA_KUWAIT && this.mMno != Mno.TELEFONICA_GERMANY && this.mMno != Mno.ETISALAT_UAE && this.mMno != Mno.TELE2_SWE) {
            return false;
        }
        Log.e("CallStateMachine", "[AlertingCall] processMessage DBR LOST ignored!");
        return true;
    }

    private boolean error_AlertingCall(Message msg) {
        this.mCsm.handleSPRoutgoingError(msg);
        if (!this.mCsm.mIsWPSCall) {
            return false;
        }
        CallStateMachine callStateMachine = this.mCsm;
        CallStateMachine callStateMachine2 = this.mCsm;
        callStateMachine.sendMessageDelayed(26, (long) UtStateMachine.HTTP_READ_TIMEOUT_GCF);
        return true;
    }

    private void earlymedia_AlertingCall(Message msg) {
        Log.i("CallStateMachine", "mSession.getCallProfile().isVideoCRBT: " + this.mSession.getCallProfile().isVideoCRBT());
        if (this.mRegistration != null && this.mSession.getCallProfile().isVideoCRBT()) {
            this.mVolteSvcIntf.startVideoEarlyMedia(this.mSession.getSessionId());
        }
        this.mCsm.notifyOnEarlyMediaStarted(msg.arg1);
    }

    private void update_AlertingCall(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        CallProfile profile = bundle.getParcelable("profile");
        int srvccVersion = this.mModule.getSrvccVersion(this.mSession.getPhoneId());
        if (profile == null && srvccVersion != 0) {
            if (srvccVersion >= 10 || DeviceUtil.getGcfMode()) {
                Log.i("CallStateMachine", "MO aSRVCC supported");
                this.mVolteSvcIntf.sendReInvite(this.mSession.getSessionId(), new SipReason("SIP", bundle.getInt("cause"), bundle.getString("reasonText"), new String[0]));
            }
        }
    }

    private void forwarded_AlertingCall() {
        this.mCsm.stopRingTimer();
        if (!this.mMno.isKor()) {
            this.mCsm.notifyOnCallForwarded();
        }
    }
}
