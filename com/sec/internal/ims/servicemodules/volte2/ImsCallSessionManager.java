package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.Looper;
import android.os.RemoteException;
import android.telephony.CellLocation;
import android.telephony.PreciseDataConnectionState;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipErrorKor;
import com.sec.internal.constants.ims.SipErrorUscc;
import com.sec.internal.constants.ims.SipErrorVzw;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.util.FileTaskUtil;
import com.sec.internal.ims.servicemodules.volte2.data.IncomingCallEvent;
import com.sec.internal.ims.servicemodules.volte2.data.SIPDataEvent;
import com.sec.internal.ims.settings.GlobalSettingsManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;

public class ImsCallSessionManager {
    private static final int INVALID_PHONE_ID = -1;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsCallSessionManager.class.getSimpleName();
    private ImsCallSession mIncomingCallSession = null;
    private final NetworkStateListener mNetworkStateListener = new NetworkStateListener() {
        public void onDataConnectionStateChanged(int networkType, boolean isWifiConnected, int phoneId) {
            String access$000 = ImsCallSessionManager.LOG_TAG;
            Log.i(access$000, "onDataConnectionStateChanged(): networkType [" + TelephonyManagerExt.getNetworkTypeName(networkType) + "]isWifiConnected [" + isWifiConnected + "], phoneId [" + phoneId + "]");
            if (SimUtil.getSimMno(phoneId) == Mno.CELLC_SOUTHAFRICA && !isWifiConnected) {
                for (ImsCallSession session : ImsCallSessionManager.this.mSessionMap.values()) {
                    if (session.getPhoneId() == phoneId) {
                        try {
                            session.terminate(21);
                        } catch (RemoteException e) {
                            Log.e(ImsCallSessionManager.LOG_TAG, "WIFI DISCONNECTED: ", e);
                        }
                    }
                }
            }
        }

        public void onCellLocationChanged(CellLocation location, int phoneId) {
            String access$000 = ImsCallSessionManager.LOG_TAG;
            Log.i(access$000, "onCellLocationChanged, phoneId: " + phoneId);
        }

        public void onEpdgConnected(int phoneId) {
            String access$000 = ImsCallSessionManager.LOG_TAG;
            Log.i(access$000, "onEpdgConnected: [" + phoneId + "]");
            handleEpdgState(phoneId, true);
        }

        public void onEpdgDisconnected(int phoneId) {
            String access$000 = ImsCallSessionManager.LOG_TAG;
            Log.i(access$000, "onEpdgDisconnected: [" + phoneId + "]");
            handleEpdgState(phoneId, false);
        }

        private void handleEpdgState(int phoneId, boolean connected) {
            int logClass = connected ? LogClass.VOLTE_EPDG_CONNECTED : LogClass.VOLTE_EPDG_DISCONNECTED;
            IMSLog.c(logClass, "" + phoneId);
            for (ImsCallSession session : ImsCallSessionManager.this.mSessionMap.values()) {
                if (session.getPhoneId() == phoneId && session.getCallProfile().getNetworkType() != 15) {
                    session.setEpdgState(connected);
                }
            }
        }

        public void onEpdgDeregisterRequested(int phoneId) {
        }

        public void onEpdgRegisterRequested(int phoneId, boolean cdmaAvailability) {
        }

        public void onEpdgIpsecDisconnected(int phoneId) {
        }

        public void onDefaultNetworkStateChanged(int phoneId) {
        }

        public void onIKEAuthFAilure(int phoneId) {
        }

        public void onPreciseDataConnectionStateChanged(int phoneId, PreciseDataConnectionState state) {
        }
    };
    /* access modifiers changed from: private */
    public IPdnController mPdnController;
    /* access modifiers changed from: private */
    public ImsCallSession mPendingOutgoingCall = null;
    private final IRegistrationManager mRegMan;
    /* access modifiers changed from: private */
    public ImsCallSessionFactory mSessionFactory;
    /* access modifiers changed from: private */
    public final Map<Integer, ImsCallSession> mSessionMap;
    /* access modifiers changed from: private */
    public ITelephonyManager mTelephonyManager;
    private final Map<Integer, ImsCallSession> mUnmodifiableSessionMap;
    /* access modifiers changed from: private */
    public IVolteServiceModuleInternal mVolteServiceModule;

    public ImsCallSessionManager(IVolteServiceModuleInternal module, ITelephonyManager telephonyManager, IPdnController pdnController, IRegistrationManager regMan, Looper looper) {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        this.mSessionMap = concurrentHashMap;
        this.mUnmodifiableSessionMap = Collections.unmodifiableMap(concurrentHashMap);
        this.mVolteServiceModule = module;
        this.mSessionFactory = new ImsCallSessionFactory(module, looper);
        this.mTelephonyManager = telephonyManager;
        this.mPdnController = pdnController;
        this.mRegMan = regMan;
        pdnController.registerForNetworkState(this.mNetworkStateListener);
        this.mSessionMap.clear();
    }

    private class ImsCallSessionBuilder {
        private boolean mIsEmergency;
        private Mno mMno;
        private boolean mNeedToSetCallToPending;
        private int mPhoneId;
        private CallProfile mProfile;
        private ImsRegistration mRegInfo;
        private int mSubId;

        private ImsCallSessionBuilder() {
        }

        public ImsCallSession createSession(CallProfile profile, ImsRegistration regInfo) throws RemoteException {
            if (profile != null) {
                this.mProfile = profile;
                this.mRegInfo = regInfo;
                parseArguments();
                processNeedToSetCallToPending();
                processNetworkType();
                checkExistingCallSessions();
                processImpuAndCmc();
                ImsCallSession session = ImsCallSessionManager.this.mSessionFactory.create(this.mProfile, this.mRegInfo, this.mNeedToSetCallToPending);
                if (session != null) {
                    if (this.mRegInfo == null && this.mNeedToSetCallToPending && this.mProfile.getCmcType() == 0 && !this.mProfile.isForceCSFB()) {
                        setPendingOutgoingCall(session);
                    }
                    ImsCallSessionManager.this.addCallSession(session);
                    return session;
                }
                Log.e(ImsCallSessionManager.LOG_TAG, "createSession: session create fail");
                throw new RemoteException();
            }
            Log.e(ImsCallSessionManager.LOG_TAG, "profile is null");
            throw new RemoteException("Null CallProfile.");
        }

        private void checkCanMakeCallSession() throws RemoteException {
            if (this.mRegInfo == null) {
                Log.e(ImsCallSessionManager.LOG_TAG, "cannot make new call session. not registered");
                throw new RemoteException("Not registered.");
            } else if (this.mMno == Mno.VZW && !ImsCallSessionManager.this.mVolteServiceModule.isVowifiEnabled(this.mPhoneId) && ImsCallSessionManager.this.mPdnController.isEpdgConnected(this.mPhoneId) && this.mProfile.getCallType() == 1) {
                Log.e(ImsCallSessionManager.LOG_TAG, "cannot make new call session. currently in Registering");
                throw new RemoteException("Registering.");
            }
        }

        private void checkExistingCallSessions() throws RemoteException {
            if (!this.mProfile.isConferenceCall()) {
                for (ImsCallSession tempCallSession : ImsCallSessionManager.this.mSessionMap.values()) {
                    checkOngoingCallForForkedSession(tempCallSession);
                    if (this.mRegInfo != null && tempCallSession != null && tempCallSession.getRegistration() != null && this.mRegInfo.getHandle() != tempCallSession.getRegistration().getHandle()) {
                        Log.i(ImsCallSessionManager.LOG_TAG, "skip different based regi");
                    } else if (!(tempCallSession == null || tempCallSession.getCallState() == CallConstants.STATE.HeldCall || !ImsCallUtil.isOngoingCallState(tempCallSession.getCallState()))) {
                        handleCallSessionDuringCall(tempCallSession);
                    }
                }
            }
        }

        private void checkOngoingCallForForkedSession(ImsCallSession tempCallSession) throws RemoteException {
            ImsRegistration imsRegistration = this.mRegInfo;
            if (imsRegistration != null && ImsCallSessionManager.this.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType()) && tempCallSession != null && tempCallSession.getRegistration() != null && this.mRegInfo.getPhoneId() != tempCallSession.getRegistration().getPhoneId() && tempCallSession.getCallState() != CallConstants.STATE.Idle) {
                String access$000 = ImsCallSessionManager.LOG_TAG;
                Log.e(access$000, "cannot make a forking session. ongoing call exists on the other sim. callId: " + tempCallSession.getCallId() + ", sessionId: " + tempCallSession.getSessionId() + ", callState: " + tempCallSession.getCallState());
                throw new RemoteException();
            }
        }

        private int getNetworkForCreateSession() {
            if (this.mIsEmergency) {
                int emergencyPdnPolicy = ImsRegistry.getInt(this.mPhoneId, GlobalSettingsConstants.Call.E911_PDN_SELECTION_VOWIFI, 0);
                String access$000 = ImsCallSessionManager.LOG_TAG;
                Log.i(access$000, "createSession: voiceNetwork = " + ImsCallSessionManager.this.mTelephonyManager.getVoiceNetworkType(this.mSubId));
                if (emergencyPdnPolicy == 1 && ImsCallSessionManager.this.mPdnController.isEpdgConnected(this.mPhoneId) && "VoWIFI".equalsIgnoreCase(this.mProfile.getEmergencyRat())) {
                    Log.i(ImsCallSessionManager.LOG_TAG, "createSession: use IMS PDN for WiFi e911 for e911pdnpolicy(IMSPDN_IF_IPC_RAT_EPDG).");
                    return 11;
                } else if (!this.mMno.isKor() || ImsCallSessionManager.this.mTelephonyManager.getVoiceNetworkType(this.mSubId) == 13 || ImsCallSessionManager.this.mTelephonyManager.getVoiceNetworkType(this.mSubId) == 20 || ImsCallSessionManager.this.mTelephonyManager.getVoiceNetworkType(this.mSubId) == 0 || this.mProfile.getCallType() != 8) {
                    return 15;
                } else {
                    Log.i(ImsCallSessionManager.LOG_TAG, "createSession: use IMS PDN for KOR 3g psvt e911.");
                    return 11;
                }
            } else {
                ImsRegistration imsRegistration = this.mRegInfo;
                if (imsRegistration != null) {
                    return imsRegistration.getNetworkType();
                }
                if (this.mNeedToSetCallToPending) {
                    return 11;
                }
                return -1;
            }
        }

        private void handleCallSessionDuringCall(ImsCallSession tempCallSession) throws RemoteException {
            if (this.mIsEmergency && this.mMno == Mno.VZW) {
                try {
                    Log.i(ImsCallSessionManager.LOG_TAG, "release active call before E911 dialing");
                    if (tempCallSession.getCallState() == CallConstants.STATE.IncomingCall) {
                        tempCallSession.reject(2);
                    } else {
                        tempCallSession.terminate(5, true);
                    }
                } catch (RemoteException e) {
                    Log.e(ImsCallSessionManager.LOG_TAG, "createSession: ", e);
                }
            } else if (!ImsCallSessionManager.this.isAllowUssdDuringCall(this.mMno) || this.mProfile.getCallType() != 12) {
                ImsRegistration imsRegistration = this.mRegInfo;
                if (imsRegistration == null || !ImsCallSessionManager.this.isCmcPrimaryType(imsRegistration.getImsProfile().getCmcType())) {
                    String access$000 = ImsCallSessionManager.LOG_TAG;
                    Log.e(access$000, "cannot make new call session. another call already exist callId: " + tempCallSession.getCallId() + ", sessionId: " + tempCallSession.getSessionId() + ", callState: " + tempCallSession.getCallState());
                    throw new RemoteException();
                }
                Log.e(ImsCallSessionManager.LOG_TAG, "allow CMC 2ndCall in PD");
            } else {
                Log.e(ImsCallSessionManager.LOG_TAG, "Operator allow USSD during call");
            }
        }

        private void parseArguments() {
            int phoneId = this.mProfile.getPhoneId();
            this.mPhoneId = phoneId;
            ImsRegistration imsRegistration = this.mRegInfo;
            if (imsRegistration == null) {
                this.mMno = SimUtil.getSimMno(phoneId);
            } else {
                this.mMno = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            }
            this.mSubId = SimUtil.getSubId(this.mPhoneId);
            this.mIsEmergency = ImsCallUtil.isE911Call(this.mProfile.getCallType());
            this.mNeedToSetCallToPending = this.mMno.isKor();
        }

        private void processImpuAndCmc() {
            ImsUri activeImpu = ImsCallSessionManager.this.mVolteServiceModule.getActiveImpu();
            if (TextUtils.isEmpty(this.mProfile.getLineMsisdn()) && activeImpu != null) {
                String access$000 = ImsCallSessionManager.LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("createSession: originating from ");
                sb.append(IMSLog.checker(activeImpu + ""));
                Log.i(access$000, sb.toString());
                this.mProfile.setLineMsisdn(UriUtil.getMsisdnNumber(activeImpu));
                this.mProfile.setOriginatingUri(activeImpu);
            }
            ImsRegistration imsRegistration = this.mRegInfo;
            if (imsRegistration != null && imsRegistration.getImsProfile().getCmcType() > 0) {
                ImsCallSessionManager.this.mVolteServiceModule.checkCmcP2pList(this.mRegInfo, this.mProfile);
            }
        }

        private void processNeedToSetCallToPending() {
            if (this.mMno != Mno.VZW || !ImsUtil.isCdmalessEnabled(this.mPhoneId) || this.mRegInfo != null || this.mIsEmergency) {
                if (!this.mProfile.isForceCSFB()) {
                    if (this.mProfile.getCmcType() == 2) {
                        ImsRegistration imsRegistration = this.mRegInfo;
                        if (imsRegistration != null && imsRegistration.getImsProfile().getCmcType() == 2) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
                Log.i(ImsCallSessionManager.LOG_TAG, "set needToPendingCall to true when SD or VoLTE is not registered");
                this.mNeedToSetCallToPending = true;
                this.mRegInfo = null;
                return;
            }
            Log.e(ImsCallSessionManager.LOG_TAG, "createSession: Cdmaless needToPendingCall");
            this.mNeedToSetCallToPending = true;
        }

        private void processNetworkType() throws RemoteException {
            this.mProfile.setNetworkType(getNetworkForCreateSession());
            if (this.mRegInfo == null && this.mIsEmergency && this.mProfile.getNetworkType() == 11) {
                Log.i(ImsCallSessionManager.LOG_TAG, "Need to pending E911 call over VoWifi using IMS PDN.");
                this.mNeedToSetCallToPending = true;
            }
            if (!this.mNeedToSetCallToPending) {
                if (!this.mIsEmergency) {
                    checkCanMakeCallSession();
                }
                if (this.mProfile.getNetworkType() != 15 && this.mRegInfo == null) {
                    Log.e(ImsCallSessionManager.LOG_TAG, "cannot make new call session. not registered");
                    throw new RemoteException("Not registered.");
                }
            }
        }

        private void setPendingOutgoingCall(ImsCallSession session) {
            Log.i(ImsCallSessionManager.LOG_TAG, "try to regi for pending outgoing call session");
            session.setPendingCall(true);
            ImsCallSession unused = ImsCallSessionManager.this.mPendingOutgoingCall = session;
        }
    }

    public ImsCallSession createSession(CallProfile profile, ImsRegistration regInfo) throws RemoteException {
        return new ImsCallSessionBuilder().createSession(profile, regInfo);
    }

    /* access modifiers changed from: private */
    public void addCallSession(ImsCallSession callSession) {
        int sessionID = callSession.getSessionId();
        for (ImsCallSession tempCallSession : this.mSessionMap.values()) {
            if ((sessionID != -1 && tempCallSession.getSessionId() == sessionID) || tempCallSession.getCallState() == CallConstants.STATE.EndedCall) {
                if (tempCallSession.equals(callSession)) {
                    String str = LOG_TAG;
                    Log.e(str, "same CallSession has been found : Session id is:" + tempCallSession.getSessionId() + " And corresponding CallId is:" + tempCallSession.getCallId());
                    return;
                }
                this.mSessionMap.remove(Integer.valueOf(tempCallSession.getCallId()));
            }
        }
        this.mSessionMap.put(Integer.valueOf(callSession.getCallId()), callSession);
    }

    public int getSessionCount() {
        return this.mSessionMap.size();
    }

    public int getSessionCount(int phoneId) {
        int count = 0;
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getPhoneId() == phoneId) {
                count++;
            }
        }
        return count;
    }

    public List<ImsCallSession> getSessionList() {
        List<ImsCallSession> list = new ArrayList<>();
        list.addAll(this.mSessionMap.values());
        return list;
    }

    public List<ImsCallSession> getSessionList(int phoneId) {
        List<ImsCallSession> list = new ArrayList<>();
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getPhoneId() == phoneId) {
                list.add(s);
            }
        }
        return list;
    }

    public ImsCallSession getForegroundSession() {
        return getForegroundSession(-1);
    }

    public ImsCallSession getForegroundSession(int phoneId) {
        for (ImsCallSession s : this.mSessionMap.values()) {
            if ((phoneId == -1 || s.getPhoneId() == phoneId) && s.getCallState() == CallConstants.STATE.InCall) {
                return s;
            }
        }
        return null;
    }

    public boolean hasActiveCall(int phoneId) {
        CallConstants.STATE state;
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getPhoneId() == phoneId && (state = s.getCallState()) != CallConstants.STATE.Idle && state != CallConstants.STATE.EndingCall && state != CallConstants.STATE.EndedCall) {
                return true;
            }
        }
        return false;
    }

    public ImsCallSession getSession(int sessionId) {
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getSessionId() == sessionId) {
                return s;
            }
        }
        return null;
    }

    public ImsCallSession getSessionByCallId(int callId) {
        return this.mSessionMap.get(Integer.valueOf(callId));
    }

    public ImsCallSession getSessionBySipCallId(String sipCallId) {
        if (sipCallId == null) {
            return null;
        }
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (sipCallId.equals(s.getCallProfile().getSipCallId())) {
                return s;
            }
        }
        return null;
    }

    public List<ImsCallSession> getSessionByState(CallConstants.STATE state) {
        return getSessionByState(-1, state);
    }

    public List<ImsCallSession> getSessionByState(int phoneId, CallConstants.STATE state) {
        List<ImsCallSession> result = new ArrayList<>();
        for (ImsCallSession session : this.mSessionMap.values()) {
            CallProfile profile = session.getCallProfile();
            if ((profile == null || !profile.isConferenceCall()) && (phoneId == -1 || session.getPhoneId() == phoneId)) {
                String str = LOG_TAG;
                Log.i(str, "getSessionByState(" + session.getCallId() + ") : " + session.getCallState().toString());
                if (session.getCallState() == state) {
                    result.add(session);
                }
            }
        }
        return result;
    }

    public List<ImsCallSession> getSessionByCallType(int calltype) {
        return getSessionByCallType(-1, calltype);
    }

    public List<ImsCallSession> getSessionByCallType(int phoneId, int calltype) {
        CallProfile profile;
        List<ImsCallSession> result = new ArrayList<>();
        for (ImsCallSession session : this.mSessionMap.values()) {
            if ((phoneId == -1 || session.getPhoneId() == phoneId) && (((profile = session.getCallProfile()) == null || !profile.isConferenceCall()) && profile != null && profile.getCallType() == calltype)) {
                result.add(session);
            }
        }
        return result;
    }

    public boolean hasSipCallId(String sipCallId) {
        for (Map.Entry<Integer, ImsCallSession> pair : this.mSessionMap.entrySet()) {
            ImsCallSession session = pair.getValue();
            CallProfile cp = session.getCallProfile();
            if (cp != null && cp.getSipCallId() != null && cp.getSipCallId().equals(sipCallId)) {
                String str = LOG_TAG;
                Log.i(str, "exclude the dialog with sipCallId: " + sipCallId + " sessionId: " + session.getSessionId());
                return true;
            }
        }
        return false;
    }

    public ImsCallSession getSessionByRegId(int regId) {
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getRegistration().getHandle() == regId) {
                return s;
            }
        }
        return null;
    }

    public List<ImsCallSession> getEmergencySession() {
        ArrayList<ImsCallSession> sessions = new ArrayList<>();
        for (ImsCallSession session : this.mSessionMap.values()) {
            CallProfile profile = session.getCallProfile();
            if (profile != null && ImsCallUtil.isE911Call(profile.getCallType())) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    public ImsCallSession removeSession(int sessionId) {
        for (Map.Entry<Integer, ImsCallSession> pair : this.mSessionMap.entrySet()) {
            Integer key = pair.getKey();
            if (pair.getValue().getSessionId() == sessionId) {
                return this.mSessionMap.remove(key);
            }
        }
        return null;
    }

    public int[] getCallCount(int phoneId) {
        int[] ret = new int[4];
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (session != null && (phoneId == -1 || session.getPhoneId() == phoneId)) {
                try {
                    CallConstants.STATE state = session.getCallState();
                    if (!(state == CallConstants.STATE.Idle || state == CallConstants.STATE.EndingCall)) {
                        if (state != CallConstants.STATE.EndedCall) {
                            if (session.getCallProfile() != null) {
                                ret[0] = ret[0] + 1;
                                int callType = session.getCallProfile().getCallType();
                                if (ImsCallUtil.isVideoCall(callType)) {
                                    ret[1] = ret[1] + 1;
                                } else if (session.getCallProfile().isDowngradedVideoCall()) {
                                    ret[2] = ret[2] + 1;
                                }
                                if (ImsCallUtil.isE911Call(callType)) {
                                    ret[3] = ret[3] + 1;
                                }
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    String str = LOG_TAG;
                    Log.e(str, "getCallCount: " + e.getMessage());
                }
            }
        }
        String str2 = LOG_TAG;
        Log.i(str2, "Total Call[" + phoneId + "] : " + ret[0] + " Video Call : " + ret[1] + " Downgraded Video Call : " + ret[2] + " E911 Call : " + ret[3]);
        return ret;
    }

    public int getActiveExtCallCount() {
        int activeExtCallCnt = 0;
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getCmcType() == 0 && (s.getCallState() == CallConstants.STATE.InCall || s.mIsEstablished)) {
                activeExtCallCnt++;
            }
        }
        return activeExtCallCnt;
    }

    public boolean getExtMoCall() {
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getCmcType() == 0) {
                if (s.getCallProfile().isMOCall()) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public boolean hasVideoCall() {
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (ImsCallUtil.isVideoCall(session.getCallProfile().getCallType())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRttCall() {
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (ImsCallUtil.isRttCall(session.getCallProfile().getCallType())) {
                return true;
            }
        }
        return false;
    }

    public int convertToSessionId(int callId) {
        ImsCallSession session = this.mSessionMap.get(Integer.valueOf(callId));
        if (session == null) {
            return -1;
        }
        return session.getSessionId();
    }

    public boolean hasRingingCall() {
        return hasRingingCall(-1);
    }

    public boolean hasRingingCall(int phoneId) {
        for (ImsCallSession session : this.mSessionMap.values()) {
            if ((phoneId == -1 || session.getPhoneId() == phoneId) && (session.getCallState() == CallConstants.STATE.IncomingCall || session.getPreAlerting())) {
                String str = LOG_TAG;
                Log.i(str, "session(" + session.getSessionId() + ") is in IncomingCall");
                return true;
            }
        }
        return false;
    }

    public void forceNotifyCurrentCodec() {
        for (ImsCallSession session : this.mSessionMap.values()) {
            session.forceNotifyCurrentCodec();
        }
    }

    public void sendRttMessage(String msg) {
        if (msg != null) {
            for (ImsCallSession session : this.mSessionMap.values()) {
                session.sendText(msg, msg.length());
            }
            return;
        }
        Log.i(LOG_TAG, "sendRttMessage: receive null string / do nothing");
    }

    public boolean triggerPsRedial(int phoneId, int callId, int targetPdn, ImsRegistration regiInfo) {
        ImsCallSession origSession = this.mSessionMap.get(Integer.valueOf(callId));
        if (regiInfo == null || origSession == null) {
            Log.e(LOG_TAG, "TMO_E911 Call session or IMS Registration is not exist!");
            Log.e(LOG_TAG, "TMO_E911 triggerPsRedial = false");
            return false;
        }
        CallProfile origProfile = origSession.getCallProfile();
        if (origProfile == null) {
            Log.e(LOG_TAG, "TMO_E911 triggerPsRedial = false, origProfile is null");
            return false;
        }
        origProfile.setNetworkType(targetPdn);
        ImsCallSession session = this.mSessionFactory.create(origProfile, regiInfo, false);
        if (session == null) {
            return false;
        }
        try {
            session.replaceSessionEventListener(origSession);
            session.start(origProfile.getDialingNumber(), (CallProfile) null);
            this.mSessionMap.replace(Integer.valueOf(callId), origSession, session);
            origSession.notifySessionChanged(callId);
            Log.e(LOG_TAG, "TMO_E911 triggerPsRedial = true");
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "TMO_E911 triggerPsRedial = false");
            return false;
        }
    }

    public void handleSrvccStateChange(int phoneId, int srvccState, Mno mno) {
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (session != null) {
                try {
                    if (session.getPhoneId() == phoneId) {
                        if (srvccState == 0) {
                            session.update((CallProfile) null, 100, "SRVCC HO STARTED");
                        } else if (srvccState == 1) {
                            session.terminate(8);
                        } else if (srvccState == 2 || srvccState == 3) {
                            String reasonText = "failure to transition to CS domain";
                            if (mno.isOrange()) {
                                reasonText = "handover cancelled";
                            }
                            session.update((CallProfile) null, 487, reasonText);
                        }
                    }
                } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                    String str = LOG_TAG;
                    Log.e(str, "handleReinvite: " + e.getMessage());
                }
            }
        }
    }

    public void handleEpdgHandover(int phoneId, ImsRegistration regiInfo, Mno mno) {
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (session.getPhoneId() == regiInfo.getPhoneId() && !session.isE911Call()) {
                boolean isEpdgConnected = this.mPdnController.isEpdgConnected(phoneId);
                if (isEpdgConnected && regiInfo.getEpdgStatus()) {
                    session.setEpdgState(true);
                } else if (!isEpdgConnected) {
                    session.setEpdgState(false);
                }
                if (mno == Mno.ATT || mno == Mno.ROGERS) {
                    try {
                        session.reinvite();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void endCallByDeregistered(ImsRegistration regiInfo) {
        int deRegisteringRegId = regiInfo.getHandle();
        for (ImsCallSession session : this.mSessionMap.values()) {
            try {
                ImsRegistration registration = session.getRegistration();
                if (registration != null && deRegisteringRegId == registration.getHandle()) {
                    if (session.getCallState() == CallConstants.STATE.IncomingCall) {
                        session.reject(2);
                    } else {
                        String str = LOG_TAG;
                        Log.i(str, "end call " + session.getSessionId() + " by MMTEL deregistered");
                        session.terminate(ImsCallUtil.convertDeregiReason(regiInfo.getDeregiReason()), true);
                    }
                }
            } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "endCallByDeregistered: " + e.getMessage());
            }
        }
    }

    public void endcallByNwHandover(ImsRegistration regiInfo) {
        for (ImsCallSession session : this.mSessionMap.values()) {
            ImsRegistration callRegiInfo = session.getRegistration();
            if (callRegiInfo == null || regiInfo.getPhoneId() == callRegiInfo.getPhoneId()) {
                try {
                    if (session.getCallState() == CallConstants.STATE.IncomingCall) {
                        session.reject(4);
                    } else {
                        session.terminate(4);
                    }
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "onNetworkChanged: ", e);
                }
            }
        }
    }

    public void onCallEndByCS(int phoneId) {
        Log.i(LOG_TAG, "onCallEndByCS");
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getCallProfile().getCallType() != 7 && s.getPhoneId() == phoneId) {
                try {
                    s.terminate(4);
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "onNetworkChanged: ", e);
                }
            }
        }
    }

    public void releaseAllSession(int phoneId) {
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (session != null && session.getPhoneId() == phoneId) {
                try {
                    if (session.getCallState() == CallConstants.STATE.IncomingCall) {
                        session.reject(2);
                    } else {
                        session.terminate(5, true);
                    }
                } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                    String str = LOG_TAG;
                    Log.e(str, "release all session in F/W layer: " + e.getMessage());
                }
            }
        }
    }

    public void releaseAllVideoCall() {
        for (ImsCallSession s : getSessionList()) {
            if (s.getCallProfile().getCallType() == 2) {
                try {
                    s.terminate(-1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onReleaseWfcBeforeHO(int phoneId) {
        CallProfile profile;
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (session != null) {
                try {
                    if (session.getPhoneId() == phoneId && (profile = session.getCallProfile()) != null && !ImsCallUtil.isE911Call(profile.getCallType())) {
                        if (session.getCallState() == CallConstants.STATE.IncomingCall) {
                            session.reject(2);
                        } else {
                            session.terminate(5, true);
                        }
                        String str = LOG_TAG;
                        Log.i(str, "end call " + session.getSessionId() + " Before HO");
                    }
                } catch (RemoteException | ArrayIndexOutOfBoundsException e) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "onReleaseWfcBeforeHO: " + e.getMessage());
                }
            }
        }
    }

    public void terminateMoWfcWhenWfcSettingOff(int phoneId) {
        CallProfile profile;
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (session.getPhoneId() == phoneId && (profile = session.getCallProfile()) != null && profile.getCallType() == 1 && !profile.isDowngradedVideoCall() && profile.isMOCall() && !profile.isConferenceCall()) {
                try {
                    session.terminate(5);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hasEmergencyCall(int phoneId) {
        CallProfile profile;
        for (ImsCallSession s : this.mSessionMap.values()) {
            if (s.getPhoneId() == phoneId && (profile = s.getCallProfile()) != null && ImsCallUtil.isE911Call(profile.getCallType())) {
                return true;
            }
        }
        return false;
    }

    public void setTtyMode(int phoneId, int mode) {
        for (ImsCallSession session : this.mSessionMap.values()) {
            if (session.getPhoneId() == phoneId) {
                session.setTtyMode(mode);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isAllowUssdDuringCall(Mno mno) {
        return mno == Mno.ATT || mno == Mno.TMOUS || mno.isEur() || mno.isSea() || (mno.isSwa() && mno != Mno.MOBITEL_LK) || (!(!mno.isMea() || mno == Mno.MTN_IRAN || mno == Mno.OOREDOO_QATAR) || mno.isOce() || mno.isJpn());
    }

    public void onPSBarred(boolean on) {
        String str = LOG_TAG;
        Log.i(str, "onPSBarred: on =" + on);
        if (on) {
            IMSLog.c(LogClass.VOLTE_PS_BARRING);
            for (ImsCallSession session : this.mSessionMap.values()) {
                CallConstants.STATE state = session.getCallState();
                if (state == CallConstants.STATE.IncomingCall || state == CallConstants.STATE.OutGoingCall || state == CallConstants.STATE.AlertingCall) {
                    try {
                        session.terminate(13);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "onNetworkChanged: ", e);
                    }
                }
            }
        }
    }

    public void getSIPMSGInfo(SIPDataEvent noti) {
        for (ImsCallSession session : this.mSessionMap.values()) {
            session.onReceiveSIPMSG(noti.getSipMessage(), noti.getIsRequest());
        }
    }

    public void onUpdateGeolocation() {
        for (ImsCallSession session : this.mSessionMap.values()) {
            session.onUpdateGeolocation();
        }
    }

    public ImsCallSession onImsIncomingCallEvent(IncomingCallEvent event, CallProfile profile, ImsRegistration reg, int callType, int ttyMode) {
        ImsCallSession create = this.mSessionFactory.create(profile, reg, false);
        this.mIncomingCallSession = create;
        if (create == null) {
            Log.i(LOG_TAG, "onImsIncomingCallEvent: IncomingCallSession create failed");
            return null;
        }
        create.setSessionId(event.getSessionID());
        this.mIncomingCallSession.updateCallProfile(event.getParams());
        if (!(reg.getImsProfile().getTtyType() == 1 || reg.getImsProfile().getTtyType() == 3)) {
            this.mIncomingCallSession.setTtyMode(ttyMode);
        }
        if ((reg.getImsProfile().getTtyType() == 3 || reg.getImsProfile().getTtyType() == 4) && ImsCallUtil.isRttCall(callType)) {
            if (!this.mPdnController.isEpdgConnected(reg.getPhoneId())) {
                this.mIncomingCallSession.startRttDedicatedBearerTimer(this.mVolteServiceModule.getRttDbrTimer(reg.getPhoneId()));
            }
            this.mIncomingCallSession.getCallProfile().getMediaProfile().setRttMode(1);
        }
        this.mIncomingCallSession.setPreAlerting();
        addCallSession(this.mIncomingCallSession);
        return this.mIncomingCallSession;
    }

    public ImsCallSession getIncomingCallSession() {
        return this.mIncomingCallSession;
    }

    private int[] checkHasCallAndCallType(int phoneId) {
        int[] ret = new int[6];
        synchronized (this.mSessionMap) {
            for (ImsCallSession session : this.mSessionMap.values()) {
                if (session == null || !isCmcPrimaryType(session.getCmcType())) {
                    if (session != null && session.getPhoneId() == phoneId) {
                        CallConstants.STATE state = session.getCallState();
                        if (state == CallConstants.STATE.InCall) {
                            ret[0] = 1;
                            ret[1] = session.getCallProfile().getCallType();
                        } else if (state == CallConstants.STATE.HeldCall || session.isRemoteHeld()) {
                            ret[2] = 1;
                        }
                        if (session.getCallProfile().isConferenceCall()) {
                            ret[3] = 1;
                        }
                        if (session.getCallProfile().getCallType() == 9) {
                            ret[4] = 1;
                        }
                        if (state == CallConstants.STATE.ModifyingCall || state == CallConstants.STATE.ModifyRequested || state == CallConstants.STATE.HoldingVideo || state == CallConstants.STATE.ResumingVideo) {
                            ret[5] = 1;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private SipError getErrorOnDialingState(int cmcType, int sessionCmcType, Mno mno) {
        SipError error;
        SipError sipError = SipErrorBase.OK;
        if (mno == Mno.VZW) {
            error = SipErrorVzw.BUSY_ESTABLISHING_ANOTHER_CALL;
        } else if (mno.isKor()) {
            error = SipErrorKor.BUSY_HERE;
        } else if (mno == Mno.USCC) {
            error = SipErrorUscc.BUSY_ESTABLISHING_ANOTHER_CALL;
        } else {
            error = SipErrorBase.BUSY_HERE;
        }
        if ((cmcType != 0 || !isCmcSecondaryType(sessionCmcType)) && (!isCmcSecondaryType(cmcType) || sessionCmcType != 0)) {
            return error;
        }
        return SipErrorBase.OK;
    }

    private SipError getErrorDuringCall(int callType, int sessionCallType, Mno mno) {
        SipError error = SipErrorBase.OK;
        if (mno == Mno.VIVA_KUWAIT && sessionCallType == 2 && callType == 2) {
            return SipErrorKor.BUSY_HERE;
        }
        if ((mno == Mno.ZAIN_KUWAIT || mno == Mno.OOREDOO_KUWAIT) && sessionCallType == 2) {
            return SipErrorKor.BUSY_HERE;
        }
        return error;
    }

    private SipError getErrorAsSessionState(int phoneId, int cmcType, boolean hasInCall, boolean hasHoldCall, int callType, Mno mno) {
        SipError error = SipErrorBase.OK;
        synchronized (this.mSessionMap) {
            for (ImsCallSession session : this.mSessionMap.values()) {
                if (session != null) {
                    if (!isCmcPrimaryType(session.getCmcType())) {
                        if (session.getPhoneId() == phoneId) {
                            CallConstants.STATE state = session.getCallState();
                            if (!(state == CallConstants.STATE.Idle || state == CallConstants.STATE.ReadyToCall)) {
                                if (!ImsCallUtil.isDialingCallState(state)) {
                                    if (hasHoldCall || hasInCall) {
                                        error = getErrorDuringCall(callType, session.getCallProfile().getCallType(), mno);
                                    }
                                }
                            }
                            String str = LOG_TAG;
                            Log.i(str, "checkRejectIncomingCall: found dialing session " + session.mSessionId);
                            SipError errorOnDialingState = getErrorOnDialingState(cmcType, session.mCmcType, mno);
                            return errorOnDialingState;
                        }
                    }
                }
            }
            return error;
        }
    }

    public void onRegistered(ImsRegistration regiInfo) {
        ImsCallSession imsCallSession = this.mPendingOutgoingCall;
        if (imsCallSession != null) {
            imsCallSession.handleRegistrationDone(regiInfo);
            this.mPendingOutgoingCall = null;
        }
    }

    public void handleDeregistered(int phoneId, int errorCode, Mno mno) {
        if (this.mPendingOutgoingCall == null) {
            return;
        }
        if (mno.isKor() || (mno == Mno.VZW && ImsUtil.isCdmalessEnabled(phoneId) && errorCode == 503)) {
            this.mPendingOutgoingCall.handleRegistrationFailed();
            this.mPendingOutgoingCall = null;
        }
    }

    public void onCallEnded() {
        if (this.mPendingOutgoingCall != null) {
            this.mPendingOutgoingCall = null;
        }
    }

    public SipError checkRejectIncomingCall(Context context, ImsRegistration reg, int callType) {
        SipError error;
        int numPsCall;
        ImsRegistration imsRegistration = reg;
        int i = callType;
        SipError error2 = SipErrorBase.OK;
        Mno mno = Mno.fromName(reg.getImsProfile().getMnoName());
        int phoneId = reg.getPhoneId();
        int cmcType = reg.getImsProfile().getCmcType();
        int subId = SimUtil.getSubId(phoneId);
        if (mno == Mno.VZW && this.mVolteServiceModule.getNetwork(phoneId).network == 14 && this.mVolteServiceModule.isMmtelAcquiredEver() && !this.mPdnController.isEpdgConnected(phoneId)) {
            return SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD;
        }
        if (!imsRegistration.hasService("mmtel")) {
            SipError error3 = getSipErrorForNoMmtel(imsRegistration, phoneId, i, mno);
            if (error3 != SipErrorBase.OK) {
                return error3;
            }
            error = error3;
        } else {
            error = error2;
        }
        if (needRejectByTerminalSs(context, imsRegistration, i, phoneId)) {
            return SipErrorBase.BUSY_HERE;
        }
        int[] ret = checkHasCallAndCallType(phoneId);
        boolean isModifyOngoing = false;
        boolean hasInCall = ret[0] == 1;
        int hasInCallType = ret[1];
        boolean hasHoldCall = ret[2] == 1;
        boolean hasConfCall = ret[3] == 1;
        boolean hasTtyCall = ret[4] == 1;
        if (ret[5] == 1) {
            isModifyOngoing = true;
        }
        boolean isModifyOngoing2 = isModifyOngoing;
        int i2 = cmcType;
        boolean hasTtyCall2 = hasTtyCall;
        boolean hasConfCall2 = hasConfCall;
        SipError sipError = error;
        boolean hasHoldCall2 = hasHoldCall;
        SipError error4 = getErrorAsSessionState(phoneId, cmcType, hasInCall, hasHoldCall, callType, mno);
        if (this.mVolteServiceModule.getCmcServiceModule().isCmcRegExist(phoneId)) {
            numPsCall = this.mVolteServiceModule.getCmcServiceModule().getSessionCountByCmcType(phoneId, imsRegistration);
        } else {
            numPsCall = getSessionCount(phoneId);
        }
        Log.i(LOG_TAG, "checkRejectIncomingCall: numPsCall " + numPsCall + ", hasInCall " + hasInCall + ", hasHoldCall " + hasHoldCall2 + ", hasTtyCall " + hasTtyCall2 + " isModifyOngoing " + isModifyOngoing2 + ", hasConfCall " + hasConfCall2 + ", error " + error4);
        ImsRegistration imsRegistration2 = reg;
        boolean z = hasConfCall2;
        SipError error5 = getSipErrorOnCsNetwork(imsRegistration2, mno, phoneId, subId, hasInCall, hasHoldCall2, getSipErrorAsHasCall(imsRegistration2, hasInCall, hasHoldCall2, hasConfCall2, hasInCallType, callType, mno, numPsCall, error4));
        if (mno == Mno.VZW) {
            return getSipErrorForVzw(reg, phoneId, subId, callType, Boolean.valueOf(isModifyOngoing2), Boolean.valueOf(hasTtyCall2), error5);
        } else if (mno == Mno.SPRINT && this.mVolteServiceModule.hasCsCall(phoneId)) {
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        } else {
            ImsProfile imsprofile = reg.getImsProfile();
            if (isModifyOngoing2) {
                return getSipErrorAsModifying(imsprofile);
            }
            if (!this.mVolteServiceModule.hasCsCall(phoneId) || !this.mPdnController.isEpdgConnected(phoneId) || imsprofile.getCmcType() != 0) {
                return error5;
            }
            Log.i(LOG_TAG, "checkRejectIncomingCall: hasCsCall");
            if (mno == Mno.RJIL || mno == Mno.SINGTEL || mno == Mno.FET || mno == Mno.CHT) {
                return SipErrorBase.NOT_ACCEPTABLE_HERE;
            }
            return SipErrorBase.BUSY_HERE;
        }
    }

    private SipError getSipErrorOnCsNetwork(ImsRegistration reg, Mno mno, int phoneId, int subId, boolean hasInCall, boolean hasHoldCall, SipError prevError) {
        if ((mno == Mno.ATT || mno == Mno.TMOBILE) && !reg.getImsProfile().isSoftphoneEnabled() && !hasInCall && !hasHoldCall && !this.mPdnController.isEpdgConnected(phoneId) && ((this.mVolteServiceModule.getNetwork(phoneId).network != 13 && this.mTelephonyManager.getVoiceNetworkType(subId) != 13) || this.mVolteServiceModule.getNetwork(phoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED)) {
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        }
        return prevError;
    }

    private boolean needRejectByTerminalSs(Context context, ImsRegistration reg, int callType, int phoneId) {
        if (!GlobalSettingsManager.getInstance(context, phoneId).getBoolean(GlobalSettingsConstants.SS.CALLBARRING_BY_NETWORK, false) && getSipErrorForBarring(context, phoneId, callType) == SipErrorBase.BUSY_HERE) {
            return true;
        }
        if (!GlobalSettingsManager.getInstance(context, phoneId).getBoolean(GlobalSettingsConstants.SS.CALLWAITING_BY_NETWORK, true) && reg.getImsProfile().getCmcType() == 0) {
            boolean cw = UserConfiguration.getUserConfig(context, phoneId, "enable_call_wait", true);
            int callCount = this.mVolteServiceModule.getCmcServiceHelper().getSessionCountByCmcType(phoneId, 0);
            if (callCount >= 1 && !cw) {
                String str = LOG_TAG;
                Log.i(str, "needRejectByTerminalSs: Terminal CW : " + cw + " callCount : " + callCount + " reject call");
                return true;
            }
        }
        return false;
    }

    private SipError getSipErrorForVzw(ImsRegistration reg, int phoneId, int subId, int callType, Boolean isModifyOngoing, Boolean hasTtyCall, SipError prevError) {
        SipError error = prevError;
        if (isModifyOngoing.booleanValue()) {
            error = SipErrorVzw.VIDEO_UPGRADE_REQUEST_IN_PROGRESS;
        } else if (callType == 2) {
            if ((this.mVolteServiceModule.getTtyMode(phoneId) != Extensions.TelecomManager.TTY_MODE_OFF && this.mVolteServiceModule.getTtyMode(phoneId) != Extensions.TelecomManager.RTT_MODE) || hasTtyCall.booleanValue()) {
                Log.i(LOG_TAG, "checkRejectIncomingCall: VT not allowed during TTY is on.");
                error = SipErrorVzw.TTY_ON;
            } else if (!this.mRegMan.isVoWiFiSupported(phoneId) && this.mTelephonyManager.getDataNetworkType(subId) == 18) {
                error = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "");
            }
        } else if (callType == 1 || callType == 14) {
            if (this.mTelephonyManager.getNetworkType() != 13 || this.mVolteServiceModule.getNetwork(phoneId).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
                if (this.mRegMan.isVoWiFiSupported(phoneId)) {
                    if (this.mPdnController.isEpdgConnected(phoneId) && !this.mVolteServiceModule.isVowifiEnabled(phoneId)) {
                        error = SipErrorVzw.VOWIFI_OFF;
                    }
                } else if (this.mTelephonyManager.getDataNetworkType(subId) == 18) {
                    error = new SipError(NSDSNamespaces.NSDSHttpResponseCode.BUSY_HERE, "");
                }
            } else if (!this.mVolteServiceModule.isVowifiEnabled(phoneId) && this.mPdnController.isEpdgConnected(phoneId) && !reg.hasService("mmtel")) {
                error = SipErrorVzw.VOWIFI_OFF;
            }
        }
        if (!this.mVolteServiceModule.hasCsCall(phoneId)) {
            return error;
        }
        Log.i(LOG_TAG, "checkRejectIncomingCall: hasCsCall");
        return SipErrorVzw.NOT_ACCEPTABLE_ACTIVE_1X_CALL;
    }

    private SipError getSipErrorAsModifying(ImsProfile imsprofile) {
        CallProfile profile;
        Log.i(LOG_TAG, "checkRejectIncomingCall: Reject call while Call modifying");
        SipError error = SipErrorBase.BUSY_HERE;
        if (!isCmcPrimaryType(imsprofile.getCmcType())) {
            return error;
        }
        try {
            ImsCallSession extSession = this.mVolteServiceModule.getCmcServiceModule().getSessionByCmcType(0);
            if (extSession == null || extSession.getCallState() != CallConstants.STATE.ModifyRequested || (profile = extSession.getCallProfile()) == null || profile.getCallType() != 1) {
                return error;
            }
            Log.i(LOG_TAG, "checkRejectIncomingCall: Reject upgrade call for pulling by SD");
            extSession.reject(3);
            return SipErrorBase.OK;
        } catch (RemoteException e) {
            String str = LOG_TAG;
            Log.e(str, "checkRejectIncomingCall: " + e.getMessage());
            return error;
        }
    }

    private SipError getSipErrorAsHasCall(ImsRegistration reg, boolean hasInCall, boolean hasHoldCall, boolean hasConfCall, int hasInCallType, int callType, Mno mno, int numPsCall, SipError prevError) {
        SipError error = prevError;
        if ((!hasInCall || !hasHoldCall) && numPsCall < 2) {
            if (hasInCall && ((hasInCallType == 2 || callType == 2) && ((mno.isKor() && !reg.getImsProfile().getName().contains("PS-LTE")) || mno.isChn()))) {
                String str = LOG_TAG;
                Log.i(str, "checkRejectIncomingCall: hasInCallType: " + hasInCallType + " callType: " + callType);
                error = SipErrorKor.BUSY_HERE;
            }
        } else if (mno == Mno.VZW) {
            if (this.mVolteServiceModule.isEnableCallWaitingRule()) {
                error = SipErrorVzw.BUSY_ALREADY_IN_TWO_CALLS;
            }
        } else if (mno == Mno.ATT || mno == Mno.VODAFONE_AUSTRALIA || mno == Mno.DOCOMO || mno.isChn() || mno == Mno.TMOUS || mno == Mno.VODAFONE_IRELAND) {
            Log.i(LOG_TAG, "checkRejectIncomingCall: 3rd incoming call handling in OneHold and OneActive");
        } else {
            error = SipErrorBase.BUSY_HERE;
        }
        if (mno != Mno.KDDI || !hasConfCall) {
            return error;
        }
        SipError error2 = SipErrorBase.BUSY_HERE;
        String str2 = LOG_TAG;
        Log.i(str2, "checkRejectIncomingCall: error " + error2);
        return error2;
    }

    private SipError getSipErrorForNoMmtel(ImsRegistration reg, int phoneId, int callType, Mno mno) {
        Log.i(LOG_TAG, "checkRejectIncomingCall: no mmtel registration.");
        SipError error = SipErrorBase.OK;
        if (mno == Mno.VZW) {
            if (reg.hasService("mmtel-video")) {
                return error;
            }
            Log.i(LOG_TAG, "checkRejectIncomingCall: no mmtel or mmtel-video registered.");
            if (this.mVolteServiceModule.getNetwork(phoneId).network == 13 && this.mVolteServiceModule.getNetwork(phoneId).voiceOverPs != VoPsIndication.SUPPORTED) {
                return SipErrorVzw.NOT_ACCEPTABLE_NO_VOPS;
            }
            if (this.mVolteServiceModule.getNetwork(phoneId).network == 14) {
                return SipErrorVzw.NOT_ACCEPTABLE_ON_EHRPD;
            }
            if (this.mVolteServiceModule.isCallBarredBySSAC(phoneId, callType)) {
                return SipErrorVzw.NOT_ACCEPTABLE_SSAC_ON;
            }
            if (this.mVolteServiceModule.acceptCallWhileSmsipRegistered(reg)) {
                return SipErrorBase.OK;
            }
            if (!this.mRegMan.isVoWiFiSupported(phoneId) || !this.mPdnController.isEpdgConnected(phoneId) || this.mVolteServiceModule.isVowifiEnabled(phoneId)) {
                return SipErrorVzw.NOT_ACCEPTABLE_VOLTE_OFF;
            }
            return SipErrorVzw.VOWIFI_OFF;
        } else if (mno != Mno.SKT && mno != Mno.KT) {
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        } else {
            if (reg.hasService("mmtel-video")) {
                return error;
            }
            Log.i(LOG_TAG, "checkRejectIncomingCall: no mmtel or mmtel-video registered.");
            return SipErrorBase.NOT_ACCEPTABLE_HERE;
        }
    }

    private SipError getSipErrorForBarring(Context context, int phoneId, int callType) {
        int setValue;
        Log.i(LOG_TAG, "checkRejectIncomingCall: Call barring");
        if (callType == 2) {
            setValue = UserConfiguration.getUserConfig(context, phoneId, "ss_video_cb_pref", 0);
        } else {
            setValue = UserConfiguration.getUserConfig(context, phoneId, "ss_volte_cb_pref", 0);
        }
        if ((setValue & 8) == 8) {
            Log.i(LOG_TAG, "checkRejectIncomingCall: Incoming call is barried");
            return SipErrorBase.BUSY_HERE;
        } else if ((setValue & 10) != 10 || !this.mTelephonyManager.isNetworkRoaming()) {
            return SipErrorBase.OK;
        } else {
            Log.i(LOG_TAG, "checkRejectIncomingCall: Incoming call is barried in raoming condition");
            return SipErrorBase.BUSY_HERE;
        }
    }

    private boolean ignoreCsfbByEpsOnlyNw(ImsRegistration regiInfo, int phoneId, Mno mno) {
        if (regiInfo == null || regiInfo.getImsProfile() == null || mno == Mno.DOCOMO || !regiInfo.getImsProfile().getSupportLtePreferred() || !this.mPdnController.isEpsOnlyReg(phoneId)) {
            return false;
        }
        Log.e(LOG_TAG, "EPS only registered for LTE Preferred model!");
        return true;
    }

    public boolean isCsfbErrorCode(Context context, int phoneId, int callType, SipError error) {
        return isCsfbErrorCode(context, phoneId, callType, error, 10);
    }

    public boolean isCsfbErrorCode(Context context, int phoneId, int callType, SipError error, int retryAfter) {
        Mno mno;
        int i = phoneId;
        int i2 = callType;
        SipError sipError = error;
        int i3 = retryAfter;
        if (sipError == null) {
            Log.e(LOG_TAG, "SipError was null!!");
            return false;
        } else if (!this.mVolteServiceModule.isSilentRedialEnabled(context, i)) {
            Log.e(LOG_TAG, "isSilentRedialEnabled was false!");
            return false;
        } else {
            ImsRegistration regiInfo = getImsRegistration(i);
            if (regiInfo == null) {
                mno = SimManagerFactory.getSimManager().getSimMno();
            } else {
                mno = Mno.fromName(regiInfo.getImsProfile().getMnoName());
            }
            Mno mno2 = mno;
            if (ignoreCsfbByEpsOnlyNw(regiInfo, i, mno2)) {
                Log.i(LOG_TAG, "ignore CSFB due to only EPS network!");
                return false;
            }
            String str = LOG_TAG;
            Log.i(str, "CallType : " + i2 + " SipError : " + sipError);
            if (sipError.equals(SipErrorBase.SIP_INVITE_TIMEOUT)) {
                Log.i(LOG_TAG, "Timer B expired convert to INVITE_TIMEOUT");
                sipError.setCode(1114);
            }
            if (mno2.isOrange() && regiInfo != null && 18 == regiInfo.getRegiRat()) {
                Log.i(LOG_TAG, "isCsfbErrorCode ORANGE GROUP customization in VoWIFI");
                if (isServerSipError(sipError) && this.mVolteServiceModule.isRoaming(i) && !this.mRegMan.getNetworkEvent(regiInfo.getPhoneId()).outOfService) {
                    this.mRegMan.blockVoWifiRegisterOnRoaminByCsfbError(regiInfo.getHandle(), FileTaskUtil.READ_DATA_TIMEOUT);
                    return false;
                }
            }
            if ((mno2 == Mno.LGU || mno2 == Mno.KDDI) && !this.mVolteServiceModule.isRoaming(i)) {
                Log.i(LOG_TAG, "LGU/KDDI - Do not use CSFB in home network");
                return false;
            } else if (mno2 == Mno.MTS_RUSSIA && this.mVolteServiceModule.isRoaming(i)) {
                Log.i(LOG_TAG, "MTS Russia - Do not use CSFB in roaming");
                return false;
            } else if (error.getCode() == 1117) {
                Log.i(LOG_TAG, "CALL_ENDED_BY_NW_HANDOVER_BEFORE_100_TRYING is always trigger CSFB");
                return true;
            } else {
                if (mno2 == Mno.CMCC && (error.getCode() == 503 || error.getCode() == 502 || error.getCode() == 500)) {
                    String str2 = LOG_TAG;
                    Log.i(str2, "CMCC - error code : " + error.getCode() + ", retryAfter : " + i3);
                    if (i3 > 0) {
                        return false;
                    }
                } else if ((mno2.isTmobile() || mno2 == Mno.TELEKOM_ALBANIA) && sipError.equals(SipErrorBase.MEDIA_BEARER_OR_QOS_LOST)) {
                    Log.i(LOG_TAG, "CSFB condition for T-Mobile EUR");
                    return true;
                } else if (mno2 == Mno.VIVO_BRAZIL && this.mVolteServiceModule.isRoaming(i)) {
                    Log.i(LOG_TAG, "VIVO doesn't support CSFB under roaming area");
                    return false;
                } else if (mno2 == Mno.TMOUS) {
                    if (getSessionCount() > 1) {
                        String str3 = LOG_TAG;
                        Log.i(str3, "has another call " + getSessionCount());
                        return false;
                    } else if (error.getCode() == 1001) {
                        Log.i(LOG_TAG, "TMO - Stack return -1 trigger CSFB");
                        return true;
                    }
                } else if (mno2 == Mno.VZW) {
                    boolean isCdmalessEnabled = ImsUtil.isCdmalessEnabled(phoneId);
                    String str4 = LOG_TAG;
                    Log.i(str4, "VZW - roaming(" + this.mVolteServiceModule.isRoaming(i) + ") CDMAless(" + isCdmalessEnabled + ") getLteEpsOnlyAttached(" + this.mVolteServiceModule.getLteEpsOnlyAttached(i) + ")");
                    if ((this.mVolteServiceModule.isRoaming(i) && this.mVolteServiceModule.getLteEpsOnlyAttached(i)) || (!this.mVolteServiceModule.isRoaming(i) && isCdmalessEnabled)) {
                        return false;
                    }
                    if (this.mVolteServiceModule.isRoaming(i) && isCdmalessEnabled && !this.mVolteServiceModule.getLteEpsOnlyAttached(i) && error.getCode() == 2511) {
                        return true;
                    }
                    if (ImsCallUtil.isImsOutageError(error) || error.getCode() == 2502) {
                        if (this.mVolteServiceModule.isRoaming(i) || isCdmalessEnabled) {
                            return false;
                        }
                        return true;
                    } else if (isCdmalessEnabled && error.getCode() == 1601) {
                        return true;
                    }
                } else if (mno2 == Mno.ATT && error.getCode() == 403 && i2 == 12 && this.mVolteServiceModule.isRegisteredOverLteOrNr(i) && this.mVolteServiceModule.getNetwork(i).voiceOverPs == VoPsIndication.NOT_SUPPORTED) {
                    return true;
                }
                return isCsfbErrorCodeOnList(context, phoneId, callType, mno2, error, false);
            }
        }
    }

    public boolean isCsfbErrorCodeOnList(Context context, int phoneId, int callType, Mno mno, SipError error, boolean ret) {
        String key = GlobalSettingsConstants.Call.VOICE_CSFB_ERROR_CODE_LIST;
        if (ImsCallUtil.isVideoCall(callType)) {
            key = GlobalSettingsConstants.Call.VIDEO_CSFB_ERROR_CODE_LIST;
        }
        try {
            String[] errorCodeList = GlobalSettingsManager.getInstance(context, phoneId).getStringArray(GlobalSettingsConstants.Call.ALL_CSFB_ERROR_CODE_LIST, (String[]) null);
            String str = LOG_TAG;
            Log.i(str, "all_csfb_error_code_list " + Arrays.asList(errorCodeList));
            ret = isMatchWithErrorCodeList(errorCodeList, error.getCode());
            if (!ret) {
                String[] errorCodeList2 = GlobalSettingsManager.getInstance(context, phoneId).getStringArray(key, (String[]) null);
                String str2 = LOG_TAG;
                Log.i(str2, key + " " + Arrays.asList(errorCodeList2));
                ret = isMatchWithErrorCodeList(errorCodeList2, error.getCode());
            }
            if (mno == Mno.TMOUS && ((this.mVolteServiceModule.getLteEpsOnlyAttached(phoneId) || this.mRegMan.getNetworkEvent(phoneId).network == 20) && ret && !ImsCallUtil.isE911Call(callType) && error.getCode() != SipErrorBase.ALTERNATIVE_SERVICE.getCode())) {
                ret = false;
            }
            if (!ret && ImsCallUtil.isE911Call(callType)) {
                String key2 = GlobalSettingsConstants.Call.E911_CSFB_ERROR_CODE_LIST;
                String[] errorCodeList3 = GlobalSettingsManager.getInstance(context, phoneId).getStringArray(key2, new String[0]);
                String str3 = LOG_TAG;
                Log.i(str3, key2 + " " + Arrays.asList(errorCodeList3));
                ret = isMatchWithErrorCodeList(errorCodeList3, error.getCode());
                if (mno.isChn() && ((error.getCode() == 381 || error.getCode() == 382) && ImsCallUtil.convertUrnToEccCat(error.getReason()) == 254)) {
                    Log.i(LOG_TAG, "Unrecognized service urn.");
                }
            }
            if (mno.isChn() && error.getCode() == 487 && error.getReason() != null && error.getReason().equals("Destination out of order")) {
                Log.i(LOG_TAG, "need CSFB for call forwarding");
                ret = true;
            }
        } catch (JSONException e) {
            String str4 = LOG_TAG;
            Log.e(str4, "isCsfbErrorCode fail " + e.getMessage());
        }
        String str5 = LOG_TAG;
        Log.i(str5, "isCsfbErrorCode Mno " + mno.getName() + " callType " + callType + " error " + error + " ==> " + ret);
        return ret;
    }

    public ImsRegistration getImsRegistration(int phoneId) {
        ImsRegistration[] registrationList = this.mRegMan.getRegistrationInfoByPhoneId(phoneId);
        if (registrationList == null) {
            return null;
        }
        for (ImsRegistration reg : registrationList) {
            if (reg != null && reg.getPhoneId() == phoneId && !reg.getImsProfile().hasEmergencySupport() && reg.getImsProfile().getCmcType() == 0) {
                return reg;
            }
        }
        return null;
    }

    private boolean isServerSipError(SipError error) {
        return SipErrorBase.SipErrorType.ERROR_5XX.equals(error) || SipErrorBase.SipErrorType.ERROR_6XX.equals(error) || error.getCode() == SipErrorBase.FORBIDDEN.getCode() || error.getCode() == SipErrorBase.REQUEST_TIMEOUT.getCode();
    }

    public boolean isMatchWithErrorCodeList(String[] errorCodeList, int errorCode) throws JSONException {
        boolean ret = false;
        if (errorCodeList != null) {
            int i = 0;
            while (true) {
                if (i >= errorCodeList.length) {
                    break;
                }
                String value = errorCodeList[i].replace("x", "[0-9]");
                ret = String.valueOf(errorCode).matches(value);
                if (ret) {
                    String str = LOG_TAG;
                    Log.i(str, "match with " + value);
                    break;
                }
                i++;
            }
        }
        return ret;
    }

    public int getMergeCallType(int phoneId, boolean isConfCallType) {
        int mergeCallType = 1;
        ImsRegistration regiInfo = getImsRegistration(phoneId);
        if (regiInfo != null) {
            ImsProfile profile = regiInfo.getImsProfile();
            boolean hasRttCall = hasRttCall();
            boolean hasVideoCall = hasVideoCall() && profile.getSupportMergeVideoConference();
            Mno mno = Mno.fromName(profile.getMnoName());
            if (hasVideoCall) {
                mergeCallType = (mno == Mno.ATT || !hasRttCall) ? 2 : 1;
            }
        }
        if (!isConfCallType) {
            return mergeCallType;
        }
        if (mergeCallType == 1) {
            return 5;
        }
        if (mergeCallType == 2) {
            return 6;
        }
        return mergeCallType;
    }

    public void handleEpdnSetupFail(int phoneId) {
        List<ImsCallSession> emergencySessions = getEmergencySession();
        String str = LOG_TAG;
        Log.i(str, "handleEpdnSetupFail Emergency Session Count : " + emergencySessions.size() + " phoneId : " + phoneId);
        for (ImsCallSession session : emergencySessions) {
            try {
                if (phoneId == session.getPhoneId()) {
                    CallProfile profile = session.getCallProfile();
                    if (profile == null || profile.getNetworkType() != 11) {
                        session.terminate(22);
                    } else {
                        Log.i(LOG_TAG, "handleEpdnSetupFail : skip terminate because this session uses ims pdn");
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPhoneIdByCallId(int callId) {
        ImsCallSession session = getSessionByCallId(callId);
        if (session != null) {
            return session.getPhoneId();
        }
        return -1;
    }

    public int getParticipantIdForMerge(int phoneId, int hostId) {
        List<ImsCallSession> heldCalls = getSessionByState(phoneId, CallConstants.STATE.HeldCall);
        if (heldCalls.isEmpty()) {
            Log.e(LOG_TAG, "No Hold Call : conference fail");
            return -1;
        }
        for (ImsCallSession held : heldCalls) {
            if (held.getCallId() != hostId) {
                return held.getCallId();
            }
        }
        return -1;
    }

    public void releaseSessionByState(int phoneId, CallConstants.STATE state) {
        for (ImsCallSession s : getSessionList()) {
            if (s.getPhoneId() == phoneId && s.getCallState() == state) {
                try {
                    s.terminate(5, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int sendRttSessionModifyRequest(int callId, boolean mode) {
        String str = LOG_TAG;
        Log.i(str, "sendRttSessionModifyRequest:callId : " + callId + ", mode : " + mode);
        ImsCallSession inCallSession = getSessionByCallId(callId);
        if (inCallSession == null) {
            String str2 = LOG_TAG;
            Log.i(str2, "callId(" + callId + ") is invalid");
            return -1;
        }
        IMSLog.c(LogClass.VOLTE_SEND_REQUEST_RTT, inCallSession.getPhoneId() + "," + inCallSession.getSessionId() + "," + mode);
        int currCallType = inCallSession.getCallProfile().getCallType();
        if (ImsCallUtil.isRttCall(currCallType) && mode) {
            this.mVolteServiceModule.onSendRttSessionModifyResponse(callId, mode, true);
            return 0;
        } else if (ImsCallUtil.isRttCall(currCallType) || mode) {
            CallProfile callProfile = new CallProfile();
            callProfile.setCallType(0);
            String str3 = LOG_TAG;
            Log.i(str3, "SessionId : " + inCallSession.getSessionId() + ", currCallType : " + currCallType);
            callProfile.setCallType(ImsCallUtil.getCallTypeForRtt(currCallType, mode));
            if (mode) {
                int phoneId = inCallSession.getPhoneId();
                if (!ImsRegistry.getPdnController().isEpdgConnected(phoneId)) {
                    inCallSession.startRttDedicatedBearerTimer(this.mVolteServiceModule.getRttDbrTimer(phoneId));
                }
            }
            try {
                inCallSession.update(callProfile, 0, "");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return 0;
        } else {
            this.mVolteServiceModule.onSendRttSessionModifyResponse(callId, mode, false);
            return 0;
        }
    }

    public void sendRttSessionModifyResponse(int callId, boolean accept) {
        String str = LOG_TAG;
        Log.i(str, "sendRttSessionModifyResponse: callId : " + callId + ", accept : " + accept);
        ImsCallSession inCallSession = getSessionByCallId(callId);
        if (inCallSession == null) {
            String str2 = LOG_TAG;
            Log.i(str2, "callId(" + callId + ") is invalid");
            return;
        }
        IMSLog.c(LogClass.VOLTE_SEND_RESPONSE_RTT, inCallSession.getPhoneId() + "," + inCallSession.getSessionId() + "," + accept);
        CallProfile callProfile = new CallProfile();
        callProfile.setCallType(0);
        int currCallType = inCallSession.getCallProfile().getCallType();
        String str3 = LOG_TAG;
        Log.i(str3, "SessionId : " + inCallSession.getSessionId() + ", currCallType : " + currCallType);
        callProfile.setCallType(ImsCallUtil.getCallTypeForRtt(currCallType, true));
        try {
            int phoneId = inCallSession.getPhoneId();
            if (accept) {
                if (ImsCallUtil.isRttCall(callProfile.getCallType())) {
                    if (!ImsRegistry.getPdnController().isEpdgConnected(phoneId)) {
                        inCallSession.startRttDedicatedBearerTimer(this.mVolteServiceModule.getRttDbrTimer(phoneId));
                    }
                    inCallSession.getCallProfile().getMediaProfile().setRttMode(1);
                } else {
                    inCallSession.getCallProfile().getMediaProfile().setRttMode(0);
                }
                inCallSession.accept(callProfile);
                return;
            }
            inCallSession.reject(0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isRttCall(int sessionId) {
        CallProfile profile;
        ImsCallSession session = getSessionByCallId(sessionId);
        if (session == null || (profile = session.getCallProfile()) == null) {
            return false;
        }
        boolean result = ImsCallUtil.isRttCall(profile.getCallType());
        String str = LOG_TAG;
        Log.i(str, "isRttCall, sessionId=" + sessionId + ", result=" + result);
        return result;
    }

    public Map<Integer, ImsCallSession> getUnmodifiableSessionMap() {
        return this.mUnmodifiableSessionMap;
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
