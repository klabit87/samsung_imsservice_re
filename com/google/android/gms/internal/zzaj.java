package com.google.android.gms.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.xbill.DNS.KEYRecord;

public class zzaj implements zzm {
    private static boolean DEBUG = zzaf.DEBUG;
    @Deprecated
    private zzar zzbo;
    private final zzai zzbp;
    private zzak zzbq;

    public zzaj(zzai zzai) {
        this(zzai, new zzak(KEYRecord.Flags.EXTEND));
    }

    private zzaj(zzai zzai, zzak zzak) {
        this.zzbp = zzai;
        this.zzbo = zzai;
        this.zzbq = zzak;
    }

    @Deprecated
    public zzaj(zzar zzar) {
        this(zzar, new zzak(KEYRecord.Flags.EXTEND));
    }

    @Deprecated
    private zzaj(zzar zzar, zzak zzak) {
        this.zzbo = zzar;
        this.zzbp = new zzah(zzar);
        this.zzbq = zzak;
    }

    private static List<zzl> zza(List<zzl> list, zzc zzc) {
        TreeSet treeSet = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        if (!list.isEmpty()) {
            for (zzl name : list) {
                treeSet.add(name.getName());
            }
        }
        ArrayList arrayList = new ArrayList(list);
        if (zzc.zzg != null) {
            if (!zzc.zzg.isEmpty()) {
                for (zzl next : zzc.zzg) {
                    if (!treeSet.contains(next.getName())) {
                        arrayList.add(next);
                    }
                }
            }
        } else if (!zzc.zzf.isEmpty()) {
            for (Map.Entry next2 : zzc.zzf.entrySet()) {
                if (!treeSet.contains(next2.getKey())) {
                    arrayList.add(new zzl((String) next2.getKey(), (String) next2.getValue()));
                }
            }
        }
        return arrayList;
    }

    private static void zza(String str, zzr<?> zzr, zzae zzae) throws zzae {
        zzab zzi = zzr.zzi();
        int zzh = zzr.zzh();
        try {
            zzi.zza(zzae);
            zzr.zzb(String.format("%s-retry [timeout=%s]", new Object[]{str, Integer.valueOf(zzh)}));
        } catch (zzae e) {
            zzr.zzb(String.format("%s-timeout-giveup [timeout=%s]", new Object[]{str, Integer.valueOf(zzh)}));
            throw e;
        }
    }

    private final byte[] zza(InputStream inputStream, int i) throws IOException, zzac {
        zzau zzau = new zzau(this.zzbq, i);
        byte[] bArr = null;
        if (inputStream != null) {
            try {
                bArr = this.zzbq.zzb(1024);
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    zzau.write(bArr, 0, read);
                }
                return zzau.toByteArray();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        zzaf.zza("Error occurred when closing InputStream", new Object[0]);
                    }
                }
                this.zzbq.zza(bArr);
                zzau.close();
            }
        } else {
            throw new zzac();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00fc, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00fd, code lost:
        r17 = r5;
        r13 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0102, code lost:
        r0 = r8.getStatusCode();
        com.google.android.gms.internal.zzaf.zzc("Unexpected response code %d for %s", java.lang.Integer.valueOf(r0), r27.getUrl());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0119, code lost:
        if (r13 != null) goto L_0x011b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x011b, code lost:
        r11 = new com.google.android.gms.internal.zzp(r0, r13, false, android.os.SystemClock.elapsedRealtime() - r3, (java.util.List<com.google.android.gms.internal.zzl>) r17);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x012b, code lost:
        if (r0 == 401) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0134, code lost:
        if (r0 < 400) goto L_0x0141;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0140, code lost:
        throw new com.google.android.gms.internal.zzg(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0143, code lost:
        if (r0 < 500) goto L_0x014f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x014e, code lost:
        throw new com.google.android.gms.internal.zzac(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0154, code lost:
        throw new com.google.android.gms.internal.zzac(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0155, code lost:
        r0 = new com.google.android.gms.internal.zza(r11);
        r5 = "auth";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x015d, code lost:
        r0 = new com.google.android.gms.internal.zzo();
        r5 = "network";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x016a, code lost:
        throw new com.google.android.gms.internal.zzq(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x016b, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x016c, code lost:
        r2 = java.lang.String.valueOf(r27.getUrl());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x017c, code lost:
        if (r2.length() != 0) goto L_0x017e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x017e, code lost:
        r2 = "Bad URL ".concat(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0183, code lost:
        r2 = new java.lang.String("Bad URL ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x018b, code lost:
        throw new java.lang.RuntimeException(r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x018d, code lost:
        r0 = new com.google.android.gms.internal.zzad();
        r5 = "socket";
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0102  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x016b A[ExcHandler: MalformedURLException (r0v3 'e' java.net.MalformedURLException A[CUSTOM_DECLARE]), Splitter:B:2:0x0010] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x018c A[ExcHandler: SocketTimeoutException (e java.net.SocketTimeoutException), Splitter:B:2:0x0010] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0165 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.google.android.gms.internal.zzp zzc(com.google.android.gms.internal.zzr<?> r27) throws com.google.android.gms.internal.zzae {
        /*
            r26 = this;
            r1 = r26
            r2 = r27
            long r3 = android.os.SystemClock.elapsedRealtime()
        L_0x0008:
            java.util.List r5 = java.util.Collections.emptyList()
            r6 = 1
            r7 = 2
            r8 = 0
            r9 = 0
            com.google.android.gms.internal.zzc r0 = r27.zze()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            if (r0 != 0) goto L_0x001b
            java.util.Map r0 = java.util.Collections.emptyMap()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            goto L_0x003f
        L_0x001b:
            java.util.HashMap r10 = new java.util.HashMap     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            r10.<init>()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            java.lang.String r11 = r0.zza     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            if (r11 == 0) goto L_0x002b
            java.lang.String r11 = "If-None-Match"
            java.lang.String r12 = r0.zza     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            r10.put(r11, r12)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
        L_0x002b:
            long r11 = r0.zzc     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            r13 = 0
            int r11 = (r11 > r13 ? 1 : (r11 == r13 ? 0 : -1))
            if (r11 <= 0) goto L_0x003e
            java.lang.String r11 = "If-Modified-Since"
            long r12 = r0.zzc     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            java.lang.String r0 = com.google.android.gms.internal.zzap.zzb((long) r12)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            r10.put(r11, r0)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
        L_0x003e:
            r0 = r10
        L_0x003f:
            com.google.android.gms.internal.zzai r10 = r1.zzbp     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            com.google.android.gms.internal.zzaq r10 = r10.zza(r2, r0)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00fc }
            int r12 = r10.getStatusCode()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            java.util.List r5 = r10.zzp()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r0 = 304(0x130, float:4.26E-43)
            if (r12 != r0) goto L_0x0085
            com.google.android.gms.internal.zzc r0 = r27.zze()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            if (r0 != 0) goto L_0x006b
            com.google.android.gms.internal.zzp r0 = new com.google.android.gms.internal.zzp     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r14 = 304(0x130, float:4.26E-43)
            r15 = 0
            r16 = 1
            long r11 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            long r17 = r11 - r3
            r13 = r0
            r19 = r5
            r13.<init>((int) r14, (byte[]) r15, (boolean) r16, (long) r17, (java.util.List<com.google.android.gms.internal.zzl>) r19)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            return r0
        L_0x006b:
            java.util.List r25 = zza((java.util.List<com.google.android.gms.internal.zzl>) r5, (com.google.android.gms.internal.zzc) r0)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            com.google.android.gms.internal.zzp r11 = new com.google.android.gms.internal.zzp     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r20 = 304(0x130, float:4.26E-43)
            byte[] r0 = r0.data     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r22 = 1
            long r12 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            long r23 = r12 - r3
            r19 = r11
            r21 = r0
            r19.<init>((int) r20, (byte[]) r21, (boolean) r22, (long) r23, (java.util.List<com.google.android.gms.internal.zzl>) r25)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            return r11
        L_0x0085:
            java.io.InputStream r0 = r10.getContent()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            if (r0 == 0) goto L_0x0094
            int r11 = r10.getContentLength()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            byte[] r0 = r1.zza((java.io.InputStream) r0, (int) r11)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            goto L_0x0096
        L_0x0094:
            byte[] r0 = new byte[r9]     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
        L_0x0096:
            r8 = r0
            long r13 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            long r13 = r13 - r3
            boolean r0 = DEBUG     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            if (r0 != 0) goto L_0x00a6
            r15 = 3000(0xbb8, double:1.482E-320)
            int r0 = (r13 > r15 ? 1 : (r13 == r15 ? 0 : -1))
            if (r0 <= 0) goto L_0x00d8
        L_0x00a6:
            java.lang.String r0 = "HTTP response for request=<%s> [lifetime=%d], [size=%s], [rc=%d], [retryCount=%s]"
            r11 = 5
            java.lang.Object[] r11 = new java.lang.Object[r11]     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r11[r9] = r2     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            java.lang.Long r13 = java.lang.Long.valueOf(r13)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r11[r6] = r13     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            if (r8 == 0) goto L_0x00bb
            int r13 = r8.length     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            goto L_0x00bd
        L_0x00bb:
            java.lang.String r13 = "null"
        L_0x00bd:
            r11[r7] = r13     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r13 = 3
            java.lang.Integer r14 = java.lang.Integer.valueOf(r12)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r11[r13] = r14     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r13 = 4
            com.google.android.gms.internal.zzab r14 = r27.zzi()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            int r14 = r14.zzc()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r11[r13] = r14     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            com.google.android.gms.internal.zzaf.zzb(r0, r11)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
        L_0x00d8:
            r0 = 200(0xc8, float:2.8E-43)
            if (r12 < r0) goto L_0x00f0
            r0 = 299(0x12b, float:4.19E-43)
            if (r12 > r0) goto L_0x00f0
            com.google.android.gms.internal.zzp r0 = new com.google.android.gms.internal.zzp     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r14 = 0
            long r15 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            long r15 = r15 - r3
            r11 = r0
            r13 = r8
            r17 = r5
            r11.<init>((int) r12, (byte[]) r13, (boolean) r14, (long) r15, (java.util.List<com.google.android.gms.internal.zzl>) r17)     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            return r0
        L_0x00f0:
            java.io.IOException r0 = new java.io.IOException     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            r0.<init>()     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
            throw r0     // Catch:{ SocketTimeoutException -> 0x018c, MalformedURLException -> 0x016b, IOException -> 0x00f6 }
        L_0x00f6:
            r0 = move-exception
            r17 = r5
            r13 = r8
            r8 = r10
            goto L_0x0100
        L_0x00fc:
            r0 = move-exception
            r17 = r5
            r13 = r8
        L_0x0100:
            if (r8 == 0) goto L_0x0165
            int r0 = r8.getStatusCode()
            java.lang.Object[] r5 = new java.lang.Object[r7]
            java.lang.Integer r7 = java.lang.Integer.valueOf(r0)
            r5[r9] = r7
            java.lang.String r7 = r27.getUrl()
            r5[r6] = r7
            java.lang.String r6 = "Unexpected response code %d for %s"
            com.google.android.gms.internal.zzaf.zzc(r6, r5)
            if (r13 == 0) goto L_0x015d
            com.google.android.gms.internal.zzp r5 = new com.google.android.gms.internal.zzp
            r14 = 0
            long r6 = android.os.SystemClock.elapsedRealtime()
            long r15 = r6 - r3
            r11 = r5
            r12 = r0
            r11.<init>((int) r12, (byte[]) r13, (boolean) r14, (long) r15, (java.util.List<com.google.android.gms.internal.zzl>) r17)
            r6 = 401(0x191, float:5.62E-43)
            if (r0 == r6) goto L_0x0155
            r6 = 403(0x193, float:5.65E-43)
            if (r0 != r6) goto L_0x0132
            goto L_0x0155
        L_0x0132:
            r1 = 400(0x190, float:5.6E-43)
            if (r0 < r1) goto L_0x0141
            r1 = 499(0x1f3, float:6.99E-43)
            if (r0 <= r1) goto L_0x013b
            goto L_0x0141
        L_0x013b:
            com.google.android.gms.internal.zzg r0 = new com.google.android.gms.internal.zzg
            r0.<init>(r5)
            throw r0
        L_0x0141:
            r1 = 500(0x1f4, float:7.0E-43)
            if (r0 < r1) goto L_0x014f
            r1 = 599(0x257, float:8.4E-43)
            if (r0 > r1) goto L_0x014f
            com.google.android.gms.internal.zzac r0 = new com.google.android.gms.internal.zzac
            r0.<init>(r5)
            throw r0
        L_0x014f:
            com.google.android.gms.internal.zzac r0 = new com.google.android.gms.internal.zzac
            r0.<init>(r5)
            throw r0
        L_0x0155:
            com.google.android.gms.internal.zza r0 = new com.google.android.gms.internal.zza
            r0.<init>(r5)
            java.lang.String r5 = "auth"
            goto L_0x0195
        L_0x015d:
            com.google.android.gms.internal.zzo r0 = new com.google.android.gms.internal.zzo
            r0.<init>()
            java.lang.String r5 = "network"
            goto L_0x0195
        L_0x0165:
            com.google.android.gms.internal.zzq r1 = new com.google.android.gms.internal.zzq
            r1.<init>(r0)
            throw r1
        L_0x016b:
            r0 = move-exception
            java.lang.RuntimeException r1 = new java.lang.RuntimeException
            java.lang.String r3 = "Bad URL "
            java.lang.String r2 = r27.getUrl()
            java.lang.String r2 = java.lang.String.valueOf(r2)
            int r4 = r2.length()
            if (r4 == 0) goto L_0x0183
            java.lang.String r2 = r3.concat(r2)
            goto L_0x0188
        L_0x0183:
            java.lang.String r2 = new java.lang.String
            r2.<init>(r3)
        L_0x0188:
            r1.<init>(r2, r0)
            throw r1
        L_0x018c:
            r0 = move-exception
            com.google.android.gms.internal.zzad r0 = new com.google.android.gms.internal.zzad
            r0.<init>()
            java.lang.String r5 = "socket"
        L_0x0195:
            zza(r5, r2, r0)
            goto L_0x0008
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzaj.zzc(com.google.android.gms.internal.zzr):com.google.android.gms.internal.zzp");
    }
}
