package com.sec.internal.helper.httpclient;

import com.sec.internal.log.IMSLog;
import com.squareup.okhttp.Dns;
import java.io.IOException;
import java.net.Proxy;
import java.util.Map;
import javax.net.SocketFactory;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRequestParams {
    private String mBsfUrl = null;
    private HttpRequestCallback mCallback = null;
    private String mCipherSuite = null;
    private long mConnectionTimeout = 30000;
    private Dns mDns = null;
    private boolean mFollowRedirects = true;
    private Map<String, String> mHeaders = null;
    private int mIpVersion = 0;
    private Method mMethod = null;
    private String mNafUrl = null;
    private String mPassword = null;
    private int mPhoneId = 0;
    private HttpPostBody mPostBody = null;
    private Proxy mProxy = null;
    private HttpQueryParams mQueryParams = null;
    private long mReadTimeout = 30000;
    private boolean mRetryOnConnectionFailure = true;
    private SocketFactory mSocketFactory = null;
    private boolean mUseImei = false;
    private boolean mUseProxy = false;
    private boolean mUseTls = false;
    private String mUserName = null;
    private long mWriteTimeout = 30000;

    public interface HttpRequestCallback {
        void onComplete(HttpResponseParams httpResponseParams);

        void onFail(IOException iOException);
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        TRACE,
        HEAD,
        OPTIONS
    }

    public HttpRequestParams() {
    }

    public HttpRequestParams(Method method, String url, Map<String, String> headers, HttpRequestCallback callback) {
        this.mMethod = method;
        this.mNafUrl = url;
        this.mHeaders = headers;
        this.mCallback = callback;
    }

    public HttpRequestParams(Method method, String nafurl, String bsfUrl, Map<String, String> headers, HttpRequestCallback callback) {
        this.mMethod = method;
        this.mNafUrl = nafurl;
        this.mBsfUrl = bsfUrl;
        this.mHeaders = headers;
        this.mCallback = callback;
    }

    public HttpRequestParams(Map<String, String> headers) {
        this.mHeaders = headers;
    }

    public Method getMethod() {
        return this.mMethod;
    }

    /* renamed from: com.sec.internal.helper.httpclient.HttpRequestParams$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method;

        static {
            int[] iArr = new int[Method.values().length];
            $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method = iArr;
            try {
                iArr[Method.GET.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[Method.POST.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[Method.PUT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[Method.DELETE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[Method.HEAD.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[Method.OPTIONS.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[Method.TRACE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public String getMethodString() {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[this.mMethod.ordinal()]) {
            case 1:
                return "GET";
            case 2:
                return "POST";
            case 3:
                return "PUT";
            case 4:
                return HttpController.METHOD_DELETE;
            case 5:
                return HttpController.METHOD_HEAD;
            case 6:
                return HttpController.METHOD_OPTIONS;
            case 7:
                return HttpController.METHOD_TRACE;
            default:
                return "";
        }
    }

    public HttpRequestParams setMethod(Method method) {
        this.mMethod = method;
        return this;
    }

    public String getUrl() {
        return this.mNafUrl;
    }

    public String getBsfUrl() {
        return this.mBsfUrl;
    }

    public HttpRequestParams setUrl(String url) {
        this.mNafUrl = url;
        return this;
    }

    public HttpRequestParams setBsfUrl(String url) {
        this.mBsfUrl = url;
        return this;
    }

    public HttpQueryParams getQueryParams() {
        return this.mQueryParams;
    }

    public HttpRequestParams setQueryParams(HttpQueryParams params) {
        this.mQueryParams = params;
        return this;
    }

    public Map<String, String> getHeaders() {
        return this.mHeaders;
    }

    public HttpRequestParams setHeaders(Map<String, String> mHeaders2) {
        this.mHeaders = mHeaders2;
        return this;
    }

    public HttpRequestCallback getCallback() {
        return this.mCallback;
    }

    public HttpRequestParams setCallback(HttpRequestCallback mCallback2) {
        this.mCallback = mCallback2;
        return this;
    }

    public HttpPostBody getPostBody() {
        return this.mPostBody;
    }

    public HttpRequestParams setPostBody(HttpPostBody postBody) {
        this.mPostBody = postBody;
        return this;
    }

    public HttpRequestParams setPostBody(String body) {
        this.mPostBody = new HttpPostBody(body);
        return this;
    }

    public HttpRequestParams setPostBody(JSONObject body) {
        this.mPostBody = new HttpPostBody(body);
        return this;
    }

    public HttpRequestParams setPostBody(JSONArray body) {
        this.mPostBody = new HttpPostBody(body.toString());
        return this;
    }

    public HttpRequestParams setPostBody(byte[] data) {
        this.mPostBody = new HttpPostBody(data);
        return this;
    }

    public SocketFactory getSocketFactory() {
        return this.mSocketFactory;
    }

    public HttpRequestParams setSocketFactory(SocketFactory mSockFactory) {
        this.mSocketFactory = mSockFactory;
        return this;
    }

    public long getConnectionTimeout() {
        return this.mConnectionTimeout;
    }

    public HttpRequestParams setConnectionTimeout(long connectionTimeout) {
        this.mConnectionTimeout = connectionTimeout;
        return this;
    }

    public long getReadTimeout() {
        return this.mReadTimeout;
    }

    public HttpRequestParams setReadTimeout(long readTimeout) {
        this.mReadTimeout = readTimeout;
        return this;
    }

    public long getWriteTimeout() {
        return this.mWriteTimeout;
    }

    public HttpRequestParams setWriteTimeout(long writeTimeout) {
        this.mWriteTimeout = writeTimeout;
        return this;
    }

    public HttpRequestParams setPostParams(Map<String, String> parameters) {
        this.mPostBody = new HttpPostBody(parameters);
        return this;
    }

    public boolean getFollowRedirects() {
        return this.mFollowRedirects;
    }

    public HttpRequestParams setFollowRedirects(boolean followRedirects) {
        this.mFollowRedirects = followRedirects;
        return this;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public HttpRequestParams setPhoneId(int phoneId) {
        this.mPhoneId = phoneId;
        return this;
    }

    public Dns getDns() {
        return this.mDns;
    }

    public HttpRequestParams setDns(Dns dns) {
        this.mDns = dns;
        return this;
    }

    public String getUserName() {
        return this.mUserName;
    }

    public HttpRequestParams setUserName(String username) {
        this.mUserName = username;
        return this;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public HttpRequestParams setPassword(String password) {
        this.mPassword = password;
        return this;
    }

    public boolean getUseTls() {
        return this.mUseTls;
    }

    public HttpRequestParams setUseTls(boolean useTls) {
        this.mUseTls = useTls;
        return this;
    }

    public boolean getRetryOnConnectionFailure() {
        return this.mRetryOnConnectionFailure;
    }

    public int getIpVersion() {
        return this.mIpVersion;
    }

    public HttpRequestParams setIpVersion(int mIpVersion2) {
        this.mIpVersion = mIpVersion2;
        return this;
    }

    public Proxy getProxy() {
        return this.mProxy;
    }

    public HttpRequestParams setProxy(Proxy proxy) {
        this.mProxy = proxy;
        return this;
    }

    public boolean getUseProxy() {
        return this.mUseProxy;
    }

    public HttpRequestParams setUseProxy(boolean useProxy) {
        this.mUseProxy = useProxy;
        return this;
    }

    public void setUseImei(boolean useImei) {
        this.mUseImei = useImei;
    }

    public boolean getUseImei() {
        return this.mUseImei;
    }

    public void setCipherSuite(String cipherSuite) {
        this.mCipherSuite = cipherSuite;
    }

    public String getCipherSuite() {
        return this.mCipherSuite;
    }

    public String toString() {
        StringBuffer headers = new StringBuffer();
        Map<String, String> map = this.mHeaders;
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : this.mHeaders.entrySet()) {
                headers.append("\r\n        " + entry.getKey() + " : ");
                if ("X-3GPP-Intended-Identity".equalsIgnoreCase(entry.getKey())) {
                    headers.append(IMSLog.numberChecker(entry.getValue()));
                } else {
                    headers.append(entry.getValue());
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("HttpRequestParams[\r\n    mMethod: ");
        sb.append(this.mMethod.name());
        sb.append("\r\n    mNafUrl: ");
        sb.append(IMSLog.numberChecker(this.mNafUrl));
        sb.append("\r\n    mBsfUrl: ");
        sb.append(IMSLog.numberChecker(this.mBsfUrl));
        sb.append("\r\n    mQueryParams: ");
        HttpQueryParams httpQueryParams = this.mQueryParams;
        String str = "";
        sb.append(httpQueryParams != null ? httpQueryParams.toString() : str);
        sb.append("\r\n    mHeaders: ");
        sb.append(headers);
        sb.append("\r\n    mConnectionTimeout: ");
        sb.append(this.mConnectionTimeout);
        sb.append("\r\n    mReadTimeout: ");
        sb.append(this.mReadTimeout);
        sb.append("\r\n    mWriteTimeout: ");
        sb.append(this.mWriteTimeout);
        sb.append("\r\n    mFollowRedirects: ");
        sb.append(this.mFollowRedirects);
        sb.append("\r\n]\r\n    mPostBody: ");
        HttpPostBody httpPostBody = this.mPostBody;
        if (httpPostBody != null) {
            str = IMSLog.numberChecker(httpPostBody.toString());
        }
        sb.append(str);
        sb.append("\r\n]");
        return sb.toString();
    }
}
