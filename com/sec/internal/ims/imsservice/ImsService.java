package com.sec.internal.ims.imsservice;

import android.content.Intent;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.extensions.Extensions;

public class ImsService extends ImsServiceBase {
    private static final String LOG_TAG = ImsService.class.getSimpleName();
    private static final String SERVICE_UPDATABLE_IMS_NAME = "secims";

    public void onCreate() {
        super.onCreate();
        if (Extensions.UserHandle.myUserId() == 0) {
            Log.i(LOG_TAG, "onCreate(): ");
            ServiceManager.addService(SERVICE_UPDATABLE_IMS_NAME, this.mBinder);
            ContextExt.sendBroadcastAsUser(this, new Intent("com.sec.ims.imsmanager.RESTART"), ContextExt.ALL);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String str = LOG_TAG;
        Log.i(str, "onStartCommand(): Received start id " + startId + ": " + intent);
        if (intent == null || this.mBinder == null) {
            return 1;
        }
        this.mBinder.handleIntent(intent);
        return 1;
    }

    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy(): ");
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onBind: intent " + intent);
        if (Extensions.UserHandle.myUserId() == 0) {
            return this.mBinder;
        }
        Log.d(LOG_TAG, "Do not allow bind on non-system user");
        return null;
    }
}
