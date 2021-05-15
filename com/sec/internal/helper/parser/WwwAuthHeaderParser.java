package com.sec.internal.helper.parser;

import android.util.Log;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.log.IMSLog;

public class WwwAuthHeaderParser extends HttpHeaderParser {
    private static final String TAG = "WwwAuthHeaderParser";
    private String paramSplitHeader = null;

    public WwwAuthenticateHeader wwwAuthHeaderParse(String headerVal) {
        if (headerVal == null) {
            return null;
        }
        WwwAuthenticateHeader parseAuthHeader = new WwwAuthenticateHeader();
        parse(parseAuthHeader, headerVal);
        Log.d(TAG, "WwwAuthenticateHeader - wwwAuthHeaderParse : " + parseAuthHeader.toString());
        return parseAuthHeader;
    }

    public WwwAuthenticateHeader parseHeaderValue(String headerValue) {
        WwwAuthenticateHeader parseAuthHeader = new WwwAuthenticateHeader();
        parse(parseAuthHeader, headerValue);
        IMSLog.d(TAG, "WwwAuthenticateHeader - parseHeaderValue : " + parseAuthHeader.toString());
        return parseAuthHeader;
    }

    private void parse(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        if (parsedAuthHeader != null && headerVal != null) {
            setScheme(parsedAuthHeader, headerVal);
            setRealm(parsedAuthHeader, headerVal);
            setNonce(parsedAuthHeader, headerVal);
            setAlgorithm(parsedAuthHeader, headerVal);
            setQop(parsedAuthHeader, headerVal);
            setStale(parsedAuthHeader, headerVal);
            setOpaque(parsedAuthHeader, headerVal);
            parsedAuthHeader.toString();
        }
    }

    private void setScheme(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        if (headerVal.startsWith(WwwAuthenticateHeader.HEADER_PARAM_DIGEST_SCHEME)) {
            parsedAuthHeader.setScheme(WwwAuthenticateHeader.HEADER_PARAM_DIGEST_SCHEME);
        } else if (headerVal.startsWith(WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME)) {
            parsedAuthHeader.setScheme(WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME);
        } else {
            parsedAuthHeader.setScheme(WwwAuthenticateHeader.HEADER_PARAM_UNKNOWN_SCHEME);
        }
    }

    private void setRealm(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        String realmVal = null;
        String splitHeader = getSplitHeader("realm[\\s]*=", headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            realmVal = getParamValue(splitHeader);
        }
        parsedAuthHeader.setRealm(realmVal);
    }

    private void setNonce(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        String nonceVal;
        String splitHeader = getSplitHeader("nonce[\\s]*=", headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            nonceVal = getParamValue(splitHeader);
        } else {
            nonceVal = headerVal.substring(headerVal.indexOf(",") + 1).trim();
        }
        parsedAuthHeader.setNonce(nonceVal);
    }

    private void setAlgorithm(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        String algorithmVal = null;
        String splitHeader = getSplitHeader("algorithm[\\s]*=", headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            algorithmVal = getParamValue(splitHeader);
        }
        parsedAuthHeader.setAlgorithm(algorithmVal);
    }

    private void setQop(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        String qopVal = "";
        String splitHeader = getSplitHeader("qop[\\s]*=", headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader == null) {
            Log.d(TAG, "setQop - no qop");
        } else {
            qopVal = getParamValue(splitHeader);
            Log.d(TAG, "setQop - paramSplitHeader: " + this.paramSplitHeader + ", qopVal : " + qopVal);
        }
        parsedAuthHeader.setQop(qopVal);
    }

    private void setOpaque(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        String opaqueVal = null;
        String splitHeader = getSplitHeader("opaque[\\s]*=", headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            opaqueVal = getParamValue(splitHeader);
        }
        parsedAuthHeader.setOpaque(opaqueVal);
    }

    private void setStale(WwwAuthenticateHeader parsedAuthHeader, String headerVal) {
        String staleVal = null;
        String splitHeader = getSplitHeader("stale[\\s]*=", headerVal);
        this.paramSplitHeader = splitHeader;
        if (splitHeader != null) {
            staleVal = getParamValue(splitHeader);
        }
        parsedAuthHeader.setStale(Boolean.parseBoolean(staleVal));
    }
}
