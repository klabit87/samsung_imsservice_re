package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import java.util.concurrent.atomic.AtomicInteger;

public class SIMDeviceActivation extends NSDSBaseProcedure {
    private static final String LOG_TAG = SIMDeviceActivation.class.getSimpleName();
    private String mClientId;
    private String mDeviceGroup;
    private String mMSISDN;
    private String mPushToken;
    private String mServiceName;
    private String mVIMSI;

    public SIMDeviceActivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version) {
        super(looper, context, baseFlowImpl, messenger, version);
        Log.i(LOG_TAG, "created.");
    }

    public SIMDeviceActivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version, String userAgent, String imei) {
        super(looper, context, baseFlowImpl, messenger, version, userAgent, imei);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nsdsCommonParams) {
        NSDSClient nsdsClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger messageIdGenerator = new AtomicInteger();
        NSDSClient nSDSClient = nsdsClient;
        return new NSDSRequest[]{nSDSClient.buildAuthenticationRequest(messageIdGenerator.incrementAndGet(), true, nsdsCommonParams.getChallengeResponse(), nsdsCommonParams.getAkaToken(), (String) null, nsdsCommonParams.getImsiEap(), nsdsCommonParams.getDeviceId()), nSDSClient.buildManageConnectivityRequest(messageIdGenerator.incrementAndGet(), 0, this.mVIMSI, (String) null, this.mDeviceGroup, (String) null, nsdsCommonParams.getDeviceId()), nSDSClient.buildManagePushTokenRequest(messageIdGenerator.incrementAndGet(), this.mMSISDN, this.mServiceName, this.mClientId, 0, this.mPushToken, nsdsCommonParams.getDeviceId()), nsdsClient.buildGetMSISDNRequest(messageIdGenerator.incrementAndGet(), nsdsCommonParams.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        return Message.obtain((Handler) null, 103);
    }

    public boolean shouldIncludeAuthHeader() {
        return this.mIncludeAuthorizationHeader;
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        this.mBaseFlowImpl.performOperation(32, this, new Messenger(this));
    }

    /* JADX WARNING: type inference failed for: r4v3, types: [android.os.Parcelable] */
    /* JADX WARNING: type inference failed for: r4v5, types: [android.os.Parcelable] */
    /* JADX WARNING: type inference failed for: r4v7, types: [android.os.Parcelable] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processResponse(android.os.Bundle r8) {
        /*
            r7 = this;
            r0 = 0
            r1 = 0
            r2 = 0
            r3 = 1
            if (r8 == 0) goto L_0x0033
            java.lang.String r4 = "manageConnectivity"
            android.os.Parcelable r4 = r8.getParcelable(r4)
            r0 = r4
            com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity r0 = (com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity) r0
            java.lang.String r4 = "managePushToken"
            android.os.Parcelable r4 = r8.getParcelable(r4)
            r1 = r4
            com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken r1 = (com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken) r1
            java.lang.String r4 = "getMSISDN"
            android.os.Parcelable r4 = r8.getParcelable(r4)
            r2 = r4
            com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN r2 = (com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN) r2
            r4 = 3
            com.sec.internal.constants.ims.entitilement.data.NSDSResponse[] r4 = new com.sec.internal.constants.ims.entitilement.data.NSDSResponse[r4]
            r5 = 0
            r4[r5] = r2
            r4[r3] = r0
            r6 = 2
            r4[r6] = r1
            boolean r6 = r7.retryForServerError((com.sec.internal.constants.ims.entitilement.data.NSDSResponse[]) r4)
            if (r6 == 0) goto L_0x0033
            return r5
        L_0x0033:
            if (r0 == 0) goto L_0x0068
            if (r1 == 0) goto L_0x0068
            if (r2 == 0) goto L_0x0068
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "handleSimDeviceActivationResponse : responseManageConnectivity respCode:"
            r5.append(r6)
            int r6 = r0.responseCode
            r5.append(r6)
            java.lang.String r6 = "responseManagePushToken respCode:"
            r5.append(r6)
            int r6 = r1.responseCode
            r5.append(r6)
            java.lang.String r6 = "responseGetMsisdn respCode:"
            r5.append(r6)
            int r6 = r2.responseCode
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            goto L_0x0070
        L_0x0068:
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "one of the responses is null"
            android.util.Log.e(r4, r5)
        L_0x0070:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.SIMDeviceActivation.processResponse(android.os.Bundle):boolean");
    }

    public void activateSIMDevice(String clientId, String pushToken, boolean includeAuthorizationHeader, long retryInterval) {
        this.mRetryInterval = retryInterval;
        activateSIMDevice(NSDSNamespaces.NSDSSettings.DEVICE_GROUP_20, clientId, pushToken, NSDSHelper.getVIMSIforSIMDevice(this.mContext, this.mBaseFlowImpl.getSimManager().getImsi()), this.mBaseFlowImpl.getSimManager().getMsisdn(), includeAuthorizationHeader);
    }

    public void activateSIMDevice(String deviceGroup, String clientId, String pushToken, String vimsiEap, String msisdn, boolean includeAuthorizationHeader) {
        this.mVIMSI = vimsiEap;
        this.mMSISDN = msisdn;
        this.mDeviceGroup = deviceGroup;
        this.mServiceName = NSDSNamespaces.NSDSServices.SERVICE_CONNECTIVITY_MANAGER;
        this.mClientId = clientId;
        this.mPushToken = pushToken;
        this.mIncludeAuthorizationHeader = includeAuthorizationHeader;
        executeOperationWithChallenge();
    }
}
