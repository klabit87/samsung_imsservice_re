package com.google.android.gms.internal;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.IObjectWrapper;

public final class zzcco extends zzev implements zzccm {
    zzcco(IBinder iBinder) {
        super(iBinder, "com.google.android.gms.flags.IFlagProvider");
    }

    public final boolean getBooleanFlagValue(String str, boolean z, int i) throws RemoteException {
        Parcel zzbc = zzbc();
        zzbc.writeString(str);
        zzex.zza(zzbc, z);
        zzbc.writeInt(i);
        Parcel zza = zza(2, zzbc);
        boolean zza2 = zzex.zza(zza);
        zza.recycle();
        return zza2;
    }

    public final int getIntFlagValue(String str, int i, int i2) throws RemoteException {
        Parcel zzbc = zzbc();
        zzbc.writeString(str);
        zzbc.writeInt(i);
        zzbc.writeInt(i2);
        Parcel zza = zza(3, zzbc);
        int readInt = zza.readInt();
        zza.recycle();
        return readInt;
    }

    public final long getLongFlagValue(String str, long j, int i) throws RemoteException {
        Parcel zzbc = zzbc();
        zzbc.writeString(str);
        zzbc.writeLong(j);
        zzbc.writeInt(i);
        Parcel zza = zza(4, zzbc);
        long readLong = zza.readLong();
        zza.recycle();
        return readLong;
    }

    public final String getStringFlagValue(String str, String str2, int i) throws RemoteException {
        Parcel zzbc = zzbc();
        zzbc.writeString(str);
        zzbc.writeString(str2);
        zzbc.writeInt(i);
        Parcel zza = zza(5, zzbc);
        String readString = zza.readString();
        zza.recycle();
        return readString;
    }

    public final void init(IObjectWrapper iObjectWrapper) throws RemoteException {
        Parcel zzbc = zzbc();
        zzex.zza(zzbc, (IInterface) iObjectWrapper);
        zzb(1, zzbc);
    }
}
