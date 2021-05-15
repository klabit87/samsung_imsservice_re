package com.sec.internal.ims.config.adapters;

import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class HttpAdapterUp extends HttpAdapter {
    protected static final String LOG_TAG = HttpAdapterUp.class.getSimpleName();

    public HttpAdapterUp(int phoneId) {
        super(phoneId);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String url) {
            HttpAdapterUp httpAdapterUp = HttpAdapterUp.this;
            httpAdapterUp.mUrl = httpAdapterUp.createReqUrlWithMask(new StringBuffer(url), HttpAdapterUp.this.mParams, false);
            HttpAdapterUp httpAdapterUp2 = HttpAdapterUp.this;
            httpAdapterUp2.dumpAutoConfUrl(url, httpAdapterUp2.mUrl, HttpAdapterUp.this.mParams);
            if (!HttpAdapterUp.this.openUrlConnection()) {
                return false;
            }
            HttpAdapterUp.this.setUrlConnection();
            HttpAdapterUp httpAdapterUp3 = HttpAdapterUp.this;
            httpAdapterUp3.mHttpURLConn = (HttpURLConnection) httpAdapterUp3.mURLConn;
            HttpAdapterUp.this.mState = new ReadyState();
            return true;
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public boolean close() {
            HttpAdapterUp.this.mHttpURLConn.disconnect();
            HttpAdapterUp.this.mState = new IdleState();
            return true;
        }

        public IHttpAdapter.Response request() {
            HttpAdapterUp.this.tryToConnectHttpUrlConnection();
            String stringBuffer = HttpAdapterUp.this.mUrl.toString();
            HttpAdapterUp httpAdapterUp = HttpAdapterUp.this;
            int resStatusCode = httpAdapterUp.getResStatusCode(httpAdapterUp.mHttpURLConn);
            HttpAdapterUp httpAdapterUp2 = HttpAdapterUp.this;
            Map<String, List<String>> resHeader = httpAdapterUp2.getResHeader(httpAdapterUp2.mHttpURLConn);
            HttpAdapterUp httpAdapterUp3 = HttpAdapterUp.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resHeader, httpAdapterUp3.getResBody(httpAdapterUp3.mHttpURLConn));
        }
    }

    /* access modifiers changed from: private */
    public StringBuffer createReqUrlWithMask(StringBuffer url, Map<String, String> params, boolean needMask) {
        String value;
        StringBuffer stringBuffer = url;
        if (stringBuffer != null && params != null && params.size() > 0) {
            if (stringBuffer.charAt(url.length() - 1) == '/') {
                stringBuffer.deleteCharAt(url.length() - 1);
            }
            stringBuffer.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue() == null) {
                    stringBuffer.append(key);
                    stringBuffer.append("=&");
                } else {
                    for (String val : entry.getValue().split("\\|\\|")) {
                        try {
                            stringBuffer.append(key);
                            stringBuffer.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                            if (val.contains("%")) {
                                value = val;
                            } else {
                                value = URLEncoder.encode(val, "utf-8");
                            }
                            if (needMask) {
                                if (!key.equals("IMSI")) {
                                    if (!key.equals("msisdn")) {
                                        if (!key.equals(ConfigConstants.PNAME.OTP)) {
                                            value = "xxx";
                                        }
                                    }
                                }
                                if (value.contains("%")) {
                                    if (value.length() > 8) {
                                        value = value.substring(0, 8) + "xxx";
                                    } else {
                                        value = "xxx";
                                    }
                                } else if (value.length() > 5) {
                                    value = value.substring(0, 5) + "xxx";
                                } else {
                                    value = "xxx";
                                }
                            }
                            stringBuffer.append(value);
                            stringBuffer.append("&");
                        } catch (UnsupportedEncodingException e) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "UnsupportedEncodingException occur. use plain string");
                            stringBuffer.append(val);
                            stringBuffer.append("&");
                        }
                    }
                }
            }
            stringBuffer.deleteCharAt(url.length() - 1);
        }
        return stringBuffer;
    }

    /* access modifiers changed from: private */
    public void dumpAutoConfUrl(String url, StringBuffer reqUrl, Map<String, String> params) {
        IMSLog.i(LOG_TAG, this.mPhoneId, url);
        if (!SimUtil.getSimMno(this.mPhoneId).isVodafone() || !IMSLog.isShipBuild()) {
            IMSLog.s(LOG_TAG, this.mPhoneId, reqUrl.toString());
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, createReqUrlWithMask(new StringBuffer(url), params, true).toString());
    }
}
