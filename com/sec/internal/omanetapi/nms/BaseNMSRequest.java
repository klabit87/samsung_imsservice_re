package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.strategy.TMOCmStrategy;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.data.PathList;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseNMSRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    private static final String TAG = BaseNMSRequest.class.getSimpleName();
    private static final long serialVersionUID = 8115500554434359994L;
    private final String JSON_MESSAGE_ID_TAG = "messageId";
    private final String JSON_POLICY_EXCEPTION_TAG = "policyException";
    private final String JSON_REQUEST_ERROR_TAG = "requestError";
    private final String JSON_SERVICE_EXCEPTION_TAG = "serviceException";
    /* access modifiers changed from: protected */
    public String mBaseUrl;
    private String mBoxId;
    protected transient Map<String, String> mNMSRequestHeaderMap = new HashMap();

    /* access modifiers changed from: protected */
    public abstract void buildAPISpecificURLFromBase();

    public BaseNMSRequest(String serverRoot, String apiVersion, String storeName, String boxId) {
        Log.i(TAG, "constructor1");
        buildBaseURL(serverRoot, apiVersion, storeName, boxId);
    }

    public BaseNMSRequest(String serverRoot, String apiVersion, String storeName, String boxId, boolean isNcHost) {
        Log.i(TAG, "constructor3");
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            buildBaseURL(serverRoot, apiVersion, storeName, boxId, isNcHost);
        } else {
            buildBaseURL(serverRoot, apiVersion, storeName, boxId);
        }
    }

    public BaseNMSRequest(String hostName, String port, String basePath, String apiVersion, String storeName, String boxId) {
        Log.i(TAG, "constructor2");
        String serverRoot = hostName;
        if (port != null) {
            serverRoot = hostName + ":" + port;
        }
        if (basePath != null && !basePath.trim().isEmpty()) {
            serverRoot = serverRoot + "/" + basePath.trim();
        }
        buildBaseURL(serverRoot, apiVersion, storeName, boxId);
    }

    public BaseNMSRequest(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    public BaseNMSRequest(String faxUrl, String boxId) {
        try {
            String encodedTelUri = URLEncoder.encode(boxId, "utf-8");
            String str = TAG;
            Log.d(str, "encoded telUri: " + IMSLog.checker(encodedTelUri));
            this.mBaseUrl = faxUrl + encodedTelUri;
            String str2 = TAG;
            Log.d(str2, "mBaseUrl: " + IMSLog.checker(this.mBaseUrl));
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "uri encode failed");
        }
    }

    private void buildBaseURL(String serverRoot, String apiVersion, String storeName, String boxId) {
        this.mBoxId = boxId;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(CloudMessageStrategyManager.getStrategy().getProtocol()).encodedAuthority(serverRoot).appendPath("nms").appendPath(apiVersion).appendPath(storeName).appendPath(this.mBoxId);
        String uri = builder.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }

    private void buildBaseURL(String serverRoot, String apiVersion, String storeName, String boxId, boolean isNcHost) {
        String str = TAG;
        Log.d(str, "isNcHost=" + isNcHost + ", This constructor is just for subscription");
        this.mBoxId = boxId;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(CloudMessageStrategyManager.getStrategy().getProtocol()).encodedAuthority(serverRoot).appendPath("pubsub").appendPath("oma_b").appendPath("nms").appendPath(apiVersion).appendPath(this.mBoxId);
        String uri = builder.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, uri);
    }

    public void initCommonRequestHeaders(String acceptContentType, String authorization) {
        if (acceptContentType == null || acceptContentType.isEmpty() || !(acceptContentType.compareTo("application/json") == 0 || acceptContentType.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            acceptContentType = "application/json";
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put("Accept", acceptContentType);
        this.mNMSRequestHeaderMap.put("Authorization", authorization);
        if (CloudMessageStrategyManager.getStrategy().isEnableATTHeader()) {
            this.mNMSRequestHeaderMap.put("Connection", "Keep-Alive");
            this.mNMSRequestHeaderMap.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
            this.mNMSRequestHeaderMap.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
            this.mNMSRequestHeaderMap.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
            this.mNMSRequestHeaderMap.put("x-att-deviceId", CloudMessagePreferenceManager.getInstance().getDeviceId());
        } else if (CloudMessageStrategyManager.getStrategy().isEnableTMOHeader()) {
            CloudMessageStrategyManager.getStrategy().updateHTTPHeader();
            this.mNMSRequestHeaderMap.put("User-Agent", TMOCmStrategy.TmoHttpHeaderValues.USER_AGENT_ID_VALUE);
            this.mNMSRequestHeaderMap.put("device_id", TMOCmStrategy.TmoHttpHeaderValues.DEVICE_ID_VALUE);
        }
    }

    public void initSubscribeRequestHeaders(String acceptContentType, String authorization) {
        if (acceptContentType == null || acceptContentType.isEmpty() || !(acceptContentType.compareTo("application/json") == 0 || acceptContentType.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            acceptContentType = "application/json";
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put("Accept", acceptContentType);
        this.mNMSRequestHeaderMap.put("Authorization", authorization);
        if (CloudMessageStrategyManager.getStrategy().isEnableATTHeader()) {
            this.mNMSRequestHeaderMap.put("Connection", "close");
            this.mNMSRequestHeaderMap.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
            this.mNMSRequestHeaderMap.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
            this.mNMSRequestHeaderMap.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
            this.mNMSRequestHeaderMap.put("x-att-deviceId", CloudMessagePreferenceManager.getInstance().getDeviceId());
        } else if (CloudMessageStrategyManager.getStrategy().isEnableTMOHeader()) {
            CloudMessageStrategyManager.getStrategy().updateHTTPHeader();
            this.mNMSRequestHeaderMap.put("User-Agent", TMOCmStrategy.TmoHttpHeaderValues.USER_AGENT_ID_VALUE);
            this.mNMSRequestHeaderMap.put("device_id", TMOCmStrategy.TmoHttpHeaderValues.DEVICE_ID_VALUE);
        }
    }

    /* access modifiers changed from: protected */
    public void initCommonGetRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNMSRequestHeaderMap);
        setMethod(HttpRequestParams.Method.GET);
        setFollowRedirects(false);
    }

    public void initCommonDeleteRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNMSRequestHeaderMap);
        setMethod(HttpRequestParams.Method.DELETE);
        setFollowRedirects(false);
    }

    public void addRequestHeader(String header, String value) {
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put(header, value);
    }

    public void setMultipleContentType(String header, String[] contentType) {
        StringBuilder allTypes = new StringBuilder();
        for (int i = 0; i < contentType.length; i++) {
            allTypes.append(contentType[i]);
            if (i != contentType.length - 1) {
                allTypes.append(",");
            }
        }
        if (this.mNMSRequestHeaderMap == null) {
            this.mNMSRequestHeaderMap = new HashMap();
        }
        this.mNMSRequestHeaderMap.put(header, allTypes.toString());
        setHeaders(this.mNMSRequestHeaderMap);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback) {
        String boxId = getBoxId();
        if (TextUtils.isEmpty(boxId)) {
            boxId = Util.getLineTelUriFromObjUrl(this.mBaseUrl);
            String str = TAG;
            Log.i(str, "box id is " + boxId);
        } else {
            String str2 = TAG;
            Log.i(str2, "box id is " + boxId);
        }
        initCommonRequestHeaders(CloudMessageStrategyManager.getStrategy().getContentType(), CloudMessageStrategyManager.getStrategy().getValidTokenByLine(boxId));
        return this;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelper) {
        return this;
    }

    public OMAApiResponseParam getResponse(HttpResponseParams result) {
        try {
            return (OMAApiResponseParam) new Gson().fromJson(result.getDataString(), OMAApiResponseParam.class);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, e.toString() + " ");
            e.printStackTrace();
            return null;
        }
    }

    public String getBoxId() {
        if (TextUtils.isEmpty(this.mBoxId)) {
            this.mBoxId = Util.getLineTelUriFromObjUrl(this.mBaseUrl);
        }
        return this.mBoxId;
    }

    public boolean updateToken() {
        if (TextUtils.isEmpty(this.mBoxId)) {
            this.mBoxId = Util.getLineTelUriFromObjUrl(this.mBaseUrl);
            String str = TAG;
            Log.i(str, "box id is " + this.mBoxId);
        } else {
            String str2 = TAG;
            Log.i(str2, "box id is " + this.mBoxId);
        }
        String pat = CloudMessageStrategyManager.getStrategy().getValidTokenByLine(this.mBoxId);
        if (TextUtils.isEmpty(pat)) {
            return false;
        }
        initCommonRequestHeaders(CloudMessageStrategyManager.getStrategy().getContentType(), pat);
        return true;
    }

    public boolean updateToken(String line) {
        if (this.mBoxId == null) {
            this.mBoxId = line;
        }
        String str = TAG;
        Log.d(str, "set box id : " + this.mBoxId);
        String pat = CloudMessageStrategyManager.getStrategy().getValidTokenByLine(this.mBoxId);
        if (TextUtils.isEmpty(pat)) {
            return false;
        }
        initCommonRequestHeaders(CloudMessageStrategyManager.getStrategy().getContentType(), pat);
        return true;
    }

    public void replaceUrlPrefix() {
        String newHeader = CloudMessageStrategyManager.getStrategy().getProtocol() + ":";
        if (!TextUtils.isEmpty(this.mBaseUrl) && !TextUtils.isEmpty(newHeader)) {
            Log.i(TAG, "replaceUrlPrefix with: " + newHeader);
            String replaceUrlPrefix = Util.replaceUrlPrefix(this.mBaseUrl, newHeader);
            this.mBaseUrl = replaceUrlPrefix;
            setUrl(replaceUrlPrefix);
        }
    }

    public boolean shouldCareAfterResponsePreProcess(IAPICallFlowListener callFlowListener, HttpResponseParams result, Object paramOMAResponseForBufDBObj, BufferDBChangeParam dbParam, int overwriteEvent) {
        return CloudMessageStrategyManager.getStrategy().shouldCareAfterPreProcess(callFlowListener, this, result, paramOMAResponseForBufDBObj, dbParam, overwriteEvent);
    }

    public void updateServerRoot(String serverRoot) {
        String replaceHostOfURL = Util.replaceHostOfURL(serverRoot, this.mBaseUrl);
        this.mBaseUrl = replaceHostOfURL;
        setUrl(replaceHostOfURL);
    }

    public String getResponseMessageId(String json) {
        String messageId = null;
        try {
            JSONObject jsonRoot = new JSONObject(json);
            if (jsonRoot.isNull("requestError")) {
                return null;
            }
            JSONObject jsonRequestError = jsonRoot.getJSONObject("requestError");
            if (!jsonRequestError.isNull("serviceException")) {
                JSONObject jsonServiceException = jsonRequestError.getJSONObject("serviceException");
                if (jsonServiceException != null) {
                    messageId = jsonServiceException.getString("messageId");
                }
            } else if (!jsonRequestError.isNull("policyException")) {
                JSONObject jsonPolicyException = jsonRequestError.getJSONObject("policyException");
                if (jsonPolicyException != null) {
                    return jsonPolicyException.getString("messageId");
                }
                return null;
            }
            return messageId;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initPostRequest(PathList pathList, boolean isJson) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        HttpPostBody body = null;
        if (isJson) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            body = new HttpPostBody(new Gson().toJson(pathList));
        }
        if (body != null) {
            setPostBody(body);
        }
    }
}
