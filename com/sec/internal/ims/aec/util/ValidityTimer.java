package com.sec.internal.ims.aec.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.log.AECLog;

public class ValidityTimer {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ValidityTimer.class.getSimpleName();
    private final Context mContext;
    /* access modifiers changed from: private */
    public final int mPhoneId;
    PendingIntent mTokenValidityPendingIntent = null;
    PendingIntent mVersionValidityPendingIntent = null;

    public ValidityTimer(Context context, int phoneId, final Handler handler) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
            /* JADX WARNING: Removed duplicated region for block: B:16:0x0065  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(android.content.Context r5, android.content.Intent r6) {
                /*
                    r4 = this;
                    java.lang.String r0 = r6.getAction()
                    int r1 = r0.hashCode()
                    r2 = -2131835899(0xffffffff80eec405, float:-2.1927167E-38)
                    r3 = 1
                    if (r1 == r2) goto L_0x001e
                    r2 = 1909717062(0x71d3f846, float:2.0992474E30)
                    if (r1 == r2) goto L_0x0014
                L_0x0013:
                    goto L_0x0028
                L_0x0014:
                    java.lang.String r1 = "com.sec.imsservice.aec.action.VERSION_VALIDITY_TIMEOUT"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0013
                    r0 = 0
                    goto L_0x0029
                L_0x001e:
                    java.lang.String r1 = "com.sec.imsservice.aec.action.TOKEN_VALIDITY_TIMEOUT"
                    boolean r0 = r0.equals(r1)
                    if (r0 == 0) goto L_0x0013
                    r0 = r3
                    goto L_0x0029
                L_0x0028:
                    r0 = -1
                L_0x0029:
                    if (r0 == 0) goto L_0x0065
                    if (r0 != r3) goto L_0x004a
                    java.lang.String r0 = com.sec.internal.ims.aec.util.ValidityTimer.LOG_TAG
                    com.sec.internal.ims.aec.util.ValidityTimer r1 = com.sec.internal.ims.aec.util.ValidityTimer.this
                    int r1 = r1.mPhoneId
                    java.lang.String r2 = "token validity timer is expired"
                    com.sec.internal.log.AECLog.i(r0, r2, r1)
                    com.sec.internal.ims.aec.util.ValidityTimer r0 = com.sec.internal.ims.aec.util.ValidityTimer.this
                    r0.stopTokenValidityTimer()
                    android.os.Handler r0 = r6
                    r1 = 1011(0x3f3, float:1.417E-42)
                    r0.sendEmptyMessage(r1)
                    goto L_0x0082
                L_0x004a:
                    java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "Unexpected value: "
                    r1.append(r2)
                    java.lang.String r2 = r6.getAction()
                    r1.append(r2)
                    java.lang.String r1 = r1.toString()
                    r0.<init>(r1)
                    throw r0
                L_0x0065:
                    java.lang.String r0 = com.sec.internal.ims.aec.util.ValidityTimer.LOG_TAG
                    com.sec.internal.ims.aec.util.ValidityTimer r1 = com.sec.internal.ims.aec.util.ValidityTimer.this
                    int r1 = r1.mPhoneId
                    java.lang.String r2 = "version validity timer is expired"
                    com.sec.internal.log.AECLog.i(r0, r2, r1)
                    com.sec.internal.ims.aec.util.ValidityTimer r0 = com.sec.internal.ims.aec.util.ValidityTimer.this
                    r0.stopVersionValidityTimer()
                    android.os.Handler r0 = r6
                    r1 = 1010(0x3f2, float:1.415E-42)
                    r0.sendEmptyMessage(r1)
                L_0x0082:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.util.ValidityTimer.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
            }
        }, getIntentFilter());
    }

    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AECNamespace.Action.VERSION_VALIDITY_TIMEOUT);
        intentFilter.addAction(AECNamespace.Action.TOKEN_VALIDITY_TIMEOUT);
        return intentFilter;
    }

    public void startVersionValidityTimer(int period) {
        if (period > 0) {
            String str = LOG_TAG;
            AECLog.i(str, "startVersionValidityTimer: " + period + " sec", this.mPhoneId);
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, new Intent(AECNamespace.Action.VERSION_VALIDITY_TIMEOUT), 134217728);
            this.mVersionValidityPendingIntent = broadcast;
            AlarmTimer.start(this.mContext, broadcast, ((long) period) * 1000);
            return;
        }
        AECLog.i(LOG_TAG, "startVersionValidityTimer: no limitation of duration.", this.mPhoneId);
    }

    public void startTokenValidityTimer(int period) {
        if (period > 0) {
            String str = LOG_TAG;
            AECLog.i(str, "startTokenValidityTimer: " + period + " sec", this.mPhoneId);
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, new Intent(AECNamespace.Action.TOKEN_VALIDITY_TIMEOUT), 134217728);
            this.mTokenValidityPendingIntent = broadcast;
            AlarmTimer.start(this.mContext, broadcast, ((long) period) * 1000);
            return;
        }
        AECLog.i(LOG_TAG, "startTokenValidityTimer: no limitation of duration.", this.mPhoneId);
    }

    public void stopVersionValidityTimer() {
        if (this.mVersionValidityPendingIntent != null) {
            AECLog.i(LOG_TAG, "stopVersionValidityTimer", this.mPhoneId);
            AlarmTimer.stop(this.mContext, this.mVersionValidityPendingIntent);
            this.mVersionValidityPendingIntent = null;
        }
    }

    public void stopTokenValidityTimer() {
        if (this.mTokenValidityPendingIntent != null) {
            AECLog.i(LOG_TAG, "stopTokenValidityTimer", this.mPhoneId);
            AlarmTimer.stop(this.mContext, this.mTokenValidityPendingIntent);
            this.mTokenValidityPendingIntent = null;
        }
    }
}
