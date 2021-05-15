package com.google.android.gms.internal;

import java.util.Iterator;

final class zzeei implements Iterator<zzeej> {
    private int zzmzc = (this.zzmzd.length - 1);
    private /* synthetic */ zzeeh zzmzd;

    zzeei(zzeeh zzeeh) {
        this.zzmzd = zzeeh;
    }

    public final boolean hasNext() {
        return this.zzmzc >= 0;
    }

    public final /* synthetic */ Object next() {
        zzeej zzeej = new zzeej();
        zzeej.zzmze = (this.zzmzd.value & ((long) (1 << this.zzmzc))) == 0;
        zzeej.zzmzf = (int) Math.pow(2.0d, (double) this.zzmzc);
        this.zzmzc--;
        return zzeej;
    }

    public final void remove() {
    }
}
