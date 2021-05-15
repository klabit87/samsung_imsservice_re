package com.sec.internal.ims.config.adapters;

import android.net.Network;
import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpAdapterCmcc extends HttpAdapter {
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
    };
    protected static final String LOG_TAG = HttpAdapterCmcc.class.getSimpleName();

    public HttpAdapterCmcc(int phoneId) {
        super(phoneId);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String url) {
            setNetwork((Network) null);
            if (!HttpAdapterCmcc.this.configureUrlConnection(url)) {
                return false;
            }
            HttpAdapterCmcc.this.mState = new ReadyState();
            return true;
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public IHttpAdapter.Response request() {
            HttpAdapterCmcc.this.tryToConnectHttpUrlConnectionWithinTimeOut();
            String stringBuffer = HttpAdapterCmcc.this.mUrl.toString();
            HttpAdapterCmcc httpAdapterCmcc = HttpAdapterCmcc.this;
            int resStatusCode = httpAdapterCmcc.getResStatusCode(httpAdapterCmcc.mHttpURLConn);
            HttpAdapterCmcc httpAdapterCmcc2 = HttpAdapterCmcc.this;
            Map<String, List<String>> resHeader = httpAdapterCmcc2.getResHeader(httpAdapterCmcc2.mHttpURLConn);
            HttpAdapterCmcc httpAdapterCmcc3 = HttpAdapterCmcc.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resHeader, httpAdapterCmcc3.getResBody(httpAdapterCmcc3.mHttpURLConn));
        }

        public boolean close() {
            HttpAdapterCmcc.this.mHttpURLConn.disconnect();
            HttpAdapterCmcc.this.mState = new IdleState();
            return true;
        }
    }

    private static class miTM implements TrustManager, X509TrustManager {
        private miTM() {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }
    }

    /* access modifiers changed from: protected */
    public void setSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init((KeyManager[]) null, new TrustManager[]{new miTM()}, new SecureRandom());
            SSLSocketFactory socketFactory = sc.getSocketFactory();
            IMSLog.i(LOG_TAG, this.mPhoneId, "get socketFactory for HTTPS");
            setSSLSocketFactory(socketFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    /* access modifiers changed from: protected */
    public void setSSLSocketFactory(SSLSocketFactory socketFactory) {
        super.setSSLSocketFactory(socketFactory);
        ((HttpsURLConnection) this.mURLConn).setHostnameVerifier(DO_NOT_VERIFY);
    }

    /* access modifiers changed from: protected */
    public void setHttpUrlConnection() throws IOException {
        super.setHttpUrlConnection();
        this.mHttpURLConn.setInstanceFollowRedirects(false);
    }

    /* access modifiers changed from: protected */
    public int getResStatusCode(HttpURLConnection conn) {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "fail to read status code");
            e.printStackTrace();
            return 0;
        } catch (Throwable th) {
            return 0;
        }
    }
}
