package com.sec.internal.ims.util;

import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.header.AuthorizationHeader;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.parser.WwwAuthHeaderParser;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.gba.BootstrappedSa;
import com.sec.internal.ims.gba.GbaBootstrapping;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.AkaAuth;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.net.URI;
import java.net.URISyntaxException;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpAuthGenerator {
    private static final String AKAV1_MD5 = "AKAv1-MD5";
    private static final String AKAV2_MD5 = "AKAv2-MD5";
    private static final String LOG_TAG = HttpAuthGenerator.class.getSimpleName();

    public static String generate(String challenge, String uri, String method, String user, String password) {
        String str = LOG_TAG;
        IMSLog.s(str, "generateAuthHeader: challenge= " + challenge + " uri=" + uri + " method=" + method);
        String str2 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("generateAuthHeader: user=");
        sb.append(user);
        sb.append(" password=");
        sb.append(password);
        IMSLog.s(str2, sb.toString());
        String[] element = challenge.split(" ");
        if (element.length < 2) {
            throw new IllegalArgumentException("challenge is not WWW-Authenticate");
        } else if (WwwAuthenticateHeader.HEADER_PARAM_DIGEST_SCHEME.equalsIgnoreCase(element[0])) {
            return generateDigestAuthHeader(challenge, uri, method, user, password);
        } else {
            if (WwwAuthenticateHeader.HEADER_PARAM_BASIC_SCHEME.equalsIgnoreCase(element[0])) {
                return generateBasicAuthHeader(user, password);
            }
            return null;
        }
    }

    public static String getEAPAkaChallengeResponse(int phoneId, String eapChallenge) {
        String akaResp = null;
        try {
            JSONObject jsonObject = new JSONObject(eapChallenge);
            if (jsonObject.has("eap-relay-packet")) {
                String eapMessage = jsonObject.getString("eap-relay-packet");
                ISimManager sm = SimManagerFactory.getSimManager();
                if (sm == null) {
                    return null;
                }
                String akaChallenge = StrUtil.bytesToHexString(Base64.decode(eapMessage.getBytes(), 2));
                String eapAkaResp = AKAEapAuthHelper.generateChallengeResponse(akaChallenge, sm.getIsimAuthentication(AKAEapAuthHelper.getNonce(akaChallenge)), AKAEapAuthHelper.composeRootNai(phoneId));
                if (!TextUtils.isEmpty(eapAkaResp)) {
                    JSONObject respJson = new JSONObject();
                    respJson.put("eap-relay-packet", eapAkaResp);
                    akaResp = respJson.toString();
                }
                String str = LOG_TAG;
                IMSLog.s(str, "handleEapAkaChallenge akaResp: " + akaResp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return akaResp;
    }

    public static String getAuthorizationHeader(int slotIndex, String requestUrl, String wwwAuthenticateHeader, String method) {
        return getAuthorizationHeader(slotIndex, requestUrl, wwwAuthenticateHeader, method, (String) null);
    }

    public static String getAuthorizationHeader(int slotIndex, String requestUrl, String wwwAuthenticateHeader, String method, String cipherSuite) {
        String url;
        String password;
        String password2;
        String str = wwwAuthenticateHeader;
        String str2 = method;
        IMnoStrategy mnoStrategy = getRcsStrategy(slotIndex);
        ImConfig imConfig = getImConfig(slotIndex);
        WwwAuthHeaderParser wwwAuthHeaderParser = new WwwAuthHeaderParser();
        boolean isFTwithGba = mnoStrategy != null && mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FT_WITH_GBA);
        WwwAuthenticateHeader wwwAuthParsedHeader = wwwAuthHeaderParser.parseHeaderValue(str);
        String realm = wwwAuthParsedHeader.getRealm();
        if (TextUtils.isEmpty(requestUrl)) {
            IMSLog.i(LOG_TAG, "getAuthorizationHeader: requestUrl is empty. get url from imConfig");
            url = imConfig.getFtHttpCsUri().toString();
        } else {
            url = requestUrl;
        }
        IMSLog.s(LOG_TAG, "url = " + url);
        String path = "/";
        try {
            URI naf = new URI(url);
            path = naf.getPath();
            if (naf.getQuery() != null) {
                path = path + "?" + naf.getQuery();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(path)) {
            path = "/";
        }
        IMSLog.s(LOG_TAG, "path = " + path);
        String response = "";
        if (isFTwithGba) {
            IMSLog.s(LOG_TAG, "FT with GBA is on");
            try {
                GbaBootstrapping gbaBootstrapping = GbaBootstrapping.getInstance(slotIndex);
                try {
                    BootstrappedSa bootstrappedSa = gbaBootstrapping.getBootstrappedSa(url, realm, cipherSuite, imConfig.isFtHttpTrustAllCerts());
                    GbaBootstrapping gbaBootstrapping2 = gbaBootstrapping;
                    response = generate(str, path, str2, bootstrappedSa.getBtid(), bootstrappedSa.getGbaKey());
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                    IMSLog.s(LOG_TAG, "response: " + response);
                    int i = slotIndex;
                    IMnoStrategy iMnoStrategy = mnoStrategy;
                    WwwAuthHeaderParser wwwAuthHeaderParser2 = wwwAuthHeaderParser;
                    return response;
                }
            } catch (Exception e3) {
                e = e3;
                String str3 = cipherSuite;
                e.printStackTrace();
                IMSLog.s(LOG_TAG, "response: " + response);
                int i2 = slotIndex;
                IMnoStrategy iMnoStrategy2 = mnoStrategy;
                WwwAuthHeaderParser wwwAuthHeaderParser22 = wwwAuthHeaderParser;
                return response;
            }
            IMSLog.s(LOG_TAG, "response: " + response);
            int i22 = slotIndex;
            IMnoStrategy iMnoStrategy22 = mnoStrategy;
            WwwAuthHeaderParser wwwAuthHeaderParser222 = wwwAuthHeaderParser;
            return response;
        }
        String str4 = cipherSuite;
        String password3 = imConfig.getFtHttpCsPwd();
        String algo = wwwAuthParsedHeader.getAlgorithm();
        if (algo != null) {
            password2 = password3;
            if (algo.toLowerCase().startsWith("aka")) {
                AkaAuth.AkaAuthenticationResponse akaResponse = AkaAuth.getAkaResponse(slotIndex, wwwAuthParsedHeader.getNonce());
                if (akaResponse != null) {
                    IMnoStrategy iMnoStrategy3 = mnoStrategy;
                    if (algo.equalsIgnoreCase(AKAV1_MD5)) {
                        password = akaResponse.getRes();
                        WwwAuthHeaderParser wwwAuthHeaderParser3 = wwwAuthHeaderParser;
                    } else if (algo.equalsIgnoreCase(AKAV2_MD5)) {
                        StringBuilder sb = new StringBuilder();
                        WwwAuthHeaderParser wwwAuthHeaderParser4 = wwwAuthHeaderParser;
                        sb.append(akaResponse.getRes());
                        sb.append(akaResponse.getAuthKey());
                        sb.append(akaResponse.getEncrKey());
                        password = sb.toString();
                    }
                    String response2 = generate(str, path, str2, imConfig.getFtHttpCsUser(), password);
                    IMSLog.s(LOG_TAG, "response: " + response2);
                    return response2;
                }
                WwwAuthHeaderParser wwwAuthHeaderParser5 = wwwAuthHeaderParser;
            } else {
                int i3 = slotIndex;
                IMnoStrategy iMnoStrategy4 = mnoStrategy;
                WwwAuthHeaderParser wwwAuthHeaderParser6 = wwwAuthHeaderParser;
            }
        } else {
            int i4 = slotIndex;
            password2 = password3;
            IMnoStrategy iMnoStrategy5 = mnoStrategy;
            WwwAuthHeaderParser wwwAuthHeaderParser7 = wwwAuthHeaderParser;
        }
        password = password2;
        String response22 = generate(str, path, str2, imConfig.getFtHttpCsUser(), password);
        IMSLog.s(LOG_TAG, "response: " + response22);
        return response22;
    }

    private static IMnoStrategy getRcsStrategy(int slotIndex) {
        return RcsPolicyManager.getRcsStrategy(slotIndex);
    }

    private static ImConfig getImConfig(int slotIndex) {
        return ImConfig.getInstance(slotIndex);
    }

    private static String generateDigestAuthHeader(String challenge, String uri, String method, String user, String password) {
        WwwAuthenticateHeader wwwAuthParsedHeader = new WwwAuthHeaderParser().parseHeaderValue(challenge);
        return AuthorizationHeader.getAuthorizationHeader(user, password, wwwAuthParsedHeader.getRealm(), method, uri, wwwAuthParsedHeader);
    }

    private static String generateBasicAuthHeader(String user, String password) {
        return "Basic " + Base64.encodeToString((user + ":" + password).getBytes(), 2);
    }
}
