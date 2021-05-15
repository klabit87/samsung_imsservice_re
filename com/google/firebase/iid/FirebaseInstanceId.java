package com.google.firebase.iid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FirebaseInstanceId {
    private static Map<String, FirebaseInstanceId> zzimu = new ArrayMap();
    private static final long zzokn = TimeUnit.HOURS.toSeconds(8);
    private static zzaa zzoko;
    private static ScheduledThreadPoolExecutor zzokp;
    private KeyPair zzimx;
    private final FirebaseApp zzmwq;
    private final zzw zzokq;
    private final zzx zzokr;
    private boolean zzoks = false;
    private boolean zzokt;

    private FirebaseInstanceId(FirebaseApp firebaseApp, zzw zzw) {
        if (zzw.zzf(firebaseApp) != null) {
            this.zzmwq = firebaseApp;
            this.zzokq = zzw;
            this.zzokr = new zzx(firebaseApp.getApplicationContext(), zzw);
            this.zzokt = zzcli();
            if (zzclk()) {
                zzclb();
                return;
            }
            return;
        }
        throw new IllegalStateException("FirebaseInstanceId failed to initialize, FirebaseApp is missing project ID");
    }

    public static FirebaseInstanceId getInstance() {
        return getInstance(FirebaseApp.getInstance());
    }

    public static synchronized FirebaseInstanceId getInstance(FirebaseApp firebaseApp) {
        FirebaseInstanceId firebaseInstanceId;
        synchronized (FirebaseInstanceId.class) {
            firebaseInstanceId = zzimu.get(firebaseApp.getOptions().getApplicationId());
            if (firebaseInstanceId == null) {
                if (zzoko == null) {
                    zzoko = new zzaa(firebaseApp.getApplicationContext());
                }
                firebaseInstanceId = new FirebaseInstanceId(firebaseApp, new zzw(firebaseApp.getApplicationContext()));
                zzimu.put(firebaseApp.getOptions().getApplicationId(), firebaseInstanceId);
            }
        }
        return firebaseInstanceId;
    }

    private final synchronized void startSync() {
        if (!this.zzoks) {
            zzcd(0);
        }
    }

    private final synchronized KeyPair zzawp() {
        if (this.zzimx == null) {
            this.zzimx = zzoko.zzrs("");
        }
        if (this.zzimx == null) {
            this.zzimx = zzoko.zzrq("");
        }
        return this.zzimx;
    }

    private final String zzb(String str, String str2, Bundle bundle) throws IOException {
        bundle.putString("scope", str2);
        bundle.putString("sender", str);
        bundle.putString("subtype", str);
        bundle.putString("appid", getId());
        bundle.putString("gmp_app_id", this.zzmwq.getOptions().getApplicationId());
        bundle.putString("gmsv", Integer.toString(this.zzokq.zzclo()));
        bundle.putString("osv", Integer.toString(Build.VERSION.SDK_INT));
        bundle.putString("app_ver", this.zzokq.zzclm());
        bundle.putString("app_ver_name", this.zzokq.zzcln());
        bundle.putString("cliv", "fiid-12211000");
        Bundle zzah = this.zzokr.zzah(bundle);
        if (zzah != null) {
            String string = zzah.getString("registration_id");
            if (string != null) {
                return string;
            }
            String string2 = zzah.getString("unregistered");
            if (string2 != null) {
                return string2;
            }
            String string3 = zzah.getString("error");
            if ("RST".equals(string3)) {
                zzclg();
                throw new IOException("INSTANCE_ID_RESET");
            } else if (string3 != null) {
                throw new IOException(string3);
            } else {
                String valueOf = String.valueOf(zzah);
                StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 21);
                sb.append("Unexpected response: ");
                sb.append(valueOf);
                Log.w("FirebaseInstanceId", sb.toString(), new Throwable());
                throw new IOException("SERVICE_NOT_AVAILABLE");
            }
        } else {
            throw new IOException("SERVICE_NOT_AVAILABLE");
        }
    }

    static void zzb(Runnable runnable, long j) {
        synchronized (FirebaseInstanceId.class) {
            if (zzokp == null) {
                zzokp = new ScheduledThreadPoolExecutor(1);
            }
            zzokp.schedule(runnable, j, TimeUnit.SECONDS);
        }
    }

    private final void zzclb() {
        zzab zzclc = zzclc();
        if (zzclc == null || zzclc.zzru(this.zzokq.zzclm()) || zzoko.zzcls() != null) {
            startSync();
        }
    }

    static zzaa zzcle() {
        return zzoko;
    }

    static boolean zzclf() {
        if (!Log.isLoggable("FirebaseInstanceId", 3)) {
            return Build.VERSION.SDK_INT == 23 && Log.isLoggable("FirebaseInstanceId", 3);
        }
        return true;
    }

    private final boolean zzcli() {
        ApplicationInfo applicationInfo;
        Context applicationContext = this.zzmwq.getApplicationContext();
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences("com.google.firebase.messaging", 0);
        if (sharedPreferences.contains("auto_init")) {
            return sharedPreferences.getBoolean("auto_init", true);
        }
        try {
            PackageManager packageManager = applicationContext.getPackageManager();
            if (!(packageManager == null || (applicationInfo = packageManager.getApplicationInfo(applicationContext.getPackageName(), 128)) == null || applicationInfo.metaData == null || !applicationInfo.metaData.containsKey("firebase_messaging_auto_init_enabled"))) {
                return applicationInfo.metaData.getBoolean("firebase_messaging_auto_init_enabled");
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return zzclj();
    }

    private final boolean zzclj() {
        try {
            Class.forName("com.google.firebase.messaging.FirebaseMessaging");
            return true;
        } catch (ClassNotFoundException e) {
            Context applicationContext = this.zzmwq.getApplicationContext();
            Intent intent = new Intent("com.google.firebase.MESSAGING_EVENT");
            intent.setPackage(applicationContext.getPackageName());
            ResolveInfo resolveService = applicationContext.getPackageManager().resolveService(intent, 0);
            return (resolveService == null || resolveService.serviceInfo == null) ? false : true;
        }
    }

    public void deleteInstanceId() throws IOException {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            Bundle bundle = new Bundle();
            bundle.putString("iid-operation", SoftphoneContract.SoftphoneAddress.DELETE);
            bundle.putString(SoftphoneContract.SoftphoneAddress.DELETE, "1");
            zzb("*", "*", bundle);
            zzclg();
            return;
        }
        throw new IOException("MAIN_THREAD");
    }

    public void deleteToken(String str, String str2) throws IOException {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            Bundle bundle = new Bundle();
            bundle.putString(SoftphoneContract.SoftphoneAddress.DELETE, "1");
            zzb(str, str2, bundle);
            zzoko.zzg("", str, str2);
            return;
        }
        throw new IOException("MAIN_THREAD");
    }

    /* access modifiers changed from: package-private */
    public final FirebaseApp getApp() {
        return this.zzmwq;
    }

    public long getCreationTime() {
        return zzoko.zzrp("");
    }

    public String getId() {
        zzclb();
        return zzw.zzb(zzawp());
    }

    public String getToken() {
        zzab zzclc = zzclc();
        if (zzclc == null || zzclc.zzru(this.zzokq.zzclm())) {
            startSync();
        }
        if (zzclc != null) {
            return zzclc.zzlnm;
        }
        return null;
    }

    public String getToken(String str, String str2) throws IOException {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            zzab zzq = zzoko.zzq("", str, str2);
            if (zzq != null && !zzq.zzru(this.zzokq.zzclm())) {
                return zzq.zzlnm;
            }
            String zzb = zzb(str, str2, new Bundle());
            if (zzb != null) {
                zzoko.zza("", str, str2, zzb, this.zzokq.zzclm());
            }
            return zzb;
        }
        throw new IOException("MAIN_THREAD");
    }

    /* access modifiers changed from: package-private */
    public final synchronized void zzcd(long j) {
        zzb(new zzac(this, this.zzokq, Math.min(Math.max(30, j << 1), zzokn)), j);
        this.zzoks = true;
    }

    /* access modifiers changed from: package-private */
    public final zzab zzclc() {
        return zzoko.zzq("", zzw.zzf(this.zzmwq), "*");
    }

    /* access modifiers changed from: package-private */
    public final String zzcld() throws IOException {
        return getToken(zzw.zzf(this.zzmwq), "*");
    }

    /* access modifiers changed from: package-private */
    public final synchronized void zzclg() {
        zzoko.zzawz();
        this.zzimx = null;
        if (zzclk()) {
            startSync();
        }
    }

    /* access modifiers changed from: package-private */
    public final void zzclh() {
        zzoko.zzrr("");
        startSync();
    }

    public final synchronized boolean zzclk() {
        return this.zzokt;
    }

    /* access modifiers changed from: package-private */
    public final synchronized void zzcy(boolean z) {
        this.zzoks = z;
    }

    public final synchronized void zzcz(boolean z) {
        SharedPreferences.Editor edit = this.zzmwq.getApplicationContext().getSharedPreferences("com.google.firebase.messaging", 0).edit();
        edit.putBoolean("auto_init", z);
        edit.apply();
        if (!this.zzokt && z) {
            zzclb();
        }
        this.zzokt = z;
    }

    public final synchronized void zzrl(String str) {
        zzoko.zzrl(str);
        startSync();
    }

    /* access modifiers changed from: package-private */
    public final void zzrm(String str) throws IOException {
        zzab zzclc = zzclc();
        if (zzclc == null || zzclc.zzru(this.zzokq.zzclm())) {
            throw new IOException("token not available");
        }
        Bundle bundle = new Bundle();
        String valueOf = String.valueOf("/topics/");
        String valueOf2 = String.valueOf(str);
        bundle.putString("gcm.topic", valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf));
        String str2 = zzclc.zzlnm;
        String valueOf3 = String.valueOf("/topics/");
        String valueOf4 = String.valueOf(str);
        zzb(str2, valueOf4.length() != 0 ? valueOf3.concat(valueOf4) : new String(valueOf3), bundle);
    }

    /* access modifiers changed from: package-private */
    public final void zzrn(String str) throws IOException {
        zzab zzclc = zzclc();
        if (zzclc == null || zzclc.zzru(this.zzokq.zzclm())) {
            throw new IOException("token not available");
        }
        Bundle bundle = new Bundle();
        String valueOf = String.valueOf("/topics/");
        String valueOf2 = String.valueOf(str);
        bundle.putString("gcm.topic", valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf));
        bundle.putString(SoftphoneContract.SoftphoneAddress.DELETE, "1");
        String str2 = zzclc.zzlnm;
        String valueOf3 = String.valueOf("/topics/");
        String valueOf4 = String.valueOf(str);
        zzb(str2, valueOf4.length() != 0 ? valueOf3.concat(valueOf4) : new String(valueOf3), bundle);
    }
}
