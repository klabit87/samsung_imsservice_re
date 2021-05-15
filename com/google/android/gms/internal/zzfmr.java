package com.google.android.gms.internal;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import java.io.IOException;
import java.util.Arrays;

public final class zzfmr extends zzflm<zzfmr> implements Cloneable {
    private String tag = "";
    private int zzaky = 0;
    private boolean zznet = false;
    private zzfmt zzorb = null;
    public long zzpyu = 0;
    public long zzpyv = 0;
    private long zzpyw = 0;
    private int zzpyx = 0;
    private zzfms[] zzpyy = zzfms.zzddf();
    private byte[] zzpyz = zzflv.zzpwe;
    private zzfmp zzpza = null;
    public byte[] zzpzb = zzflv.zzpwe;
    private String zzpzc = "";
    private String zzpzd = "";
    private zzfmo zzpze = null;
    private String zzpzf = "";
    public long zzpzg = 180000;
    private zzfmq zzpzh = null;
    public byte[] zzpzi = zzflv.zzpwe;
    private String zzpzj = "";
    private int zzpzk = 0;
    private int[] zzpzl = zzflv.zzpvy;
    private long zzpzm = 0;
    private boolean zzpzn = false;

    public zzfmr() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    /* access modifiers changed from: private */
    /* renamed from: zzbn */
    public final zzfmr zza(zzflj zzflj) throws IOException {
        zzfls zzfls;
        while (true) {
            int zzcxx = zzflj.zzcxx();
            switch (zzcxx) {
                case 0:
                    return this;
                case 8:
                    this.zzpyu = zzflj.zzcxz();
                    continue;
                case 18:
                    this.tag = zzflj.readString();
                    continue;
                case 26:
                    int zzb = zzflv.zzb(zzflj, 26);
                    zzfms[] zzfmsArr = this.zzpyy;
                    int length = zzfmsArr == null ? 0 : zzfmsArr.length;
                    int i = zzb + length;
                    zzfms[] zzfmsArr2 = new zzfms[i];
                    if (length != 0) {
                        System.arraycopy(this.zzpyy, 0, zzfmsArr2, 0, length);
                    }
                    while (length < i - 1) {
                        zzfmsArr2[length] = new zzfms();
                        zzflj.zza(zzfmsArr2[length]);
                        zzflj.zzcxx();
                        length++;
                    }
                    zzfmsArr2[length] = new zzfms();
                    zzflj.zza(zzfmsArr2[length]);
                    this.zzpyy = zzfmsArr2;
                    continue;
                case 34:
                    this.zzpyz = zzflj.readBytes();
                    continue;
                case 50:
                    this.zzpzb = zzflj.readBytes();
                    continue;
                case MNO.TELSTRA /*58*/:
                    if (this.zzpze == null) {
                        this.zzpze = new zzfmo();
                    }
                    zzfls = this.zzpze;
                    break;
                case 66:
                    this.zzpzc = zzflj.readString();
                    continue;
                case 74:
                    if (this.zzpza == null) {
                        this.zzpza = new zzfmp();
                    }
                    zzfls = this.zzpza;
                    break;
                case 80:
                    this.zznet = zzflj.zzcyd();
                    continue;
                case MNO.MOVISTAR_ARGENTINA /*88*/:
                    this.zzpyx = zzflj.zzcya();
                    continue;
                case 96:
                    this.zzaky = zzflj.zzcya();
                    continue;
                case 106:
                    this.zzpzd = zzflj.readString();
                    continue;
                case 114:
                    this.zzpzf = zzflj.readString();
                    continue;
                case 120:
                    this.zzpzg = zzflj.zzcyl();
                    continue;
                case 130:
                    if (this.zzpzh == null) {
                        this.zzpzh = new zzfmq();
                    }
                    zzfls = this.zzpzh;
                    break;
                case 136:
                    this.zzpyv = zzflj.zzcxz();
                    continue;
                case 146:
                    this.zzpzi = zzflj.readBytes();
                    continue;
                case 152:
                    try {
                        int zzcya = zzflj.zzcya();
                        if (!(zzcya == 0 || zzcya == 1)) {
                            if (zzcya != 2) {
                                StringBuilder sb = new StringBuilder(45);
                                sb.append(zzcya);
                                sb.append(" is not a valid enum InternalEvent");
                                throw new IllegalArgumentException(sb.toString());
                            }
                        }
                        this.zzpzk = zzcya;
                        continue;
                    } catch (IllegalArgumentException e) {
                        zzflj.zzmw(zzflj.getPosition());
                        zza(zzflj, zzcxx);
                        break;
                    }
                case MNO.UMOBILE /*160*/:
                    int zzb2 = zzflv.zzb(zzflj, MNO.UMOBILE);
                    int[] iArr = this.zzpzl;
                    int length2 = iArr == null ? 0 : iArr.length;
                    int i2 = zzb2 + length2;
                    int[] iArr2 = new int[i2];
                    if (length2 != 0) {
                        System.arraycopy(this.zzpzl, 0, iArr2, 0, length2);
                    }
                    while (length2 < i2 - 1) {
                        iArr2[length2] = zzflj.zzcya();
                        zzflj.zzcxx();
                        length2++;
                    }
                    iArr2[length2] = zzflj.zzcya();
                    this.zzpzl = iArr2;
                    continue;
                case MNO.TMOBILE_ROMANIA /*162*/:
                    int zzli = zzflj.zzli(zzflj.zzcym());
                    int position = zzflj.getPosition();
                    int i3 = 0;
                    while (zzflj.zzcyo() > 0) {
                        zzflj.zzcya();
                        i3++;
                    }
                    zzflj.zzmw(position);
                    int[] iArr3 = this.zzpzl;
                    int length3 = iArr3 == null ? 0 : iArr3.length;
                    int i4 = i3 + length3;
                    int[] iArr4 = new int[i4];
                    if (length3 != 0) {
                        System.arraycopy(this.zzpzl, 0, iArr4, 0, length3);
                    }
                    while (length3 < i4) {
                        iArr4[length3] = zzflj.zzcya();
                        length3++;
                    }
                    this.zzpzl = iArr4;
                    zzflj.zzlj(zzli);
                    continue;
                case MNO.TELIA_FI /*168*/:
                    this.zzpyw = zzflj.zzcxz();
                    continue;
                case MNO.ORANGE_SENEGAL /*176*/:
                    this.zzpzm = zzflj.zzcxz();
                    continue;
                case MNO.MTN_IRAN /*186*/:
                    if (this.zzorb == null) {
                        this.zzorb = new zzfmt();
                    }
                    zzfls = this.zzorb;
                    break;
                case MNO.KOODO /*194*/:
                    this.zzpzj = zzflj.readString();
                    continue;
                case 200:
                    this.zzpzn = zzflj.zzcyd();
                    continue;
                default:
                    if (!super.zza(zzflj, zzcxx)) {
                        return this;
                    }
                    continue;
            }
            zzflj.zza(zzfls);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: zzdde */
    public final zzfmr clone() {
        try {
            zzfmr zzfmr = (zzfmr) super.clone();
            zzfms[] zzfmsArr = this.zzpyy;
            if (zzfmsArr != null && zzfmsArr.length > 0) {
                zzfmr.zzpyy = new zzfms[zzfmsArr.length];
                int i = 0;
                while (true) {
                    zzfms[] zzfmsArr2 = this.zzpyy;
                    if (i >= zzfmsArr2.length) {
                        break;
                    }
                    if (zzfmsArr2[i] != null) {
                        zzfmr.zzpyy[i] = (zzfms) zzfmsArr2[i].clone();
                    }
                    i++;
                }
            }
            zzfmp zzfmp = this.zzpza;
            if (zzfmp != null) {
                zzfmr.zzpza = (zzfmp) zzfmp.clone();
            }
            zzfmo zzfmo = this.zzpze;
            if (zzfmo != null) {
                zzfmr.zzpze = (zzfmo) zzfmo.clone();
            }
            zzfmq zzfmq = this.zzpzh;
            if (zzfmq != null) {
                zzfmr.zzpzh = (zzfmq) zzfmq.clone();
            }
            int[] iArr = this.zzpzl;
            if (iArr != null && iArr.length > 0) {
                zzfmr.zzpzl = (int[]) iArr.clone();
            }
            zzfmt zzfmt = this.zzorb;
            if (zzfmt != null) {
                zzfmr.zzorb = (zzfmt) zzfmt.clone();
            }
            return zzfmr;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzfmr)) {
            return false;
        }
        zzfmr zzfmr = (zzfmr) obj;
        if (this.zzpyu != zzfmr.zzpyu || this.zzpyv != zzfmr.zzpyv || this.zzpyw != zzfmr.zzpyw) {
            return false;
        }
        String str = this.tag;
        if (str == null) {
            if (zzfmr.tag != null) {
                return false;
            }
        } else if (!str.equals(zzfmr.tag)) {
            return false;
        }
        if (this.zzpyx != zzfmr.zzpyx || this.zzaky != zzfmr.zzaky || this.zznet != zzfmr.zznet || !zzflq.equals((Object[]) this.zzpyy, (Object[]) zzfmr.zzpyy) || !Arrays.equals(this.zzpyz, zzfmr.zzpyz)) {
            return false;
        }
        zzfmp zzfmp = this.zzpza;
        if (zzfmp == null) {
            if (zzfmr.zzpza != null) {
                return false;
            }
        } else if (!zzfmp.equals(zzfmr.zzpza)) {
            return false;
        }
        if (!Arrays.equals(this.zzpzb, zzfmr.zzpzb)) {
            return false;
        }
        String str2 = this.zzpzc;
        if (str2 == null) {
            if (zzfmr.zzpzc != null) {
                return false;
            }
        } else if (!str2.equals(zzfmr.zzpzc)) {
            return false;
        }
        String str3 = this.zzpzd;
        if (str3 == null) {
            if (zzfmr.zzpzd != null) {
                return false;
            }
        } else if (!str3.equals(zzfmr.zzpzd)) {
            return false;
        }
        zzfmo zzfmo = this.zzpze;
        if (zzfmo == null) {
            if (zzfmr.zzpze != null) {
                return false;
            }
        } else if (!zzfmo.equals(zzfmr.zzpze)) {
            return false;
        }
        String str4 = this.zzpzf;
        if (str4 == null) {
            if (zzfmr.zzpzf != null) {
                return false;
            }
        } else if (!str4.equals(zzfmr.zzpzf)) {
            return false;
        }
        if (this.zzpzg != zzfmr.zzpzg) {
            return false;
        }
        zzfmq zzfmq = this.zzpzh;
        if (zzfmq == null) {
            if (zzfmr.zzpzh != null) {
                return false;
            }
        } else if (!zzfmq.equals(zzfmr.zzpzh)) {
            return false;
        }
        if (!Arrays.equals(this.zzpzi, zzfmr.zzpzi)) {
            return false;
        }
        String str5 = this.zzpzj;
        if (str5 == null) {
            if (zzfmr.zzpzj != null) {
                return false;
            }
        } else if (!str5.equals(zzfmr.zzpzj)) {
            return false;
        }
        if (this.zzpzk != zzfmr.zzpzk || !zzflq.equals(this.zzpzl, zzfmr.zzpzl) || this.zzpzm != zzfmr.zzpzm) {
            return false;
        }
        zzfmt zzfmt = this.zzorb;
        if (zzfmt == null) {
            if (zzfmr.zzorb != null) {
                return false;
            }
        } else if (!zzfmt.equals(zzfmr.zzorb)) {
            return false;
        }
        if (this.zzpzn != zzfmr.zzpzn) {
            return false;
        }
        return (this.zzpvl == null || this.zzpvl.isEmpty()) ? zzfmr.zzpvl == null || zzfmr.zzpvl.isEmpty() : this.zzpvl.equals(zzfmr.zzpvl);
    }

    public final int hashCode() {
        long j = this.zzpyu;
        long j2 = this.zzpyv;
        long j3 = this.zzpyw;
        int hashCode = (((((((getClass().getName().hashCode() + 527) * 31) + ((int) (j ^ (j >>> 32)))) * 31) + ((int) (j2 ^ (j2 >>> 32)))) * 31) + ((int) (j3 ^ (j3 >>> 32)))) * 31;
        String str = this.tag;
        int i = 0;
        int i2 = 1231;
        int hashCode2 = ((((((((((hashCode + (str == null ? 0 : str.hashCode())) * 31) + this.zzpyx) * 31) + this.zzaky) * 31) + (this.zznet ? 1231 : 1237)) * 31) + zzflq.hashCode((Object[]) this.zzpyy)) * 31) + Arrays.hashCode(this.zzpyz);
        zzfmp zzfmp = this.zzpza;
        int hashCode3 = ((((hashCode2 * 31) + (zzfmp == null ? 0 : zzfmp.hashCode())) * 31) + Arrays.hashCode(this.zzpzb)) * 31;
        String str2 = this.zzpzc;
        int hashCode4 = (hashCode3 + (str2 == null ? 0 : str2.hashCode())) * 31;
        String str3 = this.zzpzd;
        int hashCode5 = hashCode4 + (str3 == null ? 0 : str3.hashCode());
        zzfmo zzfmo = this.zzpze;
        int hashCode6 = ((hashCode5 * 31) + (zzfmo == null ? 0 : zzfmo.hashCode())) * 31;
        String str4 = this.zzpzf;
        int hashCode7 = str4 == null ? 0 : str4.hashCode();
        long j4 = this.zzpzg;
        int i3 = ((hashCode6 + hashCode7) * 31) + ((int) (j4 ^ (j4 >>> 32)));
        zzfmq zzfmq = this.zzpzh;
        int hashCode8 = ((((i3 * 31) + (zzfmq == null ? 0 : zzfmq.hashCode())) * 31) + Arrays.hashCode(this.zzpzi)) * 31;
        String str5 = this.zzpzj;
        int hashCode9 = str5 == null ? 0 : str5.hashCode();
        long j5 = this.zzpzm;
        int hashCode10 = ((((((hashCode8 + hashCode9) * 31) + this.zzpzk) * 31) + zzflq.hashCode(this.zzpzl)) * 31) + ((int) (j5 ^ (j5 >>> 32)));
        zzfmt zzfmt = this.zzorb;
        int hashCode11 = ((hashCode10 * 31) + (zzfmt == null ? 0 : zzfmt.hashCode())) * 31;
        if (!this.zzpzn) {
            i2 = 1237;
        }
        int i4 = (hashCode11 + i2) * 31;
        if (this.zzpvl != null && !this.zzpvl.isEmpty()) {
            i = this.zzpvl.hashCode();
        }
        return i4 + i;
    }

    public final void zza(zzflk zzflk) throws IOException {
        long j = this.zzpyu;
        if (j != 0) {
            zzflk.zzf(1, j);
        }
        String str = this.tag;
        if (str != null && !str.equals("")) {
            zzflk.zzp(2, this.tag);
        }
        zzfms[] zzfmsArr = this.zzpyy;
        int i = 0;
        if (zzfmsArr != null && zzfmsArr.length > 0) {
            int i2 = 0;
            while (true) {
                zzfms[] zzfmsArr2 = this.zzpyy;
                if (i2 >= zzfmsArr2.length) {
                    break;
                }
                zzfms zzfms = zzfmsArr2[i2];
                if (zzfms != null) {
                    zzflk.zza(3, (zzfls) zzfms);
                }
                i2++;
            }
        }
        if (!Arrays.equals(this.zzpyz, zzflv.zzpwe)) {
            zzflk.zzc(4, this.zzpyz);
        }
        if (!Arrays.equals(this.zzpzb, zzflv.zzpwe)) {
            zzflk.zzc(6, this.zzpzb);
        }
        zzfmo zzfmo = this.zzpze;
        if (zzfmo != null) {
            zzflk.zza(7, (zzfls) zzfmo);
        }
        String str2 = this.zzpzc;
        if (str2 != null && !str2.equals("")) {
            zzflk.zzp(8, this.zzpzc);
        }
        zzfmp zzfmp = this.zzpza;
        if (zzfmp != null) {
            zzflk.zza(9, (zzfls) zzfmp);
        }
        boolean z = this.zznet;
        if (z) {
            zzflk.zzl(10, z);
        }
        int i3 = this.zzpyx;
        if (i3 != 0) {
            zzflk.zzad(11, i3);
        }
        int i4 = this.zzaky;
        if (i4 != 0) {
            zzflk.zzad(12, i4);
        }
        String str3 = this.zzpzd;
        if (str3 != null && !str3.equals("")) {
            zzflk.zzp(13, this.zzpzd);
        }
        String str4 = this.zzpzf;
        if (str4 != null && !str4.equals("")) {
            zzflk.zzp(14, this.zzpzf);
        }
        long j2 = this.zzpzg;
        if (j2 != 180000) {
            zzflk.zzg(15, j2);
        }
        zzfmq zzfmq = this.zzpzh;
        if (zzfmq != null) {
            zzflk.zza(16, (zzfls) zzfmq);
        }
        long j3 = this.zzpyv;
        if (j3 != 0) {
            zzflk.zzf(17, j3);
        }
        if (!Arrays.equals(this.zzpzi, zzflv.zzpwe)) {
            zzflk.zzc(18, this.zzpzi);
        }
        int i5 = this.zzpzk;
        if (i5 != 0) {
            zzflk.zzad(19, i5);
        }
        int[] iArr = this.zzpzl;
        if (iArr != null && iArr.length > 0) {
            while (true) {
                int[] iArr2 = this.zzpzl;
                if (i >= iArr2.length) {
                    break;
                }
                zzflk.zzad(20, iArr2[i]);
                i++;
            }
        }
        long j4 = this.zzpyw;
        if (j4 != 0) {
            zzflk.zzf(21, j4);
        }
        long j5 = this.zzpzm;
        if (j5 != 0) {
            zzflk.zzf(22, j5);
        }
        zzfmt zzfmt = this.zzorb;
        if (zzfmt != null) {
            zzflk.zza(23, (zzfls) zzfmt);
        }
        String str5 = this.zzpzj;
        if (str5 != null && !str5.equals("")) {
            zzflk.zzp(24, this.zzpzj);
        }
        boolean z2 = this.zzpzn;
        if (z2) {
            zzflk.zzl(25, z2);
        }
        super.zza(zzflk);
    }

    public final /* synthetic */ zzflm zzdck() throws CloneNotSupportedException {
        return (zzfmr) clone();
    }

    public final /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzfmr) clone();
    }

    /* access modifiers changed from: protected */
    public final int zzq() {
        int[] iArr;
        int zzq = super.zzq();
        long j = this.zzpyu;
        if (j != 0) {
            zzq += zzflk.zzc(1, j);
        }
        String str = this.tag;
        if (str != null && !str.equals("")) {
            zzq += zzflk.zzq(2, this.tag);
        }
        zzfms[] zzfmsArr = this.zzpyy;
        int i = 0;
        if (zzfmsArr != null && zzfmsArr.length > 0) {
            int i2 = 0;
            while (true) {
                zzfms[] zzfmsArr2 = this.zzpyy;
                if (i2 >= zzfmsArr2.length) {
                    break;
                }
                zzfms zzfms = zzfmsArr2[i2];
                if (zzfms != null) {
                    zzq += zzflk.zzb(3, (zzfls) zzfms);
                }
                i2++;
            }
        }
        if (!Arrays.equals(this.zzpyz, zzflv.zzpwe)) {
            zzq += zzflk.zzd(4, this.zzpyz);
        }
        if (!Arrays.equals(this.zzpzb, zzflv.zzpwe)) {
            zzq += zzflk.zzd(6, this.zzpzb);
        }
        zzfmo zzfmo = this.zzpze;
        if (zzfmo != null) {
            zzq += zzflk.zzb(7, (zzfls) zzfmo);
        }
        String str2 = this.zzpzc;
        if (str2 != null && !str2.equals("")) {
            zzq += zzflk.zzq(8, this.zzpzc);
        }
        zzfmp zzfmp = this.zzpza;
        if (zzfmp != null) {
            zzq += zzflk.zzb(9, (zzfls) zzfmp);
        }
        if (this.zznet) {
            zzq += zzflk.zzlw(10) + 1;
        }
        int i3 = this.zzpyx;
        if (i3 != 0) {
            zzq += zzflk.zzag(11, i3);
        }
        int i4 = this.zzaky;
        if (i4 != 0) {
            zzq += zzflk.zzag(12, i4);
        }
        String str3 = this.zzpzd;
        if (str3 != null && !str3.equals("")) {
            zzq += zzflk.zzq(13, this.zzpzd);
        }
        String str4 = this.zzpzf;
        if (str4 != null && !str4.equals("")) {
            zzq += zzflk.zzq(14, this.zzpzf);
        }
        long j2 = this.zzpzg;
        if (j2 != 180000) {
            zzq += zzflk.zzh(15, j2);
        }
        zzfmq zzfmq = this.zzpzh;
        if (zzfmq != null) {
            zzq += zzflk.zzb(16, (zzfls) zzfmq);
        }
        long j3 = this.zzpyv;
        if (j3 != 0) {
            zzq += zzflk.zzc(17, j3);
        }
        if (!Arrays.equals(this.zzpzi, zzflv.zzpwe)) {
            zzq += zzflk.zzd(18, this.zzpzi);
        }
        int i5 = this.zzpzk;
        if (i5 != 0) {
            zzq += zzflk.zzag(19, i5);
        }
        int[] iArr2 = this.zzpzl;
        if (iArr2 != null && iArr2.length > 0) {
            int i6 = 0;
            while (true) {
                iArr = this.zzpzl;
                if (i >= iArr.length) {
                    break;
                }
                i6 += zzflk.zzlx(iArr[i]);
                i++;
            }
            zzq = zzq + i6 + (iArr.length * 2);
        }
        long j4 = this.zzpyw;
        if (j4 != 0) {
            zzq += zzflk.zzc(21, j4);
        }
        long j5 = this.zzpzm;
        if (j5 != 0) {
            zzq += zzflk.zzc(22, j5);
        }
        zzfmt zzfmt = this.zzorb;
        if (zzfmt != null) {
            zzq += zzflk.zzb(23, (zzfls) zzfmt);
        }
        String str5 = this.zzpzj;
        if (str5 != null && !str5.equals("")) {
            zzq += zzflk.zzq(24, this.zzpzj);
        }
        return this.zzpzn ? zzq + zzflk.zzlw(25) + 1 : zzq;
    }
}
