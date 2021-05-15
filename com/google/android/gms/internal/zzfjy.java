package com.google.android.gms.internal;

import java.lang.Comparable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

class zzfjy<K extends Comparable<K>, V> extends AbstractMap<K, V> {
    private boolean zzldh;
    private final int zzpsk;
    /* access modifiers changed from: private */
    public List<zzfkd> zzpsl;
    /* access modifiers changed from: private */
    public Map<K, V> zzpsm;
    private volatile zzfkf zzpsn;
    private Map<K, V> zzpso;

    private zzfjy(int i) {
        this.zzpsk = i;
        this.zzpsl = Collections.emptyList();
        this.zzpsm = Collections.emptyMap();
        this.zzpso = Collections.emptyMap();
    }

    /* synthetic */ zzfjy(int i, zzfjz zzfjz) {
        this(i);
    }

    private final int zza(K k) {
        int size = this.zzpsl.size() - 1;
        if (size >= 0) {
            int compareTo = k.compareTo((Comparable) this.zzpsl.get(size).getKey());
            if (compareTo > 0) {
                return -(size + 2);
            }
            if (compareTo == 0) {
                return size;
            }
        }
        int i = 0;
        while (i <= size) {
            int i2 = (i + size) / 2;
            int compareTo2 = k.compareTo((Comparable) this.zzpsl.get(i2).getKey());
            if (compareTo2 < 0) {
                size = i2 - 1;
            } else if (compareTo2 <= 0) {
                return i2;
            } else {
                i = i2 + 1;
            }
        }
        return -(i + 1);
    }

    /* access modifiers changed from: private */
    public final void zzdbr() {
        if (this.zzldh) {
            throw new UnsupportedOperationException();
        }
    }

    private final SortedMap<K, V> zzdbs() {
        zzdbr();
        if (this.zzpsm.isEmpty() && !(this.zzpsm instanceof TreeMap)) {
            TreeMap treeMap = new TreeMap();
            this.zzpsm = treeMap;
            this.zzpso = treeMap.descendingMap();
        }
        return (SortedMap) this.zzpsm;
    }

    static <FieldDescriptorType extends zzfhs<FieldDescriptorType>> zzfjy<FieldDescriptorType, Object> zzmq(int i) {
        return new zzfjz(i);
    }

    /* access modifiers changed from: private */
    public final V zzms(int i) {
        zzdbr();
        V value = this.zzpsl.remove(i).getValue();
        if (!this.zzpsm.isEmpty()) {
            Iterator it = zzdbs().entrySet().iterator();
            this.zzpsl.add(new zzfkd(this, (Map.Entry) it.next()));
            it.remove();
        }
        return value;
    }

    public void clear() {
        zzdbr();
        if (!this.zzpsl.isEmpty()) {
            this.zzpsl.clear();
        }
        if (!this.zzpsm.isEmpty()) {
            this.zzpsm.clear();
        }
    }

    public boolean containsKey(Object obj) {
        Comparable comparable = (Comparable) obj;
        return zza(comparable) >= 0 || this.zzpsm.containsKey(comparable);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        if (this.zzpsn == null) {
            this.zzpsn = new zzfkf(this, (zzfjz) null);
        }
        return this.zzpsn;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof zzfjy)) {
            return super.equals(obj);
        }
        zzfjy zzfjy = (zzfjy) obj;
        int size = size();
        if (size != zzfjy.size()) {
            return false;
        }
        int zzdbp = zzdbp();
        if (zzdbp != zzfjy.zzdbp()) {
            return entrySet().equals(zzfjy.entrySet());
        }
        for (int i = 0; i < zzdbp; i++) {
            if (!zzmr(i).equals(zzfjy.zzmr(i))) {
                return false;
            }
        }
        if (zzdbp != size) {
            return this.zzpsm.equals(zzfjy.zzpsm);
        }
        return true;
    }

    public V get(Object obj) {
        Comparable comparable = (Comparable) obj;
        int zza = zza(comparable);
        return zza >= 0 ? this.zzpsl.get(zza).getValue() : this.zzpsm.get(comparable);
    }

    public int hashCode() {
        int zzdbp = zzdbp();
        int i = 0;
        for (int i2 = 0; i2 < zzdbp; i2++) {
            i += this.zzpsl.get(i2).hashCode();
        }
        return this.zzpsm.size() > 0 ? i + this.zzpsm.hashCode() : i;
    }

    public final boolean isImmutable() {
        return this.zzldh;
    }

    public V remove(Object obj) {
        zzdbr();
        Comparable comparable = (Comparable) obj;
        int zza = zza(comparable);
        if (zza >= 0) {
            return zzms(zza);
        }
        if (this.zzpsm.isEmpty()) {
            return null;
        }
        return this.zzpsm.remove(comparable);
    }

    public int size() {
        return this.zzpsl.size() + this.zzpsm.size();
    }

    /* renamed from: zza */
    public final V put(K k, V v) {
        zzdbr();
        int zza = zza(k);
        if (zza >= 0) {
            return this.zzpsl.get(zza).setValue(v);
        }
        zzdbr();
        if (this.zzpsl.isEmpty() && !(this.zzpsl instanceof ArrayList)) {
            this.zzpsl = new ArrayList(this.zzpsk);
        }
        int i = -(zza + 1);
        if (i >= this.zzpsk) {
            return zzdbs().put(k, v);
        }
        int size = this.zzpsl.size();
        int i2 = this.zzpsk;
        if (size == i2) {
            zzfkd remove = this.zzpsl.remove(i2 - 1);
            zzdbs().put((Comparable) remove.getKey(), remove.getValue());
        }
        this.zzpsl.add(i, new zzfkd(this, k, v));
        return null;
    }

    public void zzbkr() {
        if (!this.zzldh) {
            this.zzpsm = this.zzpsm.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(this.zzpsm);
            this.zzpso = this.zzpso.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(this.zzpso);
            this.zzldh = true;
        }
    }

    public final int zzdbp() {
        return this.zzpsl.size();
    }

    public final Iterable<Map.Entry<K, V>> zzdbq() {
        return this.zzpsm.isEmpty() ? zzfka.zzdbt() : this.zzpsm.entrySet();
    }

    public final Map.Entry<K, V> zzmr(int i) {
        return this.zzpsl.get(i);
    }
}
