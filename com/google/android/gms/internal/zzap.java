package com.google.android.gms.internal;

import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.httpclient.HttpController;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public final class zzap {
    static List<zzl> zza(Map<String, String> map) {
        ArrayList arrayList = new ArrayList(map.size());
        for (Map.Entry next : map.entrySet()) {
            arrayList.add(new zzl((String) next.getKey(), (String) next.getValue()));
        }
        return arrayList;
    }

    static Map<String, String> zza(List<zzl> list) {
        TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        for (zzl next : list) {
            treeMap.put(next.getName(), next.getValue());
        }
        return treeMap;
    }

    public static zzc zzb(zzp zzp) {
        long j;
        long j2;
        boolean z;
        long j3;
        zzp zzp2 = zzp;
        long currentTimeMillis = System.currentTimeMillis();
        Map<String, String> map = zzp2.zzab;
        String str = map.get("Date");
        long j4 = 0;
        long zzf = str != null ? zzf(str) : 0;
        String str2 = map.get(HttpController.HEADER_CACHE_CONTROL);
        int i = 0;
        if (str2 != null) {
            String[] split = str2.split(",");
            j2 = 0;
            j = 0;
            int i2 = 0;
            while (i < split.length) {
                String trim = split[i].trim();
                if (trim.equals("no-cache") || trim.equals("no-store")) {
                    return null;
                }
                if (trim.startsWith("max-age=")) {
                    try {
                        j2 = Long.parseLong(trim.substring(8));
                    } catch (Exception e) {
                    }
                } else if (trim.startsWith("stale-while-revalidate=")) {
                    j = Long.parseLong(trim.substring(23));
                } else if (trim.equals("must-revalidate") || trim.equals("proxy-revalidate")) {
                    i2 = 1;
                }
                i++;
            }
            i = i2;
            z = true;
        } else {
            j2 = 0;
            j = 0;
            z = false;
        }
        String str3 = map.get(HttpController.HEADER_EXPIRES);
        long zzf2 = str3 != null ? zzf(str3) : 0;
        String str4 = map.get(HttpController.HEADER_LAST_MODIFIED);
        long zzf3 = str4 != null ? zzf(str4) : 0;
        String str5 = map.get(HttpController.HEADER_ETAG);
        if (z) {
            long j5 = currentTimeMillis + (j2 * 1000);
            long j6 = j5;
            j3 = i != 0 ? j5 : (j * 1000) + j5;
            j4 = j6;
        } else {
            if (zzf > 0 && zzf2 >= zzf) {
                j4 = currentTimeMillis + (zzf2 - zzf);
            }
            j3 = j4;
        }
        zzc zzc = new zzc();
        zzc.data = zzp2.data;
        zzc.zza = str5;
        zzc.zze = j4;
        zzc.zzd = j3;
        zzc.zzb = zzf;
        zzc.zzc = zzf3;
        zzc.zzf = map;
        zzc.zzg = zzp2.allHeaders;
        return zzc;
    }

    static String zzb(long j) {
        return zzo().format(new Date(j));
    }

    public static String zzb(Map<String, String> map) {
        String str = map.get("Content-Type");
        if (str == null) {
            return "ISO-8859-1";
        }
        String[] split = str.split(";");
        for (int i = 1; i < split.length; i++) {
            String[] split2 = split[i].trim().split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            if (split2.length == 2 && split2[0].equals("charset")) {
                return split2[1];
            }
        }
        return "ISO-8859-1";
    }

    private static long zzf(String str) {
        try {
            return zzo().parse(str).getTime();
        } catch (ParseException e) {
            zzaf.zza(e, "Unable to parse dateStr: %s, falling back to 0", str);
            return 0;
        }
    }

    private static SimpleDateFormat zzo() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat;
    }
}
