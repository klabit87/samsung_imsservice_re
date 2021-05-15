package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.AndsfMessage;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.FcmMessage;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class AndsfMessageParser implements IFcmMessageParser {
    private static final String LOG_TAG = AndsfMessageParser.class.getSimpleName();

    public FcmMessage parseMessage(Map data) {
        String messageType = null;
        if (data.get(NSDSNamespaces.NSDSExtras.MESSAGE_TYPE) != null) {
            messageType = data.get(NSDSNamespaces.NSDSExtras.MESSAGE_TYPE).toString();
            String str = LOG_TAG;
            IMSLog.s(str, "parseMessage: messageType " + messageType);
        }
        return new AndsfMessage(messageType);
    }
}
