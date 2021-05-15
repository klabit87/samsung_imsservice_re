package com.sec.internal.ims.util;

import android.net.Network;
import android.util.Log;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.util.FileTaskUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.log.IMSLog;

public class OpenIdAuth {
    private static final String LOG_TAG = OpenIdAuth.class.getSimpleName();

    public static class OpenIdRequest {
        /* access modifiers changed from: private */
        public final boolean mIsTrustAllCert;
        /* access modifiers changed from: private */
        public final Network mNetwork;
        /* access modifiers changed from: private */
        public final int mPhoneId;
        /* access modifiers changed from: private */
        public final String mUrl;
        /* access modifiers changed from: private */
        public final String mUserAgent;

        public OpenIdRequest(int phoneId, String url, Network network, String userAgent, boolean isTrustAllCert) {
            this.mPhoneId = phoneId;
            this.mUrl = url;
            this.mNetwork = network;
            this.mUserAgent = userAgent;
            this.mIsTrustAllCert = isTrustAllCert;
        }
    }

    public static String sendAuthRequest(OpenIdRequest req) throws HttpRequest.HttpRequestException {
        String akaResponse;
        HttpRequest httpRequest = HttpRequest.get(req.mUrl);
        setDefaultHeaders(httpRequest, req);
        int response = httpRequest.code();
        if (response == 200) {
            Log.d(LOG_TAG, "200 OK received");
            if (httpRequest.header("Content-Type").contains("application/vnd.gsma.eap-relay.v1.0+json")) {
                httpRequest.disconnect();
                String body = httpRequest.body();
                if (!(body == null || (akaResponse = HttpAuthGenerator.getEAPAkaChallengeResponse(req.mPhoneId, body)) == null)) {
                    httpRequest = HttpRequest.post(req.mUrl);
                    setDefaultHeaders(httpRequest, req);
                    httpRequest.send((CharSequence) akaResponse);
                    response = httpRequest.code();
                }
            }
        } else if (response == 302) {
            Log.d(LOG_TAG, "Received 302");
        } else if (response != 401) {
            String str = LOG_TAG;
            IMSLog.s(str, "Receive HTTP response " + httpRequest.message() + " neither 302 nor UNAUTHORIZED");
        } else {
            Log.d(LOG_TAG, "Receive 401 Unauthorized, attempt to generate response");
            httpRequest.disconnect();
            String challenge = httpRequest.wwwAuthenticate();
            String str2 = LOG_TAG;
            IMSLog.s(str2, "challenge: " + challenge);
            String authResponse = HttpAuthGenerator.getAuthorizationHeader(req.mPhoneId, req.mUrl, challenge, "GET", httpRequest.getCipherSuite());
            httpRequest = HttpRequest.get(req.mUrl);
            setDefaultHeaders(httpRequest, req);
            httpRequest.authorization(authResponse);
            response = httpRequest.code();
        }
        if (response == 302) {
            return httpRequest.header("Location");
        }
        String str3 = LOG_TAG;
        Log.d(str3, "Did not receive 302 after authentication, received : " + response);
        return null;
    }

    private static void setDefaultHeaders(HttpRequest httpRequest, OpenIdRequest request) {
        httpRequest.setParams(request.mNetwork, false, 10000, FileTaskUtil.READ_DATA_TIMEOUT, request.mUserAgent);
        if (request.mIsTrustAllCert) {
            httpRequest.trustAllCerts().trustAllHosts();
        }
        if (RcsPolicyManager.getRcsStrategy(request.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.IS_EAP_SUPPORTED)) {
            httpRequest.header("Accept", "application/vnd.gsma.eap-relay.v1.0+json");
        }
    }
}
