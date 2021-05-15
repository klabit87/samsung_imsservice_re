package com.google.firebase.iid;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class zzk {
    private static zzk zzokw;
    /* access modifiers changed from: private */
    public final Context zzaiq;
    /* access modifiers changed from: private */
    public final ScheduledExecutorService zzind;
    private int zzinf = 1;
    private zzm zzokx = new zzm(this);

    private zzk(Context context, ScheduledExecutorService scheduledExecutorService) {
        this.zzind = scheduledExecutorService;
        this.zzaiq = context.getApplicationContext();
    }

    private final synchronized <T> Task<T> zza(zzt<T> zzt) {
        if (Log.isLoggable("MessengerIpcClient", 3)) {
            String valueOf = String.valueOf(zzt);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 9);
            sb.append("Queueing ");
            sb.append(valueOf);
            Log.d("MessengerIpcClient", sb.toString());
        }
        if (!this.zzokx.zzb(zzt)) {
            zzm zzm = new zzm(this);
            this.zzokx = zzm;
            zzm.zzb(zzt);
        }
        return zzt.zzgyc.getTask();
    }

    private final synchronized int zzaws() {
        int i;
        i = this.zzinf;
        this.zzinf = i + 1;
        return i;
    }

    public static synchronized zzk zzfa(Context context) {
        zzk zzk;
        synchronized (zzk.class) {
            if (zzokw == null) {
                zzokw = new zzk(context, Executors.newSingleThreadScheduledExecutor());
            }
            zzk = zzokw;
        }
        return zzk;
    }

    public final Task<Bundle> zzj(int i, Bundle bundle) {
        return zza(new zzv(zzaws(), 1, bundle));
    }

    public final Task<Void> zzm(int i, Bundle bundle) {
        return zza(new zzs(zzaws(), 2, bundle));
    }
}
