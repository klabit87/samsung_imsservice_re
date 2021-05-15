package com.google.android.gms.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public final class zzeee<K, V> extends zzedq<K, V> {
    private Comparator<K> zzmyh;
    private zzedz<K, V> zzmyx;

    private zzeee(zzedz<K, V> zzedz, Comparator<K> comparator) {
        this.zzmyx = zzedz;
        this.zzmyh = comparator;
    }

    public static <A, B> zzeee<A, B> zzb(Map<A, B> map, Comparator<A> comparator) {
        return zzeeg.zzc(new ArrayList(map.keySet()), map, zzedr.zzbvs(), comparator);
    }

    private final zzedz<K, V> zzbs(K k) {
        zzedz<K, V> zzedz = this.zzmyx;
        while (!zzedz.isEmpty()) {
            int compare = this.zzmyh.compare(k, zzedz.getKey());
            if (compare < 0) {
                zzedz = zzedz.zzbvy();
            } else if (compare == 0) {
                return zzedz;
            } else {
                zzedz = zzedz.zzbvz();
            }
        }
        return null;
    }

    public final boolean containsKey(K k) {
        return zzbs(k) != null;
    }

    public final V get(K k) {
        zzedz zzbs = zzbs(k);
        if (zzbs != null) {
            return zzbs.getValue();
        }
        return null;
    }

    public final Comparator<K> getComparator() {
        return this.zzmyh;
    }

    public final int indexOf(K k) {
        zzedz<K, V> zzedz = this.zzmyx;
        int i = 0;
        while (!zzedz.isEmpty()) {
            int compare = this.zzmyh.compare(k, zzedz.getKey());
            if (compare == 0) {
                return i + zzedz.zzbvy().size();
            }
            if (compare < 0) {
                zzedz = zzedz.zzbvy();
            } else {
                i += zzedz.zzbvy().size() + 1;
                zzedz = zzedz.zzbvz();
            }
        }
        return -1;
    }

    public final boolean isEmpty() {
        return this.zzmyx.isEmpty();
    }

    public final Iterator<Map.Entry<K, V>> iterator() {
        return new zzedu(this.zzmyx, null, this.zzmyh, false);
    }

    public final int size() {
        return this.zzmyx.size();
    }

    public final void zza(zzeeb<K, V> zzeeb) {
        this.zzmyx.zza(zzeeb);
    }

    public final zzedq<K, V> zzbj(K k) {
        return !containsKey(k) ? this : new zzeee(this.zzmyx.zza(k, this.zzmyh).zza(null, null, zzeea.zzmyt, (zzedz) null, (zzedz) null), this.zzmyh);
    }

    public final Iterator<Map.Entry<K, V>> zzbk(K k) {
        return new zzedu(this.zzmyx, k, this.zzmyh, false);
    }

    public final K zzbl(K k) {
        zzedz<K, V> zzedz = this.zzmyx;
        zzedz<K, V> zzedz2 = null;
        while (!zzedz.isEmpty()) {
            int compare = this.zzmyh.compare(k, zzedz.getKey());
            if (compare == 0) {
                if (!zzedz.zzbvy().isEmpty()) {
                    zzedz<K, V> zzbvy = zzedz.zzbvy();
                    while (!zzbvy.zzbvz().isEmpty()) {
                        zzbvy = zzbvy.zzbvz();
                    }
                    return zzbvy.getKey();
                } else if (zzedz2 != null) {
                    return zzedz2.getKey();
                } else {
                    return null;
                }
            } else if (compare < 0) {
                zzedz = zzedz.zzbvy();
            } else {
                zzedz2 = zzedz;
                zzedz = zzedz.zzbvz();
            }
        }
        String valueOf = String.valueOf(k);
        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 50);
        sb.append("Couldn't find predecessor key of non-present key: ");
        sb.append(valueOf);
        throw new IllegalArgumentException(sb.toString());
    }

    public final K zzbvp() {
        return this.zzmyx.zzbwa().getKey();
    }

    public final K zzbvq() {
        return this.zzmyx.zzbwb().getKey();
    }

    public final Iterator<Map.Entry<K, V>> zzbvr() {
        return new zzedu(this.zzmyx, null, this.zzmyh, true);
    }

    public final zzedq<K, V> zzg(K k, V v) {
        return new zzeee(this.zzmyx.zza(k, v, this.zzmyh).zza(null, null, zzeea.zzmyt, (zzedz) null, (zzedz) null), this.zzmyh);
    }
}
