package com.sec.internal.ims.entitlement.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.AlarmTimer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IntentScheduler {
    private static final String LOG_TAG = IntentScheduler.class.getSimpleName();
    private static Map<String, PendingIntent> mActionPendingIntent = new ConcurrentHashMap();

    public static void scheduleTimer(Context context, int slotid, String action, long millis) {
        scheduleTimer(context, slotid, action, (Bundle) null, millis);
    }

    public static void scheduleTimer(Context context, int slotid, String action, Bundle extras, long millis) {
        if (mActionPendingIntent.get(intentkey(slotid, action)) != null) {
            stopTimer(context, slotid, action);
        }
        AlarmTimer.start(context, getPendingIntent(context, slotid, action, extras), millis);
        String str = LOG_TAG;
        Log.i(str, "scheduled action: " + action + " with time: " + millis + "Pending timers:" + mActionPendingIntent);
    }

    public static boolean hasActionPendingIntent(int slotid, String action) {
        if (mActionPendingIntent.get(intentkey(slotid, action)) != null) {
            return true;
        }
        return false;
    }

    private static PendingIntent getPendingIntent(Context context, int slotid, String action, Bundle extras) {
        PendingIntent pendingIntent = mActionPendingIntent.get(intentkey(slotid, action));
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent();
        intent.setAction(action);
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putInt(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, slotid);
        intent.putExtras(extras);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent, 134217728);
        mActionPendingIntent.put(intentkey(slotid, action), pendingIntent2);
        return pendingIntent2;
    }

    public static void stopTimer(Context context, int slotid, String action) {
        stopTimer(context, intentkey(slotid, action));
    }

    private static void stopTimer(Context context, String intentkey) {
        PendingIntent pendingIntent = mActionPendingIntent.get(intentkey);
        if (pendingIntent == null) {
            String str = LOG_TAG;
            Log.i(str, "stopTimer: intentkey:" + intentkey + " is not running");
            return;
        }
        AlarmTimer.stop(context, pendingIntent);
        String str2 = LOG_TAG;
        Log.i(str2, "stopped Timer for intentkey: " + intentkey);
        mActionPendingIntent.remove(intentkey);
    }

    public static void stopAllTimers(Context context) {
        Log.i(LOG_TAG, "stopAllTimers()");
        ArrayList<String> scheduledActions = new ArrayList<>();
        scheduledActions.addAll(mActionPendingIntent.keySet());
        Iterator<String> it = scheduledActions.iterator();
        while (it.hasNext()) {
            stopTimer(context, it.next());
        }
    }

    private static String intentkey(int slotid, String action) {
        if (slotid < 0) {
            slotid = 0;
        }
        return slotid + ":" + action;
    }
}
