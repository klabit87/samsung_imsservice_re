package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.FcmTokenDetail;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class NsdsInstanceIdListenerService extends FirebaseInstanceIdService {
    private static final String LOG_TAG = NsdsInstanceIdListenerService.class.getSimpleName();

    public void onTokenRefresh() {
        IMSLog.s(LOG_TAG, "onTokenRefresh()");
        for (FcmTokenDetail tokenDetail : getAllFcmTokenDetails()) {
            startTokenRefresh(tokenDetail.senderId, tokenDetail.protocolToServer, tokenDetail.deviceUid);
        }
    }

    private void startTokenRefresh(String senderId, String protocolToServer, String deviceUid) {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        intent.putExtra("gcm_sender_id", senderId);
        intent.putExtra(NSDSNamespaces.NSDSExtras.GCM_PROTOCOL_TO_SERVER, protocolToServer);
        intent.putExtra("device_id", deviceUid);
        startService(intent);
    }

    private List<FcmTokenDetail> getAllFcmTokenDetails() {
        Context context = getApplicationContext();
        Cursor cursor = context.getContentResolver().query(NSDSContractExt.GcmTokens.CONTENT_URI, new String[]{NSDSContractExt.GcmTokensColumns.SENDER_ID, NSDSContractExt.GcmTokensColumns.PROTOCOL_TO_SERVER, "device_uid"}, (String) null, (String[]) null, (String) null);
        try {
            List<FcmTokenDetail> fcmTokenDetails = new ArrayList<>();
            if (!(cursor == null || !cursor.moveToFirst() || cursor.getString(0) == null)) {
                FcmTokenDetail tokenDetail = new FcmTokenDetail();
                tokenDetail.senderId = cursor.getString(0);
                tokenDetail.protocolToServer = cursor.getString(1);
                tokenDetail.deviceUid = cursor.getString(2);
                fcmTokenDetails.add(tokenDetail);
            }
            if (cursor != null) {
                cursor.close();
            }
            return fcmTokenDetails;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }
}
