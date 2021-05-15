package com.sec.internal.ims.util.httpclient;

import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.helper.header.AuthorizationHeader;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.DigestAuth;
import com.sec.internal.helper.httpclient.DnsController;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.httpclient.HttpResponseUtils;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.parser.AuthInfoHeaderParser;
import com.sec.internal.helper.parser.WwwAuthHeaderParser;
import com.sec.internal.ims.gba.BsfResponse;
import com.sec.internal.ims.gba.GbaUtility;
import com.sec.internal.ims.gba.params.GbaData;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.gba.IGbaCallback;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;

public class GbaHttpController {
    /* access modifiers changed from: private */
    public static final String TAG = GbaHttpController.class.getSimpleName();
    private static volatile GbaHttpController sInstance = new GbaHttpController();
    /* access modifiers changed from: private */
    public IGbaServiceModule mGbaServiceModule;
    /* access modifiers changed from: private */
    public HashMap<String, LastAuthInfo> mLastAuthInfoMap;
    HttpRequestParams mNafRequestParams;

    private static class LastAuthInfo {
        public String LifeTime;
        public String btid;
        public DigestAuth digestAuth;
        public String etag;
        public String gbaKey;
        public HttpResponseParams lastNafResult;
        public String nextNonce;

        private LastAuthInfo() {
            this.btid = null;
            this.gbaKey = null;
            this.lastNafResult = null;
            this.digestAuth = null;
            this.nextNonce = null;
            this.etag = null;
            this.LifeTime = null;
        }
    }

    private GbaHttpController() {
        this.mGbaServiceModule = null;
        this.mLastAuthInfoMap = null;
        this.mNafRequestParams = null;
        this.mLastAuthInfoMap = new HashMap<>();
    }

    public static GbaHttpController getInstance() {
        return sInstance;
    }

    public void clearLastAuthInfo() {
        IMSLog.d(TAG, "clearLastAuthInfo()");
        this.mLastAuthInfoMap.clear();
        ImsRegistry.getGbaService().initGbaAccessibleObj();
    }

    public void execute(final HttpRequestParams requestParams) {
        Date keyLifeTime;
        this.mNafRequestParams = requestParams;
        LastAuthInfo lastAuthInfo = getLastAuthInfo(requestParams.getUrl(), requestParams.getPhoneId());
        if (!(lastAuthInfo == null || lastAuthInfo.btid == null || lastAuthInfo.LifeTime == null)) {
            try {
                URL tUrl = new URL(requestParams.getUrl());
                IMSLog.d(TAG, "execute(): send exist BTID and gbaKey.");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                try {
                    keyLifeTime = sdf.parse(lastAuthInfo.LifeTime);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                    lastAuthInfo.btid = null;
                    lastAuthInfo.gbaKey = null;
                    lastAuthInfo.lastNafResult = null;
                    lastAuthInfo.digestAuth = null;
                    lastAuthInfo.nextNonce = null;
                    lastAuthInfo.LifeTime = null;
                    keyLifeTime = null;
                }
                if (isKeyExpired(keyLifeTime) || lastAuthInfo.lastNafResult == null) {
                    IMSLog.d(TAG, "Btid LifeTime expired");
                    lastAuthInfo.LifeTime = null;
                } else {
                    sendRequestWithAuthorization(tUrl, requestParams, lastAuthInfo.lastNafResult, lastAuthInfo.btid, lastAuthInfo.gbaKey, false);
                    return;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }
        }
        Map<String, String> requestHeader = requestParams.getHeaders();
        HttpRequestParams nafRequestParams = makeHttpRequestParams(requestParams.getMethod(), requestParams.getUrl(), requestHeader, new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                List<String> wwwAuthHeaders;
                final HttpResponseParams httpResponseParams = result;
                if (httpResponseParams == null) {
                    IMSLog.e(GbaHttpController.TAG, "execute(): onComplete: response build failure");
                    requestParams.getCallback().onFail(new IOException("okhttp response build failure"));
                    return;
                }
                GbaHttpController.this.loggingHttpMessage(result.toString(), 1);
                int statusCode = result.getStatusCode();
                IMSLog.c(LogClass.UT_HTTP, Integer.toString(requestParams.getPhoneId()) + ",<," + Integer.toString(statusCode));
                if (statusCode != 401 || !GbaHttpController.this.useGba(requestParams)) {
                    IMSLog.i(GbaHttpController.TAG, "NO GBA process");
                    requestParams.getCallback().onComplete(httpResponseParams);
                } else if (GbaHttpController.isNeedCSFB(401, requestParams.getPhoneId())) {
                    IMSLog.i(GbaHttpController.TAG, "Special case: TIM operator requires CSFB for 401.");
                    httpResponseParams.setStatusCode(403);
                    requestParams.getCallback().onComplete(httpResponseParams);
                } else {
                    GbaHttpController.this.storeLastAuthInfo(httpResponseParams, requestParams);
                    try {
                        final URL url = new URL(requestParams.getUrl());
                        Map<String, List<String>> responseHeader = result.getHeaders();
                        List<String> wwwAuthHeaders2 = responseHeader.get("WWW-Authenticate");
                        if (wwwAuthHeaders2 == null || wwwAuthHeaders2.size() == 0) {
                            wwwAuthHeaders = responseHeader.get("WWW-Authenticate".toLowerCase());
                        } else {
                            wwwAuthHeaders = wwwAuthHeaders2;
                        }
                        if (wwwAuthHeaders == null || wwwAuthHeaders.size() == 0) {
                            IMSLog.e(GbaHttpController.TAG, "execute(): onComplete: missing header: WWW-Authenticate");
                            requestParams.getCallback().onComplete(httpResponseParams);
                            return;
                        }
                        String realm = new WwwAuthHeaderParser().parseHeaderValue(wwwAuthHeaders.get(0)).getRealm();
                        if (realm.contains("3GPP-bootstrapping")) {
                            if (requestParams.getIpVersion() > 0) {
                                DnsController dns = (DnsController) requestParams.getDns();
                                dns.setNaf(false);
                                requestParams.setDns(dns);
                            }
                            IGbaServiceModule unused = GbaHttpController.this.mGbaServiceModule = ImsRegistry.getGbaService();
                            GbaHttpController.this.mGbaServiceModule.getBtidAndGbaKey(requestParams, realm, httpResponseParams, new IGbaCallback() {
                                public void onComplete(String btid, String gbaKey, boolean gbaUicc, HttpResponseParams gbaResult) {
                                    if (btid == null || gbaKey == null) {
                                        IMSLog.e(GbaHttpController.TAG, "execute:  cannot get username and password for GBA");
                                        requestParams.getCallback().onComplete(gbaResult);
                                        return;
                                    }
                                    GbaHttpController.this.sendRequestWithAuthorization(url, requestParams, httpResponseParams, btid, gbaKey, gbaUicc);
                                }

                                public void onFail(IOException arg1) {
                                    requestParams.getCallback().onFail(arg1);
                                }
                            });
                            return;
                        }
                        IMSLog.i(GbaHttpController.TAG, "HTTP digest without GBA");
                        URL url2 = url;
                        String str = realm;
                        GbaHttpController.this.sendRequestWithAuthorization(url2, requestParams, result, requestParams.getUserName(), requestParams.getPassword(), false);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onFail(IOException arg1) {
                IMSLog.c(LogClass.UT_ERROR_HANDLE, Integer.toString(requestParams.getPhoneId()) + "," + arg1.getMessage());
                requestParams.getCallback().onFail(arg1);
            }
        }, requestParams);
        if (requestParams.getPostBody() != null) {
            nafRequestParams.setPostBody(requestParams.getPostBody());
        }
        IMSLog.c(LogClass.UT_HTTP, Integer.toString(nafRequestParams.getPhoneId()) + ",>," + nafRequestParams.getMethodString());
        HttpController.getInstance().execute(nafRequestParams);
        loggingHttpMessage(nafRequestParams.toString(), 0);
    }

    /* access modifiers changed from: private */
    public void sendRequestWithAuthorization(URL url, HttpRequestParams requestParams, HttpResponseParams result, String username, String password, boolean gbaUicc) {
        List<String> wwwAuthHeaders;
        String realm;
        String str;
        String fullUrl;
        String str2;
        IMSLog.d(TAG, "GBA: sendRequestWithAuthorization()");
        Map<String, List<String>> responseHeader = result.getHeaders();
        List<String> wwwAuthHeaders2 = responseHeader.get("WWW-Authenticate");
        if (wwwAuthHeaders2 == null || wwwAuthHeaders2.size() == 0) {
            wwwAuthHeaders = responseHeader.get("WWW-Authenticate".toLowerCase());
        } else {
            wwwAuthHeaders = wwwAuthHeaders2;
        }
        if (wwwAuthHeaders == null) {
            HttpRequestParams httpRequestParams = requestParams;
        } else if (wwwAuthHeaders.size() == 0) {
            HttpRequestParams httpRequestParams2 = requestParams;
        } else {
            WwwAuthenticateHeader wwwAuthParsedHeader = new WwwAuthHeaderParser().parseHeaderValue(wwwAuthHeaders.get(0));
            if (wwwAuthParsedHeader.getRealm() == null) {
                HttpRequestParams httpRequestParams3 = requestParams;
            } else if (TextUtils.isEmpty(wwwAuthParsedHeader.getQop())) {
                HttpRequestParams httpRequestParams4 = requestParams;
            } else {
                String[] realms = wwwAuthParsedHeader.getRealm().split(";");
                String fqdn = wwwAuthParsedHeader.getRealm();
                int length = realms.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        realm = "";
                        break;
                    }
                    String val = realms[i];
                    if (!val.contains("uicc") || !gbaUicc) {
                        if (!val.contains("uicc") && !gbaUicc) {
                            realm = val;
                            break;
                        }
                        i++;
                    } else {
                        realm = val;
                        break;
                    }
                }
                LastAuthInfo lastAuthInfo = getLastAuthInfo(url.toString(), requestParams.getPhoneId());
                DigestAuth digestAuth = lastAuthInfo.digestAuth;
                String[] qop = wwwAuthParsedHeader.getQop().split(",");
                if (lastAuthInfo.nextNonce != null) {
                    wwwAuthParsedHeader.setNonce(lastAuthInfo.nextNonce);
                }
                if (requestParams.getMethod() == HttpRequestParams.Method.PUT) {
                    if (url.getQuery() != null) {
                        fullUrl = url.getPath() + "?" + url.getQuery();
                    } else {
                        fullUrl = url.getPath();
                    }
                    String nonce = wwwAuthParsedHeader.getNonce();
                    String methodString = requestParams.getMethodString();
                    if (fullUrl.isEmpty()) {
                        str2 = "/";
                    } else {
                        str2 = fullUrl;
                    }
                    digestAuth.setDigestAuth(username, password, realm, nonce, methodString, str2, wwwAuthParsedHeader.getAlgorithm(), qop[0], new String(requestParams.getPostBody().getData()));
                } else {
                    String nonce2 = wwwAuthParsedHeader.getNonce();
                    String methodString2 = requestParams.getMethodString();
                    if (url.getPath().isEmpty()) {
                        str = "/";
                    } else {
                        str = url.getPath();
                    }
                    digestAuth.setDigestAuth(username, password, realm, nonce2, methodString2, str, wwwAuthParsedHeader.getAlgorithm(), qop[0]);
                }
                String authHeader = AuthorizationHeader.getAuthorizationHeader(digestAuth, wwwAuthParsedHeader);
                Map<String, String> nafRequestHeader = new HashMap<>();
                nafRequestHeader.put(HttpController.HEADER_HOST, requestParams.getHeaders().get(HttpController.HEADER_HOST));
                nafRequestHeader.put("User-Agent", requestParams.getHeaders().get("User-Agent"));
                nafRequestHeader.put("Authorization", authHeader);
                nafRequestHeader.put("Accept", "*/*");
                nafRequestHeader.put("Accept-Encoding", getAcceptEncoding(requestParams.getPhoneId()));
                if (requestParams.getMethod() == HttpRequestParams.Method.PUT) {
                    nafRequestHeader.put("If-Match", lastAuthInfo.etag);
                    nafRequestHeader.put("Content-Type", requestParams.getHeaders().get("Content-Type"));
                }
                if (!TextUtils.isEmpty(requestParams.getHeaders().get(HttpController.HEADER_X_TMUS_IMEI))) {
                    nafRequestHeader.put(HttpController.HEADER_X_TMUS_IMEI, requestParams.getHeaders().get(HttpController.HEADER_X_TMUS_IMEI));
                }
                if (!TextUtils.isEmpty(requestParams.getHeaders().get("X-3GPP-Intended-Identity"))) {
                    nafRequestHeader.put("X-3GPP-Intended-Identity", requestParams.getHeaders().get("X-3GPP-Intended-Identity"));
                }
                final HttpRequestParams httpRequestParams5 = requestParams;
                Map<String, String> nafRequestHeader2 = nafRequestHeader;
                final URL url2 = url;
                String str3 = authHeader;
                final String authHeader2 = username;
                DigestAuth digestAuth2 = digestAuth;
                final String str4 = password;
                LastAuthInfo lastAuthInfo2 = lastAuthInfo;
                final String str5 = fqdn;
                HttpRequestParams nafRequestParams = makeHttpRequestParams(requestParams.getMethod(), requestParams.getUrl(), nafRequestHeader2, new HttpRequestParams.HttpRequestCallback() {
                    public void onComplete(HttpResponseParams result) {
                        List<String> wwwAuthHeaders;
                        final HttpResponseParams httpResponseParams = result;
                        if (httpResponseParams == null) {
                            IMSLog.e(GbaHttpController.TAG, "onComplete: the response of 2nd time naf request build failure");
                            return;
                        }
                        GbaHttpController.this.loggingHttpMessage(result.toString(), 1);
                        int statusCode = result.getStatusCode();
                        IMSLog.c(LogClass.UT_HTTP, Integer.toString(httpRequestParams5.getPhoneId()) + ",<," + Integer.toString(statusCode));
                        if (statusCode == 200 || statusCode == 201) {
                        } else if (statusCode == 202) {
                            int i = statusCode;
                        } else if (statusCode == 401) {
                            LastAuthInfo lastAuthInfo = GbaHttpController.this.getLastAuthInfo(url2.toString(), httpRequestParams5.getPhoneId());
                            if (lastAuthInfo != null) {
                                lastAuthInfo.nextNonce = null;
                                lastAuthInfo.lastNafResult = httpResponseParams;
                                lastAuthInfo.digestAuth = new DigestAuth();
                            }
                            Map<String, List<String>> responseHeader = result.getHeaders();
                            List<String> wwwAuthHeaders2 = responseHeader.get("WWW-Authenticate");
                            if (wwwAuthHeaders2 == null || wwwAuthHeaders2.size() == 0) {
                                wwwAuthHeaders = responseHeader.get("WWW-Authenticate".toLowerCase());
                            } else {
                                wwwAuthHeaders = wwwAuthHeaders2;
                            }
                            if (wwwAuthHeaders == null) {
                            } else if (wwwAuthHeaders.size() == 0) {
                                int i2 = statusCode;
                            } else {
                                WwwAuthenticateHeader wwwAuthParsedHeader = new WwwAuthHeaderParser().parseHeaderValue(wwwAuthHeaders.get(0));
                                String realm = wwwAuthParsedHeader.getRealm();
                                if (wwwAuthParsedHeader.isStale()) {
                                    IMSLog.d(GbaHttpController.TAG, "Stale is true. Reuse same username..");
                                    int i3 = statusCode;
                                    String str = realm;
                                    GbaHttpController.this.sendRequestWithAuthorization(url2, httpRequestParams5, result, authHeader2, str4, false);
                                    return;
                                }
                                String realm2 = realm;
                                if (realm2.contains("3GPP-bootstrapping")) {
                                    IMSLog.d(GbaHttpController.TAG, "Retry GBA authentication...");
                                    if (httpRequestParams5.getIpVersion() > 0) {
                                        DnsController dns = (DnsController) httpRequestParams5.getDns();
                                        dns.setNaf(false);
                                        httpRequestParams5.setDns(dns);
                                    }
                                    IMSLog.d(GbaHttpController.TAG, "onComplete: 401 Unauthorized. reset GbaKey");
                                    GbaHttpController.this.mGbaServiceModule.resetGbaKey(realm2, httpRequestParams5.getPhoneId());
                                    GbaHttpController.this.mGbaServiceModule.getBtidAndGbaKey(httpRequestParams5, realm2, httpResponseParams, new IGbaCallback() {
                                        public void onComplete(String btid, String gbaKey, boolean gbaUicc, HttpResponseParams gbaResult) {
                                            if (btid == null || gbaKey == null) {
                                                IMSLog.e(GbaHttpController.TAG, "sendRequestWithAuthorization:  cannot get username and password for GBA");
                                                httpRequestParams5.getCallback().onComplete(gbaResult);
                                                return;
                                            }
                                            IMSLog.c(LogClass.UT_HTTP, Integer.toString(httpRequestParams5.getPhoneId()) + ",<," + Integer.toString(gbaResult.getStatusCode()));
                                            GbaHttpController.this.sendRequestWithAuthorization(url2, httpRequestParams5, httpResponseParams, btid, gbaKey, gbaUicc);
                                        }

                                        public void onFail(IOException arg1) {
                                            IMSLog.c(LogClass.UT_ERROR_HANDLE, Integer.toString(httpRequestParams5.getPhoneId()) + "," + arg1.getMessage());
                                            httpRequestParams5.getCallback().onFail(arg1);
                                        }
                                    });
                                    IMSLog.d(GbaHttpController.TAG, "GBA process end");
                                    return;
                                }
                                IMSLog.d(GbaHttpController.TAG, "HTTP digest without GBA");
                                GbaHttpController.this.sendRequestWithAuthorization(url2, httpRequestParams5, result, httpRequestParams5.getUserName(), httpRequestParams5.getPassword(), false);
                                return;
                            }
                            IMSLog.e(GbaHttpController.TAG, "sendRequestWithAuthorization(): onComplete: missing header: WWW-Authenticate");
                            httpRequestParams5.getCallback().onComplete(httpResponseParams);
                            return;
                        } else {
                            IMSLog.e(GbaHttpController.TAG, "onComplete: The response status code of 2nd time naf request is not 200");
                            httpRequestParams5.getCallback().onComplete(httpResponseParams);
                        }
                        LastAuthInfo lastAuthInfo2 = GbaHttpController.this.getLastAuthInfo(url2.toString(), httpRequestParams5.getPhoneId());
                        if (lastAuthInfo2 != null) {
                            lastAuthInfo2.btid = authHeader2;
                            lastAuthInfo2.gbaKey = str4;
                            Map<String, List<String>> responseHeader2 = result.getHeaders();
                            List<String> authInfoHeaders = responseHeader2.get("Authentication-Info");
                            if (authInfoHeaders != null) {
                                String nextNonce = new AuthInfoHeaderParser().parseHeaderValue(authInfoHeaders.get(0)).getNextNonce();
                                if (!TextUtils.isEmpty(nextNonce)) {
                                    lastAuthInfo2.nextNonce = nextNonce;
                                }
                            }
                            List<String> etagInfo = responseHeader2.get(HttpController.HEADER_ETAG);
                            if (etagInfo != null) {
                                String etagValue = etagInfo.get(0);
                                if (!TextUtils.isEmpty(etagValue)) {
                                    lastAuthInfo2.etag = etagValue;
                                }
                            }
                            HashMap access$800 = GbaHttpController.this.mLastAuthInfoMap;
                            access$800.put(GbaUtility.getNafUrl(url2.toString()) + SimUtil.getSubId(httpRequestParams5.getPhoneId()), lastAuthInfo2);
                        }
                        httpRequestParams5.getCallback().onComplete(httpResponseParams);
                    }

                    public void onFail(IOException arg1) {
                        String access$000 = GbaHttpController.TAG;
                        IMSLog.d(access$000, "The Second time naf request onFail: " + arg1.getMessage());
                        if (GbaHttpController.this.mGbaServiceModule != null) {
                            GbaHttpController.this.mGbaServiceModule.resetGbaKey(str5, httpRequestParams5.getPhoneId());
                        }
                        LastAuthInfo lastAuthInfo = GbaHttpController.this.getLastAuthInfo(url2.toString(), httpRequestParams5.getPhoneId());
                        if (lastAuthInfo != null) {
                            lastAuthInfo.btid = null;
                            lastAuthInfo.gbaKey = null;
                            lastAuthInfo.lastNafResult = null;
                            lastAuthInfo.digestAuth = null;
                            lastAuthInfo.nextNonce = null;
                            lastAuthInfo.LifeTime = null;
                        }
                        httpRequestParams5.getCallback().onFail(arg1);
                    }
                }, requestParams);
                if (requestParams.getMethod() == HttpRequestParams.Method.PUT) {
                    nafRequestParams.setPostBody(requestParams.getPostBody());
                }
                if (requestParams.getDns() != null) {
                    if (requestParams.getIpVersion() > 0) {
                        DnsController dns = (DnsController) requestParams.getDns();
                        dns.setNaf(true);
                        requestParams.setDns(dns);
                    } else {
                        HttpRequestParams httpRequestParams6 = requestParams;
                    }
                    nafRequestParams.setDns(requestParams.getDns());
                } else {
                    HttpRequestParams httpRequestParams7 = requestParams;
                }
                IMSLog.c(LogClass.UT_HTTP, Integer.toString(nafRequestParams.getPhoneId()) + ",>," + nafRequestParams.getMethodString());
                HttpController.getInstance().execute(nafRequestParams);
                loggingHttpMessage(nafRequestParams.toString(), 0);
                return;
            }
            requestParams.getCallback().onFail(new IOException("realm or Qop is null"));
            return;
        }
        IMSLog.e(TAG, "sendRequestWithAuthorization(): missing header: WWW-Authenticate");
        requestParams.getCallback().onComplete(result);
    }

    /* access modifiers changed from: private */
    public boolean useGba(HttpRequestParams requestParams) {
        Map headers = requestParams.getHeaders();
        if (headers == null) {
            IMSLog.d(TAG, "useGba(): no headers");
            return false;
        }
        String userAgent = headers.get("User-Agent");
        if (userAgent == null) {
            IMSLog.d(TAG, "useGba(): no headerUser-Agent");
            return false;
        }
        String str = TAG;
        IMSLog.d(str, "useGba(): User-Agent: " + userAgent);
        return userAgent.contains(HttpController.VAL_3GPP_GBA);
    }

    public boolean isKeyExpired(Date keyLifeTime) {
        if (keyLifeTime == null) {
            return true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        Date currentdate = new Date();
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (keyLifeTime.getTime() > currentdate.getTime() + (((long) 0) * 1000)) {
            return false;
        }
        return true;
    }

    public void sendBsfRequest(String bsfServer, int bsfPort, String username, String imei, String realm, byte[] gbaType, byte[] nafId, boolean isGbaSupported, HttpRequestParams requestParams) {
        String str = bsfServer;
        String url = buildUrl(requestParams.getPhoneId(), requestParams.getUseTls(), str, bsfPort);
        if (this.mGbaServiceModule == null) {
            this.mGbaServiceModule = ImsRegistry.getGbaService();
        }
        Map<String, String> requestHeader = new HashMap<>();
        requestHeader.put(HttpController.HEADER_HOST, str);
        StringBuilder sb = new StringBuilder();
        sb.append("GBA-service; 0.1; ");
        sb.append(isGbaSupported ? "3gpp-gba-uicc" : HttpController.VAL_3GPP_GBA);
        requestHeader.put("User-Agent", sb.toString());
        requestHeader.put("Authorization", AuthorizationHeader.getAuthorizationHeader(username, realm, "/", "", ""));
        if (requestParams.getUseImei()) {
            requestHeader.put(HttpController.HEADER_X_TMUS_IMEI, imei);
        } else {
            String str2 = imei;
        }
        final boolean z = isGbaSupported;
        final HttpRequestParams httpRequestParams = requestParams;
        final String str3 = url;
        final String str4 = username;
        final String str5 = bsfServer;
        final String str6 = imei;
        final byte[] bArr = gbaType;
        final byte[] bArr2 = nafId;
        HttpRequestParams bsfRequestParams = makeHttpRequestParams(HttpRequestParams.Method.GET, url, requestHeader, new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                HttpResponseParams httpResponseParams = result;
                if (httpResponseParams == null) {
                    IMSLog.e(GbaHttpController.TAG, "sendBsfRequest(): onComplete: response build failure");
                    GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z, httpResponseParams);
                    return;
                }
                GbaHttpController.this.loggingHttpMessage(result.toString(), 1);
                int statusCode = result.getStatusCode();
                IMSLog.c(LogClass.UT_HTTP, Integer.toString(httpRequestParams.getPhoneId()) + ",<," + Integer.toString(statusCode));
                if (statusCode != 401) {
                    String access$000 = GbaHttpController.TAG;
                    IMSLog.e(access$000, "sendBsfRequest(): onComplete: unexpected response code: " + statusCode);
                    GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z, httpResponseParams);
                    return;
                }
                Map<String, List<String>> responseHeader = result.getHeaders();
                List<String> wwwAuthHeaders = responseHeader.get("WWW-Authenticate");
                if (wwwAuthHeaders == null || wwwAuthHeaders.size() == 0) {
                    wwwAuthHeaders = responseHeader.get("WWW-Authenticate".toLowerCase());
                }
                if (wwwAuthHeaders == null || wwwAuthHeaders.size() == 0) {
                    IMSLog.e(GbaHttpController.TAG, "sendBsfRequest(): onComplete: missing header: WWW-Authenticate");
                    GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z, httpResponseParams);
                    return;
                }
                WwwAuthenticateHeader wwwAuthParsedHeader = new WwwAuthHeaderParser().parseHeaderValue(wwwAuthHeaders.get(0));
                GbaHttpController.this.sendBsfRequestWithAuthorization(str3, str4, wwwAuthParsedHeader, str5, str6, bArr, bArr2, z, httpRequestParams);
            }

            public void onFail(IOException arg1) {
                IMSLog.c(LogClass.UT_ERROR_HANDLE, Integer.toString(httpRequestParams.getPhoneId()) + "," + arg1.getMessage());
                GbaHttpController.this.gbaFailCallbacksDeQ(arg1);
            }
        }, requestParams);
        if (UtUtils.isBsfDisableTls(bsfRequestParams.getPhoneId())) {
            IMSLog.i(TAG, "sendBsfRequest() Bsf disable Tls");
            bsfRequestParams.setUseTls(false);
        }
        IMSLog.c(LogClass.UT_HTTP, Integer.toString(bsfRequestParams.getPhoneId()) + ",>," + bsfRequestParams.getMethodString());
        HttpController.getInstance().execute(bsfRequestParams);
        loggingHttpMessage(bsfRequestParams.toString(), 0);
    }

    /* access modifiers changed from: private */
    public void sendBsfRequestWithAuthorization(String url, String username, WwwAuthenticateHeader wwwAuthParsedHeader, String bsfServer, String imei, byte[] gbaType, byte[] nafId, boolean isGbaSupported, HttpRequestParams requestParams) {
        Map<String, String> requestHeader;
        HttpRequestParams.HttpRequestCallback BsfRequestCallback;
        boolean z = isGbaSupported;
        String str = TAG;
        IMSLog.d(str, "GBA: sendBsfRequestWithAuthorization(): username: " + username);
        GbaData akakeys = this.mGbaServiceModule.getPassword(wwwAuthParsedHeader.getNonce(), z, requestParams.getPhoneId());
        Map<String, String> requestHeader2 = new HashMap<>();
        requestHeader2.put(HttpController.HEADER_HOST, bsfServer);
        StringBuilder sb = new StringBuilder();
        sb.append("GBA-service; 0.1; ");
        sb.append(z ? "3gpp-gba-uicc" : HttpController.VAL_3GPP_GBA);
        requestHeader2.put("User-Agent", sb.toString());
        if (requestParams.getUseImei()) {
            requestHeader2.put(HttpController.HEADER_X_TMUS_IMEI, imei);
        } else {
            String str2 = imei;
        }
        if (akakeys == null) {
            gbaFailCallbacksDeQ(new IOException("GBA FAIL akakeys null"));
            return;
        }
        String akaPassword = akakeys.getPassword();
        if (akaPassword.startsWith("dc")) {
            String str3 = TAG;
            IMSLog.i(str3, "sendBsfRequestWithAuthorization - AUTH_SQN_FAIL, akaPassword = " + akaPassword);
            byte[] simResponse = StrUtil.hexStringToBytes(akaPassword);
            if (simResponse.length > 1) {
                byte autslen = simResponse[1];
                if (autslen > 0) {
                    requestHeader2.put("Authorization", AuthorizationHeader.getAuthorizationHeader(username, "", wwwAuthParsedHeader.getRealm(), "GET", "/", new String(Base64.encodeBase64(Arrays.copyOfRange(simResponse, 2, autslen + 2))), wwwAuthParsedHeader));
                    final boolean z2 = isGbaSupported;
                    final HttpRequestParams httpRequestParams = requestParams;
                    byte[] bArr = simResponse;
                    final String str4 = url;
                    String str5 = akaPassword;
                    final String akaPassword2 = username;
                    final String str6 = bsfServer;
                    final String str7 = imei;
                    requestHeader = requestHeader2;
                    final byte[] bArr2 = gbaType;
                    final byte[] bArr3 = nafId;
                    BsfRequestCallback = new HttpRequestParams.HttpRequestCallback() {
                        public void onComplete(HttpResponseParams result) {
                            HttpResponseParams httpResponseParams = result;
                            if (httpResponseParams == null) {
                                IMSLog.e(GbaHttpController.TAG, "sendBsfRequestWithAuthorization(): onComplete: response build failure");
                                GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z2, httpResponseParams);
                                return;
                            }
                            GbaHttpController.this.loggingHttpMessage(result.toString(), 1);
                            int statusCode = result.getStatusCode();
                            IMSLog.c(LogClass.UT_HTTP, Integer.toString(httpRequestParams.getPhoneId()) + ",<," + Integer.toString(statusCode));
                            if (statusCode != 401) {
                                String access$000 = GbaHttpController.TAG;
                                IMSLog.e(access$000, "sendBsfRequestWithAuthorization(): onComplete: unexpected response code: " + statusCode);
                                GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z2, httpResponseParams);
                                return;
                            }
                            Map<String, List<String>> responseHeader = result.getHeaders();
                            List<String> wwwAuthHeaders = responseHeader.get("WWW-Authenticate");
                            if (wwwAuthHeaders == null || wwwAuthHeaders.size() == 0) {
                                wwwAuthHeaders = responseHeader.get("WWW-Authenticate".toLowerCase());
                            }
                            if (wwwAuthHeaders == null || wwwAuthHeaders.size() == 0) {
                                IMSLog.e(GbaHttpController.TAG, "sendBsfRequestWithAuthorization(): onComplete: missing header: WWW-Authenticate");
                                GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z2, httpResponseParams);
                                return;
                            }
                            WwwAuthenticateHeader wwwAuthParsedHeader = new WwwAuthHeaderParser().parseHeaderValue(wwwAuthHeaders.get(0));
                            GbaHttpController.this.sendBsfRequestWithAuthorization(str4, akaPassword2, wwwAuthParsedHeader, str6, str7, bArr2, bArr3, z2, httpRequestParams);
                        }

                        public void onFail(IOException arg1) {
                            IMSLog.c(LogClass.UT_ERROR_HANDLE, Integer.toString(httpRequestParams.getPhoneId()) + "," + arg1.getMessage());
                            GbaHttpController.this.gbaFailCallbacksDeQ(arg1);
                        }
                    };
                } else {
                    IMSLog.d(TAG, "Invalid autslen.");
                    gbaFailCallbacksDeQ((IOException) null);
                    return;
                }
            } else {
                IMSLog.d(TAG, "Invalid simResponse.");
                gbaFailCallbacksDeQ((IOException) null);
                return;
            }
        } else {
            String str8 = akaPassword;
            requestHeader = requestHeader2;
            requestHeader.put("Authorization", AuthorizationHeader.getAuthorizationHeader(username, akakeys.getPassword(), wwwAuthParsedHeader.getRealm(), "GET", "/", wwwAuthParsedHeader));
            final boolean z3 = isGbaSupported;
            final HttpRequestParams httpRequestParams2 = requestParams;
            final WwwAuthenticateHeader wwwAuthenticateHeader = wwwAuthParsedHeader;
            final byte[] bArr4 = gbaType;
            final byte[] bArr5 = nafId;
            final GbaData gbaData = akakeys;
            BsfRequestCallback = new HttpRequestParams.HttpRequestCallback() {
                public void onComplete(HttpResponseParams result) {
                    LastAuthInfo lastAuthInfo;
                    boolean isSupportTls;
                    HttpResponseParams httpResponseParams = result;
                    if (httpResponseParams == null) {
                        IMSLog.e(GbaHttpController.TAG, "sendBsfRequestWithAuthorization(): onComplete: response build failure");
                        GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z3, httpResponseParams);
                        return;
                    }
                    GbaHttpController.this.loggingHttpMessage(result.toString(), 1);
                    int statusCode = result.getStatusCode();
                    IMSLog.c(LogClass.UT_HTTP, Integer.toString(httpRequestParams2.getPhoneId()) + ",<," + Integer.toString(statusCode));
                    if (statusCode != 200) {
                        IMSLog.e(GbaHttpController.TAG, "sendBsfRequestWithAuthorization(): onComplete: response code: " + statusCode);
                        GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z3, httpResponseParams);
                        return;
                    }
                    BsfResponse response = (BsfResponse) HttpResponseUtils.parseXmlResponse(httpResponseParams, BsfResponse.class, false);
                    if (response == null) {
                        IMSLog.e(GbaHttpController.TAG, "sendBsfRequestWithAuthorization(): onComplete: parseXmlResponse failure");
                        GbaHttpController.this.gbaCallbacksDeQ((String) null, (String) null, z3, httpResponseParams);
                        return;
                    }
                    String btid = response.getBtid();
                    String lifetime = response.getLifetime();
                    if (GbaHttpController.this.mNafRequestParams != null) {
                        GbaHttpController gbaHttpController = GbaHttpController.this;
                        lastAuthInfo = gbaHttpController.getLastAuthInfo(gbaHttpController.mNafRequestParams.getUrl(), GbaHttpController.this.mNafRequestParams.getPhoneId());
                    } else {
                        lastAuthInfo = GbaHttpController.this.getLastAuthInfo(httpRequestParams2.getUrl(), httpRequestParams2.getPhoneId());
                    }
                    if (lastAuthInfo != null) {
                        lastAuthInfo.LifeTime = lifetime;
                    }
                    boolean isSupportTls2 = httpRequestParams2.getUseTls();
                    if (TextUtils.isEmpty(httpRequestParams2.getUrl()) || !httpRequestParams2.getUrl().contains(OMAGlobalVariables.HTTPS)) {
                        isSupportTls = isSupportTls2;
                    } else {
                        isSupportTls = true;
                    }
                    String gbaKey = GbaHttpController.this.mGbaServiceModule.storeGbaDataAndGenerateKey(btid, lifetime, wwwAuthenticateHeader.getNonce(), httpRequestParams2.getCipherSuite(), bArr4, bArr5, gbaData, isSupportTls, httpRequestParams2.getPhoneId());
                    IMSLog.d(GbaHttpController.TAG, "sendBsfRequestWithAuthorization(): btid: " + btid + ", gbaKey: " + gbaKey);
                    GbaHttpController.this.gbaCallbacksDeQ(btid, gbaKey, z3, httpResponseParams);
                }

                public void onFail(IOException arg1) {
                    IMSLog.c(LogClass.UT_ERROR_HANDLE, Integer.toString(httpRequestParams2.getPhoneId()) + "," + arg1.getMessage());
                    GbaHttpController.this.gbaFailCallbacksDeQ(arg1);
                }
            };
        }
        HttpRequestParams bsfRequestParams = makeHttpRequestParams(HttpRequestParams.Method.GET, url, requestHeader, BsfRequestCallback, requestParams);
        if (UtUtils.isBsfDisableTls(bsfRequestParams.getPhoneId())) {
            IMSLog.i(TAG, "GBA: Bsf disable Tls");
            bsfRequestParams.setUseTls(false);
        }
        IMSLog.c(LogClass.UT_HTTP, Integer.toString(bsfRequestParams.getPhoneId()) + ",>," + bsfRequestParams.getMethodString());
        HttpController.getInstance().execute(bsfRequestParams);
        loggingHttpMessage(bsfRequestParams.toString(), 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x001b, code lost:
        if (com.sec.internal.helper.SimUtil.getSimMno(r6).isOneOf(com.sec.internal.constants.Mno.SPARK) != false) goto L_0x0024;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String buildUrl(int r6, boolean r7, java.lang.String r8, int r9) {
        /*
            r5 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r1 = 443(0x1bb, float:6.21E-43)
            if (r9 == r1) goto L_0x0024
            if (r7 == 0) goto L_0x001e
            com.sec.internal.constants.Mno r1 = com.sec.internal.helper.SimUtil.getSimMno(r6)
            r2 = 1
            com.sec.internal.constants.Mno[] r2 = new com.sec.internal.constants.Mno[r2]
            r3 = 0
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.SPARK
            r2[r3] = r4
            boolean r1 = r1.isOneOf(r2)
            if (r1 == 0) goto L_0x001e
            goto L_0x0024
        L_0x001e:
            java.lang.String r1 = "http://"
            r0.append(r1)
            goto L_0x0029
        L_0x0024:
            java.lang.String r1 = "https://"
            r0.append(r1)
        L_0x0029:
            r0.append(r8)
            r1 = 58
            r0.append(r1)
            r0.append(r9)
            r1 = 47
            r0.append(r1)
            java.lang.String r1 = r0.toString()
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.httpclient.GbaHttpController.buildUrl(int, boolean, java.lang.String, int):java.lang.String");
    }

    /* access modifiers changed from: private */
    public void gbaCallbacksDeQ(String btid, String gbaKey, boolean isGbaSupported, HttpResponseParams result) {
        while (!this.mGbaServiceModule.getGbaCallbacks().isEmpty()) {
            this.mGbaServiceModule.getGbaCallbacks().remove(0).onComplete(btid, gbaKey, isGbaSupported, result);
        }
    }

    /* access modifiers changed from: private */
    public void gbaFailCallbacksDeQ(IOException arg1) {
        while (!this.mGbaServiceModule.getGbaCallbacks().isEmpty()) {
            this.mGbaServiceModule.getGbaCallbacks().remove(0).onFail(arg1);
        }
    }

    /* access modifiers changed from: private */
    public void loggingHttpMessage(String message, int direction) {
        if (message != null && !Debug.isProductShip()) {
            ImsRegistry.getImsDiagMonitor().onIndication(1, hidePrivateInfoFromMsg(message.replaceAll("HttpRequestParams.*\r\n.*mMethod: ", "").replaceAll("HttpResponseParams.*\r\n.*mStatusCode=", "HTTP/1.1 ").replaceAll("\r\n.*mUrl: ", " ")), 100, direction, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.getDefault()).format(new Date()), "", "", "");
        }
    }

    private String hidePrivateInfoFromMsg(String msg) {
        if (!Debug.isProductShip()) {
            return msg;
        }
        return msg.replaceAll("sip:+[0-9+-]+", "sip:xxxxxxxxxxxxxxx").replaceAll("tel:+[0-9+-]+", "tel:xxxxxxxxxxxxxxx").replaceAll("imei:+[0-9+-]+", "imei:xxxxxxxx").replaceAll("username=\"+[^\"]+", "username=xxxxxxxxxxxxxxx").replaceAll("\"+[0-9+-]+\"", "\"xxxxxxxxxxxxxxx\"").replaceAll("target>+.+</.*target", "target>xxxxxxxxxxxxxxx</target");
    }

    private HttpRequestParams makeHttpRequestParams(HttpRequestParams.Method method, String url, Map<String, String> headers, HttpRequestParams.HttpRequestCallback callback, HttpRequestParams requestParams) {
        HttpRequestParams newRequestParams = new HttpRequestParams(method, url, headers, callback);
        if (requestParams.getSocketFactory() != null) {
            newRequestParams.setSocketFactory(requestParams.getSocketFactory());
        }
        if (requestParams.getDns() != null) {
            newRequestParams.setDns(requestParams.getDns());
        }
        newRequestParams.setUseTls(requestParams.getUseTls());
        newRequestParams.setConnectionTimeout(requestParams.getConnectionTimeout());
        newRequestParams.setReadTimeout(requestParams.getReadTimeout());
        newRequestParams.setProxy(requestParams.getProxy());
        newRequestParams.setUseProxy(requestParams.getUseProxy());
        return newRequestParams;
    }

    /* access modifiers changed from: private */
    public static boolean isNeedCSFB(int statusCode, int phoneId) {
        if (SimUtil.getSimMno(phoneId) == Mno.TELECOM_ITALY && statusCode == 401) {
            return true;
        }
        return false;
    }

    private static String getAcceptEncoding(int phoneId) {
        Mno mno = SimUtil.getSimMno(phoneId);
        if (mno == Mno.H3G || mno == Mno.SMARTFREN || mno == Mno.TMOUS || mno == Mno.TELE2_RUSSIA) {
            return "";
        }
        return "*";
    }

    /* access modifiers changed from: private */
    public LastAuthInfo getLastAuthInfo(String url, int phoneId) {
        HashMap<String, LastAuthInfo> hashMap = this.mLastAuthInfoMap;
        return hashMap.get(GbaUtility.getNafUrl(url) + SimUtil.getSubId(phoneId));
    }

    /* access modifiers changed from: private */
    public void storeLastAuthInfo(HttpResponseParams result, HttpRequestParams requestParams) {
        LastAuthInfo newLastAuthInfo = new LastAuthInfo();
        newLastAuthInfo.digestAuth = new DigestAuth();
        newLastAuthInfo.lastNafResult = result;
        HashMap<String, LastAuthInfo> hashMap = this.mLastAuthInfoMap;
        hashMap.put(GbaUtility.getNafUrl(requestParams.getUrl()) + SimUtil.getSubId(requestParams.getPhoneId()), newLastAuthInfo);
    }
}
