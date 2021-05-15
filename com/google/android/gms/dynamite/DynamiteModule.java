package com.google.android.gms.dynamite;

import android.content.Context;
import android.database.Cursor;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.common.zzf;
import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.dynamic.zzn;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public final class DynamiteModule {
    private static Boolean zzhdc;
    private static zzk zzhdd;
    private static zzm zzhde;
    private static String zzhdf;
    private static final ThreadLocal<zza> zzhdg = new ThreadLocal<>();
    private static final zzi zzhdh = new zza();
    public static final zzd zzhdi = new zzb();
    private static zzd zzhdj = new zzc();
    public static final zzd zzhdk = new zzd();
    public static final zzd zzhdl = new zze();
    public static final zzd zzhdm = new zzf();
    public static final zzd zzhdn = new zzg();
    private final Context zzhdo;

    public static class DynamiteLoaderClassLoader {
        public static ClassLoader sClassLoader;
    }

    static class zza {
        public Cursor zzhdp;

        private zza() {
        }

        /* synthetic */ zza(zza zza) {
            this();
        }
    }

    static class zzb implements zzi {
        private final int zzhdq;
        private final int zzhdr = 0;

        public zzb(int i, int i2) {
            this.zzhdq = i;
        }

        public final int zzc(Context context, String str, boolean z) {
            return 0;
        }

        public final int zzx(Context context, String str) {
            return this.zzhdq;
        }
    }

    public static class zzc extends Exception {
        private zzc(String str) {
            super(str);
        }

        /* synthetic */ zzc(String str, zza zza) {
            this(str);
        }

        private zzc(String str, Throwable th) {
            super(str, th);
        }

        /* synthetic */ zzc(String str, Throwable th, zza zza) {
            this(str, th);
        }
    }

    public interface zzd {
        zzj zza(Context context, String str, zzi zzi) throws zzc;
    }

    private DynamiteModule(Context context) {
        this.zzhdo = (Context) zzbq.checkNotNull(context);
    }

    private static Context zza(Context context, String str, int i, Cursor cursor, zzm zzm) {
        try {
            return (Context) zzn.zzy(zzm.zza(zzn.zzz(context), str, i, zzn.zzz(cursor)));
        } catch (Exception e) {
            String valueOf = String.valueOf(e.toString());
            Log.e("DynamiteModule", valueOf.length() != 0 ? "Failed to load DynamiteLoader: ".concat(valueOf) : new String("Failed to load DynamiteLoader: "));
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x007c, code lost:
        if (r3.zzhdp != null) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0095, code lost:
        if (r3.zzhdp != null) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d1, code lost:
        if (r3.zzhdp != null) goto L_0x007e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.google.android.gms.dynamite.DynamiteModule zza(android.content.Context r10, com.google.android.gms.dynamite.DynamiteModule.zzd r11, java.lang.String r12) throws com.google.android.gms.dynamite.DynamiteModule.zzc {
        /*
            java.lang.String r0 = ":"
            java.lang.String r1 = "DynamiteModule"
            java.lang.ThreadLocal<com.google.android.gms.dynamite.DynamiteModule$zza> r2 = zzhdg
            java.lang.Object r2 = r2.get()
            com.google.android.gms.dynamite.DynamiteModule$zza r2 = (com.google.android.gms.dynamite.DynamiteModule.zza) r2
            com.google.android.gms.dynamite.DynamiteModule$zza r3 = new com.google.android.gms.dynamite.DynamiteModule$zza
            r4 = 0
            r3.<init>(r4)
            java.lang.ThreadLocal<com.google.android.gms.dynamite.DynamiteModule$zza> r5 = zzhdg
            r5.set(r3)
            com.google.android.gms.dynamite.zzi r5 = zzhdh     // Catch:{ all -> 0x0121 }
            com.google.android.gms.dynamite.zzj r5 = r11.zza(r10, r12, r5)     // Catch:{ all -> 0x0121 }
            int r6 = r5.zzhds     // Catch:{ all -> 0x0121 }
            int r7 = r5.zzhdt     // Catch:{ all -> 0x0121 }
            java.lang.String r8 = java.lang.String.valueOf(r12)     // Catch:{ all -> 0x0121 }
            int r8 = r8.length()     // Catch:{ all -> 0x0121 }
            int r8 = r8 + 68
            java.lang.String r9 = java.lang.String.valueOf(r12)     // Catch:{ all -> 0x0121 }
            int r9 = r9.length()     // Catch:{ all -> 0x0121 }
            int r8 = r8 + r9
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x0121 }
            r9.<init>(r8)     // Catch:{ all -> 0x0121 }
            java.lang.String r8 = "Considering local module "
            r9.append(r8)     // Catch:{ all -> 0x0121 }
            r9.append(r12)     // Catch:{ all -> 0x0121 }
            r9.append(r0)     // Catch:{ all -> 0x0121 }
            r9.append(r6)     // Catch:{ all -> 0x0121 }
            java.lang.String r6 = " and remote module "
            r9.append(r6)     // Catch:{ all -> 0x0121 }
            r9.append(r12)     // Catch:{ all -> 0x0121 }
            r9.append(r0)     // Catch:{ all -> 0x0121 }
            r9.append(r7)     // Catch:{ all -> 0x0121 }
            java.lang.String r0 = r9.toString()     // Catch:{ all -> 0x0121 }
            android.util.Log.i(r1, r0)     // Catch:{ all -> 0x0121 }
            int r0 = r5.zzhdu     // Catch:{ all -> 0x0121 }
            if (r0 == 0) goto L_0x00f7
            int r0 = r5.zzhdu     // Catch:{ all -> 0x0121 }
            r6 = -1
            if (r0 != r6) goto L_0x0069
            int r0 = r5.zzhds     // Catch:{ all -> 0x0121 }
            if (r0 == 0) goto L_0x00f7
        L_0x0069:
            int r0 = r5.zzhdu     // Catch:{ all -> 0x0121 }
            r7 = 1
            if (r0 != r7) goto L_0x0072
            int r0 = r5.zzhdt     // Catch:{ all -> 0x0121 }
            if (r0 == 0) goto L_0x00f7
        L_0x0072:
            int r0 = r5.zzhdu     // Catch:{ all -> 0x0121 }
            if (r0 != r6) goto L_0x0089
            com.google.android.gms.dynamite.DynamiteModule r10 = zzz(r10, r12)     // Catch:{ all -> 0x0121 }
            android.database.Cursor r11 = r3.zzhdp
            if (r11 == 0) goto L_0x0083
        L_0x007e:
            android.database.Cursor r11 = r3.zzhdp
            r11.close()
        L_0x0083:
            java.lang.ThreadLocal<com.google.android.gms.dynamite.DynamiteModule$zza> r11 = zzhdg
            r11.set(r2)
            return r10
        L_0x0089:
            int r0 = r5.zzhdu     // Catch:{ all -> 0x0121 }
            if (r0 != r7) goto L_0x00dc
            int r0 = r5.zzhdt     // Catch:{ zzc -> 0x0098 }
            com.google.android.gms.dynamite.DynamiteModule r10 = zza((android.content.Context) r10, (java.lang.String) r12, (int) r0)     // Catch:{ zzc -> 0x0098 }
            android.database.Cursor r11 = r3.zzhdp
            if (r11 == 0) goto L_0x0083
            goto L_0x007e
        L_0x0098:
            r0 = move-exception
            java.lang.String r7 = "Failed to load remote module: "
            java.lang.String r8 = r0.getMessage()     // Catch:{ all -> 0x0121 }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x0121 }
            int r9 = r8.length()     // Catch:{ all -> 0x0121 }
            if (r9 == 0) goto L_0x00ae
            java.lang.String r7 = r7.concat(r8)     // Catch:{ all -> 0x0121 }
            goto L_0x00b4
        L_0x00ae:
            java.lang.String r8 = new java.lang.String     // Catch:{ all -> 0x0121 }
            r8.<init>(r7)     // Catch:{ all -> 0x0121 }
            r7 = r8
        L_0x00b4:
            android.util.Log.w(r1, r7)     // Catch:{ all -> 0x0121 }
            int r1 = r5.zzhds     // Catch:{ all -> 0x0121 }
            if (r1 == 0) goto L_0x00d4
            com.google.android.gms.dynamite.DynamiteModule$zzb r1 = new com.google.android.gms.dynamite.DynamiteModule$zzb     // Catch:{ all -> 0x0121 }
            int r5 = r5.zzhds     // Catch:{ all -> 0x0121 }
            r7 = 0
            r1.<init>(r5, r7)     // Catch:{ all -> 0x0121 }
            com.google.android.gms.dynamite.zzj r11 = r11.zza(r10, r12, r1)     // Catch:{ all -> 0x0121 }
            int r11 = r11.zzhdu     // Catch:{ all -> 0x0121 }
            if (r11 != r6) goto L_0x00d4
            com.google.android.gms.dynamite.DynamiteModule r10 = zzz(r10, r12)     // Catch:{ all -> 0x0121 }
            android.database.Cursor r11 = r3.zzhdp
            if (r11 == 0) goto L_0x0083
            goto L_0x007e
        L_0x00d4:
            com.google.android.gms.dynamite.DynamiteModule$zzc r10 = new com.google.android.gms.dynamite.DynamiteModule$zzc     // Catch:{ all -> 0x0121 }
            java.lang.String r11 = "Remote load failed. No local fallback found."
            r10.<init>(r11, r0, r4)     // Catch:{ all -> 0x0121 }
            throw r10     // Catch:{ all -> 0x0121 }
        L_0x00dc:
            com.google.android.gms.dynamite.DynamiteModule$zzc r10 = new com.google.android.gms.dynamite.DynamiteModule$zzc     // Catch:{ all -> 0x0121 }
            int r11 = r5.zzhdu     // Catch:{ all -> 0x0121 }
            r12 = 47
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x0121 }
            r0.<init>(r12)     // Catch:{ all -> 0x0121 }
            java.lang.String r12 = "VersionPolicy returned invalid code:"
            r0.append(r12)     // Catch:{ all -> 0x0121 }
            r0.append(r11)     // Catch:{ all -> 0x0121 }
            java.lang.String r11 = r0.toString()     // Catch:{ all -> 0x0121 }
            r10.<init>((java.lang.String) r11, (com.google.android.gms.dynamite.zza) r4)     // Catch:{ all -> 0x0121 }
            throw r10     // Catch:{ all -> 0x0121 }
        L_0x00f7:
            com.google.android.gms.dynamite.DynamiteModule$zzc r10 = new com.google.android.gms.dynamite.DynamiteModule$zzc     // Catch:{ all -> 0x0121 }
            int r11 = r5.zzhds     // Catch:{ all -> 0x0121 }
            int r12 = r5.zzhdt     // Catch:{ all -> 0x0121 }
            r0 = 91
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0121 }
            r1.<init>(r0)     // Catch:{ all -> 0x0121 }
            java.lang.String r0 = "No acceptable module found. Local version is "
            r1.append(r0)     // Catch:{ all -> 0x0121 }
            r1.append(r11)     // Catch:{ all -> 0x0121 }
            java.lang.String r11 = " and remote version is "
            r1.append(r11)     // Catch:{ all -> 0x0121 }
            r1.append(r12)     // Catch:{ all -> 0x0121 }
            java.lang.String r11 = "."
            r1.append(r11)     // Catch:{ all -> 0x0121 }
            java.lang.String r11 = r1.toString()     // Catch:{ all -> 0x0121 }
            r10.<init>((java.lang.String) r11, (com.google.android.gms.dynamite.zza) r4)     // Catch:{ all -> 0x0121 }
            throw r10     // Catch:{ all -> 0x0121 }
        L_0x0121:
            r10 = move-exception
            android.database.Cursor r11 = r3.zzhdp
            if (r11 == 0) goto L_0x012b
            android.database.Cursor r11 = r3.zzhdp
            r11.close()
        L_0x012b:
            java.lang.ThreadLocal<com.google.android.gms.dynamite.DynamiteModule$zza> r11 = zzhdg
            r11.set(r2)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.dynamite.DynamiteModule.zza(android.content.Context, com.google.android.gms.dynamite.DynamiteModule$zzd, java.lang.String):com.google.android.gms.dynamite.DynamiteModule");
    }

    private static DynamiteModule zza(Context context, String str, int i) throws zzc {
        Boolean bool;
        synchronized (DynamiteModule.class) {
            bool = zzhdc;
        }
        if (bool != null) {
            return bool.booleanValue() ? zzc(context, str, i) : zzb(context, str, i);
        }
        throw new zzc("Failed to determine which loading route to use.", (zza) null);
    }

    private static void zza(ClassLoader classLoader) throws zzc {
        zzm zzm;
        try {
            IBinder iBinder = (IBinder) classLoader.loadClass("com.google.android.gms.dynamiteloader.DynamiteLoaderV2").getConstructor(new Class[0]).newInstance(new Object[0]);
            if (iBinder == null) {
                zzm = null;
            } else {
                IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.dynamite.IDynamiteLoaderV2");
                zzm = queryLocalInterface instanceof zzm ? (zzm) queryLocalInterface : new zzn(iBinder);
            }
            zzhde = zzm;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new zzc("Failed to instantiate dynamite loader", e, (zza) null);
        }
    }

    private static DynamiteModule zzb(Context context, String str, int i) throws zzc {
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 51);
        sb.append("Selected remote version of ");
        sb.append(str);
        sb.append(", version >= ");
        sb.append(i);
        Log.i("DynamiteModule", sb.toString());
        zzk zzdh = zzdh(context);
        if (zzdh != null) {
            try {
                IObjectWrapper zza2 = zzdh.zza(zzn.zzz(context), str, i);
                if (zzn.zzy(zza2) != null) {
                    return new DynamiteModule((Context) zzn.zzy(zza2));
                }
                throw new zzc("Failed to load remote module.", (zza) null);
            } catch (RemoteException e) {
                throw new zzc("Failed to load remote module.", e, (zza) null);
            }
        } else {
            throw new zzc("Failed to create IDynamiteLoader.", (zza) null);
        }
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:18:0x0038=Splitter:B:18:0x0038, B:35:0x007c=Splitter:B:35:0x007c} */
    public static int zzc(android.content.Context r8, java.lang.String r9, boolean r10) {
        /*
            java.lang.Class<com.google.android.gms.dynamite.DynamiteModule> r0 = com.google.android.gms.dynamite.DynamiteModule.class
            monitor-enter(r0)
            java.lang.Boolean r1 = zzhdc     // Catch:{ all -> 0x00ea }
            if (r1 != 0) goto L_0x00b7
            android.content.Context r1 = r8.getApplicationContext()     // Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x008e }
            java.lang.ClassLoader r1 = r1.getClassLoader()     // Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x008e }
            java.lang.Class<com.google.android.gms.dynamite.DynamiteModule$DynamiteLoaderClassLoader> r2 = com.google.android.gms.dynamite.DynamiteModule.DynamiteLoaderClassLoader.class
            java.lang.String r2 = r2.getName()     // Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x008e }
            java.lang.Class r1 = r1.loadClass(r2)     // Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x008e }
            java.lang.String r2 = "sClassLoader"
            java.lang.reflect.Field r2 = r1.getDeclaredField(r2)     // Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x008e }
            monitor-enter(r1)     // Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x008e }
            r3 = 0
            java.lang.Object r4 = r2.get(r3)     // Catch:{ all -> 0x008b }
            java.lang.ClassLoader r4 = (java.lang.ClassLoader) r4     // Catch:{ all -> 0x008b }
            if (r4 == 0) goto L_0x003b
            java.lang.ClassLoader r2 = java.lang.ClassLoader.getSystemClassLoader()     // Catch:{ all -> 0x008b }
            if (r4 != r2) goto L_0x0033
        L_0x0030:
            java.lang.Boolean r2 = java.lang.Boolean.FALSE     // Catch:{ all -> 0x008b }
            goto L_0x0088
        L_0x0033:
            zza(r4)     // Catch:{ zzc -> 0x0037 }
            goto L_0x0038
        L_0x0037:
            r2 = move-exception
        L_0x0038:
            java.lang.Boolean r2 = java.lang.Boolean.TRUE     // Catch:{ all -> 0x008b }
            goto L_0x0088
        L_0x003b:
            java.lang.String r4 = "com.google.android.gms"
            android.content.Context r5 = r8.getApplicationContext()     // Catch:{ all -> 0x008b }
            java.lang.String r5 = r5.getPackageName()     // Catch:{ all -> 0x008b }
            boolean r4 = r4.equals(r5)     // Catch:{ all -> 0x008b }
            if (r4 == 0) goto L_0x0053
            java.lang.ClassLoader r4 = java.lang.ClassLoader.getSystemClassLoader()     // Catch:{ all -> 0x008b }
            r2.set(r3, r4)     // Catch:{ all -> 0x008b }
            goto L_0x0030
        L_0x0053:
            int r4 = zze(r8, r9, r10)     // Catch:{ zzc -> 0x007f }
            java.lang.String r5 = zzhdf     // Catch:{ zzc -> 0x007f }
            if (r5 == 0) goto L_0x007c
            java.lang.String r5 = zzhdf     // Catch:{ zzc -> 0x007f }
            boolean r5 = r5.isEmpty()     // Catch:{ zzc -> 0x007f }
            if (r5 == 0) goto L_0x0064
            goto L_0x007c
        L_0x0064:
            com.google.android.gms.dynamite.zzh r5 = new com.google.android.gms.dynamite.zzh     // Catch:{ zzc -> 0x007f }
            java.lang.String r6 = zzhdf     // Catch:{ zzc -> 0x007f }
            java.lang.ClassLoader r7 = java.lang.ClassLoader.getSystemClassLoader()     // Catch:{ zzc -> 0x007f }
            r5.<init>(r6, r7)     // Catch:{ zzc -> 0x007f }
            zza(r5)     // Catch:{ zzc -> 0x007f }
            r2.set(r3, r5)     // Catch:{ zzc -> 0x007f }
            java.lang.Boolean r5 = java.lang.Boolean.TRUE     // Catch:{ zzc -> 0x007f }
            zzhdc = r5     // Catch:{ zzc -> 0x007f }
            monitor-exit(r1)     // Catch:{ all -> 0x008b }
            monitor-exit(r0)     // Catch:{ all -> 0x00ea }
            return r4
        L_0x007c:
            monitor-exit(r1)     // Catch:{ all -> 0x008b }
            monitor-exit(r0)     // Catch:{ all -> 0x00ea }
            return r4
        L_0x007f:
            r4 = move-exception
            java.lang.ClassLoader r4 = java.lang.ClassLoader.getSystemClassLoader()     // Catch:{ all -> 0x008b }
            r2.set(r3, r4)     // Catch:{ all -> 0x008b }
            goto L_0x0030
        L_0x0088:
            monitor-exit(r1)     // Catch:{ all -> 0x008b }
            r1 = r2
            goto L_0x00b5
        L_0x008b:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x008b }
            throw r2     // Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x008e }
        L_0x008e:
            r1 = move-exception
            java.lang.String r2 = "DynamiteModule"
            java.lang.String r1 = java.lang.String.valueOf(r1)     // Catch:{ all -> 0x00ea }
            java.lang.String r3 = java.lang.String.valueOf(r1)     // Catch:{ all -> 0x00ea }
            int r3 = r3.length()     // Catch:{ all -> 0x00ea }
            int r3 = r3 + 30
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00ea }
            r4.<init>(r3)     // Catch:{ all -> 0x00ea }
            java.lang.String r3 = "Failed to load module via V2: "
            r4.append(r3)     // Catch:{ all -> 0x00ea }
            r4.append(r1)     // Catch:{ all -> 0x00ea }
            java.lang.String r1 = r4.toString()     // Catch:{ all -> 0x00ea }
            android.util.Log.w(r2, r1)     // Catch:{ all -> 0x00ea }
            java.lang.Boolean r1 = java.lang.Boolean.FALSE     // Catch:{ all -> 0x00ea }
        L_0x00b5:
            zzhdc = r1     // Catch:{ all -> 0x00ea }
        L_0x00b7:
            monitor-exit(r0)     // Catch:{ all -> 0x00ea }
            boolean r0 = r1.booleanValue()
            if (r0 == 0) goto L_0x00e5
            int r8 = zze(r8, r9, r10)     // Catch:{ zzc -> 0x00c3 }
            return r8
        L_0x00c3:
            r8 = move-exception
            java.lang.String r9 = "Failed to retrieve remote module version: "
            java.lang.String r8 = r8.getMessage()
            java.lang.String r8 = java.lang.String.valueOf(r8)
            int r10 = r8.length()
            if (r10 == 0) goto L_0x00d9
            java.lang.String r8 = r9.concat(r8)
            goto L_0x00de
        L_0x00d9:
            java.lang.String r8 = new java.lang.String
            r8.<init>(r9)
        L_0x00de:
            java.lang.String r9 = "DynamiteModule"
            android.util.Log.w(r9, r8)
            r8 = 0
            return r8
        L_0x00e5:
            int r8 = zzd(r8, r9, r10)
            return r8
        L_0x00ea:
            r8 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x00ea }
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.dynamite.DynamiteModule.zzc(android.content.Context, java.lang.String, boolean):int");
    }

    private static DynamiteModule zzc(Context context, String str, int i) throws zzc {
        zzm zzm;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 51);
        sb.append("Selected remote version of ");
        sb.append(str);
        sb.append(", version >= ");
        sb.append(i);
        Log.i("DynamiteModule", sb.toString());
        synchronized (DynamiteModule.class) {
            zzm = zzhde;
        }
        if (zzm != null) {
            zza zza2 = zzhdg.get();
            if (zza2 == null || zza2.zzhdp == null) {
                throw new zzc("No result cursor", (zza) null);
            }
            Context zza3 = zza(context.getApplicationContext(), str, i, zza2.zzhdp, zzm);
            if (zza3 != null) {
                return new DynamiteModule(zza3);
            }
            throw new zzc("Failed to get module context", (zza) null);
        }
        throw new zzc("DynamiteLoaderV2 was not cached.", (zza) null);
    }

    private static int zzd(Context context, String str, boolean z) {
        zzk zzdh = zzdh(context);
        if (zzdh == null) {
            return 0;
        }
        try {
            return zzdh.zza(zzn.zzz(context), str, z);
        } catch (RemoteException e) {
            String valueOf = String.valueOf(e.getMessage());
            Log.w("DynamiteModule", valueOf.length() != 0 ? "Failed to retrieve remote module version: ".concat(valueOf) : new String("Failed to retrieve remote module version: "));
            return 0;
        }
    }

    private static zzk zzdh(Context context) {
        zzk zzk;
        synchronized (DynamiteModule.class) {
            if (zzhdd != null) {
                zzk zzk2 = zzhdd;
                return zzk2;
            } else if (zzf.zzahf().isGooglePlayServicesAvailable(context) != 0) {
                return null;
            } else {
                try {
                    IBinder iBinder = (IBinder) context.createPackageContext("com.google.android.gms", 3).getClassLoader().loadClass("com.google.android.gms.chimera.container.DynamiteLoaderImpl").newInstance();
                    if (iBinder == null) {
                        zzk = null;
                    } else {
                        IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.dynamite.IDynamiteLoader");
                        zzk = queryLocalInterface instanceof zzk ? (zzk) queryLocalInterface : new zzl(iBinder);
                    }
                    if (zzk != null) {
                        zzhdd = zzk;
                        return zzk;
                    }
                } catch (Exception e) {
                    String valueOf = String.valueOf(e.getMessage());
                    Log.e("DynamiteModule", valueOf.length() != 0 ? "Failed to load IDynamiteLoader from GmsCore: ".concat(valueOf) : new String("Failed to load IDynamiteLoader from GmsCore: "));
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00a6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int zze(android.content.Context r8, java.lang.String r9, boolean r10) throws com.google.android.gms.dynamite.DynamiteModule.zzc {
        /*
            r0 = 0
            android.content.ContentResolver r1 = r8.getContentResolver()     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            if (r10 == 0) goto L_0x000a
            java.lang.String r8 = "api_force_staging"
            goto L_0x000c
        L_0x000a:
            java.lang.String r8 = "api"
        L_0x000c:
            java.lang.String r10 = java.lang.String.valueOf(r8)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            int r10 = r10.length()     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            int r10 = r10 + 42
            java.lang.String r2 = java.lang.String.valueOf(r9)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            int r2 = r2.length()     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            int r10 = r10 + r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            r2.<init>(r10)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            java.lang.String r10 = "content://com.google.android.gms.chimera/"
            r2.append(r10)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            r2.append(r8)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            java.lang.String r8 = "/"
            r2.append(r8)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            r2.append(r9)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            java.lang.String r8 = r2.toString()     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            android.net.Uri r2 = android.net.Uri.parse(r8)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            r3 = 0
            r4 = 0
            r5 = 0
            r6 = 0
            android.database.Cursor r8 = r1.query(r2, r3, r4, r5, r6)     // Catch:{ Exception -> 0x0093, all -> 0x0091 }
            if (r8 == 0) goto L_0x0079
            boolean r9 = r8.moveToFirst()     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            if (r9 == 0) goto L_0x0079
            r9 = 0
            int r9 = r8.getInt(r9)     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            if (r9 <= 0) goto L_0x0072
            java.lang.Class<com.google.android.gms.dynamite.DynamiteModule> r10 = com.google.android.gms.dynamite.DynamiteModule.class
            monitor-enter(r10)     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            r1 = 2
            java.lang.String r1 = r8.getString(r1)     // Catch:{ all -> 0x006f }
            zzhdf = r1     // Catch:{ all -> 0x006f }
            monitor-exit(r10)     // Catch:{ all -> 0x006f }
            java.lang.ThreadLocal<com.google.android.gms.dynamite.DynamiteModule$zza> r10 = zzhdg     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            java.lang.Object r10 = r10.get()     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            com.google.android.gms.dynamite.DynamiteModule$zza r10 = (com.google.android.gms.dynamite.DynamiteModule.zza) r10     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            if (r10 == 0) goto L_0x0072
            android.database.Cursor r1 = r10.zzhdp     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            if (r1 != 0) goto L_0x0072
            r10.zzhdp = r8     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            goto L_0x0073
        L_0x006f:
            r9 = move-exception
            monitor-exit(r10)     // Catch:{ all -> 0x006f }
            throw r9     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
        L_0x0072:
            r0 = r8
        L_0x0073:
            if (r0 == 0) goto L_0x0078
            r0.close()
        L_0x0078:
            return r9
        L_0x0079:
            java.lang.String r9 = "DynamiteModule"
            java.lang.String r10 = "Failed to retrieve remote module version."
            android.util.Log.w(r9, r10)     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            com.google.android.gms.dynamite.DynamiteModule$zzc r9 = new com.google.android.gms.dynamite.DynamiteModule$zzc     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            java.lang.String r10 = "Failed to connect to dynamite module ContentResolver."
            r9.<init>((java.lang.String) r10, (com.google.android.gms.dynamite.zza) r0)     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
            throw r9     // Catch:{ Exception -> 0x008c, all -> 0x0088 }
        L_0x0088:
            r9 = move-exception
            r0 = r8
            r8 = r9
            goto L_0x00a4
        L_0x008c:
            r9 = move-exception
            r7 = r9
            r9 = r8
            r8 = r7
            goto L_0x0095
        L_0x0091:
            r8 = move-exception
            goto L_0x00a4
        L_0x0093:
            r8 = move-exception
            r9 = r0
        L_0x0095:
            boolean r10 = r8 instanceof com.google.android.gms.dynamite.DynamiteModule.zzc     // Catch:{ all -> 0x00a2 }
            if (r10 == 0) goto L_0x009a
            throw r8     // Catch:{ all -> 0x00a2 }
        L_0x009a:
            com.google.android.gms.dynamite.DynamiteModule$zzc r10 = new com.google.android.gms.dynamite.DynamiteModule$zzc     // Catch:{ all -> 0x00a2 }
            java.lang.String r1 = "V2 version check failed"
            r10.<init>(r1, r8, r0)     // Catch:{ all -> 0x00a2 }
            throw r10     // Catch:{ all -> 0x00a2 }
        L_0x00a2:
            r8 = move-exception
            r0 = r9
        L_0x00a4:
            if (r0 == 0) goto L_0x00a9
            r0.close()
        L_0x00a9:
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.dynamite.DynamiteModule.zze(android.content.Context, java.lang.String, boolean):int");
    }

    public static int zzx(Context context, String str) {
        try {
            ClassLoader classLoader = context.getApplicationContext().getClassLoader();
            StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 61);
            sb.append("com.google.android.gms.dynamite.descriptors.");
            sb.append(str);
            sb.append(".ModuleDescriptor");
            Class<?> loadClass = classLoader.loadClass(sb.toString());
            Field declaredField = loadClass.getDeclaredField("MODULE_ID");
            Field declaredField2 = loadClass.getDeclaredField("MODULE_VERSION");
            if (declaredField.get((Object) null).equals(str)) {
                return declaredField2.getInt((Object) null);
            }
            String valueOf = String.valueOf(declaredField.get((Object) null));
            StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf).length() + 51 + String.valueOf(str).length());
            sb2.append("Module descriptor id '");
            sb2.append(valueOf);
            sb2.append("' didn't match expected id '");
            sb2.append(str);
            sb2.append("'");
            Log.e("DynamiteModule", sb2.toString());
            return 0;
        } catch (ClassNotFoundException e) {
            StringBuilder sb3 = new StringBuilder(String.valueOf(str).length() + 45);
            sb3.append("Local module descriptor class for ");
            sb3.append(str);
            sb3.append(" not found.");
            Log.w("DynamiteModule", sb3.toString());
            return 0;
        } catch (Exception e2) {
            String valueOf2 = String.valueOf(e2.getMessage());
            Log.e("DynamiteModule", valueOf2.length() != 0 ? "Failed to load module descriptor class: ".concat(valueOf2) : new String("Failed to load module descriptor class: "));
            return 0;
        }
    }

    public static int zzy(Context context, String str) {
        return zzc(context, str, false);
    }

    private static DynamiteModule zzz(Context context, String str) {
        String valueOf = String.valueOf(str);
        Log.i("DynamiteModule", valueOf.length() != 0 ? "Selected local version of ".concat(valueOf) : new String("Selected local version of "));
        return new DynamiteModule(context.getApplicationContext());
    }

    public final Context zzarl() {
        return this.zzhdo;
    }

    public final IBinder zzhk(String str) throws zzc {
        try {
            return (IBinder) this.zzhdo.getClassLoader().loadClass(str).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            String valueOf = String.valueOf(str);
            throw new zzc(valueOf.length() != 0 ? "Failed to instantiate module class: ".concat(valueOf) : new String("Failed to instantiate module class: "), e, (zza) null);
        }
    }
}
