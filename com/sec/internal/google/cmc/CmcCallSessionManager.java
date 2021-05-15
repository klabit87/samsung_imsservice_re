package com.sec.internal.google.cmc;

import android.os.RemoteException;
import android.util.Log;
import com.samsung.android.cmcnsd.CmcNsdManager;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IImsCallSessionEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CmcCallSessionManager {
    private final String LOG_TAG = "CmcCallSessionManager";
    /* access modifiers changed from: private */
    public IImsCallSessionEventListener mImpleEventListener;
    /* access modifiers changed from: private */
    public final boolean mIsEnabledWifiDirectFeature;
    /* access modifiers changed from: private */
    public boolean mIsReplacedSession = false;
    /* access modifiers changed from: private */
    public IImsCallSession mMainSession;
    /* access modifiers changed from: private */
    public final CmcNsdManager mManager;
    /* access modifiers changed from: private */
    public int mOrginalSessionId = 0;
    private CallProfile mReservedCallProfile = null;
    /* access modifiers changed from: private */
    public Map<Integer, IImsCallSession> mSubSessionList = new ConcurrentHashMap();
    private String mTargetNumber = "";
    /* access modifiers changed from: private */
    public IVolteServiceModule mVolteServiceModule;

    public CmcCallSessionManager(IImsCallSession mainSession, IVolteServiceModule volteServiceModule, CmcNsdManager manager, boolean isEnabledWifiDirectFeature) {
        Log.i("CmcCallSessionManager", "add mainSession");
        this.mMainSession = mainSession;
        this.mVolteServiceModule = volteServiceModule;
        this.mManager = manager;
        this.mIsEnabledWifiDirectFeature = isEnabledWifiDirectFeature;
    }

    public int getP2pSessionSize() {
        return this.mSubSessionList.size();
    }

    public boolean isReplacedSession() {
        Log.i("CmcCallSessionManager", "mIsReplacedSession: " + this.mIsReplacedSession);
        return this.mIsReplacedSession;
    }

    public void addP2pSession(IImsCallSession session) {
        if (session == null) {
            try {
                Log.i("CmcCallSessionManager", "session is null. do not add");
            } catch (RemoteException ex) {
                Log.e("CmcCallSessionManager", "getCallId failed due to " + ex.getMessage());
            }
        } else {
            int callId = session.getCallId();
            if (this.mSubSessionList.containsKey(Integer.valueOf(callId))) {
                Log.i("CmcCallSessionManager", "already contains session with this callId! Return");
                return;
            }
            Log.i("CmcCallSessionManager", "add subSession with id " + callId);
            this.mSubSessionList.put(Integer.valueOf(session.getCallId()), session);
            if (this.mImpleEventListener != null) {
                session.registerSessionEventListener(new VolteEventListener(session));
            }
            Log.i("CmcCallSessionManager", "mSubSessionList size: " + getP2pSessionSize());
        }
    }

    public void startP2pSessions(boolean startMainSession) throws RemoteException {
        if (startMainSession) {
            getMainSession().start(this.mTargetNumber, (CallProfile) null);
        }
        for (Map.Entry<Integer, IImsCallSession> e : this.mSubSessionList.entrySet()) {
            e.getValue().start(this.mTargetNumber, (CallProfile) null);
        }
    }

    public void registerSessionEventListener(IImsCallSessionEventListener eventListener) throws RemoteException {
        this.mImpleEventListener = eventListener;
        this.mMainSession.registerSessionEventListener(new VolteEventListener(this.mMainSession));
        for (Map.Entry<Integer, IImsCallSession> e : this.mSubSessionList.entrySet()) {
            IImsCallSession session = e.getValue();
            session.registerSessionEventListener(new VolteEventListener(session));
        }
    }

    public int getPhoneId() throws RemoteException {
        IImsCallSession iImsCallSession = this.mMainSession;
        if (iImsCallSession == null) {
            return 0;
        }
        return iImsCallSession.getPhoneId();
    }

    public int getCallId() throws RemoteException {
        IImsCallSession iImsCallSession = this.mMainSession;
        if (iImsCallSession == null) {
            return 0;
        }
        return iImsCallSession.getCallId();
    }

    public IImsCallSession getMainSession() {
        return this.mMainSession;
    }

    public void setMainSession(IImsCallSession session) {
        this.mMainSession = session;
    }

    public void setReservedCallProfile(CallProfile profile) {
        this.mReservedCallProfile = profile;
    }

    public int getSessionId() throws RemoteException {
        Log.i("CmcCallSessionManager", "current sessionId: " + this.mMainSession.getSessionId() + ", started(orginal) sessionId:" + this.mOrginalSessionId);
        if (this.mOrginalSessionId == 0) {
            return this.mMainSession.getSessionId();
        }
        Log.i("CmcCallSessionManager", "return mOrgMainSessionId: " + this.mOrginalSessionId);
        return this.mOrginalSessionId;
    }

    public CallProfile getCallProfile() throws RemoteException {
        IImsCallSession iImsCallSession = this.mMainSession;
        if (iImsCallSession != null) {
            return iImsCallSession.getCallProfile();
        }
        if (this.mReservedCallProfile != null) {
            Log.i("CmcCallSessionManager", "return reserved callProfile");
            return this.mReservedCallProfile;
        }
        Log.i("CmcCallSessionManager", "return dummy callProfile");
        return new CallProfile();
    }

    public int start(String target, CallProfile profile) throws RemoteException {
        if (this.mMainSession == null) {
            Log.i("CmcCallSessionManager", "need to create p2p sessions: " + target);
            this.mTargetNumber = target;
            return 0;
        }
        for (Map.Entry<Integer, IImsCallSession> e : this.mSubSessionList.entrySet()) {
            Log.i("CmcCallSessionManager", "start(), subSession cmcType: " + e.getValue().getCmcType());
            e.getValue().start(target, profile);
        }
        Log.i("CmcCallSessionManager", "start(), mainSession cmcType: " + this.mMainSession.getCmcType());
        return this.mMainSession.start(target, profile);
    }

    public boolean terminate(int reason) throws RemoteException {
        IImsCallSession iImsCallSession = this.mMainSession;
        if (iImsCallSession == null) {
            Log.i("CmcCallSessionManager", "not yet start call session. update call state as terminated.");
            return false;
        }
        try {
            iImsCallSession.terminate(reason);
        } catch (RemoteException e) {
            Log.e("CmcCallSessionManager", "exception session is maybe a cmcSession, need to terminate subSession");
        }
        for (Map.Entry<Integer, IImsCallSession> e2 : this.mSubSessionList.entrySet()) {
            e2.getValue().terminate(reason);
        }
        return true;
    }

    private class VolteEventListener extends IImsCallSessionEventListener.Stub {
        private IImsCallSession mSession;

        VolteEventListener(IImsCallSession session) {
            this.mSession = session;
        }

        public void onCalling() throws RemoteException {
            Log.i("CmcCallSessionManager", "onCalling()");
            IImsCallSession iImsCallSession = this.mSession;
            if (iImsCallSession != null && iImsCallSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onCalling();
            }
        }

        public void onTrying() throws RemoteException {
            Log.i("CmcCallSessionManager", "onTrying()");
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onTrying();
            }
        }

        public void onRingingBack() throws RemoteException {
            Log.i("CmcCallSessionManager", "onTrying()");
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onRingingBack();
            }
        }

        public void onSessionProgress(int audioEarlyMediaDir) throws RemoteException {
            Log.i("CmcCallSessionManager", "onSessionProgress()");
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onSessionProgress(audioEarlyMediaDir);
            }
        }

        public void onEarlyMediaStarted(int event) throws RemoteException {
            Log.i("CmcCallSessionManager", "onEarlyMediaStarted()");
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onEarlyMediaStarted(event);
            }
        }

        public void onEstablished(int callType) throws RemoteException {
            Log.i("CmcCallSessionManager", "onEstablished(), established on session: " + this.mSession.getSessionId() + ", mainSession: " + this.mSession.getSessionId());
            if (CmcCallSessionManager.this.mMainSession.getCallId() != this.mSession.getCallId()) {
                CmcCallSessionManager cmcCallSessionManager = CmcCallSessionManager.this;
                int unused = cmcCallSessionManager.mOrginalSessionId = cmcCallSessionManager.mMainSession.getSessionId();
                try {
                    Log.i("CmcCallSessionManager", "prev main session terminate, orgSession: " + CmcCallSessionManager.this.mOrginalSessionId);
                    CmcCallSessionManager.this.mMainSession.terminate(5);
                } catch (RemoteException e1) {
                    Log.e("CmcCallSessionManager", "main terminate failed.." + e1);
                }
                Log.i("CmcCallSessionManager", "switch main session to p2p session.");
                IImsCallSession unused2 = CmcCallSessionManager.this.mMainSession = this.mSession;
                CmcCallSessionManager.this.mSubSessionList.remove(Integer.valueOf(this.mSession.getCallId()));
            }
            CmcCallSessionManager.this.mImpleEventListener.onEstablished(callType);
            Log.i("CmcCallSessionManager", "mSubSessionList size: " + CmcCallSessionManager.this.getP2pSessionSize());
            for (Map.Entry<Integer, IImsCallSession> e : CmcCallSessionManager.this.mSubSessionList.entrySet()) {
                IImsCallSession p2pSession = e.getValue();
                if (p2pSession.getCallId() != this.mSession.getCallId()) {
                    try {
                        p2pSession.terminate(5);
                    } catch (RemoteException e2) {
                        Log.e("CmcCallSessionManager", "p2p terminate failed.." + e2);
                    }
                }
            }
        }

        public void onFailure(int reason) throws RemoteException {
            Log.i("CmcCallSessionManager", "onFailure() : reason = " + reason);
            IImsCallSession iImsCallSession = this.mSession;
            if (iImsCallSession == null) {
                Log.e("CmcCallSessionManager", "already ended!!");
            } else if (iImsCallSession.getCallId() != CmcCallSessionManager.this.mMainSession.getCallId()) {
                Log.i("CmcCallSessionManager", "remove session from mSubSessionList");
                CmcCallSessionManager.this.mSubSessionList.remove(Integer.valueOf(this.mSession.getCallId()));
                Log.i("CmcCallSessionManager", "mSubSessionList size: " + CmcCallSessionManager.this.getP2pSessionSize());
            } else {
                CmcCallSessionManager.this.mImpleEventListener.onFailure(reason);
                this.mSession = null;
            }
        }

        public void onSwitched(int callType) throws RemoteException {
            Log.i("CmcCallSessionManager", "onSwitched()");
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onSwitched(callType);
            }
        }

        public void onHeld(boolean initiator, boolean localHoldTone) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onHeld(initiator, localHoldTone);
            }
        }

        public void onResumed(boolean initiator) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onResumed(initiator);
            }
        }

        public void onForwarded() throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onForwarded();
            }
        }

        public void onEnded(int error) throws RemoteException {
            Log.i("CmcCallSessionManager", "onEnded(), error: " + error);
            if (this.mSession == null) {
                Log.e("CmcCallSessionManager", "already ended!!");
                return;
            }
            boolean unused = CmcCallSessionManager.this.mIsReplacedSession = false;
            Log.i("CmcCallSessionManager", "MainSession: " + CmcCallSessionManager.this.mMainSession.getCallId() + ", cmcType: " + CmcCallSessionManager.this.mMainSession.getCmcType());
            Log.i("CmcCallSessionManager", "mSession: " + this.mSession.getCallId() + ", cmcType: " + this.mSession.getCmcType());
            if (this.mSession.getCallId() != CmcCallSessionManager.this.mMainSession.getCallId()) {
                Log.i("CmcCallSessionManager", "mMainSession callState: " + CallConstants.STATE.values()[CmcCallSessionManager.this.mMainSession.getCallStateOrdinal()]);
                CmcCallSessionManager.this.mSubSessionList.remove(Integer.valueOf(this.mSession.getCallId()));
                Log.i("CmcCallSessionManager", "mSubSessionList size: " + CmcCallSessionManager.this.getP2pSessionSize());
                if (CallConstants.STATE.values()[CmcCallSessionManager.this.mMainSession.getCallStateOrdinal()] == CallConstants.STATE.InCall || CallConstants.STATE.values()[CmcCallSessionManager.this.mMainSession.getCallStateOrdinal()] == CallConstants.STATE.AlertingCall) {
                    Log.i("CmcCallSessionManager", "SUB(WIFI or WIFI-DIRECT) session ended, ignore onEnded");
                    this.mSession = null;
                    return;
                }
                Log.i("CmcCallSessionManager", "CMC session ended, switch mainSession to p2p session.");
                IImsCallSession unused2 = CmcCallSessionManager.this.mMainSession = this.mSession;
                boolean unused3 = CmcCallSessionManager.this.mIsReplacedSession = true;
            } else if (CmcCallSessionManager.this.mSubSessionList.size() > 0) {
                Log.i("CmcCallSessionManager", "Ignore onEnded as there are other call sessions waiting");
                return;
            } else {
                int unused4 = CmcCallSessionManager.this.mOrginalSessionId = 0;
                CmcCallSessionManager.this.mSubSessionList.remove(Integer.valueOf(this.mSession.getCallId()));
                Log.i("CmcCallSessionManager", "clear mOrginalSessionId, mSubSessionList size: " + CmcCallSessionManager.this.getP2pSessionSize());
                if (CmcCallSessionManager.this.mIsEnabledWifiDirectFeature) {
                    Log.i("CmcCallSessionManager", "mSession mdecType: " + this.mSession.getCmcType());
                    if (this.mSession.getCmcType() == 1 || this.mSession.getCmcType() == 3 || this.mSession.getCmcType() == 7 || this.mSession.getCmcType() == 5) {
                        if (error == 603) {
                            Log.i("CmcCallSessionManager", "603 case, releaseP2pNetwork!");
                            CmcCallSessionManager.this.mManager.releaseNetwork();
                        } else if (!(CmcCallSessionManager.this.mVolteServiceModule == null || CmcCallSessionManager.this.mVolteServiceModule.hasActiveCall(this.mSession.getPhoneId()) || CmcCallSessionManager.this.mManager == null)) {
                            Log.i("CmcCallSessionManager", "There are no calls, releaseP2pNetwork!");
                            CmcCallSessionManager.this.mManager.releaseNetwork();
                        }
                    }
                }
            }
            CmcCallSessionManager.this.mImpleEventListener.onEnded(error);
            this.mSession = null;
        }

        public void onSessionUpdateRequested(int type, byte[] data) {
        }

        public void onStopAlertTone() {
        }

        public void onError(int error, String errorString, int retryAfter) throws RemoteException {
            IImsCallSession iImsCallSession = this.mSession;
            if (iImsCallSession == null) {
                Log.e("CmcCallSessionManager", "already ended!!");
            } else if (iImsCallSession.getCallId() != CmcCallSessionManager.this.mMainSession.getCallId()) {
                Log.i("CmcCallSessionManager", "Remove session from sessionlist");
                CmcCallSessionManager.this.mSubSessionList.remove(Integer.valueOf(this.mSession.getCallId()));
            } else {
                CmcCallSessionManager.this.mImpleEventListener.onError(error, errorString, retryAfter);
            }
        }

        public void onProfileUpdated(MediaProfile src, MediaProfile dst) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onProfileUpdated(src, dst);
            }
        }

        public void onConferenceEstablished() throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onConferenceEstablished();
            }
        }

        public void onParticipantUpdated(int sessId, String[] participant, int[] status, int[] sipError) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onParticipantUpdated(sessId, participant, status, sipError);
            }
        }

        public void onParticipantAdded(int addedSessionId) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onParticipantAdded(addedSessionId);
            }
        }

        public void onParticipantRemoved(int removeSessionId) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onParticipantRemoved(removeSessionId);
            }
        }

        public void onConfParticipantHeld(int sessionId, boolean initiator) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onConfParticipantHeld(sessionId, initiator);
            }
        }

        public void onConfParticipantResumed(int sessionId, boolean initiator) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onConfParticipantResumed(sessionId, initiator);
            }
        }

        public void onTtyTextRequest(int event, byte[] data) {
        }

        public void onUssdResponse(int result) throws RemoteException {
        }

        public void onUssdReceived(int status, int dcs, byte[] data) throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onUssdReceived(status, dcs, data);
            }
        }

        public void onEPdgUnavailable(int reason) {
        }

        public void onEpdgStateChanged() throws RemoteException {
            if (this.mSession.getCallId() == CmcCallSessionManager.this.mMainSession.getCallId()) {
                CmcCallSessionManager.this.mImpleEventListener.onEpdgStateChanged();
            }
        }

        public void onSessionChanged(int callId) throws RemoteException {
            CmcCallSessionManager.this.mImpleEventListener.onSessionChanged(callId);
        }
    }
}
