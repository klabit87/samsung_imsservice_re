package com.sec.internal.ims.config.workflow;

import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WorkflowCookieHandler {
    private static final String LOG_TAG = WorkflowCookieHandler.class.getSimpleName();
    protected final CookieManager mCookieManager;
    protected int mPhoneId;
    protected WorkflowBase mWorkflowBase;

    public WorkflowCookieHandler(WorkflowBase base, int phoneId) {
        CookieManager cookieManager = new CookieManager();
        this.mCookieManager = cookieManager;
        this.mWorkflowBase = base;
        this.mPhoneId = phoneId;
        CookieHandler.setDefault(cookieManager);
    }

    /* access modifiers changed from: protected */
    public URI getUri() {
        try {
            return new URI(this.mWorkflowBase.mSharedInfo.getUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public List<HttpCookie> getCookie(URI uri) {
        return this.mCookieManager.getCookieStore().get(uri);
    }

    /* access modifiers changed from: protected */
    public void displayCookieInfo(HttpCookie cookie) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "cookie name:" + cookie.getName());
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "cookie value:" + cookie.getValue());
        String str3 = LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.i(str3, i3, "cookie domain:" + cookie.getDomain());
        String str4 = LOG_TAG;
        int i4 = this.mPhoneId;
        IMSLog.i(str4, i4, "cookie path:" + cookie.getPath());
        String str5 = LOG_TAG;
        int i5 = this.mPhoneId;
        IMSLog.i(str5, i5, "cookie max age:" + cookie.getMaxAge());
    }

    /* access modifiers changed from: protected */
    public boolean isCookie(IHttpAdapter.Response response) {
        if (response.getHeader().containsKey("Set-Cookie")) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "cookie exist");
            for (HttpCookie cookie : getCookie(getUri())) {
                displayCookieInfo(cookie);
            }
            return true;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "cookie does not exist");
        IMSLog.c(LogClass.WFB_NO_COOKIE, this.mWorkflowBase.mPhoneId + ",NOC");
        WorkflowBase workflowBase = this.mWorkflowBase;
        workflowBase.addEventLog(LOG_TAG + ": cookie does not exist");
        return false;
    }

    /* access modifiers changed from: protected */
    public void clearCookie() {
        URI uri = getUri();
        for (HttpCookie cookie : getCookie(uri)) {
            this.mCookieManager.getCookieStore().remove(uri, cookie);
            displayRemovedCookieInfo(cookie.getName());
        }
    }

    /* access modifiers changed from: protected */
    public void displayRemovedCookieInfo(String cookieName) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "removed cookie: " + cookieName);
    }

    /* access modifiers changed from: protected */
    public void handleCookie(IHttpAdapter.Response response) {
        if (response != null && response.getHeader().containsKey("Set-Cookie")) {
            Mno mno = SimUtil.getMno();
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleCookie: cookie exists, adding in header");
            if (mno == Mno.CMCC) {
                List<String> newCookie = new ArrayList<>();
                List<String> cookie = this.mWorkflowBase.mSharedInfo.getHttpResponse().getHeader().get("Set-Cookie");
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleCookie: cookie = " + cookie);
                for (String cookieValue : cookie) {
                    String[] values = cookieValue.split(";");
                    StringBuilder sb = new StringBuilder();
                    for (String obj : values) {
                        if (!obj.startsWith("Max-Age")) {
                            if (sb.length() == 0) {
                                sb.append(obj);
                            } else {
                                sb.append(";" + obj);
                            }
                        }
                    }
                    newCookie.add(sb.toString());
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleCookie: remove Max-Age = " + newCookie);
                this.mWorkflowBase.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, newCookie);
            } else if (!ConfigUtil.shallUsePreviousCookie(response.getStatusCode(), mno)) {
                StringBuilder sb2 = new StringBuilder();
                for (String headerValue : response.getHeader().get("Set-Cookie")) {
                    for (String cookie2 : headerValue.split(";")) {
                        String cookie3 = cookie2.trim();
                        if (sb2.length() != 0) {
                            sb2.append("; ");
                        }
                        sb2.append(cookie3);
                    }
                }
                if (sb2.length() != 0) {
                    List<String> valueList = new ArrayList<>(1);
                    valueList.add(sb2.toString());
                    this.mWorkflowBase.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, valueList);
                }
            }
        }
    }
}
