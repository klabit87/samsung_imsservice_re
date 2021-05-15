package com.sec.internal.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

public class AlarmTimer {
    private static final int BUILD_VERSION_CODE_JELLY_BEAN_MR2 = 18;
    private static final String LOG_TAG = "AlarmTimer";

    public static void start(Context context, PendingIntent intent, long millis) {
        Log.d(LOG_TAG, "start: " + intent + " millis " + millis);
        ((AlarmManager) context.getSystemService("alarm")).setExact(2, SystemClock.elapsedRealtime() + millis, intent);
    }

    public static void stop(Context context, PendingIntent intent) {
        Log.d(LOG_TAG, "stop: " + intent);
        ((AlarmManager) context.getSystemService("alarm")).cancel(intent);
    }
}
