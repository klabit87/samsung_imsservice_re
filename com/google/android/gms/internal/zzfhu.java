package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhu;
import com.google.android.gms.internal.zzfhu.zza;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class zzfhu<MessageType extends zzfhu<MessageType, BuilderType>, BuilderType extends zza<MessageType, BuilderType>> extends zzfgj<MessageType, BuilderType> {
    private static Map<Object, zzfhu<?, ?>> zzppj = new ConcurrentHashMap();
    protected zzfko zzpph = zzfko.zzdca();
    protected int zzppi = -1;

    public static abstract class zza<MessageType extends zzfhu<MessageType, BuilderType>, BuilderType extends zza<MessageType, BuilderType>> extends zzfgk<MessageType, BuilderType> {
        private final MessageType zzppk;
        protected MessageType zzppl;
        private boolean zzppm = false;

        protected zza(MessageType messagetype) {
            this.zzppk = messagetype;
            this.zzppl = (zzfhu) messagetype.zza(zzg.zzppx, (Object) null, (Object) null);
        }

        private static void zza(MessageType messagetype, MessageType messagetype2) {
            zzf zzf = zzf.zzppq;
            messagetype.zza(zzg.zzpps, (Object) zzf, (Object) messagetype2);
            messagetype.zzpph = zzf.zza(messagetype.zzpph, messagetype2.zzpph);
        }

        /* access modifiers changed from: private */
        /* renamed from: zzd */
        public final BuilderType zzb(zzfhb zzfhb, zzfhm zzfhm) throws IOException {
            zzczv();
            try {
                this.zzppl.zza(zzg.zzppv, (Object) zzfhb, (Object) zzfhm);
                return this;
            } catch (RuntimeException e) {
                if (e.getCause() instanceof IOException) {
                    throw ((IOException) e.getCause());
                }
                throw e;
            }
        }

        public /* synthetic */ Object clone() throws CloneNotSupportedException {
            zza zza = (zza) ((zzfhu) this.zzppk).zza(zzg.zzppy, (Object) null, (Object) null);
            if (!this.zzppm) {
                MessageType messagetype = this.zzppl;
                messagetype.zza(zzg.zzppw, (Object) null, (Object) null);
                messagetype.zzpph.zzbkr();
                this.zzppm = true;
            }
            zza.zza((zzfhu) this.zzppl);
            return zza;
        }

        public final boolean isInitialized() {
            return zzfhu.zza(this.zzppl, false);
        }

        public final /* synthetic */ zzfgk zza(zzfhb zzfhb, zzfhm zzfhm) throws IOException {
            return (zza) zzb(zzfhb, zzfhm);
        }

        public final BuilderType zza(MessageType messagetype) {
            zzczv();
            zza(this.zzppl, messagetype);
            return this;
        }

        public final /* synthetic */ zzfgk zzcxj() {
            return (zza) clone();
        }

        public final /* synthetic */ zzfjc zzczu() {
            return this.zzppk;
        }

        /* access modifiers changed from: protected */
        public final void zzczv() {
            if (this.zzppm) {
                MessageType messagetype = (zzfhu) this.zzppl.zza(zzg.zzppx, (Object) null, (Object) null);
                zza(messagetype, this.zzppl);
                this.zzppl = messagetype;
                this.zzppm = false;
            }
        }

        public final MessageType zzczw() {
            if (this.zzppm) {
                return this.zzppl;
            }
            MessageType messagetype = this.zzppl;
            messagetype.zza(zzg.zzppw, (Object) null, (Object) null);
            messagetype.zzpph.zzbkr();
            this.zzppm = true;
            return this.zzppl;
        }

        public final MessageType zzczx() {
            boolean z = true;
            if (!this.zzppm) {
                MessageType messagetype = this.zzppl;
                messagetype.zza(zzg.zzppw, (Object) null, (Object) null);
                messagetype.zzpph.zzbkr();
                this.zzppm = true;
            }
            MessageType messagetype2 = (zzfhu) this.zzppl;
            boolean booleanValue = Boolean.TRUE.booleanValue();
            byte byteValue = ((Byte) messagetype2.zza(zzg.zzppt, (Object) null, (Object) null)).byteValue();
            if (byteValue != 1) {
                if (byteValue == 0) {
                    z = false;
                } else {
                    if (messagetype2.zza(zzg.zzppr, (Object) Boolean.FALSE, (Object) null) == null) {
                        z = false;
                    }
                    if (booleanValue) {
                        messagetype2.zza(zzg.zzppu, (Object) z ? messagetype2 : null, (Object) null);
                    }
                }
            }
            if (z) {
                return messagetype2;
            }
            throw new zzfkm(messagetype2);
        }

        public final /* synthetic */ zzfjc zzczy() {
            if (this.zzppm) {
                return this.zzppl;
            }
            MessageType messagetype = this.zzppl;
            messagetype.zza(zzg.zzppw, (Object) null, (Object) null);
            messagetype.zzpph.zzbkr();
            this.zzppm = true;
            return this.zzppl;
        }

        public final /* synthetic */ zzfjc zzczz() {
            boolean z = true;
            if (!this.zzppm) {
                MessageType messagetype = this.zzppl;
                messagetype.zza(zzg.zzppw, (Object) null, (Object) null);
                messagetype.zzpph.zzbkr();
                this.zzppm = true;
            }
            zzfhu zzfhu = (zzfhu) this.zzppl;
            boolean booleanValue = Boolean.TRUE.booleanValue();
            byte byteValue = ((Byte) zzfhu.zza(zzg.zzppt, (Object) null, (Object) null)).byteValue();
            if (byteValue != 1) {
                if (byteValue == 0) {
                    z = false;
                } else {
                    if (zzfhu.zza(zzg.zzppr, (Object) Boolean.FALSE, (Object) null) == null) {
                        z = false;
                    }
                    if (booleanValue) {
                        zzfhu.zza(zzg.zzppu, (Object) z ? zzfhu : null, (Object) null);
                    }
                }
            }
            if (z) {
                return zzfhu;
            }
            throw new zzfkm(zzfhu);
        }
    }

    public static class zzb<T extends zzfhu<T, ?>> extends zzfgm<T> {
        private T zzppk;

        public zzb(T t) {
            this.zzppk = t;
        }

        public final /* synthetic */ Object zze(zzfhb zzfhb, zzfhm zzfhm) throws zzfie {
            return zzfhu.zza(this.zzppk, zzfhb, zzfhm);
        }
    }

    static class zzc implements zzh {
        static final zzc zzppn = new zzc();
        private static zzfhv zzppo = new zzfhv();

        private zzc() {
        }

        public final double zza(boolean z, double d, boolean z2, double d2) {
            if (z == z2 && d == d2) {
                return d;
            }
            throw zzppo;
        }

        public final int zza(boolean z, int i, boolean z2, int i2) {
            if (z == z2 && i == i2) {
                return i;
            }
            throw zzppo;
        }

        public final long zza(boolean z, long j, boolean z2, long j2) {
            if (z == z2 && j == j2) {
                return j;
            }
            throw zzppo;
        }

        public final zzfgs zza(boolean z, zzfgs zzfgs, boolean z2, zzfgs zzfgs2) {
            if (z == z2 && zzfgs.equals(zzfgs2)) {
                return zzfgs;
            }
            throw zzppo;
        }

        public final zzfic zza(zzfic zzfic, zzfic zzfic2) {
            if (zzfic.equals(zzfic2)) {
                return zzfic;
            }
            throw zzppo;
        }

        public final <T> zzfid<T> zza(zzfid<T> zzfid, zzfid<T> zzfid2) {
            if (zzfid.equals(zzfid2)) {
                return zzfid;
            }
            throw zzppo;
        }

        public final <K, V> zzfiw<K, V> zza(zzfiw<K, V> zzfiw, zzfiw<K, V> zzfiw2) {
            if (zzfiw.equals(zzfiw2)) {
                return zzfiw;
            }
            throw zzppo;
        }

        public final <T extends zzfjc> T zza(T t, T t2) {
            if (t == null && t2 == null) {
                return null;
            }
            if (t == null || t2 == null) {
                throw zzppo;
            }
            T t3 = (zzfhu) t;
            if (t3 != t2 && ((zzfhu) t3.zza(zzg.zzppz, (Object) null, (Object) null)).getClass().isInstance(t2)) {
                zzfhu zzfhu = (zzfhu) t2;
                t3.zza(zzg.zzpps, (Object) this, (Object) zzfhu);
                t3.zzpph = zza(t3.zzpph, zzfhu.zzpph);
            }
            return t;
        }

        public final zzfko zza(zzfko zzfko, zzfko zzfko2) {
            if (zzfko.equals(zzfko2)) {
                return zzfko;
            }
            throw zzppo;
        }

        public final Object zza(boolean z, Object obj, Object obj2) {
            if (z && obj.equals(obj2)) {
                return obj;
            }
            throw zzppo;
        }

        public final String zza(boolean z, String str, boolean z2, String str2) {
            if (z == z2 && str.equals(str2)) {
                return str;
            }
            throw zzppo;
        }

        public final boolean zza(boolean z, boolean z2, boolean z3, boolean z4) {
            if (z == z3 && z2 == z4) {
                return z2;
            }
            throw zzppo;
        }

        public final Object zzb(boolean z, Object obj, Object obj2) {
            if (z && obj.equals(obj2)) {
                return obj;
            }
            throw zzppo;
        }

        public final Object zzc(boolean z, Object obj, Object obj2) {
            if (z && obj.equals(obj2)) {
                return obj;
            }
            throw zzppo;
        }

        public final Object zzd(boolean z, Object obj, Object obj2) {
            if (z && obj.equals(obj2)) {
                return obj;
            }
            throw zzppo;
        }

        public final void zzdn(boolean z) {
            if (z) {
                throw zzppo;
            }
        }

        public final Object zze(boolean z, Object obj, Object obj2) {
            if (z && obj.equals(obj2)) {
                return obj;
            }
            throw zzppo;
        }

        public final Object zzf(boolean z, Object obj, Object obj2) {
            if (z && obj.equals(obj2)) {
                return obj;
            }
            throw zzppo;
        }

        public final Object zzg(boolean z, Object obj, Object obj2) {
            if (z) {
                zzfhu zzfhu = (zzfhu) obj;
                zzfjc zzfjc = (zzfjc) obj2;
                boolean z2 = true;
                if (zzfhu != zzfjc) {
                    if (!((zzfhu) zzfhu.zza(zzg.zzppz, (Object) null, (Object) null)).getClass().isInstance(zzfjc)) {
                        z2 = false;
                    } else {
                        zzfhu zzfhu2 = (zzfhu) zzfjc;
                        zzfhu.zza(zzg.zzpps, (Object) this, (Object) zzfhu2);
                        zzfhu.zzpph = zza(zzfhu.zzpph, zzfhu2.zzpph);
                    }
                }
                if (z2) {
                    return obj;
                }
            }
            throw zzppo;
        }
    }

    public static abstract class zzd<MessageType extends zzd<MessageType, BuilderType>, BuilderType> extends zzfhu<MessageType, BuilderType> implements zzfje {
        protected zzfhq<Object> zzppp = zzfhq.zzczj();
    }

    static class zze implements zzh {
        int zzmci = 0;

        zze() {
        }

        public final double zza(boolean z, double d, boolean z2, double d2) {
            this.zzmci = (this.zzmci * 53) + zzfhz.zzdf(Double.doubleToLongBits(d));
            return d;
        }

        public final int zza(boolean z, int i, boolean z2, int i2) {
            this.zzmci = (this.zzmci * 53) + i;
            return i;
        }

        public final long zza(boolean z, long j, boolean z2, long j2) {
            this.zzmci = (this.zzmci * 53) + zzfhz.zzdf(j);
            return j;
        }

        public final zzfgs zza(boolean z, zzfgs zzfgs, boolean z2, zzfgs zzfgs2) {
            this.zzmci = (this.zzmci * 53) + zzfgs.hashCode();
            return zzfgs;
        }

        public final zzfic zza(zzfic zzfic, zzfic zzfic2) {
            this.zzmci = (this.zzmci * 53) + zzfic.hashCode();
            return zzfic;
        }

        public final <T> zzfid<T> zza(zzfid<T> zzfid, zzfid<T> zzfid2) {
            this.zzmci = (this.zzmci * 53) + zzfid.hashCode();
            return zzfid;
        }

        public final <K, V> zzfiw<K, V> zza(zzfiw<K, V> zzfiw, zzfiw<K, V> zzfiw2) {
            this.zzmci = (this.zzmci * 53) + zzfiw.hashCode();
            return zzfiw;
        }

        public final <T extends zzfjc> T zza(T t, T t2) {
            int i;
            if (t == null) {
                i = 37;
            } else if (t instanceof zzfhu) {
                zzfhu zzfhu = (zzfhu) t;
                if (zzfhu.zzpno == 0) {
                    int i2 = this.zzmci;
                    this.zzmci = 0;
                    zzfhu.zza(zzg.zzpps, (Object) this, (Object) zzfhu);
                    zzfhu.zzpph = zza(zzfhu.zzpph, zzfhu.zzpph);
                    zzfhu.zzpno = this.zzmci;
                    this.zzmci = i2;
                }
                i = zzfhu.zzpno;
            } else {
                i = t.hashCode();
            }
            this.zzmci = (this.zzmci * 53) + i;
            return t;
        }

        public final zzfko zza(zzfko zzfko, zzfko zzfko2) {
            this.zzmci = (this.zzmci * 53) + zzfko.hashCode();
            return zzfko;
        }

        public final Object zza(boolean z, Object obj, Object obj2) {
            this.zzmci = (this.zzmci * 53) + zzfhz.zzdo(((Boolean) obj).booleanValue());
            return obj;
        }

        public final String zza(boolean z, String str, boolean z2, String str2) {
            this.zzmci = (this.zzmci * 53) + str.hashCode();
            return str;
        }

        public final boolean zza(boolean z, boolean z2, boolean z3, boolean z4) {
            this.zzmci = (this.zzmci * 53) + zzfhz.zzdo(z2);
            return z2;
        }

        public final Object zzb(boolean z, Object obj, Object obj2) {
            this.zzmci = (this.zzmci * 53) + ((Integer) obj).intValue();
            return obj;
        }

        public final Object zzc(boolean z, Object obj, Object obj2) {
            this.zzmci = (this.zzmci * 53) + zzfhz.zzdf(Double.doubleToLongBits(((Double) obj).doubleValue()));
            return obj;
        }

        public final Object zzd(boolean z, Object obj, Object obj2) {
            this.zzmci = (this.zzmci * 53) + zzfhz.zzdf(((Long) obj).longValue());
            return obj;
        }

        public final void zzdn(boolean z) {
            if (z) {
                throw new IllegalStateException();
            }
        }

        public final Object zze(boolean z, Object obj, Object obj2) {
            this.zzmci = (this.zzmci * 53) + obj.hashCode();
            return obj;
        }

        public final Object zzf(boolean z, Object obj, Object obj2) {
            this.zzmci = (this.zzmci * 53) + obj.hashCode();
            return obj;
        }

        public final Object zzg(boolean z, Object obj, Object obj2) {
            return zza((zzfjc) obj, (zzfjc) obj2);
        }
    }

    public static class zzf implements zzh {
        public static final zzf zzppq = new zzf();

        private zzf() {
        }

        public final double zza(boolean z, double d, boolean z2, double d2) {
            return z2 ? d2 : d;
        }

        public final int zza(boolean z, int i, boolean z2, int i2) {
            return z2 ? i2 : i;
        }

        public final long zza(boolean z, long j, boolean z2, long j2) {
            return z2 ? j2 : j;
        }

        public final zzfgs zza(boolean z, zzfgs zzfgs, boolean z2, zzfgs zzfgs2) {
            return z2 ? zzfgs2 : zzfgs;
        }

        public final zzfic zza(zzfic zzfic, zzfic zzfic2) {
            int size = zzfic.size();
            int size2 = zzfic2.size();
            if (size > 0 && size2 > 0) {
                if (!zzfic.zzcxk()) {
                    zzfic = zzfic.zzmk(size2 + size);
                }
                zzfic.addAll(zzfic2);
            }
            return size > 0 ? zzfic : zzfic2;
        }

        public final <T> zzfid<T> zza(zzfid<T> zzfid, zzfid<T> zzfid2) {
            int size = zzfid.size();
            int size2 = zzfid2.size();
            if (size > 0 && size2 > 0) {
                if (!zzfid.zzcxk()) {
                    zzfid = zzfid.zzmo(size2 + size);
                }
                zzfid.addAll(zzfid2);
            }
            return size > 0 ? zzfid : zzfid2;
        }

        public final <K, V> zzfiw<K, V> zza(zzfiw<K, V> zzfiw, zzfiw<K, V> zzfiw2) {
            if (!zzfiw2.isEmpty()) {
                if (!zzfiw.isMutable()) {
                    zzfiw = zzfiw.zzdau();
                }
                zzfiw.zza(zzfiw2);
            }
            return zzfiw;
        }

        public final <T extends zzfjc> T zza(T t, T t2) {
            return (t == null || t2 == null) ? t != null ? t : t2 : t.zzczt().zzd(t2).zzczz();
        }

        public final zzfko zza(zzfko zzfko, zzfko zzfko2) {
            return zzfko2 == zzfko.zzdca() ? zzfko : zzfko.zzb(zzfko, zzfko2);
        }

        public final Object zza(boolean z, Object obj, Object obj2) {
            return obj2;
        }

        public final String zza(boolean z, String str, boolean z2, String str2) {
            return z2 ? str2 : str;
        }

        public final boolean zza(boolean z, boolean z2, boolean z3, boolean z4) {
            return z3 ? z4 : z2;
        }

        public final Object zzb(boolean z, Object obj, Object obj2) {
            return obj2;
        }

        public final Object zzc(boolean z, Object obj, Object obj2) {
            return obj2;
        }

        public final Object zzd(boolean z, Object obj, Object obj2) {
            return obj2;
        }

        public final void zzdn(boolean z) {
        }

        public final Object zze(boolean z, Object obj, Object obj2) {
            return obj2;
        }

        public final Object zzf(boolean z, Object obj, Object obj2) {
            return obj2;
        }

        public final Object zzg(boolean z, Object obj, Object obj2) {
            return z ? zza((zzfjc) obj, (zzfjc) obj2) : obj2;
        }
    }

    /* 'enum' modifier removed */
    public static final class zzg {
        public static final int zzppr = 1;
        public static final int zzpps = 2;
        public static final int zzppt = 3;
        public static final int zzppu = 4;
        public static final int zzppv = 5;
        public static final int zzppw = 6;
        public static final int zzppx = 7;
        public static final int zzppy = 8;
        public static final int zzppz = 9;
        public static final int zzpqa = 10;
        private static final /* synthetic */ int[] zzpqb = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        public static final int zzpqc = 1;
        private static int zzpqd = 2;
        private static final /* synthetic */ int[] zzpqe = {1, 2};
        public static final int zzpqf = 1;
        public static final int zzpqg = 2;
        private static final /* synthetic */ int[] zzpqh = {1, 2};

        public static int[] values$50KLMJ33DTMIUPRFDTJMOP9FE1P6UT3FC9QMCBQ7CLN6ASJ1EHIM8JB5EDPM2PR59HKN8P949LIN8Q3FCHA6UIBEEPNMMP9R0() {
            return (int[]) zzpqb.clone();
        }
    }

    public interface zzh {
        double zza(boolean z, double d, boolean z2, double d2);

        int zza(boolean z, int i, boolean z2, int i2);

        long zza(boolean z, long j, boolean z2, long j2);

        zzfgs zza(boolean z, zzfgs zzfgs, boolean z2, zzfgs zzfgs2);

        zzfic zza(zzfic zzfic, zzfic zzfic2);

        <T> zzfid<T> zza(zzfid<T> zzfid, zzfid<T> zzfid2);

        <K, V> zzfiw<K, V> zza(zzfiw<K, V> zzfiw, zzfiw<K, V> zzfiw2);

        <T extends zzfjc> T zza(T t, T t2);

        zzfko zza(zzfko zzfko, zzfko zzfko2);

        Object zza(boolean z, Object obj, Object obj2);

        String zza(boolean z, String str, boolean z2, String str2);

        boolean zza(boolean z, boolean z2, boolean z3, boolean z4);

        Object zzb(boolean z, Object obj, Object obj2);

        Object zzc(boolean z, Object obj, Object obj2);

        Object zzd(boolean z, Object obj, Object obj2);

        void zzdn(boolean z);

        Object zze(boolean z, Object obj, Object obj2);

        Object zzf(boolean z, Object obj, Object obj2);

        Object zzg(boolean z, Object obj, Object obj2);
    }

    protected static <T extends zzfhu<T, ?>> T zza(T t, zzfgs zzfgs) throws zzfie {
        boolean z;
        T zza2 = zza(t, zzfgs, zzfhm.zzczf());
        boolean z2 = false;
        if (zza2 != null) {
            boolean booleanValue = Boolean.TRUE.booleanValue();
            byte byteValue = ((Byte) zza2.zza(zzg.zzppt, (Object) null, (Object) null)).byteValue();
            if (byteValue == 1) {
                z = true;
            } else if (byteValue == 0) {
                z = false;
            } else {
                z = zza2.zza(zzg.zzppr, (Object) Boolean.FALSE, (Object) null) != null;
                if (booleanValue) {
                    zza2.zza(zzg.zzppu, (Object) z ? zza2 : null, (Object) null);
                }
            }
            if (!z) {
                throw new zzfkm(zza2).zzdbz().zzi(zza2);
            }
        }
        if (zza2 != null) {
            boolean booleanValue2 = Boolean.TRUE.booleanValue();
            byte byteValue2 = ((Byte) zza2.zza(zzg.zzppt, (Object) null, (Object) null)).byteValue();
            if (byteValue2 == 1) {
                z2 = true;
            } else if (byteValue2 != 0) {
                if (zza2.zza(zzg.zzppr, (Object) Boolean.FALSE, (Object) null) != null) {
                    z2 = true;
                }
                if (booleanValue2) {
                    zza2.zza(zzg.zzppu, (Object) z2 ? zza2 : null, (Object) null);
                }
            }
            if (!z2) {
                throw new zzfkm(zza2).zzdbz().zzi(zza2);
            }
        }
        return zza2;
    }

    private static <T extends zzfhu<T, ?>> T zza(T t, zzfgs zzfgs, zzfhm zzfhm) throws zzfie {
        T zza2;
        try {
            zzfhb zzcxq = zzfgs.zzcxq();
            zza2 = zza(t, zzcxq, zzfhm);
            zzcxq.zzlf(0);
            return zza2;
        } catch (zzfie e) {
            throw e.zzi(zza2);
        } catch (zzfie e2) {
            throw e2;
        }
    }

    static <T extends zzfhu<T, ?>> T zza(T t, zzfhb zzfhb, zzfhm zzfhm) throws zzfie {
        T t2 = (zzfhu) t.zza(zzg.zzppx, (Object) null, (Object) null);
        try {
            t2.zza(zzg.zzppv, (Object) zzfhb, (Object) zzfhm);
            t2.zza(zzg.zzppw, (Object) null, (Object) null);
            t2.zzpph.zzbkr();
            return t2;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof zzfie) {
                throw ((zzfie) e.getCause());
            }
            throw e;
        }
    }

    protected static <T extends zzfhu<T, ?>> T zza(T t, byte[] bArr) throws zzfie {
        T zza2 = zza(t, bArr, zzfhm.zzczf());
        if (zza2 != null) {
            boolean booleanValue = Boolean.TRUE.booleanValue();
            byte byteValue = ((Byte) zza2.zza(zzg.zzppt, (Object) null, (Object) null)).byteValue();
            boolean z = false;
            if (byteValue == 1) {
                z = true;
            } else if (byteValue != 0) {
                if (zza2.zza(zzg.zzppr, (Object) Boolean.FALSE, (Object) null) != null) {
                    z = true;
                }
                if (booleanValue) {
                    zza2.zza(zzg.zzppu, (Object) z ? zza2 : null, (Object) null);
                }
            }
            if (!z) {
                throw new zzfkm(zza2).zzdbz().zzi(zza2);
            }
        }
        return zza2;
    }

    private static <T extends zzfhu<T, ?>> T zza(T t, byte[] bArr, zzfhm zzfhm) throws zzfie {
        T zza2;
        try {
            zzfhb zzbb = zzfhb.zzbb(bArr);
            zza2 = zza(t, zzbb, zzfhm);
            zzbb.zzlf(0);
            return zza2;
        } catch (zzfie e) {
            throw e.zzi(zza2);
        } catch (zzfie e2) {
            throw e2;
        }
    }

    static Object zza(Method method, Object obj, Object... objArr) {
        try {
            return method.invoke(obj, objArr);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Couldn't use Java reflection to implement protocol message reflection.", e);
        } catch (InvocationTargetException e2) {
            Throwable cause = e2.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else if (cause instanceof Error) {
                throw ((Error) cause);
            } else {
                throw new RuntimeException("Unexpected exception thrown by generated accessor method.", cause);
            }
        }
    }

    protected static final <T extends zzfhu<T, ?>> boolean zza(T t, boolean z) {
        byte byteValue = ((Byte) t.zza(zzg.zzppt, (Object) null, (Object) null)).byteValue();
        if (byteValue == 1) {
            return true;
        }
        if (byteValue == 0) {
            return false;
        }
        return t.zza(zzg.zzppr, (Object) Boolean.FALSE, (Object) null) != null;
    }

    protected static zzfic zzczr() {
        return zzfhy.zzdad();
    }

    protected static <E> zzfid<E> zzczs() {
        return zzfjo.zzdbg();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!((zzfhu) zza(zzg.zzppz, (Object) null, (Object) null)).getClass().isInstance(obj)) {
            return false;
        }
        try {
            zzc zzc2 = zzc.zzppn;
            zzfhu zzfhu = (zzfhu) obj;
            zza(zzg.zzpps, (Object) zzc2, (Object) zzfhu);
            this.zzpph = zzc2.zza(this.zzpph, zzfhu.zzpph);
            return true;
        } catch (zzfhv e) {
            return false;
        }
    }

    public int hashCode() {
        if (this.zzpno != 0) {
            return this.zzpno;
        }
        zze zze2 = new zze();
        zza(zzg.zzpps, (Object) zze2, (Object) this);
        zzfko zzfko = this.zzpph;
        this.zzpph = zze2.zza(zzfko, zzfko);
        this.zzpno = zze2.zzmci;
        return this.zzpno;
    }

    public final boolean isInitialized() {
        boolean booleanValue = Boolean.TRUE.booleanValue();
        byte byteValue = ((Byte) zza(zzg.zzppt, (Object) null, (Object) null)).byteValue();
        boolean z = true;
        if (byteValue == 1) {
            return true;
        }
        if (byteValue == 0) {
            return false;
        }
        if (zza(zzg.zzppr, (Object) Boolean.FALSE, (Object) null) == null) {
            z = false;
        }
        if (booleanValue) {
            zza(zzg.zzppu, (Object) z ? this : null, (Object) null);
        }
        return z;
    }

    public String toString() {
        return zzfjf.zza(this, super.toString());
    }

    /* access modifiers changed from: protected */
    public abstract Object zza(int i, Object obj, Object obj2);

    public void zza(zzfhg zzfhg) throws IOException {
        zzfjn.zzdbf().zzl(getClass()).zza(this, zzfhi.zzb(zzfhg));
    }

    /* access modifiers changed from: protected */
    public final boolean zza(int i, zzfhb zzfhb) throws IOException {
        if ((i & 7) == 4) {
            return false;
        }
        if (this.zzpph == zzfko.zzdca()) {
            this.zzpph = zzfko.zzdcb();
        }
        return this.zzpph.zzb(i, zzfhb);
    }

    public final zzfjl<MessageType> zzczq() {
        return (zzfjl) zza(zzg.zzpqa, (Object) null, (Object) null);
    }

    public final /* synthetic */ zzfjd zzczt() {
        zza zza2 = (zza) zza(zzg.zzppy, (Object) null, (Object) null);
        zza2.zza(this);
        return zza2;
    }

    public final /* synthetic */ zzfjc zzczu() {
        return (zzfhu) zza(zzg.zzppz, (Object) null, (Object) null);
    }

    public int zzhs() {
        if (this.zzppi == -1) {
            this.zzppi = zzfjn.zzdbf().zzl(getClass()).zzct(this);
        }
        return this.zzppi;
    }
}
