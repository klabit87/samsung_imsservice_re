package com.sec.internal.ims.entitlement.util;

import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import com.squareup.okhttp.Dns;
import java.io.IOException;
import java.util.Map;
import javax.net.SocketFactory;
import org.json.JSONArray;

public class HttpHelper {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = HttpHelper.class.getSimpleName();
    protected HttpController mHttpController = HttpController.getInstance();

    public void executeNSDSRequest(String url, Map<String, String> headers, JSONArray jsonArray, Message onComplete, SocketFactory socketFactory, Dns dns) {
        HttpRequestParams requestParams = createHttpRequestParams(HttpRequestParams.Method.POST, url, headers, onComplete);
        if (socketFactory != null) {
            requestParams.setSocketFactory(socketFactory);
        }
        if (dns != null) {
            requestParams.setDns(dns);
        }
        requestParams.setPostBody(jsonArray);
        this.mHttpController.execute(requestParams);
    }

    private HttpRequestParams createHttpRequestParams(HttpRequestParams.Method method, String url, Map<String, String> headers, Message onComplete) {
        return new HttpRequestParams(method, url, headers, buildHttpRequestCallback(onComplete));
    }

    private HttpRequestParams.HttpRequestCallback buildHttpRequestCallback(final Message OnComplete) {
        return new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                Log.i(HttpHelper.LOG_TAG, "Http request 200 ok");
                OnComplete.obj = result;
                OnComplete.sendToTarget();
            }

            public void onFail(IOException ie) {
                IMSLog.c(LogClass.ES_HTTP_FAIL, "HTP FAIL:" + ie.getMessage());
                HttpResponseParams result = new HttpResponseParams();
                result.setStatusReason(ie.getMessage());
                OnComplete.obj = result;
                OnComplete.sendToTarget();
            }
        };
    }
}
