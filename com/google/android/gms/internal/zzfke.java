package com.google.android.gms.internal;

import java.util.Iterator;
import java.util.Map;

final class zzfke implements Iterator<Map.Entry<K, V>> {
    private int pos;
    private /* synthetic */ zzfjy zzpss;
    private boolean zzpst;
    private Iterator<Map.Entry<K, V>> zzpsu;

    private zzfke(zzfjy zzfjy) {
        this.zzpss = zzfjy;
        this.pos = -1;
    }

    /* synthetic */ zzfke(zzfjy zzfjy, zzfjz zzfjz) {
        this(zzfjy);
    }

    private final Iterator<Map.Entry<K, V>> zzdbv() {
        if (this.zzpsu == null) {
            this.zzpsu = this.zzpss.zzpsm.entrySet().iterator();
        }
        return this.zzpsu;
    }

    public final boolean hasNext() {
        return this.pos + 1 < this.zzpss.zzpsl.size() || (!this.zzpss.zzpsm.isEmpty() && zzdbv().hasNext());
    }

    public final /* synthetic */ Object next() {
        this.zzpst = true;
        int i = this.pos + 1;
        this.pos = i;
        return (Map.Entry) (i < this.zzpss.zzpsl.size() ? this.zzpss.zzpsl.get(this.pos) : zzdbv().next());
    }

    public final void remove() {
        if (this.zzpst) {
            this.zzpst = false;
            this.zzpss.zzdbr();
            if (this.pos < this.zzpss.zzpsl.size()) {
                zzfjy zzfjy = this.zzpss;
                int i = this.pos;
                this.pos = i - 1;
                Object unused = zzfjy.zzms(i);
                return;
            }
            zzdbv().remove();
            return;
        }
        throw new IllegalStateException("remove() was called before next()");
    }
}
