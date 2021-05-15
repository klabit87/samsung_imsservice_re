package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import java.util.concurrent.atomic.AtomicInteger;

public class RetrieveAkaToken extends NSDSBaseProcedure {
    private static final String LOG_TAG = RetrieveAkaToken.class.getSimpleName();
    private String mClientId;
    private String mDeviceGroup;
    private String mMSISDN;
    private String mPushToken;
    private String mServiceName;
    private String mVIMSI;

    public RetrieveAkaToken(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version) {
        super(looper, context, baseFlowImpl, messenger, version);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nsdsCommonParams) {
        return new NSDSRequest[]{this.mBaseFlowImpl.getNSDSClient().buildAuthenticationRequest(new AtomicInteger().incrementAndGet(), true, nsdsCommonParams.getChallengeResponse(), nsdsCommonParams.getAkaToken(), (String) null, nsdsCommonParams.getImsiEap(), nsdsCommonParams.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        return Message.obtain((Handler) null, 118);
    }

    public boolean shouldIncludeAuthHeader() {
        return this.mIncludeAuthorizationHeader;
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        this.mBaseFlowImpl.performOperation(47, this, new Messenger(this));
    }

    /* access modifiers changed from: protected */
    public boolean processResponse(Bundle bundleNSDSResponses) {
        Log.i(LOG_TAG, "processResponse for akatoken");
        if (bundleNSDSResponses != null) {
            Response3gppAuthentication response3gppAuthentication = (Response3gppAuthentication) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH);
            if (response3gppAuthentication != null) {
                String str = LOG_TAG;
                Log.i(str, "response3gppAuthentication responseCode:" + response3gppAuthentication.responseCode);
            }
            if (retryForServerError(new NSDSResponse[]{response3gppAuthentication})) {
                Log.i(LOG_TAG, "processResponse - server error");
                return false;
            }
        }
        return true;
    }

    public void retrieveAkaToken(String clientId, String pushToken, boolean includeAuthorizationHeader, long retryInterval) {
        this.mRetryInterval = retryInterval;
        retrieveAkaToken(NSDSNamespaces.NSDSSettings.DEVICE_GROUP_20, clientId, pushToken, NSDSHelper.getVIMSIforSIMDevice(this.mContext, this.mBaseFlowImpl.getSimManager().getImsi()), this.mBaseFlowImpl.getSimManager().getMsisdn(), includeAuthorizationHeader);
    }

    public void retrieveAkaToken(String deviceGroup, String clientId, String pushToken, String vimsiEap, String msisdn, boolean includeAuthorizationHeader) {
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
