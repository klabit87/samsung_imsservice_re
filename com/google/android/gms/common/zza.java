package com.google.android.gms.common;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.google.android.gms.common.internal.zzbq;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class zza implements ServiceConnection {
    private boolean zzfqr = false;
    private final BlockingQueue<IBinder> zzfqs = new LinkedBlockingQueue();

    public final void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        this.zzfqs.add(iBinder);
    }

    public final void onServiceDisconnected(ComponentName componentName) {
    }

    public final IBinder zza(long j, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        zzbq.zzgw("BlockingServiceConnection.getServiceWithTimeout() called on main thread");
        if (!this.zzfqr) {
            this.zzfqr = true;
            IBinder poll = this.zzfqs.poll(10000, timeUnit);
            if (poll != null) {
                return poll;
            }
            throw new TimeoutException("Timed out waiting for the service connection");
        }
        throw new IllegalStateException("Cannot call get on this connection more than once");
    }

    public final IBinder zzahd() throws InterruptedException {
        zzbq.zzgw("BlockingServiceConnection.getService() called on main thread");
        if (!this.zzfqr) {
            this.zzfqr = true;
            return this.zzfqs.take();
        }
        throw new IllegalStateException("Cannot call get on this connection more than once");
    }
}
