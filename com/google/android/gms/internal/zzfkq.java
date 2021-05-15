package com.google.android.gms.internal;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.Unsafe;

final class zzfkq {
    private static final Logger logger = Logger.getLogger(zzfkq.class.getName());
    private static final Unsafe zzmdw = zzdcf();
    private static final Class<?> zzpnt = zzfgo.zzcxn();
    private static final boolean zzpop = zzdcg();
    private static final boolean zzptd = zzp(Long.TYPE);
    private static final boolean zzpte = zzp(Integer.TYPE);
    private static final zzd zzptf;
    private static final boolean zzptg = zzdch();
    private static final long zzpth = ((long) zzn(byte[].class));
    private static final long zzpti;
    private static final long zzptj;
    private static final long zzptk;
    private static final long zzptl;
    private static final long zzptm;
    private static final long zzptn;
    private static final long zzpto;
    private static final long zzptp;
    private static final long zzptq;
    private static final long zzptr;
    private static final long zzpts = ((long) zzn(Object[].class));
    private static final long zzptt = ((long) zzo(Object[].class));
    private static final long zzptu;
    /* access modifiers changed from: private */
    public static final boolean zzptv = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);

    static final class zza extends zzd {
        zza(Unsafe unsafe) {
            super(unsafe);
        }

        public final void zze(Object obj, long j, byte b) {
            if (zzfkq.zzptv) {
                zzfkq.zza(obj, j, b);
            } else {
                zzfkq.zzb(obj, j, b);
            }
        }

        public final byte zzf(Object obj, long j) {
            return zzfkq.zzptv ? zzfkq.zzb(obj, j) : zzfkq.zzc(obj, j);
        }
    }

    static final class zzb extends zzd {
        zzb(Unsafe unsafe) {
            super(unsafe);
        }

        public final void zze(Object obj, long j, byte b) {
            if (zzfkq.zzptv) {
                zzfkq.zza(obj, j, b);
            } else {
                zzfkq.zzb(obj, j, b);
            }
        }

        public final byte zzf(Object obj, long j) {
            return zzfkq.zzptv ? zzfkq.zzb(obj, j) : zzfkq.zzc(obj, j);
        }
    }

    static final class zzc extends zzd {
        zzc(Unsafe unsafe) {
            super(unsafe);
        }

        public final void zze(Object obj, long j, byte b) {
            this.zzptw.putByte(obj, j, b);
        }

        public final byte zzf(Object obj, long j) {
            return this.zzptw.getByte(obj, j);
        }
    }

    static abstract class zzd {
        Unsafe zzptw;

        zzd(Unsafe unsafe) {
            this.zzptw = unsafe;
        }

        public final int zza(Object obj, long j) {
            return this.zzptw.getInt(obj, j);
        }

        public abstract void zze(Object obj, long j, byte b);

        public abstract byte zzf(Object obj, long j);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00e7, code lost:
        r1 = zzptf;
     */
    static {
        /*
            java.lang.Class<double[]> r0 = double[].class
            java.lang.Class<float[]> r1 = float[].class
            java.lang.Class<long[]> r2 = long[].class
            java.lang.Class<int[]> r3 = int[].class
            java.lang.Class<boolean[]> r4 = boolean[].class
            java.lang.Class<com.google.android.gms.internal.zzfkq> r5 = com.google.android.gms.internal.zzfkq.class
            java.lang.String r5 = r5.getName()
            java.util.logging.Logger r5 = java.util.logging.Logger.getLogger(r5)
            logger = r5
            sun.misc.Unsafe r5 = zzdcf()
            zzmdw = r5
            java.lang.Class r5 = com.google.android.gms.internal.zzfgo.zzcxn()
            zzpnt = r5
            java.lang.Class r5 = java.lang.Long.TYPE
            boolean r5 = zzp(r5)
            zzptd = r5
            java.lang.Class r5 = java.lang.Integer.TYPE
            boolean r5 = zzp(r5)
            zzpte = r5
            sun.misc.Unsafe r5 = zzmdw
            r6 = 0
            if (r5 != 0) goto L_0x0038
            goto L_0x005d
        L_0x0038:
            boolean r5 = com.google.android.gms.internal.zzfgo.zzcxm()
            if (r5 == 0) goto L_0x0056
            boolean r5 = zzptd
            if (r5 == 0) goto L_0x004a
            com.google.android.gms.internal.zzfkq$zzb r6 = new com.google.android.gms.internal.zzfkq$zzb
            sun.misc.Unsafe r5 = zzmdw
            r6.<init>(r5)
            goto L_0x005d
        L_0x004a:
            boolean r5 = zzpte
            if (r5 == 0) goto L_0x005d
            com.google.android.gms.internal.zzfkq$zza r6 = new com.google.android.gms.internal.zzfkq$zza
            sun.misc.Unsafe r5 = zzmdw
            r6.<init>(r5)
            goto L_0x005d
        L_0x0056:
            com.google.android.gms.internal.zzfkq$zzc r6 = new com.google.android.gms.internal.zzfkq$zzc
            sun.misc.Unsafe r5 = zzmdw
            r6.<init>(r5)
        L_0x005d:
            zzptf = r6
            boolean r5 = zzdch()
            zzptg = r5
            boolean r5 = zzdcg()
            zzpop = r5
            java.lang.Class<byte[]> r5 = byte[].class
            int r5 = zzn(r5)
            long r5 = (long) r5
            zzpth = r5
            int r5 = zzn(r4)
            long r5 = (long) r5
            zzpti = r5
            int r4 = zzo(r4)
            long r4 = (long) r4
            zzptj = r4
            int r4 = zzn(r3)
            long r4 = (long) r4
            zzptk = r4
            int r3 = zzo(r3)
            long r3 = (long) r3
            zzptl = r3
            int r3 = zzn(r2)
            long r3 = (long) r3
            zzptm = r3
            int r2 = zzo(r2)
            long r2 = (long) r2
            zzptn = r2
            int r2 = zzn(r1)
            long r2 = (long) r2
            zzpto = r2
            int r1 = zzo(r1)
            long r1 = (long) r1
            zzptp = r1
            int r1 = zzn(r0)
            long r1 = (long) r1
            zzptq = r1
            int r0 = zzo(r0)
            long r0 = (long) r0
            zzptr = r0
            java.lang.Class<java.lang.Object[]> r0 = java.lang.Object[].class
            int r0 = zzn(r0)
            long r0 = (long) r0
            zzpts = r0
            java.lang.Class<java.lang.Object[]> r0 = java.lang.Object[].class
            int r0 = zzo(r0)
            long r0 = (long) r0
            zzptt = r0
            boolean r0 = com.google.android.gms.internal.zzfgo.zzcxm()
            if (r0 == 0) goto L_0x00dd
            java.lang.Class<java.nio.Buffer> r0 = java.nio.Buffer.class
            java.lang.String r1 = "effectiveDirectAddress"
            java.lang.reflect.Field r0 = zza((java.lang.Class<?>) r0, (java.lang.String) r1)
            if (r0 == 0) goto L_0x00dd
            goto L_0x00e5
        L_0x00dd:
            java.lang.Class<java.nio.Buffer> r0 = java.nio.Buffer.class
            java.lang.String r1 = "address"
            java.lang.reflect.Field r0 = zza((java.lang.Class<?>) r0, (java.lang.String) r1)
        L_0x00e5:
            if (r0 == 0) goto L_0x00f3
            com.google.android.gms.internal.zzfkq$zzd r1 = zzptf
            if (r1 != 0) goto L_0x00ec
            goto L_0x00f3
        L_0x00ec:
            sun.misc.Unsafe r1 = r1.zzptw
            long r0 = r1.objectFieldOffset(r0)
            goto L_0x00f5
        L_0x00f3:
            r0 = -1
        L_0x00f5:
            zzptu = r0
            java.nio.ByteOrder r0 = java.nio.ByteOrder.nativeOrder()
            java.nio.ByteOrder r1 = java.nio.ByteOrder.BIG_ENDIAN
            if (r0 != r1) goto L_0x0101
            r0 = 1
            goto L_0x0102
        L_0x0101:
            r0 = 0
        L_0x0102:
            zzptv = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzfkq.<clinit>():void");
    }

    private zzfkq() {
    }

    static int zza(Object obj, long j) {
        return zzptf.zza(obj, j);
    }

    private static Field zza(Class<?> cls, String str) {
        try {
            Field declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            return declaredField;
        } catch (Throwable th) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static void zza(Object obj, long j, byte b) {
        long j2 = -4 & j;
        int zza2 = zza(obj, j2);
        int i = ((~((int) j)) & 3) << 3;
        zza(obj, j2, ((255 & b) << i) | (zza2 & (~(255 << i))));
    }

    private static void zza(Object obj, long j, int i) {
        zzptf.zzptw.putInt(obj, j, i);
    }

    static void zza(byte[] bArr, long j, byte b) {
        zzptf.zze(bArr, zzpth + j, b);
    }

    /* access modifiers changed from: private */
    public static byte zzb(Object obj, long j) {
        return (byte) (zza(obj, -4 & j) >>> ((int) (((~j) & 3) << 3)));
    }

    static byte zzb(byte[] bArr, long j) {
        return zzptf.zzf(bArr, zzpth + j);
    }

    /* access modifiers changed from: private */
    public static void zzb(Object obj, long j, byte b) {
        long j2 = -4 & j;
        int i = (((int) j) & 3) << 3;
        zza(obj, j2, ((255 & b) << i) | (zza(obj, j2) & (~(255 << i))));
    }

    /* access modifiers changed from: private */
    public static byte zzc(Object obj, long j) {
        return (byte) (zza(obj, -4 & j) >>> ((int) ((j & 3) << 3)));
    }

    static boolean zzdcd() {
        return zzpop;
    }

    static boolean zzdce() {
        return zzptg;
    }

    private static Unsafe zzdcf() {
        try {
            return (Unsafe) AccessController.doPrivileged(new zzfkr());
        } catch (Throwable th) {
            return null;
        }
    }

    private static boolean zzdcg() {
        Unsafe unsafe = zzmdw;
        if (unsafe == null) {
            return false;
        }
        try {
            Class<?> cls = unsafe.getClass();
            cls.getMethod("objectFieldOffset", new Class[]{Field.class});
            cls.getMethod("arrayBaseOffset", new Class[]{Class.class});
            cls.getMethod("arrayIndexScale", new Class[]{Class.class});
            cls.getMethod("getInt", new Class[]{Object.class, Long.TYPE});
            cls.getMethod("putInt", new Class[]{Object.class, Long.TYPE, Integer.TYPE});
            cls.getMethod("getLong", new Class[]{Object.class, Long.TYPE});
            cls.getMethod("putLong", new Class[]{Object.class, Long.TYPE, Long.TYPE});
            cls.getMethod("getObject", new Class[]{Object.class, Long.TYPE});
            cls.getMethod("putObject", new Class[]{Object.class, Long.TYPE, Object.class});
            if (zzfgo.zzcxm()) {
                return true;
            }
            cls.getMethod("getByte", new Class[]{Object.class, Long.TYPE});
            cls.getMethod("putByte", new Class[]{Object.class, Long.TYPE, Byte.TYPE});
            cls.getMethod("getBoolean", new Class[]{Object.class, Long.TYPE});
            cls.getMethod("putBoolean", new Class[]{Object.class, Long.TYPE, Boolean.TYPE});
            cls.getMethod("getFloat", new Class[]{Object.class, Long.TYPE});
            cls.getMethod("putFloat", new Class[]{Object.class, Long.TYPE, Float.TYPE});
            cls.getMethod("getDouble", new Class[]{Object.class, Long.TYPE});
            cls.getMethod("putDouble", new Class[]{Object.class, Long.TYPE, Double.TYPE});
            return true;
        } catch (Throwable th) {
            Logger logger2 = logger;
            Level level = Level.WARNING;
            String valueOf = String.valueOf(th);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 71);
            sb.append("platform method missing - proto runtime falling back to safer methods: ");
            sb.append(valueOf);
            logger2.logp(level, "com.google.protobuf.UnsafeUtil", "supportsUnsafeArrayOperations", sb.toString());
            return false;
        }
    }

    private static boolean zzdch() {
        Unsafe unsafe = zzmdw;
        if (unsafe == null) {
            return false;
        }
        try {
            Class<?> cls = unsafe.getClass();
            cls.getMethod("objectFieldOffset", new Class[]{Field.class});
            cls.getMethod("getLong", new Class[]{Object.class, Long.TYPE});
            if (zzfgo.zzcxm()) {
                return true;
            }
            cls.getMethod("getByte", new Class[]{Long.TYPE});
            cls.getMethod("putByte", new Class[]{Long.TYPE, Byte.TYPE});
            cls.getMethod("getInt", new Class[]{Long.TYPE});
            cls.getMethod("putInt", new Class[]{Long.TYPE, Integer.TYPE});
            cls.getMethod("getLong", new Class[]{Long.TYPE});
            cls.getMethod("putLong", new Class[]{Long.TYPE, Long.TYPE});
            cls.getMethod("copyMemory", new Class[]{Long.TYPE, Long.TYPE, Long.TYPE});
            cls.getMethod("copyMemory", new Class[]{Object.class, Long.TYPE, Object.class, Long.TYPE, Long.TYPE});
            return true;
        } catch (Throwable th) {
            Logger logger2 = logger;
            Level level = Level.WARNING;
            String valueOf = String.valueOf(th);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 71);
            sb.append("platform method missing - proto runtime falling back to safer methods: ");
            sb.append(valueOf);
            logger2.logp(level, "com.google.protobuf.UnsafeUtil", "supportsUnsafeByteBufferOperations", sb.toString());
            return false;
        }
    }

    private static int zzn(Class<?> cls) {
        if (zzpop) {
            return zzptf.zzptw.arrayBaseOffset(cls);
        }
        return -1;
    }

    private static int zzo(Class<?> cls) {
        if (zzpop) {
            return zzptf.zzptw.arrayIndexScale(cls);
        }
        return -1;
    }

    private static boolean zzp(Class<?> cls) {
        Class<byte[]> cls2 = byte[].class;
        if (!zzfgo.zzcxm()) {
            return false;
        }
        try {
            Class<?> cls3 = zzpnt;
            cls3.getMethod("peekLong", new Class[]{cls, Boolean.TYPE});
            cls3.getMethod("pokeLong", new Class[]{cls, Long.TYPE, Boolean.TYPE});
            cls3.getMethod("pokeInt", new Class[]{cls, Integer.TYPE, Boolean.TYPE});
            cls3.getMethod("peekInt", new Class[]{cls, Boolean.TYPE});
            cls3.getMethod("pokeByte", new Class[]{cls, Byte.TYPE});
            cls3.getMethod("peekByte", new Class[]{cls});
            cls3.getMethod("pokeByteArray", new Class[]{cls, cls2, Integer.TYPE, Integer.TYPE});
            cls3.getMethod("peekByteArray", new Class[]{cls, cls2, Integer.TYPE, Integer.TYPE});
            return true;
        } catch (Throwable th) {
            return false;
        }
    }
}
