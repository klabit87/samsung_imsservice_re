package com.google.android.gms.internal;

import java.io.IOException;
import java.util.Arrays;

public final class zzfmq extends zzflm<zzfmq> implements Cloneable {
    private byte[] zzpyq = zzflv.zzpwe;
    private String zzpyr = "";
    private byte[][] zzpys = zzflv.zzpwd;
    private boolean zzpyt = false;

    public zzfmq() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    /* access modifiers changed from: private */
    /* renamed from: zzddd */
    public zzfmq clone() {
        try {
            zzfmq zzfmq = (zzfmq) super.clone();
            byte[][] bArr = this.zzpys;
            if (bArr != null && bArr.length > 0) {
                zzfmq.zzpys = (byte[][]) bArr.clone();
            }
            return zzfmq;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzfmq)) {
            return false;
        }
        zzfmq zzfmq = (zzfmq) obj;
        if (!Arrays.equals(this.zzpyq, zzfmq.zzpyq)) {
            return false;
        }
        String str = this.zzpyr;
        if (str == null) {
            if (zzfmq.zzpyr != null) {
                return false;
            }
        } else if (!str.equals(zzfmq.zzpyr)) {
            return false;
        }
        if (zzflq.zza(this.zzpys, zzfmq.zzpys) && this.zzpyt == zzfmq.zzpyt) {
            return (this.zzpvl == null || this.zzpvl.isEmpty()) ? zzfmq.zzpvl == null || zzfmq.zzpvl.isEmpty() : this.zzpvl.equals(zzfmq.zzpvl);
        }
        return false;
    }

    public final int hashCode() {
        int hashCode = (((getClass().getName().hashCode() + 527) * 31) + Arrays.hashCode(this.zzpyq)) * 31;
        String str = this.zzpyr;
        int i = 0;
        int hashCode2 = (((((hashCode + (str == null ? 0 : str.hashCode())) * 31) + zzflq.zzd(this.zzpys)) * 31) + (this.zzpyt ? 1231 : 1237)) * 31;
        if (this.zzpvl != null && !this.zzpvl.isEmpty()) {
            i = this.zzpvl.hashCode();
        }
        return hashCode2 + i;
    }

    public final /* synthetic */ zzfls zza(zzflj zzflj) throws IOException {
        while (true) {
            int zzcxx = zzflj.zzcxx();
            if (zzcxx == 0) {
                return this;
            }
            if (zzcxx == 10) {
                this.zzpyq = zzflj.readBytes();
            } else if (zzcxx == 18) {
                int zzb = zzflv.zzb(zzflj, 18);
                byte[][] bArr = this.zzpys;
                int length = bArr == null ? 0 : bArr.length;
                int i = zzb + length;
                byte[][] bArr2 = new byte[i][];
                if (length != 0) {
                    System.arraycopy(this.zzpys, 0, bArr2, 0, length);
                }
                while (length < i - 1) {
                    bArr2[length] = zzflj.readBytes();
                    zzflj.zzcxx();
                    length++;
                }
                bArr2[length] = zzflj.readBytes();
                this.zzpys = bArr2;
            } else if (zzcxx == 24) {
                this.zzpyt = zzflj.zzcyd();
            } else if (zzcxx == 34) {
                this.zzpyr = zzflj.readString();
            } else if (!super.zza(zzflj, zzcxx)) {
                return this;
            }
        }
    }

    public final void zza(zzflk zzflk) throws IOException {
        if (!Arrays.equals(this.zzpyq, zzflv.zzpwe)) {
            zzflk.zzc(1, this.zzpyq);
        }
        byte[][] bArr = this.zzpys;
        if (bArr != null && bArr.length > 0) {
            int i = 0;
            while (true) {
                byte[][] bArr2 = this.zzpys;
                if (i >= bArr2.length) {
                    break;
                }
                byte[] bArr3 = bArr2[i];
                if (bArr3 != null) {
                    zzflk.zzc(2, bArr3);
                }
                i++;
            }
        }
        boolean z = this.zzpyt;
        if (z) {
            zzflk.zzl(3, z);
        }
        String str = this.zzpyr;
        if (str != null && !str.equals("")) {
            zzflk.zzp(4, this.zzpyr);
        }
        super.zza(zzflk);
    }

    public final /* synthetic */ zzflm zzdck() throws CloneNotSupportedException {
        return (zzfmq) clone();
    }

    public final /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzfmq) clone();
    }

    /* access modifiers changed from: protected */
    public final int zzq() {
        int zzq = super.zzq();
        if (!Arrays.equals(this.zzpyq, zzflv.zzpwe)) {
            zzq += zzflk.zzd(1, this.zzpyq);
        }
        byte[][] bArr = this.zzpys;
        if (bArr != null && bArr.length > 0) {
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            while (true) {
                byte[][] bArr2 = this.zzpys;
                if (i >= bArr2.length) {
                    break;
                }
                byte[] bArr3 = bArr2[i];
                if (bArr3 != null) {
                    i3++;
                    i2 += zzflk.zzbg(bArr3);
                }
                i++;
            }
            zzq = zzq + i2 + (i3 * 1);
        }
        if (this.zzpyt) {
            zzq += zzflk.zzlw(3) + 1;
        }
        String str = this.zzpyr;
        return (str == null || str.equals("")) ? zzq : zzq + zzflk.zzq(4, this.zzpyr);
    }
}
