package com.sec.internal.ims.cmstore.params;

import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.GCMPushNotification;
import com.sec.internal.omanetapi.nms.data.NmsEventList;

public class ParamNmsNotificationList {
    public final int mDataContractType;
    public final String mDataType;
    public final String mLine;
    public final NmsEventList mNmsEventList;

    public ParamNmsNotificationList(String dataType, int contracType, String line, GCMPushNotification notification) {
        this.mDataType = dataType;
        this.mDataContractType = contracType;
        this.mNmsEventList = notification.nmsEventList;
        this.mLine = line;
    }

    public String toString() {
        return "ParamNmsNotificationList [mDataType= " + this.mDataType + " mDataContractType = " + this.mDataContractType + " mLine = " + IMSLog.checker(this.mLine) + " mOriginalMessage = ]";
    }
}
