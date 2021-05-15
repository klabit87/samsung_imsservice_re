package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.aec.util.HttpClient;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import org.json.JSONObject;

public class HttpStore {
    private String mAppId = null;
    private final Context mContext;
    private String mEapChallenge = null;
    private String mEapChallengeResp = null;
    private String mHostName = null;
    private Map<String, List<String>> mHttpHeader = null;
    private Map<String, String> mHttpParam = null;
    private JSONObject mHttpPostData = null;
    private HttpClient.Response mHttpResponse = null;
    private String mHttpUrl = null;
    private Queue mHttpUrls = null;
    private Map<String, String> mParsedXml = null;
    private final int mPhoneId;
    private String mUserAgent = null;

    public HttpStore(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
    }

    public void clearHttpStore() {
        this.mAppId = null;
        this.mEapChallenge = null;
        this.mEapChallengeResp = null;
        this.mHostName = null;
        this.mHttpHeader = null;
        this.mHttpParam = null;
        this.mHttpPostData = null;
        this.mHttpResponse = null;
        this.mHttpUrl = null;
        this.mHttpUrls = null;
        this.mParsedXml = null;
        this.mUserAgent = null;
    }

    public String getAppId() {
        return this.mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public String getHttpUrl() {
        return this.mHttpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.mHttpUrl = httpUrl;
    }

    public Queue getHttpUrls() {
        return this.mHttpUrls;
    }

    public void setHttpUrls(Queue httpUrls) {
        this.mHttpUrls = httpUrls;
    }

    public String getHostName() {
        return this.mHostName;
    }

    public void setHostName(String hostName) {
        this.mHostName = hostName;
    }

    public Map<String, List<String>> getHttpHeaders() {
        return this.mHttpHeader;
    }

    public Map<String, String> getHttpParams() {
        return this.mHttpParam;
    }

    public void setHttpParam(String key, String value) throws Exception {
        if (key.equals("app")) {
            this.mHttpParam.put(key, value);
        } else {
            this.mHttpParam.put(key, URLEncoder.encode(value, "utf-8"));
        }
    }

    public JSONObject getHttpPostData() {
        return this.mHttpPostData;
    }

    public HttpClient.Response getHttpResponse() {
        return this.mHttpResponse;
    }

    public void setHttpResponse(HttpClient.Response response) {
        this.mHttpResponse = response;
    }

    public Map<String, String> getParsedXml() {
        return this.mParsedXml;
    }

    public void setParsedXml(Map<String, String> parsedXml) {
        this.mParsedXml = parsedXml;
    }

    public String getEapChallenge() {
        return this.mEapChallenge;
    }

    public void setEapChallenge(String eapChallenge) {
        this.mEapChallenge = eapChallenge;
    }

    public String getEapChallengeResp() {
        return this.mEapChallengeResp;
    }

    public void setEapChallengeResp(String eapChallengeResp) {
        this.mEapChallengeResp = eapChallengeResp;
    }

    public void setHttpPushParam(String notifAction, String notifToken) throws Exception {
        if (!TextUtils.isEmpty(notifAction) && !TextUtils.isEmpty(notifToken)) {
            setHttpParam("notif_action", notifAction);
            setHttpParam(AECNamespace.PramsName.NOTIF_TOKEN, notifToken);
        }
    }

    public String getUserAgent() {
        return this.mUserAgent;
    }

    public void setUserAgent(String entitlementVersion) {
        this.mUserAgent = String.format(AECNamespace.Template.USER_AGENT, new Object[]{entitlementVersion, AECNamespace.Build.TERMINAL_VENDOR, AECNamespace.Build.TERMINAL_SW_VERSION, AECNamespace.Build.ANDROID_OS_VERSION});
    }

    public void initHttpGetInfo(int version, String entitlementVersion) throws Exception {
        initHttpHeaders();
        this.mHttpParam = new HashMap();
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (tm == null || tm.getImei(this.mPhoneId) == null) {
            throw new IOException("initHttpGetInfo: TelephonyManager or imei not ready");
        }
        setHttpParam("terminal_vendor", AECNamespace.Build.TERMINAL_VENDOR);
        setHttpParam("terminal_model", AECNamespace.Build.TERMINAL_MODEL);
        setHttpParam("terminal_sw_version", AECNamespace.Build.TERMINAL_SW_VERSION);
        setHttpParam("entitlement_version", entitlementVersion);
        setHttpParam(AECNamespace.PramsName.TERMINAL_ID, tm.getImei(this.mPhoneId));
        setHttpParam("vers", Integer.toString(version));
        setHttpParam("app", getAppId());
    }

    public void initHttpPostInfo(String eapChallengeResp, List<String> setCookie) throws Exception {
        initHttpHeaders();
        this.mHttpPostData = new JSONObject();
        if (setCookie == null || setCookie.isEmpty()) {
            throw new IOException("initHttpPostInfo: empty cookie");
        }
        this.mHttpHeader.put(HttpController.HEADER_COOKIE, extractCookie(setCookie));
        if (!TextUtils.isEmpty(eapChallengeResp)) {
            this.mHttpHeader.put("Content-Type", Collections.singletonList("application/vnd.gsma.eap-relay.v1.0+json"));
            this.mHttpPostData.put("eap-relay-packet", eapChallengeResp);
            return;
        }
        throw new IOException("initHttpPostInfo: empty eap challenge response");
    }

    private void initHttpHeaders() {
        HashMap hashMap = new HashMap();
        this.mHttpHeader = hashMap;
        hashMap.put(HttpController.HEADER_HOST, Collections.singletonList(getHostName()));
        this.mHttpHeader.put("User-Agent", Collections.singletonList(getUserAgent()));
        this.mHttpHeader.put("Connection", Collections.singletonList("Keep-Alive"));
        this.mHttpHeader.put("Accept", Collections.singletonList("application/vnd.gsma.eap-relay.v1.0+json".concat(", ").concat(AECNamespace.HTTP_CONTENT_TYPE.XML)));
        this.mHttpHeader.put(HttpController.HEADER_CACHE_CONTROL, Collections.singletonList("max-age=0"));
        Locale currentLocale = Locale.getDefault();
        this.mHttpHeader.put("Accept-Language", Collections.singletonList(currentLocale.getLanguage().concat("-").concat(currentLocale.getCountry())));
    }

    private List<String> extractCookie(List<String> setCookie) {
        StringBuilder sb = new StringBuilder();
        for (String httpCookie : setCookie) {
            for (String cookieValue : httpCookie.split(";")) {
                String cookieValue2 = cookieValue.trim();
                if (sb.length() != 0) {
                    sb.append("; ");
                }
                sb.append(cookieValue2);
            }
        }
        return Collections.singletonList(sb.toString());
    }
}
