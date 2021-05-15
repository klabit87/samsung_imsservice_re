package com.google.android.gms.internal;

import android.os.SystemClock;
import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class zzam implements zzb {
    private final Map<String, zzan> zzbw;
    private long zzbx;
    private final File zzby;
    private final int zzbz;

    public zzam(File file) {
        this(file, 5242880);
    }

    private zzam(File file, int i) {
        this.zzbw = new LinkedHashMap(16, 0.75f, true);
        this.zzbx = 0;
        this.zzby = file;
        this.zzbz = 5242880;
    }

    private final synchronized void remove(String str) {
        boolean delete = zze(str).delete();
        removeEntry(str);
        if (!delete) {
            zzaf.zzb("Could not delete cache entry for key=%s, filename=%s", str, zzd(str));
        }
    }

    private final void removeEntry(String str) {
        zzan remove = this.zzbw.remove(str);
        if (remove != null) {
            this.zzbx -= remove.zzca;
        }
    }

    private static int zza(InputStream inputStream) throws IOException {
        int read = inputStream.read();
        if (read != -1) {
            return read;
        }
        throw new EOFException();
    }

    private static InputStream zza(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    static String zza(zzao zzao) throws IOException {
        return new String(zza(zzao, zzc(zzao)), "UTF-8");
    }

    static void zza(OutputStream outputStream, int i) throws IOException {
        outputStream.write(i & 255);
        outputStream.write((i >> 8) & 255);
        outputStream.write((i >> 16) & 255);
        outputStream.write(i >>> 24);
    }

    static void zza(OutputStream outputStream, long j) throws IOException {
        outputStream.write((byte) ((int) j));
        outputStream.write((byte) ((int) (j >>> 8)));
        outputStream.write((byte) ((int) (j >>> 16)));
        outputStream.write((byte) ((int) (j >>> 24)));
        outputStream.write((byte) ((int) (j >>> 32)));
        outputStream.write((byte) ((int) (j >>> 40)));
        outputStream.write((byte) ((int) (j >>> 48)));
        outputStream.write((byte) ((int) (j >>> 56)));
    }

    static void zza(OutputStream outputStream, String str) throws IOException {
        byte[] bytes = str.getBytes("UTF-8");
        zza(outputStream, (long) bytes.length);
        outputStream.write(bytes, 0, bytes.length);
    }

    private final void zza(String str, zzan zzan) {
        if (!this.zzbw.containsKey(str)) {
            this.zzbx += zzan.zzca;
        } else {
            this.zzbx += zzan.zzca - this.zzbw.get(str).zzca;
        }
        this.zzbw.put(str, zzan);
    }

    private static byte[] zza(zzao zzao, long j) throws IOException {
        long zzn = zzao.zzn();
        if (j >= 0 && j <= zzn) {
            int i = (int) j;
            if (((long) i) == j) {
                byte[] bArr = new byte[i];
                new DataInputStream(zzao).readFully(bArr);
                return bArr;
            }
        }
        StringBuilder sb = new StringBuilder(73);
        sb.append("streamToBytes length=");
        sb.append(j);
        sb.append(", maxLength=");
        sb.append(zzn);
        throw new IOException(sb.toString());
    }

    static int zzb(InputStream inputStream) throws IOException {
        return (zza(inputStream) << 24) | zza(inputStream) | 0 | (zza(inputStream) << 8) | (zza(inputStream) << 16);
    }

    static List<zzl> zzb(zzao zzao) throws IOException {
        int zzb = zzb((InputStream) zzao);
        List<zzl> emptyList = zzb == 0 ? Collections.emptyList() : new ArrayList<>(zzb);
        for (int i = 0; i < zzb; i++) {
            emptyList.add(new zzl(zza(zzao).intern(), zza(zzao).intern()));
        }
        return emptyList;
    }

    static long zzc(InputStream inputStream) throws IOException {
        return (((long) zza(inputStream)) & 255) | 0 | ((((long) zza(inputStream)) & 255) << 8) | ((((long) zza(inputStream)) & 255) << 16) | ((((long) zza(inputStream)) & 255) << 24) | ((((long) zza(inputStream)) & 255) << 32) | ((((long) zza(inputStream)) & 255) << 40) | ((((long) zza(inputStream)) & 255) << 48) | ((255 & ((long) zza(inputStream))) << 56);
    }

    private static String zzd(String str) {
        int length = str.length() / 2;
        String valueOf = String.valueOf(String.valueOf(str.substring(0, length).hashCode()));
        String valueOf2 = String.valueOf(String.valueOf(str.substring(length).hashCode()));
        return valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf);
    }

    private final File zze(String str) {
        return new File(this.zzby, zzd(str));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0023, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized void initialize() {
        /*
            r9 = this;
            monitor-enter(r9)
            java.io.File r0 = r9.zzby     // Catch:{ all -> 0x0062 }
            boolean r0 = r0.exists()     // Catch:{ all -> 0x0062 }
            r1 = 0
            if (r0 != 0) goto L_0x0024
            java.io.File r0 = r9.zzby     // Catch:{ all -> 0x0062 }
            boolean r0 = r0.mkdirs()     // Catch:{ all -> 0x0062 }
            if (r0 != 0) goto L_0x0022
            java.lang.String r0 = "Unable to create cache dir %s"
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]     // Catch:{ all -> 0x0062 }
            java.io.File r3 = r9.zzby     // Catch:{ all -> 0x0062 }
            java.lang.String r3 = r3.getAbsolutePath()     // Catch:{ all -> 0x0062 }
            r2[r1] = r3     // Catch:{ all -> 0x0062 }
            com.google.android.gms.internal.zzaf.zzc(r0, r2)     // Catch:{ all -> 0x0062 }
        L_0x0022:
            monitor-exit(r9)
            return
        L_0x0024:
            java.io.File r0 = r9.zzby     // Catch:{ all -> 0x0062 }
            java.io.File[] r0 = r0.listFiles()     // Catch:{ all -> 0x0062 }
            if (r0 != 0) goto L_0x002e
            monitor-exit(r9)
            return
        L_0x002e:
            int r2 = r0.length     // Catch:{ all -> 0x0062 }
        L_0x002f:
            if (r1 >= r2) goto L_0x0060
            r3 = r0[r1]     // Catch:{ all -> 0x0062 }
            long r4 = r3.length()     // Catch:{ IOException -> 0x0059 }
            com.google.android.gms.internal.zzao r6 = new com.google.android.gms.internal.zzao     // Catch:{ IOException -> 0x0059 }
            java.io.BufferedInputStream r7 = new java.io.BufferedInputStream     // Catch:{ IOException -> 0x0059 }
            java.io.InputStream r8 = zza((java.io.File) r3)     // Catch:{ IOException -> 0x0059 }
            r7.<init>(r8)     // Catch:{ IOException -> 0x0059 }
            r6.<init>(r7, r4)     // Catch:{ IOException -> 0x0059 }
            com.google.android.gms.internal.zzan r7 = com.google.android.gms.internal.zzan.zzc(r6)     // Catch:{ all -> 0x0054 }
            r7.zzca = r4     // Catch:{ all -> 0x0054 }
            java.lang.String r4 = r7.key     // Catch:{ all -> 0x0054 }
            r9.zza((java.lang.String) r4, (com.google.android.gms.internal.zzan) r7)     // Catch:{ all -> 0x0054 }
            r6.close()     // Catch:{ IOException -> 0x0059 }
            goto L_0x005d
        L_0x0054:
            r4 = move-exception
            r6.close()     // Catch:{ IOException -> 0x0059 }
            throw r4     // Catch:{ IOException -> 0x0059 }
        L_0x0059:
            r4 = move-exception
            r3.delete()     // Catch:{ all -> 0x0062 }
        L_0x005d:
            int r1 = r1 + 1
            goto L_0x002f
        L_0x0060:
            monitor-exit(r9)
            return
        L_0x0062:
            r0 = move-exception
            monitor-exit(r9)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzam.initialize():void");
    }

    public final synchronized zzc zza(String str) {
        zzao zzao;
        zzan zzan = this.zzbw.get(str);
        if (zzan == null) {
            return null;
        }
        File zze = zze(str);
        try {
            zzao = new zzao(new BufferedInputStream(zza(zze)), zze.length());
            zzan zzc = zzan.zzc(zzao);
            if (!TextUtils.equals(str, zzc.key)) {
                zzaf.zzb("%s: key=%s, found=%s", zze.getAbsolutePath(), str, zzc.key);
                removeEntry(str);
                zzao.close();
                return null;
            }
            byte[] zza = zza(zzao, zzao.zzn());
            zzc zzc2 = new zzc();
            zzc2.data = zza;
            zzc2.zza = zzan.zza;
            zzc2.zzb = zzan.zzb;
            zzc2.zzc = zzan.zzc;
            zzc2.zzd = zzan.zzd;
            zzc2.zze = zzan.zze;
            zzc2.zzf = zzap.zza(zzan.zzg);
            zzc2.zzg = Collections.unmodifiableList(zzan.zzg);
            zzao.close();
            return zzc2;
        } catch (IOException e) {
            zzaf.zzb("%s: %s", zze.getAbsolutePath(), e.toString());
            remove(str);
            return null;
        } catch (Throwable th) {
            zzao.close();
            throw th;
        }
    }

    public final synchronized void zza(String str, zzc zzc) {
        String str2 = str;
        zzc zzc2 = zzc;
        synchronized (this) {
            long length = (long) zzc2.data.length;
            if (this.zzbx + length >= ((long) this.zzbz)) {
                if (zzaf.DEBUG) {
                    zzaf.zza("Pruning old cache entries.", new Object[0]);
                }
                long j = this.zzbx;
                long elapsedRealtime = SystemClock.elapsedRealtime();
                Iterator<Map.Entry<String, zzan>> it = this.zzbw.entrySet().iterator();
                int i = 0;
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    zzan zzan = (zzan) it.next().getValue();
                    if (zze(zzan.key).delete()) {
                        this.zzbx -= zzan.zzca;
                    } else {
                        zzaf.zzb("Could not delete cache entry for key=%s, filename=%s", zzan.key, zzd(zzan.key));
                    }
                    it.remove();
                    i++;
                    if (((float) (this.zzbx + length)) < ((float) this.zzbz) * 0.9f) {
                        break;
                    }
                }
                if (zzaf.DEBUG) {
                    zzaf.zza("pruned %d files, %d bytes, %d ms", Integer.valueOf(i), Long.valueOf(this.zzbx - j), Long.valueOf(SystemClock.elapsedRealtime() - elapsedRealtime));
                }
            }
            File zze = zze(str);
            try {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(zze));
                zzan zzan2 = new zzan(str2, zzc2);
                if (zzan2.zza(bufferedOutputStream)) {
                    bufferedOutputStream.write(zzc2.data);
                    bufferedOutputStream.close();
                    zza(str2, zzan2);
                } else {
                    bufferedOutputStream.close();
                    zzaf.zzb("Failed to write header for %s", zze.getAbsolutePath());
                    throw new IOException();
                }
            } catch (IOException e) {
                if (!zze.delete()) {
                    zzaf.zzb("Could not clean up file %s", zze.getAbsolutePath());
                }
            }
        }
    }
}
