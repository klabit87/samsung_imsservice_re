package com.google.android.gms.internal;

import com.google.android.gms.internal.zzfhs;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

final class zzfhq<FieldDescriptorType extends zzfhs<FieldDescriptorType>> {
    private static final zzfhq zzppc = new zzfhq(true);
    private boolean zzldh;
    private final zzfjy<FieldDescriptorType, Object> zzppa;
    private boolean zzppb;

    private zzfhq() {
        this.zzppb = false;
        this.zzppa = zzfjy.zzmq(16);
    }

    private zzfhq(boolean z) {
        this.zzppb = false;
        zzfjy<FieldDescriptorType, Object> zzmq = zzfjy.zzmq(0);
        this.zzppa = zzmq;
        if (!this.zzldh) {
            zzmq.zzbkr();
            this.zzldh = true;
        }
    }

    static int zza(zzfky zzfky, int i, Object obj) {
        int zzlw = zzfhg.zzlw(i);
        if (zzfky == zzfky.GROUP) {
            zzfhz.zzh((zzfjc) obj);
            zzlw <<= 1;
        }
        return zzlw + zzb(zzfky, obj);
    }

    public static Object zza(zzfhb zzfhb, zzfky zzfky, boolean z) throws IOException {
        zzfle zzfle = zzfle.STRICT;
        switch (zzfkx.zzppe[zzfky.ordinal()]) {
            case 1:
                return Double.valueOf(zzfhb.readDouble());
            case 2:
                return Float.valueOf(zzfhb.readFloat());
            case 3:
                return Long.valueOf(zzfhb.zzcxz());
            case 4:
                return Long.valueOf(zzfhb.zzcxy());
            case 5:
                return Integer.valueOf(zzfhb.zzcya());
            case 6:
                return Long.valueOf(zzfhb.zzcyb());
            case 7:
                return Integer.valueOf(zzfhb.zzcyc());
            case 8:
                return Boolean.valueOf(zzfhb.zzcyd());
            case 9:
                return zzfhb.zzcyf();
            case 10:
                return Integer.valueOf(zzfhb.zzcyg());
            case 11:
                return Integer.valueOf(zzfhb.zzcyi());
            case 12:
                return Long.valueOf(zzfhb.zzcyj());
            case 13:
                return Integer.valueOf(zzfhb.zzcyk());
            case 14:
                return Long.valueOf(zzfhb.zzcyl());
            case 15:
                return zzfle.zza(zzfhb);
            case 16:
                throw new IllegalArgumentException("readPrimitiveField() cannot handle nested groups.");
            case 17:
                throw new IllegalArgumentException("readPrimitiveField() cannot handle embedded messages.");
            case 18:
                throw new IllegalArgumentException("readPrimitiveField() cannot handle enums.");
            default:
                throw new RuntimeException("There is no way to get here, but the compiler thinks otherwise.");
        }
    }

    static void zza(zzfhg zzfhg, zzfky zzfky, int i, Object obj) throws IOException {
        if (zzfky == zzfky.GROUP) {
            zzfjc zzfjc = (zzfjc) obj;
            zzfhz.zzh(zzfjc);
            zzfhg.zze(i, zzfjc);
            return;
        }
        zzfhg.zzac(i, zzfky.zzdcj());
        switch (zzfhr.zzppe[zzfky.ordinal()]) {
            case 1:
                zzfhg.zzn(((Double) obj).doubleValue());
                return;
            case 2:
                zzfhg.zzf(((Float) obj).floatValue());
                return;
            case 3:
                zzfhg.zzcu(((Long) obj).longValue());
                return;
            case 4:
                zzfhg.zzcu(((Long) obj).longValue());
                return;
            case 5:
                zzfhg.zzls(((Integer) obj).intValue());
                return;
            case 6:
                zzfhg.zzcw(((Long) obj).longValue());
                return;
            case 7:
                zzfhg.zzlv(((Integer) obj).intValue());
                return;
            case 8:
                zzfhg.zzdl(((Boolean) obj).booleanValue());
                return;
            case 9:
                ((zzfjc) obj).zza(zzfhg);
                return;
            case 10:
                zzfhg.zze((zzfjc) obj);
                return;
            case 11:
                if (obj instanceof zzfgs) {
                    zzfhg.zzay((zzfgs) obj);
                    return;
                } else {
                    zzfhg.zztw((String) obj);
                    return;
                }
            case 12:
                if (obj instanceof zzfgs) {
                    zzfhg.zzay((zzfgs) obj);
                    return;
                }
                byte[] bArr = (byte[]) obj;
                zzfhg.zzj(bArr, 0, bArr.length);
                return;
            case 13:
                zzfhg.zzlt(((Integer) obj).intValue());
                return;
            case 14:
                zzfhg.zzlv(((Integer) obj).intValue());
                return;
            case 15:
                zzfhg.zzcw(((Long) obj).longValue());
                return;
            case 16:
                zzfhg.zzlu(((Integer) obj).intValue());
                return;
            case 17:
                zzfhg.zzcv(((Long) obj).longValue());
                return;
            case 18:
                if (obj instanceof zzfia) {
                    zzfhg.zzls(((zzfia) obj).zzhu());
                    return;
                } else {
                    zzfhg.zzls(((Integer) obj).intValue());
                    return;
                }
            default:
                return;
        }
    }

    private void zza(FieldDescriptorType fielddescriptortype, Object obj) {
        if (!fielddescriptortype.zzczn()) {
            zza(fielddescriptortype.zzczl(), obj);
        } else if (obj instanceof List) {
            ArrayList arrayList = new ArrayList();
            arrayList.addAll((List) obj);
            ArrayList arrayList2 = arrayList;
            int size = arrayList2.size();
            int i = 0;
            while (i < size) {
                Object obj2 = arrayList2.get(i);
                i++;
                zza(fielddescriptortype.zzczl(), obj2);
            }
            obj = arrayList;
        } else {
            throw new IllegalArgumentException("Wrong object type used with protocol message reflection.");
        }
        if (obj instanceof zzfig) {
            this.zzppb = true;
        }
        this.zzppa.put(fielddescriptortype, obj);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0026, code lost:
        if ((r3 instanceof com.google.android.gms.internal.zzfia) == false) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        if ((r3 instanceof byte[]) == false) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x001b, code lost:
        if ((r3 instanceof com.google.android.gms.internal.zzfig) == false) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x001e, code lost:
        r0 = false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0046 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0047  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void zza(com.google.android.gms.internal.zzfky r2, java.lang.Object r3) {
        /*
            com.google.android.gms.internal.zzfhz.checkNotNull(r3)
            int[] r0 = com.google.android.gms.internal.zzfhr.zzppd
            com.google.android.gms.internal.zzfld r2 = r2.zzdci()
            int r2 = r2.ordinal()
            r2 = r0[r2]
            r0 = 1
            r1 = 0
            switch(r2) {
                case 1: goto L_0x0041;
                case 2: goto L_0x003e;
                case 3: goto L_0x003b;
                case 4: goto L_0x0038;
                case 5: goto L_0x0035;
                case 6: goto L_0x0032;
                case 7: goto L_0x0029;
                case 8: goto L_0x0020;
                case 9: goto L_0x0015;
                default: goto L_0x0014;
            }
        L_0x0014:
            goto L_0x0044
        L_0x0015:
            boolean r2 = r3 instanceof com.google.android.gms.internal.zzfjc
            if (r2 != 0) goto L_0x0043
            boolean r2 = r3 instanceof com.google.android.gms.internal.zzfig
            if (r2 == 0) goto L_0x001e
            goto L_0x0043
        L_0x001e:
            r0 = r1
            goto L_0x0043
        L_0x0020:
            boolean r2 = r3 instanceof java.lang.Integer
            if (r2 != 0) goto L_0x0043
            boolean r2 = r3 instanceof com.google.android.gms.internal.zzfia
            if (r2 == 0) goto L_0x001e
            goto L_0x0043
        L_0x0029:
            boolean r2 = r3 instanceof com.google.android.gms.internal.zzfgs
            if (r2 != 0) goto L_0x0043
            boolean r2 = r3 instanceof byte[]
            if (r2 == 0) goto L_0x001e
            goto L_0x0043
        L_0x0032:
            boolean r0 = r3 instanceof java.lang.String
            goto L_0x0043
        L_0x0035:
            boolean r0 = r3 instanceof java.lang.Boolean
            goto L_0x0043
        L_0x0038:
            boolean r0 = r3 instanceof java.lang.Double
            goto L_0x0043
        L_0x003b:
            boolean r0 = r3 instanceof java.lang.Float
            goto L_0x0043
        L_0x003e:
            boolean r0 = r3 instanceof java.lang.Long
            goto L_0x0043
        L_0x0041:
            boolean r0 = r3 instanceof java.lang.Integer
        L_0x0043:
            r1 = r0
        L_0x0044:
            if (r1 == 0) goto L_0x0047
            return
        L_0x0047:
            java.lang.IllegalArgumentException r2 = new java.lang.IllegalArgumentException
            java.lang.String r3 = "Wrong object type used with protocol message reflection."
            r2.<init>(r3)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzfhq.zza(com.google.android.gms.internal.zzfky, java.lang.Object):void");
    }

    private static int zzb(zzfhs<?> zzfhs, Object obj) {
        zzfky zzczl = zzfhs.zzczl();
        int zzhu = zzfhs.zzhu();
        if (!zzfhs.zzczn()) {
            return zza(zzczl, zzhu, obj);
        }
        int i = 0;
        List<Object> list = (List) obj;
        if (zzfhs.zzczo()) {
            for (Object zzb : list) {
                i += zzb(zzczl, zzb);
            }
            return zzfhg.zzlw(zzhu) + i + zzfhg.zzmf(i);
        }
        for (Object zza : list) {
            i += zza(zzczl, zzhu, zza);
        }
        return i;
    }

    private static int zzb(zzfky zzfky, Object obj) {
        switch (zzfhr.zzppe[zzfky.ordinal()]) {
            case 1:
                return zzfhg.zzo(((Double) obj).doubleValue());
            case 2:
                return zzfhg.zzg(((Float) obj).floatValue());
            case 3:
                return zzfhg.zzcx(((Long) obj).longValue());
            case 4:
                return zzfhg.zzcy(((Long) obj).longValue());
            case 5:
                return zzfhg.zzlx(((Integer) obj).intValue());
            case 6:
                return zzfhg.zzda(((Long) obj).longValue());
            case 7:
                return zzfhg.zzma(((Integer) obj).intValue());
            case 8:
                return zzfhg.zzdm(((Boolean) obj).booleanValue());
            case 9:
                return zzfhg.zzg((zzfjc) obj);
            case 10:
                return obj instanceof zzfig ? zzfhg.zza((zzfig) obj) : zzfhg.zzf((zzfjc) obj);
            case 11:
                return obj instanceof zzfgs ? zzfhg.zzaz((zzfgs) obj) : zzfhg.zztx((String) obj);
            case 12:
                return obj instanceof zzfgs ? zzfhg.zzaz((zzfgs) obj) : zzfhg.zzbd((byte[]) obj);
            case 13:
                return zzfhg.zzly(((Integer) obj).intValue());
            case 14:
                return zzfhg.zzmb(((Integer) obj).intValue());
            case 15:
                return zzfhg.zzdb(((Long) obj).longValue());
            case 16:
                return zzfhg.zzlz(((Integer) obj).intValue());
            case 17:
                return zzfhg.zzcz(((Long) obj).longValue());
            case 18:
                return obj instanceof zzfia ? zzfhg.zzmc(((zzfia) obj).zzhu()) : zzfhg.zzmc(((Integer) obj).intValue());
            default:
                throw new RuntimeException("There is no way to get here, but the compiler thinks otherwise.");
        }
    }

    private static int zzb(Map.Entry<FieldDescriptorType, Object> entry) {
        zzfhs zzfhs = (zzfhs) entry.getKey();
        Object value = entry.getValue();
        if (zzfhs.zzczm() != zzfld.MESSAGE || zzfhs.zzczn() || zzfhs.zzczo()) {
            return zzb((zzfhs<?>) zzfhs, value);
        }
        boolean z = value instanceof zzfig;
        int zzhu = ((zzfhs) entry.getKey()).zzhu();
        return z ? zzfhg.zzb(zzhu, (zzfik) (zzfig) value) : zzfhg.zzd(zzhu, (zzfjc) value);
    }

    public static <T extends zzfhs<T>> zzfhq<T> zzczj() {
        return zzppc;
    }

    public final /* synthetic */ Object clone() throws CloneNotSupportedException {
        zzfhq zzfhq = new zzfhq();
        for (int i = 0; i < this.zzppa.zzdbp(); i++) {
            Map.Entry<FieldDescriptorType, Object> zzmr = this.zzppa.zzmr(i);
            zzfhq.zza((zzfhs) zzmr.getKey(), zzmr.getValue());
        }
        for (Map.Entry next : this.zzppa.zzdbq()) {
            zzfhq.zza((zzfhs) next.getKey(), next.getValue());
        }
        zzfhq.zzppb = this.zzppb;
        return zzfhq;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof zzfhq)) {
            return false;
        }
        return this.zzppa.equals(((zzfhq) obj).zzppa);
    }

    public final int hashCode() {
        return this.zzppa.hashCode();
    }

    public final Iterator<Map.Entry<FieldDescriptorType, Object>> iterator() {
        return this.zzppb ? new zzfij(this.zzppa.entrySet().iterator()) : this.zzppa.entrySet().iterator();
    }

    public final int zzczk() {
        int i = 0;
        for (int i2 = 0; i2 < this.zzppa.zzdbp(); i2++) {
            i += zzb(this.zzppa.zzmr(i2));
        }
        for (Map.Entry<FieldDescriptorType, Object> zzb : this.zzppa.zzdbq()) {
            i += zzb(zzb);
        }
        return i;
    }
}
