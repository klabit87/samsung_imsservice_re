package com.sec.internal.ims.servicemodules.volte2;

import android.content.ContentValues;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallStateEvent;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.volte2.data.ConfCallSetupData;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ImsConfSession extends ImsCallSession {
    private final String LOG_TAG = "ImsConfSession";
    /* access modifiers changed from: private */
    public final List<String> mGroupInvitingParticipants = new ArrayList();
    /* access modifiers changed from: private */
    public final SparseArray<String> mGroupParticipants = new SparseArray<>();
    /* access modifiers changed from: private */
    public final List<ImsCallSession> mInvitingParticipants = new ArrayList();
    /* access modifiers changed from: private */
    public boolean mIsExtendToConference = false;
    /* access modifiers changed from: private */
    public final SparseIntArray mParticipantStatus = new SparseIntArray();
    /* access modifiers changed from: private */
    public final SparseArray<ImsCallSession> mParticipants = new SparseArray<>();
    /* access modifiers changed from: private */
    public int mPendingAddParticipantId = 0;

    public enum ConfUpdateCmd {
        UNKNOWN,
        ADD_PARTICIPANT,
        REMOVE_PARTICIPANT;

        public String toString() {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd[ordinal()];
            if (i == 1) {
                return "[ADD_PARTICIPANT]";
            }
            if (i != 2) {
                return "[Unknown]";
            }
            return "[REMOVE_PARTICIPANT]";
        }
    }

    public class ConfCallStateMachine extends CallStateMachine {
        public static final int ON_CONFERENCE_CALL_TIMEOUT = 104;
        static final int ON_PARTICIPANT_ADDED = 101;
        static final int ON_PARTICIPANT_REMOVED = 102;
        static final int ON_PARTICIPANT_UPDATED = 103;
        private int mConfErrorCode = -1;
        /* access modifiers changed from: private */
        public ConfUpdateCmd mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
        /* access modifiers changed from: private */
        public int mPrevActiveSession = -1;
        private boolean mSentConfData = false;
        final ConfCallStateMachine mThisConfSm = this;
        final /* synthetic */ ImsConfSession this$0;

        /* JADX WARNING: Illegal instructions before constructor call */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        ConfCallStateMachine(com.sec.internal.ims.servicemodules.volte2.ImsConfSession r17, android.content.Context r18, com.sec.internal.ims.servicemodules.volte2.ImsCallSession r19, com.sec.ims.ImsRegistration r20, com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r21, com.sec.internal.constants.Mno r22, com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r23, android.os.RemoteCallbackList<com.sec.ims.volte2.IImsCallSessionEventListener> r24, com.sec.internal.interfaces.ims.core.IRegistrationManager r25, com.sec.internal.ims.servicemodules.volte2.IImsMediaController r26, android.os.Looper r27) {
            /*
                r16 = this;
                r13 = r16
                r14 = r17
                r13.this$0 = r14
                java.lang.String r10 = "ConfCallStateMachine"
                r0 = r16
                r1 = r18
                r2 = r19
                r3 = r20
                r4 = r21
                r5 = r22
                r6 = r23
                r7 = r24
                r8 = r25
                r9 = r26
                r11 = r27
                r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
                r0 = -1
                r13.mPrevActiveSession = r0
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfUpdateCmd r1 = com.sec.internal.ims.servicemodules.volte2.ImsConfSession.ConfUpdateCmd.UNKNOWN
                r13.mConfUpdateCmd = r1
                r13.mConfErrorCode = r0
                r0 = 0
                r13.mSentConfData = r0
                r13.mThisConfSm = r13
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$ReadyToCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$ReadyToCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r1 = r16
                r3 = r19
                r6 = r22
                r12 = r16
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mReadyToCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$OutgoingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$OutgoingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mOutgoingCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$AlertingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$AlertingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mAlertingCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$InCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$InCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mInCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$HeldCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$HeldCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mHeldCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$HoldingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$HoldingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mHoldingCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$ResumingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$ResumingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mResumingCall = r15
                com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$EndingCall r15 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession$ConfCallStateMachine$EndingCall
                android.content.Context r2 = r13.mContext
                com.sec.ims.ImsRegistration r4 = r13.mRegistration
                com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r5 = r13.mModule
                com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r7 = r13.mVolteSvcIntf
                android.os.RemoteCallbackList r8 = r13.mListeners
                com.sec.internal.interfaces.ims.core.IRegistrationManager r9 = r13.mRegistrationManager
                com.sec.internal.ims.servicemodules.volte2.IImsMediaController r10 = r13.mMediaController
                r0 = r15
                r0.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)
                r13.mEndingCall = r15
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsConfSession.ConfCallStateMachine.<init>(com.sec.internal.ims.servicemodules.volte2.ImsConfSession, android.content.Context, com.sec.internal.ims.servicemodules.volte2.ImsCallSession, com.sec.ims.ImsRegistration, com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal, com.sec.internal.constants.Mno, com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface, android.os.RemoteCallbackList, com.sec.internal.interfaces.ims.core.IRegistrationManager, com.sec.internal.ims.servicemodules.volte2.IImsMediaController, android.os.Looper):void");
        }

        class ReadyToCall extends ImsReadyToCall {
            ReadyToCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[ReadyToCall] processMessage " + msg.what);
                int i = msg.what;
                if (i != 11) {
                    if (i == 72) {
                        merge(msg.arg1, msg.arg2);
                    } else if (i != 211) {
                        return super.processMessage(msg);
                    } else {
                        Log.i("CallStateMachine", "registration is not available.");
                        ConfCallStateMachine.this.notifyOnError(ImSessionEvent.CONFERENCE_INFO_UPDATED, "No registration.", 0);
                        ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                        confCallStateMachine.transitionTo(confCallStateMachine.mEndingCall);
                        ConfCallStateMachine.this.sendMessage(3);
                    }
                } else if (this.mRegistration != null && !this.mModule.isProhibited(ConfCallStateMachine.this.this$0.mPhoneId)) {
                    if (ConfCallStateMachine.this.mThisConfSm.hasMessages(211)) {
                        ConfCallStateMachine.this.mThisConfSm.removeMessages(211);
                    }
                    if (ConfCallStateMachine.this.this$0.mCallProfile.getConferenceType() != 2) {
                        return super.processMessage(msg);
                    }
                    Log.i("CallStateMachine", "bindToNetwork for Group call");
                    this.mMediaController.bindToNetwork(this.mRegistration.getNetwork());
                    boolean unused = ConfCallStateMachine.this.this$0.mIsExtendToConference = true;
                    if (ConfCallStateMachine.this.this$0.mCallProfile.getDialingNumber() == null) {
                        ConfCallStateMachine.this.this$0.mCallProfile.setDialingNumber(ConfCallStateMachine.this.this$0.getConferenceUri(this.mRegistration.getImsProfile()));
                    }
                    conference((List) msg.obj);
                } else if (!this.mMno.isKor()) {
                    Log.e("CallStateMachine", "start: registration is not available.");
                    ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1001, "No registration."));
                } else if (ConfCallStateMachine.this.this$0.mCallProfile.isForceCSFB()) {
                    Log.e("CallStateMachine", "start: Volte not registered. ForceCSFB");
                    ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(6010, "VOLTE_NOT_REGISTERED"));
                } else {
                    Log.i("CallStateMachine", "IMS is not registered. Wait to 10 sec");
                    ConfCallStateMachine.this.mThisSm.sendMessageDelayed(211, 10000);
                }
                return true;
            }

            private void conference(List<String> participants) {
                ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                confCallStateMachine.callType = confCallStateMachine.this$0.mCallProfile.getCallType();
                if (participants.size() > 0) {
                    ImsProfile profile = this.mRegistration.getImsProfile();
                    ConfCallSetupData data = new ConfCallSetupData(ConfCallStateMachine.this.this$0.getConferenceUri(profile), participants, ConfCallStateMachine.this.callType);
                    data.enableSubscription(ConfCallStateMachine.this.this$0.getConfSubscribeEnabled(profile));
                    data.setSubscribeDialogType(ConfCallStateMachine.this.this$0.getConfSubscribeDialogType(profile));
                    data.setReferUriType(ConfCallStateMachine.this.this$0.getConfReferUriType(profile));
                    data.setRemoveReferUriType(ConfCallStateMachine.this.this$0.getConfRemoveReferUriType(profile));
                    data.setReferUriAsserted(ConfCallStateMachine.this.this$0.getConfReferUriAsserted(profile));
                    data.setOriginatingUri(ConfCallStateMachine.this.this$0.getOriginatingUri());
                    data.setUseAnonymousUpdate(ConfCallStateMachine.this.this$0.getConfUseAnonymousUpdate(profile));
                    data.setSupportPrematureEnd(ConfCallStateMachine.this.this$0.getConfSupportPrematureEnd(profile));
                    int sessionId = this.mVolteSvcIntf.startNWayConferenceCall(this.mRegistration.getHandle(), data);
                    if (sessionId < 0) {
                        ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1001, "Not enough participant."));
                        return;
                    }
                    ConfCallStateMachine.this.this$0.mGroupInvitingParticipants.addAll(participants);
                    Log.i("CallStateMachine", "[ReadyToCall] startNWayConferenceCall() returned session id " + sessionId);
                    ConfCallStateMachine.this.this$0.setSessionId(sessionId);
                    ConfCallStateMachine.this.this$0.mCallProfile.setDirection(0);
                    ConfCallStateMachine confCallStateMachine2 = ConfCallStateMachine.this;
                    int cameraId = confCallStateMachine2.determineCamera(confCallStateMachine2.callType, false);
                    if (cameraId >= 0) {
                        ConfCallStateMachine.this.this$0.startCamera(cameraId);
                    }
                    ConfCallStateMachine confCallStateMachine3 = ConfCallStateMachine.this;
                    confCallStateMachine3.transitionTo(confCallStateMachine3.mOutgoingCall);
                    return;
                }
                ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1001, "Not enough participant."));
            }

            private void merge(int heldCallId, int activeCallId) {
                int i;
                int i2;
                int i3;
                int i4 = heldCallId;
                int i5 = activeCallId;
                Log.i("CallStateMachine", "HeldCallId : " + i4 + " AcitveCallId : " + i5);
                int activeSessionId = -1;
                int heldSessionId = -1;
                List<ImsCallSession> sessions = new ArrayList<>();
                ImsCallSession session = this.mModule.getSessionByCallId(i4);
                if (session != null) {
                    Log.i("CallStateMachine", "Held Session Id : " + session.getSessionId());
                    sessions.add(session);
                    heldSessionId = session.getSessionId();
                }
                ImsCallSession session2 = this.mModule.getSessionByCallId(i5);
                if (!(session2 == null || session2.getCallState() == CallConstants.STATE.ResumingCall || session2.getCallState() == CallConstants.STATE.ResumingVideo)) {
                    Log.i("CallStateMachine", "Active Session Id : " + session2.getSessionId());
                    sessions.add(session2);
                    activeSessionId = session2.getSessionId();
                }
                IMSLog.c(LogClass.VOLTE_MERGE, "Merge," + ConfCallStateMachine.this.this$0.mPhoneId + "," + heldSessionId + "," + activeSessionId);
                if (activeSessionId < 0) {
                    i3 = 4;
                    i2 = -1;
                    i = 0;
                } else if (heldSessionId < 0) {
                    i3 = 4;
                    i2 = -1;
                    i = 0;
                } else {
                    if (ConfCallStateMachine.this.this$0.mCallProfile.getForegroundSessionId() >= 0 && ConfCallStateMachine.this.this$0.mCallProfile.getForegroundSessionId() != activeSessionId) {
                        int tmp = activeSessionId;
                        activeSessionId = heldSessionId;
                        heldSessionId = tmp;
                    }
                    if (session2 != null) {
                        ConfCallStateMachine.this.this$0.mCallProfile.setOriginatingUri(session2.getOriginatingUri());
                    }
                    ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                    confCallStateMachine.callType = confCallStateMachine.this$0.mCallProfile.getCallType();
                    ConfCallStateMachine.this.this$0.mInvitingParticipants.addAll(sessions);
                    if (this.mRegistration == null || this.mRegistration.getImsProfile() == null) {
                        ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1001, "Not Registration."));
                        return;
                    }
                    int mergeCallType = this.mModule.getMergeCallType(ConfCallStateMachine.this.this$0.mPhoneId, ConfCallStateMachine.this.callType == 5 || ConfCallStateMachine.this.callType == 6);
                    ImsProfile profile = this.mRegistration.getImsProfile();
                    String confUri = ConfCallStateMachine.this.this$0.getConferenceUri(profile);
                    if (this.mMno == Mno.KDDI) {
                        Log.i("CallStateMachine", "[KDDI]Change ConfUri for Threeway merge call.");
                        confUri = "sip:mmtel@3pty-factory.ims.mnc051.mcc440.3gppnetwork.org";
                    }
                    ConfCallSetupData data = new ConfCallSetupData(confUri, activeSessionId, heldSessionId, mergeCallType);
                    data.enableSubscription(ConfCallStateMachine.this.this$0.getConfSubscribeEnabled(profile));
                    data.setSubscribeDialogType(ConfCallStateMachine.this.this$0.getConfSubscribeDialogType(profile));
                    data.setReferUriType(ConfCallStateMachine.this.this$0.getConfReferUriType(profile));
                    data.setRemoveReferUriType(ConfCallStateMachine.this.this$0.getConfRemoveReferUriType(profile));
                    data.setReferUriAsserted(ConfCallStateMachine.this.this$0.getConfReferUriAsserted(profile));
                    data.setUseAnonymousUpdate(ConfCallStateMachine.this.this$0.getConfUseAnonymousUpdate(profile));
                    data.setOriginatingUri(ConfCallStateMachine.this.this$0.getOriginatingUri());
                    data.setSupportPrematureEnd(ConfCallStateMachine.this.this$0.getConfSupportPrematureEnd(profile));
                    if (ConfCallStateMachine.this.this$0.mCallProfile.getAdditionalSipHeaders() != null) {
                        data.setExtraSipHeaders(ConfCallStateMachine.this.this$0.mCallProfile.getAdditionalSipHeaders());
                    }
                    int sessionId = this.mVolteSvcIntf.startNWayConferenceCall(this.mRegistration.getHandle(), data);
                    if (sessionId < 0) {
                        ConfCallStateMachine.this.mThisSm.sendMessage(4, 0, -1, new SipError(1001, "remote exception."));
                        return;
                    }
                    ConfCallStateMachine.this.this$0.setSessionId(sessionId);
                    if (ConfCallStateMachine.this.determineCamera(mergeCallType, false) >= 0) {
                        ConfCallStateMachine.this.this$0.startCamera(-1);
                    }
                    int unused = ConfCallStateMachine.this.mPrevActiveSession = activeSessionId;
                    ConfCallStateMachine.this.this$0.mCallProfile.setDirection(0);
                    ConfCallStateMachine.this.mThisConfSm.sendMessageDelayed(104, 45000);
                    ConfCallStateMachine confCallStateMachine2 = ConfCallStateMachine.this;
                    confCallStateMachine2.transitionTo(confCallStateMachine2.mOutgoingCall);
                    return;
                }
                ConfCallStateMachine.this.mThisSm.sendMessage(i3, i, i2, new SipError(1001, "Not enough participant."));
            }
        }

        class OutgoingCall extends ImsOutgoingCall {
            OutgoingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public void enter() {
                ConfCallStateMachine.this.callType = 0;
                ConfCallStateMachine.this.errorCode = -1;
                ConfCallStateMachine.this.errorMessage = "";
                Log.i("CallStateMachine", "Enter [OutgoingCall]");
                if (ConfCallStateMachine.this.this$0.mCallProfile != null) {
                    ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                    confCallStateMachine.callType = confCallStateMachine.this$0.mCallProfile.getCallType();
                }
                ConfCallStateMachine confCallStateMachine2 = ConfCallStateMachine.this;
                int cameraId = confCallStateMachine2.determineCamera(confCallStateMachine2.callType, false);
                if (cameraId >= 0) {
                    ConfCallStateMachine.this.this$0.startCamera(cameraId);
                }
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[OutgoingCall] processMessage " + msg.what);
                int i = msg.what;
                if (i == 4) {
                    int retryafter = msg.arg1;
                    if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                        return super.processMessage(msg);
                    }
                    SipError err = (SipError) msg.obj;
                    Log.e("CallStateMachine", "[OutgoingCall] conference error code: " + err.getCode() + ": errorMessage " + err.getReason() + ": Retry After " + retryafter);
                    ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                    confCallStateMachine.notifyOnError(1104, confCallStateMachine.errorMessage, Math.max(retryafter, 0));
                    ConfCallStateMachine.this.onConferenceFailError();
                    return true;
                } else if (i != 41) {
                    if (i != 104) {
                        return super.processMessage(msg);
                    }
                    return false;
                } else if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                    return super.processMessage(msg);
                } else {
                    ConfCallStateMachine.this.onConferenceEstablished();
                    return true;
                }
            }
        }

        class AlertingCall extends ImsAlertingCall {
            AlertingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public void enter() {
                ConfCallStateMachine.this.callType = 0;
                ConfCallStateMachine.this.errorCode = -1;
                ConfCallStateMachine.this.errorMessage = "";
                Log.i("CallStateMachine", "Enter [AlertingCall]");
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[AlertingCall] processMessage " + msg.what);
                int i = msg.what;
                if (i != 4) {
                    if (i != 41) {
                        if (i != 104) {
                            return super.processMessage(msg);
                        }
                        return false;
                    } else if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                        return super.processMessage(msg);
                    } else {
                        ConfCallStateMachine.this.onConferenceEstablished();
                        return true;
                    }
                } else if (!ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                    return super.processMessage(msg);
                } else {
                    SipError err = (SipError) msg.obj;
                    Log.e("CallStateMachine", "[AlertingCall] conference error code: " + err.getCode() + ": errorMessage " + err.getReason());
                    ConfCallStateMachine confCallStateMachine = ConfCallStateMachine.this;
                    confCallStateMachine.notifyOnError(1104, confCallStateMachine.errorMessage);
                    ConfCallStateMachine.this.onConferenceFailError();
                    return true;
                }
            }
        }

        class InCall extends ImsInCall {
            InCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[InCall] processMessage " + msg.what);
                int i = msg.what;
                if (!(i == 1 || i == 3)) {
                    if (i == 73) {
                        boolean unused = ConfCallStateMachine.this.this$0.mIsExtendToConference = true;
                        return super.processMessage(msg);
                    } else if (i == 75) {
                        onReferStatus(msg);
                        return true;
                    } else if (i == 91) {
                        ConfCallStateMachine.this.notifyOnModified(msg.arg1);
                        if (ConfCallStateMachine.this.this$0.mPendingAddParticipantId != 0) {
                            ConfCallStateMachine.this.this$0.smCallStateMachine.sendMessage(53, ConfCallStateMachine.this.this$0.mPendingAddParticipantId, 0, (Object) null);
                            int unused2 = ConfCallStateMachine.this.this$0.mPendingAddParticipantId = 0;
                        }
                        return true;
                    } else if (!(i == 53 || i == 54)) {
                        switch (i) {
                            case 101:
                            case 102:
                            case 103:
                            case 104:
                                break;
                            default:
                                return super.processMessage(msg);
                        }
                    }
                }
                return false;
            }

            /* access modifiers changed from: package-private */
            public void onReferStatus(Message msg) {
                if (this.mMno == Mno.LGU && msg.arg1 > 200 && !ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall()) {
                    Log.e("CallStateMachine", "[InCall] On_Refer_Status conference setup fail error=" + msg.arg1);
                    ConfCallStateMachine.this.notifyOnError(1105, "Add user to session failure");
                } else if ((msg.arg1 == 400 || msg.arg1 == 403 || msg.arg1 == 404 || msg.arg1 == 488 || msg.arg1 == 405) && this.mMno != Mno.KDDI) {
                    Log.e("CallStateMachine", "[InCall] On_Refer_Status Fail Error");
                    ConfCallStateMachine.this.onReferStatusFailError();
                } else if (msg.arg1 == 487 && ConfCallStateMachine.this.mConfUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                    Log.i("CallStateMachine", "[InCall] On_Refer_Status ADD USER FAILED : notify error 487");
                    ConfCallStateMachine.this.this$0.mInvitingParticipants.clear();
                }
            }
        }

        class HoldingCall extends ImsHoldingCall {
            HoldingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[HoldingCall] processMessage " + msg.what);
                int i = msg.what;
                if (i == 1 || i == 53 || i == 54) {
                    return false;
                }
                switch (i) {
                    case 101:
                    case 102:
                    case 103:
                    case 104:
                        return false;
                    default:
                        return super.processMessage(msg);
                }
            }
        }

        class HeldCall extends ImsHeldCall {
            HeldCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public void enter() {
                ConfCallStateMachine.this.callType = 0;
                ConfCallStateMachine.this.errorCode = -1;
                ConfCallStateMachine.this.errorMessage = "";
                ConfCallStateMachine.this.notifyOnHeld(true);
                Log.i("CallStateMachine", "Enter [HeldCall]");
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[HeldCall] processMessage " + msg.what);
                int i = msg.what;
                if (i == 1) {
                    return false;
                }
                if (i == 75) {
                    onReferStatus(msg);
                    return true;
                } else if (i == 53 || i == 54) {
                    return false;
                } else {
                    switch (i) {
                        case 101:
                        case 102:
                        case 103:
                        case 104:
                            return false;
                        default:
                            return super.processMessage(msg);
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void onReferStatus(Message msg) {
                if ((msg.arg1 == 403 || msg.arg1 == 404 || msg.arg1 == 488 || msg.arg1 == 405) && this.mMno != Mno.KDDI) {
                    Log.e("CallStateMachine", "[HeldCall] On_Refer_Status Fail Error");
                    ConfCallStateMachine.this.onReferStatusFailError();
                } else if (msg.arg1 == 487 && ConfCallStateMachine.this.mConfUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                    Log.i("CallStateMachine", "[HeldCall] On_Refer_Status ADD USER FAILED : notify error 487");
                    ConfCallStateMachine.this.this$0.mInvitingParticipants.clear();
                }
            }
        }

        class ResumingCall extends ImsResumingCall {
            ResumingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[ResumingCall] processMessage " + msg.what);
                int i = msg.what;
                if (!(i == 1 || i == 3)) {
                    if (i != 4) {
                        if (!(i == 53 || i == 54)) {
                            switch (i) {
                                case 101:
                                case 102:
                                case 103:
                                case 104:
                                    break;
                            }
                        }
                    } else {
                        SipError err = (SipError) msg.obj;
                        int errorCode = err.getCode();
                        if (ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall() && errorCode == 800) {
                            Log.e("CallStateMachine", "[ResumingCall] conference error code: " + errorCode + ": errorMessage " + err.getReason() + "handle as NOT_HANDLED");
                            return false;
                        }
                    }
                    return super.processMessage(msg);
                }
                return false;
            }
        }

        class EndingCall extends ImsEndingCall {
            EndingCall(Context context, ImsCallSession session, ImsRegistration reg, IVolteServiceModuleInternal volteModule, Mno mno, IVolteServiceInterface stackIf, RemoteCallbackList<IImsCallSessionEventListener> listener, IRegistrationManager rm, IImsMediaController mediactnr, Looper looper, CallStateMachine csm) {
                super(context, session, reg, volteModule, mno, stackIf, listener, rm, mediactnr, looper, csm);
            }

            public boolean processMessage(Message msg) {
                Log.i("CallStateMachine", "[EndingCall] processMessage " + msg.what);
                if (msg.what != 3) {
                    return super.processMessage(msg);
                }
                if (ConfCallStateMachine.this.this$0.mCallProfile.isConferenceCall() && (this.mMno == Mno.CMCC || this.mMno == Mno.CU || this.mMno == Mno.SAMSUNG)) {
                    Log.e("CallStateMachine", "[EndingCall] conference ENDED");
                    ConfCallStateMachine.this.onConferenceFailError();
                }
                return super.processMessage(msg);
            }
        }

        /* access modifiers changed from: protected */
        public void unhandledMessage(Message msg) {
            Log.i("ImsConfSession", "[ANY_STATE] unhandledMessage " + msg.what);
            int i = msg.what;
            if (i == 1) {
                onConferenceEnded();
                super.unhandledMessage(msg);
            } else if (i != 3) {
                if (i != 4) {
                    if (i == 53) {
                        addConferenceParticipant(msg);
                    } else if (i != 54) {
                        switch (i) {
                            case 101:
                                onConferenceParticipantAdded(msg);
                                return;
                            case 102:
                                onConferenceParticipantRemoved(msg);
                                return;
                            case 103:
                                onConferenceParticipantUpdated(msg);
                                return;
                            case 104:
                                onConferenceCallTimeout();
                                return;
                            default:
                                super.unhandledMessage(msg);
                                return;
                        }
                    } else {
                        removeConferenceParticipant(msg);
                    }
                } else if (this.this$0.mCallProfile.isConferenceCall()) {
                    SipError err = (SipError) msg.obj;
                    int errorCode = err.getCode();
                    String errorMessage = err.getReason();
                    Log.e("ImsConfSession", "[ANY_STATE] conference error code: " + errorCode + ": errorMessage " + errorMessage + " ConfUpdateCmd: " + this.mConfUpdateCmd.toString());
                    if (this.mConfUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                        if (this.this$0.mCallProfile.getConferenceType() == 1) {
                            Log.e("ImsConfSession", "Participant add fail, clear list");
                            this.this$0.mInvitingParticipants.clear();
                        }
                        notifyOnError(1105, errorMessage, 0);
                    } else if (this.mConfUpdateCmd == ConfUpdateCmd.REMOVE_PARTICIPANT) {
                        notifyOnError(1106, errorMessage, 0);
                    }
                    this.mConfErrorCode = errorCode;
                    onConferenceFailError(msg, this.mConfUpdateCmd);
                } else {
                    super.unhandledMessage(msg);
                }
            } else if (!this.this$0.mCallProfile.isConferenceCall() || this.this$0.mInvitingParticipants.size() <= 0) {
                super.unhandledMessage(msg);
            } else {
                Log.e("ImsConfSession", "[ANY_STATE] Conference call ended before merge request is not completed");
                for (ImsCallSession participants : this.this$0.mInvitingParticipants) {
                    try {
                        participants.terminate(7);
                    } catch (RemoteException e) {
                    }
                }
                this.mVolteSvcIntf.endCall(this.this$0.getSessionId(), this.this$0.mCallProfile.getCallType(), getSipReasonFromUserReason(7));
                onConferenceEnded();
                transitionTo(this.mEndingCall);
                super.unhandledMessage(msg);
            }
        }

        /* access modifiers changed from: private */
        public void onConferenceFailError() {
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
            if (this.this$0.mParticipants.size() <= 0) {
                onConferenceEnded();
                if (this.this$0.getCallState() != CallConstants.STATE.EndingCall) {
                    transitionTo(this.mEndingCall);
                    sendMessage(3);
                }
            }
            if (this.this$0.mCallProfile.getCallType() != 5 && this.this$0.mCallProfile.getCallType() != 6) {
                try {
                    ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                    if (session != null) {
                        Log.e("ImsConfSession", "conf fail; resume session:: " + this.mPrevActiveSession);
                        session.resume();
                    }
                    this.mPrevActiveSession = -1;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private void addConferenceParticipant(Message msg) {
            String[] uris;
            this.mConfUpdateCmd = ConfUpdateCmd.ADD_PARTICIPANT;
            if (this.this$0.mCallProfile.getConferenceType() == 2) {
                if (this.this$0.mIsExtendToConference) {
                    uris = (String[]) msg.obj;
                } else {
                    uris = ((String) msg.obj).split("\\$");
                }
                for (String tempUri : uris) {
                    ImsConfSession imsConfSession = this.this$0;
                    ImsUri uri = imsConfSession.buildUri(tempUri, (String) null, imsConfSession.mCallProfile.getCallType());
                    Log.i("ImsConfSession", "addConferenceParticipant " + IMSLog.checker(uri.toString()));
                    this.this$0.mGroupInvitingParticipants.add(uri.toString());
                    if (this.mVolteSvcIntf.addParticipantToNWayConferenceCall(this.this$0.getSessionId(), uri.toString()) < 0) {
                        Log.e("ImsConfSession", "addConferenceParticipant failed.");
                        return;
                    }
                }
                return;
            }
            int callId = msg.arg1;
            ImsCallSession session = this.mModule.getSessionByCallId(callId);
            if (session == null) {
                Log.e("ImsConfSession", "[ANY_STATE] ADD_PARTICIPANT: session not exist with callId=" + callId);
            } else if (session.getCallState() == CallConstants.STATE.InCall || session.getCallState() == CallConstants.STATE.HeldCall) {
                this.this$0.mInvitingParticipants.add(session);
                if (this.this$0.getCallState() == CallConstants.STATE.HeldCall) {
                    this.mPrevActiveSession = session.getSessionId();
                }
                if (this.mVolteSvcIntf.addParticipantToNWayConferenceCall(this.this$0.getSessionId(), session.getSessionId()) < 0) {
                    Log.e("ImsConfSession", "addConferenceParticipant: fail.");
                }
            } else {
                Log.e("ImsConfSession", "[ANY_STATE] call to be added is neither InCall nor HeldCall.");
            }
        }

        private void removeConferenceParticipant(Message msg) {
            this.mConfUpdateCmd = ConfUpdateCmd.REMOVE_PARTICIPANT;
            if (this.mMno.isKor()) {
                Log.i("ImsConfSession", "KOR operator do not support remove participant");
            } else if (this.this$0.mCallProfile.getConferenceType() == 2) {
                ImsConfSession imsConfSession = this.this$0;
                ImsUri uri = imsConfSession.buildUri((String) msg.obj, (String) null, imsConfSession.mCallProfile.getCallType());
                Log.i("ImsConfSession", "removeConferenceParticipant " + IMSLog.checker(uri.toString()));
                if (this.mVolteSvcIntf.removeParticipantFromNWayConferenceCall(this.this$0.getSessionId(), uri.toString()) < 0) {
                    Log.e("ImsConfSession", "removeConferenceParticipant failed.");
                }
            } else {
                int callId = msg.arg1;
                int participantId = this.this$0.getParticipantId(callId);
                if (participantId == -1) {
                    Log.e("ImsConfSession", "[ANY_STATE] REMOVE_PARTICIPANT: session not exist with callId=" + callId);
                } else if (this.mVolteSvcIntf.removeParticipantFromNWayConferenceCall(this.this$0.getSessionId(), participantId) < 0) {
                    Log.e("ImsConfSession", "removeConferenceParticipant: fail.");
                }
            }
        }

        private void onConferenceParticipantAdded(Message msg) {
            for (CallStateEvent.ParticipantUser p : (List) msg.obj) {
                ImsCallSession session = this.this$0.getSessionFromInvitingParticipants(p.getSessionId());
                if (this.this$0.mParticipants.get(p.getParticipantId()) != null) {
                    Log.e("ImsConfSession", "[ANY_STATE] already added participantId=" + p.getParticipantId());
                } else if (session == null) {
                    Log.e("ImsConfSession", "[ANY_STATE] ON_PARTICIPANT_ADDED: session not exist with sessionId=" + p.getSessionId());
                } else {
                    int participantstatus = 2;
                    if (session.isRemoteHeld()) {
                        participantstatus = 6;
                    }
                    Log.i("ImsConfSession", "[ANY_STATE] participant status=" + participantstatus);
                    notifyParticipantAdded(session.getCallId());
                    this.mModule.onConferenceParticipantAdded(this.this$0.getSessionId(), p.getUri());
                    this.this$0.mInvitingParticipants.remove(session);
                    this.this$0.mParticipants.put(p.getParticipantId(), session);
                    this.this$0.mParticipantStatus.put(p.getParticipantId(), participantstatus);
                    Log.i("ImsConfSession", "[ANY_STATE] participant added - sessionId=" + p.getSessionId() + " participantId=" + p.getParticipantId());
                    if (this.this$0.mInvitingParticipants.size() == 0) {
                        Log.i("ImsConfSession", "[ANY_STATE] all participant add success!");
                        this.mThisConfSm.removeMessages(104);
                    }
                }
            }
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
        }

        private void onConferenceParticipantRemoved(Message msg) {
            for (CallStateEvent.ParticipantUser p : (List) msg.obj) {
                ImsCallSession session = (ImsCallSession) this.this$0.mParticipants.get(p.getParticipantId());
                if (session == null) {
                    Log.e("ImsConfSession", "[ANY_STATE] ON_PARTICIPANT_REMOVED: participant not exist. participantId=" + p.getParticipantId());
                } else {
                    notifyParticipantRemoved(session.getCallId());
                    this.mModule.onConferenceParticipantRemoved(this.this$0.getSessionId(), p.getUri());
                    this.this$0.mParticipants.remove(p.getParticipantId());
                    this.this$0.mParticipantStatus.delete(p.getParticipantId());
                    Log.i("ImsConfSession", "[ANY_STATE] partcitipant removed - sessionId=" + p.getSessionId() + " participantId=" + p.getParticipantId());
                }
            }
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
            checkParticipantCount();
        }

        private void onConferenceParticipantUpdated(Message msg) {
            updateConferenceParticipants((List) msg.obj);
            checkParticipantCount();
            Log.i("ImsConfSession", "[ANY_STATE] participant list updated ");
        }

        private boolean isErrorCodeToResumeSession(int errorCode) {
            return errorCode == 486 || errorCode == 487 || errorCode == 480 || errorCode == 403 || errorCode == 503 || errorCode == 400 || errorCode == 606;
        }

        private void handleConferenceFailResumeError(Message msg) {
            try {
                ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                int errorCode = ((SipError) msg.obj).getCode();
                if (session != null) {
                    Log.e("ImsConfSession", "conf fail; resume session:: " + this.mPrevActiveSession);
                    session.resume();
                }
                if (((errorCode == 487 || errorCode == 606) && this.mMno.isChn()) || ((errorCode == 403 || errorCode == 480) && this.mMno == Mno.IDEA_INDIA)) {
                    this.mVolteSvcIntf.endCall(this.this$0.getSessionId(), this.this$0.mCallProfile.getCallType(), getSipReasonFromUserReason(7));
                    onConferenceEnded();
                    transitionTo(this.mEndingCall);
                    super.unhandledMessage(msg);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void handleConferenceFailError(Message msg, ConfUpdateCmd confUpdateCmd) {
            try {
                Log.i("ImsConfSession", "confUpdateCmd : " + confUpdateCmd);
                int errorCode = ((SipError) msg.obj).getCode();
                if (confUpdateCmd != ConfUpdateCmd.ADD_PARTICIPANT) {
                    if (errorCode != 800 || this.mMno != Mno.KDDI) {
                        if ((errorCode != 500 || (!this.mMno.isChn() && this.mMno != Mno.DLOG)) && !(errorCode == 5000 && this.mMno == Mno.TELIA_FINLAND)) {
                            List<ImsCallSession> sessions = this.mModule.getSessionByState(this.this$0.mPhoneId, CallConstants.STATE.HeldCall);
                            for (int i = 0; i < sessions.size(); i++) {
                                Log.e("ImsConfSession", "conf fail; terminate callsession; session::" + sessions.get(i).getCallId());
                                sessions.get(i).terminate(7);
                            }
                        } else {
                            ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                            if (session != null) {
                                Log.i("ImsConfSession", "conf fail; resume session:: " + this.mPrevActiveSession + ", errorCode: " + errorCode);
                                session.resume();
                            }
                        }
                        this.mVolteSvcIntf.endCall(this.this$0.getSessionId(), this.this$0.mCallProfile.getCallType(), getSipReasonFromUserReason(7));
                        onConferenceEnded();
                        transitionTo(this.mEndingCall);
                        super.unhandledMessage(msg);
                    }
                } else if (errorCode >= 5000) {
                    onConferenceEnded();
                    transitionTo(this.mEndingCall);
                    sendMessage(3);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private void onConferenceFailError(Message msg, ConfUpdateCmd confUpdateCmd) {
            this.mConfUpdateCmd = ConfUpdateCmd.UNKNOWN;
            int errorCode = ((SipError) msg.obj).getCode();
            Log.e("ImsConfSession", "[ANY_STATE] onConferenceFailError : " + errorCode);
            if (this.this$0.mCallProfile.getCallType() != 5 && this.this$0.mCallProfile.getCallType() != 6) {
                if (isErrorCodeToResumeSession(errorCode)) {
                    handleConferenceFailResumeError(msg);
                } else {
                    handleConferenceFailError(msg, confUpdateCmd);
                }
                this.mPrevActiveSession = -1;
            }
        }

        private void onConferenceEnded() {
            if (!this.mSentConfData) {
                ContentValues confItem = new ContentValues();
                confItem.put(DiagnosisConstants.PSCI_KEY_PARTICIPANT_NUMBER, Integer.valueOf(this.this$0.mParticipants.size()));
                ImsLogAgentUtil.storeLogToAgent(this.this$0.mPhoneId, this.mContext, DiagnosisConstants.FEATURE_PSCI, confItem);
                this.mSentConfData = true;
            }
            this.this$0.mParticipantStatus.clear();
            this.this$0.mParticipants.clear();
            this.mThisConfSm.removeMessages(104);
        }

        /* access modifiers changed from: private */
        public void onConferenceEstablished() {
            this.this$0.notifyOnConferenceEstablished();
            transitionTo(this.mInCall);
        }

        /* access modifiers changed from: package-private */
        public void notifyParticipantAdded(int participantId) {
            int length = this.mListeners.beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mListeners.getBroadcastItem(i).onParticipantAdded(participantId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }

        /* access modifiers changed from: package-private */
        public void notifyParticipantRemoved(int participantId) {
            int length = this.mListeners.beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mListeners.getBroadcastItem(i).onParticipantRemoved(participantId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }

        /* access modifiers changed from: package-private */
        public void notifyParticipantsUpdated(String[] participant, int[] status, int[] sipError) {
            int length = this.mListeners.beginBroadcast();
            for (int i = 0; i < length; i++) {
                try {
                    this.mListeners.getBroadcastItem(i).onParticipantUpdated(this.this$0.getSessionId(), participant, status, sipError);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mListeners.finishBroadcast();
        }

        /* access modifiers changed from: package-private */
        public void updateConferenceParticipants(List<CallStateEvent.ParticipantUser> updatedList) {
            if (this.this$0.mCallProfile.getConferenceType() == 1) {
                updateNwayConferenceParticipants(updatedList);
            } else if (this.this$0.mCallProfile.getConferenceType() == 2) {
                updateGroupConferenceParticipants(updatedList);
            }
        }

        private void updateNwayConferenceParticipants(List<CallStateEvent.ParticipantUser> updatedList) {
            for (CallStateEvent.ParticipantUser user : updatedList) {
                ImsUri uri = ImsUri.parse(user.getUri());
                String participant = uri != null ? uri.getMsisdn() : user.getUri();
                int participantId = user.getParticipantId();
                int status = user.getParticipantStatus();
                ImsCallSession session = (ImsCallSession) this.this$0.mParticipants.get(participantId);
                Log.i("ImsConfSession", "updateConferenceParticipants: " + participantId + " status " + participantStatus(status));
                if (session != null) {
                    if (status == 4) {
                        Log.i("ImsConfSession", "old participant in non-active state. remove it." + IMSLog.checker(participant));
                        notifyParticipantRemoved(session.getCallId());
                        this.mModule.onConferenceParticipantRemoved(this.this$0.getSessionId(), user.getUri());
                        this.this$0.mParticipants.remove(participantId);
                        this.this$0.mParticipantStatus.delete(participantId);
                    } else {
                        int prevStatus = this.this$0.mParticipantStatus.get(participantId);
                        this.this$0.mParticipantStatus.put(user.getParticipantId(), status);
                        if (status == 6 && prevStatus != 6 && Mno.RJIL != this.mMno) {
                            notifyConfParticipantOnHeld(session.getCallId(), false);
                        } else if (!(status != 2 || prevStatus == 2 || Mno.RJIL == this.mMno)) {
                            notifyConfParticipanOnResumed(session.getCallId(), false);
                        }
                    }
                }
                Log.i("ImsConfSession", "updateConferenceParticipants: new participant.");
            }
        }

        private void updateGroupConferenceParticipants(List<CallStateEvent.ParticipantUser> updatedList) {
            int participantSize = updatedList.size();
            Log.i("ImsConfSession", "updateGroupConferenceParticipants participantSize=" + participantSize);
            String[] participant = new String[participantSize];
            int[] status = new int[participantSize];
            int[] sipError = new int[participantSize];
            int[] participantId = new int[participantSize];
            for (int i = 0; i < participantSize; i++) {
                CallStateEvent.ParticipantUser user = updatedList.get(i);
                ImsUri imsUri = ImsUri.parse(user.getUri());
                participant[i] = imsUri != null ? imsUri.getMsisdn() : user.getUri();
                status[i] = user.getParticipantStatus();
                participantId[i] = user.getParticipantId();
                this.this$0.mParticipantStatus.put(participantId[i], status[i]);
                Log.i("ImsConfSession", "participant=" + IMSLog.checker(participant[i]) + ", participantId=" + participantId[i] + ", status=" + participantStatus(status[i]));
                if (status[i] == 4) {
                    this.this$0.mGroupParticipants.remove(participantId[i]);
                    sipError[i] = this.mConfErrorCode;
                } else {
                    this.this$0.mGroupParticipants.put(participantId[i], participant[i]);
                    sipError[i] = 0;
                }
            }
            this.mConfErrorCode = -1;
            notifyParticipantsUpdated(participant, status, sipError);
        }

        private void checkParticipantCount() {
            Log.i("ImsConfSession", "checkParticipantCount mParticipants=" + this.this$0.mParticipants.size() + ", mGroupParticipants=" + this.this$0.mGroupParticipants.size());
            if (this.this$0.mCallProfile.getConferenceType() == 1) {
                if (this.this$0.mParticipants.size() == 0 && this.this$0.mInvitingParticipants.size() == 0) {
                    this.mThisSm.sendMessage(1, 5);
                }
            } else if (this.this$0.mCallProfile.getConferenceType() == 2 && this.mMno == Mno.KDDI && this.this$0.mGroupParticipants.size() == 0) {
                this.mThisSm.sendMessage(1, 5);
            }
        }

        private void onConferenceCallTimeout() {
            Log.i("ImsConfSession", "onConferenceCallTimeout");
            try {
                for (ImsCallSession tempSession : this.this$0.mInvitingParticipants) {
                    tempSession.terminate(7);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.mThisSm.sendMessage(1, 5);
        }

        private String participantStatus(int status) {
            switch (status) {
                case 1:
                    return "INVITING";
                case 2:
                    return "ACTIVE";
                case 3:
                    return "REMOVING";
                case 4:
                    return "NON_ACTIVE";
                case 5:
                    return "ALERTING";
                case 6:
                    return "ON-HOLD";
                default:
                    return "UNKNOWN";
            }
        }

        /* access modifiers changed from: private */
        public void onReferStatusFailError() {
            if (this.mConfUpdateCmd == ConfUpdateCmd.ADD_PARTICIPANT) {
                Log.e("ImsConfSession", "On_Refer_Status ADD USER FAILED");
                notifyOnError(1105, this.errorMessage, 0);
                this.this$0.mInvitingParticipants.clear();
            } else if (this.mConfUpdateCmd == ConfUpdateCmd.REMOVE_PARTICIPANT) {
                Log.e("ImsConfSession", "On_Refer_Status REMOVE USER FAILED");
                notifyOnError(1106, this.errorMessage, 0);
            } else if (this.this$0.mIsExtendToConference) {
                Log.e("ImsConfSession", "On_Refer_Status extendToConference failed.");
                boolean unused = this.this$0.mIsExtendToConference = false;
                notifyOnError(1105, "Add user to session failure");
            } else {
                Log.i("ImsConfSession", "On_Refer_Status TerminateConference");
                this.mThisSm.sendMessage(1, 5);
                try {
                    ImsCallSession session = this.mModule.getSession(this.mPrevActiveSession);
                    if (session != null) {
                        Log.e("ImsConfSession", "Conf Fail; Resume Session:: " + this.mPrevActiveSession);
                        session.resume();
                    }
                    this.mPrevActiveSession = -1;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ImsConfSession(Context context, CallProfile profile, ImsRegistration reg, Looper looper, IVolteServiceModuleInternal module) {
        super(context, profile, reg, looper, module);
    }

    public void init(IVolteServiceInterface stackIf, IRegistrationManager rm) {
        this.mVolteSvcIntf = stackIf;
        this.mRegistrationManager = rm;
        if (this.mRegistration == null) {
            this.mMno = SimUtil.getSimMno(this.mPhoneId);
        } else {
            this.mMno = Mno.fromName(this.mRegistration.getImsProfile().getMnoName());
        }
        this.smCallStateMachine = new ConfCallStateMachine(this, this.mContext, this, this.mRegistration, this.mModule, this.mMno, this.mVolteSvcIntf, this.mListeners, this.mRegistrationManager, this.mMediaController, this.mLooper);
        this.smCallStateMachine.init();
        this.mImsCallDedicatedBearer = new ImsCallDedicatedBearer(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine);
        this.mImsCallSessionEventHandler = new ImsCallSessionEventHandler(this, this.mModule, this.mRegistration, this.mRegistrationManager, this.mMno, this.mAm, this.smCallStateMachine, this.mCallProfile, this.mVolteSvcIntf, this.mMediaController);
        this.mVolteSvcIntf.registerForCallStateEvent(this.mVolteStackEventHandler, 1, (Object) null);
        this.mVolteSvcIntf.registerForReferStatus(this.mVolteStackEventHandler, 5, this);
        this.mMediaController.registerForMediaEvent(this);
        Log.i("ImsConfSession", "start ConfCallStateMachine state");
        this.smCallStateMachine.start();
        setIsNrSaMode();
    }

    public int start(String target, CallProfile profile) throws RemoteException {
        if (profile == null) {
            profile = this.mCallProfile;
        }
        if (profile.getConferenceType() == 2) {
            startConference(target.split("\\$"), profile);
            return 0;
        }
        super.start(target, profile);
        return 0;
    }

    public void startIncoming() {
        super.startIncoming();
    }

    public void merge(int heldsessionId, int activesessionId) {
        this.smCallStateMachine.sendMessage(72, heldsessionId, activesessionId, (Object) null);
    }

    public void startConference(String[] participants, CallProfile profile) throws RemoteException {
        if (profile == null) {
            Log.e("ImsConfSession", "startConference(): profile is NULL");
            throw new RemoteException("Cannot make conference call: profile is null");
        } else if (participants != null) {
            List<String> participantsList = new ArrayList<>();
            for (String p : participants) {
                participantsList.add(buildUri(p, (String) null, profile.getCallType()).toString());
            }
            this.smCallStateMachine.sendMessage(11, (Object) participantsList);
        } else {
            Log.e("ImsConfSession", "start(): there is no participants");
            throw new RemoteException("Cannot conference : participants is null");
        }
    }

    public void inviteParticipants(int participantId) {
        ImsCallSession participant = this.mModule.getSessionByCallId(participantId);
        ImsProfile profile = this.mRegistration.getImsProfile();
        if (!(profile == null || !profile.getSupportUpgradeVideoConference() || this.mCallProfile == null || participant == null || participant.getCallProfile() == null || ImsCallUtil.isVideoCall(this.mCallProfile.getCallType()) || !ImsCallUtil.isVideoCall(participant.getCallProfile().getCallType()))) {
            Log.i("ImsConfSession", "Need to Upgrade to Conference Call for add Video Participants");
            startCamera(-1);
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(2);
            if (this.smCallStateMachine.modifyCallType(callProfile, true)) {
                Log.i("ImsConfSession", "Modify Request success pending add Participant");
                this.mPendingAddParticipantId = participantId;
                return;
            }
        }
        this.smCallStateMachine.sendMessage(53, participantId, 0, (Object) null);
    }

    public void removeParticipants(int participantId) {
        this.smCallStateMachine.sendMessage(54, participantId, 0, (Object) null);
    }

    public void inviteGroupParticipant(String participant) {
        this.smCallStateMachine.sendMessage(53, (Object) participant);
    }

    public void removeGroupParticipant(String participant) {
        this.smCallStateMachine.sendMessage(54, (Object) participant);
    }

    public void extendToConference(String[] participants) throws RemoteException {
        if (this.mIsExtendToConference) {
            this.smCallStateMachine.sendMessage(53, (Object) participants);
        } else {
            super.extendToConference(participants);
        }
    }

    public void holdVideo() {
        Log.i("ImsConfSession", "Unsupported API - holdVideo()");
    }

    public void resumeVideo() {
        Log.i("ImsConfSession", "Unsupported API - resumeVideo()");
    }

    public void sendText(String text, int len) {
        super.sendText(text, len);
    }

    public void sendDtmf(int code, int duration, Message result) throws RemoteException {
        super.sendDtmf(code, duration, result);
    }

    public void startDtmf(int code) throws RemoteException {
        super.startDtmf(code);
    }

    public void stopDtmf() throws RemoteException {
        super.stopDtmf();
    }

    public void setTtyMode(int ttyMode) {
        Log.e("ImsConfSession", "Not supported operation");
    }

    /* access modifiers changed from: protected */
    public void onImsCallEvent(CallStateEvent event) {
        Log.i("ImsConfSession", "mCallProfile.isConferenceCall() " + this.mCallProfile.isConferenceCall());
        if (!this.mCallProfile.isConferenceCall()) {
            if (this.mMno != Mno.SKT && this.mMno != Mno.LGU) {
                super.onImsCallEvent(event);
                return;
            } else if (!event.isConference()) {
                super.onImsCallEvent(event);
                return;
            } else {
                Log.i("ImsConfSession", "Change to callprofile type");
                this.mCallProfile.setConferenceCall(2);
            }
        }
        if (event.getSessionID() != getSessionId()) {
            Log.i("ImsConfSession", "not interest other sessionId " + event.getSessionID());
            return;
        }
        Log.i("ImsConfSession", "event state : " + event.getState());
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[event.getState().ordinal()];
        if (i == 1) {
            updateCallProfile(event.getParams());
            this.mCallProfile.setCallType(event.getCallType());
            this.mCallProfile.setDialingNumber(ImsCallUtil.getRemoteCallerId(event.getPeerAddr(), this.mMno, Debug.isProductShip()));
            this.mCallProfile.setRemoteVideoCapa(event.getRemoteVideoCapa());
            this.smCallStateMachine.sendMessage(41);
            if (this.mMno == Mno.SKT) {
                Log.i("ImsConfSession", "event callType : " + event.getCallType());
                this.smCallStateMachine.sendMessage(91, event.getCallType());
            }
        } else if (i == 2) {
            SipError error = event.getErrorCode();
            Log.e("ImsConfSession", "sendMessage CallStateMachine.ON_ENDED");
            if (error == null) {
                this.smCallStateMachine.sendMessage(3);
            } else {
                this.smCallStateMachine.sendMessage(3, error.getCode(), -1, error.getReason());
            }
        } else if (i == 3) {
            this.mCallProfile.setCallType(event.getCallType());
            this.smCallStateMachine.sendMessage(101, (Object) event.getUpdatedParticipantsList());
        } else if (i == 4) {
            this.mCallProfile.setCallType(event.getCallType());
            this.smCallStateMachine.sendMessage(102, (Object) event.getUpdatedParticipantsList());
        } else if (i != 5) {
            super.onImsCallEvent(event);
        } else {
            this.mCallProfile.setCallType(event.getCallType());
            if (this.mMno == Mno.SKT || this.mMno == Mno.LGU) {
                this.smCallStateMachine.sendMessageDelayed(103, (Object) event.getUpdatedParticipantsList(), 100);
            } else {
                this.smCallStateMachine.sendMessage(103, (Object) event.getUpdatedParticipantsList());
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.volte2.ImsConfSession$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd;

        static {
            int[] iArr = new int[CallStateEvent.CALL_STATE.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE = iArr;
            try {
                iArr[CallStateEvent.CALL_STATE.ESTABLISHED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.ENDED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_ADDED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_REMOVED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$volte2$CallStateEvent$CALL_STATE[CallStateEvent.CALL_STATE.CONFERENCE_PARTICIPANTS_UPDATED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            int[] iArr2 = new int[ConfUpdateCmd.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd = iArr2;
            try {
                iArr2[ConfUpdateCmd.ADD_PARTICIPANT.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$volte2$ImsConfSession$ConfUpdateCmd[ConfUpdateCmd.REMOVE_PARTICIPANT.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public void handleRegistrationDone(ImsRegistration regInfo) {
        Log.i("ImsConfSession", "handleRegistrationDone");
        this.mRegistration = regInfo;
        this.smCallStateMachine.onRegistrationDone(regInfo);
        this.smCallStateMachine.sendMessage(11);
    }

    public void handleRegistrationFailed() {
        Log.i("ImsConfSession", "handleRegistrationFailed");
        this.mRegistration = null;
        this.smCallStateMachine.sendMessage(211);
    }

    /* access modifiers changed from: private */
    public void notifyOnConferenceEstablished() {
        int length = this.mListeners.beginBroadcast();
        for (int i = 0; i < length; i++) {
            try {
                this.mListeners.getBroadcastItem(i).onConferenceEstablished();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mListeners.finishBroadcast();
    }

    /* access modifiers changed from: private */
    public int getParticipantId(int callId) {
        for (int i = 0; i < this.mParticipants.size(); i++) {
            if (this.mParticipants.valueAt(i).getCallId() == callId) {
                return this.mParticipants.keyAt(i);
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public ImsCallSession getSessionFromInvitingParticipants(int sessionId) {
        for (ImsCallSession s : this.mInvitingParticipants) {
            if (s.getSessionId() == sessionId) {
                return s;
            }
        }
        return null;
    }
}
