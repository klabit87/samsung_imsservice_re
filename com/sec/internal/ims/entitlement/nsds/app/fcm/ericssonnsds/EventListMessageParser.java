package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.EventListMessage;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.FcmMessage;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class EventListMessageParser implements IFcmMessageParser {
    private static final String LOG_TAG = EventListMessageParser.class.getSimpleName();

    public FcmMessage parseMessage(Map data) {
        Gson gson = new Gson();
        EventListMessage message = null;
        try {
            if (data.get("message") != null) {
                String origMessage = data.get("message").toString();
                message = (EventListMessage) gson.fromJson(origMessage, EventListMessage.class);
                message.setOrigMessage(origMessage);
                if (message.eventList != null) {
                    String str = LOG_TAG;
                    IMSLog.s(str, "parseMessage: event date-" + message.eventList.date + " events-" + message.eventList.events);
                } else {
                    String str2 = LOG_TAG;
                    IMSLog.e(str2, "parseMessage: parsing failed for " + origMessage);
                }
            }
        } catch (JsonSyntaxException e) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "cannot parse received message" + e.getMessage());
        }
        return message;
    }
}
