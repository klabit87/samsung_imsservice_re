package com.google.android.gms.common.internal;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;

public final class zzo extends zze {
    private /* synthetic */ zzd zzgfk;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public zzo(zzd zzd, int i, Bundle bundle) {
        super(zzd, i, (Bundle) null);
        this.zzgfk = zzd;
    }

    /* access modifiers changed from: protected */
    public final boolean zzama() {
        this.zzgfk.zzgew.zzf(ConnectionResult.zzfqt);
        return true;
    }

    /* access modifiers changed from: protected */
    public final void zzj(ConnectionResult connectionResult) {
        this.zzgfk.zzgew.zzf(connectionResult);
        this.zzgfk.onConnectionFailed(connectionResult);
    }
}
