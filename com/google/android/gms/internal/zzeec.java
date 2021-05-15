package com.google.android.gms.internal;

public final class zzeec<K, V> extends zzeed<K, V> {
    zzeec(K k, V v) {
        super(k, v, zzedy.zzbvx(), zzedy.zzbvx());
    }

    zzeec(K k, V v, zzedz<K, V> zzedz, zzedz<K, V> zzedz2) {
        super(k, v, zzedz, zzedz2);
    }

    public final int size() {
        return zzbvy().size() + 1 + zzbvz().size();
    }

    /* access modifiers changed from: protected */
    public final zzeed<K, V> zza(K k, V v, zzedz<K, V> zzedz, zzedz<K, V> zzedz2) {
        if (k == null) {
            k = getKey();
        }
        if (v == null) {
            v = getValue();
        }
        if (zzedz == null) {
            zzedz = zzbvy();
        }
        if (zzedz2 == null) {
            zzedz2 = zzbvz();
        }
        return new zzeec(k, v, zzedz, zzedz2);
    }

    /* access modifiers changed from: protected */
    public final int zzbvv() {
        return zzeea.zzmys;
    }

    public final boolean zzbvw() {
        return true;
    }
}
