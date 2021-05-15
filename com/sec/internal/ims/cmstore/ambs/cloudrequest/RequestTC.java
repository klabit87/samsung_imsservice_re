package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
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

public class RequestTC extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = -1949112470222946734L;
    /* access modifiers changed from: private */
    public final String TAG = RequestTC.class.getSimpleName();
    /* access modifiers changed from: private */
    public String mTcId = null;
    /* access modifiers changed from: private */
    public String mTcType = ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL;

    public RequestTC(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super("application/json", callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                int retryAfter;
                String strBody = result.getDataString();
                String access$000 = RequestTC.this.TAG;
                Log.d(access$000, "onComplete StatusCode: " + result.getStatusCode() + " strbodyEmpty:" + TextUtils.isEmpty(strBody) + " strbody: " + IMSLog.checker(strBody));
                if (result.getStatusCode() == 200) {
                    if (!TextUtils.isEmpty(strBody)) {
                        try {
                            String unused = RequestTC.this.mTcId = new JSONObject(strBody).getJSONObject("tc").getString("id");
                            CloudMessagePreferenceManager.getInstance().saveTermConditionId(RequestTC.this.mTcId);
                            if (!ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL.equals(RequestTC.this.mTcType) && !"Text".equals(RequestTC.this.mTcType)) {
                                RequestTC.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
                            }
                            RequestTC.this.goSuccessfulCall();
                            return;
                        } catch (JSONException ex) {
                            Log.e(RequestTC.this.TAG, ex.getMessage());
                        }
                    }
                } else if ((result.getStatusCode() == 503 || result.getStatusCode() == 429) && (retryAfter = RequestTC.this.checkRetryAfter(result)) > 0) {
                    callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                    return;
                }
                RequestTC.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }

            public void onFail(IOException arg1) {
                String access$000 = RequestTC.this.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                RequestTC.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_CPS_DEFAULT);
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.CPS_HOST_NAME + "/tc/v1/" + ATTGlobalVariables.MSG_STORE_SERVICE_NAME + "?contentType=Url");
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new RequestTC(callback, cloudMessageManagerHelper);
    }
}
