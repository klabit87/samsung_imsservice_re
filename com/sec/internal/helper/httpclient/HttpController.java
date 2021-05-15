package com.sec.internal.helper.httpclient;

import android.webkit.URLUtil;
import com.sec.internal.log.IMSLog;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.CipherSuite;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Random;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpController {
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CONTENT_TYPE_CAB_XML = "application/vnd.oma.cab-address-book+xml";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XCAP_EL_XML = "application/xcap-el+xml";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String ENCODING_GZIP = "gzip";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_AUTHENTICATION_INFO = "Authentication-Info";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_ID = "Content-ID";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_DEVICE_AGENT = "Device-Agent";
    public static final String HEADER_ETAG = "ETag";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String HEADER_FILE_ICON = "File-Icon";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_LOCATION = "Location";
    public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String HEADER_RANGE = "Range";
    public static final String HEADER_REFERER = "Referer";
    public static final String HEADER_SERVER = "Server";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String HEADER_X_TMUS_IMEI = "X-TMUS-IMEI";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_TRACE = "TRACE";
    public static final String PARAM_CHARSET = "charset";
    /* access modifiers changed from: private */
    public static final String TAG = HttpController.class.getSimpleName();
    public static final String VAL_3GPP_GBA = "3gpp-gba";
    private static volatile Random random = new Random();
    private static volatile HttpController sInstance = new HttpController();
    private final int API_SIGNATURE_MAX_INT = 100000;
    private boolean mIsDebugHttps = false;

    public void setDebugHttps(boolean isDebugHttps) {
        this.mIsDebugHttps = isDebugHttps;
    }

    private HttpController() {
    }

    public static HttpController getInstance() {
        return sInstance;
    }

    public void execute(final HttpRequestParams requestParams) {
        if (isValidRequestParam(requestParams, true)) {
            Call call = getCall(requestParams);
            if (call == null) {
                requestParams.getCallback().onFail(new IOException("okhttp fail to create call"));
                return;
            }
            final String signature = generateRandomString(100000);
            String str = TAG;
            IMSLog.i(str, "HTTP Request " + signature + " " + requestParams.getClass().getSimpleName());
            String str2 = TAG;
            IMSLog.i(str2, "HTTP Request " + signature + " " + requestParams);
            try {
                call.enqueue(new Callback() {
                    public void onResponse(Response response) {
                        HttpResponseParams result = HttpResponseBuilder.buildResponse(response);
                        if (result == null) {
                            requestParams.getCallback().onFail(new IOException("okhttp response build failure"));
                            return;
                        }
                        if (requestParams.getUseTls() && response.handshake() != null) {
                            result.setCipherSuite(response.handshake().cipherSuite());
                        }
                        String access$000 = HttpController.TAG;
                        IMSLog.i(access$000, "HTTP response: " + signature + " " + requestParams.getClass().getSimpleName() + " " + result);
                        requestParams.getCallback().onComplete(result);
                    }

                    public void onFailure(Request arg0, IOException arg1) {
                        String access$000 = HttpController.TAG;
                        IMSLog.i(access$000, "HTTP Request " + signature + " " + requestParams.getClass().getSimpleName() + "  failed: " + IMSLog.numberChecker(arg0.urlString(), 5) + " with " + arg0.method() + " Reason: " + arg1.getMessage());
                        requestParams.getCallback().onFail(arg1);
                    }
                });
            } catch (IllegalStateException e) {
                e.printStackTrace();
                requestParams.getCallback().onFail(new IOException("okhttp malformed response"));
            }
        }
    }

    public HttpResponseParams syncExecute(HttpRequestParams requestParams) {
        Call call;
        if (!isValidRequestParam(requestParams, false) || (call = getCall(requestParams)) == null) {
            return null;
        }
        String signature = generateRandomString(100000);
        String str = TAG;
        IMSLog.i(str, "HTTP Request " + signature + " " + requestParams);
        Response response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null) {
            HttpResponseParams result = HttpResponseBuilder.buildResponse(response);
            String str2 = TAG;
            IMSLog.i(str2, "HTTP response: " + signature + " " + result);
            return result;
        }
        String str3 = TAG;
        IMSLog.i(str3, "HTTP response: " + signature + " null");
        return null;
    }

    private Call getCall(HttpRequestParams requestParams) {
        Request request = HttpRequestBuilder.buildRequest(requestParams);
        if (request != null) {
            return getOkHttpClient(requestParams).newCall(request);
        }
        IMSLog.e(TAG, "getCall(): okhttp request build failure");
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00bd  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.squareup.okhttp.OkHttpClient getOkHttpClient(com.sec.internal.helper.httpclient.HttpRequestParams r9) {
        /*
            r8 = this;
            com.squareup.okhttp.OkHttpClient r0 = new com.squareup.okhttp.OkHttpClient
            r0.<init>()
            long r1 = r9.getConnectionTimeout()
            r3 = 0
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            r5 = 2147483647(0x7fffffff, double:1.060997895E-314)
            if (r1 < 0) goto L_0x0023
            long r1 = r9.getConnectionTimeout()
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 > 0) goto L_0x0023
            long r1 = r9.getConnectionTimeout()
            java.util.concurrent.TimeUnit r7 = java.util.concurrent.TimeUnit.MILLISECONDS
            r0.setConnectTimeout(r1, r7)
        L_0x0023:
            long r1 = r9.getReadTimeout()
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 < 0) goto L_0x003c
            long r1 = r9.getReadTimeout()
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 > 0) goto L_0x003c
            long r1 = r9.getReadTimeout()
            java.util.concurrent.TimeUnit r7 = java.util.concurrent.TimeUnit.MILLISECONDS
            r0.setReadTimeout(r1, r7)
        L_0x003c:
            long r1 = r9.getWriteTimeout()
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 < 0) goto L_0x0055
            long r1 = r9.getWriteTimeout()
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 > 0) goto L_0x0055
            long r1 = r9.getWriteTimeout()
            java.util.concurrent.TimeUnit r3 = java.util.concurrent.TimeUnit.MILLISECONDS
            r0.setWriteTimeout(r1, r3)
        L_0x0055:
            com.squareup.okhttp.Dns r1 = r9.getDns()
            if (r1 == 0) goto L_0x0062
            com.squareup.okhttp.Dns r1 = r9.getDns()
            r0.setDns(r1)
        L_0x0062:
            javax.net.SocketFactory r1 = r9.getSocketFactory()
            if (r1 == 0) goto L_0x006f
            javax.net.SocketFactory r1 = r9.getSocketFactory()
            r0.setSocketFactory(r1)
        L_0x006f:
            boolean r1 = r9.getFollowRedirects()
            r0.setFollowRedirects(r1)
            boolean r1 = r9.getRetryOnConnectionFailure()
            r0.setRetryOnConnectionFailure(r1)
            java.lang.String r1 = r9.getUrl()     // Catch:{ Exception -> 0x00c5 }
            java.lang.String r2 = "https://wsg"
            boolean r1 = r1.startsWith(r2)     // Catch:{ Exception -> 0x00c5 }
            if (r1 != 0) goto L_0x00af
            boolean r1 = r8.mIsDebugHttps     // Catch:{ Exception -> 0x00c5 }
            if (r1 == 0) goto L_0x008e
            goto L_0x00af
        L_0x008e:
            boolean r1 = r9.getUseTls()     // Catch:{ Exception -> 0x00c5 }
            if (r1 == 0) goto L_0x00b6
            javax.net.ssl.SSLSocketFactory r1 = createSslSocketFactory()     // Catch:{ Exception -> 0x00c5 }
            r0.setSslSocketFactory(r1)     // Catch:{ Exception -> 0x00c5 }
            com.squareup.okhttp.ConnectionSpec r1 = configConnectionSpec()     // Catch:{ Exception -> 0x00c5 }
            java.util.List r1 = java.util.Collections.singletonList(r1)     // Catch:{ Exception -> 0x00c5 }
            r0.setConnectionSpecs(r1)     // Catch:{ Exception -> 0x00c5 }
            com.sec.internal.helper.httpclient.HttpController$2 r1 = new com.sec.internal.helper.httpclient.HttpController$2     // Catch:{ Exception -> 0x00c5 }
            r1.<init>()     // Catch:{ Exception -> 0x00c5 }
            r0.setHostnameVerifier(r1)     // Catch:{ Exception -> 0x00c5 }
            goto L_0x00b6
        L_0x00af:
            javax.net.ssl.SSLSocketFactory r1 = createSslSocketFactory()     // Catch:{ Exception -> 0x00c5 }
            r0.setSslSocketFactory(r1)     // Catch:{ Exception -> 0x00c5 }
        L_0x00b6:
            boolean r1 = r9.getUseProxy()
            if (r1 == 0) goto L_0x00c4
            java.net.Proxy r1 = r9.getProxy()
            r0.setProxy(r1)
        L_0x00c4:
            return r0
        L_0x00c5:
            r1 = move-exception
            java.lang.String r2 = TAG
            java.lang.String r3 = "Could not load keystore "
            com.sec.internal.log.IMSLog.d(r2, r3)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.httpclient.HttpController.getOkHttpClient(com.sec.internal.helper.httpclient.HttpRequestParams):com.squareup.okhttp.OkHttpClient");
    }

    private static ConnectionSpec configConnectionSpec() {
        return new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).cipherSuites(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA, CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA, CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA).build();
    }

    private static SSLSocketFactory createSslSocketFactory() throws Exception {
        TrustManager[] byPassTrustManagers = {new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init((KeyManager[]) null, byPassTrustManagers, new SecureRandom());
        return sslContext.getSocketFactory();
    }

    private boolean isValidRequestParam(HttpRequestParams reqParams, boolean async) {
        if (reqParams == null || reqParams.getMethod() == null) {
            IMSLog.e(TAG, "isValidRequestParam(): invalid param, vail");
            return false;
        } else if (async && reqParams.getCallback() == null) {
            IMSLog.e(TAG, "isValidRequestParam(): callback is null for async call");
            return false;
        } else if (URLUtil.isValidUrl(reqParams.getUrl())) {
            return true;
        } else {
            String str = TAG;
            IMSLog.e(str, "isValidRequestParam(): invalid uri: " + IMSLog.numberChecker(reqParams.getUrl()));
            return false;
        }
    }

    private String generateRandomString(int length) {
        return String.valueOf(random.nextInt(length));
    }
}
