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

public class HttpAdapterVzwMvs extends HttpAdapter {
    protected static final String LOG_TAG = HttpAdapterVzwMvs.class.getSimpleName();
    protected static SSLSocketFactory mSocketFactory = null;

    public HttpAdapterVzwMvs(int phoneId) {
        super(phoneId);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String url) {
            IMSLog.i(HttpAdapterVzwMvs.LOG_TAG, HttpAdapterVzwMvs.this.mPhoneId, "open urlConnection");
            if (!HttpAdapterVzwMvs.this.configureUrlConnection(url)) {
                return false;
            }
            HttpAdapterVzwMvs.this.mState = new ReadyState();
            return true;
        }

        public void setNetwork(Network network) {
            String str = HttpAdapterVzwMvs.LOG_TAG;
            int i = HttpAdapterVzwMvs.this.mPhoneId;
            IMSLog.i(str, i, "setNetwork: " + network);
            HttpAdapterVzwMvs.this.mNetwork = network;
            if (HttpAdapterVzwMvs.this.mNetwork == null) {
                IMSLog.i(HttpAdapterVzwMvs.LOG_TAG, HttpAdapterVzwMvs.this.mPhoneId, "setNetwork: reset mSocketFactory");
                HttpAdapterVzwMvs.mSocketFactory = null;
            }
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public IHttpAdapter.Response request() {
            IMSLog.i(HttpAdapterVzwMvs.LOG_TAG, HttpAdapterVzwMvs.this.mPhoneId, "connect httpUrlConnection");
            HttpAdapterVzwMvs.this.tryToConnectHttpUrlConnection();
            String stringBuffer = HttpAdapterVzwMvs.this.mUrl.toString();
            HttpAdapterVzwMvs httpAdapterVzwMvs = HttpAdapterVzwMvs.this;
            int resStatusCode = httpAdapterVzwMvs.getResStatusCode(httpAdapterVzwMvs.mHttpURLConn);
            HttpAdapterVzwMvs httpAdapterVzwMvs2 = HttpAdapterVzwMvs.this;
            Map<String, List<String>> resHeader = httpAdapterVzwMvs2.getResHeader(httpAdapterVzwMvs2.mHttpURLConn);
            HttpAdapterVzwMvs httpAdapterVzwMvs3 = HttpAdapterVzwMvs.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resHeader, httpAdapterVzwMvs3.getResBody(httpAdapterVzwMvs3.mHttpURLConn));
        }

        public boolean close() {
            IMSLog.i(HttpAdapterVzwMvs.LOG_TAG, HttpAdapterVzwMvs.this.mPhoneId, "close httpUrlConnection");
            HttpAdapterVzwMvs.this.mHttpURLConn.disconnect();
            HttpAdapterVzwMvs.this.mState = new IdleState();
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
                IMSLog.i(LOG_TAG, this.mPhoneId, "get socketFactory for HTTPS");
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                throw new Error(e);
            }
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "set sslSocketFactor for HTTPS");
        ((HttpsURLConnection) this.mURLConn).setSSLSocketFactory(mSocketFactory);
    }

    /* access modifiers changed from: protected */
    public void setHttpUrlConnection() throws IOException {
        super.setHttpUrlConnection();
        this.mHttpURLConn.setRequestProperty("Accept-Encoding", "gzip");
        this.mHttpURLConn.setRequestProperty("Keep-Alive", CloudMessageProviderContract.JsonData.TRUE);
    }

    /* access modifiers changed from: protected */
    public int getResStatusCode(HttpURLConnection conn) {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "getResStatusCode: fail to read status code: " + e.getMessage());
            return 0;
        }
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
        IMSLog.i(str, i, "encoding: " + encoding + " isNeededGZIPInputStream: " + isNeededGZIPInputStream);
        if (isNeededGZIPInputStream) {
            try {
                in = new GZIPInputStream(conn.getInputStream());
            } catch (IOException e) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "fail to read body");
                e.printStackTrace();
            } catch (Throwable th) {
                th.addSuppressed(th);
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
                    if (readLine == null) {
                        break;
                    }
                    sb.append(read);
                }
                body = new byte[sb.toString().length()];
                System.arraycopy(sb.toString().getBytes(), 0, body, 0, sb.toString().length());
                bufferReader.close();
                inputStreamReader.close();
                in.close();
                return body;
            } catch (Throwable th2) {
                inputStreamReader.close();
                throw th2;
            }
            throw th;
        } catch (Throwable th3) {
            in.close();
            throw th3;
        }
    }
}
