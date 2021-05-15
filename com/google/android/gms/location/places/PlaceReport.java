package com.google.android.gms.location.places;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.ReflectedParcelable;
import com.google.android.gms.common.internal.zzbg;
import com.google.android.gms.common.internal.zzbi;
import com.google.android.gms.internal.zzbgl;
import com.google.android.gms.internal.zzbgo;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.util.Arrays;

public class PlaceReport extends zzbgl implements ReflectedParcelable {
    public static final Parcelable.Creator<PlaceReport> CREATOR = new zzl();
    private final String mTag;
    private final String zzdwr;
    private int zzehz;
    private final String zzivz;

    PlaceReport(int i, String str, String str2, String str3) {
        this.zzehz = i;
        this.zzivz = str;
        this.mTag = str2;
        this.zzdwr = str3;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.google.android.gms.location.places.PlaceReport create(java.lang.String r8, java.lang.String r9) {
        /*
            com.google.android.gms.common.internal.zzbq.checkNotNull(r8)
            com.google.android.gms.common.internal.zzbq.zzgv(r9)
            java.lang.String r0 = "unknown"
            com.google.android.gms.common.internal.zzbq.zzgv(r0)
            int r1 = r0.hashCode()
            r2 = 0
            r3 = 5
            r4 = 4
            r5 = 3
            r6 = 2
            r7 = 1
            switch(r1) {
                case -1436706272: goto L_0x004b;
                case -1194968642: goto L_0x0040;
                case -284840886: goto L_0x0038;
                case -262743844: goto L_0x002e;
                case 1164924125: goto L_0x0024;
                case 1287171955: goto L_0x001a;
                default: goto L_0x0019;
            }
        L_0x0019:
            goto L_0x0055
        L_0x001a:
            java.lang.String r1 = "inferredRadioSignals"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0055
            r1 = r5
            goto L_0x0056
        L_0x0024:
            java.lang.String r1 = "inferredSnappedToRoad"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0055
            r1 = r3
            goto L_0x0056
        L_0x002e:
            java.lang.String r1 = "inferredReverseGeocoding"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0055
            r1 = r4
            goto L_0x0056
        L_0x0038:
            boolean r1 = r0.equals(r0)
            if (r1 == 0) goto L_0x0055
            r1 = r2
            goto L_0x0056
        L_0x0040:
            java.lang.String r1 = "userReported"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0055
            r1 = r7
            goto L_0x0056
        L_0x004b:
            java.lang.String r1 = "inferredGeofencing"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0055
            r1 = r6
            goto L_0x0056
        L_0x0055:
            r1 = -1
        L_0x0056:
            if (r1 == 0) goto L_0x0063
            if (r1 == r7) goto L_0x0063
            if (r1 == r6) goto L_0x0063
            if (r1 == r5) goto L_0x0063
            if (r1 == r4) goto L_0x0063
            if (r1 == r3) goto L_0x0063
            goto L_0x0064
        L_0x0063:
            r2 = r7
        L_0x0064:
            java.lang.String r1 = "Invalid source"
            com.google.android.gms.common.internal.zzbq.checkArgument(r2, r1)
            com.google.android.gms.location.places.PlaceReport r1 = new com.google.android.gms.location.places.PlaceReport
            r1.<init>(r7, r8, r9, r0)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.location.places.PlaceReport.create(java.lang.String, java.lang.String):com.google.android.gms.location.places.PlaceReport");
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PlaceReport)) {
            return false;
        }
        PlaceReport placeReport = (PlaceReport) obj;
        return zzbg.equal(this.zzivz, placeReport.zzivz) && zzbg.equal(this.mTag, placeReport.mTag) && zzbg.equal(this.zzdwr, placeReport.zzdwr);
    }

    public String getPlaceId() {
        return this.zzivz;
    }

    public String getTag() {
        return this.mTag;
    }

    public int hashCode() {
        return Arrays.hashCode(new Object[]{this.zzivz, this.mTag, this.zzdwr});
    }

    public String toString() {
        zzbi zzx = zzbg.zzx(this);
        zzx.zzg("placeId", this.zzivz);
        zzx.zzg("tag", this.mTag);
        if (!NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(this.zzdwr)) {
            zzx.zzg("source", this.zzdwr);
        }
        return zzx.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        int zze = zzbgo.zze(parcel);
        zzbgo.zzc(parcel, 1, this.zzehz);
        zzbgo.zza(parcel, 2, getPlaceId(), false);
        zzbgo.zza(parcel, 3, getTag(), false);
        zzbgo.zza(parcel, 4, this.zzdwr, false);
        zzbgo.zzai(parcel, zze);
    }
}
