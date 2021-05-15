package com.google.android.gms.internal;

import android.os.RemoteException;

public final class zzccj extends zzcce<String> {
    public zzccj(int i, String str, String str2) {
        super(0, str, str2);
    }

    /* access modifiers changed from: private */
    /* renamed from: zze */
    public final String zza(zzccm zzccm) {
        try {
            return zzccm.getStringFlagValue(getKey(), (String) zzje(), getSource());
        } catch (RemoteException e) {
            return (String) zzje();
        }
    }
}
