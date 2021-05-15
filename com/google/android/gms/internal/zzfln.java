package com.google.android.gms.internal;

import com.google.android.gms.internal.zzflm;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public final class zzfln<M extends zzflm<M>, T> {
    public final int tag;
    private int type;
    protected final Class<T> zznro;
    private zzfhu<?, ?> zzppk;
    protected final boolean zzpvm;

    private zzfln(int i, Class<T> cls, int i2, boolean z) {
        this(11, cls, (zzfhu<?, ?>) null, i2, false);
    }

    private zzfln(int i, Class<T> cls, zzfhu<?, ?> zzfhu, int i2, boolean z) {
        this.type = i;
        this.zznro = cls;
        this.tag = i2;
        this.zzpvm = false;
        this.zzppk = null;
    }

    public static <M extends zzflm<M>, T extends zzfls> zzfln<M, T> zza(int i, Class<T> cls, long j) {
        return new zzfln<>(11, cls, (int) j, false);
    }

    private final Object zzbj(zzflj zzflj) {
        Class componentType = this.zzpvm ? this.zznro.getComponentType() : this.zznro;
        try {
            int i = this.type;
            if (i == 10) {
                zzfls zzfls = (zzfls) componentType.newInstance();
                zzflj.zza(zzfls, this.tag >>> 3);
                return zzfls;
            } else if (i == 11) {
                zzfls zzfls2 = (zzfls) componentType.newInstance();
                zzflj.zza(zzfls2);
                return zzfls2;
            } else {
                int i2 = this.type;
                StringBuilder sb = new StringBuilder(24);
                sb.append("Unknown type ");
                sb.append(i2);
                throw new IllegalArgumentException(sb.toString());
            }
        } catch (InstantiationException e) {
            String valueOf = String.valueOf(componentType);
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf).length() + 33);
            sb2.append("Error creating instance of class ");
            sb2.append(valueOf);
            throw new IllegalArgumentException(sb2.toString(), e);
        } catch (IllegalAccessException e2) {
            String valueOf2 = String.valueOf(componentType);
            StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf2).length() + 33);
            sb3.append("Error creating instance of class ");
            sb3.append(valueOf2);
            throw new IllegalArgumentException(sb3.toString(), e2);
        } catch (IOException e3) {
            throw new IllegalArgumentException("Error reading extension field", e3);
        }
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof zzfln)) {
            return false;
        }
        zzfln zzfln = (zzfln) obj;
        return this.type == zzfln.type && this.zznro == zzfln.zznro && this.tag == zzfln.tag && this.zzpvm == zzfln.zzpvm;
    }

    public final int hashCode() {
        return ((((((this.type + 1147) * 31) + this.zznro.hashCode()) * 31) + this.tag) * 31) + (this.zzpvm ? 1 : 0);
    }

    /* access modifiers changed from: protected */
    public final void zza(Object obj, zzflk zzflk) {
        try {
            zzflk.zzmy(this.tag);
            int i = this.type;
            if (i == 10) {
                ((zzfls) obj).zza(zzflk);
                zzflk.zzac(this.tag >>> 3, 4);
            } else if (i == 11) {
                zzflk.zzb((zzfls) obj);
            } else {
                int i2 = this.type;
                StringBuilder sb = new StringBuilder(24);
                sb.append("Unknown type ");
                sb.append(i2);
                throw new IllegalArgumentException(sb.toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public final T zzbq(List<zzflu> list) {
        if (list == null) {
            return null;
        }
        if (this.zzpvm) {
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < list.size(); i++) {
                zzflu zzflu = list.get(i);
                if (zzflu.zzjwl.length != 0) {
                    arrayList.add(zzbj(zzflj.zzbe(zzflu.zzjwl)));
                }
            }
            int size = arrayList.size();
            if (size == 0) {
                return null;
            }
            Class<T> cls = this.zznro;
            T cast = cls.cast(Array.newInstance(cls.getComponentType(), size));
            for (int i2 = 0; i2 < size; i2++) {
                Array.set(cast, i2, arrayList.get(i2));
            }
            return cast;
        } else if (list.isEmpty()) {
            return null;
        } else {
            return this.zznro.cast(zzbj(zzflj.zzbe(list.get(list.size() - 1).zzjwl)));
        }
    }

    /* access modifiers changed from: protected */
    public final int zzcw(Object obj) {
        int i = this.tag >>> 3;
        int i2 = this.type;
        if (i2 == 10) {
            return (zzflk.zzlw(i) << 1) + ((zzfls) obj).zzhs();
        }
        if (i2 == 11) {
            return zzflk.zzb(i, (zzfls) obj);
        }
        int i3 = this.type;
        StringBuilder sb = new StringBuilder(24);
        sb.append("Unknown type ");
        sb.append(i3);
        throw new IllegalArgumentException(sb.toString());
    }
}
