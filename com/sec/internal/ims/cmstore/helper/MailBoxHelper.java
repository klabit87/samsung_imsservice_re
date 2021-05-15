package com.sec.internal.ims.cmstore.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MailBoxHelper {
    private static final String TAG = MailBoxHelper.class.getSimpleName();

    public static boolean isMailBoxReset(String strbody) {
        JSONObject notification;
        JSONObject nmsEventList;
        JSONArray nmsEvents;
        JSONObject nmsEvent;
        try {
            JSONArray notifications = new JSONObject(strbody).getJSONArray("notificationList");
            if (notifications == null || (notification = (JSONObject) notifications.opt(0)) == null || (nmsEventList = notification.getJSONObject("nmsEventList")) == null || (nmsEvents = nmsEventList.getJSONArray("nmsEvent")) == null || (nmsEvent = (JSONObject) nmsEvents.opt(0)) == null || !nmsEvent.has("resetBox")) {
                return false;
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
