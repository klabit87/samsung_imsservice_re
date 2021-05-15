package com.sec.internal.helper.header;

public class WwwAuthenticateHeader extends AuthenticationHeaders {
    public static final String HEADER_NAME = "WWW-Authenticate";
    public static final String HEADER_PARAM_ALGORITHM = "algorithm";
    public static final String HEADER_PARAM_BASIC_SCHEME = "Basic";
    public static final String HEADER_PARAM_DIGEST_SCHEME = "Digest";
    public static final String HEADER_PARAM_NONCE = "nonce";
    public static final String HEADER_PARAM_OPAQUE = "opaque";
    public static final String HEADER_PARAM_REALM = "realm";
    public static final String HEADER_PARAM_STALE = "stale";
    public static final String HEADER_PARAM_UNKNOWN_SCHEME = "Unknown";
    private String algorithm = null;
    private String nonce = null;
    private String opaque = null;
    private String qop = null;
    private String realm = null;
    private String scheme = null;
    private boolean stale = false;

    public void setScheme(String scheme2) {
        this.scheme = scheme2;
    }

    public String getRealm() {
        return this.realm;
    }

    public void setRealm(String realm2) {
        this.realm = realm2;
    }

    public String getNonce() {
        return this.nonce;
    }

    public void setNonce(String nonce2) {
        this.nonce = nonce2;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String algorithm2) {
        this.algorithm = algorithm2;
    }

    public String getQop() {
        return this.qop;
    }

    public void setQop(String qop2) {
        this.qop = qop2;
    }

    public String getOpaque() {
        return this.opaque;
    }

    public void setOpaque(String opaque2) {
        this.opaque = opaque2;
    }

    public boolean isStale() {
        return this.stale;
    }

    public void setStale(boolean b) {
        this.stale = b;
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        StringBuilder sb = new StringBuilder();
        sb.append("WwwAuthenticateHeader [");
        String str6 = "";
        if (this.scheme != null) {
            str = "scheme=" + this.scheme + ", ";
        } else {
            str = str6;
        }
        sb.append(str);
        if (this.realm != null) {
            str2 = "realm=" + this.realm + ", ";
        } else {
            str2 = str6;
        }
        sb.append(str2);
        if (this.nonce != null) {
            str3 = "nonce=" + this.nonce + ", ";
        } else {
            str3 = str6;
        }
        sb.append(str3);
        if (this.algorithm != null) {
            str4 = "algorithm=" + this.algorithm + ", ";
        } else {
            str4 = str6;
        }
        sb.append(str4);
        if (this.qop != null) {
            str5 = "qop=" + this.qop + ", ";
        } else {
            str5 = str6;
        }
        sb.append(str5);
        if (this.opaque != null) {
            str6 = "opaque=" + this.opaque + ", ";
        }
        sb.append(str6);
        sb.append("stale=");
        sb.append(this.stale);
        sb.append("]");
        return sb.toString();
    }
}
