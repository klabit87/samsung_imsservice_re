package com.google.android.gms.internal;

import android.content.Context;
import android.os.PowerManager;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.common.util.zzaa;
import com.google.android.gms.common.util.zzw;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public final class zzcyz {
    private static boolean DEBUG = false;
    private static String TAG = "WakeLock";
    private static ScheduledExecutorService zzimq;
    private static String zzkma = "*gcore*:";
    private final Context mContext;
    private final String zzgjx;
    private final String zzgjz;
    private final PowerManager.WakeLock zzkmb;
    private WorkSource zzkmc;
    private final int zzkmd;
    private final String zzkme;
    private boolean zzkmf;
    private final Map<String, Integer[]> zzkmg;
    private int zzkmh;
    private AtomicInteger zzkmi;

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public zzcyz(Context context, int i, String str) {
        this(context, 1, str, (String) null, context == null ? null : context.getPackageName());
    }

    private zzcyz(Context context, int i, String str, String str2, String str3) {
        this(context, 1, str, (String) null, str3, (String) null);
    }

    private zzcyz(Context context, int i, String str, String str2, String str3, String str4) {
        this.zzkmf = true;
        this.zzkmg = new HashMap();
        this.zzkmi = new AtomicInteger(0);
        zzbq.zzh(str, "Wake lock name can NOT be empty");
        this.zzkmd = i;
        this.zzkme = null;
        this.zzgjz = null;
        this.mContext = context.getApplicationContext();
        if (!"com.google.android.gms".equals(context.getPackageName())) {
            String valueOf = String.valueOf(zzkma);
            String valueOf2 = String.valueOf(str);
            this.zzgjx = valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf);
        } else {
            this.zzgjx = str;
        }
        this.zzkmb = ((PowerManager) context.getSystemService("power")).newWakeLock(i, str);
        if (zzaa.zzda(this.mContext)) {
            WorkSource zzw = zzaa.zzw(context, zzw.zzhb(str3) ? context.getPackageName() : str3);
            this.zzkmc = zzw;
            if (zzw != null && zzaa.zzda(this.mContext)) {
                WorkSource workSource = this.zzkmc;
                if (workSource != null) {
                    workSource.add(zzw);
                } else {
                    this.zzkmc = zzw;
                }
                try {
                    this.zzkmb.setWorkSource(this.zzkmc);
                } catch (IllegalArgumentException e) {
                    Log.wtf(TAG, e.toString());
                }
            }
        }
        if (zzimq == null) {
            zzimq = zzbhg.zzanc().newSingleThreadScheduledExecutor();
        }
    }

    /* access modifiers changed from: private */
    public final void zzew(int i) {
        if (this.zzkmb.isHeld()) {
            try {
                this.zzkmb.release();
            } catch (RuntimeException e) {
                if (e.getClass().equals(RuntimeException.class)) {
                    Log.e(TAG, String.valueOf(this.zzgjx).concat("was already released!"), new IllegalStateException());
                    return;
                }
                throw e;
            }
        }
    }

    private final String zzlf(String str) {
        return this.zzkmf ? !TextUtils.isEmpty(str) ? str : this.zzkme : this.zzkme;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0052, code lost:
        if (r13 == false) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005a, code lost:
        if (r11.zzkmh == 0) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        com.google.android.gms.common.stats.zze.zzanp();
        com.google.android.gms.common.stats.zze.zza(r11.mContext, com.google.android.gms.common.stats.zzc.zza(r11.zzkmb, r4), 7, r11.zzgjx, r4, (java.lang.String) null, r11.zzkmd, com.google.android.gms.common.util.zzaa.zzb(r11.zzkmc), 1000);
        r11.zzkmh++;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void acquire(long r12) {
        /*
            r11 = this;
            java.util.concurrent.atomic.AtomicInteger r12 = r11.zzkmi
            r12.incrementAndGet()
            r12 = 0
            java.lang.String r4 = r11.zzlf(r12)
            monitor-enter(r11)
            java.util.Map<java.lang.String, java.lang.Integer[]> r12 = r11.zzkmg     // Catch:{ all -> 0x0092 }
            boolean r12 = r12.isEmpty()     // Catch:{ all -> 0x0092 }
            r13 = 0
            if (r12 == 0) goto L_0x0018
            int r12 = r11.zzkmh     // Catch:{ all -> 0x0092 }
            if (r12 <= 0) goto L_0x0027
        L_0x0018:
            android.os.PowerManager$WakeLock r12 = r11.zzkmb     // Catch:{ all -> 0x0092 }
            boolean r12 = r12.isHeld()     // Catch:{ all -> 0x0092 }
            if (r12 != 0) goto L_0x0027
            java.util.Map<java.lang.String, java.lang.Integer[]> r12 = r11.zzkmg     // Catch:{ all -> 0x0092 }
            r12.clear()     // Catch:{ all -> 0x0092 }
            r11.zzkmh = r13     // Catch:{ all -> 0x0092 }
        L_0x0027:
            boolean r12 = r11.zzkmf     // Catch:{ all -> 0x0092 }
            r10 = 1
            if (r12 == 0) goto L_0x0054
            java.util.Map<java.lang.String, java.lang.Integer[]> r12 = r11.zzkmg     // Catch:{ all -> 0x0092 }
            java.lang.Object r12 = r12.get(r4)     // Catch:{ all -> 0x0092 }
            java.lang.Integer[] r12 = (java.lang.Integer[]) r12     // Catch:{ all -> 0x0092 }
            if (r12 != 0) goto L_0x0045
            java.util.Map<java.lang.String, java.lang.Integer[]> r12 = r11.zzkmg     // Catch:{ all -> 0x0092 }
            java.lang.Integer[] r0 = new java.lang.Integer[r10]     // Catch:{ all -> 0x0092 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r10)     // Catch:{ all -> 0x0092 }
            r0[r13] = r1     // Catch:{ all -> 0x0092 }
            r12.put(r4, r0)     // Catch:{ all -> 0x0092 }
            r13 = r10
            goto L_0x0052
        L_0x0045:
            r0 = r12[r13]     // Catch:{ all -> 0x0092 }
            int r0 = r0.intValue()     // Catch:{ all -> 0x0092 }
            int r0 = r0 + r10
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x0092 }
            r12[r13] = r0     // Catch:{ all -> 0x0092 }
        L_0x0052:
            if (r13 != 0) goto L_0x005c
        L_0x0054:
            boolean r12 = r11.zzkmf     // Catch:{ all -> 0x0092 }
            if (r12 != 0) goto L_0x007d
            int r12 = r11.zzkmh     // Catch:{ all -> 0x0092 }
            if (r12 != 0) goto L_0x007d
        L_0x005c:
            com.google.android.gms.common.stats.zze.zzanp()     // Catch:{ all -> 0x0092 }
            android.content.Context r0 = r11.mContext     // Catch:{ all -> 0x0092 }
            android.os.PowerManager$WakeLock r12 = r11.zzkmb     // Catch:{ all -> 0x0092 }
            java.lang.String r1 = com.google.android.gms.common.stats.zzc.zza(r12, r4)     // Catch:{ all -> 0x0092 }
            r2 = 7
            java.lang.String r3 = r11.zzgjx     // Catch:{ all -> 0x0092 }
            r5 = 0
            int r6 = r11.zzkmd     // Catch:{ all -> 0x0092 }
            android.os.WorkSource r12 = r11.zzkmc     // Catch:{ all -> 0x0092 }
            java.util.List r7 = com.google.android.gms.common.util.zzaa.zzb(r12)     // Catch:{ all -> 0x0092 }
            r8 = 1000(0x3e8, double:4.94E-321)
            com.google.android.gms.common.stats.zze.zza(r0, r1, r2, r3, r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0092 }
            int r12 = r11.zzkmh     // Catch:{ all -> 0x0092 }
            int r12 = r12 + r10
            r11.zzkmh = r12     // Catch:{ all -> 0x0092 }
        L_0x007d:
            monitor-exit(r11)     // Catch:{ all -> 0x0092 }
            android.os.PowerManager$WakeLock r12 = r11.zzkmb
            r12.acquire()
            java.util.concurrent.ScheduledExecutorService r12 = zzimq
            com.google.android.gms.internal.zzcza r13 = new com.google.android.gms.internal.zzcza
            r13.<init>(r11)
            r0 = 1000(0x3e8, double:4.94E-321)
            java.util.concurrent.TimeUnit r2 = java.util.concurrent.TimeUnit.MILLISECONDS
            r12.schedule(r13, r0, r2)
            return
        L_0x0092:
            r12 = move-exception
            monitor-exit(r11)     // Catch:{ all -> 0x0092 }
            throw r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzcyz.acquire(long):void");
    }

    public final boolean isHeld() {
        return this.zzkmb.isHeld();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0045, code lost:
        if (r0 != false) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004d, code lost:
        if (r11.zzkmh == 1) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        com.google.android.gms.common.stats.zze.zzanp();
        com.google.android.gms.common.stats.zze.zza(r11.mContext, com.google.android.gms.common.stats.zzc.zza(r11.zzkmb, r5), 8, r11.zzgjx, r5, (java.lang.String) null, r11.zzkmd, com.google.android.gms.common.util.zzaa.zzb(r11.zzkmc));
        r11.zzkmh--;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void release() {
        /*
            r11 = this;
            java.util.concurrent.atomic.AtomicInteger r0 = r11.zzkmi
            int r0 = r0.decrementAndGet()
            if (r0 >= 0) goto L_0x0010
            java.lang.String r0 = TAG
            java.lang.String r1 = "release without a matched acquire!"
            android.util.Log.e(r0, r1)
        L_0x0010:
            r0 = 0
            java.lang.String r5 = r11.zzlf(r0)
            monitor-enter(r11)
            boolean r0 = r11.zzkmf     // Catch:{ all -> 0x0074 }
            r9 = 1
            r10 = 0
            if (r0 == 0) goto L_0x0047
            java.util.Map<java.lang.String, java.lang.Integer[]> r0 = r11.zzkmg     // Catch:{ all -> 0x0074 }
            java.lang.Object r0 = r0.get(r5)     // Catch:{ all -> 0x0074 }
            java.lang.Integer[] r0 = (java.lang.Integer[]) r0     // Catch:{ all -> 0x0074 }
            if (r0 != 0) goto L_0x0028
        L_0x0026:
            r0 = r10
            goto L_0x0045
        L_0x0028:
            r1 = r0[r10]     // Catch:{ all -> 0x0074 }
            int r1 = r1.intValue()     // Catch:{ all -> 0x0074 }
            if (r1 != r9) goto L_0x0037
            java.util.Map<java.lang.String, java.lang.Integer[]> r0 = r11.zzkmg     // Catch:{ all -> 0x0074 }
            r0.remove(r5)     // Catch:{ all -> 0x0074 }
            r0 = r9
            goto L_0x0045
        L_0x0037:
            r1 = r0[r10]     // Catch:{ all -> 0x0074 }
            int r1 = r1.intValue()     // Catch:{ all -> 0x0074 }
            int r1 = r1 - r9
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0074 }
            r0[r10] = r1     // Catch:{ all -> 0x0074 }
            goto L_0x0026
        L_0x0045:
            if (r0 != 0) goto L_0x004f
        L_0x0047:
            boolean r0 = r11.zzkmf     // Catch:{ all -> 0x0074 }
            if (r0 != 0) goto L_0x006f
            int r0 = r11.zzkmh     // Catch:{ all -> 0x0074 }
            if (r0 != r9) goto L_0x006f
        L_0x004f:
            com.google.android.gms.common.stats.zze.zzanp()     // Catch:{ all -> 0x0074 }
            android.content.Context r1 = r11.mContext     // Catch:{ all -> 0x0074 }
            android.os.PowerManager$WakeLock r0 = r11.zzkmb     // Catch:{ all -> 0x0074 }
            java.lang.String r2 = com.google.android.gms.common.stats.zzc.zza(r0, r5)     // Catch:{ all -> 0x0074 }
            r3 = 8
            java.lang.String r4 = r11.zzgjx     // Catch:{ all -> 0x0074 }
            r6 = 0
            int r7 = r11.zzkmd     // Catch:{ all -> 0x0074 }
            android.os.WorkSource r0 = r11.zzkmc     // Catch:{ all -> 0x0074 }
            java.util.List r8 = com.google.android.gms.common.util.zzaa.zzb(r0)     // Catch:{ all -> 0x0074 }
            com.google.android.gms.common.stats.zze.zza(r1, r2, r3, r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0074 }
            int r0 = r11.zzkmh     // Catch:{ all -> 0x0074 }
            int r0 = r0 - r9
            r11.zzkmh = r0     // Catch:{ all -> 0x0074 }
        L_0x006f:
            monitor-exit(r11)     // Catch:{ all -> 0x0074 }
            r11.zzew(r10)
            return
        L_0x0074:
            r0 = move-exception
            monitor-exit(r11)     // Catch:{ all -> 0x0074 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzcyz.release():void");
    }

    public final void setReferenceCounted(boolean z) {
        this.zzkmb.setReferenceCounted(false);
        this.zzkmf = false;
    }
}
