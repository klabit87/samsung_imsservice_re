package com.sec.internal.google;

import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.ImsSuppServiceNotification;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsVideoCallCallback;
import com.android.ims.internal.IImsVideoCallProvider;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.ims.IRttEventListener;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.google.cmc.CmcCallSessionManager;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;

public class ImsCallSessionImpl extends IImsCallSession.Stub {
    protected static final int SET_LOCAL_CALL_PROFILE = 1;
    protected static final int SET_LOCAL_REMOTE_CALL_PROFILE = 3;
    protected static final int SET_REMOTE_CALL_PROFILE = 2;
    private final String LOG_TAG = "ImsCallSessionImpl";
    protected String mCallId = "";
    public int mCallIdInt = -1;
    public ImsCallProfile mCallProfile;
    public final GoogleImsService mGoogleImsService;
    public IImsCallSession mImpl;
    public ImsVideoCallProviderImpl mImsVideoCallProvider;
    public boolean mIsCWNotified = false;
    public boolean mIsConferenceHost = false;
    public boolean mIsConferenceParticipant = false;
    public boolean mIsEcbmSupport = false;
    public IImsCallSessionListener mListener;
    protected ImsCallProfile mLocalCallProfile = new ImsCallProfile(1, 3);
    protected ImsCallProfile mRemoteCallProfile = new ImsCallProfile(1, 3);
    public final IRttEventListener mRttEventListener = new IRttEventListener.Stub() {
        public void onRttEvent(String rttEvent) throws RemoteException {
            ImsCallSessionImpl.this.mListener.callSessionRttMessageReceived(rttEvent);
        }

        public void onRttEventBySession(int sessionId, String rttEvent) throws RemoteException {
            if (ImsCallSessionImpl.this.getSessionId() == sessionId) {
                ImsCallSessionImpl.this.mListener.callSessionRttMessageReceived(rttEvent);
            }
        }

        public void onSendRttSessionModifyRequest(int callId, boolean mode) throws RemoteException {
            if (ImsCallSessionImpl.this.getCallIdInt() == callId) {
                ImsCallSessionImpl.this.mCallProfile.mMediaProfile.setRttMode(mode);
                ImsCallSessionImpl.this.mListener.callSessionRttModifyRequestReceived(ImsCallSessionImpl.this.mCallProfile);
            }
        }

        public void onSendRttSessionModifyResponse(int callId, boolean mode, boolean result) throws RemoteException {
            if (ImsCallSessionImpl.this.getCallIdInt() == callId) {
                if (mode == result) {
                    ImsCallSessionImpl.this.mCallProfile.mMediaProfile.setRttMode(1);
                } else {
                    ImsCallSessionImpl.this.mCallProfile.mMediaProfile.setRttMode(0);
                }
                if (result) {
                    ImsCallSessionImpl.this.mListener.callSessionRttModifyResponseReceived(1);
                } else {
                    ImsCallSessionImpl.this.mListener.callSessionRttModifyResponseReceived(2);
                }
            }
        }
    };
    public com.sec.ims.volte2.IImsCallSession mSession;
    public int mState = 0;
    protected final IImsCallSessionEventListener mVolteEventListener = new ImsCallSessionEventListener(this);
    public final IVolteServiceModule mVolteServiceModule;

    public ImsCallSessionImpl(ImsCallProfile profile, com.sec.ims.volte2.IImsCallSession callSession, IImsCallSessionListener listener, GoogleImsService googleImsService) {
        this.mCallProfile = profile;
        setCallProfile(3);
        this.mSession = callSession;
        this.mListener = listener;
        this.mImpl = this;
        this.mGoogleImsService = googleImsService;
        this.mVolteServiceModule = ImsRegistry.getServiceModuleManager().getVolteServiceModule();
        if (callSession != null) {
            try {
                this.mSession.registerSessionEventListener(this.mVolteEventListener);
                this.mVolteServiceModule.registerRttEventListener(this.mSession.getPhoneId(), this.mRttEventListener);
                int callId = this.mSession.getCallId();
                this.mCallIdInt = callId;
                if (callId > 0) {
                    this.mCallId = Integer.toString(callId);
                }
            } catch (RemoteException e) {
            }
            this.mImsVideoCallProvider = new ImsVideoCallProviderImpl(this.mSession);
        }
    }

    public void initP2pImpl() {
        Log.d("ImsCallSessionImpl", "initP2pImpl(), duplicate");
    }

    public CmcCallSessionManager getCmcCallSessionManager() {
        Log.d("ImsCallSessionImpl", "getCmcCallSessionManager(), duplicate");
        return null;
    }

    public void close() {
        this.mGoogleImsService.onCallClosed(this.mCallIdInt);
        try {
            if (this.mImsVideoCallProvider != null) {
                this.mImsVideoCallProvider.setCallback((IImsVideoCallCallback) null);
            }
        } catch (RemoteException e) {
        }
    }

    public String getCallId() {
        return this.mCallId;
    }

    public int getCallIdInt() {
        return this.mCallIdInt;
    }

    /* access modifiers changed from: private */
    public int getSessionId() throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession == null) {
            return -1;
        }
        return iImsCallSession.getSessionId();
    }

    public ImsCallProfile getCallProfile() {
        return this.mCallProfile;
    }

    public ImsCallProfile getLocalCallProfile() {
        int callType = 3;
        if (!this.mCallProfile.getCallExtraBoolean("call_mode_changeable", false)) {
            callType = 2;
        }
        this.mLocalCallProfile.mCallType = callType;
        return this.mLocalCallProfile;
    }

    /* access modifiers changed from: protected */
    public void setCallProfile(int capability) {
        Parcel p = Parcel.obtain();
        this.mCallProfile.writeToParcel(p, 0);
        if ((capability & 1) == 1) {
            p.setDataPosition(0);
            ImsCallProfile imsCallProfile = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(p);
            this.mLocalCallProfile = imsCallProfile;
            imsCallProfile.mRestrictCause = this.mCallProfile.mRestrictCause;
            this.mLocalCallProfile.mMediaProfile.copyFrom(this.mCallProfile.mMediaProfile);
        }
        if ((capability & 2) == 2) {
            p.setDataPosition(0);
            ImsCallProfile imsCallProfile2 = (ImsCallProfile) ImsCallProfile.CREATOR.createFromParcel(p);
            this.mRemoteCallProfile = imsCallProfile2;
            imsCallProfile2.mRestrictCause = this.mCallProfile.mRestrictCause;
            this.mRemoteCallProfile.mMediaProfile.copyFrom(this.mCallProfile.mMediaProfile);
        }
        p.recycle();
    }

    public ImsCallProfile getRemoteCallProfile() {
        int callType = 3;
        if (!this.mCallProfile.getCallExtraBoolean("call_mode_changeable", false)) {
            callType = 2;
        }
        this.mRemoteCallProfile.mCallType = callType;
        return this.mRemoteCallProfile;
    }

    public String getProperty(String name) {
        if (this.mSession == null || !TextUtils.equals("RawInviteMessage", name)) {
            return null;
        }
        try {
            return this.mSession.getIncomingInviteRawSip();
        } catch (RemoteException e) {
            return null;
        }
    }

    public CallConstants.STATE getInternalState() throws RemoteException {
        if (this.mSession == null) {
            return CallConstants.STATE.Idle;
        }
        return CallConstants.STATE.values()[this.mSession.getCallStateOrdinal()];
    }

    public CallConstants.STATE getPrevInternalState() throws RemoteException {
        if (this.mSession == null) {
            return CallConstants.STATE.Idle;
        }
        return CallConstants.STATE.values()[this.mSession.getPrevCallStateOrdinal()];
    }

    public int getState() {
        if (this.mIsConferenceParticipant) {
            return 8;
        }
        return this.mState;
    }

    /* renamed from: com.sec.internal.google.ImsCallSessionImpl$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE;

        static {
            int[] iArr = new int[CallConstants.STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE = iArr;
            try {
                iArr[CallConstants.STATE.InCall.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.IncomingCall.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.OutGoingCall.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.AlertingCall.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.HoldingCall.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.HeldCall.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ResumingCall.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ModifyingCall.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ModifyRequested.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.HoldingVideo.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.VideoHeld.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[CallConstants.STATE.ResumingVideo.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    public boolean isInCall() throws RemoteException {
        if (this.mState != 4 || this.mSession == null) {
            return false;
        }
        switch (AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallConstants$STATE[getInternalState().ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return true;
            default:
                return false;
        }
    }

    public void setListener(IImsCallSessionListener listener) {
        this.mListener = listener;
    }

    public IImsCallSessionListener getListener() {
        return this.mListener;
    }

    public void setMute(boolean muted) {
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    public void start(String callee, ImsCallProfile profile) throws RemoteException {
        int targetDialogId;
        if (this.mSession == null || this.mVolteServiceModule == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionInitiatedFailed(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        this.mState = 1;
        this.mCallProfile = profile;
        setCallProfile(3);
        CallProfile cp = this.mSession.getCallProfile();
        cp.setDialingNumber(callee);
        if (isEmergencyCall()) {
            this.mCallProfile.setCallExtra("CallRadioTech", Integer.toString(this.mSession.getCallProfile().getRadioTech()));
        }
        this.mVolteServiceModule.setAutomaticMode(this.mSession.getPhoneId(), this.mCallProfile.mMediaProfile.isRttCall());
        cp.getMediaProfile().setRttMode(this.mCallProfile.mMediaProfile.getRttMode());
        if (this.mCallProfile.getCallExtraBoolean("CallPull")) {
            Bundle oemExtras = this.mCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
            DialogEvent de = this.mVolteServiceModule.getLastDialogEvent(this.mSession.getPhoneId());
            int pullFailReason = 101;
            if (!(de == null || oemExtras == null)) {
                int dialogId = oemExtras.getInt("android.telephony.ImsExternalCallTracker.extra.EXTERNAL_CALL_ID");
                for (Dialog pullTarget : de.getDialogList()) {
                    if (pullTarget != null) {
                        if (SimUtil.getSimMno(this.mSession.getPhoneId()) == Mno.VZW) {
                            targetDialogId = ImsCallUtil.getIdForString(pullTarget.getSipCallId());
                        } else {
                            try {
                                targetDialogId = Integer.parseInt(pullTarget.getDialogId());
                            } catch (NumberFormatException e) {
                            }
                        }
                        if (dialogId == targetDialogId && !TextUtils.isEmpty(pullTarget.getSipCallId()) && !TextUtils.isEmpty(pullTarget.getSipLocalTag()) && !TextUtils.isEmpty(pullTarget.getSipRemoteTag())) {
                            this.mCallProfile.mCallType = DataTypeConvertor.convertToGoogleCallType(pullTarget.getCallType());
                            cp.setCallType(pullTarget.getCallType());
                            cp.setPullCall(true);
                            try {
                                this.mSession.pulling(de.getMsisdn(), pullTarget);
                                return;
                            } catch (RemoteException e2) {
                                pullFailReason = 1015;
                            }
                        }
                    }
                }
            }
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionInitiatedFailed(new ImsReasonInfo(pullFailReason, 0));
                return;
            }
            return;
        }
        try {
            if (this.mSession.start(callee, cp) < 0) {
                throw new RemoteException("start return -1");
            }
        } catch (RemoteException e3) {
            IImsCallSessionListener iImsCallSessionListener3 = this.mListener;
            if (iImsCallSessionListener3 != null) {
                iImsCallSessionListener3.callSessionInitiatedFailed(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void startConference(String[] participants, ImsCallProfile profile) throws RemoteException {
        if (this.mSession == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionInitiatedFailed(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        try {
            this.mState = 1;
            this.mCallProfile = profile;
            setCallProfile(3);
            this.mSession.startConference(participants, this.mSession.getCallProfile());
        } catch (RemoteException e) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionInitiatedFailed(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void accept(int callType, ImsStreamMediaProfile profile) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionTerminated(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        int incomingCallType = iImsCallSession.getCallProfile().getCallType();
        int answerCallType = DataTypeConvertor.convertToSecCallType(callType);
        if (ImsCallUtil.isTtyCall(incomingCallType) || incomingCallType == 12) {
            answerCallType = incomingCallType;
        }
        CallProfile answerCallProfile = new CallProfile();
        answerCallProfile.setCallType(answerCallType);
        answerCallProfile.getMediaProfile().setRttMode(profile.mRttMode);
        try {
            this.mSession.accept(answerCallProfile);
        } catch (RemoteException e) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionTerminated(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void reject(int reason) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionTerminated(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        try {
            iImsCallSession.reject(convertRejectReasonFromFW(reason));
        } catch (RemoteException e) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionTerminated(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void terminate(int reason) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionTerminated(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        try {
            if (iImsCallSession.getCallProfile().getCallType() == 12) {
                this.mSession.info(3, "1");
            } else {
                this.mSession.terminate(convertEndReasonFromFW(reason));
            }
        } catch (RemoteException e) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionTerminated(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void hold(ImsStreamMediaProfile profile) throws RemoteException {
        if (this.mSession == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionHoldFailed(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        try {
            this.mSession.hold(new MediaProfile());
        } catch (RemoteException e) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionHoldFailed(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void resume(ImsStreamMediaProfile profile) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionResumeFailed(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        try {
            iImsCallSession.resume();
        } catch (RemoteException e) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionResumeFailed(new ImsReasonInfo(103, 0));
            }
        }
    }

    private void inviteParticipants(int participantId) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            try {
                iImsCallSession.inviteParticipants(participantId);
            } catch (RemoteException e) {
            }
        }
    }

    public void merge() throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession == null || this.mVolteServiceModule == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionMergeFailed(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        int hostId = iImsCallSession.getCallId();
        if (this.mGoogleImsService.hasConferenceHost()) {
            hostId = this.mGoogleImsService.getConferenceHost().getCallIdInt();
        }
        int participantId = this.mVolteServiceModule.getParticipantIdForMerge(this.mSession.getPhoneId(), hostId);
        if (participantId == -1) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionMergeFailed(new ImsReasonInfo(102, 0));
            }
        } else if (this.mGoogleImsService.hasConferenceHost()) {
            this.mGoogleImsService.setInitialMerge(false);
            this.mGoogleImsService.getConferenceHost().inviteParticipants(participantId);
        } else {
            CallProfile callProfile = DataTypeConvertor.convertToSecCallProfile(this.mSession.getPhoneId(), this.mCallProfile, this.mVolteServiceModule.getTtyMode(this.mSession.getPhoneId()) != 0);
            callProfile.setConferenceCall(1);
            callProfile.setLineMsisdn(this.mSession.getCallProfile().getLineMsisdn());
            callProfile.setOriginatingUri(this.mSession.getCallProfile().getOriginatingUri());
            com.sec.ims.volte2.IImsCallSession confSession = null;
            try {
                confSession = this.mVolteServiceModule.createSession(callProfile);
            } catch (RemoteException e) {
            }
            if (confSession == null) {
                IImsCallSessionListener iImsCallSessionListener3 = this.mListener;
                if (iImsCallSessionListener3 != null) {
                    iImsCallSessionListener3.callSessionMergeFailed(new ImsReasonInfo(102, 0));
                    return;
                }
                return;
            }
            confSession.setEpdgState(TextUtils.equals(this.mCallProfile.getCallExtra("CallRadioTech"), String.valueOf(18)));
            this.mIsConferenceHost = true;
            this.mGoogleImsService.setInitialMerge(true);
            this.mGoogleImsService.setConferenceHost(this);
            ImsCallSessionImpl conference = new ImsCallSessionImpl(this.mCallProfile, confSession, (IImsCallSessionListener) null, this.mGoogleImsService);
            confSession.merge(participantId, hostId);
            this.mListener.callSessionMergeStarted(conference, conference.getCallProfile());
        }
    }

    public void update(int callType, ImsStreamMediaProfile profile) {
    }

    public void extendToConference(String[] participants) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession == null) {
            IImsCallSessionListener iImsCallSessionListener = this.mListener;
            if (iImsCallSessionListener != null) {
                iImsCallSessionListener.callSessionConferenceExtendFailed(new ImsReasonInfo(102, 0));
                return;
            }
            return;
        }
        try {
            iImsCallSession.extendToConference(participants);
        } catch (RemoteException e) {
            IImsCallSessionListener iImsCallSessionListener2 = this.mListener;
            if (iImsCallSessionListener2 != null) {
                iImsCallSessionListener2.callSessionConferenceExtendFailed(new ImsReasonInfo(103, 0));
            }
        }
    }

    public void inviteParticipants(String[] participants) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            try {
                iImsCallSession.extendToConference(participants);
            } catch (RemoteException e) {
            }
        }
    }

    public void removeParticipants(String[] participants) throws RemoteException {
        if (this.mSession != null) {
            try {
                for (String participant : participants) {
                    int participantId = this.mGoogleImsService.getParticipantId(participant);
                    this.mGoogleImsService.updateParticipant(participantId, "disconnecting", 3);
                    this.mSession.removeParticipants(participantId);
                }
            } catch (RemoteException e) {
            }
        }
    }

    private int convertDtmfToCode(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c == '*') {
            return 10;
        }
        if (c == '#') {
            return 11;
        }
        return -1;
    }

    public void sendDtmf(char c, Message result) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            iImsCallSession.sendDtmf(convertDtmfToCode(c), 0, (Message) null);
            if (result != null && result.replyTo != null) {
                result.replyTo.send(result);
            }
        }
    }

    public void startDtmf(char c) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            iImsCallSession.startDtmf(convertDtmfToCode(c));
        }
    }

    public void stopDtmf() throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            iImsCallSession.stopDtmf();
        }
    }

    public void sendUssd(String ussdMessage) throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            iImsCallSession.info(2, ussdMessage);
        }
    }

    public boolean isMultiparty() throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        return iImsCallSession != null && iImsCallSession.getCallProfile().isConferenceCall();
    }

    public boolean isEmergencyCall() {
        return this.mCallProfile.getCallExtraBoolean("e_call") || this.mCallProfile.mServiceType == 2;
    }

    public boolean isWifiCall() {
        return Integer.toString(18).equals(this.mCallProfile.getCallExtra("CallRadioTech"));
    }

    public IImsVideoCallProvider getVideoCallProvider() {
        return this.mImsVideoCallProvider;
    }

    public ImsStreamMediaProfile getImsStreamMediaProfile() {
        return new ImsStreamMediaProfile(2, 3, 0, -1, this.mCallProfile.mMediaProfile.getRttMode());
    }

    public void updateConferenceStatus(int callId, String status) {
        if ("disconnected".equals(status)) {
            this.mGoogleImsService.updateParticipant(callId, status, 2);
        } else {
            this.mGoogleImsService.updateParticipant(callId, status);
        }
        try {
            ImsCallSessionImpl host = this.mGoogleImsService.getConferenceHost();
            this.mGoogleImsService.updateSecConferenceInfo(this.mCallProfile);
            if (this.mListener != null) {
                this.mListener.callSessionUpdated(this.mCallProfile);
                this.mListener.callSessionConferenceStateUpdated(this.mGoogleImsService.getImsConferenceState());
            } else if (host != null && host.getListener() != null) {
                host.getListener().callSessionUpdated(this.mCallProfile);
                host.getListener().callSessionConferenceStateUpdated(this.mGoogleImsService.getImsConferenceState());
            }
        } catch (RemoteException e) {
        }
    }

    public void onSuppServiceReceived(int notificationType, int code) throws RemoteException {
        this.mListener.callSessionSuppServiceReceived(new ImsSuppServiceNotification(notificationType, code, 1, 0, (String) null, (String[]) null));
    }

    /* access modifiers changed from: protected */
    public void updateCmcCallExtras(CallProfile cp) throws RemoteException {
        Bundle oemExtras = null;
        if (this.mCallProfile.mCallExtras.containsKey("android.telephony.ims.extra.OEM_EXTRAS") && (oemExtras = this.mCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS")) != null) {
            oemExtras.remove("com.samsung.telephony.extra.CMC_CS_DTMF_KEY");
            this.mCallProfile.mCallExtras.remove("android.telephony.ims.extra.OEM_EXTRAS");
        }
        if (oemExtras == null) {
            oemExtras = new Bundle();
        }
        int cmcType = this.mSession.getCmcType();
        int sessionId = this.mSession.getSessionId();
        oemExtras.putInt("com.samsung.telephony.extra.CMC_TYPE", cmcType);
        oemExtras.putInt("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId);
        if (cmcType == 1) {
            oemExtras.putString("com.samsung.telephony.extra.CMC_DIAL_TO", cp.getDialingNumber());
            int dtmfKey = cp.getCmcDtmfKey();
            if (dtmfKey > -1 && dtmfKey < 12) {
                char keyChar = 0;
                if (dtmfKey >= 0 && dtmfKey <= 9) {
                    keyChar = (char) (dtmfKey + 48);
                } else if (dtmfKey == 10) {
                    keyChar = '*';
                } else if (dtmfKey == 11) {
                    keyChar = '#';
                }
                oemExtras.putString("com.samsung.telephony.extra.CMC_CS_DTMF_KEY", Character.toString(keyChar));
            }
        } else if (isCmcSecondaryType(cmcType)) {
            oemExtras.putString("com.samsung.telephony.extra.CMC_PD_CALL_CONNECT_TIME", cp.getCmcCallTime());
        }
        if (!TextUtils.isEmpty(cp.getReplaceSipCallId())) {
            oemExtras.putString("com.samsung.telephony.extra.CMC_DEVICE_ID_BY_SD", cp.getCmcDeviceId());
        } else if (cp.getCmcDeviceId() != null) {
            oemExtras.putString("com.samsung.telephony.extra.CMC_DEVICE_ID", cp.getCmcDeviceId());
        }
        this.mCallProfile.mCallExtras.putBundle("android.telephony.ims.extra.OEM_EXTRAS", oemExtras);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005a, code lost:
        return 340;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0060, code lost:
        return 338;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0063, code lost:
        return 354;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int convertErrorReasonToFw(int r3) {
        /*
            r2 = this;
            r0 = r3
            r1 = 381(0x17d, float:5.34E-43)
            if (r3 == r1) goto L_0x0084
            r1 = 382(0x17e, float:5.35E-43)
            if (r3 == r1) goto L_0x0084
            r1 = 1111(0x457, float:1.557E-42)
            if (r3 == r1) goto L_0x0081
            r1 = 1112(0x458, float:1.558E-42)
            if (r3 == r1) goto L_0x0081
            r1 = 3009(0xbc1, float:4.217E-42)
            if (r3 == r1) goto L_0x007e
            r1 = 3010(0xbc2, float:4.218E-42)
            if (r3 == r1) goto L_0x007b
            switch(r3) {
                case 200: goto L_0x0078;
                case 210: goto L_0x0075;
                case 220: goto L_0x0078;
                case 400: goto L_0x0072;
                case 408: goto L_0x006f;
                case 415: goto L_0x006c;
                case 480: goto L_0x0069;
                case 484: goto L_0x0066;
                case 580: goto L_0x0063;
                case 600: goto L_0x0060;
                case 603: goto L_0x005d;
                case 606: goto L_0x005a;
                case 1105: goto L_0x0057;
                case 1703: goto L_0x0054;
                case 1802: goto L_0x0051;
                case 2511: goto L_0x004e;
                case 3001: goto L_0x007b;
                default: goto L_0x001c;
            }
        L_0x001c:
            switch(r3) {
                case 403: goto L_0x004b;
                case 404: goto L_0x0048;
                case 405: goto L_0x0045;
                case 406: goto L_0x005a;
                default: goto L_0x001f;
            }
        L_0x001f:
            switch(r3) {
                case 486: goto L_0x0060;
                case 487: goto L_0x0042;
                case 488: goto L_0x005a;
                default: goto L_0x0022;
            }
        L_0x0022:
            switch(r3) {
                case 500: goto L_0x003f;
                case 501: goto L_0x003f;
                case 502: goto L_0x0063;
                case 503: goto L_0x003c;
                case 504: goto L_0x0039;
                case 505: goto L_0x0063;
                default: goto L_0x0025;
            }
        L_0x0025:
            switch(r3) {
                case 2504: goto L_0x007b;
                case 2505: goto L_0x007e;
                case 2506: goto L_0x0035;
                default: goto L_0x0028;
            }
        L_0x0028:
            switch(r3) {
                case 6007: goto L_0x007b;
                case 6008: goto L_0x007b;
                case 6009: goto L_0x0031;
                case 6010: goto L_0x002d;
                default: goto L_0x002b;
            }
        L_0x002b:
            goto L_0x0087
        L_0x002d:
            r0 = 146(0x92, float:2.05E-43)
            goto L_0x0087
        L_0x0031:
            r0 = 4005(0xfa5, float:5.612E-42)
            goto L_0x0087
        L_0x0035:
            r0 = 1016(0x3f8, float:1.424E-42)
            goto L_0x0087
        L_0x0039:
            r0 = 353(0x161, float:4.95E-43)
            goto L_0x0087
        L_0x003c:
            r0 = 352(0x160, float:4.93E-43)
            goto L_0x0087
        L_0x003f:
            r0 = 351(0x15f, float:4.92E-43)
            goto L_0x0087
        L_0x0042:
            r0 = 339(0x153, float:4.75E-43)
            goto L_0x0087
        L_0x0045:
            r0 = 342(0x156, float:4.79E-43)
            goto L_0x0087
        L_0x0048:
            r0 = 1515(0x5eb, float:2.123E-42)
            goto L_0x0087
        L_0x004b:
            r0 = 332(0x14c, float:4.65E-43)
            goto L_0x0087
        L_0x004e:
            r0 = 122(0x7a, float:1.71E-43)
            goto L_0x0087
        L_0x0051:
            r0 = 202(0xca, float:2.83E-43)
            goto L_0x0087
        L_0x0054:
            r0 = 1407(0x57f, float:1.972E-42)
            goto L_0x0087
        L_0x0057:
            r0 = 3115(0xc2b, float:4.365E-42)
            goto L_0x0087
        L_0x005a:
            r0 = 340(0x154, float:4.76E-43)
            goto L_0x0087
        L_0x005d:
            r0 = 361(0x169, float:5.06E-43)
            goto L_0x0087
        L_0x0060:
            r0 = 338(0x152, float:4.74E-43)
            goto L_0x0087
        L_0x0063:
            r0 = 354(0x162, float:4.96E-43)
            goto L_0x0087
        L_0x0066:
            r0 = 337(0x151, float:4.72E-43)
            goto L_0x0087
        L_0x0069:
            r0 = 504(0x1f8, float:7.06E-43)
            goto L_0x0087
        L_0x006c:
            r0 = 334(0x14e, float:4.68E-43)
            goto L_0x0087
        L_0x006f:
            r0 = 335(0x14f, float:4.7E-43)
            goto L_0x0087
        L_0x0072:
            r0 = 331(0x14b, float:4.64E-43)
            goto L_0x0087
        L_0x0075:
            r0 = 510(0x1fe, float:7.15E-43)
            goto L_0x0087
        L_0x0078:
            r0 = 501(0x1f5, float:7.02E-43)
            goto L_0x0087
        L_0x007b:
            r0 = 1014(0x3f6, float:1.421E-42)
            goto L_0x0087
        L_0x007e:
            r0 = 1404(0x57c, float:1.967E-42)
            goto L_0x0087
        L_0x0081:
            r0 = 1201(0x4b1, float:1.683E-42)
            goto L_0x0087
        L_0x0084:
            r0 = 9000(0x2328, float:1.2612E-41)
        L_0x0087:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.google.ImsCallSessionImpl.convertErrorReasonToFw(int):int");
    }

    private int convertRejectReasonFromFW(int reason) {
        if (reason != 112) {
            if (reason == 142) {
                return 7;
            }
            if (reason == 144) {
                return 8;
            }
            if (reason == 338) {
                return 2;
            }
            if (reason == 340) {
                return 9;
            }
            if (reason == 365) {
                return 16;
            }
            if (reason == 4007) {
                return 15;
            }
            switch (reason) {
                case 502:
                case 503:
                    return 13;
                case Id.REQUEST_IM_SENDMSG /*504*/:
                    return 3;
                case Id.REQUEST_IM_SEND_COMPOSING_STATUS /*505*/:
                    break;
                case Id.REQUEST_IM_SEND_NOTI_STATUS /*506*/:
                    return 12;
                default:
                    return -1;
            }
        }
        return 6;
    }

    /* access modifiers changed from: protected */
    public int convertEndReasonFromFW(int reason) {
        int endReason = reason;
        if (reason == 501) {
            return 5;
        }
        if (reason == 4003) {
            return 20;
        }
        if (reason != 4005) {
            return endReason;
        }
        return 26;
    }

    public ImsReasonInfo changeCmcErrorReason(int cmcType, int error) {
        return changeCmcErrorReason(cmcType, error, "");
    }

    public ImsReasonInfo changeCmcErrorReason(int cmcType, int error, String errorString) {
        ImsReasonInfo reasonInfo = new ImsReasonInfo(convertErrorReasonToFw(error), error, errorString);
        if (isCmcPrimaryType(cmcType)) {
            if (error == 1115 || error == 1401) {
                reasonInfo.mCode = 501;
                reasonInfo.mExtraCode = 200;
            }
        } else if (isCmcSecondaryType(cmcType)) {
            if (error == 404 && "PD_NOT_REGISTERED".equals(errorString)) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = 6001;
            } else if (error == 404 && "SD_NOT_REGISTERED".equals(errorString)) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = 6004;
            } else if (error == 480 && "REJECT_REASON_PD_UNREACHABLE".equals(errorString)) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = 6002;
            } else if (error == 486 && SipErrorBase.E911_NOT_ALLOWED_ON_SD.getReason().equals(errorString)) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = 6003;
            } else if (error == 1401) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = Id.REQUEST_CHATBOT_ANONYMIZE;
            } else if (error == 415 && "SD_NOT_SUPPORTED_VT".equals(errorString)) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = 6005;
            } else if (error == 1115) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = 6006;
            } else if (error == 486 && "PD_CALL_EXISTS_ON_THE_OTHER_SLOT".equals(errorString)) {
                reasonInfo.mCode = 352;
                reasonInfo.mExtraCode = 6011;
            }
        }
        return reasonInfo;
    }

    public void updateCallProfile() throws RemoteException {
        int oir;
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            Mno mno = SimUtil.getSimMno(iImsCallSession.getPhoneId());
            CallProfile cp = this.mSession.getCallProfile();
            if (cp != null) {
                if (cp.getHDIcon() == 1) {
                    this.mCallProfile.mRestrictCause = 0;
                } else {
                    this.mCallProfile.mRestrictCause = 3;
                }
                if (cp.isConferenceCall()) {
                    this.mCallProfile.setCallExtraBoolean("conference", true);
                    this.mCallProfile.setCallExtraInt("dialstring", 1);
                    if (mno.isKor() || mno == Mno.CHT) {
                        int oir2 = this.mGoogleImsService.getOirExtraFromDialingNumber(cp.getDialingNumber());
                        this.mCallProfile.setCallExtraInt("oir", oir2);
                        this.mCallProfile.setCallExtraInt("cnap", oir2);
                    } else {
                        this.mCallProfile.setCallExtraInt("oir", 2);
                        this.mCallProfile.setCallExtraInt("cnap", 2);
                    }
                } else {
                    this.mCallProfile.setCallExtraBoolean("conference", false);
                    this.mCallProfile.setCallExtraInt("dialstring", 0);
                    String number = cp.getDialingNumber();
                    String Pletteting = cp.getLetteringText();
                    if (TextUtils.isEmpty(number)) {
                        number = NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
                    }
                    if (mno == Mno.DOCOMO) {
                        oir = this.mGoogleImsService.getOirExtraFromDialingNumberForDcm(Pletteting);
                    } else {
                        oir = this.mGoogleImsService.getOirExtraFromDialingNumber(number);
                    }
                    this.mCallProfile.setCallExtraInt("oir", oir);
                    this.mCallProfile.setCallExtraInt("cnap", oir);
                }
                this.mCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.IS_TWO_PHONE_MODE", !TextUtils.isEmpty(cp.getNumberPlus()));
                this.mCallProfile.setCallExtra("com.samsung.telephony.extra.PHOTO_RING_AVAILABLE", cp.getPhotoRing());
                this.mCallProfile.setCallExtra("com.samsung.telephony.extra.ALERT_INFO", cp.getAlertInfo());
                this.mCallProfile.setCallExtra("com.samsung.telephony.extra.DUAL_NUMBER", cp.getNumberPlus());
                this.mCallProfile.setCallExtra("com.samsung.telephony.extra.SKT_CONFERENCE_CALL_SUPPORT", cp.getConferenceSupported());
                if (cp.getHistoryInfo() != null) {
                    this.mCallProfile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_REDIRECT_NUMBER", cp.getHistoryInfo());
                    if ("anonymous".equalsIgnoreCase(cp.getHistoryInfo())) {
                        this.mCallProfile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_PRESENTATION", "1");
                    } else {
                        this.mCallProfile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_PRESENTATION", "0");
                    }
                }
                if (cp.getDtmfEvent() != null) {
                    this.mCallProfile.setCallExtra("com.samsung.telephony.extra.DTMF_EVENT", cp.getDtmfEvent());
                }
                if (!TextUtils.isEmpty(cp.getLineMsisdn())) {
                    this.mCallProfile.setCallExtra("com.samsung.telephony.extra.LINE_MSISDN", cp.getLineMsisdn());
                }
                this.mCallProfile.setCallExtra("com.samsung.telephony.extra.SIP_CALL_ID", cp.getSipCallId());
                this.mCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.MT_CONFERENCE", "1".equals(cp.getIsFocus()));
                if (cp.getDirection() == 0) {
                    if (isCmcSecondaryType(this.mSession.getCmcType())) {
                        this.mCallProfile.setCallExtra("oi", cp.getLetteringText());
                    }
                } else if (mno == Mno.KT) {
                    this.mCallProfile.setCallExtra("oi", ImsCallUtil.removeUriPlusPrefix(cp.getDialingNumber(), "+82", "0", Debug.isProductShip()));
                } else if (mno.isAus()) {
                    this.mCallProfile.setCallExtra("oi", ImsCallUtil.removeUriPlusPrefix(cp.getDialingNumber(), "+61", "0", Debug.isProductShip()));
                } else if (TextUtils.isEmpty(this.mCallProfile.getCallExtra("oi"))) {
                    this.mCallProfile.setCallExtra("oi", cp.getDialingNumber());
                }
                if (!isCmcSecondaryType(this.mSession.getCmcType()) || cp.getDirection() != 0) {
                    this.mCallProfile.setCallExtra("cna", cp.getLetteringText());
                }
                this.mCallProfile.setCallExtraInt("com.samsung.telephony.extra.CMC_RECORDING_EVENT", cp.getCmcRecordEvent());
                this.mCallProfile.mCallType = DataTypeConvertor.convertToGoogleCallType(cp.getCallType());
                this.mCallProfile.mMediaProfile = DataTypeConvertor.convertToGoogleMediaProfile(cp.getMediaProfile());
                this.mCallProfile.setCallExtraBoolean("call_mode_changeable", cp.hasRemoteVideoCapa());
                this.mCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.VIDEO_CRBT", cp.isVideoCRBT());
                if (isCmcSecondaryType(this.mSession.getCmcType())) {
                    this.mCallProfile.setCallExtra("CallRadioTech", String.valueOf(14));
                } else {
                    this.mCallProfile.setCallExtra("CallRadioTech", Integer.toString(cp.getRadioTech()));
                }
                this.mCallProfile.setCallExtraBoolean("com.samsung.telephony.extra.CAN_TRANSFER_CALL", this.mVolteServiceModule.isVolteSupportECT(this.mSession.getPhoneId()));
                this.mCallProfile.setCallExtraInt("com.samsung.telephony.extra.CALL_DIRECTION", cp.getDirection());
                this.mCallProfile.setCallExtraInt("com.samsung.telephony.extra.AUDIO_RX_TRACK_ID", cp.getAudioRxTrackId());
                this.mCallProfile.setCallExtra("feature_caps", cp.getFeatureCaps());
                this.mCallProfile.setCallExtraInt("com.samsung.telephony.extra.VT_RECORDING_STATE", cp.getRecordState());
                if (this.mIsEcbmSupport) {
                    this.mCallProfile.setCallExtraBoolean("imsEcmSupport", true);
                }
                if (this.mVolteServiceModule.getCmcServiceHelper().isCmcRegExist(this.mSession.getPhoneId())) {
                    updateCmcCallExtras(cp);
                }
                setCallProfile(3);
                return;
            }
            return;
        }
        throw new RemoteException();
    }

    public void updateHoldToneType(boolean localHoldTone) {
        if (localHoldTone) {
            this.mCallProfile.mMediaProfile.mAudioDirection = 0;
        }
    }

    public void sendRttMessage(String rttMessage) throws RemoteException {
        this.mSession.sendText(rttMessage, rttMessage.length());
    }

    public void sendRttModifyRequest(ImsCallProfile to) throws RemoteException {
        this.mVolteServiceModule.sendRttSessionModifyRequest(this.mSession.getCallId(), to.mMediaProfile.isRttCall());
    }

    public void sendRttModifyResponse(boolean response) throws RemoteException {
        this.mCallProfile.mMediaProfile.setRttMode(response);
        this.mVolteServiceModule.sendRttSessionModifyResponse(this.mSession.getCallId(), response);
    }

    public void deflect(String deflectNumber) {
    }

    public void transfer(String number, boolean isConfirmationRequired) throws RemoteException {
        if (!isConfirmationRequired) {
            this.mVolteServiceModule.pushCall(this.mSession.getCallId(), number);
            return;
        }
        throw new RemoteException("not support assured transfer");
    }

    public void consultativeTransfer(IImsCallSession mImsCallSession) throws RemoteException {
        this.mVolteServiceModule.consultativeTransferCall(this.mSession.getCallId(), Integer.parseInt(mImsCallSession.getCallId()));
    }

    public void cancelTransferCall() throws RemoteException {
        com.sec.ims.volte2.IImsCallSession iImsCallSession = this.mSession;
        if (iImsCallSession != null) {
            iImsCallSession.cancelTransfer();
            return;
        }
        throw new RemoteException();
    }

    public String participantStatus(int status) {
        switch (status) {
            case 1:
                return "dialing-out";
            case 2:
                return "connected";
            case 3:
                return "disconnecting";
            case 4:
                return "disconnected";
            case 5:
                return "alerting";
            case 6:
                return "muted-via-focus";
            default:
                return "pending";
        }
    }

    public int getTtyModeFromCallType(int phoneId, int callType) {
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule == null) {
            return 0;
        }
        if (!(iVolteServiceModule.getTtyMode(phoneId) == 0)) {
            return 0;
        }
        switch (callType) {
            case 9:
                return 1;
            case 10:
                return 2;
            case 11:
                return 3;
            default:
                return 0;
        }
    }

    public boolean isCmcPrimaryType(int cmcType) {
        if (cmcType == 1 || cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    public boolean isP2pPrimaryType(int cmcType) {
        if (cmcType == 3 || cmcType == 5 || cmcType == 7) {
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
