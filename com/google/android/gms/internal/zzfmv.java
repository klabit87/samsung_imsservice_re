package com.google.android.gms.internal;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import java.io.IOException;

public final class zzfmv extends zzflm<zzfmv> {
    public long zzgoc = 0;
    public String zzpzs = "";
    public String zzpzt = "";
    public long zzpzu = 0;
    public String zzpzv = "";
    public long zzpzw = 0;
    public String zzpzx = "";
    public String zzpzy = "";
    public String zzpzz = "";
    public String zzqaa = "";
    public String zzqab = "";
    public int zzqac = 0;
    public zzfmu[] zzqad = zzfmu.zzddi();

    public zzfmv() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    public static zzfmv zzbi(byte[] bArr) throws zzflr {
        return (zzfmv) zzfls.zza(new zzfmv(), bArr);
    }

    public final /* synthetic */ zzfls zza(zzflj zzflj) throws IOException {
        while (true) {
            int zzcxx = zzflj.zzcxx();
            switch (zzcxx) {
                case 0:
                    return this;
                case 10:
                    this.zzpzs = zzflj.readString();
                    break;
                case 18:
                    this.zzpzt = zzflj.readString();
                    break;
                case 24:
                    this.zzpzu = zzflj.zzcxz();
                    break;
                case 34:
                    this.zzpzv = zzflj.readString();
                    break;
                case 40:
                    this.zzpzw = zzflj.zzcxz();
                    break;
                case 48:
                    this.zzgoc = zzflj.zzcxz();
                    break;
                case MNO.TELSTRA /*58*/:
                    this.zzpzx = zzflj.readString();
                    break;
                case 66:
                    this.zzpzy = zzflj.readString();
                    break;
                case 74:
                    this.zzpzz = zzflj.readString();
                    break;
                case 82:
                    this.zzqaa = zzflj.readString();
                    break;
                case MNO.DLOG /*90*/:
                    this.zzqab = zzflj.readString();
                    break;
                case 96:
                    this.zzqac = zzflj.zzcya();
                    break;
                case 106:
                    int zzb = zzflv.zzb(zzflj, 106);
                    zzfmu[] zzfmuArr = this.zzqad;
                    int length = zzfmuArr == null ? 0 : zzfmuArr.length;
                    int i = zzb + length;
                    zzfmu[] zzfmuArr2 = new zzfmu[i];
                    if (length != 0) {
                        System.arraycopy(this.zzqad, 0, zzfmuArr2, 0, length);
                    }
                    while (length < i - 1) {
                        zzfmuArr2[length] = new zzfmu();
                        zzflj.zza(zzfmuArr2[length]);
                        zzflj.zzcxx();
                        length++;
                    }
                    zzfmuArr2[length] = new zzfmu();
                    zzflj.zza(zzfmuArr2[length]);
                    this.zzqad = zzfmuArr2;
                    break;
                default:
                    if (super.zza(zzflj, zzcxx)) {
                        break;
                    } else {
                        return this;
                    }
            }
        }
    }

    public final void zza(zzflk zzflk) throws IOException {
        String str = this.zzpzs;
        if (str != null && !str.equals("")) {
            zzflk.zzp(1, this.zzpzs);
        }
        String str2 = this.zzpzt;
        if (str2 != null && !str2.equals("")) {
            zzflk.zzp(2, this.zzpzt);
        }
        long j = this.zzpzu;
        if (j != 0) {
            zzflk.zzf(3, j);
        }
        String str3 = this.zzpzv;
        if (str3 != null && !str3.equals("")) {
            zzflk.zzp(4, this.zzpzv);
        }
        long j2 = this.zzpzw;
        if (j2 != 0) {
            zzflk.zzf(5, j2);
        }
        long j3 = this.zzgoc;
        if (j3 != 0) {
            zzflk.zzf(6, j3);
        }
        String str4 = this.zzpzx;
        if (str4 != null && !str4.equals("")) {
            zzflk.zzp(7, this.zzpzx);
        }
        String str5 = this.zzpzy;
        if (str5 != null && !str5.equals("")) {
            zzflk.zzp(8, this.zzpzy);
        }
        String str6 = this.zzpzz;
        if (str6 != null && !str6.equals("")) {
            zzflk.zzp(9, this.zzpzz);
        }
        String str7 = this.zzqaa;
        if (str7 != null && !str7.equals("")) {
            zzflk.zzp(10, this.zzqaa);
        }
        String str8 = this.zzqab;
        if (str8 != null && !str8.equals("")) {
            zzflk.zzp(11, this.zzqab);
        }
        int i = this.zzqac;
        if (i != 0) {
            zzflk.zzad(12, i);
        }
        zzfmu[] zzfmuArr = this.zzqad;
        if (zzfmuArr != null && zzfmuArr.length > 0) {
            int i2 = 0;
            while (true) {
                zzfmu[] zzfmuArr2 = this.zzqad;
                if (i2 >= zzfmuArr2.length) {
                    break;
                }
                zzfmu zzfmu = zzfmuArr2[i2];
                if (zzfmu != null) {
                    zzflk.zza(13, (zzfls) zzfmu);
                }
                i2++;
            }
        }
        super.zza(zzflk);
    }

    /* access modifiers changed from: protected */
    public final int zzq() {
        int zzq = super.zzq();
        String str = this.zzpzs;
        if (str != null && !str.equals("")) {
            zzq += zzflk.zzq(1, this.zzpzs);
        }
        String str2 = this.zzpzt;
        if (str2 != null && !str2.equals("")) {
            zzq += zzflk.zzq(2, this.zzpzt);
        }
        long j = this.zzpzu;
        if (j != 0) {
            zzq += zzflk.zzc(3, j);
        }
        String str3 = this.zzpzv;
        if (str3 != null && !str3.equals("")) {
            zzq += zzflk.zzq(4, this.zzpzv);
        }
        long j2 = this.zzpzw;
        if (j2 != 0) {
            zzq += zzflk.zzc(5, j2);
        }
        long j3 = this.zzgoc;
        if (j3 != 0) {
            zzq += zzflk.zzc(6, j3);
        }
        String str4 = this.zzpzx;
        if (str4 != null && !str4.equals("")) {
            zzq += zzflk.zzq(7, this.zzpzx);
        }
        String str5 = this.zzpzy;
        if (str5 != null && !str5.equals("")) {
            zzq += zzflk.zzq(8, this.zzpzy);
        }
        String str6 = this.zzpzz;
        if (str6 != null && !str6.equals("")) {
            zzq += zzflk.zzq(9, this.zzpzz);
        }
        String str7 = this.zzqaa;
        if (str7 != null && !str7.equals("")) {
            zzq += zzflk.zzq(10, this.zzqaa);
        }
        String str8 = this.zzqab;
        if (str8 != null && !str8.equals("")) {
            zzq += zzflk.zzq(11, this.zzqab);
        }
        int i = this.zzqac;
        if (i != 0) {
            zzq += zzflk.zzag(12, i);
        }
        zzfmu[] zzfmuArr = this.zzqad;
        if (zzfmuArr != null && zzfmuArr.length > 0) {
            int i2 = 0;
            while (true) {
                zzfmu[] zzfmuArr2 = this.zzqad;
                if (i2 >= zzfmuArr2.length) {
                    break;
                }
                zzfmu zzfmu = zzfmuArr2[i2];
                if (zzfmu != null) {
                    zzq += zzflk.zzb(13, (zzfls) zzfmu);
                }
                i2++;
            }
        }
        return zzq;
    }
}
