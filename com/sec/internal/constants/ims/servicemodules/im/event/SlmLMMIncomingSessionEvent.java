package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;

public class SlmLMMIncomingSessionEvent {
    public ImsUri mInitiator;
    public String mInitiatorAlias;
    public String mOwnImsi;
    public Object mRawHandle;

    public String toString() {
        return "SlmLMMIncomingSessionEvent [mRawHandle=" + this.mRawHandle + ", mInitiator=" + IMSLog.checker(this.mInitiator) + ", mInitiatorAlias=" + IMSLog.checker(this.mInitiatorAlias) + "]";
    }
}
