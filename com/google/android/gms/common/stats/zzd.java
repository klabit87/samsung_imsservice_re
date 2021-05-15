package com.google.android.gms.common.stats;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.internal.zzbgm;
import java.util.ArrayList;

public final class zzd implements Parcelable.Creator<WakeLockEvent> {
    public final /* synthetic */ Object createFromParcel(Parcel parcel) {
        Parcel parcel2 = parcel;
        int zzd = zzbgm.zzd(parcel);
        long j = 0;
        long j2 = 0;
        long j3 = 0;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        String str = null;
        ArrayList<String> arrayList = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        float f = 0.0f;
        while (parcel.dataPosition() < zzd) {
            int readInt = parcel.readInt();
            switch (65535 & readInt) {
                case 1:
                    i = zzbgm.zzg(parcel2, readInt);
                    break;
                case 2:
                    j = zzbgm.zzi(parcel2, readInt);
                    break;
                case 4:
                    str = zzbgm.zzq(parcel2, readInt);
                    break;
                case 5:
                    i3 = zzbgm.zzg(parcel2, readInt);
                    break;
                case 6:
                    arrayList = zzbgm.zzac(parcel2, readInt);
                    break;
                case 8:
                    j2 = zzbgm.zzi(parcel2, readInt);
                    break;
                case 10:
                    str3 = zzbgm.zzq(parcel2, readInt);
                    break;
                case 11:
                    i2 = zzbgm.zzg(parcel2, readInt);
                    break;
                case 12:
                    str2 = zzbgm.zzq(parcel2, readInt);
                    break;
                case 13:
                    str4 = zzbgm.zzq(parcel2, readInt);
                    break;
                case 14:
                    i4 = zzbgm.zzg(parcel2, readInt);
                    break;
                case 15:
                    f = zzbgm.zzl(parcel2, readInt);
                    break;
                case 16:
                    j3 = zzbgm.zzi(parcel2, readInt);
                    break;
                case 17:
                    str5 = zzbgm.zzq(parcel2, readInt);
                    break;
                default:
                    zzbgm.zzb(parcel2, readInt);
                    break;
            }
        }
        zzbgm.zzaf(parcel2, zzd);
        return new WakeLockEvent(i, j, i2, str, i3, arrayList, str2, j2, i4, str3, str4, f, j3, str5);
    }

    public final /* synthetic */ Object[] newArray(int i) {
        return new WakeLockEvent[i];
    }
}
