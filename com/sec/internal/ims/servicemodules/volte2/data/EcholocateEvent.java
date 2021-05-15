package com.sec.internal.ims.servicemodules.volte2.data;

public class EcholocateEvent {
    EcholocateRtpMessage mRtpData;
    EcholocateSignalMessage mSignalData;
    EcholocateType mType;

    public enum EcholocateType {
        signalMsg,
        rtpMsg
    }

    public static class EcholocateSignalMessage {
        String callId;
        String contents;
        String cseq;
        boolean isEpdgCall;
        String line1;
        String origin;
        String reason;
        String sessionid;

        public String getOrigin() {
            return this.origin;
        }

        public String getLine1() {
            return this.line1;
        }

        public String getCallId() {
            return this.callId;
        }

        public String getCseq() {
            return this.cseq;
        }

        public String getSessionid() {
            return this.sessionid;
        }

        public String getReason() {
            return this.reason;
        }

        public String getContents() {
            return this.contents;
        }

        public boolean isEpdgCall() {
            return this.isEpdgCall;
        }

        public EcholocateSignalMessage(String origin2, String line12, String callId2, String cseq2, String sessionid2, String reason2, String contents2, boolean isEpdgCall2) {
            this.origin = origin2;
            this.line1 = line12;
            this.callId = callId2;
            this.cseq = cseq2;
            this.sessionid = sessionid2;
            this.reason = reason2;
            this.contents = contents2;
            this.isEpdgCall = isEpdgCall2;
        }
    }

    public static class EcholocateRtpMessage {
        String delay;
        String dir;
        String id;
        String jitter;
        String lossrate;
        String measuredperiod;

        public String getDir() {
            return this.dir;
        }

        public String getId() {
            return this.id;
        }

        public String getLossrate() {
            return this.lossrate;
        }

        public String getDelay() {
            return this.delay;
        }

        public String getJitter() {
            return this.jitter;
        }

        public String getMeasuredperiod() {
            return this.measuredperiod;
        }

        public EcholocateRtpMessage(String dir2, String id2, String lossrate2, String delay2, String jitter2, String measuredperiod2) {
            this.dir = dir2;
            this.id = id2;
            this.lossrate = lossrate2;
            this.delay = delay2;
            this.jitter = jitter2;
            this.measuredperiod = measuredperiod2;
        }
    }

    public void setType(EcholocateType type) {
        this.mType = type;
    }

    public EcholocateType getType() {
        return this.mType;
    }

    public void setSignalData(String origin, String line1, String callId, String cseq, String sessionid, String reason, String contents, boolean isEpdgCall) {
        this.mSignalData = new EcholocateSignalMessage(origin, line1, callId, cseq, sessionid, reason, contents, isEpdgCall);
    }

    public EcholocateSignalMessage getSignalData() {
        return this.mSignalData;
    }

    public void setRtpData(String dir, String id, String lossrate, String delay, String jitter, String measuredperiod) {
        this.mRtpData = new EcholocateRtpMessage(dir, id, lossrate, delay, jitter, measuredperiod);
    }

    public EcholocateRtpMessage getRtpData() {
        return this.mRtpData;
    }
}
