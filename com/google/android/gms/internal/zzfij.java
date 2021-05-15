package com.google.android.gms.internal;

import java.util.Iterator;
import java.util.Map;

final class zzfij<K> implements Iterator<Map.Entry<K, Object>> {
    private Iterator<Map.Entry<K, Object>> zzmyq;

    public zzfij(Iterator<Map.Entry<K, Object>> it) {
        this.zzmyq = it;
    }

    public final boolean hasNext() {
        return this.zzmyq.hasNext();
    }

    public final /* synthetic */ Object next() {
        Map.Entry next = this.zzmyq.next();
        return next.getValue() instanceof zzfig ? new zzfii(next) : next;
    }

    public final void remove() {
        this.zzmyq.remove();
    }
}
