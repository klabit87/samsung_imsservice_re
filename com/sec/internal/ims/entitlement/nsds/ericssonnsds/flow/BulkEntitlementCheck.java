package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BulkEntitlementCheck extends NSDSBaseProcedure {
    private static final String LOG_TAG = BulkEntitlementCheck.class.getSimpleName();
    private boolean mIncludeGetMSISDN = false;
    private ArrayList<String> mServiceList = new ArrayList<>();

    public BulkEntitlementCheck(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version) {
        super(looper, context, baseFlowImpl, messenger, version);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nsdsCommonParams) {
        NSDSClient nsdsClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger messageIdGenerator = new AtomicInteger();
        NSDSRequest requestAuthentication = nsdsClient.buildAuthenticationRequest(messageIdGenerator.incrementAndGet(), true, nsdsCommonParams.getChallengeResponse(), nsdsCommonParams.getAkaToken(), (String) null, nsdsCommonParams.getImsiEap(), nsdsCommonParams.getDeviceId());
        NSDSRequest requestServiceEntitlementStatus = nsdsClient.buildServiceEntitlementStatusRequest(messageIdGenerator.incrementAndGet(), this.mServiceList, nsdsCommonParams.getDeviceId());
        if (this.mIncludeGetMSISDN) {
            return new NSDSRequest[]{requestAuthentication, requestServiceEntitlementStatus, nsdsClient.buildGetMSISDNRequest(messageIdGenerator.incrementAndGet(), nsdsCommonParams.getDeviceId())};
        }
        return new NSDSRequest[]{requestAuthentication, requestServiceEntitlementStatus};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        return Message.obtain((Handler) null, 101);
    }

    /* JADX WARNING: type inference failed for: r1v6, types: [android.os.Parcelable] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processResponse(android.os.Bundle r4) {
        /*
            r3 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "processResponse:"
            r1.append(r2)
            int r2 = r3.mRetryCount
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r0 = 0
            if (r4 == 0) goto L_0x0027
            java.lang.String r1 = "serviceEntitlementStatus"
            android.os.Parcelable r1 = r4.getParcelable(r1)
            r0 = r1
            com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus r0 = (com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus) r0
            goto L_0x002f
        L_0x0027:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "responseCollection is null"
            android.util.Log.e(r1, r2)
        L_0x002f:
            boolean r1 = r3.retryForServerError((com.sec.internal.constants.ims.entitilement.data.NSDSResponse) r0)
            r1 = r1 ^ 1
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BulkEntitlementCheck.processResponse(android.os.Bundle):boolean");
    }

    public boolean shouldIncludeAuthHeader() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        this.mBaseFlowImpl.performOperation(30, this, new Messenger(this));
    }

    public void checkBulkEntitlement(List<String> serviceList, boolean includeGetMSISN) {
        this.mServiceList.addAll(serviceList);
        this.mIncludeGetMSISDN = includeGetMSISN;
        executeOperationWithChallenge();
    }

    public void checkBulkEntitlement(List<String> serviceList, boolean includeGetMSISN, long retryInterval) {
        this.mRetryInterval = retryInterval;
        checkBulkEntitlement(serviceList, includeGetMSISN);
    }
}
