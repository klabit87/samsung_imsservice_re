package com.google.android.gms.common.internal;

import com.google.android.gms.common.ConnectionResult;

public final class zzm implements zzj {
    private /* synthetic */ zzd zzgfk;

    public zzm(zzd zzd) {
        this.zzgfk = zzd;
    }

    public final void zzf(ConnectionResult connectionResult) {
        if (connectionResult.isSuccess()) {
            zzd zzd = this.zzgfk;
            zzd.zza((zzan) null, zzd.zzaly());
        } else if (this.zzgfk.zzgfc != null) {
            this.zzgfk.zzgfc.onConnectionFailed(connectionResult);
        }
    }
}
