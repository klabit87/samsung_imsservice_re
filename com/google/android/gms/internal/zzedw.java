package com.google.android.gms.internal;

import java.util.Iterator;
import java.util.Map;

final class zzedw<T> implements Iterator<T> {
    private Iterator<Map.Entry<T, Void>> zzmyq;

    public zzedw(Iterator<Map.Entry<T, Void>> it) {
        this.zzmyq = it;
    }

    public final boolean hasNext() {
        return this.zzmyq.hasNext();
    }

    public final T next() {
        return this.zzmyq.next().getKey();
    }

    public final void remove() {
        this.zzmyq.remove();
    }
}
