package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.os.PowerManager;
import com.sec.internal.log.AECLog;

public class PowerController {
    private final String LOG_TAG = PowerController.class.getSimpleName();
    private final int mPhoneId;
    private final String mTag;
    final PowerManager.WakeLock mWakeLock;

    public PowerController(Context context, int phoneId) {
        this.mPhoneId = phoneId;
        this.mTag = this.LOG_TAG + this.mPhoneId;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, this.mTag);
    }

    public void lock(long timeout) {
        String str = this.LOG_TAG;
        AECLog.d(str, "lock: " + this.mTag, this.mPhoneId);
        this.mWakeLock.acquire(timeout);
    }

    public void release() {
        if (this.mWakeLock.isHeld()) {
            String str = this.LOG_TAG;
            AECLog.d(str, "release: " + this.mTag, this.mPhoneId);
            this.mWakeLock.release();
        }
    }

    public void sleep(long millis) {
        String str = this.LOG_TAG;
        AECLog.d(str, "sleep: " + millis, this.mPhoneId);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
