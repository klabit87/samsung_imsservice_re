package com.sec.imsservice.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.sec.internal.ims.imsservice.ImsServiceBase;

public class VshService extends ImsServiceBase {
    private static final String LOG_TAG = VshService.class.getSimpleName();

    public IBinder onBind(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onBind:" + intent);
        return this.mBinder.getBinder("vs");
    }
}
