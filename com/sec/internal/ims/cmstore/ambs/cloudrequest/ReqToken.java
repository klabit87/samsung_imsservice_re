package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
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
import java.util.HashMap;
import java.util.Map;

public class ReqToken extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = 1981673139716461230L;
    /* access modifiers changed from: private */
    public final String TAG = ReqToken.class.getSimpleName();

    public ReqToken(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.POST);
        setPostParams(makePostData());
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String strBody = result.getDataString();
                String access$000 = ReqToken.this.TAG;
                Log.d(access$000, "StatusCode: " + result.getStatusCode() + " strBody: " + IMSLog.checker(strBody));
                if (result.getStatusCode() != 200 || TextUtils.isEmpty(strBody)) {
                    if (result.getStatusCode() == 503 || result.getStatusCode() == 429) {
                        int retryAfter = ReqToken.this.checkRetryAfter(result);
                        if (retryAfter > 0) {
                            callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                            return;
                        }
                        return;
                    }
                    ReqToken.this.goFailedCall(CommonErrorName.DEFAULT_ERROR_TYPE);
                } else if (strBody.indexOf("atsToken=") >= 0) {
                    CloudMessagePreferenceManager.getInstance().saveAtsToken(ReqToken.this.removeLastNewLineChar(strBody.substring(strBody.indexOf("atsToken=") + "atsToken=".length())));
                    ReqToken.this.goSuccessfulCall();
                } else {
                    ReqToken.this.goFailedCall(CommonErrorName.DEFAULT_ERROR_TYPE);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = ReqToken.this.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                ReqToken.this.goFailedCall();
            }
        });
    }

    /* access modifiers changed from: private */
    public String removeLastNewLineChar(String atsToken) {
        if (atsToken == null || atsToken.length() <= 0 || atsToken.charAt(atsToken.length() - 1) != 10) {
            return atsToken;
        }
        return atsToken.substring(0, atsToken.length() - 1);
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.ACMS_HOST_NAME + "/commonLogin/nxsATS/TokenGen");
    }

    private Map<String, String> makePostData() {
        Map<String, String> postData = new HashMap<>();
        postData.put("TG_OP", "TokenGen");
        postData.put("appID", ATTGlobalVariables.APP_ID);
        postData.put("ctnID", CloudMessagePreferenceManager.getInstance().getUserCtn());
        postData.put("authZCode", CloudMessagePreferenceManager.getInstance().getAuthZCode());
        return postData;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new ReqToken(callback, cloudMessageManagerHelper);
    }
}
