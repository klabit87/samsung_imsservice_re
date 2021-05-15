package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.FcmMessage;
import java.util.Map;

public interface IFcmMessageParser {
    FcmMessage parseMessage(Map map);
}
