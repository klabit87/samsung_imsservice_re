package com.sec.internal.helper.header;

public class AuthenticationInfoHeader extends AuthenticationHeaders {
    public static final String HEADER_PARAM_NEXTNONCE = "nextnonce";
    public static final String HEADER_PARAM_RSP_AUTH = "rspauth";
    private String cnonce;
    private String nextNonce;
    private String nonceCount;
    private String qop;
    private String rspauth;

    public void setQop(String qop2) {
        this.qop = qop2;
    }

    public void setRspauth(String rspauth2) {
        this.rspauth = rspauth2;
    }

    public void setCnonce(String cnonce2) {
        this.cnonce = cnonce2;
    }

    public String getNextNonce() {
        return this.nextNonce;
    }

    public void setNextNonce(String nextNonce2) {
        this.nextNonce = nextNonce2;
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        StringBuilder sb = new StringBuilder();
        sb.append("AuthenticationInfoHeader [");
        String str5 = "";
        if (this.qop != null) {
            str = "qop=" + this.qop + ", ";
        } else {
            str = str5;
        }
        sb.append(str);
        if (this.rspauth != null) {
            str2 = "rspauth=" + this.rspauth + ", ";
        } else {
            str2 = str5;
        }
        sb.append(str2);
        if (this.cnonce != null) {
            str3 = "cnonce=" + this.cnonce + ", ";
        } else {
            str3 = str5;
        }
        sb.append(str3);
        if (this.nonceCount != null) {
            str4 = "nonceCount=" + this.nonceCount;
        } else {
            str4 = str5;
        }
        sb.append(str4);
        if (this.nextNonce != null) {
            str5 = "nextNonce=" + this.nextNonce;
        }
        sb.append(str5);
        sb.append("]");
        return sb.toString();
    }
}
