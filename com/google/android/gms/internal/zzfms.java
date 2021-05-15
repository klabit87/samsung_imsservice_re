package com.google.android.gms.internal;

import java.io.IOException;

public final class zzfms extends zzflm<zzfms> implements Cloneable {
    private static volatile zzfms[] zzpzo;
    private String key = "";
    private String value = "";

    public zzfms() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    public static zzfms[] zzddf() {
        if (zzpzo == null) {
            synchronized (zzflq.zzpvt) {
                if (zzpzo == null) {
                    zzpzo = new zzfms[0];
                }
            }
        }
        return zzpzo;
    }

    /* access modifiers changed from: private */
    /* renamed from: zzddg */
    public zzfms clone() {
        try {
            return (zzfms) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzfms)) {
            return false;
        }
        zzfms zzfms = (zzfms) obj;
        String str = this.key;
        if (str == null) {
            if (zzfms.key != null) {
                return false;
            }
        } else if (!str.equals(zzfms.key)) {
            return false;
        }
        String str2 = this.value;
        if (str2 == null) {
            if (zzfms.value != null) {
                return false;
            }
        } else if (!str2.equals(zzfms.value)) {
            return false;
        }
        return (this.zzpvl == null || this.zzpvl.isEmpty()) ? zzfms.zzpvl == null || zzfms.zzpvl.isEmpty() : this.zzpvl.equals(zzfms.zzpvl);
    }

    public final int hashCode() {
        int hashCode = (getClass().getName().hashCode() + 527) * 31;
        String str = this.key;
        int i = 0;
        int hashCode2 = (hashCode + (str == null ? 0 : str.hashCode())) * 31;
        String str2 = this.value;
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
            if (zzcxx == 10) {
                this.key = zzflj.readString();
            } else if (zzcxx == 18) {
                this.value = zzflj.readString();
            } else if (!super.zza(zzflj, zzcxx)) {
                return this;
            }
        }
    }

    public final void zza(zzflk zzflk) throws IOException {
        String str = this.key;
        if (str != null && !str.equals("")) {
            zzflk.zzp(1, this.key);
        }
        String str2 = this.value;
        if (str2 != null && !str2.equals("")) {
            zzflk.zzp(2, this.value);
        }
        super.zza(zzflk);
    }

    public final /* synthetic */ zzflm zzdck() throws CloneNotSupportedException {
        return (zzfms) clone();
    }

    public final /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzfms) clone();
    }

    /* access modifiers changed from: protected */
    public final int zzq() {
        int zzq = super.zzq();
        String str = this.key;
        if (str != null && !str.equals("")) {
            zzq += zzflk.zzq(1, this.key);
        }
        String str2 = this.value;
        return (str2 == null || str2.equals("")) ? zzq : zzq + zzflk.zzq(2, this.value);
    }
}
