package com.sec.internal.ims.entitlement.softphone;

import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpQueryParams;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class SoftphoneHttpTransaction {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = SoftphoneHttpTransaction.class.getSimpleName();
    private final SoftphoneClient mClient;
    private long mConnectionTimeout = -1;
    private JSONObject mContent = null;
    private JSONArray mContents = new JSONArray();
    private byte[] mData = null;
    private Map<String, String> mHeader = new HashMap();
    private HttpRequestParams.Method mMethod = null;
    private LinkedHashMap<String, String> mQueryParams = new LinkedHashMap<>();
    private boolean mQueryParamsEncoded = false;
    private long mReadTimeout = -1;
    private String mStringBody = null;
    private String mURL = null;
    private long mWriteTimeout = -1;

    public SoftphoneHttpTransaction(SoftphoneClient client) {
        this.mClient = client;
    }

    public void setRequestURL(String url) {
        this.mURL = url;
    }

    public void setRequestMethod(HttpRequestParams.Method method) {
        this.mMethod = method;
    }

    public void setQueryParameters(LinkedHashMap<String, String> params, boolean encoded) {
        this.mQueryParams = params;
        this.mQueryParamsEncoded = encoded;
    }

    public void addRequestHeader(String header, String value) {
        this.mHeader.put(header, value);
    }

    public void setStringBody(String body) {
        this.mStringBody = body;
    }

    public void setJsonBody(JSONObject body) {
        this.mContent = body;
    }

    public void setByteData(byte[] data) {
        this.mData = data;
    }

    public void commit(Message onComplete) {
        executeRequest(onComplete);
    }

    public void setTimeout(long timeout) {
        setConnectionTimeout(timeout);
        setReadTimeout(timeout);
        setWriteTimeout(timeout);
    }

    public void initHttpRequest(String path) {
        this.mURL = "https://" + this.mClient.mHost + path;
        this.mHeader.clear();
        this.mHeader.put(HttpController.HEADER_HOST, this.mClient.mHost);
        this.mHeader.put("Accept", "application/json");
        if (this.mClient.getAccessToken() != null) {
            Map<String, String> map = this.mHeader;
            map.put("Authorization", this.mClient.getAccessTokenType() + " " + this.mClient.getAccessToken());
        }
    }

    private HttpRequestParams buildRequestParams(HttpRequestParams.HttpRequestCallback callback) {
        HttpRequestParams requestParams = new HttpRequestParams(this.mMethod, this.mURL, this.mHeader, callback);
        String str = this.mStringBody;
        if (str != null) {
            requestParams.setPostBody(str);
        } else {
            JSONObject jSONObject = this.mContent;
            if (jSONObject != null) {
                requestParams.setPostBody(jSONObject);
            } else if (this.mContents.length() > 0) {
                requestParams.setPostBody(this.mContents);
            } else {
                byte[] bArr = this.mData;
                if (bArr != null) {
                    requestParams.setPostBody(bArr);
                }
            }
        }
        if (!this.mQueryParams.isEmpty()) {
            requestParams.setQueryParams(new HttpQueryParams(this.mQueryParams, this.mQueryParamsEncoded));
        }
        long j = this.mConnectionTimeout;
        if (j != -1) {
            requestParams.setConnectionTimeout(j);
        }
        long j2 = this.mReadTimeout;
        if (j2 != -1) {
            requestParams.setReadTimeout(j2);
        }
        long j3 = this.mWriteTimeout;
        if (j3 != -1) {
            requestParams.setWriteTimeout(j3);
        }
        requestParams.setFollowRedirects(false);
        return requestParams;
    }

    private void executeRequest(final Message OnComplete) {
        HttpController.getInstance().execute(buildRequestParams(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                OnComplete.obj = result;
                OnComplete.sendToTarget();
            }

            public void onFail(IOException ie) {
                Log.e(SoftphoneHttpTransaction.LOG_TAG, "Http request failed");
                OnComplete.obj = new HttpResponseParams();
                OnComplete.sendToTarget();
            }
        }));
    }

    private void setConnectionTimeout(long timeout) {
        this.mConnectionTimeout = timeout;
    }

    private void setReadTimeout(long timeout) {
        this.mReadTimeout = timeout;
    }

    private void setWriteTimeout(long timeout) {
        this.mWriteTimeout = timeout;
    }
}
