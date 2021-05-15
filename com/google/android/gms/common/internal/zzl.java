package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

public final class zzl implements ServiceConnection {
    private /* synthetic */ zzd zzgfk;
    private final int zzgfn;

    public zzl(zzd zzd, int i) {
        this.zzgfk = zzd;
        this.zzgfn = i;
    }

    public final void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        zzay zzay;
        zzd zzd = this.zzgfk;
        if (iBinder == null) {
            zzd.zzce(16);
            return;
        }
        synchronized (zzd.zzgeu) {
            zzd zzd2 = this.zzgfk;
            if (iBinder == null) {
                zzay = null;
            } else {
                IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.common.internal.IGmsServiceBroker");
                zzay = (queryLocalInterface == null || !(queryLocalInterface instanceof zzay)) ? new zzaz(iBinder) : (zzay) queryLocalInterface;
            }
            zzay unused = zzd2.zzgev = zzay;
        }
        this.zzgfk.zza(0, (Bundle) null, this.zzgfn);
    }

    public final void onServiceDisconnected(ComponentName componentName) {
        synchronized (this.zzgfk.zzgeu) {
            zzay unused = this.zzgfk.zzgev = null;
        }
        this.zzgfk.mHandler.sendMessage(this.zzgfk.mHandler.obtainMessage(6, this.zzgfn, 1));
    }
}
