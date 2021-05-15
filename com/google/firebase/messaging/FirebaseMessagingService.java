package com.google.firebase.messaging;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.iid.zzb;
import com.google.firebase.iid.zzz;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class FirebaseMessagingService extends zzb {
    private static final Queue<String> zzoma = new ArrayDeque(10);

    static boolean zzal(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        return "1".equals(bundle.getString("google.c.a.e"));
    }

    static void zzr(Bundle bundle) {
        Iterator it = bundle.keySet().iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (str != null && str.startsWith("google.c.")) {
                it.remove();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0061  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x017a  */
    /* JADX WARNING: Removed duplicated region for block: B:97:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void handleIntent(android.content.Intent r12) {
        /*
            r11 = this;
            java.lang.String r0 = r12.getAction()
            if (r0 != 0) goto L_0x0008
            java.lang.String r0 = ""
        L_0x0008:
            int r1 = r0.hashCode()
            r2 = 75300319(0x47cfddf, float:2.973903E-36)
            r3 = -1
            r4 = 0
            r5 = 1
            if (r1 == r2) goto L_0x0024
            r2 = 366519424(0x15d8a480, float:8.750124E-26)
            if (r1 == r2) goto L_0x001a
            goto L_0x002e
        L_0x001a:
            java.lang.String r1 = "com.google.android.c2dm.intent.RECEIVE"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002e
            r0 = r4
            goto L_0x002f
        L_0x0024:
            java.lang.String r1 = "com.google.firebase.messaging.NOTIFICATION_DISMISS"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002e
            r0 = r5
            goto L_0x002f
        L_0x002e:
            r0 = r3
        L_0x002f:
            java.lang.String r1 = "FirebaseMessaging"
            if (r0 == 0) goto L_0x0061
            if (r0 == r5) goto L_0x0053
            java.lang.String r0 = "Unknown intent action: "
            java.lang.String r12 = r12.getAction()
            java.lang.String r12 = java.lang.String.valueOf(r12)
            int r2 = r12.length()
            if (r2 == 0) goto L_0x004a
            java.lang.String r12 = r0.concat(r12)
            goto L_0x004f
        L_0x004a:
            java.lang.String r12 = new java.lang.String
            r12.<init>(r0)
        L_0x004f:
            android.util.Log.d(r1, r12)
            goto L_0x0060
        L_0x0053:
            android.os.Bundle r0 = r12.getExtras()
            boolean r0 = zzal(r0)
            if (r0 == 0) goto L_0x0060
            com.google.firebase.messaging.zzd.zzh(r11, r12)
        L_0x0060:
            return
        L_0x0061:
            java.lang.String r0 = "google.message_id"
            java.lang.String r2 = r12.getStringExtra(r0)
            boolean r6 = android.text.TextUtils.isEmpty(r2)
            r7 = 3
            if (r6 == 0) goto L_0x0070
        L_0x006e:
            r6 = r4
            goto L_0x00af
        L_0x0070:
            java.util.Queue<java.lang.String> r6 = zzoma
            boolean r6 = r6.contains(r2)
            if (r6 == 0) goto L_0x009a
            boolean r6 = android.util.Log.isLoggable(r1, r7)
            if (r6 == 0) goto L_0x0098
            java.lang.String r6 = "Received duplicate message: "
            java.lang.String r8 = java.lang.String.valueOf(r2)
            int r9 = r8.length()
            if (r9 == 0) goto L_0x008f
            java.lang.String r6 = r6.concat(r8)
            goto L_0x0095
        L_0x008f:
            java.lang.String r8 = new java.lang.String
            r8.<init>(r6)
            r6 = r8
        L_0x0095:
            android.util.Log.d(r1, r6)
        L_0x0098:
            r6 = r5
            goto L_0x00af
        L_0x009a:
            java.util.Queue<java.lang.String> r6 = zzoma
            int r6 = r6.size()
            r8 = 10
            if (r6 < r8) goto L_0x00a9
            java.util.Queue<java.lang.String> r6 = zzoma
            r6.remove()
        L_0x00a9:
            java.util.Queue<java.lang.String> r6 = zzoma
            r6.add(r2)
            goto L_0x006e
        L_0x00af:
            r8 = 2
            if (r6 != 0) goto L_0x0174
            java.lang.String r6 = "message_type"
            java.lang.String r6 = r12.getStringExtra(r6)
            java.lang.String r9 = "gcm"
            if (r6 != 0) goto L_0x00bd
            r6 = r9
        L_0x00bd:
            int r10 = r6.hashCode()
            switch(r10) {
                case -2062414158: goto L_0x00e3;
                case 102161: goto L_0x00db;
                case 814694033: goto L_0x00d0;
                case 814800675: goto L_0x00c5;
                default: goto L_0x00c4;
            }
        L_0x00c4:
            goto L_0x00ec
        L_0x00c5:
            java.lang.String r4 = "send_event"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x00ec
            r3 = r8
            goto L_0x00ec
        L_0x00d0:
            java.lang.String r4 = "send_error"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x00ec
            r3 = r7
            goto L_0x00ec
        L_0x00db:
            boolean r9 = r6.equals(r9)
            if (r9 == 0) goto L_0x00ec
            r3 = r4
            goto L_0x00ec
        L_0x00e3:
            java.lang.String r4 = "deleted_messages"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x00ec
            r3 = r5
        L_0x00ec:
            if (r3 == 0) goto L_0x0136
            if (r3 == r5) goto L_0x0132
            if (r3 == r8) goto L_0x012a
            if (r3 == r7) goto L_0x010f
            java.lang.String r12 = "Received message with unknown type: "
            java.lang.String r3 = java.lang.String.valueOf(r6)
            int r4 = r3.length()
            if (r4 == 0) goto L_0x0105
            java.lang.String r12 = r12.concat(r3)
            goto L_0x010b
        L_0x0105:
            java.lang.String r3 = new java.lang.String
            r3.<init>(r12)
            r12 = r3
        L_0x010b:
            android.util.Log.w(r1, r12)
            goto L_0x0174
        L_0x010f:
            java.lang.String r1 = r12.getStringExtra(r0)
            if (r1 != 0) goto L_0x011b
            java.lang.String r1 = "message_id"
            java.lang.String r1 = r12.getStringExtra(r1)
        L_0x011b:
            com.google.firebase.messaging.SendException r3 = new com.google.firebase.messaging.SendException
            java.lang.String r4 = "error"
            java.lang.String r12 = r12.getStringExtra(r4)
            r3.<init>(r12)
            r11.onSendError(r1, r3)
            goto L_0x0174
        L_0x012a:
            java.lang.String r12 = r12.getStringExtra(r0)
            r11.onMessageSent(r12)
            goto L_0x0174
        L_0x0132:
            r11.onDeletedMessages()
            goto L_0x0174
        L_0x0136:
            android.os.Bundle r1 = r12.getExtras()
            boolean r1 = zzal(r1)
            if (r1 == 0) goto L_0x0143
            com.google.firebase.messaging.zzd.zzf(r11, r12)
        L_0x0143:
            android.os.Bundle r1 = r12.getExtras()
            if (r1 != 0) goto L_0x014e
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
        L_0x014e:
            java.lang.String r3 = "android.support.content.wakelockid"
            r1.remove(r3)
            boolean r3 = com.google.firebase.messaging.zza.zzai(r1)
            if (r3 == 0) goto L_0x016c
            com.google.firebase.messaging.zza r3 = com.google.firebase.messaging.zza.zzfc(r11)
            boolean r3 = r3.zzt(r1)
            if (r3 != 0) goto L_0x0174
            boolean r3 = zzal(r1)
            if (r3 == 0) goto L_0x016c
            com.google.firebase.messaging.zzd.zzi(r11, r12)
        L_0x016c:
            com.google.firebase.messaging.RemoteMessage r12 = new com.google.firebase.messaging.RemoteMessage
            r12.<init>(r1)
            r11.onMessageReceived(r12)
        L_0x0174:
            boolean r12 = android.text.TextUtils.isEmpty(r2)
            if (r12 != 0) goto L_0x0189
            android.os.Bundle r12 = new android.os.Bundle
            r12.<init>()
            r12.putString(r0, r2)
            com.google.firebase.iid.zzk r0 = com.google.firebase.iid.zzk.zzfa(r11)
            r0.zzm(r8, r12)
        L_0x0189:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.messaging.FirebaseMessagingService.handleIntent(android.content.Intent):void");
    }

    public void onDeletedMessages() {
    }

    public void onMessageReceived(RemoteMessage remoteMessage) {
    }

    public void onMessageSent(String str) {
    }

    public void onSendError(String str, Exception exc) {
    }

    /* access modifiers changed from: protected */
    public final Intent zzp(Intent intent) {
        return zzz.zzclq().zzclr();
    }

    public final boolean zzq(Intent intent) {
        if (!"com.google.firebase.messaging.NOTIFICATION_OPEN".equals(intent.getAction())) {
            return false;
        }
        PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("pending_intent");
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Log.e("FirebaseMessaging", "Notification pending intent canceled");
            }
        }
        if (!zzal(intent.getExtras())) {
            return true;
        }
        zzd.zzg(this, intent);
        return true;
    }
}
