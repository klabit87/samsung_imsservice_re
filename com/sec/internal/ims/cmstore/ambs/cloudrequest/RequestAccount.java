package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestAccount extends BaseProvisionAPIRequest {
    private static final String ACCOUNT_STATUS_Active = "Active";
    private static final String ACCOUNT_STATUS_PROVISIONED = "Provisioned";
    /* access modifiers changed from: private */
    public static final String TAG = RequestAccount.class.getSimpleName();
    private static final long serialVersionUID = -8780447710529534093L;

    public RequestAccount(final IAPICallFlowListener callFlowListener) {
        super("application/json", callFlowListener, new CloudMessageManagerHelper());
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                int retryAfter;
                String strbody = result.getDataString();
                String access$000 = RequestAccount.TAG;
                Log.d(access$000, "onComplete StatusCode: " + result.getStatusCode() + " strbody: " + IMSLog.checker(strbody));
                if (result.getStatusCode() == 200 && !TextUtils.isEmpty(strbody)) {
                    try {
                        JSONArray jsonArray = new JSONObject(strbody).getJSONObject("serviceAccountList").getJSONArray("serviceAccount");
                        if (jsonArray != null) {
                            if (jsonArray.length() != 0) {
                                String status = jsonArray.getJSONObject(0).getString("status");
                                String access$0002 = RequestAccount.TAG;
                                Log.d(access$0002, "200OK non empty response, status: " + status);
                                if (!RequestAccount.ACCOUNT_STATUS_Active.equals(status)) {
                                    if (!RequestAccount.ACCOUNT_STATUS_PROVISIONED.equals(status)) {
                                        RequestAccount.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC);
                                        return;
                                    }
                                }
                                RequestAccount.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_REQACCOUNT_EXIST_USER);
                                return;
                            }
                        }
                        RequestAccount.this.goSuccessfulCall(ReqConstant.HAPPY_PATH_REQACCOUNT_GET_TC);
                        return;
                    } catch (JSONException e) {
                        Log.e(RequestAccount.TAG, e.getMessage());
                    }
                } else if ((result.getStatusCode() == 503 || result.getStatusCode() == 429) && (retryAfter = RequestAccount.this.checkRetryAfter(result)) > 0) {
                    callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                    return;
                }
                if (!RequestAccount.this.checkAndHandleCPSError(strbody)) {
                    RequestAccount.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = RequestAccount.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                RequestAccount.this.goFailedCall();
            }
        });
    }

    public static void handleExternalUserOptIn(IAPICallFlowListener listener) {
        listener.onSuccessfulCall(new RequestAccount(listener), ReqConstant.HAPPY_PATH_BINARY_SMS_PROVISIONED);
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new RequestAccount(callback);
    }
}
