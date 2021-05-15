package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.log.IMSLog;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private Object mLock = new Object();

    public RegistrationIntentService() {
        super(TAG);
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onHandleIntent(android.content.Intent r10) {
        /*
            r9 = this;
            java.lang.String r0 = "gcm_sender_id"
            java.lang.String r0 = r10.getStringExtra(r0)
            java.lang.String r1 = "gcm_protocol_to_server"
            java.lang.String r1 = r10.getStringExtra(r1)
            java.lang.String r2 = "device_id"
            java.lang.String r2 = r10.getStringExtra(r2)
            java.lang.Object r3 = r9.mLock     // Catch:{ Exception -> 0x00a3 }
            monitor-enter(r3)     // Catch:{ Exception -> 0x00a3 }
            com.google.firebase.iid.FirebaseInstanceId r4 = com.google.firebase.iid.FirebaseInstanceId.getInstance()     // Catch:{ all -> 0x00a0 }
            boolean r5 = android.text.TextUtils.isEmpty(r0)     // Catch:{ all -> 0x00a0 }
            if (r5 == 0) goto L_0x0028
            java.lang.String r5 = TAG     // Catch:{ all -> 0x00a0 }
            java.lang.String r6 = "FCM_Sender_ID is not ready yet. Will get token once its ready"
            com.sec.internal.log.IMSLog.s(r5, r6)     // Catch:{ all -> 0x00a0 }
            monitor-exit(r3)     // Catch:{ all -> 0x00a0 }
            return
        L_0x0028:
            java.lang.String r5 = TAG     // Catch:{ all -> 0x00a0 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a0 }
            r6.<init>()     // Catch:{ all -> 0x00a0 }
            java.lang.String r7 = "FCMSenderID: "
            r6.append(r7)     // Catch:{ all -> 0x00a0 }
            r6.append(r0)     // Catch:{ all -> 0x00a0 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x00a0 }
            com.sec.internal.log.IMSLog.s(r5, r6)     // Catch:{ all -> 0x00a0 }
            java.lang.String r5 = "FCM"
            java.lang.String r5 = r4.getToken(r0, r5)     // Catch:{ all -> 0x00a0 }
            java.lang.String r6 = TAG     // Catch:{ all -> 0x00a0 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a0 }
            r7.<init>()     // Catch:{ all -> 0x00a0 }
            java.lang.String r8 = "FCM Registration Token: "
            r7.append(r8)     // Catch:{ all -> 0x00a0 }
            r7.append(r5)     // Catch:{ all -> 0x00a0 }
            java.lang.String r8 = " for FCMsenderId:"
            r7.append(r8)     // Catch:{ all -> 0x00a0 }
            r7.append(r0)     // Catch:{ all -> 0x00a0 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x00a0 }
            com.sec.internal.log.IMSLog.s(r6, r7)     // Catch:{ all -> 0x00a0 }
            r6 = 336068608(0x14080000, float:6.866245E-27)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a0 }
            r7.<init>()     // Catch:{ all -> 0x00a0 }
            java.lang.String r8 = "TKN:"
            r7.append(r8)     // Catch:{ all -> 0x00a0 }
            r7.append(r5)     // Catch:{ all -> 0x00a0 }
            java.lang.String r8 = ",SID:"
            r7.append(r8)     // Catch:{ all -> 0x00a0 }
            r7.append(r0)     // Catch:{ all -> 0x00a0 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x00a0 }
            com.sec.internal.log.IMSLog.c(r6, r7)     // Catch:{ all -> 0x00a0 }
            java.lang.String r6 = "managePushToken"
            boolean r6 = r6.equals(r1)     // Catch:{ all -> 0x00a0 }
            if (r6 == 0) goto L_0x0093
            r9.sendRegistrationToServer(r5, r2)     // Catch:{ all -> 0x00a0 }
            java.lang.String r6 = "sent_token_to_server"
            r7 = 1
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.save((android.content.Context) r9, (java.lang.String) r2, (java.lang.String) r6, (boolean) r7)     // Catch:{ all -> 0x00a0 }
            goto L_0x009e
        L_0x0093:
            java.lang.String r6 = "broadcastToAndsfApp"
            boolean r6 = r6.equals(r1)     // Catch:{ all -> 0x00a0 }
            if (r6 == 0) goto L_0x009e
            r9.notifyAndsfPushTokenReady(r5, r0)     // Catch:{ all -> 0x00a0 }
        L_0x009e:
            monitor-exit(r3)     // Catch:{ all -> 0x00a0 }
            goto L_0x00d3
        L_0x00a0:
            r4 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x00a0 }
            throw r4     // Catch:{ Exception -> 0x00a3 }
        L_0x00a3:
            r3 = move-exception
            java.lang.String r4 = TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Failed to complete token refresh"
            r5.append(r6)
            java.lang.String r6 = r3.getMessage()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.s(r4, r5)
            java.lang.String r4 = "managePushToken"
            boolean r4 = r4.equals(r1)
            if (r4 == 0) goto L_0x00d3
            java.lang.String r4 = "sent_token_to_server"
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.remove(r9, r2, r4)
            r4 = 0
            java.lang.String r5 = "sent_token_to_server"
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.save((android.content.Context) r9, (java.lang.String) r2, (java.lang.String) r5, (boolean) r4)
        L_0x00d3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds.RegistrationIntentService.onHandleIntent(android.content.Intent):void");
    }

    private void sendRegistrationToServer(String token, String deviceUid) {
        String str = TAG;
        IMSLog.s(str, "Received token from FCM:" + token);
        String encodedPushToken = Base64.encodeToString(token.getBytes(), 2);
        String pushTokenFromSharedPref = NSDSSharedPrefHelper.get(this, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        if (TextUtils.isEmpty(pushTokenFromSharedPref)) {
            pushTokenFromSharedPref = "";
        }
        if (!pushTokenFromSharedPref.equals(encodedPushToken)) {
            NSDSSharedPrefHelper.save((Context) this, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN, encodedPushToken);
        }
    }

    private void notifyAndsfPushTokenReady(String token, String senderId) {
        IMSLog.i(TAG, "notifyNonConnMgrPushTokenReady()");
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.DEVICE_PUSH_TOKEN_READY);
        intent.putExtra("device_push_token", token);
        intent.putExtra("gcm_sender_id", senderId);
        sendBroadcast(intent);
    }
}
