package com.google.android.gms.common;

import java.util.Arrays;

final class zzi extends zzh {
    private final byte[] zzfre;

    zzi(byte[] bArr) {
        super(Arrays.copyOfRange(bArr, 0, 25));
        this.zzfre = bArr;
    }

    /* access modifiers changed from: package-private */
    public final byte[] getBytes() {
        return this.zzfre;
    }
}
