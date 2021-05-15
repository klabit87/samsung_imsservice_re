package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestCreateAccount extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = -8278931619238563919L;
    /* access modifiers changed from: private */
    public final String TAG = RequestCreateAccount.class.getSimpleName();

    public RequestCreateAccount(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.POST);
        setPostBody(makePostData());
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String strBody = result.getDataString();
                String access$000 = RequestCreateAccount.this.TAG;
                Log.d(access$000, "onComplete StatusCode: " + result.getStatusCode() + " strbody: " + IMSLog.checker(strBody));
                if (result.getStatusCode() == 201) {
                    CloudMessagePreferenceManager.getInstance().saveLastApiRequestCreateAccount(true);
                    if (CloudMessagePreferenceManager.getInstance().getUserTbs()) {
                        CloudMessagePreferenceManager.getInstance().saveUserTbsRquired(false);
                    }
                    RequestCreateAccount.this.goSuccessfulCall();
                    return;
                }
                if (result.getStatusCode() == 503 || result.getStatusCode() == 429) {
                    int retryAfter = RequestCreateAccount.this.checkRetryAfter(result);
                    if (retryAfter > 0) {
                        callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                        return;
                    }
                } else if (result.getStatusCode() == 200 && RequestCreateAccount.this.checkAndHandleCPSError(strBody)) {
                    return;
                }
                RequestCreateAccount.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }

            public void onFail(IOException arg1) {
                String access$000 = RequestCreateAccount.this.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                RequestCreateAccount.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME);
    }

    private JSONObject makePostData() {
        String strTcId = CloudMessagePreferenceManager.getInstance().getTermConditionId();
        try {
            JSONObject root = new JSONObject();
            JSONObject tc = new JSONObject();
            JSONObject tcParent = new JSONObject();
            String str = this.TAG;
            Log.d(str, "id: " + strTcId);
            tc.put("id", strTcId);
            tc.put("action", "Accept");
            tcParent.put("tc", tc);
            root.put("createServiceAccountRequest", tcParent);
            return root;
        } catch (JSONException e) {
            Log.e(this.TAG, e.getMessage());
            return null;
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new RequestCreateAccount(callback, cloudMessageManagerHelper);
    }
}
