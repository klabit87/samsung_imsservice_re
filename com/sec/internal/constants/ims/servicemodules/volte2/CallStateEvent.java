package com.sec.internal.constants.ims.servicemodules.volte2;

import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class CallStateEvent {
    private ALTERNATIVE_SERVICE mAlternativeService = ALTERNATIVE_SERVICE.NONE;
    private String mAlternativeServiceReason = "";
    private String mAlternativeServiceType = "";
    private String mAlternativeServiceUrn = "";
    private int mCallType = -1;
    private String mCmcCallTime = "";
    private String mCmcDeviceId = "";
    private boolean mIsConference = false;
    private boolean mIsSdToSdPull = false;
    private CallParams mParams = null;
    private NameAddr mPeerAddr = null;
    private boolean mRemoteVideoCapa = false;
    private int mRetryAfter = 0;
    private int mSessionID = -1;
    private SipError mSipErrorCode = null;
    private CALL_STATE mState = CALL_STATE.NOT_INITIALIZED;
    private List<ParticipantUser> mUpdatedParticipants = new ArrayList();

    public enum ALTERNATIVE_SERVICE {
        NONE,
        INITIAL_REGISTRATION,
        EMERGENCY_REGISTRATION,
        EMERGENCY
    }

    public enum CALL_STATE {
        NOT_INITIALIZED,
        CALLING,
        TRYING,
        EARLY_MEDIA_START,
        SESSIONPROGRESS,
        RINGING_BACK,
        FORWARDED,
        ESTABLISHED,
        REFRESHFAIL,
        ENDED,
        HELD_LOCAL,
        HELD_REMOTE,
        HELD_BOTH,
        MODIFY_REQUESTED,
        MODIFIED,
        CONFERENCE_ADDED,
        CONFERENCE_REMOVED,
        CONFERENCE_PARTICIPANTS_UPDATED,
        EXTEND_TO_CONFERENCE,
        ERROR
    }

    public static class ParticipantUser {
        private final int mParticipantId;
        private final int mParticipantStatus;
        private final int mSessionId;
        private final String mUri;

        public ParticipantUser(int participantId, int sessionId, int participantStatus, String uri) {
            this.mParticipantId = participantId;
            this.mSessionId = sessionId;
            this.mParticipantStatus = participantStatus;
            this.mUri = uri;
        }

        public int getParticipantId() {
            return this.mParticipantId;
        }

        public int getParticipantStatus() {
            return this.mParticipantStatus;
        }

        public int getSessionId() {
            return this.mSessionId;
        }

        public String getUri() {
            return this.mUri;
        }
    }

    public CallStateEvent() {
    }

    public CallStateEvent(CALL_STATE state) {
        this.mState = state;
    }

    public void setState(CALL_STATE state) {
        this.mState = state;
    }

    public CALL_STATE getState() {
        return this.mState;
    }

    public void setSessionID(int sessionID) {
        this.mSessionID = sessionID;
    }

    public int getSessionID() {
        return this.mSessionID;
    }

    public void setCallType(int type) {
        this.mCallType = type;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public void setPeerAddr(NameAddr peerAddr) {
        this.mPeerAddr = peerAddr;
    }

    public NameAddr getPeerAddr() {
        return this.mPeerAddr;
    }

    public void setRemoteVideoCapa(boolean capa) {
        this.mRemoteVideoCapa = capa;
    }

    public boolean getRemoteVideoCapa() {
        return this.mRemoteVideoCapa;
    }

    public void setParams(CallParams params) {
        this.mParams = params;
    }

    public CallParams getParams() {
        return this.mParams;
    }

    public void addUpdatedParticipantsList(String uri, int participantId, int sessionId, int status) {
        this.mUpdatedParticipants.add(new ParticipantUser(participantId, sessionId, status, uri));
    }

    public List<ParticipantUser> getUpdatedParticipantsList() {
        return this.mUpdatedParticipants;
    }

    public void setErrorCode(SipError errorCode) {
        this.mSipErrorCode = errorCode;
    }

    public SipError getErrorCode() {
        return this.mSipErrorCode;
    }

    public void setAlternativeService(ALTERNATIVE_SERVICE as) {
        this.mAlternativeService = as;
    }

    public ALTERNATIVE_SERVICE getAlternativeService() {
        return this.mAlternativeService;
    }

    public void setAlternativeServiceType(String type) {
        this.mAlternativeServiceType = type;
    }

    public String getAlternativeServiceType() {
        return this.mAlternativeServiceType;
    }

    public void setAlternativeServiceReason(String reason) {
        this.mAlternativeServiceReason = reason;
    }

    public String getAlternativeServiceReason() {
        return this.mAlternativeServiceReason;
    }

    public void setAlternativeServiceUrn(String serviceUrn) {
        this.mAlternativeServiceUrn = serviceUrn;
    }

    public String getAlternativeServiceUrn() {
        return this.mAlternativeServiceUrn;
    }

    public void setRetryAfter(int retryAfter) {
        this.mRetryAfter = retryAfter;
    }

    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    public void setConference(boolean conf) {
        this.mIsConference = conf;
    }

    public boolean isConference() {
        return this.mIsConference;
    }

    public String getCmcDeviceId() {
        return this.mCmcDeviceId;
    }

    public void setCmcDeviceId(String cmcDeviceId) {
        this.mCmcDeviceId = cmcDeviceId;
    }

    public String getCmcCallTime() {
        return this.mCmcCallTime;
    }

    public void setCmcCallTime(String cmcCallTime) {
        this.mCmcCallTime = cmcCallTime;
    }

    public boolean getIsSdToSdPull() {
        return this.mIsSdToSdPull;
    }

    public void setIsSdToSdPull(boolean isSdToSdPull) {
        this.mIsSdToSdPull = isSdToSdPull;
    }

    public String toString() {
        return "CallStateEvent [sessionId=" + this.mSessionID + ", state=" + this.mState + ", peer=" + IMSLog.checker(this.mPeerAddr) + ", mSipErrorCode=" + this.mSipErrorCode + ", Params=" + this.mParams + "]";
    }
}
