package com.sec.internal.ims.config;

import android.content.Context;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SharedInfo {
    private final String LOG_TAG = SharedInfo.class.getSimpleName();
    private HashMap<String, String> mAKAParams = new HashMap<>();
    private String mClientPlatform = "";
    private String mClientVersion = "";
    private Map<String, List<String>> mHttpHeader = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    private Map<String, String> mHttpParam = new HashMap();
    private String mHttpPort = "80";
    private IHttpAdapter.Response mHttpResponse = null;
    private String mHttpUrl = null;
    private String mHttpsPort = "443";
    private int mInternal503ErrRetryCount = 0;
    private int mInternal511ErrRetryCount = 0;
    private int mInternalErrRetryCount = 0;
    private boolean mIsRcsByUser = false;
    private HashMap<String, String> mOidcParams = new HashMap<>();
    private String mOtp = null;
    private Map<String, String> mParsedXml = null;
    private String mRcsProfile = "";
    private String mRcsVersion = "";
    private ISimManager mSm;
    private String mUserImsi = "";
    private String mUserMethod = "GET";
    private String mUserMsisdn = "";
    private String mXml = null;

    public SharedInfo(Context context, ISimManager sm, String rcsProfile, String rcsVersion, String clientPlatform, String clientVersion) {
        this.mSm = sm;
        this.mRcsProfile = rcsProfile;
        this.mRcsVersion = rcsVersion;
        this.mClientPlatform = clientPlatform;
        this.mClientVersion = clientVersion;
        String str = this.LOG_TAG;
        Log.i(str, "rcsProfile: " + rcsProfile + " rcsVersion: " + rcsVersion + " clientVersion: " + clientVersion);
    }

    public void setHttpClean() {
        this.mHttpHeader = getInitHttpHeaders();
        this.mHttpParam = getInitHttpParams();
    }

    public void setHttpDefault() {
        if (getUrl() != null && getUrl().contains("https://")) {
            Log.i(this.LOG_TAG, "change https -> http");
            setUrl(getUrl().replace("https://", "http://"));
        }
        this.mHttpHeader = getInitHttpHeaders();
        this.mHttpParam = getInitHttpParams();
    }

    public void setHttpCMCC() {
        if (getUrl() != null && getUrl().contains("https://")) {
            Log.i(this.LOG_TAG, "change https -> http");
            setUrl(getUrl().replace("https://", "http://"));
        }
        if (getUrl() != null) {
            String[] ipStrs = getUrl().replaceFirst("https?://", "").split(":");
            if (ipStrs.length > 2) {
                this.mHttpPort = ipStrs[1];
                this.mHttpsPort = ipStrs[2];
                String url = getUrl();
                setUrl(url.replace(":" + ipStrs[2], ""));
            }
        }
        if (getUrl() != null && getUrl().contains(":443")) {
            Log.i(this.LOG_TAG, "change port 443 -> 80");
            setUrl(getUrl().replace(":443", ":80"));
        }
        this.mHttpHeader = getCMCCInitHttpHeaders();
        this.mHttpParam = getInitHttpParams();
    }

    public void setHttpSPR() {
        if (getUrl() != null && getUrl().contains("https://")) {
            Log.i(this.LOG_TAG, "change https -> http");
            setUrl(getUrl().replace("https://", "http://"));
        }
        if (getUrl() != null && getUrl().contains("http://")) {
            Log.i(this.LOG_TAG, "change http -> enriched header");
            setUrl(getUrl().replace("http://", "http://oap7.sprintpcs.com/http://"));
        }
        this.mHttpHeader = getInitHttpHeaders();
        this.mHttpParam = getInitHttpParams();
    }

    public void resetHttpSPR() {
        if (getUrl() != null && getUrl().contains("https://")) {
            Log.i(this.LOG_TAG, "change https -> http");
            setUrl(getUrl().replace("https://", "http://"));
        }
        if (getUrl() != null && getUrl().contains("http://")) {
            Log.i(this.LOG_TAG, "change enriched header -> http");
            setUrl(getUrl().replace("http://oap7.sprintpcs.com/http://", "http://"));
        }
        this.mHttpHeader = getInitHttpHeaders();
        this.mHttpParam = getInitHttpParams();
    }

    public void setHttpsDefault() {
        if (getUrl() != null && getUrl().contains("http://")) {
            Log.i(this.LOG_TAG, "change http -> https");
            setUrl(getUrl().replace("http://", "https://"));
        }
        this.mHttpHeader = getInitHttpsHeaders();
        this.mHttpParam = getInitHttpsParams();
    }

    public void setHttpsWithPreviousCookies() {
        List<String> lastCookies = getHttpHeaders().get(HttpController.HEADER_COOKIE);
        setHttpsDefault();
        if (lastCookies != null) {
            addHttpHeader(HttpController.HEADER_COOKIE, lastCookies);
        }
    }

    public void setHttpsCMCC() {
        if (getUrl() != null) {
            String[] ipStrs = getUrl().replaceFirst("https?://", "").split(":");
            if (ipStrs.length > 2) {
                this.mHttpPort = ipStrs[1];
                this.mHttpsPort = ipStrs[2];
                setUrl(getUrl().replace(":" + ipStrs[1], ""));
            } else if (ipStrs.length == 2) {
                setUrl(getUrl().replace(":" + ipStrs[1], ":" + this.mHttpsPort));
            }
        }
        if (getUrl() != null && getUrl().contains("http://")) {
            Log.i(this.LOG_TAG, "change http -> https");
            setUrl(getUrl().replace("http://", "https://"));
        }
        this.mHttpHeader = getCMCCInitHttpsHeaders();
        this.mHttpParam = getInitHttpsParams();
    }

    public void setHttpsSPR() {
        if (getUrl() != null && getUrl().contains("http://")) {
            Log.i(this.LOG_TAG, "change http -> https");
            setUrl(getUrl().replace("http://", "https://"));
        }
        if (getUrl() != null && getUrl().contains("https://oap7.sprintpcs.com/https://")) {
            Log.i(this.LOG_TAG, "change enriched header -> https");
            setUrl(getUrl().replace("https://oap7.sprintpcs.com/https://", "https://"));
        }
        this.mHttpHeader = getInitHttpsHeaders();
        this.mHttpParam = getInitHttpsParamsSPR();
    }

    public void changeConfigProxyUriForHttp() {
        if (getUrl() != null && getUrl().contains("/cookie/")) {
            Log.i(this.LOG_TAG, "ConfigProxyUri: change cookie -> conf");
            setUrl(getUrl().replace("cookie", "conf"));
        }
    }

    public void setHttpProxyDefault() {
        if (getUrl() != null && getUrl().contains("https://")) {
            Log.i(this.LOG_TAG, "change https -> http");
            setUrl(getUrl().replace("https://", "http://"));
        }
        this.mHttpHeader = getInitHttpsHeaders();
        this.mHttpParam = getInitHttpsParams();
    }

    public String getUrl() {
        return this.mHttpUrl;
    }

    public void setUrl(String url) {
        this.mHttpUrl = url;
    }

    public Map<String, List<String>> getHttpHeaders() {
        return this.mHttpHeader;
    }

    public void addHttpHeader(String field, List<String> values) {
        this.mHttpHeader.put(field, values);
    }

    public Map<String, String> getHttpParams() {
        return this.mHttpParam;
    }

    public void addHttpParam(String key, String value) {
        this.mHttpParam.put(key, value);
    }

    public IHttpAdapter.Response getHttpResponse() {
        return this.mHttpResponse;
    }

    public void setHttpResponse(IHttpAdapter.Response response) {
        this.mHttpResponse = response;
    }

    public String getXml() {
        return this.mXml;
    }

    public void setXml(String xml) {
        this.mXml = xml;
    }

    public Map<String, String> getParsedXml() {
        return this.mParsedXml;
    }

    public void setParsedXml(Map<String, String> parsedXml) {
        this.mParsedXml = parsedXml;
    }

    public String getOtp() {
        return this.mOtp;
    }

    public void setOtp(String otp) {
        this.mOtp = otp;
    }

    public String getUserMsisdn() {
        return this.mUserMsisdn;
    }

    public void setUserMsisdn(String msisdn) {
        this.mUserMsisdn = msisdn;
    }

    public String getUserMethod() {
        return this.mUserMethod;
    }

    public void setUserMethod(String method) {
        this.mUserMethod = method;
    }

    public void setUserImsi(String mimsi) {
        this.mUserImsi = mimsi;
        String str = this.LOG_TAG;
        Log.i(str, "setUserImsi:" + IMSLog.checker(this.mUserImsi));
    }

    private Map<String, List<String>> getInitHttpHeaders() {
        Locale currentLocale = Locale.getDefault();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpController.HEADER_HOST, Arrays.asList(new String[]{getHost(getUrl())}));
        headers.put("User-Agent", Arrays.asList(new String[]{getUserAgent()}));
        headers.put("Connection", Arrays.asList(new String[]{"Keep-Alive"}));
        if (currentLocale != null) {
            headers.put("Accept-Language", Arrays.asList(new String[]{currentLocale.getLanguage().concat("-").concat(currentLocale.getCountry())}));
        }
        headers.put(HttpController.HEADER_CACHE_CONTROL, Arrays.asList(new String[]{"max-age=0"}));
        return headers;
    }

    private Map<String, List<String>> getCMCCInitHttpHeaders() {
        Locale currentLocale = Locale.getDefault();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpController.HEADER_HOST, Arrays.asList(new String[]{getHost(getUrl())}));
        headers.put("User-Agent", Arrays.asList(new String[]{getUserAgent()}));
        headers.put("Connection", Arrays.asList(new String[]{"Keep-Alive"}));
        if (currentLocale != null) {
            headers.put("Accept-Language", Arrays.asList(new String[]{currentLocale.getLanguage().concat("-").concat(currentLocale.getCountry())}));
        }
        headers.put(HttpController.HEADER_CACHE_CONTROL, Arrays.asList(new String[]{"max-age=0"}));
        return headers;
    }

    private Map<String, String> getInitHttpParams() {
        return new HashMap<>();
    }

    private Map<String, List<String>> getInitHttpsHeaders() {
        Locale currentLocale = Locale.getDefault();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpController.HEADER_HOST, Arrays.asList(new String[]{getHost(getUrl())}));
        headers.put("User-Agent", Arrays.asList(new String[]{getUserAgent()}));
        headers.put("Connection", Arrays.asList(new String[]{"Keep-Alive"}));
        if (currentLocale != null) {
            headers.put("Accept-Language", Arrays.asList(new String[]{currentLocale.getLanguage().concat("-").concat(currentLocale.getCountry())}));
        }
        headers.put(HttpController.HEADER_CACHE_CONTROL, Arrays.asList(new String[]{"max-age=0"}));
        return headers;
    }

    private Map<String, List<String>> getCMCCInitHttpsHeaders() {
        Locale currentLocale = Locale.getDefault();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpController.HEADER_HOST, Arrays.asList(new String[]{getHost(getUrl())}));
        headers.put("User-Agent", Arrays.asList(new String[]{getUserAgent()}));
        headers.put("Connection", Arrays.asList(new String[]{"Keep-Alive"}));
        if (currentLocale != null) {
            headers.put("Accept-Language", Arrays.asList(new String[]{currentLocale.getLanguage().concat("-").concat(currentLocale.getCountry())}));
        }
        return headers;
    }

    private Map<String, String> getInitHttpsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("vers", "0");
        params.put("rcs_version", this.mRcsVersion);
        params.put(ConfigConstants.PNAME.RCS_PROFILE, this.mRcsProfile);
        params.put(ConfigConstants.PNAME.CLIENT_VENDOR, ConfigConstants.PVALUE.CLIENT_VENDOR);
        params.put(ConfigConstants.PNAME.CLIENT_VERSION, this.mClientPlatform + this.mClientVersion);
        return params;
    }

    private Map<String, String> getInitHttpsParamsSPR() {
        return new HashMap<>();
    }

    private String getHost(String url) {
        if (url == null) {
            return "";
        }
        String host = url.replaceFirst("https?://", "");
        int indexOfSlash = host.indexOf(47);
        if (indexOfSlash > 0) {
            return host.substring(0, indexOfSlash);
        }
        return host;
    }

    private String getUserAgent() {
        String version;
        String str;
        String str2;
        String model = ConfigContract.BUILD.getTerminalModel();
        String version2 = ConfigContract.BUILD.getTerminalSwVersion();
        ISimManager iSimManager = this.mSm;
        if (iSimManager == null) {
            Log.i(this.LOG_TAG, "getUserAgent: ISimManager is null, return");
            return "";
        }
        Mno mno = iSimManager.getSimMno();
        if (Mno.TMOBILE.equals(mno) || Mno.SFR.equals(mno) || Mno.TMOBILE_CZ.equals(mno)) {
            if (version2.length() > 8) {
                str = version2.substring(version2.length() - 8, version2.length());
            } else {
                str = version2;
            }
            version = str;
        } else {
            if (version2.length() > 3) {
                str2 = version2.substring(version2.length() - 3, version2.length());
            } else {
                str2 = version2;
            }
            version = str2;
        }
        return ConfigUtil.getFormattedUserAgent(mno, model, version, this.mClientVersion);
    }

    public void parseOidcParams(String url) {
        this.mOidcParams.clear();
        if (url != null) {
            String[] params = url.split("\\?")[1].split("&");
            for (int i = 0; i < params.length; i++) {
                this.mOidcParams.put(params[i].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)[0], params[i].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)[1]);
            }
        }
    }

    public void parseAkaParams(String url) {
        Log.d(this.LOG_TAG, "AKA (Digest) Params parsing");
        if (url != null) {
            String[] params = url.split("\\s+")[1].split(",");
            for (int i = 0; i < params.length; i++) {
                this.mAKAParams.put(params[i].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)[0], params[i].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)[1]);
            }
        }
    }

    public HashMap<String, String> getOidcParams() {
        return this.mOidcParams;
    }

    public HashMap<String, String> getAKAParams() {
        return this.mAKAParams;
    }

    public int getInternalErrRetryCount() {
        return this.mInternalErrRetryCount;
    }

    public void setInternalErrRetryCount(int count) {
        this.mInternalErrRetryCount = count;
    }

    public boolean isRcsByUser() {
        return this.mIsRcsByUser;
    }

    public void setRcsByUser(boolean isRcsByUser) {
        this.mIsRcsByUser = isRcsByUser;
    }

    public int getInternal503ErrRetryCount() {
        return this.mInternal503ErrRetryCount;
    }

    public void setInternal503ErrRetryCount(int count) {
        this.mInternal503ErrRetryCount = count;
    }

    public int getInternal511ErrRetryCount() {
        return this.mInternal511ErrRetryCount;
    }

    public void setInternal511ErrRetryCount(int count) {
        this.mInternal511ErrRetryCount = count;
    }
}
