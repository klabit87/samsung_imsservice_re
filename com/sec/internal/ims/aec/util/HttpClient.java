package com.sec.internal.ims.aec.util;

import android.net.Network;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.log.AECLog;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import org.json.JSONObject;

public class HttpClient {
    private static final HostnameVerifier DO_NOT_VERIFY = $$Lambda$HttpClient$o2W_9uDYzJgp7ktnGMKhRurmusU.INSTANCE;
    private static final String GET = "GET";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = HttpClient.class.getSimpleName();
    private static final int MAX_CHUNK_SIZE = 512000;
    private static final int MAX_CONN_TIMEOUT = 30000;
    private static final int MAX_READ_TIMEOUT = 30000;
    private static final int MIN_CHUNK_SIZE = 61440;
    private static final String POST = "POST";
    private static final String SSL_PROTOCOL = "TLS";
    Map<String, List<String>> mHeaders = null;
    private HttpsURLConnection mHttpsURLConn = null;
    private Network mNetwork = null;
    Map<String, String> mParams = null;
    /* access modifiers changed from: private */
    public int mPhoneId;
    private JSONObject mPostData = null;

    static /* synthetic */ boolean lambda$static$0(String hostname, SSLSession session) {
        return true;
    }

    public HttpClient(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        HashMap hashMap = new HashMap();
        this.mHeaders = hashMap;
        hashMap.putAll(headers);
    }

    public void setParams(Map<String, String> params) {
        HashMap hashMap = new HashMap();
        this.mParams = hashMap;
        hashMap.putAll(params);
    }

    public void setPostData(JSONObject postData) {
        this.mPostData = postData;
    }

    public String getPostData() {
        return this.mPostData.toString().replaceAll("\\\\", "");
    }

    public void setNetwork(Network network) {
        this.mNetwork = network;
    }

    private HttpsURLConnection openURLConnection(String urlConn) throws Exception {
        URL url = new URL(urlConn);
        Network network = this.mNetwork;
        URLConnection urlConnection = network == null ? url.openConnection() : network.openConnection(url);
        if (urlConnection instanceof HttpsURLConnection) {
            SSLContext sc = SSLContext.getInstance(SSL_PROTOCOL);
            sc.init((KeyManager[]) null, (TrustManager[]) null, (SecureRandom) null);
            ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sc.getSocketFactory());
            ((HttpsURLConnection) urlConnection).setHostnameVerifier(DO_NOT_VERIFY);
        }
        return (HttpsURLConnection) urlConnection;
    }

    public Response getURLConnection(String url) throws Exception {
        try {
            String str = LOG_TAG;
            AECLog.i(str, "[HTTP GET] " + url, this.mPhoneId);
            HttpsURLConnection openURLConnection = openURLConnection(createReqUrl(url, this.mParams));
            this.mHttpsURLConn = openURLConnection;
            setRequestHeader(openURLConnection, this.mHeaders);
            this.mHttpsURLConn.setConnectTimeout(30000);
            this.mHttpsURLConn.setReadTimeout(30000);
            this.mHttpsURLConn.setRequestMethod("GET");
            this.mHttpsURLConn.setChunkedStreamingMode(0);
            this.mHttpsURLConn.connect();
            return getResponse(this.mHttpsURLConn);
        } catch (IOException e) {
            closeURLConnection();
            throw new IOException("getURLConnection IOException: " + e.getMessage());
        } catch (Exception e2) {
            closeURLConnection();
            throw new Exception("getURLConnection Exception: " + e2.getMessage());
        }
    }

    public Response postURLConnection(String url) throws Exception {
        try {
            String str = LOG_TAG;
            AECLog.i(str, "[HTTP POST] " + url, this.mPhoneId);
            CookieHandler.setDefault((CookieHandler) null);
            HttpsURLConnection openURLConnection = openURLConnection(url);
            this.mHttpsURLConn = openURLConnection;
            setRequestHeader(openURLConnection, this.mHeaders);
            AECLog.d(LOG_TAG, getPostData(), this.mPhoneId);
            this.mHttpsURLConn.setConnectTimeout(30000);
            this.mHttpsURLConn.setReadTimeout(30000);
            this.mHttpsURLConn.setRequestMethod("POST");
            this.mHttpsURLConn.setUseCaches(false);
            this.mHttpsURLConn.setDoOutput(true);
            this.mHttpsURLConn.setDoInput(true);
            OutputStream outputStream = this.mHttpsURLConn.getOutputStream();
            outputStream.write(getPostData().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            return getResponse(this.mHttpsURLConn);
        } catch (IOException e) {
            closeURLConnection();
            throw new IOException("postURLConnection IOException: " + e.getMessage());
        } catch (Exception e2) {
            closeURLConnection();
            throw new Exception("postURLConnection Exception: " + e2.getMessage());
        }
    }

    public void closeURLConnection() {
        HttpsURLConnection httpsURLConnection = this.mHttpsURLConn;
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
            this.mHttpsURLConn = null;
        }
    }

    /* access modifiers changed from: package-private */
    public String createReqUrl(String url, Map<String, String> params) {
        StringBuilder sbParams = new StringBuilder("?");
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue().contains(",")) {
                    for (String svc : entry.getValue().split(",")) {
                        sbParams.append(entry.getKey());
                        sbParams.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                        sbParams.append(svc.trim());
                        sbParams.append("&");
                    }
                } else {
                    sbParams.append(entry.getKey());
                    sbParams.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    sbParams.append(entry.getValue());
                    sbParams.append("&");
                }
            }
            sbParams.deleteCharAt(sbParams.length() - 1);
            AECLog.s(LOG_TAG, sbParams.toString(), this.mPhoneId);
        }
        return url + sbParams;
    }

    /* access modifiers changed from: package-private */
    public void setRequestHeader(HttpURLConnection conn, Map<String, List<String>> headers) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                conn.setRequestProperty(entry.getKey(), value);
                String str = LOG_TAG;
                AECLog.i(str, entry.getKey() + " : " + value, this.mPhoneId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Response getResponse(HttpURLConnection conn) {
        return new Response(getResStatusCode(conn), getResHeader(conn), getResBody(conn));
    }

    /* access modifiers changed from: package-private */
    public int getResStatusCode(HttpURLConnection conn) {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public Map<String, List<String>> getResHeader(HttpURLConnection conn) {
        return conn.getHeaderFields();
    }

    /* access modifiers changed from: package-private */
    public byte[] getResBody(HttpURLConnection conn) {
        int bodyReadPartial;
        byte[] body = "".getBytes();
        byte[] bodyTemp = new byte[512000];
        int bodyReadTotal = 0;
        InputStream in = null;
        try {
            InputStream in2 = new BufferedInputStream(conn.getInputStream());
            do {
                bodyReadPartial = in2.read(bodyTemp, bodyReadTotal, 61440);
                if (bodyReadPartial > 0) {
                    bodyReadTotal += bodyReadPartial;
                    continue;
                }
            } while (bodyReadPartial >= 0);
            if (bodyReadPartial == -1 && bodyReadTotal > 0) {
                body = new byte[bodyReadTotal];
                System.arraycopy(bodyTemp, 0, body, 0, bodyReadTotal);
            }
            try {
                in2.close();
            } catch (IOException e) {
                AECLog.e(LOG_TAG, "failed to close input stream", this.mPhoneId);
            }
        } catch (IOException e2) {
            AECLog.e(LOG_TAG, "failed to read input stream", this.mPhoneId);
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    AECLog.e(LOG_TAG, "failed to close input stream", this.mPhoneId);
                }
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                    AECLog.e(LOG_TAG, "failed to close input stream", this.mPhoneId);
                }
            }
            throw th;
        }
        return body;
    }

    public class Response {
        private byte[] mBody;
        private Map<String, List<String>> mHeader;
        private int mStatusCode;

        public Response(int statusCode, Map<String, List<String>> header, byte[] body) {
            this.mStatusCode = statusCode;
            this.mHeader = header;
            this.mBody = body;
            debugPrint();
        }

        public int getStatusCode() {
            return this.mStatusCode;
        }

        public Map<String, List<String>> getHeader() {
            return this.mHeader;
        }

        public byte[] getBody() {
            return this.mBody;
        }

        private void debugPrint() {
            String access$000 = HttpClient.LOG_TAG;
            AECLog.i(access$000, "[HTTP " + this.mStatusCode + "]", HttpClient.this.mPhoneId);
            Map<String, List<String>> map = this.mHeader;
            if (map != null && map.size() > 0) {
                for (String key : this.mHeader.keySet()) {
                    StringBuilder headerField = new StringBuilder();
                    headerField.append(key);
                    headerField.append(" : ");
                    for (String value : this.mHeader.get(key)) {
                        headerField.append(value);
                    }
                    AECLog.i(HttpClient.LOG_TAG, headerField.toString(), HttpClient.this.mPhoneId);
                }
            }
        }
    }
}
