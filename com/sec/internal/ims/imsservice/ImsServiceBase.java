package com.sec.internal.ims.imsservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.samsung.android.ims.SemImsService;
import com.sec.ims.IImsService;
import com.sec.ims.extensions.Extensions;

public abstract class ImsServiceBase extends Service {
    private static final String LOG_TAG = ImsServiceBase.class.getSimpleName();
    protected IImsService.Stub mBinder = null;
    protected SemImsService.Stub mSemBinder = null;

    public void onCreate() {
        super.onCreate();
        try {
            if (Extensions.UserHandle.myUserId() != 0) {
                Log.d(LOG_TAG, "Do not initialize on non-system user");
                stopSelf();
                return;
            }
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "IllegalStateException occurred");
        }
        Log.i(LOG_TAG, "onCreate(): ");
        this.mBinder = ImsServiceStub.getInstance();
        this.mSemBinder = SemImsServiceStub.getInstance();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    public IBinder onBind(Intent intent) {
        if (Extensions.UserHandle.myUserId() == 0) {
            return this.mBinder;
        }
        Log.d(LOG_TAG, "Do not allow bind on non-system user");
        return null;
    }
}
