package com.sec.internal.ims.config.adapters;

import android.net.Network;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class HttpAdapterVzw extends HttpAdapter {
    protected static final String LOG_TAG = HttpAdapterVzw.class.getSimpleName();
    protected static SSLSocketFactory mSocketFactory = null;

    public HttpAdapterVzw(int phoneId) {
        super(phoneId);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String url) {
            IMSLog.d(HttpAdapterVzw.LOG_TAG, HttpAdapterVzw.this.mPhoneId, "open urlConnection");
            if (!HttpAdapterVzw.this.configureUrlConnection(url)) {
                return false;
            }
            HttpAdapterVzw.this.mState = new ReadyState();
            return true;
        }

        public void setNetwork(Network network) {
            String str = HttpAdapterVzw.LOG_TAG;
            int i = HttpAdapterVzw.this.mPhoneId;
            IMSLog.d(str, i, "setNetwork: " + network);
            HttpAdapterVzw.this.mNetwork = network;
            if (HttpAdapterVzw.this.mNetwork == null) {
                IMSLog.d(HttpAdapterVzw.LOG_TAG, HttpAdapterVzw.this.mPhoneId, "setNetwork: reset mSocketFactory");
                HttpAdapterVzw.mSocketFactory = null;
            }
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public IHttpAdapter.Response request() {
            IMSLog.d(HttpAdapterVzw.LOG_TAG, HttpAdapterVzw.this.mPhoneId, "connect httpUrlConnection");
            HttpAdapterVzw.this.tryToConnectHttpUrlConnection();
            String stringBuffer = HttpAdapterVzw.this.mUrl.toString();
            HttpAdapterVzw httpAdapterVzw = HttpAdapterVzw.this;
            int resStatusCode = httpAdapterVzw.getResStatusCode(httpAdapterVzw.mHttpURLConn);
            HttpAdapterVzw httpAdapterVzw2 = HttpAdapterVzw.this;
            Map<String, List<String>> resHeader = httpAdapterVzw2.getResHeader(httpAdapterVzw2.mHttpURLConn);
            HttpAdapterVzw httpAdapterVzw3 = HttpAdapterVzw.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resHeader, httpAdapterVzw3.getResBody(httpAdapterVzw3.mHttpURLConn));
        }

        public boolean close() {
            IMSLog.d(HttpAdapterVzw.LOG_TAG, HttpAdapterVzw.this.mPhoneId, "close httpUrlConnection");
            HttpAdapterVzw.this.mHttpURLConn.disconnect();
            HttpAdapterVzw.this.mState = new IdleState();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void setSocketFactory() {
        if (mSocketFactory == null) {
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init((KeyManager[]) null, (TrustManager[]) null, (SecureRandom) null);
                mSocketFactory = sc.getSocketFactory();
                IMSLog.d(LOG_TAG, this.mPhoneId, "get socketFactory for HTTPS");
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new Error(e);
            }
        }
        IMSLog.d(LOG_TAG, this.mPhoneId, "set sslSocketFactor for HTTPS");
        ((HttpsURLConnection) this.mURLConn).setSSLSocketFactory(mSocketFactory);
    }

    /* access modifiers changed from: protected */
    public void setHttpUrlConnection() throws IOException {
        super.setHttpUrlConnection();
        this.mHttpURLConn.setRequestProperty("Accept-Encoding", "gzip");
        this.mHttpURLConn.setRequestProperty("Keep-Alive", CloudMessageProviderContract.JsonData.TRUE);
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    /* access modifiers changed from: protected */
    public byte[] getContentLengthBody(byte[] body, HttpURLConnection conn, int contentLength) {
        InputStream in;
        BufferedReader bufferReader;
        String encoding = conn.getContentEncoding();
        boolean isNeededGZIPInputStream = encoding != null && encoding.equals("gzip");
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "encoding: " + encoding + " isNeededGZIPInputStream: " + isNeededGZIPInputStream);
        if (isNeededGZIPInputStream) {
            try {
                in = new GZIPInputStream(conn.getInputStream());
            } catch (IOException e) {
                try {
                    IMSLog.e(LOG_TAG, this.mPhoneId, "fail to read body");
                    e.printStackTrace();
                } catch (Throwable th) {
                }
                return body;
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            in = new BufferedInputStream(conn.getInputStream());
        }
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            try {
                bufferReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String readLine = bufferReader.readLine();
                    String read = readLine;
                    if (readLine != null) {
                        sb.append(read);
                    } else {
                        body = new byte[sb.toString().length()];
                        System.arraycopy(sb.toString().getBytes(), 0, body, 0, sb.toString().length());
                        bufferReader.close();
                        inputStreamReader.close();
                        in.close();
                        return body;
                    }
                }
            } catch (Throwable th3) {
                inputStreamReader.close();
                throw th3;
            }
            throw th;
        } catch (Throwable th4) {
            in.close();
            throw th4;
        }
    }
}
