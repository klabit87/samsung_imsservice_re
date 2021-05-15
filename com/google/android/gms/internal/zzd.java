package com.google.android.gms.internal;

import android.os.Process;
import java.util.concurrent.BlockingQueue;

public final class zzd extends Thread {
    private static final boolean DEBUG = zzaf.DEBUG;
    private final BlockingQueue<zzr<?>> zzh;
    /* access modifiers changed from: private */
    public final BlockingQueue<zzr<?>> zzi;
    private final zzb zzj;
    /* access modifiers changed from: private */
    public final zzaa zzk;
    private volatile boolean zzl = false;
    private final zzf zzm;

    public zzd(BlockingQueue<zzr<?>> blockingQueue, BlockingQueue<zzr<?>> blockingQueue2, zzb zzb, zzaa zzaa) {
        this.zzh = blockingQueue;
        this.zzi = blockingQueue2;
        this.zzj = zzb;
        this.zzk = zzaa;
        this.zzm = new zzf(this);
    }

    private final void processRequest() throws InterruptedException {
        zzr take = this.zzh.take();
        take.zzb("cache-queue-take");
        take.isCanceled();
        zzc zza = this.zzj.zza(take.getUrl());
        if (zza == null) {
            take.zzb("cache-miss");
            if (!this.zzm.zzb(take)) {
                this.zzi.put(take);
            }
        } else if (zza.zza()) {
            take.zzb("cache-hit-expired");
            take.zza(zza);
            if (!this.zzm.zzb(take)) {
                this.zzi.put(take);
            }
        } else {
            take.zzb("cache-hit");
            zzx zza2 = take.zza(new zzp(zza.data, zza.zzf));
            take.zzb("cache-hit-parsed");
            if (zza.zze < System.currentTimeMillis()) {
                take.zzb("cache-hit-refresh-needed");
                take.zza(zza);
                zza2.zzbi = true;
                if (!this.zzm.zzb(take)) {
                    this.zzk.zza(take, zza2, new zze(this, take));
                    return;
                }
            }
            this.zzk.zzb(take, zza2);
        }
    }

    public final void quit() {
        this.zzl = true;
        interrupt();
    }

    public final void run() {
        if (DEBUG) {
            zzaf.zza("start new dispatcher", new Object[0]);
        }
        Process.setThreadPriority(10);
        this.zzj.initialize();
        while (true) {
            try {
                processRequest();
            } catch (InterruptedException e) {
                if (this.zzl) {
                    return;
                }
            }
        }
    }
}
