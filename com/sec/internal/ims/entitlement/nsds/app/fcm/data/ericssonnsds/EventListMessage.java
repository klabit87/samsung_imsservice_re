package com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import com.google.gson.annotations.SerializedName;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;

public class EventListMessage extends FcmMessage {
    private static final String LOG_TAG = EventListMessage.class.getSimpleName();
    @SerializedName("eventList")
    public EventList eventList;

    public static class EventList {
        @SerializedName("date")
        public String date;
        @SerializedName("events")
        public ArrayList<EventType> events;
    }

    public boolean shouldBroadcast(Context context) {
        return true;
    }

    public void broadcastFcmMessage(Context context) {
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION);
        intent.putExtra(NSDSNamespaces.NSDSExtras.ORIG_PUSH_MESSAGE, this.origMessage);
        EventList eventList2 = this.eventList;
        if (eventList2 != null) {
            intent.putExtra("date", eventList2.date);
            intent.putStringArrayListExtra(NSDSNamespaces.NSDSExtras.EVENT_LIST, createStringArrayListFromEvents());
        }
        intent.putExtra(NSDSNamespaces.NSDSExtras.NOTIFCATION_TITLE, getNotificationTitle());
        intent.putExtra(NSDSNamespaces.NSDSExtras.NOTIFCATION_CONTENT, getNotificationContent());
        String str = LOG_TAG;
        IMSLog.s(str, "push notification broadcastIntent: " + intent.toString() + intent.getExtras());
        IntentUtil.sendBroadcast(context, intent, ContextExt.CURRENT_OR_SELF);
    }

    private ArrayList<String> createStringArrayListFromEvents() {
        ArrayList<String> eventsList = new ArrayList<>();
        Iterator<EventType> it = this.eventList.events.iterator();
        while (it.hasNext()) {
            eventsList.add(it.next().toString());
        }
        return eventsList;
    }

    private String getNotificationTitle() {
        return "PNS: ";
    }

    private String getNotificationContent() {
        if (this.eventList != null) {
            return "date:" + this.eventList.date + " events:" + this.eventList.events;
        }
        return "malformed messsage: " + this.origMessage;
    }
}
