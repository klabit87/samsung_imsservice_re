package com.google.android.gms.internal;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

class zzfkf extends AbstractSet<Map.Entry<K, V>> {
    private /* synthetic */ zzfjy zzpss;

    private zzfkf(zzfjy zzfjy) {
        this.zzpss = zzfjy;
    }

    /* synthetic */ zzfkf(zzfjy zzfjy, zzfjz zzfjz) {
        this(zzfjy);
    }

    public /* synthetic */ boolean add(Object obj) {
        Map.Entry entry = (Map.Entry) obj;
        if (contains(entry)) {
            return false;
        }
        this.zzpss.put((Comparable) entry.getKey(), entry.getValue());
        return true;
    }

    public void clear() {
        this.zzpss.clear();
    }

    public boolean contains(Object obj) {
        Map.Entry entry = (Map.Entry) obj;
        Object obj2 = this.zzpss.get(entry.getKey());
        Object value = entry.getValue();
        if (obj2 != value) {
            return obj2 != null && obj2.equals(value);
        }
        return true;
    }

    public Iterator<Map.Entry<K, V>> iterator() {
        return new zzfke(this.zzpss, (zzfjz) null);
    }

    public boolean remove(Object obj) {
        Map.Entry entry = (Map.Entry) obj;
        if (!contains(entry)) {
            return false;
        }
        this.zzpss.remove(entry.getKey());
        return true;
    }

    public int size() {
        return this.zzpss.size();
    }
}
