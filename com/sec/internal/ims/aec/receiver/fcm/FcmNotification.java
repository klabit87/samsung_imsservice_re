package com.sec.internal.ims.aec.receiver.fcm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.sec.internal.ims.entitlement.fcm.interfaces.IFcmEventListener;
import com.sec.internal.log.AECLog;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class FcmNotification implements IFcmEventListener {
    private static final String FCM_APP = "app";
    private static final String FCM_DATA = "data";
    private static final String FCM_FROM = "from";
    private static final String FCM_TIMESTAMP = "timestamp";
    private static final String LOG_TAG = FcmNotification.class.getSimpleName();
    private static final String[] filterStr = {"\"", "\\[", "\\]", "app="};
    private final Handler mModuleHandler;

    public FcmNotification(Handler handler) {
        this.mModuleHandler = handler;
    }

    public void onMessageReceived(Context context, String from, Map data) {
        String str = LOG_TAG;
        AECLog.s(str, "onMessageReceived: " + data.toString() + " from " + from);
        sendFcmNotification(getFcmNotification(from, data));
    }

    private void sendFcmNotification(Map<String, String> notification) {
        Bundle bundle = new Bundle();
        bundle.putString("from", notification.get("from"));
        bundle.putString("app", notification.get("app"));
        bundle.putString("timestamp", notification.get("timestamp"));
        Message message = this.mModuleHandler.obtainMessage();
        message.what = 7;
        message.obj = bundle;
        this.mModuleHandler.sendMessage(message);
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> getFcmNotification(String from, Map data) {
        Map<String, String> notification = new HashMap<>();
        try {
            notification.put("from", from);
            if (data.get(FCM_DATA) != null) {
                JSONObject message = new JSONObject(String.valueOf(data.get(FCM_DATA)));
                notification.put("app", filterStr(message.optString("app", "")));
                notification.put("timestamp", message.optString("timestamp", ""));
            } else if (!(data.get("app") == null || data.get("timestamp") == null)) {
                notification.put("app", filterStr(String.valueOf(data.get("app"))));
                notification.put("timestamp", String.valueOf(data.get("timestamp")));
            }
        } catch (JSONException e) {
            String str = LOG_TAG;
            AECLog.e(str, "getFcmNotification: " + e.getMessage());
        }
        return notification;
    }

    /* access modifiers changed from: package-private */
    public String filterStr(String str) {
        String filteredStr = str.replaceAll("&", ",").trim();
        for (String filter : filterStr) {
            filteredStr = filteredStr.replaceAll(filter, "");
        }
        return filteredStr;
    }
}
