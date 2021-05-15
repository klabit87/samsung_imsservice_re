package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

public class RequestPat extends BaseProvisionAPIRequest {
    /* access modifiers changed from: private */
    public static final String TAG = RequestPat.class.getSimpleName();
    private static final long serialVersionUID = 2825360222614488236L;

    public RequestPat(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                int retryAfter;
                String strBody = result.getDataString();
                if ((result.getStatusCode() == 200 || result.getStatusCode() == 206) && !TextUtils.isEmpty(strBody)) {
                    try {
                        String pat = new JSONObject(strBody).getJSONObject("token").getString("Encrypted");
                        if (!TextUtils.isEmpty(pat)) {
                            CloudMessagePreferenceManager.getInstance().savePATAndTime(pat);
                            RequestPat.this.goSuccessfulCall();
                            return;
                        }
                    } catch (JSONException ex) {
                        Log.e(RequestPat.TAG, ex.getMessage());
                    }
                } else if (result.getStatusCode() == 302 || result.getStatusCode() == 404) {
                    RequestPat.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_SESSION_ID);
                    return;
                } else if (result.getStatusCode() == 400) {
                    RequestPat.this.goFailedCall();
                    return;
                } else if ((result.getStatusCode() == 503 || result.getStatusCode() == 429) && (retryAfter = RequestPat.this.checkRetryAfter(result)) > 0) {
                    callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                    return;
                }
                RequestPat.this.goFailedCall();
            }

            public void onFail(IOException arg1) {
                String access$100 = RequestPat.TAG;
                Log.e(access$100, "Http request onFail: " + arg1.getMessage());
                RequestPat.this.goFailedCall();
            }
        });
    }

    public void updateUrl() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("https://");
            sb.append(getMsDomainAndSessionHelper());
            sb.append("/line/token?ValidFor=");
            sb.append(URLEncoder.encode(String.valueOf(ReqConstant.PAT_LIFE_CYCLE), "UTF-8"));
            sb.append("&Revision=");
            sb.append(URLEncoder.encode("1", "UTF-8"));
            sb.append("&ApplicationId=");
            sb.append(URLEncoder.encode(ATTGlobalVariables.APPLICATION_ID, "UTF-8"));
            sb.append("&ContextInfo=");
            sb.append(URLEncoder.encode("version=" + ATTGlobalVariables.VERSION_NAME, "UTF-8"));
            setUrl(sb.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String getMsDomainAndSessionHelper() {
        return CloudMessagePreferenceManager.getInstance().getRedirectDomain() + "/handset/session" + CloudMessagePreferenceManager.getInstance().getMsgStoreSessionId();
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new RequestPat(callback, cloudMessageManagerHelper);
    }
}
