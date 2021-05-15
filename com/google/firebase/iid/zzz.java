package com.google.firebase.iid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.SimpleArrayMap;
import com.sec.internal.imscr.LogClass;
import java.util.ArrayDeque;
import java.util.Queue;

public final class zzz {
    private static zzz zzolj;
    private final SimpleArrayMap<String, String> zzolk = new SimpleArrayMap<>();
    private Boolean zzoll = null;
    final Queue<Intent> zzolm = new ArrayDeque();
    private Queue<Intent> zzoln = new ArrayDeque();

    private zzz() {
    }

    public static PendingIntent zza(Context context, int i, Intent intent, int i2) {
        Intent intent2 = new Intent(context, FirebaseInstanceIdReceiver.class);
        intent2.setAction("com.google.firebase.MESSAGING_EVENT");
        intent2.putExtra("wrapped_intent", intent);
        return PendingIntent.getBroadcast(context, i, intent2, LogClass.IM_SWITCH_OFF);
    }

    public static synchronized zzz zzclq() {
        zzz zzz;
        synchronized (zzz.class) {
            if (zzolj == null) {
                zzolj = new zzz();
            }
            zzz = zzolj;
        }
        return zzz;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00db A[Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00f2 A[Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00f7 A[Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }] */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0104 A[Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x010e A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final int zze(android.content.Context r6, android.content.Intent r7) {
        /*
            r5 = this;
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r0 = r5.zzolk
            monitor-enter(r0)
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r1 = r5.zzolk     // Catch:{ all -> 0x0143 }
            java.lang.String r2 = r7.getAction()     // Catch:{ all -> 0x0143 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x0143 }
            java.lang.String r1 = (java.lang.String) r1     // Catch:{ all -> 0x0143 }
            monitor-exit(r0)     // Catch:{ all -> 0x0143 }
            r0 = 0
            if (r1 != 0) goto L_0x00ab
            android.content.pm.PackageManager r1 = r6.getPackageManager()
            android.content.pm.ResolveInfo r1 = r1.resolveService(r7, r0)
            if (r1 == 0) goto L_0x00a3
            android.content.pm.ServiceInfo r2 = r1.serviceInfo
            if (r2 != 0) goto L_0x0023
            goto L_0x00a3
        L_0x0023:
            android.content.pm.ServiceInfo r1 = r1.serviceInfo
            java.lang.String r2 = r6.getPackageName()
            java.lang.String r3 = r1.packageName
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x006d
            java.lang.String r2 = r1.name
            if (r2 != 0) goto L_0x0036
            goto L_0x006d
        L_0x0036:
            java.lang.String r1 = r1.name
            java.lang.String r2 = "."
            boolean r2 = r1.startsWith(r2)
            if (r2 == 0) goto L_0x005c
            java.lang.String r2 = r6.getPackageName()
            java.lang.String r2 = java.lang.String.valueOf(r2)
            java.lang.String r1 = java.lang.String.valueOf(r1)
            int r3 = r1.length()
            if (r3 == 0) goto L_0x0057
            java.lang.String r1 = r2.concat(r1)
            goto L_0x005c
        L_0x0057:
            java.lang.String r1 = new java.lang.String
            r1.<init>(r2)
        L_0x005c:
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r2 = r5.zzolk
            monitor-enter(r2)
            android.support.v4.util.SimpleArrayMap<java.lang.String, java.lang.String> r3 = r5.zzolk     // Catch:{ all -> 0x006a }
            java.lang.String r4 = r7.getAction()     // Catch:{ all -> 0x006a }
            r3.put(r4, r1)     // Catch:{ all -> 0x006a }
            monitor-exit(r2)     // Catch:{ all -> 0x006a }
            goto L_0x00ab
        L_0x006a:
            r6 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x006a }
            throw r6
        L_0x006d:
            java.lang.String r2 = r1.packageName
            java.lang.String r1 = r1.name
            java.lang.String r3 = java.lang.String.valueOf(r2)
            int r3 = r3.length()
            int r3 = r3 + 94
            java.lang.String r4 = java.lang.String.valueOf(r1)
            int r4 = r4.length()
            int r3 = r3 + r4
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>(r3)
            java.lang.String r3 = "Error resolving target intent service, skipping classname enforcement. Resolved service was: "
            r4.append(r3)
            r4.append(r2)
            java.lang.String r2 = "/"
            r4.append(r2)
            r4.append(r1)
            java.lang.String r1 = r4.toString()
            java.lang.String r2 = "FirebaseInstanceId"
            android.util.Log.e(r2, r1)
            goto L_0x00d7
        L_0x00a3:
            java.lang.String r1 = "FirebaseInstanceId"
            java.lang.String r2 = "Failed to resolve target intent service, skipping classname enforcement"
            android.util.Log.e(r1, r2)
            goto L_0x00d7
        L_0x00ab:
            r2 = 3
            java.lang.String r3 = "FirebaseInstanceId"
            boolean r2 = android.util.Log.isLoggable(r3, r2)
            if (r2 == 0) goto L_0x00d0
            java.lang.String r2 = "Restricting intent to a specific service: "
            java.lang.String r3 = java.lang.String.valueOf(r1)
            int r4 = r3.length()
            if (r4 == 0) goto L_0x00c5
            java.lang.String r2 = r2.concat(r3)
            goto L_0x00cb
        L_0x00c5:
            java.lang.String r3 = new java.lang.String
            r3.<init>(r2)
            r2 = r3
        L_0x00cb:
            java.lang.String r3 = "FirebaseInstanceId"
            android.util.Log.d(r3, r2)
        L_0x00d0:
            java.lang.String r2 = r6.getPackageName()
            r7.setClassName(r2, r1)
        L_0x00d7:
            java.lang.Boolean r1 = r5.zzoll     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            if (r1 != 0) goto L_0x00ea
            java.lang.String r1 = "android.permission.WAKE_LOCK"
            int r1 = r6.checkCallingOrSelfPermission(r1)     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            if (r1 != 0) goto L_0x00e4
            r0 = 1
        L_0x00e4:
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r0)     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            r5.zzoll = r0     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
        L_0x00ea:
            java.lang.Boolean r0 = r5.zzoll     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            boolean r0 = r0.booleanValue()     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            if (r0 == 0) goto L_0x00f7
            android.content.ComponentName r6 = android.support.v4.content.WakefulBroadcastReceiver.startWakefulService(r6, r7)     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            goto L_0x0102
        L_0x00f7:
            android.content.ComponentName r6 = r6.startService(r7)     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            java.lang.String r7 = "FirebaseInstanceId"
            java.lang.String r0 = "Missing wake lock permission, service start may be delayed"
            android.util.Log.d(r7, r0)     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
        L_0x0102:
            if (r6 != 0) goto L_0x010e
            java.lang.String r6 = "FirebaseInstanceId"
            java.lang.String r7 = "Error while delivering the message: ServiceIntent not found."
            android.util.Log.e(r6, r7)     // Catch:{ SecurityException -> 0x0138, IllegalStateException -> 0x0110 }
            r6 = 404(0x194, float:5.66E-43)
            return r6
        L_0x010e:
            r6 = -1
            return r6
        L_0x0110:
            r6 = move-exception
            java.lang.String r6 = java.lang.String.valueOf(r6)
            java.lang.String r7 = java.lang.String.valueOf(r6)
            int r7 = r7.length()
            int r7 = r7 + 45
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>(r7)
            java.lang.String r7 = "Failed to start service while in background: "
            r0.append(r7)
            r0.append(r6)
            java.lang.String r6 = r0.toString()
            java.lang.String r7 = "FirebaseInstanceId"
            android.util.Log.e(r7, r6)
            r6 = 402(0x192, float:5.63E-43)
            return r6
        L_0x0138:
            r6 = move-exception
            java.lang.String r7 = "FirebaseInstanceId"
            java.lang.String r0 = "Error while delivering the message to the serviceIntent"
            android.util.Log.e(r7, r0, r6)
            r6 = 401(0x191, float:5.62E-43)
            return r6
        L_0x0143:
            r6 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0143 }
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzz.zze(android.content.Context, android.content.Intent):int");
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final int zzb(android.content.Context r4, java.lang.String r5, android.content.Intent r6) {
        /*
            r3 = this;
            int r0 = r5.hashCode()
            r1 = -842411455(0xffffffffcdc9d241, float:-4.23249952E8)
            r2 = 1
            if (r0 == r1) goto L_0x001a
            r1 = 41532704(0x279bd20, float:1.8347907E-37)
            if (r0 == r1) goto L_0x0010
            goto L_0x0024
        L_0x0010:
            java.lang.String r0 = "com.google.firebase.MESSAGING_EVENT"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = r2
            goto L_0x0025
        L_0x001a:
            java.lang.String r0 = "com.google.firebase.INSTANCE_ID_EVENT"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x0024
            r0 = 0
            goto L_0x0025
        L_0x0024:
            r0 = -1
        L_0x0025:
            if (r0 == 0) goto L_0x004b
            if (r0 == r2) goto L_0x0048
            java.lang.String r4 = "Unknown service action: "
            java.lang.String r5 = java.lang.String.valueOf(r5)
            int r6 = r5.length()
            if (r6 == 0) goto L_0x003a
            java.lang.String r4 = r4.concat(r5)
            goto L_0x0040
        L_0x003a:
            java.lang.String r5 = new java.lang.String
            r5.<init>(r4)
            r4 = r5
        L_0x0040:
            java.lang.String r5 = "FirebaseInstanceId"
            android.util.Log.w(r5, r4)
            r4 = 500(0x1f4, float:7.0E-43)
            return r4
        L_0x0048:
            java.util.Queue<android.content.Intent> r0 = r3.zzoln
            goto L_0x004d
        L_0x004b:
            java.util.Queue<android.content.Intent> r0 = r3.zzolm
        L_0x004d:
            r0.offer(r6)
            android.content.Intent r6 = new android.content.Intent
            r6.<init>(r5)
            java.lang.String r5 = r4.getPackageName()
            r6.setPackage(r5)
            int r4 = r3.zze(r4, r6)
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzz.zzb(android.content.Context, java.lang.String, android.content.Intent):int");
    }

    public final Intent zzclr() {
        return this.zzoln.poll();
    }
}
