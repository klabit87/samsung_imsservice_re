package com.sec.internal.helper.parser;

import com.sec.internal.helper.header.AuthenticationInfoHeader;
import com.sec.internal.log.IMSLog;

public class AuthInfoHeaderParser extends HttpHeaderParser {
    private static final String REGEX_CNONCE = "cnonce[\\s]*=";
    private static final String REGEX_NEXTNONCE = "nextnonce[\\s]*=";
    private static final String REGEX_NONCECOUNT = "nc[\\s]*=";
    private static final String REGEX_RSPAUTH = "rspauth[\\s]*=";
    private static final String TAG = "AuthInfoHeaderParser";
    private String paramSplitHeader = null;

    public AuthenticationInfoHeader parseHeaderValue(String headerValue) {
        AuthenticationInfoHeader parseAuthHeader = new AuthenticationInfoHeader();
        parse(parseAuthHeader, headerValue);
        IMSLog.d(TAG, "AuthenticationInfoHeader - parseHeaderValue : " + parseAuthHeader.toString());
        return parseAuthHeader;
    }

    private void parse(AuthenticationInfoHeader parseAuthInfoHeader, String headerValue) {
        if (parseAuthInfoHeader == null || headerValue == null) {
            throw new IllegalArgumentException("Authentication-Info Header Value is Null");
        }
        setQop(parseAuthInfoHeader, headerValue);
        setRspAuth(parseAuthInfoHeader, headerValue);
        setCNonce(parseAuthInfoHeader, headerValue);
        setNonceCount(parseAuthInfoHeader, headerValue);
        setNextNonce(parseAuthInfoHeader, headerValue);
        parseAuthInfoHeader.toString();
    }

    private void setQop(AuthenticationInfoHeader parsedAuthInfoHeader, String headerVal) {
        String qopVal = null;
        String splitHeader = getSplitHeader("qop[\\s]*=", headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            qopVal = getParamValue(splitHeader);
        }
        parsedAuthInfoHeader.setQop(qopVal);
    }

    private void setRspAuth(AuthenticationInfoHeader parsedAuthInfoHeader, String headerVal) {
        String rspAuthVal = null;
        String splitHeader = getSplitHeader(REGEX_RSPAUTH, headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            rspAuthVal = getParamValue(splitHeader);
        }
        parsedAuthInfoHeader.setRspauth(rspAuthVal);
    }

    private void setCNonce(AuthenticationInfoHeader parsedAuthInfoHeader, String headerVal) {
        String cNonceVal = null;
        String splitHeader = getSplitHeader(REGEX_CNONCE, headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            cNonceVal = getParamValue(splitHeader);
        }
        parsedAuthInfoHeader.setCnonce(cNonceVal);
    }

    private void setNonceCount(AuthenticationInfoHeader parsedAuthInfoHeader, String headerVal) {
        String nonceCountVal = null;
        String splitHeader = getSplitHeader(REGEX_NONCECOUNT, headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            nonceCountVal = getParamValue(splitHeader);
        }
        parsedAuthInfoHeader.setCnonce(nonceCountVal);
    }

    private void setNextNonce(AuthenticationInfoHeader parsedAuthInfoHeader, String headerVal) {
        String nextNonceVal = null;
        String splitHeader = getSplitHeader(REGEX_NEXTNONCE, headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            nextNonceVal = getParamValue(splitHeader);
        }
        parsedAuthInfoHeader.setNextNonce(nextNonceVal);
    }
}
