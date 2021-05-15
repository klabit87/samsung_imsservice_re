package com.google.firebase.messaging;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.internal.zzflr;
import com.google.android.gms.internal.zzfmu;
import com.google.android.gms.internal.zzfmv;
import com.google.android.gms.measurement.AppMeasurement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class zzc {
    private static Object zza(zzfmv zzfmv, String str, zzb zzb) {
        Object obj = null;
        try {
            Class cls = Class.forName("com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty");
            Bundle zzay = zzay(zzfmv.zzpzs, zzfmv.zzpzt);
            Object newInstance = cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            try {
                cls.getField("mOrigin").set(newInstance, str);
                cls.getField("mCreationTimestamp").set(newInstance, Long.valueOf(zzfmv.zzpzu));
                cls.getField("mName").set(newInstance, zzfmv.zzpzs);
                cls.getField("mValue").set(newInstance, zzfmv.zzpzt);
                if (!TextUtils.isEmpty(zzfmv.zzpzv)) {
                    obj = zzfmv.zzpzv;
                }
                cls.getField("mTriggerEventName").set(newInstance, obj);
                cls.getField("mTimedOutEventName").set(newInstance, !TextUtils.isEmpty(zzfmv.zzqaa) ? zzfmv.zzqaa : zzb.zzbta());
                cls.getField("mTimedOutEventParams").set(newInstance, zzay);
                cls.getField("mTriggerTimeout").set(newInstance, Long.valueOf(zzfmv.zzpzw));
                cls.getField("mTriggeredEventName").set(newInstance, !TextUtils.isEmpty(zzfmv.zzpzy) ? zzfmv.zzpzy : zzb.zzbsz());
                cls.getField("mTriggeredEventParams").set(newInstance, zzay);
                cls.getField("mTimeToLive").set(newInstance, Long.valueOf(zzfmv.zzgoc));
                cls.getField("mExpiredEventName").set(newInstance, !TextUtils.isEmpty(zzfmv.zzqab) ? zzfmv.zzqab : zzb.zzbtb());
                cls.getField("mExpiredEventParams").set(newInstance, zzay);
                return newInstance;
            } catch (Exception e) {
                e = e;
                obj = newInstance;
                Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e);
                return obj;
            }
        } catch (Exception e2) {
            e = e2;
            Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e);
            return obj;
        }
    }

    private static String zza(zzfmv zzfmv, zzb zzb) {
        return (zzfmv == null || TextUtils.isEmpty(zzfmv.zzpzz)) ? zzb.zzbtc() : zzfmv.zzpzz;
    }

    private static List<Object> zza(AppMeasurement appMeasurement, String str) {
        List arrayList = new ArrayList();
        try {
            Method declaredMethod = AppMeasurement.class.getDeclaredMethod("getConditionalUserProperties", new Class[]{String.class, String.class});
            declaredMethod.setAccessible(true);
            arrayList = (List) declaredMethod.invoke(appMeasurement, new Object[]{str, ""});
        } catch (Exception e) {
            Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e);
        }
        if (Log.isLoggable("FirebaseAbtUtil", 2)) {
            int size = arrayList.size();
            StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 55);
            sb.append("Number of currently set _Es for origin: ");
            sb.append(str);
            sb.append(" is ");
            sb.append(size);
            Log.v("FirebaseAbtUtil", sb.toString());
        }
        return arrayList;
    }

    private static void zza(Context context, String str, String str2, String str3, String str4) {
        if (Log.isLoggable("FirebaseAbtUtil", 2)) {
            String valueOf = String.valueOf(str);
            Log.v("FirebaseAbtUtil", valueOf.length() != 0 ? "_CE(experimentId) called by ".concat(valueOf) : new String("_CE(experimentId) called by "));
        }
        if (zzey(context)) {
            AppMeasurement zzde = zzde(context);
            try {
                Method declaredMethod = AppMeasurement.class.getDeclaredMethod("clearConditionalUserProperty", new Class[]{String.class, String.class, Bundle.class});
                declaredMethod.setAccessible(true);
                if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                    StringBuilder sb = new StringBuilder(String.valueOf(str2).length() + 17 + String.valueOf(str3).length());
                    sb.append("Clearing _E: [");
                    sb.append(str2);
                    sb.append(", ");
                    sb.append(str3);
                    sb.append("]");
                    Log.v("FirebaseAbtUtil", sb.toString());
                }
                declaredMethod.invoke(zzde, new Object[]{str2, str4, zzay(str2, str3)});
            } catch (Exception e) {
                Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e);
            }
        }
    }

    public static void zza(Context context, String str, byte[] bArr, zzb zzb, int i) {
        boolean z;
        String str2;
        String str3 = str;
        String str4 = "com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty";
        int i2 = 2;
        if (Log.isLoggable("FirebaseAbtUtil", 2)) {
            String valueOf = String.valueOf(str);
            Log.v("FirebaseAbtUtil", valueOf.length() != 0 ? "_SE called by ".concat(valueOf) : new String("_SE called by "));
        }
        if (zzey(context)) {
            AppMeasurement zzde = zzde(context);
            zzfmv zzam = zzam(bArr);
            if (zzam != null) {
                try {
                    Class.forName(str4);
                    boolean z2 = false;
                    for (Object next : zza(zzde, str3)) {
                        String zzbe = zzbe(next);
                        String zzbf = zzbf(next);
                        long longValue = ((Long) Class.forName(str4).getField("mCreationTimestamp").get(next)).longValue();
                        if (!zzam.zzpzs.equals(zzbe) || !zzam.zzpzt.equals(zzbf)) {
                            zzfmu[] zzfmuArr = zzam.zzqad;
                            int length = zzfmuArr.length;
                            int i3 = 0;
                            while (true) {
                                if (i3 >= length) {
                                    z = false;
                                    break;
                                } else if (zzfmuArr[i3].zzpzs.equals(zzbe)) {
                                    if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                                        StringBuilder sb = new StringBuilder(String.valueOf(zzbe).length() + 33 + String.valueOf(zzbf).length());
                                        sb.append("_E is found in the _OE list. [");
                                        sb.append(zzbe);
                                        sb.append(", ");
                                        sb.append(zzbf);
                                        sb.append("]");
                                        Log.v("FirebaseAbtUtil", sb.toString());
                                    }
                                    z = true;
                                } else {
                                    i3++;
                                }
                            }
                            if (!z) {
                                str2 = str4;
                                if (zzam.zzpzu > longValue) {
                                    if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                                        StringBuilder sb2 = new StringBuilder(String.valueOf(zzbe).length() + 115 + String.valueOf(zzbf).length());
                                        sb2.append("Clearing _E as it was not in the _OE list, andits start time is older than the start time of the _E to be set. [");
                                        sb2.append(zzbe);
                                        sb2.append(", ");
                                        sb2.append(zzbf);
                                        sb2.append("]");
                                        Log.v("FirebaseAbtUtil", sb2.toString());
                                    }
                                    zza(context, str3, zzbe, zzbf, zza(zzam, zzb));
                                } else {
                                    Context context2 = context;
                                    zzb zzb2 = zzb;
                                    if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                                        StringBuilder sb3 = new StringBuilder(String.valueOf(zzbe).length() + 109 + String.valueOf(zzbf).length());
                                        sb3.append("_E was not found in the _OE list, but not clearing it as it has a new start time than the _E to be set.  [");
                                        sb3.append(zzbe);
                                        sb3.append(", ");
                                        sb3.append(zzbf);
                                        sb3.append("]");
                                        Log.v("FirebaseAbtUtil", sb3.toString());
                                    }
                                }
                            } else {
                                Context context3 = context;
                                zzb zzb3 = zzb;
                                str2 = str4;
                            }
                            str4 = str2;
                            i2 = 2;
                        } else {
                            if (Log.isLoggable("FirebaseAbtUtil", i2)) {
                                StringBuilder sb4 = new StringBuilder(String.valueOf(zzbe).length() + 23 + String.valueOf(zzbf).length());
                                sb4.append("_E is already set. [");
                                sb4.append(zzbe);
                                sb4.append(", ");
                                sb4.append(zzbf);
                                sb4.append("]");
                                Log.v("FirebaseAbtUtil", sb4.toString());
                            }
                            z2 = true;
                        }
                    }
                    Context context4 = context;
                    zzb zzb4 = zzb;
                    if (!z2) {
                        zza(zzde, context, str, zzam, zzb, 1);
                    } else if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                        String str5 = zzam.zzpzs;
                        String str6 = zzam.zzpzt;
                        StringBuilder sb5 = new StringBuilder(String.valueOf(str5).length() + 44 + String.valueOf(str6).length());
                        sb5.append("_E is already set. Not setting it again [");
                        sb5.append(str5);
                        sb5.append(", ");
                        sb5.append(str6);
                        sb5.append("]");
                        Log.v("FirebaseAbtUtil", sb5.toString());
                    }
                } catch (Exception e) {
                    Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e);
                }
            } else if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                Log.v("FirebaseAbtUtil", "_SE failed; either _P was not set, or we couldn't deserialize the _P.");
            }
        }
    }

    private static void zza(AppMeasurement appMeasurement, Context context, String str, zzfmv zzfmv, zzb zzb, int i) {
        AppMeasurement appMeasurement2 = appMeasurement;
        Context context2 = context;
        String str2 = str;
        zzfmv zzfmv2 = zzfmv;
        if (Log.isLoggable("FirebaseAbtUtil", 2)) {
            String str3 = zzfmv2.zzpzs;
            String str4 = zzfmv2.zzpzt;
            StringBuilder sb = new StringBuilder(String.valueOf(str3).length() + 7 + String.valueOf(str4).length());
            sb.append("_SEI: ");
            sb.append(str3);
            sb.append(" ");
            sb.append(str4);
            Log.v("FirebaseAbtUtil", sb.toString());
        }
        try {
            Class.forName("com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty");
            List<Object> zza = zza(appMeasurement2, str2);
            if (zza(appMeasurement2, str2).size() >= zzb(appMeasurement2, str2)) {
                if ((zzfmv2.zzqac != 0 ? zzfmv2.zzqac : 1) == 1) {
                    Object obj = zza.get(0);
                    String zzbe = zzbe(obj);
                    String zzbf = zzbf(obj);
                    if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                        StringBuilder sb2 = new StringBuilder(String.valueOf(zzbe).length() + 38);
                        sb2.append("Clearing _E due to overflow policy: [");
                        sb2.append(zzbe);
                        sb2.append("]");
                        Log.v("FirebaseAbtUtil", sb2.toString());
                    }
                    zza(context2, str2, zzbe, zzbf, zza(zzfmv, zzb));
                } else if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                    String str5 = zzfmv2.zzpzs;
                    String str6 = zzfmv2.zzpzt;
                    StringBuilder sb3 = new StringBuilder(String.valueOf(str5).length() + 44 + String.valueOf(str6).length());
                    sb3.append("_E won't be set due to overflow policy. [");
                    sb3.append(str5);
                    sb3.append(", ");
                    sb3.append(str6);
                    sb3.append("]");
                    Log.v("FirebaseAbtUtil", sb3.toString());
                    return;
                } else {
                    return;
                }
            }
            for (Object next : zza) {
                String zzbe2 = zzbe(next);
                String zzbf2 = zzbf(next);
                if (zzbe2.equals(zzfmv2.zzpzs) && !zzbf2.equals(zzfmv2.zzpzt) && Log.isLoggable("FirebaseAbtUtil", 2)) {
                    StringBuilder sb4 = new StringBuilder(String.valueOf(zzbe2).length() + 77 + String.valueOf(zzbf2).length());
                    sb4.append("Clearing _E, as only one _V of the same _E can be set atany given time: [");
                    sb4.append(zzbe2);
                    sb4.append(", ");
                    sb4.append(zzbf2);
                    sb4.append("].");
                    Log.v("FirebaseAbtUtil", sb4.toString());
                    zza(context2, str2, zzbe2, zzbf2, zza(zzfmv, zzb));
                }
            }
            Object zza2 = zza(zzfmv2, str2, zzb);
            if (zza2 != null) {
                try {
                    Method declaredMethod = AppMeasurement.class.getDeclaredMethod("setConditionalUserProperty", new Class[]{Class.forName("com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty")});
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(appMeasurement2, new Object[]{zza2});
                } catch (Exception e) {
                    Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e);
                }
            } else if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                String str7 = zzfmv2.zzpzs;
                String str8 = zzfmv2.zzpzt;
                StringBuilder sb5 = new StringBuilder(String.valueOf(str7).length() + 42 + String.valueOf(str8).length());
                sb5.append("Could not create _CUP for: [");
                sb5.append(str7);
                sb5.append(", ");
                sb5.append(str8);
                sb5.append("]. Skipping.");
                Log.v("FirebaseAbtUtil", sb5.toString());
            }
        } catch (Exception e2) {
            Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e2);
        }
    }

    private static zzfmv zzam(byte[] bArr) {
        try {
            return zzfmv.zzbi(bArr);
        } catch (zzflr e) {
            return null;
        }
    }

    private static Bundle zzay(String str, String str2) {
        Bundle bundle = new Bundle();
        bundle.putString(str, str2);
        return bundle;
    }

    private static int zzb(AppMeasurement appMeasurement, String str) {
        try {
            Method declaredMethod = AppMeasurement.class.getDeclaredMethod("getMaxUserProperties", new Class[]{String.class});
            declaredMethod.setAccessible(true);
            return ((Integer) declaredMethod.invoke(appMeasurement, new Object[]{str})).intValue();
        } catch (Exception e) {
            Log.e("FirebaseAbtUtil", "Could not complete the operation due to an internal error.", e);
            return 20;
        }
    }

    private static String zzbe(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return (String) Class.forName("com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty").getField("mName").get(obj);
    }

    private static String zzbf(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return (String) Class.forName("com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty").getField("mValue").get(obj);
    }

    private static AppMeasurement zzde(Context context) {
        try {
            return AppMeasurement.getInstance(context);
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }

    private static boolean zzey(Context context) {
        if (zzde(context) == null) {
            if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                Log.v("FirebaseAbtUtil", "Firebase Analytics not available");
            }
            return false;
        }
        try {
            Class.forName("com.google.android.gms.measurement.AppMeasurement$ConditionalUserProperty");
            return true;
        } catch (ClassNotFoundException e) {
            if (Log.isLoggable("FirebaseAbtUtil", 2)) {
                Log.v("FirebaseAbtUtil", "Firebase Analytics library is missing support for abt. Please update to a more recent version.");
            }
            return false;
        }
    }
}
