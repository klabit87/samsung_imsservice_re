package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReqZCode extends BaseProvisionAPIRequest {
    public static final String REQUET_ID_PRIX = "AMBS";
    /* access modifiers changed from: private */
    public static final String TAG = ReqZCode.class.getSimpleName();
    public static final String ZCODE_SEND_FROM = "74611666";
    public static final String ZCODE_SEND_FROM_FFA = "74611888";
    public static final String ZCODE_SMS_BEGIN = "AT&T FREE MESSAGE - This is an automated message, please ignore.";
    private static volatile String lastRequestKey = null;
    private static final int mMaxRequestIdDigitLength = 4;
    private static final long serialVersionUID = -6914421196386591646L;

    public ReqZCode(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.POST);
        setPostParams(makePostData());
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String strBody = result.getDataString();
                String access$000 = ReqZCode.TAG;
                Log.d(access$000, "onComplete StatusCode: " + result.getStatusCode() + " strbody: " + IMSLog.checker(strBody));
                if (result.getStatusCode() == 200) {
                    if (!TextUtils.isEmpty(strBody)) {
                        ReqZCode.this.goFailedCall(AmbsUtils.findErrorCode(strBody, "errorCode=", '&'));
                    }
                } else if (result.getStatusCode() == 503 || result.getStatusCode() == 429) {
                    int retryAfter = ReqZCode.this.checkRetryAfter(result);
                    if (retryAfter > 0) {
                        callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                    }
                } else {
                    ReqZCode.this.goFailedCall();
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = ReqZCode.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                ReqZCode.this.goFailedCall();
            }
        });
    }

    private String makeRequestId(int length) {
        return REQUET_ID_PRIX + AmbsUtils.generateRandomString(length - REQUET_ID_PRIX.length(), true);
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.ACMS_HOST_NAME + "/commonLogin/nxsATS/AuthZCode");
    }

    private Map<String, String> makePostData() {
        lastRequestKey = makeRequestId(4);
        String str = TAG;
        Log.i(str, "ReqZCode() " + lastRequestKey);
        CloudMessagePreferenceManager.getInstance().saveZCodeLastRequestId(lastRequestKey);
        Map<String, String> postData = new HashMap<>();
        postData.put("TG_OP", "AuthZCode");
        postData.put("appID", ATTGlobalVariables.APP_ID);
        postData.put("ctnID", CloudMessagePreferenceManager.getInstance().getUserCtn());
        postData.put("requestID", lastRequestKey);
        return postData;
    }

    public static boolean isSmsZCode(String zcodeBody, String fromNumber) {
        if (!zcodeBody.startsWith(ZCODE_SMS_BEGIN) || !zcodeBody.contains(REQUET_ID_PRIX)) {
            return false;
        }
        if (!fromNumber.equals(ZCODE_SEND_FROM) && !fromNumber.equals(ZCODE_SEND_FROM_FFA)) {
            return false;
        }
        return true;
    }

    public static void handleSmsZCode(String zcodeBody, IAPICallFlowListener listener, IRetryStackAdapterHelper retryStackAdapterHelper) {
        Class<ReqZCode> cls = ReqZCode.class;
        if (TextUtils.isEmpty(lastRequestKey)) {
            lastRequestKey = CloudMessagePreferenceManager.getInstance().getZCodeLastRequestId(lastRequestKey);
            String simpleName = cls.getSimpleName();
            Log.v(simpleName, "read last requestId from preference" + lastRequestKey);
        }
        String str = TAG;
        Log.v(str, "handleSmsZCode() lastReqKey:" + lastRequestKey + " zcodeBody: " + IMSLog.checker(zcodeBody));
        if (!TextUtils.isEmpty(lastRequestKey) && zcodeBody.contains(lastRequestKey) && zcodeBody.length() >= zcodeBody.indexOf(lastRequestKey) + lastRequestKey.length() + 1) {
            CloudMessagePreferenceManager.getInstance().saveAuthZCode(zcodeBody.substring(zcodeBody.indexOf(lastRequestKey) + lastRequestKey.length() + 1));
            CloudMessagePreferenceManager.getInstance().removeUserInputNumberCount();
            IHttpAPICommonInterface top = retryStackAdapterHelper.getLastFailedRequest();
            if (top != null && top.getClass().getSimpleName().equals(cls.getSimpleName())) {
                IHttpAPICommonInterface popApi = retryStackAdapterHelper.pop();
                String topApiName = popApi == null ? null : popApi.getClass().getSimpleName();
                String str2 = TAG;
                Log.d(str2, "API " + topApiName + " Pop from Retry Stack");
            }
            listener.onFixedFlow(EnumProvision.ProvisionEventType.REQ_ATS_TOKEN.getId());
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new ReqZCode(callback, cloudMessageManagerHelper);
    }
}
