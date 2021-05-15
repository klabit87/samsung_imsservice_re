package com.google.android.gms.internal;

import android.os.RemoteException;

public final class zzcch extends zzcce<Integer> {
    public zzcch(int i, String str, Integer num) {
        super(0, str, num);
    }

    /* access modifiers changed from: private */
    /* renamed from: zzc */
    public final Integer zza(zzccm zzccm) {
        try {
            return Integer.valueOf(zzccm.getIntFlagValue(getKey(), ((Integer) zzje()).intValue(), getSource()));
        } catch (RemoteException e) {
            return (Integer) zzje();
        }
    }
}
