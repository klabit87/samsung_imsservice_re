package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public final class ImdnNotificationEvent {
    public final Date mCpimDate;
    public final Date mImdnDate;
    public final String mImdnId;
    public ImsUri mRemoteUri;
    public final NotificationStatus mStatus;

    public ImdnNotificationEvent(String imdnId, Date imdnDate, ImsUri remoteUri, NotificationStatus status, Date cpimDate) {
        this.mImdnId = imdnId;
        this.mImdnDate = imdnDate;
        this.mRemoteUri = remoteUri;
        this.mStatus = status;
        this.mCpimDate = cpimDate;
    }

    public String toString() {
        return "ImdnNotificationEvent [mImdnId=" + this.mImdnId + ", mImdnDate=" + this.mImdnDate + ", mRemoteUri=" + IMSLog.checker(this.mRemoteUri) + ", mStatus=" + this.mStatus + ", mCpimDate=" + this.mCpimDate + "]";
    }
}
