package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.CloudMessageManagerHelper;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestAccountEligibility extends BaseProvisionAPIRequest {
    /* access modifiers changed from: private */
    public static final String TAG = RequestAccountEligibility.class.getSimpleName();
    private static final long serialVersionUID = 6388797514968224882L;

    public RequestAccountEligibility(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", callFlowListener, new CloudMessageManagerHelper());
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String strbody = result.getDataString();
                String access$000 = RequestAccountEligibility.TAG;
                Log.d(access$000, "onComplete StatusCode: " + result.getStatusCode() + " strbody: " + IMSLog.checker(strbody));
                if (result.getStatusCode() == 503 || result.getStatusCode() == 429) {
                    int retryAfter = RequestAccountEligibility.this.checkRetryAfter(result);
                    if (retryAfter > 0) {
                        callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                        return;
                    }
                } else if (result.getStatusCode() == 200 && !TextUtils.isEmpty(strbody)) {
                    try {
                        boolean isEligible = new JSONObject(strbody).getJSONObject("serviceEligibilityList").getJSONArray("serviceEligibility").getJSONObject(0).getBoolean("isEligible");
                        String access$0002 = RequestAccountEligibility.TAG;
                        Log.d(access$0002, "account eligible: " + isEligible);
                        if (isEligible) {
                            RequestAccountEligibility.this.goSuccessfulCall();
                            return;
                        } else {
                            RequestAccountEligibility.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_ACCOUNT_NOT_ELIGIBLE);
                            return;
                        }
                    } catch (JSONException e) {
                        Log.e(RequestAccountEligibility.TAG, e.getMessage());
                    }
                }
                if (!RequestAccountEligibility.this.checkAndHandleCPSError(strbody)) {
                    RequestAccountEligibility.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = RequestAccountEligibility.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                RequestAccountEligibility.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/eligibility/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new RequestAccountEligibility(callback, cloudMessageManagerHelper);
    }
}
