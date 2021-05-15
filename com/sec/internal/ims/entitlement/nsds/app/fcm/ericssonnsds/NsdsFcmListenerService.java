package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import android.content.Context;
import com.sec.internal.constants.ims.entitilement.FcmNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.fcm.interfaces.IFcmEventListener;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.FcmMessage;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class NsdsFcmListenerService implements IFcmEventListener {
    private static final String LOG_TAG = NsdsFcmListenerService.class.getSimpleName();

    public void onMessageReceived(Context context, String from, Map data) {
        String str = LOG_TAG;
        IMSLog.s(str, "onMessageReceived: From: " + from + "data: " + data.toString());
        IFcmMessageParser parser = getPnsParser(data);
        if (parser != null) {
            FcmMessage message = parser.parseMessage(data);
            if (message != null && message.shouldBroadcast(context)) {
                message.broadcastFcmMessage(context);
                return;
            }
            return;
        }
        IMSLog.e(LOG_TAG, "onMessageReceived: parsing failed.");
    }

    private IFcmMessageParser getPnsParser(Map data) {
        if (data == null) {
            IMSLog.e(LOG_TAG, "getPnsParser: data null, vail");
            return null;
        } else if (data.containsKey(NSDSNamespaces.NSDSExtras.MESSAGE_TYPE)) {
            IMSLog.s(LOG_TAG, "getPnsParser: AndsfMessageParser");
            return new AndsfMessageParser();
        } else if (data.get(FcmNamespaces.PUSH_MESSAGE) != null) {
            IMSLog.s(LOG_TAG, "getPnsParser: PushMessageParser");
            return new PushMessageParser();
        } else if (data.get("message") == null) {
            return null;
        } else {
            IMSLog.s(LOG_TAG, "getPnsParser: EventListMessageParser");
            return new EventListMessageParser();
        }
    }
}
