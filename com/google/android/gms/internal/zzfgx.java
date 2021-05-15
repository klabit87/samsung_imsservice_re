package com.google.android.gms.internal;

final class zzfgx {
    private final byte[] buffer;
    private final zzfhg zzpob;

    private zzfgx(int i) {
        byte[] bArr = new byte[i];
        this.buffer = bArr;
        this.zzpob = zzfhg.zzbc(bArr);
    }

    /* synthetic */ zzfgx(int i, zzfgt zzfgt) {
        this(i);
    }

    public final zzfgs zzcxv() {
        this.zzpob.zzcyx();
        return new zzfgz(this.buffer);
    }

    public final zzfhg zzcxw() {
        return this.zzpob;
    }
}
