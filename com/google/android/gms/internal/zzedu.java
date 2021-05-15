package com.google.android.gms.internal;

import java.util.AbstractMap;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

public final class zzedu<K, V> implements Iterator<Map.Entry<K, V>> {
    private final Stack<zzeed<K, V>> zzmyn = new Stack<>();
    private final boolean zzmyo;

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0027, code lost:
        if (r6 == false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0040, code lost:
        if (r6 != false) goto L_0x0029;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0043, code lost:
        r3 = r3.zzbvy();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    zzedu(com.google.android.gms.internal.zzedz<K, V> r3, K r4, java.util.Comparator<K> r5, boolean r6) {
        /*
            r2 = this;
            r2.<init>()
            java.util.Stack r0 = new java.util.Stack
            r0.<init>()
            r2.zzmyn = r0
            r2.zzmyo = r6
        L_0x000c:
            boolean r0 = r3.isEmpty()
            if (r0 != 0) goto L_0x0048
            if (r4 == 0) goto L_0x0024
            java.lang.Object r0 = r3.getKey()
            if (r6 == 0) goto L_0x001f
            int r0 = r5.compare(r4, r0)
            goto L_0x0025
        L_0x001f:
            int r0 = r5.compare(r0, r4)
            goto L_0x0025
        L_0x0024:
            r0 = 1
        L_0x0025:
            if (r0 >= 0) goto L_0x002e
            if (r6 != 0) goto L_0x0043
        L_0x0029:
            com.google.android.gms.internal.zzedz r3 = r3.zzbvz()
            goto L_0x000c
        L_0x002e:
            if (r0 != 0) goto L_0x0038
            java.util.Stack<com.google.android.gms.internal.zzeed<K, V>> r4 = r2.zzmyn
            com.google.android.gms.internal.zzeed r3 = (com.google.android.gms.internal.zzeed) r3
            r4.push(r3)
            return
        L_0x0038:
            java.util.Stack<com.google.android.gms.internal.zzeed<K, V>> r0 = r2.zzmyn
            r1 = r3
            com.google.android.gms.internal.zzeed r1 = (com.google.android.gms.internal.zzeed) r1
            r0.push(r1)
            if (r6 == 0) goto L_0x0043
            goto L_0x0029
        L_0x0043:
            com.google.android.gms.internal.zzedz r3 = r3.zzbvy()
            goto L_0x000c
        L_0x0048:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzedu.<init>(com.google.android.gms.internal.zzedz, java.lang.Object, java.util.Comparator, boolean):void");
    }

    /* access modifiers changed from: private */
    public final Map.Entry<K, V> next() {
        try {
            zzeed pop = this.zzmyn.pop();
            AbstractMap.SimpleEntry simpleEntry = new AbstractMap.SimpleEntry(pop.getKey(), pop.getValue());
            if (this.zzmyo) {
                for (zzedz zzbvy = pop.zzbvy(); !zzbvy.isEmpty(); zzbvy = zzbvy.zzbvz()) {
                    this.zzmyn.push((zzeed) zzbvy);
                }
            } else {
                for (zzedz zzbvz = pop.zzbvz(); !zzbvz.isEmpty(); zzbvz = zzbvz.zzbvy()) {
                    this.zzmyn.push((zzeed) zzbvz);
                }
            }
            return simpleEntry;
        } catch (EmptyStackException e) {
            throw new NoSuchElementException();
        }
    }

    public final boolean hasNext() {
        return this.zzmyn.size() > 0;
    }

    public final void remove() {
        throw new UnsupportedOperationException("remove called on immutable collection");
    }
}
