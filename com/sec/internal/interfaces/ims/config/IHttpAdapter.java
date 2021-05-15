package com.sec.internal.interfaces.ims.config;

import android.content.Context;
import android.net.Network;
import android.os.SemSystemProperties;
import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IHttpAdapter {
    public static final boolean IS_ENGG_BIN = ConfigConstants.VALUE.INFO_COMPLETED.equals(SemSystemProperties.get("ro.product_ship", ConfigConstants.VALUE.INFO_COMPLETED));

    boolean close();

    Network getNetwork();

    boolean open(String str);

    Response request();

    void setContext(Context context);

    void setHeaders(Map<String, List<String>> map);

    void setMethod(String str);

    void setNetwork(Network network);

    void setParams(Map<String, String> map);

    public static class Response {
        private static final String CHARSET = "utf-8";
        public static final int EXCEPTION_CONNECT = 802;
        public static final int EXCEPTION_SOCKET = 803;
        public static final int EXCEPTION_SOCKET_TIMEOUT = 804;
        public static final int EXCEPTION_SSL = 801;
        public static final int EXCEPTION_SSL_HANDSHAKE = 800;
        public static final int EXCEPTION_UNKNOWN_HOST = 805;
        private static final String LOG_TAG = IHttpAdapter.class.getSimpleName();
        private byte[] mBody = null;
        private Map<String, List<String>> mHeader = null;
        private int mStatusCode = 0;

        public Response(String url, int statusCode, Map<String, List<String>> header, byte[] body) {
            this.mStatusCode = statusCode;
            this.mHeader = header;
            this.mBody = body;
            debugPrint();
        }

        public int getStatusCode() {
            return this.mStatusCode;
        }

        public void setStatusCode(int statusCode) {
            this.mStatusCode = statusCode;
        }

        public Map<String, List<String>> getHeader() {
            return this.mHeader;
        }

        public byte[] getBody() {
            return this.mBody;
        }

        private void debugPrint() {
            Log.d(LOG_TAG, "HTTP(S) response : status code:" + this.mStatusCode);
            IMSLog.c(LogClass.HTTP_RESPONSE, "HR:" + this.mStatusCode);
            if (this.mHeader != null) {
                Log.d(LOG_TAG, "+++ HTTP(S) response : header");
                if (this.mHeader.size() > 0) {
                    for (String key : this.mHeader.keySet()) {
                        StringBuffer headerField = new StringBuffer();
                        headerField.append(key);
                        headerField.append(":");
                        for (String value : this.mHeader.get(key)) {
                            headerField.append("[");
                            headerField.append(value);
                            headerField.append("]");
                        }
                        debugPrintHeaderField(headerField);
                    }
                }
                Log.d(LOG_TAG, "--- HTTP(S) response : header");
            }
            if (this.mBody != null) {
                Log.d(LOG_TAG, "+++ HTTP(S) response : body");
                try {
                    if (this.mBody.length > 256) {
                        Log.d(LOG_TAG, new String(this.mBody, 0, 128, CHARSET));
                        Log.d(LOG_TAG, new String(this.mBody, this.mBody.length - 128, 128, CHARSET));
                    } else {
                        Log.d(LOG_TAG, new String(this.mBody, CHARSET));
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.e(LOG_TAG, "UnsupportedEncodingException: " + e.getMessage());
                }
                Log.d(LOG_TAG, "--- HTTP(S) response : body");
            }
            if (IHttpAdapter.IS_ENGG_BIN) {
                String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date());
                StringBuffer buf = new StringBuffer();
                Map<String, List<String>> map = this.mHeader;
                if (map != null && map.size() > 0) {
                    for (String key2 : this.mHeader.keySet()) {
                        for (String value2 : this.mHeader.get(key2)) {
                            if (buf.length() == 0) {
                                buf.append(value2 + "\n");
                            } else {
                                buf.append(key2 + ": " + value2 + "\n");
                            }
                        }
                    }
                }
                String httpContents = buf.toString();
                if (this.mBody != null) {
                    httpContents = httpContents + "\n";
                    try {
                        httpContents = httpContents + new String(this.mBody, CHARSET);
                    } catch (UnsupportedEncodingException e2) {
                        Log.e(LOG_TAG, "UnsupportedEncodingException: " + e2.getMessage());
                    }
                }
                if (httpContents != null) {
                    ImsRegistry.getImsDiagMonitor().onIndication(1, httpContents, 100, 1, timestamp, "", "", "");
                }
            }
        }

        private void debugPrintHeaderField(StringBuffer headerField) {
            Log.d(LOG_TAG, headerField.toString());
        }
    }
}
