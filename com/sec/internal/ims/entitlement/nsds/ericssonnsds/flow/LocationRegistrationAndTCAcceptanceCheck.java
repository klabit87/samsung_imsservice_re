package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.ims.entitlement.nsds.NSDSModuleBase;
import com.sec.internal.ims.entitlement.nsds.NSDSModuleFactory;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationRegistrationAndTCAcceptanceCheck extends NSDSBaseProcedure {
    private static final String LOG_TAG = LocationRegistrationAndTCAcceptanceCheck.class.getSimpleName();
    private Messenger mMessenger;
    private String mServiceFingerPrint;

    public LocationRegistrationAndTCAcceptanceCheck(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version) {
        super(looper, context, baseFlowImpl, messenger, version);
        this.mMessenger = messenger;
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nsdsCommonParams) {
        NSDSClient nsdsClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger messageIdGenerator = new AtomicInteger();
        return new NSDSRequest[]{nsdsClient.buildAuthenticationRequest(messageIdGenerator.incrementAndGet(), true, nsdsCommonParams.getChallengeResponse(), nsdsCommonParams.getAkaToken(), (String) null, nsdsCommonParams.getImsiEap(), nsdsCommonParams.getDeviceId()), nsdsClient.buildManageLocationAndTCRequest(messageIdGenerator.incrementAndGet(), this.mServiceFingerPrint, nsdsCommonParams.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        return Message.obtain((Handler) null, 104);
    }

    /* JADX WARNING: type inference failed for: r1v6, types: [android.os.Parcelable] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processResponse(android.os.Bundle r5) {
        /*
            r4 = this;
            r0 = 0
            if (r5 == 0) goto L_0x000c
            java.lang.String r1 = "manageLocationAndTC"
            android.os.Parcelable r1 = r5.getParcelable(r1)
            r0 = r1
            com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC r0 = (com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC) r0
        L_0x000c:
            boolean r1 = r4.retryForServerError(r0)
            if (r1 == 0) goto L_0x0014
            r1 = 0
            return r1
        L_0x0014:
            if (r0 == 0) goto L_0x0039
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "handleResponseManageLocationAndTC : messageId:"
            r2.append(r3)
            int r3 = r0.messageId
            r2.append(r3)
            java.lang.String r3 = "responseCode:"
            r2.append(r3)
            int r3 = r0.responseCode
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
        L_0x0039:
            r1 = 1
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.LocationRegistrationAndTCAcceptanceCheck.processResponse(android.os.Bundle):boolean");
    }

    /* access modifiers changed from: protected */
    public boolean retryForServerError(NSDSResponse nsdsResponse) {
        NSDSModuleBase nsdsModule;
        if (super.retryForServerError(nsdsResponse)) {
            return true;
        }
        if (nsdsResponse != null && nsdsResponse.responseCode == 1041) {
            if (this.mRetryCount < 1) {
                String str = LOG_TAG;
                Log.i(str, "Failed with ERROR_INVALID_FINGERPRINT. Retrying count:" + this.mRetryCount);
                this.mRetryCount = this.mRetryCount + 1;
                List<String> serviceList = new ArrayList<>();
                serviceList.add("vowifi");
                new BulkEntitlementCheck(getLooper(), this.mContext, this.mBaseFlowImpl, this.mMessenger, "1.0").checkBulkEntitlement(serviceList, true);
                return true;
            } else if (this.mRetryCount != 1 || (nsdsModule = NSDSModuleFactory.getInstance().getNsdsModule(this.mBaseFlowImpl.getSimManager())) == null) {
                return false;
            } else {
                nsdsModule.deactivateSimDevice(1);
                return true;
            }
        }
        return false;
    }

    public boolean shouldIncludeAuthHeader() {
        return this.mIncludeAuthorizationHeader;
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        this.mBaseFlowImpl.performOperation(33, this, new Messenger(this));
    }

    public void checkLocationAndTC(String serviceFingerPrint, boolean includeAuthorizationHeader, long retryInterval) {
        this.mServiceFingerPrint = serviceFingerPrint;
        this.mIncludeAuthorizationHeader = includeAuthorizationHeader;
        this.mRetryInterval = retryInterval;
        executeOperationWithChallenge();
    }
}
