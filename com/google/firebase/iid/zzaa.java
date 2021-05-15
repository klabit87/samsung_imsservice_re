package com.google.firebase.iid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.google.android.gms.common.util.zzx;
import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

final class zzaa {
    private Context zzaiq;
    private SharedPreferences zzioc;

    public zzaa(Context context) {
        this(context, "com.google.android.gms.appid");
    }

    private zzaa(Context context, String str) {
        this.zzaiq = context;
        this.zzioc = context.getSharedPreferences(str, 0);
        String valueOf = String.valueOf(str);
        String valueOf2 = String.valueOf("-no-backup");
        File file = new File(zzx.getNoBackupFilesDir(this.zzaiq), valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf));
        if (!file.exists()) {
            try {
                if (file.createNewFile() && !isEmpty()) {
                    Log.i("FirebaseInstanceId", "App restored, clearing state");
                    zzawz();
                    FirebaseInstanceId.getInstance().zzclg();
                }
            } catch (IOException e) {
                if (Log.isLoggable("FirebaseInstanceId", 3)) {
                    String valueOf3 = String.valueOf(e.getMessage());
                    Log.d("FirebaseInstanceId", valueOf3.length() != 0 ? "Error creating file in no backup dir: ".concat(valueOf3) : new String("Error creating file in no backup dir: "));
                }
            }
        }
    }

    private final synchronized boolean isEmpty() {
        return this.zzioc.getAll().isEmpty();
    }

    private static String zzbk(String str, String str2) {
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 3 + String.valueOf(str2).length());
        sb.append(str);
        sb.append("|S|");
        sb.append(str2);
        return sb.toString();
    }

    private static String zzp(String str, String str2, String str3) {
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 4 + String.valueOf(str2).length() + String.valueOf(str3).length());
        sb.append(str);
        sb.append("|T|");
        sb.append(str2);
        sb.append("|");
        sb.append(str3);
        return sb.toString();
    }

    public final synchronized void zza(String str, String str2, String str3, String str4, String str5) {
        String zzc = zzab.zzc(str4, str5, System.currentTimeMillis());
        if (zzc != null) {
            SharedPreferences.Editor edit = this.zzioc.edit();
            edit.putString(zzp(str, str2, str3), zzc);
            edit.commit();
        }
    }

    public final synchronized void zzawz() {
        this.zzioc.edit().clear().commit();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized java.lang.String zzcls() {
        /*
            r4 = this;
            monitor-enter(r4)
            android.content.SharedPreferences r0 = r4.zzioc     // Catch:{ all -> 0x0025 }
            java.lang.String r1 = "topic_operaion_queue"
            r2 = 0
            java.lang.String r0 = r0.getString(r1, r2)     // Catch:{ all -> 0x0025 }
            if (r0 == 0) goto L_0x0023
            java.lang.String r1 = ","
            java.lang.String[] r0 = r0.split(r1)     // Catch:{ all -> 0x0025 }
            int r1 = r0.length     // Catch:{ all -> 0x0025 }
            r3 = 1
            if (r1 <= r3) goto L_0x0023
            r1 = r0[r3]     // Catch:{ all -> 0x0025 }
            boolean r1 = android.text.TextUtils.isEmpty(r1)     // Catch:{ all -> 0x0025 }
            if (r1 != 0) goto L_0x0023
            r0 = r0[r3]     // Catch:{ all -> 0x0025 }
            monitor-exit(r4)
            return r0
        L_0x0023:
            monitor-exit(r4)
            return r2
        L_0x0025:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzaa.zzcls():java.lang.String");
    }

    public final synchronized void zzg(String str, String str2, String str3) {
        String zzp = zzp(str, str2, str3);
        SharedPreferences.Editor edit = this.zzioc.edit();
        edit.remove(zzp);
        edit.commit();
    }

    public final synchronized zzab zzq(String str, String str2, String str3) {
        return zzab.zzrt(this.zzioc.getString(zzp(str, str2, str3), (String) null));
    }

    public final synchronized void zzrl(String str) {
        String string = this.zzioc.getString("topic_operaion_queue", "");
        StringBuilder sb = new StringBuilder(String.valueOf(string).length() + 1 + String.valueOf(str).length());
        sb.append(string);
        sb.append(",");
        sb.append(str);
        this.zzioc.edit().putString("topic_operaion_queue", sb.toString()).apply();
    }

    public final synchronized boolean zzro(String str) {
        boolean z;
        String string = this.zzioc.getString("topic_operaion_queue", "");
        String valueOf = String.valueOf(",");
        String valueOf2 = String.valueOf(str);
        if (string.startsWith(valueOf2.length() != 0 ? valueOf.concat(valueOf2) : new String(valueOf))) {
            String valueOf3 = String.valueOf(",");
            String valueOf4 = String.valueOf(str);
            this.zzioc.edit().putString("topic_operaion_queue", string.substring((valueOf4.length() != 0 ? valueOf3.concat(valueOf4) : new String(valueOf3)).length())).apply();
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public final synchronized long zzrp(String str) {
        String string = this.zzioc.getString(zzbk(str, "cre"), (String) null);
        if (string != null) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public final synchronized KeyPair zzrq(String str) {
        KeyPair zzawn;
        zzawn = zza.zzawn();
        long currentTimeMillis = System.currentTimeMillis();
        SharedPreferences.Editor edit = this.zzioc.edit();
        edit.putString(zzbk(str, "|P|"), Base64.encodeToString(zzawn.getPublic().getEncoded(), 11));
        edit.putString(zzbk(str, "|K|"), Base64.encodeToString(zzawn.getPrivate().getEncoded(), 11));
        edit.putString(zzbk(str, "cre"), Long.toString(currentTimeMillis));
        edit.commit();
        return zzawn;
    }

    public final synchronized void zzrr(String str) {
        String concat = String.valueOf(str).concat("|T|");
        SharedPreferences.Editor edit = this.zzioc.edit();
        for (String next : this.zzioc.getAll().keySet()) {
            if (next.startsWith(concat)) {
                edit.remove(next);
            }
        }
        edit.commit();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0079, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final synchronized java.security.KeyPair zzrs(java.lang.String r5) {
        /*
            r4 = this;
            monitor-enter(r4)
            android.content.SharedPreferences r0 = r4.zzioc     // Catch:{ all -> 0x007a }
            java.lang.String r1 = "|P|"
            java.lang.String r1 = zzbk(r5, r1)     // Catch:{ all -> 0x007a }
            r2 = 0
            java.lang.String r0 = r0.getString(r1, r2)     // Catch:{ all -> 0x007a }
            android.content.SharedPreferences r1 = r4.zzioc     // Catch:{ all -> 0x007a }
            java.lang.String r3 = "|K|"
            java.lang.String r5 = zzbk(r5, r3)     // Catch:{ all -> 0x007a }
            java.lang.String r5 = r1.getString(r5, r2)     // Catch:{ all -> 0x007a }
            if (r0 == 0) goto L_0x0078
            if (r5 != 0) goto L_0x0021
            goto L_0x0078
        L_0x0021:
            r1 = 8
            byte[] r0 = android.util.Base64.decode(r0, r1)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            byte[] r5 = android.util.Base64.decode(r5, r1)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            java.lang.String r1 = "RSA"
            java.security.KeyFactory r1 = java.security.KeyFactory.getInstance(r1)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            java.security.spec.X509EncodedKeySpec r3 = new java.security.spec.X509EncodedKeySpec     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            r3.<init>(r0)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            java.security.PublicKey r0 = r1.generatePublic(r3)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            java.security.spec.PKCS8EncodedKeySpec r3 = new java.security.spec.PKCS8EncodedKeySpec     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            r3.<init>(r5)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            java.security.PrivateKey r5 = r1.generatePrivate(r3)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            java.security.KeyPair r1 = new java.security.KeyPair     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            r1.<init>(r0, r5)     // Catch:{ NoSuchAlgorithmException | InvalidKeySpecException -> 0x004a }
            monitor-exit(r4)
            return r1
        L_0x004a:
            r5 = move-exception
            java.lang.String r0 = "FirebaseInstanceId"
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x007a }
            java.lang.String r1 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x007a }
            int r1 = r1.length()     // Catch:{ all -> 0x007a }
            int r1 = r1 + 19
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x007a }
            r3.<init>(r1)     // Catch:{ all -> 0x007a }
            java.lang.String r1 = "Invalid key stored "
            r3.append(r1)     // Catch:{ all -> 0x007a }
            r3.append(r5)     // Catch:{ all -> 0x007a }
            java.lang.String r5 = r3.toString()     // Catch:{ all -> 0x007a }
            android.util.Log.w(r0, r5)     // Catch:{ all -> 0x007a }
            com.google.firebase.iid.FirebaseInstanceId r5 = com.google.firebase.iid.FirebaseInstanceId.getInstance()     // Catch:{ all -> 0x007a }
            r5.zzclg()     // Catch:{ all -> 0x007a }
            monitor-exit(r4)
            return r2
        L_0x0078:
            monitor-exit(r4)
            return r2
        L_0x007a:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzaa.zzrs(java.lang.String):java.security.KeyPair");
    }
}
