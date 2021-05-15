package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class ImSubjectData {
    private final ImsUri mParticipant;
    private final String mSubject;
    private final Date mTimestamp;

    public ImSubjectData(String subject, ImsUri participant, Date timestamp) {
        this.mSubject = subject;
        this.mParticipant = participant;
        this.mTimestamp = timestamp;
    }

    public String getSubject() {
        return this.mSubject;
    }

    public ImsUri getParticipant() {
        return this.mParticipant;
    }

    public Date getTimestamp() {
        return this.mTimestamp;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImSubjectData other = (ImSubjectData) obj;
        if (!this.mSubject.equals(other.mSubject) || !this.mParticipant.equals(other.mParticipant) || !this.mTimestamp.equals(other.mTimestamp)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "ImSubjectData [subject=" + IMSLog.checker(this.mSubject) + ", participant=" + IMSLog.checker(this.mParticipant) + ", timestamp=" + this.mTimestamp + ']';
    }
}
