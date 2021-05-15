package com.google.android.gms.common.internal;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

final class zzh extends Handler {
    private /* synthetic */ zzd zzgfk;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public zzh(zzd zzd, Looper looper) {
        super(looper);
        this.zzgfk = zzd;
    }

    private static void zza(Message message) {
        zzi zzi = (zzi) message.obj;
        zzi.zzamb();
        zzi.unregister();
    }

    private static boolean zzb(Message message) {
        return message.what == 2 || message.what == 1 || message.what == 7;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v24, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v2, resolved type: android.app.PendingIntent} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void handleMessage(android.os.Message r8) {
        /*
            r7 = this;
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            java.util.concurrent.atomic.AtomicInteger r0 = r0.zzgfh
            int r0 = r0.get()
            int r1 = r8.arg1
            if (r0 == r1) goto L_0x0016
            boolean r0 = zzb(r8)
            if (r0 == 0) goto L_0x0015
            zza(r8)
        L_0x0015:
            return
        L_0x0016:
            int r0 = r8.what
            r1 = 4
            r2 = 1
            r3 = 5
            if (r0 == r2) goto L_0x002a
            int r0 = r8.what
            r4 = 7
            if (r0 == r4) goto L_0x002a
            int r0 = r8.what
            if (r0 == r1) goto L_0x002a
            int r0 = r8.what
            if (r0 != r3) goto L_0x0036
        L_0x002a:
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            boolean r0 = r0.isConnecting()
            if (r0 != 0) goto L_0x0036
            zza(r8)
            return
        L_0x0036:
            int r0 = r8.what
            r4 = 8
            r5 = 3
            r6 = 0
            if (r0 != r1) goto L_0x0081
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            com.google.android.gms.common.ConnectionResult r1 = new com.google.android.gms.common.ConnectionResult
            int r8 = r8.arg2
            r1.<init>(r8)
            com.google.android.gms.common.ConnectionResult unused = r0.zzgff = r1
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            boolean r8 = r8.zzalz()
            if (r8 == 0) goto L_0x0060
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            boolean r8 = r8.zzgfg
            if (r8 != 0) goto L_0x0060
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            r8.zza((int) r5, null)
            return
        L_0x0060:
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            com.google.android.gms.common.ConnectionResult r8 = r8.zzgff
            if (r8 == 0) goto L_0x006f
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            com.google.android.gms.common.ConnectionResult r8 = r8.zzgff
            goto L_0x0074
        L_0x006f:
            com.google.android.gms.common.ConnectionResult r8 = new com.google.android.gms.common.ConnectionResult
            r8.<init>(r4)
        L_0x0074:
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            com.google.android.gms.common.internal.zzj r0 = r0.zzgew
            r0.zzf(r8)
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            r0.onConnectionFailed(r8)
            return
        L_0x0081:
            int r0 = r8.what
            if (r0 != r3) goto L_0x00a6
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            com.google.android.gms.common.ConnectionResult r8 = r8.zzgff
            if (r8 == 0) goto L_0x0094
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            com.google.android.gms.common.ConnectionResult r8 = r8.zzgff
            goto L_0x0099
        L_0x0094:
            com.google.android.gms.common.ConnectionResult r8 = new com.google.android.gms.common.ConnectionResult
            r8.<init>(r4)
        L_0x0099:
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            com.google.android.gms.common.internal.zzj r0 = r0.zzgew
            r0.zzf(r8)
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            r0.onConnectionFailed(r8)
            return
        L_0x00a6:
            int r0 = r8.what
            if (r0 != r5) goto L_0x00c9
            java.lang.Object r0 = r8.obj
            boolean r0 = r0 instanceof android.app.PendingIntent
            if (r0 == 0) goto L_0x00b5
            java.lang.Object r0 = r8.obj
            r6 = r0
            android.app.PendingIntent r6 = (android.app.PendingIntent) r6
        L_0x00b5:
            com.google.android.gms.common.ConnectionResult r0 = new com.google.android.gms.common.ConnectionResult
            int r8 = r8.arg2
            r0.<init>(r8, r6)
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            com.google.android.gms.common.internal.zzj r8 = r8.zzgew
            r8.zzf(r0)
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            r8.onConnectionFailed(r0)
            return
        L_0x00c9:
            int r0 = r8.what
            r1 = 6
            if (r0 != r1) goto L_0x00f3
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            r0.zza((int) r3, null)
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            com.google.android.gms.common.internal.zzf r0 = r0.zzgfb
            if (r0 == 0) goto L_0x00e6
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            com.google.android.gms.common.internal.zzf r0 = r0.zzgfb
            int r1 = r8.arg2
            r0.onConnectionSuspended(r1)
        L_0x00e6:
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            int r8 = r8.arg2
            r0.onConnectionSuspended(r8)
            com.google.android.gms.common.internal.zzd r8 = r7.zzgfk
            boolean unused = r8.zza((int) r3, (int) r2, r6)
            return
        L_0x00f3:
            int r0 = r8.what
            r1 = 2
            if (r0 != r1) goto L_0x0104
            com.google.android.gms.common.internal.zzd r0 = r7.zzgfk
            boolean r0 = r0.isConnected()
            if (r0 != 0) goto L_0x0104
            zza(r8)
            return
        L_0x0104:
            boolean r0 = zzb(r8)
            if (r0 == 0) goto L_0x0112
            java.lang.Object r8 = r8.obj
            com.google.android.gms.common.internal.zzi r8 = (com.google.android.gms.common.internal.zzi) r8
            r8.zzamc()
            return
        L_0x0112:
            int r8 = r8.what
            r0 = 45
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>(r0)
            java.lang.String r0 = "Don't know how to handle message: "
            r1.append(r0)
            r1.append(r8)
            java.lang.String r8 = r1.toString()
            java.lang.Exception r0 = new java.lang.Exception
            r0.<init>()
            java.lang.String r1 = "GmsClient"
            android.util.Log.wtf(r1, r8, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.internal.zzh.handleMessage(android.os.Message):void");
    }
}
