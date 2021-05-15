package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiRequestParam;
import com.sec.internal.omanetapi.nms.data.NmsSubscriptionUpdate;

public class IndividualSubscription extends BaseNMSRequest {
    public static final String TAG = IndividualSubscription.class.getSimpleName();
    private static final long serialVersionUID = -461469967960054356L;
    private String mSubscriptionId;

    public IndividualSubscription(String serverRoot, String apiVersion, String storeName, String boxId, String subscriptionId) {
        super(serverRoot, apiVersion, storeName, boxId);
        this.mSubscriptionId = subscriptionId;
        buildAPISpecificURLFromBase();
    }

    public IndividualSubscription(String url) {
        super(url);
    }

    public void initPostRequest(NmsSubscriptionUpdate update, boolean isJson) {
        setUrl(this.mBaseUrl);
        setMethod(HttpRequestParams.Method.POST);
        setFollowRedirects(false);
        HttpPostBody body = null;
        if (isJson) {
            this.mNMSRequestHeaderMap.put("Content-Type", "application/json");
            setHeaders(this.mNMSRequestHeaderMap);
            OMAApiRequestParam.NmsSubscriptionUpdateRequest request = new OMAApiRequestParam.NmsSubscriptionUpdateRequest();
            request.nmsSubscriptionUpdate = update;
            body = new HttpPostBody(new Gson().toJson(request));
        }
        if (body != null) {
            setPostBody(body);
        }
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder builder = Uri.parse(this.mBaseUrl).buildUpon();
        builder.appendPath("subscriptions");
        builder.appendPath(this.mSubscriptionId);
        this.mBaseUrl = builder.build().toString();
        Log.i(TAG, IMSLog.checker(this.mBaseUrl));
    }
}
