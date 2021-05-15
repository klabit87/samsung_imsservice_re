package com.google.android.gms.internal;

import java.util.Iterator;

final class zzeeh implements Iterable<zzeej> {
    /* access modifiers changed from: private */
    public final int length;
    /* access modifiers changed from: private */
    public long value;

    public zzeeh(int i) {
        int i2 = i + 1;
        int floor = (int) Math.floor(Math.log((double) i2) / Math.log(2.0d));
        this.length = floor;
        this.value = (((long) Math.pow(2.0d, (double) floor)) - 1) & ((long) i2);
    }

    public final Iterator<zzeej> iterator() {
        return new zzeei(this);
    }
}
