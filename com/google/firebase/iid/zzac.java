package com.google.firebase.iid;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;
import java.io.IOException;

final class zzac implements Runnable {
    private final zzw zzokq;
    private final long zzolp;
    private final PowerManager.WakeLock zzolq;
    private final FirebaseInstanceId zzolr;

    zzac(FirebaseInstanceId firebaseInstanceId, zzw zzw, long j) {
        this.zzolr = firebaseInstanceId;
        this.zzokq = zzw;
        this.zzolp = j;
        PowerManager.WakeLock newWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(1, "fiid-sync");
        this.zzolq = newWakeLock;
        newWakeLock.setReferenceCounted(false);
    }

    private final boolean zzclt() {
        zzab zzclc = this.zzolr.zzclc();
        if (zzclc != null && !zzclc.zzru(this.zzokq.zzclm())) {
            return true;
        }
        try {
            String zzcld = this.zzolr.zzcld();
            if (zzcld == null) {
                Log.e("FirebaseInstanceId", "Token retrieval failed: null");
                return false;
            }
            if (Log.isLoggable("FirebaseInstanceId", 3)) {
                Log.d("FirebaseInstanceId", "Token successfully retrieved");
            }
            if (zzclc == null || (zzclc != null && !zzcld.equals(zzclc.zzlnm))) {
                Context context = getContext();
                Intent intent = new Intent("com.google.firebase.iid.TOKEN_REFRESH");
                Intent intent2 = new Intent("com.google.firebase.INSTANCE_ID_EVENT");
                intent2.setClass(context, FirebaseInstanceIdReceiver.class);
                intent2.putExtra("wrapped_intent", intent);
                context.sendBroadcast(intent2);
            }
            return true;
        } catch (IOException | SecurityException e) {
            String valueOf = String.valueOf(e.getMessage());
            Log.e("FirebaseInstanceId", valueOf.length() != 0 ? "Token retrieval failed: ".concat(valueOf) : new String("Token retrieval failed: "));
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
        if (zzrv(r1) != false) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean zzclu() {
        /*
            r3 = this;
        L_0x0000:
            com.google.firebase.iid.FirebaseInstanceId r0 = r3.zzolr
            monitor-enter(r0)
            com.google.firebase.iid.zzaa r1 = com.google.firebase.iid.FirebaseInstanceId.zzcle()     // Catch:{ all -> 0x0029 }
            java.lang.String r1 = r1.zzcls()     // Catch:{ all -> 0x0029 }
            if (r1 != 0) goto L_0x0018
            java.lang.String r1 = "FirebaseInstanceId"
            java.lang.String r2 = "topic sync succeeded"
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x0029 }
            r1 = 1
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            return r1
        L_0x0018:
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            boolean r0 = r3.zzrv(r1)
            if (r0 != 0) goto L_0x0021
            r0 = 0
            return r0
        L_0x0021:
            com.google.firebase.iid.zzaa r0 = com.google.firebase.iid.FirebaseInstanceId.zzcle()
            r0.zzro(r1)
            goto L_0x0000
        L_0x0029:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0029 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzac.zzclu():boolean");
    }

    private final boolean zzrv(String str) {
        String str2;
        String[] split = str.split("!");
        if (split.length == 2) {
            String str3 = split[0];
            String str4 = split[1];
            char c = 65535;
            try {
                int hashCode = str3.hashCode();
                if (hashCode != 83) {
                    if (hashCode == 85) {
                        if (str3.equals("U")) {
                            c = 1;
                        }
                    }
                } else if (str3.equals("S")) {
                    c = 0;
                }
                if (c == 0) {
                    this.zzolr.zzrm(str4);
                    if (FirebaseInstanceId.zzclf()) {
                        str2 = "subscribe operation succeeded";
                    }
                } else if (c == 1) {
                    this.zzolr.zzrn(str4);
                    if (FirebaseInstanceId.zzclf()) {
                        str2 = "unsubscribe operation succeeded";
                    }
                }
                Log.d("FirebaseInstanceId", str2);
            } catch (IOException e) {
                String valueOf = String.valueOf(e.getMessage());
                Log.e("FirebaseInstanceId", valueOf.length() != 0 ? "Topic sync failed: ".concat(valueOf) : new String("Topic sync failed: "));
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final Context getContext() {
        return this.zzolr.getApp().getApplicationContext();
    }

    public final void run() {
        FirebaseInstanceId firebaseInstanceId;
        this.zzolq.acquire();
        try {
            boolean z = true;
            this.zzolr.zzcy(true);
            if (this.zzokq.zzcll() == 0) {
                z = false;
            }
            if (!z) {
                firebaseInstanceId = this.zzolr;
            } else {
                if (!zzclv()) {
                    new zzad(this).zzclw();
                } else if (!zzclt() || !zzclu()) {
                    this.zzolr.zzcd(this.zzolp);
                } else {
                    firebaseInstanceId = this.zzolr;
                }
            }
            firebaseInstanceId.zzcy(false);
        } finally {
            this.zzolq.release();
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean zzclv() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService("connectivity");
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
