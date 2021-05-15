package com.sec.internal.helper.httpclient;

import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;

public class HttpResponseParams {
    private String mCipherSuite = null;
    private byte[] mDataBinary = null;
    private String mDataString = null;
    private Map<String, List<String>> mHeaders = null;
    private int mStatusCode = -1;
    private String mStatusReason = null;

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public void setStatusCode(int statusCode) {
        this.mStatusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return this.mHeaders;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.mHeaders = headers;
    }

    public String getDataString() {
        return this.mDataString;
    }

    public void setDataString(String dataString) {
        this.mDataString = dataString;
    }

    public byte[] getDataBinary() {
        return this.mDataBinary;
    }

    public void setStatusReason(String statusReason) {
        this.mStatusReason = statusReason;
    }

    public String getStatusReason() {
        return this.mStatusReason;
    }

    public void setDataBinary(byte[] dataBinary) {
        this.mDataBinary = dataBinary;
    }

    public void setCipherSuite(String cipherSuite) {
        this.mCipherSuite = cipherSuite;
    }

    public String getCipherSuite() {
        return this.mCipherSuite;
    }

    public String toString() {
        StringBuffer headers = new StringBuffer();
        Map<String, List<String>> map = this.mHeaders;
        if (map != null) {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                headers.append("\r\n        " + entry.getKey() + " : " + entry.getValue());
            }
        }
        try {
            return "HttpResponseParams[\r\n    mStatusCode=" + this.mStatusCode + "\r\n    mHeaders=" + headers + "\r\n    mDataString=" + IMSLog.numberChecker(this.mDataString) + "]";
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return "HttpResponseParams[\r\n    mStatusCode=" + this.mStatusCode + "\r\n    mHeaders=" + headers + "\r\n    mDataString length=" + this.mDataString.length() + "]";
        }
    }
}
