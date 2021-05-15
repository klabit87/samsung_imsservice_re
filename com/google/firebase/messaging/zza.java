package com.google.firebase.messaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.R;
import com.google.android.gms.common.util.zzs;
import com.sec.internal.imscr.LogClass;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.MissingFormatArgumentException;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONArray;
import org.json.JSONException;

final class zza {
    private static zza zzolt;
    private final Context mContext;
    private Bundle zzgco;
    private Method zzolu;
    private Method zzolv;
    private final AtomicInteger zzolw = new AtomicInteger((int) SystemClock.elapsedRealtime());

    private zza(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private final Notification zza(CharSequence charSequence, String str, int i, Integer num, Uri uri, PendingIntent pendingIntent, PendingIntent pendingIntent2, String str2) {
        Notification.Builder smallIcon = new Notification.Builder(this.mContext).setAutoCancel(true).setSmallIcon(i);
        if (!TextUtils.isEmpty(charSequence)) {
            smallIcon.setContentTitle(charSequence);
        }
        if (!TextUtils.isEmpty(str)) {
            smallIcon.setContentText(str);
            smallIcon.setStyle(new Notification.BigTextStyle().bigText(str));
        }
        if (num != null) {
            smallIcon.setColor(num.intValue());
        }
        if (uri != null) {
            smallIcon.setSound(uri);
        }
        if (pendingIntent != null) {
            smallIcon.setContentIntent(pendingIntent);
        }
        if (pendingIntent2 != null) {
            smallIcon.setDeleteIntent(pendingIntent2);
        }
        if (str2 != null) {
            if (this.zzolu == null) {
                this.zzolu = zzrx("setChannelId");
            }
            if (this.zzolu == null) {
                this.zzolu = zzrx("setChannel");
            }
            Method method = this.zzolu;
            if (method == null) {
                Log.e("FirebaseMessaging", "Error while setting the notification channel");
            } else {
                try {
                    method.invoke(smallIcon, new Object[]{str2});
                } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException e) {
                    Log.e("FirebaseMessaging", "Error while setting the notification channel", e);
                }
            }
        }
        return smallIcon.build();
    }

    private static void zza(Intent intent, Bundle bundle) {
        for (String str : bundle.keySet()) {
            if (str.startsWith("google.c.a.") || str.equals("from")) {
                intent.putExtra(str, bundle.getString(str));
            }
        }
    }

    static boolean zzai(Bundle bundle) {
        return "1".equals(zzd(bundle, "gcm.n.e")) || zzd(bundle, "gcm.n.icon") != null;
    }

    static Uri zzaj(Bundle bundle) {
        String zzd = zzd(bundle, "gcm.n.link_android");
        if (TextUtils.isEmpty(zzd)) {
            zzd = zzd(bundle, "gcm.n.link");
        }
        if (!TextUtils.isEmpty(zzd)) {
            return Uri.parse(zzd);
        }
        return null;
    }

    static String zzak(Bundle bundle) {
        String zzd = zzd(bundle, "gcm.n.sound2");
        return TextUtils.isEmpty(zzd) ? zzd(bundle, "gcm.n.sound") : zzd;
    }

    private final Bundle zzawf() {
        Bundle bundle = this.zzgco;
        if (bundle != null) {
            return bundle;
        }
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(), 128);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (applicationInfo == null || applicationInfo.metaData == null) {
            return Bundle.EMPTY;
        }
        Bundle bundle2 = applicationInfo.metaData;
        this.zzgco = bundle2;
        return bundle2;
    }

    static String zzd(Bundle bundle, String str) {
        String string = bundle.getString(str);
        return string == null ? bundle.getString(str.replace("gcm.n.", "gcm.notification.")) : string;
    }

    static synchronized zza zzfc(Context context) {
        zza zza;
        synchronized (zza.class) {
            if (zzolt == null) {
                zzolt = new zza(context);
            }
            zza = zzolt;
        }
        return zza;
    }

    static String zzh(Bundle bundle, String str) {
        String valueOf = String.valueOf(str);
        String valueOf2 = String.valueOf("_loc_key");
        return zzd(bundle, valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf));
    }

    static Object[] zzi(Bundle bundle, String str) {
        String valueOf = String.valueOf(str);
        String valueOf2 = String.valueOf("_loc_args");
        String zzd = zzd(bundle, valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf));
        if (TextUtils.isEmpty(zzd)) {
            return null;
        }
        try {
            JSONArray jSONArray = new JSONArray(zzd);
            int length = jSONArray.length();
            Object[] objArr = new String[length];
            for (int i = 0; i < length; i++) {
                objArr[i] = jSONArray.opt(i);
            }
            return objArr;
        } catch (JSONException e) {
            String valueOf3 = String.valueOf(str);
            String valueOf4 = String.valueOf("_loc_args");
            String substring = (valueOf4.length() != 0 ? valueOf3.concat(valueOf4) : new String(valueOf3)).substring(6);
            StringBuilder sb = new StringBuilder(String.valueOf(substring).length() + 41 + String.valueOf(zzd).length());
            sb.append("Malformed ");
            sb.append(substring);
            sb.append(": ");
            sb.append(zzd);
            sb.append("  Default value will be used.");
            Log.w("FirebaseMessaging", sb.toString());
            return null;
        }
    }

    private final boolean zzit(int i) {
        if (Build.VERSION.SDK_INT != 26) {
            return true;
        }
        try {
            if (!(this.mContext.getResources().getDrawable(i, (Resources.Theme) null) instanceof AdaptiveIconDrawable)) {
                return true;
            }
            StringBuilder sb = new StringBuilder(77);
            sb.append("Adaptive icons cannot be used in notifications. Ignoring icon id: ");
            sb.append(i);
            Log.e("FirebaseMessaging", sb.toString());
            return false;
        } catch (Resources.NotFoundException e) {
            return false;
        }
    }

    private final String zzj(Bundle bundle, String str) {
        String zzd = zzd(bundle, str);
        if (!TextUtils.isEmpty(zzd)) {
            return zzd;
        }
        String zzh = zzh(bundle, str);
        if (TextUtils.isEmpty(zzh)) {
            return null;
        }
        Resources resources = this.mContext.getResources();
        int identifier = resources.getIdentifier(zzh, "string", this.mContext.getPackageName());
        if (identifier == 0) {
            String valueOf = String.valueOf(str);
            String valueOf2 = String.valueOf("_loc_key");
            String substring = (valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf)).substring(6);
            StringBuilder sb = new StringBuilder(String.valueOf(substring).length() + 49 + String.valueOf(zzh).length());
            sb.append(substring);
            sb.append(" resource not found: ");
            sb.append(zzh);
            sb.append(" Default value will be used.");
            Log.w("FirebaseMessaging", sb.toString());
            return null;
        }
        Object[] zzi = zzi(bundle, str);
        if (zzi == null) {
            return resources.getString(identifier);
        }
        try {
            return resources.getString(identifier, zzi);
        } catch (MissingFormatArgumentException e) {
            String arrays = Arrays.toString(zzi);
            StringBuilder sb2 = new StringBuilder(String.valueOf(zzh).length() + 58 + String.valueOf(arrays).length());
            sb2.append("Missing format argument for ");
            sb2.append(zzh);
            sb2.append(": ");
            sb2.append(arrays);
            sb2.append(" Default value will be used.");
            Log.w("FirebaseMessaging", sb2.toString(), e);
            return null;
        }
    }

    private static Method zzrx(String str) {
        try {
            return Notification.Builder.class.getMethod(str, new Class[]{String.class});
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    private final Integer zzry(String str) {
        if (Build.VERSION.SDK_INT < 21) {
            return null;
        }
        if (!TextUtils.isEmpty(str)) {
            try {
                return Integer.valueOf(Color.parseColor(str));
            } catch (IllegalArgumentException e) {
                StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 54);
                sb.append("Color ");
                sb.append(str);
                sb.append(" not valid. Notification will use default color.");
                Log.w("FirebaseMessaging", sb.toString());
            }
        }
        int i = zzawf().getInt("com.google.firebase.messaging.default_notification_color", 0);
        if (i != 0) {
            try {
                return Integer.valueOf(ContextCompat.getColor(this.mContext, i));
            } catch (Resources.NotFoundException e2) {
                Log.w("FirebaseMessaging", "Cannot find the color resource referenced in AndroidManifest.");
            }
        }
        return null;
    }

    private final String zzrz(String str) {
        String str2;
        if (!zzs.isAtLeastO()) {
            return null;
        }
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        try {
            if (this.zzolv == null) {
                this.zzolv = notificationManager.getClass().getMethod("getNotificationChannel", new Class[]{String.class});
            }
            if (!TextUtils.isEmpty(str)) {
                if (this.zzolv.invoke(notificationManager, new Object[]{str}) != null) {
                    return str;
                }
                StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 122);
                sb.append("Notification Channel requested (");
                sb.append(str);
                sb.append(") has not been created by the app. Manifest configuration, or default, value will be used.");
                Log.w("FirebaseMessaging", sb.toString());
            }
            String string = zzawf().getString("com.google.firebase.messaging.default_notification_channel_id");
            if (!TextUtils.isEmpty(string)) {
                if (this.zzolv.invoke(notificationManager, new Object[]{string}) != null) {
                    return string;
                }
                str2 = "Notification Channel set in AndroidManifest.xml has not been created by the app. Default value will be used.";
            } else {
                str2 = "Missing Default Notification Channel metadata in AndroidManifest. Default value will be used.";
            }
            Log.w("FirebaseMessaging", str2);
            if (this.zzolv.invoke(notificationManager, new Object[]{"fcm_fallback_notification_channel"}) == null) {
                Class<?> cls = Class.forName("android.app.NotificationChannel");
                Object newInstance = cls.getConstructor(new Class[]{String.class, CharSequence.class, Integer.TYPE}).newInstance(new Object[]{"fcm_fallback_notification_channel", this.mContext.getString(R.string.fcm_fallback_notification_channel_label), 3});
                notificationManager.getClass().getMethod("createNotificationChannel", new Class[]{cls}).invoke(notificationManager, new Object[]{newInstance});
            }
            return "fcm_fallback_notification_channel";
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | LinkageError | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            Log.e("FirebaseMessaging", "Error while setting the notification channel", e);
            return null;
        }
    }

    private final PendingIntent zzu(Bundle bundle) {
        Intent intent;
        String zzd = zzd(bundle, "gcm.n.click_action");
        if (!TextUtils.isEmpty(zzd)) {
            intent = new Intent(zzd);
            intent.setPackage(this.mContext.getPackageName());
            intent.setFlags(LogClass.SIM_EVENT);
        } else {
            Uri zzaj = zzaj(bundle);
            if (zzaj != null) {
                intent = new Intent("android.intent.action.VIEW");
                intent.setPackage(this.mContext.getPackageName());
                intent.setData(zzaj);
            } else {
                intent = this.mContext.getPackageManager().getLaunchIntentForPackage(this.mContext.getPackageName());
                if (intent == null) {
                    Log.w("FirebaseMessaging", "No activity found to launch app");
                }
            }
        }
        if (intent == null) {
            return null;
        }
        intent.addFlags(67108864);
        Bundle bundle2 = new Bundle(bundle);
        FirebaseMessagingService.zzr(bundle2);
        intent.putExtras(bundle2);
        for (String str : bundle2.keySet()) {
            if (str.startsWith("gcm.n.") || str.startsWith("gcm.notification.")) {
                intent.removeExtra(str);
            }
        }
        return PendingIntent.getActivity(this.mContext, this.zzolw.incrementAndGet(), intent, LogClass.IM_SWITCH_OFF);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0122  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0185  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01cd  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x01de  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0234  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0249  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean zzt(android.os.Bundle r14) {
        /*
            r13 = this;
            java.lang.String r0 = "gcm.n.noui"
            java.lang.String r0 = zzd(r14, r0)
            java.lang.String r1 = "1"
            boolean r0 = r1.equals(r0)
            r1 = 1
            if (r0 == 0) goto L_0x0010
            return r1
        L_0x0010:
            android.content.Context r0 = r13.mContext
            java.lang.String r2 = "keyguard"
            java.lang.Object r0 = r0.getSystemService(r2)
            android.app.KeyguardManager r0 = (android.app.KeyguardManager) r0
            boolean r0 = r0.inKeyguardRestrictedInputMode()
            r2 = 0
            if (r0 != 0) goto L_0x005c
            boolean r0 = com.google.android.gms.common.util.zzs.zzanx()
            if (r0 != 0) goto L_0x002c
            r3 = 10
            android.os.SystemClock.sleep(r3)
        L_0x002c:
            int r0 = android.os.Process.myPid()
            android.content.Context r3 = r13.mContext
            java.lang.String r4 = "activity"
            java.lang.Object r3 = r3.getSystemService(r4)
            android.app.ActivityManager r3 = (android.app.ActivityManager) r3
            java.util.List r3 = r3.getRunningAppProcesses()
            if (r3 == 0) goto L_0x005c
            java.util.Iterator r3 = r3.iterator()
        L_0x0044:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x005c
            java.lang.Object r4 = r3.next()
            android.app.ActivityManager$RunningAppProcessInfo r4 = (android.app.ActivityManager.RunningAppProcessInfo) r4
            int r5 = r4.pid
            if (r5 != r0) goto L_0x0044
            int r0 = r4.importance
            r3 = 100
            if (r0 != r3) goto L_0x005c
            r0 = r1
            goto L_0x005d
        L_0x005c:
            r0 = r2
        L_0x005d:
            if (r0 == 0) goto L_0x0060
            return r2
        L_0x0060:
            java.lang.String r0 = "gcm.n.title"
            java.lang.String r0 = r13.zzj(r14, r0)
            boolean r3 = android.text.TextUtils.isEmpty(r0)
            if (r3 == 0) goto L_0x007c
            android.content.Context r0 = r13.mContext
            android.content.pm.ApplicationInfo r0 = r0.getApplicationInfo()
            android.content.Context r3 = r13.mContext
            android.content.pm.PackageManager r3 = r3.getPackageManager()
            java.lang.CharSequence r0 = r0.loadLabel(r3)
        L_0x007c:
            r4 = r0
            java.lang.String r0 = "gcm.n.body"
            java.lang.String r5 = r13.zzj(r14, r0)
            java.lang.String r0 = "gcm.n.icon"
            java.lang.String r0 = zzd(r14, r0)
            boolean r3 = android.text.TextUtils.isEmpty(r0)
            java.lang.String r12 = "FirebaseMessaging"
            if (r3 != 0) goto L_0x00e5
            android.content.Context r3 = r13.mContext
            android.content.res.Resources r3 = r3.getResources()
            android.content.Context r6 = r13.mContext
            java.lang.String r6 = r6.getPackageName()
            java.lang.String r7 = "drawable"
            int r6 = r3.getIdentifier(r0, r7, r6)
            if (r6 == 0) goto L_0x00ac
            boolean r7 = r13.zzit(r6)
            if (r7 == 0) goto L_0x00ac
            goto L_0x010b
        L_0x00ac:
            android.content.Context r6 = r13.mContext
            java.lang.String r6 = r6.getPackageName()
            java.lang.String r7 = "mipmap"
            int r3 = r3.getIdentifier(r0, r7, r6)
            if (r3 == 0) goto L_0x00c2
            boolean r6 = r13.zzit(r3)
            if (r6 == 0) goto L_0x00c2
            r6 = r3
            goto L_0x010b
        L_0x00c2:
            java.lang.String r3 = java.lang.String.valueOf(r0)
            int r3 = r3.length()
            int r3 = r3 + 61
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>(r3)
            java.lang.String r3 = "Icon resource "
            r6.append(r3)
            r6.append(r0)
            java.lang.String r0 = " not found. Notification will use default icon."
            r6.append(r0)
            java.lang.String r0 = r6.toString()
            android.util.Log.w(r12, r0)
        L_0x00e5:
            android.os.Bundle r0 = r13.zzawf()
            java.lang.String r3 = "com.google.firebase.messaging.default_notification_icon"
            int r0 = r0.getInt(r3, r2)
            if (r0 == 0) goto L_0x00f7
            boolean r3 = r13.zzit(r0)
            if (r3 != 0) goto L_0x00ff
        L_0x00f7:
            android.content.Context r0 = r13.mContext
            android.content.pm.ApplicationInfo r0 = r0.getApplicationInfo()
            int r0 = r0.icon
        L_0x00ff:
            if (r0 == 0) goto L_0x0107
            boolean r3 = r13.zzit(r0)
            if (r3 != 0) goto L_0x010a
        L_0x0107:
            r0 = 17301651(0x1080093, float:2.4979667E-38)
        L_0x010a:
            r6 = r0
        L_0x010b:
            java.lang.String r0 = "gcm.n.color"
            java.lang.String r0 = zzd(r14, r0)
            java.lang.Integer r7 = r13.zzry(r0)
            java.lang.String r0 = zzak(r14)
            boolean r3 = android.text.TextUtils.isEmpty(r0)
            r8 = 0
            if (r3 == 0) goto L_0x0122
            r0 = r8
            goto L_0x017b
        L_0x0122:
            java.lang.String r3 = "default"
            boolean r3 = r3.equals(r0)
            if (r3 != 0) goto L_0x0176
            android.content.Context r3 = r13.mContext
            android.content.res.Resources r3 = r3.getResources()
            android.content.Context r9 = r13.mContext
            java.lang.String r9 = r9.getPackageName()
            java.lang.String r10 = "raw"
            int r3 = r3.getIdentifier(r0, r10, r9)
            if (r3 == 0) goto L_0x0176
            android.content.Context r3 = r13.mContext
            java.lang.String r3 = r3.getPackageName()
            java.lang.String r9 = java.lang.String.valueOf(r3)
            int r9 = r9.length()
            int r9 = r9 + 24
            java.lang.String r10 = java.lang.String.valueOf(r0)
            int r10 = r10.length()
            int r9 = r9 + r10
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>(r9)
            java.lang.String r9 = "android.resource://"
            r10.append(r9)
            r10.append(r3)
            java.lang.String r3 = "/raw/"
            r10.append(r3)
            r10.append(r0)
            java.lang.String r0 = r10.toString()
            android.net.Uri r0 = android.net.Uri.parse(r0)
            goto L_0x017b
        L_0x0176:
            r0 = 2
            android.net.Uri r0 = android.media.RingtoneManager.getDefaultUri(r0)
        L_0x017b:
            android.app.PendingIntent r3 = r13.zzu(r14)
            boolean r9 = com.google.firebase.messaging.FirebaseMessagingService.zzal(r14)
            if (r9 == 0) goto L_0x01b9
            android.content.Intent r8 = new android.content.Intent
            java.lang.String r9 = "com.google.firebase.messaging.NOTIFICATION_OPEN"
            r8.<init>(r9)
            zza(r8, r14)
            java.lang.String r9 = "pending_intent"
            r8.putExtra(r9, r3)
            android.content.Context r3 = r13.mContext
            java.util.concurrent.atomic.AtomicInteger r9 = r13.zzolw
            int r9 = r9.incrementAndGet()
            r10 = 1073741824(0x40000000, float:2.0)
            android.app.PendingIntent r3 = com.google.firebase.iid.zzz.zza(r3, r9, r8, r10)
            android.content.Intent r8 = new android.content.Intent
            java.lang.String r9 = "com.google.firebase.messaging.NOTIFICATION_DISMISS"
            r8.<init>(r9)
            zza(r8, r14)
            android.content.Context r9 = r13.mContext
            java.util.concurrent.atomic.AtomicInteger r11 = r13.zzolw
            int r11 = r11.incrementAndGet()
            android.app.PendingIntent r8 = com.google.firebase.iid.zzz.zza(r9, r11, r8, r10)
        L_0x01b9:
            r9 = r3
            r10 = r8
            boolean r3 = com.google.android.gms.common.util.zzs.isAtLeastO()
            if (r3 == 0) goto L_0x01de
            android.content.Context r3 = r13.mContext
            android.content.pm.ApplicationInfo r3 = r3.getApplicationInfo()
            int r3 = r3.targetSdkVersion
            r8 = 25
            if (r3 <= r8) goto L_0x01de
            java.lang.String r3 = "gcm.n.android_channel_id"
            java.lang.String r3 = zzd(r14, r3)
            java.lang.String r11 = r13.zzrz(r3)
            r3 = r13
            r8 = r0
            android.app.Notification r0 = r3.zza(r4, r5, r6, r7, r8, r9, r10, r11)
            goto L_0x0227
        L_0x01de:
            android.support.v4.app.NotificationCompat$Builder r3 = new android.support.v4.app.NotificationCompat$Builder
            android.content.Context r8 = r13.mContext
            r3.<init>(r8)
            android.support.v4.app.NotificationCompat$Builder r3 = r3.setAutoCancel(r1)
            android.support.v4.app.NotificationCompat$Builder r3 = r3.setSmallIcon(r6)
            boolean r6 = android.text.TextUtils.isEmpty(r4)
            if (r6 != 0) goto L_0x01f6
            r3.setContentTitle(r4)
        L_0x01f6:
            boolean r4 = android.text.TextUtils.isEmpty(r5)
            if (r4 != 0) goto L_0x020b
            r3.setContentText(r5)
            android.support.v4.app.NotificationCompat$BigTextStyle r4 = new android.support.v4.app.NotificationCompat$BigTextStyle
            r4.<init>()
            android.support.v4.app.NotificationCompat$BigTextStyle r4 = r4.bigText(r5)
            r3.setStyle(r4)
        L_0x020b:
            if (r7 == 0) goto L_0x0214
            int r4 = r7.intValue()
            r3.setColor(r4)
        L_0x0214:
            if (r0 == 0) goto L_0x0219
            r3.setSound(r0)
        L_0x0219:
            if (r9 == 0) goto L_0x021e
            r3.setContentIntent(r9)
        L_0x021e:
            if (r10 == 0) goto L_0x0223
            r3.setDeleteIntent(r10)
        L_0x0223:
            android.app.Notification r0 = r3.build()
        L_0x0227:
            java.lang.String r3 = "gcm.n.tag"
            java.lang.String r14 = zzd(r14, r3)
            r3 = 3
            boolean r3 = android.util.Log.isLoggable(r12, r3)
            if (r3 == 0) goto L_0x0239
            java.lang.String r3 = "Showing notification"
            android.util.Log.d(r12, r3)
        L_0x0239:
            android.content.Context r3 = r13.mContext
            java.lang.String r4 = "notification"
            java.lang.Object r3 = r3.getSystemService(r4)
            android.app.NotificationManager r3 = (android.app.NotificationManager) r3
            boolean r4 = android.text.TextUtils.isEmpty(r14)
            if (r4 == 0) goto L_0x0260
            long r4 = android.os.SystemClock.uptimeMillis()
            r14 = 37
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>(r14)
            java.lang.String r14 = "FCM-Notification:"
            r6.append(r14)
            r6.append(r4)
            java.lang.String r14 = r6.toString()
        L_0x0260:
            r3.notify(r14, r2, r0)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.messaging.zza.zzt(android.os.Bundle):boolean");
    }
}
