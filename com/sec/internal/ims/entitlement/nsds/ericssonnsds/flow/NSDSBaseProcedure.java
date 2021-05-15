package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.log.IMSLog;

public abstract class NSDSBaseProcedure extends Handler {
    protected static final int BASE_OP_MAX_RETRY = 4;
    public static final int EXEC_ENTITLEMENT_OP_WITH_CHALLENGE = 2;
    private static final String LOG_TAG = NSDSBaseProcedure.class.getSimpleName();
    public static final int RESPONSE_RECEIVED = 1;
    protected BaseFlowImpl mBaseFlowImpl;
    protected Context mContext;
    protected String mImeiForUA;
    protected boolean mIncludeAuthorizationHeader;
    protected Messenger mMessenger;
    protected int mRetryCount = 0;
    protected long mRetryInterval = 0;
    protected String mUserAgent;
    protected String mVersion;

    public abstract NSDSRequest[] buildRequests(NSDSCommonParameters nSDSCommonParameters);

    /* access modifiers changed from: protected */
    public abstract void executeOperationWithChallenge();

    /* access modifiers changed from: protected */
    public abstract Message getResponseMessage();

    /* access modifiers changed from: protected */
    public abstract boolean processResponse(Bundle bundle);

    public abstract boolean shouldIncludeAuthHeader();

    public NSDSBaseProcedure(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = baseFlowImpl;
        this.mMessenger = messenger;
        this.mVersion = version;
        this.mUserAgent = null;
        this.mImeiForUA = null;
    }

    public NSDSBaseProcedure(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String version, String userAgent, String imei) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = baseFlowImpl;
        this.mMessenger = messenger;
        this.mVersion = version;
        this.mUserAgent = userAgent;
        this.mImeiForUA = imei;
    }

    public boolean isResponseAkaChallenge(Response3gppAuthentication response3GppAuthentication) {
        return response3GppAuthentication != null && response3GppAuthentication.responseCode == 1003;
    }

    /* access modifiers changed from: protected */
    public Response3gppAuthentication getResponse3gppAuthenticatoin(Bundle bundleNSDSResponses) {
        if (bundleNSDSResponses != null) {
            return (Response3gppAuthentication) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH);
        }
        return null;
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        boolean shouldIgnoreChallenge = true;
        if (i != 1) {
            if (i != 2) {
                String str = LOG_TAG;
                IMSLog.i(str, "Unknown flow request: " + msg.what);
                return;
            }
            executeOperationWithChallenge();
        } else if (isResponseAkaChallenge(getResponse3gppAuthenticatoin(msg.getData()))) {
            Message requestMessage = (Message) msg.getData().getParcelable(BaseFlowImpl.KEY_REQUEST_MESSAGE);
            Response3gppAuthentication response3gppAuthentication = getResponse3gppAuthenticatoin(msg.getData());
            if (requestMessage == null || requestMessage.arg1 != 1) {
                shouldIgnoreChallenge = false;
            }
            if (requestMessage == null || shouldIgnoreChallenge) {
                reportResult(msg.getData());
            } else {
                this.mBaseFlowImpl.resubmitWithChallenge(requestMessage, response3gppAuthentication);
            }
        } else if (processResponse(msg.getData())) {
            reportResult(msg.getData());
        }
    }

    private void reportResult(Bundle bundleNSDSResponses) {
        try {
            if (this.mMessenger != null) {
                Message responseMessage = getResponseMessage();
                responseMessage.setData(bundleNSDSResponses);
                this.mMessenger.send(responseMessage);
                return;
            }
            IMSLog.i(LOG_TAG, "mMessenger is null:");
        } catch (RemoteException re) {
            String str = LOG_TAG;
            IMSLog.s(str, "Could not send response to the caller" + re.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public boolean retryForServerError(NSDSResponse nsdsResponse) {
        IMSLog.i(LOG_TAG, "retryForServerError:" + this.mRetryCount);
        if (nsdsResponse == null || nsdsResponse.responseCode != 1111 || this.mRetryCount >= 4) {
            if (!(nsdsResponse == null || nsdsResponse.responseCode == 1041)) {
                this.mRetryCount = 0;
            }
            return false;
        }
        IMSLog.i(LOG_TAG, "Failed with server error");
        this.mRetryCount++;
        sendEmptyMessageDelayed(2, this.mRetryInterval);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean retryForServerError(NSDSResponse[] nsdsResponses) {
        boolean serverError = false;
        int length = nsdsResponses.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            } else if (isServerErrror(nsdsResponses[i])) {
                serverError = true;
                break;
            } else {
                i++;
            }
        }
        IMnoNsdsStrategy mnoStrategy = getMnoVSimStrategy();
        if (!serverError || mnoStrategy == null || this.mRetryCount >= mnoStrategy.getBaseOperationMaxRetry()) {
            this.mRetryCount = 0;
            return false;
        }
        IMSLog.i(LOG_TAG, "Failed with server error. Retrying count:" + this.mRetryCount);
        this.mRetryCount = this.mRetryCount + 1;
        executeOperationWithChallenge();
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isServerErrror(NSDSResponse nsdsResponse) {
        return nsdsResponse != null && nsdsResponse.responseCode == 1111;
    }

    /* access modifiers changed from: protected */
    public String getVersionInfo() {
        return this.mVersion;
    }

    /* access modifiers changed from: protected */
    public String getUserAgent() {
        return this.mUserAgent;
    }

    /* access modifiers changed from: protected */
    public String getImeiForUA() {
        return this.mImeiForUA;
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsStrategy getMnoVSimStrategy() {
        return MnoNsdsStrategyCreator.getInstance(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex()).getMnoStrategy();
    }
}
