package com.google.android.gms.internal;

import java.util.Comparator;

public abstract class zzeed<K, V> implements zzedz<K, V> {
    private final V value;
    private final K zzmbd;
    private zzedz<K, V> zzmyv;
    private final zzedz<K, V> zzmyw;

    zzeed(K k, V v, zzedz<K, V> zzedz, zzedz<K, V> zzedz2) {
        this.zzmbd = k;
        this.value = v;
        this.zzmyv = zzedz == null ? zzedy.zzbvx() : zzedz;
        this.zzmyw = zzedz2 == null ? zzedy.zzbvx() : zzedz2;
    }

    private static int zzb(zzedz zzedz) {
        return zzedz.zzbvw() ? zzeea.zzmyt : zzeea.zzmys;
    }

    private final zzeed<K, V> zzb(K k, V v, Integer num, zzedz<K, V> zzedz, zzedz<K, V> zzedz2) {
        K k2 = this.zzmbd;
        V v2 = this.value;
        if (zzedz == null) {
            zzedz = this.zzmyv;
        }
        if (zzedz2 == null) {
            zzedz2 = this.zzmyw;
        }
        return num == zzeea.zzmys ? new zzeec(k2, v2, zzedz, zzedz2) : new zzedx(k2, v2, zzedz, zzedz2);
    }

    private final zzedz<K, V> zzbwc() {
        if (this.zzmyv.isEmpty()) {
            return zzedy.zzbvx();
        }
        zzeed zzbwd = (this.zzmyv.zzbvw() || this.zzmyv.zzbvy().zzbvw()) ? this : zzbwd();
        return zzbwd.zza((Object) null, (Object) null, ((zzeed) zzbwd.zzmyv).zzbwc(), (zzedz) null).zzbwe();
    }

    private final zzeed<K, V> zzbwd() {
        zzeed<K, V> zzbwh = zzbwh();
        return zzbwh.zzmyw.zzbvy().zzbvw() ? zzbwh.zza((K) null, (V) null, (zzedz<K, V>) null, ((zzeed) zzbwh.zzmyw).zzbwg()).zzbwf().zzbwh() : zzbwh;
    }

    private final zzeed<K, V> zzbwe() {
        zzeed zzbwf = (!this.zzmyw.zzbvw() || this.zzmyv.zzbvw()) ? this : zzbwf();
        if (zzbwf.zzmyv.zzbvw() && ((zzeed) zzbwf.zzmyv).zzmyv.zzbvw()) {
            zzbwf = zzbwf.zzbwg();
        }
        return (!zzbwf.zzmyv.zzbvw() || !zzbwf.zzmyw.zzbvw()) ? zzbwf : zzbwf.zzbwh();
    }

    private final zzeed<K, V> zzbwf() {
        return (zzeed) this.zzmyw.zza(null, null, zzbvv(), zzb((Object) null, (Object) null, zzeea.zzmys, (zzedz) null, ((zzeed) this.zzmyw).zzmyv), (zzeed<K, V>) null);
    }

    private final zzeed<K, V> zzbwg() {
        return (zzeed) this.zzmyv.zza(null, null, zzbvv(), (zzedz) null, zzb((Object) null, (Object) null, zzeea.zzmys, ((zzeed) this.zzmyv).zzmyw, (zzedz<K, V>) null));
    }

    private final zzeed<K, V> zzbwh() {
        zzedz<K, V> zzedz = this.zzmyv;
        zzedz<K, V> zza = zzedz.zza(null, null, zzb(zzedz), (zzedz<K, V>) null, (zzedz<K, V>) null);
        zzedz<K, V> zzedz2 = this.zzmyw;
        return zzb((Object) null, (Object) null, zzb(this), zza, zzedz2.zza(null, null, zzb(zzedz2), (zzedz<K, V>) null, (zzedz<K, V>) null));
    }

    public final K getKey() {
        return this.zzmbd;
    }

    public final V getValue() {
        return this.value;
    }

    public final boolean isEmpty() {
        return false;
    }

    public final /* synthetic */ zzedz zza(Object obj, Object obj2, int i, zzedz zzedz, zzedz zzedz2) {
        return zzb((Object) null, (Object) null, i, zzedz, zzedz2);
    }

    public final zzedz<K, V> zza(K k, V v, Comparator<K> comparator) {
        int compare = comparator.compare(k, this.zzmbd);
        return (compare < 0 ? zza((Object) null, (Object) null, this.zzmyv.zza(k, v, comparator), (zzedz<K, V>) null) : compare == 0 ? zza(k, v, (zzedz) null, (zzedz) null) : zza((Object) null, (Object) null, (zzedz) null, this.zzmyw.zza(k, v, comparator))).zzbwe();
    }

    public final zzedz<K, V> zza(K k, Comparator<K> comparator) {
        zzeed<K, V> zzeed;
        if (comparator.compare(k, this.zzmbd) < 0) {
            zzeed zzbwd = (this.zzmyv.isEmpty() || this.zzmyv.zzbvw() || ((zzeed) this.zzmyv).zzmyv.zzbvw()) ? this : zzbwd();
            zzeed = zzbwd.zza((Object) null, (Object) null, zzbwd.zzmyv.zza(k, comparator), (zzedz<K, V>) null);
        } else {
            zzeed zzbwg = this.zzmyv.zzbvw() ? zzbwg() : this;
            if (!zzbwg.zzmyw.isEmpty() && !zzbwg.zzmyw.zzbvw() && !((zzeed) zzbwg.zzmyw).zzmyv.zzbvw()) {
                zzbwg = zzbwg.zzbwh();
                if (zzbwg.zzmyv.zzbvy().zzbvw()) {
                    zzbwg = zzbwg.zzbwg().zzbwh();
                }
            }
            if (comparator.compare(k, zzbwg.zzmbd) == 0) {
                if (zzbwg.zzmyw.isEmpty()) {
                    return zzedy.zzbvx();
                }
                zzedz<K, V> zzbwa = zzbwg.zzmyw.zzbwa();
                zzbwg = zzbwg.zza(zzbwa.getKey(), zzbwa.getValue(), (zzedz) null, ((zzeed) zzbwg.zzmyw).zzbwc());
            }
            zzeed = zzbwg.zza((Object) null, (Object) null, (zzedz) null, zzbwg.zzmyw.zza(k, comparator));
        }
        return zzeed.zzbwe();
    }

    /* access modifiers changed from: protected */
    public abstract zzeed<K, V> zza(K k, V v, zzedz<K, V> zzedz, zzedz<K, V> zzedz2);

    /* access modifiers changed from: package-private */
    public void zza(zzedz<K, V> zzedz) {
        this.zzmyv = zzedz;
    }

    public final void zza(zzeeb<K, V> zzeeb) {
        this.zzmyv.zza(zzeeb);
        zzeeb.zzh(this.zzmbd, this.value);
        this.zzmyw.zza(zzeeb);
    }

    /* access modifiers changed from: protected */
    public abstract int zzbvv();

    public final zzedz<K, V> zzbvy() {
        return this.zzmyv;
    }

    public final zzedz<K, V> zzbvz() {
        return this.zzmyw;
    }

    public final zzedz<K, V> zzbwa() {
        return this.zzmyv.isEmpty() ? this : this.zzmyv.zzbwa();
    }

    public final zzedz<K, V> zzbwb() {
        return this.zzmyw.isEmpty() ? this : this.zzmyw.zzbwb();
    }
}
