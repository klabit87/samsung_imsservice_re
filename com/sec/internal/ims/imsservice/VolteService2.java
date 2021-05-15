package com.sec.internal.ims.imsservice;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class VolteService2 extends ImsServiceBase {
    private static final String LOG_TAG = "VolteService";

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind:" + intent);
        if (super.onBind(intent) == null) {
            return null;
        }
        return this.mBinder.getBinder("mmtel");
    }
}
