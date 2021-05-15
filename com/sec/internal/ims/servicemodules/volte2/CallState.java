package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Looper;
import android.os.RemoteCallbackList;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.State;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;

public class CallState extends State {
    protected static final String LOG_TAG = "CallStateMachine";
    protected Context mContext = null;
    CallStateMachine mCsm = null;
    protected RemoteCallbackList<IImsCallSessionEventListener> mListeners = null;
    protected IImsMediaController mMediaController = null;
    protected Mno mMno = Mno.DEFAULT;
    protected IVolteServiceModuleInternal mModule = null;
    protected ImsRegistration mRegistration = null;
    protected IRegistrationManager mRegistrationManager = null;
    protected ImsCallSession mSession = null;
    protected ITelephonyManager mTelephonyManager;
    protected IVolteServiceInterface mVolteSvcIntf = null;

    CallState(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
        this.mContext = context;
        this.mVolteSvcIntf = stackIf;
        this.mMno = mno;
        this.mCsm = csm;
        this.mSession = session;
        this.mRegistration = reg;
        this.mModule = volteModule;
        this.mRegistrationManager = rm;
        this.mMediaController = mediactnr;
        this.mListeners = listener;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
    }
}
