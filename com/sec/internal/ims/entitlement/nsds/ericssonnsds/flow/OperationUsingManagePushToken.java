package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import java.util.concurrent.atomic.AtomicInteger;

public class OperationUsingManagePushToken extends NSDSBaseProcedure {
    private static final String LOG_TAG = OperationUsingManagePushToken.class.getSimpleName();
    private String mClientId;
    private String mMSISDN;
    private int mOperation;
    private String mPushToken;
    private String mServiceName;

    public OperationUsingManagePushToken(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version) {
        super(looper, context, baseFlowImpl, messenger, version);
        Log.i(LOG_TAG, "created.");
    }

    public OperationUsingManagePushToken(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version, String userAgent, String imei) {
        super(looper, context, baseFlowImpl, messenger, version, userAgent, imei);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nsdsCommonParams) {
        NSDSClient nsdsClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger messageIdGenerator = new AtomicInteger();
        NSDSClient nSDSClient = nsdsClient;
        return new NSDSRequest[]{nSDSClient.buildAuthenticationRequest(messageIdGenerator.incrementAndGet(), true, nsdsCommonParams.getChallengeResponse(), nsdsCommonParams.getAkaToken(), (String) null, nsdsCommonParams.getImsiEap(), nsdsCommonParams.getDeviceId()), nSDSClient.buildManagePushTokenRequest(messageIdGenerator.incrementAndGet(), this.mMSISDN, this.mServiceName, this.mClientId, this.mOperation, this.mPushToken, nsdsCommonParams.getDeviceId())};
    }

    /* JADX WARNING: type inference failed for: r1v4, types: [android.os.Parcelable] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processResponse(android.os.Bundle r4) {
        /*
            r3 = this;
            r0 = 0
            if (r4 == 0) goto L_0x000d
            java.lang.String r1 = "managePushToken"
            android.os.Parcelable r1 = r4.getParcelable(r1)
            r0 = r1
            com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken r0 = (com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken) r0
            goto L_0x0015
        L_0x000d:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "responseCollection is null"
            android.util.Log.e(r1, r2)
        L_0x0015:
            boolean r1 = r3.retryForServerError((com.sec.internal.constants.ims.entitilement.data.NSDSResponse) r0)
            r1 = r1 ^ 1
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.OperationUsingManagePushToken.processResponse(android.os.Bundle):boolean");
    }

    public boolean shouldIncludeAuthHeader() {
        return true;
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        int respMsgId = 112;
        if (this.mOperation == 1) {
            respMsgId = 113;
        }
        return Message.obtain((Handler) null, respMsgId);
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        if (this.mOperation == 1) {
            this.mBaseFlowImpl.performOperation(42, this, new Messenger(this));
        } else {
            this.mBaseFlowImpl.performOperation(41, this, new Messenger(this));
        }
    }

    public void updatePushToken(String msisdn, String serviceName, String clientId, int operation, String pushToken, boolean includeAuthorizationHeader) {
        this.mMSISDN = msisdn;
        this.mServiceName = serviceName;
        this.mClientId = clientId;
        this.mOperation = operation;
        this.mPushToken = pushToken;
        this.mIncludeAuthorizationHeader = includeAuthorizationHeader;
        executeOperationWithChallenge();
    }

    public void registerVoWiFiPushToken(String msisdn, String clientId, String pushToken, String serviceName, boolean includeAuthorizationHeader, long retryInterval) {
        this.mRetryInterval = retryInterval;
        updatePushToken(msisdn, serviceName, clientId, 0, pushToken, includeAuthorizationHeader);
    }

    public void removeVoWiFiPushToken(String msisdn, String clientId, String pushToken, String serviceName, boolean includeAuthorizationHeader, long retryInterval) {
        this.mRetryInterval = retryInterval;
        updatePushToken(msisdn, serviceName, clientId, 1, pushToken, includeAuthorizationHeader);
    }

    public void removeVoWiFiPushToken(String msisdn, String clientId, String pushToken, String serviceName, String akaToken, String imsiEap, boolean includeAuthorizationHeader, long retryInterval) {
        this.mMSISDN = msisdn;
        this.mServiceName = serviceName;
        this.mClientId = clientId;
        this.mOperation = 1;
        this.mPushToken = pushToken;
        this.mRetryInterval = retryInterval;
        this.mIncludeAuthorizationHeader = includeAuthorizationHeader;
        executeOperationWithAkaToken(imsiEap, akaToken);
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithAkaToken(String imsiEap, String akaToken) {
        this.mBaseFlowImpl.performOperationWithAkaToken(42, imsiEap, akaToken, this, new Messenger(this));
    }
}
