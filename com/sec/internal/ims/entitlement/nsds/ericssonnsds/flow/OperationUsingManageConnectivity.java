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

public class OperationUsingManageConnectivity extends NSDSBaseProcedure {
    private static final String LOG_TAG = OperationUsingManageConnectivity.class.getSimpleName();
    protected String mDeviceGroup;
    protected int mOperation;
    protected String mRemoteDeviceId;
    protected String mVIMSI;

    public OperationUsingManageConnectivity(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version, String userAgent, String imei) {
        super(looper, context, baseFlowImpl, messenger, version, userAgent, imei);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nsdsCommonParams) {
        NSDSClient nsdsClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger messageIdGenerator = new AtomicInteger();
        NSDSClient nSDSClient = nsdsClient;
        return new NSDSRequest[]{nSDSClient.buildAuthenticationRequest(messageIdGenerator.incrementAndGet(), true, nsdsCommonParams.getChallengeResponse(), nsdsCommonParams.getAkaToken(), (String) null, nsdsCommonParams.getImsiEap(), nsdsCommonParams.getDeviceId()), nSDSClient.buildManageConnectivityRequest(messageIdGenerator.incrementAndGet(), this.mOperation, this.mVIMSI, this.mRemoteDeviceId, this.mDeviceGroup, (String) null, nsdsCommonParams.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        int respMsgId = 102;
        int i = this.mOperation;
        if (i == 1) {
            respMsgId = 109;
        } else if (i == 0) {
            respMsgId = 102;
        } else if (i == 2) {
            respMsgId = 111;
        }
        return Message.obtain((Handler) null, respMsgId);
    }

    /* JADX WARNING: type inference failed for: r1v6, types: [android.os.Parcelable] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean processResponse(android.os.Bundle r4) {
        /*
            r3 = this;
            r0 = 0
            if (r4 == 0) goto L_0x000d
            java.lang.String r1 = "manageConnectivity"
            android.os.Parcelable r1 = r4.getParcelable(r1)
            r0 = r1
            com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity r0 = (com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity) r0
            goto L_0x0015
        L_0x000d:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "responseCollection is null"
            android.util.Log.e(r1, r2)
        L_0x0015:
            int r1 = r3.mOperation
            r2 = 2
            if (r1 == r2) goto L_0x0023
            boolean r1 = r3.retryForServerError((com.sec.internal.constants.ims.entitilement.data.NSDSResponse) r0)
            if (r1 != 0) goto L_0x0021
            goto L_0x0023
        L_0x0021:
            r1 = 0
            goto L_0x0024
        L_0x0023:
            r1 = 1
        L_0x0024:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.OperationUsingManageConnectivity.processResponse(android.os.Bundle):boolean");
    }

    public boolean shouldIncludeAuthHeader() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        int baseFlowMsgType = 31;
        int i = this.mOperation;
        if (i == 1) {
            baseFlowMsgType = 38;
        } else if (i == 0) {
            baseFlowMsgType = 31;
        } else if (i == 2) {
            baseFlowMsgType = 40;
        }
        this.mBaseFlowImpl.performOperation(baseFlowMsgType, this, new Messenger(this));
    }
}
