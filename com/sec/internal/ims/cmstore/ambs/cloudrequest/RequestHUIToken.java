package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class RequestHUIToken extends BaseProvisionAPIRequest {
    /* access modifiers changed from: private */
    public static final String TAG = RequestHUIToken.class.getSimpleName();
    private static final long serialVersionUID = -5155400496558292974L;
    /* access modifiers changed from: private */
    public transient HttpCookie cookieServerIDInBody;
    /* access modifiers changed from: private */
    public transient HttpCookie cookieTokenInBody;

    public RequestHUIToken(final IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String strBody = result.getDataString();
                String access$000 = RequestHUIToken.TAG;
                Log.d(access$000, "onComplete StatusCode: " + result.getStatusCode() + " strbody: " + IMSLog.checker(strBody));
                if (result.getStatusCode() != 200 || TextUtils.isEmpty(strBody)) {
                    if (result.getStatusCode() == 503 || result.getStatusCode() == 429) {
                        int retryAfter = RequestHUIToken.this.checkRetryAfter(result);
                        if (retryAfter > 0) {
                            callFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, retryAfter);
                            return;
                        }
                        return;
                    }
                    RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_HUI_JSON);
                } else if (strBody.indexOf(ATTConstants.ATTErrorNames.encoreesb) < 0) {
                    Log.d(RequestHUIToken.TAG, "NOT 6014");
                    CloudMessagePreferenceManager.getInstance().saveIfHUI6014Err(false);
                    String msToken = RequestHUIToken.this.getParameter(strBody, "msToken=\"", "\"");
                    String serverID = RequestHUIToken.this.getParameter(strBody, "serverID=\"", "\"");
                    String redirectDomain = RequestHUIToken.this.getParameter(strBody, "redirectDomain=\"", "\"");
                    String cometRedirectDomain = RequestHUIToken.this.getParameter(strBody, "cometRedirectDomain=\"", "\"");
                    String access$0002 = RequestHUIToken.TAG;
                    Log.d(access$0002, "msToken=" + msToken + ", serverID=" + serverID + ", redirectDomain=" + redirectDomain + ", cometRedirectDomain" + cometRedirectDomain);
                    if (!ATTGlobalVariables.isGcmReplacePolling()) {
                        HttpCookie unused = RequestHUIToken.this.cookieTokenInBody = new HttpCookie("MSToken", msToken);
                        RequestHUIToken.this.cookieTokenInBody.setDomain(redirectDomain);
                        RequestHUIToken.this.cookieTokenInBody.setVersion(0);
                        HttpCookie unused2 = RequestHUIToken.this.cookieServerIDInBody = new HttpCookie("SERVERID", serverID);
                        RequestHUIToken.this.cookieServerIDInBody.setDomain(redirectDomain);
                        RequestHUIToken.this.cookieServerIDInBody.setVersion(0);
                    }
                    if (!TextUtils.isEmpty(msToken) && !TextUtils.isEmpty(redirectDomain)) {
                        CloudMessagePreferenceManager.getInstance().saveMsgStoreSessionId(msToken);
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            String access$0003 = RequestHUIToken.TAG;
                            Log.d(access$0003, "nms value in SP =" + CloudMessagePreferenceManager.getInstance().getNmsHost());
                            CloudMessagePreferenceManager.getInstance().saveNmsHost(redirectDomain);
                        } else {
                            RequestHUIToken requestHUIToken = RequestHUIToken.this;
                            requestHUIToken.updateCookie(requestHUIToken.getUrl());
                            CloudMessagePreferenceManager.getInstance().saveNmsHost(redirectDomain);
                            CloudMessagePreferenceManager.getInstance().saveNcHost(cometRedirectDomain);
                        }
                        String oldRedirectDomain = CloudMessagePreferenceManager.getInstance().getRedirectDomain();
                        CloudMessagePreferenceManager.getInstance().saveRedirectDomain(redirectDomain);
                        if (!TextUtils.isEmpty(oldRedirectDomain) && !redirectDomain.equals(oldRedirectDomain)) {
                            Log.d(RequestHUIToken.TAG, "redirect domain changed, need mail reset.");
                            callFlowListener.onGoToEvent(EnumProvision.ProvisionEventType.MAILBOX_MIGRATION_RESET.getId(), (Object) null);
                        }
                        CloudMessagePreferenceManager.getInstance().saveLastApiRequestCreateAccount(false);
                        RequestHUIToken.this.goSuccessfulCall();
                    }
                } else if (strBody.indexOf(ATTConstants.ATTErrorNames.ERR_ENCORE_METASWITCH_ACCOUNT_NOT_PROVISIONED) >= 0) {
                    CloudMessagePreferenceManager.getInstance().saveIfHUI6014Err(true);
                    if (CloudMessagePreferenceManager.getInstance().isLastAPIRequestCreateAccount()) {
                        Log.d(RequestHUIToken.TAG, "Last successful API call was CreateServiceAccount");
                        RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.LAST_RETRY_CREATE_ACCOUNT);
                        return;
                    }
                    RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_ENCORE_METASWITCH_ACCOUNT_NOT_PROVISIONED);
                } else {
                    RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_HUI_JSON);
                }
            }

            public void onFail(IOException arg1) {
                String access$000 = RequestHUIToken.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                RequestHUIToken.this.goFailedCall();
            }
        });
    }

    /* access modifiers changed from: private */
    public String getParameter(String url, String name, String strSep) {
        int iBegin = url.indexOf(name);
        if (iBegin < 0) {
            return null;
        }
        int iBegin2 = iBegin + name.length();
        int iEnd = url.indexOf(strSep, iBegin2);
        String subStr = url.substring(iBegin2);
        if (iEnd > 0) {
            return url.substring(iBegin2, iEnd);
        }
        return subStr;
    }

    public void updateUrl() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("https://");
            sb.append(ATTGlobalVariables.MSG_PROXY_HOST_NAME);
            sb.append("/encore/security/GetHUIMSToken?clientType=handset&ApplicationId=");
            sb.append(URLEncoder.encode(ATTGlobalVariables.APPLICATION_ID, "UTF-8"));
            sb.append("&ContextInfo=");
            sb.append(URLEncoder.encode("version=" + ATTGlobalVariables.VERSION_NAME, "UTF-8"));
            setUrl(sb.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void updateCookie(String url) {
        Log.d(TAG, "updateCookie");
        try {
            this.mCookieStore.removeAll();
            URI newUri = new URI(url);
            this.mCookieStore.add(newUri, this.cookieTokenInBody);
            this.mCookieStore.add(newUri, this.cookieServerIDInBody);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new RequestHUIToken(callback, cloudMessageManagerHelper);
    }
}
