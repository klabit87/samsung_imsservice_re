package com.google.android.gms.common.internal;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;

public final class zzn extends zze {
    private /* synthetic */ zzd zzgfk;
    private IBinder zzgfo;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public zzn(zzd zzd, int i, IBinder iBinder, Bundle bundle) {
        super(zzd, i, bundle);
        this.zzgfk = zzd;
        this.zzgfo = iBinder;
    }

    /* access modifiers changed from: protected */
    public final boolean zzama() {
        try {
            String interfaceDescriptor = this.zzgfo.getInterfaceDescriptor();
            if (!this.zzgfk.zzhn().equals(interfaceDescriptor)) {
                String zzhn = this.zzgfk.zzhn();
                StringBuilder sb = new StringBuilder(String.valueOf(zzhn).length() + 34 + String.valueOf(interfaceDescriptor).length());
                sb.append("service descriptor mismatch: ");
                sb.append(zzhn);
                sb.append(" vs. ");
                sb.append(interfaceDescriptor);
                Log.e("GmsClient", sb.toString());
                return false;
            }
            IInterface zzd = this.zzgfk.zzd(this.zzgfo);
            if (zzd == null || (!this.zzgfk.zza(2, 4, zzd) && !this.zzgfk.zza(3, 4, zzd))) {
                return false;
            }
            ConnectionResult unused = this.zzgfk.zzgff = null;
            Bundle zzagp = this.zzgfk.zzagp();
            if (this.zzgfk.zzgfb == null) {
                return true;
            }
            this.zzgfk.zzgfb.onConnected(zzagp);
            return true;
        } catch (RemoteException e) {
            Log.w("GmsClient", "service probably died");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public final void zzj(ConnectionResult connectionResult) {
        if (this.zzgfk.zzgfc != null) {
            this.zzgfk.zzgfc.onConnectionFailed(connectionResult);
        }
        this.zzgfk.onConnectionFailed(connectionResult);
    }
}
