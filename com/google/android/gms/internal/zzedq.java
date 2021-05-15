package com.google.android.gms.internal;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public abstract class zzedq<K, V> implements Iterable<Map.Entry<K, V>> {
    public abstract boolean containsKey(K k);

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof zzedq)) {
            return false;
        }
        zzedq zzedq = (zzedq) obj;
        if (!getComparator().equals(zzedq.getComparator()) || size() != zzedq.size()) {
            return false;
        }
        Iterator it = iterator();
        Iterator it2 = zzedq.iterator();
        while (it.hasNext()) {
            if (!((Map.Entry) it.next()).equals(it2.next())) {
                return false;
            }
        }
        return true;
    }

    public abstract V get(K k);

    public abstract Comparator<K> getComparator();

    public int hashCode() {
        int hashCode = getComparator().hashCode();
        Iterator it = iterator();
        while (it.hasNext()) {
            hashCode = (hashCode * 31) + ((Map.Entry) it.next()).hashCode();
        }
        return hashCode;
    }

    public abstract int indexOf(K k);

    public abstract boolean isEmpty();

    public abstract Iterator<Map.Entry<K, V>> iterator();

    public abstract int size();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("{");
        Iterator it = iterator();
        boolean z = true;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (z) {
                z = false;
            } else {
                sb.append(", ");
            }
            sb.append("(");
            sb.append(entry.getKey());
            sb.append("=>");
            sb.append(entry.getValue());
            sb.append(")");
        }
        sb.append("};");
        return sb.toString();
    }

    public abstract void zza(zzeeb<K, V> zzeeb);

    public abstract zzedq<K, V> zzbj(K k);

    public abstract Iterator<Map.Entry<K, V>> zzbk(K k);

    public abstract K zzbl(K k);

    public abstract K zzbvp();

    public abstract K zzbvq();

    public abstract Iterator<Map.Entry<K, V>> zzbvr();

    public abstract zzedq<K, V> zzg(K k, V v);
}
