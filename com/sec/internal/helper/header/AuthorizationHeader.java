package com.sec.internal.helper.header;

import android.text.TextUtils;
import com.sec.internal.helper.httpclient.DigestAuth;

public class AuthorizationHeader extends AuthenticationHeaders {
    private String algorithm = null;
    private String cnonce = null;
    private String nonce = null;
    private String nonceCount = null;
    private String opaque = null;
    private String qop = null;
    private String realm = null;
    private String response = null;
    private String uri = "/";
    private String userName = null;

    public String getUserName() {
        return this.userName;
    }

    public String getRealm() {
        return this.realm;
    }

    public String getNonce() {
        return this.nonce;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getQop() {
        return this.qop;
    }

    public String getCnonce() {
        return this.cnonce;
    }

    public void setCnonce(String cnonce2) {
        this.cnonce = cnonce2;
    }

    public String getNonceCount() {
        return this.nonceCount;
    }

    public String getResponse() {
        return this.response;
    }

    public final String getParamValue(String pValue) {
        return pValue == null ? "" : pValue;
    }

    public String toString() {
        return "AuthorizationHeader [username=" + getParamValue(this.userName) + ", realm=" + getParamValue(this.realm) + ", nonce=" + getParamValue(this.nonce) + ", algorithm=" + getParamValue(this.algorithm) + ", uri=" + getParamValue(this.uri) + ", qop=" + getParamValue(this.qop) + ", opaque=" + getParamValue(this.opaque) + ", cnonce=" + getParamValue(this.cnonce) + ", nonceCount=" + getParamValue(this.nonceCount) + ", response=" + getParamValue(this.response) + "]";
    }

    public static String getAuthorizationHeader(DigestAuth digestAuth, WwwAuthenticateHeader wwwAuthParsedHeader) {
        StringBuilder sb = new StringBuilder();
        sb.append(getAuthorizationHeader(digestAuth.getUsername(), digestAuth.getRealm(), digestAuth.getDigestUri(), digestAuth.getNonce(), digestAuth.getResp()));
        sb.append(", algorithm=");
        sb.append(digestAuth.getAlgorithm());
        if (!TextUtils.isEmpty(digestAuth.getQop())) {
            sb.append(", nc=");
            sb.append(digestAuth.getNC());
            sb.append(", qop=");
            sb.append(digestAuth.getQop());
        }
        if (!TextUtils.isEmpty(digestAuth.getCnonce())) {
            sb.append(", cnonce=\"");
            sb.append(digestAuth.getCnonce());
            sb.append("\"");
        }
        if (!TextUtils.isEmpty(wwwAuthParsedHeader.getOpaque())) {
            sb.append(", opaque=\"");
            sb.append(wwwAuthParsedHeader.getOpaque());
            sb.append("\"");
        }
        return sb.toString();
    }

    public static String getAuthorizationHeader(String username, String password, String realm2, String method, String uri2, WwwAuthenticateHeader wwwAuthParsedHeader) {
        String str = username;
        String str2 = password;
        String str3 = realm2;
        String str4 = method;
        String str5 = uri2;
        DigestAuth digestAuth = new DigestAuth(str, str2, str3, wwwAuthParsedHeader.getNonce(), str4, str5, wwwAuthParsedHeader.getAlgorithm(), wwwAuthParsedHeader.getQop().split(",")[0]);
        StringBuilder sb = new StringBuilder();
        sb.append(getAuthorizationHeader(digestAuth.getUsername(), digestAuth.getRealm(), digestAuth.getDigestUri(), digestAuth.getNonce(), digestAuth.getResp()));
        if (!TextUtils.isEmpty(digestAuth.getAlgorithm())) {
            sb.append(", algorithm=");
            sb.append(digestAuth.getAlgorithm());
        }
        if (!TextUtils.isEmpty(digestAuth.getQop())) {
            sb.append(", nc=");
            sb.append(digestAuth.getNC());
            sb.append(", qop=");
            sb.append(digestAuth.getQop());
        }
        if (!TextUtils.isEmpty(digestAuth.getCnonce())) {
            sb.append(", cnonce=\"");
            sb.append(digestAuth.getCnonce());
            sb.append("\"");
        }
        if (!TextUtils.isEmpty(wwwAuthParsedHeader.getOpaque())) {
            sb.append(", opaque=\"");
            sb.append(wwwAuthParsedHeader.getOpaque());
            sb.append("\"");
        }
        return sb.toString();
    }

    public static String getAuthorizationHeader(String username, String password, String realm2, String method, String uri2, String auts, WwwAuthenticateHeader wwwAuthParsedHeader) {
        String str = username;
        String str2 = password;
        String str3 = realm2;
        String str4 = method;
        String str5 = uri2;
        DigestAuth digestAuth = new DigestAuth(str, str2, str3, wwwAuthParsedHeader.getNonce(), str4, str5, wwwAuthParsedHeader.getAlgorithm(), wwwAuthParsedHeader.getQop().split(",")[0]);
        StringBuilder sb = new StringBuilder();
        sb.append(getAuthorizationHeader(digestAuth.getUsername(), digestAuth.getRealm(), digestAuth.getDigestUri(), digestAuth.getNonce(), digestAuth.getResp()));
        sb.append(", algorithm=");
        sb.append(digestAuth.getAlgorithm());
        sb.append(", auts=\"");
        sb.append(auts);
        sb.append("\"");
        if (!TextUtils.isEmpty(digestAuth.getQop())) {
            sb.append(", nc=");
            sb.append(digestAuth.getNC());
            sb.append(", qop=");
            sb.append(digestAuth.getQop());
        }
        if (!TextUtils.isEmpty(digestAuth.getCnonce())) {
            sb.append(", cnonce=\"");
            sb.append(digestAuth.getCnonce());
            sb.append("\"");
        }
        if (!TextUtils.isEmpty(wwwAuthParsedHeader.getOpaque())) {
            sb.append(", opaque=\"");
            sb.append(wwwAuthParsedHeader.getOpaque());
            sb.append("\"");
        }
        return sb.toString();
    }

    public static String getAuthorizationHeader(String username, String realm2, String uri2, String nonce2, String response2) {
        return "Digest username=\"" + username + "\", realm=\"" + realm2 + "\", uri=\"" + uri2 + "\", nonce=\"" + nonce2 + "\", response=\"" + response2 + "\"";
    }
}
