package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.entitilement.FcmNamespaces;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.FcmMessage;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.PushMessage;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class PushMessageParser implements IFcmMessageParser {
    private static final String LOG_TAG = PushMessageParser.class.getSimpleName();

    public FcmMessage parseMessage(Map data) {
        Gson gson = new Gson();
        String origMessage = null;
        String confirmationUrl = null;
        if (data.get(FcmNamespaces.PUSH_MESSAGE) != null) {
            origMessage = data.get(FcmNamespaces.PUSH_MESSAGE).toString();
        }
        if (data.get("confirmation_url") != null) {
            confirmationUrl = data.get("confirmation_url").toString();
        }
        try {
            if (TextUtils.isEmpty(origMessage)) {
                return null;
            }
            PushMessage pushMessage = (PushMessage) gson.fromJson(origMessage, PushMessage.class);
            pushMessage.setOrigMessage(origMessage);
            pushMessage.setConfirmUrl(confirmationUrl);
            String str = LOG_TAG;
            IMSLog.i(str, "parseMessage: message type-" + pushMessage.pnsType + " subtype-" + pushMessage.pnsSubtype);
            return pushMessage;
        } catch (JsonSyntaxException e) {
            String str2 = LOG_TAG;
            IMSLog.e(str2, "cannot parse received message" + e.getMessage());
            return null;
        }
    }
}
