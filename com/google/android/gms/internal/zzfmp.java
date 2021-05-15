package com.google.android.gms.internal;

import java.io.IOException;

public final class zzfmp extends zzflm<zzfmp> implements Cloneable {
    private String version = "";
    private int zzjgl = 0;
    private String zzpyp = "";

    public zzfmp() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    /* access modifiers changed from: private */
    /* renamed from: zzddc */
    public zzfmp clone() {
        try {
            return (zzfmp) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzfmp)) {
            return false;
        }
        zzfmp zzfmp = (zzfmp) obj;
        if (this.zzjgl != zzfmp.zzjgl) {
            return false;
        }
        String str = this.zzpyp;
        if (str == null) {
            if (zzfmp.zzpyp != null) {
                return false;
            }
        } else if (!str.equals(zzfmp.zzpyp)) {
            return false;
        }
        String str2 = this.version;
        if (str2 == null) {
            if (zzfmp.version != null) {
                return false;
            }
        } else if (!str2.equals(zzfmp.version)) {
            return false;
        }
        return (this.zzpvl == null || this.zzpvl.isEmpty()) ? zzfmp.zzpvl == null || zzfmp.zzpvl.isEmpty() : this.zzpvl.equals(zzfmp.zzpvl);
    }

    public final int hashCode() {
        int hashCode = (((getClass().getName().hashCode() + 527) * 31) + this.zzjgl) * 31;
        String str = this.zzpyp;
        int i = 0;
        int hashCode2 = (hashCode + (str == null ? 0 : str.hashCode())) * 31;
        String str2 = this.version;
        int hashCode3 = (hashCode2 + (str2 == null ? 0 : str2.hashCode())) * 31;
        if (this.zzpvl != null && !this.zzpvl.isEmpty()) {
            i = this.zzpvl.hashCode();
        }
        return hashCode3 + i;
    }

    public final /* synthetic */ zzfls zza(zzflj zzflj) throws IOException {
        while (true) {
            int zzcxx = zzflj.zzcxx();
            if (zzcxx == 0) {
                return this;
            }
            if (zzcxx == 8) {
                this.zzjgl = zzflj.zzcya();
            } else if (zzcxx == 18) {
                this.zzpyp = zzflj.readString();
            } else if (zzcxx == 26) {
                this.version = zzflj.readString();
            } else if (!super.zza(zzflj, zzcxx)) {
                return this;
            }
        }
    }

    public final void zza(zzflk zzflk) throws IOException {
        int i = this.zzjgl;
        if (i != 0) {
            zzflk.zzad(1, i);
        }
        String str = this.zzpyp;
        if (str != null && !str.equals("")) {
            zzflk.zzp(2, this.zzpyp);
        }
        String str2 = this.version;
        if (str2 != null && !str2.equals("")) {
            zzflk.zzp(3, this.version);
        }
        super.zza(zzflk);
    }

    public final /* synthetic */ zzflm zzdck() throws CloneNotSupportedException {
        return (zzfmp) clone();
    }

    public final /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzfmp) clone();
    }

    /* access modifiers changed from: protected */
    public final int zzq() {
        int zzq = super.zzq();
        int i = this.zzjgl;
        if (i != 0) {
            zzq += zzflk.zzag(1, i);
        }
        String str = this.zzpyp;
        if (str != null && !str.equals("")) {
            zzq += zzflk.zzq(2, this.zzpyp);
        }
        String str2 = this.version;
        return (str2 == null || str2.equals("")) ? zzq : zzq + zzflk.zzq(3, this.version);
    }
}
