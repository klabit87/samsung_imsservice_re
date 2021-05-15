package com.google.android.gms.internal;

import java.io.IOException;

public final class zzfmo extends zzflm<zzfmo> implements Cloneable {
    private String[] zzpyk = zzflv.EMPTY_STRING_ARRAY;
    private String[] zzpyl = zzflv.EMPTY_STRING_ARRAY;
    private int[] zzpym = zzflv.zzpvy;
    private long[] zzpyn = zzflv.zzpvz;
    private long[] zzpyo = zzflv.zzpvz;

    public zzfmo() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    /* access modifiers changed from: private */
    /* renamed from: zzddb */
    public zzfmo clone() {
        try {
            zzfmo zzfmo = (zzfmo) super.clone();
            String[] strArr = this.zzpyk;
            if (strArr != null && strArr.length > 0) {
                zzfmo.zzpyk = (String[]) strArr.clone();
            }
            String[] strArr2 = this.zzpyl;
            if (strArr2 != null && strArr2.length > 0) {
                zzfmo.zzpyl = (String[]) strArr2.clone();
            }
            int[] iArr = this.zzpym;
            if (iArr != null && iArr.length > 0) {
                zzfmo.zzpym = (int[]) iArr.clone();
            }
            long[] jArr = this.zzpyn;
            if (jArr != null && jArr.length > 0) {
                zzfmo.zzpyn = (long[]) jArr.clone();
            }
            long[] jArr2 = this.zzpyo;
            if (jArr2 != null && jArr2.length > 0) {
                zzfmo.zzpyo = (long[]) jArr2.clone();
            }
            return zzfmo;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzfmo)) {
            return false;
        }
        zzfmo zzfmo = (zzfmo) obj;
        if (zzflq.equals((Object[]) this.zzpyk, (Object[]) zzfmo.zzpyk) && zzflq.equals((Object[]) this.zzpyl, (Object[]) zzfmo.zzpyl) && zzflq.equals(this.zzpym, zzfmo.zzpym) && zzflq.equals(this.zzpyn, zzfmo.zzpyn) && zzflq.equals(this.zzpyo, zzfmo.zzpyo)) {
            return (this.zzpvl == null || this.zzpvl.isEmpty()) ? zzfmo.zzpvl == null || zzfmo.zzpvl.isEmpty() : this.zzpvl.equals(zzfmo.zzpvl);
        }
        return false;
    }

    public final int hashCode() {
        return ((((((((((((getClass().getName().hashCode() + 527) * 31) + zzflq.hashCode((Object[]) this.zzpyk)) * 31) + zzflq.hashCode((Object[]) this.zzpyl)) * 31) + zzflq.hashCode(this.zzpym)) * 31) + zzflq.hashCode(this.zzpyn)) * 31) + zzflq.hashCode(this.zzpyo)) * 31) + ((this.zzpvl == null || this.zzpvl.isEmpty()) ? 0 : this.zzpvl.hashCode());
    }

    public final /* synthetic */ zzfls zza(zzflj zzflj) throws IOException {
        int i;
        long zzcxz;
        long zzcxz2;
        while (true) {
            int zzcxx = zzflj.zzcxx();
            if (zzcxx == 0) {
                return this;
            }
            if (zzcxx == 10) {
                int zzb = zzflv.zzb(zzflj, 10);
                String[] strArr = this.zzpyk;
                int length = strArr == null ? 0 : strArr.length;
                int i2 = zzb + length;
                String[] strArr2 = new String[i2];
                if (length != 0) {
                    System.arraycopy(this.zzpyk, 0, strArr2, 0, length);
                }
                while (length < i2 - 1) {
                    strArr2[length] = zzflj.readString();
                    zzflj.zzcxx();
                    length++;
                }
                strArr2[length] = zzflj.readString();
                this.zzpyk = strArr2;
            } else if (zzcxx == 18) {
                int zzb2 = zzflv.zzb(zzflj, 18);
                String[] strArr3 = this.zzpyl;
                int length2 = strArr3 == null ? 0 : strArr3.length;
                int i3 = zzb2 + length2;
                String[] strArr4 = new String[i3];
                if (length2 != 0) {
                    System.arraycopy(this.zzpyl, 0, strArr4, 0, length2);
                }
                while (length2 < i3 - 1) {
                    strArr4[length2] = zzflj.readString();
                    zzflj.zzcxx();
                    length2++;
                }
                strArr4[length2] = zzflj.readString();
                this.zzpyl = strArr4;
            } else if (zzcxx != 24) {
                if (zzcxx == 26) {
                    i = zzflj.zzli(zzflj.zzcym());
                    int position = zzflj.getPosition();
                    int i4 = 0;
                    while (zzflj.zzcyo() > 0) {
                        zzflj.zzcya();
                        i4++;
                    }
                    zzflj.zzmw(position);
                    int[] iArr = this.zzpym;
                    int length3 = iArr == null ? 0 : iArr.length;
                    int i5 = i4 + length3;
                    int[] iArr2 = new int[i5];
                    if (length3 != 0) {
                        System.arraycopy(this.zzpym, 0, iArr2, 0, length3);
                    }
                    while (length3 < i5) {
                        iArr2[length3] = zzflj.zzcya();
                        length3++;
                    }
                    this.zzpym = iArr2;
                } else if (zzcxx == 32) {
                    int zzb3 = zzflv.zzb(zzflj, 32);
                    long[] jArr = this.zzpyn;
                    int length4 = jArr == null ? 0 : jArr.length;
                    int i6 = zzb3 + length4;
                    long[] jArr2 = new long[i6];
                    if (length4 != 0) {
                        System.arraycopy(this.zzpyn, 0, jArr2, 0, length4);
                    }
                    while (true) {
                        int i7 = i6 - 1;
                        zzcxz = zzflj.zzcxz();
                        if (length4 >= i7) {
                            break;
                        }
                        jArr2[length4] = zzcxz;
                        zzflj.zzcxx();
                        length4++;
                    }
                    jArr2[length4] = zzcxz;
                    this.zzpyn = jArr2;
                } else if (zzcxx == 34) {
                    i = zzflj.zzli(zzflj.zzcym());
                    int position2 = zzflj.getPosition();
                    int i8 = 0;
                    while (zzflj.zzcyo() > 0) {
                        zzflj.zzcxz();
                        i8++;
                    }
                    zzflj.zzmw(position2);
                    long[] jArr3 = this.zzpyn;
                    int length5 = jArr3 == null ? 0 : jArr3.length;
                    int i9 = i8 + length5;
                    long[] jArr4 = new long[i9];
                    if (length5 != 0) {
                        System.arraycopy(this.zzpyn, 0, jArr4, 0, length5);
                    }
                    while (length5 < i9) {
                        jArr4[length5] = zzflj.zzcxz();
                        length5++;
                    }
                    this.zzpyn = jArr4;
                } else if (zzcxx == 40) {
                    int zzb4 = zzflv.zzb(zzflj, 40);
                    long[] jArr5 = this.zzpyo;
                    int length6 = jArr5 == null ? 0 : jArr5.length;
                    int i10 = zzb4 + length6;
                    long[] jArr6 = new long[i10];
                    if (length6 != 0) {
                        System.arraycopy(this.zzpyo, 0, jArr6, 0, length6);
                    }
                    while (true) {
                        int i11 = i10 - 1;
                        zzcxz2 = zzflj.zzcxz();
                        if (length6 >= i11) {
                            break;
                        }
                        jArr6[length6] = zzcxz2;
                        zzflj.zzcxx();
                        length6++;
                    }
                    jArr6[length6] = zzcxz2;
                    this.zzpyo = jArr6;
                } else if (zzcxx == 42) {
                    i = zzflj.zzli(zzflj.zzcym());
                    int position3 = zzflj.getPosition();
                    int i12 = 0;
                    while (zzflj.zzcyo() > 0) {
                        zzflj.zzcxz();
                        i12++;
                    }
                    zzflj.zzmw(position3);
                    long[] jArr7 = this.zzpyo;
                    int length7 = jArr7 == null ? 0 : jArr7.length;
                    int i13 = i12 + length7;
                    long[] jArr8 = new long[i13];
                    if (length7 != 0) {
                        System.arraycopy(this.zzpyo, 0, jArr8, 0, length7);
                    }
                    while (length7 < i13) {
                        jArr8[length7] = zzflj.zzcxz();
                        length7++;
                    }
                    this.zzpyo = jArr8;
                } else if (!super.zza(zzflj, zzcxx)) {
                    return this;
                }
                zzflj.zzlj(i);
            } else {
                int zzb5 = zzflv.zzb(zzflj, 24);
                int[] iArr3 = this.zzpym;
                int length8 = iArr3 == null ? 0 : iArr3.length;
                int i14 = zzb5 + length8;
                int[] iArr4 = new int[i14];
                if (length8 != 0) {
                    System.arraycopy(this.zzpym, 0, iArr4, 0, length8);
                }
                while (length8 < i14 - 1) {
                    iArr4[length8] = zzflj.zzcya();
                    zzflj.zzcxx();
                    length8++;
                }
                iArr4[length8] = zzflj.zzcya();
                this.zzpym = iArr4;
            }
        }
    }

    public final void zza(zzflk zzflk) throws IOException {
        String[] strArr = this.zzpyk;
        int i = 0;
        if (strArr != null && strArr.length > 0) {
            int i2 = 0;
            while (true) {
                String[] strArr2 = this.zzpyk;
                if (i2 >= strArr2.length) {
                    break;
                }
                String str = strArr2[i2];
                if (str != null) {
                    zzflk.zzp(1, str);
                }
                i2++;
            }
        }
        String[] strArr3 = this.zzpyl;
        if (strArr3 != null && strArr3.length > 0) {
            int i3 = 0;
            while (true) {
                String[] strArr4 = this.zzpyl;
                if (i3 >= strArr4.length) {
                    break;
                }
                String str2 = strArr4[i3];
                if (str2 != null) {
                    zzflk.zzp(2, str2);
                }
                i3++;
            }
        }
        int[] iArr = this.zzpym;
        if (iArr != null && iArr.length > 0) {
            int i4 = 0;
            while (true) {
                int[] iArr2 = this.zzpym;
                if (i4 >= iArr2.length) {
                    break;
                }
                zzflk.zzad(3, iArr2[i4]);
                i4++;
            }
        }
        long[] jArr = this.zzpyn;
        if (jArr != null && jArr.length > 0) {
            int i5 = 0;
            while (true) {
                long[] jArr2 = this.zzpyn;
                if (i5 >= jArr2.length) {
                    break;
                }
                zzflk.zzf(4, jArr2[i5]);
                i5++;
            }
        }
        long[] jArr3 = this.zzpyo;
        if (jArr3 != null && jArr3.length > 0) {
            while (true) {
                long[] jArr4 = this.zzpyo;
                if (i >= jArr4.length) {
                    break;
                }
                zzflk.zzf(5, jArr4[i]);
                i++;
            }
        }
        super.zza(zzflk);
    }

    public final /* synthetic */ zzflm zzdck() throws CloneNotSupportedException {
        return (zzfmo) clone();
    }

    public final /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzfmo) clone();
    }

    /* access modifiers changed from: protected */
    public final int zzq() {
        long[] jArr;
        int[] iArr;
        int zzq = super.zzq();
        String[] strArr = this.zzpyk;
        int i = 0;
        if (strArr != null && strArr.length > 0) {
            int i2 = 0;
            int i3 = 0;
            int i4 = 0;
            while (true) {
                String[] strArr2 = this.zzpyk;
                if (i2 >= strArr2.length) {
                    break;
                }
                String str = strArr2[i2];
                if (str != null) {
                    i4++;
                    i3 += zzflk.zztx(str);
                }
                i2++;
            }
            zzq = zzq + i3 + (i4 * 1);
        }
        String[] strArr3 = this.zzpyl;
        if (strArr3 != null && strArr3.length > 0) {
            int i5 = 0;
            int i6 = 0;
            int i7 = 0;
            while (true) {
                String[] strArr4 = this.zzpyl;
                if (i5 >= strArr4.length) {
                    break;
                }
                String str2 = strArr4[i5];
                if (str2 != null) {
                    i7++;
                    i6 += zzflk.zztx(str2);
                }
                i5++;
            }
            zzq = zzq + i6 + (i7 * 1);
        }
        int[] iArr2 = this.zzpym;
        if (iArr2 != null && iArr2.length > 0) {
            int i8 = 0;
            int i9 = 0;
            while (true) {
                iArr = this.zzpym;
                if (i8 >= iArr.length) {
                    break;
                }
                i9 += zzflk.zzlx(iArr[i8]);
                i8++;
            }
            zzq = zzq + i9 + (iArr.length * 1);
        }
        long[] jArr2 = this.zzpyn;
        if (jArr2 != null && jArr2.length > 0) {
            int i10 = 0;
            int i11 = 0;
            while (true) {
                jArr = this.zzpyn;
                if (i10 >= jArr.length) {
                    break;
                }
                i11 += zzflk.zzdj(jArr[i10]);
                i10++;
            }
            zzq = zzq + i11 + (jArr.length * 1);
        }
        long[] jArr3 = this.zzpyo;
        if (jArr3 == null || jArr3.length <= 0) {
            return zzq;
        }
        int i12 = 0;
        while (true) {
            long[] jArr4 = this.zzpyo;
            if (i >= jArr4.length) {
                return zzq + i12 + (jArr4.length * 1);
            }
            i12 += zzflk.zzdj(jArr4[i]);
            i++;
        }
    }
}
