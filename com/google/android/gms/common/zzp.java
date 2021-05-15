package com.google.android.gms.common;

class zzp {
    private static final zzp zzfrl = new zzp(true, (String) null, (Throwable) null);
    final Throwable cause;
    final boolean zzfrm;
    private String zzfrn;

    zzp(boolean z, String str, Throwable th) {
        this.zzfrm = z;
        this.zzfrn = str;
        this.cause = th;
    }

    static zzp zza(String str, zzh zzh, boolean z, boolean z2) {
        return new zzr(str, zzh, z, z2);
    }

    static zzp zzahj() {
        return zzfrl;
    }

    static zzp zzd(String str, Throwable th) {
        return new zzp(false, str, th);
    }

    static zzp zzgg(String str) {
        return new zzp(false, str, (Throwable) null);
    }

    /* access modifiers changed from: package-private */
    public String getErrorMessage() {
        return this.zzfrn;
    }
}
