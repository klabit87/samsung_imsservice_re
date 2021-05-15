package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.net.Network;
import android.net.TrafficStats;
import android.os.Process;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class HttpAdapter implements IHttpAdapter {
    protected static final String CHUNKED = "chunked";
    protected static final String GZIP = "gzip";
    protected static final String LOG_TAG = HttpAdapter.class.getSimpleName();
    protected static final int MAX_CHUNK_SIZE = 512000;
    protected static final long MAX_TIMEOUT = 30000;
    protected static final int MIN_CHUNK_SIZE = 61440;
    protected static final String SSL_PROTOCOL = "TLS";
    protected static CookieStore sCookieStore;
    protected final Map<String, List<String>> mHeaders = new HashMap();
    /* access modifiers changed from: private */
    public String mHttpMethodName = "GET";
    protected HttpURLConnection mHttpURLConn = null;
    protected Network mNetwork = null;
    protected final Map<String, String> mParams = new HashMap();
    protected int mPhoneId = 0;
    protected State mState;
    protected URL mURL = null;
    protected URLConnection mURLConn = null;
    protected StringBuffer mUrl = null;

    protected interface State extends IHttpAdapter {
    }

    static {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        sCookieStore = cookieManager.getCookieStore();
    }

    public HttpAdapter(int phoneId) {
        this.mPhoneId = phoneId;
        this.mState = new IdleState();
    }

    public boolean open(String url) {
        return this.mState.open(url);
    }

    public boolean close() {
        return this.mState.close();
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.mState.setHeaders(headers);
    }

    public void setParams(Map<String, String> params) {
        this.mState.setParams(params);
    }

    public void setMethod(String method) {
    }

    public void setContext(Context context) {
        this.mState.setContext(context);
    }

    public void setNetwork(Network network) {
        this.mState.setNetwork(network);
    }

    public Network getNetwork() {
        return this.mState.getNetwork();
    }

    public IHttpAdapter.Response request() {
        return this.mState.request();
    }

    protected class IdleState implements State {
        protected IdleState() {
        }

        public boolean open(String url) {
            if (!HttpAdapter.this.configureUrlConnection(url)) {
                return false;
            }
            HttpAdapter.this.mState = new ReadyState();
            return true;
        }

        public boolean close() {
            return false;
        }

        public void setHeaders(Map<String, List<String>> headers) {
            HttpAdapter.this.mHeaders.clear();
            HttpAdapter.this.mHeaders.putAll(headers);
        }

        public void setMethod(String method) {
            HttpAdapter.this.mState.setMethod(method);
            String unused = HttpAdapter.this.mHttpMethodName = method;
        }

        public void setParams(Map<String, String> params) {
            HttpAdapter.this.mParams.clear();
            HttpAdapter.this.mParams.putAll(params);
        }

        public void setContext(Context context) {
        }

        public void setNetwork(Network network) {
            String str = HttpAdapter.LOG_TAG;
            int i = HttpAdapter.this.mPhoneId;
            IMSLog.i(str, i, "setNetwork: " + network);
            HttpAdapter.this.mNetwork = network;
        }

        public Network getNetwork() {
            String str = HttpAdapter.LOG_TAG;
            int i = HttpAdapter.this.mPhoneId;
            IMSLog.i(str, i, "getNetwork: " + HttpAdapter.this.mNetwork);
            return HttpAdapter.this.mNetwork;
        }

        public IHttpAdapter.Response request() {
            return null;
        }
    }

    protected class ReadyState implements State {
        protected ReadyState() {
        }

        public boolean open(String url) {
            return false;
        }

        public boolean close() {
            HttpAdapter.this.mHttpURLConn.disconnect();
            HttpAdapter.this.mState = new IdleState();
            return true;
        }

        public void setHeaders(Map<String, List<String>> map) {
        }

        public void setMethod(String method) {
            String unused = HttpAdapter.this.mHttpMethodName = method;
        }

        public void setParams(Map<String, String> map) {
        }

        public void setContext(Context context) {
        }

        public void setNetwork(Network network) {
        }

        public Network getNetwork() {
            return null;
        }

        public IHttpAdapter.Response request() {
            byte[] bArr;
            HttpAdapter.this.tryToConnectHttpUrlConnectionWithinTimeOut();
            String stringBuffer = HttpAdapter.this.mUrl.toString();
            HttpAdapter httpAdapter = HttpAdapter.this;
            int resStatusCode = httpAdapter.getResStatusCode(httpAdapter.mHttpURLConn);
            HttpAdapter httpAdapter2 = HttpAdapter.this;
            Map<String, List<String>> resHeader = httpAdapter2.getResHeader(httpAdapter2.mHttpURLConn);
            if (HttpAdapter.this.mHttpMethodName.equalsIgnoreCase("POST")) {
                HttpAdapter httpAdapter3 = HttpAdapter.this;
                bArr = httpAdapter3.getPostResBody(httpAdapter3.mHttpURLConn);
            } else {
                HttpAdapter httpAdapter4 = HttpAdapter.this;
                bArr = httpAdapter4.getResBody(httpAdapter4.mHttpURLConn);
            }
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resHeader, bArr);
        }
    }

    /* access modifiers changed from: protected */
    public boolean configureUrlConnection(String url) {
        this.mUrl = createReqUrl(url, new StringBuffer(url), this.mParams);
        if (!openUrlConnection()) {
            return false;
        }
        setUrlConnection();
        IMSLog.i(LOG_TAG, this.mPhoneId, "configure httpUrlConnection based on urlConnection");
        this.mHttpURLConn = (HttpURLConnection) this.mURLConn;
        return true;
    }

    /* access modifiers changed from: protected */
    public StringBuffer createReqUrl(String url, StringBuffer reqUrl, Map<String, String> params) {
        IMSLog.i(LOG_TAG, this.mPhoneId, url);
        if (!(reqUrl == null || params == null || params.size() <= 0)) {
            if (this.mHttpMethodName.equalsIgnoreCase("GET")) {
                if (reqUrl.charAt(reqUrl.length() - 1) == '/') {
                    reqUrl.deleteCharAt(reqUrl.length() - 1);
                }
                reqUrl.append("?");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    reqUrl.append(entry.getKey());
                    reqUrl.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    try {
                        if (entry.getValue() == null) {
                            reqUrl.append("&");
                        } else {
                            if (entry.getValue().contains("%")) {
                                reqUrl.append(entry.getValue());
                            } else {
                                reqUrl.append(URLEncoder.encode(entry.getValue(), "utf-8"));
                            }
                            reqUrl.append("&");
                        }
                    } catch (UnsupportedEncodingException e) {
                        IMSLog.e(LOG_TAG, this.mPhoneId, "UnsupportedEncodingException occur. use plain string");
                        reqUrl.append(entry.getValue());
                    }
                }
                reqUrl.deleteCharAt(reqUrl.length() - 1);
            } else if (this.mHttpMethodName.equalsIgnoreCase("POST")) {
                StringBuilder postData = new StringBuilder();
                if (reqUrl.charAt(reqUrl.length() - 1) == '/') {
                    reqUrl.deleteCharAt(reqUrl.length() - 1);
                }
                reqUrl.append("?");
                for (Map.Entry<String, String> entry2 : params.entrySet()) {
                    if (postData.length() != 0) {
                        postData.append('&');
                    }
                    postData.append(entry2.getKey());
                    postData.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    try {
                        if (entry2.getValue().contains("%")) {
                            IMSLog.e(LOG_TAG, "already encoded. use plain string");
                            postData.append(entry2.getValue());
                        } else {
                            postData.append(URLEncoder.encode(entry2.getValue(), "utf-8"));
                        }
                    } catch (UnsupportedEncodingException e2) {
                        IMSLog.i(LOG_TAG, "UnsupportedEncodingException occur. use plain string");
                        postData.append(entry2.getValue());
                    }
                }
                reqUrl.append(postData.toString());
            }
            IMSLog.s(LOG_TAG, this.mPhoneId, reqUrl.toString());
        }
        return reqUrl;
    }

    /* access modifiers changed from: protected */
    public boolean openUrlConnection() {
        try {
            URL url = new URL(this.mUrl.toString());
            this.mURL = url;
            this.mURLConn = this.mNetwork != null ? this.mNetwork.openConnection(url) : url.openConnection();
            return true;
        } catch (MalformedURLException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "wrong url address");
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "cannot open url connection");
            e2.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void setUrlConnection() {
        if (this.mURLConn instanceof HttpsURLConnection) {
            setSocketFactory();
        } else {
            removeOldCookies();
        }
    }

    /* access modifiers changed from: protected */
    public void setSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance(SSL_PROTOCOL);
            sc.init((KeyManager[]) null, (TrustManager[]) null, (SecureRandom) null);
            SSLSocketFactory socketFactory = sc.getSocketFactory();
            IMSLog.i(LOG_TAG, this.mPhoneId, "get socketFactory for HTTPS");
            setSSLSocketFactory(socketFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    /* access modifiers changed from: protected */
    public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "set sslSocketFactory for HTTPS");
        ((HttpsURLConnection) this.mURLConn).setSSLSocketFactory(socketFactory);
    }

    /* access modifiers changed from: protected */
    public void removeOldCookies() {
        try {
            URI uri = this.mURL.toURI();
            for (HttpCookie cookie : sCookieStore.get(uri)) {
                sCookieStore.remove(uri, cookie);
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "remove old cookies for HTTP request");
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
    }

    /* access modifiers changed from: protected */
    public void tryToConnectHttpUrlConnectionWithinTimeOut() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        long retryTime = 30000;
        TrafficStats.setThreadStatsTag(Process.myTid());
        do {
            if (retryTime < 30000) {
                try {
                    URL url = new URL(this.mUrl.toString());
                    this.mURL = url;
                    this.mURLConn = this.mNetwork != null ? this.mNetwork.openConnection(url) : url.openConnection();
                    setUrlConnection();
                    this.mHttpURLConn = (HttpURLConnection) this.mURLConn;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    this.mHttpURLConn.disconnect();
                    retryTime = Calendar.getInstance().getTimeInMillis() - startTime;
                }
            }
            setHttpUrlConnection();
            addReqHeader(this.mHttpURLConn, this.mHeaders);
            this.mHttpURLConn.connect();
            retryTime = 30000;
            if (retryTime < 30000) {
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            }
        } while (retryTime < 30000);
    }

    /* access modifiers changed from: protected */
    public void tryToConnectHttpUrlConnection() {
        TrafficStats.setThreadStatsTag(Process.myTid());
        try {
            setHttpUrlConnection();
            addReqHeader(this.mHttpURLConn, this.mHeaders);
            this.mHttpURLConn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            this.mHttpURLConn.disconnect();
        }
    }

    /* access modifiers changed from: protected */
    public void setHttpUrlConnection() throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        this.mHttpURLConn.setConnectTimeout(30000);
        this.mHttpURLConn.setReadTimeout(60000);
        if (this.mHttpMethodName.equalsIgnoreCase("POST")) {
            this.mHttpURLConn.setRequestMethod("POST");
        } else {
            this.mHttpURLConn.setRequestMethod("GET");
        }
        this.mHttpURLConn.setChunkedStreamingMode(0);
    }

    /* access modifiers changed from: protected */
    public void addReqHeader(HttpURLConnection conn, Map<String, List<String>> headers) {
        String httpHeaders;
        String httpHeaders2;
        String str;
        String str2;
        IMSLog.i(LOG_TAG, this.mPhoneId, "+++ request header");
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            boolean first = true;
            for (String value : entry.getValue()) {
                if (first) {
                    conn.setRequestProperty(entry.getKey(), value);
                } else {
                    conn.addRequestProperty(entry.getKey(), value);
                }
                first = false;
                displayReqHeader(entry.getKey(), value);
            }
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "--- request header");
        if (IS_ENGG_BIN) {
            String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date());
            if (conn instanceof HttpsURLConnection) {
                if (this.mHttpMethodName.equalsIgnoreCase("POST")) {
                    str2 = "HTTPS POST\n";
                } else {
                    str2 = "HTTPS GET\n";
                }
                httpHeaders = str2;
            } else {
                if (this.mHttpMethodName.equalsIgnoreCase("POST")) {
                    str = "HTTP POST\n";
                } else {
                    str = "HTTP GET\n";
                }
                httpHeaders = str;
            }
            for (Map.Entry<String, List<String>> entry2 : headers.entrySet()) {
                for (String value2 : entry2.getValue()) {
                    if (entry2.getKey().equals(HttpController.HEADER_HOST)) {
                        httpHeaders2 = httpHeaders + entry2.getKey() + ": " + this.mUrl.toString() + "\n";
                    } else {
                        httpHeaders2 = httpHeaders + entry2.getKey() + ": " + value2 + "\n";
                    }
                }
            }
            ImsRegistry.getImsDiagMonitor().onIndication(1, httpHeaders, 100, 0, timestamp, "", "", "");
        }
    }

    /* access modifiers changed from: protected */
    public void displayReqHeader(String key, String value) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, key + ":" + value);
    }

    /* access modifiers changed from: protected */
    public int getResStatusCode(HttpURLConnection conn) {
        int statusCode = 0;
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "getResStatusCode: fail to read status code");
            if (e instanceof SSLHandshakeException) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "SSLHandshakeException: response code define 800");
                statusCode = 800;
            } else if (e instanceof SSLException) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "SSLException: response code define 801");
                statusCode = 801;
            } else {
                if (!(e instanceof ConnectException)) {
                    if (!(e instanceof InterruptedIOException)) {
                        if (e instanceof SocketException) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "SocketException: response code define 803");
                            statusCode = 803;
                        } else if (e instanceof SocketTimeoutException) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "SocketTimeoutException: response code define 804");
                            statusCode = 804;
                        } else if (e instanceof UnknownHostException) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "UnknownHostException: response code define 805");
                            statusCode = 805;
                        }
                    }
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "ConnectException: response code define 802");
                statusCode = 802;
            }
            e.printStackTrace();
            return statusCode;
        } catch (Throwable th) {
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public Map<String, List<String>> getResHeader(HttpURLConnection conn) {
        return conn.getHeaderFields();
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* access modifiers changed from: protected */
    public byte[] getContentLengthBody(byte[] body, HttpURLConnection conn, int contentLength) {
        InputStream in;
        try {
            in = new BufferedInputStream(conn.getInputStream());
            int bodyReadTotal = 0;
            byte[] bodyTemp = new byte[(contentLength * 2)];
            body = new byte[contentLength];
            while (true) {
                int read = in.read(bodyTemp, bodyReadTotal, bodyTemp.length - bodyReadTotal);
                int bodyReadPartial = read;
                if (read == -1) {
                    break;
                }
                bodyReadTotal += bodyReadPartial;
            }
            if (contentLength != bodyReadTotal) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "getContentLengthBody: wrong http header(header:" + contentLength + ",actual:" + bodyReadTotal + ")");
            }
            System.arraycopy(bodyTemp, 0, body, 0, contentLength);
            in.close();
            return body;
        } catch (IOException e) {
            try {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getContentLengthBody: fail to read body");
                e.printStackTrace();
            } catch (Throwable th) {
            }
            return body;
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    /* access modifiers changed from: protected */
    public byte[] getTransferEncodingBody(byte[] body, HttpURLConnection conn) {
        byte[] bodyTemp;
        boolean readError;
        try {
            InputStream in = new BufferedInputStream(conn.getInputStream());
            int bodyReadPartial = 0;
            int bodyReadTotal = 0;
            try {
                bodyTemp = new byte[512000];
                readError = false;
                do {
                    bodyReadPartial = in.read(bodyTemp, bodyReadTotal, 61440);
                    if (bodyReadPartial > 0) {
                        bodyReadTotal += bodyReadPartial;
                        continue;
                    }
                } while (bodyReadPartial >= 0);
            } catch (IOException e) {
                readError = true;
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "getTransferEncodingBody: error reading chunked input stream" + e.getMessage());
            } catch (Throwable th) {
                in.close();
                throw th;
            }
            if (bodyReadPartial != -1 || bodyReadTotal <= 0 || readError) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getTransferEncodingBody: chunked body empty or error");
            } else {
                body = new byte[bodyReadTotal];
                System.arraycopy(bodyTemp, 0, body, 0, bodyReadTotal);
                String str2 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str2, i2, "getTransferEncodingBody: chunked response length [" + bodyReadTotal + "]");
            }
            in.close();
            return body;
        } catch (IOException e2) {
            try {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getTransferEncodingBody: fail to read body");
                e2.printStackTrace();
            } catch (Throwable th2) {
            }
            return body;
        } catch (Throwable th3) {
            th.addSuppressed(th3);
        }
    }

    /* access modifiers changed from: protected */
    public byte[] getResBody(HttpURLConnection conn) {
        byte[] body = null;
        if (conn.getHeaderField("Content-Length") != null) {
            int contentLength = Integer.parseInt(conn.getHeaderField("Content-Length"));
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "getResBody: Content-Length " + contentLength);
            if (contentLength <= 0) {
                return null;
            }
            body = getContentLengthBody((byte[]) null, conn, contentLength);
        }
        if (!CHUNKED.equals(conn.getHeaderField(HttpRequest.HEADER_TRANSFER_ENCODING))) {
            return body;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "getResBody: Transfer-Encoding");
        return getTransferEncodingBody(body, conn);
    }

    /* access modifiers changed from: private */
    public byte[] getPostResBody(HttpURLConnection conn) {
        String str;
        StringBuilder sb;
        InputStream in = null;
        int bodyReadTotal = 0;
        byte[] body = null;
        if (conn.getHeaderField("Content-Length") != null) {
            int contentLength = Integer.parseInt(conn.getHeaderField("Content-Length"));
            if (contentLength <= 0) {
                return null;
            }
            byte[] bodyTemp = new byte[(contentLength * 2)];
            body = new byte[contentLength];
            try {
                InputStream in2 = new BufferedInputStream(conn.getInputStream());
                while (true) {
                    int read = in2.read(bodyTemp, bodyReadTotal, bodyTemp.length - bodyReadTotal);
                    int bodyReadPartial = read;
                    if (read == -1) {
                        break;
                    }
                    bodyReadTotal += bodyReadPartial;
                }
                if (contentLength != bodyReadTotal) {
                    String str2 = LOG_TAG;
                    IMSLog.e(str2, "wrong http header(header:" + contentLength + ",actual:" + bodyReadTotal + ")");
                }
                System.arraycopy(bodyTemp, 0, body, 0, contentLength);
                try {
                    in2.close();
                } catch (IOException e) {
                    e = e;
                    str = LOG_TAG;
                    sb = new StringBuilder();
                }
            } catch (IOException e2) {
                IMSLog.e(LOG_TAG, "fail to read body");
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        e = e3;
                        str = LOG_TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e4) {
                        String str3 = LOG_TAG;
                        IMSLog.e(str3, "Error closing input stream: " + e4.getMessage());
                    }
                }
                throw th;
            }
        }
        return body;
        sb.append("Error closing input stream: ");
        sb.append(e.getMessage());
        IMSLog.e(str, sb.toString());
        return body;
    }

    /* access modifiers changed from: protected */
    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
