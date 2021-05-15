package com.google.android.gms.internal;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.dynamic.zzn;
import com.google.android.gms.dynamite.DynamiteModule;
import com.google.android.gms.dynamite.descriptors.com.google.android.gms.flags.ModuleDescriptor;

public final class zzccl {
    private boolean zzarf = false;
    private zzccm zzhqe = null;

    public final void initialize(Context context) {
        synchronized (this) {
            if (!this.zzarf) {
                try {
                    zzccm asInterface = zzccn.asInterface(DynamiteModule.zza(context, DynamiteModule.zzhdn, ModuleDescriptor.MODULE_ID).zzhk("com.google.android.gms.flags.impl.FlagProviderImpl"));
                    this.zzhqe = asInterface;
                    asInterface.init(zzn.zzz(context));
                    this.zzarf = true;
                } catch (RemoteException | DynamiteModule.zzc e) {
                    Log.w("FlagValueProvider", "Failed to initialize flags module.", e);
                }
            }
        }
    }

    public final <T> T zzb(zzcce<T> zzcce) {
        synchronized (this) {
            if (this.zzarf) {
                return zzcce.zza(this.zzhqe);
            }
            T zzje = zzcce.zzje();
            return zzje;
        }
    }
}
