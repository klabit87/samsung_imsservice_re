package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.Dialog;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.IImsMediaCallProvider;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.CmcInfoEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.RrcConnectionEvent;
import com.sec.internal.constants.ims.servicemodules.volte2.UssdEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;

public class ImsCallSession extends IImsCallSession.Stub {
    protected static final int EVT_IMS_CALL_EVENT = 1;
    protected static final int EVT_IMS_MEDIA_EVENT = 2;
    protected static final int EVT_REFER_STATUS = 5;
    protected static final int EVT_RRC_CONNECTION_EVENT = 3;
    protected static final int EVT_USSD_EVENT = 4;
    /* access modifiers changed from: private */
    public String LOG_TAG = "ImsCallSession";
    protected PreciseAlarmManager mAm = null;
    protected int mCallId = -1;
    protected CallProfile mCallProfile = null;
    protected int mCmcType = 0;
    protected Context mContext = null;
    private int mDtmfCode = 0;
    private int mEndReason = 0;
    private int mEndType = 0;
    protected boolean mHandOffTimedOut = false;
    protected ImsCallDedicatedBearer mImsCallDedicatedBearer = null;
    protected ImsCallSessionEventHandler mImsCallSessionEventHandler = null;
    private boolean mIsCameraStartByAPP = false;
    protected boolean mIsEstablished = false;
    protected boolean mIsNrSaMode = false;
    private boolean mIsUsingCamera = false;
    protected KeepAliveSender mKaSender = null;
    protected int mLastUsedCamera = -1;
    protected final RemoteCallbackList<IImsCallSessionEventListener> mListeners = new RemoteCallbackList<>();
    protected boolean mLocalHoldTone = true;
    protected final Looper mLooper;
    protected IImsMediaController mMediaController = null;
    protected Mno mMno = Mno.DEFAULT;
    protected CallProfile mModifyRequestedProfile = null;
    protected IVolteServiceModuleInternal mModule = null;
    private boolean mNeedToLateEndedNotify = false;
    protected boolean mOldLocalHoldTone = true;
    protected int mPhoneId = 0;
    protected int mPrevUsedCamera = -1;
    protected ImsRegistration mRegistration = null;
    protected IRegistrationManager mRegistrationManager = null;
    protected int mResumeCallRetriggerTimer = 0;
    protected int mSessionId = -1;
    private ITelephonyManager mTelephonyManager;
    private boolean mUserCameraOff = false;
    protected Handler mUssdStackEventHandler = null;
    private int mVideoCrbtSupportType = 0;
    protected Handler mVolteStackEventHandler = null;
    protected IVolteServiceInterface mVolteSvcIntf = null;
    protected CallStateMachine smCallStateMachine = null;

    public void setDedicatedBearerState(int qci, int state) {
        this.mImsCallDedicatedBearer.setDedicatedBearerState(qci, state);
    }

    public int getDedicatedBearerState(int qci) {
        return this.mImsCallDedicatedBearer.getDedicatedBearerState(qci);
    }

    public void setPreAlerting() {
        this.smCallStateMachine.setPreAlerting();
    }

    public boolean getPreAlerting() {
        return this.smCallStateMachine.getPreAlerting();
    }

    public void onReceiveSIPMSG(String sipMessage, boolean isRequest) {
        if (!TextUtils.isEmpty(sipMessage)) {
            this.smCallStateMachine.onReceiveSIPMSG(sipMessage, isRequest);
        }
    }

    /* access modifiers changed from: protected */
    public void notifySessionChanged(int callId) {
        int length = this.mListeners.beginBroadcast();
        String str = this.LOG_TAG;
        Log.i(str, "onSessionChanged callId=" + callId + ":" + length);
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onSessionChanged(callId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    public ImsCallSession(Context context, CallProfile profile, ImsRegistration reg, Looper looper, IVolteServiceModuleInternal module) {
        this.mContext = context;
        this.mModule = module;
        this.mCallProfile = profile;
        this.mRegistration = reg;
        this.mLooper = looper;
        this.mAm = PreciseAlarmManager.getInstance(context);
        this.mVolteStackEventHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int i = msg.what;
                if (i == 1) {
                    ImsCallSession.this.onImsCallEvent((CallStateEvent) ((AsyncResult) msg.obj).result);
                } else if (i == 2) {
                    ImsCallSession.this.mImsCallSessionEventHandler.onImsMediaEvent((IMSMediaEvent) msg.obj);
                } else if (i == 3) {
                    RrcConnectionEvent rrcEvent = (RrcConnectionEvent) ((AsyncResult) msg.obj).result;
                    String access$000 = ImsCallSession.this.LOG_TAG;
                    Log.i(access$000, "rrcEvent.getEvent() : " + rrcEvent.getEvent());
                    if (ImsCallSession.this.mMno != Mno.VZW && ImsCallSession.this.mMno != Mno.DOCOMO && ImsCallSession.this.mMno != Mno.SWISSCOM) {
                        return;
                    }
                    if (rrcEvent.getEvent() == RrcConnectionEvent.RrcEvent.REJECTED || rrcEvent.getEvent() == RrcConnectionEvent.RrcEvent.TIMER_EXPIRED) {
                        ImsCallSession.this.smCallStateMachine.sendMessage(401);
                    }
                } else if (i == 5) {
                    ImsCallSession.this.mImsCallSessionEventHandler.onReferStatus((AsyncResult) msg.obj);
                }
            }
        };
        this.mUssdStackEventHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                AsyncResult result = (AsyncResult) msg.obj;
                if (msg.what == 4) {
                    ImsCallSession.this.mImsCallSessionEventHandler.onUssdEvent((UssdEvent) result.result);
                }
            }
        };
        this.mMediaController = this.mModule.getImsMediaController();
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        setPhoneId(this.mRegistration);
        if (this.mMediaController.isSupportingCameraMotor()) {
            this.mIsCameraStartByAPP = true;
        }
    }

    public void init(IVolteServiceInterface stackIf, IRegistrationManager rm) {
        ImsRegistration imsRegistration;
        this.mVolteSvcIntf = stackIf;
        this.mRegistrationManager = rm;
        ImsRegistration imsRegistration2 = this.mRegistration;
        if (imsRegistration2 != null) {
            if (imsRegistration2.getImsProfile().isSamsungMdmnEnabled()) {
                this.mMno = Mno.MDMN;
                Log.i(this.LOG_TAG, "init(): this is MDMN call");
                this.mCallProfile.setSamsungMdmnCall(true);
                CallProfile callProfile = this.mCallProfile;
                callProfile.setOriginatingUri(ImsUri.parse("sip:" + this.mRegistration.getImpi()));
            } else {
                this.mMno = Mno.fromName(this.mRegistration.getImsProfile().getMnoName());
            }
            this.mCmcType = this.mRegistration.getImsProfile().getCmcType();
        } else {
            this.mMno = SimUtil.getSimMno(this.mPhoneId);
        }
        CallStateMachine callStateMachine = new CallStateMachine(this.mContext, this, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, "CallStateMachine", this.mLooper);
        this.smCallStateMachine = callStateMachine;
        callStateMachine.init();
        Log.i(this.LOG_TAG, "start CallStatMachine");
        this.smCallStateMachine.start();
        this.mImsCallDedicatedBearer = new ImsCallDedicatedBearer(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine);
        this.mImsCallSessionEventHandler = new ImsCallSessionEventHandler(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine, this.mCallProfile, this.mVolteSvcIntf, this.mMediaController);
        this.mVolteSvcIntf.registerForCallStateEvent(this.mVolteStackEventHandler, 1, (Object) null);
        this.mVolteSvcIntf.registerForRrcConnectionEvent(this.mVolteStackEventHandler, 3, (Object) null);
        this.mVolteSvcIntf.registerForUssdEvent(this.mUssdStackEventHandler, 4, (Object) null);
        this.mVolteSvcIntf.registerForReferStatus(this.mVolteStackEventHandler, 5, this);
        this.mMediaController.registerForMediaEvent(this);
        if ((this.mMno == Mno.VZW || this.mMno == Mno.CMCC || this.mMno == Mno.VIVA_BAHRAIN || this.mMno == Mno.ETISALAT_UAE) && (imsRegistration = this.mRegistration) != null) {
            int port = 5060;
            String[] pcscfInfo = this.mRegistrationManager.getCurrentPcscf(imsRegistration.getHandle());
            if (pcscfInfo != null) {
                try {
                    port = Integer.parseInt(pcscfInfo[1]);
                } catch (NumberFormatException e) {
                    Log.i(this.LOG_TAG, "use default port 5060");
                }
                this.mKaSender = new KeepAliveSender(this.mContext, this.mRegistration, pcscfInfo[0], port, this.mMno);
                if (this.mMno != Mno.VZW) {
                    this.mResumeCallRetriggerTimer = 1500;
                }
            }
        }
        setIsNrSaMode();
    }

    public void setIsNrSaMode() {
        NetworkEvent ne = this.mModule.getNetwork(getPhoneId());
        this.mIsNrSaMode = this.mMno == Mno.TMOUS && ne != null && ne.network == 20;
        String str = this.LOG_TAG;
        Log.i(str, "mIsNrSaMode = " + this.mIsNrSaMode);
    }

    public void setPendingCall(boolean isPending) {
        this.smCallStateMachine.setPendingCall(isPending);
    }

    public boolean isEpdgCall() {
        IPdnController pc = ImsRegistry.getPdnController();
        boolean isEpdgCall = pc.isEpdgConnected(this.mPhoneId);
        if (!ImsCallUtil.isE911Call(this.mCallProfile.getCallType())) {
            return isEpdgCall;
        }
        int emergencyPdnPolicy = ImsRegistry.getInt(this.mPhoneId, GlobalSettingsConstants.Call.E911_PDN_SELECTION_VOWIFI, 0);
        if (emergencyPdnPolicy == 0) {
            Log.i(this.LOG_TAG, "use isEmergencyEpdgConnected for EPDN");
            return pc.isEmergencyEpdgConnected(this.mPhoneId);
        } else if (emergencyPdnPolicy != 1) {
            return isEpdgCall;
        } else {
            Log.i(this.LOG_TAG, "use EmergemcyRat for IPC_RAT_EPDG");
            return "VoWIFI".equalsIgnoreCase(this.mCallProfile.getEmergencyRat());
        }
    }

    public void setCallId(int callId) {
        this.mCallId = callId;
    }

    public int getCallId() {
        return this.mCallId;
    }

    public void setSessionId(int sessionId) {
        String str = this.LOG_TAG + "(" + sessionId + ")";
        this.LOG_TAG = str;
        Log.i(str, "Session ID : [" + sessionId + "]");
        this.mSessionId = sessionId;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public void setCmcType(int cmcType) {
        this.mCmcType = cmcType;
    }

    public int getCmcType() {
        return this.mCmcType;
    }

    public void setVideoCrbtSupportType(int videoCrbtSupportType) {
        this.mVideoCrbtSupportType = videoCrbtSupportType;
    }

    public void setIsEstablished(boolean isEstablished) {
        this.mIsEstablished = isEstablished;
    }

    public boolean getIsEstablished() {
        return this.mIsEstablished;
    }

    public int getVideoCrbtSupportType() {
        return this.mVideoCrbtSupportType;
    }

    public CallConstants.STATE getCallState() {
        return this.smCallStateMachine.getState();
    }

    public int getCallStateOrdinal() {
        return this.smCallStateMachine.getState().ordinal();
    }

    public int getPrevCallStateOrdinal() {
        CallStateMachine callStateMachine = this.smCallStateMachine;
        return callStateMachine.getPreviousStateByName(callStateMachine.getPreviousState().getName()).ordinal();
    }

    public CallProfile getCallProfile() {
        return this.mCallProfile;
    }

    public CallProfile getModifyRequestedProfile() {
        return this.mModifyRequestedProfile;
    }

    public synchronized boolean getUsingCamera() {
        return this.mIsUsingCamera;
    }

    public synchronized void setUsingCamera(boolean use) {
        this.mIsUsingCamera = use;
    }

    public void setUserCameraOff(boolean value) {
        String str = this.LOG_TAG;
        Log.i(str, "setUserCameraOff : " + value);
        this.mUserCameraOff = value;
    }

    public void setHoldBeforeTransfer(boolean value) {
        this.smCallStateMachine.mHoldBeforeTransfer = value;
    }

    public void registerSessionEventListener(IImsCallSessionEventListener listener) throws RemoteException {
        Log.i(this.LOG_TAG, "registerListener");
        this.mListeners.register(listener);
        if (this.mNeedToLateEndedNotify) {
            try {
                CallConstants.STATE callState = this.smCallStateMachine.getState();
                if (callState == CallConstants.STATE.EndingCall || callState == CallConstants.STATE.EndedCall) {
                    Log.e(this.LOG_TAG, "notify the ended call for a late registered session.");
                    this.smCallStateMachine.notifyOnEnded(getErrorCode());
                }
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
    }

    public void unregisterSessionEventListener(IImsCallSessionEventListener listener) throws RemoteException {
        Log.i(this.LOG_TAG, "deregisterListener");
        this.mListeners.unregister(listener);
    }

    public void replaceSessionEventListener(ImsCallSession origSession) {
        int length = origSession.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            this.mListeners.register(origSession.mListeners.getBroadcastItem(i));
        }
        origSession.mListeners.finishBroadcast();
    }

    public int start(String target, CallProfile profile) throws RemoteException {
        IMSLog.LAZER_TYPE lazer_type = IMSLog.LAZER_TYPE.CALL;
        IMSLog.lazer(lazer_type, getCallId() + " - START OUTGOING");
        if (target != null) {
            String deviceId = null;
            if (this.mRegistration != null && this.mCallProfile.isSamsungMdmnCall()) {
                if (!isCmcPrimaryType(this.mRegistration.getImsProfile().getCmcType())) {
                    this.mCallProfile.setLetteringText(target);
                    deviceId = this.mRegistration.getImsProfile().getPriDeviceIdWithURN();
                } else {
                    deviceId = this.mCallProfile.getDialingDevice();
                }
                target = this.mRegistration.getImpi();
                this.mCallProfile.setDialingNumber(target);
                String str = this.LOG_TAG;
                Log.i(str, "start(): this is MDMN call / target=" + target + " / Lettering=" + this.mCallProfile.getLetteringText());
            }
            if (this.mMno == Mno.TMOUS) {
                if (!target.startsWith("*67*") && target.startsWith("*67")) {
                    this.mCallProfile.setCLI("*67");
                    target = target.substring(3);
                    String str2 = this.LOG_TAG;
                    Log.i(str2, "Start(): *67 Call : cli=*67 target=" + target);
                } else if (target.startsWith("*82")) {
                    this.mCallProfile.setCLI("*82");
                    target = target.substring(3);
                    String str3 = this.LOG_TAG;
                    Log.i(str3, "Start(): *82 Call : cli=*82 target=" + target);
                }
            }
            if (TextUtils.isEmpty(this.mCallProfile.getDialingNumber())) {
                this.mCallProfile.setDialingNumber(target);
            }
            if (ImsCallUtil.isE911Call(this.mCallProfile.getCallType()) && ImsUtil.isSimMobilityActivated(this.mPhoneId)) {
                CallProfile callProfile = this.mCallProfile;
                callProfile.setUrn(this.mModule.updateEccUrn(this.mPhoneId, callProfile.getDialingNumber()));
                String str4 = this.LOG_TAG;
                Log.i(str4, "UpdateECCUrn : " + this.mCallProfile.getUrn());
            }
            ImsRegistration imsRegistration = this.mRegistration;
            if (imsRegistration == null || !imsRegistration.getImsProfile().isSoftphoneEnabled() || this.mCallProfile.getCallType() != 13) {
                CallStateMachine callStateMachine = this.smCallStateMachine;
                callStateMachine.sendMessage(callStateMachine.obtainMessage(11, (Object) deviceId));
            } else {
                this.mCallProfile.setCLI("*31#");
                if (target.length() > 3 && (target.startsWith("*82") || (!target.startsWith("*67*") && target.startsWith("*67")))) {
                    Log.i(this.LOG_TAG, "Remove CLI code for Softphone E911 call");
                    this.mCallProfile.setDialingNumber(target.substring(3));
                }
                this.smCallStateMachine.sendMessage(13);
            }
            return getCallId();
        }
        IMSLog.LAZER_TYPE lazer_type2 = IMSLog.LAZER_TYPE.CALL;
        IMSLog.lazer(lazer_type2, getCallId() + " - OUTGOING FAIL by target is empty");
        Log.e(this.LOG_TAG, "start(): target is NULL");
        throw new RemoteException("Cannot make call: target URI is null");
    }

    public void startIncoming() {
        this.smCallStateMachine.sendMessage(21);
    }

    public void accept(CallProfile profile) throws RemoteException {
        this.smCallStateMachine.sendMessage(22, (Object) profile);
    }

    public void reject(int reason) throws RemoteException {
        this.mEndType = 2;
        this.mEndReason = reason;
        this.smCallStateMachine.sendMessage(23, reason);
    }

    public void terminate(int reason) throws RemoteException {
        terminate(reason, false);
    }

    public void terminate(int reason, boolean localRelease) throws RemoteException {
        int i;
        if (!isEpdgCall() || reason != 8) {
            Log.i(this.LOG_TAG, "Local Release ? " + localRelease);
            if (localRelease) {
                i = 3;
            } else {
                i = 1;
            }
            this.mEndType = i;
            this.mEndReason = reason;
            if (this.smCallStateMachine.quit) {
                throw new RemoteException("Session already quitted");
            } else if (reason == 22) {
                this.smCallStateMachine.sendMessage(CallStateMachine.ON_EPDN_SETUP_FAIL, reason, this.mEndType);
            } else if (this.mCallProfile.getRejectCause() != 0) {
                this.smCallStateMachine.sendMessage(3, reason, this.mEndType);
            } else {
                this.smCallStateMachine.sendMessage(1, reason, this.mEndType);
            }
        } else {
            Log.i(this.LOG_TAG, "SRVCC Completed. But already switched to epdg, don't terminate call");
        }
    }

    public void hold(MediaProfile media) throws RemoteException {
        this.smCallStateMachine.sendMessage(51);
    }

    public void resume() throws RemoteException {
        this.smCallStateMachine.sendMessage(71);
    }

    public void update(CallProfile profile, int cause, String reasonText) throws RemoteException {
        Bundle bundle = new Bundle();
        bundle.putParcelable("profile", profile);
        bundle.putInt("cause", cause);
        bundle.putString("reasonText", reasonText);
        if (profile != null || isSrvccAvailable()) {
            this.smCallStateMachine.sendMessage(52, (Object) bundle);
        } else {
            Log.i(this.LOG_TAG, "SRVCC isn't available");
        }
    }

    public void setMute(boolean muted) throws RemoteException {
    }

    public void reinvite() throws RemoteException {
        this.smCallStateMachine.sendMessage(502);
    }

    public void recording(int cmd, String path) throws RemoteException {
    }

    public void transfer(String msisdn) throws RemoteException {
        this.mModule.transfer(this.mSessionId, msisdn);
    }

    public void cancelTransfer() throws RemoteException {
        Log.i(this.LOG_TAG, "cancelTransfer:");
        this.smCallStateMachine.sendMessage(60);
    }

    public void pushCall(String msisdn) {
        String str = this.LOG_TAG;
        Log.i(str, "transfer: msisdn=" + IMSLog.checker(msisdn));
        this.smCallStateMachine.sendMessage(59, (Object) msisdn);
    }

    public ImsRegistration getRegistration() {
        return this.mRegistration;
    }

    public void setEpdgState(boolean connected) {
        if (connected) {
            this.mCallProfile.setRadioTech(18);
        } else {
            this.mCallProfile.setRadioTech(14);
        }
        this.smCallStateMachine.sendMessage(400, (int) connected);
    }

    public void setEpdgStateNoNotify(boolean connected) {
        String str = this.LOG_TAG;
        Log.i(str, "setEpdgStateNoNotify: " + connected);
        if (connected) {
            this.mCallProfile.setRadioTech(18);
        } else {
            this.mCallProfile.setRadioTech(14);
        }
    }

    public int pulling(String msisdn, Dialog targetDialog) throws RemoteException {
        if (msisdn == null || targetDialog == null) {
            Log.e(this.LOG_TAG, "transfer(): target is NULL");
            throw new RemoteException("Cannot transfer call: target is empty");
        }
        this.mCallProfile.setDialingNumber(targetDialog.getRemoteNumber());
        this.mCallProfile.setConferenceCall(0);
        Bundle bundle = new Bundle();
        bundle.putString("msisdn", msisdn);
        bundle.putParcelable("targetDialog", targetDialog);
        ContentValues tfItem = new ContentValues();
        ImsRegistration imsRegistration = this.mRegistration;
        if (imsRegistration == null || !imsRegistration.getImsProfile().isSoftphoneEnabled()) {
            Log.i(this.LOG_TAG, "pulling for mdService MEP");
            tfItem.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_TOTAL_COUNT, 1);
            tfItem.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_MEP_COUNT, 1);
        } else {
            Log.i(this.LOG_TAG, "pulling for Softphone");
            tfItem.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_TOTAL_COUNT, 1);
            tfItem.put(DiagnosisConstants.DRPT_KEY_MULTIDEVICE_SOFTPHONE_COUNT, 1);
        }
        tfItem.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        ImsLogAgentUtil.storeLogToAgent(this.mPhoneId, this.mContext, "DRPT", tfItem);
        this.smCallStateMachine.sendMessage(12, (Object) bundle);
        return getCallId();
    }

    public void merge(int heldsessionId, int activesessionId) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void startConference(String[] participants, CallProfile profile) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void inviteParticipants(int participantId) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void removeParticipants(int participantId) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void inviteGroupParticipant(String participant) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void removeGroupParticipant(String participant) throws RemoteException {
        throw new RemoteException("Invalid IMS session.");
    }

    public void extendToConference(String[] participants) throws RemoteException {
        if (participants != null) {
            this.smCallStateMachine.sendMessage(73, (Object) participants);
        } else {
            Log.e(this.LOG_TAG, "extendToConference(): there is no participants");
            throw new RemoteException("Cannot extendToConference : participants is null");
        }
    }

    public int startECT(int type, String targetUri) throws RemoteException {
        return -1;
    }

    public int acceptECTRequest() throws RemoteException {
        return -1;
    }

    public int rejectECTRequest() throws RemoteException {
        return -1;
    }

    public void holdVideo() throws RemoteException {
        this.smCallStateMachine.sendMessage(80);
    }

    public void resumeVideo() throws RemoteException {
        this.smCallStateMachine.sendMessage(81);
    }

    public void startCameraForProvider(int cameraId) {
        if (this.mMno.isKor() && !this.mIsCameraStartByAPP && ((!isTPhoneRelaxMode() && getCallState() == CallConstants.STATE.IncomingCall) || getCallState() == CallConstants.STATE.ReadyToCall)) {
            Log.i(this.LOG_TAG, "do not trigger startCamera");
        } else if (this.mMno == Mno.DOCOMO || this.mMno.isKor() || (this.mIsCameraStartByAPP && cameraId != -1)) {
            Log.i(this.LOG_TAG, "startCamera called for DCM or KOR");
            boolean isVideoCall = ImsCallUtil.isVideoCall(getCallProfile().getCallType());
            boolean isModifyOngoing = getCallState() == CallConstants.STATE.ModifyingCall || getCallState() == CallConstants.STATE.ModifyRequested;
            if (isVideoCall || (!isVideoCall && isModifyOngoing)) {
                setUserCameraOff(false);
                startCamera(cameraId);
            }
        } else if (cameraId == -1) {
            Log.i(this.LOG_TAG, "startCamera called with dummy.txt.txt type");
            startLastUsedCamera();
        }
    }

    public void stopCameraForProvider(boolean isDummyCam) {
        if (this.mMno == Mno.DOCOMO || this.mMno.isKor()) {
            Log.i(this.LOG_TAG, "stopCamera called for DCM or KOR");
            boolean isVideoCall = ImsCallUtil.isVideoCall(getCallProfile().getCallType());
            boolean isModifyOngoing = getCallState() == CallConstants.STATE.ModifyingCall || getCallState() == CallConstants.STATE.ModifyRequested;
            if (isVideoCall || (!isVideoCall && isModifyOngoing)) {
                setUserCameraOff(true);
                if (getUsingCamera()) {
                    stopCamera();
                }
            }
        } else if (isDummyCam) {
            setUserCameraOff(true);
            if (getUsingCamera()) {
                stopCamera();
            }
        }
    }

    public void sendDtmf(int code, int duration, Message result) throws RemoteException {
        Bundle dtmfData = new Bundle();
        dtmfData.putInt(AuthenticationHeaders.HEADER_PARAM_CODE, code);
        dtmfData.putInt("mode", 0);
        dtmfData.putInt("operation", 1);
        dtmfData.putParcelable("result", result);
        this.smCallStateMachine.sendMessage(56, (Object) dtmfData);
    }

    public void sendText(String text, int len) {
        Bundle bundle = new Bundle();
        bundle.putInt("len", len);
        bundle.putString("text", text);
        this.smCallStateMachine.sendMessage(64, (Object) bundle);
    }

    public void startDtmf(int code) throws RemoteException {
        this.mDtmfCode = code;
        Bundle dtmfData = new Bundle();
        dtmfData.putInt(AuthenticationHeaders.HEADER_PARAM_CODE, code);
        dtmfData.putInt("mode", 1);
        dtmfData.putInt("operation", 1);
        this.smCallStateMachine.sendMessage(56, (Object) dtmfData);
    }

    public void stopDtmf() throws RemoteException {
        Bundle dtmfData = new Bundle();
        dtmfData.putInt(AuthenticationHeaders.HEADER_PARAM_CODE, this.mDtmfCode);
        dtmfData.putInt("mode", 1);
        dtmfData.putInt("operation", 2);
        this.smCallStateMachine.sendMessage(56, (Object) dtmfData);
        this.mDtmfCode = 0;
    }

    public void setTtyMode(int ttyMode) {
        Log.i(this.LOG_TAG, "setTtyMode: " + ttyMode);
        boolean desTty = false;
        boolean curTty = this.mCallProfile.getCallType() == 9 || this.mCallProfile.getCallType() == 11 || this.mCallProfile.getCallType() == 10 || this.mCallProfile.getCallType() == 14 || this.mCallProfile.getCallType() == 15;
        if (!(ttyMode == Extensions.TelecomManager.TTY_MODE_OFF || ttyMode == Extensions.TelecomManager.RTT_MODE)) {
            desTty = true;
        }
        Log.i(this.LOG_TAG, "setTtyMode: curTty " + curTty + " desTty " + desTty);
        if (!curTty && desTty) {
            CallProfile profile = new CallProfile();
            profile.setCallType(9);
            profile.getMediaProfile().setVideoQuality(this.mCallProfile.getMediaProfile().getVideoQuality());
            Bundle bundle = new Bundle();
            bundle.putParcelable("profile", profile);
            this.smCallStateMachine.sendMessage(52, (Object) bundle);
        }
    }

    public void info(int type, String request) throws RemoteException {
        String str = this.LOG_TAG;
        Log.i(str, "info: request=" + IMSLog.checker(request));
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("info", request);
        this.smCallStateMachine.sendMessage(101, (Object) bundle);
    }

    public void notifyImsMediaEvent(IMSMediaEvent event) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyImsMediaEvent: " + event.getState());
        if (getCallState() == CallConstants.STATE.ResumingCall) {
            Handler handler = this.mVolteStackEventHandler;
            handler.sendMessageDelayed(handler.obtainMessage(2, event), 1000);
            return;
        }
        Handler handler2 = this.mVolteStackEventHandler;
        handler2.sendMessage(handler2.obtainMessage(2, event));
    }

    public void notifyCmcDtmfEvent(int dtmfKey) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyCmcDtmfEvent, dtmfKey: " + dtmfKey);
        this.smCallStateMachine.sendMessage(86, dtmfKey, -1);
    }

    public void notifyCmcInfoEvent(CmcInfoEvent cmcInfoEvent) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyCmcInfoEvent, cmcInfo: " + cmcInfoEvent);
        this.smCallStateMachine.sendMessage(87, (Object) cmcInfoEvent);
    }

    public IImsMediaCallProvider getMediaCallProvider() throws RemoteException {
        return this.mMediaController;
    }

    public void requestCallDataUsage() throws RemoteException {
        this.smCallStateMachine.requestCallDataUsage();
    }

    public String getIncomingInviteRawSip() {
        return this.mCallProfile.getSipInviteMsg();
    }

    public void updateCallProfile(CallParams param) {
        String str;
        String str2 = this.LOG_TAG;
        Log.i(str2, "updateCallProfile: " + param);
        if (param.getPLettering() != null) {
            this.mCallProfile.setLetteringText(param.getPLettering());
        }
        if (param.getHistoryInfo() != null) {
            this.mCallProfile.setHistoryInfo(param.getHistoryInfo());
        }
        if (param.getAlertInfo() != null) {
            this.mCallProfile.setAlertInfo(param.getAlertInfo());
        }
        if (param.getPhotoRing() != null) {
            this.mCallProfile.setPhotoRing(param.getPhotoRing());
        }
        if (param.getNumberPlus() != null) {
            this.mCallProfile.setNumberPlus(param.getNumberPlus());
        }
        if (param.getModifyHeader() != null) {
            this.mCallProfile.setModifyHeader(param.getModifyHeader());
        }
        if (param.getConferenceSupported() != null) {
            this.mCallProfile.setConferenceSupported(param.getConferenceSupported());
        }
        if (param.getIsFocus() != null) {
            this.mCallProfile.setIsFocus(param.getIsFocus());
        }
        if (param.getisHDIcon() > 0) {
            this.mCallProfile.setHDIcon(param.getisHDIcon());
        }
        if (param.getRetryAfter() > 0) {
            this.mCallProfile.setRetryAfterTime(param.getRetryAfter());
        }
        if (param.getAudioRxTrackId() > 0) {
            this.mCallProfile.setAudioRxTrackId(param.getAudioRxTrackId());
        }
        if (param.getFeatureCaps() != null) {
            this.mCallProfile.setFeatureCaps(param.getFeatureCaps());
        }
        setAudioCodecTypeProfile(param);
        setMediaProfile(param);
        this.mCallProfile.setAudioEarlyMediaDir(param.getAudioEarlyMediaDir());
        if (param.getSipCallId() != null) {
            this.mCallProfile.setSipCallId(param.getSipCallId());
        }
        if (param.getSipInviteMsg() != null) {
            this.mCallProfile.setSipInviteMsg(param.getSipInviteMsg());
        }
        if (param.getTerminatingId() != null) {
            this.mCallProfile.setOriginatingUri(param.getTerminatingId());
            String msisdn = UriUtil.getMsisdnNumber(param.getTerminatingId());
            CallProfile callProfile = this.mCallProfile;
            if (msisdn == null) {
                str = param.getTerminatingId().toString();
            } else {
                str = msisdn;
            }
            callProfile.setLineMsisdn(str);
        }
        setVerstat(param);
        if (param.getHasDiversion()) {
            this.mCallProfile.setHasDiversion(param.getHasDiversion());
        }
    }

    private void setMediaProfile(CallParams param) {
        if (this.mCallProfile.getMediaProfile() != null) {
            this.mCallProfile.getMediaProfile().setVideoOrientation(param.getVideoOrientation());
            String oldVideoSize = this.mCallProfile.getMediaProfile().getVideoSize();
            this.mCallProfile.getMediaProfile().setVideoSize(param.getVideoWidth(), param.getVideoHeight());
            String newVideoSize = this.mCallProfile.getMediaProfile().getVideoSize();
            if (!oldVideoSize.equals(newVideoSize) && param.getVideoCrbtType() == 0) {
                if (!newVideoSize.contains("LAND") || newVideoSize.contains("QCIF")) {
                    this.mMediaController.changeCameraCapabilities(getSessionId(), param.getVideoWidth(), param.getVideoHeight());
                } else {
                    this.mMediaController.changeCameraCapabilities(getSessionId(), param.getVideoHeight(), param.getVideoWidth());
                }
            }
        }
    }

    private void setVerstat(CallParams param) {
        if (param.getVerstat() != null) {
            String[] params = param.getVerstat().split("[<>:;@]");
            String verstat = "";
            int length = params.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String p = params[i];
                if (p.contains("verstat")) {
                    verstat = p.substring(p.indexOf(AuthenticationHeaders.HEADER_PRARAM_SPERATOR) + 1);
                    break;
                }
                i++;
            }
            String str = this.LOG_TAG;
            Log.i(str, "verstat " + IMSLog.checker(verstat));
            this.mCallProfile.setVerstat(verstat);
        }
    }

    private void setAudioCodecTypeProfile(CallParams param) {
        if (param.getAudioCodec() != null) {
            this.mCallProfile.getMediaProfile().setAudioCodec(ImsCallUtil.getAudioCodec(param.getAudioCodec()));
        }
    }

    public void forceNotifyCurrentCodec() {
        this.smCallStateMachine.sendMessage(100);
    }

    /* access modifiers changed from: protected */
    public void onImsCallEvent(CallStateEvent event) {
        this.mImsCallSessionEventHandler.onImsCallEventHandler(event);
    }

    /* access modifiers changed from: protected */
    public void notifyCallDowngraded() {
        IMSMediaEvent event = new IMSMediaEvent();
        event.setSessionID(this.mSessionId);
        event.setPhoneId(this.mPhoneId);
        event.setState(IMSMediaEvent.MEDIA_STATE.CALL_DOWNGRADED);
        this.mMediaController.onCallDowngraded(event);
    }

    /* access modifiers changed from: protected */
    public ImsUri buildUri(String number, String deviceId, int callType) {
        UriGenerator generator = UriGeneratorFactory.getInstance().get(getOriginatingUri());
        if (callType == 12) {
            return generator.getUssdRuri(number);
        }
        if (!TextUtils.isEmpty(number) && number.toLowerCase().startsWith("urn")) {
            return ImsUri.parse(number);
        }
        if (this.mCmcType > 0 && !TextUtils.isEmpty(number) && number.contains("@")) {
            number = number.substring(0, number.indexOf("@"));
            String str = this.LOG_TAG;
            Log.i(str, "number = " + IMSLog.checker(number));
        }
        return generator.getNetworkPreferredUri(UriGenerator.URIServiceType.VOLTE_URI, number, deviceId);
    }

    /* access modifiers changed from: protected */
    public ImsUri getOriginatingUri() {
        if (this.mCallProfile.getOriginatingUri() != null) {
            return this.mCallProfile.getOriginatingUri();
        }
        return getPreferredImpu(this.mRegistration);
    }

    private ImsUri getPreferredImpu(ImsRegistration reg) {
        ImsUri ou = reg.getPreferredImpu().getUri();
        if ((this.mMno != Mno.ATT && this.mMno != Mno.SMARTFREN) || DeviceUtil.getGcfMode().booleanValue() || reg.getImpuList() == null) {
            return ou;
        }
        for (NameAddr addr : reg.getImpuList()) {
            if (addr.getUri().getUriType().equals(ImsUri.UriType.TEL_URI)) {
                Log.i(this.LOG_TAG, "getPreferredImpu: Found TEL URI");
                return addr.getUri();
            }
        }
        return ou;
    }

    /* access modifiers changed from: protected */
    public ImsUri getSecondImpu(ImsRegistration reg) {
        ImsUri ou = reg.getPreferredImpu().getUri();
        if (this.mMno != Mno.LGU || reg.getImpuList() == null) {
            return ou;
        }
        for (NameAddr addr : reg.getImpuList()) {
            if (addr.getUri() != ou) {
                Log.i(this.LOG_TAG, "getSecondImpu: Found Second Number");
                return addr.getUri();
            }
        }
        return ou;
    }

    /* access modifiers changed from: protected */
    public String getConferenceUri(ImsProfile profile) {
        ISimManager simManager = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (profile == null || simManager == null) {
            return null;
        }
        return ImsCallUtil.getConferenceUri(profile, simManager.getSimOperator(), this.mTelephonyManager.getIsimDomain(simManager.getSubscriptionId()), this.mMno);
    }

    /* access modifiers changed from: protected */
    public String getConfSubscribeEnabled(ImsProfile profile) {
        if (profile == null) {
            return null;
        }
        return profile.getConferenceSubscribe();
    }

    /* access modifiers changed from: protected */
    public String getConfSubscribeDialogType(ImsProfile profile) {
        if (profile == null) {
            return null;
        }
        return profile.getConferenceDialogType();
    }

    /* access modifiers changed from: protected */
    public String getConfReferUriType(ImsProfile profile) {
        if (profile == null) {
            return null;
        }
        return profile.getConferenceReferUriType();
    }

    /* access modifiers changed from: protected */
    public String getConfRemoveReferUriType(ImsProfile profile) {
        if (profile == null) {
            return null;
        }
        return profile.getConferenceRemoveReferUriType();
    }

    /* access modifiers changed from: protected */
    public String getConfReferUriAsserted(ImsProfile profile) {
        if (profile == null) {
            return null;
        }
        return profile.getConferenceReferUriAsserted();
    }

    /* access modifiers changed from: protected */
    public String getConfUseAnonymousUpdate(ImsProfile profile) {
        if (profile == null) {
            return null;
        }
        return profile.getConferenceUseAnonymousUpdate();
    }

    /* access modifiers changed from: protected */
    public boolean getConfSupportPrematureEnd(ImsProfile profile) {
        if (profile == null) {
            return false;
        }
        return profile.getConferenceSupportPrematureEnd();
    }

    /* access modifiers changed from: protected */
    public void setRttDedicatedBearerTimeoutMessage(Message msg) {
        this.mImsCallDedicatedBearer.setRttDedicatedBearerTimeoutMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void startRttDedicatedBearerTimer(long millis) {
        this.mImsCallDedicatedBearer.startRttDedicatedBearerTimer(millis);
    }

    /* access modifiers changed from: protected */
    public void stopRttDedicatedBearerTimer() {
        this.mImsCallDedicatedBearer.stopRttDedicatedBearerTimer();
    }

    /* access modifiers changed from: protected */
    public boolean getDRBLost() {
        return this.mImsCallDedicatedBearer.getDRBLost();
    }

    /* access modifiers changed from: protected */
    public void setDRBLost(boolean losted) {
        this.mImsCallDedicatedBearer.setDRBLost(losted);
    }

    public int getErrorCode() {
        if (this.smCallStateMachine.sipError != null) {
            return this.smCallStateMachine.sipError.getCode();
        }
        if (this.smCallStateMachine.sipReason != null) {
            return this.smCallStateMachine.sipReason.getCause();
        }
        return this.smCallStateMachine.errorCode;
    }

    public String getErrorMessage() {
        if (this.smCallStateMachine.sipError != null) {
            return this.smCallStateMachine.sipError.getReason();
        }
        if (this.smCallStateMachine.sipReason != null) {
            return this.smCallStateMachine.sipReason.getText();
        }
        return this.smCallStateMachine.errorMessage;
    }

    public int getEndType() {
        return this.mEndType;
    }

    public int getEndReason() {
        return this.mEndReason;
    }

    public void setEndReason(int reason) {
        this.mEndReason = reason;
    }

    public void setEndType(int type) {
        this.mEndType = type;
    }

    public void setPreviewResolution(int width, int height) {
        String str = this.LOG_TAG;
        Log.i(str, "setPreviewResolution width : " + width + " height : " + height);
        this.mMediaController.setPreviewResolution(width, height);
    }

    public void startCamera(int cameraId) {
        NetworkEvent ne;
        if ((this.mMno == Mno.DOCOMO || this.mMno.isKor()) && this.mUserCameraOff) {
            Log.i(this.LOG_TAG, "Camera is Off by user");
            return;
        }
        String str = this.LOG_TAG;
        Log.i(str, "startCamera " + cameraId);
        if (cameraId < 0 || cameraId == 2) {
            int defaultCameraId = this.mMediaController.getDefaultCameraId();
            this.mLastUsedCamera = defaultCameraId;
            if (defaultCameraId < 0) {
                this.mLastUsedCamera = 1;
            }
        } else {
            this.mLastUsedCamera = cameraId;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "mLastUsedCamera " + this.mLastUsedCamera);
        if (this.mMno.isKor() && (!((ne = this.mModule.getNetwork(this.mPhoneId)) == null || ne.network == 13 || ne.network == 20) || (this.mCallProfile.getMediaProfile().getWidth() == 176 && this.mCallProfile.getMediaProfile().getHeight() == 144))) {
            setPreviewResolution(MNO.ORANGE_SENEGAL, 144);
        }
        this.mMediaController.startCamera(this.mSessionId, this.mLastUsedCamera);
    }

    public void startLastUsedCamera() {
        String str = this.LOG_TAG;
        Log.i(str, "startLastUsedCamera " + this.mLastUsedCamera);
        startCamera(this.mLastUsedCamera);
    }

    public void stopCamera() {
        Log.i(this.LOG_TAG, "stopCamera");
        this.mMediaController.stopCamera(this.mSessionId);
        this.mIsUsingCamera = false;
    }

    public void setStartCameraState(boolean isStartCameraSuccess) {
        this.smCallStateMachine.setStartCameraState(isStartCameraSuccess);
    }

    public void onSwitchCamera() {
        String str = this.LOG_TAG;
        Log.i(str, "onSwitchCamera - mLastUsedCamera " + this.mLastUsedCamera);
        if (this.mLastUsedCamera == 1) {
            this.mLastUsedCamera = 0;
        } else {
            this.mLastUsedCamera = 1;
        }
    }

    public void onUpdateGeolocation() {
        Log.i(this.LOG_TAG, "onUpdateGeolocation: ON_LOCATION_ACQUIRING_SUCCESS");
        this.smCallStateMachine.sendMessage(501);
    }

    public void handleRegistrationDone(ImsRegistration regInfo) {
        Log.i(this.LOG_TAG, "handleRegistrationDone");
        this.mRegistration = regInfo;
        this.smCallStateMachine.onRegistrationDone(regInfo);
        setPendingCall(false);
        boolean isEmergency = ImsCallUtil.isE911Call(this.mCallProfile.getCallType());
        if (this.mMno.isKor() || isEmergency) {
            this.smCallStateMachine.sendMessage(11);
        } else if (this.mMno != Mno.VZW) {
        } else {
            if (this.mRegistration.hasService("mmtel")) {
                this.smCallStateMachine.sendMessage(11);
            } else {
                this.smCallStateMachine.sendMessage(211);
            }
        }
    }

    public void handleRegistrationFailed() {
        Log.i(this.LOG_TAG, "handleRegistrationFailed");
        this.mRegistration = null;
        setPendingCall(false);
        this.smCallStateMachine.sendMessage(211);
    }

    public void replaceRegistrationInfo(ImsRegistration regInfo) {
        String str = this.LOG_TAG;
        Log.i(str, "replaceRegistrationInfo from " + this.mRegistration.getHandle() + " to " + regInfo.getHandle());
        this.mRegistration = regInfo;
    }

    public void replaceSipCallId(String sipCallId) {
        String str = this.LOG_TAG;
        Log.i(str, "replaceSipCallId " + sipCallId);
        this.mCallProfile.setSipCallId(sipCallId);
        this.mVolteSvcIntf.replaceSipCallId(this.mSessionId, sipCallId);
    }

    /* access modifiers changed from: protected */
    public boolean isTPhoneRelaxMode() {
        return ImsCallUtil.isTPhoneRelaxMode(this.mContext, this.mCallProfile.getDialingNumber());
    }

    /* access modifiers changed from: protected */
    public int setPhoneId(ImsRegistration registration) {
        if (registration != null) {
            this.mPhoneId = registration.getPhoneId();
        } else {
            this.mPhoneId = this.mModule.getDefaultPhoneId();
        }
        return this.mPhoneId;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public long getCmcCallEstablishTime() {
        return this.smCallStateMachine.mCmcCallEstablishTime;
    }

    public boolean isRemoteHeld() {
        return this.smCallStateMachine.mRemoteHeld;
    }

    private boolean isSrvccAvailable() {
        return ImsCallUtil.isSrvccAvailable(this.mModule.getSrvccVersion(this.mPhoneId), this.mMno, isEpdgCall(), getCallState(), this.mCallProfile.isConferenceCall());
    }

    /* access modifiers changed from: protected */
    public String getPEmergencyInfoOfAtt(String impi) {
        return ImsCallUtil.getPEmergencyInfoOfAtt(this.mContext, impi);
    }

    public boolean isE911Call() {
        CallProfile callProfile = this.mCallProfile;
        return callProfile != null && ImsCallUtil.isE911Call(callProfile.getCallType());
    }

    /* access modifiers changed from: protected */
    public boolean getCameraStartByApp() {
        return this.mIsCameraStartByAPP;
    }

    public boolean isCmcPrimaryType(int cmcType) {
        if (cmcType == 1 || cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    public boolean isCmcSecondaryType(int cmcType) {
        if (cmcType == 2 || cmcType == 4 || cmcType == 8) {
            return true;
        }
        return false;
    }
}
