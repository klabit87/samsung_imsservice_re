package com.google.android.gms.internal;

import android.os.RemoteException;

public final class zzccg extends zzcce<Boolean> {
    public zzccg(int i, String str, Boolean bool) {
        super(0, str, bool);
    }

    /* access modifiers changed from: private */
    /* renamed from: zzb */
    public final Boolean zza(zzccm zzccm) {
        try {
            return Boolean.valueOf(zzccm.getBooleanFlagValue(getKey(), ((Boolean) zzje()).booleanValue(), getSource()));
        } catch (RemoteException e) {
            return (Boolean) zzje();
        }
    }
}
