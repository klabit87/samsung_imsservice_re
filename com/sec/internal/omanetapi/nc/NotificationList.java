package com.sec.internal.omanetapi.nc;

import android.net.Uri;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nc.data.LongPollingRequestParameters;

public class NotificationList extends BaseNCRequest {
    private static final String TAG = NotificationList.class.getSimpleName();
    private static final long serialVersionUID = 1611862466283057959L;

    public NotificationList(String serverRoot, String apiVersion, String userId, String serverUrl) {
        super(serverRoot, apiVersion, userId);
        buildAPISpecificURLFromBase();
    }

    public NotificationList(String baseUrl) {
        super(baseUrl);
        String str = TAG;
        Log.i(str, "NotificationList: baseUrl: " + IMSLog.checker(this.mBaseUrl));
    }

    public void initPostRequest(LongPollingRequestParameters longPollingRequestParameters, boolean isJson) {
        OMAApiRequestParam.NotificationListRequest request = new OMAApiRequestParam.NotificationListRequest();
        request.longPollingRequestParameters = longPollingRequestParameters;
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        String str = TAG;
        Log.d(str, "initPostRequest " + isJson);
        HttpPostBody body = null;
        if (isJson) {
            this.mNCRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNCRequestHeaderMap);
            if (!ATTGlobalVariables.isGcmReplacePolling()) {
                body = new HttpPostBody(new GsonBuilder().serializeNulls().create().toJson(request));
            }
        }
        if (body != null) {
            Log.d(TAG, "initPostRequest");
            setPostBody(body);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        this.mBaseUrl = Uri.parse(this.mBaseUrl).buildUpon().build().toString();
        String str = TAG;
        Log.i(str, "NotificationList: baseUrl: " + IMSLog.checker(this.mBaseUrl));
    }
}
