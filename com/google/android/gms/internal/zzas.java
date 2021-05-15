package com.google.android.gms.internal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLSocketFactory;

public final class zzas extends zzai {
    private final zzat zzcg;
    private final SSLSocketFactory zzch;

    public zzas() {
        this((zzat) null);
    }

    private zzas(zzat zzat) {
        this((zzat) null, (SSLSocketFactory) null);
    }

    private zzas(zzat zzat, SSLSocketFactory sSLSocketFactory) {
        this.zzcg = null;
        this.zzch = null;
    }

    private static InputStream zza(HttpURLConnection httpURLConnection) {
        try {
            return httpURLConnection.getInputStream();
        } catch (IOException e) {
            return httpURLConnection.getErrorStream();
        }
    }

    private static void zza(HttpURLConnection httpURLConnection, zzr<?> zzr) throws IOException, zza {
        byte[] zzf = zzr.zzf();
        if (zzf != null) {
            httpURLConnection.setDoOutput(true);
            String valueOf = String.valueOf("UTF-8");
            httpURLConnection.addRequestProperty("Content-Type", valueOf.length() != 0 ? "application/x-www-form-urlencoded; charset=".concat(valueOf) : new String("application/x-www-form-urlencoded; charset="));
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.write(zzf);
            dataOutputStream.close();
        }
    }

    private static List<zzl> zzc(Map<String, List<String>> map) {
        ArrayList arrayList = new ArrayList(map.size());
        for (Map.Entry next : map.entrySet()) {
            if (next.getKey() != null) {
                for (String zzl : (List) next.getValue()) {
                    arrayList.add(new zzl((String) next.getKey(), zzl));
                }
            }
        }
        return arrayList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a9, code lost:
        r0.setRequestMethod(r8);
        zza(r0, r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00b2, code lost:
        r0.setRequestMethod(r8);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final com.google.android.gms.internal.zzaq zza(com.google.android.gms.internal.zzr<?> r7, java.util.Map<java.lang.String, java.lang.String> r8) throws java.io.IOException, com.google.android.gms.internal.zza {
        /*
            r6 = this;
            java.lang.String r0 = r7.getUrl()
            java.util.HashMap r1 = new java.util.HashMap
            r1.<init>()
            java.util.Map r2 = r7.getHeaders()
            r1.putAll(r2)
            r1.putAll(r8)
            com.google.android.gms.internal.zzat r8 = r6.zzcg
            if (r8 == 0) goto L_0x003b
            java.lang.String r8 = r8.zzg(r0)
            if (r8 != 0) goto L_0x003a
            java.io.IOException r7 = new java.io.IOException
            java.lang.String r8 = "URL blocked by rewriter: "
            java.lang.String r0 = java.lang.String.valueOf(r0)
            int r1 = r0.length()
            if (r1 == 0) goto L_0x0030
            java.lang.String r8 = r8.concat(r0)
            goto L_0x0036
        L_0x0030:
            java.lang.String r0 = new java.lang.String
            r0.<init>(r8)
            r8 = r0
        L_0x0036:
            r7.<init>(r8)
            throw r7
        L_0x003a:
            r0 = r8
        L_0x003b:
            java.net.URL r8 = new java.net.URL
            r8.<init>(r0)
            java.net.URLConnection r0 = r8.openConnection()
            java.net.HttpURLConnection r0 = (java.net.HttpURLConnection) r0
            boolean r2 = java.net.HttpURLConnection.getFollowRedirects()
            r0.setInstanceFollowRedirects(r2)
            int r2 = r7.zzh()
            r0.setConnectTimeout(r2)
            r0.setReadTimeout(r2)
            r2 = 0
            r0.setUseCaches(r2)
            r3 = 1
            r0.setDoInput(r3)
            java.lang.String r8 = r8.getProtocol()
            java.lang.String r4 = "https"
            r4.equals(r8)
            java.util.Set r8 = r1.keySet()
            java.util.Iterator r8 = r8.iterator()
        L_0x0070:
            boolean r4 = r8.hasNext()
            if (r4 == 0) goto L_0x0086
            java.lang.Object r4 = r8.next()
            java.lang.String r4 = (java.lang.String) r4
            java.lang.Object r5 = r1.get(r4)
            java.lang.String r5 = (java.lang.String) r5
            r0.addRequestProperty(r4, r5)
            goto L_0x0070
        L_0x0086:
            int r8 = r7.getMethod()
            switch(r8) {
                case -1: goto L_0x00b5;
                case 0: goto L_0x00b0;
                case 1: goto L_0x00a7;
                case 2: goto L_0x00a4;
                case 3: goto L_0x00a1;
                case 4: goto L_0x009e;
                case 5: goto L_0x009b;
                case 6: goto L_0x0098;
                case 7: goto L_0x0095;
                default: goto L_0x008d;
            }
        L_0x008d:
            java.lang.IllegalStateException r7 = new java.lang.IllegalStateException
            java.lang.String r8 = "Unknown method type."
            r7.<init>(r8)
            throw r7
        L_0x0095:
            java.lang.String r8 = "PATCH"
            goto L_0x00a9
        L_0x0098:
            java.lang.String r8 = "TRACE"
            goto L_0x00b2
        L_0x009b:
            java.lang.String r8 = "OPTIONS"
            goto L_0x00b2
        L_0x009e:
            java.lang.String r8 = "HEAD"
            goto L_0x00b2
        L_0x00a1:
            java.lang.String r8 = "DELETE"
            goto L_0x00b2
        L_0x00a4:
            java.lang.String r8 = "PUT"
            goto L_0x00a9
        L_0x00a7:
            java.lang.String r8 = "POST"
        L_0x00a9:
            r0.setRequestMethod(r8)
            zza((java.net.HttpURLConnection) r0, (com.google.android.gms.internal.zzr<?>) r7)
            goto L_0x00b5
        L_0x00b0:
            java.lang.String r8 = "GET"
        L_0x00b2:
            r0.setRequestMethod(r8)
        L_0x00b5:
            int r8 = r0.getResponseCode()
            r1 = -1
            if (r8 == r1) goto L_0x00f8
            int r7 = r7.getMethod()
            r1 = 4
            if (r7 == r1) goto L_0x00d4
            r7 = 100
            if (r7 > r8) goto L_0x00cb
            r7 = 200(0xc8, float:2.8E-43)
            if (r8 < r7) goto L_0x00d4
        L_0x00cb:
            r7 = 204(0xcc, float:2.86E-43)
            if (r8 == r7) goto L_0x00d4
            r7 = 304(0x130, float:4.26E-43)
            if (r8 == r7) goto L_0x00d4
            r2 = r3
        L_0x00d4:
            com.google.android.gms.internal.zzaq r7 = new com.google.android.gms.internal.zzaq
            if (r2 != 0) goto L_0x00e4
            java.util.Map r0 = r0.getHeaderFields()
            java.util.List r0 = zzc(r0)
            r7.<init>(r8, r0)
            return r7
        L_0x00e4:
            java.util.Map r1 = r0.getHeaderFields()
            java.util.List r1 = zzc(r1)
            int r2 = r0.getContentLength()
            java.io.InputStream r0 = zza(r0)
            r7.<init>(r8, r1, r2, r0)
            return r7
        L_0x00f8:
            java.io.IOException r7 = new java.io.IOException
            java.lang.String r8 = "Could not retrieve response code from HttpUrlConnection."
            r7.<init>(r8)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzas.zza(com.google.android.gms.internal.zzr, java.util.Map):com.google.android.gms.internal.zzaq");
    }
}
