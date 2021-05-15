package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
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

public class RequestDeleteAccount extends BaseProvisionAPIRequest {
    /* access modifiers changed from: private */
    public static final String TAG = RequestDeleteAccount.class.getSimpleName();
    private static final long serialVersionUID = -6638272236079743088L;

    public RequestDeleteAccount(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.DELETE);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                int retryAfter;
                String strBody = result.getDataString();
                String access$000 = RequestDeleteAccount.TAG;
                Log.d(access$000, "onComplete StatusCode: " + result.getStatusCode() + " strbody: " + IMSLog.checker(strBody));
                if (result.getStatusCode() != 200) {
                    if ((result.getStatusCode() == 503 || result.getStatusCode() == 429) && (retryAfter = RequestDeleteAccount.this.checkRetryAfter(result)) > 0) {
                        callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                        return;
                    }
                    if (!RequestDeleteAccount.this.checkAndHandleCPSError(strBody)) {
                        RequestDeleteAccount.this.goFailedCall();
                    }
                } else if (TextUtils.isEmpty(strBody)) {
                    RequestDeleteAccount.this.goFailedCall();
                } else {
                    try {
                        JSONArray serviceAccount = new JSONObject(strBody).getJSONObject("deletedServiceAccountList").getJSONArray("serviceAccount");
                        if (serviceAccount == null || serviceAccount.length() == 0 || serviceAccount.getJSONObject(0) == null) {
                            RequestDeleteAccount.this.goFailedCall();
                        } else if (serviceAccount.getJSONObject(0).has("serviceId")) {
                            Log.d(RequestDeleteAccount.TAG, "deleted successfully");
                            RequestDeleteAccount.this.goSuccessfulCall();
                        } else {
                            RequestDeleteAccount.this.goFailedCall();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onFail(IOException arg1) {
                RequestDeleteAccount.this.goFailedCall();
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/svcaccount/v1/msgstoreoemtbs?deleteAll=true");
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new RequestDeleteAccount(callback, cloudMessageManagerHelper);
    }
}
