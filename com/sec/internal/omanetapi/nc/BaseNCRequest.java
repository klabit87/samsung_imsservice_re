package com.sec.internal.omanetapi.nc;

import android.net.Uri;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseNCRequest extends HttpRequestParams implements IHttpAPICommonInterface {
    private static final String TAG = BaseNCRequest.class.getSimpleName();
    private static final long serialVersionUID = 7698970710818917306L;
    protected String mBaseUrl;
    protected transient Map<String, String> mNCRequestHeaderMap = new HashMap();

    /* access modifiers changed from: protected */
    public abstract void buildAPISpecificURLFromBase();

    public BaseNCRequest(String serverRoot, String apiVersion, String userId) {
        Log.i(TAG, "constructor");
        buildBaseURL(serverRoot, apiVersion, userId);
    }

    public BaseNCRequest(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    private void buildBaseURL(String serverRoot, String apiVersion, String userId) {
        Uri.Builder builder = new Uri.Builder();
        String protocol = CloudMessageStrategyManager.getStrategy().getProtocol();
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            builder.scheme(protocol).authority(serverRoot).appendPath("pubsub").appendPath("oma_b").appendPath("notificationchannel").appendPath(apiVersion).appendPath(userId);
        } else {
            builder.scheme(protocol).authority(serverRoot).appendPath("notificationchannel").appendPath(apiVersion).appendPath(userId);
        }
        String uri = builder.build().toString();
        this.mBaseUrl = uri;
        Log.i(TAG, IMSLog.checker(uri));
    }

    public void initCommonRequestHeaders(String acceptContentType, String authorization) {
        if (acceptContentType == null || acceptContentType.isEmpty() || !(acceptContentType.compareTo("application/json") == 0 || acceptContentType.compareTo(HttpController.CONTENT_TYPE_XML) == 0)) {
            acceptContentType = "application/json";
        }
        String accContentType = acceptContentType;
        if (this.mNCRequestHeaderMap == null) {
            this.mNCRequestHeaderMap = new HashMap();
        }
        this.mNCRequestHeaderMap.put("Accept", accContentType);
        this.mNCRequestHeaderMap.put("Authorization", authorization);
        if (CloudMessageStrategyManager.getStrategy().isEnableATTHeader()) {
            this.mNCRequestHeaderMap.put("Connection", "close");
            this.mNCRequestHeaderMap.put("x-att-clientVersion", ATTGlobalVariables.VERSION_NAME);
            this.mNCRequestHeaderMap.put("x-att-clientId", ATTGlobalVariables.getHttpClientID());
            this.mNCRequestHeaderMap.put("x-att-contextInfo", ATTGlobalVariables.BUILD_INFO);
            this.mNCRequestHeaderMap.put("x-att-deviceId", CloudMessagePreferenceManager.getInstance().getDeviceId());
        }
    }

    public void initCommonGetRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNCRequestHeaderMap);
        setMethod(HttpRequestParams.Method.GET);
        setFollowRedirects(false);
    }

    public void initCommonDeleteRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNCRequestHeaderMap);
        setMethod(HttpRequestParams.Method.DELETE);
        setFollowRedirects(false);
    }

    public void initCommonPutRequest() {
        setUrl(this.mBaseUrl);
        setHeaders(this.mNCRequestHeaderMap);
        setMethod(HttpRequestParams.Method.PUT);
        setFollowRedirects(false);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback) {
        return this;
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelper) {
        return this;
    }

    public boolean shouldCareAfterResponsePreProcess(IAPICallFlowListener callFlowListener, HttpResponseParams result, Object paramOMAResponseForBufDBObj, BufferDBChangeParam dbParam, int overwriteEvent) {
        return CloudMessageStrategyManager.getStrategy().shouldCareAfterPreProcess(callFlowListener, this, result, paramOMAResponseForBufDBObj, dbParam, overwriteEvent);
    }

    public void updateServerRoot(String serverRoot) {
        String replaceHostOfURL = Util.replaceHostOfURL(serverRoot, this.mBaseUrl);
        this.mBaseUrl = replaceHostOfURL;
        setUrl(replaceHostOfURL);
    }
}
