package com.sec.internal.ims.imsservice;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.sec.ims.extensions.Extensions;

public class ImsStatusService extends ImsServiceBase {
    private static final String LOG_TAG = ImsStatusService.class.getSimpleName();

    public void onCreate() {
        super.onCreate();
    }

    public IBinder onBind(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onBind:" + intent);
        if (Extensions.UserHandle.myUserId() == 0) {
            return this.mBinder.getBinder("ImsStatus");
        }
        Log.d(LOG_TAG, "Do not allow bind on non-system user");
        return null;
    }
}
