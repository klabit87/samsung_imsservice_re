package com.google.firebase.iid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Base64;
import android.util.Log;
import com.google.android.gms.common.util.zzs;
import com.google.firebase.FirebaseApp;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public final class zzw {
    private final Context zzaiq;
    private String zzcs;
    private String zzold;
    private int zzole;
    private int zzolf = 0;

    public zzw(Context context) {
        this.zzaiq = context;
    }

    public static String zzb(KeyPair keyPair) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA1").digest(keyPair.getPublic().getEncoded());
            digest[0] = (byte) ((digest[0] & 15) + 112);
            return Base64.encodeToString(digest, 0, 8, 11);
        } catch (NoSuchAlgorithmException e) {
            Log.w("FirebaseInstanceId", "Unexpected error, device missing required algorithms");
            return null;
        }
    }

    private final synchronized void zzclp() {
        PackageInfo zzog = zzog(this.zzaiq.getPackageName());
        if (zzog != null) {
            this.zzold = Integer.toString(zzog.versionCode);
            this.zzcs = zzog.versionName;
        }
    }

    public static String zzf(FirebaseApp firebaseApp) {
        String gcmSenderId = firebaseApp.getOptions().getGcmSenderId();
        if (gcmSenderId != null) {
            return gcmSenderId;
        }
        String applicationId = firebaseApp.getOptions().getApplicationId();
        if (!applicationId.startsWith("1:")) {
            return applicationId;
        }
        String[] split = applicationId.split(":");
        if (split.length < 2) {
            return null;
        }
        String str = split[1];
        if (str.isEmpty()) {
            return null;
        }
        return str;
    }

    private final PackageInfo zzog(String str) {
        try {
            return this.zzaiq.getPackageManager().getPackageInfo(str, 0);
        } catch (PackageManager.NameNotFoundException e) {
            String valueOf = String.valueOf(e);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 23);
            sb.append("Failed to find package ");
            sb.append(valueOf);
            Log.w("FirebaseInstanceId", sb.toString());
            return null;
        }
    }

    public final synchronized int zzcll() {
        if (this.zzolf != 0) {
            return this.zzolf;
        }
        PackageManager packageManager = this.zzaiq.getPackageManager();
        if (packageManager.checkPermission("com.google.android.c2dm.permission.SEND", "com.google.android.gms") == -1) {
            Log.e("FirebaseInstanceId", "Google Play services missing or without correct permission.");
            return 0;
        }
        if (!zzs.isAtLeastO()) {
            Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
            intent.setPackage("com.google.android.gms");
            List<ResolveInfo> queryIntentServices = packageManager.queryIntentServices(intent, 0);
            if (queryIntentServices != null && queryIntentServices.size() > 0) {
                this.zzolf = 1;
                return 1;
            }
        }
        Intent intent2 = new Intent("com.google.iid.TOKEN_REQUEST");
        intent2.setPackage("com.google.android.gms");
        List<ResolveInfo> queryBroadcastReceivers = packageManager.queryBroadcastReceivers(intent2, 0);
        if (queryBroadcastReceivers == null || queryBroadcastReceivers.size() <= 0) {
            Log.w("FirebaseInstanceId", "Failed to resolve IID implementation package, falling back");
            if (zzs.isAtLeastO()) {
                this.zzolf = 2;
            } else {
                this.zzolf = 1;
            }
            return this.zzolf;
        }
        this.zzolf = 2;
        return 2;
    }

    public final synchronized String zzclm() {
        if (this.zzold == null) {
            zzclp();
        }
        return this.zzold;
    }

    public final synchronized String zzcln() {
        if (this.zzcs == null) {
            zzclp();
        }
        return this.zzcs;
    }

    public final synchronized int zzclo() {
        PackageInfo zzog;
        if (this.zzole == 0 && (zzog = zzog("com.google.android.gms")) != null) {
            this.zzole = zzog.versionCode;
        }
        return this.zzole;
    }
}
