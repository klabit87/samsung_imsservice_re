package com.google.android.gms.common.internal;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public final class zzk extends zzax {
    private zzd zzgfm;
    private final int zzgfn;

    public zzk(zzd zzd, int i) {
        this.zzgfm = zzd;
        this.zzgfn = i;
    }

    public final void zza(int i, Bundle bundle) {
        Log.wtf("GmsClient", "received deprecated onAccountValidationComplete callback, ignoring", new Exception());
    }

    public final void zza(int i, IBinder iBinder, Bundle bundle) {
        zzbq.checkNotNull(this.zzgfm, "onPostInitComplete can be called only once per call to getRemoteService");
        this.zzgfm.zza(i, iBinder, bundle, this.zzgfn);
        this.zzgfm = null;
    }
}
