package com.sec.internal.ims.servicemodules.volte2;

import android.os.Message;
import android.util.Log;
import com.sec.epdg.EpdgManager;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;

public class ImsCallDedicatedBearer {
    private static final String LOG_TAG = ImsCallDedicatedBearer.class.getSimpleName();
    private PreciseAlarmManager mAm = null;
    private boolean mIsDRBLost = false;
    private Mno mMno = Mno.DEFAULT;
    private IVolteServiceModuleInternal mModule = null;
    private ImsRegistration mRegistration = null;
    private IRegistrationManager mRegistrationManager = null;
    private int mRttBearerState = 3;
    private Message mRttDedicatedBearerTimeoutMessage = null;
    private ImsCallSession mSession = null;
    private int mVideoBearerState = 3;
    private int mVideoNGbrBearerState = 3;
    private int mVoiceBearerState = 3;
    private CallStateMachine smCallStateMachine = null;

    public ImsCallDedicatedBearer(ImsCallSession session, IVolteServiceModuleInternal volteModule, ImsRegistration reg, IRegistrationManager rm, Mno mno, PreciseAlarmManager am, CallStateMachine csm) {
        this.mSession = session;
        this.mModule = volteModule;
        this.mRegistration = reg;
        this.mRegistrationManager = rm;
        this.mMno = mno;
        this.smCallStateMachine = csm;
        this.mAm = am;
    }

    private boolean isIgnoredDedicatedBearLost(int qci) {
        if ((qci == 99 || qci == 1) && this.mMno == Mno.ATT) {
            return true;
        }
        if (qci == 99 || (this.mMno != Mno.VZW && !this.mMno.isKor() && this.mMno != Mno.TELENOR_NORWAY && this.mMno != Mno.SFR && this.mMno != Mno.TELE2NL && this.mMno != Mno.CLARO_PERU && this.mMno != Mno.ENTEL_PERU && this.mMno != Mno.SMARTFREN && this.mMno != Mno.CABLE_PANAMA)) {
            return false;
        }
        return true;
    }

    private void onDedicatedBearerLost(int qci) {
        EpdgManager epdgManager;
        if (isIgnoredDedicatedBearLost(qci)) {
            String str = LOG_TAG;
            Log.i(str, "onDedicatedBearerLost: ignore DBR lost for mno:" + this.mMno + " qci:" + qci);
            return;
        }
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mSession.getPhoneId());
        if (sm != null && sm.isSimAvailable() && this.mRegistrationManager.isVoWiFiSupported(this.mSession.getPhoneId()) && (((epdgManager = this.mModule.getEpdgManager()) != null && epdgManager.isDuringHandoverForIMSBySim(this.mSession.getPhoneId())) || this.mSession.isEpdgCall())) {
            String str2 = LOG_TAG;
            Log.i(str2, "onDedicatedBearerLost: ignore Dedicated Bearer Lost due to EPDG for mno:" + this.mMno + ", qci:" + qci);
        } else if (this.mMno != Mno.KDDI || !this.smCallStateMachine.mConfCallAdded) {
            String str3 = LOG_TAG;
            Log.i(str3, "onDedicatedBearerLost: Dedicated Bearer Lost mno:" + this.mMno + ", qci:" + qci);
            if (this.mMno.isChn()) {
                if (this.mSession.getCallState() == CallConstants.STATE.InCall) {
                    this.mIsDRBLost = true;
                    this.smCallStateMachine.sendMessageDelayed(5000, qci, 20);
                    return;
                }
                this.smCallStateMachine.sendMessage(5000, qci);
            } else if (qci == 99) {
                Message obtainMessage = this.smCallStateMachine.obtainMessage(210);
                this.mRttDedicatedBearerTimeoutMessage = obtainMessage;
                this.smCallStateMachine.sendMessageDelayed(obtainMessage, 500);
            } else {
                this.mIsDRBLost = true;
                this.smCallStateMachine.sendMessageDelayed(5000, qci, 1000);
            }
        } else {
            String str4 = LOG_TAG;
            Log.i(str4, "onDedicatedBearerLost: igonre dedicated Bearer Lost mno:" + this.mMno + " after ending 3way conference call");
        }
    }

    public void setDedicatedBearerState(int qci, int state) {
        String str = LOG_TAG;
        Log.i(str, "qci:" + qci + ", state:" + state);
        if (qci == 1) {
            if (this.mVoiceBearerState != 3 && state == 3) {
                onDedicatedBearerLost(qci);
            }
            this.mVoiceBearerState = state;
        } else if (qci == 2 || qci == 3) {
            if ((this.mMno == Mno.CTC || this.mMno == Mno.CU || this.mMno == Mno.CTCMO) && this.mVideoBearerState != 3 && state == 3) {
                onDedicatedBearerLost(qci);
            }
            this.mVideoBearerState = state;
        } else if (qci == 7 || qci == 8 || qci == 9) {
            this.mVideoNGbrBearerState = state;
        } else if (qci == 99) {
            if (this.mRttBearerState == 3 && state == 1) {
                this.mSession.stopRttDedicatedBearerTimer();
            } else if (this.mRttBearerState == 1 && state == 3) {
                onDedicatedBearerLost(qci);
            }
            this.mRttBearerState = state;
        }
    }

    public int getDedicatedBearerState(int qci) {
        int state = 3;
        if (qci == 1) {
            state = this.mVoiceBearerState;
        } else if (qci == 2 || qci == 3) {
            state = this.mVideoBearerState;
        } else if (qci == 7 || qci == 8 || qci == 9) {
            state = this.mVideoNGbrBearerState;
        } else if (qci != 99) {
            String str = LOG_TAG;
            Log.i(str, "unknown qci:" + qci);
        } else {
            state = this.mRttBearerState;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "qci:" + qci + ", state:" + state);
        return state;
    }

    /* access modifiers changed from: protected */
    public void startRttDedicatedBearerTimer(long millis) {
        if (millis <= 0) {
            String str = LOG_TAG;
            Log.i(str, "startRttDedicatedBearerTimer: Not start RttDedicatedBearerTimer : millis = " + millis);
        } else if (this.mMno == Mno.ATT || this.mSession.mIsNrSaMode) {
            Log.i(LOG_TAG, "startRttDedicatedBearerTimer: Not start RttDedicatedBearerTimer for ATT");
        } else {
            ImsRegistration imsRegistration = this.mRegistration;
            if (imsRegistration != null && imsRegistration.getImsProfile() != null && this.mRegistration.getImsProfile().getUsePrecondition() == 0) {
                Log.i(LOG_TAG, "startRttDedicatedBearerTimer: Not start RttDedicatedBearerTimer");
            } else if (getDedicatedBearerState(99) != 3) {
                Log.i(LOG_TAG, "RTT Dedicated Bearer opened");
            } else if (this.mRttDedicatedBearerTimeoutMessage != null) {
                Log.i(LOG_TAG, "RTT Dedicated Bearer Timer already has been started");
            } else {
                String str2 = LOG_TAG;
                Log.i(str2, "startRttDedicatedBearerTimer: " + millis);
                stopRttDedicatedBearerTimer();
                this.mRttDedicatedBearerTimeoutMessage = this.smCallStateMachine.obtainMessage(209);
                this.mAm.sendMessageDelayed(getClass().getSimpleName(), this.mRttDedicatedBearerTimeoutMessage, millis);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopRttDedicatedBearerTimer() {
        if (this.mRttDedicatedBearerTimeoutMessage != null) {
            Log.i(LOG_TAG, "stopRttDedicatedBearerTimer: ");
            this.mAm.removeMessage(this.mRttDedicatedBearerTimeoutMessage);
            this.mRttDedicatedBearerTimeoutMessage = null;
        }
    }

    /* access modifiers changed from: protected */
    public void setRttDedicatedBearerTimeoutMessage(Message msg) {
        this.mRttDedicatedBearerTimeoutMessage = msg;
    }

    public boolean getDRBLost() {
        return this.mIsDRBLost;
    }

    public void setDRBLost(boolean losted) {
        this.mIsDRBLost = losted;
    }
}
