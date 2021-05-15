package com.google.android.gms.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class zzedo<K, V> extends zzedq<K, V> {
    /* access modifiers changed from: private */
    public final K[] zzmav;
    /* access modifiers changed from: private */
    public final V[] zzmyg;
    private final Comparator<K> zzmyh;

    public zzedo(Comparator<K> comparator) {
        this.zzmav = new Object[0];
        this.zzmyg = new Object[0];
        this.zzmyh = comparator;
    }

    private zzedo(Comparator<K> comparator, K[] kArr, V[] vArr) {
        this.zzmav = kArr;
        this.zzmyg = vArr;
        this.zzmyh = comparator;
    }

    public static <A, B, C> zzedo<A, C> zza(List<A> list, Map<B, C> map, zzedt<A, B> zzedt, Comparator<A> comparator) {
        Collections.sort(list, comparator);
        int size = list.size();
        Object[] objArr = new Object[size];
        Object[] objArr2 = new Object[size];
        int i = 0;
        for (A next : list) {
            objArr[i] = next;
            objArr2[i] = map.get(zzedt.zzbo(next));
            i++;
        }
        return new zzedo<>(comparator, objArr, objArr2);
    }

    private static <T> T[] zza(T[] tArr, int i, T t) {
        int length = tArr.length + 1;
        T[] tArr2 = new Object[length];
        System.arraycopy(tArr, 0, tArr2, 0, i);
        tArr2[i] = t;
        System.arraycopy(tArr, i, tArr2, i + 1, (length - i) - 1);
        return tArr2;
    }

    private static <T> T[] zzb(T[] tArr, int i, T t) {
        int length = tArr.length;
        T[] tArr2 = new Object[length];
        System.arraycopy(tArr, 0, tArr2, 0, length);
        tArr2[i] = t;
        return tArr2;
    }

    private final int zzbm(K k) {
        int i = 0;
        while (true) {
            K[] kArr = this.zzmav;
            if (i >= kArr.length || this.zzmyh.compare(kArr[i], k) >= 0) {
                return i;
            }
            i++;
        }
        return i;
    }

    private final int zzbn(K k) {
        int i = 0;
        for (K compare : this.zzmav) {
            if (this.zzmyh.compare(k, compare) == 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static <T> T[] zzc(T[] tArr, int i) {
        int length = tArr.length - 1;
        T[] tArr2 = new Object[length];
        System.arraycopy(tArr, 0, tArr2, 0, i);
        System.arraycopy(tArr, i + 1, tArr2, i, length - i);
        return tArr2;
    }

    private final Iterator<Map.Entry<K, V>> zzj(int i, boolean z) {
        return new zzedp(this, i, z);
    }

    public final boolean containsKey(K k) {
        return zzbn(k) != -1;
    }

    public final V get(K k) {
        int zzbn = zzbn(k);
        if (zzbn != -1) {
            return this.zzmyg[zzbn];
        }
        return null;
    }

    public final Comparator<K> getComparator() {
        return this.zzmyh;
    }

    public final int indexOf(K k) {
        return zzbn(k);
    }

    public final boolean isEmpty() {
        return this.zzmav.length == 0;
    }

    public final Iterator<Map.Entry<K, V>> iterator() {
        return zzj(0, false);
    }

    public final int size() {
        return this.zzmav.length;
    }

    public final void zza(zzeeb<K, V> zzeeb) {
        int i = 0;
        while (true) {
            K[] kArr = this.zzmav;
            if (i < kArr.length) {
                zzeeb.zzh(kArr[i], this.zzmyg[i]);
                i++;
            } else {
                return;
            }
        }
    }

    public final zzedq<K, V> zzbj(K k) {
        int zzbn = zzbn(k);
        if (zzbn == -1) {
            return this;
        }
        return new zzedo(this.zzmyh, zzc(this.zzmav, zzbn), zzc(this.zzmyg, zzbn));
    }

    public final Iterator<Map.Entry<K, V>> zzbk(K k) {
        return zzj(zzbm(k), false);
    }

    public final K zzbl(K k) {
        int zzbn = zzbn(k);
        if (zzbn == -1) {
            throw new IllegalArgumentException("Can't find predecessor of nonexistent key");
        } else if (zzbn > 0) {
            return this.zzmav[zzbn - 1];
        } else {
            return null;
        }
    }

    public final K zzbvp() {
        K[] kArr = this.zzmav;
        if (kArr.length > 0) {
            return kArr[0];
        }
        return null;
    }

    public final K zzbvq() {
        K[] kArr = this.zzmav;
        if (kArr.length > 0) {
            return kArr[kArr.length - 1];
        }
        return null;
    }

    public final Iterator<Map.Entry<K, V>> zzbvr() {
        return zzj(this.zzmav.length - 1, true);
    }

    public final zzedq<K, V> zzg(K k, V v) {
        int zzbn = zzbn(k);
        if (zzbn != -1) {
            if (this.zzmav[zzbn] == k && this.zzmyg[zzbn] == v) {
                return this;
            }
            return new zzedo(this.zzmyh, zzb(this.zzmav, zzbn, k), zzb(this.zzmyg, zzbn, v));
        } else if (this.zzmav.length > 25) {
            HashMap hashMap = new HashMap(this.zzmav.length + 1);
            int i = 0;
            while (true) {
                K[] kArr = this.zzmav;
                if (i < kArr.length) {
                    hashMap.put(kArr[i], this.zzmyg[i]);
                    i++;
                } else {
                    hashMap.put(k, v);
                    return zzeee.zzb(hashMap, this.zzmyh);
                }
            }
        } else {
            int zzbm = zzbm(k);
            return new zzedo(this.zzmyh, zza(this.zzmav, zzbm, k), zza(this.zzmyg, zzbm, v));
        }
    }
}
