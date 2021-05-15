package com.sec.internal.ims.imsservice;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.sec.ims.extensions.Extensions;

public class CapabilityService extends ImsServiceBase {
    private static final String LOG_TAG = "CapabilityService";

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind:");
        if (Extensions.UserHandle.myUserId() == 0) {
            return this.mBinder.getBinder("options");
        }
        Log.d(LOG_TAG, "Do not allow bind on non-system user");
        return null;
    }
}
