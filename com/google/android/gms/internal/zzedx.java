package com.google.android.gms.internal;

public final class zzedx<K, V> extends zzeed<K, V> {
    private int size = -1;

    zzedx(K k, V v, zzedz<K, V> zzedz, zzedz<K, V> zzedz2) {
        super(k, v, zzedz, zzedz2);
    }

    public final int size() {
        if (this.size == -1) {
            this.size = zzbvy().size() + 1 + zzbvz().size();
        }
        return this.size;
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
        return new zzedx(k, v, zzedz, zzedz2);
    }

    /* access modifiers changed from: package-private */
    public final void zza(zzedz<K, V> zzedz) {
        if (this.size == -1) {
            super.zza(zzedz);
            return;
        }
        throw new IllegalStateException("Can't set left after using size");
    }

    /* access modifiers changed from: protected */
    public final int zzbvv() {
        return zzeea.zzmyt;
    }

    public final boolean zzbvw() {
        return false;
    }
}
