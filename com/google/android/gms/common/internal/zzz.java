package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.zzc;
import com.google.android.gms.common.zzf;
import com.google.android.gms.internal.zzbgl;
import com.google.android.gms.internal.zzbgo;

public final class zzz extends zzbgl {
    public static final Parcelable.Creator<zzz> CREATOR = new zzaa();
    private int version;
    private int zzggb;
    private int zzggc;
    String zzggd;
    IBinder zzgge;
    Scope[] zzggf;
    Bundle zzggg;
    Account zzggh;
    zzc[] zzggi;

    public zzz(int i) {
        this.version = 3;
        this.zzggc = zzf.GOOGLE_PLAY_SERVICES_VERSION_CODE;
        this.zzggb = i;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: android.accounts.Account} */
    /* JADX WARNING: type inference failed for: r1v1 */
    /* JADX WARNING: type inference failed for: r1v3, types: [com.google.android.gms.common.internal.zzan] */
    /* JADX WARNING: type inference failed for: r1v9 */
    /* JADX WARNING: type inference failed for: r1v10 */
    /* JADX WARNING: type inference failed for: r1v11 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    zzz(int r1, int r2, int r3, java.lang.String r4, android.os.IBinder r5, com.google.android.gms.common.api.Scope[] r6, android.os.Bundle r7, android.accounts.Account r8, com.google.android.gms.common.zzc[] r9) {
        /*
            r0 = this;
            r0.<init>()
            r0.version = r1
            r0.zzggb = r2
            r0.zzggc = r3
            java.lang.String r2 = "com.google.android.gms"
            boolean r3 = r2.equals(r4)
            if (r3 == 0) goto L_0x0014
            r0.zzggd = r2
            goto L_0x0016
        L_0x0014:
            r0.zzggd = r4
        L_0x0016:
            r2 = 2
            if (r1 >= r2) goto L_0x0038
            r1 = 0
            if (r5 == 0) goto L_0x0035
            if (r5 != 0) goto L_0x001f
            goto L_0x0031
        L_0x001f:
            java.lang.String r1 = "com.google.android.gms.common.internal.IAccountAccessor"
            android.os.IInterface r1 = r5.queryLocalInterface(r1)
            boolean r2 = r1 instanceof com.google.android.gms.common.internal.zzan
            if (r2 == 0) goto L_0x002c
            com.google.android.gms.common.internal.zzan r1 = (com.google.android.gms.common.internal.zzan) r1
            goto L_0x0031
        L_0x002c:
            com.google.android.gms.common.internal.zzap r1 = new com.google.android.gms.common.internal.zzap
            r1.<init>(r5)
        L_0x0031:
            android.accounts.Account r1 = com.google.android.gms.common.internal.zza.zza(r1)
        L_0x0035:
            r0.zzggh = r1
            goto L_0x003c
        L_0x0038:
            r0.zzgge = r5
            r0.zzggh = r8
        L_0x003c:
            r0.zzggf = r6
            r0.zzggg = r7
            r0.zzggi = r9
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.internal.zzz.<init>(int, int, int, java.lang.String, android.os.IBinder, com.google.android.gms.common.api.Scope[], android.os.Bundle, android.accounts.Account, com.google.android.gms.common.zzc[]):void");
    }

    public final void writeToParcel(Parcel parcel, int i) {
        int zze = zzbgo.zze(parcel);
        zzbgo.zzc(parcel, 1, this.version);
        zzbgo.zzc(parcel, 2, this.zzggb);
        zzbgo.zzc(parcel, 3, this.zzggc);
        zzbgo.zza(parcel, 4, this.zzggd, false);
        zzbgo.zza(parcel, 5, this.zzgge, false);
        zzbgo.zza(parcel, 6, (T[]) this.zzggf, i, false);
        zzbgo.zza(parcel, 7, this.zzggg, false);
        zzbgo.zza(parcel, 8, (Parcelable) this.zzggh, i, false);
        zzbgo.zza(parcel, 10, (T[]) this.zzggi, i, false);
        zzbgo.zzai(parcel, zze);
    }
}
